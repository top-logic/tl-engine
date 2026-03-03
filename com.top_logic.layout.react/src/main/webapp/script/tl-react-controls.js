import { React as e, useTLFieldValue as U, getComponent as re, useTLState as w, useTLCommand as B, TLChild as R, useTLUpload as q, useI18N as j, useTLDataUrl as X, register as k } from "tl-react-bridge";
const { useCallback: se } = e, ce = ({ state: t }) => {
  const [o, l] = U(), r = se(
    (n) => {
      l(n.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: ie } = e, de = ({ state: t, config: o }) => {
  const [l, r] = U(), n = ie(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      r(i);
    },
    [r]
  ), a = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: n,
      step: a,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ue } = e, me = ({ state: t }) => {
  const [o, l] = U(), r = ue(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: pe } = e, be = ({ state: t, config: o }) => {
  var c;
  const [l, r] = U(), n = pe(
    (s) => {
      r(s.target.value || null);
    },
    [r]
  ), a = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const s = ((c = a.find((i) => i.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: fe } = e, he = ({ state: t }) => {
  const [o, l] = U(), r = fe(
    (n) => {
      l(n.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: o === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: o === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Ee = ({ controlId: t, state: o }) => {
  const l = o.columns ?? [], r = o.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((n, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, l.map((c) => {
    const s = c.cellModule ? re(c.cellModule) : void 0, i = n[c.name];
    if (s) {
      const m = { value: i, editable: o.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: t + "-" + a + "-" + c.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: ve } = e, _e = ({ command: t, label: o, disabled: l }) => {
  const r = w(), n = B(), a = t ?? "click", c = o ?? r.label, s = l ?? r.disabled === !0, i = ve(() => {
    n(a);
  }, [n, a]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: s,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: ge } = e, ye = ({ command: t, label: o, active: l, disabled: r }) => {
  const n = w(), a = B(), c = t ?? "click", s = o ?? n.label, i = l ?? n.active === !0, m = r ?? n.disabled === !0, b = ge(() => {
    a(c);
  }, [a, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: m,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    s
  );
}, Ce = () => {
  const t = w(), o = B(), l = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ke } = e, Ne = () => {
  const t = w(), o = B(), l = t.tabs ?? [], r = t.activeTabId, n = ke((a) => {
    a !== r && o("selectTab", { tabId: a });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === r,
      className: "tlReactTabBar__tab" + (a.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => n(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, we = () => {
  const t = w(), o = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((r, n) => /* @__PURE__ */ e.createElement("div", { key: n, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: r })))));
}, Se = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Le = () => {
  const t = w(), o = q(), [l, r] = e.useState("idle"), [n, a] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), m = t.status ?? "idle", b = t.error, f = m === "received" ? "idle" : l !== "idle" ? l : m, N = e.useCallback(async () => {
    if (l === "recording") {
      const E = c.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (l !== "uploading") {
      if (a(null), !window.isSecureContext || !navigator.mediaDevices) {
        a("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = E, s.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(E, L ? { mimeType: L } : void 0);
        c.current = x, x.ondataavailable = (g) => {
          g.data.size > 0 && s.current.push(g.data);
        }, x.onstop = async () => {
          E.getTracks().forEach((T) => T.stop()), i.current = null;
          const g = new Blob(s.current, { type: x.mimeType || "audio/webm" });
          if (s.current = [], g.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const S = new FormData();
          S.append("audio", g, "recording.webm"), await o(S), r("idle");
        }, x.start(), r("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), a("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [l, o]), _ = j(Se), u = f === "recording" ? _["js.audioRecorder.stop"] : f === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], d = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: N,
      disabled: d,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[n]), b && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b));
}, Re = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, xe = () => {
  const t = w(), o = X(), l = !!t.hasAudio, r = t.dataRevision ?? 0, [n, a] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    l ? n === "disabled" && a("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), a("disabled"));
  }, [l]), e.useEffect(() => {
    r !== i.current && (i.current = r, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (n === "playing" || n === "paused" || n === "loading") && a("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (n === "disabled" || n === "loading")
      return;
    if (n === "playing") {
      c.current && c.current.pause(), a("paused");
      return;
    }
    if (n === "paused" && c.current) {
      c.current.play(), a("playing");
      return;
    }
    if (!s.current) {
      a("loading");
      try {
        const d = await fetch(o);
        if (!d.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", d.status), a("idle");
          return;
        }
        const v = await d.blob();
        s.current = URL.createObjectURL(v);
      } catch (d) {
        console.error("[TLAudioPlayer] Fetch error:", d), a("idle");
        return;
      }
    }
    const u = new Audio(s.current);
    c.current = u, u.onended = () => {
      a("idle");
    }, u.play(), a("playing");
  }, [n, o]), b = j(Re), f = n === "loading" ? b["js.loading"] : n === "playing" ? b["js.audioPlayer.pause"] : n === "disabled" ? b["js.audioPlayer.noAudio"] : b["js.audioPlayer.play"], N = n === "disabled" || n === "loading", _ = ["tlAudioPlayer__button"];
  return n === "playing" && _.push("tlAudioPlayer__button--playing"), n === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: m,
      disabled: N,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${n === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Te = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, De = () => {
  const t = w(), o = q(), [l, r] = e.useState("idle"), [n, a] = e.useState(!1), c = e.useRef(null), s = t.status ?? "idle", i = t.error, m = t.accept ?? "", b = s === "received" ? "idle" : l !== "idle" ? l : s, f = e.useCallback(async (g) => {
    r("uploading");
    const S = new FormData();
    S.append("file", g, g.name), await o(S), r("idle");
  }, [o]), N = e.useCallback((g) => {
    var T;
    const S = (T = g.target.files) == null ? void 0 : T[0];
    S && f(S);
  }, [f]), _ = e.useCallback(() => {
    var g;
    l !== "uploading" && ((g = c.current) == null || g.click());
  }, [l]), u = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), a(!0);
  }, []), d = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), a(!1);
  }, []), v = e.useCallback((g) => {
    var T;
    if (g.preventDefault(), g.stopPropagation(), a(!1), l === "uploading") return;
    const S = (T = g.dataTransfer.files) == null ? void 0 : T[0];
    S && f(S);
  }, [l, f]), E = b === "uploading", L = j(Te), x = b === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${n ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: d,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: m || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (b === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: E,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Be = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Pe = () => {
  const t = w(), o = X(), l = B(), r = !!t.hasData, n = t.dataRevision ?? 0, a = t.fileName ?? "download", c = !!t.clearable, [s, i] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!r || s)) {
      i(!0);
      try {
        const _ = o + (o.includes("?") ? "&" : "?") + "rev=" + n, u = await fetch(_);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const d = await u.blob(), v = URL.createObjectURL(d), E = document.createElement("a");
        E.href = v, E.download = a, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(v);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        i(!1);
      }
    }
  }, [r, s, o, n, a]), b = e.useCallback(async () => {
    r && await l("clear");
  }, [r, l]), f = j(Be);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const N = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: b,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, je = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ie = () => {
  const t = w(), o = q(), [l, r] = e.useState("idle"), [n, a] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), m = e.useRef(null), b = e.useRef(null), f = e.useRef(null), N = e.useRef(null), _ = t.error, u = e.useMemo(
    () => {
      var p;
      return !!(window.isSecureContext && ((p = navigator.mediaDevices) != null && p.getUserMedia));
    },
    []
  ), d = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((p) => p.stop()), m.current = null), i.current && (i.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    d(), r("idle");
  }, [d]), E = e.useCallback(async () => {
    var p;
    if (l !== "uploading") {
      if (a(null), !u) {
        (p = f.current) == null || p.click();
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = C, r("overlayOpen");
      } catch (C) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", C), a("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [l, u]), L = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const p = i.current, C = b.current;
    if (!p || !C)
      return;
    C.width = p.videoWidth, C.height = p.videoHeight;
    const y = C.getContext("2d");
    y && (y.drawImage(p, 0, 0), d(), r("uploading"), C.toBlob(async (D) => {
      if (!D) {
        r("idle");
        return;
      }
      const M = new FormData();
      M.append("photo", D, "capture.jpg"), await o(M), r("idle");
    }, "image/jpeg", 0.85));
  }, [l, o, d]), x = e.useCallback(async (p) => {
    var D;
    const C = (D = p.target.files) == null ? void 0 : D[0];
    if (!C) return;
    r("uploading");
    const y = new FormData();
    y.append("photo", C, C.name), await o(y), r("idle"), f.current && (f.current.value = "");
  }, [o]);
  e.useEffect(() => {
    l === "overlayOpen" && i.current && m.current && (i.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var C;
    if (l !== "overlayOpen") return;
    (C = N.current) == null || C.focus();
    const p = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = p;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const p = (C) => {
      C.key === "Escape" && v();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [l, v]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((p) => p.stop()), m.current = null);
  }, []);
  const g = j(je), S = l === "uploading" ? g["js.uploading"] : g["js.photoCapture.open"], T = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && T.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  c && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const h = ["tlPhotoCapture__mirrorBtn"];
  return c && h.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: E,
      disabled: l === "uploading",
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !u && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: b, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: I.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: h.join(" "),
        onClick: () => s((p) => !p),
        title: g["js.photoCapture.mirror"],
        "aria-label": g["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: L,
        title: g["js.photoCapture.capture"],
        "aria-label": g["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: g["js.photoCapture.close"],
        "aria-label": g["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g[n]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, Me = {
  "js.photoViewer.alt": "Captured photo"
}, $e = () => {
  const t = w(), o = X(), l = !!t.hasPhoto, r = t.dataRevision ?? 0, [n, a] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!l) {
      n && (URL.revokeObjectURL(n), a(null));
      return;
    }
    if (r === c.current && n)
      return;
    c.current = r, n && (URL.revokeObjectURL(n), a(null));
    let i = !1;
    return (async () => {
      try {
        const m = await fetch(o);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const b = await m.blob();
        i || a(URL.createObjectURL(b));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      i = !0;
    };
  }, [l, r, o]), e.useEffect(() => () => {
    n && URL.revokeObjectURL(n);
  }, []);
  const s = j(Me);
  return !l || !n ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: n,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ee, useRef: O } = e, Ae = () => {
  const t = w(), o = B(), l = t.orientation, r = t.resizable === !0, n = t.children ?? [], a = l === "horizontal", c = n.length > 0 && n.every((d) => d.collapsed), s = !c && n.some((d) => d.collapsed), i = c ? !a : a, m = O(null), b = O(null), f = O(null), N = ee((d, v) => {
    const E = {
      overflow: d.scrolling || "auto"
    };
    return d.collapsed ? c && !i ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : v !== void 0 ? E.flex = `0 0 ${v}px` : d.unit === "%" || s ? E.flex = `${d.size} 0 0%` : E.flex = `0 0 ${d.size}px`, d.minSize > 0 && !d.collapsed && (E.minWidth = a ? d.minSize : void 0, E.minHeight = a ? void 0 : d.minSize), E;
  }, [a, c, s, i]), _ = ee((d, v) => {
    d.preventDefault();
    const E = m.current;
    if (!E) return;
    const L = n[v], x = n[v + 1], g = E.querySelectorAll(":scope > .tlSplitPanel__child"), S = [];
    g.forEach((h) => {
      S.push(a ? h.offsetWidth : h.offsetHeight);
    }), f.current = S, b.current = {
      splitterIndex: v,
      startPos: a ? d.clientX : d.clientY,
      startSizeBefore: S[v],
      startSizeAfter: S[v + 1],
      childBefore: L,
      childAfter: x
    };
    const T = (h) => {
      const p = b.current;
      if (!p || !f.current) return;
      const y = (a ? h.clientX : h.clientY) - p.startPos, D = p.childBefore.minSize || 0, M = p.childAfter.minSize || 0;
      let $ = p.startSizeBefore + y, A = p.startSizeAfter - y;
      $ < D && (A += $ - D, $ = D), A < M && ($ += A - M, A = M), f.current[p.splitterIndex] = $, f.current[p.splitterIndex + 1] = A;
      const Z = E.querySelectorAll(":scope > .tlSplitPanel__child"), J = Z[p.splitterIndex], Q = Z[p.splitterIndex + 1];
      J && (J.style.flex = `0 0 ${$}px`), Q && (Q.style.flex = `0 0 ${A}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", T), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const h = {};
        n.forEach((p, C) => {
          const y = p.control;
          y != null && y.controlId && f.current && (h[y.controlId] = f.current[C]);
        }), o("updateSizes", { sizes: h });
      }
      f.current = null, b.current = null;
    };
    document.addEventListener("mousemove", T), document.addEventListener("mouseup", I), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [n, a, o]), u = [];
  return n.forEach((d, v) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${d.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(d)
        },
        /* @__PURE__ */ e.createElement(R, { control: d.control })
      )
    ), r && v < n.length - 1) {
      const E = n[v + 1];
      !d.collapsed && !E.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (x) => _(x, v)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      className: `tlSplitPanel tlSplitPanel--${l}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    u
  );
}, { useCallback: W } = e, ze = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ue = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Fe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Oe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), We = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ve = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), He = () => {
  const t = w(), o = B(), l = j(ze), r = t.title, n = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, c = t.showMaximize === !0, s = t.showPopOut === !0, i = t.toolbarButtons ?? [], m = n === "MINIMIZED", b = n === "MAXIMIZED", f = n === "HIDDEN", N = W(() => {
    o("toggleMinimize");
  }, [o]), _ = W(() => {
    o("toggleMaximize");
  }, [o]), u = W(() => {
    o("popOut");
  }, [o]);
  if (f)
    return null;
  const d = b ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${n.toLowerCase()}`,
      style: d
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((v, E) => /* @__PURE__ */ e.createElement("span", { key: E, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(R, { control: v }))), a && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: m ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Fe, null) : /* @__PURE__ */ e.createElement(Ue, null)
    ), c && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: b ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      b ? /* @__PURE__ */ e.createElement(We, null) : /* @__PURE__ */ e.createElement(Oe, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ve, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child }))
  );
}, Ke = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, Ye = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: P, useState: V, useEffect: F, useRef: Y } = e, Ge = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function G(t, o, l, r) {
  const n = [];
  for (const a of t)
    a.type === "nav" ? n.push({ id: a.id, type: "nav", groupId: r }) : a.type === "command" ? n.push({ id: a.id, type: "command", groupId: r }) : a.type === "group" && (n.push({ id: a.id, type: "group" }), (l.get(a.id) ?? a.expanded) && !o && n.push(...G(a.children, o, l, a.id)));
  return n;
}
const z = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, qe = ({ item: t, active: o, collapsed: l, onSelect: r, tabIndex: n, itemRef: a, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (o ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(t.id),
    title: l ? t.label : void 0,
    tabIndex: n,
    ref: a,
    onFocus: () => c(t.id)
  },
  l && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(z, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !l && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), Xe = ({ item: t, collapsed: o, onExecute: l, tabIndex: r, itemRef: n, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(t.id),
    title: o ? t.label : void 0,
    tabIndex: r,
    ref: n,
    onFocus: () => a(t.id)
  },
  /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Ze = ({ item: t, collapsed: o }) => o && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: o ? t.label : void 0 }, /* @__PURE__ */ e.createElement(z, { icon: t.icon }), !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), Je = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Qe = ({ item: t, activeItemId: o, onSelect: l, onExecute: r, onClose: n }) => {
  const a = Y(null);
  F(() => {
    const s = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => n(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [n]), F(() => {
    const s = (i) => {
      i.key === "Escape" && n();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
  }, [n]);
  const c = P((s) => {
    s.type === "nav" ? (l(s.id), n()) : s.type === "command" && (r(s.id), n());
  }, [l, r, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, t.label), t.children.map((s) => {
    if (s.type === "nav" || s.type === "command") {
      const i = s.type === "nav" && s.id === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => c(s)
        },
        /* @__PURE__ */ e.createElement(z, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, et = ({
  item: t,
  expanded: o,
  activeItemId: l,
  collapsed: r,
  onSelect: n,
  onExecute: a,
  onToggleGroup: c,
  tabIndex: s,
  itemRef: i,
  onFocus: m,
  focusedId: b,
  setItemRef: f,
  onItemFocus: N,
  flyoutGroupId: _,
  onOpenFlyout: u,
  onCloseFlyout: d
}) => {
  const v = P(() => {
    r ? _ === t.id ? d() : u(t.id) : c(t.id);
  }, [r, _, t.id, c, u, d]), E = r && _ === t.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: v,
      title: r ? t.label : void 0,
      "aria-expanded": r ? E : o,
      tabIndex: s,
      ref: i,
      onFocus: () => m(t.id)
    },
    /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
    !r && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlSidebar__chevron" + (o ? " tlSidebar__chevron--open" : ""),
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
    Qe,
    {
      item: t,
      activeItemId: l,
      onSelect: n,
      onExecute: a,
      onClose: d
    }
  ), o && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((L) => /* @__PURE__ */ e.createElement(
    oe,
    {
      key: L.id,
      item: L,
      activeItemId: l,
      collapsed: r,
      onSelect: n,
      onExecute: a,
      onToggleGroup: c,
      focusedId: b,
      setItemRef: f,
      onItemFocus: N,
      groupStates: null,
      flyoutGroupId: _,
      onOpenFlyout: u,
      onCloseFlyout: d
    }
  ))));
}, oe = ({
  item: t,
  activeItemId: o,
  collapsed: l,
  onSelect: r,
  onExecute: n,
  onToggleGroup: a,
  focusedId: c,
  setItemRef: s,
  onItemFocus: i,
  groupStates: m,
  flyoutGroupId: b,
  onOpenFlyout: f,
  onCloseFlyout: N
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        qe,
        {
          item: t,
          active: t.id === o,
          collapsed: l,
          onSelect: r,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Xe,
        {
          item: t,
          collapsed: l,
          onExecute: n,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ze, { item: t, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(Je, null);
    case "group": {
      const _ = m ? m.get(t.id) ?? t.expanded : t.expanded;
      return /* @__PURE__ */ e.createElement(
        et,
        {
          item: t,
          expanded: _,
          activeItemId: o,
          collapsed: l,
          onSelect: r,
          onExecute: n,
          onToggleGroup: a,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i,
          focusedId: c,
          setItemRef: s,
          onItemFocus: i,
          flyoutGroupId: b,
          onOpenFlyout: f,
          onCloseFlyout: N
        }
      );
    }
    default:
      return null;
  }
}, tt = () => {
  const t = w(), o = B(), l = j(Ge), r = t.items ?? [], n = t.activeItemId, a = t.collapsed, [c, s] = V(() => {
    const h = /* @__PURE__ */ new Map(), p = (C) => {
      for (const y of C)
        y.type === "group" && (h.set(y.id, y.expanded), p(y.children));
    };
    return p(r), h;
  }), i = P((h) => {
    s((p) => {
      const C = new Map(p), y = C.get(h) ?? !1;
      return C.set(h, !y), o("toggleGroup", { itemId: h, expanded: !y }), C;
    });
  }, [o]), m = P((h) => {
    h !== n && o("selectItem", { itemId: h });
  }, [o, n]), b = P((h) => {
    o("executeCommand", { itemId: h });
  }, [o]), f = P(() => {
    o("toggleCollapse", {});
  }, [o]), [N, _] = V(null), u = P((h) => {
    _(h);
  }, []), d = P(() => {
    _(null);
  }, []);
  F(() => {
    a || _(null);
  }, [a]);
  const [v, E] = V(() => {
    const h = G(r, a, c);
    return h.length > 0 ? h[0].id : "";
  }), L = Y(/* @__PURE__ */ new Map()), x = P((h) => (p) => {
    p ? L.current.set(h, p) : L.current.delete(h);
  }, []), g = P((h) => {
    E(h);
  }, []), S = Y(0), T = P((h) => {
    E(h), S.current++;
  }, []);
  F(() => {
    const h = L.current.get(v);
    h && document.activeElement !== h && h.focus();
  }, [v, S.current]);
  const I = P((h) => {
    if (h.key === "Escape" && N !== null) {
      h.preventDefault(), d();
      return;
    }
    const p = G(r, a, c);
    if (p.length === 0) return;
    const C = p.findIndex((D) => D.id === v);
    if (C < 0) return;
    const y = p[C];
    switch (h.key) {
      case "ArrowDown": {
        h.preventDefault();
        const D = (C + 1) % p.length;
        T(p[D].id);
        break;
      }
      case "ArrowUp": {
        h.preventDefault();
        const D = (C - 1 + p.length) % p.length;
        T(p[D].id);
        break;
      }
      case "Home": {
        h.preventDefault(), T(p[0].id);
        break;
      }
      case "End": {
        h.preventDefault(), T(p[p.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        h.preventDefault(), y.type === "nav" ? m(y.id) : y.type === "command" ? b(y.id) : y.type === "group" && (a ? N === y.id ? d() : u(y.id) : i(y.id));
        break;
      }
      case "ArrowRight": {
        y.type === "group" && !a && ((c.get(y.id) ?? !1) || (h.preventDefault(), i(y.id)));
        break;
      }
      case "ArrowLeft": {
        y.type === "group" && !a && (c.get(y.id) ?? !1) && (h.preventDefault(), i(y.id));
        break;
      }
    }
  }, [
    r,
    a,
    c,
    v,
    N,
    T,
    m,
    b,
    i,
    u,
    d
  ]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: I }, r.map((h) => /* @__PURE__ */ e.createElement(
    oe,
    {
      key: h.id,
      item: h,
      activeItemId: n,
      collapsed: a,
      onSelect: m,
      onExecute: b,
      onToggleGroup: i,
      focusedId: v,
      setItemRef: x,
      onItemFocus: g,
      groupStates: c,
      flyoutGroupId: N,
      onOpenFlyout: u,
      onCloseFlyout: d
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: a ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, at = () => {
  const t = w(), o = t.direction ?? "column", l = t.gap ?? "default", r = t.align ?? "stretch", n = t.wrap === !0, a = t.children ?? [], c = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${r}`,
    n ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { className: c }, a.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s })));
}, nt = () => {
  const t = w(), o = t.columns, l = t.minColumnWidth, r = t.gap ?? "default", n = t.children ?? [], a = {};
  return l ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : o && (a.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { className: `tlGrid tlGrid--gap-${r}`, style: a }, n.map((c, s) => /* @__PURE__ */ e.createElement(R, { key: s, control: c })));
}, lt = () => {
  const t = w(), o = t.title, l = t.variant ?? "outlined", r = t.padding ?? "default", n = t.headerActions ?? [], a = t.child, c = o != null || n.length > 0;
  return /* @__PURE__ */ e.createElement("div", { className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, n.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(R, { control: a })));
}, ot = () => {
  const t = w(), o = t.title ?? "", l = t.leading, r = t.actions ?? [], n = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    n === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(R, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s }))));
}, { useCallback: rt } = e, st = () => {
  const t = w(), o = B(), l = t.items ?? [], r = rt((n) => {
    o("navigate", { itemId: n });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((n, a) => {
    const c = a === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: n.id, className: "tlBreadcrumb__entry" }, a > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, n.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(n.id)
      },
      n.label
    ));
  })));
}, { useCallback: ct } = e, it = () => {
  const t = w(), o = B(), l = t.items ?? [], r = t.activeItemId, n = ct((a) => {
    a !== r && o("selectItem", { itemId: a });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((a) => {
    const c = a.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: a.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => n(a.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + a.icon, "aria-hidden": "true" }), a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, a.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, a.label)
    );
  }));
}, { useCallback: te, useEffect: ae, useRef: dt } = e, ut = {
  "js.dialog.close": "Close"
}, mt = () => {
  const t = w(), o = B(), l = j(ut), r = t.open === !0, n = t.title ?? "", a = t.size ?? "medium", c = t.closeOnBackdrop !== !1, s = t.actions ?? [], i = t.child, m = dt(null), b = te(() => {
    o("close");
  }, [o]), f = te((_) => {
    c && _.target === _.currentTarget && b();
  }, [c, b]);
  if (ae(() => {
    if (!r) return;
    const _ = (u) => {
      u.key === "Escape" && b();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [r, b]), ae(() => {
    r && m.current && m.current.focus();
  }, [r]), !r) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { className: "tlDialog__backdrop", onClick: f }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${a}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: m,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, n), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: b,
        title: l["js.dialog.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(R, { control: i })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((_, u) => /* @__PURE__ */ e.createElement(R, { key: u, control: _ })))
  ));
}, { useCallback: pt, useEffect: bt } = e, ft = {
  "js.drawer.close": "Close"
}, ht = () => {
  const t = w(), o = B(), l = j(ft), r = t.open === !0, n = t.position ?? "right", a = t.size ?? "medium", c = t.title ?? null, s = t.child, i = pt(() => {
    o("close");
  }, [o]);
  bt(() => {
    if (!r) return;
    const b = (f) => {
      f.key === "Escape" && i();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [r, i]);
  const m = [
    "tlDrawer",
    `tlDrawer--${n}`,
    `tlDrawer--${a}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { className: m, "aria-hidden": !r }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: i,
      title: l["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(R, { control: s })));
}, { useCallback: ne, useEffect: Et, useState: vt } = e, _t = () => {
  const t = w(), o = B(), l = t.message ?? "", r = t.variant ?? "info", n = t.action, a = t.duration ?? 5e3, c = t.visible === !0, [s, i] = vt(!1), m = ne(() => {
    i(!0), setTimeout(() => {
      o("dismiss"), i(!1);
    }, 200);
  }, [o]), b = ne(() => {
    n && o(n.commandName), m();
  }, [o, n, m]);
  return Et(() => {
    if (!c || a === 0) return;
    const f = setTimeout(m, a);
    return () => clearTimeout(f);
  }, [c, a, m]), !c && !s ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlSnackbar tlSnackbar--${r}${s ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: b }, n.label)
  );
}, { useCallback: H, useEffect: K, useRef: gt, useState: le } = e, yt = () => {
  const t = w(), o = B(), l = t.open === !0, r = t.anchorId, n = t.items ?? [], a = gt(null), [c, s] = le({ top: 0, left: 0 }), [i, m] = le(0), b = n.filter((u) => u.type === "item" && !u.disabled);
  K(() => {
    var g, S;
    if (!l || !r) return;
    const u = document.getElementById(r);
    if (!u) return;
    const d = u.getBoundingClientRect(), v = ((g = a.current) == null ? void 0 : g.offsetHeight) ?? 200, E = ((S = a.current) == null ? void 0 : S.offsetWidth) ?? 200;
    let L = d.bottom + 4, x = d.left;
    L + v > window.innerHeight && (L = d.top - v - 4), x + E > window.innerWidth && (x = d.right - E), s({ top: L, left: x }), m(0);
  }, [l, r]);
  const f = H(() => {
    o("close");
  }, [o]), N = H((u) => {
    o("selectItem", { itemId: u });
  }, [o]);
  K(() => {
    if (!l) return;
    const u = (d) => {
      a.current && !a.current.contains(d.target) && f();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [l, f]);
  const _ = H((u) => {
    if (u.key === "Escape") {
      f();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), m((d) => (d + 1) % b.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), m((d) => (d - 1 + b.length) % b.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const d = b[i];
      d && N(d.id);
    }
  }, [f, N, b, i]);
  return K(() => {
    l && a.current && a.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlMenu",
      role: "menu",
      ref: a,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: _
    },
    n.map((u, d) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: d, className: "tlMenu__separator" });
      const E = b.indexOf(u) === i;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (E ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: E ? 0 : -1,
          onClick: () => N(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
};
k("TLButton", _e);
k("TLToggleButton", ye);
k("TLTextInput", ce);
k("TLNumberInput", de);
k("TLDatePicker", me);
k("TLSelect", be);
k("TLCheckbox", he);
k("TLTable", Ee);
k("TLCounter", Ce);
k("TLTabBar", Ne);
k("TLFieldList", we);
k("TLAudioRecorder", Le);
k("TLAudioPlayer", xe);
k("TLFileUpload", De);
k("TLDownload", Pe);
k("TLPhotoCapture", Ie);
k("TLPhotoViewer", $e);
k("TLSplitPanel", Ae);
k("TLPanel", He);
k("TLMaximizeRoot", Ke);
k("TLDeckPane", Ye);
k("TLSidebar", tt);
k("TLStack", at);
k("TLGrid", nt);
k("TLCard", lt);
k("TLAppBar", ot);
k("TLBreadcrumb", st);
k("TLBottomBar", it);
k("TLDialog", mt);
k("TLDrawer", ht);
k("TLSnackbar", _t);
k("TLMenu", yt);
