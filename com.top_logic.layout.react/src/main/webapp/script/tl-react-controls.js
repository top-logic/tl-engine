import { React as e, useTLFieldValue as X, getComponent as _e, useTLState as x, useTLCommand as M, TLChild as T, useTLUpload as re, useI18N as z, useTLDataUrl as se, register as L } from "tl-react-bridge";
const { useCallback: Ee } = e, ve = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = Ee(
    (a) => {
      n(a.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactTextInput tlReactTextInput--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: r,
      value: l ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: ge } = e, Ce = ({ controlId: r, state: t, config: l }) => {
  const [n, s] = X(), a = ge(
    (c) => {
      const i = c.target.value, d = i === "" ? null : Number(i);
      s(d);
    },
    [s]
  ), o = l != null && l.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: r,
      value: n != null ? String(n) : "",
      onChange: a,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ye } = e, ke = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = ye(
    (a) => {
      n(a.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactDatePicker tlReactDatePicker--immutable" }, l ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: r,
      value: l ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: we } = e, Ne = ({ controlId: r, state: t, config: l }) => {
  var c;
  const [n, s] = X(), a = we(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), o = t.options ?? (l == null ? void 0 : l.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = o.find((d) => d.value === n)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: r,
      value: n ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: Le } = e, Se = ({ controlId: r, state: t }) => {
  const [l, n] = X(), s = Le(
    (a) => {
      n(a.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: l === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: l === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, xe = ({ controlId: r, state: t }) => {
  const l = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: r, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, l.map((o) => {
    const c = o.cellModule ? _e(o.cellModule) : void 0, i = s[o.name];
    if (c) {
      const d = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: r + "-" + a + "-" + o.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Te } = e, Re = ({ controlId: r, command: t, label: l, disabled: n }) => {
  const s = x(), a = M(), o = t ?? "click", c = l ?? s.label, i = n ?? s.disabled === !0, d = Te(() => {
    a(o);
  }, [a, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: d,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: De } = e, Be = ({ controlId: r, command: t, label: l, active: n, disabled: s }) => {
  const a = x(), o = M(), c = t ?? "click", i = l ?? a.label, d = n ?? a.active === !0, p = s ?? a.disabled === !0, f = De(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: f,
      disabled: p,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    i
  );
}, Pe = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: je } = e, Fe = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.tabs ?? [], s = t.activeTabId, a = je((o) => {
    o !== s && l("selectTab", { tabId: o });
  }, [l, s]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === s,
      className: "tlReactTabBar__tab" + (o.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, Me = ({ controlId: r }) => {
  const t = x(), l = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((s, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(T, { control: s })))));
}, Ie = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ae = ({ controlId: r }) => {
  const t = x(), l = re(), [n, s] = e.useState("idle"), [a, o] = e.useState(null), c = e.useRef(null), i = e.useRef([]), d = e.useRef(null), p = t.status ?? "idle", f = t.error, b = p === "received" ? "idle" : n !== "idle" ? n : p, y = e.useCallback(async () => {
    if (n === "recording") {
      const h = c.current;
      h && h.state !== "inactive" && h.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const h = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        d.current = h, i.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(h, B ? { mimeType: B } : void 0);
        c.current = P, P.ondataavailable = (w) => {
          w.data.size > 0 && i.current.push(w.data);
        }, P.onstop = async () => {
          h.getTracks().forEach((j) => j.stop()), d.current = null;
          const w = new Blob(i.current, { type: P.mimeType || "audio/webm" });
          if (i.current = [], w.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const R = new FormData();
          R.append("audio", w, "recording.webm"), await l(R), s("idle");
        }, P.start(), s("recording");
      } catch (h) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", h), o("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [n, l]), g = z(Ie), m = b === "recording" ? g["js.audioRecorder.stop"] : b === "uploading" ? g["js.uploading"] : g["js.audioRecorder.record"], u = b === "uploading", E = ["tlAudioRecorder__button"];
  return b === "recording" && E.push("tlAudioRecorder__button--recording"), b === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: y,
      disabled: u,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, g[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f));
}, $e = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ze = ({ controlId: r }) => {
  const t = x(), l = se(), n = !!t.hasAudio, s = t.dataRevision ?? 0, [a, o] = e.useState(n ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), d = e.useRef(s);
  e.useEffect(() => {
    n ? a === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    s !== d.current && (d.current = s, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && o("idle"));
  }, [s]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      c.current && c.current.pause(), o("paused");
      return;
    }
    if (a === "paused" && c.current) {
      c.current.play(), o("playing");
      return;
    }
    if (!i.current) {
      o("loading");
      try {
        const u = await fetch(l);
        if (!u.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", u.status), o("idle");
          return;
        }
        const E = await u.blob();
        i.current = URL.createObjectURL(E);
      } catch (u) {
        console.error("[TLAudioPlayer] Fetch error:", u), o("idle");
        return;
      }
    }
    const m = new Audio(i.current);
    c.current = m, m.onended = () => {
      o("idle");
    }, m.play(), o("playing");
  }, [a, l]), f = z($e), b = a === "loading" ? f["js.loading"] : a === "playing" ? f["js.audioPlayer.pause"] : a === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], y = a === "disabled" || a === "loading", g = ["tlAudioPlayer__button"];
  return a === "playing" && g.push("tlAudioPlayer__button--playing"), a === "loading" && g.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: p,
      disabled: y,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Oe = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ue = ({ controlId: r }) => {
  const t = x(), l = re(), [n, s] = e.useState("idle"), [a, o] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", d = t.error, p = t.accept ?? "", f = i === "received" ? "idle" : n !== "idle" ? n : i, b = e.useCallback(async (w) => {
    s("uploading");
    const R = new FormData();
    R.append("file", w, w.name), await l(R), s("idle");
  }, [l]), y = e.useCallback((w) => {
    var j;
    const R = (j = w.target.files) == null ? void 0 : j[0];
    R && b(R);
  }, [b]), g = e.useCallback(() => {
    var w;
    n !== "uploading" && ((w = c.current) == null || w.click());
  }, [n]), m = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!0);
  }, []), u = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!1);
  }, []), E = e.useCallback((w) => {
    var j;
    if (w.preventDefault(), w.stopPropagation(), o(!1), n === "uploading") return;
    const R = (j = w.dataTransfer.files) == null ? void 0 : j[0];
    R && b(R);
  }, [n, b]), h = f === "uploading", B = z(Oe), P = f === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: m,
      onDragLeave: u,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: p || void 0,
        onChange: y,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: g,
        disabled: h,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
}, We = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ve = ({ controlId: r }) => {
  const t = x(), l = se(), n = M(), s = !!t.hasData, a = t.dataRevision ?? 0, o = t.fileName ?? "download", c = !!t.clearable, [i, d] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!s || i)) {
      d(!0);
      try {
        const g = l + (l.includes("?") ? "&" : "?") + "rev=" + a, m = await fetch(g);
        if (!m.ok) {
          console.error("[TLDownload] Failed to fetch data:", m.status);
          return;
        }
        const u = await m.blob(), E = URL.createObjectURL(u), h = document.createElement("a");
        h.href = E, h.download = o, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(E);
      } catch (g) {
        console.error("[TLDownload] Fetch error:", g);
      } finally {
        d(!1);
      }
    }
  }, [s, i, l, a, o]), f = e.useCallback(async () => {
    s && await n("clear");
  }, [s, n]), b = z(We);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const y = i ? b["js.downloading"] : b["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: i,
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
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
}, Ke = ({ controlId: r }) => {
  const t = x(), l = re(), [n, s] = e.useState("idle"), [a, o] = e.useState(null), [c, i] = e.useState(!1), d = e.useRef(null), p = e.useRef(null), f = e.useRef(null), b = e.useRef(null), y = e.useRef(null), g = t.error, m = e.useMemo(
    () => {
      var _;
      return !!(window.isSecureContext && ((_ = navigator.mediaDevices) != null && _.getUserMedia));
    },
    []
  ), u = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((_) => _.stop()), p.current = null), d.current && (d.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    u(), s("idle");
  }, [u]), h = e.useCallback(async () => {
    var _;
    if (n !== "uploading") {
      if (o(null), !m) {
        (_ = b.current) == null || _.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = S, s("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), o("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [n, m]), B = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const _ = d.current, S = f.current;
    if (!_ || !S)
      return;
    S.width = _.videoWidth, S.height = _.videoHeight;
    const N = S.getContext("2d");
    N && (N.drawImage(_, 0, 0), u(), s("uploading"), S.toBlob(async (F) => {
      if (!F) {
        s("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", F, "capture.jpg"), await l(H), s("idle");
    }, "image/jpeg", 0.85));
  }, [n, l, u]), P = e.useCallback(async (_) => {
    var F;
    const S = (F = _.target.files) == null ? void 0 : F[0];
    if (!S) return;
    s("uploading");
    const N = new FormData();
    N.append("photo", S, S.name), await l(N), s("idle"), b.current && (b.current.value = "");
  }, [l]);
  e.useEffect(() => {
    n === "overlayOpen" && d.current && p.current && (d.current.srcObject = p.current);
  }, [n]), e.useEffect(() => {
    var S;
    if (n !== "overlayOpen") return;
    (S = y.current) == null || S.focus();
    const _ = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = _;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const _ = (S) => {
      S.key === "Escape" && E();
    };
    return document.addEventListener("keydown", _), () => document.removeEventListener("keydown", _);
  }, [n, E]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((_) => _.stop()), p.current = null);
  }, []);
  const w = z(He), R = n === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], j = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && j.push("tlPhotoCapture__cameraBtn--uploading");
  const O = ["tlPhotoCapture__overlayVideo"];
  c && O.push("tlPhotoCapture__overlayVideo--mirrored");
  const C = ["tlPhotoCapture__mirrorBtn"];
  return c && C.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: j.join(" "),
      onClick: h,
      disabled: n === "uploading",
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !m && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: f, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: y,
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
        className: O.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: C.join(" "),
        onClick: () => i((_) => !_),
        title: w["js.photoCapture.mirror"],
        "aria-label": w["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: w["js.photoCapture.capture"],
        "aria-label": w["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: E,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[a]), g && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, g));
}, Ge = {
  "js.photoViewer.alt": "Captured photo"
}, Ye = ({ controlId: r }) => {
  const t = x(), l = se(), n = !!t.hasPhoto, s = t.dataRevision ?? 0, [a, o] = e.useState(null), c = e.useRef(s);
  e.useEffect(() => {
    if (!n) {
      a && (URL.revokeObjectURL(a), o(null));
      return;
    }
    if (s === c.current && a)
      return;
    c.current = s, a && (URL.revokeObjectURL(a), o(null));
    let d = !1;
    return (async () => {
      try {
        const p = await fetch(l);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const f = await p.blob();
        d || o(URL.createObjectURL(f));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      d = !0;
    };
  }, [n, s, l]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = z(Ge);
  return !n || !a ? /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ce, useRef: Q } = e, Xe = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.orientation, s = t.resizable === !0, a = t.children ?? [], o = n === "horizontal", c = a.length > 0 && a.every((u) => u.collapsed), i = !c && a.some((u) => u.collapsed), d = c ? !o : o, p = Q(null), f = Q(null), b = Q(null), y = ce((u, E) => {
    const h = {
      overflow: u.scrolling || "auto"
    };
    return u.collapsed ? c && !d ? h.flex = "1 0 0%" : h.flex = "0 0 auto" : E !== void 0 ? h.flex = `0 0 ${E}px` : u.unit === "%" || i ? h.flex = `${u.size} 0 0%` : h.flex = `0 0 ${u.size}px`, u.minSize > 0 && !u.collapsed && (h.minWidth = o ? u.minSize : void 0, h.minHeight = o ? void 0 : u.minSize), h;
  }, [o, c, i, d]), g = ce((u, E) => {
    u.preventDefault();
    const h = p.current;
    if (!h) return;
    const B = a[E], P = a[E + 1], w = h.querySelectorAll(":scope > .tlSplitPanel__child"), R = [];
    w.forEach((C) => {
      R.push(o ? C.offsetWidth : C.offsetHeight);
    }), b.current = R, f.current = {
      splitterIndex: E,
      startPos: o ? u.clientX : u.clientY,
      startSizeBefore: R[E],
      startSizeAfter: R[E + 1],
      childBefore: B,
      childAfter: P
    };
    const j = (C) => {
      const _ = f.current;
      if (!_ || !b.current) return;
      const N = (o ? C.clientX : C.clientY) - _.startPos, F = _.childBefore.minSize || 0, H = _.childAfter.minSize || 0;
      let U = _.startSizeBefore + N, K = _.startSizeAfter - N;
      U < F && (K += U - F, U = F), K < H && (U += K - H, K = H), b.current[_.splitterIndex] = U, b.current[_.splitterIndex + 1] = K;
      const Y = h.querySelectorAll(":scope > .tlSplitPanel__child"), q = Y[_.splitterIndex], v = Y[_.splitterIndex + 1];
      q && (q.style.flex = `0 0 ${U}px`), v && (v.style.flex = `0 0 ${K}px`);
    }, O = () => {
      if (document.removeEventListener("mousemove", j), document.removeEventListener("mouseup", O), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const C = {};
        a.forEach((_, S) => {
          const N = _.control;
          N != null && N.controlId && b.current && (C[N.controlId] = b.current[S]);
        }), l("updateSizes", { sizes: C });
      }
      b.current = null, f.current = null;
    };
    document.addEventListener("mousemove", j), document.addEventListener("mouseup", O), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, o, l]), m = [];
  return a.forEach((u, E) => {
    if (m.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${E}`,
          className: `tlSplitPanel__child${u.collapsed && d ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: y(u)
        },
        /* @__PURE__ */ e.createElement(T, { control: u.control })
      )
    ), s && E < a.length - 1) {
      const h = a[E + 1];
      !u.collapsed && !h.collapsed && m.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${E}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => g(P, E)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: r,
      className: `tlSplitPanel tlSplitPanel--${n}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: d ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    m
  );
}, { useCallback: ee } = e, qe = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Qe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), et = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), at = ({ controlId: r }) => {
  const t = x(), l = M(), n = z(qe), s = t.title, a = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, d = t.toolbarButtons ?? [], p = a === "MINIMIZED", f = a === "MAXIMIZED", b = a === "HIDDEN", y = ee(() => {
    l("toggleMinimize");
  }, [l]), g = ee(() => {
    l("toggleMaximize");
  }, [l]), m = ee(() => {
    l("popOut");
  }, [l]);
  if (b)
    return null;
  const u = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: u
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, d.map((E, h) => /* @__PURE__ */ e.createElement("span", { key: h, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(T, { control: E }))), o && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: p ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Je, null) : /* @__PURE__ */ e.createElement(Ze, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(et, null) : /* @__PURE__ */ e.createElement(Qe, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: m,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(tt, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(T, { control: t.child }))
  );
}, nt = ({ controlId: r }) => {
  const t = x();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(T, { control: t.child })
  );
}, lt = ({ controlId: r }) => {
  const t = x();
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(T, { control: t.activeChild }));
}, { useCallback: $, useState: te, useEffect: Z, useRef: le } = e, ot = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function oe(r, t, l, n) {
  const s = [];
  for (const a of r)
    a.type === "nav" ? s.push({ id: a.id, type: "nav", groupId: n }) : a.type === "command" ? s.push({ id: a.id, type: "command", groupId: n }) : a.type === "group" && (s.push({ id: a.id, type: "group" }), (l.get(a.id) ?? a.expanded) && !t && s.push(...oe(a.children, t, l, a.id)));
  return s;
}
const G = ({ icon: r }) => r ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + r, "aria-hidden": "true" }) : null, rt = ({ item: r, active: t, collapsed: l, onSelect: n, tabIndex: s, itemRef: a, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(r.id),
    title: l ? r.label : void 0,
    tabIndex: s,
    ref: a,
    onFocus: () => o(r.id)
  },
  l && r.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(G, { icon: r.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, r.badge)) : /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
  !l && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
), st = ({ item: r, collapsed: t, onExecute: l, tabIndex: n, itemRef: s, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(r.id),
    title: t ? r.label : void 0,
    tabIndex: n,
    ref: s,
    onFocus: () => a(r.id)
  },
  /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)
), ct = ({ item: r, collapsed: t }) => t && !r.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? r.label : void 0 }, /* @__PURE__ */ e.createElement(G, { icon: r.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)), it = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), dt = ({ item: r, activeItemId: t, onSelect: l, onExecute: n, onClose: s }) => {
  const a = le(null);
  Z(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), Z(() => {
    const c = (i) => {
      i.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const o = $((c) => {
    c.type === "nav" ? (l(c.id), s()) : c.type === "command" && (n(c.id), s());
  }, [l, n, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, r.label), r.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const i = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => o(c)
        },
        /* @__PURE__ */ e.createElement(G, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, ut = ({
  item: r,
  expanded: t,
  activeItemId: l,
  collapsed: n,
  onSelect: s,
  onExecute: a,
  onToggleGroup: o,
  tabIndex: c,
  itemRef: i,
  onFocus: d,
  focusedId: p,
  setItemRef: f,
  onItemFocus: b,
  flyoutGroupId: y,
  onOpenFlyout: g,
  onCloseFlyout: m
}) => {
  const u = $(() => {
    n ? y === r.id ? m() : g(r.id) : o(r.id);
  }, [n, y, r.id, o, g, m]), E = n && y === r.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: u,
      title: n ? r.label : void 0,
      "aria-expanded": n ? E : t,
      tabIndex: c,
      ref: i,
      onFocus: () => d(r.id)
    },
    /* @__PURE__ */ e.createElement(G, { icon: r.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
    !n && /* @__PURE__ */ e.createElement(
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
  ), E && /* @__PURE__ */ e.createElement(
    dt,
    {
      item: r,
      activeItemId: l,
      onSelect: s,
      onExecute: a,
      onClose: m
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, r.children.map((h) => /* @__PURE__ */ e.createElement(
    fe,
    {
      key: h.id,
      item: h,
      activeItemId: l,
      collapsed: n,
      onSelect: s,
      onExecute: a,
      onToggleGroup: o,
      focusedId: p,
      setItemRef: f,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: y,
      onOpenFlyout: g,
      onCloseFlyout: m
    }
  ))));
}, fe = ({
  item: r,
  activeItemId: t,
  collapsed: l,
  onSelect: n,
  onExecute: s,
  onToggleGroup: a,
  focusedId: o,
  setItemRef: c,
  onItemFocus: i,
  groupStates: d,
  flyoutGroupId: p,
  onOpenFlyout: f,
  onCloseFlyout: b
}) => {
  switch (r.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        rt,
        {
          item: r,
          active: r.id === t,
          collapsed: l,
          onSelect: n,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        st,
        {
          item: r,
          collapsed: l,
          onExecute: s,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(ct, { item: r, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(it, null);
    case "group": {
      const y = d ? d.get(r.id) ?? r.expanded : r.expanded;
      return /* @__PURE__ */ e.createElement(
        ut,
        {
          item: r,
          expanded: y,
          activeItemId: t,
          collapsed: l,
          onSelect: n,
          onExecute: s,
          onToggleGroup: a,
          tabIndex: o === r.id ? 0 : -1,
          itemRef: c(r.id),
          onFocus: i,
          focusedId: o,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: p,
          onOpenFlyout: f,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, mt = ({ controlId: r }) => {
  const t = x(), l = M(), n = z(ot), s = t.items ?? [], a = t.activeItemId, o = t.collapsed, [c, i] = te(() => {
    const C = /* @__PURE__ */ new Map(), _ = (S) => {
      for (const N of S)
        N.type === "group" && (C.set(N.id, N.expanded), _(N.children));
    };
    return _(s), C;
  }), d = $((C) => {
    i((_) => {
      const S = new Map(_), N = S.get(C) ?? !1;
      return S.set(C, !N), l("toggleGroup", { itemId: C, expanded: !N }), S;
    });
  }, [l]), p = $((C) => {
    C !== a && l("selectItem", { itemId: C });
  }, [l, a]), f = $((C) => {
    l("executeCommand", { itemId: C });
  }, [l]), b = $(() => {
    l("toggleCollapse", {});
  }, [l]), [y, g] = te(null), m = $((C) => {
    g(C);
  }, []), u = $(() => {
    g(null);
  }, []);
  Z(() => {
    o || g(null);
  }, [o]);
  const [E, h] = te(() => {
    const C = oe(s, o, c);
    return C.length > 0 ? C[0].id : "";
  }), B = le(/* @__PURE__ */ new Map()), P = $((C) => (_) => {
    _ ? B.current.set(C, _) : B.current.delete(C);
  }, []), w = $((C) => {
    h(C);
  }, []), R = le(0), j = $((C) => {
    h(C), R.current++;
  }, []);
  Z(() => {
    const C = B.current.get(E);
    C && document.activeElement !== C && C.focus();
  }, [E, R.current]);
  const O = $((C) => {
    if (C.key === "Escape" && y !== null) {
      C.preventDefault(), u();
      return;
    }
    const _ = oe(s, o, c);
    if (_.length === 0) return;
    const S = _.findIndex((F) => F.id === E);
    if (S < 0) return;
    const N = _[S];
    switch (C.key) {
      case "ArrowDown": {
        C.preventDefault();
        const F = (S + 1) % _.length;
        j(_[F].id);
        break;
      }
      case "ArrowUp": {
        C.preventDefault();
        const F = (S - 1 + _.length) % _.length;
        j(_[F].id);
        break;
      }
      case "Home": {
        C.preventDefault(), j(_[0].id);
        break;
      }
      case "End": {
        C.preventDefault(), j(_[_.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        C.preventDefault(), N.type === "nav" ? p(N.id) : N.type === "command" ? f(N.id) : N.type === "group" && (o ? y === N.id ? u() : m(N.id) : d(N.id));
        break;
      }
      case "ArrowRight": {
        N.type === "group" && !o && ((c.get(N.id) ?? !1) || (C.preventDefault(), d(N.id)));
        break;
      }
      case "ArrowLeft": {
        N.type === "group" && !o && (c.get(N.id) ?? !1) && (C.preventDefault(), d(N.id));
        break;
      }
    }
  }, [
    s,
    o,
    c,
    E,
    y,
    j,
    p,
    f,
    d,
    m,
    u
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: O }, s.map((C) => /* @__PURE__ */ e.createElement(
    fe,
    {
      key: C.id,
      item: C,
      activeItemId: a,
      collapsed: o,
      onSelect: p,
      onExecute: f,
      onToggleGroup: d,
      focusedId: E,
      setItemRef: P,
      onItemFocus: w,
      groupStates: c,
      flyoutGroupId: y,
      onOpenFlyout: m,
      onCloseFlyout: u
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: o ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, pt = ({ controlId: r }) => {
  const t = x(), l = t.direction ?? "column", n = t.gap ?? "default", s = t.align ?? "stretch", a = t.wrap === !0, o = t.children ?? [], c = [
    "tlStack",
    `tlStack--${l}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${s}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: c }, o.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i })));
}, ft = ({ controlId: r }) => {
  const t = x(), l = t.columns, n = t.minColumnWidth, s = t.gap ?? "default", a = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : l && (o.gridTemplateColumns = `repeat(${l}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: r, className: `tlGrid tlGrid--gap-${s}`, style: o }, a.map((c, i) => /* @__PURE__ */ e.createElement(T, { key: i, control: c })));
}, bt = ({ controlId: r }) => {
  const t = x(), l = t.title, n = t.variant ?? "outlined", s = t.padding ?? "default", a = t.headerActions ?? [], o = t.child, c = l != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: `tlCard tlCard--${n}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, l && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, l), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(T, { control: o })));
}, ht = ({ controlId: r }) => {
  const t = x(), l = t.title ?? "", n = t.leading, s = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: r, className: c }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(T, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, l), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: i }))));
}, { useCallback: _t } = e, Et = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.items ?? [], s = _t((a) => {
    l("navigate", { itemId: a });
  }, [l]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((a, o) => {
    const c = o === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => s(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: vt } = e, gt = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.items ?? [], s = t.activeItemId, a = vt((o) => {
    o !== s && l("selectItem", { itemId: o });
  }, [l, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
    const c = o.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => a(o.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: ie, useEffect: de, useRef: Ct } = e, yt = {
  "js.dialog.close": "Close"
}, kt = ({ controlId: r }) => {
  const t = x(), l = M(), n = z(yt), s = t.open === !0, a = t.title ?? "", o = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], d = t.child, p = Ct(null), f = ie(() => {
    l("close");
  }, [l]), b = ie((g) => {
    c && g.target === g.currentTarget && f();
  }, [c, f]);
  if (de(() => {
    if (!s) return;
    const g = (m) => {
      m.key === "Escape" && f();
    };
    return document.addEventListener("keydown", g), () => document.removeEventListener("keydown", g);
  }, [s, f]), de(() => {
    s && p.current && p.current.focus();
  }, [s]), !s) return null;
  const y = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": y,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: y }, a), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: f,
        title: n["js.dialog.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(T, { control: d })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((g, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: g })))
  ));
}, { useCallback: wt, useEffect: Nt } = e, Lt = {
  "js.drawer.close": "Close"
}, St = ({ controlId: r }) => {
  const t = x(), l = M(), n = z(Lt), s = t.open === !0, a = t.position ?? "right", o = t.size ?? "medium", c = t.title ?? null, i = t.child, d = wt(() => {
    l("close");
  }, [l]);
  Nt(() => {
    if (!s) return;
    const f = (b) => {
      b.key === "Escape" && d();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [s, d]);
  const p = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${o}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: r, className: p, "aria-hidden": !s }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: d,
      title: n["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(T, { control: i })));
}, { useCallback: ue, useEffect: xt, useState: Tt } = e, Rt = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.message ?? "", s = t.variant ?? "info", a = t.action, o = t.duration ?? 5e3, c = t.visible === !0, [i, d] = Tt(!1), p = ue(() => {
    d(!0), setTimeout(() => {
      l("dismiss"), d(!1);
    }, 200);
  }, [l]), f = ue(() => {
    a && l(a.commandName), p();
  }, [l, a, p]);
  return xt(() => {
    if (!c || o === 0) return;
    const b = setTimeout(p, o);
    return () => clearTimeout(b);
  }, [c, o, p]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: f }, a.label)
  );
}, { useCallback: ae, useEffect: ne, useRef: Dt, useState: me } = e, Bt = ({ controlId: r }) => {
  const t = x(), l = M(), n = t.open === !0, s = t.anchorId, a = t.items ?? [], o = Dt(null), [c, i] = me({ top: 0, left: 0 }), [d, p] = me(0), f = a.filter((m) => m.type === "item" && !m.disabled);
  ne(() => {
    var w, R;
    if (!n || !s) return;
    const m = document.getElementById(s);
    if (!m) return;
    const u = m.getBoundingClientRect(), E = ((w = o.current) == null ? void 0 : w.offsetHeight) ?? 200, h = ((R = o.current) == null ? void 0 : R.offsetWidth) ?? 200;
    let B = u.bottom + 4, P = u.left;
    B + E > window.innerHeight && (B = u.top - E - 4), P + h > window.innerWidth && (P = u.right - h), i({ top: B, left: P }), p(0);
  }, [n, s]);
  const b = ae(() => {
    l("close");
  }, [l]), y = ae((m) => {
    l("selectItem", { itemId: m });
  }, [l]);
  ne(() => {
    if (!n) return;
    const m = (u) => {
      o.current && !o.current.contains(u.target) && b();
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [n, b]);
  const g = ae((m) => {
    if (m.key === "Escape") {
      b();
      return;
    }
    if (m.key === "ArrowDown")
      m.preventDefault(), p((u) => (u + 1) % f.length);
    else if (m.key === "ArrowUp")
      m.preventDefault(), p((u) => (u - 1 + f.length) % f.length);
    else if (m.key === "Enter" || m.key === " ") {
      m.preventDefault();
      const u = f[d];
      u && y(u.id);
    }
  }, [b, y, f, d]);
  return ne(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: g
    },
    a.map((m, u) => {
      if (m.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: u, className: "tlMenu__separator" });
      const h = f.indexOf(m) === d;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: m.id,
          type: "button",
          className: "tlMenu__item" + (h ? " tlMenu__item--focused" : "") + (m.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: m.disabled,
          tabIndex: h ? 0 : -1,
          onClick: () => y(m.id)
        },
        m.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + m.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, m.label)
      );
    })
  ) : null;
}, Pt = ({ controlId: r }) => {
  const t = x(), l = t.header, n = t.content, s = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAppShell" }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(T, { control: l })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(T, { control: n })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(T, { control: s })), /* @__PURE__ */ e.createElement(T, { control: a }));
}, jt = () => {
  const t = x().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, pe = 50, Ft = () => {
  const r = x(), t = M(), l = r.columns ?? [], n = r.totalRowCount ?? 0, s = r.rows ?? [], a = r.rowHeight ?? 36, o = r.selectionMode ?? "single", c = r.selectedCount ?? 0, i = o === "multi", d = 40, p = e.useRef(null), f = e.useRef(null), [b, y] = e.useState({}), g = e.useRef(null), m = e.useRef(!1), u = e.useRef(null), [E, h] = e.useState(null);
  e.useEffect(() => {
    g.current || y({});
  }, [l]);
  const B = (v) => b[v.name] ?? v.width, P = n * a, w = e.useCallback((v, k, D) => {
    D.preventDefault(), D.stopPropagation(), g.current = { column: v, startX: D.clientX, startWidth: k };
    const I = (W) => {
      const V = g.current;
      if (!V) return;
      const J = Math.max(pe, V.startWidth + (W.clientX - V.startX));
      y((he) => ({ ...he, [V.column]: J }));
    }, A = (W) => {
      document.removeEventListener("mousemove", I), document.removeEventListener("mouseup", A);
      const V = g.current;
      if (V) {
        const J = Math.max(pe, V.startWidth + (W.clientX - V.startX));
        t("columnResize", { column: V.column, width: J }), g.current = null, m.current = !0, requestAnimationFrame(() => {
          m.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", I), document.addEventListener("mouseup", A);
  }, [t]), R = e.useCallback(() => {
    f.current !== null && clearTimeout(f.current), f.current = window.setTimeout(() => {
      const v = p.current;
      if (!v) return;
      const k = v.scrollTop, D = Math.ceil(v.clientHeight / a), I = Math.floor(k / a);
      t("scroll", { start: I, count: D });
    }, 80);
  }, [t, a]), j = e.useCallback((v, k) => {
    if (m.current) return;
    let D;
    !k || k === "desc" ? D = "asc" : D = "desc", t("sort", { column: v, direction: D });
  }, [t]), O = e.useCallback((v, k) => {
    u.current = v, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", v);
  }, []), C = e.useCallback((v, k) => {
    if (!u.current || u.current === v) {
      h(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const D = k.currentTarget.getBoundingClientRect(), I = k.clientX < D.left + D.width / 2 ? "left" : "right";
    h({ column: v, side: I });
  }, []), _ = e.useCallback((v) => {
    v.preventDefault();
    const k = u.current;
    if (!k || !E) {
      u.current = null, h(null);
      return;
    }
    let D = l.findIndex((A) => A.name === E.column);
    if (D < 0) {
      u.current = null, h(null);
      return;
    }
    const I = l.findIndex((A) => A.name === k);
    E.side === "right" && D++, I < D && D--, t("columnReorder", { column: k, targetIndex: D }), u.current = null, h(null);
  }, [l, E, t]), S = e.useCallback(() => {
    u.current = null, h(null);
  }, []), N = e.useCallback((v, k) => {
    k.shiftKey && k.preventDefault(), t("select", {
      rowIndex: v,
      ctrlKey: k.ctrlKey || k.metaKey,
      shiftKey: k.shiftKey
    });
  }, [t]), F = e.useCallback((v, k) => {
    k.stopPropagation(), t("select", { rowIndex: v, ctrlKey: !0, shiftKey: !1 });
  }, [t]), H = e.useCallback(() => {
    const v = c === n && n > 0;
    t("selectAll", { selected: !v });
  }, [t, c, n]), U = l.reduce((v, k) => v + B(k), 0) + (i ? d : 0), K = c === n && n > 0, Y = c > 0 && c < n, q = e.useCallback((v) => {
    v && (v.indeterminate = Y);
  }, [Y]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlTableView" }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", style: { minWidth: U } }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow" }, i && /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView__headerCell tlTableView__checkboxCell",
      style: { width: d, minWidth: d }
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        ref: q,
        className: "tlTableView__checkbox",
        checked: K,
        onChange: H
      }
    )
  ), l.map((v, k) => {
    const D = B(v), I = k === l.length - 1;
    let A = "tlTableView__headerCell";
    return v.sortable && (A += " tlTableView__headerCell--sortable"), E && E.column === v.name && (A += " tlTableView__headerCell--dragOver-" + E.side), /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.name,
        className: A,
        style: I ? { flex: "1 0 auto", minWidth: D, position: "relative" } : { width: D, minWidth: D, position: "relative" },
        draggable: !0,
        onClick: v.sortable ? () => j(v.name, v.sortDirection) : void 0,
        onDragStart: (W) => O(v.name, W),
        onDragOver: (W) => C(v.name, W),
        onDrop: _,
        onDragEnd: S
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, v.label),
      v.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, v.sortDirection === "asc" ? "▲" : "▼"),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__resizeHandle",
          onMouseDown: (W) => w(v.name, D, W)
        }
      )
    );
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      style: { flex: "0 0 0", minHeight: "100%" },
      onDragOver: (v) => {
        if (u.current && l.length > 0) {
          const k = l[l.length - 1];
          k.name !== u.current && (v.preventDefault(), v.dataTransfer.dropEffect = "move", h({ column: k.name, side: "right" }));
        }
      },
      onDrop: _
    }
  ))), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      className: "tlTableView__body",
      onScroll: R
    },
    /* @__PURE__ */ e.createElement("div", { style: { height: P, position: "relative" } }, s.map((v) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: v.id,
        className: "tlTableView__row" + (v.selected ? " tlTableView__row--selected" : ""),
        style: {
          position: "absolute",
          top: v.index * a,
          height: a,
          minWidth: U,
          width: "100%"
        },
        onClick: (k) => N(v.index, k)
      },
      i && /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__cell tlTableView__checkboxCell",
          style: { width: d, minWidth: d },
          onClick: (k) => k.stopPropagation()
        },
        /* @__PURE__ */ e.createElement(
          "input",
          {
            type: "checkbox",
            className: "tlTableView__checkbox",
            checked: v.selected,
            onChange: () => {
            },
            onClick: (k) => F(v.index, k),
            tabIndex: -1
          }
        )
      ),
      l.map((k, D) => {
        const I = B(k), A = D === l.length - 1;
        return /* @__PURE__ */ e.createElement(
          "div",
          {
            key: k.name,
            className: "tlTableView__cell",
            style: A ? { flex: "1 0 auto", minWidth: I } : { width: I, minWidth: I }
          },
          /* @__PURE__ */ e.createElement(T, { control: v.cells[k.name] })
        );
      })
    )))
  ));
}, Mt = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, be = e.createContext(Mt), { useMemo: It, useRef: At, useState: $t, useEffect: zt } = e, Ot = 320, Ut = ({ controlId: r }) => {
  const t = x(), l = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", s = t.readOnly === !0, a = t.children ?? [], o = At(null), [c, i] = $t(
    n === "top" ? "top" : "side"
  );
  zt(() => {
    if (n !== "auto") {
      i(n);
      return;
    }
    const y = o.current;
    if (!y) return;
    const g = new ResizeObserver((m) => {
      for (const u of m) {
        const h = u.contentRect.width / l;
        i(h < Ot ? "top" : "side");
      }
    });
    return g.observe(y), () => g.disconnect();
  }, [n, l]);
  const d = It(() => ({
    readOnly: s,
    resolvedLabelPosition: c
  }), [s, c]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / l))}rem`}, 1fr))`
  }, b = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(be.Provider, { value: d }, /* @__PURE__ */ e.createElement("div", { id: r, className: b, style: f, ref: o }, a.map((y, g) => /* @__PURE__ */ e.createElement(T, { key: g, control: y }))));
}, { useCallback: Wt } = e, Vt = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ht = ({ controlId: r }) => {
  const t = x(), l = M(), n = z(Vt), s = t.header, a = t.headerActions ?? [], o = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", d = t.fullLine === !0, p = t.children ?? [], f = s != null || a.length > 0 || o, b = Wt(() => {
    l("toggleCollapse");
  }, [l]), y = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    d ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: y }, f && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
      "aria-expanded": !c,
      title: c ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, a.map((g, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: g })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((g, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: g }))));
}, { useContext: Kt, useState: Gt, useCallback: Yt } = e, Xt = ({ controlId: r }) => {
  const t = x(), l = Kt(be), n = t.label ?? "", s = t.required === !0, a = t.error, o = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? l.resolvedLabelPosition, d = t.fullLine === !0, p = t.visible !== !1, f = t.field, b = l.readOnly, [y, g] = Gt(!1), m = Yt(() => g((h) => !h), []);
  if (!p) return null;
  const u = a != null, E = [
    "tlFormField",
    `tlFormField--${i}`,
    b ? "tlFormField--readonly" : "",
    d ? "tlFormField--fullLine" : "",
    u ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: E }, c && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__dirtyBar" }), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), s && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && !b && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: m,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(T, { control: f })), !b && u && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, a)), !b && o && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
};
L("TLButton", Re);
L("TLToggleButton", Be);
L("TLTextInput", ve);
L("TLNumberInput", Ce);
L("TLDatePicker", ke);
L("TLSelect", Ne);
L("TLCheckbox", Se);
L("TLTable", xe);
L("TLCounter", Pe);
L("TLTabBar", Fe);
L("TLFieldList", Me);
L("TLAudioRecorder", Ae);
L("TLAudioPlayer", ze);
L("TLFileUpload", Ue);
L("TLDownload", Ve);
L("TLPhotoCapture", Ke);
L("TLPhotoViewer", Ye);
L("TLSplitPanel", Xe);
L("TLPanel", at);
L("TLMaximizeRoot", nt);
L("TLDeckPane", lt);
L("TLSidebar", mt);
L("TLStack", pt);
L("TLGrid", ft);
L("TLCard", bt);
L("TLAppBar", ht);
L("TLBreadcrumb", Et);
L("TLBottomBar", gt);
L("TLDialog", kt);
L("TLDrawer", St);
L("TLSnackbar", Rt);
L("TLMenu", Bt);
L("TLAppShell", Pt);
L("TLTextCell", jt);
L("TLTableView", Ft);
L("TLFormLayout", Ut);
L("TLFormGroup", Ht);
L("TLFormField", Xt);
