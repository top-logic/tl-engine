import { React as e, useTLFieldValue as Ce, getComponent as ft, useTLState as K, useTLCommand as le, TLChild as Y, useTLUpload as We, useI18N as oe, useTLDataUrl as Ve, register as H } from "tl-react-bridge";
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
  const c = K(), i = le(), o = t ?? "click", d = r ?? c.label, s = n ?? c.disabled === !0, a = Nt(() => {
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
  const i = K(), o = le(), d = t ?? "click", s = r ?? i.label, a = n ?? i.active === !0, m = c ?? i.disabled === !0, h = Rt(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: h,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    s
  );
}, Dt = ({ controlId: l }) => {
  const t = K(), r = le(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: xt } = e, It = ({ controlId: l }) => {
  const t = K(), r = le(), n = t.tabs ?? [], c = t.activeTabId, i = xt((o) => {
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
  const t = K(), r = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Pt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Mt = ({ controlId: l }) => {
  const t = K(), r = We(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", h = t.error, _ = m === "received" ? "idle" : n !== "idle" ? n : m, L = e.useCallback(async () => {
    if (n === "recording") {
      const k = d.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = k, s.current = [];
        const O = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, O ? { mimeType: O } : void 0);
        d.current = P, P.ondataavailable = (f) => {
          f.data.size > 0 && s.current.push(f.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((g) => g.stop()), a.current = null;
          const f = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], f.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const E = new FormData();
          E.append("audio", f, "recording.webm"), await r(E), c("idle");
        }, P.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), o("js.audioRecorder.error.denied"), c("idle");
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), h && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h));
}, At = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ot = ({ controlId: l }) => {
  const t = K(), r = Ve(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), a = e.useRef(c);
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
  }, [i, r]), h = oe(At), _ = i === "loading" ? h["js.loading"] : i === "playing" ? h["js.audioPlayer.pause"] : i === "disabled" ? h["js.audioPlayer.noAudio"] : h["js.audioPlayer.play"], L = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
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
  const t = K(), r = We(), [n, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", a = t.error, m = t.accept ?? "", h = s === "received" ? "idle" : n !== "idle" ? n : s, _ = e.useCallback(async (f) => {
    c("uploading");
    const E = new FormData();
    E.append("file", f, f.name), await r(E), c("idle");
  }, [r]), L = e.useCallback((f) => {
    var g;
    const E = (g = f.target.files) == null ? void 0 : g[0];
    E && _(E);
  }, [_]), b = e.useCallback(() => {
    var f;
    n !== "uploading" && ((f = d.current) == null || f.click());
  }, [n]), v = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), p = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), N = e.useCallback((f) => {
    var g;
    if (f.preventDefault(), f.stopPropagation(), o(!1), n === "uploading") return;
    const E = (g = f.dataTransfer.files) == null ? void 0 : g[0];
    E && _(E);
  }, [n, _]), k = h === "uploading", O = oe($t), P = h === "uploading" ? O["js.uploading"] : O["js.fileUpload.choose"];
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
        className: "tlFileUpload__button" + (h === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: k,
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
  const t = K(), r = Ve(), n = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      a(!0);
      try {
        const b = r + (r.includes("?") ? "&" : "?") + "rev=" + i, v = await fetch(b);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const p = await v.blob(), N = URL.createObjectURL(p), k = document.createElement("a");
        k.href = N, k.download = o, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(N);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        a(!1);
      }
    }
  }, [c, s, r, i, o]), h = e.useCallback(async () => {
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
      onClick: h,
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
}, Ut = ({ controlId: l }) => {
  const t = K(), r = We(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), h = e.useRef(null), _ = e.useRef(null), L = e.useRef(null), b = t.error, v = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), p = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    p(), c("idle");
  }, [p]), k = e.useCallback(async () => {
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
  }, [n, v]), O = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const R = a.current, A = h.current;
    if (!R || !A)
      return;
    A.width = R.videoWidth, A.height = R.videoHeight;
    const S = A.getContext("2d");
    S && (S.drawImage(R, 0, 0), p(), c("uploading"), A.toBlob(async (D) => {
      if (!D) {
        c("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", D, "capture.jpg"), await r(V), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, r, p]), P = e.useCallback(async (R) => {
    var D;
    const A = (D = R.target.files) == null ? void 0 : D[0];
    if (!A) return;
    c("uploading");
    const S = new FormData();
    S.append("photo", A, A.name), await r(S), c("idle"), _.current && (_.current.value = "");
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
  const f = oe(zt), E = n === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const X = ["tlPhotoCapture__overlayVideo"];
  d && X.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return d && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: k,
      disabled: n === "uploading",
      title: E,
      "aria-label": E
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: h, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        className: X.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: T.join(" "),
        onClick: () => s((R) => !R),
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Wt = {
  "js.photoViewer.alt": "Captured photo"
}, Vt = ({ controlId: l }) => {
  const t = K(), r = Ve(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), d = e.useRef(c);
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
        const h = await m.blob();
        a || o(URL.createObjectURL(h));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [n, c, r]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(Wt);
  return !n || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ye, useRef: Ie } = e, Kt = ({ controlId: l }) => {
  const t = K(), r = le(), n = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = n === "horizontal", d = i.length > 0 && i.every((p) => p.collapsed), s = !d && i.some((p) => p.collapsed), a = d ? !o : o, m = Ie(null), h = Ie(null), _ = Ie(null), L = Ye((p, N) => {
    const k = {
      overflow: p.scrolling || "auto"
    };
    return p.collapsed ? d && !a ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : N !== void 0 ? k.flex = `0 0 ${N}px` : p.unit === "%" || s ? k.flex = `${p.size} 0 0%` : k.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (k.minWidth = o ? p.minSize : void 0, k.minHeight = o ? void 0 : p.minSize), k;
  }, [o, d, s, a]), b = Ye((p, N) => {
    p.preventDefault();
    const k = m.current;
    if (!k) return;
    const O = i[N], P = i[N + 1], f = k.querySelectorAll(":scope > .tlSplitPanel__child"), E = [];
    f.forEach((T) => {
      E.push(o ? T.offsetWidth : T.offsetHeight);
    }), _.current = E, h.current = {
      splitterIndex: N,
      startPos: o ? p.clientX : p.clientY,
      startSizeBefore: E[N],
      startSizeAfter: E[N + 1],
      childBefore: O,
      childAfter: P
    };
    const g = (T) => {
      const R = h.current;
      if (!R || !_.current) return;
      const S = (o ? T.clientX : T.clientY) - R.startPos, D = R.childBefore.minSize || 0, V = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + S, q = R.startSizeAfter - S;
      ee < D && (q += ee - D, ee = D), q < V && (ee += q - V, q = V), _.current[R.splitterIndex] = ee, _.current[R.splitterIndex + 1] = q;
      const ae = k.querySelectorAll(":scope > .tlSplitPanel__child"), B = ae[R.splitterIndex], j = ae[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${q}px`);
    }, X = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", X), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const T = {};
        i.forEach((R, A) => {
          const S = R.control;
          S != null && S.controlId && _.current && (T[S.controlId] = _.current[A]);
        }), r("updateSizes", { sizes: T });
      }
      _.current = null, h.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", X), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
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
      const k = i[N + 1];
      !p.collapsed && !k.collapsed && v.push(
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
  const t = K(), r = le(), n = oe(Yt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, a = t.toolbarButtons ?? [], m = i === "MINIMIZED", h = i === "MAXIMIZED", _ = i === "HIDDEN", L = je(() => {
    r("toggleMinimize");
  }, [r]), b = je(() => {
    r("toggleMaximize");
  }, [r]), v = je(() => {
    r("popOut");
  }, [r]);
  if (_)
    return null;
  const p = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: p
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, a.map((N, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: N }))), o && !h && /* @__PURE__ */ e.createElement(
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
        title: h ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(Zt, null) : /* @__PURE__ */ e.createElement(qt, null)
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
  const t = K();
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
  const t = K();
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
  setItemRef: h,
  onItemFocus: _,
  flyoutGroupId: L,
  onOpenFlyout: b,
  onCloseFlyout: v
}) => {
  const p = Le(null), [N, k] = Te(null), O = ue(() => {
    n ? L === l.id ? v() : (p.current && k(p.current.getBoundingClientRect()), b(l.id)) : o(l.id);
  }, [n, L, l.id, o, b, v]), P = ue((E) => {
    p.current = E, s(E);
  }, [s]), f = n && L === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: O,
      title: n ? l.label : void 0,
      "aria-expanded": n ? f : t,
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
  ), f && /* @__PURE__ */ e.createElement(
    sn,
    {
      item: l,
      activeItemId: r,
      anchorRect: N,
      onSelect: c,
      onExecute: i,
      onClose: v
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((E) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: E.id,
      item: E,
      activeItemId: r,
      collapsed: n,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: h,
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
  onOpenFlyout: h,
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
          onOpenFlyout: h,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, un = ({ controlId: l }) => {
  const t = K(), r = le(), n = oe(nn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = Te(() => {
    const T = /* @__PURE__ */ new Map(), R = (A) => {
      for (const S of A)
        S.type === "group" && (T.set(S.id, S.expanded), R(S.children));
    };
    return R(c), T;
  }), a = ue((T) => {
    s((R) => {
      const A = new Map(R), S = A.get(T) ?? !1;
      return A.set(T, !S), r("toggleGroup", { itemId: T, expanded: !S }), A;
    });
  }, [r]), m = ue((T) => {
    T !== i && r("selectItem", { itemId: T });
  }, [r, i]), h = ue((T) => {
    r("executeCommand", { itemId: T });
  }, [r]), _ = ue(() => {
    r("toggleCollapse", {});
  }, [r]), [L, b] = Te(null), v = ue((T) => {
    b(T);
  }, []), p = ue(() => {
    b(null);
  }, []);
  Re(() => {
    o || b(null);
  }, [o]);
  const [N, k] = Te(() => {
    const T = He(c, o, d);
    return T.length > 0 ? T[0].id : "";
  }), O = Le(/* @__PURE__ */ new Map()), P = ue((T) => (R) => {
    R ? O.current.set(T, R) : O.current.delete(T);
  }, []), f = ue((T) => {
    k(T);
  }, []), E = Le(0), g = ue((T) => {
    k(T), E.current++;
  }, []);
  Re(() => {
    const T = O.current.get(N);
    T && document.activeElement !== T && T.focus();
  }, [N, E.current]);
  const X = ue((T) => {
    if (T.key === "Escape" && L !== null) {
      T.preventDefault(), p();
      return;
    }
    const R = He(c, o, d);
    if (R.length === 0) return;
    const A = R.findIndex((D) => D.id === N);
    if (A < 0) return;
    const S = R[A];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const D = (A + 1) % R.length;
        g(R[D].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const D = (A - 1 + R.length) % R.length;
        g(R[D].id);
        break;
      }
      case "Home": {
        T.preventDefault(), g(R[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), g(R[R.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), S.type === "nav" ? m(S.id) : S.type === "command" ? h(S.id) : S.type === "group" && (o ? L === S.id ? p() : v(S.id) : a(S.id));
        break;
      }
      case "ArrowRight": {
        S.type === "group" && !o && ((d.get(S.id) ?? !1) || (T.preventDefault(), a(S.id)));
        break;
      }
      case "ArrowLeft": {
        S.type === "group" && !o && (d.get(S.id) ?? !1) && (T.preventDefault(), a(S.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    N,
    L,
    g,
    m,
    h,
    a,
    v,
    p
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: X }, c.map((T) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: T.id,
      item: T,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: h,
      onToggleGroup: a,
      focusedId: N,
      setItemRef: P,
      onItemFocus: f,
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
  const t = K(), r = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, o.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })));
}, mn = ({ controlId: l }) => {
  const t = K(), r = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : r && (o.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((d, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: d })));
}, pn = ({ controlId: l }) => {
  const t = K(), r = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, d = r != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, fn = ({ controlId: l }) => {
  const t = K(), r = t.title ?? "", n = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s }))));
}, { useCallback: hn } = e, _n = ({ controlId: l }) => {
  const t = K(), r = le(), n = t.items ?? [], c = hn((i) => {
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
  const t = K(), r = le(), n = t.items ?? [], c = t.activeItemId, i = bn((o) => {
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
  const t = K(), r = le(), n = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = En(null), d = Ge(() => {
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
  const r = K().dialogs ?? [], n = yn(r.length);
  return Cn(() => {
    r.length < n.current && r.length > 0, n.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, r.map((c) => /* @__PURE__ */ e.createElement(Y, { key: c.controlId, control: c })));
}, { useCallback: qe, useRef: ye, useState: Ze } = e, kn = {
  "js.window.close": "Close"
}, Sn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Nn = ({ controlId: l }) => {
  const t = K(), r = le(), n = oe(kn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, d = t.resizable === !0, s = t.child, a = t.actions ?? [], [m, h] = Ze(null), [_, L] = Ze(null), b = ye(null), v = ye(null), p = ye(null), N = ye(null), k = qe(() => {
    r("close");
  }, [r]), O = qe((E, g) => {
    g.preventDefault();
    const X = p.current;
    if (!X) return;
    const T = X.getBoundingClientRect();
    N.current = {
      dir: E,
      startX: g.clientX,
      startY: g.clientY,
      startW: T.width,
      startH: T.height
    };
    const R = (S) => {
      const D = N.current;
      if (!D) return;
      const V = S.clientX - D.startX, ee = S.clientY - D.startY;
      let q = D.startW, ae = D.startH;
      D.dir.includes("e") && (q = D.startW + V), D.dir.includes("w") && (q = D.startW - V), D.dir.includes("s") && (ae = D.startH + ee), D.dir.includes("n") && (ae = D.startH - ee);
      const B = Math.max(200, q), j = Math.max(100, ae);
      b.current = B, v.current = j, h(B), L(j);
    }, A = () => {
      document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", A);
      const S = b.current, D = v.current;
      (S != null || D != null) && (r("resize", {
        ...S != null ? { width: Math.round(S) + "px" } : {},
        ...D != null ? { height: Math.round(D) + "px" } : {}
      }), b.current = null, v.current = null, h(null), L(null)), N.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", A);
  }, [r]), P = {
    width: m != null ? m + "px" : i,
    ..._ != null ? { height: _ + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, f = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: P,
      ref: p,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: f }, c), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: k,
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
    a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, a.map((E, g) => /* @__PURE__ */ e.createElement(Y, { key: g, control: E }))),
    d && Sn.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (g) => O(E, g)
      }
    ))
  );
}, { useCallback: Tn, useEffect: Rn } = e, Ln = {
  "js.drawer.close": "Close"
}, Dn = ({ controlId: l }) => {
  const t = K(), r = le(), n = oe(Ln), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, a = Tn(() => {
    r("close");
  }, [r]);
  Rn(() => {
    if (!c) return;
    const h = (_) => {
      _.key === "Escape" && a();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
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
  const t = K(), r = le(), n = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, a = t.generation ?? 0, [m, h] = In(!1), _ = Qe(() => {
    h(!0), setTimeout(() => {
      r("dismiss", { generation: a }), h(!1);
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
  const t = K(), r = le(), n = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Pn(null), [d, s] = Je({ top: 0, left: 0 }), [a, m] = Je(0), h = i.filter((v) => v.type === "item" && !v.disabled);
  Me(() => {
    var f, E;
    if (!n || !c) return;
    const v = document.getElementById(c);
    if (!v) return;
    const p = v.getBoundingClientRect(), N = ((f = o.current) == null ? void 0 : f.offsetHeight) ?? 200, k = ((E = o.current) == null ? void 0 : E.offsetWidth) ?? 200;
    let O = p.bottom + 4, P = p.left;
    O + N > window.innerHeight && (O = p.top - N - 4), P + k > window.innerWidth && (P = p.right - k), s({ top: O, left: P }), m(0);
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
      v.preventDefault(), m((p) => (p + 1) % h.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((p) => (p - 1 + h.length) % h.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const p = h[a];
      p && L(p.id);
    }
  }, [_, L, h, a]);
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
      const k = h.indexOf(v) === a;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => L(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, An = ({ controlId: l }) => {
  const t = K(), r = t.header, n = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, On = () => {
  const t = K().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, $n = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, et = 50, Bn = () => {
  const l = K(), t = le(), r = oe($n), n = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, d = l.selectionMode ?? "single", s = l.selectedCount ?? 0, a = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, h = e.useMemo(
    () => n.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [n]
  ), _ = d === "multi", L = 40, b = 20, v = e.useRef(null), p = e.useRef(null), N = e.useRef(null), [k, O] = e.useState({}), P = e.useRef(null), f = e.useRef(!1), E = e.useRef(null), [g, X] = e.useState(null), [T, R] = e.useState(null);
  e.useEffect(() => {
    P.current || O({});
  }, [n]);
  const A = e.useCallback((w) => k[w.name] ?? w.width, [k]), S = e.useMemo(() => {
    const w = [];
    let x = _ && a > 0 ? L : 0;
    for (let U = 0; U < a && U < n.length; U++)
      w.push(x), x += A(n[U]);
    return w;
  }, [n, a, _, L, A]), D = c * o, V = e.useCallback((w, x, U) => {
    U.preventDefault(), U.stopPropagation(), P.current = { column: w, startX: U.clientX, startWidth: x };
    const te = (y) => {
      const I = P.current;
      if (!I) return;
      const F = Math.max(et, I.startWidth + (y.clientX - I.startX));
      O((J) => ({ ...J, [I.column]: F }));
    }, ne = (y) => {
      document.removeEventListener("mousemove", te), document.removeEventListener("mouseup", ne);
      const I = P.current;
      if (I) {
        const F = Math.max(et, I.startWidth + (y.clientX - I.startX));
        t("columnResize", { column: I.column, width: F }), P.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", te), document.addEventListener("mouseup", ne);
  }, [t]), ee = e.useCallback(() => {
    v.current && p.current && (v.current.scrollLeft = p.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const w = p.current;
      if (!w) return;
      const x = w.scrollTop, U = Math.ceil(w.clientHeight / o), te = Math.floor(x / o);
      t("scroll", { start: te, count: U });
    }, 80);
  }, [t, o]), q = e.useCallback((w, x, U) => {
    if (f.current) return;
    let te;
    !x || x === "desc" ? te = "asc" : te = "desc";
    const ne = U.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: te, mode: ne });
  }, [t]), ae = e.useCallback((w, x) => {
    E.current = w, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", w);
  }, []), B = e.useCallback((w, x) => {
    if (!E.current || E.current === w) {
      X(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), te = x.clientX < U.left + U.width / 2 ? "left" : "right";
    X({ column: w, side: te });
  }, []), j = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const x = E.current;
    if (!x || !g) {
      E.current = null, X(null);
      return;
    }
    let U = n.findIndex((ne) => ne.name === g.column);
    if (U < 0) {
      E.current = null, X(null);
      return;
    }
    const te = n.findIndex((ne) => ne.name === x);
    g.side === "right" && U++, te < U && U--, t("columnReorder", { column: x, targetIndex: U }), E.current = null, X(null);
  }, [n, g, t]), Z = e.useCallback(() => {
    E.current = null, X(null);
  }, []), u = e.useCallback((w, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), C = e.useCallback((w, x) => {
    x.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), $ = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), M = e.useCallback((w, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: w, expanded: x });
  }, [t]), W = e.useCallback((w, x) => {
    x.preventDefault(), R({ x: x.clientX, y: x.clientY, colIdx: w });
  }, []), G = e.useCallback(() => {
    T && (t("setFrozenColumnCount", { count: T.colIdx + 1 }), R(null));
  }, [T, t]), Q = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), R(null);
  }, [t]);
  e.useEffect(() => {
    if (!T) return;
    const w = () => R(null), x = (U) => {
      U.key === "Escape" && R(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", x), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", x);
    };
  }, [T]);
  const se = n.reduce((w, x) => w + A(x), 0) + (_ ? L : 0), re = s === c && c > 0, fe = s > 0 && s < c, xe = e.useCallback((w) => {
    w && (w.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!E.current) return;
        w.preventDefault();
        const x = p.current, U = v.current;
        if (!x) return;
        const te = x.getBoundingClientRect(), ne = 40, y = 8;
        w.clientX < te.left + ne ? x.scrollLeft = Math.max(0, x.scrollLeft - y) : w.clientX > te.right - ne && (x.scrollLeft += y), U && (U.scrollLeft = x.scrollLeft);
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
        onDragOver: (w) => {
          E.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== E.current && X({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: xe,
          className: "tlTableView__checkbox",
          checked: re,
          onChange: $
        }
      )
    ), n.map((w, x) => {
      const U = A(w), te = x === n.length - 1;
      let ne = "tlTableView__headerCell";
      w.sortable && (ne += " tlTableView__headerCell--sortable"), g && g.column === w.name && (ne += " tlTableView__headerCell--dragOver-" + g.side);
      const y = x < a, I = x === a - 1;
      return y && (ne += " tlTableView__headerCell--frozen"), I && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ne,
          style: {
            ...te && !y ? { flex: "1 0 auto", minWidth: U } : { width: U, minWidth: U },
            position: y ? "sticky" : "relative",
            ...y ? { left: S[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (F) => q(w.name, w.sortDirection, F) : void 0,
          onContextMenu: (F) => W(x, F),
          onDragStart: (F) => ae(w.name, F),
          onDragOver: (F) => B(w.name, F),
          onDrop: j,
          onDragEnd: Z
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", h > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (F) => V(w.name, U, F)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (E.current && n.length > 0) {
            const x = n[n.length - 1];
            x.name !== E.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", X({ column: x.name, side: "right" }));
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
      /* @__PURE__ */ e.createElement("div", { style: { height: D, position: "relative", minWidth: se } }, i.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            minWidth: se,
            width: "100%"
          },
          onClick: (x) => u(w.index, x)
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
            onClick: (x) => x.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (x) => C(w.index, x),
              tabIndex: -1
            }
          )
        ),
        n.map((x, U) => {
          const te = A(x), ne = U === n.length - 1, y = U < a, I = U === a - 1;
          let F = "tlTableView__cell";
          y && (F += " tlTableView__cell--frozen"), I && (F += " tlTableView__cell--frozenLast");
          const J = m && U === 0, ve = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: F,
              style: {
                ...ne && !y ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...y ? { position: "sticky", left: S[U], zIndex: 2 } : {}
              }
            },
            J ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ve * b } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pt) => M(w.index, !w.expanded, pt)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: w.cells[x.name] })) : /* @__PURE__ */ e.createElement(Y, { control: w.cells[x.name] })
          );
        })
      )))
    ),
    T && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: T.y, left: T.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      T.colIdx + 1 !== a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: G }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      a > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Q }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Fn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ct = e.createContext(Fn), { useMemo: Hn, useRef: zn, useState: Un, useEffect: Wn } = e, Vn = 320, Kn = ({ controlId: l }) => {
  const t = K(), r = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, d = zn(null), [s, a] = Un(
    n === "top" ? "top" : "side"
  );
  Wn(() => {
    if (n !== "auto") {
      a(n);
      return;
    }
    const b = d.current;
    if (!b) return;
    const v = new ResizeObserver((p) => {
      for (const N of p) {
        const O = N.contentRect.width / r;
        a(O < Vn ? "top" : "side");
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
  const t = K(), r = le(), n = oe(Gn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], h = c != null || i.length > 0 || o, _ = Yn(() => {
    r("toggleCollapse");
  }, [r]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    a ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, h && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
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
  const t = K(), r = qn(ct), n = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? r.resolvedLabelPosition, a = t.fullLine === !0, m = t.visible !== !1, h = t.field, _ = r.readOnly, [L, b] = Zn(!1), v = Qn(() => b((k) => !k), []);
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: h })), !_ && p && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  const l = K(), t = le(), r = l.iconCss, n = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, d = l.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, a = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), h = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: h, href: "#", onClick: m, title: o }, a) : /* @__PURE__ */ e.createElement("span", { className: h, title: o }, a);
}, tl = 20, nl = () => {
  const l = K(), t = le(), r = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [s, a] = e.useState(-1), m = e.useRef(null), h = e.useCallback((f, E) => {
    t(E ? "collapse" : "expand", { nodeId: f });
  }, [t]), _ = e.useCallback((f, E) => {
    t("select", {
      nodeId: f,
      ctrlKey: E.ctrlKey || E.metaKey,
      shiftKey: E.shiftKey
    });
  }, [t]), L = e.useCallback((f, E) => {
    E.preventDefault(), t("contextMenu", { nodeId: f, x: E.clientX, y: E.clientY });
  }, [t]), b = e.useRef(null), v = e.useCallback((f, E) => {
    const g = E.getBoundingClientRect(), X = f.clientY - g.top, T = g.height / 3;
    return X < T ? "above" : X > T * 2 ? "below" : "within";
  }, []), p = e.useCallback((f, E) => {
    E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", f);
  }, []), N = e.useCallback((f, E) => {
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const g = v(E, E.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: f, position: g }), b.current = null;
    }, 50);
  }, [t, v]), k = e.useCallback((f, E) => {
    E.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const g = v(E, E.currentTarget);
    t("drop", { nodeId: f, position: g });
  }, [t, v]), O = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((f) => {
    if (r.length === 0) return;
    let E = s;
    switch (f.key) {
      case "ArrowDown":
        f.preventDefault(), E = Math.min(s + 1, r.length - 1);
        break;
      case "ArrowUp":
        f.preventDefault(), E = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (f.preventDefault(), s >= 0 && s < r.length) {
          const g = r[s];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (E = s + 1);
        }
        break;
      case "ArrowLeft":
        if (f.preventDefault(), s >= 0 && s < r.length) {
          const g = r[s];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const X = g.depth;
            for (let T = s - 1; T >= 0; T--)
              if (r[T].depth < X) {
                E = T;
                break;
              }
          }
        }
        break;
      case "Enter":
        f.preventDefault(), s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: f.ctrlKey || f.metaKey,
          shiftKey: f.shiftKey
        });
        return;
      case " ":
        f.preventDefault(), n === "multi" && s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        f.preventDefault(), E = 0;
        break;
      case "End":
        f.preventDefault(), E = r.length - 1;
        break;
      default:
        return;
    }
    E !== s && a(E);
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
    r.map((f, E) => /* @__PURE__ */ e.createElement(
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
          E === s ? "tlTreeView__node--focused" : "",
          o === f.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === f.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === f.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: f.depth * tl },
        draggable: c,
        onClick: (g) => _(f.id, g),
        onContextMenu: (g) => L(f.id, g),
        onDragStart: (g) => p(f.id, g),
        onDragOver: i ? (g) => N(f.id, g) : void 0,
        onDrop: i ? (g) => k(f.id, g) : void 0,
        onDragEnd: O
      },
      f.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), h(f.id, f.expanded);
          },
          tabIndex: -1,
          "aria-label": f.expanded ? "Collapse" : "Expand"
        },
        f.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: f.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: f.content }))
    ))
  );
};
var Ae = { exports: {} }, ce = {}, Oe = { exports: {} }, z = {};
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
  if (tt) return z;
  tt = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), h = Symbol.for("react.activity"), _ = Symbol.iterator;
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
  function N(u, C, $) {
    this.props = u, this.context = C, this.refs = p, this.updater = $ || b;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(u, C) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, C, "setState");
  }, N.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function k() {
  }
  k.prototype = N.prototype;
  function O(u, C, $) {
    this.props = u, this.context = C, this.refs = p, this.updater = $ || b;
  }
  var P = O.prototype = new k();
  P.constructor = O, v(P, N.prototype), P.isPureReactComponent = !0;
  var f = Array.isArray;
  function E() {
  }
  var g = { H: null, A: null, T: null, S: null }, X = Object.prototype.hasOwnProperty;
  function T(u, C, $) {
    var M = $.ref;
    return {
      $$typeof: l,
      type: u,
      key: C,
      ref: M !== void 0 ? M : null,
      props: $
    };
  }
  function R(u, C) {
    return T(u.type, C, u.props);
  }
  function A(u) {
    return typeof u == "object" && u !== null && u.$$typeof === l;
  }
  function S(u) {
    var C = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function($) {
      return C[$];
    });
  }
  var D = /\/+/g;
  function V(u, C) {
    return typeof u == "object" && u !== null && u.key != null ? S("" + u.key) : C.toString(36);
  }
  function ee(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(E, E) : (u.status = "pending", u.then(
          function(C) {
            u.status === "pending" && (u.status = "fulfilled", u.value = C);
          },
          function(C) {
            u.status === "pending" && (u.status = "rejected", u.reason = C);
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
  function q(u, C, $, M, W) {
    var G = typeof u;
    (G === "undefined" || G === "boolean") && (u = null);
    var Q = !1;
    if (u === null) Q = !0;
    else
      switch (G) {
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
              return Q = u._init, q(
                Q(u._payload),
                C,
                $,
                M,
                W
              );
          }
      }
    if (Q)
      return W = W(u), Q = M === "" ? "." + V(u, 0) : M, f(W) ? ($ = "", Q != null && ($ = Q.replace(D, "$&/") + "/"), q(W, C, $, "", function(fe) {
        return fe;
      })) : W != null && (A(W) && (W = R(
        W,
        $ + (W.key == null || u && u.key === W.key ? "" : ("" + W.key).replace(
          D,
          "$&/"
        ) + "/") + Q
      )), C.push(W)), 1;
    Q = 0;
    var se = M === "" ? "." : M + ":";
    if (f(u))
      for (var re = 0; re < u.length; re++)
        M = u[re], G = se + V(M, re), Q += q(
          M,
          C,
          $,
          G,
          W
        );
    else if (re = L(u), typeof re == "function")
      for (u = re.call(u), re = 0; !(M = u.next()).done; )
        M = M.value, G = se + V(M, re++), Q += q(
          M,
          C,
          $,
          G,
          W
        );
    else if (G === "object") {
      if (typeof u.then == "function")
        return q(
          ee(u),
          C,
          $,
          M,
          W
        );
      throw C = String(u), Error(
        "Objects are not valid as a React child (found: " + (C === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : C) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function ae(u, C, $) {
    if (u == null) return u;
    var M = [], W = 0;
    return q(u, M, "", "", function(G) {
      return C.call($, G, W++);
    }), M;
  }
  function B(u) {
    if (u._status === -1) {
      var C = u._result;
      C = C(), C.then(
        function($) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = $);
        },
        function($) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = $);
        }
      ), u._status === -1 && (u._status = 0, u._result = C);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var j = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var C = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(C)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, Z = {
    map: ae,
    forEach: function(u, C, $) {
      ae(
        u,
        function() {
          C.apply(this, arguments);
        },
        $
      );
    },
    count: function(u) {
      var C = 0;
      return ae(u, function() {
        C++;
      }), C;
    },
    toArray: function(u) {
      return ae(u, function(C) {
        return C;
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
  return z.Activity = h, z.Children = Z, z.Component = N, z.Fragment = r, z.Profiler = c, z.PureComponent = O, z.StrictMode = n, z.Suspense = s, z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return g.H.useMemoCache(u);
    }
  }, z.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, z.cacheSignal = function() {
    return null;
  }, z.cloneElement = function(u, C, $) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var M = v({}, u.props), W = u.key;
    if (C != null)
      for (G in C.key !== void 0 && (W = "" + C.key), C)
        !X.call(C, G) || G === "key" || G === "__self" || G === "__source" || G === "ref" && C.ref === void 0 || (M[G] = C[G]);
    var G = arguments.length - 2;
    if (G === 1) M.children = $;
    else if (1 < G) {
      for (var Q = Array(G), se = 0; se < G; se++)
        Q[se] = arguments[se + 2];
      M.children = Q;
    }
    return T(u.type, W, M);
  }, z.createContext = function(u) {
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
  }, z.createElement = function(u, C, $) {
    var M, W = {}, G = null;
    if (C != null)
      for (M in C.key !== void 0 && (G = "" + C.key), C)
        X.call(C, M) && M !== "key" && M !== "__self" && M !== "__source" && (W[M] = C[M]);
    var Q = arguments.length - 2;
    if (Q === 1) W.children = $;
    else if (1 < Q) {
      for (var se = Array(Q), re = 0; re < Q; re++)
        se[re] = arguments[re + 2];
      W.children = se;
    }
    if (u && u.defaultProps)
      for (M in Q = u.defaultProps, Q)
        W[M] === void 0 && (W[M] = Q[M]);
    return T(u, G, W);
  }, z.createRef = function() {
    return { current: null };
  }, z.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, z.isValidElement = A, z.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: B
    };
  }, z.memo = function(u, C) {
    return {
      $$typeof: a,
      type: u,
      compare: C === void 0 ? null : C
    };
  }, z.startTransition = function(u) {
    var C = g.T, $ = {};
    g.T = $;
    try {
      var M = u(), W = g.S;
      W !== null && W($, M), typeof M == "object" && M !== null && typeof M.then == "function" && M.then(E, j);
    } catch (G) {
      j(G);
    } finally {
      C !== null && $.types !== null && (C.types = $.types), g.T = C;
    }
  }, z.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, z.use = function(u) {
    return g.H.use(u);
  }, z.useActionState = function(u, C, $) {
    return g.H.useActionState(u, C, $);
  }, z.useCallback = function(u, C) {
    return g.H.useCallback(u, C);
  }, z.useContext = function(u) {
    return g.H.useContext(u);
  }, z.useDebugValue = function() {
  }, z.useDeferredValue = function(u, C) {
    return g.H.useDeferredValue(u, C);
  }, z.useEffect = function(u, C) {
    return g.H.useEffect(u, C);
  }, z.useEffectEvent = function(u) {
    return g.H.useEffectEvent(u);
  }, z.useId = function() {
    return g.H.useId();
  }, z.useImperativeHandle = function(u, C, $) {
    return g.H.useImperativeHandle(u, C, $);
  }, z.useInsertionEffect = function(u, C) {
    return g.H.useInsertionEffect(u, C);
  }, z.useLayoutEffect = function(u, C) {
    return g.H.useLayoutEffect(u, C);
  }, z.useMemo = function(u, C) {
    return g.H.useMemo(u, C);
  }, z.useOptimistic = function(u, C) {
    return g.H.useOptimistic(u, C);
  }, z.useReducer = function(u, C, $) {
    return g.H.useReducer(u, C, $);
  }, z.useRef = function(u) {
    return g.H.useRef(u);
  }, z.useState = function(u) {
    return g.H.useState(u);
  }, z.useSyncExternalStore = function(u, C, $) {
    return g.H.useSyncExternalStore(
      u,
      C,
      $
    );
  }, z.useTransition = function() {
    return g.H.useTransition();
  }, z.version = "19.2.4", z;
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
    var h = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: h == null ? null : "" + h,
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
      var m = a.as, h = d(m, a.crossOrigin), _ = typeof a.integrity == "string" ? a.integrity : void 0, L = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? n.d.S(
        s,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: h,
          integrity: _,
          fetchPriority: L
        }
      ) : m === "script" && n.d.X(s, {
        crossOrigin: h,
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
      var m = a.as, h = d(m, a.crossOrigin);
      n.d.L(s, m, {
        crossOrigin: h,
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
    (h) => {
      h.stopPropagation(), r(l.value);
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
  const r = le(), n = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], h = t.emptyOptionLabel ?? "", _ = i && c && !d && s, L = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = L["js.dropdownSelect.nothingFound"], v = ie(
    (y) => L["js.dropdownSelect.removeChip"].replace("{0}", y),
    [L]
  ), [p, N] = me(!1), [k, O] = me(""), [P, f] = me(-1), [E, g] = me(!1), [X, T] = me({}), [R, A] = me(null), [S, D] = me(null), [V, ee] = me(null), q = Ee(null), ae = Ee(null), B = Ee(null), j = Ee(n);
  j.current = n;
  const Z = Ee(-1), u = ze(
    () => new Set(n.map((y) => y.value)),
    [n]
  ), C = ze(() => {
    let y = m.filter((I) => !u.has(I.value));
    if (k) {
      const I = k.toLowerCase();
      y = y.filter((F) => F.label.toLowerCase().includes(I));
    }
    return y;
  }, [m, u, k]);
  he(() => {
    k && C.length === 1 ? f(0) : f(-1);
  }, [C.length, k]), he(() => {
    p && a && ae.current && ae.current.focus();
  }, [p, a, n]), he(() => {
    var F, J;
    if (Z.current < 0) return;
    const y = Z.current;
    Z.current = -1;
    const I = (F = q.current) == null ? void 0 : F.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    I && I.length > 0 ? I[Math.min(y, I.length - 1)].focus() : (J = q.current) == null || J.focus();
  }, [n]), he(() => {
    if (!p) return;
    const y = (I) => {
      q.current && !q.current.contains(I.target) && B.current && !B.current.contains(I.target) && (N(!1), O(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [p]), he(() => {
    if (!p || !q.current) return;
    const y = q.current.getBoundingClientRect(), I = window.innerHeight - y.bottom, J = I < 300 && y.top > I;
    T({
      left: y.left,
      width: y.width,
      ...J ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [p]);
  const $ = ie(async () => {
    if (!(d || !s) && (N(!0), O(""), f(-1), g(!1), !a))
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
  }, [d, s, a, r]), M = ie(() => {
    var y;
    N(!1), O(""), f(-1), (y = q.current) == null || y.focus();
  }, []), W = ie(
    (y) => {
      let I;
      if (c) {
        const F = m.find((J) => J.value === y);
        if (F)
          I = [...j.current, F];
        else
          return;
      } else {
        const F = m.find((J) => J.value === y);
        if (F)
          I = [F];
        else
          return;
      }
      j.current = I, r("valueChanged", { value: I.map((F) => F.value) }), c ? (O(""), f(-1)) : M();
    },
    [c, m, r, M]
  ), G = ie(
    (y) => {
      Z.current = j.current.findIndex((F) => F.value === y);
      const I = j.current.filter((F) => F.value !== y);
      j.current = I, r("valueChanged", { value: I.map((F) => F.value) });
    },
    [r]
  ), Q = ie(
    (y) => {
      y.stopPropagation(), r("valueChanged", { value: [] }), M();
    },
    [r, M]
  ), se = ie((y) => {
    O(y.target.value);
  }, []), re = ie(
    (y) => {
      if (!p) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), $();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), f(
            (I) => I < C.length - 1 ? I + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), f(
            (I) => I > 0 ? I - 1 : C.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), P >= 0 && P < C.length && W(C[P].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), M();
          break;
        case "Tab":
          M();
          break;
        case "Backspace":
          k === "" && c && n.length > 0 && G(n[n.length - 1].value);
          break;
      }
    },
    [
      p,
      $,
      M,
      C,
      P,
      W,
      k,
      c,
      n,
      G
    ]
  ), fe = ie(
    async (y) => {
      y.preventDefault(), g(!1);
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
    },
    [r]
  ), xe = ie(
    (y, I) => {
      A(y), I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), w = ie(
    (y, I) => {
      if (I.preventDefault(), I.dataTransfer.dropEffect = "move", R === null || R === y) {
        D(null), ee(null);
        return;
      }
      const F = I.currentTarget.getBoundingClientRect(), J = F.left + F.width / 2, ve = I.clientX < J ? "before" : "after";
      D(y), ee(ve);
    },
    [R]
  ), x = ie(
    (y) => {
      if (y.preventDefault(), R === null || S === null || V === null || R === S) return;
      const I = [...j.current], [F] = I.splice(R, 1);
      let J = S;
      R < S ? J = V === "before" ? J - 1 : J : J = V === "before" ? J : J + 1, I.splice(J, 0, F), j.current = I, r("valueChanged", { value: I.map((ve) => ve.value) }), A(null), D(null), ee(null);
    },
    [R, S, V, r]
  ), U = ie(() => {
    A(null), D(null), ee(null);
  }, []);
  if (he(() => {
    if (P < 0 || !B.current) return;
    const y = B.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, h) : n.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ke, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const te = !o && n.length > 0 && !d, ne = p ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: X
    },
    (a || E) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: ae,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
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
      !a && !E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: fe }, L["js.dropdownSelect.error"])),
      a && C.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      a && C.map((y, I) => /* @__PURE__ */ e.createElement(
        il,
        {
          key: y.value,
          id: `${l}-opt-${I}`,
          option: y,
          highlighted: I === P,
          searchTerm: k,
          onSelect: W,
          onMouseEnter: () => f(I)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: q,
      className: "tlDropdownSelect" + (p ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": p,
      "aria-haspopup": "listbox",
      "aria-owns": p ? `${l}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: p ? void 0 : $,
      onKeyDown: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, h) : n.map((y, I) => {
      let F = "";
      return R === I ? F = "tlDropdownSelect__chip--dragging" : S === I && V === "before" ? F = "tlDropdownSelect__chip--dropBefore" : S === I && V === "after" && (F = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        cl,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: G,
          removeLabel: v(y.label),
          draggable: _,
          onDragStart: _ ? (J) => xe(I, J) : void 0,
          onDragOver: _ ? (J) => w(I, J) : void 0,
          onDrop: _ ? x : void 0,
          onDragEnd: _ ? U : void 0,
          dragClassName: _ ? F : void 0
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
    (m) => (h) => {
      o.current = m, h.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = $e((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = $e(
    (m) => (h) => {
      h.preventDefault();
      const _ = h.dataTransfer.getData(it);
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
    l.map((m, h) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: h,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? d(h) : void 0,
        onDragOver: s,
        onDrop: a(h)
      }
    ))
  );
};
function ut(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Ue(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function dt(l) {
  if (!Ue(l)) return [0, 0, 0];
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
      var O;
      const p = (O = i.current) == null ? void 0 : O.getBoundingClientRect();
      if (!p) return;
      const N = Math.max(0, Math.min(1, (b - p.left) / p.width)), k = Math.max(0, Math.min(1, 1 - (v - p.top) / p.height));
      t(Be(r, N, k));
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
      var k;
      const v = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!v) return;
      const N = Math.max(0, Math.min(1, (b - v.top) / v.height)) * 360;
      t(Be(N, n, c));
    },
    [n, c, t]
  ), h = _e(
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
      onPointerDown: h,
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
  const [a, m] = we("palette"), [h, _] = we(t), L = El(null), b = oe(vl), [v, p] = we(null);
  gl(() => {
    if (!l.current || !L.current) return;
    const B = l.current.getBoundingClientRect(), j = L.current.getBoundingClientRect();
    let Z = B.bottom + 4, u = B.left;
    Z + j.height > window.innerHeight && (Z = B.top - j.height - 4), u + j.width > window.innerWidth && (u = Math.max(0, B.right - j.width)), p({ top: Z, left: u });
  }, [l]);
  const N = h != null, [k, O, P] = N ? dt(h) : [0, 0, 0], [f, E] = we((h == null ? void 0 : h.toUpperCase()) ?? "");
  Fe(() => {
    E((h == null ? void 0 : h.toUpperCase()) ?? "");
  }, [h]), Fe(() => {
    const B = (j) => {
      j.key === "Escape" && d();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [d]), Fe(() => {
    const B = (Z) => {
      L.current && !L.current.contains(Z.target) && d();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [d]);
  const g = de(
    (B) => (j) => {
      const Z = parseInt(j.target.value, 10);
      if (isNaN(Z)) return;
      const u = ut(Z);
      _(mt(B === "r" ? u : k, B === "g" ? u : O, B === "b" ? u : P));
    },
    [k, O, P]
  ), X = de(
    (B) => {
      if (h != null) {
        B.dataTransfer.setData(it, h.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = h, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [h]
  ), T = de((B) => {
    const j = B.target.value;
    E(j), Ue(j) && _(j);
  }, []), R = de(() => {
    _(null);
  }, []), A = de((B) => {
    _(B);
  }, []), S = de(
    (B) => {
      o(B);
    },
    [o]
  ), D = de(
    (B, j) => {
      const Z = [...r], u = Z[B];
      Z[B] = Z[j], Z[j] = u, s(Z);
    },
    [r, s]
  ), V = de(
    (B, j) => {
      const Z = [...r];
      Z[B] = j, s(Z);
    },
    [r, s]
  ), ee = de(() => {
    s([...c]);
  }, [c, s]), q = de(
    (B) => {
      if (bl(r, B)) return;
      const j = r.indexOf(null);
      if (j < 0) return;
      const Z = [...r];
      Z[j] = B.toUpperCase(), s(Z);
    },
    [r, s]
  ), ae = de(() => {
    h != null && q(h), o(h);
  }, [h, o, q]);
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
        onConfirm: S,
        onSwap: D,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(_l, { color: h ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: h } : void 0,
        draggable: N,
        onDragStart: N ? X : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? k : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? O : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? P : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (f !== "" && !Ue(f) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: f,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: ae }, b["js.colorInput.ok"]))
  );
}, yl = { "js.colorInput.chooseColor": "Choose color" }, { useState: wl, useCallback: ke, useRef: kl } = e, Sl = ({ controlId: l, state: t }) => {
  const r = le(), n = oe(yl), [c, i] = wl(!1), o = kl(null), d = t.value, s = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, h = t.defaultPalette ?? a, _ = ke(() => {
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
      defaultPalette: h,
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
  const d = oe(Rl), [s, a] = ge("simple"), [m, h] = ge(""), [_, L] = ge(t ?? ""), [b, v] = ge(!1), [p, N] = ge(null), k = ot(null), O = ot(null);
  Nl(() => {
    if (!l.current || !k.current) return;
    const S = l.current.getBoundingClientRect(), D = k.current.getBoundingClientRect();
    let V = S.bottom + 4, ee = S.left;
    V + D.height > window.innerHeight && (V = S.top - D.height - 4), ee + D.width > window.innerWidth && (ee = Math.max(0, S.right - D.width)), N({ top: V, left: ee });
  }, [l]), Se(() => {
    !n && !b && o().catch(() => v(!0));
  }, [n, b, o]), Se(() => {
    n && O.current && O.current.focus();
  }, [n]), Se(() => {
    const S = (D) => {
      D.key === "Escape" && i();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [i]), Se(() => {
    const S = (V) => {
      k.current && !k.current.contains(V.target) && i();
    }, D = setTimeout(() => document.addEventListener("mousedown", S), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", S);
    };
  }, [i]);
  const P = Tl(() => {
    if (!m) return r;
    const S = m.toLowerCase();
    return r.filter(
      (D) => D.prefix.toLowerCase().includes(S) || D.label.toLowerCase().includes(S) || D.terms != null && D.terms.some((V) => V.includes(S))
    );
  }, [r, m]), f = pe((S) => {
    h(S.target.value);
  }, []), E = pe(
    (S) => {
      c(S);
    },
    [c]
  ), g = pe((S) => {
    L(S);
  }, []), X = pe((S) => {
    L(S.target.value);
  }, []), T = pe(() => {
    c(_ || null);
  }, [_, c]), R = pe(() => {
    c(null);
  }, [c]), A = pe(async (S) => {
    S.preventDefault(), v(!1);
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
      ref: k,
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
        ref: O,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: f,
        placeholder: d["js.iconSelect.filterPlaceholder"],
        "aria-label": d["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => h(""),
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
        (S) => S.variants.map((D) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.encoded,
            className: "tlIconSelect__iconCell" + (D.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": D.encoded === t,
            tabIndex: 0,
            title: S.label,
            onClick: () => s === "simple" ? E(D.encoded) : g(D.encoded),
            onKeyDown: (V) => {
              (V.key === "Enter" || V.key === " ") && (V.preventDefault(), s === "simple" ? E(D.encoded) : g(D.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(De, { encoded: D.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: X
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(De, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, d["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, d["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, d["js.iconSelect.ok"]))
  );
}, Dl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: xl, useCallback: Ne, useRef: Il } = e, jl = ({ controlId: l, state: t }) => {
  const r = le(), n = oe(Dl), [c, i] = xl(!1), o = Il(null), d = t.value, s = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], h = t.iconsLoaded === !0, _ = Ne(() => {
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
      iconsLoaded: h,
      onSelect: L,
      onCancel: b,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, d ? /* @__PURE__ */ e.createElement(De, { encoded: d }) : null));
};
H("TLButton", Tt);
H("TLToggleButton", Lt);
H("TLTextInput", _t);
H("TLNumberInput", vt);
H("TLDatePicker", gt);
H("TLSelect", yt);
H("TLCheckbox", kt);
H("TLTable", St);
H("TLCounter", Dt);
H("TLTabBar", It);
H("TLFieldList", jt);
H("TLAudioRecorder", Mt);
H("TLAudioPlayer", Ot);
H("TLFileUpload", Bt);
H("TLDownload", Ht);
H("TLPhotoCapture", Ut);
H("TLPhotoViewer", Vt);
H("TLSplitPanel", Kt);
H("TLPanel", Jt);
H("TLMaximizeRoot", en);
H("TLDeckPane", tn);
H("TLSidebar", un);
H("TLStack", dn);
H("TLGrid", mn);
H("TLCard", pn);
H("TLAppBar", fn);
H("TLBreadcrumb", _n);
H("TLBottomBar", vn);
H("TLDialog", gn);
H("TLDialogManager", wn);
H("TLWindow", Nn);
H("TLDrawer", Dn);
H("TLSnackbar", jn);
H("TLMenu", Mn);
H("TLAppShell", An);
H("TLTextCell", On);
H("TLTableView", Bn);
H("TLFormLayout", Kn);
H("TLFormGroup", Xn);
H("TLFormField", Jn);
H("TLResourceCell", el);
H("TLTreeView", nl);
H("TLDropdownSelect", ul);
H("TLColorInput", Sl);
H("TLIconSelect", jl);
