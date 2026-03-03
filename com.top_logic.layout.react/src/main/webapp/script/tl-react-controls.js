import { React as e, useTLFieldValue as F, getComponent as se, useTLState as T, useTLCommand as P, TLChild as x, useTLUpload as X, useI18N as A, useTLDataUrl as Z, register as S } from "tl-react-bridge";
const { useCallback: ce } = e, ie = ({ controlId: o, state: t }) => {
  const [s, n] = F(), r = ce(
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
}, { useCallback: de } = e, ue = ({ controlId: o, state: t, config: s }) => {
  const [n, r] = F(), a = de(
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
}, { useCallback: me } = e, pe = ({ controlId: o, state: t }) => {
  const [s, n] = F(), r = me(
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
}, { useCallback: be } = e, fe = ({ controlId: o, state: t, config: s }) => {
  var c;
  const [n, r] = F(), a = be(
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
}, { useCallback: he } = e, _e = ({ controlId: o, state: t }) => {
  const [s, n] = F(), r = he(
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
}, Ee = ({ controlId: o, state: t }) => {
  const s = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: o, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, s.map((r) => /* @__PURE__ */ e.createElement("th", { key: r.name }, r.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((r, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, s.map((l) => {
    const c = l.cellModule ? se(l.cellModule) : void 0, i = r[l.name];
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
}, { useCallback: ve } = e, Ce = ({ controlId: o, command: t, label: s, disabled: n }) => {
  const r = T(), a = P(), l = t ?? "click", c = s ?? r.label, i = n ?? r.disabled === !0, d = ve(() => {
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
}, { useCallback: ge } = e, ye = ({ controlId: o, command: t, label: s, active: n, disabled: r }) => {
  const a = T(), l = P(), c = t ?? "click", i = s ?? a.label, d = n ?? a.active === !0, b = r ?? a.disabled === !0, f = ge(() => {
    l(c);
  }, [l, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: o,
      onClick: f,
      disabled: b,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    i
  );
}, ke = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: we } = e, Ne = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.tabs ?? [], r = t.activeTabId, a = we((l) => {
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
}, Se = ({ controlId: o }) => {
  const t = T(), s = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((r, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(x, { control: r })))));
}, Le = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Te = ({ controlId: o }) => {
  const t = T(), s = X(), [n, r] = e.useState("idle"), [a, l] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), b = t.status ?? "idle", f = t.error, _ = b === "received" ? "idle" : n !== "idle" ? n : b, w = e.useCallback(async () => {
    if (n === "recording") {
      const v = c.current;
      v && v.state !== "inactive" && v.stop();
      return;
    }
    if (n !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const v = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = v, i.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(v, D ? { mimeType: D } : void 0);
        c.current = R, R.ondataavailable = (y) => {
          y.data.size > 0 && i.current.push(y.data);
        }, R.onstop = async () => {
          v.getTracks().forEach((g) => g.stop()), d.current = null;
          const y = new Blob(i.current, { type: R.mimeType || "audio/webm" });
          if (i.current = [], y.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const p = new FormData();
          p.append("audio", y, "recording.webm"), await s(p), r("idle");
        }, R.start(), r("recording");
      } catch (v) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", v), l("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [n, s]), k = A(Le), u = _ === "recording" ? k["js.audioRecorder.stop"] : _ === "uploading" ? k["js.uploading"] : k["js.audioRecorder.record"], m = _ === "uploading", C = ["tlAudioRecorder__button"];
  return _ === "recording" && C.push("tlAudioRecorder__button--recording"), _ === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: w,
      disabled: m,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, k[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, xe = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Re = ({ controlId: o }) => {
  const t = T(), s = Z(), n = !!t.hasAudio, r = t.dataRevision ?? 0, [a, l] = e.useState(n ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(r);
  e.useEffect(() => {
    n ? a === "disabled" && l("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), l("disabled"));
  }, [n]), e.useEffect(() => {
    r !== d.current && (d.current = r, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && l("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const b = e.useCallback(async () => {
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
        const m = await fetch(s);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), l("idle");
          return;
        }
        const C = await m.blob();
        i.current = URL.createObjectURL(C);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), l("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      l("idle");
    }, u.play(), l("playing");
  }, [a, s]), f = A(xe), _ = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], w = a === "disabled" || a === "loading", k = ["tlAudioPlayer__button"];
  return a === "playing" && k.push("tlAudioPlayer__button--playing"), a === "loading" && k.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: b,
      disabled: w,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, De = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Be = ({ controlId: o }) => {
  const t = T(), s = X(), [n, r] = e.useState("idle"), [a, l] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, b = t.accept ?? "", f = i === "received" ? "idle" : n !== "idle" ? n : i, _ = e.useCallback(async (y) => {
    r("uploading");
    const p = new FormData();
    p.append("file", y, y.name), await s(p), r("idle");
  }, [s]), w = e.useCallback((y) => {
    var g;
    const p = (g = y.target.files) == null ? void 0 : g[0];
    p && _(p);
  }, [_]), k = e.useCallback(() => {
    var y;
    n !== "uploading" && ((y = c.current) == null || y.click());
  }, [n]), u = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), l(!0);
  }, []), m = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), l(!1);
  }, []), C = e.useCallback((y) => {
    var g;
    if (y.preventDefault(), y.stopPropagation(), l(!1), n === "uploading") return;
    const p = (g = y.dataTransfer.files) == null ? void 0 : g[0];
    p && _(p);
  }, [n, _]), v = f === "uploading", D = A(De), R = f === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: m,
      onDrop: C
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: b || void 0,
        onChange: w,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: k,
        disabled: v,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
}, Pe = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, je = ({ controlId: o }) => {
  const t = T(), s = Z(), n = P(), r = !!t.hasData, a = t.dataRevision ?? 0, l = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), b = e.useCallback(async () => {
    if (!(!r || i)) {
      d(!0);
      try {
        const k = s + (s.includes("?") ? "&" : "?") + "rev=" + a, u = await fetch(k);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const m = await u.blob(), C = URL.createObjectURL(m), v = document.createElement("a");
        v.href = C, v.download = l, v.style.display = "none", document.body.appendChild(v), v.click(), document.body.removeChild(v), URL.revokeObjectURL(C);
      } catch (k) {
        console.error("[TLDownload] Fetch error:", k);
      } finally {
        d(!1);
      }
    }
  }, [r, i, s, a, l]), f = e.useCallback(async () => {
    r && await n("clear");
  }, [r, n]), _ = A(Pe);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const w = i ? _["js.downloading"] : _["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: i,
      title: w,
      "aria-label": w
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
}, Me = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ae = ({ controlId: o }) => {
  const t = T(), s = X(), [n, r] = e.useState("idle"), [a, l] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), b = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), w = e.useRef(null), k = t.error, u = e.useMemo(
    () => {
      var h;
      return !!(window.isSecureContext && ((h = navigator.mediaDevices) != null && h.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    b.current && (b.current.getTracks().forEach((h) => h.stop()), b.current = null), d.current && (d.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    m(), r("idle");
  }, [m]), v = e.useCallback(async () => {
    var h;
    if (n !== "uploading") {
      if (l(null), !u) {
        (h = _.current) == null || h.click();
        return;
      }
      try {
        const L = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        b.current = L, r("overlayOpen");
      } catch (L) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", L), l("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [n, u]), D = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const h = d.current, L = f.current;
    if (!h || !L)
      return;
    L.width = h.videoWidth, L.height = h.videoHeight;
    const N = L.getContext("2d");
    N && (N.drawImage(h, 0, 0), m(), r("uploading"), L.toBlob(async (B) => {
      if (!B) {
        r("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", B, "capture.jpg"), await s(I), r("idle");
    }, "image/jpeg", 0.85));
  }, [n, s, m]), R = e.useCallback(async (h) => {
    var B;
    const L = (B = h.target.files) == null ? void 0 : B[0];
    if (!L) return;
    r("uploading");
    const N = new FormData();
    N.append("photo", L, L.name), await s(N), r("idle"), _.current && (_.current.value = "");
  }, [s]);
  e.useEffect(() => {
    n === "overlayOpen" && d.current && b.current && (d.current.srcObject = b.current);
  }, [n]), e.useEffect(() => {
    var L;
    if (n !== "overlayOpen") return;
    (L = w.current) == null || L.focus();
    const h = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = h;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const h = (L) => {
      L.key === "Escape" && C();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [n, C]), e.useEffect(() => () => {
    b.current && (b.current.getTracks().forEach((h) => h.stop()), b.current = null);
  }, []);
  const y = A(Me), p = n === "uploading" ? y["js.uploading"] : y["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const j = ["tlPhotoCapture__overlayVideo"];
  c && j.push("tlPhotoCapture__overlayVideo--mirrored");
  const E = ["tlPhotoCapture__mirrorBtn"];
  return c && E.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: v,
      disabled: n === "uploading",
      title: p,
      "aria-label": p
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
      ref: w,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: d,
        className: j.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: E.join(" "),
        onClick: () => i((h) => !h),
        title: y["js.photoCapture.mirror"],
        "aria-label": y["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: y["js.photoCapture.capture"],
        "aria-label": y["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: y["js.photoCapture.close"],
        "aria-label": y["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, y[a]), k && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, k));
}, Ie = {
  "js.photoViewer.alt": "Captured photo"
}, $e = ({ controlId: o }) => {
  const t = T(), s = Z(), n = !!t.hasPhoto, r = t.dataRevision ?? 0, [a, l] = e.useState(null), c = e.useRef(r);
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
        const b = await fetch(s);
        if (!b.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", b.status);
          return;
        }
        const f = await b.blob();
        d || l(URL.createObjectURL(f));
      } catch (b) {
        console.error("[TLPhotoViewer] Fetch error:", b);
      }
    })(), () => {
      d = !0;
    };
  }, [n, r, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = A(Ie);
  return !n || !a ? /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: te, useRef: O } = e, ze = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.orientation, r = t.resizable === !0, a = t.children ?? [], l = n === "horizontal", c = a.length > 0 && a.every((m) => m.collapsed), i = !c && a.some((m) => m.collapsed), d = c ? !l : l, b = O(null), f = O(null), _ = O(null), w = te((m, C) => {
    const v = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !d ? v.flex = "1 0 0%" : v.flex = "0 0 auto" : C !== void 0 ? v.flex = `0 0 ${C}px` : m.unit === "%" || i ? v.flex = `${m.size} 0 0%` : v.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (v.minWidth = l ? m.minSize : void 0, v.minHeight = l ? void 0 : m.minSize), v;
  }, [l, c, i, d]), k = te((m, C) => {
    m.preventDefault();
    const v = b.current;
    if (!v) return;
    const D = a[C], R = a[C + 1], y = v.querySelectorAll(":scope > .tlSplitPanel__child"), p = [];
    y.forEach((E) => {
      p.push(l ? E.offsetWidth : E.offsetHeight);
    }), _.current = p, f.current = {
      splitterIndex: C,
      startPos: l ? m.clientX : m.clientY,
      startSizeBefore: p[C],
      startSizeAfter: p[C + 1],
      childBefore: D,
      childAfter: R
    };
    const g = (E) => {
      const h = f.current;
      if (!h || !_.current) return;
      const N = (l ? E.clientX : E.clientY) - h.startPos, B = h.childBefore.minSize || 0, I = h.childAfter.minSize || 0;
      let $ = h.startSizeBefore + N, z = h.startSizeAfter - N;
      $ < B && (z += $ - B, $ = B), z < I && ($ += z - I, z = I), _.current[h.splitterIndex] = $, _.current[h.splitterIndex + 1] = z;
      const J = v.querySelectorAll(":scope > .tlSplitPanel__child"), Q = J[h.splitterIndex], ee = J[h.splitterIndex + 1];
      Q && (Q.style.flex = `0 0 ${$}px`), ee && (ee.style.flex = `0 0 ${z}px`);
    }, j = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", j), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const E = {};
        a.forEach((h, L) => {
          const N = h.control;
          N != null && N.controlId && _.current && (E[N.controlId] = _.current[L]);
        }), s("updateSizes", { sizes: E });
      }
      _.current = null, f.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", j), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, l, s]), u = [];
  return a.forEach((m, C) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${m.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: w(m)
        },
        /* @__PURE__ */ e.createElement(x, { control: m.control })
      )
    ), r && C < a.length - 1) {
      const v = a[C + 1];
      !m.collapsed && !v.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (R) => k(R, C)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: b,
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
}, { useCallback: W } = e, Ue = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Fe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Ve = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Oe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), We = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ke = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), He = ({ controlId: o }) => {
  const t = T(), s = P(), n = A(Ue), r = t.title, a = t.expansionState ?? "NORMALIZED", l = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], b = a === "MINIMIZED", f = a === "MAXIMIZED", _ = a === "HIDDEN", w = W(() => {
    s("toggleMinimize");
  }, [s]), k = W(() => {
    s("toggleMaximize");
  }, [s]), u = W(() => {
    s("popOut");
  }, [s]);
  if (_)
    return null;
  const m = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((C, v) => /* @__PURE__ */ e.createElement("span", { key: v, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(x, { control: C }))), l && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: b ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Ve, null) : /* @__PURE__ */ e.createElement(Fe, null)
    ), c && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(We, null) : /* @__PURE__ */ e.createElement(Oe, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ke, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(x, { control: t.child }))
  );
}, Ye = ({ controlId: o }) => {
  const t = T();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(x, { control: t.child })
  );
}, Ge = ({ controlId: o }) => {
  const t = T();
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(x, { control: t.activeChild }));
}, { useCallback: M, useState: K, useEffect: V, useRef: G } = e, qe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function q(o, t, s, n) {
  const r = [];
  for (const a of o)
    a.type === "nav" ? r.push({ id: a.id, type: "nav", groupId: n }) : a.type === "command" ? r.push({ id: a.id, type: "command", groupId: n }) : a.type === "group" && (r.push({ id: a.id, type: "group" }), (s.get(a.id) ?? a.expanded) && !t && r.push(...q(a.children, t, s, a.id)));
  return r;
}
const U = ({ icon: o }) => o ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + o, "aria-hidden": "true" }) : null, Xe = ({ item: o, active: t, collapsed: s, onSelect: n, tabIndex: r, itemRef: a, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(o.id),
    title: s ? o.label : void 0,
    tabIndex: r,
    ref: a,
    onFocus: () => l(o.id)
  },
  s && o.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(U, { icon: o.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, o.badge)) : /* @__PURE__ */ e.createElement(U, { icon: o.icon }),
  !s && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
  !s && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
), Ze = ({ item: o, collapsed: t, onExecute: s, tabIndex: n, itemRef: r, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => s(o.id),
    title: t ? o.label : void 0,
    tabIndex: n,
    ref: r,
    onFocus: () => a(o.id)
  },
  /* @__PURE__ */ e.createElement(U, { icon: o.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)
), Je = ({ item: o, collapsed: t }) => t && !o.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? o.label : void 0 }, /* @__PURE__ */ e.createElement(U, { icon: o.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)), Qe = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), et = ({ item: o, activeItemId: t, onSelect: s, onExecute: n, onClose: r }) => {
  const a = G(null);
  V(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [r]), V(() => {
    const c = (i) => {
      i.key === "Escape" && r();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [r]);
  const l = M((c) => {
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
        /* @__PURE__ */ e.createElement(U, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, tt = ({
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
  focusedId: b,
  setItemRef: f,
  onItemFocus: _,
  flyoutGroupId: w,
  onOpenFlyout: k,
  onCloseFlyout: u
}) => {
  const m = M(() => {
    n ? w === o.id ? u() : k(o.id) : l(o.id);
  }, [n, w, o.id, l, k, u]), C = n && w === o.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (C ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: m,
      title: n ? o.label : void 0,
      "aria-expanded": n ? C : t,
      tabIndex: c,
      ref: i,
      onFocus: () => d(o.id)
    },
    /* @__PURE__ */ e.createElement(U, { icon: o.icon }),
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
  ), C && /* @__PURE__ */ e.createElement(
    et,
    {
      item: o,
      activeItemId: s,
      onSelect: r,
      onExecute: a,
      onClose: u
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, o.children.map((v) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: v.id,
      item: v,
      activeItemId: s,
      collapsed: n,
      onSelect: r,
      onExecute: a,
      onToggleGroup: l,
      focusedId: b,
      setItemRef: f,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: w,
      onOpenFlyout: k,
      onCloseFlyout: u
    }
  ))));
}, re = ({
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
  flyoutGroupId: b,
  onOpenFlyout: f,
  onCloseFlyout: _
}) => {
  switch (o.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Xe,
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
        Ze,
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
      return /* @__PURE__ */ e.createElement(Je, { item: o, collapsed: s });
    case "separator":
      return /* @__PURE__ */ e.createElement(Qe, null);
    case "group": {
      const w = d ? d.get(o.id) ?? o.expanded : o.expanded;
      return /* @__PURE__ */ e.createElement(
        tt,
        {
          item: o,
          expanded: w,
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
          flyoutGroupId: b,
          onOpenFlyout: f,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, at = ({ controlId: o }) => {
  const t = T(), s = P(), n = A(qe), r = t.items ?? [], a = t.activeItemId, l = t.collapsed, [c, i] = K(() => {
    const E = /* @__PURE__ */ new Map(), h = (L) => {
      for (const N of L)
        N.type === "group" && (E.set(N.id, N.expanded), h(N.children));
    };
    return h(r), E;
  }), d = M((E) => {
    i((h) => {
      const L = new Map(h), N = L.get(E) ?? !1;
      return L.set(E, !N), s("toggleGroup", { itemId: E, expanded: !N }), L;
    });
  }, [s]), b = M((E) => {
    E !== a && s("selectItem", { itemId: E });
  }, [s, a]), f = M((E) => {
    s("executeCommand", { itemId: E });
  }, [s]), _ = M(() => {
    s("toggleCollapse", {});
  }, [s]), [w, k] = K(null), u = M((E) => {
    k(E);
  }, []), m = M(() => {
    k(null);
  }, []);
  V(() => {
    l || k(null);
  }, [l]);
  const [C, v] = K(() => {
    const E = q(r, l, c);
    return E.length > 0 ? E[0].id : "";
  }), D = G(/* @__PURE__ */ new Map()), R = M((E) => (h) => {
    h ? D.current.set(E, h) : D.current.delete(E);
  }, []), y = M((E) => {
    v(E);
  }, []), p = G(0), g = M((E) => {
    v(E), p.current++;
  }, []);
  V(() => {
    const E = D.current.get(C);
    E && document.activeElement !== E && E.focus();
  }, [C, p.current]);
  const j = M((E) => {
    if (E.key === "Escape" && w !== null) {
      E.preventDefault(), m();
      return;
    }
    const h = q(r, l, c);
    if (h.length === 0) return;
    const L = h.findIndex((B) => B.id === C);
    if (L < 0) return;
    const N = h[L];
    switch (E.key) {
      case "ArrowDown": {
        E.preventDefault();
        const B = (L + 1) % h.length;
        g(h[B].id);
        break;
      }
      case "ArrowUp": {
        E.preventDefault();
        const B = (L - 1 + h.length) % h.length;
        g(h[B].id);
        break;
      }
      case "Home": {
        E.preventDefault(), g(h[0].id);
        break;
      }
      case "End": {
        E.preventDefault(), g(h[h.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        E.preventDefault(), N.type === "nav" ? b(N.id) : N.type === "command" ? f(N.id) : N.type === "group" && (l ? w === N.id ? m() : u(N.id) : d(N.id));
        break;
      }
      case "ArrowRight": {
        N.type === "group" && !l && ((c.get(N.id) ?? !1) || (E.preventDefault(), d(N.id)));
        break;
      }
      case "ArrowLeft": {
        N.type === "group" && !l && (c.get(N.id) ?? !1) && (E.preventDefault(), d(N.id));
        break;
      }
    }
  }, [
    r,
    l,
    c,
    C,
    w,
    g,
    b,
    f,
    d,
    u,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, l ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(x, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(x, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: j }, r.map((E) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: E.id,
      item: E,
      activeItemId: a,
      collapsed: l,
      onSelect: b,
      onExecute: f,
      onToggleGroup: d,
      focusedId: C,
      setItemRef: R,
      onItemFocus: y,
      groupStates: c,
      flyoutGroupId: w,
      onOpenFlyout: u,
      onCloseFlyout: m
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
}, nt = ({ controlId: o }) => {
  const t = T(), s = t.direction ?? "column", n = t.gap ?? "default", r = t.align ?? "stretch", a = t.wrap === !0, l = t.children ?? [], c = [
    "tlStack",
    `tlStack--${s}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${r}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: c }, l.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i })));
}, lt = ({ controlId: o }) => {
  const t = T(), s = t.columns, n = t.minColumnWidth, r = t.gap ?? "default", a = t.children ?? [], l = {};
  return n ? l.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : s && (l.gridTemplateColumns = `repeat(${s}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: o, className: `tlGrid tlGrid--gap-${r}`, style: l }, a.map((c, i) => /* @__PURE__ */ e.createElement(x, { key: i, control: c })));
}, ot = ({ controlId: o }) => {
  const t = T(), s = t.title, n = t.variant ?? "outlined", r = t.padding ?? "default", a = t.headerActions ?? [], l = t.child, c = s != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: `tlCard tlCard--${n}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, s && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(x, { control: l })));
}, rt = ({ controlId: o }) => {
  const t = T(), s = t.title ?? "", n = t.leading, r = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: o, className: c }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(x, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((i, d) => /* @__PURE__ */ e.createElement(x, { key: d, control: i }))));
}, { useCallback: st } = e, ct = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.items ?? [], r = st((a) => {
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
}, { useCallback: it } = e, dt = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.items ?? [], r = t.activeItemId, a = it((l) => {
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
}, { useCallback: ae, useEffect: ne, useRef: ut } = e, mt = {
  "js.dialog.close": "Close"
}, pt = ({ controlId: o }) => {
  const t = T(), s = P(), n = A(mt), r = t.open === !0, a = t.title ?? "", l = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], d = t.child, b = ut(null), f = ae(() => {
    s("close");
  }, [s]), _ = ae((k) => {
    c && k.target === k.currentTarget && f();
  }, [c, f]);
  if (ne(() => {
    if (!r) return;
    const k = (u) => {
      u.key === "Escape" && f();
    };
    return document.addEventListener("keydown", k), () => document.removeEventListener("keydown", k);
  }, [r, f]), ne(() => {
    r && b.current && b.current.focus();
  }, [r]), !r) return null;
  const w = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDialog__backdrop", onClick: _ }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${l}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": w,
      ref: b,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: w }, a), /* @__PURE__ */ e.createElement(
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
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((k, u) => /* @__PURE__ */ e.createElement(x, { key: u, control: k })))
  ));
}, { useCallback: bt, useEffect: ft } = e, ht = {
  "js.drawer.close": "Close"
}, _t = ({ controlId: o }) => {
  const t = T(), s = P(), n = A(ht), r = t.open === !0, a = t.position ?? "right", l = t.size ?? "medium", c = t.title ?? null, i = t.child, d = bt(() => {
    s("close");
  }, [s]);
  ft(() => {
    if (!r) return;
    const f = (_) => {
      _.key === "Escape" && d();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [r, d]);
  const b = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${l}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: o, className: b, "aria-hidden": !r }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
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
}, { useCallback: le, useEffect: Et, useState: vt } = e, Ct = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.message ?? "", r = t.variant ?? "info", a = t.action, l = t.duration ?? 5e3, c = t.visible === !0, [i, d] = vt(!1), b = le(() => {
    d(!0), setTimeout(() => {
      s("dismiss"), d(!1);
    }, 200);
  }, [s]), f = le(() => {
    a && s(a.commandName), b();
  }, [s, a, b]);
  return Et(() => {
    if (!c || l === 0) return;
    const _ = setTimeout(b, l);
    return () => clearTimeout(_);
  }, [c, l, b]), !c && !i ? null : /* @__PURE__ */ e.createElement(
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
}, { useCallback: H, useEffect: Y, useRef: gt, useState: oe } = e, yt = ({ controlId: o }) => {
  const t = T(), s = P(), n = t.open === !0, r = t.anchorId, a = t.items ?? [], l = gt(null), [c, i] = oe({ top: 0, left: 0 }), [d, b] = oe(0), f = a.filter((u) => u.type === "item" && !u.disabled);
  Y(() => {
    var y, p;
    if (!n || !r) return;
    const u = document.getElementById(r);
    if (!u) return;
    const m = u.getBoundingClientRect(), C = ((y = l.current) == null ? void 0 : y.offsetHeight) ?? 200, v = ((p = l.current) == null ? void 0 : p.offsetWidth) ?? 200;
    let D = m.bottom + 4, R = m.left;
    D + C > window.innerHeight && (D = m.top - C - 4), R + v > window.innerWidth && (R = m.right - v), i({ top: D, left: R }), b(0);
  }, [n, r]);
  const _ = H(() => {
    s("close");
  }, [s]), w = H((u) => {
    s("selectItem", { itemId: u });
  }, [s]);
  Y(() => {
    if (!n) return;
    const u = (m) => {
      l.current && !l.current.contains(m.target) && _();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [n, _]);
  const k = H((u) => {
    if (u.key === "Escape") {
      _();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), b((m) => (m + 1) % f.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), b((m) => (m - 1 + f.length) % f.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const m = f[d];
      m && w(m.id);
    }
  }, [_, w, f, d]);
  return Y(() => {
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
      onKeyDown: k
    },
    a.map((u, m) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: m, className: "tlMenu__separator" });
      const v = f.indexOf(u) === d;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (v ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: v ? 0 : -1,
          onClick: () => w(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, kt = ({ controlId: o }) => {
  const t = T(), s = t.header, n = t.content, r = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAppShell" }, s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(x, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(x, { control: n })), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(x, { control: r })), /* @__PURE__ */ e.createElement(x, { control: a }));
}, wt = () => {
  const t = T().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Nt = () => {
  const o = T(), t = P(), s = o.columns ?? [], n = o.totalRowCount ?? 0, r = o.rows ?? [], a = o.rowHeight ?? 36, l = o.selectionMode ?? "single", c = o.selectedCount ?? 0, i = l === "multi", d = 40, b = e.useRef(null), f = e.useRef(null), _ = n * a, w = e.useCallback(() => {
    f.current !== null && clearTimeout(f.current), f.current = window.setTimeout(() => {
      const p = b.current;
      if (!p) return;
      const g = p.scrollTop, j = Math.ceil(p.clientHeight / a), E = Math.floor(g / a);
      t("scroll", { start: E, count: j });
    }, 80);
  }, [t, a]), k = e.useCallback((p, g) => {
    let j;
    !g || g === "desc" ? j = "asc" : j = "desc", t("sort", { column: p, direction: j });
  }, [t]), u = e.useCallback((p, g) => {
    g.shiftKey && g.preventDefault(), t("select", {
      rowIndex: p,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), m = e.useCallback((p, g) => {
    g.stopPropagation(), t("select", { rowIndex: p, ctrlKey: !0, shiftKey: !1 });
  }, [t]), C = e.useCallback(() => {
    const p = c === n && n > 0;
    t("selectAll", { selected: !p });
  }, [t, c, n]), v = s.reduce((p, g) => p + g.width, 0) + (i ? d : 0), D = c === n && n > 0, R = c > 0 && c < n, y = e.useCallback((p) => {
    p && (p.indeterminate = R);
  }, [R]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlTableView" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", style: { width: v } }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow" }, i && /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView__headerCell tlTableView__checkboxCell",
      style: { width: d, minWidth: d }
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        ref: y,
        className: "tlTableView__checkbox",
        checked: D,
        onChange: C
      }
    )
  ), s.map((p) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: p.name,
      className: "tlTableView__headerCell" + (p.sortable ? " tlTableView__headerCell--sortable" : ""),
      style: { width: p.width, minWidth: p.width },
      onClick: p.sortable ? () => k(p.name, p.sortDirection) : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, p.label),
    p.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, p.sortDirection === "asc" ? "▲" : "▼")
  )))), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: b,
      className: "tlTableView__body",
      onScroll: w
    },
    /* @__PURE__ */ e.createElement("div", { style: { height: _, position: "relative" } }, r.map((p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p.id,
        className: "tlTableView__row" + (p.selected ? " tlTableView__row--selected" : ""),
        style: {
          position: "absolute",
          top: p.index * a,
          height: a,
          width: v
        },
        onClick: (g) => u(p.index, g)
      },
      i && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__cell tlTableView__checkboxCell",
          style: { width: d, minWidth: d },
          onClick: (g) => g.stopPropagation()
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            className: "tlTableView__checkbox",
            checked: p.selected,
            onChange: () => {
            },
            onClick: (g) => m(p.index, g),
            tabIndex: -1
          }
        )
      ),
      s.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.name,
          className: "tlTableView__cell",
          style: { width: g.width, minWidth: g.width }
        },
        /* @__PURE__ */ e.createElement(x, { control: p.cells[g.name] })
      ))
    )))
  ));
};
S("TLButton", Ce);
S("TLToggleButton", ye);
S("TLTextInput", ie);
S("TLNumberInput", ue);
S("TLDatePicker", pe);
S("TLSelect", fe);
S("TLCheckbox", _e);
S("TLTable", Ee);
S("TLCounter", ke);
S("TLTabBar", Ne);
S("TLFieldList", Se);
S("TLAudioRecorder", Te);
S("TLAudioPlayer", Re);
S("TLFileUpload", Be);
S("TLDownload", je);
S("TLPhotoCapture", Ae);
S("TLPhotoViewer", $e);
S("TLSplitPanel", ze);
S("TLPanel", He);
S("TLMaximizeRoot", Ye);
S("TLDeckPane", Ge);
S("TLSidebar", at);
S("TLStack", nt);
S("TLGrid", lt);
S("TLCard", ot);
S("TLAppBar", rt);
S("TLBreadcrumb", ct);
S("TLBottomBar", dt);
S("TLDialog", pt);
S("TLDrawer", _t);
S("TLSnackbar", Ct);
S("TLMenu", yt);
S("TLAppShell", kt);
S("TLTextCell", wt);
S("TLTableView", Nt);
