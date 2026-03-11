import { React as e, useTLFieldValue as Ee, getComponent as at, useTLState as W, useTLCommand as ne, TLChild as K, useTLUpload as Be, useI18N as ie, useTLDataUrl as $e, register as F } from "tl-react-bridge";
const { useCallback: ot } = e, st = ({ controlId: l, state: t }) => {
  const [a, n] = Ee(), c = ot(
    (u) => {
      n(u.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: l,
      value: a ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: ct } = e, it = ({ controlId: l, state: t, config: a }) => {
  const [n, c] = Ee(), u = ct(
    (d) => {
      const s = d.target.value, r = s === "" ? null : Number(s);
      c(r);
    },
    [c]
  ), o = a != null && a.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: n != null ? String(n) : "",
      onChange: u,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ut } = e, dt = ({ controlId: l, state: t }) => {
  const [a, n] = Ee(), c = ut(
    (u) => {
      n(u.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: l,
      value: a ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: mt } = e, pt = ({ controlId: l, state: t, config: a }) => {
  var d;
  const [n, c] = Ee(), u = mt(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const s = ((d = o.find((r) => r.value === n)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: n ?? "",
      onChange: u,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: ft } = e, ht = ({ controlId: l, state: t }) => {
  const [a, n] = Ee(), c = ft(
    (u) => {
      n(u.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: a === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: a === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, _t = ({ controlId: l, state: t }) => {
  const a = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, a.map((o) => {
    const d = o.cellModule ? at(o.cellModule) : void 0, s = c[o.name];
    if (d) {
      const r = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: l + "-" + u + "-" + o.name,
          state: r
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: bt } = e, vt = ({ controlId: l, command: t, label: a, disabled: n }) => {
  const c = W(), u = ne(), o = t ?? "click", d = a ?? c.label, s = n ?? c.disabled === !0, r = bt(() => {
    u(o);
  }, [u, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: r,
      disabled: s,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: Et } = e, gt = ({ controlId: l, command: t, label: a, active: n, disabled: c }) => {
  const u = W(), o = ne(), d = t ?? "click", s = a ?? u.label, r = n ?? u.active === !0, m = c ?? u.disabled === !0, p = Et(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : "")
    },
    s
  );
}, Ct = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: yt } = e, wt = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.tabs ?? [], c = t.activeTabId, u = yt((o) => {
    o !== c && a("selectTab", { tabId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => u(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, kt = ({ controlId: l }) => {
  const t = W(), a = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, Nt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, St = ({ controlId: l }) => {
  const t = W(), a = Be(), [n, c] = e.useState("idle"), [u, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, E = m === "received" ? "idle" : n !== "idle" ? n : m, R = e.useCallback(async () => {
    if (n === "recording") {
      const S = d.current;
      S && S.state !== "inactive" && S.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = S, s.current = [];
        const O = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(S, O ? { mimeType: O } : void 0);
        d.current = P, P.ondataavailable = (f) => {
          f.data.size > 0 && s.current.push(f.data);
        }, P.onstop = async () => {
          S.getTracks().forEach((C) => C.stop()), r.current = null;
          const f = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], f.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", f, "recording.webm"), await a(g), c("idle");
        }, P.start(), c("recording");
      } catch (S) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", S), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, a]), _ = ie(Nt), b = E === "recording" ? _["js.audioRecorder.stop"] : E === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], h = E === "uploading", N = ["tlAudioRecorder__button"];
  return E === "recording" && N.push("tlAudioRecorder__button--recording"), E === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: R,
      disabled: h,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${E === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[u]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Tt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Rt = ({ controlId: l }) => {
  const t = W(), a = $e(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [u, o] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
  e.useEffect(() => {
    n ? u === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    c !== r.current && (r.current = c, d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (u === "playing" || u === "paused" || u === "loading") && o("idle"));
  }, [c]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (u === "disabled" || u === "loading")
      return;
    if (u === "playing") {
      d.current && d.current.pause(), o("paused");
      return;
    }
    if (u === "paused" && d.current) {
      d.current.play(), o("playing");
      return;
    }
    if (!s.current) {
      o("loading");
      try {
        const h = await fetch(a);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), o("idle");
          return;
        }
        const N = await h.blob();
        s.current = URL.createObjectURL(N);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), o("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    d.current = b, b.onended = () => {
      o("idle");
    }, b.play(), o("playing");
  }, [u, a]), p = ie(Tt), E = u === "loading" ? p["js.loading"] : u === "playing" ? p["js.audioPlayer.pause"] : u === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], R = u === "disabled" || u === "loading", _ = ["tlAudioPlayer__button"];
  return u === "playing" && _.push("tlAudioPlayer__button--playing"), u === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: m,
      disabled: R,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Dt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Lt = ({ controlId: l }) => {
  const t = W(), a = Be(), [n, c] = e.useState("idle"), [u, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : n !== "idle" ? n : s, E = e.useCallback(async (f) => {
    c("uploading");
    const g = new FormData();
    g.append("file", f, f.name), await a(g), c("idle");
  }, [a]), R = e.useCallback((f) => {
    var C;
    const g = (C = f.target.files) == null ? void 0 : C[0];
    g && E(g);
  }, [E]), _ = e.useCallback(() => {
    var f;
    n !== "uploading" && ((f = d.current) == null || f.click());
  }, [n]), b = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), h = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), N = e.useCallback((f) => {
    var C;
    if (f.preventDefault(), f.stopPropagation(), o(!1), n === "uploading") return;
    const g = (C = f.dataTransfer.files) == null ? void 0 : C[0];
    g && E(g);
  }, [n, E]), S = p === "uploading", O = ie(Dt), P = p === "uploading" ? O["js.uploading"] : O["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: h,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: m || void 0,
        onChange: R,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: S,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, xt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, It = ({ controlId: l }) => {
  const t = W(), a = $e(), n = ne(), c = !!t.hasData, u = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const _ = a + (a.includes("?") ? "&" : "?") + "rev=" + u, b = await fetch(_);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const h = await b.blob(), N = URL.createObjectURL(h), S = document.createElement("a");
        S.href = N, S.download = o, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(N);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, u, o]), p = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), E = ie(xt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, E["js.download.noFile"]));
  const R = s ? E["js.downloading"] : E["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: E["js.download.clear"],
      "aria-label": E["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Pt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, jt = ({ controlId: l }) => {
  const t = W(), a = Be(), [n, c] = e.useState("idle"), [u, o] = e.useState(null), [d, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), E = e.useRef(null), R = e.useRef(null), _ = t.error, b = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    h(), c("idle");
  }, [h]), S = e.useCallback(async () => {
    var T;
    if (n !== "uploading") {
      if (o(null), !b) {
        (T = E.current) == null || T.click();
        return;
      }
      try {
        const M = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = M, c("overlayOpen");
      } catch (M) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", M), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [n, b]), O = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const T = r.current, M = p.current;
    if (!T || !M)
      return;
    M.width = T.videoWidth, M.height = T.videoHeight;
    const I = M.getContext("2d");
    I && (I.drawImage(T, 0, 0), h(), c("uploading"), M.toBlob(async (Y) => {
      if (!Y) {
        c("idle");
        return;
      }
      const te = new FormData();
      te.append("photo", Y, "capture.jpg"), await a(te), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, a, h]), P = e.useCallback(async (T) => {
    var Y;
    const M = (Y = T.target.files) == null ? void 0 : Y[0];
    if (!M) return;
    c("uploading");
    const I = new FormData();
    I.append("photo", M, M.name), await a(I), c("idle"), E.current && (E.current.value = "");
  }, [a]);
  e.useEffect(() => {
    n === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var M;
    if (n !== "overlayOpen") return;
    (M = R.current) == null || M.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const T = (M) => {
      M.key === "Escape" && N();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [n, N]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const f = ie(Pt), g = n === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const X = ["tlPhotoCapture__overlayVideo"];
  d && X.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return d && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: S,
      disabled: n === "uploading",
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !b && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: E,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: R,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: r,
        className: X.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: k.join(" "),
        onClick: () => s((T) => !T),
        title: f["js.photoCapture.mirror"],
        "aria-label": f["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: O,
        title: f["js.photoCapture.capture"],
        "aria-label": f["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: f["js.photoCapture.close"],
        "aria-label": f["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[u]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, Mt = {
  "js.photoViewer.alt": "Captured photo"
}, At = ({ controlId: l }) => {
  const t = W(), a = $e(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [u, o] = e.useState(null), d = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      u && (URL.revokeObjectURL(u), o(null));
      return;
    }
    if (c === d.current && u)
      return;
    d.current = c, u && (URL.revokeObjectURL(u), o(null));
    let r = !1;
    return (async () => {
      try {
        const m = await fetch(a);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        r || o(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      r = !0;
    };
  }, [n, c, a]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const s = ie(Mt);
  return !n || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ze, useRef: Se } = e, Ot = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.orientation, c = t.resizable === !0, u = t.children ?? [], o = n === "horizontal", d = u.length > 0 && u.every((h) => h.collapsed), s = !d && u.some((h) => h.collapsed), r = d ? !o : o, m = Se(null), p = Se(null), E = Se(null), R = ze((h, N) => {
    const S = {
      overflow: h.scrolling || "auto"
    };
    return h.collapsed ? d && !r ? S.flex = "1 0 0%" : S.flex = "0 0 auto" : N !== void 0 ? S.flex = `0 0 ${N}px` : h.unit === "%" || s ? S.flex = `${h.size} 0 0%` : S.flex = `0 0 ${h.size}px`, h.minSize > 0 && !h.collapsed && (S.minWidth = o ? h.minSize : void 0, S.minHeight = o ? void 0 : h.minSize), S;
  }, [o, d, s, r]), _ = ze((h, N) => {
    h.preventDefault();
    const S = m.current;
    if (!S) return;
    const O = u[N], P = u[N + 1], f = S.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    f.forEach((k) => {
      g.push(o ? k.offsetWidth : k.offsetHeight);
    }), E.current = g, p.current = {
      splitterIndex: N,
      startPos: o ? h.clientX : h.clientY,
      startSizeBefore: g[N],
      startSizeAfter: g[N + 1],
      childBefore: O,
      childAfter: P
    };
    const C = (k) => {
      const T = p.current;
      if (!T || !E.current) return;
      const I = (o ? k.clientX : k.clientY) - T.startPos, Y = T.childBefore.minSize || 0, te = T.childAfter.minSize || 0;
      let le = T.startSizeBefore + I, Z = T.startSizeAfter - I;
      le < Y && (Z += le - Y, le = Y), Z < te && (le += Z - te, Z = te), E.current[T.splitterIndex] = le, E.current[T.splitterIndex + 1] = Z;
      const se = S.querySelectorAll(":scope > .tlSplitPanel__child"), B = se[T.splitterIndex], z = se[T.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${le}px`), z && (z.style.flex = `0 0 ${Z}px`);
    }, X = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", X), document.body.style.cursor = "", document.body.style.userSelect = "", E.current) {
        const k = {};
        u.forEach((T, M) => {
          const I = T.control;
          I != null && I.controlId && E.current && (k[I.controlId] = E.current[M]);
        }), a("updateSizes", { sizes: k });
      }
      E.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", X), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, o, a]), b = [];
  return u.forEach((h, N) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${h.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: R(h)
        },
        /* @__PURE__ */ e.createElement(K, { control: h.control })
      )
    ), c && N < u.length - 1) {
      const S = u[N + 1];
      !h.collapsed && !S.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => _(P, N)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: r ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    b
  );
}, { useCallback: Te } = e, Bt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, $t = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Ft = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ht = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ut = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Vt = ({ controlId: l }) => {
  const t = W(), a = ne(), n = ie(Bt), c = t.title, u = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, r = t.toolbarButtons ?? [], m = u === "MINIMIZED", p = u === "MAXIMIZED", E = u === "HIDDEN", R = Te(() => {
    a("toggleMinimize");
  }, [a]), _ = Te(() => {
    a("toggleMaximize");
  }, [a]), b = Te(() => {
    a("popOut");
  }, [a]);
  if (E)
    return null;
  const h = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}`,
      style: h
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, r.map((N, S) => /* @__PURE__ */ e.createElement("span", { key: S, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(K, { control: N }))), o && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: R,
        title: m ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Ft, null) : /* @__PURE__ */ e.createElement($t, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: p ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Ht, null) : /* @__PURE__ */ e.createElement(zt, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ut, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child }))
  );
}, Wt = ({ controlId: l }) => {
  const t = W();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, Kt = ({ controlId: l }) => {
  const t = W();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ue, useState: ye, useEffect: we, useRef: ke } = e, Yt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Me(l, t, a, n) {
  const c = [];
  for (const u of l)
    u.type === "nav" ? c.push({ id: u.id, type: "nav", groupId: n }) : u.type === "command" ? c.push({ id: u.id, type: "command", groupId: n }) : u.type === "group" && (c.push({ id: u.id, type: "group" }), (a.get(u.id) ?? u.expanded) && !t && c.push(...Me(u.children, t, a, u.id)));
  return c;
}
const _e = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Gt = ({ item: l, active: t, collapsed: a, onSelect: n, tabIndex: c, itemRef: u, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: a ? l.label : void 0,
    tabIndex: c,
    ref: u,
    onFocus: () => o(l.id)
  },
  a && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(_e, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !a && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), qt = ({ item: l, collapsed: t, onExecute: a, tabIndex: n, itemRef: c, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Xt = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(_e, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Zt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Qt = ({ item: l, activeItemId: t, anchorRect: a, onSelect: n, onExecute: c, onClose: u }) => {
  const o = ke(null);
  we(() => {
    const r = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [u]), we(() => {
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [u]);
  const d = ue((r) => {
    r.type === "nav" ? (n(r.id), u()) : r.type === "command" && (c(r.id), u());
  }, [n, c, u]), s = {};
  return a && (s.left = a.right, s.top = a.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((r) => {
    if (r.type === "nav" || r.type === "command") {
      const m = r.type === "nav" && r.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: r.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(r)
        },
        /* @__PURE__ */ e.createElement(_e, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, Jt = ({
  item: l,
  expanded: t,
  activeItemId: a,
  collapsed: n,
  onSelect: c,
  onExecute: u,
  onToggleGroup: o,
  tabIndex: d,
  itemRef: s,
  onFocus: r,
  focusedId: m,
  setItemRef: p,
  onItemFocus: E,
  flyoutGroupId: R,
  onOpenFlyout: _,
  onCloseFlyout: b
}) => {
  const h = ke(null), [N, S] = ye(null), O = ue(() => {
    n ? R === l.id ? b() : (h.current && S(h.current.getBoundingClientRect()), _(l.id)) : o(l.id);
  }, [n, R, l.id, o, _, b]), P = ue((g) => {
    h.current = g, s(g);
  }, [s]), f = n && R === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: O,
      title: n ? l.label : void 0,
      "aria-expanded": n ? f : t,
      tabIndex: d,
      ref: P,
      onFocus: () => r(l.id)
    },
    /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
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
  ), f && /* @__PURE__ */ e.createElement(
    Qt,
    {
      item: l,
      activeItemId: a,
      anchorRect: N,
      onSelect: c,
      onExecute: u,
      onClose: b
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((g) => /* @__PURE__ */ e.createElement(
    Qe,
    {
      key: g.id,
      item: g,
      activeItemId: a,
      collapsed: n,
      onSelect: c,
      onExecute: u,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: E,
      groupStates: null,
      flyoutGroupId: R,
      onOpenFlyout: _,
      onCloseFlyout: b
    }
  ))));
}, Qe = ({
  item: l,
  activeItemId: t,
  collapsed: a,
  onSelect: n,
  onExecute: c,
  onToggleGroup: u,
  focusedId: o,
  setItemRef: d,
  onItemFocus: s,
  groupStates: r,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: E
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Gt,
        {
          item: l,
          active: l.id === t,
          collapsed: a,
          onSelect: n,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        qt,
        {
          item: l,
          collapsed: a,
          onExecute: c,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Xt, { item: l, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(Zt, null);
    case "group": {
      const R = r ? r.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Jt,
        {
          item: l,
          expanded: R,
          activeItemId: t,
          collapsed: a,
          onSelect: n,
          onExecute: c,
          onToggleGroup: u,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: d,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: E
        }
      );
    }
    default:
      return null;
  }
}, en = ({ controlId: l }) => {
  const t = W(), a = ne(), n = ie(Yt), c = t.items ?? [], u = t.activeItemId, o = t.collapsed, [d, s] = ye(() => {
    const k = /* @__PURE__ */ new Map(), T = (M) => {
      for (const I of M)
        I.type === "group" && (k.set(I.id, I.expanded), T(I.children));
    };
    return T(c), k;
  }), r = ue((k) => {
    s((T) => {
      const M = new Map(T), I = M.get(k) ?? !1;
      return M.set(k, !I), a("toggleGroup", { itemId: k, expanded: !I }), M;
    });
  }, [a]), m = ue((k) => {
    k !== u && a("selectItem", { itemId: k });
  }, [a, u]), p = ue((k) => {
    a("executeCommand", { itemId: k });
  }, [a]), E = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [R, _] = ye(null), b = ue((k) => {
    _(k);
  }, []), h = ue(() => {
    _(null);
  }, []);
  we(() => {
    o || _(null);
  }, [o]);
  const [N, S] = ye(() => {
    const k = Me(c, o, d);
    return k.length > 0 ? k[0].id : "";
  }), O = ke(/* @__PURE__ */ new Map()), P = ue((k) => (T) => {
    T ? O.current.set(k, T) : O.current.delete(k);
  }, []), f = ue((k) => {
    S(k);
  }, []), g = ke(0), C = ue((k) => {
    S(k), g.current++;
  }, []);
  we(() => {
    const k = O.current.get(N);
    k && document.activeElement !== k && k.focus();
  }, [N, g.current]);
  const X = ue((k) => {
    if (k.key === "Escape" && R !== null) {
      k.preventDefault(), h();
      return;
    }
    const T = Me(c, o, d);
    if (T.length === 0) return;
    const M = T.findIndex((Y) => Y.id === N);
    if (M < 0) return;
    const I = T[M];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const Y = (M + 1) % T.length;
        C(T[Y].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const Y = (M - 1 + T.length) % T.length;
        C(T[Y].id);
        break;
      }
      case "Home": {
        k.preventDefault(), C(T[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), C(T[T.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), I.type === "nav" ? m(I.id) : I.type === "command" ? p(I.id) : I.type === "group" && (o ? R === I.id ? h() : b(I.id) : r(I.id));
        break;
      }
      case "ArrowRight": {
        I.type === "group" && !o && ((d.get(I.id) ?? !1) || (k.preventDefault(), r(I.id)));
        break;
      }
      case "ArrowLeft": {
        I.type === "group" && !o && (d.get(I.id) ?? !1) && (k.preventDefault(), r(I.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    N,
    R,
    C,
    m,
    p,
    r,
    b,
    h
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: X }, c.map((k) => /* @__PURE__ */ e.createElement(
    Qe,
    {
      key: k.id,
      item: k,
      activeItemId: u,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: N,
      setItemRef: P,
      onItemFocus: f,
      groupStates: d,
      flyoutGroupId: R,
      onOpenFlyout: b,
      onCloseFlyout: h
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: E,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, tn = ({ controlId: l }) => {
  const t = W(), a = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", u = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, o.map((s, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: s })));
}, nn = ({ controlId: l }) => {
  const t = W(), a = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", u = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, u.map((d, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: d })));
}, rn = ({ controlId: l }) => {
  const t = W(), a = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", u = t.headerActions ?? [], o = t.child, d = a != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((s, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: o })));
}, ln = ({ controlId: l }) => {
  const t = W(), a = t.title ?? "", n = t.leading, c = t.actions ?? [], u = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(K, { key: r, control: s }))));
}, { useCallback: an } = e, on = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.items ?? [], c = an((u) => {
    a("navigate", { itemId: u });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((u, o) => {
    const d = o === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: u.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), d ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, u.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: sn } = e, cn = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.items ?? [], c = t.activeItemId, u = sn((o) => {
    o !== c && a("selectItem", { itemId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
    const d = o.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => u(o.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: He, useEffect: Ue, useRef: un } = e, dn = {
  "js.dialog.close": "Close"
}, mn = ({ controlId: l }) => {
  const t = W(), a = ne(), n = ie(dn), c = t.open === !0, u = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, s = t.actions ?? [], r = t.child, m = un(null), p = He(() => {
    a("close");
  }, [a]), E = He((_) => {
    d && _.target === _.currentTarget && p();
  }, [d, p]);
  if (Ue(() => {
    if (!c) return;
    const _ = (b) => {
      b.key === "Escape" && p();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [c, p]), Ue(() => {
    c && m.current && m.current.focus();
  }, [c]), !c) return null;
  const R = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: E }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": R,
      ref: m,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: R }, u), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: p,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(K, { control: r })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((_, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: _ })))
  ));
}, { useCallback: pn, useEffect: fn } = e, hn = {
  "js.drawer.close": "Close"
}, _n = ({ controlId: l }) => {
  const t = W(), a = ne(), n = ie(hn), c = t.open === !0, u = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, r = pn(() => {
    a("close");
  }, [a]);
  fn(() => {
    if (!c) return;
    const p = (E) => {
      E.key === "Escape" && r();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, r]);
  const m = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: r,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(K, { control: s })));
}, { useCallback: Ve, useEffect: bn, useState: vn } = e, En = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.message ?? "", c = t.content ?? "", u = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, [r, m] = vn(!1), p = Ve(() => {
    m(!0), setTimeout(() => {
      a("dismiss"), m(!1);
    }, 200);
  }, [a]), E = Ve(() => {
    o && a(o.commandName), p();
  }, [a, o, p]);
  return bn(() => {
    if (!s || d === 0) return;
    const R = setTimeout(p, d);
    return () => clearTimeout(R);
  }, [s, d, p]), !s && !r ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${u}${r ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: E }, o.label)
  );
}, { useCallback: Re, useEffect: De, useRef: gn, useState: We } = e, Cn = ({ controlId: l }) => {
  const t = W(), a = ne(), n = t.open === !0, c = t.anchorId, u = t.items ?? [], o = gn(null), [d, s] = We({ top: 0, left: 0 }), [r, m] = We(0), p = u.filter((b) => b.type === "item" && !b.disabled);
  De(() => {
    var f, g;
    if (!n || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const h = b.getBoundingClientRect(), N = ((f = o.current) == null ? void 0 : f.offsetHeight) ?? 200, S = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let O = h.bottom + 4, P = h.left;
    O + N > window.innerHeight && (O = h.top - N - 4), P + S > window.innerWidth && (P = h.right - S), s({ top: O, left: P }), m(0);
  }, [n, c]);
  const E = Re(() => {
    a("close");
  }, [a]), R = Re((b) => {
    a("selectItem", { itemId: b });
  }, [a]);
  De(() => {
    if (!n) return;
    const b = (h) => {
      o.current && !o.current.contains(h.target) && E();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [n, E]);
  const _ = Re((b) => {
    if (b.key === "Escape") {
      E();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), m((h) => (h + 1) % p.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), m((h) => (h - 1 + p.length) % p.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const h = p[r];
      h && R(h.id);
    }
  }, [E, R, p, r]);
  return De(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: _
    },
    u.map((b, h) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: h, className: "tlMenu__separator" });
      const S = p.indexOf(b) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (S ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: S ? 0 : -1,
          onClick: () => R(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, yn = ({ controlId: l }) => {
  const t = W(), a = t.header, n = t.content, c = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: c })), /* @__PURE__ */ e.createElement(K, { control: u }));
}, wn = () => {
  const t = W().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, kn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ke = 50, Nn = () => {
  const l = W(), t = ne(), a = ie(kn), n = l.columns ?? [], c = l.totalRowCount ?? 0, u = l.rows ?? [], o = l.rowHeight ?? 36, d = l.selectionMode ?? "single", s = l.selectedCount ?? 0, r = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, p = e.useMemo(
    () => n.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [n]
  ), E = d === "multi", R = 40, _ = 20, b = e.useRef(null), h = e.useRef(null), N = e.useRef(null), [S, O] = e.useState({}), P = e.useRef(null), f = e.useRef(!1), g = e.useRef(null), [C, X] = e.useState(null), [k, T] = e.useState(null);
  e.useEffect(() => {
    P.current || O({});
  }, [n]);
  const M = e.useCallback((w) => S[w.name] ?? w.width, [S]), I = e.useMemo(() => {
    const w = [];
    let D = E && r > 0 ? R : 0;
    for (let H = 0; H < r && H < n.length; H++)
      w.push(D), D += M(n[H]);
    return w;
  }, [n, r, E, R, M]), Y = c * o, te = e.useCallback((w, D, H) => {
    H.preventDefault(), H.stopPropagation(), P.current = { column: w, startX: H.clientX, startWidth: D };
    const J = (y) => {
      const L = P.current;
      if (!L) return;
      const A = Math.max(Ke, L.startWidth + (y.clientX - L.startX));
      O((Q) => ({ ...Q, [L.column]: A }));
    }, ee = (y) => {
      document.removeEventListener("mousemove", J), document.removeEventListener("mouseup", ee);
      const L = P.current;
      if (L) {
        const A = Math.max(Ke, L.startWidth + (y.clientX - L.startX));
        t("columnResize", { column: L.column, width: A }), P.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", J), document.addEventListener("mouseup", ee);
  }, [t]), le = e.useCallback(() => {
    b.current && h.current && (b.current.scrollLeft = h.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const w = h.current;
      if (!w) return;
      const D = w.scrollTop, H = Math.ceil(w.clientHeight / o), J = Math.floor(D / o);
      t("scroll", { start: J, count: H });
    }, 80);
  }, [t, o]), Z = e.useCallback((w, D, H) => {
    if (f.current) return;
    let J;
    !D || D === "desc" ? J = "asc" : J = "desc";
    const ee = H.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: J, mode: ee });
  }, [t]), se = e.useCallback((w, D) => {
    g.current = w, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", w);
  }, []), B = e.useCallback((w, D) => {
    if (!g.current || g.current === w) {
      X(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const H = D.currentTarget.getBoundingClientRect(), J = D.clientX < H.left + H.width / 2 ? "left" : "right";
    X({ column: w, side: J });
  }, []), z = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const D = g.current;
    if (!D || !C) {
      g.current = null, X(null);
      return;
    }
    let H = n.findIndex((ee) => ee.name === C.column);
    if (H < 0) {
      g.current = null, X(null);
      return;
    }
    const J = n.findIndex((ee) => ee.name === D);
    C.side === "right" && H++, J < H && H--, t("columnReorder", { column: D, targetIndex: H }), g.current = null, X(null);
  }, [n, C, t]), G = e.useCallback(() => {
    g.current = null, X(null);
  }, []), i = e.useCallback((w, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), v = e.useCallback((w, D) => {
    D.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), j = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), x = e.useCallback((w, D, H) => {
    H.stopPropagation(), t("expand", { rowIndex: w, expanded: D });
  }, [t]), U = e.useCallback((w, D) => {
    D.preventDefault(), T({ x: D.clientX, y: D.clientY, colIdx: w });
  }, []), V = e.useCallback(() => {
    k && (t("setFrozenColumnCount", { count: k.colIdx + 1 }), T(null));
  }, [k, t]), q = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), T(null);
  }, [t]);
  e.useEffect(() => {
    if (!k) return;
    const w = () => T(null), D = (H) => {
      H.key === "Escape" && T(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", D);
    };
  }, [k]);
  const ae = n.reduce((w, D) => w + M(D), 0) + (E ? R : 0), re = s === c && c > 0, pe = s > 0 && s < c, Ne = e.useCallback((w) => {
    w && (w.indeterminate = pe);
  }, [pe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!g.current) return;
        w.preventDefault();
        const D = h.current, H = b.current;
        if (!D) return;
        const J = D.getBoundingClientRect(), ee = 40, y = 8;
        w.clientX < J.left + ee ? D.scrollLeft = Math.max(0, D.scrollLeft - y) : w.clientX > J.right - ee && (D.scrollLeft += y), H && (H.scrollLeft = D.scrollLeft);
      },
      onDrop: z
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ae } }, E && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: R,
          minWidth: R,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== g.current && X({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ne,
          className: "tlTableView__checkbox",
          checked: re,
          onChange: j
        }
      )
    ), n.map((w, D) => {
      const H = M(w), J = D === n.length - 1;
      let ee = "tlTableView__headerCell";
      w.sortable && (ee += " tlTableView__headerCell--sortable"), C && C.column === w.name && (ee += " tlTableView__headerCell--dragOver-" + C.side);
      const y = D < r, L = D === r - 1;
      return y && (ee += " tlTableView__headerCell--frozen"), L && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ee,
          style: {
            ...J && !y ? { flex: "1 0 auto", minWidth: H } : { width: H, minWidth: H },
            position: y ? "sticky" : "relative",
            ...y ? { left: I[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (A) => Z(w.name, w.sortDirection, A) : void 0,
          onContextMenu: (A) => U(D, A),
          onDragStart: (A) => se(w.name, A),
          onDragOver: (A) => B(w.name, A),
          onDrop: z,
          onDragEnd: G
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", p > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (A) => te(w.name, H, A)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (g.current && n.length > 0) {
            const D = n[n.length - 1];
            D.name !== g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", X({ column: D.name, side: "right" }));
          }
        },
        onDrop: z
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: h,
        className: "tlTableView__body",
        onScroll: le
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: Y, position: "relative", minWidth: ae } }, u.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            minWidth: ae,
            width: "100%"
          },
          onClick: (D) => i(w.index, D)
        },
        E && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: R,
              minWidth: R,
              ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (D) => v(w.index, D),
              tabIndex: -1
            }
          )
        ),
        n.map((D, H) => {
          const J = M(D), ee = H === n.length - 1, y = H < r, L = H === r - 1;
          let A = "tlTableView__cell";
          y && (A += " tlTableView__cell--frozen"), L && (A += " tlTableView__cell--frozenLast");
          const Q = m && H === 0, be = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: A,
              style: {
                ...ee && !y ? { flex: "1 0 auto", minWidth: J } : { width: J, minWidth: J },
                ...y ? { position: "sticky", left: I[H], zIndex: 2 } : {}
              }
            },
            Q ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: be * _ } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (lt) => x(w.index, !w.expanded, lt)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: w.cells[D.name] })) : /* @__PURE__ */ e.createElement(K, { control: w.cells[D.name] })
          );
        })
      )))
    ),
    k && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: k.y, left: k.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      k.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: V }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: q }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Sn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Je = e.createContext(Sn), { useMemo: Tn, useRef: Rn, useState: Dn, useEffect: Ln } = e, xn = 320, In = ({ controlId: l }) => {
  const t = W(), a = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, u = t.children ?? [], o = t.noModelMessage, d = Rn(null), [s, r] = Dn(
    n === "top" ? "top" : "side"
  );
  Ln(() => {
    if (n !== "auto") {
      r(n);
      return;
    }
    const _ = d.current;
    if (!_) return;
    const b = new ResizeObserver((h) => {
      for (const N of h) {
        const O = N.contentRect.width / a;
        r(O < xn ? "top" : "side");
      }
    });
    return b.observe(_), () => b.disconnect();
  }, [n, a]);
  const m = Tn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), E = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, R = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Je.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: R, style: E, ref: d }, u.map((_, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: _ }))));
}, { useCallback: Pn } = e, jn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Mn = ({ controlId: l }) => {
  const t = W(), a = ne(), n = ie(jn), c = t.header, u = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || u.length > 0 || o, E = Pn(() => {
    a("toggleCollapse");
  }, [a]), R = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    r ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: R }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: E,
      "aria-expanded": !d,
      title: d ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: d ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((_, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: _ })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((_, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: _ }))));
}, { useContext: An, useState: On, useCallback: Bn } = e, $n = ({ controlId: l }) => {
  const t = W(), a = An(Je), n = t.label ?? "", c = t.required === !0, u = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? a.resolvedLabelPosition, r = t.fullLine === !0, m = t.visible !== !1, p = t.field, E = a.readOnly, [R, _] = On(!1), b = Bn(() => _((S) => !S), []);
  if (!m) return null;
  const h = u != null, N = [
    "tlFormField",
    `tlFormField--${s}`,
    E ? "tlFormField--readonly" : "",
    r ? "tlFormField--fullLine" : "",
    h ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: b,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: p })), !E && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, u)), !E && o && R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Fn = () => {
  const l = W(), t = ne(), a = l.iconCss, n = l.iconSrc, c = l.label, u = l.cssClass, o = l.tooltip, d = l.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((E) => {
    E.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", u].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, zn = 20, Hn = () => {
  const l = W(), t = ne(), a = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((f, g) => {
    t(g ? "collapse" : "expand", { nodeId: f });
  }, [t]), E = e.useCallback((f, g) => {
    t("select", {
      nodeId: f,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), R = e.useCallback((f, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: f, x: g.clientX, y: g.clientY });
  }, [t]), _ = e.useRef(null), b = e.useCallback((f, g) => {
    const C = g.getBoundingClientRect(), X = f.clientY - C.top, k = C.height / 3;
    return X < k ? "above" : X > k * 2 ? "below" : "within";
  }, []), h = e.useCallback((f, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", f);
  }, []), N = e.useCallback((f, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const C = b(g, g.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t("dragOver", { nodeId: f, position: C }), _.current = null;
    }, 50);
  }, [t, b]), S = e.useCallback((f, g) => {
    g.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const C = b(g, g.currentTarget);
    t("drop", { nodeId: f, position: C });
  }, [t, b]), O = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((f) => {
    if (a.length === 0) return;
    let g = s;
    switch (f.key) {
      case "ArrowDown":
        f.preventDefault(), g = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        f.preventDefault(), g = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const X = C.depth;
            for (let k = s - 1; k >= 0; k--)
              if (a[k].depth < X) {
                g = k;
                break;
              }
          }
        }
        break;
      case "Enter":
        f.preventDefault(), s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: f.ctrlKey || f.metaKey,
          shiftKey: f.shiftKey
        });
        return;
      case " ":
        f.preventDefault(), n === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        f.preventDefault(), g = 0;
        break;
      case "End":
        f.preventDefault(), g = a.length - 1;
        break;
      default:
        return;
    }
    g !== s && r(g);
  }, [s, a, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    a.map((f, g) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: f.id,
        role: "treeitem",
        "aria-expanded": f.expandable ? f.expanded : void 0,
        "aria-selected": f.selected,
        "aria-level": f.depth + 1,
        className: [
          "tlTreeView__node",
          f.selected ? "tlTreeView__node--selected" : "",
          g === s ? "tlTreeView__node--focused" : "",
          o === f.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === f.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === f.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: f.depth * zn },
        draggable: c,
        onClick: (C) => E(f.id, C),
        onContextMenu: (C) => R(f.id, C),
        onDragStart: (C) => h(f.id, C),
        onDragOver: u ? (C) => N(f.id, C) : void 0,
        onDrop: u ? (C) => S(f.id, C) : void 0,
        onDragEnd: O
      },
      f.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(f.id, f.expanded);
          },
          tabIndex: -1,
          "aria-label": f.expanded ? "Collapse" : "Expand"
        },
        f.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: f.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: f.content }))
    ))
  );
};
var Le = { exports: {} }, oe = {}, xe = { exports: {} }, $ = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Ye;
function Un() {
  if (Ye) return $;
  Ye = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), E = Symbol.iterator;
  function R(i) {
    return i === null || typeof i != "object" ? null : (i = E && i[E] || i["@@iterator"], typeof i == "function" ? i : null);
  }
  var _ = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, b = Object.assign, h = {};
  function N(i, v, j) {
    this.props = i, this.context = v, this.refs = h, this.updater = j || _;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(i, v) {
    if (typeof i != "object" && typeof i != "function" && i != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, i, v, "setState");
  }, N.prototype.forceUpdate = function(i) {
    this.updater.enqueueForceUpdate(this, i, "forceUpdate");
  };
  function S() {
  }
  S.prototype = N.prototype;
  function O(i, v, j) {
    this.props = i, this.context = v, this.refs = h, this.updater = j || _;
  }
  var P = O.prototype = new S();
  P.constructor = O, b(P, N.prototype), P.isPureReactComponent = !0;
  var f = Array.isArray;
  function g() {
  }
  var C = { H: null, A: null, T: null, S: null }, X = Object.prototype.hasOwnProperty;
  function k(i, v, j) {
    var x = j.ref;
    return {
      $$typeof: l,
      type: i,
      key: v,
      ref: x !== void 0 ? x : null,
      props: j
    };
  }
  function T(i, v) {
    return k(i.type, v, i.props);
  }
  function M(i) {
    return typeof i == "object" && i !== null && i.$$typeof === l;
  }
  function I(i) {
    var v = { "=": "=0", ":": "=2" };
    return "$" + i.replace(/[=:]/g, function(j) {
      return v[j];
    });
  }
  var Y = /\/+/g;
  function te(i, v) {
    return typeof i == "object" && i !== null && i.key != null ? I("" + i.key) : v.toString(36);
  }
  function le(i) {
    switch (i.status) {
      case "fulfilled":
        return i.value;
      case "rejected":
        throw i.reason;
      default:
        switch (typeof i.status == "string" ? i.then(g, g) : (i.status = "pending", i.then(
          function(v) {
            i.status === "pending" && (i.status = "fulfilled", i.value = v);
          },
          function(v) {
            i.status === "pending" && (i.status = "rejected", i.reason = v);
          }
        )), i.status) {
          case "fulfilled":
            return i.value;
          case "rejected":
            throw i.reason;
        }
    }
    throw i;
  }
  function Z(i, v, j, x, U) {
    var V = typeof i;
    (V === "undefined" || V === "boolean") && (i = null);
    var q = !1;
    if (i === null) q = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          q = !0;
          break;
        case "object":
          switch (i.$$typeof) {
            case l:
            case t:
              q = !0;
              break;
            case m:
              return q = i._init, Z(
                q(i._payload),
                v,
                j,
                x,
                U
              );
          }
      }
    if (q)
      return U = U(i), q = x === "" ? "." + te(i, 0) : x, f(U) ? (j = "", q != null && (j = q.replace(Y, "$&/") + "/"), Z(U, v, j, "", function(pe) {
        return pe;
      })) : U != null && (M(U) && (U = T(
        U,
        j + (U.key == null || i && i.key === U.key ? "" : ("" + U.key).replace(
          Y,
          "$&/"
        ) + "/") + q
      )), v.push(U)), 1;
    q = 0;
    var ae = x === "" ? "." : x + ":";
    if (f(i))
      for (var re = 0; re < i.length; re++)
        x = i[re], V = ae + te(x, re), q += Z(
          x,
          v,
          j,
          V,
          U
        );
    else if (re = R(i), typeof re == "function")
      for (i = re.call(i), re = 0; !(x = i.next()).done; )
        x = x.value, V = ae + te(x, re++), q += Z(
          x,
          v,
          j,
          V,
          U
        );
    else if (V === "object") {
      if (typeof i.then == "function")
        return Z(
          le(i),
          v,
          j,
          x,
          U
        );
      throw v = String(i), Error(
        "Objects are not valid as a React child (found: " + (v === "[object Object]" ? "object with keys {" + Object.keys(i).join(", ") + "}" : v) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return q;
  }
  function se(i, v, j) {
    if (i == null) return i;
    var x = [], U = 0;
    return Z(i, x, "", "", function(V) {
      return v.call(j, V, U++);
    }), x;
  }
  function B(i) {
    if (i._status === -1) {
      var v = i._result;
      v = v(), v.then(
        function(j) {
          (i._status === 0 || i._status === -1) && (i._status = 1, i._result = j);
        },
        function(j) {
          (i._status === 0 || i._status === -1) && (i._status = 2, i._result = j);
        }
      ), i._status === -1 && (i._status = 0, i._result = v);
    }
    if (i._status === 1) return i._result.default;
    throw i._result;
  }
  var z = typeof reportError == "function" ? reportError : function(i) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var v = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof i == "object" && i !== null && typeof i.message == "string" ? String(i.message) : String(i),
        error: i
      });
      if (!window.dispatchEvent(v)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", i);
      return;
    }
    console.error(i);
  }, G = {
    map: se,
    forEach: function(i, v, j) {
      se(
        i,
        function() {
          v.apply(this, arguments);
        },
        j
      );
    },
    count: function(i) {
      var v = 0;
      return se(i, function() {
        v++;
      }), v;
    },
    toArray: function(i) {
      return se(i, function(v) {
        return v;
      }) || [];
    },
    only: function(i) {
      if (!M(i))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return i;
    }
  };
  return $.Activity = p, $.Children = G, $.Component = N, $.Fragment = a, $.Profiler = c, $.PureComponent = O, $.StrictMode = n, $.Suspense = s, $.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, $.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(i) {
      return C.H.useMemoCache(i);
    }
  }, $.cache = function(i) {
    return function() {
      return i.apply(null, arguments);
    };
  }, $.cacheSignal = function() {
    return null;
  }, $.cloneElement = function(i, v, j) {
    if (i == null)
      throw Error(
        "The argument must be a React element, but you passed " + i + "."
      );
    var x = b({}, i.props), U = i.key;
    if (v != null)
      for (V in v.key !== void 0 && (U = "" + v.key), v)
        !X.call(v, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && v.ref === void 0 || (x[V] = v[V]);
    var V = arguments.length - 2;
    if (V === 1) x.children = j;
    else if (1 < V) {
      for (var q = Array(V), ae = 0; ae < V; ae++)
        q[ae] = arguments[ae + 2];
      x.children = q;
    }
    return k(i.type, U, x);
  }, $.createContext = function(i) {
    return i = {
      $$typeof: o,
      _currentValue: i,
      _currentValue2: i,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, i.Provider = i, i.Consumer = {
      $$typeof: u,
      _context: i
    }, i;
  }, $.createElement = function(i, v, j) {
    var x, U = {}, V = null;
    if (v != null)
      for (x in v.key !== void 0 && (V = "" + v.key), v)
        X.call(v, x) && x !== "key" && x !== "__self" && x !== "__source" && (U[x] = v[x]);
    var q = arguments.length - 2;
    if (q === 1) U.children = j;
    else if (1 < q) {
      for (var ae = Array(q), re = 0; re < q; re++)
        ae[re] = arguments[re + 2];
      U.children = ae;
    }
    if (i && i.defaultProps)
      for (x in q = i.defaultProps, q)
        U[x] === void 0 && (U[x] = q[x]);
    return k(i, V, U);
  }, $.createRef = function() {
    return { current: null };
  }, $.forwardRef = function(i) {
    return { $$typeof: d, render: i };
  }, $.isValidElement = M, $.lazy = function(i) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: i },
      _init: B
    };
  }, $.memo = function(i, v) {
    return {
      $$typeof: r,
      type: i,
      compare: v === void 0 ? null : v
    };
  }, $.startTransition = function(i) {
    var v = C.T, j = {};
    C.T = j;
    try {
      var x = i(), U = C.S;
      U !== null && U(j, x), typeof x == "object" && x !== null && typeof x.then == "function" && x.then(g, z);
    } catch (V) {
      z(V);
    } finally {
      v !== null && j.types !== null && (v.types = j.types), C.T = v;
    }
  }, $.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, $.use = function(i) {
    return C.H.use(i);
  }, $.useActionState = function(i, v, j) {
    return C.H.useActionState(i, v, j);
  }, $.useCallback = function(i, v) {
    return C.H.useCallback(i, v);
  }, $.useContext = function(i) {
    return C.H.useContext(i);
  }, $.useDebugValue = function() {
  }, $.useDeferredValue = function(i, v) {
    return C.H.useDeferredValue(i, v);
  }, $.useEffect = function(i, v) {
    return C.H.useEffect(i, v);
  }, $.useEffectEvent = function(i) {
    return C.H.useEffectEvent(i);
  }, $.useId = function() {
    return C.H.useId();
  }, $.useImperativeHandle = function(i, v, j) {
    return C.H.useImperativeHandle(i, v, j);
  }, $.useInsertionEffect = function(i, v) {
    return C.H.useInsertionEffect(i, v);
  }, $.useLayoutEffect = function(i, v) {
    return C.H.useLayoutEffect(i, v);
  }, $.useMemo = function(i, v) {
    return C.H.useMemo(i, v);
  }, $.useOptimistic = function(i, v) {
    return C.H.useOptimistic(i, v);
  }, $.useReducer = function(i, v, j) {
    return C.H.useReducer(i, v, j);
  }, $.useRef = function(i) {
    return C.H.useRef(i);
  }, $.useState = function(i) {
    return C.H.useState(i);
  }, $.useSyncExternalStore = function(i, v, j) {
    return C.H.useSyncExternalStore(
      i,
      v,
      j
    );
  }, $.useTransition = function() {
    return C.H.useTransition();
  }, $.version = "19.2.4", $;
}
var Ge;
function Vn() {
  return Ge || (Ge = 1, xe.exports = Un()), xe.exports;
}
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var qe;
function Wn() {
  if (qe) return oe;
  qe = 1;
  var l = Vn();
  function t(s) {
    var r = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      r += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        r += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + r + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function a() {
  }
  var n = {
    d: {
      f: a,
      r: function() {
        throw Error(t(522));
      },
      D: a,
      C: a,
      L: a,
      m: a,
      X: a,
      S: a,
      M: a
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function u(s, r, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: s,
      containerInfo: r,
      implementation: m
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(s, r) {
    if (s === "font") return "";
    if (typeof r == "string")
      return r === "use-credentials" ? r : "";
  }
  return oe.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, oe.createPortal = function(s, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return u(s, r, null, m);
  }, oe.flushSync = function(s) {
    var r = o.T, m = n.p;
    try {
      if (o.T = null, n.p = 2, s) return s();
    } finally {
      o.T = r, n.p = m, n.d.f();
    }
  }, oe.preconnect = function(s, r) {
    typeof s == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, n.d.C(s, r));
  }, oe.prefetchDNS = function(s) {
    typeof s == "string" && n.d.D(s);
  }, oe.preinit = function(s, r) {
    if (typeof s == "string" && r && typeof r.as == "string") {
      var m = r.as, p = d(m, r.crossOrigin), E = typeof r.integrity == "string" ? r.integrity : void 0, R = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? n.d.S(
        s,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: E,
          fetchPriority: R
        }
      ) : m === "script" && n.d.X(s, {
        crossOrigin: p,
        integrity: E,
        fetchPriority: R,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, oe.preinitModule = function(s, r) {
    if (typeof s == "string")
      if (typeof r == "object" && r !== null) {
        if (r.as == null || r.as === "script") {
          var m = d(
            r.as,
            r.crossOrigin
          );
          n.d.M(s, {
            crossOrigin: m,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && n.d.M(s);
  }, oe.preload = function(s, r) {
    if (typeof s == "string" && typeof r == "object" && r !== null && typeof r.as == "string") {
      var m = r.as, p = d(m, r.crossOrigin);
      n.d.L(s, m, {
        crossOrigin: p,
        integrity: typeof r.integrity == "string" ? r.integrity : void 0,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0,
        type: typeof r.type == "string" ? r.type : void 0,
        fetchPriority: typeof r.fetchPriority == "string" ? r.fetchPriority : void 0,
        referrerPolicy: typeof r.referrerPolicy == "string" ? r.referrerPolicy : void 0,
        imageSrcSet: typeof r.imageSrcSet == "string" ? r.imageSrcSet : void 0,
        imageSizes: typeof r.imageSizes == "string" ? r.imageSizes : void 0,
        media: typeof r.media == "string" ? r.media : void 0
      });
    }
  }, oe.preloadModule = function(s, r) {
    if (typeof s == "string")
      if (r) {
        var m = d(r.as, r.crossOrigin);
        n.d.m(s, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else n.d.m(s);
  }, oe.requestFormReset = function(s) {
    n.d.r(s);
  }, oe.unstable_batchedUpdates = function(s, r) {
    return s(r);
  }, oe.useFormState = function(s, r, m) {
    return o.H.useFormState(s, r, m);
  }, oe.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, oe.version = "19.2.4", oe;
}
var Xe;
function Kn() {
  if (Xe) return Le.exports;
  Xe = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Le.exports = Wn(), Le.exports;
}
var Yn = Kn();
const { useState: me, useCallback: ce, useRef: ve, useEffect: fe, useMemo: Ae } = e;
function Fe({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Gn({
  option: l,
  removable: t,
  onRemove: a,
  removeLabel: n,
  draggable: c,
  onDragStart: u,
  onDragOver: o,
  onDrop: d,
  onDragEnd: s,
  dragClassName: r
}) {
  const m = ce(
    (p) => {
      p.stopPropagation(), a(l.value);
    },
    [a, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (r ? " " + r : ""),
      draggable: c || void 0,
      onDragStart: u,
      onDragOver: o,
      onDrop: d,
      onDragEnd: s
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Fe, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": n
      },
      "×"
    )
  );
}
function qn({
  option: l,
  highlighted: t,
  searchTerm: a,
  onSelect: n,
  onMouseEnter: c,
  id: u
}) {
  const o = ce(() => n(l.value), [n, l.value]), d = Ae(() => {
    if (!a) return l.label;
    const s = l.label.toLowerCase().indexOf(a.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + a.length)), l.label.substring(s + a.length));
  }, [l.label, a]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: u,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: o,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(Fe, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const Xn = ({ controlId: l, state: t }) => {
  const a = ne(), n = t.value ?? [], c = t.multiSelect === !0, u = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", E = u && c && !d && s, R = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), _ = R["js.dropdownSelect.nothingFound"], b = ce(
    (y) => R["js.dropdownSelect.removeChip"].replace("{0}", y),
    [R]
  ), [h, N] = me(!1), [S, O] = me(""), [P, f] = me(-1), [g, C] = me(!1), [X, k] = me({}), [T, M] = me(null), [I, Y] = me(null), [te, le] = me(null), Z = ve(null), se = ve(null), B = ve(null), z = ve(n);
  z.current = n;
  const G = ve(-1), i = Ae(
    () => new Set(n.map((y) => y.value)),
    [n]
  ), v = Ae(() => {
    let y = m.filter((L) => !i.has(L.value));
    if (S) {
      const L = S.toLowerCase();
      y = y.filter((A) => A.label.toLowerCase().includes(L));
    }
    return y;
  }, [m, i, S]);
  fe(() => {
    S && v.length === 1 ? f(0) : f(-1);
  }, [v.length, S]), fe(() => {
    h && r && se.current && se.current.focus();
  }, [h, r, n]), fe(() => {
    var A, Q;
    if (G.current < 0) return;
    const y = G.current;
    G.current = -1;
    const L = (A = Z.current) == null ? void 0 : A.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(y, L.length - 1)].focus() : (Q = Z.current) == null || Q.focus();
  }, [n]), fe(() => {
    if (!h) return;
    const y = (L) => {
      Z.current && !Z.current.contains(L.target) && B.current && !B.current.contains(L.target) && (N(!1), O(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [h]), fe(() => {
    if (!h || !Z.current) return;
    const y = Z.current.getBoundingClientRect(), L = window.innerHeight - y.bottom, Q = L < 300 && y.top > L;
    k({
      left: y.left,
      width: y.width,
      ...Q ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [h]);
  const j = ce(async () => {
    if (!(d || !s) && (N(!0), O(""), f(-1), C(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
  }, [d, s, r, a]), x = ce(() => {
    var y;
    N(!1), O(""), f(-1), (y = Z.current) == null || y.focus();
  }, []), U = ce(
    (y) => {
      let L;
      if (c) {
        const A = m.find((Q) => Q.value === y);
        if (A)
          L = [...z.current, A];
        else
          return;
      } else {
        const A = m.find((Q) => Q.value === y);
        if (A)
          L = [A];
        else
          return;
      }
      z.current = L, a("valueChanged", { value: L.map((A) => A.value) }), c ? (O(""), f(-1)) : x();
    },
    [c, m, a, x]
  ), V = ce(
    (y) => {
      G.current = z.current.findIndex((A) => A.value === y);
      const L = z.current.filter((A) => A.value !== y);
      z.current = L, a("valueChanged", { value: L.map((A) => A.value) });
    },
    [a]
  ), q = ce(
    (y) => {
      y.stopPropagation(), a("valueChanged", { value: [] }), x();
    },
    [a, x]
  ), ae = ce((y) => {
    O(y.target.value);
  }, []), re = ce(
    (y) => {
      if (!h) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), j();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), f(
            (L) => L < v.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), f(
            (L) => L > 0 ? L - 1 : v.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), P >= 0 && P < v.length && U(v[P].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), x();
          break;
        case "Tab":
          x();
          break;
        case "Backspace":
          S === "" && c && n.length > 0 && V(n[n.length - 1].value);
          break;
      }
    },
    [
      h,
      j,
      x,
      v,
      P,
      U,
      S,
      c,
      n,
      V
    ]
  ), pe = ce(
    async (y) => {
      y.preventDefault(), C(!1);
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
    },
    [a]
  ), Ne = ce(
    (y, L) => {
      M(y), L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), w = ce(
    (y, L) => {
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", T === null || T === y) {
        Y(null), le(null);
        return;
      }
      const A = L.currentTarget.getBoundingClientRect(), Q = A.left + A.width / 2, be = L.clientX < Q ? "before" : "after";
      Y(y), le(be);
    },
    [T]
  ), D = ce(
    (y) => {
      if (y.preventDefault(), T === null || I === null || te === null || T === I) return;
      const L = [...z.current], [A] = L.splice(T, 1);
      let Q = I;
      T < I ? Q = te === "before" ? Q - 1 : Q : Q = te === "before" ? Q : Q + 1, L.splice(Q, 0, A), z.current = L, a("valueChanged", { value: L.map((be) => be.value) }), M(null), Y(null), le(null);
    },
    [T, I, te, a]
  ), H = ce(() => {
    M(null), Y(null), le(null);
  }, []);
  if (fe(() => {
    if (P < 0 || !B.current) return;
    const y = B.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : n.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Fe, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const J = !o && n.length > 0 && !d, ee = h ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: X
    },
    (r || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: se,
        type: "text",
        className: "tlDropdownSelect__search",
        value: S,
        onChange: ae,
        onKeyDown: re,
        placeholder: R["js.dropdownSelect.filterPlaceholder"],
        "aria-label": R["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": P >= 0 ? `${l}-opt-${P}` : void 0,
        "aria-controls": `${l}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${l}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !r && !g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, R["js.dropdownSelect.error"])),
      r && v.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, _),
      r && v.map((y, L) => /* @__PURE__ */ e.createElement(
        qn,
        {
          key: y.value,
          id: `${l}-opt-${L}`,
          option: y,
          highlighted: L === P,
          searchTerm: S,
          onSelect: U,
          onMouseEnter: () => f(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: Z,
      className: "tlDropdownSelect" + (h ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": h,
      "aria-haspopup": "listbox",
      "aria-owns": h ? `${l}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: h ? void 0 : j,
      onKeyDown: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : n.map((y, L) => {
      let A = "";
      return T === L ? A = "tlDropdownSelect__chip--dragging" : I === L && te === "before" ? A = "tlDropdownSelect__chip--dropBefore" : I === L && te === "after" && (A = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Gn,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: V,
          removeLabel: b(y.label),
          draggable: E,
          onDragStart: E ? (Q) => Ne(L, Q) : void 0,
          onDragOver: E ? (Q) => w(L, Q) : void 0,
          onDrop: E ? D : void 0,
          onDragEnd: E ? H : void 0,
          dragClassName: E ? A : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, J && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: q,
        "aria-label": R["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, h ? "▲" : "▼"))
  ), ee && Yn.createPortal(ee, document.body));
}, { useCallback: Ie, useRef: Zn } = e, et = "application/x-tl-color", Qn = ({
  colors: l,
  columns: t,
  onSelect: a,
  onConfirm: n,
  onSwap: c,
  onReplace: u
}) => {
  const o = Zn(null), d = Ie(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = Ie((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = Ie(
    (m) => (p) => {
      p.preventDefault();
      const E = p.dataTransfer.getData(et);
      E ? u(m, E) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
    },
    [c, u]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => a(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? d(p) : void 0,
        onDragOver: s,
        onDrop: r(p)
      }
    ))
  );
};
function tt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Oe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function nt(l) {
  if (!Oe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function rt(l, t, a) {
  const n = (c) => tt(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(a);
}
function Jn(l, t, a) {
  const n = l / 255, c = t / 255, u = a / 255, o = Math.max(n, c, u), d = Math.min(n, c, u), s = o - d;
  let r = 0;
  s !== 0 && (o === n ? r = (c - u) / s % 6 : o === c ? r = (u - n) / s + 2 : r = (n - c) / s + 4, r *= 60, r < 0 && (r += 360));
  const m = o === 0 ? 0 : s / o;
  return [r, m, o];
}
function er(l, t, a) {
  const n = a * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), u = a - n;
  let o = 0, d = 0, s = 0;
  return l < 60 ? (o = n, d = c, s = 0) : l < 120 ? (o = c, d = n, s = 0) : l < 180 ? (o = 0, d = n, s = c) : l < 240 ? (o = 0, d = c, s = n) : l < 300 ? (o = c, d = 0, s = n) : (o = n, d = 0, s = c), [
    Math.round((o + u) * 255),
    Math.round((d + u) * 255),
    Math.round((s + u) * 255)
  ];
}
function tr(l) {
  return Jn(...nt(l));
}
function Pe(l, t, a) {
  return rt(...er(l, t, a));
}
const { useCallback: he, useRef: Ze } = e, nr = ({ color: l, onColorChange: t }) => {
  const [a, n, c] = tr(l), u = Ze(null), o = Ze(null), d = he(
    (_, b) => {
      var O;
      const h = (O = u.current) == null ? void 0 : O.getBoundingClientRect();
      if (!h) return;
      const N = Math.max(0, Math.min(1, (_ - h.left) / h.width)), S = Math.max(0, Math.min(1, 1 - (b - h.top) / h.height));
      t(Pe(a, N, S));
    },
    [a, t]
  ), s = he(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), d(_.clientX, _.clientY);
    },
    [d]
  ), r = he(
    (_) => {
      _.buttons !== 0 && d(_.clientX, _.clientY);
    },
    [d]
  ), m = he(
    (_) => {
      var S;
      const b = (S = o.current) == null ? void 0 : S.getBoundingClientRect();
      if (!b) return;
      const N = Math.max(0, Math.min(1, (_ - b.top) / b.height)) * 360;
      t(Pe(N, n, c));
    },
    [n, c, t]
  ), p = he(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), m(_.clientY);
    },
    [m]
  ), E = he(
    (_) => {
      _.buttons !== 0 && m(_.clientY);
    },
    [m]
  ), R = Pe(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: "tlColorInput__svField",
      style: { backgroundColor: R },
      onPointerDown: s,
      onPointerMove: r
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: E
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${a / 360 * 100}%` }
      }
    )
  ));
};
function rr(l, t) {
  const a = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === a);
}
const lr = {
  "js.colorInput.paletteTab": "Color Palette",
  "js.colorInput.mixerTab": "Color Mixer",
  "js.colorInput.current": "Current",
  "js.colorInput.new": "New",
  "js.colorInput.red": "Red",
  "js.colorInput.green": "Green",
  "js.colorInput.blue": "Blue",
  "js.colorInput.hex": "Hex",
  "js.colorInput.clear": "Clear",
  "js.colorInput.reset": "Reset",
  "js.colorInput.cancel": "Cancel",
  "js.colorInput.ok": "OK"
}, { useState: ge, useCallback: de, useEffect: je, useRef: ar, useLayoutEffect: or } = e, sr = ({
  anchorRef: l,
  currentColor: t,
  palette: a,
  paletteColumns: n,
  defaultPalette: c,
  canReset: u,
  onConfirm: o,
  onCancel: d,
  onPaletteChange: s
}) => {
  const [r, m] = ge("palette"), [p, E] = ge(t), R = ar(null), _ = ie(lr), [b, h] = ge(null);
  or(() => {
    if (!l.current || !R.current) return;
    const B = l.current.getBoundingClientRect(), z = R.current.getBoundingClientRect();
    let G = B.bottom + 4, i = B.left;
    G + z.height > window.innerHeight && (G = B.top - z.height - 4), i + z.width > window.innerWidth && (i = Math.max(0, B.right - z.width)), h({ top: G, left: i });
  }, [l]);
  const N = p != null, [S, O, P] = N ? nt(p) : [0, 0, 0], [f, g] = ge((p == null ? void 0 : p.toUpperCase()) ?? "");
  je(() => {
    g((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), je(() => {
    const B = (z) => {
      z.key === "Escape" && d();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [d]), je(() => {
    const B = (G) => {
      R.current && !R.current.contains(G.target) && d();
    }, z = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(z), document.removeEventListener("mousedown", B);
    };
  }, [d]);
  const C = de(
    (B) => (z) => {
      const G = parseInt(z.target.value, 10);
      if (isNaN(G)) return;
      const i = tt(G);
      E(rt(B === "r" ? i : S, B === "g" ? i : O, B === "b" ? i : P));
    },
    [S, O, P]
  ), X = de(
    (B) => {
      p != null && (B.dataTransfer.setData(et, p.toUpperCase()), B.dataTransfer.effectAllowed = "copy");
    },
    [p]
  ), k = de((B) => {
    const z = B.target.value;
    g(z), Oe(z) && E(z);
  }, []), T = de(() => {
    E(null);
  }, []), M = de((B) => {
    E(B);
  }, []), I = de(
    (B) => {
      o(B);
    },
    [o]
  ), Y = de(
    (B, z) => {
      const G = [...a], i = G[B];
      G[B] = G[z], G[z] = i, s(G);
    },
    [a, s]
  ), te = de(
    (B, z) => {
      const G = [...a];
      G[B] = z, s(G);
    },
    [a, s]
  ), le = de(() => {
    s([...c]);
  }, [c, s]), Z = de(
    (B) => {
      if (rr(a, B)) return;
      const z = a.indexOf(null);
      if (z < 0) return;
      const G = [...a];
      G[z] = B.toUpperCase(), s(G);
    },
    [a, s]
  ), se = de(() => {
    p != null && Z(p), o(p);
  }, [p, o, Z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: R,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      _["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      _["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Qn,
      {
        colors: a,
        columns: n,
        onSelect: M,
        onConfirm: I,
        onSwap: Y,
        onReplace: te
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: le }, _["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(nr, { color: p ?? "#000000", onColorChange: E }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: p } : void 0,
        draggable: N,
        onDragStart: N ? X : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? S : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? O : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? P : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (f !== "" && !Oe(f) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: f,
        onChange: k
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, _["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, _["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: se }, _["js.colorInput.ok"]))
  );
}, cr = { "js.colorInput.chooseColor": "Choose color" }, { useState: ir, useCallback: Ce, useRef: ur } = e, dr = ({ controlId: l, state: t }) => {
  const a = ne(), n = ie(cr), [c, u] = ir(!1), o = ur(null), d = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, E = Ce(() => {
    s && u(!0);
  }, [s]), R = Ce(
    (h) => {
      u(!1), a("valueChanged", { value: h });
    },
    [a]
  ), _ = Ce(() => {
    u(!1);
  }, []), b = Ce(
    (h) => {
      a("paletteChanged", { palette: h });
    },
    [a]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (d == null ? " tlColorInput__swatch--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      onClick: E,
      disabled: t.disabled === !0,
      title: d ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    sr,
    {
      anchorRef: o,
      currentColor: d,
      palette: r,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: R,
      onCancel: _,
      onPaletteChange: b
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (d == null ? " tlColorInput--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      title: d ?? ""
    }
  );
};
F("TLButton", vt);
F("TLToggleButton", gt);
F("TLTextInput", st);
F("TLNumberInput", it);
F("TLDatePicker", dt);
F("TLSelect", pt);
F("TLCheckbox", ht);
F("TLTable", _t);
F("TLCounter", Ct);
F("TLTabBar", wt);
F("TLFieldList", kt);
F("TLAudioRecorder", St);
F("TLAudioPlayer", Rt);
F("TLFileUpload", Lt);
F("TLDownload", It);
F("TLPhotoCapture", jt);
F("TLPhotoViewer", At);
F("TLSplitPanel", Ot);
F("TLPanel", Vt);
F("TLMaximizeRoot", Wt);
F("TLDeckPane", Kt);
F("TLSidebar", en);
F("TLStack", tn);
F("TLGrid", nn);
F("TLCard", rn);
F("TLAppBar", ln);
F("TLBreadcrumb", on);
F("TLBottomBar", cn);
F("TLDialog", mn);
F("TLDrawer", _n);
F("TLSnackbar", En);
F("TLMenu", Cn);
F("TLAppShell", yn);
F("TLTextCell", wn);
F("TLTableView", Nn);
F("TLFormLayout", In);
F("TLFormGroup", Mn);
F("TLFormField", $n);
F("TLResourceCell", Fn);
F("TLTreeView", Hn);
F("TLDropdownSelect", Xn);
F("TLColorInput", dr);
