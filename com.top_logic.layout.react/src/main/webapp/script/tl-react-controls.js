import { React as e, useTLFieldValue as fe, getComponent as Ke, useTLState as z, useTLCommand as X, TLChild as V, useTLUpload as Le, useI18N as le, useTLDataUrl as Re, register as B } from "tl-react-bridge";
const { useCallback: Ye } = e, Ge = ({ controlId: a, state: t }) => {
  const [l, r] = fe(), i = Ye(
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
  const [r, i] = fe(), u = qe(
    (d) => {
      const c = d.target.value, n = c === "" ? null : Number(c);
      i(n);
    },
    [i]
  ), o = l != null && l.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: a,
      value: r != null ? String(r) : "",
      onChange: u,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Ze } = e, Qe = ({ controlId: a, state: t }) => {
  const [l, r] = fe(), i = Ze(
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
  const [r, i] = fe(), u = Je(
    (c) => {
      i(c.target.value || null);
    },
    [i]
  ), o = t.options ?? (l == null ? void 0 : l.options) ?? [];
  if (t.editable === !1) {
    const c = ((d = o.find((n) => n.value === r)) == null ? void 0 : d.label) ?? "";
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
    o.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: tt } = e, nt = ({ controlId: a, state: t }) => {
  const [l, r] = fe(), i = tt(
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
  return /* @__PURE__ */ e.createElement("table", { id: a, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, l.map((o) => {
    const d = o.cellModule ? Ke(o.cellModule) : void 0, c = i[o.name];
    if (d) {
      const n = { value: c, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: a + "-" + u + "-" + o.name,
          state: n
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, c != null ? String(c) : "");
  })))));
}, { useCallback: at } = e, lt = ({ controlId: a, command: t, label: l, disabled: r }) => {
  const i = z(), u = X(), o = t ?? "click", d = l ?? i.label, c = r ?? i.disabled === !0, n = at(() => {
    u(o);
  }, [u, o]);
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
  const u = z(), o = X(), d = t ?? "click", c = l ?? u.label, n = r ?? u.active === !0, p = i ?? u.disabled === !0, E = ot(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: E,
      disabled: p,
      className: "tlReactButton" + (n ? " tlReactButtonActive" : "")
    },
    c
  );
}, ct = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: it } = e, ut = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.tabs ?? [], i = t.activeTabId, u = it((o) => {
    o !== i && l("selectTab", { tabId: o });
  }, [l, i]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === i,
      className: "tlReactTabBar__tab" + (o.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => u(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, dt = ({ controlId: a }) => {
  const t = z(), l = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(V, { control: i })))));
}, mt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, pt = ({ controlId: a }) => {
  const t = z(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(null), d = e.useRef(null), c = e.useRef([]), n = e.useRef(null), p = t.status ?? "idle", E = t.error, C = p === "received" ? "idle" : r !== "idle" ? r : p, x = e.useCallback(async () => {
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
        n.current = S, c.current = [];
        const I = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", $ = new MediaRecorder(S, I ? { mimeType: I } : void 0);
        d.current = $, $.ondataavailable = (m) => {
          m.data.size > 0 && c.current.push(m.data);
        }, $.onstop = async () => {
          S.getTracks().forEach((y) => y.stop()), n.current = null;
          const m = new Blob(c.current, { type: $.mimeType || "audio/webm" });
          if (c.current = [], m.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const v = new FormData();
          v.append("audio", m, "recording.webm"), await l(v), i("idle");
        }, $.start(), i("recording");
      } catch (S) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", S), o("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, l]), w = le(mt), h = C === "recording" ? w["js.audioRecorder.stop"] : C === "uploading" ? w["js.uploading"] : w["js.audioRecorder.record"], b = C === "uploading", T = ["tlAudioRecorder__button"];
  return C === "recording" && T.push("tlAudioRecorder__button--recording"), C === "uploading" && T.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: x,
      disabled: b,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${C === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, w[u]), E && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E));
}, ft = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ht = ({ controlId: a }) => {
  const t = z(), l = Re(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [u, o] = e.useState(r ? "idle" : "disabled"), d = e.useRef(null), c = e.useRef(null), n = e.useRef(i);
  e.useEffect(() => {
    r ? u === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), o("disabled"));
  }, [r]), e.useEffect(() => {
    i !== n.current && (n.current = i, d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (u === "playing" || u === "paused" || u === "loading") && o("idle"));
  }, [i]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const p = e.useCallback(async () => {
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
    if (!c.current) {
      o("loading");
      try {
        const b = await fetch(l);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), o("idle");
          return;
        }
        const T = await b.blob();
        c.current = URL.createObjectURL(T);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), o("idle");
        return;
      }
    }
    const h = new Audio(c.current);
    d.current = h, h.onended = () => {
      o("idle");
    }, h.play(), o("playing");
  }, [u, l]), E = le(ft), C = u === "loading" ? E["js.loading"] : u === "playing" ? E["js.audioPlayer.pause"] : u === "disabled" ? E["js.audioPlayer.noAudio"] : E["js.audioPlayer.play"], x = u === "disabled" || u === "loading", w = ["tlAudioPlayer__button"];
  return u === "playing" && w.push("tlAudioPlayer__button--playing"), u === "loading" && w.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: p,
      disabled: x,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, bt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, _t = ({ controlId: a }) => {
  const t = z(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(!1), d = e.useRef(null), c = t.status ?? "idle", n = t.error, p = t.accept ?? "", E = c === "received" ? "idle" : r !== "idle" ? r : c, C = e.useCallback(async (m) => {
    i("uploading");
    const v = new FormData();
    v.append("file", m, m.name), await l(v), i("idle");
  }, [l]), x = e.useCallback((m) => {
    var y;
    const v = (y = m.target.files) == null ? void 0 : y[0];
    v && C(v);
  }, [C]), w = e.useCallback(() => {
    var m;
    r !== "uploading" && ((m = d.current) == null || m.click());
  }, [r]), h = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), o(!0);
  }, []), b = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), o(!1);
  }, []), T = e.useCallback((m) => {
    var y;
    if (m.preventDefault(), m.stopPropagation(), o(!1), r === "uploading") return;
    const v = (y = m.dataTransfer.files) == null ? void 0 : y[0];
    v && C(v);
  }, [r, C]), S = E === "uploading", I = le(bt), $ = E === "uploading" ? I["js.uploading"] : I["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: h,
      onDragLeave: b,
      onDrop: T
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: p || void 0,
        onChange: x,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (E === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: w,
        disabled: S,
        title: $,
        "aria-label": $
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    n && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, n)
  );
}, Et = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, vt = ({ controlId: a }) => {
  const t = z(), l = Re(), r = X(), i = !!t.hasData, u = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [c, n] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || c)) {
      n(!0);
      try {
        const w = l + (l.includes("?") ? "&" : "?") + "rev=" + u, h = await fetch(w);
        if (!h.ok) {
          console.error("[TLDownload] Failed to fetch data:", h.status);
          return;
        }
        const b = await h.blob(), T = URL.createObjectURL(b), S = document.createElement("a");
        S.href = T, S.download = o, S.style.display = "none", document.body.appendChild(S), S.click(), document.body.removeChild(S), URL.revokeObjectURL(T);
      } catch (w) {
        console.error("[TLDownload] Fetch error:", w);
      } finally {
        n(!1);
      }
    }
  }, [i, c, l, u, o]), E = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), C = le(Et);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, C["js.download.noFile"]));
  const x = c ? C["js.downloading"] : C["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: c,
      title: x,
      "aria-label": x
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: E,
      title: C["js.download.clear"],
      "aria-label": C["js.download.clearFile"]
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
  const t = z(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(null), [d, c] = e.useState(!1), n = e.useRef(null), p = e.useRef(null), E = e.useRef(null), C = e.useRef(null), x = e.useRef(null), w = t.error, h = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((N) => N.stop()), p.current = null), n.current && (n.current.srcObject = null);
  }, []), T = e.useCallback(() => {
    b(), i("idle");
  }, [b]), S = e.useCallback(async () => {
    var N;
    if (r !== "uploading") {
      if (o(null), !h) {
        (N = C.current) == null || N.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = D, i("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), o("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, h]), I = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const N = n.current, D = E.current;
    if (!N || !D)
      return;
    D.width = N.videoWidth, D.height = N.videoHeight;
    const M = D.getContext("2d");
    M && (M.drawImage(N, 0, 0), b(), i("uploading"), D.toBlob(async (H) => {
      if (!H) {
        i("idle");
        return;
      }
      const Y = new FormData();
      Y.append("photo", H, "capture.jpg"), await l(Y), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, l, b]), $ = e.useCallback(async (N) => {
    var H;
    const D = (H = N.target.files) == null ? void 0 : H[0];
    if (!D) return;
    i("uploading");
    const M = new FormData();
    M.append("photo", D, D.name), await l(M), i("idle"), C.current && (C.current.value = "");
  }, [l]);
  e.useEffect(() => {
    r === "overlayOpen" && n.current && p.current && (n.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var D;
    if (r !== "overlayOpen") return;
    (D = x.current) == null || D.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const N = (D) => {
      D.key === "Escape" && T();
    };
    return document.addEventListener("keydown", N), () => document.removeEventListener("keydown", N);
  }, [r, T]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((N) => N.stop()), p.current = null);
  }, []);
  const m = le(gt), v = r === "uploading" ? m["js.uploading"] : m["js.photoCapture.open"], y = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && y.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  d && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return d && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
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
  )), !h && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: C,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: $
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: E, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: x,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: T }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: n,
        className: U.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: k.join(" "),
        onClick: () => c((N) => !N),
        title: m["js.photoCapture.mirror"],
        "aria-label": m["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: I,
        title: m["js.photoCapture.capture"],
        "aria-label": m["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: T,
        title: m["js.photoCapture.close"],
        "aria-label": m["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m[u]), w && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w));
}, Ct = {
  "js.photoViewer.alt": "Captured photo"
}, wt = ({ controlId: a }) => {
  const t = z(), l = Re(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [u, o] = e.useState(null), d = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      u && (URL.revokeObjectURL(u), o(null));
      return;
    }
    if (i === d.current && u)
      return;
    d.current = i, u && (URL.revokeObjectURL(u), o(null));
    let n = !1;
    return (async () => {
      try {
        const p = await fetch(l);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const E = await p.blob();
        n || o(URL.createObjectURL(E));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      n = !0;
    };
  }, [r, i, l]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const c = le(Ct);
  return !r || !u ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: De, useRef: ge } = e, kt = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.orientation, i = t.resizable === !0, u = t.children ?? [], o = r === "horizontal", d = u.length > 0 && u.every((b) => b.collapsed), c = !d && u.some((b) => b.collapsed), n = d ? !o : o, p = ge(null), E = ge(null), C = ge(null), x = De((b, T) => {
    const S = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? d && !n ? S.flex = "1 0 0%" : S.flex = "0 0 auto" : T !== void 0 ? S.flex = `0 0 ${T}px` : b.unit === "%" || c ? S.flex = `${b.size} 0 0%` : S.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (S.minWidth = o ? b.minSize : void 0, S.minHeight = o ? void 0 : b.minSize), S;
  }, [o, d, c, n]), w = De((b, T) => {
    b.preventDefault();
    const S = p.current;
    if (!S) return;
    const I = u[T], $ = u[T + 1], m = S.querySelectorAll(":scope > .tlSplitPanel__child"), v = [];
    m.forEach((k) => {
      v.push(o ? k.offsetWidth : k.offsetHeight);
    }), C.current = v, E.current = {
      splitterIndex: T,
      startPos: o ? b.clientX : b.clientY,
      startSizeBefore: v[T],
      startSizeAfter: v[T + 1],
      childBefore: I,
      childAfter: $
    };
    const y = (k) => {
      const N = E.current;
      if (!N || !C.current) return;
      const M = (o ? k.clientX : k.clientY) - N.startPos, H = N.childBefore.minSize || 0, Y = N.childAfter.minSize || 0;
      let te = N.startSizeBefore + M, W = N.startSizeAfter - M;
      te < H && (W += te - H, te = H), W < Y && (te += W - Y, W = Y), C.current[N.splitterIndex] = te, C.current[N.splitterIndex + 1] = W;
      const ne = S.querySelectorAll(":scope > .tlSplitPanel__child"), ce = ne[N.splitterIndex], oe = ne[N.splitterIndex + 1];
      ce && (ce.style.flex = `0 0 ${te}px`), oe && (oe.style.flex = `0 0 ${W}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", C.current) {
        const k = {};
        u.forEach((N, D) => {
          const M = N.control;
          M != null && M.controlId && C.current && (k[M.controlId] = C.current[D]);
        }), l("updateSizes", { sizes: k });
      }
      C.current = null, E.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", U), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, o, l]), h = [];
  return u.forEach((b, T) => {
    if (h.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${T}`,
          className: `tlSplitPanel__child${b.collapsed && n ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: x(b)
        },
        /* @__PURE__ */ e.createElement(V, { control: b.control })
      )
    ), i && T < u.length - 1) {
      const S = u[T + 1];
      !b.collapsed && !S.collapsed && h.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${T}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: ($) => w($, T)
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
    h
  );
}, { useCallback: ye } = e, St = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Rt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Dt = ({ controlId: a }) => {
  const t = z(), l = X(), r = le(St), i = t.title, u = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, c = t.showPopOut === !0, n = t.toolbarButtons ?? [], p = u === "MINIMIZED", E = u === "MAXIMIZED", C = u === "HIDDEN", x = ye(() => {
    l("toggleMinimize");
  }, [l]), w = ye(() => {
    l("toggleMaximize");
  }, [l]), h = ye(() => {
    l("popOut");
  }, [l]);
  if (C)
    return null;
  const b = E ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${u.toLowerCase()}`,
      style: b
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, n.map((T, S) => /* @__PURE__ */ e.createElement("span", { key: S, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(V, { control: T }))), o && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: x,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Tt, null) : /* @__PURE__ */ e.createElement(Nt, null)
    ), d && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: E ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      E ? /* @__PURE__ */ e.createElement(Rt, null) : /* @__PURE__ */ e.createElement(Lt, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(xt, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(V, { control: t.child }))
  );
}, Pt = ({ controlId: a }) => {
  const t = z();
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
  const t = z();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(V, { control: t.activeChild }));
}, { useCallback: ae, useState: be, useEffect: _e, useRef: Ee } = e, Mt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ne(a, t, l, r) {
  const i = [];
  for (const u of a)
    u.type === "nav" ? i.push({ id: u.id, type: "nav", groupId: r }) : u.type === "command" ? i.push({ id: u.id, type: "command", groupId: r }) : u.type === "group" && (i.push({ id: u.id, type: "group" }), (l.get(u.id) ?? u.expanded) && !t && i.push(...Ne(u.children, t, l, u.id)));
  return i;
}
const ue = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, At = ({ item: a, active: t, collapsed: l, onSelect: r, tabIndex: i, itemRef: u, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(a.id),
    title: l ? a.label : void 0,
    tabIndex: i,
    ref: u,
    onFocus: () => o(a.id)
  },
  l && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ue, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(ue, { icon: a.icon }),
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
  /* @__PURE__ */ e.createElement(ue, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), Bt = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(ue, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), It = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ft = ({ item: a, activeItemId: t, anchorRect: l, onSelect: r, onExecute: i, onClose: u }) => {
  const o = Ee(null);
  _e(() => {
    const n = (p) => {
      o.current && !o.current.contains(p.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", n), () => document.removeEventListener("mousedown", n);
  }, [u]), _e(() => {
    const n = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", n), () => document.removeEventListener("keydown", n);
  }, [u]);
  const d = ae((n) => {
    n.type === "nav" ? (r(n.id), u()) : n.type === "command" && (i(n.id), u());
  }, [r, i, u]), c = {};
  return l && (c.left = l.right, c.top = l.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, a.label), a.children.map((n) => {
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
        /* @__PURE__ */ e.createElement(ue, { icon: n.icon }),
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
  onToggleGroup: o,
  tabIndex: d,
  itemRef: c,
  onFocus: n,
  focusedId: p,
  setItemRef: E,
  onItemFocus: C,
  flyoutGroupId: x,
  onOpenFlyout: w,
  onCloseFlyout: h
}) => {
  const b = Ee(null), [T, S] = be(null), I = ae(() => {
    r ? x === a.id ? h() : (b.current && S(b.current.getBoundingClientRect()), w(a.id)) : o(a.id);
  }, [r, x, a.id, o, w, h]), $ = ae((v) => {
    b.current = v, c(v);
  }, [c]), m = r && x === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (m ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: I,
      title: r ? a.label : void 0,
      "aria-expanded": r ? m : t,
      tabIndex: d,
      ref: $,
      onFocus: () => n(a.id)
    },
    /* @__PURE__ */ e.createElement(ue, { icon: a.icon }),
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
      anchorRect: T,
      onSelect: i,
      onExecute: u,
      onClose: h
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, a.children.map((v) => /* @__PURE__ */ e.createElement(
    ze,
    {
      key: v.id,
      item: v,
      activeItemId: l,
      collapsed: r,
      onSelect: i,
      onExecute: u,
      onToggleGroup: o,
      focusedId: p,
      setItemRef: E,
      onItemFocus: C,
      groupStates: null,
      flyoutGroupId: x,
      onOpenFlyout: w,
      onCloseFlyout: h
    }
  ))));
}, ze = ({
  item: a,
  activeItemId: t,
  collapsed: l,
  onSelect: r,
  onExecute: i,
  onToggleGroup: u,
  focusedId: o,
  setItemRef: d,
  onItemFocus: c,
  groupStates: n,
  flyoutGroupId: p,
  onOpenFlyout: E,
  onCloseFlyout: C
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
          tabIndex: o === a.id ? 0 : -1,
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
          tabIndex: o === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Bt, { item: a, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(It, null);
    case "group": {
      const x = n ? n.get(a.id) ?? a.expanded : a.expanded;
      return /* @__PURE__ */ e.createElement(
        $t,
        {
          item: a,
          expanded: x,
          activeItemId: t,
          collapsed: l,
          onSelect: r,
          onExecute: i,
          onToggleGroup: u,
          tabIndex: o === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: c,
          focusedId: o,
          setItemRef: d,
          onItemFocus: c,
          flyoutGroupId: p,
          onOpenFlyout: E,
          onCloseFlyout: C
        }
      );
    }
    default:
      return null;
  }
}, zt = ({ controlId: a }) => {
  const t = z(), l = X(), r = le(Mt), i = t.items ?? [], u = t.activeItemId, o = t.collapsed, [d, c] = be(() => {
    const k = /* @__PURE__ */ new Map(), N = (D) => {
      for (const M of D)
        M.type === "group" && (k.set(M.id, M.expanded), N(M.children));
    };
    return N(i), k;
  }), n = ae((k) => {
    c((N) => {
      const D = new Map(N), M = D.get(k) ?? !1;
      return D.set(k, !M), l("toggleGroup", { itemId: k, expanded: !M }), D;
    });
  }, [l]), p = ae((k) => {
    k !== u && l("selectItem", { itemId: k });
  }, [l, u]), E = ae((k) => {
    l("executeCommand", { itemId: k });
  }, [l]), C = ae(() => {
    l("toggleCollapse", {});
  }, [l]), [x, w] = be(null), h = ae((k) => {
    w(k);
  }, []), b = ae(() => {
    w(null);
  }, []);
  _e(() => {
    o || w(null);
  }, [o]);
  const [T, S] = be(() => {
    const k = Ne(i, o, d);
    return k.length > 0 ? k[0].id : "";
  }), I = Ee(/* @__PURE__ */ new Map()), $ = ae((k) => (N) => {
    N ? I.current.set(k, N) : I.current.delete(k);
  }, []), m = ae((k) => {
    S(k);
  }, []), v = Ee(0), y = ae((k) => {
    S(k), v.current++;
  }, []);
  _e(() => {
    const k = I.current.get(T);
    k && document.activeElement !== k && k.focus();
  }, [T, v.current]);
  const U = ae((k) => {
    if (k.key === "Escape" && x !== null) {
      k.preventDefault(), b();
      return;
    }
    const N = Ne(i, o, d);
    if (N.length === 0) return;
    const D = N.findIndex((H) => H.id === T);
    if (D < 0) return;
    const M = N[D];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const H = (D + 1) % N.length;
        y(N[H].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const H = (D - 1 + N.length) % N.length;
        y(N[H].id);
        break;
      }
      case "Home": {
        k.preventDefault(), y(N[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), y(N[N.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), M.type === "nav" ? p(M.id) : M.type === "command" ? E(M.id) : M.type === "group" && (o ? x === M.id ? b() : h(M.id) : n(M.id));
        break;
      }
      case "ArrowRight": {
        M.type === "group" && !o && ((d.get(M.id) ?? !1) || (k.preventDefault(), n(M.id)));
        break;
      }
      case "ArrowLeft": {
        M.type === "group" && !o && (d.get(M.id) ?? !1) && (k.preventDefault(), n(M.id));
        break;
      }
    }
  }, [
    i,
    o,
    d,
    T,
    x,
    y,
    p,
    E,
    n,
    h,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, i.map((k) => /* @__PURE__ */ e.createElement(
    ze,
    {
      key: k.id,
      item: k,
      activeItemId: u,
      collapsed: o,
      onSelect: p,
      onExecute: E,
      onToggleGroup: n,
      focusedId: T,
      setItemRef: $,
      onItemFocus: m,
      groupStates: d,
      flyoutGroupId: x,
      onOpenFlyout: h,
      onCloseFlyout: b
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: C,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, Ut = ({ controlId: a }) => {
  const t = z(), l = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", u = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${l}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: d }, o.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c })));
}, Vt = ({ controlId: a }) => {
  const t = z(), l = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", u = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : l && (o.gridTemplateColumns = `repeat(${l}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${i}`, style: o }, u.map((d, c) => /* @__PURE__ */ e.createElement(V, { key: c, control: d })));
}, Ht = ({ controlId: a }) => {
  const t = z(), l = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", u = t.headerActions ?? [], o = t.child, d = l != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${r}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, l && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, l), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(V, { control: o })));
}, Wt = ({ controlId: a }) => {
  const t = z(), l = t.title ?? "", r = t.leading, i = t.actions ?? [], u = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: d }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(V, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, l), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((c, n) => /* @__PURE__ */ e.createElement(V, { key: n, control: c }))));
}, { useCallback: Kt } = e, Yt = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.items ?? [], i = Kt((u) => {
    l("navigate", { itemId: u });
  }, [l]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((u, o) => {
    const d = o === r.length - 1;
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
        onClick: () => i(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: Gt } = e, qt = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.items ?? [], i = t.activeItemId, u = Gt((o) => {
    o !== i && l("selectItem", { itemId: o });
  }, [l, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((o) => {
    const d = o.id === i;
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
}, { useCallback: Pe, useEffect: je, useRef: Xt } = e, Zt = {
  "js.dialog.close": "Close"
}, Qt = ({ controlId: a }) => {
  const t = z(), l = X(), r = le(Zt), i = t.open === !0, u = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, c = t.actions ?? [], n = t.child, p = Xt(null), E = Pe(() => {
    l("close");
  }, [l]), C = Pe((w) => {
    d && w.target === w.currentTarget && E();
  }, [d, E]);
  if (je(() => {
    if (!i) return;
    const w = (h) => {
      h.key === "Escape" && E();
    };
    return document.addEventListener("keydown", w), () => document.removeEventListener("keydown", w);
  }, [i, E]), je(() => {
    i && p.current && p.current.focus();
  }, [i]), !i) return null;
  const x = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDialog__backdrop", onClick: C }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": x,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: x }, u), /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(V, { control: n })),
    c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, c.map((w, h) => /* @__PURE__ */ e.createElement(V, { key: h, control: w })))
  ));
}, { useCallback: Jt, useEffect: en } = e, tn = {
  "js.drawer.close": "Close"
}, nn = ({ controlId: a }) => {
  const t = z(), l = X(), r = le(tn), i = t.open === !0, u = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, c = t.child, n = Jt(() => {
    l("close");
  }, [l]);
  en(() => {
    if (!i) return;
    const E = (C) => {
      C.key === "Escape" && n();
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
  }, [i, n]);
  const p = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${o}`,
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
}, { useCallback: Me, useEffect: rn, useState: an } = e, ln = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.message ?? "", i = t.content ?? "", u = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, c = t.visible === !0, [n, p] = an(!1), E = Me(() => {
    p(!0), setTimeout(() => {
      l("dismiss"), p(!1);
    }, 200);
  }, [l]), C = Me(() => {
    o && l(o.commandName), E();
  }, [l, o, E]);
  return rn(() => {
    if (!c || d === 0) return;
    const x = setTimeout(E, d);
    return () => clearTimeout(x);
  }, [c, d, E]), !c && !n ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlSnackbar tlSnackbar--${u}${n ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: C }, o.label)
  );
}, { useCallback: Ce, useEffect: we, useRef: on, useState: Ae } = e, sn = ({ controlId: a }) => {
  const t = z(), l = X(), r = t.open === !0, i = t.anchorId, u = t.items ?? [], o = on(null), [d, c] = Ae({ top: 0, left: 0 }), [n, p] = Ae(0), E = u.filter((h) => h.type === "item" && !h.disabled);
  we(() => {
    var m, v;
    if (!r || !i) return;
    const h = document.getElementById(i);
    if (!h) return;
    const b = h.getBoundingClientRect(), T = ((m = o.current) == null ? void 0 : m.offsetHeight) ?? 200, S = ((v = o.current) == null ? void 0 : v.offsetWidth) ?? 200;
    let I = b.bottom + 4, $ = b.left;
    I + T > window.innerHeight && (I = b.top - T - 4), $ + S > window.innerWidth && ($ = b.right - S), c({ top: I, left: $ }), p(0);
  }, [r, i]);
  const C = Ce(() => {
    l("close");
  }, [l]), x = Ce((h) => {
    l("selectItem", { itemId: h });
  }, [l]);
  we(() => {
    if (!r) return;
    const h = (b) => {
      o.current && !o.current.contains(b.target) && C();
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [r, C]);
  const w = Ce((h) => {
    if (h.key === "Escape") {
      C();
      return;
    }
    if (h.key === "ArrowDown")
      h.preventDefault(), p((b) => (b + 1) % E.length);
    else if (h.key === "ArrowUp")
      h.preventDefault(), p((b) => (b - 1 + E.length) % E.length);
    else if (h.key === "Enter" || h.key === " ") {
      h.preventDefault();
      const b = E[n];
      b && x(b.id);
    }
  }, [C, x, E, n]);
  return we(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: w
    },
    u.map((h, b) => {
      if (h.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
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
          onClick: () => x(h.id)
        },
        h.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + h.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, h.label)
      );
    })
  ) : null;
}, cn = ({ controlId: a }) => {
  const t = z(), l = t.header, r = t.content, i = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(V, { control: l })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(V, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(V, { control: i })), /* @__PURE__ */ e.createElement(V, { control: u }));
}, un = () => {
  const t = z().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, dn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Oe = 50, mn = () => {
  const a = z(), t = X(), l = le(dn), r = a.columns ?? [], i = a.totalRowCount ?? 0, u = a.rows ?? [], o = a.rowHeight ?? 36, d = a.selectionMode ?? "single", c = a.selectedCount ?? 0, n = a.frozenColumnCount ?? 0, p = a.treeMode ?? !1, E = e.useMemo(
    () => r.filter((g) => g.sortPriority && g.sortPriority > 0).length,
    [r]
  ), C = d === "multi", x = 40, w = 20, h = e.useRef(null), b = e.useRef(null), T = e.useRef(null), [S, I] = e.useState({}), $ = e.useRef(null), m = e.useRef(!1), v = e.useRef(null), [y, U] = e.useState(null), [k, N] = e.useState(null);
  e.useEffect(() => {
    $.current || I({});
  }, [r]);
  const D = e.useCallback((g) => S[g.name] ?? g.width, [S]), M = e.useMemo(() => {
    const g = [];
    let L = C && n > 0 ? x : 0;
    for (let F = 0; F < n && F < r.length; F++)
      g.push(L), L += D(r[F]);
    return g;
  }, [r, n, C, x, D]), H = i * o, Y = e.useCallback((g, L, F) => {
    F.preventDefault(), F.stopPropagation(), $.current = { column: g, startX: F.clientX, startWidth: L };
    const G = (ee) => {
      const re = $.current;
      if (!re) return;
      const Z = Math.max(Oe, re.startWidth + (ee.clientX - re.startX));
      I((ve) => ({ ...ve, [re.column]: Z }));
    }, q = (ee) => {
      document.removeEventListener("mousemove", G), document.removeEventListener("mouseup", q);
      const re = $.current;
      if (re) {
        const Z = Math.max(Oe, re.startWidth + (ee.clientX - re.startX));
        t("columnResize", { column: re.column, width: Z }), $.current = null, m.current = !0, requestAnimationFrame(() => {
          m.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", G), document.addEventListener("mouseup", q);
  }, [t]), te = e.useCallback(() => {
    h.current && b.current && (h.current.scrollLeft = b.current.scrollLeft), T.current !== null && clearTimeout(T.current), T.current = window.setTimeout(() => {
      const g = b.current;
      if (!g) return;
      const L = g.scrollTop, F = Math.ceil(g.clientHeight / o), G = Math.floor(L / o);
      t("scroll", { start: G, count: F });
    }, 80);
  }, [t, o]), W = e.useCallback((g, L, F) => {
    if (m.current) return;
    let G;
    !L || L === "desc" ? G = "asc" : G = "desc";
    const q = F.shiftKey ? "add" : "replace";
    t("sort", { column: g, direction: G, mode: q });
  }, [t]), ne = e.useCallback((g, L) => {
    v.current = g, L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", g);
  }, []), ce = e.useCallback((g, L) => {
    if (!v.current || v.current === g) {
      U(null);
      return;
    }
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const F = L.currentTarget.getBoundingClientRect(), G = L.clientX < F.left + F.width / 2 ? "left" : "right";
    U({ column: g, side: G });
  }, []), oe = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const L = v.current;
    if (!L || !y) {
      v.current = null, U(null);
      return;
    }
    let F = r.findIndex((q) => q.name === y.column);
    if (F < 0) {
      v.current = null, U(null);
      return;
    }
    const G = r.findIndex((q) => q.name === L);
    y.side === "right" && F++, G < F && F--, t("columnReorder", { column: L, targetIndex: F }), v.current = null, U(null);
  }, [r, y, t]), de = e.useCallback(() => {
    v.current = null, U(null);
  }, []), s = e.useCallback((g, L) => {
    L.shiftKey && L.preventDefault(), t("select", {
      rowIndex: g,
      ctrlKey: L.ctrlKey || L.metaKey,
      shiftKey: L.shiftKey
    });
  }, [t]), _ = e.useCallback((g, L) => {
    L.stopPropagation(), t("select", { rowIndex: g, ctrlKey: !0, shiftKey: !1 });
  }, [t]), A = e.useCallback(() => {
    const g = c === i && i > 0;
    t("selectAll", { selected: !g });
  }, [t, c, i]), P = e.useCallback((g, L, F) => {
    F.stopPropagation(), t("expand", { rowIndex: g, expanded: L });
  }, [t]), f = e.useCallback((g, L) => {
    L.preventDefault(), N({ x: L.clientX, y: L.clientY, colIdx: g });
  }, []), R = e.useCallback(() => {
    k && (t("setFrozenColumnCount", { count: k.colIdx + 1 }), N(null));
  }, [k, t]), j = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), N(null);
  }, [t]);
  e.useEffect(() => {
    if (!k) return;
    const g = () => N(null), L = (F) => {
      F.key === "Escape" && N(null);
    };
    return document.addEventListener("mousedown", g), document.addEventListener("keydown", L), () => {
      document.removeEventListener("mousedown", g), document.removeEventListener("keydown", L);
    };
  }, [k]);
  const K = r.reduce((g, L) => g + D(L), 0) + (C ? x : 0), Q = c === i && i > 0, he = c > 0 && c < i, Ve = e.useCallback((g) => {
    g && (g.indeterminate = he);
  }, [he]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (g) => {
        if (!v.current) return;
        g.preventDefault();
        const L = b.current, F = h.current;
        if (!L) return;
        const G = L.getBoundingClientRect(), q = 40, ee = 8;
        g.clientX < G.left + q ? L.scrollLeft = Math.max(0, L.scrollLeft - ee) : g.clientX > G.right - q && (L.scrollLeft += ee), F && (F.scrollLeft = L.scrollLeft);
      },
      onDrop: oe
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: K } }, C && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: x,
          minWidth: x,
          ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (g) => {
          v.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", r.length > 0 && r[0].name !== v.current && U({ column: r[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ve,
          className: "tlTableView__checkbox",
          checked: Q,
          onChange: A
        }
      )
    ), r.map((g, L) => {
      const F = D(g), G = L === r.length - 1;
      let q = "tlTableView__headerCell";
      g.sortable && (q += " tlTableView__headerCell--sortable"), y && y.column === g.name && (q += " tlTableView__headerCell--dragOver-" + y.side);
      const ee = L < n, re = L === n - 1;
      return ee && (q += " tlTableView__headerCell--frozen"), re && (q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.name,
          className: q,
          style: {
            ...G && !ee ? { flex: "1 0 auto", minWidth: F } : { width: F, minWidth: F },
            position: ee ? "sticky" : "relative",
            ...ee ? { left: M[L], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: g.sortable ? (Z) => W(g.name, g.sortDirection, Z) : void 0,
          onContextMenu: (Z) => f(L, Z),
          onDragStart: (Z) => ne(g.name, Z),
          onDragOver: (Z) => ce(g.name, Z),
          onDrop: oe,
          onDragEnd: de
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, g.label),
        g.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, g.sortDirection === "asc" ? "▲" : "▼", E > 1 && g.sortPriority != null && g.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, g.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (Z) => Y(g.name, F, Z)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (g) => {
          if (v.current && r.length > 0) {
            const L = r[r.length - 1];
            L.name !== v.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", U({ column: L.name, side: "right" }));
          }
        },
        onDrop: oe
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: te
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: H, position: "relative", minWidth: K } }, u.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: "tlTableView__row" + (g.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: g.index * o,
            height: o,
            minWidth: K,
            width: "100%"
          },
          onClick: (L) => s(g.index, L)
        },
        C && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (n > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: x,
              minWidth: x,
              ...n > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (L) => L.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: g.selected,
              onChange: () => {
              },
              onClick: (L) => _(g.index, L),
              tabIndex: -1
            }
          )
        ),
        r.map((L, F) => {
          const G = D(L), q = F === r.length - 1, ee = F < n, re = F === n - 1;
          let Z = "tlTableView__cell";
          ee && (Z += " tlTableView__cell--frozen"), re && (Z += " tlTableView__cell--frozenLast");
          const ve = p && F === 0, He = g.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: L.name,
              className: Z,
              style: {
                ...q && !ee ? { flex: "1 0 auto", minWidth: G } : { width: G, minWidth: G },
                ...ee ? { position: "sticky", left: M[F], zIndex: 2 } : {}
              }
            },
            ve ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: He * w } }, g.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (We) => P(g.index, !g.expanded, We)
              },
              g.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(V, { control: g.cells[L.name] })) : /* @__PURE__ */ e.createElement(V, { control: g.cells[L.name] })
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
        onMouseDown: (g) => g.stopPropagation()
      },
      k.colIdx + 1 !== n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: R }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.freezeUpTo"])),
      n > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: j }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.unfreezeAll"]))
    )
  );
}, pn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ue = e.createContext(pn), { useMemo: fn, useRef: hn, useState: bn, useEffect: _n } = e, En = 320, vn = ({ controlId: a }) => {
  const t = z(), l = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, u = t.children ?? [], o = t.noModelMessage, d = hn(null), [c, n] = bn(
    r === "top" ? "top" : "side"
  );
  _n(() => {
    if (r !== "auto") {
      n(r);
      return;
    }
    const w = d.current;
    if (!w) return;
    const h = new ResizeObserver((b) => {
      for (const T of b) {
        const I = T.contentRect.width / l;
        n(I < En ? "top" : "side");
      }
    });
    return h.observe(w), () => h.disconnect();
  }, [r, l]);
  const p = fn(() => ({
    readOnly: i,
    resolvedLabelPosition: c
  }), [i, c]), C = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / l))}rem`}, 1fr))`
  }, x = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Ue.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: a, className: x, style: C, ref: d }, u.map((w, h) => /* @__PURE__ */ e.createElement(V, { key: h, control: w }))));
}, { useCallback: gn } = e, yn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Cn = ({ controlId: a }) => {
  const t = z(), l = X(), r = le(yn), i = t.header, u = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, c = t.border ?? "none", n = t.fullLine === !0, p = t.children ?? [], E = i != null || u.length > 0 || o, C = gn(() => {
    l("toggleCollapse");
  }, [l]), x = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    n ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: x }, E && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: C,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, i), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((w, h) => /* @__PURE__ */ e.createElement(V, { key: h, control: w })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((w, h) => /* @__PURE__ */ e.createElement(V, { key: h, control: w }))));
}, { useContext: wn, useState: kn, useCallback: Sn } = e, Nn = ({ controlId: a }) => {
  const t = z(), l = wn(Ue), r = t.label ?? "", i = t.required === !0, u = t.error, o = t.helpText, d = t.dirty === !0, c = t.labelPosition ?? l.resolvedLabelPosition, n = t.fullLine === !0, p = t.visible !== !1, E = t.field, C = l.readOnly, [x, w] = kn(!1), h = Sn(() => w((S) => !S), []);
  if (!p) return null;
  const b = u != null, T = [
    "tlFormField",
    `tlFormField--${c}`,
    C ? "tlFormField--readonly" : "",
    n ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: T }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), i && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !C && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(V, { control: E })), !C && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, u)), !C && o && x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Tn = () => {
  const a = z(), t = X(), l = a.iconCss, r = a.iconSrc, i = a.label, u = a.cssClass, o = a.tooltip, d = a.hasLink, c = l ? /* @__PURE__ */ e.createElement("i", { className: l }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, n = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), p = e.useCallback((C) => {
    C.preventDefault(), t("goto", {});
  }, [t]), E = ["tlResourceCell", u].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: E, href: "#", onClick: p, title: o }, n) : /* @__PURE__ */ e.createElement("span", { className: E, title: o }, n);
}, Ln = 20, Rn = () => {
  const a = z(), t = X(), l = a.nodes ?? [], r = a.selectionMode ?? "single", i = a.dragEnabled ?? !1, u = a.dropEnabled ?? !1, o = a.dropIndicatorNodeId ?? null, d = a.dropIndicatorPosition ?? null, [c, n] = e.useState(-1), p = e.useRef(null), E = e.useCallback((m, v) => {
    t(v ? "collapse" : "expand", { nodeId: m });
  }, [t]), C = e.useCallback((m, v) => {
    t("select", {
      nodeId: m,
      ctrlKey: v.ctrlKey || v.metaKey,
      shiftKey: v.shiftKey
    });
  }, [t]), x = e.useCallback((m, v) => {
    v.preventDefault(), t("contextMenu", { nodeId: m, x: v.clientX, y: v.clientY });
  }, [t]), w = e.useRef(null), h = e.useCallback((m, v) => {
    const y = v.getBoundingClientRect(), U = m.clientY - y.top, k = y.height / 3;
    return U < k ? "above" : U > k * 2 ? "below" : "within";
  }, []), b = e.useCallback((m, v) => {
    v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", m);
  }, []), T = e.useCallback((m, v) => {
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const y = h(v, v.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: m, position: y }), w.current = null;
    }, 50);
  }, [t, h]), S = e.useCallback((m, v) => {
    v.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const y = h(v, v.currentTarget);
    t("drop", { nodeId: m, position: y });
  }, [t, h]), I = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
  }, [t]), $ = e.useCallback((m) => {
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
            const U = y.depth;
            for (let k = c - 1; k >= 0; k--)
              if (l[k].depth < U) {
                v = k;
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
      onKeyDown: $
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
          o === m.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === m.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === m.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: m.depth * Ln },
        draggable: i,
        onClick: (y) => C(m.id, y),
        onContextMenu: (y) => x(m.id, y),
        onDragStart: (y) => b(m.id, y),
        onDragOver: u ? (y) => T(m.id, y) : void 0,
        onDrop: u ? (y) => S(m.id, y) : void 0,
        onDragEnd: I
      },
      m.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (y) => {
            y.stopPropagation(), E(m.id, m.expanded);
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
var ke = { exports: {} }, J = {}, Se = { exports: {} }, O = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var Be;
function xn() {
  if (Be) return O;
  Be = 1;
  var a = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), l = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), c = Symbol.for("react.suspense"), n = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), E = Symbol.for("react.activity"), C = Symbol.iterator;
  function x(s) {
    return s === null || typeof s != "object" ? null : (s = C && s[C] || s["@@iterator"], typeof s == "function" ? s : null);
  }
  var w = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, h = Object.assign, b = {};
  function T(s, _, A) {
    this.props = s, this.context = _, this.refs = b, this.updater = A || w;
  }
  T.prototype.isReactComponent = {}, T.prototype.setState = function(s, _) {
    if (typeof s != "object" && typeof s != "function" && s != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, s, _, "setState");
  }, T.prototype.forceUpdate = function(s) {
    this.updater.enqueueForceUpdate(this, s, "forceUpdate");
  };
  function S() {
  }
  S.prototype = T.prototype;
  function I(s, _, A) {
    this.props = s, this.context = _, this.refs = b, this.updater = A || w;
  }
  var $ = I.prototype = new S();
  $.constructor = I, h($, T.prototype), $.isPureReactComponent = !0;
  var m = Array.isArray;
  function v() {
  }
  var y = { H: null, A: null, T: null, S: null }, U = Object.prototype.hasOwnProperty;
  function k(s, _, A) {
    var P = A.ref;
    return {
      $$typeof: a,
      type: s,
      key: _,
      ref: P !== void 0 ? P : null,
      props: A
    };
  }
  function N(s, _) {
    return k(s.type, _, s.props);
  }
  function D(s) {
    return typeof s == "object" && s !== null && s.$$typeof === a;
  }
  function M(s) {
    var _ = { "=": "=0", ":": "=2" };
    return "$" + s.replace(/[=:]/g, function(A) {
      return _[A];
    });
  }
  var H = /\/+/g;
  function Y(s, _) {
    return typeof s == "object" && s !== null && s.key != null ? M("" + s.key) : _.toString(36);
  }
  function te(s) {
    switch (s.status) {
      case "fulfilled":
        return s.value;
      case "rejected":
        throw s.reason;
      default:
        switch (typeof s.status == "string" ? s.then(v, v) : (s.status = "pending", s.then(
          function(_) {
            s.status === "pending" && (s.status = "fulfilled", s.value = _);
          },
          function(_) {
            s.status === "pending" && (s.status = "rejected", s.reason = _);
          }
        )), s.status) {
          case "fulfilled":
            return s.value;
          case "rejected":
            throw s.reason;
        }
    }
    throw s;
  }
  function W(s, _, A, P, f) {
    var R = typeof s;
    (R === "undefined" || R === "boolean") && (s = null);
    var j = !1;
    if (s === null) j = !0;
    else
      switch (R) {
        case "bigint":
        case "string":
        case "number":
          j = !0;
          break;
        case "object":
          switch (s.$$typeof) {
            case a:
            case t:
              j = !0;
              break;
            case p:
              return j = s._init, W(
                j(s._payload),
                _,
                A,
                P,
                f
              );
          }
      }
    if (j)
      return f = f(s), j = P === "" ? "." + Y(s, 0) : P, m(f) ? (A = "", j != null && (A = j.replace(H, "$&/") + "/"), W(f, _, A, "", function(he) {
        return he;
      })) : f != null && (D(f) && (f = N(
        f,
        A + (f.key == null || s && s.key === f.key ? "" : ("" + f.key).replace(
          H,
          "$&/"
        ) + "/") + j
      )), _.push(f)), 1;
    j = 0;
    var K = P === "" ? "." : P + ":";
    if (m(s))
      for (var Q = 0; Q < s.length; Q++)
        P = s[Q], R = K + Y(P, Q), j += W(
          P,
          _,
          A,
          R,
          f
        );
    else if (Q = x(s), typeof Q == "function")
      for (s = Q.call(s), Q = 0; !(P = s.next()).done; )
        P = P.value, R = K + Y(P, Q++), j += W(
          P,
          _,
          A,
          R,
          f
        );
    else if (R === "object") {
      if (typeof s.then == "function")
        return W(
          te(s),
          _,
          A,
          P,
          f
        );
      throw _ = String(s), Error(
        "Objects are not valid as a React child (found: " + (_ === "[object Object]" ? "object with keys {" + Object.keys(s).join(", ") + "}" : _) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return j;
  }
  function ne(s, _, A) {
    if (s == null) return s;
    var P = [], f = 0;
    return W(s, P, "", "", function(R) {
      return _.call(A, R, f++);
    }), P;
  }
  function ce(s) {
    if (s._status === -1) {
      var _ = s._result;
      _ = _(), _.then(
        function(A) {
          (s._status === 0 || s._status === -1) && (s._status = 1, s._result = A);
        },
        function(A) {
          (s._status === 0 || s._status === -1) && (s._status = 2, s._result = A);
        }
      ), s._status === -1 && (s._status = 0, s._result = _);
    }
    if (s._status === 1) return s._result.default;
    throw s._result;
  }
  var oe = typeof reportError == "function" ? reportError : function(s) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var _ = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof s == "object" && s !== null && typeof s.message == "string" ? String(s.message) : String(s),
        error: s
      });
      if (!window.dispatchEvent(_)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", s);
      return;
    }
    console.error(s);
  }, de = {
    map: ne,
    forEach: function(s, _, A) {
      ne(
        s,
        function() {
          _.apply(this, arguments);
        },
        A
      );
    },
    count: function(s) {
      var _ = 0;
      return ne(s, function() {
        _++;
      }), _;
    },
    toArray: function(s) {
      return ne(s, function(_) {
        return _;
      }) || [];
    },
    only: function(s) {
      if (!D(s))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return s;
    }
  };
  return O.Activity = E, O.Children = de, O.Component = T, O.Fragment = l, O.Profiler = i, O.PureComponent = I, O.StrictMode = r, O.Suspense = c, O.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = y, O.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(s) {
      return y.H.useMemoCache(s);
    }
  }, O.cache = function(s) {
    return function() {
      return s.apply(null, arguments);
    };
  }, O.cacheSignal = function() {
    return null;
  }, O.cloneElement = function(s, _, A) {
    if (s == null)
      throw Error(
        "The argument must be a React element, but you passed " + s + "."
      );
    var P = h({}, s.props), f = s.key;
    if (_ != null)
      for (R in _.key !== void 0 && (f = "" + _.key), _)
        !U.call(_, R) || R === "key" || R === "__self" || R === "__source" || R === "ref" && _.ref === void 0 || (P[R] = _[R]);
    var R = arguments.length - 2;
    if (R === 1) P.children = A;
    else if (1 < R) {
      for (var j = Array(R), K = 0; K < R; K++)
        j[K] = arguments[K + 2];
      P.children = j;
    }
    return k(s.type, f, P);
  }, O.createContext = function(s) {
    return s = {
      $$typeof: o,
      _currentValue: s,
      _currentValue2: s,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, s.Provider = s, s.Consumer = {
      $$typeof: u,
      _context: s
    }, s;
  }, O.createElement = function(s, _, A) {
    var P, f = {}, R = null;
    if (_ != null)
      for (P in _.key !== void 0 && (R = "" + _.key), _)
        U.call(_, P) && P !== "key" && P !== "__self" && P !== "__source" && (f[P] = _[P]);
    var j = arguments.length - 2;
    if (j === 1) f.children = A;
    else if (1 < j) {
      for (var K = Array(j), Q = 0; Q < j; Q++)
        K[Q] = arguments[Q + 2];
      f.children = K;
    }
    if (s && s.defaultProps)
      for (P in j = s.defaultProps, j)
        f[P] === void 0 && (f[P] = j[P]);
    return k(s, R, f);
  }, O.createRef = function() {
    return { current: null };
  }, O.forwardRef = function(s) {
    return { $$typeof: d, render: s };
  }, O.isValidElement = D, O.lazy = function(s) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: s },
      _init: ce
    };
  }, O.memo = function(s, _) {
    return {
      $$typeof: n,
      type: s,
      compare: _ === void 0 ? null : _
    };
  }, O.startTransition = function(s) {
    var _ = y.T, A = {};
    y.T = A;
    try {
      var P = s(), f = y.S;
      f !== null && f(A, P), typeof P == "object" && P !== null && typeof P.then == "function" && P.then(v, oe);
    } catch (R) {
      oe(R);
    } finally {
      _ !== null && A.types !== null && (_.types = A.types), y.T = _;
    }
  }, O.unstable_useCacheRefresh = function() {
    return y.H.useCacheRefresh();
  }, O.use = function(s) {
    return y.H.use(s);
  }, O.useActionState = function(s, _, A) {
    return y.H.useActionState(s, _, A);
  }, O.useCallback = function(s, _) {
    return y.H.useCallback(s, _);
  }, O.useContext = function(s) {
    return y.H.useContext(s);
  }, O.useDebugValue = function() {
  }, O.useDeferredValue = function(s, _) {
    return y.H.useDeferredValue(s, _);
  }, O.useEffect = function(s, _) {
    return y.H.useEffect(s, _);
  }, O.useEffectEvent = function(s) {
    return y.H.useEffectEvent(s);
  }, O.useId = function() {
    return y.H.useId();
  }, O.useImperativeHandle = function(s, _, A) {
    return y.H.useImperativeHandle(s, _, A);
  }, O.useInsertionEffect = function(s, _) {
    return y.H.useInsertionEffect(s, _);
  }, O.useLayoutEffect = function(s, _) {
    return y.H.useLayoutEffect(s, _);
  }, O.useMemo = function(s, _) {
    return y.H.useMemo(s, _);
  }, O.useOptimistic = function(s, _) {
    return y.H.useOptimistic(s, _);
  }, O.useReducer = function(s, _, A) {
    return y.H.useReducer(s, _, A);
  }, O.useRef = function(s) {
    return y.H.useRef(s);
  }, O.useState = function(s) {
    return y.H.useState(s);
  }, O.useSyncExternalStore = function(s, _, A) {
    return y.H.useSyncExternalStore(
      s,
      _,
      A
    );
  }, O.useTransition = function() {
    return y.H.useTransition();
  }, O.version = "19.2.4", O;
}
var Ie;
function Dn() {
  return Ie || (Ie = 1, Se.exports = xn()), Se.exports;
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
var Fe;
function Pn() {
  if (Fe) return J;
  Fe = 1;
  var a = Dn();
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
    var E = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: E == null ? null : "" + E,
      children: c,
      containerInfo: n,
      implementation: p
    };
  }
  var o = a.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(c, n) {
    if (c === "font") return "";
    if (typeof n == "string")
      return n === "use-credentials" ? n : "";
  }
  return J.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, J.createPortal = function(c, n) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!n || n.nodeType !== 1 && n.nodeType !== 9 && n.nodeType !== 11)
      throw Error(t(299));
    return u(c, n, null, p);
  }, J.flushSync = function(c) {
    var n = o.T, p = r.p;
    try {
      if (o.T = null, r.p = 2, c) return c();
    } finally {
      o.T = n, r.p = p, r.d.f();
    }
  }, J.preconnect = function(c, n) {
    typeof c == "string" && (n ? (n = n.crossOrigin, n = typeof n == "string" ? n === "use-credentials" ? n : "" : void 0) : n = null, r.d.C(c, n));
  }, J.prefetchDNS = function(c) {
    typeof c == "string" && r.d.D(c);
  }, J.preinit = function(c, n) {
    if (typeof c == "string" && n && typeof n.as == "string") {
      var p = n.as, E = d(p, n.crossOrigin), C = typeof n.integrity == "string" ? n.integrity : void 0, x = typeof n.fetchPriority == "string" ? n.fetchPriority : void 0;
      p === "style" ? r.d.S(
        c,
        typeof n.precedence == "string" ? n.precedence : void 0,
        {
          crossOrigin: E,
          integrity: C,
          fetchPriority: x
        }
      ) : p === "script" && r.d.X(c, {
        crossOrigin: E,
        integrity: C,
        fetchPriority: x,
        nonce: typeof n.nonce == "string" ? n.nonce : void 0
      });
    }
  }, J.preinitModule = function(c, n) {
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
  }, J.preload = function(c, n) {
    if (typeof c == "string" && typeof n == "object" && n !== null && typeof n.as == "string") {
      var p = n.as, E = d(p, n.crossOrigin);
      r.d.L(c, p, {
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
  }, J.preloadModule = function(c, n) {
    if (typeof c == "string")
      if (n) {
        var p = d(n.as, n.crossOrigin);
        r.d.m(c, {
          as: typeof n.as == "string" && n.as !== "script" ? n.as : void 0,
          crossOrigin: p,
          integrity: typeof n.integrity == "string" ? n.integrity : void 0
        });
      } else r.d.m(c);
  }, J.requestFormReset = function(c) {
    r.d.r(c);
  }, J.unstable_batchedUpdates = function(c, n) {
    return c(n);
  }, J.useFormState = function(c, n, p) {
    return o.H.useFormState(c, n, p);
  }, J.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, J.version = "19.2.4", J;
}
var $e;
function jn() {
  if ($e) return ke.exports;
  $e = 1;
  function a() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(a);
      } catch (t) {
        console.error(t);
      }
  }
  return a(), ke.exports = Pn(), ke.exports;
}
var Mn = jn();
const { useState: me, useCallback: se, useRef: pe, useEffect: ie, useMemo: Te } = e;
function xe({ image: a }) {
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
  removeLabel: r
}) {
  const i = se(
    (u) => {
      u.stopPropagation(), l(a.value);
    },
    [l, a.value]
  );
  return /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chip" }, /* @__PURE__ */ e.createElement(xe, { image: a.image }), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, a.label), t && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDropdownSelect__chipRemove",
      onClick: i,
      "aria-label": r
    },
    "×"
  ));
}
function On({
  option: a,
  highlighted: t,
  searchTerm: l,
  onSelect: r,
  onMouseEnter: i,
  id: u
}) {
  const o = se(() => r(a.value), [r, a.value]), d = Te(() => {
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
      onClick: o,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(xe, { image: a.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const Bn = ({ controlId: a, state: t }) => {
  const l = X(), r = t.value ?? [], i = t.multiSelect === !0, u = t.mandatory === !0, o = t.disabled === !0, d = t.editable !== !1, c = t.optionsLoaded === !0, n = t.options ?? [], p = t.emptyOptionLabel ?? "", E = le({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), C = E["js.dropdownSelect.nothingFound"], x = se(
    (f) => E["js.dropdownSelect.removeChip"].replace("{0}", f),
    [E]
  ), [w, h] = me(!1), [b, T] = me(""), [S, I] = me(-1), [$, m] = me(!1), [v, y] = me({}), U = pe(null), k = pe(null), N = pe(null), D = pe(r);
  D.current = r;
  const M = pe(-1), H = Te(
    () => new Set(r.map((f) => f.value)),
    [r]
  ), Y = Te(() => {
    let f = n.filter((R) => !H.has(R.value));
    if (b) {
      const R = b.toLowerCase();
      f = f.filter((j) => j.label.toLowerCase().includes(R));
    }
    return f;
  }, [n, H, b]);
  ie(() => {
    b && Y.length === 1 ? I(0) : I(-1);
  }, [Y.length, b]), ie(() => {
    w && c && k.current && k.current.focus();
  }, [w, c, r]), ie(() => {
    var j, K;
    if (M.current < 0) return;
    const f = M.current;
    M.current = -1;
    const R = (j = U.current) == null ? void 0 : j.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    R && R.length > 0 ? R[Math.min(f, R.length - 1)].focus() : (K = U.current) == null || K.focus();
  }, [r]), ie(() => {
    if (!w) return;
    const f = (R) => {
      U.current && !U.current.contains(R.target) && N.current && !N.current.contains(R.target) && (h(!1), T(""));
    };
    return document.addEventListener("mousedown", f), () => document.removeEventListener("mousedown", f);
  }, [w]), ie(() => {
    if (!w || !U.current) return;
    const f = U.current.getBoundingClientRect(), R = window.innerHeight - f.bottom, K = R < 300 && f.top > R;
    y({
      left: f.left,
      width: f.width,
      ...K ? { bottom: window.innerHeight - f.top } : { top: f.bottom }
    });
  }, [w]);
  const te = se(async () => {
    if (!(o || !d) && (h(!0), T(""), I(-1), m(!1), !c))
      try {
        await l("loadOptions");
      } catch {
        m(!0);
      }
  }, [o, d, c, l]), W = se(() => {
    var f;
    h(!1), T(""), I(-1), (f = U.current) == null || f.focus();
  }, []), ne = se(
    (f) => {
      let R;
      if (i) {
        const j = n.find((K) => K.value === f);
        if (j)
          R = [...D.current, j];
        else
          return;
      } else {
        const j = n.find((K) => K.value === f);
        if (j)
          R = [j];
        else
          return;
      }
      D.current = R, l("valueChanged", { value: R.map((j) => j.value) }), i ? (T(""), I(-1)) : W();
    },
    [i, n, l, W]
  ), ce = se(
    (f) => {
      M.current = D.current.findIndex((j) => j.value === f);
      const R = D.current.filter((j) => j.value !== f);
      D.current = R, l("valueChanged", { value: R.map((j) => j.value) });
    },
    [l]
  ), oe = se(
    (f) => {
      f.stopPropagation(), l("valueChanged", { value: [] }), W();
    },
    [l, W]
  ), de = se((f) => {
    T(f.target.value);
  }, []), s = se(
    (f) => {
      if (!w) {
        if (f.key === "ArrowDown" || f.key === "ArrowUp" || f.key === "Enter" || f.key === " ") {
          if (f.target.tagName === "BUTTON") return;
          f.preventDefault(), f.stopPropagation(), te();
        }
        return;
      }
      switch (f.key) {
        case "ArrowDown":
          f.preventDefault(), f.stopPropagation(), I(
            (R) => R < Y.length - 1 ? R + 1 : 0
          );
          break;
        case "ArrowUp":
          f.preventDefault(), f.stopPropagation(), I(
            (R) => R > 0 ? R - 1 : Y.length - 1
          );
          break;
        case "Enter":
          f.preventDefault(), f.stopPropagation(), S >= 0 && S < Y.length && ne(Y[S].value);
          break;
        case "Escape":
          f.preventDefault(), f.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          b === "" && i && r.length > 0 && ce(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      te,
      W,
      Y,
      S,
      ne,
      b,
      i,
      r,
      ce
    ]
  ), _ = se(
    async (f) => {
      f.preventDefault(), m(!1);
      try {
        await l("loadOptions");
      } catch {
        m(!0);
      }
    },
    [l]
  );
  if (ie(() => {
    if (S < 0 || !N.current) return;
    const f = N.current.querySelector(
      `[id="${a}-opt-${S}"]`
    );
    f && f.scrollIntoView({ block: "nearest" });
  }, [S, a]), !d)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((f) => /* @__PURE__ */ e.createElement("span", { key: f.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(xe, { image: f.image }), /* @__PURE__ */ e.createElement("span", null, f.label))));
  const A = !u && r.length > 0 && !o, P = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlDropdownSelect__dropdown",
      style: v
    },
    (c || $) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: k,
        type: "text",
        className: "tlDropdownSelect__search",
        value: b,
        onChange: de,
        onKeyDown: s,
        placeholder: E["js.dropdownSelect.filterPlaceholder"],
        "aria-label": E["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": S >= 0 ? `${a}-opt-${S}` : void 0,
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
      !c && !$ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      $ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: _ }, E["js.dropdownSelect.error"])),
      c && Y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, C),
      c && Y.map((f, R) => /* @__PURE__ */ e.createElement(
        On,
        {
          key: f.value,
          id: `${a}-opt-${R}`,
          option: f,
          highlighted: R === S,
          searchTerm: b,
          onSelect: ne,
          onMouseEnter: () => I(R)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      ref: U,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (o ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${a}-listbox` : void 0,
      tabIndex: o ? -1 : 0,
      onClick: w ? void 0 : te,
      onKeyDown: s
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((f) => /* @__PURE__ */ e.createElement(
      An,
      {
        key: f.value,
        option: f,
        removable: !o && (i || !u),
        onRemove: ce,
        removeLabel: x(f.label)
      }
    ))),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, A && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: oe,
        "aria-label": E["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), P && Mn.createPortal(P, document.body));
};
B("TLButton", lt);
B("TLToggleButton", st);
B("TLTextInput", Ge);
B("TLNumberInput", Xe);
B("TLDatePicker", Qe);
B("TLSelect", et);
B("TLCheckbox", nt);
B("TLTable", rt);
B("TLCounter", ct);
B("TLTabBar", ut);
B("TLFieldList", dt);
B("TLAudioRecorder", pt);
B("TLAudioPlayer", ht);
B("TLFileUpload", _t);
B("TLDownload", vt);
B("TLPhotoCapture", yt);
B("TLPhotoViewer", wt);
B("TLSplitPanel", kt);
B("TLPanel", Dt);
B("TLMaximizeRoot", Pt);
B("TLDeckPane", jt);
B("TLSidebar", zt);
B("TLStack", Ut);
B("TLGrid", Vt);
B("TLCard", Ht);
B("TLAppBar", Wt);
B("TLBreadcrumb", Yt);
B("TLBottomBar", qt);
B("TLDialog", Qt);
B("TLDrawer", nn);
B("TLSnackbar", ln);
B("TLMenu", sn);
B("TLAppShell", cn);
B("TLTextCell", un);
B("TLTableView", mn);
B("TLFormLayout", vn);
B("TLFormGroup", Cn);
B("TLFormField", Nn);
B("TLResourceCell", Tn);
B("TLTreeView", Rn);
B("TLDropdownSelect", Bn);
