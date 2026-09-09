import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as X, useKeyboardBinding as me, useTLUpload as Ve, TLChild as G, useI18N as ue, useTLDataUrl as Ke, scrollToAnchor as Rn, useStandaloneKeyboardScope as Be, KeyboardScopeProvider as Bt, useFocusTrap as Ot, CMD_VALUE_CHANGED as tt, anchoredOverlayProps as Ln, register as U } from "tl-react-bridge";
const { useCallback: Ut, useRef: xn } = e, Mn = 300, In = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: Mn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = ne(), s = xn(!1), u = Ut(
    (w) => {
      s.current = !0, a(w.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = Ut(async () => {
    await o(), r && s.current && (s.current = !1, i("commit"));
  }, [o, r, i]), d = t.multiline === !0;
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
      onChange: u,
      onBlur: c,
      disabled: t.disabled === !0,
      className: g,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: zt } = e, Pn = 300, jn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: Pn }), i = zt(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = zt(() => {
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
}, { useCallback: Vt } = e, An = 300, Bn = ({ controlId: l, state: t, config: n }) => {
  const [a, o, i] = De({ debounceMs: An }), s = Vt(
    (p) => {
      const h = p.target.value;
      o(h === "" ? null : h);
    },
    [o]
  ), u = Vt(() => {
    i();
  }, [i]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, c = t.hasWarnings === !0, d = t.errorMessage, m = [
    "tlReactNumberInput",
    r ? "tlReactNumberInput--error" : "",
    !r && c ? "tlReactNumberInput--warning" : ""
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
      className: m,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: On } = e, Fn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = On(
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
}, { useCallback: $n } = e, Hn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), i = $n(
    (m) => {
      o(m.target.value || null);
    },
    [o]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((d = s.find((p) => p.value === a)) == null ? void 0 : d.label) ?? "";
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
}, { useCallback: Wn } = e, Un = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], i = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, c = Wn(
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
      "aria-invalid": u || void 0
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
}, { useCallback: zn, useRef: Vn, useEffect: Kn } = e, Yn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, i = Vn(null);
  Kn(() => {
    i.current && (i.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = zn(
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
const { useCallback: Gn } = e, Xn = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: i }) => {
  const s = X(), u = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = i ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, w = s.appearance, v = s.size, _ = s.cssClasses, C = s.navigateUrl, k = Gn(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(r);
  }, [u, r, C]), L = s.keyGesture;
  me(L, () => m || h ? !1 : (k(), !0));
  const E = p === "icon-only", y = p === "label-only" || p === "icon-label" || E && !d, b = g ?? (E ? c : void 0), S = b ? `text:${b}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : "") + (_ ? " " + _ : ""),
      "data-tooltip": S,
      "aria-label": d || E ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, qn = ({ controlId: l }) => {
  const t = X(), n = Ve(), a = e.useRef(null), [o, i] = e.useState(!1), s = t.label ?? "", u = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var L;
    r || o || (L = a.current) == null || L.click();
  }, [r, o]), w = e.useCallback(async (L) => {
    const E = L.target.files;
    if (!E || E.length === 0) return;
    const y = new FormData();
    for (let b = 0; b < E.length; b++)
      y.append("file", E[b], E[b].name);
    L.target.value = "", i(!0);
    try {
      await n(y);
    } finally {
      i(!1);
    }
  }, [n]), v = d === "icon-only", _ = d === "icon-only" || d === "icon-label", C = d === "label-only" || d === "icon-label" || v && !u, k = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
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
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": v ? s : void 0
    },
    _ && u && /* @__PURE__ */ e.createElement(Ne, { encoded: u, className: "tlReactButton__image" }),
    C && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: Zn } = e, Qn = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const i = X(), s = ne(), u = t ?? "click", r = n ?? i.label, c = a ?? i.active === !0, d = o ?? i.disabled === !0, m = Zn(() => {
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
}, Jn = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: el } = e, tl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.tabs ?? [], o = t.activeTabId, i = el((s) => {
    s !== o && n("selectTab", { tabId: s });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === o,
      className: "tlReactTabBar__tab" + (s.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(s.id)
    },
    s.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: s.icon, className: "tlReactTabBar__tabIcon" }),
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, nl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, ll = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, al = ({ controlId: l }) => {
  const t = X(), n = Ve(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), u = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (a !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = C, r.current = [];
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(C, k ? { mimeType: k } : void 0);
        u.current = L, L.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, L.onstop = async () => {
          C.getTracks().forEach((b) => b.stop()), c.current = null;
          const E = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const y = new FormData();
          y.append("audio", E, "recording.webm"), await n(y), o("idle");
        }, L.start(), o("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(ll), w = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], v = p === "uploading", _ = ["tlAudioRecorder__button"];
  return p === "recording" && _.push("tlAudioRecorder__button--recording"), p === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: h,
      disabled: v,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[i]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, rl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ol = ({ controlId: l }) => {
  const t = X(), n = Ke(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [i, s] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
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
  }, [i, n]), m = ue(rl), p = i === "loading" ? m["js.loading"] : i === "playing" ? m["js.audioPlayer.pause"] : i === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = i === "disabled" || i === "loading", g = ["tlAudioPlayer__button"];
  return i === "playing" && g.push("tlAudioPlayer__button--playing"), i === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, sl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, cl = ({ controlId: l }) => {
  const t = X(), n = Ve(), [a, o] = e.useState("idle"), [i, s] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (E) => {
    o("uploading");
    const y = new FormData();
    y.append("file", E, E.name), await n(y), o("idle");
  }, [n]), h = e.useCallback((E) => {
    var b;
    const y = (b = E.target.files) == null ? void 0 : b[0];
    y && p(y);
  }, [p]), g = e.useCallback(() => {
    var E;
    a !== "uploading" && ((E = u.current) == null || E.click());
  }, [a]), w = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), v = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), _ = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const y = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    y && p(y);
  }, [a, p]), C = m === "uploading", k = ue(sl), L = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
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
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: g,
        disabled: C,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, il = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, ul = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ve(), i = Ke(), s = ue(il), u = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, w] = e.useState("idle"), [v, _] = e.useState(!1), [C, k] = e.useState(!1), L = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || C)) {
      k(!0);
      try {
        const B = i + (i.includes("?") ? "&" : "?") + "rev=" + d, M = await fetch(B);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const D = await M.blob(), Y = URL.createObjectURL(D), f = document.createElement("a");
        f.href = Y, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(Y);
      } catch (B) {
        console.error("[TLBinaryField] Fetch error:", B);
      } finally {
        k(!1);
      }
    }
  }, [r, C, i, d, c]), y = e.useCallback(async (B) => {
    w("uploading");
    const M = new FormData();
    M.append("file", B, B.name), await o(M), w("idle");
  }, [o]), b = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", S = e.useCallback((B) => {
    var D;
    const M = (D = B.target.files) == null ? void 0 : D[0];
    M && y(M);
  }, [y]), x = e.useCallback(() => {
    var B;
    b || (B = L.current) == null || B.click();
  }, [b]), T = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), _(!0);
  }, []), V = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), _(!1);
  }, []), F = e.useCallback((B) => {
    var D;
    if (B.preventDefault(), B.stopPropagation(), _(!1), b) return;
    const M = (D = B.dataTransfer.files) == null ? void 0 : D[0];
    M && y(M);
  }, [b, y]), A = C ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), H = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (C ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: E,
      disabled: C,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
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
        accept: m || void 0,
        onChange: S,
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
}, dl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function ml(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const pl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ve(), o = Ke(), i = ue(dl), s = t.chips ?? [], u = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (E) => {
    const y = Array.from(E);
    if (y.length !== 0) {
      c(!0);
      try {
        const b = new FormData();
        for (const S of y)
          b.append("file", S, S.name);
        await a(b);
      } finally {
        c(!1);
      }
    }
  }, [a]), g = e.useCallback(async (E) => {
    if (E.hasData)
      try {
        const y = o + "&key=" + encodeURIComponent(E.key), b = await fetch(y);
        if (!b.ok) {
          console.error("[TLFileChips] Failed to fetch data:", b.status);
          return;
        }
        const S = await b.blob(), x = URL.createObjectURL(S), T = document.createElement("a");
        T.href = x, T.download = E.name, T.style.display = "none", document.body.appendChild(T), T.click(), document.body.removeChild(T), URL.revokeObjectURL(x);
      } catch (y) {
        console.error("[TLFileChips] Fetch error:", y);
      }
  }, [o]), w = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), v = e.useCallback(() => {
    var E;
    r || (E = p.current) == null || E.click();
  }, [r]), _ = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), m(!0));
  }, [u]), C = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), m(!1));
  }, [u]), k = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), m(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
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
      onDragLeave: C,
      onDrop: k
    },
    s.map((E) => {
      const y = i["js.download.file"].replace("{0}", E.name), b = i["js.fileChips.remove"].replace("{0}", E.name);
      return /* @__PURE__ */ e.createElement("span", { key: E.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => g(E),
          disabled: !E.hasData,
          title: E.hasData ? y : E.name
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
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, ml(E.size))
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
        ref: p,
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
}, fl = 3e4;
function hl(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const bl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => i((u) => u + 1), fl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, hl(n, o));
}, gl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, El = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (i) => {
    i.preventDefault(), Rn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function vl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function _l(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Cl = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${_l(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    vl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, yl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, wl = ({ controlId: l }) => {
  const t = X(), n = Ke(), a = ne(), o = !!t.hasData, i = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + i, w = await fetch(g);
        if (!w.ok) {
          console.error("[TLDownload] Failed to fetch data:", w.status);
          return;
        }
        const v = await w.blob(), _ = URL.createObjectURL(v), C = document.createElement("a");
        C.href = _, C.download = s, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(_);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, i, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = ue(yl);
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
}, kl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Nl = ({ controlId: l }) => {
  const t = X(), n = Ve(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), [u, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, w = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), v = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((T) => T.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    v(), o("idle");
  }, [v]), C = e.useCallback(async () => {
    var T;
    if (a !== "uploading") {
      if (s(null), !w) {
        (T = p.current) == null || T.click();
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
    const T = c.current, V = m.current;
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
    F.append("photo", V, V.name), await n(F), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
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
  const E = ue(kl), y = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const S = ["tlPhotoCapture__overlayVideo"];
  u && S.push("tlPhotoCapture__overlayVideo--mirrored");
  const x = ["tlPhotoCapture__mirrorBtn"];
  return u && x.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: C,
      disabled: a === "uploading",
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !w && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
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
        className: S.join(" "),
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[i]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Sl = {
  "js.photoViewer.alt": "Captured photo"
}, Dl = ({ controlId: l }) => {
  const t = X(), n = Ke(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [i, s] = e.useState(null), u = e.useRef(o);
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
  const r = ue(Sl);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, Tl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Rl = ({ controlId: l }) => {
  const t = X(), n = Ke(), a = !!t.hasPdf, o = t.dataRevision ?? 0, i = ue(Tl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: Kt, useRef: gt } = e, Ll = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.orientation, o = t.resizable === !0, i = t.children ?? [], s = a === "horizontal", u = i.length > 0 && i.every((v) => v.collapsed), r = !u && i.some((v) => v.collapsed), c = u ? !s : s, d = gt(null), m = gt(null), p = gt(null), h = Kt((v, _) => {
    const C = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? u && !c ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : _ !== void 0 ? C.flex = `0 0 ${_}px` : C.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (C.minWidth = s ? v.minSize : void 0, C.minHeight = s ? void 0 : v.minSize), C;
  }, [s, u, r, c]), g = Kt((v, _) => {
    v.preventDefault();
    const C = d.current;
    if (!C) return;
    const k = i[_], L = i[_ + 1], E = C.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    E.forEach((x) => {
      y.push(s ? x.offsetWidth : x.offsetHeight);
    }), p.current = y, m.current = {
      splitterIndex: _,
      startPos: s ? v.clientX : v.clientY,
      startSizeBefore: y[_],
      startSizeAfter: y[_ + 1],
      childBefore: k,
      childAfter: L
    };
    const b = (x) => {
      const T = m.current;
      if (!T || !p.current) return;
      const F = (s ? x.clientX : x.clientY) - T.startPos, A = T.childBefore.minSize || 0, H = T.childAfter.minSize || 0;
      let P = T.startSizeBefore + F, j = T.startSizeAfter - F;
      P < A && (j += P - A, P = A), j < H && (P += j - H, j = H), p.current[T.splitterIndex] = P, p.current[T.splitterIndex + 1] = j;
      const B = C.querySelectorAll(":scope > .tlSplitPanel__child"), M = B[T.splitterIndex], D = B[T.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${P}px`), D && (D.style.flex = `0 0 ${j}px`);
    }, S = () => {
      if (document.removeEventListener("mousemove", b), document.removeEventListener("mouseup", S), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const x = {};
        i.forEach((T, V) => {
          const F = T.control;
          F != null && F.controlId && p.current && (x[F.controlId] = p.current[V]);
        }), n("updateSizes", { sizes: x });
      }
      p.current = null, m.current = null;
    };
    document.addEventListener("mousemove", b), document.addEventListener("mouseup", S), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, s, n]), w = [];
  return i.forEach((v, _) => {
    if (w.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${v.collapsed && c ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: h(v)
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control })
      )
    ), o && _ < i.length - 1) {
      const C = i[_ + 1];
      !v.collapsed && !C.collapsed && w.push(
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
        flexDirection: c ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    w
  );
}, Rt = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Et } = e, xl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ml = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Il = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Pl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), jl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Al = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Bl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(xl), o = t.title, i = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = i === "MINIMIZED", w = i === "MAXIMIZED", v = i === "HIDDEN", _ = Et(() => {
    n("toggleMinimize");
  }, [n]), C = Et(() => {
    n("toggleMaximize");
  }, [n]), k = Et(() => {
    n("popOut");
  }, [n]);
  if (v)
    return null;
  const L = w ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = s && !w || u && !g || r, y = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || E;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: L
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !w && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(Il, null) : /* @__PURE__ */ e.createElement(Ml, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: w ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      w ? /* @__PURE__ */ e.createElement(jl, null) : /* @__PURE__ */ e.createElement(Pl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Al, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(G, { control: t.child })),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Rt, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, Ol = ({ controlId: l }) => {
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
}, Fl = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild }));
}, { useCallback: Ee, useState: ct, useEffect: Lt, useRef: ut } = e, $l = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function xt(l, t, n, a) {
  const o = [];
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      o.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? o.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (o.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && o.push(...xt(i.children, t, n, i.id)));
  return o;
}
const ze = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, Hl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: i,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ze, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(ze, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Wl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(ze, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Ul = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(ze, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), zl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Vl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: i }) => {
  const s = ut(null);
  Lt(() => {
    const c = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [i]), Be(!0, { ESCAPE: i });
  const u = Ee((c) => {
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
        /* @__PURE__ */ e.createElement(ze, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, Kl = ({
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
  onItemFocus: p,
  flyoutGroupId: h,
  onOpenFlyout: g,
  onCloseFlyout: w
}) => {
  const v = ut(null), [_, C] = ct(null), k = Ee(() => {
    a ? h === l.id ? w() : (v.current && C(v.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, w]), L = Ee((y) => {
    v.current = y, r(y);
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
      onFocus: () => c(l.id)
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
    Vl,
    {
      item: l,
      activeItemId: n,
      anchorRect: _,
      onSelect: o,
      onExecute: i,
      onClose: w
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((y) => /* @__PURE__ */ e.createElement(
    mn,
    {
      key: y.id,
      item: y,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: i,
      onToggleGroup: s,
      focusedId: d,
      setItemRef: m,
      onItemFocus: p,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: g,
      onCloseFlyout: w
    }
  ))));
}, mn = ({
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
  onCloseFlyout: p
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        Hl,
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
        Wl,
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
      return /* @__PURE__ */ e.createElement(Ul, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(zl, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Kl,
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
          onCloseFlyout: p
        }
      );
    }
    default:
      return null;
  }
}, Yl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue($l), o = t.items ?? [], i = t.activeItemId, s = t.collapsed, u = t.drawerOpen, r = u ? !1 : s, [c, d] = ct(() => {
    const A = /* @__PURE__ */ new Map(), H = (P) => {
      for (const j of P)
        j.type === "group" && (A.set(j.id, j.expanded), H(j.children));
    };
    return H(o), A;
  }), m = Ee((A) => {
    d((H) => {
      const P = new Map(H), j = P.get(A) ?? !1;
      return P.set(A, !j), n("toggleGroup", { itemId: A, expanded: !j }), P;
    });
  }, [n]), p = Ee((A) => {
    A !== i && n("selectItem", { itemId: A });
  }, [n, i]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), w = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [v, _] = ct(null), C = Ee((A) => {
    _(A);
  }, []), k = Ee(() => {
    _(null);
  }, []);
  Lt(() => {
    r || _(null);
  }, [r]);
  const [L, E] = ct(() => {
    const A = xt(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), y = ut(/* @__PURE__ */ new Map()), b = Ee((A) => (H) => {
    H ? y.current.set(A, H) : y.current.delete(A);
  }, []), S = Ee((A) => {
    E(A);
  }, []), x = ut(0), T = Ee((A) => {
    E(A), x.current++;
  }, []);
  Lt(() => {
    const A = y.current.get(L);
    A && document.activeElement !== A && A.focus();
  }, [L, x.current]);
  const V = Ee((A) => {
    if (A.key === "Escape" && v !== null) {
      A.preventDefault(), k();
      return;
    }
    const H = xt(o, r, c);
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
        A.preventDefault(), j.type === "nav" ? p(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (r ? v === j.id ? k() : C(j.id) : m(j.id));
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
    L,
    v,
    T,
    p,
    h,
    m,
    C,
    k
  ]), F = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: F }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: w, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, o.map((A) => /* @__PURE__ */ e.createElement(
    mn,
    {
      key: A.id,
      item: A,
      activeItemId: i,
      collapsed: r,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: L,
      setItemRef: b,
      onItemFocus: S,
      groupStates: c,
      flyoutGroupId: v,
      onOpenFlyout: C,
      onCloseFlyout: k
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, Gl = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", i = t.wrap === !0, s = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    i ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((c, d) => /* @__PURE__ */ e.createElement(G, { key: d, control: c })));
}, Xl = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, ql = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", i = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, i.map((u, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: u })));
}, Zl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", i = t.headerActions ?? [], s = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(G, { control: s })));
}, Ql = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, i = t.children ?? [], s = t.actions ?? [], u = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: Jl } = e, ea = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = Jl((i) => {
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
}, { useCallback: ta } = e, na = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = t.activeItemId, i = ta((s) => {
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
}, { useCallback: Yt, useRef: la } = e, aa = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ra = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, i = t.child, s = la(null), u = Yt(() => {
    n("close");
  }, [n]), r = Yt((c) => {
    o && c.target === c.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(Bt, null, /* @__PURE__ */ e.createElement(aa, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(G, { control: i })
  )) : null;
}, { useEffect: oa, useRef: sa } = e, ca = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = sa(n.length);
  return oa(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: nt, useRef: Fe, useState: lt } = e, ia = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ua = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, da = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], ma = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ua), o = t.title ?? "", i = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = lt(null), [w, v] = lt(null), [_, C] = lt(null), k = Fe(null), [L, E] = lt(!1), y = Fe(null), b = Fe(null), S = Fe(null), x = Fe(null), T = Fe(null), V = nt(() => {
    n("close");
  }, [n]);
  Ot(!0, x, "field");
  const F = nt((B, M) => {
    M.preventDefault();
    const D = x.current;
    if (!D) return;
    const Y = D.getBoundingClientRect(), f = !k.current, I = k.current ?? { x: Y.left, y: Y.top };
    f && (k.current = I, C(I)), T.current = {
      dir: B,
      startX: M.clientX,
      startY: M.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...I },
      symmetric: f
    };
    const K = (Z) => {
      const $ = T.current;
      if (!$) return;
      const te = Z.clientX - $.startX, ce = Z.clientY - $.startY;
      let ae = $.startW, ge = $.startH, ve = 0, we = 0;
      $.symmetric ? ($.dir.includes("e") && (ae = $.startW + 2 * te), $.dir.includes("w") && (ae = $.startW - 2 * te), $.dir.includes("s") && (ge = $.startH + 2 * ce), $.dir.includes("n") && (ge = $.startH - 2 * ce)) : ($.dir.includes("e") && (ae = $.startW + te), $.dir.includes("w") && (ae = $.startW - te, ve = te), $.dir.includes("s") && (ge = $.startH + ce), $.dir.includes("n") && (ge = $.startH - ce, we = ce));
      const Te = Math.max(200, ae), Re = Math.max(100, ge);
      $.symmetric ? (ve = ($.startW - Te) / 2, we = ($.startH - Re) / 2) : ($.dir.includes("w") && Te === 200 && (ve = $.startW - 200), $.dir.includes("n") && Re === 100 && (we = $.startH - 100)), b.current = Te, S.current = Re, g(Te), v(Re);
      const Oe = {
        x: $.startPos.x + ve,
        y: $.startPos.y + we
      };
      k.current = Oe, C(Oe);
    }, W = () => {
      document.removeEventListener("mousemove", K), document.removeEventListener("mouseup", W);
      const Z = b.current, $ = S.current;
      (Z != null || $ != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...$ != null ? { height: Math.round($) } : {}
      }), T.current = null;
    };
    document.addEventListener("mousemove", K), document.addEventListener("mouseup", W);
  }, [n]), A = nt((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const M = x.current;
    if (!M) return;
    const D = M.getBoundingClientRect(), Y = k.current ?? { x: D.left, y: D.top }, f = B.clientX - Y.x, I = B.clientY - Y.y, K = (Z) => {
      const $ = window.innerWidth, te = window.innerHeight;
      let ce = Z.clientX - f, ae = Z.clientY - I;
      const ge = M.offsetWidth, ve = M.offsetHeight;
      ce + ge > $ && (ce = $ - ge), ae + ve > te && (ae = te - ve), ce < 0 && (ce = 0), ae < 0 && (ae = 0);
      const we = { x: ce, y: ae };
      k.current = we, C(we);
    }, W = () => {
      document.removeEventListener("mousemove", K), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", K), document.addEventListener("mouseup", W);
  }, []), H = nt(() => {
    var B, M;
    if (L) {
      const D = y.current;
      D && (C(D.x !== -1 ? { x: D.x, y: D.y } : null), g(D.w), v(D.h)), E(!1);
    } else {
      const D = x.current, Y = D == null ? void 0 : D.getBoundingClientRect();
      y.current = {
        x: ((B = k.current) == null ? void 0 : B.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((M = k.current) == null ? void 0 : M.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: h ?? (Y == null ? void 0 : Y.width) ?? null,
        h: w ?? null
      }, E(!0), C({ x: 0, y: 0 }), g(null), v(null);
    }
  }, [L, h, w]), P = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...w != null ? { height: w + "px" } : s != null ? { height: s } : {},
    ...u != null && w == null ? { minHeight: u } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Bt, { modal: !0 }, /* @__PURE__ */ e.createElement(ia, { onClose: V }), /* @__PURE__ */ e.createElement(
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
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(G, { control: c })),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((B, M) => /* @__PURE__ */ e.createElement(G, { key: M, control: B }))),
    r && !L && da.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (M) => F(B, M)
      }
    ))
  ));
}, { useCallback: pa } = e, fa = {
  "js.drawer.close": "Close"
}, ha = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(fa), o = t.open === !0, i = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, r = t.child, c = pa(() => {
    n("close");
  }, [n]);
  Be(o, { ESCAPE: c });
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(G, { control: r })));
}, { useCallback: at, useRef: ba } = e, ga = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ba(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", u = at((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = at(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = at((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = at((m) => {
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
}, { useCallback: Ea, useEffect: Gt, useRef: va, useState: Xt } = e, _a = 250, Ca = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.message ?? "", o = t.content ?? "", i = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [c, d] = Xt(!1), [m, p] = Xt(!1), h = va(!1);
  Gt(() => {
    h.current = !1;
  }, [r]);
  const g = Ea(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Gt(() => {
    if (!u || s === 0 || m) return;
    const w = setTimeout(g, h.current ? _a : s);
    return () => clearTimeout(w);
  }, [u, s, m, g]), !u && !c ? null : /* @__PURE__ */ e.createElement(
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
    o ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: o } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: ya, useEffect: qt, useMemo: wa, useRef: ka, useState: Na } = e, Sa = 1e3;
function Da(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), i = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const Ta = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", i = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = wa(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [p, h] = Na(0), g = a && s != null;
  qt(() => {
    if (!g) return;
    const L = setInterval(() => h((E) => E + 1), Sa);
    return () => clearInterval(L);
  }, [g, s]);
  const w = ka(null);
  qt(() => {
    !g || d == null || s == null || w.current !== s && (Date.now() + m < s + d || (w.current = s, n("deadlinePassed", {})));
  }, [p, g, s, d, m, n]);
  const v = ya(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const _ = s != null ? s - (Date.now() + m) : null;
  if (r != null && _ != null && _ > r) return null;
  const C = _ != null ? Da(_) : null, k = c != null;
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
      onClick: k ? v : void 0,
      onKeyDown: k ? (L) => {
        (L.key === "Enter" || L.key === " ") && (L.preventDefault(), v());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    C !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, C)
  );
}, { useCallback: vt, useEffect: Zt, useRef: Ra, useState: Qt } = e, La = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.anchorId, i = t.anchorX, s = t.anchorY, u = t.items ?? [], r = Ra(null), [c, d] = Qt({ top: 0, left: 0 }), [m, p] = Qt(0), h = u.filter((_) => _.type === "item" && !_.disabled);
  Zt(() => {
    var b, S;
    if (!a) return;
    const _ = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, C = ((S = r.current) == null ? void 0 : S.offsetWidth) ?? 200;
    if (i != null && s != null) {
      let x = s, T = i;
      x + _ > window.innerHeight && (x = Math.max(0, window.innerHeight - _)), T + C > window.innerWidth && (T = Math.max(0, window.innerWidth - C)), d({ top: x, left: T }), p(0);
      return;
    }
    if (!o) return;
    const k = document.getElementById(o);
    if (!k) return;
    const L = k.getBoundingClientRect();
    let E = L.bottom + 4, y = L.left;
    E + _ > window.innerHeight && (E = L.top - _ - 4), y + C > window.innerWidth && (y = L.right - C), d({ top: E, left: y }), p(0);
  }, [a, o, i, s]);
  const g = vt(() => {
    n("close");
  }, [n]), w = vt((_) => {
    n("selectItem", { itemId: _ });
  }, [n]);
  Zt(() => {
    if (!a) return;
    const _ = (C) => {
      r.current && !r.current.contains(C.target) && g();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [a, g]);
  const v = vt((_) => {
    if (_.key === "Escape") {
      _.preventDefault(), g();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), p((C) => (C + 1) % h.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), p((C) => (C - 1 + h.length) % h.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const C = h[m];
      C && w(C.id);
    }
  }, [g, w, h, m]);
  return Ot(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: v
    },
    u.map((_, C) => {
      if (_.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const L = h.indexOf(_) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: _.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (_.disabled ? " tlMenu__item--disabled" : "") + (_.cssClasses ? " " + _.cssClasses : ""),
          role: "menuitem",
          disabled: _.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => w(_.id)
        },
        _.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: _.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, _.label)
      );
    })
  ) : null;
}, xa = 768, Ma = ({ controlId: l }) => {
  const t = X(), n = ne();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${xa}px)`), c = (m) => {
      n("reportDisplayClass", { displayClass: m ? "COMPACT" : "REGULAR" });
    };
    c(r.matches);
    const d = (m) => c(m.matches);
    return r.addEventListener("change", d), () => r.removeEventListener("change", d);
  }, [n]);
  const a = t.header, o = t.notices, i = t.content, s = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: a })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(G, { control: i })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: s })), /* @__PURE__ */ e.createElement(G, { control: u }));
}, Ia = ({ controlId: l }) => {
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
}, Pa = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), ja = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, Jt = 50, Aa = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function _t(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, Aa));
}
const Mt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Ba = Mt + ", button:not([disabled]), a[href]";
function pn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Ct(l, t, n = {}) {
  const a = pn(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), s = i == null ? void 0 : i.querySelector(Mt);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const i of o) {
    const s = i.querySelector(Mt);
    if (s) return s;
  }
  return null;
}
const Oa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ja), o = e.useRef(null);
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
  const i = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, w = e.useMemo(
    () => i.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [i]
  ), v = c === "multi", _ = 40, C = 20, k = e.useRef(null), L = e.useRef(null), E = e.useRef(null), y = e.useRef(null), b = e.useRef(null), [S, x] = e.useState({}), T = e.useRef(null), V = e.useRef(!1), F = e.useRef(null), [A, H] = e.useState(null), [P, j] = e.useState(null), [B, M] = e.useState(null), [D, Y] = e.useState(0);
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
  }, [i]);
  const f = e.useCallback((N) => S[N.name] ?? N.width, [S]), I = e.useMemo(() => {
    const N = [];
    let R = v && p > 0 ? _ : 0;
    for (let z = 0; z < p && z < i.length; z++)
      N.push(R), R += f(i[z]);
    return N;
  }, [i, p, v, _, f]), K = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let N = v ? _ : 0;
    for (let R = 0; R < p && R < i.length; R++)
      N += f(i[R]);
    return N;
  }, [i, p, v, _, f]), W = s * r, Z = e.useRef(null), $ = e.useCallback((N, R, z) => {
    z.preventDefault(), z.stopPropagation(), T.current = { column: N, startX: z.clientX, startWidth: R };
    let Q = z.clientX, ee = 0;
    const re = () => {
      const se = T.current;
      if (!se) return;
      const de = Math.max(Jt, se.startWidth + (Q - se.startX) + ee);
      x((Ce) => ({ ...Ce, [se.column]: de }));
    }, oe = () => {
      const se = E.current, de = k.current;
      if (!se || !T.current) return;
      const Ce = se.getBoundingClientRect(), Le = 40, Ht = 8, Tn = se.scrollLeft;
      Q > Ce.right - Le ? se.scrollLeft += Ht : Q < Ce.left + Le && (se.scrollLeft = Math.max(0, se.scrollLeft - Ht));
      const Wt = se.scrollLeft - Tn;
      Wt !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Wt, re()), Z.current = requestAnimationFrame(oe);
    };
    Z.current = requestAnimationFrame(oe);
    const he = (se) => {
      Q = se.clientX, re();
    }, pe = (se) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", pe), Z.current !== null && (cancelAnimationFrame(Z.current), Z.current = null);
      const de = T.current;
      if (de) {
        const Ce = Math.max(Jt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ce }), T.current = null, V.current = !0, requestAnimationFrame(() => {
          V.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    k.current && E.current && (k.current.scrollLeft = E.current.scrollLeft), y.current !== null && clearTimeout(y.current), y.current = window.setTimeout(() => {
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
  }, [n]), ae = e.useCallback((N, R) => {
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
    let z = i.findIndex((ee) => ee.name === A.column);
    if (z < 0) {
      F.current = null, H(null);
      return;
    }
    const Q = i.findIndex((ee) => ee.name === R);
    A.side === "right" && z++, Q < z && z--, n("columnReorder", { column: R, targetIndex: z }), F.current = null, H(null);
  }, [i, A, n]), we = e.useCallback(() => {
    F.current = null, H(null);
  }, []), Te = e.useCallback((N, R) => {
    var ee, re, oe, he;
    const z = window.getSelection();
    if (z && !z.isCollapsed && R.currentTarget.contains(z.anchorNode))
      return;
    if (!_t(R) && ((ee = E.current) == null || ee.focus({ preventScroll: !0 }), !R.ctrlKey && !R.metaKey && !R.shiftKey)) {
      const pe = (he = (oe = (re = R.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      b.current = { index: N, col: pe ?? void 0 };
    }
    const Q = u.find((pe) => pe.index === N);
    _t(R) && (Q != null && Q.selected) && !R.ctrlKey && !R.metaKey && !R.shiftKey || n("select", {
      rowIndex: N,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    });
  }, [n, u]), Re = e.useCallback((N, R, z) => {
    n("moveSelection", { direction: N, extend: R, move: z });
  }, [n]), Oe = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), ft = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), et = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const N = E.current;
    if (!N)
      return;
    const R = m * r, z = R + r;
    R < N.scrollTop ? N.scrollTop = R : z > N.scrollTop + N.clientHeight && (N.scrollTop = z - N.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const N = b.current, R = E.current;
    if (!N || !R)
      return;
    const z = u.find((re) => re.index === N.index);
    if (!z || !Ct(R, z.id))
      return;
    b.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !R.contains(Q))
      return;
    const ee = Ct(R, z.id, { col: N.col, last: N.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const O = e.useCallback((N) => {
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
    const oe = pn(R, ee).flatMap((Le) => Array.from(Le.querySelectorAll(Ba))), he = oe.indexOf(z);
    if (he < 0)
      return;
    const pe = !N.shiftKey;
    if (!(pe ? he === oe.length - 1 : he === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const Ce = u.find((Le) => Le.index === de);
    Ce && Ct(R, Ce.id) || (N.preventDefault(), b.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), q = e.useCallback((N, R) => {
    R.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), le = e.useCallback(() => {
    const N = d === s && s > 0;
    n("selectAll", { selected: !N });
  }, [n, d, s]), ie = e.useCallback((N, R, z) => {
    z.stopPropagation(), n("expand", { rowIndex: N, expanded: R });
  }, [n]), Ye = e.useCallback((N, R) => {
    R.preventDefault(), j({ x: R.clientX, y: R.clientY, colIdx: N });
  }, []), Cn = e.useCallback(() => {
    P && (n("setFrozenColumnCount", { count: P.colIdx + 1 }), j(null));
  }, [P, n]), yn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), j(null);
  }, [n]), wn = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const R = L.current, z = k.current;
    if (!R || !z)
      return;
    const Q = R.clientWidth, ee = [{ x: 0, count: 0 }];
    z.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - R.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: K, count: p };
    const oe = (pe) => {
      const se = pe.clientX - R.getBoundingClientRect().left;
      re = ee.reduce(
        (de, Ce) => Math.abs(Ce.x - se) < Math.abs(de.x - se) ? Ce : de,
        ee[0]
      ), M(re);
    }, he = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", he), M(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", he);
  }, [K, p, n]);
  e.useEffect(() => {
    if (!P) return;
    const N = () => j(null);
    return document.addEventListener("mousedown", N), () => document.removeEventListener("mousedown", N);
  }, [P]), Be(!!P, { ESCAPE: () => j(null) });
  const kn = e.useCallback((N, R) => {
    R.stopPropagation(), R.preventDefault(), n("openFilter", { column: N });
  }, [n]), Nn = e.useCallback((N) => {
    N.stopPropagation(), N.preventDefault(), n("openColumnSelect", {});
  }, [n]), ht = i.reduce((N, R) => N + f(R), 0) + (v ? _ : 0), bt = g ? 32 : 0, Sn = d === s && s > 0, $t = d > 0 && d < s, Dn = e.useCallback((N) => {
    N && (N.indeterminate = $t);
  }, [$t]);
  return /* @__PURE__ */ e.createElement(Bt, { active: et }, /* @__PURE__ */ e.createElement(
    Pa,
    {
      isMulti: v,
      cursorIndex: m,
      onMove: Re,
      onToggle: Oe,
      onSelectAll: ft
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
        style: { width: ht, paddingRight: bt + D }
      },
      v && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: _,
            minWidth: _,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (N) => {
            F.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== F.current && H({ column: i[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Dn,
            className: "tlTableView__checkbox",
            checked: Sn,
            onChange: le
          }
        )
      ),
      i.map((N, R) => {
        const z = f(N);
        i.length - 1;
        let Q = "tlTableView__headerCell";
        N.sortable && (Q += " tlTableView__headerCell--sortable"), A && A.column === N.name && (Q += " tlTableView__headerCell--dragOver-" + A.side);
        const ee = R < p, re = R === p - 1;
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
              ...ee ? { left: I[R], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: N.sortable ? (oe) => ce(N.name, N.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Ye(R, oe),
            onDragStart: (oe) => ae(N.name, oe),
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
              onClick: (oe) => kn(N.name, oe)
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
            if (F.current && i.length > 0) {
              const R = i[i.length - 1];
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
        onMouseDown: wn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Nn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: E,
        className: "tlTableView__body",
        onScroll: te,
        onKeyDown: O,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: ht, paddingRight: bt } }, u.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : "") + (N.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: N.index * r,
            height: r,
            width: ht,
            paddingRight: bt,
            ...N.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (R) => {
            (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && !_t(R) && R.preventDefault();
          },
          onClick: (R) => Te(N.index, R)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: _,
              minWidth: _,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
              onClick: (R) => q(N.index, R),
              tabIndex: -1
            }
          )
        ),
        i.map((R, z) => {
          const Q = f(R), ee = z === i.length - 1, re = z < p, oe = z === p - 1;
          let he = "tlTableView__cell";
          re && (he += " tlTableView__cell--frozen"), oe && (he += " tlTableView__cell--frozenLast");
          const pe = h && z === 0, se = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: R.name,
              className: he,
              "data-row": N.id,
              "data-col": R.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: I[z], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * C } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => ie(N.index, !N.expanded, de)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), N.cells[R.name] && /* @__PURE__ */ e.createElement(G, { control: N.cells[R.name] })) : N.cells[R.name] && /* @__PURE__ */ e.createElement(G, { control: N.cells[R.name] })
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
      P.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Cn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: yn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, Fa = {
  "js.table.columnSearch": "Find column"
}, $a = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Fa), o = t.entries ?? [], i = o.filter((E) => E.visible).length, [s, u] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((E) => {
    m.current = E, h(E);
  }, []), w = e.useCallback((E, y) => {
    n("columnVisible", { column: E, visible: y });
  }, [n]), v = e.useCallback((E, y) => {
    d.current = E, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", E);
  }, []), _ = e.useCallback((E, y) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const b = y.currentTarget.getBoundingClientRect(), S = y.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: S });
  }, [g]), C = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), k = e.useCallback((E) => {
    E.preventDefault();
    const y = d.current, b = m.current;
    if (d.current = null, g(null), !y || !b)
      return;
    const S = o.findIndex((V) => V.name === b.name), x = o.findIndex((V) => V.name === y);
    if (S < 0 || x < 0)
      return;
    let T = b.side === "top" ? S : S + 1;
    x < T && T--, T !== x && n("columnReorder", { column: y, targetIndex: T });
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, c.map((E) => {
    const y = E.visible && i <= 1;
    let b = "tlColumnSelect__row";
    return p && p.name === E.name && (b += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (S) => v(E.name, S),
        onDragOver: (S) => _(E.name, S),
        onDrop: k,
        onDragEnd: C
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: E.visible,
          disabled: y,
          onChange: (S) => w(E.name, S.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: It, useRef: Qe, useCallback: it, useMemo: je, useEffect: en } = e, Ha = {
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
}, _e = 44, dt = 15, ye = 6e4, Wa = 36e5, Ie = 864e5, Ua = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ue(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function za(l) {
  return Se(l);
}
function Je(l, t) {
  return Se(l) === Se(t);
}
function Me(l) {
  return (l - Se(l)) / ye;
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
  return "tlCalEvent--c" + Math.abs(t) % Ua;
}
function pt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function Va(l) {
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
function Ka(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Ae(l, n, t.start) + "–" + Ae(l, n, t.end);
}
const Ya = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Ga = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Ya.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Xa(l) {
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
      topMin: Me(s.start),
      botMin: Me(s.start) + Math.max(15, (s.end - s.start) / ye),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && i(), n;
}
const yt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Pt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const i = Qe(!1), s = (u) => {
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
}, fn = (l) => {
  const [t, n] = It(null), a = Qe(null);
  a.current = t;
  const o = it((u) => n(u), []), i = it(() => n(null), []), s = it(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: i };
}, qa = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: i, dayStartHour: s, dayEndHour: u, now: r, send: c, editable: d, i18n: m } = l, p = je(() => {
    const P = n === "DAY" ? 1 : 7, j = [];
    for (let B = 0; B < P; B++) {
      const M = Ue(t, B);
      n === "WORK_WEEK" && i.includes(new Date(M).getDay()) || j.push(M);
    }
    return j;
  }, [t, n, i]), h = fn(c), g = Qe(null), w = Qe(null), [v, _] = It(null), C = Qe(null);
  C.current = v;
  const [k, L] = It(Date.now());
  en(() => {
    const P = window.setInterval(() => L(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const E = it(
    (P, j) => {
      const B = g.current;
      if (!B)
        return { dayIndex: 0, min: 0 };
      const M = B.getBoundingClientRect(), D = M.width / p.length, Y = Xe(Math.floor((P - M.left) / D), 0, p.length - 1), f = j - M.top + B.scrollTop, I = Xe(f / _e * 60, 0, 1440);
      return { dayIndex: Y, min: I };
    },
    [p.length]
  );
  en(() => {
    if (!v)
      return;
    const P = (M) => {
      const D = C.current;
      if (!D)
        return;
      const { dayIndex: Y, min: f } = E(M.clientX, M.clientY);
      D.mode === "move" ? _({ ...D, dayStart: p[Y], startMin: Xe(Ge(f - D.grabMin), 0, 1440 - D.dur) }) : D.mode === "resize" ? _({ ...D, endMin: Xe(Ge(f), D.startMin + dt, 1440) }) : _({ ...D, toMin: Xe(Ge(f), 0, 1440) });
    }, j = () => {
      const M = C.current;
      if (_(null), !!M)
        if (M.mode === "move") {
          const D = M.dayStart + M.startMin * ye;
          D !== M.origStartMs && c("moveEvent", { eventId: M.id, start: D, end: D + M.dur * ye });
        } else if (M.mode === "resize") {
          const D = M.dayStart + M.endMin * ye;
          D !== M.origEndMs && c("resizeEvent", { eventId: M.id, end: D });
        } else {
          const D = Math.min(M.fromMin, M.toMin), Y = Math.max(M.fromMin, M.toMin);
          Y - D >= dt && h.open({ start: M.dayStart + D * ye, end: M.dayStart + Y * ye, allDay: !1 });
        }
    }, B = () => _(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", B), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", B);
    };
  }, [v, p, E, c, h.open]);
  const y = (P, j, B) => {
    if (!d || !j.movable)
      return;
    P.stopPropagation(), yt(P), h.discard();
    const { min: M } = E(P.clientX, P.clientY), D = (j.end - j.start) / ye;
    _({
      mode: "move",
      id: j.id,
      grabMin: M - Me(j.start),
      dur: D,
      dayStart: B,
      startMin: Me(j.start),
      origStartMs: j.start
    });
  }, b = (P, j, B) => {
    !d || !j.resizable || (P.stopPropagation(), yt(P), h.discard(), _({
      mode: "resize",
      id: j.id,
      dayStart: B,
      startMin: Me(j.start),
      endMin: Me(j.end),
      origEndMs: j.end
    }));
  }, S = (P, j) => {
    if (!d || P.button !== 0)
      return;
    yt(P), h.discard();
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
  }, [a, v]), V = je(() => p.map(
    (P) => Xa(
      T.filter((j) => !j.allDay && j.start < P + Ie && j.end > P)
    )
  ), [p, T]), F = je(() => p.map((P) => T.filter((j) => j.allDay && j.start < P + Ie && j.end > P)), [p, T]), A = s * _e, H = u * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((P) => {
    const j = i.includes(new Date(P).getDay()), B = Je(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (B ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Ae(o, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), p.map((P, j) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: P,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: P, end: P + Ie, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === P && /* @__PURE__ */ e.createElement(
      Pt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
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
        onClick: (M) => {
          M.stopPropagation(), c("selectEvent", { eventId: B.id });
        }
      },
      B.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: w }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, x.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * _e } }, P === 0 ? "" : Ae(o, { hour: "numeric" }, Se(t) + P * Wa)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((P, j) => {
    const B = i.includes(new Date(P).getDay()), M = v && ("dayStart" in v && v.dayStart === P) ? v : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (B ? " tlCalCol--nonworking" : ""),
        onPointerDown: (D) => S(D, P)
      },
      x.map((D) => /* @__PURE__ */ e.createElement("div", { key: D, className: "tlCalHourLine", style: { top: D * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: H - A } }),
      Je(P, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Me(Date.now()) / 60 * _e } }),
      V[j].map((D) => {
        const Y = v !== null && "id" in v && v.id === D.ev.id, f = D.topMin / 60 * _e, I = (D.botMin - D.topMin) / 60 * _e, K = 100 / D.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.ev.id,
            className: "tlCalEvent " + mt(D.ev.category) + (D.ev.selected ? " tlCalEvent--selected" : "") + (Y ? " tlCalEvent--dragging" : ""),
            style: pt(D.ev, {
              top: f,
              height: I,
              left: `${D.col * K}%`,
              width: `calc(${K}% - 2px)`
            }),
            title: D.ev.tooltip,
            onPointerDown: (W) => y(W, D.ev, P),
            onClick: (W) => {
              W.stopPropagation(), c("selectEvent", { eventId: D.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, Ka(o, D.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, D.ev.title),
          d && D.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (W) => b(W, D.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Pt,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Me(h.pending.start) / 60 * _e,
            height: (h.pending.end - h.pending.start) / ye / 60 * _e
          },
          onCommit: h.commit,
          onDiscard: h.discard
        }
      ),
      M && M.mode === "create" && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlCalEvent tlCalEvent--preview",
          style: {
            top: Math.min(M.fromMin, M.toMin) / 60 * _e,
            height: Math.abs(M.toMin - M.fromMin) / 60 * _e
          }
        }
      )
    );
  })))));
}, Za = 3, Qa = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: i, send: s, editable: u, now: r, i18n: c } = l, d = fn(s), m = je(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const w = [];
      for (let v = 0; v < 7; v++)
        w.push(Ue(t, g * 7 + v));
      h.push(w);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const w = h.dataTransfer.getData("text/plain"), v = a.find((C) => C.id === w);
    if (!v || !u || !v.movable)
      return;
    const _ = g - Se(v.start);
    s("moveEvent", { eventId: w, start: v.start + _, end: v.end + _ });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Ae(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const w = h[0], v = Ue(w, 7), _ = a.filter((k) => (k.allDay || k.end - k.start >= Ie) && k.start < v && k.end > w).sort((k, L) => k.start - L.start).slice(0, 3), C = _.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const L = new Date(k).getMonth() === new Date(n).getMonth(), E = i.includes(new Date(k).getDay()), y = Je(k, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (L ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => p(b, k),
          onClick: () => u && d.open({ start: k, end: k + Ie, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (y ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: k, granularity: "DAY" });
            }
          },
          new Date(k).getDate()
        ),
        d.pending && d.pending.start === k && /* @__PURE__ */ e.createElement(
          Pt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, _.map((k, L) => {
      const E = Math.max(0, Math.floor((Se(Math.max(k.start, w)) - w) / Ie)), y = Math.min(7, Math.ceil((k.end - w) / Ie));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + mt(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: pt(k, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, y) + 1}`,
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
      const E = a.filter((S) => !S.allDay && S.end - S.start < Ie && Je(S.start, k)).sort((S, x) => S.start - x.start), y = E.slice(0, Za), b = E.length - y.length;
      return y.map((S, x) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.id,
          className: "tlCalChip " + mt(S.category) + (S.selected ? " tlCalEvent--selected" : ""),
          style: pt(S, { gridColumn: L + 1, gridRow: C + 1 + x }),
          draggable: u && S.movable,
          onDragStart: (T) => T.dataTransfer.setData("text/plain", S.id),
          title: S.tooltip,
          onClick: (T) => {
            T.stopPropagation(), s("selectEvent", { eventId: S.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Ae(o, { hour: "numeric", minute: "2-digit" }, S.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, S.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: L + 1, gridRow: C + 1 + y.length },
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
}, Ja = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: i, send: s, now: u } = l, r = je(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Se(h.start);
      const w = h.end;
      for (; g < w; )
        p.add(g), g = Ue(g, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = je(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const w = new Date(p);
      return w.setDate(p.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(w);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = Se(Ue(p, -((h.getDay() - o + 7) % 7))), w = Array.from({ length: 42 }, (v, _) => Ue(g, _));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Ae(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((v, _) => /* @__PURE__ */ e.createElement("div", { key: "h" + _, className: "tlCalMiniWd" }, v)), w.map((v) => {
      const _ = new Date(v).getMonth() === h.getMonth(), C = i.includes(new Date(v).getDay()), k = Je(v, u), L = r.has(za(v));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v,
          className: "tlCalMiniDay" + (_ ? "" : " tlCalMiniDay--other") + (C ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (L ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: v, granularity: "DAY" })
        },
        new Date(v).getDate()
      );
    })));
  }));
}, er = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ha), o = t.granularity ?? "WEEK", i = t.rangeStart ?? Date.now(), s = t.anchor ?? i, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: Va(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Ga, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(Qa, { ctx: r, rangeStart: i, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(Ja, { ctx: r, rangeStart: i }) : /* @__PURE__ */ e.createElement(qa, { ctx: r, rangeStart: i, granularity: o })));
}, tr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, hn = e.createContext(tr), { useMemo: nr, useRef: lr, useState: ar, useEffect: rr } = e, or = 320, sr = "TLTableView", cr = "TLPanel", ir = ({ controlId: l }) => {
  var v;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, i = t.children ?? [], s = t.noModelMessage, u = lr(null), [r, c] = ar(
    a === "top" ? "top" : "side"
  );
  rr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const _ = u.current;
    if (!_) return;
    const C = new ResizeObserver((k) => {
      for (const L of k) {
        const y = L.contentRect.width / n;
        c(y < or ? "top" : "side");
      }
    });
    return C.observe(_), () => C.disconnect();
  }, [a, n]);
  const d = nr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = i.length === 1 ? i[0] : void 0, g = !!h && (h.module === sr || h.module === cr && ((v = h.state) == null ? void 0 : v.bare) === !0), w = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(hn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: w, style: p, ref: u }, i.map((_, C) => /* @__PURE__ */ e.createElement(G, { key: C, control: _ }))));
}, { useCallback: ur } = e, dr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, mr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(dr), o = t.headerControl ?? null, i = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || i.length > 0 || s, p = ur(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((g, w) => /* @__PURE__ */ e.createElement(G, { key: w, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, w) => /* @__PURE__ */ e.createElement(G, { key: w, control: g }))));
}, { useContext: pr, useState: fr, useCallback: hr } = e, br = ({ controlId: l }) => {
  const t = X(), n = pr(hn), a = t.label ?? "", o = t.required === !0, i = t.error, s = t.errorIcon, u = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, w = t.field, v = n.readOnly, [_, C] = fr(!1), k = hr(() => C((S) => !S), []), L = m === "hidden", E = i != null, y = u != null && u.length > 0, b = [
    "tlFormField",
    `tlFormField--${m}`,
    v ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && y ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !v && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !v && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: w })), !v && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Rt, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !v && !E && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((S, x) => /* @__PURE__ */ e.createElement("div", { key: x, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Rt, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, S)))), !v && c && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, gr = "goto", Er = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.iconCss, o = t.iconSrc, i = t.label, s = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), m = e.useCallback((g) => {
    g.preventDefault(), n(gr, {});
  }, [n]), p = ["tlResourceCell", s].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
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
}, vr = 20, _r = () => {
  var y;
  const l = X(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((y = n.find((b) => b.selected)) == null ? void 0 : y.id) ?? null;
  e.useEffect(() => {
    var S;
    if (m == null)
      return;
    const b = (S = d.current) == null ? void 0 : S.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [m]);
  const p = e.useCallback((b, S) => {
    t(S ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, S) => {
    var T;
    const x = window.getSelection();
    x && !x.isCollapsed && S.currentTarget.contains(x.anchorNode) || ((T = d.current) == null || T.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: S.ctrlKey || S.metaKey,
      shiftKey: S.shiftKey
    }));
  }, [t]), g = e.useCallback((b, S) => {
    S.preventDefault(), t("contextMenu", { nodeId: b, x: S.clientX, y: S.clientY });
  }, [t]), w = e.useRef(null), v = e.useCallback((b, S) => {
    const x = S.getBoundingClientRect(), T = b.clientY - x.top, V = x.height / 3;
    return T < V ? "above" : T > V * 2 ? "below" : "within";
  }, []), _ = e.useCallback((b, S) => {
    S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", b);
  }, []), C = e.useCallback((b, S) => {
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const x = v(S, S.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: x }), w.current = null;
    }, 50);
  }, [t, v]), k = e.useCallback((b, S) => {
    S.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const x = v(S, S.currentTarget);
    t("drop", { nodeId: b, position: x });
  }, [t, v]), L = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
  }, [t]), E = e.useCallback((b) => {
    if (n.length === 0) return;
    let S = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), S = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), S = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const x = n[r];
          if (x.expandable && !x.expanded) {
            t("expand", { nodeId: x.id });
            return;
          } else x.expanded && (S = r + 1);
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
                S = V;
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
        b.preventDefault(), S = 0;
        break;
      case "End":
        b.preventDefault(), S = n.length - 1;
        break;
      default:
        return;
    }
    S !== r && c(S);
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
    n.map((b, S) => /* @__PURE__ */ e.createElement(
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
          S === r ? "tlTreeView__node--focused" : "",
          s === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * vr },
        draggable: o,
        onMouseDown: (x) => {
          (x.shiftKey || x.ctrlKey || x.metaKey || x.detail > 1) && x.preventDefault();
        },
        onClick: (x) => h(b.id, x),
        onContextMenu: (x) => g(b.id, x),
        onDragStart: (x) => _(b.id, x),
        onDragOver: i ? (x) => C(b.id, x) : void 0,
        onDrop: i ? (x) => k(b.id, x) : void 0,
        onDragEnd: L
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (x) => {
            x.stopPropagation(), p(b.id, b.expanded);
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
var wt = { exports: {} }, be = {}, kt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var tn;
function Cr() {
  if (tn) return J;
  tn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
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
  }, w = Object.assign, v = {};
  function _(f, I, K) {
    this.props = f, this.context = I, this.refs = v, this.updater = K || g;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(f, I) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, I, "setState");
  }, _.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function C() {
  }
  C.prototype = _.prototype;
  function k(f, I, K) {
    this.props = f, this.context = I, this.refs = v, this.updater = K || g;
  }
  var L = k.prototype = new C();
  L.constructor = k, w(L, _.prototype), L.isPureReactComponent = !0;
  var E = Array.isArray;
  function y() {
  }
  var b = { H: null, A: null, T: null, S: null }, S = Object.prototype.hasOwnProperty;
  function x(f, I, K) {
    var W = K.ref;
    return {
      $$typeof: l,
      type: f,
      key: I,
      ref: W !== void 0 ? W : null,
      props: K
    };
  }
  function T(f, I) {
    return x(f.type, I, f.props);
  }
  function V(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function F(f) {
    var I = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(K) {
      return I[K];
    });
  }
  var A = /\/+/g;
  function H(f, I) {
    return typeof f == "object" && f !== null && f.key != null ? F("" + f.key) : I.toString(36);
  }
  function P(f) {
    switch (f.status) {
      case "fulfilled":
        return f.value;
      case "rejected":
        throw f.reason;
      default:
        switch (typeof f.status == "string" ? f.then(y, y) : (f.status = "pending", f.then(
          function(I) {
            f.status === "pending" && (f.status = "fulfilled", f.value = I);
          },
          function(I) {
            f.status === "pending" && (f.status = "rejected", f.reason = I);
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
  function j(f, I, K, W, Z) {
    var $ = typeof f;
    ($ === "undefined" || $ === "boolean") && (f = null);
    var te = !1;
    if (f === null) te = !0;
    else
      switch ($) {
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
              return te = f._init, j(
                te(f._payload),
                I,
                K,
                W,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = W === "" ? "." + H(f, 0) : W, E(Z) ? (K = "", te != null && (K = te.replace(A, "$&/") + "/"), j(Z, I, K, "", function(ge) {
        return ge;
      })) : Z != null && (V(Z) && (Z = T(
        Z,
        K + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), I.push(Z)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (E(f))
      for (var ae = 0; ae < f.length; ae++)
        W = f[ae], $ = ce + H(W, ae), te += j(
          W,
          I,
          K,
          $,
          Z
        );
    else if (ae = h(f), typeof ae == "function")
      for (f = ae.call(f), ae = 0; !(W = f.next()).done; )
        W = W.value, $ = ce + H(W, ae++), te += j(
          W,
          I,
          K,
          $,
          Z
        );
    else if ($ === "object") {
      if (typeof f.then == "function")
        return j(
          P(f),
          I,
          K,
          W,
          Z
        );
      throw I = String(f), Error(
        "Objects are not valid as a React child (found: " + (I === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : I) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function B(f, I, K) {
    if (f == null) return f;
    var W = [], Z = 0;
    return j(f, W, "", "", function($) {
      return I.call(K, $, Z++);
    }), W;
  }
  function M(f) {
    if (f._status === -1) {
      var I = f._result;
      I = I(), I.then(
        function(K) {
          (f._status === 0 || f._status === -1) && (f._status = 1, f._result = K);
        },
        function(K) {
          (f._status === 0 || f._status === -1) && (f._status = 2, f._result = K);
        }
      ), f._status === -1 && (f._status = 0, f._result = I);
    }
    if (f._status === 1) return f._result.default;
    throw f._result;
  }
  var D = typeof reportError == "function" ? reportError : function(f) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var I = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof f == "object" && f !== null && typeof f.message == "string" ? String(f.message) : String(f),
        error: f
      });
      if (!window.dispatchEvent(I)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", f);
      return;
    }
    console.error(f);
  }, Y = {
    map: B,
    forEach: function(f, I, K) {
      B(
        f,
        function() {
          I.apply(this, arguments);
        },
        K
      );
    },
    count: function(f) {
      var I = 0;
      return B(f, function() {
        I++;
      }), I;
    },
    toArray: function(f) {
      return B(f, function(I) {
        return I;
      }) || [];
    },
    only: function(f) {
      if (!V(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return J.Activity = m, J.Children = Y, J.Component = _, J.Fragment = n, J.Profiler = o, J.PureComponent = k, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
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
  }, J.cloneElement = function(f, I, K) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var W = w({}, f.props), Z = f.key;
    if (I != null)
      for ($ in I.key !== void 0 && (Z = "" + I.key), I)
        !S.call(I, $) || $ === "key" || $ === "__self" || $ === "__source" || $ === "ref" && I.ref === void 0 || (W[$] = I[$]);
    var $ = arguments.length - 2;
    if ($ === 1) W.children = K;
    else if (1 < $) {
      for (var te = Array($), ce = 0; ce < $; ce++)
        te[ce] = arguments[ce + 2];
      W.children = te;
    }
    return x(f.type, Z, W);
  }, J.createContext = function(f) {
    return f = {
      $$typeof: s,
      _currentValue: f,
      _currentValue2: f,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, f.Provider = f, f.Consumer = {
      $$typeof: i,
      _context: f
    }, f;
  }, J.createElement = function(f, I, K) {
    var W, Z = {}, $ = null;
    if (I != null)
      for (W in I.key !== void 0 && ($ = "" + I.key), I)
        S.call(I, W) && W !== "key" && W !== "__self" && W !== "__source" && (Z[W] = I[W]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = K;
    else if (1 < te) {
      for (var ce = Array(te), ae = 0; ae < te; ae++)
        ce[ae] = arguments[ae + 2];
      Z.children = ce;
    }
    if (f && f.defaultProps)
      for (W in te = f.defaultProps, te)
        Z[W] === void 0 && (Z[W] = te[W]);
    return x(f, $, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(f) {
    return { $$typeof: u, render: f };
  }, J.isValidElement = V, J.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: M
    };
  }, J.memo = function(f, I) {
    return {
      $$typeof: c,
      type: f,
      compare: I === void 0 ? null : I
    };
  }, J.startTransition = function(f) {
    var I = b.T, K = {};
    b.T = K;
    try {
      var W = f(), Z = b.S;
      Z !== null && Z(K, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(y, D);
    } catch ($) {
      D($);
    } finally {
      I !== null && K.types !== null && (I.types = K.types), b.T = I;
    }
  }, J.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, J.use = function(f) {
    return b.H.use(f);
  }, J.useActionState = function(f, I, K) {
    return b.H.useActionState(f, I, K);
  }, J.useCallback = function(f, I) {
    return b.H.useCallback(f, I);
  }, J.useContext = function(f) {
    return b.H.useContext(f);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(f, I) {
    return b.H.useDeferredValue(f, I);
  }, J.useEffect = function(f, I) {
    return b.H.useEffect(f, I);
  }, J.useEffectEvent = function(f) {
    return b.H.useEffectEvent(f);
  }, J.useId = function() {
    return b.H.useId();
  }, J.useImperativeHandle = function(f, I, K) {
    return b.H.useImperativeHandle(f, I, K);
  }, J.useInsertionEffect = function(f, I) {
    return b.H.useInsertionEffect(f, I);
  }, J.useLayoutEffect = function(f, I) {
    return b.H.useLayoutEffect(f, I);
  }, J.useMemo = function(f, I) {
    return b.H.useMemo(f, I);
  }, J.useOptimistic = function(f, I) {
    return b.H.useOptimistic(f, I);
  }, J.useReducer = function(f, I, K) {
    return b.H.useReducer(f, I, K);
  }, J.useRef = function(f) {
    return b.H.useRef(f);
  }, J.useState = function(f) {
    return b.H.useState(f);
  }, J.useSyncExternalStore = function(f, I, K) {
    return b.H.useSyncExternalStore(
      f,
      I,
      K
    );
  }, J.useTransition = function() {
    return b.H.useTransition();
  }, J.version = "19.2.4", J;
}
var nn;
function yr() {
  return nn || (nn = 1, kt.exports = Cr()), kt.exports;
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
var ln;
function wr() {
  if (ln) return be;
  ln = 1;
  var l = yr();
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
  return be.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, be.createPortal = function(r, c) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!c || c.nodeType !== 1 && c.nodeType !== 9 && c.nodeType !== 11)
      throw Error(t(299));
    return i(r, c, null, d);
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
      var d = c.as, m = u(d, c.crossOrigin), p = typeof c.integrity == "string" ? c.integrity : void 0, h = typeof c.fetchPriority == "string" ? c.fetchPriority : void 0;
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
  }, be.preload = function(r, c) {
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
  }, be.preloadModule = function(r, c) {
    if (typeof r == "string")
      if (c) {
        var d = u(c.as, c.crossOrigin);
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
var an;
function kr() {
  if (an) return wt.exports;
  an = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), wt.exports = wr(), wt.exports;
}
var bn = kr();
const { useState: xe, useCallback: fe, useRef: qe, useEffect: $e, useMemo: jt } = e, Nr = "goto", Sr = "option";
function Ft({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Dr({
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
  const d = fe(
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
    /* @__PURE__ */ e.createElement(Ft, { image: l.image }),
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
function Tr({
  option: l,
  onGoto: t
}) {
  const n = fe(
    (o) => {
      o.preventDefault(), t(l.value);
    },
    [t, l.value]
  ), a = /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(Ft, { image: l.image }), /* @__PURE__ */ e.createElement("span", null, l.label));
  return l.link ? /* @__PURE__ */ e.createElement("a", { className: "tlDropdownSelect__readonlyValue tlResourceCell", href: "#", onClick: n }, a) : /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__readonlyValue" }, a);
}
function Rr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: i
}) {
  const s = fe(() => a(l.value), [a, l.value]), u = jt(() => {
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
    /* @__PURE__ */ e.createElement(Ft, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Lr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, i = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = i && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], w = fe(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [v, _] = xe(!1), [C, k] = xe(""), [L, E] = xe(-1), [y, b] = xe(!1), [S, x] = xe({}), [T, V] = xe(null), [F, A] = xe(null), [H, P] = xe(null), j = qe(null), B = qe(null), M = qe(null), D = qe(a);
  D.current = a;
  const Y = qe(-1), f = jt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), I = jt(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (C) {
      const q = C.toLowerCase();
      O = O.filter((le) => le.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, C]);
  $e(() => {
    C && I.length === 1 ? E(0) : E(-1);
  }, [I.length, C]), $e(() => {
    v && c && B.current && B.current.focus();
  }, [v, c, a]), $e(() => {
    var le, ie;
    if (Y.current < 0) return;
    const O = Y.current;
    Y.current = -1;
    const q = (le = j.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), $e(() => {
    if (!v) return;
    const O = (q) => {
      j.current && !j.current.contains(q.target) && M.current && !M.current.contains(q.target) && (_(!1), k(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [v]), $e(() => {
    if (!v || !j.current) return;
    const O = j.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    x({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [v]);
  const K = fe(async () => {
    if (!(u || !r) && (_(!0), k(""), E(-1), b(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [u, r, c, n]), W = fe(() => {
    var O;
    _(!1), k(""), E(-1), (O = j.current) == null || O.focus();
  }, []), Z = fe(
    (O) => {
      let q;
      if (o) {
        const le = d.find((ie) => ie.value === O);
        if (le)
          q = [...D.current, le];
        else
          return;
      } else {
        const le = d.find((ie) => ie.value === O);
        if (le)
          q = [le];
        else
          return;
      }
      D.current = q, n(tt, { value: q.map((le) => le.value) }), o ? (k(""), E(-1)) : W();
    },
    [o, d, n, W]
  ), $ = fe(
    (O) => {
      Y.current = D.current.findIndex((le) => le.value === O);
      const q = D.current.filter((le) => le.value !== O);
      D.current = q, n(tt, { value: q.map((le) => le.value) });
    },
    [n]
  ), te = fe(
    (O) => {
      O.stopPropagation(), n(tt, { value: [] }), W();
    },
    [n, W]
  ), ce = fe((O) => {
    k(O.target.value);
  }, []), ae = fe(
    (O) => {
      n(Nr, { [Sr]: O });
    },
    [n]
  ), ge = fe(
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
            (q) => q < I.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), E(
            (q) => q > 0 ? q - 1 : I.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), L >= 0 && L < I.length && Z(I[L].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          C === "" && o && a.length > 0 && $(a[a.length - 1].value);
          break;
      }
    },
    [
      v,
      K,
      W,
      I,
      L,
      Z,
      C,
      o,
      a,
      $
    ]
  ), ve = fe(
    async (O) => {
      O.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), we = fe(
    (O, q) => {
      V(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), Te = fe(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", T === null || T === O) {
        A(null), P(null);
        return;
      }
      const le = q.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Ye = q.clientX < ie ? "before" : "after";
      A(O), P(Ye);
    },
    [T]
  ), Re = fe(
    (O) => {
      if (O.preventDefault(), T === null || F === null || H === null || T === F) return;
      const q = [...D.current], [le] = q.splice(T, 1);
      let ie = F;
      T < F ? ie = H === "before" ? ie - 1 : ie : ie = H === "before" ? ie : ie + 1, q.splice(ie, 0, le), D.current = q, n(tt, { value: q.map((Ye) => Ye.value) }), V(null), A(null), P(null);
    },
    [T, F, H, n]
  ), Oe = fe(() => {
    V(null), A(null), P(null);
  }, []);
  if ($e(() => {
    if (L < 0 || !M.current) return;
    const O = M.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement(Tr, { key: O.value, option: O, onGoto: ae })));
  const ft = !s && a.length > 0 && !u, et = v ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: S,
      ...Ln
    },
    (c || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: ce,
        onKeyDown: ge,
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
      !c && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ve }, h["js.dropdownSelect.error"])),
      c && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      c && I.map((O, q) => /* @__PURE__ */ e.createElement(
        Rr,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === L,
          searchTerm: C,
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
      ref: j,
      className: "tlDropdownSelect" + (v ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": v,
      "aria-haspopup": "listbox",
      "aria-owns": v ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: v ? void 0 : K,
      onKeyDown: ge
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let le = "";
      return T === q ? le = "tlDropdownSelect__chip--dragging" : F === q && H === "before" ? le = "tlDropdownSelect__chip--dropBefore" : F === q && H === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Dr,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !s),
          onRemove: $,
          removeLabel: w(O.label),
          draggable: p,
          onDragStart: p ? (ie) => we(q, ie) : void 0,
          onDragOver: p ? (ie) => Te(q, ie) : void 0,
          onDrop: p ? Re : void 0,
          onDragEnd: p ? Oe : void 0,
          dragClassName: p ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, ft && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, v ? "▲" : "▼"))
  ), et && bn.createPortal(et, document.body));
}, { useCallback: Nt, useRef: xr } = e, gn = "application/x-tl-color", Mr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: i
}) => {
  const s = xr(null), u = Nt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Nt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = Nt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(gn);
      p ? i(d, p) : s.current !== null && s.current !== d && o(s.current, d), s.current = null;
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
function En(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function At(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function vn(l) {
  if (!At(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function _n(l, t, n) {
  const a = (o) => En(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ir(l, t, n) {
  const a = l / 255, o = t / 255, i = n / 255, s = Math.max(a, o, i), u = Math.min(a, o, i), r = s - u;
  let c = 0;
  r !== 0 && (s === a ? c = (o - i) / r % 6 : s === o ? c = (i - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function Pr(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let s = 0, u = 0, r = 0;
  return l < 60 ? (s = a, u = o, r = 0) : l < 120 ? (s = o, u = a, r = 0) : l < 180 ? (s = 0, u = a, r = o) : l < 240 ? (s = 0, u = o, r = a) : l < 300 ? (s = o, u = 0, r = a) : (s = a, u = 0, r = o), [
    Math.round((s + i) * 255),
    Math.round((u + i) * 255),
    Math.round((r + i) * 255)
  ];
}
function jr(l) {
  return Ir(...vn(l));
}
function St(l, t, n) {
  return _n(...Pr(l, t, n));
}
const { useCallback: He, useRef: rn } = e, Ar = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = jr(l), i = rn(null), s = rn(null), u = He(
    (g, w) => {
      var k;
      const v = (k = i.current) == null ? void 0 : k.getBoundingClientRect();
      if (!v) return;
      const _ = Math.max(0, Math.min(1, (g - v.left) / v.width)), C = Math.max(0, Math.min(1, 1 - (w - v.top) / v.height));
      t(St(n, _, C));
    },
    [n, t]
  ), r = He(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), u(g.clientX, g.clientY);
    },
    [u]
  ), c = He(
    (g) => {
      g.buttons !== 0 && u(g.clientX, g.clientY);
    },
    [u]
  ), d = He(
    (g) => {
      var C;
      const w = (C = s.current) == null ? void 0 : C.getBoundingClientRect();
      if (!w) return;
      const _ = Math.max(0, Math.min(1, (g - w.top) / w.height)) * 360;
      t(St(_, a, o));
    },
    [a, o, t]
  ), m = He(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = He(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = St(n, 1, 1);
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
function Br(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Or = {
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
}, { useState: rt, useCallback: ke, useEffect: on, useRef: Fr, useLayoutEffect: $r } = e, Hr = ({
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
  const [c, d] = rt("palette"), [m, p] = rt(t), h = Fr(null), g = ue(Or), [w, v] = rt(null);
  $r(() => {
    if (!l.current || !h.current) return;
    const M = l.current.getBoundingClientRect(), D = h.current.getBoundingClientRect();
    let Y = M.bottom + 4, f = M.left;
    Y + D.height > window.innerHeight && (Y = M.top - D.height - 4), f + D.width > window.innerWidth && (f = Math.max(0, M.right - D.width)), v({ top: Y, left: f });
  }, [l]);
  const _ = m != null, [C, k, L] = _ ? vn(m) : [0, 0, 0], [E, y] = rt((m == null ? void 0 : m.toUpperCase()) ?? "");
  on(() => {
    y((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Be(!0, { ESCAPE: u }), on(() => {
    const M = (Y) => {
      h.current && !h.current.contains(Y.target) && u();
    }, D = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const b = ke(
    (M) => (D) => {
      const Y = parseInt(D.target.value, 10);
      if (isNaN(Y)) return;
      const f = En(Y);
      p(_n(M === "r" ? f : C, M === "g" ? f : k, M === "b" ? f : L));
    },
    [C, k, L]
  ), S = ke(
    (M) => {
      if (m != null) {
        M.dataTransfer.setData(gn, m.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const D = document.createElement("div");
        D.style.width = "33px", D.style.height = "33px", D.style.backgroundColor = m, D.style.borderRadius = "3px", D.style.border = "1px solid rgba(0,0,0,0.1)", D.style.position = "absolute", D.style.top = "-9999px", document.body.appendChild(D), M.dataTransfer.setDragImage(D, 16, 16), requestAnimationFrame(() => document.body.removeChild(D));
      }
    },
    [m]
  ), x = ke((M) => {
    const D = M.target.value;
    y(D), At(D) && p(D);
  }, []), T = ke(() => {
    p(null);
  }, []), V = ke((M) => {
    p(M);
  }, []), F = ke(
    (M) => {
      s(M);
    },
    [s]
  ), A = ke(
    (M, D) => {
      const Y = [...n], f = Y[M];
      Y[M] = Y[D], Y[D] = f, r(Y);
    },
    [n, r]
  ), H = ke(
    (M, D) => {
      const Y = [...n];
      Y[M] = D, r(Y);
    },
    [n, r]
  ), P = ke(() => {
    r([...o]);
  }, [o, r]), j = ke(
    (M) => {
      if (Br(n, M)) return;
      const D = n.indexOf(null);
      if (D < 0) return;
      const Y = [...n];
      Y[D] = M.toUpperCase(), r(Y);
    },
    [n, r]
  ), B = ke(() => {
    m != null && j(m), s(m);
  }, [m, s, j]);
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
      Mr,
      {
        colors: n,
        columns: a,
        onSelect: V,
        onConfirm: F,
        onSwap: A,
        onReplace: H
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Ar, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: _ ? S : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? C : "",
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
        className: "tlColorInput__input" + (E !== "" && !At(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: x
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, g["js.colorInput.ok"]))
  );
}, Wr = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ur, useCallback: ot, useRef: zr } = e, Vr = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(Wr), [s, u] = Ur(!1), r = zr(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = ot(() => {
    d && u(!0);
  }, [d]), w = ot(
    (C) => {
      u(!1), a(C);
    },
    [a]
  ), v = ot(() => {
    u(!1);
  }, []), _ = ot(
    (C) => {
      o("paletteChanged", { palette: C });
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
      "aria-label": i["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    Hr,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
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
      className: "tlColorInput tlColorInput--immutable" + (c == null ? " tlColorInput--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      title: c ?? ""
    }
  );
}, { useState: Ze, useCallback: Pe, useEffect: Dt, useRef: sn, useLayoutEffect: Kr, useMemo: Yr } = e, Gr = {
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
}, Xr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: i,
  onLoadIcons: s
}) => {
  const u = ue(Gr), [r, c] = Ze("simple"), [d, m] = Ze(""), [p, h] = Ze(t ?? ""), [g, w] = Ze(!1), [v, _] = Ze(null), C = sn(null), k = sn(null);
  Kr(() => {
    if (!l.current || !C.current) return;
    const F = l.current.getBoundingClientRect(), A = C.current.getBoundingClientRect();
    let H = F.bottom + 4, P = F.left;
    H + A.height > window.innerHeight && (H = F.top - A.height - 4), P + A.width > window.innerWidth && (P = Math.max(0, F.right - A.width)), _({ top: H, left: P });
  }, [l]), Dt(() => {
    !a && !g && s().catch(() => w(!0));
  }, [a, g, s]), Dt(() => {
    a && k.current && k.current.focus();
  }, [a]), Be(!0, { ESCAPE: i }), Dt(() => {
    const F = (H) => {
      C.current && !C.current.contains(H.target) && i();
    }, A = setTimeout(() => document.addEventListener("mousedown", F), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", F);
    };
  }, [i]);
  const L = Yr(() => {
    if (!d) return n;
    const F = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(F) || A.label.toLowerCase().includes(F) || A.terms != null && A.terms.some((H) => H.includes(F))
    );
  }, [n, d]), E = Pe((F) => {
    m(F.target.value);
  }, []), y = Pe(
    (F) => {
      o(F);
    },
    [o]
  ), b = Pe((F) => {
    h(F);
  }, []), S = Pe((F) => {
    h(F.target.value);
  }, []), x = Pe(() => {
    o(p || null);
  }, [p, o]), T = Pe(() => {
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
      ref: C,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
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
        onChange: E,
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
            onClick: () => r === "simple" ? y(A.encoded) : b(A.encoded),
            onKeyDown: (H) => {
              (H.key === "Enter" || H.key === " ") && (H.preventDefault(), r === "simple" ? y(A.encoded) : b(A.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ne, { encoded: A.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: p,
        onChange: S
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: x }, u["js.iconSelect.ok"]))
  );
}, qr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Zr, useCallback: st, useRef: Qr } = e, Jr = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(qr), [s, u] = Zr(!1), r = Qr(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = st(() => {
    d && !m && u(!0);
  }, [d, m]), w = st(
    (C) => {
      u(!1), a(C);
    },
    [a]
  ), v = st(() => {
    u(!1);
  }, []), _ = st(async () => {
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
      "aria-label": i["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    Xr,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: w,
      onCancel: v,
      onLoadIcons: _
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: We, useEffect: eo, useMemo: cn, useRef: to, useState: Tt } = e, no = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, lo = [1, 2, 3, 4];
function ao(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function ro(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of lo)
    n >= o && (a = o);
  return a;
}
function oo(l, t) {
  const n = no[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function so(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, p) => !!(a[m] && a[m][p]), i = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, s = [];
  let u = 0, r = 0;
  const c = (m) => {
    let p = null;
    for (const g of s) g.rowStart === m && (p = g);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let g = p.rowStart; g < p.rowEnd; g++)
        for (let w = p.colEnd; w < h; w++) i(g, w);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(oo(m.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let g = 0;
    for (let k = r; k < n && !o(u, k); k++)
      g++;
    if (h > g) {
      for (c(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      g = 0;
      for (let k = r; k < n && !o(u, k); k++)
        g++;
      h = Math.min(h, g);
    }
    const w = r, v = r + h, _ = u, C = u + p;
    s.push({ id: m.id, colStart: w, colEnd: v, rowStart: _, rowEnd: C });
    for (let k = _; k < C; k++)
      for (let L = w; L < v; L++) i(k, L);
    r = v, r >= n && (r = 0, u++);
  }
  c(u);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (o(m, p)) continue;
      const h = s.find((g) => g.rowEnd === m && g.colStart <= p && p < g.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let g = h.colStart; g < h.colEnd; g++) i(m, g);
      }
    }
  return s;
}
const co = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((y) => y && y.id), i = to(null), [s, u] = Tt(1), r = t.editMode === !0;
  eo(() => {
    const y = i.current;
    if (!y) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, S = ao(a, b), x = () => u(ro(y.clientWidth, S));
    x();
    const T = new ResizeObserver(x);
    return T.observe(y), () => T.disconnect();
  }, [a]);
  const c = cn(() => so(o, s), [o, s]), d = cn(() => {
    const y = {};
    for (const b of c) y[b.id] = b;
    return y;
  }, [c]), [m, p] = Tt(null), [h, g] = Tt(null), w = We((y, b) => {
    if (!r) {
      y.preventDefault();
      return;
    }
    p(b), y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", b);
  }, [r]), v = We((y, b) => {
    if (!r || !m || m === b) return;
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const S = y.currentTarget.getBoundingClientRect(), x = y.clientX < S.left + S.width / 2;
    g((T) => T && T.id === b && T.before === x ? T : { id: b, before: x });
  }, [r, m]), _ = We(() => {
  }, []), C = We((y, b, S) => {
    const x = o.map((A) => A.id), T = x.indexOf(y);
    if (T < 0) return;
    x.splice(T, 1);
    const V = x.indexOf(b);
    if (V < 0) {
      x.splice(T, 0, y);
      return;
    }
    const F = S ? V : V + 1;
    x.splice(F, 0, y), n("reorder", { order: x });
  }, [o, n]), k = We((y, b) => {
    if (!r || !m || m === b) return;
    y.preventDefault();
    const S = y.currentTarget.getBoundingClientRect(), x = y.clientX < S.left + S.width / 2;
    C(m, b, x), p(null), g(null);
  }, [r, m, C]), L = We(() => {
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
      ref: i,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: E }, o.map((y) => {
      const b = d[y.id];
      if (!b) return null;
      const S = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, x = ["tlDashboard__tile"];
      return m === y.id && x.push("tlDashboard__tile--dragging"), h && h.id === y.id && x.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: x.join(" "),
          style: S,
          draggable: r,
          onDragStart: (T) => w(T, y.id),
          onDragOver: (T) => v(T, y.id),
          onDragLeave: _,
          onDrop: (T) => k(T, y.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(G, { control: y.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: io, useRef: un, useState: dn, useEffect: uo, useLayoutEffect: mo } = e, po = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, fo = ({ group: l }) => {
  var m, p;
  const [t, n] = dn(!1), [a, o] = dn({}), i = un(null), s = un(null), u = io(() => {
    n((h) => !h);
  }, []);
  mo(() => {
    if (!t) return;
    const h = () => {
      const g = i.current;
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
  }, [t]), uo(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && i.current && !i.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Be(t, { ESCAPE: () => n(!1) }), Ot(t, s, "first");
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
  ), bn.createPortal(
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
      r.map((h, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (p = l.subGroups) == null ? void 0 : p.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((w, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: w })))))
    ),
    document.body
  ));
}, ho = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(fo, { group: o }) : /* @__PURE__ */ e.createElement(po, { group: o }))));
}, bo = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(G, { control: t.frame }));
}, go = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.content, o = t.breadcrumb ?? null;
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
}, Eo = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, vo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), _o = {
  "js.sidebar.openDrawer": "Open navigation"
}, Co = ({ controlId: l }) => {
  const t = ne(), n = ue(_o);
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
U("TLButton", Xn);
U("TLUploadButton", qn);
U("TLToggleButton", Qn);
U("TLTextInput", In);
U("TLPasswordInput", jn);
U("TLNumberInput", Bn);
U("TLDatePicker", Fn);
U("TLSelect", Hn);
U("TLBooleanChoice", Un);
U("TLCheckbox", Yn);
U("TLCounter", Jn);
U("TLTabBar", tl);
U("TLFieldList", nl);
U("TLAudioRecorder", al);
U("TLAudioPlayer", ol);
U("TLFileUpload", cl);
U("TLBinaryField", ul);
U("TLFileChips", pl);
U("TLRelativeTime", bl);
U("TLAnchor", gl);
U("TLScrollLink", El);
U("TLAvatar", Cl);
U("TLDownload", wl);
U("TLPhotoCapture", Nl);
U("TLPhotoViewer", Dl);
U("TLPdfViewer", Rl);
U("TLSplitPanel", Ll);
U("TLPanel", Bl);
U("TLInset", Xl);
U("TLMaximizeRoot", Ol);
U("TLDeckPane", Fl);
U("TLSidebar", Yl);
U("TLStack", Gl);
U("TLGrid", ql);
U("TLCard", Zl);
U("TLAppBar", Ql);
U("TLBreadcrumb", ea);
U("TLBottomBar", na);
U("TLDialog", ra);
U("TLDialogManager", ca);
U("TLWindow", ma);
U("TLDrawer", ha);
U("TLMenuRegion", ga);
U("TLSnackbar", Ca);
U("TLNoticeBar", Ta);
U("TLMenu", La);
U("TLAppShell", Ma);
U("TLText", Ia);
U("TLTableView", Oa);
U("TLColumnSelect", $a);
U("TLCalendar", er);
U("TLFormLayout", ir);
U("TLFormGroup", mr);
U("TLFormField", br);
U("TLResourceCell", Er);
U("TLTreeView", _r);
U("TLDropdownSelect", Lr);
U("TLColorInput", Vr);
U("TLIconSelect", Jr);
U("TLDashboard", co);
U("TLToolbar", ho);
U("TLTileStack", bo);
U("TLAdaptiveDetail", go);
U("TLSlot", Eo);
U("TLSlotContent", vo);
U("TLDrawerToggle", Co);
