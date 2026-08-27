import { React as e, useTLFieldValue as Se, useTLCommand as oe, useTLState as G, useKeyboardBinding as me, useTLUpload as Be, TLChild as K, useI18N as ue, useTLDataUrl as Oe, scrollToAnchor as rn, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as bt, useFocusTrap as _t, CMD_VALUE_CHANGED as We, anchoredOverlayProps as on, register as $ } from "tl-react-bridge";
const { useCallback: wt, useRef: sn } = e, cn = 300, un = ({ controlId: l, state: t }) => {
  const [n, a, s] = Se({
    debounceMs: cn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), c = oe(), i = sn(!1), u = wt(
    (S) => {
      i.current = !0, a(S.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = wt(async () => {
    await s(), r && i.current && (i.current = !1, c("commit"));
  }, [s, r, c]), d = t.multiline === !0;
  if (t.editable === !1) {
    const S = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: S,
        style: d ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const p = t.hasError === !0, f = t.hasWarnings === !0, g = t.errorMessage, _ = [
    "tlReactTextInput",
    d ? "tlReactTextInput--multiline" : "",
    p ? "tlReactTextInput--error" : "",
    !p && f ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, d ? /* @__PURE__ */ e.createElement(
    "textarea",
    {
      rows: t.rows ?? 3,
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: _,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: _,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
    }
  ));
}, { useCallback: yt } = e, dn = 300, mn = ({ controlId: l, state: t }) => {
  const [n, a, s] = Se({ debounceMs: dn }), c = yt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = yt(() => {
    s();
  }, [s]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = t.errorMessage, d = [
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
      onBlur: i,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && o ? o : void 0
    }
  ));
}, { useCallback: St } = e, pn = 300, fn = ({ controlId: l, state: t, config: n }) => {
  const [a, s, c] = Se({ debounceMs: pn }), i = St(
    (f) => {
      const g = f.target.value;
      s(g === "" ? null : g);
    },
    [s]
  ), u = St(() => {
    c();
  }, [c]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, o = t.hasWarnings === !0, d = t.errorMessage, p = [
    "tlReactNumberInput",
    r ? "tlReactNumberInput--error" : "",
    !r && o ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: a != null ? String(a) : "",
      onChange: i,
      onBlur: u,
      disabled: t.disabled === !0,
      className: p,
      "aria-invalid": r || void 0,
      title: r && d ? d : void 0
    }
  ));
}, { useCallback: hn } = e, bn = ({ controlId: l, state: t }) => {
  const [n, a] = Se(), s = hn(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const c = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && i ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: t.inputType ?? "date",
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: _n } = e, gn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, s] = Se(), c = _n(
    (p) => {
      s(p.target.value || null);
    },
    [s]
  ), i = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((d = i.find((f) => f.value === a)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = [
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
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    i.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: vn } = e, En = ({ controlId: l, state: t }) => {
  const [n, a] = Se(), s = t.options ?? [], c = t.presentation === "select", i = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, o = vn(
    (f) => {
      const g = s[f];
      a(g ? g.value : null);
    },
    [s, a]
  ), d = s.findIndex((f) => f.value === (n ?? null));
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlBooleanChoice tlBooleanChoice--immutable" }, d >= 0 ? s[d].label : "");
  const p = [
    "tlBooleanChoice",
    u ? "tlBooleanChoice--error" : "",
    !u && r ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: p + " tlReactSelect",
      value: d >= 0 ? String(d) : "",
      disabled: i,
      "aria-invalid": u || void 0,
      onChange: (f) => o(Number(f.target.value))
    },
    d < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((f, g) => /* @__PURE__ */ e.createElement("option", { key: g, value: String(g) }, f.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: p + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": u || void 0
    },
    s.map((f, g) => /* @__PURE__ */ e.createElement("label", { key: g, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: d === g,
        disabled: i,
        onChange: () => o(g)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, f.label)))
  );
}, { useCallback: Cn, useRef: wn, useEffect: yn } = e, Sn = ({ controlId: l, state: t }) => {
  const [n, a] = Se(), s = t.triState === !0, c = wn(null);
  yn(() => {
    c.current && (c.current.indeterminate = s && n !== !0 && n !== !1);
  }, [s, n]);
  const i = Cn(
    (d) => {
      if (!s) {
        a(d.target.checked);
        return;
      }
      a(n === !0 ? !1 : n === !1 ? null : !0);
    },
    [a, s, n]
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
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = [
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
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0,
      "aria-checked": s && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function ye({ encoded: l, className: t }) {
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
const { useCallback: kn } = e, Nn = ({ controlId: l, command: t, label: n, image: a, disabled: s, displayMode: c }) => {
  const i = G(), u = oe(), r = t ?? "click", o = n ?? i.label, d = a ?? i.image, p = s ?? i.disabled === !0, f = c ?? i.displayMode ?? "label-only", g = i.hidden === !0, _ = i.tooltip, S = i.appearance, C = i.size, v = i.navigateUrl, y = kn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(r);
  }, [u, r, v]), I = i.keyGesture;
  me(I, () => p || g ? !1 : (y(), !0));
  const L = f === "icon-only", b = f === "label-only" || f === "icon-label" || L && !d, w = _ ?? (L ? o : void 0), h = w ? `text:${w}` : void 0;
  return g ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: p,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (S === "link" ? " tlReactButton--link" : "") + (S === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": d || L ? o : void 0
    },
    d && /* @__PURE__ */ e.createElement(ye, { encoded: d, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, Tn = ({ controlId: l }) => {
  const t = G(), n = Be(), a = e.useRef(null), [s, c] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, d = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, _ = e.useCallback(() => {
    var L;
    r || s || (L = a.current) == null || L.click();
  }, [r, s]), S = e.useCallback(async (L) => {
    const b = L.target.files;
    if (!b || b.length === 0) return;
    const w = new FormData();
    for (let h = 0; h < b.length; h++)
      w.append("file", b[h], b[h].name);
    L.target.value = "", c(!0);
    try {
      await n(w);
    } finally {
      c(!1);
    }
  }, [n]), C = d === "icon-only", v = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || C && !u, I = r || s;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: g || void 0,
      onChange: S,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: _,
      disabled: I,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: Rn } = e, Dn = ({ controlId: l, command: t, label: n, active: a, disabled: s }) => {
  const c = G(), i = oe(), u = t ?? "click", r = n ?? c.label, o = a ?? c.active === !0, d = s ?? c.disabled === !0, p = Rn(() => {
    i(u);
  }, [i, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: d,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    r
  );
}, Ln = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: xn } = e, In = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.tabs ?? [], s = t.activeTabId, c = xn((i) => {
    i !== s && n("selectTab", { tabId: i });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((i) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: i.id,
      role: "tab",
      "aria-selected": i.id === s,
      className: "tlReactTabBar__tab" + (i.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(i.id)
    },
    i.icon && /* @__PURE__ */ e.createElement(ye, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Pn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((s, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: s })))));
}, Mn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, jn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), d = t.status ?? "idle", p = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, g = e.useCallback(async () => {
    if (a === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = y, r.current = [];
        const I = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(y, I ? { mimeType: I } : void 0);
        u.current = L, L.ondataavailable = (b) => {
          b.data.size > 0 && r.current.push(b.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((h) => h.stop()), o.current = null;
          const b = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], b.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const w = new FormData();
          w.append("audio", b, "recording.webm"), await n(w), s("idle");
        }, L.start(), s("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), i("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [a, n]), _ = ue(Mn), S = f === "recording" ? _["js.audioRecorder.stop"] : f === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], C = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: g,
      disabled: C,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[c]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, An = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Bn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasAudio, s = t.dataRevision ?? 0, [c, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(s);
  e.useEffect(() => {
    a ? c === "disabled" && i("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), i("disabled"));
  }, [a]), e.useEffect(() => {
    s !== o.current && (o.current = s, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (c === "playing" || c === "paused" || c === "loading") && i("idle"));
  }, [s]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (c === "disabled" || c === "loading")
      return;
    if (c === "playing") {
      u.current && u.current.pause(), i("paused");
      return;
    }
    if (c === "paused" && u.current) {
      u.current.play(), i("playing");
      return;
    }
    if (!r.current) {
      i("loading");
      try {
        const C = await fetch(n);
        if (!C.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", C.status), i("idle");
          return;
        }
        const v = await C.blob();
        r.current = URL.createObjectURL(v);
      } catch (C) {
        console.error("[TLAudioPlayer] Fetch error:", C), i("idle");
        return;
      }
    }
    const S = new Audio(r.current);
    u.current = S, S.onended = () => {
      i("idle");
    }, S.play(), i("playing");
  }, [c, n]), p = ue(An), f = c === "loading" ? p["js.loading"] : c === "playing" ? p["js.audioPlayer.pause"] : c === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = c === "disabled" || c === "loading", _ = ["tlAudioPlayer__button"];
  return c === "playing" && _.push("tlAudioPlayer__button--playing"), c === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: d,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, On = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Fn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, s] = e.useState("idle"), [c, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, d = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (b) => {
    s("uploading");
    const w = new FormData();
    w.append("file", b, b.name), await n(w), s("idle");
  }, [n]), g = e.useCallback((b) => {
    var h;
    const w = (h = b.target.files) == null ? void 0 : h[0];
    w && f(w);
  }, [f]), _ = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = u.current) == null || b.click());
  }, [a]), S = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!0);
  }, []), C = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), i(!1);
  }, []), v = e.useCallback((b) => {
    var h;
    if (b.preventDefault(), b.stopPropagation(), i(!1), a === "uploading") return;
    const w = (h = b.dataTransfer.files) == null ? void 0 : h[0];
    w && f(w);
  }, [a, f]), y = p === "uploading", I = ue(On), L = p === "uploading" ? I["js.uploading"] : I["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: C,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: d || void 0,
        onChange: g,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: y,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, $n = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Un = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, s = Be(), c = Oe(), i = ue($n), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", d = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [_, S] = e.useState("idle"), [C, v] = e.useState(!1), [y, I] = e.useState(!1), L = e.useRef(null), b = e.useCallback(async () => {
    if (!(!r || y)) {
      I(!0);
      try {
        const F = c + (c.includes("?") ? "&" : "?") + "rev=" + d, B = await fetch(F);
        if (!B.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", B.status);
          return;
        }
        const M = await B.blob(), q = URL.createObjectURL(M), m = document.createElement("a");
        m.href = q, m.download = o, m.style.display = "none", document.body.appendChild(m), m.click(), document.body.removeChild(m), URL.revokeObjectURL(q);
      } catch (F) {
        console.error("[TLBinaryField] Fetch error:", F);
      } finally {
        I(!1);
      }
    }
  }, [r, y, c, d, o]), w = e.useCallback(async (F) => {
    S("uploading");
    const B = new FormData();
    B.append("file", F, F.name), await s(B), S("idle");
  }, [s]), h = (f === "received" ? "idle" : _ !== "idle" ? _ : f) === "uploading", D = e.useCallback((F) => {
    var M;
    const B = (M = F.target.files) == null ? void 0 : M[0];
    B && w(B);
  }, [w]), R = e.useCallback(() => {
    var F;
    h || (F = L.current) == null || F.click();
  }, [h]), N = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!0);
  }, []), z = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!1);
  }, []), A = e.useCallback((F) => {
    var M;
    if (F.preventDefault(), F.stopPropagation(), v(!1), h) return;
    const B = (M = F.dataTransfer.files) == null ? void 0 : M[0];
    B && w(B);
  }, [h, w]), x = y ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: y,
      title: x,
      "aria-label": x
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Z = h, H = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: z,
      onDrop: A
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "file",
        accept: p || void 0,
        onChange: D,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Z ? " tlFileUpload__button--uploading" : ""),
        onClick: R,
        disabled: Z,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, Hn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Wn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const zn = ({ controlId: l }) => {
  const t = G(), n = oe(), a = Be(), s = Oe(), c = ue(Hn), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [d, p] = e.useState(!1), f = e.useRef(null), g = e.useCallback(async (b) => {
    const w = Array.from(b);
    if (w.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const D of w)
          h.append("file", D, D.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), _ = e.useCallback(async (b) => {
    if (b.hasData)
      try {
        const w = s + "&key=" + encodeURIComponent(b.key), h = await fetch(w);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const D = await h.blob(), R = URL.createObjectURL(D), N = document.createElement("a");
        N.href = R, N.download = b.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(R);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [s]), S = e.useCallback((b) => {
    b.target.files && g(b.target.files), b.target.value = "";
  }, [g]), C = e.useCallback(() => {
    var b;
    r || (b = f.current) == null || b.click();
  }, [r]), v = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!0));
  }, [u]), y = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!1));
  }, [u]), I = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), p(!1), !r && b.dataTransfer.files && g(b.dataTransfer.files));
  }, [u, r, g]), L = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: L,
      onDragOver: v,
      onDragLeave: y,
      onDrop: I
    },
    i.map((b) => {
      const w = c["js.download.file"].replace("{0}", b.name), h = c["js.fileChips.remove"].replace("{0}", b.name);
      return /* @__PURE__ */ e.createElement("span", { key: b.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => _(b),
          disabled: !b.hasData,
          title: b.hasData ? w : b.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, b.name),
        b.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Wn(b.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: b.key }),
          title: h,
          "aria-label": h
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
        onChange: S,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: C,
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
}, Vn = 3e4;
function Kn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), s = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? s.format(Math.trunc(n / 1), "second") : a < 3600 ? s.format(Math.trunc(n / 60), "minute") : a < 86400 ? s.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? s.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Yn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, s = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => c((u) => u + 1), Vn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Kn(n, s));
}, Gn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Xn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const s = (c) => {
    c.preventDefault(), rn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: s }, a);
};
function qn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Zn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Qn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Zn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    qn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Jn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, el = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = oe(), s = !!t.hasData, c = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!s || r)) {
      o(!0);
      try {
        const _ = n + (n.includes("?") ? "&" : "?") + "rev=" + c, S = await fetch(_);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const C = await S.blob(), v = URL.createObjectURL(C), y = document.createElement("a");
        y.href = v, y.download = i, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        o(!1);
      }
    }
  }, [s, r, n, c, i]), p = e.useCallback(async () => {
    s && await a("clear");
  }, [s, a]), f = ue(Jn);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const g = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", i);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: r,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: i }, i), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, tl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, nl = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), _ = t.error, S = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), C = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    C(), s("idle");
  }, [C]), y = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !S) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const z = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = z, s("overlayOpen");
      } catch (z) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", z), i("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [a, S]), I = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, z = p.current;
    if (!N || !z)
      return;
    z.width = N.videoWidth, z.height = N.videoHeight;
    const A = z.getContext("2d");
    A && (A.drawImage(N, 0, 0), C(), s("uploading"), z.toBlob(async (x) => {
      if (!x) {
        s("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", x, "capture.jpg"), await n(O), s("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), L = e.useCallback(async (N) => {
    var x;
    const z = (x = N.target.files) == null ? void 0 : x[0];
    if (!z) return;
    s("uploading");
    const A = new FormData();
    A.append("photo", z, z.name), await n(A), s("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && d.current && (o.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var z;
    if (a !== "overlayOpen") return;
    (z = g.current) == null || z.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Le(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null);
  }, []);
  const b = ue(tl), w = a === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  u && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const R = ["tlPhotoCapture__mirrorBtn"];
  return u && R.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: g,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: D.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: R.join(" "),
        onClick: () => r((N) => !N),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: I,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[c]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, ll = {
  "js.photoViewer.alt": "Captured photo"
}, al = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPhoto, s = t.dataRevision ?? 0, [c, i] = e.useState(null), u = e.useRef(s);
  e.useEffect(() => {
    if (!a) {
      c && (URL.revokeObjectURL(c), i(null));
      return;
    }
    if (s === u.current && c)
      return;
    u.current = s, c && (URL.revokeObjectURL(c), i(null));
    let o = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const p = await d.blob();
        o || i(URL.createObjectURL(p));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      o = !0;
    };
  }, [a, s, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const r = ue(ll);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, rl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, ol = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, s = t.dataRevision ?? 0, c = ue(rl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + s, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: kt, useRef: et } = e, sl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.orientation, s = t.resizable === !0, c = t.children ?? [], i = a === "horizontal", u = c.length > 0 && c.every((C) => C.collapsed), r = !u && c.some((C) => C.collapsed), o = u ? !i : i, d = et(null), p = et(null), f = et(null), g = kt((C, v) => {
    const y = {
      overflow: C.scrolling || "auto"
    };
    return C.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : v !== void 0 ? y.flex = `0 0 ${v}px` : y.flex = `${C.size} 1 0%`, C.minSize > 0 && !C.collapsed && (y.minWidth = i ? C.minSize : void 0, y.minHeight = i ? void 0 : C.minSize), y;
  }, [i, u, r, o]), _ = kt((C, v) => {
    C.preventDefault();
    const y = d.current;
    if (!y) return;
    const I = c[v], L = c[v + 1], b = y.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    b.forEach((R) => {
      w.push(i ? R.offsetWidth : R.offsetHeight);
    }), f.current = w, p.current = {
      splitterIndex: v,
      startPos: i ? C.clientX : C.clientY,
      startSizeBefore: w[v],
      startSizeAfter: w[v + 1],
      childBefore: I,
      childAfter: L
    };
    const h = (R) => {
      const N = p.current;
      if (!N || !f.current) return;
      const A = (i ? R.clientX : R.clientY) - N.startPos, x = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Z = N.startSizeBefore + A, H = N.startSizeAfter - A;
      Z < x && (H += Z - x, Z = x), H < O && (Z += H - O, H = O), f.current[N.splitterIndex] = Z, f.current[N.splitterIndex + 1] = H;
      const F = y.querySelectorAll(":scope > .tlSplitPanel__child"), B = F[N.splitterIndex], M = F[N.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${Z}px`), M && (M.style.flex = `0 0 ${H}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const R = {};
        c.forEach((N, z) => {
          const A = N.control;
          A != null && A.controlId && f.current && (R[A.controlId] = f.current[z]);
        }), n("updateSizes", { sizes: R });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", D), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, i, n]), S = [];
  return c.forEach((C, v) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${C.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(C)
        },
        /* @__PURE__ */ e.createElement(K, { control: C.control })
      )
    ), s && v < c.length - 1) {
      const y = c[v + 1];
      !C.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => _(L, v)
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
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: tt } = e, cl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, il = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ul = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), dl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ml = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), pl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), fl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(cl), s = t.title, c = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, d = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", g = t.errorMessage, _ = c === "MINIMIZED", S = c === "MAXIMIZED", C = c === "HIDDEN", v = tt(() => {
    n("toggleMinimize");
  }, [n]), y = tt(() => {
    n("toggleMaximize");
  }, [n]), I = tt(() => {
    n("popOut");
  }, [n]);
  if (C)
    return null;
  const L = S ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, b = i && !S || u && !_ || r, w = !!s && s.trim() !== "" || !!t.titleContent || !!t.toolbar || b;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: L
    },
    w && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!s && s.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !S && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: _ ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      _ ? /* @__PURE__ */ e.createElement(ul, null) : /* @__PURE__ */ e.createElement(il, null)
    ), u && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: S ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      S ? /* @__PURE__ */ e.createElement(ml, null) : /* @__PURE__ */ e.createElement(dl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: I,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(pl, null)
    ))),
    !_ && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !_ && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, g)),
    !_ && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, hl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, bl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ge, useState: Xe, useEffect: dt, useRef: Ze } = e, _l = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function mt(l, t, n, a) {
  const s = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      s.push({ id: c.id, type: "nav", groupId: a });
    } else c.type === "command" ? s.push({ id: c.id, type: "command", groupId: a }) : c.type === "group" && (s.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && s.push(...mt(c.children, t, n, c.id)));
  return s;
}
const Ae = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, gl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: s, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: s,
    ref: c,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), vl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: s,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), El = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Cl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), wl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: s, onClose: c }) => {
  const i = Ze(null);
  dt(() => {
    const o = (d) => {
      i.current && !i.current.contains(d.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [c]), Le(!0, { ESCAPE: c });
  const u = ge((o) => {
    o.type === "nav" ? (a(o.id), c()) : o.type === "command" && (s(o.id), c());
  }, [a, s, c]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: i, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" && o.hidden) return null;
    if (o.type === "nav" || o.type === "command") {
      const d = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(o)
        },
        /* @__PURE__ */ e.createElement(Ae, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, yl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: s,
  onExecute: c,
  onToggleGroup: i,
  tabIndex: u,
  itemRef: r,
  onFocus: o,
  focusedId: d,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: g,
  onOpenFlyout: _,
  onCloseFlyout: S
}) => {
  const C = Ze(null), [v, y] = Xe(null), I = ge(() => {
    a ? g === l.id ? S() : (C.current && y(C.current.getBoundingClientRect()), _(l.id)) : i(l.id);
  }, [a, g, l.id, i, _, S]), L = ge((w) => {
    C.current = w, r(w);
  }, [r]), b = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: I,
      title: a ? l.label : void 0,
      "aria-expanded": a ? b : t,
      tabIndex: u,
      ref: L,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
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
  ), b && /* @__PURE__ */ e.createElement(
    wl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: s,
      onExecute: c,
      onClose: S
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: w.id,
      item: w,
      activeItemId: n,
      collapsed: a,
      onSelect: s,
      onExecute: c,
      onToggleGroup: i,
      focusedId: d,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: _,
      onCloseFlyout: S
    }
  ))));
}, Ht = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: s,
  onToggleGroup: c,
  focusedId: i,
  setItemRef: u,
  onItemFocus: r,
  groupStates: o,
  flyoutGroupId: d,
  onOpenFlyout: p,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        gl,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        vl,
        {
          item: l,
          collapsed: n,
          onExecute: s,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(El, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Cl, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        yl,
        {
          item: l,
          expanded: g,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: s,
          onToggleGroup: c,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: i,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: d,
          onOpenFlyout: p,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, Sl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(_l), s = t.items ?? [], c = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, d] = Xe(() => {
    const x = /* @__PURE__ */ new Map(), O = (Z) => {
      for (const H of Z)
        H.type === "group" && (x.set(H.id, H.expanded), O(H.children));
    };
    return O(s), x;
  }), p = ge((x) => {
    d((O) => {
      const Z = new Map(O), H = Z.get(x) ?? !1;
      return Z.set(x, !H), n("toggleGroup", { itemId: x, expanded: !H }), Z;
    });
  }, [n]), f = ge((x) => {
    x !== c && n("selectItem", { itemId: x });
  }, [n, c]), g = ge((x) => {
    n("executeCommand", { itemId: x });
  }, [n]), _ = ge(() => {
    n("toggleCollapse", {});
  }, [n]), S = ge(() => {
    n("toggleDrawer", {});
  }, [n]), [C, v] = Xe(null), y = ge((x) => {
    v(x);
  }, []), I = ge(() => {
    v(null);
  }, []);
  dt(() => {
    r || v(null);
  }, [r]);
  const [L, b] = Xe(() => {
    const x = mt(s, r, o);
    return x.length > 0 ? x[0].id : "";
  }), w = Ze(/* @__PURE__ */ new Map()), h = ge((x) => (O) => {
    O ? w.current.set(x, O) : w.current.delete(x);
  }, []), D = ge((x) => {
    b(x);
  }, []), R = Ze(0), N = ge((x) => {
    b(x), R.current++;
  }, []);
  dt(() => {
    const x = w.current.get(L);
    x && document.activeElement !== x && x.focus();
  }, [L, R.current]);
  const z = ge((x) => {
    if (x.key === "Escape" && C !== null) {
      x.preventDefault(), I();
      return;
    }
    const O = mt(s, r, o);
    if (O.length === 0) return;
    const Z = O.findIndex((F) => F.id === L);
    if (Z < 0) return;
    const H = O[Z];
    switch (x.key) {
      case "ArrowDown": {
        x.preventDefault();
        const F = (Z + 1) % O.length;
        N(O[F].id);
        break;
      }
      case "ArrowUp": {
        x.preventDefault();
        const F = (Z - 1 + O.length) % O.length;
        N(O[F].id);
        break;
      }
      case "Home": {
        x.preventDefault(), N(O[0].id);
        break;
      }
      case "End": {
        x.preventDefault(), N(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        x.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? g(H.id) : H.type === "group" && (r ? C === H.id ? I() : y(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (x.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (x.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    s,
    r,
    o,
    L,
    C,
    N,
    f,
    g,
    p,
    y,
    I
  ]), A = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: z }, s.map((x) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: x.id,
      item: x,
      activeItemId: c,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: h,
      onItemFocus: D,
      groupStates: o,
      flyoutGroupId: C,
      onOpenFlyout: y,
      onCloseFlyout: I
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, kl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", s = t.align ?? "stretch", c = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${s}`,
    c ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o })));
}, Nl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Tl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, s = t.gap ?? "default", c = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: i }, c.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Rl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", s = t.padding ?? "default", c = t.headerActions ?? [], i = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, Dl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, s = t.children ?? [], c = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, s.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))));
}, { useCallback: Ll } = e, xl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], s = Ll((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((c, i) => {
    const u = i === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: c.id, className: "tlBreadcrumb__entry" }, i > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => s(c.id)
      },
      c.label
    ));
  })));
}, { useCallback: Il } = e, Pl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], s = t.activeItemId, c = Il((i) => {
    i !== s && n("selectItem", { itemId: i });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((i) => {
    const u = i.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: i.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => c(i.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + i.icon, "aria-hidden": "true" }), i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, i.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, i.label)
    );
  }));
}, { useCallback: Nt, useRef: Ml } = e, jl = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Al = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, s = t.closeOnBackdrop !== !1, c = t.child, i = Ml(null), u = Nt(() => {
    n("close");
  }, [n]), r = Nt((o) => {
    s && o.target === o.currentTarget && u();
  }, [s, u]);
  return a ? /* @__PURE__ */ e.createElement(bt, null, /* @__PURE__ */ e.createElement(jl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: c })
  )) : null;
}, { useEffect: Bl, useRef: Ol } = e, Fl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Ol(n.length);
  return Bl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((s) => /* @__PURE__ */ e.createElement(K, { key: s.controlId, control: s })));
}, { useCallback: ze, useRef: Ie, useState: Ve } = e, $l = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ul = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Hl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Wl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Ul), s = t.title ?? "", c = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, d = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, _] = Ve(null), [S, C] = Ve(null), [v, y] = Ve(null), I = Ie(null), [L, b] = Ve(!1), w = Ie(null), h = Ie(null), D = Ie(null), R = Ie(null), N = Ie(null), z = ze(() => {
    n("close");
  }, [n]);
  _t(!0, R, "field");
  const A = ze((F, B) => {
    B.preventDefault();
    const M = R.current;
    if (!M) return;
    const q = M.getBoundingClientRect(), m = !I.current, T = I.current ?? { x: q.left, y: q.top };
    m && (I.current = T, y(T)), N.current = {
      dir: F,
      startX: B.clientX,
      startY: B.clientY,
      startW: q.width,
      startH: q.height,
      startPos: { ...T },
      symmetric: m
    };
    const V = (X) => {
      const j = N.current;
      if (!j) return;
      const te = X.clientX - j.startX, ce = X.clientY - j.startY;
      let ne = j.startW, _e = j.startH, ve = 0, Ce = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * te), j.dir.includes("w") && (ne = j.startW - 2 * te), j.dir.includes("s") && (_e = j.startH + 2 * ce), j.dir.includes("n") && (_e = j.startH - 2 * ce)) : (j.dir.includes("e") && (ne = j.startW + te), j.dir.includes("w") && (ne = j.startW - te, ve = te), j.dir.includes("s") && (_e = j.startH + ce), j.dir.includes("n") && (_e = j.startH - ce, Ce = ce));
      const ke = Math.max(200, ne), Ne = Math.max(100, _e);
      j.symmetric ? (ve = (j.startW - ke) / 2, Ce = (j.startH - Ne) / 2) : (j.dir.includes("w") && ke === 200 && (ve = j.startW - 200), j.dir.includes("n") && Ne === 100 && (Ce = j.startH - 100)), h.current = ke, D.current = Ne, _(ke), C(Ne);
      const xe = {
        x: j.startPos.x + ve,
        y: j.startPos.y + Ce
      };
      I.current = xe, y(xe);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
      const X = h.current, j = D.current;
      (X != null || j != null) && n("resize", {
        ...X != null ? { width: Math.round(X) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, [n]), x = ze((F) => {
    if (F.button !== 0 || F.target.closest("button")) return;
    F.preventDefault();
    const B = R.current;
    if (!B) return;
    const M = B.getBoundingClientRect(), q = I.current ?? { x: M.left, y: M.top }, m = F.clientX - q.x, T = F.clientY - q.y, V = (X) => {
      const j = window.innerWidth, te = window.innerHeight;
      let ce = X.clientX - m, ne = X.clientY - T;
      const _e = B.offsetWidth, ve = B.offsetHeight;
      ce + _e > j && (ce = j - _e), ne + ve > te && (ne = te - ve), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const Ce = { x: ce, y: ne };
      I.current = Ce, y(Ce);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, []), O = ze(() => {
    var F, B;
    if (L) {
      const M = w.current;
      M && (y(M.x !== -1 ? { x: M.x, y: M.y } : null), _(M.w), C(M.h)), b(!1);
    } else {
      const M = R.current, q = M == null ? void 0 : M.getBoundingClientRect();
      w.current = {
        x: ((F = I.current) == null ? void 0 : F.x) ?? (q == null ? void 0 : q.left) ?? -1,
        y: ((B = I.current) == null ? void 0 : B.y) ?? (q == null ? void 0 : q.top) ?? -1,
        w: g ?? (q == null ? void 0 : q.width) ?? null,
        h: S ?? null
      }, b(!0), y({ x: 0, y: 0 }), _(null), C(null);
    }
  }, [L, g, S]), Z = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : c,
    ...S != null ? { height: S + "px" } : i != null ? { height: i } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(bt, { modal: !0 }, /* @__PURE__ */ e.createElement($l, { onClose: z }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Z,
      ref: R,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : x,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, s),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
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
          onClick: z,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(K, { control: o })),
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), d.map((F, B) => /* @__PURE__ */ e.createElement(K, { key: B, control: F }))),
    r && !L && Hl.map((F) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: F,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${F}`,
        onMouseDown: (B) => A(F, B)
      }
    ))
  ));
}, { useCallback: zl } = e, Vl = {
  "js.drawer.close": "Close"
}, Kl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Vl), s = t.open === !0, c = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = zl(() => {
    n("close");
  }, [n]);
  Le(s, { ESCAPE: o });
  const d = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${i}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !s }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(K, { control: r })));
}, { useCallback: Yl } = e, Gl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.child, s = Yl((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: s }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: Xl, useEffect: Tt, useRef: ql, useState: Rt } = e, Zl = 250, Ql = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.message ?? "", s = t.content ?? "", c = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, d] = Rt(!1), [p, f] = Rt(!1), g = ql(!1);
  Tt(() => {
    g.current = !1;
  }, [r]);
  const _ = Xl(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Tt(() => {
    if (!u || i === 0 || p) return;
    const S = setTimeout(_, g.current ? Zl : i);
    return () => clearTimeout(S);
  }, [u, i, p, _]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${c}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        g.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    s ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: s } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: nt, useEffect: Dt, useRef: Jl, useState: Lt } = e, ea = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, s = t.anchorId, c = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Jl(null), [o, d] = Lt({ top: 0, left: 0 }), [p, f] = Lt(0), g = u.filter((v) => v.type === "item" && !v.disabled);
  Dt(() => {
    var h, D;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (c != null && i != null) {
      let R = i, N = c;
      R + v > window.innerHeight && (R = Math.max(0, window.innerHeight - v)), N + y > window.innerWidth && (N = Math.max(0, window.innerWidth - y)), d({ top: R, left: N }), f(0);
      return;
    }
    if (!s) return;
    const I = document.getElementById(s);
    if (!I) return;
    const L = I.getBoundingClientRect();
    let b = L.bottom + 4, w = L.left;
    b + v > window.innerHeight && (b = L.top - v - 4), w + y > window.innerWidth && (w = L.right - y), d({ top: b, left: w }), f(0);
  }, [a, s, c, i]);
  const _ = nt(() => {
    n("close");
  }, [n]), S = nt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  Dt(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && _();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, _]);
  const C = nt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), _();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((y) => (y + 1) % g.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((y) => (y - 1 + g.length) % g.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const y = g[p];
      y && S(y.id);
    }
  }, [_, S, g, p]);
  return _t(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: C
    },
    u.map((v, y) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const L = g.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, ta = 768, na = ({ controlId: l }) => {
  const t = G(), n = oe();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${ta}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(o.matches);
    const p = (f) => d(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, s = t.content, c = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: s })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: c })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, la = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", s = t.hasTooltip === !0, c = t.role || void 0, i = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: i,
      role: c,
      "data-tooltip": s ? "key:tooltip" : void 0
    },
    n
  );
}, aa = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: s }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (s(), !0) : !1), null), ra = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, xt = 50, oa = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, oa));
}
const pt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', sa = pt + ", button:not([disabled]), a[href]";
function Wt(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function at(l, t, n = {}) {
  const a = Wt(l, t);
  if (n.col) {
    const c = a.find((u) => u.dataset.col === n.col), i = c == null ? void 0 : c.querySelector(pt);
    if (i) return i;
  }
  if (n.col)
    return null;
  const s = n.last ? [...a].reverse() : a;
  for (const c of s) {
    const i = c.querySelector(pt);
    if (i) return i;
  }
  return null;
}
const ca = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(ra), s = e.useRef(null);
  e.useEffect(() => {
    const E = s.current;
    if (!E) return;
    const k = (U) => {
      const Q = U.detail;
      let ee = Q.target;
      for (; ee && ee !== E; ) {
        const ae = ee.dataset.row, re = ee.dataset.col;
        if (ae != null && re != null) {
          Q.resolved = { key: ae + "|" + re };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return E.addEventListener("tl-tooltip-resolve", k), () => E.removeEventListener("tl-tooltip-resolve", k);
  }, []);
  const c = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", d = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, _ = t.columnSelect ?? !1, S = e.useMemo(
    () => c.filter((E) => E.sortPriority && E.sortPriority > 0).length,
    [c]
  ), C = o === "multi", v = 40, y = 20, I = e.useRef(null), L = e.useRef(null), b = e.useRef(null), w = e.useRef(null), h = e.useRef(null), [D, R] = e.useState({}), N = e.useRef(null), z = e.useRef(!1), A = e.useRef(null), [x, O] = e.useState(null), [Z, H] = e.useState(null), [F, B] = e.useState(null), [M, q] = e.useState(0);
  e.useEffect(() => {
    const E = b.current;
    if (!E)
      return;
    const k = () => {
      const Q = E.offsetWidth - E.clientWidth;
      q((ee) => ee === Q ? ee : Q);
    };
    k();
    const U = new ResizeObserver(k);
    return U.observe(E), () => U.disconnect();
  }, []), e.useEffect(() => {
    N.current || R({});
  }, [c]);
  const m = e.useCallback((E) => D[E.name] ?? E.width, [D]), T = e.useMemo(() => {
    const E = [];
    let k = C && f > 0 ? v : 0;
    for (let U = 0; U < f && U < c.length; U++)
      E.push(k), k += m(c[U]);
    return E;
  }, [c, f, C, v, m]), V = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let E = C ? v : 0;
    for (let k = 0; k < f && k < c.length; k++)
      E += m(c[k]);
    return E;
  }, [c, f, C, v, m]), W = i * r, X = e.useRef(null), j = e.useCallback((E, k, U) => {
    U.preventDefault(), U.stopPropagation(), N.current = { column: E, startX: U.clientX, startWidth: k };
    let Q = U.clientX, ee = 0;
    const ae = () => {
      const se = N.current;
      if (!se) return;
      const de = Math.max(xt, se.startWidth + (Q - se.startX) + ee);
      R((Ee) => ({ ...Ee, [se.column]: de }));
    }, re = () => {
      const se = b.current, de = I.current;
      if (!se || !N.current) return;
      const Ee = se.getBoundingClientRect(), Te = 40, Et = 8, an = se.scrollLeft;
      Q > Ee.right - Te ? se.scrollLeft += Et : Q < Ee.left + Te && (se.scrollLeft = Math.max(0, se.scrollLeft - Et));
      const Ct = se.scrollLeft - an;
      Ct !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Ct, ae()), X.current = requestAnimationFrame(re);
    };
    X.current = requestAnimationFrame(re);
    const fe = (se) => {
      Q = se.clientX, ae();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), X.current !== null && (cancelAnimationFrame(X.current), X.current = null);
      const de = N.current;
      if (de) {
        const Ee = Math.max(xt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ee }), N.current = null, z.current = !0, requestAnimationFrame(() => {
          z.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    I.current && b.current && (I.current.scrollLeft = b.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const E = b.current;
      if (!E) return;
      const k = E.scrollTop, U = Math.ceil(E.clientHeight / r), Q = Math.floor(k / r);
      n("scroll", { start: Q, count: U });
    }, 80);
  }, [n, r]), ce = e.useCallback((E, k, U) => {
    if (z.current) return;
    let Q;
    !k || k === "desc" ? Q = "asc" : Q = "desc";
    const ee = U.shiftKey ? "add" : "replace";
    n("sort", { column: E, direction: Q, mode: ee });
  }, [n]), ne = e.useCallback((E, k) => {
    A.current = E, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", E);
  }, []), _e = e.useCallback((E, k) => {
    if (!A.current || A.current === E) {
      O(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const U = k.currentTarget.getBoundingClientRect(), Q = k.clientX < U.left + U.width / 2 ? "left" : "right";
    O({ column: E, side: Q });
  }, []), ve = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const k = A.current;
    if (!k || !x) {
      A.current = null, O(null);
      return;
    }
    let U = c.findIndex((ee) => ee.name === x.column);
    if (U < 0) {
      A.current = null, O(null);
      return;
    }
    const Q = c.findIndex((ee) => ee.name === k);
    x.side === "right" && U++, Q < U && U--, n("columnReorder", { column: k, targetIndex: U }), A.current = null, O(null);
  }, [c, x, n]), Ce = e.useCallback(() => {
    A.current = null, O(null);
  }, []), ke = e.useCallback((E, k) => {
    var ee, ae, re, fe;
    const U = window.getSelection();
    if (U && !U.isCollapsed && k.currentTarget.contains(U.anchorNode))
      return;
    if (!lt(k) && ((ee = b.current) == null || ee.focus({ preventScroll: !0 }), !k.ctrlKey && !k.metaKey && !k.shiftKey)) {
      const pe = (fe = (re = (ae = k.target) == null ? void 0 : ae.closest) == null ? void 0 : re.call(ae, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      h.current = { index: E, col: pe ?? void 0 };
    }
    const Q = u.find((pe) => pe.index === E);
    lt(k) && (Q != null && Q.selected) && !k.ctrlKey && !k.metaKey && !k.shiftKey || n("select", {
      rowIndex: E,
      ctrlKey: k.ctrlKey || k.metaKey,
      shiftKey: k.shiftKey
    });
  }, [n, u]), Ne = e.useCallback((E, k, U) => {
    n("moveSelection", { direction: E, extend: k, move: U });
  }, [n]), xe = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: C, shiftKey: !1 });
  }, [n, p, C]), He = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), P = e.useCallback(
    () => !!s.current && s.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const E = b.current;
    if (!E)
      return;
    const k = p * r, U = k + r;
    k < E.scrollTop ? E.scrollTop = k : U > E.scrollTop + E.clientHeight && (E.scrollTop = U - E.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const E = h.current, k = b.current;
    if (!E || !k)
      return;
    const U = u.find((ae) => ae.index === E.index);
    if (!U || !at(k, U.id))
      return;
    h.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !k.contains(Q))
      return;
    const ee = at(k, U.id, { col: E.col, last: E.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const Y = e.useCallback((E) => {
    if (E.key !== "Tab")
      return;
    const k = b.current, U = document.activeElement;
    if (!k || !U || !k.contains(U))
      return;
    const Q = U.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, ae = u.find((Te) => Te.id === ee);
    if (!ae)
      return;
    const re = Wt(k, ee).flatMap((Te) => Array.from(Te.querySelectorAll(sa))), fe = re.indexOf(U);
    if (fe < 0)
      return;
    const pe = !E.shiftKey;
    if (!(pe ? fe === re.length - 1 : fe === 0))
      return;
    const de = pe ? ae.index + 1 : ae.index - 1;
    if (de < 0 || de >= i)
      return;
    const Ee = u.find((Te) => Te.index === de);
    Ee && at(k, Ee.id) || (E.preventDefault(), h.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), le = e.useCallback((E, k) => {
    k.stopPropagation(), n("select", { rowIndex: E, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ie = e.useCallback(() => {
    const E = d === i && i > 0;
    n("selectAll", { selected: !E });
  }, [n, d, i]), Fe = e.useCallback((E, k, U) => {
    U.stopPropagation(), n("expand", { rowIndex: E, expanded: k });
  }, [n]), qt = e.useCallback((E, k) => {
    k.preventDefault(), H({ x: k.clientX, y: k.clientY, colIdx: E });
  }, []), Zt = e.useCallback(() => {
    Z && (n("setFrozenColumnCount", { count: Z.colIdx + 1 }), H(null));
  }, [Z, n]), Qt = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), H(null);
  }, [n]), Jt = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const k = L.current, U = I.current;
    if (!k || !U)
      return;
    const Q = k.clientWidth, ee = [{ x: 0, count: 0 }];
    U.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - k.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let ae = { x: V, count: f };
    const re = (pe) => {
      const se = pe.clientX - k.getBoundingClientRect().left;
      ae = ee.reduce(
        (de, Ee) => Math.abs(Ee.x - se) < Math.abs(de.x - se) ? Ee : de,
        ee[0]
      ), B(ae);
    }, fe = () => {
      document.removeEventListener("mousemove", re), document.removeEventListener("mouseup", fe), B(null), ae.count !== f && n("setFrozenColumnCount", { count: ae.count });
    };
    document.addEventListener("mousemove", re), document.addEventListener("mouseup", fe);
  }, [V, f, n]);
  e.useEffect(() => {
    if (!Z) return;
    const E = () => H(null);
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [Z]), Le(!!Z, { ESCAPE: () => H(null) });
  const en = e.useCallback((E, k) => {
    k.stopPropagation(), k.preventDefault(), n("openFilter", { column: E });
  }, [n]), tn = e.useCallback((E) => {
    E.stopPropagation(), E.preventDefault(), n("openColumnSelect", {});
  }, [n]), Qe = c.reduce((E, k) => E + m(k), 0) + (C ? v : 0), Je = _ ? 32 : 0, nn = d === i && i > 0, vt = d > 0 && d < i, ln = e.useCallback((E) => {
    E && (E.indeterminate = vt);
  }, [vt]);
  return /* @__PURE__ */ e.createElement(bt, { active: P }, /* @__PURE__ */ e.createElement(
    aa,
    {
      isMulti: C,
      cursorIndex: p,
      onMove: Ne,
      onToggle: xe,
      onSelectAll: He
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (E) => {
        if (!A.current) return;
        E.preventDefault();
        const k = b.current, U = I.current;
        if (!k) return;
        const Q = k.getBoundingClientRect(), ee = 40, ae = 8;
        E.clientX < Q.left + ee ? k.scrollLeft = Math.max(0, k.scrollLeft - ae) : E.clientX > Q.right - ee && (k.scrollLeft += ae), U && (U.scrollLeft = k.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: L }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: I }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Qe, paddingRight: Je + M }
      },
      C && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: v,
            minWidth: v,
            ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (E) => {
            A.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== A.current && O({ column: c[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: ln,
            className: "tlTableView__checkbox",
            checked: nn,
            onChange: ie
          }
        )
      ),
      c.map((E, k) => {
        const U = m(E);
        c.length - 1;
        let Q = "tlTableView__headerCell";
        E.sortable && (Q += " tlTableView__headerCell--sortable"), x && x.column === E.name && (Q += " tlTableView__headerCell--dragOver-" + x.side);
        const ee = k < f, ae = k === f - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), ae && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: E.name,
            className: Q,
            "data-col-idx": k,
            style: {
              width: U,
              minWidth: U,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: T[k], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: E.sortable ? (re) => ce(E.name, E.sortDirection, re) : void 0,
            onContextMenu: (re) => qt(k, re),
            onDragStart: (re) => ne(E.name, re),
            onDragOver: (re) => _e(E.name, re),
            onDrop: ve,
            onDragEnd: Ce
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
              onMouseDown: (re) => re.stopPropagation(),
              onClick: (re) => en(E.name, re)
            },
            /* @__PURE__ */ e.createElement("i", { className: E.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          E.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, E.sortDirection === "asc" ? "▲" : "▼", S > 1 && E.sortPriority != null && E.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, E.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (re) => j(E.name, U, re)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (E) => {
            if (A.current && c.length > 0) {
              const k = c[c.length - 1];
              k.name !== A.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", O({ column: k.name, side: "right" }));
            }
          },
          onDrop: ve
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (F ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: V },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Jt
      }
    ), _ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: tn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: te,
        onKeyDown: Y,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: Qe, paddingRight: Je } }, u.map((E) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: "tlTableView__row" + (E.selected ? " tlTableView__row--selected" : "") + (E.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: E.index * r,
            height: r,
            width: Qe,
            paddingRight: Je,
            ...E.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (k) => {
            (k.shiftKey || k.ctrlKey || k.metaKey || k.detail > 1) && !lt(k) && k.preventDefault();
          },
          onClick: (k) => ke(E.index, k)
        },
        C && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: v,
              minWidth: v,
              ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (k) => k.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: E.selected,
              onChange: () => {
              },
              onClick: (k) => le(E.index, k),
              tabIndex: -1
            }
          )
        ),
        c.map((k, U) => {
          const Q = m(k), ee = U === c.length - 1, ae = U < f, re = U === f - 1;
          let fe = "tlTableView__cell";
          ae && (fe += " tlTableView__cell--frozen"), re && (fe += " tlTableView__cell--frozenLast");
          const pe = g && U === 0, se = E.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: k.name,
              className: fe,
              "data-row": E.id,
              "data-col": k.name,
              style: {
                ...ee && !ae ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...ae ? { position: "sticky", left: T[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, E.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Fe(E.index, !E.expanded, de)
              },
              E.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), E.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: E.cells[k.name] })) : E.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: E.cells[k.name] })
          );
        })
      )))
    ),
    F && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: F.x } }),
    Z && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: Z.y, left: Z.x, zIndex: 1e4 },
        onMouseDown: (E) => E.stopPropagation()
      },
      Z.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Zt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Qt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ia = {
  "js.table.columnSearch": "Find column"
}, ua = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(ia), s = t.entries ?? [], c = s.filter((b) => b.visible).length, [i, u] = e.useState(""), r = i.trim().toLowerCase(), o = r ? s.filter((b) => b.label.toLowerCase().includes(r)) : s, d = e.useRef(null), p = e.useRef(null), [f, g] = e.useState(null), _ = e.useCallback((b) => {
    p.current = b, g(b);
  }, []), S = e.useCallback((b, w) => {
    n("columnVisible", { column: b, visible: w });
  }, [n]), C = e.useCallback((b, w) => {
    d.current = b, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, []), v = e.useCallback((b, w) => {
    if (!d.current || d.current === b) {
      _(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const h = w.currentTarget.getBoundingClientRect(), D = w.clientY < h.top + h.height / 2 ? "top" : "bottom";
    _({ name: b, side: D });
  }, [_]), y = e.useCallback(() => {
    d.current = null, _(null);
  }, [_]), I = e.useCallback((b) => {
    b.preventDefault();
    const w = d.current, h = p.current;
    if (d.current = null, _(null), !w || !h)
      return;
    const D = s.findIndex((z) => z.name === h.name), R = s.findIndex((z) => z.name === w);
    if (D < 0 || R < 0)
      return;
    let N = h.side === "top" ? D : D + 1;
    R < N && N--, N !== R && n("columnReorder", { column: w, targetIndex: N });
  }, [s, n, _]), L = s.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, L && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: i,
      onChange: (b) => u(b.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, o.map((b) => {
    const w = b.visible && c <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === b.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: b.name,
        className: h,
        draggable: !0,
        onDragStart: (D) => C(b.name, D),
        onDragOver: (D) => v(b.name, D),
        onDrop: I,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: b.visible,
          disabled: w,
          onChange: (D) => S(b.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, b.label))
    );
  })));
}, da = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, zt = e.createContext(da), { useMemo: ma, useRef: pa, useState: fa, useEffect: ha } = e, ba = 320, _a = "TLTableView", ga = "TLPanel", va = ({ controlId: l }) => {
  var C;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", s = t.readOnly === !0, c = t.children ?? [], i = t.noModelMessage, u = pa(null), [r, o] = fa(
    a === "top" ? "top" : "side"
  );
  ha(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const y = new ResizeObserver((I) => {
      for (const L of I) {
        const w = L.contentRect.width / n;
        o(w < ba ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const d = ma(() => ({
    readOnly: s,
    resolvedLabelPosition: r
  }), [s, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = c.length === 1 ? c[0] : void 0, _ = !!g && (g.module === _a || g.module === ga && ((C = g.state) == null ? void 0 : C.bare) === !0), S = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : "",
    _ ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(zt.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: f, ref: u }, c.map((v, y) => /* @__PURE__ */ e.createElement(K, { key: y, control: v }))));
}, { useCallback: Ea } = e, Ca = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, wa = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Ca), s = t.headerControl ?? null, c = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, d = t.children ?? [], p = s != null || c.length > 0 || i, f = Ea(() => {
    n("toggleCollapse");
  }, [n]), g = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, i && /* @__PURE__ */ e.createElement(
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: s })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((_, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: _ })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((_, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: _ }))));
}, { useContext: ya, useState: Sa, useCallback: ka } = e, Na = ({ controlId: l }) => {
  const t = G(), n = ya(zt), a = t.label ?? "", s = t.required === !0, c = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, d = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, g = t.visible !== !1, _ = t.hasTooltip === !0, S = t.field, C = n.readOnly, [v, y] = Sa(!1), I = ka(() => y((D) => !D), []), L = p === "hidden", b = c != null, w = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    C ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    !b && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: g ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": _ ? "key:tooltip" : void 0
    },
    a
  ), s && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !C && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: I,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: S })), !C && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, c)), !C && !b && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !C && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Ta = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.iconCss, s = t.iconSrc, c = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : s ? /* @__PURE__ */ e.createElement("img", { src: s, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, o, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), p = e.useCallback((_) => {
    _.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", i].filter(Boolean).join(" "), g = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: p,
      "data-tooltip": g
    },
    d
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": g }, d);
}, Ra = 20, Da = () => {
  var w;
  const l = G(), t = oe(), n = l.nodes ?? [], a = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), d = e.useRef(null), p = ((w = n.find((h) => h.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var D;
    if (p == null)
      return;
    const h = (D = d.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, D) => {
    t(D ? "collapse" : "expand", { nodeId: h });
  }, [t]), g = e.useCallback((h, D) => {
    var N;
    const R = window.getSelection();
    R && !R.isCollapsed && D.currentTarget.contains(R.anchorNode) || ((N = d.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), _ = e.useCallback((h, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: h, x: D.clientX, y: D.clientY });
  }, [t]), S = e.useRef(null), C = e.useCallback((h, D) => {
    const R = D.getBoundingClientRect(), N = h.clientY - R.top, z = R.height / 3;
    return N < z ? "above" : N > z * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", h);
  }, []), y = e.useCallback((h, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const R = C(D, D.currentTarget);
    S.current != null && window.clearTimeout(S.current), S.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: R }), S.current = null;
    }, 50);
  }, [t, C]), I = e.useCallback((h, D) => {
    D.preventDefault(), S.current != null && (window.clearTimeout(S.current), S.current = null);
    const R = C(D, D.currentTarget);
    t("drop", { nodeId: h, position: R });
  }, [t, C]), L = e.useCallback(() => {
    S.current != null && (window.clearTimeout(S.current), S.current = null), t("dragEnd");
  }, [t]), b = e.useCallback((h) => {
    if (n.length === 0) return;
    let D = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), D = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), D = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const R = n[r];
          if (R.expandable && !R.expanded) {
            t("expand", { nodeId: R.id });
            return;
          } else R.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const R = n[r];
          if (R.expanded) {
            t("collapse", { nodeId: R.id });
            return;
          } else {
            const N = R.depth;
            for (let z = r - 1; z >= 0; z--)
              if (n[z].depth < N) {
                D = z;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), a === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), D = 0;
        break;
      case "End":
        h.preventDefault(), D = n.length - 1;
        break;
      default:
        return;
    }
    D !== r && o(D);
  }, [r, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: d,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: b
    },
    n.map((h, D) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: h.id,
        role: "treeitem",
        "aria-expanded": h.expandable ? h.expanded : void 0,
        "aria-selected": h.selected,
        "aria-level": h.depth + 1,
        className: [
          "tlTreeView__node",
          h.selected ? "tlTreeView__node--selected" : "",
          D === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * Ra },
        draggable: s,
        onMouseDown: (R) => {
          (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && R.preventDefault();
        },
        onClick: (R) => g(h.id, R),
        onContextMenu: (R) => _(h.id, R),
        onDragStart: (R) => v(h.id, R),
        onDragOver: c ? (R) => y(h.id, R) : void 0,
        onDrop: c ? (R) => I(h.id, R) : void 0,
        onDragEnd: L
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (R) => {
            R.stopPropagation(), f(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: h.content }))
    ))
  );
};
var rt = { exports: {} }, he = {}, ot = { exports: {} }, J = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var It;
function La() {
  if (It) return J;
  It = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), s = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function g(m) {
    return m === null || typeof m != "object" ? null : (m = f && m[f] || m["@@iterator"], typeof m == "function" ? m : null);
  }
  var _ = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, S = Object.assign, C = {};
  function v(m, T, V) {
    this.props = m, this.context = T, this.refs = C, this.updater = V || _;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(m, T) {
    if (typeof m != "object" && typeof m != "function" && m != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, m, T, "setState");
  }, v.prototype.forceUpdate = function(m) {
    this.updater.enqueueForceUpdate(this, m, "forceUpdate");
  };
  function y() {
  }
  y.prototype = v.prototype;
  function I(m, T, V) {
    this.props = m, this.context = T, this.refs = C, this.updater = V || _;
  }
  var L = I.prototype = new y();
  L.constructor = I, S(L, v.prototype), L.isPureReactComponent = !0;
  var b = Array.isArray;
  function w() {
  }
  var h = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function R(m, T, V) {
    var W = V.ref;
    return {
      $$typeof: l,
      type: m,
      key: T,
      ref: W !== void 0 ? W : null,
      props: V
    };
  }
  function N(m, T) {
    return R(m.type, T, m.props);
  }
  function z(m) {
    return typeof m == "object" && m !== null && m.$$typeof === l;
  }
  function A(m) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + m.replace(/[=:]/g, function(V) {
      return T[V];
    });
  }
  var x = /\/+/g;
  function O(m, T) {
    return typeof m == "object" && m !== null && m.key != null ? A("" + m.key) : T.toString(36);
  }
  function Z(m) {
    switch (m.status) {
      case "fulfilled":
        return m.value;
      case "rejected":
        throw m.reason;
      default:
        switch (typeof m.status == "string" ? m.then(w, w) : (m.status = "pending", m.then(
          function(T) {
            m.status === "pending" && (m.status = "fulfilled", m.value = T);
          },
          function(T) {
            m.status === "pending" && (m.status = "rejected", m.reason = T);
          }
        )), m.status) {
          case "fulfilled":
            return m.value;
          case "rejected":
            throw m.reason;
        }
    }
    throw m;
  }
  function H(m, T, V, W, X) {
    var j = typeof m;
    (j === "undefined" || j === "boolean") && (m = null);
    var te = !1;
    if (m === null) te = !0;
    else
      switch (j) {
        case "bigint":
        case "string":
        case "number":
          te = !0;
          break;
        case "object":
          switch (m.$$typeof) {
            case l:
            case t:
              te = !0;
              break;
            case d:
              return te = m._init, H(
                te(m._payload),
                T,
                V,
                W,
                X
              );
          }
      }
    if (te)
      return X = X(m), te = W === "" ? "." + O(m, 0) : W, b(X) ? (V = "", te != null && (V = te.replace(x, "$&/") + "/"), H(X, T, V, "", function(_e) {
        return _e;
      })) : X != null && (z(X) && (X = N(
        X,
        V + (X.key == null || m && m.key === X.key ? "" : ("" + X.key).replace(
          x,
          "$&/"
        ) + "/") + te
      )), T.push(X)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (b(m))
      for (var ne = 0; ne < m.length; ne++)
        W = m[ne], j = ce + O(W, ne), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (ne = g(m), typeof ne == "function")
      for (m = ne.call(m), ne = 0; !(W = m.next()).done; )
        W = W.value, j = ce + O(W, ne++), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (j === "object") {
      if (typeof m.then == "function")
        return H(
          Z(m),
          T,
          V,
          W,
          X
        );
      throw T = String(m), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(m).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function F(m, T, V) {
    if (m == null) return m;
    var W = [], X = 0;
    return H(m, W, "", "", function(j) {
      return T.call(V, j, X++);
    }), W;
  }
  function B(m) {
    if (m._status === -1) {
      var T = m._result;
      T = T(), T.then(
        function(V) {
          (m._status === 0 || m._status === -1) && (m._status = 1, m._result = V);
        },
        function(V) {
          (m._status === 0 || m._status === -1) && (m._status = 2, m._result = V);
        }
      ), m._status === -1 && (m._status = 0, m._result = T);
    }
    if (m._status === 1) return m._result.default;
    throw m._result;
  }
  var M = typeof reportError == "function" ? reportError : function(m) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var T = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof m == "object" && m !== null && typeof m.message == "string" ? String(m.message) : String(m),
        error: m
      });
      if (!window.dispatchEvent(T)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", m);
      return;
    }
    console.error(m);
  }, q = {
    map: F,
    forEach: function(m, T, V) {
      F(
        m,
        function() {
          T.apply(this, arguments);
        },
        V
      );
    },
    count: function(m) {
      var T = 0;
      return F(m, function() {
        T++;
      }), T;
    },
    toArray: function(m) {
      return F(m, function(T) {
        return T;
      }) || [];
    },
    only: function(m) {
      if (!z(m))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return m;
    }
  };
  return J.Activity = p, J.Children = q, J.Component = v, J.Fragment = n, J.Profiler = s, J.PureComponent = I, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(m) {
      return h.H.useMemoCache(m);
    }
  }, J.cache = function(m) {
    return function() {
      return m.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(m, T, V) {
    if (m == null)
      throw Error(
        "The argument must be a React element, but you passed " + m + "."
      );
    var W = S({}, m.props), X = m.key;
    if (T != null)
      for (j in T.key !== void 0 && (X = "" + T.key), T)
        !D.call(T, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && T.ref === void 0 || (W[j] = T[j]);
    var j = arguments.length - 2;
    if (j === 1) W.children = V;
    else if (1 < j) {
      for (var te = Array(j), ce = 0; ce < j; ce++)
        te[ce] = arguments[ce + 2];
      W.children = te;
    }
    return R(m.type, X, W);
  }, J.createContext = function(m) {
    return m = {
      $$typeof: i,
      _currentValue: m,
      _currentValue2: m,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, m.Provider = m, m.Consumer = {
      $$typeof: c,
      _context: m
    }, m;
  }, J.createElement = function(m, T, V) {
    var W, X = {}, j = null;
    if (T != null)
      for (W in T.key !== void 0 && (j = "" + T.key), T)
        D.call(T, W) && W !== "key" && W !== "__self" && W !== "__source" && (X[W] = T[W]);
    var te = arguments.length - 2;
    if (te === 1) X.children = V;
    else if (1 < te) {
      for (var ce = Array(te), ne = 0; ne < te; ne++)
        ce[ne] = arguments[ne + 2];
      X.children = ce;
    }
    if (m && m.defaultProps)
      for (W in te = m.defaultProps, te)
        X[W] === void 0 && (X[W] = te[W]);
    return R(m, j, X);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(m) {
    return { $$typeof: u, render: m };
  }, J.isValidElement = z, J.lazy = function(m) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: m },
      _init: B
    };
  }, J.memo = function(m, T) {
    return {
      $$typeof: o,
      type: m,
      compare: T === void 0 ? null : T
    };
  }, J.startTransition = function(m) {
    var T = h.T, V = {};
    h.T = V;
    try {
      var W = m(), X = h.S;
      X !== null && X(V, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(w, M);
    } catch (j) {
      M(j);
    } finally {
      T !== null && V.types !== null && (T.types = V.types), h.T = T;
    }
  }, J.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, J.use = function(m) {
    return h.H.use(m);
  }, J.useActionState = function(m, T, V) {
    return h.H.useActionState(m, T, V);
  }, J.useCallback = function(m, T) {
    return h.H.useCallback(m, T);
  }, J.useContext = function(m) {
    return h.H.useContext(m);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(m, T) {
    return h.H.useDeferredValue(m, T);
  }, J.useEffect = function(m, T) {
    return h.H.useEffect(m, T);
  }, J.useEffectEvent = function(m) {
    return h.H.useEffectEvent(m);
  }, J.useId = function() {
    return h.H.useId();
  }, J.useImperativeHandle = function(m, T, V) {
    return h.H.useImperativeHandle(m, T, V);
  }, J.useInsertionEffect = function(m, T) {
    return h.H.useInsertionEffect(m, T);
  }, J.useLayoutEffect = function(m, T) {
    return h.H.useLayoutEffect(m, T);
  }, J.useMemo = function(m, T) {
    return h.H.useMemo(m, T);
  }, J.useOptimistic = function(m, T) {
    return h.H.useOptimistic(m, T);
  }, J.useReducer = function(m, T, V) {
    return h.H.useReducer(m, T, V);
  }, J.useRef = function(m) {
    return h.H.useRef(m);
  }, J.useState = function(m) {
    return h.H.useState(m);
  }, J.useSyncExternalStore = function(m, T, V) {
    return h.H.useSyncExternalStore(
      m,
      T,
      V
    );
  }, J.useTransition = function() {
    return h.H.useTransition();
  }, J.version = "19.2.4", J;
}
var Pt;
function xa() {
  return Pt || (Pt = 1, ot.exports = La()), ot.exports;
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
var Mt;
function Ia() {
  if (Mt) return he;
  Mt = 1;
  var l = xa();
  function t(r) {
    var o = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var d = 2; d < arguments.length; d++)
        o += "&args[]=" + encodeURIComponent(arguments[d]);
    }
    return "Minified React error #" + r + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  }, s = Symbol.for("react.portal");
  function c(r, o, d) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: s,
      key: p == null ? null : "" + p,
      children: r,
      containerInfo: o,
      implementation: d
    };
  }
  var i = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, o) {
    if (r === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(r, o) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return c(r, o, null, d);
  }, he.flushSync = function(r) {
    var o = i.T, d = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = d, a.d.f();
    }
  }, he.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, he.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, he.preinit = function(r, o) {
    if (typeof r == "string" && o && typeof o.as == "string") {
      var d = o.as, p = u(d, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, g = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      d === "style" ? a.d.S(
        r,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: g
        }
      ) : d === "script" && a.d.X(r, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: g,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, he.preinitModule = function(r, o) {
    if (typeof r == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var d = u(
            o.as,
            o.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: d,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && a.d.M(r);
  }, he.preload = function(r, o) {
    if (typeof r == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var d = o.as, p = u(d, o.crossOrigin);
      a.d.L(r, d, {
        crossOrigin: p,
        integrity: typeof o.integrity == "string" ? o.integrity : void 0,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0,
        type: typeof o.type == "string" ? o.type : void 0,
        fetchPriority: typeof o.fetchPriority == "string" ? o.fetchPriority : void 0,
        referrerPolicy: typeof o.referrerPolicy == "string" ? o.referrerPolicy : void 0,
        imageSrcSet: typeof o.imageSrcSet == "string" ? o.imageSrcSet : void 0,
        imageSizes: typeof o.imageSizes == "string" ? o.imageSizes : void 0,
        media: typeof o.media == "string" ? o.media : void 0
      });
    }
  }, he.preloadModule = function(r, o) {
    if (typeof r == "string")
      if (o) {
        var d = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: d,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, he.requestFormReset = function(r) {
    a.d.r(r);
  }, he.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, he.useFormState = function(r, o, d) {
    return i.H.useFormState(r, o, d);
  }, he.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var jt;
function Pa() {
  if (jt) return rt.exports;
  jt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), rt.exports = Ia(), rt.exports;
}
var Vt = Pa();
const { useState: Re, useCallback: be, useRef: $e, useEffect: Pe, useMemo: ft } = e;
function gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Ma({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: s,
  onDragStart: c,
  onDragOver: i,
  onDrop: u,
  onDragEnd: r,
  dragClassName: o
}) {
  const d = be(
    (p) => {
      p.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: s || void 0,
      onDragStart: c,
      onDragOver: i,
      onDrop: u,
      onDragEnd: r
    },
    s && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(gt, { image: l.image }),
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
function ja({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: s,
  id: c
}) {
  const i = be(() => a(l.value), [a, l.value]), u = ft(() => {
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
      onClick: i,
      onMouseEnter: s
    },
    /* @__PURE__ */ e.createElement(gt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Aa = ({ controlId: l, state: t }) => {
  const n = oe(), a = t.value ?? [], s = t.multiSelect === !0, c = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, d = t.options ?? [], p = t.emptyOptionLabel ?? "", f = c && s && !u && r, g = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), _ = g["js.dropdownSelect.nothingFound"], S = be(
    (P) => g["js.dropdownSelect.removeChip"].replace("{0}", P),
    [g]
  ), [C, v] = Re(!1), [y, I] = Re(""), [L, b] = Re(-1), [w, h] = Re(!1), [D, R] = Re({}), [N, z] = Re(null), [A, x] = Re(null), [O, Z] = Re(null), H = $e(null), F = $e(null), B = $e(null), M = $e(a);
  M.current = a;
  const q = $e(-1), m = ft(
    () => new Set(a.map((P) => P.value)),
    [a]
  ), T = ft(() => {
    let P = d.filter((Y) => !m.has(Y.value));
    if (y) {
      const Y = y.toLowerCase();
      P = P.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return P;
  }, [d, m, y]);
  Pe(() => {
    y && T.length === 1 ? b(0) : b(-1);
  }, [T.length, y]), Pe(() => {
    C && o && F.current && F.current.focus();
  }, [C, o, a]), Pe(() => {
    var le, ie;
    if (q.current < 0) return;
    const P = q.current;
    q.current = -1;
    const Y = (le = H.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(P, Y.length - 1)].focus() : (ie = H.current) == null || ie.focus();
  }, [a]), Pe(() => {
    if (!C) return;
    const P = (Y) => {
      H.current && !H.current.contains(Y.target) && B.current && !B.current.contains(Y.target) && (v(!1), I(""));
    };
    return document.addEventListener("mousedown", P), () => document.removeEventListener("mousedown", P);
  }, [C]), Pe(() => {
    if (!C || !H.current) return;
    const P = H.current.getBoundingClientRect(), Y = window.innerHeight - P.bottom, ie = Y < 300 && P.top > Y;
    R({
      left: P.left,
      width: P.width,
      ...ie ? { bottom: window.innerHeight - P.top } : { top: P.bottom }
    });
  }, [C]);
  const V = be(async () => {
    if (!(u || !r) && (v(!0), I(""), b(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = be(() => {
    var P;
    v(!1), I(""), b(-1), (P = H.current) == null || P.focus();
  }, []), X = be(
    (P) => {
      let Y;
      if (s) {
        const le = d.find((ie) => ie.value === P);
        if (le)
          Y = [...M.current, le];
        else
          return;
      } else {
        const le = d.find((ie) => ie.value === P);
        if (le)
          Y = [le];
        else
          return;
      }
      M.current = Y, n(We, { value: Y.map((le) => le.value) }), s ? (I(""), b(-1)) : W();
    },
    [s, d, n, W]
  ), j = be(
    (P) => {
      q.current = M.current.findIndex((le) => le.value === P);
      const Y = M.current.filter((le) => le.value !== P);
      M.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), te = be(
    (P) => {
      P.stopPropagation(), n(We, { value: [] }), W();
    },
    [n, W]
  ), ce = be((P) => {
    I(P.target.value);
  }, []), ne = be(
    (P) => {
      if (!C) {
        if (P.key === "ArrowDown" || P.key === "ArrowUp" || P.key === "Enter" || P.key === " ") {
          if (P.target.tagName === "BUTTON") return;
          P.preventDefault(), P.stopPropagation(), V();
        }
        return;
      }
      switch (P.key) {
        case "ArrowDown":
          P.preventDefault(), P.stopPropagation(), b(
            (Y) => Y < T.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          P.preventDefault(), P.stopPropagation(), b(
            (Y) => Y > 0 ? Y - 1 : T.length - 1
          );
          break;
        case "Enter":
          P.preventDefault(), P.stopPropagation(), L >= 0 && L < T.length && X(T[L].value);
          break;
        case "Escape":
          P.preventDefault(), P.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          y === "" && s && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      V,
      W,
      T,
      L,
      X,
      y,
      s,
      a,
      j
    ]
  ), _e = be(
    async (P) => {
      P.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), ve = be(
    (P, Y) => {
      z(P), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(P));
    },
    []
  ), Ce = be(
    (P, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", N === null || N === P) {
        x(null), Z(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Fe = Y.clientX < ie ? "before" : "after";
      x(P), Z(Fe);
    },
    [N]
  ), ke = be(
    (P) => {
      if (P.preventDefault(), N === null || A === null || O === null || N === A) return;
      const Y = [...M.current], [le] = Y.splice(N, 1);
      let ie = A;
      N < A ? ie = O === "before" ? ie - 1 : ie : ie = O === "before" ? ie : ie + 1, Y.splice(ie, 0, le), M.current = Y, n(We, { value: Y.map((Fe) => Fe.value) }), z(null), x(null), Z(null);
    },
    [N, A, O, n]
  ), Ne = be(() => {
    z(null), x(null), Z(null);
  }, []);
  if (Pe(() => {
    if (L < 0 || !B.current) return;
    const P = B.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    P && P.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((P) => /* @__PURE__ */ e.createElement("span", { key: P.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(gt, { image: P.image }), /* @__PURE__ */ e.createElement("span", null, P.label))));
  const xe = !i && a.length > 0 && !u, He = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...on
    },
    (o || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: F,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ce,
        onKeyDown: ne,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
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
      !o && !w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: _e }, g["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, _),
      o && T.map((P, Y) => /* @__PURE__ */ e.createElement(
        ja,
        {
          key: P.value,
          id: `${l}-opt-${Y}`,
          option: P,
          highlighted: Y === L,
          searchTerm: y,
          onSelect: X,
          onMouseEnter: () => b(Y)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: H,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: C ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((P, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : A === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : A === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ma,
        {
          key: P.value,
          option: P,
          removable: !u && (s || !i),
          onRemove: j,
          removeLabel: S(P.label),
          draggable: f,
          onDragStart: f ? (ie) => ve(Y, ie) : void 0,
          onDragOver: f ? (ie) => Ce(Y, ie) : void 0,
          onDrop: f ? ke : void 0,
          onDragEnd: f ? Ne : void 0,
          dragClassName: f ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, xe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), He && Vt.createPortal(He, document.body));
}, { useCallback: st, useRef: Ba } = e, Kt = "application/x-tl-color", Oa = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: s,
  onReplace: c
}) => {
  const i = Ba(null), u = st(
    (d) => (p) => {
      i.current = d, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = st((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), o = st(
    (d) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Kt);
      f ? c(d, f) : i.current !== null && i.current !== d && s(i.current, d), i.current = null;
    },
    [s, c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((d, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (d == null ? " tlColorInput__paletteCell--empty" : ""),
        style: d != null ? { backgroundColor: d } : void 0,
        title: d ?? "",
        draggable: d != null,
        onClick: d != null ? () => n(d) : void 0,
        onDoubleClick: d != null ? () => a(d) : void 0,
        onDragStart: d != null ? u(p) : void 0,
        onDragOver: r,
        onDrop: o(p)
      }
    ))
  );
};
function Yt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ht(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Gt(l) {
  if (!ht(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Xt(l, t, n) {
  const a = (s) => Yt(s).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Fa(l, t, n) {
  const a = l / 255, s = t / 255, c = n / 255, i = Math.max(a, s, c), u = Math.min(a, s, c), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (s - c) / r % 6 : i === s ? o = (c - a) / r + 2 : o = (a - s) / r + 4, o *= 60, o < 0 && (o += 360));
  const d = i === 0 ? 0 : r / i;
  return [o, d, i];
}
function $a(l, t, n) {
  const a = n * t, s = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = s, r = 0) : l < 120 ? (i = s, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = s) : l < 240 ? (i = 0, u = s, r = a) : l < 300 ? (i = s, u = 0, r = a) : (i = a, u = 0, r = s), [
    Math.round((i + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function Ua(l) {
  return Fa(...Gt(l));
}
function ct(l, t, n) {
  return Xt(...$a(l, t, n));
}
const { useCallback: Me, useRef: At } = e, Ha = ({ color: l, onColorChange: t }) => {
  const [n, a, s] = Ua(l), c = At(null), i = At(null), u = Me(
    (_, S) => {
      var I;
      const C = (I = c.current) == null ? void 0 : I.getBoundingClientRect();
      if (!C) return;
      const v = Math.max(0, Math.min(1, (_ - C.left) / C.width)), y = Math.max(0, Math.min(1, 1 - (S - C.top) / C.height));
      t(ct(n, v, y));
    },
    [n, t]
  ), r = Me(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), u(_.clientX, _.clientY);
    },
    [u]
  ), o = Me(
    (_) => {
      _.buttons !== 0 && u(_.clientX, _.clientY);
    },
    [u]
  ), d = Me(
    (_) => {
      var y;
      const S = (y = i.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const v = Math.max(0, Math.min(1, (_ - S.top) / S.height)) * 360;
      t(ct(v, a, s));
    },
    [a, s, t]
  ), p = Me(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), d(_.clientY);
    },
    [d]
  ), f = Me(
    (_) => {
      _.buttons !== 0 && d(_.clientY);
    },
    [d]
  ), g = ct(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
      onPointerDown: r,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - s) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
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
function Wa(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const za = {
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
}, { useState: Ke, useCallback: we, useEffect: Bt, useRef: Va, useLayoutEffect: Ka } = e, Ya = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: s,
  canReset: c,
  onConfirm: i,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [o, d] = Ke("palette"), [p, f] = Ke(t), g = Va(null), _ = ue(za), [S, C] = Ke(null);
  Ka(() => {
    if (!l.current || !g.current) return;
    const B = l.current.getBoundingClientRect(), M = g.current.getBoundingClientRect();
    let q = B.bottom + 4, m = B.left;
    q + M.height > window.innerHeight && (q = B.top - M.height - 4), m + M.width > window.innerWidth && (m = Math.max(0, B.right - M.width)), C({ top: q, left: m });
  }, [l]);
  const v = p != null, [y, I, L] = v ? Gt(p) : [0, 0, 0], [b, w] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  Bt(() => {
    w((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), Bt(() => {
    const B = (q) => {
      g.current && !g.current.contains(q.target) && u();
    }, M = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(M), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const h = we(
    (B) => (M) => {
      const q = parseInt(M.target.value, 10);
      if (isNaN(q)) return;
      const m = Yt(q);
      f(Xt(B === "r" ? m : y, B === "g" ? m : I, B === "b" ? m : L));
    },
    [y, I, L]
  ), D = we(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(Kt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const M = document.createElement("div");
        M.style.width = "33px", M.style.height = "33px", M.style.backgroundColor = p, M.style.borderRadius = "3px", M.style.border = "1px solid rgba(0,0,0,0.1)", M.style.position = "absolute", M.style.top = "-9999px", document.body.appendChild(M), B.dataTransfer.setDragImage(M, 16, 16), requestAnimationFrame(() => document.body.removeChild(M));
      }
    },
    [p]
  ), R = we((B) => {
    const M = B.target.value;
    w(M), ht(M) && f(M);
  }, []), N = we(() => {
    f(null);
  }, []), z = we((B) => {
    f(B);
  }, []), A = we(
    (B) => {
      i(B);
    },
    [i]
  ), x = we(
    (B, M) => {
      const q = [...n], m = q[B];
      q[B] = q[M], q[M] = m, r(q);
    },
    [n, r]
  ), O = we(
    (B, M) => {
      const q = [...n];
      q[B] = M, r(q);
    },
    [n, r]
  ), Z = we(() => {
    r([...s]);
  }, [s, r]), H = we(
    (B) => {
      if (Wa(n, B)) return;
      const M = n.indexOf(null);
      if (M < 0) return;
      const q = [...n];
      q[M] = B.toUpperCase(), r(q);
    },
    [n, r]
  ), F = we(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: g,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("palette")
      },
      _["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      _["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Oa,
      {
        colors: n,
        columns: a,
        onSelect: z,
        onConfirm: A,
        onSwap: x,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Z }, _["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Ha, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: p } : void 0,
        draggable: v,
        onDragStart: v ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? I : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !ht(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, _["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, _["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: F }, _["js.colorInput.ok"]))
  );
}, Ga = { "js.colorInput.chooseColor": "Choose color" }, { useState: Xa, useCallback: Ye, useRef: qa } = e, Za = ({ controlId: l, state: t }) => {
  const [n, a] = Se(), s = oe(), c = ue(Ga), [i, u] = Xa(!1), r = qa(null), o = n, d = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, g = t.defaultPalette ?? p, _ = Ye(() => {
    d && u(!0);
  }, [d]), S = Ye(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), C = Ye(() => {
    u(!1);
  }, []), v = Ye(
    (y) => {
      s("paletteChanged", { palette: y });
    },
    [s]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": c["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Ya,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: g,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: C,
      onPaletteChange: v
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (o == null ? " tlColorInput--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      title: o ?? ""
    }
  );
}, { useState: Ue, useCallback: De, useEffect: it, useRef: Ot, useLayoutEffect: Qa, useMemo: Ja } = e, er = {
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
}, tr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: s,
  onCancel: c,
  onLoadIcons: i
}) => {
  const u = ue(er), [r, o] = Ue("simple"), [d, p] = Ue(""), [f, g] = Ue(t ?? ""), [_, S] = Ue(!1), [C, v] = Ue(null), y = Ot(null), I = Ot(null);
  Qa(() => {
    if (!l.current || !y.current) return;
    const A = l.current.getBoundingClientRect(), x = y.current.getBoundingClientRect();
    let O = A.bottom + 4, Z = A.left;
    O + x.height > window.innerHeight && (O = A.top - x.height - 4), Z + x.width > window.innerWidth && (Z = Math.max(0, A.right - x.width)), v({ top: O, left: Z });
  }, [l]), it(() => {
    !a && !_ && i().catch(() => S(!0));
  }, [a, _, i]), it(() => {
    a && I.current && I.current.focus();
  }, [a]), Le(!0, { ESCAPE: c }), it(() => {
    const A = (O) => {
      y.current && !y.current.contains(O.target) && c();
    }, x = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(x), document.removeEventListener("mousedown", A);
    };
  }, [c]);
  const L = Ja(() => {
    if (!d) return n;
    const A = d.toLowerCase();
    return n.filter(
      (x) => x.prefix.toLowerCase().includes(A) || x.label.toLowerCase().includes(A) || x.terms != null && x.terms.some((O) => O.includes(A))
    );
  }, [n, d]), b = De((A) => {
    p(A.target.value);
  }, []), w = De(
    (A) => {
      s(A);
    },
    [s]
  ), h = De((A) => {
    g(A);
  }, []), D = De((A) => {
    g(A.target.value);
  }, []), R = De(() => {
    s(f || null);
  }, [f, s]), N = De(() => {
    s(null);
  }, [s]), z = De(async (A) => {
    A.preventDefault(), S(!1);
    try {
      await i();
    } catch {
      S(!0);
    }
  }, [i]);
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
        onClick: () => o("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: I,
        type: "text",
        className: "tlIconSelect__search",
        value: d,
        onChange: b,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), d && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => p(""),
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
      !a && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: z }, u["js.iconSelect.loadError"])),
      a && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && L.map(
        (A) => A.variants.map((x) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: x.encoded,
            className: "tlIconSelect__iconCell" + (x.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": x.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => r === "simple" ? w(x.encoded) : h(x.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? w(x.encoded) : h(x.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ye, { encoded: x.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ye, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, nr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: lr, useCallback: Ge, useRef: ar } = e, rr = ({ controlId: l, state: t }) => {
  const [n, a] = Se(), s = oe(), c = ue(nr), [i, u] = lr(!1), r = ar(null), o = n, d = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], g = t.iconsLoaded === !0, _ = Ge(() => {
    d && !p && u(!0);
  }, [d, p]), S = Ge(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), C = Ge(() => {
    u(!1);
  }, []), v = Ge(async () => {
    await s("loadIcons");
  }, [s]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: p,
      title: o ?? "",
      "aria-label": c["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    tr,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: g,
      onSelect: S,
      onCancel: C,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: je, useEffect: or, useMemo: Ft, useRef: sr, useState: ut } = e, cr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, ir = [1, 2, 3, 4];
function ur(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), s = n[2] || "px";
  return s === "rem" || s === "em" ? a * t : a;
}
function dr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const s of ir)
    n >= s && (a = s);
  return a;
}
function mr(l, t) {
  const n = cr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function pr(l, t) {
  const n = Math.max(1, t), a = {}, s = (p, f) => !!(a[p] && a[p][f]), c = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const _ of i) _.rowStart === p && (f = _);
    if (!f) return;
    let g = f.colEnd;
    for (; g < n && !s(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let _ = f.rowStart; _ < f.rowEnd; _++)
        for (let S = f.colEnd; S < g; S++) c(_, S);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(mr(p.width, n), n);
    for (; s(u, r); )
      r++, r >= n && (r = 0, u++);
    let _ = 0;
    for (let I = r; I < n && !s(u, I); I++)
      _++;
    if (g > _) {
      for (o(u), r = 0, u++; s(u, r); )
        r++, r >= n && (r = 0, u++);
      _ = 0;
      for (let I = r; I < n && !s(u, I); I++)
        _++;
      g = Math.min(g, _);
    }
    const S = r, C = r + g, v = u, y = u + f;
    i.push({ id: p.id, colStart: S, colEnd: C, rowStart: v, rowEnd: y });
    for (let I = v; I < y; I++)
      for (let L = S; L < C; L++) c(I, L);
    r = C, r >= n && (r = 0, u++);
  }
  o(u);
  let d = 0;
  for (const p of i) p.rowEnd > d && (d = p.rowEnd);
  for (let p = 1; p < d; p++)
    for (let f = 0; f < n; f++) {
      if (s(p, f)) continue;
      const g = i.find((_) => _.rowEnd === p && _.colStart <= f && f < _.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let _ = g.colStart; _ < g.colEnd; _++) c(p, _);
      }
    }
  return i;
}
const fr = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.minColWidth ?? "16rem", s = (t.children ?? []).filter((w) => w && w.id), c = sr(null), [i, u] = ut(1), r = t.editMode === !0;
  or(() => {
    const w = c.current;
    if (!w) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = ur(a, h), R = () => u(dr(w.clientWidth, D));
    R();
    const N = new ResizeObserver(R);
    return N.observe(w), () => N.disconnect();
  }, [a]);
  const o = Ft(() => pr(s, i), [s, i]), d = Ft(() => {
    const w = {};
    for (const h of o) w[h.id] = h;
    return w;
  }, [o]), [p, f] = ut(null), [g, _] = ut(null), S = je((w, h) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    f(h), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", h);
  }, [r]), C = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    _((N) => N && N.id === h && N.before === R ? N : { id: h, before: R });
  }, [r, p]), v = je(() => {
  }, []), y = je((w, h, D) => {
    const R = s.map((x) => x.id), N = R.indexOf(w);
    if (N < 0) return;
    R.splice(N, 1);
    const z = R.indexOf(h);
    if (z < 0) {
      R.splice(N, 0, w);
      return;
    }
    const A = D ? z : z + 1;
    R.splice(A, 0, w), n("reorder", { order: R });
  }, [s, n]), I = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault();
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    y(p, h, R), f(null), _(null);
  }, [r, p, y]), L = je(() => {
    f(null), _(null);
  }, []), b = {
    display: "grid",
    gridTemplateColumns: `repeat(${i}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: c,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, s.map((w) => {
      const h = d[w.id];
      if (!h) return null;
      const D = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, R = ["tlDashboard__tile"];
      return p === w.id && R.push("tlDashboard__tile--dragging"), g && g.id === w.id && R.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: R.join(" "),
          style: D,
          draggable: r,
          onDragStart: (N) => S(N, w.id),
          onDragOver: (N) => C(N, w.id),
          onDragLeave: v,
          onDrop: (N) => I(N, w.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: hr, useRef: $t, useState: Ut, useEffect: br, useLayoutEffect: _r } = e, gr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, vr = ({ group: l }) => {
  var p, f;
  const [t, n] = Ut(!1), [a, s] = Ut({}), c = $t(null), i = $t(null), u = hr(() => {
    n((g) => !g);
  }, []);
  _r(() => {
    if (!t) return;
    const g = () => {
      const _ = c.current;
      if (!_) return;
      const S = _.getBoundingClientRect();
      s({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), br(() => {
    if (!t) return;
    const g = (_) => {
      i.current && !i.current.contains(_.target) && c.current && !c.current.contains(_.target) && n(!1);
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [t]), Le(t, { ESCAPE: () => n(!1) }), _t(t, i, "first");
  const r = l.items.filter((g) => g != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: r[0] })));
  const o = l.label ?? l.name, d = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      type: "button",
      className: "tlToolbar__menuTrigger" + (d ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (g) => g.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": d ? o : void 0,
      title: d ? o : void 0
    },
    d ? /* @__PURE__ */ e.createElement(ye, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Vt.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: i,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? a : void 0,
        onClick: () => n(!1)
      },
      r.map((g, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: g }))),
      (f = l.subGroups) == null ? void 0 : f.map((g, _) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${_}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((S, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: S })))))
    ),
    document.body
  ));
}, Er = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((s) => s.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((s, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(vr, { group: s }) : /* @__PURE__ */ e.createElement(gr, { group: s }))));
}, Cr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, wr = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.content, s = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, s && s.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, s.map((c, i) => {
    const u = i === s.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: c.depth }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, c.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: c.depth })
      },
      c.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, yr = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: a })));
}, Sr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), kr = {
  "js.sidebar.openDrawer": "Open navigation"
}, Nr = ({ controlId: l }) => {
  const t = oe(), n = ue(kr);
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
$("TLButton", Nn);
$("TLUploadButton", Tn);
$("TLToggleButton", Dn);
$("TLTextInput", un);
$("TLPasswordInput", mn);
$("TLNumberInput", fn);
$("TLDatePicker", bn);
$("TLSelect", gn);
$("TLBooleanChoice", En);
$("TLCheckbox", Sn);
$("TLCounter", Ln);
$("TLTabBar", In);
$("TLFieldList", Pn);
$("TLAudioRecorder", jn);
$("TLAudioPlayer", Bn);
$("TLFileUpload", Fn);
$("TLBinaryField", Un);
$("TLFileChips", zn);
$("TLRelativeTime", Yn);
$("TLAnchor", Gn);
$("TLScrollLink", Xn);
$("TLAvatar", Qn);
$("TLDownload", el);
$("TLPhotoCapture", nl);
$("TLPhotoViewer", al);
$("TLPdfViewer", ol);
$("TLSplitPanel", sl);
$("TLPanel", fl);
$("TLInset", Nl);
$("TLMaximizeRoot", hl);
$("TLDeckPane", bl);
$("TLSidebar", Sl);
$("TLStack", kl);
$("TLGrid", Tl);
$("TLCard", Rl);
$("TLAppBar", Dl);
$("TLBreadcrumb", xl);
$("TLBottomBar", Pl);
$("TLDialog", Al);
$("TLDialogManager", Fl);
$("TLWindow", Wl);
$("TLDrawer", Kl);
$("TLContextMenuRegion", Gl);
$("TLSnackbar", Ql);
$("TLMenu", ea);
$("TLAppShell", na);
$("TLText", la);
$("TLTableView", ca);
$("TLColumnSelect", ua);
$("TLFormLayout", va);
$("TLFormGroup", wa);
$("TLFormField", Na);
$("TLResourceCell", Ta);
$("TLTreeView", Da);
$("TLDropdownSelect", Aa);
$("TLColorInput", Za);
$("TLIconSelect", rr);
$("TLDashboard", fr);
$("TLToolbar", Er);
$("TLTileStack", Cr);
$("TLAdaptiveDetail", wr);
$("TLSlot", yr);
$("TLSlotContent", Sr);
$("TLDrawerToggle", Nr);
