import { React as e, useTLFieldValue as F, getComponent as Q, useTLState as x, useTLCommand as M, TLChild as T, useTLUpload as H, useI18N as B, useTLDataUrl as K, register as w } from "tl-react-bridge";
const { useCallback: ee } = e, te = ({ state: t }) => {
  const [o, a] = F(), r = ee(
    (n) => {
      a(n.target.value);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: ae } = e, ne = ({ state: t, config: o }) => {
  const [a, r] = F(), n = ae(
    (c) => {
      const i = c.target.value, d = i === "" ? null : Number(i);
      r(d);
    },
    [r]
  ), l = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: n,
      step: l,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: le } = e, oe = ({ state: t }) => {
  const [o, a] = F(), r = le(
    (n) => {
      a(n.target.value || null);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: re } = e, se = ({ state: t, config: o }) => {
  var c;
  const [a, r] = F(), n = re(
    (i) => {
      r(i.target.value || null);
    },
    [r]
  ), l = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = l.find((d) => d.value === a)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: ce } = e, ie = ({ state: t }) => {
  const [o, a] = F(), r = ce(
    (n) => {
      a(n.target.checked);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: o === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: o === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, de = ({ controlId: t, state: o }) => {
  const a = o.columns ?? [], r = o.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((n, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((c) => {
    const i = c.cellModule ? Q(c.cellModule) : void 0, d = n[c.name];
    if (i) {
      const u = { value: d, editable: o.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        i,
        {
          controlId: t + "-" + l + "-" + c.name,
          state: u
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: ue } = e, me = ({ command: t, label: o, disabled: a }) => {
  const r = x(), n = M(), l = t ?? "click", c = o ?? r.label, i = a ?? r.disabled === !0, d = ue(() => {
    n(l);
  }, [n, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: pe } = e, be = ({ command: t, label: o, active: a, disabled: r }) => {
  const n = x(), l = M(), c = t ?? "click", i = o ?? n.label, d = a ?? n.active === !0, u = r ?? n.disabled === !0, h = pe(() => {
    l(c);
  }, [l, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: h,
      disabled: u,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    i
  );
}, fe = () => {
  const t = x(), o = M(), a = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: he } = e, ve = () => {
  const t = x(), o = M(), a = t.tabs ?? [], r = t.activeTabId, n = he((l) => {
    l !== r && o("selectTab", { tabId: l });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === r,
      className: "tlReactTabBar__tab" + (l.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => n(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, Ee = () => {
  const t = x(), o = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((r, n) => /* @__PURE__ */ e.createElement("div", { key: n, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(T, { control: r })))));
}, _e = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ce = () => {
  const t = x(), o = H(), [a, r] = e.useState("idle"), [n, l] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), u = t.status ?? "idle", h = t.error, p = u === "received" ? "idle" : a !== "idle" ? a : u, k = e.useCallback(async () => {
    if (a === "recording") {
      const b = c.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (a !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = b, i.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(b, L ? { mimeType: L } : void 0);
        c.current = R, R.ondataavailable = (s) => {
          s.data.size > 0 && i.current.push(s.data);
        }, R.onstop = async () => {
          b.getTracks().forEach((y) => y.stop()), d.current = null;
          const s = new Blob(i.current, { type: R.mimeType || "audio/webm" });
          if (i.current = [], s.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const f = new FormData();
          f.append("audio", s, "recording.webm"), await o(f), r("idle");
        }, R.start(), r("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), l("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [a, o]), _ = B(_e), C = p === "recording" ? _["js.audioRecorder.stop"] : p === "uploading" ? _["js.uploading"] : _["js.audioRecorder.record"], m = p === "uploading", E = ["tlAudioRecorder__button"];
  return p === "recording" && E.push("tlAudioRecorder__button--recording"), p === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: k,
      disabled: m,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, _[n]), h && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h));
}, ge = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ye = () => {
  const t = x(), o = K(), a = !!t.hasAudio, r = t.dataRevision ?? 0, [n, l] = e.useState(a ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(r);
  e.useEffect(() => {
    a ? n === "disabled" && l("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), l("disabled"));
  }, [a]), e.useEffect(() => {
    r !== d.current && (d.current = r, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (n === "playing" || n === "paused" || n === "loading") && l("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const u = e.useCallback(async () => {
    if (n === "disabled" || n === "loading")
      return;
    if (n === "playing") {
      c.current && c.current.pause(), l("paused");
      return;
    }
    if (n === "paused" && c.current) {
      c.current.play(), l("playing");
      return;
    }
    if (!i.current) {
      l("loading");
      try {
        const m = await fetch(o);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), l("idle");
          return;
        }
        const E = await m.blob();
        i.current = URL.createObjectURL(E);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), l("idle");
        return;
      }
    }
    const C = new Audio(i.current);
    c.current = C, C.onended = () => {
      l("idle");
    }, C.play(), l("playing");
  }, [n, o]), h = B(ge), p = n === "loading" ? h["js.loading"] : n === "playing" ? h["js.audioPlayer.pause"] : n === "disabled" ? h["js.audioPlayer.noAudio"] : h["js.audioPlayer.play"], k = n === "disabled" || n === "loading", _ = ["tlAudioPlayer__button"];
  return n === "playing" && _.push("tlAudioPlayer__button--playing"), n === "loading" && _.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: u,
      disabled: k,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${n === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ke = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, we = () => {
  const t = x(), o = H(), [a, r] = e.useState("idle"), [n, l] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, u = t.accept ?? "", h = i === "received" ? "idle" : a !== "idle" ? a : i, p = e.useCallback(async (s) => {
    r("uploading");
    const f = new FormData();
    f.append("file", s, s.name), await o(f), r("idle");
  }, [o]), k = e.useCallback((s) => {
    var y;
    const f = (y = s.target.files) == null ? void 0 : y[0];
    f && p(f);
  }, [p]), _ = e.useCallback(() => {
    var s;
    a !== "uploading" && ((s = c.current) == null || s.click());
  }, [a]), C = e.useCallback((s) => {
    s.preventDefault(), s.stopPropagation(), l(!0);
  }, []), m = e.useCallback((s) => {
    s.preventDefault(), s.stopPropagation(), l(!1);
  }, []), E = e.useCallback((s) => {
    var y;
    if (s.preventDefault(), s.stopPropagation(), l(!1), a === "uploading") return;
    const f = (y = s.dataTransfer.files) == null ? void 0 : y[0];
    f && p(f);
  }, [a, p]), b = h === "uploading", L = B(ke), R = h === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${n ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: m,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: u || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (h === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: _,
        disabled: b,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
}, Se = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ne = () => {
  const t = x(), o = K(), a = M(), r = !!t.hasData, n = t.dataRevision ?? 0, l = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), u = e.useCallback(async () => {
    if (!(!r || i)) {
      d(!0);
      try {
        const _ = o + (o.includes("?") ? "&" : "?") + "rev=" + n, C = await fetch(_);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const m = await C.blob(), E = URL.createObjectURL(m), b = document.createElement("a");
        b.href = E, b.download = l, b.style.display = "none", document.body.appendChild(b), b.click(), document.body.removeChild(b), URL.revokeObjectURL(E);
      } catch (_) {
        console.error("[TLDownload] Fetch error:", _);
      } finally {
        d(!1);
      }
    }
  }, [r, i, o, n, l]), h = e.useCallback(async () => {
    r && await a("clear");
  }, [r, a]), p = B(Se);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const k = i ? p["js.downloading"] : p["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: u,
      disabled: i,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: h,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Le = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Re = () => {
  const t = x(), o = H(), [a, r] = e.useState("idle"), [n, l] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), u = e.useRef(null), h = e.useRef(null), p = e.useRef(null), k = e.useRef(null), _ = t.error, C = e.useMemo(
    () => {
      var v;
      return !!(window.isSecureContext && ((v = navigator.mediaDevices) != null && v.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    u.current && (u.current.getTracks().forEach((v) => v.stop()), u.current = null), d.current && (d.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    m(), r("idle");
  }, [m]), b = e.useCallback(async () => {
    var v;
    if (a !== "uploading") {
      if (l(null), !C) {
        (v = p.current) == null || v.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        u.current = S, r("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), l("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [a, C]), L = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const v = d.current, S = h.current;
    if (!v || !S)
      return;
    S.width = v.videoWidth, S.height = v.videoHeight;
    const P = S.getContext("2d");
    P && (P.drawImage(v, 0, 0), m(), r("uploading"), S.toBlob(async (j) => {
      if (!j) {
        r("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", j, "capture.jpg"), await o(I), r("idle");
    }, "image/jpeg", 0.85));
  }, [a, o, m]), R = e.useCallback(async (v) => {
    var j;
    const S = (j = v.target.files) == null ? void 0 : j[0];
    if (!S) return;
    r("uploading");
    const P = new FormData();
    P.append("photo", S, S.name), await o(P), r("idle"), p.current && (p.current.value = "");
  }, [o]);
  e.useEffect(() => {
    a === "overlayOpen" && d.current && u.current && (d.current.srcObject = u.current);
  }, [a]), e.useEffect(() => {
    var S;
    if (a !== "overlayOpen") return;
    (S = k.current) == null || S.focus();
    const v = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = v;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const v = (S) => {
      S.key === "Escape" && E();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [a, E]), e.useEffect(() => () => {
    u.current && (u.current.getTracks().forEach((v) => v.stop()), u.current = null);
  }, []);
  const s = B(Le), f = a === "uploading" ? s["js.uploading"] : s["js.photoCapture.open"], y = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && y.push("tlPhotoCapture__cameraBtn--uploading");
  const g = ["tlPhotoCapture__overlayVideo"];
  c && g.push("tlPhotoCapture__overlayVideo--mirrored");
  const N = ["tlPhotoCapture__mirrorBtn"];
  return c && N.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: b,
      disabled: a === "uploading",
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: R
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: h, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: E }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: d,
        className: g.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: N.join(" "),
        onClick: () => i((v) => !v),
        title: s["js.photoCapture.mirror"],
        "aria-label": s["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: L,
        title: s["js.photoCapture.capture"],
        "aria-label": s["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: E,
        title: s["js.photoCapture.close"],
        "aria-label": s["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, s[n]), _ && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _));
}, xe = {
  "js.photoViewer.alt": "Captured photo"
}, Pe = () => {
  const t = x(), o = K(), a = !!t.hasPhoto, r = t.dataRevision ?? 0, [n, l] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!a) {
      n && (URL.revokeObjectURL(n), l(null));
      return;
    }
    if (r === c.current && n)
      return;
    c.current = r, n && (URL.revokeObjectURL(n), l(null));
    let d = !1;
    return (async () => {
      try {
        const u = await fetch(o);
        if (!u.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", u.status);
          return;
        }
        const h = await u.blob();
        d || l(URL.createObjectURL(h));
      } catch (u) {
        console.error("[TLPhotoViewer] Fetch error:", u);
      }
    })(), () => {
      d = !0;
    };
  }, [a, r, o]), e.useEffect(() => () => {
    n && URL.revokeObjectURL(n);
  }, []);
  const i = B(xe);
  return !a || !n ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: n,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: q, useRef: $ } = e, Te = () => {
  const t = x(), o = M(), a = t.orientation, r = t.resizable === !0, n = t.children ?? [], l = a === "horizontal", c = n.length > 0 && n.every((m) => m.collapsed), i = !c && n.some((m) => m.collapsed), d = c ? !l : l, u = $(null), h = $(null), p = $(null), k = q((m, E) => {
    const b = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !d ? b.flex = "1 0 0%" : b.flex = "0 0 auto" : E !== void 0 ? b.flex = `0 0 ${E}px` : m.unit === "%" || i ? b.flex = `${m.size} 0 0%` : b.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (b.minWidth = l ? m.minSize : void 0, b.minHeight = l ? void 0 : m.minSize), b;
  }, [l, c, i, d]), _ = q((m, E) => {
    m.preventDefault();
    const b = u.current;
    if (!b) return;
    const L = n[E], R = n[E + 1], s = b.querySelectorAll(":scope > .tlSplitPanel__child"), f = [];
    s.forEach((N) => {
      f.push(l ? N.offsetWidth : N.offsetHeight);
    }), p.current = f, h.current = {
      splitterIndex: E,
      startPos: l ? m.clientX : m.clientY,
      startSizeBefore: f[E],
      startSizeAfter: f[E + 1],
      childBefore: L,
      childAfter: R
    };
    const y = (N) => {
      const v = h.current;
      if (!v || !p.current) return;
      const P = (l ? N.clientX : N.clientY) - v.startPos, j = v.childBefore.minSize || 0, I = v.childAfter.minSize || 0;
      let z = v.startSizeBefore + P, U = v.startSizeAfter - P;
      z < j && (U += z - j, z = j), U < I && (z += U - I, U = I), p.current[v.splitterIndex] = z, p.current[v.splitterIndex + 1] = U;
      const W = b.querySelectorAll(":scope > .tlSplitPanel__child"), Y = W[v.splitterIndex], G = W[v.splitterIndex + 1];
      Y && (Y.style.flex = `0 0 ${z}px`), G && (G.style.flex = `0 0 ${U}px`);
    }, g = () => {
      if (document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", g), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const N = {};
        n.forEach((v, S) => {
          const P = v.control;
          P != null && P.controlId && p.current && (N[P.controlId] = p.current[S]);
        }), o("updateSizes", { sizes: N });
      }
      p.current = null, h.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", g), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [n, l, o]), C = [];
  return n.forEach((m, E) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${E}`,
          className: `tlSplitPanel__child${m.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(m)
        },
        /* @__PURE__ */ e.createElement(T, { control: m.control })
      )
    ), r && E < n.length - 1) {
      const b = n[E + 1];
      !m.collapsed && !b.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${E}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (R) => _(R, E)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: `tlSplitPanel tlSplitPanel--${a}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    C
  );
}, { useCallback: O } = e, je = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, De = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Me = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Be = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ie = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ue = () => {
  const t = x(), o = M(), a = B(je), r = t.title, n = t.expansionState ?? "NORMALIZED", l = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], u = n === "MINIMIZED", h = n === "MAXIMIZED", p = n === "HIDDEN", k = O(() => {
    o("toggleMinimize");
  }, [o]), _ = O(() => {
    o("toggleMaximize");
  }, [o]), C = O(() => {
    o("popOut");
  }, [o]);
  if (p)
    return null;
  const m = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${n.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((E, b) => /* @__PURE__ */ e.createElement("span", { key: b, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(T, { control: E }))), l && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: u ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      u ? /* @__PURE__ */ e.createElement(Me, null) : /* @__PURE__ */ e.createElement(De, null)
    ), c && !u && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: _,
        title: h ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(Ie, null) : /* @__PURE__ */ e.createElement(Be, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ze, null)
    ))),
    !u && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(T, { control: t.child }))
  );
}, Ae = () => {
  const t = x();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(T, { control: t.child })
  );
}, Fe = () => {
  const t = x();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(T, { control: t.activeChild }));
}, { useCallback: D, useState: X, useEffect: $e, useRef: Z } = e, Oe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function V(t, o, a, r) {
  const n = [];
  for (const l of t)
    l.type === "nav" ? n.push({ id: l.id, type: "nav", groupId: r }) : l.type === "command" ? n.push({ id: l.id, type: "command", groupId: r }) : l.type === "group" && (n.push({ id: l.id, type: "group" }), (a.get(l.id) ?? l.expanded) && !o && n.push(...V(l.children, o, a, l.id)));
  return n;
}
const A = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Ve = ({ item: t, active: o, collapsed: a, onSelect: r, tabIndex: n, itemRef: l, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (o ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(t.id),
    title: a ? t.label : void 0,
    tabIndex: n,
    ref: l,
    onFocus: () => c(t.id)
  },
  a && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !a && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), He = ({ item: t, collapsed: o, onExecute: a, tabIndex: r, itemRef: n, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(t.id),
    title: o ? t.label : void 0,
    tabIndex: r,
    ref: n,
    onFocus: () => l(t.id)
  },
  /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Ke = ({ item: t, collapsed: o }) => o && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: o ? t.label : void 0 }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), We = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ye = ({
  item: t,
  expanded: o,
  activeItemId: a,
  collapsed: r,
  onSelect: n,
  onExecute: l,
  onToggleGroup: c,
  tabIndex: i,
  itemRef: d,
  onFocus: u,
  focusedId: h,
  setItemRef: p,
  onItemFocus: k
}) => /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" }, /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__groupHeader",
    onClick: () => c(t.id),
    title: r ? t.label : void 0,
    "aria-expanded": o,
    tabIndex: i,
    ref: d,
    onFocus: () => u(t.id)
  },
  /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !r && /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlSidebar__chevron" + (o ? " tlSidebar__chevron--open" : ""),
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
), o && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((_) => /* @__PURE__ */ e.createElement(
  J,
  {
    key: _.id,
    item: _,
    activeItemId: a,
    collapsed: r,
    onSelect: n,
    onExecute: l,
    onToggleGroup: c,
    focusedId: h,
    setItemRef: p,
    onItemFocus: k,
    groupStates: null
  }
)))), J = ({
  item: t,
  activeItemId: o,
  collapsed: a,
  onSelect: r,
  onExecute: n,
  onToggleGroup: l,
  focusedId: c,
  setItemRef: i,
  onItemFocus: d,
  groupStates: u
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ve,
        {
          item: t,
          active: t.id === o,
          collapsed: a,
          onSelect: r,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: i(t.id),
          onFocus: d
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        He,
        {
          item: t,
          collapsed: a,
          onExecute: n,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: i(t.id),
          onFocus: d
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ke, { item: t, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(We, null);
    case "group": {
      const h = u ? u.get(t.id) ?? t.expanded : t.expanded;
      return /* @__PURE__ */ e.createElement(
        Ye,
        {
          item: t,
          expanded: h,
          activeItemId: o,
          collapsed: a,
          onSelect: r,
          onExecute: n,
          onToggleGroup: l,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: i(t.id),
          onFocus: d,
          focusedId: c,
          setItemRef: i,
          onItemFocus: d
        }
      );
    }
    default:
      return null;
  }
}, Ge = () => {
  const t = x(), o = M(), a = B(Oe), r = t.items ?? [], n = t.activeItemId, l = t.collapsed, [c, i] = X(() => {
    const s = /* @__PURE__ */ new Map(), f = (y) => {
      for (const g of y)
        g.type === "group" && (s.set(g.id, g.expanded), f(g.children));
    };
    return f(r), s;
  }), d = D((s) => {
    i((f) => {
      const y = new Map(f), g = y.get(s) ?? !1;
      return y.set(s, !g), o("toggleGroup", { itemId: s, expanded: !g }), y;
    });
  }, [o]), u = D((s) => {
    s !== n && o("selectItem", { itemId: s });
  }, [o, n]), h = D((s) => {
    o("executeCommand", { itemId: s });
  }, [o]), p = D(() => {
    o("toggleCollapse", {});
  }, [o]), [k, _] = X(() => {
    const s = V(r, l, c);
    return s.length > 0 ? s[0].id : "";
  }), C = Z(/* @__PURE__ */ new Map()), m = D((s) => (f) => {
    f ? C.current.set(s, f) : C.current.delete(s);
  }, []), E = D((s) => {
    _(s);
  }, []), b = Z(0), L = D((s) => {
    _(s), b.current++;
  }, []);
  $e(() => {
    const s = C.current.get(k);
    s && document.activeElement !== s && s.focus();
  }, [k, b.current]);
  const R = D((s) => {
    const f = V(r, l, c);
    if (f.length === 0) return;
    const y = f.findIndex((N) => N.id === k);
    if (y < 0) return;
    const g = f[y];
    switch (s.key) {
      case "ArrowDown": {
        s.preventDefault();
        const N = (y + 1) % f.length;
        L(f[N].id);
        break;
      }
      case "ArrowUp": {
        s.preventDefault();
        const N = (y - 1 + f.length) % f.length;
        L(f[N].id);
        break;
      }
      case "Home": {
        s.preventDefault(), L(f[0].id);
        break;
      }
      case "End": {
        s.preventDefault(), L(f[f.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        s.preventDefault(), g.type === "nav" ? u(g.id) : g.type === "command" ? h(g.id) : g.type === "group" && d(g.id);
        break;
      }
      case "ArrowRight": {
        g.type === "group" && !l && ((c.get(g.id) ?? !1) || (s.preventDefault(), d(g.id)));
        break;
      }
      case "ArrowLeft": {
        g.type === "group" && !l && (c.get(g.id) ?? !1) && (s.preventDefault(), d(g.id));
        break;
      }
    }
  }, [r, l, c, k, L, u, h, d]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, l ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: R }, r.map((s) => /* @__PURE__ */ e.createElement(
    J,
    {
      key: s.id,
      item: s,
      activeItemId: n,
      collapsed: l,
      onSelect: u,
      onExecute: h,
      onToggleGroup: d,
      focusedId: k,
      setItemRef: m,
      onItemFocus: E,
      groupStates: c
    }
  ))), l ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: p,
      title: l ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: l ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
};
w("TLButton", me);
w("TLToggleButton", be);
w("TLTextInput", te);
w("TLNumberInput", ne);
w("TLDatePicker", oe);
w("TLSelect", se);
w("TLCheckbox", ie);
w("TLTable", de);
w("TLCounter", fe);
w("TLTabBar", ve);
w("TLFieldList", Ee);
w("TLAudioRecorder", Ce);
w("TLAudioPlayer", ye);
w("TLFileUpload", we);
w("TLDownload", Ne);
w("TLPhotoCapture", Re);
w("TLPhotoViewer", Pe);
w("TLSplitPanel", Te);
w("TLPanel", Ue);
w("TLMaximizeRoot", Ae);
w("TLDeckPane", Fe);
w("TLSidebar", Ge);
