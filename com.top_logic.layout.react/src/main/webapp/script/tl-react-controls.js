import { React as e, useTLFieldValue as I, getComponent as W, useTLState as w, useTLCommand as S, TLChild as x, useTLUpload as O, useI18N as D, useTLDataUrl as $, register as _ } from "tl-react-bridge";
const { useCallback: q } = e, X = ({ state: l }) => {
  const [r, a] = I(), o = q(
    (t) => {
      a(t.target.value);
    },
    [a]
  );
  return l.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: o,
      disabled: l.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Z } = e, G = ({ state: l, config: r }) => {
  const [a, o] = I(), t = Z(
    (s) => {
      const c = s.target.value, u = c === "" ? null : Number(c);
      o(u);
    },
    [o]
  ), n = r != null && r.decimal ? "0.01" : "1";
  return l.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: t,
      step: n,
      disabled: l.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: J } = e, Q = ({ state: l }) => {
  const [r, a] = I(), o = J(
    (t) => {
      a(t.target.value || null);
    },
    [a]
  );
  return l.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: o,
      disabled: l.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: ee } = e, te = ({ state: l, config: r }) => {
  var s;
  const [a, o] = I(), t = ee(
    (c) => {
      o(c.target.value || null);
    },
    [o]
  ), n = l.options ?? (r == null ? void 0 : r.options) ?? [];
  if (l.editable === !1) {
    const c = ((s = n.find((u) => u.value === a)) == null ? void 0 : s.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, c);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: t,
      disabled: l.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    n.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: ae } = e, le = ({ state: l }) => {
  const [r, a] = I(), o = ae(
    (t) => {
      a(t.target.checked);
    },
    [a]
  );
  return l.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: r === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: r === !0,
      onChange: o,
      disabled: l.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, ne = ({ controlId: l, state: r }) => {
  const a = r.columns ?? [], o = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((t, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, a.map((s) => {
    const c = s.cellModule ? W(s.cellModule) : void 0, u = t[s.name];
    if (c) {
      const m = { value: u, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: l + "-" + n + "-" + s.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, u != null ? String(u) : "");
  })))));
}, { useCallback: oe } = e, re = ({ command: l, label: r, disabled: a }) => {
  const o = w(), t = S(), n = l ?? "click", s = r ?? o.label, c = a ?? o.disabled === !0, u = oe(() => {
    t(n);
  }, [t, n]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: u,
      disabled: c,
      className: "tlReactButton"
    },
    s
  );
}, { useCallback: se } = e, ce = ({ command: l, label: r, active: a, disabled: o }) => {
  const t = w(), n = S(), s = l ?? "click", c = r ?? t.label, u = a ?? t.active === !0, m = o ?? t.disabled === !0, v = se(() => {
    n(s);
  }, [n, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: m,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    c
  );
}, ie = () => {
  const l = w(), r = S(), a = l.count ?? 0, o = l.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ue } = e, de = () => {
  const l = w(), r = S(), a = l.tabs ?? [], o = l.activeTabId, t = ue((n) => {
    n !== o && r("selectTab", { tabId: n });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((n) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: n.id,
      role: "tab",
      "aria-selected": n.id === o,
      className: "tlReactTabBar__tab" + (n.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => t(n.id)
    },
    n.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, l.activeContent && /* @__PURE__ */ e.createElement(x, { control: l.activeContent })));
}, me = () => {
  const l = w(), r = l.title, a = l.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(x, { control: o })))));
}, pe = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, he = () => {
  const l = w(), r = O(), [a, o] = e.useState("idle"), [t, n] = e.useState(null), s = e.useRef(null), c = e.useRef([]), u = e.useRef(null), m = l.status ?? "idle", v = l.error, p = m === "received" ? "idle" : a !== "idle" ? a : m, k = e.useCallback(async () => {
    if (a === "recording") {
      const d = s.current;
      d && d.state !== "inactive" && d.stop();
      return;
    }
    if (a !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const d = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        u.current = d, c.current = [];
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(d, T ? { mimeType: T } : void 0);
        s.current = L, L.ondataavailable = (b) => {
          b.data.size > 0 && c.current.push(b.data);
        }, L.onstop = async () => {
          d.getTracks().forEach((R) => R.stop()), u.current = null;
          const b = new Blob(c.current, { type: L.mimeType || "audio/webm" });
          if (c.current = [], b.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const g = new FormData();
          g.append("audio", b, "recording.webm"), await r(g), o("idle");
        }, L.start(), o("recording");
      } catch (d) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", d), n("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, r]), E = D(pe), C = p === "recording" ? E["js.audioRecorder.stop"] : p === "uploading" ? E["js.uploading"] : E["js.audioRecorder.record"], i = p === "uploading", f = ["tlAudioRecorder__button"];
  return p === "recording" && f.push("tlAudioRecorder__button--recording"), p === "uploading" && f.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: k,
      disabled: i,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), t && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E[t]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, be = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, fe = () => {
  const l = w(), r = $(), a = !!l.hasAudio, o = l.dataRevision ?? 0, [t, n] = e.useState(a ? "idle" : "disabled"), s = e.useRef(null), c = e.useRef(null), u = e.useRef(o);
  e.useEffect(() => {
    a ? t === "disabled" && n("idle") : (s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), n("disabled"));
  }, [a]), e.useEffect(() => {
    o !== u.current && (u.current = o, s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (t === "playing" || t === "paused" || t === "loading") && n("idle"));
  }, [o]), e.useEffect(() => () => {
    s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      s.current && s.current.pause(), n("paused");
      return;
    }
    if (t === "paused" && s.current) {
      s.current.play(), n("playing");
      return;
    }
    if (!c.current) {
      n("loading");
      try {
        const i = await fetch(r);
        if (!i.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", i.status), n("idle");
          return;
        }
        const f = await i.blob();
        c.current = URL.createObjectURL(f);
      } catch (i) {
        console.error("[TLAudioPlayer] Fetch error:", i), n("idle");
        return;
      }
    }
    const C = new Audio(c.current);
    s.current = C, C.onended = () => {
      n("idle");
    }, C.play(), n("playing");
  }, [t, r]), v = D(be), p = t === "loading" ? v["js.loading"] : t === "playing" ? v["js.audioPlayer.pause"] : t === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], k = t === "disabled" || t === "loading", E = ["tlAudioPlayer__button"];
  return t === "playing" && E.push("tlAudioPlayer__button--playing"), t === "loading" && E.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: m,
      disabled: k,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ve = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ce = () => {
  const l = w(), r = O(), [a, o] = e.useState("idle"), [t, n] = e.useState(!1), s = e.useRef(null), c = l.status ?? "idle", u = l.error, m = l.accept ?? "", v = c === "received" ? "idle" : a !== "idle" ? a : c, p = e.useCallback(async (b) => {
    o("uploading");
    const g = new FormData();
    g.append("file", b, b.name), await r(g), o("idle");
  }, [r]), k = e.useCallback((b) => {
    var R;
    const g = (R = b.target.files) == null ? void 0 : R[0];
    g && p(g);
  }, [p]), E = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = s.current) == null || b.click());
  }, [a]), C = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), n(!0);
  }, []), i = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), n(!1);
  }, []), f = e.useCallback((b) => {
    var R;
    if (b.preventDefault(), b.stopPropagation(), n(!1), a === "uploading") return;
    const g = (R = b.dataTransfer.files) == null ? void 0 : R[0];
    g && p(g);
  }, [a, p]), d = v === "uploading", T = D(ve), L = v === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: i,
      onDrop: f
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: s,
        type: "file",
        accept: m || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (v === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: E,
        disabled: d,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
}, Ee = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ye = () => {
  const l = w(), r = $(), a = S(), o = !!l.hasData, t = l.dataRevision ?? 0, n = l.fileName ?? "download", s = !!l.clearable, [c, u] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!o || c)) {
      u(!0);
      try {
        const E = r + (r.includes("?") ? "&" : "?") + "rev=" + t, C = await fetch(E);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const i = await C.blob(), f = URL.createObjectURL(i), d = document.createElement("a");
        d.href = f, d.download = n, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(f);
      } catch (E) {
        console.error("[TLDownload] Fetch error:", E);
      } finally {
        u(!1);
      }
    }
  }, [o, c, r, t, n]), v = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = D(Ee);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const k = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: c,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, _e = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ge = () => {
  const l = w(), r = O(), [a, o] = e.useState("idle"), [t, n] = e.useState(null), [s, c] = e.useState(!1), u = e.useRef(null), m = e.useRef(null), v = e.useRef(null), p = e.useRef(null), k = e.useRef(null), E = l.error, C = e.useMemo(
    () => {
      var h;
      return !!(window.isSecureContext && ((h = navigator.mediaDevices) != null && h.getUserMedia));
    },
    []
  ), i = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((h) => h.stop()), m.current = null), u.current && (u.current.srcObject = null);
  }, []), f = e.useCallback(() => {
    i(), o("idle");
  }, [i]), d = e.useCallback(async () => {
    var h;
    if (a !== "uploading") {
      if (n(null), !C) {
        (h = p.current) == null || h.click();
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = y, o("overlayOpen");
      } catch (y) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", y), n("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, C]), T = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const h = u.current, y = v.current;
    if (!h || !y)
      return;
    y.width = h.videoWidth, y.height = h.videoHeight;
    const N = y.getContext("2d");
    N && (N.drawImage(h, 0, 0), i(), o("uploading"), y.toBlob(async (j) => {
      if (!j) {
        o("idle");
        return;
      }
      const M = new FormData();
      M.append("photo", j, "capture.jpg"), await r(M), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, r, i]), L = e.useCallback(async (h) => {
    var j;
    const y = (j = h.target.files) == null ? void 0 : j[0];
    if (!y) return;
    o("uploading");
    const N = new FormData();
    N.append("photo", y, y.name), await r(N), o("idle"), p.current && (p.current.value = "");
  }, [r]);
  e.useEffect(() => {
    a === "overlayOpen" && u.current && m.current && (u.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var y;
    if (a !== "overlayOpen") return;
    (y = k.current) == null || y.focus();
    const h = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = h;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const h = (y) => {
      y.key === "Escape" && f();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [a, f]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((h) => h.stop()), m.current = null);
  }, []);
  const b = D(_e), g = a === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], R = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && R.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  s && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return s && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: d,
      disabled: a === "uploading",
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: f }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: u,
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
        onClick: () => c((h) => !h),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: f,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), t && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[t]), E && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E));
}, ke = {
  "js.photoViewer.alt": "Captured photo"
}, we = () => {
  const l = w(), r = $(), a = !!l.hasPhoto, o = l.dataRevision ?? 0, [t, n] = e.useState(null), s = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      t && (URL.revokeObjectURL(t), n(null));
      return;
    }
    if (o === s.current && t)
      return;
    s.current = o, t && (URL.revokeObjectURL(t), n(null));
    let u = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const v = await m.blob();
        u || n(URL.createObjectURL(v));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      u = !0;
    };
  }, [a, o, r]), e.useEffect(() => () => {
    t && URL.revokeObjectURL(t);
  }, []);
  const c = D(ke);
  return !a || !t ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: t,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: K, useRef: A } = e, Le = () => {
  const l = w(), r = S(), a = l.orientation, o = l.resizable === !0, t = l.children ?? [], n = a === "horizontal", s = t.length > 0 && t.every((i) => i.collapsed), c = !s && t.some((i) => i.collapsed), u = s ? !n : n, m = A(null), v = A(null), p = A(null), k = K((i, f) => {
    const d = {
      overflow: i.scrolling || "auto"
    };
    return i.collapsed ? s && !u ? d.flex = "1 0 0%" : d.flex = "0 0 auto" : f !== void 0 ? d.flex = `0 0 ${f}px` : i.unit === "%" || c ? d.flex = `${i.size} 0 0%` : d.flex = `0 0 ${i.size}px`, i.minSize > 0 && !i.collapsed && (d.minWidth = n ? i.minSize : void 0, d.minHeight = n ? void 0 : i.minSize), d;
  }, [n, s, c, u]), E = K((i, f) => {
    i.preventDefault();
    const d = m.current;
    if (!d) return;
    const T = t[f], L = t[f + 1], b = d.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    b.forEach((P) => {
      g.push(n ? P.offsetWidth : P.offsetHeight);
    }), p.current = g, v.current = {
      splitterIndex: f,
      startPos: n ? i.clientX : i.clientY,
      startSizeBefore: g[f],
      startSizeAfter: g[f + 1],
      childBefore: T,
      childAfter: L
    };
    const R = (P) => {
      const h = v.current;
      if (!h || !p.current) return;
      const N = (n ? P.clientX : P.clientY) - h.startPos, j = h.childBefore.minSize || 0, M = h.childAfter.minSize || 0;
      let B = h.startSizeBefore + N, z = h.startSizeAfter - N;
      B < j && (z += B - j, B = j), z < M && (B += z - M, z = M), p.current[h.splitterIndex] = B, p.current[h.splitterIndex + 1] = z;
      const V = d.querySelectorAll(":scope > .tlSplitPanel__child"), H = V[h.splitterIndex], Y = V[h.splitterIndex + 1];
      H && (H.style.flex = `0 0 ${B}px`), Y && (Y.style.flex = `0 0 ${z}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const P = {};
        t.forEach((h, y) => {
          const N = h.control;
          N != null && N.controlId && p.current && (P[N.controlId] = p.current[y]);
        }), r("updateSizes", { sizes: P });
      }
      p.current = null, v.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", U), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [t, n, r]), C = [];
  return t.forEach((i, f) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${f}`,
          className: `tlSplitPanel__child${i.collapsed && u ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(i)
        },
        /* @__PURE__ */ e.createElement(x, { control: i.control })
      )
    ), o && f < t.length - 1) {
      const d = t[f + 1];
      !i.collapsed && !d.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${f}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (L) => E(L, f)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      className: `tlSplitPanel tlSplitPanel--${a}${s ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: u ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    C
  );
}, { useCallback: F } = e, Re = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ne = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Pe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Te = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Se = () => {
  const l = w(), r = S(), a = D(Re), o = l.title, t = l.expansionState ?? "NORMALIZED", n = l.showMinimize === !0, s = l.showMaximize === !0, c = l.showPopOut === !0, u = l.toolbarButtons ?? [], m = t === "MINIMIZED", v = t === "MAXIMIZED", p = t === "HIDDEN", k = F(() => {
    r("toggleMinimize");
  }, [r]), E = F(() => {
    r("toggleMaximize");
  }, [r]), C = F(() => {
    r("popOut");
  }, [r]);
  if (p)
    return null;
  const i = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${t.toLowerCase()}`,
      style: i
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, u.map((f, d) => /* @__PURE__ */ e.createElement("span", { key: d, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(x, { control: f }))), n && !v && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: m ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Pe, null) : /* @__PURE__ */ e.createElement(Ne, null)
    ), s && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: v ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      v ? /* @__PURE__ */ e.createElement(je, null) : /* @__PURE__ */ e.createElement(Te, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(xe, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(x, { control: l.child }))
  );
}, De = () => {
  const l = w();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${l.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(x, { control: l.child })
  );
}, Me = () => {
  const l = w();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, l.activeChild && /* @__PURE__ */ e.createElement(x, { control: l.activeChild }));
};
_("TLButton", re);
_("TLToggleButton", ce);
_("TLTextInput", X);
_("TLNumberInput", G);
_("TLDatePicker", Q);
_("TLSelect", te);
_("TLCheckbox", le);
_("TLTable", ne);
_("TLCounter", ie);
_("TLTabBar", de);
_("TLFieldList", me);
_("TLAudioRecorder", he);
_("TLAudioPlayer", fe);
_("TLFileUpload", Ce);
_("TLDownload", ye);
_("TLPhotoCapture", ge);
_("TLPhotoViewer", we);
_("TLSplitPanel", Le);
_("TLPanel", Se);
_("TLMaximizeRoot", De);
_("TLDeckPane", Me);
