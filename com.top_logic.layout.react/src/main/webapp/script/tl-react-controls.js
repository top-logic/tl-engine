import { React as e, useTLFieldValue as T, getComponent as U, useTLState as k, useTLCommand as N, TLChild as D, useTLUpload as j, useI18N as y, useTLDataUrl as P, register as v } from "tl-react-bridge";
const { useCallback: S } = e, A = ({ state: n }) => {
  const [s, t] = T(), o = S(
    (a) => {
      t(a.target.value);
    },
    [t]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: s ?? "",
      onChange: o,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: F } = e, B = ({ state: n, config: s }) => {
  const [t, o] = T(), a = F(
    (r) => {
      const c = r.target.value, i = c === "" ? null : Number(c);
      o(i);
    },
    [o]
  ), l = s != null && s.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: t != null ? String(t) : "",
      onChange: a,
      step: l,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: I } = e, M = ({ state: n }) => {
  const [s, t] = T(), o = I(
    (a) => {
      t(a.target.value || null);
    },
    [t]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: s ?? "",
      onChange: o,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: O } = e, V = ({ state: n, config: s }) => {
  const [t, o] = T(), a = O(
    (r) => {
      o(r.target.value || null);
    },
    [o]
  ), l = n.options ?? (s == null ? void 0 : s.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: t ?? "",
      onChange: a,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((r) => /* @__PURE__ */ e.createElement("option", { key: r.value, value: r.value }, r.label))
  );
}, { useCallback: $ } = e, x = ({ state: n }) => {
  const [s, t] = T(), o = $(
    (a) => {
      t(a.target.checked);
    },
    [t]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: s === !0,
      onChange: o,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, K = ({ controlId: n, state: s }) => {
  const t = s.columns ?? [], o = s.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, t.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((a, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, t.map((r) => {
    const c = r.cellModule ? U(r.cellModule) : void 0, i = a[r.name];
    if (c) {
      const m = { value: i, editable: s.editable };
      return /* @__PURE__ */ e.createElement("td", { key: r.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: n + "-" + l + "-" + r.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: r.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Y } = e, W = ({ command: n, label: s, disabled: t }) => {
  const o = k(), a = N(), l = n ?? "click", r = s ?? o.label, c = t ?? o.disabled === !0, i = Y(() => {
    a(l);
  }, [a, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: c,
      className: "tlReactButton"
    },
    r
  );
}, { useCallback: H } = e, q = ({ command: n, label: s, active: t, disabled: o }) => {
  const a = k(), l = N(), r = n ?? "click", c = s ?? a.label, i = t ?? a.active === !0, m = o ?? a.disabled === !0, d = H(() => {
    l(r);
  }, [l, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: m,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, z = () => {
  const n = k(), s = N(), t = n.count ?? 0, o = n.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, t), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: G } = e, J = () => {
  const n = k(), s = N(), t = n.tabs ?? [], o = n.activeTabId, a = G((l) => {
    l !== o && s("selectTab", { tabId: l });
  }, [s, o]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, t.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === o,
      className: "tlReactTabBar__tab" + (l.id === o ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, n.activeContent && /* @__PURE__ */ e.createElement(D, { control: n.activeContent })));
}, Q = () => {
  const n = k(), s = n.title, t = n.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, t.map((o, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(D, { control: o })))));
}, X = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Z = () => {
  const n = k(), s = j(), [t, o] = e.useState("idle"), [a, l] = e.useState(null), r = e.useRef(null), c = e.useRef([]), i = e.useRef(null), m = n.status ?? "idle", d = n.error, p = m === "received" ? "idle" : t !== "idle" ? t : m, w = e.useCallback(async () => {
    if (t === "recording") {
      const h = r.current;
      h && h.state !== "inactive" && h.stop();
      return;
    }
    if (t !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const h = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = h, c.current = [];
        const f = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", g = new MediaRecorder(h, f ? { mimeType: f } : void 0);
        r.current = g, g.ondataavailable = (u) => {
          u.data.size > 0 && c.current.push(u.data);
        }, g.onstop = async () => {
          h.getTracks().forEach((R) => R.stop()), i.current = null;
          const u = new Blob(c.current, { type: g.mimeType || "audio/webm" });
          if (c.current = [], u.size === 0) {
            o("idle");
            return;
          }
          o("uploading");
          const _ = new FormData();
          _.append("audio", u, "recording.webm"), await s(_), o("idle");
        }, g.start(), o("recording");
      } catch (h) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", h), l("js.audioRecorder.error.denied"), o("idle");
      }
    }
  }, [t, s]), b = y(X), C = p === "recording" ? b["js.audioRecorder.stop"] : p === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = p === "uploading", L = ["tlAudioRecorder__button"];
  return p === "recording" && L.push("tlAudioRecorder__button--recording"), p === "uploading" && L.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: w,
      disabled: E,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[a]), d && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, d));
}, ee = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, te = () => {
  const n = k(), s = P(), t = !!n.hasAudio, o = n.dataRevision ?? 0, [a, l] = e.useState(t ? "idle" : "disabled"), r = e.useRef(null), c = e.useRef(null), i = e.useRef(o);
  e.useEffect(() => {
    t ? a === "disabled" && l("idle") : (r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), l("disabled"));
  }, [t]), e.useEffect(() => {
    o !== i.current && (i.current = o, r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (a === "playing" || a === "paused" || a === "loading") && l("idle"));
  }, [o]), e.useEffect(() => () => {
    r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      r.current && r.current.pause(), l("paused");
      return;
    }
    if (a === "paused" && r.current) {
      r.current.play(), l("playing");
      return;
    }
    if (!c.current) {
      l("loading");
      try {
        const E = await fetch(s);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), l("idle");
          return;
        }
        const L = await E.blob();
        c.current = URL.createObjectURL(L);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), l("idle");
        return;
      }
    }
    const C = new Audio(c.current);
    r.current = C, C.onended = () => {
      l("idle");
    }, C.play(), l("playing");
  }, [a, s]), d = y(ee), p = a === "loading" ? d["js.loading"] : a === "playing" ? d["js.audioPlayer.pause"] : a === "disabled" ? d["js.audioPlayer.noAudio"] : d["js.audioPlayer.play"], w = a === "disabled" || a === "loading", b = ["tlAudioPlayer__button"];
  return a === "playing" && b.push("tlAudioPlayer__button--playing"), a === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: w,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ae = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, oe = () => {
  const n = k(), s = j(), [t, o] = e.useState("idle"), [a, l] = e.useState(!1), r = e.useRef(null), c = n.status ?? "idle", i = n.error, m = n.accept ?? "", d = c === "received" ? "idle" : t !== "idle" ? t : c, p = e.useCallback(async (u) => {
    o("uploading");
    const _ = new FormData();
    _.append("file", u, u.name), await s(_), o("idle");
  }, [s]), w = e.useCallback((u) => {
    var R;
    const _ = (R = u.target.files) == null ? void 0 : R[0];
    _ && p(_);
  }, [p]), b = e.useCallback(() => {
    var u;
    t !== "uploading" && ((u = r.current) == null || u.click());
  }, [t]), C = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!0);
  }, []), E = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!1);
  }, []), L = e.useCallback((u) => {
    var R;
    if (u.preventDefault(), u.stopPropagation(), l(!1), t === "uploading") return;
    const _ = (R = u.dataTransfer.files) == null ? void 0 : R[0];
    _ && p(_);
  }, [t, p]), h = d === "uploading", f = y(ae), g = d === "uploading" ? f["js.uploading"] : f["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: C,
      onDragLeave: E,
      onDrop: L
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: r,
        type: "file",
        accept: m || void 0,
        onChange: w,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (d === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: h,
        title: g,
        "aria-label": g
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, le = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ne = () => {
  const n = k(), s = P(), t = N(), o = !!n.hasData, a = n.dataRevision ?? 0, l = n.fileName ?? "download", r = !!n.clearable, [c, i] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!o || c)) {
      i(!0);
      try {
        const b = s + (s.includes("?") ? "&" : "?") + "rev=" + a, C = await fetch(b);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const E = await C.blob(), L = URL.createObjectURL(E), h = document.createElement("a");
        h.href = L, h.download = l, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(L);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        i(!1);
      }
    }
  }, [o, c, s, a, l]), d = e.useCallback(async () => {
    o && await t("clear");
  }, [o, t]), p = y(le);
  if (!o)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const w = c ? p["js.downloading"] : p["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: c,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), r && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: d,
      title: p["js.download.clear"],
      "aria-label": p["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, re = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.insecure": "Camera requires a secure connection (HTTPS).",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, se = () => {
  const n = k(), s = j(), [t, o] = e.useState("idle"), [a, l] = e.useState(null), r = e.useRef(null), c = e.useRef(null), i = e.useRef(null), m = n.error, d = e.useCallback(() => {
    c.current && (c.current.getTracks().forEach((f) => f.stop()), c.current = null), r.current && (r.current.srcObject = null);
  }, []), p = e.useCallback(async () => {
    if (t !== "uploading") {
      if (t === "previewing") {
        d(), o("idle");
        return;
      }
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.photoCapture.error.insecure");
        return;
      }
      try {
        const f = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        c.current = f, o("previewing");
      } catch (f) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", f), l("js.photoCapture.error.denied"), o("idle");
      }
    }
  }, [t, d]), w = e.useCallback(async () => {
    if (t !== "previewing")
      return;
    const f = r.current, g = i.current;
    if (!f || !g)
      return;
    g.width = f.videoWidth, g.height = f.videoHeight;
    const u = g.getContext("2d");
    u && (u.drawImage(f, 0, 0), d(), o("uploading"), g.toBlob(async (_) => {
      if (!_) {
        o("idle");
        return;
      }
      const R = new FormData();
      R.append("photo", _, "capture.jpg"), await s(R), o("idle");
    }, "image/jpeg", 0.85));
  }, [t, s, d]);
  e.useEffect(() => {
    t === "previewing" && r.current && c.current && (r.current.srcObject = c.current);
  }, [t]), e.useEffect(() => () => {
    c.current && (c.current.getTracks().forEach((f) => f.stop()), c.current = null);
  }, []);
  const b = y(re), C = t === "previewing" ? b["js.photoCapture.close"] : t === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], E = t === "uploading", L = ["tlPhotoCapture__cameraBtn"];
  t === "previewing" && L.push("tlPhotoCapture__cameraBtn--active");
  const h = ["tlPhotoCapture__captureBtn"];
  return t === "uploading" && h.push("tlPhotoCapture__captureBtn--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: p,
      disabled: E,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  ), t === "previewing" && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: w,
      title: b["js.photoCapture.capture"],
      "aria-label": b["js.photoCapture.capture"]
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__captureIcon" })
  )), t === "previewing" && /* @__PURE__ */ e.createElement(
    "video",
    {
      ref: r,
      className: "tlPhotoCapture__preview",
      autoPlay: !0,
      muted: !0,
      playsInline: !0
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: i, style: { display: "none" } }), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[a]), m && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m));
}, ce = {
  "js.photoViewer.alt": "Captured photo"
}, ie = () => {
  const n = k(), s = P(), t = !!n.hasPhoto, o = n.dataRevision ?? 0, [a, l] = e.useState(null), r = e.useRef(o);
  e.useEffect(() => {
    if (!t) {
      a && (URL.revokeObjectURL(a), l(null));
      return;
    }
    if (o === r.current && a)
      return;
    r.current = o, a && (URL.revokeObjectURL(a), l(null));
    let i = !1;
    return (async () => {
      try {
        const m = await fetch(s);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const d = await m.blob();
        i || l(URL.createObjectURL(d));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      i = !0;
    };
  }, [t, o, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const c = y(ce);
  return !t || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: c["js.photoViewer.alt"]
    }
  ));
};
v("TLButton", W);
v("TLToggleButton", q);
v("TLTextInput", A);
v("TLNumberInput", B);
v("TLDatePicker", M);
v("TLSelect", V);
v("TLCheckbox", x);
v("TLTable", K);
v("TLCounter", z);
v("TLTabBar", J);
v("TLFieldList", Q);
v("TLAudioRecorder", Z);
v("TLAudioPlayer", te);
v("TLFileUpload", oe);
v("TLDownload", ne);
v("TLPhotoCapture", se);
v("TLPhotoViewer", ie);
