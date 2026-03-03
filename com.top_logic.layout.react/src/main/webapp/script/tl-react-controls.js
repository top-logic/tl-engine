import { React as e, useTLFieldValue as $, getComponent as ee, useTLState as S, useTLCommand as j, TLChild as R, useTLUpload as Y, useI18N as D, useTLDataUrl as G, register as k } from "tl-react-bridge";
const { useCallback: te } = e, ae = ({ state: t }) => {
  const [r, l] = $(), o = te(
    (n) => {
      l(n.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: ne } = e, le = ({ state: t, config: r }) => {
  const [l, o] = $(), n = ne(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      o(i);
    },
    [o]
  ), a = r != null && r.decimal ? "0.01" : "1";
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
}, { useCallback: re } = e, oe = ({ state: t }) => {
  const [r, l] = $(), o = re(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: se } = e, ce = ({ state: t, config: r }) => {
  var c;
  const [l, o] = $(), n = se(
    (s) => {
      o(s.target.value || null);
    },
    [o]
  ), a = t.options ?? (r == null ? void 0 : r.options) ?? [];
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
}, { useCallback: ie } = e, de = ({ state: t }) => {
  const [r, l] = $(), o = ie(
    (n) => {
      l(n.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
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
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, ue = ({ controlId: t, state: r }) => {
  const l = r.columns ?? [], o = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((n, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, l.map((c) => {
    const s = c.cellModule ? ee(c.cellModule) : void 0, i = n[c.name];
    if (s) {
      const p = { value: i, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: t + "-" + a + "-" + c.name,
          state: p
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: me } = e, pe = ({ command: t, label: r, disabled: l }) => {
  const o = S(), n = j(), a = t ?? "click", c = r ?? o.label, s = l ?? o.disabled === !0, i = me(() => {
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
}, { useCallback: be } = e, fe = ({ command: t, label: r, active: l, disabled: o }) => {
  const n = S(), a = j(), c = t ?? "click", s = r ?? n.label, i = l ?? n.active === !0, p = o ?? n.disabled === !0, v = be(() => {
    a(c);
  }, [a, c]);
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
  const t = S(), r = j(), l = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ve } = e, _e = () => {
  const t = S(), r = j(), l = t.tabs ?? [], o = t.activeTabId, n = ve((a) => {
    a !== o && r("selectTab", { tabId: a });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === o,
      className: "tlReactTabBar__tab" + (a.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => n(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, Ee = () => {
  const t = S(), r = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((o, n) => /* @__PURE__ */ e.createElement("div", { key: n, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: o })))));
}, ge = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ce = () => {
  const t = S(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), p = t.status ?? "idle", v = t.error, f = p === "received" ? "idle" : l !== "idle" ? l : p, N = e.useCallback(async () => {
    if (l === "recording") {
      const b = c.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (l !== "uploading") {
      if (a(null), !window.isSecureContext || !navigator.mediaDevices) {
        a("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = b, s.current = [];
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(b, T ? { mimeType: T } : void 0);
        c.current = P, P.ondataavailable = (_) => {
          _.data.size > 0 && s.current.push(_.data);
        }, P.onstop = async () => {
          b.getTracks().forEach((L) => L.stop()), i.current = null;
          const _ = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], _.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const w = new FormData();
          w.append("audio", _, "recording.webm"), await r(w), o("idle");
        }, P.start(), o("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), a("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [l, r]), g = D(ge), y = f === "recording" ? g["js.audioRecorder.stop"] : f === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], m = f === "uploading", h = ["tlAudioRecorder__button"];
  return f === "recording" && h.push("tlAudioRecorder__button--recording"), f === "uploading" && h.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: N,
      disabled: m,
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[n]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, ye = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ke = () => {
  const t = S(), r = G(), l = !!t.hasAudio, o = t.dataRevision ?? 0, [n, a] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(o);
  e.useEffect(() => {
    l ? n === "disabled" && a("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), a("disabled"));
  }, [l]), e.useEffect(() => {
    o !== i.current && (i.current = o, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (n === "playing" || n === "paused" || n === "loading") && a("idle"));
  }, [o]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const p = e.useCallback(async () => {
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
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), a("idle");
          return;
        }
        const h = await m.blob();
        s.current = URL.createObjectURL(h);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), a("idle");
        return;
      }
    }
    const y = new Audio(s.current);
    c.current = y, y.onended = () => {
      a("idle");
    }, y.play(), a("playing");
  }, [n, r]), v = D(ye), f = n === "loading" ? v["js.loading"] : n === "playing" ? v["js.audioPlayer.pause"] : n === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], N = n === "disabled" || n === "loading", g = ["tlAudioPlayer__button"];
  return n === "playing" && g.push("tlAudioPlayer__button--playing"), n === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: p,
      disabled: N,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${n === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ne = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, we = () => {
  const t = S(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(!1), c = e.useRef(null), s = t.status ?? "idle", i = t.error, p = t.accept ?? "", v = s === "received" ? "idle" : l !== "idle" ? l : s, f = e.useCallback(async (_) => {
    o("uploading");
    const w = new FormData();
    w.append("file", _, _.name), await r(w), o("idle");
  }, [r]), N = e.useCallback((_) => {
    var L;
    const w = (L = _.target.files) == null ? void 0 : L[0];
    w && f(w);
  }, [f]), g = e.useCallback(() => {
    var _;
    l !== "uploading" && ((_ = c.current) == null || _.click());
  }, [l]), y = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), a(!0);
  }, []), m = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), a(!1);
  }, []), h = e.useCallback((_) => {
    var L;
    if (_.preventDefault(), _.stopPropagation(), a(!1), l === "uploading") return;
    const w = (L = _.dataTransfer.files) == null ? void 0 : L[0];
    w && f(w);
  }, [l, f]), b = v === "uploading", T = D(Ne), P = v === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${n ? " tlFileUpload--dragover" : ""}`,
      onDragOver: y,
      onDragLeave: m,
      onDrop: h
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: p || void 0,
        onChange: N,
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
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Se = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Le = () => {
  const t = S(), r = G(), l = j(), o = !!t.hasData, n = t.dataRevision ?? 0, a = t.fileName ?? "download", c = !!t.clearable, [s, i] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!o || s)) {
      i(!0);
      try {
        const g = r + (r.includes("?") ? "&" : "?") + "rev=" + n, y = await fetch(g);
        if (!y.ok) {
          console.error("[TLDownload] Failed to fetch data:", y.status);
          return;
        }
        const m = await y.blob(), h = URL.createObjectURL(m), b = document.createElement("a");
        b.href = h, b.download = a, b.style.display = "none", document.body.appendChild(b), b.click(), document.body.removeChild(b), URL.revokeObjectURL(h);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        i(!1);
      }
    }
  }, [o, s, r, n, a]), v = e.useCallback(async () => {
    o && await l("clear");
  }, [o, l]), f = D(Se);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const N = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
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
}, Te = () => {
  const t = S(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), p = e.useRef(null), v = e.useRef(null), f = e.useRef(null), N = e.useRef(null), g = t.error, y = e.useMemo(
    () => {
      var d;
      return !!(window.isSecureContext && ((d = navigator.mediaDevices) != null && d.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null), i.current && (i.current.srcObject = null);
  }, []), h = e.useCallback(() => {
    m(), o("idle");
  }, [m]), b = e.useCallback(async () => {
    var d;
    if (l !== "uploading") {
      if (a(null), !y) {
        (d = f.current) == null || d.click();
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = C, o("overlayOpen");
      } catch (C) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", C), a("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [l, y]), T = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const d = i.current, C = v.current;
    if (!d || !C)
      return;
    C.width = d.videoWidth, C.height = d.videoHeight;
    const E = C.getContext("2d");
    E && (E.drawImage(d, 0, 0), m(), o("uploading"), C.toBlob(async (x) => {
      if (!x) {
        o("idle");
        return;
      }
      const A = new FormData();
      A.append("photo", x, "capture.jpg"), await r(A), o("idle");
    }, "image/jpeg", 0.85));
  }, [l, r, m]), P = e.useCallback(async (d) => {
    var x;
    const C = (x = d.target.files) == null ? void 0 : x[0];
    if (!C) return;
    o("uploading");
    const E = new FormData();
    E.append("photo", C, C.name), await r(E), o("idle"), f.current && (f.current.value = "");
  }, [r]);
  e.useEffect(() => {
    l === "overlayOpen" && i.current && p.current && (i.current.srcObject = p.current);
  }, [l]), e.useEffect(() => {
    var C;
    if (l !== "overlayOpen") return;
    (C = N.current) == null || C.focus();
    const d = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = d;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const d = (C) => {
      C.key === "Escape" && h();
    };
    return document.addEventListener("keydown", d), () => document.removeEventListener("keydown", d);
  }, [l, h]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null);
  }, []);
  const _ = D(Re), w = l === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], L = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && L.push("tlPhotoCapture__cameraBtn--uploading");
  const M = ["tlPhotoCapture__overlayVideo"];
  c && M.push("tlPhotoCapture__overlayVideo--mirrored");
  const u = ["tlPhotoCapture__mirrorBtn"];
  return c && u.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: b,
      disabled: l === "uploading",
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !y && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
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
        className: M.join(" "),
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
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: h,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[n]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, xe = {
  "js.photoViewer.alt": "Captured photo"
}, Pe = () => {
  const t = S(), r = G(), l = !!t.hasPhoto, o = t.dataRevision ?? 0, [n, a] = e.useState(null), c = e.useRef(o);
  e.useEffect(() => {
    if (!l) {
      n && (URL.revokeObjectURL(n), a(null));
      return;
    }
    if (o === c.current && n)
      return;
    c.current = o, n && (URL.revokeObjectURL(n), a(null));
    let i = !1;
    return (async () => {
      try {
        const p = await fetch(r);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const v = await p.blob();
        i || a(URL.createObjectURL(v));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      i = !0;
    };
  }, [l, o, r]), e.useEffect(() => () => {
    n && URL.revokeObjectURL(n);
  }, []);
  const s = D(xe);
  return !l || !n ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: n,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: J, useRef: O } = e, Be = () => {
  const t = S(), r = j(), l = t.orientation, o = t.resizable === !0, n = t.children ?? [], a = l === "horizontal", c = n.length > 0 && n.every((m) => m.collapsed), s = !c && n.some((m) => m.collapsed), i = c ? !a : a, p = O(null), v = O(null), f = O(null), N = J((m, h) => {
    const b = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !i ? b.flex = "1 0 0%" : b.flex = "0 0 auto" : h !== void 0 ? b.flex = `0 0 ${h}px` : m.unit === "%" || s ? b.flex = `${m.size} 0 0%` : b.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (b.minWidth = a ? m.minSize : void 0, b.minHeight = a ? void 0 : m.minSize), b;
  }, [a, c, s, i]), g = J((m, h) => {
    m.preventDefault();
    const b = p.current;
    if (!b) return;
    const T = n[h], P = n[h + 1], _ = b.querySelectorAll(":scope > .tlSplitPanel__child"), w = [];
    _.forEach((u) => {
      w.push(a ? u.offsetWidth : u.offsetHeight);
    }), f.current = w, v.current = {
      splitterIndex: h,
      startPos: a ? m.clientX : m.clientY,
      startSizeBefore: w[h],
      startSizeAfter: w[h + 1],
      childBefore: T,
      childAfter: P
    };
    const L = (u) => {
      const d = v.current;
      if (!d || !f.current) return;
      const E = (a ? u.clientX : u.clientY) - d.startPos, x = d.childBefore.minSize || 0, A = d.childAfter.minSize || 0;
      let I = d.startSizeBefore + E, z = d.startSizeAfter - E;
      I < x && (z += I - x, I = x), z < A && (I += z - A, z = A), f.current[d.splitterIndex] = I, f.current[d.splitterIndex + 1] = z;
      const q = b.querySelectorAll(":scope > .tlSplitPanel__child"), X = q[d.splitterIndex], Z = q[d.splitterIndex + 1];
      X && (X.style.flex = `0 0 ${I}px`), Z && (Z.style.flex = `0 0 ${z}px`);
    }, M = () => {
      if (document.removeEventListener("mousemove", L), document.removeEventListener("mouseup", M), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const u = {};
        n.forEach((d, C) => {
          const E = d.control;
          E != null && E.controlId && f.current && (u[E.controlId] = f.current[C]);
        }), r("updateSizes", { sizes: u });
      }
      f.current = null, v.current = null;
    };
    document.addEventListener("mousemove", L), document.addEventListener("mouseup", M), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [n, a, r]), y = [];
  return n.forEach((m, h) => {
    if (y.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${h}`,
          className: `tlSplitPanel__child${m.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(m)
        },
        /* @__PURE__ */ e.createElement(R, { control: m.control })
      )
    ), o && h < n.length - 1) {
      const b = n[h + 1];
      !m.collapsed && !b.collapsed && y.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${h}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (P) => g(P, h)
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
    y
  );
}, { useCallback: V } = e, je = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, De = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Me = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ae = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ie = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ue = () => {
  const t = S(), r = j(), l = D(je), o = t.title, n = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, c = t.showMaximize === !0, s = t.showPopOut === !0, i = t.toolbarButtons ?? [], p = n === "MINIMIZED", v = n === "MAXIMIZED", f = n === "HIDDEN", N = V(() => {
    r("toggleMinimize");
  }, [r]), g = V(() => {
    r("toggleMaximize");
  }, [r]), y = V(() => {
    r("popOut");
  }, [r]);
  if (f)
    return null;
  const m = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${n.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((h, b) => /* @__PURE__ */ e.createElement("span", { key: b, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(R, { control: h }))), a && !v && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Me, null) : /* @__PURE__ */ e.createElement(De, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: v ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      v ? /* @__PURE__ */ e.createElement(Ie, null) : /* @__PURE__ */ e.createElement(Ae, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ze, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child }))
  );
}, $e = () => {
  const t = S();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, Fe = () => {
  const t = S();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: B, useState: H, useEffect: F, useRef: W } = e, Oe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function K(t, r, l, o) {
  const n = [];
  for (const a of t)
    a.type === "nav" ? n.push({ id: a.id, type: "nav", groupId: o }) : a.type === "command" ? n.push({ id: a.id, type: "command", groupId: o }) : a.type === "group" && (n.push({ id: a.id, type: "group" }), (l.get(a.id) ?? a.expanded) && !r && n.push(...K(a.children, r, l, a.id)));
  return n;
}
const U = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Ve = ({ item: t, active: r, collapsed: l, onSelect: o, tabIndex: n, itemRef: a, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (r ? " tlSidebar__navItem--active" : ""),
    onClick: () => o(t.id),
    title: l ? t.label : void 0,
    tabIndex: n,
    ref: a,
    onFocus: () => c(t.id)
  },
  l && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(U, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !l && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), He = ({ item: t, collapsed: r, onExecute: l, tabIndex: o, itemRef: n, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(t.id),
    title: r ? t.label : void 0,
    tabIndex: o,
    ref: n,
    onFocus: () => a(t.id)
  },
  /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), We = ({ item: t, collapsed: r }) => r && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: r ? t.label : void 0 }, /* @__PURE__ */ e.createElement(U, { icon: t.icon }), !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), Ke = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ye = ({ item: t, activeItemId: r, onSelect: l, onExecute: o, onClose: n }) => {
  const a = W(null);
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
  const c = B((s) => {
    s.type === "nav" ? (l(s.id), n()) : s.type === "command" && (o(s.id), n());
  }, [l, o, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, t.label), t.children.map((s) => {
    if (s.type === "nav" || s.type === "command") {
      const i = s.type === "nav" && s.id === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => c(s)
        },
        /* @__PURE__ */ e.createElement(U, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ge = ({
  item: t,
  expanded: r,
  activeItemId: l,
  collapsed: o,
  onSelect: n,
  onExecute: a,
  onToggleGroup: c,
  tabIndex: s,
  itemRef: i,
  onFocus: p,
  focusedId: v,
  setItemRef: f,
  onItemFocus: N,
  flyoutGroupId: g,
  onOpenFlyout: y,
  onCloseFlyout: m
}) => {
  const h = B(() => {
    o ? g === t.id ? m() : y(t.id) : c(t.id);
  }, [o, g, t.id, c, y, m]), b = o && g === t.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: h,
      title: o ? t.label : void 0,
      "aria-expanded": o ? b : r,
      tabIndex: s,
      ref: i,
      onFocus: () => p(t.id)
    },
    /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
    !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
    !o && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlSidebar__chevron" + (r ? " tlSidebar__chevron--open" : ""),
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
      onSelect: n,
      onExecute: a,
      onClose: m
    }
  ), r && !o && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((T) => /* @__PURE__ */ e.createElement(
    Q,
    {
      key: T.id,
      item: T,
      activeItemId: l,
      collapsed: o,
      onSelect: n,
      onExecute: a,
      onToggleGroup: c,
      focusedId: v,
      setItemRef: f,
      onItemFocus: N,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: y,
      onCloseFlyout: m
    }
  ))));
}, Q = ({
  item: t,
  activeItemId: r,
  collapsed: l,
  onSelect: o,
  onExecute: n,
  onToggleGroup: a,
  focusedId: c,
  setItemRef: s,
  onItemFocus: i,
  groupStates: p,
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: N
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ve,
        {
          item: t,
          active: t.id === r,
          collapsed: l,
          onSelect: o,
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
          onExecute: n,
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
          activeItemId: r,
          collapsed: l,
          onSelect: o,
          onExecute: n,
          onToggleGroup: a,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i,
          focusedId: c,
          setItemRef: s,
          onItemFocus: i,
          flyoutGroupId: v,
          onOpenFlyout: f,
          onCloseFlyout: N
        }
      );
    }
    default:
      return null;
  }
}, qe = () => {
  const t = S(), r = j(), l = D(Oe), o = t.items ?? [], n = t.activeItemId, a = t.collapsed, [c, s] = H(() => {
    const u = /* @__PURE__ */ new Map(), d = (C) => {
      for (const E of C)
        E.type === "group" && (u.set(E.id, E.expanded), d(E.children));
    };
    return d(o), u;
  }), i = B((u) => {
    s((d) => {
      const C = new Map(d), E = C.get(u) ?? !1;
      return C.set(u, !E), r("toggleGroup", { itemId: u, expanded: !E }), C;
    });
  }, [r]), p = B((u) => {
    u !== n && r("selectItem", { itemId: u });
  }, [r, n]), v = B((u) => {
    r("executeCommand", { itemId: u });
  }, [r]), f = B(() => {
    r("toggleCollapse", {});
  }, [r]), [N, g] = H(null), y = B((u) => {
    g(u);
  }, []), m = B(() => {
    g(null);
  }, []);
  F(() => {
    a || g(null);
  }, [a]);
  const [h, b] = H(() => {
    const u = K(o, a, c);
    return u.length > 0 ? u[0].id : "";
  }), T = W(/* @__PURE__ */ new Map()), P = B((u) => (d) => {
    d ? T.current.set(u, d) : T.current.delete(u);
  }, []), _ = B((u) => {
    b(u);
  }, []), w = W(0), L = B((u) => {
    b(u), w.current++;
  }, []);
  F(() => {
    const u = T.current.get(h);
    u && document.activeElement !== u && u.focus();
  }, [h, w.current]);
  const M = B((u) => {
    if (u.key === "Escape" && N !== null) {
      u.preventDefault(), m();
      return;
    }
    const d = K(o, a, c);
    if (d.length === 0) return;
    const C = d.findIndex((x) => x.id === h);
    if (C < 0) return;
    const E = d[C];
    switch (u.key) {
      case "ArrowDown": {
        u.preventDefault();
        const x = (C + 1) % d.length;
        L(d[x].id);
        break;
      }
      case "ArrowUp": {
        u.preventDefault();
        const x = (C - 1 + d.length) % d.length;
        L(d[x].id);
        break;
      }
      case "Home": {
        u.preventDefault(), L(d[0].id);
        break;
      }
      case "End": {
        u.preventDefault(), L(d[d.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        u.preventDefault(), E.type === "nav" ? p(E.id) : E.type === "command" ? v(E.id) : E.type === "group" && (a ? N === E.id ? m() : y(E.id) : i(E.id));
        break;
      }
      case "ArrowRight": {
        E.type === "group" && !a && ((c.get(E.id) ?? !1) || (u.preventDefault(), i(E.id)));
        break;
      }
      case "ArrowLeft": {
        E.type === "group" && !a && (c.get(E.id) ?? !1) && (u.preventDefault(), i(E.id));
        break;
      }
    }
  }, [
    o,
    a,
    c,
    h,
    N,
    L,
    p,
    v,
    i,
    y,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: M }, o.map((u) => /* @__PURE__ */ e.createElement(
    Q,
    {
      key: u.id,
      item: u,
      activeItemId: n,
      collapsed: a,
      onSelect: p,
      onExecute: v,
      onToggleGroup: i,
      focusedId: h,
      setItemRef: P,
      onItemFocus: _,
      groupStates: c,
      flyoutGroupId: N,
      onOpenFlyout: y,
      onCloseFlyout: m
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
}, Xe = () => {
  const t = S(), r = t.direction ?? "column", l = t.gap ?? "default", o = t.align ?? "stretch", n = t.wrap === !0, a = t.children ?? [], c = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${o}`,
    n ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { className: c }, a.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s })));
}, Ze = () => {
  const t = S(), r = t.columns, l = t.minColumnWidth, o = t.gap ?? "default", n = t.children ?? [], a = {};
  return l ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : r && (a.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { className: `tlGrid tlGrid--gap-${o}`, style: a }, n.map((c, s) => /* @__PURE__ */ e.createElement(R, { key: s, control: c })));
}, Je = () => {
  const t = S(), r = t.title, l = t.variant ?? "outlined", o = t.padding ?? "default", n = t.headerActions ?? [], a = t.child, c = r != null || n.length > 0;
  return /* @__PURE__ */ e.createElement("div", { className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, n.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(R, { control: a })));
}, Qe = () => {
  const t = S(), r = t.title ?? "", l = t.leading, o = t.actions ?? [], n = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    n === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(R, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, o.map((s, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: s }))));
}, { useCallback: et } = e, tt = () => {
  const t = S(), r = j(), l = t.items ?? [], o = et((n) => {
    r("navigate", { itemId: n });
  }, [r]);
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
        onClick: () => o(n.id)
      },
      n.label
    ));
  })));
}, { useCallback: at } = e, nt = () => {
  const t = S(), r = j(), l = t.items ?? [], o = t.activeItemId, n = at((a) => {
    a !== o && r("selectItem", { itemId: a });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((a) => {
    const c = a.id === o;
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
};
k("TLButton", pe);
k("TLToggleButton", fe);
k("TLTextInput", ae);
k("TLNumberInput", le);
k("TLDatePicker", oe);
k("TLSelect", ce);
k("TLCheckbox", de);
k("TLTable", ue);
k("TLCounter", he);
k("TLTabBar", _e);
k("TLFieldList", Ee);
k("TLAudioRecorder", Ce);
k("TLAudioPlayer", ke);
k("TLFileUpload", we);
k("TLDownload", Le);
k("TLPhotoCapture", Te);
k("TLPhotoViewer", Pe);
k("TLSplitPanel", Be);
k("TLPanel", Ue);
k("TLMaximizeRoot", $e);
k("TLDeckPane", Fe);
k("TLSidebar", qe);
k("TLStack", Xe);
k("TLGrid", Ze);
k("TLCard", Je);
k("TLAppBar", Qe);
k("TLBreadcrumb", tt);
k("TLBottomBar", nt);
