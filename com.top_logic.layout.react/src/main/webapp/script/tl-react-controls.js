import { React as e, useTLFieldValue as ke, useTLCommand as ae, useTLState as G, useKeyboardBinding as me, useTLUpload as Ae, TLChild as K, useI18N as ue, useTLDataUrl as Oe, scrollToAnchor as sn, useStandaloneKeyboardScope as Le, KeyboardScopeProvider as gt, useFocusTrap as _t, CMD_VALUE_CHANGED as We, anchoredOverlayProps as cn, register as F } from "tl-react-bridge";
const { useCallback: yt, useRef: un } = e, dn = 300, mn = ({ controlId: l, state: t }) => {
  const [n, a, s] = ke({
    debounceMs: dn,
    sendOnBlur: t.sendValueOnBlur === !0
  }), i = ae(), c = un(!1), u = yt(
    (k) => {
      c.current = !0, a(k.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, o = yt(async () => {
    await s(), r && c.current && (c.current = !1, i("commit"));
  }, [s, r, i]), m = t.multiline === !0;
  if (t.editable === !1) {
    const k = "tlReactTextInput tlReactTextInput--immutable" + (m ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: k,
        style: m ? { whiteSpace: "pre-wrap" } : void 0
      },
      n ?? ""
    );
  }
  const d = t.hasError === !0, f = t.hasWarnings === !0, _ = t.errorMessage, g = [
    "tlReactTextInput",
    m ? "tlReactTextInput--multiline" : "",
    d ? "tlReactTextInput--error" : "",
    !d && f ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, m ? /* @__PURE__ */ e.createElement(
    "textarea",
    {
      rows: t.rows ?? 3,
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: u,
      onBlur: o,
      disabled: t.disabled === !0,
      className: g,
      "aria-invalid": d || void 0,
      title: d && _ ? _ : void 0
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
      className: g,
      "aria-invalid": d || void 0,
      title: d && _ ? _ : void 0
    }
  ));
}, { useCallback: kt } = e, pn = 300, fn = ({ controlId: l, state: t }) => {
  const [n, a, s] = ke({ debounceMs: pn }), i = kt(
    (d) => {
      a(d.target.value);
    },
    [a]
  ), c = kt(() => {
    s();
  }, [s]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const u = t.hasError === !0, r = t.hasWarnings === !0, o = t.errorMessage, m = [
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
      className: m,
      "aria-invalid": u || void 0,
      title: u && o ? o : void 0
    }
  ));
}, { useCallback: St } = e, hn = 300, bn = ({ controlId: l, state: t, config: n }) => {
  const [a, s, i] = ke({ debounceMs: hn }), c = St(
    (f) => {
      const _ = f.target.value;
      s(_ === "" ? null : _);
    },
    [s]
  ), u = St(() => {
    i();
  }, [i]);
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const r = t.hasError === !0, o = t.hasWarnings === !0, m = t.errorMessage, d = [
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
      onChange: c,
      onBlur: u,
      disabled: t.disabled === !0,
      className: d,
      "aria-invalid": r || void 0,
      title: r && m ? m : void 0
    }
  ));
}, { useCallback: gn } = e, _n = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = gn(
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
      onChange: s,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: vn } = e, En = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, s] = ke(), i = vn(
    (d) => {
      s(d.target.value || null);
    },
    [s]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const d = ((m = c.find((f) => f.value === a)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, d);
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
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((d) => /* @__PURE__ */ e.createElement("option", { key: d.value, value: d.value }, d.label))
  ));
}, { useCallback: Cn } = e, wn = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = t.options ?? [], i = t.presentation === "select", c = t.disabled === !0, u = t.hasError === !0, r = t.hasWarnings === !0, o = Cn(
    (f) => {
      const _ = s[f];
      a(_ ? _.value : null);
    },
    [s, a]
  ), m = s.findIndex((f) => f.value === (n ?? null));
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlBooleanChoice tlBooleanChoice--immutable" }, m >= 0 ? s[m].label : "");
  const d = [
    "tlBooleanChoice",
    u ? "tlBooleanChoice--error" : "",
    !u && r ? "tlBooleanChoice--warning" : ""
  ].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      className: d + " tlReactSelect",
      value: m >= 0 ? String(m) : "",
      disabled: c,
      "aria-invalid": u || void 0,
      onChange: (f) => o(Number(f.target.value))
    },
    m < 0 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((f, _) => /* @__PURE__ */ e.createElement("option", { key: _, value: String(_) }, f.label))
  ) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: d + " tlBooleanChoice--radio",
      role: "radiogroup",
      "aria-invalid": u || void 0
    },
    s.map((f, _) => /* @__PURE__ */ e.createElement("label", { key: _, className: "tlBooleanChoice__option" }, /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "radio",
        name: l,
        checked: m === _,
        disabled: c,
        onChange: () => o(_)
      }
    ), /* @__PURE__ */ e.createElement("span", { className: "tlBooleanChoice__label" }, f.label)))
  );
}, { useCallback: yn, useRef: kn, useEffect: Sn } = e, Nn = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = t.triState === !0, i = kn(null);
  Sn(() => {
    i.current && (i.current.indeterminate = s && n !== !0 && n !== !1);
  }, [s, n]);
  const c = yn(
    (m) => {
      if (!s) {
        a(m.target.checked);
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
        ref: i,
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
      ref: i,
      checked: n === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0,
      "aria-checked": s && n !== !0 && n !== !1 ? "mixed" : n === !0
    }
  );
};
function ye({ encoded: l, className: t }) {
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
const { useCallback: Tn } = e, Rn = ({ controlId: l, command: t, label: n, image: a, disabled: s, displayMode: i }) => {
  const c = G(), u = ae(), r = t ?? "click", o = n ?? c.label, m = a ?? c.image, d = s ?? c.disabled === !0, f = i ?? c.displayMode ?? "label-only", _ = c.hidden === !0, g = c.tooltip, k = c.appearance, E = c.size, v = c.cssClasses, y = c.navigateUrl, L = Tn(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    u(r);
  }, [u, r, y]), x = c.keyGesture;
  me(x, () => d || _ ? !1 : (L(), !0));
  const b = f === "icon-only", w = f === "label-only" || f === "icon-label" || b && !m, h = g ?? (b ? o : void 0), D = h ? `text:${h}` : void 0;
  return _ ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: L,
      disabled: d,
      className: "tlReactButton" + (b ? " tlReactButton--iconOnly" : "") + (f === "label-only" ? " tlReactButton--labelOnly" : "") + (k === "link" ? " tlReactButton--link" : "") + (k === "primary" ? " tlReactButton--primary" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : "") + (v ? " " + v : ""),
      "data-tooltip": D,
      "aria-label": m || b ? o : void 0
    },
    m && /* @__PURE__ */ e.createElement(ye, { encoded: m, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, o)
  );
}, Dn = ({ controlId: l }) => {
  const t = G(), n = Ae(), a = e.useRef(null), [s, i] = e.useState(!1), c = t.label ?? "", u = t.image, r = t.disabled === !0, o = t.hidden === !0, m = t.displayMode ?? "label-only", d = t.appearance, f = t.accept, _ = t.multiple === !0, g = e.useCallback(() => {
    var x;
    r || s || (x = a.current) == null || x.click();
  }, [r, s]), k = e.useCallback(async (x) => {
    const b = x.target.files;
    if (!b || b.length === 0) return;
    const w = new FormData();
    for (let h = 0; h < b.length; h++)
      w.append("file", b[h], b[h].name);
    x.target.value = "", i(!0);
    try {
      await n(w);
    } finally {
      i(!1);
    }
  }, [n]), E = m === "icon-only", v = m === "icon-only" || m === "icon-label", y = m === "label-only" || m === "icon-label" || E && !u, L = r || s;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: f && f !== "*" ? f : void 0,
      multiple: _ || void 0,
      onChange: k,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: L,
      style: o ? { display: "none" } : void 0,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (d === "link" ? " tlReactButton--link" : "") + (d === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": E ? c : void 0
    },
    v && u && /* @__PURE__ */ e.createElement(ye, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  ));
}, { useCallback: Ln } = e, xn = ({ controlId: l, command: t, label: n, active: a, disabled: s }) => {
  const i = G(), c = ae(), u = t ?? "click", r = n ?? i.label, o = a ?? i.active === !0, m = s ?? i.disabled === !0, d = Ln(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: d,
      disabled: m,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    r
  );
}, In = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Mn } = e, Pn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.tabs ?? [], s = t.activeTabId, i = Mn((c) => {
    c !== s && n("selectTab", { tabId: c });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === s,
      className: "tlReactTabBar__tab" + (c.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(c.id)
    },
    c.icon && /* @__PURE__ */ e.createElement(ye, { encoded: c.icon, className: "tlReactTabBar__tabIcon" }),
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, jn = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((s, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: s })))));
}, Bn = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, An = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [i, c] = e.useState(null), u = e.useRef(null), r = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", d = t.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, _ = e.useCallback(async () => {
    if (a === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (a !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = y, r.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, L ? { mimeType: L } : void 0);
        u.current = x, x.ondataavailable = (b) => {
          b.data.size > 0 && r.current.push(b.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((h) => h.stop()), o.current = null;
          const b = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], b.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const w = new FormData();
          w.append("audio", b, "recording.webm"), await n(w), s("idle");
        }, x.start(), s("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), c("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [a, n]), g = ue(Bn), k = f === "recording" ? g["js.audioRecorder.stop"] : f === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], E = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: _,
      disabled: E,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[i]), d && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, d));
}, On = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Fn = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasAudio, s = t.dataRevision ?? 0, [i, c] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), o = e.useRef(s);
  e.useEffect(() => {
    a ? i === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), c("disabled"));
  }, [a]), e.useEffect(() => {
    s !== o.current && (o.current = s, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (i === "playing" || i === "paused" || i === "loading") && c("idle"));
  }, [s]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const m = e.useCallback(async () => {
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
        const E = await fetch(n);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), c("idle");
          return;
        }
        const v = await E.blob();
        r.current = URL.createObjectURL(v);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), c("idle");
        return;
      }
    }
    const k = new Audio(r.current);
    u.current = k, k.onended = () => {
      c("idle");
    }, k.play(), c("playing");
  }, [i, n]), d = ue(On), f = i === "loading" ? d["js.loading"] : i === "playing" ? d["js.audioPlayer.pause"] : i === "disabled" ? d["js.audioPlayer.noAudio"] : d["js.audioPlayer.play"], _ = i === "disabled" || i === "loading", g = ["tlAudioPlayer__button"];
  return i === "playing" && g.push("tlAudioPlayer__button--playing"), i === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: m,
      disabled: _,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, $n = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Un = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [i, c] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", o = t.error, m = t.accept ?? "", d = r === "received" ? "idle" : a !== "idle" ? a : r, f = e.useCallback(async (b) => {
    s("uploading");
    const w = new FormData();
    w.append("file", b, b.name), await n(w), s("idle");
  }, [n]), _ = e.useCallback((b) => {
    var h;
    const w = (h = b.target.files) == null ? void 0 : h[0];
    w && f(w);
  }, [f]), g = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = u.current) == null || b.click());
  }, [a]), k = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!0);
  }, []), E = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!1);
  }, []), v = e.useCallback((b) => {
    var h;
    if (b.preventDefault(), b.stopPropagation(), c(!1), a === "uploading") return;
    const w = (h = b.dataTransfer.files) == null ? void 0 : h[0];
    w && f(w);
  }, [a, f]), y = d === "uploading", L = ue($n), x = d === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: k,
      onDragLeave: E,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: _,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (d === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: g,
        disabled: y,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Hn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Wn = ({ controlId: l, state: t }) => {
  const a = G() ?? t ?? {}, s = Ae(), i = Oe(), c = ue(Hn), u = a.editable !== !1, r = !!a.hasData, o = a.fileName ?? "download", m = a.dataRevision ?? 0, d = a.accept ?? "", f = a.status ?? "idle", _ = a.error ?? null, [g, k] = e.useState("idle"), [E, v] = e.useState(!1), [y, L] = e.useState(!1), x = e.useRef(null), b = e.useCallback(async () => {
    if (!(!r || y)) {
      L(!0);
      try {
        const $ = i + (i.includes("?") ? "&" : "?") + "rev=" + m, A = await fetch($);
        if (!A.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", A.status);
          return;
        }
        const P = await A.blob(), q = URL.createObjectURL(P), p = document.createElement("a");
        p.href = q, p.download = o, p.style.display = "none", document.body.appendChild(p), p.click(), document.body.removeChild(p), URL.revokeObjectURL(q);
      } catch ($) {
        console.error("[TLBinaryField] Fetch error:", $);
      } finally {
        L(!1);
      }
    }
  }, [r, y, i, m, o]), w = e.useCallback(async ($) => {
    k("uploading");
    const A = new FormData();
    A.append("file", $, $.name), await s(A), k("idle");
  }, [s]), h = (f === "received" ? "idle" : g !== "idle" ? g : f) === "uploading", D = e.useCallback(($) => {
    var P;
    const A = (P = $.target.files) == null ? void 0 : P[0];
    A && w(A);
  }, [w]), R = e.useCallback(() => {
    var $;
    h || ($ = x.current) == null || $.click();
  }, [h]), N = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!0);
  }, []), z = e.useCallback(($) => {
    $.preventDefault(), $.stopPropagation(), v(!1);
  }, []), B = e.useCallback(($) => {
    var P;
    if ($.preventDefault(), $.stopPropagation(), v(!1), h) return;
    const A = (P = $.dataTransfer.files) == null ? void 0 : P[0];
    A && w(A);
  }, [h, w]), I = y ? c["js.downloading"] : c["js.download.file"].replace("{0}", o), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: y,
      title: I,
      "aria-label": I
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const Z = h, H = h ? c["js.uploading"] : c["js.fileUpload.choose"];
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
        ref: x,
        type: "file",
        accept: d || void 0,
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
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, _)
  );
}, zn = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function Vn(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Kn = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Ae(), s = Oe(), i = ue(zn), c = t.chips ?? [], u = t.editable === !0, [r, o] = e.useState(!1), [m, d] = e.useState(!1), f = e.useRef(null), _ = e.useCallback(async (b) => {
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
  }, [a]), g = e.useCallback(async (b) => {
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
  }, [s]), k = e.useCallback((b) => {
    b.target.files && _(b.target.files), b.target.value = "";
  }, [_]), E = e.useCallback(() => {
    var b;
    r || (b = f.current) == null || b.click();
  }, [r]), v = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), d(!0));
  }, [u]), y = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), d(!1));
  }, [u]), L = e.useCallback((b) => {
    u && (b.preventDefault(), b.stopPropagation(), d(!1), !r && b.dataTransfer.files && _(b.dataTransfer.files));
  }, [u, r, _]), x = [
    "tlFileChips",
    u ? "tlFileChips--editable" : "",
    m ? "tlFileChips--dragover" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: x,
      onDragOver: v,
      onDragLeave: y,
      onDrop: L
    },
    c.map((b) => {
      const w = i["js.download.file"].replace("{0}", b.name), h = i["js.fileChips.remove"].replace("{0}", b.name);
      return /* @__PURE__ */ e.createElement("span", { key: b.key, className: "tlFileChip" }, /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlFileChip__main",
          onClick: () => g(b),
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
        b.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, Vn(b.size))
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
}, Yn = 3e4;
function Gn(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), s = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? s.format(Math.trunc(n / 1), "second") : a < 3600 ? s.format(Math.trunc(n / 60), "minute") : a < 86400 ? s.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? s.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Xn = ({ controlId: l }) => {
  const t = G(), n = t.timestamp, a = t.label ?? void 0, s = t.locale || navigator.language, [, i] = e.useState(0);
  return e.useEffect(() => {
    const c = setInterval(() => i((u) => u + 1), Yn);
    return () => clearInterval(c);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Gn(n, s));
}, qn = ({ controlId: l }) => {
  const t = G(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Zn = ({ controlId: l }) => {
  const t = G(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const s = (i) => {
    i.preventDefault(), sn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: s }, a);
};
function Qn(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Jn(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const el = ({ controlId: l }) => {
  const n = G().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Jn(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Qn(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, tl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, nl = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = ae(), s = !!t.hasData, i = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [r, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!s || r)) {
      o(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + i, k = await fetch(g);
        if (!k.ok) {
          console.error("[TLDownload] Failed to fetch data:", k.status);
          return;
        }
        const E = await k.blob(), v = URL.createObjectURL(E), y = document.createElement("a");
        y.href = v, y.download = c, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(v);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        o(!1);
      }
    }
  }, [s, r, n, i, c]), d = e.useCallback(async () => {
    s && await a("clear");
  }, [s, a]), f = ue(tl);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const _ = r ? f["js.downloading"] : f["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: r,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: d,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, ll = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, al = ({ controlId: l }) => {
  const t = G(), n = Ae(), [a, s] = e.useState("idle"), [i, c] = e.useState(null), [u, r] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), d = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), g = t.error, k = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    E(), s("idle");
  }, [E]), y = e.useCallback(async () => {
    var N;
    if (a !== "uploading") {
      if (c(null), !k) {
        (N = f.current) == null || N.click();
        return;
      }
      try {
        const z = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = z, s("overlayOpen");
      } catch (z) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", z), c("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [a, k]), L = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const N = o.current, z = d.current;
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
  }, [a, n, E]), x = e.useCallback(async (N) => {
    var I;
    const z = (I = N.target.files) == null ? void 0 : I[0];
    if (!z) return;
    s("uploading");
    const B = new FormData();
    B.append("photo", z, z.name), await n(B), s("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var z;
    if (a !== "overlayOpen") return;
    (z = _.current) == null || z.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [a]), Le(a === "overlayOpen", { ESCAPE: v }), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((N) => N.stop()), m.current = null);
  }, []);
  const b = ue(ll), w = a === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], h = ["tlPhotoCapture__cameraBtn"];
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
  )), !k && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: d, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: _,
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
        onClick: L,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[i]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, rl = {
  "js.photoViewer.alt": "Captured photo"
}, ol = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPhoto, s = t.dataRevision ?? 0, [i, c] = e.useState(null), u = e.useRef(s);
  e.useEffect(() => {
    if (!a) {
      i && (URL.revokeObjectURL(i), c(null));
      return;
    }
    if (s === u.current && i)
      return;
    u.current = s, i && (URL.revokeObjectURL(i), c(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const d = await m.blob();
        o || c(URL.createObjectURL(d));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [a, s, n]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const r = ue(rl);
  return !a || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, sl = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, cl = ({ controlId: l }) => {
  const t = G(), n = Oe(), a = !!t.hasPdf, s = t.dataRevision ?? 0, i = ue(sl), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, o = n + "&rev=" + s, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(o);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: i["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, i["js.pdfViewer.noDocument"]));
}, { useCallback: Nt, useRef: et } = e, il = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.orientation, s = t.resizable === !0, i = t.children ?? [], c = a === "horizontal", u = i.length > 0 && i.every((E) => E.collapsed), r = !u && i.some((E) => E.collapsed), o = u ? !c : c, m = et(null), d = et(null), f = et(null), _ = Nt((E, v) => {
    const y = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : v !== void 0 ? y.flex = `0 0 ${v}px` : y.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (y.minWidth = c ? E.minSize : void 0, y.minHeight = c ? void 0 : E.minSize), y;
  }, [c, u, r, o]), g = Nt((E, v) => {
    E.preventDefault();
    const y = m.current;
    if (!y) return;
    const L = i[v], x = i[v + 1], b = y.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    b.forEach((R) => {
      w.push(c ? R.offsetWidth : R.offsetHeight);
    }), f.current = w, d.current = {
      splitterIndex: v,
      startPos: c ? E.clientX : E.clientY,
      startSizeBefore: w[v],
      startSizeAfter: w[v + 1],
      childBefore: L,
      childAfter: x
    };
    const h = (R) => {
      const N = d.current;
      if (!N || !f.current) return;
      const B = (c ? R.clientX : R.clientY) - N.startPos, I = N.childBefore.minSize || 0, O = N.childAfter.minSize || 0;
      let Z = N.startSizeBefore + B, H = N.startSizeAfter - B;
      Z < I && (H += Z - I, Z = I), H < O && (Z += H - O, H = O), f.current[N.splitterIndex] = Z, f.current[N.splitterIndex + 1] = H;
      const $ = y.querySelectorAll(":scope > .tlSplitPanel__child"), A = $[N.splitterIndex], P = $[N.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${Z}px`), P && (P.style.flex = `0 0 ${H}px`);
    }, D = () => {
      if (document.removeEventListener("mousemove", h), document.removeEventListener("mouseup", D), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const R = {};
        i.forEach((N, z) => {
          const B = N.control;
          B != null && B.controlId && f.current && (R[B.controlId] = f.current[z]);
        }), n("updateSizes", { sizes: R });
      }
      f.current = null, d.current = null;
    };
    document.addEventListener("mousemove", h), document.addEventListener("mouseup", D), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, c, n]), k = [];
  return i.forEach((E, v) => {
    if (k.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${E.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: _(E)
        },
        /* @__PURE__ */ e.createElement(K, { control: E.control })
      )
    ), s && v < i.length - 1) {
      const y = i[v + 1];
      !E.collapsed && !y.collapsed && k.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (x) => g(x, v)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
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
}, dt = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: tt } = e, ul = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, dl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ml = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), pl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), fl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), hl = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), bl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(ul), s = t.title, i = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, o = t.fullLine === !0, m = t.fill === !0, d = t.hoverActions === !0, f = t.appearance === "card", _ = t.errorMessage, g = i === "MINIMIZED", k = i === "MAXIMIZED", E = i === "HIDDEN", v = tt(() => {
    n("toggleMinimize");
  }, [n]), y = tt(() => {
    n("toggleMaximize");
  }, [n]), L = tt(() => {
    n("popOut");
  }, [n]);
  if (E)
    return null;
  const x = k ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, b = c && !k || u && !g || r, w = !!s && s.trim() !== "" || !!t.titleContent || !!t.toolbar || b;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${o ? " tlPanel--fullLine" : ""}${m ? " tlPanel--fill" : ""}${d ? " tlPanel--hoverActions" : ""}${f ? " tlPanel--card" : ""}`,
      style: x
    },
    w && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!s && s.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(K, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), c && !k && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(ml, null) : /* @__PURE__ */ e.createElement(dl, null)
    ), u && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: k ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      k ? /* @__PURE__ */ e.createElement(fl, null) : /* @__PURE__ */ e.createElement(pl, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(hl, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !g && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(dt, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, _)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, gl = ({ controlId: l }) => {
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
}, { useCallback: _e, useState: qe, useEffect: mt, useRef: Ze } = e, vl = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function pt(l, t, n, a) {
  const s = [];
  for (const i of l)
    if (i.type === "nav") {
      if (i.hidden) continue;
      s.push({ id: i.id, type: "nav", groupId: a });
    } else i.type === "command" ? s.push({ id: i.id, type: "command", groupId: a }) : i.type === "group" && (s.push({ id: i.id, type: "group" }), (n.get(i.id) ?? i.expanded) && !t && s.push(...pt(i.children, t, n, i.id)));
  return s;
}
const Be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlSidebar__icon" }) : null, El = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: s, itemRef: i, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: s,
    ref: i,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Cl = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: s, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: s,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(Be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), wl = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), yl = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), kl = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: s, onClose: i }) => {
  const c = Ze(null);
  mt(() => {
    const o = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [i]), Le(!0, { ESCAPE: i });
  const u = _e((o) => {
    o.type === "nav" ? (a(o.id), i()) : o.type === "command" && (s(o.id), i());
  }, [a, s, i]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" && o.hidden) return null;
    if (o.type === "nav" || o.type === "command") {
      const m = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
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
}, Sl = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: s,
  onExecute: i,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: r,
  onFocus: o,
  focusedId: m,
  setItemRef: d,
  onItemFocus: f,
  flyoutGroupId: _,
  onOpenFlyout: g,
  onCloseFlyout: k
}) => {
  const E = Ze(null), [v, y] = qe(null), L = _e(() => {
    a ? _ === l.id ? k() : (E.current && y(E.current.getBoundingClientRect()), g(l.id)) : c(l.id);
  }, [a, _, l.id, c, g, k]), x = _e((w) => {
    E.current = w, r(w);
  }, [r]), b = a && _ === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: L,
      title: a ? l.label : void 0,
      "aria-expanded": a ? b : t,
      tabIndex: u,
      ref: x,
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
  ), b && /* @__PURE__ */ e.createElement(
    kl,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: s,
      onExecute: i,
      onClose: k
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    zt,
    {
      key: w.id,
      item: w,
      activeItemId: n,
      collapsed: a,
      onSelect: s,
      onExecute: i,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: d,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: _,
      onOpenFlyout: g,
      onCloseFlyout: k
    }
  ))));
}, zt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: s,
  onToggleGroup: i,
  focusedId: c,
  setItemRef: u,
  onItemFocus: r,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: d,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        El,
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
        Cl,
        {
          item: l,
          collapsed: n,
          onExecute: s,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(wl, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(yl, null);
    case "group": {
      const _ = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Sl,
        {
          item: l,
          expanded: _,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: s,
          onToggleGroup: i,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: c,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: m,
          onOpenFlyout: d,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, Nl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(vl), s = t.items ?? [], i = t.activeItemId, c = t.collapsed, u = t.drawerOpen, r = u ? !1 : c, [o, m] = qe(() => {
    const I = /* @__PURE__ */ new Map(), O = (Z) => {
      for (const H of Z)
        H.type === "group" && (I.set(H.id, H.expanded), O(H.children));
    };
    return O(s), I;
  }), d = _e((I) => {
    m((O) => {
      const Z = new Map(O), H = Z.get(I) ?? !1;
      return Z.set(I, !H), n("toggleGroup", { itemId: I, expanded: !H }), Z;
    });
  }, [n]), f = _e((I) => {
    I !== i && n("selectItem", { itemId: I });
  }, [n, i]), _ = _e((I) => {
    n("executeCommand", { itemId: I });
  }, [n]), g = _e(() => {
    n("toggleCollapse", {});
  }, [n]), k = _e(() => {
    n("toggleDrawer", {});
  }, [n]), [E, v] = qe(null), y = _e((I) => {
    v(I);
  }, []), L = _e(() => {
    v(null);
  }, []);
  mt(() => {
    r || v(null);
  }, [r]);
  const [x, b] = qe(() => {
    const I = pt(s, r, o);
    return I.length > 0 ? I[0].id : "";
  }), w = Ze(/* @__PURE__ */ new Map()), h = _e((I) => (O) => {
    O ? w.current.set(I, O) : w.current.delete(I);
  }, []), D = _e((I) => {
    b(I);
  }, []), R = Ze(0), N = _e((I) => {
    b(I), R.current++;
  }, []);
  mt(() => {
    const I = w.current.get(x);
    I && document.activeElement !== I && I.focus();
  }, [x, R.current]);
  const z = _e((I) => {
    if (I.key === "Escape" && E !== null) {
      I.preventDefault(), L();
      return;
    }
    const O = pt(s, r, o);
    if (O.length === 0) return;
    const Z = O.findIndex(($) => $.id === x);
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
        I.preventDefault(), H.type === "nav" ? f(H.id) : H.type === "command" ? _(H.id) : H.type === "group" && (r ? E === H.id ? L() : y(H.id) : d(H.id));
        break;
      }
      case "ArrowRight": {
        H.type === "group" && !r && ((o.get(H.id) ?? !1) || (I.preventDefault(), d(H.id)));
        break;
      }
      case "ArrowLeft": {
        H.type === "group" && !r && (o.get(H.id) ?? !1) && (I.preventDefault(), d(H.id));
        break;
      }
    }
  }, [
    s,
    r,
    o,
    x,
    E,
    N,
    f,
    _,
    d,
    y,
    L
  ]), B = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: k, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: z }, s.map((I) => /* @__PURE__ */ e.createElement(
    zt,
    {
      key: I.id,
      item: I,
      activeItemId: i,
      collapsed: r,
      onSelect: f,
      onExecute: _,
      onToggleGroup: d,
      focusedId: x,
      setItemRef: h,
      onItemFocus: D,
      groupStates: o,
      flyoutGroupId: E,
      onOpenFlyout: y,
      onCloseFlyout: L
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Tl = ({ controlId: l }) => {
  const t = G(), n = t.direction ?? "column", a = t.gap ?? "default", s = t.align ?? "stretch", i = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${s}`,
    i ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : "",
    t.cssClass ?? ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((o, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: o })));
}, Rl = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Dl = ({ controlId: l }) => {
  const t = G(), n = t.columns, a = t.minColumnWidth, s = t.gap ?? "default", i = t.children ?? [], c = {};
  return a ? c.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: c }, i.map((u, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: u })));
}, Ll = ({ controlId: l }) => {
  const t = G(), n = t.title, a = t.variant ?? "outlined", s = t.padding ?? "default", i = t.headerActions ?? [], c = t.child, u = n != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((r, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(K, { control: c })));
}, xl = ({ controlId: l }) => {
  const t = G(), n = t.title ?? "", a = t.leading, s = t.trailing, i = t.children ?? [], c = t.actions ?? [], u = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((m, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: m }))), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((m, d) => /* @__PURE__ */ e.createElement(K, { key: d, control: m }))), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(K, { control: s })));
}, { useCallback: Il } = e, Ml = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = Il((i) => {
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
        onClick: () => s(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: Pl } = e, jl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.items ?? [], s = t.activeItemId, i = Pl((c) => {
    c !== s && n("selectItem", { itemId: c });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((c) => {
    const u = c.id === s;
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
}, { useCallback: Tt, useRef: Bl } = e, Al = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Ol = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.closeOnBackdrop !== !1, i = t.child, c = Bl(null), u = Tt(() => {
    n("close");
  }, [n]), r = Tt((o) => {
    s && o.target === o.currentTarget && u();
  }, [s, u]);
  return a ? /* @__PURE__ */ e.createElement(gt, null, /* @__PURE__ */ e.createElement(Al, { onClose: u }), /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: i })
  )) : null;
}, { useEffect: Fl, useRef: $l } = e, Ul = ({ controlId: l }) => {
  const n = G().dialogs ?? [], a = $l(n.length);
  return Fl(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((s) => /* @__PURE__ */ e.createElement(K, { key: s.controlId, control: s })));
}, { useCallback: ze, useRef: Ie, useState: Ve } = e, Hl = ({ onClose: l }) => (me("ESCAPE", () => (l(), !0)), null), Wl = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, zl = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Vl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(Wl), s = t.title ?? "", i = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, o = t.child, m = t.actions ?? [], d = t.toolbar, f = t.buttonBar, [_, g] = Ve(null), [k, E] = Ve(null), [v, y] = Ve(null), L = Ie(null), [x, b] = Ve(!1), w = Ie(null), h = Ie(null), D = Ie(null), R = Ie(null), N = Ie(null), z = ze(() => {
    n("close");
  }, [n]);
  _t(!0, R, "field");
  const B = ze(($, A) => {
    A.preventDefault();
    const P = R.current;
    if (!P) return;
    const q = P.getBoundingClientRect(), p = !L.current, T = L.current ?? { x: q.left, y: q.top };
    p && (L.current = T, y(T)), N.current = {
      dir: $,
      startX: A.clientX,
      startY: A.clientY,
      startW: q.width,
      startH: q.height,
      startPos: { ...T },
      symmetric: p
    };
    const V = (X) => {
      const j = N.current;
      if (!j) return;
      const te = X.clientX - j.startX, ce = X.clientY - j.startY;
      let ne = j.startW, ge = j.startH, ve = 0, Ce = 0;
      j.symmetric ? (j.dir.includes("e") && (ne = j.startW + 2 * te), j.dir.includes("w") && (ne = j.startW - 2 * te), j.dir.includes("s") && (ge = j.startH + 2 * ce), j.dir.includes("n") && (ge = j.startH - 2 * ce)) : (j.dir.includes("e") && (ne = j.startW + te), j.dir.includes("w") && (ne = j.startW - te, ve = te), j.dir.includes("s") && (ge = j.startH + ce), j.dir.includes("n") && (ge = j.startH - ce, Ce = ce));
      const Se = Math.max(200, ne), Ne = Math.max(100, ge);
      j.symmetric ? (ve = (j.startW - Se) / 2, Ce = (j.startH - Ne) / 2) : (j.dir.includes("w") && Se === 200 && (ve = j.startW - 200), j.dir.includes("n") && Ne === 100 && (Ce = j.startH - 100)), h.current = Se, D.current = Ne, g(Se), E(Ne);
      const xe = {
        x: j.startPos.x + ve,
        y: j.startPos.y + Ce
      };
      L.current = xe, y(xe);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
      const X = h.current, j = D.current;
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
    const P = A.getBoundingClientRect(), q = L.current ?? { x: P.left, y: P.top }, p = $.clientX - q.x, T = $.clientY - q.y, V = (X) => {
      const j = window.innerWidth, te = window.innerHeight;
      let ce = X.clientX - p, ne = X.clientY - T;
      const ge = A.offsetWidth, ve = A.offsetHeight;
      ce + ge > j && (ce = j - ge), ne + ve > te && (ne = te - ve), ce < 0 && (ce = 0), ne < 0 && (ne = 0);
      const Ce = { x: ce, y: ne };
      L.current = Ce, y(Ce);
    }, W = () => {
      document.removeEventListener("mousemove", V), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", V), document.addEventListener("mouseup", W);
  }, []), O = ze(() => {
    var $, A;
    if (x) {
      const P = w.current;
      P && (y(P.x !== -1 ? { x: P.x, y: P.y } : null), g(P.w), E(P.h)), b(!1);
    } else {
      const P = R.current, q = P == null ? void 0 : P.getBoundingClientRect();
      w.current = {
        x: (($ = L.current) == null ? void 0 : $.x) ?? (q == null ? void 0 : q.left) ?? -1,
        y: ((A = L.current) == null ? void 0 : A.y) ?? (q == null ? void 0 : q.top) ?? -1,
        w: _ ?? (q == null ? void 0 : q.width) ?? null,
        h: k ?? null
      }, b(!0), y({ x: 0, y: 0 }), g(null), E(null);
    }
  }, [x, _, k]), Z = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : i,
    ...k != null ? { height: k + "px" } : c != null ? { height: c } : {},
    ...u != null && k == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, H = l + "-title";
  return /* @__PURE__ */ e.createElement(gt, { modal: !0 }, /* @__PURE__ */ e.createElement(Hl, { onClose: z }), /* @__PURE__ */ e.createElement(
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
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : I,
        onDoubleClick: r ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: H }, s),
      d && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: d })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
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
    (m.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), m.map(($, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: $ }))),
    r && !x && zl.map(($) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: $,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${$}`,
        onMouseDown: (A) => B($, A)
      }
    ))
  ));
}, { useCallback: Kl } = e, Yl = {
  "js.drawer.close": "Close"
}, Gl = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(Yl), s = t.open === !0, i = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, r = t.child, o = Kl(() => {
    n("close");
  }, [n]);
  Le(s, { ESCAPE: o });
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${c}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !s }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
}, { useCallback: Ke, useRef: Xl } = e, ql = ({ controlId: l }) => {
  const t = G(), n = ae(), a = Xl(null), s = t.child, c = (t.trigger ?? "contextmenu") === "click", u = Ke((d) => {
    d.preventDefault(), d.stopPropagation(), n("openMenu", { x: d.clientX, y: d.clientY });
  }, [n]), r = Ke(() => {
    var f;
    const d = (f = a.current) == null ? void 0 : f.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(d ? d.left : 0),
      y: Math.round(d ? d.bottom : 0)
    });
  }, [n]), o = Ke((d) => {
    d.preventDefault(), d.stopPropagation(), r();
  }, [r]), m = Ke((d) => {
    (d.key === "Enter" || d.key === " ") && (d.preventDefault(), r());
  }, [r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenuRegion" + (c ? " tlMenuRegion--click" : ""),
      ref: a,
      onContextMenu: c ? void 0 : u,
      onClick: c ? o : void 0,
      role: c ? "button" : void 0,
      tabIndex: c ? 0 : void 0,
      "aria-haspopup": c ? "menu" : void 0,
      onKeyDown: c ? m : void 0
    },
    s && /* @__PURE__ */ e.createElement(K, { control: s })
  );
}, { useCallback: Zl, useEffect: Rt, useRef: Ql, useState: Dt } = e, Jl = 250, ea = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.message ?? "", s = t.content ?? "", i = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [o, m] = Dt(!1), [d, f] = Dt(!1), _ = Ql(!1);
  Rt(() => {
    _.current = !1;
  }, [r]);
  const g = Zl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
  return Rt(() => {
    if (!u || c === 0 || d) return;
    const k = setTimeout(g, _.current ? Jl : c);
    return () => clearTimeout(k);
  }, [u, c, d, g]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite",
      onMouseEnter: () => {
        _.current = !0, f(!0);
      },
      onMouseLeave: () => f(!1)
    },
    s ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: s } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: ta, useEffect: Lt, useMemo: na, useRef: la, useState: aa } = e, ra = 1e3;
function oa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, s = Math.floor(t / 3600), i = (c) => c < 10 ? `0${c}` : `${c}`;
  return s > 0 ? `${s}:${i(a)}:${i(n)}` : `${a}:${i(n)}`;
}
const sa = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.visible === !0, s = t.severity ?? "info", i = t.text ?? "", c = t.deadline ?? null, u = t.serverNow ?? null, r = t.leadMs ?? null, o = t.actionLabel ?? null, m = t.pingGraceMs ?? null, d = na(
    () => u != null ? u - Date.now() : 0,
    [u]
  ), [f, _] = aa(0), g = a && c != null;
  Lt(() => {
    if (!g) return;
    const x = setInterval(() => _((b) => b + 1), ra);
    return () => clearInterval(x);
  }, [g, c]);
  const k = la(null);
  Lt(() => {
    !g || m == null || c == null || k.current !== c && (Date.now() + d < c + m || (k.current = c, n("deadlinePassed", {})));
  }, [f, g, c, m, d, n]);
  const E = ta(() => {
    o != null && n("action", {});
  }, [n, o]);
  if (!a) return null;
  const v = c != null ? c - (Date.now() + d) : null;
  if (r != null && v != null && v > r) return null;
  const y = v != null ? oa(v) : null, L = o != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${s}${L ? " tlNoticeBar--clickable" : ""}`,
      role: L ? "button" : "status",
      "aria-live": "polite",
      tabIndex: L ? 0 : void 0,
      title: o ?? void 0,
      "aria-label": L ? `${i} ${o}` : void 0,
      onClick: L ? E : void 0,
      onKeyDown: L ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), E());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, i),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: nt, useEffect: xt, useRef: ca, useState: It } = e, ia = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.open === !0, s = t.anchorId, i = t.anchorX, c = t.anchorY, u = t.items ?? [], r = ca(null), [o, m] = It({ top: 0, left: 0 }), [d, f] = It(0), _ = u.filter((v) => v.type === "item" && !v.disabled);
  xt(() => {
    var h, D;
    if (!a) return;
    const v = ((h = r.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (i != null && c != null) {
      let R = c, N = i;
      R + v > window.innerHeight && (R = Math.max(0, window.innerHeight - v)), N + y > window.innerWidth && (N = Math.max(0, window.innerWidth - y)), m({ top: R, left: N }), f(0);
      return;
    }
    if (!s) return;
    const L = document.getElementById(s);
    if (!L) return;
    const x = L.getBoundingClientRect();
    let b = x.bottom + 4, w = x.left;
    b + v > window.innerHeight && (b = x.top - v - 4), w + y > window.innerWidth && (w = x.right - y), m({ top: b, left: w }), f(0);
  }, [a, s, i, c]);
  const g = nt(() => {
    n("close");
  }, [n]), k = nt((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  xt(() => {
    if (!a) return;
    const v = (y) => {
      r.current && !r.current.contains(y.target) && g();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [a, g]);
  const E = nt((v) => {
    if (v.key === "Escape") {
      v.preventDefault(), g();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((y) => (y + 1) % _.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((y) => (y - 1 + _.length) % _.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const y = _[d];
      y && k(y.id);
    }
  }, [g, k, _, d]);
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
      const x = _.indexOf(v) === d;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : "") + (v.cssClasses ? " " + v.cssClasses : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => k(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement(ye, { encoded: v.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, ua = 768, da = ({ controlId: l }) => {
  const t = G(), n = ae();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${ua}px)`), o = (d) => {
      n("reportDisplayClass", { displayClass: d ? "COMPACT" : "REGULAR" });
    };
    o(r.matches);
    const m = (d) => o(d.matches);
    return r.addEventListener("change", m), () => r.removeEventListener("change", m);
  }, [n]);
  const a = t.header, s = t.notices, i = t.content, c = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: i })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: c })), /* @__PURE__ */ e.createElement(K, { control: u }));
}, ma = ({ controlId: l }) => {
  const t = G(), n = t.text ?? "", a = t.cssClass ?? "", s = t.hasTooltip === !0, i = t.role || void 0, c = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      role: i,
      "data-tooltip": s ? "key:tooltip" : void 0
    },
    n
  );
}, pa = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: s }) => (me("ArrowUp", () => (n("up", !1, !1), !0)), me("ArrowDown", () => (n("down", !1, !1), !0)), me("Home", () => (n("home", !1, !1), !0)), me("End", () => (n("end", !1, !1), !0)), me("PageUp", () => (n("pageUp", !1, !1), !0)), me("PageDown", () => (n("pageDown", !1, !1), !0)), me("Shift+ArrowUp", () => (n("up", l, !1), !0)), me("Shift+ArrowDown", () => (n("down", l, !1), !0)), me("Shift+Home", () => (n("home", l, !1), !0)), me("Shift+End", () => (n("end", l, !1), !0)), me("Shift+PageUp", () => (n("pageUp", l, !1), !0)), me("Shift+PageDown", () => (n("pageDown", l, !1), !0)), me("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), me("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), me("Space", () => t < 0 ? !1 : (a(), !0)), me("Ctrl+A", () => l ? (s(), !0) : !1), null), fa = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.freezeSplitter": "Drag to choose the columns that stay in place while scrolling",
  "js.table.filter": "Filter",
  "js.table.columns": "Columns"
}, Mt = 50, ha = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, ha));
}
const ft = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', ba = ft + ", button:not([disabled]), a[href]";
function Vt(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function at(l, t, n = {}) {
  const a = Vt(l, t);
  if (n.col) {
    const i = a.find((u) => u.dataset.col === n.col), c = i == null ? void 0 : i.querySelector(ft);
    if (c) return c;
  }
  if (n.col)
    return null;
  const s = n.last ? [...a].reverse() : a;
  for (const i of s) {
    const c = i.querySelector(ft);
    if (c) return c;
  }
  return null;
}
const ga = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(fa), s = e.useRef(null);
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
  const i = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, d = t.cursorIndex ?? -1, f = t.frozenColumnCount ?? 0, _ = t.treeMode ?? !1, g = t.columnSelect ?? !1, k = e.useMemo(
    () => i.filter((C) => C.sortPriority && C.sortPriority > 0).length,
    [i]
  ), E = o === "multi", v = 40, y = 20, L = e.useRef(null), x = e.useRef(null), b = e.useRef(null), w = e.useRef(null), h = e.useRef(null), [D, R] = e.useState({}), N = e.useRef(null), z = e.useRef(!1), B = e.useRef(null), [I, O] = e.useState(null), [Z, H] = e.useState(null), [$, A] = e.useState(null), [P, q] = e.useState(0);
  e.useEffect(() => {
    const C = b.current;
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
  }, [i]);
  const p = e.useCallback((C) => D[C.name] ?? C.width, [D]), T = e.useMemo(() => {
    const C = [];
    let S = E && f > 0 ? v : 0;
    for (let U = 0; U < f && U < i.length; U++)
      C.push(S), S += p(i[U]);
    return C;
  }, [i, f, E, v, p]), V = e.useMemo(() => {
    if (f <= 0)
      return 0;
    let C = E ? v : 0;
    for (let S = 0; S < f && S < i.length; S++)
      C += p(i[S]);
    return C;
  }, [i, f, E, v, p]), W = c * r, X = e.useRef(null), j = e.useCallback((C, S, U) => {
    U.preventDefault(), U.stopPropagation(), N.current = { column: C, startX: U.clientX, startWidth: S };
    let Q = U.clientX, ee = 0;
    const re = () => {
      const se = N.current;
      if (!se) return;
      const de = Math.max(Mt, se.startWidth + (Q - se.startX) + ee);
      R((Ee) => ({ ...Ee, [se.column]: de }));
    }, oe = () => {
      const se = b.current, de = L.current;
      if (!se || !N.current) return;
      const Ee = se.getBoundingClientRect(), Te = 40, Ct = 8, on = se.scrollLeft;
      Q > Ee.right - Te ? se.scrollLeft += Ct : Q < Ee.left + Te && (se.scrollLeft = Math.max(0, se.scrollLeft - Ct));
      const wt = se.scrollLeft - on;
      wt !== 0 && (de && (de.scrollLeft = se.scrollLeft), ee += wt, re()), X.current = requestAnimationFrame(oe);
    };
    X.current = requestAnimationFrame(oe);
    const fe = (se) => {
      Q = se.clientX, re();
    }, pe = (se) => {
      document.removeEventListener("mousemove", fe), document.removeEventListener("mouseup", pe), X.current !== null && (cancelAnimationFrame(X.current), X.current = null);
      const de = N.current;
      if (de) {
        const Ee = Math.max(Mt, de.startWidth + (se.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: Ee }), N.current = null, z.current = !0, requestAnimationFrame(() => {
          z.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", fe), document.addEventListener("mouseup", pe);
  }, [n]), te = e.useCallback(() => {
    L.current && b.current && (L.current.scrollLeft = b.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const C = b.current;
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
  }, []), ge = e.useCallback((C, S) => {
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
    let U = i.findIndex((ee) => ee.name === I.column);
    if (U < 0) {
      B.current = null, O(null);
      return;
    }
    const Q = i.findIndex((ee) => ee.name === S);
    I.side === "right" && U++, Q < U && U--, n("columnReorder", { column: S, targetIndex: U }), B.current = null, O(null);
  }, [i, I, n]), Ce = e.useCallback(() => {
    B.current = null, O(null);
  }, []), Se = e.useCallback((C, S) => {
    var ee, re, oe, fe;
    const U = window.getSelection();
    if (U && !U.isCollapsed && S.currentTarget.contains(U.anchorNode))
      return;
    if (!lt(S) && ((ee = b.current) == null || ee.focus({ preventScroll: !0 }), !S.ctrlKey && !S.metaKey && !S.shiftKey)) {
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
    d < 0 || n("select", { rowIndex: d, ctrlKey: E, shiftKey: !1 });
  }, [n, d, E]), He = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), M = e.useCallback(
    () => !!s.current && s.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (d < 0)
      return;
    const C = b.current;
    if (!C)
      return;
    const S = d * r, U = S + r;
    S < C.scrollTop ? C.scrollTop = S : U > C.scrollTop + C.clientHeight && (C.scrollTop = U - C.clientHeight);
  }, [d, r]), e.useEffect(() => {
    const C = h.current, S = b.current;
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
    const S = b.current, U = document.activeElement;
    if (!S || !U || !S.contains(U))
      return;
    const Q = U.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = u.find((Te) => Te.id === ee);
    if (!re)
      return;
    const oe = Vt(S, ee).flatMap((Te) => Array.from(Te.querySelectorAll(ba))), fe = oe.indexOf(U);
    if (fe < 0)
      return;
    const pe = !C.shiftKey;
    if (!(pe ? fe === oe.length - 1 : fe === 0))
      return;
    const de = pe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= c)
      return;
    const Ee = u.find((Te) => Te.index === de);
    Ee && at(S, Ee.id) || (C.preventDefault(), h.current = { index: de, last: !pe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [u, c, n]), le = e.useCallback((C, S) => {
    S.stopPropagation(), n("select", { rowIndex: C, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ie = e.useCallback(() => {
    const C = m === c && c > 0;
    n("selectAll", { selected: !C });
  }, [n, m, c]), Fe = e.useCallback((C, S, U) => {
    U.stopPropagation(), n("expand", { rowIndex: C, expanded: S });
  }, [n]), Qt = e.useCallback((C, S) => {
    S.preventDefault(), H({ x: S.clientX, y: S.clientY, colIdx: C });
  }, []), Jt = e.useCallback(() => {
    Z && (n("setFrozenColumnCount", { count: Z.colIdx + 1 }), H(null));
  }, [Z, n]), en = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), H(null);
  }, [n]), tn = e.useCallback((C) => {
    C.preventDefault(), C.stopPropagation();
    const S = x.current, U = L.current;
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
  const nn = e.useCallback((C, S) => {
    S.stopPropagation(), S.preventDefault(), n("openFilter", { column: C });
  }, [n]), ln = e.useCallback((C) => {
    C.stopPropagation(), C.preventDefault(), n("openColumnSelect", {});
  }, [n]), Qe = i.reduce((C, S) => C + p(S), 0) + (E ? v : 0), Je = g ? 32 : 0, an = m === c && c > 0, Et = m > 0 && m < c, rn = e.useCallback((C) => {
    C && (C.indeterminate = Et);
  }, [Et]);
  return /* @__PURE__ */ e.createElement(gt, { active: M }, /* @__PURE__ */ e.createElement(
    pa,
    {
      isMulti: E,
      cursorIndex: d,
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
        const S = b.current, U = L.current;
        if (!S) return;
        const Q = S.getBoundingClientRect(), ee = 40, re = 8;
        C.clientX < Q.left + ee ? S.scrollLeft = Math.max(0, S.scrollLeft - re) : C.clientX > Q.right - ee && (S.scrollLeft += re), U && (U.scrollLeft = S.scrollLeft);
      },
      onDrop: ve
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: x }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: L }, /* @__PURE__ */ e.createElement(
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
            B.current && (C.preventDefault(), C.dataTransfer.dropEffect = "move", i.length > 0 && i[0].name !== B.current && O({ column: i[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: rn,
            className: "tlTableView__checkbox",
            checked: an,
            onChange: ie
          }
        )
      ),
      i.map((C, S) => {
        const U = p(C);
        i.length - 1;
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
            onContextMenu: (oe) => Qt(S, oe),
            onDragStart: (oe) => ne(C.name, oe),
            onDragOver: (oe) => ge(C.name, oe),
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
              onClick: (oe) => nn(C.name, oe)
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
            if (B.current && i.length > 0) {
              const S = i[i.length - 1];
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
        onMouseDown: tn
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: ln
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
      /* @__PURE__ */ e.createElement("div", { style: { height: W, position: "relative", width: Qe, paddingRight: Je } }, u.map((C) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C.id,
          className: "tlTableView__row" + (C.selected ? " tlTableView__row--selected" : "") + (C.index === d ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: C.index * r,
            height: r,
            width: Qe,
            paddingRight: Je,
            ...C.index === d ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
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
        i.map((S, U) => {
          const Q = p(S), ee = U === i.length - 1, re = U < f, oe = U === f - 1;
          let fe = "tlTableView__cell";
          re && (fe += " tlTableView__cell--frozen"), oe && (fe += " tlTableView__cell--frozenLast");
          const pe = _ && U === 0, se = C.treeDepth ?? 0;
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
      Z.colIdx + 1 !== f && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Jt }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      f > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: en }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, _a = {
  "js.table.columnSearch": "Find column"
}, va = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(_a), s = t.entries ?? [], i = s.filter((b) => b.visible).length, [c, u] = e.useState(""), r = c.trim().toLowerCase(), o = r ? s.filter((b) => b.label.toLowerCase().includes(r)) : s, m = e.useRef(null), d = e.useRef(null), [f, _] = e.useState(null), g = e.useCallback((b) => {
    d.current = b, _(b);
  }, []), k = e.useCallback((b, w) => {
    n("columnVisible", { column: b, visible: w });
  }, [n]), E = e.useCallback((b, w) => {
    m.current = b, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, []), v = e.useCallback((b, w) => {
    if (!m.current || m.current === b) {
      g(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const h = w.currentTarget.getBoundingClientRect(), D = w.clientY < h.top + h.height / 2 ? "top" : "bottom";
    g({ name: b, side: D });
  }, [g]), y = e.useCallback(() => {
    m.current = null, g(null);
  }, [g]), L = e.useCallback((b) => {
    b.preventDefault();
    const w = m.current, h = d.current;
    if (m.current = null, g(null), !w || !h)
      return;
    const D = s.findIndex((z) => z.name === h.name), R = s.findIndex((z) => z.name === w);
    if (D < 0 || R < 0)
      return;
    let N = h.side === "top" ? D : D + 1;
    R < N && N--, N !== R && n("columnReorder", { column: w, targetIndex: N });
  }, [s, n, g]), x = s.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: L }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: c,
      onChange: (b) => u(b.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, o.map((b) => {
    const w = b.visible && i <= 1;
    let h = "tlColumnSelect__row";
    return f && f.name === b.name && (h += " tlColumnSelect__row--dragOver-" + f.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: b.name,
        className: h,
        draggable: !0,
        onDragStart: (D) => E(b.name, D),
        onDragOver: (D) => v(b.name, D),
        onDrop: L,
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
          onChange: (D) => k(b.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, b.label))
    );
  })));
}, Ea = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Kt = e.createContext(Ea), { useMemo: Ca, useRef: wa, useState: ya, useEffect: ka } = e, Sa = 320, Na = "TLTableView", Ta = "TLPanel", Ra = ({ controlId: l }) => {
  var E;
  const t = G(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", s = t.readOnly === !0, i = t.children ?? [], c = t.noModelMessage, u = wa(null), [r, o] = ya(
    a === "top" ? "top" : "side"
  );
  ka(() => {
    if (a !== "auto") {
      o(a);
      return;
    }
    const v = u.current;
    if (!v) return;
    const y = new ResizeObserver((L) => {
      for (const x of L) {
        const w = x.contentRect.width / n;
        o(w < Sa ? "top" : "side");
      }
    });
    return y.observe(v), () => y.disconnect();
  }, [a, n]);
  const m = Ca(() => ({
    readOnly: s,
    resolvedLabelPosition: r
  }), [s, r]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, _ = i.length === 1 ? i[0] : void 0, g = !!_ && (_.module === Na || _.module === Ta && ((E = _.state) == null ? void 0 : E.bare) === !0), k = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Kt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: f, ref: u }, i.map((v, y) => /* @__PURE__ */ e.createElement(K, { key: y, control: v }))));
}, { useCallback: Da } = e, La = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, xa = ({ controlId: l }) => {
  const t = G(), n = ae(), a = ue(La), s = t.headerControl ?? null, i = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], d = s != null || i.length > 0 || c, f = Da(() => {
    n("toggleCollapse");
  }, [n]), _ = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: _ }, d && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: s })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((g, k) => /* @__PURE__ */ e.createElement(K, { key: k, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((g, k) => /* @__PURE__ */ e.createElement(K, { key: k, control: g }))));
}, { useContext: Ia, useState: Ma, useCallback: Pa } = e, ja = ({ controlId: l }) => {
  const t = G(), n = Ia(Kt), a = t.label ?? "", s = t.required === !0, i = t.error, c = t.errorIcon, u = t.warnings, r = t.warningIcon, o = t.helpText, m = t.dirty === !0, d = t.labelPosition ?? n.resolvedLabelPosition, f = t.fullLine === !0, _ = t.visible !== !1, g = t.hasTooltip === !0, k = t.field, E = n.readOnly, [v, y] = Ma(!1), L = Pa(() => y((D) => !D), []), x = d === "hidden", b = i != null, w = u != null && u.length > 0, h = [
    "tlFormField",
    `tlFormField--${d}`,
    E ? "tlFormField--readonly" : "",
    f ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    !b && w ? "tlFormField--warning" : "",
    m ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: _ ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), s && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), m && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: k })), !E && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(dt, { image: c, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, i)), !E && !b && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, u.map((D, R) => /* @__PURE__ */ e.createElement("div", { key: R, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(dt, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !E && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Ba = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.iconCss, s = t.iconSrc, i = t.label, c = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, o = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : s ? /* @__PURE__ */ e.createElement("img", { src: s, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, o, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), d = e.useCallback((g) => {
    g.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", c].filter(Boolean).join(" "), _ = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: d,
      "data-tooltip": _
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": _ }, m);
}, Aa = 20, Oa = () => {
  var w;
  const l = G(), t = ae(), n = l.nodes ?? [], a = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, o] = e.useState(-1), m = e.useRef(null), d = ((w = n.find((h) => h.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var D;
    if (d == null)
      return;
    const h = (D = m.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    h && h.scrollIntoView({ block: "nearest" });
  }, [d]);
  const f = e.useCallback((h, D) => {
    t(D ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, D) => {
    var N;
    const R = window.getSelection();
    R && !R.isCollapsed && D.currentTarget.contains(R.anchorNode) || ((N = m.current) == null || N.focus({ preventScroll: !0 }), t("select", {
      nodeId: h,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), g = e.useCallback((h, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: h, x: D.clientX, y: D.clientY });
  }, [t]), k = e.useRef(null), E = e.useCallback((h, D) => {
    const R = D.getBoundingClientRect(), N = h.clientY - R.top, z = R.height / 3;
    return N < z ? "above" : N > z * 2 ? "below" : "within";
  }, []), v = e.useCallback((h, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", h);
  }, []), y = e.useCallback((h, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const R = E(D, D.currentTarget);
    k.current != null && window.clearTimeout(k.current), k.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: R }), k.current = null;
    }, 50);
  }, [t, E]), L = e.useCallback((h, D) => {
    D.preventDefault(), k.current != null && (window.clearTimeout(k.current), k.current = null);
    const R = E(D, D.currentTarget);
    t("drop", { nodeId: h, position: R });
  }, [t, E]), x = e.useCallback(() => {
    k.current != null && (window.clearTimeout(k.current), k.current = null), t("dragEnd");
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
      ref: m,
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
          c === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * Aa },
        draggable: s,
        onMouseDown: (R) => {
          (R.shiftKey || R.ctrlKey || R.metaKey || R.detail > 1) && R.preventDefault();
        },
        onClick: (R) => _(h.id, R),
        onContextMenu: (R) => g(h.id, R),
        onDragStart: (R) => v(h.id, R),
        onDragOver: i ? (R) => y(h.id, R) : void 0,
        onDrop: i ? (R) => L(h.id, R) : void 0,
        onDragEnd: x
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
var Pt;
function Fa() {
  if (Pt) return J;
  Pt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), s = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), d = Symbol.for("react.activity"), f = Symbol.iterator;
  function _(p) {
    return p === null || typeof p != "object" ? null : (p = f && p[f] || p["@@iterator"], typeof p == "function" ? p : null);
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
  }, k = Object.assign, E = {};
  function v(p, T, V) {
    this.props = p, this.context = T, this.refs = E, this.updater = V || g;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(p, T) {
    if (typeof p != "object" && typeof p != "function" && p != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, p, T, "setState");
  }, v.prototype.forceUpdate = function(p) {
    this.updater.enqueueForceUpdate(this, p, "forceUpdate");
  };
  function y() {
  }
  y.prototype = v.prototype;
  function L(p, T, V) {
    this.props = p, this.context = T, this.refs = E, this.updater = V || g;
  }
  var x = L.prototype = new y();
  x.constructor = L, k(x, v.prototype), x.isPureReactComponent = !0;
  var b = Array.isArray;
  function w() {
  }
  var h = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function R(p, T, V) {
    var W = V.ref;
    return {
      $$typeof: l,
      type: p,
      key: T,
      ref: W !== void 0 ? W : null,
      props: V
    };
  }
  function N(p, T) {
    return R(p.type, T, p.props);
  }
  function z(p) {
    return typeof p == "object" && p !== null && p.$$typeof === l;
  }
  function B(p) {
    var T = { "=": "=0", ":": "=2" };
    return "$" + p.replace(/[=:]/g, function(V) {
      return T[V];
    });
  }
  var I = /\/+/g;
  function O(p, T) {
    return typeof p == "object" && p !== null && p.key != null ? B("" + p.key) : T.toString(36);
  }
  function Z(p) {
    switch (p.status) {
      case "fulfilled":
        return p.value;
      case "rejected":
        throw p.reason;
      default:
        switch (typeof p.status == "string" ? p.then(w, w) : (p.status = "pending", p.then(
          function(T) {
            p.status === "pending" && (p.status = "fulfilled", p.value = T);
          },
          function(T) {
            p.status === "pending" && (p.status = "rejected", p.reason = T);
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
  function H(p, T, V, W, X) {
    var j = typeof p;
    (j === "undefined" || j === "boolean") && (p = null);
    var te = !1;
    if (p === null) te = !0;
    else
      switch (j) {
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
            case m:
              return te = p._init, H(
                te(p._payload),
                T,
                V,
                W,
                X
              );
          }
      }
    if (te)
      return X = X(p), te = W === "" ? "." + O(p, 0) : W, b(X) ? (V = "", te != null && (V = te.replace(I, "$&/") + "/"), H(X, T, V, "", function(ge) {
        return ge;
      })) : X != null && (z(X) && (X = N(
        X,
        V + (X.key == null || p && p.key === X.key ? "" : ("" + X.key).replace(
          I,
          "$&/"
        ) + "/") + te
      )), T.push(X)), 1;
    te = 0;
    var ce = W === "" ? "." : W + ":";
    if (b(p))
      for (var ne = 0; ne < p.length; ne++)
        W = p[ne], j = ce + O(W, ne), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (ne = _(p), typeof ne == "function")
      for (p = ne.call(p), ne = 0; !(W = p.next()).done; )
        W = W.value, j = ce + O(W, ne++), te += H(
          W,
          T,
          V,
          j,
          X
        );
    else if (j === "object") {
      if (typeof p.then == "function")
        return H(
          Z(p),
          T,
          V,
          W,
          X
        );
      throw T = String(p), Error(
        "Objects are not valid as a React child (found: " + (T === "[object Object]" ? "object with keys {" + Object.keys(p).join(", ") + "}" : T) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function $(p, T, V) {
    if (p == null) return p;
    var W = [], X = 0;
    return H(p, W, "", "", function(j) {
      return T.call(V, j, X++);
    }), W;
  }
  function A(p) {
    if (p._status === -1) {
      var T = p._result;
      T = T(), T.then(
        function(V) {
          (p._status === 0 || p._status === -1) && (p._status = 1, p._result = V);
        },
        function(V) {
          (p._status === 0 || p._status === -1) && (p._status = 2, p._result = V);
        }
      ), p._status === -1 && (p._status = 0, p._result = T);
    }
    if (p._status === 1) return p._result.default;
    throw p._result;
  }
  var P = typeof reportError == "function" ? reportError : function(p) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var T = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof p == "object" && p !== null && typeof p.message == "string" ? String(p.message) : String(p),
        error: p
      });
      if (!window.dispatchEvent(T)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", p);
      return;
    }
    console.error(p);
  }, q = {
    map: $,
    forEach: function(p, T, V) {
      $(
        p,
        function() {
          T.apply(this, arguments);
        },
        V
      );
    },
    count: function(p) {
      var T = 0;
      return $(p, function() {
        T++;
      }), T;
    },
    toArray: function(p) {
      return $(p, function(T) {
        return T;
      }) || [];
    },
    only: function(p) {
      if (!z(p))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return p;
    }
  };
  return J.Activity = d, J.Children = q, J.Component = v, J.Fragment = n, J.Profiler = s, J.PureComponent = L, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = h, J.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(p) {
      return h.H.useMemoCache(p);
    }
  }, J.cache = function(p) {
    return function() {
      return p.apply(null, arguments);
    };
  }, J.cacheSignal = function() {
    return null;
  }, J.cloneElement = function(p, T, V) {
    if (p == null)
      throw Error(
        "The argument must be a React element, but you passed " + p + "."
      );
    var W = k({}, p.props), X = p.key;
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
    return R(p.type, X, W);
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
  }, J.createElement = function(p, T, V) {
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
    if (p && p.defaultProps)
      for (W in te = p.defaultProps, te)
        X[W] === void 0 && (X[W] = te[W]);
    return R(p, j, X);
  }, J.createRef = function() {
    return { current: null };
  }, J.forwardRef = function(p) {
    return { $$typeof: u, render: p };
  }, J.isValidElement = z, J.lazy = function(p) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: p },
      _init: A
    };
  }, J.memo = function(p, T) {
    return {
      $$typeof: o,
      type: p,
      compare: T === void 0 ? null : T
    };
  }, J.startTransition = function(p) {
    var T = h.T, V = {};
    h.T = V;
    try {
      var W = p(), X = h.S;
      X !== null && X(V, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(w, P);
    } catch (j) {
      P(j);
    } finally {
      T !== null && V.types !== null && (T.types = V.types), h.T = T;
    }
  }, J.unstable_useCacheRefresh = function() {
    return h.H.useCacheRefresh();
  }, J.use = function(p) {
    return h.H.use(p);
  }, J.useActionState = function(p, T, V) {
    return h.H.useActionState(p, T, V);
  }, J.useCallback = function(p, T) {
    return h.H.useCallback(p, T);
  }, J.useContext = function(p) {
    return h.H.useContext(p);
  }, J.useDebugValue = function() {
  }, J.useDeferredValue = function(p, T) {
    return h.H.useDeferredValue(p, T);
  }, J.useEffect = function(p, T) {
    return h.H.useEffect(p, T);
  }, J.useEffectEvent = function(p) {
    return h.H.useEffectEvent(p);
  }, J.useId = function() {
    return h.H.useId();
  }, J.useImperativeHandle = function(p, T, V) {
    return h.H.useImperativeHandle(p, T, V);
  }, J.useInsertionEffect = function(p, T) {
    return h.H.useInsertionEffect(p, T);
  }, J.useLayoutEffect = function(p, T) {
    return h.H.useLayoutEffect(p, T);
  }, J.useMemo = function(p, T) {
    return h.H.useMemo(p, T);
  }, J.useOptimistic = function(p, T) {
    return h.H.useOptimistic(p, T);
  }, J.useReducer = function(p, T, V) {
    return h.H.useReducer(p, T, V);
  }, J.useRef = function(p) {
    return h.H.useRef(p);
  }, J.useState = function(p) {
    return h.H.useState(p);
  }, J.useSyncExternalStore = function(p, T, V) {
    return h.H.useSyncExternalStore(
      p,
      T,
      V
    );
  }, J.useTransition = function() {
    return h.H.useTransition();
  }, J.version = "19.2.4", J;
}
var jt;
function $a() {
  return jt || (jt = 1, ot.exports = Fa()), ot.exports;
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
var Bt;
function Ua() {
  if (Bt) return he;
  Bt = 1;
  var l = $a();
  function t(r) {
    var o = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        o += "&args[]=" + encodeURIComponent(arguments[m]);
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
  function i(r, o, m) {
    var d = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: s,
      key: d == null ? null : "" + d,
      children: r,
      containerInfo: o,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, o) {
    if (r === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return he.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, he.createPortal = function(r, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return i(r, o, null, m);
  }, he.flushSync = function(r) {
    var o = c.T, m = a.p;
    try {
      if (c.T = null, a.p = 2, r) return r();
    } finally {
      c.T = o, a.p = m, a.d.f();
    }
  }, he.preconnect = function(r, o) {
    typeof r == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, a.d.C(r, o));
  }, he.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, he.preinit = function(r, o) {
    if (typeof r == "string" && o && typeof o.as == "string") {
      var m = o.as, d = u(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, _ = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? a.d.S(
        r,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: d,
          integrity: f,
          fetchPriority: _
        }
      ) : m === "script" && a.d.X(r, {
        crossOrigin: d,
        integrity: f,
        fetchPriority: _,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, he.preinitModule = function(r, o) {
    if (typeof r == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var m = u(
            o.as,
            o.crossOrigin
          );
          a.d.M(r, {
            crossOrigin: m,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && a.d.M(r);
  }, he.preload = function(r, o) {
    if (typeof r == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var m = o.as, d = u(m, o.crossOrigin);
      a.d.L(r, m, {
        crossOrigin: d,
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
        var m = u(o.as, o.crossOrigin);
        a.d.m(r, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else a.d.m(r);
  }, he.requestFormReset = function(r) {
    a.d.r(r);
  }, he.unstable_batchedUpdates = function(r, o) {
    return r(o);
  }, he.useFormState = function(r, o, m) {
    return c.H.useFormState(r, o, m);
  }, he.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, he.version = "19.2.4", he;
}
var At;
function Ha() {
  if (At) return rt.exports;
  At = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), rt.exports = Ua(), rt.exports;
}
var Yt = Ha();
const { useState: Re, useCallback: be, useRef: $e, useEffect: Me, useMemo: ht } = e;
function vt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(ye, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function Wa({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: s,
  onDragStart: i,
  onDragOver: c,
  onDrop: u,
  onDragEnd: r,
  dragClassName: o
}) {
  const m = be(
    (d) => {
      d.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: s || void 0,
      onDragStart: i,
      onDragOver: c,
      onDrop: u,
      onDragEnd: r
    },
    s && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(vt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": a
      },
      "×"
    )
  );
}
function za({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: s,
  id: i
}) {
  const c = be(() => a(l.value), [a, l.value]), u = ht(() => {
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
      onMouseEnter: s
    },
    /* @__PURE__ */ e.createElement(vt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Va = ({ controlId: l, state: t }) => {
  const n = ae(), a = t.value ?? [], s = t.multiSelect === !0, i = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], d = t.emptyOptionLabel ?? "", f = i && s && !u && r, _ = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = _["js.dropdownSelect.nothingFound"], k = be(
    (M) => _["js.dropdownSelect.removeChip"].replace("{0}", M),
    [_]
  ), [E, v] = Re(!1), [y, L] = Re(""), [x, b] = Re(-1), [w, h] = Re(!1), [D, R] = Re({}), [N, z] = Re(null), [B, I] = Re(null), [O, Z] = Re(null), H = $e(null), $ = $e(null), A = $e(null), P = $e(a);
  P.current = a;
  const q = $e(-1), p = ht(
    () => new Set(a.map((M) => M.value)),
    [a]
  ), T = ht(() => {
    let M = m.filter((Y) => !p.has(Y.value));
    if (y) {
      const Y = y.toLowerCase();
      M = M.filter((le) => le.label.toLowerCase().includes(Y));
    }
    return M;
  }, [m, p, y]);
  Me(() => {
    y && T.length === 1 ? b(0) : b(-1);
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
    if (!(u || !r) && (v(!0), L(""), b(-1), h(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        h(!0);
      }
  }, [u, r, o, n]), W = be(() => {
    var M;
    v(!1), L(""), b(-1), (M = H.current) == null || M.focus();
  }, []), X = be(
    (M) => {
      let Y;
      if (s) {
        const le = m.find((ie) => ie.value === M);
        if (le)
          Y = [...P.current, le];
        else
          return;
      } else {
        const le = m.find((ie) => ie.value === M);
        if (le)
          Y = [le];
        else
          return;
      }
      P.current = Y, n(We, { value: Y.map((le) => le.value) }), s ? (L(""), b(-1)) : W();
    },
    [s, m, n, W]
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
          M.preventDefault(), M.stopPropagation(), b(
            (Y) => Y < T.length - 1 ? Y + 1 : 0
          );
          break;
        case "ArrowUp":
          M.preventDefault(), M.stopPropagation(), b(
            (Y) => Y > 0 ? Y - 1 : T.length - 1
          );
          break;
        case "Enter":
          M.preventDefault(), M.stopPropagation(), x >= 0 && x < T.length && X(T[x].value);
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
      x,
      X,
      y,
      s,
      a,
      j
    ]
  ), ge = be(
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
    if (x < 0 || !A.current) return;
    const M = A.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    M && M.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((M) => /* @__PURE__ */ e.createElement("span", { key: M.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(vt, { image: M.image }), /* @__PURE__ */ e.createElement("span", null, M.label))));
  const xe = !c && a.length > 0 && !u, He = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...cn
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
        placeholder: _["js.dropdownSelect.filterPlaceholder"],
        "aria-label": _["js.dropdownSelect.filterPlaceholder"],
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
      !o && !w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ge }, _["js.dropdownSelect.error"])),
      o && T.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      o && T.map((M, Y) => /* @__PURE__ */ e.createElement(
        za,
        {
          key: M.value,
          id: `${l}-opt-${Y}`,
          option: M,
          highlighted: Y === x,
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
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : V,
      onKeyDown: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, d) : a.map((M, Y) => {
      let le = "";
      return N === Y ? le = "tlDropdownSelect__chip--dragging" : B === Y && O === "before" ? le = "tlDropdownSelect__chip--dropBefore" : B === Y && O === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Wa,
        {
          key: M.value,
          option: M,
          removable: !u && (s || !c),
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
        "aria-label": _["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), He && Yt.createPortal(He, document.body));
}, { useCallback: st, useRef: Ka } = e, Gt = "application/x-tl-color", Ya = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: s,
  onReplace: i
}) => {
  const c = Ka(null), u = st(
    (m) => (d) => {
      c.current = m, d.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = st((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = st(
    (m) => (d) => {
      d.preventDefault();
      const f = d.dataTransfer.getData(Gt);
      f ? i(m, f) : c.current !== null && c.current !== m && s(c.current, m), c.current = null;
    },
    [s, i]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((m, d) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: d,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => n(m) : void 0,
        onDoubleClick: m != null ? () => a(m) : void 0,
        onDragStart: m != null ? u(d) : void 0,
        onDragOver: r,
        onDrop: o(d)
      }
    ))
  );
};
function Xt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function bt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function qt(l) {
  if (!bt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Zt(l, t, n) {
  const a = (s) => Xt(s).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function Ga(l, t, n) {
  const a = l / 255, s = t / 255, i = n / 255, c = Math.max(a, s, i), u = Math.min(a, s, i), r = c - u;
  let o = 0;
  r !== 0 && (c === a ? o = (s - i) / r % 6 : c === s ? o = (i - a) / r + 2 : o = (a - s) / r + 4, o *= 60, o < 0 && (o += 360));
  const m = c === 0 ? 0 : r / c;
  return [o, m, c];
}
function Xa(l, t, n) {
  const a = n * t, s = a * (1 - Math.abs(l / 60 % 2 - 1)), i = n - a;
  let c = 0, u = 0, r = 0;
  return l < 60 ? (c = a, u = s, r = 0) : l < 120 ? (c = s, u = a, r = 0) : l < 180 ? (c = 0, u = a, r = s) : l < 240 ? (c = 0, u = s, r = a) : l < 300 ? (c = s, u = 0, r = a) : (c = a, u = 0, r = s), [
    Math.round((c + i) * 255),
    Math.round((u + i) * 255),
    Math.round((r + i) * 255)
  ];
}
function qa(l) {
  return Ga(...qt(l));
}
function ct(l, t, n) {
  return Zt(...Xa(l, t, n));
}
const { useCallback: Pe, useRef: Ot } = e, Za = ({ color: l, onColorChange: t }) => {
  const [n, a, s] = qa(l), i = Ot(null), c = Ot(null), u = Pe(
    (g, k) => {
      var L;
      const E = (L = i.current) == null ? void 0 : L.getBoundingClientRect();
      if (!E) return;
      const v = Math.max(0, Math.min(1, (g - E.left) / E.width)), y = Math.max(0, Math.min(1, 1 - (k - E.top) / E.height));
      t(ct(n, v, y));
    },
    [n, t]
  ), r = Pe(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), u(g.clientX, g.clientY);
    },
    [u]
  ), o = Pe(
    (g) => {
      g.buttons !== 0 && u(g.clientX, g.clientY);
    },
    [u]
  ), m = Pe(
    (g) => {
      var y;
      const k = (y = c.current) == null ? void 0 : y.getBoundingClientRect();
      if (!k) return;
      const v = Math.max(0, Math.min(1, (g - k.top) / k.height)) * 360;
      t(ct(v, a, s));
    },
    [a, s, t]
  ), d = Pe(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), m(g.clientY);
    },
    [m]
  ), f = Pe(
    (g) => {
      g.buttons !== 0 && m(g.clientY);
    },
    [m]
  ), _ = ct(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: _ },
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
      ref: c,
      className: "tlColorInput__hueSlider",
      onPointerDown: d,
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
function Qa(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const Ja = {
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
}, { useState: Ye, useCallback: we, useEffect: Ft, useRef: er, useLayoutEffect: tr } = e, nr = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: s,
  canReset: i,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [o, m] = Ye("palette"), [d, f] = Ye(t), _ = er(null), g = ue(Ja), [k, E] = Ye(null);
  tr(() => {
    if (!l.current || !_.current) return;
    const A = l.current.getBoundingClientRect(), P = _.current.getBoundingClientRect();
    let q = A.bottom + 4, p = A.left;
    q + P.height > window.innerHeight && (q = A.top - P.height - 4), p + P.width > window.innerWidth && (p = Math.max(0, A.right - P.width)), E({ top: q, left: p });
  }, [l]);
  const v = d != null, [y, L, x] = v ? qt(d) : [0, 0, 0], [b, w] = Ye((d == null ? void 0 : d.toUpperCase()) ?? "");
  Ft(() => {
    w((d == null ? void 0 : d.toUpperCase()) ?? "");
  }, [d]), Le(!0, { ESCAPE: u }), Ft(() => {
    const A = (q) => {
      _.current && !_.current.contains(q.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const h = we(
    (A) => (P) => {
      const q = parseInt(P.target.value, 10);
      if (isNaN(q)) return;
      const p = Xt(q);
      f(Zt(A === "r" ? p : y, A === "g" ? p : L, A === "b" ? p : x));
    },
    [y, L, x]
  ), D = we(
    (A) => {
      if (d != null) {
        A.dataTransfer.setData(Gt, d.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = d, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [d]
  ), R = we((A) => {
    const P = A.target.value;
    w(P), bt(P) && f(P);
  }, []), N = we(() => {
    f(null);
  }, []), z = we((A) => {
    f(A);
  }, []), B = we(
    (A) => {
      c(A);
    },
    [c]
  ), I = we(
    (A, P) => {
      const q = [...n], p = q[A];
      q[A] = q[P], q[P] = p, r(q);
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
      if (Qa(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const q = [...n];
      q[P] = A.toUpperCase(), r(q);
    },
    [n, r]
  ), $ = we(() => {
    d != null && H(d), c(d);
  }, [d, c, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: _,
      style: k ? { top: k.top, left: k.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      g["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      g["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Ya,
      {
        colors: n,
        columns: a,
        onSelect: z,
        onConfirm: B,
        onSwap: I,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Z }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Za, { color: d ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: d } : void 0,
        draggable: v,
        onDragStart: v ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? y : "",
        onChange: h("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
        onChange: h("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? x : "",
        onChange: h("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !bt(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: $ }, g["js.colorInput.ok"]))
  );
}, lr = { "js.colorInput.chooseColor": "Choose color" }, { useState: ar, useCallback: Ge, useRef: rr } = e, or = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = ae(), i = ue(lr), [c, u] = ar(!1), r = rr(null), o = n, m = t.editable !== !1, d = t.palette ?? [], f = t.paletteColumns ?? 6, _ = t.defaultPalette ?? d, g = Ge(() => {
    m && u(!0);
  }, [m]), k = Ge(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Ge(() => {
    u(!1);
  }, []), v = Ge(
    (y) => {
      s("paletteChanged", { palette: y });
    },
    [s]
  );
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlColorInput__swatch" + (o == null ? " tlColorInput__swatch--noColor" : ""),
      style: o != null ? { backgroundColor: o } : void 0,
      onClick: g,
      disabled: t.disabled === !0,
      title: o ?? "",
      "aria-label": i["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    nr,
    {
      anchorRef: r,
      currentColor: o,
      palette: d,
      paletteColumns: f,
      defaultPalette: _,
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
}, { useState: Ue, useCallback: De, useEffect: it, useRef: $t, useLayoutEffect: sr, useMemo: cr } = e, ir = {
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
}, ur = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: s,
  onCancel: i,
  onLoadIcons: c
}) => {
  const u = ue(ir), [r, o] = Ue("simple"), [m, d] = Ue(""), [f, _] = Ue(t ?? ""), [g, k] = Ue(!1), [E, v] = Ue(null), y = $t(null), L = $t(null);
  sr(() => {
    if (!l.current || !y.current) return;
    const B = l.current.getBoundingClientRect(), I = y.current.getBoundingClientRect();
    let O = B.bottom + 4, Z = B.left;
    O + I.height > window.innerHeight && (O = B.top - I.height - 4), Z + I.width > window.innerWidth && (Z = Math.max(0, B.right - I.width)), v({ top: O, left: Z });
  }, [l]), it(() => {
    !a && !g && c().catch(() => k(!0));
  }, [a, g, c]), it(() => {
    a && L.current && L.current.focus();
  }, [a]), Le(!0, { ESCAPE: i }), it(() => {
    const B = (O) => {
      y.current && !y.current.contains(O.target) && i();
    }, I = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", B);
    };
  }, [i]);
  const x = cr(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (I) => I.prefix.toLowerCase().includes(B) || I.label.toLowerCase().includes(B) || I.terms != null && I.terms.some((O) => O.includes(B))
    );
  }, [n, m]), b = De((B) => {
    d(B.target.value);
  }, []), w = De(
    (B) => {
      s(B);
    },
    [s]
  ), h = De((B) => {
    _(B);
  }, []), D = De((B) => {
    _(B.target.value);
  }, []), R = De(() => {
    s(f || null);
  }, [f, s]), N = De(() => {
    s(null);
  }, [s]), z = De(async (B) => {
    B.preventDefault(), k(!1);
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
        value: m,
        onChange: b,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => d(""),
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: z }, u["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && x.map(
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
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ye, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, dr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: mr, useCallback: Xe, useRef: pr } = e, fr = ({ controlId: l, state: t }) => {
  const [n, a] = ke(), s = ae(), i = ue(dr), [c, u] = mr(!1), r = pr(null), o = n, m = t.editable !== !1, d = t.disabled === !0, f = t.icons ?? [], _ = t.iconsLoaded === !0, g = Xe(() => {
    m && !d && u(!0);
  }, [m, d]), k = Xe(
    (y) => {
      u(!1), a(y);
    },
    [a]
  ), E = Xe(() => {
    u(!1);
  }, []), v = Xe(async () => {
    await s("loadIcons");
  }, [s]);
  return m ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      className: "tlIconSelect__swatch" + (o == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: g,
      disabled: d,
      title: o ?? "",
      "aria-label": i["js.iconSelect.chooseIcon"]
    },
    o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    ur,
    {
      anchorRef: r,
      currentValue: o,
      icons: f,
      iconsLoaded: _,
      onSelect: k,
      onCancel: E,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, o ? /* @__PURE__ */ e.createElement(ye, { encoded: o }) : null));
}, { useCallback: je, useEffect: hr, useMemo: Ut, useRef: br, useState: ut } = e, gr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, _r = [1, 2, 3, 4];
function vr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), s = n[2] || "px";
  return s === "rem" || s === "em" ? a * t : a;
}
function Er(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const s of _r)
    n >= s && (a = s);
  return a;
}
function Cr(l, t) {
  const n = gr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function wr(l, t) {
  const n = Math.max(1, t), a = {}, s = (d, f) => !!(a[d] && a[d][f]), i = (d, f) => {
    a[d] || (a[d] = {}), a[d][f] = !0;
  }, c = [];
  let u = 0, r = 0;
  const o = (d) => {
    let f = null;
    for (const g of c) g.rowStart === d && (f = g);
    if (!f) return;
    let _ = f.colEnd;
    for (; _ < n && !s(d, _); ) _++;
    if (_ !== f.colEnd) {
      for (let g = f.rowStart; g < f.rowEnd; g++)
        for (let k = f.colEnd; k < _; k++) i(g, k);
      f.colEnd = _;
    }
  };
  for (const d of l) {
    const f = n <= 1 ? 1 : Math.max(1, d.rowSpan || 1);
    let _ = Math.min(Cr(d.width, n), n);
    for (; s(u, r); )
      r++, r >= n && (r = 0, u++);
    let g = 0;
    for (let L = r; L < n && !s(u, L); L++)
      g++;
    if (_ > g) {
      for (o(u), r = 0, u++; s(u, r); )
        r++, r >= n && (r = 0, u++);
      g = 0;
      for (let L = r; L < n && !s(u, L); L++)
        g++;
      _ = Math.min(_, g);
    }
    const k = r, E = r + _, v = u, y = u + f;
    c.push({ id: d.id, colStart: k, colEnd: E, rowStart: v, rowEnd: y });
    for (let L = v; L < y; L++)
      for (let x = k; x < E; x++) i(L, x);
    r = E, r >= n && (r = 0, u++);
  }
  o(u);
  let m = 0;
  for (const d of c) d.rowEnd > m && (m = d.rowEnd);
  for (let d = 1; d < m; d++)
    for (let f = 0; f < n; f++) {
      if (s(d, f)) continue;
      const _ = c.find((g) => g.rowEnd === d && g.colStart <= f && f < g.colEnd);
      if (_) {
        _.rowEnd = d + 1;
        for (let g = _.colStart; g < _.colEnd; g++) i(d, g);
      }
    }
  return c;
}
const yr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.minColWidth ?? "16rem", s = (t.children ?? []).filter((w) => w && w.id), i = br(null), [c, u] = ut(1), r = t.editMode === !0;
  hr(() => {
    const w = i.current;
    if (!w) return;
    const h = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = vr(a, h), R = () => u(Er(w.clientWidth, D));
    R();
    const N = new ResizeObserver(R);
    return N.observe(w), () => N.disconnect();
  }, [a]);
  const o = Ut(() => wr(s, c), [s, c]), m = Ut(() => {
    const w = {};
    for (const h of o) w[h.id] = h;
    return w;
  }, [o]), [d, f] = ut(null), [_, g] = ut(null), k = je((w, h) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    f(h), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", h);
  }, [r]), E = je((w, h) => {
    if (!r || !d || d === h) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    g((N) => N && N.id === h && N.before === R ? N : { id: h, before: R });
  }, [r, d]), v = je(() => {
  }, []), y = je((w, h, D) => {
    const R = s.map((I) => I.id), N = R.indexOf(w);
    if (N < 0) return;
    R.splice(N, 1);
    const z = R.indexOf(h);
    if (z < 0) {
      R.splice(N, 0, w);
      return;
    }
    const B = D ? z : z + 1;
    R.splice(B, 0, w), n("reorder", { order: R });
  }, [s, n]), L = je((w, h) => {
    if (!r || !d || d === h) return;
    w.preventDefault();
    const D = w.currentTarget.getBoundingClientRect(), R = w.clientX < D.left + D.width / 2;
    y(d, h, R), f(null), g(null);
  }, [r, d, y]), x = je(() => {
    f(null), g(null);
  }, []), b = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, s.map((w) => {
      const h = m[w.id];
      if (!h) return null;
      const D = {
        gridColumn: `${h.colStart + 1} / ${h.colEnd + 1}`,
        gridRow: `${h.rowStart + 1} / ${h.rowEnd + 1}`
      }, R = ["tlDashboard__tile"];
      return d === w.id && R.push("tlDashboard__tile--dragging"), _ && _.id === w.id && R.push(_.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: R.join(" "),
          style: D,
          draggable: r,
          onDragStart: (N) => k(N, w.id),
          onDragOver: (N) => E(N, w.id),
          onDragLeave: v,
          onDrop: (N) => L(N, w.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: kr, useRef: Ht, useState: Wt, useEffect: Sr, useLayoutEffect: Nr } = e, Tr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, Rr = ({ group: l }) => {
  var d, f;
  const [t, n] = Wt(!1), [a, s] = Wt({}), i = Ht(null), c = Ht(null), u = kr(() => {
    n((_) => !_);
  }, []);
  Nr(() => {
    if (!t) return;
    const _ = () => {
      const g = i.current;
      if (!g) return;
      const k = g.getBoundingClientRect();
      s({
        position: "fixed",
        top: k.bottom + 4,
        right: Math.max(8, window.innerWidth - k.right),
        left: "auto"
      });
    };
    return _(), window.addEventListener("resize", _), window.addEventListener("scroll", _, !0), () => {
      window.removeEventListener("resize", _), window.removeEventListener("scroll", _, !0);
    };
  }, [t]), Sr(() => {
    if (!t) return;
    const _ = (g) => {
      c.current && !c.current.contains(g.target) && i.current && !i.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [t]), Le(t, { ESCAPE: () => n(!1) }), _t(t, c, "first");
  const r = l.items.filter((_) => _ != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((d = l.subGroups) != null && d.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: r[0] })));
  const o = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: i,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onMouseDown: (_) => _.preventDefault(),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? o : void 0,
      title: m ? o : void 0
    },
    m ? /* @__PURE__ */ e.createElement(ye, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, o), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), Yt.createPortal(
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
      r.map((_, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: _ }))),
      (f = l.subGroups) == null ? void 0 : f.map((_, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), _.items.map((k, E) => /* @__PURE__ */ e.createElement("div", { key: E, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: k })))))
    ),
    document.body
  ));
}, Dr = ({ controlId: l }) => {
  const a = (G().groups ?? []).filter((s) => s.items.some((i) => i != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((s, i) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, i > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(Rr, { group: s }) : /* @__PURE__ */ e.createElement(Tr, { group: s }))));
}, Lr = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, xr = ({ controlId: l }) => {
  const t = G(), n = ae(), a = t.content, s = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, s && s.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, s.map((i, c) => {
    const u = c === s.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: i.depth }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: i.depth })
      },
      i.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, Ir = ({ controlId: l }) => {
  const n = G().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: a })));
}, Mr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Pr = {
  "js.sidebar.openDrawer": "Open navigation"
}, jr = ({ controlId: l }) => {
  const t = ae(), n = ue(Pr);
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
F("TLButton", Rn);
F("TLUploadButton", Dn);
F("TLToggleButton", xn);
F("TLTextInput", mn);
F("TLPasswordInput", fn);
F("TLNumberInput", bn);
F("TLDatePicker", _n);
F("TLSelect", En);
F("TLBooleanChoice", wn);
F("TLCheckbox", Nn);
F("TLCounter", In);
F("TLTabBar", Pn);
F("TLFieldList", jn);
F("TLAudioRecorder", An);
F("TLAudioPlayer", Fn);
F("TLFileUpload", Un);
F("TLBinaryField", Wn);
F("TLFileChips", Kn);
F("TLRelativeTime", Xn);
F("TLAnchor", qn);
F("TLScrollLink", Zn);
F("TLAvatar", el);
F("TLDownload", nl);
F("TLPhotoCapture", al);
F("TLPhotoViewer", ol);
F("TLPdfViewer", cl);
F("TLSplitPanel", il);
F("TLPanel", bl);
F("TLInset", Rl);
F("TLMaximizeRoot", gl);
F("TLDeckPane", _l);
F("TLSidebar", Nl);
F("TLStack", Tl);
F("TLGrid", Dl);
F("TLCard", Ll);
F("TLAppBar", xl);
F("TLBreadcrumb", Ml);
F("TLBottomBar", jl);
F("TLDialog", Ol);
F("TLDialogManager", Ul);
F("TLWindow", Vl);
F("TLDrawer", Gl);
F("TLMenuRegion", ql);
F("TLSnackbar", ea);
F("TLNoticeBar", sa);
F("TLMenu", ia);
F("TLAppShell", da);
F("TLText", ma);
F("TLTableView", ga);
F("TLColumnSelect", va);
F("TLFormLayout", Ra);
F("TLFormGroup", xa);
F("TLFormField", ja);
F("TLResourceCell", Ba);
F("TLTreeView", Oa);
F("TLDropdownSelect", Va);
F("TLColorInput", or);
F("TLIconSelect", fr);
F("TLDashboard", yr);
F("TLToolbar", Dr);
F("TLTileStack", Lr);
F("TLAdaptiveDetail", xr);
F("TLSlot", Ir);
F("TLSlotContent", Mr);
F("TLDrawerToggle", jr);
