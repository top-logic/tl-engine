import { React as e, useTLFieldValue as pe, getComponent as Ke, useTLState as $, useTLCommand as q, TLChild as z, useTLUpload as Le, useI18N as ae, useTLDataUrl as Re, register as A } from "tl-react-bridge";
const { useCallback: Ye } = e, Ge = ({ controlId: a, state: t }) => {
  const [l, r] = pe(), i = Ye(
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
  const [r, i] = pe(), u = qe(
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
  const [l, r] = pe(), i = Ze(
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
  const [r, i] = pe(), u = Je(
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
  const [l, r] = pe(), i = tt(
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
  const i = $(), u = q(), o = t ?? "click", d = l ?? i.label, c = r ?? i.disabled === !0, n = at(() => {
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
  const u = $(), o = q(), d = t ?? "click", c = l ?? u.label, n = r ?? u.active === !0, p = i ?? u.disabled === !0, E = ot(() => {
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
  const t = $(), l = q(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: it } = e, ut = ({ controlId: a }) => {
  const t = $(), l = q(), r = t.tabs ?? [], i = t.activeTabId, u = it((o) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, dt = ({ controlId: a }) => {
  const t = $(), l = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(z, { control: i })))));
}, mt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, pt = ({ controlId: a }) => {
  const t = $(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(null), d = e.useRef(null), c = e.useRef([]), n = e.useRef(null), p = t.status ?? "idle", E = t.error, C = p === "received" ? "idle" : r !== "idle" ? r : p, x = e.useCallback(async () => {
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
        n.current = N, c.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", F = new MediaRecorder(N, B ? { mimeType: B } : void 0);
        d.current = F, F.ondataavailable = (m) => {
          m.data.size > 0 && c.current.push(m.data);
        }, F.onstop = async () => {
          N.getTracks().forEach((y) => y.stop()), n.current = null;
          const m = new Blob(c.current, { type: F.mimeType || "audio/webm" });
          if (c.current = [], m.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const v = new FormData();
          v.append("audio", m, "recording.webm"), await l(v), i("idle");
        }, F.start(), i("recording");
      } catch (N) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", N), o("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, l]), w = ae(mt), h = C === "recording" ? w["js.audioRecorder.stop"] : C === "uploading" ? w["js.uploading"] : w["js.audioRecorder.record"], _ = C === "uploading", L = ["tlAudioRecorder__button"];
  return C === "recording" && L.push("tlAudioRecorder__button--recording"), C === "uploading" && L.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: x,
      disabled: _,
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
  const t = $(), l = Re(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [u, o] = e.useState(r ? "idle" : "disabled"), d = e.useRef(null), c = e.useRef(null), n = e.useRef(i);
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
        const _ = await fetch(l);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), o("idle");
          return;
        }
        const L = await _.blob();
        c.current = URL.createObjectURL(L);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), o("idle");
        return;
      }
    }
    const h = new Audio(c.current);
    d.current = h, h.onended = () => {
      o("idle");
    }, h.play(), o("playing");
  }, [u, l]), E = ae(ft), C = u === "loading" ? E["js.loading"] : u === "playing" ? E["js.audioPlayer.pause"] : u === "disabled" ? E["js.audioPlayer.noAudio"] : E["js.audioPlayer.play"], x = u === "disabled" || u === "loading", w = ["tlAudioPlayer__button"];
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
  const t = $(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(!1), d = e.useRef(null), c = t.status ?? "idle", n = t.error, p = t.accept ?? "", E = c === "received" ? "idle" : r !== "idle" ? r : c, C = e.useCallback(async (m) => {
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
  }, []), _ = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), o(!1);
  }, []), L = e.useCallback((m) => {
    var y;
    if (m.preventDefault(), m.stopPropagation(), o(!1), r === "uploading") return;
    const v = (y = m.dataTransfer.files) == null ? void 0 : y[0];
    v && C(v);
  }, [r, C]), N = E === "uploading", B = ae(bt), F = E === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: h,
      onDragLeave: _,
      onDrop: L
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
        disabled: N,
        title: F,
        "aria-label": F
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
  const t = $(), l = Re(), r = q(), i = !!t.hasData, u = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [c, n] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || c)) {
      n(!0);
      try {
        const w = l + (l.includes("?") ? "&" : "?") + "rev=" + u, h = await fetch(w);
        if (!h.ok) {
          console.error("[TLDownload] Failed to fetch data:", h.status);
          return;
        }
        const _ = await h.blob(), L = URL.createObjectURL(_), N = document.createElement("a");
        N.href = L, N.download = o, N.style.display = "none", document.body.appendChild(N), N.click(), document.body.removeChild(N), URL.revokeObjectURL(L);
      } catch (w) {
        console.error("[TLDownload] Fetch error:", w);
      } finally {
        n(!1);
      }
    }
  }, [i, c, l, u, o]), E = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), C = ae(Et);
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
  const t = $(), l = Le(), [r, i] = e.useState("idle"), [u, o] = e.useState(null), [d, c] = e.useState(!1), n = e.useRef(null), p = e.useRef(null), E = e.useRef(null), C = e.useRef(null), x = e.useRef(null), w = t.error, h = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((T) => T.stop()), p.current = null), n.current && (n.current.srcObject = null);
  }, []), L = e.useCallback(() => {
    _(), i("idle");
  }, [_]), N = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (o(null), !h) {
        (T = C.current) == null || T.click();
        return;
      }
      try {
        const P = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = P, i("overlayOpen");
      } catch (P) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", P), o("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, h]), B = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = n.current, P = E.current;
    if (!T || !P)
      return;
    P.width = T.videoWidth, P.height = T.videoHeight;
    const D = P.getContext("2d");
    D && (D.drawImage(T, 0, 0), _(), i("uploading"), P.toBlob(async (H) => {
      if (!H) {
        i("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", H, "capture.jpg"), await l(K), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, l, _]), F = e.useCallback(async (T) => {
    var H;
    const P = (H = T.target.files) == null ? void 0 : H[0];
    if (!P) return;
    i("uploading");
    const D = new FormData();
    D.append("photo", P, P.name), await l(D), i("idle"), C.current && (C.current.value = "");
  }, [l]);
  e.useEffect(() => {
    r === "overlayOpen" && n.current && p.current && (n.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var P;
    if (r !== "overlayOpen") return;
    (P = x.current) == null || P.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const T = (P) => {
      P.key === "Escape" && L();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [r, L]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((T) => T.stop()), p.current = null);
  }, []);
  const m = ae(gt), v = r === "uploading" ? m["js.uploading"] : m["js.photoCapture.open"], y = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && y.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  d && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return d && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: N,
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
      onChange: F
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: L }),
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
        onClick: () => c((T) => !T),
        title: m["js.photoCapture.mirror"],
        "aria-label": m["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: m["js.photoCapture.capture"],
        "aria-label": m["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: L,
        title: m["js.photoCapture.close"],
        "aria-label": m["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m[u]), w && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w));
}, Ct = {
  "js.photoViewer.alt": "Captured photo"
}, wt = ({ controlId: a }) => {
  const t = $(), l = Re(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [u, o] = e.useState(null), d = e.useRef(i);
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
  const c = ae(Ct);
  return !r || !u ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: De, useRef: ve } = e, kt = ({ controlId: a }) => {
  const t = $(), l = q(), r = t.orientation, i = t.resizable === !0, u = t.children ?? [], o = r === "horizontal", d = u.length > 0 && u.every((_) => _.collapsed), c = !d && u.some((_) => _.collapsed), n = d ? !o : o, p = ve(null), E = ve(null), C = ve(null), x = De((_, L) => {
    const N = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? d && !n ? N.flex = "1 0 0%" : N.flex = "0 0 auto" : L !== void 0 ? N.flex = `0 0 ${L}px` : _.unit === "%" || c ? N.flex = `${_.size} 0 0%` : N.flex = `0 0 ${_.size}px`, _.minSize > 0 && !_.collapsed && (N.minWidth = o ? _.minSize : void 0, N.minHeight = o ? void 0 : _.minSize), N;
  }, [o, d, c, n]), w = De((_, L) => {
    _.preventDefault();
    const N = p.current;
    if (!N) return;
    const B = u[L], F = u[L + 1], m = N.querySelectorAll(":scope > .tlSplitPanel__child"), v = [];
    m.forEach((k) => {
      v.push(o ? k.offsetWidth : k.offsetHeight);
    }), C.current = v, E.current = {
      splitterIndex: L,
      startPos: o ? _.clientX : _.clientY,
      startSizeBefore: v[L],
      startSizeAfter: v[L + 1],
      childBefore: B,
      childAfter: F
    };
    const y = (k) => {
      const T = E.current;
      if (!T || !C.current) return;
      const D = (o ? k.clientX : k.clientY) - T.startPos, H = T.childBefore.minSize || 0, K = T.childAfter.minSize || 0;
      let ee = T.startSizeBefore + D, Y = T.startSizeAfter - D;
      ee < H && (Y += ee - H, ee = H), Y < K && (ee += Y - K, Y = K), C.current[T.splitterIndex] = ee, C.current[T.splitterIndex + 1] = Y;
      const oe = N.querySelectorAll(":scope > .tlSplitPanel__child"), ce = oe[T.splitterIndex], le = oe[T.splitterIndex + 1];
      ce && (ce.style.flex = `0 0 ${ee}px`), le && (le.style.flex = `0 0 ${Y}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", C.current) {
        const k = {};
        u.forEach((T, P) => {
          const D = T.control;
          D != null && D.controlId && C.current && (k[D.controlId] = C.current[P]);
        }), l("updateSizes", { sizes: k });
      }
      C.current = null, E.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", U), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, o, l]), h = [];
  return u.forEach((_, L) => {
    if (h.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${L}`,
          className: `tlSplitPanel__child${_.collapsed && n ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: x(_)
        },
        /* @__PURE__ */ e.createElement(z, { control: _.control })
      )
    ), i && L < u.length - 1) {
      const N = u[L + 1];
      !_.collapsed && !N.collapsed && h.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${L}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (F) => w(F, L)
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
}, { useCallback: ge } = e, St = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Rt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Dt = ({ controlId: a }) => {
  const t = $(), l = q(), r = ae(St), i = t.title, u = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, c = t.showPopOut === !0, n = t.toolbarButtons ?? [], p = u === "MINIMIZED", E = u === "MAXIMIZED", C = u === "HIDDEN", x = ge(() => {
    l("toggleMinimize");
  }, [l]), w = ge(() => {
    l("toggleMaximize");
  }, [l]), h = ge(() => {
    l("popOut");
  }, [l]);
  if (C)
    return null;
  const _ = E ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${u.toLowerCase()}`,
      style: _
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, n.map((L, N) => /* @__PURE__ */ e.createElement("span", { key: N, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(z, { control: L }))), o && !E && /* @__PURE__ */ e.createElement(
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
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(z, { control: t.child }))
  );
}, Pt = ({ controlId: a }) => {
  const t = $();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(z, { control: t.child })
  );
}, jt = ({ controlId: a }) => {
  const t = $();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(z, { control: t.activeChild }));
}, { useCallback: re, useState: he, useEffect: be, useRef: _e } = e, Mt = {
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
const ie = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, At = ({ item: a, active: t, collapsed: l, onSelect: r, tabIndex: i, itemRef: u, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(a.id),
    title: l ? a.label : void 0,
    tabIndex: i,
    ref: u,
    onFocus: () => o(a.id)
  },
  l && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ie, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(ie, { icon: a.icon }),
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
  /* @__PURE__ */ e.createElement(ie, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), Bt = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(ie, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), It = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ft = ({ item: a, activeItemId: t, anchorRect: l, onSelect: r, onExecute: i, onClose: u }) => {
  const o = _e(null);
  be(() => {
    const n = (p) => {
      o.current && !o.current.contains(p.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", n), () => document.removeEventListener("mousedown", n);
  }, [u]), be(() => {
    const n = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", n), () => document.removeEventListener("keydown", n);
  }, [u]);
  const d = re((n) => {
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
        /* @__PURE__ */ e.createElement(ie, { icon: n.icon }),
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
  const _ = _e(null), [L, N] = he(null), B = re(() => {
    r ? x === a.id ? h() : (_.current && N(_.current.getBoundingClientRect()), w(a.id)) : o(a.id);
  }, [r, x, a.id, o, w, h]), F = re((v) => {
    _.current = v, c(v);
  }, [c]), m = r && x === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (m ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: B,
      title: r ? a.label : void 0,
      "aria-expanded": r ? m : t,
      tabIndex: d,
      ref: F,
      onFocus: () => n(a.id)
    },
    /* @__PURE__ */ e.createElement(ie, { icon: a.icon }),
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
      anchorRect: L,
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
  const t = $(), l = q(), r = ae(Mt), i = t.items ?? [], u = t.activeItemId, o = t.collapsed, [d, c] = he(() => {
    const k = /* @__PURE__ */ new Map(), T = (P) => {
      for (const D of P)
        D.type === "group" && (k.set(D.id, D.expanded), T(D.children));
    };
    return T(i), k;
  }), n = re((k) => {
    c((T) => {
      const P = new Map(T), D = P.get(k) ?? !1;
      return P.set(k, !D), l("toggleGroup", { itemId: k, expanded: !D }), P;
    });
  }, [l]), p = re((k) => {
    k !== u && l("selectItem", { itemId: k });
  }, [l, u]), E = re((k) => {
    l("executeCommand", { itemId: k });
  }, [l]), C = re(() => {
    l("toggleCollapse", {});
  }, [l]), [x, w] = he(null), h = re((k) => {
    w(k);
  }, []), _ = re(() => {
    w(null);
  }, []);
  be(() => {
    o || w(null);
  }, [o]);
  const [L, N] = he(() => {
    const k = Ne(i, o, d);
    return k.length > 0 ? k[0].id : "";
  }), B = _e(/* @__PURE__ */ new Map()), F = re((k) => (T) => {
    T ? B.current.set(k, T) : B.current.delete(k);
  }, []), m = re((k) => {
    N(k);
  }, []), v = _e(0), y = re((k) => {
    N(k), v.current++;
  }, []);
  be(() => {
    const k = B.current.get(L);
    k && document.activeElement !== k && k.focus();
  }, [L, v.current]);
  const U = re((k) => {
    if (k.key === "Escape" && x !== null) {
      k.preventDefault(), _();
      return;
    }
    const T = Ne(i, o, d);
    if (T.length === 0) return;
    const P = T.findIndex((H) => H.id === L);
    if (P < 0) return;
    const D = T[P];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const H = (P + 1) % T.length;
        y(T[H].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const H = (P - 1 + T.length) % T.length;
        y(T[H].id);
        break;
      }
      case "Home": {
        k.preventDefault(), y(T[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), y(T[T.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), D.type === "nav" ? p(D.id) : D.type === "command" ? E(D.id) : D.type === "group" && (o ? x === D.id ? _() : h(D.id) : n(D.id));
        break;
      }
      case "ArrowRight": {
        D.type === "group" && !o && ((d.get(D.id) ?? !1) || (k.preventDefault(), n(D.id)));
        break;
      }
      case "ArrowLeft": {
        D.type === "group" && !o && (d.get(D.id) ?? !1) && (k.preventDefault(), n(D.id));
        break;
      }
    }
  }, [
    i,
    o,
    d,
    L,
    x,
    y,
    p,
    E,
    n,
    h,
    _
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, i.map((k) => /* @__PURE__ */ e.createElement(
    ze,
    {
      key: k.id,
      item: k,
      activeItemId: u,
      collapsed: o,
      onSelect: p,
      onExecute: E,
      onToggleGroup: n,
      focusedId: L,
      setItemRef: F,
      onItemFocus: m,
      groupStates: d,
      flyoutGroupId: x,
      onOpenFlyout: h,
      onCloseFlyout: _
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, Ut = ({ controlId: a }) => {
  const t = $(), l = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", u = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${l}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: d }, o.map((c, n) => /* @__PURE__ */ e.createElement(z, { key: n, control: c })));
}, Vt = ({ controlId: a }) => {
  const t = $(), l = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", u = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : l && (o.gridTemplateColumns = `repeat(${l}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${i}`, style: o }, u.map((d, c) => /* @__PURE__ */ e.createElement(z, { key: c, control: d })));
}, Ht = ({ controlId: a }) => {
  const t = $(), l = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", u = t.headerActions ?? [], o = t.child, d = l != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${r}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, l && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, l), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((c, n) => /* @__PURE__ */ e.createElement(z, { key: n, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(z, { control: o })));
}, Wt = ({ controlId: a }) => {
  const t = $(), l = t.title ?? "", r = t.leading, i = t.actions ?? [], u = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: d }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, l), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((c, n) => /* @__PURE__ */ e.createElement(z, { key: n, control: c }))));
}, { useCallback: Kt } = e, Yt = ({ controlId: a }) => {
  const t = $(), l = q(), r = t.items ?? [], i = Kt((u) => {
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
  const t = $(), l = q(), r = t.items ?? [], i = t.activeItemId, u = Gt((o) => {
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
  const t = $(), l = q(), r = ae(Zt), i = t.open === !0, u = t.title ?? "", o = t.size ?? "medium", d = t.closeOnBackdrop !== !1, c = t.actions ?? [], n = t.child, p = Xt(null), E = Pe(() => {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(z, { control: n })),
    c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, c.map((w, h) => /* @__PURE__ */ e.createElement(z, { key: h, control: w })))
  ));
}, { useCallback: Jt, useEffect: en } = e, tn = {
  "js.drawer.close": "Close"
}, nn = ({ controlId: a }) => {
  const t = $(), l = q(), r = ae(tn), i = t.open === !0, u = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, c = t.child, n = Jt(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, c && /* @__PURE__ */ e.createElement(z, { control: c })));
}, { useCallback: Me, useEffect: rn, useState: an } = e, ln = ({ controlId: a }) => {
  const t = $(), l = q(), r = t.message ?? "", i = t.content ?? "", u = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, c = t.visible === !0, [n, p] = an(!1), E = Me(() => {
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
}, { useCallback: ye, useEffect: Ce, useRef: on, useState: Ae } = e, sn = ({ controlId: a }) => {
  const t = $(), l = q(), r = t.open === !0, i = t.anchorId, u = t.items ?? [], o = on(null), [d, c] = Ae({ top: 0, left: 0 }), [n, p] = Ae(0), E = u.filter((h) => h.type === "item" && !h.disabled);
  Ce(() => {
    var m, v;
    if (!r || !i) return;
    const h = document.getElementById(i);
    if (!h) return;
    const _ = h.getBoundingClientRect(), L = ((m = o.current) == null ? void 0 : m.offsetHeight) ?? 200, N = ((v = o.current) == null ? void 0 : v.offsetWidth) ?? 200;
    let B = _.bottom + 4, F = _.left;
    B + L > window.innerHeight && (B = _.top - L - 4), F + N > window.innerWidth && (F = _.right - N), c({ top: B, left: F }), p(0);
  }, [r, i]);
  const C = ye(() => {
    l("close");
  }, [l]), x = ye((h) => {
    l("selectItem", { itemId: h });
  }, [l]);
  Ce(() => {
    if (!r) return;
    const h = (_) => {
      o.current && !o.current.contains(_.target) && C();
    };
    return document.addEventListener("mousedown", h), () => document.removeEventListener("mousedown", h);
  }, [r, C]);
  const w = ye((h) => {
    if (h.key === "Escape") {
      C();
      return;
    }
    if (h.key === "ArrowDown")
      h.preventDefault(), p((_) => (_ + 1) % E.length);
    else if (h.key === "ArrowUp")
      h.preventDefault(), p((_) => (_ - 1 + E.length) % E.length);
    else if (h.key === "Enter" || h.key === " ") {
      h.preventDefault();
      const _ = E[n];
      _ && x(_.id);
    }
  }, [C, x, E, n]);
  return Ce(() => {
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
    u.map((h, _) => {
      if (h.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: _, className: "tlMenu__separator" });
      const N = E.indexOf(h) === n;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: h.id,
          type: "button",
          className: "tlMenu__item" + (N ? " tlMenu__item--focused" : "") + (h.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: h.disabled,
          tabIndex: N ? 0 : -1,
          onClick: () => x(h.id)
        },
        h.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + h.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, h.label)
      );
    })
  ) : null;
}, cn = ({ controlId: a }) => {
  const t = $(), l = t.header, r = t.content, i = t.footer, u = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(z, { control: l })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(z, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(z, { control: i })), /* @__PURE__ */ e.createElement(z, { control: u }));
}, un = () => {
  const t = $().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, dn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Oe = 50, mn = () => {
  const a = $(), t = q(), l = ae(dn), r = a.columns ?? [], i = a.totalRowCount ?? 0, u = a.rows ?? [], o = a.rowHeight ?? 36, d = a.selectionMode ?? "single", c = a.selectedCount ?? 0, n = a.frozenColumnCount ?? 0, p = a.treeMode ?? !1, E = e.useMemo(
    () => r.filter((g) => g.sortPriority && g.sortPriority > 0).length,
    [r]
  ), C = d === "multi", x = 40, w = 20, h = e.useRef(null), _ = e.useRef(null), L = e.useRef(null), [N, B] = e.useState({}), F = e.useRef(null), m = e.useRef(!1), v = e.useRef(null), [y, U] = e.useState(null), [k, T] = e.useState(null);
  e.useEffect(() => {
    F.current || B({});
  }, [r]);
  const P = e.useCallback((g) => N[g.name] ?? g.width, [N]), D = e.useMemo(() => {
    const g = [];
    let R = C && n > 0 ? x : 0;
    for (let I = 0; I < n && I < r.length; I++)
      g.push(R), R += P(r[I]);
    return g;
  }, [r, n, C, x, P]), H = i * o, K = e.useCallback((g, R, I) => {
    I.preventDefault(), I.stopPropagation(), F.current = { column: g, startX: I.clientX, startWidth: R };
    const W = (J) => {
      const ne = F.current;
      if (!ne) return;
      const X = Math.max(Oe, ne.startWidth + (J.clientX - ne.startX));
      B((Ee) => ({ ...Ee, [ne.column]: X }));
    }, G = (J) => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", G);
      const ne = F.current;
      if (ne) {
        const X = Math.max(Oe, ne.startWidth + (J.clientX - ne.startX));
        t("columnResize", { column: ne.column, width: X }), F.current = null, m.current = !0, requestAnimationFrame(() => {
          m.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", G);
  }, [t]), ee = e.useCallback(() => {
    h.current && _.current && (h.current.scrollLeft = _.current.scrollLeft), L.current !== null && clearTimeout(L.current), L.current = window.setTimeout(() => {
      const g = _.current;
      if (!g) return;
      const R = g.scrollTop, I = Math.ceil(g.clientHeight / o), W = Math.floor(R / o);
      t("scroll", { start: W, count: I });
    }, 80);
  }, [t, o]), Y = e.useCallback((g, R, I) => {
    if (m.current) return;
    let W;
    !R || R === "desc" ? W = "asc" : W = "desc";
    const G = I.shiftKey ? "add" : "replace";
    t("sort", { column: g, direction: W, mode: G });
  }, [t]), oe = e.useCallback((g, R) => {
    v.current = g, R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", g);
  }, []), ce = e.useCallback((g, R) => {
    if (!v.current || v.current === g) {
      U(null);
      return;
    }
    R.preventDefault(), R.dataTransfer.dropEffect = "move";
    const I = R.currentTarget.getBoundingClientRect(), W = R.clientX < I.left + I.width / 2 ? "left" : "right";
    U({ column: g, side: W });
  }, []), le = e.useCallback((g) => {
    g.preventDefault(), g.stopPropagation();
    const R = v.current;
    if (!R || !y) {
      v.current = null, U(null);
      return;
    }
    let I = r.findIndex((G) => G.name === y.column);
    if (I < 0) {
      v.current = null, U(null);
      return;
    }
    const W = r.findIndex((G) => G.name === R);
    y.side === "right" && I++, W < I && I--, t("columnReorder", { column: R, targetIndex: I }), v.current = null, U(null);
  }, [r, y, t]), ue = e.useCallback(() => {
    v.current = null, U(null);
  }, []), s = e.useCallback((g, R) => {
    R.shiftKey && R.preventDefault(), t("select", {
      rowIndex: g,
      ctrlKey: R.ctrlKey || R.metaKey,
      shiftKey: R.shiftKey
    });
  }, [t]), b = e.useCallback((g, R) => {
    R.stopPropagation(), t("select", { rowIndex: g, ctrlKey: !0, shiftKey: !1 });
  }, [t]), f = e.useCallback(() => {
    const g = c === i && i > 0;
    t("selectAll", { selected: !g });
  }, [t, c, i]), S = e.useCallback((g, R, I) => {
    I.stopPropagation(), t("expand", { rowIndex: g, expanded: R });
  }, [t]), M = e.useCallback((g, R) => {
    R.preventDefault(), T({ x: R.clientX, y: R.clientY, colIdx: g });
  }, []), O = e.useCallback(() => {
    k && (t("setFrozenColumnCount", { count: k.colIdx + 1 }), T(null));
  }, [k, t]), V = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), T(null);
  }, [t]);
  e.useEffect(() => {
    if (!k) return;
    const g = () => T(null), R = (I) => {
      I.key === "Escape" && T(null);
    };
    return document.addEventListener("mousedown", g), document.addEventListener("keydown", R), () => {
      document.removeEventListener("mousedown", g), document.removeEventListener("keydown", R);
    };
  }, [k]);
  const te = r.reduce((g, R) => g + P(R), 0) + (C ? x : 0), Z = c === i && i > 0, fe = c > 0 && c < i, Ve = e.useCallback((g) => {
    g && (g.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (g) => {
        if (!v.current) return;
        g.preventDefault();
        const R = _.current, I = h.current;
        if (!R) return;
        const W = R.getBoundingClientRect(), G = 40, J = 8;
        g.clientX < W.left + G ? R.scrollLeft = Math.max(0, R.scrollLeft - J) : g.clientX > W.right - G && (R.scrollLeft += J), I && (I.scrollLeft = R.scrollLeft);
      },
      onDrop: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: te } }, C && /* @__PURE__ */ e.createElement(
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
          checked: Z,
          onChange: f
        }
      )
    ), r.map((g, R) => {
      const I = P(g), W = R === r.length - 1;
      let G = "tlTableView__headerCell";
      g.sortable && (G += " tlTableView__headerCell--sortable"), y && y.column === g.name && (G += " tlTableView__headerCell--dragOver-" + y.side);
      const J = R < n, ne = R === n - 1;
      return J && (G += " tlTableView__headerCell--frozen"), ne && (G += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.name,
          className: G,
          style: {
            ...W && !J ? { flex: "1 0 auto", minWidth: I } : { width: I, minWidth: I },
            position: J ? "sticky" : "relative",
            ...J ? { left: D[R], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: g.sortable ? (X) => Y(g.name, g.sortDirection, X) : void 0,
          onContextMenu: (X) => M(R, X),
          onDragStart: (X) => oe(g.name, X),
          onDragOver: (X) => ce(g.name, X),
          onDrop: le,
          onDragEnd: ue
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, g.label),
        g.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, g.sortDirection === "asc" ? "▲" : "▼", E > 1 && g.sortPriority != null && g.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, g.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (X) => K(g.name, I, X)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (g) => {
          if (v.current && r.length > 0) {
            const R = r[r.length - 1];
            R.name !== v.current && (g.preventDefault(), g.dataTransfer.dropEffect = "move", U({ column: R.name, side: "right" }));
          }
        },
        onDrop: le
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: _,
        className: "tlTableView__body",
        onScroll: ee
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: H, position: "relative", minWidth: te } }, u.map((g) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: g.id,
          className: "tlTableView__row" + (g.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: g.index * o,
            height: o,
            minWidth: te,
            width: "100%"
          },
          onClick: (R) => s(g.index, R)
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
            onClick: (R) => R.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: g.selected,
              onChange: () => {
              },
              onClick: (R) => b(g.index, R),
              tabIndex: -1
            }
          )
        ),
        r.map((R, I) => {
          const W = P(R), G = I === r.length - 1, J = I < n, ne = I === n - 1;
          let X = "tlTableView__cell";
          J && (X += " tlTableView__cell--frozen"), ne && (X += " tlTableView__cell--frozenLast");
          const Ee = p && I === 0, He = g.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: R.name,
              className: X,
              style: {
                ...G && !J ? { flex: "1 0 auto", minWidth: W } : { width: W, minWidth: W },
                ...J ? { position: "sticky", left: D[I], zIndex: 2 } : {}
              }
            },
            Ee ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: He * w } }, g.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (We) => S(g.index, !g.expanded, We)
              },
              g.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(z, { control: g.cells[R.name] })) : /* @__PURE__ */ e.createElement(z, { control: g.cells[R.name] })
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
      k.colIdx + 1 !== n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: O }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.freezeUpTo"])),
      n > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: V }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.unfreezeAll"]))
    )
  );
}, pn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ue = e.createContext(pn), { useMemo: fn, useRef: hn, useState: bn, useEffect: _n } = e, En = 320, vn = ({ controlId: a }) => {
  const t = $(), l = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, u = t.children ?? [], o = t.noModelMessage, d = hn(null), [c, n] = bn(
    r === "top" ? "top" : "side"
  );
  _n(() => {
    if (r !== "auto") {
      n(r);
      return;
    }
    const w = d.current;
    if (!w) return;
    const h = new ResizeObserver((_) => {
      for (const L of _) {
        const B = L.contentRect.width / l;
        n(B < En ? "top" : "side");
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
  return o ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(Ue.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: a, className: x, style: C, ref: d }, u.map((w, h) => /* @__PURE__ */ e.createElement(z, { key: h, control: w }))));
}, { useCallback: gn } = e, yn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Cn = ({ controlId: a }) => {
  const t = $(), l = q(), r = ae(yn), i = t.header, u = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, c = t.border ?? "none", n = t.fullLine === !0, p = t.children ?? [], E = i != null || u.length > 0 || o, C = gn(() => {
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, i), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((w, h) => /* @__PURE__ */ e.createElement(z, { key: h, control: w })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((w, h) => /* @__PURE__ */ e.createElement(z, { key: h, control: w }))));
}, { useContext: wn, useState: kn, useCallback: Sn } = e, Nn = ({ controlId: a }) => {
  const t = $(), l = wn(Ue), r = t.label ?? "", i = t.required === !0, u = t.error, o = t.helpText, d = t.dirty === !0, c = t.labelPosition ?? l.resolvedLabelPosition, n = t.fullLine === !0, p = t.visible !== !1, E = t.field, C = l.readOnly, [x, w] = kn(!1), h = Sn(() => w((N) => !N), []);
  if (!p) return null;
  const _ = u != null, L = [
    "tlFormField",
    `tlFormField--${c}`,
    C ? "tlFormField--readonly" : "",
    n ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), i && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !C && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(z, { control: E })), !C && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  const a = $(), t = q(), l = a.iconCss, r = a.iconSrc, i = a.label, u = a.cssClass, o = a.tooltip, d = a.hasLink, c = l ? /* @__PURE__ */ e.createElement("i", { className: l }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, n = /* @__PURE__ */ e.createElement(e.Fragment, null, c, i && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, i)), p = e.useCallback((C) => {
    C.preventDefault(), t("goto", {});
  }, [t]), E = ["tlResourceCell", u].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: E, href: "#", onClick: p, title: o }, n) : /* @__PURE__ */ e.createElement("span", { className: E, title: o }, n);
}, Ln = 20, Rn = () => {
  const a = $(), t = q(), l = a.nodes ?? [], r = a.selectionMode ?? "single", i = a.dragEnabled ?? !1, u = a.dropEnabled ?? !1, o = a.dropIndicatorNodeId ?? null, d = a.dropIndicatorPosition ?? null, [c, n] = e.useState(-1), p = e.useRef(null), E = e.useCallback((m, v) => {
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
  }, []), _ = e.useCallback((m, v) => {
    v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", m);
  }, []), L = e.useCallback((m, v) => {
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const y = h(v, v.currentTarget);
    w.current != null && window.clearTimeout(w.current), w.current = window.setTimeout(() => {
      t("dragOver", { nodeId: m, position: y }), w.current = null;
    }, 50);
  }, [t, h]), N = e.useCallback((m, v) => {
    v.preventDefault(), w.current != null && (window.clearTimeout(w.current), w.current = null);
    const y = h(v, v.currentTarget);
    t("drop", { nodeId: m, position: y });
  }, [t, h]), B = e.useCallback(() => {
    w.current != null && (window.clearTimeout(w.current), w.current = null), t("dragEnd");
  }, [t]), F = e.useCallback((m) => {
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
      onKeyDown: F
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
        onDragStart: (y) => _(m.id, y),
        onDragOver: u ? (y) => L(m.id, y) : void 0,
        onDrop: u ? (y) => N(m.id, y) : void 0,
        onDragEnd: B
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
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(z, { control: m.content }))
    ))
  );
};
var we = { exports: {} }, Q = {}, ke = { exports: {} }, j = {};
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
  if (Be) return j;
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
  }, h = Object.assign, _ = {};
  function L(s, b, f) {
    this.props = s, this.context = b, this.refs = _, this.updater = f || w;
  }
  L.prototype.isReactComponent = {}, L.prototype.setState = function(s, b) {
    if (typeof s != "object" && typeof s != "function" && s != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, s, b, "setState");
  }, L.prototype.forceUpdate = function(s) {
    this.updater.enqueueForceUpdate(this, s, "forceUpdate");
  };
  function N() {
  }
  N.prototype = L.prototype;
  function B(s, b, f) {
    this.props = s, this.context = b, this.refs = _, this.updater = f || w;
  }
  var F = B.prototype = new N();
  F.constructor = B, h(F, L.prototype), F.isPureReactComponent = !0;
  var m = Array.isArray;
  function v() {
  }
  var y = { H: null, A: null, T: null, S: null }, U = Object.prototype.hasOwnProperty;
  function k(s, b, f) {
    var S = f.ref;
    return {
      $$typeof: a,
      type: s,
      key: b,
      ref: S !== void 0 ? S : null,
      props: f
    };
  }
  function T(s, b) {
    return k(s.type, b, s.props);
  }
  function P(s) {
    return typeof s == "object" && s !== null && s.$$typeof === a;
  }
  function D(s) {
    var b = { "=": "=0", ":": "=2" };
    return "$" + s.replace(/[=:]/g, function(f) {
      return b[f];
    });
  }
  var H = /\/+/g;
  function K(s, b) {
    return typeof s == "object" && s !== null && s.key != null ? D("" + s.key) : b.toString(36);
  }
  function ee(s) {
    switch (s.status) {
      case "fulfilled":
        return s.value;
      case "rejected":
        throw s.reason;
      default:
        switch (typeof s.status == "string" ? s.then(v, v) : (s.status = "pending", s.then(
          function(b) {
            s.status === "pending" && (s.status = "fulfilled", s.value = b);
          },
          function(b) {
            s.status === "pending" && (s.status = "rejected", s.reason = b);
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
  function Y(s, b, f, S, M) {
    var O = typeof s;
    (O === "undefined" || O === "boolean") && (s = null);
    var V = !1;
    if (s === null) V = !0;
    else
      switch (O) {
        case "bigint":
        case "string":
        case "number":
          V = !0;
          break;
        case "object":
          switch (s.$$typeof) {
            case a:
            case t:
              V = !0;
              break;
            case p:
              return V = s._init, Y(
                V(s._payload),
                b,
                f,
                S,
                M
              );
          }
      }
    if (V)
      return M = M(s), V = S === "" ? "." + K(s, 0) : S, m(M) ? (f = "", V != null && (f = V.replace(H, "$&/") + "/"), Y(M, b, f, "", function(fe) {
        return fe;
      })) : M != null && (P(M) && (M = T(
        M,
        f + (M.key == null || s && s.key === M.key ? "" : ("" + M.key).replace(
          H,
          "$&/"
        ) + "/") + V
      )), b.push(M)), 1;
    V = 0;
    var te = S === "" ? "." : S + ":";
    if (m(s))
      for (var Z = 0; Z < s.length; Z++)
        S = s[Z], O = te + K(S, Z), V += Y(
          S,
          b,
          f,
          O,
          M
        );
    else if (Z = x(s), typeof Z == "function")
      for (s = Z.call(s), Z = 0; !(S = s.next()).done; )
        S = S.value, O = te + K(S, Z++), V += Y(
          S,
          b,
          f,
          O,
          M
        );
    else if (O === "object") {
      if (typeof s.then == "function")
        return Y(
          ee(s),
          b,
          f,
          S,
          M
        );
      throw b = String(s), Error(
        "Objects are not valid as a React child (found: " + (b === "[object Object]" ? "object with keys {" + Object.keys(s).join(", ") + "}" : b) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return V;
  }
  function oe(s, b, f) {
    if (s == null) return s;
    var S = [], M = 0;
    return Y(s, S, "", "", function(O) {
      return b.call(f, O, M++);
    }), S;
  }
  function ce(s) {
    if (s._status === -1) {
      var b = s._result;
      b = b(), b.then(
        function(f) {
          (s._status === 0 || s._status === -1) && (s._status = 1, s._result = f);
        },
        function(f) {
          (s._status === 0 || s._status === -1) && (s._status = 2, s._result = f);
        }
      ), s._status === -1 && (s._status = 0, s._result = b);
    }
    if (s._status === 1) return s._result.default;
    throw s._result;
  }
  var le = typeof reportError == "function" ? reportError : function(s) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var b = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof s == "object" && s !== null && typeof s.message == "string" ? String(s.message) : String(s),
        error: s
      });
      if (!window.dispatchEvent(b)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", s);
      return;
    }
    console.error(s);
  }, ue = {
    map: oe,
    forEach: function(s, b, f) {
      oe(
        s,
        function() {
          b.apply(this, arguments);
        },
        f
      );
    },
    count: function(s) {
      var b = 0;
      return oe(s, function() {
        b++;
      }), b;
    },
    toArray: function(s) {
      return oe(s, function(b) {
        return b;
      }) || [];
    },
    only: function(s) {
      if (!P(s))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return s;
    }
  };
  return j.Activity = E, j.Children = ue, j.Component = L, j.Fragment = l, j.Profiler = i, j.PureComponent = B, j.StrictMode = r, j.Suspense = c, j.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = y, j.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(s) {
      return y.H.useMemoCache(s);
    }
  }, j.cache = function(s) {
    return function() {
      return s.apply(null, arguments);
    };
  }, j.cacheSignal = function() {
    return null;
  }, j.cloneElement = function(s, b, f) {
    if (s == null)
      throw Error(
        "The argument must be a React element, but you passed " + s + "."
      );
    var S = h({}, s.props), M = s.key;
    if (b != null)
      for (O in b.key !== void 0 && (M = "" + b.key), b)
        !U.call(b, O) || O === "key" || O === "__self" || O === "__source" || O === "ref" && b.ref === void 0 || (S[O] = b[O]);
    var O = arguments.length - 2;
    if (O === 1) S.children = f;
    else if (1 < O) {
      for (var V = Array(O), te = 0; te < O; te++)
        V[te] = arguments[te + 2];
      S.children = V;
    }
    return k(s.type, M, S);
  }, j.createContext = function(s) {
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
  }, j.createElement = function(s, b, f) {
    var S, M = {}, O = null;
    if (b != null)
      for (S in b.key !== void 0 && (O = "" + b.key), b)
        U.call(b, S) && S !== "key" && S !== "__self" && S !== "__source" && (M[S] = b[S]);
    var V = arguments.length - 2;
    if (V === 1) M.children = f;
    else if (1 < V) {
      for (var te = Array(V), Z = 0; Z < V; Z++)
        te[Z] = arguments[Z + 2];
      M.children = te;
    }
    if (s && s.defaultProps)
      for (S in V = s.defaultProps, V)
        M[S] === void 0 && (M[S] = V[S]);
    return k(s, O, M);
  }, j.createRef = function() {
    return { current: null };
  }, j.forwardRef = function(s) {
    return { $$typeof: d, render: s };
  }, j.isValidElement = P, j.lazy = function(s) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: s },
      _init: ce
    };
  }, j.memo = function(s, b) {
    return {
      $$typeof: n,
      type: s,
      compare: b === void 0 ? null : b
    };
  }, j.startTransition = function(s) {
    var b = y.T, f = {};
    y.T = f;
    try {
      var S = s(), M = y.S;
      M !== null && M(f, S), typeof S == "object" && S !== null && typeof S.then == "function" && S.then(v, le);
    } catch (O) {
      le(O);
    } finally {
      b !== null && f.types !== null && (b.types = f.types), y.T = b;
    }
  }, j.unstable_useCacheRefresh = function() {
    return y.H.useCacheRefresh();
  }, j.use = function(s) {
    return y.H.use(s);
  }, j.useActionState = function(s, b, f) {
    return y.H.useActionState(s, b, f);
  }, j.useCallback = function(s, b) {
    return y.H.useCallback(s, b);
  }, j.useContext = function(s) {
    return y.H.useContext(s);
  }, j.useDebugValue = function() {
  }, j.useDeferredValue = function(s, b) {
    return y.H.useDeferredValue(s, b);
  }, j.useEffect = function(s, b) {
    return y.H.useEffect(s, b);
  }, j.useEffectEvent = function(s) {
    return y.H.useEffectEvent(s);
  }, j.useId = function() {
    return y.H.useId();
  }, j.useImperativeHandle = function(s, b, f) {
    return y.H.useImperativeHandle(s, b, f);
  }, j.useInsertionEffect = function(s, b) {
    return y.H.useInsertionEffect(s, b);
  }, j.useLayoutEffect = function(s, b) {
    return y.H.useLayoutEffect(s, b);
  }, j.useMemo = function(s, b) {
    return y.H.useMemo(s, b);
  }, j.useOptimistic = function(s, b) {
    return y.H.useOptimistic(s, b);
  }, j.useReducer = function(s, b, f) {
    return y.H.useReducer(s, b, f);
  }, j.useRef = function(s) {
    return y.H.useRef(s);
  }, j.useState = function(s) {
    return y.H.useState(s);
  }, j.useSyncExternalStore = function(s, b, f) {
    return y.H.useSyncExternalStore(
      s,
      b,
      f
    );
  }, j.useTransition = function() {
    return y.H.useTransition();
  }, j.version = "19.2.4", j;
}
var Ie;
function Dn() {
  return Ie || (Ie = 1, ke.exports = xn()), ke.exports;
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
  if (Fe) return Q;
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
  return Q.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, Q.createPortal = function(c, n) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!n || n.nodeType !== 1 && n.nodeType !== 9 && n.nodeType !== 11)
      throw Error(t(299));
    return u(c, n, null, p);
  }, Q.flushSync = function(c) {
    var n = o.T, p = r.p;
    try {
      if (o.T = null, r.p = 2, c) return c();
    } finally {
      o.T = n, r.p = p, r.d.f();
    }
  }, Q.preconnect = function(c, n) {
    typeof c == "string" && (n ? (n = n.crossOrigin, n = typeof n == "string" ? n === "use-credentials" ? n : "" : void 0) : n = null, r.d.C(c, n));
  }, Q.prefetchDNS = function(c) {
    typeof c == "string" && r.d.D(c);
  }, Q.preinit = function(c, n) {
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
  }, Q.preinitModule = function(c, n) {
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
  }, Q.preload = function(c, n) {
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
  }, Q.preloadModule = function(c, n) {
    if (typeof c == "string")
      if (n) {
        var p = d(n.as, n.crossOrigin);
        r.d.m(c, {
          as: typeof n.as == "string" && n.as !== "script" ? n.as : void 0,
          crossOrigin: p,
          integrity: typeof n.integrity == "string" ? n.integrity : void 0
        });
      } else r.d.m(c);
  }, Q.requestFormReset = function(c) {
    r.d.r(c);
  }, Q.unstable_batchedUpdates = function(c, n) {
    return c(n);
  }, Q.useFormState = function(c, n, p) {
    return o.H.useFormState(c, n, p);
  }, Q.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, Q.version = "19.2.4", Q;
}
var $e;
function jn() {
  if ($e) return we.exports;
  $e = 1;
  function a() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(a);
      } catch (t) {
        console.error(t);
      }
  }
  return a(), we.exports = Pn(), we.exports;
}
var Mn = jn();
const { useState: de, useCallback: se, useRef: Se, useEffect: me, useMemo: Te } = e;
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
      "aria-label": r,
      tabIndex: -1
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
  const l = q(), r = t.value ?? [], i = t.multiSelect === !0, u = t.mandatory === !0, o = t.disabled === !0, d = t.editable !== !1, c = t.optionsLoaded === !0, n = t.options ?? [], p = t.emptyOptionLabel ?? "", E = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), C = E["js.dropdownSelect.nothingFound"], x = se(
    (f) => E["js.dropdownSelect.removeChip"].replace("{0}", f),
    [E]
  ), [w, h] = de(!1), [_, L] = de(""), [N, B] = de(-1), [F, m] = de(!1), [v, y] = de({}), U = Se(null), k = Se(null), T = Se(null), P = Te(
    () => new Set(r.map((f) => f.value)),
    [r]
  ), D = Te(() => {
    let f = n.filter((S) => !P.has(S.value));
    if (_) {
      const S = _.toLowerCase();
      f = f.filter((M) => M.label.toLowerCase().includes(S));
    }
    return f;
  }, [n, P, _]);
  me(() => {
    B(-1);
  }, [D.length]), me(() => {
    w && c && k.current && k.current.focus();
  }, [w, c]), me(() => {
    if (!w) return;
    const f = (S) => {
      U.current && !U.current.contains(S.target) && T.current && !T.current.contains(S.target) && (h(!1), L(""));
    };
    return document.addEventListener("mousedown", f), () => document.removeEventListener("mousedown", f);
  }, [w]), me(() => {
    if (!w || !U.current) return;
    const f = U.current.getBoundingClientRect(), S = window.innerHeight - f.bottom, O = S < 300 && f.top > S;
    y({
      left: f.left,
      width: f.width,
      ...O ? { bottom: window.innerHeight - f.top } : { top: f.bottom }
    });
  }, [w]);
  const H = se(async () => {
    if (!(o || !d) && (h(!0), L(""), B(-1), m(!1), !c))
      try {
        await l("loadOptions");
      } catch {
        m(!0);
      }
  }, [o, d, c, l]), K = se(() => {
    var f;
    h(!1), L(""), B(-1), (f = U.current) == null || f.focus();
  }, []), ee = se(
    (f) => {
      var M;
      let S;
      if (i) {
        const O = n.find((V) => V.value === f);
        if (O)
          S = [...r, O];
        else
          return;
      } else {
        const O = n.find((V) => V.value === f);
        if (O)
          S = [O];
        else
          return;
      }
      l("valueChanged", { value: S.map((O) => O.value) }), i ? (L(""), B(-1), (M = k.current) == null || M.focus()) : K();
    },
    [i, r, n, l, K]
  ), Y = se(
    (f) => {
      const S = r.filter((M) => M.value !== f);
      l("valueChanged", { value: S.map((M) => M.value) });
    },
    [r, l]
  ), oe = se(
    (f) => {
      f.stopPropagation(), l("valueChanged", { value: [] }), K();
    },
    [l, K]
  ), ce = se((f) => {
    L(f.target.value);
  }, []), le = se(
    (f) => {
      if (!w) {
        (f.key === "ArrowDown" || f.key === "ArrowUp" || f.key === "Enter" || f.key === " ") && (f.preventDefault(), H());
        return;
      }
      switch (f.key) {
        case "ArrowDown":
          f.preventDefault(), B(
            (S) => S < D.length - 1 ? S + 1 : 0
          );
          break;
        case "ArrowUp":
          f.preventDefault(), B(
            (S) => S > 0 ? S - 1 : D.length - 1
          );
          break;
        case "Enter":
          f.preventDefault(), N >= 0 && N < D.length && ee(D[N].value);
          break;
        case "Escape":
          f.preventDefault(), K();
          break;
        case "Tab":
          K();
          break;
        case "Backspace":
          _ === "" && i && r.length > 0 && Y(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      H,
      K,
      D,
      N,
      ee,
      _,
      i,
      r,
      Y
    ]
  ), ue = se(
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
  if (me(() => {
    if (N < 0 || !T.current) return;
    const f = T.current.querySelector(
      `[id="${a}-opt-${N}"]`
    );
    f && f.scrollIntoView({ block: "nearest" });
  }, [N, a]), !d)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((f) => /* @__PURE__ */ e.createElement("span", { key: f.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(xe, { image: f.image }), /* @__PURE__ */ e.createElement("span", null, f.label))));
  const s = !u && r.length > 0 && !o, b = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: T,
      className: "tlDropdownSelect__dropdown",
      style: v
    },
    (c || F) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: k,
        type: "text",
        className: "tlDropdownSelect__search",
        value: _,
        onChange: ce,
        onKeyDown: le,
        placeholder: E["js.dropdownSelect.filterPlaceholder"],
        "aria-label": E["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": N >= 0 ? `${a}-opt-${N}` : void 0,
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
      !c && !F && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      F && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ue }, E["js.dropdownSelect.error"])),
      c && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, C),
      c && D.map((f, S) => /* @__PURE__ */ e.createElement(
        On,
        {
          key: f.value,
          id: `${a}-opt-${S}`,
          option: f,
          highlighted: S === N,
          searchTerm: _,
          onSelect: ee,
          onMouseEnter: () => B(S)
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
      onClick: w ? void 0 : H,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((f) => /* @__PURE__ */ e.createElement(
      An,
      {
        key: f.value,
        option: f,
        removable: !o && (i || !u),
        onRemove: Y,
        removeLabel: x(f.label)
      }
    ))),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: oe,
        "aria-label": E["js.dropdownSelect.clear"],
        tabIndex: -1
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), b && Mn.createPortal(b, document.body));
};
A("TLButton", lt);
A("TLToggleButton", st);
A("TLTextInput", Ge);
A("TLNumberInput", Xe);
A("TLDatePicker", Qe);
A("TLSelect", et);
A("TLCheckbox", nt);
A("TLTable", rt);
A("TLCounter", ct);
A("TLTabBar", ut);
A("TLFieldList", dt);
A("TLAudioRecorder", pt);
A("TLAudioPlayer", ht);
A("TLFileUpload", _t);
A("TLDownload", vt);
A("TLPhotoCapture", yt);
A("TLPhotoViewer", wt);
A("TLSplitPanel", kt);
A("TLPanel", Dt);
A("TLMaximizeRoot", Pt);
A("TLDeckPane", jt);
A("TLSidebar", zt);
A("TLStack", Ut);
A("TLGrid", Vt);
A("TLCard", Ht);
A("TLAppBar", Wt);
A("TLBreadcrumb", Yt);
A("TLBottomBar", qt);
A("TLDialog", Qt);
A("TLDrawer", nn);
A("TLSnackbar", ln);
A("TLMenu", sn);
A("TLAppShell", cn);
A("TLTextCell", un);
A("TLTableView", mn);
A("TLFormLayout", vn);
A("TLFormGroup", Cn);
A("TLFormField", Nn);
A("TLResourceCell", Tn);
A("TLTreeView", Rn);
A("TLDropdownSelect", Bn);
