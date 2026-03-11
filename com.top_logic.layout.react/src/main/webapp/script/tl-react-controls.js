import { React as e, useTLFieldValue as _e, getComponent as Ke, useTLState as H, useTLCommand as ee, TLChild as V, useTLUpload as Re, useI18N as ce, useTLDataUrl as xe, register as F } from "tl-react-bridge";
const { useCallback: Ye } = e, Ge = ({ controlId: a, state: t }) => {
  const [l, r] = _e(), i = Ye(
    (u) => {
      r(u.target.value);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactTextInput tlReactTextInput--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: a,
      value: l ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: qe } = e, Xe = ({ controlId: a, state: t, config: l }) => {
  const [r, i] = _e(), u = qe(
    (d) => {
      const c = d.target.value, n = c === "" ? null : Number(c);
      i(n);
    },
    [i]
  ), s = l != null && l.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: a,
      value: r != null ? String(r) : "",
      onChange: u,
      step: s,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Ze } = e, Qe = ({ controlId: a, state: t }) => {
  const [l, r] = _e(), i = Ze(
    (u) => {
      r(u.target.value || null);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactDatePicker tlReactDatePicker--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: a,
      value: l ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Je } = e, et = ({ controlId: a, state: t, config: l }) => {
  var d;
  const [r, i] = _e(), u = Je(
    (c) => {
      i(c.target.value || null);
    },
    [i]
  ), s = t.options ?? (l == null ? void 0 : l.options) ?? [];
  if (t.editable === !1) {
    const c = ((d = s.find((n) => n.value === r)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactSelect tlReactSelect--immutable" }, c);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: a,
      value: r ?? "",
      onChange: u,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: tt } = e, nt = ({ controlId: a, state: t }) => {
  const [l, r] = _e(), i = tt(
    (u) => {
      r(u.target.checked);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: l === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: l === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, rt = ({ controlId: a, state: t }) => {
  const l = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: a, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, l.map((s) => {
    const d = s.cellModule ? Ke(s.cellModule) : void 0, c = i[s.name];
    if (d) {
      const n = { value: c, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: a + "-" + u + "-" + s.name,
          state: n
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, c != null ? String(c) : "");
  })))));
}, { useCallback: at } = e, lt = ({ controlId: a, command: t, label: l, disabled: r }) => {
  const i = H(), u = ee(), s = t ?? "click", d = l ?? i.label, c = r ?? i.disabled === !0, n = at(() => {
    u(s);
  }, [u, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: n,
      disabled: c,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: ot } = e, st = ({ controlId: a, command: t, label: l, active: r, disabled: i }) => {
  const u = H(), s = ee(), d = t ?? "click", c = l ?? u.label, n = r ?? u.active === !0, p = i ?? u.disabled === !0, C = ot(() => {
    s(d);
  }, [s, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: C,
      disabled: p,
      className: "tlReactButton" + (n ? " tlReactButtonActive" : "")
    },
    c
  );
}, ct = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: it } = e, ut = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.tabs ?? [], i = t.activeTabId, u = it((s) => {
    s !== i && l("selectTab", { tabId: s });
  }, [l, i]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === i,
      className: "tlReactTabBar__tab" + (s.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => u(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, dt = ({ controlId: a }) => {
  const t = H(), l = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(V, { control: i })))));
}, mt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, pt = ({ controlId: a }) => {
  const t = H(), l = Re(), [r, i] = e.useState("idle"), [u, s] = e.useState(null), d = e.useRef(null), c = e.useRef([]), n = e.useRef(null), p = t.status ?? "idle", C = t.error, g = p === "received" ? "idle" : r !== "idle" ? r : p, L = e.useCallback(async () => {
    if (r === "recording") {
      const S = d.current;
      S && S.state !== "inactive" && S.stop();
      return;
    }
    if (r !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        n.current = S, c.current = [];
        const $ = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", O = new MediaRecorder(S, $ ? { mimeType: $ } : void 0);
        d.current = O, O.ondataavailable = (m) => {
          m.data.size > 0 && c.current.push(m.data);
        }, O.onstop = async () => {
          S.getTracks().forEach((y) => y.stop()), n.current = null;
          const m = new Blob(c.current, { type: O.mimeType || "audio/webm" });
          if (c.current = [], m.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const v = new FormData();
          v.append("audio", m, "recording.webm"), await l(v), i("idle");
        }, O.start(), i("recording");
      } catch (S) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", S), s("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, l]), T = ce(mt), b = g === "recording" ? T["js.audioRecorder.stop"] : g === "uploading" ? T["js.uploading"] : T["js.audioRecorder.record"], h = g === "uploading", D = ["tlAudioRecorder__button"];
  return g === "recording" && D.push("tlAudioRecorder__button--recording"), g === "uploading" && D.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: D.join(" "),
      onClick: L,
      disabled: h,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${g === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, T[u]), C && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C));
}, ft = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ht = ({ controlId: a }) => {
  const t = H(), l = xe(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [u, s] = e.useState(r ? "idle" : "disabled"), d = e.useRef(null), c = e.useRef(null), n = e.useRef(i);
  e.useEffect(() => {
    r ? u === "disabled" && s("idle") : (d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), s("disabled"));
  }, [r]), e.useEffect(() => {
    i !== n.current && (n.current = i, d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (u === "playing" || u === "paused" || u === "loading") && s("idle"));
  }, [i]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (u === "disabled" || u === "loading")
      return;
    if (u === "playing") {
      d.current && d.current.pause(), s("paused");
      return;
    }
    if (u === "paused" && d.current) {
      d.current.play(), s("playing");
      return;
    }
    if (!c.current) {
      s("loading");
      try {
        const h = await fetch(l);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), s("idle");
          return;
        }
        const D = await h.blob();
        c.current = URL.createObjectURL(D);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), s("idle");
        return;
      }
    }
    const b = new Audio(c.current);
    d.current = b, b.onended = () => {
      s("idle");
    }, b.play(), s("playing");
  }, [u, l]), C = ce(ft), g = u === "loading" ? C["js.loading"] : u === "playing" ? C["js.audioPlayer.pause"] : u === "disabled" ? C["js.audioPlayer.noAudio"] : C["js.audioPlayer.play"], L = u === "disabled" || u === "loading", T = ["tlAudioPlayer__button"];
  return u === "playing" && T.push("tlAudioPlayer__button--playing"), u === "loading" && T.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: p,
      disabled: L,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, bt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, _t = ({ controlId: a }) => {
  const t = H(), l = Re(), [r, i] = e.useState("idle"), [u, s] = e.useState(!1), d = e.useRef(null), c = t.status ?? "idle", n = t.error, p = t.accept ?? "", C = c === "received" ? "idle" : r !== "idle" ? r : c, g = e.useCallback(async (m) => {
    i("uploading");
    const v = new FormData();
    v.append("file", m, m.name), await l(v), i("idle");
  }, [l]), L = e.useCallback((m) => {
    var y;
    const v = (y = m.target.files) == null ? void 0 : y[0];
    v && g(v);
  }, [g]), T = e.useCallback(() => {
    var m;
    r !== "uploading" && ((m = d.current) == null || m.click());
  }, [r]), b = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), s(!0);
  }, []), h = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), s(!1);
  }, []), D = e.useCallback((m) => {
    var y;
    if (m.preventDefault(), m.stopPropagation(), s(!1), r === "uploading") return;
    const v = (y = m.dataTransfer.files) == null ? void 0 : y[0];
    v && g(v);
  }, [r, g]), S = C === "uploading", $ = ce(bt), O = C === "uploading" ? $["js.uploading"] : $["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: h,
      onDrop: D
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: p || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (C === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: T,
        disabled: S,
        title: O,
        "aria-label": O
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    n && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, n)
  );
}, vt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Et = ({ controlId: a }) => {
  const t = H(), l = xe(), r = ee(), i = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", d = !!t.clearable, [c, n] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || c)) {
      n(!0);
      try {
        const T = l + (l.includes("?") ? "&" : "?") + "rev=" + u, b = await fetch(T);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const h = await b.blob(), D = URL.createObjectURL(h), S = document.createElement("a");
        S.href = D, S.download = s, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(D);
      } catch (T) {
        console.error("[TLDownload] Fetch error:", T);
      } finally {
        n(!1);
      }
    }
  }, [i, c, l, u, s]), C = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), g = ce(vt);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, g["js.download.noFile"]));
  const L = c ? g["js.downloading"] : g["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: c,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: C,
      title: g["js.download.clear"],
      "aria-label": g["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, gt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, yt = ({ controlId: a }) => {
  const t = H(), l = Re(), [r, i] = e.useState("idle"), [u, s] = e.useState(null), [d, c] = e.useState(!1), n = e.useRef(null), p = e.useRef(null), C = e.useRef(null), g = e.useRef(null), L = e.useRef(null), T = t.error, b = e.useMemo(
    () => {
      var k;
      return !!(window.isSecureContext && ((k = navigator.mediaDevices) != null && k.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((k) => k.stop()), p.current = null), n.current && (n.current.srcObject = null);
  }, []), D = e.useCallback(() => {
    h(), i("idle");
  }, [h]), S = e.useCallback(async () => {
    var k;
    if (r !== "uploading") {
      if (s(null), !b) {
        (k = g.current) == null || k.click();
        return;
      }
      try {
        const A = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = A, i("overlayOpen");
      } catch (A) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", A), s("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, b]), $ = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const k = n.current, A = C.current;
    if (!k || !A)
      return;
    A.width = k.videoWidth, A.height = k.videoHeight;
    const P = A.getContext("2d");
    P && (P.drawImage(k, 0, 0), h(), i("uploading"), A.toBlob(async (K) => {
      if (!K) {
        i("idle");
        return;
      }
      const Q = new FormData();
      Q.append("photo", K, "capture.jpg"), await l(Q), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, l, h]), O = e.useCallback(async (k) => {
    var K;
    const A = (K = k.target.files) == null ? void 0 : K[0];
    if (!A) return;
    i("uploading");
    const P = new FormData();
    P.append("photo", A, A.name), await l(P), i("idle"), g.current && (g.current.value = "");
  }, [l]);
  e.useEffect(() => {
    r === "overlayOpen" && n.current && p.current && (n.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var A;
    if (r !== "overlayOpen") return;
    (A = L.current) == null || A.focus();
    const k = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = k;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const k = (A) => {
      A.key === "Escape" && D();
    };
    return document.addEventListener("keydown", k), () => document.removeEventListener("keydown", k);
  }, [r, D]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((k) => k.stop()), p.current = null);
  }, []);
  const m = ce(gt), v = r === "uploading" ? m["js.uploading"] : m["js.photoCapture.open"], y = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && y.push("tlPhotoCapture__cameraBtn--uploading");
  const Y = ["tlPhotoCapture__overlayVideo"];
  d && Y.push("tlPhotoCapture__overlayVideo--mirrored");
  const w = ["tlPhotoCapture__mirrorBtn"];
  return d && w.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: S,
      disabled: r === "uploading",
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !b && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: g,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: O
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: C, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: D }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: n,
        className: Y.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: w.join(" "),
        onClick: () => c((k) => !k),
        title: m["js.photoCapture.mirror"],
        "aria-label": m["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: $,
        title: m["js.photoCapture.capture"],
        "aria-label": m["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: D,
        title: m["js.photoCapture.close"],
        "aria-label": m["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m[u]), T && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, T));
}, Ct = {
  "js.photoViewer.alt": "Captured photo"
}, wt = ({ controlId: a }) => {
  const t = H(), l = xe(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [u, s] = e.useState(null), d = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      u && (URL.revokeObjectURL(u), s(null));
      return;
    }
    if (i === d.current && u)
      return;
    d.current = i, u && (URL.revokeObjectURL(u), s(null));
    let n = !1;
    return (async () => {
      try {
        const p = await fetch(l);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const C = await p.blob();
        n || s(URL.createObjectURL(C));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      n = !0;
    };
  }, [r, i, l]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const c = ce(Ct);
  return !r || !u ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: je, useRef: Ce } = e, kt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.orientation, i = t.resizable === !0, u = t.children ?? [], s = r === "horizontal", d = u.length > 0 && u.every((h) => h.collapsed), c = !d && u.some((h) => h.collapsed), n = d ? !s : s, p = Ce(null), C = Ce(null), g = Ce(null), L = je((h, D) => {
    const S = {
      overflow: h.scrolling || "auto"
    };
    return h.collapsed ? d && !n ? S.flex = "1 0 0%" : S.flex = "0 0 auto" : D !== void 0 ? S.flex = `0 0 ${D}px` : h.unit === "%" || c ? S.flex = `${h.size} 0 0%` : S.flex = `0 0 ${h.size}px`, h.minSize > 0 && !h.collapsed && (S.minWidth = s ? h.minSize : void 0, S.minHeight = s ? void 0 : h.minSize), S;
  }, [s, d, c, n]), T = je((h, D) => {
    h.preventDefault();
    const S = p.current;
    if (!S) return;
    const $ = u[D], O = u[D + 1], m = S.querySelectorAll(":scope > .tlSplitPanel__child"), v = [];
    m.forEach((w) => {
      v.push(s ? w.offsetWidth : w.offsetHeight);
    }), g.current = v, C.current = {
      splitterIndex: D,
      startPos: s ? h.clientX : h.clientY,
      startSizeBefore: v[D],
      startSizeAfter: v[D + 1],
      childBefore: $,
      childAfter: O
    };
    const y = (w) => {
      const k = C.current;
      if (!k || !g.current) return;
      const P = (s ? w.clientX : w.clientY) - k.startPos, K = k.childBefore.minSize || 0, Q = k.childAfter.minSize || 0;
      let ae = k.startSizeBefore + P, q = k.startSizeAfter - P;
      ae < K && (q += ae - K, ae = K), q < Q && (ae += q - Q, q = Q), g.current[k.splitterIndex] = ae, g.current[k.splitterIndex + 1] = q;
      const oe = S.querySelectorAll(":scope > .tlSplitPanel__child"), ie = oe[k.splitterIndex], te = oe[k.splitterIndex + 1];
      ie && (ie.style.flex = `0 0 ${ae}px`), te && (te.style.flex = `0 0 ${q}px`);
    }, Y = () => {
      if (document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", Y), document.body.style.cursor = "", document.body.style.userSelect = "", g.current) {
        const w = {};
        u.forEach((k, A) => {
          const P = k.control;
          P != null && P.controlId && g.current && (w[P.controlId] = g.current[A]);
        }), l("updateSizes", { sizes: w });
      }
      g.current = null, C.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", Y), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, s, l]), b = [];
  return u.forEach((h, D) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${D}`,
          className: `tlSplitPanel__child${h.collapsed && n ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(h)
        },
        /* @__PURE__ */ e.createElement(V, { control: h.control })
      )
    ), i && D < u.length - 1) {
      const S = u[D + 1];
      !h.collapsed && !S.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${D}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (O) => T(O, D)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: a,
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
}, { useCallback: we } = e, St = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Dt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Rt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), xt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = ce(St), i = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, d = t.showMaximize === !0, c = t.showPopOut === !0, n = t.toolbarButtons ?? [], p = u === "MINIMIZED", C = u === "MAXIMIZED", g = u === "HIDDEN", L = we(() => {
    l("toggleMinimize");
  }, [l]), T = we(() => {
    l("toggleMaximize");
  }, [l]), b = we(() => {
    l("popOut");
  }, [l]);
  if (g)
    return null;
  const h = C ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${u.toLowerCase()}`,
      style: h
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, n.map((D, S) => /* @__PURE__ */ e.createElement("span", { key: S, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(V, { control: D }))), s && !C && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Tt, null) : /* @__PURE__ */ e.createElement(Nt, null)
    ), d && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: T,
        title: C ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      C ? /* @__PURE__ */ e.createElement(Dt, null) : /* @__PURE__ */ e.createElement(Lt, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Rt, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(V, { control: t.child }))
  );
}, Pt = ({ controlId: a }) => {
  const t = H();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(V, { control: t.child })
  );
}, jt = ({ controlId: a }) => {
  const t = H();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(V, { control: t.activeChild }));
}, { useCallback: se, useState: ve, useEffect: Ee, useRef: ge } = e, Mt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Le(a, t, l, r) {
  const i = [];
  for (const u of a)
    u.type === "nav" ? i.push({ id: u.id, type: "nav", groupId: r }) : u.type === "command" ? i.push({ id: u.id, type: "command", groupId: r }) : u.type === "group" && (i.push({ id: u.id, type: "group" }), (l.get(u.id) ?? u.expanded) && !t && i.push(...Le(u.children, t, l, u.id)));
  return i;
}
const fe = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, At = ({ item: a, active: t, collapsed: l, onSelect: r, tabIndex: i, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(a.id),
    title: l ? a.label : void 0,
    tabIndex: i,
    ref: u,
    onFocus: () => s(a.id)
  },
  l && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(fe, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(fe, { icon: a.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
  !l && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
), Ot = ({ item: a, collapsed: t, onExecute: l, tabIndex: r, itemRef: i, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(a.id),
    title: t ? a.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => u(a.id)
  },
  /* @__PURE__ */ e.createElement(fe, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), Bt = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(fe, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), It = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ft = ({ item: a, activeItemId: t, anchorRect: l, onSelect: r, onExecute: i, onClose: u }) => {
  const s = ge(null);
  Ee(() => {
    const n = (p) => {
      s.current && !s.current.contains(p.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", n), () => document.removeEventListener("mousedown", n);
  }, [u]), Ee(() => {
    const n = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", n), () => document.removeEventListener("keydown", n);
  }, [u]);
  const d = se((n) => {
    n.type === "nav" ? (r(n.id), u()) : n.type === "command" && (i(n.id), u());
  }, [r, i, u]), c = {};
  return l && (c.left = l.right, c.top = l.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, a.label), a.children.map((n) => {
    if (n.type === "nav" || n.type === "command") {
      const p = n.type === "nav" && n.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: n.id,
          className: "tlSidebar__flyoutItem" + (p ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(n)
        },
        /* @__PURE__ */ e.createElement(fe, { icon: n.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
        n.type === "nav" && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
      );
    }
    return n.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: n.id, className: "tlSidebar__flyoutSectionHeader" }, n.label) : n.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: n.id, className: "tlSidebar__separator" }) : null;
  }));
}, $t = ({
  item: a,
  expanded: t,
  activeItemId: l,
  collapsed: r,
  onSelect: i,
  onExecute: u,
  onToggleGroup: s,
  tabIndex: d,
  itemRef: c,
  onFocus: n,
  focusedId: p,
  setItemRef: C,
  onItemFocus: g,
  flyoutGroupId: L,
  onOpenFlyout: T,
  onCloseFlyout: b
}) => {
  const h = ge(null), [D, S] = ve(null), $ = se(() => {
    r ? L === a.id ? b() : (h.current && S(h.current.getBoundingClientRect()), T(a.id)) : s(a.id);
  }, [r, L, a.id, s, T, b]), O = se((v) => {
    h.current = v, c(v);
  }, [c]), m = r && L === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (m ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: $,
      title: r ? a.label : void 0,
      "aria-expanded": r ? m : t,
      tabIndex: d,
      ref: O,
      onFocus: () => n(a.id)
    },
    /* @__PURE__ */ e.createElement(fe, { icon: a.icon }),
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
  ), m && /* @__PURE__ */ e.createElement(
    Ft,
    {
      item: a,
      activeItemId: l,
      anchorRect: D,
      onSelect: i,
      onExecute: u,
      onClose: b
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, a.children.map((v) => /* @__PURE__ */ e.createElement(
    He,
    {
      key: v.id,
      item: v,
      activeItemId: l,
      collapsed: r,
      onSelect: i,
      onExecute: u,
      onToggleGroup: s,
      focusedId: p,
      setItemRef: C,
      onItemFocus: g,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: T,
      onCloseFlyout: b
    }
  ))));
}, He = ({
  item: a,
  activeItemId: t,
  collapsed: l,
  onSelect: r,
  onExecute: i,
  onToggleGroup: u,
  focusedId: s,
  setItemRef: d,
  onItemFocus: c,
  groupStates: n,
  flyoutGroupId: p,
  onOpenFlyout: C,
  onCloseFlyout: g
}) => {
  switch (a.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        At,
        {
          item: a,
          active: a.id === t,
          collapsed: l,
          onSelect: r,
          tabIndex: s === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: c
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ot,
        {
          item: a,
          collapsed: l,
          onExecute: i,
          tabIndex: s === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Bt, { item: a, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(It, null);
    case "group": {
      const L = n ? n.get(a.id) ?? a.expanded : a.expanded;
      return /* @__PURE__ */ e.createElement(
        $t,
        {
          item: a,
          expanded: L,
          activeItemId: t,
          collapsed: l,
          onSelect: r,
          onExecute: i,
          onToggleGroup: u,
          tabIndex: s === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: c,
          focusedId: s,
          setItemRef: d,
          onItemFocus: c,
          flyoutGroupId: p,
          onOpenFlyout: C,
          onCloseFlyout: g
        }
      );
    }
    default:
      return null;
  }
}, zt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = ce(Mt), i = t.items ?? [], u = t.activeItemId, s = t.collapsed, [d, c] = ve(() => {
    const w = /* @__PURE__ */ new Map(), k = (A) => {
      for (const P of A)
        P.type === "group" && (w.set(P.id, P.expanded), k(P.children));
    };
    return k(i), w;
  }), n = se((w) => {
    c((k) => {
      const A = new Map(k), P = A.get(w) ?? !1;
      return A.set(w, !P), l("toggleGroup", { itemId: w, expanded: !P }), A;
    });
  }, [l]), p = se((w) => {
    w !== u && l("selectItem", { itemId: w });
  }, [l, u]), C = se((w) => {
    l("executeCommand", { itemId: w });
  }, [l]), g = se(() => {
    l("toggleCollapse", {});
  }, [l]), [L, T] = ve(null), b = se((w) => {
    T(w);
  }, []), h = se(() => {
    T(null);
  }, []);
  Ee(() => {
    s || T(null);
  }, [s]);
  const [D, S] = ve(() => {
    const w = Le(i, s, d);
    return w.length > 0 ? w[0].id : "";
  }), $ = ge(/* @__PURE__ */ new Map()), O = se((w) => (k) => {
    k ? $.current.set(w, k) : $.current.delete(w);
  }, []), m = se((w) => {
    S(w);
  }, []), v = ge(0), y = se((w) => {
    S(w), v.current++;
  }, []);
  Ee(() => {
    const w = $.current.get(D);
    w && document.activeElement !== w && w.focus();
  }, [D, v.current]);
  const Y = se((w) => {
    if (w.key === "Escape" && L !== null) {
      w.preventDefault(), h();
      return;
    }
    const k = Le(i, s, d);
    if (k.length === 0) return;
    const A = k.findIndex((K) => K.id === D);
    if (A < 0) return;
    const P = k[A];
    switch (w.key) {
      case "ArrowDown": {
        w.preventDefault();
        const K = (A + 1) % k.length;
        y(k[K].id);
        break;
      }
      case "ArrowUp": {
        w.preventDefault();
        const K = (A - 1 + k.length) % k.length;
        y(k[K].id);
        break;
      }
      case "Home": {
        w.preventDefault(), y(k[0].id);
        break;
      }
      case "End": {
        w.preventDefault(), y(k[k.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        w.preventDefault(), P.type === "nav" ? p(P.id) : P.type === "command" ? C(P.id) : P.type === "group" && (s ? L === P.id ? h() : b(P.id) : n(P.id));
        break;
      }
      case "ArrowRight": {
        P.type === "group" && !s && ((d.get(P.id) ?? !1) || (w.preventDefault(), n(P.id)));
        break;
      }
      case "ArrowLeft": {
        P.type === "group" && !s && (d.get(P.id) ?? !1) && (w.preventDefault(), n(P.id));
        break;
      }
    }
  }, [
    i,
    s,
    d,
    D,
    L,
    y,
    p,
    C,
    n,
    b,
    h
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: Y }, i.map((w) => /* @__PURE__ */ e.createElement(
    He,
    {
      key: w.id,
      item: w,
      activeItemId: u,
      collapsed: s,
      onSelect: p,
      onExecute: C,
      onToggleGroup: n,
      focusedId: D,
      setItemRef: O,
      onItemFocus: m,
      groupStates: d,
      flyoutGroupId: L,
      onOpenFlyout: b,
      onCloseFlyout: h
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: g,
      title: s ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: s ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, Ut = ({ controlId: a }) => {
  const t = H(), l = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", u = t.wrap === !0, s = t.children ?? [], d = [
    "tlStack",
    `tlStack--${l}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: d }, s.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c })));
}, Ht = ({ controlId: a }) => {
  const t = H(), l = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", u = t.children ?? [], s = {};
  return r ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : l && (s.gridTemplateColumns = `repeat(${l}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${i}`, style: s }, u.map((d, c) => /* @__PURE__ */ e.createElement(V, { key: c, control: d })));
}, Vt = ({ controlId: a }) => {
  const t = H(), l = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, d = l != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${r}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, l && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, l), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(V, { control: s })));
}, Wt = ({ controlId: a }) => {
  const t = H(), l = t.title ?? "", r = t.leading, i = t.actions ?? [], u = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: d }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(V, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, l), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c }))));
}, { useCallback: Kt } = e, Yt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.items ?? [], i = Kt((u) => {
    l("navigate", { itemId: u });
  }, [l]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((u, s) => {
    const d = s === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: u.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => i(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: Gt } = e, qt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.items ?? [], i = t.activeItemId, u = Gt((s) => {
    s !== i && l("selectItem", { itemId: s });
  }, [l, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((s) => {
    const d = s.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => u(s.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: Me, useEffect: Ae, useRef: Xt } = e, Zt = {
  "js.dialog.close": "Close"
}, Qt = ({ controlId: a }) => {
  const t = H(), l = ee(), r = ce(Zt), i = t.open === !0, u = t.title ?? "", s = t.size ?? "medium", d = t.closeOnBackdrop !== !1, c = t.actions ?? [], n = t.child, p = Xt(null), C = Me(() => {
    l("close");
  }, [l]), g = Me((T) => {
    d && T.target === T.currentTarget && C();
  }, [d, C]);
  if (Ae(() => {
    if (!i) return;
    const T = (b) => {
      b.key === "Escape" && C();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [i, C]), Ae(() => {
    i && p.current && p.current.focus();
  }, [i]), !i) return null;
  const L = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDialog__backdrop", onClick: g }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${s}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": L,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: L }, u), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: C,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(V, { control: n })),
    c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, c.map((T, b) => /* @__PURE__ */ e.createElement(V, { key: b, control: T })))
  ));
}, { useCallback: Jt, useEffect: en } = e, tn = {
  "js.drawer.close": "Close"
}, nn = ({ controlId: a }) => {
  const t = H(), l = ee(), r = ce(tn), i = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", d = t.title ?? null, c = t.child, n = Jt(() => {
    l("close");
  }, [l]);
  en(() => {
    if (!i) return;
    const C = (g) => {
      g.key === "Escape" && n();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [i, n]);
  const p = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${s}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: a, className: p, "aria-hidden": !i }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, c && /* @__PURE__ */ e.createElement(V, { control: c })));
}, { useCallback: Oe, useEffect: rn, useState: an } = e, ln = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.message ?? "", i = t.content ?? "", u = t.variant ?? "info", s = t.action, d = t.duration ?? 5e3, c = t.visible === !0, [n, p] = an(!1), C = Oe(() => {
    p(!0), setTimeout(() => {
      l("dismiss"), p(!1);
    }, 200);
  }, [l]), g = Oe(() => {
    s && l(s.commandName), C();
  }, [l, s, C]);
  return rn(() => {
    if (!c || d === 0) return;
    const L = setTimeout(C, d);
    return () => clearTimeout(L);
  }, [c, d, C]), !c && !n ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlSnackbar tlSnackbar--${u}${n ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    s && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: g }, s.label)
  );
}, { useCallback: ke, useEffect: Se, useRef: on, useState: Be } = e, sn = ({ controlId: a }) => {
  const t = H(), l = ee(), r = t.open === !0, i = t.anchorId, u = t.items ?? [], s = on(null), [d, c] = Be({ top: 0, left: 0 }), [n, p] = Be(0), C = u.filter((b) => b.type === "item" && !b.disabled);
  Se(() => {
    var m, v;
    if (!r || !i) return;
    const b = document.getElementById(i);
    if (!b) return;
    const h = b.getBoundingClientRect(), D = ((m = s.current) == null ? void 0 : m.offsetHeight) ?? 200, S = ((v = s.current) == null ? void 0 : v.offsetWidth) ?? 200;
    let $ = h.bottom + 4, O = h.left;
    $ + D > window.innerHeight && ($ = h.top - D - 4), O + S > window.innerWidth && (O = h.right - S), c({ top: $, left: O }), p(0);
  }, [r, i]);
  const g = ke(() => {
    l("close");
  }, [l]), L = ke((b) => {
    l("selectItem", { itemId: b });
  }, [l]);
  Se(() => {
    if (!r) return;
    const b = (h) => {
      s.current && !s.current.contains(h.target) && g();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [r, g]);
  const T = ke((b) => {
    if (b.key === "Escape") {
      g();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), p((h) => (h + 1) % C.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), p((h) => (h - 1 + C.length) % C.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const h = C[n];
      h && L(h.id);
    }
  }, [g, L, C, n]);
  return Se(() => {
    r && s.current && s.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: T
    },
    u.map((b, h) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: h, className: "tlMenu__separator" });
      const S = C.indexOf(b) === n;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (S ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: S ? 0 : -1,
          onClick: () => L(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, cn = ({ controlId: a }) => {
  const t = H(), l = t.header, r = t.content, i = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(V, { control: l })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(V, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(V, { control: i })), /* @__PURE__ */ e.createElement(V, { control: u }));
}, un = () => {
  const t = H().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, dn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ie = 50, mn = () => {
  const a = H(), t = ee(), l = ce(dn), r = a.columns ?? [], i = a.totalRowCount ?? 0, u = a.rows ?? [], s = a.rowHeight ?? 36, d = a.selectionMode ?? "single", c = a.selectedCount ?? 0, n = a.frozenColumnCount ?? 0, p = a.treeMode ?? !1, C = e.useMemo(
    () => r.filter((E) => E.sortPriority && E.sortPriority > 0).length,
    [r]
  ), g = d === "multi", L = 40, T = 20, b = e.useRef(null), h = e.useRef(null), D = e.useRef(null), [S, $] = e.useState({}), O = e.useRef(null), m = e.useRef(!1), v = e.useRef(null), [y, Y] = e.useState(null), [w, k] = e.useState(null);
  e.useEffect(() => {
    O.current || $({});
  }, [r]);
  const A = e.useCallback((E) => S[E.name] ?? E.width, [S]), P = e.useMemo(() => {
    const E = [];
    let N = g && n > 0 ? L : 0;
    for (let I = 0; I < n && I < r.length; I++)
      E.push(N), N += A(r[I]);
    return E;
  }, [r, n, g, L, A]), K = i * s, Q = e.useCallback((E, N, I) => {
    I.preventDefault(), I.stopPropagation(), O.current = { column: E, startX: I.clientX, startWidth: N };
    const X = (_) => {
      const R = O.current;
      if (!R) return;
      const M = Math.max(Ie, R.startWidth + (_.clientX - R.startX));
      $((G) => ({ ...G, [R.column]: M }));
    }, Z = (_) => {
      document.removeEventListener("mousemove", X), document.removeEventListener("mouseup", Z);
      const R = O.current;
      if (R) {
        const M = Math.max(Ie, R.startWidth + (_.clientX - R.startX));
        t("columnResize", { column: R.column, width: M }), O.current = null, m.current = !0, requestAnimationFrame(() => {
          m.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", X), document.addEventListener("mouseup", Z);
  }, [t]), ae = e.useCallback(() => {
    b.current && h.current && (b.current.scrollLeft = h.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const E = h.current;
      if (!E) return;
      const N = E.scrollTop, I = Math.ceil(E.clientHeight / s), X = Math.floor(N / s);
      t("scroll", { start: X, count: I });
    }, 80);
  }, [t, s]), q = e.useCallback((E, N, I) => {
    if (m.current) return;
    let X;
    !N || N === "desc" ? X = "asc" : X = "desc";
    const Z = I.shiftKey ? "add" : "replace";
    t("sort", { column: E, direction: X, mode: Z });
  }, [t]), oe = e.useCallback((E, N) => {
    v.current = E, N.dataTransfer.effectAllowed = "move", N.dataTransfer.setData("text/plain", E);
  }, []), ie = e.useCallback((E, N) => {
    if (!v.current || v.current === E) {
      Y(null);
      return;
    }
    N.preventDefault(), N.dataTransfer.dropEffect = "move";
    const I = N.currentTarget.getBoundingClientRect(), X = N.clientX < I.left + I.width / 2 ? "left" : "right";
    Y({ column: E, side: X });
  }, []), te = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation();
    const N = v.current;
    if (!N || !y) {
      v.current = null, Y(null);
      return;
    }
    let I = r.findIndex((Z) => Z.name === y.column);
    if (I < 0) {
      v.current = null, Y(null);
      return;
    }
    const X = r.findIndex((Z) => Z.name === N);
    y.side === "right" && I++, X < I && I--, t("columnReorder", { column: N, targetIndex: I }), v.current = null, Y(null);
  }, [r, y, t]), ue = e.useCallback(() => {
    v.current = null, Y(null);
  }, []), o = e.useCallback((E, N) => {
    N.shiftKey && N.preventDefault(), t("select", {
      rowIndex: E,
      ctrlKey: N.ctrlKey || N.metaKey,
      shiftKey: N.shiftKey
    });
  }, [t]), f = e.useCallback((E, N) => {
    N.stopPropagation(), t("select", { rowIndex: E, ctrlKey: !0, shiftKey: !1 });
  }, [t]), j = e.useCallback(() => {
    const E = c === i && i > 0;
    t("selectAll", { selected: !E });
  }, [t, c, i]), x = e.useCallback((E, N, I) => {
    I.stopPropagation(), t("expand", { rowIndex: E, expanded: N });
  }, [t]), z = e.useCallback((E, N) => {
    N.preventDefault(), k({ x: N.clientX, y: N.clientY, colIdx: E });
  }, []), U = e.useCallback(() => {
    w && (t("setFrozenColumnCount", { count: w.colIdx + 1 }), k(null));
  }, [w, t]), W = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), k(null);
  }, [t]);
  e.useEffect(() => {
    if (!w) return;
    const E = () => k(null), N = (I) => {
      I.key === "Escape" && k(null);
    };
    return document.addEventListener("mousedown", E), document.addEventListener("keydown", N), () => {
      document.removeEventListener("mousedown", E), document.removeEventListener("keydown", N);
    };
  }, [w]);
  const ne = r.reduce((E, N) => E + A(N), 0) + (g ? L : 0), J = c === i && i > 0, me = c > 0 && c < i, ye = e.useCallback((E) => {
    E && (E.indeterminate = me);
  }, [me]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (E) => {
        if (!v.current) return;
        E.preventDefault();
        const N = h.current, I = b.current;
        if (!N) return;
        const X = N.getBoundingClientRect(), Z = 40, _ = 8;
        E.clientX < X.left + Z ? N.scrollLeft = Math.max(0, N.scrollLeft - _) : E.clientX > X.right - Z && (N.scrollLeft += _), I && (I.scrollLeft = N.scrollLeft);
      },
      onDrop: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ne } }, g && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: L,
          minWidth: L,
          ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (E) => {
          v.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", r.length > 0 && r[0].name !== v.current && Y({ column: r[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ye,
          className: "tlTableView__checkbox",
          checked: J,
          onChange: j
        }
      )
    ), r.map((E, N) => {
      const I = A(E), X = N === r.length - 1;
      let Z = "tlTableView__headerCell";
      E.sortable && (Z += " tlTableView__headerCell--sortable"), y && y.column === E.name && (Z += " tlTableView__headerCell--dragOver-" + y.side);
      const _ = N < n, R = N === n - 1;
      return _ && (Z += " tlTableView__headerCell--frozen"), R && (Z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.name,
          className: Z,
          style: {
            ...X && !_ ? { flex: "1 0 auto", minWidth: I } : { width: I, minWidth: I },
            position: _ ? "sticky" : "relative",
            ..._ ? { left: P[N], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: E.sortable ? (M) => q(E.name, E.sortDirection, M) : void 0,
          onContextMenu: (M) => z(N, M),
          onDragStart: (M) => oe(E.name, M),
          onDragOver: (M) => ie(E.name, M),
          onDrop: te,
          onDragEnd: ue
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, E.label),
        E.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, E.sortDirection === "asc" ? "▲" : "▼", C > 1 && E.sortPriority != null && E.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, E.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (M) => Q(E.name, I, M)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (E) => {
          if (v.current && r.length > 0) {
            const N = r[r.length - 1];
            N.name !== v.current && (E.preventDefault(), E.dataTransfer.dropEffect = "move", Y({ column: N.name, side: "right" }));
          }
        },
        onDrop: te
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: h,
        className: "tlTableView__body",
        onScroll: ae
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: K, position: "relative", minWidth: ne } }, u.map((E) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: E.id,
          className: "tlTableView__row" + (E.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: E.index * s,
            height: s,
            minWidth: ne,
            width: "100%"
          },
          onClick: (N) => o(E.index, N)
        },
        g && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: L,
              minWidth: L,
              ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (N) => N.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: E.selected,
              onChange: () => {
              },
              onClick: (N) => f(E.index, N),
              tabIndex: -1
            }
          )
        ),
        r.map((N, I) => {
          const X = A(N), Z = I === r.length - 1, _ = I < n, R = I === n - 1;
          let M = "tlTableView__cell";
          _ && (M += " tlTableView__cell--frozen"), R && (M += " tlTableView__cell--frozenLast");
          const G = p && I === 0, he = E.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: N.name,
              className: M,
              style: {
                ...Z && !_ ? { flex: "1 0 auto", minWidth: X } : { width: X, minWidth: X },
                ..._ ? { position: "sticky", left: P[I], zIndex: 2 } : {}
              }
            },
            G ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: he * T } }, E.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (We) => x(E.index, !E.expanded, We)
              },
              E.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(V, { control: E.cells[N.name] })) : /* @__PURE__ */ e.createElement(V, { control: E.cells[N.name] })
          );
        })
      )))
    ),
    w && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: w.y, left: w.x, zIndex: 1e4 },
        onMouseDown: (E) => E.stopPropagation()
      },
      w.colIdx + 1 !== n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: U }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.freezeUpTo"])),
      n > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: W }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.unfreezeAll"]))
    )
  );
}, pn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ve = e.createContext(pn), { useMemo: fn, useRef: hn, useState: bn, useEffect: _n } = e, vn = 320, En = ({ controlId: a }) => {
  const t = H(), l = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, d = hn(null), [c, n] = bn(
    r === "top" ? "top" : "side"
  );
  _n(() => {
    if (r !== "auto") {
      n(r);
      return;
    }
    const T = d.current;
    if (!T) return;
    const b = new ResizeObserver((h) => {
      for (const D of h) {
        const $ = D.contentRect.width / l;
        n($ < vn ? "top" : "side");
      }
    });
    return b.observe(T), () => b.disconnect();
  }, [r, l]);
  const p = fn(() => ({
    readOnly: i,
    resolvedLabelPosition: c
  }), [i, c]), g = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / l))}rem`}, 1fr))`
  }, L = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(Ve.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: a, className: L, style: g, ref: d }, u.map((T, b) => /* @__PURE__ */ e.createElement(V, { key: b, control: T }))));
}, { useCallback: gn } = e, yn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Cn = ({ controlId: a }) => {
  const t = H(), l = ee(), r = ce(yn), i = t.header, u = t.headerActions ?? [], s = t.collapsible === !0, d = t.collapsed === !0, c = t.border ?? "none", n = t.fullLine === !0, p = t.children ?? [], C = i != null || u.length > 0 || s, g = gn(() => {
    l("toggleCollapse");
  }, [l]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    n ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: L }, C && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: g,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, i), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((T, b) => /* @__PURE__ */ e.createElement(V, { key: b, control: T })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((T, b) => /* @__PURE__ */ e.createElement(V, { key: b, control: T }))));
}, { useContext: wn, useState: kn, useCallback: Sn } = e, Nn = ({ controlId: a }) => {
  const t = H(), l = wn(Ve), r = t.label ?? "", i = t.required === !0, u = t.error, s = t.helpText, d = t.dirty === !0, c = t.labelPosition ?? l.resolvedLabelPosition, n = t.fullLine === !0, p = t.visible !== !1, C = t.field, g = l.readOnly, [L, T] = kn(!1), b = Sn(() => T((S) => !S), []);
  if (!p) return null;
  const h = u != null, D = [
    "tlFormField",
    `tlFormField--${c}`,
    g ? "tlFormField--readonly" : "",
    n ? "tlFormField--fullLine" : "",
    h ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), i && !g && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), s && !g && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(V, { control: C })), !g && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, u)), !g && s && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, s));
}, Tn = () => {
  const a = H(), t = ee(), l = a.iconCss, r = a.iconSrc, i = a.label, u = a.cssClass, s = a.tooltip, d = a.hasLink, c = l ? /* @__PURE__ */ e.createElement("i", { className: l }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, n = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), p = e.useCallback((g) => {
    g.preventDefault(), t("goto", {});
  }, [t]), C = ["tlResourceCell", u].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: C, href: "#", onClick: p, title: s }, n) : /* @__PURE__ */ e.createElement("span", { className: C, title: s }, n);
}, Ln = 20, Dn = () => {
  const a = H(), t = ee(), l = a.nodes ?? [], r = a.selectionMode ?? "single", i = a.dragEnabled ?? !1, u = a.dropEnabled ?? !1, s = a.dropIndicatorNodeId ?? null, d = a.dropIndicatorPosition ?? null, [c, n] = e.useState(-1), p = e.useRef(null), C = e.useCallback((m, v) => {
    t(v ? "collapse" : "expand", { nodeId: m });
  }, [t]), g = e.useCallback((m, v) => {
    t("select", {
      nodeId: m,
      ctrlKey: v.ctrlKey || v.metaKey,
      shiftKey: v.shiftKey
    });
  }, [t]), L = e.useCallback((m, v) => {
    v.preventDefault(), t("contextMenu", { nodeId: m, x: v.clientX, y: v.clientY });
  }, [t]), T = e.useRef(null), b = e.useCallback((m, v) => {
    const y = v.getBoundingClientRect(), Y = m.clientY - y.top, w = y.height / 3;
    return Y < w ? "above" : Y > w * 2 ? "below" : "within";
  }, []), h = e.useCallback((m, v) => {
    v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", m);
  }, []), D = e.useCallback((m, v) => {
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const y = b(v, v.currentTarget);
    T.current != null && window.clearTimeout(T.current), T.current = window.setTimeout(() => {
      t("dragOver", { nodeId: m, position: y }), T.current = null;
    }, 50);
  }, [t, b]), S = e.useCallback((m, v) => {
    v.preventDefault(), T.current != null && (window.clearTimeout(T.current), T.current = null);
    const y = b(v, v.currentTarget);
    t("drop", { nodeId: m, position: y });
  }, [t, b]), $ = e.useCallback(() => {
    T.current != null && (window.clearTimeout(T.current), T.current = null), t("dragEnd");
  }, [t]), O = e.useCallback((m) => {
    if (l.length === 0) return;
    let v = c;
    switch (m.key) {
      case "ArrowDown":
        m.preventDefault(), v = Math.min(c + 1, l.length - 1);
        break;
      case "ArrowUp":
        m.preventDefault(), v = Math.max(c - 1, 0);
        break;
      case "ArrowRight":
        if (m.preventDefault(), c >= 0 && c < l.length) {
          const y = l[c];
          if (y.expandable && !y.expanded) {
            t("expand", { nodeId: y.id });
            return;
          } else y.expanded && (v = c + 1);
        }
        break;
      case "ArrowLeft":
        if (m.preventDefault(), c >= 0 && c < l.length) {
          const y = l[c];
          if (y.expanded) {
            t("collapse", { nodeId: y.id });
            return;
          } else {
            const Y = y.depth;
            for (let w = c - 1; w >= 0; w--)
              if (l[w].depth < Y) {
                v = w;
                break;
              }
          }
        }
        break;
      case "Enter":
        m.preventDefault(), c >= 0 && c < l.length && t("select", {
          nodeId: l[c].id,
          ctrlKey: m.ctrlKey || m.metaKey,
          shiftKey: m.shiftKey
        });
        return;
      case " ":
        m.preventDefault(), r === "multi" && c >= 0 && c < l.length && t("select", {
          nodeId: l[c].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        m.preventDefault(), v = 0;
        break;
      case "End":
        m.preventDefault(), v = l.length - 1;
        break;
      default:
        return;
    }
    v !== c && n(v);
  }, [c, l, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: O
    },
    l.map((m, v) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: m.id,
        role: "treeitem",
        "aria-expanded": m.expandable ? m.expanded : void 0,
        "aria-selected": m.selected,
        "aria-level": m.depth + 1,
        className: [
          "tlTreeView__node",
          m.selected ? "tlTreeView__node--selected" : "",
          v === c ? "tlTreeView__node--focused" : "",
          s === m.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          s === m.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          s === m.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: m.depth * Ln },
        draggable: i,
        onClick: (y) => g(m.id, y),
        onContextMenu: (y) => L(m.id, y),
        onDragStart: (y) => h(m.id, y),
        onDragOver: u ? (y) => D(m.id, y) : void 0,
        onDrop: u ? (y) => S(m.id, y) : void 0,
        onDragEnd: $
      },
      m.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (y) => {
            y.stopPropagation(), C(m.id, m.expanded);
          },
          tabIndex: -1,
          "aria-label": m.expanded ? "Collapse" : "Expand"
        },
        m.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: m.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(V, { control: m.content }))
    ))
  );
};
var Ne = { exports: {} }, re = {}, Te = { exports: {} }, B = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Fe;
function Rn() {
  if (Fe) return B;
  Fe = 1;
  var a = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), l = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), c = Symbol.for("react.suspense"), n = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), C = Symbol.for("react.activity"), g = Symbol.iterator;
  function L(o) {
    return o === null || typeof o != "object" ? null : (o = g && o[g] || o["@@iterator"], typeof o == "function" ? o : null);
  }
  var T = {
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
  function D(o, f, j) {
    this.props = o, this.context = f, this.refs = h, this.updater = j || T;
  }
  D.prototype.isReactComponent = {}, D.prototype.setState = function(o, f) {
    if (typeof o != "object" && typeof o != "function" && o != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, o, f, "setState");
  }, D.prototype.forceUpdate = function(o) {
    this.updater.enqueueForceUpdate(this, o, "forceUpdate");
  };
  function S() {
  }
  S.prototype = D.prototype;
  function $(o, f, j) {
    this.props = o, this.context = f, this.refs = h, this.updater = j || T;
  }
  var O = $.prototype = new S();
  O.constructor = $, b(O, D.prototype), O.isPureReactComponent = !0;
  var m = Array.isArray;
  function v() {
  }
  var y = { H: null, A: null, T: null, S: null }, Y = Object.prototype.hasOwnProperty;
  function w(o, f, j) {
    var x = j.ref;
    return {
      $$typeof: a,
      type: o,
      key: f,
      ref: x !== void 0 ? x : null,
      props: j
    };
  }
  function k(o, f) {
    return w(o.type, f, o.props);
  }
  function A(o) {
    return typeof o == "object" && o !== null && o.$$typeof === a;
  }
  function P(o) {
    var f = { "=": "=0", ":": "=2" };
    return "$" + o.replace(/[=:]/g, function(j) {
      return f[j];
    });
  }
  var K = /\/+/g;
  function Q(o, f) {
    return typeof o == "object" && o !== null && o.key != null ? P("" + o.key) : f.toString(36);
  }
  function ae(o) {
    switch (o.status) {
      case "fulfilled":
        return o.value;
      case "rejected":
        throw o.reason;
      default:
        switch (typeof o.status == "string" ? o.then(v, v) : (o.status = "pending", o.then(
          function(f) {
            o.status === "pending" && (o.status = "fulfilled", o.value = f);
          },
          function(f) {
            o.status === "pending" && (o.status = "rejected", o.reason = f);
          }
        )), o.status) {
          case "fulfilled":
            return o.value;
          case "rejected":
            throw o.reason;
        }
    }
    throw o;
  }
  function q(o, f, j, x, z) {
    var U = typeof o;
    (U === "undefined" || U === "boolean") && (o = null);
    var W = !1;
    if (o === null) W = !0;
    else
      switch (U) {
        case "bigint":
        case "string":
        case "number":
          W = !0;
          break;
        case "object":
          switch (o.$$typeof) {
            case a:
            case t:
              W = !0;
              break;
            case p:
              return W = o._init, q(
                W(o._payload),
                f,
                j,
                x,
                z
              );
          }
      }
    if (W)
      return z = z(o), W = x === "" ? "." + Q(o, 0) : x, m(z) ? (j = "", W != null && (j = W.replace(K, "$&/") + "/"), q(z, f, j, "", function(me) {
        return me;
      })) : z != null && (A(z) && (z = k(
        z,
        j + (z.key == null || o && o.key === z.key ? "" : ("" + z.key).replace(
          K,
          "$&/"
        ) + "/") + W
      )), f.push(z)), 1;
    W = 0;
    var ne = x === "" ? "." : x + ":";
    if (m(o))
      for (var J = 0; J < o.length; J++)
        x = o[J], U = ne + Q(x, J), W += q(
          x,
          f,
          j,
          U,
          z
        );
    else if (J = L(o), typeof J == "function")
      for (o = J.call(o), J = 0; !(x = o.next()).done; )
        x = x.value, U = ne + Q(x, J++), W += q(
          x,
          f,
          j,
          U,
          z
        );
    else if (U === "object") {
      if (typeof o.then == "function")
        return q(
          ae(o),
          f,
          j,
          x,
          z
        );
      throw f = String(o), Error(
        "Objects are not valid as a React child (found: " + (f === "[object Object]" ? "object with keys {" + Object.keys(o).join(", ") + "}" : f) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return W;
  }
  function oe(o, f, j) {
    if (o == null) return o;
    var x = [], z = 0;
    return q(o, x, "", "", function(U) {
      return f.call(j, U, z++);
    }), x;
  }
  function ie(o) {
    if (o._status === -1) {
      var f = o._result;
      f = f(), f.then(
        function(j) {
          (o._status === 0 || o._status === -1) && (o._status = 1, o._result = j);
        },
        function(j) {
          (o._status === 0 || o._status === -1) && (o._status = 2, o._result = j);
        }
      ), o._status === -1 && (o._status = 0, o._result = f);
    }
    if (o._status === 1) return o._result.default;
    throw o._result;
  }
  var te = typeof reportError == "function" ? reportError : function(o) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var f = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof o == "object" && o !== null && typeof o.message == "string" ? String(o.message) : String(o),
        error: o
      });
      if (!window.dispatchEvent(f)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", o);
      return;
    }
    console.error(o);
  }, ue = {
    map: oe,
    forEach: function(o, f, j) {
      oe(
        o,
        function() {
          f.apply(this, arguments);
        },
        j
      );
    },
    count: function(o) {
      var f = 0;
      return oe(o, function() {
        f++;
      }), f;
    },
    toArray: function(o) {
      return oe(o, function(f) {
        return f;
      }) || [];
    },
    only: function(o) {
      if (!A(o))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return o;
    }
  };
  return B.Activity = C, B.Children = ue, B.Component = D, B.Fragment = l, B.Profiler = i, B.PureComponent = $, B.StrictMode = r, B.Suspense = c, B.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = y, B.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(o) {
      return y.H.useMemoCache(o);
    }
  }, B.cache = function(o) {
    return function() {
      return o.apply(null, arguments);
    };
  }, B.cacheSignal = function() {
    return null;
  }, B.cloneElement = function(o, f, j) {
    if (o == null)
      throw Error(
        "The argument must be a React element, but you passed " + o + "."
      );
    var x = b({}, o.props), z = o.key;
    if (f != null)
      for (U in f.key !== void 0 && (z = "" + f.key), f)
        !Y.call(f, U) || U === "key" || U === "__self" || U === "__source" || U === "ref" && f.ref === void 0 || (x[U] = f[U]);
    var U = arguments.length - 2;
    if (U === 1) x.children = j;
    else if (1 < U) {
      for (var W = Array(U), ne = 0; ne < U; ne++)
        W[ne] = arguments[ne + 2];
      x.children = W;
    }
    return w(o.type, z, x);
  }, B.createContext = function(o) {
    return o = {
      $$typeof: s,
      _currentValue: o,
      _currentValue2: o,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, o.Provider = o, o.Consumer = {
      $$typeof: u,
      _context: o
    }, o;
  }, B.createElement = function(o, f, j) {
    var x, z = {}, U = null;
    if (f != null)
      for (x in f.key !== void 0 && (U = "" + f.key), f)
        Y.call(f, x) && x !== "key" && x !== "__self" && x !== "__source" && (z[x] = f[x]);
    var W = arguments.length - 2;
    if (W === 1) z.children = j;
    else if (1 < W) {
      for (var ne = Array(W), J = 0; J < W; J++)
        ne[J] = arguments[J + 2];
      z.children = ne;
    }
    if (o && o.defaultProps)
      for (x in W = o.defaultProps, W)
        z[x] === void 0 && (z[x] = W[x]);
    return w(o, U, z);
  }, B.createRef = function() {
    return { current: null };
  }, B.forwardRef = function(o) {
    return { $$typeof: d, render: o };
  }, B.isValidElement = A, B.lazy = function(o) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: o },
      _init: ie
    };
  }, B.memo = function(o, f) {
    return {
      $$typeof: n,
      type: o,
      compare: f === void 0 ? null : f
    };
  }, B.startTransition = function(o) {
    var f = y.T, j = {};
    y.T = j;
    try {
      var x = o(), z = y.S;
      z !== null && z(j, x), typeof x == "object" && x !== null && typeof x.then == "function" && x.then(v, te);
    } catch (U) {
      te(U);
    } finally {
      f !== null && j.types !== null && (f.types = j.types), y.T = f;
    }
  }, B.unstable_useCacheRefresh = function() {
    return y.H.useCacheRefresh();
  }, B.use = function(o) {
    return y.H.use(o);
  }, B.useActionState = function(o, f, j) {
    return y.H.useActionState(o, f, j);
  }, B.useCallback = function(o, f) {
    return y.H.useCallback(o, f);
  }, B.useContext = function(o) {
    return y.H.useContext(o);
  }, B.useDebugValue = function() {
  }, B.useDeferredValue = function(o, f) {
    return y.H.useDeferredValue(o, f);
  }, B.useEffect = function(o, f) {
    return y.H.useEffect(o, f);
  }, B.useEffectEvent = function(o) {
    return y.H.useEffectEvent(o);
  }, B.useId = function() {
    return y.H.useId();
  }, B.useImperativeHandle = function(o, f, j) {
    return y.H.useImperativeHandle(o, f, j);
  }, B.useInsertionEffect = function(o, f) {
    return y.H.useInsertionEffect(o, f);
  }, B.useLayoutEffect = function(o, f) {
    return y.H.useLayoutEffect(o, f);
  }, B.useMemo = function(o, f) {
    return y.H.useMemo(o, f);
  }, B.useOptimistic = function(o, f) {
    return y.H.useOptimistic(o, f);
  }, B.useReducer = function(o, f, j) {
    return y.H.useReducer(o, f, j);
  }, B.useRef = function(o) {
    return y.H.useRef(o);
  }, B.useState = function(o) {
    return y.H.useState(o);
  }, B.useSyncExternalStore = function(o, f, j) {
    return y.H.useSyncExternalStore(
      o,
      f,
      j
    );
  }, B.useTransition = function() {
    return y.H.useTransition();
  }, B.version = "19.2.4", B;
}
var $e;
function xn() {
  return $e || ($e = 1, Te.exports = Rn()), Te.exports;
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
var ze;
function Pn() {
  if (ze) return re;
  ze = 1;
  var a = xn();
  function t(c) {
    var n = "https://react.dev/errors/" + c;
    if (1 < arguments.length) {
      n += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var p = 2; p < arguments.length; p++)
        n += "&args[]=" + encodeURIComponent(arguments[p]);
    }
    return "Minified React error #" + c + "; visit " + n + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function l() {
  }
  var r = {
    d: {
      f: l,
      r: function() {
        throw Error(t(522));
      },
      D: l,
      C: l,
      L: l,
      m: l,
      X: l,
      S: l,
      M: l
    },
    p: 0,
    findDOMNode: null
  }, i = Symbol.for("react.portal");
  function u(c, n, p) {
    var C = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: C == null ? null : "" + C,
      children: c,
      containerInfo: n,
      implementation: p
    };
  }
  var s = a.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(c, n) {
    if (c === "font") return "";
    if (typeof n == "string")
      return n === "use-credentials" ? n : "";
  }
  return re.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, re.createPortal = function(c, n) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!n || n.nodeType !== 1 && n.nodeType !== 9 && n.nodeType !== 11)
      throw Error(t(299));
    return u(c, n, null, p);
  }, re.flushSync = function(c) {
    var n = s.T, p = r.p;
    try {
      if (s.T = null, r.p = 2, c) return c();
    } finally {
      s.T = n, r.p = p, r.d.f();
    }
  }, re.preconnect = function(c, n) {
    typeof c == "string" && (n ? (n = n.crossOrigin, n = typeof n == "string" ? n === "use-credentials" ? n : "" : void 0) : n = null, r.d.C(c, n));
  }, re.prefetchDNS = function(c) {
    typeof c == "string" && r.d.D(c);
  }, re.preinit = function(c, n) {
    if (typeof c == "string" && n && typeof n.as == "string") {
      var p = n.as, C = d(p, n.crossOrigin), g = typeof n.integrity == "string" ? n.integrity : void 0, L = typeof n.fetchPriority == "string" ? n.fetchPriority : void 0;
      p === "style" ? r.d.S(
        c,
        typeof n.precedence == "string" ? n.precedence : void 0,
        {
          crossOrigin: C,
          integrity: g,
          fetchPriority: L
        }
      ) : p === "script" && r.d.X(c, {
        crossOrigin: C,
        integrity: g,
        fetchPriority: L,
        nonce: typeof n.nonce == "string" ? n.nonce : void 0
      });
    }
  }, re.preinitModule = function(c, n) {
    if (typeof c == "string")
      if (typeof n == "object" && n !== null) {
        if (n.as == null || n.as === "script") {
          var p = d(
            n.as,
            n.crossOrigin
          );
          r.d.M(c, {
            crossOrigin: p,
            integrity: typeof n.integrity == "string" ? n.integrity : void 0,
            nonce: typeof n.nonce == "string" ? n.nonce : void 0
          });
        }
      } else n == null && r.d.M(c);
  }, re.preload = function(c, n) {
    if (typeof c == "string" && typeof n == "object" && n !== null && typeof n.as == "string") {
      var p = n.as, C = d(p, n.crossOrigin);
      r.d.L(c, p, {
        crossOrigin: C,
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
  }, re.preloadModule = function(c, n) {
    if (typeof c == "string")
      if (n) {
        var p = d(n.as, n.crossOrigin);
        r.d.m(c, {
          as: typeof n.as == "string" && n.as !== "script" ? n.as : void 0,
          crossOrigin: p,
          integrity: typeof n.integrity == "string" ? n.integrity : void 0
        });
      } else r.d.m(c);
  }, re.requestFormReset = function(c) {
    r.d.r(c);
  }, re.unstable_batchedUpdates = function(c, n) {
    return c(n);
  }, re.useFormState = function(c, n, p) {
    return s.H.useFormState(c, n, p);
  }, re.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, re.version = "19.2.4", re;
}
var Ue;
function jn() {
  if (Ue) return Ne.exports;
  Ue = 1;
  function a() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(a);
      } catch (t) {
        console.error(t);
      }
  }
  return a(), Ne.exports = Pn(), Ne.exports;
}
var Mn = jn();
const { useState: de, useCallback: le, useRef: be, useEffect: pe, useMemo: De } = e;
function Pe({ image: a }) {
  if (!a) return null;
  if (a.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: a, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = a.startsWith("css:") ? a.substring(4) : a.startsWith("colored:") ? a.substring(8) : a;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function An({
  option: a,
  removable: t,
  onRemove: l,
  removeLabel: r,
  draggable: i,
  onDragStart: u,
  onDragOver: s,
  onDrop: d,
  onDragEnd: c,
  dragClassName: n
}) {
  const p = le(
    (C) => {
      C.stopPropagation(), l(a.value);
    },
    [l, a.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (n ? " " + n : ""),
      draggable: i || void 0,
      onDragStart: u,
      onDragOver: s,
      onDrop: d,
      onDragEnd: c
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Pe, { image: a.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, a.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: p,
        "aria-label": r
      },
      "×"
    )
  );
}
function On({
  option: a,
  highlighted: t,
  searchTerm: l,
  onSelect: r,
  onMouseEnter: i,
  id: u
}) {
  const s = le(() => r(a.value), [r, a.value]), d = De(() => {
    if (!l) return a.label;
    const c = a.label.toLowerCase().indexOf(l.toLowerCase());
    return c < 0 ? a.label : /* @__PURE__ */ e.createElement(e.Fragment, null, a.label.substring(0, c), /* @__PURE__ */ e.createElement("strong", null, a.label.substring(c, c + l.length)), a.label.substring(c + l.length));
  }, [a.label, l]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: u,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(Pe, { image: a.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const Bn = ({ controlId: a, state: t }) => {
  const l = ee(), r = t.value ?? [], i = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, d = t.disabled === !0, c = t.editable !== !1, n = t.optionsLoaded === !0, p = t.options ?? [], C = t.emptyOptionLabel ?? "", g = u && i && !d && c && n, L = ce({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), T = L["js.dropdownSelect.nothingFound"], b = le(
    (_) => L["js.dropdownSelect.removeChip"].replace("{0}", _),
    [L]
  ), [h, D] = de(!1), [S, $] = de(""), [O, m] = de(-1), [v, y] = de(!1), [Y, w] = de({}), [k, A] = de(null), [P, K] = de(null), [Q, ae] = de(null), q = be(null), oe = be(null), ie = be(null), te = be(r);
  te.current = r;
  const ue = be(-1), o = De(
    () => new Set(r.map((_) => _.value)),
    [r]
  ), f = De(() => {
    let _ = p.filter((R) => !o.has(R.value));
    if (S) {
      const R = S.toLowerCase();
      _ = _.filter((M) => M.label.toLowerCase().includes(R));
    }
    return _;
  }, [p, o, S]);
  pe(() => {
    S && f.length === 1 ? m(0) : m(-1);
  }, [f.length, S]), pe(() => {
    h && n && oe.current && oe.current.focus();
  }, [h, n, r]), pe(() => {
    var M, G;
    if (ue.current < 0) return;
    const _ = ue.current;
    ue.current = -1;
    const R = (M = q.current) == null ? void 0 : M.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    R && R.length > 0 ? R[Math.min(_, R.length - 1)].focus() : (G = q.current) == null || G.focus();
  }, [r]), pe(() => {
    if (!h) return;
    const _ = (R) => {
      q.current && !q.current.contains(R.target) && ie.current && !ie.current.contains(R.target) && (D(!1), $(""));
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [h]), pe(() => {
    if (!h || !q.current) return;
    const _ = q.current.getBoundingClientRect(), R = window.innerHeight - _.bottom, G = R < 300 && _.top > R;
    w({
      left: _.left,
      width: _.width,
      ...G ? { bottom: window.innerHeight - _.top } : { top: _.bottom }
    });
  }, [h]);
  const j = le(async () => {
    if (!(d || !c) && (D(!0), $(""), m(-1), y(!1), !n))
      try {
        await l("loadOptions");
      } catch {
        y(!0);
      }
  }, [d, c, n, l]), x = le(() => {
    var _;
    D(!1), $(""), m(-1), (_ = q.current) == null || _.focus();
  }, []), z = le(
    (_) => {
      let R;
      if (i) {
        const M = p.find((G) => G.value === _);
        if (M)
          R = [...te.current, M];
        else
          return;
      } else {
        const M = p.find((G) => G.value === _);
        if (M)
          R = [M];
        else
          return;
      }
      te.current = R, l("valueChanged", { value: R.map((M) => M.value) }), i ? ($(""), m(-1)) : x();
    },
    [i, p, l, x]
  ), U = le(
    (_) => {
      ue.current = te.current.findIndex((M) => M.value === _);
      const R = te.current.filter((M) => M.value !== _);
      te.current = R, l("valueChanged", { value: R.map((M) => M.value) });
    },
    [l]
  ), W = le(
    (_) => {
      _.stopPropagation(), l("valueChanged", { value: [] }), x();
    },
    [l, x]
  ), ne = le((_) => {
    $(_.target.value);
  }, []), J = le(
    (_) => {
      if (!h) {
        if (_.key === "ArrowDown" || _.key === "ArrowUp" || _.key === "Enter" || _.key === " ") {
          if (_.target.tagName === "BUTTON") return;
          _.preventDefault(), _.stopPropagation(), j();
        }
        return;
      }
      switch (_.key) {
        case "ArrowDown":
          _.preventDefault(), _.stopPropagation(), m(
            (R) => R < f.length - 1 ? R + 1 : 0
          );
          break;
        case "ArrowUp":
          _.preventDefault(), _.stopPropagation(), m(
            (R) => R > 0 ? R - 1 : f.length - 1
          );
          break;
        case "Enter":
          _.preventDefault(), _.stopPropagation(), O >= 0 && O < f.length && z(f[O].value);
          break;
        case "Escape":
          _.preventDefault(), _.stopPropagation(), x();
          break;
        case "Tab":
          x();
          break;
        case "Backspace":
          S === "" && i && r.length > 0 && U(r[r.length - 1].value);
          break;
      }
    },
    [
      h,
      j,
      x,
      f,
      O,
      z,
      S,
      i,
      r,
      U
    ]
  ), me = le(
    async (_) => {
      _.preventDefault(), y(!1);
      try {
        await l("loadOptions");
      } catch {
        y(!0);
      }
    },
    [l]
  ), ye = le(
    (_, R) => {
      A(_), R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", String(_));
    },
    []
  ), E = le(
    (_, R) => {
      if (R.preventDefault(), R.dataTransfer.dropEffect = "move", k === null || k === _) {
        K(null), ae(null);
        return;
      }
      const M = R.currentTarget.getBoundingClientRect(), G = M.left + M.width / 2, he = R.clientX < G ? "before" : "after";
      K(_), ae(he);
    },
    [k]
  ), N = le(
    (_) => {
      if (_.preventDefault(), k === null || P === null || Q === null || k === P) return;
      const R = [...te.current], [M] = R.splice(k, 1);
      let G = P;
      k < P ? G = Q === "before" ? G - 1 : G : G = Q === "before" ? G : G + 1, R.splice(G, 0, M), te.current = R, l("valueChanged", { value: R.map((he) => he.value) }), A(null), K(null), ae(null);
    },
    [k, P, Q, l]
  ), I = le(() => {
    A(null), K(null), ae(null);
  }, []);
  if (pe(() => {
    if (O < 0 || !ie.current) return;
    const _ = ie.current.querySelector(
      `[id="${a}-opt-${O}"]`
    );
    _ && _.scrollIntoView({ block: "nearest" });
  }, [O, a]), !c)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, C) : r.map((_) => /* @__PURE__ */ e.createElement("span", { key: _.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Pe, { image: _.image }), /* @__PURE__ */ e.createElement("span", null, _.label))));
  const X = !s && r.length > 0 && !d, Z = h ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: ie,
      className: "tlDropdownSelect__dropdown",
      style: Y
    },
    (n || v) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: oe,
        type: "text",
        className: "tlDropdownSelect__search",
        value: S,
        onChange: ne,
        onKeyDown: J,
        placeholder: L["js.dropdownSelect.filterPlaceholder"],
        "aria-label": L["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": O >= 0 ? `${a}-opt-${O}` : void 0,
        "aria-controls": `${a}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${a}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !n && !v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: me }, L["js.dropdownSelect.error"])),
      n && f.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, T),
      n && f.map((_, R) => /* @__PURE__ */ e.createElement(
        On,
        {
          key: _.value,
          id: `${a}-opt-${R}`,
          option: _,
          highlighted: R === O,
          searchTerm: S,
          onSelect: z,
          onMouseEnter: () => m(R)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      ref: q,
      className: "tlDropdownSelect" + (h ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": h,
      "aria-haspopup": "listbox",
      "aria-owns": h ? `${a}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: h ? void 0 : j,
      onKeyDown: J
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, C) : r.map((_, R) => {
      let M = "";
      return k === R ? M = "tlDropdownSelect__chip--dragging" : P === R && Q === "before" ? M = "tlDropdownSelect__chip--dropBefore" : P === R && Q === "after" && (M = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        An,
        {
          key: _.value,
          option: _,
          removable: !d && (i || !s),
          onRemove: U,
          removeLabel: b(_.label),
          draggable: g,
          onDragStart: g ? (G) => ye(R, G) : void 0,
          onDragOver: g ? (G) => E(R, G) : void 0,
          onDrop: g ? N : void 0,
          onDragEnd: g ? I : void 0,
          dragClassName: g ? M : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, X && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: W,
        "aria-label": L["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, h ? "▲" : "▼"))
  ), Z && Mn.createPortal(Z, document.body));
};
F("TLButton", lt);
F("TLToggleButton", st);
F("TLTextInput", Ge);
F("TLNumberInput", Xe);
F("TLDatePicker", Qe);
F("TLSelect", et);
F("TLCheckbox", nt);
F("TLTable", rt);
F("TLCounter", ct);
F("TLTabBar", ut);
F("TLFieldList", dt);
F("TLAudioRecorder", pt);
F("TLAudioPlayer", ht);
F("TLFileUpload", _t);
F("TLDownload", Et);
F("TLPhotoCapture", yt);
F("TLPhotoViewer", wt);
F("TLSplitPanel", kt);
F("TLPanel", xt);
F("TLMaximizeRoot", Pt);
F("TLDeckPane", jt);
F("TLSidebar", zt);
F("TLStack", Ut);
F("TLGrid", Ht);
F("TLCard", Vt);
F("TLAppBar", Wt);
F("TLBreadcrumb", Yt);
F("TLBottomBar", qt);
F("TLDialog", Qt);
F("TLDrawer", nn);
F("TLSnackbar", ln);
F("TLMenu", sn);
F("TLAppShell", cn);
F("TLTextCell", un);
F("TLTableView", mn);
F("TLFormLayout", En);
F("TLFormGroup", Cn);
F("TLFormField", Nn);
F("TLResourceCell", Tn);
F("TLTreeView", Dn);
F("TLDropdownSelect", Bn);
