import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as G, useKeyboardBinding as me, useTLUpload as Ve, TLChild as q, useI18N as ue, useTLDataUrl as Ke, scrollToAnchor as Tn, useStandaloneKeyboardScope as Be, KeyboardScopeProvider as jt, useFocusTrap as At, CMD_VALUE_CHANGED as tt, anchoredOverlayProps as Rn, register as U } from "tl-react-bridge";
const { useCallback: Ht, useRef: Ln } = e, xn = 300, In = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: xn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), c = ne(), s = Ln(!1), u = Ht(
    (w) => {
      s.current = !0, a(w.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, i = Ht(async () => {
    await o(), r && s.current && (s.current = !1, c("commit"));
  }, [o, r, c]), d = t.multiline === !0;
  if (t.editable === !1) {
    const w = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: w,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const f = t.hasError === !0, m = t.hasWarnings === !0, h = t.errorMessage, g = [
    "tlReactTextInput",
    d ? "tlReactTextInput--multiline" : "",
    f ? "tlReactTextInput--error" : "",
    !f && m ? "tlReactTextInput--warning" : ""
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
      "aria-invalid": f || void 0,
      title: f && h ? h : void 0
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
      "aria-invalid": f || void 0,
      title: f && h ? h : void 0
    }
  ));
}, { useCallback: Wt } = e, Mn = 300, Pn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: Mn }), c = Wt(
    (f) => {
      a(f.target.value);
    },
    [a]
  ), s = Wt(() => {
    o();
  }, [o]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, i = t.errorMessage, d = [
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
      onBlur: s,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && i ? i : void 0
    }
  ));
}, { useCallback: Ut } = e, jn = 300, An = ({ controlId: l, state: t, config: n }) => {
  const [a, o, c] = De({ debounceMs: jn }), s = Ut(
    (m) => {
      const h = m.target.value;
      o(h === "" ? null : h);
    },
    [o]
  ), u = Ut(() => {
    c();
  }, [c]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, i = t.hasWarnings === !0, d = t.errorMessage, f = [
    "tlReactNumberInput",
    r ? "tlReactNumberInput--error" : "",
    !r && i ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: a != null ? String(a) : "",
      onChange: s,
      onBlur: u,
      disabled: t.disabled === !0,
      className: f,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: Bn } = e, On = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = Bn(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: Fn } = e, $n = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), c = Fn(
    (f) => {
      o(f.target.value || null);
    },
    [o]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const f = ((d = s.find((m) => m.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, f);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, i = [
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
      className: i,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((f) => /* @__PURE__ */ e.createElement("option", { key: f.value, value: f.value }, f.label))
  ));
}, { useCallback: Hn } = e, Wn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], c = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, i = Hn(
    (m) => {
      const h = o[m];
      a(h ? h.value : null);
    },
    [o, a]
  ), d = o.findIndex((m) => m.value === (n ?? null));
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlBooleanChoice tlBooleanChoice--immutable" }, d >= 0 ? o[d].label : "");
  const f = [
    "tlBooleanChoice",
    u ? "tlBooleanChoice--error" : "",
    !u && r ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: f + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: s,
      "aria-invalid": u || void 0,
      onChange: (m) => i(Number(m.target.value))
    },
    d < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((m, h) => /* @__PURE__ */ e.createElement("option", { key: h, value: String(h) }, m.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: f + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": u || void 0
    },
    o.map((m, h) => /* @__PURE__ */ e.createElement("label", { key: h, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: d === h,
        disabled: s,
        onChange: () => i(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, m.label)))
  );
}, { useCallback: Un, useRef: zn, useEffect: Vn } = e, Kn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, c = zn(null);
  Vn(() => {
    c.current && (c.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = Un(
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
        ref: c,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, r = t.hasWarnings === !0, i = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && r ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      ref: c,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0,
      "aria-checked": o && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function Se({ encoded: l, className: t }) {
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
const { useCallback: Yn } = e, Gn = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: c }) => {
  const s = G(), u = ne(), r = t ?? "click", i = n ?? s.label, d = a ?? s.image, f = o ?? s.disabled === !0, m = c ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, w = s.appearance, v = s.size, _ = s.navigateUrl, y = Yn(() => {
    if (_) {
      window.location.assign(_);
      return;
    }
    u(r);
  }, [u, r, _]), k = s.keyGesture;
  me(k, () => f || h ? !1 : (y(), !0));
  const L = m === "icon-only", E = m === "label-only" || m === "icon-label" || L && !d, C = g ?? (L ? i : void 0), b = C ? `text:${C}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: f,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (m === "label-only" ? " tlReactButton--labelOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": b,
      "aria-label": d || L ? i : void 0
    },
    d && /* @__PURE__ */ e.createElement(Se, { encoded: d, className: "tlReactButton__image" }),
    E && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  );
}, Xn = ({ controlId: l }) => {
  const t = G(), n = Ve(), a = e.useRef(null), [o, c] = e.useState(!1), s = t.label ?? "", u = t.image, r = t.disabled === !0, i = t.hidden === !0, d = t.displayMode ?? "label-only", f = t.appearance, m = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var L;
    r || o || (L = a.current) == null || L.click();
  }, [r, o]), w = e.useCallback(async (L) => {
    const E = L.target.files;
    if (!E || E.length === 0) return;
    const C = new FormData();
    for (let b = 0; b < E.length; b++)
      C.append("file", E[b], E[b].name);
    L.target.value = "", c(!0);
    try {
      await n(C);
    } finally {
      c(!1);
    }
  }, [n]), v = d === "icon-only", _ = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || v && !u, k = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: m && m !== "*" ? m : void 0,
      multiple: h || void 0,
      onChange: w,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: k,
      style: i ? { display: "none" } : void 0,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (f === "link" ? " tlReactButton--link" : "") + (f === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": v ? s : void 0
    },
    _ && u && /* @__PURE__ */ e.createElement(Se, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: qn } = e, Zn = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const c = G(), s = ne(), u = t ?? "click", r = n ?? c.label, i = a ?? c.active === !0, d = o ?? c.disabled === !0, f = qn(() => {
    s(u);
  }, [s, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: f,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    r
  );
}, Qn = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Jn } = e, el = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.tabs ?? [], o = t.activeTabId, c = Jn((s) => {
    s !== o && n("selectTab", { tabId: s });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === o,
      className: "tlReactTabBar__tab" + (s.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.icon && /* @__PURE__ */ e.createElement(Se, { encoded: s.icon, className: "tlReactTabBar__tabIcon" }),
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, tl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: o })))));
}, nl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ll = ({ controlId: l }) => {
  const t = G(), n = Ve(), [a, o] = e.useState("idle"), [c, s] = e.useState(null), u = e.useRef(null), r = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", f = t.error, m = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const y = u.current;
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
        i.current = y, r.current = [];
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(y, k ? { mimeType: k } : void 0);
        u.current = L, L.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), i.current = null;
          const E = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const C = new FormData();
          C.append("audio", E, "recording.webm"), await n(C), o("idle");
        }, L.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(nl), w = m === "recording" ? g["js.audioRecorder.stop"] : m === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], v = m === "uploading", _ = ["tlAudioRecorder__button"];
  return m === "recording" && _.push("tlAudioRecorder__button--recording"), m === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: h,
      disabled: v,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${m === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[c]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, al = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, rl = ({ controlId: l }) => {
  const t = G(), n = Ke(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [c, s] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), i = e.useRef(o);
  e.useEffect(() => {
    a ? c === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), s("disabled"));
  }, [a]), e.useEffect(() => {
    o !== i.current && (i.current = o, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (c === "playing" || c === "paused" || c === "loading") && s("idle"));
  }, [o]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (c === "disabled" || c === "loading")
      return;
    if (c === "playing") {
      u.current && u.current.pause(), s("paused");
      return;
    }
    if (c === "paused" && u.current) {
      u.current.play(), s("playing");
      return;
    }
    if (!r.current) {
      s("loading");
      try {
        const v = await fetch(n);
        if (!v.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", v.status), s("idle");
          return;
        }
        const _ = await v.blob();
        r.current = URL.createObjectURL(_);
      } catch (v) {
        console.error("[TLAudioPlayer] Fetch error:", v), s("idle");
        return;
      }
    }
    const w = new Audio(r.current);
    u.current = w, w.onended = () => {
      s("idle");
    }, w.play(), s("playing");
  }, [c, n]), f = ue(al), m = c === "loading" ? f["js.loading"] : c === "playing" ? f["js.audioPlayer.pause"] : c === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], h = c === "disabled" || c === "loading", g = ["tlAudioPlayer__button"];
  return c === "playing" && g.push("tlAudioPlayer__button--playing"), c === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: d,
      disabled: h,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ol = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, sl = ({ controlId: l }) => {
  const t = G(), n = Ve(), [a, o] = e.useState("idle"), [c, s] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", i = t.error, d = t.accept ?? "", f = r === "received" ? "idle" : a !== "idle" ? a : r, m = e.useCallback(async (E) => {
    o("uploading");
    const C = new FormData();
    C.append("file", E, E.name), await n(C), o("idle");
  }, [n]), h = e.useCallback((E) => {
    var b;
    const C = (b = E.target.files) == null ? void 0 : b[0];
    C && m(C);
  }, [m]), g = e.useCallback(() => {
    var E;
    a !== "uploading" && ((E = u.current) == null || E.click());
  }, [a]), w = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), v = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), _ = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const C = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    C && m(C);
  }, [a, m]), y = f === "uploading", k = ue(ol), L = f === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: w,
      onDragLeave: v,
      onDrop: _
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
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: g,
        disabled: y,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, cl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, il = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, o = Ve(), c = Ke(), s = ue(cl), u = a.editable !== !1, r = !!a.hasData, i = a.fileName ?? "download", d = a.dataRevision ?? 0, f = a.accept ?? "", m = a.status ?? "idle", h = a.error ?? null, [g, w] = e.useState("idle"), [v, _] = e.useState(!1), [y, k] = e.useState(!1), L = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || y)) {
      k(!0);
      try {
        const B = c + (c.includes("?") ? "&" : "?") + "rev=" + d, I = await fetch(B);
        if (!I.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", I.status);
          return;
        }
        const S = await I.blob(), Y = URL.createObjectURL(S), p = document.createElement("a");
        p.href = Y, p.download = i, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL(Y);
      } catch (B) {
        console.error("[TLBinaryField] Fetch error:", B);
      } finally {
        k(!1);
      }
    }
  }, [r, y, c, d, i]), C = e.useCallback(async (B) => {
    w("uploading");
    const I = new FormData();
    I.append("file", B, B.name), await o(I), w("idle");
  }, [o]), b = (m === "received" ? "idle" : g !== "idle" ? g : m) === "uploading", D = e.useCallback((B) => {
    var S;
    const I = (S = B.target.files) == null ? void 0 : S[0];
    I && C(I);
  }, [C]), x = e.useCallback(() => {
    var B;
    b || (B = L.current) == null || B.click();
  }, [b]), T = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), _(!0);
  }, []), V = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), _(!1);
  }, []), F = e.useCallback((B) => {
    var S;
    if (B.preventDefault(), B.stopPropagation(), _(!1), b) return;
    const I = (S = B.dataTransfer.files) == null ? void 0 : S[0];
    I && C(I);
  }, [b, C]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", i), H = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, H) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const P = b, j = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${v ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
      onDragLeave: V,
      onDrop: F
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "file",
        accept: f || void 0,
        onChange: D,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: x,
        disabled: P,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && H,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, ul = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function dl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const ml = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ve(), o = Ke(), c = ue(ul), s = t.chips ?? [], u = t.editable === !0, [r, i] = e.useState(!1), [d, f] = e.useState(!1), m = e.useRef(null), h = e.useCallback(async (E) => {
    const C = Array.from(E);
    if (C.length !== 0) {
      i(!0);
      try {
        const b = new FormData();
        for (const D of C)
          b.append("file", D, D.name);
        await a(b);
      } finally {
        i(!1);
      }
    }
  }, [a]), g = e.useCallback(async (E) => {
    if (E.hasData)
      try {
        const C = o + "&key=" + encodeURIComponent(E.key), b = await fetch(C);
        if (!b.ok) {
          console.error("[TLFileChips] Failed to fetch data:", b.status);
          return;
        }
        const D = await b.blob(), x = URL.createObjectURL(D), T = document.createElement("a");
        T.href = x, T.download = E.name, T.style.display = "none", document.body.appendChild(T), T.click(), document.body.removeChild(T), URL.revokeObjectURL(x);
      } catch (C) {
        console.error("[TLFileChips] Fetch error:", C);
      }
  }, [o]), w = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), v = e.useCallback(() => {
    var E;
    r || (E = m.current) == null || E.click();
  }, [r]), _ = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!0));
  }, [u]), y = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!1));
  }, [u]), k = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
  }, [u, r, h]), L = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: L,
      onDragOver: _,
      onDragLeave: y,
      onDrop: k
    },
    s.map((E) => {
      const C = c["js.download.file"].replace("{0}", E.name), b = c["js.fileChips.remove"].replace("{0}", E.name);
      return /* @__PURE__ */ e.createElement("span", { key: E.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => g(E),
          disabled: !E.hasData,
          title: E.hasData ? C : E.name
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
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, dl(E.size))
      ), u && /* @__PURE__ */ e.createElement(
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
    u && /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: m,
        type: "file",
        multiple: !0,
        onChange: w,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: v,
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
}, pl = 3e4;
function fl(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const hl = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => c((u) => u + 1), pl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, fl(n, o));
}, bl = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, gl = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (c) => {
    c.preventDefault(), Tn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function El(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function vl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const _l = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${vl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    El(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Cl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, yl = ({ controlId: l }) => {
  const t = G(), n = Ke(), a = ne(), o = !!t.hasData, c = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [r, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      i(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + c, w = await fetch(g);
        if (!w.ok) {
          console.error("[TLDownload] Failed to fetch data:", w.status);
          return;
        }
        const v = await w.blob(), _ = URL.createObjectURL(v), y = document.createElement("a");
        y.href = _, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(_);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [o, r, n, c, s]), f = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), m = ue(Cl);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, m["js.download.noFile"]));
  const h = r ? m["js.downloading"] : m["js.download.file"].replace("{0}", s);
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
      onClick: f,
      title: m["js.download.clear"],
      "aria-label": m["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, wl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, kl = ({ controlId: l }) => {
  const t = G(), n = Ve(), [a, o] = e.useState("idle"), [c, s] = e.useState(null), [u, r] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), f = e.useRef(null), m = e.useRef(null), h = e.useRef(null), g = t.error, w = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), v = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((T) => T.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    v(), o("idle");
  }, [v]), y = e.useCallback(async () => {
    var T;
    if (a !== "uploading") {
      if (s(null), !w) {
        (T = m.current) == null || T.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = V, o("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, w]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const T = i.current, V = f.current;
    if (!T || !V)
      return;
    V.width = T.videoWidth, V.height = T.videoHeight;
    const F = V.getContext("2d");
    F && (F.drawImage(T, 0, 0), v(), o("uploading"), V.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", A, "capture.jpg"), await n(H), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, v]), L = e.useCallback(async (T) => {
    var A;
    const V = (A = T.target.files) == null ? void 0 : A[0];
    if (!V) return;
    o("uploading");
    const F = new FormData();
    F.append("photo", V, V.name), await n(F), o("idle"), m.current && (m.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var V;
    if (a !== "overlayOpen") return;
    (V = h.current) == null || V.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [a]), Be(a === "overlayOpen", { ESCAPE: _ }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((T) => T.stop()), d.current = null);
  }, []);
  const E = ue(wl), C = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  u && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const x = ["tlPhotoCapture__mirrorBtn"];
  return u && x.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !w && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: m,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        ref: i,
        className: D.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: x.join(" "),
        onClick: () => r((T) => !T),
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[c]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Nl = {
  "js.photoViewer.alt": "Captured photo"
}, Sl = ({ controlId: l }) => {
  const t = G(), n = Ke(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [c, s] = e.useState(null), u = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      c && (URL.revokeObjectURL(c), s(null));
      return;
    }
    if (o === u.current && c)
      return;
    u.current = o, c && (URL.revokeObjectURL(c), s(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const f = await d.blob();
        i || s(URL.createObjectURL(f));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [a, o, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const r = ue(Nl);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Dl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Tl = ({ controlId: l }) => {
  const t = G(), n = Ke(), a = !!t.hasPdf, o = t.dataRevision ?? 0, c = ue(Dl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, i = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(i);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: zt, useRef: bt } = e, Rl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.orientation, o = t.resizable === !0, c = t.children ?? [], s = a === "horizontal", u = c.length > 0 && c.every((v) => v.collapsed), r = !u && c.some((v) => v.collapsed), i = u ? !s : s, d = bt(null), f = bt(null), m = bt(null), h = zt((v, _) => {
    const y = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? u && !i ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : _ !== void 0 ? y.flex = `0 0 ${_}px` : y.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (y.minWidth = s ? v.minSize : void 0, y.minHeight = s ? void 0 : v.minSize), y;
  }, [s, u, r, i]), g = zt((v, _) => {
    v.preventDefault();
    const y = d.current;
    if (!y) return;
    const k = c[_], L = c[_ + 1], E = y.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    E.forEach((x) => {
      C.push(s ? x.offsetWidth : x.offsetHeight);
    }), m.current = C, f.current = {
      splitterIndex: _,
      startPos: s ? v.clientX : v.clientY,
      startSizeBefore: C[_],
      startSizeAfter: C[_ + 1],
      childBefore: k,
      childAfter: L
    };
    const b = (x) => {
      const T = f.current;
      if (!T || !m.current) return;
      const F = (s ? x.clientX : x.clientY) - T.startPos, A = T.childBefore.minSize || 0, H = T.childAfter.minSize || 0;
      let P = T.startSizeBefore + F, j = T.startSizeAfter - F;
      P < A && (j += P - A, P = A), j < H && (P += j - H, j = H), m.current[T.splitterIndex] = P, m.current[T.splitterIndex + 1] = j;
      const B = y.querySelectorAll(":scope > .tlSplitPanel__child"), I = B[T.splitterIndex], S = B[T.splitterIndex + 1];
      I && (I.style.flex = `0 0 ${P}px`), S && (S.style.flex = `0 0 ${j}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", b), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", m.current) {
        const x = {};
        c.forEach((T, V) => {
          const F = T.control;
          F != null && F.controlId && m.current && (x[F.controlId] = m.current[V]);
        }), n("updateSizes", { sizes: x });
      }
      m.current = null, f.current = null;
    };
    document.addEventListener("mousemove", b), document.addEventListener("mouseup", D), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, s, n]), w = [];
  return c.forEach((v, _) => {
    if (w.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${v.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: h(v)
        },
        /* @__PURE__ */ e.createElement(q, { control: v.control })
      )
    ), o && _ < c.length - 1) {
      const y = c[_ + 1];
      !v.collapsed && !y.collapsed && w.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => g(L, _)
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
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    w
  );
}, it = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: gt } = e, Ll = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, xl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Il = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ml = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Pl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), jl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Al = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Ll), o = t.title, c = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, i = t.fullLine === !0, d = t.fill === !0, f = t.hoverActions === !0, m = t.appearance === "card", h = t.errorMessage, g = c === "MINIMIZED", w = c === "MAXIMIZED", v = c === "HIDDEN", _ = gt(() => {
    n("toggleMinimize");
  }, [n]), y = gt(() => {
    n("toggleMaximize");
  }, [n]), k = gt(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const L = w ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = s && !w || u && !g || r, C = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || E;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${i ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${f ? " tlPanel--hoverActions" : ""}${m ? " tlPanel--card" : ""}`,
      style: L
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(q, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(q, { control: t.toolbar }), s && !w && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(Il, null) : /* @__PURE__ */ e.createElement(xl, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: w ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      w ? /* @__PURE__ */ e.createElement(Pl, null) : /* @__PURE__ */ e.createElement(Ml, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(jl, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child })),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(it, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(q, { control: t.buttonBar }))
  );
}, Bl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(q, { control: t.child })
  );
}, Ol = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(q, { control: t.activeChild }));
}, { useCallback: Ee, useState: st, useEffect: Tt, useRef: ut } = e, Fl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Rt(l, t, n, a) {
  const o = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      o.push({ id: c.id, type: "nav", groupId: a });
    } else c.type === "command" ? o.push({ id: c.id, type: "command", groupId: a }) : c.type === "group" && (o.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && o.push(...Rt(c.children, t, n, c.id)));
  return o;
}
const ze = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Se, { encoded: l, className: "tlSidebar__icon" }) : null, $l = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: c,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ze, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(ze, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Hl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(ze, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Wl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(ze, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Ul = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), zl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: c }) => {
  const s = ut(null);
  Tt(() => {
    const i = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", i), () => document.removeEventListener("mousedown", i);
  }, [c]), Be(!0, { ESCAPE: c });
  const u = Ee((i) => {
    i.type === "nav" ? (a(i.id), c()) : i.type === "command" && (o(i.id), c());
  }, [a, o, c]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((i) => {
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
        /* @__PURE__ */ e.createElement(ze, { icon: i.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, i.label),
        i.type === "nav" && i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, i.badge)
      );
    }
    return i.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: i.id, className: "tlSidebar__flyoutSectionHeader" }, i.label) : i.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: i.id, className: "tlSidebar__separator" }) : null;
  }));
}, Vl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: o,
  onExecute: c,
  onToggleGroup: s,
  tabIndex: u,
  itemRef: r,
  onFocus: i,
  focusedId: d,
  setItemRef: f,
  onItemFocus: m,
  flyoutGroupId: h,
  onOpenFlyout: g,
  onCloseFlyout: w
}) => {
  const v = ut(null), [_, y] = st(null), k = Ee(() => {
    a ? h === l.id ? w() : (v.current && y(v.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, w]), L = Ee((C) => {
    v.current = C, r(C);
  }, [r]), E = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? E : t,
      tabIndex: u,
      ref: L,
      onFocus: () => i(l.id)
    },
    /* @__PURE__ */ e.createElement(ze, { icon: l.icon }),
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
    zl,
    {
      item: l,
      activeItemId: n,
      anchorRect: _,
      onSelect: o,
      onExecute: c,
      onClose: w
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((C) => /* @__PURE__ */ e.createElement(
    un,
    {
      key: C.id,
      item: C,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: c,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: f,
      onItemFocus: m,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: g,
      onCloseFlyout: w
    }
  ))));
}, un = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: o,
  onToggleGroup: c,
  focusedId: s,
  setItemRef: u,
  onItemFocus: r,
  groupStates: i,
  flyoutGroupId: d,
  onOpenFlyout: f,
  onCloseFlyout: m
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        $l,
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
        Hl,
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
      return /* @__PURE__ */ e.createElement(Wl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ul, null);
    case "group": {
      const h = i ? i.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Vl,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: o,
          onToggleGroup: c,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: s,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: d,
          onOpenFlyout: f,
          onCloseFlyout: m
        }
      );
    }
    default:
      return null;
  }
}, Kl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Fl), o = t.items ?? [], c = t.activeItemId, s = t.collapsed, u = t.drawerOpen, r = u ? !1 : s, [i, d] = st(() => {
    const A = /* @__PURE__ */ new Map(), H = (P) => {
      for (const j of P)
        j.type === "group" && (A.set(j.id, j.expanded), H(j.children));
    };
    return H(o), A;
  }), f = Ee((A) => {
    d((H) => {
      const P = new Map(H), j = P.get(A) ?? !1;
      return P.set(A, !j), n("toggleGroup", { itemId: A, expanded: !j }), P;
    });
  }, [n]), m = Ee((A) => {
    A !== c && n("selectItem", { itemId: A });
  }, [n, c]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), w = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [v, _] = st(null), y = Ee((A) => {
    _(A);
  }, []), k = Ee(() => {
    _(null);
  }, []);
  Tt(() => {
    r || _(null);
  }, [r]);
  const [L, E] = st(() => {
    const A = Rt(o, r, i);
    return A.length > 0 ? A[0].id : "";
  }), C = ut(/* @__PURE__ */ new Map()), b = Ee((A) => (H) => {
    H ? C.current.set(A, H) : C.current.delete(A);
  }, []), D = Ee((A) => {
    E(A);
  }, []), x = ut(0), T = Ee((A) => {
    E(A), x.current++;
  }, []);
  Tt(() => {
    const A = C.current.get(L);
    A && document.activeElement !== A && A.focus();
  }, [L, x.current]);
  const V = Ee((A) => {
    if (A.key === "Escape" && v !== null) {
      A.preventDefault(), k();
      return;
    }
    const H = Rt(o, r, i);
    if (H.length === 0) return;
    const P = H.findIndex((B) => B.id === L);
    if (P < 0) return;
    const j = H[P];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const B = (P + 1) % H.length;
        T(H[B].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const B = (P - 1 + H.length) % H.length;
        T(H[B].id);
        break;
      }
      case "Home": {
        A.preventDefault(), T(H[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), T(H[H.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), j.type === "nav" ? m(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (r ? v === j.id ? k() : y(j.id) : f(j.id));
        break;
      }
      case "ArrowRight": {
        j.type === "group" && !r && ((i.get(j.id) ?? !1) || (A.preventDefault(), f(j.id)));
        break;
      }
      case "ArrowLeft": {
        j.type === "group" && !r && (i.get(j.id) ?? !1) && (A.preventDefault(), f(j.id));
        break;
      }
    }
  }, [
    o,
    r,
    i,
    L,
    v,
    T,
    m,
    h,
    f,
    y,
    k
  ]), F = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: F }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(q, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: w, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, o.map((A) => /* @__PURE__ */ e.createElement(
    un,
    {
      key: A.id,
      item: A,
      activeItemId: c,
      collapsed: r,
      onSelect: m,
      onExecute: h,
      onToggleGroup: f,
      focusedId: L,
      setItemRef: b,
      onItemFocus: D,
      groupStates: i,
      flyoutGroupId: v,
      onOpenFlyout: y,
      onCloseFlyout: k
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, Yl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", c = t.wrap === !0, s = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    c ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((i, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: i })));
}, Gl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, Xl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", c = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, c.map((u, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: u })));
}, ql = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", c = t.headerActions ?? [], s = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, i) => /* @__PURE__ */ e.createElement(q, { key: i, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(q, { control: s })));
}, Zl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, o = t.children ?? [], c = t.actions ?? [], s = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    s === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, o.map((i, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: i }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((i, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: i }))));
}, { useCallback: Ql } = e, Jl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = Ql((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((c, s) => {
    const u = s === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: c.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => o(c.id)
      },
      c.label
    ));
  })));
}, { useCallback: ea } = e, ta = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = t.activeItemId, c = ea((s) => {
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
        onClick: () => c(s.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: Vt, useRef: na } = e, la = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), aa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, c = t.child, s = na(null), u = Vt(() => {
    n("close");
  }, [n]), r = Vt((i) => {
    o && i.target === i.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(jt, null, /* @__PURE__ */ e.createElement(la, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(q, { control: c })
  )) : null;
}, { useEffect: ra, useRef: oa } = e, sa = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = oa(n.length);
  return ra(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(q, { key: o.controlId, control: o })));
}, { useCallback: nt, useRef: Fe, useState: lt } = e, ca = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ia = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, ua = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], da = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(ia), o = t.title ?? "", c = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, i = t.child, d = t.actions ?? [], f = t.toolbar, m = t.buttonBar, [h, g] = lt(null), [w, v] = lt(null), [_, y] = lt(null), k = Fe(null), [L, E] = lt(!1), C = Fe(null), b = Fe(null), D = Fe(null), x = Fe(null), T = Fe(null), V = nt(() => {
    n("close");
  }, [n]);
  At(!0, x, "field");
  const F = nt((B, I) => {
    I.preventDefault();
    const S = x.current;
    if (!S) return;
    const Y = S.getBoundingClientRect(), p = !k.current, M = k.current ?? { x: Y.left, y: Y.top };
    p && (k.current = M, y(M)), T.current = {
      dir: B,
      startX: I.clientX,
      startY: I.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...M },
      symmetric: p
    };
    const K = (Z) => {
      const $ = T.current;
      if (!$) return;
      const te = Z.clientX - $.startX, ce = Z.clientY - $.startY;
      let le = $.startW, ge = $.startH, ve = 0, we = 0;
      $.symmetric ? ($.dir.includes("e") && (le = $.startW + 2 * te), $.dir.includes("w") && (le = $.startW - 2 * te), $.dir.includes("s") && (ge = $.startH + 2 * ce), $.dir.includes("n") && (ge = $.startH - 2 * ce)) : ($.dir.includes("e") && (le = $.startW + te), $.dir.includes("w") && (le = $.startW - te, ve = te), $.dir.includes("s") && (ge = $.startH + ce), $.dir.includes("n") && (ge = $.startH - ce, we = ce));
      const Te = Math.max(200, le), Re = Math.max(100, ge);
      $.symmetric ? (ve = ($.startW - Te) / 2, we = ($.startH - Re) / 2) : ($.dir.includes("w") && Te === 200 && (ve = $.startW - 200), $.dir.includes("n") && Re === 100 && (we = $.startH - 100)), b.current = Te, D.current = Re, g(Te), v(Re);
      const Oe = {
        x: $.startPos.x + ve,
        y: $.startPos.y + we
      };
      k.current = Oe, y(Oe);
    }, W = () => {
      document.removeEventListener("mousemove", K), document.removeEventListener("mouseup", W);
      const Z = b.current, $ = D.current;
      (Z != null || $ != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...$ != null ? { height: Math.round($) } : {}
      }), T.current = null;
    };
    document.addEventListener("mousemove", K), document.addEventListener("mouseup", W);
  }, [n]), A = nt((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const I = x.current;
    if (!I) return;
    const S = I.getBoundingClientRect(), Y = k.current ?? { x: S.left, y: S.top }, p = B.clientX - Y.x, M = B.clientY - Y.y, K = (Z) => {
      const $ = window.innerWidth, te = window.innerHeight;
      let ce = Z.clientX - p, le = Z.clientY - M;
      const ge = I.offsetWidth, ve = I.offsetHeight;
      ce + ge > $ && (ce = $ - ge), le + ve > te && (le = te - ve), ce < 0 && (ce = 0), le < 0 && (le = 0);
      const we = { x: ce, y: le };
      k.current = we, y(we);
    }, W = () => {
      document.removeEventListener("mousemove", K), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", K), document.addEventListener("mouseup", W);
  }, []), H = nt(() => {
    var B, I;
    if (L) {
      const S = C.current;
      S && (y(S.x !== -1 ? { x: S.x, y: S.y } : null), g(S.w), v(S.h)), E(!1);
    } else {
      const S = x.current, Y = S == null ? void 0 : S.getBoundingClientRect();
      C.current = {
        x: ((B = k.current) == null ? void 0 : B.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((I = k.current) == null ? void 0 : I.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: h ?? (Y == null ? void 0 : Y.width) ?? null,
        h: w ?? null
      }, E(!0), y({ x: 0, y: 0 }), g(null), v(null);
    }
  }, [L, h, w]), P = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : c,
    ...w != null ? { height: w + "px" } : s != null ? { height: s } : {},
    ...u != null && w == null ? { minHeight: u } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(jt, { modal: !0 }, /* @__PURE__ */ e.createElement(ca, { onClose: V }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: x,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : A,
        onDoubleClick: r ? H : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, o),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(q, { control: f })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: H,
          title: L ? a["js.window.restore"] : a["js.window.maximize"]
        },
        L ? (
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(q, { control: i })),
    (d.length > 0 || m) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m && /* @__PURE__ */ e.createElement(q, { control: m }), d.map((B, I) => /* @__PURE__ */ e.createElement(q, { key: I, control: B }))),
    r && !L && ua.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (I) => F(B, I)
      }
    ))
  ));
}, { useCallback: ma } = e, pa = {
  "js.drawer.close": "Close"
}, fa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(pa), o = t.open === !0, c = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, r = t.child, i = ma(() => {
    n("close");
  }, [n]);
  Be(o, { ESCAPE: i });
  const d = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${s}`,
    o ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !o }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(q, { control: r })));
}, { useCallback: ha } = e, ba = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.child, o = ha((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: o }, a && /* @__PURE__ */ e.createElement(q, { control: a }));
}, { useCallback: ga, useEffect: Kt, useRef: Ea, useState: Yt } = e, va = 250, _a = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.message ?? "", o = t.content ?? "", c = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [i, d] = Yt(!1), [f, m] = Yt(!1), h = Ea(!1);
  Kt(() => {
    h.current = !1;
  }, [r]);
  const g = ga(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Kt(() => {
    if (!u || s === 0 || f) return;
    const w = setTimeout(g, h.current ? va : s);
    return () => clearTimeout(w);
  }, [u, s, f, g]), !u && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${c}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        h.current = !0, m(!0);
      },
      onMouseLeave: () => m(!1)
    },
    o ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: o } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: Ca, useEffect: Gt, useMemo: ya, useRef: wa, useState: ka } = e, Na = 1e3;
function Sa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), c = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${c(a)}:${c(n)}` : `${a}:${c(n)}`;
}
const Da = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", c = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, i = t.actionLabel ?? null, d = t.pingGraceMs ?? null, f = ya(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [m, h] = ka(0), g = a && s != null;
  Gt(() => {
    if (!g) return;
    const L = setInterval(() => h((E) => E + 1), Na);
    return () => clearInterval(L);
  }, [g, s]);
  const w = wa(null);
  Gt(() => {
    !g || d == null || s == null || w.current !== s && (Date.now() + f < s + d || (w.current = s, n("deadlinePassed", {})));
  }, [m, g, s, d, f, n]);
  const v = Ca(() => {
    i != null && n("action", {});
  }, [n, i]);
  if (!a) return null;
  const _ = s != null ? s - (Date.now() + f) : null;
  if (r != null && _ != null && _ > r) return null;
  const y = _ != null ? Sa(_) : null, k = i != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: i ?? void 0,
      "aria-label": k ? `${c} ${i}` : void 0,
      onClick: k ? v : void 0,
      onKeyDown: k ? (L) => {
        (L.key === "Enter" || L.key === " ") && (L.preventDefault(), v());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, c),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: Et, useEffect: Xt, useRef: Ta, useState: qt } = e, Ra = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.anchorId, c = t.anchorX, s = t.anchorY, u = t.items ?? [], r = Ta(null), [i, d] = qt({ top: 0, left: 0 }), [f, m] = qt(0), h = u.filter((_) => _.type === "item" && !_.disabled);
  Xt(() => {
    var b, D;
    if (!a) return;
    const _ = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (c != null && s != null) {
      let x = s, T = c;
      x + _ > window.innerHeight && (x = Math.max(0, window.innerHeight - _)), T + y > window.innerWidth && (T = Math.max(0, window.innerWidth - y)), d({ top: x, left: T }), m(0);
      return;
    }
    if (!o) return;
    const k = document.getElementById(o);
    if (!k) return;
    const L = k.getBoundingClientRect();
    let E = L.bottom + 4, C = L.left;
    E + _ > window.innerHeight && (E = L.top - _ - 4), C + y > window.innerWidth && (C = L.right - y), d({ top: E, left: C }), m(0);
  }, [a, o, c, s]);
  const g = Et(() => {
    n("close");
  }, [n]), w = Et((_) => {
    n("selectItem", { itemId: _ });
  }, [n]);
  Xt(() => {
    if (!a) return;
    const _ = (y) => {
      r.current && !r.current.contains(y.target) && g();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [a, g]);
  const v = Et((_) => {
    if (_.key === "Escape") {
      _.preventDefault(), g();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), m((y) => (y + 1) % h.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), m((y) => (y - 1 + h.length) % h.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const y = h[f];
      y && w(y.id);
    }
  }, [g, w, h, f]);
  return At(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: i.top, left: i.left },
      onKeyDown: v
    },
    u.map((_, y) => {
      if (_.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const L = h.indexOf(_) === f;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: _.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (_.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: _.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => w(_.id)
        },
        _.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + _.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, _.label)
      );
    })
  ) : null;
}, La = 768, xa = ({ controlId: l }) => {
  const t = G(), n = ne();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${La}px)`), i = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    i(r.matches);
    const d = (f) => i(f.matches);
    return r.addEventListener("change", d), () => r.removeEventListener("change", d);
  }, [n]);
  const a = t.header, o = t.notices, c = t.content, s = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: a })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(q, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: s })), /* @__PURE__ */ e.createElement(q, { control: u }));
}, Ia = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", o = t.hasTooltip === !0, c = t.role || void 0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      role: c,
      "data-tooltip": o ? "key:tooltip" : void 0
    },
    n
  );
}, Ma = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), Pa = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, Zt = 50, ja = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function vt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, ja));
}
const Lt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Aa = Lt + ", button:not([disabled]), a[href]";
function dn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function _t(l, t, n = {}) {
  const a = dn(l, t);
  if (n.col) {
    const c = a.find((u) => u.dataset.col === n.col), s = c == null ? void 0 : c.querySelector(Lt);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const c of o) {
    const s = c.querySelector(Lt);
    if (s) return s;
  }
  return null;
}
const Ba = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Pa), o = e.useRef(null);
  e.useEffect(() => {
    const N = o.current;
    if (!N) return;
    const R = (z) => {
      const Q = z.detail;
      let ee = Q.target;
      for (; ee && ee !== N; ) {
        const re = ee.dataset.row, oe = ee.dataset.col;
        if (re != null && oe != null) {
          Q.resolved = { key: re + "|" + oe };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", R), () => N.removeEventListener("tl-tooltip-resolve", R);
  }, []);
  const c = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, i = t.selectionMode ?? "single", d = t.selectedCount ?? 0, f = t.cursorIndex ?? -1, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, w = e.useMemo(
    () => c.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [c]
  ), v = i === "multi", _ = 40, y = 20, k = e.useRef(null), L = e.useRef(null), E = e.useRef(null), C = e.useRef(null), b = e.useRef(null), [D, x] = e.useState({}), T = e.useRef(null), V = e.useRef(!1), F = e.useRef(null), [A, H] = e.useState(null), [P, j] = e.useState(null), [B, I] = e.useState(null), [S, Y] = e.useState(0);
  e.useEffect(() => {
    const N = E.current;
    if (!N)
      return;
    const R = () => {
      const Q = N.offsetWidth - N.clientWidth;
      Y((ee) => ee === Q ? ee : Q);
    };
    R();
    const z = new ResizeObserver(R);
    return z.observe(N), () => z.disconnect();
  }, []), e.useEffect(() => {
    T.current || x({});
  }, [c]);
  const p = e.useCallback((N) => D[N.name] ?? N.width, [D]), M = e.useMemo(() => {
    const N = [];
    let R = v && m > 0 ? _ : 0;
    for (let z = 0; z < m && z < c.length; z++)
      N.push(R), R += p(c[z]);
    return N;
  }, [c, m, v, _, p]), K = e.useMemo(() => {
    if (m <= 0)
      return 0;
    let N = v ? _ : 0;
    for (let R = 0; R < m && R < c.length; R++)
      N += p(c[R]);
    return N;
  }, [c, m, v, _, p]), W = s * r, Z = e.useRef(null), $ = e.useCallback((N, R, z) => {
    z.preventDefault(), z.stopPropagation(), T.current = { column: N, startX: z.clientX, startWidth: R };
    let Q = z.clientX, ee = 0;
    const re = () => {
      const se = T.current;
      if (!se) return;
      const de = Math.max(Zt, se.startWidth + (Q - se.startX) + ee);
      x((Ce) => ({ ...Ce, [se.column]: de }));
    }, oe = () => {
      const se = E.current, de = k.current;
      if (!se || !T.current) return;
      const Ce = se.getBoundingClientRect(), Le = 40, Ft = 8, Dn = se.scrollLeft;
      Q > Ce.right - Le ? se.scrollLeft += Ft : Q < Ce.left + Le && (se.scrollLeft = Math.max(0, se.scrollLeft - Ft));
      const $t = se.scrollLeft - Dn;
      $t !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += $t, re()), Z.current = requestAnimationFrame(oe);
    };
    Z.current = requestAnimationFrame(oe);
    const fe = (se) => {
      Q = se.clientX, re();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), Z.current !== null && (cancelAnimationFrame(Z.current), Z.current = null);
      const de = T.current;
      if (de) {
        const Ce = Math.max(Zt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ce }), T.current = null, V.current = !0, requestAnimationFrame(() => {
          V.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    k.current && E.current && (k.current.scrollLeft = E.current.scrollLeft), C.current !== null && clearTimeout(C.current), C.current = window.setTimeout(() => {
      const N = E.current;
      if (!N) return;
      const R = N.scrollTop, z = Math.ceil(N.clientHeight / r), Q = Math.floor(R / r);
      n("scroll", { start: Q, count: z });
    }, 80);
  }, [n, r]), ce = e.useCallback((N, R, z) => {
    if (V.current) return;
    let Q;
    !R || R === "desc" ? Q = "asc" : Q = "desc";
    const ee = z.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: Q, mode: ee });
  }, [n]), le = e.useCallback((N, R) => {
    F.current = N, R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", N);
  }, []), ge = e.useCallback((N, R) => {
    if (!F.current || F.current === N) {
      H(null);
      return;
    }
    R.preventDefault(), R.dataTransfer.dropEffect = "move";
    const z = R.currentTarget.getBoundingClientRect(), Q = R.clientX < z.left + z.width / 2 ? "left" : "right";
    H({ column: N, side: Q });
  }, []), ve = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const R = F.current;
    if (!R || !A) {
      F.current = null, H(null);
      return;
    }
    let z = c.findIndex((ee) => ee.name === A.column);
    if (z < 0) {
      F.current = null, H(null);
      return;
    }
    const Q = c.findIndex((ee) => ee.name === R);
    A.side === "right" && z++, Q < z && z--, n("columnReorder", { column: R, targetIndex: z }), F.current = null, H(null);
  }, [c, A, n]), we = e.useCallback(() => {
    F.current = null, H(null);
  }, []), Te = e.useCallback((N, R) => {
    var ee, re, oe, fe;
    const z = window.getSelection();
    if (z && !z.isCollapsed && R.currentTarget.contains(z.anchorNode))
      return;
    if (!vt(R) && ((ee = E.current) == null || ee.focus({ preventScroll: !0 }), !R.ctrlKey && !R.metaKey && !R.shiftKey)) {
      const pe = (fe = (oe = (re = R.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      b.current = { index: N, col: pe ?? void 0 };
    }
    const Q = u.find((pe) => pe.index === N);
    vt(R) && (Q != null && Q.selected) && !R.ctrlKey && !R.metaKey && !R.shiftKey || n("select", {
      rowIndex: N,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    });
  }, [n, u]), Re = e.useCallback((N, R, z) => {
    n("moveSelection", { direction: N, extend: R, move: z });
  }, [n]), Oe = e.useCallback(() => {
    f < 0 || n("select", { rowIndex: f, ctrlKey: v, shiftKey: !1 });
  }, [n, f, v]), et = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), O = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (f < 0)
      return;
    const N = E.current;
    if (!N)
      return;
    const R = f * r, z = R + r;
    R < N.scrollTop ? N.scrollTop = R : z > N.scrollTop + N.clientHeight && (N.scrollTop = z - N.clientHeight);
  }, [f, r]), e.useEffect(() => {
    const N = b.current, R = E.current;
    if (!N || !R)
      return;
    const z = u.find((re) => re.index === N.index);
    if (!z || !_t(R, z.id))
      return;
    b.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !R.contains(Q))
      return;
    const ee = _t(R, z.id, { col: N.col, last: N.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const X = e.useCallback((N) => {
    if (N.key !== "Tab")
      return;
    const R = E.current, z = document.activeElement;
    if (!R || !z || !R.contains(z))
      return;
    const Q = z.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = u.find((Le) => Le.id === ee);
    if (!re)
      return;
    const oe = dn(R, ee).flatMap((Le) => Array.from(Le.querySelectorAll(Aa))), fe = oe.indexOf(z);
    if (fe < 0)
      return;
    const pe = !N.shiftKey;
    if (!(pe ? fe === oe.length - 1 : fe === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const Ce = u.find((Le) => Le.index === de);
    Ce && _t(R, Ce.id) || (N.preventDefault(), b.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), ae = e.useCallback((N, R) => {
    R.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ie = e.useCallback(() => {
    const N = d === s && s > 0;
    n("selectAll", { selected: !N });
  }, [n, d, s]), Ye = e.useCallback((N, R, z) => {
    z.stopPropagation(), n("expand", { rowIndex: N, expanded: R });
  }, [n]), vn = e.useCallback((N, R) => {
    R.preventDefault(), j({ x: R.clientX, y: R.clientY, colIdx: N });
  }, []), _n = e.useCallback(() => {
    P && (n("setFrozenColumnCount", { count: P.colIdx + 1 }), j(null));
  }, [P, n]), Cn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), j(null);
  }, [n]), yn = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const R = L.current, z = k.current;
    if (!R || !z)
      return;
    const Q = R.clientWidth, ee = [{ x: 0, count: 0 }];
    z.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - R.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: K, count: m };
    const oe = (pe) => {
      const se = pe.clientX - R.getBoundingClientRect().left;
      re = ee.reduce(
        (de, Ce) => Math.abs(Ce.x - se) < Math.abs(de.x - se) ? Ce : de,
        ee[0]
      ), I(re);
    }, fe = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", fe), I(null), re.count !== m && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", fe);
  }, [K, m, n]);
  e.useEffect(() => {
    if (!P) return;
    const N = () => j(null);
    return document.addEventListener("mousedown", N), () => document.removeEventListener("mousedown", N);
  }, [P]), Be(!!P, { ESCAPE: () => j(null) });
  const wn = e.useCallback((N, R) => {
    R.stopPropagation(), R.preventDefault(), n("openFilter", { column: N });
  }, [n]), kn = e.useCallback((N) => {
    N.stopPropagation(), N.preventDefault(), n("openColumnSelect", {});
  }, [n]), ft = c.reduce((N, R) => N + p(R), 0) + (v ? _ : 0), ht = g ? 32 : 0, Nn = d === s && s > 0, Ot = d > 0 && d < s, Sn = e.useCallback((N) => {
    N && (N.indeterminate = Ot);
  }, [Ot]);
  return /* @__PURE__ */ e.createElement(jt, { active: O }, /* @__PURE__ */ e.createElement(
    Ma,
    {
      isMulti: v,
      cursorIndex: f,
      onMove: Re,
      onToggle: Oe,
      onSelectAll: et
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (N) => {
        if (!F.current) return;
        N.preventDefault();
        const R = E.current, z = k.current;
        if (!R) return;
        const Q = R.getBoundingClientRect(), ee = 40, re = 8;
        N.clientX < Q.left + ee ? R.scrollLeft = Math.max(0, R.scrollLeft - re) : N.clientX > Q.right - ee && (R.scrollLeft += re), z && (z.scrollLeft = R.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: L }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: k }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: ft, paddingRight: ht + S }
      },
      v && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: _,
            minWidth: _,
            ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (N) => {
            F.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== F.current && H({ column: c[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Sn,
            className: "tlTableView__checkbox",
            checked: Nn,
            onChange: ie
          }
        )
      ),
      c.map((N, R) => {
        const z = p(N);
        c.length - 1;
        let Q = "tlTableView__headerCell";
        N.sortable && (Q += " tlTableView__headerCell--sortable"), A && A.column === N.name && (Q += " tlTableView__headerCell--dragOver-" + A.side);
        const ee = R < m, re = R === m - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.name,
            className: Q,
            "data-col-idx": R,
            style: {
              width: z,
              minWidth: z,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: M[R], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: N.sortable ? (oe) => ce(N.name, N.sortDirection, oe) : void 0,
            onContextMenu: (oe) => vn(R, oe),
            onDragStart: (oe) => le(N.name, oe),
            onDragOver: (oe) => ge(N.name, oe),
            onDrop: ve,
            onDragEnd: we
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
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => wn(N.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: N.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", w > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => $(N.name, z, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (N) => {
            if (F.current && c.length > 0) {
              const R = c[c.length - 1];
              R.name !== F.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", H({ column: R.name, side: "right" }));
            }
          },
          onDrop: ve
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (B ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: K },
        title: a["js.table.freezeSplitter"],
        onMouseDown: yn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: kn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: E,
        className: "tlTableView__body",
        onScroll: te,
        onKeyDown: X,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: ft, paddingRight: ht } }, u.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : "") + (N.index === f ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: N.index * r,
            height: r,
            width: ft,
            paddingRight: ht,
            ...N.index === f ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (R) => {
            (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && !vt(R) && R.preventDefault();
          },
          onClick: (R) => Te(N.index, R)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: _,
              minWidth: _,
              ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (R) => R.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (R) => ae(N.index, R),
              tabIndex: -1
            }
          )
        ),
        c.map((R, z) => {
          const Q = p(R), ee = z === c.length - 1, re = z < m, oe = z === m - 1;
          let fe = "tlTableView__cell";
          re && (fe += " tlTableView__cell--frozen"), oe && (fe += " tlTableView__cell--frozenLast");
          const pe = h && z === 0, se = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: R.name,
              className: fe,
              "data-row": N.id,
              "data-col": R.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: M[z], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Ye(N.index, !N.expanded, de)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), N.cells[R.name] && /* @__PURE__ */ e.createElement(q, { control: N.cells[R.name] })) : N.cells[R.name] && /* @__PURE__ */ e.createElement(q, { control: N.cells[R.name] })
          );
        })
      )))
    ),
    B && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: B.x } }),
    P && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: P.y, left: P.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      P.colIdx + 1 !== m && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: _n }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      m > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Cn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, Oa = {
  "js.table.columnSearch": "Find column"
}, Fa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Oa), o = t.entries ?? [], c = o.filter((E) => E.visible).length, [s, u] = e.useState(""), r = s.trim().toLowerCase(), i = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), f = e.useRef(null), [m, h] = e.useState(null), g = e.useCallback((E) => {
    f.current = E, h(E);
  }, []), w = e.useCallback((E, C) => {
    n("columnVisible", { column: E, visible: C });
  }, [n]), v = e.useCallback((E, C) => {
    d.current = E, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", E);
  }, []), _ = e.useCallback((E, C) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const b = C.currentTarget.getBoundingClientRect(), D = C.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: D });
  }, [g]), y = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), k = e.useCallback((E) => {
    E.preventDefault();
    const C = d.current, b = f.current;
    if (d.current = null, g(null), !C || !b)
      return;
    const D = o.findIndex((V) => V.name === b.name), x = o.findIndex((V) => V.name === C);
    if (D < 0 || x < 0)
      return;
    let T = b.side === "top" ? D : D + 1;
    x < T && T--, T !== x && n("columnReorder", { column: C, targetIndex: T });
  }, [o, n, g]), L = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: k }, L && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (E) => u(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, i.map((E) => {
    const C = E.visible && c <= 1;
    let b = "tlColumnSelect__row";
    return m && m.name === E.name && (b += " tlColumnSelect__row--dragOver-" + m.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (D) => v(E.name, D),
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
          disabled: C,
          onChange: (D) => w(E.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: xt, useRef: Qe, useCallback: ct, useMemo: je, useEffect: Qt } = e, $a = {
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
}, _e = 44, dt = 15, ye = 6e4, Ha = 36e5, Me = 864e5, Wa = 8;
function Ne(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ue(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function Ua(l) {
  return Ne(l);
}
function Je(l, t) {
  return Ne(l) === Ne(t);
}
function Ie(l) {
  return (l - Ne(l)) / ye;
}
function Ge(l) {
  return Math.round(l / dt) * dt;
}
function Xe(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function mt(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % Wa;
}
function pt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function za(l) {
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
function Ae(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function Va(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Ae(l, n, t.start) + "–" + Ae(l, n, t.end);
}
const Ka = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Ya = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Ka.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Ga(l) {
  const t = [...l].sort((s, u) => s.start - u.start || u.end - s.end), n = [];
  let a = [], o = -1;
  const c = () => {
    const s = a.reduce((u, r) => Math.max(u, r.col + 1), 0);
    for (const u of a)
      u.cols = s;
    n.push(...a), a = [], o = -1;
  };
  for (const s of t) {
    a.length > 0 && s.start >= o && c();
    const u = new Set(a.filter((i) => i.ev.end > s.start).map((i) => i.col));
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
  return a.length > 0 && c(), n;
}
const Ct = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, It = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const c = Qe(!1), s = (u) => {
    c.current || (c.current = !0, u === null ? o() : a(u));
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
}, mn = (l) => {
  const [t, n] = xt(null), a = Qe(null);
  a.current = t;
  const o = ct((u) => n(u), []), c = ct(() => n(null), []), s = ct(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: c };
}, Xa = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: c, dayStartHour: s, dayEndHour: u, now: r, send: i, editable: d, i18n: f } = l, m = je(() => {
    const P = n === "DAY" ? 1 : 7, j = [];
    for (let B = 0; B < P; B++) {
      const I = Ue(t, B);
      n === "WORK_WEEK" && c.includes(new Date(I).getDay()) || j.push(I);
    }
    return j;
  }, [t, n, c]), h = mn(i), g = Qe(null), w = Qe(null), [v, _] = xt(null), y = Qe(null);
  y.current = v;
  const [k, L] = xt(Date.now());
  Qt(() => {
    const P = window.setInterval(() => L(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const E = ct(
    (P, j) => {
      const B = g.current;
      if (!B)
        return { dayIndex: 0, min: 0 };
      const I = B.getBoundingClientRect(), S = I.width / m.length, Y = Xe(Math.floor((P - I.left) / S), 0, m.length - 1), p = j - I.top + B.scrollTop, M = Xe(p / _e * 60, 0, 1440);
      return { dayIndex: Y, min: M };
    },
    [m.length]
  );
  Qt(() => {
    if (!v)
      return;
    const P = (I) => {
      const S = y.current;
      if (!S)
        return;
      const { dayIndex: Y, min: p } = E(I.clientX, I.clientY);
      S.mode === "move" ? _({ ...S, dayStart: m[Y], startMin: Xe(Ge(p - S.grabMin), 0, 1440 - S.dur) }) : S.mode === "resize" ? _({ ...S, endMin: Xe(Ge(p), S.startMin + dt, 1440) }) : _({ ...S, toMin: Xe(Ge(p), 0, 1440) });
    }, j = () => {
      const I = y.current;
      if (_(null), !!I)
        if (I.mode === "move") {
          const S = I.dayStart + I.startMin * ye;
          S !== I.origStartMs && i("moveEvent", { eventId: I.id, start: S, end: S + I.dur * ye });
        } else if (I.mode === "resize") {
          const S = I.dayStart + I.endMin * ye;
          S !== I.origEndMs && i("resizeEvent", { eventId: I.id, end: S });
        } else {
          const S = Math.min(I.fromMin, I.toMin), Y = Math.max(I.fromMin, I.toMin);
          Y - S >= dt && h.open({ start: I.dayStart + S * ye, end: I.dayStart + Y * ye, allDay: !1 });
        }
    }, B = () => _(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", B), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", B);
    };
  }, [v, m, E, i, h.open]);
  const C = (P, j, B) => {
    if (!d || !j.movable)
      return;
    P.stopPropagation(), Ct(P), h.discard();
    const { min: I } = E(P.clientX, P.clientY), S = (j.end - j.start) / ye;
    _({
      mode: "move",
      id: j.id,
      grabMin: I - Ie(j.start),
      dur: S,
      dayStart: B,
      startMin: Ie(j.start),
      origStartMs: j.start
    });
  }, b = (P, j, B) => {
    !d || !j.resizable || (P.stopPropagation(), Ct(P), h.discard(), _({
      mode: "resize",
      id: j.id,
      dayStart: B,
      startMin: Ie(j.start),
      endMin: Ie(j.end),
      origEndMs: j.end
    }));
  }, D = (P, j) => {
    if (!d || P.button !== 0)
      return;
    Ct(P), h.discard();
    const { min: B } = E(P.clientX, P.clientY);
    _({ mode: "create", dayStart: j, fromMin: Ge(B), toMin: Ge(B) });
  }, x = Array.from({ length: 24 }, (P, j) => j), T = je(() => {
    if (v === null || !("id" in v))
      return a;
    const P = v;
    return a.map((j) => {
      if (j.id !== P.id)
        return j;
      if (P.mode === "move") {
        const B = P.dayStart + P.startMin * ye;
        return { ...j, start: B, end: B + P.dur * ye };
      }
      return { ...j, end: P.dayStart + P.endMin * ye };
    });
  }, [a, v]), V = je(() => m.map(
    (P) => Ga(
      T.filter((j) => !j.allDay && j.start < P + Me && j.end > P)
    )
  ), [m, T]), F = je(() => m.map((P) => T.filter((j) => j.allDay && j.start < P + Me && j.end > P)), [m, T]), A = s * _e, H = u * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), m.map((P) => {
    const j = c.includes(new Date(P).getDay()), B = Je(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (B ? " tlCalDayHead--today" : ""),
        onClick: () => i("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Ae(o, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, f["js.calendar.allDay"]), m.map((P, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: P,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: P, end: P + Me, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === P && /* @__PURE__ */ e.createElement(
      It,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: f["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    F[j].map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B.id,
        className: "tlCalAllDayEvent " + mt(B.category) + (B.selected ? " tlCalEvent--selected" : ""),
        style: pt(B),
        title: B.tooltip,
        onClick: (I) => {
          I.stopPropagation(), i("selectEvent", { eventId: B.id });
        }
      },
      B.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: w }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, x.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * _e } }, P === 0 ? "" : Ae(o, { hour: "numeric" }, Ne(t) + P * Ha)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${m.length}, 1fr)` } }, m.map((P, j) => {
    const B = c.includes(new Date(P).getDay()), I = v && ("dayStart" in v && v.dayStart === P) ? v : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (B ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => D(S, P)
      },
      x.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: H - A } }),
      Je(P, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * _e } }),
      V[j].map((S) => {
        const Y = v !== null && "id" in v && v.id === S.ev.id, p = S.topMin / 60 * _e, M = (S.botMin - S.topMin) / 60 * _e, K = 100 / S.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.ev.id,
            className: "tlCalEvent " + mt(S.ev.category) + (S.ev.selected ? " tlCalEvent--selected" : "") + (Y ? " tlCalEvent--dragging" : ""),
            style: pt(S.ev, {
              top: p,
              height: M,
              left: `${S.col * K}%`,
              width: `calc(${K}% - 2px)`
            }),
            title: S.ev.tooltip,
            onPointerDown: (W) => C(W, S.ev, P),
            onClick: (W) => {
              W.stopPropagation(), i("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Va(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (W) => b(W, S.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Ne(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        It,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: f["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * _e,
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
}, qa = 3, Za = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: c, send: s, editable: u, now: r, i18n: i } = l, d = mn(s), f = je(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const w = [];
      for (let v = 0; v < 7; v++)
        w.push(Ue(t, g * 7 + v));
      h.push(w);
    }
    return h;
  }, [t]), m = (h, g) => {
    h.preventDefault();
    const w = h.dataTransfer.getData("text/plain"), v = a.find((y) => y.id === w);
    if (!v || !u || !v.movable)
      return;
    const _ = g - Ne(v.start);
    s("moveEvent", { eventId: w, start: v.start + _, end: v.end + _ });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, f[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Ae(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, f.map((h, g) => {
    const w = h[0], v = Ue(w, 7), _ = a.filter((k) => (k.allDay || k.end - k.start >= Me) && k.start < v && k.end > w).sort((k, L) => k.start - L.start).slice(0, 3), y = _.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const L = new Date(k).getMonth() === new Date(n).getMonth(), E = c.includes(new Date(k).getDay()), C = Je(k, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (L ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => m(b, k),
          onClick: () => u && d.open({ start: k, end: k + Me, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (C ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: k, granularity: "DAY" });
            }
          },
          new Date(k).getDate()
        ),
        d.pending && d.pending.start === k && /* @__PURE__ */ e.createElement(
          It,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: i["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, _.map((k, L) => {
      const E = Math.max(0, Math.floor((Ne(Math.max(k.start, w)) - w) / Me)), C = Math.min(7, Math.ceil((k.end - w) / Me));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + mt(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: pt(k, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, C) + 1}`,
            gridRow: L + 1
          }),
          draggable: u && k.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: k.id });
          }
        },
        k.title
      );
    }), h.map((k, L) => {
      const E = a.filter((D) => !D.allDay && D.end - D.start < Me && Je(D.start, k)).sort((D, x) => D.start - x.start), C = E.slice(0, qa), b = E.length - C.length;
      return C.map((D, x) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: D.id,
          className: "tlCalChip " + mt(D.category) + (D.selected ? " tlCalEvent--selected" : ""),
          style: pt(D, { gridColumn: L + 1, gridRow: y + 1 + x }),
          draggable: u && D.movable,
          onDragStart: (T) => T.dataTransfer.setData("text/plain", D.id),
          title: D.tooltip,
          onClick: (T) => {
            T.stopPropagation(), s("selectEvent", { eventId: D.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Ae(o, { hour: "numeric", minute: "2-digit" }, D.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, D.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: L + 1, gridRow: y + 1 + C.length },
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
}, Qa = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: c, send: s, now: u } = l, r = je(() => {
    const m = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Ne(h.start);
      const w = h.end;
      for (; g < w; )
        m.add(g), g = Ue(g, 1);
    }
    return m;
  }, [n]), i = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (m, h) => new Date(i, h, 1).getTime()), f = je(() => {
    const m = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const w = new Date(m);
      return w.setDate(m.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(w);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((m) => {
    const h = new Date(m), g = Ne(Ue(m, -((h.getDay() - o + 7) % 7))), w = Array.from({ length: 42 }, (v, _) => Ue(g, _));
    return /* @__PURE__ */ e.createElement("div", { key: m, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: m, granularity: "MONTH" })
      },
      Ae(a, { month: "long" }, m)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, f.map((v, _) => /* @__PURE__ */ e.createElement("div", { key: "h" + _, className: "tlCalMiniWd" }, v)), w.map((v) => {
      const _ = new Date(v).getMonth() === h.getMonth(), y = c.includes(new Date(v).getDay()), k = Je(v, u), L = r.has(Ua(v));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v,
          className: "tlCalMiniDay" + (_ ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (L ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: v, granularity: "DAY" })
        },
        new Date(v).getDate()
      );
    })));
  }));
}, Ja = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue($a), o = t.granularity ?? "WEEK", c = t.rangeStart ?? Date.now(), s = t.anchor ?? c, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: za(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Ya, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(Za, { ctx: r, rangeStart: c, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(Qa, { ctx: r, rangeStart: c }) : /* @__PURE__ */ e.createElement(Xa, { ctx: r, rangeStart: c, granularity: o })));
}, er = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, pn = e.createContext(er), { useMemo: tr, useRef: nr, useState: lr, useEffect: ar } = e, rr = 320, or = "TLTableView", sr = "TLPanel", cr = ({ controlId: l }) => {
  var v;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, c = t.children ?? [], s = t.noModelMessage, u = nr(null), [r, i] = lr(
    a === "top" ? "top" : "side"
  );
  ar(() => {
    if (a !== "auto") {
      i(a);
      return;
    }
    const _ = u.current;
    if (!_) return;
    const y = new ResizeObserver((k) => {
      for (const L of k) {
        const C = L.contentRect.width / n;
        i(C < rr ? "top" : "side");
      }
    });
    return y.observe(_), () => y.disconnect();
  }, [a, n]);
  const d = tr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), m = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = c.length === 1 ? c[0] : void 0, g = !!h && (h.module === or || h.module === sr && ((v = h.state) == null ? void 0 : v.bare) === !0), w = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(pn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: w, style: m, ref: u }, c.map((_, y) => /* @__PURE__ */ e.createElement(q, { key: y, control: _ }))));
}, { useCallback: ir } = e, ur = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, dr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(ur), o = t.headerControl ?? null, c = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", i = t.fullLine === !0, d = t.children ?? [], f = o != null || c.length > 0 || s, m = ir(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    i ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: m,
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(q, { control: o })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((g, w) => /* @__PURE__ */ e.createElement(q, { key: w, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, w) => /* @__PURE__ */ e.createElement(q, { key: w, control: g }))));
}, { useContext: mr, useState: pr, useCallback: fr } = e, hr = ({ controlId: l }) => {
  const t = G(), n = mr(pn), a = t.label ?? "", o = t.required === !0, c = t.error, s = t.errorIcon, u = t.warnings, r = t.warningIcon, i = t.helpText, d = t.dirty === !0, f = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, w = t.field, v = n.readOnly, [_, y] = pr(!1), k = fr(() => y((D) => !D), []), L = f === "hidden", E = c != null, C = u != null && u.length > 0, b = [
    "tlFormField",
    `tlFormField--${f}`,
    v ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && C ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !v && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), i && !v && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: w })), !v && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(it, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, c)), !v && !E && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, x) => /* @__PURE__ */ e.createElement("div", { key: x, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(it, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !v && i && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, br = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.iconCss, o = t.iconSrc, c = t.label, s = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, i = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, i, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), f = e.useCallback((g) => {
    g.preventDefault(), n("goto", {});
  }, [n]), m = ["tlResourceCell", s].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: m,
      href: "#",
      onClick: f,
      "data-tooltip": h
    },
    d
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: m, "data-tooltip": h }, d);
}, gr = 20, Er = () => {
  var C;
  const l = G(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, i] = e.useState(-1), d = e.useRef(null), f = ((C = n.find((b) => b.selected)) == null ? void 0 : C.id) ?? null;
  e.useEffect(() => {
    var D;
    if (f == null)
      return;
    const b = (D = d.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [f]);
  const m = e.useCallback((b, D) => {
    t(D ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, D) => {
    var T;
    const x = window.getSelection();
    x && !x.isCollapsed && D.currentTarget.contains(x.anchorNode) || ((T = d.current) == null || T.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), g = e.useCallback((b, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: b, x: D.clientX, y: D.clientY });
  }, [t]), w = e.useRef(null), v = e.useCallback((b, D) => {
    const x = D.getBoundingClientRect(), T = b.clientY - x.top, V = x.height / 3;
    return T < V ? "above" : T > V * 2 ? "below" : "within";
  }, []), _ = e.useCallback((b, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const x = v(D, D.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: x }), w.current = null;
    }, 50);
  }, [t, v]), k = e.useCallback((b, D) => {
    D.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const x = v(D, D.currentTarget);
    t("drop", { nodeId: b, position: x });
  }, [t, v]), L = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
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
          const x = n[r];
          if (x.expandable && !x.expanded) {
            t("expand", { nodeId: x.id });
            return;
          } else x.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const x = n[r];
          if (x.expanded) {
            t("collapse", { nodeId: x.id });
            return;
          } else {
            const T = x.depth;
            for (let V = r - 1; V >= 0; V--)
              if (n[V].depth < T) {
                D = V;
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
    D !== r && i(D);
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
          s === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * gr },
        draggable: o,
        onMouseDown: (x) => {
          (x.shiftKey || x.ctrlKey || x.metaKey || x.detail > 1) && x.preventDefault();
        },
        onClick: (x) => h(b.id, x),
        onContextMenu: (x) => g(b.id, x),
        onDragStart: (x) => _(b.id, x),
        onDragOver: c ? (x) => y(b.id, x) : void 0,
        onDrop: c ? (x) => k(b.id, x) : void 0,
        onDragEnd: L
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (x) => {
            x.stopPropagation(), m(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(q, { control: b.content }))
    ))
  );
};
var yt = { exports: {} }, he = {}, wt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Jt;
function vr() {
  if (Jt) return J;
  Jt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), i = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), f = Symbol.for("react.activity"), m = Symbol.iterator;
  function h(p) {
    return p === null || typeof p != "object" ? null : (p = m && p[m] || p["@@iterator"], typeof p == "function" ? p : null);
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
  }, w = Object.assign, v = {};
  function _(p, M, K) {
    this.props = p, this.context = M, this.refs = v, this.updater = K || g;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(p, M) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, M, "setState");
  }, _.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function y() {
  }
  y.prototype = _.prototype;
  function k(p, M, K) {
    this.props = p, this.context = M, this.refs = v, this.updater = K || g;
  }
  var L = k.prototype = new y();
  L.constructor = k, w(L, _.prototype), L.isPureReactComponent = !0;
  var E = Array.isArray;
  function C() {
  }
  var b = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function x(p, M, K) {
    var W = K.ref;
    return {
      $$typeof: l,
      type: p,
      key: M,
      ref: W !== void 0 ? W : null,
      props: K
    };
  }
  function T(p, M) {
    return x(p.type, M, p.props);
  }
  function V(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function F(p) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(K) {
      return M[K];
    });
  }
  var A = /\/+/g;
  function H(p, M) {
    return typeof p == "object" && p !== null && p.key != null ? F("" + p.key) : M.toString(36);
  }
  function P(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(C, C) : (p.status = "pending", p.then(
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
  function j(p, M, K, W, Z) {
    var $ = typeof p;
    ($ === "undefined" || $ === "boolean") && (p = null);
    var te = !1;
    if (p === null) te = !0;
    else
      switch ($) {
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
                M,
                K,
                W,
                Z
              );
          }
      }
    if (te)
      return Z = Z(p), te = W === "" ? "." + H(p, 0) : W, E(Z) ? (K = "", te != null && (K = te.replace(A, "$&/") + "/"), j(Z, M, K, "", function(ge) {
        return ge;
      })) : Z != null && (V(Z) && (Z = T(
        Z,
        K + (Z.key == null || p && p.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (E(p))
      for (var le = 0; le < p.length; le++)
        W = p[le], $ = ce + H(W, le), te += j(
          W,
          M,
          K,
          $,
          Z
        );
    else if (le = h(p), typeof le == "function")
      for (p = le.call(p), le = 0; !(W = p.next()).done; )
        W = W.value, $ = ce + H(W, le++), te += j(
          W,
          M,
          K,
          $,
          Z
        );
    else if ($ === "object") {
      if (typeof p.then == "function")
        return j(
          P(p),
          M,
          K,
          W,
          Z
        );
      throw M = String(p), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function B(p, M, K) {
    if (p == null) return p;
    var W = [], Z = 0;
    return j(p, W, "", "", function($) {
      return M.call(K, $, Z++);
    }), W;
  }
  function I(p) {
    if (p._status === -1) {
      var M = p._result;
      M = M(), M.then(
        function(K) {
          (p._status === 0 || p._status === -1) && (p._status = 1, p._result = K);
        },
        function(K) {
          (p._status === 0 || p._status === -1) && (p._status = 2, p._result = K);
        }
      ), p._status === -1 && (p._status = 0, p._result = M);
    }
    if (p._status === 1) return p._result.default;
    throw p._result;
  }
  var S = typeof reportError == "function" ? reportError : function(p) {
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
  }, Y = {
    map: B,
    forEach: function(p, M, K) {
      B(
        p,
        function() {
          M.apply(this, arguments);
        },
        K
      );
    },
    count: function(p) {
      var M = 0;
      return B(p, function() {
        M++;
      }), M;
    },
    toArray: function(p) {
      return B(p, function(M) {
        return M;
      }) || [];
    },
    only: function(p) {
      if (!V(p))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return p;
    }
  };
  return J.Activity = f, J.Children = Y, J.Component = _, J.Fragment = n, J.Profiler = o, J.PureComponent = k, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
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
  }, J.cloneElement = function(p, M, K) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var W = w({}, p.props), Z = p.key;
    if (M != null)
      for ($ in M.key !== void 0 && (Z = "" + M.key), M)
        !D.call(M, $) || $ === "key" || $ === "__self" || $ === "__source" || $ === "ref" && M.ref === void 0 || (W[$] = M[$]);
    var $ = arguments.length - 2;
    if ($ === 1) W.children = K;
    else if (1 < $) {
      for (var te = Array($), ce = 0; ce < $; ce++)
        te[ce] = arguments[ce + 2];
      W.children = te;
    }
    return x(p.type, Z, W);
  }, J.createContext = function(p) {
    return p = {
      $$typeof: s,
      _currentValue: p,
      _currentValue2: p,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, p.Provider = p, p.Consumer = {
      $$typeof: c,
      _context: p
    }, p;
  }, J.createElement = function(p, M, K) {
    var W, Z = {}, $ = null;
    if (M != null)
      for (W in M.key !== void 0 && ($ = "" + M.key), M)
        D.call(M, W) && W !== "key" && W !== "__self" && W !== "__source" && (Z[W] = M[W]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = K;
    else if (1 < te) {
      for (var ce = Array(te), le = 0; le < te; le++)
        ce[le] = arguments[le + 2];
      Z.children = ce;
    }
    if (p && p.defaultProps)
      for (W in te = p.defaultProps, te)
        Z[W] === void 0 && (Z[W] = te[W]);
    return x(p, $, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(p) {
    return { $$typeof: u, render: p };
  }, J.isValidElement = V, J.lazy = function(p) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: p },
      _init: I
    };
  }, J.memo = function(p, M) {
    return {
      $$typeof: i,
      type: p,
      compare: M === void 0 ? null : M
    };
  }, J.startTransition = function(p) {
    var M = b.T, K = {};
    b.T = K;
    try {
      var W = p(), Z = b.S;
      Z !== null && Z(K, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(C, S);
    } catch ($) {
      S($);
    } finally {
      M !== null && K.types !== null && (M.types = K.types), b.T = M;
    }
  }, J.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, J.use = function(p) {
    return b.H.use(p);
  }, J.useActionState = function(p, M, K) {
    return b.H.useActionState(p, M, K);
  }, J.useCallback = function(p, M) {
    return b.H.useCallback(p, M);
  }, J.useContext = function(p) {
    return b.H.useContext(p);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(p, M) {
    return b.H.useDeferredValue(p, M);
  }, J.useEffect = function(p, M) {
    return b.H.useEffect(p, M);
  }, J.useEffectEvent = function(p) {
    return b.H.useEffectEvent(p);
  }, J.useId = function() {
    return b.H.useId();
  }, J.useImperativeHandle = function(p, M, K) {
    return b.H.useImperativeHandle(p, M, K);
  }, J.useInsertionEffect = function(p, M) {
    return b.H.useInsertionEffect(p, M);
  }, J.useLayoutEffect = function(p, M) {
    return b.H.useLayoutEffect(p, M);
  }, J.useMemo = function(p, M) {
    return b.H.useMemo(p, M);
  }, J.useOptimistic = function(p, M) {
    return b.H.useOptimistic(p, M);
  }, J.useReducer = function(p, M, K) {
    return b.H.useReducer(p, M, K);
  }, J.useRef = function(p) {
    return b.H.useRef(p);
  }, J.useState = function(p) {
    return b.H.useState(p);
  }, J.useSyncExternalStore = function(p, M, K) {
    return b.H.useSyncExternalStore(
      p,
      M,
      K
    );
  }, J.useTransition = function() {
    return b.H.useTransition();
  }, J.version = "19.2.4", J;
}
var en;
function _r() {
  return en || (en = 1, wt.exports = vr()), wt.exports;
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
var tn;
function Cr() {
  if (tn) return he;
  tn = 1;
  var l = _r();
  function t(r) {
    var i = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      i += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        i += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + r + "; visit " + i + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function c(r, i, d) {
    var f = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: o,
      key: f == null ? null : "" + f,
      children: r,
      containerInfo: i,
      implementation: d
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, i) {
    if (r === "font") return "";
    if (typeof i == "string")
      return i === "use-credentials" ? i : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(r, i) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!i || i.nodeType !== 1 && i.nodeType !== 9 && i.nodeType !== 11)
      throw Error(t(299));
    return c(r, i, null, d);
  }, he.flushSync = function(r) {
    var i = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, r) return r();
    } finally {
      s.T = i, a.p = d, a.d.f();
    }
  }, he.preconnect = function(r, i) {
    typeof r == "string" && (i ? (i = i.crossOrigin, i = typeof i == "string" ? i === "use-credentials" ? i : "" : void 0) : i = null, a.d.C(r, i));
  }, he.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, he.preinit = function(r, i) {
    if (typeof r == "string" && i && typeof i.as == "string") {
      var d = i.as, f = u(d, i.crossOrigin), m = typeof i.integrity == "string" ? i.integrity : void 0, h = typeof i.fetchPriority == "string" ? i.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof i.precedence == "string" ? i.precedence : void 0,
        {
          crossOrigin: f,
          integrity: m,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: f,
        integrity: m,
        fetchPriority: h,
        nonce: typeof i.nonce == "string" ? i.nonce : void 0
      });
    }
  }, he.preinitModule = function(r, i) {
    if (typeof r == "string")
      if (typeof i == "object" && i !== null) {
        if (i.as == null || i.as === "script") {
          var d = u(
            i.as,
            i.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: d,
            integrity: typeof i.integrity == "string" ? i.integrity : void 0,
            nonce: typeof i.nonce == "string" ? i.nonce : void 0
          });
        }
      } else i == null && a.d.M(r);
  }, he.preload = function(r, i) {
    if (typeof r == "string" && typeof i == "object" && i !== null && typeof i.as == "string") {
      var d = i.as, f = u(d, i.crossOrigin);
      a.d.L(r, d, {
        crossOrigin: f,
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
  }, he.preloadModule = function(r, i) {
    if (typeof r == "string")
      if (i) {
        var d = u(i.as, i.crossOrigin);
        a.d.m(r, {
          as: typeof i.as == "string" && i.as !== "script" ? i.as : void 0,
          crossOrigin: d,
          integrity: typeof i.integrity == "string" ? i.integrity : void 0
        });
      } else a.d.m(r);
  }, he.requestFormReset = function(r) {
    a.d.r(r);
  }, he.unstable_batchedUpdates = function(r, i) {
    return r(i);
  }, he.useFormState = function(r, i, d) {
    return s.H.useFormState(r, i, d);
  }, he.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var nn;
function yr() {
  if (nn) return yt.exports;
  nn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), yt.exports = Cr(), yt.exports;
}
var fn = yr();
const { useState: xe, useCallback: be, useRef: qe, useEffect: $e, useMemo: Mt } = e;
function Bt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(it, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function wr({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: o,
  onDragStart: c,
  onDragOver: s,
  onDrop: u,
  onDragEnd: r,
  dragClassName: i
}) {
  const d = be(
    (f) => {
      f.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (i ? " " + i : ""),
      draggable: o || void 0,
      onDragStart: c,
      onDragOver: s,
      onDrop: u,
      onDragEnd: r
    },
    o && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Bt, { image: l.image }),
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
function kr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: c
}) {
  const s = be(() => a(l.value), [a, l.value]), u = Mt(() => {
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
      onClick: s,
      onMouseEnter: o
    },
    /* @__PURE__ */ e.createElement(Bt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Nr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, c = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, i = t.optionsLoaded === !0, d = t.options ?? [], f = t.emptyOptionLabel ?? "", m = c && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], w = be(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [v, _] = xe(!1), [y, k] = xe(""), [L, E] = xe(-1), [C, b] = xe(!1), [D, x] = xe({}), [T, V] = xe(null), [F, A] = xe(null), [H, P] = xe(null), j = qe(null), B = qe(null), I = qe(null), S = qe(a);
  S.current = a;
  const Y = qe(-1), p = Mt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = Mt(() => {
    let O = d.filter((X) => !p.has(X.value));
    if (y) {
      const X = y.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(X));
    }
    return O;
  }, [d, p, y]);
  $e(() => {
    y && M.length === 1 ? E(0) : E(-1);
  }, [M.length, y]), $e(() => {
    v && i && B.current && B.current.focus();
  }, [v, i, a]), $e(() => {
    var ae, ie;
    if (Y.current < 0) return;
    const O = Y.current;
    Y.current = -1;
    const X = (ae = j.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    X && X.length > 0 ? X[Math.min(O, X.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), $e(() => {
    if (!v) return;
    const O = (X) => {
      j.current && !j.current.contains(X.target) && I.current && !I.current.contains(X.target) && (_(!1), k(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [v]), $e(() => {
    if (!v || !j.current) return;
    const O = j.current.getBoundingClientRect(), X = window.innerHeight - O.bottom, ie = X < 300 && O.top > X;
    x({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [v]);
  const K = be(async () => {
    if (!(u || !r) && (_(!0), k(""), E(-1), b(!1), !i))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [u, r, i, n]), W = be(() => {
    var O;
    _(!1), k(""), E(-1), (O = j.current) == null || O.focus();
  }, []), Z = be(
    (O) => {
      let X;
      if (o) {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          X = [...S.current, ae];
        else
          return;
      } else {
        const ae = d.find((ie) => ie.value === O);
        if (ae)
          X = [ae];
        else
          return;
      }
      S.current = X, n(tt, { value: X.map((ae) => ae.value) }), o ? (k(""), E(-1)) : W();
    },
    [o, d, n, W]
  ), $ = be(
    (O) => {
      Y.current = S.current.findIndex((ae) => ae.value === O);
      const X = S.current.filter((ae) => ae.value !== O);
      S.current = X, n(tt, { value: X.map((ae) => ae.value) });
    },
    [n]
  ), te = be(
    (O) => {
      O.stopPropagation(), n(tt, { value: [] }), W();
    },
    [n, W]
  ), ce = be((O) => {
    k(O.target.value);
  }, []), le = be(
    (O) => {
      if (!v) {
        if (O.key === "ArrowDown" || O.key === "ArrowUp" || O.key === "Enter" || O.key === " ") {
          if (O.target.tagName === "BUTTON") return;
          O.preventDefault(), O.stopPropagation(), K();
        }
        return;
      }
      switch (O.key) {
        case "ArrowDown":
          O.preventDefault(), O.stopPropagation(), E(
            (X) => X < M.length - 1 ? X + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), E(
            (X) => X > 0 ? X - 1 : M.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), L >= 0 && L < M.length && Z(M[L].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          y === "" && o && a.length > 0 && $(a[a.length - 1].value);
          break;
      }
    },
    [
      v,
      K,
      W,
      M,
      L,
      Z,
      y,
      o,
      a,
      $
    ]
  ), ge = be(
    async (O) => {
      O.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), ve = be(
    (O, X) => {
      V(O), X.dataTransfer.effectAllowed = "move", X.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), we = be(
    (O, X) => {
      if (X.preventDefault(), X.dataTransfer.dropEffect = "move", T === null || T === O) {
        A(null), P(null);
        return;
      }
      const ae = X.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Ye = X.clientX < ie ? "before" : "after";
      A(O), P(Ye);
    },
    [T]
  ), Te = be(
    (O) => {
      if (O.preventDefault(), T === null || F === null || H === null || T === F) return;
      const X = [...S.current], [ae] = X.splice(T, 1);
      let ie = F;
      T < F ? ie = H === "before" ? ie - 1 : ie : ie = H === "before" ? ie : ie + 1, X.splice(ie, 0, ae), S.current = X, n(tt, { value: X.map((Ye) => Ye.value) }), V(null), A(null), P(null);
    },
    [T, F, H, n]
  ), Re = be(() => {
    V(null), A(null), P(null);
  }, []);
  if ($e(() => {
    if (L < 0 || !I.current) return;
    const O = I.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Bt, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const Oe = !s && a.length > 0 && !u, et = v ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: I,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...Rn
    },
    (i || C) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ce,
        onKeyDown: le,
        placeholder: h["js.dropdownSelect.filterPlaceholder"],
        "aria-label": h["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": L >= 0 ? `${l}-opt-${L}` : void 0,
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
      !i && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ge }, h["js.dropdownSelect.error"])),
      i && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      i && M.map((O, X) => /* @__PURE__ */ e.createElement(
        kr,
        {
          key: O.value,
          id: `${l}-opt-${X}`,
          option: O,
          highlighted: X === L,
          searchTerm: y,
          onSelect: Z,
          onMouseEnter: () => E(X)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: j,
      className: "tlDropdownSelect" + (v ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": v,
      "aria-haspopup": "listbox",
      "aria-owns": v ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: v ? void 0 : K,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, f) : a.map((O, X) => {
      let ae = "";
      return T === X ? ae = "tlDropdownSelect__chip--dragging" : F === X && H === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : F === X && H === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        wr,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !s),
          onRemove: $,
          removeLabel: w(O.label),
          draggable: m,
          onDragStart: m ? (ie) => ve(X, ie) : void 0,
          onDragOver: m ? (ie) => we(X, ie) : void 0,
          onDrop: m ? Te : void 0,
          onDragEnd: m ? Re : void 0,
          dragClassName: m ? ae : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Oe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, v ? "▲" : "▼"))
  ), et && fn.createPortal(et, document.body));
}, { useCallback: kt, useRef: Sr } = e, hn = "application/x-tl-color", Dr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: c
}) => {
  const s = Sr(null), u = kt(
    (d) => (f) => {
      s.current = d, f.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = kt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), i = kt(
    (d) => (f) => {
      f.preventDefault();
      const m = f.dataTransfer.getData(hn);
      m ? c(d, m) : s.current !== null && s.current !== d && o(s.current, d), s.current = null;
    },
    [o, c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((d, f) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: f,
        className: "tlColorInput__paletteCell" + (d == null ? " tlColorInput__paletteCell--empty" : ""),
        style: d != null ? { backgroundColor: d } : void 0,
        title: d ?? "",
        draggable: d != null,
        onClick: d != null ? () => n(d) : void 0,
        onDoubleClick: d != null ? () => a(d) : void 0,
        onDragStart: d != null ? u(f) : void 0,
        onDragOver: r,
        onDrop: i(f)
      }
    ))
  );
};
function bn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Pt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function gn(l) {
  if (!Pt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function En(l, t, n) {
  const a = (o) => bn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Tr(l, t, n) {
  const a = l / 255, o = t / 255, c = n / 255, s = Math.max(a, o, c), u = Math.min(a, o, c), r = s - u;
  let i = 0;
  r !== 0 && (s === a ? i = (o - c) / r % 6 : s === o ? i = (c - a) / r + 2 : i = (a - o) / r + 4, i *= 60, i < 0 && (i += 360));
  const d = s === 0 ? 0 : r / s;
  return [i, d, s];
}
function Rr(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let s = 0, u = 0, r = 0;
  return l < 60 ? (s = a, u = o, r = 0) : l < 120 ? (s = o, u = a, r = 0) : l < 180 ? (s = 0, u = a, r = o) : l < 240 ? (s = 0, u = o, r = a) : l < 300 ? (s = o, u = 0, r = a) : (s = a, u = 0, r = o), [
    Math.round((s + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function Lr(l) {
  return Tr(...gn(l));
}
function Nt(l, t, n) {
  return En(...Rr(l, t, n));
}
const { useCallback: He, useRef: ln } = e, xr = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = Lr(l), c = ln(null), s = ln(null), u = He(
    (g, w) => {
      var k;
      const v = (k = c.current) == null ? void 0 : k.getBoundingClientRect();
      if (!v) return;
      const _ = Math.max(0, Math.min(1, (g - v.left) / v.width)), y = Math.max(0, Math.min(1, 1 - (w - v.top) / v.height));
      t(Nt(n, _, y));
    },
    [n, t]
  ), r = He(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), u(g.clientX, g.clientY);
    },
    [u]
  ), i = He(
    (g) => {
      g.buttons !== 0 && u(g.clientX, g.clientY);
    },
    [u]
  ), d = He(
    (g) => {
      var y;
      const w = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!w) return;
      const _ = Math.max(0, Math.min(1, (g - w.top) / w.height)) * 360;
      t(Nt(_, a, o));
    },
    [a, o, t]
  ), f = He(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), m = He(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Nt(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: h },
      onPointerDown: r,
      onPointerMove: i
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
      onPointerDown: f,
      onPointerMove: m
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
function Ir(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Mr = {
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
}, { useState: at, useCallback: ke, useEffect: an, useRef: Pr, useLayoutEffect: jr } = e, Ar = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: o,
  canReset: c,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [i, d] = at("palette"), [f, m] = at(t), h = Pr(null), g = ue(Mr), [w, v] = at(null);
  jr(() => {
    if (!l.current || !h.current) return;
    const I = l.current.getBoundingClientRect(), S = h.current.getBoundingClientRect();
    let Y = I.bottom + 4, p = I.left;
    Y + S.height > window.innerHeight && (Y = I.top - S.height - 4), p + S.width > window.innerWidth && (p = Math.max(0, I.right - S.width)), v({ top: Y, left: p });
  }, [l]);
  const _ = f != null, [y, k, L] = _ ? gn(f) : [0, 0, 0], [E, C] = at((f == null ? void 0 : f.toUpperCase()) ?? "");
  an(() => {
    C((f == null ? void 0 : f.toUpperCase()) ?? "");
  }, [f]), Be(!0, { ESCAPE: u }), an(() => {
    const I = (Y) => {
      h.current && !h.current.contains(Y.target) && u();
    }, S = setTimeout(() => document.addEventListener("mousedown", I), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", I);
    };
  }, [u]);
  const b = ke(
    (I) => (S) => {
      const Y = parseInt(S.target.value, 10);
      if (isNaN(Y)) return;
      const p = bn(Y);
      m(En(I === "r" ? p : y, I === "g" ? p : k, I === "b" ? p : L));
    },
    [y, k, L]
  ), D = ke(
    (I) => {
      if (f != null) {
        I.dataTransfer.setData(hn, f.toUpperCase()), I.dataTransfer.effectAllowed = "move";
        const S = document.createElement("div");
        S.style.width = "33px", S.style.height = "33px", S.style.backgroundColor = f, S.style.borderRadius = "3px", S.style.border = "1px solid rgba(0,0,0,0.1)", S.style.position = "absolute", S.style.top = "-9999px", document.body.appendChild(S), I.dataTransfer.setDragImage(S, 16, 16), requestAnimationFrame(() => document.body.removeChild(S));
      }
    },
    [f]
  ), x = ke((I) => {
    const S = I.target.value;
    C(S), Pt(S) && m(S);
  }, []), T = ke(() => {
    m(null);
  }, []), V = ke((I) => {
    m(I);
  }, []), F = ke(
    (I) => {
      s(I);
    },
    [s]
  ), A = ke(
    (I, S) => {
      const Y = [...n], p = Y[I];
      Y[I] = Y[S], Y[S] = p, r(Y);
    },
    [n, r]
  ), H = ke(
    (I, S) => {
      const Y = [...n];
      Y[I] = S, r(Y);
    },
    [n, r]
  ), P = ke(() => {
    r([...o]);
  }, [o, r]), j = ke(
    (I) => {
      if (Ir(n, I)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const Y = [...n];
      Y[S] = I.toUpperCase(), r(Y);
    },
    [n, r]
  ), B = ke(() => {
    f != null && j(f), s(f);
  }, [f, s, j]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: w ? { top: w.top, left: w.left, visibility: "visible" } : { visibility: "hidden" }
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
      Dr,
      {
        colors: n,
        columns: a,
        onSelect: V,
        onConfirm: F,
        onSwap: A,
        onReplace: H
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(xr, { color: f ?? "#000000", onColorChange: m }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (_ ? "" : " tlColorInput--noColor"),
        style: _ ? { backgroundColor: f } : void 0,
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
        value: _ ? L : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (E !== "" && !Pt(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: x
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, g["js.colorInput.ok"]))
  );
}, Br = { "js.colorInput.chooseColor": "Choose color" }, { useState: Or, useCallback: rt, useRef: Fr } = e, $r = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), c = ue(Br), [s, u] = Or(!1), r = Fr(null), i = n, d = t.editable !== !1, f = t.palette ?? [], m = t.paletteColumns ?? 6, h = t.defaultPalette ?? f, g = rt(() => {
    d && u(!0);
  }, [d]), w = rt(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), v = rt(() => {
    u(!1);
  }, []), _ = rt(
    (y) => {
      o("paletteChanged", { palette: y });
    },
    [o]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (i == null ? " tlColorInput__swatch--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      onClick: g,
      disabled: t.disabled === !0,
      title: i ?? "",
      "aria-label": c["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    Ar,
    {
      anchorRef: r,
      currentColor: i,
      palette: f,
      paletteColumns: m,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: w,
      onCancel: v,
      onPaletteChange: _
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
}, { useState: Ze, useCallback: Pe, useEffect: St, useRef: rn, useLayoutEffect: Hr, useMemo: Wr } = e, Ur = {
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
}, zr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: c,
  onLoadIcons: s
}) => {
  const u = ue(Ur), [r, i] = Ze("simple"), [d, f] = Ze(""), [m, h] = Ze(t ?? ""), [g, w] = Ze(!1), [v, _] = Ze(null), y = rn(null), k = rn(null);
  Hr(() => {
    if (!l.current || !y.current) return;
    const F = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let H = F.bottom + 4, P = F.left;
    H + A.height > window.innerHeight && (H = F.top - A.height - 4), P + A.width > window.innerWidth && (P = Math.max(0, F.right - A.width)), _({ top: H, left: P });
  }, [l]), St(() => {
    !a && !g && s().catch(() => w(!0));
  }, [a, g, s]), St(() => {
    a && k.current && k.current.focus();
  }, [a]), Be(!0, { ESCAPE: c }), St(() => {
    const F = (H) => {
      y.current && !y.current.contains(H.target) && c();
    }, A = setTimeout(() => document.addEventListener("mousedown", F), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", F);
    };
  }, [c]);
  const L = Wr(() => {
    if (!d) return n;
    const F = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(F) || A.label.toLowerCase().includes(F) || A.terms != null && A.terms.some((H) => H.includes(F))
    );
  }, [n, d]), E = Pe((F) => {
    f(F.target.value);
  }, []), C = Pe(
    (F) => {
      o(F);
    },
    [o]
  ), b = Pe((F) => {
    h(F);
  }, []), D = Pe((F) => {
    h(F.target.value);
  }, []), x = Pe(() => {
    o(m || null);
  }, [m, o]), T = Pe(() => {
    o(null);
  }, [o]), V = Pe(async (F) => {
    F.preventDefault(), w(!1);
    try {
      await s();
    } catch {
      w(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => i("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
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
        onChange: E,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), d && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => f(""),
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      a && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && L.map(
        (F) => F.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: F.label,
            onClick: () => r === "simple" ? C(A.encoded) : b(A.encoded),
            onKeyDown: (H) => {
              (H.key === "Enter" || H.key === " ") && (H.preventDefault(), r === "simple" ? C(A.encoded) : b(A.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: A.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: m,
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, m && /* @__PURE__ */ e.createElement(Se, { encoded: m })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, m ? m.startsWith("css:") ? m.substring(4) : m : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: x }, u["js.iconSelect.ok"]))
  );
}, Vr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Kr, useCallback: ot, useRef: Yr } = e, Gr = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), c = ue(Vr), [s, u] = Kr(!1), r = Yr(null), i = n, d = t.editable !== !1, f = t.disabled === !0, m = t.icons ?? [], h = t.iconsLoaded === !0, g = ot(() => {
    d && !f && u(!0);
  }, [d, f]), w = ot(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), v = ot(() => {
    u(!1);
  }, []), _ = ot(async () => {
    await o("loadIcons");
  }, [o]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (i == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: g,
      disabled: f,
      title: i ?? "",
      "aria-label": c["js.iconSelect.chooseIcon"]
    },
    i ? /* @__PURE__ */ e.createElement(Se, { encoded: i }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    zr,
    {
      anchorRef: r,
      currentValue: i,
      icons: m,
      iconsLoaded: h,
      onSelect: w,
      onCancel: v,
      onLoadIcons: _
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(Se, { encoded: i }) : null));
}, { useCallback: We, useEffect: Xr, useMemo: on, useRef: qr, useState: Dt } = e, Zr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Qr = [1, 2, 3, 4];
function Jr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function eo(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Qr)
    n >= o && (a = o);
  return a;
}
function to(l, t) {
  const n = Zr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function no(l, t) {
  const n = Math.max(1, t), a = {}, o = (f, m) => !!(a[f] && a[f][m]), c = (f, m) => {
    a[f] || (a[f] = {}), a[f][m] = !0;
  }, s = [];
  let u = 0, r = 0;
  const i = (f) => {
    let m = null;
    for (const g of s) g.rowStart === f && (m = g);
    if (!m) return;
    let h = m.colEnd;
    for (; h < n && !o(f, h); ) h++;
    if (h !== m.colEnd) {
      for (let g = m.rowStart; g < m.rowEnd; g++)
        for (let w = m.colEnd; w < h; w++) c(g, w);
      m.colEnd = h;
    }
  };
  for (const f of l) {
    const m = n <= 1 ? 1 : Math.max(1, f.rowSpan || 1);
    let h = Math.min(to(f.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let g = 0;
    for (let k = r; k < n && !o(u, k); k++)
      g++;
    if (h > g) {
      for (i(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      g = 0;
      for (let k = r; k < n && !o(u, k); k++)
        g++;
      h = Math.min(h, g);
    }
    const w = r, v = r + h, _ = u, y = u + m;
    s.push({ id: f.id, colStart: w, colEnd: v, rowStart: _, rowEnd: y });
    for (let k = _; k < y; k++)
      for (let L = w; L < v; L++) c(k, L);
    r = v, r >= n && (r = 0, u++);
  }
  i(u);
  let d = 0;
  for (const f of s) f.rowEnd > d && (d = f.rowEnd);
  for (let f = 1; f < d; f++)
    for (let m = 0; m < n; m++) {
      if (o(f, m)) continue;
      const h = s.find((g) => g.rowEnd === f && g.colStart <= m && m < g.colEnd);
      if (h) {
        h.rowEnd = f + 1;
        for (let g = h.colStart; g < h.colEnd; g++) c(f, g);
      }
    }
  return s;
}
const lo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((C) => C && C.id), c = qr(null), [s, u] = Dt(1), r = t.editMode === !0;
  Xr(() => {
    const C = c.current;
    if (!C) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = Jr(a, b), x = () => u(eo(C.clientWidth, D));
    x();
    const T = new ResizeObserver(x);
    return T.observe(C), () => T.disconnect();
  }, [a]);
  const i = on(() => no(o, s), [o, s]), d = on(() => {
    const C = {};
    for (const b of i) C[b.id] = b;
    return C;
  }, [i]), [f, m] = Dt(null), [h, g] = Dt(null), w = We((C, b) => {
    if (!r) {
      C.preventDefault();
      return;
    }
    m(b), C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", b);
  }, [r]), v = We((C, b) => {
    if (!r || !f || f === b) return;
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const D = C.currentTarget.getBoundingClientRect(), x = C.clientX < D.left + D.width / 2;
    g((T) => T && T.id === b && T.before === x ? T : { id: b, before: x });
  }, [r, f]), _ = We(() => {
  }, []), y = We((C, b, D) => {
    const x = o.map((A) => A.id), T = x.indexOf(C);
    if (T < 0) return;
    x.splice(T, 1);
    const V = x.indexOf(b);
    if (V < 0) {
      x.splice(T, 0, C);
      return;
    }
    const F = D ? V : V + 1;
    x.splice(F, 0, C), n("reorder", { order: x });
  }, [o, n]), k = We((C, b) => {
    if (!r || !f || f === b) return;
    C.preventDefault();
    const D = C.currentTarget.getBoundingClientRect(), x = C.clientX < D.left + D.width / 2;
    y(f, b, x), m(null), g(null);
  }, [r, f, y]), L = We(() => {
    m(null), g(null);
  }, []), E = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: c,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: E }, o.map((C) => {
      const b = d[C.id];
      if (!b) return null;
      const D = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, x = ["tlDashboard__tile"];
      return f === C.id && x.push("tlDashboard__tile--dragging"), h && h.id === C.id && x.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C.id,
          className: x.join(" "),
          style: D,
          draggable: r,
          onDragStart: (T) => w(T, C.id),
          onDragOver: (T) => v(T, C.id),
          onDragLeave: _,
          onDrop: (T) => k(T, C.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(q, { control: C.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ao, useRef: sn, useState: cn, useEffect: ro, useLayoutEffect: oo } = e, so = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(q, { control: n }))));
}, co = ({ group: l }) => {
  var f, m;
  const [t, n] = cn(!1), [a, o] = cn({}), c = sn(null), s = sn(null), u = ao(() => {
    n((h) => !h);
  }, []);
  oo(() => {
    if (!t) return;
    const h = () => {
      const g = c.current;
      if (!g) return;
      const w = g.getBoundingClientRect();
      o({
        position: "fixed",
        top: w.bottom + 4,
        right: Math.max(8, window.innerWidth - w.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), ro(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && c.current && !c.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Be(t, { ESCAPE: () => n(!1) }), At(t, s, "first");
  const r = l.items.filter((h) => h != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((f = l.subGroups) != null && f.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(q, { control: r[0] })));
  const i = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
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
  ), fn.createPortal(
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
      r.map((h, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(q, { control: h }))),
      (m = l.subGroups) == null ? void 0 : m.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((w, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(q, { control: w })))))
    ),
    document.body
  ));
}, io = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((o) => o.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(co, { group: o }) : /* @__PURE__ */ e.createElement(so, { group: o }))));
}, uo = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(q, { control: t.frame }));
}, mo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.content, o = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, o && o.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, o.map((c, s) => {
    const u = s === o.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: c.depth }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, c.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: c.depth })
      },
      c.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(q, { control: a })));
}, po = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })));
}, fo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ho = {
  "js.sidebar.openDrawer": "Open navigation"
}, bo = ({ controlId: l }) => {
  const t = ne(), n = ue(ho);
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
U("TLButton", Gn);
U("TLUploadButton", Xn);
U("TLToggleButton", Zn);
U("TLTextInput", In);
U("TLPasswordInput", Pn);
U("TLNumberInput", An);
U("TLDatePicker", On);
U("TLSelect", $n);
U("TLBooleanChoice", Wn);
U("TLCheckbox", Kn);
U("TLCounter", Qn);
U("TLTabBar", el);
U("TLFieldList", tl);
U("TLAudioRecorder", ll);
U("TLAudioPlayer", rl);
U("TLFileUpload", sl);
U("TLBinaryField", il);
U("TLFileChips", ml);
U("TLRelativeTime", hl);
U("TLAnchor", bl);
U("TLScrollLink", gl);
U("TLAvatar", _l);
U("TLDownload", yl);
U("TLPhotoCapture", kl);
U("TLPhotoViewer", Sl);
U("TLPdfViewer", Tl);
U("TLSplitPanel", Rl);
U("TLPanel", Al);
U("TLInset", Gl);
U("TLMaximizeRoot", Bl);
U("TLDeckPane", Ol);
U("TLSidebar", Kl);
U("TLStack", Yl);
U("TLGrid", Xl);
U("TLCard", ql);
U("TLAppBar", Zl);
U("TLBreadcrumb", Jl);
U("TLBottomBar", ta);
U("TLDialog", aa);
U("TLDialogManager", sa);
U("TLWindow", da);
U("TLDrawer", fa);
U("TLContextMenuRegion", ba);
U("TLSnackbar", _a);
U("TLNoticeBar", Da);
U("TLMenu", Ra);
U("TLAppShell", xa);
U("TLText", Ia);
U("TLTableView", Ba);
U("TLColumnSelect", Fa);
U("TLCalendar", Ja);
U("TLFormLayout", cr);
U("TLFormGroup", dr);
U("TLFormField", hr);
U("TLResourceCell", br);
U("TLTreeView", Er);
U("TLDropdownSelect", Nr);
U("TLColorInput", $r);
U("TLIconSelect", Gr);
U("TLDashboard", lo);
U("TLToolbar", io);
U("TLTileStack", uo);
U("TLAdaptiveDetail", mo);
U("TLSlot", po);
U("TLSlotContent", fo);
U("TLDrawerToggle", bo);
