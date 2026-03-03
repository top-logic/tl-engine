import { React as e, useTLFieldValue as U, getComponent as ne, useTLState as w, useTLCommand as B, TLChild as L, useTLUpload as Y, useI18N as P, useTLDataUrl as G, register as k } from "tl-react-bridge";
const { useCallback: le } = e, oe = ({ state: t }) => {
  const [o, l] = U(), r = le(
    (a) => {
      l(a.target.value);
    },
    [l]
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
}, { useCallback: re } = e, se = ({ state: t, config: o }) => {
  const [l, r] = U(), a = re(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      r(i);
    },
    [r]
  ), n = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: a,
      step: n,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ce } = e, ie = ({ state: t }) => {
  const [o, l] = U(), r = ce(
    (a) => {
      l(a.target.value || null);
    },
    [l]
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
}, { useCallback: de } = e, ue = ({ state: t, config: o }) => {
  var c;
  const [l, r] = U(), a = de(
    (s) => {
      r(s.target.value || null);
    },
    [r]
  ), n = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const s = ((c = n.find((i) => i.value === l)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    n.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: me } = e, pe = ({ state: t }) => {
  const [o, l] = U(), r = me(
    (a) => {
      l(a.target.checked);
    },
    [l]
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
}, be = ({ controlId: t, state: o }) => {
  const l = o.columns ?? [], r = o.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, l.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((a, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, l.map((c) => {
    const s = c.cellModule ? ne(c.cellModule) : void 0, i = a[c.name];
    if (s) {
      const d = { value: i, editable: o.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: t + "-" + n + "-" + c.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: fe } = e, he = ({ command: t, label: o, disabled: l }) => {
  const r = w(), a = B(), n = t ?? "click", c = o ?? r.label, s = l ?? r.disabled === !0, i = fe(() => {
    a(n);
  }, [a, n]);
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
}, { useCallback: ve } = e, Ee = ({ command: t, label: o, active: l, disabled: r }) => {
  const a = w(), n = B(), c = t ?? "click", s = o ?? a.label, i = l ?? a.active === !0, d = r ?? a.disabled === !0, b = ve(() => {
    n(c);
  }, [n, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: b,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    s
  );
}, _e = () => {
  const t = w(), o = B(), l = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ge } = e, Ce = () => {
  const t = w(), o = B(), l = t.tabs ?? [], r = t.activeTabId, a = ge((n) => {
    n !== r && o("selectTab", { tabId: n });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((n) => /* @__PURE__ */ e.createElement(
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
}, ye = () => {
  const t = w(), o = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((r, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(L, { control: r })))));
}, ke = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ne = () => {
  const t = w(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), d = t.status ?? "idle", b = t.error, f = d === "received" ? "idle" : l !== "idle" ? l : d, N = e.useCallback(async () => {
    if (l === "recording") {
      const h = c.current;
      h && h.state !== "inactive" && h.stop();
      return;
    }
    if (l !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const h = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = h, s.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(h, x ? { mimeType: x } : void 0);
        c.current = D, D.ondataavailable = (_) => {
          _.data.size > 0 && s.current.push(_.data);
        }, D.onstop = async () => {
          h.getTracks().forEach((R) => R.stop()), i.current = null;
          const _ = new Blob(s.current, { type: D.mimeType || "audio/webm" });
          if (s.current = [], _.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const S = new FormData();
          S.append("audio", _, "recording.webm"), await o(S), r("idle");
        }, D.start(), r("recording");
      } catch (h) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", h), n("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [l, o]), v = P(ke), C = f === "recording" ? v["js.audioRecorder.stop"] : f === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], p = f === "uploading", E = ["tlAudioRecorder__button"];
  return f === "recording" && E.push("tlAudioRecorder__button--recording"), f === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: N,
      disabled: p,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[a]), b && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b));
}, we = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Se = () => {
  const t = w(), o = G(), l = !!t.hasAudio, r = t.dataRevision ?? 0, [a, n] = e.useState(l ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(r);
  e.useEffect(() => {
    l ? a === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), n("disabled"));
  }, [l]), e.useEffect(() => {
    r !== i.current && (i.current = r, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (a === "playing" || a === "paused" || a === "loading") && n("idle"));
  }, [r]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const d = e.useCallback(async () => {
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
    if (!s.current) {
      n("loading");
      try {
        const p = await fetch(o);
        if (!p.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", p.status), n("idle");
          return;
        }
        const E = await p.blob();
        s.current = URL.createObjectURL(E);
      } catch (p) {
        console.error("[TLAudioPlayer] Fetch error:", p), n("idle");
        return;
      }
    }
    const C = new Audio(s.current);
    c.current = C, C.onended = () => {
      n("idle");
    }, C.play(), n("playing");
  }, [a, o]), b = P(we), f = a === "loading" ? b["js.loading"] : a === "playing" ? b["js.audioPlayer.pause"] : a === "disabled" ? b["js.audioPlayer.noAudio"] : b["js.audioPlayer.play"], N = a === "disabled" || a === "loading", v = ["tlAudioPlayer__button"];
  return a === "playing" && v.push("tlAudioPlayer__button--playing"), a === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: d,
      disabled: N,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Le = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Re = () => {
  const t = w(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(!1), c = e.useRef(null), s = t.status ?? "idle", i = t.error, d = t.accept ?? "", b = s === "received" ? "idle" : l !== "idle" ? l : s, f = e.useCallback(async (_) => {
    r("uploading");
    const S = new FormData();
    S.append("file", _, _.name), await o(S), r("idle");
  }, [o]), N = e.useCallback((_) => {
    var R;
    const S = (R = _.target.files) == null ? void 0 : R[0];
    S && f(S);
  }, [f]), v = e.useCallback(() => {
    var _;
    l !== "uploading" && ((_ = c.current) == null || _.click());
  }, [l]), C = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), n(!0);
  }, []), p = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), n(!1);
  }, []), E = e.useCallback((_) => {
    var R;
    if (_.preventDefault(), _.stopPropagation(), n(!1), l === "uploading") return;
    const S = (R = _.dataTransfer.files) == null ? void 0 : R[0];
    S && f(S);
  }, [l, f]), h = b === "uploading", x = P(Le), D = b === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: p,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: d || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (b === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: h,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, xe = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Te = () => {
  const t = w(), o = G(), l = B(), r = !!t.hasData, a = t.dataRevision ?? 0, n = t.fileName ?? "download", c = !!t.clearable, [s, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!r || s)) {
      i(!0);
      try {
        const v = o + (o.includes("?") ? "&" : "?") + "rev=" + a, C = await fetch(v);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const p = await C.blob(), E = URL.createObjectURL(p), h = document.createElement("a");
        h.href = E, h.download = n, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(E);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        i(!1);
      }
    }
  }, [r, s, o, a, n]), b = e.useCallback(async () => {
    r && await l("clear");
  }, [r, l]), f = P(xe);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const N = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: s,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: b,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, De = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Be = () => {
  const t = w(), o = Y(), [l, r] = e.useState("idle"), [a, n] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), b = e.useRef(null), f = e.useRef(null), N = e.useRef(null), v = t.error, C = e.useMemo(
    () => {
      var u;
      return !!(window.isSecureContext && ((u = navigator.mediaDevices) != null && u.getUserMedia));
    },
    []
  ), p = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((u) => u.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    p(), r("idle");
  }, [p]), h = e.useCallback(async () => {
    var u;
    if (l !== "uploading") {
      if (n(null), !C) {
        (u = f.current) == null || u.click();
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = y, r("overlayOpen");
      } catch (y) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", y), n("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [l, C]), x = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const u = i.current, y = b.current;
    if (!u || !y)
      return;
    y.width = u.videoWidth, y.height = u.videoHeight;
    const g = y.getContext("2d");
    g && (g.drawImage(u, 0, 0), p(), r("uploading"), y.toBlob(async (T) => {
      if (!T) {
        r("idle");
        return;
      }
      const M = new FormData();
      M.append("photo", T, "capture.jpg"), await o(M), r("idle");
    }, "image/jpeg", 0.85));
  }, [l, o, p]), D = e.useCallback(async (u) => {
    var T;
    const y = (T = u.target.files) == null ? void 0 : T[0];
    if (!y) return;
    r("uploading");
    const g = new FormData();
    g.append("photo", y, y.name), await o(g), r("idle"), f.current && (f.current.value = "");
  }, [o]);
  e.useEffect(() => {
    l === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [l]), e.useEffect(() => {
    var y;
    if (l !== "overlayOpen") return;
    (y = N.current) == null || y.focus();
    const u = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = u;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const u = (y) => {
      y.key === "Escape" && E();
    };
    return document.addEventListener("keydown", u), () => document.removeEventListener("keydown", u);
  }, [l, E]), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((u) => u.stop()), d.current = null);
  }, []);
  const _ = P(De), S = l === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], R = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && R.push("tlPhotoCapture__cameraBtn--uploading");
  const I = ["tlPhotoCapture__overlayVideo"];
  c && I.push("tlPhotoCapture__overlayVideo--mirrored");
  const m = ["tlPhotoCapture__mirrorBtn"];
  return c && m.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: h,
      disabled: l === "uploading",
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !C && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: D
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: b, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: E }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: I.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: m.join(" "),
        onClick: () => s((u) => !u),
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: x,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: E,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, je = {
  "js.photoViewer.alt": "Captured photo"
}, Pe = () => {
  const t = w(), o = G(), l = !!t.hasPhoto, r = t.dataRevision ?? 0, [a, n] = e.useState(null), c = e.useRef(r);
  e.useEffect(() => {
    if (!l) {
      a && (URL.revokeObjectURL(a), n(null));
      return;
    }
    if (r === c.current && a)
      return;
    c.current = r, a && (URL.revokeObjectURL(a), n(null));
    let i = !1;
    return (async () => {
      try {
        const d = await fetch(o);
        if (!d.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", d.status);
          return;
        }
        const b = await d.blob();
        i || n(URL.createObjectURL(b));
      } catch (d) {
        console.error("[TLPhotoViewer] Fetch error:", d);
      }
    })(), () => {
      i = !0;
    };
  }, [l, r, o]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const s = P(je);
  return !l || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: J, useRef: O } = e, Ie = () => {
  const t = w(), o = B(), l = t.orientation, r = t.resizable === !0, a = t.children ?? [], n = l === "horizontal", c = a.length > 0 && a.every((p) => p.collapsed), s = !c && a.some((p) => p.collapsed), i = c ? !n : n, d = O(null), b = O(null), f = O(null), N = J((p, E) => {
    const h = {
      overflow: p.scrolling || "auto"
    };
    return p.collapsed ? c && !i ? h.flex = "1 0 0%" : h.flex = "0 0 auto" : E !== void 0 ? h.flex = `0 0 ${E}px` : p.unit === "%" || s ? h.flex = `${p.size} 0 0%` : h.flex = `0 0 ${p.size}px`, p.minSize > 0 && !p.collapsed && (h.minWidth = n ? p.minSize : void 0, h.minHeight = n ? void 0 : p.minSize), h;
  }, [n, c, s, i]), v = J((p, E) => {
    p.preventDefault();
    const h = d.current;
    if (!h) return;
    const x = a[E], D = a[E + 1], _ = h.querySelectorAll(":scope > .tlSplitPanel__child"), S = [];
    _.forEach((m) => {
      S.push(n ? m.offsetWidth : m.offsetHeight);
    }), f.current = S, b.current = {
      splitterIndex: E,
      startPos: n ? p.clientX : p.clientY,
      startSizeBefore: S[E],
      startSizeAfter: S[E + 1],
      childBefore: x,
      childAfter: D
    };
    const R = (m) => {
      const u = b.current;
      if (!u || !f.current) return;
      const g = (n ? m.clientX : m.clientY) - u.startPos, T = u.childBefore.minSize || 0, M = u.childAfter.minSize || 0;
      let $ = u.startSizeBefore + g, A = u.startSizeAfter - g;
      $ < T && (A += $ - T, $ = T), A < M && ($ += A - M, A = M), f.current[u.splitterIndex] = $, f.current[u.splitterIndex + 1] = A;
      const q = h.querySelectorAll(":scope > .tlSplitPanel__child"), X = q[u.splitterIndex], Z = q[u.splitterIndex + 1];
      X && (X.style.flex = `0 0 ${$}px`), Z && (Z.style.flex = `0 0 ${A}px`);
    }, I = () => {
      if (document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", I), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const m = {};
        a.forEach((u, y) => {
          const g = u.control;
          g != null && g.controlId && f.current && (m[g.controlId] = f.current[y]);
        }), o("updateSizes", { sizes: m });
      }
      f.current = null, b.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", I), document.body.style.cursor = n ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, n, o]), C = [];
  return a.forEach((p, E) => {
    if (C.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${E}`,
          className: `tlSplitPanel__child${p.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(p)
        },
        /* @__PURE__ */ e.createElement(L, { control: p.control })
      )
    ), r && E < a.length - 1) {
      const h = a[E + 1];
      !p.collapsed && !h.collapsed && C.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${E}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (D) => v(D, E)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: d,
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
}, $e = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Ae = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ze = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Ue = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Fe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Oe = () => {
  const t = w(), o = B(), l = P(Me), r = t.title, a = t.expansionState ?? "NORMALIZED", n = t.showMinimize === !0, c = t.showMaximize === !0, s = t.showPopOut === !0, i = t.toolbarButtons ?? [], d = a === "MINIMIZED", b = a === "MAXIMIZED", f = a === "HIDDEN", N = V(() => {
    o("toggleMinimize");
  }, [o]), v = V(() => {
    o("toggleMaximize");
  }, [o]), C = V(() => {
    o("popOut");
  }, [o]);
  if (f)
    return null;
  const p = b ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: p
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((E, h) => /* @__PURE__ */ e.createElement("span", { key: h, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(L, { control: E }))), n && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: d ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      d ? /* @__PURE__ */ e.createElement(Ae, null) : /* @__PURE__ */ e.createElement($e, null)
    ), c && !d && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: b ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      b ? /* @__PURE__ */ e.createElement(Ue, null) : /* @__PURE__ */ e.createElement(ze, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Fe, null)
    ))),
    !d && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(L, { control: t.child }))
  );
}, Ve = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(L, { control: t.child })
  );
}, We = () => {
  const t = w();
  return /* @__PURE__ */ e.createElement("div", { className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(L, { control: t.activeChild }));
}, { useCallback: j, useState: W, useEffect: F, useRef: H } = e, He = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function K(t, o, l, r) {
  const a = [];
  for (const n of t)
    n.type === "nav" ? a.push({ id: n.id, type: "nav", groupId: r }) : n.type === "command" ? a.push({ id: n.id, type: "command", groupId: r }) : n.type === "group" && (a.push({ id: n.id, type: "group" }), (l.get(n.id) ?? n.expanded) && !o && a.push(...K(n.children, o, l, n.id)));
  return a;
}
const z = ({ icon: t }) => t ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + t, "aria-hidden": "true" }) : null, Ke = ({ item: t, active: o, collapsed: l, onSelect: r, tabIndex: a, itemRef: n, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (o ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(t.id),
    title: l ? t.label : void 0,
    tabIndex: a,
    ref: n,
    onFocus: () => c(t.id)
  },
  l && t.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(z, { icon: t.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, t.badge)) : /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
  !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label),
  !l && t.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, t.badge)
), Ye = ({ item: t, collapsed: o, onExecute: l, tabIndex: r, itemRef: a, onFocus: n }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => l(t.id),
    title: o ? t.label : void 0,
    tabIndex: r,
    ref: a,
    onFocus: () => n(t.id)
  },
  /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)
), Ge = ({ item: t, collapsed: o }) => o && !t.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: o ? t.label : void 0 }, /* @__PURE__ */ e.createElement(z, { icon: t.icon }), !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, t.label)), qe = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Xe = ({ item: t, activeItemId: o, onSelect: l, onExecute: r, onClose: a }) => {
  const n = H(null);
  F(() => {
    const s = (i) => {
      n.current && !n.current.contains(i.target) && setTimeout(() => a(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [a]), F(() => {
    const s = (i) => {
      i.key === "Escape" && a();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
  }, [a]);
  const c = j((s) => {
    s.type === "nav" ? (l(s.id), a()) : s.type === "command" && (r(s.id), a());
  }, [l, r, a]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: n, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, t.label), t.children.map((s) => {
    if (s.type === "nav" || s.type === "command") {
      const i = s.type === "nav" && s.id === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (i ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => c(s)
        },
        /* @__PURE__ */ e.createElement(z, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, Ze = ({
  item: t,
  expanded: o,
  activeItemId: l,
  collapsed: r,
  onSelect: a,
  onExecute: n,
  onToggleGroup: c,
  tabIndex: s,
  itemRef: i,
  onFocus: d,
  focusedId: b,
  setItemRef: f,
  onItemFocus: N,
  flyoutGroupId: v,
  onOpenFlyout: C,
  onCloseFlyout: p
}) => {
  const E = j(() => {
    r ? v === t.id ? p() : C(t.id) : c(t.id);
  }, [r, v, t.id, c, C, p]), h = r && v === t.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: E,
      title: r ? t.label : void 0,
      "aria-expanded": r ? h : o,
      tabIndex: s,
      ref: i,
      onFocus: () => d(t.id)
    },
    /* @__PURE__ */ e.createElement(z, { icon: t.icon }),
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
  ), h && /* @__PURE__ */ e.createElement(
    Xe,
    {
      item: t,
      activeItemId: l,
      onSelect: a,
      onExecute: n,
      onClose: p
    }
  ), o && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, t.children.map((x) => /* @__PURE__ */ e.createElement(
    ae,
    {
      key: x.id,
      item: x,
      activeItemId: l,
      collapsed: r,
      onSelect: a,
      onExecute: n,
      onToggleGroup: c,
      focusedId: b,
      setItemRef: f,
      onItemFocus: N,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: C,
      onCloseFlyout: p
    }
  ))));
}, ae = ({
  item: t,
  activeItemId: o,
  collapsed: l,
  onSelect: r,
  onExecute: a,
  onToggleGroup: n,
  focusedId: c,
  setItemRef: s,
  onItemFocus: i,
  groupStates: d,
  flyoutGroupId: b,
  onOpenFlyout: f,
  onCloseFlyout: N
}) => {
  switch (t.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ke,
        {
          item: t,
          active: t.id === o,
          collapsed: l,
          onSelect: r,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Ye,
        {
          item: t,
          collapsed: l,
          onExecute: a,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Ge, { item: t, collapsed: l });
    case "separator":
      return /* @__PURE__ */ e.createElement(qe, null);
    case "group": {
      const v = d ? d.get(t.id) ?? t.expanded : t.expanded;
      return /* @__PURE__ */ e.createElement(
        Ze,
        {
          item: t,
          expanded: v,
          activeItemId: o,
          collapsed: l,
          onSelect: r,
          onExecute: a,
          onToggleGroup: n,
          tabIndex: c === t.id ? 0 : -1,
          itemRef: s(t.id),
          onFocus: i,
          focusedId: c,
          setItemRef: s,
          onItemFocus: i,
          flyoutGroupId: b,
          onOpenFlyout: f,
          onCloseFlyout: N
        }
      );
    }
    default:
      return null;
  }
}, Je = () => {
  const t = w(), o = B(), l = P(He), r = t.items ?? [], a = t.activeItemId, n = t.collapsed, [c, s] = W(() => {
    const m = /* @__PURE__ */ new Map(), u = (y) => {
      for (const g of y)
        g.type === "group" && (m.set(g.id, g.expanded), u(g.children));
    };
    return u(r), m;
  }), i = j((m) => {
    s((u) => {
      const y = new Map(u), g = y.get(m) ?? !1;
      return y.set(m, !g), o("toggleGroup", { itemId: m, expanded: !g }), y;
    });
  }, [o]), d = j((m) => {
    m !== a && o("selectItem", { itemId: m });
  }, [o, a]), b = j((m) => {
    o("executeCommand", { itemId: m });
  }, [o]), f = j(() => {
    o("toggleCollapse", {});
  }, [o]), [N, v] = W(null), C = j((m) => {
    v(m);
  }, []), p = j(() => {
    v(null);
  }, []);
  F(() => {
    n || v(null);
  }, [n]);
  const [E, h] = W(() => {
    const m = K(r, n, c);
    return m.length > 0 ? m[0].id : "";
  }), x = H(/* @__PURE__ */ new Map()), D = j((m) => (u) => {
    u ? x.current.set(m, u) : x.current.delete(m);
  }, []), _ = j((m) => {
    h(m);
  }, []), S = H(0), R = j((m) => {
    h(m), S.current++;
  }, []);
  F(() => {
    const m = x.current.get(E);
    m && document.activeElement !== m && m.focus();
  }, [E, S.current]);
  const I = j((m) => {
    if (m.key === "Escape" && N !== null) {
      m.preventDefault(), p();
      return;
    }
    const u = K(r, n, c);
    if (u.length === 0) return;
    const y = u.findIndex((T) => T.id === E);
    if (y < 0) return;
    const g = u[y];
    switch (m.key) {
      case "ArrowDown": {
        m.preventDefault();
        const T = (y + 1) % u.length;
        R(u[T].id);
        break;
      }
      case "ArrowUp": {
        m.preventDefault();
        const T = (y - 1 + u.length) % u.length;
        R(u[T].id);
        break;
      }
      case "Home": {
        m.preventDefault(), R(u[0].id);
        break;
      }
      case "End": {
        m.preventDefault(), R(u[u.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        m.preventDefault(), g.type === "nav" ? d(g.id) : g.type === "command" ? b(g.id) : g.type === "group" && (n ? N === g.id ? p() : C(g.id) : i(g.id));
        break;
      }
      case "ArrowRight": {
        g.type === "group" && !n && ((c.get(g.id) ?? !1) || (m.preventDefault(), i(g.id)));
        break;
      }
      case "ArrowLeft": {
        g.type === "group" && !n && (c.get(g.id) ?? !1) && (m.preventDefault(), i(g.id));
        break;
      }
    }
  }, [
    r,
    n,
    c,
    E,
    N,
    R,
    d,
    b,
    i,
    C,
    p
  ]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar" + (n ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, n ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: I }, r.map((m) => /* @__PURE__ */ e.createElement(
    ae,
    {
      key: m.id,
      item: m,
      activeItemId: a,
      collapsed: n,
      onSelect: d,
      onExecute: b,
      onToggleGroup: i,
      focusedId: E,
      setItemRef: D,
      onItemFocus: _,
      groupStates: c,
      flyoutGroupId: N,
      onOpenFlyout: C,
      onCloseFlyout: p
    }
  ))), n ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(L, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(L, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: n ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
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
}, Qe = () => {
  const t = w(), o = t.direction ?? "column", l = t.gap ?? "default", r = t.align ?? "stretch", a = t.wrap === !0, n = t.children ?? [], c = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${r}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { className: c }, n.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s })));
}, et = () => {
  const t = w(), o = t.columns, l = t.minColumnWidth, r = t.gap ?? "default", a = t.children ?? [], n = {};
  return l ? n.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : o && (n.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { className: `tlGrid tlGrid--gap-${r}`, style: n }, a.map((c, s) => /* @__PURE__ */ e.createElement(L, { key: s, control: c })));
}, tt = () => {
  const t = w(), o = t.title, l = t.variant ?? "outlined", r = t.padding ?? "default", a = t.headerActions ?? [], n = t.child, c = o != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { className: `tlCard tlCard--${l}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(L, { control: n })));
}, at = () => {
  const t = w(), o = t.title ?? "", l = t.leading, r = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { className: c }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(L, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((s, i) => /* @__PURE__ */ e.createElement(L, { key: i, control: s }))));
}, { useCallback: nt } = e, lt = () => {
  const t = w(), o = B(), l = t.items ?? [], r = nt((a) => {
    o("navigate", { itemId: a });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((a, n) => {
    const c = n === l.length - 1;
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
}, { useCallback: ot } = e, rt = () => {
  const t = w(), o = B(), l = t.items ?? [], r = t.activeItemId, a = ot((n) => {
    n !== r && o("selectItem", { itemId: n });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("nav", { className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((n) => {
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
}, { useCallback: Q, useEffect: ee, useRef: st } = e, ct = {
  "js.dialog.close": "Close"
}, it = () => {
  const t = w(), o = B(), l = P(ct), r = t.open === !0, a = t.title ?? "", n = t.size ?? "medium", c = t.closeOnBackdrop !== !1, s = t.actions ?? [], i = t.child, d = st(null), b = Q(() => {
    o("close");
  }, [o]), f = Q((v) => {
    c && v.target === v.currentTarget && b();
  }, [c, b]);
  if (ee(() => {
    if (!r) return;
    const v = (C) => {
      C.key === "Escape" && b();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [r, b]), ee(() => {
    r && d.current && d.current.focus();
  }, [r]), !r) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { className: "tlDialog__backdrop", onClick: f }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${n}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: d,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, a), /* @__PURE__ */ e.createElement(
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(L, { control: i })),
    s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, s.map((v, C) => /* @__PURE__ */ e.createElement(L, { key: C, control: v })))
  ));
}, { useCallback: dt, useEffect: ut } = e, mt = {
  "js.drawer.close": "Close"
}, pt = () => {
  const t = w(), o = B(), l = P(mt), r = t.open === !0, a = t.position ?? "right", n = t.size ?? "medium", c = t.title ?? null, s = t.child, i = dt(() => {
    o("close");
  }, [o]);
  ut(() => {
    if (!r) return;
    const b = (f) => {
      f.key === "Escape" && i();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [r, i]);
  const d = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${n}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { className: d, "aria-hidden": !r }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: i,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(L, { control: s })));
}, { useCallback: te, useEffect: bt, useState: ft } = e, ht = () => {
  const t = w(), o = B(), l = t.message ?? "", r = t.variant ?? "info", a = t.action, n = t.duration ?? 5e3, c = t.visible === !0, [s, i] = ft(!1), d = te(() => {
    i(!0), setTimeout(() => {
      o("dismiss"), i(!1);
    }, 200);
  }, [o]), b = te(() => {
    a && o(a.commandName), d();
  }, [o, a, d]);
  return bt(() => {
    if (!c || n === 0) return;
    const f = setTimeout(d, n);
    return () => clearTimeout(f);
  }, [c, n, d]), !c && !s ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlSnackbar tlSnackbar--${r}${s ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: b }, a.label)
  );
};
k("TLButton", he);
k("TLToggleButton", Ee);
k("TLTextInput", oe);
k("TLNumberInput", se);
k("TLDatePicker", ie);
k("TLSelect", ue);
k("TLCheckbox", pe);
k("TLTable", be);
k("TLCounter", _e);
k("TLTabBar", Ce);
k("TLFieldList", ye);
k("TLAudioRecorder", Ne);
k("TLAudioPlayer", Se);
k("TLFileUpload", Re);
k("TLDownload", Te);
k("TLPhotoCapture", Be);
k("TLPhotoViewer", Pe);
k("TLSplitPanel", Ie);
k("TLPanel", Oe);
k("TLMaximizeRoot", Ve);
k("TLDeckPane", We);
k("TLSidebar", Je);
k("TLStack", Qe);
k("TLGrid", et);
k("TLCard", tt);
k("TLAppBar", at);
k("TLBreadcrumb", lt);
k("TLBottomBar", rt);
k("TLDialog", it);
k("TLDrawer", pt);
k("TLSnackbar", ht);
