import { React as e, useTLFieldValue as Ce, getComponent as dt, useTLState as K, useTLCommand as le, TLChild as G, useTLUpload as Ue, useI18N as re, useTLDataUrl as Ve, register as H } from "tl-react-bridge";
const { useCallback: mt } = e, pt = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = mt(
    (i) => {
      l(i.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  ));
}, { useCallback: ft } = e, ht = ({ controlId: n, state: t, config: r }) => {
  const [l, c] = Ce(), i = ft(
    (d) => {
      const s = d.target.value, a = s === "" ? null : Number(s);
      c(a);
    },
    [c]
  ), o = r != null && r.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: i,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  ));
}, { useCallback: _t } = e, bt = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = _t(
    (i) => {
      l(i.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  ));
}, { useCallback: vt } = e, Et = ({ controlId: n, state: t, config: r }) => {
  var d;
  const [l, c] = Ce(), i = vt(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), o = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const s = ((d = o.find((a) => a.value === l)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  ));
}, { useCallback: gt } = e, Ct = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = gt(
    (i) => {
      l(i.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: n,
      checked: r === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: n,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, yt = ({ controlId: n, state: t }) => {
  const r = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: n, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, r.map((o) => {
    const d = o.cellModule ? dt(o.cellModule) : void 0, s = c[o.name];
    if (d) {
      const a = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: n + "-" + i + "-" + o.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: wt } = e, kt = ({ controlId: n, command: t, label: r, disabled: l }) => {
  const c = K(), i = le(), o = t ?? "click", d = r ?? c.label, s = l ?? c.disabled === !0, a = wt(() => {
    i(o);
  }, [i, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: a,
      disabled: s,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: St } = e, Nt = ({ controlId: n, command: t, label: r, active: l, disabled: c }) => {
  const i = K(), o = le(), d = t ?? "click", s = r ?? i.label, a = l ?? i.active === !0, m = c ?? i.disabled === !0, p = St(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    s
  );
}, Tt = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Rt } = e, Lt = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.tabs ?? [], c = t.activeTabId, i = Rt((o) => {
    o !== c && r("selectTab", { tabId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, Dt = ({ controlId: n }) => {
  const t = K(), r = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: c })))));
}, xt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, It = ({ controlId: n }) => {
  const t = K(), r = Ue(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, b = m === "received" ? "idle" : l !== "idle" ? l : m, N = e.useCallback(async () => {
    if (l === "recording") {
      const k = d.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (l !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = k, s.current = [];
        const $ = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, $ ? { mimeType: $ } : void 0);
        d.current = P, P.ondataavailable = (h) => {
          h.data.size > 0 && s.current.push(h.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((C) => C.stop()), a.current = null;
          const h = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], h.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", h, "recording.webm"), await r(g), c("idle");
        }, P.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, r]), _ = re(xt), v = b === "recording" ? _["js.audioRecorder.stop"] : b === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], f = b === "uploading", T = ["tlAudioRecorder__button"];
  return b === "recording" && T.push("tlAudioRecorder__button--recording"), b === "uploading" && T.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: N,
      disabled: f,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Pt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, jt = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), a = e.useRef(c);
  e.useEffect(() => {
    l ? i === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [l]), e.useEffect(() => {
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
        const f = await fetch(r);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const T = await f.blob();
        s.current = URL.createObjectURL(T);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const v = new Audio(s.current);
    d.current = v, v.onended = () => {
      o("idle");
    }, v.play(), o("playing");
  }, [i, r]), p = re(Pt), b = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = i === "disabled" || i === "loading", _ = ["tlAudioPlayer__button"];
  return i === "playing" && _.push("tlAudioPlayer__button--playing"), i === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: m,
      disabled: N,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Mt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, At = ({ controlId: n }) => {
  const t = K(), r = Ue(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, b = e.useCallback(async (h) => {
    c("uploading");
    const g = new FormData();
    g.append("file", h, h.name), await r(g), c("idle");
  }, [r]), N = e.useCallback((h) => {
    var C;
    const g = (C = h.target.files) == null ? void 0 : C[0];
    g && b(g);
  }, [b]), _ = e.useCallback(() => {
    var h;
    l !== "uploading" && ((h = d.current) == null || h.click());
  }, [l]), v = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!0);
  }, []), f = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!1);
  }, []), T = e.useCallback((h) => {
    var C;
    if (h.preventDefault(), h.stopPropagation(), o(!1), l === "uploading") return;
    const g = (C = h.dataTransfer.files) == null ? void 0 : C[0];
    g && b(g);
  }, [l, b]), k = p === "uploading", $ = re(Mt), P = p === "uploading" ? $["js.uploading"] : $["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: f,
      onDrop: T
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: m || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: k,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, Ot = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, $t = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      a(!0);
      try {
        const _ = r + (r.includes("?") ? "&" : "?") + "rev=" + i, v = await fetch(_);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const f = await v.blob(), T = URL.createObjectURL(f), k = document.createElement("a");
        k.href = T, k.download = o, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(T);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        a(!1);
      }
    }
  }, [c, s, r, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), b = re(Ot);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const N = s ? b["js.downloading"] : b["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Bt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ft = ({ controlId: n }) => {
  const t = K(), r = Ue(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), b = e.useRef(null), N = e.useRef(null), _ = t.error, v = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), T = e.useCallback(() => {
    f(), c("idle");
  }, [f]), k = e.useCallback(async () => {
    var L;
    if (l !== "uploading") {
      if (o(null), !v) {
        (L = b.current) == null || L.click();
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
  }, [l, v]), $ = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const L = a.current, A = p.current;
    if (!L || !A)
      return;
    A.width = L.videoWidth, A.height = L.videoHeight;
    const R = A.getContext("2d");
    R && (R.drawImage(L, 0, 0), f(), c("uploading"), A.toBlob(async (j) => {
      if (!j) {
        c("idle");
        return;
      }
      const Y = new FormData();
      Y.append("photo", j, "capture.jpg"), await r(Y), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, r, f]), P = e.useCallback(async (L) => {
    var j;
    const A = (j = L.target.files) == null ? void 0 : j[0];
    if (!A) return;
    c("uploading");
    const R = new FormData();
    R.append("photo", A, A.name), await r(R), c("idle"), b.current && (b.current.value = "");
  }, [r]);
  e.useEffect(() => {
    l === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var A;
    if (l !== "overlayOpen") return;
    (A = N.current) == null || A.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const L = (A) => {
      A.key === "Escape" && T();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [l, T]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const h = re(Bt), g = l === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const q = ["tlPhotoCapture__overlayVideo"];
  d && q.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return d && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: k,
      disabled: l === "uploading",
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: T }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: q.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
        onClick: () => s((L) => !L),
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
        onClick: T,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[i]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, zt = {
  "js.photoViewer.alt": "Captured photo"
}, Ht = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), d = e.useRef(c);
  e.useEffect(() => {
    if (!l) {
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
        const p = await m.blob();
        a || o(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [l, c, r]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = re(zt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ke, useRef: xe } = e, Ut = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", d = i.length > 0 && i.every((f) => f.collapsed), s = !d && i.some((f) => f.collapsed), a = d ? !o : o, m = xe(null), p = xe(null), b = xe(null), N = Ke((f, T) => {
    const k = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? d && !a ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : T !== void 0 ? k.flex = `0 0 ${T}px` : f.unit === "%" || s ? k.flex = `${f.size} 0 0%` : k.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (k.minWidth = o ? f.minSize : void 0, k.minHeight = o ? void 0 : f.minSize), k;
  }, [o, d, s, a]), _ = Ke((f, T) => {
    f.preventDefault();
    const k = m.current;
    if (!k) return;
    const $ = i[T], P = i[T + 1], h = k.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    h.forEach((S) => {
      g.push(o ? S.offsetWidth : S.offsetHeight);
    }), b.current = g, p.current = {
      splitterIndex: T,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: g[T],
      startSizeAfter: g[T + 1],
      childBefore: $,
      childAfter: P
    };
    const C = (S) => {
      const L = p.current;
      if (!L || !b.current) return;
      const R = (o ? S.clientX : S.clientY) - L.startPos, j = L.childBefore.minSize || 0, Y = L.childAfter.minSize || 0;
      let ne = L.startSizeBefore + R, Q = L.startSizeAfter - R;
      ne < j && (Q += ne - j, ne = j), Q < Y && (ne += Q - Y, Q = Y), b.current[L.splitterIndex] = ne, b.current[L.splitterIndex + 1] = Q;
      const ce = k.querySelectorAll(":scope > .tlSplitPanel__child"), F = ce[L.splitterIndex], M = ce[L.splitterIndex + 1];
      F && (F.style.flex = `0 0 ${ne}px`), M && (M.style.flex = `0 0 ${Q}px`);
    }, q = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", q), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const S = {};
        i.forEach((L, A) => {
          const R = L.control;
          R != null && R.controlId && b.current && (S[R.controlId] = b.current[A]);
        }), r("updateSizes", { sizes: S });
      }
      b.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", q), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, r]), v = [];
  return i.forEach((f, T) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${T}`,
          className: `tlSplitPanel__child${f.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(f)
        },
        /* @__PURE__ */ e.createElement(G, { control: f.control })
      )
    ), c && T < i.length - 1) {
      const k = i[T + 1];
      !f.collapsed && !k.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${T}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (P) => _(P, T)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: n,
      className: `tlSplitPanel tlSplitPanel--${l}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    v
  );
}, { useCallback: Ie } = e, Vt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Wt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Kt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Yt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Gt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Xt = ({ controlId: n }) => {
  const t = K(), r = le(), l = re(Vt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, a = t.toolbarButtons ?? [], m = i === "MINIMIZED", p = i === "MAXIMIZED", b = i === "HIDDEN", N = Ie(() => {
    r("toggleMinimize");
  }, [r]), _ = Ie(() => {
    r("toggleMaximize");
  }, [r]), v = Ie(() => {
    r("popOut");
  }, [r]);
  if (b)
    return null;
  const f = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, a.map((T, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(G, { control: T }))), o && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: m ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Kt, null) : /* @__PURE__ */ e.createElement(Wt, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: p ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Gt, null) : /* @__PURE__ */ e.createElement(Yt, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(qt, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(G, { control: t.child }))
  );
}, Zt = ({ controlId: n }) => {
  const t = K();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(G, { control: t.child })
  );
}, Qt = ({ controlId: n }) => {
  const t = K();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild }));
}, { useCallback: ue, useState: Ne, useEffect: Te, useRef: Re } = e, Jt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Fe(n, t, r, l) {
  const c = [];
  for (const i of n)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: l }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: l }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (r.get(i.id) ?? i.expanded) && !t && c.push(...Fe(i.children, t, r, i.id)));
  return c;
}
const be = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, en = ({ item: n, active: t, collapsed: r, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: r ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  r && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(be, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
  !r && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
), tn = ({ item: n, collapsed: t, onExecute: r, tabIndex: l, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(n.id),
    title: t ? n.label : void 0,
    tabIndex: l,
    ref: c,
    onFocus: () => i(n.id)
  },
  /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), nn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(be, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), ln = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), an = ({ item: n, activeItemId: t, anchorRect: r, onSelect: l, onExecute: c, onClose: i }) => {
  const o = Re(null);
  Te(() => {
    const a = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [i]), Te(() => {
    const a = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [i]);
  const d = ue((a) => {
    a.type === "nav" ? (l(a.id), i()) : a.type === "command" && (c(a.id), i());
  }, [l, c, i]), s = {};
  return r && (s.left = r.right, s.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, n.label), n.children.map((a) => {
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
}, rn = ({
  item: n,
  expanded: t,
  activeItemId: r,
  collapsed: l,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: d,
  itemRef: s,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: b,
  flyoutGroupId: N,
  onOpenFlyout: _,
  onCloseFlyout: v
}) => {
  const f = Re(null), [T, k] = Ne(null), $ = ue(() => {
    l ? N === n.id ? v() : (f.current && k(f.current.getBoundingClientRect()), _(n.id)) : o(n.id);
  }, [l, N, n.id, o, _, v]), P = ue((g) => {
    f.current = g, s(g);
  }, [s]), h = l && N === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: $,
      title: l ? n.label : void 0,
      "aria-expanded": l ? h : t,
      tabIndex: d,
      ref: P,
      onFocus: () => a(n.id)
    },
    /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
    !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
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
  ), h && /* @__PURE__ */ e.createElement(
    an,
    {
      item: n,
      activeItemId: r,
      anchorRect: T,
      onSelect: c,
      onExecute: i,
      onClose: v
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, n.children.map((g) => /* @__PURE__ */ e.createElement(
    at,
    {
      key: g.id,
      item: g,
      activeItemId: r,
      collapsed: l,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: _,
      onCloseFlyout: v
    }
  ))));
}, at = ({
  item: n,
  activeItemId: t,
  collapsed: r,
  onSelect: l,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: d,
  onItemFocus: s,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: b
}) => {
  switch (n.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        en,
        {
          item: n,
          active: n.id === t,
          collapsed: r,
          onSelect: l,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        tn,
        {
          item: n,
          collapsed: r,
          onExecute: c,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(nn, { item: n, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(ln, null);
    case "group": {
      const N = a ? a.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        rn,
        {
          item: n,
          expanded: N,
          activeItemId: t,
          collapsed: r,
          onSelect: l,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s,
          focusedId: o,
          setItemRef: d,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, on = ({ controlId: n }) => {
  const t = K(), r = le(), l = re(Jt), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = Ne(() => {
    const S = /* @__PURE__ */ new Map(), L = (A) => {
      for (const R of A)
        R.type === "group" && (S.set(R.id, R.expanded), L(R.children));
    };
    return L(c), S;
  }), a = ue((S) => {
    s((L) => {
      const A = new Map(L), R = A.get(S) ?? !1;
      return A.set(S, !R), r("toggleGroup", { itemId: S, expanded: !R }), A;
    });
  }, [r]), m = ue((S) => {
    S !== i && r("selectItem", { itemId: S });
  }, [r, i]), p = ue((S) => {
    r("executeCommand", { itemId: S });
  }, [r]), b = ue(() => {
    r("toggleCollapse", {});
  }, [r]), [N, _] = Ne(null), v = ue((S) => {
    _(S);
  }, []), f = ue(() => {
    _(null);
  }, []);
  Te(() => {
    o || _(null);
  }, [o]);
  const [T, k] = Ne(() => {
    const S = Fe(c, o, d);
    return S.length > 0 ? S[0].id : "";
  }), $ = Re(/* @__PURE__ */ new Map()), P = ue((S) => (L) => {
    L ? $.current.set(S, L) : $.current.delete(S);
  }, []), h = ue((S) => {
    k(S);
  }, []), g = Re(0), C = ue((S) => {
    k(S), g.current++;
  }, []);
  Te(() => {
    const S = $.current.get(T);
    S && document.activeElement !== S && S.focus();
  }, [T, g.current]);
  const q = ue((S) => {
    if (S.key === "Escape" && N !== null) {
      S.preventDefault(), f();
      return;
    }
    const L = Fe(c, o, d);
    if (L.length === 0) return;
    const A = L.findIndex((j) => j.id === T);
    if (A < 0) return;
    const R = L[A];
    switch (S.key) {
      case "ArrowDown": {
        S.preventDefault();
        const j = (A + 1) % L.length;
        C(L[j].id);
        break;
      }
      case "ArrowUp": {
        S.preventDefault();
        const j = (A - 1 + L.length) % L.length;
        C(L[j].id);
        break;
      }
      case "Home": {
        S.preventDefault(), C(L[0].id);
        break;
      }
      case "End": {
        S.preventDefault(), C(L[L.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        S.preventDefault(), R.type === "nav" ? m(R.id) : R.type === "command" ? p(R.id) : R.type === "group" && (o ? N === R.id ? f() : v(R.id) : a(R.id));
        break;
      }
      case "ArrowRight": {
        R.type === "group" && !o && ((d.get(R.id) ?? !1) || (S.preventDefault(), a(R.id)));
        break;
      }
      case "ArrowLeft": {
        R.type === "group" && !o && (d.get(R.id) ?? !1) && (S.preventDefault(), a(R.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    T,
    N,
    C,
    m,
    p,
    a,
    v,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: q }, c.map((S) => /* @__PURE__ */ e.createElement(
    at,
    {
      key: S.id,
      item: S,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: a,
      focusedId: T,
      setItemRef: P,
      onItemFocus: h,
      groupStates: d,
      flyoutGroupId: N,
      onOpenFlyout: v,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: o ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, sn = ({ controlId: n }) => {
  const t = K(), r = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: d }, o.map((s, a) => /* @__PURE__ */ e.createElement(G, { key: a, control: s })));
}, cn = ({ controlId: n }) => {
  const t = K(), r = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : r && (o.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((d, s) => /* @__PURE__ */ e.createElement(G, { key: s, control: d })));
}, un = ({ controlId: n }) => {
  const t = K(), r = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, d = r != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, a) => /* @__PURE__ */ e.createElement(G, { key: a, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, dn = ({ controlId: n }) => {
  const t = K(), r = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: d }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, a) => /* @__PURE__ */ e.createElement(G, { key: a, control: s }))));
}, { useCallback: mn } = e, pn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.items ?? [], c = mn((i) => {
    r("navigate", { itemId: i });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((i, o) => {
    const d = o === l.length - 1;
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
}, { useCallback: fn } = e, hn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.items ?? [], c = t.activeItemId, i = fn((o) => {
    o !== c && r("selectItem", { itemId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((o) => {
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
}, { useCallback: Ye, useEffect: Ge, useRef: _n } = e, bn = {
  "js.dialog.close": "Close"
}, vn = ({ controlId: n }) => {
  const t = K(), r = le(), l = re(bn), c = t.open === !0, i = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, s = t.actions ?? [], a = t.child, m = _n(null), p = Ye(() => {
    r("close");
  }, [r]), b = Ye((_) => {
    d && _.target === _.currentTarget && p();
  }, [d, p]);
  if (Ge(() => {
    if (!c) return;
    const _ = (v) => {
      v.key === "Escape" && p();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [c, p]), Ge(() => {
    c && m.current && m.current.focus();
  }, [c]), !c) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: m,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, i), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: p,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(G, { control: a })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((_, v) => /* @__PURE__ */ e.createElement(G, { key: v, control: _ })))
  ));
}, { useCallback: En, useEffect: gn } = e, Cn = {
  "js.drawer.close": "Close"
}, yn = ({ controlId: n }) => {
  const t = K(), r = le(), l = re(Cn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, a = En(() => {
    r("close");
  }, [r]);
  gn(() => {
    if (!c) return;
    const p = (b) => {
      b.key === "Escape" && a();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, a]);
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: n, className: m, "aria-hidden": !c }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: a,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(G, { control: s })));
}, { useCallback: qe, useEffect: wn, useState: kn } = e, Sn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, [a, m] = kn(!1), p = qe(() => {
    m(!0), setTimeout(() => {
      r("dismiss"), m(!1);
    }, 200);
  }, [r]), b = qe(() => {
    o && r(o.commandName), p();
  }, [r, o, p]);
  return wn(() => {
    if (!s || d === 0) return;
    const N = setTimeout(p, d);
    return () => clearTimeout(N);
  }, [s, d, p]), !s && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlSnackbar tlSnackbar--${i}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: b }, o.label)
  );
}, { useCallback: Pe, useEffect: je, useRef: Nn, useState: Xe } = e, Tn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Nn(null), [d, s] = Xe({ top: 0, left: 0 }), [a, m] = Xe(0), p = i.filter((v) => v.type === "item" && !v.disabled);
  je(() => {
    var h, g;
    if (!l || !c) return;
    const v = document.getElementById(c);
    if (!v) return;
    const f = v.getBoundingClientRect(), T = ((h = o.current) == null ? void 0 : h.offsetHeight) ?? 200, k = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let $ = f.bottom + 4, P = f.left;
    $ + T > window.innerHeight && ($ = f.top - T - 4), P + k > window.innerWidth && (P = f.right - k), s({ top: $, left: P }), m(0);
  }, [l, c]);
  const b = Pe(() => {
    r("close");
  }, [r]), N = Pe((v) => {
    r("selectItem", { itemId: v });
  }, [r]);
  je(() => {
    if (!l) return;
    const v = (f) => {
      o.current && !o.current.contains(f.target) && b();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [l, b]);
  const _ = Pe((v) => {
    if (v.key === "Escape") {
      b();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), m((f) => (f + 1) % p.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((f) => (f - 1 + p.length) % p.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const f = p[a];
      f && N(f.id);
    }
  }, [b, N, p, a]);
  return je(() => {
    l && o.current && o.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: _
    },
    i.map((v, f) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const k = p.indexOf(v) === a;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => N(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Rn = ({ controlId: n }) => {
  const t = K(), r = t.header, l = t.content, c = t.footer, i = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(G, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: c })), /* @__PURE__ */ e.createElement(G, { control: i }));
}, Ln = () => {
  const t = K().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Dn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ze = 50, xn = () => {
  const n = K(), t = le(), r = re(Dn), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, d = n.selectionMode ?? "single", s = n.selectedCount ?? 0, a = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [l]
  ), b = d === "multi", N = 40, _ = 20, v = e.useRef(null), f = e.useRef(null), T = e.useRef(null), [k, $] = e.useState({}), P = e.useRef(null), h = e.useRef(!1), g = e.useRef(null), [C, q] = e.useState(null), [S, L] = e.useState(null);
  e.useEffect(() => {
    P.current || $({});
  }, [l]);
  const A = e.useCallback((w) => k[w.name] ?? w.width, [k]), R = e.useMemo(() => {
    const w = [];
    let D = b && a > 0 ? N : 0;
    for (let U = 0; U < a && U < l.length; U++)
      w.push(D), D += A(l[U]);
    return w;
  }, [l, a, b, N, A]), j = c * o, Y = e.useCallback((w, D, U) => {
    U.preventDefault(), U.stopPropagation(), P.current = { column: w, startX: U.clientX, startWidth: D };
    const ee = (y) => {
      const x = P.current;
      if (!x) return;
      const B = Math.max(Ze, x.startWidth + (y.clientX - x.startX));
      $((J) => ({ ...J, [x.column]: B }));
    }, te = (y) => {
      document.removeEventListener("mousemove", ee), document.removeEventListener("mouseup", te);
      const x = P.current;
      if (x) {
        const B = Math.max(Ze, x.startWidth + (y.clientX - x.startX));
        t("columnResize", { column: x.column, width: B }), P.current = null, h.current = !0, requestAnimationFrame(() => {
          h.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ee), document.addEventListener("mouseup", te);
  }, [t]), ne = e.useCallback(() => {
    v.current && f.current && (v.current.scrollLeft = f.current.scrollLeft), T.current !== null && clearTimeout(T.current), T.current = window.setTimeout(() => {
      const w = f.current;
      if (!w) return;
      const D = w.scrollTop, U = Math.ceil(w.clientHeight / o), ee = Math.floor(D / o);
      t("scroll", { start: ee, count: U });
    }, 80);
  }, [t, o]), Q = e.useCallback((w, D, U) => {
    if (h.current) return;
    let ee;
    !D || D === "desc" ? ee = "asc" : ee = "desc";
    const te = U.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: ee, mode: te });
  }, [t]), ce = e.useCallback((w, D) => {
    g.current = w, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", w);
  }, []), F = e.useCallback((w, D) => {
    if (!g.current || g.current === w) {
      q(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const U = D.currentTarget.getBoundingClientRect(), ee = D.clientX < U.left + U.width / 2 ? "left" : "right";
    q({ column: w, side: ee });
  }, []), M = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const D = g.current;
    if (!D || !C) {
      g.current = null, q(null);
      return;
    }
    let U = l.findIndex((te) => te.name === C.column);
    if (U < 0) {
      g.current = null, q(null);
      return;
    }
    const ee = l.findIndex((te) => te.name === D);
    C.side === "right" && U++, ee < U && U--, t("columnReorder", { column: D, targetIndex: U }), g.current = null, q(null);
  }, [l, C, t]), X = e.useCallback(() => {
    g.current = null, q(null);
  }, []), u = e.useCallback((w, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), E = e.useCallback((w, D) => {
    D.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), O = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), I = e.useCallback((w, D, U) => {
    U.stopPropagation(), t("expand", { rowIndex: w, expanded: D });
  }, [t]), V = e.useCallback((w, D) => {
    D.preventDefault(), L({ x: D.clientX, y: D.clientY, colIdx: w });
  }, []), W = e.useCallback(() => {
    S && (t("setFrozenColumnCount", { count: S.colIdx + 1 }), L(null));
  }, [S, t]), Z = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), L(null);
  }, [t]);
  e.useEffect(() => {
    if (!S) return;
    const w = () => L(null), D = (U) => {
      U.key === "Escape" && L(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", D);
    };
  }, [S]);
  const oe = l.reduce((w, D) => w + A(D), 0) + (b ? N : 0), ae = s === c && c > 0, fe = s > 0 && s < c, De = e.useCallback((w) => {
    w && (w.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!g.current) return;
        w.preventDefault();
        const D = f.current, U = v.current;
        if (!D) return;
        const ee = D.getBoundingClientRect(), te = 40, y = 8;
        w.clientX < ee.left + te ? D.scrollLeft = Math.max(0, D.scrollLeft - y) : w.clientX > ee.right - te && (D.scrollLeft += y), U && (U.scrollLeft = D.scrollLeft);
      },
      onDrop: M
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: oe } }, b && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== g.current && q({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: De,
          className: "tlTableView__checkbox",
          checked: ae,
          onChange: O
        }
      )
    ), l.map((w, D) => {
      const U = A(w), ee = D === l.length - 1;
      let te = "tlTableView__headerCell";
      w.sortable && (te += " tlTableView__headerCell--sortable"), C && C.column === w.name && (te += " tlTableView__headerCell--dragOver-" + C.side);
      const y = D < a, x = D === a - 1;
      return y && (te += " tlTableView__headerCell--frozen"), x && (te += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: te,
          style: {
            ...ee && !y ? { flex: "1 0 auto", minWidth: U } : { width: U, minWidth: U },
            position: y ? "sticky" : "relative",
            ...y ? { left: R[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (B) => Q(w.name, w.sortDirection, B) : void 0,
          onContextMenu: (B) => V(D, B),
          onDragStart: (B) => ce(w.name, B),
          onDragOver: (B) => F(w.name, B),
          onDrop: M,
          onDragEnd: X
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", p > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (B) => Y(w.name, U, B)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (g.current && l.length > 0) {
            const D = l[l.length - 1];
            D.name !== g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", q({ column: D.name, side: "right" }));
          }
        },
        onDrop: M
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: ne
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: j, position: "relative", minWidth: oe } }, i.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            minWidth: oe,
            width: "100%"
          },
          onClick: (D) => u(w.index, D)
        },
        b && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: N,
              minWidth: N,
              ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
              onClick: (D) => E(w.index, D),
              tabIndex: -1
            }
          )
        ),
        l.map((D, U) => {
          const ee = A(D), te = U === l.length - 1, y = U < a, x = U === a - 1;
          let B = "tlTableView__cell";
          y && (B += " tlTableView__cell--frozen"), x && (B += " tlTableView__cell--frozenLast");
          const J = m && U === 0, ve = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: B,
              style: {
                ...te && !y ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...y ? { position: "sticky", left: R[U], zIndex: 2 } : {}
              }
            },
            J ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ve * _ } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (ut) => I(w.index, !w.expanded, ut)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(G, { control: w.cells[D.name] })) : /* @__PURE__ */ e.createElement(G, { control: w.cells[D.name] })
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
        onMouseDown: (w) => w.stopPropagation()
      },
      S.colIdx + 1 !== a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: W }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      a > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Z }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, In = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, rt = e.createContext(In), { useMemo: Pn, useRef: jn, useState: Mn, useEffect: An } = e, On = 320, $n = ({ controlId: n }) => {
  const t = K(), r = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, d = jn(null), [s, a] = Mn(
    l === "top" ? "top" : "side"
  );
  An(() => {
    if (l !== "auto") {
      a(l);
      return;
    }
    const _ = d.current;
    if (!_) return;
    const v = new ResizeObserver((f) => {
      for (const T of f) {
        const $ = T.contentRect.width / r;
        a($ < On ? "top" : "side");
      }
    });
    return v.observe(_), () => v.disconnect();
  }, [l, r]);
  const m = Pn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(rt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: N, style: b, ref: d }, i.map((_, v) => /* @__PURE__ */ e.createElement(G, { key: v, control: _ }))));
}, { useCallback: Bn } = e, Fn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, zn = ({ controlId: n }) => {
  const t = K(), r = le(), l = re(Fn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, b = Bn(() => {
    r("toggleCollapse");
  }, [r]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    a ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: N }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
      "aria-expanded": !d,
      title: d ? l["js.formGroup.expand"] : l["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((_, v) => /* @__PURE__ */ e.createElement(G, { key: v, control: _ })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((_, v) => /* @__PURE__ */ e.createElement(G, { key: v, control: _ }))));
}, { useContext: Hn, useState: Un, useCallback: Vn } = e, Wn = ({ controlId: n }) => {
  const t = K(), r = Hn(rt), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? r.resolvedLabelPosition, a = t.fullLine === !0, m = t.visible !== !1, p = t.field, b = r.readOnly, [N, _] = Un(!1), v = Vn(() => _((k) => !k), []);
  if (!m) return null;
  const f = i != null, T = [
    "tlFormField",
    `tlFormField--${s}`,
    b ? "tlFormField--readonly" : "",
    a ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: T }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !b && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: p })), !b && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !b && o && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Kn = () => {
  const n = K(), t = le(), r = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, d = n.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, a = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((b) => {
    b.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, a) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, a);
}, Yn = 20, Gn = () => {
  const n = K(), t = le(), r = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, d = n.dropIndicatorPosition ?? null, [s, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((h, g) => {
    t(g ? "collapse" : "expand", { nodeId: h });
  }, [t]), b = e.useCallback((h, g) => {
    t("select", {
      nodeId: h,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), N = e.useCallback((h, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: h, x: g.clientX, y: g.clientY });
  }, [t]), _ = e.useRef(null), v = e.useCallback((h, g) => {
    const C = g.getBoundingClientRect(), q = h.clientY - C.top, S = C.height / 3;
    return q < S ? "above" : q > S * 2 ? "below" : "within";
  }, []), f = e.useCallback((h, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", h);
  }, []), T = e.useCallback((h, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const C = v(g, g.currentTarget);
    _.current != null && window.clearTimeout(_.current), _.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: C }), _.current = null;
    }, 50);
  }, [t, v]), k = e.useCallback((h, g) => {
    g.preventDefault(), _.current != null && (window.clearTimeout(_.current), _.current = null);
    const C = v(g, g.currentTarget);
    t("drop", { nodeId: h, position: C });
  }, [t, v]), $ = e.useCallback(() => {
    _.current != null && (window.clearTimeout(_.current), _.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((h) => {
    if (r.length === 0) return;
    let g = s;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), g = Math.min(s + 1, r.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), g = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const C = r[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const C = r[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const q = C.depth;
            for (let S = s - 1; S >= 0; S--)
              if (r[S].depth < q) {
                g = S;
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
        h.preventDefault(), l === "multi" && s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), g = 0;
        break;
      case "End":
        h.preventDefault(), g = r.length - 1;
        break;
      default:
        return;
    }
    g !== s && a(g);
  }, [s, r, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    r.map((h, g) => /* @__PURE__ */ e.createElement(
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
          g === s ? "tlTreeView__node--focused" : "",
          o === h.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === h.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === h.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * Yn },
        draggable: c,
        onClick: (C) => b(h.id, C),
        onContextMenu: (C) => N(h.id, C),
        onDragStart: (C) => f(h.id, C),
        onDragOver: i ? (C) => T(h.id, C) : void 0,
        onDrop: i ? (C) => k(h.id, C) : void 0,
        onDragEnd: $
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: h.content }))
    ))
  );
};
var Me = { exports: {} }, se = {}, Ae = { exports: {} }, z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Qe;
function qn() {
  if (Qe) return z;
  Qe = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), b = Symbol.iterator;
  function N(u) {
    return u === null || typeof u != "object" ? null : (u = b && u[b] || u["@@iterator"], typeof u == "function" ? u : null);
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
  }, v = Object.assign, f = {};
  function T(u, E, O) {
    this.props = u, this.context = E, this.refs = f, this.updater = O || _;
  }
  T.prototype.isReactComponent = {}, T.prototype.setState = function(u, E) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, E, "setState");
  }, T.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function k() {
  }
  k.prototype = T.prototype;
  function $(u, E, O) {
    this.props = u, this.context = E, this.refs = f, this.updater = O || _;
  }
  var P = $.prototype = new k();
  P.constructor = $, v(P, T.prototype), P.isPureReactComponent = !0;
  var h = Array.isArray;
  function g() {
  }
  var C = { H: null, A: null, T: null, S: null }, q = Object.prototype.hasOwnProperty;
  function S(u, E, O) {
    var I = O.ref;
    return {
      $$typeof: n,
      type: u,
      key: E,
      ref: I !== void 0 ? I : null,
      props: O
    };
  }
  function L(u, E) {
    return S(u.type, E, u.props);
  }
  function A(u) {
    return typeof u == "object" && u !== null && u.$$typeof === n;
  }
  function R(u) {
    var E = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function(O) {
      return E[O];
    });
  }
  var j = /\/+/g;
  function Y(u, E) {
    return typeof u == "object" && u !== null && u.key != null ? R("" + u.key) : E.toString(36);
  }
  function ne(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(g, g) : (u.status = "pending", u.then(
          function(E) {
            u.status === "pending" && (u.status = "fulfilled", u.value = E);
          },
          function(E) {
            u.status === "pending" && (u.status = "rejected", u.reason = E);
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
  function Q(u, E, O, I, V) {
    var W = typeof u;
    (W === "undefined" || W === "boolean") && (u = null);
    var Z = !1;
    if (u === null) Z = !0;
    else
      switch (W) {
        case "bigint":
        case "string":
        case "number":
          Z = !0;
          break;
        case "object":
          switch (u.$$typeof) {
            case n:
            case t:
              Z = !0;
              break;
            case m:
              return Z = u._init, Q(
                Z(u._payload),
                E,
                O,
                I,
                V
              );
          }
      }
    if (Z)
      return V = V(u), Z = I === "" ? "." + Y(u, 0) : I, h(V) ? (O = "", Z != null && (O = Z.replace(j, "$&/") + "/"), Q(V, E, O, "", function(fe) {
        return fe;
      })) : V != null && (A(V) && (V = L(
        V,
        O + (V.key == null || u && u.key === V.key ? "" : ("" + V.key).replace(
          j,
          "$&/"
        ) + "/") + Z
      )), E.push(V)), 1;
    Z = 0;
    var oe = I === "" ? "." : I + ":";
    if (h(u))
      for (var ae = 0; ae < u.length; ae++)
        I = u[ae], W = oe + Y(I, ae), Z += Q(
          I,
          E,
          O,
          W,
          V
        );
    else if (ae = N(u), typeof ae == "function")
      for (u = ae.call(u), ae = 0; !(I = u.next()).done; )
        I = I.value, W = oe + Y(I, ae++), Z += Q(
          I,
          E,
          O,
          W,
          V
        );
    else if (W === "object") {
      if (typeof u.then == "function")
        return Q(
          ne(u),
          E,
          O,
          I,
          V
        );
      throw E = String(u), Error(
        "Objects are not valid as a React child (found: " + (E === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : E) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Z;
  }
  function ce(u, E, O) {
    if (u == null) return u;
    var I = [], V = 0;
    return Q(u, I, "", "", function(W) {
      return E.call(O, W, V++);
    }), I;
  }
  function F(u) {
    if (u._status === -1) {
      var E = u._result;
      E = E(), E.then(
        function(O) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = O);
        },
        function(O) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = O);
        }
      ), u._status === -1 && (u._status = 0, u._result = E);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var M = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var E = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(E)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, X = {
    map: ce,
    forEach: function(u, E, O) {
      ce(
        u,
        function() {
          E.apply(this, arguments);
        },
        O
      );
    },
    count: function(u) {
      var E = 0;
      return ce(u, function() {
        E++;
      }), E;
    },
    toArray: function(u) {
      return ce(u, function(E) {
        return E;
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
  return z.Activity = p, z.Children = X, z.Component = T, z.Fragment = r, z.Profiler = c, z.PureComponent = $, z.StrictMode = l, z.Suspense = s, z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return C.H.useMemoCache(u);
    }
  }, z.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, z.cacheSignal = function() {
    return null;
  }, z.cloneElement = function(u, E, O) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var I = v({}, u.props), V = u.key;
    if (E != null)
      for (W in E.key !== void 0 && (V = "" + E.key), E)
        !q.call(E, W) || W === "key" || W === "__self" || W === "__source" || W === "ref" && E.ref === void 0 || (I[W] = E[W]);
    var W = arguments.length - 2;
    if (W === 1) I.children = O;
    else if (1 < W) {
      for (var Z = Array(W), oe = 0; oe < W; oe++)
        Z[oe] = arguments[oe + 2];
      I.children = Z;
    }
    return S(u.type, V, I);
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
  }, z.createElement = function(u, E, O) {
    var I, V = {}, W = null;
    if (E != null)
      for (I in E.key !== void 0 && (W = "" + E.key), E)
        q.call(E, I) && I !== "key" && I !== "__self" && I !== "__source" && (V[I] = E[I]);
    var Z = arguments.length - 2;
    if (Z === 1) V.children = O;
    else if (1 < Z) {
      for (var oe = Array(Z), ae = 0; ae < Z; ae++)
        oe[ae] = arguments[ae + 2];
      V.children = oe;
    }
    if (u && u.defaultProps)
      for (I in Z = u.defaultProps, Z)
        V[I] === void 0 && (V[I] = Z[I]);
    return S(u, W, V);
  }, z.createRef = function() {
    return { current: null };
  }, z.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, z.isValidElement = A, z.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: F
    };
  }, z.memo = function(u, E) {
    return {
      $$typeof: a,
      type: u,
      compare: E === void 0 ? null : E
    };
  }, z.startTransition = function(u) {
    var E = C.T, O = {};
    C.T = O;
    try {
      var I = u(), V = C.S;
      V !== null && V(O, I), typeof I == "object" && I !== null && typeof I.then == "function" && I.then(g, M);
    } catch (W) {
      M(W);
    } finally {
      E !== null && O.types !== null && (E.types = O.types), C.T = E;
    }
  }, z.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, z.use = function(u) {
    return C.H.use(u);
  }, z.useActionState = function(u, E, O) {
    return C.H.useActionState(u, E, O);
  }, z.useCallback = function(u, E) {
    return C.H.useCallback(u, E);
  }, z.useContext = function(u) {
    return C.H.useContext(u);
  }, z.useDebugValue = function() {
  }, z.useDeferredValue = function(u, E) {
    return C.H.useDeferredValue(u, E);
  }, z.useEffect = function(u, E) {
    return C.H.useEffect(u, E);
  }, z.useEffectEvent = function(u) {
    return C.H.useEffectEvent(u);
  }, z.useId = function() {
    return C.H.useId();
  }, z.useImperativeHandle = function(u, E, O) {
    return C.H.useImperativeHandle(u, E, O);
  }, z.useInsertionEffect = function(u, E) {
    return C.H.useInsertionEffect(u, E);
  }, z.useLayoutEffect = function(u, E) {
    return C.H.useLayoutEffect(u, E);
  }, z.useMemo = function(u, E) {
    return C.H.useMemo(u, E);
  }, z.useOptimistic = function(u, E) {
    return C.H.useOptimistic(u, E);
  }, z.useReducer = function(u, E, O) {
    return C.H.useReducer(u, E, O);
  }, z.useRef = function(u) {
    return C.H.useRef(u);
  }, z.useState = function(u) {
    return C.H.useState(u);
  }, z.useSyncExternalStore = function(u, E, O) {
    return C.H.useSyncExternalStore(
      u,
      E,
      O
    );
  }, z.useTransition = function() {
    return C.H.useTransition();
  }, z.version = "19.2.4", z;
}
var Je;
function Xn() {
  return Je || (Je = 1, Ae.exports = qn()), Ae.exports;
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
var et;
function Zn() {
  if (et) return se;
  et = 1;
  var n = Xn();
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
  var l = {
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
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: s,
      containerInfo: a,
      implementation: m
    };
  }
  var o = n.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(s, a) {
    if (s === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, se.createPortal = function(s, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return i(s, a, null, m);
  }, se.flushSync = function(s) {
    var a = o.T, m = l.p;
    try {
      if (o.T = null, l.p = 2, s) return s();
    } finally {
      o.T = a, l.p = m, l.d.f();
    }
  }, se.preconnect = function(s, a) {
    typeof s == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, l.d.C(s, a));
  }, se.prefetchDNS = function(s) {
    typeof s == "string" && l.d.D(s);
  }, se.preinit = function(s, a) {
    if (typeof s == "string" && a && typeof a.as == "string") {
      var m = a.as, p = d(m, a.crossOrigin), b = typeof a.integrity == "string" ? a.integrity : void 0, N = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? l.d.S(
        s,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: b,
          fetchPriority: N
        }
      ) : m === "script" && l.d.X(s, {
        crossOrigin: p,
        integrity: b,
        fetchPriority: N,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, se.preinitModule = function(s, a) {
    if (typeof s == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = d(
            a.as,
            a.crossOrigin
          );
          l.d.M(s, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && l.d.M(s);
  }, se.preload = function(s, a) {
    if (typeof s == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = d(m, a.crossOrigin);
      l.d.L(s, m, {
        crossOrigin: p,
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
  }, se.preloadModule = function(s, a) {
    if (typeof s == "string")
      if (a) {
        var m = d(a.as, a.crossOrigin);
        l.d.m(s, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else l.d.m(s);
  }, se.requestFormReset = function(s) {
    l.d.r(s);
  }, se.unstable_batchedUpdates = function(s, a) {
    return s(a);
  }, se.useFormState = function(s, a, m) {
    return o.H.useFormState(s, a, m);
  }, se.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var tt;
function Qn() {
  if (tt) return Me.exports;
  tt = 1;
  function n() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(n);
      } catch (t) {
        console.error(t);
      }
  }
  return n(), Me.exports = Zn(), Me.exports;
}
var Jn = Qn();
const { useState: me, useCallback: ie, useRef: Ee, useEffect: he, useMemo: ze } = e;
function We({ image: n }) {
  if (!n) return null;
  if (n.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = n.startsWith("css:") ? n.substring(4) : n.startsWith("colored:") ? n.substring(8) : n;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function el({
  option: n,
  removable: t,
  onRemove: r,
  removeLabel: l,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: d,
  onDragEnd: s,
  dragClassName: a
}) {
  const m = ie(
    (p) => {
      p.stopPropagation(), r(n.value);
    },
    [r, n.value]
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
    /* @__PURE__ */ e.createElement(We, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, n.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": l
      },
      "×"
    )
  );
}
function tl({
  option: n,
  highlighted: t,
  searchTerm: r,
  onSelect: l,
  onMouseEnter: c,
  id: i
}) {
  const o = ie(() => l(n.value), [l, n.value]), d = ze(() => {
    if (!r) return n.label;
    const s = n.label.toLowerCase().indexOf(r.toLowerCase());
    return s < 0 ? n.label : /* @__PURE__ */ e.createElement(e.Fragment, null, n.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, n.label.substring(s, s + r.length)), n.label.substring(s + r.length));
  }, [n.label, r]);
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
    /* @__PURE__ */ e.createElement(We, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const nl = ({ controlId: n, state: t }) => {
  const r = le(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", b = i && c && !d && s, N = re({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), _ = N["js.dropdownSelect.nothingFound"], v = ie(
    (y) => N["js.dropdownSelect.removeChip"].replace("{0}", y),
    [N]
  ), [f, T] = me(!1), [k, $] = me(""), [P, h] = me(-1), [g, C] = me(!1), [q, S] = me({}), [L, A] = me(null), [R, j] = me(null), [Y, ne] = me(null), Q = Ee(null), ce = Ee(null), F = Ee(null), M = Ee(l);
  M.current = l;
  const X = Ee(-1), u = ze(
    () => new Set(l.map((y) => y.value)),
    [l]
  ), E = ze(() => {
    let y = m.filter((x) => !u.has(x.value));
    if (k) {
      const x = k.toLowerCase();
      y = y.filter((B) => B.label.toLowerCase().includes(x));
    }
    return y;
  }, [m, u, k]);
  he(() => {
    k && E.length === 1 ? h(0) : h(-1);
  }, [E.length, k]), he(() => {
    f && a && ce.current && ce.current.focus();
  }, [f, a, l]), he(() => {
    var B, J;
    if (X.current < 0) return;
    const y = X.current;
    X.current = -1;
    const x = (B = Q.current) == null ? void 0 : B.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(y, x.length - 1)].focus() : (J = Q.current) == null || J.focus();
  }, [l]), he(() => {
    if (!f) return;
    const y = (x) => {
      Q.current && !Q.current.contains(x.target) && F.current && !F.current.contains(x.target) && (T(!1), $(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [f]), he(() => {
    if (!f || !Q.current) return;
    const y = Q.current.getBoundingClientRect(), x = window.innerHeight - y.bottom, J = x < 300 && y.top > x;
    S({
      left: y.left,
      width: y.width,
      ...J ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [f]);
  const O = ie(async () => {
    if (!(d || !s) && (T(!0), $(""), h(-1), C(!1), !a))
      try {
        await r("loadOptions");
      } catch {
        C(!0);
      }
  }, [d, s, a, r]), I = ie(() => {
    var y;
    T(!1), $(""), h(-1), (y = Q.current) == null || y.focus();
  }, []), V = ie(
    (y) => {
      let x;
      if (c) {
        const B = m.find((J) => J.value === y);
        if (B)
          x = [...M.current, B];
        else
          return;
      } else {
        const B = m.find((J) => J.value === y);
        if (B)
          x = [B];
        else
          return;
      }
      M.current = x, r("valueChanged", { value: x.map((B) => B.value) }), c ? ($(""), h(-1)) : I();
    },
    [c, m, r, I]
  ), W = ie(
    (y) => {
      X.current = M.current.findIndex((B) => B.value === y);
      const x = M.current.filter((B) => B.value !== y);
      M.current = x, r("valueChanged", { value: x.map((B) => B.value) });
    },
    [r]
  ), Z = ie(
    (y) => {
      y.stopPropagation(), r("valueChanged", { value: [] }), I();
    },
    [r, I]
  ), oe = ie((y) => {
    $(y.target.value);
  }, []), ae = ie(
    (y) => {
      if (!f) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), O();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), h(
            (x) => x < E.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), h(
            (x) => x > 0 ? x - 1 : E.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), P >= 0 && P < E.length && V(E[P].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), I();
          break;
        case "Tab":
          I();
          break;
        case "Backspace":
          k === "" && c && l.length > 0 && W(l[l.length - 1].value);
          break;
      }
    },
    [
      f,
      O,
      I,
      E,
      P,
      V,
      k,
      c,
      l,
      W
    ]
  ), fe = ie(
    async (y) => {
      y.preventDefault(), C(!1);
      try {
        await r("loadOptions");
      } catch {
        C(!0);
      }
    },
    [r]
  ), De = ie(
    (y, x) => {
      A(y), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), w = ie(
    (y, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", L === null || L === y) {
        j(null), ne(null);
        return;
      }
      const B = x.currentTarget.getBoundingClientRect(), J = B.left + B.width / 2, ve = x.clientX < J ? "before" : "after";
      j(y), ne(ve);
    },
    [L]
  ), D = ie(
    (y) => {
      if (y.preventDefault(), L === null || R === null || Y === null || L === R) return;
      const x = [...M.current], [B] = x.splice(L, 1);
      let J = R;
      L < R ? J = Y === "before" ? J - 1 : J : J = Y === "before" ? J : J + 1, x.splice(J, 0, B), M.current = x, r("valueChanged", { value: x.map((ve) => ve.value) }), A(null), j(null), ne(null);
    },
    [L, R, Y, r]
  ), U = ie(() => {
    A(null), j(null), ne(null);
  }, []);
  if (he(() => {
    if (P < 0 || !F.current) return;
    const y = F.current.querySelector(
      `[id="${n}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(We, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const ee = !o && l.length > 0 && !d, te = f ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: F,
      className: "tlDropdownSelect__dropdown",
      style: q
    },
    (a || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: ce,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
        onChange: oe,
        onKeyDown: ae,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": P >= 0 ? `${n}-opt-${P}` : void 0,
        "aria-controls": `${n}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${n}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !a && !g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: fe }, N["js.dropdownSelect.error"])),
      a && E.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, _),
      a && E.map((y, x) => /* @__PURE__ */ e.createElement(
        tl,
        {
          key: y.value,
          id: `${n}-opt-${x}`,
          option: y,
          highlighted: x === P,
          searchTerm: k,
          onSelect: V,
          onMouseEnter: () => h(x)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: Q,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${n}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: f ? void 0 : O,
      onKeyDown: ae
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((y, x) => {
      let B = "";
      return L === x ? B = "tlDropdownSelect__chip--dragging" : R === x && Y === "before" ? B = "tlDropdownSelect__chip--dropBefore" : R === x && Y === "after" && (B = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        el,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: W,
          removeLabel: v(y.label),
          draggable: b,
          onDragStart: b ? (J) => De(x, J) : void 0,
          onDragOver: b ? (J) => w(x, J) : void 0,
          onDrop: b ? D : void 0,
          onDragEnd: b ? U : void 0,
          dragClassName: b ? B : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, ee && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Z,
        "aria-label": N["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), te && Jn.createPortal(te, document.body));
}, { useCallback: Oe, useRef: ll } = e, ot = "application/x-tl-color", al = ({
  colors: n,
  columns: t,
  onSelect: r,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = ll(null), d = Oe(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = Oe((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = Oe(
    (m) => (p) => {
      p.preventDefault();
      const b = p.dataTransfer.getData(ot);
      b ? i(m, b) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
    },
    [c, i]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    n.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => l(m) : void 0,
        onDragStart: m != null ? d(p) : void 0,
        onDragOver: s,
        onDrop: a(p)
      }
    ))
  );
};
function st(n) {
  return Math.max(0, Math.min(255, Math.round(n)));
}
function He(n) {
  return /^#[0-9a-fA-F]{6}$/.test(n);
}
function ct(n) {
  if (!He(n)) return [0, 0, 0];
  const t = parseInt(n.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function it(n, t, r) {
  const l = (c) => st(c).toString(16).padStart(2, "0");
  return "#" + l(n) + l(t) + l(r);
}
function rl(n, t, r) {
  const l = n / 255, c = t / 255, i = r / 255, o = Math.max(l, c, i), d = Math.min(l, c, i), s = o - d;
  let a = 0;
  s !== 0 && (o === l ? a = (c - i) / s % 6 : o === c ? a = (i - l) / s + 2 : a = (l - c) / s + 4, a *= 60, a < 0 && (a += 360));
  const m = o === 0 ? 0 : s / o;
  return [a, m, o];
}
function ol(n, t, r) {
  const l = r * t, c = l * (1 - Math.abs(n / 60 % 2 - 1)), i = r - l;
  let o = 0, d = 0, s = 0;
  return n < 60 ? (o = l, d = c, s = 0) : n < 120 ? (o = c, d = l, s = 0) : n < 180 ? (o = 0, d = l, s = c) : n < 240 ? (o = 0, d = c, s = l) : n < 300 ? (o = c, d = 0, s = l) : (o = l, d = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((d + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function sl(n) {
  return rl(...ct(n));
}
function $e(n, t, r) {
  return it(...ol(n, t, r));
}
const { useCallback: _e, useRef: nt } = e, cl = ({ color: n, onColorChange: t }) => {
  const [r, l, c] = sl(n), i = nt(null), o = nt(null), d = _e(
    (_, v) => {
      var $;
      const f = ($ = i.current) == null ? void 0 : $.getBoundingClientRect();
      if (!f) return;
      const T = Math.max(0, Math.min(1, (_ - f.left) / f.width)), k = Math.max(0, Math.min(1, 1 - (v - f.top) / f.height));
      t($e(r, T, k));
    },
    [r, t]
  ), s = _e(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), d(_.clientX, _.clientY);
    },
    [d]
  ), a = _e(
    (_) => {
      _.buttons !== 0 && d(_.clientX, _.clientY);
    },
    [d]
  ), m = _e(
    (_) => {
      var k;
      const v = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!v) return;
      const T = Math.max(0, Math.min(1, (_ - v.top) / v.height)) * 360;
      t($e(T, l, c));
    },
    [l, c, t]
  ), p = _e(
    (_) => {
      _.preventDefault(), _.target.setPointerCapture(_.pointerId), m(_.clientY);
    },
    [m]
  ), b = _e(
    (_) => {
      _.buttons !== 0 && m(_.clientY);
    },
    [m]
  ), N = $e(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: N },
      onPointerDown: s,
      onPointerMove: a
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${l * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: b
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
function il(n, t) {
  const r = t.toUpperCase();
  return n.some((l) => l != null && l.toUpperCase() === r);
}
const ul = {
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
}, { useState: ye, useCallback: de, useEffect: Be, useRef: dl, useLayoutEffect: ml } = e, pl = ({
  anchorRef: n,
  currentColor: t,
  palette: r,
  paletteColumns: l,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: d,
  onPaletteChange: s
}) => {
  const [a, m] = ye("palette"), [p, b] = ye(t), N = dl(null), _ = re(ul), [v, f] = ye(null);
  ml(() => {
    if (!n.current || !N.current) return;
    const F = n.current.getBoundingClientRect(), M = N.current.getBoundingClientRect();
    let X = F.bottom + 4, u = F.left;
    X + M.height > window.innerHeight && (X = F.top - M.height - 4), u + M.width > window.innerWidth && (u = Math.max(0, F.right - M.width)), f({ top: X, left: u });
  }, [n]);
  const T = p != null, [k, $, P] = T ? ct(p) : [0, 0, 0], [h, g] = ye((p == null ? void 0 : p.toUpperCase()) ?? "");
  Be(() => {
    g((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Be(() => {
    const F = (M) => {
      M.key === "Escape" && d();
    };
    return document.addEventListener("keydown", F), () => document.removeEventListener("keydown", F);
  }, [d]), Be(() => {
    const F = (X) => {
      N.current && !N.current.contains(X.target) && d();
    }, M = setTimeout(() => document.addEventListener("mousedown", F), 0);
    return () => {
      clearTimeout(M), document.removeEventListener("mousedown", F);
    };
  }, [d]);
  const C = de(
    (F) => (M) => {
      const X = parseInt(M.target.value, 10);
      if (isNaN(X)) return;
      const u = st(X);
      b(it(F === "r" ? u : k, F === "g" ? u : $, F === "b" ? u : P));
    },
    [k, $, P]
  ), q = de(
    (F) => {
      if (p != null) {
        F.dataTransfer.setData(ot, p.toUpperCase()), F.dataTransfer.effectAllowed = "move";
        const M = document.createElement("div");
        M.style.width = "33px", M.style.height = "33px", M.style.backgroundColor = p, M.style.borderRadius = "3px", M.style.border = "1px solid rgba(0,0,0,0.1)", M.style.position = "absolute", M.style.top = "-9999px", document.body.appendChild(M), F.dataTransfer.setDragImage(M, 16, 16), requestAnimationFrame(() => document.body.removeChild(M));
      }
    },
    [p]
  ), S = de((F) => {
    const M = F.target.value;
    g(M), He(M) && b(M);
  }, []), L = de(() => {
    b(null);
  }, []), A = de((F) => {
    b(F);
  }, []), R = de(
    (F) => {
      o(F);
    },
    [o]
  ), j = de(
    (F, M) => {
      const X = [...r], u = X[F];
      X[F] = X[M], X[M] = u, s(X);
    },
    [r, s]
  ), Y = de(
    (F, M) => {
      const X = [...r];
      X[F] = M, s(X);
    },
    [r, s]
  ), ne = de(() => {
    s([...c]);
  }, [c, s]), Q = de(
    (F) => {
      if (il(r, F)) return;
      const M = r.indexOf(null);
      if (M < 0) return;
      const X = [...r];
      X[M] = F.toUpperCase(), s(X);
    },
    [r, s]
  ), ce = de(() => {
    p != null && Q(p), o(p);
  }, [p, o, Q]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: N,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      _["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      _["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      al,
      {
        colors: r,
        columns: l,
        onSelect: A,
        onConfirm: R,
        onSwap: j,
        onReplace: Y
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ne }, _["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(cl, { color: p ?? "#000000", onColorChange: b }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, _["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (T ? "" : " tlColorInput--noColor"),
        style: T ? { backgroundColor: p } : void 0,
        draggable: T,
        onDragStart: T ? q : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? k : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? $ : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? P : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, _["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !He(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: S
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, _["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, _["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: ce }, _["js.colorInput.ok"]))
  );
}, fl = { "js.colorInput.chooseColor": "Choose color" }, { useState: hl, useCallback: we, useRef: _l } = e, bl = ({ controlId: n, state: t }) => {
  const r = le(), l = re(fl), [c, i] = hl(!1), o = _l(null), d = t.value, s = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, b = we(() => {
    s && i(!0);
  }, [s]), N = we(
    (f) => {
      i(!1), r("valueChanged", { value: f });
    },
    [r]
  ), _ = we(() => {
    i(!1);
  }, []), v = we(
    (f) => {
      r("paletteChanged", { palette: f });
    },
    [r]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (d == null ? " tlColorInput__swatch--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      onClick: b,
      disabled: t.disabled === !0,
      title: d ?? "",
      "aria-label": l["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    pl,
    {
      anchorRef: o,
      currentColor: d,
      palette: a,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: _,
      onPaletteChange: v
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: n,
      className: "tlColorInput tlColorInput--immutable" + (d == null ? " tlColorInput--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      title: d ?? ""
    }
  );
}, { useState: ge, useCallback: pe, useEffect: ke, useRef: lt, useLayoutEffect: vl, useMemo: El } = e, gl = {
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
  "js.iconSelect.clear": "Clear icon"
};
function Le({ encoded: n, className: t }) {
  if (n.startsWith("css:")) {
    const r = n.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  if (n.startsWith("colored:")) {
    const r = n.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  return n.startsWith("/") || n.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
}
const Cl = ({
  anchorRef: n,
  currentValue: t,
  icons: r,
  iconsLoaded: l,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const d = re(gl), [s, a] = ge("simple"), [m, p] = ge(""), [b, N] = ge(t ?? ""), [_, v] = ge(!1), [f, T] = ge(null), k = lt(null), $ = lt(null);
  vl(() => {
    if (!n.current || !k.current) return;
    const R = n.current.getBoundingClientRect(), j = k.current.getBoundingClientRect();
    let Y = R.bottom + 4, ne = R.left;
    Y + j.height > window.innerHeight && (Y = R.top - j.height - 4), ne + j.width > window.innerWidth && (ne = Math.max(0, R.right - j.width)), T({ top: Y, left: ne });
  }, [n]), ke(() => {
    !l && !_ && o().catch(() => v(!0));
  }, [l, _, o]), ke(() => {
    l && $.current && $.current.focus();
  }, [l]), ke(() => {
    const R = (j) => {
      j.key === "Escape" && i();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [i]), ke(() => {
    const R = (Y) => {
      k.current && !k.current.contains(Y.target) && i();
    }, j = setTimeout(() => document.addEventListener("mousedown", R), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", R);
    };
  }, [i]);
  const P = El(() => {
    if (!m) return r;
    const R = m.toLowerCase();
    return r.filter(
      (j) => j.prefix.toLowerCase().includes(R) || j.label.toLowerCase().includes(R) || j.terms != null && j.terms.some((Y) => Y.includes(R))
    );
  }, [r, m]), h = pe((R) => {
    p(R.target.value);
  }, []), g = pe(
    (R) => {
      c(R);
    },
    [c]
  ), C = pe((R) => {
    N(R);
  }, []), q = pe((R) => {
    N(R.target.value);
  }, []), S = pe(() => {
    c(b || null);
  }, [b, c]), L = pe(() => {
    c(null);
  }, [c]), A = pe(async (R) => {
    R.preventDefault(), v(!1);
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
      style: f ? { top: f.top, left: f.left, visibility: "visible" } : { visibility: "hidden" }
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
    ), t && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: L,
        title: d["js.iconSelect.clear"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox",
        style: s === "advanced" ? { maxHeight: "160px" } : void 0
      },
      !l && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: A }, d["js.iconSelect.loadError"])),
      l && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, d["js.iconSelect.noResults"]),
      l && P.map(
        (R) => R.variants.map((j) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: j.encoded,
            className: "tlIconSelect__iconCell" + (j.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": j.encoded === t,
            tabIndex: 0,
            title: R.label,
            onClick: () => s === "simple" ? g(j.encoded) : C(j.encoded),
            onKeyDown: (Y) => {
              (Y.key === "Enter" || Y.key === " ") && (Y.preventDefault(), s === "simple" ? g(j.encoded) : C(j.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Le, { encoded: j.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: b,
        onChange: q
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, b && /* @__PURE__ */ e.createElement(Le, { encoded: b })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, b ? b.startsWith("css:") ? b.substring(4) : b : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, d["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: S }, d["js.iconSelect.ok"]))
  );
}, yl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: wl, useCallback: Se, useRef: kl } = e, Sl = ({ controlId: n, state: t }) => {
  const r = le(), l = re(yl), [c, i] = wl(!1), o = kl(null), d = t.value, s = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, b = Se(() => {
    s && !a && i(!0);
  }, [s, a]), N = Se(
    (f) => {
      i(!1), r("valueChanged", { value: f });
    },
    [r]
  ), _ = Se(() => {
    i(!1);
  }, []), v = Se(async () => {
    await r("loadIcons");
  }, [r]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (d == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: b,
      disabled: a,
      title: d ?? "",
      "aria-label": l["js.iconSelect.chooseIcon"]
    },
    d ? /* @__PURE__ */ e.createElement(Le, { encoded: d }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Cl,
    {
      anchorRef: o,
      currentValue: d,
      icons: m,
      iconsLoaded: p,
      onSelect: N,
      onCancel: _,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, d ? /* @__PURE__ */ e.createElement(Le, { encoded: d }) : null));
};
H("TLButton", kt);
H("TLToggleButton", Nt);
H("TLTextInput", pt);
H("TLNumberInput", ht);
H("TLDatePicker", bt);
H("TLSelect", Et);
H("TLCheckbox", Ct);
H("TLTable", yt);
H("TLCounter", Tt);
H("TLTabBar", Lt);
H("TLFieldList", Dt);
H("TLAudioRecorder", It);
H("TLAudioPlayer", jt);
H("TLFileUpload", At);
H("TLDownload", $t);
H("TLPhotoCapture", Ft);
H("TLPhotoViewer", Ht);
H("TLSplitPanel", Ut);
H("TLPanel", Xt);
H("TLMaximizeRoot", Zt);
H("TLDeckPane", Qt);
H("TLSidebar", on);
H("TLStack", sn);
H("TLGrid", cn);
H("TLCard", un);
H("TLAppBar", dn);
H("TLBreadcrumb", pn);
H("TLBottomBar", hn);
H("TLDialog", vn);
H("TLDrawer", yn);
H("TLSnackbar", Sn);
H("TLMenu", Tn);
H("TLAppShell", Rn);
H("TLTextCell", Ln);
H("TLTableView", xn);
H("TLFormLayout", $n);
H("TLFormGroup", zn);
H("TLFormField", Wn);
H("TLResourceCell", Kn);
H("TLTreeView", Gn);
H("TLDropdownSelect", nl);
H("TLColorInput", bl);
H("TLIconSelect", Sl);
