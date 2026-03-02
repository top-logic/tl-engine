import { React as e, useTLFieldValue as z, getComponent as Y, useTLState as w, useTLCommand as D, TLChild as S, useTLUpload as I, useI18N as x, useTLDataUrl as F, register as L } from "tl-react-bridge";
const { useCallback: K } = e, W = ({ state: n }) => {
  const [r, t] = z(), o = K(
    (a) => {
      t(a.target.value);
    },
    [t]
  );
  return n.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: o,
      disabled: n.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: H } = e, q = ({ state: n, config: r }) => {
  const [t, o] = z(), a = H(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      o(i);
    },
    [o]
  ), l = r != null && r.decimal ? "0.01" : "1";
  return n.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, t != null ? String(t) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: t != null ? String(t) : "",
      onChange: a,
      step: l,
      disabled: n.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: X } = e, Z = ({ state: n }) => {
  const [r, t] = z(), o = X(
    (a) => {
      t(a.target.value || null);
    },
    [t]
  );
  return n.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: o,
      disabled: n.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: G } = e, J = ({ state: n, config: r }) => {
  var c;
  const [t, o] = z(), a = G(
    (s) => {
      o(s.target.value || null);
    },
    [o]
  ), l = n.options ?? (r == null ? void 0 : r.options) ?? [];
  if (n.editable === !1) {
    const s = ((c = l.find((i) => i.value === t)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: t ?? "",
      onChange: a,
      disabled: n.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: Q } = e, ee = ({ state: n }) => {
  const [r, t] = z(), o = Q(
    (a) => {
      t(a.target.checked);
    },
    [t]
  );
  return n.editable === !1 ? /* @__PURE__ */ e.createElement(
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
      disabled: n.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, te = ({ controlId: n, state: r }) => {
  const t = r.columns ?? [], o = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, t.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((a, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, t.map((c) => {
    const s = c.cellModule ? Y(c.cellModule) : void 0, i = a[c.name];
    if (s) {
      const d = { value: i, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: n + "-" + l + "-" + c.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: ae } = e, ne = ({ command: n, label: r, disabled: t }) => {
  const o = w(), a = D(), l = n ?? "click", c = r ?? o.label, s = t ?? o.disabled === !0, i = ae(() => {
    a(l);
  }, [a, l]);
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
}, { useCallback: le } = e, oe = ({ command: n, label: r, active: t, disabled: o }) => {
  const a = w(), l = D(), c = n ?? "click", s = r ?? a.label, i = t ?? a.active === !0, d = o ?? a.disabled === !0, v = le(() => {
    l(c);
  }, [l, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    s
  );
}, re = () => {
  const n = w(), r = D(), t = n.count ?? 0, o = n.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, t), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: se } = e, ce = () => {
  const n = w(), r = D(), t = n.tabs ?? [], o = n.activeTabId, a = se((l) => {
    l !== o && r("selectTab", { tabId: l });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, t.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === o,
      className: "tlReactTabBar__tab" + (l.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, n.activeContent && /* @__PURE__ */ e.createElement(S, { control: n.activeContent })));
}, ie = () => {
  const n = w(), r = n.title, t = n.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, t.map((o, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(S, { control: o })))));
}, ue = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, de = () => {
  const n = w(), r = I(), [t, o] = e.useState("idle"), [a, l] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), d = n.status ?? "idle", v = n.error, f = d === "received" ? "idle" : t !== "idle" ? t : d, p = e.useCallback(async () => {
    if (t === "recording") {
      const C = c.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (t !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = C, s.current = [];
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(C, N ? { mimeType: N } : void 0);
        c.current = R, R.ondataavailable = (u) => {
          u.data.size > 0 && s.current.push(u.data);
        }, R.onstop = async () => {
          C.getTracks().forEach((_) => _.stop()), i.current = null;
          const u = new Blob(s.current, { type: R.mimeType || "audio/webm" });
          if (s.current = [], u.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const E = new FormData();
          E.append("audio", u, "recording.webm"), await r(E), o("idle");
        }, R.start(), o("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), l("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [t, r]), m = x(ue), b = f === "recording" ? m["js.audioRecorder.stop"] : f === "uploading" ? m["js.uploading"] : m["js.audioRecorder.record"], g = f === "uploading", k = ["tlAudioRecorder__button"];
  return f === "recording" && k.push("tlAudioRecorder__button--recording"), f === "uploading" && k.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: p,
      disabled: g,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, me = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, pe = () => {
  const n = w(), r = F(), t = !!n.hasAudio, o = n.dataRevision ?? 0, [a, l] = e.useState(t ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(o);
  e.useEffect(() => {
    t ? a === "disabled" && l("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), l("disabled"));
  }, [t]), e.useEffect(() => {
    o !== i.current && (i.current = o, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (a === "playing" || a === "paused" || a === "loading") && l("idle"));
  }, [o]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const d = e.useCallback(async () => {
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
    if (!s.current) {
      l("loading");
      try {
        const g = await fetch(r);
        if (!g.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", g.status), l("idle");
          return;
        }
        const k = await g.blob();
        s.current = URL.createObjectURL(k);
      } catch (g) {
        console.error("[TLAudioPlayer] Fetch error:", g), l("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    c.current = b, b.onended = () => {
      l("idle");
    }, b.play(), l("playing");
  }, [a, r]), v = x(me), f = a === "loading" ? v["js.loading"] : a === "playing" ? v["js.audioPlayer.pause"] : a === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], p = a === "disabled" || a === "loading", m = ["tlAudioPlayer__button"];
  return a === "playing" && m.push("tlAudioPlayer__button--playing"), a === "loading" && m.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: m.join(" "),
      onClick: d,
      disabled: p,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, be = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, fe = () => {
  const n = w(), r = I(), [t, o] = e.useState("idle"), [a, l] = e.useState(!1), c = e.useRef(null), s = n.status ?? "idle", i = n.error, d = n.accept ?? "", v = s === "received" ? "idle" : t !== "idle" ? t : s, f = e.useCallback(async (u) => {
    o("uploading");
    const E = new FormData();
    E.append("file", u, u.name), await r(E), o("idle");
  }, [r]), p = e.useCallback((u) => {
    var _;
    const E = (_ = u.target.files) == null ? void 0 : _[0];
    E && f(E);
  }, [f]), m = e.useCallback(() => {
    var u;
    t !== "uploading" && ((u = c.current) == null || u.click());
  }, [t]), b = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!0);
  }, []), g = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!1);
  }, []), k = e.useCallback((u) => {
    var _;
    if (u.preventDefault(), u.stopPropagation(), l(!1), t === "uploading") return;
    const E = (_ = u.dataTransfer.files) == null ? void 0 : _[0];
    E && f(E);
  }, [t, f]), C = v === "uploading", N = x(be), R = v === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: g,
      onDrop: k
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: d || void 0,
        onChange: p,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (v === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: m,
        disabled: C,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, he = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ve = () => {
  const n = w(), r = F(), t = D(), o = !!n.hasData, a = n.dataRevision ?? 0, l = n.fileName ?? "download", c = !!n.clearable, [s, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!o || s)) {
      i(!0);
      try {
        const m = r + (r.includes("?") ? "&" : "?") + "rev=" + a, b = await fetch(m);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const g = await b.blob(), k = URL.createObjectURL(g), C = document.createElement("a");
        C.href = k, C.download = l, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(k);
      } catch (m) {
        console.error("[TLDownload] Fetch error:", m);
      } finally {
        i(!1);
      }
    }
  }, [o, s, r, a, l]), v = e.useCallback(async () => {
    o && await t("clear");
  }, [o, t]), f = x(he);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const p = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: s,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), c && /* @__PURE__ */ e.createElement(
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
}, Ce = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ee = () => {
  const n = w(), r = I(), [t, o] = e.useState("idle"), [a, l] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), v = e.useRef(null), f = e.useRef(null), p = e.useRef(null), m = n.error, b = e.useMemo(
    () => {
      var h;
      return !!(window.isSecureContext && ((h = navigator.mediaDevices) != null && h.getUserMedia));
    },
    []
  ), g = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((h) => h.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), k = e.useCallback(() => {
    g(), o("idle");
  }, [g]), C = e.useCallback(async () => {
    var h;
    if (t !== "uploading") {
      if (l(null), !b) {
        (h = f.current) == null || h.click();
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = y, o("overlayOpen");
      } catch (y) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", y), l("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [t, b]), N = e.useCallback(async () => {
    if (t !== "overlayOpen")
      return;
    const h = i.current, y = v.current;
    if (!h || !y)
      return;
    y.width = h.videoWidth, y.height = h.videoHeight;
    const P = y.getContext("2d");
    P && (P.drawImage(h, 0, 0), g(), o("uploading"), y.toBlob(async (T) => {
      if (!T) {
        o("idle");
        return;
      }
      const U = new FormData();
      U.append("photo", T, "capture.jpg"), await r(U), o("idle");
    }, "image/jpeg", 0.85));
  }, [t, r, g]), R = e.useCallback(async (h) => {
    var T;
    const y = (T = h.target.files) == null ? void 0 : T[0];
    if (!y) return;
    o("uploading");
    const P = new FormData();
    P.append("photo", y, y.name), await r(P), o("idle"), f.current && (f.current.value = "");
  }, [r]);
  e.useEffect(() => {
    t === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [t]), e.useEffect(() => {
    var y;
    if (t !== "overlayOpen") return;
    (y = p.current) == null || y.focus();
    const h = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = h;
    };
  }, [t]), e.useEffect(() => {
    if (t !== "overlayOpen") return;
    const h = (y) => {
      y.key === "Escape" && k();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [t, k]), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((h) => h.stop()), d.current = null);
  }, []);
  const u = x(Ce), E = t === "uploading" ? u["js.uploading"] : u["js.photoCapture.open"], _ = ["tlPhotoCapture__cameraBtn"];
  t === "uploading" && _.push("tlPhotoCapture__cameraBtn--uploading");
  const M = ["tlPhotoCapture__overlayVideo"];
  c && M.push("tlPhotoCapture__overlayVideo--mirrored");
  const j = ["tlPhotoCapture__mirrorBtn"];
  return c && j.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: C,
      disabled: t === "uploading",
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !b && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: R
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), t === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: k }),
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
        className: j.join(" "),
        onClick: () => s((h) => !h),
        title: u["js.photoCapture.mirror"],
        "aria-label": u["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: N,
        title: u["js.photoCapture.capture"],
        "aria-label": u["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: k,
        title: u["js.photoCapture.close"],
        "aria-label": u["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, u[a]), m && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m));
}, _e = {
  "js.photoViewer.alt": "Captured photo"
}, ye = () => {
  const n = w(), r = F(), t = !!n.hasPhoto, o = n.dataRevision ?? 0, [a, l] = e.useState(null), c = e.useRef(o);
  e.useEffect(() => {
    if (!t) {
      a && (URL.revokeObjectURL(a), l(null));
      return;
    }
    if (o === c.current && a)
      return;
    c.current = o, a && (URL.revokeObjectURL(a), l(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(r);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const v = await d.blob();
        i || l(URL.createObjectURL(v));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [t, o, r]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const s = x(_e);
  return !t || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: V, useRef: B } = e, ge = () => {
  const n = w(), r = D(), t = n.orientation, o = n.resizable === !0, a = n.children ?? [], l = t === "horizontal", c = B(null), s = B(null), i = B(null), d = V((p, m) => {
    const b = {
      overflow: p.scrolling || "auto",
      position: "relative"
    };
    return p.collapsed ? b.flex = `0 0 ${p.size}px` : m !== void 0 ? b.flex = `0 0 ${m}px` : p.unit === "%" ? b.flex = `${p.size} 0 0%` : b.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (b.minWidth = l ? p.minSize : void 0, b.minHeight = l ? void 0 : p.minSize), b;
  }, [l]), v = V((p, m) => {
    p.preventDefault();
    const b = c.current;
    if (!b) return;
    const g = a[m], k = a[m + 1], C = b.querySelectorAll(":scope > .tlSplitPanel__child"), N = [];
    C.forEach((E) => {
      N.push(l ? E.offsetWidth : E.offsetHeight);
    }), i.current = N, s.current = {
      splitterIndex: m,
      startPos: l ? p.clientX : p.clientY,
      startSizeBefore: N[m],
      startSizeAfter: N[m + 1],
      childBefore: g,
      childAfter: k
    };
    const R = (E) => {
      const _ = s.current;
      if (!_ || !i.current) return;
      const j = (l ? E.clientX : E.clientY) - _.startPos, h = _.childBefore.minSize || 0, y = _.childAfter.minSize || 0;
      let P = _.startSizeBefore + j, T = _.startSizeAfter - j;
      P < h && (T += P - h, P = h), T < y && (P += T - y, T = y), i.current[_.splitterIndex] = P, i.current[_.splitterIndex + 1] = T;
      const U = b.querySelectorAll(":scope > .tlSplitPanel__child"), O = U[_.splitterIndex], $ = U[_.splitterIndex + 1];
      O && (O.style.flex = `0 0 ${P}px`), $ && ($.style.flex = `0 0 ${T}px`);
    }, u = () => {
      if (document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", u), document.body.style.cursor = "", document.body.style.userSelect = "", i.current) {
        const E = {};
        a.forEach((_, M) => {
          const j = _.control;
          j != null && j.controlId && i.current && (E[j.controlId] = i.current[M]);
        }), r("updateSizes", { sizes: E });
      }
      i.current = null, s.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", u), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, l, r]), f = [];
  return a.forEach((p, m) => {
    if (f.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${m}`,
          className: "tlSplitPanel__child",
          style: d(p)
        },
        /* @__PURE__ */ e.createElement(S, { control: p.control })
      )
    ), o && m < a.length - 1) {
      const b = a[m + 1];
      !p.collapsed && !b.collapsed && f.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${m}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${t}`,
            onMouseDown: (k) => v(k, m)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: `tlSplitPanel tlSplitPanel--${t}`,
      style: {
        display: "flex",
        flexDirection: l ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    f
  );
}, { useCallback: A } = e, ke = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Le = () => {
  const n = w(), r = D(), t = x(ke), o = n.title, a = n.expansionState ?? "NORMALIZED", l = n.showMinimize === !0, c = n.showMaximize === !0, s = n.showPopOut === !0, i = n.toolbarButtons ?? [], d = a === "MINIMIZED", v = a === "MAXIMIZED", f = a === "HIDDEN", p = A(() => {
    r("toggleMinimize");
  }, [r]), m = A(() => {
    r("toggleMaximize");
  }, [r]), b = A(() => {
    r("popOut");
  }, [r]);
  if (f)
    return null;
  const g = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: g
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((k, C) => /* @__PURE__ */ e.createElement("span", { key: C, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(S, { control: k }))), l && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: p,
        title: d ? t["js.panel.restore"] : t["js.panel.minimize"]
      },
      d ? "□" : "─"
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: m,
        title: v ? t["js.panel.restore"] : t["js.panel.maximize"]
      },
      v ? "⧉" : "□"
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: t["js.panel.popOut"]
      },
      "↗"
    ))),
    !d && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content", style: { flex: 1, overflow: "auto", position: "relative" } }, /* @__PURE__ */ e.createElement(S, { control: n.child }))
  );
}, we = () => {
  const n = w();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${n.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(S, { control: n.child })
  );
}, Re = () => {
  const n = w();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, n.activeChild && /* @__PURE__ */ e.createElement(S, { control: n.activeChild }));
};
L("TLButton", ne);
L("TLToggleButton", oe);
L("TLTextInput", W);
L("TLNumberInput", q);
L("TLDatePicker", Z);
L("TLSelect", J);
L("TLCheckbox", ee);
L("TLTable", te);
L("TLCounter", re);
L("TLTabBar", ce);
L("TLFieldList", ie);
L("TLAudioRecorder", de);
L("TLAudioPlayer", pe);
L("TLFileUpload", fe);
L("TLDownload", ve);
L("TLPhotoCapture", Ee);
L("TLPhotoViewer", ye);
L("TLSplitPanel", ge);
L("TLPanel", Le);
L("TLMaximizeRoot", we);
L("TLDeckPane", Re);
