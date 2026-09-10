import { React as e, useTLFieldValue as De, useTLCommand as ne, useTLState as X, useKeyboardBinding as pe, useTLUpload as Ye, useFill as yt, FillBarrier as Pe, TLChild as G, useI18N as ue, useTLDataUrl as Ge, scrollToAnchor as Zn, useStandaloneKeyboardScope as Oe, useFillHost as rt, FillProvider as ot, KeyboardScopeProvider as Yt, useFocusTrap as Gt, CMD_VALUE_CHANGED as it, anchoredOverlayProps as Qn, register as U } from "tl-react-bridge";
const { useCallback: en, useRef: Jn } = e, el = 300, tl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: el,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = ne(), s = Jn(!1), i = en(
    (N) => {
      s.current = !0, a(N.target.value);
    },
    [a]
  ), r = t.commitOnBlur === !0, c = en(async () => {
    await o(), r && s.current && (s.current = !1, u("commit"));
  }, [o, r, u]), d = t.multiline === !0;
  if (t.editable === !1) {
    const N = "tlReactTextInput tlReactTextInput--immutable" + (d ? " tlReactTextInput--multiline" : "");
    return /* @__PURE__ */ e.createElement(
      "span",
      {
        id: l,
        className: N,
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
}, { useCallback: tn } = e, nl = 300, ll = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({ debounceMs: nl }), u = tn(
    (m) => {
      a(m.target.value);
    },
    [a]
  ), s = tn(() => {
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
}, { useCallback: nn } = e, al = 300, rl = ({ controlId: l, state: t }) => {
  const [n, a, o] = De({
    debounceMs: al,
    sendOnBlur: t.sendValueOnBlur === !0
  }), u = nn(
    (p) => {
      const h = p.target.value;
      a(h === "" ? null : h);
    },
    [a]
  ), s = nn(() => {
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
}, { useCallback: ol } = e, sl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ol(
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
}, { useCallback: cl } = e, il = ({ controlId: l, state: t, config: n }) => {
  var d;
  const [a, o] = De(), u = cl(
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
}, { useCallback: ul } = e, dl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.options ?? [], u = t.presentation === "select", s = t.disabled === !0, i = t.hasError === !0, r = t.hasWarnings === !0, c = ul(
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
}, { useCallback: ml, useRef: pl, useEffect: fl } = e, hl = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = t.triState === !0, u = pl(null);
  fl(() => {
    u.current && (u.current.indeterminate = o && n !== !0 && n !== !1);
  }, [o, n]);
  const s = ml(
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
const { useCallback: bl } = e, gl = ({ controlId: l, command: t, label: n, image: a, disabled: o, displayMode: u }) => {
  const s = X(), i = ne(), r = t ?? "click", c = n ?? s.label, d = a ?? s.image, m = o ?? s.disabled === !0, p = u ?? s.displayMode ?? "label-only", h = s.hidden === !0, g = s.tooltip, N = s.appearance, C = s.size, _ = s.cssClasses, y = s.navigateUrl, k = bl(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    i(r);
  }, [i, r, y]), x = s.keyGesture;
  pe(x, () => m || h ? !1 : (k(), !0));
  const E = p === "icon-only", w = p === "label-only" || p === "icon-label" || E && !d, b = g ?? (E ? c : void 0), D = b ? `text:${b}` : void 0;
  return h ? null : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: k,
      disabled: m,
      className: "tlReactButton" + (E ? " tlReactButton--iconOnly" : "") + (p === "label-only" ? " tlReactButton--labelOnly" : "") + (N === "link" ? " tlReactButton--link" : "") + (N === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : "") + (_ ? " " + _ : ""),
      "data-tooltip": D,
      "aria-label": d || E ? c : void 0
    },
    d && /* @__PURE__ */ e.createElement(Ne, { encoded: d, className: "tlReactButton__image" }),
    w && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, El = ({ controlId: l }) => {
  const t = X(), n = Ye(), a = e.useRef(null), [o, u] = e.useState(!1), s = t.label ?? "", i = t.image, r = t.disabled === !0, c = t.hidden === !0, d = t.displayMode ?? "label-only", m = t.appearance, p = t.accept, h = t.multiple === !0, g = e.useCallback(() => {
    var x;
    r || o || (x = a.current) == null || x.click();
  }, [r, o]), N = e.useCallback(async (x) => {
    const E = x.target.files;
    if (!E || E.length === 0) return;
    const w = new FormData();
    for (let b = 0; b < E.length; b++)
      w.append("file", E[b], E[b].name);
    x.target.value = "", u(!0);
    try {
      await n(w);
    } finally {
      u(!1);
    }
  }, [n]), C = d === "icon-only", _ = d === "icon-only" || d === "icon-label", y = d === "label-only" || d === "icon-label" || C && !i, k = r || o;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: p && p !== "*" ? p : void 0,
      multiple: h || void 0,
      onChange: N,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: g,
      disabled: k,
      style: c ? { display: "none" } : void 0,
      className: "tlReactButton" + (C ? " tlReactButton--iconOnly" : "") + (m === "link" ? " tlReactButton--link" : "") + (m === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": C ? s : void 0
    },
    _ && i && /* @__PURE__ */ e.createElement(Ne, { encoded: i, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  ));
}, { useCallback: vl } = e, _l = ({ controlId: l, command: t, label: n, active: a, disabled: o }) => {
  const u = X(), s = ne(), i = t ?? "click", r = n ?? u.label, c = a ?? u.active === !0, d = o ?? u.disabled === !0, m = vl(() => {
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
}, Cl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: yl } = e, wl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0), o = t.tabs ?? [], u = t.activeTabId, s = yl((i) => {
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
}, kl = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: o })))));
}, Nl = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Sl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), r = e.useRef([]), c = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, h = e.useCallback(async () => {
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
        const k = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, k ? { mimeType: k } : void 0);
        i.current = x, x.ondataavailable = (E) => {
          E.data.size > 0 && r.current.push(E.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((b) => b.stop()), c.current = null;
          const E = new Blob(r.current, { type: x.mimeType || "audio/webm" });
          if (r.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const w = new FormData();
          w.append("audio", E, "recording.webm"), await n(w), o("idle");
        }, x.start(), o("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), g = ue(Nl), N = p === "recording" ? g["js.audioRecorder.stop"] : p === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], C = p === "uploading", _ = ["tlAudioRecorder__button"];
  return p === "recording" && _.push("tlAudioRecorder__button--recording"), p === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: h,
      disabled: C,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Dl = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Tl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [u, s] = e.useState(a ? "idle" : "disabled"), i = e.useRef(null), r = e.useRef(null), c = e.useRef(o);
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
        const C = await fetch(n);
        if (!C.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", C.status), s("idle");
          return;
        }
        const _ = await C.blob();
        r.current = URL.createObjectURL(_);
      } catch (C) {
        console.error("[TLAudioPlayer] Fetch error:", C), s("idle");
        return;
      }
    }
    const N = new Audio(r.current);
    i.current = N, N.onended = () => {
      s("idle");
    }, N.play(), s("playing");
  }, [u, n]), m = ue(Dl), p = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], h = u === "disabled" || u === "loading", g = ["tlAudioPlayer__button"];
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
}, Rl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ll = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), r = t.status ?? "idle", c = t.error, d = t.accept ?? "", m = r === "received" ? "idle" : a !== "idle" ? a : r, p = e.useCallback(async (E) => {
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
  }, [a]), N = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), C = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), _ = e.useCallback((E) => {
    var b;
    if (E.preventDefault(), E.stopPropagation(), s(!1), a === "uploading") return;
    const w = (b = E.dataTransfer.files) == null ? void 0 : b[0];
    w && p(w);
  }, [a, p]), y = m === "uploading", k = ue(Rl), x = m === "uploading" ? k["js.uploading"] : k["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: N,
      onDragLeave: C,
      onDrop: _
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
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, xl = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, Ml = ({ controlId: l, state: t }) => {
  const a = X() ?? t ?? {}, o = Ye(), u = Ge(), s = ue(xl), i = a.editable !== !1, r = !!a.hasData, c = a.fileName ?? "download", d = a.dataRevision ?? 0, m = a.accept ?? "", p = a.status ?? "idle", h = a.error ?? null, [g, N] = e.useState("idle"), [C, _] = e.useState(!1), [y, k] = e.useState(!1), x = e.useRef(null), E = e.useCallback(async () => {
    if (!(!r || y)) {
      k(!0);
      try {
        const I = u + (u.includes("?") ? "&" : "?") + "rev=" + d, L = await fetch(I);
        if (!L.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", L.status);
          return;
        }
        const S = await L.blob(), $ = URL.createObjectURL(S), f = document.createElement("a");
        f.href = $, f.download = c, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL($);
      } catch (I) {
        console.error("[TLBinaryField] Fetch error:", I);
      } finally {
        k(!1);
      }
    }
  }, [r, y, u, d, c]), w = e.useCallback(async (I) => {
    N("uploading");
    const L = new FormData();
    L.append("file", I, I.name), await o(L), N("idle");
  }, [o]), b = (p === "received" ? "idle" : g !== "idle" ? g : p) === "uploading", D = e.useCallback((I) => {
    var S;
    const L = (S = I.target.files) == null ? void 0 : S[0];
    L && w(L);
  }, [w]), j = e.useCallback(() => {
    var I;
    b || (I = x.current) == null || I.click();
  }, [b]), R = e.useCallback((I) => {
    I.preventDefault(), I.stopPropagation(), _(!0);
  }, []), H = e.useCallback((I) => {
    I.preventDefault(), I.stopPropagation(), _(!1);
  }, []), K = e.useCallback((I) => {
    var S;
    if (I.preventDefault(), I.stopPropagation(), _(!1), b) return;
    const L = (S = I.dataTransfer.files) == null ? void 0 : S[0];
    L && w(L);
  }, [b, w]), A = y ? s["js.downloading"] : s["js.download.file"].replace("{0}", c), z = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, z) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, s["js.download.noFile"]));
  const B = b, P = b ? s["js.uploading"] : s["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${C ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: H,
      onDrop: K
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
        className: "tlFileUpload__button" + (B ? " tlFileUpload__button--uploading" : ""),
        onClick: j,
        disabled: B,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && z,
    h && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, h)
  );
}, Il = {
  "js.fileChips.add": "Add file",
  "js.fileChips.remove": "Remove {0}",
  "js.uploading": "Uploading…",
  "js.download.file": "Download {0}"
};
function jl(l) {
  return l < 1024 ? l + " B" : l < 1024 * 1024 ? (l / 1024).toFixed(l < 10 * 1024 ? 1 : 0) + " KB" : (l / (1024 * 1024)).toFixed(1) + " MB";
}
const Pl = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Ye(), o = Ge(), u = ue(Il), s = t.chips ?? [], i = t.editable === !0, [r, c] = e.useState(!1), [d, m] = e.useState(!1), p = e.useRef(null), h = e.useCallback(async (E) => {
    const w = Array.from(E);
    if (w.length !== 0) {
      c(!0);
      try {
        const b = new FormData();
        for (const D of w)
          b.append("file", D, D.name);
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
        const D = await b.blob(), j = URL.createObjectURL(D), R = document.createElement("a");
        R.href = j, R.download = E.name, R.style.display = "none", document.body.appendChild(R), R.click(), document.body.removeChild(R), URL.revokeObjectURL(j);
      } catch (w) {
        console.error("[TLFileChips] Fetch error:", w);
      }
  }, [o]), N = e.useCallback((E) => {
    E.target.files && h(E.target.files), E.target.value = "";
  }, [h]), C = e.useCallback(() => {
    var E;
    r || (E = p.current) == null || E.click();
  }, [r]), _ = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!0));
  }, [i]), y = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1));
  }, [i]), k = e.useCallback((E) => {
    i && (E.preventDefault(), E.stopPropagation(), m(!1), !r && E.dataTransfer.files && h(E.dataTransfer.files));
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
      onDragOver: _,
      onDragLeave: y,
      onDrop: k
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
        E.size != null && /* @__PURE__ */ e.createElement("span", { className: "tlFileChip__size" }, jl(E.size))
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
        onChange: N,
        style: { display: "none" }
      }
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileChips__add" + (r ? " tlFileChips__add--uploading" : ""),
        onClick: C,
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
}, Bl = 3e4;
function Al(l, t) {
  const n = Math.round((l - Date.now()) / 1e3), a = Math.abs(n), o = new Intl.RelativeTimeFormat(t, { numeric: "auto" });
  return a < 60 ? o.format(Math.trunc(n / 1), "second") : a < 3600 ? o.format(Math.trunc(n / 60), "minute") : a < 86400 ? o.format(Math.trunc(n / 3600), "hour") : a < 7 * 86400 ? o.format(Math.trunc(n / 86400), "day") : new Date(l).toLocaleDateString(t);
}
const Fl = ({ controlId: l }) => {
  const t = X(), n = t.timestamp, a = t.label ?? void 0, o = t.locale || navigator.language, [, u] = e.useState(0);
  return e.useEffect(() => {
    const s = setInterval(() => u((i) => i + 1), Bl);
    return () => clearInterval(s);
  }, []), n == null ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime tlRelativeTime--empty" }) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlRelativeTime", title: a }, Al(n, o));
}, Ol = ({ controlId: l }) => {
  const t = X(), n = t.anchor ?? void 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAnchor", "data-tl-anchor": n }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child }));
}, $l = ({ controlId: l }) => {
  const t = X(), n = t.target, a = t.label ?? "";
  if (n == null)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlScrollLink tlScrollLink--empty" });
  const o = (u) => {
    u.preventDefault(), Zn(n);
  };
  return /* @__PURE__ */ e.createElement("a", { id: l, className: "tlScrollLink", href: "#", onClick: o }, a);
};
function Hl(l) {
  const t = l.trim().split(/\s+/).filter(Boolean);
  return t.length === 0 ? "?" : t.length === 1 ? t[0].slice(0, 2).toUpperCase() : (t[0][0] + t[t.length - 1][0]).toUpperCase();
}
function Wl(l) {
  let t = 0;
  for (let n = 0; n < l.length; n++)
    t = t * 31 + l.charCodeAt(n) | 0;
  return Math.abs(t) % 360;
}
const Ul = ({ controlId: l }) => {
  const n = X().name;
  return n ? /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlAvatar",
      style: { backgroundColor: `hsl(${Wl(n)}, 45%, 45%)` },
      title: n,
      "aria-label": n
    },
    Hl(n)
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlAvatar tlAvatar--empty" });
}, Vl = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, zl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = ne(), o = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [r, c] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || r)) {
      c(!0);
      try {
        const g = n + (n.includes("?") ? "&" : "?") + "rev=" + u, N = await fetch(g);
        if (!N.ok) {
          console.error("[TLDownload] Failed to fetch data:", N.status);
          return;
        }
        const C = await N.blob(), _ = URL.createObjectURL(C), y = document.createElement("a");
        y.href = _, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(_);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        c(!1);
      }
    }
  }, [o, r, n, u, s]), m = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = ue(Vl);
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
}, Kl = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Yl = ({ controlId: l }) => {
  const t = X(), n = Ye(), [a, o] = e.useState("idle"), [u, s] = e.useState(null), [i, r] = e.useState(!1), c = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = t.error, N = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), C = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null), c.current && (c.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    C(), o("idle");
  }, [C]), y = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (s(null), !N) {
        (R = p.current) == null || R.click();
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
  }, [a, N]), k = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = c.current, H = m.current;
    if (!R || !H)
      return;
    H.width = R.videoWidth, H.height = R.videoHeight;
    const K = H.getContext("2d");
    K && (K.drawImage(R, 0, 0), C(), o("uploading"), H.toBlob(async (A) => {
      if (!A) {
        o("idle");
        return;
      }
      const z = new FormData();
      z.append("photo", A, "capture.jpg"), await n(z), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, C]), x = e.useCallback(async (R) => {
    var A;
    const H = (A = R.target.files) == null ? void 0 : A[0];
    if (!H) return;
    o("uploading");
    const K = new FormData();
    K.append("photo", H, H.name), await n(K), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && c.current && d.current && (c.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var H;
    if (a !== "overlayOpen") return;
    (H = h.current) == null || H.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), Oe(a === "overlayOpen", { ESCAPE: _ }), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((R) => R.stop()), d.current = null);
  }, []);
  const E = ue(Kl), w = a === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], b = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && b.push("tlPhotoCapture__cameraBtn--uploading");
  const D = ["tlPhotoCapture__overlayVideo"];
  i && D.push("tlPhotoCapture__overlayVideo--mirrored");
  const j = ["tlPhotoCapture__mirrorBtn"];
  return i && j.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
  )), !N && /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
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
        className: j.join(" "),
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
        onClick: k,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[u]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Gl = {
  "js.photoViewer.alt": "Captured photo"
}, Xl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(o);
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
  const r = ue(Gl);
  return !a || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: t.alt || r["js.photoViewer.alt"]
    }
  ));
}, ql = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, Zl = ({ controlId: l }) => {
  const t = X(), n = Ge(), a = !!t.hasPdf, o = t.dataRevision ?? 0, u = ue(ql), i = n.indexOf("react-api/"), r = i >= 0 ? n.slice(0, i) : n, c = n + "&rev=" + o, d = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(c);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: d,
      title: u["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, u["js.pdfViewer.noDocument"]));
}, { useCallback: ln, useRef: Dt } = e, Ql = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0), o = t.orientation, u = t.resizable === !0, s = t.children ?? [], i = o === "horizontal", r = s.length > 0 && s.every((_) => _.collapsed), c = !r && s.some((_) => _.collapsed), d = r ? !i : i, m = Dt(null), p = Dt(null), h = Dt(null), g = ln((_, y) => {
    const k = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? r && !d ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : y !== void 0 ? k.flex = `0 0 ${y}px` : k.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (k.minWidth = i ? _.minSize : void 0, k.minHeight = i ? void 0 : _.minSize), k;
  }, [i, r, c, d]), N = ln((_, y) => {
    _.preventDefault();
    const k = m.current;
    if (!k) return;
    const x = s[y], E = s[y + 1], w = k.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    w.forEach((R) => {
      b.push(i ? R.offsetWidth : R.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: y,
      startPos: i ? _.clientX : _.clientY,
      startSizeBefore: b[y],
      startSizeAfter: b[y + 1],
      childBefore: x,
      childAfter: E
    };
    const D = (R) => {
      const H = p.current;
      if (!H || !h.current) return;
      const A = (i ? R.clientX : R.clientY) - H.startPos, z = H.childBefore.minSize || 0, B = H.childAfter.minSize || 0;
      let P = H.startSizeBefore + A, I = H.startSizeAfter - A;
      P < z && (I += P - z, P = z), I < B && (P += I - B, I = B), h.current[H.splitterIndex] = P, h.current[H.splitterIndex + 1] = I;
      const L = k.querySelectorAll(":scope > .tlSplitPanel__child"), S = L[H.splitterIndex], $ = L[H.splitterIndex + 1];
      S && (S.style.flex = `0 0 ${P}px`), $ && ($.style.flex = `0 0 ${I}px`);
    }, j = () => {
      if (document.removeEventListener("mousemove", D), document.removeEventListener("mouseup", j), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const R = {};
        s.forEach((H, K) => {
          const A = H.control;
          A != null && A.controlId && h.current && (R[A.controlId] = h.current[K]);
        }), n("updateSizes", { sizes: R });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", D), document.addEventListener("mouseup", j), document.body.style.cursor = i ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, i, n]), C = [];
  return s.forEach((_, y) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${y}`,
          className: `tlSplitPanel__child${_.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(_)
        },
        /* @__PURE__ */ e.createElement(G, { control: _.control })
      )
    ), u && y < s.length - 1) {
      const k = s[y + 1];
      !_.collapsed && !k.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${y}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (E) => N(E, y)
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
    C
  );
}, Ot = ({ image: l, className: t }) => {
  if (!l || l === "none") return null;
  const n = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `${t ? t + " " : ""}${n}`, "aria-hidden": "true" });
}, { useCallback: Tt } = e, Jl = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ea = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ta = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), na = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), la = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), aa = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ra = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Jl), o = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, r = t.showPopOut === !0, c = t.fullLine === !0, d = t.fill === !0, m = t.hoverActions === !0, p = t.appearance === "card", h = t.errorMessage, g = u === "MINIMIZED", N = u === "MAXIMIZED", C = u === "HIDDEN", _ = Tt(() => {
    n("toggleMinimize");
  }, [n]), y = Tt(() => {
    n("toggleMaximize");
  }, [n]), k = Tt(() => {
    n("popOut");
  }, [n]), x = yt(d && !C);
  if (C)
    return null;
  const E = N ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" }, w = s && !N || i && !g || r, b = !!o && o.trim() !== "" || !!t.titleContent || !!t.toolbar || w;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${c ? " tlPanel--fullLine" : ""}${x ? " " + x : ""}${m ? " tlPanel--hoverActions" : ""}${p ? " tlPanel--card" : ""}`,
      style: E
    },
    b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, !!o && o.trim() !== "" && /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), t.titleContent && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__titleContent" }, /* @__PURE__ */ e.createElement(G, { control: t.titleContent })), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(G, { control: t.toolbar }), s && !N && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: g ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      g ? /* @__PURE__ */ e.createElement(ta, null) : /* @__PURE__ */ e.createElement(ea, null)
    ), i && !g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: N ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      N ? /* @__PURE__ */ e.createElement(la, null) : /* @__PURE__ */ e.createElement(na, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(aa, null)
    ))),
    !g && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: t.child }))),
    !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error tlPanel__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ot, { image: t.errorIcon, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, h)),
    !g && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(G, { control: t.buttonBar }))
  );
}, oa = ({ controlId: l }) => {
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
}, sa = ({ controlId: l }) => {
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
}, { useCallback: Ee, useState: bt, useEffect: $t, useRef: Et } = e, ca = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ht(l, t, n, a) {
  const o = [];
  for (const u of l)
    if (u.type === "nav") {
      if (u.hidden) continue;
      o.push({ id: u.id, type: "nav", groupId: a });
    } else u.type === "command" ? o.push({ id: u.id, type: "command", groupId: a }) : u.type === "group" && (o.push({ id: u.id, type: "group" }), (n.get(u.id) ?? u.expanded) && !t && o.push(...Ht(u.children, t, n, u.id)));
  return o;
}
const Ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlSidebar__icon" }) : null, ia = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: o, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: o,
    ref: u,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ua = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: o, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: o,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), da = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), ma = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), pa = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: o, onClose: u }) => {
  const s = Et(null);
  $t(() => {
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
        /* @__PURE__ */ e.createElement(Ke, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, fa = ({
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
  onCloseFlyout: N
}) => {
  const C = Et(null), [_, y] = bt(null), k = Ee(() => {
    a ? h === l.id ? N() : (C.current && y(C.current.getBoundingClientRect()), g(l.id)) : s(l.id);
  }, [a, h, l.id, s, g, N]), x = Ee((w) => {
    C.current = w, r(w);
  }, [r]), E = a && h === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: k,
      title: a ? l.label : void 0,
      "aria-expanded": a ? E : t,
      tabIndex: i,
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
  ), E && /* @__PURE__ */ e.createElement(
    pa,
    {
      item: l,
      activeItemId: n,
      anchorRect: _,
      onSelect: o,
      onExecute: u,
      onClose: N
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
      onOpenFlyout: g,
      onCloseFlyout: N
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
        ia,
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
        ua,
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
      return /* @__PURE__ */ e.createElement(da, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(ma, null);
    case "group": {
      const h = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        fa,
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
}, ha = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ca), o = t.items ?? [], u = t.activeItemId, s = t.collapsed, i = t.drawerOpen, r = i ? !1 : s, [c, d] = bt(() => {
    const A = /* @__PURE__ */ new Map(), z = (B) => {
      for (const P of B)
        P.type === "group" && (A.set(P.id, P.expanded), z(P.children));
    };
    return z(o), A;
  }), m = Ee((A) => {
    d((z) => {
      const B = new Map(z), P = B.get(A) ?? !1;
      return B.set(A, !P), n("toggleGroup", { itemId: A, expanded: !P }), B;
    });
  }, [n]), p = Ee((A) => {
    A !== u && n("selectItem", { itemId: A });
  }, [n, u]), h = Ee((A) => {
    n("executeCommand", { itemId: A });
  }, [n]), g = Ee(() => {
    n("toggleCollapse", {});
  }, [n]), N = Ee(() => {
    n("toggleDrawer", {});
  }, [n]), [C, _] = bt(null), y = Ee((A) => {
    _(A);
  }, []), k = Ee(() => {
    _(null);
  }, []);
  $t(() => {
    r || _(null);
  }, [r]);
  const [x, E] = bt(() => {
    const A = Ht(o, r, c);
    return A.length > 0 ? A[0].id : "";
  }), w = Et(/* @__PURE__ */ new Map()), b = Ee((A) => (z) => {
    z ? w.current.set(A, z) : w.current.delete(A);
  }, []), D = Ee((A) => {
    E(A);
  }, []), j = Et(0), R = Ee((A) => {
    E(A), j.current++;
  }, []);
  $t(() => {
    const A = w.current.get(x);
    A && document.activeElement !== A && A.focus();
  }, [x, j.current]);
  const H = Ee((A) => {
    if (A.key === "Escape" && C !== null) {
      A.preventDefault(), k();
      return;
    }
    const z = Ht(o, r, c);
    if (z.length === 0) return;
    const B = z.findIndex((I) => I.id === x);
    if (B < 0) return;
    const P = z[B];
    switch (A.key) {
      case "ArrowDown": {
        A.preventDefault();
        const I = (B + 1) % z.length;
        R(z[I].id);
        break;
      }
      case "ArrowUp": {
        A.preventDefault();
        const I = (B - 1 + z.length) % z.length;
        R(z[I].id);
        break;
      }
      case "Home": {
        A.preventDefault(), R(z[0].id);
        break;
      }
      case "End": {
        A.preventDefault(), R(z[z.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        A.preventDefault(), P.type === "nav" ? p(P.id) : P.type === "command" ? h(P.id) : P.type === "group" && (r ? C === P.id ? k() : y(P.id) : m(P.id));
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
    C,
    R,
    p,
    h,
    m,
    y,
    k
  ]), K = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (i ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: K }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(G, { control: t.drawerToggleContribution }), i && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: N, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, o.map((A) => /* @__PURE__ */ e.createElement(
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
      setItemRef: b,
      onItemFocus: D,
      groupStates: c,
      flyoutGroupId: C,
      onOpenFlyout: y,
      onCloseFlyout: k
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
}, ba = ({ controlId: l }) => {
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
}, ga = ({ controlId: l }) => {
  const t = X(), [n, a] = rt();
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlInset " + n : "tlInset" }, t.child && /* @__PURE__ */ e.createElement(G, { control: t.child })));
}, Ea = ({ controlId: l }) => {
  const t = X(), n = t.columns, a = t.minColumnWidth, o = t.gap ?? "default", u = t.children ?? [], s = {};
  return a ? s.gridTemplateColumns = `repeat(auto-fit, minmax(min(${a}, 100%), 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${o}`, style: s }, u.map((i, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: i })));
}, va = ({ controlId: l }) => {
  const t = X(), n = t.title, a = t.variant ?? "outlined", o = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = n != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((r, c) => /* @__PURE__ */ e.createElement(G, { key: c, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))));
}, _a = ({ controlId: l }) => {
  const t = X(), n = t.title ?? "", a = t.leading, o = t.trailing, u = t.children ?? [], s = t.actions ?? [], i = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, u.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((d, m) => /* @__PURE__ */ e.createElement(G, { key: m, control: d }))), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__trailing" }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, { useCallback: Ca } = e, ya = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = Ca((u) => {
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
}, { useCallback: wa } = e, ka = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.items ?? [], o = t.activeItemId, u = wa((s) => {
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
}, { useCallback: an, useRef: Na } = e, Sa = ({ onClose: l }) => (pe("ESCAPE", () => (l(), !0)), null), Da = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.closeOnBackdrop !== !1, u = t.child, s = Na(null), i = an(() => {
    n("close");
  }, [n]), r = an((c) => {
    o && c.target === c.currentTarget && i();
  }, [o, i]);
  return a ? /* @__PURE__ */ e.createElement(Yt, null, /* @__PURE__ */ e.createElement(Sa, { onClose: i }), /* @__PURE__ */ e.createElement(
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
}, { useEffect: Ta, useRef: Ra } = e, La = ({ controlId: l }) => {
  const n = X().dialogs ?? [], a = Ra(n.length);
  return Ta(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((o) => /* @__PURE__ */ e.createElement(G, { key: o.controlId, control: o })));
}, { useCallback: ut, useRef: He, useState: dt } = e, xa = ({ onClose: l }) => (pe("ESCAPE", () => (l(), !0)), null), Ma = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ia = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], ja = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ma), o = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, r = t.resizable === !0, c = t.child, d = t.actions ?? [], m = t.toolbar, p = t.buttonBar, [h, g] = dt(null), [N, C] = dt(null), [_, y] = dt(null), k = He(null), [x, E] = dt(!1), w = He(null), b = He(null), D = He(null), j = He(null), R = He(null), H = ut(() => {
    n("close");
  }, [n]);
  Gt(!0, j, "field");
  const K = ut((I, L) => {
    L.preventDefault();
    const S = j.current;
    if (!S) return;
    const $ = S.getBoundingClientRect(), f = !k.current, M = k.current ?? { x: $.left, y: $.top };
    f && (k.current = M, y(M)), R.current = {
      dir: I,
      startX: L.clientX,
      startY: L.clientY,
      startW: $.width,
      startH: $.height,
      startPos: { ...M },
      symmetric: f
    };
    const Y = (Z) => {
      const F = R.current;
      if (!F) return;
      const te = Z.clientX - F.startX, se = Z.clientY - F.startY;
      let ae = F.startW, me = F.startH, ye = 0, we = 0;
      F.symmetric ? (F.dir.includes("e") && (ae = F.startW + 2 * te), F.dir.includes("w") && (ae = F.startW - 2 * te), F.dir.includes("s") && (me = F.startH + 2 * se), F.dir.includes("n") && (me = F.startH - 2 * se)) : (F.dir.includes("e") && (ae = F.startW + te), F.dir.includes("w") && (ae = F.startW - te, ye = te), F.dir.includes("s") && (me = F.startH + se), F.dir.includes("n") && (me = F.startH - se, we = se));
      const Te = Math.max(200, ae), Re = Math.max(100, me);
      F.symmetric ? (ye = (F.startW - Te) / 2, we = (F.startH - Re) / 2) : (F.dir.includes("w") && Te === 200 && (ye = F.startW - 200), F.dir.includes("n") && Re === 100 && (we = F.startH - 100)), b.current = Te, D.current = Re, g(Te), C(Re);
      const $e = {
        x: F.startPos.x + ye,
        y: F.startPos.y + we
      };
      k.current = $e, y($e);
    }, W = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", W);
      const Z = b.current, F = D.current;
      (Z != null || F != null) && n("resize", {
        ...Z != null ? { width: Math.round(Z) } : {},
        ...F != null ? { height: Math.round(F) } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", W);
  }, [n]), A = ut((I) => {
    if (I.button !== 0 || I.target.closest("button")) return;
    I.preventDefault();
    const L = j.current;
    if (!L) return;
    const S = L.getBoundingClientRect(), $ = k.current ?? { x: S.left, y: S.top }, f = I.clientX - $.x, M = I.clientY - $.y, Y = (Z) => {
      const F = window.innerWidth, te = window.innerHeight;
      let se = Z.clientX - f, ae = Z.clientY - M;
      const me = L.offsetWidth, ye = L.offsetHeight;
      se + me > F && (se = F - me), ae + ye > te && (ae = te - ye), se < 0 && (se = 0), ae < 0 && (ae = 0);
      const we = { x: se, y: ae };
      k.current = we, y(we);
    }, W = () => {
      document.removeEventListener("mousemove", Y), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", Y), document.addEventListener("mouseup", W);
  }, []), z = ut(() => {
    var I, L;
    if (x) {
      const S = w.current;
      S && (y(S.x !== -1 ? { x: S.x, y: S.y } : null), g(S.w), C(S.h)), E(!1);
    } else {
      const S = j.current, $ = S == null ? void 0 : S.getBoundingClientRect();
      w.current = {
        x: ((I = k.current) == null ? void 0 : I.x) ?? ($ == null ? void 0 : $.left) ?? -1,
        y: ((L = k.current) == null ? void 0 : L.y) ?? ($ == null ? void 0 : $.top) ?? -1,
        w: h ?? ($ == null ? void 0 : $.width) ?? null,
        h: N ?? null
      }, E(!0), y({ x: 0, y: 0 }), g(null), C(null);
    }
  }, [x, h, N]), B = x ? { position: "absolute", top: 0, left: 0, width: "100vw", maxWidth: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : u,
    ...N != null ? { height: N + "px" } : s != null ? { height: s } : {},
    ...i != null && N == null ? { minHeight: i } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, P = l + "-title";
  return /* @__PURE__ */ e.createElement(Yt, { modal: !0 }, /* @__PURE__ */ e.createElement(xa, { onClose: H }), /* @__PURE__ */ e.createElement(
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
        onDoubleClick: r ? z : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: P }, o),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(G, { control: m })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: z,
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
    (d.length > 0 || p) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p && /* @__PURE__ */ e.createElement(G, { control: p }), d.map((I, L) => /* @__PURE__ */ e.createElement(G, { key: L, control: I }))),
    r && !x && Ia.map((I) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: I,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${I}`,
        onMouseDown: (L) => K(I, L)
      }
    ))
  ));
}, { useCallback: Pa } = e, Ba = {
  "js.drawer.close": "Close"
}, Aa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(Ba), o = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, r = t.child, c = Pa(() => {
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
}, { useCallback: mt, useRef: Fa } = e, Oa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = Fa(null), o = t.child, s = (t.trigger ?? "contextmenu") === "click", i = mt((m) => {
    m.preventDefault(), m.stopPropagation(), n("openMenu", { x: m.clientX, y: m.clientY });
  }, [n]), r = mt(() => {
    var p;
    const m = (p = a.current) == null ? void 0 : p.getBoundingClientRect();
    n("openMenu", {
      x: Math.round(m ? m.left : 0),
      y: Math.round(m ? m.bottom : 0)
    });
  }, [n]), c = mt((m) => {
    m.preventDefault(), m.stopPropagation(), r();
  }, [r]), d = mt((m) => {
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
}, { useCallback: $a, useEffect: rn, useRef: Ha, useState: on } = e, Wa = 250, Ua = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.message ?? "", o = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, r = t.generation ?? 0, [c, d] = on(!1), [m, p] = on(!1), h = Ha(!1);
  rn(() => {
    h.current = !1;
  }, [r]);
  const g = $a(() => {
    d(!0), setTimeout(() => {
      n("dismiss", { generation: r }), d(!1);
    }, 200);
  }, [n, r]);
  return rn(() => {
    if (!i || s === 0 || m) return;
    const N = setTimeout(g, h.current ? Wa : s);
    return () => clearTimeout(N);
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
}, { useCallback: Va, useEffect: sn, useMemo: za, useRef: Ka, useState: Ya } = e, Ga = 1e3;
function Xa(l) {
  const t = Math.max(0, Math.floor(l / 1e3)), n = t % 60, a = Math.floor(t / 60) % 60, o = Math.floor(t / 3600), u = (s) => s < 10 ? `0${s}` : `${s}`;
  return o > 0 ? `${o}:${u(a)}:${u(n)}` : `${a}:${u(n)}`;
}
const qa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.visible === !0, o = t.severity ?? "info", u = t.text ?? "", s = t.deadline ?? null, i = t.serverNow ?? null, r = t.leadMs ?? null, c = t.actionLabel ?? null, d = t.pingGraceMs ?? null, m = za(
    () => i != null ? i - Date.now() : 0,
    [i]
  ), [p, h] = Ya(0), g = a && s != null;
  sn(() => {
    if (!g) return;
    const x = setInterval(() => h((E) => E + 1), Ga);
    return () => clearInterval(x);
  }, [g, s]);
  const N = Ka(null);
  sn(() => {
    !g || d == null || s == null || N.current !== s && (Date.now() + m < s + d || (N.current = s, n("deadlinePassed", {})));
  }, [p, g, s, d, m, n]);
  const C = Va(() => {
    c != null && n("action", {});
  }, [n, c]);
  if (!a) return null;
  const _ = s != null ? s - (Date.now() + m) : null;
  if (r != null && _ != null && _ > r) return null;
  const y = _ != null ? Xa(_) : null, k = c != null;
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlNoticeBar tlNoticeBar--${o}${k ? " tlNoticeBar--clickable" : ""}`,
      role: k ? "button" : "status",
      "aria-live": "polite",
      tabIndex: k ? 0 : void 0,
      title: c ?? void 0,
      "aria-label": k ? `${u} ${c}` : void 0,
      onClick: k ? C : void 0,
      onKeyDown: k ? (x) => {
        (x.key === "Enter" || x.key === " ") && (x.preventDefault(), C());
      } : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__text" }, u),
    y !== null && /* @__PURE__ */ e.createElement("span", { className: "tlNoticeBar__countdown" }, y)
  );
}, { useCallback: Rt, useEffect: cn, useRef: Za, useState: un } = e, Qa = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.open === !0, o = t.anchorId, u = t.anchorX, s = t.anchorY, i = t.items ?? [], r = Za(null), [c, d] = un({ top: 0, left: 0 }), [m, p] = un(0), h = i.filter((_) => _.type === "item" && !_.disabled);
  cn(() => {
    var b, D;
    if (!a) return;
    const _ = ((b = r.current) == null ? void 0 : b.offsetHeight) ?? 200, y = ((D = r.current) == null ? void 0 : D.offsetWidth) ?? 200;
    if (u != null && s != null) {
      let j = s, R = u;
      j + _ > window.innerHeight && (j = Math.max(0, window.innerHeight - _)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), d({ top: j, left: R }), p(0);
      return;
    }
    if (!o) return;
    const k = document.getElementById(o);
    if (!k) return;
    const x = k.getBoundingClientRect();
    let E = x.bottom + 4, w = x.left;
    E + _ > window.innerHeight && (E = x.top - _ - 4), w + y > window.innerWidth && (w = x.right - y), d({ top: E, left: w }), p(0);
  }, [a, o, u, s]);
  const g = Rt(() => {
    n("close");
  }, [n]), N = Rt((_) => {
    n("selectItem", { itemId: _ });
  }, [n]);
  cn(() => {
    if (!a) return;
    const _ = (y) => {
      r.current && !r.current.contains(y.target) && g();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [a, g]);
  const C = Rt((_) => {
    if (_.key === "Escape") {
      _.preventDefault(), g();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), p((y) => (y + 1) % h.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), p((y) => (y - 1 + h.length) % h.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const y = h[m];
      y && N(y.id);
    }
  }, [g, N, h, m]);
  return Gt(a, r), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: C
    },
    i.map((_, y) => {
      if (_.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const x = h.indexOf(_) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: _.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (_.disabled ? " tlMenu__item--disabled" : "") + (_.cssClasses ? " " + _.cssClasses : ""),
          role: "menuitem",
          disabled: _.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => N(_.id)
        },
        _.icon && /* @__PURE__ */ e.createElement(Ne, { encoded: _.icon, className: "tlMenu__icon" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, _.label)
      );
    })
  ) : null;
}, Ja = 768, er = ({ controlId: l }) => {
  const t = X(), n = ne(), a = yt(!0);
  e.useEffect(() => {
    const c = window.matchMedia(`(max-width: ${Ja}px)`), d = (p) => {
      n("reportDisplayClass", { displayClass: p ? "COMPACT" : "REGULAR" });
    };
    d(c.matches);
    const m = (p) => d(p.matches);
    return c.addEventListener("change", m), () => c.removeEventListener("change", m);
  }, [n]);
  const o = t.header, u = t.notices, s = t.content, i = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell " + a }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: o })), u && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__notices" }, /* @__PURE__ */ e.createElement(G, { control: u })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Pe, null, /* @__PURE__ */ e.createElement(G, { control: s }))), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement(G, { control: r }));
}, tr = ({ controlId: l }) => {
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
}, nr = ({ isMulti: l, cursorIndex: t, onMove: n, onToggle: a, onSelectAll: o }) => (pe("ArrowUp", () => (n("up", !1, !1), !0)), pe("ArrowDown", () => (n("down", !1, !1), !0)), pe("Home", () => (n("home", !1, !1), !0)), pe("End", () => (n("end", !1, !1), !0)), pe("PageUp", () => (n("pageUp", !1, !1), !0)), pe("PageDown", () => (n("pageDown", !1, !1), !0)), pe("Shift+ArrowUp", () => (n("up", l, !1), !0)), pe("Shift+ArrowDown", () => (n("down", l, !1), !0)), pe("Shift+Home", () => (n("home", l, !1), !0)), pe("Shift+End", () => (n("end", l, !1), !0)), pe("Shift+PageUp", () => (n("pageUp", l, !1), !0)), pe("Shift+PageDown", () => (n("pageDown", l, !1), !0)), pe("Ctrl+ArrowUp", () => (n("up", !1, l), !0)), pe("Ctrl+ArrowDown", () => (n("down", !1, l), !0)), pe("Space", () => t < 0 ? !1 : (a(), !0)), pe("Ctrl+A", () => l ? (o(), !0) : !1), null), lr = {
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
}, ar = 300, dn = 50, rr = 'input, textarea, select, button, a, [contenteditable="true"], [role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], [role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], [role="slider"], [role="menu"], [role="menuitem"]';
function Lt(l) {
  var n;
  const t = l.target;
  return !!((n = t == null ? void 0 : t.closest) != null && n.call(t, rr));
}
const Wt = 'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), select:not([disabled]), [contenteditable="true"]', or = Wt + ", button:not([disabled]), a[href]";
function kn(l, t) {
  return Array.from(l.querySelectorAll("[data-row][data-col]")).filter((n) => n.dataset.row === t);
}
function xt(l, t, n = {}) {
  const a = kn(l, t);
  if (n.col) {
    const u = a.find((i) => i.dataset.col === n.col), s = u == null ? void 0 : u.querySelector(Wt);
    if (s) return s;
  }
  if (n.col)
    return null;
  const o = n.last ? [...a].reverse() : a;
  for (const u of o) {
    const s = u.querySelector(Wt);
    if (s) return s;
  }
  return null;
}
const sr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(lr), o = e.useRef(null);
  e.useEffect(() => {
    const v = o.current;
    if (!v) return;
    const T = (V) => {
      const Q = V.detail;
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
  const u = t.columns ?? [], s = t.totalRowCount ?? 0, i = t.rows ?? [], r = t.rowHeight ?? 36, c = t.selectionMode ?? "single", d = t.selectedCount ?? 0, m = t.cursorIndex ?? -1, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = t.columnSelect ?? !1, N = t.filterBar ?? !1, C = t.namedFilters ?? [], _ = t.activeNamedFilter ?? "", y = t.search ?? "", k = t.filterSaving ?? !1, x = e.useMemo(
    () => u.filter((v) => v.sortPriority && v.sortPriority > 0).length,
    [u]
  ), E = c === "multi", w = 40, b = 20, D = e.useRef(null), j = e.useRef(null), R = e.useRef(null), H = e.useRef(null), K = e.useRef(null), [A, z] = e.useState({}), B = e.useRef(null), P = e.useRef(!1), I = e.useRef(null), [L, S] = e.useState(null), [$, f] = e.useState(null), [M, Y] = e.useState(null), [W, Z] = e.useState(0);
  e.useEffect(() => {
    const v = R.current;
    if (!v)
      return;
    const T = () => {
      const Q = v.offsetWidth - v.clientWidth;
      Z((ee) => ee === Q ? ee : Q);
    };
    T();
    const V = new ResizeObserver(T);
    return V.observe(v), () => V.disconnect();
  }, []), e.useEffect(() => {
    B.current || z({});
  }, [u]);
  const F = e.useCallback((v) => A[v.name] ?? v.width, [A]), te = e.useMemo(() => {
    const v = [];
    let T = E && p > 0 ? w : 0;
    for (let V = 0; V < p && V < u.length; V++)
      v.push(T), T += F(u[V]);
    return v;
  }, [u, p, E, w, F]), se = e.useMemo(() => {
    if (p <= 0)
      return 0;
    let v = E ? w : 0;
    for (let T = 0; T < p && T < u.length; T++)
      v += F(u[T]);
    return v;
  }, [u, p, E, w, F]), ae = s * r, me = e.useRef(null), ye = e.useCallback((v, T, V) => {
    V.preventDefault(), V.stopPropagation(), B.current = { column: v, startX: V.clientX, startWidth: T };
    let Q = V.clientX, ee = 0;
    const re = () => {
      const ce = B.current;
      if (!ce) return;
      const de = Math.max(dn, ce.startWidth + (Q - ce.startX) + ee);
      z((_e) => ({ ..._e, [ce.column]: de }));
    }, oe = () => {
      const ce = R.current, de = D.current;
      if (!ce || !B.current) return;
      const _e = ce.getBoundingClientRect(), xe = 40, Qt = 8, qn = ce.scrollLeft;
      Q > _e.right - xe ? ce.scrollLeft += Qt : Q < _e.left + xe && (ce.scrollLeft = Math.max(0, ce.scrollLeft - Qt));
      const Jt = ce.scrollLeft - qn;
      Jt !== 0 && (de && (de.scrollLeft = ce.scrollLeft), ee += Jt, re()), me.current = requestAnimationFrame(oe);
    };
    me.current = requestAnimationFrame(oe);
    const be = (ce) => {
      Q = ce.clientX, re();
    }, fe = (ce) => {
      document.removeEventListener("mousemove", be), document.removeEventListener("mouseup", fe), me.current !== null && (cancelAnimationFrame(me.current), me.current = null);
      const de = B.current;
      if (de) {
        const _e = Math.max(dn, de.startWidth + (ce.clientX - de.startX) + ee);
        n("columnResize", { column: de.column, width: _e }), B.current = null, P.current = !0, requestAnimationFrame(() => {
          P.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", be), document.addEventListener("mouseup", fe);
  }, [n]), we = e.useCallback(() => {
    D.current && R.current && (D.current.scrollLeft = R.current.scrollLeft), H.current !== null && clearTimeout(H.current), H.current = window.setTimeout(() => {
      const v = R.current;
      if (!v) return;
      const T = v.scrollTop, V = Math.ceil(v.clientHeight / r), Q = Math.floor(T / r);
      n("scroll", { start: Q, count: V });
    }, 80);
  }, [n, r]), Te = e.useCallback((v, T, V) => {
    if (P.current) return;
    let Q;
    !T || T === "desc" ? Q = "asc" : Q = "desc";
    const ee = V.shiftKey ? "add" : "replace";
    n("sort", { column: v, direction: Q, mode: ee });
  }, [n]), Re = e.useCallback((v, T) => {
    I.current = v, T.dataTransfer.effectAllowed = "move", T.dataTransfer.setData("text/plain", v);
  }, []), $e = e.useCallback((v, T) => {
    if (!I.current || I.current === v) {
      S(null);
      return;
    }
    T.preventDefault(), T.dataTransfer.dropEffect = "move";
    const V = T.currentTarget.getBoundingClientRect(), Q = T.clientX < V.left + V.width / 2 ? "left" : "right";
    S({ column: v, side: Q });
  }, []), Xe = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = I.current;
    if (!T || !L) {
      I.current = null, S(null);
      return;
    }
    let V = u.findIndex((ee) => ee.name === L.column);
    if (V < 0) {
      I.current = null, S(null);
      return;
    }
    const Q = u.findIndex((ee) => ee.name === T);
    L.side === "right" && V++, Q < V && V--, n("columnReorder", { column: T, targetIndex: V }), I.current = null, S(null);
  }, [u, L, n]), st = e.useCallback(() => {
    I.current = null, S(null);
  }, []), O = e.useCallback((v, T) => {
    var ee, re, oe, be;
    const V = window.getSelection();
    if (V && !V.isCollapsed && T.currentTarget.contains(V.anchorNode))
      return;
    if (!Lt(T) && ((ee = R.current) == null || ee.focus({ preventScroll: !0 }), !T.ctrlKey && !T.metaKey && !T.shiftKey)) {
      const fe = (be = (oe = (re = T.target) == null ? void 0 : re.closest) == null ? void 0 : oe.call(re, "[data-col]")) == null ? void 0 : be.getAttribute("data-col");
      K.current = { index: v, col: fe ?? void 0 };
    }
    const Q = i.find((fe) => fe.index === v);
    Lt(T) && (Q != null && Q.selected) && !T.ctrlKey && !T.metaKey && !T.shiftKey || n("select", {
      rowIndex: v,
      ctrlKey: T.ctrlKey || T.metaKey,
      shiftKey: T.shiftKey
    });
  }, [n, i]), q = e.useCallback((v, T, V) => {
    n("moveSelection", { direction: v, extend: T, move: V });
  }, [n]), le = e.useCallback(() => {
    m < 0 || n("select", { rowIndex: m, ctrlKey: E, shiftKey: !1 });
  }, [n, m, E]), ie = e.useCallback(() => {
    n("selectAll", { selected: !0 });
  }, [n]), qe = e.useCallback(
    () => !!o.current && o.current.contains(document.activeElement),
    []
  );
  e.useEffect(() => {
    if (m < 0)
      return;
    const v = R.current;
    if (!v)
      return;
    const T = m * r, V = T + r;
    T < v.scrollTop ? v.scrollTop = T : V > v.scrollTop + v.clientHeight && (v.scrollTop = V - v.clientHeight);
  }, [m, r]), e.useEffect(() => {
    const v = K.current, T = R.current;
    if (!v || !T)
      return;
    const V = i.find((re) => re.index === v.index);
    if (!V || !xt(T, V.id))
      return;
    K.current = null;
    const Q = document.activeElement;
    if (Q && Q !== document.body && !T.contains(Q))
      return;
    const ee = xt(T, V.id, { col: v.col, last: v.last });
    ee && (ee.focus({ preventScroll: !0 }), ee instanceof HTMLInputElement && ee.select());
  }, [i]);
  const Mn = e.useCallback((v) => {
    if (v.key !== "Tab")
      return;
    const T = R.current, V = document.activeElement;
    if (!T || !V || !T.contains(V))
      return;
    const Q = V.closest("[data-row][data-col]");
    if (!Q)
      return;
    const ee = Q.dataset.row, re = i.find((xe) => xe.id === ee);
    if (!re)
      return;
    const oe = kn(T, ee).flatMap((xe) => Array.from(xe.querySelectorAll(or))), be = oe.indexOf(V);
    if (be < 0)
      return;
    const fe = !v.shiftKey;
    if (!(fe ? be === oe.length - 1 : be === 0))
      return;
    const de = fe ? re.index + 1 : re.index - 1;
    if (de < 0 || de >= s)
      return;
    const _e = i.find((xe) => xe.index === de);
    _e && xt(T, _e.id) || (v.preventDefault(), K.current = { index: de, last: !fe }, n("select", { rowIndex: de, ctrlKey: !1, shiftKey: !1 }));
  }, [i, s, n]), In = e.useCallback((v, T) => {
    T.stopPropagation(), n("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [n]), jn = e.useCallback(() => {
    const v = d === s && s > 0;
    n("selectAll", { selected: !v });
  }, [n, d, s]), Pn = e.useCallback((v, T, V) => {
    V.stopPropagation(), n("expand", { rowIndex: v, expanded: T });
  }, [n]), Bn = e.useCallback((v, T) => {
    T.preventDefault(), f({ x: T.clientX, y: T.clientY, colIdx: v });
  }, []), An = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), f(null));
  }, [$, n]), Fn = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), f(null);
  }, [n]), On = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation();
    const T = j.current, V = D.current;
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
    }, be = () => {
      document.removeEventListener("mousemove", oe), document.removeEventListener("mouseup", be), Y(null), re.count !== p && n("setFrozenColumnCount", { count: re.count });
    };
    document.addEventListener("mousemove", oe), document.addEventListener("mouseup", be);
  }, [se, p, n]);
  e.useEffect(() => {
    if (!$) return;
    const v = () => f(null);
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [$]), Oe(!!$, { ESCAPE: () => f(null) });
  const $n = e.useCallback((v, T) => {
    T.stopPropagation(), T.preventDefault(), n("openFilter", { column: v });
  }, [n]), Hn = e.useCallback((v) => {
    v.stopPropagation(), v.preventDefault(), n("openColumnSelect", {});
  }, [n]), [Wn, qt] = e.useState(y), wt = e.useRef(!1), Le = e.useRef(null);
  e.useEffect(() => {
    wt.current || qt(y);
  }, [y]), e.useEffect(() => () => {
    Le.current !== null && clearTimeout(Le.current);
  }, []);
  const ct = e.useCallback((v) => {
    Le.current !== null && (clearTimeout(Le.current), Le.current = null), wt.current = !1, n("search", { term: v });
  }, [n]), Un = e.useCallback((v) => {
    qt(v), wt.current = !0, Le.current !== null && clearTimeout(Le.current), Le.current = window.setTimeout(() => ct(v), ar);
  }, [ct]), Vn = e.useCallback((v) => {
    v.key === "Enter" && (v.preventDefault(), ct(v.currentTarget.value));
  }, [ct]), zn = e.useCallback((v) => {
    v === _ ? n("clearFilter", {}) : n("applyNamedFilter", { id: v });
  }, [_, n]), Kn = e.useCallback((v, T) => {
    T.stopPropagation(), n("deleteNamedFilter", { id: v });
  }, [n]), [Ze, Qe] = e.useState(null), kt = e.useCallback(() => {
    const v = (Ze ?? "").trim();
    v && (n("saveNamedFilter", { filterName: v }), Qe(null));
  }, [Ze, n]), Yn = e.useCallback((v) => {
    v.key === "Enter" ? (v.preventDefault(), kt()) : v.key === "Escape" && (v.preventDefault(), Qe(null));
  }, [kt]), Nt = u.reduce((v, T) => v + F(T), 0) + (E ? w : 0), St = g ? 32 : 0, Gn = d === s && s > 0, Zt = d > 0 && d < s, Xn = e.useCallback((v) => {
    v && (v.indeterminate = Zt);
  }, [Zt]);
  return /* @__PURE__ */ e.createElement(Yt, { active: qe }, /* @__PURE__ */ e.createElement(
    nr,
    {
      isMulti: E,
      cursorIndex: m,
      onMove: q,
      onToggle: le,
      onSelectAll: ie
    }
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (v) => {
        if (!I.current) return;
        v.preventDefault();
        const T = R.current, V = D.current;
        if (!T) return;
        const Q = T.getBoundingClientRect(), ee = 40, re = 8;
        v.clientX < Q.left + ee ? T.scrollLeft = Math.max(0, T.scrollLeft - re) : v.clientX > Q.right - ee && (T.scrollLeft += re), V && (V.scrollLeft = T.scrollLeft);
      },
      onDrop: Xe
    },
    N && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterBar" }, C.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlTableView__filterChips" }, C.map((v) => {
      const T = v.id === _;
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
            onClick: () => zn(v.id)
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
            onClick: (V) => Kn(v.id, V)
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
        value: Wn,
        onChange: (v) => Un(v.target.value),
        onKeyDown: Vn
      }
    )), k && (Ze === null ? /* @__PURE__ */ e.createElement(
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
        onChange: (v) => Qe(v.target.value),
        onKeyDown: Yn
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
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerArea", ref: j }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: D }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerRow",
        style: { width: Nt, paddingRight: St + W }
      },
      E && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
          style: {
            width: w,
            minWidth: w,
            ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
          },
          onDragOver: (v) => {
            I.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", u.length > 0 && u[0].name !== I.current && S({ column: u[0].name, side: "left" }));
          }
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            ref: Xn,
            className: "tlTableView__checkbox",
            checked: Gn,
            onChange: jn
          }
        )
      ),
      u.map((v, T) => {
        const V = F(v);
        u.length - 1;
        let Q = "tlTableView__headerCell";
        v.sortable && (Q += " tlTableView__headerCell--sortable"), L && L.column === v.name && (Q += " tlTableView__headerCell--dragOver-" + L.side);
        const ee = T < p, re = T === p - 1;
        return ee && (Q += " tlTableView__headerCell--frozen"), re && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
          "div",
          {
            key: v.name,
            className: Q,
            "data-col-idx": T,
            style: {
              width: V,
              minWidth: V,
              position: ee ? "sticky" : "relative",
              ...ee ? { left: te[T], zIndex: 2 } : {}
            },
            draggable: !0,
            onClick: v.sortable ? (oe) => Te(v.name, v.sortDirection, oe) : void 0,
            onContextMenu: (oe) => Bn(T, oe),
            onDragStart: (oe) => Re(v.name, oe),
            onDragOver: (oe) => $e(v.name, oe),
            onDrop: Xe,
            onDragEnd: st
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
              onClick: (oe) => $n(v.name, oe)
            },
            /* @__PURE__ */ e.createElement("i", { className: v.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
          ),
          v.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, v.sortDirection === "asc" ? "▲" : "▼", x > 1 && v.sortPriority != null && v.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, v.sortPriority)),
          /* @__PURE__ */ e.createElement(
            "div",
            {
              className: "tlTableView__resizeHandle",
              onMouseDown: (oe) => ye(v.name, V, oe)
            }
          )
        );
      }),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          style: { flex: "0 0 0", minHeight: "100%" },
          onDragOver: (v) => {
            if (I.current && u.length > 0) {
              const T = u[u.length - 1];
              T.name !== I.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", S({ column: T.name, side: "right" }));
            }
          },
          onDrop: Xe
        }
      )
    )), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__frozenSplitter" + (M ? " tlTableView__frozenSplitter--active" : ""),
        style: { left: se },
        title: a["js.table.freezeSplitter"],
        onMouseDown: On
      }
    ), g && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlTableView__columnsButton",
        title: a["js.table.columns"],
        "aria-label": a["js.table.columns"],
        onClick: Hn
      },
      /* @__PURE__ */ e.createElement("i", { className: "bi bi-gear" })
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: R,
        className: "tlTableView__body",
        onScroll: we,
        onKeyDown: Mn,
        tabIndex: 0
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ae, position: "relative", width: Nt, paddingRight: St } }, i.map((v) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: "tlTableView__row" + (v.selected ? " tlTableView__row--selected" : "") + (v.index === m ? " tlTableView__row--cursor" : ""),
          style: {
            position: "absolute",
            top: v.index * r,
            height: r,
            width: Nt,
            paddingRight: St,
            ...v.index === m ? { outline: "2px solid var(--color-primary, #1a73e8)", outlineOffset: "-2px" } : {}
          },
          onMouseDown: (T) => {
            (T.shiftKey || T.ctrlKey || T.metaKey || T.detail > 1) && !Lt(T) && T.preventDefault();
          },
          onClick: (T) => O(v.index, T)
        },
        E && /* @__PURE__ */ e.createElement(
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
              checked: v.selected,
              onChange: () => {
              },
              onClick: (T) => In(v.index, T),
              tabIndex: -1
            }
          )
        ),
        u.map((T, V) => {
          const Q = F(T), ee = V === u.length - 1, re = V < p, oe = V === p - 1;
          let be = "tlTableView__cell";
          re && (be += " tlTableView__cell--frozen"), oe && (be += " tlTableView__cell--frozenLast");
          const fe = h && V === 0, ce = v.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: T.name,
              className: be,
              "data-row": v.id,
              "data-col": T.name,
              style: {
                ...ee && !re ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...re ? { position: "sticky", left: te[V], zIndex: 2 } : {}
              }
            },
            fe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * b } }, v.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => Pn(v.index, !v.expanded, de)
              },
              v.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), v.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: v.cells[T.name] })) : v.cells[T.name] && /* @__PURE__ */ e.createElement(G, { control: v.cells[T.name] })
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
      $.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: An }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Fn }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  ));
}, cr = {
  "js.table.columnSearch": "Find column"
}, ir = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(cr), o = t.entries ?? [], u = o.filter((E) => E.visible).length, [s, i] = e.useState(""), r = s.trim().toLowerCase(), c = r ? o.filter((E) => E.label.toLowerCase().includes(r)) : o, d = e.useRef(null), m = e.useRef(null), [p, h] = e.useState(null), g = e.useCallback((E) => {
    m.current = E, h(E);
  }, []), N = e.useCallback((E, w) => {
    n("columnVisible", { column: E, visible: w });
  }, [n]), C = e.useCallback((E, w) => {
    d.current = E, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", E);
  }, []), _ = e.useCallback((E, w) => {
    if (!d.current || d.current === E) {
      g(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const b = w.currentTarget.getBoundingClientRect(), D = w.clientY < b.top + b.height / 2 ? "top" : "bottom";
    g({ name: E, side: D });
  }, [g]), y = e.useCallback(() => {
    d.current = null, g(null);
  }, [g]), k = e.useCallback((E) => {
    E.preventDefault();
    const w = d.current, b = m.current;
    if (d.current = null, g(null), !w || !b)
      return;
    const D = o.findIndex((H) => H.name === b.name), j = o.findIndex((H) => H.name === w);
    if (D < 0 || j < 0)
      return;
    let R = b.side === "top" ? D : D + 1;
    j < R && R--, R !== j && n("columnReorder", { column: w, targetIndex: R });
  }, [o, n, g]), x = o.length > 10;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlColumnSelect", onDrop: k }, x && /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__search" }, /* @__PURE__ */ e.createElement("i", { className: "bi bi-search", "aria-hidden": "true" }), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "search",
      className: "tlColumnSelect__searchInput",
      placeholder: a["js.table.columnSearch"],
      "aria-label": a["js.table.columnSearch"],
      value: s,
      onChange: (E) => i(E.target.value)
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColumnSelect__list" + (x ? " tlColumnSelect__list--fixed" : "") }, c.map((E) => {
    const w = E.visible && u <= 1;
    let b = "tlColumnSelect__row";
    return p && p.name === E.name && (b += " tlColumnSelect__row--dragOver-" + p.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E.name,
        className: b,
        draggable: !0,
        onDragStart: (D) => C(E.name, D),
        onDragOver: (D) => _(E.name, D),
        onDrop: k,
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
          onChange: (D) => N(E.name, D.target.checked)
        }
      ), /* @__PURE__ */ e.createElement("span", null, E.label))
    );
  })));
}, { useState: Ut, useRef: lt, useCallback: gt, useMemo: Ae, useEffect: mn } = e, ur = {
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
}, ve = 44, vt = 15, Ce = 6e4, dr = 36e5, je = 864e5, mr = 8;
function Se(l) {
  const t = new Date(l);
  return t.setHours(0, 0, 0, 0), t.getTime();
}
function ze(l, t) {
  const n = new Date(l);
  return n.setDate(n.getDate() + t), n.getTime();
}
function pr(l) {
  return Se(l);
}
function at(l, t) {
  return Se(l) === Se(t);
}
function Ie(l) {
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
  return "tlCalEvent--c" + Math.abs(t) % mr;
}
function Ct(l, t) {
  return l.color ? { ...t, "--cal-ev-bg": l.color } : t;
}
function fr(l) {
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
function hr(l, t) {
  const n = { hour: "numeric", minute: "2-digit" };
  return Fe(l, n, t.start) + "–" + Fe(l, n, t.end);
}
const br = [
  { key: "DAY", label: "js.calendar.day" },
  { key: "WORK_WEEK", label: "js.calendar.workWeek" },
  { key: "WEEK", label: "js.calendar.week" },
  { key: "MONTH", label: "js.calendar.month" },
  { key: "YEAR", label: "js.calendar.year" }
], gr = ({ title: l, granularity: t, i18n: n, send: a }) => /* @__PURE__ */ e.createElement("div", { className: "tlCalToolbar" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalNav" }, /* @__PURE__ */ e.createElement("button", { className: "tlCalBtn", onClick: () => a("navigate", { direction: "TODAY" }) }, n["js.calendar.today"]), /* @__PURE__ */ e.createElement(
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
)), /* @__PURE__ */ e.createElement("div", { className: "tlCalTitle" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCalGranularity" }, br.map((o) => /* @__PURE__ */ e.createElement(
  "button",
  {
    key: o.key,
    className: "tlCalBtn" + (o.key === t ? " tlCalBtn--active" : ""),
    onClick: () => a("switchGranularity", { granularity: o.key })
  },
  n[o.label]
))));
function Er(l) {
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
      topMin: Ie(s.start),
      botMin: Ie(s.start) + Math.max(15, (s.end - s.start) / Ce),
      col: r,
      cols: 1
    }), o = Math.max(o, s.end);
  }
  return a.length > 0 && u(), n;
}
const Mt = (l) => {
  l.preventDefault(), l.currentTarget.setPointerCapture(l.pointerId);
}, Vt = ({ className: l, placeholder: t, style: n, onCommit: a, onDiscard: o }) => {
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
  const [t, n] = Ut(null), a = lt(null);
  a.current = t;
  const o = gt((i) => n(i), []), u = gt(() => n(null), []), s = gt(
    (i) => {
      const r = a.current;
      r && l("createSlot", { ...r, title: i }), n(null);
    },
    [l]
  );
  return { pending: t, open: o, commit: s, discard: u };
}, vr = ({
  ctx: l,
  rangeStart: t,
  granularity: n
}) => {
  const { events: a, locale: o, nonWorkingDays: u, dayStartHour: s, dayEndHour: i, now: r, send: c, editable: d, i18n: m } = l, p = Ae(() => {
    const B = n === "DAY" ? 1 : 7, P = [];
    for (let I = 0; I < B; I++) {
      const L = ze(t, I);
      n === "WORK_WEEK" && u.includes(new Date(L).getDay()) || P.push(L);
    }
    return P;
  }, [t, n, u]), h = Nn(c), g = lt(null), N = lt(null), [C, _] = Ut(null), y = lt(null);
  y.current = C;
  const [k, x] = Ut(Date.now());
  mn(() => {
    const B = window.setInterval(() => x(Date.now()), 6e4);
    return () => window.clearInterval(B);
  }, []);
  const E = gt(
    (B, P) => {
      const I = g.current;
      if (!I)
        return { dayIndex: 0, min: 0 };
      const L = I.getBoundingClientRect(), S = L.width / p.length, $ = et(Math.floor((B - L.left) / S), 0, p.length - 1), f = P - L.top + I.scrollTop, M = et(f / ve * 60, 0, 1440);
      return { dayIndex: $, min: M };
    },
    [p.length]
  );
  mn(() => {
    if (!C)
      return;
    const B = (L) => {
      const S = y.current;
      if (!S)
        return;
      const { dayIndex: $, min: f } = E(L.clientX, L.clientY);
      S.mode === "move" ? _({ ...S, dayStart: p[$], startMin: et(Je(f - S.grabMin), 0, 1440 - S.dur) }) : S.mode === "resize" ? _({ ...S, endMin: et(Je(f), S.startMin + vt, 1440) }) : _({ ...S, toMin: et(Je(f), 0, 1440) });
    }, P = () => {
      const L = y.current;
      if (_(null), !!L)
        if (L.mode === "move") {
          const S = L.dayStart + L.startMin * Ce;
          S !== L.origStartMs && c("moveEvent", { eventId: L.id, start: S, end: S + L.dur * Ce });
        } else if (L.mode === "resize") {
          const S = L.dayStart + L.endMin * Ce;
          S !== L.origEndMs && c("resizeEvent", { eventId: L.id, end: S });
        } else {
          const S = Math.min(L.fromMin, L.toMin), $ = Math.max(L.fromMin, L.toMin);
          $ - S >= vt && h.open({ start: L.dayStart + S * Ce, end: L.dayStart + $ * Ce, allDay: !1 });
        }
    }, I = () => _(null);
    return window.addEventListener("pointermove", B), window.addEventListener("pointerup", P, { once: !0 }), window.addEventListener("pointercancel", I), () => {
      window.removeEventListener("pointermove", B), window.removeEventListener("pointerup", P), window.removeEventListener("pointercancel", I);
    };
  }, [C, p, E, c, h.open]);
  const w = (B, P, I) => {
    if (!d || !P.movable)
      return;
    B.stopPropagation(), Mt(B), h.discard();
    const { min: L } = E(B.clientX, B.clientY), S = (P.end - P.start) / Ce;
    _({
      mode: "move",
      id: P.id,
      grabMin: L - Ie(P.start),
      dur: S,
      dayStart: I,
      startMin: Ie(P.start),
      origStartMs: P.start
    });
  }, b = (B, P, I) => {
    !d || !P.resizable || (B.stopPropagation(), Mt(B), h.discard(), _({
      mode: "resize",
      id: P.id,
      dayStart: I,
      startMin: Ie(P.start),
      endMin: Ie(P.end),
      origEndMs: P.end
    }));
  }, D = (B, P) => {
    if (!d || B.button !== 0)
      return;
    Mt(B), h.discard();
    const { min: I } = E(B.clientX, B.clientY);
    _({ mode: "create", dayStart: P, fromMin: Je(I), toMin: Je(I) });
  }, j = Array.from({ length: 24 }, (B, P) => P), R = Ae(() => {
    if (C === null || !("id" in C))
      return a;
    const B = C;
    return a.map((P) => {
      if (P.id !== B.id)
        return P;
      if (B.mode === "move") {
        const I = B.dayStart + B.startMin * Ce;
        return { ...P, start: I, end: I + B.dur * Ce };
      }
      return { ...P, end: B.dayStart + B.endMin * Ce };
    });
  }, [a, C]), H = Ae(() => p.map(
    (B) => Er(
      R.filter((P) => !P.allDay && P.start < B + je && P.end > B)
    )
  ), [p, R]), K = Ae(() => p.map((B) => R.filter((P) => P.allDay && P.start < B + je && P.end > B)), [p, R]), A = s * ve, z = i * ve;
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeGrid" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeHeader" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalGutter" }), p.map((B) => {
    const P = u.includes(new Date(B).getDay()), I = at(B, l.now);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalDayHead" + (P ? " tlCalDayHead--nonworking" : "") + (I ? " tlCalDayHead--today" : ""),
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
      Vt,
      {
        className: "tlCalAllDayEvent tlCalEvent--preview",
        placeholder: m["js.calendar.newEventTitle"],
        onCommit: h.commit,
        onDiscard: h.discard
      }
    ),
    K[P].map((I) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: I.id,
        className: "tlCalAllDayEvent " + _t(I.category) + (I.selected ? " tlCalEvent--selected" : ""),
        style: Ct(I),
        title: I.tooltip,
        onClick: (L) => {
          L.stopPropagation(), c("selectEvent", { eventId: I.id });
        }
      },
      I.title
    ))
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlCalScroll", ref: N }, /* @__PURE__ */ e.createElement("div", { className: "tlCalTimeBody", style: { height: 24 * ve } }, /* @__PURE__ */ e.createElement("div", { className: "tlCalHourAxis" }, j.map((B) => /* @__PURE__ */ e.createElement("div", { key: B, className: "tlCalHourLabel", style: { top: B * ve } }, B === 0 ? "" : Fe(o, { hour: "numeric" }, Se(t) + B * dr)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalColumns", ref: g, style: { gridTemplateColumns: `repeat(${p.length}, 1fr)` } }, p.map((B, P) => {
    const I = u.includes(new Date(B).getDay()), L = C && ("dayStart" in C && C.dayStart === B) ? C : null;
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: B,
        className: "tlCalCol" + (I ? " tlCalCol--nonworking" : ""),
        onPointerDown: (S) => D(S, B)
      },
      j.map((S) => /* @__PURE__ */ e.createElement("div", { key: S, className: "tlCalHourLine", style: { top: S * ve } })),
      /* @__PURE__ */ e.createElement("div", { className: "tlCalWorkBand", style: { top: A, height: z - A } }),
      at(B, k) && /* @__PURE__ */ e.createElement("div", { className: "tlCalNowLine", style: { top: Ie(Date.now()) / 60 * ve } }),
      H[P].map((S) => {
        const $ = C !== null && "id" in C && C.id === S.ev.id, f = S.topMin / 60 * ve, M = (S.botMin - S.topMin) / 60 * ve, Y = 100 / S.cols;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: S.ev.id,
            className: "tlCalEvent " + _t(S.ev.category) + (S.ev.selected ? " tlCalEvent--selected" : "") + ($ ? " tlCalEvent--dragging" : ""),
            style: Ct(S.ev, {
              top: f,
              height: M,
              left: `${S.col * Y}%`,
              width: `calc(${Y}% - 2px)`
            }),
            title: S.ev.tooltip,
            onPointerDown: (W) => w(W, S.ev, B),
            onClick: (W) => {
              W.stopPropagation(), c("selectEvent", { eventId: S.ev.id });
            }
          },
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTime" }, hr(o, S.ev)),
          /* @__PURE__ */ e.createElement("span", { className: "tlCalEventTitle" }, S.ev.title),
          d && S.ev.resizable && /* @__PURE__ */ e.createElement("span", { className: "tlCalResizeHandle", onPointerDown: (W) => b(W, S.ev, B) })
        );
      }),
      h.pending && !h.pending.allDay && Se(h.pending.start) === B && /* @__PURE__ */ e.createElement(
        Vt,
        {
          className: "tlCalEvent tlCalEvent--preview",
          placeholder: m["js.calendar.newEventTitle"],
          style: {
            top: Ie(h.pending.start) / 60 * ve,
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
}, _r = 3, Cr = ({ ctx: l, rangeStart: t, anchorMonth: n }) => {
  const { events: a, locale: o, nonWorkingDays: u, send: s, editable: i, now: r, i18n: c } = l, d = Nn(s), m = Ae(() => {
    const h = [];
    for (let g = 0; g < 6; g++) {
      const N = [];
      for (let C = 0; C < 7; C++)
        N.push(ze(t, g * 7 + C));
      h.push(N);
    }
    return h;
  }, [t]), p = (h, g) => {
    h.preventDefault();
    const N = h.dataTransfer.getData("text/plain"), C = a.find((y) => y.id === N);
    if (!C || !i || !C.movable)
      return;
    const _ = g - Se(C.start);
    s("moveEvent", { eventId: N, start: C.start + _, end: C.end + _ });
  };
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalMonth" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthHead" }, m[0].map((h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlCalMonthWeekday" }, Fe(o, { weekday: "short" }, h)))), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthBody" }, m.map((h, g) => {
    const N = h[0], C = ze(N, 7), _ = a.filter((k) => (k.allDay || k.end - k.start >= je) && k.start < C && k.end > N).sort((k, x) => k.start - x.start).slice(0, 3), y = _.length;
    return /* @__PURE__ */ e.createElement("div", { key: g, className: "tlCalMonthWeek" }, /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthDays" }, h.map((k) => {
      const x = new Date(k).getMonth() === new Date(n).getMonth(), E = u.includes(new Date(k).getDay()), w = at(k, r);
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k,
          className: "tlCalMonthCell" + (x ? "" : " tlCalMonthCell--other") + (E ? " tlCalMonthCell--nonworking" : ""),
          onDragOver: (b) => b.preventDefault(),
          onDrop: (b) => p(b, k),
          onClick: () => i && d.open({ start: k, end: k + je, allDay: !0 })
        },
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlCalMonthDayNum" + (w ? " tlCalMonthDayNum--today" : ""),
            onClick: (b) => {
              b.stopPropagation(), s("goto", { date: k, granularity: "DAY" });
            }
          },
          new Date(k).getDate()
        ),
        d.pending && d.pending.start === k && /* @__PURE__ */ e.createElement(
          Vt,
          {
            className: "tlCalMonthBar tlCalEvent--preview tlCalMonthCreate",
            placeholder: c["js.calendar.newEventTitle"],
            onCommit: d.commit,
            onDiscard: d.discard
          }
        )
      );
    })), /* @__PURE__ */ e.createElement("div", { className: "tlCalMonthOverlay" }, _.map((k, x) => {
      const E = Math.max(0, Math.floor((Se(Math.max(k.start, N)) - N) / je)), w = Math.min(7, Math.ceil((k.end - N) / je));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlCalMonthBar " + _t(k.category) + (k.selected ? " tlCalEvent--selected" : ""),
          style: Ct(k, {
            gridColumn: `${E + 1} / ${Math.max(E + 1, w) + 1}`,
            gridRow: x + 1
          }),
          draggable: i && k.movable,
          onDragStart: (b) => b.dataTransfer.setData("text/plain", k.id),
          title: k.tooltip,
          onClick: (b) => {
            b.stopPropagation(), s("selectEvent", { eventId: k.id });
          }
        },
        k.title
      );
    }), h.map((k, x) => {
      const E = a.filter((D) => !D.allDay && D.end - D.start < je && at(D.start, k)).sort((D, j) => D.start - j.start), w = E.slice(0, _r), b = E.length - w.length;
      return w.map((D, j) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: D.id,
          className: "tlCalChip " + _t(D.category) + (D.selected ? " tlCalEvent--selected" : ""),
          style: Ct(D, { gridColumn: x + 1, gridRow: y + 1 + j }),
          draggable: i && D.movable,
          onDragStart: (R) => R.dataTransfer.setData("text/plain", D.id),
          title: D.tooltip,
          onClick: (R) => {
            R.stopPropagation(), s("selectEvent", { eventId: D.id });
          }
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipDot" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTime" }, Fe(o, { hour: "numeric", minute: "2-digit" }, D.start)),
        /* @__PURE__ */ e.createElement("span", { className: "tlCalChipTitle" }, D.title)
      )).concat(
        b > 0 ? [
          /* @__PURE__ */ e.createElement(
            "div",
            {
              key: "more-" + k,
              className: "tlCalMore",
              style: { gridColumn: x + 1, gridRow: y + 1 + w.length },
              onClick: () => s("goto", { date: k, granularity: "DAY" })
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
}, yr = ({ ctx: l, rangeStart: t }) => {
  const { events: n, locale: a, firstDayOfWeek: o, nonWorkingDays: u, send: s, now: i } = l, r = Ae(() => {
    const p = /* @__PURE__ */ new Set();
    for (const h of n) {
      let g = Se(h.start);
      const N = h.end;
      for (; g < N; )
        p.add(g), g = ze(g, 1);
    }
    return p;
  }, [n]), c = new Date(t).getFullYear(), d = Array.from({ length: 12 }, (p, h) => new Date(c, h, 1).getTime()), m = Ae(() => {
    const p = new Date(2023, 0, 1);
    return Array.from({ length: 7 }, (h, g) => {
      const N = new Date(p);
      return N.setDate(p.getDate() + (o + g) % 7), new Intl.DateTimeFormat(a, { weekday: "narrow" }).format(N);
    });
  }, [a, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlCalYear" }, d.map((p) => {
    const h = new Date(p), g = Se(ze(p, -((h.getDay() - o + 7) % 7))), N = Array.from({ length: 42 }, (C, _) => ze(g, _));
    return /* @__PURE__ */ e.createElement("div", { key: p, className: "tlCalMini" }, /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlCalMiniTitle",
        onClick: () => s("goto", { date: p, granularity: "MONTH" })
      },
      Fe(a, { month: "long" }, p)
    ), /* @__PURE__ */ e.createElement("div", { className: "tlCalMiniGrid" }, m.map((C, _) => /* @__PURE__ */ e.createElement("div", { key: "h" + _, className: "tlCalMiniWd" }, C)), N.map((C) => {
      const _ = new Date(C).getMonth() === h.getMonth(), y = u.includes(new Date(C).getDay()), k = at(C, i), x = r.has(pr(C));
      return /* @__PURE__ */ e.createElement(
        "div",
        {
          key: C,
          className: "tlCalMiniDay" + (_ ? "" : " tlCalMiniDay--other") + (y ? " tlCalMiniDay--nonworking" : "") + (k ? " tlCalMiniDay--today" : "") + (x ? " tlCalMiniDay--event" : ""),
          onClick: () => s("goto", { date: C, granularity: "DAY" })
        },
        new Date(C).getDate()
      );
    })));
  }));
}, wr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(ur), o = t.granularity ?? "WEEK", u = t.rangeStart ?? Date.now(), s = t.anchor ?? u, i = t.title ?? "", r = {
    locale: t.locale ?? "en",
    firstDayOfWeek: t.firstDayOfWeek ?? 0,
    nonWorkingDays: t.nonWorkingDays ?? [0, 6],
    dayStartHour: t.dayStartHour ?? 8,
    dayEndHour: t.dayEndHour ?? 18,
    editable: t.editable !== !1,
    now: t.now ?? Date.now(),
    events: fr(t.events),
    send: n,
    i18n: a
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCalendar" }, /* @__PURE__ */ e.createElement(gr, { title: i, granularity: o, i18n: a, send: n }), /* @__PURE__ */ e.createElement("div", { className: "tlCalBody" }, o === "MONTH" ? /* @__PURE__ */ e.createElement(Cr, { ctx: r, rangeStart: u, anchorMonth: s }) : o === "YEAR" ? /* @__PURE__ */ e.createElement(yr, { ctx: r, rangeStart: u }) : /* @__PURE__ */ e.createElement(vr, { ctx: r, rangeStart: u, granularity: o })));
}, kr = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Sn = e.createContext(kr), { useMemo: Nr, useRef: Sr, useState: Dr, useEffect: Tr } = e, Rr = 320, Lr = "TLTableView", xr = "TLPanel", Mr = ({ controlId: l }) => {
  var C;
  const t = X(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", o = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Sr(null), [r, c] = Dr(
    a === "top" ? "top" : "side"
  );
  Tr(() => {
    if (a !== "auto") {
      c(a);
      return;
    }
    const _ = i.current;
    if (!_) return;
    const y = new ResizeObserver((k) => {
      for (const x of k) {
        const w = x.contentRect.width / n;
        c(w < Rr ? "top" : "side");
      }
    });
    return y.observe(_), () => y.disconnect();
  }, [a, n]);
  const d = Nr(() => ({
    readOnly: o,
    resolvedLabelPosition: r
  }), [o, r]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, h = u.length === 1 ? u[0] : void 0, g = !!h && (h.module === Lr || h.module === xr && ((C = h.state) == null ? void 0 : C.bare) === !0), N = [
    "tlFormLayout",
    o ? "tlFormLayout--readonly" : "",
    g ? "tlFormLayout--flush" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Sn.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: p, ref: i }, u.map((_, y) => /* @__PURE__ */ e.createElement(G, { key: y, control: _ }))));
}, { useCallback: Ir } = e, jr = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Pr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = ue(jr), o = t.headerControl ?? null, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, r = t.border ?? "none", c = t.fullLine === !0, d = t.children ?? [], m = o != null || u.length > 0 || s, p = Ir(() => {
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
  ), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: o })), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((g, N) => /* @__PURE__ */ e.createElement(G, { key: N, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, d.map((g, N) => /* @__PURE__ */ e.createElement(G, { key: N, control: g }))));
}, { useContext: Br, useState: Ar, useCallback: Fr } = e, Or = ({ controlId: l }) => {
  const t = X(), n = Br(Sn), a = t.label ?? "", o = t.required === !0, u = t.error, s = t.errorIcon, i = t.warnings, r = t.warningIcon, c = t.helpText, d = t.dirty === !0, m = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, g = t.hasTooltip === !0, N = t.field, C = n.readOnly, [_, y] = Ar(!1), k = Fr(() => y((D) => !D), []), x = m === "hidden", E = u != null, w = i != null && i.length > 0, b = [
    "tlFormField",
    `tlFormField--${m}`,
    C ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    E ? "tlFormField--error" : "",
    !E && w ? "tlFormField--warning" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: b, style: h ? void 0 : { display: "none" } }, !x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": g ? "key:tooltip" : void 0
    },
    a
  ), o && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), c && !C && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: k,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: N })), !C && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(Ot, { image: s, className: "tlFormField__errorIcon" }), /* @__PURE__ */ e.createElement("span", null, u)), !C && !E && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, i.map((D, j) => /* @__PURE__ */ e.createElement("div", { key: j, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(Ot, { image: r, className: "tlFormField__warningIcon" }), /* @__PURE__ */ e.createElement("span", null, D)))), !C && c && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, c));
}, $r = "goto", Hr = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.iconCss, o = t.iconSrc, u = t.label, s = t.cssClass, i = t.hasTooltip === !0, r = t.hasLink, c = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : o ? /* @__PURE__ */ e.createElement("img", { src: o, className: "tlTypeIcon", alt: "" }) : null, d = /* @__PURE__ */ e.createElement(e.Fragment, null, c, u && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, u)), m = e.useCallback((g) => {
    g.preventDefault(), n($r, {});
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
}, Wr = 20, Ur = () => {
  var w;
  const l = X(), t = ne(), n = l.nodes ?? [], a = l.selectionMode ?? "single", o = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [r, c] = e.useState(-1), d = e.useRef(null), m = ((w = n.find((b) => b.selected)) == null ? void 0 : w.id) ?? null;
  e.useEffect(() => {
    var D;
    if (m == null)
      return;
    const b = (D = d.current) == null ? void 0 : D.querySelector(".tlTreeView__node--selected");
    b && b.scrollIntoView({ block: "nearest" });
  }, [m]);
  const p = e.useCallback((b, D) => {
    t(D ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, D) => {
    var R;
    const j = window.getSelection();
    j && !j.isCollapsed && D.currentTarget.contains(j.anchorNode) || ((R = d.current) == null || R.focus({ preventScroll: !0 }), t("select", {
      nodeId: b,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    }));
  }, [t]), g = e.useCallback((b, D) => {
    D.preventDefault(), t("contextMenu", { nodeId: b, x: D.clientX, y: D.clientY });
  }, [t]), N = e.useRef(null), C = e.useCallback((b, D) => {
    const j = D.getBoundingClientRect(), R = b.clientY - j.top, H = j.height / 3;
    return R < H ? "above" : R > H * 2 ? "below" : "within";
  }, []), _ = e.useCallback((b, D) => {
    D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", b);
  }, []), y = e.useCallback((b, D) => {
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const j = C(D, D.currentTarget);
    N.current != null && window.clearTimeout(N.current), N.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: j }), N.current = null;
    }, 50);
  }, [t, C]), k = e.useCallback((b, D) => {
    D.preventDefault(), N.current != null && (window.clearTimeout(N.current), N.current = null);
    const j = C(D, D.currentTarget);
    t("drop", { nodeId: b, position: j });
  }, [t, C]), x = e.useCallback(() => {
    N.current != null && (window.clearTimeout(N.current), N.current = null), t("dragEnd");
  }, [t]), E = e.useCallback((b) => {
    if (n.length === 0) return;
    let D = r;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), D = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), D = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const j = n[r];
          if (j.expandable && !j.expanded) {
            t("expand", { nodeId: j.id });
            return;
          } else j.expanded && (D = r + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), r >= 0 && r < n.length) {
          const j = n[r];
          if (j.expanded) {
            t("collapse", { nodeId: j.id });
            return;
          } else {
            const R = j.depth;
            for (let H = r - 1; H >= 0; H--)
              if (n[H].depth < R) {
                D = H;
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
        b.preventDefault(), D = 0;
        break;
      case "End":
        b.preventDefault(), D = n.length - 1;
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
      onKeyDown: E
    },
    n.map((b, D) => /* @__PURE__ */ e.createElement(
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
          D === r ? "tlTreeView__node--focused" : "",
          s === b.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * Wr },
        draggable: o,
        onMouseDown: (j) => {
          (j.shiftKey || j.ctrlKey || j.metaKey || j.detail > 1) && j.preventDefault();
        },
        onClick: (j) => h(b.id, j),
        onContextMenu: (j) => g(b.id, j),
        onDragStart: (j) => _(b.id, j),
        onDragOver: u ? (j) => y(b.id, j) : void 0,
        onDrop: u ? (j) => k(b.id, j) : void 0,
        onDragEnd: x
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (j) => {
            j.stopPropagation(), p(b.id, b.expanded);
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
var It = { exports: {} }, ge = {}, jt = { exports: {} }, J = {};
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
  }, N = Object.assign, C = {};
  function _(f, M, Y) {
    this.props = f, this.context = M, this.refs = C, this.updater = Y || g;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(f, M) {
    if (typeof f != "object" && typeof f != "function" && f != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, f, M, "setState");
  }, _.prototype.forceUpdate = function(f) {
    this.updater.enqueueForceUpdate(this, f, "forceUpdate");
  };
  function y() {
  }
  y.prototype = _.prototype;
  function k(f, M, Y) {
    this.props = f, this.context = M, this.refs = C, this.updater = Y || g;
  }
  var x = k.prototype = new y();
  x.constructor = k, N(x, _.prototype), x.isPureReactComponent = !0;
  var E = Array.isArray;
  function w() {
  }
  var b = { H: null, A: null, T: null, S: null }, D = Object.prototype.hasOwnProperty;
  function j(f, M, Y) {
    var W = Y.ref;
    return {
      $$typeof: l,
      type: f,
      key: M,
      ref: W !== void 0 ? W : null,
      props: Y
    };
  }
  function R(f, M) {
    return j(f.type, M, f.props);
  }
  function H(f) {
    return typeof f == "object" && f !== null && f.$$typeof === l;
  }
  function K(f) {
    var M = { "=": "=0", ":": "=2" };
    return "$" + f.replace(/[=:]/g, function(Y) {
      return M[Y];
    });
  }
  var A = /\/+/g;
  function z(f, M) {
    return typeof f == "object" && f !== null && f.key != null ? K("" + f.key) : M.toString(36);
  }
  function B(f) {
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
  function P(f, M, Y, W, Z) {
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
                M,
                Y,
                W,
                Z
              );
          }
      }
    if (te)
      return Z = Z(f), te = W === "" ? "." + z(f, 0) : W, E(Z) ? (Y = "", te != null && (Y = te.replace(A, "$&/") + "/"), P(Z, M, Y, "", function(me) {
        return me;
      })) : Z != null && (H(Z) && (Z = R(
        Z,
        Y + (Z.key == null || f && f.key === Z.key ? "" : ("" + Z.key).replace(
          A,
          "$&/"
        ) + "/") + te
      )), M.push(Z)), 1;
    te = 0;
    var se = W === "" ? "." : W + ":";
    if (E(f))
      for (var ae = 0; ae < f.length; ae++)
        W = f[ae], F = se + z(W, ae), te += P(
          W,
          M,
          Y,
          F,
          Z
        );
    else if (ae = h(f), typeof ae == "function")
      for (f = ae.call(f), ae = 0; !(W = f.next()).done; )
        W = W.value, F = se + z(W, ae++), te += P(
          W,
          M,
          Y,
          F,
          Z
        );
    else if (F === "object") {
      if (typeof f.then == "function")
        return P(
          B(f),
          M,
          Y,
          W,
          Z
        );
      throw M = String(f), Error(
        "Objects are not valid as a React child (found: " + (M === "[object Object]" ? "object with keys {" + Object.keys(f).join(", ") + "}" : M) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return te;
  }
  function I(f, M, Y) {
    if (f == null) return f;
    var W = [], Z = 0;
    return P(f, W, "", "", function(F) {
      return M.call(Y, F, Z++);
    }), W;
  }
  function L(f) {
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
  var S = typeof reportError == "function" ? reportError : function(f) {
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
  }, $ = {
    map: I,
    forEach: function(f, M, Y) {
      I(
        f,
        function() {
          M.apply(this, arguments);
        },
        Y
      );
    },
    count: function(f) {
      var M = 0;
      return I(f, function() {
        M++;
      }), M;
    },
    toArray: function(f) {
      return I(f, function(M) {
        return M;
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
  return J.Activity = m, J.Children = $, J.Component = _, J.Fragment = n, J.Profiler = o, J.PureComponent = k, J.StrictMode = a, J.Suspense = r, J.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = b, J.__COMPILER_RUNTIME = {
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
    var W = N({}, f.props), Z = f.key;
    if (M != null)
      for (F in M.key !== void 0 && (Z = "" + M.key), M)
        !D.call(M, F) || F === "key" || F === "__self" || F === "__source" || F === "ref" && M.ref === void 0 || (W[F] = M[F]);
    var F = arguments.length - 2;
    if (F === 1) W.children = Y;
    else if (1 < F) {
      for (var te = Array(F), se = 0; se < F; se++)
        te[se] = arguments[se + 2];
      W.children = te;
    }
    return j(f.type, Z, W);
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
    var W, Z = {}, F = null;
    if (M != null)
      for (W in M.key !== void 0 && (F = "" + M.key), M)
        D.call(M, W) && W !== "key" && W !== "__self" && W !== "__source" && (Z[W] = M[W]);
    var te = arguments.length - 2;
    if (te === 1) Z.children = Y;
    else if (1 < te) {
      for (var se = Array(te), ae = 0; ae < te; ae++)
        se[ae] = arguments[ae + 2];
      Z.children = se;
    }
    if (f && f.defaultProps)
      for (W in te = f.defaultProps, te)
        Z[W] === void 0 && (Z[W] = te[W]);
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
      var W = f(), Z = b.S;
      Z !== null && Z(Y, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(w, S);
    } catch (F) {
      S(F);
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
var fn;
function zr() {
  return fn || (fn = 1, jt.exports = Vr()), jt.exports;
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
function Kr() {
  if (hn) return ge;
  hn = 1;
  var l = zr();
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
  return ge.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, ge.createPortal = function(r, c) {
    var d = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!c || c.nodeType !== 1 && c.nodeType !== 9 && c.nodeType !== 11)
      throw Error(t(299));
    return u(r, c, null, d);
  }, ge.flushSync = function(r) {
    var c = s.T, d = a.p;
    try {
      if (s.T = null, a.p = 2, r) return r();
    } finally {
      s.T = c, a.p = d, a.d.f();
    }
  }, ge.preconnect = function(r, c) {
    typeof r == "string" && (c ? (c = c.crossOrigin, c = typeof c == "string" ? c === "use-credentials" ? c : "" : void 0) : c = null, a.d.C(r, c));
  }, ge.prefetchDNS = function(r) {
    typeof r == "string" && a.d.D(r);
  }, ge.preinit = function(r, c) {
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
  }, ge.preinitModule = function(r, c) {
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
  }, ge.preload = function(r, c) {
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
  }, ge.preloadModule = function(r, c) {
    if (typeof r == "string")
      if (c) {
        var d = i(c.as, c.crossOrigin);
        a.d.m(r, {
          as: typeof c.as == "string" && c.as !== "script" ? c.as : void 0,
          crossOrigin: d,
          integrity: typeof c.integrity == "string" ? c.integrity : void 0
        });
      } else a.d.m(r);
  }, ge.requestFormReset = function(r) {
    a.d.r(r);
  }, ge.unstable_batchedUpdates = function(r, c) {
    return r(c);
  }, ge.useFormState = function(r, c, d) {
    return s.H.useFormState(r, c, d);
  }, ge.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, ge.version = "19.2.4", ge;
}
var bn;
function Yr() {
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
  return l(), It.exports = Kr(), It.exports;
}
var Dn = Yr();
const { useState: Me, useCallback: he, useRef: tt, useEffect: We, useMemo: zt } = e, Gr = "goto", Xr = "option";
function Xt({ image: l }) {
  return l ? l.startsWith("/") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" }) : /* @__PURE__ */ e.createElement(Ne, { encoded: l, className: "tlDropdownSelect__optionIcon" }) : null;
}
function qr({
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
  const d = he(
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
    /* @__PURE__ */ e.createElement(Xt, { image: l.image }),
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
function Zr({
  option: l,
  onGoto: t
}) {
  const n = he(
    (o) => {
      o.preventDefault(), t(l.value);
    },
    [t, l.value]
  ), a = /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(Xt, { image: l.image }), /* @__PURE__ */ e.createElement("span", null, l.label));
  return l.link ? /* @__PURE__ */ e.createElement("a", { className: "tlDropdownSelect__readonlyValue tlResourceCell", href: "#", onClick: n }, a) : /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__readonlyValue" }, a);
}
function Qr({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: o,
  id: u
}) {
  const s = he(() => a(l.value), [a, l.value]), i = zt(() => {
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
    /* @__PURE__ */ e.createElement(Xt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const Jr = ({ controlId: l, state: t }) => {
  const n = ne(), a = t.value ?? [], o = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, r = t.editable !== !1, c = t.optionsLoaded === !0, d = t.options ?? [], m = t.emptyOptionLabel ?? "", p = u && o && !i && r, h = ue({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), g = h["js.dropdownSelect.nothingFound"], N = he(
    (O) => h["js.dropdownSelect.removeChip"].replace("{0}", O),
    [h]
  ), [C, _] = Me(!1), [y, k] = Me(""), [x, E] = Me(-1), [w, b] = Me(!1), [D, j] = Me({}), [R, H] = Me(null), [K, A] = Me(null), [z, B] = Me(null), P = tt(null), I = tt(null), L = tt(null), S = tt(a);
  S.current = a;
  const $ = tt(-1), f = zt(
    () => new Set(a.map((O) => O.value)),
    [a]
  ), M = zt(() => {
    let O = d.filter((q) => !f.has(q.value));
    if (y) {
      const q = y.toLowerCase();
      O = O.filter((le) => le.label.toLowerCase().includes(q));
    }
    return O;
  }, [d, f, y]);
  We(() => {
    y && M.length === 1 ? E(0) : E(-1);
  }, [M.length, y]), We(() => {
    C && c && I.current && I.current.focus();
  }, [C, c, a]), We(() => {
    var le, ie;
    if ($.current < 0) return;
    const O = $.current;
    $.current = -1;
    const q = (le = P.current) == null ? void 0 : le.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    q && q.length > 0 ? q[Math.min(O, q.length - 1)].focus() : (ie = P.current) == null || ie.focus();
  }, [a]), We(() => {
    if (!C) return;
    const O = (q) => {
      P.current && !P.current.contains(q.target) && L.current && !L.current.contains(q.target) && (_(!1), k(""));
    };
    return document.addEventListener("mousedown", O), () => document.removeEventListener("mousedown", O);
  }, [C]), We(() => {
    if (!C || !P.current) return;
    const O = P.current.getBoundingClientRect(), q = window.innerHeight - O.bottom, ie = q < 300 && O.top > q;
    j({
      left: O.left,
      width: O.width,
      ...ie ? { bottom: window.innerHeight - O.top } : { top: O.bottom }
    });
  }, [C]);
  const Y = he(async () => {
    if (!(i || !r) && (_(!0), k(""), E(-1), b(!1), !c))
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
  }, [i, r, c, n]), W = he(() => {
    var O;
    _(!1), k(""), E(-1), (O = P.current) == null || O.focus();
  }, []), Z = he(
    (O) => {
      let q;
      if (o) {
        const le = d.find((ie) => ie.value === O);
        if (le)
          q = [...S.current, le];
        else
          return;
      } else {
        const le = d.find((ie) => ie.value === O);
        if (le)
          q = [le];
        else
          return;
      }
      S.current = q, n(it, { value: q.map((le) => le.value) }), o ? (k(""), E(-1)) : W();
    },
    [o, d, n, W]
  ), F = he(
    (O) => {
      $.current = S.current.findIndex((le) => le.value === O);
      const q = S.current.filter((le) => le.value !== O);
      S.current = q, n(it, { value: q.map((le) => le.value) });
    },
    [n]
  ), te = he(
    (O) => {
      O.stopPropagation(), n(it, { value: [] }), W();
    },
    [n, W]
  ), se = he((O) => {
    k(O.target.value);
  }, []), ae = he(
    (O) => {
      n(Gr, { [Xr]: O });
    },
    [n]
  ), me = he(
    (O) => {
      if (!C) {
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
          O.preventDefault(), O.stopPropagation(), x >= 0 && x < M.length && Z(M[x].value);
          break;
        case "Escape":
          O.preventDefault(), O.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          y === "" && o && a.length > 0 && F(a[a.length - 1].value);
          break;
      }
    },
    [
      C,
      Y,
      W,
      M,
      x,
      Z,
      y,
      o,
      a,
      F
    ]
  ), ye = he(
    async (O) => {
      O.preventDefault(), b(!1);
      try {
        await n("loadOptions");
      } catch {
        b(!0);
      }
    },
    [n]
  ), we = he(
    (O, q) => {
      H(O), q.dataTransfer.effectAllowed = "move", q.dataTransfer.setData("text/plain", String(O));
    },
    []
  ), Te = he(
    (O, q) => {
      if (q.preventDefault(), q.dataTransfer.dropEffect = "move", R === null || R === O) {
        A(null), B(null);
        return;
      }
      const le = q.currentTarget.getBoundingClientRect(), ie = le.left + le.width / 2, qe = q.clientX < ie ? "before" : "after";
      A(O), B(qe);
    },
    [R]
  ), Re = he(
    (O) => {
      if (O.preventDefault(), R === null || K === null || z === null || R === K) return;
      const q = [...S.current], [le] = q.splice(R, 1);
      let ie = K;
      R < K ? ie = z === "before" ? ie - 1 : ie : ie = z === "before" ? ie : ie + 1, q.splice(ie, 0, le), S.current = q, n(it, { value: q.map((qe) => qe.value) }), H(null), A(null), B(null);
    },
    [R, K, z, n]
  ), $e = he(() => {
    H(null), A(null), B(null);
  }, []);
  if (We(() => {
    if (x < 0 || !L.current) return;
    const O = L.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    O && O.scrollIntoView({ block: "nearest" });
  }, [x, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.map((O) => /* @__PURE__ */ e.createElement(Zr, { key: O.value, option: O, onGoto: ae })));
  const Xe = !s && a.length > 0 && !i, st = C ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlDropdownSelect__dropdown",
      style: D,
      ...Qn
    },
    (c || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: I,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: se,
        onKeyDown: me,
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
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ye }, h["js.dropdownSelect.error"])),
      c && M.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, g),
      c && M.map((O, q) => /* @__PURE__ */ e.createElement(
        Qr,
        {
          key: O.value,
          id: `${l}-opt-${q}`,
          option: O,
          highlighted: q === x,
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
      ref: P,
      className: "tlDropdownSelect" + (C ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": C,
      "aria-haspopup": "listbox",
      "aria-owns": C ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: C ? void 0 : Y,
      onKeyDown: me
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : a.map((O, q) => {
      let le = "";
      return R === q ? le = "tlDropdownSelect__chip--dragging" : K === q && z === "before" ? le = "tlDropdownSelect__chip--dropBefore" : K === q && z === "after" && (le = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        qr,
        {
          key: O.value,
          option: O,
          removable: !i && (o || !s),
          onRemove: F,
          removeLabel: N(O.label),
          draggable: p,
          onDragStart: p ? (ie) => we(q, ie) : void 0,
          onDragOver: p ? (ie) => Te(q, ie) : void 0,
          onDrop: p ? Re : void 0,
          onDragEnd: p ? $e : void 0,
          dragClassName: p ? le : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Xe && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: te,
        "aria-label": h["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, C ? "▲" : "▼"))
  ), st && Dn.createPortal(st, document.body));
}, { useCallback: Pt, useRef: eo } = e, Tn = "application/x-tl-color", to = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: o,
  onReplace: u
}) => {
  const s = eo(null), i = Pt(
    (d) => (m) => {
      s.current = d, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Pt((d) => {
    d.preventDefault(), d.dataTransfer.dropEffect = "move";
  }, []), c = Pt(
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
function Kt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Ln(l) {
  if (!Kt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function xn(l, t, n) {
  const a = (o) => Rn(o).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function no(l, t, n) {
  const a = l / 255, o = t / 255, u = n / 255, s = Math.max(a, o, u), i = Math.min(a, o, u), r = s - i;
  let c = 0;
  r !== 0 && (s === a ? c = (o - u) / r % 6 : s === o ? c = (u - a) / r + 2 : c = (a - o) / r + 4, c *= 60, c < 0 && (c += 360));
  const d = s === 0 ? 0 : r / s;
  return [c, d, s];
}
function lo(l, t, n) {
  const a = n * t, o = a * (1 - Math.abs(l / 60 % 2 - 1)), u = n - a;
  let s = 0, i = 0, r = 0;
  return l < 60 ? (s = a, i = o, r = 0) : l < 120 ? (s = o, i = a, r = 0) : l < 180 ? (s = 0, i = a, r = o) : l < 240 ? (s = 0, i = o, r = a) : l < 300 ? (s = o, i = 0, r = a) : (s = a, i = 0, r = o), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((r + u) * 255)
  ];
}
function ao(l) {
  return no(...Ln(l));
}
function Bt(l, t, n) {
  return xn(...lo(l, t, n));
}
const { useCallback: Ue, useRef: gn } = e, ro = ({ color: l, onColorChange: t }) => {
  const [n, a, o] = ao(l), u = gn(null), s = gn(null), i = Ue(
    (g, N) => {
      var k;
      const C = (k = u.current) == null ? void 0 : k.getBoundingClientRect();
      if (!C) return;
      const _ = Math.max(0, Math.min(1, (g - C.left) / C.width)), y = Math.max(0, Math.min(1, 1 - (N - C.top) / C.height));
      t(Bt(n, _, y));
    },
    [n, t]
  ), r = Ue(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), i(g.clientX, g.clientY);
    },
    [i]
  ), c = Ue(
    (g) => {
      g.buttons !== 0 && i(g.clientX, g.clientY);
    },
    [i]
  ), d = Ue(
    (g) => {
      var y;
      const N = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!N) return;
      const _ = Math.max(0, Math.min(1, (g - N.top) / N.height)) * 360;
      t(Bt(_, a, o));
    },
    [a, o, t]
  ), m = Ue(
    (g) => {
      g.preventDefault(), g.target.setPointerCapture(g.pointerId), d(g.clientY);
    },
    [d]
  ), p = Ue(
    (g) => {
      g.buttons !== 0 && d(g.clientY);
    },
    [d]
  ), h = Bt(n, 1, 1);
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
function oo(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const so = {
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
}, { useState: pt, useCallback: ke, useEffect: En, useRef: co, useLayoutEffect: io } = e, uo = ({
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
  const [c, d] = pt("palette"), [m, p] = pt(t), h = co(null), g = ue(so), [N, C] = pt(null);
  io(() => {
    if (!l.current || !h.current) return;
    const L = l.current.getBoundingClientRect(), S = h.current.getBoundingClientRect();
    let $ = L.bottom + 4, f = L.left;
    $ + S.height > window.innerHeight && ($ = L.top - S.height - 4), f + S.width > window.innerWidth && (f = Math.max(0, L.right - S.width)), C({ top: $, left: f });
  }, [l]);
  const _ = m != null, [y, k, x] = _ ? Ln(m) : [0, 0, 0], [E, w] = pt((m == null ? void 0 : m.toUpperCase()) ?? "");
  En(() => {
    w((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Oe(!0, { ESCAPE: i }), En(() => {
    const L = ($) => {
      h.current && !h.current.contains($.target) && i();
    }, S = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(S), document.removeEventListener("mousedown", L);
    };
  }, [i]);
  const b = ke(
    (L) => (S) => {
      const $ = parseInt(S.target.value, 10);
      if (isNaN($)) return;
      const f = Rn($);
      p(xn(L === "r" ? f : y, L === "g" ? f : k, L === "b" ? f : x));
    },
    [y, k, x]
  ), D = ke(
    (L) => {
      if (m != null) {
        L.dataTransfer.setData(Tn, m.toUpperCase()), L.dataTransfer.effectAllowed = "move";
        const S = document.createElement("div");
        S.style.width = "33px", S.style.height = "33px", S.style.backgroundColor = m, S.style.borderRadius = "3px", S.style.border = "1px solid rgba(0,0,0,0.1)", S.style.position = "absolute", S.style.top = "-9999px", document.body.appendChild(S), L.dataTransfer.setDragImage(S, 16, 16), requestAnimationFrame(() => document.body.removeChild(S));
      }
    },
    [m]
  ), j = ke((L) => {
    const S = L.target.value;
    w(S), Kt(S) && p(S);
  }, []), R = ke(() => {
    p(null);
  }, []), H = ke((L) => {
    p(L);
  }, []), K = ke(
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
  ), z = ke(
    (L, S) => {
      const $ = [...n];
      $[L] = S, r($);
    },
    [n, r]
  ), B = ke(() => {
    r([...o]);
  }, [o, r]), P = ke(
    (L) => {
      if (oo(n, L)) return;
      const S = n.indexOf(null);
      if (S < 0) return;
      const $ = [...n];
      $[S] = L.toUpperCase(), r($);
    },
    [n, r]
  ), I = ke(() => {
    m != null && P(m), s(m);
  }, [m, s, P]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: h,
      style: N ? { top: N.top, left: N.left, visibility: "visible" } : { visibility: "hidden" }
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
      to,
      {
        colors: n,
        columns: a,
        onSelect: H,
        onConfirm: K,
        onSwap: A,
        onReplace: z
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: B }, g["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(ro, { color: m ?? "#000000", onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (_ ? "" : " tlColorInput--noColor"),
        style: _ ? { backgroundColor: m } : void 0,
        draggable: _,
        onDragStart: _ ? D : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? y : "",
        onChange: b("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? k : "",
        onChange: b("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? x : "",
        onChange: b("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (E !== "" && !Kt(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: j
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, g["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: I }, g["js.colorInput.ok"]))
  );
}, mo = { "js.colorInput.chooseColor": "Choose color" }, { useState: po, useCallback: ft, useRef: fo } = e, ho = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(mo), [s, i] = po(!1), r = fo(null), c = n, d = t.editable !== !1, m = t.palette ?? [], p = t.paletteColumns ?? 6, h = t.defaultPalette ?? m, g = ft(() => {
    d && i(!0);
  }, [d]), N = ft(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = ft(() => {
    i(!1);
  }, []), _ = ft(
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
    uo,
    {
      anchorRef: r,
      currentColor: c,
      palette: m,
      paletteColumns: p,
      defaultPalette: h,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: C,
      onPaletteChange: _
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
}, { useState: nt, useCallback: Be, useEffect: At, useRef: vn, useLayoutEffect: bo, useMemo: go } = e, Eo = {
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
}, vo = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: o,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = ue(Eo), [r, c] = nt("simple"), [d, m] = nt(""), [p, h] = nt(t ?? ""), [g, N] = nt(!1), [C, _] = nt(null), y = vn(null), k = vn(null);
  bo(() => {
    if (!l.current || !y.current) return;
    const K = l.current.getBoundingClientRect(), A = y.current.getBoundingClientRect();
    let z = K.bottom + 4, B = K.left;
    z + A.height > window.innerHeight && (z = K.top - A.height - 4), B + A.width > window.innerWidth && (B = Math.max(0, K.right - A.width)), _({ top: z, left: B });
  }, [l]), At(() => {
    !a && !g && s().catch(() => N(!0));
  }, [a, g, s]), At(() => {
    a && k.current && k.current.focus();
  }, [a]), Oe(!0, { ESCAPE: u }), At(() => {
    const K = (z) => {
      y.current && !y.current.contains(z.target) && u();
    }, A = setTimeout(() => document.addEventListener("mousedown", K), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", K);
    };
  }, [u]);
  const x = go(() => {
    if (!d) return n;
    const K = d.toLowerCase();
    return n.filter(
      (A) => A.prefix.toLowerCase().includes(K) || A.label.toLowerCase().includes(K) || A.terms != null && A.terms.some((z) => z.includes(K))
    );
  }, [n, d]), E = Be((K) => {
    m(K.target.value);
  }, []), w = Be(
    (K) => {
      o(K);
    },
    [o]
  ), b = Be((K) => {
    h(K);
  }, []), D = Be((K) => {
    h(K.target.value);
  }, []), j = Be(() => {
    o(p || null);
  }, [p, o]), R = Be(() => {
    o(null);
  }, [o]), H = Be(async (K) => {
    K.preventDefault(), N(!1);
    try {
      await s();
    } catch {
      N(!0);
    }
  }, [s]);
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
        ref: k,
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: H }, i["js.iconSelect.loadError"])),
      a && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      a && x.map(
        (K) => K.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: K.label,
            onClick: () => r === "simple" ? w(A.encoded) : b(A.encoded),
            onKeyDown: (z) => {
              (z.key === "Enter" || z.key === " ") && (z.preventDefault(), r === "simple" ? w(A.encoded) : b(A.encoded));
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
        onChange: D
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, p && /* @__PURE__ */ e.createElement(Ne, { encoded: p })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, p ? p.startsWith("css:") ? p.substring(4) : p : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: j }, i["js.iconSelect.ok"]))
  );
}, _o = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Co, useCallback: ht, useRef: yo } = e, wo = ({ controlId: l, state: t }) => {
  const [n, a] = De(), o = ne(), u = ue(_o), [s, i] = Co(!1), r = yo(null), c = n, d = t.editable !== !1, m = t.disabled === !0, p = t.icons ?? [], h = t.iconsLoaded === !0, g = ht(() => {
    d && !m && i(!0);
  }, [d, m]), N = ht(
    (y) => {
      i(!1), a(y);
    },
    [a]
  ), C = ht(() => {
    i(!1);
  }, []), _ = ht(async () => {
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
    vo,
    {
      anchorRef: r,
      currentValue: c,
      icons: p,
      iconsLoaded: h,
      onSelect: N,
      onCancel: C,
      onLoadIcons: _
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, c ? /* @__PURE__ */ e.createElement(Ne, { encoded: c }) : null));
}, { useCallback: Ve, useEffect: ko, useMemo: _n, useRef: No, useState: Ft } = e, So = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Do = [1, 2, 3, 4];
function To(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), o = n[2] || "px";
  return o === "rem" || o === "em" ? a * t : a;
}
function Ro(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const o of Do)
    n >= o && (a = o);
  return a;
}
function Lo(l, t) {
  const n = So[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function xo(l, t) {
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
        for (let N = p.colEnd; N < h; N++) u(g, N);
      p.colEnd = h;
    }
  };
  for (const m of l) {
    const p = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let h = Math.min(Lo(m.width, n), n);
    for (; o(i, r); )
      r++, r >= n && (r = 0, i++);
    let g = 0;
    for (let k = r; k < n && !o(i, k); k++)
      g++;
    if (h > g) {
      for (c(i), r = 0, i++; o(i, r); )
        r++, r >= n && (r = 0, i++);
      g = 0;
      for (let k = r; k < n && !o(i, k); k++)
        g++;
      h = Math.min(h, g);
    }
    const N = r, C = r + h, _ = i, y = i + p;
    s.push({ id: m.id, colStart: N, colEnd: C, rowStart: _, rowEnd: y });
    for (let k = _; k < y; k++)
      for (let x = N; x < C; x++) u(k, x);
    r = C, r >= n && (r = 0, i++);
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
const Mo = ({ controlId: l }) => {
  const t = X(), n = ne(), a = t.minColWidth ?? "16rem", o = (t.children ?? []).filter((w) => w && w.id), u = No(null), [s, i] = Ft(1), r = t.editMode === !0;
  ko(() => {
    const w = u.current;
    if (!w) return;
    const b = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, D = To(a, b), j = () => i(Ro(w.clientWidth, D));
    j();
    const R = new ResizeObserver(j);
    return R.observe(w), () => R.disconnect();
  }, [a]);
  const c = _n(() => xo(o, s), [o, s]), d = _n(() => {
    const w = {};
    for (const b of c) w[b.id] = b;
    return w;
  }, [c]), [m, p] = Ft(null), [h, g] = Ft(null), N = Ve((w, b) => {
    if (!r) {
      w.preventDefault();
      return;
    }
    p(b), w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", b);
  }, [r]), C = Ve((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const D = w.currentTarget.getBoundingClientRect(), j = w.clientX < D.left + D.width / 2;
    g((R) => R && R.id === b && R.before === j ? R : { id: b, before: j });
  }, [r, m]), _ = Ve(() => {
  }, []), y = Ve((w, b, D) => {
    const j = o.map((A) => A.id), R = j.indexOf(w);
    if (R < 0) return;
    j.splice(R, 1);
    const H = j.indexOf(b);
    if (H < 0) {
      j.splice(R, 0, w);
      return;
    }
    const K = D ? H : H + 1;
    j.splice(K, 0, w), n("reorder", { order: j });
  }, [o, n]), k = Ve((w, b) => {
    if (!r || !m || m === b) return;
    w.preventDefault();
    const D = w.currentTarget.getBoundingClientRect(), j = w.clientX < D.left + D.width / 2;
    y(m, b, j), p(null), g(null);
  }, [r, m, y]), x = Ve(() => {
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
      const D = {
        gridColumn: `${b.colStart + 1} / ${b.colEnd + 1}`,
        gridRow: `${b.rowStart + 1} / ${b.rowEnd + 1}`
      }, j = ["tlDashboard__tile"];
      return m === w.id && j.push("tlDashboard__tile--dragging"), h && h.id === w.id && j.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: j.join(" "),
          style: D,
          draggable: r,
          onDragStart: (R) => N(R, w.id),
          onDragOver: (R) => C(R, w.id),
          onDragLeave: _,
          onDrop: (R) => k(R, w.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(G, { control: w.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: Io, useRef: Cn, useState: yn, useEffect: jo, useLayoutEffect: Po } = e, Bo = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(G, { control: n }))));
}, Ao = ({ group: l }) => {
  var m, p;
  const [t, n] = yn(!1), [a, o] = yn({}), u = Cn(null), s = Cn(null), i = Io(() => {
    n((h) => !h);
  }, []);
  Po(() => {
    if (!t) return;
    const h = () => {
      const g = u.current;
      if (!g) return;
      const N = g.getBoundingClientRect();
      o({
        position: "fixed",
        top: N.bottom + 4,
        right: Math.max(8, window.innerWidth - N.right),
        left: "auto"
      });
    };
    return h(), window.addEventListener("resize", h), window.addEventListener("scroll", h, !0), () => {
      window.removeEventListener("resize", h), window.removeEventListener("scroll", h, !0);
    };
  }, [t]), jo(() => {
    if (!t) return;
    const h = (g) => {
      s.current && !s.current.contains(g.target) && u.current && !u.current.contains(g.target) && n(!1);
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [t]), Oe(t, { ESCAPE: () => n(!1) }), Gt(t, s, "first");
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
      r.map((h, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: h }))),
      (p = l.subGroups) == null ? void 0 : p.map((h, g) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${g}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), h.items.map((N, C) => /* @__PURE__ */ e.createElement("div", { key: C, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(G, { control: N })))))
    ),
    document.body
  ));
}, Fo = ({ controlId: l }) => {
  const a = (X().groups ?? []).filter((o) => o.items.some((u) => u != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((o, u) => /* @__PURE__ */ e.createElement(e.Fragment, { key: o.name }, u > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), o.display === "menu" ? /* @__PURE__ */ e.createElement(Ao, { group: o }) : /* @__PURE__ */ e.createElement(Bo, { group: o }))));
}, Oo = ({ frame: l, covered: t }) => {
  const [n, a] = rt(), o = [
    "tlTileStack__frame",
    t ? "tlTileStack__frame--covered" : "",
    n
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { className: o }, /* @__PURE__ */ e.createElement(G, { control: l })));
}, $o = ({ controlId: l }) => {
  const t = X(), [n, a] = rt(), o = t.frames ?? [], u = t.activeIndex ?? 0;
  return /* @__PURE__ */ e.createElement(ot, { host: a }, /* @__PURE__ */ e.createElement("div", { id: l, className: n ? "tlTileStack " + n : "tlTileStack" }, o.map((s, i) => /* @__PURE__ */ e.createElement(Oo, { key: s.controlId, frame: s, covered: i !== u }))));
}, Ho = ({ controlId: l }) => {
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
}, Wo = ({ controlId: l }) => {
  const n = X().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, Uo = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), Vo = {
  "js.sidebar.openDrawer": "Open navigation"
}, zo = ({ controlId: l }) => {
  const t = ne(), n = ue(Vo);
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
U("TLButton", gl);
U("TLUploadButton", El);
U("TLToggleButton", _l);
U("TLTextInput", tl);
U("TLPasswordInput", ll);
U("TLNumberInput", rl);
U("TLDatePicker", sl);
U("TLSelect", il);
U("TLBooleanChoice", dl);
U("TLCheckbox", hl);
U("TLCounter", Cl);
U("TLTabBar", wl);
U("TLFieldList", kl);
U("TLAudioRecorder", Sl);
U("TLAudioPlayer", Tl);
U("TLFileUpload", Ll);
U("TLBinaryField", Ml);
U("TLFileChips", Pl);
U("TLRelativeTime", Fl);
U("TLAnchor", Ol);
U("TLScrollLink", $l);
U("TLAvatar", Ul);
U("TLDownload", zl);
U("TLPhotoCapture", Yl);
U("TLPhotoViewer", Xl);
U("TLPdfViewer", Zl);
U("TLSplitPanel", Ql);
U("TLPanel", ra);
U("TLInset", ga);
U("TLMaximizeRoot", oa);
U("TLDeckPane", sa);
U("TLSidebar", ha);
U("TLStack", ba);
U("TLGrid", Ea);
U("TLCard", va);
U("TLAppBar", _a);
U("TLBreadcrumb", ya);
U("TLBottomBar", ka);
U("TLDialog", Da);
U("TLDialogManager", La);
U("TLWindow", ja);
U("TLDrawer", Aa);
U("TLMenuRegion", Oa);
U("TLSnackbar", Ua);
U("TLNoticeBar", qa);
U("TLMenu", Qa);
U("TLAppShell", er);
U("TLText", tr);
U("TLTableView", sr);
U("TLColumnSelect", ir);
U("TLCalendar", wr);
U("TLFormLayout", Mr);
U("TLFormGroup", Pr);
U("TLFormField", Or);
U("TLResourceCell", Hr);
U("TLTreeView", Ur);
U("TLDropdownSelect", Jr);
U("TLColorInput", ho);
U("TLIconSelect", wo);
U("TLDashboard", Mo);
U("TLToolbar", Fo);
U("TLTileStack", $o);
U("TLAdaptiveDetail", Ho);
U("TLSlot", Wo);
U("TLSlotContent", Uo);
U("TLDrawerToggle", zo);
