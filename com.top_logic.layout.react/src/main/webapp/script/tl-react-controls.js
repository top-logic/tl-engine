import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as X, useKeyboardBinding as me, useTLUpload as Ke, useFill as gt, FillBarrier as Pe, TLChild as G, useI18N as ue, useTLDataUrl as Ye, scrollToAnchor as In, useStandaloneKeyboardScope as Oe, useFillHost as tt, FillProvider as nt, KeyboardScopeProvider as Ht, useFocusTrap as Wt, CMD_VALUE_CHANGED as at, anchoredOverlayProps as Pn, register as z } from "tl-react-bridge";
const { useCallback: Yt, useRef: jn } = e, An = 300, Bn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: An,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ne(), s = jn(!1), i = Yt(
    (k) => {
      s.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = Yt(async () => {
    await o(), r && s.current && (s.current = !1, u("commit"));
  }, [o, r, u]), d = t.multiline === !0;
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
}, { useCallback: Gt } = e, On = 300, Fn = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: On }), u = Gt(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = Gt(() => {
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
}, { useCallback: Xt } = e, $n = 300, Hn = ({ controlId: l, state: t, config: n }) => {
  const [a, o, u] = De({ debounceMs: $n }), s = Xt(
    (p) => {
      const h = p.target.value;
      o(h === "" ? null : h);
    },
    [o]
  ), i = Xt(() => {
    u();
  }, [u]);
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
      onBlur: i,
      disabled: t.disabled === !0,
      className: m,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: Wn } = e, Un = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = Wn(
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
}, { useCallback: zn } = e, Vn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), u = zn(
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
}, { useCallback: Kn } = e, Yn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], u = t.presentation === "select", s = t.disabled === !0, i = t.hasError === !0, r = t.hasWarnings === !0, c = Kn(
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
}, { useCallback: Gn, useRef: Xn, useEffect: qn } = e, Zn = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, u = Xn(null);
  qn(() => {
    u.current && (u.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = Gn(
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
const { useCallback: Qn } = e, Jn = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: u }) => {
  const s = X(), i = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = u ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, k = s.appearance, _ = s.size, v = s.cssClasses, y = s.navigateUrl, C = Qn(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    i(r);
  }, [i, r, y]), L = s.keyGesture;
  me(L, () => m || h ? !1 : (C(), !0));
  const E = p === "icon-only", w = p === "label-only" || p === "icon-label" || E && !d, b = g ?? (E ? c : void 0), S = b ? `text:${b}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: m,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : "") + (v ? " " + v : ""),
      "data-tooltip": S,
      "aria-label": d || E ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, el = ({ controlId: l }) => {
  const t = X(), n = Ke(), a = e.useRef(null), [o, u] = e.useState(!1), s = t.label ?? "", i = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var L;
    r || o || (L = a.current) == null || L.click();
  }, [r, o]), k = e.useCallback(async (L) => {
    const E = L.target.files;
    if (!E || E.length === 0) return;
    const w = new FormData();
    for (let b = 0; b < E.length; b++)
      w.append("file", E[b], E[b].name);
    L.target.value = "", u(!0);
    try {
      await n(w);
    } finally {
      u(!1);
    }
  }, [n]), _ = d === "icon-only", v = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || _ && !i, C = r || o;
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
      onClick: g,
      disabled: C,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (_ ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": _ ? s : void 0
    },
    v && i && /* @__PURE__ */ e.createElement(Ne, { encoded: i, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: tl } = e, nl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const u = X(), s = ne(), i = t ?? "click", r = n ?? u.label, c = a ?? u.active === !0, d = o ?? u.disabled === !0, m = tl(() => {
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
}, ll = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: al } = e, rl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = gt(!0), o = t.tabs ?? [], u = t.activeTabId, s = al((i) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, /* @__PURE__ */ e.createElement(Pe, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, ol = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, sl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, cl = ({ controlId: l }) => {
  const t = X(), n = Ke(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        const C = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(y, C ? { mimeType: C } : void 0);
        i.current = L, L.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), c.current = null;
          const E = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const w = new FormData();
          w.append("audio", E, "recording.webm"), await n(w), o("idle");
        }, L.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(sl), k = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], _ = p === "uploading", v = ["tlAudioRecorder__button"];
  return p === "recording" && v.push("tlAudioRecorder__button--recording"), p === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: h,
      disabled: _,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, il = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ul = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [u, s] = e.useState(a ? "idle" : "disabled"), i = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
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
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), s("idle");
          return;
        }
        const v = await _.blob();
        r.current = URL.createObjectURL(v);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), s("idle");
        return;
      }
    }
    const k = new Audio(r.current);
    i.current = k, k.onended = () => {
      s("idle");
    }, k.play(), s("playing");
  }, [u, n]), m = ue(il), p = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = u === "disabled" || u === "loading", g = ["tlAudioPlayer__button"];
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
}, dl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ml = ({ controlId: l }) => {
  const t = X(), n = Ke(), [a, o] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (E) => {
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
  }, [a]), k = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), _ = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), v = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const w = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    w && p(w);
  }, [a, p]), y = m === "uploading", C = ue(dl), L = m === "uploading" ? C["js.uploading"] : C["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
      onDragLeave: _,
      onDrop: v
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
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, pl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, fl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ke(), u = Ye(), s = ue(pl), i = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, k] = e.useState("idle"), [_, v] = e.useState(!1), [y, C] = e.useState(!1), L = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || y)) {
      C(!0);
      try {
        const B = u + (u.includes("?") ? "&" : "?") + "rev=" + d, x = await fetch(B);
        if (!x.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", x.status);
          return;
        }
        const D = await x.blob(), K = URL.createObjectURL(D), f = document.createElement("a");
        f.href = K, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(K);
      } catch (B) {
        console.error("[TLBinaryField] Fetch error:", B);
      } finally {
        C(!1);
      }
    }
  }, [r, y, u, d, c]), w = e.useCallback(async (B) => {
    k("uploading");
    const x = new FormData();
    x.append("file", B, B.name), await o(x), k("idle");
  }, [o]), b = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", S = e.useCallback((B) => {
    var D;
    const x = (D = B.target.files) == null ? void 0 : D[0];
    x && w(x);
  }, [w]), I = e.useCallback(() => {
    var B;
    b || (B = L.current) == null || B.click();
  }, [b]), R = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), v(!0);
  }, []), $ = e.useCallback((B) => {
    B.preventDefault(), B.stopPropagation(), v(!1);
  }, []), H = e.useCallback((B) => {
    var D;
    if (B.preventDefault(), B.stopPropagation(), v(!1), b) return;
    const x = (D = B.dataTransfer.files) == null ? void 0 : D[0];
    x && w(x);
  }, [b, w]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), W = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, W) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const P = b, j = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: $,
      onDrop: H
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
        onClick: I,
        disabled: P,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && W,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, hl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function bl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const gl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ke(), o = Ye(), u = ue(hl), s = t.chips ?? [], i = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (E) => {
    const w = Array.from(E);
    if (w.length !== 0) {
      c(!0);
      try {
        const b = new FormData();
        for (const S of w)
          b.append("file", S, S.name);
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
        const S = await b.blob(), I = URL.createObjectURL(S), R = document.createElement("a");
        R.href = I, R.download = E.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(I);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [o]), k = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), _ = e.useCallback(() => {
    var E;
    r || (E = p.current) == null || E.click();
  }, [r]), v = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!0));
  }, [i]), y = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1));
  }, [i]), C = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
  }, [i, r, h]), L = [
    "tlFileChips",
    i ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: L,
      onDragOver: v,
      onDragLeave: y,
      onDrop: C
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
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, bl(E.size))
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
}, El = 3e4;
function vl(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const _l = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, u] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => u((i) => i + 1), El);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, vl(n, o));
}, Cl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, yl = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (u) => {
    u.preventDefault(), In(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function wl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function kl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Nl = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${kl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    wl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Sl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Dl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = ne(), o = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + u, k = await fetch(g);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const _ = await k.blob(), v = URL.createObjectURL(_), y = document.createElement("a");
        y.href = v, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, u, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = ue(Sl);
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
}, Tl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Rl = ({ controlId: l }) => {
  const t = X(), n = Ke(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), [i, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, k = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    _(), o("idle");
  }, [_]), y = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (s(null), !k) {
        (R = p.current) == null || R.click();
        return;
      }
      try {
        const $ = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = $, o("overlayOpen");
      } catch ($) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", $), s("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, k]), C = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = c.current, $ = m.current;
    if (!R || !$)
      return;
    $.width = R.videoWidth, $.height = R.videoHeight;
    const H = $.getContext("2d");
    H && (H.drawImage(R, 0, 0), _(), o("uploading"), $.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const W = new FormData();
      W.append("photo", A, "capture.jpg"), await n(W), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), L = e.useCallback(async (R) => {
    var A;
    const $ = (A = R.target.files) == null ? void 0 : A[0];
    if (!$) return;
    o("uploading");
    const H = new FormData();
    H.append("photo", $, $.name), await n(H), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var $;
    if (a !== "overlayOpen") return;
    ($ = h.current) == null || $.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), Oe(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null);
  }, []);
  const E = ue(Tl), w = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const S = ["tlPhotoCapture__overlayVideo"];
  i && S.push("tlPhotoCapture__overlayVideo--mirrored");
  const I = ["tlPhotoCapture__mirrorBtn"];
  return i && I.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
  )), !k && /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
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
        className: I.join(" "),
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
        onClick: C,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[u]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Ll = {
  "js.photoViewer.alt": "Captured photo"
}, xl = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(o);
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
  const r = ue(Ll);
  return !a || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, Ml = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Il = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = !!t.hasPdf, o = t.dataRevision ?? 0, u = ue(Ml), i = n.indexOf("react-api/"), r = i >= 0 ? n.slice(0, i) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: u["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, u["js.pdfViewer.noDocument"]));
}, { useCallback: qt, useRef: Ct } = e, Pl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = gt(!0), o = t.orientation, u = t.resizable === !0, s = t.children ?? [], i = o === "horizontal", r = s.length > 0 && s.every((v) => v.collapsed), c = !r && s.some((v) => v.collapsed), d = r ? !i : i, m = Ct(null), p = Ct(null), h = Ct(null), g = qt((v, y) => {
    const C = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? r && !d ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : y !== void 0 ? C.flex = `0 0 ${y}px` : C.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (C.minWidth = i ? v.minSize : void 0, C.minHeight = i ? void 0 : v.minSize), C;
  }, [i, r, c, d]), k = qt((v, y) => {
    v.preventDefault();
    const C = m.current;
    if (!C) return;
    const L = s[y], E = s[y + 1], w = C.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    w.forEach((R) => {
      b.push(i ? R.offsetWidth : R.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: y,
      startPos: i ? v.clientX : v.clientY,
      startSizeBefore: b[y],
      startSizeAfter: b[y + 1],
      childBefore: L,
      childAfter: E
    };
    const S = (R) => {
      const $ = p.current;
      if (!$ || !h.current) return;
      const A = (i ? R.clientX : R.clientY) - $.startPos, W = $.childBefore.minSize || 0, P = $.childAfter.minSize || 0;
      let j = $.startSizeBefore + A, B = $.startSizeAfter - A;
      j < W && (B += j - W, j = W), B < P && (j += B - P, B = P), h.current[$.splitterIndex] = j, h.current[$.splitterIndex + 1] = B;
      const x = C.querySelectorAll(":scope > .tlSplitPanel__child"), D = x[$.splitterIndex], K = x[$.splitterIndex + 1];
      D && (D.style.flex = `0 0 ${j}px`), K && (K.style.flex = `0 0 ${B}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", S), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const R = {};
        s.forEach(($, H) => {
          const A = $.control;
          A != null && A.controlId && h.current && (R[A.controlId] = h.current[H]);
        }), n("updateSizes", { sizes: R });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", S), document.addEventListener("mouseup", I), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), _ = [];
  return s.forEach((v, y) => {
    if (_.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${y}`,
          className: `tlSplitPanel__child${v.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(v)
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control })
      )
    ), u && y < s.length - 1) {
      const C = s[y + 1];
      !v.collapsed && !C.collapsed && _.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${y}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (E) => k(E, y)
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
}, It = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: yt } = e, jl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Al = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Bl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ol = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Fl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), $l = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Hl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(jl), o = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = u === "MINIMIZED", k = u === "MAXIMIZED", _ = u === "HIDDEN", v = yt(() => {
    n("toggleMinimize");
  }, [n]), y = yt(() => {
    n("toggleMaximize");
  }, [n]), C = yt(() => {
    n("popOut");
  }, [n]), L = gt(d && !_);
  if (_)
    return null;
  const E = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, w = s && !k || i && !g || r, b = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || w;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${L ? " " + L : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: E
    },
    b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(Bl, null) : /* @__PURE__ */ e.createElement(Al, null)
    ), i && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(Fl, null) : /* @__PURE__ */ e.createElement(Ol, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement($l, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(It, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, Wl = ({ controlId: l }) => {
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
}, Ul = ({ controlId: l }) => {
  const t = X(), [n, a] = tt();
  return /* @__PURE__ */ e.createElement(nt, { host: a }, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: n ? "tlDeckPane " + n : "tlDeckPane",
      style: { width: "100%", height: "100%" }
    },
    t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild })
  ));
}, { useCallback: Ee, useState: dt, useEffect: Pt, useRef: pt } = e, zl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function jt(l, t, n, a) {
  const o = [];
  for (const u of l)
    if (u.type === "nav") {
      if (u.hidden) continue;
      o.push({ id: u.id, type: "nav", groupId: a });
    } else u.type === "command" ? o.push({ id: u.id, type: "command", groupId: a }) : u.type === "group" && (o.push({ id: u.id, type: "group" }), (n.get(u.id) ?? u.expanded) && !t && o.push(...jt(u.children, t, n, u.id)));
  return o;
}
const Ve = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, Vl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: u,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ve, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ve, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Kl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(Ve, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Yl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ve, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Gl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Xl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: u }) => {
  const s = pt(null);
  Pt(() => {
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
        /* @__PURE__ */ e.createElement(Ve, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, ql = ({
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
  onCloseFlyout: k
}) => {
  const _ = pt(null), [v, y] = dt(null), C = Ee(() => {
    a ? h === l.id ? k() : (_.current && y(_.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, k]), L = Ee((w) => {
    _.current = w, r(w);
  }, [r]), E = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: C,
      title: a ? l.label : void 0,
      "aria-expanded": a ? E : t,
      tabIndex: i,
      ref: L,
      onFocus: () => c(l.id)
    },
    /* @__PURE__ */ e.createElement(Ve, { icon: l.icon }),
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
    Xl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: o,
      onExecute: u,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    bn,
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
      onCloseFlyout: k
    }
  ))));
}, bn = ({
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
        Vl,
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
        Kl,
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
      return /* @__PURE__ */ e.createElement(Yl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Gl, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        ql,
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
}, Zl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(zl), o = t.items ?? [], u = t.activeItemId, s = t.collapsed, i = t.drawerOpen, r = i ? !1 : s, [c, d] = dt(() => {
    const A = /* @__PURE__ */ new Map(), W = (P) => {
      for (const j of P)
        j.type === "group" && (A.set(j.id, j.expanded), W(j.children));
    };
    return W(o), A;
  }), m = Ee((A) => {
    d((W) => {
      const P = new Map(W), j = P.get(A) ?? !1;
      return P.set(A, !j), n("toggleGroup", { itemId: A, expanded: !j }), P;
    });
  }, [n]), p = Ee((A) => {
    A !== u && n("selectItem", { itemId: A });
  }, [n, u]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), k = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [_, v] = dt(null), y = Ee((A) => {
    v(A);
  }, []), C = Ee(() => {
    v(null);
  }, []);
  Pt(() => {
    r || v(null);
  }, [r]);
  const [L, E] = dt(() => {
    const A = jt(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), w = pt(/* @__PURE__ */ new Map()), b = Ee((A) => (W) => {
    W ? w.current.set(A, W) : w.current.delete(A);
  }, []), S = Ee((A) => {
    E(A);
  }, []), I = pt(0), R = Ee((A) => {
    E(A), I.current++;
  }, []);
  Pt(() => {
    const A = w.current.get(L);
    A && document.activeElement !== A && A.focus();
  }, [L, I.current]);
  const $ = Ee((A) => {
    if (A.key === "Escape" && _ !== null) {
      A.preventDefault(), C();
      return;
    }
    const W = jt(o, r, c);
    if (W.length === 0) return;
    const P = W.findIndex((B) => B.id === L);
    if (P < 0) return;
    const j = W[P];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const B = (P + 1) % W.length;
        R(W[B].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const B = (P - 1 + W.length) % W.length;
        R(W[B].id);
        break;
      }
      case "Home": {
        A.preventDefault(), R(W[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), R(W[W.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), j.type === "nav" ? p(j.id) : j.type === "command" ? h(j.id) : j.type === "group" && (r ? _ === j.id ? C() : y(j.id) : m(j.id));
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
    _,
    R,
    p,
    h,
    m,
    y,
    C
  ]), H = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (i ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: H }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), i && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: $ }, o.map((A) => /* @__PURE__ */ e.createElement(
    bn,
    {
      key: A.id,
      item: A,
      activeItemId: u,
      collapsed: r,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: L,
      setItemRef: b,
      onItemFocus: S,
      groupStates: c,
      flyoutGroupId: _,
      onOpenFlyout: y,
      onCloseFlyout: C
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Pe, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, Ql = ({ controlId: l }) => {
  const t = X(), n = t.direction ?? "column", a = t.gap ?? "default", o = t.align ?? "stretch", u = t.wrap === !0, s = t.growFirst === !0, i = t.children ?? [], [r, c] = tt(), d = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${o}`,
    u ? "tlStack--wrap" : "",
    s ? "tlStack--grow-first" : "",
    r,
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(nt, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, i.map((m, p) => /* @__PURE__ */ e.createElement(G, { key: p, control: m }))));
}, Jl = ({ controlId: l }) => {
  const t = X(), [n, a] = tt();
  return /* @__PURE__ */ e.createElement(nt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, ea = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", u = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, u.map((i, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: i })));
}, ta = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = n != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, na = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, u = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, u.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: la } = e, aa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = la((u) => {
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
}, { useCallback: ra } = e, oa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = t.activeItemId, u = ra((s) => {
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
}, { useCallback: Zt, useRef: sa } = e, ca = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), ia = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, u = t.child, s = sa(null), i = Zt(() => {
    n("close");
  }, [n]), r = Zt((c) => {
    o && c.target === c.currentTarget && i();
  }, [o, i]);
  return a ? /* @__PURE__ */ e.createElement(Ht, null, /* @__PURE__ */ e.createElement(ca, { onClose: i }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: u }))
  )) : null;
}, { useEffect: ua, useRef: da } = e, ma = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = da(n.length);
  return ua(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: rt, useRef: $e, useState: ot } = e, pa = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), fa = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, ha = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], ba = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(fa), o = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = ot(null), [k, _] = ot(null), [v, y] = ot(null), C = $e(null), [L, E] = ot(!1), w = $e(null), b = $e(null), S = $e(null), I = $e(null), R = $e(null), $ = rt(() => {
    n("close");
  }, [n]);
  Wt(!0, I, "field");
  const H = rt((B, x) => {
    x.preventDefault();
    const D = I.current;
    if (!D) return;
    const K = D.getBoundingClientRect(), f = !C.current, M = C.current ?? { x: K.left, y: K.top };
    f && (C.current = M, y(M)), R.current = {
      dir: B,
      startX: x.clientX,
      startY: x.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...M },
      symmetric: f
    };
    const Y = (Z) => {
      const F = R.current;
      if (!F) return;
      const te = Z.clientX - F.startX, ce = Z.clientY - F.startY;
      let ae = F.startW, ge = F.startH, ve = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (ae = F.startW + 2 * te), F.dir.includes("w") && (ae = F.startW - 2 * te), F.dir.includes("s") && (ge = F.startH + 2 * ce), F.dir.includes("n") && (ge = F.startH - 2 * ce)) : (F.dir.includes("e") && (ae = F.startW + te), F.dir.includes("w") && (ae = F.startW - te, ve = te), F.dir.includes("s") && (ge = F.startH + ce), F.dir.includes("n") && (ge = F.startH - ce, we = ce));
      const Te = Math.max(200, ae), Re = Math.max(100, ge);
      F.symmetric ? (ve = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ve = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), b.current = Te, S.current = Re, g(Te), _(Re);
      const Fe = {
        x: F.startPos.x + ve,
        y: F.startPos.y + we
      };
      C.current = Fe, y(Fe);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
      const Z = b.current, F = S.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, [n]), A = rt((B) => {
    if (B.button !== 0 || B.target.closest("button")) return;
    B.preventDefault();
    const x = I.current;
    if (!x) return;
    const D = x.getBoundingClientRect(), K = C.current ?? { x: D.left, y: D.top }, f = B.clientX - K.x, M = B.clientY - K.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let ce = Z.clientX - f, ae = Z.clientY - M;
      const ge = x.offsetWidth, ve = x.offsetHeight;
      ce + ge > F && (ce = F - ge), ae + ve > te && (ae = te - ve), ce < 0 && (ce = 0), ae < 0 && (ae = 0);
      const we = { x: ce, y: ae };
      C.current = we, y(we);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, []), W = rt(() => {
    var B, x;
    if (L) {
      const D = w.current;
      D && (y(D.x !== -1 ? { x: D.x, y: D.y } : null), g(D.w), _(D.h)), E(!1);
    } else {
      const D = I.current, K = D == null ? void 0 : D.getBoundingClientRect();
      w.current = {
        x: ((B = C.current) == null ? void 0 : B.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((x = C.current) == null ? void 0 : x.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: h ?? (K == null ? void 0 : K.width) ?? null,
        h: k ?? null
      }, E(!0), y({ x: 0, y: 0 }), g(null), _(null);
    }
  }, [L, h, k]), P = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : u,
    ...k != null ? { height: k + "px" } : s != null ? { height: s } : {},
    ...i != null && k == null ? { minHeight: i } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, j = l + "-title";
  return /* @__PURE__ */ e.createElement(Ht, { modal: !0 }, /* @__PURE__ */ e.createElement(pa, { onClose: $ }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: I,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": j
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : A,
        onDoubleClick: r ? W : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: j }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: W,
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
          onClick: $,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: c }))),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((B, x) => /* @__PURE__ */ e.createElement(G, { key: x, control: B }))),
    r && !L && ha.map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${B}`,
        onMouseDown: (x) => H(B, x)
      }
    ))
  ));
}, { useCallback: ga } = e, Ea = {
  "js.drawer.close": "Close"
}, va = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ea), o = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, r = t.child, c = ga(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, /* @__PURE__ */ e.createElement(Pe, null, r && /* @__PURE__ */ e.createElement(G, { control: r }))));
}, { useCallback: st, useRef: _a } = e, Ca = ({ controlId: l }) => {
  const t = X(), n = ne(), a = _a(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", i = st((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = st(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = st((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = st((m) => {
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
}, { useCallback: ya, useEffect: Qt, useRef: wa, useState: Jt } = e, ka = 250, Na = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.message ?? "", o = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, r = t.generation ?? 0, [c, d] = Jt(!1), [m, p] = Jt(!1), h = wa(!1);
  Qt(() => {
    h.current = !1;
  }, [r]);
  const g = ya(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Qt(() => {
    if (!i || s === 0 || m) return;
    const k = setTimeout(g, h.current ? ka : s);
    return () => clearTimeout(k);
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
}, { useCallback: Sa, useEffect: en, useMemo: Da, useRef: Ta, useState: Ra } = e, La = 1e3;
function xa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), u = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${u(a)}:${u(n)}` : `${a}:${u(n)}`;
}
const Ma = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", u = t.text ?? "", s = t.deadline ?? null, i = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = Da(
    () => i != null ? i - Date.now() : 0,
    [i]
  ), [p, h] = Ra(0), g = a && s != null;
  en(() => {
    if (!g) return;
    const L = setInterval(() => h((E) => E + 1), La);
    return () => clearInterval(L);
  }, [g, s]);
  const k = Ta(null);
  en(() => {
    !g || d == null || s == null || k.current !== s && (Date.now() + m < s + d || (k.current = s, n("deadlinePassed", {})));
  }, [p, g, s, d, m, n]);
  const _ = Sa(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const v = s != null ? s - (Date.now() + m) : null;
  if (r != null && v != null && v > r) return null;
  const y = v != null ? xa(v) : null, C = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${C ? " tlNoticeBar--clickable" : ""}`,
      role: C ? "button" : "status",
      "aria-live": "polite",
      tabIndex: C ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": C ? `${u} ${c}` : void 0,
      onClick: C ? _ : void 0,
      onKeyDown: C ? (L) => {
        (L.key === "Enter" || L.key === " ") && (L.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, u),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: wt, useEffect: tn, useRef: Ia, useState: nn } = e, Pa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.anchorId, u = t.anchorX, s = t.anchorY, i = t.items ?? [], r = Ia(null), [c, d] = nn({ top: 0, left: 0 }), [m, p] = nn(0), h = i.filter((v) => v.type === "item" && !v.disabled);
  tn(() => {
    var b, S;
    if (!a) return;
    const v = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((S = r.current) == null ? void 0 : S.offsetWidth) ?? 200;
    if (u != null && s != null) {
      let I = s, R = u;
      I + v > window.innerHeight && (I = Math.max(0, window.innerHeight - v)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), d({ top: I, left: R }), p(0);
      return;
    }
    if (!o) return;
    const C = document.getElementById(o);
    if (!C) return;
    const L = C.getBoundingClientRect();
    let E = L.bottom + 4, w = L.left;
    E + v > window.innerHeight && (E = L.top - v - 4), w + y > window.innerWidth && (w = L.right - y), d({ top: E, left: w }), p(0);
  }, [a, o, u, s]);
  const g = wt(() => {
    n("close");
  }, [n]), k = wt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  tn(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && g();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, g]);
  const _ = wt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), g();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), p((y) => (y + 1) % h.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), p((y) => (y - 1 + h.length) % h.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const y = h[m];
      y && k(y.id);
    }
  }, [g, k, h, m]);
  return Wt(a, r), a ? /* @__PURE__ */ e.createElement(
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
    i.map((v, y) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const L = h.indexOf(v) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : "") + (v.cssClasses ? " " + v.cssClasses : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => k(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: v.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, ja = 768, Aa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = gt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${ja}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (p) => d(p.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, u = t.notices, s = t.content, i = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: o })), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement(G, { control: r }));
}, Ba = ({ controlId: l }) => {
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
}, Oa = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), null), Fa = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, ln = 50, $a = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function kt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, $a));
}
const At = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Ha = At + ", button:not([disabled]), a[href]";
function gn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Nt(l, t, n = {}) {
  const a = gn(l, t);
  if (n.col) {
    const u = a.find((i) => i.dataset.col === n.col), s = u == null ? void 0 : u.querySelector(At);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const u of o) {
    const s = u.querySelector(At);
    if (s) return s;
  }
  return null;
}
const Wa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Fa), o = e.useRef(null);
  e.useEffect(() => {
    const N = o.current;
    if (!N) return;
    const T = (V) => {
      const Q = V.detail;
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
    return N.addEventListener("tl-tooltip-resolve", T), () => N.removeEventListener("tl-tooltip-resolve", T);
  }, []);
  const u = t.columns ?? [], s = t.totalRowCount ?? 0, i = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, k = e.useMemo(
    () => u.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [u]
  ), _ = c === "multi", v = 40, y = 20, C = e.useRef(null), L = e.useRef(null), E = e.useRef(null), w = e.useRef(null), b = e.useRef(null), [S, I] = e.useState({}), R = e.useRef(null), $ = e.useRef(!1), H = e.useRef(null), [A, W] = e.useState(null), [P, j] = e.useState(null), [B, x] = e.useState(null), [D, K] = e.useState(0);
  e.useEffect(() => {
    const N = E.current;
    if (!N)
      return;
    const T = () => {
      const Q = N.offsetWidth - N.clientWidth;
      K((ee) => ee === Q ? ee : Q);
    };
    T();
    const V = new ResizeObserver(T);
    return V.observe(N), () => V.disconnect();
  }, []), e.useEffect(() => {
    R.current || I({});
  }, [u]);
  const f = e.useCallback((N) => S[N.name] ?? N.width, [S]), M = e.useMemo(() => {
    const N = [];
    let T = _ && p > 0 ? v : 0;
    for (let V = 0; V < p && V < u.length; V++)
      N.push(T), T += f(u[V]);
    return N;
  }, [u, p, _, v, f]), Y = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let N = _ ? v : 0;
    for (let T = 0; T < p && T < u.length; T++)
      N += f(u[T]);
    return N;
  }, [u, p, _, v, f]), U = s * r, Z = e.useRef(null), F = e.useCallback((N, T, V) => {
    V.preventDefault(), V.stopPropagation(), R.current = { column: N, startX: V.clientX, startWidth: T };
    let Q = V.clientX, ee = 0;
    const re = () => {
      const se = R.current;
      if (!se) return;
      const de = Math.max(ln, se.startWidth + (Q - se.startX) + ee);
      I((Ce) => ({ ...Ce, [se.column]: de }));
    }, oe = () => {
      const se = E.current, de = C.current;
      if (!se || !R.current) return;
      const Ce = se.getBoundingClientRect(), Le = 40, Vt = 8, Mn = se.scrollLeft;
      Q > Ce.right - Le ? se.scrollLeft += Vt : Q < Ce.left + Le && (se.scrollLeft = Math.max(0, se.scrollLeft - Vt));
      const Kt = se.scrollLeft - Mn;
      Kt !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Kt, re()), Z.current = requestAnimationFrame(oe);
    };
    Z.current = requestAnimationFrame(oe);
    const he = (se) => {
      Q = se.clientX, re();
    }, pe = (se) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", pe), Z.current !== null && (cancelAnimationFrame(Z.current), Z.current = null);
      const de = R.current;
      if (de) {
        const Ce = Math.max(ln, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ce }), R.current = null, $.current = !0, requestAnimationFrame(() => {
          $.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    C.current && E.current && (C.current.scrollLeft = E.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const N = E.current;
      if (!N) return;
      const T = N.scrollTop, V = Math.ceil(N.clientHeight / r), Q = Math.floor(T / r);
      n("scroll", { start: Q, count: V });
    }, 80);
  }, [n, r]), ce = e.useCallback((N, T, V) => {
    if ($.current) return;
    let Q;
    !T || T === "desc" ? Q = "asc" : Q = "desc";
    const ee = V.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: Q, mode: ee });
  }, [n]), ae = e.useCallback((N, T) => {
    H.current = N, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", N);
  }, []), ge = e.useCallback((N, T) => {
    if (!H.current || H.current === N) {
      W(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const V = T.currentTarget.getBoundingClientRect(), Q = T.clientX < V.left + V.width / 2 ? "left" : "right";
    W({ column: N, side: Q });
  }, []), ve = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const T = H.current;
    if (!T || !A) {
      H.current = null, W(null);
      return;
    }
    let V = u.findIndex((ee) => ee.name === A.column);
    if (V < 0) {
      H.current = null, W(null);
      return;
    }
    const Q = u.findIndex((ee) => ee.name === T);
    A.side === "right" && V++, Q < V && V--, n("columnReorder", { column: T, targetIndex: V }), H.current = null, W(null);
  }, [u, A, n]), we = e.useCallback(() => {
    H.current = null, W(null);
  }, []), Te = e.useCallback((N, T) => {
    var ee, re, oe, he;
    const V = window.getSelection();
    if (V && !V.isCollapsed && T.currentTarget.contains(V.anchorNode))
      return;
    if (!kt(T) && ((ee = E.current) == null || ee.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const pe = (he = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      b.current = { index: N, col: pe ?? void 0 };
    }
    const Q = i.find((pe) => pe.index === N);
    kt(T) && (Q != null && Q.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: N,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, i]), Re = e.useCallback((N, T, V) => {
    n("moveSelection", { direction: N, extend: T, move: V });
  }, [n]), Fe = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: _, shiftKey: !1 });
  }, [n, m, _]), Et = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), lt = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const N = E.current;
    if (!N)
      return;
    const T = m * r, V = T + r;
    T < N.scrollTop ? N.scrollTop = T : V > N.scrollTop + N.clientHeight && (N.scrollTop = V - N.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const N = b.current, T = E.current;
    if (!N || !T)
      return;
    const V = i.find((re) => re.index === N.index);
    if (!V || !Nt(T, V.id))
      return;
    b.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !T.contains(Q))
      return;
    const ee = Nt(T, V.id, { col: N.col, last: N.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [i]);
  const O = e.useCallback((N) => {
    if (N.key !== "Tab")
      return;
    const T = E.current, V = document.activeElement;
    if (!T || !V || !T.contains(V))
      return;
    const Q = V.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = i.find((Le) => Le.id === ee);
    if (!re)
      return;
    const oe = gn(T, ee).flatMap((Le) => Array.from(Le.querySelectorAll(Ha))), he = oe.indexOf(V);
    if (he < 0)
      return;
    const pe = !N.shiftKey;
    if (!(pe ? he === oe.length - 1 : he === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const Ce = i.find((Le) => Le.index === de);
    Ce && Nt(T, Ce.id) || (N.preventDefault(), b.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [i, s, n]), q = e.useCallback((N, T) => {
    T.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), le = e.useCallback(() => {
    const N = d === s && s > 0;
    n("selectAll", { selected: !N });
  }, [n, d, s]), ie = e.useCallback((N, T, V) => {
    V.stopPropagation(), n("expand", { rowIndex: N, expanded: T });
  }, [n]), Ge = e.useCallback((N, T) => {
    T.preventDefault(), j({ x: T.clientX, y: T.clientY, colIdx: N });
  }, []), Nn = e.useCallback(() => {
    P && (n("setFrozenColumnCount", { count: P.colIdx + 1 }), j(null));
  }, [P, n]), Sn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), j(null);
  }, [n]), Dn = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const T = L.current, V = C.current;
    if (!T || !V)
      return;
    const Q = T.clientWidth, ee = [{ x: 0, count: 0 }];
    V.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - T.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: Y, count: p };
    const oe = (pe) => {
      const se = pe.clientX - T.getBoundingClientRect().left;
      re = ee.reduce(
        (de, Ce) => Math.abs(Ce.x - se) < Math.abs(de.x - se) ? Ce : de,
        ee[0]
      ), x(re);
    }, he = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", he), x(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", he);
  }, [Y, p, n]);
  e.useEffect(() => {
    if (!P) return;
    const N = () => j(null);
    return document.addEventListener("mousedown", N), () => document.removeEventListener("mousedown", N);
  }, [P]), Oe(!!P, { ESCAPE: () => j(null) });
  const Tn = e.useCallback((N, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: N });
  }, [n]), Rn = e.useCallback((N) => {
    N.stopPropagation(), N.preventDefault(), n("openColumnSelect", {});
  }, [n]), vt = u.reduce((N, T) => N + f(T), 0) + (_ ? v : 0), _t = g ? 32 : 0, Ln = d === s && s > 0, zt = d > 0 && d < s, xn = e.useCallback((N) => {
    N && (N.indeterminate = zt);
  }, [zt]);
  return /* @__PURE__ */ e.createElement(Ht, { active: lt }, /* @__PURE__ */ e.createElement(
    Oa,
    {
      isMulti: _,
      cursorIndex: m,
      onMove: Re,
      onToggle: Fe,
      onSelectAll: Et
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (N) => {
        if (!H.current) return;
        N.preventDefault();
        const T = E.current, V = C.current;
        if (!T) return;
        const Q = T.getBoundingClientRect(), ee = 40, re = 8;
        N.clientX < Q.left + ee ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : N.clientX > Q.right - ee && (T.scrollLeft += re), V && (V.scrollLeft = T.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: L }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: C }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: vt, paddingRight: _t + D }
      },
      _ && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: v,
            minWidth: v,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (N) => {
            H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", u.length > 0 && u[0].name !== H.current && W({ column: u[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: xn,
            className: "tlTableView__checkbox",
            checked: Ln,
            onChange: le
          }
        )
      ),
      u.map((N, T) => {
        const V = f(N);
        u.length - 1;
        let Q = "tlTableView__headerCell";
        N.sortable && (Q += " tlTableView__headerCell--sortable"), A && A.column === N.name && (Q += " tlTableView__headerCell--dragOver-" + A.side);
        const ee = T < p, re = T === p - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.name,
            className: Q,
            "data-col-idx": T,
            style: {
              width: V,
              minWidth: V,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: M[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: N.sortable ? (oe) => ce(N.name, N.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Ge(T, oe),
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
              onClick: (oe) => Tn(N.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: N.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", k > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => F(N.name, V, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (N) => {
            if (H.current && u.length > 0) {
              const T = u[u.length - 1];
              T.name !== H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", W({ column: T.name, side: "right" }));
            }
          },
          onDrop: ve
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (B ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: Y },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Dn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Rn
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
      /* @__PURE__ */ e.createElement("div", { style: { height: U, position: "relative", width: vt, paddingRight: _t } }, i.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : "") + (N.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: N.index * r,
            height: r,
            width: vt,
            paddingRight: _t,
            ...N.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !kt(T) && T.preventDefault();
          },
          onClick: (T) => Te(N.index, T)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: v,
              minWidth: v,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (T) => T.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (T) => q(N.index, T),
              tabIndex: -1
            }
          )
        ),
        u.map((T, V) => {
          const Q = f(T), ee = V === u.length - 1, re = V < p, oe = V === p - 1;
          let he = "tlTableView__cell";
          re && (he += " tlTableView__cell--frozen"), oe && (he += " tlTableView__cell--frozenLast");
          const pe = h && V === 0, se = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: he,
              "data-row": N.id,
              "data-col": T.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: M[V], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => ie(N.index, !N.expanded, de)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), N.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: N.cells[T.name] })) : N.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: N.cells[T.name] })
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
      P.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Nn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Sn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, Ua = {
  "js.table.columnSearch": "Find column"
}, za = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ua), o = t.entries ?? [], u = o.filter((E) => E.visible).length, [s, i] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((E) => {
    m.current = E, h(E);
  }, []), k = e.useCallback((E, w) => {
    n("columnVisible", { column: E, visible: w });
  }, [n]), _ = e.useCallback((E, w) => {
    d.current = E, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", E);
  }, []), v = e.useCallback((E, w) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const b = w.currentTarget.getBoundingClientRect(), S = w.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: S });
  }, [g]), y = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), C = e.useCallback((E) => {
    E.preventDefault();
    const w = d.current, b = m.current;
    if (d.current = null, g(null), !w || !b)
      return;
    const S = o.findIndex(($) => $.name === b.name), I = o.findIndex(($) => $.name === w);
    if (S < 0 || I < 0)
      return;
    let R = b.side === "top" ? S : S + 1;
    I < R && R--, R !== I && n("columnReorder", { column: w, targetIndex: R });
  }, [o, n, g]), L = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: C }, L && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (E) => i(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, c.map((E) => {
    const w = E.visible && u <= 1;
    let b = "tlColumnSelect__row";
    return p && p.name === E.name && (b += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (S) => _(E.name, S),
        onDragOver: (S) => v(E.name, S),
        onDrop: C,
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
          onChange: (S) => k(E.name, S.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: Bt, useRef: Je, useCallback: mt, useMemo: Ae, useEffect: an } = e, Va = {
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
}, _e = 44, ft = 15, ye = 6e4, Ka = 36e5, Ie = 864e5, Ya = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function ze(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function Ga(l) {
  return Se(l);
}
function et(l, t) {
  return Se(l) === Se(t);
}
function Me(l) {
  return (l - Se(l)) / ye;
}
function Xe(l) {
  return Math.round(l / ft) * ft;
}
function qe(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function ht(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % Ya;
}
function bt(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function Xa(l) {
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
function qa(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Be(l, n, t.start) + "–" + Be(l, n, t.end);
}
const Za = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], Qa = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Za.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Ja(l) {
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
      topMin: Me(s.start),
      botMin: Me(s.start) + Math.max(15, (s.end - s.start) / ye),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && u(), n;
}
const St = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Ot = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
  const u = Je(!1), s = (i) => {
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
}, En = (l) => {
  const [t, n] = Bt(null), a = Je(null);
  a.current = t;
  const o = mt((i) => n(i), []), u = mt(() => n(null), []), s = mt(
    (i) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: i }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: u };
}, er = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: u, dayStartHour: s, dayEndHour: i, now: r, send: c, editable: d, i18n: m } = l, p = Ae(() => {
    const P = n === "DAY" ? 1 : 7, j = [];
    for (let B = 0; B < P; B++) {
      const x = ze(t, B);
      n === "WORK_WEEK" && u.includes(new Date(x).getDay()) || j.push(x);
    }
    return j;
  }, [t, n, u]), h = En(c), g = Je(null), k = Je(null), [_, v] = Bt(null), y = Je(null);
  y.current = _;
  const [C, L] = Bt(Date.now());
  an(() => {
    const P = window.setInterval(() => L(Date.now()), 6e4);
    return () => window.clearInterval(P);
  }, []);
  const E = mt(
    (P, j) => {
      const B = g.current;
      if (!B)
        return { dayIndex: 0, min: 0 };
      const x = B.getBoundingClientRect(), D = x.width / p.length, K = qe(Math.floor((P - x.left) / D), 0, p.length - 1), f = j - x.top + B.scrollTop, M = qe(f / _e * 60, 0, 1440);
      return { dayIndex: K, min: M };
    },
    [p.length]
  );
  an(() => {
    if (!_)
      return;
    const P = (x) => {
      const D = y.current;
      if (!D)
        return;
      const { dayIndex: K, min: f } = E(x.clientX, x.clientY);
      D.mode === "move" ? v({ ...D, dayStart: p[K], startMin: qe(Xe(f - D.grabMin), 0, 1440 - D.dur) }) : D.mode === "resize" ? v({ ...D, endMin: qe(Xe(f), D.startMin + ft, 1440) }) : v({ ...D, toMin: qe(Xe(f), 0, 1440) });
    }, j = () => {
      const x = y.current;
      if (v(null), !!x)
        if (x.mode === "move") {
          const D = x.dayStart + x.startMin * ye;
          D !== x.origStartMs && c("moveEvent", { eventId: x.id, start: D, end: D + x.dur * ye });
        } else if (x.mode === "resize") {
          const D = x.dayStart + x.endMin * ye;
          D !== x.origEndMs && c("resizeEvent", { eventId: x.id, end: D });
        } else {
          const D = Math.min(x.fromMin, x.toMin), K = Math.max(x.fromMin, x.toMin);
          K - D >= ft && h.open({ start: x.dayStart + D * ye, end: x.dayStart + K * ye, allDay: !1 });
        }
    }, B = () => v(null);
    return window.addEventListener("pointermove", P), window.addEventListener("pointerup", j, { once: !0 }), window.addEventListener("pointercancel", B), () => {
      window.removeEventListener("pointermove", P), window.removeEventListener("pointerup", j), window.removeEventListener("pointercancel", B);
    };
  }, [_, p, E, c, h.open]);
  const w = (P, j, B) => {
    if (!d || !j.movable)
      return;
    P.stopPropagation(), St(P), h.discard();
    const { min: x } = E(P.clientX, P.clientY), D = (j.end - j.start) / ye;
    v({
      mode: "move",
      id: j.id,
      grabMin: x - Me(j.start),
      dur: D,
      dayStart: B,
      startMin: Me(j.start),
      origStartMs: j.start
    });
  }, b = (P, j, B) => {
    !d || !j.resizable || (P.stopPropagation(), St(P), h.discard(), v({
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
    St(P), h.discard();
    const { min: B } = E(P.clientX, P.clientY);
    v({ mode: "create", dayStart: j, fromMin: Xe(B), toMin: Xe(B) });
  }, I = Array.from({ length: 24 }, (P, j) => j), R = Ae(() => {
    if (_ === null || !("id" in _))
      return a;
    const P = _;
    return a.map((j) => {
      if (j.id !== P.id)
        return j;
      if (P.mode === "move") {
        const B = P.dayStart + P.startMin * ye;
        return { ...j, start: B, end: B + P.dur * ye };
      }
      return { ...j, end: P.dayStart + P.endMin * ye };
    });
  }, [a, _]), $ = Ae(() => p.map(
    (P) => Ja(
      R.filter((j) => !j.allDay && j.start < P + Ie && j.end > P)
    )
  ), [p, R]), H = Ae(() => p.map((P) => R.filter((j) => j.allDay && j.start < P + Ie && j.end > P)), [p, R]), A = s * _e, W = i * _e;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((P) => {
    const j = u.includes(new Date(P).getDay()), B = et(P, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalDayHead" + (j ? " tlCalDayHead--nonworking" : "") + (B ? " tlCalDayHead--today" : ""),
        onClick: () => c("goto", { date: P, granularity: "DAY" })
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlCalDayName" }, Be(o, { weekday: "short" }, P)),
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
      Ot,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    H[j].map((B) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B.id,
        className: "tlCalAllDayEvent " + ht(B.category) + (B.selected ? " tlCalEvent--selected" : ""),
        style: bt(B),
        title: B.tooltip,
        onClick: (x) => {
          x.stopPropagation(), c("selectEvent", { eventId: B.id });
        }
      },
      B.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: k }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * _e } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, I.map((P) => /* @__PURE__ */ e.createElement("div", { key: P, className: "tlCalHourLabel", style: { top: P * _e } }, P === 0 ? "" : Be(o, { hour: "numeric" }, Se(t) + P * Ka)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((P, j) => {
    const B = u.includes(new Date(P).getDay()), x = _ && ("dayStart" in _ && _.dayStart === P) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: P,
        className: "tlCalCol" + (B ? " tlCalCol--nonworking" : ""),
        onPointerDown: (D) => S(D, P)
      },
      I.map((D) => /* @__PURE__ */ e.createElement("div", { key: D, className: "tlCalHourLine", style: { top: D * _e } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: W - A } }),
      et(P, C) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Me(Date.now()) / 60 * _e } }),
      $[j].map((D) => {
        const K = _ !== null && "id" in _ && _.id === D.ev.id, f = D.topMin / 60 * _e, M = (D.botMin - D.topMin) / 60 * _e, Y = 100 / D.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.ev.id,
            className: "tlCalEvent " + ht(D.ev.category) + (D.ev.selected ? " tlCalEvent--selected" : "") + (K ? " tlCalEvent--dragging" : ""),
            style: bt(D.ev, {
              top: f,
              height: M,
              left: `${D.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: D.ev.tooltip,
            onPointerDown: (U) => w(U, D.ev, P),
            onClick: (U) => {
              U.stopPropagation(), c("selectEvent", { eventId: D.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, qa(o, D.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, D.ev.title),
          d && D.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (U) => b(U, D.ev, P) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === P && /* @__PURE__ */ e.createElement(
        Ot,
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
      x && x.mode === "create" && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlCalEvent tlCalEvent--preview",
          style: {
            top: Math.min(x.fromMin, x.toMin) / 60 * _e,
            height: Math.abs(x.toMin - x.fromMin) / 60 * _e
          }
        }
      )
    );
  })))));
}, tr = 3, nr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: u, send: s, editable: i, now: r, i18n: c } = l, d = En(s), m = Ae(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const k = [];
      for (let _ = 0; _ < 7; _++)
        k.push(ze(t, g * 7 + _));
      h.push(k);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const k = h.dataTransfer.getData("text/plain"), _ = a.find((y) => y.id === k);
    if (!_ || !i || !_.movable)
      return;
    const v = g - Se(_.start);
    s("moveEvent", { eventId: k, start: _.start + v, end: _.end + v });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Be(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const k = h[0], _ = ze(k, 7), v = a.filter((C) => (C.allDay || C.end - C.start >= Ie) && C.start < _ && C.end > k).sort((C, L) => C.start - L.start).slice(0, 3), y = v.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((C) => {
      const L = new Date(C).getMonth() === new Date(n).getMonth(), E = u.includes(new Date(C).getDay()), w = et(C, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C,
          className: "tlCalMonthCell" + (L ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => p(b, C),
          onClick: () => i && d.open({ start: C, end: C + Ie, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (w ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: C, granularity: "DAY" });
            }
          },
          new Date(C).getDate()
        ),
        d.pending && d.pending.start === C && /* @__PURE__ */ e.createElement(
          Ot,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, v.map((C, L) => {
      const E = Math.max(0, Math.floor((Se(Math.max(C.start, k)) - k) / Ie)), w = Math.min(7, Math.ceil((C.end - k) / Ie));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C.id,
          className: "tlCalMonthBar " + ht(C.category) + (C.selected ? " tlCalEvent--selected" : ""),
          style: bt(C, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, w) + 1}`,
            gridRow: L + 1
          }),
          draggable: i && C.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", C.id),
          title: C.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: C.id });
          }
        },
        C.title
      );
    }), h.map((C, L) => {
      const E = a.filter((S) => !S.allDay && S.end - S.start < Ie && et(S.start, C)).sort((S, I) => S.start - I.start), w = E.slice(0, tr), b = E.length - w.length;
      return w.map((S, I) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.id,
          className: "tlCalChip " + ht(S.category) + (S.selected ? " tlCalEvent--selected" : ""),
          style: bt(S, { gridColumn: L + 1, gridRow: y + 1 + I }),
          draggable: i && S.movable,
          onDragStart: (R) => R.dataTransfer.setData("text/plain", S.id),
          title: S.tooltip,
          onClick: (R) => {
            R.stopPropagation(), s("selectEvent", { eventId: S.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Be(o, { hour: "numeric", minute: "2-digit" }, S.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, S.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + C,
              className: "tlCalMore",
              style: { gridColumn: L + 1, gridRow: y + 1 + w.length },
              onClick: () => s("goto", { date: C, granularity: "DAY" })
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
}, lr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: u, send: s, now: i } = l, r = Ae(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Se(h.start);
      const k = h.end;
      for (; g < k; )
        p.add(g), g = ze(g, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Ae(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const k = new Date(p);
      return k.setDate(p.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(k);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = Se(ze(p, -((h.getDay() - o + 7) % 7))), k = Array.from({ length: 42 }, (_, v) => ze(g, v));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Be(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((_, v) => /* @__PURE__ */ e.createElement("div", { key: "h" + v, className: "tlCalMiniWd" }, _)), k.map((_) => {
      const v = new Date(_).getMonth() === h.getMonth(), y = u.includes(new Date(_).getDay()), C = et(_, i), L = r.has(Ga(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (v ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (C ? " tlCalMiniDay--today" : "") + (L ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, ar = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Va), o = t.granularity ?? "WEEK", u = t.rangeStart ?? Date.now(), s = t.anchor ?? u, i = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: Xa(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(Qa, { title: i, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(nr, { ctx: r, rangeStart: u, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(lr, { ctx: r, rangeStart: u }) : /* @__PURE__ */ e.createElement(er, { ctx: r, rangeStart: u, granularity: o })));
}, rr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, vn = e.createContext(rr), { useMemo: or, useRef: sr, useState: cr, useEffect: ir } = e, ur = 320, dr = "TLTableView", mr = "TLPanel", pr = ({ controlId: l }) => {
  var _;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = sr(null), [r, c] = cr(
    a === "top" ? "top" : "side"
  );
  ir(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const v = i.current;
    if (!v) return;
    const y = new ResizeObserver((C) => {
      for (const L of C) {
        const w = L.contentRect.width / n;
        c(w < ur ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const d = or(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = u.length === 1 ? u[0] : void 0, g = !!h && (h.module === dr || h.module === mr && ((_ = h.state) == null ? void 0 : _.bare) === !0), k = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(vn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: p, ref: i }, u.map((v, y) => /* @__PURE__ */ e.createElement(G, { key: y, control: v }))));
}, { useCallback: fr } = e, hr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, br = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(hr), o = t.headerControl ?? null, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || u.length > 0 || s, p = fr(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((g, k) => /* @__PURE__ */ e.createElement(G, { key: k, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, k) => /* @__PURE__ */ e.createElement(G, { key: k, control: g }))));
}, { useContext: gr, useState: Er, useCallback: vr } = e, _r = ({ controlId: l }) => {
  const t = X(), n = gr(vn), a = t.label ?? "", o = t.required === !0, u = t.error, s = t.errorIcon, i = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, k = t.field, _ = n.readOnly, [v, y] = Er(!1), C = vr(() => y((S) => !S), []), L = m === "hidden", E = u != null, w = i != null && i.length > 0, b = [
    "tlFormField",
    `tlFormField--${m}`,
    _ ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: C,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: k })), !_ && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(It, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, u)), !_ && !E && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((S, I) => /* @__PURE__ */ e.createElement("div", { key: I, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(It, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, S)))), !_ && c && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Cr = "goto", yr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.iconCss, o = t.iconSrc, u = t.label, s = t.cssClass, i = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, u && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, u)), m = e.useCallback((g) => {
    g.preventDefault(), n(Cr, {});
  }, [n]), p = ["tlResourceCell", s].filter(Boolean).join(" "), h = i ? "key:tooltip" : void 0;
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
}, wr = 20, kr = () => {
  var w;
  const l = X(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((w = n.find((b) => b.selected)) == null ? void 0 : w.id) ?? null;
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
    var R;
    const I = window.getSelection();
    I && !I.isCollapsed && S.currentTarget.contains(I.anchorNode) || ((R = d.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: S.ctrlKey || S.metaKey,
      shiftKey: S.shiftKey
    }));
  }, [t]), g = e.useCallback((b, S) => {
    S.preventDefault(), t("contextMenu", { nodeId: b, x: S.clientX, y: S.clientY });
  }, [t]), k = e.useRef(null), _ = e.useCallback((b, S) => {
    const I = S.getBoundingClientRect(), R = b.clientY - I.top, $ = I.height / 3;
    return R < $ ? "above" : R > $ * 2 ? "below" : "within";
  }, []), v = e.useCallback((b, S) => {
    S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, S) => {
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const I = _(S, S.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: I }), k.current = null;
    }, 50);
  }, [t, _]), C = e.useCallback((b, S) => {
    S.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const I = _(S, S.currentTarget);
    t("drop", { nodeId: b, position: I });
  }, [t, _]), L = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
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
          const I = n[r];
          if (I.expandable && !I.expanded) {
            t("expand", { nodeId: I.id });
            return;
          } else I.expanded && (S = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const I = n[r];
          if (I.expanded) {
            t("collapse", { nodeId: I.id });
            return;
          } else {
            const R = I.depth;
            for (let $ = r - 1; $ >= 0; $--)
              if (n[$].depth < R) {
                S = $;
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
          s === b.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * wr },
        draggable: o,
        onMouseDown: (I) => {
          (I.shiftKey || I.ctrlKey || I.metaKey || I.detail > 1) && I.preventDefault();
        },
        onClick: (I) => h(b.id, I),
        onContextMenu: (I) => g(b.id, I),
        onDragStart: (I) => v(b.id, I),
        onDragOver: u ? (I) => y(b.id, I) : void 0,
        onDrop: u ? (I) => C(b.id, I) : void 0,
        onDragEnd: L
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (I) => {
            I.stopPropagation(), p(b.id, b.expanded);
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
var Dt = { exports: {} }, be = {}, Tt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var rn;
function Nr() {
  if (rn) return J;
  rn = 1;
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
  }, k = Object.assign, _ = {};
  function v(f, M, Y) {
    this.props = f, this.context = M, this.refs = _, this.updater = Y || g;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(f, M) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, M, "setState");
  }, v.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function y() {
  }
  y.prototype = v.prototype;
  function C(f, M, Y) {
    this.props = f, this.context = M, this.refs = _, this.updater = Y || g;
  }
  var L = C.prototype = new y();
  L.constructor = C, k(L, v.prototype), L.isPureReactComponent = !0;
  var E = Array.isArray;
  function w() {
  }
  var b = { H: null, A: null, T: null, S: null }, S = Object.prototype.hasOwnProperty;
  function I(f, M, Y) {
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
    return I(f.type, M, f.props);
  }
  function $(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function H(f) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var A = /\/+/g;
  function W(f, M) {
    return typeof f == "object" && f !== null && f.key != null ? H("" + f.key) : M.toString(36);
  }
  function P(f) {
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
  function j(f, M, Y, U, Z) {
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
              return te = f._init, j(
                te(f._payload),
                M,
                Y,
                U,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = U === "" ? "." + W(f, 0) : U, E(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), j(Z, M, Y, "", function(ge) {
        return ge;
      })) : Z != null && ($(Z) && (Z = R(
        Z,
        Y + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var ce = U === "" ? "." : U + ":";
    if (E(f))
      for (var ae = 0; ae < f.length; ae++)
        U = f[ae], F = ce + W(U, ae), te += j(
          U,
          M,
          Y,
          F,
          Z
        );
    else if (ae = h(f), typeof ae == "function")
      for (f = ae.call(f), ae = 0; !(U = f.next()).done; )
        U = U.value, F = ce + W(U, ae++), te += j(
          U,
          M,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof f.then == "function")
        return j(
          P(f),
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
  function B(f, M, Y) {
    if (f == null) return f;
    var U = [], Z = 0;
    return j(f, U, "", "", function(F) {
      return M.call(Y, F, Z++);
    }), U;
  }
  function x(f) {
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
  var D = typeof reportError == "function" ? reportError : function(f) {
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
  }, K = {
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
      if (!$(f))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return f;
    }
  };
  return J.Activity = m, J.Children = K, J.Component = v, J.Fragment = n, J.Profiler = o, J.PureComponent = C, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
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
    var U = k({}, f.props), Z = f.key;
    if (M != null)
      for (F in M.key !== void 0 && (Z = "" + M.key), M)
        !S.call(M, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && M.ref === void 0 || (U[F] = M[F]);
    var F = arguments.length - 2;
    if (F === 1) U.children = Y;
    else if (1 < F) {
      for (var te = Array(F), ce = 0; ce < F; ce++)
        te[ce] = arguments[ce + 2];
      U.children = te;
    }
    return I(f.type, Z, U);
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
        S.call(M, U) && U !== "key" && U !== "__self" && U !== "__source" && (Z[U] = M[U]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = Y;
    else if (1 < te) {
      for (var ce = Array(te), ae = 0; ae < te; ae++)
        ce[ae] = arguments[ae + 2];
      Z.children = ce;
    }
    if (f && f.defaultProps)
      for (U in te = f.defaultProps, te)
        Z[U] === void 0 && (Z[U] = te[U]);
    return I(f, F, Z);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(f) {
    return { $$typeof: i, render: f };
  }, J.isValidElement = $, J.lazy = function(f) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: f },
      _init: x
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
      Z !== null && Z(Y, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(w, D);
    } catch (F) {
      D(F);
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
var on;
function Sr() {
  return on || (on = 1, Tt.exports = Nr()), Tt.exports;
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
var sn;
function Dr() {
  if (sn) return be;
  sn = 1;
  var l = Sr();
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
var cn;
function Tr() {
  if (cn) return Dt.exports;
  cn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Dt.exports = Dr(), Dt.exports;
}
var _n = Tr();
const { useState: xe, useCallback: fe, useRef: Ze, useEffect: He, useMemo: Ft } = e, Rr = "goto", Lr = "option";
function Ut({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function xr({
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
      onDragStart: u,
      onDragOver: s,
      onDrop: i,
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
function Mr({
  option: l,
  onGoto: t
}) {
  const n = fe(
    (o) => {
      o.preventDefault(), t(l.value);
    },
    [t, l.value]
  ), a = /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(Ut, { image: l.image }), /* @__PURE__ */ e.createElement("span", null, l.label));
  return l.link ? /* @__PURE__ */ e.createElement("a", { className: "tlDropdownSelect__readonlyValue tlResourceCell", href: "#", onClick: n }, a) : /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__readonlyValue" }, a);
}
function Ir({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: u
}) {
  const s = fe(() => a(l.value), [a, l.value]), i = Ft(() => {
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
    /* @__PURE__ */ e.createElement(Ut, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const Pr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = u && o && !i && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], k = fe(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, v] = xe(!1), [y, C] = xe(""), [L, E] = xe(-1), [w, b] = xe(!1), [S, I] = xe({}), [R, $] = xe(null), [H, A] = xe(null), [W, P] = xe(null), j = Ze(null), B = Ze(null), x = Ze(null), D = Ze(a);
  D.current = a;
  const K = Ze(-1), f = Ft(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = Ft(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (y) {
      const q = y.toLowerCase();
      O = O.filter((le) => le.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, y]);
  He(() => {
    y && M.length === 1 ? E(0) : E(-1);
  }, [M.length, y]), He(() => {
    _ && c && B.current && B.current.focus();
  }, [_, c, a]), He(() => {
    var le, ie;
    if (K.current < 0) return;
    const O = K.current;
    K.current = -1;
    const q = (le = j.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = j.current) == null || ie.focus();
  }, [a]), He(() => {
    if (!_) return;
    const O = (q) => {
      j.current && !j.current.contains(q.target) && x.current && !x.current.contains(q.target) && (v(!1), C(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), He(() => {
    if (!_ || !j.current) return;
    const O = j.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    I({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = fe(async () => {
    if (!(i || !r) && (v(!0), C(""), E(-1), b(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [i, r, c, n]), U = fe(() => {
    var O;
    v(!1), C(""), E(-1), (O = j.current) == null || O.focus();
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
      D.current = q, n(at, { value: q.map((le) => le.value) }), o ? (C(""), E(-1)) : U();
    },
    [o, d, n, U]
  ), F = fe(
    (O) => {
      K.current = D.current.findIndex((le) => le.value === O);
      const q = D.current.filter((le) => le.value !== O);
      D.current = q, n(at, { value: q.map((le) => le.value) });
    },
    [n]
  ), te = fe(
    (O) => {
      O.stopPropagation(), n(at, { value: [] }), U();
    },
    [n, U]
  ), ce = fe((O) => {
    C(O.target.value);
  }, []), ae = fe(
    (O) => {
      n(Rr, { [Lr]: O });
    },
    [n]
  ), ge = fe(
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
            (q) => q < M.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), E(
            (q) => q > 0 ? q - 1 : M.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), L >= 0 && L < M.length && Z(M[L].value);
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
      _,
      Y,
      U,
      M,
      L,
      Z,
      y,
      o,
      a,
      F
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
      $(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), Te = fe(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", R === null || R === O) {
        A(null), P(null);
        return;
      }
      const le = q.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Ge = q.clientX < ie ? "before" : "after";
      A(O), P(Ge);
    },
    [R]
  ), Re = fe(
    (O) => {
      if (O.preventDefault(), R === null || H === null || W === null || R === H) return;
      const q = [...D.current], [le] = q.splice(R, 1);
      let ie = H;
      R < H ? ie = W === "before" ? ie - 1 : ie : ie = W === "before" ? ie : ie + 1, q.splice(ie, 0, le), D.current = q, n(at, { value: q.map((Ge) => Ge.value) }), $(null), A(null), P(null);
    },
    [R, H, W, n]
  ), Fe = fe(() => {
    $(null), A(null), P(null);
  }, []);
  if (He(() => {
    if (L < 0 || !x.current) return;
    const O = x.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement(Mr, { key: O.value, option: O, onGoto: ae })));
  const Et = !s && a.length > 0 && !i, lt = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: x,
      className: "tlDropdownSelect__dropdown",
      style: S,
      ...Pn
    },
    (c || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
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
      !c && !w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ve }, h["js.dropdownSelect.error"])),
      c && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      c && M.map((O, q) => /* @__PURE__ */ e.createElement(
        Ir,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === L,
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
      ref: j,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: _ ? void 0 : Y,
      onKeyDown: ge
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let le = "";
      return R === q ? le = "tlDropdownSelect__chip--dragging" : H === q && W === "before" ? le = "tlDropdownSelect__chip--dropBefore" : H === q && W === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        xr,
        {
          key: O.value,
          option: O,
          removable: !i && (o || !s),
          onRemove: F,
          removeLabel: k(O.label),
          draggable: p,
          onDragStart: p ? (ie) => we(q, ie) : void 0,
          onDragOver: p ? (ie) => Te(q, ie) : void 0,
          onDrop: p ? Re : void 0,
          onDragEnd: p ? Fe : void 0,
          dragClassName: p ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Et && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), lt && _n.createPortal(lt, document.body));
}, { useCallback: Rt, useRef: jr } = e, Cn = "application/x-tl-color", Ar = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: u
}) => {
  const s = jr(null), i = Rt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Rt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = Rt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(Cn);
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
function yn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function $t(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function wn(l) {
  if (!$t(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function kn(l, t, n) {
  const a = (o) => yn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Br(l, t, n) {
  const a = l / 255, o = t / 255, u = n / 255, s = Math.max(a, o, u), i = Math.min(a, o, u), r = s - i;
  let c = 0;
  r !== 0 && (s === a ? c = (o - u) / r % 6 : s === o ? c = (u - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function Or(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), u = n - a;
  let s = 0, i = 0, r = 0;
  return l < 60 ? (s = a, i = o, r = 0) : l < 120 ? (s = o, i = a, r = 0) : l < 180 ? (s = 0, i = a, r = o) : l < 240 ? (s = 0, i = o, r = a) : l < 300 ? (s = o, i = 0, r = a) : (s = a, i = 0, r = o), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((r + u) * 255)
  ];
}
function Fr(l) {
  return Br(...wn(l));
}
function Lt(l, t, n) {
  return kn(...Or(l, t, n));
}
const { useCallback: We, useRef: un } = e, $r = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = Fr(l), u = un(null), s = un(null), i = We(
    (g, k) => {
      var C;
      const _ = (C = u.current) == null ? void 0 : C.getBoundingClientRect();
      if (!_) return;
      const v = Math.max(0, Math.min(1, (g - _.left) / _.width)), y = Math.max(0, Math.min(1, 1 - (k - _.top) / _.height));
      t(Lt(n, v, y));
    },
    [n, t]
  ), r = We(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), i(g.clientX, g.clientY);
    },
    [i]
  ), c = We(
    (g) => {
      g.buttons !== 0 && i(g.clientX, g.clientY);
    },
    [i]
  ), d = We(
    (g) => {
      var y;
      const k = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!k) return;
      const v = Math.max(0, Math.min(1, (g - k.top) / k.height)) * 360;
      t(Lt(v, a, o));
    },
    [a, o, t]
  ), m = We(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = We(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Lt(n, 1, 1);
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
function Hr(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Wr = {
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
}, { useState: ct, useCallback: ke, useEffect: dn, useRef: Ur, useLayoutEffect: zr } = e, Vr = ({
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
  const [c, d] = ct("palette"), [m, p] = ct(t), h = Ur(null), g = ue(Wr), [k, _] = ct(null);
  zr(() => {
    if (!l.current || !h.current) return;
    const x = l.current.getBoundingClientRect(), D = h.current.getBoundingClientRect();
    let K = x.bottom + 4, f = x.left;
    K + D.height > window.innerHeight && (K = x.top - D.height - 4), f + D.width > window.innerWidth && (f = Math.max(0, x.right - D.width)), _({ top: K, left: f });
  }, [l]);
  const v = m != null, [y, C, L] = v ? wn(m) : [0, 0, 0], [E, w] = ct((m == null ? void 0 : m.toUpperCase()) ?? "");
  dn(() => {
    w((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Oe(!0, { ESCAPE: i }), dn(() => {
    const x = (K) => {
      h.current && !h.current.contains(K.target) && i();
    }, D = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", x);
    };
  }, [i]);
  const b = ke(
    (x) => (D) => {
      const K = parseInt(D.target.value, 10);
      if (isNaN(K)) return;
      const f = yn(K);
      p(kn(x === "r" ? f : y, x === "g" ? f : C, x === "b" ? f : L));
    },
    [y, C, L]
  ), S = ke(
    (x) => {
      if (m != null) {
        x.dataTransfer.setData(Cn, m.toUpperCase()), x.dataTransfer.effectAllowed = "move";
        const D = document.createElement("div");
        D.style.width = "33px", D.style.height = "33px", D.style.backgroundColor = m, D.style.borderRadius = "3px", D.style.border = "1px solid rgba(0,0,0,0.1)", D.style.position = "absolute", D.style.top = "-9999px", document.body.appendChild(D), x.dataTransfer.setDragImage(D, 16, 16), requestAnimationFrame(() => document.body.removeChild(D));
      }
    },
    [m]
  ), I = ke((x) => {
    const D = x.target.value;
    w(D), $t(D) && p(D);
  }, []), R = ke(() => {
    p(null);
  }, []), $ = ke((x) => {
    p(x);
  }, []), H = ke(
    (x) => {
      s(x);
    },
    [s]
  ), A = ke(
    (x, D) => {
      const K = [...n], f = K[x];
      K[x] = K[D], K[D] = f, r(K);
    },
    [n, r]
  ), W = ke(
    (x, D) => {
      const K = [...n];
      K[x] = D, r(K);
    },
    [n, r]
  ), P = ke(() => {
    r([...o]);
  }, [o, r]), j = ke(
    (x) => {
      if (Hr(n, x)) return;
      const D = n.indexOf(null);
      if (D < 0) return;
      const K = [...n];
      K[D] = x.toUpperCase(), r(K);
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
      style: k ? { top: k.top, left: k.left, visibility: "visible" } : { visibility: "hidden" }
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
      Ar,
      {
        colors: n,
        columns: a,
        onSelect: $,
        onConfirm: H,
        onSwap: A,
        onReplace: W
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: P }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement($r, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: m } : void 0,
        draggable: v,
        onDragStart: v ? S : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? C : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
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
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: B }, g["js.colorInput.ok"]))
  );
}, Kr = { "js.colorInput.chooseColor": "Choose color" }, { useState: Yr, useCallback: it, useRef: Gr } = e, Xr = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(Kr), [s, i] = Yr(!1), r = Gr(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = it(() => {
    d && i(!0);
  }, [d]), k = it(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), _ = it(() => {
    i(!1);
  }, []), v = it(
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
    Vr,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: k,
      onCancel: _,
      onPaletteChange: v
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
}, { useState: Qe, useCallback: je, useEffect: xt, useRef: mn, useLayoutEffect: qr, useMemo: Zr } = e, Qr = {
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
}, Jr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = ue(Qr), [r, c] = Qe("simple"), [d, m] = Qe(""), [p, h] = Qe(t ?? ""), [g, k] = Qe(!1), [_, v] = Qe(null), y = mn(null), C = mn(null);
  qr(() => {
    if (!l.current || !y.current) return;
    const H = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let W = H.bottom + 4, P = H.left;
    W + A.height > window.innerHeight && (W = H.top - A.height - 4), P + A.width > window.innerWidth && (P = Math.max(0, H.right - A.width)), v({ top: W, left: P });
  }, [l]), xt(() => {
    !a && !g && s().catch(() => k(!0));
  }, [a, g, s]), xt(() => {
    a && C.current && C.current.focus();
  }, [a]), Oe(!0, { ESCAPE: u }), xt(() => {
    const H = (W) => {
      y.current && !y.current.contains(W.target) && u();
    }, A = setTimeout(() => document.addEventListener("mousedown", H), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", H);
    };
  }, [u]);
  const L = Zr(() => {
    if (!d) return n;
    const H = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(H) || A.label.toLowerCase().includes(H) || A.terms != null && A.terms.some((W) => W.includes(H))
    );
  }, [n, d]), E = je((H) => {
    m(H.target.value);
  }, []), w = je(
    (H) => {
      o(H);
    },
    [o]
  ), b = je((H) => {
    h(H);
  }, []), S = je((H) => {
    h(H.target.value);
  }, []), I = je(() => {
    o(p || null);
  }, [p, o]), R = je(() => {
    o(null);
  }, [o]), $ = je(async (H) => {
    H.preventDefault(), k(!1);
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
      ref: y,
      style: _ ? { top: _.top, left: _.left, visibility: "visible" } : { visibility: "hidden" }
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
        ref: C,
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: $ }, i["js.iconSelect.loadError"])),
      a && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      a && L.map(
        (H) => H.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: H.label,
            onClick: () => r === "simple" ? w(A.encoded) : b(A.encoded),
            onKeyDown: (W) => {
              (W.key === "Enter" || W.key === " ") && (W.preventDefault(), r === "simple" ? w(A.encoded) : b(A.encoded));
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
        onChange: S
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: I }, i["js.iconSelect.ok"]))
  );
}, eo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: to, useCallback: ut, useRef: no } = e, lo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(eo), [s, i] = to(!1), r = no(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = ut(() => {
    d && !m && i(!0);
  }, [d, m]), k = ut(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), _ = ut(() => {
    i(!1);
  }, []), v = ut(async () => {
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
    Jr,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: k,
      onCancel: _,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ue, useEffect: ao, useMemo: pn, useRef: ro, useState: Mt } = e, oo = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, so = [1, 2, 3, 4];
function co(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function io(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of so)
    n >= o && (a = o);
  return a;
}
function uo(l, t) {
  const n = oo[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function mo(l, t) {
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
        for (let k = p.colEnd; k < h; k++) u(g, k);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(uo(m.width, n), n);
    for (; o(i, r); )
      r++, r >= n && (r = 0, i++);
    let g = 0;
    for (let C = r; C < n && !o(i, C); C++)
      g++;
    if (h > g) {
      for (c(i), r = 0, i++; o(i, r); )
        r++, r >= n && (r = 0, i++);
      g = 0;
      for (let C = r; C < n && !o(i, C); C++)
        g++;
      h = Math.min(h, g);
    }
    const k = r, _ = r + h, v = i, y = i + p;
    s.push({ id: m.id, colStart: k, colEnd: _, rowStart: v, rowEnd: y });
    for (let C = v; C < y; C++)
      for (let L = k; L < _; L++) u(C, L);
    r = _, r >= n && (r = 0, i++);
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
const po = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((w) => w && w.id), u = ro(null), [s, i] = Mt(1), r = t.editMode === !0;
  ao(() => {
    const w = u.current;
    if (!w) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, S = co(a, b), I = () => i(io(w.clientWidth, S));
    I();
    const R = new ResizeObserver(I);
    return R.observe(w), () => R.disconnect();
  }, [a]);
  const c = pn(() => mo(o, s), [o, s]), d = pn(() => {
    const w = {};
    for (const b of c) w[b.id] = b;
    return w;
  }, [c]), [m, p] = Mt(null), [h, g] = Mt(null), k = Ue((w, b) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    p(b), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, [r]), _ = Ue((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const S = w.currentTarget.getBoundingClientRect(), I = w.clientX < S.left + S.width / 2;
    g((R) => R && R.id === b && R.before === I ? R : { id: b, before: I });
  }, [r, m]), v = Ue(() => {
  }, []), y = Ue((w, b, S) => {
    const I = o.map((A) => A.id), R = I.indexOf(w);
    if (R < 0) return;
    I.splice(R, 1);
    const $ = I.indexOf(b);
    if ($ < 0) {
      I.splice(R, 0, w);
      return;
    }
    const H = S ? $ : $ + 1;
    I.splice(H, 0, w), n("reorder", { order: I });
  }, [o, n]), C = Ue((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault();
    const S = w.currentTarget.getBoundingClientRect(), I = w.clientX < S.left + S.width / 2;
    y(m, b, I), p(null), g(null);
  }, [r, m, y]), L = Ue(() => {
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
      const S = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, I = ["tlDashboard__tile"];
      return m === w.id && I.push("tlDashboard__tile--dragging"), h && h.id === w.id && I.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: I.join(" "),
          style: S,
          draggable: r,
          onDragStart: (R) => k(R, w.id),
          onDragOver: (R) => _(R, w.id),
          onDragLeave: v,
          onDrop: (R) => C(R, w.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(G, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: fo, useRef: fn, useState: hn, useEffect: ho, useLayoutEffect: bo } = e, go = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Eo = ({ group: l }) => {
  var m, p;
  const [t, n] = hn(!1), [a, o] = hn({}), u = fn(null), s = fn(null), i = fo(() => {
    n((h) => !h);
  }, []);
  bo(() => {
    if (!t) return;
    const h = () => {
      const g = u.current;
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
  }, [t]), ho(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && u.current && !u.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Oe(t, { ESCAPE: () => n(!1) }), Wt(t, s, "first");
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
  ), _n.createPortal(
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
      (p = l.subGroups) == null ? void 0 : p.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((k, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: k })))))
    ),
    document.body
  ));
}, vo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((u) => u != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, u) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, u > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Eo, { group: o }) : /* @__PURE__ */ e.createElement(go, { group: o }))));
}, _o = ({ frame: l, covered: t }) => {
  const [n, a] = tt(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(nt, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, Co = ({ controlId: l }) => {
  const t = X(), [n, a] = tt(), o = t.frames ?? [], u = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(nt, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, i) => /* @__PURE__ */ e.createElement(_o, { key: s.controlId, frame: s, covered: i !== u }))));
}, yo = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.content, o = t.breadcrumb ?? null;
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
}, wo = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, ko = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), No = {
  "js.sidebar.openDrawer": "Open navigation"
}, So = ({ controlId: l }) => {
  const t = ne(), n = ue(No);
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
z("TLButton", Jn);
z("TLUploadButton", el);
z("TLToggleButton", nl);
z("TLTextInput", Bn);
z("TLPasswordInput", Fn);
z("TLNumberInput", Hn);
z("TLDatePicker", Un);
z("TLSelect", Vn);
z("TLBooleanChoice", Yn);
z("TLCheckbox", Zn);
z("TLCounter", ll);
z("TLTabBar", rl);
z("TLFieldList", ol);
z("TLAudioRecorder", cl);
z("TLAudioPlayer", ul);
z("TLFileUpload", ml);
z("TLBinaryField", fl);
z("TLFileChips", gl);
z("TLRelativeTime", _l);
z("TLAnchor", Cl);
z("TLScrollLink", yl);
z("TLAvatar", Nl);
z("TLDownload", Dl);
z("TLPhotoCapture", Rl);
z("TLPhotoViewer", xl);
z("TLPdfViewer", Il);
z("TLSplitPanel", Pl);
z("TLPanel", Hl);
z("TLInset", Jl);
z("TLMaximizeRoot", Wl);
z("TLDeckPane", Ul);
z("TLSidebar", Zl);
z("TLStack", Ql);
z("TLGrid", ea);
z("TLCard", ta);
z("TLAppBar", na);
z("TLBreadcrumb", aa);
z("TLBottomBar", oa);
z("TLDialog", ia);
z("TLDialogManager", ma);
z("TLWindow", ba);
z("TLDrawer", va);
z("TLMenuRegion", Ca);
z("TLSnackbar", Na);
z("TLNoticeBar", Ma);
z("TLMenu", Pa);
z("TLAppShell", Aa);
z("TLText", Ba);
z("TLTableView", Wa);
z("TLColumnSelect", za);
z("TLCalendar", ar);
z("TLFormLayout", pr);
z("TLFormGroup", br);
z("TLFormField", _r);
z("TLResourceCell", yr);
z("TLTreeView", kr);
z("TLDropdownSelect", Pr);
z("TLColorInput", Xr);
z("TLIconSelect", lo);
z("TLDashboard", po);
z("TLToolbar", vo);
z("TLTileStack", Co);
z("TLAdaptiveDetail", yo);
z("TLSlot", wo);
z("TLSlotContent", ko);
z("TLDrawerToggle", So);
