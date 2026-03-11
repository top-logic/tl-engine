import { React as e, useTLFieldValue as Ee, getComponent as lt, useTLState as W, useTLCommand as ee, TLChild as K, useTLUpload as Be, useI18N as oe, useTLDataUrl as $e, register as F } from "tl-react-bridge";
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
  const c = W(), i = ee(), o = t ?? "click", d = a ?? c.label, s = r ?? c.disabled === !0, n = _t(() => {
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
  const i = W(), o = ee(), d = t ?? "click", s = a ?? i.label, n = r ?? i.active === !0, m = c ?? i.disabled === !0, E = vt(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: E,
      disabled: m,
      className: "tlReactButton" + (n ? " tlReactButtonActive" : "")
    },
    s
  );
}, gt = ({ controlId: l }) => {
  const t = W(), a = ee(), r = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ct } = e, yt = ({ controlId: l }) => {
  const t = W(), a = ee(), r = t.tabs ?? [], c = t.activeTabId, i = Ct((o) => {
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
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), n = e.useRef(null), m = t.status ?? "idle", E = t.error, b = m === "received" ? "idle" : r !== "idle" ? r : m, k = e.useCallback(async () => {
    if (r === "recording") {
      const S = d.current;
      S && S.state !== "inactive" && S.stop();
      return;
    }
    if (r !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        n.current = S, s.current = [];
        const $ = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(S, $ ? { mimeType: $ } : void 0);
        d.current = P, P.ondataavailable = (p) => {
          p.data.size > 0 && s.current.push(p.data);
        }, P.onstop = async () => {
          S.getTracks().forEach((y) => y.stop()), n.current = null;
          const p = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], p.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", p, "recording.webm"), await a(g), c("idle");
        }, P.start(), c("recording");
      } catch (S) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", S), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [r, a]), v = oe(kt), h = b === "recording" ? v["js.audioRecorder.stop"] : b === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], f = b === "uploading", R = ["tlAudioRecorder__button"];
  return b === "recording" && R.push("tlAudioRecorder__button--recording"), b === "uploading" && R.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: k,
      disabled: f,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[i]), E && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E));
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
        const R = await f.blob();
        s.current = URL.createObjectURL(R);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const h = new Audio(s.current);
    d.current = h, h.onended = () => {
      o("idle");
    }, h.play(), o("playing");
  }, [i, a]), E = oe(St), b = i === "loading" ? E["js.loading"] : i === "playing" ? E["js.audioPlayer.pause"] : i === "disabled" ? E["js.audioPlayer.noAudio"] : E["js.audioPlayer.play"], k = i === "disabled" || i === "loading", v = ["tlAudioPlayer__button"];
  return i === "playing" && v.push("tlAudioPlayer__button--playing"), i === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: m,
      disabled: k,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Rt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Lt = ({ controlId: l }) => {
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", n = t.error, m = t.accept ?? "", E = s === "received" ? "idle" : r !== "idle" ? r : s, b = e.useCallback(async (p) => {
    c("uploading");
    const g = new FormData();
    g.append("file", p, p.name), await a(g), c("idle");
  }, [a]), k = e.useCallback((p) => {
    var y;
    const g = (y = p.target.files) == null ? void 0 : y[0];
    g && b(g);
  }, [b]), v = e.useCallback(() => {
    var p;
    r !== "uploading" && ((p = d.current) == null || p.click());
  }, [r]), h = e.useCallback((p) => {
    p.preventDefault(), p.stopPropagation(), o(!0);
  }, []), f = e.useCallback((p) => {
    p.preventDefault(), p.stopPropagation(), o(!1);
  }, []), R = e.useCallback((p) => {
    var y;
    if (p.preventDefault(), p.stopPropagation(), o(!1), r === "uploading") return;
    const g = (y = p.dataTransfer.files) == null ? void 0 : y[0];
    g && b(g);
  }, [r, b]), S = E === "uploading", $ = oe(Rt), P = E === "uploading" ? $["js.uploading"] : $["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: h,
      onDragLeave: f,
      onDrop: R
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
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
        className: "tlFileUpload__button" + (E === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: S,
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
  const t = W(), a = $e(), r = ee(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, n] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      n(!0);
      try {
        const v = a + (a.includes("?") ? "&" : "?") + "rev=" + i, h = await fetch(v);
        if (!h.ok) {
          console.error("[TLDownload] Failed to fetch data:", h.status);
          return;
        }
        const f = await h.blob(), R = URL.createObjectURL(f), S = document.createElement("a");
        S.href = R, S.download = o, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(R);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        n(!1);
      }
    }
  }, [c, s, a, i, o]), E = e.useCallback(async () => {
    c && await r("clear");
  }, [c, r]), b = oe(Dt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const k = s ? b["js.downloading"] : b["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: E,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
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
  const t = W(), a = Be(), [r, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), n = e.useRef(null), m = e.useRef(null), E = e.useRef(null), b = e.useRef(null), k = e.useRef(null), v = t.error, h = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), n.current && (n.current.srcObject = null);
  }, []), R = e.useCallback(() => {
    f(), c("idle");
  }, [f]), S = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (o(null), !h) {
        (T = b.current) == null || T.click();
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
  }, [r, h]), $ = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = n.current, j = E.current;
    if (!T || !j)
      return;
    j.width = T.videoWidth, j.height = T.videoHeight;
    const L = j.getContext("2d");
    L && (L.drawImage(T, 0, 0), f(), c("uploading"), j.toBlob(async (O) => {
      if (!O) {
        c("idle");
        return;
      }
      const U = new FormData();
      U.append("photo", O, "capture.jpg"), await a(U), c("idle");
    }, "image/jpeg", 0.85));
  }, [r, a, f]), P = e.useCallback(async (T) => {
    var O;
    const j = (O = T.target.files) == null ? void 0 : O[0];
    if (!j) return;
    c("uploading");
    const L = new FormData();
    L.append("photo", j, j.name), await a(L), c("idle"), b.current && (b.current.value = "");
  }, [a]);
  e.useEffect(() => {
    r === "overlayOpen" && n.current && m.current && (n.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var j;
    if (r !== "overlayOpen") return;
    (j = k.current) == null || j.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const T = (j) => {
      j.key === "Escape" && R();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [r, R]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const p = oe(It), g = r === "uploading" ? p["js.uploading"] : p["js.photoCapture.open"], y = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && y.push("tlPhotoCapture__cameraBtn--uploading");
  const G = ["tlPhotoCapture__overlayVideo"];
  d && G.push("tlPhotoCapture__overlayVideo--mirrored");
  const N = ["tlPhotoCapture__mirrorBtn"];
  return d && N.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: S,
      disabled: r === "uploading",
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !h && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: E, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: R }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: n,
        className: G.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: N.join(" "),
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
        onClick: $,
        title: p["js.photoCapture.capture"],
        "aria-label": p["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: R,
        title: p["js.photoCapture.close"],
        "aria-label": p["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, p[i]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
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
        const E = await m.blob();
        n || o(URL.createObjectURL(E));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      n = !0;
    };
  }, [r, c, a]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(jt);
  return !r || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ze, useRef: Se } = e, At = ({ controlId: l }) => {
  const t = W(), a = ee(), r = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = r === "horizontal", d = i.length > 0 && i.every((f) => f.collapsed), s = !d && i.some((f) => f.collapsed), n = d ? !o : o, m = Se(null), E = Se(null), b = Se(null), k = ze((f, R) => {
    const S = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? d && !n ? S.flex = "1 0 0%" : S.flex = "0 0 auto" : R !== void 0 ? S.flex = `0 0 ${R}px` : f.unit === "%" || s ? S.flex = `${f.size} 0 0%` : S.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (S.minWidth = o ? f.minSize : void 0, S.minHeight = o ? void 0 : f.minSize), S;
  }, [o, d, s, n]), v = ze((f, R) => {
    f.preventDefault();
    const S = m.current;
    if (!S) return;
    const $ = i[R], P = i[R + 1], p = S.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    p.forEach((N) => {
      g.push(o ? N.offsetWidth : N.offsetHeight);
    }), b.current = g, E.current = {
      splitterIndex: R,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: g[R],
      startSizeAfter: g[R + 1],
      childBefore: $,
      childAfter: P
    };
    const y = (N) => {
      const T = E.current;
      if (!T || !b.current) return;
      const L = (o ? N.clientX : N.clientY) - T.startPos, O = T.childBefore.minSize || 0, U = T.childAfter.minSize || 0;
      let X = T.startSizeBefore + L, Z = T.startSizeAfter - L;
      X < O && (Z += X - O, X = O), Z < U && (X += Z - U, Z = U), b.current[T.splitterIndex] = X, b.current[T.splitterIndex + 1] = Z;
      const se = S.querySelectorAll(":scope > .tlSplitPanel__child"), ce = se[T.splitterIndex], ne = se[T.splitterIndex + 1];
      ce && (ce.style.flex = `0 0 ${X}px`), ne && (ne.style.flex = `0 0 ${Z}px`);
    }, G = () => {
      if (document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", G), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const N = {};
        i.forEach((T, j) => {
          const L = T.control;
          L != null && L.controlId && b.current && (N[L.controlId] = b.current[j]);
        }), a("updateSizes", { sizes: N });
      }
      b.current = null, E.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", G), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), h = [];
  return i.forEach((f, R) => {
    if (h.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${R}`,
          className: `tlSplitPanel__child${f.collapsed && n ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(f)
        },
        /* @__PURE__ */ e.createElement(K, { control: f.control })
      )
    ), c && R < i.length - 1) {
      const S = i[R + 1];
      !f.collapsed && !S.collapsed && h.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${R}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (P) => v(P, R)
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
    h
  );
}, { useCallback: Te } = e, Ot = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Bt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), $t = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ft = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ht = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ut = ({ controlId: l }) => {
  const t = W(), a = ee(), r = oe(Ot), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, n = t.toolbarButtons ?? [], m = i === "MINIMIZED", E = i === "MAXIMIZED", b = i === "HIDDEN", k = Te(() => {
    a("toggleMinimize");
  }, [a]), v = Te(() => {
    a("toggleMaximize");
  }, [a]), h = Te(() => {
    a("popOut");
  }, [a]);
  if (b)
    return null;
  const f = E ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, n.map((R, S) => /* @__PURE__ */ e.createElement("span", { key: S, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(K, { control: R }))), o && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement($t, null) : /* @__PURE__ */ e.createElement(Bt, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: E ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      E ? /* @__PURE__ */ e.createElement(zt, null) : /* @__PURE__ */ e.createElement(Ft, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
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
  setItemRef: E,
  onItemFocus: b,
  flyoutGroupId: k,
  onOpenFlyout: v,
  onCloseFlyout: h
}) => {
  const f = ke(null), [R, S] = ye(null), $ = ie(() => {
    r ? k === l.id ? h() : (f.current && S(f.current.getBoundingClientRect()), v(l.id)) : o(l.id);
  }, [r, k, l.id, o, v, h]), P = ie((g) => {
    f.current = g, s(g);
  }, [s]), p = r && k === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (p ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: $,
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
      anchorRect: R,
      onSelect: c,
      onExecute: i,
      onClose: h
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
      setItemRef: E,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: v,
      onCloseFlyout: h
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
  onOpenFlyout: E,
  onCloseFlyout: b
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
      const k = n ? n.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Qt,
        {
          item: l,
          expanded: k,
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
          onOpenFlyout: E,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, Jt = ({ controlId: l }) => {
  const t = W(), a = ee(), r = oe(Kt), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = ye(() => {
    const N = /* @__PURE__ */ new Map(), T = (j) => {
      for (const L of j)
        L.type === "group" && (N.set(L.id, L.expanded), T(L.children));
    };
    return T(c), N;
  }), n = ie((N) => {
    s((T) => {
      const j = new Map(T), L = j.get(N) ?? !1;
      return j.set(N, !L), a("toggleGroup", { itemId: N, expanded: !L }), j;
    });
  }, [a]), m = ie((N) => {
    N !== i && a("selectItem", { itemId: N });
  }, [a, i]), E = ie((N) => {
    a("executeCommand", { itemId: N });
  }, [a]), b = ie(() => {
    a("toggleCollapse", {});
  }, [a]), [k, v] = ye(null), h = ie((N) => {
    v(N);
  }, []), f = ie(() => {
    v(null);
  }, []);
  we(() => {
    o || v(null);
  }, [o]);
  const [R, S] = ye(() => {
    const N = Me(c, o, d);
    return N.length > 0 ? N[0].id : "";
  }), $ = ke(/* @__PURE__ */ new Map()), P = ie((N) => (T) => {
    T ? $.current.set(N, T) : $.current.delete(N);
  }, []), p = ie((N) => {
    S(N);
  }, []), g = ke(0), y = ie((N) => {
    S(N), g.current++;
  }, []);
  we(() => {
    const N = $.current.get(R);
    N && document.activeElement !== N && N.focus();
  }, [R, g.current]);
  const G = ie((N) => {
    if (N.key === "Escape" && k !== null) {
      N.preventDefault(), f();
      return;
    }
    const T = Me(c, o, d);
    if (T.length === 0) return;
    const j = T.findIndex((O) => O.id === R);
    if (j < 0) return;
    const L = T[j];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const O = (j + 1) % T.length;
        y(T[O].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const O = (j - 1 + T.length) % T.length;
        y(T[O].id);
        break;
      }
      case "Home": {
        N.preventDefault(), y(T[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), y(T[T.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), L.type === "nav" ? m(L.id) : L.type === "command" ? E(L.id) : L.type === "group" && (o ? k === L.id ? f() : h(L.id) : n(L.id));
        break;
      }
      case "ArrowRight": {
        L.type === "group" && !o && ((d.get(L.id) ?? !1) || (N.preventDefault(), n(L.id)));
        break;
      }
      case "ArrowLeft": {
        L.type === "group" && !o && (d.get(L.id) ?? !1) && (N.preventDefault(), n(L.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    R,
    k,
    y,
    m,
    E,
    n,
    h,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: G }, c.map((N) => /* @__PURE__ */ e.createElement(
    Qe,
    {
      key: N.id,
      item: N,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: E,
      onToggleGroup: n,
      focusedId: R,
      setItemRef: P,
      onItemFocus: p,
      groupStates: d,
      flyoutGroupId: k,
      onOpenFlyout: h,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
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
  const t = W(), a = ee(), r = t.items ?? [], c = ln((i) => {
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
  const t = W(), a = ee(), r = t.items ?? [], c = t.activeItemId, i = on((o) => {
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
  const t = W(), a = ee(), r = oe(un), c = t.open === !0, i = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, s = t.actions ?? [], n = t.child, m = cn(null), E = He(() => {
    a("close");
  }, [a]), b = He((v) => {
    d && v.target === v.currentTarget && E();
  }, [d, E]);
  if (Ue(() => {
    if (!c) return;
    const v = (h) => {
      h.key === "Escape" && E();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [c, E]), Ue(() => {
    c && m.current && m.current.focus();
  }, [c]), !c) return null;
  const k = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": k,
      ref: m,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: k }, i), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: E,
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
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((v, h) => /* @__PURE__ */ e.createElement(K, { key: h, control: v })))
  ));
}, { useCallback: mn, useEffect: pn } = e, fn = {
  "js.drawer.close": "Close"
}, hn = ({ controlId: l }) => {
  const t = W(), a = ee(), r = oe(fn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, n = mn(() => {
    a("close");
  }, [a]);
  pn(() => {
    if (!c) return;
    const E = (b) => {
      b.key === "Escape" && n();
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
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
  const t = W(), a = ee(), r = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, [n, m] = bn(!1), E = Ve(() => {
    m(!0), setTimeout(() => {
      a("dismiss"), m(!1);
    }, 200);
  }, [a]), b = Ve(() => {
    o && a(o.commandName), E();
  }, [a, o, E]);
  return _n(() => {
    if (!s || d === 0) return;
    const k = setTimeout(E, d);
    return () => clearTimeout(k);
  }, [s, d, E]), !s && !n ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${n ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: b }, o.label)
  );
}, { useCallback: Re, useEffect: Le, useRef: En, useState: We } = e, gn = ({ controlId: l }) => {
  const t = W(), a = ee(), r = t.open === !0, c = t.anchorId, i = t.items ?? [], o = En(null), [d, s] = We({ top: 0, left: 0 }), [n, m] = We(0), E = i.filter((h) => h.type === "item" && !h.disabled);
  Le(() => {
    var p, g;
    if (!r || !c) return;
    const h = document.getElementById(c);
    if (!h) return;
    const f = h.getBoundingClientRect(), R = ((p = o.current) == null ? void 0 : p.offsetHeight) ?? 200, S = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let $ = f.bottom + 4, P = f.left;
    $ + R > window.innerHeight && ($ = f.top - R - 4), P + S > window.innerWidth && (P = f.right - S), s({ top: $, left: P }), m(0);
  }, [r, c]);
  const b = Re(() => {
    a("close");
  }, [a]), k = Re((h) => {
    a("selectItem", { itemId: h });
  }, [a]);
  Le(() => {
    if (!r) return;
    const h = (f) => {
      o.current && !o.current.contains(f.target) && b();
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [r, b]);
  const v = Re((h) => {
    if (h.key === "Escape") {
      b();
      return;
    }
    if (h.key === "ArrowDown")
      h.preventDefault(), m((f) => (f + 1) % E.length);
    else if (h.key === "ArrowUp")
      h.preventDefault(), m((f) => (f - 1 + E.length) % E.length);
    else if (h.key === "Enter" || h.key === " ") {
      h.preventDefault();
      const f = E[n];
      f && k(f.id);
    }
  }, [b, k, E, n]);
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
      onKeyDown: v
    },
    i.map((h, f) => {
      if (h.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const S = E.indexOf(h) === n;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: h.id,
          type: "button",
          className: "tlMenu__item" + (S ? " tlMenu__item--focused" : "") + (h.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: h.disabled,
          tabIndex: S ? 0 : -1,
          onClick: () => k(h.id)
        },
        h.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + h.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, h.label)
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
  const l = W(), t = ee(), a = oe(wn), r = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, d = l.selectionMode ?? "single", s = l.selectedCount ?? 0, n = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, E = e.useMemo(
    () => r.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [r]
  ), b = d === "multi", k = 40, v = 20, h = e.useRef(null), f = e.useRef(null), R = e.useRef(null), [S, $] = e.useState({}), P = e.useRef(null), p = e.useRef(!1), g = e.useRef(null), [y, G] = e.useState(null), [N, T] = e.useState(null);
  e.useEffect(() => {
    P.current || $({});
  }, [r]);
  const j = e.useCallback((w) => S[w.name] ?? w.width, [S]), L = e.useMemo(() => {
    const w = [];
    let D = b && n > 0 ? k : 0;
    for (let z = 0; z < n && z < r.length; z++)
      w.push(D), D += j(r[z]);
    return w;
  }, [r, n, b, k, j]), O = c * o, U = e.useCallback((w, D, z) => {
    z.preventDefault(), z.stopPropagation(), P.current = { column: w, startX: z.clientX, startWidth: D };
    const Q = (C) => {
      const x = P.current;
      if (!x) return;
      const A = Math.max(Ke, x.startWidth + (C.clientX - x.startX));
      $((q) => ({ ...q, [x.column]: A }));
    }, J = (C) => {
      document.removeEventListener("mousemove", Q), document.removeEventListener("mouseup", J);
      const x = P.current;
      if (x) {
        const A = Math.max(Ke, x.startWidth + (C.clientX - x.startX));
        t("columnResize", { column: x.column, width: A }), P.current = null, p.current = !0, requestAnimationFrame(() => {
          p.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Q), document.addEventListener("mouseup", J);
  }, [t]), X = e.useCallback(() => {
    h.current && f.current && (h.current.scrollLeft = f.current.scrollLeft), R.current !== null && clearTimeout(R.current), R.current = window.setTimeout(() => {
      const w = f.current;
      if (!w) return;
      const D = w.scrollTop, z = Math.ceil(w.clientHeight / o), Q = Math.floor(D / o);
      t("scroll", { start: Q, count: z });
    }, 80);
  }, [t, o]), Z = e.useCallback((w, D, z) => {
    if (p.current) return;
    let Q;
    !D || D === "desc" ? Q = "asc" : Q = "desc";
    const J = z.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: Q, mode: J });
  }, [t]), se = e.useCallback((w, D) => {
    g.current = w, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", w);
  }, []), ce = e.useCallback((w, D) => {
    if (!g.current || g.current === w) {
      G(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const z = D.currentTarget.getBoundingClientRect(), Q = D.clientX < z.left + z.width / 2 ? "left" : "right";
    G({ column: w, side: Q });
  }, []), ne = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const D = g.current;
    if (!D || !y) {
      g.current = null, G(null);
      return;
    }
    let z = r.findIndex((J) => J.name === y.column);
    if (z < 0) {
      g.current = null, G(null);
      return;
    }
    const Q = r.findIndex((J) => J.name === D);
    y.side === "right" && z++, Q < z && z--, t("columnReorder", { column: D, targetIndex: z }), g.current = null, G(null);
  }, [r, y, t]), ue = e.useCallback(() => {
    g.current = null, G(null);
  }, []), u = e.useCallback((w, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), _ = e.useCallback((w, D) => {
    D.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), M = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), I = e.useCallback((w, D, z) => {
    z.stopPropagation(), t("expand", { rowIndex: w, expanded: D });
  }, [t]), H = e.useCallback((w, D) => {
    D.preventDefault(), T({ x: D.clientX, y: D.clientY, colIdx: w });
  }, []), V = e.useCallback(() => {
    N && (t("setFrozenColumnCount", { count: N.colIdx + 1 }), T(null));
  }, [N, t]), Y = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), T(null);
  }, [t]);
  e.useEffect(() => {
    if (!N) return;
    const w = () => T(null), D = (z) => {
      z.key === "Escape" && T(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", D);
    };
  }, [N]);
  const re = r.reduce((w, D) => w + j(D), 0) + (b ? k : 0), te = s === c && c > 0, pe = s > 0 && s < c, Ne = e.useCallback((w) => {
    w && (w.indeterminate = pe);
  }, [pe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!g.current) return;
        w.preventDefault();
        const D = f.current, z = h.current;
        if (!D) return;
        const Q = D.getBoundingClientRect(), J = 40, C = 8;
        w.clientX < Q.left + J ? D.scrollLeft = Math.max(0, D.scrollLeft - C) : w.clientX > Q.right - J && (D.scrollLeft += C), z && (z.scrollLeft = D.scrollLeft);
      },
      onDrop: ne
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: re } }, b && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: k,
          minWidth: k,
          ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", r.length > 0 && r[0].name !== g.current && G({ column: r[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ne,
          className: "tlTableView__checkbox",
          checked: te,
          onChange: M
        }
      )
    ), r.map((w, D) => {
      const z = j(w), Q = D === r.length - 1;
      let J = "tlTableView__headerCell";
      w.sortable && (J += " tlTableView__headerCell--sortable"), y && y.column === w.name && (J += " tlTableView__headerCell--dragOver-" + y.side);
      const C = D < n, x = D === n - 1;
      return C && (J += " tlTableView__headerCell--frozen"), x && (J += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: J,
          style: {
            ...Q && !C ? { flex: "1 0 auto", minWidth: z } : { width: z, minWidth: z },
            position: C ? "sticky" : "relative",
            ...C ? { left: L[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (A) => Z(w.name, w.sortDirection, A) : void 0,
          onContextMenu: (A) => H(D, A),
          onDragStart: (A) => se(w.name, A),
          onDragOver: (A) => ce(w.name, A),
          onDrop: ne,
          onDragEnd: ue
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", E > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (A) => U(w.name, z, A)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (g.current && r.length > 0) {
            const D = r[r.length - 1];
            D.name !== g.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", G({ column: D.name, side: "right" }));
          }
        },
        onDrop: ne
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: X
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", minWidth: re } }, i.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            minWidth: re,
            width: "100%"
          },
          onClick: (D) => u(w.index, D)
        },
        b && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: k,
              minWidth: k,
              ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
              onClick: (D) => _(w.index, D),
              tabIndex: -1
            }
          )
        ),
        r.map((D, z) => {
          const Q = j(D), J = z === r.length - 1, C = z < n, x = z === n - 1;
          let A = "tlTableView__cell";
          C && (A += " tlTableView__cell--frozen"), x && (A += " tlTableView__cell--frozenLast");
          const q = m && z === 0, be = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: A,
              style: {
                ...J && !C ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...C ? { position: "sticky", left: L[z], zIndex: 2 } : {}
              }
            },
            q ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: be * v } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (rt) => I(w.index, !w.expanded, rt)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: w.cells[D.name] })) : /* @__PURE__ */ e.createElement(K, { control: w.cells[D.name] })
          );
        })
      )))
    ),
    N && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: N.y, left: N.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      N.colIdx + 1 !== n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: V }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      n > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Y }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
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
    const v = d.current;
    if (!v) return;
    const h = new ResizeObserver((f) => {
      for (const R of f) {
        const $ = R.contentRect.width / a;
        n($ < Dn ? "top" : "side");
      }
    });
    return h.observe(v), () => h.disconnect();
  }, [r, a]);
  const m = Sn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, k = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Je.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: b, ref: d }, i.map((v, h) => /* @__PURE__ */ e.createElement(K, { key: h, control: v }))));
}, { useCallback: In } = e, Pn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, jn = ({ controlId: l }) => {
  const t = W(), a = ee(), r = oe(Pn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", n = t.fullLine === !0, m = t.children ?? [], E = c != null || i.length > 0 || o, b = In(() => {
    a("toggleCollapse");
  }, [a]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    n ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: k }, E && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((v, h) => /* @__PURE__ */ e.createElement(K, { key: h, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((v, h) => /* @__PURE__ */ e.createElement(K, { key: h, control: v }))));
}, { useContext: Mn, useState: An, useCallback: On } = e, Bn = ({ controlId: l }) => {
  const t = W(), a = Mn(Je), r = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? a.resolvedLabelPosition, n = t.fullLine === !0, m = t.visible !== !1, E = t.field, b = a.readOnly, [k, v] = An(!1), h = On(() => v((S) => !S), []);
  if (!m) return null;
  const f = i != null, R = [
    "tlFormField",
    `tlFormField--${s}`,
    b ? "tlFormField--readonly" : "",
    n ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: R }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), c && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !b && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: E })), !b && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !b && o && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, $n = () => {
  const l = W(), t = ee(), a = l.iconCss, r = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, d = l.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, n = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((b) => {
    b.preventDefault(), t("goto", {});
  }, [t]), E = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: E, href: "#", onClick: m, title: o }, n) : /* @__PURE__ */ e.createElement("span", { className: E, title: o }, n);
}, Fn = 20, zn = () => {
  const l = W(), t = ee(), a = l.nodes ?? [], r = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [s, n] = e.useState(-1), m = e.useRef(null), E = e.useCallback((p, g) => {
    t(g ? "collapse" : "expand", { nodeId: p });
  }, [t]), b = e.useCallback((p, g) => {
    t("select", {
      nodeId: p,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), k = e.useCallback((p, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: p, x: g.clientX, y: g.clientY });
  }, [t]), v = e.useRef(null), h = e.useCallback((p, g) => {
    const y = g.getBoundingClientRect(), G = p.clientY - y.top, N = y.height / 3;
    return G < N ? "above" : G > N * 2 ? "below" : "within";
  }, []), f = e.useCallback((p, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", p);
  }, []), R = e.useCallback((p, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const y = h(g, g.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: p, position: y }), v.current = null;
    }, 50);
  }, [t, h]), S = e.useCallback((p, g) => {
    g.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const y = h(g, g.currentTarget);
    t("drop", { nodeId: p, position: y });
  }, [t, h]), $ = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
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
          const y = a[s];
          if (y.expandable && !y.expanded) {
            t("expand", { nodeId: y.id });
            return;
          } else y.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (p.preventDefault(), s >= 0 && s < a.length) {
          const y = a[s];
          if (y.expanded) {
            t("collapse", { nodeId: y.id });
            return;
          } else {
            const G = y.depth;
            for (let N = s - 1; N >= 0; N--)
              if (a[N].depth < G) {
                g = N;
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
        onClick: (y) => b(p.id, y),
        onContextMenu: (y) => k(p.id, y),
        onDragStart: (y) => f(p.id, y),
        onDragOver: i ? (y) => R(p.id, y) : void 0,
        onDrop: i ? (y) => S(p.id, y) : void 0,
        onDragEnd: $
      },
      p.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (y) => {
            y.stopPropagation(), E(p.id, p.expanded);
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
var De = { exports: {} }, le = {}, xe = { exports: {} }, B = {};
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
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), n = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), E = Symbol.for("react.activity"), b = Symbol.iterator;
  function k(u) {
    return u === null || typeof u != "object" ? null : (u = b && u[b] || u["@@iterator"], typeof u == "function" ? u : null);
  }
  var v = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, h = Object.assign, f = {};
  function R(u, _, M) {
    this.props = u, this.context = _, this.refs = f, this.updater = M || v;
  }
  R.prototype.isReactComponent = {}, R.prototype.setState = function(u, _) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, _, "setState");
  }, R.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function S() {
  }
  S.prototype = R.prototype;
  function $(u, _, M) {
    this.props = u, this.context = _, this.refs = f, this.updater = M || v;
  }
  var P = $.prototype = new S();
  P.constructor = $, h(P, R.prototype), P.isPureReactComponent = !0;
  var p = Array.isArray;
  function g() {
  }
  var y = { H: null, A: null, T: null, S: null }, G = Object.prototype.hasOwnProperty;
  function N(u, _, M) {
    var I = M.ref;
    return {
      $$typeof: l,
      type: u,
      key: _,
      ref: I !== void 0 ? I : null,
      props: M
    };
  }
  function T(u, _) {
    return N(u.type, _, u.props);
  }
  function j(u) {
    return typeof u == "object" && u !== null && u.$$typeof === l;
  }
  function L(u) {
    var _ = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function(M) {
      return _[M];
    });
  }
  var O = /\/+/g;
  function U(u, _) {
    return typeof u == "object" && u !== null && u.key != null ? L("" + u.key) : _.toString(36);
  }
  function X(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(g, g) : (u.status = "pending", u.then(
          function(_) {
            u.status === "pending" && (u.status = "fulfilled", u.value = _);
          },
          function(_) {
            u.status === "pending" && (u.status = "rejected", u.reason = _);
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
  function Z(u, _, M, I, H) {
    var V = typeof u;
    (V === "undefined" || V === "boolean") && (u = null);
    var Y = !1;
    if (u === null) Y = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          Y = !0;
          break;
        case "object":
          switch (u.$$typeof) {
            case l:
            case t:
              Y = !0;
              break;
            case m:
              return Y = u._init, Z(
                Y(u._payload),
                _,
                M,
                I,
                H
              );
          }
      }
    if (Y)
      return H = H(u), Y = I === "" ? "." + U(u, 0) : I, p(H) ? (M = "", Y != null && (M = Y.replace(O, "$&/") + "/"), Z(H, _, M, "", function(pe) {
        return pe;
      })) : H != null && (j(H) && (H = T(
        H,
        M + (H.key == null || u && u.key === H.key ? "" : ("" + H.key).replace(
          O,
          "$&/"
        ) + "/") + Y
      )), _.push(H)), 1;
    Y = 0;
    var re = I === "" ? "." : I + ":";
    if (p(u))
      for (var te = 0; te < u.length; te++)
        I = u[te], V = re + U(I, te), Y += Z(
          I,
          _,
          M,
          V,
          H
        );
    else if (te = k(u), typeof te == "function")
      for (u = te.call(u), te = 0; !(I = u.next()).done; )
        I = I.value, V = re + U(I, te++), Y += Z(
          I,
          _,
          M,
          V,
          H
        );
    else if (V === "object") {
      if (typeof u.then == "function")
        return Z(
          X(u),
          _,
          M,
          I,
          H
        );
      throw _ = String(u), Error(
        "Objects are not valid as a React child (found: " + (_ === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : _) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Y;
  }
  function se(u, _, M) {
    if (u == null) return u;
    var I = [], H = 0;
    return Z(u, I, "", "", function(V) {
      return _.call(M, V, H++);
    }), I;
  }
  function ce(u) {
    if (u._status === -1) {
      var _ = u._result;
      _ = _(), _.then(
        function(M) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = M);
        },
        function(M) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = M);
        }
      ), u._status === -1 && (u._status = 0, u._result = _);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var ne = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var _ = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(_)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, ue = {
    map: se,
    forEach: function(u, _, M) {
      se(
        u,
        function() {
          _.apply(this, arguments);
        },
        M
      );
    },
    count: function(u) {
      var _ = 0;
      return se(u, function() {
        _++;
      }), _;
    },
    toArray: function(u) {
      return se(u, function(_) {
        return _;
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
  return B.Activity = E, B.Children = ue, B.Component = R, B.Fragment = a, B.Profiler = c, B.PureComponent = $, B.StrictMode = r, B.Suspense = s, B.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = y, B.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return y.H.useMemoCache(u);
    }
  }, B.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, B.cacheSignal = function() {
    return null;
  }, B.cloneElement = function(u, _, M) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var I = h({}, u.props), H = u.key;
    if (_ != null)
      for (V in _.key !== void 0 && (H = "" + _.key), _)
        !G.call(_, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && _.ref === void 0 || (I[V] = _[V]);
    var V = arguments.length - 2;
    if (V === 1) I.children = M;
    else if (1 < V) {
      for (var Y = Array(V), re = 0; re < V; re++)
        Y[re] = arguments[re + 2];
      I.children = Y;
    }
    return N(u.type, H, I);
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
  }, B.createElement = function(u, _, M) {
    var I, H = {}, V = null;
    if (_ != null)
      for (I in _.key !== void 0 && (V = "" + _.key), _)
        G.call(_, I) && I !== "key" && I !== "__self" && I !== "__source" && (H[I] = _[I]);
    var Y = arguments.length - 2;
    if (Y === 1) H.children = M;
    else if (1 < Y) {
      for (var re = Array(Y), te = 0; te < Y; te++)
        re[te] = arguments[te + 2];
      H.children = re;
    }
    if (u && u.defaultProps)
      for (I in Y = u.defaultProps, Y)
        H[I] === void 0 && (H[I] = Y[I]);
    return N(u, V, H);
  }, B.createRef = function() {
    return { current: null };
  }, B.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, B.isValidElement = j, B.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: ce
    };
  }, B.memo = function(u, _) {
    return {
      $$typeof: n,
      type: u,
      compare: _ === void 0 ? null : _
    };
  }, B.startTransition = function(u) {
    var _ = y.T, M = {};
    y.T = M;
    try {
      var I = u(), H = y.S;
      H !== null && H(M, I), typeof I == "object" && I !== null && typeof I.then == "function" && I.then(g, ne);
    } catch (V) {
      ne(V);
    } finally {
      _ !== null && M.types !== null && (_.types = M.types), y.T = _;
    }
  }, B.unstable_useCacheRefresh = function() {
    return y.H.useCacheRefresh();
  }, B.use = function(u) {
    return y.H.use(u);
  }, B.useActionState = function(u, _, M) {
    return y.H.useActionState(u, _, M);
  }, B.useCallback = function(u, _) {
    return y.H.useCallback(u, _);
  }, B.useContext = function(u) {
    return y.H.useContext(u);
  }, B.useDebugValue = function() {
  }, B.useDeferredValue = function(u, _) {
    return y.H.useDeferredValue(u, _);
  }, B.useEffect = function(u, _) {
    return y.H.useEffect(u, _);
  }, B.useEffectEvent = function(u) {
    return y.H.useEffectEvent(u);
  }, B.useId = function() {
    return y.H.useId();
  }, B.useImperativeHandle = function(u, _, M) {
    return y.H.useImperativeHandle(u, _, M);
  }, B.useInsertionEffect = function(u, _) {
    return y.H.useInsertionEffect(u, _);
  }, B.useLayoutEffect = function(u, _) {
    return y.H.useLayoutEffect(u, _);
  }, B.useMemo = function(u, _) {
    return y.H.useMemo(u, _);
  }, B.useOptimistic = function(u, _) {
    return y.H.useOptimistic(u, _);
  }, B.useReducer = function(u, _, M) {
    return y.H.useReducer(u, _, M);
  }, B.useRef = function(u) {
    return y.H.useRef(u);
  }, B.useState = function(u) {
    return y.H.useState(u);
  }, B.useSyncExternalStore = function(u, _, M) {
    return y.H.useSyncExternalStore(
      u,
      _,
      M
    );
  }, B.useTransition = function() {
    return y.H.useTransition();
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
  if (qe) return le;
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
    var E = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: E == null ? null : "" + E,
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
  return le.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, le.createPortal = function(s, n) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!n || n.nodeType !== 1 && n.nodeType !== 9 && n.nodeType !== 11)
      throw Error(t(299));
    return i(s, n, null, m);
  }, le.flushSync = function(s) {
    var n = o.T, m = r.p;
    try {
      if (o.T = null, r.p = 2, s) return s();
    } finally {
      o.T = n, r.p = m, r.d.f();
    }
  }, le.preconnect = function(s, n) {
    typeof s == "string" && (n ? (n = n.crossOrigin, n = typeof n == "string" ? n === "use-credentials" ? n : "" : void 0) : n = null, r.d.C(s, n));
  }, le.prefetchDNS = function(s) {
    typeof s == "string" && r.d.D(s);
  }, le.preinit = function(s, n) {
    if (typeof s == "string" && n && typeof n.as == "string") {
      var m = n.as, E = d(m, n.crossOrigin), b = typeof n.integrity == "string" ? n.integrity : void 0, k = typeof n.fetchPriority == "string" ? n.fetchPriority : void 0;
      m === "style" ? r.d.S(
        s,
        typeof n.precedence == "string" ? n.precedence : void 0,
        {
          crossOrigin: E,
          integrity: b,
          fetchPriority: k
        }
      ) : m === "script" && r.d.X(s, {
        crossOrigin: E,
        integrity: b,
        fetchPriority: k,
        nonce: typeof n.nonce == "string" ? n.nonce : void 0
      });
    }
  }, le.preinitModule = function(s, n) {
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
  }, le.preload = function(s, n) {
    if (typeof s == "string" && typeof n == "object" && n !== null && typeof n.as == "string") {
      var m = n.as, E = d(m, n.crossOrigin);
      r.d.L(s, m, {
        crossOrigin: E,
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
  }, le.preloadModule = function(s, n) {
    if (typeof s == "string")
      if (n) {
        var m = d(n.as, n.crossOrigin);
        r.d.m(s, {
          as: typeof n.as == "string" && n.as !== "script" ? n.as : void 0,
          crossOrigin: m,
          integrity: typeof n.integrity == "string" ? n.integrity : void 0
        });
      } else r.d.m(s);
  }, le.requestFormReset = function(s) {
    r.d.r(s);
  }, le.unstable_batchedUpdates = function(s, n) {
    return s(n);
  }, le.useFormState = function(s, n, m) {
    return o.H.useFormState(s, n, m);
  }, le.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, le.version = "19.2.4", le;
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
const { useState: de, useCallback: ae, useRef: ve, useEffect: fe, useMemo: Ae } = e;
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
  const m = ae(
    (E) => {
      E.stopPropagation(), a(l.value);
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
  const o = ae(() => r(l.value), [r, l.value]), d = Ae(() => {
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
  const a = ee(), r = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, n = t.optionsLoaded === !0, m = t.options ?? [], E = t.emptyOptionLabel ?? "", b = i && c && !d && s, k = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), v = k["js.dropdownSelect.nothingFound"], h = ae(
    (C) => k["js.dropdownSelect.removeChip"].replace("{0}", C),
    [k]
  ), [f, R] = de(!1), [S, $] = de(""), [P, p] = de(-1), [g, y] = de(!1), [G, N] = de({}), [T, j] = de(null), [L, O] = de(null), [U, X] = de(null), Z = ve(null), se = ve(null), ce = ve(null), ne = ve(r);
  ne.current = r;
  const ue = ve(-1), u = Ae(
    () => new Set(r.map((C) => C.value)),
    [r]
  ), _ = Ae(() => {
    let C = m.filter((x) => !u.has(x.value));
    if (S) {
      const x = S.toLowerCase();
      C = C.filter((A) => A.label.toLowerCase().includes(x));
    }
    return C;
  }, [m, u, S]);
  fe(() => {
    S && _.length === 1 ? p(0) : p(-1);
  }, [_.length, S]), fe(() => {
    f && n && se.current && se.current.focus();
  }, [f, n, r]), fe(() => {
    var A, q;
    if (ue.current < 0) return;
    const C = ue.current;
    ue.current = -1;
    const x = (A = Z.current) == null ? void 0 : A.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(C, x.length - 1)].focus() : (q = Z.current) == null || q.focus();
  }, [r]), fe(() => {
    if (!f) return;
    const C = (x) => {
      Z.current && !Z.current.contains(x.target) && ce.current && !ce.current.contains(x.target) && (R(!1), $(""));
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [f]), fe(() => {
    if (!f || !Z.current) return;
    const C = Z.current.getBoundingClientRect(), x = window.innerHeight - C.bottom, q = x < 300 && C.top > x;
    N({
      left: C.left,
      width: C.width,
      ...q ? { bottom: window.innerHeight - C.top } : { top: C.bottom }
    });
  }, [f]);
  const M = ae(async () => {
    if (!(d || !s) && (R(!0), $(""), p(-1), y(!1), !n))
      try {
        await a("loadOptions");
      } catch {
        y(!0);
      }
  }, [d, s, n, a]), I = ae(() => {
    var C;
    R(!1), $(""), p(-1), (C = Z.current) == null || C.focus();
  }, []), H = ae(
    (C) => {
      let x;
      if (c) {
        const A = m.find((q) => q.value === C);
        if (A)
          x = [...ne.current, A];
        else
          return;
      } else {
        const A = m.find((q) => q.value === C);
        if (A)
          x = [A];
        else
          return;
      }
      ne.current = x, a("valueChanged", { value: x.map((A) => A.value) }), c ? ($(""), p(-1)) : I();
    },
    [c, m, a, I]
  ), V = ae(
    (C) => {
      ue.current = ne.current.findIndex((A) => A.value === C);
      const x = ne.current.filter((A) => A.value !== C);
      ne.current = x, a("valueChanged", { value: x.map((A) => A.value) });
    },
    [a]
  ), Y = ae(
    (C) => {
      C.stopPropagation(), a("valueChanged", { value: [] }), I();
    },
    [a, I]
  ), re = ae((C) => {
    $(C.target.value);
  }, []), te = ae(
    (C) => {
      if (!f) {
        if (C.key === "ArrowDown" || C.key === "ArrowUp" || C.key === "Enter" || C.key === " ") {
          if (C.target.tagName === "BUTTON") return;
          C.preventDefault(), C.stopPropagation(), M();
        }
        return;
      }
      switch (C.key) {
        case "ArrowDown":
          C.preventDefault(), C.stopPropagation(), p(
            (x) => x < _.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          C.preventDefault(), C.stopPropagation(), p(
            (x) => x > 0 ? x - 1 : _.length - 1
          );
          break;
        case "Enter":
          C.preventDefault(), C.stopPropagation(), P >= 0 && P < _.length && H(_[P].value);
          break;
        case "Escape":
          C.preventDefault(), C.stopPropagation(), I();
          break;
        case "Tab":
          I();
          break;
        case "Backspace":
          S === "" && c && r.length > 0 && V(r[r.length - 1].value);
          break;
      }
    },
    [
      f,
      M,
      I,
      _,
      P,
      H,
      S,
      c,
      r,
      V
    ]
  ), pe = ae(
    async (C) => {
      C.preventDefault(), y(!1);
      try {
        await a("loadOptions");
      } catch {
        y(!0);
      }
    },
    [a]
  ), Ne = ae(
    (C, x) => {
      j(C), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(C));
    },
    []
  ), w = ae(
    (C, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", T === null || T === C) {
        O(null), X(null);
        return;
      }
      const A = x.currentTarget.getBoundingClientRect(), q = A.left + A.width / 2, be = x.clientX < q ? "before" : "after";
      O(C), X(be);
    },
    [T]
  ), D = ae(
    (C) => {
      if (C.preventDefault(), T === null || L === null || U === null || T === L) return;
      const x = [...ne.current], [A] = x.splice(T, 1);
      let q = L;
      T < L ? q = U === "before" ? q - 1 : q : q = U === "before" ? q : q + 1, x.splice(q, 0, A), ne.current = x, a("valueChanged", { value: x.map((be) => be.value) }), j(null), O(null), X(null);
    },
    [T, L, U, a]
  ), z = ae(() => {
    j(null), O(null), X(null);
  }, []);
  if (fe(() => {
    if (P < 0 || !ce.current) return;
    const C = ce.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    C && C.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, E) : r.map((C) => /* @__PURE__ */ e.createElement("span", { key: C.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Fe, { image: C.image }), /* @__PURE__ */ e.createElement("span", null, C.label))));
  const Q = !o && r.length > 0 && !d, J = f ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: ce,
      className: "tlDropdownSelect__dropdown",
      style: G
    },
    (n || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: se,
        type: "text",
        className: "tlDropdownSelect__search",
        value: S,
        onChange: re,
        onKeyDown: te,
        placeholder: k["js.dropdownSelect.filterPlaceholder"],
        "aria-label": k["js.dropdownSelect.filterPlaceholder"],
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: pe }, k["js.dropdownSelect.error"])),
      n && _.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, v),
      n && _.map((C, x) => /* @__PURE__ */ e.createElement(
        Gn,
        {
          key: C.value,
          id: `${l}-opt-${x}`,
          option: C,
          highlighted: x === P,
          searchTerm: S,
          onSelect: H,
          onMouseEnter: () => p(x)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: Z,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${l}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: f ? void 0 : M,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, E) : r.map((C, x) => {
      let A = "";
      return T === x ? A = "tlDropdownSelect__chip--dragging" : L === x && U === "before" ? A = "tlDropdownSelect__chip--dropBefore" : L === x && U === "after" && (A = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Yn,
        {
          key: C.value,
          option: C,
          removable: !d && (c || !o),
          onRemove: V,
          removeLabel: h(C.label),
          draggable: b,
          onDragStart: b ? (q) => Ne(x, q) : void 0,
          onDragOver: b ? (q) => w(x, q) : void 0,
          onDrop: b ? D : void 0,
          onDragEnd: b ? z : void 0,
          dragClassName: b ? A : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Q && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Y,
        "aria-label": k["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), J && Kn.createPortal(J, document.body));
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
    (v, h) => {
      var $;
      const f = ($ = i.current) == null ? void 0 : $.getBoundingClientRect();
      if (!f) return;
      const R = Math.max(0, Math.min(1, (v - f.left) / f.width)), S = Math.max(0, Math.min(1, 1 - (h - f.top) / f.height));
      t(Pe(a, R, S));
    },
    [a, t]
  ), s = he(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), d(v.clientX, v.clientY);
    },
    [d]
  ), n = he(
    (v) => {
      v.buttons !== 0 && d(v.clientX, v.clientY);
    },
    [d]
  ), m = he(
    (v) => {
      var S;
      const h = (S = o.current) == null ? void 0 : S.getBoundingClientRect();
      if (!h) return;
      const R = Math.max(0, Math.min(1, (v - h.top) / h.height)) * 360;
      t(Pe(R, r, c));
    },
    [r, c, t]
  ), E = he(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), m(v.clientY);
    },
    [m]
  ), b = he(
    (v) => {
      v.buttons !== 0 && m(v.clientY);
    },
    [m]
  ), k = Pe(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: k },
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
      onPointerDown: E,
      onPointerMove: b
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
  "js.colorInput.reset": "Reset",
  "js.colorInput.cancel": "Cancel",
  "js.colorInput.ok": "OK"
}, { useState: ge, useCallback: me, useEffect: je, useRef: rr, useLayoutEffect: lr } = e, ar = ({
  anchorRef: l,
  currentColor: t,
  palette: a,
  paletteColumns: r,
  defaultPalette: c,
  onConfirm: i,
  onCancel: o,
  onPaletteChange: d
}) => {
  const [s, n] = ge("palette"), [m, E] = ge(t), b = rr(null), k = oe(nr), [v, h] = ge(null);
  lr(() => {
    if (!l.current || !b.current) return;
    const L = l.current.getBoundingClientRect(), O = b.current.getBoundingClientRect();
    let U = L.bottom + 4, X = L.left;
    U + O.height > window.innerHeight && (U = L.top - O.height - 4), X + O.width > window.innerWidth && (X = Math.max(0, L.right - O.width)), h({ top: U, left: X });
  }, [l]);
  const [f, R, S] = tt(m), [$, P] = ge(m.toUpperCase());
  je(() => {
    P(m.toUpperCase());
  }, [m]), je(() => {
    const L = (O) => {
      O.key === "Escape" && o();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [o]), je(() => {
    const L = (U) => {
      b.current && !b.current.contains(U.target) && o();
    }, O = setTimeout(() => document.addEventListener("mousedown", L), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", L);
    };
  }, [o]);
  const p = me(
    (L) => (O) => {
      const U = parseInt(O.target.value, 10);
      if (isNaN(U)) return;
      const X = et(U);
      E(nt(L === "r" ? X : f, L === "g" ? X : R, L === "b" ? X : S));
    },
    [f, R, S]
  ), g = me((L) => {
    const O = L.target.value;
    P(O), Oe(O) && E(O);
  }, []), y = me((L) => {
    E(L);
  }, []), G = me(
    (L) => {
      i(L);
    },
    [i]
  ), N = me(
    (L, O) => {
      const U = [...a], X = U[L];
      U[L] = U[O], U[O] = X, d(U);
    },
    [a, d]
  ), T = me(() => {
    d([...c]);
  }, [c, d]), j = me(() => {
    i(m);
  }, [m, i]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: b,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (s === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => n("palette")
      },
      k["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (s === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => n("mixer")
      },
      k["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, s === "palette" ? /* @__PURE__ */ e.createElement(
      Zn,
      {
        colors: a,
        columns: r,
        onSelect: y,
        onConfirm: G,
        onSwap: N
      }
    ) : /* @__PURE__ */ e.createElement(tr, { color: m, onColorChange: E }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, k["js.colorInput.current"]), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: t } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, k["js.colorInput.new"]), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: m } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, k["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: f,
        onChange: p("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, k["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: R,
        onChange: p("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, k["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S,
        onChange: p("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, k["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (Oe($) ? "" : " tlColorInput__input--error"),
        type: "text",
        value: $,
        onChange: g
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s === "palette" && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, k["js.colorInput.reset"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: o }, k["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: j }, k["js.colorInput.ok"]))
  );
}, or = { "js.colorInput.chooseColor": "Choose color" }, { useState: sr, useCallback: Ce, useRef: cr } = e, ir = ({ controlId: l, state: t }) => {
  const a = ee(), r = oe(or), [c, i] = sr(!1), o = cr(null), d = t.value, s = t.editable !== !1, n = t.palette ?? [], m = t.paletteColumns ?? 6, E = t.defaultPalette ?? n, b = Ce(() => {
    s && i(!0);
  }, [s]), k = Ce(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), v = Ce(() => {
    i(!1);
  }, []), h = Ce(
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
      onClick: b,
      disabled: t.disabled === !0,
      title: d ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    ar,
    {
      anchorRef: o,
      currentColor: d ?? "#000000",
      palette: n,
      paletteColumns: m,
      defaultPalette: E,
      onConfirm: k,
      onCancel: v,
      onPaletteChange: h
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
