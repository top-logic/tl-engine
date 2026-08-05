import { React as e, useTLFieldValue as Re, useTLCommand as ae, useTLState as G, useKeyboardBinding as de, useTLUpload as Oe, TLChild as K, useI18N as ie, useTLDataUrl as Fe, scrollToAnchor as Qt, useStandaloneKeyboardScope as xe, KeyboardScopeProvider as ht, useFocusTrap as bt, CMD_VALUE_CHANGED as We, anchoredOverlayProps as Jt, register as U } from "tl-react-bridge";
const { useCallback: Ct, useRef: en } = e, tn = 300, nn = ({ controlId: l, state: t }) => {
  const [n, a, s] = Re({ debounceMs: tn }), c = ae(), i = en(!1), u = Ct(
    (k) => {
      i.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = Ct(async () => {
    await s(), r && i.current && (i.current = !1, c("commit"));
  }, [s, r, c]), d = t.multiline === !0;
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
}, { useCallback: wt } = e, ln = 300, an = ({ controlId: l, state: t }) => {
  const [n, a, s] = Re({ debounceMs: ln }), c = wt(
    (p) => {
      a(p.target.value);
    },
    [a]
  ), i = wt(() => {
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
}, { useCallback: yt } = e, rn = 300, on = ({ controlId: l, state: t, config: n }) => {
  const [a, s, c] = Re({ debounceMs: rn }), i = yt(
    (f) => {
      const g = f.target.value;
      s(g === "" ? null : g);
    },
    [s]
  ), u = yt(() => {
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
}, { useCallback: sn } = e, cn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), s = sn(
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
      type: "date",
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: un } = e, dn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, s] = Re(), c = un(
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
}, { useCallback: mn, useRef: pn, useEffect: fn } = e, hn = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), s = t.triState === !0, c = pn(null);
  fn(() => {
    c.current && (c.current.indeterminate = s && n !== !0 && n !== !1);
  }, [s, n]);
  const i = mn(
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
const { useCallback: bn } = e, _n = ({ controlId: l, command: t, label: n, image: a, disabled: s, displayMode: c }) => {
  const i = G(), u = ae(), r = t ?? "click", o = n ?? i.label, d = a ?? i.image, p = s ?? i.disabled === !0, f = c ?? i.displayMode ?? "label-only", g = i.hidden === !0, b = i.tooltip, k = i.appearance, C = i.size, v = i.navigateUrl, w = bn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(r);
  }, [u, r, v]), I = i.keyGesture;
  de(I, () => p || g ? !1 : (w(), !0));
  const R = f === "icon-only", _ = f === "label-only" || f === "icon-label" || R && !d, E = b ?? (R ? o : void 0), h = E ? `text:${E}` : void 0;
  return g ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: p,
      className: "tlReactButton" + (R ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": d || R ? o : void 0
    },
    d && /* @__PURE__ */ e.createElement(ye, { encoded: d, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, gn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = e.useRef(null), [s, c] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, d = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, b = e.useCallback(() => {
    var R;
    r || s || (R = a.current) == null || R.click();
  }, [r, s]), k = e.useCallback(async (R) => {
    const _ = R.target.files;
    if (!_ || _.length === 0) return;
    const E = new FormData();
    for (let h = 0; h < _.length; h++)
      E.append("file", _[h], _[h].name);
    R.target.value = "", c(!0);
    try {
      await n(E);
    } finally {
      c(!1);
    }
  }, [n]), C = d === "icon-only", v = d === "icon-only" || d === "icon-label", w = d === "label-only" || d === "icon-label" || C && !u, I = r || s;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: g || void 0,
      onChange: k,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: I,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: vn } = e, En = ({ controlId: l, command: t, label: n, active: a, disabled: s }) => {
  const c = G(), i = ae(), u = t ?? "click", r = n ?? c.label, o = a ?? c.active === !0, d = s ?? c.disabled === !0, p = vn(() => {
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
}, Cn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: wn } = e, yn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.tabs ?? [], s = t.activeTabId, c = wn((i) => {
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
}, kn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((s, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: s })))));
}, Sn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Nn = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), d = t.status ?? "idle", p = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, g = e.useCallback(async () => {
    if (a === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (a !== "uploading") {
      if (i(null), !window.isSecureContext || !navigator.mediaDevices) {
        i("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = w, r.current = [];
        const I = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(w, I ? { mimeType: I } : void 0);
        u.current = R, R.ondataavailable = (_) => {
          _.data.size > 0 && r.current.push(_.data);
        }, R.onstop = async () => {
          w.getTracks().forEach((h) => h.stop()), o.current = null;
          const _ = new Blob(r.current, { type: R.mimeType || "audio/webm" });
          if (r.current = [], _.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const E = new FormData();
          E.append("audio", _, "recording.webm"), await n(E), s("idle");
        }, R.start(), s("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), i("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [a, n]), b = ie(Sn), k = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], C = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: g,
      disabled: C,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[c]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Tn = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Rn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasAudio, s = t.dataRevision ?? 0, [c, i] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(s);
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
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      i("idle");
    }, k.play(), i("playing");
  }, [c, n]), p = ie(Tn), f = c === "loading" ? p["js.loading"] : c === "playing" ? p["js.audioPlayer.pause"] : c === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = c === "disabled" || c === "loading", b = ["tlAudioPlayer__button"];
  return c === "playing" && b.push("tlAudioPlayer__button--playing"), c === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: d,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Dn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ln = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, s] = e.useState("idle"), [c, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, d = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (_) => {
    s("uploading");
    const E = new FormData();
    E.append("file", _, _.name), await n(E), s("idle");
  }, [n]), g = e.useCallback((_) => {
    var h;
    const E = (h = _.target.files) == null ? void 0 : h[0];
    E && f(E);
  }, [f]), b = e.useCallback(() => {
    var _;
    a !== "uploading" && ((_ = u.current) == null || _.click());
  }, [a]), k = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!0);
  }, []), C = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!1);
  }, []), v = e.useCallback((_) => {
    var h;
    if (_.preventDefault(), _.stopPropagation(), i(!1), a === "uploading") return;
    const E = (h = _.dataTransfer.files) == null ? void 0 : h[0];
    E && f(E);
  }, [a, f]), w = p === "uploading", I = ie(Dn), R = p === "uploading" ? I["js.uploading"] : I["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
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
        onClick: b,
        disabled: w,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, xn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, In = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, s = Oe(), c = Fe(), i = ie(xn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", d = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [b, k] = e.useState("idle"), [C, v] = e.useState(!1), [w, I] = e.useState(!1), R = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!r || w)) {
      I(!0);
      try {
        const $ = c + (c.includes("?") ? "&" : "?") + "rev=" + d, A = await fetch($);
        if (!A.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", A.status);
          return;
        }
        const P = await A.blob(), X = URL.createObjectURL(P), m = document.createElement("a");
        m.href = X, m.download = o, m.style.display = "none", document.body.appendChild(m), m.click(), document.body.removeChild(m), URL.revokeObjectURL(X);
      } catch ($) {
        console.error("[TLBinaryField] Fetch error:", $);
      } finally {
        I(!1);
      }
    }
  }, [r, w, c, d, o]), E = e.useCallback(async ($) => {
    k("uploading");
    const A = new FormData();
    A.append("file", $, $.name), await s(A), k("idle");
  }, [s]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", L = e.useCallback(($) => {
    var P;
    const A = (P = $.target.files) == null ? void 0 : P[0];
    A && E(A);
  }, [E]), S = e.useCallback(() => {
    var $;
    h || ($ = R.current) == null || $.click();
  }, [h]), N = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!0);
  }, []), B = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!1);
  }, []), F = e.useCallback(($) => {
    var P;
    if ($.preventDefault(), $.stopPropagation(), v(!1), h) return;
    const A = (P = $.dataTransfer.files) == null ? void 0 : P[0];
    A && E(A);
  }, [h, E]), x = w ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: w,
      title: x,
      "aria-label": x
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, i["js.download.noFile"]));
  const Q = h, H = h ? i["js.uploading"] : i["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: B,
      onDrop: F
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: R,
        type: "file",
        accept: p || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (Q ? " tlFileUpload__button--uploading" : ""),
        onClick: S,
        disabled: Q,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, Pn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function jn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Mn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Oe(), s = Fe(), c = ie(Pn), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [d, p] = e.useState(!1), f = e.useRef(null), g = e.useCallback(async (_) => {
    const E = Array.from(_);
    if (E.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const L of E)
          h.append("file", L, L.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), b = e.useCallback(async (_) => {
    if (_.hasData)
      try {
        const E = s + "&key=" + encodeURIComponent(_.key), h = await fetch(E);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const L = await h.blob(), S = URL.createObjectURL(L), N = document.createElement("a");
        N.href = S, N.download = _.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(S);
      } catch (E) {
        console.error("[TLFileChips] Fetch error:", E);
      }
  }, [s]), k = e.useCallback((_) => {
    _.target.files && g(_.target.files), _.target.value = "";
  }, [g]), C = e.useCallback(() => {
    var _;
    r || (_ = f.current) == null || _.click();
  }, [r]), v = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!0));
  }, [u]), w = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1));
  }, [u]), I = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1), !r && _.dataTransfer.files && g(_.dataTransfer.files));
  }, [u, r, g]), R = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: R,
      onDragOver: v,
      onDragLeave: w,
      onDrop: I
    },
    i.map((_) => {
      const E = c["js.download.file"].replace("{0}", _.name), h = c["js.fileChips.remove"].replace("{0}", _.name);
      return /* @__PURE__ */ e.createElement("span", { key: _.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(_),
          disabled: !_.hasData,
          title: _.hasData ? E : _.name
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
        _.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, jn(_.size))
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
        onChange: k,
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
}, An = 3e4;
function Bn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), s = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? s.format(Math.trunc(n / 1), "second") : a < 3600 ? s.format(Math.trunc(n / 60), "minute") : a < 86400 ? s.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? s.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const On = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, s = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => c((u) => u + 1), An);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Bn(n, s));
}, Fn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, $n = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const s = (c) => {
    c.preventDefault(), Qt(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: s }, a);
};
function Un(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Hn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Wn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Hn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Un(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Vn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, zn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = ae(), s = !!t.hasData, c = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!s || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + c, k = await fetch(b);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const C = await k.blob(), v = URL.createObjectURL(C), w = document.createElement("a");
        w.href = v, w.download = i, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [s, r, n, c, i]), p = e.useCallback(async () => {
    s && await a("clear");
  }, [s, a]), f = ie(Vn);
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
}, Kn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Yn = ({ controlId: l }) => {
  const t = G(), n = Oe(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), b = t.error, k = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), C = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    C(), s("idle");
  }, [C]), w = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !k) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const B = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = B, s("overlayOpen");
      } catch (B) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", B), i("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [a, k]), I = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, B = p.current;
    if (!N || !B)
      return;
    B.width = N.videoWidth, B.height = N.videoHeight;
    const F = B.getContext("2d");
    F && (F.drawImage(N, 0, 0), C(), s("uploading"), B.toBlob(async (x) => {
      if (!x) {
        s("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", x, "capture.jpg"), await n(O), s("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), R = e.useCallback(async (N) => {
    var x;
    const B = (x = N.target.files) == null ? void 0 : x[0];
    if (!B) return;
    s("uploading");
    const F = new FormData();
    F.append("photo", B, B.name), await n(F), s("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && d.current && (o.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var B;
    if (a !== "overlayOpen") return;
    (B = g.current) == null || B.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), xe(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null);
  }, []);
  const _ = ie(Kn), E = a === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const L = ["tlPhotoCapture__overlayVideo"];
  u && L.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return u && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: w,
      disabled: a === "uploading",
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !k && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: R
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
        className: L.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[c]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Gn = {
  "js.photoViewer.alt": "Captured photo"
}, Xn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasPhoto, s = t.dataRevision ?? 0, [c, i] = e.useState(null), u = e.useRef(s);
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
  const r = ie(Gn);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, qn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Zn = ({ controlId: l }) => {
  const t = G(), n = Fe(), a = !!t.hasPdf, s = t.dataRevision ?? 0, c = ie(qn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + s, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: kt, useRef: Je } = e, Qn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.orientation, s = t.resizable === !0, c = t.children ?? [], i = a === "horizontal", u = c.length > 0 && c.every((C) => C.collapsed), r = !u && c.some((C) => C.collapsed), o = u ? !i : i, d = Je(null), p = Je(null), f = Je(null), g = kt((C, v) => {
    const w = {
      overflow: C.scrolling || "auto"
    };
    return C.collapsed ? u && !o ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : v !== void 0 ? w.flex = `0 0 ${v}px` : w.flex = `${C.size} 1 0%`, C.minSize > 0 && !C.collapsed && (w.minWidth = i ? C.minSize : void 0, w.minHeight = i ? void 0 : C.minSize), w;
  }, [i, u, r, o]), b = kt((C, v) => {
    C.preventDefault();
    const w = d.current;
    if (!w) return;
    const I = c[v], R = c[v + 1], _ = w.querySelectorAll(":scope > .tlSplitPanel__child"), E = [];
    _.forEach((S) => {
      E.push(i ? S.offsetWidth : S.offsetHeight);
    }), f.current = E, p.current = {
      splitterIndex: v,
      startPos: i ? C.clientX : C.clientY,
      startSizeBefore: E[v],
      startSizeAfter: E[v + 1],
      childBefore: I,
      childAfter: R
    };
    const h = (S) => {
      const N = p.current;
      if (!N || !f.current) return;
      const F = (i ? S.clientX : S.clientY) - N.startPos, x = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Q = N.startSizeBefore + F, H = N.startSizeAfter - F;
      Q < x && (H += Q - x, Q = x), H < O && (Q += H - O, H = O), f.current[N.splitterIndex] = Q, f.current[N.splitterIndex + 1] = H;
      const $ = w.querySelectorAll(":scope > .tlSplitPanel__child"), A = $[N.splitterIndex], P = $[N.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${Q}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, L = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", L), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const S = {};
        c.forEach((N, B) => {
          const F = N.control;
          F != null && F.controlId && f.current && (S[F.controlId] = f.current[B]);
        }), n("updateSizes", { sizes: S });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", L), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, i, n]), k = [];
  return c.forEach((C, v) => {
    if (k.push(
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
      const w = c[v + 1];
      !C.collapsed && !w.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (R) => b(R, v)
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
    k
  );
}, qe = ({ image: l, className: t }) => {
  if (!l) return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: et } = e, Jn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, el = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), tl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), nl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ll = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), al = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), rl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Jn), s = t.title, c = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, d = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", g = t.errorMessage, b = c === "MINIMIZED", k = c === "MAXIMIZED", C = c === "HIDDEN", v = et(() => {
    n("toggleMinimize");
  }, [n]), w = et(() => {
    n("toggleMaximize");
  }, [n]), I = et(() => {
    n("popOut");
  }, [n]);
  if (C)
    return null;
  const R = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, _ = i && !k || u && !b || r, E = !!s && s.trim() !== "" || !!t.titleContent || !!t.toolbar || _;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: R
    },
    E && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!s && s.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(tl, null) : /* @__PURE__ */ e.createElement(el, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(ll, null) : /* @__PURE__ */ e.createElement(nl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: I,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(al, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, g)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, ol = ({ controlId: l }) => {
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
}, sl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: be, useState: Xe, useEffect: ut, useRef: Ze } = e, cl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function dt(l, t, n, a) {
  const s = [];
  for (const c of l)
    if (c.type === "nav") {
      if (c.hidden) continue;
      s.push({ id: c.id, type: "nav", groupId: a });
    } else c.type === "command" ? s.push({ id: c.id, type: "command", groupId: a }) : c.type === "group" && (s.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && s.push(...dt(c.children, t, n, c.id)));
  return s;
}
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, il = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: s, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: s,
    ref: c,
    onFocus: () => i(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ul = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: s,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), dl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), ml = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), pl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: s, onClose: c }) => {
  const i = Ze(null);
  ut(() => {
    const o = (d) => {
      i.current && !i.current.contains(d.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [c]), xe(!0, { ESCAPE: c });
  const u = be((o) => {
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
        /* @__PURE__ */ e.createElement(Be, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, fl = ({
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
  onOpenFlyout: b,
  onCloseFlyout: k
}) => {
  const C = Ze(null), [v, w] = Xe(null), I = be(() => {
    a ? g === l.id ? k() : (C.current && w(C.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, g, l.id, i, b, k]), R = be((E) => {
    C.current = E, r(E);
  }, [r]), _ = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: I,
      title: a ? l.label : void 0,
      "aria-expanded": a ? _ : t,
      tabIndex: u,
      ref: R,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
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
    pl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: s,
      onExecute: c,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((E) => /* @__PURE__ */ e.createElement(
    Ut,
    {
      key: E.id,
      item: E,
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
      onOpenFlyout: b,
      onCloseFlyout: k
    }
  ))));
}, Ut = ({
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
        il,
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
        ul,
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
      return /* @__PURE__ */ e.createElement(dl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(ml, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        fl,
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
}, hl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(cl), s = t.items ?? [], c = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, d] = Xe(() => {
    const x = /* @__PURE__ */ new Map(), O = (Q) => {
      for (const H of Q)
        H.type === "group" && (x.set(H.id, H.expanded), O(H.children));
    };
    return O(s), x;
  }), p = be((x) => {
    d((O) => {
      const Q = new Map(O), H = Q.get(x) ?? !1;
      return Q.set(x, !H), n("toggleGroup", { itemId: x, expanded: !H }), Q;
    });
  }, [n]), f = be((x) => {
    x !== c && n("selectItem", { itemId: x });
  }, [n, c]), g = be((x) => {
    n("executeCommand", { itemId: x });
  }, [n]), b = be(() => {
    n("toggleCollapse", {});
  }, [n]), k = be(() => {
    n("toggleDrawer", {});
  }, [n]), [C, v] = Xe(null), w = be((x) => {
    v(x);
  }, []), I = be(() => {
    v(null);
  }, []);
  ut(() => {
    r || v(null);
  }, [r]);
  const [R, _] = Xe(() => {
    const x = dt(s, r, o);
    return x.length > 0 ? x[0].id : "";
  }), E = Ze(/* @__PURE__ */ new Map()), h = be((x) => (O) => {
    O ? E.current.set(x, O) : E.current.delete(x);
  }, []), L = be((x) => {
    _(x);
  }, []), S = Ze(0), N = be((x) => {
    _(x), S.current++;
  }, []);
  ut(() => {
    const x = E.current.get(R);
    x && document.activeElement !== x && x.focus();
  }, [R, S.current]);
  const B = be((x) => {
    if (x.key === "Escape" && C !== null) {
      x.preventDefault(), I();
      return;
    }
    const O = dt(s, r, o);
    if (O.length === 0) return;
    const Q = O.findIndex(($) => $.id === R);
    if (Q < 0) return;
    const H = O[Q];
    switch (x.key) {
      case "ArrowDown": {
        x.preventDefault();
        const $ = (Q + 1) % O.length;
        N(O[$].id);
        break;
      }
      case "ArrowUp": {
        x.preventDefault();
        const $ = (Q - 1 + O.length) % O.length;
        N(O[$].id);
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
        x.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? g(H.id) : H.type === "group" && (r ? C === H.id ? I() : w(H.id) : p(H.id));
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
    R,
    C,
    N,
    f,
    g,
    p,
    w,
    I
  ]), F = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: F }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: B }, s.map((x) => /* @__PURE__ */ e.createElement(
    Ut,
    {
      key: x.id,
      item: x,
      activeItemId: c,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: R,
      setItemRef: h,
      onItemFocus: L,
      groupStates: o,
      flyoutGroupId: C,
      onOpenFlyout: w,
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
}, bl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", s = t.align ?? "stretch", c = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${s}`,
    c ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o })));
}, _l = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, gl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, s = t.gap ?? "default", c = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: i }, c.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, vl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", s = t.padding ?? "default", c = t.headerActions ?? [], i = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, El = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, s = t.children ?? [], c = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, s.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))));
}, { useCallback: Cl } = e, wl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = Cl((c) => {
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
}, { useCallback: yl } = e, kl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = t.activeItemId, c = yl((i) => {
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
}, { useCallback: St, useRef: Sl } = e, Nl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), Tl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.closeOnBackdrop !== !1, c = t.child, i = Sl(null), u = St(() => {
    n("close");
  }, [n]), r = St((o) => {
    s && o.target === o.currentTarget && u();
  }, [s, u]);
  return a ? /* @__PURE__ */ e.createElement(ht, null, /* @__PURE__ */ e.createElement(Nl, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: Rl, useRef: Dl } = e, Ll = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Dl(n.length);
  return Rl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((s) => /* @__PURE__ */ e.createElement(K, { key: s.controlId, control: s })));
}, { useCallback: Ve, useRef: Pe, useState: ze } = e, xl = ({ onClose: l }) => (de("ESCAPE", () => (l(), !0)), null), Il = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Pl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], jl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Il), s = t.title ?? "", c = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, d = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, b] = ze(null), [k, C] = ze(null), [v, w] = ze(null), I = Pe(null), [R, _] = ze(!1), E = Pe(null), h = Pe(null), L = Pe(null), S = Pe(null), N = Pe(null), B = Ve(() => {
    n("close");
  }, [n]);
  bt(!0, S, "field");
  const F = Ve(($, A) => {
    A.preventDefault();
    const P = S.current;
    if (!P) return;
    const X = P.getBoundingClientRect(), m = !I.current, T = I.current ?? { x: X.left, y: X.top };
    m && (I.current = T, w(T)), N.current = {
      dir: $,
      startX: A.clientX,
      startY: A.clientY,
      startW: X.width,
      startH: X.height,
      startPos: { ...T },
      symmetric: m
    };
    const z = (q) => {
      const M = N.current;
      if (!M) return;
      const ee = q.clientX - M.startX, oe = q.clientY - M.startY;
      let ne = M.startW, he = M.startH, ve = 0, Ee = 0;
      M.symmetric ? (M.dir.includes("e") && (ne = M.startW + 2 * ee), M.dir.includes("w") && (ne = M.startW - 2 * ee), M.dir.includes("s") && (he = M.startH + 2 * oe), M.dir.includes("n") && (he = M.startH - 2 * oe)) : (M.dir.includes("e") && (ne = M.startW + ee), M.dir.includes("w") && (ne = M.startW - ee, ve = ee), M.dir.includes("s") && (he = M.startH + oe), M.dir.includes("n") && (he = M.startH - oe, Ee = oe));
      const ke = Math.max(200, ne), Se = Math.max(100, he);
      M.symmetric ? (ve = (M.startW - ke) / 2, Ee = (M.startH - Se) / 2) : (M.dir.includes("w") && ke === 200 && (ve = M.startW - 200), M.dir.includes("n") && Se === 100 && (Ee = M.startH - 100)), h.current = ke, L.current = Se, b(ke), C(Se);
      const Ie = {
        x: M.startPos.x + ve,
        y: M.startPos.y + Ee
      };
      I.current = Ie, w(Ie);
    }, W = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", W);
      const q = h.current, M = L.current;
      (q != null || M != null) && n("resize", {
        ...q != null ? { width: Math.round(q) } : {},
        ...M != null ? { height: Math.round(M) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", W);
  }, [n]), x = Ve(($) => {
    if ($.button !== 0 || $.target.closest("button")) return;
    $.preventDefault();
    const A = S.current;
    if (!A) return;
    const P = A.getBoundingClientRect(), X = I.current ?? { x: P.left, y: P.top }, m = $.clientX - X.x, T = $.clientY - X.y, z = (q) => {
      const M = window.innerWidth, ee = window.innerHeight;
      let oe = q.clientX - m, ne = q.clientY - T;
      const he = A.offsetWidth, ve = A.offsetHeight;
      oe + he > M && (oe = M - he), ne + ve > ee && (ne = ee - ve), oe < 0 && (oe = 0), ne < 0 && (ne = 0);
      const Ee = { x: oe, y: ne };
      I.current = Ee, w(Ee);
    }, W = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", W);
  }, []), O = Ve(() => {
    var $, A;
    if (R) {
      const P = E.current;
      P && (w(P.x !== -1 ? { x: P.x, y: P.y } : null), b(P.w), C(P.h)), _(!1);
    } else {
      const P = S.current, X = P == null ? void 0 : P.getBoundingClientRect();
      E.current = {
        x: (($ = I.current) == null ? void 0 : $.x) ?? (X == null ? void 0 : X.left) ?? -1,
        y: ((A = I.current) == null ? void 0 : A.y) ?? (X == null ? void 0 : X.top) ?? -1,
        w: g ?? (X == null ? void 0 : X.width) ?? null,
        h: k ?? null
      }, _(!0), w({ x: 0, y: 0 }), b(null), C(null);
    }
  }, [R, g, k]), Q = R ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : c,
    ...k != null ? { height: k + "px" } : i != null ? { height: i } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(ht, { modal: !0 }, /* @__PURE__ */ e.createElement(xl, { onClose: B }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Q,
      ref: S,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": H
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${R ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: R ? void 0 : x,
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
          title: R ? a["js.window.restore"] : a["js.window.maximize"]
        },
        R ? (
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
          onClick: B,
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
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), d.map(($, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: $ }))),
    r && !R && Pl.map(($) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: $,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${$}`,
        onMouseDown: (A) => F($, A)
      }
    ))
  ));
}, { useCallback: Ml } = e, Al = {
  "js.drawer.close": "Close"
}, Bl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(Al), s = t.open === !0, c = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Ml(() => {
    n("close");
  }, [n]);
  xe(s, { ESCAPE: o });
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
}, { useCallback: Ol } = e, Fl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.child, s = Ol((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: s }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: $l, useEffect: Nt, useRef: Ul, useState: Tt } = e, Hl = 250, Wl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.message ?? "", s = t.content ?? "", c = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, d] = Tt(!1), [p, f] = Tt(!1), g = Ul(!1);
  Nt(() => {
    g.current = !1;
  }, [r]);
  const b = $l(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Nt(() => {
    if (!u || i === 0 || p) return;
    const k = setTimeout(b, g.current ? Hl : i);
    return () => clearTimeout(k);
  }, [u, i, p, b]), !u && !o ? null : /* @__PURE__ */ e.createElement(
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
}, { useCallback: tt, useEffect: Rt, useRef: Vl, useState: Dt } = e, zl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.anchorId, c = t.anchorX, i = t.anchorY, u = t.items ?? [], r = Vl(null), [o, d] = Dt({ top: 0, left: 0 }), [p, f] = Dt(0), g = u.filter((v) => v.type === "item" && !v.disabled);
  Rt(() => {
    var h, L;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, w = ((L = r.current) == null ? void 0 : L.offsetWidth) ?? 200;
    if (c != null && i != null) {
      let S = i, N = c;
      S + v > window.innerHeight && (S = Math.max(0, window.innerHeight - v)), N + w > window.innerWidth && (N = Math.max(0, window.innerWidth - w)), d({ top: S, left: N }), f(0);
      return;
    }
    if (!s) return;
    const I = document.getElementById(s);
    if (!I) return;
    const R = I.getBoundingClientRect();
    let _ = R.bottom + 4, E = R.left;
    _ + v > window.innerHeight && (_ = R.top - v - 4), E + w > window.innerWidth && (E = R.right - w), d({ top: _, left: E }), f(0);
  }, [a, s, c, i]);
  const b = tt(() => {
    n("close");
  }, [n]), k = tt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  Rt(() => {
    if (!a) return;
    const v = (w) => {
      r.current && !r.current.contains(w.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, b]);
  const C = tt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), b();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((w) => (w + 1) % g.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((w) => (w - 1 + g.length) % g.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const w = g[p];
      w && k(w.id);
    }
  }, [b, k, g, p]);
  return bt(a, r), a ? /* @__PURE__ */ e.createElement(
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
    u.map((v, w) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: w, className: "tlMenu__separator" });
      const R = g.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (R ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: R ? 0 : -1,
          onClick: () => k(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Kl = 768, Yl = ({ controlId: l }) => {
  const t = G(), n = ae();
  e.useEffect(() => {
    const o = window.matchMedia(`(max-width: ${Kl}px)`), d = (f) => {
      n("reportDisplayClass", { displayClass: f ? "COMPACT" : "REGULAR" });
    };
    d(o.matches);
    const p = (f) => d(f.matches);
    return o.addEventListener("change", p), () => o.removeEventListener("change", p);
  }, [n]);
  const a = t.header, s = t.content, c = t.footer, i = t.snackbar, u = t.dialogManager, r = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: s })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: c })), /* @__PURE__ */ e.createElement(K, { control: i }), u && /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, Gl = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", s = t.hasTooltip === !0, c = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      "data-tooltip": s ? "key:tooltip" : void 0
    },
    n
  );
}, Xl = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: s }) => (de("ArrowUp", () => (n("up", !1, !1), !0)), de("ArrowDown", () => (n("down", !1, !1), !0)), de("Home", () => (n("home", !1, !1), !0)), de("End", () => (n("end", !1, !1), !0)), de("PageUp", () => (n("pageUp", !1, !1), !0)), de("PageDown", () => (n("pageDown", !1, !1), !0)), de("Shift+ArrowUp", () => (n("up", l, !1), !0)), de("Shift+ArrowDown", () => (n("down", l, !1), !0)), de("Shift+Home", () => (n("home", l, !1), !0)), de("Shift+End", () => (n("end", l, !1), !0)), de("Shift+PageUp", () => (n("pageUp", l, !1), !0)), de("Shift+PageDown", () => (n("pageDown", l, !1), !0)), de("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), de("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), de("Space", () => t < 0 ? !1 : (a(), !0)), de("Ctrl+A", () => l ? (s(), !0) : !1), null), ql = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, Lt = 50, Zl = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function nt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, Zl));
}
const mt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', Ql = mt + ", button:not([disabled]), a[href]";
function Ht(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function lt(l, t, n = {}) {
  const a = Ht(l, t);
  if (n.col) {
    const c = a.find((u) => u.dataset.col === n.col), i = c == null ? void 0 : c.querySelector(mt);
    if (i) return i;
  }
  if (n.col)
    return null;
  const s = n.last ? [...a].reverse() : a;
  for (const c of s) {
    const i = c.querySelector(mt);
    if (i) return i;
  }
  return null;
}
const Jl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(ql), s = e.useRef(null);
  e.useEffect(() => {
    const y = s.current;
    if (!y) return;
    const D = (V) => {
      const J = V.detail;
      let te = J.target;
      for (; te && te !== y; ) {
        const se = te.dataset.row, re = te.dataset.col;
        if (se != null && re != null) {
          J.resolved = { key: se + "|" + re };
          return;
        }
        te = te.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", D), () => y.removeEventListener("tl-tooltip-resolve", D);
  }, []);
  const c = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", d = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, b = t.columnSelect ?? !1, k = e.useMemo(
    () => c.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [c]
  ), C = o === "multi", v = 40, w = 20, I = e.useRef(null), R = e.useRef(null), _ = e.useRef(null), E = e.useRef(null), [h, L] = e.useState({}), S = e.useRef(null), N = e.useRef(!1), B = e.useRef(null), [F, x] = e.useState(null), [O, Q] = e.useState(null);
  e.useEffect(() => {
    S.current || L({});
  }, [c]);
  const H = e.useCallback((y) => h[y.name] ?? y.width, [h]), $ = e.useMemo(() => {
    const y = [];
    let D = C && f > 0 ? v : 0;
    for (let V = 0; V < f && V < c.length; V++)
      y.push(D), D += H(c[V]);
    return y;
  }, [c, f, C, v, H]), A = i * r, P = e.useRef(null), X = e.useCallback((y, D, V) => {
    V.preventDefault(), V.stopPropagation(), S.current = { column: y, startX: V.clientX, startWidth: D };
    let J = V.clientX, te = 0;
    const se = () => {
      const ue = S.current;
      if (!ue) return;
      const me = Math.max(Lt, ue.startWidth + (J - ue.startX) + te);
      L((we) => ({ ...we, [ue.column]: me }));
    }, re = () => {
      const ue = R.current, me = I.current;
      if (!ue || !S.current) return;
      const we = ue.getBoundingClientRect(), Ne = 40, vt = 8, Zt = ue.scrollLeft;
      J > we.right - Ne ? ue.scrollLeft += vt : J < we.left + Ne && (ue.scrollLeft = Math.max(0, ue.scrollLeft - vt));
      const Et = ue.scrollLeft - Zt;
      Et !== 0 && (me && (me.scrollLeft = ue.scrollLeft), te += Et, se()), P.current = requestAnimationFrame(re);
    };
    P.current = requestAnimationFrame(re);
    const _e = (ue) => {
      J = ue.clientX, se();
    }, ge = (ue) => {
      document.removeEventListener("mousemove", _e), document.removeEventListener("mouseup", ge), P.current !== null && (cancelAnimationFrame(P.current), P.current = null);
      const me = S.current;
      if (me) {
        const we = Math.max(Lt, me.startWidth + (ue.clientX - me.startX) + te);
        n("columnResize", { column: me.column, width: we }), S.current = null, N.current = !0, requestAnimationFrame(() => {
          N.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", _e), document.addEventListener("mouseup", ge);
  }, [n]), m = e.useCallback(() => {
    I.current && R.current && (I.current.scrollLeft = R.current.scrollLeft), _.current !== null && clearTimeout(_.current), _.current = window.setTimeout(() => {
      const y = R.current;
      if (!y) return;
      const D = y.scrollTop, V = Math.ceil(y.clientHeight / r), J = Math.floor(D / r);
      n("scroll", { start: J, count: V });
    }, 80);
  }, [n, r]), T = e.useCallback((y, D, V) => {
    if (N.current) return;
    let J;
    !D || D === "desc" ? J = "asc" : J = "desc";
    const te = V.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: J, mode: te });
  }, [n]), z = e.useCallback((y, D) => {
    B.current = y, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", y);
  }, []), W = e.useCallback((y, D) => {
    if (!B.current || B.current === y) {
      x(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const V = D.currentTarget.getBoundingClientRect(), J = D.clientX < V.left + V.width / 2 ? "left" : "right";
    x({ column: y, side: J });
  }, []), q = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const D = B.current;
    if (!D || !F) {
      B.current = null, x(null);
      return;
    }
    let V = c.findIndex((te) => te.name === F.column);
    if (V < 0) {
      B.current = null, x(null);
      return;
    }
    const J = c.findIndex((te) => te.name === D);
    F.side === "right" && V++, J < V && V--, n("columnReorder", { column: D, targetIndex: V }), B.current = null, x(null);
  }, [c, F, n]), M = e.useCallback(() => {
    B.current = null, x(null);
  }, []), ee = e.useCallback((y, D) => {
    var te, se, re, _e;
    const V = window.getSelection();
    if (V && !V.isCollapsed && D.currentTarget.contains(V.anchorNode))
      return;
    if (!nt(D) && ((te = R.current) == null || te.focus({ preventScroll: !0 }), !D.ctrlKey && !D.metaKey && !D.shiftKey)) {
      const ge = (_e = (re = (se = D.target) == null ? void 0 : se.closest) == null ? void 0 : re.call(se, "[data-col]")) == null ? void 0 : _e.getAttribute("data-col");
      E.current = { index: y, col: ge ?? void 0 };
    }
    const J = u.find((ge) => ge.index === y);
    nt(D) && (J != null && J.selected) && !D.ctrlKey && !D.metaKey && !D.shiftKey || n("select", {
      rowIndex: y,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [n, u]), oe = e.useCallback((y, D, V) => {
    n("moveSelection", { direction: y, extend: D, move: V });
  }, [n]), ne = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: C, shiftKey: !1 });
  }, [n, p, C]), he = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), ve = e.useCallback(
    () => !!s.current && s.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const y = R.current;
    if (!y)
      return;
    const D = p * r, V = D + r;
    D < y.scrollTop ? y.scrollTop = D : V > y.scrollTop + y.clientHeight && (y.scrollTop = V - y.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const y = E.current, D = R.current;
    if (!y || !D)
      return;
    const V = u.find((se) => se.index === y.index);
    if (!V || !lt(D, V.id))
      return;
    E.current = null;
    const J = document.activeElement;
    if (J && J !== document.body && !D.contains(J))
      return;
    const te = lt(D, V.id, { col: y.col, last: y.last });
    te && (te.focus({ preventScroll: !0 }), te instanceof HTMLInputElement && te.select());
  }, [u]);
  const Ee = e.useCallback((y) => {
    if (y.key !== "Tab")
      return;
    const D = R.current, V = document.activeElement;
    if (!D || !V || !D.contains(V))
      return;
    const J = V.closest("[data-row][data-col]");
    if (!J)
      return;
    const te = J.dataset.row, se = u.find((Ne) => Ne.id === te);
    if (!se)
      return;
    const re = Ht(D, te).flatMap((Ne) => Array.from(Ne.querySelectorAll(Ql))), _e = re.indexOf(V);
    if (_e < 0)
      return;
    const ge = !y.shiftKey;
    if (!(ge ? _e === re.length - 1 : _e === 0))
      return;
    const me = ge ? se.index + 1 : se.index - 1;
    if (me < 0 || me >= i)
      return;
    const we = u.find((Ne) => Ne.index === me);
    we && lt(D, we.id) || (y.preventDefault(), E.current = { index: me, last: !ge }, n("select", { rowIndex: me, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), ke = e.useCallback((y, D) => {
    D.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), Se = e.useCallback(() => {
    const y = d === i && i > 0;
    n("selectAll", { selected: !y });
  }, [n, d, i]), Ie = e.useCallback((y, D, V) => {
    V.stopPropagation(), n("expand", { rowIndex: y, expanded: D });
  }, [n]), He = e.useCallback((y, D) => {
    D.preventDefault(), Q({ x: D.clientX, y: D.clientY, colIdx: y });
  }, []), j = e.useCallback(() => {
    O && (n("setFrozenColumnCount", { count: O.colIdx + 1 }), Q(null));
  }, [O, n]), Y = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), Q(null);
  }, [n]);
  e.useEffect(() => {
    if (!O) return;
    const y = () => Q(null);
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [O]), xe(!!O, { ESCAPE: () => Q(null) });
  const le = e.useCallback((y, D) => {
    D.stopPropagation(), D.preventDefault(), n("openFilter", { column: y });
  }, [n]), ce = e.useCallback((y) => {
    y.stopPropagation(), y.preventDefault(), n("openColumnSelect", {});
  }, [n]), De = c.reduce((y, D) => y + H(D), 0) + (C ? v : 0), Qe = b ? 32 : 0, Xt = d === i && i > 0, gt = d > 0 && d < i, qt = e.useCallback((y) => {
    y && (y.indeterminate = gt);
  }, [gt]);
  return /* @__PURE__ */ e.createElement(ht, { active: ve }, /* @__PURE__ */ e.createElement(
    Xl,
    {
      isMulti: C,
      cursorIndex: p,
      onMove: oe,
      onToggle: ne,
      onSelectAll: he
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (y) => {
        if (!B.current) return;
        y.preventDefault();
        const D = R.current, V = I.current;
        if (!D) return;
        const J = D.getBoundingClientRect(), te = 40, se = 8;
        y.clientX < J.left + te ? D.scrollLeft = Math.max(0, D.scrollLeft - se) : y.clientX > J.right - te && (D.scrollLeft += se), V && (V.scrollLeft = D.scrollLeft);
      },
      onDrop: q
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: I }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: De, paddingRight: Qe } }, C && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: v,
          minWidth: v,
          ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          B.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== B.current && x({ column: c[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: qt,
          className: "tlTableView__checkbox",
          checked: Xt,
          onChange: Se
        }
      )
    ), c.map((y, D) => {
      const V = H(y);
      c.length - 1;
      let J = "tlTableView__headerCell";
      y.sortable && (J += " tlTableView__headerCell--sortable"), F && F.column === y.name && (J += " tlTableView__headerCell--dragOver-" + F.side);
      const te = D < f, se = D === f - 1;
      return te && (J += " tlTableView__headerCell--frozen"), se && (J += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: J,
          style: {
            width: V,
            minWidth: V,
            position: te ? "sticky" : "relative",
            ...te ? { left: $[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (re) => T(y.name, y.sortDirection, re) : void 0,
          onContextMenu: (re) => He(D, re),
          onDragStart: (re) => z(y.name, re),
          onDragOver: (re) => W(y.name, re),
          onDrop: q,
          onDragEnd: M
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, y.label),
        y.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (y.filterActive ? " tlTableView__filterButton--active" : ""),
            title: a["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: y.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (re) => re.stopPropagation(),
            onClick: (re) => le(y.name, re)
          },
          /* @__PURE__ */ e.createElement("i", { className: y.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", k > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => X(y.name, V, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (B.current && c.length > 0) {
            const D = c[c.length - 1];
            D.name !== B.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", x({ column: D.name, side: "right" }));
          }
        },
        onDrop: q
      }
    ))), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: ce
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: R,
        className: "tlTableView__body",
        onScroll: m,
        onKeyDown: Ee,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: A, position: "relative", width: De, paddingRight: Qe } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : "") + (y.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: y.index * r,
            height: r,
            width: De,
            paddingRight: Qe,
            ...y.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (D) => {
            (D.shiftKey || D.ctrlKey || D.metaKey || D.detail > 1) && !nt(D) && D.preventDefault();
          },
          onClick: (D) => ee(y.index, D)
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
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (D) => ke(y.index, D),
              tabIndex: -1
            }
          )
        ),
        c.map((D, V) => {
          const J = H(D), te = V === c.length - 1, se = V < f, re = V === f - 1;
          let _e = "tlTableView__cell";
          se && (_e += " tlTableView__cell--frozen"), re && (_e += " tlTableView__cell--frozenLast");
          const ge = g && V === 0, ue = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: _e,
              "data-row": y.id,
              "data-col": D.name,
              style: {
                ...te && !se ? { flex: "1 0 auto", minWidth: J } : { width: J, minWidth: J },
                ...se ? { position: "sticky", left: $[V], zIndex: 2 } : {}
              }
            },
            ge ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ue * w } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => Ie(y.index, !y.expanded, me)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), y.cells[D.name] && /* @__PURE__ */ e.createElement(K, { control: y.cells[D.name] })) : y.cells[D.name] && /* @__PURE__ */ e.createElement(K, { control: y.cells[D.name] })
          );
        })
      )))
    ),
    O && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: O.y, left: O.x, zIndex: 1e4 },
        onMouseDown: (y) => y.stopPropagation()
      },
      O.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: j }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Y }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, ea = {
  "js.table.columnSearch": "Find column"
}, ta = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(ea), s = t.entries ?? [], c = s.filter((_) => _.visible).length, [i, u] = e.useState(""), r = i.trim().toLowerCase(), o = r ? s.filter((_) => _.label.toLowerCase().includes(r)) : s, d = e.useRef(null), p = e.useRef(null), [f, g] = e.useState(null), b = e.useCallback((_) => {
    p.current = _, g(_);
  }, []), k = e.useCallback((_, E) => {
    n("columnVisible", { column: _, visible: E });
  }, [n]), C = e.useCallback((_, E) => {
    d.current = _, E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", _);
  }, []), v = e.useCallback((_, E) => {
    if (!d.current || d.current === _) {
      b(null);
      return;
    }
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const h = E.currentTarget.getBoundingClientRect(), L = E.clientY < h.top + h.height / 2 ? "top" : "bottom";
    b({ name: _, side: L });
  }, [b]), w = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), I = e.useCallback((_) => {
    _.preventDefault();
    const E = d.current, h = p.current;
    if (d.current = null, b(null), !E || !h)
      return;
    const L = s.findIndex((B) => B.name === h.name), S = s.findIndex((B) => B.name === E);
    if (L < 0 || S < 0)
      return;
    let N = h.side === "top" ? L : L + 1;
    S < N && N--, N !== S && n("columnReorder", { column: E, targetIndex: N });
  }, [s, n, b]), R = s.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: I }, R && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: i,
      onChange: (_) => u(_.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (R ? " tlColumnSelect__list--fixed" : "") }, o.map((_) => {
    const E = _.visible && c <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === _.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: _.name,
        className: h,
        draggable: !0,
        onDragStart: (L) => C(_.name, L),
        onDragOver: (L) => v(_.name, L),
        onDrop: I,
        onDragEnd: w
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: _.visible,
          disabled: E,
          onChange: (L) => k(_.name, L.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, _.label))
    );
  })));
}, na = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Wt = e.createContext(na), { useMemo: la, useRef: aa, useState: ra, useEffect: oa } = e, sa = 320, ca = "TLTableView", ia = "TLPanel", ua = ({ controlId: l }) => {
  var C;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", s = t.readOnly === !0, c = t.children ?? [], i = t.noModelMessage, u = aa(null), [r, o] = ra(
    a === "top" ? "top" : "side"
  );
  oa(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const w = new ResizeObserver((I) => {
      for (const R of I) {
        const E = R.contentRect.width / n;
        o(E < sa ? "top" : "side");
      }
    });
    return w.observe(v), () => w.disconnect();
  }, [a, n]);
  const d = la(() => ({
    readOnly: s,
    resolvedLabelPosition: r
  }), [s, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = c.length === 1 ? c[0] : void 0, b = !!g && (g.module === ca || g.module === ia && ((C = g.state) == null ? void 0 : C.bare) === !0), k = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Wt.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: f, ref: u }, c.map((v, w) => /* @__PURE__ */ e.createElement(K, { key: w, control: v }))));
}, { useCallback: da } = e, ma = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, pa = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ie(ma), s = t.headerControl ?? null, c = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, d = t.children ?? [], p = s != null || c.length > 0 || i, f = da(() => {
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: s })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((b, k) => /* @__PURE__ */ e.createElement(K, { key: k, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((b, k) => /* @__PURE__ */ e.createElement(K, { key: k, control: b }))));
}, { useContext: fa, useState: ha, useCallback: ba } = e, _a = ({ controlId: l }) => {
  const t = G(), n = fa(Wt), a = t.label ?? "", s = t.required === !0, c = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, d = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, g = t.visible !== !1, b = t.hasTooltip === !0, k = t.field, C = n.readOnly, [v, w] = ha(!1), I = ba(() => w((L) => !L), []), R = p === "hidden", _ = c != null, E = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    C ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    !_ && E ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: g ? void 0 : { display: "none" } }, !R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: k })), !C && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, c)), !C && !_ && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((L, S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, L)))), !C && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ga = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.iconCss, s = t.iconSrc, c = t.label, i = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : s ? /* @__PURE__ */ e.createElement("img", { src: s, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, o, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), p = e.useCallback((b) => {
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
}, va = 20, Ea = () => {
  var E;
  const l = G(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), d = e.useRef(null), p = ((E = n.find((h) => h.selected)) == null ? void 0 : E.id) ?? null;
  e.useEffect(() => {
    var L;
    if (p == null)
      return;
    const h = (L = d.current) == null ? void 0 : L.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, L) => {
    t(L ? "collapse" : "expand", { nodeId: h });
  }, [t]), g = e.useCallback((h, L) => {
    var N;
    const S = window.getSelection();
    S && !S.isCollapsed && L.currentTarget.contains(S.anchorNode) || ((N = d.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: L.ctrlKey || L.metaKey,
      shiftKey: L.shiftKey
    }));
  }, [t]), b = e.useCallback((h, L) => {
    L.preventDefault(), t("contextMenu", { nodeId: h, x: L.clientX, y: L.clientY });
  }, [t]), k = e.useRef(null), C = e.useCallback((h, L) => {
    const S = L.getBoundingClientRect(), N = h.clientY - S.top, B = S.height / 3;
    return N < B ? "above" : N > B * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, L) => {
    L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", h);
  }, []), w = e.useCallback((h, L) => {
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const S = C(L, L.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: S }), k.current = null;
    }, 50);
  }, [t, C]), I = e.useCallback((h, L) => {
    L.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const S = C(L, L.currentTarget);
    t("drop", { nodeId: h, position: S });
  }, [t, C]), R = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
  }, [t]), _ = e.useCallback((h) => {
    if (n.length === 0) return;
    let L = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), L = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), L = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expandable && !S.expanded) {
            t("expand", { nodeId: S.id });
            return;
          } else S.expanded && (L = r + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const S = n[r];
          if (S.expanded) {
            t("collapse", { nodeId: S.id });
            return;
          } else {
            const N = S.depth;
            for (let B = r - 1; B >= 0; B--)
              if (n[B].depth < N) {
                L = B;
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
        h.preventDefault(), L = 0;
        break;
      case "End":
        h.preventDefault(), L = n.length - 1;
        break;
      default:
        return;
    }
    L !== r && o(L);
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
    n.map((h, L) => /* @__PURE__ */ e.createElement(
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
          L === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * va },
        draggable: s,
        onMouseDown: (S) => {
          (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && S.preventDefault();
        },
        onClick: (S) => g(h.id, S),
        onContextMenu: (S) => b(h.id, S),
        onDragStart: (S) => v(h.id, S),
        onDragOver: c ? (S) => w(h.id, S) : void 0,
        onDrop: c ? (S) => I(h.id, S) : void 0,
        onDragEnd: R
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (S) => {
            S.stopPropagation(), f(h.id, h.expanded);
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
var at = { exports: {} }, pe = {}, rt = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var xt;
function Ca() {
  if (xt) return Z;
  xt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), s = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), i = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), d = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
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
  }, k = Object.assign, C = {};
  function v(m, T, z) {
    this.props = m, this.context = T, this.refs = C, this.updater = z || b;
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
  function w() {
  }
  w.prototype = v.prototype;
  function I(m, T, z) {
    this.props = m, this.context = T, this.refs = C, this.updater = z || b;
  }
  var R = I.prototype = new w();
  R.constructor = I, k(R, v.prototype), R.isPureReactComponent = !0;
  var _ = Array.isArray;
  function E() {
  }
  var h = { H: null, A: null, T: null, S: null }, L = Object.prototype.hasOwnProperty;
  function S(m, T, z) {
    var W = z.ref;
    return {
      $$typeof: l,
      type: m,
      key: T,
      ref: W !== void 0 ? W : null,
      props: z
    };
  }
  function N(m, T) {
    return S(m.type, T, m.props);
  }
  function B(m) {
    return typeof m == "object" && m !== null && m.$$typeof === l;
  }
  function F(m) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + m.replace(/[=:]/g, function(z) {
      return T[z];
    });
  }
  var x = /\/+/g;
  function O(m, T) {
    return typeof m == "object" && m !== null && m.key != null ? F("" + m.key) : T.toString(36);
  }
  function Q(m) {
    switch (m.status) {
      case "fulfilled":
        return m.value;
      case "rejected":
        throw m.reason;
      default:
        switch (typeof m.status == "string" ? m.then(E, E) : (m.status = "pending", m.then(
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
  function H(m, T, z, W, q) {
    var M = typeof m;
    (M === "undefined" || M === "boolean") && (m = null);
    var ee = !1;
    if (m === null) ee = !0;
    else
      switch (M) {
        case "bigint":
        case "string":
        case "number":
          ee = !0;
          break;
        case "object":
          switch (m.$$typeof) {
            case l:
            case t:
              ee = !0;
              break;
            case d:
              return ee = m._init, H(
                ee(m._payload),
                T,
                z,
                W,
                q
              );
          }
      }
    if (ee)
      return q = q(m), ee = W === "" ? "." + O(m, 0) : W, _(q) ? (z = "", ee != null && (z = ee.replace(x, "$&/") + "/"), H(q, T, z, "", function(he) {
        return he;
      })) : q != null && (B(q) && (q = N(
        q,
        z + (q.key == null || m && m.key === q.key ? "" : ("" + q.key).replace(
          x,
          "$&/"
        ) + "/") + ee
      )), T.push(q)), 1;
    ee = 0;
    var oe = W === "" ? "." : W + ":";
    if (_(m))
      for (var ne = 0; ne < m.length; ne++)
        W = m[ne], M = oe + O(W, ne), ee += H(
          W,
          T,
          z,
          M,
          q
        );
    else if (ne = g(m), typeof ne == "function")
      for (m = ne.call(m), ne = 0; !(W = m.next()).done; )
        W = W.value, M = oe + O(W, ne++), ee += H(
          W,
          T,
          z,
          M,
          q
        );
    else if (M === "object") {
      if (typeof m.then == "function")
        return H(
          Q(m),
          T,
          z,
          W,
          q
        );
      throw T = String(m), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(m).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return ee;
  }
  function $(m, T, z) {
    if (m == null) return m;
    var W = [], q = 0;
    return H(m, W, "", "", function(M) {
      return T.call(z, M, q++);
    }), W;
  }
  function A(m) {
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
    map: $,
    forEach: function(m, T, z) {
      $(
        m,
        function() {
          T.apply(this, arguments);
        },
        z
      );
    },
    count: function(m) {
      var T = 0;
      return $(m, function() {
        T++;
      }), T;
    },
    toArray: function(m) {
      return $(m, function(T) {
        return T;
      }) || [];
    },
    only: function(m) {
      if (!B(m))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return m;
    }
  };
  return Z.Activity = p, Z.Children = X, Z.Component = v, Z.Fragment = n, Z.Profiler = s, Z.PureComponent = I, Z.StrictMode = a, Z.Suspense = r, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(m) {
      return h.H.useMemoCache(m);
    }
  }, Z.cache = function(m) {
    return function() {
      return m.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(m, T, z) {
    if (m == null)
      throw Error(
        "The argument must be a React element, but you passed " + m + "."
      );
    var W = k({}, m.props), q = m.key;
    if (T != null)
      for (M in T.key !== void 0 && (q = "" + T.key), T)
        !L.call(T, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && T.ref === void 0 || (W[M] = T[M]);
    var M = arguments.length - 2;
    if (M === 1) W.children = z;
    else if (1 < M) {
      for (var ee = Array(M), oe = 0; oe < M; oe++)
        ee[oe] = arguments[oe + 2];
      W.children = ee;
    }
    return S(m.type, q, W);
  }, Z.createContext = function(m) {
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
  }, Z.createElement = function(m, T, z) {
    var W, q = {}, M = null;
    if (T != null)
      for (W in T.key !== void 0 && (M = "" + T.key), T)
        L.call(T, W) && W !== "key" && W !== "__self" && W !== "__source" && (q[W] = T[W]);
    var ee = arguments.length - 2;
    if (ee === 1) q.children = z;
    else if (1 < ee) {
      for (var oe = Array(ee), ne = 0; ne < ee; ne++)
        oe[ne] = arguments[ne + 2];
      q.children = oe;
    }
    if (m && m.defaultProps)
      for (W in ee = m.defaultProps, ee)
        q[W] === void 0 && (q[W] = ee[W]);
    return S(m, M, q);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(m) {
    return { $$typeof: u, render: m };
  }, Z.isValidElement = B, Z.lazy = function(m) {
    return {
      $$typeof: d,
      _payload: { _status: -1, _result: m },
      _init: A
    };
  }, Z.memo = function(m, T) {
    return {
      $$typeof: o,
      type: m,
      compare: T === void 0 ? null : T
    };
  }, Z.startTransition = function(m) {
    var T = h.T, z = {};
    h.T = z;
    try {
      var W = m(), q = h.S;
      q !== null && q(z, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(E, P);
    } catch (M) {
      P(M);
    } finally {
      T !== null && z.types !== null && (T.types = z.types), h.T = T;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, Z.use = function(m) {
    return h.H.use(m);
  }, Z.useActionState = function(m, T, z) {
    return h.H.useActionState(m, T, z);
  }, Z.useCallback = function(m, T) {
    return h.H.useCallback(m, T);
  }, Z.useContext = function(m) {
    return h.H.useContext(m);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(m, T) {
    return h.H.useDeferredValue(m, T);
  }, Z.useEffect = function(m, T) {
    return h.H.useEffect(m, T);
  }, Z.useEffectEvent = function(m) {
    return h.H.useEffectEvent(m);
  }, Z.useId = function() {
    return h.H.useId();
  }, Z.useImperativeHandle = function(m, T, z) {
    return h.H.useImperativeHandle(m, T, z);
  }, Z.useInsertionEffect = function(m, T) {
    return h.H.useInsertionEffect(m, T);
  }, Z.useLayoutEffect = function(m, T) {
    return h.H.useLayoutEffect(m, T);
  }, Z.useMemo = function(m, T) {
    return h.H.useMemo(m, T);
  }, Z.useOptimistic = function(m, T) {
    return h.H.useOptimistic(m, T);
  }, Z.useReducer = function(m, T, z) {
    return h.H.useReducer(m, T, z);
  }, Z.useRef = function(m) {
    return h.H.useRef(m);
  }, Z.useState = function(m) {
    return h.H.useState(m);
  }, Z.useSyncExternalStore = function(m, T, z) {
    return h.H.useSyncExternalStore(
      m,
      T,
      z
    );
  }, Z.useTransition = function() {
    return h.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var It;
function wa() {
  return It || (It = 1, rt.exports = Ca()), rt.exports;
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
var Pt;
function ya() {
  if (Pt) return pe;
  Pt = 1;
  var l = wa();
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
  return pe.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, pe.createPortal = function(r, o) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return c(r, o, null, d);
  }, pe.flushSync = function(r) {
    var o = i.T, d = a.p;
    try {
      if (i.T = null, a.p = 2, r) return r();
    } finally {
      i.T = o, a.p = d, a.d.f();
    }
  }, pe.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, pe.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, pe.preinit = function(r, o) {
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
  }, pe.preinitModule = function(r, o) {
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
  }, pe.preload = function(r, o) {
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
  }, pe.preloadModule = function(r, o) {
    if (typeof r == "string")
      if (o) {
        var d = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: d,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, pe.requestFormReset = function(r) {
    a.d.r(r);
  }, pe.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, pe.useFormState = function(r, o, d) {
    return i.H.useFormState(r, o, d);
  }, pe.useFormStatus = function() {
    return i.H.useHostTransitionStatus();
  }, pe.version = "19.2.4", pe;
}
var jt;
function ka() {
  if (jt) return at.exports;
  jt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), at.exports = ya(), at.exports;
}
var Vt = ka();
const { useState: Te, useCallback: fe, useRef: $e, useEffect: je, useMemo: pt } = e;
function _t({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Sa({
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
  const d = fe(
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
    /* @__PURE__ */ e.createElement(_t, { image: l.image }),
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
function Na({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: s,
  id: c
}) {
  const i = fe(() => a(l.value), [a, l.value]), u = pt(() => {
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
    /* @__PURE__ */ e.createElement(_t, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Ta = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], s = t.multiSelect === !0, c = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, d = t.options ?? [], p = t.emptyOptionLabel ?? "", f = c && s && !u && r, g = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = g["js.dropdownSelect.nothingFound"], k = fe(
    (j) => g["js.dropdownSelect.removeChip"].replace("{0}", j),
    [g]
  ), [C, v] = Te(!1), [w, I] = Te(""), [R, _] = Te(-1), [E, h] = Te(!1), [L, S] = Te({}), [N, B] = Te(null), [F, x] = Te(null), [O, Q] = Te(null), H = $e(null), $ = $e(null), A = $e(null), P = $e(a);
  P.current = a;
  const X = $e(-1), m = pt(
    () => new Set(a.map((j) => j.value)),
    [a]
  ), T = pt(() => {
    let j = d.filter((Y) => !m.has(Y.value));
    if (w) {
      const Y = w.toLowerCase();
      j = j.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return j;
  }, [d, m, w]);
  je(() => {
    w && T.length === 1 ? _(0) : _(-1);
  }, [T.length, w]), je(() => {
    C && o && $.current && $.current.focus();
  }, [C, o, a]), je(() => {
    var le, ce;
    if (X.current < 0) return;
    const j = X.current;
    X.current = -1;
    const Y = (le = H.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(j, Y.length - 1)].focus() : (ce = H.current) == null || ce.focus();
  }, [a]), je(() => {
    if (!C) return;
    const j = (Y) => {
      H.current && !H.current.contains(Y.target) && A.current && !A.current.contains(Y.target) && (v(!1), I(""));
    };
    return document.addEventListener("mousedown", j), () => document.removeEventListener("mousedown", j);
  }, [C]), je(() => {
    if (!C || !H.current) return;
    const j = H.current.getBoundingClientRect(), Y = window.innerHeight - j.bottom, ce = Y < 300 && j.top > Y;
    S({
      left: j.left,
      width: j.width,
      ...ce ? { bottom: window.innerHeight - j.top } : { top: j.bottom }
    });
  }, [C]);
  const z = fe(async () => {
    if (!(u || !r) && (v(!0), I(""), _(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = fe(() => {
    var j;
    v(!1), I(""), _(-1), (j = H.current) == null || j.focus();
  }, []), q = fe(
    (j) => {
      let Y;
      if (s) {
        const le = d.find((ce) => ce.value === j);
        if (le)
          Y = [...P.current, le];
        else
          return;
      } else {
        const le = d.find((ce) => ce.value === j);
        if (le)
          Y = [le];
        else
          return;
      }
      P.current = Y, n(We, { value: Y.map((le) => le.value) }), s ? (I(""), _(-1)) : W();
    },
    [s, d, n, W]
  ), M = fe(
    (j) => {
      X.current = P.current.findIndex((le) => le.value === j);
      const Y = P.current.filter((le) => le.value !== j);
      P.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), ee = fe(
    (j) => {
      j.stopPropagation(), n(We, { value: [] }), W();
    },
    [n, W]
  ), oe = fe((j) => {
    I(j.target.value);
  }, []), ne = fe(
    (j) => {
      if (!C) {
        if (j.key === "ArrowDown" || j.key === "ArrowUp" || j.key === "Enter" || j.key === " ") {
          if (j.target.tagName === "BUTTON") return;
          j.preventDefault(), j.stopPropagation(), z();
        }
        return;
      }
      switch (j.key) {
        case "ArrowDown":
          j.preventDefault(), j.stopPropagation(), _(
            (Y) => Y < T.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          j.preventDefault(), j.stopPropagation(), _(
            (Y) => Y > 0 ? Y - 1 : T.length - 1
          );
          break;
        case "Enter":
          j.preventDefault(), j.stopPropagation(), R >= 0 && R < T.length && q(T[R].value);
          break;
        case "Escape":
          j.preventDefault(), j.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          w === "" && s && a.length > 0 && M(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      z,
      W,
      T,
      R,
      q,
      w,
      s,
      a,
      M
    ]
  ), he = fe(
    async (j) => {
      j.preventDefault(), h(!1);
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
    },
    [n]
  ), ve = fe(
    (j, Y) => {
      B(j), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(j));
    },
    []
  ), Ee = fe(
    (j, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", N === null || N === j) {
        x(null), Q(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), ce = le.left + le.width / 2, De = Y.clientX < ce ? "before" : "after";
      x(j), Q(De);
    },
    [N]
  ), ke = fe(
    (j) => {
      if (j.preventDefault(), N === null || F === null || O === null || N === F) return;
      const Y = [...P.current], [le] = Y.splice(N, 1);
      let ce = F;
      N < F ? ce = O === "before" ? ce - 1 : ce : ce = O === "before" ? ce : ce + 1, Y.splice(ce, 0, le), P.current = Y, n(We, { value: Y.map((De) => De.value) }), B(null), x(null), Q(null);
    },
    [N, F, O, n]
  ), Se = fe(() => {
    B(null), x(null), Q(null);
  }, []);
  if (je(() => {
    if (R < 0 || !A.current) return;
    const j = A.current.querySelector(
      `[id="${l}-opt-${R}"]`
    );
    j && j.scrollIntoView({ block: "nearest" });
  }, [R, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((j) => /* @__PURE__ */ e.createElement("span", { key: j.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(_t, { image: j.image }), /* @__PURE__ */ e.createElement("span", null, j.label))));
  const Ie = !i && a.length > 0 && !u, He = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: L,
      ...Jt
    },
    (o || E) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: $,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
        onChange: oe,
        onKeyDown: ne,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": R >= 0 ? `${l}-opt-${R}` : void 0,
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
      !o && !E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: he }, g["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && T.map((j, Y) => /* @__PURE__ */ e.createElement(
        Na,
        {
          key: j.value,
          id: `${l}-opt-${Y}`,
          option: j,
          highlighted: Y === R,
          searchTerm: w,
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
      ref: H,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: C ? void 0 : z,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((j, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : F === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : F === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Sa,
        {
          key: j.value,
          option: j,
          removable: !u && (s || !i),
          onRemove: M,
          removeLabel: k(j.label),
          draggable: f,
          onDragStart: f ? (ce) => ve(Y, ce) : void 0,
          onDragOver: f ? (ce) => Ee(Y, ce) : void 0,
          onDrop: f ? ke : void 0,
          onDragEnd: f ? Se : void 0,
          dragClassName: f ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Ie && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: ee,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), He && Vt.createPortal(He, document.body));
}, { useCallback: ot, useRef: Ra } = e, zt = "application/x-tl-color", Da = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: s,
  onReplace: c
}) => {
  const i = Ra(null), u = ot(
    (d) => (p) => {
      i.current = d, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = ot((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), o = ot(
    (d) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(zt);
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
function Kt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ft(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Yt(l) {
  if (!ft(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Gt(l, t, n) {
  const a = (s) => Kt(s).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function La(l, t, n) {
  const a = l / 255, s = t / 255, c = n / 255, i = Math.max(a, s, c), u = Math.min(a, s, c), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (s - c) / r % 6 : i === s ? o = (c - a) / r + 2 : o = (a - s) / r + 4, o *= 60, o < 0 && (o += 360));
  const d = i === 0 ? 0 : r / i;
  return [o, d, i];
}
function xa(l, t, n) {
  const a = n * t, s = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = s, r = 0) : l < 120 ? (i = s, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = s) : l < 240 ? (i = 0, u = s, r = a) : l < 300 ? (i = s, u = 0, r = a) : (i = a, u = 0, r = s), [
    Math.round((i + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function Ia(l) {
  return La(...Yt(l));
}
function st(l, t, n) {
  return Gt(...xa(l, t, n));
}
const { useCallback: Me, useRef: Mt } = e, Pa = ({ color: l, onColorChange: t }) => {
  const [n, a, s] = Ia(l), c = Mt(null), i = Mt(null), u = Me(
    (b, k) => {
      var I;
      const C = (I = c.current) == null ? void 0 : I.getBoundingClientRect();
      if (!C) return;
      const v = Math.max(0, Math.min(1, (b - C.left) / C.width)), w = Math.max(0, Math.min(1, 1 - (k - C.top) / C.height));
      t(st(n, v, w));
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
      var w;
      const k = (w = i.current) == null ? void 0 : w.getBoundingClientRect();
      if (!k) return;
      const v = Math.max(0, Math.min(1, (b - k.top) / k.height)) * 360;
      t(st(v, a, s));
    },
    [a, s, t]
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
  ), g = st(n, 1, 1);
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
function ja(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Ma = {
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
}, { useState: Ke, useCallback: Ce, useEffect: At, useRef: Aa, useLayoutEffect: Ba } = e, Oa = ({
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
  const [o, d] = Ke("palette"), [p, f] = Ke(t), g = Aa(null), b = ie(Ma), [k, C] = Ke(null);
  Ba(() => {
    if (!l.current || !g.current) return;
    const A = l.current.getBoundingClientRect(), P = g.current.getBoundingClientRect();
    let X = A.bottom + 4, m = A.left;
    X + P.height > window.innerHeight && (X = A.top - P.height - 4), m + P.width > window.innerWidth && (m = Math.max(0, A.right - P.width)), C({ top: X, left: m });
  }, [l]);
  const v = p != null, [w, I, R] = v ? Yt(p) : [0, 0, 0], [_, E] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  At(() => {
    E((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), xe(!0, { ESCAPE: u }), At(() => {
    const A = (X) => {
      g.current && !g.current.contains(X.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const h = Ce(
    (A) => (P) => {
      const X = parseInt(P.target.value, 10);
      if (isNaN(X)) return;
      const m = Kt(X);
      f(Gt(A === "r" ? m : w, A === "g" ? m : I, A === "b" ? m : R));
    },
    [w, I, R]
  ), L = Ce(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(zt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), S = Ce((A) => {
    const P = A.target.value;
    E(P), ft(P) && f(P);
  }, []), N = Ce(() => {
    f(null);
  }, []), B = Ce((A) => {
    f(A);
  }, []), F = Ce(
    (A) => {
      i(A);
    },
    [i]
  ), x = Ce(
    (A, P) => {
      const X = [...n], m = X[A];
      X[A] = X[P], X[P] = m, r(X);
    },
    [n, r]
  ), O = Ce(
    (A, P) => {
      const X = [...n];
      X[A] = P, r(X);
    },
    [n, r]
  ), Q = Ce(() => {
    r([...s]);
  }, [s, r]), H = Ce(
    (A) => {
      if (ja(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const X = [...n];
      X[P] = A.toUpperCase(), r(X);
    },
    [n, r]
  ), $ = Ce(() => {
    p != null && H(p), i(p);
  }, [p, i, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: g,
      style: k ? { top: k.top, left: k.left, visibility: "visible" } : { visibility: "hidden" }
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
      Da,
      {
        colors: n,
        columns: a,
        onSelect: B,
        onConfirm: F,
        onSwap: x,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Q }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Pa, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: v ? L : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? w : "",
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
        value: v ? R : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !ft(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: $ }, b["js.colorInput.ok"]))
  );
}, Fa = { "js.colorInput.chooseColor": "Choose color" }, { useState: $a, useCallback: Ye, useRef: Ua } = e, Ha = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), s = ae(), c = ie(Fa), [i, u] = $a(!1), r = Ua(null), o = n, d = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, g = t.defaultPalette ?? p, b = Ye(() => {
    d && u(!0);
  }, [d]), k = Ye(
    (w) => {
      u(!1), a(w);
    },
    [a]
  ), C = Ye(() => {
    u(!1);
  }, []), v = Ye(
    (w) => {
      s("paletteChanged", { palette: w });
    },
    [s]
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
      "aria-label": c["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Oa,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: g,
      canReset: t.canReset !== !1,
      onConfirm: k,
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
}, { useState: Ue, useCallback: Le, useEffect: ct, useRef: Bt, useLayoutEffect: Wa, useMemo: Va } = e, za = {
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
}, Ka = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: s,
  onCancel: c,
  onLoadIcons: i
}) => {
  const u = ie(za), [r, o] = Ue("simple"), [d, p] = Ue(""), [f, g] = Ue(t ?? ""), [b, k] = Ue(!1), [C, v] = Ue(null), w = Bt(null), I = Bt(null);
  Wa(() => {
    if (!l.current || !w.current) return;
    const F = l.current.getBoundingClientRect(), x = w.current.getBoundingClientRect();
    let O = F.bottom + 4, Q = F.left;
    O + x.height > window.innerHeight && (O = F.top - x.height - 4), Q + x.width > window.innerWidth && (Q = Math.max(0, F.right - x.width)), v({ top: O, left: Q });
  }, [l]), ct(() => {
    !a && !b && i().catch(() => k(!0));
  }, [a, b, i]), ct(() => {
    a && I.current && I.current.focus();
  }, [a]), xe(!0, { ESCAPE: c }), ct(() => {
    const F = (O) => {
      w.current && !w.current.contains(O.target) && c();
    }, x = setTimeout(() => document.addEventListener("mousedown", F), 0);
    return () => {
      clearTimeout(x), document.removeEventListener("mousedown", F);
    };
  }, [c]);
  const R = Va(() => {
    if (!d) return n;
    const F = d.toLowerCase();
    return n.filter(
      (x) => x.prefix.toLowerCase().includes(F) || x.label.toLowerCase().includes(F) || x.terms != null && x.terms.some((O) => O.includes(F))
    );
  }, [n, d]), _ = Le((F) => {
    p(F.target.value);
  }, []), E = Le(
    (F) => {
      s(F);
    },
    [s]
  ), h = Le((F) => {
    g(F);
  }, []), L = Le((F) => {
    g(F.target.value);
  }, []), S = Le(() => {
    s(f || null);
  }, [f, s]), N = Le(() => {
    s(null);
  }, [s]), B = Le(async (F) => {
    F.preventDefault(), k(!1);
    try {
      await i();
    } catch {
      k(!0);
    }
  }, [i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: w,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: B }, u["js.iconSelect.loadError"])),
      a && R.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && R.map(
        (F) => F.variants.map((x) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: x.encoded,
            className: "tlIconSelect__iconCell" + (x.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": x.encoded === t,
            tabIndex: 0,
            title: F.label,
            onClick: () => r === "simple" ? E(x.encoded) : h(x.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? E(x.encoded) : h(x.encoded));
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
        onChange: L
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ye, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, u["js.iconSelect.ok"]))
  );
}, Ya = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Ga, useCallback: Ge, useRef: Xa } = e, qa = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), s = ae(), c = ie(Ya), [i, u] = Ga(!1), r = Xa(null), o = n, d = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], g = t.iconsLoaded === !0, b = Ge(() => {
    d && !p && u(!0);
  }, [d, p]), k = Ge(
    (w) => {
      u(!1), a(w);
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
      onClick: b,
      disabled: p,
      title: o ?? "",
      "aria-label": c["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Ka,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: g,
      onSelect: k,
      onCancel: C,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: Ae, useEffect: Za, useMemo: Ot, useRef: Qa, useState: it } = e, Ja = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, er = [1, 2, 3, 4];
function tr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), s = n[2] || "px";
  return s === "rem" || s === "em" ? a * t : a;
}
function nr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const s of er)
    n >= s && (a = s);
  return a;
}
function lr(l, t) {
  const n = Ja[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function ar(l, t) {
  const n = Math.max(1, t), a = {}, s = (p, f) => !!(a[p] && a[p][f]), c = (p, f) => {
    a[p] || (a[p] = {}), a[p][f] = !0;
  }, i = [];
  let u = 0, r = 0;
  const o = (p) => {
    let f = null;
    for (const b of i) b.rowStart === p && (f = b);
    if (!f) return;
    let g = f.colEnd;
    for (; g < n && !s(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let b = f.rowStart; b < f.rowEnd; b++)
        for (let k = f.colEnd; k < g; k++) c(b, k);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(lr(p.width, n), n);
    for (; s(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let I = r; I < n && !s(u, I); I++)
      b++;
    if (g > b) {
      for (o(u), r = 0, u++; s(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let I = r; I < n && !s(u, I); I++)
        b++;
      g = Math.min(g, b);
    }
    const k = r, C = r + g, v = u, w = u + f;
    i.push({ id: p.id, colStart: k, colEnd: C, rowStart: v, rowEnd: w });
    for (let I = v; I < w; I++)
      for (let R = k; R < C; R++) c(I, R);
    r = C, r >= n && (r = 0, u++);
  }
  o(u);
  let d = 0;
  for (const p of i) p.rowEnd > d && (d = p.rowEnd);
  for (let p = 1; p < d; p++)
    for (let f = 0; f < n; f++) {
      if (s(p, f)) continue;
      const g = i.find((b) => b.rowEnd === p && b.colStart <= f && f < b.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let b = g.colStart; b < g.colEnd; b++) c(p, b);
      }
    }
  return i;
}
const rr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.minColWidth ?? "16rem", s = (t.children ?? []).filter((E) => E && E.id), c = Qa(null), [i, u] = it(1), r = t.editMode === !0;
  Za(() => {
    const E = c.current;
    if (!E) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, L = tr(a, h), S = () => u(nr(E.clientWidth, L));
    S();
    const N = new ResizeObserver(S);
    return N.observe(E), () => N.disconnect();
  }, [a]);
  const o = Ot(() => ar(s, i), [s, i]), d = Ot(() => {
    const E = {};
    for (const h of o) E[h.id] = h;
    return E;
  }, [o]), [p, f] = it(null), [g, b] = it(null), k = Ae((E, h) => {
    if (!r) {
      E.preventDefault();
      return;
    }
    f(h), E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", h);
  }, [r]), C = Ae((E, h) => {
    if (!r || !p || p === h) return;
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const L = E.currentTarget.getBoundingClientRect(), S = E.clientX < L.left + L.width / 2;
    b((N) => N && N.id === h && N.before === S ? N : { id: h, before: S });
  }, [r, p]), v = Ae(() => {
  }, []), w = Ae((E, h, L) => {
    const S = s.map((x) => x.id), N = S.indexOf(E);
    if (N < 0) return;
    S.splice(N, 1);
    const B = S.indexOf(h);
    if (B < 0) {
      S.splice(N, 0, E);
      return;
    }
    const F = L ? B : B + 1;
    S.splice(F, 0, E), n("reorder", { order: S });
  }, [s, n]), I = Ae((E, h) => {
    if (!r || !p || p === h) return;
    E.preventDefault();
    const L = E.currentTarget.getBoundingClientRect(), S = E.clientX < L.left + L.width / 2;
    w(p, h, S), f(null), b(null);
  }, [r, p, w]), R = Ae(() => {
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
      ref: c,
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, s.map((E) => {
      const h = d[E.id];
      if (!h) return null;
      const L = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, S = ["tlDashboard__tile"];
      return p === E.id && S.push("tlDashboard__tile--dragging"), g && g.id === E.id && S.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: S.join(" "),
          style: L,
          draggable: r,
          onDragStart: (N) => k(N, E.id),
          onDragOver: (N) => C(N, E.id),
          onDragLeave: v,
          onDrop: (N) => I(N, E.id),
          onDragEnd: R
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: or, useRef: Ft, useState: $t, useEffect: sr, useLayoutEffect: cr } = e, ir = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, ur = ({ group: l }) => {
  var p, f;
  const [t, n] = $t(!1), [a, s] = $t({}), c = Ft(null), i = Ft(null), u = or(() => {
    n((g) => !g);
  }, []);
  cr(() => {
    if (!t) return;
    const g = () => {
      const b = c.current;
      if (!b) return;
      const k = b.getBoundingClientRect();
      s({
        position: "fixed",
        top: k.bottom + 4,
        right: Math.max(8, window.innerWidth - k.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), sr(() => {
    if (!t) return;
    const g = (b) => {
      i.current && !i.current.contains(b.target) && c.current && !c.current.contains(b.target) && n(!1);
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [t]), xe(t, { ESCAPE: () => n(!1) }), bt(t, i, "first");
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
      r.map((g, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: g }))),
      (f = l.subGroups) == null ? void 0 : f.map((g, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((k, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: k })))))
    ),
    document.body
  ));
}, dr = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((s) => s.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((s, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(ur, { group: s }) : /* @__PURE__ */ e.createElement(ir, { group: s }))));
}, mr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, pr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.content, s = t.breadcrumb ?? null;
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
}, fr = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: a })));
}, hr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), br = {
  "js.sidebar.openDrawer": "Open navigation"
}, _r = ({ controlId: l }) => {
  const t = ae(), n = ie(br);
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
U("TLButton", _n);
U("TLUploadButton", gn);
U("TLToggleButton", En);
U("TLTextInput", nn);
U("TLPasswordInput", an);
U("TLNumberInput", on);
U("TLDatePicker", cn);
U("TLSelect", dn);
U("TLCheckbox", hn);
U("TLCounter", Cn);
U("TLTabBar", yn);
U("TLFieldList", kn);
U("TLAudioRecorder", Nn);
U("TLAudioPlayer", Rn);
U("TLFileUpload", Ln);
U("TLBinaryField", In);
U("TLFileChips", Mn);
U("TLRelativeTime", On);
U("TLAnchor", Fn);
U("TLScrollLink", $n);
U("TLAvatar", Wn);
U("TLDownload", zn);
U("TLPhotoCapture", Yn);
U("TLPhotoViewer", Xn);
U("TLPdfViewer", Zn);
U("TLSplitPanel", Qn);
U("TLPanel", rl);
U("TLInset", _l);
U("TLMaximizeRoot", ol);
U("TLDeckPane", sl);
U("TLSidebar", hl);
U("TLStack", bl);
U("TLGrid", gl);
U("TLCard", vl);
U("TLAppBar", El);
U("TLBreadcrumb", wl);
U("TLBottomBar", kl);
U("TLDialog", Tl);
U("TLDialogManager", Ll);
U("TLWindow", jl);
U("TLDrawer", Bl);
U("TLContextMenuRegion", Fl);
U("TLSnackbar", Wl);
U("TLMenu", zl);
U("TLAppShell", Yl);
U("TLText", Gl);
U("TLTableView", Jl);
U("TLColumnSelect", ta);
U("TLFormLayout", ua);
U("TLFormGroup", pa);
U("TLFormField", _a);
U("TLResourceCell", ga);
U("TLTreeView", Ea);
U("TLDropdownSelect", Ta);
U("TLColorInput", Ha);
U("TLIconSelect", qa);
U("TLDashboard", rr);
U("TLToolbar", dr);
U("TLTileStack", mr);
U("TLAdaptiveDetail", pr);
U("TLSlot", fr);
U("TLSlotContent", hr);
U("TLDrawerToggle", _r);
