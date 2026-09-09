import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as G, useKeyboardBinding as me, useTLUpload as Ye, TLChild as q, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as Kn, useStandaloneKeyboardScope as Fe, KeyboardScopeProvider as Ht, useFocusTrap as Wt, CMD_VALUE_CHANGED as rt, anchoredOverlayProps as Yn, register as W } from "tl-react-bridge";
const { useCallback: Gt, useRef: Gn } = e, Xn = 300, qn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: Xn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), c = ne(), s = Gn(!1), u = Gt(
    (k) => {
      s.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, i = Gt(async () => {
    await o(), r && s.current && (s.current = !1, c("commit"));
  }, [o, r, c]), d = t.multiline === !0;
  if (t.editable === !1) {
    const k = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: k,
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
}, { useCallback: Xt } = e, Zn = 300, Qn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: Zn }), c = Xt(
    (f) => {
      a(f.target.value);
    },
    [a]
  ), s = Xt(() => {
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
}, { useCallback: qt } = e, Jn = 300, el = ({ controlId: l, state: t, config: n }) => {
  const [a, o, c] = De({ debounceMs: Jn }), s = qt(
    (m) => {
      const h = m.target.value;
      o(h === "" ? null : h);
    },
    [o]
  ), u = qt(() => {
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
}, { useCallback: tl } = e, nl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = tl(
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
}, { useCallback: ll } = e, al = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), c = ll(
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
}, { useCallback: rl } = e, ol = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], c = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, i = rl(
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
}, { useCallback: sl, useRef: cl, useEffect: il } = e, ul = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, c = cl(null);
  il(() => {
    c.current && (c.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = sl(
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
const { useCallback: dl } = e, ml = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: c }) => {
  const s = G(), u = ne(), r = t ?? "click", i = n ?? s.label, d = a ?? s.image, f = o ?? s.disabled === !0, m = c ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, k = s.appearance, _ = s.size, C = s.navigateUrl, w = dl(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(r);
  }, [u, r, C]), N = s.keyGesture;
  me(N, () => f || h ? !1 : (w(), !0));
  const x = m === "icon-only", E = m === "label-only" || m === "icon-label" || x && !d, y = g ?? (x ? i : void 0), b = y ? `text:${y}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: f,
      className: "tlReactButton" + (x ? " tlReactButton--iconOnly" : "") + (m === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": b,
      "aria-label": d || x ? i : void 0
    },
    d && /* @__PURE__ */ e.createElement(Se, { encoded: d, className: "tlReactButton__image" }),
    E && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  );
}, pl = ({ controlId: l }) => {
  const t = G(), n = Ye(), a = e.useRef(null), [o, c] = e.useState(!1), s = t.label ?? "", u = t.image, r = t.disabled === !0, i = t.hidden === !0, d = t.displayMode ?? "label-only", f = t.appearance, m = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), k = e.useCallback(async (x) => {
    const E = x.target.files;
    if (!E || E.length === 0) return;
    const y = new FormData();
    for (let b = 0; b < E.length; b++)
      y.append("file", E[b], E[b].name);
    x.target.value = "", c(!0);
    try {
      await n(y);
    } finally {
      c(!1);
    }
  }, [n]), _ = d === "icon-only", C = d === "icon-only" || d === "icon-label", w = d === "label-only" || d === "icon-label" || _ && !u, N = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: m && m !== "*" ? m : void 0,
      multiple: h || void 0,
      onChange: k,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: N,
      style: i ? { display: "none" } : void 0,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (f === "link" ? " tlReactButton--link" : "") + (f === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? s : void 0
    },
    C && u && /* @__PURE__ */ e.createElement(Se, { encoded: u, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: fl } = e, hl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const c = G(), s = ne(), u = t ?? "click", r = n ?? c.label, i = a ?? c.active === !0, d = o ?? c.disabled === !0, f = fl(() => {
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
}, bl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: gl } = e, El = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.tabs ?? [], o = t.activeTabId, c = gl((s) => {
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
}, vl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: o })))));
}, _l = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Cl = ({ controlId: l }) => {
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [c, s] = e.useState(null), u = e.useRef(null), r = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", f = t.error, m = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        i.current = w, r.current = [];
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(w, N ? { mimeType: N } : void 0);
        u.current = x, x.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, x.onstop = async () => {
          w.getTracks().forEach((b) => b.stop()), i.current = null;
          const E = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const y = new FormData();
          y.append("audio", E, "recording.webm"), await n(y), o("idle");
        }, x.start(), o("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(_l), k = m === "recording" ? g["js.audioRecorder.stop"] : m === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], _ = m === "uploading", C = ["tlAudioRecorder__button"];
  return m === "recording" && C.push("tlAudioRecorder__button--recording"), m === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: h,
      disabled: _,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${m === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[c]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, yl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, wl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [c, s] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), i = e.useRef(o);
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
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), s("idle");
          return;
        }
        const C = await _.blob();
        r.current = URL.createObjectURL(C);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), s("idle");
        return;
      }
    }
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      s("idle");
    }, k.play(), s("playing");
  }, [c, n]), f = ue(yl), m = c === "loading" ? f["js.loading"] : c === "playing" ? f["js.audioPlayer.pause"] : c === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], h = c === "disabled" || c === "loading", g = ["tlAudioPlayer__button"];
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
}, kl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Nl = ({ controlId: l }) => {
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [c, s] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", i = t.error, d = t.accept ?? "", f = r === "received" ? "idle" : a !== "idle" ? a : r, m = e.useCallback(async (E) => {
    o("uploading");
    const y = new FormData();
    y.append("file", E, E.name), await n(y), o("idle");
  }, [n]), h = e.useCallback((E) => {
    var b;
    const y = (b = E.target.files) == null ? void 0 : b[0];
    y && m(y);
  }, [m]), g = e.useCallback(() => {
    var E;
    a !== "uploading" && ((E = u.current) == null || E.click());
  }, [a]), k = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), _ = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), C = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const y = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    y && m(y);
  }, [a, m]), w = f === "uploading", N = ue(kl), x = f === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
      onDragLeave: _,
      onDrop: C
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
        disabled: w,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Sl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Dl = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, o = Ye(), c = Ge(), s = ue(Sl), u = a.editable !== !1, r = !!a.hasData, i = a.fileName ?? "download", d = a.dataRevision ?? 0, f = a.accept ?? "", m = a.status ?? "idle", h = a.error ?? null, [g, k] = e.useState("idle"), [_, C] = e.useState(!1), [w, N] = e.useState(!1), x = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || w)) {
      N(!0);
      try {
        const j = c + (c.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(j);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const S = await L.blob(), $ = URL.createObjectURL(S), p = document.createElement("a");
        p.href = $, p.download = i, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL($);
      } catch (j) {
        console.error("[TLBinaryField] Fetch error:", j);
      } finally {
        N(!1);
      }
    }
  }, [r, w, c, d, i]), y = e.useCallback(async (j) => {
    k("uploading");
    const L = new FormData();
    L.append("file", j, j.name), await o(L), k("idle");
  }, [o]), b = (m === "received" ? "idle" : g !== "idle" ? g : m) === "uploading", R = e.useCallback((j) => {
    var S;
    const L = (S = j.target.files) == null ? void 0 : S[0];
    L && y(L);
  }, [y]), I = e.useCallback(() => {
    var j;
    b || (j = x.current) == null || j.click();
  }, [b]), D = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!0);
  }, []), K = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!1);
  }, []), U = e.useCallback((j) => {
    var S;
    if (j.preventDefault(), j.stopPropagation(), C(!1), b) return;
    const L = (S = j.dataTransfer.files) == null ? void 0 : S[0];
    L && y(L);
  }, [b, y]), B = w ? s["js.downloading"] : s["js.download.file"].replace("{0}", i), V = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: E,
      disabled: w,
      title: B,
      "aria-label": B
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, V) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const P = b, A = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: D,
      onDragLeave: K,
      onDrop: U
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "file",
        accept: f || void 0,
        onChange: R,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: I,
        disabled: P,
        title: A,
        "aria-label": A
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && V,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Tl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Rl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Ll = ({ controlId: l }) => {
  const t = G(), n = ne(), a = Ye(), o = Ge(), c = ue(Tl), s = t.chips ?? [], u = t.editable === !0, [r, i] = e.useState(!1), [d, f] = e.useState(!1), m = e.useRef(null), h = e.useCallback(async (E) => {
    const y = Array.from(E);
    if (y.length !== 0) {
      i(!0);
      try {
        const b = new FormData();
        for (const R of y)
          b.append("file", R, R.name);
        await a(b);
      } finally {
        i(!1);
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
        const R = await b.blob(), I = URL.createObjectURL(R), D = document.createElement("a");
        D.href = I, D.download = E.name, D.style.display = "none", document.body.appendChild(D), D.click(), document.body.removeChild(D), URL.revokeObjectURL(I);
      } catch (y) {
        console.error("[TLFileChips] Fetch error:", y);
      }
  }, [o]), k = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), _ = e.useCallback(() => {
    var E;
    r || (E = m.current) == null || E.click();
  }, [r]), C = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!0));
  }, [u]), w = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!1));
  }, [u]), N = e.useCallback((E) => {
    u && (E.preventDefault(), E.stopPropagation(), f(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
  }, [u, r, h]), x = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: x,
      onDragOver: C,
      onDragLeave: w,
      onDrop: N
    },
    s.map((E) => {
      const y = c["js.download.file"].replace("{0}", E.name), b = c["js.fileChips.remove"].replace("{0}", E.name);
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
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Rl(E.size))
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
        onChange: k,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: _,
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
}, xl = 3e4;
function Il(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Ml = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => c((u) => u + 1), xl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Il(n, o));
}, jl = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, Pl = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (c) => {
    c.preventDefault(), Kn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function Al(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Bl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Fl = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Bl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Al(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Ol = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, $l = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = ne(), o = !!t.hasData, c = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [r, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      i(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + c, k = await fetch(g);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const _ = await k.blob(), C = URL.createObjectURL(_), w = document.createElement("a");
        w.href = C, w.download = s, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(C);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [o, r, n, c, s]), f = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), m = ue(Ol);
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
}, Hl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Wl = ({ controlId: l }) => {
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [c, s] = e.useState(null), [u, r] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), f = e.useRef(null), m = e.useRef(null), h = e.useRef(null), g = t.error, k = e.useMemo(
    () => {
      var D;
      return !!(window.isSecureContext && ((D = navigator.mediaDevices) != null && D.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((D) => D.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    _(), o("idle");
  }, [_]), w = e.useCallback(async () => {
    var D;
    if (a !== "uploading") {
      if (s(null), !k) {
        (D = m.current) == null || D.click();
        return;
      }
      try {
        const K = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = K, o("overlayOpen");
      } catch (K) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", K), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, k]), N = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const D = i.current, K = f.current;
    if (!D || !K)
      return;
    K.width = D.videoWidth, K.height = D.videoHeight;
    const U = K.getContext("2d");
    U && (U.drawImage(D, 0, 0), _(), o("uploading"), K.toBlob(async (B) => {
      if (!B) {
        o("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", B, "capture.jpg"), await n(V), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), x = e.useCallback(async (D) => {
    var B;
    const K = (B = D.target.files) == null ? void 0 : B[0];
    if (!K) return;
    o("uploading");
    const U = new FormData();
    U.append("photo", K, K.name), await n(U), o("idle"), m.current && (m.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var K;
    if (a !== "overlayOpen") return;
    (K = h.current) == null || K.focus();
    const D = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = D;
    };
  }, [a]), Fe(a === "overlayOpen", { ESCAPE: C }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((D) => D.stop()), d.current = null);
  }, []);
  const E = ue(Hl), y = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const R = ["tlPhotoCapture__overlayVideo"];
  u && R.push("tlPhotoCapture__overlayVideo--mirrored");
  const I = ["tlPhotoCapture__mirrorBtn"];
  return u && I.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: w,
      disabled: a === "uploading",
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !k && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: m,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: R.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: I.join(" "),
        onClick: () => r((D) => !D),
        title: E["js.photoCapture.mirror"],
        "aria-label": E["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: N,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[c]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Ul = {
  "js.photoViewer.alt": "Captured photo"
}, zl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [c, s] = e.useState(null), u = e.useRef(o);
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
  const r = ue(Ul);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Vl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Kl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasPdf, o = t.dataRevision ?? 0, c = ue(Vl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, i = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(i);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: Zt, useRef: yt } = e, Yl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.orientation, o = t.resizable === !0, c = t.children ?? [], s = a === "horizontal", u = c.length > 0 && c.every((_) => _.collapsed), r = !u && c.some((_) => _.collapsed), i = u ? !s : s, d = yt(null), f = yt(null), m = yt(null), h = Zt((_, C) => {
    const w = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? u && !i ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : C !== void 0 ? w.flex = `0 0 ${C}px` : w.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (w.minWidth = s ? _.minSize : void 0, w.minHeight = s ? void 0 : _.minSize), w;
  }, [s, u, r, i]), g = Zt((_, C) => {
    _.preventDefault();
    const w = d.current;
    if (!w) return;
    const N = c[C], x = c[C + 1], E = w.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    E.forEach((I) => {
      y.push(s ? I.offsetWidth : I.offsetHeight);
    }), m.current = y, f.current = {
      splitterIndex: C,
      startPos: s ? _.clientX : _.clientY,
      startSizeBefore: y[C],
      startSizeAfter: y[C + 1],
      childBefore: N,
      childAfter: x
    };
    const b = (I) => {
      const D = f.current;
      if (!D || !m.current) return;
      const U = (s ? I.clientX : I.clientY) - D.startPos, B = D.childBefore.minSize || 0, V = D.childAfter.minSize || 0;
      let P = D.startSizeBefore + U, A = D.startSizeAfter - U;
      P < B && (A += P - B, P = B), A < V && (P += A - V, A = V), m.current[D.splitterIndex] = P, m.current[D.splitterIndex + 1] = A;
      const j = w.querySelectorAll(":scope > .tlSplitPanel__child"), L = j[D.splitterIndex], S = j[D.splitterIndex + 1];
      L && (L.style.flex = `0 0 ${P}px`), S && (S.style.flex = `0 0 ${A}px`);
    }, R = () => {
      if (document.removeEventListener("mousemove", b), document.removeEventListener("mouseup", R), document.body.style.cursor = "", document.body.style.userSelect = "", m.current) {
        const I = {};
        c.forEach((D, K) => {
          const U = D.control;
          U != null && U.controlId && m.current && (I[U.controlId] = m.current[K]);
        }), n("updateSizes", { sizes: I });
      }
      m.current = null, f.current = null;
    };
    document.addEventListener("mousemove", b), document.addEventListener("mouseup", R), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, s, n]), k = [];
  return c.forEach((_, C) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${_.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: h(_)
        },
        /* @__PURE__ */ e.createElement(q, { control: _.control })
      )
    ), o && C < c.length - 1) {
      const w = c[C + 1];
      !_.collapsed && !w.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (x) => g(x, C)
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
    k
  );
}, pt = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: wt } = e, Gl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Xl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ql = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Zl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ql = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Jl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ea = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Gl), o = t.title, c = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, i = t.fullLine === !0, d = t.fill === !0, f = t.hoverActions === !0, m = t.appearance === "card", h = t.errorMessage, g = c === "MINIMIZED", k = c === "MAXIMIZED", _ = c === "HIDDEN", C = wt(() => {
    n("toggleMinimize");
  }, [n]), w = wt(() => {
    n("toggleMaximize");
  }, [n]), N = wt(() => {
    n("popOut");
  }, [n]);
  if (_)
    return null;
  const x = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = s && !k || u && !g || r, y = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || E;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${i ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${f ? " tlPanel--hoverActions" : ""}${m ? " tlPanel--card" : ""}`,
      style: x
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(q, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(q, { control: t.toolbar }), s && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(ql, null) : /* @__PURE__ */ e.createElement(Xl, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(Ql, null) : /* @__PURE__ */ e.createElement(Zl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Jl, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child })),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(pt, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(q, { control: t.buttonBar }))
  );
}, ta = ({ controlId: l }) => {
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
}, na = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(q, { control: t.activeChild }));
}, { useCallback: Ee, useState: dt, useEffect: jt, useRef: ft } = e, la = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Pt(l, t, n, a) {
  const o = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      o.push({ id: c.id, type: "nav", groupId: a });
    } else c.type === "command" ? o.push({ id: c.id, type: "command", groupId: a }) : c.type === "group" && (o.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && o.push(...Pt(c.children, t, n, c.id)));
  return o;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Se, { encoded: l, className: "tlSidebar__icon" }) : null, aa = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: c,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ra = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), oa = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), sa = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ca = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: c }) => {
  const s = ft(null);
  jt(() => {
    const i = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", i), () => document.removeEventListener("mousedown", i);
  }, [c]), Fe(!0, { ESCAPE: c });
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
        /* @__PURE__ */ e.createElement(Ke, { icon: i.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, i.label),
        i.type === "nav" && i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, i.badge)
      );
    }
    return i.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: i.id, className: "tlSidebar__flyoutSectionHeader" }, i.label) : i.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: i.id, className: "tlSidebar__separator" }) : null;
  }));
}, ia = ({
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
  onCloseFlyout: k
}) => {
  const _ = ft(null), [C, w] = dt(null), N = Ee(() => {
    a ? h === l.id ? k() : (_.current && w(_.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, k]), x = Ee((y) => {
    _.current = y, r(y);
  }, [r]), E = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: N,
      title: a ? l.label : void 0,
      "aria-expanded": a ? E : t,
      tabIndex: u,
      ref: x,
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
  ), E && /* @__PURE__ */ e.createElement(
    ca,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: o,
      onExecute: c,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((y) => /* @__PURE__ */ e.createElement(
    gn,
    {
      key: y.id,
      item: y,
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
      onCloseFlyout: k
    }
  ))));
}, gn = ({
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
        aa,
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
        ra,
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
      return /* @__PURE__ */ e.createElement(oa, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(sa, null);
    case "group": {
      const h = i ? i.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ia,
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
}, ua = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(la), o = t.items ?? [], c = t.activeItemId, s = t.collapsed, u = t.drawerOpen, r = u ? !1 : s, [i, d] = dt(() => {
    const B = /* @__PURE__ */ new Map(), V = (P) => {
      for (const A of P)
        A.type === "group" && (B.set(A.id, A.expanded), V(A.children));
    };
    return V(o), B;
  }), f = Ee((B) => {
    d((V) => {
      const P = new Map(V), A = P.get(B) ?? !1;
      return P.set(B, !A), n("toggleGroup", { itemId: B, expanded: !A }), P;
    });
  }, [n]), m = Ee((B) => {
    B !== c && n("selectItem", { itemId: B });
  }, [n, c]), h = Ee((B) => {
    n("executeCommand", { itemId: B });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), k = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [_, C] = dt(null), w = Ee((B) => {
    C(B);
  }, []), N = Ee(() => {
    C(null);
  }, []);
  jt(() => {
    r || C(null);
  }, [r]);
  const [x, E] = dt(() => {
    const B = Pt(o, r, i);
    return B.length > 0 ? B[0].id : "";
  }), y = ft(/* @__PURE__ */ new Map()), b = Ee((B) => (V) => {
    V ? y.current.set(B, V) : y.current.delete(B);
  }, []), R = Ee((B) => {
    E(B);
  }, []), I = ft(0), D = Ee((B) => {
    E(B), I.current++;
  }, []);
  jt(() => {
    const B = y.current.get(x);
    B && document.activeElement !== B && B.focus();
  }, [x, I.current]);
  const K = Ee((B) => {
    if (B.key === "Escape" && _ !== null) {
      B.preventDefault(), N();
      return;
    }
    const V = Pt(o, r, i);
    if (V.length === 0) return;
    const P = V.findIndex((j) => j.id === x);
    if (P < 0) return;
    const A = V[P];
    switch (B.key) {
      case "ArrowDown": {
        B.preventDefault();
        const j = (P + 1) % V.length;
        D(V[j].id);
        break;
      }
      case "ArrowUp": {
        B.preventDefault();
        const j = (P - 1 + V.length) % V.length;
        D(V[j].id);
        break;
      }
      case "Home": {
        B.preventDefault(), D(V[0].id);
        break;
      }
      case "End": {
        B.preventDefault(), D(V[V.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        B.preventDefault(), A.type === "nav" ? m(A.id) : A.type === "command" ? h(A.id) : A.type === "group" && (r ? _ === A.id ? N() : w(A.id) : f(A.id));
        break;
      }
      case "ArrowRight": {
        A.type === "group" && !r && ((i.get(A.id) ?? !1) || (B.preventDefault(), f(A.id)));
        break;
      }
      case "ArrowLeft": {
        A.type === "group" && !r && (i.get(A.id) ?? !1) && (B.preventDefault(), f(A.id));
        break;
      }
    }
  }, [
    o,
    r,
    i,
    x,
    _,
    D,
    m,
    h,
    f,
    w,
    N
  ]), U = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: U }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(q, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, o.map((B) => /* @__PURE__ */ e.createElement(
    gn,
    {
      key: B.id,
      item: B,
      activeItemId: c,
      collapsed: r,
      onSelect: m,
      onExecute: h,
      onToggleGroup: f,
      focusedId: x,
      setItemRef: b,
      onItemFocus: R,
      groupStates: i,
      flyoutGroupId: _,
      onOpenFlyout: w,
      onCloseFlyout: N
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
}, da = ({ controlId: l }) => {
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
}, ma = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, pa = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", c = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, c.map((u, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: u })));
}, fa = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", c = t.headerActions ?? [], s = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, i) => /* @__PURE__ */ e.createElement(q, { key: i, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(q, { control: s })));
}, ha = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, o = t.children ?? [], c = t.actions ?? [], s = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    s === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, o.map((i, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: i }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((i, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: i }))));
}, { useCallback: ba } = e, ga = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = ba((c) => {
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
}, { useCallback: Ea } = e, va = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = t.activeItemId, c = Ea((s) => {
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
}, { useCallback: Qt, useRef: _a } = e, Ca = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ya = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, c = t.child, s = _a(null), u = Qt(() => {
    n("close");
  }, [n]), r = Qt((i) => {
    o && i.target === i.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(Ht, null, /* @__PURE__ */ e.createElement(Ca, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: wa, useRef: ka } = e, Na = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = ka(n.length);
  return wa(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(q, { key: o.controlId, control: o })));
}, { useCallback: ot, useRef: He, useState: st } = e, Sa = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Da = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ta = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Ra = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Da), o = t.title ?? "", c = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, i = t.child, d = t.actions ?? [], f = t.toolbar, m = t.buttonBar, [h, g] = st(null), [k, _] = st(null), [C, w] = st(null), N = He(null), [x, E] = st(!1), y = He(null), b = He(null), R = He(null), I = He(null), D = He(null), K = ot(() => {
    n("close");
  }, [n]);
  Wt(!0, I, "field");
  const U = ot((j, L) => {
    L.preventDefault();
    const S = I.current;
    if (!S) return;
    const $ = S.getBoundingClientRect(), p = !N.current, M = N.current ?? { x: $.left, y: $.top };
    p && (N.current = M, w(M)), D.current = {
      dir: j,
      startX: L.clientX,
      startY: L.clientY,
      startW: $.width,
      startH: $.height,
      startPos: { ...M },
      symmetric: p
    };
    const Y = (Z) => {
      const F = D.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let le = F.startW, pe = F.startH, ye = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (le = F.startW + 2 * te), F.dir.includes("w") && (le = F.startW - 2 * te), F.dir.includes("s") && (pe = F.startH + 2 * se), F.dir.includes("n") && (pe = F.startH - 2 * se)) : (F.dir.includes("e") && (le = F.startW + te), F.dir.includes("w") && (le = F.startW - te, ye = te), F.dir.includes("s") && (pe = F.startH + se), F.dir.includes("n") && (pe = F.startH - se, we = se));
      const Te = Math.max(200, le), Re = Math.max(100, pe);
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), b.current = Te, R.current = Re, g(Te), _(Re);
      const Oe = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      N.current = Oe, w(Oe);
    }, H = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", H);
      const Z = b.current, F = R.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), D.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", H);
  }, [n]), B = ot((j) => {
    if (j.button !== 0 || j.target.closest("button")) return;
    j.preventDefault();
    const L = I.current;
    if (!L) return;
    const S = L.getBoundingClientRect(), $ = N.current ?? { x: S.left, y: S.top }, p = j.clientX - $.x, M = j.clientY - $.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - p, le = Z.clientY - M;
      const pe = L.offsetWidth, ye = L.offsetHeight;
      se + pe > F && (se = F - pe), le + ye > te && (le = te - ye), se < 0 && (se = 0), le < 0 && (le = 0);
      const we = { x: se, y: le };
      N.current = we, w(we);
    }, H = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", H);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", H);
  }, []), V = ot(() => {
    var j, L;
    if (x) {
      const S = y.current;
      S && (w(S.x !== -1 ? { x: S.x, y: S.y } : null), g(S.w), _(S.h)), E(!1);
    } else {
      const S = I.current, $ = S == null ? void 0 : S.getBoundingClientRect();
      y.current = {
        x: ((j = N.current) == null ? void 0 : j.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = N.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: k ?? null
      }, E(!0), w({ x: 0, y: 0 }), g(null), _(null);
    }
  }, [x, h, k]), P = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : c,
    ...k != null ? { height: k + "px" } : s != null ? { height: s } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, A = l + "-title";
  return /* @__PURE__ */ e.createElement(Ht, { modal: !0 }, /* @__PURE__ */ e.createElement(Sa, { onClose: K }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: I,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": A
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : B,
        onDoubleClick: r ? V : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: A }, o),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(q, { control: f })),
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
          onClick: K,
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
    (d.length > 0 || m) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m && /* @__PURE__ */ e.createElement(q, { control: m }), d.map((j, L) => /* @__PURE__ */ e.createElement(q, { key: L, control: j }))),
    r && !x && Ta.map((j) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: j,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${j}`,
        onMouseDown: (L) => U(j, L)
      }
    ))
  ));
}, { useCallback: La } = e, xa = {
  "js.drawer.close": "Close"
}, Ia = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(xa), o = t.open === !0, c = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, r = t.child, i = La(() => {
    n("close");
  }, [n]);
  Fe(o, { ESCAPE: i });
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
}, { useCallback: Ma } = e, ja = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.child, o = Ma((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: o }, a && /* @__PURE__ */ e.createElement(q, { control: a }));
}, { useCallback: Pa, useEffect: Jt, useRef: Aa, useState: en } = e, Ba = 250, Fa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.message ?? "", o = t.content ?? "", c = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [i, d] = en(!1), [f, m] = en(!1), h = Aa(!1);
  Jt(() => {
    h.current = !1;
  }, [r]);
  const g = Pa(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Jt(() => {
    if (!u || s === 0 || f) return;
    const k = setTimeout(g, h.current ? Ba : s);
    return () => clearTimeout(k);
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
}, { useCallback: Oa, useEffect: tn, useMemo: $a, useRef: Ha, useState: Wa } = e, Ua = 1e3;
function za(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), c = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${c(a)}:${c(n)}` : `${a}:${c(n)}`;
}
const Va = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", c = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, i = t.actionLabel ?? null, d = t.pingGraceMs ?? null, f = $a(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [m, h] = Wa(0), g = a && s != null;
  tn(() => {
    if (!g) return;
    const x = setInterval(() => h((E) => E + 1), Ua);
    return () => clearInterval(x);
  }, [g, s]);
  const k = Ha(null);
  tn(() => {
    !g || d == null || s == null || k.current !== s && (Date.now() + f < s + d || (k.current = s, n("deadlinePassed", {})));
  }, [m, g, s, d, f, n]);
  const _ = Oa(() => {
    i != null && n("action", {});
  }, [n, i]);
  if (!a) return null;
  const C = s != null ? s - (Date.now() + f) : null;
  if (r != null && C != null && C > r) return null;
  const w = C != null ? za(C) : null, N = i != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${N ? " tlNoticeBar--clickable" : ""}`,
      role: N ? "button" : "status",
      "aria-live": "polite",
      tabIndex: N ? 0 : void 0,
      title: i ?? void 0,
      "aria-label": N ? `${c} ${i}` : void 0,
      onClick: N ? _ : void 0,
      onKeyDown: N ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, c),
    w !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, w)
  );
}, { useCallback: kt, useEffect: nn, useRef: Ka, useState: ln } = e, Ya = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.anchorId, c = t.anchorX, s = t.anchorY, u = t.items ?? [], r = Ka(null), [i, d] = ln({ top: 0, left: 0 }), [f, m] = ln(0), h = u.filter((C) => C.type === "item" && !C.disabled);
  nn(() => {
    var b, R;
    if (!a) return;
    const C = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, w = ((R = r.current) == null ? void 0 : R.offsetWidth) ?? 200;
    if (c != null && s != null) {
      let I = s, D = c;
      I + C > window.innerHeight && (I = Math.max(0, window.innerHeight - C)), D + w > window.innerWidth && (D = Math.max(0, window.innerWidth - w)), d({ top: I, left: D }), m(0);
      return;
    }
    if (!o) return;
    const N = document.getElementById(o);
    if (!N) return;
    const x = N.getBoundingClientRect();
    let E = x.bottom + 4, y = x.left;
    E + C > window.innerHeight && (E = x.top - C - 4), y + w > window.innerWidth && (y = x.right - w), d({ top: E, left: y }), m(0);
  }, [a, o, c, s]);
  const g = kt(() => {
    n("close");
  }, [n]), k = kt((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  nn(() => {
    if (!a) return;
    const C = (w) => {
      r.current && !r.current.contains(w.target) && g();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [a, g]);
  const _ = kt((C) => {
    if (C.key === "Escape") {
      C.preventDefault(), g();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), m((w) => (w + 1) % h.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), m((w) => (w - 1 + h.length) % h.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const w = h[f];
      w && k(w.id);
    }
  }, [g, k, h, f]);
  return Wt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: i.top, left: i.left },
      onKeyDown: _
    },
    u.map((C, w) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: w, className: "tlMenu__separator" });
      const x = h.indexOf(C) === f;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => k(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + C.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, Ga = 768, Xa = ({ controlId: l }) => {
  const t = G(), n = ne();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${Ga}px)`), i = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    i(r.matches);
    const d = (f) => i(f.matches);
    return r.addEventListener("change", d), () => r.removeEventListener("change", d);
  }, [n]);
  const a = t.header, o = t.notices, c = t.content, s = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: a })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(q, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: s })), /* @__PURE__ */ e.createElement(q, { control: u }));
}, qa = ({ controlId: l }) => {
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
}, Za = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), Qa = {
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
}, Ja = 300, an = 50, er = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function Nt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, er));
}
const At = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', tr = At + ", button:not([disabled]), a[href]";
function En(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function St(l, t, n = {}) {
  const a = En(l, t);
  if (n.col) {
    const c = a.find((u) => u.dataset.col === n.col), s = c == null ? void 0 : c.querySelector(At);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const c of o) {
    const s = c.querySelector(At);
    if (s) return s;
  }
  return null;
}
const nr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Qa), o = e.useRef(null);
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
  const c = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, i = t.selectionMode ?? "single", d = t.selectedCount ?? 0, f = t.cursorIndex ?? -1, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, k = t.filterBar ?? !1, _ = t.namedFilters ?? [], C = t.activeNamedFilter ?? "", w = t.search ?? "", N = t.filterSaving ?? !1, x = e.useMemo(
    () => c.filter((v) => v.sortPriority && v.sortPriority > 0).length,
    [c]
  ), E = i === "multi", y = 40, b = 20, R = e.useRef(null), I = e.useRef(null), D = e.useRef(null), K = e.useRef(null), U = e.useRef(null), [B, V] = e.useState({}), P = e.useRef(null), A = e.useRef(!1), j = e.useRef(null), [L, S] = e.useState(null), [$, p] = e.useState(null), [M, Y] = e.useState(null), [H, Z] = e.useState(0);
  e.useEffect(() => {
    const v = D.current;
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
    P.current || V({});
  }, [c]);
  const F = e.useCallback((v) => B[v.name] ?? v.width, [B]), te = e.useMemo(() => {
    const v = [];
    let T = E && m > 0 ? y : 0;
    for (let z = 0; z < m && z < c.length; z++)
      v.push(T), T += F(c[z]);
    return v;
  }, [c, m, E, y, F]), se = e.useMemo(() => {
    if (m <= 0)
      return 0;
    let v = E ? y : 0;
    for (let T = 0; T < m && T < c.length; T++)
      v += F(c[T]);
    return v;
  }, [c, m, E, y, F]), le = s * r, pe = e.useRef(null), ye = e.useCallback((v, T, z) => {
    z.preventDefault(), z.stopPropagation(), P.current = { column: v, startX: z.clientX, startWidth: T };
    let Q = z.clientX, ee = 0;
    const re = () => {
      const ce = P.current;
      if (!ce) return;
      const de = Math.max(an, ce.startWidth + (Q - ce.startX) + ee);
      V((_e) => ({ ..._e, [ce.column]: de }));
    }, oe = () => {
      const ce = D.current, de = R.current;
      if (!ce || !P.current) return;
      const _e = ce.getBoundingClientRect(), xe = 40, Kt = 8, Vn = ce.scrollLeft;
      Q > _e.right - xe ? ce.scrollLeft += Kt : Q < _e.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Kt));
      const Yt = ce.scrollLeft - Vn;
      Yt !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += Yt, re()), pe.current = requestAnimationFrame(oe);
    };
    pe.current = requestAnimationFrame(oe);
    const he = (ce) => {
      Q = ce.clientX, re();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", fe), pe.current !== null && (cancelAnimationFrame(pe.current), pe.current = null);
      const de = P.current;
      if (de) {
        const _e = Math.max(an, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: _e }), P.current = null, A.current = !0, requestAnimationFrame(() => {
          A.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", fe);
  }, [n]), we = e.useCallback(() => {
    R.current && D.current && (R.current.scrollLeft = D.current.scrollLeft), K.current !== null && clearTimeout(K.current), K.current = window.setTimeout(() => {
      const v = D.current;
      if (!v) return;
      const T = v.scrollTop, z = Math.ceil(v.clientHeight / r), Q = Math.floor(T / r);
      n("scroll", { start: Q, count: z });
    }, 80);
  }, [n, r]), Te = e.useCallback((v, T, z) => {
    if (A.current) return;
    let Q;
    !T || T === "desc" ? Q = "asc" : Q = "desc";
    const ee = z.shiftKey ? "add" : "replace";
    n("sort", { column: v, direction: Q, mode: ee });
  }, [n]), Re = e.useCallback((v, T) => {
    j.current = v, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", v);
  }, []), Oe = e.useCallback((v, T) => {
    if (!j.current || j.current === v) {
      S(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const z = T.currentTarget.getBoundingClientRect(), Q = T.clientX < z.left + z.width / 2 ? "left" : "right";
    S({ column: v, side: Q });
  }, []), $e = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = j.current;
    if (!T || !L) {
      j.current = null, S(null);
      return;
    }
    let z = c.findIndex((ee) => ee.name === L.column);
    if (z < 0) {
      j.current = null, S(null);
      return;
    }
    const Q = c.findIndex((ee) => ee.name === T);
    L.side === "right" && z++, Q < z && z--, n("columnReorder", { column: T, targetIndex: z }), j.current = null, S(null);
  }, [c, L, n]), O = e.useCallback(() => {
    j.current = null, S(null);
  }, []), X = e.useCallback((v, T) => {
    var ee, re, oe, he;
    const z = window.getSelection();
    if (z && !z.isCollapsed && T.currentTarget.contains(z.anchorNode))
      return;
    if (!Nt(T) && ((ee = D.current) == null || ee.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const fe = (he = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      U.current = { index: v, col: fe ?? void 0 };
    }
    const Q = u.find((fe) => fe.index === v);
    Nt(T) && (Q != null && Q.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: v,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, u]), ae = e.useCallback((v, T, z) => {
    n("moveSelection", { direction: v, extend: T, move: z });
  }, [n]), ie = e.useCallback(() => {
    f < 0 || n("select", { rowIndex: f, ctrlKey: E, shiftKey: !1 });
  }, [n, f, E]), Xe = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Sn = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (f < 0)
      return;
    const v = D.current;
    if (!v)
      return;
    const T = f * r, z = T + r;
    T < v.scrollTop ? v.scrollTop = T : z > v.scrollTop + v.clientHeight && (v.scrollTop = z - v.clientHeight);
  }, [f, r]), e.useEffect(() => {
    const v = U.current, T = D.current;
    if (!v || !T)
      return;
    const z = u.find((re) => re.index === v.index);
    if (!z || !St(T, z.id))
      return;
    U.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !T.contains(Q))
      return;
    const ee = St(T, z.id, { col: v.col, last: v.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const Dn = e.useCallback((v) => {
    if (v.key !== "Tab")
      return;
    const T = D.current, z = document.activeElement;
    if (!T || !z || !T.contains(z))
      return;
    const Q = z.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = u.find((xe) => xe.id === ee);
    if (!re)
      return;
    const oe = En(T, ee).flatMap((xe) => Array.from(xe.querySelectorAll(tr))), he = oe.indexOf(z);
    if (he < 0)
      return;
    const fe = !v.shiftKey;
    if (!(fe ? he === oe.length - 1 : he === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const _e = u.find((xe) => xe.index === de);
    _e && St(T, _e.id) || (v.preventDefault(), U.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), Tn = e.useCallback((v, T) => {
    T.stopPropagation(), n("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Rn = e.useCallback(() => {
    const v = d === s && s > 0;
    n("selectAll", { selected: !v });
  }, [n, d, s]), Ln = e.useCallback((v, T, z) => {
    z.stopPropagation(), n("expand", { rowIndex: v, expanded: T });
  }, [n]), xn = e.useCallback((v, T) => {
    T.preventDefault(), p({ x: T.clientX, y: T.clientY, colIdx: v });
  }, []), In = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), p(null));
  }, [$, n]), Mn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), p(null);
  }, [n]), jn = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = I.current, z = R.current;
    if (!T || !z)
      return;
    const Q = T.clientWidth, ee = [{ x: 0, count: 0 }];
    z.querySelectorAll("[data-col-idx]").forEach((fe) => {
      const ce = fe.getBoundingClientRect().right - T.getBoundingClientRect().left;
      ce > 0 && ce <= Q && ee.push({ x: ce, count: Number(fe.dataset.colIdx) + 1 });
    });
    let re = { x: se, count: m };
    const oe = (fe) => {
      const ce = fe.clientX - T.getBoundingClientRect().left;
      re = ee.reduce(
        (de, _e) => Math.abs(_e.x - ce) < Math.abs(de.x - ce) ? _e : de,
        ee[0]
      ), Y(re);
    }, he = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", he), Y(null), re.count !== m && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", he);
  }, [se, m, n]);
  e.useEffect(() => {
    if (!$) return;
    const v = () => p(null);
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [$]), Fe(!!$, { ESCAPE: () => p(null) });
  const Pn = e.useCallback((v, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: v });
  }, [n]), An = e.useCallback((v) => {
    v.stopPropagation(), v.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Bn, zt] = e.useState(w), Et = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    Et.current || zt(w);
  }, [w]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const at = e.useCallback((v) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), Et.current = !1, n("search", { term: v });
  }, [n]), Fn = e.useCallback((v) => {
    zt(v), Et.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => at(v), Ja);
  }, [at]), On = e.useCallback((v) => {
    v.key === "Enter" && (v.preventDefault(), at(v.currentTarget.value));
  }, [at]), $n = e.useCallback((v) => {
    v === C ? n("clearFilter", {}) : n("applyNamedFilter", { id: v });
  }, [C, n]), Hn = e.useCallback((v, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: v });
  }, [n]), [qe, Ze] = e.useState(null), vt = e.useCallback(() => {
    const v = (qe ?? "").trim();
    v && (n("saveNamedFilter", { filterName: v }), Ze(null));
  }, [qe, n]), Wn = e.useCallback((v) => {
    v.key === "Enter" ? (v.preventDefault(), vt()) : v.key === "Escape" && (v.preventDefault(), Ze(null));
  }, [vt]), _t = c.reduce((v, T) => v + F(T), 0) + (E ? y : 0), Ct = g ? 32 : 0, Un = d === s && s > 0, Vt = d > 0 && d < s, zn = e.useCallback((v) => {
    v && (v.indeterminate = Vt);
  }, [Vt]);
  return /* @__PURE__ */ e.createElement(Ht, { active: Sn }, /* @__PURE__ */ e.createElement(
    Za,
    {
      isMulti: E,
      cursorIndex: f,
      onMove: ae,
      onToggle: ie,
      onSelectAll: Xe
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (v) => {
        if (!j.current) return;
        v.preventDefault();
        const T = D.current, z = R.current;
        if (!T) return;
        const Q = T.getBoundingClientRect(), ee = 40, re = 8;
        v.clientX < Q.left + ee ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : v.clientX > Q.right - ee && (T.scrollLeft += re), z && (z.scrollLeft = T.scrollLeft);
      },
      onDrop: $e
    },
    k && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, _.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, _.map((v) => {
      const T = v.id === C;
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
            onClick: () => $n(v.id)
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
            onClick: (z) => Hn(v.id, z)
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
        value: Bn,
        onChange: (v) => Fn(v.target.value),
        onKeyDown: On
      }
    )), N && (qe === null ? /* @__PURE__ */ e.createElement(
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
        onChange: (v) => Ze(v.target.value),
        onKeyDown: Wn
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !qe.trim(),
        onClick: vt
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
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: I }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: R }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: _t, paddingRight: Ct + H }
      },
      E && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: y,
            minWidth: y,
            ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (v) => {
            j.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== j.current && S({ column: c[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: zn,
            className: "tlTableView__checkbox",
            checked: Un,
            onChange: Rn
          }
        )
      ),
      c.map((v, T) => {
        const z = F(v);
        c.length - 1;
        let Q = "tlTableView__headerCell";
        v.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === v.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = T < m, re = T === m - 1;
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
            onContextMenu: (oe) => xn(T, oe),
            onDragStart: (oe) => Re(v.name, oe),
            onDragOver: (oe) => Oe(v.name, oe),
            onDrop: $e,
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
              onClick: (oe) => Pn(v.name, oe)
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
            if (j.current && c.length > 0) {
              const T = c[c.length - 1];
              T.name !== j.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", S({ column: T.name, side: "right" }));
            }
          },
          onDrop: $e
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (M ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: jn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: An
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: D,
        className: "tlTableView__body",
        onScroll: we,
        onKeyDown: Dn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: le, position: "relative", width: _t, paddingRight: Ct } }, u.map((v) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: "tlTableView__row" + (v.selected ? " tlTableView__row--selected" : "") + (v.index === f ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: v.index * r,
            height: r,
            width: _t,
            paddingRight: Ct,
            ...v.index === f ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !Nt(T) && T.preventDefault();
          },
          onClick: (T) => X(v.index, T)
        },
        E && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: y,
              minWidth: y,
              ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
              onClick: (T) => Tn(v.index, T),
              tabIndex: -1
            }
          )
        ),
        c.map((T, z) => {
          const Q = F(T), ee = z === c.length - 1, re = z < m, oe = z === m - 1;
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
                onClick: (de) => Ln(v.index, !v.expanded, de)
              },
              v.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), v.cells[T.name] && /* @__PURE__ */ e.createElement(q, { control: v.cells[T.name] })) : v.cells[T.name] && /* @__PURE__ */ e.createElement(q, { control: v.cells[T.name] })
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
      $.colIdx + 1 !== m && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: In }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      m > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Mn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, lr = {
  "js.table.columnSearch": "Find column"
}, ar = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(lr), o = t.entries ?? [], c = o.filter((E) => E.visible).length, [s, u] = e.useState(""), r = s.trim().toLowerCase(), i = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), f = e.useRef(null), [m, h] = e.useState(null), g = e.useCallback((E) => {
    f.current = E, h(E);
  }, []), k = e.useCallback((E, y) => {
    n("columnVisible", { column: E, visible: y });
  }, [n]), _ = e.useCallback((E, y) => {
    d.current = E, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", E);
  }, []), C = e.useCallback((E, y) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const b = y.currentTarget.getBoundingClientRect(), R = y.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: R });
  }, [g]), w = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), N = e.useCallback((E) => {
    E.preventDefault();
    const y = d.current, b = f.current;
    if (d.current = null, g(null), !y || !b)
      return;
    const R = o.findIndex((K) => K.name === b.name), I = o.findIndex((K) => K.name === y);
    if (R < 0 || I < 0)
      return;
    let D = b.side === "top" ? R : R + 1;
    I < D && D--, D !== I && n("columnReorder", { column: y, targetIndex: D });
  }, [o, n, g]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: N }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (E) => u(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, i.map((E) => {
    const y = E.visible && c <= 1;
    let b = "tlColumnSelect__row";
    return m && m.name === E.name && (b += " tlColumnSelect__row--dragOver-" + m.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (R) => _(E.name, R),
        onDragOver: (R) => C(E.name, R),
        onDrop: N,
        onDragEnd: w
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: E.visible,
          disabled: y,
          onChange: (R) => k(E.name, R.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: Bt, useRef: nt, useCallback: mt, useMemo: Ae, useEffect: rn } = e, rr = {
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
}, ve = 44, ht = 15, Ce = 6e4, or = 36e5, je = 864e5, sr = 8;
function Ne(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ve(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function cr(l) {
  return Ne(l);
}
function lt(l, t) {
  return Ne(l) === Ne(t);
}
function Me(l) {
  return (l - Ne(l)) / Ce;
}
function Qe(l) {
  return Math.round(l / ht) * ht;
}
function Je(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function bt(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % sr;
}
function gt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function ir(l) {
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
function Be(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function ur(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Be(l, n, t.start) + "–" + Be(l, n, t.end);
}
const dr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], mr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, dr.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function pr(l) {
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
      topMin: Me(s.start),
      botMin: Me(s.start) + Math.max(15, (s.end - s.start) / Ce),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && c(), n;
}
const Dt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ft = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const c = nt(!1), s = (u) => {
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
}, vn = (l) => {
  const [t, n] = Bt(null), a = nt(null);
  a.current = t;
  const o = mt((u) => n(u), []), c = mt(() => n(null), []), s = mt(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: c };
}, fr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: c, dayStartHour: s, dayEndHour: u, now: r, send: i, editable: d, i18n: f } = l, m = Ae(() => {
    const P = n === "DAY" ? 1 : 7, A = [];
    for (let j = 0; j < P; j++) {
      const L = Ve(t, j);
      n === "WORK_WEEK" && c.includes(new Date(L).getDay()) || A.push(L);
    }
    return A;
  }, [t, n, c]), h = vn(i), g = nt(null), k = nt(null), [_, C] = Bt(null), w = nt(null);
  w.current = _;
  const [N, x] = Bt(Date.now());
  rn(() => {
    const P = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const E = mt(
    (P, A) => {
      const j = g.current;
      if (!j)
        return { dayIndex: 0, min: 0 };
      const L = j.getBoundingClientRect(), S = L.width / m.length, $ = Je(Math.floor((P - L.left) / S), 0, m.length - 1), p = A - L.top + j.scrollTop, M = Je(p / ve * 60, 0, 1440);
      return { dayIndex: $, min: M };
    },
    [m.length]
  );
  rn(() => {
    if (!_)
      return;
    const P = (L) => {
      const S = w.current;
      if (!S)
        return;
      const { dayIndex: $, min: p } = E(L.clientX, L.clientY);
      S.mode === "move" ? C({ ...S, dayStart: m[$], startMin: Je(Qe(p - S.grabMin), 0, 1440 - S.dur) }) : S.mode === "resize" ? C({ ...S, endMin: Je(Qe(p), S.startMin + ht, 1440) }) : C({ ...S, toMin: Je(Qe(p), 0, 1440) });
    }, A = () => {
      const L = w.current;
      if (C(null), !!L)
        if (L.mode === "move") {
          const S = L.dayStart + L.startMin * Ce;
          S !== L.origStartMs && i("moveEvent", { eventId: L.id, start: S, end: S + L.dur * Ce });
        } else if (L.mode === "resize") {
          const S = L.dayStart + L.endMin * Ce;
          S !== L.origEndMs && i("resizeEvent", { eventId: L.id, end: S });
        } else {
          const S = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - S >= ht && h.open({ start: L.dayStart + S * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, j = () => C(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", A, { once: !0 }), window.addEventListener("pointercancel", j), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", A), window.removeEventListener("pointercancel", j);
    };
  }, [_, m, E, i, h.open]);
  const y = (P, A, j) => {
    if (!d || !A.movable)
      return;
    P.stopPropagation(), Dt(P), h.discard();
    const { min: L } = E(P.clientX, P.clientY), S = (A.end - A.start) / Ce;
    C({
      mode: "move",
      id: A.id,
      grabMin: L - Me(A.start),
      dur: S,
      dayStart: j,
      startMin: Me(A.start),
      origStartMs: A.start
    });
  }, b = (P, A, j) => {
    !d || !A.resizable || (P.stopPropagation(), Dt(P), h.discard(), C({
      mode: "resize",
      id: A.id,
      dayStart: j,
      startMin: Me(A.start),
      endMin: Me(A.end),
      origEndMs: A.end
    }));
  }, R = (P, A) => {
    if (!d || P.button !== 0)
      return;
    Dt(P), h.discard();
    const { min: j } = E(P.clientX, P.clientY);
    C({ mode: "create", dayStart: A, fromMin: Qe(j), toMin: Qe(j) });
  }, I = Array.from({ length: 24 }, (P, A) => A), D = Ae(() => {
    if (_ === null || !("id" in _))
      return a;
    const P = _;
    return a.map((A) => {
      if (A.id !== P.id)
        return A;
      if (P.mode === "move") {
        const j = P.dayStart + P.startMin * Ce;
        return { ...A, start: j, end: j + P.dur * Ce };
      }
      return { ...A, end: P.dayStart + P.endMin * Ce };
    });
  }, [a, _]), K = Ae(() => m.map(
    (P) => pr(
      D.filter((A) => !A.allDay && A.start < P + je && A.end > P)
    )
  ), [m, D]), U = Ae(() => m.map((P) => D.filter((A) => A.allDay && A.start < P + je && A.end > P)), [m, D]), B = s * ve, V = u * ve;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), m.map((P) => {
    const A = c.includes(new Date(P).getDay()), j = lt(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (A ? " tlCalDayHead--nonworking" : "") + (j ? " tlCalDayHead--today" : ""),
        onClick: () => i("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Be(o, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, f["js.calendar.allDay"]), m.map((P, A) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: P,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: P, end: P + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === P && /* @__PURE__ */ e.createElement(
      Ft,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: f["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    U[A].map((j) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: j.id,
        className: "tlCalAllDayEvent " + bt(j.category) + (j.selected ? " tlCalEvent--selected" : ""),
        style: gt(j),
        title: j.tooltip,
        onClick: (L) => {
          L.stopPropagation(), i("selectEvent", { eventId: j.id });
        }
      },
      j.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: k }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * ve } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, I.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * ve } }, P === 0 ? "" : Be(o, { hour: "numeric" }, Ne(t) + P * or)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${m.length}, 1fr)` } }, m.map((P, A) => {
    const j = c.includes(new Date(P).getDay()), L = _ && ("dayStart" in _ && _.dayStart === P) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (j ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => R(S, P)
      },
      I.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * ve } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: B, height: V - B } }),
      lt(P, N) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Me(Date.now()) / 60 * ve } }),
      K[A].map((S) => {
        const $ = _ !== null && "id" in _ && _.id === S.ev.id, p = S.topMin / 60 * ve, M = (S.botMin - S.topMin) / 60 * ve, Y = 100 / S.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.ev.id,
            className: "tlCalEvent " + bt(S.ev.category) + (S.ev.selected ? " tlCalEvent--selected" : "") + ($ ? " tlCalEvent--dragging" : ""),
            style: gt(S.ev, {
              top: p,
              height: M,
              left: `${S.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: S.ev.tooltip,
            onPointerDown: (H) => y(H, S.ev, P),
            onClick: (H) => {
              H.stopPropagation(), i("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, ur(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (H) => b(H, S.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Ne(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Ft,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: f["js.calendar.newEventTitle"],
          style: {
            top: Me(h.pending.start) / 60 * ve,
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
}, hr = 3, br = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: c, send: s, editable: u, now: r, i18n: i } = l, d = vn(s), f = Ae(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const k = [];
      for (let _ = 0; _ < 7; _++)
        k.push(Ve(t, g * 7 + _));
      h.push(k);
    }
    return h;
  }, [t]), m = (h, g) => {
    h.preventDefault();
    const k = h.dataTransfer.getData("text/plain"), _ = a.find((w) => w.id === k);
    if (!_ || !u || !_.movable)
      return;
    const C = g - Ne(_.start);
    s("moveEvent", { eventId: k, start: _.start + C, end: _.end + C });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, f[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Be(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, f.map((h, g) => {
    const k = h[0], _ = Ve(k, 7), C = a.filter((N) => (N.allDay || N.end - N.start >= je) && N.start < _ && N.end > k).sort((N, x) => N.start - x.start).slice(0, 3), w = C.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((N) => {
      const x = new Date(N).getMonth() === new Date(n).getMonth(), E = c.includes(new Date(N).getDay()), y = lt(N, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => m(b, N),
          onClick: () => u && d.open({ start: N, end: N + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (y ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: N, granularity: "DAY" });
            }
          },
          new Date(N).getDate()
        ),
        d.pending && d.pending.start === N && /* @__PURE__ */ e.createElement(
          Ft,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: i["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, C.map((N, x) => {
      const E = Math.max(0, Math.floor((Ne(Math.max(N.start, k)) - k) / je)), y = Math.min(7, Math.ceil((N.end - k) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlCalMonthBar " + bt(N.category) + (N.selected ? " tlCalEvent--selected" : ""),
          style: gt(N, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, y) + 1}`,
            gridRow: x + 1
          }),
          draggable: u && N.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", N.id),
          title: N.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: N.id });
          }
        },
        N.title
      );
    }), h.map((N, x) => {
      const E = a.filter((R) => !R.allDay && R.end - R.start < je && lt(R.start, N)).sort((R, I) => R.start - I.start), y = E.slice(0, hr), b = E.length - y.length;
      return y.map((R, I) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.id,
          className: "tlCalChip " + bt(R.category) + (R.selected ? " tlCalEvent--selected" : ""),
          style: gt(R, { gridColumn: x + 1, gridRow: w + 1 + I }),
          draggable: u && R.movable,
          onDragStart: (D) => D.dataTransfer.setData("text/plain", R.id),
          title: R.tooltip,
          onClick: (D) => {
            D.stopPropagation(), s("selectEvent", { eventId: R.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Be(o, { hour: "numeric", minute: "2-digit" }, R.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, R.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + N,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: w + 1 + y.length },
              onClick: () => s("goto", { date: N, granularity: "DAY" })
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
}, gr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: c, send: s, now: u } = l, r = Ae(() => {
    const m = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Ne(h.start);
      const k = h.end;
      for (; g < k; )
        m.add(g), g = Ve(g, 1);
    }
    return m;
  }, [n]), i = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (m, h) => new Date(i, h, 1).getTime()), f = Ae(() => {
    const m = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const k = new Date(m);
      return k.setDate(m.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(k);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((m) => {
    const h = new Date(m), g = Ne(Ve(m, -((h.getDay() - o + 7) % 7))), k = Array.from({ length: 42 }, (_, C) => Ve(g, C));
    return /* @__PURE__ */ e.createElement("div", { key: m, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: m, granularity: "MONTH" })
      },
      Be(a, { month: "long" }, m)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, f.map((_, C) => /* @__PURE__ */ e.createElement("div", { key: "h" + C, className: "tlCalMiniWd" }, _)), k.map((_) => {
      const C = new Date(_).getMonth() === h.getMonth(), w = c.includes(new Date(_).getDay()), N = lt(_, u), x = r.has(cr(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (C ? "" : " tlCalMiniDay--other") + (w ? " tlCalMiniDay--nonworking" : "") + (N ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, Er = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(rr), o = t.granularity ?? "WEEK", c = t.rangeStart ?? Date.now(), s = t.anchor ?? c, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: ir(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(mr, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(br, { ctx: r, rangeStart: c, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(gr, { ctx: r, rangeStart: c }) : /* @__PURE__ */ e.createElement(fr, { ctx: r, rangeStart: c, granularity: o })));
}, vr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, _n = e.createContext(vr), { useMemo: _r, useRef: Cr, useState: yr, useEffect: wr } = e, kr = 320, Nr = "TLTableView", Sr = "TLPanel", Dr = ({ controlId: l }) => {
  var _;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, c = t.children ?? [], s = t.noModelMessage, u = Cr(null), [r, i] = yr(
    a === "top" ? "top" : "side"
  );
  wr(() => {
    if (a !== "auto") {
      i(a);
      return;
    }
    const C = u.current;
    if (!C) return;
    const w = new ResizeObserver((N) => {
      for (const x of N) {
        const y = x.contentRect.width / n;
        i(y < kr ? "top" : "side");
      }
    });
    return w.observe(C), () => w.disconnect();
  }, [a, n]);
  const d = _r(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), m = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = c.length === 1 ? c[0] : void 0, g = !!h && (h.module === Nr || h.module === Sr && ((_ = h.state) == null ? void 0 : _.bare) === !0), k = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(_n.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: m, ref: u }, c.map((C, w) => /* @__PURE__ */ e.createElement(q, { key: w, control: C }))));
}, { useCallback: Tr } = e, Rr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Lr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Rr), o = t.headerControl ?? null, c = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", i = t.fullLine === !0, d = t.children ?? [], f = o != null || c.length > 0 || s, m = Tr(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(q, { control: o })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((g, k) => /* @__PURE__ */ e.createElement(q, { key: k, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, k) => /* @__PURE__ */ e.createElement(q, { key: k, control: g }))));
}, { useContext: xr, useState: Ir, useCallback: Mr } = e, jr = ({ controlId: l }) => {
  const t = G(), n = xr(_n), a = t.label ?? "", o = t.required === !0, c = t.error, s = t.errorIcon, u = t.warnings, r = t.warningIcon, i = t.helpText, d = t.dirty === !0, f = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, k = t.field, _ = n.readOnly, [C, w] = Ir(!1), N = Mr(() => w((R) => !R), []), x = f === "hidden", E = c != null, y = u != null && u.length > 0, b = [
    "tlFormField",
    `tlFormField--${f}`,
    _ ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && y ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), i && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: N,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: k })), !_ && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(pt, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, c)), !_ && !E && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((R, I) => /* @__PURE__ */ e.createElement("div", { key: I, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(pt, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, R)))), !_ && i && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, Pr = ({ controlId: l }) => {
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
}, Ar = 20, Br = () => {
  var y;
  const l = G(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, i] = e.useState(-1), d = e.useRef(null), f = ((y = n.find((b) => b.selected)) == null ? void 0 : y.id) ?? null;
  e.useEffect(() => {
    var R;
    if (f == null)
      return;
    const b = (R = d.current) == null ? void 0 : R.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [f]);
  const m = e.useCallback((b, R) => {
    t(R ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, R) => {
    var D;
    const I = window.getSelection();
    I && !I.isCollapsed && R.currentTarget.contains(I.anchorNode) || ((D = d.current) == null || D.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    }));
  }, [t]), g = e.useCallback((b, R) => {
    R.preventDefault(), t("contextMenu", { nodeId: b, x: R.clientX, y: R.clientY });
  }, [t]), k = e.useRef(null), _ = e.useCallback((b, R) => {
    const I = R.getBoundingClientRect(), D = b.clientY - I.top, K = I.height / 3;
    return D < K ? "above" : D > K * 2 ? "below" : "within";
  }, []), C = e.useCallback((b, R) => {
    R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", b);
  }, []), w = e.useCallback((b, R) => {
    R.preventDefault(), R.dataTransfer.dropEffect = "move";
    const I = _(R, R.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: I }), k.current = null;
    }, 50);
  }, [t, _]), N = e.useCallback((b, R) => {
    R.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const I = _(R, R.currentTarget);
    t("drop", { nodeId: b, position: I });
  }, [t, _]), x = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
  }, [t]), E = e.useCallback((b) => {
    if (n.length === 0) return;
    let R = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), R = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), R = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const I = n[r];
          if (I.expandable && !I.expanded) {
            t("expand", { nodeId: I.id });
            return;
          } else I.expanded && (R = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const I = n[r];
          if (I.expanded) {
            t("collapse", { nodeId: I.id });
            return;
          } else {
            const D = I.depth;
            for (let K = r - 1; K >= 0; K--)
              if (n[K].depth < D) {
                R = K;
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
        b.preventDefault(), R = 0;
        break;
      case "End":
        b.preventDefault(), R = n.length - 1;
        break;
      default:
        return;
    }
    R !== r && i(R);
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
    n.map((b, R) => /* @__PURE__ */ e.createElement(
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
          R === r ? "tlTreeView__node--focused" : "",
          s === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * Ar },
        draggable: o,
        onMouseDown: (I) => {
          (I.shiftKey || I.ctrlKey || I.metaKey || I.detail > 1) && I.preventDefault();
        },
        onClick: (I) => h(b.id, I),
        onContextMenu: (I) => g(b.id, I),
        onDragStart: (I) => C(b.id, I),
        onDragOver: c ? (I) => w(b.id, I) : void 0,
        onDrop: c ? (I) => N(b.id, I) : void 0,
        onDragEnd: x
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (I) => {
            I.stopPropagation(), m(b.id, b.expanded);
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
var Tt = { exports: {} }, be = {}, Rt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var on;
function Fr() {
  if (on) return J;
  on = 1;
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
  }, k = Object.assign, _ = {};
  function C(p, M, Y) {
    this.props = p, this.context = M, this.refs = _, this.updater = Y || g;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(p, M) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, M, "setState");
  }, C.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function w() {
  }
  w.prototype = C.prototype;
  function N(p, M, Y) {
    this.props = p, this.context = M, this.refs = _, this.updater = Y || g;
  }
  var x = N.prototype = new w();
  x.constructor = N, k(x, C.prototype), x.isPureReactComponent = !0;
  var E = Array.isArray;
  function y() {
  }
  var b = { H: null, A: null, T: null, S: null }, R = Object.prototype.hasOwnProperty;
  function I(p, M, Y) {
    var H = Y.ref;
    return {
      $$typeof: l,
      type: p,
      key: M,
      ref: H !== void 0 ? H : null,
      props: Y
    };
  }
  function D(p, M) {
    return I(p.type, M, p.props);
  }
  function K(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function U(p) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var B = /\/+/g;
  function V(p, M) {
    return typeof p == "object" && p !== null && p.key != null ? U("" + p.key) : M.toString(36);
  }
  function P(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(y, y) : (p.status = "pending", p.then(
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
  function A(p, M, Y, H, Z) {
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
              return te = p._init, A(
                te(p._payload),
                M,
                Y,
                H,
                Z
              );
          }
      }
    if (te)
      return Z = Z(p), te = H === "" ? "." + V(p, 0) : H, E(Z) ? (Y = "", te != null && (Y = te.replace(B, "$&/") + "/"), A(Z, M, Y, "", function(pe) {
        return pe;
      })) : Z != null && (K(Z) && (Z = D(
        Z,
        Y + (Z.key == null || p && p.key === Z.key ? "" : ("" + Z.key).replace(
          B,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var se = H === "" ? "." : H + ":";
    if (E(p))
      for (var le = 0; le < p.length; le++)
        H = p[le], F = se + V(H, le), te += A(
          H,
          M,
          Y,
          F,
          Z
        );
    else if (le = h(p), typeof le == "function")
      for (p = le.call(p), le = 0; !(H = p.next()).done; )
        H = H.value, F = se + V(H, le++), te += A(
          H,
          M,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof p.then == "function")
        return A(
          P(p),
          M,
          Y,
          H,
          Z
        );
      throw M = String(p), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function j(p, M, Y) {
    if (p == null) return p;
    var H = [], Z = 0;
    return A(p, H, "", "", function(F) {
      return M.call(Y, F, Z++);
    }), H;
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
  }, $ = {
    map: j,
    forEach: function(p, M, Y) {
      j(
        p,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(p) {
      var M = 0;
      return j(p, function() {
        M++;
      }), M;
    },
    toArray: function(p) {
      return j(p, function(M) {
        return M;
      }) || [];
    },
    only: function(p) {
      if (!K(p))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return p;
    }
  };
  return J.Activity = f, J.Children = $, J.Component = C, J.Fragment = n, J.Profiler = o, J.PureComponent = N, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
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
  }, J.cloneElement = function(p, M, Y) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var H = k({}, p.props), Z = p.key;
    if (M != null)
      for (F in M.key !== void 0 && (Z = "" + M.key), M)
        !R.call(M, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && M.ref === void 0 || (H[F] = M[F]);
    var F = arguments.length - 2;
    if (F === 1) H.children = Y;
    else if (1 < F) {
      for (var te = Array(F), se = 0; se < F; se++)
        te[se] = arguments[se + 2];
      H.children = te;
    }
    return I(p.type, Z, H);
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
  }, J.createElement = function(p, M, Y) {
    var H, Z = {}, F = null;
    if (M != null)
      for (H in M.key !== void 0 && (F = "" + M.key), M)
        R.call(M, H) && H !== "key" && H !== "__self" && H !== "__source" && (Z[H] = M[H]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = Y;
    else if (1 < te) {
      for (var se = Array(te), le = 0; le < te; le++)
        se[le] = arguments[le + 2];
      Z.children = se;
    }
    if (p && p.defaultProps)
      for (H in te = p.defaultProps, te)
        Z[H] === void 0 && (Z[H] = te[H]);
    return I(p, F, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(p) {
    return { $$typeof: u, render: p };
  }, J.isValidElement = K, J.lazy = function(p) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: p },
      _init: L
    };
  }, J.memo = function(p, M) {
    return {
      $$typeof: i,
      type: p,
      compare: M === void 0 ? null : M
    };
  }, J.startTransition = function(p) {
    var M = b.T, Y = {};
    b.T = Y;
    try {
      var H = p(), Z = b.S;
      Z !== null && Z(Y, H), typeof H == "object" && H !== null && typeof H.then == "function" && H.then(y, S);
    } catch (F) {
      S(F);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), b.T = M;
    }
  }, J.unstable_useCacheRefresh = function() {
    return b.H.useCacheRefresh();
  }, J.use = function(p) {
    return b.H.use(p);
  }, J.useActionState = function(p, M, Y) {
    return b.H.useActionState(p, M, Y);
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
  }, J.useImperativeHandle = function(p, M, Y) {
    return b.H.useImperativeHandle(p, M, Y);
  }, J.useInsertionEffect = function(p, M) {
    return b.H.useInsertionEffect(p, M);
  }, J.useLayoutEffect = function(p, M) {
    return b.H.useLayoutEffect(p, M);
  }, J.useMemo = function(p, M) {
    return b.H.useMemo(p, M);
  }, J.useOptimistic = function(p, M) {
    return b.H.useOptimistic(p, M);
  }, J.useReducer = function(p, M, Y) {
    return b.H.useReducer(p, M, Y);
  }, J.useRef = function(p) {
    return b.H.useRef(p);
  }, J.useState = function(p) {
    return b.H.useState(p);
  }, J.useSyncExternalStore = function(p, M, Y) {
    return b.H.useSyncExternalStore(
      p,
      M,
      Y
    );
  }, J.useTransition = function() {
    return b.H.useTransition();
  }, J.version = "19.2.4", J;
}
var sn;
function Or() {
  return sn || (sn = 1, Rt.exports = Fr()), Rt.exports;
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
var cn;
function $r() {
  if (cn) return be;
  cn = 1;
  var l = Or();
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
  return be.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, be.createPortal = function(r, i) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!i || i.nodeType !== 1 && i.nodeType !== 9 && i.nodeType !== 11)
      throw Error(t(299));
    return c(r, i, null, d);
  }, be.flushSync = function(r) {
    var i = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, r) return r();
    } finally {
      s.T = i, a.p = d, a.d.f();
    }
  }, be.preconnect = function(r, i) {
    typeof r == "string" && (i ? (i = i.crossOrigin, i = typeof i == "string" ? i === "use-credentials" ? i : "" : void 0) : i = null, a.d.C(r, i));
  }, be.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, be.preinit = function(r, i) {
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
  }, be.preinitModule = function(r, i) {
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
  }, be.preload = function(r, i) {
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
  }, be.preloadModule = function(r, i) {
    if (typeof r == "string")
      if (i) {
        var d = u(i.as, i.crossOrigin);
        a.d.m(r, {
          as: typeof i.as == "string" && i.as !== "script" ? i.as : void 0,
          crossOrigin: d,
          integrity: typeof i.integrity == "string" ? i.integrity : void 0
        });
      } else a.d.m(r);
  }, be.requestFormReset = function(r) {
    a.d.r(r);
  }, be.unstable_batchedUpdates = function(r, i) {
    return r(i);
  }, be.useFormState = function(r, i, d) {
    return s.H.useFormState(r, i, d);
  }, be.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, be.version = "19.2.4", be;
}
var un;
function Hr() {
  if (un) return Tt.exports;
  un = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Tt.exports = $r(), Tt.exports;
}
var Cn = Hr();
const { useState: Ie, useCallback: ge, useRef: et, useEffect: We, useMemo: Ot } = e;
function Ut({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(pt, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Wr({
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
  const d = ge(
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
    /* @__PURE__ */ e.createElement(Ut, { image: l.image }),
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
function Ur({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: c
}) {
  const s = ge(() => a(l.value), [a, l.value]), u = Ot(() => {
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
    /* @__PURE__ */ e.createElement(Ut, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const zr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, c = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, i = t.optionsLoaded === !0, d = t.options ?? [], f = t.emptyOptionLabel ?? "", m = c && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], k = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, C] = Ie(!1), [w, N] = Ie(""), [x, E] = Ie(-1), [y, b] = Ie(!1), [R, I] = Ie({}), [D, K] = Ie(null), [U, B] = Ie(null), [V, P] = Ie(null), A = et(null), j = et(null), L = et(null), S = et(a);
  S.current = a;
  const $ = et(-1), p = Ot(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = Ot(() => {
    let O = d.filter((X) => !p.has(X.value));
    if (w) {
      const X = w.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(X));
    }
    return O;
  }, [d, p, w]);
  We(() => {
    w && M.length === 1 ? E(0) : E(-1);
  }, [M.length, w]), We(() => {
    _ && i && j.current && j.current.focus();
  }, [_, i, a]), We(() => {
    var ae, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const X = (ae = A.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    X && X.length > 0 ? X[Math.min(O, X.length - 1)].focus() : (ie = A.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!_) return;
    const O = (X) => {
      A.current && !A.current.contains(X.target) && L.current && !L.current.contains(X.target) && (C(!1), N(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), We(() => {
    if (!_ || !A.current) return;
    const O = A.current.getBoundingClientRect(), X = window.innerHeight - O.bottom, ie = X < 300 && O.top > X;
    I({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = ge(async () => {
    if (!(u || !r) && (C(!0), N(""), E(-1), b(!1), !i))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [u, r, i, n]), H = ge(() => {
    var O;
    C(!1), N(""), E(-1), (O = A.current) == null || O.focus();
  }, []), Z = ge(
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
      S.current = X, n(rt, { value: X.map((ae) => ae.value) }), o ? (N(""), E(-1)) : H();
    },
    [o, d, n, H]
  ), F = ge(
    (O) => {
      $.current = S.current.findIndex((ae) => ae.value === O);
      const X = S.current.filter((ae) => ae.value !== O);
      S.current = X, n(rt, { value: X.map((ae) => ae.value) });
    },
    [n]
  ), te = ge(
    (O) => {
      O.stopPropagation(), n(rt, { value: [] }), H();
    },
    [n, H]
  ), se = ge((O) => {
    N(O.target.value);
  }, []), le = ge(
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
          O.preventDefault(), O.stopPropagation(), x >= 0 && x < M.length && Z(M[x].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), H();
          break;
        case "Tab":
          H();
          break;
        case "Backspace":
          w === "" && o && a.length > 0 && F(a[a.length - 1].value);
          break;
      }
    },
    [
      _,
      Y,
      H,
      M,
      x,
      Z,
      w,
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
    (O, X) => {
      K(O), X.dataTransfer.effectAllowed = "move", X.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), we = ge(
    (O, X) => {
      if (X.preventDefault(), X.dataTransfer.dropEffect = "move", D === null || D === O) {
        B(null), P(null);
        return;
      }
      const ae = X.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Xe = X.clientX < ie ? "before" : "after";
      B(O), P(Xe);
    },
    [D]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), D === null || U === null || V === null || D === U) return;
      const X = [...S.current], [ae] = X.splice(D, 1);
      let ie = U;
      D < U ? ie = V === "before" ? ie - 1 : ie : ie = V === "before" ? ie : ie + 1, X.splice(ie, 0, ae), S.current = X, n(rt, { value: X.map((Xe) => Xe.value) }), K(null), B(null), P(null);
    },
    [D, U, V, n]
  ), Re = ge(() => {
    K(null), B(null), P(null);
  }, []);
  if (We(() => {
    if (x < 0 || !L.current) return;
    const O = L.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ut, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const Oe = !s && a.length > 0 && !u, $e = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: R,
      ...Yn
    },
    (i || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: j,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
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
      !i && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, h["js.dropdownSelect.error"])),
      i && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      i && M.map((O, X) => /* @__PURE__ */ e.createElement(
        Ur,
        {
          key: O.value,
          id: `${l}-opt-${X}`,
          option: O,
          highlighted: X === x,
          searchTerm: w,
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
      ref: A,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: _ ? void 0 : Y,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, f) : a.map((O, X) => {
      let ae = "";
      return D === X ? ae = "tlDropdownSelect__chip--dragging" : U === X && V === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : U === X && V === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Wr,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !s),
          onRemove: F,
          removeLabel: k(O.label),
          draggable: m,
          onDragStart: m ? (ie) => ye(X, ie) : void 0,
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), $e && Cn.createPortal($e, document.body));
}, { useCallback: Lt, useRef: Vr } = e, yn = "application/x-tl-color", Kr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: c
}) => {
  const s = Vr(null), u = Lt(
    (d) => (f) => {
      s.current = d, f.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Lt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), i = Lt(
    (d) => (f) => {
      f.preventDefault();
      const m = f.dataTransfer.getData(yn);
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
function wn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function $t(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function kn(l) {
  if (!$t(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Nn(l, t, n) {
  const a = (o) => wn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Yr(l, t, n) {
  const a = l / 255, o = t / 255, c = n / 255, s = Math.max(a, o, c), u = Math.min(a, o, c), r = s - u;
  let i = 0;
  r !== 0 && (s === a ? i = (o - c) / r % 6 : s === o ? i = (c - a) / r + 2 : i = (a - o) / r + 4, i *= 60, i < 0 && (i += 360));
  const d = s === 0 ? 0 : r / s;
  return [i, d, s];
}
function Gr(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let s = 0, u = 0, r = 0;
  return l < 60 ? (s = a, u = o, r = 0) : l < 120 ? (s = o, u = a, r = 0) : l < 180 ? (s = 0, u = a, r = o) : l < 240 ? (s = 0, u = o, r = a) : l < 300 ? (s = o, u = 0, r = a) : (s = a, u = 0, r = o), [
    Math.round((s + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function Xr(l) {
  return Yr(...kn(l));
}
function xt(l, t, n) {
  return Nn(...Gr(l, t, n));
}
const { useCallback: Ue, useRef: dn } = e, qr = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = Xr(l), c = dn(null), s = dn(null), u = Ue(
    (g, k) => {
      var N;
      const _ = (N = c.current) == null ? void 0 : N.getBoundingClientRect();
      if (!_) return;
      const C = Math.max(0, Math.min(1, (g - _.left) / _.width)), w = Math.max(0, Math.min(1, 1 - (k - _.top) / _.height));
      t(xt(n, C, w));
    },
    [n, t]
  ), r = Ue(
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
      var w;
      const k = (w = s.current) == null ? void 0 : w.getBoundingClientRect();
      if (!k) return;
      const C = Math.max(0, Math.min(1, (g - k.top) / k.height)) * 360;
      t(xt(C, a, o));
    },
    [a, o, t]
  ), f = Ue(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), m = Ue(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = xt(n, 1, 1);
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
function Zr(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Qr = {
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
}, { useState: ct, useCallback: ke, useEffect: mn, useRef: Jr, useLayoutEffect: eo } = e, to = ({
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
  const [i, d] = ct("palette"), [f, m] = ct(t), h = Jr(null), g = ue(Qr), [k, _] = ct(null);
  eo(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), S = h.current.getBoundingClientRect();
    let $ = L.bottom + 4, p = L.left;
    $ + S.height > window.innerHeight && ($ = L.top - S.height - 4), p + S.width > window.innerWidth && (p = Math.max(0, L.right - S.width)), _({ top: $, left: p });
  }, [l]);
  const C = f != null, [w, N, x] = C ? kn(f) : [0, 0, 0], [E, y] = ct((f == null ? void 0 : f.toUpperCase()) ?? "");
  mn(() => {
    y((f == null ? void 0 : f.toUpperCase()) ?? "");
  }, [f]), Fe(!0, { ESCAPE: u }), mn(() => {
    const L = ($) => {
      h.current && !h.current.contains($.target) && u();
    }, S = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const b = ke(
    (L) => (S) => {
      const $ = parseInt(S.target.value, 10);
      if (isNaN($)) return;
      const p = wn($);
      m(Nn(L === "r" ? p : w, L === "g" ? p : N, L === "b" ? p : x));
    },
    [w, N, x]
  ), R = ke(
    (L) => {
      if (f != null) {
        L.dataTransfer.setData(yn, f.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const S = document.createElement("div");
        S.style.width = "33px", S.style.height = "33px", S.style.backgroundColor = f, S.style.borderRadius = "3px", S.style.border = "1px solid rgba(0,0,0,0.1)", S.style.position = "absolute", S.style.top = "-9999px", document.body.appendChild(S), L.dataTransfer.setDragImage(S, 16, 16), requestAnimationFrame(() => document.body.removeChild(S));
      }
    },
    [f]
  ), I = ke((L) => {
    const S = L.target.value;
    y(S), $t(S) && m(S);
  }, []), D = ke(() => {
    m(null);
  }, []), K = ke((L) => {
    m(L);
  }, []), U = ke(
    (L) => {
      s(L);
    },
    [s]
  ), B = ke(
    (L, S) => {
      const $ = [...n], p = $[L];
      $[L] = $[S], $[S] = p, r($);
    },
    [n, r]
  ), V = ke(
    (L, S) => {
      const $ = [...n];
      $[L] = S, r($);
    },
    [n, r]
  ), P = ke(() => {
    r([...o]);
  }, [o, r]), A = ke(
    (L) => {
      if (Zr(n, L)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const $ = [...n];
      $[S] = L.toUpperCase(), r($);
    },
    [n, r]
  ), j = ke(() => {
    f != null && A(f), s(f);
  }, [f, s, A]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: k ? { top: k.top, left: k.left, visibility: "visible" } : { visibility: "hidden" }
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
      Kr,
      {
        colors: n,
        columns: a,
        onSelect: K,
        onConfirm: U,
        onSwap: B,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(qr, { color: f ?? "#000000", onColorChange: m }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: f } : void 0,
        draggable: C,
        onDragStart: C ? R : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? w : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? N : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? x : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (E !== "" && !$t(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: I
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: D }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: j }, g["js.colorInput.ok"]))
  );
}, no = { "js.colorInput.chooseColor": "Choose color" }, { useState: lo, useCallback: it, useRef: ao } = e, ro = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), c = ue(no), [s, u] = lo(!1), r = ao(null), i = n, d = t.editable !== !1, f = t.palette ?? [], m = t.paletteColumns ?? 6, h = t.defaultPalette ?? f, g = it(() => {
    d && u(!0);
  }, [d]), k = it(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = it(() => {
    u(!1);
  }, []), C = it(
    (w) => {
      o("paletteChanged", { palette: w });
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
    to,
    {
      anchorRef: r,
      currentColor: i,
      palette: f,
      paletteColumns: m,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: k,
      onCancel: _,
      onPaletteChange: C
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
}, { useState: tt, useCallback: Pe, useEffect: It, useRef: pn, useLayoutEffect: oo, useMemo: so } = e, co = {
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
}, io = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: c,
  onLoadIcons: s
}) => {
  const u = ue(co), [r, i] = tt("simple"), [d, f] = tt(""), [m, h] = tt(t ?? ""), [g, k] = tt(!1), [_, C] = tt(null), w = pn(null), N = pn(null);
  oo(() => {
    if (!l.current || !w.current) return;
    const U = l.current.getBoundingClientRect(), B = w.current.getBoundingClientRect();
    let V = U.bottom + 4, P = U.left;
    V + B.height > window.innerHeight && (V = U.top - B.height - 4), P + B.width > window.innerWidth && (P = Math.max(0, U.right - B.width)), C({ top: V, left: P });
  }, [l]), It(() => {
    !a && !g && s().catch(() => k(!0));
  }, [a, g, s]), It(() => {
    a && N.current && N.current.focus();
  }, [a]), Fe(!0, { ESCAPE: c }), It(() => {
    const U = (V) => {
      w.current && !w.current.contains(V.target) && c();
    }, B = setTimeout(() => document.addEventListener("mousedown", U), 0);
    return () => {
      clearTimeout(B), document.removeEventListener("mousedown", U);
    };
  }, [c]);
  const x = so(() => {
    if (!d) return n;
    const U = d.toLowerCase();
    return n.filter(
      (B) => B.prefix.toLowerCase().includes(U) || B.label.toLowerCase().includes(U) || B.terms != null && B.terms.some((V) => V.includes(U))
    );
  }, [n, d]), E = Pe((U) => {
    f(U.target.value);
  }, []), y = Pe(
    (U) => {
      o(U);
    },
    [o]
  ), b = Pe((U) => {
    h(U);
  }, []), R = Pe((U) => {
    h(U.target.value);
  }, []), I = Pe(() => {
    o(m || null);
  }, [m, o]), D = Pe(() => {
    o(null);
  }, [o]), K = Pe(async (U) => {
    U.preventDefault(), k(!1);
    try {
      await s();
    } catch {
      k(!0);
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
        ref: N,
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && x.map(
        (U) => U.variants.map((B) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: B.encoded,
            className: "tlIconSelect__iconCell" + (B.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": B.encoded === t,
            tabIndex: 0,
            title: U.label,
            onClick: () => r === "simple" ? y(B.encoded) : b(B.encoded),
            onKeyDown: (V) => {
              (V.key === "Enter" || V.key === " ") && (V.preventDefault(), r === "simple" ? y(B.encoded) : b(B.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: B.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: m,
        onChange: R
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, m && /* @__PURE__ */ e.createElement(Se, { encoded: m })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, m ? m.startsWith("css:") ? m.substring(4) : m : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: D }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: I }, u["js.iconSelect.ok"]))
  );
}, uo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: mo, useCallback: ut, useRef: po } = e, fo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), c = ue(uo), [s, u] = mo(!1), r = po(null), i = n, d = t.editable !== !1, f = t.disabled === !0, m = t.icons ?? [], h = t.iconsLoaded === !0, g = ut(() => {
    d && !f && u(!0);
  }, [d, f]), k = ut(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = ut(() => {
    u(!1);
  }, []), C = ut(async () => {
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
    io,
    {
      anchorRef: r,
      currentValue: i,
      icons: m,
      iconsLoaded: h,
      onSelect: k,
      onCancel: _,
      onLoadIcons: C
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(Se, { encoded: i }) : null));
}, { useCallback: ze, useEffect: ho, useMemo: fn, useRef: bo, useState: Mt } = e, go = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Eo = [1, 2, 3, 4];
function vo(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function _o(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Eo)
    n >= o && (a = o);
  return a;
}
function Co(l, t) {
  const n = go[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function yo(l, t) {
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
        for (let k = m.colEnd; k < h; k++) c(g, k);
      m.colEnd = h;
    }
  };
  for (const f of l) {
    const m = n <= 1 ? 1 : Math.max(1, f.rowSpan || 1);
    let h = Math.min(Co(f.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let g = 0;
    for (let N = r; N < n && !o(u, N); N++)
      g++;
    if (h > g) {
      for (i(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      g = 0;
      for (let N = r; N < n && !o(u, N); N++)
        g++;
      h = Math.min(h, g);
    }
    const k = r, _ = r + h, C = u, w = u + m;
    s.push({ id: f.id, colStart: k, colEnd: _, rowStart: C, rowEnd: w });
    for (let N = C; N < w; N++)
      for (let x = k; x < _; x++) c(N, x);
    r = _, r >= n && (r = 0, u++);
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
const wo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((y) => y && y.id), c = bo(null), [s, u] = Mt(1), r = t.editMode === !0;
  ho(() => {
    const y = c.current;
    if (!y) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, R = vo(a, b), I = () => u(_o(y.clientWidth, R));
    I();
    const D = new ResizeObserver(I);
    return D.observe(y), () => D.disconnect();
  }, [a]);
  const i = fn(() => yo(o, s), [o, s]), d = fn(() => {
    const y = {};
    for (const b of i) y[b.id] = b;
    return y;
  }, [i]), [f, m] = Mt(null), [h, g] = Mt(null), k = ze((y, b) => {
    if (!r) {
      y.preventDefault();
      return;
    }
    m(b), y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", b);
  }, [r]), _ = ze((y, b) => {
    if (!r || !f || f === b) return;
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const R = y.currentTarget.getBoundingClientRect(), I = y.clientX < R.left + R.width / 2;
    g((D) => D && D.id === b && D.before === I ? D : { id: b, before: I });
  }, [r, f]), C = ze(() => {
  }, []), w = ze((y, b, R) => {
    const I = o.map((B) => B.id), D = I.indexOf(y);
    if (D < 0) return;
    I.splice(D, 1);
    const K = I.indexOf(b);
    if (K < 0) {
      I.splice(D, 0, y);
      return;
    }
    const U = R ? K : K + 1;
    I.splice(U, 0, y), n("reorder", { order: I });
  }, [o, n]), N = ze((y, b) => {
    if (!r || !f || f === b) return;
    y.preventDefault();
    const R = y.currentTarget.getBoundingClientRect(), I = y.clientX < R.left + R.width / 2;
    w(f, b, I), m(null), g(null);
  }, [r, f, w]), x = ze(() => {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: E }, o.map((y) => {
      const b = d[y.id];
      if (!b) return null;
      const R = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, I = ["tlDashboard__tile"];
      return f === y.id && I.push("tlDashboard__tile--dragging"), h && h.id === y.id && I.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: I.join(" "),
          style: R,
          draggable: r,
          onDragStart: (D) => k(D, y.id),
          onDragOver: (D) => _(D, y.id),
          onDragLeave: C,
          onDrop: (D) => N(D, y.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(q, { control: y.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ko, useRef: hn, useState: bn, useEffect: No, useLayoutEffect: So } = e, Do = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(q, { control: n }))));
}, To = ({ group: l }) => {
  var f, m;
  const [t, n] = bn(!1), [a, o] = bn({}), c = hn(null), s = hn(null), u = ko(() => {
    n((h) => !h);
  }, []);
  So(() => {
    if (!t) return;
    const h = () => {
      const g = c.current;
      if (!g) return;
      const k = g.getBoundingClientRect();
      o({
        position: "fixed",
        top: k.bottom + 4,
        right: Math.max(8, window.innerWidth - k.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), No(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && c.current && !c.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Fe(t, { ESCAPE: () => n(!1) }), Wt(t, s, "first");
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
  ), Cn.createPortal(
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
      (m = l.subGroups) == null ? void 0 : m.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((k, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(q, { control: k })))))
    ),
    document.body
  ));
}, Ro = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((o) => o.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(To, { group: o }) : /* @__PURE__ */ e.createElement(Do, { group: o }))));
}, Lo = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(q, { control: t.frame }));
}, xo = ({ controlId: l }) => {
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
}, Io = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })));
}, Mo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), jo = {
  "js.sidebar.openDrawer": "Open navigation"
}, Po = ({ controlId: l }) => {
  const t = ne(), n = ue(jo);
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
W("TLButton", ml);
W("TLUploadButton", pl);
W("TLToggleButton", hl);
W("TLTextInput", qn);
W("TLPasswordInput", Qn);
W("TLNumberInput", el);
W("TLDatePicker", nl);
W("TLSelect", al);
W("TLBooleanChoice", ol);
W("TLCheckbox", ul);
W("TLCounter", bl);
W("TLTabBar", El);
W("TLFieldList", vl);
W("TLAudioRecorder", Cl);
W("TLAudioPlayer", wl);
W("TLFileUpload", Nl);
W("TLBinaryField", Dl);
W("TLFileChips", Ll);
W("TLRelativeTime", Ml);
W("TLAnchor", jl);
W("TLScrollLink", Pl);
W("TLAvatar", Fl);
W("TLDownload", $l);
W("TLPhotoCapture", Wl);
W("TLPhotoViewer", zl);
W("TLPdfViewer", Kl);
W("TLSplitPanel", Yl);
W("TLPanel", ea);
W("TLInset", ma);
W("TLMaximizeRoot", ta);
W("TLDeckPane", na);
W("TLSidebar", ua);
W("TLStack", da);
W("TLGrid", pa);
W("TLCard", fa);
W("TLAppBar", ha);
W("TLBreadcrumb", ga);
W("TLBottomBar", va);
W("TLDialog", ya);
W("TLDialogManager", Na);
W("TLWindow", Ra);
W("TLDrawer", Ia);
W("TLContextMenuRegion", ja);
W("TLSnackbar", Fa);
W("TLNoticeBar", Va);
W("TLMenu", Ya);
W("TLAppShell", Xa);
W("TLText", qa);
W("TLTableView", nr);
W("TLColumnSelect", ar);
W("TLCalendar", Er);
W("TLFormLayout", Dr);
W("TLFormGroup", Lr);
W("TLFormField", jr);
W("TLResourceCell", Pr);
W("TLTreeView", Br);
W("TLDropdownSelect", zr);
W("TLColorInput", ro);
W("TLIconSelect", fo);
W("TLDashboard", wo);
W("TLToolbar", Ro);
W("TLTileStack", Lo);
W("TLAdaptiveDetail", xo);
W("TLSlot", Io);
W("TLSlotContent", Mo);
W("TLDrawerToggle", Po);
