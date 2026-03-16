import { React as e, useTLFieldValue as Ce, getComponent as ft, useTLState as G, useTLCommand as le, TLChild as Y, useTLUpload as Ue, useI18N as oe, useTLDataUrl as Ve, register as z } from "tl-react-bridge";
const { useCallback: ht } = e, _t = ({ controlId: l, state: t }) => {
  const [r, n] = Ce(), c = ht(
    (i) => {
      n(i.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  ));
}, { useCallback: bt } = e, vt = ({ controlId: l, state: t, config: r }) => {
  const [n, c] = Ce(), i = bt(
    (d) => {
      const s = d.target.value, a = s === "" ? null : Number(s);
      c(a);
    },
    [c]
  ), o = r != null && r.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: n != null ? String(n) : "",
      onChange: i,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  ));
}, { useCallback: Et } = e, gt = ({ controlId: l, state: t }) => {
  const [r, n] = Ce(), c = Et(
    (i) => {
      n(i.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  ));
}, { useCallback: Ct } = e, yt = ({ controlId: l, state: t, config: r }) => {
  var d;
  const [n, c] = Ce(), i = Ct(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), o = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const s = ((d = o.find((a) => a.value === n)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  ));
}, { useCallback: wt } = e, kt = ({ controlId: l, state: t }) => {
  const [r, n] = Ce(), c = wt(
    (i) => {
      n(i.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: r === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, St = ({ controlId: l, state: t }) => {
  const r = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, r.map((o) => {
    const d = o.cellModule ? ft(o.cellModule) : void 0, s = c[o.name];
    if (d) {
      const a = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: l + "-" + i + "-" + o.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: Nt } = e, Tt = ({ controlId: l, command: t, label: r, disabled: n }) => {
  const c = G(), i = le(), o = t ?? "click", d = r ?? c.label, s = n ?? c.disabled === !0, a = Nt(() => {
    i(o);
  }, [i, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: a,
      disabled: s,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: Rt } = e, Lt = ({ controlId: l, command: t, label: r, active: n, disabled: c }) => {
  const i = G(), o = le(), d = t ?? "click", s = r ?? i.label, a = n ?? i.active === !0, m = c ?? i.disabled === !0, f = Rt(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: f,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    s
  );
}, Dt = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: xt } = e, It = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.tabs ?? [], c = t.activeTabId, i = xt((o) => {
    o !== c && r("selectTab", { tabId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, jt = ({ controlId: l }) => {
  const t = G(), r = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Pt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Mt = ({ controlId: l }) => {
  const t = G(), r = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", f = t.error, _ = m === "received" ? "idle" : n !== "idle" ? n : m, L = e.useCallback(async () => {
    if (n === "recording") {
      const w = d.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = w, s.current = [];
        const $ = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(w, $ ? { mimeType: $ } : void 0);
        d.current = P, P.ondataavailable = (h) => {
          h.data.size > 0 && s.current.push(h.data);
        }, P.onstop = async () => {
          w.getTracks().forEach((E) => E.stop()), a.current = null;
          const h = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], h.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const C = new FormData();
          C.append("audio", h, "recording.webm"), await r(C), c("idle");
        }, P.start(), c("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, r]), b = oe(Pt), v = _ === "recording" ? b["js.audioRecorder.stop"] : _ === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], p = _ === "uploading", N = ["tlAudioRecorder__button"];
  return _ === "recording" && N.push("tlAudioRecorder__button--recording"), _ === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: L,
      disabled: p,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, At = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ot = ({ controlId: l }) => {
  const t = G(), r = Ve(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), a = e.useRef(c);
  e.useEffect(() => {
    n ? i === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    c !== a.current && (a.current = c, d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
  }, [c]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      d.current && d.current.pause(), o("paused");
      return;
    }
    if (i === "paused" && d.current) {
      d.current.play(), o("playing");
      return;
    }
    if (!s.current) {
      o("loading");
      try {
        const p = await fetch(r);
        if (!p.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", p.status), o("idle");
          return;
        }
        const N = await p.blob();
        s.current = URL.createObjectURL(N);
      } catch (p) {
        console.error("[TLAudioPlayer] Fetch error:", p), o("idle");
        return;
      }
    }
    const v = new Audio(s.current);
    d.current = v, v.onended = () => {
      o("idle");
    }, v.play(), o("playing");
  }, [i, r]), f = oe(At), _ = i === "loading" ? f["js.loading"] : i === "playing" ? f["js.audioPlayer.pause"] : i === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], L = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: L,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, $t = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Bt = ({ controlId: l }) => {
  const t = G(), r = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", a = t.error, m = t.accept ?? "", f = s === "received" ? "idle" : n !== "idle" ? n : s, _ = e.useCallback(async (h) => {
    c("uploading");
    const C = new FormData();
    C.append("file", h, h.name), await r(C), c("idle");
  }, [r]), L = e.useCallback((h) => {
    var E;
    const C = (E = h.target.files) == null ? void 0 : E[0];
    C && _(C);
  }, [_]), b = e.useCallback(() => {
    var h;
    n !== "uploading" && ((h = d.current) == null || h.click());
  }, [n]), v = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!0);
  }, []), p = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!1);
  }, []), N = e.useCallback((h) => {
    var E;
    if (h.preventDefault(), h.stopPropagation(), o(!1), n === "uploading") return;
    const C = (E = h.dataTransfer.files) == null ? void 0 : E[0];
    C && _(C);
  }, [n, _]), w = f === "uploading", $ = oe($t), P = f === "uploading" ? $["js.uploading"] : $["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: p,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: m || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: w,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, Ft = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ht = ({ controlId: l }) => {
  const t = G(), r = Ve(), n = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      a(!0);
      try {
        const b = r + (r.includes("?") ? "&" : "?") + "rev=" + i, v = await fetch(b);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const p = await v.blob(), N = URL.createObjectURL(p), w = document.createElement("a");
        w.href = N, w.download = o, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(N);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        a(!1);
      }
    }
  }, [c, s, r, i, o]), f = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), _ = oe(Ft);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const L = s ? _["js.downloading"] : _["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: _["js.download.clear"],
      "aria-label": _["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, zt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Wt = ({ controlId: l }) => {
  const t = G(), r = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), f = e.useRef(null), _ = e.useRef(null), L = e.useRef(null), b = t.error, v = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), p = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    p(), c("idle");
  }, [p]), w = e.useCallback(async () => {
    var R;
    if (n !== "uploading") {
      if (o(null), !v) {
        (R = _.current) == null || R.click();
        return;
      }
      try {
        const A = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = A, c("overlayOpen");
      } catch (A) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", A), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [n, v]), $ = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const R = a.current, A = f.current;
    if (!R || !A)
      return;
    A.width = R.videoWidth, A.height = R.videoHeight;
    const T = A.getContext("2d");
    T && (T.drawImage(R, 0, 0), p(), c("uploading"), A.toBlob(async (I) => {
      if (!I) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", I, "capture.jpg"), await r(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, r, p]), P = e.useCallback(async (R) => {
    var I;
    const A = (I = R.target.files) == null ? void 0 : I[0];
    if (!A) return;
    c("uploading");
    const T = new FormData();
    T.append("photo", A, A.name), await r(T), c("idle"), _.current && (_.current.value = "");
  }, [r]);
  e.useEffect(() => {
    n === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var A;
    if (n !== "overlayOpen") return;
    (A = L.current) == null || A.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const R = (A) => {
      A.key === "Escape" && N();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [n, N]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const h = oe(zt), C = n === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  d && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return d && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: w,
      disabled: n === "uploading",
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: _,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        ref: a,
        className: V.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
        onClick: () => s((R) => !R),
        title: h["js.photoCapture.mirror"],
        "aria-label": h["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: $,
        title: h["js.photoCapture.capture"],
        "aria-label": h["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Ut = {
  "js.photoViewer.alt": "Captured photo"
}, Vt = ({ controlId: l }) => {
  const t = G(), r = Ve(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), d = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (c === d.current && i)
      return;
    d.current = c, i && (URL.revokeObjectURL(i), o(null));
    let a = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const f = await m.blob();
        a || o(URL.createObjectURL(f));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [n, c, r]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(Ut);
  return !n || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ye, useRef: Ie } = e, Kt = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = n === "horizontal", d = i.length > 0 && i.every((p) => p.collapsed), s = !d && i.some((p) => p.collapsed), a = d ? !o : o, m = Ie(null), f = Ie(null), _ = Ie(null), L = Ye((p, N) => {
    const w = {
      overflow: p.scrolling || "auto"
    };
    return p.collapsed ? d && !a ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : N !== void 0 ? w.flex = `0 0 ${N}px` : p.unit === "%" || s ? w.flex = `${p.size} 0 0%` : w.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (w.minWidth = o ? p.minSize : void 0, w.minHeight = o ? void 0 : p.minSize), w;
  }, [o, d, s, a]), b = Ye((p, N) => {
    p.preventDefault();
    const w = m.current;
    if (!w) return;
    const $ = i[N], P = i[N + 1], h = w.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    h.forEach((S) => {
      C.push(o ? S.offsetWidth : S.offsetHeight);
    }), _.current = C, f.current = {
      splitterIndex: N,
      startPos: o ? p.clientX : p.clientY,
      startSizeBefore: C[N],
      startSizeAfter: C[N + 1],
      childBefore: $,
      childAfter: P
    };
    const E = (S) => {
      const R = f.current;
      if (!R || !_.current) return;
      const T = (o ? S.clientX : S.clientY) - R.startPos, I = R.childBefore.minSize || 0, O = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + T, Z = R.startSizeAfter - T;
      ee < I && (Z += ee - I, ee = I), Z < O && (ee += Z - O, Z = O), _.current[R.splitterIndex] = ee, _.current[R.splitterIndex + 1] = Z;
      const ae = w.querySelectorAll(":scope > .tlSplitPanel__child"), B = ae[R.splitterIndex], j = ae[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${Z}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const S = {};
        i.forEach((R, A) => {
          const T = R.control;
          T != null && T.controlId && _.current && (S[T.controlId] = _.current[A]);
        }), r("updateSizes", { sizes: S });
      }
      _.current = null, f.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", V), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, r]), v = [];
  return i.forEach((p, N) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${p.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(p)
        },
        /* @__PURE__ */ e.createElement(Y, { control: p.control })
      )
    ), c && N < i.length - 1) {
      const w = i[N + 1];
      !p.collapsed && !w.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => b(P, N)
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
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    v
  );
}, { useCallback: je } = e, Yt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Gt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Jt = ({ controlId: l }) => {
  const t = G(), r = le(), n = oe(Yt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, a = t.toolbarButtons ?? [], m = i === "MINIMIZED", f = i === "MAXIMIZED", _ = i === "HIDDEN", L = je(() => {
    r("toggleMinimize");
  }, [r]), b = je(() => {
    r("toggleMaximize");
  }, [r]), v = je(() => {
    r("popOut");
  }, [r]);
  if (_)
    return null;
  const p = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: p
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, a.map((N, w) => /* @__PURE__ */ e.createElement("span", { key: w, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: N }))), o && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: m ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Xt, null) : /* @__PURE__ */ e.createElement(Gt, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(Zt, null) : /* @__PURE__ */ e.createElement(qt, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Qt, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child }))
  );
}, en = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, tn = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: Te, useEffect: Re, useRef: Le } = e, nn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function He(l, t, r, n) {
  const c = [];
  for (const i of l)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: n }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: n }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (r.get(i.id) ?? i.expanded) && !t && c.push(...He(i.children, t, r, i.id)));
  return c;
}
const be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, ln = ({ item: l, active: t, collapsed: r, onSelect: n, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: r ? l.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(l.id)
  },
  r && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !r && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), an = ({ item: l, collapsed: t, onExecute: r, tabIndex: n, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), rn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), on = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), sn = ({ item: l, activeItemId: t, anchorRect: r, onSelect: n, onExecute: c, onClose: i }) => {
  const o = Le(null);
  Re(() => {
    const a = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [i]), Re(() => {
    const a = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [i]);
  const d = ue((a) => {
    a.type === "nav" ? (n(a.id), i()) : a.type === "command" && (c(a.id), i());
  }, [n, c, i]), s = {};
  return r && (s.left = r.right, s.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
    if (a.type === "nav" || a.type === "command") {
      const m = a.type === "nav" && a.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: a.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(a)
        },
        /* @__PURE__ */ e.createElement(be, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, cn = ({
  item: l,
  expanded: t,
  activeItemId: r,
  collapsed: n,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: d,
  itemRef: s,
  onFocus: a,
  focusedId: m,
  setItemRef: f,
  onItemFocus: _,
  flyoutGroupId: L,
  onOpenFlyout: b,
  onCloseFlyout: v
}) => {
  const p = Le(null), [N, w] = Te(null), $ = ue(() => {
    n ? L === l.id ? v() : (p.current && w(p.current.getBoundingClientRect()), b(l.id)) : o(l.id);
  }, [n, L, l.id, o, b, v]), P = ue((C) => {
    p.current = C, s(C);
  }, [s]), h = n && L === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: $,
      title: n ? l.label : void 0,
      "aria-expanded": n ? h : t,
      tabIndex: d,
      ref: P,
      onFocus: () => a(l.id)
    },
    /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
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
  ), h && /* @__PURE__ */ e.createElement(
    sn,
    {
      item: l,
      activeItemId: r,
      anchorRect: N,
      onSelect: c,
      onExecute: i,
      onClose: v
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((C) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: C.id,
      item: C,
      activeItemId: r,
      collapsed: n,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: f,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: b,
      onCloseFlyout: v
    }
  ))));
}, st = ({
  item: l,
  activeItemId: t,
  collapsed: r,
  onSelect: n,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: d,
  onItemFocus: s,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: f,
  onCloseFlyout: _
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        ln,
        {
          item: l,
          active: l.id === t,
          collapsed: r,
          onSelect: n,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        an,
        {
          item: l,
          collapsed: r,
          onExecute: c,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(rn, { item: l, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(on, null);
    case "group": {
      const L = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        cn,
        {
          item: l,
          expanded: L,
          activeItemId: t,
          collapsed: r,
          onSelect: n,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: d,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: f,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, un = ({ controlId: l }) => {
  const t = G(), r = le(), n = oe(nn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = Te(() => {
    const S = /* @__PURE__ */ new Map(), R = (A) => {
      for (const T of A)
        T.type === "group" && (S.set(T.id, T.expanded), R(T.children));
    };
    return R(c), S;
  }), a = ue((S) => {
    s((R) => {
      const A = new Map(R), T = A.get(S) ?? !1;
      return A.set(S, !T), r("toggleGroup", { itemId: S, expanded: !T }), A;
    });
  }, [r]), m = ue((S) => {
    S !== i && r("selectItem", { itemId: S });
  }, [r, i]), f = ue((S) => {
    r("executeCommand", { itemId: S });
  }, [r]), _ = ue(() => {
    r("toggleCollapse", {});
  }, [r]), [L, b] = Te(null), v = ue((S) => {
    b(S);
  }, []), p = ue(() => {
    b(null);
  }, []);
  Re(() => {
    o || b(null);
  }, [o]);
  const [N, w] = Te(() => {
    const S = He(c, o, d);
    return S.length > 0 ? S[0].id : "";
  }), $ = Le(/* @__PURE__ */ new Map()), P = ue((S) => (R) => {
    R ? $.current.set(S, R) : $.current.delete(S);
  }, []), h = ue((S) => {
    w(S);
  }, []), C = Le(0), E = ue((S) => {
    w(S), C.current++;
  }, []);
  Re(() => {
    const S = $.current.get(N);
    S && document.activeElement !== S && S.focus();
  }, [N, C.current]);
  const V = ue((S) => {
    if (S.key === "Escape" && L !== null) {
      S.preventDefault(), p();
      return;
    }
    const R = He(c, o, d);
    if (R.length === 0) return;
    const A = R.findIndex((I) => I.id === N);
    if (A < 0) return;
    const T = R[A];
    switch (S.key) {
      case "ArrowDown": {
        S.preventDefault();
        const I = (A + 1) % R.length;
        E(R[I].id);
        break;
      }
      case "ArrowUp": {
        S.preventDefault();
        const I = (A - 1 + R.length) % R.length;
        E(R[I].id);
        break;
      }
      case "Home": {
        S.preventDefault(), E(R[0].id);
        break;
      }
      case "End": {
        S.preventDefault(), E(R[R.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        S.preventDefault(), T.type === "nav" ? m(T.id) : T.type === "command" ? f(T.id) : T.type === "group" && (o ? L === T.id ? p() : v(T.id) : a(T.id));
        break;
      }
      case "ArrowRight": {
        T.type === "group" && !o && ((d.get(T.id) ?? !1) || (S.preventDefault(), a(T.id)));
        break;
      }
      case "ArrowLeft": {
        T.type === "group" && !o && (d.get(T.id) ?? !1) && (S.preventDefault(), a(T.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    N,
    L,
    E,
    m,
    f,
    a,
    v,
    p
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((S) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: S.id,
      item: S,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: f,
      onToggleGroup: a,
      focusedId: N,
      setItemRef: P,
      onItemFocus: h,
      groupStates: d,
      flyoutGroupId: L,
      onOpenFlyout: v,
      onCloseFlyout: p
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, dn = ({ controlId: l }) => {
  const t = G(), r = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, o.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })));
}, mn = ({ controlId: l }) => {
  const t = G(), r = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : r && (o.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((d, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: d })));
}, pn = ({ controlId: l }) => {
  const t = G(), r = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, d = r != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, fn = ({ controlId: l }) => {
  const t = G(), r = t.title ?? "", n = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s }))));
}, { useCallback: hn } = e, _n = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.items ?? [], c = hn((i) => {
    r("navigate", { itemId: i });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((i, o) => {
    const d = o === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: i.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), d ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: bn } = e, vn = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.items ?? [], c = t.activeItemId, i = bn((o) => {
    o !== c && r("selectItem", { itemId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
    const d = o.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => i(o.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: Ge, useEffect: Xe, useRef: En } = e, gn = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = En(null), d = Ge(() => {
    r("close");
  }, [r]), s = Ge((a) => {
    c && a.target === a.currentTarget && d();
  }, [c, d]);
  return Xe(() => {
    if (!n) return;
    const a = (m) => {
      m.key === "Escape" && d();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [n, d]), Xe(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: s,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Y, { control: i })
  ) : null;
}, { useEffect: Cn, useRef: yn } = e, wn = ({ controlId: l }) => {
  const r = G().dialogs ?? [], n = yn(r.length);
  return Cn(() => {
    r.length < n.current && r.length > 0, n.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, r.map((c) => /* @__PURE__ */ e.createElement(Y, { key: c.controlId, control: c })));
}, { useCallback: qe, useRef: ye, useState: Ze } = e, kn = {
  "js.window.close": "Close"
}, Sn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Nn = ({ controlId: l }) => {
  const t = G(), r = le(), n = oe(kn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, d = t.resizable === !0, s = t.child, a = t.actions ?? [], m = t.toolbarButtons ?? [], [f, _] = Ze(null), [L, b] = Ze(null), v = ye(null), p = ye(null), N = ye(null), w = ye(null), $ = qe(() => {
    r("close");
  }, [r]), P = qe((E, V) => {
    V.preventDefault();
    const S = N.current;
    if (!S) return;
    const R = S.getBoundingClientRect();
    w.current = {
      dir: E,
      startX: V.clientX,
      startY: V.clientY,
      startW: R.width,
      startH: R.height
    };
    const A = (I) => {
      const O = w.current;
      if (!O) return;
      const ee = I.clientX - O.startX, Z = I.clientY - O.startY;
      let ae = O.startW, B = O.startH;
      O.dir.includes("e") && (ae = O.startW + ee), O.dir.includes("w") && (ae = O.startW - ee), O.dir.includes("s") && (B = O.startH + Z), O.dir.includes("n") && (B = O.startH - Z);
      const j = Math.max(200, ae), q = Math.max(100, B);
      v.current = j, p.current = q, _(j), b(q);
    }, T = () => {
      document.removeEventListener("mousemove", A), document.removeEventListener("mouseup", T);
      const I = v.current, O = p.current;
      (I != null || O != null) && (r("resize", {
        ...I != null ? { width: Math.round(I) + "px" } : {},
        ...O != null ? { height: Math.round(O) + "px" } : {}
      }), v.current = null, p.current = null, _(null), b(null)), w.current = null;
    };
    document.addEventListener("mousemove", A), document.addEventListener("mouseup", T);
  }, [r]), h = {
    width: f != null ? f + "px" : i,
    ...L != null ? { height: L + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, C = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: h,
      ref: N,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": C
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: C }, c), m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((E, V) => /* @__PURE__ */ e.createElement("span", { key: V, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: E })))), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: $,
        title: n["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Y, { control: s })),
    a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, a.map((E, V) => /* @__PURE__ */ e.createElement(Y, { key: V, control: E }))),
    d && Sn.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (V) => P(E, V)
      }
    ))
  );
}, { useCallback: Tn, useEffect: Rn } = e, Ln = {
  "js.drawer.close": "Close"
}, Dn = ({ controlId: l }) => {
  const t = G(), r = le(), n = oe(Ln), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, a = Tn(() => {
    r("close");
  }, [r]);
  Rn(() => {
    if (!c) return;
    const f = (_) => {
      _.key === "Escape" && a();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [c, a]);
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: a,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(Y, { control: s })));
}, { useCallback: Qe, useEffect: xn, useState: In } = e, jn = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, a = t.generation ?? 0, [m, f] = In(!1), _ = Qe(() => {
    f(!0), setTimeout(() => {
      r("dismiss", { generation: a }), f(!1);
    }, 200);
  }, [r, a]), L = Qe(() => {
    o && r(o.commandName), _();
  }, [r, o, _]);
  return xn(() => {
    if (!s || d === 0) return;
    const b = setTimeout(_, d);
    return () => clearTimeout(b);
  }, [s, d, _]), console.log("[TLSnackbar] render", { visible: s, exiting: m, generation: a, content: c, message: n }), !s && !m ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${m ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: L }, o.label)
  );
}, { useCallback: Pe, useEffect: Me, useRef: Pn, useState: Je } = e, Mn = ({ controlId: l }) => {
  const t = G(), r = le(), n = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Pn(null), [d, s] = Je({ top: 0, left: 0 }), [a, m] = Je(0), f = i.filter((v) => v.type === "item" && !v.disabled);
  Me(() => {
    var h, C;
    if (!n || !c) return;
    const v = document.getElementById(c);
    if (!v) return;
    const p = v.getBoundingClientRect(), N = ((h = o.current) == null ? void 0 : h.offsetHeight) ?? 200, w = ((C = o.current) == null ? void 0 : C.offsetWidth) ?? 200;
    let $ = p.bottom + 4, P = p.left;
    $ + N > window.innerHeight && ($ = p.top - N - 4), P + w > window.innerWidth && (P = p.right - w), s({ top: $, left: P }), m(0);
  }, [n, c]);
  const _ = Pe(() => {
    r("close");
  }, [r]), L = Pe((v) => {
    r("selectItem", { itemId: v });
  }, [r]);
  Me(() => {
    if (!n) return;
    const v = (p) => {
      o.current && !o.current.contains(p.target) && _();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [n, _]);
  const b = Pe((v) => {
    if (v.key === "Escape") {
      _();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), m((p) => (p + 1) % f.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((p) => (p - 1 + f.length) % f.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const p = f[a];
      p && L(p.id);
    }
  }, [_, L, f, a]);
  return Me(() => {
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
      onKeyDown: b
    },
    i.map((v, p) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: p, className: "tlMenu__separator" });
      const w = f.indexOf(v) === a;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (w ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: w ? 0 : -1,
          onClick: () => L(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, An = ({ controlId: l }) => {
  const t = G(), r = t.header, n = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, On = () => {
  const t = G().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, $n = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, et = 50, Bn = () => {
  const l = G(), t = le(), r = oe($n), n = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, d = l.selectionMode ?? "single", s = l.selectedCount ?? 0, a = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, f = e.useMemo(
    () => n.filter((k) => k.sortPriority && k.sortPriority > 0).length,
    [n]
  ), _ = d === "multi", L = 40, b = 20, v = e.useRef(null), p = e.useRef(null), N = e.useRef(null), [w, $] = e.useState({}), P = e.useRef(null), h = e.useRef(!1), C = e.useRef(null), [E, V] = e.useState(null), [S, R] = e.useState(null);
  e.useEffect(() => {
    P.current || $({});
  }, [n]);
  const A = e.useCallback((k) => w[k.name] ?? k.width, [w]), T = e.useMemo(() => {
    const k = [];
    let D = _ && a > 0 ? L : 0;
    for (let U = 0; U < a && U < n.length; U++)
      k.push(D), D += A(n[U]);
    return k;
  }, [n, a, _, L, A]), I = c * o, O = e.useCallback((k, D, U) => {
    U.preventDefault(), U.stopPropagation(), P.current = { column: k, startX: U.clientX, startWidth: D };
    const te = (y) => {
      const x = P.current;
      if (!x) return;
      const H = Math.max(et, x.startWidth + (y.clientX - x.startX));
      $((J) => ({ ...J, [x.column]: H }));
    }, ne = (y) => {
      document.removeEventListener("mousemove", te), document.removeEventListener("mouseup", ne);
      const x = P.current;
      if (x) {
        const H = Math.max(et, x.startWidth + (y.clientX - x.startX));
        t("columnResize", { column: x.column, width: H }), P.current = null, h.current = !0, requestAnimationFrame(() => {
          h.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", te), document.addEventListener("mouseup", ne);
  }, [t]), ee = e.useCallback(() => {
    v.current && p.current && (v.current.scrollLeft = p.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const k = p.current;
      if (!k) return;
      const D = k.scrollTop, U = Math.ceil(k.clientHeight / o), te = Math.floor(D / o);
      t("scroll", { start: te, count: U });
    }, 80);
  }, [t, o]), Z = e.useCallback((k, D, U) => {
    if (h.current) return;
    let te;
    !D || D === "desc" ? te = "asc" : te = "desc";
    const ne = U.shiftKey ? "add" : "replace";
    t("sort", { column: k, direction: te, mode: ne });
  }, [t]), ae = e.useCallback((k, D) => {
    C.current = k, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", k);
  }, []), B = e.useCallback((k, D) => {
    if (!C.current || C.current === k) {
      V(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const U = D.currentTarget.getBoundingClientRect(), te = D.clientX < U.left + U.width / 2 ? "left" : "right";
    V({ column: k, side: te });
  }, []), j = e.useCallback((k) => {
    k.preventDefault(), k.stopPropagation();
    const D = C.current;
    if (!D || !E) {
      C.current = null, V(null);
      return;
    }
    let U = n.findIndex((ne) => ne.name === E.column);
    if (U < 0) {
      C.current = null, V(null);
      return;
    }
    const te = n.findIndex((ne) => ne.name === D);
    E.side === "right" && U++, te < U && U--, t("columnReorder", { column: D, targetIndex: U }), C.current = null, V(null);
  }, [n, E, t]), q = e.useCallback(() => {
    C.current = null, V(null);
  }, []), u = e.useCallback((k, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: k,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), g = e.useCallback((k, D) => {
    D.stopPropagation(), t("select", { rowIndex: k, ctrlKey: !0, shiftKey: !1 });
  }, [t]), F = e.useCallback(() => {
    const k = s === c && c > 0;
    t("selectAll", { selected: !k });
  }, [t, s, c]), M = e.useCallback((k, D, U) => {
    U.stopPropagation(), t("expand", { rowIndex: k, expanded: D });
  }, [t]), K = e.useCallback((k, D) => {
    D.preventDefault(), R({ x: D.clientX, y: D.clientY, colIdx: k });
  }, []), X = e.useCallback(() => {
    S && (t("setFrozenColumnCount", { count: S.colIdx + 1 }), R(null));
  }, [S, t]), Q = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), R(null);
  }, [t]);
  e.useEffect(() => {
    if (!S) return;
    const k = () => R(null), D = (U) => {
      U.key === "Escape" && R(null);
    };
    return document.addEventListener("mousedown", k), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", k), document.removeEventListener("keydown", D);
    };
  }, [S]);
  const se = n.reduce((k, D) => k + A(D), 0) + (_ ? L : 0), re = s === c && c > 0, fe = s > 0 && s < c, xe = e.useCallback((k) => {
    k && (k.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (k) => {
        if (!C.current) return;
        k.preventDefault();
        const D = p.current, U = v.current;
        if (!D) return;
        const te = D.getBoundingClientRect(), ne = 40, y = 8;
        k.clientX < te.left + ne ? D.scrollLeft = Math.max(0, D.scrollLeft - y) : k.clientX > te.right - ne && (D.scrollLeft += y), U && (U.scrollLeft = D.scrollLeft);
      },
      onDrop: j
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: se } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: L,
          minWidth: L,
          ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (k) => {
          C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== C.current && V({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: xe,
          className: "tlTableView__checkbox",
          checked: re,
          onChange: F
        }
      )
    ), n.map((k, D) => {
      const U = A(k), te = D === n.length - 1;
      let ne = "tlTableView__headerCell";
      k.sortable && (ne += " tlTableView__headerCell--sortable"), E && E.column === k.name && (ne += " tlTableView__headerCell--dragOver-" + E.side);
      const y = D < a, x = D === a - 1;
      return y && (ne += " tlTableView__headerCell--frozen"), x && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.name,
          className: ne,
          style: {
            ...te && !y ? { flex: "1 0 auto", minWidth: U } : { width: U, minWidth: U },
            position: y ? "sticky" : "relative",
            ...y ? { left: T[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: k.sortable ? (H) => Z(k.name, k.sortDirection, H) : void 0,
          onContextMenu: (H) => K(D, H),
          onDragStart: (H) => ae(k.name, H),
          onDragOver: (H) => B(k.name, H),
          onDrop: j,
          onDragEnd: q
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, k.label),
        k.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, k.sortDirection === "asc" ? "▲" : "▼", f > 1 && k.sortPriority != null && k.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, k.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (H) => O(k.name, U, H)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (k) => {
          if (C.current && n.length > 0) {
            const D = n[n.length - 1];
            D.name !== C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", V({ column: D.name, side: "right" }));
          }
        },
        onDrop: j
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: p,
        className: "tlTableView__body",
        onScroll: ee
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: I, position: "relative", minWidth: se } }, i.map((k) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlTableView__row" + (k.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: k.index * o,
            height: o,
            minWidth: se,
            width: "100%"
          },
          onClick: (D) => u(k.index, D)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: L,
              minWidth: L,
              ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: k.selected,
              onChange: () => {
              },
              onClick: (D) => g(k.index, D),
              tabIndex: -1
            }
          )
        ),
        n.map((D, U) => {
          const te = A(D), ne = U === n.length - 1, y = U < a, x = U === a - 1;
          let H = "tlTableView__cell";
          y && (H += " tlTableView__cell--frozen"), x && (H += " tlTableView__cell--frozenLast");
          const J = m && U === 0, ve = k.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: H,
              style: {
                ...ne && !y ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...y ? { position: "sticky", left: T[U], zIndex: 2 } : {}
              }
            },
            J ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ve * b } }, k.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pt) => M(k.index, !k.expanded, pt)
              },
              k.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: k.cells[D.name] })) : /* @__PURE__ */ e.createElement(Y, { control: k.cells[D.name] })
          );
        })
      )))
    ),
    S && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: S.y, left: S.x, zIndex: 1e4 },
        onMouseDown: (k) => k.stopPropagation()
      },
      S.colIdx + 1 !== a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: X }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      a > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Q }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Fn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ct = e.createContext(Fn), { useMemo: Hn, useRef: zn, useState: Wn, useEffect: Un } = e, Vn = 320, Kn = ({ controlId: l }) => {
  const t = G(), r = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, d = zn(null), [s, a] = Wn(
    n === "top" ? "top" : "side"
  );
  Un(() => {
    if (n !== "auto") {
      a(n);
      return;
    }
    const b = d.current;
    if (!b) return;
    const v = new ResizeObserver((p) => {
      for (const N of p) {
        const $ = N.contentRect.width / r;
        a($ < Vn ? "top" : "side");
      }
    });
    return v.observe(b), () => v.disconnect();
  }, [n, r]);
  const m = Hn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), _ = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, L = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: L, style: _, ref: d }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useCallback: Yn } = e, Gn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Xn = ({ controlId: l }) => {
  const t = G(), r = le(), n = oe(Gn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], f = c != null || i.length > 0 || o, _ = Yn(() => {
    r("toggleCollapse");
  }, [r]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    a ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useContext: qn, useState: Zn, useCallback: Qn } = e, Jn = ({ controlId: l }) => {
  const t = G(), r = qn(ct), n = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? r.resolvedLabelPosition, a = t.fullLine === !0, m = t.visible !== !1, f = t.field, _ = r.readOnly, [L, b] = Zn(!1), v = Qn(() => b((w) => !w), []);
  if (!m) return null;
  const p = i != null, N = [
    "tlFormField",
    `tlFormField--${s}`,
    _ ? "tlFormField--readonly" : "",
    a ? "tlFormField--fullLine" : "",
    p ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: v,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: f })), !_ && p && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !_ && o && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, el = () => {
  const l = G(), t = le(), r = l.iconCss, n = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, d = l.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, a = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), f = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: f, href: "#", onClick: m, title: o }, a) : /* @__PURE__ */ e.createElement("span", { className: f, title: o }, a);
}, tl = 20, nl = () => {
  const l = G(), t = le(), r = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [s, a] = e.useState(-1), m = e.useRef(null), f = e.useCallback((h, C) => {
    t(C ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, C) => {
    t("select", {
      nodeId: h,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), L = e.useCallback((h, C) => {
    C.preventDefault(), t("contextMenu", { nodeId: h, x: C.clientX, y: C.clientY });
  }, [t]), b = e.useRef(null), v = e.useCallback((h, C) => {
    const E = C.getBoundingClientRect(), V = h.clientY - E.top, S = E.height / 3;
    return V < S ? "above" : V > S * 2 ? "below" : "within";
  }, []), p = e.useCallback((h, C) => {
    C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", h);
  }, []), N = e.useCallback((h, C) => {
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const E = v(C, C.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: E }), b.current = null;
    }, 50);
  }, [t, v]), w = e.useCallback((h, C) => {
    C.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const E = v(C, C.currentTarget);
    t("drop", { nodeId: h, position: E });
  }, [t, v]), $ = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((h) => {
    if (r.length === 0) return;
    let C = s;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), C = Math.min(s + 1, r.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), C = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const E = r[s];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (C = s + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const E = r[s];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const V = E.depth;
            for (let S = s - 1; S >= 0; S--)
              if (r[S].depth < V) {
                C = S;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), n === "multi" && s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), C = 0;
        break;
      case "End":
        h.preventDefault(), C = r.length - 1;
        break;
      default:
        return;
    }
    C !== s && a(C);
  }, [s, r, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    r.map((h, C) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: h.id,
        role: "treeitem",
        "aria-expanded": h.expandable ? h.expanded : void 0,
        "aria-selected": h.selected,
        "aria-level": h.depth + 1,
        className: [
          "tlTreeView__node",
          h.selected ? "tlTreeView__node--selected" : "",
          C === s ? "tlTreeView__node--focused" : "",
          o === h.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === h.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === h.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * tl },
        draggable: c,
        onClick: (E) => _(h.id, E),
        onContextMenu: (E) => L(h.id, E),
        onDragStart: (E) => p(h.id, E),
        onDragOver: i ? (E) => N(h.id, E) : void 0,
        onDrop: i ? (E) => w(h.id, E) : void 0,
        onDragEnd: $
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), f(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: h.content }))
    ))
  );
};
var Ae = { exports: {} }, ce = {}, Oe = { exports: {} }, W = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var tt;
function ll() {
  if (tt) return W;
  tt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), f = Symbol.for("react.activity"), _ = Symbol.iterator;
  function L(u) {
    return u === null || typeof u != "object" ? null : (u = _ && u[_] || u["@@iterator"], typeof u == "function" ? u : null);
  }
  var b = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, v = Object.assign, p = {};
  function N(u, g, F) {
    this.props = u, this.context = g, this.refs = p, this.updater = F || b;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(u, g) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, g, "setState");
  }, N.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function w() {
  }
  w.prototype = N.prototype;
  function $(u, g, F) {
    this.props = u, this.context = g, this.refs = p, this.updater = F || b;
  }
  var P = $.prototype = new w();
  P.constructor = $, v(P, N.prototype), P.isPureReactComponent = !0;
  var h = Array.isArray;
  function C() {
  }
  var E = { H: null, A: null, T: null, S: null }, V = Object.prototype.hasOwnProperty;
  function S(u, g, F) {
    var M = F.ref;
    return {
      $$typeof: l,
      type: u,
      key: g,
      ref: M !== void 0 ? M : null,
      props: F
    };
  }
  function R(u, g) {
    return S(u.type, g, u.props);
  }
  function A(u) {
    return typeof u == "object" && u !== null && u.$$typeof === l;
  }
  function T(u) {
    var g = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function(F) {
      return g[F];
    });
  }
  var I = /\/+/g;
  function O(u, g) {
    return typeof u == "object" && u !== null && u.key != null ? T("" + u.key) : g.toString(36);
  }
  function ee(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(C, C) : (u.status = "pending", u.then(
          function(g) {
            u.status === "pending" && (u.status = "fulfilled", u.value = g);
          },
          function(g) {
            u.status === "pending" && (u.status = "rejected", u.reason = g);
          }
        )), u.status) {
          case "fulfilled":
            return u.value;
          case "rejected":
            throw u.reason;
        }
    }
    throw u;
  }
  function Z(u, g, F, M, K) {
    var X = typeof u;
    (X === "undefined" || X === "boolean") && (u = null);
    var Q = !1;
    if (u === null) Q = !0;
    else
      switch (X) {
        case "bigint":
        case "string":
        case "number":
          Q = !0;
          break;
        case "object":
          switch (u.$$typeof) {
            case l:
            case t:
              Q = !0;
              break;
            case m:
              return Q = u._init, Z(
                Q(u._payload),
                g,
                F,
                M,
                K
              );
          }
      }
    if (Q)
      return K = K(u), Q = M === "" ? "." + O(u, 0) : M, h(K) ? (F = "", Q != null && (F = Q.replace(I, "$&/") + "/"), Z(K, g, F, "", function(fe) {
        return fe;
      })) : K != null && (A(K) && (K = R(
        K,
        F + (K.key == null || u && u.key === K.key ? "" : ("" + K.key).replace(
          I,
          "$&/"
        ) + "/") + Q
      )), g.push(K)), 1;
    Q = 0;
    var se = M === "" ? "." : M + ":";
    if (h(u))
      for (var re = 0; re < u.length; re++)
        M = u[re], X = se + O(M, re), Q += Z(
          M,
          g,
          F,
          X,
          K
        );
    else if (re = L(u), typeof re == "function")
      for (u = re.call(u), re = 0; !(M = u.next()).done; )
        M = M.value, X = se + O(M, re++), Q += Z(
          M,
          g,
          F,
          X,
          K
        );
    else if (X === "object") {
      if (typeof u.then == "function")
        return Z(
          ee(u),
          g,
          F,
          M,
          K
        );
      throw g = String(u), Error(
        "Objects are not valid as a React child (found: " + (g === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : g) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function ae(u, g, F) {
    if (u == null) return u;
    var M = [], K = 0;
    return Z(u, M, "", "", function(X) {
      return g.call(F, X, K++);
    }), M;
  }
  function B(u) {
    if (u._status === -1) {
      var g = u._result;
      g = g(), g.then(
        function(F) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = F);
        },
        function(F) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = F);
        }
      ), u._status === -1 && (u._status = 0, u._result = g);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var j = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var g = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(g)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, q = {
    map: ae,
    forEach: function(u, g, F) {
      ae(
        u,
        function() {
          g.apply(this, arguments);
        },
        F
      );
    },
    count: function(u) {
      var g = 0;
      return ae(u, function() {
        g++;
      }), g;
    },
    toArray: function(u) {
      return ae(u, function(g) {
        return g;
      }) || [];
    },
    only: function(u) {
      if (!A(u))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return u;
    }
  };
  return W.Activity = f, W.Children = q, W.Component = N, W.Fragment = r, W.Profiler = c, W.PureComponent = $, W.StrictMode = n, W.Suspense = s, W.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, W.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return E.H.useMemoCache(u);
    }
  }, W.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, W.cacheSignal = function() {
    return null;
  }, W.cloneElement = function(u, g, F) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var M = v({}, u.props), K = u.key;
    if (g != null)
      for (X in g.key !== void 0 && (K = "" + g.key), g)
        !V.call(g, X) || X === "key" || X === "__self" || X === "__source" || X === "ref" && g.ref === void 0 || (M[X] = g[X]);
    var X = arguments.length - 2;
    if (X === 1) M.children = F;
    else if (1 < X) {
      for (var Q = Array(X), se = 0; se < X; se++)
        Q[se] = arguments[se + 2];
      M.children = Q;
    }
    return S(u.type, K, M);
  }, W.createContext = function(u) {
    return u = {
      $$typeof: o,
      _currentValue: u,
      _currentValue2: u,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, u.Provider = u, u.Consumer = {
      $$typeof: i,
      _context: u
    }, u;
  }, W.createElement = function(u, g, F) {
    var M, K = {}, X = null;
    if (g != null)
      for (M in g.key !== void 0 && (X = "" + g.key), g)
        V.call(g, M) && M !== "key" && M !== "__self" && M !== "__source" && (K[M] = g[M]);
    var Q = arguments.length - 2;
    if (Q === 1) K.children = F;
    else if (1 < Q) {
      for (var se = Array(Q), re = 0; re < Q; re++)
        se[re] = arguments[re + 2];
      K.children = se;
    }
    if (u && u.defaultProps)
      for (M in Q = u.defaultProps, Q)
        K[M] === void 0 && (K[M] = Q[M]);
    return S(u, X, K);
  }, W.createRef = function() {
    return { current: null };
  }, W.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, W.isValidElement = A, W.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: B
    };
  }, W.memo = function(u, g) {
    return {
      $$typeof: a,
      type: u,
      compare: g === void 0 ? null : g
    };
  }, W.startTransition = function(u) {
    var g = E.T, F = {};
    E.T = F;
    try {
      var M = u(), K = E.S;
      K !== null && K(F, M), typeof M == "object" && M !== null && typeof M.then == "function" && M.then(C, j);
    } catch (X) {
      j(X);
    } finally {
      g !== null && F.types !== null && (g.types = F.types), E.T = g;
    }
  }, W.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, W.use = function(u) {
    return E.H.use(u);
  }, W.useActionState = function(u, g, F) {
    return E.H.useActionState(u, g, F);
  }, W.useCallback = function(u, g) {
    return E.H.useCallback(u, g);
  }, W.useContext = function(u) {
    return E.H.useContext(u);
  }, W.useDebugValue = function() {
  }, W.useDeferredValue = function(u, g) {
    return E.H.useDeferredValue(u, g);
  }, W.useEffect = function(u, g) {
    return E.H.useEffect(u, g);
  }, W.useEffectEvent = function(u) {
    return E.H.useEffectEvent(u);
  }, W.useId = function() {
    return E.H.useId();
  }, W.useImperativeHandle = function(u, g, F) {
    return E.H.useImperativeHandle(u, g, F);
  }, W.useInsertionEffect = function(u, g) {
    return E.H.useInsertionEffect(u, g);
  }, W.useLayoutEffect = function(u, g) {
    return E.H.useLayoutEffect(u, g);
  }, W.useMemo = function(u, g) {
    return E.H.useMemo(u, g);
  }, W.useOptimistic = function(u, g) {
    return E.H.useOptimistic(u, g);
  }, W.useReducer = function(u, g, F) {
    return E.H.useReducer(u, g, F);
  }, W.useRef = function(u) {
    return E.H.useRef(u);
  }, W.useState = function(u) {
    return E.H.useState(u);
  }, W.useSyncExternalStore = function(u, g, F) {
    return E.H.useSyncExternalStore(
      u,
      g,
      F
    );
  }, W.useTransition = function() {
    return E.H.useTransition();
  }, W.version = "19.2.4", W;
}
var nt;
function al() {
  return nt || (nt = 1, Oe.exports = ll()), Oe.exports;
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
var lt;
function rl() {
  if (lt) return ce;
  lt = 1;
  var l = al();
  function t(s) {
    var a = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function r() {
  }
  var n = {
    d: {
      f: r,
      r: function() {
        throw Error(t(522));
      },
      D: r,
      C: r,
      L: r,
      m: r,
      X: r,
      S: r,
      M: r
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function i(s, a, m) {
    var f = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: f == null ? null : "" + f,
      children: s,
      containerInfo: a,
      implementation: m
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(s, a) {
    if (s === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, ce.createPortal = function(s, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return i(s, a, null, m);
  }, ce.flushSync = function(s) {
    var a = o.T, m = n.p;
    try {
      if (o.T = null, n.p = 2, s) return s();
    } finally {
      o.T = a, n.p = m, n.d.f();
    }
  }, ce.preconnect = function(s, a) {
    typeof s == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, n.d.C(s, a));
  }, ce.prefetchDNS = function(s) {
    typeof s == "string" && n.d.D(s);
  }, ce.preinit = function(s, a) {
    if (typeof s == "string" && a && typeof a.as == "string") {
      var m = a.as, f = d(m, a.crossOrigin), _ = typeof a.integrity == "string" ? a.integrity : void 0, L = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? n.d.S(
        s,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: f,
          integrity: _,
          fetchPriority: L
        }
      ) : m === "script" && n.d.X(s, {
        crossOrigin: f,
        integrity: _,
        fetchPriority: L,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, ce.preinitModule = function(s, a) {
    if (typeof s == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = d(
            a.as,
            a.crossOrigin
          );
          n.d.M(s, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && n.d.M(s);
  }, ce.preload = function(s, a) {
    if (typeof s == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, f = d(m, a.crossOrigin);
      n.d.L(s, m, {
        crossOrigin: f,
        integrity: typeof a.integrity == "string" ? a.integrity : void 0,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0,
        type: typeof a.type == "string" ? a.type : void 0,
        fetchPriority: typeof a.fetchPriority == "string" ? a.fetchPriority : void 0,
        referrerPolicy: typeof a.referrerPolicy == "string" ? a.referrerPolicy : void 0,
        imageSrcSet: typeof a.imageSrcSet == "string" ? a.imageSrcSet : void 0,
        imageSizes: typeof a.imageSizes == "string" ? a.imageSizes : void 0,
        media: typeof a.media == "string" ? a.media : void 0
      });
    }
  }, ce.preloadModule = function(s, a) {
    if (typeof s == "string")
      if (a) {
        var m = d(a.as, a.crossOrigin);
        n.d.m(s, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else n.d.m(s);
  }, ce.requestFormReset = function(s) {
    n.d.r(s);
  }, ce.unstable_batchedUpdates = function(s, a) {
    return s(a);
  }, ce.useFormState = function(s, a, m) {
    return o.H.useFormState(s, a, m);
  }, ce.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var at;
function ol() {
  if (at) return Ae.exports;
  at = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ae.exports = rl(), Ae.exports;
}
var sl = ol();
const { useState: me, useCallback: ie, useRef: Ee, useEffect: he, useMemo: ze } = e;
function Ke({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function cl({
  option: l,
  removable: t,
  onRemove: r,
  removeLabel: n,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: d,
  onDragEnd: s,
  dragClassName: a
}) {
  const m = ie(
    (f) => {
      f.stopPropagation(), r(l.value);
    },
    [r, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
      draggable: c || void 0,
      onDragStart: i,
      onDragOver: o,
      onDrop: d,
      onDragEnd: s
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Ke, { image: l.image }),
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
function il({
  option: l,
  highlighted: t,
  searchTerm: r,
  onSelect: n,
  onMouseEnter: c,
  id: i
}) {
  const o = ie(() => n(l.value), [n, l.value]), d = ze(() => {
    if (!r) return l.label;
    const s = l.label.toLowerCase().indexOf(r.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + r.length)), l.label.substring(s + r.length));
  }, [l.label, r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: i,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: o,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(Ke, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const ul = ({ controlId: l, state: t }) => {
  const r = le(), n = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], f = t.emptyOptionLabel ?? "", _ = i && c && !d && s, L = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = L["js.dropdownSelect.nothingFound"], v = ie(
    (y) => L["js.dropdownSelect.removeChip"].replace("{0}", y),
    [L]
  ), [p, N] = me(!1), [w, $] = me(""), [P, h] = me(-1), [C, E] = me(!1), [V, S] = me({}), [R, A] = me(null), [T, I] = me(null), [O, ee] = me(null), Z = Ee(null), ae = Ee(null), B = Ee(null), j = Ee(n);
  j.current = n;
  const q = Ee(-1), u = ze(
    () => new Set(n.map((y) => y.value)),
    [n]
  ), g = ze(() => {
    let y = m.filter((x) => !u.has(x.value));
    if (w) {
      const x = w.toLowerCase();
      y = y.filter((H) => H.label.toLowerCase().includes(x));
    }
    return y;
  }, [m, u, w]);
  he(() => {
    w && g.length === 1 ? h(0) : h(-1);
  }, [g.length, w]), he(() => {
    p && a && ae.current && ae.current.focus();
  }, [p, a, n]), he(() => {
    var H, J;
    if (q.current < 0) return;
    const y = q.current;
    q.current = -1;
    const x = (H = Z.current) == null ? void 0 : H.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(y, x.length - 1)].focus() : (J = Z.current) == null || J.focus();
  }, [n]), he(() => {
    if (!p) return;
    const y = (x) => {
      Z.current && !Z.current.contains(x.target) && B.current && !B.current.contains(x.target) && (N(!1), $(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [p]), he(() => {
    if (!p || !Z.current) return;
    const y = Z.current.getBoundingClientRect(), x = window.innerHeight - y.bottom, J = x < 300 && y.top > x;
    S({
      left: y.left,
      width: y.width,
      ...J ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [p]);
  const F = ie(async () => {
    if (!(d || !s) && (N(!0), $(""), h(-1), E(!1), !a))
      try {
        await r("loadOptions");
      } catch {
        E(!0);
      }
  }, [d, s, a, r]), M = ie(() => {
    var y;
    N(!1), $(""), h(-1), (y = Z.current) == null || y.focus();
  }, []), K = ie(
    (y) => {
      let x;
      if (c) {
        const H = m.find((J) => J.value === y);
        if (H)
          x = [...j.current, H];
        else
          return;
      } else {
        const H = m.find((J) => J.value === y);
        if (H)
          x = [H];
        else
          return;
      }
      j.current = x, r("valueChanged", { value: x.map((H) => H.value) }), c ? ($(""), h(-1)) : M();
    },
    [c, m, r, M]
  ), X = ie(
    (y) => {
      q.current = j.current.findIndex((H) => H.value === y);
      const x = j.current.filter((H) => H.value !== y);
      j.current = x, r("valueChanged", { value: x.map((H) => H.value) });
    },
    [r]
  ), Q = ie(
    (y) => {
      y.stopPropagation(), r("valueChanged", { value: [] }), M();
    },
    [r, M]
  ), se = ie((y) => {
    $(y.target.value);
  }, []), re = ie(
    (y) => {
      if (!p) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), F();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), h(
            (x) => x < g.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), h(
            (x) => x > 0 ? x - 1 : g.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), P >= 0 && P < g.length && K(g[P].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), M();
          break;
        case "Tab":
          M();
          break;
        case "Backspace":
          w === "" && c && n.length > 0 && X(n[n.length - 1].value);
          break;
      }
    },
    [
      p,
      F,
      M,
      g,
      P,
      K,
      w,
      c,
      n,
      X
    ]
  ), fe = ie(
    async (y) => {
      y.preventDefault(), E(!1);
      try {
        await r("loadOptions");
      } catch {
        E(!0);
      }
    },
    [r]
  ), xe = ie(
    (y, x) => {
      A(y), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), k = ie(
    (y, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", R === null || R === y) {
        I(null), ee(null);
        return;
      }
      const H = x.currentTarget.getBoundingClientRect(), J = H.left + H.width / 2, ve = x.clientX < J ? "before" : "after";
      I(y), ee(ve);
    },
    [R]
  ), D = ie(
    (y) => {
      if (y.preventDefault(), R === null || T === null || O === null || R === T) return;
      const x = [...j.current], [H] = x.splice(R, 1);
      let J = T;
      R < T ? J = O === "before" ? J - 1 : J : J = O === "before" ? J : J + 1, x.splice(J, 0, H), j.current = x, r("valueChanged", { value: x.map((ve) => ve.value) }), A(null), I(null), ee(null);
    },
    [R, T, O, r]
  ), U = ie(() => {
    A(null), I(null), ee(null);
  }, []);
  if (he(() => {
    if (P < 0 || !B.current) return;
    const y = B.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, f) : n.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ke, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const te = !o && n.length > 0 && !d, ne = p ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: V
    },
    (a || C) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: ae,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
        onChange: se,
        onKeyDown: re,
        placeholder: L["js.dropdownSelect.filterPlaceholder"],
        "aria-label": L["js.dropdownSelect.filterPlaceholder"],
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
      !a && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: fe }, L["js.dropdownSelect.error"])),
      a && g.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      a && g.map((y, x) => /* @__PURE__ */ e.createElement(
        il,
        {
          key: y.value,
          id: `${l}-opt-${x}`,
          option: y,
          highlighted: x === P,
          searchTerm: w,
          onSelect: K,
          onMouseEnter: () => h(x)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: Z,
      className: "tlDropdownSelect" + (p ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": p,
      "aria-haspopup": "listbox",
      "aria-owns": p ? `${l}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: p ? void 0 : F,
      onKeyDown: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, f) : n.map((y, x) => {
      let H = "";
      return R === x ? H = "tlDropdownSelect__chip--dragging" : T === x && O === "before" ? H = "tlDropdownSelect__chip--dropBefore" : T === x && O === "after" && (H = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        cl,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: X,
          removeLabel: v(y.label),
          draggable: _,
          onDragStart: _ ? (J) => xe(x, J) : void 0,
          onDragOver: _ ? (J) => k(x, J) : void 0,
          onDrop: _ ? D : void 0,
          onDragEnd: _ ? U : void 0,
          dragClassName: _ ? H : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, te && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": L["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, p ? "▲" : "▼"))
  ), ne && sl.createPortal(ne, document.body));
}, { useCallback: $e, useRef: dl } = e, it = "application/x-tl-color", ml = ({
  colors: l,
  columns: t,
  onSelect: r,
  onConfirm: n,
  onSwap: c,
  onReplace: i
}) => {
  const o = dl(null), d = $e(
    (m) => (f) => {
      o.current = m, f.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = $e((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = $e(
    (m) => (f) => {
      f.preventDefault();
      const _ = f.dataTransfer.getData(it);
      _ ? i(m, _) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
    },
    [c, i]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((m, f) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: f,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? d(f) : void 0,
        onDragOver: s,
        onDrop: a(f)
      }
    ))
  );
};
function ut(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function We(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function dt(l) {
  if (!We(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function mt(l, t, r) {
  const n = (c) => ut(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(r);
}
function pl(l, t, r) {
  const n = l / 255, c = t / 255, i = r / 255, o = Math.max(n, c, i), d = Math.min(n, c, i), s = o - d;
  let a = 0;
  s !== 0 && (o === n ? a = (c - i) / s % 6 : o === c ? a = (i - n) / s + 2 : a = (n - c) / s + 4, a *= 60, a < 0 && (a += 360));
  const m = o === 0 ? 0 : s / o;
  return [a, m, o];
}
function fl(l, t, r) {
  const n = r * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), i = r - n;
  let o = 0, d = 0, s = 0;
  return l < 60 ? (o = n, d = c, s = 0) : l < 120 ? (o = c, d = n, s = 0) : l < 180 ? (o = 0, d = n, s = c) : l < 240 ? (o = 0, d = c, s = n) : l < 300 ? (o = c, d = 0, s = n) : (o = n, d = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((d + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function hl(l) {
  return pl(...dt(l));
}
function Be(l, t, r) {
  return mt(...fl(l, t, r));
}
const { useCallback: _e, useRef: rt } = e, _l = ({ color: l, onColorChange: t }) => {
  const [r, n, c] = hl(l), i = rt(null), o = rt(null), d = _e(
    (b, v) => {
      var $;
      const p = ($ = i.current) == null ? void 0 : $.getBoundingClientRect();
      if (!p) return;
      const N = Math.max(0, Math.min(1, (b - p.left) / p.width)), w = Math.max(0, Math.min(1, 1 - (v - p.top) / p.height));
      t(Be(r, N, w));
    },
    [r, t]
  ), s = _e(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), d(b.clientX, b.clientY);
    },
    [d]
  ), a = _e(
    (b) => {
      b.buttons !== 0 && d(b.clientX, b.clientY);
    },
    [d]
  ), m = _e(
    (b) => {
      var w;
      const v = (w = o.current) == null ? void 0 : w.getBoundingClientRect();
      if (!v) return;
      const N = Math.max(0, Math.min(1, (b - v.top) / v.height)) * 360;
      t(Be(N, n, c));
    },
    [n, c, t]
  ), f = _e(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), _ = _e(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), L = Be(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: L },
      onPointerDown: s,
      onPointerMove: a
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
      onPointerDown: f,
      onPointerMove: _
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${r / 360 * 100}%` }
      }
    )
  ));
};
function bl(l, t) {
  const r = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === r);
}
const vl = {
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
}, { useState: we, useCallback: de, useEffect: Fe, useRef: El, useLayoutEffect: gl } = e, Cl = ({
  anchorRef: l,
  currentColor: t,
  palette: r,
  paletteColumns: n,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: d,
  onPaletteChange: s
}) => {
  const [a, m] = we("palette"), [f, _] = we(t), L = El(null), b = oe(vl), [v, p] = we(null);
  gl(() => {
    if (!l.current || !L.current) return;
    const B = l.current.getBoundingClientRect(), j = L.current.getBoundingClientRect();
    let q = B.bottom + 4, u = B.left;
    q + j.height > window.innerHeight && (q = B.top - j.height - 4), u + j.width > window.innerWidth && (u = Math.max(0, B.right - j.width)), p({ top: q, left: u });
  }, [l]);
  const N = f != null, [w, $, P] = N ? dt(f) : [0, 0, 0], [h, C] = we((f == null ? void 0 : f.toUpperCase()) ?? "");
  Fe(() => {
    C((f == null ? void 0 : f.toUpperCase()) ?? "");
  }, [f]), Fe(() => {
    const B = (j) => {
      j.key === "Escape" && d();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [d]), Fe(() => {
    const B = (q) => {
      L.current && !L.current.contains(q.target) && d();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [d]);
  const E = de(
    (B) => (j) => {
      const q = parseInt(j.target.value, 10);
      if (isNaN(q)) return;
      const u = ut(q);
      _(mt(B === "r" ? u : w, B === "g" ? u : $, B === "b" ? u : P));
    },
    [w, $, P]
  ), V = de(
    (B) => {
      if (f != null) {
        B.dataTransfer.setData(it, f.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = f, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [f]
  ), S = de((B) => {
    const j = B.target.value;
    C(j), We(j) && _(j);
  }, []), R = de(() => {
    _(null);
  }, []), A = de((B) => {
    _(B);
  }, []), T = de(
    (B) => {
      o(B);
    },
    [o]
  ), I = de(
    (B, j) => {
      const q = [...r], u = q[B];
      q[B] = q[j], q[j] = u, s(q);
    },
    [r, s]
  ), O = de(
    (B, j) => {
      const q = [...r];
      q[B] = j, s(q);
    },
    [r, s]
  ), ee = de(() => {
    s([...c]);
  }, [c, s]), Z = de(
    (B) => {
      if (bl(r, B)) return;
      const j = r.indexOf(null);
      if (j < 0) return;
      const q = [...r];
      q[j] = B.toUpperCase(), s(q);
    },
    [r, s]
  ), ae = de(() => {
    f != null && Z(f), o(f);
  }, [f, o, Z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: L,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      ml,
      {
        colors: r,
        columns: n,
        onSelect: A,
        onConfirm: T,
        onSwap: I,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(_l, { color: f ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: f } : void 0,
        draggable: N,
        onDragStart: N ? V : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? w : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? $ : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? P : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !We(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: ae }, b["js.colorInput.ok"]))
  );
}, yl = { "js.colorInput.chooseColor": "Choose color" }, { useState: wl, useCallback: ke, useRef: kl } = e, Sl = ({ controlId: l, state: t }) => {
  const r = le(), n = oe(yl), [c, i] = wl(!1), o = kl(null), d = t.value, s = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, f = t.defaultPalette ?? a, _ = ke(() => {
    s && i(!0);
  }, [s]), L = ke(
    (p) => {
      i(!1), r("valueChanged", { value: p });
    },
    [r]
  ), b = ke(() => {
    i(!1);
  }, []), v = ke(
    (p) => {
      r("paletteChanged", { palette: p });
    },
    [r]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (d == null ? " tlColorInput__swatch--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: d ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Cl,
    {
      anchorRef: o,
      currentColor: d,
      palette: a,
      paletteColumns: m,
      defaultPalette: f,
      canReset: t.canReset !== !1,
      onConfirm: L,
      onCancel: b,
      onPaletteChange: v
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
}, { useState: ge, useCallback: pe, useEffect: Se, useRef: ot, useLayoutEffect: Nl, useMemo: Tl } = e, Rl = {
  "js.iconSelect.simpleTab": "Simple",
  "js.iconSelect.advancedTab": "Advanced",
  "js.iconSelect.filterPlaceholder": "Filter icons…",
  "js.iconSelect.noResults": "No icons found",
  "js.iconSelect.loading": "Loading…",
  "js.iconSelect.loadError": "Failed to load. Click to retry.",
  "js.iconSelect.classLabel": "Class",
  "js.iconSelect.previewLabel": "Preview",
  "js.iconSelect.cancel": "Cancel",
  "js.iconSelect.ok": "OK",
  "js.iconSelect.clear": "Clear icon",
  "js.iconSelect.clearFilter": "Clear filter"
};
function De({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const r = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const r = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const Ll = ({
  anchorRef: l,
  currentValue: t,
  icons: r,
  iconsLoaded: n,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const d = oe(Rl), [s, a] = ge("simple"), [m, f] = ge(""), [_, L] = ge(t ?? ""), [b, v] = ge(!1), [p, N] = ge(null), w = ot(null), $ = ot(null);
  Nl(() => {
    if (!l.current || !w.current) return;
    const T = l.current.getBoundingClientRect(), I = w.current.getBoundingClientRect();
    let O = T.bottom + 4, ee = T.left;
    O + I.height > window.innerHeight && (O = T.top - I.height - 4), ee + I.width > window.innerWidth && (ee = Math.max(0, T.right - I.width)), N({ top: O, left: ee });
  }, [l]), Se(() => {
    !n && !b && o().catch(() => v(!0));
  }, [n, b, o]), Se(() => {
    n && $.current && $.current.focus();
  }, [n]), Se(() => {
    const T = (I) => {
      I.key === "Escape" && i();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [i]), Se(() => {
    const T = (O) => {
      w.current && !w.current.contains(O.target) && i();
    }, I = setTimeout(() => document.addEventListener("mousedown", T), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", T);
    };
  }, [i]);
  const P = Tl(() => {
    if (!m) return r;
    const T = m.toLowerCase();
    return r.filter(
      (I) => I.prefix.toLowerCase().includes(T) || I.label.toLowerCase().includes(T) || I.terms != null && I.terms.some((O) => O.includes(T))
    );
  }, [r, m]), h = pe((T) => {
    f(T.target.value);
  }, []), C = pe(
    (T) => {
      c(T);
    },
    [c]
  ), E = pe((T) => {
    L(T);
  }, []), V = pe((T) => {
    L(T.target.value);
  }, []), S = pe(() => {
    c(_ || null);
  }, [_, c]), R = pe(() => {
    c(null);
  }, [c]), A = pe(async (T) => {
    T.preventDefault(), v(!1);
    try {
      await o();
    } catch {
      v(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: w,
      style: p ? { top: p.top, left: p.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("simple")
      },
      d["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("advanced")
      },
      d["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: $,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: h,
        placeholder: d["js.iconSelect.filterPlaceholder"],
        "aria-label": d["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => f(""),
        title: d["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !n && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: A }, d["js.iconSelect.loadError"])),
      n && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, d["js.iconSelect.noResults"]),
      n && P.map(
        (T) => T.variants.map((I) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: I.encoded,
            className: "tlIconSelect__iconCell" + (I.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": I.encoded === t,
            tabIndex: 0,
            title: T.label,
            onClick: () => s === "simple" ? C(I.encoded) : E(I.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), s === "simple" ? C(I.encoded) : E(I.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(De, { encoded: I.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: V
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(De, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, d["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, d["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, d["js.iconSelect.ok"]))
  );
}, Dl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: xl, useCallback: Ne, useRef: Il } = e, jl = ({ controlId: l, state: t }) => {
  const r = le(), n = oe(Dl), [c, i] = xl(!1), o = Il(null), d = t.value, s = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], f = t.iconsLoaded === !0, _ = Ne(() => {
    s && !a && i(!0);
  }, [s, a]), L = Ne(
    (p) => {
      i(!1), r("valueChanged", { value: p });
    },
    [r]
  ), b = Ne(() => {
    i(!1);
  }, []), v = Ne(async () => {
    await r("loadIcons");
  }, [r]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (d == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: a,
      title: d ?? "",
      "aria-label": n["js.iconSelect.chooseIcon"]
    },
    d ? /* @__PURE__ */ e.createElement(De, { encoded: d }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ll,
    {
      anchorRef: o,
      currentValue: d,
      icons: m,
      iconsLoaded: f,
      onSelect: L,
      onCancel: b,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, d ? /* @__PURE__ */ e.createElement(De, { encoded: d }) : null));
};
z("TLButton", Tt);
z("TLToggleButton", Lt);
z("TLTextInput", _t);
z("TLNumberInput", vt);
z("TLDatePicker", gt);
z("TLSelect", yt);
z("TLCheckbox", kt);
z("TLTable", St);
z("TLCounter", Dt);
z("TLTabBar", It);
z("TLFieldList", jt);
z("TLAudioRecorder", Mt);
z("TLAudioPlayer", Ot);
z("TLFileUpload", Bt);
z("TLDownload", Ht);
z("TLPhotoCapture", Wt);
z("TLPhotoViewer", Vt);
z("TLSplitPanel", Kt);
z("TLPanel", Jt);
z("TLMaximizeRoot", en);
z("TLDeckPane", tn);
z("TLSidebar", un);
z("TLStack", dn);
z("TLGrid", mn);
z("TLCard", pn);
z("TLAppBar", fn);
z("TLBreadcrumb", _n);
z("TLBottomBar", vn);
z("TLDialog", gn);
z("TLDialogManager", wn);
z("TLWindow", Nn);
z("TLDrawer", Dn);
z("TLSnackbar", jn);
z("TLMenu", Mn);
z("TLAppShell", An);
z("TLTextCell", On);
z("TLTableView", Bn);
z("TLFormLayout", Kn);
z("TLFormGroup", Xn);
z("TLFormField", Jn);
z("TLResourceCell", el);
z("TLTreeView", nl);
z("TLDropdownSelect", ul);
z("TLColorInput", Sl);
z("TLIconSelect", jl);
