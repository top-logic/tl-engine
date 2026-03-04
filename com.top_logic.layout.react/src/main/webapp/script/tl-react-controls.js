import { React as e, useTLFieldValue as X, getComponent as Le, useTLState as R, useTLCommand as z, TLChild as P, useTLUpload as ie, useI18N as W, useTLDataUrl as de, register as T } from "tl-react-bridge";
const { useCallback: Te } = e, xe = ({ controlId: a, state: t }) => {
  const [n, r] = X(), s = Te(
    (l) => {
      r(l.target.value);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: a,
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Se } = e, De = ({ controlId: a, state: t, config: n }) => {
  const [r, s] = X(), l = Se(
    (c) => {
      const i = c.target.value, m = i === "" ? null : Number(i);
      s(m);
    },
    [s]
  ), o = n != null && n.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: a,
      value: r != null ? String(r) : "",
      onChange: l,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Re } = e, Pe = ({ controlId: a, state: t }) => {
  const [n, r] = X(), s = Re(
    (l) => {
      r(l.target.value || null);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: a,
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Be } = e, je = ({ controlId: a, state: t, config: n }) => {
  var c;
  const [r, s] = X(), l = Be(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), o = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = o.find((m) => m.value === r)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: a,
      value: r ?? "",
      onChange: l,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: Fe } = e, Me = ({ controlId: a, state: t }) => {
  const [n, r] = X(), s = Fe(
    (l) => {
      r(l.target.checked);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: n === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Ie = ({ controlId: a, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: a, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((s, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, n.map((o) => {
    const c = o.cellModule ? Le(o.cellModule) : void 0, i = s[o.name];
    if (c) {
      const m = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: a + "-" + l + "-" + o.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Ae } = e, ze = ({ controlId: a, command: t, label: n, disabled: r }) => {
  const s = R(), l = z(), o = t ?? "click", c = n ?? s.label, i = r ?? s.disabled === !0, m = Ae(() => {
    l(o);
  }, [l, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: m,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: $e } = e, Ve = ({ controlId: a, command: t, label: n, active: r, disabled: s }) => {
  const l = R(), o = z(), c = t ?? "click", i = n ?? l.label, m = r ?? l.active === !0, _ = s ?? l.disabled === !0, h = $e(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: h,
      disabled: _,
      className: "tlReactButton" + (m ? " tlReactButtonActive" : "")
    },
    i
  );
}, Oe = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ue } = e, We = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.tabs ?? [], s = t.activeTabId, l = Ue((o) => {
    o !== s && n("selectTab", { tabId: o });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === s,
      className: "tlReactTabBar__tab" + (o.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => l(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, Ke = ({ controlId: a }) => {
  const t = R(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((s, l) => /* @__PURE__ */ e.createElement("div", { key: l, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(P, { control: s })))));
}, He = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ge = ({ controlId: a }) => {
  const t = R(), n = ie(), [r, s] = e.useState("idle"), [l, o] = e.useState(null), c = e.useRef(null), i = e.useRef([]), m = e.useRef(null), _ = t.status ?? "idle", h = t.error, f = _ === "received" ? "idle" : r !== "idle" ? r : _, L = e.useCallback(async () => {
    if (r === "recording") {
      const y = c.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (r !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        m.current = y, i.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(y, B ? { mimeType: B } : void 0);
        c.current = j, j.ondataavailable = (d) => {
          d.data.size > 0 && i.current.push(d.data);
        }, j.onstop = async () => {
          y.getTracks().forEach((w) => w.stop()), m.current = null;
          const d = new Blob(i.current, { type: j.mimeType || "audio/webm" });
          if (i.current = [], d.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const E = new FormData();
          E.append("audio", d, "recording.webm"), await n(E), s("idle");
        }, j.start(), s("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), o("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [r, n]), v = W(He), u = f === "recording" ? v["js.audioRecorder.stop"] : f === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], b = f === "uploading", N = ["tlAudioRecorder__button"];
  return f === "recording" && N.push("tlAudioRecorder__button--recording"), f === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: L,
      disabled: b,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[l]), h && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h));
}, Ye = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Xe = ({ controlId: a }) => {
  const t = R(), n = de(), r = !!t.hasAudio, s = t.dataRevision ?? 0, [l, o] = e.useState(r ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), m = e.useRef(s);
  e.useEffect(() => {
    r ? l === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), o("disabled"));
  }, [r]), e.useEffect(() => {
    s !== m.current && (m.current = s, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (l === "playing" || l === "paused" || l === "loading") && o("idle"));
  }, [s]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const _ = e.useCallback(async () => {
    if (l === "disabled" || l === "loading")
      return;
    if (l === "playing") {
      c.current && c.current.pause(), o("paused");
      return;
    }
    if (l === "paused" && c.current) {
      c.current.play(), o("playing");
      return;
    }
    if (!i.current) {
      o("loading");
      try {
        const b = await fetch(n);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), o("idle");
          return;
        }
        const N = await b.blob();
        i.current = URL.createObjectURL(N);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), o("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      o("idle");
    }, u.play(), o("playing");
  }, [l, n]), h = W(Ye), f = l === "loading" ? h["js.loading"] : l === "playing" ? h["js.audioPlayer.pause"] : l === "disabled" ? h["js.audioPlayer.noAudio"] : h["js.audioPlayer.play"], L = l === "disabled" || l === "loading", v = ["tlAudioPlayer__button"];
  return l === "playing" && v.push("tlAudioPlayer__button--playing"), l === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: _,
      disabled: L,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${l === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, qe = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ze = ({ controlId: a }) => {
  const t = R(), n = ie(), [r, s] = e.useState("idle"), [l, o] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", m = t.error, _ = t.accept ?? "", h = i === "received" ? "idle" : r !== "idle" ? r : i, f = e.useCallback(async (d) => {
    s("uploading");
    const E = new FormData();
    E.append("file", d, d.name), await n(E), s("idle");
  }, [n]), L = e.useCallback((d) => {
    var w;
    const E = (w = d.target.files) == null ? void 0 : w[0];
    E && f(E);
  }, [f]), v = e.useCallback(() => {
    var d;
    r !== "uploading" && ((d = c.current) == null || d.click());
  }, [r]), u = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), o(!0);
  }, []), b = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), o(!1);
  }, []), N = e.useCallback((d) => {
    var w;
    if (d.preventDefault(), d.stopPropagation(), o(!1), r === "uploading") return;
    const E = (w = d.dataTransfer.files) == null ? void 0 : w[0];
    E && f(E);
  }, [r, f]), y = h === "uploading", B = W(qe), j = h === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${l ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: b,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: _ || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (h === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: y,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    m && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, m)
  );
}, Je = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Qe = ({ controlId: a }) => {
  const t = R(), n = de(), r = z(), s = !!t.hasData, l = t.dataRevision ?? 0, o = t.fileName ?? "download", c = !!t.clearable, [i, m] = e.useState(!1), _ = e.useCallback(async () => {
    if (!(!s || i)) {
      m(!0);
      try {
        const v = n + (n.includes("?") ? "&" : "?") + "rev=" + l, u = await fetch(v);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const b = await u.blob(), N = URL.createObjectURL(b), y = document.createElement("a");
        y.href = N, y.download = o, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(N);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        m(!1);
      }
    }
  }, [s, i, n, l, o]), h = e.useCallback(async () => {
    s && await r("clear");
  }, [s, r]), f = W(Je);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const L = i ? f["js.downloading"] : f["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: i,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: h,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, et = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, tt = ({ controlId: a }) => {
  const t = R(), n = ie(), [r, s] = e.useState("idle"), [l, o] = e.useState(null), [c, i] = e.useState(!1), m = e.useRef(null), _ = e.useRef(null), h = e.useRef(null), f = e.useRef(null), L = e.useRef(null), v = t.error, u = e.useMemo(
    () => {
      var k;
      return !!(window.isSecureContext && ((k = navigator.mediaDevices) != null && k.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    _.current && (_.current.getTracks().forEach((k) => k.stop()), _.current = null), m.current && (m.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    b(), s("idle");
  }, [b]), y = e.useCallback(async () => {
    var k;
    if (r !== "uploading") {
      if (o(null), !u) {
        (k = f.current) == null || k.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        _.current = D, s("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), o("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [r, u]), B = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const k = m.current, D = h.current;
    if (!k || !D)
      return;
    D.width = k.videoWidth, D.height = k.videoHeight;
    const x = D.getContext("2d");
    x && (x.drawImage(k, 0, 0), b(), s("uploading"), D.toBlob(async (M) => {
      if (!M) {
        s("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", M, "capture.jpg"), await n(H), s("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, b]), j = e.useCallback(async (k) => {
    var M;
    const D = (M = k.target.files) == null ? void 0 : M[0];
    if (!D) return;
    s("uploading");
    const x = new FormData();
    x.append("photo", D, D.name), await n(x), s("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && m.current && _.current && (m.current.srcObject = _.current);
  }, [r]), e.useEffect(() => {
    var D;
    if (r !== "overlayOpen") return;
    (D = L.current) == null || D.focus();
    const k = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = k;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const k = (D) => {
      D.key === "Escape" && N();
    };
    return document.addEventListener("keydown", k), () => document.removeEventListener("keydown", k);
  }, [r, N]), e.useEffect(() => () => {
    _.current && (_.current.getTracks().forEach((k) => k.stop()), _.current = null);
  }, []);
  const d = W(et), E = r === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], w = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && w.push("tlPhotoCapture__cameraBtn--uploading");
  const A = ["tlPhotoCapture__overlayVideo"];
  c && A.push("tlPhotoCapture__overlayVideo--mirrored");
  const g = ["tlPhotoCapture__mirrorBtn"];
  return c && g.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: y,
      disabled: r === "uploading",
      title: E,
      "aria-label": E
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
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: h, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: m,
        className: A.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: g.join(" "),
        onClick: () => i((k) => !k),
        title: d["js.photoCapture.mirror"],
        "aria-label": d["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: d["js.photoCapture.capture"],
        "aria-label": d["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: d["js.photoCapture.close"],
        "aria-label": d["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, d[l]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, nt = {
  "js.photoViewer.alt": "Captured photo"
}, at = ({ controlId: a }) => {
  const t = R(), n = de(), r = !!t.hasPhoto, s = t.dataRevision ?? 0, [l, o] = e.useState(null), c = e.useRef(s);
  e.useEffect(() => {
    if (!r) {
      l && (URL.revokeObjectURL(l), o(null));
      return;
    }
    if (s === c.current && l)
      return;
    c.current = s, l && (URL.revokeObjectURL(l), o(null));
    let m = !1;
    return (async () => {
      try {
        const _ = await fetch(n);
        if (!_.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", _.status);
          return;
        }
        const h = await _.blob();
        m || o(URL.createObjectURL(h));
      } catch (_) {
        console.error("[TLPhotoViewer] Fetch error:", _);
      }
    })(), () => {
      m = !0;
    };
  }, [r, s, n]), e.useEffect(() => () => {
    l && URL.revokeObjectURL(l);
  }, []);
  const i = W(nt);
  return !r || !l ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: l,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: me, useRef: ne } = e, lt = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.orientation, s = t.resizable === !0, l = t.children ?? [], o = r === "horizontal", c = l.length > 0 && l.every((b) => b.collapsed), i = !c && l.some((b) => b.collapsed), m = c ? !o : o, _ = ne(null), h = ne(null), f = ne(null), L = me((b, N) => {
    const y = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? c && !m ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : N !== void 0 ? y.flex = `0 0 ${N}px` : b.unit === "%" || i ? y.flex = `${b.size} 0 0%` : y.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (y.minWidth = o ? b.minSize : void 0, y.minHeight = o ? void 0 : b.minSize), y;
  }, [o, c, i, m]), v = me((b, N) => {
    b.preventDefault();
    const y = _.current;
    if (!y) return;
    const B = l[N], j = l[N + 1], d = y.querySelectorAll(":scope > .tlSplitPanel__child"), E = [];
    d.forEach((g) => {
      E.push(o ? g.offsetWidth : g.offsetHeight);
    }), f.current = E, h.current = {
      splitterIndex: N,
      startPos: o ? b.clientX : b.clientY,
      startSizeBefore: E[N],
      startSizeAfter: E[N + 1],
      childBefore: B,
      childAfter: j
    };
    const w = (g) => {
      const k = h.current;
      if (!k || !f.current) return;
      const x = (o ? g.clientX : g.clientY) - k.startPos, M = k.childBefore.minSize || 0, H = k.childAfter.minSize || 0;
      let G = k.startSizeBefore + x, K = k.startSizeAfter - x;
      G < M && (K += G - M, G = M), K < H && (G += K - H, K = H), f.current[k.splitterIndex] = G, f.current[k.splitterIndex + 1] = K;
      const q = y.querySelectorAll(":scope > .tlSplitPanel__child"), Z = q[k.splitterIndex], J = q[k.splitterIndex + 1];
      Z && (Z.style.flex = `0 0 ${G}px`), J && (J.style.flex = `0 0 ${K}px`);
    }, A = () => {
      if (document.removeEventListener("mousemove", w), document.removeEventListener("mouseup", A), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const g = {};
        l.forEach((k, D) => {
          const x = k.control;
          x != null && x.controlId && f.current && (g[x.controlId] = f.current[D]);
        }), n("updateSizes", { sizes: g });
      }
      f.current = null, h.current = null;
    };
    document.addEventListener("mousemove", w), document.addEventListener("mouseup", A), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [l, o, n]), u = [];
  return l.forEach((b, N) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${b.collapsed && m ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(b)
        },
        /* @__PURE__ */ e.createElement(P, { control: b.control })
      )
    ), s && N < l.length - 1) {
      const y = l[N + 1];
      !b.collapsed && !y.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (j) => v(j, N)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: _,
      id: a,
      className: `tlSplitPanel tlSplitPanel--${r}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: m ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    u
  );
}, { useCallback: ae } = e, rt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ot = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), st = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ct = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), it = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), dt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ut = ({ controlId: a }) => {
  const t = R(), n = z(), r = W(rt), s = t.title, l = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, m = t.toolbarButtons ?? [], _ = l === "MINIMIZED", h = l === "MAXIMIZED", f = l === "HIDDEN", L = ae(() => {
    n("toggleMinimize");
  }, [n]), v = ae(() => {
    n("toggleMaximize");
  }, [n]), u = ae(() => {
    n("popOut");
  }, [n]);
  if (f)
    return null;
  const b = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${l.toLowerCase()}`,
      style: b
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((N, y) => /* @__PURE__ */ e.createElement("span", { key: y, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(P, { control: N }))), o && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: _ ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      _ ? /* @__PURE__ */ e.createElement(st, null) : /* @__PURE__ */ e.createElement(ot, null)
    ), c && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: h ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(it, null) : /* @__PURE__ */ e.createElement(ct, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(dt, null)
    ))),
    !_ && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(P, { control: t.child }))
  );
}, mt = ({ controlId: a }) => {
  const t = R();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(P, { control: t.child })
  );
}, pt = ({ controlId: a }) => {
  const t = R();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(P, { control: t.activeChild }));
}, { useCallback: U, useState: le, useEffect: Q, useRef: se } = e, ft = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ce(a, t, n, r) {
  const s = [];
  for (const l of a)
    l.type === "nav" ? s.push({ id: l.id, type: "nav", groupId: r }) : l.type === "command" ? s.push({ id: l.id, type: "command", groupId: r }) : l.type === "group" && (s.push({ id: l.id, type: "group" }), (n.get(l.id) ?? l.expanded) && !t && s.push(...ce(l.children, t, n, l.id)));
  return s;
}
const Y = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, bt = ({ item: a, active: t, collapsed: n, onSelect: r, tabIndex: s, itemRef: l, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(a.id),
    title: n ? a.label : void 0,
    tabIndex: s,
    ref: l,
    onFocus: () => o(a.id)
  },
  n && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Y, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(Y, { icon: a.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
  !n && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
), ht = ({ item: a, collapsed: t, onExecute: n, tabIndex: r, itemRef: s, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(a.id),
    title: t ? a.label : void 0,
    tabIndex: r,
    ref: s,
    onFocus: () => l(a.id)
  },
  /* @__PURE__ */ e.createElement(Y, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), _t = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(Y, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), Et = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), vt = ({ item: a, activeItemId: t, onSelect: n, onExecute: r, onClose: s }) => {
  const l = se(null);
  Q(() => {
    const c = (i) => {
      l.current && !l.current.contains(i.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), Q(() => {
    const c = (i) => {
      i.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const o = U((c) => {
    c.type === "nav" ? (n(c.id), s()) : c.type === "command" && (r(c.id), s());
  }, [n, r, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: l, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, a.label), a.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => o(c)
        },
        /* @__PURE__ */ e.createElement(Y, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, gt = ({
  item: a,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: s,
  onExecute: l,
  onToggleGroup: o,
  tabIndex: c,
  itemRef: i,
  onFocus: m,
  focusedId: _,
  setItemRef: h,
  onItemFocus: f,
  flyoutGroupId: L,
  onOpenFlyout: v,
  onCloseFlyout: u
}) => {
  const b = U(() => {
    r ? L === a.id ? u() : v(a.id) : o(a.id);
  }, [r, L, a.id, o, v, u]), N = r && L === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (N ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: b,
      title: r ? a.label : void 0,
      "aria-expanded": r ? N : t,
      tabIndex: c,
      ref: i,
      onFocus: () => m(a.id)
    },
    /* @__PURE__ */ e.createElement(Y, { icon: a.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
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
  ), N && /* @__PURE__ */ e.createElement(
    vt,
    {
      item: a,
      activeItemId: n,
      onSelect: s,
      onExecute: l,
      onClose: u
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, a.children.map((y) => /* @__PURE__ */ e.createElement(
    Ee,
    {
      key: y.id,
      item: y,
      activeItemId: n,
      collapsed: r,
      onSelect: s,
      onExecute: l,
      onToggleGroup: o,
      focusedId: _,
      setItemRef: h,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: v,
      onCloseFlyout: u
    }
  ))));
}, Ee = ({
  item: a,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: s,
  onToggleGroup: l,
  focusedId: o,
  setItemRef: c,
  onItemFocus: i,
  groupStates: m,
  flyoutGroupId: _,
  onOpenFlyout: h,
  onCloseFlyout: f
}) => {
  switch (a.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        bt,
        {
          item: a,
          active: a.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: o === a.id ? 0 : -1,
          itemRef: c(a.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ht,
        {
          item: a,
          collapsed: n,
          onExecute: s,
          tabIndex: o === a.id ? 0 : -1,
          itemRef: c(a.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(_t, { item: a, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Et, null);
    case "group": {
      const L = m ? m.get(a.id) ?? a.expanded : a.expanded;
      return /* @__PURE__ */ e.createElement(
        gt,
        {
          item: a,
          expanded: L,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: s,
          onToggleGroup: l,
          tabIndex: o === a.id ? 0 : -1,
          itemRef: c(a.id),
          onFocus: i,
          focusedId: o,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: _,
          onOpenFlyout: h,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, Ct = ({ controlId: a }) => {
  const t = R(), n = z(), r = W(ft), s = t.items ?? [], l = t.activeItemId, o = t.collapsed, [c, i] = le(() => {
    const g = /* @__PURE__ */ new Map(), k = (D) => {
      for (const x of D)
        x.type === "group" && (g.set(x.id, x.expanded), k(x.children));
    };
    return k(s), g;
  }), m = U((g) => {
    i((k) => {
      const D = new Map(k), x = D.get(g) ?? !1;
      return D.set(g, !x), n("toggleGroup", { itemId: g, expanded: !x }), D;
    });
  }, [n]), _ = U((g) => {
    g !== l && n("selectItem", { itemId: g });
  }, [n, l]), h = U((g) => {
    n("executeCommand", { itemId: g });
  }, [n]), f = U(() => {
    n("toggleCollapse", {});
  }, [n]), [L, v] = le(null), u = U((g) => {
    v(g);
  }, []), b = U(() => {
    v(null);
  }, []);
  Q(() => {
    o || v(null);
  }, [o]);
  const [N, y] = le(() => {
    const g = ce(s, o, c);
    return g.length > 0 ? g[0].id : "";
  }), B = se(/* @__PURE__ */ new Map()), j = U((g) => (k) => {
    k ? B.current.set(g, k) : B.current.delete(g);
  }, []), d = U((g) => {
    y(g);
  }, []), E = se(0), w = U((g) => {
    y(g), E.current++;
  }, []);
  Q(() => {
    const g = B.current.get(N);
    g && document.activeElement !== g && g.focus();
  }, [N, E.current]);
  const A = U((g) => {
    if (g.key === "Escape" && L !== null) {
      g.preventDefault(), b();
      return;
    }
    const k = ce(s, o, c);
    if (k.length === 0) return;
    const D = k.findIndex((M) => M.id === N);
    if (D < 0) return;
    const x = k[D];
    switch (g.key) {
      case "ArrowDown": {
        g.preventDefault();
        const M = (D + 1) % k.length;
        w(k[M].id);
        break;
      }
      case "ArrowUp": {
        g.preventDefault();
        const M = (D - 1 + k.length) % k.length;
        w(k[M].id);
        break;
      }
      case "Home": {
        g.preventDefault(), w(k[0].id);
        break;
      }
      case "End": {
        g.preventDefault(), w(k[k.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        g.preventDefault(), x.type === "nav" ? _(x.id) : x.type === "command" ? h(x.id) : x.type === "group" && (o ? L === x.id ? b() : u(x.id) : m(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !o && ((c.get(x.id) ?? !1) || (g.preventDefault(), m(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !o && (c.get(x.id) ?? !1) && (g.preventDefault(), m(x.id));
        break;
      }
    }
  }, [
    s,
    o,
    c,
    N,
    L,
    w,
    _,
    h,
    m,
    u,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: A }, s.map((g) => /* @__PURE__ */ e.createElement(
    Ee,
    {
      key: g.id,
      item: g,
      activeItemId: l,
      collapsed: o,
      onSelect: _,
      onExecute: h,
      onToggleGroup: m,
      focusedId: N,
      setItemRef: j,
      onItemFocus: d,
      groupStates: c,
      flyoutGroupId: L,
      onOpenFlyout: u,
      onCloseFlyout: b
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, yt = ({ controlId: a }) => {
  const t = R(), n = t.direction ?? "column", r = t.gap ?? "default", s = t.align ?? "stretch", l = t.wrap === !0, o = t.children ?? [], c = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${s}`,
    l ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: c }, o.map((i, m) => /* @__PURE__ */ e.createElement(P, { key: m, control: i })));
}, kt = ({ controlId: a }) => {
  const t = R(), n = t.columns, r = t.minColumnWidth, s = t.gap ?? "default", l = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (o.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${s}`, style: o }, l.map((c, i) => /* @__PURE__ */ e.createElement(P, { key: i, control: c })));
}, wt = ({ controlId: a }) => {
  const t = R(), n = t.title, r = t.variant ?? "outlined", s = t.padding ?? "default", l = t.headerActions ?? [], o = t.child, c = n != null || l.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${r}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), l.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, l.map((i, m) => /* @__PURE__ */ e.createElement(P, { key: m, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(P, { control: o })));
}, Nt = ({ controlId: a }) => {
  const t = R(), n = t.title ?? "", r = t.leading, s = t.actions ?? [], l = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    l === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: c }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(P, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, m) => /* @__PURE__ */ e.createElement(P, { key: m, control: i }))));
}, { useCallback: Lt } = e, Tt = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.items ?? [], s = Lt((l) => {
    n("navigate", { itemId: l });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((l, o) => {
    const c = o === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: l.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, l.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => s(l.id)
      },
      l.label
    ));
  })));
}, { useCallback: xt } = e, St = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.items ?? [], s = t.activeItemId, l = xt((o) => {
    o !== s && n("selectItem", { itemId: o });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((o) => {
    const c = o.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => l(o.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: pe, useEffect: fe, useRef: Dt } = e, Rt = {
  "js.dialog.close": "Close"
}, Pt = ({ controlId: a }) => {
  const t = R(), n = z(), r = W(Rt), s = t.open === !0, l = t.title ?? "", o = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], m = t.child, _ = Dt(null), h = pe(() => {
    n("close");
  }, [n]), f = pe((v) => {
    c && v.target === v.currentTarget && h();
  }, [c, h]);
  if (fe(() => {
    if (!s) return;
    const v = (u) => {
      u.key === "Escape" && h();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [s, h]), fe(() => {
    s && _.current && _.current.focus();
  }, [s]), !s) return null;
  const L = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDialog__backdrop", onClick: f }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": L,
      ref: _,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: L }, l), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: h,
        title: r["js.dialog.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(P, { control: m })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((v, u) => /* @__PURE__ */ e.createElement(P, { key: u, control: v })))
  ));
}, { useCallback: Bt, useEffect: jt } = e, Ft = {
  "js.drawer.close": "Close"
}, Mt = ({ controlId: a }) => {
  const t = R(), n = z(), r = W(Ft), s = t.open === !0, l = t.position ?? "right", o = t.size ?? "medium", c = t.title ?? null, i = t.child, m = Bt(() => {
    n("close");
  }, [n]);
  jt(() => {
    if (!s) return;
    const h = (f) => {
      f.key === "Escape" && m();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [s, m]);
  const _ = [
    "tlDrawer",
    `tlDrawer--${l}`,
    `tlDrawer--${o}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: a, className: _, "aria-hidden": !s }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: m,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(P, { control: i })));
}, { useCallback: be, useEffect: It, useState: At } = e, zt = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.message ?? "", s = t.variant ?? "info", l = t.action, o = t.duration ?? 5e3, c = t.visible === !0, [i, m] = At(!1), _ = be(() => {
    m(!0), setTimeout(() => {
      n("dismiss"), m(!1);
    }, 200);
  }, [n]), h = be(() => {
    l && n(l.commandName), _();
  }, [n, l, _]);
  return It(() => {
    if (!c || o === 0) return;
    const f = setTimeout(_, o);
    return () => clearTimeout(f);
  }, [c, o, _]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    l && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: h }, l.label)
  );
}, { useCallback: re, useEffect: oe, useRef: $t, useState: he } = e, Vt = ({ controlId: a }) => {
  const t = R(), n = z(), r = t.open === !0, s = t.anchorId, l = t.items ?? [], o = $t(null), [c, i] = he({ top: 0, left: 0 }), [m, _] = he(0), h = l.filter((u) => u.type === "item" && !u.disabled);
  oe(() => {
    var d, E;
    if (!r || !s) return;
    const u = document.getElementById(s);
    if (!u) return;
    const b = u.getBoundingClientRect(), N = ((d = o.current) == null ? void 0 : d.offsetHeight) ?? 200, y = ((E = o.current) == null ? void 0 : E.offsetWidth) ?? 200;
    let B = b.bottom + 4, j = b.left;
    B + N > window.innerHeight && (B = b.top - N - 4), j + y > window.innerWidth && (j = b.right - y), i({ top: B, left: j }), _(0);
  }, [r, s]);
  const f = re(() => {
    n("close");
  }, [n]), L = re((u) => {
    n("selectItem", { itemId: u });
  }, [n]);
  oe(() => {
    if (!r) return;
    const u = (b) => {
      o.current && !o.current.contains(b.target) && f();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [r, f]);
  const v = re((u) => {
    if (u.key === "Escape") {
      f();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), _((b) => (b + 1) % h.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), _((b) => (b - 1 + h.length) % h.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const b = h[m];
      b && L(b.id);
    }
  }, [f, L, h, m]);
  return oe(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: v
    },
    l.map((u, b) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
      const y = h.indexOf(u) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (y ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: y ? 0 : -1,
          onClick: () => L(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, Ot = ({ controlId: a }) => {
  const t = R(), n = t.header, r = t.content, s = t.footer, l = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(P, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(P, { control: r })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(P, { control: s })), /* @__PURE__ */ e.createElement(P, { control: l }));
}, Ut = () => {
  const t = R().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, _e = 50, Wt = () => {
  const a = R(), t = z(), n = a.columns ?? [], r = a.totalRowCount ?? 0, s = a.rows ?? [], l = a.rowHeight ?? 36, o = a.selectionMode ?? "single", c = a.selectedCount ?? 0, i = a.frozenColumnCount ?? 0, m = a.treeMode ?? !1, _ = e.useMemo(
    () => n.filter((p) => p.sortPriority && p.sortPriority > 0).length,
    [n]
  ), h = o === "multi", f = 40, L = 20, v = e.useRef(null), u = e.useRef(null), b = e.useRef(null), [N, y] = e.useState({}), B = e.useRef(null), j = e.useRef(!1), d = e.useRef(null), [E, w] = e.useState(null);
  e.useEffect(() => {
    B.current || y({});
  }, [n]);
  const A = e.useCallback((p) => N[p.name] ?? p.width, [N]), g = e.useMemo(() => {
    const p = [];
    let C = h && i > 0 ? f : 0;
    for (let S = 0; S < i && S < n.length; S++)
      p.push(C), C += A(n[S]);
    return p;
  }, [n, i, h, f, A]), k = r * l, D = e.useCallback((p, C, S) => {
    S.preventDefault(), S.stopPropagation(), B.current = { column: p, startX: S.clientX, startWidth: C };
    const F = ($) => {
      const O = B.current;
      if (!O) return;
      const V = Math.max(_e, O.startWidth + ($.clientX - O.startX));
      y((te) => ({ ...te, [O.column]: V }));
    }, I = ($) => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", I);
      const O = B.current;
      if (O) {
        const V = Math.max(_e, O.startWidth + ($.clientX - O.startX));
        t("columnResize", { column: O.column, width: V }), B.current = null, j.current = !0, requestAnimationFrame(() => {
          j.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", I);
  }, [t]), x = e.useCallback(() => {
    v.current && u.current && (v.current.scrollLeft = u.current.scrollLeft), b.current !== null && clearTimeout(b.current), b.current = window.setTimeout(() => {
      const p = u.current;
      if (!p) return;
      const C = p.scrollTop, S = Math.ceil(p.clientHeight / l), F = Math.floor(C / l);
      t("scroll", { start: F, count: S });
    }, 80);
  }, [t, l]), M = e.useCallback((p, C, S) => {
    if (j.current) return;
    let F;
    !C || C === "desc" ? F = "asc" : F = "desc";
    const I = S.shiftKey ? "add" : "replace";
    t("sort", { column: p, direction: F, mode: I });
  }, [t]), H = e.useCallback((p, C) => {
    d.current = p, C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", p);
  }, []), G = e.useCallback((p, C) => {
    if (!d.current || d.current === p) {
      w(null);
      return;
    }
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const S = C.currentTarget.getBoundingClientRect(), F = C.clientX < S.left + S.width / 2 ? "left" : "right";
    w({ column: p, side: F });
  }, []), K = e.useCallback((p) => {
    p.preventDefault(), p.stopPropagation();
    const C = d.current;
    if (!C || !E) {
      d.current = null, w(null);
      return;
    }
    let S = n.findIndex((I) => I.name === E.column);
    if (S < 0) {
      d.current = null, w(null);
      return;
    }
    const F = n.findIndex((I) => I.name === C);
    E.side === "right" && S++, F < S && S--, t("columnReorder", { column: C, targetIndex: S }), d.current = null, w(null);
  }, [n, E, t]), q = e.useCallback(() => {
    d.current = null, w(null);
  }, []), Z = e.useCallback((p, C) => {
    C.shiftKey && C.preventDefault(), t("select", {
      rowIndex: p,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), J = e.useCallback((p, C) => {
    C.stopPropagation(), t("select", { rowIndex: p, ctrlKey: !0, shiftKey: !1 });
  }, [t]), ge = e.useCallback(() => {
    const p = c === r && r > 0;
    t("selectAll", { selected: !p });
  }, [t, c, r]), Ce = e.useCallback((p, C, S) => {
    S.stopPropagation(), t("expand", { rowIndex: p, expanded: C });
  }, [t]), ee = n.reduce((p, C) => p + A(C), 0) + (h ? f : 0), ye = c === r && r > 0, ue = c > 0 && c < r, ke = e.useCallback((p) => {
    p && (p.indeterminate = ue);
  }, [ue]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (p) => {
        if (!d.current) return;
        p.preventDefault();
        const C = u.current, S = v.current;
        if (!C) return;
        const F = C.getBoundingClientRect(), I = 40, $ = 8;
        p.clientX < F.left + I ? C.scrollLeft = Math.max(0, C.scrollLeft - $) : p.clientX > F.right - I && (C.scrollLeft += $), S && (S.scrollLeft = C.scrollLeft);
      },
      onDrop: K
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ee } }, h && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: f,
          minWidth: f,
          ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (p) => {
          d.current && (p.preventDefault(), p.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== d.current && w({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ke,
          className: "tlTableView__checkbox",
          checked: ye,
          onChange: ge
        }
      )
    ), n.map((p, C) => {
      const S = A(p), F = C === n.length - 1;
      let I = "tlTableView__headerCell";
      p.sortable && (I += " tlTableView__headerCell--sortable"), E && E.column === p.name && (I += " tlTableView__headerCell--dragOver-" + E.side);
      const $ = C < i, O = C === i - 1;
      return $ && (I += " tlTableView__headerCell--frozen"), O && (I += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: p.name,
          className: I,
          style: {
            ...F && !$ ? { flex: "1 0 auto", minWidth: S } : { width: S, minWidth: S },
            position: $ ? "sticky" : "relative",
            ...$ ? { left: g[C], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: p.sortable ? (V) => M(p.name, p.sortDirection, V) : void 0,
          onDragStart: (V) => H(p.name, V),
          onDragOver: (V) => G(p.name, V),
          onDrop: K,
          onDragEnd: q
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, p.label),
        p.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, p.sortDirection === "asc" ? "▲" : "▼", _ > 1 && p.sortPriority != null && p.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, p.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (V) => D(p.name, S, V)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (p) => {
          if (d.current && n.length > 0) {
            const C = n[n.length - 1];
            C.name !== d.current && (p.preventDefault(), p.dataTransfer.dropEffect = "move", w({ column: C.name, side: "right" }));
          }
        },
        onDrop: K
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: u,
        className: "tlTableView__body",
        onScroll: x
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: k, position: "relative", minWidth: ee } }, s.map((p) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: p.id,
          className: "tlTableView__row" + (p.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: p.index * l,
            height: l,
            minWidth: ee,
            width: "100%"
          },
          onClick: (C) => Z(p.index, C)
        },
        h && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: f,
              minWidth: f,
              ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (C) => C.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: p.selected,
              onChange: () => {
              },
              onClick: (C) => J(p.index, C),
              tabIndex: -1
            }
          )
        ),
        n.map((C, S) => {
          const F = A(C), I = S === n.length - 1, $ = S < i, O = S === i - 1;
          let V = "tlTableView__cell";
          $ && (V += " tlTableView__cell--frozen"), O && (V += " tlTableView__cell--frozenLast");
          const te = m && S === 0, we = p.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: C.name,
              className: V,
              style: {
                ...I && !$ ? { flex: "1 0 auto", minWidth: F } : { width: F, minWidth: F },
                ...$ ? { position: "sticky", left: g[S], zIndex: 2 } : {}
              }
            },
            te ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: we * L } }, p.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (Ne) => Ce(p.index, !p.expanded, Ne)
              },
              p.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(P, { control: p.cells[C.name] })) : /* @__PURE__ */ e.createElement(P, { control: p.cells[C.name] })
          );
        })
      )))
    )
  );
}, Kt = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ve = e.createContext(Kt), { useMemo: Ht, useRef: Gt, useState: Yt, useEffect: Xt } = e, qt = 320, Zt = ({ controlId: a }) => {
  const t = R(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", s = t.readOnly === !0, l = t.children ?? [], o = Gt(null), [c, i] = Yt(
    r === "top" ? "top" : "side"
  );
  Xt(() => {
    if (r !== "auto") {
      i(r);
      return;
    }
    const L = o.current;
    if (!L) return;
    const v = new ResizeObserver((u) => {
      for (const b of u) {
        const y = b.contentRect.width / n;
        i(y < qt ? "top" : "side");
      }
    });
    return v.observe(L), () => v.disconnect();
  }, [r, n]);
  const m = Ht(() => ({
    readOnly: s,
    resolvedLabelPosition: c
  }), [s, c]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, f = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ve.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: a, className: f, style: h, ref: o }, l.map((L, v) => /* @__PURE__ */ e.createElement(P, { key: v, control: L }))));
}, { useCallback: Jt } = e, Qt = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, en = ({ controlId: a }) => {
  const t = R(), n = z(), r = W(Qt), s = t.header, l = t.headerActions ?? [], o = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", m = t.fullLine === !0, _ = t.children ?? [], h = s != null || l.length > 0 || o, f = Jt(() => {
    n("toggleCollapse");
  }, [n]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    m ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: L }, h && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
      "aria-expanded": !c,
      title: c ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), l.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, l.map((v, u) => /* @__PURE__ */ e.createElement(P, { key: u, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, _.map((v, u) => /* @__PURE__ */ e.createElement(P, { key: u, control: v }))));
}, { useContext: tn, useState: nn, useCallback: an } = e, ln = ({ controlId: a }) => {
  const t = R(), n = tn(ve), r = t.label ?? "", s = t.required === !0, l = t.error, o = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, _ = t.visible !== !1, h = t.field, f = n.readOnly, [L, v] = nn(!1), u = an(() => v((y) => !y), []);
  if (!_) return null;
  const b = l != null, N = [
    "tlFormField",
    `tlFormField--${i}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: N }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), s && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !f && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(P, { control: h })), !f && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, l)), !f && o && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, rn = () => {
  const a = R(), t = z(), n = a.iconCss, r = a.iconSrc, s = a.label, l = a.cssClass, o = a.tooltip, c = a.hasLink, i = n ? /* @__PURE__ */ e.createElement("i", { className: n }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, i, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), _ = e.useCallback((f) => {
    f.preventDefault(), t("goto", {});
  }, [t]), h = ["tlResourceCell", l].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("a", { className: h, href: "#", onClick: _, title: o }, m) : /* @__PURE__ */ e.createElement("span", { className: h, title: o }, m);
}, on = 20, sn = () => {
  const a = R(), t = z(), n = a.nodes ?? [], r = a.selectionMode ?? "single", s = a.dragEnabled ?? !1, l = a.dropEnabled ?? !1, o = a.dropIndicatorNodeId ?? null, c = a.dropIndicatorPosition ?? null, [i, m] = e.useState(-1), _ = e.useRef(null), h = e.useCallback((d, E) => {
    t(E ? "collapse" : "expand", { nodeId: d });
  }, [t]), f = e.useCallback((d, E) => {
    t("select", {
      nodeId: d,
      ctrlKey: E.ctrlKey || E.metaKey,
      shiftKey: E.shiftKey
    });
  }, [t]), L = e.useCallback((d, E) => {
    E.preventDefault(), t("contextMenu", { nodeId: d, x: E.clientX, y: E.clientY });
  }, [t]), v = e.useRef(null), u = e.useCallback((d, E) => {
    const w = E.getBoundingClientRect(), A = d.clientY - w.top, g = w.height / 3;
    return A < g ? "above" : A > g * 2 ? "below" : "within";
  }, []), b = e.useCallback((d, E) => {
    E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", d);
  }, []), N = e.useCallback((d, E) => {
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const w = u(E, E.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: d, position: w }), v.current = null;
    }, 50);
  }, [t, u]), y = e.useCallback((d, E) => {
    E.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const w = u(E, E.currentTarget);
    t("drop", { nodeId: d, position: w });
  }, [t, u]), B = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((d) => {
    if (n.length === 0) return;
    let E = i;
    switch (d.key) {
      case "ArrowDown":
        d.preventDefault(), E = Math.min(i + 1, n.length - 1);
        break;
      case "ArrowUp":
        d.preventDefault(), E = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (d.preventDefault(), i >= 0 && i < n.length) {
          const w = n[i];
          if (w.expandable && !w.expanded) {
            t("expand", { nodeId: w.id });
            return;
          } else w.expanded && (E = i + 1);
        }
        break;
      case "ArrowLeft":
        if (d.preventDefault(), i >= 0 && i < n.length) {
          const w = n[i];
          if (w.expanded) {
            t("collapse", { nodeId: w.id });
            return;
          } else {
            const A = w.depth;
            for (let g = i - 1; g >= 0; g--)
              if (n[g].depth < A) {
                E = g;
                break;
              }
          }
        }
        break;
      case "Enter":
        d.preventDefault(), i >= 0 && i < n.length && t("select", {
          nodeId: n[i].id,
          ctrlKey: d.ctrlKey || d.metaKey,
          shiftKey: d.shiftKey
        });
        return;
      case " ":
        d.preventDefault(), r === "multi" && i >= 0 && i < n.length && t("select", {
          nodeId: n[i].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        d.preventDefault(), E = 0;
        break;
      case "End":
        d.preventDefault(), E = n.length - 1;
        break;
      default:
        return;
    }
    E !== i && m(E);
  }, [i, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: _,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    n.map((d, E) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: d.id,
        role: "treeitem",
        "aria-expanded": d.expandable ? d.expanded : void 0,
        "aria-selected": d.selected,
        "aria-level": d.depth + 1,
        className: [
          "tlTreeView__node",
          d.selected ? "tlTreeView__node--selected" : "",
          E === i ? "tlTreeView__node--focused" : "",
          o === d.id && c === "above" ? "tlTreeView__node--drop-above" : "",
          o === d.id && c === "within" ? "tlTreeView__node--drop-within" : "",
          o === d.id && c === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: d.depth * on },
        draggable: s,
        onClick: (w) => f(d.id, w),
        onContextMenu: (w) => L(d.id, w),
        onDragStart: (w) => b(d.id, w),
        onDragOver: l ? (w) => N(d.id, w) : void 0,
        onDrop: l ? (w) => y(d.id, w) : void 0,
        onDragEnd: B
      },
      d.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (w) => {
            w.stopPropagation(), h(d.id, d.expanded);
          },
          tabIndex: -1,
          "aria-label": d.expanded ? "Collapse" : "Expand"
        },
        d.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: d.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(P, { control: d.content }))
    ))
  );
};
T("TLButton", ze);
T("TLToggleButton", Ve);
T("TLTextInput", xe);
T("TLNumberInput", De);
T("TLDatePicker", Pe);
T("TLSelect", je);
T("TLCheckbox", Me);
T("TLTable", Ie);
T("TLCounter", Oe);
T("TLTabBar", We);
T("TLFieldList", Ke);
T("TLAudioRecorder", Ge);
T("TLAudioPlayer", Xe);
T("TLFileUpload", Ze);
T("TLDownload", Qe);
T("TLPhotoCapture", tt);
T("TLPhotoViewer", at);
T("TLSplitPanel", lt);
T("TLPanel", ut);
T("TLMaximizeRoot", mt);
T("TLDeckPane", pt);
T("TLSidebar", Ct);
T("TLStack", yt);
T("TLGrid", kt);
T("TLCard", wt);
T("TLAppBar", Nt);
T("TLBreadcrumb", Tt);
T("TLBottomBar", St);
T("TLDialog", Pt);
T("TLDrawer", Mt);
T("TLSnackbar", zt);
T("TLMenu", Vt);
T("TLAppShell", Ot);
T("TLTextCell", Ut);
T("TLTableView", Wt);
T("TLFormLayout", Zt);
T("TLFormGroup", en);
T("TLFormField", ln);
T("TLResourceCell", rn);
T("TLTreeView", sn);
