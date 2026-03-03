import { React as e, useTLFieldValue as F, getComponent as X, useTLState as k, useTLCommand as j, TLChild as R, useTLUpload as V, useI18N as D, useTLDataUrl as H, register as _ } from "tl-react-bridge";
const { useCallback: Z } = e, J = ({ state: t }) => {
  const [n, a] = F(), r = Z(
    (l) => {
      a(l.target.value);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Q } = e, ee = ({ state: t, config: n }) => {
  const [a, r] = F(), l = Q(
    (s) => {
      const c = s.target.value, i = c === "" ? null : Number(c);
      r(i);
    },
    [r]
  ), o = n != null && n.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: l,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: te } = e, ae = ({ state: t }) => {
  const [n, a] = F(), r = te(
    (l) => {
      a(l.target.value || null);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: le } = e, ne = ({ state: t, config: n }) => {
  var s;
  const [a, r] = F(), l = le(
    (c) => {
      r(c.target.value || null);
    },
    [r]
  ), o = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const c = ((s = o.find((i) => i.value === a)) == null ? void 0 : s.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, c);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: l,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: oe } = e, re = ({ state: t }) => {
  const [n, a] = F(), r = oe(
    (l) => {
      a(l.target.checked);
    },
    [a]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: n === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: n === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, se = ({ controlId: t, state: n }) => {
  const a = n.columns ?? [], r = n.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((l) => /* @__PURE__ */ e.createElement("th", { key: l.name }, l.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((l, o) => /* @__PURE__ */ e.createElement("tr", { key: o }, a.map((s) => {
    const c = s.cellModule ? X(s.cellModule) : void 0, i = l[s.name];
    if (c) {
      const d = { value: i, editable: n.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: t + "-" + o + "-" + s.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: ce } = e, ie = ({ command: t, label: n, disabled: a }) => {
  const r = k(), l = j(), o = t ?? "click", s = n ?? r.label, c = a ?? r.disabled === !0, i = ce(() => {
    l(o);
  }, [l, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: c,
      className: "tlReactButton"
    },
    s
  );
}, { useCallback: de } = e, ue = ({ command: t, label: n, active: a, disabled: r }) => {
  const l = k(), o = j(), s = t ?? "click", c = n ?? l.label, i = a ?? l.active === !0, d = r ?? l.disabled === !0, m = de(() => {
    o(s);
  }, [o, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: m,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, me = () => {
  const t = k(), n = j(), a = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: pe } = e, be = () => {
  const t = k(), n = j(), a = t.tabs ?? [], r = t.activeTabId, l = pe((o) => {
    o !== r && n("selectTab", { tabId: o });
  }, [n, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === r,
      className: "tlReactTabBar__tab" + (o.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => l(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, he = () => {
  const t = k(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((r, l) => /* @__PURE__ */ e.createElement("div", { key: l, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: r })))));
}, fe = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ve = () => {
  const t = k(), n = V(), [a, r] = e.useState("idle"), [l, o] = e.useState(null), s = e.useRef(null), c = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", m = t.error, p = d === "received" ? "idle" : a !== "idle" ? a : d, S = e.useCallback(async () => {
    if (a === "recording") {
      const b = s.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (a !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = b, c.current = [];
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", N = new MediaRecorder(b, T ? { mimeType: T } : void 0);
        s.current = N, N.ondataavailable = (f) => {
          f.data.size > 0 && c.current.push(f.data);
        }, N.onstop = async () => {
          b.getTracks().forEach((w) => w.stop()), i.current = null;
          const f = new Blob(c.current, { type: N.mimeType || "audio/webm" });
          if (c.current = [], f.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const y = new FormData();
          y.append("audio", f, "recording.webm"), await n(y), r("idle");
        }, N.start(), r("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), o("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [a, n]), C = D(fe), E = p === "recording" ? C["js.audioRecorder.stop"] : p === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], u = p === "uploading", v = ["tlAudioRecorder__button"];
  return p === "recording" && v.push("tlAudioRecorder__button--recording"), p === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: S,
      disabled: u,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[l]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Ee = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, _e = () => {
  const t = k(), n = H(), a = !!t.hasAudio, r = t.dataRevision ?? 0, [l, o] = e.useState(a ? "idle" : "disabled"), s = e.useRef(null), c = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    a ? l === "disabled" && o("idle") : (s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), o("disabled"));
  }, [a]), e.useEffect(() => {
    r !== i.current && (i.current = r, s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (l === "playing" || l === "paused" || l === "loading") && o("idle"));
  }, [r]), e.useEffect(() => () => {
    s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const d = e.useCallback(async () => {
    if (l === "disabled" || l === "loading")
      return;
    if (l === "playing") {
      s.current && s.current.pause(), o("paused");
      return;
    }
    if (l === "paused" && s.current) {
      s.current.play(), o("playing");
      return;
    }
    if (!c.current) {
      o("loading");
      try {
        const u = await fetch(n);
        if (!u.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", u.status), o("idle");
          return;
        }
        const v = await u.blob();
        c.current = URL.createObjectURL(v);
      } catch (u) {
        console.error("[TLAudioPlayer] Fetch error:", u), o("idle");
        return;
      }
    }
    const E = new Audio(c.current);
    s.current = E, E.onended = () => {
      o("idle");
    }, E.play(), o("playing");
  }, [l, n]), m = D(Ee), p = l === "loading" ? m["js.loading"] : l === "playing" ? m["js.audioPlayer.pause"] : l === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], S = l === "disabled" || l === "loading", C = ["tlAudioPlayer__button"];
  return l === "playing" && C.push("tlAudioPlayer__button--playing"), l === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: d,
      disabled: S,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${l === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ce = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ge = () => {
  const t = k(), n = V(), [a, r] = e.useState("idle"), [l, o] = e.useState(!1), s = e.useRef(null), c = t.status ?? "idle", i = t.error, d = t.accept ?? "", m = c === "received" ? "idle" : a !== "idle" ? a : c, p = e.useCallback(async (f) => {
    r("uploading");
    const y = new FormData();
    y.append("file", f, f.name), await n(y), r("idle");
  }, [n]), S = e.useCallback((f) => {
    var w;
    const y = (w = f.target.files) == null ? void 0 : w[0];
    y && p(y);
  }, [p]), C = e.useCallback(() => {
    var f;
    a !== "uploading" && ((f = s.current) == null || f.click());
  }, [a]), E = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), u = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), v = e.useCallback((f) => {
    var w;
    if (f.preventDefault(), f.stopPropagation(), o(!1), a === "uploading") return;
    const y = (w = f.dataTransfer.files) == null ? void 0 : w[0];
    y && p(y);
  }, [a, p]), b = m === "uploading", T = D(Ce), N = m === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${l ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: u,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: s,
        type: "file",
        accept: d || void 0,
        onChange: S,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: b,
        title: N,
        "aria-label": N
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, ye = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ke = () => {
  const t = k(), n = H(), a = j(), r = !!t.hasData, l = t.dataRevision ?? 0, o = t.fileName ?? "download", s = !!t.clearable, [c, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!r || c)) {
      i(!0);
      try {
        const C = n + (n.includes("?") ? "&" : "?") + "rev=" + l, E = await fetch(C);
        if (!E.ok) {
          console.error("[TLDownload] Failed to fetch data:", E.status);
          return;
        }
        const u = await E.blob(), v = URL.createObjectURL(u), b = document.createElement("a");
        b.href = v, b.download = o, b.style.display = "none", document.body.appendChild(b), b.click(), document.body.removeChild(b), URL.revokeObjectURL(v);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        i(!1);
      }
    }
  }, [r, c, n, l, o]), m = e.useCallback(async () => {
    r && await a("clear");
  }, [r, a]), p = D(ye);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const S = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: c,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: m,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Se = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ne = () => {
  const t = k(), n = V(), [a, r] = e.useState("idle"), [l, o] = e.useState(null), [s, c] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), m = e.useRef(null), p = e.useRef(null), S = e.useRef(null), C = t.error, E = e.useMemo(
    () => {
      var h;
      return !!(window.isSecureContext && ((h = navigator.mediaDevices) != null && h.getUserMedia));
    },
    []
  ), u = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((h) => h.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    u(), r("idle");
  }, [u]), b = e.useCallback(async () => {
    var h;
    if (a !== "uploading") {
      if (o(null), !E) {
        (h = p.current) == null || h.click();
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = g, r("overlayOpen");
      } catch (g) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", g), o("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [a, E]), T = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const h = i.current, g = m.current;
    if (!h || !g)
      return;
    g.width = h.videoWidth, g.height = h.videoHeight;
    const L = g.getContext("2d");
    L && (L.drawImage(h, 0, 0), u(), r("uploading"), g.toBlob(async (x) => {
      if (!x) {
        r("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", x, "capture.jpg"), await n(I), r("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, u]), N = e.useCallback(async (h) => {
    var x;
    const g = (x = h.target.files) == null ? void 0 : x[0];
    if (!g) return;
    r("uploading");
    const L = new FormData();
    L.append("photo", g, g.name), await n(L), r("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [a]), e.useEffect(() => {
    var g;
    if (a !== "overlayOpen") return;
    (g = S.current) == null || g.focus();
    const h = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = h;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const h = (g) => {
      g.key === "Escape" && v();
    };
    return document.addEventListener("keydown", h), () => document.removeEventListener("keydown", h);
  }, [a, v]), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((h) => h.stop()), d.current = null);
  }, []);
  const f = D(Se), y = a === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], w = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && w.push("tlPhotoCapture__cameraBtn--uploading");
  const z = ["tlPhotoCapture__overlayVideo"];
  s && z.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return s && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: b,
      disabled: a === "uploading",
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !E && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: p,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: N
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: S,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: z.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: P.join(" "),
        onClick: () => c((h) => !h),
        title: f["js.photoCapture.mirror"],
        "aria-label": f["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: f["js.photoCapture.capture"],
        "aria-label": f["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: f["js.photoCapture.close"],
        "aria-label": f["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[l]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, we = {
  "js.photoViewer.alt": "Captured photo"
}, Le = () => {
  const t = k(), n = H(), a = !!t.hasPhoto, r = t.dataRevision ?? 0, [l, o] = e.useState(null), s = e.useRef(r);
  e.useEffect(() => {
    if (!a) {
      l && (URL.revokeObjectURL(l), o(null));
      return;
    }
    if (r === s.current && l)
      return;
    s.current = r, l && (URL.revokeObjectURL(l), o(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const m = await d.blob();
        i || o(URL.createObjectURL(m));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [a, r, n]), e.useEffect(() => () => {
    l && URL.revokeObjectURL(l);
  }, []);
  const c = D(we);
  return !a || !l ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: l,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: q, useRef: O } = e, Re = () => {
  const t = k(), n = j(), a = t.orientation, r = t.resizable === !0, l = t.children ?? [], o = a === "horizontal", s = l.length > 0 && l.every((u) => u.collapsed), c = !s && l.some((u) => u.collapsed), i = s ? !o : o, d = O(null), m = O(null), p = O(null), S = q((u, v) => {
    const b = {
      overflow: u.scrolling || "auto"
    };
    return u.collapsed ? s && !i ? b.flex = "1 0 0%" : b.flex = "0 0 auto" : v !== void 0 ? b.flex = `0 0 ${v}px` : u.unit === "%" || c ? b.flex = `${u.size} 0 0%` : b.flex = `0 0 ${u.size}px`, u.minSize > 0 && !u.collapsed && (b.minWidth = o ? u.minSize : void 0, b.minHeight = o ? void 0 : u.minSize), b;
  }, [o, s, c, i]), C = q((u, v) => {
    u.preventDefault();
    const b = d.current;
    if (!b) return;
    const T = l[v], N = l[v + 1], f = b.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    f.forEach((P) => {
      y.push(o ? P.offsetWidth : P.offsetHeight);
    }), p.current = y, m.current = {
      splitterIndex: v,
      startPos: o ? u.clientX : u.clientY,
      startSizeBefore: y[v],
      startSizeAfter: y[v + 1],
      childBefore: T,
      childAfter: N
    };
    const w = (P) => {
      const h = m.current;
      if (!h || !p.current) return;
      const L = (o ? P.clientX : P.clientY) - h.startPos, x = h.childBefore.minSize || 0, I = h.childAfter.minSize || 0;
      let M = h.startSizeBefore + L, B = h.startSizeAfter - L;
      M < x && (B += M - x, M = x), B < I && (M += B - I, B = I), p.current[h.splitterIndex] = M, p.current[h.splitterIndex + 1] = B;
      const W = b.querySelectorAll(":scope > .tlSplitPanel__child"), Y = W[h.splitterIndex], K = W[h.splitterIndex + 1];
      Y && (Y.style.flex = `0 0 ${M}px`), K && (K.style.flex = `0 0 ${B}px`);
    }, z = () => {
      if (document.removeEventListener("mousemove", w), document.removeEventListener("mouseup", z), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const P = {};
        l.forEach((h, g) => {
          const L = h.control;
          L != null && L.controlId && p.current && (P[L.controlId] = p.current[g]);
        }), n("updateSizes", { sizes: P });
      }
      p.current = null, m.current = null;
    };
    document.addEventListener("mousemove", w), document.addEventListener("mouseup", z), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [l, o, n]), E = [];
  return l.forEach((u, v) => {
    if (E.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${u.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: S(u)
        },
        /* @__PURE__ */ e.createElement(R, { control: u.control })
      )
    ), r && v < l.length - 1) {
      const b = l[v + 1];
      !u.collapsed && !b.collapsed && E.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (N) => C(N, v)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: d,
      className: `tlSplitPanel tlSplitPanel--${a}${s ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    E
  );
}, { useCallback: $ } = e, Pe = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Te = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), xe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), De = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Ie = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Me = () => {
  const t = k(), n = j(), a = D(Pe), r = t.title, l = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, s = t.showMaximize === !0, c = t.showPopOut === !0, i = t.toolbarButtons ?? [], d = l === "MINIMIZED", m = l === "MAXIMIZED", p = l === "HIDDEN", S = $(() => {
    n("toggleMinimize");
  }, [n]), C = $(() => {
    n("toggleMaximize");
  }, [n]), E = $(() => {
    n("popOut");
  }, [n]);
  if (p)
    return null;
  const u = m ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${l.toLowerCase()}`,
      style: u
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((v, b) => /* @__PURE__ */ e.createElement("span", { key: b, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(R, { control: v }))), o && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: d ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      d ? /* @__PURE__ */ e.createElement(xe, null) : /* @__PURE__ */ e.createElement(Te, null)
    ), s && !d && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: m ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      m ? /* @__PURE__ */ e.createElement(De, null) : /* @__PURE__ */ e.createElement(je, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Ie, null)
    ))),
    !d && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child }))
  );
}, Be = () => {
  const t = k();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, ze = () => {
  const t = k();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: U, useState: Ue } = e, Ae = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
}, A = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Fe = ({ item: t, active: n, collapsed: a, onSelect: r }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (n ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(t.id),
    title: a ? t.label : void 0
  },
  a && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !a && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), Oe = ({ item: t, collapsed: n, onExecute: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(t.id),
    title: n ? t.label : void 0
  },
  /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), $e = ({ item: t, collapsed: n }) => n && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: n ? t.label : void 0 }, /* @__PURE__ */ e.createElement(A, { icon: t.icon }), !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), Ve = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), He = ({ item: t, activeItemId: n, collapsed: a, onSelect: r, onExecute: l, onToggleGroup: o }) => {
  const [s, c] = Ue(t.expanded), i = U(() => {
    const d = !s;
    c(d), o(t.id, d);
  }, [s, t.id, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: i,
      title: a ? t.label : void 0,
      "aria-expanded": s
    },
    /* @__PURE__ */ e.createElement(A, { icon: t.icon }),
    !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
    !a && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlSidebar__chevron" + (s ? " tlSidebar__chevron--open" : ""),
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
  ), s && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((d) => /* @__PURE__ */ e.createElement(
    G,
    {
      key: d.id,
      item: d,
      activeItemId: n,
      collapsed: a,
      onSelect: r,
      onExecute: l,
      onToggleGroup: o
    }
  ))));
}, G = ({ item: t, activeItemId: n, collapsed: a, onSelect: r, onExecute: l, onToggleGroup: o }) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Fe,
        {
          item: t,
          active: t.id === n,
          collapsed: a,
          onSelect: r
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(Oe, { item: t, collapsed: a, onExecute: l });
    case "header":
      return /* @__PURE__ */ e.createElement($e, { item: t, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ve, null);
    case "group":
      return /* @__PURE__ */ e.createElement(
        He,
        {
          item: t,
          activeItemId: n,
          collapsed: a,
          onSelect: r,
          onExecute: l,
          onToggleGroup: o
        }
      );
    default:
      return null;
  }
}, We = () => {
  const t = k(), n = j(), a = D(Ae), r = t.items ?? [], l = t.activeItemId, o = t.collapsed, s = U((m) => {
    m !== l && n("selectItem", { itemId: m });
  }, [n, l]), c = U((m) => {
    n("executeCommand", { itemId: m });
  }, [n]), i = U(() => {
    n("toggleCollapse", {});
  }, [n]), d = U((m, p) => {
    n("toggleGroup", { itemId: m, expanded: p });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": a["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items" }, r.map((m) => /* @__PURE__ */ e.createElement(
    G,
    {
      key: m.id,
      item: m,
      activeItemId: l,
      collapsed: o,
      onSelect: s,
      onExecute: c,
      onToggleGroup: d
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: i,
      title: o ? a["js.sidebar.expand"] : a["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
};
_("TLButton", ie);
_("TLToggleButton", ue);
_("TLTextInput", J);
_("TLNumberInput", ee);
_("TLDatePicker", ae);
_("TLSelect", ne);
_("TLCheckbox", re);
_("TLTable", se);
_("TLCounter", me);
_("TLTabBar", be);
_("TLFieldList", he);
_("TLAudioRecorder", ve);
_("TLAudioPlayer", _e);
_("TLFileUpload", ge);
_("TLDownload", ke);
_("TLPhotoCapture", Ne);
_("TLPhotoViewer", Le);
_("TLSplitPanel", Re);
_("TLPanel", Me);
_("TLMaximizeRoot", Be);
_("TLDeckPane", ze);
_("TLSidebar", We);
