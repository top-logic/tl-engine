import { React as e, useTLFieldValue as A, getComponent as X, useTLState as k, useTLCommand as j, TLChild as T, useTLUpload as V, useI18N as D, useTLDataUrl as H, register as _ } from "tl-react-bridge";
const { useCallback: Z } = e, J = ({ state: t }) => {
  const [n, a] = A(), o = Z(
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
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Q } = e, ee = ({ state: t, config: n }) => {
  const [a, o] = A(), l = Q(
    (s) => {
      const c = s.target.value, u = c === "" ? null : Number(c);
      o(u);
    },
    [o]
  ), r = n != null && n.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, a != null ? String(a) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: l,
      step: r,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: te } = e, ae = ({ state: t }) => {
  const [n, a] = A(), o = te(
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
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: le } = e, ne = ({ state: t, config: n }) => {
  var s;
  const [a, o] = A(), l = le(
    (c) => {
      o(c.target.value || null);
    },
    [o]
  ), r = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const c = ((s = r.find((u) => u.value === a)) == null ? void 0 : s.label) ?? "";
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
    r.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: oe } = e, re = ({ state: t }) => {
  const [n, a] = A(), o = oe(
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
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, se = ({ controlId: t, state: n }) => {
  const a = n.columns ?? [], o = n.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((l) => /* @__PURE__ */ e.createElement("th", { key: l.name }, l.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((l, r) => /* @__PURE__ */ e.createElement("tr", { key: r }, a.map((s) => {
    const c = s.cellModule ? X(s.cellModule) : void 0, u = l[s.name];
    if (c) {
      const i = { value: u, editable: n.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: t + "-" + r + "-" + s.name,
          state: i
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, u != null ? String(u) : "");
  })))));
}, { useCallback: ce } = e, ie = ({ command: t, label: n, disabled: a }) => {
  const o = k(), l = j(), r = t ?? "click", s = n ?? o.label, c = a ?? o.disabled === !0, u = ce(() => {
    l(r);
  }, [l, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: u,
      disabled: c,
      className: "tlReactButton"
    },
    s
  );
}, { useCallback: ue } = e, de = ({ command: t, label: n, active: a, disabled: o }) => {
  const l = k(), r = j(), s = t ?? "click", c = n ?? l.label, u = a ?? l.active === !0, i = o ?? l.disabled === !0, v = ue(() => {
    r(s);
  }, [r, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: i,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    c
  );
}, me = () => {
  const t = k(), n = j(), a = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: pe } = e, be = () => {
  const t = k(), n = j(), a = t.tabs ?? [], o = t.activeTabId, l = pe((r) => {
    r !== o && n("selectTab", { tabId: r });
  }, [n, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((r) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: r.id,
      role: "tab",
      "aria-selected": r.id === o,
      className: "tlReactTabBar__tab" + (r.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => l(r.id)
    },
    r.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, he = () => {
  const t = k(), n = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((o, l) => /* @__PURE__ */ e.createElement("div", { key: l, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(T, { control: o })))));
}, fe = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ve = () => {
  const t = k(), n = V(), [a, o] = e.useState("idle"), [l, r] = e.useState(null), s = e.useRef(null), c = e.useRef([]), u = e.useRef(null), i = t.status ?? "idle", v = t.error, p = i === "received" ? "idle" : a !== "idle" ? a : i, w = e.useCallback(async () => {
    if (a === "recording") {
      const m = s.current;
      m && m.state !== "inactive" && m.stop();
      return;
    }
    if (a !== "uploading") {
      if (r(null), !window.isSecureContext || !navigator.mediaDevices) {
        r("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const m = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        u.current = m, c.current = [];
        const P = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", N = new MediaRecorder(m, P ? { mimeType: P } : void 0);
        s.current = N, N.ondataavailable = (h) => {
          h.data.size > 0 && c.current.push(h.data);
        }, N.onstop = async () => {
          m.getTracks().forEach((L) => L.stop()), u.current = null;
          const h = new Blob(c.current, { type: N.mimeType || "audio/webm" });
          if (c.current = [], h.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const y = new FormData();
          y.append("audio", h, "recording.webm"), await n(y), o("idle");
        }, N.start(), o("recording");
      } catch (m) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", m), r("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [a, n]), C = D(fe), E = p === "recording" ? C["js.audioRecorder.stop"] : p === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], d = p === "uploading", f = ["tlAudioRecorder__button"];
  return p === "recording" && f.push("tlAudioRecorder__button--recording"), p === "uploading" && f.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: w,
      disabled: d,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[l]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, Ee = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, _e = () => {
  const t = k(), n = H(), a = !!t.hasAudio, o = t.dataRevision ?? 0, [l, r] = e.useState(a ? "idle" : "disabled"), s = e.useRef(null), c = e.useRef(null), u = e.useRef(o);
  e.useEffect(() => {
    a ? l === "disabled" && r("idle") : (s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), r("disabled"));
  }, [a]), e.useEffect(() => {
    o !== u.current && (u.current = o, s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (l === "playing" || l === "paused" || l === "loading") && r("idle"));
  }, [o]), e.useEffect(() => () => {
    s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const i = e.useCallback(async () => {
    if (l === "disabled" || l === "loading")
      return;
    if (l === "playing") {
      s.current && s.current.pause(), r("paused");
      return;
    }
    if (l === "paused" && s.current) {
      s.current.play(), r("playing");
      return;
    }
    if (!c.current) {
      r("loading");
      try {
        const d = await fetch(n);
        if (!d.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", d.status), r("idle");
          return;
        }
        const f = await d.blob();
        c.current = URL.createObjectURL(f);
      } catch (d) {
        console.error("[TLAudioPlayer] Fetch error:", d), r("idle");
        return;
      }
    }
    const E = new Audio(c.current);
    s.current = E, E.onended = () => {
      r("idle");
    }, E.play(), r("playing");
  }, [l, n]), v = D(Ee), p = l === "loading" ? v["js.loading"] : l === "playing" ? v["js.audioPlayer.pause"] : l === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], w = l === "disabled" || l === "loading", C = ["tlAudioPlayer__button"];
  return l === "playing" && C.push("tlAudioPlayer__button--playing"), l === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: i,
      disabled: w,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${l === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ce = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ge = () => {
  const t = k(), n = V(), [a, o] = e.useState("idle"), [l, r] = e.useState(!1), s = e.useRef(null), c = t.status ?? "idle", u = t.error, i = t.accept ?? "", v = c === "received" ? "idle" : a !== "idle" ? a : c, p = e.useCallback(async (h) => {
    o("uploading");
    const y = new FormData();
    y.append("file", h, h.name), await n(y), o("idle");
  }, [n]), w = e.useCallback((h) => {
    var L;
    const y = (L = h.target.files) == null ? void 0 : L[0];
    y && p(y);
  }, [p]), C = e.useCallback(() => {
    var h;
    a !== "uploading" && ((h = s.current) == null || h.click());
  }, [a]), E = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), r(!0);
  }, []), d = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), r(!1);
  }, []), f = e.useCallback((h) => {
    var L;
    if (h.preventDefault(), h.stopPropagation(), r(!1), a === "uploading") return;
    const y = (L = h.dataTransfer.files) == null ? void 0 : L[0];
    y && p(y);
  }, [a, p]), m = v === "uploading", P = D(Ce), N = v === "uploading" ? P["js.uploading"] : P["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${l ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: d,
      onDrop: f
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: s,
        type: "file",
        accept: i || void 0,
        onChange: w,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (v === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: m,
        title: N,
        "aria-label": N
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
}, ye = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ke = () => {
  const t = k(), n = H(), a = j(), o = !!t.hasData, l = t.dataRevision ?? 0, r = t.fileName ?? "download", s = !!t.clearable, [c, u] = e.useState(!1), i = e.useCallback(async () => {
    if (!(!o || c)) {
      u(!0);
      try {
        const C = n + (n.includes("?") ? "&" : "?") + "rev=" + l, E = await fetch(C);
        if (!E.ok) {
          console.error("[TLDownload] Failed to fetch data:", E.status);
          return;
        }
        const d = await E.blob(), f = URL.createObjectURL(d), m = document.createElement("a");
        m.href = f, m.download = r, m.style.display = "none", document.body.appendChild(m), m.click(), document.body.removeChild(m), URL.revokeObjectURL(f);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        u(!1);
      }
    }
  }, [o, c, n, l, r]), v = e.useCallback(async () => {
    o && await a("clear");
  }, [o, a]), p = D(ye);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const w = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", r);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: i,
      disabled: c,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: r }, r), s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, we = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ne = () => {
  const t = k(), n = V(), [a, o] = e.useState("idle"), [l, r] = e.useState(null), [s, c] = e.useState(!1), u = e.useRef(null), i = e.useRef(null), v = e.useRef(null), p = e.useRef(null), w = e.useRef(null), C = t.error, E = e.useMemo(
    () => {
      var b;
      return !!(window.isSecureContext && ((b = navigator.mediaDevices) != null && b.getUserMedia));
    },
    []
  ), d = e.useCallback(() => {
    i.current && (i.current.getTracks().forEach((b) => b.stop()), i.current = null), u.current && (u.current.srcObject = null);
  }, []), f = e.useCallback(() => {
    d(), o("idle");
  }, [d]), m = e.useCallback(async () => {
    var b;
    if (a !== "uploading") {
      if (r(null), !E) {
        (b = p.current) == null || b.click();
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        i.current = g, o("overlayOpen");
      } catch (g) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", g), r("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [a, E]), P = e.useCallback(async () => {
    if (a !== "overlayOpen")
      return;
    const b = u.current, g = v.current;
    if (!b || !g)
      return;
    g.width = b.videoWidth, g.height = b.videoHeight;
    const R = g.getContext("2d");
    R && (R.drawImage(b, 0, 0), d(), o("uploading"), g.toBlob(async (x) => {
      if (!x) {
        o("idle");
        return;
      }
      const M = new FormData();
      M.append("photo", x, "capture.jpg"), await n(M), o("idle");
    }, "image/jpeg", 0.85));
  }, [a, n, d]), N = e.useCallback(async (b) => {
    var x;
    const g = (x = b.target.files) == null ? void 0 : x[0];
    if (!g) return;
    o("uploading");
    const R = new FormData();
    R.append("photo", g, g.name), await n(R), o("idle"), p.current && (p.current.value = "");
  }, [n]);
  e.useEffect(() => {
    a === "overlayOpen" && u.current && i.current && (u.current.srcObject = i.current);
  }, [a]), e.useEffect(() => {
    var g;
    if (a !== "overlayOpen") return;
    (g = w.current) == null || g.focus();
    const b = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = b;
    };
  }, [a]), e.useEffect(() => {
    if (a !== "overlayOpen") return;
    const b = (g) => {
      g.key === "Escape" && f();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [a, f]), e.useEffect(() => () => {
    i.current && (i.current.getTracks().forEach((b) => b.stop()), i.current = null);
  }, []);
  const h = D(we), y = a === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], L = ["tlPhotoCapture__cameraBtn"];
  a === "uploading" && L.push("tlPhotoCapture__cameraBtn--uploading");
  const z = ["tlPhotoCapture__overlayVideo"];
  s && z.push("tlPhotoCapture__overlayVideo--mirrored");
  const S = ["tlPhotoCapture__mirrorBtn"];
  return s && S.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: m,
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), a === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: w,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: f }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: u,
        className: z.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: S.join(" "),
        onClick: () => c((b) => !b),
        title: h["js.photoCapture.mirror"],
        "aria-label": h["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: P,
        title: h["js.photoCapture.capture"],
        "aria-label": h["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: f,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[l]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, Le = {
  "js.photoViewer.alt": "Captured photo"
}, Re = () => {
  const t = k(), n = H(), a = !!t.hasPhoto, o = t.dataRevision ?? 0, [l, r] = e.useState(null), s = e.useRef(o);
  e.useEffect(() => {
    if (!a) {
      l && (URL.revokeObjectURL(l), r(null));
      return;
    }
    if (o === s.current && l)
      return;
    s.current = o, l && (URL.revokeObjectURL(l), r(null));
    let u = !1;
    return (async () => {
      try {
        const i = await fetch(n);
        if (!i.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", i.status);
          return;
        }
        const v = await i.blob();
        u || r(URL.createObjectURL(v));
      } catch (i) {
        console.error("[TLPhotoViewer] Fetch error:", i);
      }
    })(), () => {
      u = !0;
    };
  }, [a, o, n]), e.useEffect(() => () => {
    l && URL.revokeObjectURL(l);
  }, []);
  const c = D(Le);
  return !a || !l ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: l,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: q, useRef: O } = e, Se = () => {
  const t = k(), n = j(), a = t.orientation, o = t.resizable === !0, l = t.children ?? [], r = a === "horizontal", s = l.length > 0 && l.every((d) => d.collapsed), c = !s && l.some((d) => d.collapsed), u = s ? !r : r, i = O(null), v = O(null), p = O(null), w = q((d, f) => {
    const m = {
      overflow: d.scrolling || "auto"
    };
    return d.collapsed ? s && !u ? m.flex = "1 0 0%" : m.flex = "0 0 auto" : f !== void 0 ? m.flex = `0 0 ${f}px` : d.unit === "%" || c ? m.flex = `${d.size} 0 0%` : m.flex = `0 0 ${d.size}px`, d.minSize > 0 && !d.collapsed && (m.minWidth = r ? d.minSize : void 0, m.minHeight = r ? void 0 : d.minSize), m;
  }, [r, s, c, u]), C = q((d, f) => {
    d.preventDefault();
    const m = i.current;
    if (!m) return;
    const P = l[f], N = l[f + 1], h = m.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    h.forEach((S) => {
      y.push(r ? S.offsetWidth : S.offsetHeight);
    }), p.current = y, v.current = {
      splitterIndex: f,
      startPos: r ? d.clientX : d.clientY,
      startSizeBefore: y[f],
      startSizeAfter: y[f + 1],
      childBefore: P,
      childAfter: N
    };
    const L = (S) => {
      const b = v.current;
      if (!b || !p.current) return;
      const R = (r ? S.clientX : S.clientY) - b.startPos, x = b.childBefore.minSize || 0, M = b.childAfter.minSize || 0;
      let I = b.startSizeBefore + R, B = b.startSizeAfter - R;
      I < x && (B += I - x, I = x), B < M && (I += B - M, B = M), p.current[b.splitterIndex] = I, p.current[b.splitterIndex + 1] = B;
      const W = m.querySelectorAll(":scope > .tlSplitPanel__child"), Y = W[b.splitterIndex], K = W[b.splitterIndex + 1];
      Y && (Y.style.flex = `0 0 ${I}px`), K && (K.style.flex = `0 0 ${B}px`);
    }, z = () => {
      if (document.removeEventListener("mousemove", L), document.removeEventListener("mouseup", z), document.body.style.cursor = "", document.body.style.userSelect = "", p.current) {
        const S = {};
        l.forEach((b, g) => {
          const R = b.control;
          R != null && R.controlId && p.current && (S[R.controlId] = p.current[g]);
        }), n("updateSizes", { sizes: S });
      }
      p.current = null, v.current = null;
    };
    document.addEventListener("mousemove", L), document.addEventListener("mouseup", z), document.body.style.cursor = r ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [l, r, n]), E = [];
  return l.forEach((d, f) => {
    if (E.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${f}`,
          className: `tlSplitPanel__child${d.collapsed && u ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: w(d)
        },
        /* @__PURE__ */ e.createElement(T, { control: d.control })
      )
    ), o && f < l.length - 1) {
      const m = l[f + 1];
      !d.collapsed && !m.collapsed && E.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${f}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${a}`,
            onMouseDown: (N) => C(N, f)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: `tlSplitPanel tlSplitPanel--${a}${s ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: u ? "row" : "column",
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
}, Te = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), xe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), De = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Me = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Ie = () => {
  const t = k(), n = j(), a = D(Pe), o = t.title, l = t.expansionState ?? "NORMALIZED", r = t.showMinimize === !0, s = t.showMaximize === !0, c = t.showPopOut === !0, u = t.toolbarButtons ?? [], i = l === "MINIMIZED", v = l === "MAXIMIZED", p = l === "HIDDEN", w = $(() => {
    n("toggleMinimize");
  }, [n]), C = $(() => {
    n("toggleMaximize");
  }, [n]), E = $(() => {
    n("popOut");
  }, [n]);
  if (p)
    return null;
  const d = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${l.toLowerCase()}`,
      style: d
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, u.map((f, m) => /* @__PURE__ */ e.createElement("span", { key: m, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(T, { control: f }))), r && !v && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: w,
        title: i ? a["js.panel.restore"] : a["js.panel.minimize"]
      },
      i ? /* @__PURE__ */ e.createElement(xe, null) : /* @__PURE__ */ e.createElement(Te, null)
    ), s && !i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: v ? a["js.panel.restore"] : a["js.panel.maximize"]
      },
      v ? /* @__PURE__ */ e.createElement(De, null) : /* @__PURE__ */ e.createElement(je, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: a["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Me, null)
    ))),
    !i && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(T, { control: t.child }))
  );
}, Be = () => {
  const t = k();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(T, { control: t.child })
  );
}, ze = () => {
  const t = k();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(T, { control: t.activeChild }));
}, { useCallback: U, useState: Ue } = e, F = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Ae = ({ item: t, active: n, collapsed: a, onSelect: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__navItem" + (n ? " tlSidebar__navItem--active" : ""),
    onClick: () => o(t.id),
    title: a ? t.label : void 0
  },
  /* @__PURE__ */ e.createElement(F, { icon: t.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Fe = ({ item: t, collapsed: n, onExecute: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__commandItem",
    onClick: () => a(t.id),
    title: n ? t.label : void 0
  },
  /* @__PURE__ */ e.createElement(F, { icon: t.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Oe = ({ item: t, collapsed: n }) => /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: n ? t.label : void 0 }, /* @__PURE__ */ e.createElement(F, { icon: t.icon }), !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), $e = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ve = ({ item: t, activeItemId: n, collapsed: a, onSelect: o, onExecute: l, onToggleGroup: r }) => {
  const [s, c] = Ue(t.expanded), u = U(() => {
    const i = !s;
    c(i), r(t.id, i);
  }, [s, t.id, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__groupHeader",
      onClick: u,
      title: a ? t.label : void 0
    },
    /* @__PURE__ */ e.createElement(F, { icon: t.icon }),
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
  ), s && !a && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((i) => /* @__PURE__ */ e.createElement(
    G,
    {
      key: i.id,
      item: i,
      activeItemId: n,
      collapsed: a,
      onSelect: o,
      onExecute: l,
      onToggleGroup: r
    }
  ))));
}, G = ({ item: t, activeItemId: n, collapsed: a, onSelect: o, onExecute: l, onToggleGroup: r }) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ae,
        {
          item: t,
          active: t.id === n,
          collapsed: a,
          onSelect: o
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(Fe, { item: t, collapsed: a, onExecute: l });
    case "header":
      return /* @__PURE__ */ e.createElement(Oe, { item: t, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement($e, null);
    case "group":
      return /* @__PURE__ */ e.createElement(
        Ve,
        {
          item: t,
          activeItemId: n,
          collapsed: a,
          onSelect: o,
          onExecute: l,
          onToggleGroup: r
        }
      );
    default:
      return null;
  }
}, He = () => {
  const t = k(), n = j(), a = t.items ?? [], o = t.activeItemId, l = t.collapsed, r = U((i) => {
    i !== o && n("selectItem", { itemId: i });
  }, [n, o]), s = U((i) => {
    n("executeCommand", { itemId: i });
  }, [n]), c = U(() => {
    n("toggleCollapse", {});
  }, [n]), u = U((i, v) => {
    n("toggleGroup", { itemId: i, expanded: v });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav" }, !l && t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items" }, a.map((i) => /* @__PURE__ */ e.createElement(
    G,
    {
      key: i.id,
      item: i,
      activeItemId: o,
      collapsed: l,
      onSelect: r,
      onExecute: s,
      onToggleGroup: u
    }
  ))), !l && t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: c,
      title: l ? "Expand sidebar" : "Collapse sidebar"
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
_("TLButton", ie);
_("TLToggleButton", de);
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
_("TLPhotoViewer", Re);
_("TLSplitPanel", Se);
_("TLPanel", Ie);
_("TLMaximizeRoot", Be);
_("TLDeckPane", ze);
_("TLSidebar", He);
