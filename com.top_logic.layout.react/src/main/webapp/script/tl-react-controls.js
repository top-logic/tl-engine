import { React as e, useTLFieldValue as Te, getComponent as yt, useTLState as Z, useTLCommand as oe, TLChild as q, useTLUpload as Ze, useI18N as se, useTLDataUrl as Qe, register as z } from "tl-react-bridge";
const { useCallback: wt } = e, kt = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = wt(
    (o) => {
      n(o.target.value);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "");
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = t.errorMessage, a = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0,
      title: u && i ? i : void 0
    }
  ));
}, { useCallback: St } = e, Nt = ({ controlId: l, state: t, config: r }) => {
  const [n, c] = Te(), u = St(
    (m) => {
      const p = m.target.value;
      c(p === "" ? null : p);
    },
    [c]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "");
  const s = t.hasError === !0, i = t.hasWarnings === !0, a = t.errorMessage, o = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && i ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: r != null && r.decimal ? "decimal" : "numeric",
      value: n != null ? String(n) : "",
      onChange: u,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && a ? a : void 0
    }
  ));
}, { useCallback: Rt } = e, Tt = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = Rt(
    (a) => {
      n(a.target.value || null);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "");
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = [
    "tlReactDatePicker",
    u ? "tlReactDatePicker--error" : "",
    !u && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    }
  ));
}, { useCallback: xt } = e, Lt = ({ controlId: l, state: t, config: r }) => {
  var m;
  const [n, c] = Te(), u = xt(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), s = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = s.find((f) => f.value === n)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const i = t.hasError === !0, a = t.hasWarnings === !0, o = [
    "tlReactSelect",
    i ? "tlReactSelect--error" : "",
    !i && a ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: u,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": i || void 0
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = Dt(
    (a) => {
      n(a.target.checked);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        checked: r === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && s ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    }
  );
}, Mt = ({ controlId: l, state: t }) => {
  const r = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, r.map((s) => {
    const i = s.cellModule ? yt(s.cellModule) : void 0, a = c[s.name];
    if (i) {
      const o = { value: a, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        i,
        {
          controlId: l + "-" + u + "-" + s.name,
          state: o
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, a != null ? String(a) : "");
  })))));
};
function ke({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const r = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const r = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const { useCallback: Pt } = e, jt = ({ controlId: l, command: t, label: r, disabled: n }) => {
  const c = Z(), u = oe(), s = t ?? "click", i = r ?? c.label, a = n ?? c.disabled === !0, o = c.hidden === !0, m = c.image, p = c.iconOnly === !0, f = o ? { display: "none" } : void 0, g = Pt(() => {
    u(s);
  }, [u, s]);
  return m && p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: g,
      disabled: a,
      style: f,
      className: "tlReactButton tlReactButton--icon",
      title: i,
      "aria-label": i
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: m })
  ) : m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: g,
      disabled: a,
      style: f,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: g,
      disabled: a,
      style: f,
      className: "tlReactButton"
    },
    i
  );
}, { useCallback: Bt } = e, At = ({ controlId: l, command: t, label: r, active: n, disabled: c }) => {
  const u = Z(), s = oe(), i = t ?? "click", a = r ?? u.label, o = n ?? u.active === !0, m = c ?? u.disabled === !0, p = Bt(() => {
    s(i);
  }, [s, i]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    a
  );
}, Ot = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: $t } = e, Ft = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.tabs ?? [], c = t.activeTabId, u = $t((s) => {
    s !== c && r("selectTab", { tabId: s });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === c,
      className: "tlReactTabBar__tab" + (s.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => u(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, Ht = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: c })))));
}, Wt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, zt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), a = e.useRef([]), o = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : n !== "idle" ? n : m, g = e.useCallback(async () => {
    if (n === "recording") {
      const y = i.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (n !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = y, a.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(y, L ? { mimeType: L } : void 0);
        i.current = j, j.ondataavailable = (v) => {
          v.data.size > 0 && a.current.push(v.data);
        }, j.onstop = async () => {
          y.getTracks().forEach((_) => _.stop()), o.current = null;
          const v = new Blob(a.current, { type: j.mimeType || "audio/webm" });
          if (a.current = [], v.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const w = new FormData();
          w.append("audio", v, "recording.webm"), await r(w), c("idle");
        }, j.start(), c("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), s("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, r]), h = se(Wt), E = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], b = f === "uploading", N = ["tlAudioRecorder__button"];
  return f === "recording" && N.push("tlAudioRecorder__button--recording"), f === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: g,
      disabled: b,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[u]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Ut = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Vt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [u, s] = e.useState(n ? "idle" : "disabled"), i = e.useRef(null), a = e.useRef(null), o = e.useRef(c);
  e.useEffect(() => {
    n ? u === "disabled" && s("idle") : (i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), s("disabled"));
  }, [n]), e.useEffect(() => {
    c !== o.current && (o.current = c, i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (u === "playing" || u === "paused" || u === "loading") && s("idle"));
  }, [c]), e.useEffect(() => () => {
    i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
  }, []);
  const m = e.useCallback(async () => {
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
    if (!a.current) {
      s("loading");
      try {
        const b = await fetch(r);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), s("idle");
          return;
        }
        const N = await b.blob();
        a.current = URL.createObjectURL(N);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), s("idle");
        return;
      }
    }
    const E = new Audio(a.current);
    i.current = E, E.onended = () => {
      s("idle");
    }, E.play(), s("playing");
  }, [u, r]), p = se(Ut), f = u === "loading" ? p["js.loading"] : u === "playing" ? p["js.audioPlayer.pause"] : u === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], g = u === "disabled" || u === "loading", h = ["tlAudioPlayer__button"];
  return u === "playing" && h.push("tlAudioPlayer__button--playing"), u === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: m,
      disabled: g,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Kt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Yt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), a = t.status ?? "idle", o = t.error, m = t.accept ?? "", p = a === "received" ? "idle" : n !== "idle" ? n : a, f = e.useCallback(async (v) => {
    c("uploading");
    const w = new FormData();
    w.append("file", v, v.name), await r(w), c("idle");
  }, [r]), g = e.useCallback((v) => {
    var _;
    const w = (_ = v.target.files) == null ? void 0 : _[0];
    w && f(w);
  }, [f]), h = e.useCallback(() => {
    var v;
    n !== "uploading" && ((v = i.current) == null || v.click());
  }, [n]), E = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!0);
  }, []), b = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!1);
  }, []), N = e.useCallback((v) => {
    var _;
    if (v.preventDefault(), v.stopPropagation(), s(!1), n === "uploading") return;
    const w = (_ = v.dataTransfer.files) == null ? void 0 : _[0];
    w && f(w);
  }, [n, f]), y = p === "uploading", L = se(Kt), j = p === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: b,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: i,
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
        onClick: h,
        disabled: y,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Gt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Xt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = oe(), c = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [a, o] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || a)) {
      o(!0);
      try {
        const h = r + (r.includes("?") ? "&" : "?") + "rev=" + u, E = await fetch(h);
        if (!E.ok) {
          console.error("[TLDownload] Failed to fetch data:", E.status);
          return;
        }
        const b = await E.blob(), N = URL.createObjectURL(b), y = document.createElement("a");
        y.href = N, y.download = s, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(N);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [c, a, r, u, s]), p = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), f = se(Gt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const g = a ? f["js.downloading"] : f["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: a,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), i && /* @__PURE__ */ e.createElement(
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
}, qt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(null), [i, a] = e.useState(!1), o = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), g = e.useRef(null), h = t.error, E = e.useMemo(
    () => {
      var k;
      return !!(window.isSecureContext && ((k = navigator.mediaDevices) != null && k.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((k) => k.stop()), m.current = null), o.current && (o.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    b(), c("idle");
  }, [b]), y = e.useCallback(async () => {
    var k;
    if (n !== "uploading") {
      if (s(null), !E) {
        (k = f.current) == null || k.click();
        return;
      }
      try {
        const M = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = M, c("overlayOpen");
      } catch (M) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", M), s("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [n, E]), L = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const k = o.current, M = p.current;
    if (!k || !M)
      return;
    M.width = k.videoWidth, M.height = k.videoHeight;
    const x = M.getContext("2d");
    x && (x.drawImage(k, 0, 0), b(), c("uploading"), M.toBlob(async (F) => {
      if (!F) {
        c("idle");
        return;
      }
      const Y = new FormData();
      Y.append("photo", F, "capture.jpg"), await r(Y), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, r, b]), j = e.useCallback(async (k) => {
    var F;
    const M = (F = k.target.files) == null ? void 0 : F[0];
    if (!M) return;
    c("uploading");
    const x = new FormData();
    x.append("photo", M, M.name), await r(x), c("idle"), f.current && (f.current.value = "");
  }, [r]);
  e.useEffect(() => {
    n === "overlayOpen" && o.current && m.current && (o.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var M;
    if (n !== "overlayOpen") return;
    (M = g.current) == null || M.focus();
    const k = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = k;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const k = (M) => {
      M.key === "Escape" && N();
    };
    return document.addEventListener("keydown", k), () => document.removeEventListener("keydown", k);
  }, [n, N]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((k) => k.stop()), m.current = null);
  }, []);
  const v = se(qt), w = n === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], _ = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && _.push("tlPhotoCapture__cameraBtn--uploading");
  const $ = ["tlPhotoCapture__overlayVideo"];
  i && $.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return i && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: y,
      disabled: n === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !E && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: g,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: $.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
        onClick: () => a((k) => !k),
        title: v["js.photoCapture.mirror"],
        "aria-label": v["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: L,
        title: v["js.photoCapture.capture"],
        "aria-label": v["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: v["js.photoCapture.close"],
        "aria-label": v["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[u]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, Qt = {
  "js.photoViewer.alt": "Captured photo"
}, Jt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      u && (URL.revokeObjectURL(u), s(null));
      return;
    }
    if (c === i.current && u)
      return;
    i.current = c, u && (URL.revokeObjectURL(u), s(null));
    let o = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        o || s(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      o = !0;
    };
  }, [n, c, r]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const a = se(Qt);
  return !n || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, { useCallback: lt, useRef: $e } = e, en = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.orientation, c = t.resizable === !0, u = t.children ?? [], s = n === "horizontal", i = u.length > 0 && u.every((b) => b.collapsed), a = !i && u.some((b) => b.collapsed), o = i ? !s : s, m = $e(null), p = $e(null), f = $e(null), g = lt((b, N) => {
    const y = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? i && !o ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : N !== void 0 ? y.flex = `0 0 ${N}px` : b.unit === "%" || a ? y.flex = `${b.size} 0 0%` : y.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (y.minWidth = s ? b.minSize : void 0, y.minHeight = s ? void 0 : b.minSize), y;
  }, [s, i, a, o]), h = lt((b, N) => {
    b.preventDefault();
    const y = m.current;
    if (!y) return;
    const L = u[N], j = u[N + 1], v = y.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    v.forEach((S) => {
      w.push(s ? S.offsetWidth : S.offsetHeight);
    }), f.current = w, p.current = {
      splitterIndex: N,
      startPos: s ? b.clientX : b.clientY,
      startSizeBefore: w[N],
      startSizeAfter: w[N + 1],
      childBefore: L,
      childAfter: j
    };
    const _ = (S) => {
      const k = p.current;
      if (!k || !f.current) return;
      const x = (s ? S.clientX : S.clientY) - k.startPos, F = k.childBefore.minSize || 0, Y = k.childAfter.minSize || 0;
      let re = k.startSizeBefore + x, W = k.startSizeAfter - x;
      re < F && (W += re - F, re = F), W < Y && (re += W - Y, W = Y), f.current[k.splitterIndex] = re, f.current[k.splitterIndex + 1] = W;
      const J = y.querySelectorAll(":scope > .tlSplitPanel__child"), O = J[k.splitterIndex], P = J[k.splitterIndex + 1];
      O && (O.style.flex = `0 0 ${re}px`), P && (P.style.flex = `0 0 ${W}px`);
    }, $ = () => {
      if (document.removeEventListener("mousemove", _), document.removeEventListener("mouseup", $), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const S = {};
        u.forEach((k, M) => {
          const x = k.control;
          x != null && x.controlId && f.current && (S[x.controlId] = f.current[M]);
        }), r("updateSizes", { sizes: S });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", _), document.addEventListener("mouseup", $), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, s, r]), E = [];
  return u.forEach((b, N) => {
    if (E.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${b.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(b)
        },
        /* @__PURE__ */ e.createElement(q, { control: b.control })
      )
    ), c && N < u.length - 1) {
      const y = u[N + 1];
      !b.collapsed && !y.collapsed && E.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (j) => h(j, N)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${i ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    E
  );
}, { useCallback: Fe } = e, tn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), on = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), sn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(tn), c = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, m = t.toolbarButtons ?? [], p = u === "MINIMIZED", f = u === "MAXIMIZED", g = u === "HIDDEN", h = Fe(() => {
    r("toggleMinimize");
  }, [r]), E = Fe(() => {
    r("toggleMaximize");
  }, [r]), b = Fe(() => {
    r("popOut");
  }, [r]);
  if (g)
    return null;
  const N = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: N
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((y, L) => /* @__PURE__ */ e.createElement("span", { key: L, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: y }))), s && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: p ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(ln, null) : /* @__PURE__ */ e.createElement(nn, null)
    ), i && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(on, null) : /* @__PURE__ */ e.createElement(rn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(an, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child }))
  );
}, cn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(q, { control: t.child })
  );
}, un = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(q, { control: t.activeChild }));
}, { useCallback: de, useState: Be, useEffect: Ae, useRef: Oe } = e, dn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ge(l, t, r, n) {
  const c = [];
  for (const u of l)
    u.type === "nav" ? c.push({ id: u.id, type: "nav", groupId: n }) : u.type === "command" ? c.push({ id: u.id, type: "command", groupId: n }) : u.type === "group" && (c.push({ id: u.id, type: "group" }), (r.get(u.id) ?? u.expanded) && !t && c.push(...Ge(u.children, t, r, u.id)));
  return c;
}
const Se = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, mn = ({ item: l, active: t, collapsed: r, onSelect: n, tabIndex: c, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: r ? l.label : void 0,
    tabIndex: c,
    ref: u,
    onFocus: () => s(l.id)
  },
  r && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !r && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), pn = ({ item: l, collapsed: t, onExecute: r, tabIndex: n, itemRef: c, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), fn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), hn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), _n = ({ item: l, activeItemId: t, anchorRect: r, onSelect: n, onExecute: c, onClose: u }) => {
  const s = Oe(null);
  Ae(() => {
    const o = (m) => {
      s.current && !s.current.contains(m.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [u]), Ae(() => {
    const o = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [u]);
  const i = de((o) => {
    o.type === "nav" ? (n(o.id), u()) : o.type === "command" && (c(o.id), u());
  }, [n, c, u]), a = {};
  return r && (a.left = r.right, a.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" || o.type === "command") {
      const m = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => i(o)
        },
        /* @__PURE__ */ e.createElement(Se, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, bn = ({
  item: l,
  expanded: t,
  activeItemId: r,
  collapsed: n,
  onSelect: c,
  onExecute: u,
  onToggleGroup: s,
  tabIndex: i,
  itemRef: a,
  onFocus: o,
  focusedId: m,
  setItemRef: p,
  onItemFocus: f,
  flyoutGroupId: g,
  onOpenFlyout: h,
  onCloseFlyout: E
}) => {
  const b = Oe(null), [N, y] = Be(null), L = de(() => {
    n ? g === l.id ? E() : (b.current && y(b.current.getBoundingClientRect()), h(l.id)) : s(l.id);
  }, [n, g, l.id, s, h, E]), j = de((w) => {
    b.current = w, a(w);
  }, [a]), v = n && g === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: L,
      title: n ? l.label : void 0,
      "aria-expanded": n ? v : t,
      tabIndex: i,
      ref: j,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !n && /* @__PURE__ */ e.createElement(
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
    _n,
    {
      item: l,
      activeItemId: r,
      anchorRect: N,
      onSelect: c,
      onExecute: u,
      onClose: E
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((w) => /* @__PURE__ */ e.createElement(
    ht,
    {
      key: w.id,
      item: w,
      activeItemId: r,
      collapsed: n,
      onSelect: c,
      onExecute: u,
      onToggleGroup: s,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: h,
      onCloseFlyout: E
    }
  ))));
}, ht = ({
  item: l,
  activeItemId: t,
  collapsed: r,
  onSelect: n,
  onExecute: c,
  onToggleGroup: u,
  focusedId: s,
  setItemRef: i,
  onItemFocus: a,
  groupStates: o,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        mn,
        {
          item: l,
          active: l.id === t,
          collapsed: r,
          onSelect: n,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        pn,
        {
          item: l,
          collapsed: r,
          onExecute: c,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(fn, { item: l, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(hn, null);
    case "group": {
      const g = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        bn,
        {
          item: l,
          expanded: g,
          activeItemId: t,
          collapsed: r,
          onSelect: n,
          onExecute: c,
          onToggleGroup: u,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a,
          focusedId: s,
          setItemRef: i,
          onItemFocus: a,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, vn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(dn), c = t.items ?? [], u = t.activeItemId, s = t.collapsed, [i, a] = Be(() => {
    const S = /* @__PURE__ */ new Map(), k = (M) => {
      for (const x of M)
        x.type === "group" && (S.set(x.id, x.expanded), k(x.children));
    };
    return k(c), S;
  }), o = de((S) => {
    a((k) => {
      const M = new Map(k), x = M.get(S) ?? !1;
      return M.set(S, !x), r("toggleGroup", { itemId: S, expanded: !x }), M;
    });
  }, [r]), m = de((S) => {
    S !== u && r("selectItem", { itemId: S });
  }, [r, u]), p = de((S) => {
    r("executeCommand", { itemId: S });
  }, [r]), f = de(() => {
    r("toggleCollapse", {});
  }, [r]), [g, h] = Be(null), E = de((S) => {
    h(S);
  }, []), b = de(() => {
    h(null);
  }, []);
  Ae(() => {
    s || h(null);
  }, [s]);
  const [N, y] = Be(() => {
    const S = Ge(c, s, i);
    return S.length > 0 ? S[0].id : "";
  }), L = Oe(/* @__PURE__ */ new Map()), j = de((S) => (k) => {
    k ? L.current.set(S, k) : L.current.delete(S);
  }, []), v = de((S) => {
    y(S);
  }, []), w = Oe(0), _ = de((S) => {
    y(S), w.current++;
  }, []);
  Ae(() => {
    const S = L.current.get(N);
    S && document.activeElement !== S && S.focus();
  }, [N, w.current]);
  const $ = de((S) => {
    if (S.key === "Escape" && g !== null) {
      S.preventDefault(), b();
      return;
    }
    const k = Ge(c, s, i);
    if (k.length === 0) return;
    const M = k.findIndex((F) => F.id === N);
    if (M < 0) return;
    const x = k[M];
    switch (S.key) {
      case "ArrowDown": {
        S.preventDefault();
        const F = (M + 1) % k.length;
        _(k[F].id);
        break;
      }
      case "ArrowUp": {
        S.preventDefault();
        const F = (M - 1 + k.length) % k.length;
        _(k[F].id);
        break;
      }
      case "Home": {
        S.preventDefault(), _(k[0].id);
        break;
      }
      case "End": {
        S.preventDefault(), _(k[k.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        S.preventDefault(), x.type === "nav" ? m(x.id) : x.type === "command" ? p(x.id) : x.type === "group" && (s ? g === x.id ? b() : E(x.id) : o(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !s && ((i.get(x.id) ?? !1) || (S.preventDefault(), o(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !s && (i.get(x.id) ?? !1) && (S.preventDefault(), o(x.id));
        break;
      }
    }
  }, [
    c,
    s,
    i,
    N,
    g,
    _,
    m,
    p,
    o,
    E,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: $ }, c.map((S) => /* @__PURE__ */ e.createElement(
    ht,
    {
      key: S.id,
      item: S,
      activeItemId: u,
      collapsed: s,
      onSelect: m,
      onExecute: p,
      onToggleGroup: o,
      focusedId: N,
      setItemRef: j,
      onItemFocus: v,
      groupStates: i,
      flyoutGroupId: g,
      onOpenFlyout: E,
      onCloseFlyout: b
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: s ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: s ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, En = ({ controlId: l }) => {
  const t = Z(), r = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", u = t.wrap === !0, s = t.children ?? [], i = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: i }, s.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })));
}, gn = ({ controlId: l }) => {
  const t = Z(), r = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", u = t.children ?? [], s = {};
  return n ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : r && (s.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: s }, u.map((i, a) => /* @__PURE__ */ e.createElement(q, { key: a, control: i })));
}, Cn = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = r != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(q, { control: s })));
}, yn = ({ controlId: l }) => {
  const t = Z(), r = t.title ?? "", n = t.leading, c = t.actions ?? [], u = t.variant ?? "flat", i = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: i }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a }))));
}, { useCallback: wn } = e, kn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.items ?? [], c = wn((u) => {
    r("navigate", { itemId: u });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((u, s) => {
    const i = s === n.length - 1;
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
        onClick: () => c(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: Sn } = e, Nn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.items ?? [], c = t.activeItemId, u = Sn((s) => {
    s !== c && r("selectItem", { itemId: s });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((s) => {
    const i = s.id === c;
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
}, { useCallback: rt, useEffect: ot, useRef: Rn } = e, Tn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.open === !0, c = t.closeOnBackdrop !== !1, u = t.child, s = Rn(null), i = rt(() => {
    r("close");
  }, [r]), a = rt((o) => {
    c && o.target === o.currentTarget && i();
  }, [c, i]);
  return ot(() => {
    if (!n) return;
    const o = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [n, i]), ot(() => {
    n && s.current && s.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(q, { control: u })
  ) : null;
}, { useEffect: xn, useRef: Ln } = e, Dn = ({ controlId: l }) => {
  const r = Z().dialogs ?? [], n = Ln(r.length);
  return xn(() => {
    r.length < n.current && r.length > 0, n.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, r.map((c) => /* @__PURE__ */ e.createElement(q, { key: c.controlId, control: c })));
}, { useCallback: xe, useRef: ge, useState: Le } = e, In = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Mn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Pn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(In), c = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, a = t.resizable === !0, o = t.child, m = t.actions ?? [], p = t.toolbarButtons ?? [], [f, g] = Le(null), [h, E] = Le(null), [b, N] = Le(null), y = ge(null), [L, j] = Le(!1), v = ge(null), w = ge(null), _ = ge(null), $ = ge(null), S = ge(null), k = xe(() => {
    r("close");
  }, [r]), M = xe((W, J) => {
    J.preventDefault();
    const O = $.current;
    if (!O) return;
    const P = O.getBoundingClientRect(), Q = !y.current, d = y.current ?? { x: P.left, y: P.top };
    Q && (y.current = d, N(d)), S.current = {
      dir: W,
      startX: J.clientX,
      startY: J.clientY,
      startW: P.width,
      startH: P.height,
      startPos: { ...d },
      symmetric: Q
    };
    const C = (B) => {
      const D = S.current;
      if (!D) return;
      const K = B.clientX - D.startX, G = B.clientY - D.startY;
      let le = D.startW, ee = D.startH, ue = 0, me = 0;
      D.symmetric ? (D.dir.includes("e") && (le = D.startW + 2 * K), D.dir.includes("w") && (le = D.startW - 2 * K), D.dir.includes("s") && (ee = D.startH + 2 * G), D.dir.includes("n") && (ee = D.startH - 2 * G)) : (D.dir.includes("e") && (le = D.startW + K), D.dir.includes("w") && (le = D.startW - K, ue = K), D.dir.includes("s") && (ee = D.startH + G), D.dir.includes("n") && (ee = D.startH - G, me = G));
      const he = Math.max(200, le), R = Math.max(100, ee);
      D.symmetric ? (ue = (D.startW - he) / 2, me = (D.startH - R) / 2) : (D.dir.includes("w") && he === 200 && (ue = D.startW - 200), D.dir.includes("n") && R === 100 && (me = D.startH - 100)), w.current = he, _.current = R, g(he), E(R);
      const I = {
        x: D.startPos.x + ue,
        y: D.startPos.y + me
      };
      y.current = I, N(I);
    }, H = () => {
      document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", H);
      const B = w.current, D = _.current;
      (B != null || D != null) && r("resize", {
        ...B != null ? { width: Math.round(B) + "px" } : {},
        ...D != null ? { height: Math.round(D) + "px" } : {}
      }), S.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", H);
  }, [r]), x = xe((W) => {
    if (W.button !== 0 || W.target.closest("button")) return;
    W.preventDefault();
    const J = $.current;
    if (!J) return;
    const O = J.getBoundingClientRect(), P = y.current ?? { x: O.left, y: O.top }, Q = W.clientX - P.x, d = W.clientY - P.y, C = (B) => {
      const D = window.innerWidth, K = window.innerHeight;
      let G = B.clientX - Q, le = B.clientY - d;
      const ee = J.offsetWidth, ue = J.offsetHeight;
      G + ee > D && (G = D - ee), le + ue > K && (le = K - ue), G < 0 && (G = 0), le < 0 && (le = 0);
      const me = { x: G, y: le };
      y.current = me, N(me);
    }, H = () => {
      document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", H);
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", H);
  }, []), F = xe(() => {
    var W, J;
    if (L) {
      const O = v.current;
      O && (N(O.x !== -1 ? { x: O.x, y: O.y } : null), g(O.w), E(O.h)), j(!1);
    } else {
      const O = $.current, P = O == null ? void 0 : O.getBoundingClientRect();
      v.current = {
        x: ((W = y.current) == null ? void 0 : W.x) ?? (P == null ? void 0 : P.left) ?? -1,
        y: ((J = y.current) == null ? void 0 : J.y) ?? (P == null ? void 0 : P.top) ?? -1,
        w: f ?? (P == null ? void 0 : P.width) ?? null,
        h: h ?? null
      }, j(!0), N({ x: 0, y: 0 }), g(null), E(null);
    }
  }, [L, f, h]), Y = L ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: f != null ? f + "px" : u,
    ...h != null ? { height: h + "px" } : s != null ? { height: s } : {},
    ...i != null && h == null ? { minHeight: i } : {},
    maxHeight: b ? "100vh" : "80vh",
    ...b ? { position: "absolute", left: b.x + "px", top: b.y + "px" } : {}
  }, re = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Y,
      ref: $,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": re
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : x,
        onDoubleClick: F
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: re }, c),
      p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, p.map((W, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: W })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: F,
          title: L ? n["js.window.restore"] : n["js.window.maximize"]
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
          onClick: k,
          title: n["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(q, { control: o })),
    m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m.map((W, J) => /* @__PURE__ */ e.createElement(q, { key: J, control: W }))),
    a && !L && Mn.map((W) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: W,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${W}`,
        onMouseDown: (J) => M(W, J)
      }
    ))
  );
}, { useCallback: jn, useEffect: Bn } = e, An = {
  "js.drawer.close": "Close"
}, On = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(An), c = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, a = t.child, o = jn(() => {
    r("close");
  }, [r]);
  Bn(() => {
    if (!c) return;
    const p = (f) => {
      f.key === "Escape" && o();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, o]);
  const m = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${s}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, i !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, i), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
      title: n["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(q, { control: a })));
}, { useCallback: $n, useEffect: Fn, useState: Hn } = e, Wn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.message ?? "", c = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, a = t.generation ?? 0, [o, m] = Hn(!1), p = $n(() => {
    m(!0), setTimeout(() => {
      r("dismiss", { generation: a }), m(!1);
    }, 200);
  }, [r, a]);
  return Fn(() => {
    if (!i || s === 0) return;
    const f = setTimeout(p, s);
    return () => clearTimeout(f);
  }, [i, s, p]), !i && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${u}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n)
  );
}, { useCallback: He, useEffect: We, useRef: zn, useState: at } = e, Un = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.open === !0, c = t.anchorId, u = t.items ?? [], s = zn(null), [i, a] = at({ top: 0, left: 0 }), [o, m] = at(0), p = u.filter((E) => E.type === "item" && !E.disabled);
  We(() => {
    var v, w;
    if (!n || !c) return;
    const E = document.getElementById(c);
    if (!E) return;
    const b = E.getBoundingClientRect(), N = ((v = s.current) == null ? void 0 : v.offsetHeight) ?? 200, y = ((w = s.current) == null ? void 0 : w.offsetWidth) ?? 200;
    let L = b.bottom + 4, j = b.left;
    L + N > window.innerHeight && (L = b.top - N - 4), j + y > window.innerWidth && (j = b.right - y), a({ top: L, left: j }), m(0);
  }, [n, c]);
  const f = He(() => {
    r("close");
  }, [r]), g = He((E) => {
    r("selectItem", { itemId: E });
  }, [r]);
  We(() => {
    if (!n) return;
    const E = (b) => {
      s.current && !s.current.contains(b.target) && f();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [n, f]);
  const h = He((E) => {
    if (E.key === "Escape") {
      f();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), m((b) => (b + 1) % p.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), m((b) => (b - 1 + p.length) % p.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const b = p[o];
      b && g(b.id);
    }
  }, [f, g, p, o]);
  return We(() => {
    n && s.current && s.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: i.top, left: i.left },
      onKeyDown: h
    },
    u.map((E, b) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
      const y = p.indexOf(E) === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: E.id,
          type: "button",
          className: "tlMenu__item" + (y ? " tlMenu__item--focused" : "") + (E.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: E.disabled,
          tabIndex: y ? 0 : -1,
          onClick: () => g(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + E.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, Vn = ({ controlId: l }) => {
  const t = Z(), r = t.header, n = t.content, c = t.footer, u = t.snackbar, s = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: c })), /* @__PURE__ */ e.createElement(q, { control: u }), s && /* @__PURE__ */ e.createElement(q, { control: s }));
}, Kn = () => {
  const t = Z().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Yn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, st = 50, Gn = () => {
  const l = Z(), t = oe(), r = se(Yn), n = l.columns ?? [], c = l.totalRowCount ?? 0, u = l.rows ?? [], s = l.rowHeight ?? 36, i = l.selectionMode ?? "single", a = l.selectedCount ?? 0, o = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, p = e.useMemo(
    () => n.filter((R) => R.sortPriority && R.sortPriority > 0).length,
    [n]
  ), f = i === "multi", g = 40, h = 20, E = e.useRef(null), b = e.useRef(null), N = e.useRef(null), [y, L] = e.useState({}), j = e.useRef(null), v = e.useRef(!1), w = e.useRef(null), [_, $] = e.useState(null), [S, k] = e.useState(null);
  e.useEffect(() => {
    j.current || L({});
  }, [n]);
  const M = e.useCallback((R) => y[R.name] ?? R.width, [y]), x = e.useMemo(() => {
    const R = [];
    let I = f && o > 0 ? g : 0;
    for (let X = 0; X < o && X < n.length; X++)
      R.push(I), I += M(n[X]);
    return R;
  }, [n, o, f, g, M]), F = c * s, Y = e.useRef(null), re = e.useCallback((R, I, X) => {
    X.preventDefault(), X.stopPropagation(), j.current = { column: R, startX: X.clientX, startWidth: I };
    let te = X.clientX, T = 0;
    const A = () => {
      const ae = j.current;
      if (!ae) return;
      const pe = Math.max(st, ae.startWidth + (te - ae.startX) + T);
      L((Ee) => ({ ...Ee, [ae.column]: pe }));
    }, U = () => {
      const ae = b.current, pe = E.current;
      if (!ae || !j.current) return;
      const Ee = ae.getBoundingClientRect(), et = 40, tt = 8, Ct = ae.scrollLeft;
      te > Ee.right - et ? ae.scrollLeft += tt : te < Ee.left + et && (ae.scrollLeft = Math.max(0, ae.scrollLeft - tt));
      const nt = ae.scrollLeft - Ct;
      nt !== 0 && (pe && (pe.scrollLeft = ae.scrollLeft), T += nt, A()), Y.current = requestAnimationFrame(U);
    };
    Y.current = requestAnimationFrame(U);
    const ne = (ae) => {
      te = ae.clientX, A();
    }, _e = (ae) => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", _e), Y.current !== null && (cancelAnimationFrame(Y.current), Y.current = null);
      const pe = j.current;
      if (pe) {
        const Ee = Math.max(st, pe.startWidth + (ae.clientX - pe.startX) + T);
        t("columnResize", { column: pe.column, width: Ee }), j.current = null, v.current = !0, requestAnimationFrame(() => {
          v.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", _e);
  }, [t]), W = e.useCallback(() => {
    E.current && b.current && (E.current.scrollLeft = b.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const R = b.current;
      if (!R) return;
      const I = R.scrollTop, X = Math.ceil(R.clientHeight / s), te = Math.floor(I / s);
      t("scroll", { start: te, count: X });
    }, 80);
  }, [t, s]), J = e.useCallback((R, I, X) => {
    if (v.current) return;
    let te;
    !I || I === "desc" ? te = "asc" : te = "desc";
    const T = X.shiftKey ? "add" : "replace";
    t("sort", { column: R, direction: te, mode: T });
  }, [t]), O = e.useCallback((R, I) => {
    w.current = R, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", R);
  }, []), P = e.useCallback((R, I) => {
    if (!w.current || w.current === R) {
      $(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const X = I.currentTarget.getBoundingClientRect(), te = I.clientX < X.left + X.width / 2 ? "left" : "right";
    $({ column: R, side: te });
  }, []), Q = e.useCallback((R) => {
    R.preventDefault(), R.stopPropagation();
    const I = w.current;
    if (!I || !_) {
      w.current = null, $(null);
      return;
    }
    let X = n.findIndex((T) => T.name === _.column);
    if (X < 0) {
      w.current = null, $(null);
      return;
    }
    const te = n.findIndex((T) => T.name === I);
    _.side === "right" && X++, te < X && X--, t("columnReorder", { column: I, targetIndex: X }), w.current = null, $(null);
  }, [n, _, t]), d = e.useCallback(() => {
    w.current = null, $(null);
  }, []), C = e.useCallback((R, I) => {
    I.shiftKey && I.preventDefault(), t("select", {
      rowIndex: R,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [t]), H = e.useCallback((R, I) => {
    I.stopPropagation(), t("select", { rowIndex: R, ctrlKey: !0, shiftKey: !1 });
  }, [t]), B = e.useCallback(() => {
    const R = a === c && c > 0;
    t("selectAll", { selected: !R });
  }, [t, a, c]), D = e.useCallback((R, I, X) => {
    X.stopPropagation(), t("expand", { rowIndex: R, expanded: I });
  }, [t]), K = e.useCallback((R, I) => {
    I.preventDefault(), k({ x: I.clientX, y: I.clientY, colIdx: R });
  }, []), G = e.useCallback(() => {
    S && (t("setFrozenColumnCount", { count: S.colIdx + 1 }), k(null));
  }, [S, t]), le = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), k(null);
  }, [t]);
  e.useEffect(() => {
    if (!S) return;
    const R = () => k(null), I = (X) => {
      X.key === "Escape" && k(null);
    };
    return document.addEventListener("mousedown", R), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", R), document.removeEventListener("keydown", I);
    };
  }, [S]);
  const ee = n.reduce((R, I) => R + M(I), 0) + (f ? g : 0), ue = a === c && c > 0, me = a > 0 && a < c, he = e.useCallback((R) => {
    R && (R.indeterminate = me);
  }, [me]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (R) => {
        if (!w.current) return;
        R.preventDefault();
        const I = b.current, X = E.current;
        if (!I) return;
        const te = I.getBoundingClientRect(), T = 40, A = 8;
        R.clientX < te.left + T ? I.scrollLeft = Math.max(0, I.scrollLeft - A) : R.clientX > te.right - T && (I.scrollLeft += A), X && (X.scrollLeft = I.scrollLeft);
      },
      onDrop: Q
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ee } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (o > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: g,
          minWidth: g,
          ...o > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (R) => {
          w.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== w.current && $({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: he,
          className: "tlTableView__checkbox",
          checked: ue,
          onChange: B
        }
      )
    ), n.map((R, I) => {
      const X = M(R);
      n.length - 1;
      let te = "tlTableView__headerCell";
      R.sortable && (te += " tlTableView__headerCell--sortable"), _ && _.column === R.name && (te += " tlTableView__headerCell--dragOver-" + _.side);
      const T = I < o, A = I === o - 1;
      return T && (te += " tlTableView__headerCell--frozen"), A && (te += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.name,
          className: te,
          style: {
            width: X,
            minWidth: X,
            position: T ? "sticky" : "relative",
            ...T ? { left: x[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: R.sortable ? (U) => J(R.name, R.sortDirection, U) : void 0,
          onContextMenu: (U) => K(I, U),
          onDragStart: (U) => O(R.name, U),
          onDragOver: (U) => P(R.name, U),
          onDrop: Q,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, R.label),
        R.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, R.sortDirection === "asc" ? "▲" : "▼", p > 1 && R.sortPriority != null && R.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, R.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (U) => re(R.name, X, U)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (R) => {
          if (w.current && n.length > 0) {
            const I = n[n.length - 1];
            I.name !== w.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", $({ column: I.name, side: "right" }));
          }
        },
        onDrop: Q
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: W
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: F, position: "relative", width: ee } }, u.map((R) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.id,
          className: "tlTableView__row" + (R.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: R.index * s,
            height: s,
            width: ee
          },
          onClick: (I) => C(R.index, I)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (o > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: g,
              minWidth: g,
              ...o > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: R.selected,
              onChange: () => {
              },
              onClick: (I) => H(R.index, I),
              tabIndex: -1
            }
          )
        ),
        n.map((I, X) => {
          const te = M(I), T = X === n.length - 1, A = X < o, U = X === o - 1;
          let ne = "tlTableView__cell";
          A && (ne += " tlTableView__cell--frozen"), U && (ne += " tlTableView__cell--frozenLast");
          const _e = m && X === 0, ae = R.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: ne,
              style: {
                ...T && !A ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...A ? { position: "sticky", left: x[X], zIndex: 2 } : {}
              }
            },
            _e ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * h } }, R.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => D(R.index, !R.expanded, pe)
              },
              R.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(q, { control: R.cells[I.name] })) : /* @__PURE__ */ e.createElement(q, { control: R.cells[I.name] })
          );
        })
      )))
    ),
    S && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: S.y, left: S.x, zIndex: 1e4 },
        onMouseDown: (R) => R.stopPropagation()
      },
      S.colIdx + 1 !== o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: G }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      o > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: le }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Xn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, _t = e.createContext(Xn), { useMemo: qn, useRef: Zn, useState: Qn, useEffect: Jn } = e, el = 320, tl = ({ controlId: l }) => {
  const t = Z(), r = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Zn(null), [a, o] = Qn(
    n === "top" ? "top" : "side"
  );
  Jn(() => {
    if (n !== "auto") {
      o(n);
      return;
    }
    const h = i.current;
    if (!h) return;
    const E = new ResizeObserver((b) => {
      for (const N of b) {
        const L = N.contentRect.width / r;
        o(L < el ? "top" : "side");
      }
    });
    return E.observe(h), () => E.disconnect();
  }, [n, r]);
  const m = qn(() => ({
    readOnly: c,
    resolvedLabelPosition: a
  }), [c, a]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, g = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(_t.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: g, style: f, ref: i }, u.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h }))));
}, { useCallback: nl } = e, ll = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, rl = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(ll), c = t.header, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, m = t.children ?? [], p = c != null || u.length > 0 || s, f = nl(() => {
    r("toggleCollapse");
  }, [r]), g = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    i ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: g }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
      "aria-expanded": !i,
      title: i ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h }))));
}, { useContext: ol, useState: al, useCallback: sl } = e, cl = ({ controlId: l }) => {
  const t = Z(), r = ol(_t), n = t.label ?? "", c = t.required === !0, u = t.error, s = t.warnings, i = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? r.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.field, g = r.readOnly, [h, E] = al(!1), b = sl(() => E((j) => !j), []);
  if (!p) return null;
  const N = u != null, y = s != null && s.length > 0, L = [
    "tlFormField",
    `tlFormField--${o}`,
    g ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    N ? "tlFormField--error" : "",
    !N && y ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !g && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), i && !g && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: b,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: f })), !g && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, u)), !g && !N && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, s.map((j, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, j)))), !g && i && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, il = () => {
  const l = Z(), t = oe(), r = l.iconCss, n = l.iconSrc, c = l.label, u = l.cssClass, s = l.tooltip, i = l.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, o = /* @__PURE__ */ e.createElement(e.Fragment, null, a, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((f) => {
    f.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", u].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: s }, o) : /* @__PURE__ */ e.createElement("span", { className: p, title: s }, o);
}, ul = 20, dl = () => {
  const l = Z(), t = oe(), r = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), m = e.useRef(null), p = e.useCallback((v, w) => {
    t(w ? "collapse" : "expand", { nodeId: v });
  }, [t]), f = e.useCallback((v, w) => {
    t("select", {
      nodeId: v,
      ctrlKey: w.ctrlKey || w.metaKey,
      shiftKey: w.shiftKey
    });
  }, [t]), g = e.useCallback((v, w) => {
    w.preventDefault(), t("contextMenu", { nodeId: v, x: w.clientX, y: w.clientY });
  }, [t]), h = e.useRef(null), E = e.useCallback((v, w) => {
    const _ = w.getBoundingClientRect(), $ = v.clientY - _.top, S = _.height / 3;
    return $ < S ? "above" : $ > S * 2 ? "below" : "within";
  }, []), b = e.useCallback((v, w) => {
    w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", v);
  }, []), N = e.useCallback((v, w) => {
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const _ = E(w, w.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: v, position: _ }), h.current = null;
    }, 50);
  }, [t, E]), y = e.useCallback((v, w) => {
    w.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const _ = E(w, w.currentTarget);
    t("drop", { nodeId: v, position: _ });
  }, [t, E]), L = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((v) => {
    if (r.length === 0) return;
    let w = a;
    switch (v.key) {
      case "ArrowDown":
        v.preventDefault(), w = Math.min(a + 1, r.length - 1);
        break;
      case "ArrowUp":
        v.preventDefault(), w = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (v.preventDefault(), a >= 0 && a < r.length) {
          const _ = r[a];
          if (_.expandable && !_.expanded) {
            t("expand", { nodeId: _.id });
            return;
          } else _.expanded && (w = a + 1);
        }
        break;
      case "ArrowLeft":
        if (v.preventDefault(), a >= 0 && a < r.length) {
          const _ = r[a];
          if (_.expanded) {
            t("collapse", { nodeId: _.id });
            return;
          } else {
            const $ = _.depth;
            for (let S = a - 1; S >= 0; S--)
              if (r[S].depth < $) {
                w = S;
                break;
              }
          }
        }
        break;
      case "Enter":
        v.preventDefault(), a >= 0 && a < r.length && t("select", {
          nodeId: r[a].id,
          ctrlKey: v.ctrlKey || v.metaKey,
          shiftKey: v.shiftKey
        });
        return;
      case " ":
        v.preventDefault(), n === "multi" && a >= 0 && a < r.length && t("select", {
          nodeId: r[a].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        v.preventDefault(), w = 0;
        break;
      case "End":
        v.preventDefault(), w = r.length - 1;
        break;
      default:
        return;
    }
    w !== a && o(w);
  }, [a, r, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    r.map((v, w) => /* @__PURE__ */ e.createElement(
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
          w === a ? "tlTreeView__node--focused" : "",
          s === v.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === v.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === v.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: v.depth * ul },
        draggable: c,
        onClick: (_) => f(v.id, _),
        onContextMenu: (_) => g(v.id, _),
        onDragStart: (_) => b(v.id, _),
        onDragOver: u ? (_) => N(v.id, _) : void 0,
        onDrop: u ? (_) => y(v.id, _) : void 0,
        onDragEnd: L
      },
      v.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (_) => {
            _.stopPropagation(), p(v.id, v.expanded);
          },
          tabIndex: -1,
          "aria-label": v.expanded ? "Collapse" : "Expand"
        },
        v.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: v.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(q, { control: v.content }))
    ))
  );
};
var ze = { exports: {} }, ce = {}, Ue = { exports: {} }, V = {};
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
function ml() {
  if (ct) return V;
  ct = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), i = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function g(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var h = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, E = Object.assign, b = {};
  function N(d, C, H) {
    this.props = d, this.context = C, this.refs = b, this.updater = H || h;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(d, C) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, C, "setState");
  }, N.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = N.prototype;
  function L(d, C, H) {
    this.props = d, this.context = C, this.refs = b, this.updater = H || h;
  }
  var j = L.prototype = new y();
  j.constructor = L, E(j, N.prototype), j.isPureReactComponent = !0;
  var v = Array.isArray;
  function w() {
  }
  var _ = { H: null, A: null, T: null, S: null }, $ = Object.prototype.hasOwnProperty;
  function S(d, C, H) {
    var B = H.ref;
    return {
      $$typeof: l,
      type: d,
      key: C,
      ref: B !== void 0 ? B : null,
      props: H
    };
  }
  function k(d, C) {
    return S(d.type, C, d.props);
  }
  function M(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function x(d) {
    var C = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(H) {
      return C[H];
    });
  }
  var F = /\/+/g;
  function Y(d, C) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : C.toString(36);
  }
  function re(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(w, w) : (d.status = "pending", d.then(
          function(C) {
            d.status === "pending" && (d.status = "fulfilled", d.value = C);
          },
          function(C) {
            d.status === "pending" && (d.status = "rejected", d.reason = C);
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
  function W(d, C, H, B, D) {
    var K = typeof d;
    (K === "undefined" || K === "boolean") && (d = null);
    var G = !1;
    if (d === null) G = !0;
    else
      switch (K) {
        case "bigint":
        case "string":
        case "number":
          G = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              G = !0;
              break;
            case m:
              return G = d._init, W(
                G(d._payload),
                C,
                H,
                B,
                D
              );
          }
      }
    if (G)
      return D = D(d), G = B === "" ? "." + Y(d, 0) : B, v(D) ? (H = "", G != null && (H = G.replace(F, "$&/") + "/"), W(D, C, H, "", function(ue) {
        return ue;
      })) : D != null && (M(D) && (D = k(
        D,
        H + (D.key == null || d && d.key === D.key ? "" : ("" + D.key).replace(
          F,
          "$&/"
        ) + "/") + G
      )), C.push(D)), 1;
    G = 0;
    var le = B === "" ? "." : B + ":";
    if (v(d))
      for (var ee = 0; ee < d.length; ee++)
        B = d[ee], K = le + Y(B, ee), G += W(
          B,
          C,
          H,
          K,
          D
        );
    else if (ee = g(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(B = d.next()).done; )
        B = B.value, K = le + Y(B, ee++), G += W(
          B,
          C,
          H,
          K,
          D
        );
    else if (K === "object") {
      if (typeof d.then == "function")
        return W(
          re(d),
          C,
          H,
          B,
          D
        );
      throw C = String(d), Error(
        "Objects are not valid as a React child (found: " + (C === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : C) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return G;
  }
  function J(d, C, H) {
    if (d == null) return d;
    var B = [], D = 0;
    return W(d, B, "", "", function(K) {
      return C.call(H, K, D++);
    }), B;
  }
  function O(d) {
    if (d._status === -1) {
      var C = d._result;
      C = C(), C.then(
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = H);
        },
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = H);
        }
      ), d._status === -1 && (d._status = 0, d._result = C);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var C = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(C)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Q = {
    map: J,
    forEach: function(d, C, H) {
      J(
        d,
        function() {
          C.apply(this, arguments);
        },
        H
      );
    },
    count: function(d) {
      var C = 0;
      return J(d, function() {
        C++;
      }), C;
    },
    toArray: function(d) {
      return J(d, function(C) {
        return C;
      }) || [];
    },
    only: function(d) {
      if (!M(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return V.Activity = p, V.Children = Q, V.Component = N, V.Fragment = r, V.Profiler = c, V.PureComponent = L, V.StrictMode = n, V.Suspense = a, V.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = _, V.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return _.H.useMemoCache(d);
    }
  }, V.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, V.cacheSignal = function() {
    return null;
  }, V.cloneElement = function(d, C, H) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var B = E({}, d.props), D = d.key;
    if (C != null)
      for (K in C.key !== void 0 && (D = "" + C.key), C)
        !$.call(C, K) || K === "key" || K === "__self" || K === "__source" || K === "ref" && C.ref === void 0 || (B[K] = C[K]);
    var K = arguments.length - 2;
    if (K === 1) B.children = H;
    else if (1 < K) {
      for (var G = Array(K), le = 0; le < K; le++)
        G[le] = arguments[le + 2];
      B.children = G;
    }
    return S(d.type, D, B);
  }, V.createContext = function(d) {
    return d = {
      $$typeof: s,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: u,
      _context: d
    }, d;
  }, V.createElement = function(d, C, H) {
    var B, D = {}, K = null;
    if (C != null)
      for (B in C.key !== void 0 && (K = "" + C.key), C)
        $.call(C, B) && B !== "key" && B !== "__self" && B !== "__source" && (D[B] = C[B]);
    var G = arguments.length - 2;
    if (G === 1) D.children = H;
    else if (1 < G) {
      for (var le = Array(G), ee = 0; ee < G; ee++)
        le[ee] = arguments[ee + 2];
      D.children = le;
    }
    if (d && d.defaultProps)
      for (B in G = d.defaultProps, G)
        D[B] === void 0 && (D[B] = G[B]);
    return S(d, K, D);
  }, V.createRef = function() {
    return { current: null };
  }, V.forwardRef = function(d) {
    return { $$typeof: i, render: d };
  }, V.isValidElement = M, V.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: O
    };
  }, V.memo = function(d, C) {
    return {
      $$typeof: o,
      type: d,
      compare: C === void 0 ? null : C
    };
  }, V.startTransition = function(d) {
    var C = _.T, H = {};
    _.T = H;
    try {
      var B = d(), D = _.S;
      D !== null && D(H, B), typeof B == "object" && B !== null && typeof B.then == "function" && B.then(w, P);
    } catch (K) {
      P(K);
    } finally {
      C !== null && H.types !== null && (C.types = H.types), _.T = C;
    }
  }, V.unstable_useCacheRefresh = function() {
    return _.H.useCacheRefresh();
  }, V.use = function(d) {
    return _.H.use(d);
  }, V.useActionState = function(d, C, H) {
    return _.H.useActionState(d, C, H);
  }, V.useCallback = function(d, C) {
    return _.H.useCallback(d, C);
  }, V.useContext = function(d) {
    return _.H.useContext(d);
  }, V.useDebugValue = function() {
  }, V.useDeferredValue = function(d, C) {
    return _.H.useDeferredValue(d, C);
  }, V.useEffect = function(d, C) {
    return _.H.useEffect(d, C);
  }, V.useEffectEvent = function(d) {
    return _.H.useEffectEvent(d);
  }, V.useId = function() {
    return _.H.useId();
  }, V.useImperativeHandle = function(d, C, H) {
    return _.H.useImperativeHandle(d, C, H);
  }, V.useInsertionEffect = function(d, C) {
    return _.H.useInsertionEffect(d, C);
  }, V.useLayoutEffect = function(d, C) {
    return _.H.useLayoutEffect(d, C);
  }, V.useMemo = function(d, C) {
    return _.H.useMemo(d, C);
  }, V.useOptimistic = function(d, C) {
    return _.H.useOptimistic(d, C);
  }, V.useReducer = function(d, C, H) {
    return _.H.useReducer(d, C, H);
  }, V.useRef = function(d) {
    return _.H.useRef(d);
  }, V.useState = function(d) {
    return _.H.useState(d);
  }, V.useSyncExternalStore = function(d, C, H) {
    return _.H.useSyncExternalStore(
      d,
      C,
      H
    );
  }, V.useTransition = function() {
    return _.H.useTransition();
  }, V.version = "19.2.4", V;
}
var it;
function pl() {
  return it || (it = 1, Ue.exports = ml()), Ue.exports;
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
function fl() {
  if (ut) return ce;
  ut = 1;
  var l = pl();
  function t(a) {
    var o = "https://react.dev/errors/" + a;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        o += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + a + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function r() {
  }
  var n = {
    d: {
      f: r,
      r: function() {
        throw Error(t(522));
      },
      D: r,
      C: r,
      L: r,
      m: r,
      X: r,
      S: r,
      M: r
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function u(a, o, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: a,
      containerInfo: o,
      implementation: m
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function i(a, o) {
    if (a === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, ce.createPortal = function(a, o) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return u(a, o, null, m);
  }, ce.flushSync = function(a) {
    var o = s.T, m = n.p;
    try {
      if (s.T = null, n.p = 2, a) return a();
    } finally {
      s.T = o, n.p = m, n.d.f();
    }
  }, ce.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, n.d.C(a, o));
  }, ce.prefetchDNS = function(a) {
    typeof a == "string" && n.d.D(a);
  }, ce.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var m = o.as, p = i(m, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, g = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      m === "style" ? n.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: g
        }
      ) : m === "script" && n.d.X(a, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: g,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, ce.preinitModule = function(a, o) {
    if (typeof a == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var m = i(
            o.as,
            o.crossOrigin
          );
          n.d.M(a, {
            crossOrigin: m,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && n.d.M(a);
  }, ce.preload = function(a, o) {
    if (typeof a == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var m = o.as, p = i(m, o.crossOrigin);
      n.d.L(a, m, {
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
  }, ce.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var m = i(o.as, o.crossOrigin);
        n.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: m,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else n.d.m(a);
  }, ce.requestFormReset = function(a) {
    n.d.r(a);
  }, ce.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, ce.useFormState = function(a, o, m) {
    return s.H.useFormState(a, o, m);
  }, ce.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var dt;
function hl() {
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
  return l(), ze.exports = fl(), ze.exports;
}
var _l = hl();
const { useState: be, useCallback: ie, useRef: Ne, useEffect: Ce, useMemo: Xe } = e;
function Je({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function bl({
  option: l,
  removable: t,
  onRemove: r,
  removeLabel: n,
  draggable: c,
  onDragStart: u,
  onDragOver: s,
  onDrop: i,
  onDragEnd: a,
  dragClassName: o
}) {
  const m = ie(
    (p) => {
      p.stopPropagation(), r(l.value);
    },
    [r, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: c || void 0,
      onDragStart: u,
      onDragOver: s,
      onDrop: i,
      onDragEnd: a
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": n
      },
      "×"
    )
  );
}
function vl({
  option: l,
  highlighted: t,
  searchTerm: r,
  onSelect: n,
  onMouseEnter: c,
  id: u
}) {
  const s = ie(() => n(l.value), [n, l.value]), i = Xe(() => {
    if (!r) return l.label;
    const a = l.label.toLowerCase().indexOf(r.toLowerCase());
    return a < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, a), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(a, a + r.length)), l.label.substring(a + r.length));
  }, [l.label, r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: u,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const El = ({ controlId: l, state: t }) => {
  const r = oe(), n = t.value ?? [], c = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = u && c && !i && a, g = se({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = g["js.dropdownSelect.nothingFound"], E = ie(
    (T) => g["js.dropdownSelect.removeChip"].replace("{0}", T),
    [g]
  ), [b, N] = be(!1), [y, L] = be(""), [j, v] = be(-1), [w, _] = be(!1), [$, S] = be({}), [k, M] = be(null), [x, F] = be(null), [Y, re] = be(null), W = Ne(null), J = Ne(null), O = Ne(null), P = Ne(n);
  P.current = n;
  const Q = Ne(-1), d = Xe(
    () => new Set(n.map((T) => T.value)),
    [n]
  ), C = Xe(() => {
    let T = m.filter((A) => !d.has(A.value));
    if (y) {
      const A = y.toLowerCase();
      T = T.filter((U) => U.label.toLowerCase().includes(A));
    }
    return T;
  }, [m, d, y]);
  Ce(() => {
    y && C.length === 1 ? v(0) : v(-1);
  }, [C.length, y]), Ce(() => {
    b && o && J.current && J.current.focus();
  }, [b, o, n]), Ce(() => {
    var U, ne;
    if (Q.current < 0) return;
    const T = Q.current;
    Q.current = -1;
    const A = (U = W.current) == null ? void 0 : U.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    A && A.length > 0 ? A[Math.min(T, A.length - 1)].focus() : (ne = W.current) == null || ne.focus();
  }, [n]), Ce(() => {
    if (!b) return;
    const T = (A) => {
      W.current && !W.current.contains(A.target) && O.current && !O.current.contains(A.target) && (N(!1), L(""));
    };
    return document.addEventListener("mousedown", T), () => document.removeEventListener("mousedown", T);
  }, [b]), Ce(() => {
    if (!b || !W.current) return;
    const T = W.current.getBoundingClientRect(), A = window.innerHeight - T.bottom, ne = A < 300 && T.top > A;
    S({
      left: T.left,
      width: T.width,
      ...ne ? { bottom: window.innerHeight - T.top } : { top: T.bottom }
    });
  }, [b]);
  const H = ie(async () => {
    if (!(i || !a) && (N(!0), L(""), v(-1), _(!1), !o))
      try {
        await r("loadOptions");
      } catch {
        _(!0);
      }
  }, [i, a, o, r]), B = ie(() => {
    var T;
    N(!1), L(""), v(-1), (T = W.current) == null || T.focus();
  }, []), D = ie(
    (T) => {
      let A;
      if (c) {
        const U = m.find((ne) => ne.value === T);
        if (U)
          A = [...P.current, U];
        else
          return;
      } else {
        const U = m.find((ne) => ne.value === T);
        if (U)
          A = [U];
        else
          return;
      }
      P.current = A, r("valueChanged", { value: A.map((U) => U.value) }), c ? (L(""), v(-1)) : B();
    },
    [c, m, r, B]
  ), K = ie(
    (T) => {
      Q.current = P.current.findIndex((U) => U.value === T);
      const A = P.current.filter((U) => U.value !== T);
      P.current = A, r("valueChanged", { value: A.map((U) => U.value) });
    },
    [r]
  ), G = ie(
    (T) => {
      T.stopPropagation(), r("valueChanged", { value: [] }), B();
    },
    [r, B]
  ), le = ie((T) => {
    L(T.target.value);
  }, []), ee = ie(
    (T) => {
      if (!b) {
        if (T.key === "ArrowDown" || T.key === "ArrowUp" || T.key === "Enter" || T.key === " ") {
          if (T.target.tagName === "BUTTON") return;
          T.preventDefault(), T.stopPropagation(), H();
        }
        return;
      }
      switch (T.key) {
        case "ArrowDown":
          T.preventDefault(), T.stopPropagation(), v(
            (A) => A < C.length - 1 ? A + 1 : 0
          );
          break;
        case "ArrowUp":
          T.preventDefault(), T.stopPropagation(), v(
            (A) => A > 0 ? A - 1 : C.length - 1
          );
          break;
        case "Enter":
          T.preventDefault(), T.stopPropagation(), j >= 0 && j < C.length && D(C[j].value);
          break;
        case "Escape":
          T.preventDefault(), T.stopPropagation(), B();
          break;
        case "Tab":
          B();
          break;
        case "Backspace":
          y === "" && c && n.length > 0 && K(n[n.length - 1].value);
          break;
      }
    },
    [
      b,
      H,
      B,
      C,
      j,
      D,
      y,
      c,
      n,
      K
    ]
  ), ue = ie(
    async (T) => {
      T.preventDefault(), _(!1);
      try {
        await r("loadOptions");
      } catch {
        _(!0);
      }
    },
    [r]
  ), me = ie(
    (T, A) => {
      M(T), A.dataTransfer.effectAllowed = "move", A.dataTransfer.setData("text/plain", String(T));
    },
    []
  ), he = ie(
    (T, A) => {
      if (A.preventDefault(), A.dataTransfer.dropEffect = "move", k === null || k === T) {
        F(null), re(null);
        return;
      }
      const U = A.currentTarget.getBoundingClientRect(), ne = U.left + U.width / 2, _e = A.clientX < ne ? "before" : "after";
      F(T), re(_e);
    },
    [k]
  ), R = ie(
    (T) => {
      if (T.preventDefault(), k === null || x === null || Y === null || k === x) return;
      const A = [...P.current], [U] = A.splice(k, 1);
      let ne = x;
      k < x ? ne = Y === "before" ? ne - 1 : ne : ne = Y === "before" ? ne : ne + 1, A.splice(ne, 0, U), P.current = A, r("valueChanged", { value: A.map((_e) => _e.value) }), M(null), F(null), re(null);
    },
    [k, x, Y, r]
  ), I = ie(() => {
    M(null), F(null), re(null);
  }, []);
  if (Ce(() => {
    if (j < 0 || !O.current) return;
    const T = O.current.querySelector(
      `[id="${l}-opt-${j}"]`
    );
    T && T.scrollIntoView({ block: "nearest" });
  }, [j, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : n.map((T) => /* @__PURE__ */ e.createElement("span", { key: T.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Je, { image: T.image }), /* @__PURE__ */ e.createElement("span", null, T.label))));
  const X = !s && n.length > 0 && !i, te = b ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: O,
      className: "tlDropdownSelect__dropdown",
      style: $
    },
    (o || w) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: le,
        onKeyDown: ee,
        placeholder: g["js.dropdownSelect.filterPlaceholder"],
        "aria-label": g["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": j >= 0 ? `${l}-opt-${j}` : void 0,
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
      w && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ue }, g["js.dropdownSelect.error"])),
      o && C.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && C.map((T, A) => /* @__PURE__ */ e.createElement(
        vl,
        {
          key: T.value,
          id: `${l}-opt-${A}`,
          option: T,
          highlighted: A === j,
          searchTerm: y,
          onSelect: D,
          onMouseEnter: () => v(A)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: W,
      className: "tlDropdownSelect" + (b ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": b,
      "aria-haspopup": "listbox",
      "aria-owns": b ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: b ? void 0 : H,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : n.map((T, A) => {
      let U = "";
      return k === A ? U = "tlDropdownSelect__chip--dragging" : x === A && Y === "before" ? U = "tlDropdownSelect__chip--dropBefore" : x === A && Y === "after" && (U = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        bl,
        {
          key: T.value,
          option: T,
          removable: !i && (c || !s),
          onRemove: K,
          removeLabel: E(T.label),
          draggable: f,
          onDragStart: f ? (ne) => me(A, ne) : void 0,
          onDragOver: f ? (ne) => he(A, ne) : void 0,
          onDrop: f ? R : void 0,
          onDragEnd: f ? I : void 0,
          dragClassName: f ? U : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, X && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: G,
        "aria-label": g["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, b ? "▲" : "▼"))
  ), te && _l.createPortal(te, document.body));
}, { useCallback: Ve, useRef: gl } = e, bt = "application/x-tl-color", Cl = ({
  colors: l,
  columns: t,
  onSelect: r,
  onConfirm: n,
  onSwap: c,
  onReplace: u
}) => {
  const s = gl(null), i = Ve(
    (m) => (p) => {
      s.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Ve((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), o = Ve(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(bt);
      f ? u(m, f) : s.current !== null && s.current !== m && c(s.current, m), s.current = null;
    },
    [c, u]
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
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? i(p) : void 0,
        onDragOver: a,
        onDrop: o(p)
      }
    ))
  );
};
function vt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Et(l) {
  if (!qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function gt(l, t, r) {
  const n = (c) => vt(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(r);
}
function yl(l, t, r) {
  const n = l / 255, c = t / 255, u = r / 255, s = Math.max(n, c, u), i = Math.min(n, c, u), a = s - i;
  let o = 0;
  a !== 0 && (s === n ? o = (c - u) / a % 6 : s === c ? o = (u - n) / a + 2 : o = (n - c) / a + 4, o *= 60, o < 0 && (o += 360));
  const m = s === 0 ? 0 : a / s;
  return [o, m, s];
}
function wl(l, t, r) {
  const n = r * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), u = r - n;
  let s = 0, i = 0, a = 0;
  return l < 60 ? (s = n, i = c, a = 0) : l < 120 ? (s = c, i = n, a = 0) : l < 180 ? (s = 0, i = n, a = c) : l < 240 ? (s = 0, i = c, a = n) : l < 300 ? (s = c, i = 0, a = n) : (s = n, i = 0, a = c), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((a + u) * 255)
  ];
}
function kl(l) {
  return yl(...Et(l));
}
function Ke(l, t, r) {
  return gt(...wl(l, t, r));
}
const { useCallback: ye, useRef: mt } = e, Sl = ({ color: l, onColorChange: t }) => {
  const [r, n, c] = kl(l), u = mt(null), s = mt(null), i = ye(
    (h, E) => {
      var L;
      const b = (L = u.current) == null ? void 0 : L.getBoundingClientRect();
      if (!b) return;
      const N = Math.max(0, Math.min(1, (h - b.left) / b.width)), y = Math.max(0, Math.min(1, 1 - (E - b.top) / b.height));
      t(Ke(r, N, y));
    },
    [r, t]
  ), a = ye(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), i(h.clientX, h.clientY);
    },
    [i]
  ), o = ye(
    (h) => {
      h.buttons !== 0 && i(h.clientX, h.clientY);
    },
    [i]
  ), m = ye(
    (h) => {
      var y;
      const E = (y = s.current) == null ? void 0 : y.getBoundingClientRect();
      if (!E) return;
      const N = Math.max(0, Math.min(1, (h - E.top) / E.height)) * 360;
      t(Ke(N, n, c));
    },
    [n, c, t]
  ), p = ye(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), m(h.clientY);
    },
    [m]
  ), f = ye(
    (h) => {
      h.buttons !== 0 && m(h.clientY);
    },
    [m]
  ), g = Ke(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
      onPointerDown: a,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: f
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${r / 360 * 100}%` }
      }
    )
  ));
};
function Nl(l, t) {
  const r = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === r);
}
const Rl = {
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
}, { useState: De, useCallback: fe, useEffect: Ye, useRef: Tl, useLayoutEffect: xl } = e, Ll = ({
  anchorRef: l,
  currentColor: t,
  palette: r,
  paletteColumns: n,
  defaultPalette: c,
  canReset: u,
  onConfirm: s,
  onCancel: i,
  onPaletteChange: a
}) => {
  const [o, m] = De("palette"), [p, f] = De(t), g = Tl(null), h = se(Rl), [E, b] = De(null);
  xl(() => {
    if (!l.current || !g.current) return;
    const O = l.current.getBoundingClientRect(), P = g.current.getBoundingClientRect();
    let Q = O.bottom + 4, d = O.left;
    Q + P.height > window.innerHeight && (Q = O.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, O.right - P.width)), b({ top: Q, left: d });
  }, [l]);
  const N = p != null, [y, L, j] = N ? Et(p) : [0, 0, 0], [v, w] = De((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ye(() => {
    w((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ye(() => {
    const O = (P) => {
      P.key === "Escape" && i();
    };
    return document.addEventListener("keydown", O), () => document.removeEventListener("keydown", O);
  }, [i]), Ye(() => {
    const O = (Q) => {
      g.current && !g.current.contains(Q.target) && i();
    }, P = setTimeout(() => document.addEventListener("mousedown", O), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", O);
    };
  }, [i]);
  const _ = fe(
    (O) => (P) => {
      const Q = parseInt(P.target.value, 10);
      if (isNaN(Q)) return;
      const d = vt(Q);
      f(gt(O === "r" ? d : y, O === "g" ? d : L, O === "b" ? d : j));
    },
    [y, L, j]
  ), $ = fe(
    (O) => {
      if (p != null) {
        O.dataTransfer.setData(bt, p.toUpperCase()), O.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), O.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), S = fe((O) => {
    const P = O.target.value;
    w(P), qe(P) && f(P);
  }, []), k = fe(() => {
    f(null);
  }, []), M = fe((O) => {
    f(O);
  }, []), x = fe(
    (O) => {
      s(O);
    },
    [s]
  ), F = fe(
    (O, P) => {
      const Q = [...r], d = Q[O];
      Q[O] = Q[P], Q[P] = d, a(Q);
    },
    [r, a]
  ), Y = fe(
    (O, P) => {
      const Q = [...r];
      Q[O] = P, a(Q);
    },
    [r, a]
  ), re = fe(() => {
    a([...c]);
  }, [c, a]), W = fe(
    (O) => {
      if (Nl(r, O)) return;
      const P = r.indexOf(null);
      if (P < 0) return;
      const Q = [...r];
      Q[P] = O.toUpperCase(), a(Q);
    },
    [r, a]
  ), J = fe(() => {
    p != null && W(p), s(p);
  }, [p, s, W]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: g,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Cl,
      {
        colors: r,
        columns: n,
        onSelect: M,
        onConfirm: x,
        onSwap: F,
        onReplace: Y
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: re }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Sl, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: p } : void 0,
        draggable: N,
        onDragStart: N ? $ : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? y : "",
        onChange: _("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? L : "",
        onChange: _("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? j : "",
        onChange: _("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !qe(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: k }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, h["js.colorInput.ok"]))
  );
}, Dl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Il, useCallback: Ie, useRef: Ml } = e, Pl = ({ controlId: l, state: t }) => {
  const r = oe(), n = se(Dl), [c, u] = Il(!1), s = Ml(null), i = t.value, a = t.editable !== !1, o = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? o, f = Ie(() => {
    a && u(!0);
  }, [a]), g = Ie(
    (b) => {
      u(!1), r("valueChanged", { value: b });
    },
    [r]
  ), h = Ie(() => {
    u(!1);
  }, []), E = Ie(
    (b) => {
      r("paletteChanged", { palette: b });
    },
    [r]
  );
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (i == null ? " tlColorInput__swatch--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      onClick: f,
      disabled: t.disabled === !0,
      title: i ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Ll,
    {
      anchorRef: s,
      currentColor: i,
      palette: o,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: g,
      onCancel: h,
      onPaletteChange: E
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (i == null ? " tlColorInput--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      title: i ?? ""
    }
  );
}, { useState: Re, useCallback: ve, useEffect: Me, useRef: pt, useLayoutEffect: jl, useMemo: Bl } = e, Al = {
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
}, Ol = ({
  anchorRef: l,
  currentValue: t,
  icons: r,
  iconsLoaded: n,
  onSelect: c,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = se(Al), [a, o] = Re("simple"), [m, p] = Re(""), [f, g] = Re(t ?? ""), [h, E] = Re(!1), [b, N] = Re(null), y = pt(null), L = pt(null);
  jl(() => {
    if (!l.current || !y.current) return;
    const x = l.current.getBoundingClientRect(), F = y.current.getBoundingClientRect();
    let Y = x.bottom + 4, re = x.left;
    Y + F.height > window.innerHeight && (Y = x.top - F.height - 4), re + F.width > window.innerWidth && (re = Math.max(0, x.right - F.width)), N({ top: Y, left: re });
  }, [l]), Me(() => {
    !n && !h && s().catch(() => E(!0));
  }, [n, h, s]), Me(() => {
    n && L.current && L.current.focus();
  }, [n]), Me(() => {
    const x = (F) => {
      F.key === "Escape" && u();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [u]), Me(() => {
    const x = (Y) => {
      y.current && !y.current.contains(Y.target) && u();
    }, F = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(F), document.removeEventListener("mousedown", x);
    };
  }, [u]);
  const j = Bl(() => {
    if (!m) return r;
    const x = m.toLowerCase();
    return r.filter(
      (F) => F.prefix.toLowerCase().includes(x) || F.label.toLowerCase().includes(x) || F.terms != null && F.terms.some((Y) => Y.includes(x))
    );
  }, [r, m]), v = ve((x) => {
    p(x.target.value);
  }, []), w = ve(
    (x) => {
      c(x);
    },
    [c]
  ), _ = ve((x) => {
    g(x);
  }, []), $ = ve((x) => {
    g(x.target.value);
  }, []), S = ve(() => {
    c(f || null);
  }, [f, c]), k = ve(() => {
    c(null);
  }, [c]), M = ve(async (x) => {
    x.preventDefault(), E(!1);
    try {
      await s();
    } catch {
      E(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      i["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      i["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: v,
        placeholder: i["js.iconSelect.filterPlaceholder"],
        "aria-label": i["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => p(""),
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
      !n && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: M }, i["js.iconSelect.loadError"])),
      n && j.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      n && j.map(
        (x) => x.variants.map((F) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: F.encoded,
            className: "tlIconSelect__iconCell" + (F.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": F.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => a === "simple" ? w(F.encoded) : _(F.encoded),
            onKeyDown: (Y) => {
              (Y.key === "Enter" || Y.key === " ") && (Y.preventDefault(), a === "simple" ? w(F.encoded) : _(F.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ke, { encoded: F.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: $
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ke, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: k }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, i["js.iconSelect.ok"]))
  );
}, $l = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Fl, useCallback: Pe, useRef: Hl } = e, Wl = ({ controlId: l, state: t }) => {
  const r = oe(), n = se($l), [c, u] = Fl(!1), s = Hl(null), i = t.value, a = t.editable !== !1, o = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, f = Pe(() => {
    a && !o && u(!0);
  }, [a, o]), g = Pe(
    (b) => {
      u(!1), r("valueChanged", { value: b });
    },
    [r]
  ), h = Pe(() => {
    u(!1);
  }, []), E = Pe(async () => {
    await r("loadIcons");
  }, [r]);
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (i == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: o,
      title: i ?? "",
      "aria-label": n["js.iconSelect.chooseIcon"]
    },
    i ? /* @__PURE__ */ e.createElement(ke, { encoded: i }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ol,
    {
      anchorRef: s,
      currentValue: i,
      icons: m,
      iconsLoaded: p,
      onSelect: g,
      onCancel: h,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(ke, { encoded: i }) : null));
}, { useCallback: we, useEffect: zl, useMemo: ft, useRef: Ul, useState: je } = e, Vl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Kl = [1, 2, 3, 4];
function Yl(l, t) {
  const r = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!r) return 16 * t;
  const n = parseFloat(r[1]), c = r[2] || "px";
  return c === "rem" || c === "em" ? n * t : n;
}
function Gl(l, t) {
  const r = Math.max(1, Math.floor(l / t));
  let n = 1;
  for (const c of Kl)
    r >= c && (n = c);
  return n;
}
function Xl(l, t) {
  const r = Vl[l] ?? 1;
  return Math.max(1, Math.round(r * t));
}
function ql(l, t) {
  const r = Math.max(1, t), n = {}, c = (p, f) => !!(n[p] && n[p][f]), u = (p, f) => {
    n[p] || (n[p] = {}), n[p][f] = !0;
  }, s = [];
  let i = 0, a = 0;
  const o = (p) => {
    let f = null;
    for (const h of s) h.rowStart === p && (f = h);
    if (!f) return;
    let g = f.colEnd;
    for (; g < r && !c(p, g); ) g++;
    if (g !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let E = f.colEnd; E < g; E++) u(h, E);
      f.colEnd = g;
    }
  };
  for (const p of l) {
    const f = r <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let g = Math.min(Xl(p.width, r), r);
    for (; c(i, a); )
      a++, a >= r && (a = 0, i++);
    let h = 0;
    for (let L = a; L < r && !c(i, L); L++)
      h++;
    if (g > h) {
      for (o(i), a = 0, i++; c(i, a); )
        a++, a >= r && (a = 0, i++);
      h = 0;
      for (let L = a; L < r && !c(i, L); L++)
        h++;
      g = Math.min(g, h);
    }
    const E = a, b = a + g, N = i, y = i + f;
    s.push({ id: p.id, colStart: E, colEnd: b, rowStart: N, rowEnd: y });
    for (let L = N; L < y; L++)
      for (let j = E; j < b; j++) u(L, j);
    a = b, a >= r && (a = 0, i++);
  }
  o(i);
  let m = 0;
  for (const p of s) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let f = 0; f < r; f++) {
      if (c(p, f)) continue;
      const g = s.find((h) => h.rowEnd === p && h.colStart <= f && f < h.colEnd);
      if (g) {
        g.rowEnd = p + 1;
        for (let h = g.colStart; h < g.colEnd; h++) u(p, h);
      }
    }
  return s;
}
const Zl = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((_) => _ && _.id), u = Ul(null), [s, i] = je(1), [a, o] = je(!1);
  zl(() => {
    const _ = u.current;
    if (!_) return;
    const $ = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, S = Yl(n, $), k = () => i(Gl(_.clientWidth, S));
    k();
    const M = new ResizeObserver(k);
    return M.observe(_), () => M.disconnect();
  }, [n]);
  const m = ft(() => ql(c, s), [c, s]), p = ft(() => {
    const _ = {};
    for (const $ of m) _[$.id] = $;
    return _;
  }, [m]), [f, g] = je(null), [h, E] = je(null), b = we((_, $) => {
    if (!a) {
      _.preventDefault();
      return;
    }
    g($), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", $);
  }, [a]), N = we((_, $) => {
    if (!a || !f || f === $) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const S = _.currentTarget.getBoundingClientRect(), k = _.clientX < S.left + S.width / 2;
    E((M) => M && M.id === $ && M.before === k ? M : { id: $, before: k });
  }, [a, f]), y = we(() => {
  }, []), L = we((_, $, S) => {
    const k = c.map((Y) => Y.id), M = k.indexOf(_);
    if (M < 0) return;
    k.splice(M, 1);
    const x = k.indexOf($);
    if (x < 0) {
      k.splice(M, 0, _);
      return;
    }
    const F = S ? x : x + 1;
    k.splice(F, 0, _), r("reorder", { order: k });
  }, [c, r]), j = we((_, $) => {
    if (!a || !f || f === $) return;
    _.preventDefault();
    const S = _.currentTarget.getBoundingClientRect(), k = _.clientX < S.left + S.width / 2;
    L(f, $, k), g(null), E(null);
  }, [a, f, L]), v = we(() => {
    g(null), E(null);
  }, []), w = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: u,
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__toolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDashboard__editBtn" + (a ? " tlDashboard__editBtn--active" : ""),
        onClick: () => o((_) => !_)
      },
      a ? "Done" : "Edit Layout"
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: w }, c.map((_) => {
      const $ = p[_.id];
      if (!$) return null;
      const S = {
        gridColumn: `${$.colStart + 1} / ${$.colEnd + 1}`,
        gridRow: `${$.rowStart + 1} / ${$.rowEnd + 1}`
      }, k = ["tlDashboard__tile"];
      return f === _.id && k.push("tlDashboard__tile--dragging"), h && h.id === _.id && k.push(h.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: k.join(" "),
          style: S,
          draggable: a,
          onDragStart: (M) => b(M, _.id),
          onDragOver: (M) => N(M, _.id),
          onDragLeave: y,
          onDrop: (M) => j(M, _.id),
          onDragEnd: v
        },
        /* @__PURE__ */ e.createElement(q, { control: _.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
};
z("TLButton", jt);
z("TLToggleButton", At);
z("TLTextInput", kt);
z("TLNumberInput", Nt);
z("TLDatePicker", Tt);
z("TLSelect", Lt);
z("TLCheckbox", It);
z("TLTable", Mt);
z("TLCounter", Ot);
z("TLTabBar", Ft);
z("TLFieldList", Ht);
z("TLAudioRecorder", zt);
z("TLAudioPlayer", Vt);
z("TLFileUpload", Yt);
z("TLDownload", Xt);
z("TLPhotoCapture", Zt);
z("TLPhotoViewer", Jt);
z("TLSplitPanel", en);
z("TLPanel", sn);
z("TLMaximizeRoot", cn);
z("TLDeckPane", un);
z("TLSidebar", vn);
z("TLStack", En);
z("TLGrid", gn);
z("TLCard", Cn);
z("TLAppBar", yn);
z("TLBreadcrumb", kn);
z("TLBottomBar", Nn);
z("TLDialog", Tn);
z("TLDialogManager", Dn);
z("TLWindow", Pn);
z("TLDrawer", On);
z("TLSnackbar", Wn);
z("TLMenu", Un);
z("TLAppShell", Vn);
z("TLTextCell", Kn);
z("TLTableView", Gn);
z("TLFormLayout", tl);
z("TLFormGroup", rl);
z("TLFormField", cl);
z("TLResourceCell", il);
z("TLTreeView", dl);
z("TLDropdownSelect", El);
z("TLColorInput", Pl);
z("TLIconSelect", Wl);
z("TLDashboard", Zl);
