import { React as e, useTLFieldValue as Re, getComponent as xt, useTLState as q, useTLCommand as ne, TLChild as V, useTLUpload as et, useI18N as ae, useTLDataUrl as tt, register as z } from "tl-react-bridge";
const { useCallback: Lt } = e, Dt = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), i = Lt(
    (a) => {
      r(a.target.value);
    },
    [r]
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
  const [n, r] = Re(), i = It(
    (a) => {
      r(a.target.value);
    },
    [r]
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
}, { useCallback: Pt } = e, jt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Re(), s = Pt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, o = t.errorMessage, a = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": c || void 0,
      title: c && o ? o : void 0
    }
  ));
}, { useCallback: At } = e, Bt = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), i = At(
    (o) => {
      r(o.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
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
  const [r, i] = Re(), s = Ot(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((b) => b.value === r)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, o = t.hasWarnings === !0, a = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && o ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Ft } = e, Ht = ({ controlId: l, state: t }) => {
  const [n, r] = Re(), i = Ft(
    (o) => {
      r(o.target.checked);
    },
    [r]
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
}, Wt = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? xt(c.cellModule) : void 0, o = i[c.name];
    if (u) {
      const a = { value: o, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + s + "-" + c.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, o != null ? String(o) : "");
  })))));
};
function Ne({ encoded: l, className: t }) {
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
const { useCallback: zt } = e, Ut = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = ne(), o = t ?? "click", a = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, b = s ?? c.displayMode ?? "label-only", E = c.hidden === !0, f = c.tooltip, S = E ? { display: "none" } : void 0, w = c.appearance, g = c.size, y = c.navigateUrl, D = zt(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    u(o);
  }, [u, o, y]), L = b === "icon-only", v = b === "icon-only" || b === "icon-label", _ = b === "label-only" || b === "icon-label" || L && !m, C = f ?? (L ? a : void 0), H = C ? `text:${C}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: D,
      disabled: p,
      style: S,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (g === "small" ? " tlReactButton--small" : "") + (g === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": H,
      "aria-label": L ? a : void 0
    },
    v && m && /* @__PURE__ */ e.createElement(Ne, { encoded: m, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Vt } = e, Kt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = ne(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, m = i ?? s.disabled === !0, p = Vt(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    o
  );
}, Yt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Gt } = e, Xt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.tabs ?? [], i = t.activeTabId, s = Gt((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === i,
      className: "tlReactTabBar__tab" + (c.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(c.id)
    },
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, qt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(V, { control: i })))));
}, Zt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Qt = ({ controlId: l }) => {
  const t = q(), n = et(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, b = m === "received" ? "idle" : r !== "idle" ? r : m, E = e.useCallback(async () => {
    if (r === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (r !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = y, o.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(y, D ? { mimeType: D } : void 0);
        u.current = L, L.ondataavailable = (v) => {
          v.data.size > 0 && o.current.push(v.data);
        }, L.onstop = async () => {
          y.getTracks().forEach((C) => C.stop()), a.current = null;
          const v = new Blob(o.current, { type: L.mimeType || "audio/webm" });
          if (o.current = [], v.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", v, "recording.webm"), await n(_), i("idle");
        }, L.start(), i("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(Zt), S = b === "recording" ? f["js.audioRecorder.stop"] : b === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], w = b === "uploading", g = ["tlAudioRecorder__button"];
  return b === "recording" && g.push("tlAudioRecorder__button--recording"), b === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: E,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Jt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, en = ({ controlId: l }) => {
  const t = q(), n = tt(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
  e.useEffect(() => {
    r ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), c("disabled"));
  }, [r]), e.useEffect(() => {
    i !== a.current && (a.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
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
        const g = await w.blob();
        o.current = URL.createObjectURL(g);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = ae(Jt), b = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], E = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: E,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, tn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, nn = ({ controlId: l }) => {
  const t = q(), n = et(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : r !== "idle" ? r : o, b = e.useCallback(async (v) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", v, v.name), await n(_), i("idle");
  }, [n]), E = e.useCallback((v) => {
    var C;
    const _ = (C = v.target.files) == null ? void 0 : C[0];
    _ && b(_);
  }, [b]), f = e.useCallback(() => {
    var v;
    r !== "uploading" && ((v = u.current) == null || v.click());
  }, [r]), S = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), c(!0);
  }, []), w = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), c(!1);
  }, []), g = e.useCallback((v) => {
    var C;
    if (v.preventDefault(), v.stopPropagation(), c(!1), r === "uploading") return;
    const _ = (C = v.dataTransfer.files) == null ? void 0 : C[0];
    _ && b(_);
  }, [r, b]), y = p === "uploading", D = ae(tn), L = p === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: E,
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
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, ln = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, rn = ({ controlId: l }) => {
  const t = q(), n = tt(), r = ne(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), g = URL.createObjectURL(w), y = document.createElement("a");
        y.href = g, y.download = c, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(g);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), b = ae(ln);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const E = o ? b["js.downloading"] : b["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: o,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, on = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, an = ({ controlId: l }) => {
  const t = q(), n = et(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), b = e.useRef(null), E = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    w(), i("idle");
  }, [w]), y = e.useCallback(async () => {
    var R;
    if (r !== "uploading") {
      if (c(null), !S) {
        (R = b.current) == null || R.click();
        return;
      }
      try {
        const U = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = U, i("overlayOpen");
      } catch (U) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", U), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, S]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const R = a.current, U = p.current;
    if (!R || !U)
      return;
    U.width = R.videoWidth, U.height = R.videoHeight;
    const j = U.getContext("2d");
    j && (j.drawImage(R, 0, 0), w(), i("uploading"), U.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", T, "capture.jpg"), await n($), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), L = e.useCallback(async (R) => {
    var T;
    const U = (T = R.target.files) == null ? void 0 : T[0];
    if (!U) return;
    i("uploading");
    const j = new FormData();
    j.append("photo", U, U.name), await n(j), i("idle"), b.current && (b.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var U;
    if (r !== "overlayOpen") return;
    (U = E.current) == null || U.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const R = (U) => {
      U.key === "Escape" && g();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [r, g]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const v = ae(on), _ = r === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  u && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const M = ["tlPhotoCapture__mirrorBtn"];
  return u && M.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: y,
      disabled: r === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: E,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: g }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: H.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: M.join(" "),
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
        onClick: D,
        title: v["js.photoCapture.capture"],
        "aria-label": v["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: g,
        title: v["js.photoCapture.close"],
        "aria-label": v["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, sn = {
  "js.photoViewer.alt": "Captured photo"
}, cn = ({ controlId: l }) => {
  const t = q(), n = tt(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      s && (URL.revokeObjectURL(s), c(null));
      return;
    }
    if (i === u.current && s)
      return;
    u.current = i, s && (URL.revokeObjectURL(s), c(null));
    let a = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        a || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const o = ae(sn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: at, useRef: He } = e, un = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), o = !u && s.some((w) => w.collapsed), a = u ? !c : c, m = He(null), p = He(null), b = He(null), E = at((w, g) => {
    const y = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !a ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : g !== void 0 ? y.flex = `0 0 ${g}px` : y.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (y.minWidth = c ? w.minSize : void 0, y.minHeight = c ? void 0 : w.minSize), y;
  }, [c, u, o, a]), f = at((w, g) => {
    w.preventDefault();
    const y = m.current;
    if (!y) return;
    const D = s[g], L = s[g + 1], v = y.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    v.forEach((M) => {
      _.push(c ? M.offsetWidth : M.offsetHeight);
    }), b.current = _, p.current = {
      splitterIndex: g,
      startPos: c ? w.clientX : w.clientY,
      startSizeBefore: _[g],
      startSizeAfter: _[g + 1],
      childBefore: D,
      childAfter: L
    };
    const C = (M) => {
      const R = p.current;
      if (!R || !b.current) return;
      const j = (c ? M.clientX : M.clientY) - R.startPos, T = R.childBefore.minSize || 0, $ = R.childAfter.minSize || 0;
      let J = R.startSizeBefore + j, F = R.startSizeAfter - j;
      J < T && (F += J - T, J = T), F < $ && (J += F - $, F = $), b.current[R.splitterIndex] = J, b.current[R.splitterIndex + 1] = F;
      const Z = y.querySelectorAll(":scope > .tlSplitPanel__child"), A = Z[R.splitterIndex], P = Z[R.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${J}px`), P && (P.style.flex = `0 0 ${F}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const M = {};
        s.forEach((R, U) => {
          const j = R.control;
          j != null && j.controlId && b.current && (M[j.controlId] = b.current[U]);
        }), n("updateSizes", { sizes: M });
      }
      b.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", H), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((w, g) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${w.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: E(w)
        },
        /* @__PURE__ */ e.createElement(V, { control: w.control })
      )
    ), i && g < s.length - 1) {
      const y = s[g + 1];
      !w.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (L) => f(L, g)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, { useCallback: We } = e, dn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, mn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), pn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), fn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), hn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), bn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), _n = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(dn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", b = s === "HIDDEN", E = We(() => {
    n("toggleMinimize");
  }, [n]), f = We(() => {
    n("toggleMaximize");
  }, [n]), S = We(() => {
    n("popOut");
  }, [n]);
  if (b)
    return null;
  const w = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(V, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(pn, null) : /* @__PURE__ */ e.createElement(mn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(hn, null) : /* @__PURE__ */ e.createElement(fn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(bn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(V, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(V, { control: t.buttonBar }))
  );
}, vn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(V, { control: t.child })
  );
}, En = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(V, { control: t.activeChild }));
}, { useCallback: ue, useState: Oe, useEffect: $e, useRef: Fe } = e, gn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ze(l, t, n, r) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...Ze(s.children, t, n, s.id)));
  return i;
}
const Te = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Cn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Te, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), yn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), wn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Te, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), kn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Sn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Fe(null);
  $e(() => {
    const a = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [s]), $e(() => {
    const a = (m) => {
      m.key === "Escape" && s();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [s]);
  const u = ue((a) => {
    a.type === "nav" ? (r(a.id), s()) : a.type === "command" && (i(a.id), s());
  }, [r, i, s]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
    if (a.type === "nav" && a.hidden) return null;
    if (a.type === "nav" || a.type === "command") {
      const m = a.type === "nav" && a.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: a.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(a)
        },
        /* @__PURE__ */ e.createElement(Te, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, Nn = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: s,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: o,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: b,
  flyoutGroupId: E,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const w = Fe(null), [g, y] = Oe(null), D = ue(() => {
    r ? E === l.id ? S() : (w.current && y(w.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, E, l.id, c, f, S]), L = ue((_) => {
    w.current = _, o(_);
  }, [o]), v = r && E === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: r ? l.label : void 0,
      "aria-expanded": r ? v : t,
      tabIndex: u,
      ref: L,
      onFocus: () => a(l.id)
    },
    /* @__PURE__ */ e.createElement(Te, { icon: l.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !r && /* @__PURE__ */ e.createElement(
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
    Sn,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Ct,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: p,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: E,
      onOpenFlyout: f,
      onCloseFlyout: S
    }
  ))));
}, Ct = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: s,
  focusedId: c,
  setItemRef: u,
  onItemFocus: o,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: b
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        Cn,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        yn,
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
      return /* @__PURE__ */ e.createElement(wn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(kn, null);
    case "group": {
      const E = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Nn,
        {
          item: l,
          expanded: E,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
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
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, Tn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(gn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, m] = Oe(() => {
    const T = /* @__PURE__ */ new Map(), $ = (J) => {
      for (const F of J)
        F.type === "group" && (T.set(F.id, F.expanded), $(F.children));
    };
    return $(i), T;
  }), p = ue((T) => {
    m(($) => {
      const J = new Map($), F = J.get(T) ?? !1;
      return J.set(T, !F), n("toggleGroup", { itemId: T, expanded: !F }), J;
    });
  }, [n]), b = ue((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), E = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [w, g] = Oe(null), y = ue((T) => {
    g(T);
  }, []), D = ue(() => {
    g(null);
  }, []);
  $e(() => {
    o || g(null);
  }, [o]);
  const [L, v] = Oe(() => {
    const T = Ze(i, o, a);
    return T.length > 0 ? T[0].id : "";
  }), _ = Fe(/* @__PURE__ */ new Map()), C = ue((T) => ($) => {
    $ ? _.current.set(T, $) : _.current.delete(T);
  }, []), H = ue((T) => {
    v(T);
  }, []), M = Fe(0), R = ue((T) => {
    v(T), M.current++;
  }, []);
  $e(() => {
    const T = _.current.get(L);
    T && document.activeElement !== T && T.focus();
  }, [L, M.current]);
  const U = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), D();
      return;
    }
    const $ = Ze(i, o, a);
    if ($.length === 0) return;
    const J = $.findIndex((Z) => Z.id === L);
    if (J < 0) return;
    const F = $[J];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const Z = (J + 1) % $.length;
        R($[Z].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const Z = (J - 1 + $.length) % $.length;
        R($[Z].id);
        break;
      }
      case "Home": {
        T.preventDefault(), R($[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), R($[$.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), F.type === "nav" ? b(F.id) : F.type === "command" ? E(F.id) : F.type === "group" && (o ? w === F.id ? D() : y(F.id) : p(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !o && ((a.get(F.id) ?? !1) || (T.preventDefault(), p(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !o && (a.get(F.id) ?? !1) && (T.preventDefault(), p(F.id));
        break;
      }
    }
  }, [
    i,
    o,
    a,
    L,
    w,
    R,
    b,
    E,
    p,
    y,
    D
  ]), j = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: j }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(V, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, i.map((T) => /* @__PURE__ */ e.createElement(
    Ct,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: o,
      onSelect: b,
      onExecute: E,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: C,
      onItemFocus: H,
      groupStates: a,
      flyoutGroupId: w,
      onOpenFlyout: y,
      onCloseFlyout: D
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: o ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, Rn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], o = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: o }, u.map((a, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: a })));
}, xn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(V, { control: t.child }));
}, Ln = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(V, { key: o, control: u })));
}, Dn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(V, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(V, { control: c })));
}, In = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(V, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: a }))));
}, { useCallback: Mn } = e, Pn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = Mn((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((s, c) => {
    const u = c === r.length - 1;
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
}, { useCallback: jn } = e, An = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = t.activeItemId, s = jn((c) => {
    c !== i && n("selectItem", { itemId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((c) => {
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
}, { useCallback: st, useEffect: ct, useRef: Bn } = e, On = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Bn(null), u = st(() => {
    n("close");
  }, [n]), o = st((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return ct(() => {
    if (!r) return;
    const a = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [r, u]), ct(() => {
    r && c.current && c.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: o,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(V, { control: s })
  ) : null;
}, { useEffect: $n, useRef: Fn } = e, Hn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = Fn(n.length);
  return $n(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(V, { key: i.controlId, control: i })));
}, { useCallback: Ie, useRef: ye, useState: Me } = e, Wn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, zn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Un = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(Wn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbar, b = t.buttonBar, [E, f] = Me(null), [S, w] = Me(null), [g, y] = Me(null), D = ye(null), [L, v] = Me(!1), _ = ye(null), C = ye(null), H = ye(null), M = ye(null), R = ye(null), U = Ie(() => {
    n("close");
  }, [n]), j = Ie((Z, A) => {
    A.preventDefault();
    const P = M.current;
    if (!P) return;
    const Y = P.getBoundingClientRect(), d = !D.current, k = D.current ?? { x: Y.left, y: Y.top };
    d && (D.current = k, y(k)), R.current = {
      dir: Z,
      startX: A.clientX,
      startY: A.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const W = (G) => {
      const I = R.current;
      if (!I) return;
      const Q = G.clientX - I.startX, te = G.clientY - I.startY;
      let ee = I.startW, ie = I.startH, me = 0, de = 0;
      I.symmetric ? (I.dir.includes("e") && (ee = I.startW + 2 * Q), I.dir.includes("w") && (ee = I.startW - 2 * Q), I.dir.includes("s") && (ie = I.startH + 2 * te), I.dir.includes("n") && (ie = I.startH - 2 * te)) : (I.dir.includes("e") && (ee = I.startW + Q), I.dir.includes("w") && (ee = I.startW - Q, me = Q), I.dir.includes("s") && (ie = I.startH + te), I.dir.includes("n") && (ie = I.startH - te, de = te));
      const be = Math.max(200, ee), he = Math.max(100, ie);
      I.symmetric ? (me = (I.startW - be) / 2, de = (I.startH - he) / 2) : (I.dir.includes("w") && be === 200 && (me = I.startW - 200), I.dir.includes("n") && he === 100 && (de = I.startH - 100)), C.current = be, H.current = he, f(be), w(he);
      const Ee = {
        x: I.startPos.x + me,
        y: I.startPos.y + de
      };
      D.current = Ee, y(Ee);
    }, B = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", B);
      const G = C.current, I = H.current;
      (G != null || I != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...I != null ? { height: Math.round(I) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", B);
  }, [n]), T = Ie((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const A = M.current;
    if (!A) return;
    const P = A.getBoundingClientRect(), Y = D.current ?? { x: P.left, y: P.top }, d = Z.clientX - Y.x, k = Z.clientY - Y.y, W = (G) => {
      const I = window.innerWidth, Q = window.innerHeight;
      let te = G.clientX - d, ee = G.clientY - k;
      const ie = A.offsetWidth, me = A.offsetHeight;
      te + ie > I && (te = I - ie), ee + me > Q && (ee = Q - me), te < 0 && (te = 0), ee < 0 && (ee = 0);
      const de = { x: te, y: ee };
      D.current = de, y(de);
    }, B = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", B);
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", B);
  }, []), $ = Ie(() => {
    var Z, A;
    if (L) {
      const P = _.current;
      P && (y(P.x !== -1 ? { x: P.x, y: P.y } : null), f(P.w), w(P.h)), v(!1);
    } else {
      const P = M.current, Y = P == null ? void 0 : P.getBoundingClientRect();
      _.current = {
        x: ((Z = D.current) == null ? void 0 : Z.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((A = D.current) == null ? void 0 : A.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: E ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, v(!0), y({ x: 0, y: 0 }), f(null), w(null);
    }
  }, [L, E, S]), J = L ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: E != null ? E + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, F = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: J,
      ref: M,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": F
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : T,
        onDoubleClick: o ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(V, { control: p })),
      o && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: $,
          title: L ? r["js.window.restore"] : r["js.window.maximize"]
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
          onClick: U,
          title: r["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(V, { control: a })),
    (m.length > 0 || b) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, b && /* @__PURE__ */ e.createElement(V, { control: b }), m.map((Z, A) => /* @__PURE__ */ e.createElement(V, { key: A, control: Z }))),
    o && !L && zn.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (A) => j(Z, A)
      }
    ))
  );
}, { useCallback: Vn, useEffect: Kn } = e, Yn = {
  "js.drawer.close": "Close"
}, Gn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(Yn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Vn(() => {
    n("close");
  }, [n]);
  Kn(() => {
    if (!i) return;
    const p = (b) => {
      b.key === "Escape" && a();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, a]);
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
      onClick: a,
      title: r["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(V, { control: o })));
}, { useCallback: Xn } = e, qn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.child, i = Xn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(V, { control: r }));
}, { useCallback: Zn, useEffect: Qn, useState: Jn } = e, el = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, m] = Jn(!1), p = Zn(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return Qn(() => {
    if (!u || c === 0) return;
    const b = setTimeout(p, c);
    return () => clearTimeout(b);
  }, [u, c, p]), !u && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: ze, useEffect: Ue, useRef: tl, useState: it } = e, nl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = tl(null), [a, m] = it({ top: 0, left: 0 }), [p, b] = it(0), E = u.filter((g) => g.type === "item" && !g.disabled);
  Ue(() => {
    var C, H;
    if (!r) return;
    const g = ((C = o.current) == null ? void 0 : C.offsetHeight) ?? 200, y = ((H = o.current) == null ? void 0 : H.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let M = c, R = s;
      M + g > window.innerHeight && (M = Math.max(0, window.innerHeight - g)), R + y > window.innerWidth && (R = Math.max(0, window.innerWidth - y)), m({ top: M, left: R }), b(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const L = D.getBoundingClientRect();
    let v = L.bottom + 4, _ = L.left;
    v + g > window.innerHeight && (v = L.top - g - 4), _ + y > window.innerWidth && (_ = L.right - y), m({ top: v, left: _ }), b(0);
  }, [r, i, s, c]);
  const f = ze(() => {
    n("close");
  }, [n]), S = ze((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  Ue(() => {
    if (!r) return;
    const g = (y) => {
      o.current && !o.current.contains(y.target) && f();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [r, f]);
  const w = ze((g) => {
    if (g.key === "Escape") {
      f();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), b((y) => (y + 1) % E.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), b((y) => (y - 1 + E.length) % E.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const y = E[p];
      y && S(y.id);
    }
  }, [f, S, E, p]);
  return Ue(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: a.top, left: a.left },
      onKeyDown: w
    },
    u.map((g, y) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const L = E.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, ll = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(V, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(V, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(V, { control: i })), /* @__PURE__ */ e.createElement(V, { control: s }), c && /* @__PURE__ */ e.createElement(V, { control: c }), u && /* @__PURE__ */ e.createElement(V, { control: u }));
}, rl = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, s = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, ol = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter"
}, ut = 50, al = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(ol), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const h = (x) => {
      const O = x.detail;
      let K = O.target;
      for (; K && K !== N; ) {
        const oe = K.dataset.row, le = K.dataset.col;
        if (oe != null && le != null) {
          O.resolved = { key: oe + "|" + le };
          return;
        }
        K = K.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", h), () => N.removeEventListener("tl-tooltip-resolve", h);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, b = t.treeMode ?? !1, E = e.useMemo(
    () => s.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [s]
  ), f = a === "multi", S = 40, w = 20, g = e.useRef(null), y = e.useRef(null), D = e.useRef(null), [L, v] = e.useState({}), _ = e.useRef(null), C = e.useRef(!1), H = e.useRef(null), [M, R] = e.useState(null), [U, j] = e.useState(null);
  e.useEffect(() => {
    _.current || v({});
  }, [s]);
  const T = e.useCallback((N) => L[N.name] ?? N.width, [L]), $ = e.useMemo(() => {
    const N = [];
    let h = f && p > 0 ? S : 0;
    for (let x = 0; x < p && x < s.length; x++)
      N.push(h), h += T(s[x]);
    return N;
  }, [s, p, f, S, T]), J = c * o, F = e.useRef(null), Z = e.useCallback((N, h, x) => {
    x.preventDefault(), x.stopPropagation(), _.current = { column: N, startX: x.clientX, startWidth: h };
    let O = x.clientX, K = 0;
    const oe = () => {
      const re = _.current;
      if (!re) return;
      const pe = Math.max(ut, re.startWidth + (O - re.startX) + K);
      v((Ce) => ({ ...Ce, [re.column]: pe }));
    }, le = () => {
      const re = y.current, pe = g.current;
      if (!re || !_.current) return;
      const Ce = re.getBoundingClientRect(), lt = 40, rt = 8, Rt = re.scrollLeft;
      O > Ce.right - lt ? re.scrollLeft += rt : O < Ce.left + lt && (re.scrollLeft = Math.max(0, re.scrollLeft - rt));
      const ot = re.scrollLeft - Rt;
      ot !== 0 && (pe && (pe.scrollLeft = re.scrollLeft), K += ot, oe()), F.current = requestAnimationFrame(le);
    };
    F.current = requestAnimationFrame(le);
    const ge = (re) => {
      O = re.clientX, oe();
    }, De = (re) => {
      document.removeEventListener("mousemove", ge), document.removeEventListener("mouseup", De), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = _.current;
      if (pe) {
        const Ce = Math.max(ut, pe.startWidth + (re.clientX - pe.startX) + K);
        n("columnResize", { column: pe.column, width: Ce }), _.current = null, C.current = !0, requestAnimationFrame(() => {
          C.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ge), document.addEventListener("mouseup", De);
  }, [n]), A = e.useCallback(() => {
    g.current && y.current && (g.current.scrollLeft = y.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const N = y.current;
      if (!N) return;
      const h = N.scrollTop, x = Math.ceil(N.clientHeight / o), O = Math.floor(h / o);
      n("scroll", { start: O, count: x });
    }, 80);
  }, [n, o]), P = e.useCallback((N, h, x) => {
    if (C.current) return;
    let O;
    !h || h === "desc" ? O = "asc" : O = "desc";
    const K = x.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: O, mode: K });
  }, [n]), Y = e.useCallback((N, h) => {
    H.current = N, h.dataTransfer.effectAllowed = "move", h.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, h) => {
    if (!H.current || H.current === N) {
      R(null);
      return;
    }
    h.preventDefault(), h.dataTransfer.dropEffect = "move";
    const x = h.currentTarget.getBoundingClientRect(), O = h.clientX < x.left + x.width / 2 ? "left" : "right";
    R({ column: N, side: O });
  }, []), k = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const h = H.current;
    if (!h || !M) {
      H.current = null, R(null);
      return;
    }
    let x = s.findIndex((K) => K.name === M.column);
    if (x < 0) {
      H.current = null, R(null);
      return;
    }
    const O = s.findIndex((K) => K.name === h);
    M.side === "right" && x++, O < x && x--, n("columnReorder", { column: h, targetIndex: x }), H.current = null, R(null);
  }, [s, M, n]), W = e.useCallback(() => {
    H.current = null, R(null);
  }, []), B = e.useCallback((N, h) => {
    h.shiftKey && h.preventDefault(), n("select", {
      rowIndex: N,
      ctrlKey: h.ctrlKey || h.metaKey,
      shiftKey: h.shiftKey
    });
  }, [n]), G = e.useCallback((N, h) => {
    h.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), I = e.useCallback(() => {
    const N = m === c && c > 0;
    n("selectAll", { selected: !N });
  }, [n, m, c]), Q = e.useCallback((N, h, x) => {
    x.stopPropagation(), n("expand", { rowIndex: N, expanded: h });
  }, [n]), te = e.useCallback((N, h) => {
    h.preventDefault(), j({ x: h.clientX, y: h.clientY, colIdx: N });
  }, []), ee = e.useCallback(() => {
    U && (n("setFrozenColumnCount", { count: U.colIdx + 1 }), j(null));
  }, [U, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), j(null);
  }, [n]);
  e.useEffect(() => {
    if (!U) return;
    const N = () => j(null), h = (x) => {
      x.key === "Escape" && j(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", h), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", h);
    };
  }, [U]);
  const me = e.useCallback((N, h) => {
    h.stopPropagation(), h.preventDefault(), n("openFilter", { column: N });
  }, [n]), de = s.reduce((N, h) => N + T(h), 0) + (f ? S : 0), be = m === c && c > 0, he = m > 0 && m < c, Ee = e.useCallback((N) => {
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
        if (!H.current) return;
        N.preventDefault();
        const h = y.current, x = g.current;
        if (!h) return;
        const O = h.getBoundingClientRect(), K = 40, oe = 8;
        N.clientX < O.left + K ? h.scrollLeft = Math.max(0, h.scrollLeft - oe) : N.clientX > O.right - K && (h.scrollLeft += oe), x && (x.scrollLeft = h.scrollLeft);
      },
      onDrop: k
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: g }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== H.current && R({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ee,
          className: "tlTableView__checkbox",
          checked: be,
          onChange: I
        }
      )
    ), s.map((N, h) => {
      const x = T(N);
      s.length - 1;
      let O = "tlTableView__headerCell";
      N.sortable && (O += " tlTableView__headerCell--sortable"), M && M.column === N.name && (O += " tlTableView__headerCell--dragOver-" + M.side);
      const K = h < p, oe = h === p - 1;
      return K && (O += " tlTableView__headerCell--frozen"), oe && (O += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: O,
          style: {
            width: x,
            minWidth: x,
            position: K ? "sticky" : "relative",
            ...K ? { left: $[h], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (le) => P(N.name, N.sortDirection, le) : void 0,
          onContextMenu: (le) => te(h, le),
          onDragStart: (le) => Y(N.name, le),
          onDragOver: (le) => d(N.name, le),
          onDrop: k,
          onDragEnd: W
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (N.filterActive ? " tlTableView__filterButton--active" : ""),
            title: r["js.table.filter"],
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
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", E > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (le) => Z(N.name, x, le)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (H.current && s.length > 0) {
            const h = s[s.length - 1];
            h.name !== H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", R({ column: h.name, side: "right" }));
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
        onScroll: A
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
          onClick: (h) => B(N.index, h)
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
            onClick: (h) => h.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (h) => G(N.index, h),
              tabIndex: -1
            }
          )
        ),
        s.map((h, x) => {
          const O = T(h), K = x === s.length - 1, oe = x < p, le = x === p - 1;
          let ge = "tlTableView__cell";
          oe && (ge += " tlTableView__cell--frozen"), le && (ge += " tlTableView__cell--frozenLast");
          const De = b && x === 0, re = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: h.name,
              className: ge,
              "data-row": N.id,
              "data-col": h.name,
              style: {
                ...K && !oe ? { flex: "1 0 auto", minWidth: O } : { width: O, minWidth: O },
                ...oe ? { position: "sticky", left: $[x], zIndex: 2 } : {}
              }
            },
            De ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: re * w } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => Q(N.index, !N.expanded, pe)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(V, { control: N.cells[h.name] })) : /* @__PURE__ */ e.createElement(V, { control: N.cells[h.name] })
          );
        })
      )))
    ),
    U && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: U.y, left: U.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      U.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, sl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, yt = e.createContext(sl), { useMemo: cl, useRef: il, useState: ul, useEffect: dl } = e, ml = 320, pl = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = il(null), [o, a] = ul(
    r === "top" ? "top" : "side"
  );
  dl(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((w) => {
      for (const g of w) {
        const D = g.contentRect.width / n;
        a(D < ml ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const m = cl(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, E = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(yt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: E, style: b, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f }))));
}, { useCallback: fl } = e, hl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, bl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(hl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, b = fl(() => {
    n("toggleCollapse");
  }, [n]), E = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: E }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
      "aria-expanded": !u,
      title: u ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(V, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f }))));
}, { useContext: _l, useState: vl, useCallback: El } = e, gl = ({ controlId: l }) => {
  const t = q(), n = _l(yt), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, b = t.hasTooltip === !0, E = t.field, f = n.readOnly, [S, w] = vl(!1), g = El(() => w((v) => !v), []);
  if (!p) return null;
  const y = s != null, D = c != null && c.length > 0, L = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    y ? "tlFormField--error" : "",
    !y && D ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    r
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: g,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(V, { control: E })), !f && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !y && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((v, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
}, Cl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), b = ["tlResourceCell", c].filter(Boolean).join(" "), E = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: b,
      href: "#",
      onClick: p,
      "data-tooltip": E
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: b, "data-tooltip": E }, m);
}, yl = 20, wl = () => {
  const l = q(), t = ne(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((v, _) => {
    t(_ ? "collapse" : "expand", { nodeId: v });
  }, [t]), b = e.useCallback((v, _) => {
    t("select", {
      nodeId: v,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), E = e.useCallback((v, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: v, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), S = e.useCallback((v, _) => {
    const C = _.getBoundingClientRect(), H = v.clientY - C.top, M = C.height / 3;
    return H < M ? "above" : H > M * 2 ? "below" : "within";
  }, []), w = e.useCallback((v, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", v);
  }, []), g = e.useCallback((v, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const C = S(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: v, position: C }), f.current = null;
    }, 50);
  }, [t, S]), y = e.useCallback((v, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const C = S(_, _.currentTarget);
    t("drop", { nodeId: v, position: C });
  }, [t, S]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), L = e.useCallback((v) => {
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
          const C = n[o];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (_ = o + 1);
        }
        break;
      case "ArrowLeft":
        if (v.preventDefault(), o >= 0 && o < n.length) {
          const C = n[o];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const H = C.depth;
            for (let M = o - 1; M >= 0; M--)
              if (n[M].depth < H) {
                _ = M;
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
        v.preventDefault(), r === "multi" && o >= 0 && o < n.length && t("select", {
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
    _ !== o && a(_);
  }, [o, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: L
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
        style: { paddingLeft: v.depth * yl },
        draggable: i,
        onClick: (C) => b(v.id, C),
        onContextMenu: (C) => E(v.id, C),
        onDragStart: (C) => w(v.id, C),
        onDragOver: s ? (C) => g(v.id, C) : void 0,
        onDrop: s ? (C) => y(v.id, C) : void 0,
        onDragEnd: D
      },
      v.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(v.id, v.expanded);
          },
          tabIndex: -1,
          "aria-label": v.expanded ? "Collapse" : "Expand"
        },
        v.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: v.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(V, { control: v.content }))
    ))
  );
};
var Ve = { exports: {} }, se = {}, Ke = { exports: {} }, X = {};
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
function kl() {
  if (dt) return X;
  dt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), b = Symbol.iterator;
  function E(d) {
    return d === null || typeof d != "object" ? null : (d = b && d[b] || d["@@iterator"], typeof d == "function" ? d : null);
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
  function g(d, k, W) {
    this.props = d, this.context = k, this.refs = w, this.updater = W || f;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, g.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = g.prototype;
  function D(d, k, W) {
    this.props = d, this.context = k, this.refs = w, this.updater = W || f;
  }
  var L = D.prototype = new y();
  L.constructor = D, S(L, g.prototype), L.isPureReactComponent = !0;
  var v = Array.isArray;
  function _() {
  }
  var C = { H: null, A: null, T: null, S: null }, H = Object.prototype.hasOwnProperty;
  function M(d, k, W) {
    var B = W.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: B !== void 0 ? B : null,
      props: W
    };
  }
  function R(d, k) {
    return M(d.type, k, d.props);
  }
  function U(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function j(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(W) {
      return k[W];
    });
  }
  var T = /\/+/g;
  function $(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? j("" + d.key) : k.toString(36);
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
  function F(d, k, W, B, G) {
    var I = typeof d;
    (I === "undefined" || I === "boolean") && (d = null);
    var Q = !1;
    if (d === null) Q = !0;
    else
      switch (I) {
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
              return Q = d._init, F(
                Q(d._payload),
                k,
                W,
                B,
                G
              );
          }
      }
    if (Q)
      return G = G(d), Q = B === "" ? "." + $(d, 0) : B, v(G) ? (W = "", Q != null && (W = Q.replace(T, "$&/") + "/"), F(G, k, W, "", function(ie) {
        return ie;
      })) : G != null && (U(G) && (G = R(
        G,
        W + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), k.push(G)), 1;
    Q = 0;
    var te = B === "" ? "." : B + ":";
    if (v(d))
      for (var ee = 0; ee < d.length; ee++)
        B = d[ee], I = te + $(B, ee), Q += F(
          B,
          k,
          W,
          I,
          G
        );
    else if (ee = E(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(B = d.next()).done; )
        B = B.value, I = te + $(B, ee++), Q += F(
          B,
          k,
          W,
          I,
          G
        );
    else if (I === "object") {
      if (typeof d.then == "function")
        return F(
          J(d),
          k,
          W,
          B,
          G
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, k, W) {
    if (d == null) return d;
    var B = [], G = 0;
    return F(d, B, "", "", function(I) {
      return k.call(W, I, G++);
    }), B;
  }
  function A(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = W);
        },
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = W);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
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
    map: Z,
    forEach: function(d, k, W) {
      Z(
        d,
        function() {
          k.apply(this, arguments);
        },
        W
      );
    },
    count: function(d) {
      var k = 0;
      return Z(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return Z(d, function(k) {
        return k;
      }) || [];
    },
    only: function(d) {
      if (!U(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return X.Activity = p, X.Children = Y, X.Component = g, X.Fragment = n, X.Profiler = i, X.PureComponent = D, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, X.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return C.H.useMemoCache(d);
    }
  }, X.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, X.cacheSignal = function() {
    return null;
  }, X.cloneElement = function(d, k, W) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var B = S({}, d.props), G = d.key;
    if (k != null)
      for (I in k.key !== void 0 && (G = "" + k.key), k)
        !H.call(k, I) || I === "key" || I === "__self" || I === "__source" || I === "ref" && k.ref === void 0 || (B[I] = k[I]);
    var I = arguments.length - 2;
    if (I === 1) B.children = W;
    else if (1 < I) {
      for (var Q = Array(I), te = 0; te < I; te++)
        Q[te] = arguments[te + 2];
      B.children = Q;
    }
    return M(d.type, G, B);
  }, X.createContext = function(d) {
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
  }, X.createElement = function(d, k, W) {
    var B, G = {}, I = null;
    if (k != null)
      for (B in k.key !== void 0 && (I = "" + k.key), k)
        H.call(k, B) && B !== "key" && B !== "__self" && B !== "__source" && (G[B] = k[B]);
    var Q = arguments.length - 2;
    if (Q === 1) G.children = W;
    else if (1 < Q) {
      for (var te = Array(Q), ee = 0; ee < Q; ee++)
        te[ee] = arguments[ee + 2];
      G.children = te;
    }
    if (d && d.defaultProps)
      for (B in Q = d.defaultProps, Q)
        G[B] === void 0 && (G[B] = Q[B]);
    return M(d, I, G);
  }, X.createRef = function() {
    return { current: null };
  }, X.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, X.isValidElement = U, X.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, X.memo = function(d, k) {
    return {
      $$typeof: a,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, X.startTransition = function(d) {
    var k = C.T, W = {};
    C.T = W;
    try {
      var B = d(), G = C.S;
      G !== null && G(W, B), typeof B == "object" && B !== null && typeof B.then == "function" && B.then(_, P);
    } catch (I) {
      P(I);
    } finally {
      k !== null && W.types !== null && (k.types = W.types), C.T = k;
    }
  }, X.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, X.use = function(d) {
    return C.H.use(d);
  }, X.useActionState = function(d, k, W) {
    return C.H.useActionState(d, k, W);
  }, X.useCallback = function(d, k) {
    return C.H.useCallback(d, k);
  }, X.useContext = function(d) {
    return C.H.useContext(d);
  }, X.useDebugValue = function() {
  }, X.useDeferredValue = function(d, k) {
    return C.H.useDeferredValue(d, k);
  }, X.useEffect = function(d, k) {
    return C.H.useEffect(d, k);
  }, X.useEffectEvent = function(d) {
    return C.H.useEffectEvent(d);
  }, X.useId = function() {
    return C.H.useId();
  }, X.useImperativeHandle = function(d, k, W) {
    return C.H.useImperativeHandle(d, k, W);
  }, X.useInsertionEffect = function(d, k) {
    return C.H.useInsertionEffect(d, k);
  }, X.useLayoutEffect = function(d, k) {
    return C.H.useLayoutEffect(d, k);
  }, X.useMemo = function(d, k) {
    return C.H.useMemo(d, k);
  }, X.useOptimistic = function(d, k) {
    return C.H.useOptimistic(d, k);
  }, X.useReducer = function(d, k, W) {
    return C.H.useReducer(d, k, W);
  }, X.useRef = function(d) {
    return C.H.useRef(d);
  }, X.useState = function(d) {
    return C.H.useState(d);
  }, X.useSyncExternalStore = function(d, k, W) {
    return C.H.useSyncExternalStore(
      d,
      k,
      W
    );
  }, X.useTransition = function() {
    return C.H.useTransition();
  }, X.version = "19.2.4", X;
}
var mt;
function Sl() {
  return mt || (mt = 1, Ke.exports = kl()), Ke.exports;
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
function Nl() {
  if (pt) return se;
  pt = 1;
  var l = Sl();
  function t(o) {
    var a = "https://react.dev/errors/" + o;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + o + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var r = {
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
  function s(o, a, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: o,
      containerInfo: a,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, a) {
    if (o === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(o, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return s(o, a, null, m);
  }, se.flushSync = function(o) {
    var a = c.T, m = r.p;
    try {
      if (c.T = null, r.p = 2, o) return o();
    } finally {
      c.T = a, r.p = m, r.d.f();
    }
  }, se.preconnect = function(o, a) {
    typeof o == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, r.d.C(o, a));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && r.d.D(o);
  }, se.preinit = function(o, a) {
    if (typeof o == "string" && a && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin), b = typeof a.integrity == "string" ? a.integrity : void 0, E = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? r.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: b,
          fetchPriority: E
        }
      ) : m === "script" && r.d.X(o, {
        crossOrigin: p,
        integrity: b,
        fetchPriority: E,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, se.preinitModule = function(o, a) {
    if (typeof o == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = u(
            a.as,
            a.crossOrigin
          );
          r.d.M(o, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && r.d.M(o);
  }, se.preload = function(o, a) {
    if (typeof o == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin);
      r.d.L(o, m, {
        crossOrigin: p,
        integrity: typeof a.integrity == "string" ? a.integrity : void 0,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0,
        type: typeof a.type == "string" ? a.type : void 0,
        fetchPriority: typeof a.fetchPriority == "string" ? a.fetchPriority : void 0,
        referrerPolicy: typeof a.referrerPolicy == "string" ? a.referrerPolicy : void 0,
        imageSrcSet: typeof a.imageSrcSet == "string" ? a.imageSrcSet : void 0,
        imageSizes: typeof a.imageSizes == "string" ? a.imageSizes : void 0,
        media: typeof a.media == "string" ? a.media : void 0
      });
    }
  }, se.preloadModule = function(o, a) {
    if (typeof o == "string")
      if (a) {
        var m = u(a.as, a.crossOrigin);
        r.d.m(o, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else r.d.m(o);
  }, se.requestFormReset = function(o) {
    r.d.r(o);
  }, se.unstable_batchedUpdates = function(o, a) {
    return o(a);
  }, se.useFormState = function(o, a, m) {
    return c.H.useFormState(o, a, m);
  }, se.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var ft;
function Tl() {
  if (ft) return Ve.exports;
  ft = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ve.exports = Nl(), Ve.exports;
}
var wt = Tl();
const { useState: _e, useCallback: ce, useRef: xe, useEffect: we, useMemo: Qe } = e;
function nt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Rl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: s,
  onDragOver: c,
  onDrop: u,
  onDragEnd: o,
  dragClassName: a
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
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
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
        "aria-label": r
      },
      "×"
    )
  );
}
function xl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const c = ce(() => r(l.value), [r, l.value]), u = Qe(() => {
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
const Ll = ({ controlId: l, state: t }) => {
  const n = ne(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", b = s && i && !u && o, E = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = E["js.dropdownSelect.nothingFound"], S = ce(
    (h) => E["js.dropdownSelect.removeChip"].replace("{0}", h),
    [E]
  ), [w, g] = _e(!1), [y, D] = _e(""), [L, v] = _e(-1), [_, C] = _e(!1), [H, M] = _e({}), [R, U] = _e(null), [j, T] = _e(null), [$, J] = _e(null), F = xe(null), Z = xe(null), A = xe(null), P = xe(r);
  P.current = r;
  const Y = xe(-1), d = Qe(
    () => new Set(r.map((h) => h.value)),
    [r]
  ), k = Qe(() => {
    let h = m.filter((x) => !d.has(x.value));
    if (y) {
      const x = y.toLowerCase();
      h = h.filter((O) => O.label.toLowerCase().includes(x));
    }
    return h;
  }, [m, d, y]);
  we(() => {
    y && k.length === 1 ? v(0) : v(-1);
  }, [k.length, y]), we(() => {
    w && a && Z.current && Z.current.focus();
  }, [w, a, r]), we(() => {
    var O, K;
    if (Y.current < 0) return;
    const h = Y.current;
    Y.current = -1;
    const x = (O = F.current) == null ? void 0 : O.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(h, x.length - 1)].focus() : (K = F.current) == null || K.focus();
  }, [r]), we(() => {
    if (!w) return;
    const h = (x) => {
      F.current && !F.current.contains(x.target) && A.current && !A.current.contains(x.target) && (g(!1), D(""));
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [w]), we(() => {
    if (!w || !F.current) return;
    const h = F.current.getBoundingClientRect(), x = window.innerHeight - h.bottom, K = x < 300 && h.top > x;
    M({
      left: h.left,
      width: h.width,
      ...K ? { bottom: window.innerHeight - h.top } : { top: h.bottom }
    });
  }, [w]);
  const W = ce(async () => {
    if (!(u || !o) && (g(!0), D(""), v(-1), C(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, o, a, n]), B = ce(() => {
    var h;
    g(!1), D(""), v(-1), (h = F.current) == null || h.focus();
  }, []), G = ce(
    (h) => {
      let x;
      if (i) {
        const O = m.find((K) => K.value === h);
        if (O)
          x = [...P.current, O];
        else
          return;
      } else {
        const O = m.find((K) => K.value === h);
        if (O)
          x = [O];
        else
          return;
      }
      P.current = x, n("valueChanged", { value: x.map((O) => O.value) }), i ? (D(""), v(-1)) : B();
    },
    [i, m, n, B]
  ), I = ce(
    (h) => {
      Y.current = P.current.findIndex((O) => O.value === h);
      const x = P.current.filter((O) => O.value !== h);
      P.current = x, n("valueChanged", { value: x.map((O) => O.value) });
    },
    [n]
  ), Q = ce(
    (h) => {
      h.stopPropagation(), n("valueChanged", { value: [] }), B();
    },
    [n, B]
  ), te = ce((h) => {
    D(h.target.value);
  }, []), ee = ce(
    (h) => {
      if (!w) {
        if (h.key === "ArrowDown" || h.key === "ArrowUp" || h.key === "Enter" || h.key === " ") {
          if (h.target.tagName === "BUTTON") return;
          h.preventDefault(), h.stopPropagation(), W();
        }
        return;
      }
      switch (h.key) {
        case "ArrowDown":
          h.preventDefault(), h.stopPropagation(), v(
            (x) => x < k.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          h.preventDefault(), h.stopPropagation(), v(
            (x) => x > 0 ? x - 1 : k.length - 1
          );
          break;
        case "Enter":
          h.preventDefault(), h.stopPropagation(), L >= 0 && L < k.length && G(k[L].value);
          break;
        case "Escape":
          h.preventDefault(), h.stopPropagation(), B();
          break;
        case "Tab":
          B();
          break;
        case "Backspace":
          y === "" && i && r.length > 0 && I(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      W,
      B,
      k,
      L,
      G,
      y,
      i,
      r,
      I
    ]
  ), ie = ce(
    async (h) => {
      h.preventDefault(), C(!1);
      try {
        await n("loadOptions");
      } catch {
        C(!0);
      }
    },
    [n]
  ), me = ce(
    (h, x) => {
      U(h), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(h));
    },
    []
  ), de = ce(
    (h, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", R === null || R === h) {
        T(null), J(null);
        return;
      }
      const O = x.currentTarget.getBoundingClientRect(), K = O.left + O.width / 2, oe = x.clientX < K ? "before" : "after";
      T(h), J(oe);
    },
    [R]
  ), be = ce(
    (h) => {
      if (h.preventDefault(), R === null || j === null || $ === null || R === j) return;
      const x = [...P.current], [O] = x.splice(R, 1);
      let K = j;
      R < j ? K = $ === "before" ? K - 1 : K : K = $ === "before" ? K : K + 1, x.splice(K, 0, O), P.current = x, n("valueChanged", { value: x.map((oe) => oe.value) }), U(null), T(null), J(null);
    },
    [R, j, $, n]
  ), he = ce(() => {
    U(null), T(null), J(null);
  }, []);
  if (we(() => {
    if (L < 0 || !A.current) return;
    const h = A.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    h && h.scrollIntoView({ block: "nearest" });
  }, [L, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((h) => /* @__PURE__ */ e.createElement("span", { key: h.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(nt, { image: h.image }), /* @__PURE__ */ e.createElement("span", null, h.label))));
  const Ee = !c && r.length > 0 && !u, N = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (a || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: te,
        onKeyDown: ee,
        placeholder: E["js.dropdownSelect.filterPlaceholder"],
        "aria-label": E["js.dropdownSelect.filterPlaceholder"],
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
      !a && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, E["js.dropdownSelect.error"])),
      a && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      a && k.map((h, x) => /* @__PURE__ */ e.createElement(
        xl,
        {
          key: h.value,
          id: `${l}-opt-${x}`,
          option: h,
          highlighted: x === L,
          searchTerm: y,
          onSelect: G,
          onMouseEnter: () => v(x)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: F,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: w ? void 0 : W,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((h, x) => {
      let O = "";
      return R === x ? O = "tlDropdownSelect__chip--dragging" : j === x && $ === "before" ? O = "tlDropdownSelect__chip--dropBefore" : j === x && $ === "after" && (O = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Rl,
        {
          key: h.value,
          option: h,
          removable: !u && (i || !c),
          onRemove: I,
          removeLabel: S(h.label),
          draggable: b,
          onDragStart: b ? (K) => me(x, K) : void 0,
          onDragOver: b ? (K) => de(x, K) : void 0,
          onDrop: b ? be : void 0,
          onDragEnd: b ? he : void 0,
          dragClassName: b ? O : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Ee && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": E["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), N && wt.createPortal(N, document.body));
}, { useCallback: Ye, useRef: Dl } = e, kt = "application/x-tl-color", Il = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Dl(null), u = Ye(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ye((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = Ye(
    (m) => (p) => {
      p.preventDefault();
      const b = p.dataTransfer.getData(kt);
      b ? s(m, b) : c.current !== null && c.current !== m && i(c.current, m), c.current = null;
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
        onDoubleClick: m != null ? () => r(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: o,
        onDrop: a(p)
      }
    ))
  );
};
function St(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Je(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Nt(l) {
  if (!Je(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Tt(l, t, n) {
  const r = (i) => St(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Ml(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const m = c === 0 ? 0 : o / c;
  return [a, m, c];
}
function Pl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function jl(l) {
  return Ml(...Nt(l));
}
function Ge(l, t, n) {
  return Tt(...Pl(l, t, n));
}
const { useCallback: ke, useRef: ht } = e, Al = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = jl(l), s = ht(null), c = ht(null), u = ke(
    (f, S) => {
      var D;
      const w = (D = s.current) == null ? void 0 : D.getBoundingClientRect();
      if (!w) return;
      const g = Math.max(0, Math.min(1, (f - w.left) / w.width)), y = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(Ge(n, g, y));
    },
    [n, t]
  ), o = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), a = ke(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = ke(
    (f) => {
      var y;
      const S = (y = c.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const g = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Ge(g, r, i));
    },
    [r, i, t]
  ), p = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), b = ke(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), E = Ge(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: E },
      onPointerDown: o,
      onPointerMove: a
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${r * 100}%`, top: `${(1 - i) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: b
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
function Bl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Ol = {
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
}, { useState: Pe, useCallback: fe, useEffect: Xe, useRef: $l, useLayoutEffect: Fl } = e, Hl = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: s,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: o
}) => {
  const [a, m] = Pe("palette"), [p, b] = Pe(t), E = $l(null), f = ae(Ol), [S, w] = Pe(null);
  Fl(() => {
    if (!l.current || !E.current) return;
    const A = l.current.getBoundingClientRect(), P = E.current.getBoundingClientRect();
    let Y = A.bottom + 4, d = A.left;
    Y + P.height > window.innerHeight && (Y = A.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, A.right - P.width)), w({ top: Y, left: d });
  }, [l]);
  const g = p != null, [y, D, L] = g ? Nt(p) : [0, 0, 0], [v, _] = Pe((p == null ? void 0 : p.toUpperCase()) ?? "");
  Xe(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Xe(() => {
    const A = (P) => {
      P.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Xe(() => {
    const A = (Y) => {
      E.current && !E.current.contains(Y.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const C = fe(
    (A) => (P) => {
      const Y = parseInt(P.target.value, 10);
      if (isNaN(Y)) return;
      const d = St(Y);
      b(Tt(A === "r" ? d : y, A === "g" ? d : D, A === "b" ? d : L));
    },
    [y, D, L]
  ), H = fe(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(kt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), M = fe((A) => {
    const P = A.target.value;
    _(P), Je(P) && b(P);
  }, []), R = fe(() => {
    b(null);
  }, []), U = fe((A) => {
    b(A);
  }, []), j = fe(
    (A) => {
      c(A);
    },
    [c]
  ), T = fe(
    (A, P) => {
      const Y = [...n], d = Y[A];
      Y[A] = Y[P], Y[P] = d, o(Y);
    },
    [n, o]
  ), $ = fe(
    (A, P) => {
      const Y = [...n];
      Y[A] = P, o(Y);
    },
    [n, o]
  ), J = fe(() => {
    o([...i]);
  }, [i, o]), F = fe(
    (A) => {
      if (Bl(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const Y = [...n];
      Y[P] = A.toUpperCase(), o(Y);
    },
    [n, o]
  ), Z = fe(() => {
    p != null && F(p), c(p);
  }, [p, c, F]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: E,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Il,
      {
        colors: n,
        columns: r,
        onSelect: U,
        onConfirm: j,
        onSwap: T,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: J }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Al, { color: p ?? "#000000", onColorChange: b }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (g ? "" : " tlColorInput--noColor"),
        style: g ? { backgroundColor: p } : void 0,
        draggable: g,
        onDragStart: g ? H : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? y : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? D : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? L : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !Je(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: M
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, f["js.colorInput.ok"]))
  );
}, Wl = { "js.colorInput.chooseColor": "Choose color" }, { useState: zl, useCallback: je, useRef: Ul } = e, Vl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Wl), [i, s] = zl(!1), c = Ul(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, b = je(() => {
    o && s(!0);
  }, [o]), E = je(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = je(() => {
    s(!1);
  }, []), S = je(
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
      onClick: b,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Hl,
    {
      anchorRef: c,
      currentColor: u,
      palette: a,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: E,
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
}, { useState: Le, useCallback: ve, useEffect: Ae, useRef: bt, useLayoutEffect: Kl, useMemo: Yl } = e, Gl = {
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
}, Xl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ae(Gl), [o, a] = Le("simple"), [m, p] = Le(""), [b, E] = Le(t ?? ""), [f, S] = Le(!1), [w, g] = Le(null), y = bt(null), D = bt(null);
  Kl(() => {
    if (!l.current || !y.current) return;
    const j = l.current.getBoundingClientRect(), T = y.current.getBoundingClientRect();
    let $ = j.bottom + 4, J = j.left;
    $ + T.height > window.innerHeight && ($ = j.top - T.height - 4), J + T.width > window.innerWidth && (J = Math.max(0, j.right - T.width)), g({ top: $, left: J });
  }, [l]), Ae(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), Ae(() => {
    r && D.current && D.current.focus();
  }, [r]), Ae(() => {
    const j = (T) => {
      T.key === "Escape" && s();
    };
    return document.addEventListener("keydown", j), () => document.removeEventListener("keydown", j);
  }, [s]), Ae(() => {
    const j = ($) => {
      y.current && !y.current.contains($.target) && s();
    }, T = setTimeout(() => document.addEventListener("mousedown", j), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", j);
    };
  }, [s]);
  const L = Yl(() => {
    if (!m) return n;
    const j = m.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(j) || T.label.toLowerCase().includes(j) || T.terms != null && T.terms.some(($) => $.includes(j))
    );
  }, [n, m]), v = ve((j) => {
    p(j.target.value);
  }, []), _ = ve(
    (j) => {
      i(j);
    },
    [i]
  ), C = ve((j) => {
    E(j);
  }, []), H = ve((j) => {
    E(j.target.value);
  }, []), M = ve(() => {
    i(b || null);
  }, [b, i]), R = ve(() => {
    i(null);
  }, [i]), U = ve(async (j) => {
    j.preventDefault(), S(!1);
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
        onClick: () => a("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
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
      !r && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: U }, u["js.iconSelect.loadError"])),
      r && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && L.map(
        (j) => j.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: j.label,
            onClick: () => o === "simple" ? _(T.encoded) : C(T.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), o === "simple" ? _(T.encoded) : C(T.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ne, { encoded: T.encoded })
        ))
      )
    ),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: b,
        onChange: H
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, b && /* @__PURE__ */ e.createElement(Ne, { encoded: b })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, b ? b.startsWith("css:") ? b.substring(4) : b : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: M }, u["js.iconSelect.ok"]))
  );
}, ql = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Zl, useCallback: Be, useRef: Ql } = e, Jl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(ql), [i, s] = Zl(!1), c = Ql(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, b = Be(() => {
    o && !a && s(!0);
  }, [o, a]), E = Be(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Be(() => {
    s(!1);
  }, []), S = Be(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: a,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Ne, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Xl,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: E,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Ne, { encoded: u }) : null));
}, { useCallback: Se, useEffect: er, useMemo: _t, useRef: tr, useState: qe } = e, nr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, lr = [1, 2, 3, 4];
function rr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function or(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of lr)
    n >= i && (r = i);
  return r;
}
function ar(l, t) {
  const n = nr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function sr(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, b) => !!(r[p] && r[p][b]), s = (p, b) => {
    r[p] || (r[p] = {}), r[p][b] = !0;
  }, c = [];
  let u = 0, o = 0;
  const a = (p) => {
    let b = null;
    for (const f of c) f.rowStart === p && (b = f);
    if (!b) return;
    let E = b.colEnd;
    for (; E < n && !i(p, E); ) E++;
    if (E !== b.colEnd) {
      for (let f = b.rowStart; f < b.rowEnd; f++)
        for (let S = b.colEnd; S < E; S++) s(f, S);
      b.colEnd = E;
    }
  };
  for (const p of l) {
    const b = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let E = Math.min(ar(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let D = o; D < n && !i(u, D); D++)
      f++;
    if (E > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let D = o; D < n && !i(u, D); D++)
        f++;
      E = Math.min(E, f);
    }
    const S = o, w = o + E, g = u, y = u + b;
    c.push({ id: p.id, colStart: S, colEnd: w, rowStart: g, rowEnd: y });
    for (let D = g; D < y; D++)
      for (let L = S; L < w; L++) s(D, L);
    o = w, o >= n && (o = 0, u++);
  }
  a(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let b = 0; b < n; b++) {
      if (i(p, b)) continue;
      const E = c.find((f) => f.rowEnd === p && f.colStart <= b && b < f.colEnd);
      if (E) {
        E.rowEnd = p + 1;
        for (let f = E.colStart; f < E.colEnd; f++) s(p, f);
      }
    }
  return c;
}
const cr = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), s = tr(null), [c, u] = qe(1), o = t.editMode === !0;
  er(() => {
    const _ = s.current;
    if (!_) return;
    const C = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = rr(r, C), M = () => u(or(_.clientWidth, H));
    M();
    const R = new ResizeObserver(M);
    return R.observe(_), () => R.disconnect();
  }, [r]);
  const a = _t(() => sr(i, c), [i, c]), m = _t(() => {
    const _ = {};
    for (const C of a) _[C.id] = C;
    return _;
  }, [a]), [p, b] = qe(null), [E, f] = qe(null), S = Se((_, C) => {
    if (!o) {
      _.preventDefault();
      return;
    }
    b(C), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", C);
  }, [o]), w = Se((_, C) => {
    if (!o || !p || p === C) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const H = _.currentTarget.getBoundingClientRect(), M = _.clientX < H.left + H.width / 2;
    f((R) => R && R.id === C && R.before === M ? R : { id: C, before: M });
  }, [o, p]), g = Se(() => {
  }, []), y = Se((_, C, H) => {
    const M = i.map((T) => T.id), R = M.indexOf(_);
    if (R < 0) return;
    M.splice(R, 1);
    const U = M.indexOf(C);
    if (U < 0) {
      M.splice(R, 0, _);
      return;
    }
    const j = H ? U : U + 1;
    M.splice(j, 0, _), n("reorder", { order: M });
  }, [i, n]), D = Se((_, C) => {
    if (!o || !p || p === C) return;
    _.preventDefault();
    const H = _.currentTarget.getBoundingClientRect(), M = _.clientX < H.left + H.width / 2;
    y(p, C, M), b(null), f(null);
  }, [o, p, y]), L = Se(() => {
    b(null), f(null);
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
      const C = m[_.id];
      if (!C) return null;
      const H = {
        gridColumn: `${C.colStart + 1} / ${C.colEnd + 1}`,
        gridRow: `${C.rowStart + 1} / ${C.rowEnd + 1}`
      }, M = ["tlDashboard__tile"];
      return p === _.id && M.push("tlDashboard__tile--dragging"), E && E.id === _.id && M.push(E.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: M.join(" "),
          style: H,
          draggable: o,
          onDragStart: (R) => S(R, _.id),
          onDragOver: (R) => w(R, _.id),
          onDragLeave: g,
          onDrop: (R) => D(R, _.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(V, { control: _.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ir, useRef: vt, useState: Et, useEffect: gt, useLayoutEffect: ur } = e, dr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: n }))));
}, mr = ({ group: l }) => {
  var p, b;
  const [t, n] = Et(!1), [r, i] = Et({}), s = vt(null), c = vt(null), u = ir(() => {
    n((E) => !E);
  }, []);
  ur(() => {
    if (!t) return;
    const E = () => {
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
    return E(), window.addEventListener("resize", E), window.addEventListener("scroll", E, !0), () => {
      window.removeEventListener("resize", E), window.removeEventListener("scroll", E, !0);
    };
  }, [t]), gt(() => {
    if (!t) return;
    const E = (f) => {
      c.current && !c.current.contains(f.target) && s.current && !s.current.contains(f.target) && n(!1);
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [t]), gt(() => {
    if (!t) return;
    const E = (f) => {
      f.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
  }, [t]);
  const o = l.items.filter((E) => E != null);
  if (o.length === 0) return null;
  if (o.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: o[0] })));
  const a = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? a : void 0,
      title: m ? a : void 0
    },
    m ? /* @__PURE__ */ e.createElement(Ne, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, a), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), wt.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: c,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? r : void 0,
        onClick: () => n(!1)
      },
      o.map((E, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: E }))),
      (b = l.subGroups) == null ? void 0 : b.map((E, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), E.items.map((S, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: S })))))
    ),
    document.body
  ));
}, pr = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(mr, { group: i }) : /* @__PURE__ */ e.createElement(dr, { group: i }))));
}, fr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(V, { control: t.frame }));
}, hr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(V, { key: i, control: r })));
}, br = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), _r = {
  "js.sidebar.openDrawer": "Open navigation"
}, vr = ({ controlId: l }) => {
  const t = ne(), n = ae(_r);
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
z("TLButton", Ut);
z("TLToggleButton", Kt);
z("TLTextInput", Dt);
z("TLPasswordInput", Mt);
z("TLNumberInput", jt);
z("TLDatePicker", Bt);
z("TLSelect", $t);
z("TLCheckbox", Ht);
z("TLTable", Wt);
z("TLCounter", Yt);
z("TLTabBar", Xt);
z("TLFieldList", qt);
z("TLAudioRecorder", Qt);
z("TLAudioPlayer", en);
z("TLFileUpload", nn);
z("TLDownload", rn);
z("TLPhotoCapture", an);
z("TLPhotoViewer", cn);
z("TLSplitPanel", un);
z("TLPanel", _n);
z("TLInset", xn);
z("TLMaximizeRoot", vn);
z("TLDeckPane", En);
z("TLSidebar", Tn);
z("TLStack", Rn);
z("TLGrid", Ln);
z("TLCard", Dn);
z("TLAppBar", In);
z("TLBreadcrumb", Pn);
z("TLBottomBar", An);
z("TLDialog", On);
z("TLDialogManager", Hn);
z("TLWindow", Un);
z("TLDrawer", Gn);
z("TLContextMenuRegion", qn);
z("TLSnackbar", el);
z("TLMenu", nl);
z("TLAppShell", ll);
z("TLText", rl);
z("TLTableView", al);
z("TLFormLayout", pl);
z("TLFormGroup", bl);
z("TLFormField", gl);
z("TLResourceCell", Cl);
z("TLTreeView", wl);
z("TLDropdownSelect", Ll);
z("TLColorInput", Vl);
z("TLIconSelect", Jl);
z("TLDashboard", cr);
z("TLToolbar", pr);
z("TLTileStack", fr);
z("TLSlot", hr);
z("TLSlotContent", br);
z("TLDrawerToggle", vr);
