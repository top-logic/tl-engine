import { React as e, useTLFieldValue as Re, getComponent as Lt, useTLState as q, useTLCommand as te, useTLUpload as xe, TLChild as K, useI18N as ae, useTLDataUrl as Ie, register as H } from "tl-react-bridge";
const { useCallback: Dt } = e, xt = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), i = Dt(
    (r) => {
      a(r.target.value);
    },
    [a]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: It } = e, Mt = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), i = It(
    (r) => {
      a(r.target.value);
    },
    [a]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: jt } = e, Pt = ({ controlId: l, state: t, config: n }) => {
  const [a, i] = Re(), s = jt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, o = t.errorMessage, r = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: a != null ? String(a) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: r,
      "aria-invalid": c || void 0,
      title: c && o ? o : void 0
    }
  ));
}, { useCallback: Bt } = e, At = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), i = Bt(
    (o) => {
      a(o.target.value || null);
    },
    [a]
  );
  if (t.editable === !1) {
    const o = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, o);
  }
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && c ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: Ot } = e, $t = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [a, i] = Re(), s = Ot(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((h) => h.value === a)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, o = t.hasWarnings === !0, r = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && o ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: r,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Ft } = e, Wt = ({ controlId: l, state: t }) => {
  const [n, a] = Re(), i = Ft(
    (o) => {
      a(o.target.checked);
    },
    [a]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    s ? "tlReactCheckbox--error" : "",
    !s && c ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  );
}, Ht = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], a = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, a.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Lt(c.cellModule) : void 0, o = i[c.name];
    if (u) {
      const r = { value: o, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + s + "-" + c.name,
          state: r
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, o != null ? String(o) : "");
  })))));
};
function be({ encoded: l, className: t }) {
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
const { useCallback: Ut } = e, zt = ({ controlId: l, command: t, label: n, image: a, disabled: i, displayMode: s }) => {
  const c = q(), u = te(), o = t ?? "click", r = n ?? c.label, m = a ?? c.image, p = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", g = c.hidden === !0, f = c.tooltip, S = g ? { display: "none" } : void 0, w = c.appearance, C = c.size, y = c.navigateUrl, x = Ut(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    u(o);
  }, [u, o, y]), D = h === "icon-only", v = h === "icon-only" || h === "icon-label", _ = h === "label-only" || h === "icon-label" || D && !m, E = f ?? (D ? r : void 0), U = E ? `text:${E}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: x,
      disabled: p,
      style: S,
      className: "tlReactButton" + (D ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (w === "primary" ? " tlReactButton--primary" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": U,
      "aria-label": D ? r : void 0
    },
    v && m && /* @__PURE__ */ e.createElement(be, { encoded: m, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, r)
  );
}, Vt = ({ controlId: l }) => {
  const t = q(), n = xe(), a = e.useRef(null), [i, s] = e.useState(!1), c = t.label ?? "", u = t.image, o = t.disabled === !0, r = t.hidden === !0, m = t.displayMode ?? "label-only", p = t.appearance, h = t.accept, g = t.multiple === !0, f = e.useCallback(() => {
    var D;
    o || i || (D = a.current) == null || D.click();
  }, [o, i]), S = e.useCallback(async (D) => {
    const v = D.target.files;
    if (!v || v.length === 0) return;
    const _ = new FormData();
    for (let E = 0; E < v.length; E++)
      _.append("file", v[E], v[E].name);
    D.target.value = "", s(!0);
    try {
      await n(_);
    } finally {
      s(!1);
    }
  }, [n]), w = m === "icon-only", C = m === "icon-only" || m === "icon-label", y = m === "label-only" || m === "icon-label" || w && !u, x = o || i;
  return /* @__PURE__ */ e.createElement("span", { id: l, style: { display: "contents" } }, /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: a,
      type: "file",
      accept: h && h !== "*" ? h : void 0,
      multiple: g || void 0,
      onChange: S,
      style: { display: "none" }
    }
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: f,
      disabled: x,
      style: r ? { display: "none" } : void 0,
      className: "tlReactButton" + (w ? " tlReactButton--iconOnly" : "") + (p === "link" ? " tlReactButton--link" : "") + (p === "primary" ? " tlReactButton--primary" : ""),
      "aria-label": w ? c : void 0
    },
    C && u && /* @__PURE__ */ e.createElement(be, { encoded: u, className: "tlReactButton__image" }),
    y && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  ));
}, { useCallback: Kt } = e, Yt = ({ controlId: l, command: t, label: n, active: a, disabled: i }) => {
  const s = q(), c = te(), u = t ?? "click", o = n ?? s.label, r = a ?? s.active === !0, m = i ?? s.disabled === !0, p = Kt(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : "")
    },
    o
  );
}, Gt = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Xt } = e, qt = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.tabs ?? [], i = t.activeTabId, s = Xt((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === i,
      className: "tlReactTabBar__tab" + (c.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(c.id)
    },
    c.icon && /* @__PURE__ */ e.createElement(be, { encoded: c.icon, className: "tlReactTabBar__tabIcon" }),
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Zt = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: i })))));
}, Qt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Jt = ({ controlId: l }) => {
  const t = q(), n = xe(), [a, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : a !== "idle" ? a : m, g = e.useCallback(async () => {
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
        r.current = y, o.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(y, x ? { mimeType: x } : void 0);
        u.current = D, D.ondataavailable = (v) => {
          v.data.size > 0 && o.current.push(v.data);
        }, D.onstop = async () => {
          y.getTracks().forEach((E) => E.stop()), r.current = null;
          const v = new Blob(o.current, { type: D.mimeType || "audio/webm" });
          if (o.current = [], v.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", v, "recording.webm"), await n(_), i("idle");
        }, D.start(), i("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [a, n]), f = ae(Qt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], w = h === "uploading", C = ["tlAudioRecorder__button"];
  return h === "recording" && C.push("tlAudioRecorder__button--recording"), h === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: g,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, en = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, tn = ({ controlId: l }) => {
  const t = q(), n = Ie(), a = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(a ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), r = e.useRef(i);
  e.useEffect(() => {
    a ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), c("disabled"));
  }, [a]), e.useEffect(() => {
    i !== r.current && (r.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), c("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), c("playing");
      return;
    }
    if (!o.current) {
      c("loading");
      try {
        const w = await fetch(n);
        if (!w.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", w.status), c("idle");
          return;
        }
        const C = await w.blob();
        o.current = URL.createObjectURL(C);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = ae(en), h = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: g,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, nn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ln = ({ controlId: l }) => {
  const t = q(), n = xe(), [a, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : a !== "idle" ? a : o, h = e.useCallback(async (v) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", v, v.name), await n(_), i("idle");
  }, [n]), g = e.useCallback((v) => {
    var E;
    const _ = (E = v.target.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [h]), f = e.useCallback(() => {
    var v;
    a !== "uploading" && ((v = u.current) == null || v.click());
  }, [a]), S = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), c(!0);
  }, []), w = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), c(!1);
  }, []), C = e.useCallback((v) => {
    var E;
    if (v.preventDefault(), v.stopPropagation(), c(!1), a === "uploading") return;
    const _ = (E = v.dataTransfer.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [a, h]), y = p === "uploading", x = ae(nn), D = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: C
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: g,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: y,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, an = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, on = ({ controlId: l, state: t }) => {
  const a = q() ?? t ?? {}, i = xe(), s = Ie(), c = ae(an), u = a.editable !== !1, o = !!a.hasData, r = a.fileName ?? "download", m = a.dataRevision ?? 0, p = a.accept ?? "", h = a.status ?? "idle", g = a.error ?? null, [f, S] = e.useState("idle"), [w, C] = e.useState(!1), [y, x] = e.useState(!1), D = e.useRef(null), v = e.useCallback(async () => {
    if (!(!o || y)) {
      x(!0);
      try {
        const A = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(A);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const I = await M.blob(), Y = URL.createObjectURL(I), d = document.createElement("a");
        d.href = Y, d.download = r, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(Y);
      } catch (A) {
        console.error("[TLBinaryField] Fetch error:", A);
      } finally {
        x(!1);
      }
    }
  }, [o, y, s, m, r]), _ = e.useCallback(async (A) => {
    S("uploading");
    const M = new FormData();
    M.append("file", A, A.name), await i(M), S("idle");
  }, [i]), E = (h === "received" ? "idle" : f !== "idle" ? f : h) === "uploading", U = e.useCallback((A) => {
    var I;
    const M = (I = A.target.files) == null ? void 0 : I[0];
    M && _(M);
  }, [_]), P = e.useCallback(() => {
    var A;
    E || (A = D.current) == null || A.click();
  }, [E]), R = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!0);
  }, []), V = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), C(!1);
  }, []), B = e.useCallback((A) => {
    var I;
    if (A.preventDefault(), A.stopPropagation(), C(!1), E) return;
    const M = (I = A.dataTransfer.files) == null ? void 0 : I[0];
    M && _(M);
  }, [E, _]), T = y ? c["js.downloading"] : c["js.download.file"].replace("{0}", r), O = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (y ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: v,
      disabled: y,
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: r }, r));
  if (!u)
    return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, O) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const J = E, $ = E ? c["js.uploading"] : c["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${w ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: V,
      onDrop: B
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "file",
        accept: p || void 0,
        onChange: U,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (J ? " tlFileUpload__button--uploading" : ""),
        onClick: P,
        disabled: J,
        title: $,
        "aria-label": $
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && O,
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, g)
  );
}, rn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, sn = ({ controlId: l }) => {
  const t = q(), n = Ie(), a = te(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      r(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), C = URL.createObjectURL(w), y = document.createElement("a");
        y.href = C, y.download = c, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(C);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        r(!1);
      }
    }
  }, [i, o, n, s, c]), p = e.useCallback(async () => {
    i && await a("clear");
  }, [i, a]), h = ae(rn);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const g = o ? h["js.downloading"] : h["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: o,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, cn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, un = ({ controlId: l }) => {
  const t = q(), n = xe(), [a, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), g = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    w(), i("idle");
  }, [w]), y = e.useCallback(async () => {
    var R;
    if (a !== "uploading") {
      if (c(null), !S) {
        (R = h.current) == null || R.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = V, i("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [a, S]), x = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const R = r.current, V = p.current;
    if (!R || !V)
      return;
    V.width = R.videoWidth, V.height = R.videoHeight;
    const B = V.getContext("2d");
    B && (B.drawImage(R, 0, 0), w(), i("uploading"), V.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", T, "capture.jpg"), await n(O), i("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, w]), D = e.useCallback(async (R) => {
    var T;
    const V = (T = R.target.files) == null ? void 0 : T[0];
    if (!V) return;
    i("uploading");
    const B = new FormData();
    B.append("photo", V, V.name), await n(B), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var V;
    if (a !== "overlayOpen") return;
    (V = g.current) == null || V.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const R = (V) => {
      V.key === "Escape" && C();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [a, C]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const v = ae(cn), _ = a === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  u && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return u && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: y,
      disabled: a === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: r,
        className: U.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: P.join(" "),
        onClick: () => o((R) => !R),
        title: v["js.photoCapture.mirror"],
        "aria-label": v["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: x,
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, dn = {
  "js.photoViewer.alt": "Captured photo"
}, mn = ({ controlId: l }) => {
  const t = q(), n = Ie(), a = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!a) {
      s && (URL.revokeObjectURL(s), c(null));
      return;
    }
    if (i === u.current && s)
      return;
    u.current = i, s && (URL.revokeObjectURL(s), c(null));
    let r = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        r || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      r = !0;
    };
  }, [a, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const o = ae(dn);
  return !a || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, pn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, fn = ({ controlId: l }) => {
  const t = q(), n = Ie(), a = !!t.hasPdf, i = t.dataRevision ?? 0, s = ae(pn), u = n.indexOf("react-api/"), o = u >= 0 ? n.slice(0, u) : n, r = n + "&rev=" + i, m = o + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(r);
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: rt, useRef: Ue } = e, hn = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = a === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), o = !u && s.some((w) => w.collapsed), r = u ? !c : c, m = Ue(null), p = Ue(null), h = Ue(null), g = rt((w, C) => {
    const y = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !r ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : C !== void 0 ? y.flex = `0 0 ${C}px` : y.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (y.minWidth = c ? w.minSize : void 0, y.minHeight = c ? void 0 : w.minSize), y;
  }, [c, u, o, r]), f = rt((w, C) => {
    w.preventDefault();
    const y = m.current;
    if (!y) return;
    const x = s[C], D = s[C + 1], v = y.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    v.forEach((P) => {
      _.push(c ? P.offsetWidth : P.offsetHeight);
    }), h.current = _, p.current = {
      splitterIndex: C,
      startPos: c ? w.clientX : w.clientY,
      startSizeBefore: _[C],
      startSizeAfter: _[C + 1],
      childBefore: x,
      childAfter: D
    };
    const E = (P) => {
      const R = p.current;
      if (!R || !h.current) return;
      const B = (c ? P.clientX : P.clientY) - R.startPos, T = R.childBefore.minSize || 0, O = R.childAfter.minSize || 0;
      let J = R.startSizeBefore + B, $ = R.startSizeAfter - B;
      J < T && ($ += J - T, J = T), $ < O && (J += $ - O, $ = O), h.current[R.splitterIndex] = J, h.current[R.splitterIndex + 1] = $;
      const A = y.querySelectorAll(":scope > .tlSplitPanel__child"), M = A[R.splitterIndex], I = A[R.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${J}px`), I && (I.style.flex = `0 0 ${$}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const P = {};
        s.forEach((R, V) => {
          const B = R.control;
          B != null && B.controlId && h.current && (P[B.controlId] = h.current[V]);
        }), n("updateSizes", { sizes: P });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", U), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((w, C) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${w.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(w)
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control })
      )
    ), i && C < s.length - 1) {
      const y = s[C + 1];
      !w.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (D) => f(D, C)
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
        flexDirection: r ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, { useCallback: ze } = e, bn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, _n = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), vn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), En = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), gn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), yn = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(bn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, r = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", h = s === "HIDDEN", g = ze(() => {
    n("toggleMinimize");
  }, [n]), f = ze(() => {
    n("toggleMaximize");
  }, [n]), S = ze(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const w = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${r ? " tlPanel--fullLine" : ""}`,
      style: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: m ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(vn, null) : /* @__PURE__ */ e.createElement(_n, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(gn, null) : /* @__PURE__ */ e.createElement(En, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Cn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, wn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, kn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ue, useState: Fe, useEffect: We, useRef: He } = e, Sn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Je(l, t, n, a) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: a });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: a }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...Je(s.children, t, n, s.id)));
  return i;
}
const Te = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(be, { encoded: l, className: "tlSidebar__icon" }) : null, Nn = ({ item: l, active: t, collapsed: n, onSelect: a, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => a(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Te, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Tn = ({ item: l, collapsed: t, onExecute: n, tabIndex: a, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: a,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Rn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Te, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Ln = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Dn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: a, onExecute: i, onClose: s }) => {
  const c = He(null);
  We(() => {
    const r = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [s]), We(() => {
    const r = (m) => {
      m.key === "Escape" && s();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [s]);
  const u = ue((r) => {
    r.type === "nav" ? (a(r.id), s()) : r.type === "command" && (i(r.id), s());
  }, [a, i, s]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((r) => {
    if (r.type === "nav" && r.hidden) return null;
    if (r.type === "nav" || r.type === "command") {
      const m = r.type === "nav" && r.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: r.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(r)
        },
        /* @__PURE__ */ e.createElement(Te, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, xn = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: a,
  onSelect: i,
  onExecute: s,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: o,
  onFocus: r,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: g,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const w = He(null), [C, y] = Fe(null), x = ue(() => {
    a ? g === l.id ? S() : (w.current && y(w.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [a, g, l.id, c, f, S]), D = ue((_) => {
    w.current = _, o(_);
  }, [o]), v = a && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
      title: a ? l.label : void 0,
      "aria-expanded": a ? v : t,
      tabIndex: u,
      ref: D,
      onFocus: () => r(l.id)
    },
    /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
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
    Dn,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Ct,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: a,
      onSelect: i,
      onExecute: s,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: p,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: f,
      onCloseFlyout: S
    }
  ))));
}, Ct = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: a,
  onExecute: i,
  onToggleGroup: s,
  focusedId: c,
  setItemRef: u,
  onItemFocus: o,
  groupStates: r,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        Nn,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: a,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Tn,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Rn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ln, null);
    case "group": {
      const g = r ? r.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        xn,
        {
          item: l,
          expanded: g,
          activeItemId: t,
          collapsed: n,
          onSelect: a,
          onExecute: i,
          onToggleGroup: s,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o,
          focusedId: c,
          setItemRef: u,
          onItemFocus: o,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, In = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(Sn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [r, m] = Fe(() => {
    const T = /* @__PURE__ */ new Map(), O = (J) => {
      for (const $ of J)
        $.type === "group" && (T.set($.id, $.expanded), O($.children));
    };
    return O(i), T;
  }), p = ue((T) => {
    m((O) => {
      const J = new Map(O), $ = J.get(T) ?? !1;
      return J.set(T, !$), n("toggleGroup", { itemId: T, expanded: !$ }), J;
    });
  }, [n]), h = ue((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), g = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [w, C] = Fe(null), y = ue((T) => {
    C(T);
  }, []), x = ue(() => {
    C(null);
  }, []);
  We(() => {
    o || C(null);
  }, [o]);
  const [D, v] = Fe(() => {
    const T = Je(i, o, r);
    return T.length > 0 ? T[0].id : "";
  }), _ = He(/* @__PURE__ */ new Map()), E = ue((T) => (O) => {
    O ? _.current.set(T, O) : _.current.delete(T);
  }, []), U = ue((T) => {
    v(T);
  }, []), P = He(0), R = ue((T) => {
    v(T), P.current++;
  }, []);
  We(() => {
    const T = _.current.get(D);
    T && document.activeElement !== T && T.focus();
  }, [D, P.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), x();
      return;
    }
    const O = Je(i, o, r);
    if (O.length === 0) return;
    const J = O.findIndex((A) => A.id === D);
    if (J < 0) return;
    const $ = O[J];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const A = (J + 1) % O.length;
        R(O[A].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const A = (J - 1 + O.length) % O.length;
        R(O[A].id);
        break;
      }
      case "Home": {
        T.preventDefault(), R(O[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), R(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), $.type === "nav" ? h($.id) : $.type === "command" ? g($.id) : $.type === "group" && (o ? w === $.id ? x() : y($.id) : p($.id));
        break;
      }
      case "ArrowRight": {
        $.type === "group" && !o && ((r.get($.id) ?? !1) || (T.preventDefault(), p($.id)));
        break;
      }
      case "ArrowLeft": {
        $.type === "group" && !o && (r.get($.id) ?? !1) && (T.preventDefault(), p($.id));
        break;
      }
    }
  }, [
    i,
    o,
    r,
    D,
    w,
    R,
    h,
    g,
    p,
    y,
    x
  ]), B = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, i.map((T) => /* @__PURE__ */ e.createElement(
    Ct,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: o,
      onSelect: h,
      onExecute: g,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: E,
      onItemFocus: U,
      groupStates: r,
      flyoutGroupId: w,
      onOpenFlyout: y,
      onCloseFlyout: x
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: o ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: o ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Mn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", a = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], o = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${a}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: o }, u.map((r, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: r })));
}, jn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, Pn = ({ controlId: l }) => {
  const t = q(), n = t.columns, a = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return a ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${a}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: u })));
}, Bn = ({ controlId: l }) => {
  const t = q(), n = t.title, a = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${a}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(K, { control: c })));
}, An = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", a = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((r, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: r }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((r, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: r }))));
}, { useCallback: On } = e, $n = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.items ?? [], i = On((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, a.map((s, c) => {
    const u = c === a.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, c > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => i(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: Fn } = e, Wn = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.items ?? [], i = t.activeItemId, s = Fn((c) => {
    c !== i && n("selectItem", { itemId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, a.map((c) => {
    const u = c.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: c.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(c.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + c.icon, "aria-hidden": "true" }), c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, c.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, c.label)
    );
  }));
}, { useCallback: st, useEffect: ct, useRef: Hn } = e, Un = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Hn(null), u = st(() => {
    n("close");
  }, [n]), o = st((r) => {
    i && r.target === r.currentTarget && u();
  }, [i, u]);
  return ct(() => {
    if (!a) return;
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [a, u]), ct(() => {
    a && c.current && c.current.focus();
  }, [a]), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: o,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: s })
  ) : null;
}, { useEffect: zn, useRef: Vn } = e, Kn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], a = Vn(n.length);
  return zn(() => {
    n.length < a.current && n.length > 0, a.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(K, { key: i.controlId, control: i })));
}, { useCallback: je, useRef: we, useState: Pe } = e, Yn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Gn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Xn = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(Yn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, r = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [g, f] = Pe(null), [S, w] = Pe(null), [C, y] = Pe(null), x = we(null), [D, v] = Pe(!1), _ = we(null), E = we(null), U = we(null), P = we(null), R = we(null), V = je(() => {
    n("close");
  }, [n]), B = je((A, M) => {
    M.preventDefault();
    const I = P.current;
    if (!I) return;
    const Y = I.getBoundingClientRect(), d = !x.current, k = x.current ?? { x: Y.left, y: Y.top };
    d && (x.current = k, y(k)), R.current = {
      dir: A,
      startX: M.clientX,
      startY: M.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const z = (X) => {
      const j = R.current;
      if (!j) return;
      const Q = X.clientX - j.startX, ne = X.clientY - j.startY;
      let ee = j.startW, ie = j.startH, me = 0, de = 0;
      j.symmetric ? (j.dir.includes("e") && (ee = j.startW + 2 * Q), j.dir.includes("w") && (ee = j.startW - 2 * Q), j.dir.includes("s") && (ie = j.startH + 2 * ne), j.dir.includes("n") && (ie = j.startH - 2 * ne)) : (j.dir.includes("e") && (ee = j.startW + Q), j.dir.includes("w") && (ee = j.startW - Q, me = Q), j.dir.includes("s") && (ie = j.startH + ne), j.dir.includes("n") && (ie = j.startH - ne, de = ne));
      const _e = Math.max(200, ee), he = Math.max(100, ie);
      j.symmetric ? (me = (j.startW - _e) / 2, de = (j.startH - he) / 2) : (j.dir.includes("w") && _e === 200 && (me = j.startW - 200), j.dir.includes("n") && he === 100 && (de = j.startH - 100)), E.current = _e, U.current = he, f(_e), w(he);
      const ge = {
        x: j.startPos.x + me,
        y: j.startPos.y + de
      };
      x.current = ge, y(ge);
    }, F = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", F);
      const X = E.current, j = U.current;
      (X != null || j != null) && n("resize", {
        ...X != null ? { width: Math.round(X) + "px" } : {},
        ...j != null ? { height: Math.round(j) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", F);
  }, [n]), T = je((A) => {
    if (A.button !== 0 || A.target.closest("button")) return;
    A.preventDefault();
    const M = P.current;
    if (!M) return;
    const I = M.getBoundingClientRect(), Y = x.current ?? { x: I.left, y: I.top }, d = A.clientX - Y.x, k = A.clientY - Y.y, z = (X) => {
      const j = window.innerWidth, Q = window.innerHeight;
      let ne = X.clientX - d, ee = X.clientY - k;
      const ie = M.offsetWidth, me = M.offsetHeight;
      ne + ie > j && (ne = j - ie), ee + me > Q && (ee = Q - me), ne < 0 && (ne = 0), ee < 0 && (ee = 0);
      const de = { x: ne, y: ee };
      x.current = de, y(de);
    }, F = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", F);
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", F);
  }, []), O = je(() => {
    var A, M;
    if (D) {
      const I = _.current;
      I && (y(I.x !== -1 ? { x: I.x, y: I.y } : null), f(I.w), w(I.h)), v(!1);
    } else {
      const I = P.current, Y = I == null ? void 0 : I.getBoundingClientRect();
      _.current = {
        x: ((A = x.current) == null ? void 0 : A.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((M = x.current) == null ? void 0 : M.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: g ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, v(!0), y({ x: 0, y: 0 }), f(null), w(null);
    }
  }, [D, g, S]), J = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: g != null ? g + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, $ = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: J,
      ref: P,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": $
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : T,
        onDoubleClick: o ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: $ }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      o && /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(K, { control: r })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(K, { control: h }), m.map((A, M) => /* @__PURE__ */ e.createElement(K, { key: M, control: A }))),
    o && !D && Gn.map((A) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${A}`,
        onMouseDown: (M) => B(A, M)
      }
    ))
  );
}, { useCallback: qn, useEffect: Zn } = e, Qn = {
  "js.drawer.close": "Close"
}, Jn = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(Qn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, r = qn(() => {
    n("close");
  }, [n]);
  Zn(() => {
    if (!i) return;
    const p = (h) => {
      h.key === "Escape" && r();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, r]);
  const m = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${c}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: r,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(K, { control: o })));
}, { useCallback: el } = e, tl = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.child, i = el((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, a && /* @__PURE__ */ e.createElement(K, { control: a }));
}, { useCallback: nl, useEffect: ll, useState: al } = e, ol = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [r, m] = al(!1), p = nl(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return ll(() => {
    if (!u || c === 0) return;
    const h = setTimeout(p, c);
    return () => clearTimeout(h);
  }, [u, c, p]), !u && !r ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${r ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, a)
  );
}, { useCallback: Ve, useEffect: Ke, useRef: rl, useState: it } = e, sl = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = rl(null), [r, m] = it({ top: 0, left: 0 }), [p, h] = it(0), g = u.filter((C) => C.type === "item" && !C.disabled);
  Ke(() => {
    var E, U;
    if (!a) return;
    const C = ((E = o.current) == null ? void 0 : E.offsetHeight) ?? 200, y = ((U = o.current) == null ? void 0 : U.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let P = c, R = s;
      P + C > window.innerHeight && (P = Math.max(0, window.innerHeight - C)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), m({ top: P, left: R }), h(0);
      return;
    }
    if (!i) return;
    const x = document.getElementById(i);
    if (!x) return;
    const D = x.getBoundingClientRect();
    let v = D.bottom + 4, _ = D.left;
    v + C > window.innerHeight && (v = D.top - C - 4), _ + y > window.innerWidth && (_ = D.right - y), m({ top: v, left: _ }), h(0);
  }, [a, i, s, c]);
  const f = Ve(() => {
    n("close");
  }, [n]), S = Ve((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  Ke(() => {
    if (!a) return;
    const C = (y) => {
      o.current && !o.current.contains(y.target) && f();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [a, f]);
  const w = Ve((C) => {
    if (C.key === "Escape") {
      f();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), h((y) => (y + 1) % g.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), h((y) => (y - 1 + g.length) % g.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const y = g[p];
      y && S(y.id);
    }
  }, [f, S, g, p]);
  return Ke(() => {
    a && o.current && o.current.focus();
  }, [a]), a ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: r.top, left: r.left },
      onKeyDown: w
    },
    u.map((C, y) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const D = g.indexOf(C) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => S(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + C.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, cl = 768, il = ({ controlId: l }) => {
  const t = q(), n = te();
  e.useEffect(() => {
    const r = window.matchMedia(`(max-width: ${cl}px)`), m = (h) => {
      n("reportDisplayClass", { displayClass: h ? "COMPACT" : "REGULAR" });
    };
    m(r.matches);
    const p = (h) => m(h.matches);
    return r.addEventListener("change", p), () => r.removeEventListener("change", p);
  }, [n]);
  const a = t.header, i = t.content, s = t.footer, c = t.snackbar, u = t.dialogManager, o = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: i })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: s })), /* @__PURE__ */ e.createElement(K, { control: c }), u && /* @__PURE__ */ e.createElement(K, { control: u }), o && /* @__PURE__ */ e.createElement(K, { control: o }));
}, ul = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", a = t.cssClass ?? "", i = t.hasTooltip === !0, s = a ? `tlText ${a}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, dl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, ut = 50, ml = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(dl), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const b = (L) => {
      const W = L.detail;
      let G = W.target;
      for (; G && G !== N; ) {
        const re = G.dataset.row, le = G.dataset.col;
        if (re != null && le != null) {
          W.resolved = { key: re + "|" + le };
          return;
        }
        G = G.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", b), () => N.removeEventListener("tl-tooltip-resolve", b);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, r = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, g = e.useMemo(
    () => s.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [s]
  ), f = r === "multi", S = 40, w = 20, C = e.useRef(null), y = e.useRef(null), x = e.useRef(null), [D, v] = e.useState({}), _ = e.useRef(null), E = e.useRef(!1), U = e.useRef(null), [P, R] = e.useState(null), [V, B] = e.useState(null);
  e.useEffect(() => {
    _.current || v({});
  }, [s]);
  const T = e.useCallback((N) => D[N.name] ?? N.width, [D]), O = e.useMemo(() => {
    const N = [];
    let b = f && p > 0 ? S : 0;
    for (let L = 0; L < p && L < s.length; L++)
      N.push(b), b += T(s[L]);
    return N;
  }, [s, p, f, S, T]), J = c * o, $ = e.useRef(null), A = e.useCallback((N, b, L) => {
    L.preventDefault(), L.stopPropagation(), _.current = { column: N, startX: L.clientX, startWidth: b };
    let W = L.clientX, G = 0;
    const re = () => {
      const oe = _.current;
      if (!oe) return;
      const pe = Math.max(ut, oe.startWidth + (W - oe.startX) + G);
      v((ye) => ({ ...ye, [oe.column]: pe }));
    }, le = () => {
      const oe = y.current, pe = C.current;
      if (!oe || !_.current) return;
      const ye = oe.getBoundingClientRect(), lt = 40, at = 8, Rt = oe.scrollLeft;
      W > ye.right - lt ? oe.scrollLeft += at : W < ye.left + lt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - at));
      const ot = oe.scrollLeft - Rt;
      ot !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), G += ot, re()), $.current = requestAnimationFrame(le);
    };
    $.current = requestAnimationFrame(le);
    const Ce = (oe) => {
      W = oe.clientX, re();
    }, Me = (oe) => {
      document.removeEventListener("mousemove", Ce), document.removeEventListener("mouseup", Me), $.current !== null && (cancelAnimationFrame($.current), $.current = null);
      const pe = _.current;
      if (pe) {
        const ye = Math.max(ut, pe.startWidth + (oe.clientX - pe.startX) + G);
        n("columnResize", { column: pe.column, width: ye }), _.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ce), document.addEventListener("mouseup", Me);
  }, [n]), M = e.useCallback(() => {
    C.current && y.current && (C.current.scrollLeft = y.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const N = y.current;
      if (!N) return;
      const b = N.scrollTop, L = Math.ceil(N.clientHeight / o), W = Math.floor(b / o);
      n("scroll", { start: W, count: L });
    }, 80);
  }, [n, o]), I = e.useCallback((N, b, L) => {
    if (E.current) return;
    let W;
    !b || b === "desc" ? W = "asc" : W = "desc";
    const G = L.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: W, mode: G });
  }, [n]), Y = e.useCallback((N, b) => {
    U.current = N, b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, b) => {
    if (!U.current || U.current === N) {
      R(null);
      return;
    }
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const L = b.currentTarget.getBoundingClientRect(), W = b.clientX < L.left + L.width / 2 ? "left" : "right";
    R({ column: N, side: W });
  }, []), k = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const b = U.current;
    if (!b || !P) {
      U.current = null, R(null);
      return;
    }
    let L = s.findIndex((G) => G.name === P.column);
    if (L < 0) {
      U.current = null, R(null);
      return;
    }
    const W = s.findIndex((G) => G.name === b);
    P.side === "right" && L++, W < L && L--, n("columnReorder", { column: b, targetIndex: L }), U.current = null, R(null);
  }, [s, P, n]), z = e.useCallback(() => {
    U.current = null, R(null);
  }, []), F = e.useCallback((N, b) => {
    b.shiftKey && b.preventDefault(), n("select", {
      rowIndex: N,
      ctrlKey: b.ctrlKey || b.metaKey,
      shiftKey: b.shiftKey
    });
  }, [n]), X = e.useCallback((N, b) => {
    b.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), j = e.useCallback(() => {
    const N = m === c && c > 0;
    n("selectAll", { selected: !N });
  }, [n, m, c]), Q = e.useCallback((N, b, L) => {
    L.stopPropagation(), n("expand", { rowIndex: N, expanded: b });
  }, [n]), ne = e.useCallback((N, b) => {
    b.preventDefault(), B({ x: b.clientX, y: b.clientY, colIdx: N });
  }, []), ee = e.useCallback(() => {
    V && (n("setFrozenColumnCount", { count: V.colIdx + 1 }), B(null));
  }, [V, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), B(null);
  }, [n]);
  e.useEffect(() => {
    if (!V) return;
    const N = () => B(null), b = (L) => {
      L.key === "Escape" && B(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", b), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", b);
    };
  }, [V]);
  const me = e.useCallback((N, b) => {
    b.stopPropagation(), b.preventDefault(), n("openFilter", { column: N });
  }, [n]), de = s.reduce((N, b) => N + T(b), 0) + (f ? S : 0), _e = m === c && c > 0, he = m > 0 && m < c, ge = e.useCallback((N) => {
    N && (N.indeterminate = he);
  }, [he]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (N) => {
        if (!U.current) return;
        N.preventDefault();
        const b = y.current, L = C.current;
        if (!b) return;
        const W = b.getBoundingClientRect(), G = 40, re = 8;
        N.clientX < W.left + G ? b.scrollLeft = Math.max(0, b.scrollLeft - re) : N.clientX > W.right - G && (b.scrollLeft += re), L && (L.scrollLeft = b.scrollLeft);
      },
      onDrop: k
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: C }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          U.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== U.current && R({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ge,
          className: "tlTableView__checkbox",
          checked: _e,
          onChange: j
        }
      )
    ), s.map((N, b) => {
      const L = T(N);
      s.length - 1;
      let W = "tlTableView__headerCell";
      N.sortable && (W += " tlTableView__headerCell--sortable"), P && P.column === N.name && (W += " tlTableView__headerCell--dragOver-" + P.side);
      const G = b < p, re = b === p - 1;
      return G && (W += " tlTableView__headerCell--frozen"), re && (W += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: W,
          style: {
            width: L,
            minWidth: L,
            position: G ? "sticky" : "relative",
            ...G ? { left: O[b], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (le) => I(N.name, N.sortDirection, le) : void 0,
          onContextMenu: (le) => ne(b, le),
          onDragStart: (le) => Y(N.name, le),
          onDragOver: (le) => d(N.name, le),
          onDrop: k,
          onDragEnd: z
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
            onMouseDown: (le) => le.stopPropagation(),
            onClick: (le) => me(N.name, le)
          },
          /* @__PURE__ */ e.createElement("i", { className: N.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", g > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (le) => A(N.name, L, le)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (U.current && s.length > 0) {
            const b = s[s.length - 1];
            b.name !== U.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", R({ column: b.name, side: "right" }));
          }
        },
        onDrop: k
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: y,
        className: "tlTableView__body",
        onScroll: M
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: J, position: "relative", width: de } }, u.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: N.index * o,
            height: o,
            width: de
          },
          onClick: (b) => F(N.index, b)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: S,
              minWidth: S,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (b) => b.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (b) => X(N.index, b),
              tabIndex: -1
            }
          )
        ),
        s.map((b, L) => {
          const W = T(b), G = L === s.length - 1, re = L < p, le = L === p - 1;
          let Ce = "tlTableView__cell";
          re && (Ce += " tlTableView__cell--frozen"), le && (Ce += " tlTableView__cell--frozenLast");
          const Me = h && L === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: b.name,
              className: Ce,
              "data-row": N.id,
              "data-col": b.name,
              style: {
                ...G && !re ? { flex: "1 0 auto", minWidth: W } : { width: W, minWidth: W },
                ...re ? { position: "sticky", left: O[L], zIndex: 2 } : {}
              }
            },
            Me ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * w } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => Q(N.index, !N.expanded, pe)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: N.cells[b.name] })) : /* @__PURE__ */ e.createElement(K, { control: N.cells[b.name] })
          );
        })
      )))
    ),
    V && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: V.y, left: V.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      V.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, pl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, yt = e.createContext(pl), { useMemo: fl, useRef: hl, useState: bl, useEffect: _l } = e, vl = 320, El = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, a = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = hl(null), [o, r] = bl(
    a === "top" ? "top" : "side"
  );
  _l(() => {
    if (a !== "auto") {
      r(a);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((w) => {
      for (const C of w) {
        const x = C.contentRect.width / n;
        r(x < vl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [a, n]);
  const m = fl(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${`${Math.max(16, Math.floor(64 / n))}rem`}, 100%), 1fr))`
  }, g = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(yt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: g, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useCallback: gl } = e, Cl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, yl = ({ controlId: l }) => {
  const t = q(), n = te(), a = ae(Cl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, h = gl(() => {
    n("toggleCollapse");
  }, [n]), g = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    r ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useContext: wl, useState: kl, useCallback: Sl } = e, Nl = ({ controlId: l }) => {
  const t = q(), n = wl(yt), a = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, r = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, g = t.field, f = n.readOnly, [S, w] = kl(!1), C = Sl(() => w((v) => !v), []);
  if (!p) return null;
  const y = s != null, x = c != null && c.length > 0, D = [
    "tlFormField",
    `tlFormField--${r}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    y ? "tlFormField--error" : "",
    !y && x ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    a
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: g })), !f && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlFormField__errorIcon",
      viewBox: "0 0 16 16",
      width: "14",
      height: "14",
      "aria-hidden": "true"
    },
    /* @__PURE__ */ e.createElement("path", { d: "M8 1l7 14H1L8 1z", fill: "none", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "6", x2: "8", y2: "10", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "12", r: "0.8", fill: "currentColor" })
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !y && x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((v, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlFormField__warningIcon",
      viewBox: "0 0 16 16",
      width: "14",
      height: "14",
      "aria-hidden": "true"
    },
    /* @__PURE__ */ e.createElement("path", { d: "M8 1l7 14H1L8 1z", fill: "none", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "6", x2: "8", y2: "10", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "12", r: "0.8", fill: "currentColor" })
  ), /* @__PURE__ */ e.createElement("span", null, v)))), !f && u && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, Tl = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, r = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, r, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", c].filter(Boolean).join(" "), g = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: p,
      "data-tooltip": g
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": g }, m);
}, Rl = 20, Ll = () => {
  const l = q(), t = te(), n = l.nodes ?? [], a = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((v, _) => {
    t(_ ? "collapse" : "expand", { nodeId: v });
  }, [t]), h = e.useCallback((v, _) => {
    t("select", {
      nodeId: v,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), g = e.useCallback((v, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: v, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), S = e.useCallback((v, _) => {
    const E = _.getBoundingClientRect(), U = v.clientY - E.top, P = E.height / 3;
    return U < P ? "above" : U > P * 2 ? "below" : "within";
  }, []), w = e.useCallback((v, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", v);
  }, []), C = e.useCallback((v, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const E = S(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: v, position: E }), f.current = null;
    }, 50);
  }, [t, S]), y = e.useCallback((v, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = S(_, _.currentTarget);
    t("drop", { nodeId: v, position: E });
  }, [t, S]), x = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((v) => {
    if (n.length === 0) return;
    let _ = o;
    switch (v.key) {
      case "ArrowDown":
        v.preventDefault(), _ = Math.min(o + 1, n.length - 1);
        break;
      case "ArrowUp":
        v.preventDefault(), _ = Math.max(o - 1, 0);
        break;
      case "ArrowRight":
        if (v.preventDefault(), o >= 0 && o < n.length) {
          const E = n[o];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (_ = o + 1);
        }
        break;
      case "ArrowLeft":
        if (v.preventDefault(), o >= 0 && o < n.length) {
          const E = n[o];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const U = E.depth;
            for (let P = o - 1; P >= 0; P--)
              if (n[P].depth < U) {
                _ = P;
                break;
              }
          }
        }
        break;
      case "Enter":
        v.preventDefault(), o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: v.ctrlKey || v.metaKey,
          shiftKey: v.shiftKey
        });
        return;
      case " ":
        v.preventDefault(), a === "multi" && o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        v.preventDefault(), _ = 0;
        break;
      case "End":
        v.preventDefault(), _ = n.length - 1;
        break;
      default:
        return;
    }
    _ !== o && r(_);
  }, [o, n, t, a]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: D
    },
    n.map((v, _) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: v.id,
        role: "treeitem",
        "aria-expanded": v.expandable ? v.expanded : void 0,
        "aria-selected": v.selected,
        "aria-level": v.depth + 1,
        className: [
          "tlTreeView__node",
          v.selected ? "tlTreeView__node--selected" : "",
          _ === o ? "tlTreeView__node--focused" : "",
          c === v.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === v.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === v.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: v.depth * Rl },
        draggable: i,
        onClick: (E) => h(v.id, E),
        onContextMenu: (E) => g(v.id, E),
        onDragStart: (E) => w(v.id, E),
        onDragOver: s ? (E) => C(v.id, E) : void 0,
        onDrop: s ? (E) => y(v.id, E) : void 0,
        onDragEnd: x
      },
      v.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(v.id, v.expanded);
          },
          tabIndex: -1,
          "aria-label": v.expanded ? "Collapse" : "Expand"
        },
        v.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: v.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: v.content }))
    ))
  );
};
var Ye = { exports: {} }, se = {}, Ge = { exports: {} }, Z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var dt;
function Dl() {
  if (dt) return Z;
  dt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), a = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
  function g(d) {
    return d === null || typeof d != "object" ? null : (d = h && d[h] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var f = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, S = Object.assign, w = {};
  function C(d, k, z) {
    this.props = d, this.context = k, this.refs = w, this.updater = z || f;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, C.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = C.prototype;
  function x(d, k, z) {
    this.props = d, this.context = k, this.refs = w, this.updater = z || f;
  }
  var D = x.prototype = new y();
  D.constructor = x, S(D, C.prototype), D.isPureReactComponent = !0;
  var v = Array.isArray;
  function _() {
  }
  var E = { H: null, A: null, T: null, S: null }, U = Object.prototype.hasOwnProperty;
  function P(d, k, z) {
    var F = z.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: F !== void 0 ? F : null,
      props: z
    };
  }
  function R(d, k) {
    return P(d.type, k, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(z) {
      return k[z];
    });
  }
  var T = /\/+/g;
  function O(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : k.toString(36);
  }
  function J(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
          function(k) {
            d.status === "pending" && (d.status = "fulfilled", d.value = k);
          },
          function(k) {
            d.status === "pending" && (d.status = "rejected", d.reason = k);
          }
        )), d.status) {
          case "fulfilled":
            return d.value;
          case "rejected":
            throw d.reason;
        }
    }
    throw d;
  }
  function $(d, k, z, F, X) {
    var j = typeof d;
    (j === "undefined" || j === "boolean") && (d = null);
    var Q = !1;
    if (d === null) Q = !0;
    else
      switch (j) {
        case "bigint":
        case "string":
        case "number":
          Q = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              Q = !0;
              break;
            case m:
              return Q = d._init, $(
                Q(d._payload),
                k,
                z,
                F,
                X
              );
          }
      }
    if (Q)
      return X = X(d), Q = F === "" ? "." + O(d, 0) : F, v(X) ? (z = "", Q != null && (z = Q.replace(T, "$&/") + "/"), $(X, k, z, "", function(ie) {
        return ie;
      })) : X != null && (V(X) && (X = R(
        X,
        z + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), k.push(X)), 1;
    Q = 0;
    var ne = F === "" ? "." : F + ":";
    if (v(d))
      for (var ee = 0; ee < d.length; ee++)
        F = d[ee], j = ne + O(F, ee), Q += $(
          F,
          k,
          z,
          j,
          X
        );
    else if (ee = g(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(F = d.next()).done; )
        F = F.value, j = ne + O(F, ee++), Q += $(
          F,
          k,
          z,
          j,
          X
        );
    else if (j === "object") {
      if (typeof d.then == "function")
        return $(
          J(d),
          k,
          z,
          F,
          X
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function A(d, k, z) {
    if (d == null) return d;
    var F = [], X = 0;
    return $(d, F, "", "", function(j) {
      return k.call(z, j, X++);
    }), F;
  }
  function M(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(z) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = z);
        },
        function(z) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = z);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var I = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var k = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(k)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Y = {
    map: A,
    forEach: function(d, k, z) {
      A(
        d,
        function() {
          k.apply(this, arguments);
        },
        z
      );
    },
    count: function(d) {
      var k = 0;
      return A(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return A(d, function(k) {
        return k;
      }) || [];
    },
    only: function(d) {
      if (!V(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return Z.Activity = p, Z.Children = Y, Z.Component = C, Z.Fragment = n, Z.Profiler = i, Z.PureComponent = x, Z.StrictMode = a, Z.Suspense = o, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, Z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(d, k, z) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var F = S({}, d.props), X = d.key;
    if (k != null)
      for (j in k.key !== void 0 && (X = "" + k.key), k)
        !U.call(k, j) || j === "key" || j === "__self" || j === "__source" || j === "ref" && k.ref === void 0 || (F[j] = k[j]);
    var j = arguments.length - 2;
    if (j === 1) F.children = z;
    else if (1 < j) {
      for (var Q = Array(j), ne = 0; ne < j; ne++)
        Q[ne] = arguments[ne + 2];
      F.children = Q;
    }
    return P(d.type, X, F);
  }, Z.createContext = function(d) {
    return d = {
      $$typeof: c,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: s,
      _context: d
    }, d;
  }, Z.createElement = function(d, k, z) {
    var F, X = {}, j = null;
    if (k != null)
      for (F in k.key !== void 0 && (j = "" + k.key), k)
        U.call(k, F) && F !== "key" && F !== "__self" && F !== "__source" && (X[F] = k[F]);
    var Q = arguments.length - 2;
    if (Q === 1) X.children = z;
    else if (1 < Q) {
      for (var ne = Array(Q), ee = 0; ee < Q; ee++)
        ne[ee] = arguments[ee + 2];
      X.children = ne;
    }
    if (d && d.defaultProps)
      for (F in Q = d.defaultProps, Q)
        X[F] === void 0 && (X[F] = Q[F]);
    return P(d, j, X);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = V, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: M
    };
  }, Z.memo = function(d, k) {
    return {
      $$typeof: r,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, Z.startTransition = function(d) {
    var k = E.T, z = {};
    E.T = z;
    try {
      var F = d(), X = E.S;
      X !== null && X(z, F), typeof F == "object" && F !== null && typeof F.then == "function" && F.then(_, I);
    } catch (j) {
      I(j);
    } finally {
      k !== null && z.types !== null && (k.types = z.types), E.T = k;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, Z.use = function(d) {
    return E.H.use(d);
  }, Z.useActionState = function(d, k, z) {
    return E.H.useActionState(d, k, z);
  }, Z.useCallback = function(d, k) {
    return E.H.useCallback(d, k);
  }, Z.useContext = function(d) {
    return E.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, k) {
    return E.H.useDeferredValue(d, k);
  }, Z.useEffect = function(d, k) {
    return E.H.useEffect(d, k);
  }, Z.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, Z.useId = function() {
    return E.H.useId();
  }, Z.useImperativeHandle = function(d, k, z) {
    return E.H.useImperativeHandle(d, k, z);
  }, Z.useInsertionEffect = function(d, k) {
    return E.H.useInsertionEffect(d, k);
  }, Z.useLayoutEffect = function(d, k) {
    return E.H.useLayoutEffect(d, k);
  }, Z.useMemo = function(d, k) {
    return E.H.useMemo(d, k);
  }, Z.useOptimistic = function(d, k) {
    return E.H.useOptimistic(d, k);
  }, Z.useReducer = function(d, k, z) {
    return E.H.useReducer(d, k, z);
  }, Z.useRef = function(d) {
    return E.H.useRef(d);
  }, Z.useState = function(d) {
    return E.H.useState(d);
  }, Z.useSyncExternalStore = function(d, k, z) {
    return E.H.useSyncExternalStore(
      d,
      k,
      z
    );
  }, Z.useTransition = function() {
    return E.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var mt;
function xl() {
  return mt || (mt = 1, Ge.exports = Dl()), Ge.exports;
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
var pt;
function Il() {
  if (pt) return se;
  pt = 1;
  var l = xl();
  function t(o) {
    var r = "https://react.dev/errors/" + o;
    if (1 < arguments.length) {
      r += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        r += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + o + "; visit " + r + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  }, i = Symbol.for("react.portal");
  function s(o, r, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: o,
      containerInfo: r,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, r) {
    if (o === "font") return "";
    if (typeof r == "string")
      return r === "use-credentials" ? r : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = a, se.createPortal = function(o, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return s(o, r, null, m);
  }, se.flushSync = function(o) {
    var r = c.T, m = a.p;
    try {
      if (c.T = null, a.p = 2, o) return o();
    } finally {
      c.T = r, a.p = m, a.d.f();
    }
  }, se.preconnect = function(o, r) {
    typeof o == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, a.d.C(o, r));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && a.d.D(o);
  }, se.preinit = function(o, r) {
    if (typeof o == "string" && r && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin), h = typeof r.integrity == "string" ? r.integrity : void 0, g = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? a.d.S(
        o,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: g
        }
      ) : m === "script" && a.d.X(o, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: g,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, se.preinitModule = function(o, r) {
    if (typeof o == "string")
      if (typeof r == "object" && r !== null) {
        if (r.as == null || r.as === "script") {
          var m = u(
            r.as,
            r.crossOrigin
          );
          a.d.M(o, {
            crossOrigin: m,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && a.d.M(o);
  }, se.preload = function(o, r) {
    if (typeof o == "string" && typeof r == "object" && r !== null && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin);
      a.d.L(o, m, {
        crossOrigin: p,
        integrity: typeof r.integrity == "string" ? r.integrity : void 0,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0,
        type: typeof r.type == "string" ? r.type : void 0,
        fetchPriority: typeof r.fetchPriority == "string" ? r.fetchPriority : void 0,
        referrerPolicy: typeof r.referrerPolicy == "string" ? r.referrerPolicy : void 0,
        imageSrcSet: typeof r.imageSrcSet == "string" ? r.imageSrcSet : void 0,
        imageSizes: typeof r.imageSizes == "string" ? r.imageSizes : void 0,
        media: typeof r.media == "string" ? r.media : void 0
      });
    }
  }, se.preloadModule = function(o, r) {
    if (typeof o == "string")
      if (r) {
        var m = u(r.as, r.crossOrigin);
        a.d.m(o, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else a.d.m(o);
  }, se.requestFormReset = function(o) {
    a.d.r(o);
  }, se.unstable_batchedUpdates = function(o, r) {
    return o(r);
  }, se.useFormState = function(o, r, m) {
    return c.H.useFormState(o, r, m);
  }, se.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var ft;
function Ml() {
  if (ft) return Ye.exports;
  ft = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ye.exports = Il(), Ye.exports;
}
var wt = Ml();
const { useState: ve, useCallback: ce, useRef: Le, useEffect: ke, useMemo: et } = e;
function nt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function jl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: a,
  draggable: i,
  onDragStart: s,
  onDragOver: c,
  onDrop: u,
  onDragEnd: o,
  dragClassName: r
}) {
  const m = ce(
    (p) => {
      p.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (r ? " " + r : ""),
      draggable: i || void 0,
      onDragStart: s,
      onDragOver: c,
      onDrop: u,
      onDragEnd: o
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(nt, { image: l.image }),
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
function Pl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: a,
  onMouseEnter: i,
  id: s
}) {
  const c = ce(() => a(l.value), [a, l.value]), u = et(() => {
    if (!n) return l.label;
    const o = l.label.toLowerCase().indexOf(n.toLowerCase());
    return o < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, o), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(o, o + n.length)), l.label.substring(o + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: c,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(nt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Bl = ({ controlId: l, state: t }) => {
  const n = te(), a = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = s && i && !u && o, g = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = g["js.dropdownSelect.nothingFound"], S = ce(
    (b) => g["js.dropdownSelect.removeChip"].replace("{0}", b),
    [g]
  ), [w, C] = ve(!1), [y, x] = ve(""), [D, v] = ve(-1), [_, E] = ve(!1), [U, P] = ve({}), [R, V] = ve(null), [B, T] = ve(null), [O, J] = ve(null), $ = Le(null), A = Le(null), M = Le(null), I = Le(a);
  I.current = a;
  const Y = Le(-1), d = et(
    () => new Set(a.map((b) => b.value)),
    [a]
  ), k = et(() => {
    let b = m.filter((L) => !d.has(L.value));
    if (y) {
      const L = y.toLowerCase();
      b = b.filter((W) => W.label.toLowerCase().includes(L));
    }
    return b;
  }, [m, d, y]);
  ke(() => {
    y && k.length === 1 ? v(0) : v(-1);
  }, [k.length, y]), ke(() => {
    w && r && A.current && A.current.focus();
  }, [w, r, a]), ke(() => {
    var W, G;
    if (Y.current < 0) return;
    const b = Y.current;
    Y.current = -1;
    const L = (W = $.current) == null ? void 0 : W.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(b, L.length - 1)].focus() : (G = $.current) == null || G.focus();
  }, [a]), ke(() => {
    if (!w) return;
    const b = (L) => {
      $.current && !$.current.contains(L.target) && M.current && !M.current.contains(L.target) && (C(!1), x(""));
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [w]), ke(() => {
    if (!w || !$.current) return;
    const b = $.current.getBoundingClientRect(), L = window.innerHeight - b.bottom, G = L < 300 && b.top > L;
    P({
      left: b.left,
      width: b.width,
      ...G ? { bottom: window.innerHeight - b.top } : { top: b.bottom }
    });
  }, [w]);
  const z = ce(async () => {
    if (!(u || !o) && (C(!0), x(""), v(-1), E(!1), !r))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, o, r, n]), F = ce(() => {
    var b;
    C(!1), x(""), v(-1), (b = $.current) == null || b.focus();
  }, []), X = ce(
    (b) => {
      let L;
      if (i) {
        const W = m.find((G) => G.value === b);
        if (W)
          L = [...I.current, W];
        else
          return;
      } else {
        const W = m.find((G) => G.value === b);
        if (W)
          L = [W];
        else
          return;
      }
      I.current = L, n("valueChanged", { value: L.map((W) => W.value) }), i ? (x(""), v(-1)) : F();
    },
    [i, m, n, F]
  ), j = ce(
    (b) => {
      Y.current = I.current.findIndex((W) => W.value === b);
      const L = I.current.filter((W) => W.value !== b);
      I.current = L, n("valueChanged", { value: L.map((W) => W.value) });
    },
    [n]
  ), Q = ce(
    (b) => {
      b.stopPropagation(), n("valueChanged", { value: [] }), F();
    },
    [n, F]
  ), ne = ce((b) => {
    x(b.target.value);
  }, []), ee = ce(
    (b) => {
      if (!w) {
        if (b.key === "ArrowDown" || b.key === "ArrowUp" || b.key === "Enter" || b.key === " ") {
          if (b.target.tagName === "BUTTON") return;
          b.preventDefault(), b.stopPropagation(), z();
        }
        return;
      }
      switch (b.key) {
        case "ArrowDown":
          b.preventDefault(), b.stopPropagation(), v(
            (L) => L < k.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          b.preventDefault(), b.stopPropagation(), v(
            (L) => L > 0 ? L - 1 : k.length - 1
          );
          break;
        case "Enter":
          b.preventDefault(), b.stopPropagation(), D >= 0 && D < k.length && X(k[D].value);
          break;
        case "Escape":
          b.preventDefault(), b.stopPropagation(), F();
          break;
        case "Tab":
          F();
          break;
        case "Backspace":
          y === "" && i && a.length > 0 && j(a[a.length - 1].value);
          break;
      }
    },
    [
      w,
      z,
      F,
      k,
      D,
      X,
      y,
      i,
      a,
      j
    ]
  ), ie = ce(
    async (b) => {
      b.preventDefault(), E(!1);
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
    },
    [n]
  ), me = ce(
    (b, L) => {
      V(b), L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", String(b));
    },
    []
  ), de = ce(
    (b, L) => {
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", R === null || R === b) {
        T(null), J(null);
        return;
      }
      const W = L.currentTarget.getBoundingClientRect(), G = W.left + W.width / 2, re = L.clientX < G ? "before" : "after";
      T(b), J(re);
    },
    [R]
  ), _e = ce(
    (b) => {
      if (b.preventDefault(), R === null || B === null || O === null || R === B) return;
      const L = [...I.current], [W] = L.splice(R, 1);
      let G = B;
      R < B ? G = O === "before" ? G - 1 : G : G = O === "before" ? G : G + 1, L.splice(G, 0, W), I.current = L, n("valueChanged", { value: L.map((re) => re.value) }), V(null), T(null), J(null);
    },
    [R, B, O, n]
  ), he = ce(() => {
    V(null), T(null), J(null);
  }, []);
  if (ke(() => {
    if (D < 0 || !M.current) return;
    const b = M.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    b && b.scrollIntoView({ block: "nearest" });
  }, [D, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : a.map((b) => /* @__PURE__ */ e.createElement("span", { key: b.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(nt, { image: b.image }), /* @__PURE__ */ e.createElement("span", null, b.label))));
  const ge = !c && a.length > 0 && !u, N = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: U
    },
    (r || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: A,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: ne,
        onKeyDown: ee,
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
      !r && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, g["js.dropdownSelect.error"])),
      r && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      r && k.map((b, L) => /* @__PURE__ */ e.createElement(
        Pl,
        {
          key: b.value,
          id: `${l}-opt-${L}`,
          option: b,
          highlighted: L === D,
          searchTerm: y,
          onSelect: X,
          onMouseEnter: () => v(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: $,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: w ? void 0 : z,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, a.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : a.map((b, L) => {
      let W = "";
      return R === L ? W = "tlDropdownSelect__chip--dragging" : B === L && O === "before" ? W = "tlDropdownSelect__chip--dropBefore" : B === L && O === "after" && (W = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        jl,
        {
          key: b.value,
          option: b,
          removable: !u && (i || !c),
          onRemove: j,
          removeLabel: S(b.label),
          draggable: h,
          onDragStart: h ? (G) => me(L, G) : void 0,
          onDragOver: h ? (G) => de(L, G) : void 0,
          onDrop: h ? _e : void 0,
          onDragEnd: h ? he : void 0,
          dragClassName: h ? W : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, ge && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), N && wt.createPortal(N, document.body));
}, { useCallback: Xe, useRef: Al } = e, kt = "application/x-tl-color", Ol = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: a,
  onSwap: i,
  onReplace: s
}) => {
  const c = Al(null), u = Xe(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Xe((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = Xe(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(kt);
      h ? s(m, h) : c.current !== null && c.current !== m && i(c.current, m), c.current = null;
    },
    [i, s]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => n(m) : void 0,
        onDoubleClick: m != null ? () => a(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: o,
        onDrop: r(p)
      }
    ))
  );
};
function St(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function tt(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Nt(l) {
  if (!tt(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Tt(l, t, n) {
  const a = (i) => St(i).toString(16).padStart(2, "0");
  return "#" + a(l) + a(t) + a(n);
}
function $l(l, t, n) {
  const a = l / 255, i = t / 255, s = n / 255, c = Math.max(a, i, s), u = Math.min(a, i, s), o = c - u;
  let r = 0;
  o !== 0 && (c === a ? r = (i - s) / o % 6 : c === i ? r = (s - a) / o + 2 : r = (a - i) / o + 4, r *= 60, r < 0 && (r += 360));
  const m = c === 0 ? 0 : o / c;
  return [r, m, c];
}
function Fl(l, t, n) {
  const a = n * t, i = a * (1 - Math.abs(l / 60 % 2 - 1)), s = n - a;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = a, u = i, o = 0) : l < 120 ? (c = i, u = a, o = 0) : l < 180 ? (c = 0, u = a, o = i) : l < 240 ? (c = 0, u = i, o = a) : l < 300 ? (c = i, u = 0, o = a) : (c = a, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function Wl(l) {
  return $l(...Nt(l));
}
function qe(l, t, n) {
  return Tt(...Fl(l, t, n));
}
const { useCallback: Se, useRef: ht } = e, Hl = ({ color: l, onColorChange: t }) => {
  const [n, a, i] = Wl(l), s = ht(null), c = ht(null), u = Se(
    (f, S) => {
      var x;
      const w = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!w) return;
      const C = Math.max(0, Math.min(1, (f - w.left) / w.width)), y = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(qe(n, C, y));
    },
    [n, t]
  ), o = Se(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), r = Se(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = Se(
    (f) => {
      var y;
      const S = (y = c.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const C = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(qe(C, a, i));
    },
    [a, i, t]
  ), p = Se(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = Se(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), g = qe(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
      onPointerDown: o,
      onPointerMove: r
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${a * 100}%`, top: `${(1 - i) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: h
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
function Ul(l, t) {
  const n = t.toUpperCase();
  return l.some((a) => a != null && a.toUpperCase() === n);
}
const zl = {
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
}, { useState: Be, useCallback: fe, useEffect: Ze, useRef: Vl, useLayoutEffect: Kl } = e, Yl = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: a,
  defaultPalette: i,
  canReset: s,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: o
}) => {
  const [r, m] = Be("palette"), [p, h] = Be(t), g = Vl(null), f = ae(zl), [S, w] = Be(null);
  Kl(() => {
    if (!l.current || !g.current) return;
    const M = l.current.getBoundingClientRect(), I = g.current.getBoundingClientRect();
    let Y = M.bottom + 4, d = M.left;
    Y + I.height > window.innerHeight && (Y = M.top - I.height - 4), d + I.width > window.innerWidth && (d = Math.max(0, M.right - I.width)), w({ top: Y, left: d });
  }, [l]);
  const C = p != null, [y, x, D] = C ? Nt(p) : [0, 0, 0], [v, _] = Be((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ze(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ze(() => {
    const M = (I) => {
      I.key === "Escape" && u();
    };
    return document.addEventListener("keydown", M), () => document.removeEventListener("keydown", M);
  }, [u]), Ze(() => {
    const M = (Y) => {
      g.current && !g.current.contains(Y.target) && u();
    }, I = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const E = fe(
    (M) => (I) => {
      const Y = parseInt(I.target.value, 10);
      if (isNaN(Y)) return;
      const d = St(Y);
      h(Tt(M === "r" ? d : y, M === "g" ? d : x, M === "b" ? d : D));
    },
    [y, x, D]
  ), U = fe(
    (M) => {
      if (p != null) {
        M.dataTransfer.setData(kt, p.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const I = document.createElement("div");
        I.style.width = "33px", I.style.height = "33px", I.style.backgroundColor = p, I.style.borderRadius = "3px", I.style.border = "1px solid rgba(0,0,0,0.1)", I.style.position = "absolute", I.style.top = "-9999px", document.body.appendChild(I), M.dataTransfer.setDragImage(I, 16, 16), requestAnimationFrame(() => document.body.removeChild(I));
      }
    },
    [p]
  ), P = fe((M) => {
    const I = M.target.value;
    _(I), tt(I) && h(I);
  }, []), R = fe(() => {
    h(null);
  }, []), V = fe((M) => {
    h(M);
  }, []), B = fe(
    (M) => {
      c(M);
    },
    [c]
  ), T = fe(
    (M, I) => {
      const Y = [...n], d = Y[M];
      Y[M] = Y[I], Y[I] = d, o(Y);
    },
    [n, o]
  ), O = fe(
    (M, I) => {
      const Y = [...n];
      Y[M] = I, o(Y);
    },
    [n, o]
  ), J = fe(() => {
    o([...i]);
  }, [i, o]), $ = fe(
    (M) => {
      if (Ul(n, M)) return;
      const I = n.indexOf(null);
      if (I < 0) return;
      const Y = [...n];
      Y[I] = M.toUpperCase(), o(Y);
    },
    [n, o]
  ), A = fe(() => {
    p != null && $(p), c(p);
  }, [p, c, $]);
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
        className: "tlColorInput__tab" + (r === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Ol,
      {
        colors: n,
        columns: a,
        onSelect: V,
        onConfirm: B,
        onSwap: T,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: J }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Hl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: p } : void 0,
        draggable: C,
        onDragStart: C ? U : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? y : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? x : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? D : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !tt(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: P
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: A }, f["js.colorInput.ok"]))
  );
}, Gl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Xl, useCallback: Ae, useRef: ql } = e, Zl = ({ controlId: l, state: t }) => {
  const n = te(), a = ae(Gl), [i, s] = Xl(!1), c = ql(null), u = t.value, o = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, h = Ae(() => {
    o && s(!0);
  }, [o]), g = Ae(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Ae(() => {
    s(!1);
  }, []), S = Ae(
    (w) => {
      n("paletteChanged", { palette: w });
    },
    [n]
  );
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": a["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Yl,
    {
      anchorRef: c,
      currentColor: u,
      palette: r,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: g,
      onCancel: f,
      onPaletteChange: S
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
    }
  );
}, { useState: De, useCallback: Ee, useEffect: Oe, useRef: bt, useLayoutEffect: Ql, useMemo: Jl } = e, ea = {
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
}, ta = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: a,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ae(ea), [o, r] = De("simple"), [m, p] = De(""), [h, g] = De(t ?? ""), [f, S] = De(!1), [w, C] = De(null), y = bt(null), x = bt(null);
  Ql(() => {
    if (!l.current || !y.current) return;
    const B = l.current.getBoundingClientRect(), T = y.current.getBoundingClientRect();
    let O = B.bottom + 4, J = B.left;
    O + T.height > window.innerHeight && (O = B.top - T.height - 4), J + T.width > window.innerWidth && (J = Math.max(0, B.right - T.width)), C({ top: O, left: J });
  }, [l]), Oe(() => {
    !a && !f && c().catch(() => S(!0));
  }, [a, f, c]), Oe(() => {
    a && x.current && x.current.focus();
  }, [a]), Oe(() => {
    const B = (T) => {
      T.key === "Escape" && s();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [s]), Oe(() => {
    const B = (O) => {
      y.current && !y.current.contains(O.target) && s();
    }, T = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const D = Jl(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(B) || T.label.toLowerCase().includes(B) || T.terms != null && T.terms.some((O) => O.includes(B))
    );
  }, [n, m]), v = Ee((B) => {
    p(B.target.value);
  }, []), _ = Ee(
    (B) => {
      i(B);
    },
    [i]
  ), E = Ee((B) => {
    g(B);
  }, []), U = Ee((B) => {
    g(B.target.value);
  }, []), P = Ee(() => {
    i(h || null);
  }, [h, i]), R = Ee(() => {
    i(null);
  }, [i]), V = Ee(async (B) => {
    B.preventDefault(), S(!1);
    try {
      await c();
    } catch {
      S(!0);
    }
  }, [c]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: w ? { top: w.top, left: w.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => r("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => r("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: x,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: v,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
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
      !a && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      a && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      a && D.map(
        (B) => B.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => o === "simple" ? _(T.encoded) : E(T.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), o === "simple" ? _(T.encoded) : E(T.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(be, { encoded: T.encoded })
        ))
      )
    ),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: U
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(be, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: P }, u["js.iconSelect.ok"]))
  );
}, na = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: la, useCallback: $e, useRef: aa } = e, oa = ({ controlId: l, state: t }) => {
  const n = te(), a = ae(na), [i, s] = la(!1), c = aa(null), u = t.value, o = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = $e(() => {
    o && !r && s(!0);
  }, [o, r]), g = $e(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = $e(() => {
    s(!1);
  }, []), S = $e(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: r,
      title: u ?? "",
      "aria-label": a["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(be, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    ta,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: g,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(be, { encoded: u }) : null));
}, { useCallback: Ne, useEffect: ra, useMemo: _t, useRef: sa, useState: Qe } = e, ca = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, ia = [1, 2, 3, 4];
function ua(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const a = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? a * t : a;
}
function da(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let a = 1;
  for (const i of ia)
    n >= i && (a = i);
  return a;
}
function ma(l, t) {
  const n = ca[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function pa(l, t) {
  const n = Math.max(1, t), a = {}, i = (p, h) => !!(a[p] && a[p][h]), s = (p, h) => {
    a[p] || (a[p] = {}), a[p][h] = !0;
  }, c = [];
  let u = 0, o = 0;
  const r = (p) => {
    let h = null;
    for (const f of c) f.rowStart === p && (h = f);
    if (!h) return;
    let g = h.colEnd;
    for (; g < n && !i(p, g); ) g++;
    if (g !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let S = h.colEnd; S < g; S++) s(f, S);
      h.colEnd = g;
    }
  };
  for (const p of l) {
    const h = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(ma(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let x = o; x < n && !i(u, x); x++)
      f++;
    if (g > f) {
      for (r(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let x = o; x < n && !i(u, x); x++)
        f++;
      g = Math.min(g, f);
    }
    const S = o, w = o + g, C = u, y = u + h;
    c.push({ id: p.id, colStart: S, colEnd: w, rowStart: C, rowEnd: y });
    for (let x = C; x < y; x++)
      for (let D = S; D < w; D++) s(x, D);
    o = w, o >= n && (o = 0, u++);
  }
  r(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let h = 0; h < n; h++) {
      if (i(p, h)) continue;
      const g = c.find((f) => f.rowEnd === p && f.colStart <= h && h < f.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let f = g.colStart; f < g.colEnd; f++) s(p, f);
      }
    }
  return c;
}
const fa = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), s = sa(null), [c, u] = Qe(1), o = t.editMode === !0;
  ra(() => {
    const _ = s.current;
    if (!_) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, U = ua(a, E), P = () => u(da(_.clientWidth, U));
    P();
    const R = new ResizeObserver(P);
    return R.observe(_), () => R.disconnect();
  }, [a]);
  const r = _t(() => pa(i, c), [i, c]), m = _t(() => {
    const _ = {};
    for (const E of r) _[E.id] = E;
    return _;
  }, [r]), [p, h] = Qe(null), [g, f] = Qe(null), S = Ne((_, E) => {
    if (!o) {
      _.preventDefault();
      return;
    }
    h(E), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", E);
  }, [o]), w = Ne((_, E) => {
    if (!o || !p || p === E) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const U = _.currentTarget.getBoundingClientRect(), P = _.clientX < U.left + U.width / 2;
    f((R) => R && R.id === E && R.before === P ? R : { id: E, before: P });
  }, [o, p]), C = Ne(() => {
  }, []), y = Ne((_, E, U) => {
    const P = i.map((T) => T.id), R = P.indexOf(_);
    if (R < 0) return;
    P.splice(R, 1);
    const V = P.indexOf(E);
    if (V < 0) {
      P.splice(R, 0, _);
      return;
    }
    const B = U ? V : V + 1;
    P.splice(B, 0, _), n("reorder", { order: P });
  }, [i, n]), x = Ne((_, E) => {
    if (!o || !p || p === E) return;
    _.preventDefault();
    const U = _.currentTarget.getBoundingClientRect(), P = _.clientX < U.left + U.width / 2;
    y(p, E, P), h(null), f(null);
  }, [o, p, y]), D = Ne(() => {
    h(null), f(null);
  }, []), v = {
    display: "grid",
    gridTemplateColumns: `repeat(${c}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (o ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: v }, i.map((_) => {
      const E = m[_.id];
      if (!E) return null;
      const U = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, P = ["tlDashboard__tile"];
      return p === _.id && P.push("tlDashboard__tile--dragging"), g && g.id === _.id && P.push(g.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: P.join(" "),
          style: U,
          draggable: o,
          onDragStart: (R) => S(R, _.id),
          onDragOver: (R) => w(R, _.id),
          onDragLeave: C,
          onDrop: (R) => x(R, _.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(K, { control: _.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ha, useRef: vt, useState: Et, useEffect: gt, useLayoutEffect: ba } = e, _a = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, a) => /* @__PURE__ */ e.createElement("span", { key: a, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, va = ({ group: l }) => {
  var p, h;
  const [t, n] = Et(!1), [a, i] = Et({}), s = vt(null), c = vt(null), u = ha(() => {
    n((g) => !g);
  }, []);
  ba(() => {
    if (!t) return;
    const g = () => {
      const f = s.current;
      if (!f) return;
      const S = f.getBoundingClientRect();
      i({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return g(), window.addEventListener("resize", g), window.addEventListener("scroll", g, !0), () => {
      window.removeEventListener("resize", g), window.removeEventListener("scroll", g, !0);
    };
  }, [t]), gt(() => {
    if (!t) return;
    const g = (f) => {
      c.current && !c.current.contains(f.target) && s.current && !s.current.contains(f.target) && n(!1);
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [t]), gt(() => {
    if (!t) return;
    const g = (f) => {
      f.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", g), () => document.removeEventListener("keydown", g);
  }, [t]);
  const o = l.items.filter((g) => g != null);
  if (o.length === 0) return null;
  if (o.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: o[0] })));
  const r = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? r : void 0,
      title: m ? r : void 0
    },
    m ? /* @__PURE__ */ e.createElement(be, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, r), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), wt.createPortal(
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
      o.map((g, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: g }))),
      (h = l.subGroups) == null ? void 0 : h.map((g, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), g.items.map((S, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: S })))))
    ),
    document.body
  ));
}, Ea = ({ controlId: l }) => {
  const a = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, a.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(va, { group: i }) : /* @__PURE__ */ e.createElement(_a, { group: i }))));
}, ga = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, Ca = ({ controlId: l }) => {
  const t = q(), n = te(), a = t.content, i = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, i && i.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, i.map((s, c) => {
    const u = c === i.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, a && /* @__PURE__ */ e.createElement(K, { control: a })));
}, ya = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((a, i) => /* @__PURE__ */ e.createElement(K, { key: i, control: a })));
}, wa = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), ka = {
  "js.sidebar.openDrawer": "Open navigation"
}, Sa = ({ controlId: l }) => {
  const t = te(), n = ae(ka);
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
H("TLButton", zt);
H("TLUploadButton", Vt);
H("TLToggleButton", Yt);
H("TLTextInput", xt);
H("TLPasswordInput", Mt);
H("TLNumberInput", Pt);
H("TLDatePicker", At);
H("TLSelect", $t);
H("TLCheckbox", Wt);
H("TLTable", Ht);
H("TLCounter", Gt);
H("TLTabBar", qt);
H("TLFieldList", Zt);
H("TLAudioRecorder", Jt);
H("TLAudioPlayer", tn);
H("TLFileUpload", ln);
H("TLBinaryField", on);
H("TLDownload", sn);
H("TLPhotoCapture", un);
H("TLPhotoViewer", mn);
H("TLPdfViewer", fn);
H("TLSplitPanel", hn);
H("TLPanel", yn);
H("TLInset", jn);
H("TLMaximizeRoot", wn);
H("TLDeckPane", kn);
H("TLSidebar", In);
H("TLStack", Mn);
H("TLGrid", Pn);
H("TLCard", Bn);
H("TLAppBar", An);
H("TLBreadcrumb", $n);
H("TLBottomBar", Wn);
H("TLDialog", Un);
H("TLDialogManager", Kn);
H("TLWindow", Xn);
H("TLDrawer", Jn);
H("TLContextMenuRegion", tl);
H("TLSnackbar", ol);
H("TLMenu", sl);
H("TLAppShell", il);
H("TLText", ul);
H("TLTableView", ml);
H("TLFormLayout", El);
H("TLFormGroup", yl);
H("TLFormField", Nl);
H("TLResourceCell", Tl);
H("TLTreeView", Ll);
H("TLDropdownSelect", Bl);
H("TLColorInput", Zl);
H("TLIconSelect", oa);
H("TLDashboard", fa);
H("TLToolbar", Ea);
H("TLTileStack", ga);
H("TLAdaptiveDetail", Ca);
H("TLSlot", ya);
H("TLSlotContent", wa);
H("TLDrawerToggle", Sa);
