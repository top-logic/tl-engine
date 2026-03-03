import { React as e, useTLFieldValue as X, getComponent as Ee, useTLState as x, useTLCommand as I, TLChild as T, useTLUpload as se, useI18N as O, useTLDataUrl as ce, register as L } from "tl-react-bridge";
const { useCallback: ve } = e, ge = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = ve(
    (a) => {
      n(a.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactTextInput tlReactTextInput--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: r,
      value: l ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Ce } = e, ye = ({ controlId: r, state: t, config: l }) => {
  const [n, s] = X(), a = Ce(
    (c) => {
      const i = c.target.value, d = i === "" ? null : Number(i);
      s(d);
    },
    [s]
  ), o = l != null && l.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: r,
      value: n != null ? String(n) : "",
      onChange: a,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ke } = e, we = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = ke(
    (a) => {
      n(a.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactDatePicker tlReactDatePicker--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: r,
      value: l ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ne } = e, Le = ({ controlId: r, state: t, config: l }) => {
  var c;
  const [n, s] = X(), a = Ne(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), o = t.options ?? (l == null ? void 0 : l.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = o.find((d) => d.value === n)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: r,
      value: n ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: Se } = e, xe = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = Se(
    (a) => {
      n(a.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: l === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: l === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Te = ({ controlId: r, state: t }) => {
  const l = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: r, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, l.map((o) => {
    const c = o.cellModule ? Ee(o.cellModule) : void 0, i = s[o.name];
    if (c) {
      const d = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: r + "-" + a + "-" + o.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Re } = e, De = ({ controlId: r, command: t, label: l, disabled: n }) => {
  const s = x(), a = I(), o = t ?? "click", c = l ?? s.label, i = n ?? s.disabled === !0, d = Re(() => {
    a(o);
  }, [a, o]);
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
}, { useCallback: Be } = e, Pe = ({ controlId: r, command: t, label: l, active: n, disabled: s }) => {
  const a = x(), o = I(), c = t ?? "click", i = l ?? a.label, d = n ?? a.active === !0, p = s ?? a.disabled === !0, f = Be(() => {
    o(c);
  }, [o, c]);
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
}, je = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Fe } = e, Me = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.tabs ?? [], s = t.activeTabId, a = Fe((o) => {
    o !== s && l("selectTab", { tabId: o });
  }, [l, s]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === s,
      className: "tlReactTabBar__tab" + (o.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, Ie = ({ controlId: r }) => {
  const t = x(), l = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((s, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(T, { control: s })))));
}, Ae = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, $e = ({ controlId: r }) => {
  const t = x(), l = se(), [n, s] = e.useState("idle"), [a, o] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), p = t.status ?? "idle", f = t.error, b = p === "received" ? "idle" : n !== "idle" ? n : p, k = e.useCallback(async () => {
    if (n === "recording") {
      const _ = c.current;
      _ && _.state !== "inactive" && _.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const _ = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = _, i.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(_, B ? { mimeType: B } : void 0);
        c.current = P, P.ondataavailable = (w) => {
          w.data.size > 0 && i.current.push(w.data);
        }, P.onstop = async () => {
          _.getTracks().forEach((j) => j.stop()), d.current = null;
          const w = new Blob(i.current, { type: P.mimeType || "audio/webm" });
          if (i.current = [], w.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const D = new FormData();
          D.append("audio", w, "recording.webm"), await l(D), s("idle");
        }, P.start(), s("recording");
      } catch (_) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", _), o("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [n, l]), C = O(Ae), u = b === "recording" ? C["js.audioRecorder.stop"] : b === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], m = b === "uploading", h = ["tlAudioRecorder__button"];
  return b === "recording" && h.push("tlAudioRecorder__button--recording"), b === "uploading" && h.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: k,
      disabled: m,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, ze = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Oe = ({ controlId: r }) => {
  const t = x(), l = ce(), n = !!t.hasAudio, s = t.dataRevision ?? 0, [a, o] = e.useState(n ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(s);
  e.useEffect(() => {
    n ? a === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    s !== d.current && (d.current = s, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && o("idle"));
  }, [s]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      c.current && c.current.pause(), o("paused");
      return;
    }
    if (a === "paused" && c.current) {
      c.current.play(), o("playing");
      return;
    }
    if (!i.current) {
      o("loading");
      try {
        const m = await fetch(l);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), o("idle");
          return;
        }
        const h = await m.blob();
        i.current = URL.createObjectURL(h);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), o("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      o("idle");
    }, u.play(), o("playing");
  }, [a, l]), f = O(ze), b = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], k = a === "disabled" || a === "loading", C = ["tlAudioPlayer__button"];
  return a === "playing" && C.push("tlAudioPlayer__button--playing"), a === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: p,
      disabled: k,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ue = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, We = ({ controlId: r }) => {
  const t = x(), l = se(), [n, s] = e.useState("idle"), [a, o] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, p = t.accept ?? "", f = i === "received" ? "idle" : n !== "idle" ? n : i, b = e.useCallback(async (w) => {
    s("uploading");
    const D = new FormData();
    D.append("file", w, w.name), await l(D), s("idle");
  }, [l]), k = e.useCallback((w) => {
    var j;
    const D = (j = w.target.files) == null ? void 0 : j[0];
    D && b(D);
  }, [b]), C = e.useCallback(() => {
    var w;
    n !== "uploading" && ((w = c.current) == null || w.click());
  }, [n]), u = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!0);
  }, []), m = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!1);
  }, []), h = e.useCallback((w) => {
    var j;
    if (w.preventDefault(), w.stopPropagation(), o(!1), n === "uploading") return;
    const D = (j = w.dataTransfer.files) == null ? void 0 : j[0];
    D && b(D);
  }, [n, b]), _ = f === "uploading", B = O(Ue), P = f === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
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
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: _,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
}, Ve = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, He = ({ controlId: r }) => {
  const t = x(), l = ce(), n = I(), s = !!t.hasData, a = t.dataRevision ?? 0, o = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!s || i)) {
      d(!0);
      try {
        const C = l + (l.includes("?") ? "&" : "?") + "rev=" + a, u = await fetch(C);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const m = await u.blob(), h = URL.createObjectURL(m), _ = document.createElement("a");
        _.href = h, _.download = o, _.style.display = "none", document.body.appendChild(_), _.click(), document.body.removeChild(_), URL.revokeObjectURL(h);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        d(!1);
      }
    }
  }, [s, i, l, a, o]), f = e.useCallback(async () => {
    s && await n("clear");
  }, [s, n]), b = O(Ve);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const k = i ? b["js.downloading"] : b["js.download.file"].replace("{0}", o);
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Ke = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ge = ({ controlId: r }) => {
  const t = x(), l = se(), [n, s] = e.useState("idle"), [a, o] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), b = e.useRef(null), k = e.useRef(null), C = t.error, u = e.useMemo(
    () => {
      var E;
      return !!(window.isSecureContext && ((E = navigator.mediaDevices) != null && E.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((E) => E.stop()), p.current = null), d.current && (d.current.srcObject = null);
  }, []), h = e.useCallback(() => {
    m(), s("idle");
  }, [m]), _ = e.useCallback(async () => {
    var E;
    if (n !== "uploading") {
      if (o(null), !u) {
        (E = b.current) == null || E.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = S, s("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), o("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [n, u]), B = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const E = d.current, S = f.current;
    if (!E || !S)
      return;
    S.width = E.videoWidth, S.height = E.videoHeight;
    const N = S.getContext("2d");
    N && (N.drawImage(E, 0, 0), m(), s("uploading"), S.toBlob(async (F) => {
      if (!F) {
        s("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", F, "capture.jpg"), await l(H), s("idle");
    }, "image/jpeg", 0.85));
  }, [n, l, m]), P = e.useCallback(async (E) => {
    var F;
    const S = (F = E.target.files) == null ? void 0 : F[0];
    if (!S) return;
    s("uploading");
    const N = new FormData();
    N.append("photo", S, S.name), await l(N), s("idle"), b.current && (b.current.value = "");
  }, [l]);
  e.useEffect(() => {
    n === "overlayOpen" && d.current && p.current && (d.current.srcObject = p.current);
  }, [n]), e.useEffect(() => {
    var S;
    if (n !== "overlayOpen") return;
    (S = k.current) == null || S.focus();
    const E = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = E;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const E = (S) => {
      S.key === "Escape" && h();
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
  }, [n, h]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((E) => E.stop()), p.current = null);
  }, []);
  const w = O(Ke), D = n === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], j = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && j.push("tlPhotoCapture__cameraBtn--uploading");
  const W = ["tlPhotoCapture__overlayVideo"];
  c && W.push("tlPhotoCapture__overlayVideo--mirrored");
  const g = ["tlPhotoCapture__mirrorBtn"];
  return c && g.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: j.join(" "),
      onClick: _,
      disabled: n === "uploading",
      title: D,
      "aria-label": D
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !u && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: h }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: d,
        className: W.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: g.join(" "),
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
        onClick: h,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[a]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, Ye = {
  "js.photoViewer.alt": "Captured photo"
}, Xe = ({ controlId: r }) => {
  const t = x(), l = ce(), n = !!t.hasPhoto, s = t.dataRevision ?? 0, [a, o] = e.useState(null), c = e.useRef(s);
  e.useEffect(() => {
    if (!n) {
      a && (URL.revokeObjectURL(a), o(null));
      return;
    }
    if (s === c.current && a)
      return;
    c.current = s, a && (URL.revokeObjectURL(a), o(null));
    let d = !1;
    return (async () => {
      try {
        const p = await fetch(l);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const f = await p.blob();
        d || o(URL.createObjectURL(f));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      d = !0;
    };
  }, [n, s, l]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = O(Ye);
  return !n || !a ? /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ie, useRef: ee } = e, qe = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.orientation, s = t.resizable === !0, a = t.children ?? [], o = n === "horizontal", c = a.length > 0 && a.every((m) => m.collapsed), i = !c && a.some((m) => m.collapsed), d = c ? !o : o, p = ee(null), f = ee(null), b = ee(null), k = ie((m, h) => {
    const _ = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !d ? _.flex = "1 0 0%" : _.flex = "0 0 auto" : h !== void 0 ? _.flex = `0 0 ${h}px` : m.unit === "%" || i ? _.flex = `${m.size} 0 0%` : _.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (_.minWidth = o ? m.minSize : void 0, _.minHeight = o ? void 0 : m.minSize), _;
  }, [o, c, i, d]), C = ie((m, h) => {
    m.preventDefault();
    const _ = p.current;
    if (!_) return;
    const B = a[h], P = a[h + 1], w = _.querySelectorAll(":scope > .tlSplitPanel__child"), D = [];
    w.forEach((g) => {
      D.push(o ? g.offsetWidth : g.offsetHeight);
    }), b.current = D, f.current = {
      splitterIndex: h,
      startPos: o ? m.clientX : m.clientY,
      startSizeBefore: D[h],
      startSizeAfter: D[h + 1],
      childBefore: B,
      childAfter: P
    };
    const j = (g) => {
      const E = f.current;
      if (!E || !b.current) return;
      const N = (o ? g.clientX : g.clientY) - E.startPos, F = E.childBefore.minSize || 0, H = E.childAfter.minSize || 0;
      let K = E.startSizeBefore + N, U = E.startSizeAfter - N;
      K < F && (U += K - F, K = F), U < H && (K += U - H, U = H), b.current[E.splitterIndex] = K, b.current[E.splitterIndex + 1] = U;
      const q = _.querySelectorAll(":scope > .tlSplitPanel__child"), Y = q[E.splitterIndex], Z = q[E.splitterIndex + 1];
      Y && (Y.style.flex = `0 0 ${K}px`), Z && (Z.style.flex = `0 0 ${U}px`);
    }, W = () => {
      if (document.removeEventListener("mousemove", j), document.removeEventListener("mouseup", W), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const g = {};
        a.forEach((E, S) => {
          const N = E.control;
          N != null && N.controlId && b.current && (g[N.controlId] = b.current[S]);
        }), l("updateSizes", { sizes: g });
      }
      b.current = null, f.current = null;
    };
    document.addEventListener("mousemove", j), document.addEventListener("mouseup", W), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, o, l]), u = [];
  return a.forEach((m, h) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${h}`,
          className: `tlSplitPanel__child${m.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(m)
        },
        /* @__PURE__ */ e.createElement(T, { control: m.control })
      )
    ), s && h < a.length - 1) {
      const _ = a[h + 1];
      !m.collapsed && !_.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${h}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => C(P, h)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: r,
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
}, { useCallback: te } = e, Ze = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Qe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), et = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), at = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), nt = ({ controlId: r }) => {
  const t = x(), l = I(), n = O(Ze), s = t.title, a = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], p = a === "MINIMIZED", f = a === "MAXIMIZED", b = a === "HIDDEN", k = te(() => {
    l("toggleMinimize");
  }, [l]), C = te(() => {
    l("toggleMaximize");
  }, [l]), u = te(() => {
    l("popOut");
  }, [l]);
  if (b)
    return null;
  const m = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((h, _) => /* @__PURE__ */ e.createElement("span", { key: _, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(T, { control: h }))), o && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: p ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Qe, null) : /* @__PURE__ */ e.createElement(Je, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(tt, null) : /* @__PURE__ */ e.createElement(et, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(at, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(T, { control: t.child }))
  );
}, lt = ({ controlId: r }) => {
  const t = x();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(T, { control: t.child })
  );
}, ot = ({ controlId: r }) => {
  const t = x();
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(T, { control: t.activeChild }));
}, { useCallback: z, useState: ae, useEffect: J, useRef: oe } = e, rt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function re(r, t, l, n) {
  const s = [];
  for (const a of r)
    a.type === "nav" ? s.push({ id: a.id, type: "nav", groupId: n }) : a.type === "command" ? s.push({ id: a.id, type: "command", groupId: n }) : a.type === "group" && (s.push({ id: a.id, type: "group" }), (l.get(a.id) ?? a.expanded) && !t && s.push(...re(a.children, t, l, a.id)));
  return s;
}
const G = ({ icon: r }) => r ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + r, "aria-hidden": "true" }) : null, st = ({ item: r, active: t, collapsed: l, onSelect: n, tabIndex: s, itemRef: a, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(r.id),
    title: l ? r.label : void 0,
    tabIndex: s,
    ref: a,
    onFocus: () => o(r.id)
  },
  l && r.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(G, { icon: r.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, r.badge)) : /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
  !l && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
), ct = ({ item: r, collapsed: t, onExecute: l, tabIndex: n, itemRef: s, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(r.id),
    title: t ? r.label : void 0,
    tabIndex: n,
    ref: s,
    onFocus: () => a(r.id)
  },
  /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)
), it = ({ item: r, collapsed: t }) => t && !r.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? r.label : void 0 }, /* @__PURE__ */ e.createElement(G, { icon: r.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)), dt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ut = ({ item: r, activeItemId: t, onSelect: l, onExecute: n, onClose: s }) => {
  const a = oe(null);
  J(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), J(() => {
    const c = (i) => {
      i.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const o = z((c) => {
    c.type === "nav" ? (l(c.id), s()) : c.type === "command" && (n(c.id), s());
  }, [l, n, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, r.label), r.children.map((c) => {
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
        /* @__PURE__ */ e.createElement(G, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, mt = ({
  item: r,
  expanded: t,
  activeItemId: l,
  collapsed: n,
  onSelect: s,
  onExecute: a,
  onToggleGroup: o,
  tabIndex: c,
  itemRef: i,
  onFocus: d,
  focusedId: p,
  setItemRef: f,
  onItemFocus: b,
  flyoutGroupId: k,
  onOpenFlyout: C,
  onCloseFlyout: u
}) => {
  const m = z(() => {
    n ? k === r.id ? u() : C(r.id) : o(r.id);
  }, [n, k, r.id, o, C, u]), h = n && k === r.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: m,
      title: n ? r.label : void 0,
      "aria-expanded": n ? h : t,
      tabIndex: c,
      ref: i,
      onFocus: () => d(r.id)
    },
    /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
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
  ), h && /* @__PURE__ */ e.createElement(
    ut,
    {
      item: r,
      activeItemId: l,
      onSelect: s,
      onExecute: a,
      onClose: u
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, r.children.map((_) => /* @__PURE__ */ e.createElement(
    be,
    {
      key: _.id,
      item: _,
      activeItemId: l,
      collapsed: n,
      onSelect: s,
      onExecute: a,
      onToggleGroup: o,
      focusedId: p,
      setItemRef: f,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: C,
      onCloseFlyout: u
    }
  ))));
}, be = ({
  item: r,
  activeItemId: t,
  collapsed: l,
  onSelect: n,
  onExecute: s,
  onToggleGroup: a,
  focusedId: o,
  setItemRef: c,
  onItemFocus: i,
  groupStates: d,
  flyoutGroupId: p,
  onOpenFlyout: f,
  onCloseFlyout: b
}) => {
  switch (r.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        st,
        {
          item: r,
          active: r.id === t,
          collapsed: l,
          onSelect: n,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ct,
        {
          item: r,
          collapsed: l,
          onExecute: s,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(it, { item: r, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(dt, null);
    case "group": {
      const k = d ? d.get(r.id) ?? r.expanded : r.expanded;
      return /* @__PURE__ */ e.createElement(
        mt,
        {
          item: r,
          expanded: k,
          activeItemId: t,
          collapsed: l,
          onSelect: n,
          onExecute: s,
          onToggleGroup: a,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i,
          focusedId: o,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: p,
          onOpenFlyout: f,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, pt = ({ controlId: r }) => {
  const t = x(), l = I(), n = O(rt), s = t.items ?? [], a = t.activeItemId, o = t.collapsed, [c, i] = ae(() => {
    const g = /* @__PURE__ */ new Map(), E = (S) => {
      for (const N of S)
        N.type === "group" && (g.set(N.id, N.expanded), E(N.children));
    };
    return E(s), g;
  }), d = z((g) => {
    i((E) => {
      const S = new Map(E), N = S.get(g) ?? !1;
      return S.set(g, !N), l("toggleGroup", { itemId: g, expanded: !N }), S;
    });
  }, [l]), p = z((g) => {
    g !== a && l("selectItem", { itemId: g });
  }, [l, a]), f = z((g) => {
    l("executeCommand", { itemId: g });
  }, [l]), b = z(() => {
    l("toggleCollapse", {});
  }, [l]), [k, C] = ae(null), u = z((g) => {
    C(g);
  }, []), m = z(() => {
    C(null);
  }, []);
  J(() => {
    o || C(null);
  }, [o]);
  const [h, _] = ae(() => {
    const g = re(s, o, c);
    return g.length > 0 ? g[0].id : "";
  }), B = oe(/* @__PURE__ */ new Map()), P = z((g) => (E) => {
    E ? B.current.set(g, E) : B.current.delete(g);
  }, []), w = z((g) => {
    _(g);
  }, []), D = oe(0), j = z((g) => {
    _(g), D.current++;
  }, []);
  J(() => {
    const g = B.current.get(h);
    g && document.activeElement !== g && g.focus();
  }, [h, D.current]);
  const W = z((g) => {
    if (g.key === "Escape" && k !== null) {
      g.preventDefault(), m();
      return;
    }
    const E = re(s, o, c);
    if (E.length === 0) return;
    const S = E.findIndex((F) => F.id === h);
    if (S < 0) return;
    const N = E[S];
    switch (g.key) {
      case "ArrowDown": {
        g.preventDefault();
        const F = (S + 1) % E.length;
        j(E[F].id);
        break;
      }
      case "ArrowUp": {
        g.preventDefault();
        const F = (S - 1 + E.length) % E.length;
        j(E[F].id);
        break;
      }
      case "Home": {
        g.preventDefault(), j(E[0].id);
        break;
      }
      case "End": {
        g.preventDefault(), j(E[E.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        g.preventDefault(), N.type === "nav" ? p(N.id) : N.type === "command" ? f(N.id) : N.type === "group" && (o ? k === N.id ? m() : u(N.id) : d(N.id));
        break;
      }
      case "ArrowRight": {
        N.type === "group" && !o && ((c.get(N.id) ?? !1) || (g.preventDefault(), d(N.id)));
        break;
      }
      case "ArrowLeft": {
        N.type === "group" && !o && (c.get(N.id) ?? !1) && (g.preventDefault(), d(N.id));
        break;
      }
    }
  }, [
    s,
    o,
    c,
    h,
    k,
    j,
    p,
    f,
    d,
    u,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: W }, s.map((g) => /* @__PURE__ */ e.createElement(
    be,
    {
      key: g.id,
      item: g,
      activeItemId: a,
      collapsed: o,
      onSelect: p,
      onExecute: f,
      onToggleGroup: d,
      focusedId: h,
      setItemRef: P,
      onItemFocus: w,
      groupStates: c,
      flyoutGroupId: k,
      onOpenFlyout: u,
      onCloseFlyout: m
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: o ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, ft = ({ controlId: r }) => {
  const t = x(), l = t.direction ?? "column", n = t.gap ?? "default", s = t.align ?? "stretch", a = t.wrap === !0, o = t.children ?? [], c = [
    "tlStack",
    `tlStack--${l}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${s}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: c }, o.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i })));
}, bt = ({ controlId: r }) => {
  const t = x(), l = t.columns, n = t.minColumnWidth, s = t.gap ?? "default", a = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : l && (o.gridTemplateColumns = `repeat(${l}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: r, className: `tlGrid tlGrid--gap-${s}`, style: o }, a.map((c, i) => /* @__PURE__ */ e.createElement(T, { key: i, control: c })));
}, ht = ({ controlId: r }) => {
  const t = x(), l = t.title, n = t.variant ?? "outlined", s = t.padding ?? "default", a = t.headerActions ?? [], o = t.child, c = l != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: `tlCard tlCard--${n}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, l && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, l), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(T, { control: o })));
}, _t = ({ controlId: r }) => {
  const t = x(), l = t.title ?? "", n = t.leading, s = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: r, className: c }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(T, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, l), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i }))));
}, { useCallback: Et } = e, vt = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.items ?? [], s = Et((a) => {
    l("navigate", { itemId: a });
  }, [l]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((a, o) => {
    const c = o === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => s(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: gt } = e, Ct = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.items ?? [], s = t.activeItemId, a = gt((o) => {
    o !== s && l("selectItem", { itemId: o });
  }, [l, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
    const c = o.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => a(o.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: de, useEffect: ue, useRef: yt } = e, kt = {
  "js.dialog.close": "Close"
}, wt = ({ controlId: r }) => {
  const t = x(), l = I(), n = O(kt), s = t.open === !0, a = t.title ?? "", o = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], d = t.child, p = yt(null), f = de(() => {
    l("close");
  }, [l]), b = de((C) => {
    c && C.target === C.currentTarget && f();
  }, [c, f]);
  if (ue(() => {
    if (!s) return;
    const C = (u) => {
      u.key === "Escape" && f();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [s, f]), ue(() => {
    s && p.current && p.current.focus();
  }, [s]), !s) return null;
  const k = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(T, { control: d })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((C, u) => /* @__PURE__ */ e.createElement(T, { key: u, control: C })))
  ));
}, { useCallback: Nt, useEffect: Lt } = e, St = {
  "js.drawer.close": "Close"
}, xt = ({ controlId: r }) => {
  const t = x(), l = I(), n = O(St), s = t.open === !0, a = t.position ?? "right", o = t.size ?? "medium", c = t.title ?? null, i = t.child, d = Nt(() => {
    l("close");
  }, [l]);
  Lt(() => {
    if (!s) return;
    const f = (b) => {
      b.key === "Escape" && d();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [s, d]);
  const p = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${o}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: r, className: p, "aria-hidden": !s }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(T, { control: i })));
}, { useCallback: me, useEffect: Tt, useState: Rt } = e, Dt = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.message ?? "", s = t.variant ?? "info", a = t.action, o = t.duration ?? 5e3, c = t.visible === !0, [i, d] = Rt(!1), p = me(() => {
    d(!0), setTimeout(() => {
      l("dismiss"), d(!1);
    }, 200);
  }, [l]), f = me(() => {
    a && l(a.commandName), p();
  }, [l, a, p]);
  return Tt(() => {
    if (!c || o === 0) return;
    const b = setTimeout(p, o);
    return () => clearTimeout(b);
  }, [c, o, p]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: f }, a.label)
  );
}, { useCallback: ne, useEffect: le, useRef: Bt, useState: pe } = e, Pt = ({ controlId: r }) => {
  const t = x(), l = I(), n = t.open === !0, s = t.anchorId, a = t.items ?? [], o = Bt(null), [c, i] = pe({ top: 0, left: 0 }), [d, p] = pe(0), f = a.filter((u) => u.type === "item" && !u.disabled);
  le(() => {
    var w, D;
    if (!n || !s) return;
    const u = document.getElementById(s);
    if (!u) return;
    const m = u.getBoundingClientRect(), h = ((w = o.current) == null ? void 0 : w.offsetHeight) ?? 200, _ = ((D = o.current) == null ? void 0 : D.offsetWidth) ?? 200;
    let B = m.bottom + 4, P = m.left;
    B + h > window.innerHeight && (B = m.top - h - 4), P + _ > window.innerWidth && (P = m.right - _), i({ top: B, left: P }), p(0);
  }, [n, s]);
  const b = ne(() => {
    l("close");
  }, [l]), k = ne((u) => {
    l("selectItem", { itemId: u });
  }, [l]);
  le(() => {
    if (!n) return;
    const u = (m) => {
      o.current && !o.current.contains(m.target) && b();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [n, b]);
  const C = ne((u) => {
    if (u.key === "Escape") {
      b();
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
  }, [b, k, f, d]);
  return le(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: C
    },
    a.map((u, m) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: m, className: "tlMenu__separator" });
      const _ = f.indexOf(u) === d;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (_ ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: _ ? 0 : -1,
          onClick: () => k(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, jt = ({ controlId: r }) => {
  const t = x(), l = t.header, n = t.content, s = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAppShell" }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(T, { control: l })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(T, { control: n })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(T, { control: s })), /* @__PURE__ */ e.createElement(T, { control: a }));
}, Ft = () => {
  const t = x().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, fe = 50, Mt = () => {
  const r = x(), t = I(), l = r.columns ?? [], n = r.totalRowCount ?? 0, s = r.rows ?? [], a = r.rowHeight ?? 36, o = r.selectionMode ?? "single", c = r.selectedCount ?? 0, i = o === "multi", d = 40, p = e.useRef(null), f = e.useRef(null), b = e.useRef(null), [k, C] = e.useState({}), u = e.useRef(null), m = e.useRef(!1), h = e.useRef(null), [_, B] = e.useState(null);
  e.useEffect(() => {
    u.current || C({});
  }, [l]);
  const P = (v) => k[v.name] ?? v.width, w = n * a, D = e.useCallback((v, y, R) => {
    R.preventDefault(), R.stopPropagation(), u.current = { column: v, startX: R.clientX, startWidth: y };
    const M = ($) => {
      const V = u.current;
      if (!V) return;
      const Q = Math.max(fe, V.startWidth + ($.clientX - V.startX));
      C((_e) => ({ ..._e, [V.column]: Q }));
    }, A = ($) => {
      document.removeEventListener("mousemove", M), document.removeEventListener("mouseup", A);
      const V = u.current;
      if (V) {
        const Q = Math.max(fe, V.startWidth + ($.clientX - V.startX));
        t("columnResize", { column: V.column, width: Q }), u.current = null, m.current = !0, requestAnimationFrame(() => {
          m.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", M), document.addEventListener("mouseup", A);
  }, [t]), j = e.useCallback(() => {
    p.current && f.current && (p.current.scrollLeft = f.current.scrollLeft), b.current !== null && clearTimeout(b.current), b.current = window.setTimeout(() => {
      const v = f.current;
      if (!v) return;
      const y = v.scrollTop, R = Math.ceil(v.clientHeight / a), M = Math.floor(y / a);
      t("scroll", { start: M, count: R });
    }, 80);
  }, [t, a]), W = e.useCallback((v, y) => {
    if (m.current) return;
    let R;
    !y || y === "desc" ? R = "asc" : R = "desc", t("sort", { column: v, direction: R });
  }, [t]), g = e.useCallback((v, y) => {
    h.current = v, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", v);
  }, []), E = e.useCallback((v, y) => {
    if (!h.current || h.current === v) {
      B(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const R = y.currentTarget.getBoundingClientRect(), M = y.clientX < R.left + R.width / 2 ? "left" : "right";
    B({ column: v, side: M });
  }, []), S = e.useCallback((v) => {
    v.preventDefault();
    const y = h.current;
    if (!y || !_) {
      h.current = null, B(null);
      return;
    }
    let R = l.findIndex((A) => A.name === _.column);
    if (R < 0) {
      h.current = null, B(null);
      return;
    }
    const M = l.findIndex((A) => A.name === y);
    _.side === "right" && R++, M < R && R--, t("columnReorder", { column: y, targetIndex: R }), h.current = null, B(null);
  }, [l, _, t]), N = e.useCallback(() => {
    h.current = null, B(null);
  }, []), F = e.useCallback((v, y) => {
    y.shiftKey && y.preventDefault(), t("select", {
      rowIndex: v,
      ctrlKey: y.ctrlKey || y.metaKey,
      shiftKey: y.shiftKey
    });
  }, [t]), H = e.useCallback((v, y) => {
    y.stopPropagation(), t("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [t]), K = e.useCallback(() => {
    const v = c === n && n > 0;
    t("selectAll", { selected: !v });
  }, [t, c, n]), U = l.reduce((v, y) => v + P(y), 0) + (i ? d : 0), q = c === n && n > 0, Y = c > 0 && c < n, Z = e.useCallback((v) => {
    v && (v.indeterminate = Y);
  }, [Y]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (v) => {
        if (!h.current) return;
        const y = f.current, R = p.current;
        if (!y) return;
        const M = y.getBoundingClientRect(), A = 40, $ = 8;
        v.clientX < M.left + A ? y.scrollLeft = Math.max(0, y.scrollLeft - $) : v.clientX > M.right - A && (y.scrollLeft += $), R && (R.scrollLeft = y.scrollLeft);
      }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: p }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: U } }, i && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell",
        style: { width: d, minWidth: d }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Z,
          className: "tlTableView__checkbox",
          checked: q,
          onChange: K
        }
      )
    ), l.map((v, y) => {
      const R = P(v), M = y === l.length - 1;
      let A = "tlTableView__headerCell";
      return v.sortable && (A += " tlTableView__headerCell--sortable"), _ && _.column === v.name && (A += " tlTableView__headerCell--dragOver-" + _.side), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.name,
          className: A,
          style: M ? { flex: "1 0 auto", minWidth: R, position: "relative" } : { width: R, minWidth: R, position: "relative" },
          draggable: !0,
          onClick: v.sortable ? () => W(v.name, v.sortDirection) : void 0,
          onDragStart: ($) => g(v.name, $),
          onDragOver: ($) => E(v.name, $),
          onDrop: S,
          onDragEnd: N
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, v.label),
        v.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, v.sortDirection === "asc" ? "▲" : "▼"),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: ($) => D(v.name, R, $)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (v) => {
          if (h.current && l.length > 0) {
            const y = l[l.length - 1];
            y.name !== h.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", B({ column: y.name, side: "right" }));
          }
        },
        onDrop: S
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: j
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: w, position: "relative", minWidth: U } }, s.map((v) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.id,
          className: "tlTableView__row" + (v.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: v.index * a,
            height: a,
            minWidth: U,
            width: "100%"
          },
          onClick: (y) => F(v.index, y)
        },
        i && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell",
            style: { width: d, minWidth: d },
            onClick: (y) => y.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: v.selected,
              onChange: () => {
              },
              onClick: (y) => H(v.index, y),
              tabIndex: -1
            }
          )
        ),
        l.map((y, R) => {
          const M = P(y), A = R === l.length - 1;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: y.name,
              className: "tlTableView__cell",
              style: A ? { flex: "1 0 auto", minWidth: M } : { width: M, minWidth: M }
            },
            /* @__PURE__ */ e.createElement(T, { control: v.cells[y.name] })
          );
        })
      )))
    )
  );
}, It = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, he = e.createContext(It), { useMemo: At, useRef: $t, useState: zt, useEffect: Ot } = e, Ut = 320, Wt = ({ controlId: r }) => {
  const t = x(), l = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", s = t.readOnly === !0, a = t.children ?? [], o = $t(null), [c, i] = zt(
    n === "top" ? "top" : "side"
  );
  Ot(() => {
    if (n !== "auto") {
      i(n);
      return;
    }
    const k = o.current;
    if (!k) return;
    const C = new ResizeObserver((u) => {
      for (const m of u) {
        const _ = m.contentRect.width / l;
        i(_ < Ut ? "top" : "side");
      }
    });
    return C.observe(k), () => C.disconnect();
  }, [n, l]);
  const d = At(() => ({
    readOnly: s,
    resolvedLabelPosition: c
  }), [s, c]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / l))}rem`}, 1fr))`
  }, b = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(he.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: r, className: b, style: f, ref: o }, a.map((k, C) => /* @__PURE__ */ e.createElement(T, { key: C, control: k }))));
}, { useCallback: Vt } = e, Ht = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Kt = ({ controlId: r }) => {
  const t = x(), l = I(), n = O(Ht), s = t.header, a = t.headerActions ?? [], o = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", d = t.fullLine === !0, p = t.children ?? [], f = s != null || a.length > 0 || o, b = Vt(() => {
    l("toggleCollapse");
  }, [l]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    d ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: k }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, a.map((C, u) => /* @__PURE__ */ e.createElement(T, { key: u, control: C })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((C, u) => /* @__PURE__ */ e.createElement(T, { key: u, control: C }))));
}, { useContext: Gt, useState: Yt, useCallback: Xt } = e, qt = ({ controlId: r }) => {
  const t = x(), l = Gt(he), n = t.label ?? "", s = t.required === !0, a = t.error, o = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? l.resolvedLabelPosition, d = t.fullLine === !0, p = t.visible !== !1, f = t.field, b = l.readOnly, [k, C] = Yt(!1), u = Xt(() => C((_) => !_), []);
  if (!p) return null;
  const m = a != null, h = [
    "tlFormField",
    `tlFormField--${i}`,
    b ? "tlFormField--readonly" : "",
    d ? "tlFormField--fullLine" : "",
    m ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: h }, c && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__dirtyBar" }), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), s && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && !b && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(T, { control: f })), !b && m && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, a)), !b && o && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
};
L("TLButton", De);
L("TLToggleButton", Pe);
L("TLTextInput", ge);
L("TLNumberInput", ye);
L("TLDatePicker", we);
L("TLSelect", Le);
L("TLCheckbox", xe);
L("TLTable", Te);
L("TLCounter", je);
L("TLTabBar", Me);
L("TLFieldList", Ie);
L("TLAudioRecorder", $e);
L("TLAudioPlayer", Oe);
L("TLFileUpload", We);
L("TLDownload", He);
L("TLPhotoCapture", Ge);
L("TLPhotoViewer", Xe);
L("TLSplitPanel", qe);
L("TLPanel", nt);
L("TLMaximizeRoot", lt);
L("TLDeckPane", ot);
L("TLSidebar", pt);
L("TLStack", ft);
L("TLGrid", bt);
L("TLCard", ht);
L("TLAppBar", _t);
L("TLBreadcrumb", vt);
L("TLBottomBar", Ct);
L("TLDialog", wt);
L("TLDrawer", xt);
L("TLSnackbar", Dt);
L("TLMenu", Pt);
L("TLAppShell", jt);
L("TLTextCell", Ft);
L("TLTableView", Mt);
L("TLFormLayout", Wt);
L("TLFormGroup", Kt);
L("TLFormField", qt);
