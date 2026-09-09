import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as G, useKeyboardBinding as me, useTLUpload as Ye, TLChild as q, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as Kn, useStandaloneKeyboardScope as Fe, KeyboardScopeProvider as Ht, useFocusTrap as Wt, CMD_VALUE_CHANGED as rt, anchoredOverlayProps as Yn, register as W } from "tl-react-bridge";
const { useCallback: Gt, useRef: Gn } = e, Xn = 300, qn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: Xn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = ne(), c = Gn(!1), u = Gt(
    (k) => {
      c.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, s = Gt(async () => {
    await o(), r && c.current && (c.current = !1, i("commit"));
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
  const f = t.hasError === !0, m = t.hasWarnings === !0, h = t.errorMessage, b = [
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
      onBlur: s,
      disabled: t.disabled === !0,
      className: b,
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
      onBlur: s,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": f || void 0,
      title: f && h ? h : void 0
    }
  ));
}, { useCallback: Xt } = e, Zn = 300, Qn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: Zn }), i = Xt(
    (f) => {
      a(f.target.value);
    },
    [a]
  ), c = Xt(() => {
    o();
  }, [o]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, s = t.errorMessage, d = [
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
      onBlur: c,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && s ? s : void 0
    }
  ));
}, { useCallback: qt } = e, Jn = 300, el = ({ controlId: l, state: t, config: n }) => {
  const [a, o, i] = De({
    debounceMs: Jn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), c = qt(
    (h) => {
      const b = h.target.value;
      o(b === "" ? null : b);
    },
    [o]
  ), u = qt(() => {
    i();
  }, [i]), r = a == null ? "" : String(a);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r);
  const s = t.hasError === !0, d = t.hasWarnings === !0, f = t.errorMessage, m = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && d ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r,
      onChange: c,
      onBlur: u,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": s || void 0,
      title: s && f ? f : void 0
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
  const i = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && c ? "tlReactDatePicker--warning" : ""
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
}, { useCallback: ll } = e, al = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), i = ll(
    (f) => {
      o(f.target.value || null);
    },
    [o]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const f = ((d = c.find((m) => m.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, f);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, s = [
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
      className: s,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((f) => /* @__PURE__ */ e.createElement("option", { key: f.value, value: f.value }, f.label))
  ));
}, { useCallback: rl } = e, ol = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], i = t.presentation === "select", c = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, s = rl(
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
  return i ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: f + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: c,
      "aria-invalid": u || void 0,
      onChange: (m) => s(Number(m.target.value))
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
        disabled: c,
        onChange: () => s(h)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, m.label)))
  );
}, { useCallback: sl, useRef: cl, useEffect: il } = e, ul = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, i = cl(null);
  il(() => {
    i.current && (i.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const c = sl(
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
  const u = t.hasError === !0, r = t.hasWarnings === !0, s = [
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
      onChange: c,
      disabled: t.disabled === !0,
      className: s,
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
const { useCallback: dl } = e, ml = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: i }) => {
  const c = G(), u = ne(), r = t ?? "click", s = n ?? c.label, d = a ?? c.image, f = o ?? c.disabled === !0, m = i ?? c.displayMode ?? "label-only", h = c.hidden === !0, b = c.tooltip, k = c.appearance, _ = c.size, C = c.navigateUrl, w = dl(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(r);
  }, [u, r, C]), N = c.keyGesture;
  me(N, () => f || h ? !1 : (w(), !0));
  const x = m === "icon-only", E = m === "label-only" || m === "icon-label" || x && !d, y = b ?? (x ? s : void 0), g = y ? `text:${y}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: f,
      className: "tlReactButton" + (x ? " tlReactButton--iconOnly" : "") + (m === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": g,
      "aria-label": d || x ? s : void 0
    },
    d && /* @__PURE__ */ e.createElement(Se, { encoded: d, className: "tlReactButton__image" }),
    E && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  );
}, pl = ({ controlId: l }) => {
  const t = G(), n = Ye(), a = e.useRef(null), [o, i] = e.useState(!1), c = t.label ?? "", u = t.image, r = t.disabled === !0, s = t.hidden === !0, d = t.displayMode ?? "label-only", f = t.appearance, m = t.accept, h = t.multiple === !0, b = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), k = e.useCallback(async (x) => {
    const E = x.target.files;
    if (!E || E.length === 0) return;
    const y = new FormData();
    for (let g = 0; g < E.length; g++)
      y.append("file", E[g], E[g].name);
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
      accept: m && m !== "*" ? m : void 0,
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
      style: s ? { display: "none" } : void 0,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (f === "link" ? " tlReactButton--link" : "") + (f === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? c : void 0
    },
    C && u && /* @__PURE__ */ e.createElement(Se, { encoded: u, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  ));
}, { useCallback: fl } = e, hl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const i = G(), c = ne(), u = t ?? "click", r = n ?? i.label, s = a ?? i.active === !0, d = o ?? i.disabled === !0, f = fl(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: f,
      disabled: d,
      className: "tlReactButton" + (s ? " tlReactButtonActive" : "")
    },
    r
  );
}, bl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: gl } = e, El = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.tabs ?? [], o = t.activeTabId, i = gl((c) => {
    c !== o && n("selectTab", { tabId: c });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === o,
      className: "tlReactTabBar__tab" + (c.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(c.id)
    },
    c.icon && /* @__PURE__ */ e.createElement(Se, { encoded: c.icon, className: "tlReactTabBar__tabIcon" }),
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, vl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: o })))));
}, _l = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Cl = ({ controlId: l }) => {
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [i, c] = e.useState(null), u = e.useRef(null), r = e.useRef([]), s = e.useRef(null), d = t.status ?? "idle", f = t.error, m = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
    if (a === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (a !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        s.current = w, r.current = [];
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(w, N ? { mimeType: N } : void 0);
        u.current = x, x.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, x.onstop = async () => {
          w.getTracks().forEach((g) => g.stop()), s.current = null;
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
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), c("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), b = ue(_l), k = m === "recording" ? b["js.audioRecorder.stop"] : m === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], _ = m === "uploading", C = ["tlAudioRecorder__button"];
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, yl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, wl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [i, c] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), s = e.useRef(o);
  e.useEffect(() => {
    a ? i === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), c("disabled"));
  }, [a]), e.useEffect(() => {
    o !== s.current && (s.current = o, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (i === "playing" || i === "paused" || i === "loading") && c("idle"));
  }, [o]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      u.current && u.current.pause(), c("paused");
      return;
    }
    if (i === "paused" && u.current) {
      u.current.play(), c("playing");
      return;
    }
    if (!r.current) {
      c("loading");
      try {
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), c("idle");
          return;
        }
        const C = await _.blob();
        r.current = URL.createObjectURL(C);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), c("idle");
        return;
      }
    }
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      c("idle");
    }, k.play(), c("playing");
  }, [i, n]), f = ue(yl), m = i === "loading" ? f["js.loading"] : i === "playing" ? f["js.audioPlayer.pause"] : i === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], h = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: h,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, kl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Nl = ({ controlId: l }) => {
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [i, c] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", s = t.error, d = t.accept ?? "", f = r === "received" ? "idle" : a !== "idle" ? a : r, m = e.useCallback(async (E) => {
    o("uploading");
    const y = new FormData();
    y.append("file", E, E.name), await n(y), o("idle");
  }, [n]), h = e.useCallback((E) => {
    var g;
    const y = (g = E.target.files) == null ? void 0 : g[0];
    y && m(y);
  }, [m]), b = e.useCallback(() => {
    var E;
    a !== "uploading" && ((E = u.current) == null || E.click());
  }, [a]), k = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), c(!0);
  }, []), _ = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), c(!1);
  }, []), C = e.useCallback((E) => {
    var g;
    if (E.preventDefault(), E.stopPropagation(), c(!1), a === "uploading") return;
    const y = (g = E.dataTransfer.files) == null ? void 0 : g[0];
    y && m(y);
  }, [a, m]), w = f === "uploading", N = ue(kl), x = f === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
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
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: w,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    s && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, s)
  );
}, Sl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Dl = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, o = Ye(), i = Ge(), c = ue(Sl), u = a.editable !== !1, r = !!a.hasData, s = a.fileName ?? "download", d = a.dataRevision ?? 0, f = a.accept ?? "", m = a.status ?? "idle", h = a.error ?? null, [b, k] = e.useState("idle"), [_, C] = e.useState(!1), [w, N] = e.useState(!1), x = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || w)) {
      N(!0);
      try {
        const j = i + (i.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(j);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const S = await L.blob(), $ = URL.createObjectURL(S), p = document.createElement("a");
        p.href = $, p.download = s, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL($);
      } catch (j) {
        console.error("[TLBinaryField] Fetch error:", j);
      } finally {
        N(!1);
      }
    }
  }, [r, w, i, d, s]), y = e.useCallback(async (j) => {
    k("uploading");
    const L = new FormData();
    L.append("file", j, j.name), await o(L), k("idle");
  }, [o]), g = (m === "received" ? "idle" : b !== "idle" ? b : m) === "uploading", R = e.useCallback((j) => {
    var S;
    const L = (S = j.target.files) == null ? void 0 : S[0];
    L && y(L);
  }, [y]), I = e.useCallback(() => {
    var j;
    g || (j = x.current) == null || j.click();
  }, [g]), D = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!0);
  }, []), K = e.useCallback((j) => {
    j.preventDefault(), j.stopPropagation(), C(!1);
  }, []), U = e.useCallback((j) => {
    var S;
    if (j.preventDefault(), j.stopPropagation(), C(!1), g) return;
    const L = (S = j.dataTransfer.files) == null ? void 0 : S[0];
    L && y(L);
  }, [g, y]), A = w ? c["js.downloading"] : c["js.download.file"].replace("{0}", s), V = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: E,
      disabled: w,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, V) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const P = g, B = g ? c["js.uploading"] : c["js.fileUpload.choose"];
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
        title: B,
        "aria-label": B
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
  const t = G(), n = ne(), a = Ye(), o = Ge(), i = ue(Tl), c = t.chips ?? [], u = t.editable === !0, [r, s] = e.useState(!1), [d, f] = e.useState(!1), m = e.useRef(null), h = e.useCallback(async (E) => {
    const y = Array.from(E);
    if (y.length !== 0) {
      s(!0);
      try {
        const g = new FormData();
        for (const R of y)
          g.append("file", R, R.name);
        await a(g);
      } finally {
        s(!1);
      }
    }
  }, [a]), b = e.useCallback(async (E) => {
    if (E.hasData)
      try {
        const y = o + "&key=" + encodeURIComponent(E.key), g = await fetch(y);
        if (!g.ok) {
          console.error("[TLFileChips] Failed to fetch data:", g.status);
          return;
        }
        const R = await g.blob(), I = URL.createObjectURL(R), D = document.createElement("a");
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
    c.map((E) => {
      const y = i["js.download.file"].replace("{0}", E.name), g = i["js.fileChips.remove"].replace("{0}", E.name);
      return /* @__PURE__ */ e.createElement("span", { key: E.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(E),
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
}, xl = 3e4;
function Il(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Ml = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const c = setInterval(() => i((u) => u + 1), xl);
    return () => clearInterval(c);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Il(n, o));
}, jl = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, Pl = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (i) => {
    i.preventDefault(), Kn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function Bl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Al(l) {
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
      style: { backgroundColor: `hsl(${Al(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Bl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Ol = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, $l = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = ne(), o = !!t.hasData, i = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [r, s] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      s(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + i, k = await fetch(b);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const _ = await k.blob(), C = URL.createObjectURL(_), w = document.createElement("a");
        w.href = C, w.download = c, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(C);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        s(!1);
      }
    }
  }, [o, r, n, i, c]), f = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), m = ue(Ol);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, m["js.download.noFile"]));
  const h = r ? m["js.downloading"] : m["js.download.file"].replace("{0}", c);
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
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
  const t = G(), n = Ye(), [a, o] = e.useState("idle"), [i, c] = e.useState(null), [u, r] = e.useState(!1), s = e.useRef(null), d = e.useRef(null), f = e.useRef(null), m = e.useRef(null), h = e.useRef(null), b = t.error, k = e.useMemo(
    () => {
      var D;
      return !!(window.isSecureContext && ((D = navigator.mediaDevices) != null && D.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((D) => D.stop()), d.current = null), s.current && (s.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    _(), o("idle");
  }, [_]), w = e.useCallback(async () => {
    var D;
    if (a !== "uploading") {
      if (c(null), !k) {
        (D = m.current) == null || D.click();
        return;
      }
      try {
        const K = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = K, o("overlayOpen");
      } catch (K) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", K), c("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, k]), N = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const D = s.current, K = f.current;
    if (!D || !K)
      return;
    K.width = D.videoWidth, K.height = D.videoHeight;
    const U = K.getContext("2d");
    U && (U.drawImage(D, 0, 0), _(), o("uploading"), K.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", A, "capture.jpg"), await n(V), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), x = e.useCallback(async (D) => {
    var A;
    const K = (A = D.target.files) == null ? void 0 : A[0];
    if (!K) return;
    o("uploading");
    const U = new FormData();
    U.append("photo", K, K.name), await n(U), o("idle"), m.current && (m.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && s.current && d.current && (s.current.srcObject = d.current);
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
  const E = ue(Hl), y = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const R = ["tlPhotoCapture__overlayVideo"];
  u && R.push("tlPhotoCapture__overlayVideo--mirrored");
  const I = ["tlPhotoCapture__mirrorBtn"];
  return u && I.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
        ref: s,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Ul = {
  "js.photoViewer.alt": "Captured photo"
}, zl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [i, c] = e.useState(null), u = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      i && (URL.revokeObjectURL(i), c(null));
      return;
    }
    if (o === u.current && i)
      return;
    u.current = o, i && (URL.revokeObjectURL(i), c(null));
    let s = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const f = await d.blob();
        s || c(URL.createObjectURL(f));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      s = !0;
    };
  }, [a, o, n]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const r = ue(Ul);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, Vl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Kl = ({ controlId: l }) => {
  const t = G(), n = Ge(), a = !!t.hasPdf, o = t.dataRevision ?? 0, i = ue(Vl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, s = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(s);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: Zt, useRef: yt } = e, Yl = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.orientation, o = t.resizable === !0, i = t.children ?? [], c = a === "horizontal", u = i.length > 0 && i.every((_) => _.collapsed), r = !u && i.some((_) => _.collapsed), s = u ? !c : c, d = yt(null), f = yt(null), m = yt(null), h = Zt((_, C) => {
    const w = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? u && !s ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : C !== void 0 ? w.flex = `0 0 ${C}px` : w.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (w.minWidth = c ? _.minSize : void 0, w.minHeight = c ? void 0 : _.minSize), w;
  }, [c, u, r, s]), b = Zt((_, C) => {
    _.preventDefault();
    const w = d.current;
    if (!w) return;
    const N = i[C], x = i[C + 1], E = w.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    E.forEach((I) => {
      y.push(c ? I.offsetWidth : I.offsetHeight);
    }), m.current = y, f.current = {
      splitterIndex: C,
      startPos: c ? _.clientX : _.clientY,
      startSizeBefore: y[C],
      startSizeAfter: y[C + 1],
      childBefore: N,
      childAfter: x
    };
    const g = (I) => {
      const D = f.current;
      if (!D || !m.current) return;
      const U = (c ? I.clientX : I.clientY) - D.startPos, A = D.childBefore.minSize || 0, V = D.childAfter.minSize || 0;
      let P = D.startSizeBefore + U, B = D.startSizeAfter - U;
      P < A && (B += P - A, P = A), B < V && (P += B - V, B = V), m.current[D.splitterIndex] = P, m.current[D.splitterIndex + 1] = B;
      const j = w.querySelectorAll(":scope > .tlSplitPanel__child"), L = j[D.splitterIndex], S = j[D.splitterIndex + 1];
      L && (L.style.flex = `0 0 ${P}px`), S && (S.style.flex = `0 0 ${B}px`);
    }, R = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", R), document.body.style.cursor = "", document.body.style.userSelect = "", m.current) {
        const I = {};
        i.forEach((D, K) => {
          const U = D.control;
          U != null && U.controlId && m.current && (I[U.controlId] = m.current[K]);
        }), n("updateSizes", { sizes: I });
      }
      m.current = null, f.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", R), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, c, n]), k = [];
  return i.forEach((_, C) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${_.collapsed && s ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: h(_)
        },
        /* @__PURE__ */ e.createElement(q, { control: _.control })
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
        flexDirection: s ? "row" : "column",
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
  const t = G(), n = ne(), a = ue(Gl), o = t.title, i = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, s = t.fullLine === !0, d = t.fill === !0, f = t.hoverActions === !0, m = t.appearance === "card", h = t.errorMessage, b = i === "MINIMIZED", k = i === "MAXIMIZED", _ = i === "HIDDEN", C = wt(() => {
    n("toggleMinimize");
  }, [n]), w = wt(() => {
    n("toggleMaximize");
  }, [n]), N = wt(() => {
    n("popOut");
  }, [n]);
  if (_)
    return null;
  const x = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, E = c && !k || u && !b || r, y = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || E;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${s ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${f ? " tlPanel--hoverActions" : ""}${m ? " tlPanel--card" : ""}`,
      style: x
    },
    y && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(q, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(q, { control: t.toolbar }), c && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(ql, null) : /* @__PURE__ */ e.createElement(Xl, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
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
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child })),
    !b && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(pt, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(q, { control: t.buttonBar }))
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
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      o.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? o.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (o.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && o.push(...Pt(i.children, t, n, i.id)));
  return o;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Se, { encoded: l, className: "tlSidebar__icon" }) : null, aa = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: i, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: i,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ra = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: i }) => /* @__PURE__ */ e.createElement(
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
), oa = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), sa = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ca = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: i }) => {
  const c = ft(null);
  jt(() => {
    const s = (d) => {
      c.current && !c.current.contains(d.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [i]), Fe(!0, { ESCAPE: i });
  const u = Ee((s) => {
    s.type === "nav" ? (a(s.id), i()) : s.type === "command" && (o(s.id), i());
  }, [a, o, i]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((s) => {
    if (s.type === "nav" && s.hidden) return null;
    if (s.type === "nav" || s.type === "command") {
      const d = s.type === "nav" && s.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(s)
        },
        /* @__PURE__ */ e.createElement(Ke, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, ia = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: o,
  onExecute: i,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: r,
  onFocus: s,
  focusedId: d,
  setItemRef: f,
  onItemFocus: m,
  flyoutGroupId: h,
  onOpenFlyout: b,
  onCloseFlyout: k
}) => {
  const _ = ft(null), [C, w] = dt(null), N = Ee(() => {
    a ? h === l.id ? k() : (_.current && w(_.current.getBoundingClientRect()), b(l.id)) : c(l.id);
  }, [a, h, l.id, c, b, k]), x = Ee((y) => {
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
      onFocus: () => s(l.id)
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
      onExecute: i,
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
      onExecute: i,
      onToggleGroup: c,
      focusedId: d,
      setItemRef: f,
      onItemFocus: m,
      groupStates: null,
      flyoutGroupId: h,
      onOpenFlyout: b,
      onCloseFlyout: k
    }
  ))));
}, gn = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: o,
  onToggleGroup: i,
  focusedId: c,
  setItemRef: u,
  onItemFocus: r,
  groupStates: s,
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
          tabIndex: c === l.id ? 0 : -1,
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
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(oa, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(sa, null);
    case "group": {
      const h = s ? s.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ia,
        {
          item: l,
          expanded: h,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: o,
          onToggleGroup: i,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: c,
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
  const t = G(), n = ne(), a = ue(la), o = t.items ?? [], i = t.activeItemId, c = t.collapsed, u = t.drawerOpen, r = u ? !1 : c, [s, d] = dt(() => {
    const A = /* @__PURE__ */ new Map(), V = (P) => {
      for (const B of P)
        B.type === "group" && (A.set(B.id, B.expanded), V(B.children));
    };
    return V(o), A;
  }), f = Ee((A) => {
    d((V) => {
      const P = new Map(V), B = P.get(A) ?? !1;
      return P.set(A, !B), n("toggleGroup", { itemId: A, expanded: !B }), P;
    });
  }, [n]), m = Ee((A) => {
    A !== i && n("selectItem", { itemId: A });
  }, [n, i]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), b = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), k = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [_, C] = dt(null), w = Ee((A) => {
    C(A);
  }, []), N = Ee(() => {
    C(null);
  }, []);
  jt(() => {
    r || C(null);
  }, [r]);
  const [x, E] = dt(() => {
    const A = Pt(o, r, s);
    return A.length > 0 ? A[0].id : "";
  }), y = ft(/* @__PURE__ */ new Map()), g = Ee((A) => (V) => {
    V ? y.current.set(A, V) : y.current.delete(A);
  }, []), R = Ee((A) => {
    E(A);
  }, []), I = ft(0), D = Ee((A) => {
    E(A), I.current++;
  }, []);
  jt(() => {
    const A = y.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, I.current]);
  const K = Ee((A) => {
    if (A.key === "Escape" && _ !== null) {
      A.preventDefault(), N();
      return;
    }
    const V = Pt(o, r, s);
    if (V.length === 0) return;
    const P = V.findIndex((j) => j.id === x);
    if (P < 0) return;
    const B = V[P];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const j = (P + 1) % V.length;
        D(V[j].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const j = (P - 1 + V.length) % V.length;
        D(V[j].id);
        break;
      }
      case "Home": {
        A.preventDefault(), D(V[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), D(V[V.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), B.type === "nav" ? m(B.id) : B.type === "command" ? h(B.id) : B.type === "group" && (r ? _ === B.id ? N() : w(B.id) : f(B.id));
        break;
      }
      case "ArrowRight": {
        B.type === "group" && !r && ((s.get(B.id) ?? !1) || (A.preventDefault(), f(B.id)));
        break;
      }
      case "ArrowLeft": {
        B.type === "group" && !r && (s.get(B.id) ?? !1) && (A.preventDefault(), f(B.id));
        break;
      }
    }
  }, [
    o,
    r,
    s,
    x,
    _,
    D,
    m,
    h,
    f,
    w,
    N
  ]), U = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: U }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(q, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, o.map((A) => /* @__PURE__ */ e.createElement(
    gn,
    {
      key: A.id,
      item: A,
      activeItemId: i,
      collapsed: r,
      onSelect: m,
      onExecute: h,
      onToggleGroup: f,
      focusedId: x,
      setItemRef: g,
      onItemFocus: R,
      groupStates: s,
      flyoutGroupId: _,
      onOpenFlyout: w,
      onCloseFlyout: N
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, da = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", i = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    i ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : "",
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((s, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: s })));
}, ma = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(q, { control: t.child }));
}, pa = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", i = t.children ?? [], c = {};
  return a ? c.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: c }, i.map((u, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: u })));
}, fa = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", i = t.headerActions ?? [], c = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((r, s) => /* @__PURE__ */ e.createElement(q, { key: s, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(q, { control: c })));
}, ha = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, o = t.children ?? [], i = t.actions ?? [], c = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, o.map((s, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: s }))), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((s, d) => /* @__PURE__ */ e.createElement(q, { key: d, control: s }))));
}, { useCallback: ba } = e, ga = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = ba((i) => {
    n("navigate", { itemId: i });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((i, c) => {
    const u = c === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: i.id, className: "tlBreadcrumb__entry" }, c > 0 && /* @__PURE__ */ e.createElement(
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
}, { useCallback: Ea } = e, va = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.items ?? [], o = t.activeItemId, i = Ea((c) => {
    c !== o && n("selectItem", { itemId: c });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((c) => {
    const u = c.id === o;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: c.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => i(c.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + c.icon, "aria-hidden": "true" }), c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, c.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, c.label)
    );
  }));
}, { useCallback: Qt, useRef: _a } = e, Ca = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ya = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, i = t.child, c = _a(null), u = Qt(() => {
    n("close");
  }, [n]), r = Qt((s) => {
    o && s.target === s.currentTarget && u();
  }, [o, u]);
  return a ? /* @__PURE__ */ e.createElement(Ht, null, /* @__PURE__ */ e.createElement(Ca, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(q, { control: i })
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
  const t = G(), n = ne(), a = ue(Da), o = t.title ?? "", i = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, s = t.child, d = t.actions ?? [], f = t.toolbar, m = t.buttonBar, [h, b] = st(null), [k, _] = st(null), [C, w] = st(null), N = He(null), [x, E] = st(!1), y = He(null), g = He(null), R = He(null), I = He(null), D = He(null), K = ot(() => {
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
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), g.current = Te, R.current = Re, b(Te), _(Re);
      const Oe = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      N.current = Oe, w(Oe);
    }, H = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", H);
      const Z = g.current, F = R.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), D.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", H);
  }, [n]), A = ot((j) => {
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
      S && (w(S.x !== -1 ? { x: S.x, y: S.y } : null), b(S.w), _(S.h)), E(!1);
    } else {
      const S = I.current, $ = S == null ? void 0 : S.getBoundingClientRect();
      y.current = {
        x: ((j = N.current) == null ? void 0 : j.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = N.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: k ?? null
      }, E(!0), w({ x: 0, y: 0 }), b(null), _(null);
    }
  }, [x, h, k]), P = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...k != null ? { height: k + "px" } : c != null ? { height: c } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, B = l + "-title";
  return /* @__PURE__ */ e.createElement(Ht, { modal: !0 }, /* @__PURE__ */ e.createElement(Sa, { onClose: K }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: I,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(q, { control: s })),
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
  const t = G(), n = ne(), a = ue(xa), o = t.open === !0, i = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, r = t.child, s = La(() => {
    n("close");
  }, [n]);
  Fe(o, { ESCAPE: s });
  const d = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${c}`,
    o ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !o }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: s,
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
  const t = G(), n = ne(), a = t.child, o = Ma((i) => {
    i.preventDefault(), i.stopPropagation(), n("openContextMenu", { x: i.clientX, y: i.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: o }, a && /* @__PURE__ */ e.createElement(q, { control: a }));
}, { useCallback: Pa, useEffect: Jt, useRef: Ba, useState: en } = e, Aa = 250, Fa = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.message ?? "", o = t.content ?? "", i = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [s, d] = en(!1), [f, m] = en(!1), h = Ba(!1);
  Jt(() => {
    h.current = !1;
  }, [r]);
  const b = Pa(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Jt(() => {
    if (!u || c === 0 || f) return;
    const k = setTimeout(b, h.current ? Aa : c);
    return () => clearTimeout(k);
  }, [u, c, f, b]), !u && !s ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${s ? " tlSnackbar--exiting" : ""}`,
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
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), i = (c) => c < 10 ? `0${c}` : `${c}`;
  return o > 0 ? `${o}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const Va = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", i = t.text ?? "", c = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, s = t.actionLabel ?? null, d = t.pingGraceMs ?? null, f = $a(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [m, h] = Wa(0), b = a && c != null;
  tn(() => {
    if (!b) return;
    const x = setInterval(() => h((E) => E + 1), Ua);
    return () => clearInterval(x);
  }, [b, c]);
  const k = Ha(null);
  tn(() => {
    !b || d == null || c == null || k.current !== c && (Date.now() + f < c + d || (k.current = c, n("deadlinePassed", {})));
  }, [m, b, c, d, f, n]);
  const _ = Oa(() => {
    s != null && n("action", {});
  }, [n, s]);
  if (!a) return null;
  const C = c != null ? c - (Date.now() + f) : null;
  if (r != null && C != null && C > r) return null;
  const w = C != null ? za(C) : null, N = s != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${N ? " tlNoticeBar--clickable" : ""}`,
      role: N ? "button" : "status",
      "aria-live": "polite",
      tabIndex: N ? 0 : void 0,
      title: s ?? void 0,
      "aria-label": N ? `${i} ${s}` : void 0,
      onClick: N ? _ : void 0,
      onKeyDown: N ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    w !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, w)
  );
}, { useCallback: kt, useEffect: nn, useRef: Ka, useState: ln } = e, Ya = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.open === !0, o = t.anchorId, i = t.anchorX, c = t.anchorY, u = t.items ?? [], r = Ka(null), [s, d] = ln({ top: 0, left: 0 }), [f, m] = ln(0), h = u.filter((C) => C.type === "item" && !C.disabled);
  nn(() => {
    var g, R;
    if (!a) return;
    const C = ((g = r.current) == null ? void 0 : g.offsetHeight) ?? 200, w = ((R = r.current) == null ? void 0 : R.offsetWidth) ?? 200;
    if (i != null && c != null) {
      let I = c, D = i;
      I + C > window.innerHeight && (I = Math.max(0, window.innerHeight - C)), D + w > window.innerWidth && (D = Math.max(0, window.innerWidth - w)), d({ top: I, left: D }), m(0);
      return;
    }
    if (!o) return;
    const N = document.getElementById(o);
    if (!N) return;
    const x = N.getBoundingClientRect();
    let E = x.bottom + 4, y = x.left;
    E + C > window.innerHeight && (E = x.top - C - 4), y + w > window.innerWidth && (y = x.right - w), d({ top: E, left: y }), m(0);
  }, [a, o, i, c]);
  const b = kt(() => {
    n("close");
  }, [n]), k = kt((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  nn(() => {
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
      C.preventDefault(), m((w) => (w + 1) % h.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), m((w) => (w - 1 + h.length) % h.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const w = h[f];
      w && k(w.id);
    }
  }, [b, k, h, f]);
  return Wt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: s.top, left: s.left },
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
    const r = window.matchMedia(`(max-width: ${Ga}px)`), s = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    s(r.matches);
    const d = (f) => s(f.matches);
    return r.addEventListener("change", d), () => r.removeEventListener("change", d);
  }, [n]);
  const a = t.header, o = t.notices, i = t.content, c = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: a })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(q, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: i })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: c })), /* @__PURE__ */ e.createElement(q, { control: u }));
}, qa = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", o = t.hasTooltip === !0, i = t.role || void 0, c = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      role: i,
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
const Bt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', tr = Bt + ", button:not([disabled]), a[href]";
function En(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function St(l, t, n = {}) {
  const a = En(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), c = i == null ? void 0 : i.querySelector(Bt);
    if (c) return c;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const i of o) {
    const c = i.querySelector(Bt);
    if (c) return c;
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
  const i = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, s = t.selectionMode ?? "single", d = t.selectedCount ?? 0, f = t.cursorIndex ?? -1, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, b = t.columnSelect ?? !1, k = t.filterBar ?? !1, _ = t.namedFilters ?? [], C = t.activeNamedFilter ?? "", w = t.search ?? "", N = t.filterSaving ?? !1, x = e.useMemo(
    () => i.filter((v) => v.sortPriority && v.sortPriority > 0).length,
    [i]
  ), E = s === "multi", y = 40, g = 20, R = e.useRef(null), I = e.useRef(null), D = e.useRef(null), K = e.useRef(null), U = e.useRef(null), [A, V] = e.useState({}), P = e.useRef(null), B = e.useRef(!1), j = e.useRef(null), [L, S] = e.useState(null), [$, p] = e.useState(null), [M, Y] = e.useState(null), [H, Z] = e.useState(0);
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
  }, [i]);
  const F = e.useCallback((v) => A[v.name] ?? v.width, [A]), te = e.useMemo(() => {
    const v = [];
    let T = E && m > 0 ? y : 0;
    for (let z = 0; z < m && z < i.length; z++)
      v.push(T), T += F(i[z]);
    return v;
  }, [i, m, E, y, F]), se = e.useMemo(() => {
    if (m <= 0)
      return 0;
    let v = E ? y : 0;
    for (let T = 0; T < m && T < i.length; T++)
      v += F(i[T]);
    return v;
  }, [i, m, E, y, F]), le = c * r, pe = e.useRef(null), ye = e.useCallback((v, T, z) => {
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
        n("columnResize", { column: de.column, width: _e }), P.current = null, B.current = !0, requestAnimationFrame(() => {
          B.current = !1;
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
    if (B.current) return;
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
    let z = i.findIndex((ee) => ee.name === L.column);
    if (z < 0) {
      j.current = null, S(null);
      return;
    }
    const Q = i.findIndex((ee) => ee.name === T);
    L.side === "right" && z++, Q < z && z--, n("columnReorder", { column: T, targetIndex: z }), j.current = null, S(null);
  }, [i, L, n]), O = e.useCallback(() => {
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
    if (de < 0 || de >= c)
      return;
    const _e = u.find((xe) => xe.index === de);
    _e && St(T, _e.id) || (v.preventDefault(), U.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, c, n]), Tn = e.useCallback((v, T) => {
    T.stopPropagation(), n("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Rn = e.useCallback(() => {
    const v = d === c && c > 0;
    n("selectAll", { selected: !v });
  }, [n, d, c]), Ln = e.useCallback((v, T, z) => {
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
  }, [n]), Bn = e.useCallback((v) => {
    v.stopPropagation(), v.preventDefault(), n("openColumnSelect", {});
  }, [n]), [An, zt] = e.useState(w), Et = e.useRef(!1), Le = e.useRef(null);
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
  }, [vt]), _t = i.reduce((v, T) => v + F(T), 0) + (E ? y : 0), Ct = b ? 32 : 0, Un = d === c && c > 0, Vt = d > 0 && d < c, zn = e.useCallback((v) => {
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
        value: An,
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
            j.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== j.current && S({ column: i[0].name, side: "left" }));
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
      i.map((v, T) => {
        const z = F(v);
        i.length - 1;
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
            if (j.current && i.length > 0) {
              const T = i[i.length - 1];
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
    ), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Bn
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
        i.map((T, z) => {
          const Q = F(T), ee = z === i.length - 1, re = z < m, oe = z === m - 1;
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
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * g } }, v.expandable ? /* @__PURE__ */ e.createElement(
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
  const t = G(), n = ne(), a = ue(lr), o = t.entries ?? [], i = o.filter((E) => E.visible).length, [c, u] = e.useState(""), r = c.trim().toLowerCase(), s = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), f = e.useRef(null), [m, h] = e.useState(null), b = e.useCallback((E) => {
    f.current = E, h(E);
  }, []), k = e.useCallback((E, y) => {
    n("columnVisible", { column: E, visible: y });
  }, [n]), _ = e.useCallback((E, y) => {
    d.current = E, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", E);
  }, []), C = e.useCallback((E, y) => {
    if (!d.current || d.current === E) {
      b(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const g = y.currentTarget.getBoundingClientRect(), R = y.clientY < g.top + g.height / 2 ? "top" : "bottom";
    b({ name: E, side: R });
  }, [b]), w = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), N = e.useCallback((E) => {
    E.preventDefault();
    const y = d.current, g = f.current;
    if (d.current = null, b(null), !y || !g)
      return;
    const R = o.findIndex((K) => K.name === g.name), I = o.findIndex((K) => K.name === y);
    if (R < 0 || I < 0)
      return;
    let D = g.side === "top" ? R : R + 1;
    I < D && D--, D !== I && n("columnReorder", { column: y, targetIndex: D });
  }, [o, n, b]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: N }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: c,
      onChange: (E) => u(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, s.map((E) => {
    const y = E.visible && i <= 1;
    let g = "tlColumnSelect__row";
    return m && m.name === E.name && (g += " tlColumnSelect__row--dragOver-" + m.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: g,
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
}, { useState: At, useRef: nt, useCallback: mt, useMemo: Be, useEffect: rn } = e, rr = {
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
function Ae(l, t, n) {
  return new Intl.DateTimeFormat(l, t).format(new Date(n));
}
function ur(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Ae(l, n, t.start) + "–" + Ae(l, n, t.end);
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
  const t = [...l].sort((c, u) => c.start - u.start || u.end - c.end), n = [];
  let a = [], o = -1;
  const i = () => {
    const c = a.reduce((u, r) => Math.max(u, r.col + 1), 0);
    for (const u of a)
      u.cols = c;
    n.push(...a), a = [], o = -1;
  };
  for (const c of t) {
    a.length > 0 && c.start >= o && i();
    const u = new Set(a.filter((s) => s.ev.end > c.start).map((s) => s.col));
    let r = 0;
    for (; u.has(r); )
      r++;
    a.push({
      ev: c,
      topMin: Me(c.start),
      botMin: Me(c.start) + Math.max(15, (c.end - c.start) / Ce),
      col: r,
      cols: 1
    }), o = Math.max(o, c.end);
  }
  return a.length > 0 && i(), n;
}
const Dt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ft = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const i = nt(!1), c = (u) => {
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
        u.stopPropagation(), u.key === "Enter" ? c(u.currentTarget.value) : u.key === "Escape" && c(null);
      },
      onBlur: () => c(null)
    }
  ));
}, vn = (l) => {
  const [t, n] = At(null), a = nt(null);
  a.current = t;
  const o = mt((u) => n(u), []), i = mt(() => n(null), []), c = mt(
    (u) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: u }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: c, discard: i };
}, fr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: i, dayStartHour: c, dayEndHour: u, now: r, send: s, editable: d, i18n: f } = l, m = Be(() => {
    const P = n === "DAY" ? 1 : 7, B = [];
    for (let j = 0; j < P; j++) {
      const L = Ve(t, j);
      n === "WORK_WEEK" && i.includes(new Date(L).getDay()) || B.push(L);
    }
    return B;
  }, [t, n, i]), h = vn(s), b = nt(null), k = nt(null), [_, C] = At(null), w = nt(null);
  w.current = _;
  const [N, x] = At(Date.now());
  rn(() => {
    const P = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const E = mt(
    (P, B) => {
      const j = b.current;
      if (!j)
        return { dayIndex: 0, min: 0 };
      const L = j.getBoundingClientRect(), S = L.width / m.length, $ = Je(Math.floor((P - L.left) / S), 0, m.length - 1), p = B - L.top + j.scrollTop, M = Je(p / ve * 60, 0, 1440);
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
    }, B = () => {
      const L = w.current;
      if (C(null), !!L)
        if (L.mode === "move") {
          const S = L.dayStart + L.startMin * Ce;
          S !== L.origStartMs && s("moveEvent", { eventId: L.id, start: S, end: S + L.dur * Ce });
        } else if (L.mode === "resize") {
          const S = L.dayStart + L.endMin * Ce;
          S !== L.origEndMs && s("resizeEvent", { eventId: L.id, end: S });
        } else {
          const S = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - S >= ht && h.open({ start: L.dayStart + S * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, j = () => C(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", B, { once: !0 }), window.addEventListener("pointercancel", j), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", B), window.removeEventListener("pointercancel", j);
    };
  }, [_, m, E, s, h.open]);
  const y = (P, B, j) => {
    if (!d || !B.movable)
      return;
    P.stopPropagation(), Dt(P), h.discard();
    const { min: L } = E(P.clientX, P.clientY), S = (B.end - B.start) / Ce;
    C({
      mode: "move",
      id: B.id,
      grabMin: L - Me(B.start),
      dur: S,
      dayStart: j,
      startMin: Me(B.start),
      origStartMs: B.start
    });
  }, g = (P, B, j) => {
    !d || !B.resizable || (P.stopPropagation(), Dt(P), h.discard(), C({
      mode: "resize",
      id: B.id,
      dayStart: j,
      startMin: Me(B.start),
      endMin: Me(B.end),
      origEndMs: B.end
    }));
  }, R = (P, B) => {
    if (!d || P.button !== 0)
      return;
    Dt(P), h.discard();
    const { min: j } = E(P.clientX, P.clientY);
    C({ mode: "create", dayStart: B, fromMin: Qe(j), toMin: Qe(j) });
  }, I = Array.from({ length: 24 }, (P, B) => B), D = Be(() => {
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
  }, [a, _]), K = Be(() => m.map(
    (P) => pr(
      D.filter((B) => !B.allDay && B.start < P + je && B.end > P)
    )
  ), [m, D]), U = Be(() => m.map((P) => D.filter((B) => B.allDay && B.start < P + je && B.end > P)), [m, D]), A = c * ve, V = u * ve;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), m.map((P) => {
    const B = i.includes(new Date(P).getDay()), j = lt(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (B ? " tlCalDayHead--nonworking" : "") + (j ? " tlCalDayHead--today" : ""),
        onClick: () => s("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Ae(o, { weekday: "short" }, P)),
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayNum" }, new Date(P).getDate())
    );
  })), /* @__PURE__ */ e.createElement("div", { className: "tlCalAllDayRow" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter tlCalAllDayLabel" }, f["js.calendar.allDay"]), m.map((P, B) => /* @__PURE__ */ e.createElement(
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
    U[B].map((j) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: j.id,
        className: "tlCalAllDayEvent " + bt(j.category) + (j.selected ? " tlCalEvent--selected" : ""),
        style: gt(j),
        title: j.tooltip,
        onClick: (L) => {
          L.stopPropagation(), s("selectEvent", { eventId: j.id });
        }
      },
      j.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: k }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * ve } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, I.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * ve } }, P === 0 ? "" : Ae(o, { hour: "numeric" }, Ne(t) + P * or)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: b, style: { gridTemplateColumns: `repeat(${m.length}, 1fr)` } }, m.map((P, B) => {
    const j = i.includes(new Date(P).getDay()), L = _ && ("dayStart" in _ && _.dayStart === P) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (j ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => R(S, P)
      },
      I.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * ve } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: V - A } }),
      lt(P, N) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Me(Date.now()) / 60 * ve } }),
      K[B].map((S) => {
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
              H.stopPropagation(), s("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, ur(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (H) => g(H, S.ev, P) })
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
  const { events: a, locale: o, nonWorkingDays: i, send: c, editable: u, now: r, i18n: s } = l, d = vn(c), f = Be(() => {
    const h = [];
    for (let b = 0; b < 6; b++) {
      const k = [];
      for (let _ = 0; _ < 7; _++)
        k.push(Ve(t, b * 7 + _));
      h.push(k);
    }
    return h;
  }, [t]), m = (h, b) => {
    h.preventDefault();
    const k = h.dataTransfer.getData("text/plain"), _ = a.find((w) => w.id === k);
    if (!_ || !u || !_.movable)
      return;
    const C = b - Ne(_.start);
    c("moveEvent", { eventId: k, start: _.start + C, end: _.end + C });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, f[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Ae(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, f.map((h, b) => {
    const k = h[0], _ = Ve(k, 7), C = a.filter((N) => (N.allDay || N.end - N.start >= je) && N.start < _ && N.end > k).sort((N, x) => N.start - x.start).slice(0, 3), w = C.length;
    return /* @__PURE__ */ e.createElement("div", { key: b, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((N) => {
      const x = new Date(N).getMonth() === new Date(n).getMonth(), E = i.includes(new Date(N).getDay()), y = lt(N, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (g) => g.preventDefault(),
          onDrop: (g) => m(g, N),
          onClick: () => u && d.open({ start: N, end: N + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (y ? " tlCalMonthDayNum--today" : ""),
            onClick: (g) => {
              g.stopPropagation(), c("goto", { date: N, granularity: "DAY" });
            }
          },
          new Date(N).getDate()
        ),
        d.pending && d.pending.start === N && /* @__PURE__ */ e.createElement(
          Ft,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: s["js.calendar.newEventTitle"],
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
          onDragStart: (g) => g.dataTransfer.setData("text/plain", N.id),
          title: N.tooltip,
          onClick: (g) => {
            g.stopPropagation(), c("selectEvent", { eventId: N.id });
          }
        },
        N.title
      );
    }), h.map((N, x) => {
      const E = a.filter((R) => !R.allDay && R.end - R.start < je && lt(R.start, N)).sort((R, I) => R.start - I.start), y = E.slice(0, hr), g = E.length - y.length;
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
            D.stopPropagation(), c("selectEvent", { eventId: R.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Ae(o, { hour: "numeric", minute: "2-digit" }, R.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, R.title)
      )).concat(
        g > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + N,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: w + 1 + y.length },
              onClick: () => c("goto", { date: N, granularity: "DAY" })
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
}, gr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: i, send: c, now: u } = l, r = Be(() => {
    const m = /* @__PURE__ */ new Set();
    for (const h of n) {
      let b = Ne(h.start);
      const k = h.end;
      for (; b < k; )
        m.add(b), b = Ve(b, 1);
    }
    return m;
  }, [n]), s = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (m, h) => new Date(s, h, 1).getTime()), f = Be(() => {
    const m = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, b) => {
      const k = new Date(m);
      return k.setDate(m.getDate() + (o + b) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(k);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((m) => {
    const h = new Date(m), b = Ne(Ve(m, -((h.getDay() - o + 7) % 7))), k = Array.from({ length: 42 }, (_, C) => Ve(b, C));
    return /* @__PURE__ */ e.createElement("div", { key: m, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => c("goto", { date: m, granularity: "MONTH" })
      },
      Ae(a, { month: "long" }, m)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, f.map((_, C) => /* @__PURE__ */ e.createElement("div", { key: "h" + C, className: "tlCalMiniWd" }, _)), k.map((_) => {
      const C = new Date(_).getMonth() === h.getMonth(), w = i.includes(new Date(_).getDay()), N = lt(_, u), x = r.has(cr(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (C ? "" : " tlCalMiniDay--other") + (w ? " tlCalMiniDay--nonworking" : "") + (N ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => c("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, Er = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(rr), o = t.granularity ?? "WEEK", i = t.rangeStart ?? Date.now(), c = t.anchor ?? i, u = t.title ?? "", r = {
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
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(mr, { title: u, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(br, { ctx: r, rangeStart: i, anchorMonth: c }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(gr, { ctx: r, rangeStart: i }) : /* @__PURE__ */ e.createElement(fr, { ctx: r, rangeStart: i, granularity: o })));
}, vr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, _n = e.createContext(vr), { useMemo: _r, useRef: Cr, useState: yr, useEffect: wr } = e, kr = 320, Nr = "TLTableView", Sr = "TLPanel", Dr = ({ controlId: l }) => {
  var _;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, i = t.children ?? [], c = t.noModelMessage, u = Cr(null), [r, s] = yr(
    a === "top" ? "top" : "side"
  );
  wr(() => {
    if (a !== "auto") {
      s(a);
      return;
    }
    const C = u.current;
    if (!C) return;
    const w = new ResizeObserver((N) => {
      for (const x of N) {
        const y = x.contentRect.width / n;
        s(y < kr ? "top" : "side");
      }
    });
    return w.observe(C), () => w.disconnect();
  }, [a, n]);
  const d = _r(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), m = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = i.length === 1 ? i[0] : void 0, b = !!h && (h.module === Nr || h.module === Sr && ((_ = h.state) == null ? void 0 : _.bare) === !0), k = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(_n.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: m, ref: u }, i.map((C, w) => /* @__PURE__ */ e.createElement(q, { key: w, control: C }))));
}, { useCallback: Tr } = e, Rr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Lr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = ue(Rr), o = t.headerControl ?? null, i = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", s = t.fullLine === !0, d = t.children ?? [], f = o != null || i.length > 0 || c, m = Tr(() => {
    n("toggleCollapse");
  }, [n]), h = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    s ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(q, { control: o })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, k) => /* @__PURE__ */ e.createElement(q, { key: k, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, k) => /* @__PURE__ */ e.createElement(q, { key: k, control: b }))));
}, { useContext: xr, useState: Ir, useCallback: Mr } = e, jr = ({ controlId: l }) => {
  const t = G(), n = xr(_n), a = t.label ?? "", o = t.required === !0, i = t.error, c = t.errorIcon, u = t.warnings, r = t.warningIcon, s = t.helpText, d = t.dirty === !0, f = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, h = t.visible !== !1, b = t.hasTooltip === !0, k = t.field, _ = n.readOnly, [C, w] = Ir(!1), N = Mr(() => w((R) => !R), []), x = f === "hidden", E = i != null, y = u != null && u.length > 0, g = [
    "tlFormField",
    `tlFormField--${f}`,
    _ ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && y ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), o && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), s && !_ && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: k })), !_ && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(pt, { image: c, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !_ && !E && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((R, I) => /* @__PURE__ */ e.createElement("div", { key: I, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(pt, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, R)))), !_ && s && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, s));
}, Pr = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.iconCss, o = t.iconSrc, i = t.label, c = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, s, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), f = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
  }, [n]), m = ["tlResourceCell", c].filter(Boolean).join(" "), h = u ? "key:tooltip" : void 0;
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
}, Br = 20, Ar = () => {
  var y;
  const l = G(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, s] = e.useState(-1), d = e.useRef(null), f = ((y = n.find((g) => g.selected)) == null ? void 0 : y.id) ?? null;
  e.useEffect(() => {
    var R;
    if (f == null)
      return;
    const g = (R = d.current) == null ? void 0 : R.querySelector(".tlTreeView__node--selected");
    g && g.scrollIntoView({ block: "nearest" });
  }, [f]);
  const m = e.useCallback((g, R) => {
    t(R ? "collapse" : "expand", { nodeId: g });
  }, [t]), h = e.useCallback((g, R) => {
    var D;
    const I = window.getSelection();
    I && !I.isCollapsed && R.currentTarget.contains(I.anchorNode) || ((D = d.current) == null || D.focus({ preventScroll: !0 }), t("select", {
      nodeId: g,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    }));
  }, [t]), b = e.useCallback((g, R) => {
    R.preventDefault(), t("contextMenu", { nodeId: g, x: R.clientX, y: R.clientY });
  }, [t]), k = e.useRef(null), _ = e.useCallback((g, R) => {
    const I = R.getBoundingClientRect(), D = g.clientY - I.top, K = I.height / 3;
    return D < K ? "above" : D > K * 2 ? "below" : "within";
  }, []), C = e.useCallback((g, R) => {
    R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", g);
  }, []), w = e.useCallback((g, R) => {
    R.preventDefault(), R.dataTransfer.dropEffect = "move";
    const I = _(R, R.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: g, position: I }), k.current = null;
    }, 50);
  }, [t, _]), N = e.useCallback((g, R) => {
    R.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const I = _(R, R.currentTarget);
    t("drop", { nodeId: g, position: I });
  }, [t, _]), x = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
  }, [t]), E = e.useCallback((g) => {
    if (n.length === 0) return;
    let R = r;
    switch (g.key) {
      case "ArrowDown":
        g.preventDefault(), R = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        g.preventDefault(), R = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (g.preventDefault(), r >= 0 && r < n.length) {
          const I = n[r];
          if (I.expandable && !I.expanded) {
            t("expand", { nodeId: I.id });
            return;
          } else I.expanded && (R = r + 1);
        }
        break;
      case "ArrowLeft":
        if (g.preventDefault(), r >= 0 && r < n.length) {
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
        g.preventDefault(), R = 0;
        break;
      case "End":
        g.preventDefault(), R = n.length - 1;
        break;
      default:
        return;
    }
    R !== r && s(R);
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
    n.map((g, R) => /* @__PURE__ */ e.createElement(
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
          R === r ? "tlTreeView__node--focused" : "",
          c === g.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === g.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === g.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: g.depth * Br },
        draggable: o,
        onMouseDown: (I) => {
          (I.shiftKey || I.ctrlKey || I.metaKey || I.detail > 1) && I.preventDefault();
        },
        onClick: (I) => h(g.id, I),
        onContextMenu: (I) => b(g.id, I),
        onDragStart: (I) => C(g.id, I),
        onDragOver: i ? (I) => w(g.id, I) : void 0,
        onDrop: i ? (I) => N(g.id, I) : void 0,
        onDragEnd: x
      },
      g.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (I) => {
            I.stopPropagation(), m(g.id, g.expanded);
          },
          tabIndex: -1,
          "aria-label": g.expanded ? "Collapse" : "Expand"
        },
        g.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: g.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(q, { control: g.content }))
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
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), s = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), f = Symbol.for("react.activity"), m = Symbol.iterator;
  function h(p) {
    return p === null || typeof p != "object" ? null : (p = m && p[m] || p["@@iterator"], typeof p == "function" ? p : null);
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
  function C(p, M, Y) {
    this.props = p, this.context = M, this.refs = _, this.updater = Y || b;
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
    this.props = p, this.context = M, this.refs = _, this.updater = Y || b;
  }
  var x = N.prototype = new w();
  x.constructor = N, k(x, C.prototype), x.isPureReactComponent = !0;
  var E = Array.isArray;
  function y() {
  }
  var g = { H: null, A: null, T: null, S: null }, R = Object.prototype.hasOwnProperty;
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
  var A = /\/+/g;
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
  function B(p, M, Y, H, Z) {
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
              return te = p._init, B(
                te(p._payload),
                M,
                Y,
                H,
                Z
              );
          }
      }
    if (te)
      return Z = Z(p), te = H === "" ? "." + V(p, 0) : H, E(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), B(Z, M, Y, "", function(pe) {
        return pe;
      })) : Z != null && (K(Z) && (Z = D(
        Z,
        Y + (Z.key == null || p && p.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var se = H === "" ? "." : H + ":";
    if (E(p))
      for (var le = 0; le < p.length; le++)
        H = p[le], F = se + V(H, le), te += B(
          H,
          M,
          Y,
          F,
          Z
        );
    else if (le = h(p), typeof le == "function")
      for (p = le.call(p), le = 0; !(H = p.next()).done; )
        H = H.value, F = se + V(H, le++), te += B(
          H,
          M,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof p.then == "function")
        return B(
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
    return B(p, H, "", "", function(F) {
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
  return J.Activity = f, J.Children = $, J.Component = C, J.Fragment = n, J.Profiler = o, J.PureComponent = N, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(p) {
      return g.H.useMemoCache(p);
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
      $$typeof: c,
      _currentValue: p,
      _currentValue2: p,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, p.Provider = p, p.Consumer = {
      $$typeof: i,
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
      $$typeof: s,
      type: p,
      compare: M === void 0 ? null : M
    };
  }, J.startTransition = function(p) {
    var M = g.T, Y = {};
    g.T = Y;
    try {
      var H = p(), Z = g.S;
      Z !== null && Z(Y, H), typeof H == "object" && H !== null && typeof H.then == "function" && H.then(y, S);
    } catch (F) {
      S(F);
    } finally {
      M !== null && Y.types !== null && (M.types = Y.types), g.T = M;
    }
  }, J.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, J.use = function(p) {
    return g.H.use(p);
  }, J.useActionState = function(p, M, Y) {
    return g.H.useActionState(p, M, Y);
  }, J.useCallback = function(p, M) {
    return g.H.useCallback(p, M);
  }, J.useContext = function(p) {
    return g.H.useContext(p);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(p, M) {
    return g.H.useDeferredValue(p, M);
  }, J.useEffect = function(p, M) {
    return g.H.useEffect(p, M);
  }, J.useEffectEvent = function(p) {
    return g.H.useEffectEvent(p);
  }, J.useId = function() {
    return g.H.useId();
  }, J.useImperativeHandle = function(p, M, Y) {
    return g.H.useImperativeHandle(p, M, Y);
  }, J.useInsertionEffect = function(p, M) {
    return g.H.useInsertionEffect(p, M);
  }, J.useLayoutEffect = function(p, M) {
    return g.H.useLayoutEffect(p, M);
  }, J.useMemo = function(p, M) {
    return g.H.useMemo(p, M);
  }, J.useOptimistic = function(p, M) {
    return g.H.useOptimistic(p, M);
  }, J.useReducer = function(p, M, Y) {
    return g.H.useReducer(p, M, Y);
  }, J.useRef = function(p) {
    return g.H.useRef(p);
  }, J.useState = function(p) {
    return g.H.useState(p);
  }, J.useSyncExternalStore = function(p, M, Y) {
    return g.H.useSyncExternalStore(
      p,
      M,
      Y
    );
  }, J.useTransition = function() {
    return g.H.useTransition();
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
    var s = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      s += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        s += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + r + "; visit " + s + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function i(r, s, d) {
    var f = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: o,
      key: f == null ? null : "" + f,
      children: r,
      containerInfo: s,
      implementation: d
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, s) {
    if (r === "font") return "";
    if (typeof s == "string")
      return s === "use-credentials" ? s : "";
  }
  return be.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, be.createPortal = function(r, s) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!s || s.nodeType !== 1 && s.nodeType !== 9 && s.nodeType !== 11)
      throw Error(t(299));
    return i(r, s, null, d);
  }, be.flushSync = function(r) {
    var s = c.T, d = a.p;
    try {
      if (c.T = null, a.p = 2, r) return r();
    } finally {
      c.T = s, a.p = d, a.d.f();
    }
  }, be.preconnect = function(r, s) {
    typeof r == "string" && (s ? (s = s.crossOrigin, s = typeof s == "string" ? s === "use-credentials" ? s : "" : void 0) : s = null, a.d.C(r, s));
  }, be.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, be.preinit = function(r, s) {
    if (typeof r == "string" && s && typeof s.as == "string") {
      var d = s.as, f = u(d, s.crossOrigin), m = typeof s.integrity == "string" ? s.integrity : void 0, h = typeof s.fetchPriority == "string" ? s.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof s.precedence == "string" ? s.precedence : void 0,
        {
          crossOrigin: f,
          integrity: m,
          fetchPriority: h
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: f,
        integrity: m,
        fetchPriority: h,
        nonce: typeof s.nonce == "string" ? s.nonce : void 0
      });
    }
  }, be.preinitModule = function(r, s) {
    if (typeof r == "string")
      if (typeof s == "object" && s !== null) {
        if (s.as == null || s.as === "script") {
          var d = u(
            s.as,
            s.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: d,
            integrity: typeof s.integrity == "string" ? s.integrity : void 0,
            nonce: typeof s.nonce == "string" ? s.nonce : void 0
          });
        }
      } else s == null && a.d.M(r);
  }, be.preload = function(r, s) {
    if (typeof r == "string" && typeof s == "object" && s !== null && typeof s.as == "string") {
      var d = s.as, f = u(d, s.crossOrigin);
      a.d.L(r, d, {
        crossOrigin: f,
        integrity: typeof s.integrity == "string" ? s.integrity : void 0,
        nonce: typeof s.nonce == "string" ? s.nonce : void 0,
        type: typeof s.type == "string" ? s.type : void 0,
        fetchPriority: typeof s.fetchPriority == "string" ? s.fetchPriority : void 0,
        referrerPolicy: typeof s.referrerPolicy == "string" ? s.referrerPolicy : void 0,
        imageSrcSet: typeof s.imageSrcSet == "string" ? s.imageSrcSet : void 0,
        imageSizes: typeof s.imageSizes == "string" ? s.imageSizes : void 0,
        media: typeof s.media == "string" ? s.media : void 0
      });
    }
  }, be.preloadModule = function(r, s) {
    if (typeof r == "string")
      if (s) {
        var d = u(s.as, s.crossOrigin);
        a.d.m(r, {
          as: typeof s.as == "string" && s.as !== "script" ? s.as : void 0,
          crossOrigin: d,
          integrity: typeof s.integrity == "string" ? s.integrity : void 0
        });
      } else a.d.m(r);
  }, be.requestFormReset = function(r) {
    a.d.r(r);
  }, be.unstable_batchedUpdates = function(r, s) {
    return r(s);
  }, be.useFormState = function(r, s, d) {
    return c.H.useFormState(r, s, d);
  }, be.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
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
  onDragStart: i,
  onDragOver: c,
  onDrop: u,
  onDragEnd: r,
  dragClassName: s
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
      className: "tlDropdownSelect__chip" + (s ? " " + s : ""),
      draggable: o || void 0,
      onDragStart: i,
      onDragOver: c,
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
  id: i
}) {
  const c = ge(() => a(l.value), [a, l.value]), u = Ot(() => {
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
      onClick: c,
      onMouseEnter: o
    },
    /* @__PURE__ */ e.createElement(Ut, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const zr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, i = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, s = t.optionsLoaded === !0, d = t.options ?? [], f = t.emptyOptionLabel ?? "", m = i && o && !u && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = h["js.dropdownSelect.nothingFound"], k = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, C] = Ie(!1), [w, N] = Ie(""), [x, E] = Ie(-1), [y, g] = Ie(!1), [R, I] = Ie({}), [D, K] = Ie(null), [U, A] = Ie(null), [V, P] = Ie(null), B = et(null), j = et(null), L = et(null), S = et(a);
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
    _ && s && j.current && j.current.focus();
  }, [_, s, a]), We(() => {
    var ae, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const X = (ae = B.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    X && X.length > 0 ? X[Math.min(O, X.length - 1)].focus() : (ie = B.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!_) return;
    const O = (X) => {
      B.current && !B.current.contains(X.target) && L.current && !L.current.contains(X.target) && (C(!1), N(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), We(() => {
    if (!_ || !B.current) return;
    const O = B.current.getBoundingClientRect(), X = window.innerHeight - O.bottom, ie = X < 300 && O.top > X;
    I({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = ge(async () => {
    if (!(u || !r) && (C(!0), N(""), E(-1), g(!1), !s))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, r, s, n]), H = ge(() => {
    var O;
    C(!1), N(""), E(-1), (O = B.current) == null || O.focus();
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
      O.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
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
        A(null), P(null);
        return;
      }
      const ae = X.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, Xe = X.clientX < ie ? "before" : "after";
      A(O), P(Xe);
    },
    [D]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), D === null || U === null || V === null || D === U) return;
      const X = [...S.current], [ae] = X.splice(D, 1);
      let ie = U;
      D < U ? ie = V === "before" ? ie - 1 : ie : ie = V === "before" ? ie : ie + 1, X.splice(ie, 0, ae), S.current = X, n(rt, { value: X.map((Xe) => Xe.value) }), K(null), A(null), P(null);
    },
    [D, U, V, n]
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
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ut, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const Oe = !c && a.length > 0 && !u, $e = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: R,
      ...Yn
    },
    (s || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
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
      !s && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, h["js.dropdownSelect.error"])),
      s && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      s && M.map((O, X) => /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, f) : a.map((O, X) => {
      let ae = "";
      return D === X ? ae = "tlDropdownSelect__chip--dragging" : U === X && V === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : U === X && V === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Wr,
        {
          key: O.value,
          option: O,
          removable: !u && (o || !c),
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
  onReplace: i
}) => {
  const c = Vr(null), u = Lt(
    (d) => (f) => {
      c.current = d, f.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Lt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), s = Lt(
    (d) => (f) => {
      f.preventDefault();
      const m = f.dataTransfer.getData(yn);
      m ? i(d, m) : c.current !== null && c.current !== d && o(c.current, d), c.current = null;
    },
    [o, i]
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
        onDrop: s(f)
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
  const a = l / 255, o = t / 255, i = n / 255, c = Math.max(a, o, i), u = Math.min(a, o, i), r = c - u;
  let s = 0;
  r !== 0 && (c === a ? s = (o - i) / r % 6 : c === o ? s = (i - a) / r + 2 : s = (a - o) / r + 4, s *= 60, s < 0 && (s += 360));
  const d = c === 0 ? 0 : r / c;
  return [s, d, c];
}
function Gr(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let c = 0, u = 0, r = 0;
  return l < 60 ? (c = a, u = o, r = 0) : l < 120 ? (c = o, u = a, r = 0) : l < 180 ? (c = 0, u = a, r = o) : l < 240 ? (c = 0, u = o, r = a) : l < 300 ? (c = o, u = 0, r = a) : (c = a, u = 0, r = o), [
    Math.round((c + i) * 255),
    Math.round((u + i) * 255),
    Math.round((r + i) * 255)
  ];
}
function Xr(l) {
  return Yr(...kn(l));
}
function xt(l, t, n) {
  return Nn(...Gr(l, t, n));
}
const { useCallback: Ue, useRef: dn } = e, qr = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = Xr(l), i = dn(null), c = dn(null), u = Ue(
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
  ), s = Ue(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), d = Ue(
    (b) => {
      var w;
      const k = (w = c.current) == null ? void 0 : w.getBoundingClientRect();
      if (!k) return;
      const C = Math.max(0, Math.min(1, (b - k.top) / k.height)) * 360;
      t(xt(C, a, o));
    },
    [a, o, t]
  ), f = Ue(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), m = Ue(
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
      onPointerMove: s
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
      ref: c,
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
  canReset: i,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [s, d] = ct("palette"), [f, m] = ct(t), h = Jr(null), b = ue(Qr), [k, _] = ct(null);
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
  const g = ke(
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
      c(L);
    },
    [c]
  ), A = ke(
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
  }, [o, r]), B = ke(
    (L) => {
      if (Zr(n, L)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const $ = [...n];
      $[S] = L.toUpperCase(), r($);
    },
    [n, r]
  ), j = ke(() => {
    f != null && B(f), c(f);
  }, [f, c, B]);
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
        className: "tlColorInput__tab" + (s === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (s === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, s === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Kr,
      {
        colors: n,
        columns: a,
        onSelect: K,
        onConfirm: U,
        onSwap: A,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(qr, { color: f ?? "#000000", onColorChange: m }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: f } : void 0,
        draggable: C,
        onDragStart: C ? R : void 0
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
        className: "tlColorInput__input" + (E !== "" && !$t(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: I
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: D }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: j }, b["js.colorInput.ok"]))
  );
}, no = { "js.colorInput.chooseColor": "Choose color" }, { useState: lo, useCallback: it, useRef: ao } = e, ro = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(no), [c, u] = lo(!1), r = ao(null), s = n, d = t.editable !== !1, f = t.palette ?? [], m = t.paletteColumns ?? 6, h = t.defaultPalette ?? f, b = it(() => {
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
      className: "tlColorInput__swatch" + (s == null ? " tlColorInput__swatch--noColor" : ""),
      style: s != null ? { backgroundColor: s } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: s ?? "",
      "aria-label": i["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    to,
    {
      anchorRef: r,
      currentColor: s,
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
      className: "tlColorInput tlColorInput--immutable" + (s == null ? " tlColorInput--noColor" : ""),
      style: s != null ? { backgroundColor: s } : void 0,
      title: s ?? ""
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
  onCancel: i,
  onLoadIcons: c
}) => {
  const u = ue(co), [r, s] = tt("simple"), [d, f] = tt(""), [m, h] = tt(t ?? ""), [b, k] = tt(!1), [_, C] = tt(null), w = pn(null), N = pn(null);
  oo(() => {
    if (!l.current || !w.current) return;
    const U = l.current.getBoundingClientRect(), A = w.current.getBoundingClientRect();
    let V = U.bottom + 4, P = U.left;
    V + A.height > window.innerHeight && (V = U.top - A.height - 4), P + A.width > window.innerWidth && (P = Math.max(0, U.right - A.width)), C({ top: V, left: P });
  }, [l]), It(() => {
    !a && !b && c().catch(() => k(!0));
  }, [a, b, c]), It(() => {
    a && N.current && N.current.focus();
  }, [a]), Fe(!0, { ESCAPE: i }), It(() => {
    const U = (V) => {
      w.current && !w.current.contains(V.target) && i();
    }, A = setTimeout(() => document.addEventListener("mousedown", U), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", U);
    };
  }, [i]);
  const x = so(() => {
    if (!d) return n;
    const U = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(U) || A.label.toLowerCase().includes(U) || A.terms != null && A.terms.some((V) => V.includes(U))
    );
  }, [n, d]), E = Pe((U) => {
    f(U.target.value);
  }, []), y = Pe(
    (U) => {
      o(U);
    },
    [o]
  ), g = Pe((U) => {
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
      await c();
    } catch {
      k(!0);
    }
  }, [c]);
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
        onClick: () => s("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => s("advanced")
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
        onChange: R
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, m && /* @__PURE__ */ e.createElement(Se, { encoded: m })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, m ? m.startsWith("css:") ? m.substring(4) : m : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: D }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: I }, u["js.iconSelect.ok"]))
  );
}, uo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: mo, useCallback: ut, useRef: po } = e, fo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), i = ue(uo), [c, u] = mo(!1), r = po(null), s = n, d = t.editable !== !1, f = t.disabled === !0, m = t.icons ?? [], h = t.iconsLoaded === !0, b = ut(() => {
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
      className: "tlIconSelect__swatch" + (s == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: f,
      title: s ?? "",
      "aria-label": i["js.iconSelect.chooseIcon"]
    },
    s ? /* @__PURE__ */ e.createElement(Se, { encoded: s }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    io,
    {
      anchorRef: r,
      currentValue: s,
      icons: m,
      iconsLoaded: h,
      onSelect: k,
      onCancel: _,
      onLoadIcons: C
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, s ? /* @__PURE__ */ e.createElement(Se, { encoded: s }) : null));
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
  const n = Math.max(1, t), a = {}, o = (f, m) => !!(a[f] && a[f][m]), i = (f, m) => {
    a[f] || (a[f] = {}), a[f][m] = !0;
  }, c = [];
  let u = 0, r = 0;
  const s = (f) => {
    let m = null;
    for (const b of c) b.rowStart === f && (m = b);
    if (!m) return;
    let h = m.colEnd;
    for (; h < n && !o(f, h); ) h++;
    if (h !== m.colEnd) {
      for (let b = m.rowStart; b < m.rowEnd; b++)
        for (let k = m.colEnd; k < h; k++) i(b, k);
      m.colEnd = h;
    }
  };
  for (const f of l) {
    const m = n <= 1 ? 1 : Math.max(1, f.rowSpan || 1);
    let h = Math.min(Co(f.width, n), n);
    for (; o(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let N = r; N < n && !o(u, N); N++)
      b++;
    if (h > b) {
      for (s(u), r = 0, u++; o(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let N = r; N < n && !o(u, N); N++)
        b++;
      h = Math.min(h, b);
    }
    const k = r, _ = r + h, C = u, w = u + m;
    c.push({ id: f.id, colStart: k, colEnd: _, rowStart: C, rowEnd: w });
    for (let N = C; N < w; N++)
      for (let x = k; x < _; x++) i(N, x);
    r = _, r >= n && (r = 0, u++);
  }
  s(u);
  let d = 0;
  for (const f of c) f.rowEnd > d && (d = f.rowEnd);
  for (let f = 1; f < d; f++)
    for (let m = 0; m < n; m++) {
      if (o(f, m)) continue;
      const h = c.find((b) => b.rowEnd === f && b.colStart <= m && m < b.colEnd);
      if (h) {
        h.rowEnd = f + 1;
        for (let b = h.colStart; b < h.colEnd; b++) i(f, b);
      }
    }
  return c;
}
const wo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((y) => y && y.id), i = bo(null), [c, u] = Mt(1), r = t.editMode === !0;
  ho(() => {
    const y = i.current;
    if (!y) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, R = vo(a, g), I = () => u(_o(y.clientWidth, R));
    I();
    const D = new ResizeObserver(I);
    return D.observe(y), () => D.disconnect();
  }, [a]);
  const s = fn(() => yo(o, c), [o, c]), d = fn(() => {
    const y = {};
    for (const g of s) y[g.id] = g;
    return y;
  }, [s]), [f, m] = Mt(null), [h, b] = Mt(null), k = ze((y, g) => {
    if (!r) {
      y.preventDefault();
      return;
    }
    m(g), y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", g);
  }, [r]), _ = ze((y, g) => {
    if (!r || !f || f === g) return;
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const R = y.currentTarget.getBoundingClientRect(), I = y.clientX < R.left + R.width / 2;
    b((D) => D && D.id === g && D.before === I ? D : { id: g, before: I });
  }, [r, f]), C = ze(() => {
  }, []), w = ze((y, g, R) => {
    const I = o.map((A) => A.id), D = I.indexOf(y);
    if (D < 0) return;
    I.splice(D, 1);
    const K = I.indexOf(g);
    if (K < 0) {
      I.splice(D, 0, y);
      return;
    }
    const U = R ? K : K + 1;
    I.splice(U, 0, y), n("reorder", { order: I });
  }, [o, n]), N = ze((y, g) => {
    if (!r || !f || f === g) return;
    y.preventDefault();
    const R = y.currentTarget.getBoundingClientRect(), I = y.clientX < R.left + R.width / 2;
    w(f, g, I), m(null), b(null);
  }, [r, f, w]), x = ze(() => {
    m(null), b(null);
  }, []), E = {
    display: "grid",
    gridTemplateColumns: `repeat(${c}, 1fr)`,
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
      const g = d[y.id];
      if (!g) return null;
      const R = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
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
  const [t, n] = bn(!1), [a, o] = bn({}), i = hn(null), c = hn(null), u = ko(() => {
    n((h) => !h);
  }, []);
  So(() => {
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
  }, [t]), No(() => {
    if (!t) return;
    const h = (b) => {
      c.current && !c.current.contains(b.target) && i.current && !i.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Fe(t, { ESCAPE: () => n(!1) }), Wt(t, c, "first");
  const r = l.items.filter((h) => h != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((f = l.subGroups) != null && f.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(q, { control: r[0] })));
  const s = l.label ?? l.name, d = !!l.icon;
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
      "aria-label": d ? s : void 0,
      title: d ? s : void 0
    },
    d ? /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, s), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Cn.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: c,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? a : void 0,
        onClick: () => n(!1)
      },
      r.map((h, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(q, { control: h }))),
      (m = l.subGroups) == null ? void 0 : m.map((h, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((k, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(q, { control: k })))))
    ),
    document.body
  ));
}, Ro = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((o) => o.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(To, { group: o }) : /* @__PURE__ */ e.createElement(Do, { group: o }))));
}, Lo = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(q, { control: t.frame }));
}, xo = ({ controlId: l }) => {
  const t = G(), n = ne(), a = t.content, o = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, o && o.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, o.map((i, c) => {
    const u = c === o.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: i.depth }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: i.depth })
      },
      i.label
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
W("TLTreeView", Ar);
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
