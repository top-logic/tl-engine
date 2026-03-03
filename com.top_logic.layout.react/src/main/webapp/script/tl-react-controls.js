import { React as e, useTLFieldValue as $, getComponent as ae, useTLState as w, useTLCommand as D, TLChild as L, useTLUpload as Y, useI18N as j, useTLDataUrl as G, register as k } from "tl-react-bridge";
const { useCallback: ne } = e, le = ({ state: t }) => {
  const [r, l] = $(), o = ne(
    (n) => {
      l(n.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: oe } = e, re = ({ state: t, config: r }) => {
  const [l, o] = $(), n = oe(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      o(i);
    },
    [o]
  ), a = r != null && r.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: n,
      step: a,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: se } = e, ce = ({ state: t }) => {
  const [r, l] = $(), o = se(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: ie } = e, de = ({ state: t, config: r }) => {
  var c;
  const [l, o] = $(), n = ie(
    (s) => {
      o(s.target.value || null);
    },
    [o]
  ), a = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const s = ((c = a.find((i) => i.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: ue } = e, me = ({ state: t }) => {
  const [r, l] = $(), o = ue(
    (n) => {
      l(n.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: r === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: r === !0,
      onChange: o,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, pe = ({ controlId: t, state: r }) => {
  const l = r.columns ?? [], o = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((n, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, l.map((c) => {
    const s = c.cellModule ? ae(c.cellModule) : void 0, i = n[c.name];
    if (s) {
      const p = { value: i, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: t + "-" + a + "-" + c.name,
          state: p
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: be } = e, fe = ({ command: t, label: r, disabled: l }) => {
  const o = w(), n = D(), a = t ?? "click", c = r ?? o.label, s = l ?? o.disabled === !0, i = be(() => {
    n(a);
  }, [n, a]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: s,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: he } = e, ve = ({ command: t, label: r, active: l, disabled: o }) => {
  const n = w(), a = D(), c = t ?? "click", s = r ?? n.label, i = l ?? n.active === !0, p = o ?? n.disabled === !0, h = he(() => {
    a(c);
  }, [a, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: h,
      disabled: p,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    s
  );
}, _e = () => {
  const t = w(), r = D(), l = t.count ?? 0, o = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ee } = e, ge = () => {
  const t = w(), r = D(), l = t.tabs ?? [], o = t.activeTabId, n = Ee((a) => {
    a !== o && r("selectTab", { tabId: a });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === o,
      className: "tlReactTabBar__tab" + (a.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => n(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(L, { control: t.activeContent })));
}, Ce = () => {
  const t = w(), r = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((o, n) => /* @__PURE__ */ e.createElement("div", { key: n, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(L, { control: o })))));
}, ye = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ke = () => {
  const t = w(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), p = t.status ?? "idle", h = t.error, b = p === "received" ? "idle" : l !== "idle" ? l : p, N = e.useCallback(async () => {
    if (l === "recording") {
      const f = c.current;
      f && f.state !== "inactive" && f.stop();
      return;
    }
    if (l !== "uploading") {
      if (a(null), !window.isSecureContext || !navigator.mediaDevices) {
        a("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const f = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = f, s.current = [];
        const T = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", B = new MediaRecorder(f, T ? { mimeType: T } : void 0);
        c.current = B, B.ondataavailable = (E) => {
          E.data.size > 0 && s.current.push(E.data);
        }, B.onstop = async () => {
          f.getTracks().forEach((R) => R.stop()), i.current = null;
          const E = new Blob(s.current, { type: B.mimeType || "audio/webm" });
          if (s.current = [], E.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const S = new FormData();
          S.append("audio", E, "recording.webm"), await r(S), o("idle");
        }, B.start(), o("recording");
      } catch (f) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", f), a("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [l, r]), v = j(ye), C = b === "recording" ? v["js.audioRecorder.stop"] : b === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], m = b === "uploading", _ = ["tlAudioRecorder__button"];
  return b === "recording" && _.push("tlAudioRecorder__button--recording"), b === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: N,
      disabled: m,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[n]), h && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h));
}, Ne = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, we = () => {
  const t = w(), r = G(), l = !!t.hasAudio, o = t.dataRevision ?? 0, [n, a] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(o);
  e.useEffect(() => {
    l ? n === "disabled" && a("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), a("disabled"));
  }, [l]), e.useEffect(() => {
    o !== i.current && (i.current = o, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (n === "playing" || n === "paused" || n === "loading") && a("idle"));
  }, [o]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (n === "disabled" || n === "loading")
      return;
    if (n === "playing") {
      c.current && c.current.pause(), a("paused");
      return;
    }
    if (n === "paused" && c.current) {
      c.current.play(), a("playing");
      return;
    }
    if (!s.current) {
      a("loading");
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", m.status), a("idle");
          return;
        }
        const _ = await m.blob();
        s.current = URL.createObjectURL(_);
      } catch (m) {
        console.error("[TLAudioPlayer] Fetch error:", m), a("idle");
        return;
      }
    }
    const C = new Audio(s.current);
    c.current = C, C.onended = () => {
      a("idle");
    }, C.play(), a("playing");
  }, [n, r]), h = j(Ne), b = n === "loading" ? h["js.loading"] : n === "playing" ? h["js.audioPlayer.pause"] : n === "disabled" ? h["js.audioPlayer.noAudio"] : h["js.audioPlayer.play"], N = n === "disabled" || n === "loading", v = ["tlAudioPlayer__button"];
  return n === "playing" && v.push("tlAudioPlayer__button--playing"), n === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: p,
      disabled: N,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${n === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Se = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Le = () => {
  const t = w(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(!1), c = e.useRef(null), s = t.status ?? "idle", i = t.error, p = t.accept ?? "", h = s === "received" ? "idle" : l !== "idle" ? l : s, b = e.useCallback(async (E) => {
    o("uploading");
    const S = new FormData();
    S.append("file", E, E.name), await r(S), o("idle");
  }, [r]), N = e.useCallback((E) => {
    var R;
    const S = (R = E.target.files) == null ? void 0 : R[0];
    S && b(S);
  }, [b]), v = e.useCallback(() => {
    var E;
    l !== "uploading" && ((E = c.current) == null || E.click());
  }, [l]), C = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), a(!0);
  }, []), m = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), a(!1);
  }, []), _ = e.useCallback((E) => {
    var R;
    if (E.preventDefault(), E.stopPropagation(), a(!1), l === "uploading") return;
    const S = (R = E.dataTransfer.files) == null ? void 0 : R[0];
    S && b(S);
  }, [l, b]), f = h === "uploading", T = j(Se), B = h === "uploading" ? T["js.uploading"] : T["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${n ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: m,
      onDrop: _
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: p || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (h === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: f,
        title: B,
        "aria-label": B
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Re = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Te = () => {
  const t = w(), r = G(), l = D(), o = !!t.hasData, n = t.dataRevision ?? 0, a = t.fileName ?? "download", c = !!t.clearable, [s, i] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!o || s)) {
      i(!0);
      try {
        const v = r + (r.includes("?") ? "&" : "?") + "rev=" + n, C = await fetch(v);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const m = await C.blob(), _ = URL.createObjectURL(m), f = document.createElement("a");
        f.href = _, f.download = a, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(_);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        i(!1);
      }
    }
  }, [o, s, r, n, a]), h = e.useCallback(async () => {
    o && await l("clear");
  }, [o, l]), b = j(Re);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const N = s ? b["js.downloading"] : b["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: s,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: h,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, xe = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Be = () => {
  const t = w(), r = Y(), [l, o] = e.useState("idle"), [n, a] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), p = e.useRef(null), h = e.useRef(null), b = e.useRef(null), N = e.useRef(null), v = t.error, C = e.useMemo(
    () => {
      var d;
      return !!(window.isSecureContext && ((d = navigator.mediaDevices) != null && d.getUserMedia));
    },
    []
  ), m = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null), i.current && (i.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    m(), o("idle");
  }, [m]), f = e.useCallback(async () => {
    var d;
    if (l !== "uploading") {
      if (a(null), !C) {
        (d = b.current) == null || d.click();
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = y, o("overlayOpen");
      } catch (y) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", y), a("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [l, C]), T = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const d = i.current, y = h.current;
    if (!d || !y)
      return;
    y.width = d.videoWidth, y.height = d.videoHeight;
    const g = y.getContext("2d");
    g && (g.drawImage(d, 0, 0), m(), o("uploading"), y.toBlob(async (x) => {
      if (!x) {
        o("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", x, "capture.jpg"), await r(I), o("idle");
    }, "image/jpeg", 0.85));
  }, [l, r, m]), B = e.useCallback(async (d) => {
    var x;
    const y = (x = d.target.files) == null ? void 0 : x[0];
    if (!y) return;
    o("uploading");
    const g = new FormData();
    g.append("photo", y, y.name), await r(g), o("idle"), b.current && (b.current.value = "");
  }, [r]);
  e.useEffect(() => {
    l === "overlayOpen" && i.current && p.current && (i.current.srcObject = p.current);
  }, [l]), e.useEffect(() => {
    var y;
    if (l !== "overlayOpen") return;
    (y = N.current) == null || y.focus();
    const d = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = d;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const d = (y) => {
      y.key === "Escape" && _();
    };
    return document.addEventListener("keydown", d), () => document.removeEventListener("keydown", d);
  }, [l, _]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((d) => d.stop()), p.current = null);
  }, []);
  const E = j(xe), S = l === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], R = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && R.push("tlPhotoCapture__cameraBtn--uploading");
  const M = ["tlPhotoCapture__overlayVideo"];
  c && M.push("tlPhotoCapture__overlayVideo--mirrored");
  const u = ["tlPhotoCapture__mirrorBtn"];
  return c && u.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: f,
      disabled: l === "uploading",
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: b,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: B
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: h, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: M.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: u.join(" "),
        onClick: () => s((d) => !d),
        title: E["js.photoCapture.mirror"],
        "aria-label": E["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: T,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[n]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, Pe = {
  "js.photoViewer.alt": "Captured photo"
}, De = () => {
  const t = w(), r = G(), l = !!t.hasPhoto, o = t.dataRevision ?? 0, [n, a] = e.useState(null), c = e.useRef(o);
  e.useEffect(() => {
    if (!l) {
      n && (URL.revokeObjectURL(n), a(null));
      return;
    }
    if (o === c.current && n)
      return;
    c.current = o, n && (URL.revokeObjectURL(n), a(null));
    let i = !1;
    return (async () => {
      try {
        const p = await fetch(r);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const h = await p.blob();
        i || a(URL.createObjectURL(h));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      i = !0;
    };
  }, [l, o, r]), e.useEffect(() => () => {
    n && URL.revokeObjectURL(n);
  }, []);
  const s = j(Pe);
  return !l || !n ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: n,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: J, useRef: O } = e, je = () => {
  const t = w(), r = D(), l = t.orientation, o = t.resizable === !0, n = t.children ?? [], a = l === "horizontal", c = n.length > 0 && n.every((m) => m.collapsed), s = !c && n.some((m) => m.collapsed), i = c ? !a : a, p = O(null), h = O(null), b = O(null), N = J((m, _) => {
    const f = {
      overflow: m.scrolling || "auto"
    };
    return m.collapsed ? c && !i ? f.flex = "1 0 0%" : f.flex = "0 0 auto" : _ !== void 0 ? f.flex = `0 0 ${_}px` : m.unit === "%" || s ? f.flex = `${m.size} 0 0%` : f.flex = `0 0 ${m.size}px`, m.minSize > 0 && !m.collapsed && (f.minWidth = a ? m.minSize : void 0, f.minHeight = a ? void 0 : m.minSize), f;
  }, [a, c, s, i]), v = J((m, _) => {
    m.preventDefault();
    const f = p.current;
    if (!f) return;
    const T = n[_], B = n[_ + 1], E = f.querySelectorAll(":scope > .tlSplitPanel__child"), S = [];
    E.forEach((u) => {
      S.push(a ? u.offsetWidth : u.offsetHeight);
    }), b.current = S, h.current = {
      splitterIndex: _,
      startPos: a ? m.clientX : m.clientY,
      startSizeBefore: S[_],
      startSizeAfter: S[_ + 1],
      childBefore: T,
      childAfter: B
    };
    const R = (u) => {
      const d = h.current;
      if (!d || !b.current) return;
      const g = (a ? u.clientX : u.clientY) - d.startPos, x = d.childBefore.minSize || 0, I = d.childAfter.minSize || 0;
      let A = d.startSizeBefore + g, z = d.startSizeAfter - g;
      A < x && (z += A - x, A = x), z < I && (A += z - I, z = I), b.current[d.splitterIndex] = A, b.current[d.splitterIndex + 1] = z;
      const q = f.querySelectorAll(":scope > .tlSplitPanel__child"), X = q[d.splitterIndex], Z = q[d.splitterIndex + 1];
      X && (X.style.flex = `0 0 ${A}px`), Z && (Z.style.flex = `0 0 ${z}px`);
    }, M = () => {
      if (document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", M), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const u = {};
        n.forEach((d, y) => {
          const g = d.control;
          g != null && g.controlId && b.current && (u[g.controlId] = b.current[y]);
        }), r("updateSizes", { sizes: u });
      }
      b.current = null, h.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", M), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [n, a, r]), C = [];
  return n.forEach((m, _) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${m.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(m)
        },
        /* @__PURE__ */ e.createElement(L, { control: m.control })
      )
    ), o && _ < n.length - 1) {
      const f = n[_ + 1];
      !m.collapsed && !f.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (B) => v(B, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      className: `tlSplitPanel tlSplitPanel--${l}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    C
  );
}, { useCallback: V } = e, Me = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Ie = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Ae = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ue = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), $e = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Fe = () => {
  const t = w(), r = D(), l = j(Me), o = t.title, n = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, c = t.showMaximize === !0, s = t.showPopOut === !0, i = t.toolbarButtons ?? [], p = n === "MINIMIZED", h = n === "MAXIMIZED", b = n === "HIDDEN", N = V(() => {
    r("toggleMinimize");
  }, [r]), v = V(() => {
    r("toggleMaximize");
  }, [r]), C = V(() => {
    r("popOut");
  }, [r]);
  if (b)
    return null;
  const m = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${n.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((_, f) => /* @__PURE__ */ e.createElement("span", { key: f, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(L, { control: _ }))), a && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Ae, null) : /* @__PURE__ */ e.createElement(Ie, null)
    ), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: h ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(Ue, null) : /* @__PURE__ */ e.createElement(ze, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement($e, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(L, { control: t.child }))
  );
}, Oe = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(L, { control: t.child })
  );
}, Ve = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(L, { control: t.activeChild }));
}, { useCallback: P, useState: W, useEffect: F, useRef: H } = e, We = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function K(t, r, l, o) {
  const n = [];
  for (const a of t)
    a.type === "nav" ? n.push({ id: a.id, type: "nav", groupId: o }) : a.type === "command" ? n.push({ id: a.id, type: "command", groupId: o }) : a.type === "group" && (n.push({ id: a.id, type: "group" }), (l.get(a.id) ?? a.expanded) && !r && n.push(...K(a.children, r, l, a.id)));
  return n;
}
const U = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, He = ({ item: t, active: r, collapsed: l, onSelect: o, tabIndex: n, itemRef: a, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (r ? " tlSidebar__navItem--active" : ""),
    onClick: () => o(t.id),
    title: l ? t.label : void 0,
    tabIndex: n,
    ref: a,
    onFocus: () => c(t.id)
  },
  l && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(U, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !l && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), Ke = ({ item: t, collapsed: r, onExecute: l, tabIndex: o, itemRef: n, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(t.id),
    title: r ? t.label : void 0,
    tabIndex: o,
    ref: n,
    onFocus: () => a(t.id)
  },
  /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Ye = ({ item: t, collapsed: r }) => r && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: r ? t.label : void 0 }, /* @__PURE__ */ e.createElement(U, { icon: t.icon }), !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), Ge = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), qe = ({ item: t, activeItemId: r, onSelect: l, onExecute: o, onClose: n }) => {
  const a = H(null);
  F(() => {
    const s = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => n(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [n]), F(() => {
    const s = (i) => {
      i.key === "Escape" && n();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
  }, [n]);
  const c = P((s) => {
    s.type === "nav" ? (l(s.id), n()) : s.type === "command" && (o(s.id), n());
  }, [l, o, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, t.label), t.children.map((s) => {
    if (s.type === "nav" || s.type === "command") {
      const i = s.type === "nav" && s.id === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => c(s)
        },
        /* @__PURE__ */ e.createElement(U, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, Xe = ({
  item: t,
  expanded: r,
  activeItemId: l,
  collapsed: o,
  onSelect: n,
  onExecute: a,
  onToggleGroup: c,
  tabIndex: s,
  itemRef: i,
  onFocus: p,
  focusedId: h,
  setItemRef: b,
  onItemFocus: N,
  flyoutGroupId: v,
  onOpenFlyout: C,
  onCloseFlyout: m
}) => {
  const _ = P(() => {
    o ? v === t.id ? m() : C(t.id) : c(t.id);
  }, [o, v, t.id, c, C, m]), f = o && v === t.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: _,
      title: o ? t.label : void 0,
      "aria-expanded": o ? f : r,
      tabIndex: s,
      ref: i,
      onFocus: () => p(t.id)
    },
    /* @__PURE__ */ e.createElement(U, { icon: t.icon }),
    !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
    !o && /* @__PURE__ */ e.createElement(
      "svg",
      {
        className: "tlSidebar__chevron" + (r ? " tlSidebar__chevron--open" : ""),
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
  ), f && /* @__PURE__ */ e.createElement(
    qe,
    {
      item: t,
      activeItemId: l,
      onSelect: n,
      onExecute: a,
      onClose: m
    }
  ), r && !o && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((T) => /* @__PURE__ */ e.createElement(
    te,
    {
      key: T.id,
      item: T,
      activeItemId: l,
      collapsed: o,
      onSelect: n,
      onExecute: a,
      onToggleGroup: c,
      focusedId: h,
      setItemRef: b,
      onItemFocus: N,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))));
}, te = ({
  item: t,
  activeItemId: r,
  collapsed: l,
  onSelect: o,
  onExecute: n,
  onToggleGroup: a,
  focusedId: c,
  setItemRef: s,
  onItemFocus: i,
  groupStates: p,
  flyoutGroupId: h,
  onOpenFlyout: b,
  onCloseFlyout: N
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        He,
        {
          item: t,
          active: t.id === r,
          collapsed: l,
          onSelect: o,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ke,
        {
          item: t,
          collapsed: l,
          onExecute: n,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ye, { item: t, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ge, null);
    case "group": {
      const v = p ? p.get(t.id) ?? t.expanded : t.expanded;
      return /* @__PURE__ */ e.createElement(
        Xe,
        {
          item: t,
          expanded: v,
          activeItemId: r,
          collapsed: l,
          onSelect: o,
          onExecute: n,
          onToggleGroup: a,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i,
          focusedId: c,
          setItemRef: s,
          onItemFocus: i,
          flyoutGroupId: h,
          onOpenFlyout: b,
          onCloseFlyout: N
        }
      );
    }
    default:
      return null;
  }
}, Ze = () => {
  const t = w(), r = D(), l = j(We), o = t.items ?? [], n = t.activeItemId, a = t.collapsed, [c, s] = W(() => {
    const u = /* @__PURE__ */ new Map(), d = (y) => {
      for (const g of y)
        g.type === "group" && (u.set(g.id, g.expanded), d(g.children));
    };
    return d(o), u;
  }), i = P((u) => {
    s((d) => {
      const y = new Map(d), g = y.get(u) ?? !1;
      return y.set(u, !g), r("toggleGroup", { itemId: u, expanded: !g }), y;
    });
  }, [r]), p = P((u) => {
    u !== n && r("selectItem", { itemId: u });
  }, [r, n]), h = P((u) => {
    r("executeCommand", { itemId: u });
  }, [r]), b = P(() => {
    r("toggleCollapse", {});
  }, [r]), [N, v] = W(null), C = P((u) => {
    v(u);
  }, []), m = P(() => {
    v(null);
  }, []);
  F(() => {
    a || v(null);
  }, [a]);
  const [_, f] = W(() => {
    const u = K(o, a, c);
    return u.length > 0 ? u[0].id : "";
  }), T = H(/* @__PURE__ */ new Map()), B = P((u) => (d) => {
    d ? T.current.set(u, d) : T.current.delete(u);
  }, []), E = P((u) => {
    f(u);
  }, []), S = H(0), R = P((u) => {
    f(u), S.current++;
  }, []);
  F(() => {
    const u = T.current.get(_);
    u && document.activeElement !== u && u.focus();
  }, [_, S.current]);
  const M = P((u) => {
    if (u.key === "Escape" && N !== null) {
      u.preventDefault(), m();
      return;
    }
    const d = K(o, a, c);
    if (d.length === 0) return;
    const y = d.findIndex((x) => x.id === _);
    if (y < 0) return;
    const g = d[y];
    switch (u.key) {
      case "ArrowDown": {
        u.preventDefault();
        const x = (y + 1) % d.length;
        R(d[x].id);
        break;
      }
      case "ArrowUp": {
        u.preventDefault();
        const x = (y - 1 + d.length) % d.length;
        R(d[x].id);
        break;
      }
      case "Home": {
        u.preventDefault(), R(d[0].id);
        break;
      }
      case "End": {
        u.preventDefault(), R(d[d.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        u.preventDefault(), g.type === "nav" ? p(g.id) : g.type === "command" ? h(g.id) : g.type === "group" && (a ? N === g.id ? m() : C(g.id) : i(g.id));
        break;
      }
      case "ArrowRight": {
        g.type === "group" && !a && ((c.get(g.id) ?? !1) || (u.preventDefault(), i(g.id)));
        break;
      }
      case "ArrowLeft": {
        g.type === "group" && !a && (c.get(g.id) ?? !1) && (u.preventDefault(), i(g.id));
        break;
      }
    }
  }, [
    o,
    a,
    c,
    _,
    N,
    R,
    p,
    h,
    i,
    C,
    m
  ]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: M }, o.map((u) => /* @__PURE__ */ e.createElement(
    te,
    {
      key: u.id,
      item: u,
      activeItemId: n,
      collapsed: a,
      onSelect: p,
      onExecute: h,
      onToggleGroup: i,
      focusedId: _,
      setItemRef: B,
      onItemFocus: E,
      groupStates: c,
      flyoutGroupId: N,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
      title: a ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: a ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(L, { control: t.activeContent })));
}, Je = () => {
  const t = w(), r = t.direction ?? "column", l = t.gap ?? "default", o = t.align ?? "stretch", n = t.wrap === !0, a = t.children ?? [], c = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${o}`,
    n ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { className: c }, a.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s })));
}, Qe = () => {
  const t = w(), r = t.columns, l = t.minColumnWidth, o = t.gap ?? "default", n = t.children ?? [], a = {};
  return l ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : r && (a.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { className: `tlGrid tlGrid--gap-${o}`, style: a }, n.map((c, s) => /* @__PURE__ */ e.createElement(L, { key: s, control: c })));
}, et = () => {
  const t = w(), r = t.title, l = t.variant ?? "outlined", o = t.padding ?? "default", n = t.headerActions ?? [], a = t.child, c = r != null || n.length > 0;
  return /* @__PURE__ */ e.createElement("div", { className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, n.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${o}` }, /* @__PURE__ */ e.createElement(L, { control: a })));
}, tt = () => {
  const t = w(), r = t.title ?? "", l = t.leading, o = t.actions ?? [], n = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    n === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(L, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), o.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, o.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s }))));
}, { useCallback: at } = e, nt = () => {
  const t = w(), r = D(), l = t.items ?? [], o = at((n) => {
    r("navigate", { itemId: n });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((n, a) => {
    const c = a === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: n.id, className: "tlBreadcrumb__entry" }, a > 0 && /* @__PURE__ */ e.createElement(
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
        onClick: () => o(n.id)
      },
      n.label
    ));
  })));
}, { useCallback: lt } = e, ot = () => {
  const t = w(), r = D(), l = t.items ?? [], o = t.activeItemId, n = lt((a) => {
    a !== o && r("selectItem", { itemId: a });
  }, [r, o]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((a) => {
    const c = a.id === o;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: a.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => n(a.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + a.icon, "aria-hidden": "true" }), a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, a.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, a.label)
    );
  }));
}, { useCallback: Q, useEffect: ee, useRef: rt } = e, st = {
  "js.dialog.close": "Close"
}, ct = () => {
  const t = w(), r = D(), l = j(st), o = t.open === !0, n = t.title ?? "", a = t.size ?? "medium", c = t.closeOnBackdrop !== !1, s = t.actions ?? [], i = t.child, p = rt(null), h = Q(() => {
    r("close");
  }, [r]), b = Q((v) => {
    c && v.target === v.currentTarget && h();
  }, [c, h]);
  if (ee(() => {
    if (!o) return;
    const v = (C) => {
      C.key === "Escape" && h();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [o, h]), ee(() => {
    o && p.current && p.current.focus();
  }, [o]), !o) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${a}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, n), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: h,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(L, { control: i })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((v, C) => /* @__PURE__ */ e.createElement(L, { key: C, control: v })))
  ));
};
k("TLButton", fe);
k("TLToggleButton", ve);
k("TLTextInput", le);
k("TLNumberInput", re);
k("TLDatePicker", ce);
k("TLSelect", de);
k("TLCheckbox", me);
k("TLTable", pe);
k("TLCounter", _e);
k("TLTabBar", ge);
k("TLFieldList", Ce);
k("TLAudioRecorder", ke);
k("TLAudioPlayer", we);
k("TLFileUpload", Le);
k("TLDownload", Te);
k("TLPhotoCapture", Be);
k("TLPhotoViewer", De);
k("TLSplitPanel", je);
k("TLPanel", Fe);
k("TLMaximizeRoot", Oe);
k("TLDeckPane", Ve);
k("TLSidebar", Ze);
k("TLStack", Je);
k("TLGrid", Qe);
k("TLCard", et);
k("TLAppBar", tt);
k("TLBreadcrumb", nt);
k("TLBottomBar", ot);
k("TLDialog", ct);
