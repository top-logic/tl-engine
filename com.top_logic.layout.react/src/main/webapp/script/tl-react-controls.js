import { React as e, useTLFieldValue as Re, useTLCommand as oe, useTLState as G, useKeyboardBinding as me, useTLUpload as Be, TLChild as K, useI18N as ue, useTLDataUrl as Oe, scrollToAnchor as ln, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as bt, useFocusTrap as _t, CMD_VALUE_CHANGED as We, anchoredOverlayProps as an, register as $ } from "tl-react-bridge";
const { useCallback: wt, useRef: rn } = e, on = 300, sn = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: on }), s = oe(), i = rn(!1), u = wt(
    (S) => {
      i.current = !0, a(S.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = wt(async () => {
    await c(), r && i.current && (i.current = !1, s("commit"));
  }, [c, r, s]), d = t.multiline === !0;
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
  const p = t.hasError === !0, f = t.hasWarnings === !0, g = t.errorMessage, b = [
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
      className: b,
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
      className: b,
      "aria-invalid": p || void 0,
      title: p && g ? g : void 0
    }
  ));
}, { useCallback: yt } = e, cn = 300, un = ({ controlId: l, state: t }) => {
  const [n, a, c] = Re({ debounceMs: cn }), s = yt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = yt(() => {
    c();
  }, [c]);
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
      onChange: s,
      onBlur: i,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": u || void 0,
      title: u && o ? o : void 0
    }
  ));
}, { useCallback: St } = e, dn = 300, mn = ({ controlId: l, state: t, config: n }) => {
  const [a, c, s] = Re({ debounceMs: dn }), i = St(
    (f) => {
      const g = f.target.value;
      c(g === "" ? null : g);
    },
    [c]
  ), u = St(() => {
    s();
  }, [s]);
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
}, { useCallback: pn } = e, fn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = pn(
    (r) => {
      a(r.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
  }
  const s = t.hasError === !0, i = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && i ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: hn } = e, bn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, c] = Re(), s = hn(
    (p) => {
      c(p.target.value || null);
    },
    [c]
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
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    i.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: _n, useRef: gn, useEffect: vn } = e, En = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = t.triState === !0, s = gn(null);
  vn(() => {
    s.current && (s.current.indeterminate = c && n !== !0 && n !== !1);
  }, [c, n]);
  const i = _n(
    (d) => {
      if (!c) {
        a(d.target.checked);
        return;
      }
      a(n === !0 ? !1 : n === !1 ? null : !0);
    },
    [a, c, n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        ref: s,
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
      ref: s,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0,
      "aria-checked": c && n !== !0 && n !== !1 ? "mixed" : n === !0
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
const { useCallback: Cn } = e, wn = ({ controlId: l, command: t, label: n, image: a, disabled: c, displayMode: s }) => {
  const i = G(), u = oe(), r = t ?? "click", o = n ?? i.label, d = a ?? i.image, p = c ?? i.disabled === !0, f = s ?? i.displayMode ?? "label-only", g = i.hidden === !0, b = i.tooltip, S = i.appearance, E = i.size, v = i.navigateUrl, y = Cn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(r);
  }, [u, r, v]), I = i.keyGesture;
  me(I, () => p || g ? !1 : (y(), !0));
  const L = f === "icon-only", _ = f === "label-only" || f === "icon-label" || L && !d, C = b ?? (L ? o : void 0), h = C ? `text:${C}` : void 0;
  return g ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: p,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (S === "link" ? " tlReactButton--link" : "") + (S === "primary" ? " tlReactButton--primary" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": d || L ? o : void 0
    },
    d && /* @__PURE__ */ e.createElement(ye, { encoded: d, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, yn = ({ controlId: l }) => {
  const t = G(), n = Be(), a = e.useRef(null), [c, s] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, d = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, b = e.useCallback(() => {
    var L;
    r || c || (L = a.current) == null || L.click();
  }, [r, c]), S = e.useCallback(async (L) => {
    const _ = L.target.files;
    if (!_ || _.length === 0) return;
    const C = new FormData();
    for (let h = 0; h < _.length; h++)
      C.append("file", _[h], _[h].name);
    L.target.value = "", s(!0);
    try {
      await n(C);
    } finally {
      s(!1);
    }
  }, [n]), E = d === "icon-only", v = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || E && !u, I = r || c;
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
      onClick: b,
      disabled: I,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: Sn } = e, kn = ({ controlId: l, command: t, label: n, active: a, disabled: c }) => {
  const s = G(), i = oe(), u = t ?? "click", r = n ?? s.label, o = a ?? s.active === !0, d = c ?? s.disabled === !0, p = Sn(() => {
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
}, Nn = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Tn } = e, Rn = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.tabs ?? [], c = t.activeTabId, s = Tn((i) => {
    i !== c && n("selectTab", { tabId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((i) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: i.id,
      role: "tab",
      "aria-selected": i.id === c,
      className: "tlReactTabBar__tab" + (i.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(i.id)
    },
    i.icon && /* @__PURE__ */ e.createElement(ye, { encoded: i.icon, className: "tlReactTabBar__tabIcon" }),
    i.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Dn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((c, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, Ln = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, xn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), d = t.status ?? "idle", p = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, g = e.useCallback(async () => {
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
        u.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && r.current.push(_.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((h) => h.stop()), o.current = null;
          const _ = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], _.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const C = new FormData();
          C.append("audio", _, "recording.webm"), await n(C), c("idle");
        }, L.start(), c("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), i("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [a, n]), b = ue(Ln), S = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: g,
      disabled: E,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, In = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Pn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasAudio, c = t.dataRevision ?? 0, [s, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(c);
  e.useEffect(() => {
    a ? s === "disabled" && i("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), i("disabled"));
  }, [a]), e.useEffect(() => {
    c !== o.current && (o.current = c, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (s === "playing" || s === "paused" || s === "loading") && i("idle"));
  }, [c]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), i("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), i("playing");
      return;
    }
    if (!r.current) {
      i("loading");
      try {
        const E = await fetch(n);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), i("idle");
          return;
        }
        const v = await E.blob();
        r.current = URL.createObjectURL(v);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), i("idle");
        return;
      }
    }
    const S = new Audio(r.current);
    u.current = S, S.onended = () => {
      i("idle");
    }, S.play(), i("playing");
  }, [s, n]), p = ue(In), f = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = s === "disabled" || s === "loading", b = ["tlAudioPlayer__button"];
  return s === "playing" && b.push("tlAudioPlayer__button--playing"), s === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Mn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, jn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, d = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (_) => {
    c("uploading");
    const C = new FormData();
    C.append("file", _, _.name), await n(C), c("idle");
  }, [n]), g = e.useCallback((_) => {
    var h;
    const C = (h = _.target.files) == null ? void 0 : h[0];
    C && f(C);
  }, [f]), b = e.useCallback(() => {
    var _;
    a !== "uploading" && ((_ = u.current) == null || _.click());
  }, [a]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!0);
  }, []), E = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!1);
  }, []), v = e.useCallback((_) => {
    var h;
    if (_.preventDefault(), _.stopPropagation(), i(!1), a === "uploading") return;
    const C = (h = _.dataTransfer.files) == null ? void 0 : h[0];
    C && f(C);
  }, [a, f]), y = p === "uploading", I = ue(Mn), L = p === "uploading" ? I["js.uploading"] : I["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: E,
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
        onClick: b,
        disabled: y,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, An = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Bn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, c = Be(), s = Oe(), i = ue(An), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", d = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [b, S] = e.useState("idle"), [E, v] = e.useState(!1), [y, I] = e.useState(!1), L = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!r || y)) {
      I(!0);
      try {
        const F = s + (s.includes("?") ? "&" : "?") + "rev=" + d, B = await fetch(F);
        if (!B.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", B.status);
          return;
        }
        const P = await B.blob(), X = URL.createObjectURL(P), m = document.createElement("a");
        m.href = X, m.download = o, m.style.display = "none", document.body.appendChild(m), m.click(), document.body.removeChild(m), URL.revokeObjectURL(X);
      } catch (F) {
        console.error("[TLBinaryField] Fetch error:", F);
      } finally {
        I(!1);
      }
    }
  }, [r, y, s, d, o]), C = e.useCallback(async (F) => {
    S("uploading");
    const B = new FormData();
    B.append("file", F, F.name), await c(B), S("idle");
  }, [c]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", D = e.useCallback((F) => {
    var P;
    const B = (P = F.target.files) == null ? void 0 : P[0];
    B && C(B);
  }, [C]), R = e.useCallback(() => {
    var F;
    h || (F = L.current) == null || F.click();
  }, [h]), N = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!0);
  }, []), V = e.useCallback((F) => {
    F.preventDefault(), F.stopPropagation(), v(!1);
  }, []), A = e.useCallback((F) => {
    var P;
    if (F.preventDefault(), F.stopPropagation(), v(!1), h) return;
    const B = (P = F.dataTransfer.files) == null ? void 0 : P[0];
    B && C(B);
  }, [h, C]), x = y ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: y,
      title: x,
      "aria-label": x
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Z = h, U = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${E ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: V,
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
        title: U,
        "aria-label": U
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, On = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Fn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const $n = ({ controlId: l }) => {
  const t = G(), n = oe(), a = Be(), c = Oe(), s = ue(On), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [d, p] = e.useState(!1), f = e.useRef(null), g = e.useCallback(async (_) => {
    const C = Array.from(_);
    if (C.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const D of C)
          h.append("file", D, D.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), b = e.useCallback(async (_) => {
    if (_.hasData)
      try {
        const C = c + "&key=" + encodeURIComponent(_.key), h = await fetch(C);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const D = await h.blob(), R = URL.createObjectURL(D), N = document.createElement("a");
        N.href = R, N.download = _.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(R);
      } catch (C) {
        console.error("[TLFileChips] Fetch error:", C);
      }
  }, [c]), S = e.useCallback((_) => {
    _.target.files && g(_.target.files), _.target.value = "";
  }, [g]), E = e.useCallback(() => {
    var _;
    r || (_ = f.current) == null || _.click();
  }, [r]), v = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!0));
  }, [u]), y = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1));
  }, [u]), I = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1), !r && _.dataTransfer.files && g(_.dataTransfer.files));
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
    i.map((_) => {
      const C = s["js.download.file"].replace("{0}", _.name), h = s["js.fileChips.remove"].replace("{0}", _.name);
      return /* @__PURE__ */ e.createElement("span", { key: _.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(_),
          disabled: !_.hasData,
          title: _.hasData ? C : _.name
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
        /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__name" }, _.name),
        _.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Fn(_.size))
      ), u && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__remove",
          onClick: () => n("removeChip", { key: _.key }),
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
        onClick: E,
        disabled: r,
        title: r ? s["js.uploading"] : s["js.fileChips.add"]
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
      /* @__PURE__ */ e.createElement("span", null, r ? s["js.uploading"] : s["js.fileChips.add"])
    ))
  );
}, Un = 3e4;
function Hn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), c = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? c.format(Math.trunc(n / 1), "second") : a < 3600 ? c.format(Math.trunc(n / 60), "minute") : a < 86400 ? c.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? c.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Wn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, c = t.locale || navigator.language, [, s] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => s((u) => u + 1), Un);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Hn(n, c));
}, zn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Vn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const c = (s) => {
    s.preventDefault(), ln(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: c }, a);
};
function Kn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Yn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Gn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Yn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Kn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Xn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, qn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = oe(), c = !!t.hasData, s = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!c || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(b);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const E = await S.blob(), v = URL.createObjectURL(E), y = document.createElement("a");
        y.href = v, y.download = i, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [c, r, n, s, i]), p = e.useCallback(async () => {
    c && await a("clear");
  }, [c, a]), f = ue(Xn);
  if (!c)
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
}, Zn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Qn = ({ controlId: l }) => {
  const t = G(), n = Be(), [a, c] = e.useState("idle"), [s, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), b = t.error, S = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    E(), c("idle");
  }, [E]), y = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !S) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = V, c("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), i("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [a, S]), I = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, V = p.current;
    if (!N || !V)
      return;
    V.width = N.videoWidth, V.height = N.videoHeight;
    const A = V.getContext("2d");
    A && (A.drawImage(N, 0, 0), E(), c("uploading"), V.toBlob(async (x) => {
      if (!x) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", x, "capture.jpg"), await n(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, E]), L = e.useCallback(async (N) => {
    var x;
    const V = (x = N.target.files) == null ? void 0 : x[0];
    if (!V) return;
    c("uploading");
    const A = new FormData();
    A.append("photo", V, V.name), await n(A), c("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && d.current && (o.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var V;
    if (a !== "overlayOpen") return;
    (V = g.current) == null || V.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Le(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null);
  }, []);
  const _ = ue(Zn), C = a === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
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
      title: C,
      "aria-label": C
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
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: I,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[s]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Jn = {
  "js.photoViewer.alt": "Captured photo"
}, el = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPhoto, c = t.dataRevision ?? 0, [s, i] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!a) {
      s && (URL.revokeObjectURL(s), i(null));
      return;
    }
    if (c === u.current && s)
      return;
    u.current = c, s && (URL.revokeObjectURL(s), i(null));
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
  }, [a, c, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const r = ue(Jn);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, tl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, nl = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, c = t.dataRevision ?? 0, s = ue(tl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + c, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: kt, useRef: et } = e, ll = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.orientation, c = t.resizable === !0, s = t.children ?? [], i = a === "horizontal", u = s.length > 0 && s.every((E) => E.collapsed), r = !u && s.some((E) => E.collapsed), o = u ? !i : i, d = et(null), p = et(null), f = et(null), g = kt((E, v) => {
    const y = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : v !== void 0 ? y.flex = `0 0 ${v}px` : y.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (y.minWidth = i ? E.minSize : void 0, y.minHeight = i ? void 0 : E.minSize), y;
  }, [i, u, r, o]), b = kt((E, v) => {
    E.preventDefault();
    const y = d.current;
    if (!y) return;
    const I = s[v], L = s[v + 1], _ = y.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    _.forEach((R) => {
      C.push(i ? R.offsetWidth : R.offsetHeight);
    }), f.current = C, p.current = {
      splitterIndex: v,
      startPos: i ? E.clientX : E.clientY,
      startSizeBefore: C[v],
      startSizeAfter: C[v + 1],
      childBefore: I,
      childAfter: L
    };
    const h = (R) => {
      const N = p.current;
      if (!N || !f.current) return;
      const A = (i ? R.clientX : R.clientY) - N.startPos, x = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Z = N.startSizeBefore + A, U = N.startSizeAfter - A;
      Z < x && (U += Z - x, Z = x), U < O && (Z += U - O, U = O), f.current[N.splitterIndex] = Z, f.current[N.splitterIndex + 1] = U;
      const F = y.querySelectorAll(":scope > .tlSplitPanel__child"), B = F[N.splitterIndex], P = F[N.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${Z}px`), P && (P.style.flex = `0 0 ${U}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const R = {};
        s.forEach((N, V) => {
          const A = N.control;
          A != null && A.controlId && f.current && (R[A.controlId] = f.current[V]);
        }), n("updateSizes", { sizes: R });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", D), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), S = [];
  return s.forEach((E, v) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(E)
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control })
      )
    ), c && v < s.length - 1) {
      const y = s[v + 1];
      !E.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => b(L, v)
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
}, { useCallback: tt } = e, al = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, rl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ol = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), sl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), cl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), il = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ul = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(al), c = t.title, s = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, d = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", g = t.errorMessage, b = s === "MINIMIZED", S = s === "MAXIMIZED", E = s === "HIDDEN", v = tt(() => {
    n("toggleMinimize");
  }, [n]), y = tt(() => {
    n("toggleMaximize");
  }, [n]), I = tt(() => {
    n("popOut");
  }, [n]);
  if (E)
    return null;
  const L = S ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, _ = i && !S || u && !b || r, C = !!c && c.trim() !== "" || !!t.titleContent || !!t.toolbar || _;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: L
    },
    C && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!c && c.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !S && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(ol, null) : /* @__PURE__ */ e.createElement(rl, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: S ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      S ? /* @__PURE__ */ e.createElement(cl, null) : /* @__PURE__ */ e.createElement(sl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: I,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(il, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, g)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, dl = ({ controlId: l }) => {
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
}, ml = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ge, useState: Xe, useEffect: dt, useRef: Ze } = e, pl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function mt(l, t, n, a) {
  const c = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      c.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? c.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (c.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && c.push(...mt(s.children, t, n, s.id)));
  return c;
}
const Ae = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, fl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: c, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: c,
    ref: s,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), hl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: c,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), bl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ae, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), _l = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), gl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: c, onClose: s }) => {
  const i = Ze(null);
  dt(() => {
    const o = (d) => {
      i.current && !i.current.contains(d.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Le(!0, { ESCAPE: s });
  const u = ge((o) => {
    o.type === "nav" ? (a(o.id), s()) : o.type === "command" && (c(o.id), s());
  }, [a, c, s]), r = {};
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
}, vl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: c,
  onExecute: s,
  onToggleGroup: i,
  tabIndex: u,
  itemRef: r,
  onFocus: o,
  focusedId: d,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: g,
  onOpenFlyout: b,
  onCloseFlyout: S
}) => {
  const E = Ze(null), [v, y] = Xe(null), I = ge(() => {
    a ? g === l.id ? S() : (E.current && y(E.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, g, l.id, i, b, S]), L = ge((C) => {
    E.current = C, r(C);
  }, [r]), _ = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: I,
      title: a ? l.label : void 0,
      "aria-expanded": a ? _ : t,
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
  ), _ && /* @__PURE__ */ e.createElement(
    gl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: c,
      onExecute: s,
      onClose: S
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((C) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: C.id,
      item: C,
      activeItemId: n,
      collapsed: a,
      onSelect: c,
      onExecute: s,
      onToggleGroup: i,
      focusedId: d,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: b,
      onCloseFlyout: S
    }
  ))));
}, Ht = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: c,
  onToggleGroup: s,
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
        fl,
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
        hl,
        {
          item: l,
          collapsed: n,
          onExecute: c,
          tabIndex: i === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(bl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(_l, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        vl,
        {
          item: l,
          expanded: g,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: c,
          onToggleGroup: s,
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
}, El = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(pl), c = t.items ?? [], s = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, d] = Xe(() => {
    const x = /* @__PURE__ */ new Map(), O = (Z) => {
      for (const U of Z)
        U.type === "group" && (x.set(U.id, U.expanded), O(U.children));
    };
    return O(c), x;
  }), p = ge((x) => {
    d((O) => {
      const Z = new Map(O), U = Z.get(x) ?? !1;
      return Z.set(x, !U), n("toggleGroup", { itemId: x, expanded: !U }), Z;
    });
  }, [n]), f = ge((x) => {
    x !== s && n("selectItem", { itemId: x });
  }, [n, s]), g = ge((x) => {
    n("executeCommand", { itemId: x });
  }, [n]), b = ge(() => {
    n("toggleCollapse", {});
  }, [n]), S = ge(() => {
    n("toggleDrawer", {});
  }, [n]), [E, v] = Xe(null), y = ge((x) => {
    v(x);
  }, []), I = ge(() => {
    v(null);
  }, []);
  dt(() => {
    r || v(null);
  }, [r]);
  const [L, _] = Xe(() => {
    const x = mt(c, r, o);
    return x.length > 0 ? x[0].id : "";
  }), C = Ze(/* @__PURE__ */ new Map()), h = ge((x) => (O) => {
    O ? C.current.set(x, O) : C.current.delete(x);
  }, []), D = ge((x) => {
    _(x);
  }, []), R = Ze(0), N = ge((x) => {
    _(x), R.current++;
  }, []);
  dt(() => {
    const x = C.current.get(L);
    x && document.activeElement !== x && x.focus();
  }, [L, R.current]);
  const V = ge((x) => {
    if (x.key === "Escape" && E !== null) {
      x.preventDefault(), I();
      return;
    }
    const O = mt(c, r, o);
    if (O.length === 0) return;
    const Z = O.findIndex((F) => F.id === L);
    if (Z < 0) return;
    const U = O[Z];
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
        x.preventDefault(), U.type === "nav" ? f(U.id) : U.type === "command" ? g(U.id) : U.type === "group" && (r ? E === U.id ? I() : y(U.id) : p(U.id));
        break;
      }
      case "ArrowRight": {
        U.type === "group" && !r && ((o.get(U.id) ?? !1) || (x.preventDefault(), p(U.id)));
        break;
      }
      case "ArrowLeft": {
        U.type === "group" && !r && (o.get(U.id) ?? !1) && (x.preventDefault(), p(U.id));
        break;
      }
    }
  }, [
    c,
    r,
    o,
    L,
    E,
    N,
    f,
    g,
    p,
    y,
    I
  ]), A = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((x) => /* @__PURE__ */ e.createElement(
    Ht,
    {
      key: x.id,
      item: x,
      activeItemId: s,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: h,
      onItemFocus: D,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: y,
      onCloseFlyout: I
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Cl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", c = t.align ?? "stretch", s = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${c}`,
    s ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o })));
}, wl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, yl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, c = t.gap ?? "default", s = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: i }, s.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Sl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", c = t.padding ?? "default", s = t.headerActions ?? [], i = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, kl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, c = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, c.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))));
}, { useCallback: Nl } = e, Tl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], c = Nl((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((s, i) => {
    const u = i === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, i > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: Rl } = e, Dl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.items ?? [], c = t.activeItemId, s = Rl((i) => {
    i !== c && n("selectItem", { itemId: i });
  }, [n, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((i) => {
    const u = i.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: i.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(i.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + i.icon, "aria-hidden": "true" }), i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, i.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, i.label)
    );
  }));
}, { useCallback: Nt, useRef: Ll } = e, xl = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Il = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, c = t.closeOnBackdrop !== !1, s = t.child, i = Ll(null), u = Nt(() => {
    n("close");
  }, [n]), r = Nt((o) => {
    c && o.target === o.currentTarget && u();
  }, [c, u]);
  return a ? /* @__PURE__ */ e.createElement(bt, null, /* @__PURE__ */ e.createElement(xl, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: i,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: s })
  )) : null;
}, { useEffect: Pl, useRef: Ml } = e, jl = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Ml(n.length);
  return Pl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((c) => /* @__PURE__ */ e.createElement(K, { key: c.controlId, control: c })));
}, { useCallback: ze, useRef: Ie, useState: Ve } = e, Al = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Bl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ol = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Fl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Bl), c = t.title ?? "", s = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, d = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, b] = Ve(null), [S, E] = Ve(null), [v, y] = Ve(null), I = Ie(null), [L, _] = Ve(!1), C = Ie(null), h = Ie(null), D = Ie(null), R = Ie(null), N = Ie(null), V = ze(() => {
    n("close");
  }, [n]);
  _t(!0, R, "field");
  const A = ze((F, B) => {
    B.preventDefault();
    const P = R.current;
    if (!P) return;
    const X = P.getBoundingClientRect(), m = !I.current, T = I.current ?? { x: X.left, y: X.top };
    m && (I.current = T, y(T)), N.current = {
      dir: F,
      startX: B.clientX,
      startY: B.clientY,
      startW: X.width,
      startH: X.height,
      startPos: { ...T },
      symmetric: m
    };
    const z = (q) => {
      const j = N.current;
      if (!j) return;
      const te = q.clientX - j.startX, ce = q.clientY - j.startY;
      let ne = j.startW, _e = j.startH, Ee = 0, Ce = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * te), j.dir.includes("w") && (ne = j.startW - 2 * te), j.dir.includes("s") && (_e = j.startH + 2 * ce), j.dir.includes("n") && (_e = j.startH - 2 * ce)) : (j.dir.includes("e") && (ne = j.startW + te), j.dir.includes("w") && (ne = j.startW - te, Ee = te), j.dir.includes("s") && (_e = j.startH + ce), j.dir.includes("n") && (_e = j.startH - ce, Ce = ce));
      const Se = Math.max(200, ne), ke = Math.max(100, _e);
      j.symmetric ? (Ee = (j.startW - Se) / 2, Ce = (j.startH - ke) / 2) : (j.dir.includes("w") && Se === 200 && (Ee = j.startW - 200), j.dir.includes("n") && ke === 100 && (Ce = j.startH - 100)), h.current = Se, D.current = ke, b(Se), E(ke);
      const xe = {
        x: j.startPos.x + Ee,
        y: j.startPos.y + Ce
      };
      I.current = xe, y(xe);
    }, H = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", H);
      const q = h.current, j = D.current;
      (q != null || j != null) && n("resize", {
        ...q != null ? { width: Math.round(q) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", H);
  }, [n]), x = ze((F) => {
    if (F.button !== 0 || F.target.closest("button")) return;
    F.preventDefault();
    const B = R.current;
    if (!B) return;
    const P = B.getBoundingClientRect(), X = I.current ?? { x: P.left, y: P.top }, m = F.clientX - X.x, T = F.clientY - X.y, z = (q) => {
      const j = window.innerWidth, te = window.innerHeight;
      let ce = q.clientX - m, ne = q.clientY - T;
      const _e = B.offsetWidth, Ee = B.offsetHeight;
      ce + _e > j && (ce = j - _e), ne + Ee > te && (ne = te - Ee), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const Ce = { x: ce, y: ne };
      I.current = Ce, y(Ce);
    }, H = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", H);
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", H);
  }, []), O = ze(() => {
    var F, B;
    if (L) {
      const P = C.current;
      P && (y(P.x !== -1 ? { x: P.x, y: P.y } : null), b(P.w), E(P.h)), _(!1);
    } else {
      const P = R.current, X = P == null ? void 0 : P.getBoundingClientRect();
      C.current = {
        x: ((F = I.current) == null ? void 0 : F.x) ?? (X == null ? void 0 : X.left) ?? -1,
        y: ((B = I.current) == null ? void 0 : B.y) ?? (X == null ? void 0 : X.top) ?? -1,
        w: g ?? (X == null ? void 0 : X.width) ?? null,
        h: S ?? null
      }, _(!0), y({ x: 0, y: 0 }), b(null), E(null);
    }
  }, [L, g, S]), Z = L ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : s,
    ...S != null ? { height: S + "px" } : i != null ? { height: i } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, U = l + "-title";
  return /* @__PURE__ */ e.createElement(bt, { modal: !0 }, /* @__PURE__ */ e.createElement(Al, { onClose: V }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Z,
      ref: R,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": U
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : x,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: U }, c),
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(K, { control: o })),
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), d.map((F, B) => /* @__PURE__ */ e.createElement(K, { key: B, control: F }))),
    r && !L && Ol.map((F) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: F,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${F}`,
        onMouseDown: (B) => A(F, B)
      }
    ))
  ));
}, { useCallback: $l } = e, Ul = {
  "js.drawer.close": "Close"
}, Hl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(Ul), c = t.open === !0, s = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = $l(() => {
    n("close");
  }, [n]);
  Le(c, { ESCAPE: o });
  const d = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${i}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: d, "aria-hidden": !c }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
}, { useCallback: Wl } = e, zl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.child, c = Wl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: Vl, useEffect: Tt, useRef: Kl, useState: Rt } = e, Yl = 250, Gl = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.message ?? "", c = t.content ?? "", s = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, d] = Rt(!1), [p, f] = Rt(!1), g = Kl(!1);
  Tt(() => {
    g.current = !1;
  }, [r]);
  const b = Vl(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Tt(() => {
    if (!u || i === 0 || p) return;
    const S = setTimeout(b, g.current ? Yl : i);
    return () => clearTimeout(S);
  }, [u, i, p, b]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        g.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: nt, useEffect: Dt, useRef: Xl, useState: Lt } = e, ql = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.open === !0, c = t.anchorId, s = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Xl(null), [o, d] = Lt({ top: 0, left: 0 }), [p, f] = Lt(0), g = u.filter((v) => v.type === "item" && !v.disabled);
  Dt(() => {
    var h, D;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (s != null && i != null) {
      let R = i, N = s;
      R + v > window.innerHeight && (R = Math.max(0, window.innerHeight - v)), N + y > window.innerWidth && (N = Math.max(0, window.innerWidth - y)), d({ top: R, left: N }), f(0);
      return;
    }
    if (!c) return;
    const I = document.getElementById(c);
    if (!I) return;
    const L = I.getBoundingClientRect();
    let _ = L.bottom + 4, C = L.left;
    _ + v > window.innerHeight && (_ = L.top - v - 4), C + y > window.innerWidth && (C = L.right - y), d({ top: _, left: C }), f(0);
  }, [a, c, s, i]);
  const b = nt(() => {
    n("close");
  }, [n]), S = nt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  Dt(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, b]);
  const E = nt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), b();
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
  }, [b, S, g, p]);
  return _t(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: o.top, left: o.left },
      onKeyDown: E
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
}, Zl = 768, Ql = ({ controlId: l }) => {
  const t = G(), n = oe();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Zl}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(o.matches);
    const p = (f) => d(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, c = t.content, s = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, Jl = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", c = t.hasTooltip === !0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": c ? "key:tooltip" : void 0
    },
    n
  );
}, ea = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: c }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (c(), !0) : !1), null), ta = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, xt = 50, na = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, na));
}
const pt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', la = pt + ", button:not([disabled]), a[href]";
function Wt(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function at(l, t, n = {}) {
  const a = Wt(l, t);
  if (n.col) {
    const s = a.find((u) => u.dataset.col === n.col), i = s == null ? void 0 : s.querySelector(pt);
    if (i) return i;
  }
  if (n.col)
    return null;
  const c = n.last ? [...a].reverse() : a;
  for (const s of c) {
    const i = s.querySelector(pt);
    if (i) return i;
  }
  return null;
}
const aa = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(ta), c = e.useRef(null);
  e.useEffect(() => {
    const w = c.current;
    if (!w) return;
    const k = (W) => {
      const J = W.detail;
      let ee = J.target;
      for (; ee && ee !== w; ) {
        const ae = ee.dataset.row, re = ee.dataset.col;
        if (ae != null && re != null) {
          J.resolved = { key: ae + "|" + re };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return w.addEventListener("tl-tooltip-resolve", k), () => w.removeEventListener("tl-tooltip-resolve", k);
  }, []);
  const s = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", d = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, b = t.columnSelect ?? !1, S = e.useMemo(
    () => s.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [s]
  ), E = o === "multi", v = 40, y = 20, I = e.useRef(null), L = e.useRef(null), _ = e.useRef(null), C = e.useRef(null), h = e.useRef(null), [D, R] = e.useState({}), N = e.useRef(null), V = e.useRef(!1), A = e.useRef(null), [x, O] = e.useState(null), [Z, U] = e.useState(null), [F, B] = e.useState(null);
  e.useEffect(() => {
    N.current || R({});
  }, [s]);
  const P = e.useCallback((w) => D[w.name] ?? w.width, [D]), X = e.useMemo(() => {
    const w = [];
    let k = E && f > 0 ? v : 0;
    for (let W = 0; W < f && W < s.length; W++)
      w.push(k), k += P(s[W]);
    return w;
  }, [s, f, E, v, P]), m = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let w = E ? v : 0;
    for (let k = 0; k < f && k < s.length; k++)
      w += P(s[k]);
    return w;
  }, [s, f, E, v, P]), T = i * r, z = e.useRef(null), H = e.useCallback((w, k, W) => {
    W.preventDefault(), W.stopPropagation(), N.current = { column: w, startX: W.clientX, startWidth: k };
    let J = W.clientX, ee = 0;
    const ae = () => {
      const se = N.current;
      if (!se) return;
      const de = Math.max(xt, se.startWidth + (J - se.startX) + ee);
      R((ve) => ({ ...ve, [se.column]: de }));
    }, re = () => {
      const se = _.current, de = I.current;
      if (!se || !N.current) return;
      const ve = se.getBoundingClientRect(), Ne = 40, Et = 8, nn = se.scrollLeft;
      J > ve.right - Ne ? se.scrollLeft += Et : J < ve.left + Ne && (se.scrollLeft = Math.max(0, se.scrollLeft - Et));
      const Ct = se.scrollLeft - nn;
      Ct !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Ct, ae()), z.current = requestAnimationFrame(re);
    };
    z.current = requestAnimationFrame(re);
    const fe = (se) => {
      J = se.clientX, ae();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), z.current !== null && (cancelAnimationFrame(z.current), z.current = null);
      const de = N.current;
      if (de) {
        const ve = Math.max(xt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: ve }), N.current = null, V.current = !0, requestAnimationFrame(() => {
          V.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), q = e.useCallback(() => {
    I.current && _.current && (I.current.scrollLeft = _.current.scrollLeft), C.current !== null && clearTimeout(C.current), C.current = window.setTimeout(() => {
      const w = _.current;
      if (!w) return;
      const k = w.scrollTop, W = Math.ceil(w.clientHeight / r), J = Math.floor(k / r);
      n("scroll", { start: J, count: W });
    }, 80);
  }, [n, r]), j = e.useCallback((w, k, W) => {
    if (V.current) return;
    let J;
    !k || k === "desc" ? J = "asc" : J = "desc";
    const ee = W.shiftKey ? "add" : "replace";
    n("sort", { column: w, direction: J, mode: ee });
  }, [n]), te = e.useCallback((w, k) => {
    A.current = w, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", w);
  }, []), ce = e.useCallback((w, k) => {
    if (!A.current || A.current === w) {
      O(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const W = k.currentTarget.getBoundingClientRect(), J = k.clientX < W.left + W.width / 2 ? "left" : "right";
    O({ column: w, side: J });
  }, []), ne = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const k = A.current;
    if (!k || !x) {
      A.current = null, O(null);
      return;
    }
    let W = s.findIndex((ee) => ee.name === x.column);
    if (W < 0) {
      A.current = null, O(null);
      return;
    }
    const J = s.findIndex((ee) => ee.name === k);
    x.side === "right" && W++, J < W && W--, n("columnReorder", { column: k, targetIndex: W }), A.current = null, O(null);
  }, [s, x, n]), _e = e.useCallback(() => {
    A.current = null, O(null);
  }, []), Ee = e.useCallback((w, k) => {
    var ee, ae, re, fe;
    const W = window.getSelection();
    if (W && !W.isCollapsed && k.currentTarget.contains(W.anchorNode))
      return;
    if (!lt(k) && ((ee = _.current) == null || ee.focus({ preventScroll: !0 }), !k.ctrlKey && !k.metaKey && !k.shiftKey)) {
      const pe = (fe = (re = (ae = k.target) == null ? void 0 : ae.closest) == null ? void 0 : re.call(ae, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      h.current = { index: w, col: pe ?? void 0 };
    }
    const J = u.find((pe) => pe.index === w);
    lt(k) && (J != null && J.selected) && !k.ctrlKey && !k.metaKey && !k.shiftKey || n("select", {
      rowIndex: w,
      ctrlKey: k.ctrlKey || k.metaKey,
      shiftKey: k.shiftKey
    });
  }, [n, u]), Ce = e.useCallback((w, k, W) => {
    n("moveSelection", { direction: w, extend: k, move: W });
  }, [n]), Se = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: E, shiftKey: !1 });
  }, [n, p, E]), ke = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), xe = e.useCallback(
    () => !!c.current && c.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const w = _.current;
    if (!w)
      return;
    const k = p * r, W = k + r;
    k < w.scrollTop ? w.scrollTop = k : W > w.scrollTop + w.clientHeight && (w.scrollTop = W - w.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const w = h.current, k = _.current;
    if (!w || !k)
      return;
    const W = u.find((ae) => ae.index === w.index);
    if (!W || !at(k, W.id))
      return;
    h.current = null;
    const J = document.activeElement;
    if (J && J !== document.body && !k.contains(J))
      return;
    const ee = at(k, W.id, { col: w.col, last: w.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const He = e.useCallback((w) => {
    if (w.key !== "Tab")
      return;
    const k = _.current, W = document.activeElement;
    if (!k || !W || !k.contains(W))
      return;
    const J = W.closest("[data-row][data-col]");
    if (!J)
      return;
    const ee = J.dataset.row, ae = u.find((Ne) => Ne.id === ee);
    if (!ae)
      return;
    const re = Wt(k, ee).flatMap((Ne) => Array.from(Ne.querySelectorAll(la))), fe = re.indexOf(W);
    if (fe < 0)
      return;
    const pe = !w.shiftKey;
    if (!(pe ? fe === re.length - 1 : fe === 0))
      return;
    const de = pe ? ae.index + 1 : ae.index - 1;
    if (de < 0 || de >= i)
      return;
    const ve = u.find((Ne) => Ne.index === de);
    ve && at(k, ve.id) || (w.preventDefault(), h.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), M = e.useCallback((w, k) => {
    k.stopPropagation(), n("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Y = e.useCallback(() => {
    const w = d === i && i > 0;
    n("selectAll", { selected: !w });
  }, [n, d, i]), le = e.useCallback((w, k, W) => {
    W.stopPropagation(), n("expand", { rowIndex: w, expanded: k });
  }, [n]), ie = e.useCallback((w, k) => {
    k.preventDefault(), U({ x: k.clientX, y: k.clientY, colIdx: w });
  }, []), Fe = e.useCallback(() => {
    Z && (n("setFrozenColumnCount", { count: Z.colIdx + 1 }), U(null));
  }, [Z, n]), qt = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), U(null);
  }, [n]), Zt = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const k = L.current, W = I.current;
    if (!k || !W)
      return;
    const J = k.clientWidth, ee = [{ x: 0, count: 0 }];
    W.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - k.getBoundingClientRect().left;
      se > 0 && se <= J && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let ae = { x: m, count: f };
    const re = (pe) => {
      const se = pe.clientX - k.getBoundingClientRect().left;
      ae = ee.reduce(
        (de, ve) => Math.abs(ve.x - se) < Math.abs(de.x - se) ? ve : de,
        ee[0]
      ), B(ae);
    }, fe = () => {
      document.removeEventListener("mousemove", re), document.removeEventListener("mouseup", fe), B(null), ae.count !== f && n("setFrozenColumnCount", { count: ae.count });
    };
    document.addEventListener("mousemove", re), document.addEventListener("mouseup", fe);
  }, [m, f, n]);
  e.useEffect(() => {
    if (!Z) return;
    const w = () => U(null);
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [Z]), Le(!!Z, { ESCAPE: () => U(null) });
  const Qt = e.useCallback((w, k) => {
    k.stopPropagation(), k.preventDefault(), n("openFilter", { column: w });
  }, [n]), Jt = e.useCallback((w) => {
    w.stopPropagation(), w.preventDefault(), n("openColumnSelect", {});
  }, [n]), Qe = s.reduce((w, k) => w + P(k), 0) + (E ? v : 0), Je = b ? 32 : 0, en = d === i && i > 0, vt = d > 0 && d < i, tn = e.useCallback((w) => {
    w && (w.indeterminate = vt);
  }, [vt]);
  return /* @__PURE__ */ e.createElement(bt, { active: xe }, /* @__PURE__ */ e.createElement(
    ea,
    {
      isMulti: E,
      cursorIndex: p,
      onMove: Ce,
      onToggle: Se,
      onSelectAll: ke
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (w) => {
        if (!A.current) return;
        w.preventDefault();
        const k = _.current, W = I.current;
        if (!k) return;
        const J = k.getBoundingClientRect(), ee = 40, ae = 8;
        w.clientX < J.left + ee ? k.scrollLeft = Math.max(0, k.scrollLeft - ae) : w.clientX > J.right - ee && (k.scrollLeft += ae), W && (W.scrollLeft = k.scrollLeft);
      },
      onDrop: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: L }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: I }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: Qe, paddingRight: Je } }, E && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: v,
          minWidth: v,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          A.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== A.current && O({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: tn,
          className: "tlTableView__checkbox",
          checked: en,
          onChange: Y
        }
      )
    ), s.map((w, k) => {
      const W = P(w);
      s.length - 1;
      let J = "tlTableView__headerCell";
      w.sortable && (J += " tlTableView__headerCell--sortable"), x && x.column === w.name && (J += " tlTableView__headerCell--dragOver-" + x.side);
      const ee = k < f, ae = k === f - 1;
      return ee && (J += " tlTableView__headerCell--frozen"), ae && (J += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: J,
          "data-col-idx": k,
          style: {
            width: W,
            minWidth: W,
            position: ee ? "sticky" : "relative",
            ...ee ? { left: X[k], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (re) => j(w.name, w.sortDirection, re) : void 0,
          onContextMenu: (re) => ie(k, re),
          onDragStart: (re) => te(w.name, re),
          onDragOver: (re) => ce(w.name, re),
          onDrop: ne,
          onDragEnd: _e
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (w.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: w.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (re) => re.stopPropagation(),
            onClick: (re) => Qt(w.name, re)
          },
          /* @__PURE__ */ e.createElement("i", { className: w.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", S > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => H(w.name, W, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (A.current && s.length > 0) {
            const k = s[s.length - 1];
            k.name !== A.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", O({ column: k.name, side: "right" }));
          }
        },
        onDrop: ne
      }
    ))), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (F ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: m },
        title: a["js.table.freezeSplitter"],
        onMouseDown: Zt
      }
    ), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Jt
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: _,
        className: "tlTableView__body",
        onScroll: q,
        onKeyDown: He,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: T, position: "relative", width: Qe, paddingRight: Je } }, u.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : "") + (w.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: w.index * r,
            height: r,
            width: Qe,
            paddingRight: Je,
            ...w.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (k) => {
            (k.shiftKey || k.ctrlKey || k.metaKey || k.detail > 1) && !lt(k) && k.preventDefault();
          },
          onClick: (k) => Ee(w.index, k)
        },
        E && /* @__PURE__ */ e.createElement(
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
              checked: w.selected,
              onChange: () => {
              },
              onClick: (k) => M(w.index, k),
              tabIndex: -1
            }
          )
        ),
        s.map((k, W) => {
          const J = P(k), ee = W === s.length - 1, ae = W < f, re = W === f - 1;
          let fe = "tlTableView__cell";
          ae && (fe += " tlTableView__cell--frozen"), re && (fe += " tlTableView__cell--frozenLast");
          const pe = g && W === 0, se = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: k.name,
              className: fe,
              "data-row": w.id,
              "data-col": k.name,
              style: {
                ...ee && !ae ? { flex: "1 0 auto", minWidth: J } : { width: J, minWidth: J },
                ...ae ? { position: "sticky", left: X[W], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => le(w.index, !w.expanded, de)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), w.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: w.cells[k.name] })) : w.cells[k.name] && /* @__PURE__ */ e.createElement(K, { control: w.cells[k.name] })
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
        onMouseDown: (w) => w.stopPropagation()
      },
      Z.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Fe }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: qt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ra = {
  "js.table.columnSearch": "Find column"
}, oa = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(ra), c = t.entries ?? [], s = c.filter((_) => _.visible).length, [i, u] = e.useState(""), r = i.trim().toLowerCase(), o = r ? c.filter((_) => _.label.toLowerCase().includes(r)) : c, d = e.useRef(null), p = e.useRef(null), [f, g] = e.useState(null), b = e.useCallback((_) => {
    p.current = _, g(_);
  }, []), S = e.useCallback((_, C) => {
    n("columnVisible", { column: _, visible: C });
  }, [n]), E = e.useCallback((_, C) => {
    d.current = _, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", _);
  }, []), v = e.useCallback((_, C) => {
    if (!d.current || d.current === _) {
      b(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const h = C.currentTarget.getBoundingClientRect(), D = C.clientY < h.top + h.height / 2 ? "top" : "bottom";
    b({ name: _, side: D });
  }, [b]), y = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), I = e.useCallback((_) => {
    _.preventDefault();
    const C = d.current, h = p.current;
    if (d.current = null, b(null), !C || !h)
      return;
    const D = c.findIndex((V) => V.name === h.name), R = c.findIndex((V) => V.name === C);
    if (D < 0 || R < 0)
      return;
    let N = h.side === "top" ? D : D + 1;
    R < N && N--, N !== R && n("columnReorder", { column: C, targetIndex: N });
  }, [c, n, b]), L = c.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, L && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: i,
      onChange: (_) => u(_.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (L ? " tlColumnSelect__list--fixed" : "") }, o.map((_) => {
    const C = _.visible && s <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === _.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: _.name,
        className: h,
        draggable: !0,
        onDragStart: (D) => E(_.name, D),
        onDragOver: (D) => v(_.name, D),
        onDrop: I,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: _.visible,
          disabled: C,
          onChange: (D) => S(_.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, _.label))
    );
  })));
}, sa = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, zt = e.createContext(sa), { useMemo: ca, useRef: ia, useState: ua, useEffect: da } = e, ma = 320, pa = "TLTableView", fa = "TLPanel", ha = ({ controlId: l }) => {
  var E;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", c = t.readOnly === !0, s = t.children ?? [], i = t.noModelMessage, u = ia(null), [r, o] = ua(
    a === "top" ? "top" : "side"
  );
  da(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const y = new ResizeObserver((I) => {
      for (const L of I) {
        const C = L.contentRect.width / n;
        o(C < ma ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const d = ca(() => ({
    readOnly: c,
    resolvedLabelPosition: r
  }), [c, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = s.length === 1 ? s[0] : void 0, b = !!g && (g.module === pa || g.module === fa && ((E = g.state) == null ? void 0 : E.bare) === !0), S = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(zt.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: f, ref: u }, s.map((v, y) => /* @__PURE__ */ e.createElement(K, { key: y, control: v }))));
}, { useCallback: ba } = e, _a = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ga = ({ controlId: l }) => {
  const t = G(), n = oe(), a = ue(_a), c = t.headerControl ?? null, s = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, d = t.children ?? [], p = c != null || s.length > 0 || i, f = ba(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: c })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((b, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: b }))));
}, { useContext: va, useState: Ea, useCallback: Ca } = e, wa = ({ controlId: l }) => {
  const t = G(), n = va(zt), a = t.label ?? "", c = t.required === !0, s = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, d = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, g = t.visible !== !1, b = t.hasTooltip === !0, S = t.field, E = n.readOnly, [v, y] = Ea(!1), I = Ca(() => y((D) => !D), []), L = p === "hidden", _ = s != null, C = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    E ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    !_ && C ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: g ? void 0 : { display: "none" } }, !L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), c && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: S })), !E && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, s)), !E && !_ && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !E && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ya = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.iconCss, c = t.iconSrc, s = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : c ? /* @__PURE__ */ e.createElement("img", { src: c, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, o, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
    b.preventDefault(), n("goto", {});
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
}, Sa = 20, ka = () => {
  var C;
  const l = G(), t = oe(), n = l.nodes ?? [], a = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), d = e.useRef(null), p = ((C = n.find((h) => h.selected)) == null ? void 0 : C.id) ?? null;
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
  }, [t]), b = e.useCallback((h, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: h, x: D.clientX, y: D.clientY });
  }, [t]), S = e.useRef(null), E = e.useCallback((h, D) => {
    const R = D.getBoundingClientRect(), N = h.clientY - R.top, V = R.height / 3;
    return N < V ? "above" : N > V * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", h);
  }, []), y = e.useCallback((h, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const R = E(D, D.currentTarget);
    S.current != null && window.clearTimeout(S.current), S.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: R }), S.current = null;
    }, 50);
  }, [t, E]), I = e.useCallback((h, D) => {
    D.preventDefault(), S.current != null && (window.clearTimeout(S.current), S.current = null);
    const R = E(D, D.currentTarget);
    t("drop", { nodeId: h, position: R });
  }, [t, E]), L = e.useCallback(() => {
    S.current != null && (window.clearTimeout(S.current), S.current = null), t("dragEnd");
  }, [t]), _ = e.useCallback((h) => {
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
            for (let V = r - 1; V >= 0; V--)
              if (n[V].depth < N) {
                D = V;
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
      onKeyDown: _
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
        style: { paddingLeft: h.depth * Sa },
        draggable: c,
        onMouseDown: (R) => {
          (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && R.preventDefault();
        },
        onClick: (R) => g(h.id, R),
        onContextMenu: (R) => b(h.id, R),
        onDragStart: (R) => v(h.id, R),
        onDragOver: s ? (R) => y(h.id, R) : void 0,
        onDrop: s ? (R) => I(h.id, R) : void 0,
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
var rt = { exports: {} }, he = {}, ot = { exports: {} }, Q = {};
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
function Na() {
  if (It) return Q;
  It = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function g(m) {
    return m === null || typeof m != "object" ? null : (m = f && m[f] || m["@@iterator"], typeof m == "function" ? m : null);
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
  }, S = Object.assign, E = {};
  function v(m, T, z) {
    this.props = m, this.context = T, this.refs = E, this.updater = z || b;
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
  function I(m, T, z) {
    this.props = m, this.context = T, this.refs = E, this.updater = z || b;
  }
  var L = I.prototype = new y();
  L.constructor = I, S(L, v.prototype), L.isPureReactComponent = !0;
  var _ = Array.isArray;
  function C() {
  }
  var h = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function R(m, T, z) {
    var H = z.ref;
    return {
      $$typeof: l,
      type: m,
      key: T,
      ref: H !== void 0 ? H : null,
      props: z
    };
  }
  function N(m, T) {
    return R(m.type, T, m.props);
  }
  function V(m) {
    return typeof m == "object" && m !== null && m.$$typeof === l;
  }
  function A(m) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + m.replace(/[=:]/g, function(z) {
      return T[z];
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
        switch (typeof m.status == "string" ? m.then(C, C) : (m.status = "pending", m.then(
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
  function U(m, T, z, H, q) {
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
              return te = m._init, U(
                te(m._payload),
                T,
                z,
                H,
                q
              );
          }
      }
    if (te)
      return q = q(m), te = H === "" ? "." + O(m, 0) : H, _(q) ? (z = "", te != null && (z = te.replace(x, "$&/") + "/"), U(q, T, z, "", function(_e) {
        return _e;
      })) : q != null && (V(q) && (q = N(
        q,
        z + (q.key == null || m && m.key === q.key ? "" : ("" + q.key).replace(
          x,
          "$&/"
        ) + "/") + te
      )), T.push(q)), 1;
    te = 0;
    var ce = H === "" ? "." : H + ":";
    if (_(m))
      for (var ne = 0; ne < m.length; ne++)
        H = m[ne], j = ce + O(H, ne), te += U(
          H,
          T,
          z,
          j,
          q
        );
    else if (ne = g(m), typeof ne == "function")
      for (m = ne.call(m), ne = 0; !(H = m.next()).done; )
        H = H.value, j = ce + O(H, ne++), te += U(
          H,
          T,
          z,
          j,
          q
        );
    else if (j === "object") {
      if (typeof m.then == "function")
        return U(
          Z(m),
          T,
          z,
          H,
          q
        );
      throw T = String(m), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(m).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function F(m, T, z) {
    if (m == null) return m;
    var H = [], q = 0;
    return U(m, H, "", "", function(j) {
      return T.call(z, j, q++);
    }), H;
  }
  function B(m) {
    if (m._status === -1) {
      var T = m._result;
      T = T(), T.then(
        function(z) {
          (m._status === 0 || m._status === -1) && (m._status = 1, m._result = z);
        },
        function(z) {
          (m._status === 0 || m._status === -1) && (m._status = 2, m._result = z);
        }
      ), m._status === -1 && (m._status = 0, m._result = T);
    }
    if (m._status === 1) return m._result.default;
    throw m._result;
  }
  var P = typeof reportError == "function" ? reportError : function(m) {
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
  }, X = {
    map: F,
    forEach: function(m, T, z) {
      F(
        m,
        function() {
          T.apply(this, arguments);
        },
        z
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
      if (!V(m))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return m;
    }
  };
  return Q.Activity = p, Q.Children = X, Q.Component = v, Q.Fragment = n, Q.Profiler = c, Q.PureComponent = I, Q.StrictMode = a, Q.Suspense = r, Q.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Q.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(m) {
      return h.H.useMemoCache(m);
    }
  }, Q.cache = function(m) {
    return function() {
      return m.apply(null, arguments);
    };
  }, Q.cacheSignal = function() {
    return null;
  }, Q.cloneElement = function(m, T, z) {
    if (m == null)
      throw Error(
        "The argument must be a React element, but you passed " + m + "."
      );
    var H = S({}, m.props), q = m.key;
    if (T != null)
      for (j in T.key !== void 0 && (q = "" + T.key), T)
        !D.call(T, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && T.ref === void 0 || (H[j] = T[j]);
    var j = arguments.length - 2;
    if (j === 1) H.children = z;
    else if (1 < j) {
      for (var te = Array(j), ce = 0; ce < j; ce++)
        te[ce] = arguments[ce + 2];
      H.children = te;
    }
    return R(m.type, q, H);
  }, Q.createContext = function(m) {
    return m = {
      $$typeof: i,
      _currentValue: m,
      _currentValue2: m,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, m.Provider = m, m.Consumer = {
      $$typeof: s,
      _context: m
    }, m;
  }, Q.createElement = function(m, T, z) {
    var H, q = {}, j = null;
    if (T != null)
      for (H in T.key !== void 0 && (j = "" + T.key), T)
        D.call(T, H) && H !== "key" && H !== "__self" && H !== "__source" && (q[H] = T[H]);
    var te = arguments.length - 2;
    if (te === 1) q.children = z;
    else if (1 < te) {
      for (var ce = Array(te), ne = 0; ne < te; ne++)
        ce[ne] = arguments[ne + 2];
      q.children = ce;
    }
    if (m && m.defaultProps)
      for (H in te = m.defaultProps, te)
        q[H] === void 0 && (q[H] = te[H]);
    return R(m, j, q);
  }, Q.createRef = function() {
    return { current: null };
  }, Q.forwardRef = function(m) {
    return { $$typeof: u, render: m };
  }, Q.isValidElement = V, Q.lazy = function(m) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: m },
      _init: B
    };
  }, Q.memo = function(m, T) {
    return {
      $$typeof: o,
      type: m,
      compare: T === void 0 ? null : T
    };
  }, Q.startTransition = function(m) {
    var T = h.T, z = {};
    h.T = z;
    try {
      var H = m(), q = h.S;
      q !== null && q(z, H), typeof H == "object" && H !== null && typeof H.then == "function" && H.then(C, P);
    } catch (j) {
      P(j);
    } finally {
      T !== null && z.types !== null && (T.types = z.types), h.T = T;
    }
  }, Q.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, Q.use = function(m) {
    return h.H.use(m);
  }, Q.useActionState = function(m, T, z) {
    return h.H.useActionState(m, T, z);
  }, Q.useCallback = function(m, T) {
    return h.H.useCallback(m, T);
  }, Q.useContext = function(m) {
    return h.H.useContext(m);
  }, Q.useDebugValue = function() {
  }, Q.useDeferredValue = function(m, T) {
    return h.H.useDeferredValue(m, T);
  }, Q.useEffect = function(m, T) {
    return h.H.useEffect(m, T);
  }, Q.useEffectEvent = function(m) {
    return h.H.useEffectEvent(m);
  }, Q.useId = function() {
    return h.H.useId();
  }, Q.useImperativeHandle = function(m, T, z) {
    return h.H.useImperativeHandle(m, T, z);
  }, Q.useInsertionEffect = function(m, T) {
    return h.H.useInsertionEffect(m, T);
  }, Q.useLayoutEffect = function(m, T) {
    return h.H.useLayoutEffect(m, T);
  }, Q.useMemo = function(m, T) {
    return h.H.useMemo(m, T);
  }, Q.useOptimistic = function(m, T) {
    return h.H.useOptimistic(m, T);
  }, Q.useReducer = function(m, T, z) {
    return h.H.useReducer(m, T, z);
  }, Q.useRef = function(m) {
    return h.H.useRef(m);
  }, Q.useState = function(m) {
    return h.H.useState(m);
  }, Q.useSyncExternalStore = function(m, T, z) {
    return h.H.useSyncExternalStore(
      m,
      T,
      z
    );
  }, Q.useTransition = function() {
    return h.H.useTransition();
  }, Q.version = "19.2.4", Q;
}
var Pt;
function Ta() {
  return Pt || (Pt = 1, ot.exports = Na()), ot.exports;
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
function Ra() {
  if (Mt) return he;
  Mt = 1;
  var l = Ta();
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
  }, c = Symbol.for("react.portal");
  function s(r, o, d) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
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
    return s(r, o, null, d);
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
function Da() {
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
  return l(), rt.exports = Ra(), rt.exports;
}
var Vt = Da();
const { useState: Te, useCallback: be, useRef: $e, useEffect: Pe, useMemo: ft } = e;
function gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function La({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: c,
  onDragStart: s,
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
      draggable: c || void 0,
      onDragStart: s,
      onDragOver: i,
      onDrop: u,
      onDragEnd: r
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
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
function xa({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: c,
  id: s
}) {
  const i = be(() => a(l.value), [a, l.value]), u = ft(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: i,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(gt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Ia = ({ controlId: l, state: t }) => {
  const n = oe(), a = t.value ?? [], c = t.multiSelect === !0, s = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, d = t.options ?? [], p = t.emptyOptionLabel ?? "", f = s && c && !u && r, g = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = g["js.dropdownSelect.nothingFound"], S = be(
    (M) => g["js.dropdownSelect.removeChip"].replace("{0}", M),
    [g]
  ), [E, v] = Te(!1), [y, I] = Te(""), [L, _] = Te(-1), [C, h] = Te(!1), [D, R] = Te({}), [N, V] = Te(null), [A, x] = Te(null), [O, Z] = Te(null), U = $e(null), F = $e(null), B = $e(null), P = $e(a);
  P.current = a;
  const X = $e(-1), m = ft(
    () => new Set(a.map((M) => M.value)),
    [a]
  ), T = ft(() => {
    let M = d.filter((Y) => !m.has(Y.value));
    if (y) {
      const Y = y.toLowerCase();
      M = M.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return M;
  }, [d, m, y]);
  Pe(() => {
    y && T.length === 1 ? _(0) : _(-1);
  }, [T.length, y]), Pe(() => {
    E && o && F.current && F.current.focus();
  }, [E, o, a]), Pe(() => {
    var le, ie;
    if (X.current < 0) return;
    const M = X.current;
    X.current = -1;
    const Y = (le = U.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(M, Y.length - 1)].focus() : (ie = U.current) == null || ie.focus();
  }, [a]), Pe(() => {
    if (!E) return;
    const M = (Y) => {
      U.current && !U.current.contains(Y.target) && B.current && !B.current.contains(Y.target) && (v(!1), I(""));
    };
    return document.addEventListener("mousedown", M), () => document.removeEventListener("mousedown", M);
  }, [E]), Pe(() => {
    if (!E || !U.current) return;
    const M = U.current.getBoundingClientRect(), Y = window.innerHeight - M.bottom, ie = Y < 300 && M.top > Y;
    R({
      left: M.left,
      width: M.width,
      ...ie ? { bottom: window.innerHeight - M.top } : { top: M.bottom }
    });
  }, [E]);
  const z = be(async () => {
    if (!(u || !r) && (v(!0), I(""), _(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), H = be(() => {
    var M;
    v(!1), I(""), _(-1), (M = U.current) == null || M.focus();
  }, []), q = be(
    (M) => {
      let Y;
      if (c) {
        const le = d.find((ie) => ie.value === M);
        if (le)
          Y = [...P.current, le];
        else
          return;
      } else {
        const le = d.find((ie) => ie.value === M);
        if (le)
          Y = [le];
        else
          return;
      }
      P.current = Y, n(We, { value: Y.map((le) => le.value) }), c ? (I(""), _(-1)) : H();
    },
    [c, d, n, H]
  ), j = be(
    (M) => {
      X.current = P.current.findIndex((le) => le.value === M);
      const Y = P.current.filter((le) => le.value !== M);
      P.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), te = be(
    (M) => {
      M.stopPropagation(), n(We, { value: [] }), H();
    },
    [n, H]
  ), ce = be((M) => {
    I(M.target.value);
  }, []), ne = be(
    (M) => {
      if (!E) {
        if (M.key === "ArrowDown" || M.key === "ArrowUp" || M.key === "Enter" || M.key === " ") {
          if (M.target.tagName === "BUTTON") return;
          M.preventDefault(), M.stopPropagation(), z();
        }
        return;
      }
      switch (M.key) {
        case "ArrowDown":
          M.preventDefault(), M.stopPropagation(), _(
            (Y) => Y < T.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          M.preventDefault(), M.stopPropagation(), _(
            (Y) => Y > 0 ? Y - 1 : T.length - 1
          );
          break;
        case "Enter":
          M.preventDefault(), M.stopPropagation(), L >= 0 && L < T.length && q(T[L].value);
          break;
        case "Escape":
          M.preventDefault(), M.stopPropagation(), H();
          break;
        case "Tab":
          H();
          break;
        case "Backspace":
          y === "" && c && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      E,
      z,
      H,
      T,
      L,
      q,
      y,
      c,
      a,
      j
    ]
  ), _e = be(
    async (M) => {
      M.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), Ee = be(
    (M, Y) => {
      V(M), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(M));
    },
    []
  ), Ce = be(
    (M, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", N === null || N === M) {
        x(null), Z(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Fe = Y.clientX < ie ? "before" : "after";
      x(M), Z(Fe);
    },
    [N]
  ), Se = be(
    (M) => {
      if (M.preventDefault(), N === null || A === null || O === null || N === A) return;
      const Y = [...P.current], [le] = Y.splice(N, 1);
      let ie = A;
      N < A ? ie = O === "before" ? ie - 1 : ie : ie = O === "before" ? ie : ie + 1, Y.splice(ie, 0, le), P.current = Y, n(We, { value: Y.map((Fe) => Fe.value) }), V(null), x(null), Z(null);
    },
    [N, A, O, n]
  ), ke = be(() => {
    V(null), x(null), Z(null);
  }, []);
  if (Pe(() => {
    if (L < 0 || !B.current) return;
    const M = B.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    M && M.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((M) => /* @__PURE__ */ e.createElement("span", { key: M.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(gt, { image: M.image }), /* @__PURE__ */ e.createElement("span", null, M.label))));
  const xe = !i && a.length > 0 && !u, He = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...an
    },
    (o || C) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
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
      !o && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: _e }, g["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && T.map((M, Y) => /* @__PURE__ */ e.createElement(
        xa,
        {
          key: M.value,
          id: `${l}-opt-${Y}`,
          option: M,
          highlighted: Y === L,
          searchTerm: y,
          onSelect: q,
          onMouseEnter: () => _(Y)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: U,
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : z,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((M, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : A === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : A === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        La,
        {
          key: M.value,
          option: M,
          removable: !u && (c || !i),
          onRemove: j,
          removeLabel: S(M.label),
          draggable: f,
          onDragStart: f ? (ie) => Ee(Y, ie) : void 0,
          onDragOver: f ? (ie) => Ce(Y, ie) : void 0,
          onDrop: f ? Se : void 0,
          onDragEnd: f ? ke : void 0,
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), He && Vt.createPortal(He, document.body));
}, { useCallback: st, useRef: Pa } = e, Kt = "application/x-tl-color", Ma = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: c,
  onReplace: s
}) => {
  const i = Pa(null), u = st(
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
      f ? s(d, f) : i.current !== null && i.current !== d && c(i.current, d), i.current = null;
    },
    [c, s]
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
  const a = (c) => Yt(c).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function ja(l, t, n) {
  const a = l / 255, c = t / 255, s = n / 255, i = Math.max(a, c, s), u = Math.min(a, c, s), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (c - s) / r % 6 : i === c ? o = (s - a) / r + 2 : o = (a - c) / r + 4, o *= 60, o < 0 && (o += 360));
  const d = i === 0 ? 0 : r / i;
  return [o, d, i];
}
function Aa(l, t, n) {
  const a = n * t, c = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = c, r = 0) : l < 120 ? (i = c, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = c) : l < 240 ? (i = 0, u = c, r = a) : l < 300 ? (i = c, u = 0, r = a) : (i = a, u = 0, r = c), [
    Math.round((i + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function Ba(l) {
  return ja(...Gt(l));
}
function ct(l, t, n) {
  return Xt(...Aa(l, t, n));
}
const { useCallback: Me, useRef: At } = e, Oa = ({ color: l, onColorChange: t }) => {
  const [n, a, c] = Ba(l), s = At(null), i = At(null), u = Me(
    (b, S) => {
      var I;
      const E = (I = s.current) == null ? void 0 : I.getBoundingClientRect();
      if (!E) return;
      const v = Math.max(0, Math.min(1, (b - E.left) / E.width)), y = Math.max(0, Math.min(1, 1 - (S - E.top) / E.height));
      t(ct(n, v, y));
    },
    [n, t]
  ), r = Me(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), o = Me(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), d = Me(
    (b) => {
      var y;
      const S = (y = i.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const v = Math.max(0, Math.min(1, (b - S.top) / S.height)) * 360;
      t(ct(v, a, c));
    },
    [a, c, t]
  ), p = Me(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), f = Me(
    (b) => {
      b.buttons !== 0 && d(b.clientY);
    },
    [d]
  ), g = ct(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
      onPointerDown: r,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - c) * 100}%` }
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
function Fa(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const $a = {
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
}, { useState: Ke, useCallback: we, useEffect: Bt, useRef: Ua, useLayoutEffect: Ha } = e, Wa = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: c,
  canReset: s,
  onConfirm: i,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [o, d] = Ke("palette"), [p, f] = Ke(t), g = Ua(null), b = ue($a), [S, E] = Ke(null);
  Ha(() => {
    if (!l.current || !g.current) return;
    const B = l.current.getBoundingClientRect(), P = g.current.getBoundingClientRect();
    let X = B.bottom + 4, m = B.left;
    X + P.height > window.innerHeight && (X = B.top - P.height - 4), m + P.width > window.innerWidth && (m = Math.max(0, B.right - P.width)), E({ top: X, left: m });
  }, [l]);
  const v = p != null, [y, I, L] = v ? Gt(p) : [0, 0, 0], [_, C] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  Bt(() => {
    C((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), Bt(() => {
    const B = (X) => {
      g.current && !g.current.contains(X.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const h = we(
    (B) => (P) => {
      const X = parseInt(P.target.value, 10);
      if (isNaN(X)) return;
      const m = Yt(X);
      f(Xt(B === "r" ? m : y, B === "g" ? m : I, B === "b" ? m : L));
    },
    [y, I, L]
  ), D = we(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(Kt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), B.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), R = we((B) => {
    const P = B.target.value;
    C(P), ht(P) && f(P);
  }, []), N = we(() => {
    f(null);
  }, []), V = we((B) => {
    f(B);
  }, []), A = we(
    (B) => {
      i(B);
    },
    [i]
  ), x = we(
    (B, P) => {
      const X = [...n], m = X[B];
      X[B] = X[P], X[P] = m, r(X);
    },
    [n, r]
  ), O = we(
    (B, P) => {
      const X = [...n];
      X[B] = P, r(X);
    },
    [n, r]
  ), Z = we(() => {
    r([...c]);
  }, [c, r]), U = we(
    (B) => {
      if (Fa(n, B)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const X = [...n];
      X[P] = B.toUpperCase(), r(X);
    },
    [n, r]
  ), F = we(() => {
    p != null && U(p), i(p);
  }, [p, i, U]);
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
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => d("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Ma,
      {
        colors: n,
        columns: a,
        onSelect: V,
        onConfirm: A,
        onSwap: x,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Z }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Oa, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: p } : void 0,
        draggable: v,
        onDragStart: v ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? I : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !ht(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: F }, b["js.colorInput.ok"]))
  );
}, za = { "js.colorInput.chooseColor": "Choose color" }, { useState: Va, useCallback: Ye, useRef: Ka } = e, Ya = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = oe(), s = ue(za), [i, u] = Va(!1), r = Ka(null), o = n, d = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, g = t.defaultPalette ?? p, b = Ye(() => {
    d && u(!0);
  }, [d]), S = Ye(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Ye(() => {
    u(!1);
  }, []), v = Ye(
    (y) => {
      c("paletteChanged", { palette: y });
    },
    [c]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": s["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Wa,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: g,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: E,
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
}, { useState: Ue, useCallback: De, useEffect: it, useRef: Ot, useLayoutEffect: Ga, useMemo: Xa } = e, qa = {
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
}, Za = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: c,
  onCancel: s,
  onLoadIcons: i
}) => {
  const u = ue(qa), [r, o] = Ue("simple"), [d, p] = Ue(""), [f, g] = Ue(t ?? ""), [b, S] = Ue(!1), [E, v] = Ue(null), y = Ot(null), I = Ot(null);
  Ga(() => {
    if (!l.current || !y.current) return;
    const A = l.current.getBoundingClientRect(), x = y.current.getBoundingClientRect();
    let O = A.bottom + 4, Z = A.left;
    O + x.height > window.innerHeight && (O = A.top - x.height - 4), Z + x.width > window.innerWidth && (Z = Math.max(0, A.right - x.width)), v({ top: O, left: Z });
  }, [l]), it(() => {
    !a && !b && i().catch(() => S(!0));
  }, [a, b, i]), it(() => {
    a && I.current && I.current.focus();
  }, [a]), Le(!0, { ESCAPE: s }), it(() => {
    const A = (O) => {
      y.current && !y.current.contains(O.target) && s();
    }, x = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(x), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const L = Xa(() => {
    if (!d) return n;
    const A = d.toLowerCase();
    return n.filter(
      (x) => x.prefix.toLowerCase().includes(A) || x.label.toLowerCase().includes(A) || x.terms != null && x.terms.some((O) => O.includes(A))
    );
  }, [n, d]), _ = De((A) => {
    p(A.target.value);
  }, []), C = De(
    (A) => {
      c(A);
    },
    [c]
  ), h = De((A) => {
    g(A);
  }, []), D = De((A) => {
    g(A.target.value);
  }, []), R = De(() => {
    c(f || null);
  }, [f, c]), N = De(() => {
    c(null);
  }, [c]), V = De(async (A) => {
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
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
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
        onChange: _,
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
      !a && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
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
            onClick: () => r === "simple" ? C(x.encoded) : h(x.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? C(x.encoded) : h(x.encoded));
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
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, Qa = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Ja, useCallback: Ge, useRef: er } = e, tr = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), c = oe(), s = ue(Qa), [i, u] = Ja(!1), r = er(null), o = n, d = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], g = t.iconsLoaded === !0, b = Ge(() => {
    d && !p && u(!0);
  }, [d, p]), S = Ge(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Ge(() => {
    u(!1);
  }, []), v = Ge(async () => {
    await c("loadIcons");
  }, [c]);
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: p,
      title: o ?? "",
      "aria-label": s["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Za,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: g,
      onSelect: S,
      onCancel: E,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: je, useEffect: nr, useMemo: Ft, useRef: lr, useState: ut } = e, ar = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, rr = [1, 2, 3, 4];
function or(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), c = n[2] || "px";
  return c === "rem" || c === "em" ? a * t : a;
}
function sr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const c of rr)
    n >= c && (a = c);
  return a;
}
function cr(l, t) {
  const n = ar[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function ir(l, t) {
  const n = Math.max(1, t), a = {}, c = (p, f) => !!(a[p] && a[p][f]), s = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const b of i) b.rowStart === p && (f = b);
    if (!f) return;
    let g = f.colEnd;
    for (; g < n && !c(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let b = f.rowStart; b < f.rowEnd; b++)
        for (let S = f.colEnd; S < g; S++) s(b, S);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(cr(p.width, n), n);
    for (; c(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let I = r; I < n && !c(u, I); I++)
      b++;
    if (g > b) {
      for (o(u), r = 0, u++; c(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let I = r; I < n && !c(u, I); I++)
        b++;
      g = Math.min(g, b);
    }
    const S = r, E = r + g, v = u, y = u + f;
    i.push({ id: p.id, colStart: S, colEnd: E, rowStart: v, rowEnd: y });
    for (let I = v; I < y; I++)
      for (let L = S; L < E; L++) s(I, L);
    r = E, r >= n && (r = 0, u++);
  }
  o(u);
  let d = 0;
  for (const p of i) p.rowEnd > d && (d = p.rowEnd);
  for (let p = 1; p < d; p++)
    for (let f = 0; f < n; f++) {
      if (c(p, f)) continue;
      const g = i.find((b) => b.rowEnd === p && b.colStart <= f && f < b.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let b = g.colStart; b < g.colEnd; b++) s(p, b);
      }
    }
  return i;
}
const ur = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((C) => C && C.id), s = lr(null), [i, u] = ut(1), r = t.editMode === !0;
  nr(() => {
    const C = s.current;
    if (!C) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = or(a, h), R = () => u(sr(C.clientWidth, D));
    R();
    const N = new ResizeObserver(R);
    return N.observe(C), () => N.disconnect();
  }, [a]);
  const o = Ft(() => ir(c, i), [c, i]), d = Ft(() => {
    const C = {};
    for (const h of o) C[h.id] = h;
    return C;
  }, [o]), [p, f] = ut(null), [g, b] = ut(null), S = je((C, h) => {
    if (!r) {
      C.preventDefault();
      return;
    }
    f(h), C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", h);
  }, [r]), E = je((C, h) => {
    if (!r || !p || p === h) return;
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const D = C.currentTarget.getBoundingClientRect(), R = C.clientX < D.left + D.width / 2;
    b((N) => N && N.id === h && N.before === R ? N : { id: h, before: R });
  }, [r, p]), v = je(() => {
  }, []), y = je((C, h, D) => {
    const R = c.map((x) => x.id), N = R.indexOf(C);
    if (N < 0) return;
    R.splice(N, 1);
    const V = R.indexOf(h);
    if (V < 0) {
      R.splice(N, 0, C);
      return;
    }
    const A = D ? V : V + 1;
    R.splice(A, 0, C), n("reorder", { order: R });
  }, [c, n]), I = je((C, h) => {
    if (!r || !p || p === h) return;
    C.preventDefault();
    const D = C.currentTarget.getBoundingClientRect(), R = C.clientX < D.left + D.width / 2;
    y(p, h, R), f(null), b(null);
  }, [r, p, y]), L = je(() => {
    f(null), b(null);
  }, []), _ = {
    display: "grid",
    gridTemplateColumns: `repeat(${i}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, c.map((C) => {
      const h = d[C.id];
      if (!h) return null;
      const D = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, R = ["tlDashboard__tile"];
      return p === C.id && R.push("tlDashboard__tile--dragging"), g && g.id === C.id && R.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C.id,
          className: R.join(" "),
          style: D,
          draggable: r,
          onDragStart: (N) => S(N, C.id),
          onDragOver: (N) => E(N, C.id),
          onDragLeave: v,
          onDrop: (N) => I(N, C.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: C.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: dr, useRef: $t, useState: Ut, useEffect: mr, useLayoutEffect: pr } = e, fr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, hr = ({ group: l }) => {
  var p, f;
  const [t, n] = Ut(!1), [a, c] = Ut({}), s = $t(null), i = $t(null), u = dr(() => {
    n((g) => !g);
  }, []);
  pr(() => {
    if (!t) return;
    const g = () => {
      const b = s.current;
      if (!b) return;
      const S = b.getBoundingClientRect();
      c({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), mr(() => {
    if (!t) return;
    const g = (b) => {
      i.current && !i.current.contains(b.target) && s.current && !s.current.contains(b.target) && n(!1);
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
      ref: s,
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
      r.map((g, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: g }))),
      (f = l.subGroups) == null ? void 0 : f.map((g, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((S, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: S })))))
    ),
    document.body
  ));
}, br = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((c) => c.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((c, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: c.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), c.display === "menu" ? /* @__PURE__ */ e.createElement(hr, { group: c }) : /* @__PURE__ */ e.createElement(fr, { group: c }))));
}, _r = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, gr = ({ controlId: l }) => {
  const t = G(), n = oe(), a = t.content, c = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, c && c.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, c.map((s, i) => {
    const u = i === c.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, vr = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, c) => /* @__PURE__ */ e.createElement(K, { key: c, control: a })));
}, Er = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Cr = {
  "js.sidebar.openDrawer": "Open navigation"
}, wr = ({ controlId: l }) => {
  const t = oe(), n = ue(Cr);
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
$("TLButton", wn);
$("TLUploadButton", yn);
$("TLToggleButton", kn);
$("TLTextInput", sn);
$("TLPasswordInput", un);
$("TLNumberInput", mn);
$("TLDatePicker", fn);
$("TLSelect", bn);
$("TLCheckbox", En);
$("TLCounter", Nn);
$("TLTabBar", Rn);
$("TLFieldList", Dn);
$("TLAudioRecorder", xn);
$("TLAudioPlayer", Pn);
$("TLFileUpload", jn);
$("TLBinaryField", Bn);
$("TLFileChips", $n);
$("TLRelativeTime", Wn);
$("TLAnchor", zn);
$("TLScrollLink", Vn);
$("TLAvatar", Gn);
$("TLDownload", qn);
$("TLPhotoCapture", Qn);
$("TLPhotoViewer", el);
$("TLPdfViewer", nl);
$("TLSplitPanel", ll);
$("TLPanel", ul);
$("TLInset", wl);
$("TLMaximizeRoot", dl);
$("TLDeckPane", ml);
$("TLSidebar", El);
$("TLStack", Cl);
$("TLGrid", yl);
$("TLCard", Sl);
$("TLAppBar", kl);
$("TLBreadcrumb", Tl);
$("TLBottomBar", Dl);
$("TLDialog", Il);
$("TLDialogManager", jl);
$("TLWindow", Fl);
$("TLDrawer", Hl);
$("TLContextMenuRegion", zl);
$("TLSnackbar", Gl);
$("TLMenu", ql);
$("TLAppShell", Ql);
$("TLText", Jl);
$("TLTableView", aa);
$("TLColumnSelect", oa);
$("TLFormLayout", ha);
$("TLFormGroup", ga);
$("TLFormField", wa);
$("TLResourceCell", ya);
$("TLTreeView", ka);
$("TLDropdownSelect", Ia);
$("TLColorInput", Ya);
$("TLIconSelect", tr);
$("TLDashboard", ur);
$("TLToolbar", br);
$("TLTileStack", _r);
$("TLAdaptiveDetail", gr);
$("TLSlot", vr);
$("TLSlotContent", Er);
$("TLDrawerToggle", wr);
