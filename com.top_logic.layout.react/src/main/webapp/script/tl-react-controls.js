import { React as e, useTLFieldValue as Ee, getComponent as lt, useTLState as W, useTLCommand as ne, TLChild as K, useTLUpload as Be, useI18N as ce, useTLDataUrl as $e, register as F } from "tl-react-bridge";
const { useCallback: at } = e, ot = ({ controlId: l, state: t }) => {
  const [a, r] = Ee(), c = at(
    (i) => {
      r(i.target.value);
    },
    [r]
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
}, { useCallback: st } = e, ct = ({ controlId: l, state: t, config: a }) => {
  const [r, c] = Ee(), i = st(
    (d) => {
      const s = d.target.value, n = s === "" ? null : Number(s);
      c(n);
    },
    [c]
  ), o = a != null && a.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: r != null ? String(r) : "",
      onChange: i,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: it } = e, ut = ({ controlId: l, state: t }) => {
  const [a, r] = Ee(), c = it(
    (i) => {
      r(i.target.value || null);
    },
    [r]
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
}, { useCallback: dt } = e, mt = ({ controlId: l, state: t, config: a }) => {
  var d;
  const [r, c] = Ee(), i = dt(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const s = ((d = o.find((n) => n.value === r)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: r ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: pt } = e, ft = ({ controlId: l, state: t }) => {
  const [a, r] = Ee(), c = pt(
    (i) => {
      r(i.target.checked);
    },
    [r]
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
}, ht = ({ controlId: l, state: t }) => {
  const a = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, a.map((o) => {
    const d = o.cellModule ? lt(o.cellModule) : void 0, s = c[o.name];
    if (d) {
      const n = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: l + "-" + i + "-" + o.name,
          state: n
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: _t } = e, bt = ({ controlId: l, command: t, label: a, disabled: r }) => {
  const c = W(), i = ne(), o = t ?? "click", d = a ?? c.label, s = r ?? c.disabled === !0, n = _t(() => {
    i(o);
  }, [i, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: n,
      disabled: s,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: vt } = e, Et = ({ controlId: l, command: t, label: a, active: r, disabled: c }) => {
  const i = W(), o = ne(), d = t ?? "click", s = a ?? i.label, n = r ?? i.active === !0, m = c ?? i.disabled === !0, _ = vt(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: _,
      disabled: m,
      className: "tlReactButton" + (n ? " tlReactButtonActive" : "")
    },
    s
  );
}, gt = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ct } = e, yt = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.tabs ?? [], c = t.activeTabId, i = Ct((o) => {
    o !== c && a("selectTab", { tabId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, wt = ({ controlId: l }) => {
  const t = W(), a = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: c })))));
}, kt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Nt = ({ controlId: l }) => {
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), n = e.useRef(null), m = t.status ?? "idle", _ = t.error, E = m === "received" ? "idle" : r !== "idle" ? r : m, R = e.useCallback(async () => {
    if (r === "recording") {
      const N = d.current;
      N && N.state !== "inactive" && N.stop();
      return;
    }
    if (r !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const N = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        n.current = N, s.current = [];
        const O = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(N, O ? { mimeType: O } : void 0);
        d.current = P, P.ondataavailable = (p) => {
          p.data.size > 0 && s.current.push(p.data);
        }, P.onstop = async () => {
          N.getTracks().forEach((C) => C.stop()), n.current = null;
          const p = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], p.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", p, "recording.webm"), await a(g), c("idle");
        }, P.start(), c("recording");
      } catch (N) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", N), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [r, a]), h = ce(kt), b = E === "recording" ? h["js.audioRecorder.stop"] : E === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], f = E === "uploading", S = ["tlAudioRecorder__button"];
  return E === "recording" && S.push("tlAudioRecorder__button--recording"), E === "uploading" && S.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: R,
      disabled: f,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${E === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[i]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _));
}, St = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Tt = ({ controlId: l }) => {
  const t = W(), a = $e(), r = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(r ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), n = e.useRef(c);
  e.useEffect(() => {
    r ? i === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [r]), e.useEffect(() => {
    c !== n.current && (n.current = c, d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
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
        const f = await fetch(a);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const S = await f.blob();
        s.current = URL.createObjectURL(S);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    d.current = b, b.onended = () => {
      o("idle");
    }, b.play(), o("playing");
  }, [i, a]), _ = ce(St), E = i === "loading" ? _["js.loading"] : i === "playing" ? _["js.audioPlayer.pause"] : i === "disabled" ? _["js.audioPlayer.noAudio"] : _["js.audioPlayer.play"], R = i === "disabled" || i === "loading", h = ["tlAudioPlayer__button"];
  return i === "playing" && h.push("tlAudioPlayer__button--playing"), i === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: m,
      disabled: R,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Rt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Lt = ({ controlId: l }) => {
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", n = t.error, m = t.accept ?? "", _ = s === "received" ? "idle" : r !== "idle" ? r : s, E = e.useCallback(async (p) => {
    c("uploading");
    const g = new FormData();
    g.append("file", p, p.name), await a(g), c("idle");
  }, [a]), R = e.useCallback((p) => {
    var C;
    const g = (C = p.target.files) == null ? void 0 : C[0];
    g && E(g);
  }, [E]), h = e.useCallback(() => {
    var p;
    r !== "uploading" && ((p = d.current) == null || p.click());
  }, [r]), b = e.useCallback((p) => {
    p.preventDefault(), p.stopPropagation(), o(!0);
  }, []), f = e.useCallback((p) => {
    p.preventDefault(), p.stopPropagation(), o(!1);
  }, []), S = e.useCallback((p) => {
    var C;
    if (p.preventDefault(), p.stopPropagation(), o(!1), r === "uploading") return;
    const g = (C = p.dataTransfer.files) == null ? void 0 : C[0];
    g && E(g);
  }, [r, E]), N = _ === "uploading", O = ce(Rt), P = _ === "uploading" ? O["js.uploading"] : O["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: f,
      onDrop: S
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
        className: "tlFileUpload__button" + (_ === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: h,
        disabled: N,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    n && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, n)
  );
}, Dt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, xt = ({ controlId: l }) => {
  const t = W(), a = $e(), r = ne(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, n] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      n(!0);
      try {
        const h = a + (a.includes("?") ? "&" : "?") + "rev=" + i, b = await fetch(h);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const f = await b.blob(), S = URL.createObjectURL(f), N = document.createElement("a");
        N.href = S, N.download = o, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(S);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        n(!1);
      }
    }
  }, [c, s, a, i, o]), _ = e.useCallback(async () => {
    c && await r("clear");
  }, [c, r]), E = ce(Dt);
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
      onClick: _,
      title: E["js.download.clear"],
      "aria-label": E["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, It = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Pt = ({ controlId: l }) => {
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), n = e.useRef(null), m = e.useRef(null), _ = e.useRef(null), E = e.useRef(null), R = e.useRef(null), h = t.error, b = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), n.current && (n.current.srcObject = null);
  }, []), S = e.useCallback(() => {
    f(), c("idle");
  }, [f]), N = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (o(null), !b) {
        (T = E.current) == null || T.click();
        return;
      }
      try {
        const j = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = j, c("overlayOpen");
      } catch (j) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", j), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [r, b]), O = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = n.current, j = _.current;
    if (!T || !j)
      return;
    j.width = T.videoWidth, j.height = T.videoHeight;
    const I = j.getContext("2d");
    I && (I.drawImage(T, 0, 0), f(), c("uploading"), j.toBlob(async (Y) => {
      if (!Y) {
        c("idle");
        return;
      }
      const te = new FormData();
      te.append("photo", Y, "capture.jpg"), await a(te), c("idle");
    }, "image/jpeg", 0.85));
  }, [r, a, f]), P = e.useCallback(async (T) => {
    var Y;
    const j = (Y = T.target.files) == null ? void 0 : Y[0];
    if (!j) return;
    c("uploading");
    const I = new FormData();
    I.append("photo", j, j.name), await a(I), c("idle"), E.current && (E.current.value = "");
  }, [a]);
  e.useEffect(() => {
    r === "overlayOpen" && n.current && m.current && (n.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var j;
    if (r !== "overlayOpen") return;
    (j = R.current) == null || j.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const T = (j) => {
      j.key === "Escape" && S();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [r, S]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const p = ce(It), g = r === "uploading" ? p["js.uploading"] : p["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const q = ["tlPhotoCapture__overlayVideo"];
  d && q.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return d && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: N,
      disabled: r === "uploading",
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: _, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: R,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: S }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: n,
        className: q.join(" "),
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
        title: p["js.photoCapture.mirror"],
        "aria-label": p["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: O,
        title: p["js.photoCapture.capture"],
        "aria-label": p["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: S,
        title: p["js.photoCapture.close"],
        "aria-label": p["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, p[i]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, jt = {
  "js.photoViewer.alt": "Captured photo"
}, Mt = ({ controlId: l }) => {
  const t = W(), a = $e(), r = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), d = e.useRef(c);
  e.useEffect(() => {
    if (!r) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (c === d.current && i)
      return;
    d.current = c, i && (URL.revokeObjectURL(i), o(null));
    let n = !1;
    return (async () => {
      try {
        const m = await fetch(a);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const _ = await m.blob();
        n || o(URL.createObjectURL(_));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      n = !0;
    };
  }, [r, c, a]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = ce(jt);
  return !r || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ze, useRef: Se } = e, At = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = r === "horizontal", d = i.length > 0 && i.every((f) => f.collapsed), s = !d && i.some((f) => f.collapsed), n = d ? !o : o, m = Se(null), _ = Se(null), E = Se(null), R = ze((f, S) => {
    const N = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? d && !n ? N.flex = "1 0 0%" : N.flex = "0 0 auto" : S !== void 0 ? N.flex = `0 0 ${S}px` : f.unit === "%" || s ? N.flex = `${f.size} 0 0%` : N.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (N.minWidth = o ? f.minSize : void 0, N.minHeight = o ? void 0 : f.minSize), N;
  }, [o, d, s, n]), h = ze((f, S) => {
    f.preventDefault();
    const N = m.current;
    if (!N) return;
    const O = i[S], P = i[S + 1], p = N.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    p.forEach((k) => {
      g.push(o ? k.offsetWidth : k.offsetHeight);
    }), E.current = g, _.current = {
      splitterIndex: S,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: g[S],
      startSizeAfter: g[S + 1],
      childBefore: O,
      childAfter: P
    };
    const C = (k) => {
      const T = _.current;
      if (!T || !E.current) return;
      const I = (o ? k.clientX : k.clientY) - T.startPos, Y = T.childBefore.minSize || 0, te = T.childAfter.minSize || 0;
      let H = T.startSizeBefore + I, $ = T.startSizeAfter - I;
      H < Y && ($ += H - Y, H = Y), $ < te && (H += $ - te, $ = te), E.current[T.splitterIndex] = H, E.current[T.splitterIndex + 1] = $;
      const X = N.querySelectorAll(":scope > .tlSplitPanel__child"), J = X[T.splitterIndex], re = X[T.splitterIndex + 1];
      J && (J.style.flex = `0 0 ${H}px`), re && (re.style.flex = `0 0 ${$}px`);
    }, q = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", q), document.body.style.cursor = "", document.body.style.userSelect = "", E.current) {
        const k = {};
        i.forEach((T, j) => {
          const I = T.control;
          I != null && I.controlId && E.current && (k[I.controlId] = E.current[j]);
        }), a("updateSizes", { sizes: k });
      }
      E.current = null, _.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", q), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), b = [];
  return i.forEach((f, S) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${S}`,
          className: `tlSplitPanel__child${f.collapsed && n ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: R(f)
        },
        /* @__PURE__ */ e.createElement(K, { control: f.control })
      )
    ), c && S < i.length - 1) {
      const N = i[S + 1];
      !f.collapsed && !N.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${S}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (P) => h(P, S)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: n ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    b
  );
}, { useCallback: Te } = e, Ot = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Bt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), $t = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ft = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ht = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ut = ({ controlId: l }) => {
  const t = W(), a = ne(), r = ce(Ot), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, n = t.toolbarButtons ?? [], m = i === "MINIMIZED", _ = i === "MAXIMIZED", E = i === "HIDDEN", R = Te(() => {
    a("toggleMinimize");
  }, [a]), h = Te(() => {
    a("toggleMaximize");
  }, [a]), b = Te(() => {
    a("popOut");
  }, [a]);
  if (E)
    return null;
  const f = _ ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, n.map((S, N) => /* @__PURE__ */ e.createElement("span", { key: N, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(K, { control: S }))), o && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: R,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement($t, null) : /* @__PURE__ */ e.createElement(Bt, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: _ ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      _ ? /* @__PURE__ */ e.createElement(zt, null) : /* @__PURE__ */ e.createElement(Ft, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ht, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child }))
  );
}, Vt = ({ controlId: l }) => {
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
}, Wt = ({ controlId: l }) => {
  const t = W();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ie, useState: ye, useEffect: we, useRef: ke } = e, Kt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Me(l, t, a, r) {
  const c = [];
  for (const i of l)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: r }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: r }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (a.get(i.id) ?? i.expanded) && !t && c.push(...Me(i.children, t, a, i.id)));
  return c;
}
const _e = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Yt = ({ item: l, active: t, collapsed: a, onSelect: r, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: a ? l.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(l.id)
  },
  a && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(_e, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !a && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Gt = ({ item: l, collapsed: t, onExecute: a, tabIndex: r, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: c,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), qt = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(_e, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Xt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Zt = ({ item: l, activeItemId: t, anchorRect: a, onSelect: r, onExecute: c, onClose: i }) => {
  const o = ke(null);
  we(() => {
    const n = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", n), () => document.removeEventListener("mousedown", n);
  }, [i]), we(() => {
    const n = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", n), () => document.removeEventListener("keydown", n);
  }, [i]);
  const d = ie((n) => {
    n.type === "nav" ? (r(n.id), i()) : n.type === "command" && (c(n.id), i());
  }, [r, c, i]), s = {};
  return a && (s.left = a.right, s.top = a.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((n) => {
    if (n.type === "nav" || n.type === "command") {
      const m = n.type === "nav" && n.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: n.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(n)
        },
        /* @__PURE__ */ e.createElement(_e, { icon: n.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
        n.type === "nav" && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
      );
    }
    return n.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: n.id, className: "tlSidebar__flyoutSectionHeader" }, n.label) : n.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: n.id, className: "tlSidebar__separator" }) : null;
  }));
}, Qt = ({
  item: l,
  expanded: t,
  activeItemId: a,
  collapsed: r,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: d,
  itemRef: s,
  onFocus: n,
  focusedId: m,
  setItemRef: _,
  onItemFocus: E,
  flyoutGroupId: R,
  onOpenFlyout: h,
  onCloseFlyout: b
}) => {
  const f = ke(null), [S, N] = ye(null), O = ie(() => {
    r ? R === l.id ? b() : (f.current && N(f.current.getBoundingClientRect()), h(l.id)) : o(l.id);
  }, [r, R, l.id, o, h, b]), P = ie((g) => {
    f.current = g, s(g);
  }, [s]), p = r && R === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (p ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: O,
      title: r ? l.label : void 0,
      "aria-expanded": r ? p : t,
      tabIndex: d,
      ref: P,
      onFocus: () => n(l.id)
    },
    /* @__PURE__ */ e.createElement(_e, { icon: l.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
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
  ), p && /* @__PURE__ */ e.createElement(
    Zt,
    {
      item: l,
      activeItemId: a,
      anchorRect: S,
      onSelect: c,
      onExecute: i,
      onClose: b
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((g) => /* @__PURE__ */ e.createElement(
    Qe,
    {
      key: g.id,
      item: g,
      activeItemId: a,
      collapsed: r,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: _,
      onItemFocus: E,
      groupStates: null,
      flyoutGroupId: R,
      onOpenFlyout: h,
      onCloseFlyout: b
    }
  ))));
}, Qe = ({
  item: l,
  activeItemId: t,
  collapsed: a,
  onSelect: r,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: d,
  onItemFocus: s,
  groupStates: n,
  flyoutGroupId: m,
  onOpenFlyout: _,
  onCloseFlyout: E
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Yt,
        {
          item: l,
          active: l.id === t,
          collapsed: a,
          onSelect: r,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Gt,
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
      return /* @__PURE__ */ e.createElement(qt, { item: l, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(Xt, null);
    case "group": {
      const R = n ? n.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Qt,
        {
          item: l,
          expanded: R,
          activeItemId: t,
          collapsed: a,
          onSelect: r,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: d,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: _,
          onCloseFlyout: E
        }
      );
    }
    default:
      return null;
  }
}, Jt = ({ controlId: l }) => {
  const t = W(), a = ne(), r = ce(Kt), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = ye(() => {
    const k = /* @__PURE__ */ new Map(), T = (j) => {
      for (const I of j)
        I.type === "group" && (k.set(I.id, I.expanded), T(I.children));
    };
    return T(c), k;
  }), n = ie((k) => {
    s((T) => {
      const j = new Map(T), I = j.get(k) ?? !1;
      return j.set(k, !I), a("toggleGroup", { itemId: k, expanded: !I }), j;
    });
  }, [a]), m = ie((k) => {
    k !== i && a("selectItem", { itemId: k });
  }, [a, i]), _ = ie((k) => {
    a("executeCommand", { itemId: k });
  }, [a]), E = ie(() => {
    a("toggleCollapse", {});
  }, [a]), [R, h] = ye(null), b = ie((k) => {
    h(k);
  }, []), f = ie(() => {
    h(null);
  }, []);
  we(() => {
    o || h(null);
  }, [o]);
  const [S, N] = ye(() => {
    const k = Me(c, o, d);
    return k.length > 0 ? k[0].id : "";
  }), O = ke(/* @__PURE__ */ new Map()), P = ie((k) => (T) => {
    T ? O.current.set(k, T) : O.current.delete(k);
  }, []), p = ie((k) => {
    N(k);
  }, []), g = ke(0), C = ie((k) => {
    N(k), g.current++;
  }, []);
  we(() => {
    const k = O.current.get(S);
    k && document.activeElement !== k && k.focus();
  }, [S, g.current]);
  const q = ie((k) => {
    if (k.key === "Escape" && R !== null) {
      k.preventDefault(), f();
      return;
    }
    const T = Me(c, o, d);
    if (T.length === 0) return;
    const j = T.findIndex((Y) => Y.id === S);
    if (j < 0) return;
    const I = T[j];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const Y = (j + 1) % T.length;
        C(T[Y].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const Y = (j - 1 + T.length) % T.length;
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
        k.preventDefault(), I.type === "nav" ? m(I.id) : I.type === "command" ? _(I.id) : I.type === "group" && (o ? R === I.id ? f() : b(I.id) : n(I.id));
        break;
      }
      case "ArrowRight": {
        I.type === "group" && !o && ((d.get(I.id) ?? !1) || (k.preventDefault(), n(I.id)));
        break;
      }
      case "ArrowLeft": {
        I.type === "group" && !o && (d.get(I.id) ?? !1) && (k.preventDefault(), n(I.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    S,
    R,
    C,
    m,
    _,
    n,
    b,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: q }, c.map((k) => /* @__PURE__ */ e.createElement(
    Qe,
    {
      key: k.id,
      item: k,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: _,
      onToggleGroup: n,
      focusedId: S,
      setItemRef: P,
      onItemFocus: p,
      groupStates: d,
      flyoutGroupId: R,
      onOpenFlyout: b,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: E,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, en = ({ controlId: l }) => {
  const t = W(), a = t.direction ?? "column", r = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, o.map((s, n) => /* @__PURE__ */ e.createElement(K, { key: n, control: s })));
}, tn = ({ controlId: l }) => {
  const t = W(), a = t.columns, r = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((d, s) => /* @__PURE__ */ e.createElement(K, { key: s, control: d })));
}, nn = ({ controlId: l }) => {
  const t = W(), a = t.title, r = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, d = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, n) => /* @__PURE__ */ e.createElement(K, { key: n, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(K, { control: o })));
}, rn = ({ controlId: l }) => {
  const t = W(), a = t.title ?? "", r = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, n) => /* @__PURE__ */ e.createElement(K, { key: n, control: s }))));
}, { useCallback: ln } = e, an = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.items ?? [], c = ln((i) => {
    a("navigate", { itemId: i });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((i, o) => {
    const d = o === r.length - 1;
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
}, { useCallback: on } = e, sn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.items ?? [], c = t.activeItemId, i = on((o) => {
    o !== c && a("selectItem", { itemId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((o) => {
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
}, { useCallback: He, useEffect: Ue, useRef: cn } = e, un = {
  "js.dialog.close": "Close"
}, dn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = ce(un), c = t.open === !0, i = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, s = t.actions ?? [], n = t.child, m = cn(null), _ = He(() => {
    a("close");
  }, [a]), E = He((h) => {
    d && h.target === h.currentTarget && _();
  }, [d, _]);
  if (Ue(() => {
    if (!c) return;
    const h = (b) => {
      b.key === "Escape" && _();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [c, _]), Ue(() => {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: R }, i), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: _,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(K, { control: n })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((h, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: h })))
  ));
}, { useCallback: mn, useEffect: pn } = e, fn = {
  "js.drawer.close": "Close"
}, hn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = ce(fn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, n = mn(() => {
    a("close");
  }, [a]);
  pn(() => {
    if (!c) return;
    const _ = (E) => {
      E.key === "Escape" && n();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [c, n]);
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
      onClick: n,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(K, { control: s })));
}, { useCallback: Ve, useEffect: _n, useState: bn } = e, vn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, [n, m] = bn(!1), _ = Ve(() => {
    m(!0), setTimeout(() => {
      a("dismiss"), m(!1);
    }, 200);
  }, [a]), E = Ve(() => {
    o && a(o.commandName), _();
  }, [a, o, _]);
  return _n(() => {
    if (!s || d === 0) return;
    const R = setTimeout(_, d);
    return () => clearTimeout(R);
  }, [s, d, _]), !s && !n ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${n ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: E }, o.label)
  );
}, { useCallback: Re, useEffect: Le, useRef: En, useState: We } = e, gn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = t.open === !0, c = t.anchorId, i = t.items ?? [], o = En(null), [d, s] = We({ top: 0, left: 0 }), [n, m] = We(0), _ = i.filter((b) => b.type === "item" && !b.disabled);
  Le(() => {
    var p, g;
    if (!r || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const f = b.getBoundingClientRect(), S = ((p = o.current) == null ? void 0 : p.offsetHeight) ?? 200, N = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let O = f.bottom + 4, P = f.left;
    O + S > window.innerHeight && (O = f.top - S - 4), P + N > window.innerWidth && (P = f.right - N), s({ top: O, left: P }), m(0);
  }, [r, c]);
  const E = Re(() => {
    a("close");
  }, [a]), R = Re((b) => {
    a("selectItem", { itemId: b });
  }, [a]);
  Le(() => {
    if (!r) return;
    const b = (f) => {
      o.current && !o.current.contains(f.target) && E();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [r, E]);
  const h = Re((b) => {
    if (b.key === "Escape") {
      E();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), m((f) => (f + 1) % _.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), m((f) => (f - 1 + _.length) % _.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const f = _[n];
      f && R(f.id);
    }
  }, [E, R, _, n]);
  return Le(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: h
    },
    i.map((b, f) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const N = _.indexOf(b) === n;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (N ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: N ? 0 : -1,
          onClick: () => R(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, Cn = ({ controlId: l }) => {
  const t = W(), a = t.header, r = t.content, c = t.footer, i = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: r })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: c })), /* @__PURE__ */ e.createElement(K, { control: i }));
}, yn = () => {
  const t = W().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, wn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ke = 50, kn = () => {
  const l = W(), t = ne(), a = ce(wn), r = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, d = l.selectionMode ?? "single", s = l.selectedCount ?? 0, n = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, _ = e.useMemo(
    () => r.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [r]
  ), E = d === "multi", R = 40, h = 20, b = e.useRef(null), f = e.useRef(null), S = e.useRef(null), [N, O] = e.useState({}), P = e.useRef(null), p = e.useRef(!1), g = e.useRef(null), [C, q] = e.useState(null), [k, T] = e.useState(null);
  e.useEffect(() => {
    P.current || O({});
  }, [r]);
  const j = e.useCallback((w) => N[w.name] ?? w.width, [N]), I = e.useMemo(() => {
    const w = [];
    let L = E && n > 0 ? R : 0;
    for (let z = 0; z < n && z < r.length; z++)
      w.push(L), L += j(r[z]);
    return w;
  }, [r, n, E, R, j]), Y = c * o, te = e.useCallback((w, L, z) => {
    z.preventDefault(), z.stopPropagation(), P.current = { column: w, startX: z.clientX, startWidth: L };
    const Q = (y) => {
      const D = P.current;
      if (!D) return;
      const A = Math.max(Ke, D.startWidth + (y.clientX - D.startX));
      O((Z) => ({ ...Z, [D.column]: A }));
    }, ee = (y) => {
      document.removeEventListener("mousemove", Q), document.removeEventListener("mouseup", ee);
      const D = P.current;
      if (D) {
        const A = Math.max(Ke, D.startWidth + (y.clientX - D.startX));
        t("columnResize", { column: D.column, width: A }), P.current = null, p.current = !0, requestAnimationFrame(() => {
          p.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Q), document.addEventListener("mouseup", ee);
  }, [t]), H = e.useCallback(() => {
    b.current && f.current && (b.current.scrollLeft = f.current.scrollLeft), S.current !== null && clearTimeout(S.current), S.current = window.setTimeout(() => {
      const w = f.current;
      if (!w) return;
      const L = w.scrollTop, z = Math.ceil(w.clientHeight / o), Q = Math.floor(L / o);
      t("scroll", { start: Q, count: z });
    }, 80);
  }, [t, o]), $ = e.useCallback((w, L, z) => {
    if (p.current) return;
    let Q;
    !L || L === "desc" ? Q = "asc" : Q = "desc";
    const ee = z.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: Q, mode: ee });
  }, [t]), X = e.useCallback((w, L) => {
    g.current = w, L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", w);
  }, []), J = e.useCallback((w, L) => {
    if (!g.current || g.current === w) {
      q(null);
      return;
    }
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const z = L.currentTarget.getBoundingClientRect(), Q = L.clientX < z.left + z.width / 2 ? "left" : "right";
    q({ column: w, side: Q });
  }, []), re = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const L = g.current;
    if (!L || !C) {
      g.current = null, q(null);
      return;
    }
    let z = r.findIndex((ee) => ee.name === C.column);
    if (z < 0) {
      g.current = null, q(null);
      return;
    }
    const Q = r.findIndex((ee) => ee.name === L);
    C.side === "right" && z++, Q < z && z--, t("columnReorder", { column: L, targetIndex: z }), g.current = null, q(null);
  }, [r, C, t]), ue = e.useCallback(() => {
    g.current = null, q(null);
  }, []), u = e.useCallback((w, L) => {
    L.shiftKey && L.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: L.ctrlKey || L.metaKey,
      shiftKey: L.shiftKey
    });
  }, [t]), v = e.useCallback((w, L) => {
    L.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), M = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), x = e.useCallback((w, L, z) => {
    z.stopPropagation(), t("expand", { rowIndex: w, expanded: L });
  }, [t]), U = e.useCallback((w, L) => {
    L.preventDefault(), T({ x: L.clientX, y: L.clientY, colIdx: w });
  }, []), V = e.useCallback(() => {
    k && (t("setFrozenColumnCount", { count: k.colIdx + 1 }), T(null));
  }, [k, t]), G = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), T(null);
  }, [t]);
  e.useEffect(() => {
    if (!k) return;
    const w = () => T(null), L = (z) => {
      z.key === "Escape" && T(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", L), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", L);
    };
  }, [k]);
  const ae = r.reduce((w, L) => w + j(L), 0) + (E ? R : 0), le = s === c && c > 0, pe = s > 0 && s < c, Ne = e.useCallback((w) => {
    w && (w.indeterminate = pe);
  }, [pe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!g.current) return;
        w.preventDefault();
        const L = f.current, z = b.current;
        if (!L) return;
        const Q = L.getBoundingClientRect(), ee = 40, y = 8;
        w.clientX < Q.left + ee ? L.scrollLeft = Math.max(0, L.scrollLeft - y) : w.clientX > Q.right - ee && (L.scrollLeft += y), z && (z.scrollLeft = L.scrollLeft);
      },
      onDrop: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ae } }, E && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: R,
          minWidth: R,
          ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", r.length > 0 && r[0].name !== g.current && q({ column: r[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ne,
          className: "tlTableView__checkbox",
          checked: le,
          onChange: M
        }
      )
    ), r.map((w, L) => {
      const z = j(w), Q = L === r.length - 1;
      let ee = "tlTableView__headerCell";
      w.sortable && (ee += " tlTableView__headerCell--sortable"), C && C.column === w.name && (ee += " tlTableView__headerCell--dragOver-" + C.side);
      const y = L < n, D = L === n - 1;
      return y && (ee += " tlTableView__headerCell--frozen"), D && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ee,
          style: {
            ...Q && !y ? { flex: "1 0 auto", minWidth: z } : { width: z, minWidth: z },
            position: y ? "sticky" : "relative",
            ...y ? { left: I[L], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (A) => $(w.name, w.sortDirection, A) : void 0,
          onContextMenu: (A) => U(L, A),
          onDragStart: (A) => X(w.name, A),
          onDragOver: (A) => J(w.name, A),
          onDrop: re,
          onDragEnd: ue
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", _ > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (A) => te(w.name, z, A)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (g.current && r.length > 0) {
            const L = r[r.length - 1];
            L.name !== g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", q({ column: L.name, side: "right" }));
          }
        },
        onDrop: re
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: H
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: Y, position: "relative", minWidth: ae } }, i.map((w) => /* @__PURE__ */ e.createElement(
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
          onClick: (L) => u(w.index, L)
        },
        E && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: R,
              minWidth: R,
              ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (L) => L.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (L) => v(w.index, L),
              tabIndex: -1
            }
          )
        ),
        r.map((L, z) => {
          const Q = j(L), ee = z === r.length - 1, y = z < n, D = z === n - 1;
          let A = "tlTableView__cell";
          y && (A += " tlTableView__cell--frozen"), D && (A += " tlTableView__cell--frozenLast");
          const Z = m && z === 0, be = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: L.name,
              className: A,
              style: {
                ...ee && !y ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...y ? { position: "sticky", left: I[z], zIndex: 2 } : {}
              }
            },
            Z ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: be * h } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (rt) => x(w.index, !w.expanded, rt)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: w.cells[L.name] })) : /* @__PURE__ */ e.createElement(K, { control: w.cells[L.name] })
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
      k.colIdx + 1 !== n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: V }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      n > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: G }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Nn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Je = e.createContext(Nn), { useMemo: Sn, useRef: Tn, useState: Rn, useEffect: Ln } = e, Dn = 320, xn = ({ controlId: l }) => {
  const t = W(), a = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, d = Tn(null), [s, n] = Rn(
    r === "top" ? "top" : "side"
  );
  Ln(() => {
    if (r !== "auto") {
      n(r);
      return;
    }
    const h = d.current;
    if (!h) return;
    const b = new ResizeObserver((f) => {
      for (const S of f) {
        const O = S.contentRect.width / a;
        n(O < Dn ? "top" : "side");
      }
    });
    return b.observe(h), () => b.disconnect();
  }, [r, a]);
  const m = Sn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), E = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, R = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Je.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: R, style: E, ref: d }, i.map((h, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: h }))));
}, { useCallback: In } = e, Pn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, jn = ({ controlId: l }) => {
  const t = W(), a = ne(), r = ce(Pn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", n = t.fullLine === !0, m = t.children ?? [], _ = c != null || i.length > 0 || o, E = In(() => {
    a("toggleCollapse");
  }, [a]), R = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    n ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: R }, _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: E,
      "aria-expanded": !d,
      title: d ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((h, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((h, b) => /* @__PURE__ */ e.createElement(K, { key: b, control: h }))));
}, { useContext: Mn, useState: An, useCallback: On } = e, Bn = ({ controlId: l }) => {
  const t = W(), a = Mn(Je), r = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? a.resolvedLabelPosition, n = t.fullLine === !0, m = t.visible !== !1, _ = t.field, E = a.readOnly, [R, h] = An(!1), b = On(() => h((N) => !N), []);
  if (!m) return null;
  const f = i != null, S = [
    "tlFormField",
    `tlFormField--${s}`,
    E ? "tlFormField--readonly" : "",
    n ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: S }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), c && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !E && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: _ })), !E && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !E && o && R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, $n = () => {
  const l = W(), t = ne(), a = l.iconCss, r = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, d = l.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, n = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((E) => {
    E.preventDefault(), t("goto", {});
  }, [t]), _ = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: _, href: "#", onClick: m, title: o }, n) : /* @__PURE__ */ e.createElement("span", { className: _, title: o }, n);
}, Fn = 20, zn = () => {
  const l = W(), t = ne(), a = l.nodes ?? [], r = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [s, n] = e.useState(-1), m = e.useRef(null), _ = e.useCallback((p, g) => {
    t(g ? "collapse" : "expand", { nodeId: p });
  }, [t]), E = e.useCallback((p, g) => {
    t("select", {
      nodeId: p,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), R = e.useCallback((p, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: p, x: g.clientX, y: g.clientY });
  }, [t]), h = e.useRef(null), b = e.useCallback((p, g) => {
    const C = g.getBoundingClientRect(), q = p.clientY - C.top, k = C.height / 3;
    return q < k ? "above" : q > k * 2 ? "below" : "within";
  }, []), f = e.useCallback((p, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", p);
  }, []), S = e.useCallback((p, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const C = b(g, g.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: p, position: C }), h.current = null;
    }, 50);
  }, [t, b]), N = e.useCallback((p, g) => {
    g.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const C = b(g, g.currentTarget);
    t("drop", { nodeId: p, position: C });
  }, [t, b]), O = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((p) => {
    if (a.length === 0) return;
    let g = s;
    switch (p.key) {
      case "ArrowDown":
        p.preventDefault(), g = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        p.preventDefault(), g = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (p.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (p.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const q = C.depth;
            for (let k = s - 1; k >= 0; k--)
              if (a[k].depth < q) {
                g = k;
                break;
              }
          }
        }
        break;
      case "Enter":
        p.preventDefault(), s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: p.ctrlKey || p.metaKey,
          shiftKey: p.shiftKey
        });
        return;
      case " ":
        p.preventDefault(), r === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        p.preventDefault(), g = 0;
        break;
      case "End":
        p.preventDefault(), g = a.length - 1;
        break;
      default:
        return;
    }
    g !== s && n(g);
  }, [s, a, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    a.map((p, g) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: p.id,
        role: "treeitem",
        "aria-expanded": p.expandable ? p.expanded : void 0,
        "aria-selected": p.selected,
        "aria-level": p.depth + 1,
        className: [
          "tlTreeView__node",
          p.selected ? "tlTreeView__node--selected" : "",
          g === s ? "tlTreeView__node--focused" : "",
          o === p.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === p.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === p.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: p.depth * Fn },
        draggable: c,
        onClick: (C) => E(p.id, C),
        onContextMenu: (C) => R(p.id, C),
        onDragStart: (C) => f(p.id, C),
        onDragOver: i ? (C) => S(p.id, C) : void 0,
        onDrop: i ? (C) => N(p.id, C) : void 0,
        onDragEnd: O
      },
      p.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), _(p.id, p.expanded);
          },
          tabIndex: -1,
          "aria-label": p.expanded ? "Collapse" : "Expand"
        },
        p.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: p.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: p.content }))
    ))
  );
};
var De = { exports: {} }, oe = {}, xe = { exports: {} }, B = {};
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
function Hn() {
  if (Ye) return B;
  Ye = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), n = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), _ = Symbol.for("react.activity"), E = Symbol.iterator;
  function R(u) {
    return u === null || typeof u != "object" ? null : (u = E && u[E] || u["@@iterator"], typeof u == "function" ? u : null);
  }
  var h = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, b = Object.assign, f = {};
  function S(u, v, M) {
    this.props = u, this.context = v, this.refs = f, this.updater = M || h;
  }
  S.prototype.isReactComponent = {}, S.prototype.setState = function(u, v) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, v, "setState");
  }, S.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function N() {
  }
  N.prototype = S.prototype;
  function O(u, v, M) {
    this.props = u, this.context = v, this.refs = f, this.updater = M || h;
  }
  var P = O.prototype = new N();
  P.constructor = O, b(P, S.prototype), P.isPureReactComponent = !0;
  var p = Array.isArray;
  function g() {
  }
  var C = { H: null, A: null, T: null, S: null }, q = Object.prototype.hasOwnProperty;
  function k(u, v, M) {
    var x = M.ref;
    return {
      $$typeof: l,
      type: u,
      key: v,
      ref: x !== void 0 ? x : null,
      props: M
    };
  }
  function T(u, v) {
    return k(u.type, v, u.props);
  }
  function j(u) {
    return typeof u == "object" && u !== null && u.$$typeof === l;
  }
  function I(u) {
    var v = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function(M) {
      return v[M];
    });
  }
  var Y = /\/+/g;
  function te(u, v) {
    return typeof u == "object" && u !== null && u.key != null ? I("" + u.key) : v.toString(36);
  }
  function H(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(g, g) : (u.status = "pending", u.then(
          function(v) {
            u.status === "pending" && (u.status = "fulfilled", u.value = v);
          },
          function(v) {
            u.status === "pending" && (u.status = "rejected", u.reason = v);
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
  function $(u, v, M, x, U) {
    var V = typeof u;
    (V === "undefined" || V === "boolean") && (u = null);
    var G = !1;
    if (u === null) G = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          G = !0;
          break;
        case "object":
          switch (u.$$typeof) {
            case l:
            case t:
              G = !0;
              break;
            case m:
              return G = u._init, $(
                G(u._payload),
                v,
                M,
                x,
                U
              );
          }
      }
    if (G)
      return U = U(u), G = x === "" ? "." + te(u, 0) : x, p(U) ? (M = "", G != null && (M = G.replace(Y, "$&/") + "/"), $(U, v, M, "", function(pe) {
        return pe;
      })) : U != null && (j(U) && (U = T(
        U,
        M + (U.key == null || u && u.key === U.key ? "" : ("" + U.key).replace(
          Y,
          "$&/"
        ) + "/") + G
      )), v.push(U)), 1;
    G = 0;
    var ae = x === "" ? "." : x + ":";
    if (p(u))
      for (var le = 0; le < u.length; le++)
        x = u[le], V = ae + te(x, le), G += $(
          x,
          v,
          M,
          V,
          U
        );
    else if (le = R(u), typeof le == "function")
      for (u = le.call(u), le = 0; !(x = u.next()).done; )
        x = x.value, V = ae + te(x, le++), G += $(
          x,
          v,
          M,
          V,
          U
        );
    else if (V === "object") {
      if (typeof u.then == "function")
        return $(
          H(u),
          v,
          M,
          x,
          U
        );
      throw v = String(u), Error(
        "Objects are not valid as a React child (found: " + (v === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : v) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return G;
  }
  function X(u, v, M) {
    if (u == null) return u;
    var x = [], U = 0;
    return $(u, x, "", "", function(V) {
      return v.call(M, V, U++);
    }), x;
  }
  function J(u) {
    if (u._status === -1) {
      var v = u._result;
      v = v(), v.then(
        function(M) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = M);
        },
        function(M) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = M);
        }
      ), u._status === -1 && (u._status = 0, u._result = v);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var re = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var v = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(v)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, ue = {
    map: X,
    forEach: function(u, v, M) {
      X(
        u,
        function() {
          v.apply(this, arguments);
        },
        M
      );
    },
    count: function(u) {
      var v = 0;
      return X(u, function() {
        v++;
      }), v;
    },
    toArray: function(u) {
      return X(u, function(v) {
        return v;
      }) || [];
    },
    only: function(u) {
      if (!j(u))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return u;
    }
  };
  return B.Activity = _, B.Children = ue, B.Component = S, B.Fragment = a, B.Profiler = c, B.PureComponent = O, B.StrictMode = r, B.Suspense = s, B.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, B.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return C.H.useMemoCache(u);
    }
  }, B.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, B.cacheSignal = function() {
    return null;
  }, B.cloneElement = function(u, v, M) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var x = b({}, u.props), U = u.key;
    if (v != null)
      for (V in v.key !== void 0 && (U = "" + v.key), v)
        !q.call(v, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && v.ref === void 0 || (x[V] = v[V]);
    var V = arguments.length - 2;
    if (V === 1) x.children = M;
    else if (1 < V) {
      for (var G = Array(V), ae = 0; ae < V; ae++)
        G[ae] = arguments[ae + 2];
      x.children = G;
    }
    return k(u.type, U, x);
  }, B.createContext = function(u) {
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
  }, B.createElement = function(u, v, M) {
    var x, U = {}, V = null;
    if (v != null)
      for (x in v.key !== void 0 && (V = "" + v.key), v)
        q.call(v, x) && x !== "key" && x !== "__self" && x !== "__source" && (U[x] = v[x]);
    var G = arguments.length - 2;
    if (G === 1) U.children = M;
    else if (1 < G) {
      for (var ae = Array(G), le = 0; le < G; le++)
        ae[le] = arguments[le + 2];
      U.children = ae;
    }
    if (u && u.defaultProps)
      for (x in G = u.defaultProps, G)
        U[x] === void 0 && (U[x] = G[x]);
    return k(u, V, U);
  }, B.createRef = function() {
    return { current: null };
  }, B.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, B.isValidElement = j, B.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: J
    };
  }, B.memo = function(u, v) {
    return {
      $$typeof: n,
      type: u,
      compare: v === void 0 ? null : v
    };
  }, B.startTransition = function(u) {
    var v = C.T, M = {};
    C.T = M;
    try {
      var x = u(), U = C.S;
      U !== null && U(M, x), typeof x == "object" && x !== null && typeof x.then == "function" && x.then(g, re);
    } catch (V) {
      re(V);
    } finally {
      v !== null && M.types !== null && (v.types = M.types), C.T = v;
    }
  }, B.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, B.use = function(u) {
    return C.H.use(u);
  }, B.useActionState = function(u, v, M) {
    return C.H.useActionState(u, v, M);
  }, B.useCallback = function(u, v) {
    return C.H.useCallback(u, v);
  }, B.useContext = function(u) {
    return C.H.useContext(u);
  }, B.useDebugValue = function() {
  }, B.useDeferredValue = function(u, v) {
    return C.H.useDeferredValue(u, v);
  }, B.useEffect = function(u, v) {
    return C.H.useEffect(u, v);
  }, B.useEffectEvent = function(u) {
    return C.H.useEffectEvent(u);
  }, B.useId = function() {
    return C.H.useId();
  }, B.useImperativeHandle = function(u, v, M) {
    return C.H.useImperativeHandle(u, v, M);
  }, B.useInsertionEffect = function(u, v) {
    return C.H.useInsertionEffect(u, v);
  }, B.useLayoutEffect = function(u, v) {
    return C.H.useLayoutEffect(u, v);
  }, B.useMemo = function(u, v) {
    return C.H.useMemo(u, v);
  }, B.useOptimistic = function(u, v) {
    return C.H.useOptimistic(u, v);
  }, B.useReducer = function(u, v, M) {
    return C.H.useReducer(u, v, M);
  }, B.useRef = function(u) {
    return C.H.useRef(u);
  }, B.useState = function(u) {
    return C.H.useState(u);
  }, B.useSyncExternalStore = function(u, v, M) {
    return C.H.useSyncExternalStore(
      u,
      v,
      M
    );
  }, B.useTransition = function() {
    return C.H.useTransition();
  }, B.version = "19.2.4", B;
}
var Ge;
function Un() {
  return Ge || (Ge = 1, xe.exports = Hn()), xe.exports;
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
function Vn() {
  if (qe) return oe;
  qe = 1;
  var l = Un();
  function t(s) {
    var n = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      n += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        n += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + n + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function a() {
  }
  var r = {
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
  function i(s, n, m) {
    var _ = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: _ == null ? null : "" + _,
      children: s,
      containerInfo: n,
      implementation: m
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(s, n) {
    if (s === "font") return "";
    if (typeof n == "string")
      return n === "use-credentials" ? n : "";
  }
  return oe.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, oe.createPortal = function(s, n) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!n || n.nodeType !== 1 && n.nodeType !== 9 && n.nodeType !== 11)
      throw Error(t(299));
    return i(s, n, null, m);
  }, oe.flushSync = function(s) {
    var n = o.T, m = r.p;
    try {
      if (o.T = null, r.p = 2, s) return s();
    } finally {
      o.T = n, r.p = m, r.d.f();
    }
  }, oe.preconnect = function(s, n) {
    typeof s == "string" && (n ? (n = n.crossOrigin, n = typeof n == "string" ? n === "use-credentials" ? n : "" : void 0) : n = null, r.d.C(s, n));
  }, oe.prefetchDNS = function(s) {
    typeof s == "string" && r.d.D(s);
  }, oe.preinit = function(s, n) {
    if (typeof s == "string" && n && typeof n.as == "string") {
      var m = n.as, _ = d(m, n.crossOrigin), E = typeof n.integrity == "string" ? n.integrity : void 0, R = typeof n.fetchPriority == "string" ? n.fetchPriority : void 0;
      m === "style" ? r.d.S(
        s,
        typeof n.precedence == "string" ? n.precedence : void 0,
        {
          crossOrigin: _,
          integrity: E,
          fetchPriority: R
        }
      ) : m === "script" && r.d.X(s, {
        crossOrigin: _,
        integrity: E,
        fetchPriority: R,
        nonce: typeof n.nonce == "string" ? n.nonce : void 0
      });
    }
  }, oe.preinitModule = function(s, n) {
    if (typeof s == "string")
      if (typeof n == "object" && n !== null) {
        if (n.as == null || n.as === "script") {
          var m = d(
            n.as,
            n.crossOrigin
          );
          r.d.M(s, {
            crossOrigin: m,
            integrity: typeof n.integrity == "string" ? n.integrity : void 0,
            nonce: typeof n.nonce == "string" ? n.nonce : void 0
          });
        }
      } else n == null && r.d.M(s);
  }, oe.preload = function(s, n) {
    if (typeof s == "string" && typeof n == "object" && n !== null && typeof n.as == "string") {
      var m = n.as, _ = d(m, n.crossOrigin);
      r.d.L(s, m, {
        crossOrigin: _,
        integrity: typeof n.integrity == "string" ? n.integrity : void 0,
        nonce: typeof n.nonce == "string" ? n.nonce : void 0,
        type: typeof n.type == "string" ? n.type : void 0,
        fetchPriority: typeof n.fetchPriority == "string" ? n.fetchPriority : void 0,
        referrerPolicy: typeof n.referrerPolicy == "string" ? n.referrerPolicy : void 0,
        imageSrcSet: typeof n.imageSrcSet == "string" ? n.imageSrcSet : void 0,
        imageSizes: typeof n.imageSizes == "string" ? n.imageSizes : void 0,
        media: typeof n.media == "string" ? n.media : void 0
      });
    }
  }, oe.preloadModule = function(s, n) {
    if (typeof s == "string")
      if (n) {
        var m = d(n.as, n.crossOrigin);
        r.d.m(s, {
          as: typeof n.as == "string" && n.as !== "script" ? n.as : void 0,
          crossOrigin: m,
          integrity: typeof n.integrity == "string" ? n.integrity : void 0
        });
      } else r.d.m(s);
  }, oe.requestFormReset = function(s) {
    r.d.r(s);
  }, oe.unstable_batchedUpdates = function(s, n) {
    return s(n);
  }, oe.useFormState = function(s, n, m) {
    return o.H.useFormState(s, n, m);
  }, oe.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, oe.version = "19.2.4", oe;
}
var Xe;
function Wn() {
  if (Xe) return De.exports;
  Xe = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), De.exports = Vn(), De.exports;
}
var Kn = Wn();
const { useState: de, useCallback: se, useRef: ve, useEffect: fe, useMemo: Ae } = e;
function Fe({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Yn({
  option: l,
  removable: t,
  onRemove: a,
  removeLabel: r,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: d,
  onDragEnd: s,
  dragClassName: n
}) {
  const m = se(
    (_) => {
      _.stopPropagation(), a(l.value);
    },
    [a, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (n ? " " + n : ""),
      draggable: c || void 0,
      onDragStart: i,
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
        "aria-label": r
      },
      "×"
    )
  );
}
function Gn({
  option: l,
  highlighted: t,
  searchTerm: a,
  onSelect: r,
  onMouseEnter: c,
  id: i
}) {
  const o = se(() => r(l.value), [r, l.value]), d = Ae(() => {
    if (!a) return l.label;
    const s = l.label.toLowerCase().indexOf(a.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + a.length)), l.label.substring(s + a.length));
  }, [l.label, a]);
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
    /* @__PURE__ */ e.createElement(Fe, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const qn = ({ controlId: l, state: t }) => {
  const a = ne(), r = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, n = t.optionsLoaded === !0, m = t.options ?? [], _ = t.emptyOptionLabel ?? "", E = i && c && !d && s, R = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = R["js.dropdownSelect.nothingFound"], b = se(
    (y) => R["js.dropdownSelect.removeChip"].replace("{0}", y),
    [R]
  ), [f, S] = de(!1), [N, O] = de(""), [P, p] = de(-1), [g, C] = de(!1), [q, k] = de({}), [T, j] = de(null), [I, Y] = de(null), [te, H] = de(null), $ = ve(null), X = ve(null), J = ve(null), re = ve(r);
  re.current = r;
  const ue = ve(-1), u = Ae(
    () => new Set(r.map((y) => y.value)),
    [r]
  ), v = Ae(() => {
    let y = m.filter((D) => !u.has(D.value));
    if (N) {
      const D = N.toLowerCase();
      y = y.filter((A) => A.label.toLowerCase().includes(D));
    }
    return y;
  }, [m, u, N]);
  fe(() => {
    N && v.length === 1 ? p(0) : p(-1);
  }, [v.length, N]), fe(() => {
    f && n && X.current && X.current.focus();
  }, [f, n, r]), fe(() => {
    var A, Z;
    if (ue.current < 0) return;
    const y = ue.current;
    ue.current = -1;
    const D = (A = $.current) == null ? void 0 : A.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    D && D.length > 0 ? D[Math.min(y, D.length - 1)].focus() : (Z = $.current) == null || Z.focus();
  }, [r]), fe(() => {
    if (!f) return;
    const y = (D) => {
      $.current && !$.current.contains(D.target) && J.current && !J.current.contains(D.target) && (S(!1), O(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [f]), fe(() => {
    if (!f || !$.current) return;
    const y = $.current.getBoundingClientRect(), D = window.innerHeight - y.bottom, Z = D < 300 && y.top > D;
    k({
      left: y.left,
      width: y.width,
      ...Z ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [f]);
  const M = se(async () => {
    if (!(d || !s) && (S(!0), O(""), p(-1), C(!1), !n))
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
  }, [d, s, n, a]), x = se(() => {
    var y;
    S(!1), O(""), p(-1), (y = $.current) == null || y.focus();
  }, []), U = se(
    (y) => {
      let D;
      if (c) {
        const A = m.find((Z) => Z.value === y);
        if (A)
          D = [...re.current, A];
        else
          return;
      } else {
        const A = m.find((Z) => Z.value === y);
        if (A)
          D = [A];
        else
          return;
      }
      re.current = D, a("valueChanged", { value: D.map((A) => A.value) }), c ? (O(""), p(-1)) : x();
    },
    [c, m, a, x]
  ), V = se(
    (y) => {
      ue.current = re.current.findIndex((A) => A.value === y);
      const D = re.current.filter((A) => A.value !== y);
      re.current = D, a("valueChanged", { value: D.map((A) => A.value) });
    },
    [a]
  ), G = se(
    (y) => {
      y.stopPropagation(), a("valueChanged", { value: [] }), x();
    },
    [a, x]
  ), ae = se((y) => {
    O(y.target.value);
  }, []), le = se(
    (y) => {
      if (!f) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), M();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), p(
            (D) => D < v.length - 1 ? D + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), p(
            (D) => D > 0 ? D - 1 : v.length - 1
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
          N === "" && c && r.length > 0 && V(r[r.length - 1].value);
          break;
      }
    },
    [
      f,
      M,
      x,
      v,
      P,
      U,
      N,
      c,
      r,
      V
    ]
  ), pe = se(
    async (y) => {
      y.preventDefault(), C(!1);
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
    },
    [a]
  ), Ne = se(
    (y, D) => {
      j(y), D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), w = se(
    (y, D) => {
      if (D.preventDefault(), D.dataTransfer.dropEffect = "move", T === null || T === y) {
        Y(null), H(null);
        return;
      }
      const A = D.currentTarget.getBoundingClientRect(), Z = A.left + A.width / 2, be = D.clientX < Z ? "before" : "after";
      Y(y), H(be);
    },
    [T]
  ), L = se(
    (y) => {
      if (y.preventDefault(), T === null || I === null || te === null || T === I) return;
      const D = [...re.current], [A] = D.splice(T, 1);
      let Z = I;
      T < I ? Z = te === "before" ? Z - 1 : Z : Z = te === "before" ? Z : Z + 1, D.splice(Z, 0, A), re.current = D, a("valueChanged", { value: D.map((be) => be.value) }), j(null), Y(null), H(null);
    },
    [T, I, te, a]
  ), z = se(() => {
    j(null), Y(null), H(null);
  }, []);
  if (fe(() => {
    if (P < 0 || !J.current) return;
    const y = J.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, _) : r.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Fe, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const Q = !o && r.length > 0 && !d, ee = f ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: J,
      className: "tlDropdownSelect__dropdown",
      style: q
    },
    (n || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: X,
        type: "text",
        className: "tlDropdownSelect__search",
        value: N,
        onChange: ae,
        onKeyDown: le,
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
      !n && !g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, R["js.dropdownSelect.error"])),
      n && v.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      n && v.map((y, D) => /* @__PURE__ */ e.createElement(
        Gn,
        {
          key: y.value,
          id: `${l}-opt-${D}`,
          option: y,
          highlighted: D === P,
          searchTerm: N,
          onSelect: U,
          onMouseEnter: () => p(D)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: $,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${l}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: f ? void 0 : M,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, _) : r.map((y, D) => {
      let A = "";
      return T === D ? A = "tlDropdownSelect__chip--dragging" : I === D && te === "before" ? A = "tlDropdownSelect__chip--dropBefore" : I === D && te === "after" && (A = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Yn,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: V,
          removeLabel: b(y.label),
          draggable: E,
          onDragStart: E ? (Z) => Ne(D, Z) : void 0,
          onDragOver: E ? (Z) => w(D, Z) : void 0,
          onDrop: E ? L : void 0,
          onDragEnd: E ? z : void 0,
          dragClassName: E ? A : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Q && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: G,
        "aria-label": R["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), ee && Kn.createPortal(ee, document.body));
}, { useCallback: Ie, useRef: Xn } = e, Zn = ({
  colors: l,
  columns: t,
  onSelect: a,
  onConfirm: r,
  onSwap: c
}) => {
  const i = Xn(null), o = Ie(
    (n) => (m) => {
      i.current = n, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), d = Ie((n) => {
    n.preventDefault(), n.dataTransfer.dropEffect = "move";
  }, []), s = Ie(
    (n) => (m) => {
      m.preventDefault(), i.current !== null && i.current !== n && c(i.current, n), i.current = null;
    },
    [c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((n, m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m,
        className: "tlColorInput__paletteCell" + (n == null ? " tlColorInput__paletteCell--empty" : ""),
        style: n != null ? { backgroundColor: n } : void 0,
        title: n ?? "",
        draggable: n != null,
        onClick: n != null ? () => a(n) : void 0,
        onDoubleClick: n != null ? () => r(n) : void 0,
        onDragStart: n != null ? o(m) : void 0,
        onDragOver: d,
        onDrop: s(m)
      }
    ))
  );
};
function et(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Oe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function tt(l) {
  if (!Oe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function nt(l, t, a) {
  const r = (c) => et(c).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(a);
}
function Qn(l, t, a) {
  const r = l / 255, c = t / 255, i = a / 255, o = Math.max(r, c, i), d = Math.min(r, c, i), s = o - d;
  let n = 0;
  s !== 0 && (o === r ? n = (c - i) / s % 6 : o === c ? n = (i - r) / s + 2 : n = (r - c) / s + 4, n *= 60, n < 0 && (n += 360));
  const m = o === 0 ? 0 : s / o;
  return [n, m, o];
}
function Jn(l, t, a) {
  const r = a * t, c = r * (1 - Math.abs(l / 60 % 2 - 1)), i = a - r;
  let o = 0, d = 0, s = 0;
  return l < 60 ? (o = r, d = c, s = 0) : l < 120 ? (o = c, d = r, s = 0) : l < 180 ? (o = 0, d = r, s = c) : l < 240 ? (o = 0, d = c, s = r) : l < 300 ? (o = c, d = 0, s = r) : (o = r, d = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((d + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function er(l) {
  return Qn(...tt(l));
}
function Pe(l, t, a) {
  return nt(...Jn(l, t, a));
}
const { useCallback: he, useRef: Ze } = e, tr = ({ color: l, onColorChange: t }) => {
  const [a, r, c] = er(l), i = Ze(null), o = Ze(null), d = he(
    (h, b) => {
      var O;
      const f = (O = i.current) == null ? void 0 : O.getBoundingClientRect();
      if (!f) return;
      const S = Math.max(0, Math.min(1, (h - f.left) / f.width)), N = Math.max(0, Math.min(1, 1 - (b - f.top) / f.height));
      t(Pe(a, S, N));
    },
    [a, t]
  ), s = he(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), d(h.clientX, h.clientY);
    },
    [d]
  ), n = he(
    (h) => {
      h.buttons !== 0 && d(h.clientX, h.clientY);
    },
    [d]
  ), m = he(
    (h) => {
      var N;
      const b = (N = o.current) == null ? void 0 : N.getBoundingClientRect();
      if (!b) return;
      const S = Math.max(0, Math.min(1, (h - b.top) / b.height)) * 360;
      t(Pe(S, r, c));
    },
    [r, c, t]
  ), _ = he(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), m(h.clientY);
    },
    [m]
  ), E = he(
    (h) => {
      h.buttons !== 0 && m(h.clientY);
    },
    [m]
  ), R = Pe(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: R },
      onPointerDown: s,
      onPointerMove: n
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${r * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: _,
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
}, nr = {
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
}, { useState: ge, useCallback: me, useEffect: je, useRef: rr, useLayoutEffect: lr } = e, ar = ({
  anchorRef: l,
  currentColor: t,
  palette: a,
  paletteColumns: r,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: d,
  onPaletteChange: s
}) => {
  const [n, m] = ge("palette"), [_, E] = ge(t), R = rr(null), h = ce(nr), [b, f] = ge(null);
  lr(() => {
    if (!l.current || !R.current) return;
    const H = l.current.getBoundingClientRect(), $ = R.current.getBoundingClientRect();
    let X = H.bottom + 4, J = H.left;
    X + $.height > window.innerHeight && (X = H.top - $.height - 4), J + $.width > window.innerWidth && (J = Math.max(0, H.right - $.width)), f({ top: X, left: J });
  }, [l]);
  const S = _ != null, [N, O, P] = S ? tt(_) : [0, 0, 0], [p, g] = ge((_ == null ? void 0 : _.toUpperCase()) ?? "");
  je(() => {
    g((_ == null ? void 0 : _.toUpperCase()) ?? "");
  }, [_]), je(() => {
    const H = ($) => {
      $.key === "Escape" && d();
    };
    return document.addEventListener("keydown", H), () => document.removeEventListener("keydown", H);
  }, [d]), je(() => {
    const H = (X) => {
      R.current && !R.current.contains(X.target) && d();
    }, $ = setTimeout(() => document.addEventListener("mousedown", H), 0);
    return () => {
      clearTimeout($), document.removeEventListener("mousedown", H);
    };
  }, [d]);
  const C = me(
    (H) => ($) => {
      const X = parseInt($.target.value, 10);
      if (isNaN(X)) return;
      const J = et(X);
      E(nt(H === "r" ? J : N, H === "g" ? J : O, H === "b" ? J : P));
    },
    [N, O, P]
  ), q = me((H) => {
    const $ = H.target.value;
    g($), Oe($) && E($);
  }, []), k = me(() => {
    E(null);
  }, []), T = me((H) => {
    E(H);
  }, []), j = me(
    (H) => {
      o(H);
    },
    [o]
  ), I = me(
    (H, $) => {
      const X = [...a], J = X[H];
      X[H] = X[$], X[$] = J, s(X);
    },
    [a, s]
  ), Y = me(() => {
    s([...c]);
  }, [c, s]), te = me(() => {
    o(_);
  }, [_, o]);
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
        className: "tlColorInput__tab" + (n === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (n === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, n === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Zn,
      {
        colors: a,
        columns: r,
        onSelect: T,
        onConfirm: j,
        onSwap: I
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: Y }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(tr, { color: _ ?? "#000000", onColorChange: E }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (S ? "" : " tlColorInput--noColor"),
        style: S ? { backgroundColor: _ } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? N : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? O : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? P : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (p !== "" && !Oe(p) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: p,
        onChange: q
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: k }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: te }, h["js.colorInput.ok"]))
  );
}, or = { "js.colorInput.chooseColor": "Choose color" }, { useState: sr, useCallback: Ce, useRef: cr } = e, ir = ({ controlId: l, state: t }) => {
  const a = ne(), r = ce(or), [c, i] = sr(!1), o = cr(null), d = t.value, s = t.editable !== !1, n = t.palette ?? [], m = t.paletteColumns ?? 6, _ = t.defaultPalette ?? n, E = Ce(() => {
    s && i(!0);
  }, [s]), R = Ce(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), h = Ce(() => {
    i(!1);
  }, []), b = Ce(
    (f) => {
      a("paletteChanged", { palette: f });
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
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    ar,
    {
      anchorRef: o,
      currentColor: d,
      palette: n,
      paletteColumns: m,
      defaultPalette: _,
      canReset: t.canReset !== !1,
      onConfirm: R,
      onCancel: h,
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
F("TLButton", bt);
F("TLToggleButton", Et);
F("TLTextInput", ot);
F("TLNumberInput", ct);
F("TLDatePicker", ut);
F("TLSelect", mt);
F("TLCheckbox", ft);
F("TLTable", ht);
F("TLCounter", gt);
F("TLTabBar", yt);
F("TLFieldList", wt);
F("TLAudioRecorder", Nt);
F("TLAudioPlayer", Tt);
F("TLFileUpload", Lt);
F("TLDownload", xt);
F("TLPhotoCapture", Pt);
F("TLPhotoViewer", Mt);
F("TLSplitPanel", At);
F("TLPanel", Ut);
F("TLMaximizeRoot", Vt);
F("TLDeckPane", Wt);
F("TLSidebar", Jt);
F("TLStack", en);
F("TLGrid", tn);
F("TLCard", nn);
F("TLAppBar", rn);
F("TLBreadcrumb", an);
F("TLBottomBar", sn);
F("TLDialog", dn);
F("TLDrawer", hn);
F("TLSnackbar", vn);
F("TLMenu", gn);
F("TLAppShell", Cn);
F("TLTextCell", yn);
F("TLTableView", kn);
F("TLFormLayout", xn);
F("TLFormGroup", jn);
F("TLFormField", Bn);
F("TLResourceCell", $n);
F("TLTreeView", zn);
F("TLDropdownSelect", qn);
F("TLColorInput", ir);
