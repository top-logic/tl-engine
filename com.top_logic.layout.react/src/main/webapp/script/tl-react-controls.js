import { React as e, useTLFieldValue as Te, getComponent as Ct, useTLState as Z, useTLCommand as le, TLChild as X, useTLUpload as Ze, useI18N as ae, useTLDataUrl as Qe, register as z } from "tl-react-bridge";
const { useCallback: yt } = e, wt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = yt(
    (o) => {
      r(o.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const s = t.hasError === !0, a = t.hasWarnings === !0, u = t.errorMessage, c = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && a ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: c,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: kt } = e, St = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), s = kt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const a = t.hasError === !0, u = t.hasWarnings === !0, c = t.errorMessage, o = [
    "tlReactNumberInput",
    a ? "tlReactNumberInput--error" : "",
    !a && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": a || void 0,
      title: a && c ? c : void 0
    }
  ));
}, { useCallback: Nt } = e, Rt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Nt(
    (c) => {
      r(c.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const s = t.hasError === !0, a = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && a ? "tlReactDatePicker--warning" : ""
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
}, { useCallback: Tt } = e, xt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = Te(), s = Tt(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), a = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = a.find((b) => b.value === r)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, c = t.hasWarnings === !0, o = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && c ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Lt } = e, Dt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Lt(
    (c) => {
      r(c.target.checked);
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
  const s = t.hasError === !0, a = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    s ? "tlReactCheckbox--error" : "",
    !s && a ? "tlReactCheckbox--warning" : ""
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
}, It = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((a) => {
    const u = a.cellModule ? Ct(a.cellModule) : void 0, c = i[a.name];
    if (u) {
      const o = { value: c, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: a.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + s + "-" + a.name,
          state: o
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: a.name }, c != null ? String(c) : "");
  })))));
};
function ke({ encoded: l, className: t }) {
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
const { useCallback: jt } = e, Pt = ({ controlId: l, command: t, label: n, disabled: r }) => {
  const i = Z(), s = le(), a = t ?? "click", u = n ?? i.label, c = r ?? i.disabled === !0, o = i.hidden === !0, m = i.image, p = i.iconOnly === !0, b = i.tooltip, L = o ? { display: "none" } : void 0, f = b ?? u, v = b ? `text:${b}` : void 0, _ = jt(() => {
    s(a);
  }, [s, a]);
  return m && p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: _,
      disabled: c,
      style: L,
      className: "tlReactButton tlReactButton--icon",
      "data-tooltip": f ? `text:${f}` : void 0,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: m })
  ) : m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: _,
      disabled: c,
      style: L,
      className: "tlReactButton",
      "data-tooltip": v
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: _,
      disabled: c,
      style: L,
      className: "tlReactButton",
      "data-tooltip": v
    },
    u
  );
}, { useCallback: Mt } = e, At = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = Z(), a = le(), u = t ?? "click", c = n ?? s.label, o = r ?? s.active === !0, m = i ?? s.disabled === !0, p = Mt(() => {
    a(u);
  }, [a, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    c
  );
}, Bt = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ot } = e, $t = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.tabs ?? [], i = t.activeTabId, s = Ot((a) => {
    a !== i && n("selectTab", { tabId: a });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === i,
      className: "tlReactTabBar__tab" + (a.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, Ft = ({ controlId: l }) => {
  const t = Z(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(X, { control: i })))));
}, Ht = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Wt = ({ controlId: l }) => {
  const t = Z(), n = Ze(), [r, i] = e.useState("idle"), [s, a] = e.useState(null), u = e.useRef(null), c = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, b = m === "received" ? "idle" : r !== "idle" ? r : m, L = e.useCallback(async () => {
    if (r === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (r !== "uploading") {
      if (a(null), !window.isSecureContext || !navigator.mediaDevices) {
        a("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = C, c.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", H = new MediaRecorder(C, M ? { mimeType: M } : void 0);
        u.current = H, H.ondataavailable = (h) => {
          h.data.size > 0 && c.current.push(h.data);
        }, H.onstop = async () => {
          C.getTracks().forEach((k) => k.stop()), o.current = null;
          const h = new Blob(c.current, { type: H.mimeType || "audio/webm" });
          if (c.current = [], h.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const w = new FormData();
          w.append("audio", h, "recording.webm"), await n(w), i("idle");
        }, H.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), a("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(Ht), v = b === "recording" ? f["js.audioRecorder.stop"] : b === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], _ = b === "uploading", S = ["tlAudioRecorder__button"];
  return b === "recording" && S.push("tlAudioRecorder__button--recording"), b === "uploading" && S.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: L,
      disabled: _,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, zt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ut = ({ controlId: l }) => {
  const t = Z(), n = Qe(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, a] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), c = e.useRef(null), o = e.useRef(i);
  e.useEffect(() => {
    r ? s === "disabled" && a("idle") : (u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), a("disabled"));
  }, [r]), e.useEffect(() => {
    i !== o.current && (o.current = i, u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (s === "playing" || s === "paused" || s === "loading") && a("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), a("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), a("playing");
      return;
    }
    if (!c.current) {
      a("loading");
      try {
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), a("idle");
          return;
        }
        const S = await _.blob();
        c.current = URL.createObjectURL(S);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), a("idle");
        return;
      }
    }
    const v = new Audio(c.current);
    u.current = v, v.onended = () => {
      a("idle");
    }, v.play(), a("playing");
  }, [s, n]), p = ae(zt), b = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], L = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: L,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Vt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Kt = ({ controlId: l }) => {
  const t = Z(), n = Ze(), [r, i] = e.useState("idle"), [s, a] = e.useState(!1), u = e.useRef(null), c = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = c === "received" ? "idle" : r !== "idle" ? r : c, b = e.useCallback(async (h) => {
    i("uploading");
    const w = new FormData();
    w.append("file", h, h.name), await n(w), i("idle");
  }, [n]), L = e.useCallback((h) => {
    var k;
    const w = (k = h.target.files) == null ? void 0 : k[0];
    w && b(w);
  }, [b]), f = e.useCallback(() => {
    var h;
    r !== "uploading" && ((h = u.current) == null || h.click());
  }, [r]), v = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), a(!0);
  }, []), _ = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), a(!1);
  }, []), S = e.useCallback((h) => {
    var k;
    if (h.preventDefault(), h.stopPropagation(), a(!1), r === "uploading") return;
    const w = (k = h.dataTransfer.files) == null ? void 0 : k[0];
    w && b(w);
  }, [r, b]), C = p === "uploading", M = ae(Vt), H = p === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: _,
      onDrop: S
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: C,
        title: H,
        "aria-label": H
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Yt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Gt = ({ controlId: l }) => {
  const t = Z(), n = Qe(), r = le(), i = !!t.hasData, s = t.dataRevision ?? 0, a = t.fileName ?? "download", u = !!t.clearable, [c, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || c)) {
      o(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, v = await fetch(f);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const _ = await v.blob(), S = URL.createObjectURL(_), C = document.createElement("a");
        C.href = S, C.download = a, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(S);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        o(!1);
      }
    }
  }, [i, c, n, s, a]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), b = ae(Yt);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const L = c ? b["js.downloading"] : b["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: c,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), u && /* @__PURE__ */ e.createElement(
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
}, Xt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, qt = ({ controlId: l }) => {
  const t = Z(), n = Ze(), [r, i] = e.useState("idle"), [s, a] = e.useState(null), [u, c] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), b = e.useRef(null), L = e.useRef(null), f = t.error, v = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), S = e.useCallback(() => {
    _(), i("idle");
  }, [_]), C = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (a(null), !v) {
        (T = b.current) == null || T.click();
        return;
      }
      try {
        const O = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = O, i("overlayOpen");
      } catch (O) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", O), a("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, v]), M = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = o.current, O = p.current;
    if (!T || !O)
      return;
    O.width = T.videoWidth, O.height = T.videoHeight;
    const N = O.getContext("2d");
    N && (N.drawImage(T, 0, 0), _(), i("uploading"), O.toBlob(async (B) => {
      if (!B) {
        i("idle");
        return;
      }
      const q = new FormData();
      q.append("photo", B, "capture.jpg"), await n(q), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, _]), H = e.useCallback(async (T) => {
    var B;
    const O = (B = T.target.files) == null ? void 0 : B[0];
    if (!O) return;
    i("uploading");
    const N = new FormData();
    N.append("photo", O, O.name), await n(N), i("idle"), b.current && (b.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var O;
    if (r !== "overlayOpen") return;
    (O = L.current) == null || O.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const T = (O) => {
      O.key === "Escape" && S();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [r, S]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const h = ae(Xt), w = r === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], k = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && k.push("tlPhotoCapture__cameraBtn--uploading");
  const Y = ["tlPhotoCapture__overlayVideo"];
  u && Y.push("tlPhotoCapture__overlayVideo--mirrored");
  const R = ["tlPhotoCapture__mirrorBtn"];
  return u && R.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: C,
      disabled: r === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: H
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: S }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: Y.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: R.join(" "),
        onClick: () => c((T) => !T),
        title: h["js.photoCapture.mirror"],
        "aria-label": h["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: M,
        title: h["js.photoCapture.capture"],
        "aria-label": h["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: S,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, Zt = {
  "js.photoViewer.alt": "Captured photo"
}, Qt = ({ controlId: l }) => {
  const t = Z(), n = Qe(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, a] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      s && (URL.revokeObjectURL(s), a(null));
      return;
    }
    if (i === u.current && s)
      return;
    u.current = i, s && (URL.revokeObjectURL(s), a(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        o || a(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const c = ae(Zt);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: lt, useRef: $e } = e, Jt = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], a = r === "horizontal", u = s.length > 0 && s.every((_) => _.collapsed), c = !u && s.some((_) => _.collapsed), o = u ? !a : a, m = $e(null), p = $e(null), b = $e(null), L = lt((_, S) => {
    const C = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? u && !o ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : S !== void 0 ? C.flex = `0 0 ${S}px` : C.flex = `${_.size} 1 0%`, _.minSize > 0 && !_.collapsed && (C.minWidth = a ? _.minSize : void 0, C.minHeight = a ? void 0 : _.minSize), C;
  }, [a, u, c, o]), f = lt((_, S) => {
    _.preventDefault();
    const C = m.current;
    if (!C) return;
    const M = s[S], H = s[S + 1], h = C.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    h.forEach((R) => {
      w.push(a ? R.offsetWidth : R.offsetHeight);
    }), b.current = w, p.current = {
      splitterIndex: S,
      startPos: a ? _.clientX : _.clientY,
      startSizeBefore: w[S],
      startSizeAfter: w[S + 1],
      childBefore: M,
      childAfter: H
    };
    const k = (R) => {
      const T = p.current;
      if (!T || !b.current) return;
      const N = (a ? R.clientX : R.clientY) - T.startPos, B = T.childBefore.minSize || 0, q = T.childAfter.minSize || 0;
      let ne = T.startSizeBefore + N, W = T.startSizeAfter - N;
      ne < B && (W += ne - B, ne = B), W < q && (ne += W - q, W = q), b.current[T.splitterIndex] = ne, b.current[T.splitterIndex + 1] = W;
      const J = C.querySelectorAll(":scope > .tlSplitPanel__child"), A = J[T.splitterIndex], j = J[T.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${ne}px`), j && (j.style.flex = `0 0 ${W}px`);
    }, Y = () => {
      if (document.removeEventListener("mousemove", k), document.removeEventListener("mouseup", Y), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const R = {};
        s.forEach((T, O) => {
          const N = T.control;
          N != null && N.controlId && b.current && (R[N.controlId] = b.current[O]);
        }), n("updateSizes", { sizes: R });
      }
      b.current = null, p.current = null;
    };
    document.addEventListener("mousemove", k), document.addEventListener("mouseup", Y), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, a, n]), v = [];
  return s.forEach((_, S) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${S}`,
          className: `tlSplitPanel__child${_.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(_)
        },
        /* @__PURE__ */ e.createElement(X, { control: _.control })
      )
    ), i && S < s.length - 1) {
      const C = s[S + 1];
      !_.collapsed && !C.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${S}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (H) => f(H, S)
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
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    v
  );
}, { useCallback: Fe } = e, en = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), on = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), an = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(en), i = t.title, s = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, u = t.showMaximize === !0, c = t.showPopOut === !0, o = t.fullLine === !0, m = t.toolbarButtons ?? [], p = s === "MINIMIZED", b = s === "MAXIMIZED", L = s === "HIDDEN", f = Fe(() => {
    n("toggleMinimize");
  }, [n]), v = Fe(() => {
    n("toggleMaximize");
  }, [n]), _ = Fe(() => {
    n("popOut");
  }, [n]);
  if (L)
    return null;
  const S = b ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: S
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((C, M) => /* @__PURE__ */ e.createElement("span", { key: M, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: C }))), a && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(nn, null) : /* @__PURE__ */ e.createElement(tn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      b ? /* @__PURE__ */ e.createElement(rn, null) : /* @__PURE__ */ e.createElement(ln, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(on, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(X, { control: t.child }))
  );
}, sn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(X, { control: t.child })
  );
}, cn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(X, { control: t.activeChild }));
}, { useCallback: de, useState: Ae, useEffect: Be, useRef: Oe } = e, un = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ge(l, t, n, r) {
  const i = [];
  for (const s of l)
    s.type === "nav" ? i.push({ id: s.id, type: "nav", groupId: r }) : s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...Ge(s.children, t, n, s.id)));
  return i;
}
const Se = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, dn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => a(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), mn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), pn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), fn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), hn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const a = Oe(null);
  Be(() => {
    const o = (m) => {
      a.current && !a.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [s]), Be(() => {
    const o = (m) => {
      m.key === "Escape" && s();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [s]);
  const u = de((o) => {
    o.type === "nav" ? (r(o.id), s()) : o.type === "command" && (i(o.id), s());
  }, [r, i, s]), c = {};
  return n && (c.left = n.right, c.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
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
        /* @__PURE__ */ e.createElement(Se, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, _n = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: s,
  onToggleGroup: a,
  tabIndex: u,
  itemRef: c,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: b,
  flyoutGroupId: L,
  onOpenFlyout: f,
  onCloseFlyout: v
}) => {
  const _ = Oe(null), [S, C] = Ae(null), M = de(() => {
    r ? L === l.id ? v() : (_.current && C(_.current.getBoundingClientRect()), f(l.id)) : a(l.id);
  }, [r, L, l.id, a, f, v]), H = de((w) => {
    _.current = w, c(w);
  }, [c]), h = r && L === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: r ? l.label : void 0,
      "aria-expanded": r ? h : t,
      tabIndex: u,
      ref: H,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
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
  ), h && /* @__PURE__ */ e.createElement(
    hn,
    {
      item: l,
      activeItemId: n,
      anchorRect: S,
      onSelect: i,
      onExecute: s,
      onClose: v
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    ft,
    {
      key: w.id,
      item: w,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
      onToggleGroup: a,
      focusedId: m,
      setItemRef: p,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: f,
      onCloseFlyout: v
    }
  ))));
}, ft = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: s,
  focusedId: a,
  setItemRef: u,
  onItemFocus: c,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: b
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        dn,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        mn,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(pn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(fn, null);
    case "group": {
      const L = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        _n,
        {
          item: l,
          expanded: L,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: s,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: c,
          focusedId: a,
          setItemRef: u,
          onItemFocus: c,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, bn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(un), i = t.items ?? [], s = t.activeItemId, a = t.collapsed, [u, c] = Ae(() => {
    const R = /* @__PURE__ */ new Map(), T = (O) => {
      for (const N of O)
        N.type === "group" && (R.set(N.id, N.expanded), T(N.children));
    };
    return T(i), R;
  }), o = de((R) => {
    c((T) => {
      const O = new Map(T), N = O.get(R) ?? !1;
      return O.set(R, !N), n("toggleGroup", { itemId: R, expanded: !N }), O;
    });
  }, [n]), m = de((R) => {
    R !== s && n("selectItem", { itemId: R });
  }, [n, s]), p = de((R) => {
    n("executeCommand", { itemId: R });
  }, [n]), b = de(() => {
    n("toggleCollapse", {});
  }, [n]), [L, f] = Ae(null), v = de((R) => {
    f(R);
  }, []), _ = de(() => {
    f(null);
  }, []);
  Be(() => {
    a || f(null);
  }, [a]);
  const [S, C] = Ae(() => {
    const R = Ge(i, a, u);
    return R.length > 0 ? R[0].id : "";
  }), M = Oe(/* @__PURE__ */ new Map()), H = de((R) => (T) => {
    T ? M.current.set(R, T) : M.current.delete(R);
  }, []), h = de((R) => {
    C(R);
  }, []), w = Oe(0), k = de((R) => {
    C(R), w.current++;
  }, []);
  Be(() => {
    const R = M.current.get(S);
    R && document.activeElement !== R && R.focus();
  }, [S, w.current]);
  const Y = de((R) => {
    if (R.key === "Escape" && L !== null) {
      R.preventDefault(), _();
      return;
    }
    const T = Ge(i, a, u);
    if (T.length === 0) return;
    const O = T.findIndex((B) => B.id === S);
    if (O < 0) return;
    const N = T[O];
    switch (R.key) {
      case "ArrowDown": {
        R.preventDefault();
        const B = (O + 1) % T.length;
        k(T[B].id);
        break;
      }
      case "ArrowUp": {
        R.preventDefault();
        const B = (O - 1 + T.length) % T.length;
        k(T[B].id);
        break;
      }
      case "Home": {
        R.preventDefault(), k(T[0].id);
        break;
      }
      case "End": {
        R.preventDefault(), k(T[T.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        R.preventDefault(), N.type === "nav" ? m(N.id) : N.type === "command" ? p(N.id) : N.type === "group" && (a ? L === N.id ? _() : v(N.id) : o(N.id));
        break;
      }
      case "ArrowRight": {
        N.type === "group" && !a && ((u.get(N.id) ?? !1) || (R.preventDefault(), o(N.id)));
        break;
      }
      case "ArrowLeft": {
        N.type === "group" && !a && (u.get(N.id) ?? !1) && (R.preventDefault(), o(N.id));
        break;
      }
    }
  }, [
    i,
    a,
    u,
    S,
    L,
    k,
    m,
    p,
    o,
    v,
    _
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: Y }, i.map((R) => /* @__PURE__ */ e.createElement(
    ft,
    {
      key: R.id,
      item: R,
      activeItemId: s,
      collapsed: a,
      onSelect: m,
      onExecute: p,
      onToggleGroup: o,
      focusedId: S,
      setItemRef: H,
      onItemFocus: h,
      groupStates: u,
      flyoutGroupId: L,
      onOpenFlyout: v,
      onCloseFlyout: _
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: a ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: a ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, vn = ({ controlId: l }) => {
  const t = Z(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, a = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, a.map((c, o) => /* @__PURE__ */ e.createElement(X, { key: o, control: c })));
}, En = ({ controlId: l }) => {
  const t = Z(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], a = {};
  return r ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (a.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: a }, s.map((u, c) => /* @__PURE__ */ e.createElement(X, { key: c, control: u })));
}, gn = ({ controlId: l }) => {
  const t = Z(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], a = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((c, o) => /* @__PURE__ */ e.createElement(X, { key: o, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(X, { control: a })));
}, Cn = ({ controlId: l }) => {
  const t = Z(), n = t.title ?? "", r = t.leading, i = t.actions ?? [], s = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    s === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: u }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(X, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((c, o) => /* @__PURE__ */ e.createElement(X, { key: o, control: c }))));
}, { useCallback: yn } = e, wn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.items ?? [], i = yn((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((s, a) => {
    const u = a === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, a > 0 && /* @__PURE__ */ e.createElement(
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
}, { useCallback: kn } = e, Sn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.items ?? [], i = t.activeItemId, s = kn((a) => {
    a !== i && n("selectItem", { itemId: a });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((a) => {
    const u = a.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: a.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(a.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + a.icon, "aria-hidden": "true" }), a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, a.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, a.label)
    );
  }));
}, { useCallback: rt, useEffect: ot, useRef: Nn } = e, Rn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, a = Nn(null), u = rt(() => {
    n("close");
  }, [n]), c = rt((o) => {
    i && o.target === o.currentTarget && u();
  }, [i, u]);
  return ot(() => {
    if (!r) return;
    const o = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [r, u]), ot(() => {
    r && a.current && a.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: c,
      ref: a,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(X, { control: s })
  ) : null;
}, { useEffect: Tn, useRef: xn } = e, Ln = ({ controlId: l }) => {
  const n = Z().dialogs ?? [], r = xn(n.length);
  return Tn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(X, { key: i.controlId, control: i })));
}, { useCallback: Le, useRef: Ce, useState: De } = e, Dn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, In = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], jn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(Dn), i = t.title ?? "", s = t.width ?? "32rem", a = t.height ?? null, u = t.minHeight ?? null, c = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbarButtons ?? [], [b, L] = De(null), [f, v] = De(null), [_, S] = De(null), C = Ce(null), [M, H] = De(!1), h = Ce(null), w = Ce(null), k = Ce(null), Y = Ce(null), R = Ce(null), T = Le(() => {
    n("close");
  }, [n]), O = Le((W, J) => {
    J.preventDefault();
    const A = Y.current;
    if (!A) return;
    const j = A.getBoundingClientRect(), Q = !C.current, d = C.current ?? { x: j.left, y: j.top };
    Q && (C.current = d, S(d)), R.current = {
      dir: W,
      startX: J.clientX,
      startY: J.clientY,
      startW: j.width,
      startH: j.height,
      startPos: { ...d },
      symmetric: Q
    };
    const g = (P) => {
      const D = R.current;
      if (!D) return;
      const V = P.clientX - D.startX, K = P.clientY - D.startY;
      let te = D.startW, ee = D.startH, ie = 0, ue = 0;
      D.symmetric ? (D.dir.includes("e") && (te = D.startW + 2 * V), D.dir.includes("w") && (te = D.startW - 2 * V), D.dir.includes("s") && (ee = D.startH + 2 * K), D.dir.includes("n") && (ee = D.startH - 2 * K)) : (D.dir.includes("e") && (te = D.startW + V), D.dir.includes("w") && (te = D.startW - V, ie = V), D.dir.includes("s") && (ee = D.startH + K), D.dir.includes("n") && (ee = D.startH - K, ue = K));
      const he = Math.max(200, te), fe = Math.max(100, ee);
      D.symmetric ? (ie = (D.startW - he) / 2, ue = (D.startH - fe) / 2) : (D.dir.includes("w") && he === 200 && (ie = D.startW - 200), D.dir.includes("n") && fe === 100 && (ue = D.startH - 100)), w.current = he, k.current = fe, L(he), v(fe);
      const ve = {
        x: D.startPos.x + ie,
        y: D.startPos.y + ue
      };
      C.current = ve, S(ve);
    }, $ = () => {
      document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", $);
      const P = w.current, D = k.current;
      (P != null || D != null) && n("resize", {
        ...P != null ? { width: Math.round(P) + "px" } : {},
        ...D != null ? { height: Math.round(D) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", $);
  }, [n]), N = Le((W) => {
    if (W.button !== 0 || W.target.closest("button")) return;
    W.preventDefault();
    const J = Y.current;
    if (!J) return;
    const A = J.getBoundingClientRect(), j = C.current ?? { x: A.left, y: A.top }, Q = W.clientX - j.x, d = W.clientY - j.y, g = (P) => {
      const D = window.innerWidth, V = window.innerHeight;
      let K = P.clientX - Q, te = P.clientY - d;
      const ee = J.offsetWidth, ie = J.offsetHeight;
      K + ee > D && (K = D - ee), te + ie > V && (te = V - ie), K < 0 && (K = 0), te < 0 && (te = 0);
      const ue = { x: K, y: te };
      C.current = ue, S(ue);
    }, $ = () => {
      document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", $);
  }, []), B = Le(() => {
    var W, J;
    if (M) {
      const A = h.current;
      A && (S(A.x !== -1 ? { x: A.x, y: A.y } : null), L(A.w), v(A.h)), H(!1);
    } else {
      const A = Y.current, j = A == null ? void 0 : A.getBoundingClientRect();
      h.current = {
        x: ((W = C.current) == null ? void 0 : W.x) ?? (j == null ? void 0 : j.left) ?? -1,
        y: ((J = C.current) == null ? void 0 : J.y) ?? (j == null ? void 0 : j.top) ?? -1,
        w: b ?? (j == null ? void 0 : j.width) ?? null,
        h: f ?? null
      }, H(!0), S({ x: 0, y: 0 }), L(null), v(null);
    }
  }, [M, b, f]), q = M ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: b != null ? b + "px" : s,
    ...f != null ? { height: f + "px" } : a != null ? { height: a } : {},
    ...u != null && f == null ? { minHeight: u } : {},
    maxHeight: _ ? "100vh" : "80vh",
    ..._ ? { position: "absolute", left: _.x + "px", top: _.y + "px" } : {}
  }, ne = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: q,
      ref: Y,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": ne
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${M ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: M ? void 0 : N,
        onDoubleClick: B
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: ne }, i),
      p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, p.map((W, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: W })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: B,
          title: M ? r["js.window.restore"] : r["js.window.maximize"]
        },
        M ? (
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
          onClick: T,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(X, { control: o })),
    m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m.map((W, J) => /* @__PURE__ */ e.createElement(X, { key: J, control: W }))),
    c && !M && In.map((W) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: W,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${W}`,
        onMouseDown: (J) => O(W, J)
      }
    ))
  );
}, { useCallback: Pn, useEffect: Mn } = e, An = {
  "js.drawer.close": "Close"
}, Bn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(An), i = t.open === !0, s = t.position ?? "right", a = t.size ?? "medium", u = t.title ?? null, c = t.child, o = Pn(() => {
    n("close");
  }, [n]);
  Mn(() => {
    if (!i) return;
    const p = (b) => {
      b.key === "Escape" && o();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, o]);
  const m = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${a}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, c && /* @__PURE__ */ e.createElement(X, { control: c })));
}, { useCallback: On, useEffect: $n, useState: Fn } = e, Hn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", a = t.duration ?? 5e3, u = t.visible === !0, c = t.generation ?? 0, [o, m] = Fn(!1), p = On(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: c }), m(!1);
    }, 200);
  }, [n, c]);
  return $n(() => {
    if (!u || a === 0) return;
    const b = setTimeout(p, a);
    return () => clearTimeout(b);
  }, [u, a, p]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: He, useEffect: We, useRef: Wn, useState: at } = e, zn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = t.open === !0, i = t.anchorId, s = t.items ?? [], a = Wn(null), [u, c] = at({ top: 0, left: 0 }), [o, m] = at(0), p = s.filter((v) => v.type === "item" && !v.disabled);
  We(() => {
    var h, w;
    if (!r || !i) return;
    const v = document.getElementById(i);
    if (!v) return;
    const _ = v.getBoundingClientRect(), S = ((h = a.current) == null ? void 0 : h.offsetHeight) ?? 200, C = ((w = a.current) == null ? void 0 : w.offsetWidth) ?? 200;
    let M = _.bottom + 4, H = _.left;
    M + S > window.innerHeight && (M = _.top - S - 4), H + C > window.innerWidth && (H = _.right - C), c({ top: M, left: H }), m(0);
  }, [r, i]);
  const b = He(() => {
    n("close");
  }, [n]), L = He((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  We(() => {
    if (!r) return;
    const v = (_) => {
      a.current && !a.current.contains(_.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [r, b]);
  const f = He((v) => {
    if (v.key === "Escape") {
      b();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), m((_) => (_ + 1) % p.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((_) => (_ - 1 + p.length) % p.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const _ = p[o];
      _ && L(_.id);
    }
  }, [b, L, p, o]);
  return We(() => {
    r && a.current && a.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: a,
      tabIndex: -1,
      style: { position: "fixed", top: u.top, left: u.left },
      onKeyDown: f
    },
    s.map((v, _) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: _, className: "tlMenu__separator" });
      const C = p.indexOf(v) === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (C ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: C ? 0 : -1,
          onClick: () => L(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Un = ({ controlId: l }) => {
  const t = Z(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, a = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(X, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(X, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(X, { control: i })), /* @__PURE__ */ e.createElement(X, { control: s }), a && /* @__PURE__ */ e.createElement(X, { control: a }));
}, Vn = () => {
  const l = Z(), t = l.text ?? "", n = l.cssClass ?? "", r = n ? `tlText ${n}` : "tlText";
  return /* @__PURE__ */ e.createElement("span", { className: r }, t);
}, Kn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, st = 50, Yn = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(Kn), i = e.useRef(null);
  e.useEffect(() => {
    const y = i.current;
    if (!y) return;
    const I = (E) => {
      const x = E.detail;
      let F = x.target;
      for (; F && F !== y; ) {
        const G = F.dataset.row, re = F.dataset.col;
        if (G != null && re != null) {
          x.resolved = { key: G + "|" + re };
          return;
        }
        F = F.parentElement;
      }
    };
    return y.addEventListener("tl-tooltip-resolve", I), () => y.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const s = t.columns ?? [], a = t.totalRowCount ?? 0, u = t.rows ?? [], c = t.rowHeight ?? 36, o = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, b = t.treeMode ?? !1, L = e.useMemo(
    () => s.filter((y) => y.sortPriority && y.sortPriority > 0).length,
    [s]
  ), f = o === "multi", v = 40, _ = 20, S = e.useRef(null), C = e.useRef(null), M = e.useRef(null), [H, h] = e.useState({}), w = e.useRef(null), k = e.useRef(!1), Y = e.useRef(null), [R, T] = e.useState(null), [O, N] = e.useState(null);
  e.useEffect(() => {
    w.current || h({});
  }, [s]);
  const B = e.useCallback((y) => H[y.name] ?? y.width, [H]), q = e.useMemo(() => {
    const y = [];
    let I = f && p > 0 ? v : 0;
    for (let E = 0; E < p && E < s.length; E++)
      y.push(I), I += B(s[E]);
    return y;
  }, [s, p, f, v, B]), ne = a * c, W = e.useRef(null), J = e.useCallback((y, I, E) => {
    E.preventDefault(), E.stopPropagation(), w.current = { column: y, startX: E.clientX, startWidth: I };
    let x = E.clientX, F = 0;
    const G = () => {
      const oe = w.current;
      if (!oe) return;
      const me = Math.max(st, oe.startWidth + (x - oe.startX) + F);
      h((ge) => ({ ...ge, [oe.column]: me }));
    }, re = () => {
      const oe = C.current, me = S.current;
      if (!oe || !w.current) return;
      const ge = oe.getBoundingClientRect(), et = 40, tt = 8, gt = oe.scrollLeft;
      x > ge.right - et ? oe.scrollLeft += tt : x < ge.left + et && (oe.scrollLeft = Math.max(0, oe.scrollLeft - tt));
      const nt = oe.scrollLeft - gt;
      nt !== 0 && (me && (me.scrollLeft = oe.scrollLeft), F += nt, G()), W.current = requestAnimationFrame(re);
    };
    W.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      x = oe.clientX, G();
    }, xe = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", xe), W.current !== null && (cancelAnimationFrame(W.current), W.current = null);
      const me = w.current;
      if (me) {
        const ge = Math.max(st, me.startWidth + (oe.clientX - me.startX) + F);
        n("columnResize", { column: me.column, width: ge }), w.current = null, k.current = !0, requestAnimationFrame(() => {
          k.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", xe);
  }, [n]), A = e.useCallback(() => {
    S.current && C.current && (S.current.scrollLeft = C.current.scrollLeft), M.current !== null && clearTimeout(M.current), M.current = window.setTimeout(() => {
      const y = C.current;
      if (!y) return;
      const I = y.scrollTop, E = Math.ceil(y.clientHeight / c), x = Math.floor(I / c);
      n("scroll", { start: x, count: E });
    }, 80);
  }, [n, c]), j = e.useCallback((y, I, E) => {
    if (k.current) return;
    let x;
    !I || I === "desc" ? x = "asc" : x = "desc";
    const F = E.shiftKey ? "add" : "replace";
    n("sort", { column: y, direction: x, mode: F });
  }, [n]), Q = e.useCallback((y, I) => {
    Y.current = y, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", y);
  }, []), d = e.useCallback((y, I) => {
    if (!Y.current || Y.current === y) {
      T(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const E = I.currentTarget.getBoundingClientRect(), x = I.clientX < E.left + E.width / 2 ? "left" : "right";
    T({ column: y, side: x });
  }, []), g = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const I = Y.current;
    if (!I || !R) {
      Y.current = null, T(null);
      return;
    }
    let E = s.findIndex((F) => F.name === R.column);
    if (E < 0) {
      Y.current = null, T(null);
      return;
    }
    const x = s.findIndex((F) => F.name === I);
    R.side === "right" && E++, x < E && E--, n("columnReorder", { column: I, targetIndex: E }), Y.current = null, T(null);
  }, [s, R, n]), $ = e.useCallback(() => {
    Y.current = null, T(null);
  }, []), P = e.useCallback((y, I) => {
    I.shiftKey && I.preventDefault(), n("select", {
      rowIndex: y,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [n]), D = e.useCallback((y, I) => {
    I.stopPropagation(), n("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [n]), V = e.useCallback(() => {
    const y = m === a && a > 0;
    n("selectAll", { selected: !y });
  }, [n, m, a]), K = e.useCallback((y, I, E) => {
    E.stopPropagation(), n("expand", { rowIndex: y, expanded: I });
  }, [n]), te = e.useCallback((y, I) => {
    I.preventDefault(), N({ x: I.clientX, y: I.clientY, colIdx: y });
  }, []), ee = e.useCallback(() => {
    O && (n("setFrozenColumnCount", { count: O.colIdx + 1 }), N(null));
  }, [O, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), N(null);
  }, [n]);
  e.useEffect(() => {
    if (!O) return;
    const y = () => N(null), I = (E) => {
      E.key === "Escape" && N(null);
    };
    return document.addEventListener("mousedown", y), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", y), document.removeEventListener("keydown", I);
    };
  }, [O]);
  const ue = s.reduce((y, I) => y + B(I), 0) + (f ? v : 0), he = m === a && a > 0, fe = m > 0 && m < a, ve = e.useCallback((y) => {
    y && (y.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (y) => {
        if (!Y.current) return;
        y.preventDefault();
        const I = C.current, E = S.current;
        if (!I) return;
        const x = I.getBoundingClientRect(), F = 40, G = 8;
        y.clientX < x.left + F ? I.scrollLeft = Math.max(0, I.scrollLeft - G) : y.clientX > x.right - F && (I.scrollLeft += G), E && (E.scrollLeft = I.scrollLeft);
      },
      onDrop: g
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: S }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ue } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: v,
          minWidth: v,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (y) => {
          Y.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== Y.current && T({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ve,
          className: "tlTableView__checkbox",
          checked: he,
          onChange: V
        }
      )
    ), s.map((y, I) => {
      const E = B(y);
      s.length - 1;
      let x = "tlTableView__headerCell";
      y.sortable && (x += " tlTableView__headerCell--sortable"), R && R.column === y.name && (x += " tlTableView__headerCell--dragOver-" + R.side);
      const F = I < p, G = I === p - 1;
      return F && (x += " tlTableView__headerCell--frozen"), G && (x += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: x,
          style: {
            width: E,
            minWidth: E,
            position: F ? "sticky" : "relative",
            ...F ? { left: q[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: y.sortable ? (re) => j(y.name, y.sortDirection, re) : void 0,
          onContextMenu: (re) => te(I, re),
          onDragStart: (re) => Q(y.name, re),
          onDragOver: (re) => d(y.name, re),
          onDrop: g,
          onDragEnd: $
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, y.label),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼", L > 1 && y.sortPriority != null && y.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, y.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => J(y.name, E, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (Y.current && s.length > 0) {
            const I = s[s.length - 1];
            I.name !== Y.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", T({ column: I.name, side: "right" }));
          }
        },
        onDrop: g
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: C,
        className: "tlTableView__body",
        onScroll: A
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ne, position: "relative", width: ue } }, u.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: y.index * c,
            height: c,
            width: ue
          },
          onClick: (I) => P(y.index, I)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: v,
              minWidth: v,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (I) => D(y.index, I),
              tabIndex: -1
            }
          )
        ),
        s.map((I, E) => {
          const x = B(I), F = E === s.length - 1, G = E < p, re = E === p - 1;
          let Ee = "tlTableView__cell";
          G && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const xe = b && E === 0, oe = y.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Ee,
              "data-row": y.id,
              "data-col": I.name,
              style: {
                ...F && !G ? { flex: "1 0 auto", minWidth: x } : { width: x, minWidth: x },
                ...G ? { position: "sticky", left: q[E], zIndex: 2 } : {}
              }
            },
            xe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * _ } }, y.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => K(y.index, !y.expanded, me)
              },
              y.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(X, { control: y.cells[I.name] })) : /* @__PURE__ */ e.createElement(X, { control: y.cells[I.name] })
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
      O.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Gn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ht = e.createContext(Gn), { useMemo: Xn, useRef: qn, useState: Zn, useEffect: Qn } = e, Jn = 320, el = ({ controlId: l }) => {
  const t = Z(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], a = t.noModelMessage, u = qn(null), [c, o] = Zn(
    r === "top" ? "top" : "side"
  );
  Qn(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const v = new ResizeObserver((_) => {
      for (const S of _) {
        const M = S.contentRect.width / n;
        o(M < Jn ? "top" : "side");
      }
    });
    return v.observe(f), () => v.disconnect();
  }, [r, n]);
  const m = Xn(() => ({
    readOnly: i,
    resolvedLabelPosition: c
  }), [i, c]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, L = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, a)) : /* @__PURE__ */ e.createElement(ht.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: L, style: b, ref: u }, s.map((f, v) => /* @__PURE__ */ e.createElement(X, { key: v, control: f }))));
}, { useCallback: tl } = e, nl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ll = ({ controlId: l }) => {
  const t = Z(), n = le(), r = ae(nl), i = t.headerControl ?? null, s = t.headerActions ?? [], a = t.collapsible === !0, u = t.collapsed === !0, c = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || a, b = tl(() => {
    n("toggleCollapse");
  }, [n]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, a && /* @__PURE__ */ e.createElement(
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(X, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, v) => /* @__PURE__ */ e.createElement(X, { key: v, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, v) => /* @__PURE__ */ e.createElement(X, { key: v, control: f }))));
}, { useContext: rl, useState: ol, useCallback: al } = e, sl = ({ controlId: l }) => {
  const t = Z(), n = rl(ht), r = t.label ?? "", i = t.required === !0, s = t.error, a = t.warnings, u = t.helpText, c = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, b = t.hasTooltip === !0, L = t.field, f = n.readOnly, [v, _] = ol(!1), S = al(() => _((h) => !h), []);
  if (!p) return null;
  const C = s != null, M = a != null && a.length > 0, H = [
    "tlFormField",
    `tlFormField--${o}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && M ? "tlFormField--warning" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: H }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": b ? "key:tooltip" : void 0
    },
    r
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: S,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(X, { control: L })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !C && M && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, a.map((h, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, h)))), !f && u && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, cl = () => {
  const l = Z(), t = le(), n = l.iconCss, r = l.iconSrc, i = l.label, s = l.cssClass, a = l.tooltip, u = l.hasLink, c = n ? /* @__PURE__ */ e.createElement("i", { className: n }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, o = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), m = e.useCallback((b) => {
    b.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", s].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement(
    "a",
    {
      className: p,
      href: "#",
      onClick: m,
      "data-tooltip": a ? `text:${a}` : void 0
    },
    o
  ) : /* @__PURE__ */ e.createElement("span", { className: p, "data-tooltip": a ? `text:${a}` : void 0 }, o);
}, il = 20, ul = () => {
  const l = Z(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, a = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [c, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((h, w) => {
    t(w ? "collapse" : "expand", { nodeId: h });
  }, [t]), b = e.useCallback((h, w) => {
    t("select", {
      nodeId: h,
      ctrlKey: w.ctrlKey || w.metaKey,
      shiftKey: w.shiftKey
    });
  }, [t]), L = e.useCallback((h, w) => {
    w.preventDefault(), t("contextMenu", { nodeId: h, x: w.clientX, y: w.clientY });
  }, [t]), f = e.useRef(null), v = e.useCallback((h, w) => {
    const k = w.getBoundingClientRect(), Y = h.clientY - k.top, R = k.height / 3;
    return Y < R ? "above" : Y > R * 2 ? "below" : "within";
  }, []), _ = e.useCallback((h, w) => {
    w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", h);
  }, []), S = e.useCallback((h, w) => {
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const k = v(w, w.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: k }), f.current = null;
    }, 50);
  }, [t, v]), C = e.useCallback((h, w) => {
    w.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const k = v(w, w.currentTarget);
    t("drop", { nodeId: h, position: k });
  }, [t, v]), M = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), H = e.useCallback((h) => {
    if (n.length === 0) return;
    let w = c;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), w = Math.min(c + 1, n.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), w = Math.max(c - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), c >= 0 && c < n.length) {
          const k = n[c];
          if (k.expandable && !k.expanded) {
            t("expand", { nodeId: k.id });
            return;
          } else k.expanded && (w = c + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), c >= 0 && c < n.length) {
          const k = n[c];
          if (k.expanded) {
            t("collapse", { nodeId: k.id });
            return;
          } else {
            const Y = k.depth;
            for (let R = c - 1; R >= 0; R--)
              if (n[R].depth < Y) {
                w = R;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), c >= 0 && c < n.length && t("select", {
          nodeId: n[c].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), r === "multi" && c >= 0 && c < n.length && t("select", {
          nodeId: n[c].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), w = 0;
        break;
      case "End":
        h.preventDefault(), w = n.length - 1;
        break;
      default:
        return;
    }
    w !== c && o(w);
  }, [c, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: H
    },
    n.map((h, w) => /* @__PURE__ */ e.createElement(
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
          w === c ? "tlTreeView__node--focused" : "",
          a === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          a === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          a === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * il },
        draggable: i,
        onClick: (k) => b(h.id, k),
        onContextMenu: (k) => L(h.id, k),
        onDragStart: (k) => _(h.id, k),
        onDragOver: s ? (k) => S(h.id, k) : void 0,
        onDrop: s ? (k) => C(h.id, k) : void 0,
        onDragEnd: M
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (k) => {
            k.stopPropagation(), p(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(X, { control: h.content }))
    ))
  );
};
var ze = { exports: {} }, se = {}, Ue = { exports: {} }, U = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ct;
function dl() {
  if (ct) return U;
  ct = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), a = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), c = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), b = Symbol.iterator;
  function L(d) {
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
  }, v = Object.assign, _ = {};
  function S(d, g, $) {
    this.props = d, this.context = g, this.refs = _, this.updater = $ || f;
  }
  S.prototype.isReactComponent = {}, S.prototype.setState = function(d, g) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, g, "setState");
  }, S.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = S.prototype;
  function M(d, g, $) {
    this.props = d, this.context = g, this.refs = _, this.updater = $ || f;
  }
  var H = M.prototype = new C();
  H.constructor = M, v(H, S.prototype), H.isPureReactComponent = !0;
  var h = Array.isArray;
  function w() {
  }
  var k = { H: null, A: null, T: null, S: null }, Y = Object.prototype.hasOwnProperty;
  function R(d, g, $) {
    var P = $.ref;
    return {
      $$typeof: l,
      type: d,
      key: g,
      ref: P !== void 0 ? P : null,
      props: $
    };
  }
  function T(d, g) {
    return R(d.type, g, d.props);
  }
  function O(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function N(d) {
    var g = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function($) {
      return g[$];
    });
  }
  var B = /\/+/g;
  function q(d, g) {
    return typeof d == "object" && d !== null && d.key != null ? N("" + d.key) : g.toString(36);
  }
  function ne(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(w, w) : (d.status = "pending", d.then(
          function(g) {
            d.status === "pending" && (d.status = "fulfilled", d.value = g);
          },
          function(g) {
            d.status === "pending" && (d.status = "rejected", d.reason = g);
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
  function W(d, g, $, P, D) {
    var V = typeof d;
    (V === "undefined" || V === "boolean") && (d = null);
    var K = !1;
    if (d === null) K = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          K = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              K = !0;
              break;
            case m:
              return K = d._init, W(
                K(d._payload),
                g,
                $,
                P,
                D
              );
          }
      }
    if (K)
      return D = D(d), K = P === "" ? "." + q(d, 0) : P, h(D) ? ($ = "", K != null && ($ = K.replace(B, "$&/") + "/"), W(D, g, $, "", function(ie) {
        return ie;
      })) : D != null && (O(D) && (D = T(
        D,
        $ + (D.key == null || d && d.key === D.key ? "" : ("" + D.key).replace(
          B,
          "$&/"
        ) + "/") + K
      )), g.push(D)), 1;
    K = 0;
    var te = P === "" ? "." : P + ":";
    if (h(d))
      for (var ee = 0; ee < d.length; ee++)
        P = d[ee], V = te + q(P, ee), K += W(
          P,
          g,
          $,
          V,
          D
        );
    else if (ee = L(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(P = d.next()).done; )
        P = P.value, V = te + q(P, ee++), K += W(
          P,
          g,
          $,
          V,
          D
        );
    else if (V === "object") {
      if (typeof d.then == "function")
        return W(
          ne(d),
          g,
          $,
          P,
          D
        );
      throw g = String(d), Error(
        "Objects are not valid as a React child (found: " + (g === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : g) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return K;
  }
  function J(d, g, $) {
    if (d == null) return d;
    var P = [], D = 0;
    return W(d, P, "", "", function(V) {
      return g.call($, V, D++);
    }), P;
  }
  function A(d) {
    if (d._status === -1) {
      var g = d._result;
      g = g(), g.then(
        function($) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = $);
        },
        function($) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = $);
        }
      ), d._status === -1 && (d._status = 0, d._result = g);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var g = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(g)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Q = {
    map: J,
    forEach: function(d, g, $) {
      J(
        d,
        function() {
          g.apply(this, arguments);
        },
        $
      );
    },
    count: function(d) {
      var g = 0;
      return J(d, function() {
        g++;
      }), g;
    },
    toArray: function(d) {
      return J(d, function(g) {
        return g;
      }) || [];
    },
    only: function(d) {
      if (!O(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return U.Activity = p, U.Children = Q, U.Component = S, U.Fragment = n, U.Profiler = i, U.PureComponent = M, U.StrictMode = r, U.Suspense = c, U.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = k, U.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return k.H.useMemoCache(d);
    }
  }, U.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, U.cacheSignal = function() {
    return null;
  }, U.cloneElement = function(d, g, $) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var P = v({}, d.props), D = d.key;
    if (g != null)
      for (V in g.key !== void 0 && (D = "" + g.key), g)
        !Y.call(g, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && g.ref === void 0 || (P[V] = g[V]);
    var V = arguments.length - 2;
    if (V === 1) P.children = $;
    else if (1 < V) {
      for (var K = Array(V), te = 0; te < V; te++)
        K[te] = arguments[te + 2];
      P.children = K;
    }
    return R(d.type, D, P);
  }, U.createContext = function(d) {
    return d = {
      $$typeof: a,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: s,
      _context: d
    }, d;
  }, U.createElement = function(d, g, $) {
    var P, D = {}, V = null;
    if (g != null)
      for (P in g.key !== void 0 && (V = "" + g.key), g)
        Y.call(g, P) && P !== "key" && P !== "__self" && P !== "__source" && (D[P] = g[P]);
    var K = arguments.length - 2;
    if (K === 1) D.children = $;
    else if (1 < K) {
      for (var te = Array(K), ee = 0; ee < K; ee++)
        te[ee] = arguments[ee + 2];
      D.children = te;
    }
    if (d && d.defaultProps)
      for (P in K = d.defaultProps, K)
        D[P] === void 0 && (D[P] = K[P]);
    return R(d, V, D);
  }, U.createRef = function() {
    return { current: null };
  }, U.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, U.isValidElement = O, U.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, U.memo = function(d, g) {
    return {
      $$typeof: o,
      type: d,
      compare: g === void 0 ? null : g
    };
  }, U.startTransition = function(d) {
    var g = k.T, $ = {};
    k.T = $;
    try {
      var P = d(), D = k.S;
      D !== null && D($, P), typeof P == "object" && P !== null && typeof P.then == "function" && P.then(w, j);
    } catch (V) {
      j(V);
    } finally {
      g !== null && $.types !== null && (g.types = $.types), k.T = g;
    }
  }, U.unstable_useCacheRefresh = function() {
    return k.H.useCacheRefresh();
  }, U.use = function(d) {
    return k.H.use(d);
  }, U.useActionState = function(d, g, $) {
    return k.H.useActionState(d, g, $);
  }, U.useCallback = function(d, g) {
    return k.H.useCallback(d, g);
  }, U.useContext = function(d) {
    return k.H.useContext(d);
  }, U.useDebugValue = function() {
  }, U.useDeferredValue = function(d, g) {
    return k.H.useDeferredValue(d, g);
  }, U.useEffect = function(d, g) {
    return k.H.useEffect(d, g);
  }, U.useEffectEvent = function(d) {
    return k.H.useEffectEvent(d);
  }, U.useId = function() {
    return k.H.useId();
  }, U.useImperativeHandle = function(d, g, $) {
    return k.H.useImperativeHandle(d, g, $);
  }, U.useInsertionEffect = function(d, g) {
    return k.H.useInsertionEffect(d, g);
  }, U.useLayoutEffect = function(d, g) {
    return k.H.useLayoutEffect(d, g);
  }, U.useMemo = function(d, g) {
    return k.H.useMemo(d, g);
  }, U.useOptimistic = function(d, g) {
    return k.H.useOptimistic(d, g);
  }, U.useReducer = function(d, g, $) {
    return k.H.useReducer(d, g, $);
  }, U.useRef = function(d) {
    return k.H.useRef(d);
  }, U.useState = function(d) {
    return k.H.useState(d);
  }, U.useSyncExternalStore = function(d, g, $) {
    return k.H.useSyncExternalStore(
      d,
      g,
      $
    );
  }, U.useTransition = function() {
    return k.H.useTransition();
  }, U.version = "19.2.4", U;
}
var it;
function ml() {
  return it || (it = 1, Ue.exports = dl()), Ue.exports;
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
var ut;
function pl() {
  if (ut) return se;
  ut = 1;
  var l = ml();
  function t(c) {
    var o = "https://react.dev/errors/" + c;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        o += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + c + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function s(c, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: c,
      containerInfo: o,
      implementation: m
    };
  }
  var a = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(c, o) {
    if (c === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(c, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return s(c, o, null, m);
  }, se.flushSync = function(c) {
    var o = a.T, m = r.p;
    try {
      if (a.T = null, r.p = 2, c) return c();
    } finally {
      a.T = o, r.p = m, r.d.f();
    }
  }, se.preconnect = function(c, o) {
    typeof c == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, r.d.C(c, o));
  }, se.prefetchDNS = function(c) {
    typeof c == "string" && r.d.D(c);
  }, se.preinit = function(c, o) {
    if (typeof c == "string" && o && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin), b = typeof o.integrity == "string" ? o.integrity : void 0, L = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? r.d.S(
        c,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: b,
          fetchPriority: L
        }
      ) : m === "script" && r.d.X(c, {
        crossOrigin: p,
        integrity: b,
        fetchPriority: L,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, se.preinitModule = function(c, o) {
    if (typeof c == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var m = u(
            o.as,
            o.crossOrigin
          );
          r.d.M(c, {
            crossOrigin: m,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && r.d.M(c);
  }, se.preload = function(c, o) {
    if (typeof c == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var m = o.as, p = u(m, o.crossOrigin);
      r.d.L(c, m, {
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
  }, se.preloadModule = function(c, o) {
    if (typeof c == "string")
      if (o) {
        var m = u(o.as, o.crossOrigin);
        r.d.m(c, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else r.d.m(c);
  }, se.requestFormReset = function(c) {
    r.d.r(c);
  }, se.unstable_batchedUpdates = function(c, o) {
    return c(o);
  }, se.useFormState = function(c, o, m) {
    return a.H.useFormState(c, o, m);
  }, se.useFormStatus = function() {
    return a.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var dt;
function fl() {
  if (dt) return ze.exports;
  dt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), ze.exports = pl(), ze.exports;
}
var hl = fl();
const { useState: _e, useCallback: ce, useRef: Ne, useEffect: ye, useMemo: Xe } = e;
function Je({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function _l({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: s,
  onDragOver: a,
  onDrop: u,
  onDragEnd: c,
  dragClassName: o
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
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: i || void 0,
      onDragStart: s,
      onDragOver: a,
      onDrop: u,
      onDragEnd: c
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
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
function bl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const a = ce(() => r(l.value), [r, l.value]), u = Xe(() => {
    if (!n) return l.label;
    const c = l.label.toLowerCase().indexOf(n.toLowerCase());
    return c < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, c), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(c, c + n.length)), l.label.substring(c + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: a,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const vl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, a = t.mandatory === !0, u = t.disabled === !0, c = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", b = s && i && !u && c, L = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = L["js.dropdownSelect.nothingFound"], v = ce(
    (E) => L["js.dropdownSelect.removeChip"].replace("{0}", E),
    [L]
  ), [_, S] = _e(!1), [C, M] = _e(""), [H, h] = _e(-1), [w, k] = _e(!1), [Y, R] = _e({}), [T, O] = _e(null), [N, B] = _e(null), [q, ne] = _e(null), W = Ne(null), J = Ne(null), A = Ne(null), j = Ne(r);
  j.current = r;
  const Q = Ne(-1), d = Xe(
    () => new Set(r.map((E) => E.value)),
    [r]
  ), g = Xe(() => {
    let E = m.filter((x) => !d.has(x.value));
    if (C) {
      const x = C.toLowerCase();
      E = E.filter((F) => F.label.toLowerCase().includes(x));
    }
    return E;
  }, [m, d, C]);
  ye(() => {
    C && g.length === 1 ? h(0) : h(-1);
  }, [g.length, C]), ye(() => {
    _ && o && J.current && J.current.focus();
  }, [_, o, r]), ye(() => {
    var F, G;
    if (Q.current < 0) return;
    const E = Q.current;
    Q.current = -1;
    const x = (F = W.current) == null ? void 0 : F.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(E, x.length - 1)].focus() : (G = W.current) == null || G.focus();
  }, [r]), ye(() => {
    if (!_) return;
    const E = (x) => {
      W.current && !W.current.contains(x.target) && A.current && !A.current.contains(x.target) && (S(!1), M(""));
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [_]), ye(() => {
    if (!_ || !W.current) return;
    const E = W.current.getBoundingClientRect(), x = window.innerHeight - E.bottom, G = x < 300 && E.top > x;
    R({
      left: E.left,
      width: E.width,
      ...G ? { bottom: window.innerHeight - E.top } : { top: E.bottom }
    });
  }, [_]);
  const $ = ce(async () => {
    if (!(u || !c) && (S(!0), M(""), h(-1), k(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        k(!0);
      }
  }, [u, c, o, n]), P = ce(() => {
    var E;
    S(!1), M(""), h(-1), (E = W.current) == null || E.focus();
  }, []), D = ce(
    (E) => {
      let x;
      if (i) {
        const F = m.find((G) => G.value === E);
        if (F)
          x = [...j.current, F];
        else
          return;
      } else {
        const F = m.find((G) => G.value === E);
        if (F)
          x = [F];
        else
          return;
      }
      j.current = x, n("valueChanged", { value: x.map((F) => F.value) }), i ? (M(""), h(-1)) : P();
    },
    [i, m, n, P]
  ), V = ce(
    (E) => {
      Q.current = j.current.findIndex((F) => F.value === E);
      const x = j.current.filter((F) => F.value !== E);
      j.current = x, n("valueChanged", { value: x.map((F) => F.value) });
    },
    [n]
  ), K = ce(
    (E) => {
      E.stopPropagation(), n("valueChanged", { value: [] }), P();
    },
    [n, P]
  ), te = ce((E) => {
    M(E.target.value);
  }, []), ee = ce(
    (E) => {
      if (!_) {
        if (E.key === "ArrowDown" || E.key === "ArrowUp" || E.key === "Enter" || E.key === " ") {
          if (E.target.tagName === "BUTTON") return;
          E.preventDefault(), E.stopPropagation(), $();
        }
        return;
      }
      switch (E.key) {
        case "ArrowDown":
          E.preventDefault(), E.stopPropagation(), h(
            (x) => x < g.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          E.preventDefault(), E.stopPropagation(), h(
            (x) => x > 0 ? x - 1 : g.length - 1
          );
          break;
        case "Enter":
          E.preventDefault(), E.stopPropagation(), H >= 0 && H < g.length && D(g[H].value);
          break;
        case "Escape":
          E.preventDefault(), E.stopPropagation(), P();
          break;
        case "Tab":
          P();
          break;
        case "Backspace":
          C === "" && i && r.length > 0 && V(r[r.length - 1].value);
          break;
      }
    },
    [
      _,
      $,
      P,
      g,
      H,
      D,
      C,
      i,
      r,
      V
    ]
  ), ie = ce(
    async (E) => {
      E.preventDefault(), k(!1);
      try {
        await n("loadOptions");
      } catch {
        k(!0);
      }
    },
    [n]
  ), ue = ce(
    (E, x) => {
      O(E), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(E));
    },
    []
  ), he = ce(
    (E, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", T === null || T === E) {
        B(null), ne(null);
        return;
      }
      const F = x.currentTarget.getBoundingClientRect(), G = F.left + F.width / 2, re = x.clientX < G ? "before" : "after";
      B(E), ne(re);
    },
    [T]
  ), fe = ce(
    (E) => {
      if (E.preventDefault(), T === null || N === null || q === null || T === N) return;
      const x = [...j.current], [F] = x.splice(T, 1);
      let G = N;
      T < N ? G = q === "before" ? G - 1 : G : G = q === "before" ? G : G + 1, x.splice(G, 0, F), j.current = x, n("valueChanged", { value: x.map((re) => re.value) }), O(null), B(null), ne(null);
    },
    [T, N, q, n]
  ), ve = ce(() => {
    O(null), B(null), ne(null);
  }, []);
  if (ye(() => {
    if (H < 0 || !A.current) return;
    const E = A.current.querySelector(
      `[id="${l}-opt-${H}"]`
    );
    E && E.scrollIntoView({ block: "nearest" });
  }, [H, l]), !c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((E) => /* @__PURE__ */ e.createElement("span", { key: E.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Je, { image: E.image }), /* @__PURE__ */ e.createElement("span", null, E.label))));
  const y = !a && r.length > 0 && !u, I = _ ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: Y
    },
    (o || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: te,
        onKeyDown: ee,
        placeholder: L["js.dropdownSelect.filterPlaceholder"],
        "aria-label": L["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": H >= 0 ? `${l}-opt-${H}` : void 0,
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
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, L["js.dropdownSelect.error"])),
      o && g.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      o && g.map((E, x) => /* @__PURE__ */ e.createElement(
        bl,
        {
          key: E.value,
          id: `${l}-opt-${x}`,
          option: E,
          highlighted: x === H,
          searchTerm: C,
          onSelect: D,
          onMouseEnter: () => h(x)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: W,
      className: "tlDropdownSelect" + (_ ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": _,
      "aria-haspopup": "listbox",
      "aria-owns": _ ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: _ ? void 0 : $,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((E, x) => {
      let F = "";
      return T === x ? F = "tlDropdownSelect__chip--dragging" : N === x && q === "before" ? F = "tlDropdownSelect__chip--dropBefore" : N === x && q === "after" && (F = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        _l,
        {
          key: E.value,
          option: E,
          removable: !u && (i || !a),
          onRemove: V,
          removeLabel: v(E.label),
          draggable: b,
          onDragStart: b ? (G) => ue(x, G) : void 0,
          onDragOver: b ? (G) => he(x, G) : void 0,
          onDrop: b ? fe : void 0,
          onDragEnd: b ? ve : void 0,
          dragClassName: b ? F : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, y && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: K,
        "aria-label": L["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, _ ? "▲" : "▼"))
  ), I && hl.createPortal(I, document.body));
}, { useCallback: Ve, useRef: El } = e, _t = "application/x-tl-color", gl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const a = El(null), u = Ve(
    (m) => (p) => {
      a.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), c = Ve((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Ve(
    (m) => (p) => {
      p.preventDefault();
      const b = p.dataTransfer.getData(_t);
      b ? s(m, b) : a.current !== null && a.current !== m && i(a.current, m), a.current = null;
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
        onDragOver: c,
        onDrop: o(p)
      }
    ))
  );
};
function bt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function vt(l) {
  if (!qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Et(l, t, n) {
  const r = (i) => bt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Cl(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, a = Math.max(r, i, s), u = Math.min(r, i, s), c = a - u;
  let o = 0;
  c !== 0 && (a === r ? o = (i - s) / c % 6 : a === i ? o = (s - r) / c + 2 : o = (r - i) / c + 4, o *= 60, o < 0 && (o += 360));
  const m = a === 0 ? 0 : c / a;
  return [o, m, a];
}
function yl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let a = 0, u = 0, c = 0;
  return l < 60 ? (a = r, u = i, c = 0) : l < 120 ? (a = i, u = r, c = 0) : l < 180 ? (a = 0, u = r, c = i) : l < 240 ? (a = 0, u = i, c = r) : l < 300 ? (a = i, u = 0, c = r) : (a = r, u = 0, c = i), [
    Math.round((a + s) * 255),
    Math.round((u + s) * 255),
    Math.round((c + s) * 255)
  ];
}
function wl(l) {
  return Cl(...vt(l));
}
function Ke(l, t, n) {
  return Et(...yl(l, t, n));
}
const { useCallback: we, useRef: mt } = e, kl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = wl(l), s = mt(null), a = mt(null), u = we(
    (f, v) => {
      var M;
      const _ = (M = s.current) == null ? void 0 : M.getBoundingClientRect();
      if (!_) return;
      const S = Math.max(0, Math.min(1, (f - _.left) / _.width)), C = Math.max(0, Math.min(1, 1 - (v - _.top) / _.height));
      t(Ke(n, S, C));
    },
    [n, t]
  ), c = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), o = we(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = we(
    (f) => {
      var C;
      const v = (C = a.current) == null ? void 0 : C.getBoundingClientRect();
      if (!v) return;
      const S = Math.max(0, Math.min(1, (f - v.top) / v.height)) * 360;
      t(Ke(S, r, i));
    },
    [r, i, t]
  ), p = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), b = we(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), L = Ke(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: L },
      onPointerDown: c,
      onPointerMove: o
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
      ref: a,
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
function Sl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Nl = {
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
}, { useState: Ie, useCallback: pe, useEffect: Ye, useRef: Rl, useLayoutEffect: Tl } = e, xl = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: s,
  onConfirm: a,
  onCancel: u,
  onPaletteChange: c
}) => {
  const [o, m] = Ie("palette"), [p, b] = Ie(t), L = Rl(null), f = ae(Nl), [v, _] = Ie(null);
  Tl(() => {
    if (!l.current || !L.current) return;
    const A = l.current.getBoundingClientRect(), j = L.current.getBoundingClientRect();
    let Q = A.bottom + 4, d = A.left;
    Q + j.height > window.innerHeight && (Q = A.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, A.right - j.width)), _({ top: Q, left: d });
  }, [l]);
  const S = p != null, [C, M, H] = S ? vt(p) : [0, 0, 0], [h, w] = Ie((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ye(() => {
    w((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ye(() => {
    const A = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ye(() => {
    const A = (Q) => {
      L.current && !L.current.contains(Q.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const k = pe(
    (A) => (j) => {
      const Q = parseInt(j.target.value, 10);
      if (isNaN(Q)) return;
      const d = bt(Q);
      b(Et(A === "r" ? d : C, A === "g" ? d : M, A === "b" ? d : H));
    },
    [C, M, H]
  ), Y = pe(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(_t, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), A.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), R = pe((A) => {
    const j = A.target.value;
    w(j), qe(j) && b(j);
  }, []), T = pe(() => {
    b(null);
  }, []), O = pe((A) => {
    b(A);
  }, []), N = pe(
    (A) => {
      a(A);
    },
    [a]
  ), B = pe(
    (A, j) => {
      const Q = [...n], d = Q[A];
      Q[A] = Q[j], Q[j] = d, c(Q);
    },
    [n, c]
  ), q = pe(
    (A, j) => {
      const Q = [...n];
      Q[A] = j, c(Q);
    },
    [n, c]
  ), ne = pe(() => {
    c([...i]);
  }, [i, c]), W = pe(
    (A) => {
      if (Sl(n, A)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const Q = [...n];
      Q[j] = A.toUpperCase(), c(Q);
    },
    [n, c]
  ), J = pe(() => {
    p != null && W(p), a(p);
  }, [p, a, W]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: L,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      gl,
      {
        colors: n,
        columns: r,
        onSelect: O,
        onConfirm: N,
        onSwap: B,
        onReplace: q
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ne }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(kl, { color: p ?? "#000000", onColorChange: b }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (S ? "" : " tlColorInput--noColor"),
        style: S ? { backgroundColor: p } : void 0,
        draggable: S,
        onDragStart: S ? Y : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? C : "",
        onChange: k("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? M : "",
        onChange: k("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? H : "",
        onChange: k("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !qe(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, f["js.colorInput.ok"]))
  );
}, Ll = { "js.colorInput.chooseColor": "Choose color" }, { useState: Dl, useCallback: je, useRef: Il } = e, jl = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Ll), [i, s] = Dl(!1), a = Il(null), u = t.value, c = t.editable !== !1, o = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? o, b = je(() => {
    c && s(!0);
  }, [c]), L = je(
    (_) => {
      s(!1), n("valueChanged", { value: _ });
    },
    [n]
  ), f = je(() => {
    s(!1);
  }, []), v = je(
    (_) => {
      n("paletteChanged", { palette: _ });
    },
    [n]
  );
  return c ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    xl,
    {
      anchorRef: a,
      currentColor: u,
      palette: o,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: L,
      onCancel: f,
      onPaletteChange: v
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
}, { useState: Re, useCallback: be, useEffect: Pe, useRef: pt, useLayoutEffect: Pl, useMemo: Ml } = e, Al = {
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
}, Bl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: a
}) => {
  const u = ae(Al), [c, o] = Re("simple"), [m, p] = Re(""), [b, L] = Re(t ?? ""), [f, v] = Re(!1), [_, S] = Re(null), C = pt(null), M = pt(null);
  Pl(() => {
    if (!l.current || !C.current) return;
    const N = l.current.getBoundingClientRect(), B = C.current.getBoundingClientRect();
    let q = N.bottom + 4, ne = N.left;
    q + B.height > window.innerHeight && (q = N.top - B.height - 4), ne + B.width > window.innerWidth && (ne = Math.max(0, N.right - B.width)), S({ top: q, left: ne });
  }, [l]), Pe(() => {
    !r && !f && a().catch(() => v(!0));
  }, [r, f, a]), Pe(() => {
    r && M.current && M.current.focus();
  }, [r]), Pe(() => {
    const N = (B) => {
      B.key === "Escape" && s();
    };
    return document.addEventListener("keydown", N), () => document.removeEventListener("keydown", N);
  }, [s]), Pe(() => {
    const N = (q) => {
      C.current && !C.current.contains(q.target) && s();
    }, B = setTimeout(() => document.addEventListener("mousedown", N), 0);
    return () => {
      clearTimeout(B), document.removeEventListener("mousedown", N);
    };
  }, [s]);
  const H = Ml(() => {
    if (!m) return n;
    const N = m.toLowerCase();
    return n.filter(
      (B) => B.prefix.toLowerCase().includes(N) || B.label.toLowerCase().includes(N) || B.terms != null && B.terms.some((q) => q.includes(N))
    );
  }, [n, m]), h = be((N) => {
    p(N.target.value);
  }, []), w = be(
    (N) => {
      i(N);
    },
    [i]
  ), k = be((N) => {
    L(N);
  }, []), Y = be((N) => {
    L(N.target.value);
  }, []), R = be(() => {
    i(b || null);
  }, [b, i]), T = be(() => {
    i(null);
  }, [i]), O = be(async (N) => {
    N.preventDefault(), v(!1);
    try {
      await a();
    } catch {
      v(!0);
    }
  }, [a]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: C,
      style: _ ? { top: _.top, left: _.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (c === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (c === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: M,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: h,
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
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: O }, u["js.iconSelect.loadError"])),
      r && H.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && H.map(
        (N) => N.variants.map((B) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: B.encoded,
            className: "tlIconSelect__iconCell" + (B.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": B.encoded === t,
            tabIndex: 0,
            title: N.label,
            onClick: () => c === "simple" ? w(B.encoded) : k(B.encoded),
            onKeyDown: (q) => {
              (q.key === "Enter" || q.key === " ") && (q.preventDefault(), c === "simple" ? w(B.encoded) : k(B.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ke, { encoded: B.encoded })
        ))
      )
    ),
    c === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: b,
        onChange: Y
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, b && /* @__PURE__ */ e.createElement(ke, { encoded: b })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, b ? b.startsWith("css:") ? b.substring(4) : b : ""))),
    c === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, Ol = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: $l, useCallback: Me, useRef: Fl } = e, Hl = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Ol), [i, s] = $l(!1), a = Fl(null), u = t.value, c = t.editable !== !1, o = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, b = Me(() => {
    c && !o && s(!0);
  }, [c, o]), L = Me(
    (_) => {
      s(!1), n("valueChanged", { value: _ });
    },
    [n]
  ), f = Me(() => {
    s(!1);
  }, []), v = Me(async () => {
    await n("loadIcons");
  }, [n]);
  return c ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: o,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(ke, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Bl,
    {
      anchorRef: a,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: L,
      onCancel: f,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(ke, { encoded: u }) : null));
};
z("TLButton", Pt);
z("TLToggleButton", At);
z("TLTextInput", wt);
z("TLNumberInput", St);
z("TLDatePicker", Rt);
z("TLSelect", xt);
z("TLCheckbox", Dt);
z("TLTable", It);
z("TLCounter", Bt);
z("TLTabBar", $t);
z("TLFieldList", Ft);
z("TLAudioRecorder", Wt);
z("TLAudioPlayer", Ut);
z("TLFileUpload", Kt);
z("TLDownload", Gt);
z("TLPhotoCapture", qt);
z("TLPhotoViewer", Qt);
z("TLSplitPanel", Jt);
z("TLPanel", an);
z("TLMaximizeRoot", sn);
z("TLDeckPane", cn);
z("TLSidebar", bn);
z("TLStack", vn);
z("TLGrid", En);
z("TLCard", gn);
z("TLAppBar", Cn);
z("TLBreadcrumb", wn);
z("TLBottomBar", Sn);
z("TLDialog", Rn);
z("TLDialogManager", Ln);
z("TLWindow", jn);
z("TLDrawer", Bn);
z("TLSnackbar", Hn);
z("TLMenu", zn);
z("TLAppShell", Un);
z("TLText", Vn);
z("TLTableView", Yn);
z("TLFormLayout", el);
z("TLFormGroup", ll);
z("TLFormField", sl);
z("TLResourceCell", cl);
z("TLTreeView", ul);
z("TLDropdownSelect", vl);
z("TLColorInput", jl);
z("TLIconSelect", Hl);
