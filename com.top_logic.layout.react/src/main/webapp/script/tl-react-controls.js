import { React as e, useTLFieldValue as U, getComponent as K, useTLState as L, useTLCommand as M, TLChild as D, useTLUpload as F, useI18N as B, useTLDataUrl as O, register as _ } from "tl-react-bridge";
const { useCallback: W } = e, q = ({ state: l }) => {
  const [r, a] = U(), o = W(
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
}, { useCallback: X } = e, Z = ({ state: l, config: r }) => {
  const [a, o] = U(), t = X(
    (c) => {
      const s = c.target.value, u = s === "" ? null : Number(s);
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
}, { useCallback: G } = e, J = ({ state: l }) => {
  const [r, a] = U(), o = G(
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
}, { useCallback: Q } = e, ee = ({ state: l, config: r }) => {
  var c;
  const [a, o] = U(), t = Q(
    (s) => {
      o(s.target.value || null);
    },
    [o]
  ), n = l.options ?? (r == null ? void 0 : r.options) ?? [];
  if (l.editable === !1) {
    const s = ((c = n.find((u) => u.value === a)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
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
    n.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: te } = e, ae = ({ state: l }) => {
  const [r, a] = U(), o = te(
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
}, le = ({ controlId: l, state: r }) => {
  const a = r.columns ?? [], o = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((t, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, a.map((c) => {
    const s = c.cellModule ? K(c.cellModule) : void 0, u = t[c.name];
    if (s) {
      const m = { value: u, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: l + "-" + n + "-" + c.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, u != null ? String(u) : "");
  })))));
}, { useCallback: ne } = e, oe = ({ command: l, label: r, disabled: a }) => {
  const o = L(), t = M(), n = l ?? "click", c = r ?? o.label, s = a ?? o.disabled === !0, u = ne(() => {
    t(n);
  }, [t, n]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: u,
      disabled: s,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: re } = e, se = ({ command: l, label: r, active: a, disabled: o }) => {
  const t = L(), n = M(), c = l ?? "click", s = r ?? t.label, u = a ?? t.active === !0, m = o ?? t.disabled === !0, p = re(() => {
    n(c);
  }, [n, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    s
  );
}, ce = () => {
  const l = L(), r = M(), a = l.count ?? 0, o = l.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ie } = e, ue = () => {
  const l = L(), r = M(), a = l.tabs ?? [], o = l.activeTabId, t = ie((n) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, l.activeContent && /* @__PURE__ */ e.createElement(D, { control: l.activeContent })));
}, de = () => {
  const l = L(), r = l.title, a = l.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(D, { control: o })))));
}, me = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, pe = () => {
  const l = L(), r = F(), [a, o] = e.useState("idle"), [t, n] = e.useState(null), c = e.useRef(null), s = e.useRef([]), u = e.useRef(null), m = l.status ?? "idle", p = l.error, f = m === "received" ? "idle" : a !== "idle" ? a : m, k = e.useCallback(async () => {
    if (a === "recording") {
      const E = c.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (a !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        u.current = E, s.current = [];
        const P = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", N = new MediaRecorder(E, P ? { mimeType: P } : void 0);
        c.current = N, N.ondataavailable = (d) => {
          d.data.size > 0 && s.current.push(d.data);
        }, N.onstop = async () => {
          E.getTracks().forEach((R) => R.stop()), u.current = null;
          const d = new Blob(s.current, { type: N.mimeType || "audio/webm" });
          if (s.current = [], d.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const g = new FormData();
          g.append("audio", d, "recording.webm"), await r(g), o("idle");
        }, N.start(), o("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), n("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, r]), y = B(me), i = f === "recording" ? y["js.audioRecorder.stop"] : f === "uploading" ? y["js.uploading"] : y["js.audioRecorder.record"], h = f === "uploading", b = ["tlAudioRecorder__button"];
  return f === "recording" && b.push("tlAudioRecorder__button--recording"), f === "uploading" && b.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: k,
      disabled: h,
      title: i,
      "aria-label": i
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), t && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, y[t]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, he = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, be = () => {
  const l = L(), r = O(), a = !!l.hasAudio, o = l.dataRevision ?? 0, [t, n] = e.useState(a ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), u = e.useRef(o);
  e.useEffect(() => {
    a ? t === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), n("disabled"));
  }, [a]), e.useEffect(() => {
    o !== u.current && (u.current = o, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (t === "playing" || t === "paused" || t === "loading") && n("idle"));
  }, [o]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      c.current && c.current.pause(), n("paused");
      return;
    }
    if (t === "paused" && c.current) {
      c.current.play(), n("playing");
      return;
    }
    if (!s.current) {
      n("loading");
      try {
        const h = await fetch(r);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), n("idle");
          return;
        }
        const b = await h.blob();
        s.current = URL.createObjectURL(b);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), n("idle");
        return;
      }
    }
    const i = new Audio(s.current);
    c.current = i, i.onended = () => {
      n("idle");
    }, i.play(), n("playing");
  }, [t, r]), p = B(he), f = t === "loading" ? p["js.loading"] : t === "playing" ? p["js.audioPlayer.pause"] : t === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], k = t === "disabled" || t === "loading", y = ["tlAudioPlayer__button"];
  return t === "playing" && y.push("tlAudioPlayer__button--playing"), t === "loading" && y.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: m,
      disabled: k,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, fe = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ve = () => {
  const l = L(), r = F(), [a, o] = e.useState("idle"), [t, n] = e.useState(!1), c = e.useRef(null), s = l.status ?? "idle", u = l.error, m = l.accept ?? "", p = s === "received" ? "idle" : a !== "idle" ? a : s, f = e.useCallback(async (d) => {
    o("uploading");
    const g = new FormData();
    g.append("file", d, d.name), await r(g), o("idle");
  }, [r]), k = e.useCallback((d) => {
    var R;
    const g = (R = d.target.files) == null ? void 0 : R[0];
    g && f(g);
  }, [f]), y = e.useCallback(() => {
    var d;
    a !== "uploading" && ((d = c.current) == null || d.click());
  }, [a]), i = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), n(!0);
  }, []), h = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), n(!1);
  }, []), b = e.useCallback((d) => {
    var R;
    if (d.preventDefault(), d.stopPropagation(), n(!1), a === "uploading") return;
    const g = (R = d.dataTransfer.files) == null ? void 0 : R[0];
    g && f(g);
  }, [a, f]), E = p === "uploading", P = B(fe), N = p === "uploading" ? P["js.uploading"] : P["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: i,
      onDragLeave: h,
      onDrop: b
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
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
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: y,
        disabled: E,
        title: N,
        "aria-label": N
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
}, Ce = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ee = () => {
  const l = L(), r = O(), a = M(), o = !!l.hasData, t = l.dataRevision ?? 0, n = l.fileName ?? "download", c = !!l.clearable, [s, u] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!o || s)) {
      u(!0);
      try {
        const y = r + (r.includes("?") ? "&" : "?") + "rev=" + t, i = await fetch(y);
        if (!i.ok) {
          console.error("[TLDownload] Failed to fetch data:", i.status);
          return;
        }
        const h = await i.blob(), b = URL.createObjectURL(h), E = document.createElement("a");
        E.href = b, E.download = n, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(b);
      } catch (y) {
        console.error("[TLDownload] Fetch error:", y);
      } finally {
        u(!1);
      }
    }
  }, [o, s, r, t, n]), p = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), f = B(Ce);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const k = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
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
      onClick: p,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, ye = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, _e = () => {
  const l = L(), r = F(), [a, o] = e.useState("idle"), [t, n] = e.useState(null), [c, s] = e.useState(!1), u = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), k = e.useRef(null), y = l.error, i = e.useMemo(
    () => {
      var v;
      return !!(window.isSecureContext && ((v = navigator.mediaDevices) != null && v.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((v) => v.stop()), m.current = null), u.current && (u.current.srcObject = null);
  }, []), b = e.useCallback(() => {
    h(), o("idle");
  }, [h]), E = e.useCallback(async () => {
    var v;
    if (a !== "uploading") {
      if (n(null), !i) {
        (v = f.current) == null || v.click();
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = C, o("overlayOpen");
      } catch (C) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", C), n("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, i]), P = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const v = u.current, C = p.current;
    if (!v || !C)
      return;
    C.width = v.videoWidth, C.height = v.videoHeight;
    const j = C.getContext("2d");
    j && (j.drawImage(v, 0, 0), h(), o("uploading"), C.toBlob(async (x) => {
      if (!x) {
        o("idle");
        return;
      }
      const S = new FormData();
      S.append("photo", x, "capture.jpg"), await r(S), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, r, h]), N = e.useCallback(async (v) => {
    var x;
    const C = (x = v.target.files) == null ? void 0 : x[0];
    if (!C) return;
    o("uploading");
    const j = new FormData();
    j.append("photo", C, C.name), await r(j), o("idle"), f.current && (f.current.value = "");
  }, [r]);
  e.useEffect(() => {
    a === "overlayOpen" && u.current && m.current && (u.current.srcObject = m.current);
  }, [a]), e.useEffect(() => {
    var C;
    if (a !== "overlayOpen") return;
    (C = k.current) == null || C.focus();
    const v = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = v;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const v = (C) => {
      C.key === "Escape" && b();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [a, b]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((v) => v.stop()), m.current = null);
  }, []);
  const d = B(ye), g = a === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], R = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && R.push("tlPhotoCapture__cameraBtn--uploading");
  const T = ["tlPhotoCapture__overlayVideo"];
  c && T.push("tlPhotoCapture__overlayVideo--mirrored");
  const w = ["tlPhotoCapture__mirrorBtn"];
  return c && w.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: E,
      disabled: a === "uploading",
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !i && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: N
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: b }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: u,
        className: T.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: w.join(" "),
        onClick: () => s((v) => !v),
        title: d["js.photoCapture.mirror"],
        "aria-label": d["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: P,
        title: d["js.photoCapture.capture"],
        "aria-label": d["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: b,
        title: d["js.photoCapture.close"],
        "aria-label": d["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), t && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, d[t]), y && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, y));
}, ge = {
  "js.photoViewer.alt": "Captured photo"
}, ke = () => {
  const l = L(), r = O(), a = !!l.hasPhoto, o = l.dataRevision ?? 0, [t, n] = e.useState(null), c = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      t && (URL.revokeObjectURL(t), n(null));
      return;
    }
    if (o === c.current && t)
      return;
    c.current = o, t && (URL.revokeObjectURL(t), n(null));
    let u = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        u || n(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      u = !0;
    };
  }, [a, o, r]), e.useEffect(() => () => {
    t && URL.revokeObjectURL(t);
  }, []);
  const s = B(ge);
  return !a || !t ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: t,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Y, useRef: I } = e, we = () => {
  const l = L(), r = M(), a = l.orientation, o = l.resizable === !0, t = l.children ?? [], n = a === "horizontal", c = t.length > 0 && t.every((i) => i.collapsed), s = c ? !n : n, u = I(null), m = I(null), p = I(null), f = Y((i, h) => {
    const b = {
      overflow: i.scrolling || "auto"
    };
    return i.collapsed ? c ? b.flex = "1 0 0%" : b.flex = `0 0 ${i.size}px` : h !== void 0 ? b.flex = `0 0 ${h}px` : i.unit === "%" ? b.flex = `${i.size} 0 0%` : b.flex = `0 0 ${i.size}px`, i.minSize > 0 && !i.collapsed && (b.minWidth = n ? i.minSize : void 0, b.minHeight = n ? void 0 : i.minSize), b;
  }, [n, c]), k = Y((i, h) => {
    i.preventDefault();
    const b = u.current;
    if (!b) return;
    const E = t[h], P = t[h + 1], N = b.querySelectorAll(":scope > .tlSplitPanel__child"), d = [];
    N.forEach((T) => {
      d.push(n ? T.offsetWidth : T.offsetHeight);
    }), p.current = d, m.current = {
      splitterIndex: h,
      startPos: n ? i.clientX : i.clientY,
      startSizeBefore: d[h],
      startSizeAfter: d[h + 1],
      childBefore: E,
      childAfter: P
    };
    const g = (T) => {
      const w = m.current;
      if (!w || !p.current) return;
      const C = (n ? T.clientX : T.clientY) - w.startPos, j = w.childBefore.minSize || 0, x = w.childAfter.minSize || 0;
      let S = w.startSizeBefore + C, z = w.startSizeAfter - C;
      S < j && (z += S - j, S = j), z < x && (S += z - x, z = x), p.current[w.splitterIndex] = S, p.current[w.splitterIndex + 1] = z;
      const $ = b.querySelectorAll(":scope > .tlSplitPanel__child"), V = $[w.splitterIndex], H = $[w.splitterIndex + 1];
      V && (V.style.flex = `0 0 ${S}px`), H && (H.style.flex = `0 0 ${z}px`);
    }, R = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", R), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const T = {};
        t.forEach((w, v) => {
          const C = w.control;
          C != null && C.controlId && p.current && (T[C.controlId] = p.current[v]);
        }), r("updateSizes", { sizes: T });
      }
      p.current = null, m.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", R), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [t, n, r]), y = [];
  return t.forEach((i, h) => {
    if (y.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${h}`,
          className: `tlSplitPanel__child${i.collapsed && s ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: f(i)
        },
        /* @__PURE__ */ e.createElement(D, { control: i.control })
      )
    ), o && h < t.length - 1) {
      const b = t[h + 1];
      !i.collapsed && !b.collapsed && y.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${h}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (P) => k(P, h)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: `tlSplitPanel tlSplitPanel--${a}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: s ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    y
  );
}, { useCallback: A } = e, Le = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Re = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Ne = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Pe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Te = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), xe = () => {
  const l = L(), r = M(), a = B(Le), o = l.title, t = l.expansionState ?? "NORMALIZED", n = l.showMinimize === !0, c = l.showMaximize === !0, s = l.showPopOut === !0, u = l.toolbarButtons ?? [], m = t === "MINIMIZED", p = t === "MAXIMIZED", f = t === "HIDDEN", k = A(() => {
    r("toggleMinimize");
  }, [r]), y = A(() => {
    r("toggleMaximize");
  }, [r]), i = A(() => {
    r("popOut");
  }, [r]);
  if (f)
    return null;
  const h = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${t.toLowerCase()}`,
      style: h
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, u.map((b, E) => /* @__PURE__ */ e.createElement("span", { key: E, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(D, { control: b }))), n && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: m ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Ne, null) : /* @__PURE__ */ e.createElement(Re, null)
    ), c && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: p ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Te, null) : /* @__PURE__ */ e.createElement(Pe, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: i,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(je, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(D, { control: l.child }))
  );
}, Se = () => {
  const l = L();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${l.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(D, { control: l.child })
  );
}, De = () => {
  const l = L();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, l.activeChild && /* @__PURE__ */ e.createElement(D, { control: l.activeChild }));
};
_("TLButton", oe);
_("TLToggleButton", se);
_("TLTextInput", q);
_("TLNumberInput", Z);
_("TLDatePicker", J);
_("TLSelect", ee);
_("TLCheckbox", ae);
_("TLTable", le);
_("TLCounter", ce);
_("TLTabBar", ue);
_("TLFieldList", de);
_("TLAudioRecorder", pe);
_("TLAudioPlayer", be);
_("TLFileUpload", ve);
_("TLDownload", Ee);
_("TLPhotoCapture", _e);
_("TLPhotoViewer", ke);
_("TLSplitPanel", we);
_("TLPanel", xe);
_("TLMaximizeRoot", Se);
_("TLDeckPane", De);
