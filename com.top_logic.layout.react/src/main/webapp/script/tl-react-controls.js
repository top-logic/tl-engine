import { React as e, useTLFieldValue as F, getComponent as ee, useTLState as L, useTLCommand as D, TLChild as P, useTLUpload as Y, useI18N as M, useTLDataUrl as G, register as S } from "tl-react-bridge";
const { useCallback: te } = e, ae = ({ state: t }) => {
  const [o, l] = F(), r = te(
    (a) => {
      l(a.target.value);
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
}, { useCallback: ne } = e, le = ({ state: t, config: o }) => {
  const [l, r] = F(), a = ne(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      r(i);
    },
    [r]
  ), n = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: a,
      step: n,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: oe } = e, re = ({ state: t }) => {
  const [o, l] = F(), r = oe(
    (a) => {
      l(a.target.value || null);
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
}, { useCallback: se } = e, ce = ({ state: t, config: o }) => {
  var c;
  const [l, r] = F(), a = se(
    (s) => {
      r(s.target.value || null);
    },
    [r]
  ), n = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const s = ((c = n.find((i) => i.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    n.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: ie } = e, de = ({ state: t }) => {
  const [o, l] = F(), r = ie(
    (a) => {
      l(a.target.checked);
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
}, ue = ({ controlId: t, state: o }) => {
  const l = o.columns ?? [], r = o.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((a, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, l.map((c) => {
    const s = c.cellModule ? ee(c.cellModule) : void 0, i = a[c.name];
    if (s) {
      const p = { value: i, editable: o.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: t + "-" + n + "-" + c.name,
          state: p
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: me } = e, pe = ({ command: t, label: o, disabled: l }) => {
  const r = L(), a = D(), n = t ?? "click", c = o ?? r.label, s = l ?? r.disabled === !0, i = me(() => {
    a(n);
  }, [a, n]);
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
}, { useCallback: be } = e, fe = ({ command: t, label: o, active: l, disabled: r }) => {
  const a = L(), n = D(), c = t ?? "click", s = o ?? a.label, i = l ?? a.active === !0, p = r ?? a.disabled === !0, v = be(() => {
    n(c);
  }, [n, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: p,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    s
  );
}, he = () => {
  const t = L(), o = D(), l = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ve } = e, Ee = () => {
  const t = L(), o = D(), l = t.tabs ?? [], r = t.activeTabId, a = ve((n) => {
    n !== r && o("selectTab", { tabId: n });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((n) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: n.id,
      role: "tab",
      "aria-selected": n.id === r,
      className: "tlReactTabBar__tab" + (n.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(n.id)
    },
    n.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, _e = () => {
  const t = L(), o = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((r, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(P, { control: r })))));
}, ge = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ye = () => {
  const t = L(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), p = t.status ?? "idle", v = t.error, f = p === "received" ? "idle" : l !== "idle" ? l : p, k = e.useCallback(async () => {
    if (l === "recording") {
      const b = c.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (l !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = b, s.current = [];
        const R = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", T = new MediaRecorder(b, R ? { mimeType: R } : void 0);
        c.current = T, T.ondataavailable = (E) => {
          E.data.size > 0 && s.current.push(E.data);
        }, T.onstop = async () => {
          b.getTracks().forEach((N) => N.stop()), i.current = null;
          const E = new Blob(s.current, { type: T.mimeType || "audio/webm" });
          if (s.current = [], E.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const w = new FormData();
          w.append("audio", E, "recording.webm"), await o(w), r("idle");
        }, T.start(), r("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), n("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [l, o]), g = M(ge), C = f === "recording" ? g["js.audioRecorder.stop"] : f === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], m = f === "uploading", h = ["tlAudioRecorder__button"];
  return f === "recording" && h.push("tlAudioRecorder__button--recording"), f === "uploading" && h.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: k,
      disabled: m,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, Ce = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ke = () => {
  const t = L(), o = G(), l = !!t.hasAudio, r = t.dataRevision ?? 0, [a, n] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    l ? a === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), n("disabled"));
  }, [l]), e.useEffect(() => {
    r !== i.current && (i.current = r, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (a === "playing" || a === "paused" || a === "loading") && n("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      c.current && c.current.pause(), n("paused");
      return;
    }
    if (a === "paused" && c.current) {
      c.current.play(), n("playing");
      return;
    }
    if (!s.current) {
      n("loading");
      try {
        const m = await fetch(o);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), n("idle");
          return;
        }
        const h = await m.blob();
        s.current = URL.createObjectURL(h);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), n("idle");
        return;
      }
    }
    const C = new Audio(s.current);
    c.current = C, C.onended = () => {
      n("idle");
    }, C.play(), n("playing");
  }, [a, o]), v = M(Ce), f = a === "loading" ? v["js.loading"] : a === "playing" ? v["js.audioPlayer.pause"] : a === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], k = a === "disabled" || a === "loading", g = ["tlAudioPlayer__button"];
  return a === "playing" && g.push("tlAudioPlayer__button--playing"), a === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: p,
      disabled: k,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Se = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, we = () => {
  const t = L(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(!1), c = e.useRef(null), s = t.status ?? "idle", i = t.error, p = t.accept ?? "", v = s === "received" ? "idle" : l !== "idle" ? l : s, f = e.useCallback(async (E) => {
    r("uploading");
    const w = new FormData();
    w.append("file", E, E.name), await o(w), r("idle");
  }, [o]), k = e.useCallback((E) => {
    var N;
    const w = (N = E.target.files) == null ? void 0 : N[0];
    w && f(w);
  }, [f]), g = e.useCallback(() => {
    var E;
    l !== "uploading" && ((E = c.current) == null || E.click());
  }, [l]), C = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), n(!0);
  }, []), m = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), n(!1);
  }, []), h = e.useCallback((E) => {
    var N;
    if (E.preventDefault(), E.stopPropagation(), n(!1), l === "uploading") return;
    const w = (N = E.dataTransfer.files) == null ? void 0 : N[0];
    w && f(w);
  }, [l, f]), b = v === "uploading", R = M(Se), T = v === "uploading" ? R["js.uploading"] : R["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: m,
      onDrop: h
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: p || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (v === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: g,
        disabled: b,
        title: T,
        "aria-label": T
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Ne = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Le = () => {
  const t = L(), o = G(), l = D(), r = !!t.hasData, a = t.dataRevision ?? 0, n = t.fileName ?? "download", c = !!t.clearable, [s, i] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!r || s)) {
      i(!0);
      try {
        const g = o + (o.includes("?") ? "&" : "?") + "rev=" + a, C = await fetch(g);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const m = await C.blob(), h = URL.createObjectURL(m), b = document.createElement("a");
        b.href = h, b.download = n, b.style.display = "none", document.body.appendChild(b), b.click(), document.body.removeChild(b), URL.revokeObjectURL(h);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [r, s, o, a, n]), v = e.useCallback(async () => {
    r && await l("clear");
  }, [r, l]), f = M(Ne);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const k = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: s,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Re = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, xe = () => {
  const t = L(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), p = e.useRef(null), v = e.useRef(null), f = e.useRef(null), k = e.useRef(null), g = t.error, C = e.useMemo(
    () => {
      var d;
      return !!(window.isSecureContext && ((d = navigator.mediaDevices) != null && d.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null), i.current && (i.current.srcObject = null);
  }, []), h = e.useCallback(() => {
    m(), r("idle");
  }, [m]), b = e.useCallback(async () => {
    var d;
    if (l !== "uploading") {
      if (n(null), !C) {
        (d = f.current) == null || d.click();
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = y, r("overlayOpen");
      } catch (y) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", y), n("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [l, C]), R = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const d = i.current, y = v.current;
    if (!d || !y)
      return;
    y.width = d.videoWidth, y.height = d.videoHeight;
    const _ = y.getContext("2d");
    _ && (_.drawImage(d, 0, 0), m(), r("uploading"), y.toBlob(async (x) => {
      if (!x) {
        r("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", x, "capture.jpg"), await o(I), r("idle");
    }, "image/jpeg", 0.85));
  }, [l, o, m]), T = e.useCallback(async (d) => {
    var x;
    const y = (x = d.target.files) == null ? void 0 : x[0];
    if (!y) return;
    r("uploading");
    const _ = new FormData();
    _.append("photo", y, y.name), await o(_), r("idle"), f.current && (f.current.value = "");
  }, [o]);
  e.useEffect(() => {
    l === "overlayOpen" && i.current && p.current && (i.current.srcObject = p.current);
  }, [l]), e.useEffect(() => {
    var y;
    if (l !== "overlayOpen") return;
    (y = k.current) == null || y.focus();
    const d = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = d;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const d = (y) => {
      y.key === "Escape" && h();
    };
    return document.addEventListener("keydown", d), () => document.removeEventListener("keydown", d);
  }, [l, h]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null);
  }, []);
  const E = M(Re), w = l === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], N = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && N.push("tlPhotoCapture__cameraBtn--uploading");
  const B = ["tlPhotoCapture__overlayVideo"];
  c && B.push("tlPhotoCapture__overlayVideo--mirrored");
  const u = ["tlPhotoCapture__mirrorBtn"];
  return c && u.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: b,
      disabled: l === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: T
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: h }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: B.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: u.join(" "),
        onClick: () => s((d) => !d),
        title: E["js.photoCapture.mirror"],
        "aria-label": E["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: R,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: h,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[a]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Te = {
  "js.photoViewer.alt": "Captured photo"
}, Pe = () => {
  const t = L(), o = G(), l = !!t.hasPhoto, r = t.dataRevision ?? 0, [a, n] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!l) {
      a && (URL.revokeObjectURL(a), n(null));
      return;
    }
    if (r === c.current && a)
      return;
    c.current = r, a && (URL.revokeObjectURL(a), n(null));
    let i = !1;
    return (async () => {
      try {
        const p = await fetch(o);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const v = await p.blob();
        i || n(URL.createObjectURL(v));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      i = !0;
    };
  }, [l, r, o]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const s = M(Te);
  return !l || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: J, useRef: O } = e, je = () => {
  const t = L(), o = D(), l = t.orientation, r = t.resizable === !0, a = t.children ?? [], n = l === "horizontal", c = a.length > 0 && a.every((m) => m.collapsed), s = !c && a.some((m) => m.collapsed), i = c ? !n : n, p = O(null), v = O(null), f = O(null), k = J((m, h) => {
    const b = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !i ? b.flex = "1 0 0%" : b.flex = "0 0 auto" : h !== void 0 ? b.flex = `0 0 ${h}px` : m.unit === "%" || s ? b.flex = `${m.size} 0 0%` : b.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (b.minWidth = n ? m.minSize : void 0, b.minHeight = n ? void 0 : m.minSize), b;
  }, [n, c, s, i]), g = J((m, h) => {
    m.preventDefault();
    const b = p.current;
    if (!b) return;
    const R = a[h], T = a[h + 1], E = b.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    E.forEach((u) => {
      w.push(n ? u.offsetWidth : u.offsetHeight);
    }), f.current = w, v.current = {
      splitterIndex: h,
      startPos: n ? m.clientX : m.clientY,
      startSizeBefore: w[h],
      startSizeAfter: w[h + 1],
      childBefore: R,
      childAfter: T
    };
    const N = (u) => {
      const d = v.current;
      if (!d || !f.current) return;
      const _ = (n ? u.clientX : u.clientY) - d.startPos, x = d.childBefore.minSize || 0, I = d.childAfter.minSize || 0;
      let z = d.startSizeBefore + _, U = d.startSizeAfter - _;
      z < x && (U += z - x, z = x), U < I && (z += U - I, U = I), f.current[d.splitterIndex] = z, f.current[d.splitterIndex + 1] = U;
      const q = b.querySelectorAll(":scope > .tlSplitPanel__child"), X = q[d.splitterIndex], Z = q[d.splitterIndex + 1];
      X && (X.style.flex = `0 0 ${z}px`), Z && (Z.style.flex = `0 0 ${U}px`);
    }, B = () => {
      if (document.removeEventListener("mousemove", N), document.removeEventListener("mouseup", B), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const u = {};
        a.forEach((d, y) => {
          const _ = d.control;
          _ != null && _.controlId && f.current && (u[_.controlId] = f.current[y]);
        }), o("updateSizes", { sizes: u });
      }
      f.current = null, v.current = null;
    };
    document.addEventListener("mousemove", N), document.addEventListener("mouseup", B), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, n, o]), C = [];
  return a.forEach((m, h) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${h}`,
          className: `tlSplitPanel__child${m.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(m)
        },
        /* @__PURE__ */ e.createElement(P, { control: m.control })
      )
    ), r && h < a.length - 1) {
      const b = a[h + 1];
      !m.collapsed && !b.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${h}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (T) => g(T, h)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      className: `tlSplitPanel tlSplitPanel--${l}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    C
  );
}, { useCallback: V } = e, De = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Me = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Be = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ie = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ue = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ae = () => {
  const t = L(), o = D(), l = M(De), r = t.title, a = t.expansionState ?? "NORMALIZED", n = t.showMinimize === !0, c = t.showMaximize === !0, s = t.showPopOut === !0, i = t.toolbarButtons ?? [], p = a === "MINIMIZED", v = a === "MAXIMIZED", f = a === "HIDDEN", k = V(() => {
    o("toggleMinimize");
  }, [o]), g = V(() => {
    o("toggleMaximize");
  }, [o]), C = V(() => {
    o("popOut");
  }, [o]);
  if (f)
    return null;
  const m = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((h, b) => /* @__PURE__ */ e.createElement("span", { key: b, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(P, { control: h }))), n && !v && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Be, null) : /* @__PURE__ */ e.createElement(Me, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: v ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      v ? /* @__PURE__ */ e.createElement(ze, null) : /* @__PURE__ */ e.createElement(Ie, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ue, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(P, { control: t.child }))
  );
}, Fe = () => {
  const t = L();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(P, { control: t.child })
  );
}, $e = () => {
  const t = L();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(P, { control: t.activeChild }));
}, { useCallback: j, useState: H, useEffect: $, useRef: W } = e, Oe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function K(t, o, l, r) {
  const a = [];
  for (const n of t)
    n.type === "nav" ? a.push({ id: n.id, type: "nav", groupId: r }) : n.type === "command" ? a.push({ id: n.id, type: "command", groupId: r }) : n.type === "group" && (a.push({ id: n.id, type: "group" }), (l.get(n.id) ?? n.expanded) && !o && a.push(...K(n.children, o, l, n.id)));
  return a;
}
const A = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Ve = ({ item: t, active: o, collapsed: l, onSelect: r, tabIndex: a, itemRef: n, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (o ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(t.id),
    title: l ? t.label : void 0,
    tabIndex: a,
    ref: n,
    onFocus: () => c(t.id)
  },
  l && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !l && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), He = ({ item: t, collapsed: o, onExecute: l, tabIndex: r, itemRef: a, onFocus: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(t.id),
    title: o ? t.label : void 0,
    tabIndex: r,
    ref: a,
    onFocus: () => n(t.id)
  },
  /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), We = ({ item: t, collapsed: o }) => o && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: o ? t.label : void 0 }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), Ke = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ye = ({ item: t, activeItemId: o, onSelect: l, onExecute: r, onClose: a }) => {
  const n = W(null);
  $(() => {
    const s = (i) => {
      n.current && !n.current.contains(i.target) && setTimeout(() => a(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [a]), $(() => {
    const s = (i) => {
      i.key === "Escape" && a();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
  }, [a]);
  const c = j((s) => {
    s.type === "nav" ? (l(s.id), a()) : s.type === "command" && (r(s.id), a());
  }, [l, r, a]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: n, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, t.label), t.children.map((s) => {
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
        /* @__PURE__ */ e.createElement(A, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ge = ({
  item: t,
  expanded: o,
  activeItemId: l,
  collapsed: r,
  onSelect: a,
  onExecute: n,
  onToggleGroup: c,
  tabIndex: s,
  itemRef: i,
  onFocus: p,
  focusedId: v,
  setItemRef: f,
  onItemFocus: k,
  flyoutGroupId: g,
  onOpenFlyout: C,
  onCloseFlyout: m
}) => {
  const h = j(() => {
    r ? g === t.id ? m() : C(t.id) : c(t.id);
  }, [r, g, t.id, c, C, m]), b = r && g === t.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: h,
      title: r ? t.label : void 0,
      "aria-expanded": r ? b : o,
      tabIndex: s,
      ref: i,
      onFocus: () => p(t.id)
    },
    /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
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
  ), b && /* @__PURE__ */ e.createElement(
    Ye,
    {
      item: t,
      activeItemId: l,
      onSelect: a,
      onExecute: n,
      onClose: m
    }
  ), o && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((R) => /* @__PURE__ */ e.createElement(
    Q,
    {
      key: R.id,
      item: R,
      activeItemId: l,
      collapsed: r,
      onSelect: a,
      onExecute: n,
      onToggleGroup: c,
      focusedId: v,
      setItemRef: f,
      onItemFocus: k,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))));
}, Q = ({
  item: t,
  activeItemId: o,
  collapsed: l,
  onSelect: r,
  onExecute: a,
  onToggleGroup: n,
  focusedId: c,
  setItemRef: s,
  onItemFocus: i,
  groupStates: p,
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: k
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ve,
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
        He,
        {
          item: t,
          collapsed: l,
          onExecute: a,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(We, { item: t, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ke, null);
    case "group": {
      const g = p ? p.get(t.id) ?? t.expanded : t.expanded;
      return /* @__PURE__ */ e.createElement(
        Ge,
        {
          item: t,
          expanded: g,
          activeItemId: o,
          collapsed: l,
          onSelect: r,
          onExecute: a,
          onToggleGroup: n,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i,
          focusedId: c,
          setItemRef: s,
          onItemFocus: i,
          flyoutGroupId: v,
          onOpenFlyout: f,
          onCloseFlyout: k
        }
      );
    }
    default:
      return null;
  }
}, qe = () => {
  const t = L(), o = D(), l = M(Oe), r = t.items ?? [], a = t.activeItemId, n = t.collapsed, [c, s] = H(() => {
    const u = /* @__PURE__ */ new Map(), d = (y) => {
      for (const _ of y)
        _.type === "group" && (u.set(_.id, _.expanded), d(_.children));
    };
    return d(r), u;
  }), i = j((u) => {
    s((d) => {
      const y = new Map(d), _ = y.get(u) ?? !1;
      return y.set(u, !_), o("toggleGroup", { itemId: u, expanded: !_ }), y;
    });
  }, [o]), p = j((u) => {
    u !== a && o("selectItem", { itemId: u });
  }, [o, a]), v = j((u) => {
    o("executeCommand", { itemId: u });
  }, [o]), f = j(() => {
    o("toggleCollapse", {});
  }, [o]), [k, g] = H(null), C = j((u) => {
    g(u);
  }, []), m = j(() => {
    g(null);
  }, []);
  $(() => {
    n || g(null);
  }, [n]);
  const [h, b] = H(() => {
    const u = K(r, n, c);
    return u.length > 0 ? u[0].id : "";
  }), R = W(/* @__PURE__ */ new Map()), T = j((u) => (d) => {
    d ? R.current.set(u, d) : R.current.delete(u);
  }, []), E = j((u) => {
    b(u);
  }, []), w = W(0), N = j((u) => {
    b(u), w.current++;
  }, []);
  $(() => {
    const u = R.current.get(h);
    u && document.activeElement !== u && u.focus();
  }, [h, w.current]);
  const B = j((u) => {
    if (u.key === "Escape" && k !== null) {
      u.preventDefault(), m();
      return;
    }
    const d = K(r, n, c);
    if (d.length === 0) return;
    const y = d.findIndex((x) => x.id === h);
    if (y < 0) return;
    const _ = d[y];
    switch (u.key) {
      case "ArrowDown": {
        u.preventDefault();
        const x = (y + 1) % d.length;
        N(d[x].id);
        break;
      }
      case "ArrowUp": {
        u.preventDefault();
        const x = (y - 1 + d.length) % d.length;
        N(d[x].id);
        break;
      }
      case "Home": {
        u.preventDefault(), N(d[0].id);
        break;
      }
      case "End": {
        u.preventDefault(), N(d[d.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        u.preventDefault(), _.type === "nav" ? p(_.id) : _.type === "command" ? v(_.id) : _.type === "group" && (n ? k === _.id ? m() : C(_.id) : i(_.id));
        break;
      }
      case "ArrowRight": {
        _.type === "group" && !n && ((c.get(_.id) ?? !1) || (u.preventDefault(), i(_.id)));
        break;
      }
      case "ArrowLeft": {
        _.type === "group" && !n && (c.get(_.id) ?? !1) && (u.preventDefault(), i(_.id));
        break;
      }
    }
  }, [
    r,
    n,
    c,
    h,
    k,
    N,
    p,
    v,
    i,
    C,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (n ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, n ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: B }, r.map((u) => /* @__PURE__ */ e.createElement(
    Q,
    {
      key: u.id,
      item: u,
      activeItemId: a,
      collapsed: n,
      onSelect: p,
      onExecute: v,
      onToggleGroup: i,
      focusedId: h,
      setItemRef: T,
      onItemFocus: E,
      groupStates: c,
      flyoutGroupId: k,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))), n ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: n ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: n ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, Xe = () => {
  const t = L(), o = t.direction ?? "column", l = t.gap ?? "default", r = t.align ?? "stretch", a = t.wrap === !0, n = t.children ?? [], c = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${r}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { className: c }, n.map((s, i) => /* @__PURE__ */ e.createElement(P, { key: i, control: s })));
}, Ze = () => {
  const t = L(), o = t.columns, l = t.minColumnWidth, r = t.gap ?? "default", a = t.children ?? [], n = {};
  return l ? n.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : o && (n.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { className: `tlGrid tlGrid--gap-${r}`, style: n }, a.map((c, s) => /* @__PURE__ */ e.createElement(P, { key: s, control: c })));
};
S("TLButton", pe);
S("TLToggleButton", fe);
S("TLTextInput", ae);
S("TLNumberInput", le);
S("TLDatePicker", re);
S("TLSelect", ce);
S("TLCheckbox", de);
S("TLTable", ue);
S("TLCounter", he);
S("TLTabBar", Ee);
S("TLFieldList", _e);
S("TLAudioRecorder", ye);
S("TLAudioPlayer", ke);
S("TLFileUpload", we);
S("TLDownload", Le);
S("TLPhotoCapture", xe);
S("TLPhotoViewer", Pe);
S("TLSplitPanel", je);
S("TLPanel", Ae);
S("TLMaximizeRoot", Fe);
S("TLDeckPane", $e);
S("TLSidebar", qe);
S("TLStack", Xe);
S("TLGrid", Ze);
