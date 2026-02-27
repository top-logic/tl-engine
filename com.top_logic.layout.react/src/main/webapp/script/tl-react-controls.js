import { React as e, useTLFieldValue as T, getComponent as S, useTLState as k, useTLCommand as N, TLChild as D, useTLUpload as j, useI18N as y, useTLDataUrl as P, register as C } from "tl-react-bridge";
const { useCallback: U } = e, A = ({ state: r }) => {
  const [s, t] = T(), l = U(
    (a) => {
      t(a.target.value);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: s ?? "",
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: F } = e, B = ({ state: r, config: s }) => {
  const [t, l] = T(), a = F(
    (c) => {
      const n = c.target.value, i = n === "" ? null : Number(n);
      l(i);
    },
    [l]
  ), o = s != null && s.decimal ? "0.01" : "1";
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, t != null ? String(t) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: t != null ? String(t) : "",
      onChange: a,
      step: o,
      disabled: r.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: I } = e, x = ({ state: r }) => {
  const [s, t] = T(), l = I(
    (a) => {
      t(a.target.value || null);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, s ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: s ?? "",
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: M } = e, O = ({ state: r, config: s }) => {
  var c;
  const [t, l] = T(), a = M(
    (n) => {
      l(n.target.value || null);
    },
    [l]
  ), o = r.options ?? (s == null ? void 0 : s.options) ?? [];
  if (r.editable === !1) {
    const n = ((c = o.find((i) => i.value === t)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, n);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: t ?? "",
      onChange: a,
      disabled: r.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((n) => /* @__PURE__ */ e.createElement("option", { key: n.value, value: n.value }, n.label))
  );
}, { useCallback: V } = e, $ = ({ state: r }) => {
  const [s, t] = T(), l = V(
    (a) => {
      t(a.target.checked);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: s === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: s === !0,
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, K = ({ controlId: r, state: s }) => {
  const t = s.columns ?? [], l = s.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, t.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((a, o) => /* @__PURE__ */ e.createElement("tr", { key: o }, t.map((c) => {
    const n = c.cellModule ? S(c.cellModule) : void 0, i = a[c.name];
    if (n) {
      const m = { value: i, editable: s.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        n,
        {
          controlId: r + "-" + o + "-" + c.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Y } = e, W = ({ command: r, label: s, disabled: t }) => {
  const l = k(), a = N(), o = r ?? "click", c = s ?? l.label, n = t ?? l.disabled === !0, i = Y(() => {
    a(o);
  }, [a, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: n,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: H } = e, q = ({ command: r, label: s, active: t, disabled: l }) => {
  const a = k(), o = N(), c = r ?? "click", n = s ?? a.label, i = t ?? a.active === !0, m = l ?? a.disabled === !0, d = H(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: m,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    n
  );
}, z = () => {
  const r = k(), s = N(), t = r.count ?? 0, l = r.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, t), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: G } = e, J = () => {
  const r = k(), s = N(), t = r.tabs ?? [], l = r.activeTabId, a = G((o) => {
    o !== l && s("selectTab", { tabId: o });
  }, [s, l]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, t.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === l,
      className: "tlReactTabBar__tab" + (o.id === l ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, r.activeContent && /* @__PURE__ */ e.createElement(D, { control: r.activeContent })));
}, Q = () => {
  const r = k(), s = r.title, t = r.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, t.map((l, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(D, { control: l })))));
}, X = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Z = () => {
  const r = k(), s = j(), [t, l] = e.useState("idle"), [a, o] = e.useState(null), c = e.useRef(null), n = e.useRef([]), i = e.useRef(null), m = r.status ?? "idle", d = r.error, p = m === "received" ? "idle" : t !== "idle" ? t : m, L = e.useCallback(async () => {
    if (t === "recording") {
      const h = c.current;
      h && h.state !== "inactive" && h.stop();
      return;
    }
    if (t !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const h = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = h, n.current = [];
        const f = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", g = new MediaRecorder(h, f ? { mimeType: f } : void 0);
        c.current = g, g.ondataavailable = (u) => {
          u.data.size > 0 && n.current.push(u.data);
        }, g.onstop = async () => {
          h.getTracks().forEach((w) => w.stop()), i.current = null;
          const u = new Blob(n.current, { type: g.mimeType || "audio/webm" });
          if (n.current = [], u.size === 0) {
            l("idle");
            return;
          }
          l("uploading");
          const _ = new FormData();
          _.append("audio", u, "recording.webm"), await s(_), l("idle");
        }, g.start(), l("recording");
      } catch (h) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", h), o("js.audioRecorder.error.denied"), l("idle");
      }
    }
  }, [t, s]), b = y(X), v = p === "recording" ? b["js.audioRecorder.stop"] : p === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = p === "uploading", R = ["tlAudioRecorder__button"];
  return p === "recording" && R.push("tlAudioRecorder__button--recording"), p === "uploading" && R.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: L,
      disabled: E,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[a]), d && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, d));
}, ee = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, te = () => {
  const r = k(), s = P(), t = !!r.hasAudio, l = r.dataRevision ?? 0, [a, o] = e.useState(t ? "idle" : "disabled"), c = e.useRef(null), n = e.useRef(null), i = e.useRef(l);
  e.useEffect(() => {
    t ? a === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), n.current && (URL.revokeObjectURL(n.current), n.current = null), o("disabled"));
  }, [t]), e.useEffect(() => {
    l !== i.current && (i.current = l, c.current && (c.current.pause(), c.current = null), n.current && (URL.revokeObjectURL(n.current), n.current = null), (a === "playing" || a === "paused" || a === "loading") && o("idle"));
  }, [l]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), n.current && (URL.revokeObjectURL(n.current), n.current = null);
  }, []);
  const m = e.useCallback(async () => {
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
    if (!n.current) {
      o("loading");
      try {
        const E = await fetch(s);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), o("idle");
          return;
        }
        const R = await E.blob();
        n.current = URL.createObjectURL(R);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), o("idle");
        return;
      }
    }
    const v = new Audio(n.current);
    c.current = v, v.onended = () => {
      o("idle");
    }, v.play(), o("playing");
  }, [a, s]), d = y(ee), p = a === "loading" ? d["js.loading"] : a === "playing" ? d["js.audioPlayer.pause"] : a === "disabled" ? d["js.audioPlayer.noAudio"] : d["js.audioPlayer.play"], L = a === "disabled" || a === "loading", b = ["tlAudioPlayer__button"];
  return a === "playing" && b.push("tlAudioPlayer__button--playing"), a === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: L,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ae = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, le = () => {
  const r = k(), s = j(), [t, l] = e.useState("idle"), [a, o] = e.useState(!1), c = e.useRef(null), n = r.status ?? "idle", i = r.error, m = r.accept ?? "", d = n === "received" ? "idle" : t !== "idle" ? t : n, p = e.useCallback(async (u) => {
    l("uploading");
    const _ = new FormData();
    _.append("file", u, u.name), await s(_), l("idle");
  }, [s]), L = e.useCallback((u) => {
    var w;
    const _ = (w = u.target.files) == null ? void 0 : w[0];
    _ && p(_);
  }, [p]), b = e.useCallback(() => {
    var u;
    t !== "uploading" && ((u = c.current) == null || u.click());
  }, [t]), v = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), o(!0);
  }, []), E = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), o(!1);
  }, []), R = e.useCallback((u) => {
    var w;
    if (u.preventDefault(), u.stopPropagation(), o(!1), t === "uploading") return;
    const _ = (w = u.dataTransfer.files) == null ? void 0 : w[0];
    _ && p(_);
  }, [t, p]), h = d === "uploading", f = y(ae), g = d === "uploading" ? f["js.uploading"] : f["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: E,
      onDrop: R
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: m || void 0,
        onChange: L,
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
}, ne = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, oe = () => {
  const r = k(), s = P(), t = N(), l = !!r.hasData, a = r.dataRevision ?? 0, o = r.fileName ?? "download", c = !!r.clearable, [n, i] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!l || n)) {
      i(!0);
      try {
        const b = s + (s.includes("?") ? "&" : "?") + "rev=" + a, v = await fetch(b);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const E = await v.blob(), R = URL.createObjectURL(E), h = document.createElement("a");
        h.href = R, h.download = o, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(R);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        i(!1);
      }
    }
  }, [l, n, s, a, o]), d = e.useCallback(async () => {
    l && await t("clear");
  }, [l, t]), p = y(ne);
  if (!l)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, p["js.download.noFile"]));
  const L = n ? p["js.downloading"] : p["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (n ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: n,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
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
  const r = k(), s = j(), [t, l] = e.useState("idle"), [a, o] = e.useState(null), c = e.useRef(null), n = e.useRef(null), i = e.useRef(null), m = r.error, d = e.useCallback(() => {
    n.current && (n.current.getTracks().forEach((f) => f.stop()), n.current = null), c.current && (c.current.srcObject = null);
  }, []), p = e.useCallback(async () => {
    if (t !== "uploading") {
      if (t === "previewing") {
        d(), l("idle");
        return;
      }
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.photoCapture.error.insecure");
        return;
      }
      try {
        const f = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        n.current = f, l("previewing");
      } catch (f) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", f), o("js.photoCapture.error.denied"), l("idle");
      }
    }
  }, [t, d]), L = e.useCallback(async () => {
    if (t !== "previewing")
      return;
    const f = c.current, g = i.current;
    if (!f || !g)
      return;
    g.width = f.videoWidth, g.height = f.videoHeight;
    const u = g.getContext("2d");
    u && (u.drawImage(f, 0, 0), d(), l("uploading"), g.toBlob(async (_) => {
      if (!_) {
        l("idle");
        return;
      }
      const w = new FormData();
      w.append("photo", _, "capture.jpg"), await s(w), l("idle");
    }, "image/jpeg", 0.85));
  }, [t, s, d]);
  e.useEffect(() => {
    t === "previewing" && c.current && n.current && (c.current.srcObject = n.current);
  }, [t]), e.useEffect(() => () => {
    n.current && (n.current.getTracks().forEach((f) => f.stop()), n.current = null);
  }, []);
  const b = y(re), v = t === "previewing" ? b["js.photoCapture.close"] : t === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], E = t === "uploading", R = ["tlPhotoCapture__cameraBtn"];
  t === "previewing" && R.push("tlPhotoCapture__cameraBtn--active");
  const h = ["tlPhotoCapture__captureBtn"];
  return t === "uploading" && h.push("tlPhotoCapture__captureBtn--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: p,
      disabled: E,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  ), t === "previewing" && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: L,
      title: b["js.photoCapture.capture"],
      "aria-label": b["js.photoCapture.capture"]
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__captureIcon" })
  )), t === "previewing" && /* @__PURE__ */ e.createElement(
    "video",
    {
      ref: c,
      className: "tlPhotoCapture__preview",
      autoPlay: !0,
      muted: !0,
      playsInline: !0
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: i, style: { display: "none" } }), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[a]), m && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, m));
}, ce = {
  "js.photoViewer.alt": "Captured photo"
}, ie = () => {
  const r = k(), s = P(), t = !!r.hasPhoto, l = r.dataRevision ?? 0, [a, o] = e.useState(null), c = e.useRef(l);
  e.useEffect(() => {
    if (!t) {
      a && (URL.revokeObjectURL(a), o(null));
      return;
    }
    if (l === c.current && a)
      return;
    c.current = l, a && (URL.revokeObjectURL(a), o(null));
    let i = !1;
    return (async () => {
      try {
        const m = await fetch(s);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const d = await m.blob();
        i || o(URL.createObjectURL(d));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      i = !0;
    };
  }, [t, l, s]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const n = y(ce);
  return !t || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: n["js.photoViewer.alt"]
    }
  ));
};
C("TLButton", W);
C("TLToggleButton", q);
C("TLTextInput", A);
C("TLNumberInput", B);
C("TLDatePicker", x);
C("TLSelect", O);
C("TLCheckbox", $);
C("TLTable", K);
C("TLCounter", z);
C("TLTabBar", J);
C("TLFieldList", Q);
C("TLAudioRecorder", Z);
C("TLAudioPlayer", te);
C("TLFileUpload", le);
C("TLDownload", oe);
C("TLPhotoCapture", se);
C("TLPhotoViewer", ie);
