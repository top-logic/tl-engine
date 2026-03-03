import { React as e, useTLFieldValue as V, getComponent as de, useTLState as S, useTLCommand as P, TLChild as x, useTLUpload as Q, useI18N as I, useTLDataUrl as ee, register as N } from "tl-react-bridge";
const { useCallback: ue } = e, me = ({ controlId: o, state: t }) => {
  const [s, n] = V(), r = ue(
    (a) => {
      n(a.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactTextInput tlReactTextInput--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: o,
      value: s ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: pe } = e, be = ({ controlId: o, state: t, config: s }) => {
  const [n, r] = V(), a = pe(
    (c) => {
      const i = c.target.value, d = i === "" ? null : Number(i);
      r(d);
    },
    [r]
  ), l = s != null && s.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: o,
      value: n != null ? String(n) : "",
      onChange: a,
      step: l,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: he } = e, fe = ({ controlId: o, state: t }) => {
  const [s, n] = V(), r = he(
    (a) => {
      n(a.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactDatePicker tlReactDatePicker--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: o,
      value: s ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: _e } = e, Ee = ({ controlId: o, state: t, config: s }) => {
  var c;
  const [n, r] = V(), a = _e(
    (i) => {
      r(i.target.value || null);
    },
    [r]
  ), l = t.options ?? (s == null ? void 0 : s.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = l.find((d) => d.value === n)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: o,
      value: n ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: ve } = e, Ce = ({ controlId: o, state: t }) => {
  const [s, n] = V(), r = ve(
    (a) => {
      n(a.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: o,
      checked: s === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: o,
      checked: s === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, ye = ({ controlId: o, state: t }) => {
  const s = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: o, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, s.map((r) => /* @__PURE__ */ e.createElement("th", { key: r.name }, r.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((r, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, s.map((l) => {
    const c = l.cellModule ? de(l.cellModule) : void 0, i = r[l.name];
    if (c) {
      const d = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: l.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: o + "-" + a + "-" + l.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: l.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: ge } = e, ke = ({ controlId: o, command: t, label: s, disabled: n }) => {
  const r = S(), a = P(), l = t ?? "click", c = s ?? r.label, i = n ?? r.disabled === !0, d = ge(() => {
    a(l);
  }, [a, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: o,
      onClick: d,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: we } = e, Ne = ({ controlId: o, command: t, label: s, active: n, disabled: r }) => {
  const a = S(), l = P(), c = t ?? "click", i = s ?? a.label, d = n ?? a.active === !0, h = r ?? a.disabled === !0, f = we(() => {
    l(c);
  }, [l, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: o,
      onClick: f,
      disabled: h,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    i
  );
}, Le = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Se } = e, xe = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.tabs ?? [], r = t.activeTabId, a = Se((l) => {
    l !== r && s("selectTab", { tabId: l });
  }, [s, r]);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === r,
      className: "tlReactTabBar__tab" + (l.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(x, { control: t.activeContent })));
}, Te = ({ controlId: o }) => {
  const t = S(), s = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((r, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(x, { control: r })))));
}, Re = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, De = ({ controlId: o }) => {
  const t = S(), s = Q(), [n, r] = e.useState("idle"), [a, l] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), h = t.status ?? "idle", f = t.error, _ = h === "received" ? "idle" : n !== "idle" ? n : h, k = e.useCallback(async () => {
    if (n === "recording") {
      const y = c.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (n !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = y, i.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(y, B ? { mimeType: B } : void 0);
        c.current = R, R.ondataavailable = (w) => {
          w.data.size > 0 && i.current.push(w.data);
        }, R.onstop = async () => {
          y.getTracks().forEach((D) => D.stop()), d.current = null;
          const w = new Blob(i.current, { type: R.mimeType || "audio/webm" });
          if (i.current = [], w.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const T = new FormData();
          T.append("audio", w, "recording.webm"), await s(T), r("idle");
        }, R.start(), r("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), l("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [n, s]), C = I(Re), u = _ === "recording" ? C["js.audioRecorder.stop"] : _ === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], p = _ === "uploading", g = ["tlAudioRecorder__button"];
  return _ === "recording" && g.push("tlAudioRecorder__button--recording"), _ === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: k,
      disabled: p,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, Be = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Pe = ({ controlId: o }) => {
  const t = S(), s = ee(), n = !!t.hasAudio, r = t.dataRevision ?? 0, [a, l] = e.useState(n ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(r);
  e.useEffect(() => {
    n ? a === "disabled" && l("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), l("disabled"));
  }, [n]), e.useEffect(() => {
    r !== d.current && (d.current = r, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && l("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const h = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      c.current && c.current.pause(), l("paused");
      return;
    }
    if (a === "paused" && c.current) {
      c.current.play(), l("playing");
      return;
    }
    if (!i.current) {
      l("loading");
      try {
        const p = await fetch(s);
        if (!p.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", p.status), l("idle");
          return;
        }
        const g = await p.blob();
        i.current = URL.createObjectURL(g);
      } catch (p) {
        console.error("[TLAudioPlayer] Fetch error:", p), l("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      l("idle");
    }, u.play(), l("playing");
  }, [a, s]), f = I(Be), _ = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], k = a === "disabled" || a === "loading", C = ["tlAudioPlayer__button"];
  return a === "playing" && C.push("tlAudioPlayer__button--playing"), a === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: h,
      disabled: k,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, je = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Fe = ({ controlId: o }) => {
  const t = S(), s = Q(), [n, r] = e.useState("idle"), [a, l] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, h = t.accept ?? "", f = i === "received" ? "idle" : n !== "idle" ? n : i, _ = e.useCallback(async (w) => {
    r("uploading");
    const T = new FormData();
    T.append("file", w, w.name), await s(T), r("idle");
  }, [s]), k = e.useCallback((w) => {
    var D;
    const T = (D = w.target.files) == null ? void 0 : D[0];
    T && _(T);
  }, [_]), C = e.useCallback(() => {
    var w;
    n !== "uploading" && ((w = c.current) == null || w.click());
  }, [n]), u = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), l(!0);
  }, []), p = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), l(!1);
  }, []), g = e.useCallback((w) => {
    var D;
    if (w.preventDefault(), w.stopPropagation(), l(!1), n === "uploading") return;
    const T = (D = w.dataTransfer.files) == null ? void 0 : D[0];
    T && _(T);
  }, [n, _]), y = f === "uploading", B = I(je), R = f === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: p,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: h || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: y,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
}, Me = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ie = ({ controlId: o }) => {
  const t = S(), s = ee(), n = P(), r = !!t.hasData, a = t.dataRevision ?? 0, l = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), h = e.useCallback(async () => {
    if (!(!r || i)) {
      d(!0);
      try {
        const C = s + (s.includes("?") ? "&" : "?") + "rev=" + a, u = await fetch(C);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const p = await u.blob(), g = URL.createObjectURL(p), y = document.createElement("a");
        y.href = g, y.download = l, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(g);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        d(!1);
      }
    }
  }, [r, i, s, a, l]), f = e.useCallback(async () => {
    r && await n("clear");
  }, [r, n]), _ = I(Me);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const k = i ? _["js.downloading"] : _["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: h,
      disabled: i,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: _["js.download.clear"],
      "aria-label": _["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, $e = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ae = ({ controlId: o }) => {
  const t = S(), s = Q(), [n, r] = e.useState("idle"), [a, l] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), h = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), k = e.useRef(null), C = t.error, u = e.useMemo(
    () => {
      var E;
      return !!(window.isSecureContext && ((E = navigator.mediaDevices) != null && E.getUserMedia));
    },
    []
  ), p = e.useCallback(() => {
    h.current && (h.current.getTracks().forEach((E) => E.stop()), h.current = null), d.current && (d.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    p(), r("idle");
  }, [p]), y = e.useCallback(async () => {
    var E;
    if (n !== "uploading") {
      if (l(null), !u) {
        (E = _.current) == null || E.click();
        return;
      }
      try {
        const m = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        h.current = m, r("overlayOpen");
      } catch (m) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", m), l("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [n, u]), B = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const E = d.current, m = f.current;
    if (!E || !m)
      return;
    m.width = E.videoWidth, m.height = E.videoHeight;
    const b = m.getContext("2d");
    b && (b.drawImage(E, 0, 0), p(), r("uploading"), m.toBlob(async (L) => {
      if (!L) {
        r("idle");
        return;
      }
      const M = new FormData();
      M.append("photo", L, "capture.jpg"), await s(M), r("idle");
    }, "image/jpeg", 0.85));
  }, [n, s, p]), R = e.useCallback(async (E) => {
    var L;
    const m = (L = E.target.files) == null ? void 0 : L[0];
    if (!m) return;
    r("uploading");
    const b = new FormData();
    b.append("photo", m, m.name), await s(b), r("idle"), _.current && (_.current.value = "");
  }, [s]);
  e.useEffect(() => {
    n === "overlayOpen" && d.current && h.current && (d.current.srcObject = h.current);
  }, [n]), e.useEffect(() => {
    var m;
    if (n !== "overlayOpen") return;
    (m = k.current) == null || m.focus();
    const E = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = E;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const E = (m) => {
      m.key === "Escape" && g();
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
  }, [n, g]), e.useEffect(() => () => {
    h.current && (h.current.getTracks().forEach((E) => E.stop()), h.current = null);
  }, []);
  const w = I($e), T = n === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], D = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && D.push("tlPhotoCapture__cameraBtn--uploading");
  const A = ["tlPhotoCapture__overlayVideo"];
  c && A.push("tlPhotoCapture__overlayVideo--mirrored");
  const v = ["tlPhotoCapture__mirrorBtn"];
  return c && v.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: D.join(" "),
      onClick: y,
      disabled: n === "uploading",
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !u && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: _,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: R
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: g }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: d,
        className: A.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: v.join(" "),
        onClick: () => i((E) => !E),
        title: w["js.photoCapture.mirror"],
        "aria-label": w["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: w["js.photoCapture.capture"],
        "aria-label": w["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: g,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[a]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, ze = {
  "js.photoViewer.alt": "Captured photo"
}, Ue = ({ controlId: o }) => {
  const t = S(), s = ee(), n = !!t.hasPhoto, r = t.dataRevision ?? 0, [a, l] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!n) {
      a && (URL.revokeObjectURL(a), l(null));
      return;
    }
    if (r === c.current && a)
      return;
    c.current = r, a && (URL.revokeObjectURL(a), l(null));
    let d = !1;
    return (async () => {
      try {
        const h = await fetch(s);
        if (!h.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", h.status);
          return;
        }
        const f = await h.blob();
        d || l(URL.createObjectURL(f));
      } catch (h) {
        console.error("[TLPhotoViewer] Fetch error:", h);
      }
    })(), () => {
      d = !0;
    };
  }, [n, r, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = I(ze);
  return !n || !a ? /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ae, useRef: K } = e, We = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.orientation, r = t.resizable === !0, a = t.children ?? [], l = n === "horizontal", c = a.length > 0 && a.every((p) => p.collapsed), i = !c && a.some((p) => p.collapsed), d = c ? !l : l, h = K(null), f = K(null), _ = K(null), k = ae((p, g) => {
    const y = {
      overflow: p.scrolling || "auto"
    };
    return p.collapsed ? c && !d ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : g !== void 0 ? y.flex = `0 0 ${g}px` : p.unit === "%" || i ? y.flex = `${p.size} 0 0%` : y.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (y.minWidth = l ? p.minSize : void 0, y.minHeight = l ? void 0 : p.minSize), y;
  }, [l, c, i, d]), C = ae((p, g) => {
    p.preventDefault();
    const y = h.current;
    if (!y) return;
    const B = a[g], R = a[g + 1], w = y.querySelectorAll(":scope > .tlSplitPanel__child"), T = [];
    w.forEach((v) => {
      T.push(l ? v.offsetWidth : v.offsetHeight);
    }), _.current = T, f.current = {
      splitterIndex: g,
      startPos: l ? p.clientX : p.clientY,
      startSizeBefore: T[g],
      startSizeAfter: T[g + 1],
      childBefore: B,
      childAfter: R
    };
    const D = (v) => {
      const E = f.current;
      if (!E || !_.current) return;
      const b = (l ? v.clientX : v.clientY) - E.startPos, L = E.childBefore.minSize || 0, M = E.childAfter.minSize || 0;
      let z = E.startSizeBefore + b, $ = E.startSizeAfter - b;
      z < L && ($ += z - L, z = L), $ < M && (z += $ - M, $ = M), _.current[E.splitterIndex] = z, _.current[E.splitterIndex + 1] = $;
      const j = y.querySelectorAll(":scope > .tlSplitPanel__child"), U = j[E.splitterIndex], W = j[E.splitterIndex + 1];
      U && (U.style.flex = `0 0 ${z}px`), W && (W.style.flex = `0 0 ${$}px`);
    }, A = () => {
      if (document.removeEventListener("mousemove", D), document.removeEventListener("mouseup", A), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const v = {};
        a.forEach((E, m) => {
          const b = E.control;
          b != null && b.controlId && _.current && (v[b.controlId] = _.current[m]);
        }), s("updateSizes", { sizes: v });
      }
      _.current = null, f.current = null;
    };
    document.addEventListener("mousemove", D), document.addEventListener("mouseup", A), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, l, s]), u = [];
  return a.forEach((p, g) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${p.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(p)
        },
        /* @__PURE__ */ e.createElement(x, { control: p.control })
      )
    ), r && g < a.length - 1) {
      const y = a[g + 1];
      !p.collapsed && !y.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (R) => C(R, g)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: h,
      id: o,
      className: `tlSplitPanel tlSplitPanel--${n}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    u
  );
}, { useCallback: G } = e, Oe = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ve = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), He = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ke = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ge = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ye = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Xe = ({ controlId: o }) => {
  const t = S(), s = P(), n = I(Oe), r = t.title, a = t.expansionState ?? "NORMALIZED", l = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], h = a === "MINIMIZED", f = a === "MAXIMIZED", _ = a === "HIDDEN", k = G(() => {
    s("toggleMinimize");
  }, [s]), C = G(() => {
    s("toggleMaximize");
  }, [s]), u = G(() => {
    s("popOut");
  }, [s]);
  if (_)
    return null;
  const p = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: p
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((g, y) => /* @__PURE__ */ e.createElement("span", { key: y, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(x, { control: g }))), l && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: h ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      h ? /* @__PURE__ */ e.createElement(He, null) : /* @__PURE__ */ e.createElement(Ve, null)
    ), c && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(Ge, null) : /* @__PURE__ */ e.createElement(Ke, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ye, null)
    ))),
    !h && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(x, { control: t.child }))
  );
}, qe = ({ controlId: o }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(x, { control: t.child })
  );
}, Ze = ({ controlId: o }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(x, { control: t.activeChild }));
}, { useCallback: F, useState: Y, useEffect: H, useRef: Z } = e, Je = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function J(o, t, s, n) {
  const r = [];
  for (const a of o)
    a.type === "nav" ? r.push({ id: a.id, type: "nav", groupId: n }) : a.type === "command" ? r.push({ id: a.id, type: "command", groupId: n }) : a.type === "group" && (r.push({ id: a.id, type: "group" }), (s.get(a.id) ?? a.expanded) && !t && r.push(...J(a.children, t, s, a.id)));
  return r;
}
const O = ({ icon: o }) => o ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + o, "aria-hidden": "true" }) : null, Qe = ({ item: o, active: t, collapsed: s, onSelect: n, tabIndex: r, itemRef: a, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(o.id),
    title: s ? o.label : void 0,
    tabIndex: r,
    ref: a,
    onFocus: () => l(o.id)
  },
  s && o.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(O, { icon: o.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, o.badge)) : /* @__PURE__ */ e.createElement(O, { icon: o.icon }),
  !s && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
  !s && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
), et = ({ item: o, collapsed: t, onExecute: s, tabIndex: n, itemRef: r, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => s(o.id),
    title: t ? o.label : void 0,
    tabIndex: n,
    ref: r,
    onFocus: () => a(o.id)
  },
  /* @__PURE__ */ e.createElement(O, { icon: o.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)
), tt = ({ item: o, collapsed: t }) => t && !o.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? o.label : void 0 }, /* @__PURE__ */ e.createElement(O, { icon: o.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)), at = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), nt = ({ item: o, activeItemId: t, onSelect: s, onExecute: n, onClose: r }) => {
  const a = Z(null);
  H(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [r]), H(() => {
    const c = (i) => {
      i.key === "Escape" && r();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [r]);
  const l = F((c) => {
    c.type === "nav" ? (s(c.id), r()) : c.type === "command" && (n(c.id), r());
  }, [s, n, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, o.label), o.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => l(c)
        },
        /* @__PURE__ */ e.createElement(O, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, lt = ({
  item: o,
  expanded: t,
  activeItemId: s,
  collapsed: n,
  onSelect: r,
  onExecute: a,
  onToggleGroup: l,
  tabIndex: c,
  itemRef: i,
  onFocus: d,
  focusedId: h,
  setItemRef: f,
  onItemFocus: _,
  flyoutGroupId: k,
  onOpenFlyout: C,
  onCloseFlyout: u
}) => {
  const p = F(() => {
    n ? k === o.id ? u() : C(o.id) : l(o.id);
  }, [n, k, o.id, l, C, u]), g = n && k === o.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (g ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: p,
      title: n ? o.label : void 0,
      "aria-expanded": n ? g : t,
      tabIndex: c,
      ref: i,
      onFocus: () => d(o.id)
    },
    /* @__PURE__ */ e.createElement(O, { icon: o.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
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
  ), g && /* @__PURE__ */ e.createElement(
    nt,
    {
      item: o,
      activeItemId: s,
      onSelect: r,
      onExecute: a,
      onClose: u
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, o.children.map((y) => /* @__PURE__ */ e.createElement(
    ce,
    {
      key: y.id,
      item: y,
      activeItemId: s,
      collapsed: n,
      onSelect: r,
      onExecute: a,
      onToggleGroup: l,
      focusedId: h,
      setItemRef: f,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: C,
      onCloseFlyout: u
    }
  ))));
}, ce = ({
  item: o,
  activeItemId: t,
  collapsed: s,
  onSelect: n,
  onExecute: r,
  onToggleGroup: a,
  focusedId: l,
  setItemRef: c,
  onItemFocus: i,
  groupStates: d,
  flyoutGroupId: h,
  onOpenFlyout: f,
  onCloseFlyout: _
}) => {
  switch (o.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Qe,
        {
          item: o,
          active: o.id === t,
          collapsed: s,
          onSelect: n,
          tabIndex: l === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        et,
        {
          item: o,
          collapsed: s,
          onExecute: r,
          tabIndex: l === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(tt, { item: o, collapsed: s });
    case "separator":
      return /* @__PURE__ */ e.createElement(at, null);
    case "group": {
      const k = d ? d.get(o.id) ?? o.expanded : o.expanded;
      return /* @__PURE__ */ e.createElement(
        lt,
        {
          item: o,
          expanded: k,
          activeItemId: t,
          collapsed: s,
          onSelect: n,
          onExecute: r,
          onToggleGroup: a,
          tabIndex: l === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i,
          focusedId: l,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: h,
          onOpenFlyout: f,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, ot = ({ controlId: o }) => {
  const t = S(), s = P(), n = I(Je), r = t.items ?? [], a = t.activeItemId, l = t.collapsed, [c, i] = Y(() => {
    const v = /* @__PURE__ */ new Map(), E = (m) => {
      for (const b of m)
        b.type === "group" && (v.set(b.id, b.expanded), E(b.children));
    };
    return E(r), v;
  }), d = F((v) => {
    i((E) => {
      const m = new Map(E), b = m.get(v) ?? !1;
      return m.set(v, !b), s("toggleGroup", { itemId: v, expanded: !b }), m;
    });
  }, [s]), h = F((v) => {
    v !== a && s("selectItem", { itemId: v });
  }, [s, a]), f = F((v) => {
    s("executeCommand", { itemId: v });
  }, [s]), _ = F(() => {
    s("toggleCollapse", {});
  }, [s]), [k, C] = Y(null), u = F((v) => {
    C(v);
  }, []), p = F(() => {
    C(null);
  }, []);
  H(() => {
    l || C(null);
  }, [l]);
  const [g, y] = Y(() => {
    const v = J(r, l, c);
    return v.length > 0 ? v[0].id : "";
  }), B = Z(/* @__PURE__ */ new Map()), R = F((v) => (E) => {
    E ? B.current.set(v, E) : B.current.delete(v);
  }, []), w = F((v) => {
    y(v);
  }, []), T = Z(0), D = F((v) => {
    y(v), T.current++;
  }, []);
  H(() => {
    const v = B.current.get(g);
    v && document.activeElement !== v && v.focus();
  }, [g, T.current]);
  const A = F((v) => {
    if (v.key === "Escape" && k !== null) {
      v.preventDefault(), p();
      return;
    }
    const E = J(r, l, c);
    if (E.length === 0) return;
    const m = E.findIndex((L) => L.id === g);
    if (m < 0) return;
    const b = E[m];
    switch (v.key) {
      case "ArrowDown": {
        v.preventDefault();
        const L = (m + 1) % E.length;
        D(E[L].id);
        break;
      }
      case "ArrowUp": {
        v.preventDefault();
        const L = (m - 1 + E.length) % E.length;
        D(E[L].id);
        break;
      }
      case "Home": {
        v.preventDefault(), D(E[0].id);
        break;
      }
      case "End": {
        v.preventDefault(), D(E[E.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        v.preventDefault(), b.type === "nav" ? h(b.id) : b.type === "command" ? f(b.id) : b.type === "group" && (l ? k === b.id ? p() : u(b.id) : d(b.id));
        break;
      }
      case "ArrowRight": {
        b.type === "group" && !l && ((c.get(b.id) ?? !1) || (v.preventDefault(), d(b.id)));
        break;
      }
      case "ArrowLeft": {
        b.type === "group" && !l && (c.get(b.id) ?? !1) && (v.preventDefault(), d(b.id));
        break;
      }
    }
  }, [
    r,
    l,
    c,
    g,
    k,
    D,
    h,
    f,
    d,
    u,
    p
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, l ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(x, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(x, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: A }, r.map((v) => /* @__PURE__ */ e.createElement(
    ce,
    {
      key: v.id,
      item: v,
      activeItemId: a,
      collapsed: l,
      onSelect: h,
      onExecute: f,
      onToggleGroup: d,
      focusedId: g,
      setItemRef: R,
      onItemFocus: w,
      groupStates: c,
      flyoutGroupId: k,
      onOpenFlyout: u,
      onCloseFlyout: p
    }
  ))), l ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(x, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(x, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
      title: l ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: l ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(x, { control: t.activeContent })));
}, rt = ({ controlId: o }) => {
  const t = S(), s = t.direction ?? "column", n = t.gap ?? "default", r = t.align ?? "stretch", a = t.wrap === !0, l = t.children ?? [], c = [
    "tlStack",
    `tlStack--${s}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${r}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: c }, l.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i })));
}, st = ({ controlId: o }) => {
  const t = S(), s = t.columns, n = t.minColumnWidth, r = t.gap ?? "default", a = t.children ?? [], l = {};
  return n ? l.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : s && (l.gridTemplateColumns = `repeat(${s}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: o, className: `tlGrid tlGrid--gap-${r}`, style: l }, a.map((c, i) => /* @__PURE__ */ e.createElement(x, { key: i, control: c })));
}, ct = ({ controlId: o }) => {
  const t = S(), s = t.title, n = t.variant ?? "outlined", r = t.padding ?? "default", a = t.headerActions ?? [], l = t.child, c = s != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: `tlCard tlCard--${n}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, s && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(x, { control: l })));
}, it = ({ controlId: o }) => {
  const t = S(), s = t.title ?? "", n = t.leading, r = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: o, className: c }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(x, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i }))));
}, { useCallback: dt } = e, ut = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.items ?? [], r = dt((a) => {
    s("navigate", { itemId: a });
  }, [s]);
  return /* @__PURE__ */ e.createElement("nav", { id: o, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((a, l) => {
    const c = l === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, l > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, a.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: mt } = e, pt = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.items ?? [], r = t.activeItemId, a = mt((l) => {
    l !== r && s("selectItem", { itemId: l });
  }, [s, r]);
  return /* @__PURE__ */ e.createElement("nav", { id: o, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((l) => {
    const c = l.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: l.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => a(l.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + l.icon, "aria-hidden": "true" }), l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, l.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, l.label)
    );
  }));
}, { useCallback: ne, useEffect: le, useRef: bt } = e, ht = {
  "js.dialog.close": "Close"
}, ft = ({ controlId: o }) => {
  const t = S(), s = P(), n = I(ht), r = t.open === !0, a = t.title ?? "", l = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], d = t.child, h = bt(null), f = ne(() => {
    s("close");
  }, [s]), _ = ne((C) => {
    c && C.target === C.currentTarget && f();
  }, [c, f]);
  if (le(() => {
    if (!r) return;
    const C = (u) => {
      u.key === "Escape" && f();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [r, f]), le(() => {
    r && h.current && h.current.focus();
  }, [r]), !r) return null;
  const k = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDialog__backdrop", onClick: _ }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${l}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": k,
      ref: h,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: k }, a), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: f,
        title: n["js.dialog.close"]
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
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(x, { control: d })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((C, u) => /* @__PURE__ */ e.createElement(x, { key: u, control: C })))
  ));
}, { useCallback: _t, useEffect: Et } = e, vt = {
  "js.drawer.close": "Close"
}, Ct = ({ controlId: o }) => {
  const t = S(), s = P(), n = I(vt), r = t.open === !0, a = t.position ?? "right", l = t.size ?? "medium", c = t.title ?? null, i = t.child, d = _t(() => {
    s("close");
  }, [s]);
  Et(() => {
    if (!r) return;
    const f = (_) => {
      _.key === "Escape" && d();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [r, d]);
  const h = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${l}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: o, className: h, "aria-hidden": !r }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: d,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(x, { control: i })));
}, { useCallback: oe, useEffect: yt, useState: gt } = e, kt = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.message ?? "", r = t.variant ?? "info", a = t.action, l = t.duration ?? 5e3, c = t.visible === !0, [i, d] = gt(!1), h = oe(() => {
    d(!0), setTimeout(() => {
      s("dismiss"), d(!1);
    }, 200);
  }, [s]), f = oe(() => {
    a && s(a.commandName), h();
  }, [s, a, h]);
  return yt(() => {
    if (!c || l === 0) return;
    const _ = setTimeout(h, l);
    return () => clearTimeout(_);
  }, [c, l, h]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlSnackbar tlSnackbar--${r}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: f }, a.label)
  );
}, { useCallback: X, useEffect: q, useRef: wt, useState: re } = e, Nt = ({ controlId: o }) => {
  const t = S(), s = P(), n = t.open === !0, r = t.anchorId, a = t.items ?? [], l = wt(null), [c, i] = re({ top: 0, left: 0 }), [d, h] = re(0), f = a.filter((u) => u.type === "item" && !u.disabled);
  q(() => {
    var w, T;
    if (!n || !r) return;
    const u = document.getElementById(r);
    if (!u) return;
    const p = u.getBoundingClientRect(), g = ((w = l.current) == null ? void 0 : w.offsetHeight) ?? 200, y = ((T = l.current) == null ? void 0 : T.offsetWidth) ?? 200;
    let B = p.bottom + 4, R = p.left;
    B + g > window.innerHeight && (B = p.top - g - 4), R + y > window.innerWidth && (R = p.right - y), i({ top: B, left: R }), h(0);
  }, [n, r]);
  const _ = X(() => {
    s("close");
  }, [s]), k = X((u) => {
    s("selectItem", { itemId: u });
  }, [s]);
  q(() => {
    if (!n) return;
    const u = (p) => {
      l.current && !l.current.contains(p.target) && _();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [n, _]);
  const C = X((u) => {
    if (u.key === "Escape") {
      _();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), h((p) => (p + 1) % f.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), h((p) => (p - 1 + f.length) % f.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const p = f[d];
      p && k(p.id);
    }
  }, [_, k, f, d]);
  return q(() => {
    n && l.current && l.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: "tlMenu",
      role: "menu",
      ref: l,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: C
    },
    a.map((u, p) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: p, className: "tlMenu__separator" });
      const y = f.indexOf(u) === d;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (y ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: y ? 0 : -1,
          onClick: () => k(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, Lt = ({ controlId: o }) => {
  const t = S(), s = t.header, n = t.content, r = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAppShell" }, s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(x, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(x, { control: n })), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(x, { control: r })), /* @__PURE__ */ e.createElement(x, { control: a }));
}, St = () => {
  const t = S().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, se = 50, xt = () => {
  const o = S(), t = P(), s = o.columns ?? [], n = o.totalRowCount ?? 0, r = o.rows ?? [], a = o.rowHeight ?? 36, l = o.selectionMode ?? "single", c = o.selectedCount ?? 0, i = l === "multi", d = 40, h = e.useRef(null), f = e.useRef(null), [_, k] = e.useState({}), C = e.useRef(null), u = (m) => _[m.name] ?? m.width, p = n * a, g = e.useCallback((m, b, L) => {
    L.preventDefault(), L.stopPropagation(), C.current = { column: m, startX: L.clientX, startWidth: b };
    const M = ($) => {
      const j = C.current;
      if (!j) return;
      const U = Math.max(se, j.startWidth + ($.clientX - j.startX));
      k((W) => ({ ...W, [j.column]: U }));
    }, z = ($) => {
      document.removeEventListener("mousemove", M), document.removeEventListener("mouseup", z);
      const j = C.current;
      if (j) {
        const U = Math.max(se, j.startWidth + ($.clientX - j.startX));
        t("columnResize", { column: j.column, width: U }), k((W) => {
          const te = { ...W };
          return delete te[j.column], te;
        }), C.current = null;
      }
    };
    document.addEventListener("mousemove", M), document.addEventListener("mouseup", z);
  }, [t]), y = e.useCallback(() => {
    f.current !== null && clearTimeout(f.current), f.current = window.setTimeout(() => {
      const m = h.current;
      if (!m) return;
      const b = m.scrollTop, L = Math.ceil(m.clientHeight / a), M = Math.floor(b / a);
      t("scroll", { start: M, count: L });
    }, 80);
  }, [t, a]), B = e.useCallback((m, b) => {
    let L;
    !b || b === "desc" ? L = "asc" : L = "desc", t("sort", { column: m, direction: L });
  }, [t]), R = e.useCallback((m, b) => {
    b.shiftKey && b.preventDefault(), t("select", {
      rowIndex: m,
      ctrlKey: b.ctrlKey || b.metaKey,
      shiftKey: b.shiftKey
    });
  }, [t]), w = e.useCallback((m, b) => {
    b.stopPropagation(), t("select", { rowIndex: m, ctrlKey: !0, shiftKey: !1 });
  }, [t]), T = e.useCallback(() => {
    const m = c === n && n > 0;
    t("selectAll", { selected: !m });
  }, [t, c, n]), D = s.reduce((m, b) => m + u(b), 0) + (i ? d : 0), A = c === n && n > 0, v = c > 0 && c < n, E = e.useCallback((m) => {
    m && (m.indeterminate = v);
  }, [v]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlTableView" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", style: { width: D } }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow" }, i && /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView__headerCell tlTableView__checkboxCell",
      style: { width: d, minWidth: d }
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        ref: E,
        className: "tlTableView__checkbox",
        checked: A,
        onChange: T
      }
    )
  ), s.map((m) => {
    const b = u(m);
    return /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m.name,
        className: "tlTableView__headerCell" + (m.sortable ? " tlTableView__headerCell--sortable" : ""),
        style: { width: b, minWidth: b, position: "relative" },
        onClick: m.sortable ? () => B(m.name, m.sortDirection) : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, m.label),
      m.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, m.sortDirection === "asc" ? "▲" : "▼"),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__resizeHandle",
          onMouseDown: (L) => g(m.name, b, L)
        }
      )
    );
  }))), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: h,
      className: "tlTableView__body",
      onScroll: y
    },
    /* @__PURE__ */ e.createElement("div", { style: { height: p, position: "relative" } }, r.map((m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m.id,
        className: "tlTableView__row" + (m.selected ? " tlTableView__row--selected" : ""),
        style: {
          position: "absolute",
          top: m.index * a,
          height: a,
          width: D
        },
        onClick: (b) => R(m.index, b)
      },
      i && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__cell tlTableView__checkboxCell",
          style: { width: d, minWidth: d },
          onClick: (b) => b.stopPropagation()
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            className: "tlTableView__checkbox",
            checked: m.selected,
            onChange: () => {
            },
            onClick: (b) => w(m.index, b),
            tabIndex: -1
          }
        )
      ),
      s.map((b) => {
        const L = u(b);
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: b.name,
            className: "tlTableView__cell",
            style: { width: L, minWidth: L }
          },
          /* @__PURE__ */ e.createElement(x, { control: m.cells[b.name] })
        );
      })
    )))
  ));
}, Tt = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ie = e.createContext(Tt), { useMemo: Rt, useRef: Dt, useState: Bt, useEffect: Pt } = e, jt = 320, Ft = ({ controlId: o }) => {
  const t = S(), s = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", r = t.readOnly === !0, a = t.children ?? [], l = Dt(null), [c, i] = Bt(
    n === "top" ? "top" : "side"
  );
  Pt(() => {
    if (n !== "auto") {
      i(n);
      return;
    }
    const k = l.current;
    if (!k) return;
    const C = new ResizeObserver((u) => {
      for (const p of u) {
        const y = p.contentRect.width / s;
        i(y < jt ? "top" : "side");
      }
    });
    return C.observe(k), () => C.disconnect();
  }, [n, s]);
  const d = Rt(() => ({
    readOnly: r,
    resolvedLabelPosition: c
  }), [r, c]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / s))}rem`}, 1fr))`
  }, _ = [
    "tlFormLayout",
    r ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ie.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: o, className: _, style: f, ref: l }, a.map((k, C) => /* @__PURE__ */ e.createElement(x, { key: C, control: k }))));
}, { useCallback: Mt } = e, It = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, $t = ({ controlId: o }) => {
  const t = S(), s = P(), n = I(It), r = t.header, a = t.headerActions ?? [], l = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", d = t.fullLine === !0, h = t.children ?? [], f = r != null || a.length > 0 || l, _ = Mt(() => {
    s("toggleCollapse");
  }, [s]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    d ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: k }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, l && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
      "aria-expanded": !c,
      title: c ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: c ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, r), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, a.map((C, u) => /* @__PURE__ */ e.createElement(x, { key: u, control: C })))), !c && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, h.map((C, u) => /* @__PURE__ */ e.createElement(x, { key: u, control: C }))));
}, { useContext: At, useState: zt, useCallback: Ut } = e, Wt = ({ controlId: o }) => {
  const t = S(), s = At(ie), n = t.label ?? "", r = t.required === !0, a = t.error, l = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? s.resolvedLabelPosition, d = t.fullLine === !0, h = t.visible !== !1, f = t.field, _ = s.readOnly, [k, C] = zt(!1), u = Ut(() => C((y) => !y), []);
  if (!h) return null;
  const p = a != null, g = [
    "tlFormField",
    `tlFormField--${i}`,
    _ ? "tlFormField--readonly" : "",
    d ? "tlFormField--fullLine" : "",
    p ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: g }, c && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__dirtyBar" }), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), r && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), l && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: u,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(x, { control: f })), !_ && p && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, a)), !_ && l && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, l));
};
N("TLButton", ke);
N("TLToggleButton", Ne);
N("TLTextInput", me);
N("TLNumberInput", be);
N("TLDatePicker", fe);
N("TLSelect", Ee);
N("TLCheckbox", Ce);
N("TLTable", ye);
N("TLCounter", Le);
N("TLTabBar", xe);
N("TLFieldList", Te);
N("TLAudioRecorder", De);
N("TLAudioPlayer", Pe);
N("TLFileUpload", Fe);
N("TLDownload", Ie);
N("TLPhotoCapture", Ae);
N("TLPhotoViewer", Ue);
N("TLSplitPanel", We);
N("TLPanel", Xe);
N("TLMaximizeRoot", qe);
N("TLDeckPane", Ze);
N("TLSidebar", ot);
N("TLStack", rt);
N("TLGrid", st);
N("TLCard", ct);
N("TLAppBar", it);
N("TLBreadcrumb", ut);
N("TLBottomBar", pt);
N("TLDialog", ft);
N("TLDrawer", Ct);
N("TLSnackbar", kt);
N("TLMenu", Nt);
N("TLAppShell", Lt);
N("TLTextCell", St);
N("TLTableView", xt);
N("TLFormLayout", Ft);
N("TLFormGroup", $t);
N("TLFormField", Wt);
