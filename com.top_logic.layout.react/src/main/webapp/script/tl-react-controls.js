import { React as e, useTLFieldValue as T, getComponent as U, useTLState as E, useTLCommand as N, TLChild as P, useTLUpload as j, useI18N as R, useTLDataUrl as D, register as v } from "tl-react-bridge";
const { useCallback: S } = e, F = ({ state: o }) => {
  const [r, a] = T(), n = S(
    (t) => {
      a(t.target.value);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: n,
      disabled: o.disabled === !0 || o.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: B } = e, A = ({ state: o, config: r }) => {
  const [a, n] = T(), t = B(
    (s) => {
      const c = s.target.value, i = c === "" ? null : Number(c);
      n(i);
    },
    [n]
  ), l = r != null && r.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: t,
      step: l,
      disabled: o.disabled === !0 || o.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: I } = e, O = ({ state: o }) => {
  const [r, a] = T(), n = I(
    (t) => {
      a(t.target.value || null);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: n,
      disabled: o.disabled === !0 || o.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: V } = e, M = ({ state: o, config: r }) => {
  const [a, n] = T(), t = V(
    (s) => {
      n(s.target.value || null);
    },
    [n]
  ), l = o.options ?? (r == null ? void 0 : r.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: t,
      disabled: o.disabled === !0 || o.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: $ } = e, x = ({ state: o }) => {
  const [r, a] = T(), n = $(
    (t) => {
      a(t.target.checked);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: r === !0,
      onChange: n,
      disabled: o.disabled === !0 || o.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, K = ({ controlId: o, state: r }) => {
  const a = r.columns ?? [], n = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((t, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((s) => {
    const c = s.cellModule ? U(s.cellModule) : void 0, i = t[s.name];
    if (c) {
      const u = { value: i, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: o + "-" + l + "-" + s.name,
          state: u
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Y } = e, W = ({ command: o, label: r, disabled: a }) => {
  const n = E(), t = N(), l = o ?? "click", s = r ?? n.label, c = a ?? n.disabled === !0, i = Y(() => {
    t(l);
  }, [t, l]);
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
}, { useCallback: z } = e, H = ({ command: o, label: r, active: a, disabled: n }) => {
  const t = E(), l = N(), s = o ?? "click", c = r ?? t.label, i = a ?? t.active === !0, u = n ?? t.disabled === !0, f = z(() => {
    l(s);
  }, [l, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: f,
      disabled: u,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, q = () => {
  const o = E(), r = N(), a = o.count ?? 0, n = o.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: G } = e, J = () => {
  const o = E(), r = N(), a = o.tabs ?? [], n = o.activeTabId, t = G((l) => {
    l !== n && r("selectTab", { tabId: l });
  }, [r, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === n,
      className: "tlReactTabBar__tab" + (l.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => t(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, o.activeContent && /* @__PURE__ */ e.createElement(P, { control: o.activeContent })));
}, Q = () => {
  const o = E(), r = o.title, a = o.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((n, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(P, { control: n })))));
}, X = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…"
}, Z = () => {
  const o = E(), r = j(), [a, n] = e.useState("idle"), t = e.useRef(null), l = e.useRef([]), s = e.useRef(null), c = o.status ?? "idle", i = o.error, u = c === "received" ? "idle" : a !== "idle" ? a : c, f = e.useCallback(async () => {
    if (a === "recording") {
      const p = t.current;
      p && p.state !== "inactive" && p.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const p = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        s.current = p, l.current = [];
        const m = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", h = new MediaRecorder(p, m ? { mimeType: m } : void 0);
        t.current = h, h.ondataavailable = (L) => {
          L.data.size > 0 && l.current.push(L.data);
        }, h.onstop = async () => {
          p.getTracks().forEach((b) => b.stop()), s.current = null;
          const L = new Blob(l.current, { type: h.mimeType || "audio/webm" });
          if (l.current = [], L.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const k = new FormData();
          k.append("audio", L, "recording.webm"), await r(k), n("idle");
        }, h.start(), n("recording");
      } catch (p) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", p), n("idle");
      }
  }, [a, r]), d = R(X), _ = u === "recording" ? d["js.audioRecorder.stop"] : u === "uploading" ? d["js.uploading"] : d["js.audioRecorder.record"], C = u === "uploading", g = ["tlAudioRecorder__button"];
  return u === "recording" && g.push("tlAudioRecorder__button--recording"), u === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: f,
      disabled: C,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${u === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, ee = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, te = () => {
  const o = E(), r = D(), a = !!o.hasAudio, n = o.dataRevision ?? 0, [t, l] = e.useState(a ? "idle" : "disabled"), s = e.useRef(null), c = e.useRef(null), i = e.useRef(n);
  e.useEffect(() => {
    a ? t === "disabled" && l("idle") : (s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), l("disabled"));
  }, [a]), e.useEffect(() => {
    n !== i.current && (i.current = n, s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (t === "playing" || t === "paused" || t === "loading") && l("idle"));
  }, [n]), e.useEffect(() => () => {
    s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const u = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      s.current && s.current.pause(), l("paused");
      return;
    }
    if (t === "paused" && s.current) {
      s.current.play(), l("playing");
      return;
    }
    if (!c.current) {
      l("loading");
      try {
        const p = await fetch(r);
        if (!p.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", p.status), l("idle");
          return;
        }
        const m = await p.blob();
        c.current = URL.createObjectURL(m);
      } catch (p) {
        console.error("[TLAudioPlayer] Fetch error:", p), l("idle");
        return;
      }
    }
    const g = new Audio(c.current);
    s.current = g, g.onended = () => {
      l("idle");
    }, g.play(), l("playing");
  }, [t, r]), f = R(ee), d = t === "loading" ? f["js.loading"] : t === "playing" ? f["js.audioPlayer.pause"] : t === "disabled" ? f["js.audioPlayer.noAudio"] : f["js.audioPlayer.play"], _ = t === "disabled" || t === "loading", C = ["tlAudioPlayer__button"];
  return t === "playing" && C.push("tlAudioPlayer__button--playing"), t === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: u,
      disabled: _,
      title: d,
      "aria-label": d
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ae = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, le = () => {
  const o = E(), r = j(), [a, n] = e.useState("idle"), [t, l] = e.useState(!1), s = e.useRef(null), c = o.status ?? "idle", i = o.error, u = o.accept ?? "", f = c === "received" ? "idle" : a !== "idle" ? a : c, d = e.useCallback(async (b) => {
    n("uploading");
    const w = new FormData();
    w.append("file", b, b.name), await r(w), n("idle");
  }, [r]), _ = e.useCallback((b) => {
    var y;
    const w = (y = b.target.files) == null ? void 0 : y[0];
    w && d(w);
  }, [d]), C = e.useCallback(() => {
    var b;
    a !== "uploading" && ((b = s.current) == null || b.click());
  }, [a]), g = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), l(!0);
  }, []), p = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), l(!1);
  }, []), m = e.useCallback((b) => {
    var y;
    if (b.preventDefault(), b.stopPropagation(), l(!1), a === "uploading") return;
    const w = (y = b.dataTransfer.files) == null ? void 0 : y[0];
    w && d(w);
  }, [a, d]), h = f === "uploading", L = R(ae), k = f === "uploading" ? L["js.uploading"] : L["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: g,
      onDragLeave: p,
      onDrop: m
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: s,
        type: "file",
        accept: u || void 0,
        onChange: _,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (f === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: h,
        title: k,
        "aria-label": k
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, ne = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, oe = () => {
  const o = E(), r = D(), a = N(), n = !!o.hasData, t = o.dataRevision ?? 0, l = o.fileName ?? "download", s = !!o.clearable, [c, i] = e.useState(!1), u = e.useCallback(async () => {
    if (!(!n || c)) {
      i(!0);
      try {
        const C = r + (r.includes("?") ? "&" : "?") + "rev=" + t, g = await fetch(C);
        if (!g.ok) {
          console.error("[TLDownload] Failed to fetch data:", g.status);
          return;
        }
        const p = await g.blob(), m = URL.createObjectURL(p), h = document.createElement("a");
        h.href = m, h.download = l, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(m);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        i(!1);
      }
    }
  }, [n, c, r, t, l]), f = e.useCallback(async () => {
    n && await a("clear");
  }, [n, a]), d = R(ne);
  if (!n)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, d["js.download.noFile"]));
  const _ = c ? d["js.downloading"] : d["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: u,
      disabled: c,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: f,
      title: d["js.download.clear"],
      "aria-label": d["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, re = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.uploading": "Uploading…"
}, se = () => {
  const o = E(), r = j(), [a, n] = e.useState("idle"), t = e.useRef(null), l = e.useRef(null), s = e.useRef(null), c = o.error, i = e.useCallback(() => {
    l.current && (l.current.getTracks().forEach((m) => m.stop()), l.current = null), t.current && (t.current.srcObject = null);
  }, []), u = e.useCallback(async () => {
    if (a !== "uploading") {
      if (a === "previewing") {
        i(), n("idle");
        return;
      }
      try {
        const m = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        l.current = m, n("previewing");
      } catch (m) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", m), n("idle");
      }
    }
  }, [a, i]), f = e.useCallback(async () => {
    if (a !== "previewing")
      return;
    const m = t.current, h = s.current;
    if (!m || !h)
      return;
    h.width = m.videoWidth, h.height = m.videoHeight;
    const L = h.getContext("2d");
    L && (L.drawImage(m, 0, 0), i(), n("uploading"), h.toBlob(async (k) => {
      if (!k) {
        n("idle");
        return;
      }
      const b = new FormData();
      b.append("photo", k, "capture.jpg"), await r(b), n("idle");
    }, "image/jpeg", 0.85));
  }, [a, r, i]);
  e.useEffect(() => {
    a === "previewing" && t.current && l.current && (t.current.srcObject = l.current);
  }, [a]), e.useEffect(() => () => {
    l.current && (l.current.getTracks().forEach((m) => m.stop()), l.current = null);
  }, []);
  const d = R(re), _ = a === "previewing" ? d["js.photoCapture.close"] : a === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], C = a === "uploading", g = ["tlPhotoCapture__cameraBtn"];
  a === "previewing" && g.push("tlPhotoCapture__cameraBtn--active");
  const p = ["tlPhotoCapture__captureBtn"];
  return a === "uploading" && p.push("tlPhotoCapture__captureBtn--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: u,
      disabled: C,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  ), a === "previewing" && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: p.join(" "),
      onClick: f,
      title: d["js.photoCapture.capture"],
      "aria-label": d["js.photoCapture.capture"]
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__captureIcon" })
  )), a === "previewing" && /* @__PURE__ */ e.createElement(
    "video",
    {
      ref: t,
      className: "tlPhotoCapture__preview",
      autoPlay: !0,
      muted: !0,
      playsInline: !0
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: s, style: { display: "none" } }), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, c));
}, ce = {
  "js.photoViewer.alt": "Captured photo"
}, ie = () => {
  const o = E(), r = D(), a = !!o.hasPhoto, n = o.dataRevision ?? 0, [t, l] = e.useState(null), s = e.useRef(n);
  e.useEffect(() => {
    if (!a) {
      t && (URL.revokeObjectURL(t), l(null));
      return;
    }
    if (n === s.current && t)
      return;
    s.current = n, t && (URL.revokeObjectURL(t), l(null));
    let i = !1;
    return (async () => {
      try {
        const u = await fetch(r);
        if (!u.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", u.status);
          return;
        }
        const f = await u.blob();
        i || l(URL.createObjectURL(f));
      } catch (u) {
        console.error("[TLPhotoViewer] Fetch error:", u);
      }
    })(), () => {
      i = !0;
    };
  }, [a, n, r]), e.useEffect(() => () => {
    t && URL.revokeObjectURL(t);
  }, []);
  const c = R(ce);
  return !a || !t ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: t,
      alt: c["js.photoViewer.alt"]
    }
  ));
};
v("TLButton", W);
v("TLToggleButton", H);
v("TLTextInput", F);
v("TLNumberInput", A);
v("TLDatePicker", O);
v("TLSelect", M);
v("TLCheckbox", x);
v("TLTable", K);
v("TLCounter", q);
v("TLTabBar", J);
v("TLFieldList", Q);
v("TLAudioRecorder", Z);
v("TLAudioPlayer", te);
v("TLFileUpload", le);
v("TLDownload", oe);
v("TLPhotoCapture", se);
v("TLPhotoViewer", ie);
