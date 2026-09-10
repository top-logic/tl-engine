import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as X, useKeyboardBinding as me, useTLUpload as Ge, useFill as yt, FillBarrier as Pe, TLChild as G, useI18N as ue, useTLDataUrl as Xe, scrollToAnchor as el, useStandaloneKeyboardScope as Oe, useFillHost as rt, FillProvider as ot, KeyboardScopeProvider as Kt, useFocusTrap as Yt, CMD_VALUE_CHANGED as ct, anchoredOverlayProps as tl, register as z } from "tl-react-bridge";
const { useCallback: Jt, useRef: nl } = e, ll = 300, al = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: ll,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ne(), s = nl(!1), i = Jt(
    (D) => {
      s.current = !0, a(D.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = Jt(async () => {
    await o(), r && s.current && (s.current = !1, u("commit"));
  }, [o, r, u]), d = t.multiline === !0;
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
      onChange: i,
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
      onChange: i,
      onBlur: c,
      disabled: t.disabled === !0,
      className: b,
      "aria-invalid": m || void 0,
      title: m && h ? h : void 0
    }
  ));
}, { useCallback: en } = e, rl = 300, ol = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: rl }), u = en(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = en(() => {
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
}, { useCallback: tn } = e, sl = 300, cl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: sl,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = tn(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = tn(() => {
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
}, { useCallback: il } = e, ul = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = il(
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
}, { useCallback: dl } = e, ml = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), u = dl(
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
}, { useCallback: pl } = e, fl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], u = t.presentation === "select", s = t.disabled === !0, i = t.hasError === !0, r = t.hasWarnings === !0, c = pl(
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
}, { useCallback: hl, useRef: bl, useEffect: gl } = e, El = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, u = bl(null);
  gl(() => {
    u.current && (u.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = hl(
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
const { useCallback: vl } = e, _l = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: u }) => {
  const s = X(), i = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = u ?? s.displayMode ?? "label-only", h = s.hidden === !0, b = s.tooltip, D = s.appearance, _ = s.size, v = s.cssClasses, y = s.navigateUrl, N = vl(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    i(r);
  }, [i, r, y]), x = s.keyGesture;
  me(x, () => m || h ? !1 : (N(), !0));
  const g = p === "icon-only", w = p === "label-only" || p === "icon-label" || g && !d, S = b ?? (g ? c : void 0), k = S ? `text:${S}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: N,
      disabled: m,
      className: "tlReactButton" + (g ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (D === "link" ? " tlReactButton--link" : "") + (D === "primary" ? " tlReactButton--primary" : "") + (_ === "small" ? " tlReactButton--small" : "") + (_ === "large" ? " tlReactButton--large" : "") + (v ? " " + v : ""),
      "data-tooltip": k,
      "aria-label": d || g ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, Cl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = e.useRef(null), [o, u] = e.useState(!1), s = t.label ?? "", i = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, b = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), D = e.useCallback(async (x) => {
    const g = x.target.files;
    if (!g || g.length === 0) return;
    const w = new FormData();
    for (let S = 0; S < g.length; S++)
      w.append("file", g[S], g[S].name);
    x.target.value = "", u(!0);
    try {
      await n(w);
    } finally {
      u(!1);
    }
  }, [n]), _ = d === "icon-only", v = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || _ && !i, N = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: D,
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
    v && i && /* @__PURE__ */ e.createElement(Ne, { encoded: i, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: yl } = e, wl = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const u = X(), s = ne(), i = t ?? "click", r = n ?? u.label, c = a ?? u.active === !0, d = o ?? u.disabled === !0, m = yl(() => {
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
}, kl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Nl } = e, Sl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0), o = t.tabs ?? [], u = t.activeTabId, s = Nl((i) => {
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
}, Dl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, Tl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Rl = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, N ? { mimeType: N } : void 0);
        i.current = x, x.ondataavailable = (g) => {
          g.data.size > 0 && r.current.push(g.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((S) => S.stop()), c.current = null;
          const g = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], g.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const w = new FormData();
          w.append("audio", g, "recording.webm"), await n(w), o("idle");
        }, x.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), b = ue(Tl), D = p === "recording" ? b["js.audioRecorder.stop"] : p === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], _ = p === "uploading", v = ["tlAudioRecorder__button"];
  return p === "recording" && v.push("tlAudioRecorder__button--recording"), p === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: h,
      disabled: _,
      title: D,
      "aria-label": D
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Ll = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, xl = ({ controlId: l }) => {
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
    const D = new Audio(r.current);
    i.current = D, D.onended = () => {
      s("idle");
    }, D.play(), s("playing");
  }, [u, n]), m = ue(Ll), p = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = u === "disabled" || u === "loading", b = ["tlAudioPlayer__button"];
  return u === "playing" && b.push("tlAudioPlayer__button--playing"), u === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: h,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Il = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ml = ({ controlId: l }) => {
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (g) => {
    o("uploading");
    const w = new FormData();
    w.append("file", g, g.name), await n(w), o("idle");
  }, [n]), h = e.useCallback((g) => {
    var S;
    const w = (S = g.target.files) == null ? void 0 : S[0];
    w && p(w);
  }, [p]), b = e.useCallback(() => {
    var g;
    a !== "uploading" && ((g = i.current) == null || g.click());
  }, [a]), D = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), s(!0);
  }, []), _ = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), s(!1);
  }, []), v = e.useCallback((g) => {
    var S;
    if (g.preventDefault(), g.stopPropagation(), s(!1), a === "uploading") return;
    const w = (S = g.dataTransfer.files) == null ? void 0 : S[0];
    w && p(w);
  }, [a, p]), y = m === "uploading", N = ue(Il), x = m === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: D,
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
        onClick: b,
        disabled: y,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, jl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Pl = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ge(), u = Xe(), s = ue(jl), i = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [b, D] = e.useState("idle"), [_, v] = e.useState(!1), [y, N] = e.useState(!1), x = e.useRef(null), g = e.useCallback(async () => {
    if (!(!r || y)) {
      N(!0);
      try {
        const M = u + (u.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(M);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const R = await L.blob(), $ = URL.createObjectURL(R), f = document.createElement("a");
        f.href = $, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL($);
      } catch (M) {
        console.error("[TLBinaryField] Fetch error:", M);
      } finally {
        N(!1);
      }
    }
  }, [r, y, u, d, c]), w = e.useCallback(async (M) => {
    D("uploading");
    const L = new FormData();
    L.append("file", M, M.name), await o(L), D("idle");
  }, [o]), S = (p === "received" ? "idle" : b !== "idle" ? b : p) === "uploading", k = e.useCallback((M) => {
    var R;
    const L = (R = M.target.files) == null ? void 0 : R[0];
    L && w(L);
  }, [w]), j = e.useCallback(() => {
    var M;
    S || (M = x.current) == null || M.click();
  }, [S]), C = e.useCallback((M) => {
    M.preventDefault(), M.stopPropagation(), v(!0);
  }, []), H = e.useCallback((M) => {
    M.preventDefault(), M.stopPropagation(), v(!1);
  }, []), W = e.useCallback((M) => {
    var R;
    if (M.preventDefault(), M.stopPropagation(), v(!1), S) return;
    const L = (R = M.dataTransfer.files) == null ? void 0 : R[0];
    L && w(L);
  }, [S, w]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), K = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: g,
      disabled: y,
      title: A,
      "aria-label": A
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c));
  if (!i)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, K) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const B = S, P = S ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${_ ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: H,
      onDrop: W
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "file",
        accept: m || void 0,
        onChange: k,
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
    r && K,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Bl = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Al(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Fl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ge(), o = Xe(), u = ue(Bl), s = t.chips ?? [], i = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (g) => {
    const w = Array.from(g);
    if (w.length !== 0) {
      c(!0);
      try {
        const S = new FormData();
        for (const k of w)
          S.append("file", k, k.name);
        await a(S);
      } finally {
        c(!1);
      }
    }
  }, [a]), b = e.useCallback(async (g) => {
    if (g.hasData)
      try {
        const w = o + "&key=" + encodeURIComponent(g.key), S = await fetch(w);
        if (!S.ok) {
          console.error("[TLFileChips] Failed to fetch data:", S.status);
          return;
        }
        const k = await S.blob(), j = URL.createObjectURL(k), C = document.createElement("a");
        C.href = j, C.download = g.name, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(j);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [o]), D = e.useCallback((g) => {
    g.target.files && h(g.target.files), g.target.value = "";
  }, [h]), _ = e.useCallback(() => {
    var g;
    r || (g = p.current) == null || g.click();
  }, [r]), v = e.useCallback((g) => {
    i && (g.preventDefault(), g.stopPropagation(), m(!0));
  }, [i]), y = e.useCallback((g) => {
    i && (g.preventDefault(), g.stopPropagation(), m(!1));
  }, [i]), N = e.useCallback((g) => {
    i && (g.preventDefault(), g.stopPropagation(), m(!1), !r && g.dataTransfer.files && h(g.dataTransfer.files));
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
      onDragOver: v,
      onDragLeave: y,
      onDrop: N
    },
    s.map((g) => {
      const w = u["js.download.file"].replace("{0}", g.name), S = u["js.fileChips.remove"].replace("{0}", g.name);
      return /* @__PURE__ */ e.createElement("span", { key: g.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(g),
          disabled: !g.hasData,
          title: g.hasData ? w : g.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, g.name),
        g.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Al(g.size))
      ), i && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: g.key }),
          title: S,
          "aria-label": S
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
}, Ol = 3e4;
function $l(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Hl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, u] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => u((i) => i + 1), Ol);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, $l(n, o));
}, Wl = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, Ul = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (u) => {
    u.preventDefault(), el(n);
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
  const n = X().name;
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
  const t = X(), n = Xe(), a = ne(), o = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + u, D = await fetch(b);
        if (!D.ok) {
          console.error("[TLDownload] Failed to fetch data:", D.status);
          return;
        }
        const _ = await D.blob(), v = URL.createObjectURL(_), y = document.createElement("a");
        y.href = v, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
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
  const t = X(), n = Ge(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), [i, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), b = t.error, D = e.useMemo(
    () => {
      var C;
      return !!(window.isSecureContext && ((C = navigator.mediaDevices) != null && C.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((C) => C.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    _(), o("idle");
  }, [_]), y = e.useCallback(async () => {
    var C;
    if (a !== "uploading") {
      if (s(null), !D) {
        (C = p.current) == null || C.click();
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
  }, [a, D]), N = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const C = c.current, H = m.current;
    if (!C || !H)
      return;
    H.width = C.videoWidth, H.height = C.videoHeight;
    const W = H.getContext("2d");
    W && (W.drawImage(C, 0, 0), _(), o("uploading"), H.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", A, "capture.jpg"), await n(K), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, _]), x = e.useCallback(async (C) => {
    var A;
    const H = (A = C.target.files) == null ? void 0 : A[0];
    if (!H) return;
    o("uploading");
    const W = new FormData();
    W.append("photo", H, H.name), await n(W), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var H;
    if (a !== "overlayOpen") return;
    (H = h.current) == null || H.focus();
    const C = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = C;
    };
  }, [a]), Oe(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((C) => C.stop()), d.current = null);
  }, []);
  const g = ue(Xl), w = a === "uploading" ? g["js.uploading"] : g["js.photoCapture.open"], S = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && S.push("tlPhotoCapture__cameraBtn--uploading");
  const k = ["tlPhotoCapture__overlayVideo"];
  i && k.push("tlPhotoCapture__overlayVideo--mirrored");
  const j = ["tlPhotoCapture__mirrorBtn"];
  return i && j.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !D && /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: k.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: j.join(" "),
        onClick: () => r((C) => !C),
        title: g["js.photoCapture.mirror"],
        "aria-label": g["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: N,
        title: g["js.photoCapture.capture"],
        "aria-label": g["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: g["js.photoCapture.close"],
        "aria-label": g["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g[u]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Zl = {
  "js.photoViewer.alt": "Captured photo"
}, Ql = ({ controlId: l }) => {
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
  const t = X(), n = Xe(), a = !!t.hasPdf, o = t.dataRevision ?? 0, u = ue(Jl), i = n.indexOf("react-api/"), r = i >= 0 ? n.slice(0, i) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: u["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, u["js.pdfViewer.noDocument"]));
}, { useCallback: nn, useRef: Dt } = e, ta = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0), o = t.orientation, u = t.resizable === !0, s = t.children ?? [], i = o === "horizontal", r = s.length > 0 && s.every((v) => v.collapsed), c = !r && s.some((v) => v.collapsed), d = r ? !i : i, m = Dt(null), p = Dt(null), h = Dt(null), b = nn((v, y) => {
    const N = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? r && !d ? N.flex = "1 0 0%" : N.flex = "0 0 auto" : y !== void 0 ? N.flex = `0 0 ${y}px` : N.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (N.minWidth = i ? v.minSize : void 0, N.minHeight = i ? void 0 : v.minSize), N;
  }, [i, r, c, d]), D = nn((v, y) => {
    v.preventDefault();
    const N = m.current;
    if (!N) return;
    const x = s[y], g = s[y + 1], w = N.querySelectorAll(":scope > .tlSplitPanel__child"), S = [];
    w.forEach((C) => {
      S.push(i ? C.offsetWidth : C.offsetHeight);
    }), h.current = S, p.current = {
      splitterIndex: y,
      startPos: i ? v.clientX : v.clientY,
      startSizeBefore: S[y],
      startSizeAfter: S[y + 1],
      childBefore: x,
      childAfter: g
    };
    const k = (C) => {
      const H = p.current;
      if (!H || !h.current) return;
      const A = (i ? C.clientX : C.clientY) - H.startPos, K = H.childBefore.minSize || 0, B = H.childAfter.minSize || 0;
      let P = H.startSizeBefore + A, M = H.startSizeAfter - A;
      P < K && (M += P - K, P = K), M < B && (P += M - B, M = B), h.current[H.splitterIndex] = P, h.current[H.splitterIndex + 1] = M;
      const L = N.querySelectorAll(":scope > .tlSplitPanel__child"), R = L[H.splitterIndex], $ = L[H.splitterIndex + 1];
      R && (R.style.flex = `0 0 ${P}px`), $ && ($.style.flex = `0 0 ${M}px`);
    }, j = () => {
      if (document.removeEventListener("mousemove", k), document.removeEventListener("mouseup", j), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const C = {};
        s.forEach((H, W) => {
          const A = H.control;
          A != null && A.controlId && h.current && (C[A.controlId] = h.current[W]);
        }), n("updateSizes", { sizes: C });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", k), document.addEventListener("mouseup", j), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), _ = [];
  return s.forEach((v, y) => {
    if (_.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${y}`,
          className: `tlSplitPanel__child${v.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: b(v)
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control })
      )
    ), u && y < s.length - 1) {
      const N = s[y + 1];
      !v.collapsed && !N.collapsed && _.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${y}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (g) => D(g, y)
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
}, { useCallback: Tt } = e, na = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, la = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), aa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ra = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), oa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), sa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ca = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(na), o = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, b = u === "MINIMIZED", D = u === "MAXIMIZED", _ = u === "HIDDEN", v = Tt(() => {
    n("toggleMinimize");
  }, [n]), y = Tt(() => {
    n("toggleMaximize");
  }, [n]), N = Tt(() => {
    n("popOut");
  }, [n]), x = yt(d && !_);
  if (_)
    return null;
  const g = D ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, w = s && !D || i && !b || r, S = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || w;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${x ? " " + x : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: g
    },
    S && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !D && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(aa, null) : /* @__PURE__ */ e.createElement(la, null)
    ), i && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: D ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      D ? /* @__PURE__ */ e.createElement(oa, null) : /* @__PURE__ */ e.createElement(ra, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(sa, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !b && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, ia = ({ controlId: l }) => {
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
}, ua = ({ controlId: l }) => {
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
}, { useCallback: Ee, useState: bt, useEffect: Ot, useRef: Et } = e, da = {
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
  const s = Et(null);
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
  onOpenFlyout: b,
  onCloseFlyout: D
}) => {
  const _ = Et(null), [v, y] = bt(null), N = Ee(() => {
    a ? h === l.id ? D() : (_.current && y(_.current.getBoundingClientRect()), b(l.id)) : s(l.id);
  }, [a, h, l.id, s, b, D]), x = Ee((w) => {
    _.current = w, r(w);
  }, [r]), g = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (g ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: N,
      title: a ? l.label : void 0,
      "aria-expanded": a ? g : t,
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
  ), g && /* @__PURE__ */ e.createElement(
    ba,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: o,
      onExecute: u,
      onClose: D
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    wn,
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
      onOpenFlyout: b,
      onCloseFlyout: D
    }
  ))));
}, wn = ({
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
  const t = X(), n = ne(), a = ue(da), o = t.items ?? [], u = t.activeItemId, s = t.collapsed, i = t.drawerOpen, r = i ? !1 : s, [c, d] = bt(() => {
    const A = /* @__PURE__ */ new Map(), K = (B) => {
      for (const P of B)
        P.type === "group" && (A.set(P.id, P.expanded), K(P.children));
    };
    return K(o), A;
  }), m = Ee((A) => {
    d((K) => {
      const B = new Map(K), P = B.get(A) ?? !1;
      return B.set(A, !P), n("toggleGroup", { itemId: A, expanded: !P }), B;
    });
  }, [n]), p = Ee((A) => {
    A !== u && n("selectItem", { itemId: A });
  }, [n, u]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), b = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), D = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [_, v] = bt(null), y = Ee((A) => {
    v(A);
  }, []), N = Ee(() => {
    v(null);
  }, []);
  Ot(() => {
    r || v(null);
  }, [r]);
  const [x, g] = bt(() => {
    const A = $t(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), w = Et(/* @__PURE__ */ new Map()), S = Ee((A) => (K) => {
    K ? w.current.set(A, K) : w.current.delete(A);
  }, []), k = Ee((A) => {
    g(A);
  }, []), j = Et(0), C = Ee((A) => {
    g(A), j.current++;
  }, []);
  Ot(() => {
    const A = w.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, j.current]);
  const H = Ee((A) => {
    if (A.key === "Escape" && _ !== null) {
      A.preventDefault(), N();
      return;
    }
    const K = $t(o, r, c);
    if (K.length === 0) return;
    const B = K.findIndex((M) => M.id === x);
    if (B < 0) return;
    const P = K[B];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const M = (B + 1) % K.length;
        C(K[M].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const M = (B - 1 + K.length) % K.length;
        C(K[M].id);
        break;
      }
      case "Home": {
        A.preventDefault(), C(K[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), C(K[K.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), P.type === "nav" ? p(P.id) : P.type === "command" ? h(P.id) : P.type === "group" && (r ? _ === P.id ? N() : y(P.id) : m(P.id));
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
    _,
    C,
    p,
    h,
    m,
    y,
    N
  ]), W = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (i ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: W }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), i && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: D, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, o.map((A) => /* @__PURE__ */ e.createElement(
    wn,
    {
      key: A.id,
      item: A,
      activeItemId: u,
      collapsed: r,
      onSelect: p,
      onExecute: h,
      onToggleGroup: m,
      focusedId: x,
      setItemRef: S,
      onItemFocus: k,
      groupStates: c,
      flyoutGroupId: _,
      onOpenFlyout: y,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, /* @__PURE__ */ e.createElement(Pe, null, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent }))));
}, va = ({ controlId: l }) => {
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
  return /* @__PURE__ */ e.createElement(ot, { host: c }, /* @__PURE__ */ e.createElement("div", { id: l, className: d }, i.map((m, p) => /* @__PURE__ */ e.createElement(G, { key: p, control: m }))));
}, _a = ({ controlId: l }) => {
  const t = X(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Ca = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", u = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, u.map((i, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: i })));
}, ya = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = n != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, wa = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, u = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, u.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: ka } = e, Na = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = ka((u) => {
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
  const t = X(), n = ne(), a = t.items ?? [], o = t.activeItemId, u = Sa((s) => {
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
}, { useCallback: ln, useRef: Ta } = e, Ra = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), La = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, u = t.child, s = Ta(null), i = ln(() => {
    n("close");
  }, [n]), r = ln((c) => {
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
    /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: u }))
  )) : null;
}, { useEffect: xa, useRef: Ia } = e, Ma = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Ia(n.length);
  return xa(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: it, useRef: We, useState: ut } = e, ja = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Pa = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ba = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Aa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Pa), o = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, b] = ut(null), [D, _] = ut(null), [v, y] = ut(null), N = We(null), [x, g] = ut(!1), w = We(null), S = We(null), k = We(null), j = We(null), C = We(null), H = it(() => {
    n("close");
  }, [n]);
  Yt(!0, j, "field");
  const W = it((M, L) => {
    L.preventDefault();
    const R = j.current;
    if (!R) return;
    const $ = R.getBoundingClientRect(), f = !N.current, I = N.current ?? { x: $.left, y: $.top };
    f && (N.current = I, y(I)), C.current = {
      dir: M,
      startX: L.clientX,
      startY: L.clientY,
      startW: $.width,
      startH: $.height,
      startPos: { ...I },
      symmetric: f
    };
    const Y = (Z) => {
      const F = C.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let le = F.startW, pe = F.startH, ye = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (le = F.startW + 2 * te), F.dir.includes("w") && (le = F.startW - 2 * te), F.dir.includes("s") && (pe = F.startH + 2 * se), F.dir.includes("n") && (pe = F.startH - 2 * se)) : (F.dir.includes("e") && (le = F.startW + te), F.dir.includes("w") && (le = F.startW - te, ye = te), F.dir.includes("s") && (pe = F.startH + se), F.dir.includes("n") && (pe = F.startH - se, we = se));
      const Te = Math.max(200, le), Re = Math.max(100, pe);
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), S.current = Te, k.current = Re, b(Te), _(Re);
      const $e = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      N.current = $e, y($e);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
      const Z = S.current, F = k.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), C.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, [n]), A = it((M) => {
    if (M.button !== 0 || M.target.closest("button")) return;
    M.preventDefault();
    const L = j.current;
    if (!L) return;
    const R = L.getBoundingClientRect(), $ = N.current ?? { x: R.left, y: R.top }, f = M.clientX - $.x, I = M.clientY - $.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - f, le = Z.clientY - I;
      const pe = L.offsetWidth, ye = L.offsetHeight;
      se + pe > F && (se = F - pe), le + ye > te && (le = te - ye), se < 0 && (se = 0), le < 0 && (le = 0);
      const we = { x: se, y: le };
      N.current = we, y(we);
    }, U = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", U);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", U);
  }, []), K = it(() => {
    var M, L;
    if (x) {
      const R = w.current;
      R && (y(R.x !== -1 ? { x: R.x, y: R.y } : null), b(R.w), _(R.h)), g(!1);
    } else {
      const R = j.current, $ = R == null ? void 0 : R.getBoundingClientRect();
      w.current = {
        x: ((M = N.current) == null ? void 0 : M.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = N.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: D ?? null
      }, g(!0), y({ x: 0, y: 0 }), b(null), _(null);
    }
  }, [x, h, D]), B = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : u,
    ...D != null ? { height: D + "px" } : s != null ? { height: s } : {},
    ...i != null && D == null ? { minHeight: i } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
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
        onDoubleClick: r ? K : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: P }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: K,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: c }))),
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((M, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: M }))),
    r && !x && Ba.map((M) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: M,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${M}`,
        onMouseDown: (L) => W(M, L)
      }
    ))
  ));
}, { useCallback: Fa } = e, Oa = {
  "js.drawer.close": "Close"
}, $a = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Oa), o = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, r = t.child, c = Fa(() => {
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
}, { useCallback: dt, useRef: Ha } = e, Wa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ha(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", i = dt((m) => {
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
    o && /* @__PURE__ */ e.createElement(G, { control: o })
  );
}, { useCallback: Ua, useEffect: an, useRef: za, useState: rn } = e, Va = 250, Ka = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.message ?? "", o = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, r = t.generation ?? 0, [c, d] = rn(!1), [m, p] = rn(!1), h = za(!1);
  an(() => {
    h.current = !1;
  }, [r]);
  const b = Ua(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return an(() => {
    if (!i || s === 0 || m) return;
    const D = setTimeout(b, h.current ? Va : s);
    return () => clearTimeout(D);
  }, [i, s, m, b]), !i && !c ? null : /* @__PURE__ */ e.createElement(
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
}, { useCallback: Ya, useEffect: on, useMemo: Ga, useRef: Xa, useState: qa } = e, Za = 1e3;
function Qa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), u = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${u(a)}:${u(n)}` : `${a}:${u(n)}`;
}
const Ja = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", u = t.text ?? "", s = t.deadline ?? null, i = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = Ga(
    () => i != null ? i - Date.now() : 0,
    [i]
  ), [p, h] = qa(0), b = a && s != null;
  on(() => {
    if (!b) return;
    const x = setInterval(() => h((g) => g + 1), Za);
    return () => clearInterval(x);
  }, [b, s]);
  const D = Xa(null);
  on(() => {
    !b || d == null || s == null || D.current !== s && (Date.now() + m < s + d || (D.current = s, n("deadlinePassed", {})));
  }, [p, b, s, d, m, n]);
  const _ = Ya(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const v = s != null ? s - (Date.now() + m) : null;
  if (r != null && v != null && v > r) return null;
  const y = v != null ? Qa(v) : null, N = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${N ? " tlNoticeBar--clickable" : ""}`,
      role: N ? "button" : "status",
      "aria-live": "polite",
      tabIndex: N ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": N ? `${u} ${c}` : void 0,
      onClick: N ? _ : void 0,
      onKeyDown: N ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), _());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, u),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: Rt, useEffect: sn, useRef: er, useState: cn } = e, tr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.anchorId, u = t.anchorX, s = t.anchorY, i = t.items ?? [], r = er(null), [c, d] = cn({ top: 0, left: 0 }), [m, p] = cn(0), h = i.filter((v) => v.type === "item" && !v.disabled);
  sn(() => {
    var S, k;
    if (!a) return;
    const v = ((S = r.current) == null ? void 0 : S.offsetHeight) ?? 200, y = ((k = r.current) == null ? void 0 : k.offsetWidth) ?? 200;
    if (u != null && s != null) {
      let j = s, C = u;
      j + v > window.innerHeight && (j = Math.max(0, window.innerHeight - v)), C + y > window.innerWidth && (C = Math.max(0, window.innerWidth - y)), d({ top: j, left: C }), p(0);
      return;
    }
    if (!o) return;
    const N = document.getElementById(o);
    if (!N) return;
    const x = N.getBoundingClientRect();
    let g = x.bottom + 4, w = x.left;
    g + v > window.innerHeight && (g = x.top - v - 4), w + y > window.innerWidth && (w = x.right - y), d({ top: g, left: w }), p(0);
  }, [a, o, u, s]);
  const b = Rt(() => {
    n("close");
  }, [n]), D = Rt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  sn(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, b]);
  const _ = Rt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), b();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), p((y) => (y + 1) % h.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), p((y) => (y - 1 + h.length) % h.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const y = h[m];
      y && D(y.id);
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
    i.map((v, y) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const x = h.indexOf(v) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : "") + (v.cssClasses ? " " + v.cssClasses : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => D(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: v.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, nr = 768, lr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${nr}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (p) => d(p.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, u = t.notices, s = t.content, i = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: o })), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement(G, { control: r }));
}, ar = ({ controlId: l }) => {
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
}, rr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o, onActivate: u }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (o(), !0) : !1), me("Enter", () => u()), null), or = {
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
}, sr = 300, un = 50, cr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function mt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, cr));
}
const Ht = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', dn = Ht + ", button:not([disabled]), a[href]";
function kn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function Lt(l, t, n = {}) {
  const a = kn(l, t);
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
const ir = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(or), o = e.useRef(null);
  e.useEffect(() => {
    const E = o.current;
    if (!E) return;
    const T = (V) => {
      const Q = V.detail;
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
    return E.addEventListener("tl-tooltip-resolve", T), () => E.removeEventListener("tl-tooltip-resolve", T);
  }, []);
  const u = t.columns ?? [], s = t.totalRowCount ?? 0, i = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, b = t.columnSelect ?? !1, D = t.filterBar ?? !1, _ = t.namedFilters ?? [], v = t.activeNamedFilter ?? "", y = t.search ?? "", N = t.filterSaving ?? !1, x = e.useMemo(
    () => u.filter((E) => E.sortPriority && E.sortPriority > 0).length,
    [u]
  ), g = c === "multi", w = 40, S = 20, k = e.useRef(null), j = e.useRef(null), C = e.useRef(null), H = e.useRef(null), W = e.useRef(null), [A, K] = e.useState({}), B = e.useRef(null), P = e.useRef(!1), M = e.useRef(null), [L, R] = e.useState(null), [$, f] = e.useState(null), [I, Y] = e.useState(null), [U, Z] = e.useState(0);
  e.useEffect(() => {
    const E = C.current;
    if (!E)
      return;
    const T = () => {
      const Q = E.offsetWidth - E.clientWidth;
      Z((ee) => ee === Q ? ee : Q);
    };
    T();
    const V = new ResizeObserver(T);
    return V.observe(E), () => V.disconnect();
  }, []), e.useEffect(() => {
    B.current || K({});
  }, [u]);
  const F = e.useCallback((E) => A[E.name] ?? E.width, [A]), te = e.useMemo(() => {
    const E = [];
    let T = g && p > 0 ? w : 0;
    for (let V = 0; V < p && V < u.length; V++)
      E.push(T), T += F(u[V]);
    return E;
  }, [u, p, g, w, F]), se = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let E = g ? w : 0;
    for (let T = 0; T < p && T < u.length; T++)
      E += F(u[T]);
    return E;
  }, [u, p, g, w, F]), le = s * r, pe = e.useRef(null), ye = e.useCallback((E, T, V) => {
    V.preventDefault(), V.stopPropagation(), B.current = { column: E, startX: V.clientX, startWidth: T };
    let Q = V.clientX, ee = 0;
    const re = () => {
      const ce = B.current;
      if (!ce) return;
      const de = Math.max(un, ce.startWidth + (Q - ce.startX) + ee);
      K((_e) => ({ ..._e, [ce.column]: de }));
    }, oe = () => {
      const ce = C.current, de = k.current;
      if (!ce || !B.current) return;
      const _e = ce.getBoundingClientRect(), xe = 40, Zt = 8, Jn = ce.scrollLeft;
      Q > _e.right - xe ? ce.scrollLeft += Zt : Q < _e.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Zt));
      const Qt = ce.scrollLeft - Jn;
      Qt !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += Qt, re()), pe.current = requestAnimationFrame(oe);
    };
    pe.current = requestAnimationFrame(oe);
    const he = (ce) => {
      Q = ce.clientX, re();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", he), document.removeEventListener("mouseup", fe), pe.current !== null && (cancelAnimationFrame(pe.current), pe.current = null);
      const de = B.current;
      if (de) {
        const _e = Math.max(un, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: _e }), B.current = null, P.current = !0, requestAnimationFrame(() => {
          P.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", he), document.addEventListener("mouseup", fe);
  }, [n]), we = e.useCallback(() => {
    k.current && C.current && (k.current.scrollLeft = C.current.scrollLeft), H.current !== null && clearTimeout(H.current), H.current = window.setTimeout(() => {
      const E = C.current;
      if (!E) return;
      const T = E.scrollTop, V = Math.ceil(E.clientHeight / r), Q = Math.floor(T / r);
      n("scroll", { start: Q, count: V });
    }, 80);
  }, [n, r]), Te = e.useCallback((E, T, V) => {
    if (P.current) return;
    let Q;
    !T || T === "desc" ? Q = "asc" : Q = "desc";
    const ee = V.shiftKey ? "add" : "replace";
    n("sort", { column: E, direction: Q, mode: ee });
  }, [n]), Re = e.useCallback((E, T) => {
    M.current = E, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", E);
  }, []), $e = e.useCallback((E, T) => {
    if (!M.current || M.current === E) {
      R(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const V = T.currentTarget.getBoundingClientRect(), Q = T.clientX < V.left + V.width / 2 ? "left" : "right";
    R({ column: E, side: Q });
  }, []), He = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const T = M.current;
    if (!T || !L) {
      M.current = null, R(null);
      return;
    }
    let V = u.findIndex((ee) => ee.name === L.column);
    if (V < 0) {
      M.current = null, R(null);
      return;
    }
    const Q = u.findIndex((ee) => ee.name === T);
    L.side === "right" && V++, Q < V && V--, n("columnReorder", { column: T, targetIndex: V }), M.current = null, R(null);
  }, [u, L, n]), O = e.useCallback(() => {
    M.current = null, R(null);
  }, []), q = e.useCallback((E, T) => {
    var ee, re, oe, he;
    const V = window.getSelection();
    if (V && !V.isCollapsed && T.currentTarget.contains(V.anchorNode))
      return;
    if (!mt(T) && ((ee = C.current) == null || ee.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const fe = (he = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : he.getAttribute("data-col");
      W.current = { index: E, col: fe ?? void 0 };
    }
    const Q = i.find((fe) => fe.index === E);
    mt(T) && (Q != null && Q.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: E,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, i]), ae = e.useCallback((E, T) => {
    mt(T) || n("activate", { rowIndex: E });
  }, [n]), ie = e.useCallback((E, T, V) => {
    n("moveSelection", { direction: E, extend: T, move: V });
  }, [n]), qe = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: g, shiftKey: !1 });
  }, [n, m, g]), In = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), Mn = e.useCallback(() => {
    var T;
    if (m < 0)
      return !1;
    const E = document.activeElement;
    return (T = E == null ? void 0 : E.closest) != null && T.call(E, dn) ? !1 : (n("activate", { rowIndex: m }), !0);
  }, [n, m]), jn = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const E = C.current;
    if (!E)
      return;
    const T = m * r, V = T + r;
    T < E.scrollTop ? E.scrollTop = T : V > E.scrollTop + E.clientHeight && (E.scrollTop = V - E.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const E = W.current, T = C.current;
    if (!E || !T)
      return;
    const V = i.find((re) => re.index === E.index);
    if (!V || !Lt(T, V.id))
      return;
    W.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !T.contains(Q))
      return;
    const ee = Lt(T, V.id, { col: E.col, last: E.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [i]);
  const Pn = e.useCallback((E) => {
    if (E.key !== "Tab")
      return;
    const T = C.current, V = document.activeElement;
    if (!T || !V || !T.contains(V))
      return;
    const Q = V.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = i.find((xe) => xe.id === ee);
    if (!re)
      return;
    const oe = kn(T, ee).flatMap((xe) => Array.from(xe.querySelectorAll(dn))), he = oe.indexOf(V);
    if (he < 0)
      return;
    const fe = !E.shiftKey;
    if (!(fe ? he === oe.length - 1 : he === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const _e = i.find((xe) => xe.index === de);
    _e && Lt(T, _e.id) || (E.preventDefault(), W.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [i, s, n]), Bn = e.useCallback((E, T) => {
    T.stopPropagation(), n("select", { rowIndex: E, ctrlKey: !0, shiftKey: !1 });
  }, [n]), An = e.useCallback(() => {
    const E = d === s && s > 0;
    n("selectAll", { selected: !E });
  }, [n, d, s]), Fn = e.useCallback((E, T, V) => {
    V.stopPropagation(), n("expand", { rowIndex: E, expanded: T });
  }, [n]), On = e.useCallback((E, T) => {
    T.preventDefault(), f({ x: T.clientX, y: T.clientY, colIdx: E });
  }, []), $n = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), f(null));
  }, [$, n]), Hn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), f(null);
  }, [n]), Wn = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const T = j.current, V = k.current;
    if (!T || !V)
      return;
    const Q = T.clientWidth, ee = [{ x: 0, count: 0 }];
    V.querySelectorAll("[data-col-idx]").forEach((fe) => {
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
    const E = () => f(null);
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [$]), Oe(!!$, { ESCAPE: () => f(null) });
  const Un = e.useCallback((E, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: E });
  }, [n]), zn = e.useCallback((E) => {
    E.stopPropagation(), E.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Vn, Xt] = e.useState(y), wt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    wt.current || Xt(y);
  }, [y]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const st = e.useCallback((E) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), wt.current = !1, n("search", { term: E });
  }, [n]), Kn = e.useCallback((E) => {
    Xt(E), wt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => st(E), sr);
  }, [st]), Yn = e.useCallback((E) => {
    E.key === "Enter" && (E.preventDefault(), st(E.currentTarget.value));
  }, [st]), Gn = e.useCallback((E) => {
    E === v ? n("clearFilter", {}) : n("applyNamedFilter", { id: E });
  }, [v, n]), Xn = e.useCallback((E, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: E });
  }, [n]), [Ze, Qe] = e.useState(null), kt = e.useCallback(() => {
    const E = (Ze ?? "").trim();
    E && (n("saveNamedFilter", { filterName: E }), Qe(null));
  }, [Ze, n]), qn = e.useCallback((E) => {
    E.key === "Enter" ? (E.preventDefault(), kt()) : E.key === "Escape" && (E.preventDefault(), Qe(null));
  }, [kt]), Nt = u.reduce((E, T) => E + F(T), 0) + (g ? w : 0), St = b ? 32 : 0, Zn = d === s && s > 0, qt = d > 0 && d < s, Qn = e.useCallback((E) => {
    E && (E.indeterminate = qt);
  }, [qt]);
  return /* @__PURE__ */ e.createElement(Kt, { active: jn }, /* @__PURE__ */ e.createElement(
    rr,
    {
      isMulti: g,
      cursorIndex: m,
      onMove: ie,
      onToggle: qe,
      onSelectAll: In,
      onActivate: Mn
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (E) => {
        if (!M.current) return;
        E.preventDefault();
        const T = C.current, V = k.current;
        if (!T) return;
        const Q = T.getBoundingClientRect(), ee = 40, re = 8;
        E.clientX < Q.left + ee ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : E.clientX > Q.right - ee && (T.scrollLeft += re), V && (V.scrollLeft = T.scrollLeft);
      },
      onDrop: He
    },
    D && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, _.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, _.map((E) => {
      const T = E.id === v;
      return /* @__PURE__ */ e.createElement(
        "span",
        {
          key: E.id,
          className: "tlTableView__chip" + (T ? " tlTableView__chip--active" : "")
        },
        /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__chipLabel",
            "aria-pressed": T,
            title: T ? a["js.table.clearFilter"] : E.label,
            onClick: () => Gn(E.id)
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
            onClick: (V) => Xn(E.id, V)
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
        value: Vn,
        onChange: (E) => Kn(E.target.value),
        onKeyDown: Yn
      }
    )), N && (Ze === null ? /* @__PURE__ */ e.createElement(
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
        onChange: (E) => Qe(E.target.value),
        onKeyDown: qn
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__barButton",
        title: a["js.table.saveFilter"],
        "aria-label": a["js.table.saveFilter"],
        disabled: !Ze.trim(),
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
        onClick: () => Qe(null)
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-x-lg" })
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: j }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: k }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Nt, paddingRight: St + U }
      },
      g && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: w,
            minWidth: w,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (E) => {
            M.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", u.length > 0 && u[0].name !== M.current && R({ column: u[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Qn,
            className: "tlTableView__checkbox",
            checked: Zn,
            onChange: An
          }
        )
      ),
      u.map((E, T) => {
        const V = F(E);
        u.length - 1;
        let Q = "tlTableView__headerCell";
        E.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === E.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = T < p, re = T === p - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: E.name,
            className: Q,
            "data-col-idx": T,
            style: {
              width: V,
              minWidth: V,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: te[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: E.sortable ? (oe) => Te(E.name, E.sortDirection, oe) : void 0,
            onContextMenu: (oe) => On(T, oe),
            onDragStart: (oe) => Re(E.name, oe),
            onDragOver: (oe) => $e(E.name, oe),
            onDrop: He,
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
              onClick: (oe) => Un(E.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: E.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          E.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, E.sortDirection === "asc" ? "▲" : "▼", x > 1 && E.sortPriority != null && E.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, E.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => ye(E.name, V, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (E) => {
            if (M.current && u.length > 0) {
              const T = u[u.length - 1];
              T.name !== M.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", R({ column: T.name, side: "right" }));
            }
          },
          onDrop: He
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (I ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Wn
      }
    ), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: zn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: C,
        className: "tlTableView__body",
        onScroll: we,
        onKeyDown: Pn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: le, position: "relative", width: Nt, paddingRight: St } }, i.map((E) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: "tlTableView__row" + (E.selected ? " tlTableView__row--selected" : "") + (E.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: E.index * r,
            height: r,
            width: Nt,
            paddingRight: St,
            ...E.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !mt(T) && T.preventDefault();
          },
          onClick: (T) => q(E.index, T),
          onDoubleClick: (T) => ae(E.index, T)
        },
        g && /* @__PURE__ */ e.createElement(
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
              checked: E.selected,
              onChange: () => {
              },
              onClick: (T) => Bn(E.index, T),
              tabIndex: -1
            }
          )
        ),
        u.map((T, V) => {
          const Q = F(T), ee = V === u.length - 1, re = V < p, oe = V === p - 1;
          let he = "tlTableView__cell";
          re && (he += " tlTableView__cell--frozen"), oe && (he += " tlTableView__cell--frozenLast");
          const fe = h && V === 0, ce = E.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: he,
              "data-row": E.id,
              "data-col": T.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: te[V], zIndex: 2 } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * S } }, E.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Fn(E.index, !E.expanded, de)
              },
              E.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), E.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: E.cells[T.name] })) : E.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: E.cells[T.name] })
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
      $.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: $n }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Hn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ur = {
  "js.table.columnSearch": "Find column"
}, dr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ur), o = t.entries ?? [], u = o.filter((g) => g.visible).length, [s, i] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((g) => g.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), b = e.useCallback((g) => {
    m.current = g, h(g);
  }, []), D = e.useCallback((g, w) => {
    n("columnVisible", { column: g, visible: w });
  }, [n]), _ = e.useCallback((g, w) => {
    d.current = g, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", g);
  }, []), v = e.useCallback((g, w) => {
    if (!d.current || d.current === g) {
      b(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const S = w.currentTarget.getBoundingClientRect(), k = w.clientY < S.top + S.height / 2 ? "top" : "bottom";
    b({ name: g, side: k });
  }, [b]), y = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), N = e.useCallback((g) => {
    g.preventDefault();
    const w = d.current, S = m.current;
    if (d.current = null, b(null), !w || !S)
      return;
    const k = o.findIndex((H) => H.name === S.name), j = o.findIndex((H) => H.name === w);
    if (k < 0 || j < 0)
      return;
    let C = S.side === "top" ? k : k + 1;
    j < C && C--, C !== j && n("columnReorder", { column: w, targetIndex: C });
  }, [o, n, b]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: N }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (g) => i(g.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, c.map((g) => {
    const w = g.visible && u <= 1;
    let S = "tlColumnSelect__row";
    return p && p.name === g.name && (S += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: g.name,
        className: S,
        draggable: !0,
        onDragStart: (k) => _(g.name, k),
        onDragOver: (k) => v(g.name, k),
        onDrop: N,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: g.visible,
          disabled: w,
          onChange: (k) => D(g.name, k.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, g.label))
    );
  })));
}, { useState: Wt, useRef: lt, useCallback: gt, useMemo: Ae, useEffect: mn } = e, mr = {
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
}, ve = 44, vt = 15, Ce = 6e4, pr = 36e5, je = 864e5, fr = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function Ke(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function hr(l) {
  return Se(l);
}
function at(l, t) {
  return Se(l) === Se(t);
}
function Me(l) {
  return (l - Se(l)) / Ce;
}
function Je(l) {
  return Math.round(l / vt) * vt;
}
function et(l, t, n) {
  return Math.max(t, Math.min(n, l));
}
function _t(l) {
  if (!l)
    return "tlCalEvent--default";
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return "tlCalEvent--c" + Math.abs(t) % fr;
}
function Ct(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function br(l) {
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
function gr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Fe(l, n, t.start) + "–" + Fe(l, n, t.end);
}
const Er = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], vr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, Er.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function _r(l) {
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
      botMin: Me(s.start) + Math.max(15, (s.end - s.start) / Ce),
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
}, Nn = (l) => {
  const [t, n] = Wt(null), a = lt(null);
  a.current = t;
  const o = gt((i) => n(i), []), u = gt(() => n(null), []), s = gt(
    (i) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: i }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: u };
}, Cr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: u, dayStartHour: s, dayEndHour: i, now: r, send: c, editable: d, i18n: m } = l, p = Ae(() => {
    const B = n === "DAY" ? 1 : 7, P = [];
    for (let M = 0; M < B; M++) {
      const L = Ke(t, M);
      n === "WORK_WEEK" && u.includes(new Date(L).getDay()) || P.push(L);
    }
    return P;
  }, [t, n, u]), h = Nn(c), b = lt(null), D = lt(null), [_, v] = Wt(null), y = lt(null);
  y.current = _;
  const [N, x] = Wt(Date.now());
  mn(() => {
    const B = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(B);
  }, []);
  const g = gt(
    (B, P) => {
      const M = b.current;
      if (!M)
        return { dayIndex: 0, min: 0 };
      const L = M.getBoundingClientRect(), R = L.width / p.length, $ = et(Math.floor((B - L.left) / R), 0, p.length - 1), f = P - L.top + M.scrollTop, I = et(f / ve * 60, 0, 1440);
      return { dayIndex: $, min: I };
    },
    [p.length]
  );
  mn(() => {
    if (!_)
      return;
    const B = (L) => {
      const R = y.current;
      if (!R)
        return;
      const { dayIndex: $, min: f } = g(L.clientX, L.clientY);
      R.mode === "move" ? v({ ...R, dayStart: p[$], startMin: et(Je(f - R.grabMin), 0, 1440 - R.dur) }) : R.mode === "resize" ? v({ ...R, endMin: et(Je(f), R.startMin + vt, 1440) }) : v({ ...R, toMin: et(Je(f), 0, 1440) });
    }, P = () => {
      const L = y.current;
      if (v(null), !!L)
        if (L.mode === "move") {
          const R = L.dayStart + L.startMin * Ce;
          R !== L.origStartMs && c("moveEvent", { eventId: L.id, start: R, end: R + L.dur * Ce });
        } else if (L.mode === "resize") {
          const R = L.dayStart + L.endMin * Ce;
          R !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: R });
        } else {
          const R = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - R >= vt && h.open({ start: L.dayStart + R * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, M = () => v(null);
    return window.addEventListener("pointermove", B), window.addEventListener("pointerup", P, { once: !0 }), window.addEventListener("pointercancel", M), () => {
      window.removeEventListener("pointermove", B), window.removeEventListener("pointerup", P), window.removeEventListener("pointercancel", M);
    };
  }, [_, p, g, c, h.open]);
  const w = (B, P, M) => {
    if (!d || !P.movable)
      return;
    B.stopPropagation(), xt(B), h.discard();
    const { min: L } = g(B.clientX, B.clientY), R = (P.end - P.start) / Ce;
    v({
      mode: "move",
      id: P.id,
      grabMin: L - Me(P.start),
      dur: R,
      dayStart: M,
      startMin: Me(P.start),
      origStartMs: P.start
    });
  }, S = (B, P, M) => {
    !d || !P.resizable || (B.stopPropagation(), xt(B), h.discard(), v({
      mode: "resize",
      id: P.id,
      dayStart: M,
      startMin: Me(P.start),
      endMin: Me(P.end),
      origEndMs: P.end
    }));
  }, k = (B, P) => {
    if (!d || B.button !== 0)
      return;
    xt(B), h.discard();
    const { min: M } = g(B.clientX, B.clientY);
    v({ mode: "create", dayStart: P, fromMin: Je(M), toMin: Je(M) });
  }, j = Array.from({ length: 24 }, (B, P) => P), C = Ae(() => {
    if (_ === null || !("id" in _))
      return a;
    const B = _;
    return a.map((P) => {
      if (P.id !== B.id)
        return P;
      if (B.mode === "move") {
        const M = B.dayStart + B.startMin * Ce;
        return { ...P, start: M, end: M + B.dur * Ce };
      }
      return { ...P, end: B.dayStart + B.endMin * Ce };
    });
  }, [a, _]), H = Ae(() => p.map(
    (B) => _r(
      C.filter((P) => !P.allDay && P.start < B + je && P.end > B)
    )
  ), [p, C]), W = Ae(() => p.map((B) => C.filter((P) => P.allDay && P.start < B + je && P.end > B)), [p, C]), A = s * ve, K = i * ve;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((B) => {
    const P = u.includes(new Date(B).getDay()), M = at(B, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalDayHead" + (P ? " tlCalDayHead--nonworking" : "") + (M ? " tlCalDayHead--today" : ""),
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
    W[P].map((M) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: M.id,
        className: "tlCalAllDayEvent " + _t(M.category) + (M.selected ? " tlCalEvent--selected" : ""),
        style: Ct(M),
        title: M.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: M.id });
        }
      },
      M.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: D }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * ve } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, j.map((B) => /* @__PURE__ */ e.createElement("div", { key: B, className: "tlCalHourLabel", style: { top: B * ve } }, B === 0 ? "" : Fe(o, { hour: "numeric" }, Se(t) + B * pr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: b, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((B, P) => {
    const M = u.includes(new Date(B).getDay()), L = _ && ("dayStart" in _ && _.dayStart === B) ? _ : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalCol" + (M ? " tlCalCol--nonworking" : ""),
        onPointerDown: (R) => k(R, B)
      },
      j.map((R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlCalHourLine", style: { top: R * ve } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: K - A } }),
      at(B, N) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Me(Date.now()) / 60 * ve } }),
      H[P].map((R) => {
        const $ = _ !== null && "id" in _ && _.id === R.ev.id, f = R.topMin / 60 * ve, I = (R.botMin - R.topMin) / 60 * ve, Y = 100 / R.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: R.ev.id,
            className: "tlCalEvent " + _t(R.ev.category) + (R.ev.selected ? " tlCalEvent--selected" : "") + ($ ? " tlCalEvent--dragging" : ""),
            style: Ct(R.ev, {
              top: f,
              height: I,
              left: `${R.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: R.ev.tooltip,
            onPointerDown: (U) => w(U, R.ev, B),
            onClick: (U) => {
              U.stopPropagation(), c("selectEvent", { eventId: R.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, gr(o, R.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, R.ev.title),
          d && R.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (U) => S(U, R.ev, B) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === B && /* @__PURE__ */ e.createElement(
        Ut,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
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
}, yr = 3, wr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: u, send: s, editable: i, now: r, i18n: c } = l, d = Nn(s), m = Ae(() => {
    const h = [];
    for (let b = 0; b < 6; b++) {
      const D = [];
      for (let _ = 0; _ < 7; _++)
        D.push(Ke(t, b * 7 + _));
      h.push(D);
    }
    return h;
  }, [t]), p = (h, b) => {
    h.preventDefault();
    const D = h.dataTransfer.getData("text/plain"), _ = a.find((y) => y.id === D);
    if (!_ || !i || !_.movable)
      return;
    const v = b - Se(_.start);
    s("moveEvent", { eventId: D, start: _.start + v, end: _.end + v });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Fe(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, b) => {
    const D = h[0], _ = Ke(D, 7), v = a.filter((N) => (N.allDay || N.end - N.start >= je) && N.start < _ && N.end > D).sort((N, x) => N.start - x.start).slice(0, 3), y = v.length;
    return /* @__PURE__ */ e.createElement("div", { key: b, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((N) => {
      const x = new Date(N).getMonth() === new Date(n).getMonth(), g = u.includes(new Date(N).getDay()), w = at(N, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (g ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (S) => S.preventDefault(),
          onDrop: (S) => p(S, N),
          onClick: () => i && d.open({ start: N, end: N + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (w ? " tlCalMonthDayNum--today" : ""),
            onClick: (S) => {
              S.stopPropagation(), s("goto", { date: N, granularity: "DAY" });
            }
          },
          new Date(N).getDate()
        ),
        d.pending && d.pending.start === N && /* @__PURE__ */ e.createElement(
          Ut,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, v.map((N, x) => {
      const g = Math.max(0, Math.floor((Se(Math.max(N.start, D)) - D) / je)), w = Math.min(7, Math.ceil((N.end - D) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlCalMonthBar " + _t(N.category) + (N.selected ? " tlCalEvent--selected" : ""),
          style: Ct(N, {
            gridColumn: `${g + 1} / ${Math.max(g + 1, w) + 1}`,
            gridRow: x + 1
          }),
          draggable: i && N.movable,
          onDragStart: (S) => S.dataTransfer.setData("text/plain", N.id),
          title: N.tooltip,
          onClick: (S) => {
            S.stopPropagation(), s("selectEvent", { eventId: N.id });
          }
        },
        N.title
      );
    }), h.map((N, x) => {
      const g = a.filter((k) => !k.allDay && k.end - k.start < je && at(k.start, N)).sort((k, j) => k.start - j.start), w = g.slice(0, yr), S = g.length - w.length;
      return w.map((k, j) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalChip " + _t(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: Ct(k, { gridColumn: x + 1, gridRow: y + 1 + j }),
          draggable: i && k.movable,
          onDragStart: (C) => C.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (C) => {
            C.stopPropagation(), s("selectEvent", { eventId: k.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Fe(o, { hour: "numeric", minute: "2-digit" }, k.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, k.title)
      )).concat(
        S > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + N,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: y + 1 + w.length },
              onClick: () => s("goto", { date: N, granularity: "DAY" })
            },
            "+",
            S,
            " ",
            l.i18n["js.calendar.more"]
          )
        ] : []
      );
    })));
  })));
}, kr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: u, send: s, now: i } = l, r = Ae(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let b = Se(h.start);
      const D = h.end;
      for (; b < D; )
        p.add(b), b = Ke(b, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Ae(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, b) => {
      const D = new Date(p);
      return D.setDate(p.getDate() + (o + b) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(D);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), b = Se(Ke(p, -((h.getDay() - o + 7) % 7))), D = Array.from({ length: 42 }, (_, v) => Ke(b, v));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Fe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((_, v) => /* @__PURE__ */ e.createElement("div", { key: "h" + v, className: "tlCalMiniWd" }, _)), D.map((_) => {
      const v = new Date(_).getMonth() === h.getMonth(), y = u.includes(new Date(_).getDay()), N = at(_, i), x = r.has(hr(_));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _,
          className: "tlCalMiniDay" + (v ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (N ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: _, granularity: "DAY" })
        },
        new Date(_).getDate()
      );
    })));
  }));
}, Nr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(mr), o = t.granularity ?? "WEEK", u = t.rangeStart ?? Date.now(), s = t.anchor ?? u, i = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: br(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(vr, { title: i, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(wr, { ctx: r, rangeStart: u, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(kr, { ctx: r, rangeStart: u }) : /* @__PURE__ */ e.createElement(Cr, { ctx: r, rangeStart: u, granularity: o })));
}, Sr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Sn = e.createContext(Sr), { useMemo: Dr, useRef: Tr, useState: Rr, useEffect: Lr } = e, xr = 320, Ir = "TLTableView", Mr = "TLPanel", jr = ({ controlId: l }) => {
  var _;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Tr(null), [r, c] = Rr(
    a === "top" ? "top" : "side"
  );
  Lr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const v = i.current;
    if (!v) return;
    const y = new ResizeObserver((N) => {
      for (const x of N) {
        const w = x.contentRect.width / n;
        c(w < xr ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const d = Dr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = u.length === 1 ? u[0] : void 0, b = !!h && (h.module === Ir || h.module === Mr && ((_ = h.state) == null ? void 0 : _.bare) === !0), D = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Sn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: D, style: p, ref: i }, u.map((v, y) => /* @__PURE__ */ e.createElement(G, { key: y, control: v }))));
}, { useCallback: Pr } = e, Br = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ar = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Br), o = t.headerControl ?? null, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || u.length > 0 || s, p = Pr(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, D) => /* @__PURE__ */ e.createElement(G, { key: D, control: b }))));
}, { useContext: Fr, useState: Or, useCallback: $r } = e, Hr = ({ controlId: l }) => {
  const t = X(), n = Fr(Sn), a = t.label ?? "", o = t.required === !0, u = t.error, s = t.errorIcon, i = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, b = t.hasTooltip === !0, D = t.field, _ = n.readOnly, [v, y] = Or(!1), N = $r(() => y((k) => !k), []), x = m === "hidden", g = u != null, w = i != null && i.length > 0, S = [
    "tlFormField",
    `tlFormField--${m}`,
    _ ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    g ? "tlFormField--error" : "",
    !g && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: D })), !_ && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ft, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, u)), !_ && !g && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((k, j) => /* @__PURE__ */ e.createElement("div", { key: j, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ft, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, k)))), !_ && c && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, Wr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.iconCss, o = t.iconSrc, u = t.label, s = t.cssClass, i = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, u && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, u)), m = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
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
}, Ur = 20, zr = () => {
  var S;
  const l = X(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((S = n.find((k) => k.selected)) == null ? void 0 : S.id) ?? null;
  e.useEffect(() => {
    var j;
    if (m == null)
      return;
    const k = (j = d.current) == null ? void 0 : j.querySelector(".tlTreeView__node--selected");
    k && k.scrollIntoView({ block: "nearest" });
  }, [m]);
  const p = e.useCallback((k, j) => {
    t(j ? "collapse" : "expand", { nodeId: k });
  }, [t]), h = e.useCallback((k, j) => {
    var H;
    const C = window.getSelection();
    C && !C.isCollapsed && j.currentTarget.contains(C.anchorNode) || ((H = d.current) == null || H.focus({ preventScroll: !0 }), t("select", {
      nodeId: k,
      ctrlKey: j.ctrlKey || j.metaKey,
      shiftKey: j.shiftKey
    }));
  }, [t]), b = e.useCallback((k) => {
    t("activate", { nodeId: k });
  }, [t]), D = e.useCallback((k, j) => {
    j.preventDefault(), t("contextMenu", { nodeId: k, x: j.clientX, y: j.clientY });
  }, [t]), _ = e.useRef(null), v = e.useCallback((k, j) => {
    const C = j.getBoundingClientRect(), H = k.clientY - C.top, W = C.height / 3;
    return H < W ? "above" : H > W * 2 ? "below" : "within";
  }, []), y = e.useCallback((k, j) => {
    j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", k);
  }, []), N = e.useCallback((k, j) => {
    j.preventDefault(), j.dataTransfer.dropEffect = "move";
    const C = v(j, j.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t("dragOver", { nodeId: k, position: C }), _.current = null;
    }, 50);
  }, [t, v]), x = e.useCallback((k, j) => {
    j.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const C = v(j, j.currentTarget);
    t("drop", { nodeId: k, position: C });
  }, [t, v]), g = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t("dragEnd");
  }, [t]), w = e.useCallback((k) => {
    if (n.length === 0) return;
    let j = r;
    switch (k.key) {
      case "ArrowDown":
        k.preventDefault(), j = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        k.preventDefault(), j = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (k.preventDefault(), r >= 0 && r < n.length) {
          const C = n[r];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (j = r + 1);
        }
        break;
      case "ArrowLeft":
        if (k.preventDefault(), r >= 0 && r < n.length) {
          const C = n[r];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const H = C.depth;
            for (let W = r - 1; W >= 0; W--)
              if (n[W].depth < H) {
                j = W;
                break;
              }
          }
        }
        break;
      case "Enter":
        k.preventDefault(), r >= 0 && r < n.length && (k.ctrlKey || k.metaKey || k.shiftKey ? t("select", {
          nodeId: n[r].id,
          ctrlKey: k.ctrlKey || k.metaKey,
          shiftKey: k.shiftKey
        }) : b(n[r].id));
        return;
      case " ":
        k.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        k.preventDefault(), j = 0;
        break;
      case "End":
        k.preventDefault(), j = n.length - 1;
        break;
      default:
        return;
    }
    j !== r && c(j);
  }, [r, n, t, a, b]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: w
    },
    n.map((k, j) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: k.id,
        role: "treeitem",
        "aria-expanded": k.expandable ? k.expanded : void 0,
        "aria-selected": k.selected,
        "aria-level": k.depth + 1,
        className: [
          "tlTreeView__node",
          k.selected ? "tlTreeView__node--selected" : "",
          j === r ? "tlTreeView__node--focused" : "",
          s === k.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === k.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === k.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: k.depth * Ur },
        draggable: o,
        onMouseDown: (C) => {
          (C.shiftKey || C.ctrlKey || C.metaKey || C.detail > 1) && C.preventDefault();
        },
        onClick: (C) => h(k.id, C),
        onDoubleClick: () => b(k.id),
        onContextMenu: (C) => D(k.id, C),
        onDragStart: (C) => y(k.id, C),
        onDragOver: u ? (C) => N(k.id, C) : void 0,
        onDrop: u ? (C) => x(k.id, C) : void 0,
        onDragEnd: g
      },
      k.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(k.id, k.expanded);
          },
          tabIndex: -1,
          "aria-label": k.expanded ? "Collapse" : "Expand"
        },
        k.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: k.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: k.content }))
    ))
  );
};
var It = { exports: {} }, be = {}, Mt = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var pn;
function Vr() {
  if (pn) return J;
  pn = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), o = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), i = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), c = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), p = Symbol.iterator;
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
  }, D = Object.assign, _ = {};
  function v(f, I, Y) {
    this.props = f, this.context = I, this.refs = _, this.updater = Y || b;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(f, I) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, I, "setState");
  }, v.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function y() {
  }
  y.prototype = v.prototype;
  function N(f, I, Y) {
    this.props = f, this.context = I, this.refs = _, this.updater = Y || b;
  }
  var x = N.prototype = new y();
  x.constructor = N, D(x, v.prototype), x.isPureReactComponent = !0;
  var g = Array.isArray;
  function w() {
  }
  var S = { H: null, A: null, T: null, S: null }, k = Object.prototype.hasOwnProperty;
  function j(f, I, Y) {
    var U = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: I,
      ref: U !== void 0 ? U : null,
      props: Y
    };
  }
  function C(f, I) {
    return j(f.type, I, f.props);
  }
  function H(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function W(f) {
    var I = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return I[Y];
    });
  }
  var A = /\/+/g;
  function K(f, I) {
    return typeof f == "object" && f !== null && f.key != null ? W("" + f.key) : I.toString(36);
  }
  function B(f) {
    switch (f.status) {
      case "fulfilled":
        return f.value;
      case "rejected":
        throw f.reason;
      default:
        switch (typeof f.status == "string" ? f.then(w, w) : (f.status = "pending", f.then(
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
  function P(f, I, Y, U, Z) {
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
                I,
                Y,
                U,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = U === "" ? "." + K(f, 0) : U, g(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), P(Z, I, Y, "", function(pe) {
        return pe;
      })) : Z != null && (H(Z) && (Z = C(
        Z,
        Y + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), I.push(Z)), 1;
    te = 0;
    var se = U === "" ? "." : U + ":";
    if (g(f))
      for (var le = 0; le < f.length; le++)
        U = f[le], F = se + K(U, le), te += P(
          U,
          I,
          Y,
          F,
          Z
        );
    else if (le = h(f), typeof le == "function")
      for (f = le.call(f), le = 0; !(U = f.next()).done; )
        U = U.value, F = se + K(U, le++), te += P(
          U,
          I,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof f.then == "function")
        return P(
          B(f),
          I,
          Y,
          U,
          Z
        );
      throw I = String(f), Error(
        "Objects are not valid as a React child (found: " + (I === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : I) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function M(f, I, Y) {
    if (f == null) return f;
    var U = [], Z = 0;
    return P(f, U, "", "", function(F) {
      return I.call(Y, F, Z++);
    }), U;
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
  var R = typeof reportError == "function" ? reportError : function(f) {
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
    map: M,
    forEach: function(f, I, Y) {
      M(
        f,
        function() {
          I.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var I = 0;
      return M(f, function() {
        I++;
      }), I;
    },
    toArray: function(f) {
      return M(f, function(I) {
        return I;
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
  return J.Activity = m, J.Children = $, J.Component = v, J.Fragment = n, J.Profiler = o, J.PureComponent = N, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = S, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(f) {
      return S.H.useMemoCache(f);
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
    var U = D({}, f.props), Z = f.key;
    if (I != null)
      for (F in I.key !== void 0 && (Z = "" + I.key), I)
        !k.call(I, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && I.ref === void 0 || (U[F] = I[F]);
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
  }, J.createElement = function(f, I, Y) {
    var U, Z = {}, F = null;
    if (I != null)
      for (U in I.key !== void 0 && (F = "" + I.key), I)
        k.call(I, U) && U !== "key" && U !== "__self" && U !== "__source" && (Z[U] = I[U]);
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
  }, J.memo = function(f, I) {
    return {
      $$typeof: c,
      type: f,
      compare: I === void 0 ? null : I
    };
  }, J.startTransition = function(f) {
    var I = S.T, Y = {};
    S.T = Y;
    try {
      var U = f(), Z = S.S;
      Z !== null && Z(Y, U), typeof U == "object" && U !== null && typeof U.then == "function" && U.then(w, R);
    } catch (F) {
      R(F);
    } finally {
      I !== null && Y.types !== null && (I.types = Y.types), S.T = I;
    }
  }, J.unstable_useCacheRefresh = function() {
    return S.H.useCacheRefresh();
  }, J.use = function(f) {
    return S.H.use(f);
  }, J.useActionState = function(f, I, Y) {
    return S.H.useActionState(f, I, Y);
  }, J.useCallback = function(f, I) {
    return S.H.useCallback(f, I);
  }, J.useContext = function(f) {
    return S.H.useContext(f);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(f, I) {
    return S.H.useDeferredValue(f, I);
  }, J.useEffect = function(f, I) {
    return S.H.useEffect(f, I);
  }, J.useEffectEvent = function(f) {
    return S.H.useEffectEvent(f);
  }, J.useId = function() {
    return S.H.useId();
  }, J.useImperativeHandle = function(f, I, Y) {
    return S.H.useImperativeHandle(f, I, Y);
  }, J.useInsertionEffect = function(f, I) {
    return S.H.useInsertionEffect(f, I);
  }, J.useLayoutEffect = function(f, I) {
    return S.H.useLayoutEffect(f, I);
  }, J.useMemo = function(f, I) {
    return S.H.useMemo(f, I);
  }, J.useOptimistic = function(f, I) {
    return S.H.useOptimistic(f, I);
  }, J.useReducer = function(f, I, Y) {
    return S.H.useReducer(f, I, Y);
  }, J.useRef = function(f) {
    return S.H.useRef(f);
  }, J.useState = function(f) {
    return S.H.useState(f);
  }, J.useSyncExternalStore = function(f, I, Y) {
    return S.H.useSyncExternalStore(
      f,
      I,
      Y
    );
  }, J.useTransition = function() {
    return S.H.useTransition();
  }, J.version = "19.2.4", J;
}
var fn;
function Kr() {
  return fn || (fn = 1, Mt.exports = Vr()), Mt.exports;
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
var hn;
function Yr() {
  if (hn) return be;
  hn = 1;
  var l = Kr();
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
var bn;
function Gr() {
  if (bn) return It.exports;
  bn = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), It.exports = Yr(), It.exports;
}
var Dn = Gr();
const { useState: Ie, useCallback: ge, useRef: tt, useEffect: Ue, useMemo: zt } = e;
function Gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Xr({
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
function qr({
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
    /* @__PURE__ */ e.createElement(Gt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const Zr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = u && o && !i && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = h["js.dropdownSelect.nothingFound"], D = ge(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [_, v] = Ie(!1), [y, N] = Ie(""), [x, g] = Ie(-1), [w, S] = Ie(!1), [k, j] = Ie({}), [C, H] = Ie(null), [W, A] = Ie(null), [K, B] = Ie(null), P = tt(null), M = tt(null), L = tt(null), R = tt(a);
  R.current = a;
  const $ = tt(-1), f = zt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), I = zt(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (y) {
      const q = y.toLowerCase();
      O = O.filter((ae) => ae.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, y]);
  Ue(() => {
    y && I.length === 1 ? g(0) : g(-1);
  }, [I.length, y]), Ue(() => {
    _ && c && M.current && M.current.focus();
  }, [_, c, a]), Ue(() => {
    var ae, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const q = (ae = P.current) == null ? void 0 : ae.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = P.current) == null || ie.focus();
  }, [a]), Ue(() => {
    if (!_) return;
    const O = (q) => {
      P.current && !P.current.contains(q.target) && L.current && !L.current.contains(q.target) && (v(!1), N(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [_]), Ue(() => {
    if (!_ || !P.current) return;
    const O = P.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    j({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [_]);
  const Y = ge(async () => {
    if (!(i || !r) && (v(!0), N(""), g(-1), S(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        S(!0);
      }
  }, [i, r, c, n]), U = ge(() => {
    var O;
    v(!1), N(""), g(-1), (O = P.current) == null || O.focus();
  }, []), Z = ge(
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
      R.current = q, n(ct, { value: q.map((ae) => ae.value) }), o ? (N(""), g(-1)) : U();
    },
    [o, d, n, U]
  ), F = ge(
    (O) => {
      $.current = R.current.findIndex((ae) => ae.value === O);
      const q = R.current.filter((ae) => ae.value !== O);
      R.current = q, n(ct, { value: q.map((ae) => ae.value) });
    },
    [n]
  ), te = ge(
    (O) => {
      O.stopPropagation(), n(ct, { value: [] }), U();
    },
    [n, U]
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
          O.preventDefault(), O.stopPropagation(), g(
            (q) => q < I.length - 1 ? q + 1 : 0
          );
          break;
        case "ArrowUp":
          O.preventDefault(), O.stopPropagation(), g(
            (q) => q > 0 ? q - 1 : I.length - 1
          );
          break;
        case "Enter":
          O.preventDefault(), O.stopPropagation(), x >= 0 && x < I.length && Z(I[x].value);
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
      I,
      x,
      Z,
      y,
      o,
      a,
      F
    ]
  ), pe = ge(
    async (O) => {
      O.preventDefault(), S(!1);
      try {
        await n("loadOptions");
      } catch {
        S(!0);
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
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", C === null || C === O) {
        A(null), B(null);
        return;
      }
      const ae = q.currentTarget.getBoundingClientRect(), ie = ae.left + ae.width / 2, qe = q.clientX < ie ? "before" : "after";
      A(O), B(qe);
    },
    [C]
  ), Te = ge(
    (O) => {
      if (O.preventDefault(), C === null || W === null || K === null || C === W) return;
      const q = [...R.current], [ae] = q.splice(C, 1);
      let ie = W;
      C < W ? ie = K === "before" ? ie - 1 : ie : ie = K === "before" ? ie : ie + 1, q.splice(ie, 0, ae), R.current = q, n(ct, { value: q.map((qe) => qe.value) }), H(null), A(null), B(null);
    },
    [C, W, K, n]
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
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement("span", { key: O.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Gt, { image: O.image }), /* @__PURE__ */ e.createElement("span", null, O.label))));
  const $e = !s && a.length > 0 && !i, He = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: k,
      ...tl
    },
    (c || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: M,
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
      c && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      c && I.map((O, q) => /* @__PURE__ */ e.createElement(
        qr,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === x,
          searchTerm: y,
          onSelect: Z,
          onMouseEnter: () => g(q)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: P,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: _ ? void 0 : Y,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let ae = "";
      return C === q ? ae = "tlDropdownSelect__chip--dragging" : W === q && K === "before" ? ae = "tlDropdownSelect__chip--dropBefore" : W === q && K === "after" && (ae = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Xr,
        {
          key: O.value,
          option: O,
          removable: !i && (o || !s),
          onRemove: F,
          removeLabel: D(O.label),
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), He && Dn.createPortal(He, document.body));
}, { useCallback: jt, useRef: Qr } = e, Tn = "application/x-tl-color", Jr = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: u
}) => {
  const s = Qr(null), i = jt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = jt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = jt(
    (d) => (m) => {
      m.preventDefault();
      const p = m.dataTransfer.getData(Tn);
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
function Rn(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Vt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Ln(l) {
  if (!Vt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function xn(l, t, n) {
  const a = (o) => Rn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function eo(l, t, n) {
  const a = l / 255, o = t / 255, u = n / 255, s = Math.max(a, o, u), i = Math.min(a, o, u), r = s - i;
  let c = 0;
  r !== 0 && (s === a ? c = (o - u) / r % 6 : s === o ? c = (u - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function to(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), u = n - a;
  let s = 0, i = 0, r = 0;
  return l < 60 ? (s = a, i = o, r = 0) : l < 120 ? (s = o, i = a, r = 0) : l < 180 ? (s = 0, i = a, r = o) : l < 240 ? (s = 0, i = o, r = a) : l < 300 ? (s = o, i = 0, r = a) : (s = a, i = 0, r = o), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((r + u) * 255)
  ];
}
function no(l) {
  return eo(...Ln(l));
}
function Pt(l, t, n) {
  return xn(...to(l, t, n));
}
const { useCallback: ze, useRef: gn } = e, lo = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = no(l), u = gn(null), s = gn(null), i = ze(
    (b, D) => {
      var N;
      const _ = (N = u.current) == null ? void 0 : N.getBoundingClientRect();
      if (!_) return;
      const v = Math.max(0, Math.min(1, (b - _.left) / _.width)), y = Math.max(0, Math.min(1, 1 - (D - _.top) / _.height));
      t(Pt(n, v, y));
    },
    [n, t]
  ), r = ze(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), i(b.clientX, b.clientY);
    },
    [i]
  ), c = ze(
    (b) => {
      b.buttons !== 0 && i(b.clientX, b.clientY);
    },
    [i]
  ), d = ze(
    (b) => {
      var y;
      const D = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!D) return;
      const v = Math.max(0, Math.min(1, (b - D.top) / D.height)) * 360;
      t(Pt(v, a, o));
    },
    [a, o, t]
  ), m = ze(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), p = ze(
    (b) => {
      b.buttons !== 0 && d(b.clientY);
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
function ao(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const ro = {
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
}, { useState: pt, useCallback: ke, useEffect: En, useRef: oo, useLayoutEffect: so } = e, co = ({
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
  const [c, d] = pt("palette"), [m, p] = pt(t), h = oo(null), b = ue(ro), [D, _] = pt(null);
  so(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), R = h.current.getBoundingClientRect();
    let $ = L.bottom + 4, f = L.left;
    $ + R.height > window.innerHeight && ($ = L.top - R.height - 4), f + R.width > window.innerWidth && (f = Math.max(0, L.right - R.width)), _({ top: $, left: f });
  }, [l]);
  const v = m != null, [y, N, x] = v ? Ln(m) : [0, 0, 0], [g, w] = pt((m == null ? void 0 : m.toUpperCase()) ?? "");
  En(() => {
    w((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Oe(!0, { ESCAPE: i }), En(() => {
    const L = ($) => {
      h.current && !h.current.contains($.target) && i();
    }, R = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(R), document.removeEventListener("mousedown", L);
    };
  }, [i]);
  const S = ke(
    (L) => (R) => {
      const $ = parseInt(R.target.value, 10);
      if (isNaN($)) return;
      const f = Rn($);
      p(xn(L === "r" ? f : y, L === "g" ? f : N, L === "b" ? f : x));
    },
    [y, N, x]
  ), k = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(Tn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const R = document.createElement("div");
        R.style.width = "33px", R.style.height = "33px", R.style.backgroundColor = m, R.style.borderRadius = "3px", R.style.border = "1px solid rgba(0,0,0,0.1)", R.style.position = "absolute", R.style.top = "-9999px", document.body.appendChild(R), L.dataTransfer.setDragImage(R, 16, 16), requestAnimationFrame(() => document.body.removeChild(R));
      }
    },
    [m]
  ), j = ke((L) => {
    const R = L.target.value;
    w(R), Vt(R) && p(R);
  }, []), C = ke(() => {
    p(null);
  }, []), H = ke((L) => {
    p(L);
  }, []), W = ke(
    (L) => {
      s(L);
    },
    [s]
  ), A = ke(
    (L, R) => {
      const $ = [...n], f = $[L];
      $[L] = $[R], $[R] = f, r($);
    },
    [n, r]
  ), K = ke(
    (L, R) => {
      const $ = [...n];
      $[L] = R, r($);
    },
    [n, r]
  ), B = ke(() => {
    r([...o]);
  }, [o, r]), P = ke(
    (L) => {
      if (ao(n, L)) return;
      const R = n.indexOf(null);
      if (R < 0) return;
      const $ = [...n];
      $[R] = L.toUpperCase(), r($);
    },
    [n, r]
  ), M = ke(() => {
    m != null && P(m), s(m);
  }, [m, s, P]);
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
      Jr,
      {
        colors: n,
        columns: a,
        onSelect: H,
        onConfirm: W,
        onSwap: A,
        onReplace: K
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: B }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(lo, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: m } : void 0,
        draggable: v,
        onDragStart: v ? k : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: S("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? N : "",
        onChange: S("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? x : "",
        onChange: S("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (g !== "" && !Vt(g) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: g,
        onChange: j
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: C }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: M }, b["js.colorInput.ok"]))
  );
}, io = { "js.colorInput.chooseColor": "Choose color" }, { useState: uo, useCallback: ft, useRef: mo } = e, po = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(io), [s, i] = uo(!1), r = mo(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, b = ft(() => {
    d && i(!0);
  }, [d]), D = ft(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), _ = ft(() => {
    i(!1);
  }, []), v = ft(
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
      onClick: b,
      disabled: t.disabled === !0,
      title: c ?? "",
      "aria-label": u["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    co,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: D,
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
}, { useState: nt, useCallback: Be, useEffect: Bt, useRef: vn, useLayoutEffect: fo, useMemo: ho } = e, bo = {
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
}, go = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = ue(bo), [r, c] = nt("simple"), [d, m] = nt(""), [p, h] = nt(t ?? ""), [b, D] = nt(!1), [_, v] = nt(null), y = vn(null), N = vn(null);
  fo(() => {
    if (!l.current || !y.current) return;
    const W = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let K = W.bottom + 4, B = W.left;
    K + A.height > window.innerHeight && (K = W.top - A.height - 4), B + A.width > window.innerWidth && (B = Math.max(0, W.right - A.width)), v({ top: K, left: B });
  }, [l]), Bt(() => {
    !a && !b && s().catch(() => D(!0));
  }, [a, b, s]), Bt(() => {
    a && N.current && N.current.focus();
  }, [a]), Oe(!0, { ESCAPE: u }), Bt(() => {
    const W = (K) => {
      y.current && !y.current.contains(K.target) && u();
    }, A = setTimeout(() => document.addEventListener("mousedown", W), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", W);
    };
  }, [u]);
  const x = ho(() => {
    if (!d) return n;
    const W = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(W) || A.label.toLowerCase().includes(W) || A.terms != null && A.terms.some((K) => K.includes(W))
    );
  }, [n, d]), g = Be((W) => {
    m(W.target.value);
  }, []), w = Be(
    (W) => {
      o(W);
    },
    [o]
  ), S = Be((W) => {
    h(W);
  }, []), k = Be((W) => {
    h(W.target.value);
  }, []), j = Be(() => {
    o(p || null);
  }, [p, o]), C = Be(() => {
    o(null);
  }, [o]), H = Be(async (W) => {
    W.preventDefault(), D(!1);
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
        ref: N,
        type: "text",
        className: "tlIconSelect__search",
        value: d,
        onChange: g,
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
      !a && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: H }, i["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      a && x.map(
        (W) => W.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: W.label,
            onClick: () => r === "simple" ? w(A.encoded) : S(A.encoded),
            onKeyDown: (K) => {
              (K.key === "Enter" || K.key === " ") && (K.preventDefault(), r === "simple" ? w(A.encoded) : S(A.encoded));
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
        onChange: k
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: C }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: j }, i["js.iconSelect.ok"]))
  );
}, Eo = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: vo, useCallback: ht, useRef: _o } = e, Co = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(Eo), [s, i] = vo(!1), r = _o(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, b = ht(() => {
    d && !m && i(!0);
  }, [d, m]), D = ht(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), _ = ht(() => {
    i(!1);
  }, []), v = ht(async () => {
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
      "aria-label": u["js.iconSelect.chooseIcon"]
    },
    c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), s && /* @__PURE__ */ e.createElement(
    go,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: D,
      onCancel: _,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ve, useEffect: yo, useMemo: _n, useRef: wo, useState: At } = e, ko = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, No = [1, 2, 3, 4];
function So(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function Do(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of No)
    n >= o && (a = o);
  return a;
}
function To(l, t) {
  const n = ko[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Ro(l, t) {
  const n = Math.max(1, t), a = {}, o = (m, p) => !!(a[m] && a[m][p]), u = (m, p) => {
    a[m] || (a[m] = {}), a[m][p] = !0;
  }, s = [];
  let i = 0, r = 0;
  const c = (m) => {
    let p = null;
    for (const b of s) b.rowStart === m && (p = b);
    if (!p) return;
    let h = p.colEnd;
    for (; h < n && !o(m, h); ) h++;
    if (h !== p.colEnd) {
      for (let b = p.rowStart; b < p.rowEnd; b++)
        for (let D = p.colEnd; D < h; D++) u(b, D);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(To(m.width, n), n);
    for (; o(i, r); )
      r++, r >= n && (r = 0, i++);
    let b = 0;
    for (let N = r; N < n && !o(i, N); N++)
      b++;
    if (h > b) {
      for (c(i), r = 0, i++; o(i, r); )
        r++, r >= n && (r = 0, i++);
      b = 0;
      for (let N = r; N < n && !o(i, N); N++)
        b++;
      h = Math.min(h, b);
    }
    const D = r, _ = r + h, v = i, y = i + p;
    s.push({ id: m.id, colStart: D, colEnd: _, rowStart: v, rowEnd: y });
    for (let N = v; N < y; N++)
      for (let x = D; x < _; x++) u(N, x);
    r = _, r >= n && (r = 0, i++);
  }
  c(i);
  let d = 0;
  for (const m of s) m.rowEnd > d && (d = m.rowEnd);
  for (let m = 1; m < d; m++)
    for (let p = 0; p < n; p++) {
      if (o(m, p)) continue;
      const h = s.find((b) => b.rowEnd === m && b.colStart <= p && p < b.colEnd);
      if (h) {
        h.rowEnd = m + 1;
        for (let b = h.colStart; b < h.colEnd; b++) u(m, b);
      }
    }
  return s;
}
const Lo = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((w) => w && w.id), u = wo(null), [s, i] = At(1), r = t.editMode === !0;
  yo(() => {
    const w = u.current;
    if (!w) return;
    const S = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, k = So(a, S), j = () => i(Do(w.clientWidth, k));
    j();
    const C = new ResizeObserver(j);
    return C.observe(w), () => C.disconnect();
  }, [a]);
  const c = _n(() => Ro(o, s), [o, s]), d = _n(() => {
    const w = {};
    for (const S of c) w[S.id] = S;
    return w;
  }, [c]), [m, p] = At(null), [h, b] = At(null), D = Ve((w, S) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    p(S), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", S);
  }, [r]), _ = Ve((w, S) => {
    if (!r || !m || m === S) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const k = w.currentTarget.getBoundingClientRect(), j = w.clientX < k.left + k.width / 2;
    b((C) => C && C.id === S && C.before === j ? C : { id: S, before: j });
  }, [r, m]), v = Ve(() => {
  }, []), y = Ve((w, S, k) => {
    const j = o.map((A) => A.id), C = j.indexOf(w);
    if (C < 0) return;
    j.splice(C, 1);
    const H = j.indexOf(S);
    if (H < 0) {
      j.splice(C, 0, w);
      return;
    }
    const W = k ? H : H + 1;
    j.splice(W, 0, w), n("reorder", { order: j });
  }, [o, n]), N = Ve((w, S) => {
    if (!r || !m || m === S) return;
    w.preventDefault();
    const k = w.currentTarget.getBoundingClientRect(), j = w.clientX < k.left + k.width / 2;
    y(m, S, j), p(null), b(null);
  }, [r, m, y]), x = Ve(() => {
    p(null), b(null);
  }, []), g = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: g }, o.map((w) => {
      const S = d[w.id];
      if (!S) return null;
      const k = {
        gridColumn: `${S.colStart + 1} / ${S.colEnd + 1}`,
        gridRow: `${S.rowStart + 1} / ${S.rowEnd + 1}`
      }, j = ["tlDashboard__tile"];
      return m === w.id && j.push("tlDashboard__tile--dragging"), h && h.id === w.id && j.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: j.join(" "),
          style: k,
          draggable: r,
          onDragStart: (C) => D(C, w.id),
          onDragOver: (C) => _(C, w.id),
          onDragLeave: v,
          onDrop: (C) => N(C, w.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(G, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: xo, useRef: Cn, useState: yn, useEffect: Io, useLayoutEffect: Mo } = e, jo = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Po = ({ group: l }) => {
  var m, p;
  const [t, n] = yn(!1), [a, o] = yn({}), u = Cn(null), s = Cn(null), i = xo(() => {
    n((h) => !h);
  }, []);
  Mo(() => {
    if (!t) return;
    const h = () => {
      const b = u.current;
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
  }, [t]), Io(() => {
    if (!t) return;
    const h = (b) => {
      s.current && !s.current.contains(b.target) && u.current && !u.current.contains(b.target) && n(!1);
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
  ), Dn.createPortal(
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
      (p = l.subGroups) == null ? void 0 : p.map((h, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((D, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: D })))))
    ),
    document.body
  ));
}, Bo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((u) => u != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, u) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, u > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Po, { group: o }) : /* @__PURE__ */ e.createElement(jo, { group: o }))));
}, Ao = ({ frame: l, covered: t }) => {
  const [n, a] = rt(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, Fo = ({ controlId: l }) => {
  const t = X(), [n, a] = rt(), o = t.frames ?? [], u = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, i) => /* @__PURE__ */ e.createElement(Ao, { key: s.controlId, frame: s, covered: i !== u }))));
}, Oo = ({ controlId: l }) => {
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
}, $o = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, Ho = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Wo = {
  "js.sidebar.openDrawer": "Open navigation"
}, Uo = ({ controlId: l }) => {
  const t = ne(), n = ue(Wo);
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
z("TLButton", _l);
z("TLUploadButton", Cl);
z("TLToggleButton", wl);
z("TLTextInput", al);
z("TLPasswordInput", ol);
z("TLNumberInput", cl);
z("TLDatePicker", ul);
z("TLSelect", ml);
z("TLBooleanChoice", fl);
z("TLCheckbox", El);
z("TLCounter", kl);
z("TLTabBar", Sl);
z("TLFieldList", Dl);
z("TLAudioRecorder", Rl);
z("TLAudioPlayer", xl);
z("TLFileUpload", Ml);
z("TLBinaryField", Pl);
z("TLFileChips", Fl);
z("TLRelativeTime", Hl);
z("TLAnchor", Wl);
z("TLScrollLink", Ul);
z("TLAvatar", Kl);
z("TLDownload", Gl);
z("TLPhotoCapture", ql);
z("TLPhotoViewer", Ql);
z("TLPdfViewer", ea);
z("TLSplitPanel", ta);
z("TLPanel", ca);
z("TLInset", _a);
z("TLMaximizeRoot", ia);
z("TLDeckPane", ua);
z("TLSidebar", Ea);
z("TLStack", va);
z("TLGrid", Ca);
z("TLCard", ya);
z("TLAppBar", wa);
z("TLBreadcrumb", Na);
z("TLBottomBar", Da);
z("TLDialog", La);
z("TLDialogManager", Ma);
z("TLWindow", Aa);
z("TLDrawer", $a);
z("TLMenuRegion", Wa);
z("TLSnackbar", Ka);
z("TLNoticeBar", Ja);
z("TLMenu", tr);
z("TLAppShell", lr);
z("TLText", ar);
z("TLTableView", ir);
z("TLColumnSelect", dr);
z("TLCalendar", Nr);
z("TLFormLayout", jr);
z("TLFormGroup", Ar);
z("TLFormField", Hr);
z("TLResourceCell", Wr);
z("TLTreeView", zr);
z("TLDropdownSelect", Zr);
z("TLColorInput", po);
z("TLIconSelect", Co);
z("TLDashboard", Lo);
z("TLToolbar", Bo);
z("TLTileStack", Fo);
z("TLAdaptiveDetail", Oo);
z("TLSlot", $o);
z("TLSlotContent", Ho);
z("TLDrawerToggle", Uo);
