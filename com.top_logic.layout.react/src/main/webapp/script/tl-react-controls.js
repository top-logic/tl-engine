import { React as e, useTLFieldValue as X, getComponent as ve, useTLState as D, useTLCommand as I, TLChild as R, useTLUpload as se, useI18N as O, useTLDataUrl as ce, register as x } from "tl-react-bridge";
const { useCallback: Ee } = e, ge = ({ controlId: o, state: t }) => {
  const [a, l] = X(), s = Ee(
    (n) => {
      l(n.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactTextInput tlReactTextInput--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: o,
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Ce } = e, ye = ({ controlId: o, state: t, config: a }) => {
  const [l, s] = X(), n = Ce(
    (c) => {
      const i = c.target.value, m = i === "" ? null : Number(i);
      s(m);
    },
    [s]
  ), r = a != null && a.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: o,
      value: l != null ? String(l) : "",
      onChange: n,
      step: r,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ke } = e, we = ({ controlId: o, state: t }) => {
  const [a, l] = X(), s = ke(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: o,
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ne } = e, Le = ({ controlId: o, state: t, config: a }) => {
  var c;
  const [l, s] = X(), n = Ne(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), r = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = r.find((m) => m.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: o, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: o,
      value: l ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    r.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: xe } = e, Te = ({ controlId: o, state: t }) => {
  const [a, l] = X(), s = xe(
    (n) => {
      l(n.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: o,
      checked: a === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: o,
      checked: a === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Se = ({ controlId: o, state: t }) => {
  const a = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: o, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((s, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, a.map((r) => {
    const c = r.cellModule ? ve(r.cellModule) : void 0, i = s[r.name];
    if (c) {
      const m = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: r.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: o + "-" + n + "-" + r.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: r.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: De } = e, Re = ({ controlId: o, command: t, label: a, disabled: l }) => {
  const s = D(), n = I(), r = t ?? "click", c = a ?? s.label, i = l ?? s.disabled === !0, m = De(() => {
    n(r);
  }, [n, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: o,
      onClick: m,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: Pe } = e, Be = ({ controlId: o, command: t, label: a, active: l, disabled: s }) => {
  const n = D(), r = I(), c = t ?? "click", i = a ?? n.label, m = l ?? n.active === !0, f = s ?? n.disabled === !0, b = Pe(() => {
    r(c);
  }, [r, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: o,
      onClick: b,
      disabled: f,
      className: "tlReactButton" + (m ? " tlReactButtonActive" : "")
    },
    i
  );
}, je = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Fe } = e, Me = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.tabs ?? [], s = t.activeTabId, n = Fe((r) => {
    r !== s && a("selectTab", { tabId: r });
  }, [a, s]);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((r) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: r.id,
      role: "tab",
      "aria-selected": r.id === s,
      className: "tlReactTabBar__tab" + (r.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => n(r.id)
    },
    r.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, Ie = ({ controlId: o }) => {
  const t = D(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((s, n) => /* @__PURE__ */ e.createElement("div", { key: n, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: s })))));
}, Ae = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, $e = ({ controlId: o }) => {
  const t = D(), a = se(), [l, s] = e.useState("idle"), [n, r] = e.useState(null), c = e.useRef(null), i = e.useRef([]), m = e.useRef(null), f = t.status ?? "idle", b = t.error, h = f === "received" ? "idle" : l !== "idle" ? l : f, L = e.useCallback(async () => {
    if (l === "recording") {
      const E = c.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (l !== "uploading") {
      if (r(null), !window.isSecureContext || !navigator.mediaDevices) {
        r("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        m.current = E, i.current = [];
        const P = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(E, P ? { mimeType: P } : void 0);
        c.current = j, j.ondataavailable = (d) => {
          d.data.size > 0 && i.current.push(d.data);
        }, j.onstop = async () => {
          E.getTracks().forEach((N) => N.stop()), m.current = null;
          const d = new Blob(i.current, { type: j.mimeType || "audio/webm" });
          if (i.current = [], d.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const C = new FormData();
          C.append("audio", d, "recording.webm"), await a(C), s("idle");
        }, j.start(), s("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), r("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [l, a]), v = O(Ae), u = h === "recording" ? v["js.audioRecorder.stop"] : h === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], p = h === "uploading", _ = ["tlAudioRecorder__button"];
  return h === "recording" && _.push("tlAudioRecorder__button--recording"), h === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: L,
      disabled: p,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[n]), b && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b));
}, ze = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ve = ({ controlId: o }) => {
  const t = D(), a = ce(), l = !!t.hasAudio, s = t.dataRevision ?? 0, [n, r] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), m = e.useRef(s);
  e.useEffect(() => {
    l ? n === "disabled" && r("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), r("disabled"));
  }, [l]), e.useEffect(() => {
    s !== m.current && (m.current = s, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (n === "playing" || n === "paused" || n === "loading") && r("idle"));
  }, [s]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const f = e.useCallback(async () => {
    if (n === "disabled" || n === "loading")
      return;
    if (n === "playing") {
      c.current && c.current.pause(), r("paused");
      return;
    }
    if (n === "paused" && c.current) {
      c.current.play(), r("playing");
      return;
    }
    if (!i.current) {
      r("loading");
      try {
        const p = await fetch(a);
        if (!p.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", p.status), r("idle");
          return;
        }
        const _ = await p.blob();
        i.current = URL.createObjectURL(_);
      } catch (p) {
        console.error("[TLAudioPlayer] Fetch error:", p), r("idle");
        return;
      }
    }
    const u = new Audio(i.current);
    c.current = u, u.onended = () => {
      r("idle");
    }, u.play(), r("playing");
  }, [n, a]), b = O(ze), h = n === "loading" ? b["js.loading"] : n === "playing" ? b["js.audioPlayer.pause"] : n === "disabled" ? b["js.audioPlayer.noAudio"] : b["js.audioPlayer.play"], L = n === "disabled" || n === "loading", v = ["tlAudioPlayer__button"];
  return n === "playing" && v.push("tlAudioPlayer__button--playing"), n === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: f,
      disabled: L,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${n === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Oe = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ue = ({ controlId: o }) => {
  const t = D(), a = se(), [l, s] = e.useState("idle"), [n, r] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", m = t.error, f = t.accept ?? "", b = i === "received" ? "idle" : l !== "idle" ? l : i, h = e.useCallback(async (d) => {
    s("uploading");
    const C = new FormData();
    C.append("file", d, d.name), await a(C), s("idle");
  }, [a]), L = e.useCallback((d) => {
    var N;
    const C = (N = d.target.files) == null ? void 0 : N[0];
    C && h(C);
  }, [h]), v = e.useCallback(() => {
    var d;
    l !== "uploading" && ((d = c.current) == null || d.click());
  }, [l]), u = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), r(!0);
  }, []), p = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), r(!1);
  }, []), _ = e.useCallback((d) => {
    var N;
    if (d.preventDefault(), d.stopPropagation(), r(!1), l === "uploading") return;
    const C = (N = d.dataTransfer.files) == null ? void 0 : N[0];
    C && h(C);
  }, [l, h]), E = b === "uploading", P = O(Oe), j = b === "uploading" ? P["js.uploading"] : P["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlFileUpload${n ? " tlFileUpload--dragover" : ""}`,
      onDragOver: u,
      onDragLeave: p,
      onDrop: _
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: f || void 0,
        onChange: L,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (b === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: E,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    m && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, m)
  );
}, We = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ke = ({ controlId: o }) => {
  const t = D(), a = ce(), l = I(), s = !!t.hasData, n = t.dataRevision ?? 0, r = t.fileName ?? "download", c = !!t.clearable, [i, m] = e.useState(!1), f = e.useCallback(async () => {
    if (!(!s || i)) {
      m(!0);
      try {
        const v = a + (a.includes("?") ? "&" : "?") + "rev=" + n, u = await fetch(v);
        if (!u.ok) {
          console.error("[TLDownload] Failed to fetch data:", u.status);
          return;
        }
        const p = await u.blob(), _ = URL.createObjectURL(p), E = document.createElement("a");
        E.href = _, E.download = r, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(_);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        m(!1);
      }
    }
  }, [s, i, a, n, r]), b = e.useCallback(async () => {
    s && await l("clear");
  }, [s, l]), h = O(We);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const L = i ? h["js.downloading"] : h["js.download.file"].replace("{0}", r);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: f,
      disabled: i,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: r }, r), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: b,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, He = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ge = ({ controlId: o }) => {
  const t = D(), a = se(), [l, s] = e.useState("idle"), [n, r] = e.useState(null), [c, i] = e.useState(!1), m = e.useRef(null), f = e.useRef(null), b = e.useRef(null), h = e.useRef(null), L = e.useRef(null), v = t.error, u = e.useMemo(
    () => {
      var k;
      return !!(window.isSecureContext && ((k = navigator.mediaDevices) != null && k.getUserMedia));
    },
    []
  ), p = e.useCallback(() => {
    f.current && (f.current.getTracks().forEach((k) => k.stop()), f.current = null), m.current && (m.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    p(), s("idle");
  }, [p]), E = e.useCallback(async () => {
    var k;
    if (l !== "uploading") {
      if (r(null), !u) {
        (k = h.current) == null || k.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        f.current = S, s("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), r("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [l, u]), P = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const k = m.current, S = b.current;
    if (!k || !S)
      return;
    S.width = k.videoWidth, S.height = k.videoHeight;
    const T = S.getContext("2d");
    T && (T.drawImage(k, 0, 0), p(), s("uploading"), S.toBlob(async (F) => {
      if (!F) {
        s("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", F, "capture.jpg"), await a(K), s("idle");
    }, "image/jpeg", 0.85));
  }, [l, a, p]), j = e.useCallback(async (k) => {
    var F;
    const S = (F = k.target.files) == null ? void 0 : F[0];
    if (!S) return;
    s("uploading");
    const T = new FormData();
    T.append("photo", S, S.name), await a(T), s("idle"), h.current && (h.current.value = "");
  }, [a]);
  e.useEffect(() => {
    l === "overlayOpen" && m.current && f.current && (m.current.srcObject = f.current);
  }, [l]), e.useEffect(() => {
    var S;
    if (l !== "overlayOpen") return;
    (S = L.current) == null || S.focus();
    const k = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = k;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const k = (S) => {
      S.key === "Escape" && _();
    };
    return document.addEventListener("keydown", k), () => document.removeEventListener("keydown", k);
  }, [l, _]), e.useEffect(() => () => {
    f.current && (f.current.getTracks().forEach((k) => k.stop()), f.current = null);
  }, []);
  const d = O(He), C = l === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], N = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && N.push("tlPhotoCapture__cameraBtn--uploading");
  const $ = ["tlPhotoCapture__overlayVideo"];
  c && $.push("tlPhotoCapture__overlayVideo--mirrored");
  const g = ["tlPhotoCapture__mirrorBtn"];
  return c && g.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: E,
      disabled: l === "uploading",
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !u && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: b, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: m,
        className: $.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: g.join(" "),
        onClick: () => i((k) => !k),
        title: d["js.photoCapture.mirror"],
        "aria-label": d["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: P,
        title: d["js.photoCapture.capture"],
        "aria-label": d["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: d["js.photoCapture.close"],
        "aria-label": d["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, d[n]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, Ye = {
  "js.photoViewer.alt": "Captured photo"
}, Xe = ({ controlId: o }) => {
  const t = D(), a = ce(), l = !!t.hasPhoto, s = t.dataRevision ?? 0, [n, r] = e.useState(null), c = e.useRef(s);
  e.useEffect(() => {
    if (!l) {
      n && (URL.revokeObjectURL(n), r(null));
      return;
    }
    if (s === c.current && n)
      return;
    c.current = s, n && (URL.revokeObjectURL(n), r(null));
    let m = !1;
    return (async () => {
      try {
        const f = await fetch(a);
        if (!f.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", f.status);
          return;
        }
        const b = await f.blob();
        m || r(URL.createObjectURL(b));
      } catch (f) {
        console.error("[TLPhotoViewer] Fetch error:", f);
      }
    })(), () => {
      m = !0;
    };
  }, [l, s, a]), e.useEffect(() => () => {
    n && URL.revokeObjectURL(n);
  }, []);
  const i = O(Ye);
  return !l || !n ? /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: o, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: n,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ie, useRef: ee } = e, qe = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.orientation, s = t.resizable === !0, n = t.children ?? [], r = l === "horizontal", c = n.length > 0 && n.every((p) => p.collapsed), i = !c && n.some((p) => p.collapsed), m = c ? !r : r, f = ee(null), b = ee(null), h = ee(null), L = ie((p, _) => {
    const E = {
      overflow: p.scrolling || "auto"
    };
    return p.collapsed ? c && !m ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : _ !== void 0 ? E.flex = `0 0 ${_}px` : p.unit === "%" || i ? E.flex = `${p.size} 0 0%` : E.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (E.minWidth = r ? p.minSize : void 0, E.minHeight = r ? void 0 : p.minSize), E;
  }, [r, c, i, m]), v = ie((p, _) => {
    p.preventDefault();
    const E = f.current;
    if (!E) return;
    const P = n[_], j = n[_ + 1], d = E.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    d.forEach((g) => {
      C.push(r ? g.offsetWidth : g.offsetHeight);
    }), h.current = C, b.current = {
      splitterIndex: _,
      startPos: r ? p.clientX : p.clientY,
      startSizeBefore: C[_],
      startSizeAfter: C[_ + 1],
      childBefore: P,
      childAfter: j
    };
    const N = (g) => {
      const k = b.current;
      if (!k || !h.current) return;
      const T = (r ? g.clientX : g.clientY) - k.startPos, F = k.childBefore.minSize || 0, K = k.childAfter.minSize || 0;
      let H = k.startSizeBefore + T, U = k.startSizeAfter - T;
      H < F && (U += H - F, H = F), U < K && (H += U - K, U = K), h.current[k.splitterIndex] = H, h.current[k.splitterIndex + 1] = U;
      const q = E.querySelectorAll(":scope > .tlSplitPanel__child"), Y = q[k.splitterIndex], Z = q[k.splitterIndex + 1];
      Y && (Y.style.flex = `0 0 ${H}px`), Z && (Z.style.flex = `0 0 ${U}px`);
    }, $ = () => {
      if (document.removeEventListener("mousemove", N), document.removeEventListener("mouseup", $), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const g = {};
        n.forEach((k, S) => {
          const T = k.control;
          T != null && T.controlId && h.current && (g[T.controlId] = h.current[S]);
        }), a("updateSizes", { sizes: g });
      }
      h.current = null, b.current = null;
    };
    document.addEventListener("mousemove", N), document.addEventListener("mouseup", $), document.body.style.cursor = r ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [n, r, a]), u = [];
  return n.forEach((p, _) => {
    if (u.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${p.collapsed && m ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(p)
        },
        /* @__PURE__ */ e.createElement(R, { control: p.control })
      )
    ), s && _ < n.length - 1) {
      const E = n[_ + 1];
      !p.collapsed && !E.collapsed && u.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (j) => v(j, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: f,
      id: o,
      className: `tlSplitPanel tlSplitPanel--${l}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: m ? "row" : "column",
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
}, Je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Qe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), et = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), at = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), nt = ({ controlId: o }) => {
  const t = D(), a = I(), l = O(Ze), s = t.title, n = t.expansionState ?? "NORMALIZED", r = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, m = t.toolbarButtons ?? [], f = n === "MINIMIZED", b = n === "MAXIMIZED", h = n === "HIDDEN", L = te(() => {
    a("toggleMinimize");
  }, [a]), v = te(() => {
    a("toggleMaximize");
  }, [a]), u = te(() => {
    a("popOut");
  }, [a]);
  if (h)
    return null;
  const p = b ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlPanel tlPanel--${n.toLowerCase()}`,
      style: p
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((_, E) => /* @__PURE__ */ e.createElement("span", { key: E, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(R, { control: _ }))), r && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: f ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      f ? /* @__PURE__ */ e.createElement(Qe, null) : /* @__PURE__ */ e.createElement(Je, null)
    ), c && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      b ? /* @__PURE__ */ e.createElement(tt, null) : /* @__PURE__ */ e.createElement(et, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: u,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(at, null)
    ))),
    !f && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child }))
  );
}, lt = ({ controlId: o }) => {
  const t = D();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, rt = ({ controlId: o }) => {
  const t = D();
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: V, useState: ae, useEffect: J, useRef: re } = e, ot = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function oe(o, t, a, l) {
  const s = [];
  for (const n of o)
    n.type === "nav" ? s.push({ id: n.id, type: "nav", groupId: l }) : n.type === "command" ? s.push({ id: n.id, type: "command", groupId: l }) : n.type === "group" && (s.push({ id: n.id, type: "group" }), (a.get(n.id) ?? n.expanded) && !t && s.push(...oe(n.children, t, a, n.id)));
  return s;
}
const G = ({ icon: o }) => o ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + o, "aria-hidden": "true" }) : null, st = ({ item: o, active: t, collapsed: a, onSelect: l, tabIndex: s, itemRef: n, onFocus: r }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(o.id),
    title: a ? o.label : void 0,
    tabIndex: s,
    ref: n,
    onFocus: () => r(o.id)
  },
  a && o.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(G, { icon: o.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, o.badge)) : /* @__PURE__ */ e.createElement(G, { icon: o.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
  !a && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
), ct = ({ item: o, collapsed: t, onExecute: a, tabIndex: l, itemRef: s, onFocus: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(o.id),
    title: t ? o.label : void 0,
    tabIndex: l,
    ref: s,
    onFocus: () => n(o.id)
  },
  /* @__PURE__ */ e.createElement(G, { icon: o.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)
), it = ({ item: o, collapsed: t }) => t && !o.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? o.label : void 0 }, /* @__PURE__ */ e.createElement(G, { icon: o.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label)), dt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ut = ({ item: o, activeItemId: t, onSelect: a, onExecute: l, onClose: s }) => {
  const n = re(null);
  J(() => {
    const c = (i) => {
      n.current && !n.current.contains(i.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), J(() => {
    const c = (i) => {
      i.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const r = V((c) => {
    c.type === "nav" ? (a(c.id), s()) : c.type === "command" && (l(c.id), s());
  }, [a, l, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: n, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, o.label), o.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => r(c)
        },
        /* @__PURE__ */ e.createElement(G, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, mt = ({
  item: o,
  expanded: t,
  activeItemId: a,
  collapsed: l,
  onSelect: s,
  onExecute: n,
  onToggleGroup: r,
  tabIndex: c,
  itemRef: i,
  onFocus: m,
  focusedId: f,
  setItemRef: b,
  onItemFocus: h,
  flyoutGroupId: L,
  onOpenFlyout: v,
  onCloseFlyout: u
}) => {
  const p = V(() => {
    l ? L === o.id ? u() : v(o.id) : r(o.id);
  }, [l, L, o.id, r, v, u]), _ = l && L === o.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: p,
      title: l ? o.label : void 0,
      "aria-expanded": l ? _ : t,
      tabIndex: c,
      ref: i,
      onFocus: () => m(o.id)
    },
    /* @__PURE__ */ e.createElement(G, { icon: o.icon }),
    !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
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
  ), _ && /* @__PURE__ */ e.createElement(
    ut,
    {
      item: o,
      activeItemId: a,
      onSelect: s,
      onExecute: n,
      onClose: u
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, o.children.map((E) => /* @__PURE__ */ e.createElement(
    be,
    {
      key: E.id,
      item: E,
      activeItemId: a,
      collapsed: l,
      onSelect: s,
      onExecute: n,
      onToggleGroup: r,
      focusedId: f,
      setItemRef: b,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: v,
      onCloseFlyout: u
    }
  ))));
}, be = ({
  item: o,
  activeItemId: t,
  collapsed: a,
  onSelect: l,
  onExecute: s,
  onToggleGroup: n,
  focusedId: r,
  setItemRef: c,
  onItemFocus: i,
  groupStates: m,
  flyoutGroupId: f,
  onOpenFlyout: b,
  onCloseFlyout: h
}) => {
  switch (o.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        st,
        {
          item: o,
          active: o.id === t,
          collapsed: a,
          onSelect: l,
          tabIndex: r === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ct,
        {
          item: o,
          collapsed: a,
          onExecute: s,
          tabIndex: r === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(it, { item: o, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(dt, null);
    case "group": {
      const L = m ? m.get(o.id) ?? o.expanded : o.expanded;
      return /* @__PURE__ */ e.createElement(
        mt,
        {
          item: o,
          expanded: L,
          activeItemId: t,
          collapsed: a,
          onSelect: l,
          onExecute: s,
          onToggleGroup: n,
          tabIndex: r === o.id ? 0 : -1,
          itemRef: c(o.id),
          onFocus: i,
          focusedId: r,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: f,
          onOpenFlyout: b,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, pt = ({ controlId: o }) => {
  const t = D(), a = I(), l = O(ot), s = t.items ?? [], n = t.activeItemId, r = t.collapsed, [c, i] = ae(() => {
    const g = /* @__PURE__ */ new Map(), k = (S) => {
      for (const T of S)
        T.type === "group" && (g.set(T.id, T.expanded), k(T.children));
    };
    return k(s), g;
  }), m = V((g) => {
    i((k) => {
      const S = new Map(k), T = S.get(g) ?? !1;
      return S.set(g, !T), a("toggleGroup", { itemId: g, expanded: !T }), S;
    });
  }, [a]), f = V((g) => {
    g !== n && a("selectItem", { itemId: g });
  }, [a, n]), b = V((g) => {
    a("executeCommand", { itemId: g });
  }, [a]), h = V(() => {
    a("toggleCollapse", {});
  }, [a]), [L, v] = ae(null), u = V((g) => {
    v(g);
  }, []), p = V(() => {
    v(null);
  }, []);
  J(() => {
    r || v(null);
  }, [r]);
  const [_, E] = ae(() => {
    const g = oe(s, r, c);
    return g.length > 0 ? g[0].id : "";
  }), P = re(/* @__PURE__ */ new Map()), j = V((g) => (k) => {
    k ? P.current.set(g, k) : P.current.delete(g);
  }, []), d = V((g) => {
    E(g);
  }, []), C = re(0), N = V((g) => {
    E(g), C.current++;
  }, []);
  J(() => {
    const g = P.current.get(_);
    g && document.activeElement !== g && g.focus();
  }, [_, C.current]);
  const $ = V((g) => {
    if (g.key === "Escape" && L !== null) {
      g.preventDefault(), p();
      return;
    }
    const k = oe(s, r, c);
    if (k.length === 0) return;
    const S = k.findIndex((F) => F.id === _);
    if (S < 0) return;
    const T = k[S];
    switch (g.key) {
      case "ArrowDown": {
        g.preventDefault();
        const F = (S + 1) % k.length;
        N(k[F].id);
        break;
      }
      case "ArrowUp": {
        g.preventDefault();
        const F = (S - 1 + k.length) % k.length;
        N(k[F].id);
        break;
      }
      case "Home": {
        g.preventDefault(), N(k[0].id);
        break;
      }
      case "End": {
        g.preventDefault(), N(k[k.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        g.preventDefault(), T.type === "nav" ? f(T.id) : T.type === "command" ? b(T.id) : T.type === "group" && (r ? L === T.id ? p() : u(T.id) : m(T.id));
        break;
      }
      case "ArrowRight": {
        T.type === "group" && !r && ((c.get(T.id) ?? !1) || (g.preventDefault(), m(T.id)));
        break;
      }
      case "ArrowLeft": {
        T.type === "group" && !r && (c.get(T.id) ?? !1) && (g.preventDefault(), m(T.id));
        break;
      }
    }
  }, [
    s,
    r,
    c,
    _,
    L,
    N,
    f,
    b,
    m,
    u,
    p
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlSidebar" + (r ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: $ }, s.map((g) => /* @__PURE__ */ e.createElement(
    be,
    {
      key: g.id,
      item: g,
      activeItemId: n,
      collapsed: r,
      onSelect: f,
      onExecute: b,
      onToggleGroup: m,
      focusedId: _,
      setItemRef: j,
      onItemFocus: d,
      groupStates: c,
      flyoutGroupId: L,
      onOpenFlyout: u,
      onCloseFlyout: p
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: r ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: r ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, ft = ({ controlId: o }) => {
  const t = D(), a = t.direction ?? "column", l = t.gap ?? "default", s = t.align ?? "stretch", n = t.wrap === !0, r = t.children ?? [], c = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${s}`,
    n ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: c }, r.map((i, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: i })));
}, bt = ({ controlId: o }) => {
  const t = D(), a = t.columns, l = t.minColumnWidth, s = t.gap ?? "default", n = t.children ?? [], r = {};
  return l ? r.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : a && (r.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: o, className: `tlGrid tlGrid--gap-${s}`, style: r }, n.map((c, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: c })));
}, ht = ({ controlId: o }) => {
  const t = D(), a = t.title, l = t.variant ?? "outlined", s = t.padding ?? "default", n = t.headerActions ?? [], r = t.child, c = a != null || n.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, n.map((i, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(R, { control: r })));
}, _t = ({ controlId: o }) => {
  const t = D(), a = t.title ?? "", l = t.leading, s = t.actions ?? [], n = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    n === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: o, className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(R, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: i }))));
}, { useCallback: vt } = e, Et = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.items ?? [], s = vt((n) => {
    a("navigate", { itemId: n });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: o, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((n, r) => {
    const c = r === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: n.id, className: "tlBreadcrumb__entry" }, r > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, n.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => s(n.id)
      },
      n.label
    ));
  })));
}, { useCallback: gt } = e, Ct = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.items ?? [], s = t.activeItemId, n = gt((r) => {
    r !== s && a("selectItem", { itemId: r });
  }, [a, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: o, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((r) => {
    const c = r.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: r.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => n(r.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + r.icon, "aria-hidden": "true" }), r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, r.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, r.label)
    );
  }));
}, { useCallback: de, useEffect: ue, useRef: yt } = e, kt = {
  "js.dialog.close": "Close"
}, wt = ({ controlId: o }) => {
  const t = D(), a = I(), l = O(kt), s = t.open === !0, n = t.title ?? "", r = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], m = t.child, f = yt(null), b = de(() => {
    a("close");
  }, [a]), h = de((v) => {
    c && v.target === v.currentTarget && b();
  }, [c, b]);
  if (ue(() => {
    if (!s) return;
    const v = (u) => {
      u.key === "Escape" && b();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [s, b]), ue(() => {
    s && f.current && f.current.focus();
  }, [s]), !s) return null;
  const L = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlDialog__backdrop", onClick: h }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${r}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": L,
      ref: f,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: L }, n), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: b,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(R, { control: m })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((v, u) => /* @__PURE__ */ e.createElement(R, { key: u, control: v })))
  ));
}, { useCallback: Nt, useEffect: Lt } = e, xt = {
  "js.drawer.close": "Close"
}, Tt = ({ controlId: o }) => {
  const t = D(), a = I(), l = O(xt), s = t.open === !0, n = t.position ?? "right", r = t.size ?? "medium", c = t.title ?? null, i = t.child, m = Nt(() => {
    a("close");
  }, [a]);
  Lt(() => {
    if (!s) return;
    const b = (h) => {
      h.key === "Escape" && m();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [s, m]);
  const f = [
    "tlDrawer",
    `tlDrawer--${n}`,
    `tlDrawer--${r}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: o, className: f, "aria-hidden": !s }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: m,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(R, { control: i })));
}, { useCallback: me, useEffect: St, useState: Dt } = e, Rt = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.message ?? "", s = t.variant ?? "info", n = t.action, r = t.duration ?? 5e3, c = t.visible === !0, [i, m] = Dt(!1), f = me(() => {
    m(!0), setTimeout(() => {
      a("dismiss"), m(!1);
    }, 200);
  }, [a]), b = me(() => {
    n && a(n.commandName), f();
  }, [a, n, f]);
  return St(() => {
    if (!c || r === 0) return;
    const h = setTimeout(f, r);
    return () => clearTimeout(h);
  }, [c, r, f]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    n && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: b }, n.label)
  );
}, { useCallback: ne, useEffect: le, useRef: Pt, useState: pe } = e, Bt = ({ controlId: o }) => {
  const t = D(), a = I(), l = t.open === !0, s = t.anchorId, n = t.items ?? [], r = Pt(null), [c, i] = pe({ top: 0, left: 0 }), [m, f] = pe(0), b = n.filter((u) => u.type === "item" && !u.disabled);
  le(() => {
    var d, C;
    if (!l || !s) return;
    const u = document.getElementById(s);
    if (!u) return;
    const p = u.getBoundingClientRect(), _ = ((d = r.current) == null ? void 0 : d.offsetHeight) ?? 200, E = ((C = r.current) == null ? void 0 : C.offsetWidth) ?? 200;
    let P = p.bottom + 4, j = p.left;
    P + _ > window.innerHeight && (P = p.top - _ - 4), j + E > window.innerWidth && (j = p.right - E), i({ top: P, left: j }), f(0);
  }, [l, s]);
  const h = ne(() => {
    a("close");
  }, [a]), L = ne((u) => {
    a("selectItem", { itemId: u });
  }, [a]);
  le(() => {
    if (!l) return;
    const u = (p) => {
      r.current && !r.current.contains(p.target) && h();
    };
    return document.addEventListener("mousedown", u), () => document.removeEventListener("mousedown", u);
  }, [l, h]);
  const v = ne((u) => {
    if (u.key === "Escape") {
      h();
      return;
    }
    if (u.key === "ArrowDown")
      u.preventDefault(), f((p) => (p + 1) % b.length);
    else if (u.key === "ArrowUp")
      u.preventDefault(), f((p) => (p - 1 + b.length) % b.length);
    else if (u.key === "Enter" || u.key === " ") {
      u.preventDefault();
      const p = b[m];
      p && L(p.id);
    }
  }, [h, L, b, m]);
  return le(() => {
    l && r.current && r.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: o,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: v
    },
    n.map((u, p) => {
      if (u.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: p, className: "tlMenu__separator" });
      const E = b.indexOf(u) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: u.id,
          type: "button",
          className: "tlMenu__item" + (E ? " tlMenu__item--focused" : "") + (u.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: u.disabled,
          tabIndex: E ? 0 : -1,
          onClick: () => L(u.id)
        },
        u.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + u.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, u.label)
      );
    })
  ) : null;
}, jt = ({ controlId: o }) => {
  const t = D(), a = t.header, l = t.content, s = t.footer, n = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: o, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(R, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(R, { control: l })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(R, { control: s })), /* @__PURE__ */ e.createElement(R, { control: n }));
}, Ft = () => {
  const t = D().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, fe = 50, Mt = () => {
  const o = D(), t = I(), a = o.columns ?? [], l = o.totalRowCount ?? 0, s = o.rows ?? [], n = o.rowHeight ?? 36, r = o.selectionMode ?? "single", c = o.selectedCount ?? 0, i = r === "multi", m = 40, f = e.useRef(null), b = e.useRef(null), h = e.useRef(null), [L, v] = e.useState({}), u = e.useRef(null), p = e.useRef(!1), _ = e.useRef(null), [E, P] = e.useState(null);
  e.useEffect(() => {
    u.current || v({});
  }, [a]);
  const j = (y) => L[y.name] ?? y.width, d = l * n, C = e.useCallback((y, w, B) => {
    B.preventDefault(), B.stopPropagation(), u.current = { column: y, startX: B.clientX, startWidth: w };
    const M = (z) => {
      const W = u.current;
      if (!W) return;
      const Q = Math.max(fe, W.startWidth + (z.clientX - W.startX));
      v((_e) => ({ ..._e, [W.column]: Q }));
    }, A = (z) => {
      document.removeEventListener("mousemove", M), document.removeEventListener("mouseup", A);
      const W = u.current;
      if (W) {
        const Q = Math.max(fe, W.startWidth + (z.clientX - W.startX));
        t("columnResize", { column: W.column, width: Q }), u.current = null, p.current = !0, requestAnimationFrame(() => {
          p.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", M), document.addEventListener("mouseup", A);
  }, [t]), N = e.useCallback(() => {
    f.current && b.current && (f.current.scrollLeft = b.current.scrollLeft), h.current !== null && clearTimeout(h.current), h.current = window.setTimeout(() => {
      const y = b.current;
      if (!y) return;
      const w = y.scrollTop, B = Math.ceil(y.clientHeight / n), M = Math.floor(w / n);
      t("scroll", { start: M, count: B });
    }, 80);
  }, [t, n]), $ = e.useCallback((y, w) => {
    if (p.current) return;
    let B;
    !w || w === "desc" ? B = "asc" : B = "desc", t("sort", { column: y, direction: B });
  }, [t]), g = e.useCallback((y, w) => {
    _.current = y, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", y);
  }, []), k = e.useCallback((y, w) => {
    if (!_.current || _.current === y) {
      P(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const B = w.currentTarget.getBoundingClientRect(), M = w.clientX < B.left + B.width / 2 ? "left" : "right";
    P({ column: y, side: M });
  }, []), S = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation();
    const w = _.current;
    if (!w || !E) {
      _.current = null, P(null);
      return;
    }
    let B = a.findIndex((A) => A.name === E.column);
    if (B < 0) {
      _.current = null, P(null);
      return;
    }
    const M = a.findIndex((A) => A.name === w);
    E.side === "right" && B++, M < B && B--, t("columnReorder", { column: w, targetIndex: B }), _.current = null, P(null);
  }, [a, E, t]), T = e.useCallback(() => {
    _.current = null, P(null);
  }, []), F = e.useCallback((y, w) => {
    w.shiftKey && w.preventDefault(), t("select", {
      rowIndex: y,
      ctrlKey: w.ctrlKey || w.metaKey,
      shiftKey: w.shiftKey
    });
  }, [t]), K = e.useCallback((y, w) => {
    w.stopPropagation(), t("select", { rowIndex: y, ctrlKey: !0, shiftKey: !1 });
  }, [t]), H = e.useCallback(() => {
    const y = c === l && l > 0;
    t("selectAll", { selected: !y });
  }, [t, c, l]), U = a.reduce((y, w) => y + j(w), 0) + (i ? m : 0), q = c === l && l > 0, Y = c > 0 && c < l, Z = e.useCallback((y) => {
    y && (y.indeterminate = Y);
  }, [Y]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (y) => {
        if (!_.current) return;
        y.preventDefault();
        const w = b.current, B = f.current;
        if (!w) return;
        const M = w.getBoundingClientRect(), A = 40, z = 8;
        y.clientX < M.left + A ? w.scrollLeft = Math.max(0, w.scrollLeft - z) : y.clientX > M.right - A && (w.scrollLeft += z), B && (B.scrollLeft = w.scrollLeft);
      },
      onDrop: S
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: f }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: U } }, i && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell",
        style: { width: m, minWidth: m },
        onDragOver: (y) => {
          _.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", a.length > 0 && a[0].name !== _.current && P({ column: a[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Z,
          className: "tlTableView__checkbox",
          checked: q,
          onChange: H
        }
      )
    ), a.map((y, w) => {
      const B = j(y), M = w === a.length - 1;
      let A = "tlTableView__headerCell";
      return y.sortable && (A += " tlTableView__headerCell--sortable"), E && E.column === y.name && (A += " tlTableView__headerCell--dragOver-" + E.side), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.name,
          className: A,
          style: M ? { flex: "1 0 auto", minWidth: B, position: "relative" } : { width: B, minWidth: B, position: "relative" },
          draggable: !0,
          onClick: y.sortable ? () => $(y.name, y.sortDirection) : void 0,
          onDragStart: (z) => g(y.name, z),
          onDragOver: (z) => k(y.name, z),
          onDrop: S,
          onDragEnd: T
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, y.label),
        y.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, y.sortDirection === "asc" ? "▲" : "▼"),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (z) => C(y.name, B, z)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (y) => {
          if (_.current && a.length > 0) {
            const w = a[a.length - 1];
            w.name !== _.current && (y.preventDefault(), y.dataTransfer.dropEffect = "move", P({ column: w.name, side: "right" }));
          }
        },
        onDrop: S
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: N
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: d, position: "relative", minWidth: U } }, s.map((y) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: y.id,
          className: "tlTableView__row" + (y.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: y.index * n,
            height: n,
            minWidth: U,
            width: "100%"
          },
          onClick: (w) => F(y.index, w)
        },
        i && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell",
            style: { width: m, minWidth: m },
            onClick: (w) => w.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: y.selected,
              onChange: () => {
              },
              onClick: (w) => K(y.index, w),
              tabIndex: -1
            }
          )
        ),
        a.map((w, B) => {
          const M = j(w), A = B === a.length - 1;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: w.name,
              className: "tlTableView__cell",
              style: A ? { flex: "1 0 auto", minWidth: M } : { width: M, minWidth: M }
            },
            /* @__PURE__ */ e.createElement(R, { control: y.cells[w.name] })
          );
        })
      )))
    )
  );
}, It = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, he = e.createContext(It), { useMemo: At, useRef: $t, useState: zt, useEffect: Vt } = e, Ot = 320, Ut = ({ controlId: o }) => {
  const t = D(), a = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", s = t.readOnly === !0, n = t.children ?? [], r = $t(null), [c, i] = zt(
    l === "top" ? "top" : "side"
  );
  Vt(() => {
    if (l !== "auto") {
      i(l);
      return;
    }
    const L = r.current;
    if (!L) return;
    const v = new ResizeObserver((u) => {
      for (const p of u) {
        const E = p.contentRect.width / a;
        i(E < Ot ? "top" : "side");
      }
    });
    return v.observe(L), () => v.disconnect();
  }, [l, a]);
  const m = At(() => ({
    readOnly: s,
    resolvedLabelPosition: c
  }), [s, c]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, h = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(he.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: o, className: h, style: b, ref: r }, n.map((L, v) => /* @__PURE__ */ e.createElement(R, { key: v, control: L }))));
}, { useCallback: Wt } = e, Kt = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ht = ({ controlId: o }) => {
  const t = D(), a = I(), l = O(Kt), s = t.header, n = t.headerActions ?? [], r = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", m = t.fullLine === !0, f = t.children ?? [], b = s != null || n.length > 0 || r, h = Wt(() => {
    a("toggleCollapse");
  }, [a]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    m ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: L }, b && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, r && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
      "aria-expanded": !c,
      title: c ? l["js.formGroup.expand"] : l["js.formGroup.collapse"]
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, n.map((v, u) => /* @__PURE__ */ e.createElement(R, { key: u, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, f.map((v, u) => /* @__PURE__ */ e.createElement(R, { key: u, control: v }))));
}, { useContext: Gt, useState: Yt, useCallback: Xt } = e, qt = ({ controlId: o }) => {
  const t = D(), a = Gt(he), l = t.label ?? "", s = t.required === !0, n = t.error, r = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, f = t.visible !== !1, b = t.field, h = a.readOnly, [L, v] = Yt(!1), u = Xt(() => v((E) => !E), []);
  if (!f) return null;
  const p = n != null, _ = [
    "tlFormField",
    `tlFormField--${i}`,
    h ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    p ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: o, className: _ }, c && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__dirtyBar" }), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), s && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), r && !h && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(R, { control: b })), !h && p && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, n)), !h && r && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, r));
}, Zt = 20, Jt = () => {
  const o = D(), t = I(), a = o.nodes ?? [], l = o.selectionMode ?? "single", s = o.dragEnabled ?? !1, n = o.dropEnabled ?? !1, r = o.dropIndicatorNodeId ?? null, c = o.dropIndicatorPosition ?? null, [i, m] = e.useState(-1), f = e.useRef(null), b = e.useCallback((d, C) => {
    t(C ? "collapse" : "expand", { nodeId: d });
  }, [t]), h = e.useCallback((d, C) => {
    t("select", {
      nodeId: d,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), L = e.useCallback((d, C) => {
    C.preventDefault(), t("contextMenu", { nodeId: d, x: C.clientX, y: C.clientY });
  }, [t]), v = e.useRef(null), u = e.useCallback((d, C) => {
    const N = C.getBoundingClientRect(), $ = d.clientY - N.top, g = N.height / 3;
    return $ < g ? "above" : $ > g * 2 ? "below" : "within";
  }, []), p = e.useCallback((d, C) => {
    C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", d);
  }, []), _ = e.useCallback((d, C) => {
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const N = u(C, C.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: d, position: N }), v.current = null;
    }, 50);
  }, [t, u]), E = e.useCallback((d, C) => {
    C.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const N = u(C, C.currentTarget);
    t("drop", { nodeId: d, position: N });
  }, [t, u]), P = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((d) => {
    if (a.length === 0) return;
    let C = i;
    switch (d.key) {
      case "ArrowDown":
        d.preventDefault(), C = Math.min(i + 1, a.length - 1);
        break;
      case "ArrowUp":
        d.preventDefault(), C = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (d.preventDefault(), i >= 0 && i < a.length) {
          const N = a[i];
          if (N.expandable && !N.expanded) {
            t("expand", { nodeId: N.id });
            return;
          } else N.expanded && (C = i + 1);
        }
        break;
      case "ArrowLeft":
        if (d.preventDefault(), i >= 0 && i < a.length) {
          const N = a[i];
          if (N.expanded) {
            t("collapse", { nodeId: N.id });
            return;
          } else {
            const $ = N.depth;
            for (let g = i - 1; g >= 0; g--)
              if (a[g].depth < $) {
                C = g;
                break;
              }
          }
        }
        break;
      case "Enter":
        d.preventDefault(), i >= 0 && i < a.length && t("select", {
          nodeId: a[i].id,
          ctrlKey: d.ctrlKey || d.metaKey,
          shiftKey: d.shiftKey
        });
        return;
      case " ":
        d.preventDefault(), l === "multi" && i >= 0 && i < a.length && t("select", {
          nodeId: a[i].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        d.preventDefault(), C = 0;
        break;
      case "End":
        d.preventDefault(), C = a.length - 1;
        break;
      default:
        return;
    }
    C !== i && m(C);
  }, [i, a, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: f,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    a.map((d, C) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: d.id,
        role: "treeitem",
        "aria-expanded": d.expandable ? d.expanded : void 0,
        "aria-selected": d.selected,
        "aria-level": d.depth + 1,
        className: [
          "tlTreeView__node",
          d.selected ? "tlTreeView__node--selected" : "",
          C === i ? "tlTreeView__node--focused" : "",
          r === d.id && c === "above" ? "tlTreeView__node--drop-above" : "",
          r === d.id && c === "within" ? "tlTreeView__node--drop-within" : "",
          r === d.id && c === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: d.depth * Zt },
        draggable: s,
        onClick: (N) => h(d.id, N),
        onContextMenu: (N) => L(d.id, N),
        onDragStart: (N) => p(d.id, N),
        onDragOver: n ? (N) => _(d.id, N) : void 0,
        onDrop: n ? (N) => E(d.id, N) : void 0,
        onDragEnd: P
      },
      d.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (N) => {
            N.stopPropagation(), b(d.id, d.expanded);
          },
          tabIndex: -1,
          "aria-label": d.expanded ? "Collapse" : "Expand"
        },
        d.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: d.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(R, { control: d.content }))
    ))
  );
};
x("TLButton", Re);
x("TLToggleButton", Be);
x("TLTextInput", ge);
x("TLNumberInput", ye);
x("TLDatePicker", we);
x("TLSelect", Le);
x("TLCheckbox", Te);
x("TLTable", Se);
x("TLCounter", je);
x("TLTabBar", Me);
x("TLFieldList", Ie);
x("TLAudioRecorder", $e);
x("TLAudioPlayer", Ve);
x("TLFileUpload", Ue);
x("TLDownload", Ke);
x("TLPhotoCapture", Ge);
x("TLPhotoViewer", Xe);
x("TLSplitPanel", qe);
x("TLPanel", nt);
x("TLMaximizeRoot", lt);
x("TLDeckPane", rt);
x("TLSidebar", pt);
x("TLStack", ft);
x("TLGrid", bt);
x("TLCard", ht);
x("TLAppBar", _t);
x("TLBreadcrumb", Et);
x("TLBottomBar", Ct);
x("TLDialog", wt);
x("TLDrawer", Tt);
x("TLSnackbar", Rt);
x("TLMenu", Bt);
x("TLAppShell", jt);
x("TLTextCell", Ft);
x("TLTableView", Mt);
x("TLFormLayout", Ut);
x("TLFormGroup", Ht);
x("TLFormField", qt);
x("TLTreeView", Jt);
