import { React as e, useTLFieldValue as ke, useTLCommand as ae, useTLState as G, useKeyboardBinding as me, useTLUpload as Ae, TLChild as K, useI18N as ue, useTLDataUrl as Oe, scrollToAnchor as on, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as bt, useFocusTrap as _t, CMD_VALUE_CHANGED as We, anchoredOverlayProps as sn, register as F } from "tl-react-bridge";
const { useCallback: wt, useRef: cn } = e, un = 300, dn = ({ controlId: l, state: t }) => {
  const [n, a, s] = ke({
    debounceMs: un,
    sendOnBlur: t.sendValueOnBlur === !0
  }), c = ae(), i = cn(!1), u = wt(
    (k) => {
      i.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = wt(async () => {
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
}, { useCallback: yt } = e, mn = 300, pn = ({ controlId: l, state: t }) => {
  const [n, a, s] = ke({ debounceMs: mn }), c = yt(
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
}, { useCallback: kt } = e, fn = 300, hn = ({ controlId: l, state: t, config: n }) => {
  const [a, s, c] = ke({ debounceMs: fn }), i = kt(
    (f) => {
      const g = f.target.value;
      s(g === "" ? null : g);
    },
    [s]
  ), u = kt(() => {
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
}, { useCallback: bn } = e, _n = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = bn(
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
}, { useCallback: gn } = e, vn = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, s] = ke(), c = gn(
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
}, { useCallback: En } = e, Cn = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = t.options ?? [], c = t.presentation === "select", i = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, o = En(
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
}, { useCallback: wn, useRef: yn, useEffect: kn } = e, Sn = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = t.triState === !0, c = yn(null);
  kn(() => {
    c.current && (c.current.indeterminate = s && n !== !0 && n !== !1);
  }, [s, n]);
  const i = wn(
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
const { useCallback: Nn } = e, Tn = ({ controlId: l, command: t, label: n, image: a, disabled: s, displayMode: c }) => {
  const i = G(), u = ae(), r = t ?? "click", o = n ?? i.label, d = a ?? i.image, p = s ?? i.disabled === !0, f = c ?? i.displayMode ?? "label-only", g = i.hidden === !0, b = i.tooltip, k = i.appearance, E = i.size, v = i.navigateUrl, y = Nn(() => {
    if (v) {
      window.location.assign(v);
      return;
    }
    u(r);
  }, [u, r, v]), L = i.keyGesture;
  me(L, () => p || g ? !1 : (y(), !0));
  const D = f === "icon-only", _ = f === "label-only" || f === "icon-label" || D && !d, w = b ?? (D ? o : void 0), h = w ? `text:${w}` : void 0;
  return g ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: y,
      disabled: p,
      className: "tlReactButton" + (D ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": h,
      "aria-label": d || D ? o : void 0
    },
    d && /* @__PURE__ */ e.createElement(ye, { encoded: d, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, Rn = ({ controlId: l }) => {
  const t = G(), n = Ae(), a = e.useRef(null), [s, c] = e.useState(!1), i = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, d = t.displayMode ?? "label-only", p = t.appearance, f = t.accept, g = t.multiple === !0, b = e.useCallback(() => {
    var D;
    r || s || (D = a.current) == null || D.click();
  }, [r, s]), k = e.useCallback(async (D) => {
    const _ = D.target.files;
    if (!_ || _.length === 0) return;
    const w = new FormData();
    for (let h = 0; h < _.length; h++)
      w.append("file", _[h], _[h].name);
    D.target.value = "", c(!0);
    try {
      await n(w);
    } finally {
      c(!1);
    }
  }, [n]), E = d === "icon-only", v = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || E && !u, L = r || s;
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
      disabled: L,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? i : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ));
}, { useCallback: Dn } = e, Ln = ({ controlId: l, command: t, label: n, active: a, disabled: s }) => {
  const c = G(), i = ae(), u = t ?? "click", r = n ?? c.label, o = a ?? c.active === !0, d = s ?? c.disabled === !0, p = Dn(() => {
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
}, xn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: In } = e, Mn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.tabs ?? [], s = t.activeTabId, c = In((i) => {
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
}, jn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Bn = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), d = t.status ?? "idle", p = t.error, f = d === "received" ? "idle" : a !== "idle" ? a : d, g = e.useCallback(async () => {
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
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(y, L ? { mimeType: L } : void 0);
        u.current = D, D.ondataavailable = (_) => {
          _.data.size > 0 && r.current.push(_.data);
        }, D.onstop = async () => {
          y.getTracks().forEach((h) => h.stop()), o.current = null;
          const _ = new Blob(r.current, { type: D.mimeType || "audio/webm" });
          if (r.current = [], _.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const w = new FormData();
          w.append("audio", _, "recording.webm"), await n(w), s("idle");
        }, D.start(), s("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), i("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [a, n]), b = ue(jn), k = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: g,
      disabled: E,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[c]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, An = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, On = ({ controlId: l }) => {
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
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      i("idle");
    }, k.play(), i("playing");
  }, [c, n]), p = ue(An), f = c === "loading" ? p["js.loading"] : c === "playing" ? p["js.audioPlayer.pause"] : c === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = c === "disabled" || c === "loading", b = ["tlAudioPlayer__button"];
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
}, Fn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, $n = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [c, i] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, d = t.accept ?? "", p = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (_) => {
    s("uploading");
    const w = new FormData();
    w.append("file", _, _.name), await n(w), s("idle");
  }, [n]), g = e.useCallback((_) => {
    var h;
    const w = (h = _.target.files) == null ? void 0 : h[0];
    w && f(w);
  }, [f]), b = e.useCallback(() => {
    var _;
    a !== "uploading" && ((_ = u.current) == null || _.click());
  }, [a]), k = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!0);
  }, []), E = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), i(!1);
  }, []), v = e.useCallback((_) => {
    var h;
    if (_.preventDefault(), _.stopPropagation(), i(!1), a === "uploading") return;
    const w = (h = _.dataTransfer.files) == null ? void 0 : h[0];
    w && f(w);
  }, [a, f]), y = p === "uploading", L = ue(Fn), D = p === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
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
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Un = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Hn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, s = Ae(), c = Oe(), i = ue(Un), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", d = a.dataRevision ?? 0, p = a.accept ?? "", f = a.status ?? "idle", g = a.error ?? null, [b, k] = e.useState("idle"), [E, v] = e.useState(!1), [y, L] = e.useState(!1), D = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!r || y)) {
      L(!0);
      try {
        const $ = c + (c.includes("?") ? "&" : "?") + "rev=" + d, A = await fetch($);
        if (!A.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", A.status);
          return;
        }
        const P = await A.blob(), q = URL.createObjectURL(P), m = document.createElement("a");
        m.href = q, m.download = o, m.style.display = "none", document.body.appendChild(m), m.click(), document.body.removeChild(m), URL.revokeObjectURL(q);
      } catch ($) {
        console.error("[TLBinaryField] Fetch error:", $);
      } finally {
        L(!1);
      }
    }
  }, [r, y, c, d, o]), w = e.useCallback(async ($) => {
    k("uploading");
    const A = new FormData();
    A.append("file", $, $.name), await s(A), k("idle");
  }, [s]), h = (f === "received" ? "idle" : b !== "idle" ? b : f) === "uploading", x = e.useCallback(($) => {
    var P;
    const A = (P = $.target.files) == null ? void 0 : P[0];
    A && w(A);
  }, [w]), R = e.useCallback(() => {
    var $;
    h || ($ = D.current) == null || $.click();
  }, [h]), N = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!0);
  }, []), z = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!1);
  }, []), B = e.useCallback(($) => {
    var P;
    if ($.preventDefault(), $.stopPropagation(), v(!1), h) return;
    const A = (P = $.dataTransfer.files) == null ? void 0 : P[0];
    A && w(A);
  }, [h, w]), I = y ? i["js.downloading"] : i["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: y,
      title: I,
      "aria-label": I
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
      className: `tlBinaryField tlFileUpload${E ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: z,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "file",
        accept: p || void 0,
        onChange: x,
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
}, Wn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function zn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Vn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Ae(), s = Oe(), c = ue(Wn), i = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [d, p] = e.useState(!1), f = e.useRef(null), g = e.useCallback(async (_) => {
    const w = Array.from(_);
    if (w.length !== 0) {
      o(!0);
      try {
        const h = new FormData();
        for (const x of w)
          h.append("file", x, x.name);
        await a(h);
      } finally {
        o(!1);
      }
    }
  }, [a]), b = e.useCallback(async (_) => {
    if (_.hasData)
      try {
        const w = s + "&key=" + encodeURIComponent(_.key), h = await fetch(w);
        if (!h.ok) {
          console.error("[TLFileChips] Failed to fetch data:", h.status);
          return;
        }
        const x = await h.blob(), R = URL.createObjectURL(x), N = document.createElement("a");
        N.href = R, N.download = _.name, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(R);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [s]), k = e.useCallback((_) => {
    _.target.files && g(_.target.files), _.target.value = "";
  }, [g]), E = e.useCallback(() => {
    var _;
    r || (_ = f.current) == null || _.click();
  }, [r]), v = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!0));
  }, [u]), y = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1));
  }, [u]), L = e.useCallback((_) => {
    u && (_.preventDefault(), _.stopPropagation(), p(!1), !r && _.dataTransfer.files && g(_.dataTransfer.files));
  }, [u, r, g]), D = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    d ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: D,
      onDragOver: v,
      onDragLeave: y,
      onDrop: L
    },
    i.map((_) => {
      const w = c["js.download.file"].replace("{0}", _.name), h = c["js.fileChips.remove"].replace("{0}", _.name);
      return /* @__PURE__ */ e.createElement("span", { key: _.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => b(_),
          disabled: !_.hasData,
          title: _.hasData ? w : _.name
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
        _.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, zn(_.size))
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
        onClick: E,
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
}, Kn = 3e4;
function Yn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), s = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? s.format(Math.trunc(n / 1), "second") : a < 3600 ? s.format(Math.trunc(n / 60), "minute") : a < 86400 ? s.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? s.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Gn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, s = t.locale || navigator.language, [, c] = e.useState(0);
  return e.useEffect(() => {
    const i = setInterval(() => c((u) => u + 1), Kn);
    return () => clearInterval(i);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Yn(n, s));
}, Xn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, qn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const s = (c) => {
    c.preventDefault(), on(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: s }, a);
};
function Zn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Qn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Jn = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Qn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Zn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, el = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, tl = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = ae(), s = !!t.hasData, c = t.dataRevision ?? 0, i = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!s || r)) {
      o(!0);
      try {
        const b = n + (n.includes("?") ? "&" : "?") + "rev=" + c, k = await fetch(b);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const E = await k.blob(), v = URL.createObjectURL(E), y = document.createElement("a");
        y.href = v, y.download = i, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        o(!1);
      }
    }
  }, [s, r, n, c, i]), p = e.useCallback(async () => {
    s && await a("clear");
  }, [s, a]), f = ue(el);
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
}, nl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ll = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [c, i] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), b = t.error, k = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((N) => N.stop()), d.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    E(), s("idle");
  }, [E]), y = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (i(null), !k) {
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
  }, [a, k]), L = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, z = p.current;
    if (!N || !z)
      return;
    z.width = N.videoWidth, z.height = N.videoHeight;
    const B = z.getContext("2d");
    B && (B.drawImage(N, 0, 0), E(), s("uploading"), z.toBlob(async (I) => {
      if (!I) {
        s("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", I, "capture.jpg"), await n(O), s("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, E]), D = e.useCallback(async (N) => {
    var I;
    const z = (I = N.target.files) == null ? void 0 : I[0];
    if (!z) return;
    s("uploading");
    const B = new FormData();
    B.append("photo", z, z.name), await n(B), s("idle"), f.current && (f.current.value = "");
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
  const _ = ue(nl), w = a === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && h.push("tlPhotoCapture__cameraBtn--uploading");
  const x = ["tlPhotoCapture__overlayVideo"];
  u && x.push("tlPhotoCapture__overlayVideo--mirrored");
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
  )), !k && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: D
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
        className: x.join(" "),
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
        onClick: L,
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
}, al = {
  "js.photoViewer.alt": "Captured photo"
}, rl = ({ controlId: l }) => {
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
  const r = ue(al);
  return !a || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, ol = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, sl = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, s = t.dataRevision ?? 0, c = ue(ol), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + s, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: c["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, c["js.pdfViewer.noDocument"]));
}, { useCallback: St, useRef: et } = e, cl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.orientation, s = t.resizable === !0, c = t.children ?? [], i = a === "horizontal", u = c.length > 0 && c.every((E) => E.collapsed), r = !u && c.some((E) => E.collapsed), o = u ? !i : i, d = et(null), p = et(null), f = et(null), g = St((E, v) => {
    const y = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : v !== void 0 ? y.flex = `0 0 ${v}px` : y.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (y.minWidth = i ? E.minSize : void 0, y.minHeight = i ? void 0 : E.minSize), y;
  }, [i, u, r, o]), b = St((E, v) => {
    E.preventDefault();
    const y = d.current;
    if (!y) return;
    const L = c[v], D = c[v + 1], _ = y.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    _.forEach((R) => {
      w.push(i ? R.offsetWidth : R.offsetHeight);
    }), f.current = w, p.current = {
      splitterIndex: v,
      startPos: i ? E.clientX : E.clientY,
      startSizeBefore: w[v],
      startSizeAfter: w[v + 1],
      childBefore: L,
      childAfter: D
    };
    const h = (R) => {
      const N = p.current;
      if (!N || !f.current) return;
      const B = (i ? R.clientX : R.clientY) - N.startPos, I = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Z = N.startSizeBefore + B, H = N.startSizeAfter - B;
      Z < I && (H += Z - I, Z = I), H < O && (Z += H - O, H = O), f.current[N.splitterIndex] = Z, f.current[N.splitterIndex + 1] = H;
      const $ = y.querySelectorAll(":scope > .tlSplitPanel__child"), A = $[N.splitterIndex], P = $[N.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${Z}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, x = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", x), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const R = {};
        c.forEach((N, z) => {
          const B = N.control;
          B != null && B.controlId && f.current && (R[B.controlId] = f.current[z]);
        }), n("updateSizes", { sizes: R });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", x), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, i, n]), k = [];
  return c.forEach((E, v) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(E)
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control })
      )
    ), s && v < c.length - 1) {
      const y = c[v + 1];
      !E.collapsed && !y.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (D) => b(D, v)
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
}, { useCallback: tt } = e, il = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ul = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), dl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ml = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), pl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), fl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), hl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(il), s = t.title, c = t.expansionState ?? "NORMALIZED", i = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, d = t.fill === !0, p = t.hoverActions === !0, f = t.appearance === "card", g = t.errorMessage, b = c === "MINIMIZED", k = c === "MAXIMIZED", E = c === "HIDDEN", v = tt(() => {
    n("toggleMinimize");
  }, [n]), y = tt(() => {
    n("toggleMaximize");
  }, [n]), L = tt(() => {
    n("popOut");
  }, [n]);
  if (E)
    return null;
  const D = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, _ = i && !k || u && !b || r, w = !!s && s.trim() !== "" || !!t.titleContent || !!t.toolbar || _;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${d ? " tlPanel--fill" : ""}${p ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: D
    },
    w && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!s && s.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), i && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(dl, null) : /* @__PURE__ */ e.createElement(ul, null)
    ), u && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(pl, null) : /* @__PURE__ */ e.createElement(ml, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(fl, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !b && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, g)),
    !b && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, bl = ({ controlId: l }) => {
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
}, _l = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ge, useState: Xe, useEffect: dt, useRef: Ze } = e, gl = {
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
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, vl = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: s, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
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
), El = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
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
), Cl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), wl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), yl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: s, onClose: c }) => {
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
        /* @__PURE__ */ e.createElement(Be, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, kl = ({
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
  const E = Ze(null), [v, y] = Xe(null), L = ge(() => {
    a ? g === l.id ? k() : (E.current && y(E.current.getBoundingClientRect()), b(l.id)) : i(l.id);
  }, [a, g, l.id, i, b, k]), D = ge((w) => {
    E.current = w, r(w);
  }, [r]), _ = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: L,
      title: a ? l.label : void 0,
      "aria-expanded": a ? _ : t,
      tabIndex: u,
      ref: D,
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
    yl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: s,
      onExecute: c,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    Wt,
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
      onOpenFlyout: b,
      onCloseFlyout: k
    }
  ))));
}, Wt = ({
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
        vl,
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
        El,
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
      return /* @__PURE__ */ e.createElement(Cl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(wl, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        kl,
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
  const t = G(), n = ae(), a = ue(gl), s = t.items ?? [], c = t.activeItemId, i = t.collapsed, u = t.drawerOpen, r = u ? !1 : i, [o, d] = Xe(() => {
    const I = /* @__PURE__ */ new Map(), O = (Z) => {
      for (const H of Z)
        H.type === "group" && (I.set(H.id, H.expanded), O(H.children));
    };
    return O(s), I;
  }), p = ge((I) => {
    d((O) => {
      const Z = new Map(O), H = Z.get(I) ?? !1;
      return Z.set(I, !H), n("toggleGroup", { itemId: I, expanded: !H }), Z;
    });
  }, [n]), f = ge((I) => {
    I !== c && n("selectItem", { itemId: I });
  }, [n, c]), g = ge((I) => {
    n("executeCommand", { itemId: I });
  }, [n]), b = ge(() => {
    n("toggleCollapse", {});
  }, [n]), k = ge(() => {
    n("toggleDrawer", {});
  }, [n]), [E, v] = Xe(null), y = ge((I) => {
    v(I);
  }, []), L = ge(() => {
    v(null);
  }, []);
  dt(() => {
    r || v(null);
  }, [r]);
  const [D, _] = Xe(() => {
    const I = mt(s, r, o);
    return I.length > 0 ? I[0].id : "";
  }), w = Ze(/* @__PURE__ */ new Map()), h = ge((I) => (O) => {
    O ? w.current.set(I, O) : w.current.delete(I);
  }, []), x = ge((I) => {
    _(I);
  }, []), R = Ze(0), N = ge((I) => {
    _(I), R.current++;
  }, []);
  dt(() => {
    const I = w.current.get(D);
    I && document.activeElement !== I && I.focus();
  }, [D, R.current]);
  const z = ge((I) => {
    if (I.key === "Escape" && E !== null) {
      I.preventDefault(), L();
      return;
    }
    const O = mt(s, r, o);
    if (O.length === 0) return;
    const Z = O.findIndex(($) => $.id === D);
    if (Z < 0) return;
    const H = O[Z];
    switch (I.key) {
      case "ArrowDown": {
        I.preventDefault();
        const $ = (Z + 1) % O.length;
        N(O[$].id);
        break;
      }
      case "ArrowUp": {
        I.preventDefault();
        const $ = (Z - 1 + O.length) % O.length;
        N(O[$].id);
        break;
      }
      case "Home": {
        I.preventDefault(), N(O[0].id);
        break;
      }
      case "End": {
        I.preventDefault(), N(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        I.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? g(H.id) : H.type === "group" && (r ? E === H.id ? L() : y(H.id) : p(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (I.preventDefault(), p(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (I.preventDefault(), p(H.id));
        break;
      }
    }
  }, [
    s,
    r,
    o,
    D,
    E,
    N,
    f,
    g,
    p,
    y,
    L
  ]), B = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: z }, s.map((I) => /* @__PURE__ */ e.createElement(
    Wt,
    {
      key: I.id,
      item: I,
      activeItemId: c,
      collapsed: r,
      onSelect: f,
      onExecute: g,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: h,
      onItemFocus: x,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: y,
      onCloseFlyout: L
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
}, Nl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", s = t.align ?? "stretch", c = t.wrap === !0, i = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${s}`,
    c ? "tlStack--wrap" : "",
    i ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o })));
}, Tl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Rl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, s = t.gap ?? "default", c = t.children ?? [], i = {};
  return a ? i.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (i.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: i }, c.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Dl = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", s = t.padding ?? "default", c = t.headerActions ?? [], i = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(K, { control: i })));
}, Ll = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, s = t.children ?? [], c = t.actions ?? [], i = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, s.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((o, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: o }))));
}, { useCallback: xl } = e, Il = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = xl((c) => {
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
}, { useCallback: Ml } = e, Pl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = t.activeItemId, c = Ml((i) => {
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
}, { useCallback: Nt, useRef: jl } = e, Bl = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Al = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.closeOnBackdrop !== !1, c = t.child, i = jl(null), u = Nt(() => {
    n("close");
  }, [n]), r = Nt((o) => {
    s && o.target === o.currentTarget && u();
  }, [s, u]);
  return a ? /* @__PURE__ */ e.createElement(bt, null, /* @__PURE__ */ e.createElement(Bl, { onClose: u }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: Ol, useRef: Fl } = e, $l = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = Fl(n.length);
  return Ol(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((s) => /* @__PURE__ */ e.createElement(K, { key: s.controlId, control: s })));
}, { useCallback: ze, useRef: Ie, useState: Ve } = e, Ul = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Hl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Wl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], zl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(Hl), s = t.title ?? "", c = t.width ?? "32rem", i = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, d = t.actions ?? [], p = t.toolbar, f = t.buttonBar, [g, b] = Ve(null), [k, E] = Ve(null), [v, y] = Ve(null), L = Ie(null), [D, _] = Ve(!1), w = Ie(null), h = Ie(null), x = Ie(null), R = Ie(null), N = Ie(null), z = ze(() => {
    n("close");
  }, [n]);
  _t(!0, R, "field");
  const B = ze(($, A) => {
    A.preventDefault();
    const P = R.current;
    if (!P) return;
    const q = P.getBoundingClientRect(), m = !L.current, T = L.current ?? { x: q.left, y: q.top };
    m && (L.current = T, y(T)), N.current = {
      dir: $,
      startX: A.clientX,
      startY: A.clientY,
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
      const Se = Math.max(200, ne), Ne = Math.max(100, _e);
      j.symmetric ? (ve = (j.startW - Se) / 2, Ce = (j.startH - Ne) / 2) : (j.dir.includes("w") && Se === 200 && (ve = j.startW - 200), j.dir.includes("n") && Ne === 100 && (Ce = j.startH - 100)), h.current = Se, x.current = Ne, b(Se), E(Ne);
      const xe = {
        x: j.startPos.x + ve,
        y: j.startPos.y + Ce
      };
      L.current = xe, y(xe);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
      const X = h.current, j = x.current;
      (X != null || j != null) && n("resize", {
        ...X != null ? { width: Math.round(X) } : {},
        ...j != null ? { height: Math.round(j) } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, [n]), I = ze(($) => {
    if ($.button !== 0 || $.target.closest("button")) return;
    $.preventDefault();
    const A = R.current;
    if (!A) return;
    const P = A.getBoundingClientRect(), q = L.current ?? { x: P.left, y: P.top }, m = $.clientX - q.x, T = $.clientY - q.y, V = (X) => {
      const j = window.innerWidth, te = window.innerHeight;
      let ce = X.clientX - m, ne = X.clientY - T;
      const _e = A.offsetWidth, ve = A.offsetHeight;
      ce + _e > j && (ce = j - _e), ne + ve > te && (ne = te - ve), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const Ce = { x: ce, y: ne };
      L.current = Ce, y(Ce);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, []), O = ze(() => {
    var $, A;
    if (D) {
      const P = w.current;
      P && (y(P.x !== -1 ? { x: P.x, y: P.y } : null), b(P.w), E(P.h)), _(!1);
    } else {
      const P = R.current, q = P == null ? void 0 : P.getBoundingClientRect();
      w.current = {
        x: (($ = L.current) == null ? void 0 : $.x) ?? (q == null ? void 0 : q.left) ?? -1,
        y: ((A = L.current) == null ? void 0 : A.y) ?? (q == null ? void 0 : q.top) ?? -1,
        w: g ?? (q == null ? void 0 : q.width) ?? null,
        h: k ?? null
      }, _(!0), y({ x: 0, y: 0 }), b(null), E(null);
    }
  }, [D, g, k]), Z = D ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : c,
    ...k != null ? { height: k + "px" } : i != null ? { height: i } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(bt, { modal: !0 }, /* @__PURE__ */ e.createElement(Ul, { onClose: z }), /* @__PURE__ */ e.createElement(
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
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : I,
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
          title: D ? a["js.window.restore"] : a["js.window.maximize"]
        },
        D ? (
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
    (d.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), d.map(($, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: $ }))),
    r && !D && Wl.map(($) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: $,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${$}`,
        onMouseDown: (A) => B($, A)
      }
    ))
  ));
}, { useCallback: Vl } = e, Kl = {
  "js.drawer.close": "Close"
}, Yl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(Kl), s = t.open === !0, c = t.position ?? "right", i = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Vl(() => {
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
}, { useCallback: Gl } = e, Xl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.child, s = Gl((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: s }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: ql, useEffect: Tt, useRef: Zl, useState: Rt } = e, Ql = 250, Jl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.message ?? "", s = t.content ?? "", c = t.variant ?? "info", i = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, d] = Rt(!1), [p, f] = Rt(!1), g = Zl(!1);
  Tt(() => {
    g.current = !1;
  }, [r]);
  const b = ql(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return Tt(() => {
    if (!u || i === 0 || p) return;
    const k = setTimeout(b, g.current ? Ql : i);
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
}, { useCallback: ea, useEffect: Dt, useMemo: ta, useRef: na, useState: la } = e, aa = 1e3;
function ra(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, s = Math.floor(t / 3600), c = (i) => i < 10 ? `0${i}` : `${i}`;
  return s > 0 ? `${s}:${c(a)}:${c(n)}` : `${a}:${c(n)}`;
}
const oa = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.visible === !0, s = t.severity ?? "info", c = t.text ?? "", i = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, o = t.actionLabel ?? null, d = t.pingGraceMs ?? null, p = ta(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [f, g] = la(0), b = a && i != null;
  Dt(() => {
    if (!b) return;
    const D = setInterval(() => g((_) => _ + 1), aa);
    return () => clearInterval(D);
  }, [b, i]);
  const k = na(null);
  Dt(() => {
    !b || d == null || i == null || k.current !== i && (Date.now() + p < i + d || (k.current = i, n("deadlinePassed", {})));
  }, [f, b, i, d, p, n]);
  const E = ea(() => {
    o != null && n("action", {});
  }, [n, o]);
  if (!a) return null;
  const v = i != null ? i - (Date.now() + p) : null;
  if (r != null && v != null && v > r) return null;
  const y = v != null ? ra(v) : null, L = o != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${s}${L ? " tlNoticeBar--clickable" : ""}`,
      role: L ? "button" : "status",
      "aria-live": "polite",
      tabIndex: L ? 0 : void 0,
      title: o ?? void 0,
      "aria-label": L ? `${c} ${o}` : void 0,
      onClick: L ? E : void 0,
      onKeyDown: L ? (D) => {
        (D.key === "Enter" || D.key === " ") && (D.preventDefault(), E());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, c),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: nt, useEffect: Lt, useRef: sa, useState: xt } = e, ca = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.anchorId, c = t.anchorX, i = t.anchorY, u = t.items ?? [], r = sa(null), [o, d] = xt({ top: 0, left: 0 }), [p, f] = xt(0), g = u.filter((v) => v.type === "item" && !v.disabled);
  Lt(() => {
    var h, x;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((x = r.current) == null ? void 0 : x.offsetWidth) ?? 200;
    if (c != null && i != null) {
      let R = i, N = c;
      R + v > window.innerHeight && (R = Math.max(0, window.innerHeight - v)), N + y > window.innerWidth && (N = Math.max(0, window.innerWidth - y)), d({ top: R, left: N }), f(0);
      return;
    }
    if (!s) return;
    const L = document.getElementById(s);
    if (!L) return;
    const D = L.getBoundingClientRect();
    let _ = D.bottom + 4, w = D.left;
    _ + v > window.innerHeight && (_ = D.top - v - 4), w + y > window.innerWidth && (w = D.right - y), d({ top: _, left: w }), f(0);
  }, [a, s, c, i]);
  const b = nt(() => {
    n("close");
  }, [n]), k = nt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  Lt(() => {
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
      y && k(y.id);
    }
  }, [b, k, g, p]);
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
      const D = g.indexOf(v) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => k(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, ia = 768, ua = ({ controlId: l }) => {
  const t = G(), n = ae();
  e.useEffect(() => {
    const d = window.matchMedia(`(max-width: ${ia}px)`), p = (g) => {
      n("reportDisplayClass", { displayClass: g ? "COMPACT" : "REGULAR" });
    };
    p(d.matches);
    const f = (g) => p(g.matches);
    return d.addEventListener("change", f), () => d.removeEventListener("change", f);
  }, [n]);
  const a = t.header, s = t.notices, c = t.content, i = t.footer, u = t.snackbar, r = t.dialogManager, o = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: c })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: i })), /* @__PURE__ */ e.createElement(K, { control: u }), r && /* @__PURE__ */ e.createElement(K, { control: r }), o && /* @__PURE__ */ e.createElement(K, { control: o }));
}, da = ({ controlId: l }) => {
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
}, ma = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: s }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (s(), !0) : !1), null), pa = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, It = 50, fa = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, fa));
}
const pt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', ha = pt + ", button:not([disabled]), a[href]";
function zt(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function at(l, t, n = {}) {
  const a = zt(l, t);
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
const ba = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(pa), s = e.useRef(null);
  e.useEffect(() => {
    const C = s.current;
    if (!C) return;
    const S = (U) => {
      const Q = U.detail;
      let ee = Q.target;
      for (; ee && ee !== C; ) {
        const re = ee.dataset.row, oe = ee.dataset.col;
        if (re != null && oe != null) {
          Q.resolved = { key: re + "|" + oe };
          return;
        }
        ee = ee.parentElement;
      }
    };
    return C.addEventListener("tl-tooltip-resolve", S), () => C.removeEventListener("tl-tooltip-resolve", S);
  }, []);
  const c = t.columns ?? [], i = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", d = t.selectedCount ?? 0, p = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, g = t.treeMode ?? !1, b = t.columnSelect ?? !1, k = e.useMemo(
    () => c.filter((C) => C.sortPriority && C.sortPriority > 0).length,
    [c]
  ), E = o === "multi", v = 40, y = 20, L = e.useRef(null), D = e.useRef(null), _ = e.useRef(null), w = e.useRef(null), h = e.useRef(null), [x, R] = e.useState({}), N = e.useRef(null), z = e.useRef(!1), B = e.useRef(null), [I, O] = e.useState(null), [Z, H] = e.useState(null), [$, A] = e.useState(null), [P, q] = e.useState(0);
  e.useEffect(() => {
    const C = _.current;
    if (!C)
      return;
    const S = () => {
      const Q = C.offsetWidth - C.clientWidth;
      q((ee) => ee === Q ? ee : Q);
    };
    S();
    const U = new ResizeObserver(S);
    return U.observe(C), () => U.disconnect();
  }, []), e.useEffect(() => {
    N.current || R({});
  }, [c]);
  const m = e.useCallback((C) => x[C.name] ?? C.width, [x]), T = e.useMemo(() => {
    const C = [];
    let S = E && f > 0 ? v : 0;
    for (let U = 0; U < f && U < c.length; U++)
      C.push(S), S += m(c[U]);
    return C;
  }, [c, f, E, v, m]), V = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let C = E ? v : 0;
    for (let S = 0; S < f && S < c.length; S++)
      C += m(c[S]);
    return C;
  }, [c, f, E, v, m]), W = i * r, X = e.useRef(null), j = e.useCallback((C, S, U) => {
    U.preventDefault(), U.stopPropagation(), N.current = { column: C, startX: U.clientX, startWidth: S };
    let Q = U.clientX, ee = 0;
    const re = () => {
      const se = N.current;
      if (!se) return;
      const de = Math.max(It, se.startWidth + (Q - se.startX) + ee);
      R((Ee) => ({ ...Ee, [se.column]: de }));
    }, oe = () => {
      const se = _.current, de = L.current;
      if (!se || !N.current) return;
      const Ee = se.getBoundingClientRect(), Te = 40, Et = 8, rn = se.scrollLeft;
      Q > Ee.right - Te ? se.scrollLeft += Et : Q < Ee.left + Te && (se.scrollLeft = Math.max(0, se.scrollLeft - Et));
      const Ct = se.scrollLeft - rn;
      Ct !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += Ct, re()), X.current = requestAnimationFrame(oe);
    };
    X.current = requestAnimationFrame(oe);
    const fe = (se) => {
      Q = se.clientX, re();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), X.current !== null && (cancelAnimationFrame(X.current), X.current = null);
      const de = N.current;
      if (de) {
        const Ee = Math.max(It, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ee }), N.current = null, z.current = !0, requestAnimationFrame(() => {
          z.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    L.current && _.current && (L.current.scrollLeft = _.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const C = _.current;
      if (!C) return;
      const S = C.scrollTop, U = Math.ceil(C.clientHeight / r), Q = Math.floor(S / r);
      n("scroll", { start: Q, count: U });
    }, 80);
  }, [n, r]), ce = e.useCallback((C, S, U) => {
    if (z.current) return;
    let Q;
    !S || S === "desc" ? Q = "asc" : Q = "desc";
    const ee = U.shiftKey ? "add" : "replace";
    n("sort", { column: C, direction: Q, mode: ee });
  }, [n]), ne = e.useCallback((C, S) => {
    B.current = C, S.dataTransfer.effectAllowed = "move", S.dataTransfer.setData("text/plain", C);
  }, []), _e = e.useCallback((C, S) => {
    if (!B.current || B.current === C) {
      O(null);
      return;
    }
    S.preventDefault(), S.dataTransfer.dropEffect = "move";
    const U = S.currentTarget.getBoundingClientRect(), Q = S.clientX < U.left + U.width / 2 ? "left" : "right";
    O({ column: C, side: Q });
  }, []), ve = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation();
    const S = B.current;
    if (!S || !I) {
      B.current = null, O(null);
      return;
    }
    let U = c.findIndex((ee) => ee.name === I.column);
    if (U < 0) {
      B.current = null, O(null);
      return;
    }
    const Q = c.findIndex((ee) => ee.name === S);
    I.side === "right" && U++, Q < U && U--, n("columnReorder", { column: S, targetIndex: U }), B.current = null, O(null);
  }, [c, I, n]), Ce = e.useCallback(() => {
    B.current = null, O(null);
  }, []), Se = e.useCallback((C, S) => {
    var ee, re, oe, fe;
    const U = window.getSelection();
    if (U && !U.isCollapsed && S.currentTarget.contains(U.anchorNode))
      return;
    if (!lt(S) && ((ee = _.current) == null || ee.focus({ preventScroll: !0 }), !S.ctrlKey && !S.metaKey && !S.shiftKey)) {
      const pe = (fe = (oe = (re = S.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : fe.getAttribute("data-col");
      h.current = { index: C, col: pe ?? void 0 };
    }
    const Q = u.find((pe) => pe.index === C);
    lt(S) && (Q != null && Q.selected) && !S.ctrlKey && !S.metaKey && !S.shiftKey || n("select", {
      rowIndex: C,
      ctrlKey: S.ctrlKey || S.metaKey,
      shiftKey: S.shiftKey
    });
  }, [n, u]), Ne = e.useCallback((C, S, U) => {
    n("moveSelection", { direction: C, extend: S, move: U });
  }, [n]), xe = e.useCallback(() => {
    p < 0 || n("select", { rowIndex: p, ctrlKey: E, shiftKey: !1 });
  }, [n, p, E]), He = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), M = e.useCallback(
    () => !!s.current && s.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (p < 0)
      return;
    const C = _.current;
    if (!C)
      return;
    const S = p * r, U = S + r;
    S < C.scrollTop ? C.scrollTop = S : U > C.scrollTop + C.clientHeight && (C.scrollTop = U - C.clientHeight);
  }, [p, r]), e.useEffect(() => {
    const C = h.current, S = _.current;
    if (!C || !S)
      return;
    const U = u.find((re) => re.index === C.index);
    if (!U || !at(S, U.id))
      return;
    h.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !S.contains(Q))
      return;
    const ee = at(S, U.id, { col: C.col, last: C.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [u]);
  const Y = e.useCallback((C) => {
    if (C.key !== "Tab")
      return;
    const S = _.current, U = document.activeElement;
    if (!S || !U || !S.contains(U))
      return;
    const Q = U.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = u.find((Te) => Te.id === ee);
    if (!re)
      return;
    const oe = zt(S, ee).flatMap((Te) => Array.from(Te.querySelectorAll(ha))), fe = oe.indexOf(U);
    if (fe < 0)
      return;
    const pe = !C.shiftKey;
    if (!(pe ? fe === oe.length - 1 : fe === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= i)
      return;
    const Ee = u.find((Te) => Te.index === de);
    Ee && at(S, Ee.id) || (C.preventDefault(), h.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, i, n]), le = e.useCallback((C, S) => {
    S.stopPropagation(), n("select", { rowIndex: C, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ie = e.useCallback(() => {
    const C = d === i && i > 0;
    n("selectAll", { selected: !C });
  }, [n, d, i]), Fe = e.useCallback((C, S, U) => {
    U.stopPropagation(), n("expand", { rowIndex: C, expanded: S });
  }, [n]), Zt = e.useCallback((C, S) => {
    S.preventDefault(), H({ x: S.clientX, y: S.clientY, colIdx: C });
  }, []), Qt = e.useCallback(() => {
    Z && (n("setFrozenColumnCount", { count: Z.colIdx + 1 }), H(null));
  }, [Z, n]), Jt = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), H(null);
  }, [n]), en = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation();
    const S = D.current, U = L.current;
    if (!S || !U)
      return;
    const Q = S.clientWidth, ee = [{ x: 0, count: 0 }];
    U.querySelectorAll("[data-col-idx]").forEach((pe) => {
      const se = pe.getBoundingClientRect().right - S.getBoundingClientRect().left;
      se > 0 && se <= Q && ee.push({ x: se, count: Number(pe.dataset.colIdx) + 1 });
    });
    let re = { x: V, count: f };
    const oe = (pe) => {
      const se = pe.clientX - S.getBoundingClientRect().left;
      re = ee.reduce(
        (de, Ee) => Math.abs(Ee.x - se) < Math.abs(de.x - se) ? Ee : de,
        ee[0]
      ), A(re);
    }, fe = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", fe), A(null), re.count !== f && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", fe);
  }, [V, f, n]);
  e.useEffect(() => {
    if (!Z) return;
    const C = () => H(null);
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [Z]), Le(!!Z, { ESCAPE: () => H(null) });
  const tn = e.useCallback((C, S) => {
    S.stopPropagation(), S.preventDefault(), n("openFilter", { column: C });
  }, [n]), nn = e.useCallback((C) => {
    C.stopPropagation(), C.preventDefault(), n("openColumnSelect", {});
  }, [n]), Qe = c.reduce((C, S) => C + m(S), 0) + (E ? v : 0), Je = b ? 32 : 0, ln = d === i && i > 0, vt = d > 0 && d < i, an = e.useCallback((C) => {
    C && (C.indeterminate = vt);
  }, [vt]);
  return /* @__PURE__ */ e.createElement(bt, { active: M }, /* @__PURE__ */ e.createElement(
    ma,
    {
      isMulti: E,
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
      onDragOver: (C) => {
        if (!B.current) return;
        C.preventDefault();
        const S = _.current, U = L.current;
        if (!S) return;
        const Q = S.getBoundingClientRect(), ee = 40, re = 8;
        C.clientX < Q.left + ee ? S.scrollLeft = Math.max(0, S.scrollLeft - re) : C.clientX > Q.right - ee && (S.scrollLeft += re), U && (U.scrollLeft = S.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: D }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: L }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Qe, paddingRight: Je + P }
      },
      E && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (f > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: v,
            minWidth: v,
            ...f > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (C) => {
            B.current && (C.preventDefault(), C.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== B.current && O({ column: c[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: an,
            className: "tlTableView__checkbox",
            checked: ln,
            onChange: ie
          }
        )
      ),
      c.map((C, S) => {
        const U = m(C);
        c.length - 1;
        let Q = "tlTableView__headerCell";
        C.sortable && (Q += " tlTableView__headerCell--sortable"), I && I.column === C.name && (Q += " tlTableView__headerCell--dragOver-" + I.side);
        const ee = S < f, re = S === f - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: C.name,
            className: Q,
            "data-col-idx": S,
            style: {
              width: U,
              minWidth: U,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: T[S], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: C.sortable ? (oe) => ce(C.name, C.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Zt(S, oe),
            onDragStart: (oe) => ne(C.name, oe),
            onDragOver: (oe) => _e(C.name, oe),
            onDrop: ve,
            onDragEnd: Ce
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, C.label),
          C.filterable && /* @__PURE__ */ e.createElement(
            "button",
            {
              type: "button",
              className: "tlTableView__filterButton" + (C.filterActive ? " tlTableView__filterButton--active" : ""),
              title: a["js.table.filter"],
              style: {
                border: "none",
                background: "transparent",
                cursor: "pointer",
                padding: "0 4px",
                color: C.filterActive ? "#1565c0" : "inherit"
              },
              onMouseDown: (oe) => oe.stopPropagation(),
              onClick: (oe) => tn(C.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: C.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          C.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, C.sortDirection === "asc" ? "▲" : "▼", k > 1 && C.sortPriority != null && C.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, C.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => j(C.name, U, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (C) => {
            if (B.current && c.length > 0) {
              const S = c[c.length - 1];
              S.name !== B.current && (C.preventDefault(), C.dataTransfer.dropEffect = "move", O({ column: S.name, side: "right" }));
            }
          },
          onDrop: ve
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + ($ ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: V },
        title: a["js.table.freezeSplitter"],
        onMouseDown: en
      }
    ), b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: nn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: _,
        className: "tlTableView__body",
        onScroll: te,
        onKeyDown: Y,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: Qe, paddingRight: Je } }, u.map((C) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C.id,
          className: "tlTableView__row" + (C.selected ? " tlTableView__row--selected" : "") + (C.index === p ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: C.index * r,
            height: r,
            width: Qe,
            paddingRight: Je,
            ...C.index === p ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (S) => {
            (S.shiftKey || S.ctrlKey || S.metaKey || S.detail > 1) && !lt(S) && S.preventDefault();
          },
          onClick: (S) => Se(C.index, S)
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
            onClick: (S) => S.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: C.selected,
              onChange: () => {
              },
              onClick: (S) => le(C.index, S),
              tabIndex: -1
            }
          )
        ),
        c.map((S, U) => {
          const Q = m(S), ee = U === c.length - 1, re = U < f, oe = U === f - 1;
          let fe = "tlTableView__cell";
          re && (fe += " tlTableView__cell--frozen"), oe && (fe += " tlTableView__cell--frozenLast");
          const pe = g && U === 0, se = C.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: S.name,
              className: fe,
              "data-row": C.id,
              "data-col": S.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: T[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: se * y } }, C.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Fe(C.index, !C.expanded, de)
              },
              C.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), C.cells[S.name] && /* @__PURE__ */ e.createElement(K, { control: C.cells[S.name] })) : C.cells[S.name] && /* @__PURE__ */ e.createElement(K, { control: C.cells[S.name] })
          );
        })
      )))
    ),
    $ && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__frozenPreview", style: { left: $.x } }),
    Z && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: Z.y, left: Z.x, zIndex: 1e4 },
        onMouseDown: (C) => C.stopPropagation()
      },
      Z.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Qt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Jt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, _a = {
  "js.table.columnSearch": "Find column"
}, ga = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(_a), s = t.entries ?? [], c = s.filter((_) => _.visible).length, [i, u] = e.useState(""), r = i.trim().toLowerCase(), o = r ? s.filter((_) => _.label.toLowerCase().includes(r)) : s, d = e.useRef(null), p = e.useRef(null), [f, g] = e.useState(null), b = e.useCallback((_) => {
    p.current = _, g(_);
  }, []), k = e.useCallback((_, w) => {
    n("columnVisible", { column: _, visible: w });
  }, [n]), E = e.useCallback((_, w) => {
    d.current = _, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", _);
  }, []), v = e.useCallback((_, w) => {
    if (!d.current || d.current === _) {
      b(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const h = w.currentTarget.getBoundingClientRect(), x = w.clientY < h.top + h.height / 2 ? "top" : "bottom";
    b({ name: _, side: x });
  }, [b]), y = e.useCallback(() => {
    d.current = null, b(null);
  }, [b]), L = e.useCallback((_) => {
    _.preventDefault();
    const w = d.current, h = p.current;
    if (d.current = null, b(null), !w || !h)
      return;
    const x = s.findIndex((z) => z.name === h.name), R = s.findIndex((z) => z.name === w);
    if (x < 0 || R < 0)
      return;
    let N = h.side === "top" ? x : x + 1;
    R < N && N--, N !== R && n("columnReorder", { column: w, targetIndex: N });
  }, [s, n, b]), D = s.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: L }, D && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: i,
      onChange: (_) => u(_.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (D ? " tlColumnSelect__list--fixed" : "") }, o.map((_) => {
    const w = _.visible && c <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === _.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: _.name,
        className: h,
        draggable: !0,
        onDragStart: (x) => E(_.name, x),
        onDragOver: (x) => v(_.name, x),
        onDrop: L,
        onDragEnd: y
      },
      /* @__PURE__ */ e.createElement("i", { className: "tlColumnSelect__handle bi bi-grip-vertical", "aria-hidden": "true" }),
      /* @__PURE__ */ e.createElement("label", { className: "tlColumnSelect__label" }, /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          className: "tlReactCheckbox",
          checked: _.visible,
          disabled: w,
          onChange: (x) => k(_.name, x.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, _.label))
    );
  })));
}, va = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Vt = e.createContext(va), { useMemo: Ea, useRef: Ca, useState: wa, useEffect: ya } = e, ka = 320, Sa = "TLTableView", Na = "TLPanel", Ta = ({ controlId: l }) => {
  var E;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", s = t.readOnly === !0, c = t.children ?? [], i = t.noModelMessage, u = Ca(null), [r, o] = wa(
    a === "top" ? "top" : "side"
  );
  ya(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const y = new ResizeObserver((L) => {
      for (const D of L) {
        const w = D.contentRect.width / n;
        o(w < ka ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const d = Ea(() => ({
    readOnly: s,
    resolvedLabelPosition: r
  }), [s, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = c.length === 1 ? c[0] : void 0, b = !!g && (g.module === Sa || g.module === Na && ((E = g.state) == null ? void 0 : E.bare) === !0), k = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : "",
    b ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, i)) : /* @__PURE__ */ e.createElement(Vt.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: f, ref: u }, c.map((v, y) => /* @__PURE__ */ e.createElement(K, { key: y, control: v }))));
}, { useCallback: Ra } = e, Da = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, La = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(Da), s = t.headerControl ?? null, c = t.headerActions ?? [], i = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, d = t.children ?? [], p = s != null || c.length > 0 || i, f = Ra(() => {
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
}, { useContext: xa, useState: Ia, useCallback: Ma } = e, Pa = ({ controlId: l }) => {
  const t = G(), n = xa(Vt), a = t.label ?? "", s = t.required === !0, c = t.error, i = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, d = t.dirty === !0, p = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, g = t.visible !== !1, b = t.hasTooltip === !0, k = t.field, E = n.readOnly, [v, y] = Ia(!1), L = Ma(() => y((x) => !x), []), D = p === "hidden", _ = c != null, w = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${p}`,
    E ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    !_ && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: g ? void 0 : { display: "none" } }, !D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    a
  ), s && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: L,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: k })), !E && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(qe, { image: i, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, c)), !E && !_ && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((x, R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(qe, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, x)))), !E && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, ja = ({ controlId: l }) => {
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
}, Ba = 20, Aa = () => {
  var w;
  const l = G(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, i = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), d = e.useRef(null), p = ((w = n.find((h) => h.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var x;
    if (p == null)
      return;
    const h = (x = d.current) == null ? void 0 : x.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [p]);
  const f = e.useCallback((h, x) => {
    t(x ? "collapse" : "expand", { nodeId: h });
  }, [t]), g = e.useCallback((h, x) => {
    var N;
    const R = window.getSelection();
    R && !R.isCollapsed && x.currentTarget.contains(R.anchorNode) || ((N = d.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    }));
  }, [t]), b = e.useCallback((h, x) => {
    x.preventDefault(), t("contextMenu", { nodeId: h, x: x.clientX, y: x.clientY });
  }, [t]), k = e.useRef(null), E = e.useCallback((h, x) => {
    const R = x.getBoundingClientRect(), N = h.clientY - R.top, z = R.height / 3;
    return N < z ? "above" : N > z * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, x) => {
    x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", h);
  }, []), y = e.useCallback((h, x) => {
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const R = E(x, x.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: R }), k.current = null;
    }, 50);
  }, [t, E]), L = e.useCallback((h, x) => {
    x.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const R = E(x, x.currentTarget);
    t("drop", { nodeId: h, position: R });
  }, [t, E]), D = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
  }, [t]), _ = e.useCallback((h) => {
    if (n.length === 0) return;
    let x = r;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), x = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), x = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), r >= 0 && r < n.length) {
          const R = n[r];
          if (R.expandable && !R.expanded) {
            t("expand", { nodeId: R.id });
            return;
          } else R.expanded && (x = r + 1);
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
                x = z;
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
        h.preventDefault(), x = 0;
        break;
      case "End":
        h.preventDefault(), x = n.length - 1;
        break;
      default:
        return;
    }
    x !== r && o(x);
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
    n.map((h, x) => /* @__PURE__ */ e.createElement(
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
          x === r ? "tlTreeView__node--focused" : "",
          i === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          i === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          i === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * Ba },
        draggable: s,
        onMouseDown: (R) => {
          (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && R.preventDefault();
        },
        onClick: (R) => g(h.id, R),
        onContextMenu: (R) => b(h.id, R),
        onDragStart: (R) => v(h.id, R),
        onDragOver: c ? (R) => y(h.id, R) : void 0,
        onDrop: c ? (R) => L(h.id, R) : void 0,
        onDragEnd: D
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
var Mt;
function Oa() {
  if (Mt) return J;
  Mt = 1;
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
  }, k = Object.assign, E = {};
  function v(m, T, V) {
    this.props = m, this.context = T, this.refs = E, this.updater = V || b;
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
  function L(m, T, V) {
    this.props = m, this.context = T, this.refs = E, this.updater = V || b;
  }
  var D = L.prototype = new y();
  D.constructor = L, k(D, v.prototype), D.isPureReactComponent = !0;
  var _ = Array.isArray;
  function w() {
  }
  var h = { H: null, A: null, T: null, S: null }, x = Object.prototype.hasOwnProperty;
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
  function B(m) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + m.replace(/[=:]/g, function(V) {
      return T[V];
    });
  }
  var I = /\/+/g;
  function O(m, T) {
    return typeof m == "object" && m !== null && m.key != null ? B("" + m.key) : T.toString(36);
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
      return X = X(m), te = W === "" ? "." + O(m, 0) : W, _(X) ? (V = "", te != null && (V = te.replace(I, "$&/") + "/"), H(X, T, V, "", function(_e) {
        return _e;
      })) : X != null && (z(X) && (X = N(
        X,
        V + (X.key == null || m && m.key === X.key ? "" : ("" + X.key).replace(
          I,
          "$&/"
        ) + "/") + te
      )), T.push(X)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (_(m))
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
  function $(m, T, V) {
    if (m == null) return m;
    var W = [], X = 0;
    return H(m, W, "", "", function(j) {
      return T.call(V, j, X++);
    }), W;
  }
  function A(m) {
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
  }, q = {
    map: $,
    forEach: function(m, T, V) {
      $(
        m,
        function() {
          T.apply(this, arguments);
        },
        V
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
      if (!z(m))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return m;
    }
  };
  return J.Activity = p, J.Children = q, J.Component = v, J.Fragment = n, J.Profiler = s, J.PureComponent = L, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, J.__COMPILER_RUNTIME = {
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
    var W = k({}, m.props), X = m.key;
    if (T != null)
      for (j in T.key !== void 0 && (X = "" + T.key), T)
        !x.call(T, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && T.ref === void 0 || (W[j] = T[j]);
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
        x.call(T, W) && W !== "key" && W !== "__self" && W !== "__source" && (X[W] = T[W]);
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
      _init: A
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
      X !== null && X(V, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(w, P);
    } catch (j) {
      P(j);
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
function Fa() {
  return Pt || (Pt = 1, ot.exports = Oa()), ot.exports;
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
var jt;
function $a() {
  if (jt) return he;
  jt = 1;
  var l = Fa();
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
var Bt;
function Ua() {
  if (Bt) return rt.exports;
  Bt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), rt.exports = $a(), rt.exports;
}
var Kt = Ua();
const { useState: Re, useCallback: be, useRef: $e, useEffect: Me, useMemo: ft } = e;
function gt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(qe, { image: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Ha({
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
function Wa({
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
const za = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], s = t.multiSelect === !0, c = t.customOrder === !0, i = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, d = t.options ?? [], p = t.emptyOptionLabel ?? "", f = c && s && !u && r, g = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = g["js.dropdownSelect.nothingFound"], k = be(
    (M) => g["js.dropdownSelect.removeChip"].replace("{0}", M),
    [g]
  ), [E, v] = Re(!1), [y, L] = Re(""), [D, _] = Re(-1), [w, h] = Re(!1), [x, R] = Re({}), [N, z] = Re(null), [B, I] = Re(null), [O, Z] = Re(null), H = $e(null), $ = $e(null), A = $e(null), P = $e(a);
  P.current = a;
  const q = $e(-1), m = ft(
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
  Me(() => {
    y && T.length === 1 ? _(0) : _(-1);
  }, [T.length, y]), Me(() => {
    E && o && $.current && $.current.focus();
  }, [E, o, a]), Me(() => {
    var le, ie;
    if (q.current < 0) return;
    const M = q.current;
    q.current = -1;
    const Y = (le = H.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    Y && Y.length > 0 ? Y[Math.min(M, Y.length - 1)].focus() : (ie = H.current) == null || ie.focus();
  }, [a]), Me(() => {
    if (!E) return;
    const M = (Y) => {
      H.current && !H.current.contains(Y.target) && A.current && !A.current.contains(Y.target) && (v(!1), L(""));
    };
    return document.addEventListener("mousedown", M), () => document.removeEventListener("mousedown", M);
  }, [E]), Me(() => {
    if (!E || !H.current) return;
    const M = H.current.getBoundingClientRect(), Y = window.innerHeight - M.bottom, ie = Y < 300 && M.top > Y;
    R({
      left: M.left,
      width: M.width,
      ...ie ? { bottom: window.innerHeight - M.top } : { top: M.bottom }
    });
  }, [E]);
  const V = be(async () => {
    if (!(u || !r) && (v(!0), L(""), _(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = be(() => {
    var M;
    v(!1), L(""), _(-1), (M = H.current) == null || M.focus();
  }, []), X = be(
    (M) => {
      let Y;
      if (s) {
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
      P.current = Y, n(We, { value: Y.map((le) => le.value) }), s ? (L(""), _(-1)) : W();
    },
    [s, d, n, W]
  ), j = be(
    (M) => {
      q.current = P.current.findIndex((le) => le.value === M);
      const Y = P.current.filter((le) => le.value !== M);
      P.current = Y, n(We, { value: Y.map((le) => le.value) });
    },
    [n]
  ), te = be(
    (M) => {
      M.stopPropagation(), n(We, { value: [] }), W();
    },
    [n, W]
  ), ce = be((M) => {
    L(M.target.value);
  }, []), ne = be(
    (M) => {
      if (!E) {
        if (M.key === "ArrowDown" || M.key === "ArrowUp" || M.key === "Enter" || M.key === " ") {
          if (M.target.tagName === "BUTTON") return;
          M.preventDefault(), M.stopPropagation(), V();
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
          M.preventDefault(), M.stopPropagation(), D >= 0 && D < T.length && X(T[D].value);
          break;
        case "Escape":
          M.preventDefault(), M.stopPropagation(), W();
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
      E,
      V,
      W,
      T,
      D,
      X,
      y,
      s,
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
  ), ve = be(
    (M, Y) => {
      z(M), Y.dataTransfer.effectAllowed = "move", Y.dataTransfer.setData("text/plain", String(M));
    },
    []
  ), Ce = be(
    (M, Y) => {
      if (Y.preventDefault(), Y.dataTransfer.dropEffect = "move", N === null || N === M) {
        I(null), Z(null);
        return;
      }
      const le = Y.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, Fe = Y.clientX < ie ? "before" : "after";
      I(M), Z(Fe);
    },
    [N]
  ), Se = be(
    (M) => {
      if (M.preventDefault(), N === null || B === null || O === null || N === B) return;
      const Y = [...P.current], [le] = Y.splice(N, 1);
      let ie = B;
      N < B ? ie = O === "before" ? ie - 1 : ie : ie = O === "before" ? ie : ie + 1, Y.splice(ie, 0, le), P.current = Y, n(We, { value: Y.map((Fe) => Fe.value) }), z(null), I(null), Z(null);
    },
    [N, B, O, n]
  ), Ne = be(() => {
    z(null), I(null), Z(null);
  }, []);
  if (Me(() => {
    if (D < 0 || !A.current) return;
    const M = A.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    M && M.scrollIntoView({ block: "nearest" });
  }, [D, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((M) => /* @__PURE__ */ e.createElement("span", { key: M.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(gt, { image: M.image }), /* @__PURE__ */ e.createElement("span", null, M.label))));
  const xe = !i && a.length > 0 && !u, He = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: x,
      ...sn
    },
    (o || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: $,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ce,
        onKeyDown: ne,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": D >= 0 ? `${l}-opt-${D}` : void 0,
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
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      o && T.map((M, Y) => /* @__PURE__ */ e.createElement(
        Wa,
        {
          key: M.value,
          id: `${l}-opt-${Y}`,
          option: M,
          highlighted: Y === D,
          searchTerm: y,
          onSelect: X,
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
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((M, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : B === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : B === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Ha,
        {
          key: M.value,
          option: M,
          removable: !u && (s || !i),
          onRemove: j,
          removeLabel: k(M.label),
          draggable: f,
          onDragStart: f ? (ie) => ve(Y, ie) : void 0,
          onDragOver: f ? (ie) => Ce(Y, ie) : void 0,
          onDrop: f ? Se : void 0,
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), He && Kt.createPortal(He, document.body));
}, { useCallback: st, useRef: Va } = e, Yt = "application/x-tl-color", Ka = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: s,
  onReplace: c
}) => {
  const i = Va(null), u = st(
    (d) => (p) => {
      i.current = d, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = st((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), o = st(
    (d) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(Yt);
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
function Gt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ht(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Xt(l) {
  if (!ht(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function qt(l, t, n) {
  const a = (s) => Gt(s).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ya(l, t, n) {
  const a = l / 255, s = t / 255, c = n / 255, i = Math.max(a, s, c), u = Math.min(a, s, c), r = i - u;
  let o = 0;
  r !== 0 && (i === a ? o = (s - c) / r % 6 : i === s ? o = (c - a) / r + 2 : o = (a - s) / r + 4, o *= 60, o < 0 && (o += 360));
  const d = i === 0 ? 0 : r / i;
  return [o, d, i];
}
function Ga(l, t, n) {
  const a = n * t, s = a * (1 - Math.abs(l / 60 % 2 - 1)), c = n - a;
  let i = 0, u = 0, r = 0;
  return l < 60 ? (i = a, u = s, r = 0) : l < 120 ? (i = s, u = a, r = 0) : l < 180 ? (i = 0, u = a, r = s) : l < 240 ? (i = 0, u = s, r = a) : l < 300 ? (i = s, u = 0, r = a) : (i = a, u = 0, r = s), [
    Math.round((i + c) * 255),
    Math.round((u + c) * 255),
    Math.round((r + c) * 255)
  ];
}
function Xa(l) {
  return Ya(...Xt(l));
}
function ct(l, t, n) {
  return qt(...Ga(l, t, n));
}
const { useCallback: Pe, useRef: At } = e, qa = ({ color: l, onColorChange: t }) => {
  const [n, a, s] = Xa(l), c = At(null), i = At(null), u = Pe(
    (b, k) => {
      var L;
      const E = (L = c.current) == null ? void 0 : L.getBoundingClientRect();
      if (!E) return;
      const v = Math.max(0, Math.min(1, (b - E.left) / E.width)), y = Math.max(0, Math.min(1, 1 - (k - E.top) / E.height));
      t(ct(n, v, y));
    },
    [n, t]
  ), r = Pe(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), o = Pe(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), d = Pe(
    (b) => {
      var y;
      const k = (y = i.current) == null ? void 0 : y.getBoundingClientRect();
      if (!k) return;
      const v = Math.max(0, Math.min(1, (b - k.top) / k.height)) * 360;
      t(ct(v, a, s));
    },
    [a, s, t]
  ), p = Pe(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientY);
    },
    [d]
  ), f = Pe(
    (b) => {
      b.buttons !== 0 && d(b.clientY);
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
function Za(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Qa = {
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
}, { useState: Ke, useCallback: we, useEffect: Ot, useRef: Ja, useLayoutEffect: er } = e, tr = ({
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
  const [o, d] = Ke("palette"), [p, f] = Ke(t), g = Ja(null), b = ue(Qa), [k, E] = Ke(null);
  er(() => {
    if (!l.current || !g.current) return;
    const A = l.current.getBoundingClientRect(), P = g.current.getBoundingClientRect();
    let q = A.bottom + 4, m = A.left;
    q + P.height > window.innerHeight && (q = A.top - P.height - 4), m + P.width > window.innerWidth && (m = Math.max(0, A.right - P.width)), E({ top: q, left: m });
  }, [l]);
  const v = p != null, [y, L, D] = v ? Xt(p) : [0, 0, 0], [_, w] = Ke((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ot(() => {
    w((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Le(!0, { ESCAPE: u }), Ot(() => {
    const A = (q) => {
      g.current && !g.current.contains(q.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const h = we(
    (A) => (P) => {
      const q = parseInt(P.target.value, 10);
      if (isNaN(q)) return;
      const m = Gt(q);
      f(qt(A === "r" ? m : y, A === "g" ? m : L, A === "b" ? m : D));
    },
    [y, L, D]
  ), x = we(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(Yt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), R = we((A) => {
    const P = A.target.value;
    w(P), ht(P) && f(P);
  }, []), N = we(() => {
    f(null);
  }, []), z = we((A) => {
    f(A);
  }, []), B = we(
    (A) => {
      i(A);
    },
    [i]
  ), I = we(
    (A, P) => {
      const q = [...n], m = q[A];
      q[A] = q[P], q[P] = m, r(q);
    },
    [n, r]
  ), O = we(
    (A, P) => {
      const q = [...n];
      q[A] = P, r(q);
    },
    [n, r]
  ), Z = we(() => {
    r([...s]);
  }, [s, r]), H = we(
    (A) => {
      if (Za(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const q = [...n];
      q[P] = A.toUpperCase(), r(q);
    },
    [n, r]
  ), $ = we(() => {
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
      Ka,
      {
        colors: n,
        columns: a,
        onSelect: z,
        onConfirm: B,
        onSwap: I,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Z }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(qa, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: v ? x : void 0
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
        value: v ? L : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? D : "",
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
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: $ }, b["js.colorInput.ok"]))
  );
}, nr = { "js.colorInput.chooseColor": "Choose color" }, { useState: lr, useCallback: Ye, useRef: ar } = e, rr = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = ae(), c = ue(nr), [i, u] = lr(!1), r = ar(null), o = n, d = t.editable !== !1, p = t.palette ?? [], f = t.paletteColumns ?? 6, g = t.defaultPalette ?? p, b = Ye(() => {
    d && u(!0);
  }, [d]), k = Ye(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Ye(() => {
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
      onClick: b,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": c["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    tr,
    {
      anchorRef: r,
      currentColor: o,
      palette: p,
      paletteColumns: f,
      defaultPalette: g,
      canReset: t.canReset !== !1,
      onConfirm: k,
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
}, { useState: Ue, useCallback: De, useEffect: it, useRef: Ft, useLayoutEffect: or, useMemo: sr } = e, cr = {
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
}, ir = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: s,
  onCancel: c,
  onLoadIcons: i
}) => {
  const u = ue(cr), [r, o] = Ue("simple"), [d, p] = Ue(""), [f, g] = Ue(t ?? ""), [b, k] = Ue(!1), [E, v] = Ue(null), y = Ft(null), L = Ft(null);
  or(() => {
    if (!l.current || !y.current) return;
    const B = l.current.getBoundingClientRect(), I = y.current.getBoundingClientRect();
    let O = B.bottom + 4, Z = B.left;
    O + I.height > window.innerHeight && (O = B.top - I.height - 4), Z + I.width > window.innerWidth && (Z = Math.max(0, B.right - I.width)), v({ top: O, left: Z });
  }, [l]), it(() => {
    !a && !b && i().catch(() => k(!0));
  }, [a, b, i]), it(() => {
    a && L.current && L.current.focus();
  }, [a]), Le(!0, { ESCAPE: c }), it(() => {
    const B = (O) => {
      y.current && !y.current.contains(O.target) && c();
    }, I = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", B);
    };
  }, [c]);
  const D = sr(() => {
    if (!d) return n;
    const B = d.toLowerCase();
    return n.filter(
      (I) => I.prefix.toLowerCase().includes(B) || I.label.toLowerCase().includes(B) || I.terms != null && I.terms.some((O) => O.includes(B))
    );
  }, [n, d]), _ = De((B) => {
    p(B.target.value);
  }, []), w = De(
    (B) => {
      s(B);
    },
    [s]
  ), h = De((B) => {
    g(B);
  }, []), x = De((B) => {
    g(B.target.value);
  }, []), R = De(() => {
    s(f || null);
  }, [f, s]), N = De(() => {
    s(null);
  }, [s]), z = De(async (B) => {
    B.preventDefault(), k(!1);
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
        ref: L,
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: z }, u["js.iconSelect.loadError"])),
      a && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && D.map(
        (B) => B.variants.map((I) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: I.encoded,
            className: "tlIconSelect__iconCell" + (I.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": I.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => r === "simple" ? w(I.encoded) : h(I.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), r === "simple" ? w(I.encoded) : h(I.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ye, { encoded: I.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: x
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ye, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, ur = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: dr, useCallback: Ge, useRef: mr } = e, pr = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = ae(), c = ue(ur), [i, u] = dr(!1), r = mr(null), o = n, d = t.editable !== !1, p = t.disabled === !0, f = t.icons ?? [], g = t.iconsLoaded === !0, b = Ge(() => {
    d && !p && u(!0);
  }, [d, p]), k = Ge(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Ge(() => {
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
    ir,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: g,
      onSelect: k,
      onCancel: E,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: je, useEffect: fr, useMemo: $t, useRef: hr, useState: ut } = e, br = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, _r = [1, 2, 3, 4];
function gr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), s = n[2] || "px";
  return s === "rem" || s === "em" ? a * t : a;
}
function vr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const s of _r)
    n >= s && (a = s);
  return a;
}
function Er(l, t) {
  const n = br[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Cr(l, t) {
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
    let g = Math.min(Er(p.width, n), n);
    for (; s(u, r); )
      r++, r >= n && (r = 0, u++);
    let b = 0;
    for (let L = r; L < n && !s(u, L); L++)
      b++;
    if (g > b) {
      for (o(u), r = 0, u++; s(u, r); )
        r++, r >= n && (r = 0, u++);
      b = 0;
      for (let L = r; L < n && !s(u, L); L++)
        b++;
      g = Math.min(g, b);
    }
    const k = r, E = r + g, v = u, y = u + f;
    i.push({ id: p.id, colStart: k, colEnd: E, rowStart: v, rowEnd: y });
    for (let L = v; L < y; L++)
      for (let D = k; D < E; D++) c(L, D);
    r = E, r >= n && (r = 0, u++);
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
const wr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.minColWidth ?? "16rem", s = (t.children ?? []).filter((w) => w && w.id), c = hr(null), [i, u] = ut(1), r = t.editMode === !0;
  fr(() => {
    const w = c.current;
    if (!w) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, x = gr(a, h), R = () => u(vr(w.clientWidth, x));
    R();
    const N = new ResizeObserver(R);
    return N.observe(w), () => N.disconnect();
  }, [a]);
  const o = $t(() => Cr(s, i), [s, i]), d = $t(() => {
    const w = {};
    for (const h of o) w[h.id] = h;
    return w;
  }, [o]), [p, f] = ut(null), [g, b] = ut(null), k = je((w, h) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    f(h), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", h);
  }, [r]), E = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const x = w.currentTarget.getBoundingClientRect(), R = w.clientX < x.left + x.width / 2;
    b((N) => N && N.id === h && N.before === R ? N : { id: h, before: R });
  }, [r, p]), v = je(() => {
  }, []), y = je((w, h, x) => {
    const R = s.map((I) => I.id), N = R.indexOf(w);
    if (N < 0) return;
    R.splice(N, 1);
    const z = R.indexOf(h);
    if (z < 0) {
      R.splice(N, 0, w);
      return;
    }
    const B = x ? z : z + 1;
    R.splice(B, 0, w), n("reorder", { order: R });
  }, [s, n]), L = je((w, h) => {
    if (!r || !p || p === h) return;
    w.preventDefault();
    const x = w.currentTarget.getBoundingClientRect(), R = w.clientX < x.left + x.width / 2;
    y(p, h, R), f(null), b(null);
  }, [r, p, y]), D = je(() => {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, s.map((w) => {
      const h = d[w.id];
      if (!h) return null;
      const x = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, R = ["tlDashboard__tile"];
      return p === w.id && R.push("tlDashboard__tile--dragging"), g && g.id === w.id && R.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: R.join(" "),
          style: x,
          draggable: r,
          onDragStart: (N) => k(N, w.id),
          onDragOver: (N) => E(N, w.id),
          onDragLeave: v,
          onDrop: (N) => L(N, w.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: yr, useRef: Ut, useState: Ht, useEffect: kr, useLayoutEffect: Sr } = e, Nr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, Tr = ({ group: l }) => {
  var p, f;
  const [t, n] = Ht(!1), [a, s] = Ht({}), c = Ut(null), i = Ut(null), u = yr(() => {
    n((g) => !g);
  }, []);
  Sr(() => {
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
  }, [t]), kr(() => {
    if (!t) return;
    const g = (b) => {
      i.current && !i.current.contains(b.target) && c.current && !c.current.contains(b.target) && n(!1);
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
  ), Kt.createPortal(
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
      (f = l.subGroups) == null ? void 0 : f.map((g, b) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${b}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((k, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: k })))))
    ),
    document.body
  ));
}, Rr = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((s) => s.items.some((c) => c != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((s, c) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(Tr, { group: s }) : /* @__PURE__ */ e.createElement(Nr, { group: s }))));
}, Dr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, Lr = ({ controlId: l }) => {
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
}, xr = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: a })));
}, Ir = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Mr = {
  "js.sidebar.openDrawer": "Open navigation"
}, Pr = ({ controlId: l }) => {
  const t = ae(), n = ue(Mr);
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
F("TLButton", Tn);
F("TLUploadButton", Rn);
F("TLToggleButton", Ln);
F("TLTextInput", dn);
F("TLPasswordInput", pn);
F("TLNumberInput", hn);
F("TLDatePicker", _n);
F("TLSelect", vn);
F("TLBooleanChoice", Cn);
F("TLCheckbox", Sn);
F("TLCounter", xn);
F("TLTabBar", Mn);
F("TLFieldList", Pn);
F("TLAudioRecorder", Bn);
F("TLAudioPlayer", On);
F("TLFileUpload", $n);
F("TLBinaryField", Hn);
F("TLFileChips", Vn);
F("TLRelativeTime", Gn);
F("TLAnchor", Xn);
F("TLScrollLink", qn);
F("TLAvatar", Jn);
F("TLDownload", tl);
F("TLPhotoCapture", ll);
F("TLPhotoViewer", rl);
F("TLPdfViewer", sl);
F("TLSplitPanel", cl);
F("TLPanel", hl);
F("TLInset", Tl);
F("TLMaximizeRoot", bl);
F("TLDeckPane", _l);
F("TLSidebar", Sl);
F("TLStack", Nl);
F("TLGrid", Rl);
F("TLCard", Dl);
F("TLAppBar", Ll);
F("TLBreadcrumb", Il);
F("TLBottomBar", Pl);
F("TLDialog", Al);
F("TLDialogManager", $l);
F("TLWindow", zl);
F("TLDrawer", Yl);
F("TLContextMenuRegion", Xl);
F("TLSnackbar", Jl);
F("TLNoticeBar", oa);
F("TLMenu", ca);
F("TLAppShell", ua);
F("TLText", da);
F("TLTableView", ba);
F("TLColumnSelect", ga);
F("TLFormLayout", Ta);
F("TLFormGroup", La);
F("TLFormField", Pa);
F("TLResourceCell", ja);
F("TLTreeView", Aa);
F("TLDropdownSelect", za);
F("TLColorInput", rr);
F("TLIconSelect", pr);
F("TLDashboard", wr);
F("TLToolbar", Rr);
F("TLTileStack", Dr);
F("TLAdaptiveDetail", Lr);
F("TLSlot", xr);
F("TLSlotContent", Ir);
F("TLDrawerToggle", Pr);
