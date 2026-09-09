import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as X, useKeyboardBinding as me, useTLUpload as Ye, TLChild as G, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as Yn, useStandaloneKeyboardScope as Fe, KeyboardScopeProvider as Wt, useFocusTrap as Ut, CMD_VALUE_CHANGED as rt, anchoredOverlayProps as Gn, register as W } from "tl-react-bridge";
const { useCallback: Xt, useRef: Xn } = e, qn = 300, Zn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: qn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = ne(), s = Xn(!1), u = Xt(
    (k) => {
      s.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = Xt(async () => {
    await o(), r && s.current && (s.current = !1, i("commit"));
  }, [o, r, i]), d = t.multiline === !0;
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
}, { useCallback: qt } = e, Qn = 300, Jn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: Qn }), i = qt(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = qt(() => {
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
}, { useCallback: Zt } = e, el = 300, tl = ({ controlId: l, state: t, config: n }) => {
  const [a, o, i] = De({
    debounceMs: el,
    sendOnBlur: t.sendValueOnBlur === !0
  }), s = Zt(
    (h) => {
      const b = h.target.value;
      o(b === "" ? null : b);
    },
    [o]
  ), u = Zt(() => {
    i();
  }, [i]), r = a == null ? "" : String(a);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r);
  const c = t.hasError === !0, d = t.hasWarnings === !0, m = t.errorMessage, p = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && d ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r,
      onChange: s,
      onBlur: u,
      disabled: t.disabled === !0,
      className: p,
      "aria-invalid": c || void 0,
      title: c && m ? m : void 0
    }
  ));
}, { useCallback: nl } = e, ll = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = nl(
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
}, { useCallback: al } = e, rl = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), i = al(
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
}, { useCallback: ol } = e, sl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], i = t.presentation === "select", s = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, c = ol(
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
}, { useCallback: cl, useRef: il, useEffect: ul } = e, dl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, i = il(null);
  ul(() => {
    i.current && (i.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = cl(
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
const { useCallback: ml } = e, pl = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: i }) => {
  const s = X(), u = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = i ?? s.displayMode ?? "label-only", h = s.hidden === !0, b = s.tooltip, k = s.appearance, _ = s.size, C = s.cssClasses, w = s.navigateUrl, N = ml(() => {
    if (w) {
      window.location.assign(w);
      return;
    }
    u(r);
  }, [u, r, w]), x = s.keyGesture;
  me(x, () => m || h ? !1 : (N(), !0));
  const v = p === "icon-only", y = p === "label-only" || p === "icon-label" || v && !d, g = b ?? (v ? c : void 0), D = g ? `text:${g}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: N,
      disabled: m,
      className: "tlReactButton" + (v ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : "") + (C ? " " + C : ""),
      "data-tooltip": D,
      "aria-label": d || v ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, fl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = e.useRef(null), [o, i] = e.useState(!1), s = t.label ?? "", u = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, b = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), k = e.useCallback(async (x) => {
    const v = x.target.files;
    if (!v || v.length === 0) return;
    const y = new FormData();
    for (let g = 0; g < v.length; g++)
      y.append("file", v[g], v[g].name);
    x.target.value = "", i(!0);
    try {
      await n(y);
    } finally {
      i(!1);
    }
  }, [n]), _ = d === "icon-only", C = d === "icon-only" || d === "icon-label", w = d === "label-only" || d === "icon-label" || _ && !u, N = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: k,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: N,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? s : void 0
    },
    C && u && /* @__PURE__ */ e.createElement(Ne, { encoded: u, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: hl } = e, bl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const i = X(), s = ne(), u = t ?? "click", r = n ?? i.label, c = a ?? i.active === !0, d = o ?? i.disabled === !0, m = hl(() => {
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
}, gl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: vl } = e, El = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.tabs ?? [], o = t.activeTabId, i = vl((s) => {
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
}, _l = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, Cl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, yl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), u = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(w, N ? { mimeType: N } : void 0);
        u.current = x, x.ondataavailable = (v) => {
          v.data.size > 0 && r.current.push(v.data);
        }, x.onstop = async () => {
          w.getTracks().forEach((g) => g.stop()), c.current = null;
          const v = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], v.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const y = new FormData();
          y.append("audio", v, "recording.webm"), await n(y), o("idle");
        }, x.start(), o("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), b = ue(Cl), k = p === "recording" ? b["js.audioRecorder.stop"] : p === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], _ = p === "uploading", C = ["tlAudioRecorder__button"];
  return p === "recording" && C.push("tlAudioRecorder__button--recording"), p === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: h,
      disabled: _,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, wl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, kl = ({ controlId: l }) => {
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
  }, [i, n]), m = ue(wl), p = i === "loading" ? m["js.loading"] : i === "playing" ? m["js.audioPlayer.pause"] : i === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
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
}, Nl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Sl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (v) => {
    o("uploading");
    const y = new FormData();
    y.append("file", v, v.name), await n(y), o("idle");
  }, [n]), h = e.useCallback((v) => {
    var g;
    const y = (g = v.target.files) == null ? void 0 : g[0];
    y && p(y);
  }, [p]), b = e.useCallback(() => {
    var v;
    a !== "uploading" && ((v = u.current) == null || v.click());
  }, [a]), k = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!0);
  }, []), _ = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!1);
  }, []), C = e.useCallback((v) => {
    var g;
    if (v.preventDefault(), v.stopPropagation(), s(!1), a === "uploading") return;
    const y = (g = v.dataTransfer.files) == null ? void 0 : g[0];
    y && p(y);
  }, [a, p]), w = m === "uploading", N = ue(Nl), x = m === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
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
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: w,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, Dl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Tl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ye(), i = Ge(), s = ue(Dl), u = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [b, k] = e.useState("idle"), [_, C] = e.useState(!1), [w, N] = e.useState(!1), x = e.useRef(null), v = e.useCallback(async () => {
    if (!(!r || w)) {
      N(!0);
      try {
        const j = i + (i.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(j);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const S = await L.blob(), $ = URL.createObjectURL(S), f = document.createElement("a");
        f.href = $, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL($);
      } catch (j) {
        console.error("[TLBinaryField] Fetch error:", j);
      } finally {
        N(!1);
      }
    }
  }, [r, w, i, d, c]), y = e.useCallback(async (j) => {
    k("uploading");
    const L = new FormData();
    L.append("file", j, j.name), await o(L), k("idle");
  }, [o]), g = (p === "received" ? "idle" : b !== "idle" ? b : p) === "uploading", D = e.useCallback((j) => {
    var S;
    const L = (S = j.target.files) == null ? void 0 : S[0];
    L && y(L);
  }, [y]), M = e.useCallback(() => {
    var j;
    g || (j = x.current) == null || j.click();
  }, [g]), T = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!0);
  }, []), K = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!1);
  }, []), U = e.useCallback((j) => {
    var S;
    if (j.preventDefault(), j.stopPropagation(), C(!1), g) return;
    const L = (S = j.dataTransfer.files) == null ? void 0 : S[0];
    L && y(L);
  }, [g, y]), A = w ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), V = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: v,
      disabled: w,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, V) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const P = g, B = g ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
      onDragLeave: K,
      onDrop: U
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
        className: "tlFileUpload__button" + (P ? " tlFileUpload__button--uploading" : ""),
        onClick: M,
        disabled: P,
        title: B,
        "aria-label": B
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && V,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Rl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Ll(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const xl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ye(), o = Ge(), i = ue(Rl), s = t.chips ?? [], u = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (v) => {
    const y = Array.from(v);
    if (y.length !== 0) {
      c(!0);
      try {
        const g = new FormData();
        for (const D of y)
          g.append("file", D, D.name);
        await a(g);
      } finally {
        c(!1);
      }
    }
  }, [a]), b = e.useCallback(async (v) => {
    if (v.hasData)
      try {
        const y = o + "&key=" + encodeURIComponent(v.key), g = await fetch(y);
        if (!g.ok) {
          console.error("[TLFileChips] Failed to fetch data:", g.status);
          return;
        }
        const D = await g.blob(), M = URL.createObjectURL(D), T = document.createElement("a");
        T.href = M, T.download = v.name, T.style.display = "none", document.body.appendChild(T), T.click(), document.body.removeChild(T), URL.revokeObjectURL(M);
      } catch (y) {
        console.error("[TLFileChips] Fetch error:", y);
      }
  }, [o]), k = e.useCallback((v) => {
    v.target.files && h(v.target.files), v.target.value = "";
  }, [h]), _ = e.useCallback(() => {
    var v;
    r || (v = p.current) == null || v.click();
  }, [r]), C = e.useCallback((v) => {
    u && (v.preventDefault(), v.stopPropagation(), m(!0));
  }, [u]), w = e.useCallback((v) => {
    u && (v.preventDefault(), v.stopPropagation(), m(!1));
  }, [u]), N = e.useCallback((v) => {
    u && (v.preventDefault(), v.stopPropagation(), m(!1), !r && v.dataTransfer.files && h(v.dataTransfer.files));
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
    s.map((v) => {
      const y = i["js.download.file"].replace("{0}", v.name), g = i["js.fileChips.remove"].replace("{0}", v.name);
      return /* @__PURE__ */ e.createElement("span", { key: v.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(v),
          disabled: !v.hasData,
          title: v.hasData ? y : v.name
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
        v.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Ll(v.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: v.key }),
          title: g,
          "aria-label": g
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
}, Ml = 3e4;
function Il(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const jl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => i((u) => u + 1), Ml);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Il(n, o));
}, Pl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, Bl = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (i) => {
    i.preventDefault(), Yn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function Al(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Fl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Ol = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Fl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Al(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, $l = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Hl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = ne(), o = !!t.hasData, i = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + i, k = await fetch(b);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const _ = await k.blob(), C = URL.createObjectURL(_), w = document.createElement("a");
        w.href = C, w.download = s, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(C);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, i, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = ue($l);
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
}, Wl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ul = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [i, s] = e.useState(null), [u, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), b = t.error, k = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((T) => T.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    _(), o("idle");
  }, [_]), w = e.useCallback(async () => {
    var T;
    if (a !== "uploading") {
      if (s(null), !k) {
        (T = p.current) == null || T.click();
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
    const T = c.current, K = m.current;
    if (!T || !K)
      return;
    K.width = T.videoWidth, K.height = T.videoHeight;
    const U = K.getContext("2d");
    U && (U.drawImage(T, 0, 0), _(), o("uploading"), K.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", A, "capture.jpg"), await n(V), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), x = e.useCallback(async (T) => {
    var A;
    const K = (A = T.target.files) == null ? void 0 : A[0];
    if (!K) return;
    o("uploading");
    const U = new FormData();
    U.append("photo", K, K.name), await n(U), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var K;
    if (a !== "overlayOpen") return;
    (K = h.current) == null || K.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [a]), Fe(a === "overlayOpen", { ESCAPE: C }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((T) => T.stop()), d.current = null);
  }, []);
  const v = ue(Wl), y = a === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  u && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const M = ["tlPhotoCapture__mirrorBtn"];
  return u && M.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: w,
      disabled: a === "uploading",
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !k && /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
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
        className: M.join(" "),
        onClick: () => r((T) => !T),
        title: v["js.photoCapture.mirror"],
        "aria-label": v["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: N,
        title: v["js.photoCapture.capture"],
        "aria-label": v["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: v["js.photoCapture.close"],
        "aria-label": v["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, zl = {
  "js.photoViewer.alt": "Captured photo"
}, Vl = ({ controlId: l }) => {
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
  const r = ue(zl);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, Kl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Yl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPdf, o = t.dataRevision ?? 0, i = ue(Kl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: Qt, useRef: yt } = e, Gl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.orientation, o = t.resizable === !0, i = t.children ?? [], s = a === "horizontal", u = i.length > 0 && i.every((_) => _.collapsed), r = !u && i.some((_) => _.collapsed), c = u ? !s : s, d = yt(null), m = yt(null), p = yt(null), h = Qt((_, C) => {
    const w = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? u && !c ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : C !== void 0 ? w.flex = `0 0 ${C}px` : w.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (w.minWidth = s ? _.minSize : void 0, w.minHeight = s ? void 0 : _.minSize), w;
  }, [s, u, r, c]), b = Qt((_, C) => {
    _.preventDefault();
    const w = d.current;
    if (!w) return;
    const N = i[C], x = i[C + 1], v = w.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    v.forEach((M) => {
      y.push(s ? M.offsetWidth : M.offsetHeight);
    }), p.current = y, m.current = {
      splitterIndex: C,
      startPos: s ? _.clientX : _.clientY,
      startSizeBefore: y[C],
      startSizeAfter: y[C + 1],
      childBefore: N,
      childAfter: x
    };
    const g = (M) => {
      const T = m.current;
      if (!T || !p.current) return;
      const U = (s ? M.clientX : M.clientY) - T.startPos, A = T.childBefore.minSize || 0, V = T.childAfter.minSize || 0;
      let P = T.startSizeBefore + U, B = T.startSizeAfter - U;
      P < A && (B += P - A, P = A), B < V && (P += B - V, B = V), p.current[T.splitterIndex] = P, p.current[T.splitterIndex + 1] = B;
      const j = w.querySelectorAll(":scope > .tlSplitPanel__child"), L = j[T.splitterIndex], S = j[T.splitterIndex + 1];
      L && (L.style.flex = `0 0 ${P}px`), S && (S.style.flex = `0 0 ${B}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const M = {};
        i.forEach((T, K) => {
          const U = T.control;
          U != null && U.controlId && p.current && (M[U.controlId] = p.current[K]);
        }), n("updateSizes", { sizes: M });
      }
      p.current = null, m.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", D), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, s, n]), k = [];
  return i.forEach((_, C) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${_.collapsed && c ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: h(_)
        },
        /* @__PURE__ */ e.createElement(G, { control: _.control })
      )
    ), o && C < i.length - 1) {
      const w = i[C + 1];
      !_.collapsed && !w.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (x) => b(x, C)
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
    k
  );
}, jt = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: wt } = e, Xl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ql = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Zl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ql = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Jl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ea = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ta = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Xl), o = t.title, i = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, b = i === "MINIMIZED", k = i === "MAXIMIZED", _ = i === "HIDDEN", C = wt(() => {
    n("toggleMinimize");
  }, [n]), w = wt(() => {
    n("toggleMaximize");
  }, [n]), N = wt(() => {
    n("popOut");
  }, [n]);
  if (_)
    return null;
  const x = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, v = s && !k || u && !b || r, y = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || v;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: x
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Zl, null) : /* @__PURE__ */ e.createElement(ql, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(Jl, null) : /* @__PURE__ */ e.createElement(Ql, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ea, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(G, { control: t.child })),
    !b && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(jt, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, na = ({ controlId: l }) => {
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
}, la = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild }));
}, { useCallback: ve, useState: mt, useEffect: Pt, useRef: ft } = e, aa = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Bt(l, t, n, a) {
  const o = [];
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      o.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? o.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (o.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && o.push(...Bt(i.children, t, n, i.id)));
  return o;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, ra = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), oa = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: i }) => /* @__PURE__ */ e.createElement(
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
), sa = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), ca = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ia = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: i }) => {
  const s = ft(null);
  Pt(() => {
    const c = (d) => {
      s.current && !s.current.contains(d.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [i]), Fe(!0, { ESCAPE: i });
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
}, ua = ({
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
  onOpenFlyout: b,
  onCloseFlyout: k
}) => {
  const _ = ft(null), [C, w] = mt(null), N = ve(() => {
    a ? h === l.id ? k() : (_.current && w(_.current.getBoundingClientRect()), b(l.id)) : s(l.id);
  }, [a, h, l.id, s, b, k]), x = ve((y) => {
    _.current = y, r(y);
  }, [r]), v = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: N,
      title: a ? l.label : void 0,
      "aria-expanded": a ? v : t,
      tabIndex: u,
      ref: x,
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
  ), v && /* @__PURE__ */ e.createElement(
    ia,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: o,
      onExecute: i,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((y) => /* @__PURE__ */ e.createElement(
    vn,
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
      onOpenFlyout: b,
      onCloseFlyout: k
    }
  ))));
}, vn = ({
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
        ra,
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
        oa,
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
      return /* @__PURE__ */ e.createElement(sa, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(ca, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ua,
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
}, da = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(aa), o = t.items ?? [], i = t.activeItemId, s = t.collapsed, u = t.drawerOpen, r = u ? !1 : s, [c, d] = mt(() => {
    const A = /* @__PURE__ */ new Map(), V = (P) => {
      for (const B of P)
        B.type === "group" && (A.set(B.id, B.expanded), V(B.children));
    };
    return V(o), A;
  }), m = ve((A) => {
    d((V) => {
      const P = new Map(V), B = P.get(A) ?? !1;
      return P.set(A, !B), n("toggleGroup", { itemId: A, expanded: !B }), P;
    });
  }, [n]), p = ve((A) => {
    A !== i && n("selectItem", { itemId: A });
  }, [n, i]), h = ve((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), b = ve(() => {
    n("toggleCollapse", {});
  }, [n]), k = ve(() => {
    n("toggleDrawer", {});
  }, [n]), [_, C] = mt(null), w = ve((A) => {
    C(A);
  }, []), N = ve(() => {
    C(null);
  }, []);
  Pt(() => {
    r || C(null);
  }, [r]);
  const [x, v] = mt(() => {
    const A = Bt(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), y = ft(/* @__PURE__ */ new Map()), g = ve((A) => (V) => {
    V ? y.current.set(A, V) : y.current.delete(A);
  }, []), D = ve((A) => {
    v(A);
  }, []), M = ft(0), T = ve((A) => {
    v(A), M.current++;
  }, []);
  Pt(() => {
    const A = y.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, M.current]);
  const K = ve((A) => {
    if (A.key === "Escape" && _ !== null) {
      A.preventDefault(), N();
      return;
    }
    const V = Bt(o, r, c);
    if (V.length === 0) return;
    const P = V.findIndex((j) => j.id === x);
    if (P < 0) return;
    const B = V[P];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const j = (P + 1) % V.length;
        T(V[j].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const j = (P - 1 + V.length) % V.length;
        T(V[j].id);
        break;
      }
      case "Home": {
        A.preventDefault(), T(V[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), T(V[V.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), B.type === "nav" ? p(B.id) : B.type === "command" ? h(B.id) : B.type === "group" && (r ? _ === B.id ? N() : w(B.id) : m(B.id));
        break;
      }
      case "ArrowRight": {
        B.type === "group" && !r && ((c.get(B.id) ?? !1) || (A.preventDefault(), m(B.id)));
        break;
      }
      case "ArrowLeft": {
        B.type === "group" && !r && (c.get(B.id) ?? !1) && (A.preventDefault(), m(B.id));
        break;
      }
    }
  }, [
    o,
    r,
    c,
    x,
    _,
    T,
    p,
    h,
    m,
    w,
    N
  ]), U = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: U }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, o.map((A) => /* @__PURE__ */ e.createElement(
    vn,
    {
      key: A.id,
      item: A,
      activeItemId: i,
      collapsed: r,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: x,
      setItemRef: g,
      onItemFocus: D,
      groupStates: c,
      flyoutGroupId: _,
      onOpenFlyout: w,
      onCloseFlyout: N
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, ma = ({ controlId: l }) => {
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
}, pa = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, fa = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", i = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, i.map((u, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: u })));
}, ha = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", i = t.headerActions ?? [], s = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(G, { control: s })));
}, ba = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, i = t.children ?? [], s = t.actions ?? [], u = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: ga } = e, va = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = ga((i) => {
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
}, { useCallback: Ea } = e, _a = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = t.activeItemId, i = Ea((s) => {
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
}, { useCallback: Jt, useRef: Ca } = e, ya = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), wa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, i = t.child, s = Ca(null), u = Jt(() => {
    n("close");
  }, [n]), r = Jt((c) => {
    o && c.target === c.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(Wt, null, /* @__PURE__ */ e.createElement(ya, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: ka, useRef: Na } = e, Sa = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Na(n.length);
  return ka(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: ot, useRef: He, useState: st } = e, Da = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ta = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ra = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], La = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ta), o = t.title ?? "", i = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, b] = st(null), [k, _] = st(null), [C, w] = st(null), N = He(null), [x, v] = st(!1), y = He(null), g = He(null), D = He(null), M = He(null), T = He(null), K = ot(() => {
    n("close");
  }, [n]);
  Ut(!0, M, "field");
  const U = ot((j, L) => {
    L.preventDefault();
    const S = M.current;
    if (!S) return;
    const $ = S.getBoundingClientRect(), f = !N.current, I = N.current ?? { x: $.left, y: $.top };
    f && (N.current = I, w(I)), T.current = {
      dir: j,
      startX: L.clientX,
      startY: L.clientY,
      startW: $.width,
      startH: $.height,
      startPos: { ...I },
      symmetric: f
    };
    const Y = (Z) => {
      const F = T.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let le = F.startW, pe = F.startH, ye = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (le = F.startW + 2 * te), F.dir.includes("w") && (le = F.startW - 2 * te), F.dir.includes("s") && (pe = F.startH + 2 * se), F.dir.includes("n") && (pe = F.startH - 2 * se)) : (F.dir.includes("e") && (le = F.startW + te), F.dir.includes("w") && (le = F.startW - te, ye = te), F.dir.includes("s") && (pe = F.startH + se), F.dir.includes("n") && (pe = F.startH - se, we = se));
      const Te = Math.max(200, le), Re = Math.max(100, pe);
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), g.current = Te, D.current = Re, b(Te), _(Re);
      const Oe = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      N.current = Oe, w(Oe);
    }, H = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", H);
      const Z = g.current, F = D.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), T.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", H);
  }, [n]), A = ot((j) => {
    if (j.button !== 0 || j.target.closest("button")) return;
    j.preventDefault();
    const L = M.current;
    if (!L) return;
    const S = L.getBoundingClientRect(), $ = N.current ?? { x: S.left, y: S.top }, f = j.clientX - $.x, I = j.clientY - $.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - f, le = Z.clientY - I;
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
      S && (w(S.x !== -1 ? { x: S.x, y: S.y } : null), b(S.w), _(S.h)), v(!1);
    } else {
      const S = M.current, $ = S == null ? void 0 : S.getBoundingClientRect();
      y.current = {
        x: ((j = N.current) == null ? void 0 : j.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = N.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: k ?? null
      }, v(!0), w({ x: 0, y: 0 }), b(null), _(null);
    }
  }, [x, h, k]), P = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...k != null ? { height: k + "px" } : s != null ? { height: s } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, B = l + "-title";
  return /* @__PURE__ */ e.createElement(Wt, { modal: !0 }, /* @__PURE__ */ e.createElement(Da, { onClose: K }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: M,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": B
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : A,
        onDoubleClick: r ? V : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: B }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(G, { control: c })),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((j, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: j }))),
    r && !x && Ra.map((j) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: j,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${j}`,
        onMouseDown: (L) => U(j, L)
      }
    ))
  ));
}, { useCallback: xa } = e, Ma = {
  "js.drawer.close": "Close"
}, Ia = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ma), o = t.open === !0, i = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, r = t.child, c = xa(() => {
    n("close");
  }, [n]);
  Fe(o, { ESCAPE: c });
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
}, { useCallback: ct, useRef: ja } = e, Pa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ja(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", u = ct((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = ct(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = ct((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = ct((m) => {
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
}, { useCallback: Ba, useEffect: en, useRef: Aa, useState: tn } = e, Fa = 250, Oa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.message ?? "", o = t.content ?? "", i = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [c, d] = tn(!1), [m, p] = tn(!1), h = Aa(!1);
  en(() => {
    h.current = !1;
  }, [r]);
  const b = Ba(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return en(() => {
    if (!u || s === 0 || m) return;
    const k = setTimeout(b, h.current ? Fa : s);
    return () => clearTimeout(k);
  }, [u, s, m, b]), !u && !c ? null : /* @__PURE__ */ e.createElement(
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
}, { useCallback: $a, useEffect: nn, useMemo: Ha, useRef: Wa, useState: Ua } = e, za = 1e3;
function Va(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), i = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const Ka = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", i = t.text ?? "", s = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = Ha(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [p, h] = Ua(0), b = a && s != null;
  nn(() => {
    if (!b) return;
    const x = setInterval(() => h((v) => v + 1), za);
    return () => clearInterval(x);
  }, [b, s]);
  const k = Wa(null);
  nn(() => {
    !b || d == null || s == null || k.current !== s && (Date.now() + m < s + d || (k.current = s, n("deadlinePassed", {})));
  }, [p, b, s, d, m, n]);
  const _ = $a(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const C = s != null ? s - (Date.now() + m) : null;
  if (r != null && C != null && C > r) return null;
  const w = C != null ? Va(C) : null, N = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${N ? " tlNoticeBar--clickable" : ""}`,
      role: N ? "button" : "status",
      "aria-live": "polite",
      tabIndex: N ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": N ? `${i} ${c}` : void 0,
      onClick: N ? _ : void 0,
      onKeyDown: N ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    w !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, w)
  );
}, { useCallback: kt, useEffect: ln, useRef: Ya, useState: an } = e, Ga = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.anchorId, i = t.anchorX, s = t.anchorY, u = t.items ?? [], r = Ya(null), [c, d] = an({ top: 0, left: 0 }), [m, p] = an(0), h = u.filter((C) => C.type === "item" && !C.disabled);
  ln(() => {
    var g, D;
    if (!a) return;
    const C = ((g = r.current) == null ? void 0 : g.offsetHeight) ?? 200, w = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (i != null && s != null) {
      let M = s, T = i;
      M + C > window.innerHeight && (M = Math.max(0, window.innerHeight - C)), T + w > window.innerWidth && (T = Math.max(0, window.innerWidth - w)), d({ top: M, left: T }), p(0);
      return;
    }
    if (!o) return;
    const N = document.getElementById(o);
    if (!N) return;
    const x = N.getBoundingClientRect();
    let v = x.bottom + 4, y = x.left;
    v + C > window.innerHeight && (v = x.top - C - 4), y + w > window.innerWidth && (y = x.right - w), d({ top: v, left: y }), p(0);
  }, [a, o, i, s]);
  const b = kt(() => {
    n("close");
  }, [n]), k = kt((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  ln(() => {
    if (!a) return;
    const C = (w) => {
      r.current && !r.current.contains(w.target) && b();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [a, b]);
  const _ = kt((C) => {
    if (C.key === "Escape") {
      C.preventDefault(), b();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), p((w) => (w + 1) % h.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), p((w) => (w - 1 + h.length) % h.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const w = h[m];
      w && k(w.id);
    }
  }, [b, k, h, m]);
  return Ut(a, r), a ? /* @__PURE__ */ e.createElement(
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
    u.map((C, w) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: w, className: "tlMenu__separator" });
      const x = h.indexOf(C) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : "") + (C.cssClasses ? " " + C.cssClasses : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => k(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: C.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, Xa = 768, qa = ({ controlId: l }) => {
  const t = X(), n = ne();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${Xa}px)`), c = (m) => {
      n("reportDisplayClass", { displayClass: m ? "COMPACT" : "REGULAR" });
    };
    c(r.matches);
    const d = (m) => c(m.matches);
    return r.addEventListener("change", d), () => r.removeEventListener("change", d);
  }, [n]);
  const a = t.header, o = t.notices, i = t.content, s = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: a })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(G, { control: i })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: s })), /* @__PURE__ */ e.createElement(G, { control: u }));
}, Za = ({ controlId: l }) => {
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
}, Qa = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), Ja = {
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
}, er = 300, rn = 50, tr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function Nt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, tr));
}
const At = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', nr = At + ", button:not([disabled]), a[href]";
function En(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function St(l, t, n = {}) {
  const a = En(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), s = i == null ? void 0 : i.querySelector(At);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const i of o) {
    const s = i.querySelector(At);
    if (s) return s;
  }
  return null;
}
const lr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ja), o = e.useRef(null);
  e.useEffect(() => {
    const E = o.current;
    if (!E) return;
    const R = (z) => {
      const Q = z.detail;
      let ee = Q.target;
      for (; ee && ee !== E; ) {
        const re = ee.dataset.row, oe = ee.dataset.col;
        if (re != null && oe != null) {
          Q.resolved = { key: re + "|" + oe };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return E.addEventListener("tl-tooltip-resolve", R), () => E.removeEventListener("tl-tooltip-resolve", R);
  }, []);
  const i = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, b = t.columnSelect ?? !1, k = t.filterBar ?? !1, _ = t.namedFilters ?? [], C = t.activeNamedFilter ?? "", w = t.search ?? "", N = t.filterSaving ?? !1, x = e.useMemo(
    () => i.filter((E) => E.sortPriority && E.sortPriority > 0).length,
    [i]
  ), v = c === "multi", y = 40, g = 20, D = e.useRef(null), M = e.useRef(null), T = e.useRef(null), K = e.useRef(null), U = e.useRef(null), [A, V] = e.useState({}), P = e.useRef(null), B = e.useRef(!1), j = e.useRef(null), [L, S] = e.useState(null), [$, f] = e.useState(null), [I, Y] = e.useState(null), [H, Z] = e.useState(0);
  e.useEffect(() => {
    const E = T.current;
    if (!E)
      return;
    const R = () => {
      const Q = E.offsetWidth - E.clientWidth;
      Z((ee) => ee === Q ? ee : Q);
    };
    R();
    const z = new ResizeObserver(R);
    return z.observe(E), () => z.disconnect();
  }, []), e.useEffect(() => {
    P.current || V({});
  }, [i]);
  const F = e.useCallback((E) => A[E.name] ?? E.width, [A]), te = e.useMemo(() => {
    const E = [];
    let R = v && p > 0 ? y : 0;
    for (let z = 0; z < p && z < i.length; z++)
      E.push(R), R += F(i[z]);
    return E;
  }, [i, p, v, y, F]), se = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let E = v ? y : 0;
    for (let R = 0; R < p && R < i.length; R++)
      E += F(i[R]);
    return E;
  }, [i, p, v, y, F]), le = s * r, pe = e.useRef(null), ye = e.useCallback((E, R, z) => {
    z.preventDefault(), z.stopPropagation(), P.current = { column: E, startX: z.clientX, startWidth: R };
    let Q = z.clientX, ee = 0;
    const re = () => {
      const ce = P.current;
      if (!ce) return;
      const de = Math.max(rn, ce.startWidth + (Q - ce.startX) + ee);
      V((_e) => ({ ..._e, [ce.column]: de }));
    }, oe = () => {
      const ce = T.current, de = D.current;
      if (!ce || !P.current) return;
      const _e = ce.getBoundingClientRect(), xe = 40, Yt = 8, Kn = ce.scrollLeft;
      Q > _e.right - xe ? ce.scrollLeft += Yt : Q < _e.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Yt));
      const Gt = ce.scrollLeft - Kn;
      Gt !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += Gt, re()), pe.current = requestAnimationFrame(oe);
    };
    pe.current = requestAnimationFrame(oe);
    const he = (ce) => {
      Q = ce.clientX, re();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", fe), pe.current !== null && (cancelAnimationFrame(pe.current), pe.current = null);
      const de = P.current;
      if (de) {
        const _e = Math.max(rn, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: _e }), P.current = null, B.current = !0, requestAnimationFrame(() => {
          B.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", fe);
  }, [n]), we = e.useCallback(() => {
    D.current && T.current && (D.current.scrollLeft = T.current.scrollLeft), K.current !== null && clearTimeout(K.current), K.current = window.setTimeout(() => {
      const E = T.current;
      if (!E) return;
      const R = E.scrollTop, z = Math.ceil(E.clientHeight / r), Q = Math.floor(R / r);
      n("scroll", { start: Q, count: z });
    }, 80);
  }, [n, r]), Te = e.useCallback((E, R, z) => {
    if (B.current) return;
    let Q;
    !R || R === "desc" ? Q = "asc" : Q = "desc";
    const ee = z.shiftKey ? "add" : "replace";
    n("sort", { column: E, direction: Q, mode: ee });
  }, [n]), Re = e.useCallback((E, R) => {
    j.current = E, R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", E);
  }, []), Oe = e.useCallback((E, R) => {
    if (!j.current || j.current === E) {
      S(null);
      return;
    }
    R.preventDefault(), R.dataTransfer.dropEffect = "move";
    const z = R.currentTarget.getBoundingClientRect(), Q = R.clientX < z.left + z.width / 2 ? "left" : "right";
    S({ column: E, side: Q });
  }, []), $e = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const R = j.current;
    if (!R || !L) {
      j.current = null, S(null);
      return;
    }
    let z = i.findIndex((ee) => ee.name === L.column);
    if (z < 0) {
      j.current = null, S(null);
      return;
    }
    const Q = i.findIndex((ee) => ee.name === R);
    L.side === "right" && z++, Q < z && z--, n("columnReorder", { column: R, targetIndex: z }), j.current = null, S(null);
  }, [i, L, n]), O = e.useCallback(() => {
    j.current = null, S(null);
  }, []), q = e.useCallback((E, R) => {
    var ee, re, oe, he;
    const z = window.getSelection();
    if (z && !z.isCollapsed && R.currentTarget.contains(z.anchorNode))
      return;
    if (!Nt(R) && ((ee = T.current) == null || ee.focus({ preventScroll: !0 }), !R.ctrlKey && !R.metaKey && !R.shiftKey)) {
      const fe = (he = (oe = (re = R.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      U.current = { index: E, col: fe ?? void 0 };
    }
    const Q = u.find((fe) => fe.index === E);
    Nt(R) && (Q != null && Q.selected) && !R.ctrlKey && !R.metaKey && !R.shiftKey || n("select", {
      rowIndex: E,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    });
  }, [n, u]), ae = e.useCallback((E, R, z) => {
    n("moveSelection", { direction: E, extend: R, move: z });
  }, [n]), ie = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: v, shiftKey: !1 });
  }, [n, m, v]), Xe = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Dn = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const E = T.current;
    if (!E)
      return;
    const R = m * r, z = R + r;
    R < E.scrollTop ? E.scrollTop = R : z > E.scrollTop + E.clientHeight && (E.scrollTop = z - E.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const E = U.current, R = T.current;
    if (!E || !R)
      return;
    const z = u.find((re) => re.index === E.index);
    if (!z || !St(R, z.id))
      return;
    U.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !R.contains(Q))
      return;
    const ee = St(R, z.id, { col: E.col, last: E.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const Tn = e.useCallback((E) => {
    if (E.key !== "Tab")
      return;
    const R = T.current, z = document.activeElement;
    if (!R || !z || !R.contains(z))
      return;
    const Q = z.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = u.find((xe) => xe.id === ee);
    if (!re)
      return;
    const oe = En(R, ee).flatMap((xe) => Array.from(xe.querySelectorAll(nr))), he = oe.indexOf(z);
    if (he < 0)
      return;
    const fe = !E.shiftKey;
    if (!(fe ? he === oe.length - 1 : he === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const _e = u.find((xe) => xe.index === de);
    _e && St(R, _e.id) || (E.preventDefault(), U.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, s, n]), Rn = e.useCallback((E, R) => {
    R.stopPropagation(), n("select", { rowIndex: E, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Ln = e.useCallback(() => {
    const E = d === s && s > 0;
    n("selectAll", { selected: !E });
  }, [n, d, s]), xn = e.useCallback((E, R, z) => {
    z.stopPropagation(), n("expand", { rowIndex: E, expanded: R });
  }, [n]), Mn = e.useCallback((E, R) => {
    R.preventDefault(), f({ x: R.clientX, y: R.clientY, colIdx: E });
  }, []), In = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), f(null));
  }, [$, n]), jn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), f(null);
  }, [n]), Pn = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const R = M.current, z = D.current;
    if (!R || !z)
      return;
    const Q = R.clientWidth, ee = [{ x: 0, count: 0 }];
    z.querySelectorAll("[data-col-idx]").forEach((fe) => {
      const ce = fe.getBoundingClientRect().right - R.getBoundingClientRect().left;
      ce > 0 && ce <= Q && ee.push({ x: ce, count: Number(fe.dataset.colIdx) + 1 });
    });
    let re = { x: se, count: p };
    const oe = (fe) => {
      const ce = fe.clientX - R.getBoundingClientRect().left;
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
    const E = () => f(null);
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [$]), Fe(!!$, { ESCAPE: () => f(null) });
  const Bn = e.useCallback((E, R) => {
    R.stopPropagation(), R.preventDefault(), n("openFilter", { column: E });
  }, [n]), An = e.useCallback((E) => {
    E.stopPropagation(), E.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Fn, Vt] = e.useState(w), vt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    vt.current || Vt(w);
  }, [w]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const at = e.useCallback((E) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), vt.current = !1, n("search", { term: E });
  }, [n]), On = e.useCallback((E) => {
    Vt(E), vt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => at(E), er);
  }, [at]), $n = e.useCallback((E) => {
    E.key === "Enter" && (E.preventDefault(), at(E.currentTarget.value));
  }, [at]), Hn = e.useCallback((E) => {
    E === C ? n("clearFilter", {}) : n("applyNamedFilter", { id: E });
  }, [C, n]), Wn = e.useCallback((E, R) => {
    R.stopPropagation(), n("deleteNamedFilter", { id: E });
  }, [n]), [qe, Ze] = e.useState(null), Et = e.useCallback(() => {
    const E = (qe ?? "").trim();
    E && (n("saveNamedFilter", { filterName: E }), Ze(null));
  }, [qe, n]), Un = e.useCallback((E) => {
    E.key === "Enter" ? (E.preventDefault(), Et()) : E.key === "Escape" && (E.preventDefault(), Ze(null));
  }, [Et]), _t = i.reduce((E, R) => E + F(R), 0) + (v ? y : 0), Ct = b ? 32 : 0, zn = d === s && s > 0, Kt = d > 0 && d < s, Vn = e.useCallback((E) => {
    E && (E.indeterminate = Kt);
  }, [Kt]);
  return /* @__PURE__ */ e.createElement(Wt, { active: Dn }, /* @__PURE__ */ e.createElement(
    Qa,
    {
      isMulti: v,
      cursorIndex: m,
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
      onDragOver: (E) => {
        if (!j.current) return;
        E.preventDefault();
        const R = T.current, z = D.current;
        if (!R) return;
        const Q = R.getBoundingClientRect(), ee = 40, re = 8;
        E.clientX < Q.left + ee ? R.scrollLeft = Math.max(0, R.scrollLeft - re) : E.clientX > Q.right - ee && (R.scrollLeft += re), z && (z.scrollLeft = R.scrollLeft);
      },
      onDrop: $e
    },
    k && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, _.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, _.map((E) => {
      const R = E.id === C;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: E.id,
          className: "tlTableView__chip" + (R ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": R,
            title: R ? a["js.table.clearFilter"] : E.label,
            onClick: () => Hn(E.id)
          },
          E.label
        ),
        E.deletable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipRemove",
            title: a["js.table.deleteFilter"],
            "aria-label": a["js.table.deleteFilter"],
            onClick: (z) => Wn(E.id, z)
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
        value: Fn,
        onChange: (E) => On(E.target.value),
        onKeyDown: $n
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
        onChange: (E) => Ze(E.target.value),
        onKeyDown: Un
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !qe.trim(),
        onClick: Et
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
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: M }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: D }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: _t, paddingRight: Ct + H }
      },
      v && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: y,
            minWidth: y,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (E) => {
            j.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== j.current && S({ column: i[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Vn,
            className: "tlTableView__checkbox",
            checked: zn,
            onChange: Ln
          }
        )
      ),
      i.map((E, R) => {
        const z = F(E);
        i.length - 1;
        let Q = "tlTableView__headerCell";
        E.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === E.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = R < p, re = R === p - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: E.name,
            className: Q,
            "data-col-idx": R,
            style: {
              width: z,
              minWidth: z,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: te[R], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: E.sortable ? (oe) => Te(E.name, E.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Mn(R, oe),
            onDragStart: (oe) => Re(E.name, oe),
            onDragOver: (oe) => Oe(E.name, oe),
            onDrop: $e,
            onDragEnd: O
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, E.label),
          E.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (E.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: E.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => Bn(E.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: E.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          E.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, E.sortDirection === "asc" ? "▲" : "▼", x > 1 && E.sortPriority != null && E.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, E.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => ye(E.name, z, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (E) => {
            if (j.current && i.length > 0) {
              const R = i[i.length - 1];
              R.name !== j.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", S({ column: R.name, side: "right" }));
            }
          },
          onDrop: $e
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (I ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Pn
      }
    ), b && /* @__PURE__ */ e.createElement(
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
        ref: T,
        className: "tlTableView__body",
        onScroll: we,
        onKeyDown: Tn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: le, position: "relative", width: _t, paddingRight: Ct } }, u.map((E) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: "tlTableView__row" + (E.selected ? " tlTableView__row--selected" : "") + (E.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: E.index * r,
            height: r,
            width: _t,
            paddingRight: Ct,
            ...E.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (R) => {
            (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && !Nt(R) && R.preventDefault();
          },
          onClick: (R) => q(E.index, R)
        },
        v && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: y,
              minWidth: y,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (R) => R.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: E.selected,
              onChange: () => {
              },
              onClick: (R) => Rn(E.index, R),
              tabIndex: -1
            }
          )
        ),
        i.map((R, z) => {
          const Q = F(R), ee = z === i.length - 1, re = z < p, oe = z === p - 1;
          let he = "tlTableView__cell";
          re && (he += " tlTableView__cell--frozen"), oe && (he += " tlTableView__cell--frozenLast");
          const fe = h && z === 0, ce = E.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: R.name,
              className: he,
              "data-row": E.id,
              "data-col": R.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: te[z], zIndex: 2 } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * g } }, E.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => xn(E.index, !E.expanded, de)
              },
              E.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), E.cells[R.name] && /* @__PURE__ */ e.createElement(G, { control: E.cells[R.name] })) : E.cells[R.name] && /* @__PURE__ */ e.createElement(G, { control: E.cells[R.name] })
          );
        })
      )))
    ),
    I && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: I.x } }),
    $ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: $.y, left: $.x, zIndex: 1e4 },
        onMouseDown: (E) => E.stopPropagation()
      },
      $.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: In }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: jn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ar = {
  "js.table.columnSearch": "Find column"
}, rr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ar), o = t.entries ?? [], i = o.filter((v) => v.visible).length, [s, u] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((v) => v.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), b = e.useCallback((v) => {
    m.current = v, h(v);
  }, []), k = e.useCallback((v, y) => {
    n("columnVisible", { column: v, visible: y });
  }, [n]), _ = e.useCallback((v, y) => {
    d.current = v, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", v);
  }, []), C = e.useCallback((v, y) => {
    if (!d.current || d.current === v) {
      b(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const g = y.currentTarget.getBoundingClientRect(), D = y.clientY < g.top + g.height / 2 ? "top" : "bottom";
    b({ name: v, side: D });
  }, [b]), w = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), N = e.useCallback((v) => {
    v.preventDefault();
    const y = d.current, g = m.current;
    if (d.current = null, b(null), !y || !g)
      return;
    const D = o.findIndex((K) => K.name === g.name), M = o.findIndex((K) => K.name === y);
    if (D < 0 || M < 0)
      return;
    let T = g.side === "top" ? D : D + 1;
    M < T && T--, T !== M && n("columnReorder", { column: y, targetIndex: T });
  }, [o, n, b]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: N }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (v) => u(v.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, c.map((v) => {
    const y = v.visible && i <= 1;
    let g = "tlColumnSelect__row";
    return p && p.name === v.name && (g += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: g,
        draggable: !0,
        onDragStart: (D) => _(v.name, D),
        onDragOver: (D) => C(v.name, D),
        onDrop: N,
        onDragEnd: w
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: v.visible,
          disabled: y,
          onChange: (D) => k(v.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, v.label))
    );
  })));
}, { useState: Ft, useRef: nt, useCallback: pt, useMemo: Be, useEffect: on } = e, or = {
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
}, Ee = 44, ht = 15, Ce = 6e4, sr = 36e5, je = 864e5, cr = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ve(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function ir(l) {
  return Se(l);
}
function lt(l, t) {
  return Se(l) === Se(t);
}
function Ie(l) {
  return (l - Se(l)) / Ce;
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
  return "tlCalEvent--c" + Math.abs(t) % cr;
}
function gt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function ur(l) {
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
function dr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Ae(l, n, t.start) + "–" + Ae(l, n, t.end);
}
const mr = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], pr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, mr.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function fr(l) {
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
      botMin: Ie(s.start) + Math.max(15, (s.end - s.start) / Ce),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && i(), n;
}
const Dt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ot = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
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
}, _n = (l) => {
  const [t, n] = Ft(null), a = nt(null);
  a.current = t;
  const o = pt((u) => n(u), []), i = pt(() => n(null), []), s = pt(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: i };
}, hr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: i, dayStartHour: s, dayEndHour: u, now: r, send: c, editable: d, i18n: m } = l, p = Be(() => {
    const P = n === "DAY" ? 1 : 7, B = [];
    for (let j = 0; j < P; j++) {
      const L = Ve(t, j);
      n === "WORK_WEEK" && i.includes(new Date(L).getDay()) || B.push(L);
    }
    return B;
  }, [t, n, i]), h = _n(c), b = nt(null), k = nt(null), [_, C] = Ft(null), w = nt(null);
  w.current = _;
  const [N, x] = Ft(Date.now());
  on(() => {
    const P = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const v = pt(
    (P, B) => {
      const j = b.current;
      if (!j)
        return { dayIndex: 0, min: 0 };
      const L = j.getBoundingClientRect(), S = L.width / p.length, $ = Je(Math.floor((P - L.left) / S), 0, p.length - 1), f = B - L.top + j.scrollTop, I = Je(f / Ee * 60, 0, 1440);
      return { dayIndex: $, min: I };
    },
    [p.length]
  );
  on(() => {
    if (!_)
      return;
    const P = (L) => {
      const S = w.current;
      if (!S)
        return;
      const { dayIndex: $, min: f } = v(L.clientX, L.clientY);
      S.mode === "move" ? C({ ...S, dayStart: p[$], startMin: Je(Qe(f - S.grabMin), 0, 1440 - S.dur) }) : S.mode === "resize" ? C({ ...S, endMin: Je(Qe(f), S.startMin + ht, 1440) }) : C({ ...S, toMin: Je(Qe(f), 0, 1440) });
    }, B = () => {
      const L = w.current;
      if (C(null), !!L)
        if (L.mode === "move") {
          const S = L.dayStart + L.startMin * Ce;
          S !== L.origStartMs && c("moveEvent", { eventId: L.id, start: S, end: S + L.dur * Ce });
        } else if (L.mode === "resize") {
          const S = L.dayStart + L.endMin * Ce;
          S !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: S });
        } else {
          const S = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - S >= ht && h.open({ start: L.dayStart + S * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, j = () => C(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", B, { once: !0 }), window.addEventListener("pointercancel", j), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", B), window.removeEventListener("pointercancel", j);
    };
  }, [_, p, v, c, h.open]);
  const y = (P, B, j) => {
    if (!d || !B.movable)
      return;
    P.stopPropagation(), Dt(P), h.discard();
    const { min: L } = v(P.clientX, P.clientY), S = (B.end - B.start) / Ce;
    C({
      mode: "move",
      id: B.id,
      grabMin: L - Ie(B.start),
      dur: S,
      dayStart: j,
      startMin: Ie(B.start),
      origStartMs: B.start
    });
  }, g = (P, B, j) => {
    !d || !B.resizable || (P.stopPropagation(), Dt(P), h.discard(), C({
      mode: "resize",
      id: B.id,
      dayStart: j,
      startMin: Ie(B.start),
      endMin: Ie(B.end),
      origEndMs: B.end
    }));
  }, D = (P, B) => {
    if (!d || P.button !== 0)
      return;
    Dt(P), h.discard();
    const { min: j } = v(P.clientX, P.clientY);
    C({ mode: "create", dayStart: B, fromMin: Qe(j), toMin: Qe(j) });
  }, M = Array.from({ length: 24 }, (P, B) => B), T = Be(() => {
    if (_ === null || !("id" in _))
      return a;
    const P = _;
    return a.map((B) => {
      if (B.id !== P.id)
        return B;
      if (P.mode === "move") {
        const j = P.dayStart + P.startMin * Ce;
        return { ...B, start: j, end: j + P.dur * Ce };
      }
      return { ...B, end: P.dayStart + P.endMin * Ce };
    });
  }, [a, _]), K = Be(() => p.map(
    (P) => fr(
      T.filter((B) => !B.allDay && B.start < P + je && B.end > P)
    )
  ), [p, T]), U = Be(() => p.map((P) => T.filter((B) => B.allDay && B.start < P + je && B.end > P)), [p, T]), A = s * Ee, V = u * Ee;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((P) => {
    const B = i.includes(new Date(P).getDay()), j = lt(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (B ? " tlCalDayHead--nonworking" : "") + (j ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Ae(o, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, m["js.calendar.allDay"]), p.map((P, B) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: P,
      className: "tlCalAllDayCell",
      onClick: () => d && h.open({ start: P, end: P + je, allDay: !0 })
    },
    h.pending && h.pending.allDay && h.pending.start === P && /* @__PURE__ */ e.createElement(
      Ot,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    U[B].map((j) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: j.id,
        className: "tlCalAllDayEvent " + bt(j.category) + (j.selected ? " tlCalEvent--selected" : ""),
        style: gt(j),
        title: j.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: j.id });
        }
      },
      j.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: k }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * Ee } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, M.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * Ee } }, P === 0 ? "" : Ae(o, { hour: "numeric" }, Se(t) + P * sr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: b, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((P, B) => {
    const j = i.includes(new Date(P).getDay()), L = _ && ("dayStart" in _ && _.dayStart === P) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (j ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => D(S, P)
      },
      M.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * Ee } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: V - A } }),
      lt(P, N) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * Ee } }),
      K[B].map((S) => {
        const $ = _ !== null && "id" in _ && _.id === S.ev.id, f = S.topMin / 60 * Ee, I = (S.botMin - S.topMin) / 60 * Ee, Y = 100 / S.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.ev.id,
            className: "tlCalEvent " + bt(S.ev.category) + (S.ev.selected ? " tlCalEvent--selected" : "") + ($ ? " tlCalEvent--dragging" : ""),
            style: gt(S.ev, {
              top: f,
              height: I,
              left: `${S.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: S.ev.tooltip,
            onPointerDown: (H) => y(H, S.ev, P),
            onClick: (H) => {
              H.stopPropagation(), c("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, dr(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (H) => g(H, S.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Ot,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * Ee,
            height: (h.pending.end - h.pending.start) / Ce / 60 * Ee
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
            top: Math.min(L.fromMin, L.toMin) / 60 * Ee,
            height: Math.abs(L.toMin - L.fromMin) / 60 * Ee
          }
        }
      )
    );
  })))));
}, br = 3, gr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: i, send: s, editable: u, now: r, i18n: c } = l, d = _n(s), m = Be(() => {
    const h = [];
    for (let b = 0; b < 6; b++) {
      const k = [];
      for (let _ = 0; _ < 7; _++)
        k.push(Ve(t, b * 7 + _));
      h.push(k);
    }
    return h;
  }, [t]), p = (h, b) => {
    h.preventDefault();
    const k = h.dataTransfer.getData("text/plain"), _ = a.find((w) => w.id === k);
    if (!_ || !u || !_.movable)
      return;
    const C = b - Se(_.start);
    s("moveEvent", { eventId: k, start: _.start + C, end: _.end + C });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Ae(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, b) => {
    const k = h[0], _ = Ve(k, 7), C = a.filter((N) => (N.allDay || N.end - N.start >= je) && N.start < _ && N.end > k).sort((N, x) => N.start - x.start).slice(0, 3), w = C.length;
    return /* @__PURE__ */ e.createElement("div", { key: b, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((N) => {
      const x = new Date(N).getMonth() === new Date(n).getMonth(), v = i.includes(new Date(N).getDay()), y = lt(N, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (v ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (g) => g.preventDefault(),
          onDrop: (g) => p(g, N),
          onClick: () => u && d.open({ start: N, end: N + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (y ? " tlCalMonthDayNum--today" : ""),
            onClick: (g) => {
              g.stopPropagation(), s("goto", { date: N, granularity: "DAY" });
            }
          },
          new Date(N).getDate()
        ),
        d.pending && d.pending.start === N && /* @__PURE__ */ e.createElement(
          Ot,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, C.map((N, x) => {
      const v = Math.max(0, Math.floor((Se(Math.max(N.start, k)) - k) / je)), y = Math.min(7, Math.ceil((N.end - k) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlCalMonthBar " + bt(N.category) + (N.selected ? " tlCalEvent--selected" : ""),
          style: gt(N, {
            gridColumn: `${v + 1} / ${Math.max(v + 1, y) + 1}`,
            gridRow: x + 1
          }),
          draggable: u && N.movable,
          onDragStart: (g) => g.dataTransfer.setData("text/plain", N.id),
          title: N.tooltip,
          onClick: (g) => {
            g.stopPropagation(), s("selectEvent", { eventId: N.id });
          }
        },
        N.title
      );
    }), h.map((N, x) => {
      const v = a.filter((D) => !D.allDay && D.end - D.start < je && lt(D.start, N)).sort((D, M) => D.start - M.start), y = v.slice(0, br), g = v.length - y.length;
      return y.map((D, M) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: D.id,
          className: "tlCalChip " + bt(D.category) + (D.selected ? " tlCalEvent--selected" : ""),
          style: gt(D, { gridColumn: x + 1, gridRow: w + 1 + M }),
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
        g > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + N,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: w + 1 + y.length },
              onClick: () => s("goto", { date: N, granularity: "DAY" })
            },
            "+",
            g,
            " ",
            l.i18n["js.calendar.more"]
          )
        ] : []
      );
    })));
  })));
}, vr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: i, send: s, now: u } = l, r = Be(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let b = Se(h.start);
      const k = h.end;
      for (; b < k; )
        p.add(b), b = Ve(b, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Be(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, b) => {
      const k = new Date(p);
      return k.setDate(p.getDate() + (o + b) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(k);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), b = Se(Ve(p, -((h.getDay() - o + 7) % 7))), k = Array.from({ length: 42 }, (_, C) => Ve(b, C));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Ae(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((_, C) => /* @__PURE__ */ e.createElement("div", { key: "h" + C, className: "tlCalMiniWd" }, _)), k.map((_) => {
      const C = new Date(_).getMonth() === h.getMonth(), w = i.includes(new Date(_).getDay()), N = lt(_, u), x = r.has(ir(_));
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
  const t = X(), n = ne(), a = ue(or), o = t.granularity ?? "WEEK", i = t.rangeStart ?? Date.now(), s = t.anchor ?? i, u = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: ur(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(pr, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(gr, { ctx: r, rangeStart: i, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(vr, { ctx: r, rangeStart: i }) : /* @__PURE__ */ e.createElement(hr, { ctx: r, rangeStart: i, granularity: o })));
}, _r = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Cn = e.createContext(_r), { useMemo: Cr, useRef: yr, useState: wr, useEffect: kr } = e, Nr = 320, Sr = "TLTableView", Dr = "TLPanel", Tr = ({ controlId: l }) => {
  var _;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, i = t.children ?? [], s = t.noModelMessage, u = yr(null), [r, c] = wr(
    a === "top" ? "top" : "side"
  );
  kr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const C = u.current;
    if (!C) return;
    const w = new ResizeObserver((N) => {
      for (const x of N) {
        const y = x.contentRect.width / n;
        c(y < Nr ? "top" : "side");
      }
    });
    return w.observe(C), () => w.disconnect();
  }, [a, n]);
  const d = Cr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = i.length === 1 ? i[0] : void 0, b = !!h && (h.module === Sr || h.module === Dr && ((_ = h.state) == null ? void 0 : _.bare) === !0), k = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Cn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: p, ref: u }, i.map((C, w) => /* @__PURE__ */ e.createElement(G, { key: w, control: C }))));
}, { useCallback: Rr } = e, Lr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, xr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Lr), o = t.headerControl ?? null, i = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || i.length > 0 || s, p = Rr(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, k) => /* @__PURE__ */ e.createElement(G, { key: k, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, k) => /* @__PURE__ */ e.createElement(G, { key: k, control: b }))));
}, { useContext: Mr, useState: Ir, useCallback: jr } = e, Pr = ({ controlId: l }) => {
  const t = X(), n = Mr(Cn), a = t.label ?? "", o = t.required === !0, i = t.error, s = t.errorIcon, u = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, b = t.hasTooltip === !0, k = t.field, _ = n.readOnly, [C, w] = Ir(!1), N = jr(() => w((D) => !D), []), x = m === "hidden", v = i != null, y = u != null && u.length > 0, g = [
    "tlFormField",
    `tlFormField--${m}`,
    _ ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    v ? "tlFormField--error" : "",
    !v && y ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: k })), !_ && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(jt, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !_ && !v && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, M) => /* @__PURE__ */ e.createElement("div", { key: M, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(jt, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !_ && c && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Br = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.iconCss, o = t.iconSrc, i = t.label, s = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), m = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
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
}, Ar = 20, Fr = () => {
  var y;
  const l = X(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((y = n.find((g) => g.selected)) == null ? void 0 : y.id) ?? null;
  e.useEffect(() => {
    var D;
    if (m == null)
      return;
    const g = (D = d.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    g && g.scrollIntoView({ block: "nearest" });
  }, [m]);
  const p = e.useCallback((g, D) => {
    t(D ? "collapse" : "expand", { nodeId: g });
  }, [t]), h = e.useCallback((g, D) => {
    var T;
    const M = window.getSelection();
    M && !M.isCollapsed && D.currentTarget.contains(M.anchorNode) || ((T = d.current) == null || T.focus({ preventScroll: !0 }), t("select", {
      nodeId: g,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), b = e.useCallback((g, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: g, x: D.clientX, y: D.clientY });
  }, [t]), k = e.useRef(null), _ = e.useCallback((g, D) => {
    const M = D.getBoundingClientRect(), T = g.clientY - M.top, K = M.height / 3;
    return T < K ? "above" : T > K * 2 ? "below" : "within";
  }, []), C = e.useCallback((g, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", g);
  }, []), w = e.useCallback((g, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const M = _(D, D.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: g, position: M }), k.current = null;
    }, 50);
  }, [t, _]), N = e.useCallback((g, D) => {
    D.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const M = _(D, D.currentTarget);
    t("drop", { nodeId: g, position: M });
  }, [t, _]), x = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
  }, [t]), v = e.useCallback((g) => {
    if (n.length === 0) return;
    let D = r;
    switch (g.key) {
      case "ArrowDown":
        g.preventDefault(), D = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        g.preventDefault(), D = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (g.preventDefault(), r >= 0 && r < n.length) {
          const M = n[r];
          if (M.expandable && !M.expanded) {
            t("expand", { nodeId: M.id });
            return;
          } else M.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (g.preventDefault(), r >= 0 && r < n.length) {
          const M = n[r];
          if (M.expanded) {
            t("collapse", { nodeId: M.id });
            return;
          } else {
            const T = M.depth;
            for (let K = r - 1; K >= 0; K--)
              if (n[K].depth < T) {
                D = K;
                break;
              }
          }
        }
        break;
      case "Enter":
        g.preventDefault(), r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: g.ctrlKey || g.metaKey,
          shiftKey: g.shiftKey
        });
        return;
      case " ":
        g.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        g.preventDefault(), D = 0;
        break;
      case "End":
        g.preventDefault(), D = n.length - 1;
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
      onKeyDown: v
    },
    n.map((g, D) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: g.id,
        role: "treeitem",
        "aria-expanded": g.expandable ? g.expanded : void 0,
        "aria-selected": g.selected,
        "aria-level": g.depth + 1,
        className: [
          "tlTreeView__node",
          g.selected ? "tlTreeView__node--selected" : "",
          D === r ? "tlTreeView__node--focused" : "",
          s === g.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === g.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === g.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: g.depth * Ar },
        draggable: o,
        onMouseDown: (M) => {
          (M.shiftKey || M.ctrlKey || M.metaKey || M.detail > 1) && M.preventDefault();
        },
        onClick: (M) => h(g.id, M),
        onContextMenu: (M) => b(g.id, M),
        onDragStart: (M) => C(g.id, M),
        onDragOver: i ? (M) => w(g.id, M) : void 0,
        onDrop: i ? (M) => N(g.id, M) : void 0,
        onDragEnd: x
      },
      g.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (M) => {
            M.stopPropagation(), p(g.id, g.expanded);
          },
          tabIndex: -1,
          "aria-label": g.expanded ? "Collapse" : "Expand"
        },
        g.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: g.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: g.content }))
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
var sn;
function Or() {
  if (sn) return J;
  sn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
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
  }, k = Object.assign, _ = {};
  function C(f, I, Y) {
    this.props = f, this.context = I, this.refs = _, this.updater = Y || b;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(f, I) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, I, "setState");
  }, C.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function w() {
  }
  w.prototype = C.prototype;
  function N(f, I, Y) {
    this.props = f, this.context = I, this.refs = _, this.updater = Y || b;
  }
  var x = N.prototype = new w();
  x.constructor = N, k(x, C.prototype), x.isPureReactComponent = !0;
  var v = Array.isArray;
  function y() {
  }
  var g = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function M(f, I, Y) {
    var H = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: I,
      ref: H !== void 0 ? H : null,
      props: Y
    };
  }
  function T(f, I) {
    return M(f.type, I, f.props);
  }
  function K(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function U(f) {
    var I = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return I[Y];
    });
  }
  var A = /\/+/g;
  function V(f, I) {
    return typeof f == "object" && f !== null && f.key != null ? U("" + f.key) : I.toString(36);
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
  function B(f, I, Y, H, Z) {
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
              return te = f._init, B(
                te(f._payload),
                I,
                Y,
                H,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = H === "" ? "." + V(f, 0) : H, v(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), B(Z, I, Y, "", function(pe) {
        return pe;
      })) : Z != null && (K(Z) && (Z = T(
        Z,
        Y + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), I.push(Z)), 1;
    te = 0;
    var se = H === "" ? "." : H + ":";
    if (v(f))
      for (var le = 0; le < f.length; le++)
        H = f[le], F = se + V(H, le), te += B(
          H,
          I,
          Y,
          F,
          Z
        );
    else if (le = h(f), typeof le == "function")
      for (f = le.call(f), le = 0; !(H = f.next()).done; )
        H = H.value, F = se + V(H, le++), te += B(
          H,
          I,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof f.then == "function")
        return B(
          P(f),
          I,
          Y,
          H,
          Z
        );
      throw I = String(f), Error(
        "Objects are not valid as a React child (found: " + (I === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : I) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function j(f, I, Y) {
    if (f == null) return f;
    var H = [], Z = 0;
    return B(f, H, "", "", function(F) {
      return I.call(Y, F, Z++);
    }), H;
  }
  function L(f) {
    if (f._status === -1) {
      var I = f._result;
      I = I(), I.then(
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 1, f._result = Y);
        },
        function(Y) {
          (f._status === 0 || f._status === -1) && (f._status = 2, f._result = Y);
        }
      ), f._status === -1 && (f._status = 0, f._result = I);
    }
    if (f._status === 1) return f._result.default;
    throw f._result;
  }
  var S = typeof reportError == "function" ? reportError : function(f) {
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
  }, $ = {
    map: j,
    forEach: function(f, I, Y) {
      j(
        f,
        function() {
          I.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var I = 0;
      return j(f, function() {
        I++;
      }), I;
    },
    toArray: function(f) {
      return j(f, function(I) {
        return I;
      }) || [];
    },
    only: function(f) {
      if (!K(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return J.Activity = m, J.Children = $, J.Component = C, J.Fragment = n, J.Profiler = o, J.PureComponent = N, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(f) {
      return g.H.useMemoCache(f);
    }
  }, J.cache = function(f) {
    return function() {
      return f.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(f, I, Y) {
    if (f == null)
      throw Error(
        "The argument must be a React element, but you passed " + f + "."
      );
    var H = k({}, f.props), Z = f.key;
    if (I != null)
      for (F in I.key !== void 0 && (Z = "" + I.key), I)
        !D.call(I, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && I.ref === void 0 || (H[F] = I[F]);
    var F = arguments.length - 2;
    if (F === 1) H.children = Y;
    else if (1 < F) {
      for (var te = Array(F), se = 0; se < F; se++)
        te[se] = arguments[se + 2];
      H.children = te;
    }
    return M(f.type, Z, H);
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
  }, J.createElement = function(f, I, Y) {
    var H, Z = {}, F = null;
    if (I != null)
      for (H in I.key !== void 0 && (F = "" + I.key), I)
        D.call(I, H) && H !== "key" && H !== "__self" && H !== "__source" && (Z[H] = I[H]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = Y;
    else if (1 < te) {
      for (var se = Array(te), le = 0; le < te; le++)
        se[le] = arguments[le + 2];
      Z.children = se;
    }
    if (f && f.defaultProps)
      for (H in te = f.defaultProps, te)
        Z[H] === void 0 && (Z[H] = te[H]);
    return M(f, F, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(f) {
    return { $$typeof: u, render: f };
  }, J.isValidElement = K, J.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: L
    };
  }, J.memo = function(f, I) {
    return {
      $$typeof: c,
      type: f,
      compare: I === void 0 ? null : I
    };
  }, J.startTransition = function(f) {
    var I = g.T, Y = {};
    g.T = Y;
    try {
      var H = f(), Z = g.S;
      Z !== null && Z(Y, H), typeof H == "object" && H !== null && typeof H.then == "function" && H.then(y, S);
    } catch (F) {
      S(F);
    } finally {
      I !== null && Y.types !== null && (I.types = Y.types), g.T = I;
    }
  }, J.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, J.use = function(f) {
    return g.H.use(f);
  }, J.useActionState = function(f, I, Y) {
    return g.H.useActionState(f, I, Y);
  }, J.useCallback = function(f, I) {
    return g.H.useCallback(f, I);
  }, J.useContext = function(f) {
    return g.H.useContext(f);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(f, I) {
    return g.H.useDeferredValue(f, I);
  }, J.useEffect = function(f, I) {
    return g.H.useEffect(f, I);
  }, J.useEffectEvent = function(f) {
    return g.H.useEffectEvent(f);
  }, J.useId = function() {
    return g.H.useId();
  }, J.useImperativeHandle = function(f, I, Y) {
    return g.H.useImperativeHandle(f, I, Y);
  }, J.useInsertionEffect = function(f, I) {
    return g.H.useInsertionEffect(f, I);
  }, J.useLayoutEffect = function(f, I) {
    return g.H.useLayoutEffect(f, I);
  }, J.useMemo = function(f, I) {
    return g.H.useMemo(f, I);
  }, J.useOptimistic = function(f, I) {
    return g.H.useOptimistic(f, I);
  }, J.useReducer = function(f, I, Y) {
    return g.H.useReducer(f, I, Y);
  }, J.useRef = function(f) {
    return g.H.useRef(f);
  }, J.useState = function(f) {
    return g.H.useState(f);
  }, J.useSyncExternalStore = function(f, I, Y) {
    return g.H.useSyncExternalStore(
      f,
      I,
      Y
    );
  }, J.useTransition = function() {
    return g.H.useTransition();
  }, J.version = "19.2.4", J;
}
var cn;
function $r() {
  return cn || (cn = 1, Rt.exports = Or()), Rt.exports;
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
var un;
function Hr() {
  if (un) return be;
  un = 1;
  var l = $r();
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
var dn;
function Wr() {
  if (dn) return Tt.exports;
  dn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Tt.exports = Hr(), Tt.exports;
}
var yn = Wr();
const { useState: Me, useCallback: ge, useRef: et, useEffect: We, useMemo: $t } = e;
function zt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Ur({
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
    /* @__PURE__ */ e.createElement(zt, { image: l.image }),
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
function zr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: i
}) {
  const s = ge(() => a(l.value), [a, l.value]), u = $t(() => {
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
    /* @__PURE__ */ e.createElement(zt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Vr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, i = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = i && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = h["js.dropdownSelect.nothingFound"], k = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, C] = Me(!1), [w, N] = Me(""), [x, v] = Me(-1), [y, g] = Me(!1), [D, M] = Me({}), [T, K] = Me(null), [U, A] = Me(null), [V, P] = Me(null), B = et(null), j = et(null), L = et(null), S = et(a);
  S.current = a;
  const $ = et(-1), f = $t(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), I = $t(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (w) {
      const q = w.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, w]);
  We(() => {
    w && I.length === 1 ? v(0) : v(-1);
  }, [I.length, w]), We(() => {
    _ && c && j.current && j.current.focus();
  }, [_, c, a]), We(() => {
    var ae, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const q = (ae = B.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = B.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!_) return;
    const O = (q) => {
      B.current && !B.current.contains(q.target) && L.current && !L.current.contains(q.target) && (C(!1), N(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), We(() => {
    if (!_ || !B.current) return;
    const O = B.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    M({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = ge(async () => {
    if (!(u || !r) && (C(!0), N(""), v(-1), g(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, r, c, n]), H = ge(() => {
    var O;
    C(!1), N(""), v(-1), (O = B.current) == null || O.focus();
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
      S.current = q, n(rt, { value: q.map((ae) => ae.value) }), o ? (N(""), v(-1)) : H();
    },
    [o, d, n, H]
  ), F = ge(
    (O) => {
      $.current = S.current.findIndex((ae) => ae.value === O);
      const q = S.current.filter((ae) => ae.value !== O);
      S.current = q, n(rt, { value: q.map((ae) => ae.value) });
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
          O.preventDefault(), O.stopPropagation(), v(
            (q) => q < I.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), v(
            (q) => q > 0 ? q - 1 : I.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), x >= 0 && x < I.length && Z(I[x].value);
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
      I,
      x,
      Z,
      w,
      o,
      a,
      F
    ]
  ), pe = ge(
    async (O) => {
      O.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
    },
    [n]
  ), ye = ge(
    (O, q) => {
      K(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), we = ge(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", T === null || T === O) {
        A(null), P(null);
        return;
      }
      const ae = q.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Xe = q.clientX < ie ? "before" : "after";
      A(O), P(Xe);
    },
    [T]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), T === null || U === null || V === null || T === U) return;
      const q = [...S.current], [ae] = q.splice(T, 1);
      let ie = U;
      T < U ? ie = V === "before" ? ie - 1 : ie : ie = V === "before" ? ie : ie + 1, q.splice(ie, 0, ae), S.current = q, n(rt, { value: q.map((Xe) => Xe.value) }), K(null), A(null), P(null);
    },
    [T, U, V, n]
  ), Re = ge(() => {
    K(null), A(null), P(null);
  }, []);
  if (We(() => {
    if (x < 0 || !L.current) return;
    const O = L.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(zt, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const Oe = !s && a.length > 0 && !u, $e = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...Gn
    },
    (c || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
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
      !c && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, h["js.dropdownSelect.error"])),
      c && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      c && I.map((O, q) => /* @__PURE__ */ e.createElement(
        zr,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === x,
          searchTerm: w,
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
      ref: B,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: _ ? void 0 : Y,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let ae = "";
      return T === q ? ae = "tlDropdownSelect__chip--dragging" : U === q && V === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : U === q && V === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ur,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !s),
          onRemove: F,
          removeLabel: k(O.label),
          draggable: p,
          onDragStart: p ? (ie) => ye(q, ie) : void 0,
          onDragOver: p ? (ie) => we(q, ie) : void 0,
          onDrop: p ? Te : void 0,
          onDragEnd: p ? Re : void 0,
          dragClassName: p ? ae : void 0
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
  ), $e && yn.createPortal($e, document.body));
}, { useCallback: Lt, useRef: Kr } = e, wn = "application/x-tl-color", Yr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: i
}) => {
  const s = Kr(null), u = Lt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Lt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = Lt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(wn);
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
function kn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Ht(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Nn(l) {
  if (!Ht(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Sn(l, t, n) {
  const a = (o) => kn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Gr(l, t, n) {
  const a = l / 255, o = t / 255, i = n / 255, s = Math.max(a, o, i), u = Math.min(a, o, i), r = s - u;
  let c = 0;
  r !== 0 && (s === a ? c = (o - i) / r % 6 : s === o ? c = (i - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function Xr(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let s = 0, u = 0, r = 0;
  return l < 60 ? (s = a, u = o, r = 0) : l < 120 ? (s = o, u = a, r = 0) : l < 180 ? (s = 0, u = a, r = o) : l < 240 ? (s = 0, u = o, r = a) : l < 300 ? (s = o, u = 0, r = a) : (s = a, u = 0, r = o), [
    Math.round((s + i) * 255),
    Math.round((u + i) * 255),
    Math.round((r + i) * 255)
  ];
}
function qr(l) {
  return Gr(...Nn(l));
}
function xt(l, t, n) {
  return Sn(...Xr(l, t, n));
}
const { useCallback: Ue, useRef: mn } = e, Zr = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = qr(l), i = mn(null), s = mn(null), u = Ue(
    (b, k) => {
      var N;
      const _ = (N = i.current) == null ? void 0 : N.getBoundingClientRect();
      if (!_) return;
      const C = Math.max(0, Math.min(1, (b - _.left) / _.width)), w = Math.max(0, Math.min(1, 1 - (k - _.top) / _.height));
      t(xt(n, C, w));
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
      const k = (w = s.current) == null ? void 0 : w.getBoundingClientRect();
      if (!k) return;
      const C = Math.max(0, Math.min(1, (b - k.top) / k.height)) * 360;
      t(xt(C, a, o));
    },
    [a, o, t]
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
  ), h = xt(n, 1, 1);
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
function Qr(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Jr = {
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
}, { useState: it, useCallback: ke, useEffect: pn, useRef: eo, useLayoutEffect: to } = e, no = ({
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
  const [c, d] = it("palette"), [m, p] = it(t), h = eo(null), b = ue(Jr), [k, _] = it(null);
  to(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), S = h.current.getBoundingClientRect();
    let $ = L.bottom + 4, f = L.left;
    $ + S.height > window.innerHeight && ($ = L.top - S.height - 4), f + S.width > window.innerWidth && (f = Math.max(0, L.right - S.width)), _({ top: $, left: f });
  }, [l]);
  const C = m != null, [w, N, x] = C ? Nn(m) : [0, 0, 0], [v, y] = it((m == null ? void 0 : m.toUpperCase()) ?? "");
  pn(() => {
    y((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Fe(!0, { ESCAPE: u }), pn(() => {
    const L = ($) => {
      h.current && !h.current.contains($.target) && u();
    }, S = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", L);
    };
  }, [u]);
  const g = ke(
    (L) => (S) => {
      const $ = parseInt(S.target.value, 10);
      if (isNaN($)) return;
      const f = kn($);
      p(Sn(L === "r" ? f : w, L === "g" ? f : N, L === "b" ? f : x));
    },
    [w, N, x]
  ), D = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(wn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const S = document.createElement("div");
        S.style.width = "33px", S.style.height = "33px", S.style.backgroundColor = m, S.style.borderRadius = "3px", S.style.border = "1px solid rgba(0,0,0,0.1)", S.style.position = "absolute", S.style.top = "-9999px", document.body.appendChild(S), L.dataTransfer.setDragImage(S, 16, 16), requestAnimationFrame(() => document.body.removeChild(S));
      }
    },
    [m]
  ), M = ke((L) => {
    const S = L.target.value;
    y(S), Ht(S) && p(S);
  }, []), T = ke(() => {
    p(null);
  }, []), K = ke((L) => {
    p(L);
  }, []), U = ke(
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
  ), P = ke(() => {
    r([...o]);
  }, [o, r]), B = ke(
    (L) => {
      if (Qr(n, L)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const $ = [...n];
      $[S] = L.toUpperCase(), r($);
    },
    [n, r]
  ), j = ke(() => {
    m != null && B(m), s(m);
  }, [m, s, B]);
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
      Yr,
      {
        colors: n,
        columns: a,
        onSelect: K,
        onConfirm: U,
        onSwap: A,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Zr, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: m } : void 0,
        draggable: C,
        onDragStart: C ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? w : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? N : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? x : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !Ht(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: M
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: j }, b["js.colorInput.ok"]))
  );
}, lo = { "js.colorInput.chooseColor": "Choose color" }, { useState: ao, useCallback: ut, useRef: ro } = e, oo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(lo), [s, u] = ao(!1), r = ro(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, b = ut(() => {
    d && u(!0);
  }, [d]), k = ut(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = ut(() => {
    u(!1);
  }, []), C = ut(
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
    no,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
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
      className: "tlColorInput tlColorInput--immutable" + (c == null ? " tlColorInput--noColor" : ""),
      style: c != null ? { backgroundColor: c } : void 0,
      title: c ?? ""
    }
  );
}, { useState: tt, useCallback: Pe, useEffect: Mt, useRef: fn, useLayoutEffect: so, useMemo: co } = e, io = {
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
}, uo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: i,
  onLoadIcons: s
}) => {
  const u = ue(io), [r, c] = tt("simple"), [d, m] = tt(""), [p, h] = tt(t ?? ""), [b, k] = tt(!1), [_, C] = tt(null), w = fn(null), N = fn(null);
  so(() => {
    if (!l.current || !w.current) return;
    const U = l.current.getBoundingClientRect(), A = w.current.getBoundingClientRect();
    let V = U.bottom + 4, P = U.left;
    V + A.height > window.innerHeight && (V = U.top - A.height - 4), P + A.width > window.innerWidth && (P = Math.max(0, U.right - A.width)), C({ top: V, left: P });
  }, [l]), Mt(() => {
    !a && !b && s().catch(() => k(!0));
  }, [a, b, s]), Mt(() => {
    a && N.current && N.current.focus();
  }, [a]), Fe(!0, { ESCAPE: i }), Mt(() => {
    const U = (V) => {
      w.current && !w.current.contains(V.target) && i();
    }, A = setTimeout(() => document.addEventListener("mousedown", U), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", U);
    };
  }, [i]);
  const x = co(() => {
    if (!d) return n;
    const U = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(U) || A.label.toLowerCase().includes(U) || A.terms != null && A.terms.some((V) => V.includes(U))
    );
  }, [n, d]), v = Pe((U) => {
    m(U.target.value);
  }, []), y = Pe(
    (U) => {
      o(U);
    },
    [o]
  ), g = Pe((U) => {
    h(U);
  }, []), D = Pe((U) => {
    h(U.target.value);
  }, []), M = Pe(() => {
    o(p || null);
  }, [p, o]), T = Pe(() => {
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
        ref: N,
        type: "text",
        className: "tlIconSelect__search",
        value: d,
        onChange: v,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && x.map(
        (U) => U.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: U.label,
            onClick: () => r === "simple" ? y(A.encoded) : g(A.encoded),
            onKeyDown: (V) => {
              (V.key === "Enter" || V.key === " ") && (V.preventDefault(), r === "simple" ? y(A.encoded) : g(A.encoded));
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
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: M }, u["js.iconSelect.ok"]))
  );
}, mo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: po, useCallback: dt, useRef: fo } = e, ho = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(mo), [s, u] = po(!1), r = fo(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, b = dt(() => {
    d && !m && u(!0);
  }, [d, m]), k = dt(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), _ = dt(() => {
    u(!1);
  }, []), C = dt(async () => {
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
    uo,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: k,
      onCancel: _,
      onLoadIcons: C
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: ze, useEffect: bo, useMemo: hn, useRef: go, useState: It } = e, vo = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Eo = [1, 2, 3, 4];
function _o(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function Co(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Eo)
    n >= o && (a = o);
  return a;
}
function yo(l, t) {
  const n = vo[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function wo(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, p) => !!(a[m] && a[m][p]), i = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, s = [];
  let u = 0, r = 0;
  const c = (m) => {
    let p = null;
    for (const b of s) b.rowStart === m && (p = b);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let b = p.rowStart; b < p.rowEnd; b++)
        for (let k = p.colEnd; k < h; k++) i(b, k);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(yo(m.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let N = r; N < n && !o(u, N); N++)
      b++;
    if (h > b) {
      for (c(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let N = r; N < n && !o(u, N); N++)
        b++;
      h = Math.min(h, b);
    }
    const k = r, _ = r + h, C = u, w = u + p;
    s.push({ id: m.id, colStart: k, colEnd: _, rowStart: C, rowEnd: w });
    for (let N = C; N < w; N++)
      for (let x = k; x < _; x++) i(N, x);
    r = _, r >= n && (r = 0, u++);
  }
  c(u);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (o(m, p)) continue;
      const h = s.find((b) => b.rowEnd === m && b.colStart <= p && p < b.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let b = h.colStart; b < h.colEnd; b++) i(m, b);
      }
    }
  return s;
}
const ko = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((y) => y && y.id), i = go(null), [s, u] = It(1), r = t.editMode === !0;
  bo(() => {
    const y = i.current;
    if (!y) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = _o(a, g), M = () => u(Co(y.clientWidth, D));
    M();
    const T = new ResizeObserver(M);
    return T.observe(y), () => T.disconnect();
  }, [a]);
  const c = hn(() => wo(o, s), [o, s]), d = hn(() => {
    const y = {};
    for (const g of c) y[g.id] = g;
    return y;
  }, [c]), [m, p] = It(null), [h, b] = It(null), k = ze((y, g) => {
    if (!r) {
      y.preventDefault();
      return;
    }
    p(g), y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", g);
  }, [r]), _ = ze((y, g) => {
    if (!r || !m || m === g) return;
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const D = y.currentTarget.getBoundingClientRect(), M = y.clientX < D.left + D.width / 2;
    b((T) => T && T.id === g && T.before === M ? T : { id: g, before: M });
  }, [r, m]), C = ze(() => {
  }, []), w = ze((y, g, D) => {
    const M = o.map((A) => A.id), T = M.indexOf(y);
    if (T < 0) return;
    M.splice(T, 1);
    const K = M.indexOf(g);
    if (K < 0) {
      M.splice(T, 0, y);
      return;
    }
    const U = D ? K : K + 1;
    M.splice(U, 0, y), n("reorder", { order: M });
  }, [o, n]), N = ze((y, g) => {
    if (!r || !m || m === g) return;
    y.preventDefault();
    const D = y.currentTarget.getBoundingClientRect(), M = y.clientX < D.left + D.width / 2;
    w(m, g, M), p(null), b(null);
  }, [r, m, w]), x = ze(() => {
    p(null), b(null);
  }, []), v = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: v }, o.map((y) => {
      const g = d[y.id];
      if (!g) return null;
      const D = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, M = ["tlDashboard__tile"];
      return m === y.id && M.push("tlDashboard__tile--dragging"), h && h.id === y.id && M.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: M.join(" "),
          style: D,
          draggable: r,
          onDragStart: (T) => k(T, y.id),
          onDragOver: (T) => _(T, y.id),
          onDragLeave: C,
          onDrop: (T) => N(T, y.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(G, { control: y.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: No, useRef: bn, useState: gn, useEffect: So, useLayoutEffect: Do } = e, To = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Ro = ({ group: l }) => {
  var m, p;
  const [t, n] = gn(!1), [a, o] = gn({}), i = bn(null), s = bn(null), u = No(() => {
    n((h) => !h);
  }, []);
  Do(() => {
    if (!t) return;
    const h = () => {
      const b = i.current;
      if (!b) return;
      const k = b.getBoundingClientRect();
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
  }, [t]), So(() => {
    if (!t) return;
    const h = (b) => {
      s.current && !s.current.contains(b.target) && i.current && !i.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Fe(t, { ESCAPE: () => n(!1) }), Ut(t, s, "first");
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
  ), yn.createPortal(
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
      (p = l.subGroups) == null ? void 0 : p.map((h, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((k, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: k })))))
    ),
    document.body
  ));
}, Lo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Ro, { group: o }) : /* @__PURE__ */ e.createElement(To, { group: o }))));
}, xo = ({ controlId: l }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(G, { control: t.frame }));
}, Mo = ({ controlId: l }) => {
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
}, Io = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, jo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Po = {
  "js.sidebar.openDrawer": "Open navigation"
}, Bo = ({ controlId: l }) => {
  const t = ne(), n = ue(Po);
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
W("TLButton", pl);
W("TLUploadButton", fl);
W("TLToggleButton", bl);
W("TLTextInput", Zn);
W("TLPasswordInput", Jn);
W("TLNumberInput", tl);
W("TLDatePicker", ll);
W("TLSelect", rl);
W("TLBooleanChoice", sl);
W("TLCheckbox", dl);
W("TLCounter", gl);
W("TLTabBar", El);
W("TLFieldList", _l);
W("TLAudioRecorder", yl);
W("TLAudioPlayer", kl);
W("TLFileUpload", Sl);
W("TLBinaryField", Tl);
W("TLFileChips", xl);
W("TLRelativeTime", jl);
W("TLAnchor", Pl);
W("TLScrollLink", Bl);
W("TLAvatar", Ol);
W("TLDownload", Hl);
W("TLPhotoCapture", Ul);
W("TLPhotoViewer", Vl);
W("TLPdfViewer", Yl);
W("TLSplitPanel", Gl);
W("TLPanel", ta);
W("TLInset", pa);
W("TLMaximizeRoot", na);
W("TLDeckPane", la);
W("TLSidebar", da);
W("TLStack", ma);
W("TLGrid", fa);
W("TLCard", ha);
W("TLAppBar", ba);
W("TLBreadcrumb", va);
W("TLBottomBar", _a);
W("TLDialog", wa);
W("TLDialogManager", Sa);
W("TLWindow", La);
W("TLDrawer", Ia);
W("TLMenuRegion", Pa);
W("TLSnackbar", Oa);
W("TLNoticeBar", Ka);
W("TLMenu", Ga);
W("TLAppShell", qa);
W("TLText", Za);
W("TLTableView", lr);
W("TLColumnSelect", rr);
W("TLCalendar", Er);
W("TLFormLayout", Tr);
W("TLFormGroup", xr);
W("TLFormField", Pr);
W("TLResourceCell", Br);
W("TLTreeView", Fr);
W("TLDropdownSelect", Vr);
W("TLColorInput", oo);
W("TLIconSelect", ho);
W("TLDashboard", ko);
W("TLToolbar", Lo);
W("TLTileStack", xo);
W("TLAdaptiveDetail", Mo);
W("TLSlot", Io);
W("TLSlotContent", jo);
W("TLDrawerToggle", Bo);
