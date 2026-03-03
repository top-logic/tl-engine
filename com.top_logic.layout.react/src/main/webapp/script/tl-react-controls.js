import { React as e, useTLFieldValue as F, getComponent as se, useTLState as S, useTLCommand as P, TLChild as L, useTLUpload as X, useI18N as M, useTLDataUrl as Z, register as w } from "tl-react-bridge";
const { useCallback: ce } = e, ie = ({ controlId: l, state: t }) => {
  const [s, o] = F(), r = ce(
    (a) => {
      o(a.target.value);
    },
    [o]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: l,
      value: s ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: de } = e, ue = ({ controlId: l, state: t, config: s }) => {
  const [o, r] = F(), a = de(
    (c) => {
      const i = c.target.value, u = i === "" ? null : Number(i);
      r(u);
    },
    [r]
  ), n = s != null && s.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, o != null ? String(o) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: o != null ? String(o) : "",
      onChange: a,
      step: n,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: me } = e, pe = ({ controlId: l, state: t }) => {
  const [s, o] = F(), r = me(
    (a) => {
      o(a.target.value || null);
    },
    [o]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: l,
      value: s ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: be } = e, fe = ({ controlId: l, state: t, config: s }) => {
  var c;
  const [o, r] = F(), a = be(
    (i) => {
      r(i.target.value || null);
    },
    [r]
  ), n = t.options ?? (s == null ? void 0 : s.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = n.find((u) => u.value === o)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: o ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    n.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: he } = e, _e = ({ controlId: l, state: t }) => {
  const [s, o] = F(), r = he(
    (a) => {
      o(a.target.checked);
    },
    [o]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: s === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: s === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Ee = ({ controlId: l, state: t }) => {
  const s = t.columns ?? [], o = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, s.map((r) => /* @__PURE__ */ e.createElement("th", { key: r.name }, r.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((r, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, s.map((n) => {
    const c = n.cellModule ? se(n.cellModule) : void 0, i = r[n.name];
    if (c) {
      const u = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: n.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: l + "-" + a + "-" + n.name,
          state: u
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: n.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: ve } = e, ge = ({ controlId: l, command: t, label: s, disabled: o }) => {
  const r = S(), a = P(), n = t ?? "click", c = s ?? r.label, i = o ?? r.disabled === !0, u = ve(() => {
    a(n);
  }, [a, n]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: u,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: Ce } = e, ye = ({ controlId: l, command: t, label: s, active: o, disabled: r }) => {
  const a = S(), n = P(), c = t ?? "click", i = s ?? a.label, u = o ?? a.active === !0, b = r ?? a.disabled === !0, f = Ce(() => {
    n(c);
  }, [n, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: f,
      disabled: b,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    i
  );
}, ke = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, o), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: we } = e, Ne = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.tabs ?? [], r = t.activeTabId, a = we((n) => {
    n !== r && s("selectTab", { tabId: n });
  }, [s, r]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, o.map((n) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: n.id,
      role: "tab",
      "aria-selected": n.id === r,
      className: "tlReactTabBar__tab" + (n.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(n.id)
    },
    n.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(L, { control: t.activeContent })));
}, Se = ({ controlId: l }) => {
  const t = S(), s = t.title, o = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, o.map((r, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(L, { control: r })))));
}, Le = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Te = ({ controlId: l }) => {
  const t = S(), s = X(), [o, r] = e.useState("idle"), [a, n] = e.useState(null), c = e.useRef(null), i = e.useRef([]), u = e.useRef(null), b = t.status ?? "idle", f = t.error, h = b === "received" ? "idle" : o !== "idle" ? o : b, p = e.useCallback(async () => {
    if (o === "recording") {
      const g = c.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (o !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        u.current = g, i.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", R = new MediaRecorder(g, D ? { mimeType: D } : void 0);
        c.current = R, R.ondataavailable = (y) => {
          y.data.size > 0 && i.current.push(y.data);
        }, R.onstop = async () => {
          g.getTracks().forEach((x) => x.stop()), u.current = null;
          const y = new Blob(i.current, { type: R.mimeType || "audio/webm" });
          if (i.current = [], y.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const T = new FormData();
          T.append("audio", y, "recording.webm"), await s(T), r("idle");
        }, R.start(), r("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), n("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [o, s]), v = M(Le), d = h === "recording" ? v["js.audioRecorder.stop"] : h === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], m = h === "uploading", C = ["tlAudioRecorder__button"];
  return h === "recording" && C.push("tlAudioRecorder__button--recording"), h === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: p,
      disabled: m,
      title: d,
      "aria-label": d
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, Re = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, xe = ({ controlId: l }) => {
  const t = S(), s = Z(), o = !!t.hasAudio, r = t.dataRevision ?? 0, [a, n] = e.useState(o ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), u = e.useRef(r);
  e.useEffect(() => {
    o ? a === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), n("disabled"));
  }, [o]), e.useEffect(() => {
    r !== u.current && (u.current = r, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && n("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const b = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      c.current && c.current.pause(), n("paused");
      return;
    }
    if (a === "paused" && c.current) {
      c.current.play(), n("playing");
      return;
    }
    if (!i.current) {
      n("loading");
      try {
        const m = await fetch(s);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), n("idle");
          return;
        }
        const C = await m.blob();
        i.current = URL.createObjectURL(C);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), n("idle");
        return;
      }
    }
    const d = new Audio(i.current);
    c.current = d, d.onended = () => {
      n("idle");
    }, d.play(), n("playing");
  }, [a, s]), f = M(Re), h = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], p = a === "disabled" || a === "loading", v = ["tlAudioPlayer__button"];
  return a === "playing" && v.push("tlAudioPlayer__button--playing"), a === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: b,
      disabled: p,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, De = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Be = ({ controlId: l }) => {
  const t = S(), s = X(), [o, r] = e.useState("idle"), [a, n] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", u = t.error, b = t.accept ?? "", f = i === "received" ? "idle" : o !== "idle" ? o : i, h = e.useCallback(async (y) => {
    r("uploading");
    const T = new FormData();
    T.append("file", y, y.name), await s(T), r("idle");
  }, [s]), p = e.useCallback((y) => {
    var x;
    const T = (x = y.target.files) == null ? void 0 : x[0];
    T && h(T);
  }, [h]), v = e.useCallback(() => {
    var y;
    o !== "uploading" && ((y = c.current) == null || y.click());
  }, [o]), d = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), n(!0);
  }, []), m = e.useCallback((y) => {
    y.preventDefault(), y.stopPropagation(), n(!1);
  }, []), C = e.useCallback((y) => {
    var x;
    if (y.preventDefault(), y.stopPropagation(), n(!1), o === "uploading") return;
    const T = (x = y.dataTransfer.files) == null ? void 0 : x[0];
    T && h(T);
  }, [o, h]), g = f === "uploading", D = M(De), R = f === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: d,
      onDragLeave: m,
      onDrop: C
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: b || void 0,
        onChange: p,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: g,
        title: R,
        "aria-label": R
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
}, Pe = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, je = ({ controlId: l }) => {
  const t = S(), s = Z(), o = P(), r = !!t.hasData, a = t.dataRevision ?? 0, n = t.fileName ?? "download", c = !!t.clearable, [i, u] = e.useState(!1), b = e.useCallback(async () => {
    if (!(!r || i)) {
      u(!0);
      try {
        const v = s + (s.includes("?") ? "&" : "?") + "rev=" + a, d = await fetch(v);
        if (!d.ok) {
          console.error("[TLDownload] Failed to fetch data:", d.status);
          return;
        }
        const m = await d.blob(), C = URL.createObjectURL(m), g = document.createElement("a");
        g.href = C, g.download = n, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(C);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        u(!1);
      }
    }
  }, [r, i, s, a, n]), f = e.useCallback(async () => {
    r && await o("clear");
  }, [r, o]), h = M(Pe);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const p = i ? h["js.downloading"] : h["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: i,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Me = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ae = ({ controlId: l }) => {
  const t = S(), s = X(), [o, r] = e.useState("idle"), [a, n] = e.useState(null), [c, i] = e.useState(!1), u = e.useRef(null), b = e.useRef(null), f = e.useRef(null), h = e.useRef(null), p = e.useRef(null), v = t.error, d = e.useMemo(
    () => {
      var _;
      return !!(window.isSecureContext && ((_ = navigator.mediaDevices) != null && _.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    b.current && (b.current.getTracks().forEach((_) => _.stop()), b.current = null), u.current && (u.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    m(), r("idle");
  }, [m]), g = e.useCallback(async () => {
    var _;
    if (o !== "uploading") {
      if (n(null), !d) {
        (_ = h.current) == null || _.click();
        return;
      }
      try {
        const N = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        b.current = N, r("overlayOpen");
      } catch (N) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", N), n("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [o, d]), D = e.useCallback(async () => {
    if (o !== "overlayOpen")
      return;
    const _ = u.current, N = f.current;
    if (!_ || !N)
      return;
    N.width = _.videoWidth, N.height = _.videoHeight;
    const k = N.getContext("2d");
    k && (k.drawImage(_, 0, 0), m(), r("uploading"), N.toBlob(async (B) => {
      if (!B) {
        r("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", B, "capture.jpg"), await s(I), r("idle");
    }, "image/jpeg", 0.85));
  }, [o, s, m]), R = e.useCallback(async (_) => {
    var B;
    const N = (B = _.target.files) == null ? void 0 : B[0];
    if (!N) return;
    r("uploading");
    const k = new FormData();
    k.append("photo", N, N.name), await s(k), r("idle"), h.current && (h.current.value = "");
  }, [s]);
  e.useEffect(() => {
    o === "overlayOpen" && u.current && b.current && (u.current.srcObject = b.current);
  }, [o]), e.useEffect(() => {
    var N;
    if (o !== "overlayOpen") return;
    (N = p.current) == null || N.focus();
    const _ = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = _;
    };
  }, [o]), e.useEffect(() => {
    if (o !== "overlayOpen") return;
    const _ = (N) => {
      N.key === "Escape" && C();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [o, C]), e.useEffect(() => () => {
    b.current && (b.current.getTracks().forEach((_) => _.stop()), b.current = null);
  }, []);
  const y = M(Me), T = o === "uploading" ? y["js.uploading"] : y["js.photoCapture.open"], x = ["tlPhotoCapture__cameraBtn"];
  o === "uploading" && x.push("tlPhotoCapture__cameraBtn--uploading");
  const A = ["tlPhotoCapture__overlayVideo"];
  c && A.push("tlPhotoCapture__overlayVideo--mirrored");
  const E = ["tlPhotoCapture__mirrorBtn"];
  return c && E.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: x.join(" "),
      onClick: g,
      disabled: o === "uploading",
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !d && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: R
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), o === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: u,
        className: A.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: E.join(" "),
        onClick: () => i((_) => !_),
        title: y["js.photoCapture.mirror"],
        "aria-label": y["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: y["js.photoCapture.capture"],
        "aria-label": y["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: y["js.photoCapture.close"],
        "aria-label": y["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, y[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, Ie = {
  "js.photoViewer.alt": "Captured photo"
}, $e = ({ controlId: l }) => {
  const t = S(), s = Z(), o = !!t.hasPhoto, r = t.dataRevision ?? 0, [a, n] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!o) {
      a && (URL.revokeObjectURL(a), n(null));
      return;
    }
    if (r === c.current && a)
      return;
    c.current = r, a && (URL.revokeObjectURL(a), n(null));
    let u = !1;
    return (async () => {
      try {
        const b = await fetch(s);
        if (!b.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", b.status);
          return;
        }
        const f = await b.blob();
        u || n(URL.createObjectURL(f));
      } catch (b) {
        console.error("[TLPhotoViewer] Fetch error:", b);
      }
    })(), () => {
      u = !0;
    };
  }, [o, r, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = M(Ie);
  return !o || !a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: te, useRef: V } = e, ze = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.orientation, r = t.resizable === !0, a = t.children ?? [], n = o === "horizontal", c = a.length > 0 && a.every((m) => m.collapsed), i = !c && a.some((m) => m.collapsed), u = c ? !n : n, b = V(null), f = V(null), h = V(null), p = te((m, C) => {
    const g = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !u ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : C !== void 0 ? g.flex = `0 0 ${C}px` : m.unit === "%" || i ? g.flex = `${m.size} 0 0%` : g.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (g.minWidth = n ? m.minSize : void 0, g.minHeight = n ? void 0 : m.minSize), g;
  }, [n, c, i, u]), v = te((m, C) => {
    m.preventDefault();
    const g = b.current;
    if (!g) return;
    const D = a[C], R = a[C + 1], y = g.querySelectorAll(":scope > .tlSplitPanel__child"), T = [];
    y.forEach((E) => {
      T.push(n ? E.offsetWidth : E.offsetHeight);
    }), h.current = T, f.current = {
      splitterIndex: C,
      startPos: n ? m.clientX : m.clientY,
      startSizeBefore: T[C],
      startSizeAfter: T[C + 1],
      childBefore: D,
      childAfter: R
    };
    const x = (E) => {
      const _ = f.current;
      if (!_ || !h.current) return;
      const k = (n ? E.clientX : E.clientY) - _.startPos, B = _.childBefore.minSize || 0, I = _.childAfter.minSize || 0;
      let $ = _.startSizeBefore + k, z = _.startSizeAfter - k;
      $ < B && (z += $ - B, $ = B), z < I && ($ += z - I, z = I), h.current[_.splitterIndex] = $, h.current[_.splitterIndex + 1] = z;
      const J = g.querySelectorAll(":scope > .tlSplitPanel__child"), Q = J[_.splitterIndex], ee = J[_.splitterIndex + 1];
      Q && (Q.style.flex = `0 0 ${$}px`), ee && (ee.style.flex = `0 0 ${z}px`);
    }, A = () => {
      if (document.removeEventListener("mousemove", x), document.removeEventListener("mouseup", A), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const E = {};
        a.forEach((_, N) => {
          const k = _.control;
          k != null && k.controlId && h.current && (E[k.controlId] = h.current[N]);
        }), s("updateSizes", { sizes: E });
      }
      h.current = null, f.current = null;
    };
    document.addEventListener("mousemove", x), document.addEventListener("mouseup", A), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, n, s]), d = [];
  return a.forEach((m, C) => {
    if (d.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${m.collapsed && u ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: p(m)
        },
        /* @__PURE__ */ e.createElement(L, { control: m.control })
      )
    ), r && C < a.length - 1) {
      const g = a[C + 1];
      !m.collapsed && !g.collapsed && d.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
            onMouseDown: (R) => v(R, C)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: b,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${o}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: u ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    d
  );
}, { useCallback: W } = e, Ue = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Fe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Oe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Ve = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), We = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), He = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ke = ({ controlId: l }) => {
  const t = S(), s = P(), o = M(Ue), r = t.title, a = t.expansionState ?? "NORMALIZED", n = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, u = t.toolbarButtons ?? [], b = a === "MINIMIZED", f = a === "MAXIMIZED", h = a === "HIDDEN", p = W(() => {
    s("toggleMinimize");
  }, [s]), v = W(() => {
    s("toggleMaximize");
  }, [s]), d = W(() => {
    s("popOut");
  }, [s]);
  if (h)
    return null;
  const m = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, u.map((C, g) => /* @__PURE__ */ e.createElement("span", { key: g, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(L, { control: C }))), n && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: p,
        title: b ? o["js.panel.restore"] : o["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(Oe, null) : /* @__PURE__ */ e.createElement(Fe, null)
    ), c && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: f ? o["js.panel.restore"] : o["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(We, null) : /* @__PURE__ */ e.createElement(Ve, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: d,
        title: o["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(He, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(L, { control: t.child }))
  );
}, Ye = ({ controlId: l }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(L, { control: t.child })
  );
}, Ge = ({ controlId: l }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(L, { control: t.activeChild }));
}, { useCallback: j, useState: H, useEffect: O, useRef: G } = e, qe = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function q(l, t, s, o) {
  const r = [];
  for (const a of l)
    a.type === "nav" ? r.push({ id: a.id, type: "nav", groupId: o }) : a.type === "command" ? r.push({ id: a.id, type: "command", groupId: o }) : a.type === "group" && (r.push({ id: a.id, type: "group" }), (s.get(a.id) ?? a.expanded) && !t && r.push(...q(a.children, t, s, a.id)));
  return r;
}
const U = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Xe = ({ item: l, active: t, collapsed: s, onSelect: o, tabIndex: r, itemRef: a, onFocus: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => o(l.id),
    title: s ? l.label : void 0,
    tabIndex: r,
    ref: a,
    onFocus: () => n(l.id)
  },
  s && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(U, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(U, { icon: l.icon }),
  !s && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !s && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Ze = ({ item: l, collapsed: t, onExecute: s, tabIndex: o, itemRef: r, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => s(l.id),
    title: t ? l.label : void 0,
    tabIndex: o,
    ref: r,
    onFocus: () => a(l.id)
  },
  /* @__PURE__ */ e.createElement(U, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Je = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(U, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Qe = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), et = ({ item: l, activeItemId: t, onSelect: s, onExecute: o, onClose: r }) => {
  const a = G(null);
  O(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [r]), O(() => {
    const c = (i) => {
      i.key === "Escape" && r();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [r]);
  const n = j((c) => {
    c.type === "nav" ? (s(c.id), r()) : c.type === "command" && (o(c.id), r());
  }, [s, o, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => n(c)
        },
        /* @__PURE__ */ e.createElement(U, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, tt = ({
  item: l,
  expanded: t,
  activeItemId: s,
  collapsed: o,
  onSelect: r,
  onExecute: a,
  onToggleGroup: n,
  tabIndex: c,
  itemRef: i,
  onFocus: u,
  focusedId: b,
  setItemRef: f,
  onItemFocus: h,
  flyoutGroupId: p,
  onOpenFlyout: v,
  onCloseFlyout: d
}) => {
  const m = j(() => {
    o ? p === l.id ? d() : v(l.id) : n(l.id);
  }, [o, p, l.id, n, v, d]), C = o && p === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (C ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: m,
      title: o ? l.label : void 0,
      "aria-expanded": o ? C : t,
      tabIndex: c,
      ref: i,
      onFocus: () => u(l.id)
    },
    /* @__PURE__ */ e.createElement(U, { icon: l.icon }),
    !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !o && /* @__PURE__ */ e.createElement(
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
  ), C && /* @__PURE__ */ e.createElement(
    et,
    {
      item: l,
      activeItemId: s,
      onSelect: r,
      onExecute: a,
      onClose: d
    }
  ), t && !o && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((g) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: g.id,
      item: g,
      activeItemId: s,
      collapsed: o,
      onSelect: r,
      onExecute: a,
      onToggleGroup: n,
      focusedId: b,
      setItemRef: f,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: p,
      onOpenFlyout: v,
      onCloseFlyout: d
    }
  ))));
}, re = ({
  item: l,
  activeItemId: t,
  collapsed: s,
  onSelect: o,
  onExecute: r,
  onToggleGroup: a,
  focusedId: n,
  setItemRef: c,
  onItemFocus: i,
  groupStates: u,
  flyoutGroupId: b,
  onOpenFlyout: f,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Xe,
        {
          item: l,
          active: l.id === t,
          collapsed: s,
          onSelect: o,
          tabIndex: n === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ze,
        {
          item: l,
          collapsed: s,
          onExecute: r,
          tabIndex: n === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Je, { item: l, collapsed: s });
    case "separator":
      return /* @__PURE__ */ e.createElement(Qe, null);
    case "group": {
      const p = u ? u.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        tt,
        {
          item: l,
          expanded: p,
          activeItemId: t,
          collapsed: s,
          onSelect: o,
          onExecute: r,
          onToggleGroup: a,
          tabIndex: n === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i,
          focusedId: n,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: b,
          onOpenFlyout: f,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, at = ({ controlId: l }) => {
  const t = S(), s = P(), o = M(qe), r = t.items ?? [], a = t.activeItemId, n = t.collapsed, [c, i] = H(() => {
    const E = /* @__PURE__ */ new Map(), _ = (N) => {
      for (const k of N)
        k.type === "group" && (E.set(k.id, k.expanded), _(k.children));
    };
    return _(r), E;
  }), u = j((E) => {
    i((_) => {
      const N = new Map(_), k = N.get(E) ?? !1;
      return N.set(E, !k), s("toggleGroup", { itemId: E, expanded: !k }), N;
    });
  }, [s]), b = j((E) => {
    E !== a && s("selectItem", { itemId: E });
  }, [s, a]), f = j((E) => {
    s("executeCommand", { itemId: E });
  }, [s]), h = j(() => {
    s("toggleCollapse", {});
  }, [s]), [p, v] = H(null), d = j((E) => {
    v(E);
  }, []), m = j(() => {
    v(null);
  }, []);
  O(() => {
    n || v(null);
  }, [n]);
  const [C, g] = H(() => {
    const E = q(r, n, c);
    return E.length > 0 ? E[0].id : "";
  }), D = G(/* @__PURE__ */ new Map()), R = j((E) => (_) => {
    _ ? D.current.set(E, _) : D.current.delete(E);
  }, []), y = j((E) => {
    g(E);
  }, []), T = G(0), x = j((E) => {
    g(E), T.current++;
  }, []);
  O(() => {
    const E = D.current.get(C);
    E && document.activeElement !== E && E.focus();
  }, [C, T.current]);
  const A = j((E) => {
    if (E.key === "Escape" && p !== null) {
      E.preventDefault(), m();
      return;
    }
    const _ = q(r, n, c);
    if (_.length === 0) return;
    const N = _.findIndex((B) => B.id === C);
    if (N < 0) return;
    const k = _[N];
    switch (E.key) {
      case "ArrowDown": {
        E.preventDefault();
        const B = (N + 1) % _.length;
        x(_[B].id);
        break;
      }
      case "ArrowUp": {
        E.preventDefault();
        const B = (N - 1 + _.length) % _.length;
        x(_[B].id);
        break;
      }
      case "Home": {
        E.preventDefault(), x(_[0].id);
        break;
      }
      case "End": {
        E.preventDefault(), x(_[_.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        E.preventDefault(), k.type === "nav" ? b(k.id) : k.type === "command" ? f(k.id) : k.type === "group" && (n ? p === k.id ? m() : d(k.id) : u(k.id));
        break;
      }
      case "ArrowRight": {
        k.type === "group" && !n && ((c.get(k.id) ?? !1) || (E.preventDefault(), u(k.id)));
        break;
      }
      case "ArrowLeft": {
        k.type === "group" && !n && (c.get(k.id) ?? !1) && (E.preventDefault(), u(k.id));
        break;
      }
    }
  }, [
    r,
    n,
    c,
    C,
    p,
    x,
    b,
    f,
    u,
    d,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (n ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": o["js.sidebar.ariaLabel"] }, n ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: A }, r.map((E) => /* @__PURE__ */ e.createElement(
    re,
    {
      key: E.id,
      item: E,
      activeItemId: a,
      collapsed: n,
      onSelect: b,
      onExecute: f,
      onToggleGroup: u,
      focusedId: C,
      setItemRef: R,
      onItemFocus: y,
      groupStates: c,
      flyoutGroupId: p,
      onOpenFlyout: d,
      onCloseFlyout: m
    }
  ))), n ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: n ? o["js.sidebar.expand"] : o["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: n ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(L, { control: t.activeContent })));
}, nt = ({ controlId: l }) => {
  const t = S(), s = t.direction ?? "column", o = t.gap ?? "default", r = t.align ?? "stretch", a = t.wrap === !0, n = t.children ?? [], c = [
    "tlStack",
    `tlStack--${s}`,
    `tlStack--gap-${o}`,
    `tlStack--align-${r}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: c }, n.map((i, u) => /* @__PURE__ */ e.createElement(L, { key: u, control: i })));
}, lt = ({ controlId: l }) => {
  const t = S(), s = t.columns, o = t.minColumnWidth, r = t.gap ?? "default", a = t.children ?? [], n = {};
  return o ? n.gridTemplateColumns = `repeat(auto-fit, minmax(${o}, 1fr))` : s && (n.gridTemplateColumns = `repeat(${s}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${r}`, style: n }, a.map((c, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: c })));
}, ot = ({ controlId: l }) => {
  const t = S(), s = t.title, o = t.variant ?? "outlined", r = t.padding ?? "default", a = t.headerActions ?? [], n = t.child, c = s != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${o}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, s && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, u) => /* @__PURE__ */ e.createElement(L, { key: u, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(L, { control: n })));
}, rt = ({ controlId: l }) => {
  const t = S(), s = t.title ?? "", o = t.leading, r = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(L, { control: o })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((i, u) => /* @__PURE__ */ e.createElement(L, { key: u, control: i }))));
}, { useCallback: st } = e, ct = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.items ?? [], r = st((a) => {
    s("navigate", { itemId: a });
  }, [s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, o.map((a, n) => {
    const c = n === o.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, n > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, a.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: it } = e, dt = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.items ?? [], r = t.activeItemId, a = it((n) => {
    n !== r && s("selectItem", { itemId: n });
  }, [s, r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, o.map((n) => {
    const c = n.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: n.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => a(n.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + n.icon, "aria-hidden": "true" }), n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, n.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, n.label)
    );
  }));
}, { useCallback: ae, useEffect: ne, useRef: ut } = e, mt = {
  "js.dialog.close": "Close"
}, pt = ({ controlId: l }) => {
  const t = S(), s = P(), o = M(mt), r = t.open === !0, a = t.title ?? "", n = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], u = t.child, b = ut(null), f = ae(() => {
    s("close");
  }, [s]), h = ae((v) => {
    c && v.target === v.currentTarget && f();
  }, [c, f]);
  if (ne(() => {
    if (!r) return;
    const v = (d) => {
      d.key === "Escape" && f();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [r, f]), ne(() => {
    r && b.current && b.current.focus();
  }, [r]), !r) return null;
  const p = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: h }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${n}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": p,
      ref: b,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: p }, a), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: f,
        title: o["js.dialog.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(L, { control: u })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((v, d) => /* @__PURE__ */ e.createElement(L, { key: d, control: v })))
  ));
}, { useCallback: bt, useEffect: ft } = e, ht = {
  "js.drawer.close": "Close"
}, _t = ({ controlId: l }) => {
  const t = S(), s = P(), o = M(ht), r = t.open === !0, a = t.position ?? "right", n = t.size ?? "medium", c = t.title ?? null, i = t.child, u = bt(() => {
    s("close");
  }, [s]);
  ft(() => {
    if (!r) return;
    const f = (h) => {
      h.key === "Escape" && u();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [r, u]);
  const b = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${n}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: b, "aria-hidden": !r }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: u,
      title: o["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(L, { control: i })));
}, { useCallback: le, useEffect: Et, useState: vt } = e, gt = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.message ?? "", r = t.variant ?? "info", a = t.action, n = t.duration ?? 5e3, c = t.visible === !0, [i, u] = vt(!1), b = le(() => {
    u(!0), setTimeout(() => {
      s("dismiss"), u(!1);
    }, 200);
  }, [s]), f = le(() => {
    a && s(a.commandName), b();
  }, [s, a, b]);
  return Et(() => {
    if (!c || n === 0) return;
    const h = setTimeout(b, n);
    return () => clearTimeout(h);
  }, [c, n, b]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${r}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, o),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: f }, a.label)
  );
}, { useCallback: K, useEffect: Y, useRef: Ct, useState: oe } = e, yt = ({ controlId: l }) => {
  const t = S(), s = P(), o = t.open === !0, r = t.anchorId, a = t.items ?? [], n = Ct(null), [c, i] = oe({ top: 0, left: 0 }), [u, b] = oe(0), f = a.filter((d) => d.type === "item" && !d.disabled);
  Y(() => {
    var y, T;
    if (!o || !r) return;
    const d = document.getElementById(r);
    if (!d) return;
    const m = d.getBoundingClientRect(), C = ((y = n.current) == null ? void 0 : y.offsetHeight) ?? 200, g = ((T = n.current) == null ? void 0 : T.offsetWidth) ?? 200;
    let D = m.bottom + 4, R = m.left;
    D + C > window.innerHeight && (D = m.top - C - 4), R + g > window.innerWidth && (R = m.right - g), i({ top: D, left: R }), b(0);
  }, [o, r]);
  const h = K(() => {
    s("close");
  }, [s]), p = K((d) => {
    s("selectItem", { itemId: d });
  }, [s]);
  Y(() => {
    if (!o) return;
    const d = (m) => {
      n.current && !n.current.contains(m.target) && h();
    };
    return document.addEventListener("mousedown", d), () => document.removeEventListener("mousedown", d);
  }, [o, h]);
  const v = K((d) => {
    if (d.key === "Escape") {
      h();
      return;
    }
    if (d.key === "ArrowDown")
      d.preventDefault(), b((m) => (m + 1) % f.length);
    else if (d.key === "ArrowUp")
      d.preventDefault(), b((m) => (m - 1 + f.length) % f.length);
    else if (d.key === "Enter" || d.key === " ") {
      d.preventDefault();
      const m = f[u];
      m && p(m.id);
    }
  }, [h, p, f, u]);
  return Y(() => {
    o && n.current && n.current.focus();
  }, [o]), o ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: n,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: v
    },
    a.map((d, m) => {
      if (d.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: m, className: "tlMenu__separator" });
      const g = f.indexOf(d) === u;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: d.id,
          type: "button",
          className: "tlMenu__item" + (g ? " tlMenu__item--focused" : "") + (d.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: d.disabled,
          tabIndex: g ? 0 : -1,
          onClick: () => p(d.id)
        },
        d.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + d.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, d.label)
      );
    })
  ) : null;
}, kt = ({ controlId: l }) => {
  const t = S(), s = t.header, o = t.content, r = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(L, { control: s })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(L, { control: o })), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(L, { control: r })), /* @__PURE__ */ e.createElement(L, { control: a }));
}, wt = () => {
  const t = S().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Nt = () => {
  const l = S(), t = P(), s = l.columns ?? [], o = l.totalRowCount ?? 0, r = l.rows ?? [], a = l.rowHeight ?? 36, n = e.useRef(null), c = e.useRef(null), i = o * a, u = e.useCallback(() => {
    c.current !== null && clearTimeout(c.current), c.current = window.setTimeout(() => {
      const p = n.current;
      if (!p) return;
      const v = p.scrollTop, d = Math.ceil(p.clientHeight / a), m = Math.floor(v / a);
      t("scroll", { start: m, count: d });
    }, 80);
  }, [t, a]), b = e.useCallback((p, v) => {
    let d;
    !v || v === "desc" ? d = "asc" : d = "desc", t("sort", { column: p, direction: d });
  }, [t]), f = e.useCallback((p) => {
    t("select", { rowIndex: p });
  }, [t]), h = s.reduce((p, v) => p + v.width, 0);
  return /* @__PURE__ */ e.createElement("div", { className: "tlTableView" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", style: { width: h } }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow" }, s.map((p) => /* @__PURE__ */ e.createElement(
    "div",
    {
      key: p.name,
      className: "tlTableView__headerCell" + (p.sortable ? " tlTableView__headerCell--sortable" : ""),
      style: { width: p.width, minWidth: p.width },
      onClick: p.sortable ? () => b(p.name, p.sortDirection) : void 0
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, p.label),
    p.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, p.sortDirection === "asc" ? "▲" : "▼")
  )))), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: n,
      className: "tlTableView__body",
      onScroll: u
    },
    /* @__PURE__ */ e.createElement("div", { style: { height: i, position: "relative" } }, r.map((p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p.id,
        className: "tlTableView__row" + (p.selected ? " tlTableView__row--selected" : ""),
        style: {
          position: "absolute",
          top: p.index * a,
          height: a,
          width: h
        },
        onClick: () => f(p.index)
      },
      s.map((v) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: v.name,
          className: "tlTableView__cell",
          style: { width: v.width, minWidth: v.width }
        },
        /* @__PURE__ */ e.createElement(L, { control: p.cells[v.name] })
      ))
    )))
  ));
};
w("TLButton", ge);
w("TLToggleButton", ye);
w("TLTextInput", ie);
w("TLNumberInput", ue);
w("TLDatePicker", pe);
w("TLSelect", fe);
w("TLCheckbox", _e);
w("TLTable", Ee);
w("TLCounter", ke);
w("TLTabBar", Ne);
w("TLFieldList", Se);
w("TLAudioRecorder", Te);
w("TLAudioPlayer", xe);
w("TLFileUpload", Be);
w("TLDownload", je);
w("TLPhotoCapture", Ae);
w("TLPhotoViewer", $e);
w("TLSplitPanel", ze);
w("TLPanel", Ke);
w("TLMaximizeRoot", Ye);
w("TLDeckPane", Ge);
w("TLSidebar", at);
w("TLStack", nt);
w("TLGrid", lt);
w("TLCard", ot);
w("TLAppBar", rt);
w("TLBreadcrumb", ct);
w("TLBottomBar", dt);
w("TLDialog", pt);
w("TLDrawer", _t);
w("TLSnackbar", gt);
w("TLMenu", yt);
w("TLAppShell", kt);
w("TLTextCell", wt);
w("TLTableView", Nt);
