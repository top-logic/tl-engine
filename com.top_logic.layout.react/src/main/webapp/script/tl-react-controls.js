import { React as e, useTLFieldValue as F, getComponent as se, useTLState as S, useTLCommand as P, TLChild as R, useTLUpload as X, useI18N as M, useTLDataUrl as Z, register as N } from "tl-react-bridge";
const { useCallback: ce } = e, ie = ({ controlId: r, state: t }) => {
  const [s, l] = F(), o = ce(
    (a) => {
      l(a.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactTextInput tlReactTextInput--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: r,
      value: s ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: de } = e, ue = ({ controlId: r, state: t, config: s }) => {
  const [l, o] = F(), a = de(
    (c) => {
      const i = c.target.value, d = i === "" ? null : Number(i);
      o(d);
    },
    [o]
  ), n = s != null && s.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: r,
      value: l != null ? String(l) : "",
      onChange: a,
      step: n,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: me } = e, pe = ({ controlId: r, state: t }) => {
  const [s, l] = F(), o = me(
    (a) => {
      l(a.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactDatePicker tlReactDatePicker--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: r,
      value: s ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: be } = e, fe = ({ controlId: r, state: t, config: s }) => {
  var c;
  const [l, o] = F(), a = be(
    (i) => {
      o(i.target.value || null);
    },
    [o]
  ), n = t.options ?? (s == null ? void 0 : s.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = n.find((d) => d.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: r,
      value: l ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    n.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: he } = e, Ee = ({ controlId: r, state: t }) => {
  const [s, l] = F(), o = he(
    (a) => {
      l(a.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: s === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: s === !0,
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, ve = ({ controlId: r, state: t }) => {
  const s = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: r, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, s.map((o) => /* @__PURE__ */ e.createElement("th", { key: o.name }, o.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((o, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, s.map((n) => {
    const c = n.cellModule ? se(n.cellModule) : void 0, i = o[n.name];
    if (c) {
      const d = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: n.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: r + "-" + a + "-" + n.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: n.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: _e } = e, ge = ({ controlId: r, command: t, label: s, disabled: l }) => {
  const o = S(), a = P(), n = t ?? "click", c = s ?? o.label, i = l ?? o.disabled === !0, d = _e(() => {
    a(n);
  }, [a, n]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: d,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: ye } = e, Ce = ({ controlId: r, command: t, label: s, active: l, disabled: o }) => {
  const a = S(), n = P(), c = t ?? "click", i = s ?? a.label, d = l ?? a.active === !0, p = o ?? a.disabled === !0, f = ye(() => {
    n(c);
  }, [n, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: f,
      disabled: p,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    i
  );
}, ke = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ne } = e, we = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.tabs ?? [], o = t.activeTabId, a = Ne((n) => {
    n !== o && s("selectTab", { tabId: n });
  }, [s, o]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((n) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: n.id,
      role: "tab",
      "aria-selected": n.id === o,
      className: "tlReactTabBar__tab" + (n.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(n.id)
    },
    n.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, Se = ({ controlId: r }) => {
  const t = S(), s = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((o, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: o })))));
}, Le = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Re = ({ controlId: r }) => {
  const t = S(), s = X(), [l, o] = e.useState("idle"), [a, n] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), p = t.status ?? "idle", f = t.error, h = p === "received" ? "idle" : l !== "idle" ? l : p, k = e.useCallback(async () => {
    if (l === "recording") {
      const v = c.current;
      v && v.state !== "inactive" && v.stop();
      return;
    }
    if (l !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const v = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = v, i.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(v, D ? { mimeType: D } : void 0);
        c.current = x, x.ondataavailable = (g) => {
          g.data.size > 0 && i.current.push(g.data);
        }, x.onstop = async () => {
          v.getTracks().forEach((T) => T.stop()), d.current = null;
          const g = new Blob(i.current, { type: x.mimeType || "audio/webm" });
          if (i.current = [], g.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const L = new FormData();
          L.append("audio", g, "recording.webm"), await s(L), o("idle");
        }, x.start(), o("recording");
      } catch (v) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", v), n("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [l, s]), y = M(Le), u = h === "recording" ? y["js.audioRecorder.stop"] : h === "uploading" ? y["js.uploading"] : y["js.audioRecorder.record"], m = h === "uploading", _ = ["tlAudioRecorder__button"];
  return h === "recording" && _.push("tlAudioRecorder__button--recording"), h === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: k,
      disabled: m,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, y[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, xe = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Te = ({ controlId: r }) => {
  const t = S(), s = Z(), l = !!t.hasAudio, o = t.dataRevision ?? 0, [a, n] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(o);
  e.useEffect(() => {
    l ? a === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), n("disabled"));
  }, [l]), e.useEffect(() => {
    o !== d.current && (d.current = o, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && n("idle"));
  }, [o]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
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
    if (!i.current) {
      n("loading");
      try {
        const m = await fetch(s);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), n("idle");
          return;
        }
        const _ = await m.blob();
        i.current = URL.createObjectURL(_);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), n("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      n("idle");
    }, u.play(), n("playing");
  }, [a, s]), f = M(xe), h = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], k = a === "disabled" || a === "loading", y = ["tlAudioPlayer__button"];
  return a === "playing" && y.push("tlAudioPlayer__button--playing"), a === "loading" && y.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: p,
      disabled: k,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, De = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Be = ({ controlId: r }) => {
  const t = S(), s = X(), [l, o] = e.useState("idle"), [a, n] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, p = t.accept ?? "", f = i === "received" ? "idle" : l !== "idle" ? l : i, h = e.useCallback(async (g) => {
    o("uploading");
    const L = new FormData();
    L.append("file", g, g.name), await s(L), o("idle");
  }, [s]), k = e.useCallback((g) => {
    var T;
    const L = (T = g.target.files) == null ? void 0 : T[0];
    L && h(L);
  }, [h]), y = e.useCallback(() => {
    var g;
    l !== "uploading" && ((g = c.current) == null || g.click());
  }, [l]), u = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), n(!0);
  }, []), m = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation(), n(!1);
  }, []), _ = e.useCallback((g) => {
    var T;
    if (g.preventDefault(), g.stopPropagation(), n(!1), l === "uploading") return;
    const L = (T = g.dataTransfer.files) == null ? void 0 : T[0];
    L && h(L);
  }, [l, h]), v = f === "uploading", D = M(De), x = f === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: m,
      onDrop: _
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
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: y,
        disabled: v,
        title: x,
        "aria-label": x
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
}, je = ({ controlId: r }) => {
  const t = S(), s = Z(), l = P(), o = !!t.hasData, a = t.dataRevision ?? 0, n = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!o || i)) {
      d(!0);
      try {
        const y = s + (s.includes("?") ? "&" : "?") + "rev=" + a, u = await fetch(y);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const m = await u.blob(), _ = URL.createObjectURL(m), v = document.createElement("a");
        v.href = _, v.download = n, v.style.display = "none", document.body.appendChild(v), v.click(), document.body.removeChild(v), URL.revokeObjectURL(_);
      } catch (y) {
        console.error("[TLDownload] Fetch error:", y);
      } finally {
        d(!1);
      }
    }
  }, [o, i, s, a, n]), f = e.useCallback(async () => {
    o && await l("clear");
  }, [o, l]), h = M(Pe);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const k = i ? h["js.downloading"] : h["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: i,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
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
}, Ae = ({ controlId: r }) => {
  const t = S(), s = X(), [l, o] = e.useState("idle"), [a, n] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), h = e.useRef(null), k = e.useRef(null), y = t.error, u = e.useMemo(
    () => {
      var b;
      return !!(window.isSecureContext && ((b = navigator.mediaDevices) != null && b.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((b) => b.stop()), p.current = null), d.current && (d.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    m(), o("idle");
  }, [m]), v = e.useCallback(async () => {
    var b;
    if (l !== "uploading") {
      if (n(null), !u) {
        (b = h.current) == null || b.click();
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = w, o("overlayOpen");
      } catch (w) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", w), n("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [l, u]), D = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const b = d.current, w = f.current;
    if (!b || !w)
      return;
    w.width = b.videoWidth, w.height = b.videoHeight;
    const C = w.getContext("2d");
    C && (C.drawImage(b, 0, 0), m(), o("uploading"), w.toBlob(async (B) => {
      if (!B) {
        o("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", B, "capture.jpg"), await s(I), o("idle");
    }, "image/jpeg", 0.85));
  }, [l, s, m]), x = e.useCallback(async (b) => {
    var B;
    const w = (B = b.target.files) == null ? void 0 : B[0];
    if (!w) return;
    o("uploading");
    const C = new FormData();
    C.append("photo", w, w.name), await s(C), o("idle"), h.current && (h.current.value = "");
  }, [s]);
  e.useEffect(() => {
    l === "overlayOpen" && d.current && p.current && (d.current.srcObject = p.current);
  }, [l]), e.useEffect(() => {
    var w;
    if (l !== "overlayOpen") return;
    (w = k.current) == null || w.focus();
    const b = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = b;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const b = (w) => {
      w.key === "Escape" && _();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [l, _]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((b) => b.stop()), p.current = null);
  }, []);
  const g = M(Me), L = l === "uploading" ? g["js.uploading"] : g["js.photoCapture.open"], T = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && T.push("tlPhotoCapture__cameraBtn--uploading");
  const A = ["tlPhotoCapture__overlayVideo"];
  c && A.push("tlPhotoCapture__overlayVideo--mirrored");
  const E = ["tlPhotoCapture__mirrorBtn"];
  return c && E.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: v,
      disabled: l === "uploading",
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !u && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: x
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
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
        className: E.join(" "),
        onClick: () => i((b) => !b),
        title: g["js.photoCapture.mirror"],
        "aria-label": g["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: g["js.photoCapture.capture"],
        "aria-label": g["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: g["js.photoCapture.close"],
        "aria-label": g["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g[a]), y && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, y));
}, Ie = {
  "js.photoViewer.alt": "Captured photo"
}, $e = ({ controlId: r }) => {
  const t = S(), s = Z(), l = !!t.hasPhoto, o = t.dataRevision ?? 0, [a, n] = e.useState(null), c = e.useRef(o);
  e.useEffect(() => {
    if (!l) {
      a && (URL.revokeObjectURL(a), n(null));
      return;
    }
    if (o === c.current && a)
      return;
    c.current = o, a && (URL.revokeObjectURL(a), n(null));
    let d = !1;
    return (async () => {
      try {
        const p = await fetch(s);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const f = await p.blob();
        d || n(URL.createObjectURL(f));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      d = !0;
    };
  }, [l, o, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = M(Ie);
  return !l || !a ? /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: te, useRef: W } = e, ze = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.orientation, o = t.resizable === !0, a = t.children ?? [], n = l === "horizontal", c = a.length > 0 && a.every((m) => m.collapsed), i = !c && a.some((m) => m.collapsed), d = c ? !n : n, p = W(null), f = W(null), h = W(null), k = te((m, _) => {
    const v = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !d ? v.flex = "1 0 0%" : v.flex = "0 0 auto" : _ !== void 0 ? v.flex = `0 0 ${_}px` : m.unit === "%" || i ? v.flex = `${m.size} 0 0%` : v.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (v.minWidth = n ? m.minSize : void 0, v.minHeight = n ? void 0 : m.minSize), v;
  }, [n, c, i, d]), y = te((m, _) => {
    m.preventDefault();
    const v = p.current;
    if (!v) return;
    const D = a[_], x = a[_ + 1], g = v.querySelectorAll(":scope > .tlSplitPanel__child"), L = [];
    g.forEach((E) => {
      L.push(n ? E.offsetWidth : E.offsetHeight);
    }), h.current = L, f.current = {
      splitterIndex: _,
      startPos: n ? m.clientX : m.clientY,
      startSizeBefore: L[_],
      startSizeAfter: L[_ + 1],
      childBefore: D,
      childAfter: x
    };
    const T = (E) => {
      const b = f.current;
      if (!b || !h.current) return;
      const C = (n ? E.clientX : E.clientY) - b.startPos, B = b.childBefore.minSize || 0, I = b.childAfter.minSize || 0;
      let $ = b.startSizeBefore + C, z = b.startSizeAfter - C;
      $ < B && (z += $ - B, $ = B), z < I && ($ += z - I, z = I), h.current[b.splitterIndex] = $, h.current[b.splitterIndex + 1] = z;
      const J = v.querySelectorAll(":scope > .tlSplitPanel__child"), Q = J[b.splitterIndex], ee = J[b.splitterIndex + 1];
      Q && (Q.style.flex = `0 0 ${$}px`), ee && (ee.style.flex = `0 0 ${z}px`);
    }, A = () => {
      if (document.removeEventListener("mousemove", T), document.removeEventListener("mouseup", A), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const E = {};
        a.forEach((b, w) => {
          const C = b.control;
          C != null && C.controlId && h.current && (E[C.controlId] = h.current[w]);
        }), s("updateSizes", { sizes: E });
      }
      h.current = null, f.current = null;
    };
    document.addEventListener("mousemove", T), document.addEventListener("mouseup", A), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, n, s]), u = [];
  return a.forEach((m, _) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${m.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(m)
        },
        /* @__PURE__ */ e.createElement(R, { control: m.control })
      )
    ), o && _ < a.length - 1) {
      const v = a[_ + 1];
      !m.collapsed && !v.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (x) => y(x, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: r,
      className: `tlSplitPanel tlSplitPanel--${l}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    u
  );
}, { useCallback: V } = e, Ue = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Fe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Oe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), We = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ve = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), He = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ke = ({ controlId: r }) => {
  const t = S(), s = P(), l = M(Ue), o = t.title, a = t.expansionState ?? "NORMALIZED", n = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], p = a === "MINIMIZED", f = a === "MAXIMIZED", h = a === "HIDDEN", k = V(() => {
    s("toggleMinimize");
  }, [s]), y = V(() => {
    s("toggleMaximize");
  }, [s]), u = V(() => {
    s("popOut");
  }, [s]);
  if (h)
    return null;
  const m = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((_, v) => /* @__PURE__ */ e.createElement("span", { key: v, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(R, { control: _ }))), n && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Oe, null) : /* @__PURE__ */ e.createElement(Fe, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: f ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(Ve, null) : /* @__PURE__ */ e.createElement(We, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(He, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child }))
  );
}, Ye = ({ controlId: r }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, Ge = ({ controlId: r }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: j, useState: H, useEffect: O, useRef: G } = e, qe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function q(r, t, s, l) {
  const o = [];
  for (const a of r)
    a.type === "nav" ? o.push({ id: a.id, type: "nav", groupId: l }) : a.type === "command" ? o.push({ id: a.id, type: "command", groupId: l }) : a.type === "group" && (o.push({ id: a.id, type: "group" }), (s.get(a.id) ?? a.expanded) && !t && o.push(...q(a.children, t, s, a.id)));
  return o;
}
const U = ({ icon: r }) => r ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + r, "aria-hidden": "true" }) : null, Xe = ({ item: r, active: t, collapsed: s, onSelect: l, tabIndex: o, itemRef: a, onFocus: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(r.id),
    title: s ? r.label : void 0,
    tabIndex: o,
    ref: a,
    onFocus: () => n(r.id)
  },
  s && r.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(U, { icon: r.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, r.badge)) : /* @__PURE__ */ e.createElement(U, { icon: r.icon }),
  !s && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
  !s && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
), Ze = ({ item: r, collapsed: t, onExecute: s, tabIndex: l, itemRef: o, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => s(r.id),
    title: t ? r.label : void 0,
    tabIndex: l,
    ref: o,
    onFocus: () => a(r.id)
  },
  /* @__PURE__ */ e.createElement(U, { icon: r.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)
), Je = ({ item: r, collapsed: t }) => t && !r.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? r.label : void 0 }, /* @__PURE__ */ e.createElement(U, { icon: r.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)), Qe = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), et = ({ item: r, activeItemId: t, onSelect: s, onExecute: l, onClose: o }) => {
  const a = G(null);
  O(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => o(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [o]), O(() => {
    const c = (i) => {
      i.key === "Escape" && o();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [o]);
  const n = j((c) => {
    c.type === "nav" ? (s(c.id), o()) : c.type === "command" && (l(c.id), o());
  }, [s, l, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, r.label), r.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => n(c)
        },
        /* @__PURE__ */ e.createElement(U, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, tt = ({
  item: r,
  expanded: t,
  activeItemId: s,
  collapsed: l,
  onSelect: o,
  onExecute: a,
  onToggleGroup: n,
  tabIndex: c,
  itemRef: i,
  onFocus: d,
  focusedId: p,
  setItemRef: f,
  onItemFocus: h,
  flyoutGroupId: k,
  onOpenFlyout: y,
  onCloseFlyout: u
}) => {
  const m = j(() => {
    l ? k === r.id ? u() : y(r.id) : n(r.id);
  }, [l, k, r.id, n, y, u]), _ = l && k === r.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: m,
      title: l ? r.label : void 0,
      "aria-expanded": l ? _ : t,
      tabIndex: c,
      ref: i,
      onFocus: () => d(r.id)
    },
    /* @__PURE__ */ e.createElement(U, { icon: r.icon }),
    !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
    !l && /* @__PURE__ */ e.createElement(
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
  ), _ && /* @__PURE__ */ e.createElement(
    et,
    {
      item: r,
      activeItemId: s,
      onSelect: o,
      onExecute: a,
      onClose: u
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, r.children.map((v) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: v.id,
      item: v,
      activeItemId: s,
      collapsed: l,
      onSelect: o,
      onExecute: a,
      onToggleGroup: n,
      focusedId: p,
      setItemRef: f,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: y,
      onCloseFlyout: u
    }
  ))));
}, re = ({
  item: r,
  activeItemId: t,
  collapsed: s,
  onSelect: l,
  onExecute: o,
  onToggleGroup: a,
  focusedId: n,
  setItemRef: c,
  onItemFocus: i,
  groupStates: d,
  flyoutGroupId: p,
  onOpenFlyout: f,
  onCloseFlyout: h
}) => {
  switch (r.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Xe,
        {
          item: r,
          active: r.id === t,
          collapsed: s,
          onSelect: l,
          tabIndex: n === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ze,
        {
          item: r,
          collapsed: s,
          onExecute: o,
          tabIndex: n === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Je, { item: r, collapsed: s });
    case "separator":
      return /* @__PURE__ */ e.createElement(Qe, null);
    case "group": {
      const k = d ? d.get(r.id) ?? r.expanded : r.expanded;
      return /* @__PURE__ */ e.createElement(
        tt,
        {
          item: r,
          expanded: k,
          activeItemId: t,
          collapsed: s,
          onSelect: l,
          onExecute: o,
          onToggleGroup: a,
          tabIndex: n === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i,
          focusedId: n,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: p,
          onOpenFlyout: f,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, at = ({ controlId: r }) => {
  const t = S(), s = P(), l = M(qe), o = t.items ?? [], a = t.activeItemId, n = t.collapsed, [c, i] = H(() => {
    const E = /* @__PURE__ */ new Map(), b = (w) => {
      for (const C of w)
        C.type === "group" && (E.set(C.id, C.expanded), b(C.children));
    };
    return b(o), E;
  }), d = j((E) => {
    i((b) => {
      const w = new Map(b), C = w.get(E) ?? !1;
      return w.set(E, !C), s("toggleGroup", { itemId: E, expanded: !C }), w;
    });
  }, [s]), p = j((E) => {
    E !== a && s("selectItem", { itemId: E });
  }, [s, a]), f = j((E) => {
    s("executeCommand", { itemId: E });
  }, [s]), h = j(() => {
    s("toggleCollapse", {});
  }, [s]), [k, y] = H(null), u = j((E) => {
    y(E);
  }, []), m = j(() => {
    y(null);
  }, []);
  O(() => {
    n || y(null);
  }, [n]);
  const [_, v] = H(() => {
    const E = q(o, n, c);
    return E.length > 0 ? E[0].id : "";
  }), D = G(/* @__PURE__ */ new Map()), x = j((E) => (b) => {
    b ? D.current.set(E, b) : D.current.delete(E);
  }, []), g = j((E) => {
    v(E);
  }, []), L = G(0), T = j((E) => {
    v(E), L.current++;
  }, []);
  O(() => {
    const E = D.current.get(_);
    E && document.activeElement !== E && E.focus();
  }, [_, L.current]);
  const A = j((E) => {
    if (E.key === "Escape" && k !== null) {
      E.preventDefault(), m();
      return;
    }
    const b = q(o, n, c);
    if (b.length === 0) return;
    const w = b.findIndex((B) => B.id === _);
    if (w < 0) return;
    const C = b[w];
    switch (E.key) {
      case "ArrowDown": {
        E.preventDefault();
        const B = (w + 1) % b.length;
        T(b[B].id);
        break;
      }
      case "ArrowUp": {
        E.preventDefault();
        const B = (w - 1 + b.length) % b.length;
        T(b[B].id);
        break;
      }
      case "Home": {
        E.preventDefault(), T(b[0].id);
        break;
      }
      case "End": {
        E.preventDefault(), T(b[b.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        E.preventDefault(), C.type === "nav" ? p(C.id) : C.type === "command" ? f(C.id) : C.type === "group" && (n ? k === C.id ? m() : u(C.id) : d(C.id));
        break;
      }
      case "ArrowRight": {
        C.type === "group" && !n && ((c.get(C.id) ?? !1) || (E.preventDefault(), d(C.id)));
        break;
      }
      case "ArrowLeft": {
        C.type === "group" && !n && (c.get(C.id) ?? !1) && (E.preventDefault(), d(C.id));
        break;
      }
    }
  }, [
    o,
    n,
    c,
    _,
    k,
    T,
    p,
    f,
    d,
    u,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlSidebar" + (n ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, n ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: A }, o.map((E) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: E.id,
      item: E,
      activeItemId: a,
      collapsed: n,
      onSelect: p,
      onExecute: f,
      onToggleGroup: d,
      focusedId: _,
      setItemRef: x,
      onItemFocus: g,
      groupStates: c,
      flyoutGroupId: k,
      onOpenFlyout: u,
      onCloseFlyout: m
    }
  ))), n ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, nt = ({ controlId: r }) => {
  const t = S(), s = t.direction ?? "column", l = t.gap ?? "default", o = t.align ?? "stretch", a = t.wrap === !0, n = t.children ?? [], c = [
    "tlStack",
    `tlStack--${s}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${o}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: c }, n.map((i, d) => /* @__PURE__ */ e.createElement(R, { key: d, control: i })));
}, lt = ({ controlId: r }) => {
  const t = S(), s = t.columns, l = t.minColumnWidth, o = t.gap ?? "default", a = t.children ?? [], n = {};
  return l ? n.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : s && (n.gridTemplateColumns = `repeat(${s}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: r, className: `tlGrid tlGrid--gap-${o}`, style: n }, a.map((c, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: c })));
}, ot = ({ controlId: r }) => {
  const t = S(), s = t.title, l = t.variant ?? "outlined", o = t.padding ?? "default", a = t.headerActions ?? [], n = t.child, c = s != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, s && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, d) => /* @__PURE__ */ e.createElement(R, { key: d, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(R, { control: n })));
}, rt = ({ controlId: r }) => {
  const t = S(), s = t.title ?? "", l = t.leading, o = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: r, className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(R, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, s), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, o.map((i, d) => /* @__PURE__ */ e.createElement(R, { key: d, control: i }))));
}, { useCallback: st } = e, ct = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.items ?? [], o = st((a) => {
    s("navigate", { itemId: a });
  }, [s]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((a, n) => {
    const c = n === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, n > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => o(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: it } = e, dt = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.items ?? [], o = t.activeItemId, a = it((n) => {
    n !== o && s("selectItem", { itemId: n });
  }, [s, o]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((n) => {
    const c = n.id === o;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: n.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => a(n.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + n.icon, "aria-hidden": "true" }), n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, n.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, n.label)
    );
  }));
}, { useCallback: ae, useEffect: ne, useRef: ut } = e, mt = {
  "js.dialog.close": "Close"
}, pt = ({ controlId: r }) => {
  const t = S(), s = P(), l = M(mt), o = t.open === !0, a = t.title ?? "", n = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], d = t.child, p = ut(null), f = ae(() => {
    s("close");
  }, [s]), h = ae((y) => {
    c && y.target === y.currentTarget && f();
  }, [c, f]);
  if (ne(() => {
    if (!o) return;
    const y = (u) => {
      u.key === "Escape" && f();
    };
    return document.addEventListener("keydown", y), () => document.removeEventListener("keydown", y);
  }, [o, f]), ne(() => {
    o && p.current && p.current.focus();
  }, [o]), !o) return null;
  const k = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDialog__backdrop", onClick: h }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${n}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": k,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: k }, a), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: f,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(R, { control: d })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((y, u) => /* @__PURE__ */ e.createElement(R, { key: u, control: y })))
  ));
}, { useCallback: bt, useEffect: ft } = e, ht = {
  "js.drawer.close": "Close"
}, Et = ({ controlId: r }) => {
  const t = S(), s = P(), l = M(ht), o = t.open === !0, a = t.position ?? "right", n = t.size ?? "medium", c = t.title ?? null, i = t.child, d = bt(() => {
    s("close");
  }, [s]);
  ft(() => {
    if (!o) return;
    const f = (h) => {
      h.key === "Escape" && d();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [o, d]);
  const p = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${n}`,
    o ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: r, className: p, "aria-hidden": !o }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: d,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(R, { control: i })));
}, { useCallback: le, useEffect: vt, useState: _t } = e, gt = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.message ?? "", o = t.variant ?? "info", a = t.action, n = t.duration ?? 5e3, c = t.visible === !0, [i, d] = _t(!1), p = le(() => {
    d(!0), setTimeout(() => {
      s("dismiss"), d(!1);
    }, 200);
  }, [s]), f = le(() => {
    a && s(a.commandName), p();
  }, [s, a, p]);
  return vt(() => {
    if (!c || n === 0) return;
    const h = setTimeout(p, n);
    return () => clearTimeout(h);
  }, [c, n, p]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlSnackbar tlSnackbar--${o}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: f }, a.label)
  );
}, { useCallback: K, useEffect: Y, useRef: yt, useState: oe } = e, Ct = ({ controlId: r }) => {
  const t = S(), s = P(), l = t.open === !0, o = t.anchorId, a = t.items ?? [], n = yt(null), [c, i] = oe({ top: 0, left: 0 }), [d, p] = oe(0), f = a.filter((u) => u.type === "item" && !u.disabled);
  Y(() => {
    var g, L;
    if (!l || !o) return;
    const u = document.getElementById(o);
    if (!u) return;
    const m = u.getBoundingClientRect(), _ = ((g = n.current) == null ? void 0 : g.offsetHeight) ?? 200, v = ((L = n.current) == null ? void 0 : L.offsetWidth) ?? 200;
    let D = m.bottom + 4, x = m.left;
    D + _ > window.innerHeight && (D = m.top - _ - 4), x + v > window.innerWidth && (x = m.right - v), i({ top: D, left: x }), p(0);
  }, [l, o]);
  const h = K(() => {
    s("close");
  }, [s]), k = K((u) => {
    s("selectItem", { itemId: u });
  }, [s]);
  Y(() => {
    if (!l) return;
    const u = (m) => {
      n.current && !n.current.contains(m.target) && h();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [l, h]);
  const y = K((u) => {
    if (u.key === "Escape") {
      h();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), p((m) => (m + 1) % f.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), p((m) => (m - 1 + f.length) % f.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const m = f[d];
      m && k(m.id);
    }
  }, [h, k, f, d]);
  return Y(() => {
    l && n.current && n.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlMenu",
      role: "menu",
      ref: n,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: y
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
          onClick: () => k(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, kt = ({ controlId: r }) => {
  const t = S(), s = t.header, l = t.content, o = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAppShell" }, s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(R, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(R, { control: l })), o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(R, { control: o })), /* @__PURE__ */ e.createElement(R, { control: a }));
};
N("TLButton", ge);
N("TLToggleButton", Ce);
N("TLTextInput", ie);
N("TLNumberInput", ue);
N("TLDatePicker", pe);
N("TLSelect", fe);
N("TLCheckbox", Ee);
N("TLTable", ve);
N("TLCounter", ke);
N("TLTabBar", we);
N("TLFieldList", Se);
N("TLAudioRecorder", Re);
N("TLAudioPlayer", Te);
N("TLFileUpload", Be);
N("TLDownload", je);
N("TLPhotoCapture", Ae);
N("TLPhotoViewer", $e);
N("TLSplitPanel", ze);
N("TLPanel", Ke);
N("TLMaximizeRoot", Ye);
N("TLDeckPane", Ge);
N("TLSidebar", at);
N("TLStack", nt);
N("TLGrid", lt);
N("TLCard", ot);
N("TLAppBar", rt);
N("TLBreadcrumb", ct);
N("TLBottomBar", dt);
N("TLDialog", pt);
N("TLDrawer", Et);
N("TLSnackbar", gt);
N("TLMenu", Ct);
N("TLAppShell", kt);
