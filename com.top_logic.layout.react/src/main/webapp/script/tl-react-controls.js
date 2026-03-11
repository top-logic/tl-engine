import { React as e, useTLFieldValue as ee, getComponent as We, useTLState as I, useTLCommand as A, TLChild as j, useTLUpload as _e, useI18N as W, useTLDataUrl as ve, register as x } from "tl-react-bridge";
const { useCallback: He } = e, Ke = ({ controlId: a, state: t }) => {
  const [o, n] = ee(), s = He(
    (r) => {
      n(r.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: a,
      value: o ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Ge } = e, Ye = ({ controlId: a, state: t, config: o }) => {
  const [n, s] = ee(), r = Ge(
    (u) => {
      const i = u.target.value, c = i === "" ? null : Number(i);
      s(c);
    },
    [s]
  ), l = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: a,
      value: n != null ? String(n) : "",
      onChange: r,
      step: l,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Xe } = e, qe = ({ controlId: a, state: t }) => {
  const [o, n] = ee(), s = Xe(
    (r) => {
      n(r.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: a,
      value: o ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ze } = e, Je = ({ controlId: a, state: t, config: o }) => {
  var u;
  const [n, s] = ee(), r = Ze(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), l = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const i = ((u = l.find((c) => c.value === n)) == null ? void 0 : u.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: a, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: a,
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: Qe } = e, et = ({ controlId: a, state: t }) => {
  const [o, n] = ee(), s = Qe(
    (r) => {
      n(r.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: o === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: a,
      checked: o === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, tt = ({ controlId: a, state: t }) => {
  const o = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: a, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, o.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, r) => /* @__PURE__ */ e.createElement("tr", { key: r }, o.map((l) => {
    const u = l.cellModule ? We(l.cellModule) : void 0, i = s[l.name];
    if (u) {
      const c = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: l.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: a + "-" + r + "-" + l.name,
          state: c
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: l.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: nt } = e, lt = ({ controlId: a, command: t, label: o, disabled: n }) => {
  const s = I(), r = A(), l = t ?? "click", u = o ?? s.label, i = n ?? s.disabled === !0, c = nt(() => {
    r(l);
  }, [r, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: c,
      disabled: i,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: at } = e, ot = ({ controlId: a, command: t, label: o, active: n, disabled: s }) => {
  const r = I(), l = A(), u = t ?? "click", i = o ?? r.label, c = n ?? r.active === !0, m = s ?? r.disabled === !0, E = at(() => {
    l(u);
  }, [l, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: E,
      disabled: m,
      className: "tlReactButton" + (c ? " tlReactButtonActive" : "")
    },
    i
  );
}, rt = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: st } = e, ct = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.tabs ?? [], s = t.activeTabId, r = st((l) => {
    l !== s && o("selectTab", { tabId: l });
  }, [o, s]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === s,
      className: "tlReactTabBar__tab" + (l.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => r(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(j, { control: t.activeContent })));
}, it = ({ controlId: a }) => {
  const t = I(), o = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((s, r) => /* @__PURE__ */ e.createElement("div", { key: r, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(j, { control: s })))));
}, ut = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, dt = ({ controlId: a }) => {
  const t = I(), o = _e(), [n, s] = e.useState("idle"), [r, l] = e.useState(null), u = e.useRef(null), i = e.useRef([]), c = e.useRef(null), m = t.status ?? "idle", E = t.error, _ = m === "received" ? "idle" : n !== "idle" ? n : m, g = e.useCallback(async () => {
    if (n === "recording") {
      const k = u.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (n !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = k, i.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, M ? { mimeType: M } : void 0);
        u.current = P, P.ondataavailable = (d) => {
          d.data.size > 0 && i.current.push(d.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((T) => T.stop()), c.current = null;
          const d = new Blob(i.current, { type: P.mimeType || "audio/webm" });
          if (i.current = [], d.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const v = new FormData();
          v.append("audio", d, "recording.webm"), await o(v), s("idle");
        }, P.start(), s("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), l("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [n, o]), b = W(ut), p = _ === "recording" ? b["js.audioRecorder.stop"] : _ === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], f = _ === "uploading", L = ["tlAudioRecorder__button"];
  return _ === "recording" && L.push("tlAudioRecorder__button--recording"), _ === "uploading" && L.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: g,
      disabled: f,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[r]), E && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E));
}, mt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, pt = ({ controlId: a }) => {
  const t = I(), o = ve(), n = !!t.hasAudio, s = t.dataRevision ?? 0, [r, l] = e.useState(n ? "idle" : "disabled"), u = e.useRef(null), i = e.useRef(null), c = e.useRef(s);
  e.useEffect(() => {
    n ? r === "disabled" && l("idle") : (u.current && (u.current.pause(), u.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), l("disabled"));
  }, [n]), e.useEffect(() => {
    s !== c.current && (c.current = s, u.current && (u.current.pause(), u.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (r === "playing" || r === "paused" || r === "loading") && l("idle"));
  }, [s]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (r === "disabled" || r === "loading")
      return;
    if (r === "playing") {
      u.current && u.current.pause(), l("paused");
      return;
    }
    if (r === "paused" && u.current) {
      u.current.play(), l("playing");
      return;
    }
    if (!i.current) {
      l("loading");
      try {
        const f = await fetch(o);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), l("idle");
          return;
        }
        const L = await f.blob();
        i.current = URL.createObjectURL(L);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), l("idle");
        return;
      }
    }
    const p = new Audio(i.current);
    u.current = p, p.onended = () => {
      l("idle");
    }, p.play(), l("playing");
  }, [r, o]), E = W(mt), _ = r === "loading" ? E["js.loading"] : r === "playing" ? E["js.audioPlayer.pause"] : r === "disabled" ? E["js.audioPlayer.noAudio"] : E["js.audioPlayer.play"], g = r === "disabled" || r === "loading", b = ["tlAudioPlayer__button"];
  return r === "playing" && b.push("tlAudioPlayer__button--playing"), r === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: g,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${r === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ft = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, bt = ({ controlId: a }) => {
  const t = I(), o = _e(), [n, s] = e.useState("idle"), [r, l] = e.useState(!1), u = e.useRef(null), i = t.status ?? "idle", c = t.error, m = t.accept ?? "", E = i === "received" ? "idle" : n !== "idle" ? n : i, _ = e.useCallback(async (d) => {
    s("uploading");
    const v = new FormData();
    v.append("file", d, d.name), await o(v), s("idle");
  }, [o]), g = e.useCallback((d) => {
    var T;
    const v = (T = d.target.files) == null ? void 0 : T[0];
    v && _(v);
  }, [_]), b = e.useCallback(() => {
    var d;
    n !== "uploading" && ((d = u.current) == null || d.click());
  }, [n]), p = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), l(!0);
  }, []), f = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), l(!1);
  }, []), L = e.useCallback((d) => {
    var T;
    if (d.preventDefault(), d.stopPropagation(), l(!1), n === "uploading") return;
    const v = (T = d.dataTransfer.files) == null ? void 0 : T[0];
    v && _(v);
  }, [n, _]), k = E === "uploading", M = W(ft), P = E === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${r ? " tlFileUpload--dragover" : ""}`,
      onDragOver: p,
      onDragLeave: f,
      onDrop: L
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: g,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (E === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: k,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, ht = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, _t = ({ controlId: a }) => {
  const t = I(), o = ve(), n = A(), s = !!t.hasData, r = t.dataRevision ?? 0, l = t.fileName ?? "download", u = !!t.clearable, [i, c] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!s || i)) {
      c(!0);
      try {
        const b = o + (o.includes("?") ? "&" : "?") + "rev=" + r, p = await fetch(b);
        if (!p.ok) {
          console.error("[TLDownload] Failed to fetch data:", p.status);
          return;
        }
        const f = await p.blob(), L = URL.createObjectURL(f), k = document.createElement("a");
        k.href = L, k.download = l, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(L);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        c(!1);
      }
    }
  }, [s, i, o, r, l]), E = e.useCallback(async () => {
    s && await n("clear");
  }, [s, n]), _ = W(ht);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const g = i ? _["js.downloading"] : _["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: i,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: E,
      title: _["js.download.clear"],
      "aria-label": _["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, vt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Et = ({ controlId: a }) => {
  const t = I(), o = _e(), [n, s] = e.useState("idle"), [r, l] = e.useState(null), [u, i] = e.useState(!1), c = e.useRef(null), m = e.useRef(null), E = e.useRef(null), _ = e.useRef(null), g = e.useRef(null), b = t.error, p = e.useMemo(
    () => {
      var w;
      return !!(window.isSecureContext && ((w = navigator.mediaDevices) != null && w.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((w) => w.stop()), m.current = null), c.current && (c.current.srcObject = null);
  }, []), L = e.useCallback(() => {
    f(), s("idle");
  }, [f]), k = e.useCallback(async () => {
    var w;
    if (n !== "uploading") {
      if (l(null), !p) {
        (w = _.current) == null || w.click();
        return;
      }
      try {
        const S = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = S, s("overlayOpen");
      } catch (S) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", S), l("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [n, p]), M = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const w = c.current, S = E.current;
    if (!w || !S)
      return;
    S.width = w.videoWidth, S.height = w.videoHeight;
    const N = S.getContext("2d");
    N && (N.drawImage(w, 0, 0), f(), s("uploading"), S.toBlob(async (D) => {
      if (!D) {
        s("idle");
        return;
      }
      const F = new FormData();
      F.append("photo", D, "capture.jpg"), await o(F), s("idle");
    }, "image/jpeg", 0.85));
  }, [n, o, f]), P = e.useCallback(async (w) => {
    var D;
    const S = (D = w.target.files) == null ? void 0 : D[0];
    if (!S) return;
    s("uploading");
    const N = new FormData();
    N.append("photo", S, S.name), await o(N), s("idle"), _.current && (_.current.value = "");
  }, [o]);
  e.useEffect(() => {
    n === "overlayOpen" && c.current && m.current && (c.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var S;
    if (n !== "overlayOpen") return;
    (S = g.current) == null || S.focus();
    const w = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = w;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const w = (S) => {
      S.key === "Escape" && L();
    };
    return document.addEventListener("keydown", w), () => document.removeEventListener("keydown", w);
  }, [n, L]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((w) => w.stop()), m.current = null);
  }, []);
  const d = W(vt), v = n === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], T = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && T.push("tlPhotoCapture__cameraBtn--uploading");
  const B = ["tlPhotoCapture__overlayVideo"];
  u && B.push("tlPhotoCapture__overlayVideo--mirrored");
  const C = ["tlPhotoCapture__mirrorBtn"];
  return u && C.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: k,
      disabled: n === "uploading",
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !p && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: _,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: E, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: g,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: L }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: B.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: C.join(" "),
        onClick: () => i((w) => !w),
        title: d["js.photoCapture.mirror"],
        "aria-label": d["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: M,
        title: d["js.photoCapture.capture"],
        "aria-label": d["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: L,
        title: d["js.photoCapture.close"],
        "aria-label": d["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, d[r]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Ct = {
  "js.photoViewer.alt": "Captured photo"
}, gt = ({ controlId: a }) => {
  const t = I(), o = ve(), n = !!t.hasPhoto, s = t.dataRevision ?? 0, [r, l] = e.useState(null), u = e.useRef(s);
  e.useEffect(() => {
    if (!n) {
      r && (URL.revokeObjectURL(r), l(null));
      return;
    }
    if (s === u.current && r)
      return;
    u.current = s, r && (URL.revokeObjectURL(r), l(null));
    let c = !1;
    return (async () => {
      try {
        const m = await fetch(o);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const E = await m.blob();
        c || l(URL.createObjectURL(E));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      c = !0;
    };
  }, [n, s, o]), e.useEffect(() => () => {
    r && URL.revokeObjectURL(r);
  }, []);
  const i = W(Ct);
  return !n || !r ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: r,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ce, useRef: ce } = e, yt = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.orientation, s = t.resizable === !0, r = t.children ?? [], l = n === "horizontal", u = r.length > 0 && r.every((f) => f.collapsed), i = !u && r.some((f) => f.collapsed), c = u ? !l : l, m = ce(null), E = ce(null), _ = ce(null), g = Ce((f, L) => {
    const k = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? u && !c ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : L !== void 0 ? k.flex = `0 0 ${L}px` : f.unit === "%" || i ? k.flex = `${f.size} 0 0%` : k.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (k.minWidth = l ? f.minSize : void 0, k.minHeight = l ? void 0 : f.minSize), k;
  }, [l, u, i, c]), b = Ce((f, L) => {
    f.preventDefault();
    const k = m.current;
    if (!k) return;
    const M = r[L], P = r[L + 1], d = k.querySelectorAll(":scope > .tlSplitPanel__child"), v = [];
    d.forEach((C) => {
      v.push(l ? C.offsetWidth : C.offsetHeight);
    }), _.current = v, E.current = {
      splitterIndex: L,
      startPos: l ? f.clientX : f.clientY,
      startSizeBefore: v[L],
      startSizeAfter: v[L + 1],
      childBefore: M,
      childAfter: P
    };
    const T = (C) => {
      const w = E.current;
      if (!w || !_.current) return;
      const N = (l ? C.clientX : C.clientY) - w.startPos, D = w.childBefore.minSize || 0, F = w.childAfter.minSize || 0;
      let V = w.startSizeBefore + N, G = w.startSizeAfter - N;
      V < D && (G += V - D, V = D), G < F && (V += G - F, G = F), _.current[w.splitterIndex] = V, _.current[w.splitterIndex + 1] = G;
      const J = k.querySelectorAll(":scope > .tlSplitPanel__child"), Q = J[w.splitterIndex], X = J[w.splitterIndex + 1];
      Q && (Q.style.flex = `0 0 ${V}px`), X && (X.style.flex = `0 0 ${G}px`);
    }, B = () => {
      if (document.removeEventListener("mousemove", T), document.removeEventListener("mouseup", B), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const C = {};
        r.forEach((w, S) => {
          const N = w.control;
          N != null && N.controlId && _.current && (C[N.controlId] = _.current[S]);
        }), o("updateSizes", { sizes: C });
      }
      _.current = null, E.current = null;
    };
    document.addEventListener("mousemove", T), document.addEventListener("mouseup", B), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [r, l, o]), p = [];
  return r.forEach((f, L) => {
    if (p.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${L}`,
          className: `tlSplitPanel__child${f.collapsed && c ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: g(f)
        },
        /* @__PURE__ */ e.createElement(j, { control: f.control })
      )
    ), s && L < r.length - 1) {
      const k = r[L + 1];
      !f.collapsed && !k.collapsed && p.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${L}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => b(P, L)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: a,
      className: `tlSplitPanel tlSplitPanel--${n}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: c ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    p
  );
}, { useCallback: ie } = e, kt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, wt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), St = ({ controlId: a }) => {
  const t = I(), o = A(), n = W(kt), s = t.title, r = t.expansionState ?? "NORMALIZED", l = t.showMinimize === !0, u = t.showMaximize === !0, i = t.showPopOut === !0, c = t.toolbarButtons ?? [], m = r === "MINIMIZED", E = r === "MAXIMIZED", _ = r === "HIDDEN", g = ie(() => {
    o("toggleMinimize");
  }, [o]), b = ie(() => {
    o("toggleMaximize");
  }, [o]), p = ie(() => {
    o("popOut");
  }, [o]);
  if (_)
    return null;
  const f = E ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${r.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, c.map((L, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(j, { control: L }))), l && !E && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: g,
        title: m ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Nt, null) : /* @__PURE__ */ e.createElement(wt, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: E ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      E ? /* @__PURE__ */ e.createElement(Tt, null) : /* @__PURE__ */ e.createElement(Lt, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: p,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(xt, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(j, { control: t.child }))
  );
}, Rt = ({ controlId: a }) => {
  const t = I();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(j, { control: t.child })
  );
}, Dt = ({ controlId: a }) => {
  const t = I();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(j, { control: t.activeChild }));
}, { useCallback: K, useState: le, useEffect: ae, useRef: oe } = e, It = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function be(a, t, o, n) {
  const s = [];
  for (const r of a)
    r.type === "nav" ? s.push({ id: r.id, type: "nav", groupId: n }) : r.type === "command" ? s.push({ id: r.id, type: "command", groupId: n }) : r.type === "group" && (s.push({ id: r.id, type: "group" }), (o.get(r.id) ?? r.expanded) && !t && s.push(...be(r.children, t, o, r.id)));
  return s;
}
const Z = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, Mt = ({ item: a, active: t, collapsed: o, onSelect: n, tabIndex: s, itemRef: r, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(a.id),
    title: o ? a.label : void 0,
    tabIndex: s,
    ref: r,
    onFocus: () => l(a.id)
  },
  o && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Z, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(Z, { icon: a.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
  !o && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
), Pt = ({ item: a, collapsed: t, onExecute: o, tabIndex: n, itemRef: s, onFocus: r }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => o(a.id),
    title: t ? a.label : void 0,
    tabIndex: n,
    ref: s,
    onFocus: () => r(a.id)
  },
  /* @__PURE__ */ e.createElement(Z, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), jt = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(Z, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), Bt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Ft = ({ item: a, activeItemId: t, anchorRect: o, onSelect: n, onExecute: s, onClose: r }) => {
  const l = oe(null);
  ae(() => {
    const c = (m) => {
      l.current && !l.current.contains(m.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [r]), ae(() => {
    const c = (m) => {
      m.key === "Escape" && r();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [r]);
  const u = K((c) => {
    c.type === "nav" ? (n(c.id), r()) : c.type === "command" && (s(c.id), r());
  }, [n, s, r]), i = {};
  return o && (i.left = o.right, i.top = o.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: l, role: "menu", style: i }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, a.label), a.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const m = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(c)
        },
        /* @__PURE__ */ e.createElement(Z, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, $t = ({
  item: a,
  expanded: t,
  activeItemId: o,
  collapsed: n,
  onSelect: s,
  onExecute: r,
  onToggleGroup: l,
  tabIndex: u,
  itemRef: i,
  onFocus: c,
  focusedId: m,
  setItemRef: E,
  onItemFocus: _,
  flyoutGroupId: g,
  onOpenFlyout: b,
  onCloseFlyout: p
}) => {
  const f = oe(null), [L, k] = le(null), M = K(() => {
    n ? g === a.id ? p() : (f.current && k(f.current.getBoundingClientRect()), b(a.id)) : l(a.id);
  }, [n, g, a.id, l, b, p]), P = K((v) => {
    f.current = v, i(v);
  }, [i]), d = n && g === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (d ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: n ? a.label : void 0,
      "aria-expanded": n ? d : t,
      tabIndex: u,
      ref: P,
      onFocus: () => c(a.id)
    },
    /* @__PURE__ */ e.createElement(Z, { icon: a.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
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
  ), d && /* @__PURE__ */ e.createElement(
    Ft,
    {
      item: a,
      activeItemId: o,
      anchorRect: L,
      onSelect: s,
      onExecute: r,
      onClose: p
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, a.children.map((v) => /* @__PURE__ */ e.createElement(
    Te,
    {
      key: v.id,
      item: v,
      activeItemId: o,
      collapsed: n,
      onSelect: s,
      onExecute: r,
      onToggleGroup: l,
      focusedId: m,
      setItemRef: E,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: g,
      onOpenFlyout: b,
      onCloseFlyout: p
    }
  ))));
}, Te = ({
  item: a,
  activeItemId: t,
  collapsed: o,
  onSelect: n,
  onExecute: s,
  onToggleGroup: r,
  focusedId: l,
  setItemRef: u,
  onItemFocus: i,
  groupStates: c,
  flyoutGroupId: m,
  onOpenFlyout: E,
  onCloseFlyout: _
}) => {
  switch (a.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Mt,
        {
          item: a,
          active: a.id === t,
          collapsed: o,
          onSelect: n,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: u(a.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Pt,
        {
          item: a,
          collapsed: o,
          onExecute: s,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: u(a.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(jt, { item: a, collapsed: o });
    case "separator":
      return /* @__PURE__ */ e.createElement(Bt, null);
    case "group": {
      const g = c ? c.get(a.id) ?? a.expanded : a.expanded;
      return /* @__PURE__ */ e.createElement(
        $t,
        {
          item: a,
          expanded: g,
          activeItemId: t,
          collapsed: o,
          onSelect: n,
          onExecute: s,
          onToggleGroup: r,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: u(a.id),
          onFocus: i,
          focusedId: l,
          setItemRef: u,
          onItemFocus: i,
          flyoutGroupId: m,
          onOpenFlyout: E,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, zt = ({ controlId: a }) => {
  const t = I(), o = A(), n = W(It), s = t.items ?? [], r = t.activeItemId, l = t.collapsed, [u, i] = le(() => {
    const C = /* @__PURE__ */ new Map(), w = (S) => {
      for (const N of S)
        N.type === "group" && (C.set(N.id, N.expanded), w(N.children));
    };
    return w(s), C;
  }), c = K((C) => {
    i((w) => {
      const S = new Map(w), N = S.get(C) ?? !1;
      return S.set(C, !N), o("toggleGroup", { itemId: C, expanded: !N }), S;
    });
  }, [o]), m = K((C) => {
    C !== r && o("selectItem", { itemId: C });
  }, [o, r]), E = K((C) => {
    o("executeCommand", { itemId: C });
  }, [o]), _ = K(() => {
    o("toggleCollapse", {});
  }, [o]), [g, b] = le(null), p = K((C) => {
    b(C);
  }, []), f = K(() => {
    b(null);
  }, []);
  ae(() => {
    l || b(null);
  }, [l]);
  const [L, k] = le(() => {
    const C = be(s, l, u);
    return C.length > 0 ? C[0].id : "";
  }), M = oe(/* @__PURE__ */ new Map()), P = K((C) => (w) => {
    w ? M.current.set(C, w) : M.current.delete(C);
  }, []), d = K((C) => {
    k(C);
  }, []), v = oe(0), T = K((C) => {
    k(C), v.current++;
  }, []);
  ae(() => {
    const C = M.current.get(L);
    C && document.activeElement !== C && C.focus();
  }, [L, v.current]);
  const B = K((C) => {
    if (C.key === "Escape" && g !== null) {
      C.preventDefault(), f();
      return;
    }
    const w = be(s, l, u);
    if (w.length === 0) return;
    const S = w.findIndex((D) => D.id === L);
    if (S < 0) return;
    const N = w[S];
    switch (C.key) {
      case "ArrowDown": {
        C.preventDefault();
        const D = (S + 1) % w.length;
        T(w[D].id);
        break;
      }
      case "ArrowUp": {
        C.preventDefault();
        const D = (S - 1 + w.length) % w.length;
        T(w[D].id);
        break;
      }
      case "Home": {
        C.preventDefault(), T(w[0].id);
        break;
      }
      case "End": {
        C.preventDefault(), T(w[w.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        C.preventDefault(), N.type === "nav" ? m(N.id) : N.type === "command" ? E(N.id) : N.type === "group" && (l ? g === N.id ? f() : p(N.id) : c(N.id));
        break;
      }
      case "ArrowRight": {
        N.type === "group" && !l && ((u.get(N.id) ?? !1) || (C.preventDefault(), c(N.id)));
        break;
      }
      case "ArrowLeft": {
        N.type === "group" && !l && (u.get(N.id) ?? !1) && (C.preventDefault(), c(N.id));
        break;
      }
    }
  }, [
    s,
    l,
    u,
    L,
    g,
    T,
    m,
    E,
    c,
    p,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, l ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(j, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(j, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: B }, s.map((C) => /* @__PURE__ */ e.createElement(
    Te,
    {
      key: C.id,
      item: C,
      activeItemId: r,
      collapsed: l,
      onSelect: m,
      onExecute: E,
      onToggleGroup: c,
      focusedId: L,
      setItemRef: P,
      onItemFocus: d,
      groupStates: u,
      flyoutGroupId: g,
      onOpenFlyout: p,
      onCloseFlyout: f
    }
  ))), l ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(j, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(j, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
      title: l ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(j, { control: t.activeContent })));
}, At = ({ controlId: a }) => {
  const t = I(), o = t.direction ?? "column", n = t.gap ?? "default", s = t.align ?? "stretch", r = t.wrap === !0, l = t.children ?? [], u = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${s}`,
    r ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: u }, l.map((i, c) => /* @__PURE__ */ e.createElement(j, { key: c, control: i })));
}, Vt = ({ controlId: a }) => {
  const t = I(), o = t.columns, n = t.minColumnWidth, s = t.gap ?? "default", r = t.children ?? [], l = {};
  return n ? l.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : o && (l.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${s}`, style: l }, r.map((u, i) => /* @__PURE__ */ e.createElement(j, { key: i, control: u })));
}, Ot = ({ controlId: a }) => {
  const t = I(), o = t.title, n = t.variant ?? "outlined", s = t.padding ?? "default", r = t.headerActions ?? [], l = t.child, u = o != null || r.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${n}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, r.map((i, c) => /* @__PURE__ */ e.createElement(j, { key: c, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(j, { control: l })));
}, Ut = ({ controlId: a }) => {
  const t = I(), o = t.title ?? "", n = t.leading, s = t.actions ?? [], r = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    r === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: u }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(j, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, c) => /* @__PURE__ */ e.createElement(j, { key: c, control: i }))));
}, { useCallback: Wt } = e, Ht = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.items ?? [], s = Wt((r) => {
    o("navigate", { itemId: r });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((r, l) => {
    const u = l === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: r.id, className: "tlBreadcrumb__entry" }, l > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, r.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => s(r.id)
      },
      r.label
    ));
  })));
}, { useCallback: Kt } = e, Gt = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.items ?? [], s = t.activeItemId, r = Kt((l) => {
    l !== s && o("selectItem", { itemId: l });
  }, [o, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((l) => {
    const u = l.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: l.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => r(l.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + l.icon, "aria-hidden": "true" }), l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, l.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, l.label)
    );
  }));
}, { useCallback: ge, useEffect: ye, useRef: Yt } = e, Xt = {
  "js.dialog.close": "Close"
}, qt = ({ controlId: a }) => {
  const t = I(), o = A(), n = W(Xt), s = t.open === !0, r = t.title ?? "", l = t.size ?? "medium", u = t.closeOnBackdrop !== !1, i = t.actions ?? [], c = t.child, m = Yt(null), E = ge(() => {
    o("close");
  }, [o]), _ = ge((b) => {
    u && b.target === b.currentTarget && E();
  }, [u, E]);
  if (ye(() => {
    if (!s) return;
    const b = (p) => {
      p.key === "Escape" && E();
    };
    return document.addEventListener("keydown", b), () => document.removeEventListener("keydown", b);
  }, [s, E]), ye(() => {
    s && m.current && m.current.focus();
  }, [s]), !s) return null;
  const g = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDialog__backdrop", onClick: _ }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${l}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": g,
      ref: m,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: g }, r), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: E,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(j, { control: c })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((b, p) => /* @__PURE__ */ e.createElement(j, { key: p, control: b })))
  ));
}, { useCallback: Zt, useEffect: Jt } = e, Qt = {
  "js.drawer.close": "Close"
}, en = ({ controlId: a }) => {
  const t = I(), o = A(), n = W(Qt), s = t.open === !0, r = t.position ?? "right", l = t.size ?? "medium", u = t.title ?? null, i = t.child, c = Zt(() => {
    o("close");
  }, [o]);
  Jt(() => {
    if (!s) return;
    const E = (_) => {
      _.key === "Escape" && c();
    };
    return document.addEventListener("keydown", E), () => document.removeEventListener("keydown", E);
  }, [s, c]);
  const m = [
    "tlDrawer",
    `tlDrawer--${r}`,
    `tlDrawer--${l}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: a, className: m, "aria-hidden": !s }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: c,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(j, { control: i })));
}, { useCallback: ke, useEffect: tn, useState: nn } = e, ln = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.message ?? "", s = t.content ?? "", r = t.variant ?? "info", l = t.action, u = t.duration ?? 5e3, i = t.visible === !0, [c, m] = nn(!1), E = ke(() => {
    m(!0), setTimeout(() => {
      o("dismiss"), m(!1);
    }, 200);
  }, [o]), _ = ke(() => {
    l && o(l.commandName), E();
  }, [o, l, E]);
  return tn(() => {
    if (!i || u === 0) return;
    const g = setTimeout(E, u);
    return () => clearTimeout(g);
  }, [i, u, E]), !i && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlSnackbar tlSnackbar--${r}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    s ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: s } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    l && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: _ }, l.label)
  );
}, { useCallback: ue, useEffect: de, useRef: an, useState: we } = e, on = ({ controlId: a }) => {
  const t = I(), o = A(), n = t.open === !0, s = t.anchorId, r = t.items ?? [], l = an(null), [u, i] = we({ top: 0, left: 0 }), [c, m] = we(0), E = r.filter((p) => p.type === "item" && !p.disabled);
  de(() => {
    var d, v;
    if (!n || !s) return;
    const p = document.getElementById(s);
    if (!p) return;
    const f = p.getBoundingClientRect(), L = ((d = l.current) == null ? void 0 : d.offsetHeight) ?? 200, k = ((v = l.current) == null ? void 0 : v.offsetWidth) ?? 200;
    let M = f.bottom + 4, P = f.left;
    M + L > window.innerHeight && (M = f.top - L - 4), P + k > window.innerWidth && (P = f.right - k), i({ top: M, left: P }), m(0);
  }, [n, s]);
  const _ = ue(() => {
    o("close");
  }, [o]), g = ue((p) => {
    o("selectItem", { itemId: p });
  }, [o]);
  de(() => {
    if (!n) return;
    const p = (f) => {
      l.current && !l.current.contains(f.target) && _();
    };
    return document.addEventListener("mousedown", p), () => document.removeEventListener("mousedown", p);
  }, [n, _]);
  const b = ue((p) => {
    if (p.key === "Escape") {
      _();
      return;
    }
    if (p.key === "ArrowDown")
      p.preventDefault(), m((f) => (f + 1) % E.length);
    else if (p.key === "ArrowUp")
      p.preventDefault(), m((f) => (f - 1 + E.length) % E.length);
    else if (p.key === "Enter" || p.key === " ") {
      p.preventDefault();
      const f = E[c];
      f && g(f.id);
    }
  }, [_, g, E, c]);
  return de(() => {
    n && l.current && l.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: "tlMenu",
      role: "menu",
      ref: l,
      tabIndex: -1,
      style: { position: "fixed", top: u.top, left: u.left },
      onKeyDown: b
    },
    r.map((p, f) => {
      if (p.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const k = E.indexOf(p) === c;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: p.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (p.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: p.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => g(p.id)
        },
        p.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + p.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, p.label)
      );
    })
  ) : null;
}, rn = ({ controlId: a }) => {
  const t = I(), o = t.header, n = t.content, s = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(j, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(j, { control: n })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(j, { control: s })), /* @__PURE__ */ e.createElement(j, { control: r }));
}, sn = () => {
  const t = I().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, cn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ne = 50, un = () => {
  const a = I(), t = A(), o = W(cn), n = a.columns ?? [], s = a.totalRowCount ?? 0, r = a.rows ?? [], l = a.rowHeight ?? 36, u = a.selectionMode ?? "single", i = a.selectedCount ?? 0, c = a.frozenColumnCount ?? 0, m = a.treeMode ?? !1, E = e.useMemo(
    () => n.filter((h) => h.sortPriority && h.sortPriority > 0).length,
    [n]
  ), _ = u === "multi", g = 40, b = 20, p = e.useRef(null), f = e.useRef(null), L = e.useRef(null), [k, M] = e.useState({}), P = e.useRef(null), d = e.useRef(!1), v = e.useRef(null), [T, B] = e.useState(null), [C, w] = e.useState(null);
  e.useEffect(() => {
    P.current || M({});
  }, [n]);
  const S = e.useCallback((h) => k[h.name] ?? h.width, [k]), N = e.useMemo(() => {
    const h = [];
    let y = _ && c > 0 ? g : 0;
    for (let R = 0; R < c && R < n.length; R++)
      h.push(y), y += S(n[R]);
    return h;
  }, [n, c, _, g, S]), D = s * l, F = e.useCallback((h, y, R) => {
    R.preventDefault(), R.stopPropagation(), P.current = { column: h, startX: R.clientX, startWidth: y };
    const $ = (U) => {
      const H = P.current;
      if (!H) return;
      const O = Math.max(Ne, H.startWidth + (U.clientX - H.startX));
      M((se) => ({ ...se, [H.column]: O }));
    }, z = (U) => {
      document.removeEventListener("mousemove", $), document.removeEventListener("mouseup", z);
      const H = P.current;
      if (H) {
        const O = Math.max(Ne, H.startWidth + (U.clientX - H.startX));
        t("columnResize", { column: H.column, width: O }), P.current = null, d.current = !0, requestAnimationFrame(() => {
          d.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", $), document.addEventListener("mouseup", z);
  }, [t]), V = e.useCallback(() => {
    p.current && f.current && (p.current.scrollLeft = f.current.scrollLeft), L.current !== null && clearTimeout(L.current), L.current = window.setTimeout(() => {
      const h = f.current;
      if (!h) return;
      const y = h.scrollTop, R = Math.ceil(h.clientHeight / l), $ = Math.floor(y / l);
      t("scroll", { start: $, count: R });
    }, 80);
  }, [t, l]), G = e.useCallback((h, y, R) => {
    if (d.current) return;
    let $;
    !y || y === "desc" ? $ = "asc" : $ = "desc";
    const z = R.shiftKey ? "add" : "replace";
    t("sort", { column: h, direction: $, mode: z });
  }, [t]), J = e.useCallback((h, y) => {
    v.current = h, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", h);
  }, []), Q = e.useCallback((h, y) => {
    if (!v.current || v.current === h) {
      B(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const R = y.currentTarget.getBoundingClientRect(), $ = y.clientX < R.left + R.width / 2 ? "left" : "right";
    B({ column: h, side: $ });
  }, []), X = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation();
    const y = v.current;
    if (!y || !T) {
      v.current = null, B(null);
      return;
    }
    let R = n.findIndex((z) => z.name === T.column);
    if (R < 0) {
      v.current = null, B(null);
      return;
    }
    const $ = n.findIndex((z) => z.name === y);
    T.side === "right" && R++, $ < R && R--, t("columnReorder", { column: y, targetIndex: R }), v.current = null, B(null);
  }, [n, T, t]), Ie = e.useCallback(() => {
    v.current = null, B(null);
  }, []), Me = e.useCallback((h, y) => {
    y.shiftKey && y.preventDefault(), t("select", {
      rowIndex: h,
      ctrlKey: y.ctrlKey || y.metaKey,
      shiftKey: y.shiftKey
    });
  }, [t]), Pe = e.useCallback((h, y) => {
    y.stopPropagation(), t("select", { rowIndex: h, ctrlKey: !0, shiftKey: !1 });
  }, [t]), je = e.useCallback(() => {
    const h = i === s && s > 0;
    t("selectAll", { selected: !h });
  }, [t, i, s]), Be = e.useCallback((h, y, R) => {
    R.stopPropagation(), t("expand", { rowIndex: h, expanded: y });
  }, [t]), Fe = e.useCallback((h, y) => {
    y.preventDefault(), w({ x: y.clientX, y: y.clientY, colIdx: h });
  }, []), $e = e.useCallback(() => {
    C && (t("setFrozenColumnCount", { count: C.colIdx + 1 }), w(null));
  }, [C, t]), ze = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), w(null);
  }, [t]);
  e.useEffect(() => {
    if (!C) return;
    const h = () => w(null), y = (R) => {
      R.key === "Escape" && w(null);
    };
    return document.addEventListener("mousedown", h), document.addEventListener("keydown", y), () => {
      document.removeEventListener("mousedown", h), document.removeEventListener("keydown", y);
    };
  }, [C]);
  const re = n.reduce((h, y) => h + S(y), 0) + (_ ? g : 0), Ae = i === s && s > 0, Ee = i > 0 && i < s, Ve = e.useCallback((h) => {
    h && (h.indeterminate = Ee);
  }, [Ee]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (h) => {
        if (!v.current) return;
        h.preventDefault();
        const y = f.current, R = p.current;
        if (!y) return;
        const $ = y.getBoundingClientRect(), z = 40, U = 8;
        h.clientX < $.left + z ? y.scrollLeft = Math.max(0, y.scrollLeft - U) : h.clientX > $.right - z && (y.scrollLeft += U), R && (R.scrollLeft = y.scrollLeft);
      },
      onDrop: X
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: p }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: re } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: g,
          minWidth: g,
          ...c > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (h) => {
          v.current && (h.preventDefault(), h.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== v.current && B({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ve,
          className: "tlTableView__checkbox",
          checked: Ae,
          onChange: je
        }
      )
    ), n.map((h, y) => {
      const R = S(h), $ = y === n.length - 1;
      let z = "tlTableView__headerCell";
      h.sortable && (z += " tlTableView__headerCell--sortable"), T && T.column === h.name && (z += " tlTableView__headerCell--dragOver-" + T.side);
      const U = y < c, H = y === c - 1;
      return U && (z += " tlTableView__headerCell--frozen"), H && (z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: h.name,
          className: z,
          style: {
            ...$ && !U ? { flex: "1 0 auto", minWidth: R } : { width: R, minWidth: R },
            position: U ? "sticky" : "relative",
            ...U ? { left: N[y], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: h.sortable ? (O) => G(h.name, h.sortDirection, O) : void 0,
          onContextMenu: (O) => Fe(y, O),
          onDragStart: (O) => J(h.name, O),
          onDragOver: (O) => Q(h.name, O),
          onDrop: X,
          onDragEnd: Ie
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, h.label),
        h.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, h.sortDirection === "asc" ? "▲" : "▼", E > 1 && h.sortPriority != null && h.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, h.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (O) => F(h.name, R, O)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (h) => {
          if (v.current && n.length > 0) {
            const y = n[n.length - 1];
            y.name !== v.current && (h.preventDefault(), h.dataTransfer.dropEffect = "move", B({ column: y.name, side: "right" }));
          }
        },
        onDrop: X
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: V
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: D, position: "relative", minWidth: re } }, r.map((h) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: h.id,
          className: "tlTableView__row" + (h.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: h.index * l,
            height: l,
            minWidth: re,
            width: "100%"
          },
          onClick: (y) => Me(h.index, y)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: g,
              minWidth: g,
              ...c > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (y) => y.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: h.selected,
              onChange: () => {
              },
              onClick: (y) => Pe(h.index, y),
              tabIndex: -1
            }
          )
        ),
        n.map((y, R) => {
          const $ = S(y), z = R === n.length - 1, U = R < c, H = R === c - 1;
          let O = "tlTableView__cell";
          U && (O += " tlTableView__cell--frozen"), H && (O += " tlTableView__cell--frozenLast");
          const se = m && R === 0, Oe = h.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: y.name,
              className: O,
              style: {
                ...z && !U ? { flex: "1 0 auto", minWidth: $ } : { width: $, minWidth: $ },
                ...U ? { position: "sticky", left: N[R], zIndex: 2 } : {}
              }
            },
            se ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: Oe * b } }, h.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (Ue) => Be(h.index, !h.expanded, Ue)
              },
              h.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(j, { control: h.cells[y.name] })) : /* @__PURE__ */ e.createElement(j, { control: h.cells[y.name] })
          );
        })
      )))
    ),
    C && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: C.y, left: C.x, zIndex: 1e4 },
        onMouseDown: (h) => h.stopPropagation()
      },
      C.colIdx + 1 !== c && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: $e }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.freezeUpTo"])),
      c > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ze }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.unfreezeAll"]))
    )
  );
}, dn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, xe = e.createContext(dn), { useMemo: mn, useRef: pn, useState: fn, useEffect: bn } = e, hn = 320, _n = ({ controlId: a }) => {
  const t = I(), o = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", s = t.readOnly === !0, r = t.children ?? [], l = t.noModelMessage, u = pn(null), [i, c] = fn(
    n === "top" ? "top" : "side"
  );
  bn(() => {
    if (n !== "auto") {
      c(n);
      return;
    }
    const b = u.current;
    if (!b) return;
    const p = new ResizeObserver((f) => {
      for (const L of f) {
        const M = L.contentRect.width / o;
        c(M < hn ? "top" : "side");
      }
    });
    return p.observe(b), () => p.disconnect();
  }, [n, o]);
  const m = mn(() => ({
    readOnly: s,
    resolvedLabelPosition: i
  }), [s, i]), _ = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / o))}rem`}, 1fr))`
  }, g = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return l ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, l)) : /* @__PURE__ */ e.createElement(xe.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: a, className: g, style: _, ref: u }, r.map((b, p) => /* @__PURE__ */ e.createElement(j, { key: p, control: b }))));
}, { useCallback: vn } = e, En = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Cn = ({ controlId: a }) => {
  const t = I(), o = A(), n = W(En), s = t.header, r = t.headerActions ?? [], l = t.collapsible === !0, u = t.collapsed === !0, i = t.border ?? "none", c = t.fullLine === !0, m = t.children ?? [], E = s != null || r.length > 0 || l, _ = vn(() => {
    o("toggleCollapse");
  }, [o]), g = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    c ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: g }, E && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, l && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
      "aria-expanded": !u,
      title: u ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: u ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, r.map((b, p) => /* @__PURE__ */ e.createElement(j, { key: p, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, p) => /* @__PURE__ */ e.createElement(j, { key: p, control: b }))));
}, { useContext: gn, useState: yn, useCallback: kn } = e, wn = ({ controlId: a }) => {
  const t = I(), o = gn(xe), n = t.label ?? "", s = t.required === !0, r = t.error, l = t.helpText, u = t.dirty === !0, i = t.labelPosition ?? o.resolvedLabelPosition, c = t.fullLine === !0, m = t.visible !== !1, E = t.field, _ = o.readOnly, [g, b] = yn(!1), p = kn(() => b((k) => !k), []);
  if (!m) return null;
  const f = r != null, L = [
    "tlFormField",
    `tlFormField--${i}`,
    _ ? "tlFormField--readonly" : "",
    c ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    u ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), s && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), u && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), l && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: p,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(j, { control: E })), !_ && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, r)), !_ && l && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, l));
}, Nn = () => {
  const a = I(), t = A(), o = a.iconCss, n = a.iconSrc, s = a.label, r = a.cssClass, l = a.tooltip, u = a.hasLink, i = o ? /* @__PURE__ */ e.createElement("i", { className: o }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, c = /* @__PURE__ */ e.createElement(e.Fragment, null, i, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), E = ["tlResourceCell", r].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: E, href: "#", onClick: m, title: l }, c) : /* @__PURE__ */ e.createElement("span", { className: E, title: l }, c);
}, Ln = 20, Tn = () => {
  const a = I(), t = A(), o = a.nodes ?? [], n = a.selectionMode ?? "single", s = a.dragEnabled ?? !1, r = a.dropEnabled ?? !1, l = a.dropIndicatorNodeId ?? null, u = a.dropIndicatorPosition ?? null, [i, c] = e.useState(-1), m = e.useRef(null), E = e.useCallback((d, v) => {
    t(v ? "collapse" : "expand", { nodeId: d });
  }, [t]), _ = e.useCallback((d, v) => {
    t("select", {
      nodeId: d,
      ctrlKey: v.ctrlKey || v.metaKey,
      shiftKey: v.shiftKey
    });
  }, [t]), g = e.useCallback((d, v) => {
    v.preventDefault(), t("contextMenu", { nodeId: d, x: v.clientX, y: v.clientY });
  }, [t]), b = e.useRef(null), p = e.useCallback((d, v) => {
    const T = v.getBoundingClientRect(), B = d.clientY - T.top, C = T.height / 3;
    return B < C ? "above" : B > C * 2 ? "below" : "within";
  }, []), f = e.useCallback((d, v) => {
    v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", d);
  }, []), L = e.useCallback((d, v) => {
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const T = p(v, v.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: d, position: T }), b.current = null;
    }, 50);
  }, [t, p]), k = e.useCallback((d, v) => {
    v.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const T = p(v, v.currentTarget);
    t("drop", { nodeId: d, position: T });
  }, [t, p]), M = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((d) => {
    if (o.length === 0) return;
    let v = i;
    switch (d.key) {
      case "ArrowDown":
        d.preventDefault(), v = Math.min(i + 1, o.length - 1);
        break;
      case "ArrowUp":
        d.preventDefault(), v = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (d.preventDefault(), i >= 0 && i < o.length) {
          const T = o[i];
          if (T.expandable && !T.expanded) {
            t("expand", { nodeId: T.id });
            return;
          } else T.expanded && (v = i + 1);
        }
        break;
      case "ArrowLeft":
        if (d.preventDefault(), i >= 0 && i < o.length) {
          const T = o[i];
          if (T.expanded) {
            t("collapse", { nodeId: T.id });
            return;
          } else {
            const B = T.depth;
            for (let C = i - 1; C >= 0; C--)
              if (o[C].depth < B) {
                v = C;
                break;
              }
          }
        }
        break;
      case "Enter":
        d.preventDefault(), i >= 0 && i < o.length && t("select", {
          nodeId: o[i].id,
          ctrlKey: d.ctrlKey || d.metaKey,
          shiftKey: d.shiftKey
        });
        return;
      case " ":
        d.preventDefault(), n === "multi" && i >= 0 && i < o.length && t("select", {
          nodeId: o[i].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        d.preventDefault(), v = 0;
        break;
      case "End":
        d.preventDefault(), v = o.length - 1;
        break;
      default:
        return;
    }
    v !== i && c(v);
  }, [i, o, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    o.map((d, v) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: d.id,
        role: "treeitem",
        "aria-expanded": d.expandable ? d.expanded : void 0,
        "aria-selected": d.selected,
        "aria-level": d.depth + 1,
        className: [
          "tlTreeView__node",
          d.selected ? "tlTreeView__node--selected" : "",
          v === i ? "tlTreeView__node--focused" : "",
          l === d.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          l === d.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          l === d.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: d.depth * Ln },
        draggable: s,
        onClick: (T) => _(d.id, T),
        onContextMenu: (T) => g(d.id, T),
        onDragStart: (T) => f(d.id, T),
        onDragOver: r ? (T) => L(d.id, T) : void 0,
        onDrop: r ? (T) => k(d.id, T) : void 0,
        onDragEnd: M
      },
      d.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (T) => {
            T.stopPropagation(), E(d.id, d.expanded);
          },
          tabIndex: -1,
          "aria-label": d.expanded ? "Collapse" : "Expand"
        },
        d.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: d.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(j, { control: d.content }))
    ))
  );
}, { useCallback: me, useRef: xn } = e, Sn = ({
  colors: a,
  columns: t,
  onSelect: o,
  onConfirm: n,
  onSwap: s
}) => {
  const r = xn(null), l = me(
    (c) => (m) => {
      r.current = c, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), u = me((c) => {
    c.preventDefault(), c.dataTransfer.dropEffect = "move";
  }, []), i = me(
    (c) => (m) => {
      m.preventDefault(), r.current !== null && r.current !== c && s(r.current, c), r.current = null;
    },
    [s]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    a.map((c, m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m,
        className: "tlColorInput__paletteCell" + (c == null ? " tlColorInput__paletteCell--empty" : ""),
        style: c != null ? { backgroundColor: c } : void 0,
        title: c ?? "",
        draggable: c != null,
        onClick: c != null ? () => o(c) : void 0,
        onDoubleClick: c != null ? () => n(c) : void 0,
        onDragStart: c != null ? l(m) : void 0,
        onDragOver: u,
        onDrop: i(m)
      }
    ))
  );
};
function Se(a) {
  return Math.max(0, Math.min(255, Math.round(a)));
}
function he(a) {
  return /^#[0-9a-fA-F]{6}$/.test(a);
}
function Re(a) {
  if (!he(a)) return [0, 0, 0];
  const t = parseInt(a.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function De(a, t, o) {
  const n = (s) => Se(s).toString(16).padStart(2, "0");
  return "#" + n(a) + n(t) + n(o);
}
function Rn(a, t, o) {
  const n = a / 255, s = t / 255, r = o / 255, l = Math.max(n, s, r), u = Math.min(n, s, r), i = l - u;
  let c = 0;
  i !== 0 && (l === n ? c = (s - r) / i % 6 : l === s ? c = (r - n) / i + 2 : c = (n - s) / i + 4, c *= 60, c < 0 && (c += 360));
  const m = l === 0 ? 0 : i / l;
  return [c, m, l];
}
function Dn(a, t, o) {
  const n = o * t, s = n * (1 - Math.abs(a / 60 % 2 - 1)), r = o - n;
  let l = 0, u = 0, i = 0;
  return a < 60 ? (l = n, u = s, i = 0) : a < 120 ? (l = s, u = n, i = 0) : a < 180 ? (l = 0, u = n, i = s) : a < 240 ? (l = 0, u = s, i = n) : a < 300 ? (l = s, u = 0, i = n) : (l = n, u = 0, i = s), [
    Math.round((l + r) * 255),
    Math.round((u + r) * 255),
    Math.round((i + r) * 255)
  ];
}
function In(a) {
  return Rn(...Re(a));
}
function pe(a, t, o) {
  return De(...Dn(a, t, o));
}
const { useCallback: q, useRef: Le } = e, Mn = ({ color: a, onColorChange: t }) => {
  const [o, n, s] = In(a), r = Le(null), l = Le(null), u = q(
    (b, p) => {
      var M;
      const f = (M = r.current) == null ? void 0 : M.getBoundingClientRect();
      if (!f) return;
      const L = Math.max(0, Math.min(1, (b - f.left) / f.width)), k = Math.max(0, Math.min(1, 1 - (p - f.top) / f.height));
      t(pe(o, L, k));
    },
    [o, t]
  ), i = q(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), c = q(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = q(
    (b) => {
      var k;
      const p = (k = l.current) == null ? void 0 : k.getBoundingClientRect();
      if (!p) return;
      const L = Math.max(0, Math.min(1, (b - p.top) / p.height)) * 360;
      t(pe(L, n, s));
    },
    [n, s, t]
  ), E = q(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), _ = q(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), g = pe(o, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: r,
      className: "tlColorInput__svField",
      style: { backgroundColor: g },
      onPointerDown: i,
      onPointerMove: c
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - s) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: l,
      className: "tlColorInput__hueSlider",
      onPointerDown: E,
      onPointerMove: _
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${o / 360 * 100}%` }
      }
    )
  ));
}, Pn = {
  "js.colorInput.paletteTab": "Color Palette",
  "js.colorInput.mixerTab": "Color Mixer",
  "js.colorInput.current": "Current",
  "js.colorInput.new": "New",
  "js.colorInput.red": "Red",
  "js.colorInput.green": "Green",
  "js.colorInput.blue": "Blue",
  "js.colorInput.hex": "Hex",
  "js.colorInput.reset": "Reset",
  "js.colorInput.cancel": "Cancel",
  "js.colorInput.ok": "OK"
}, { useState: te, useCallback: Y, useEffect: fe, useRef: jn, useLayoutEffect: Bn } = e, Fn = ({
  anchorRef: a,
  currentColor: t,
  palette: o,
  paletteColumns: n,
  defaultPalette: s,
  onConfirm: r,
  onCancel: l,
  onPaletteChange: u
}) => {
  const [i, c] = te("palette"), [m, E] = te(t), _ = jn(null), g = W(Pn), [b, p] = te(null);
  Bn(() => {
    if (!a.current || !_.current) return;
    const N = a.current.getBoundingClientRect(), D = _.current.getBoundingClientRect();
    let F = N.bottom + 4, V = N.left;
    F + D.height > window.innerHeight && (F = N.top - D.height - 4), V + D.width > window.innerWidth && (V = Math.max(0, N.right - D.width)), p({ top: F, left: V });
  }, [a]);
  const [f, L, k] = Re(m), [M, P] = te(m.toUpperCase());
  fe(() => {
    P(m.toUpperCase());
  }, [m]), fe(() => {
    const N = (D) => {
      D.key === "Escape" && l();
    };
    return document.addEventListener("keydown", N), () => document.removeEventListener("keydown", N);
  }, [l]), fe(() => {
    const N = (F) => {
      _.current && !_.current.contains(F.target) && l();
    }, D = setTimeout(() => document.addEventListener("mousedown", N), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", N);
    };
  }, [l]);
  const d = Y(
    (N) => (D) => {
      const F = parseInt(D.target.value, 10);
      if (isNaN(F)) return;
      const V = Se(F);
      E(De(N === "r" ? V : f, N === "g" ? V : L, N === "b" ? V : k));
    },
    [f, L, k]
  ), v = Y((N) => {
    const D = N.target.value;
    P(D), he(D) && E(D);
  }, []), T = Y((N) => {
    E(N);
  }, []), B = Y(
    (N) => {
      r(N);
    },
    [r]
  ), C = Y(
    (N, D) => {
      const F = [...o], V = F[N];
      F[N] = F[D], F[D] = V, u(F);
    },
    [o, u]
  ), w = Y(() => {
    u([...s]);
  }, [s, u]), S = Y(() => {
    r(m);
  }, [m, r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: _,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (i === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => c("palette")
      },
      g["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (i === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => c("mixer")
      },
      g["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, i === "palette" ? /* @__PURE__ */ e.createElement(
      Sn,
      {
        colors: o,
        columns: n,
        onSelect: T,
        onConfirm: B,
        onSwap: C
      }
    ) : /* @__PURE__ */ e.createElement(Mn, { color: m, onColorChange: E }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.current"]), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: t } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, g["js.colorInput.new"]), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: m } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: f,
        onChange: d("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: L,
        onChange: d("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: k,
        onChange: d("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, g["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (he(M) ? "" : " tlColorInput__input--error"),
        type: "text",
        value: M,
        onChange: v
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i === "palette" && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: w }, g["js.colorInput.reset"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: l }, g["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: S }, g["js.colorInput.ok"]))))
  );
}, $n = { "js.colorInput.chooseColor": "Choose color" }, { useState: zn, useCallback: ne, useRef: An } = e, Vn = ({ controlId: a, state: t }) => {
  const o = A(), n = W($n), [s, r] = zn(!1), l = An(null), u = t.value, i = t.editable !== !1, c = t.palette ?? [], m = t.paletteColumns ?? 6, E = t.defaultPalette ?? c, _ = ne(() => {
    i && r(!0);
  }, [i]), g = ne(
    (f) => {
      r(!1), o("valueChanged", { value: f });
    },
    [o]
  ), b = ne(() => {
    r(!1);
  }, []), p = ne(
    (f) => {
      o("paletteChanged", { palette: f });
    },
    [o]
  );
  return i ? /* @__PURE__ */ e.createElement("span", { id: a, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: l,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), s && /* @__PURE__ */ e.createElement(
    Fn,
    {
      anchorRef: l,
      currentColor: u ?? "#000000",
      palette: c,
      paletteColumns: m,
      defaultPalette: E,
      onConfirm: g,
      onCancel: b,
      onPaletteChange: p
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: a,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
    }
  );
};
x("TLButton", lt);
x("TLToggleButton", ot);
x("TLTextInput", Ke);
x("TLNumberInput", Ye);
x("TLDatePicker", qe);
x("TLSelect", Je);
x("TLCheckbox", et);
x("TLTable", tt);
x("TLCounter", rt);
x("TLTabBar", ct);
x("TLFieldList", it);
x("TLAudioRecorder", dt);
x("TLAudioPlayer", pt);
x("TLFileUpload", bt);
x("TLDownload", _t);
x("TLPhotoCapture", Et);
x("TLPhotoViewer", gt);
x("TLSplitPanel", yt);
x("TLPanel", St);
x("TLMaximizeRoot", Rt);
x("TLDeckPane", Dt);
x("TLSidebar", zt);
x("TLStack", At);
x("TLGrid", Vt);
x("TLCard", Ot);
x("TLAppBar", Ut);
x("TLBreadcrumb", Ht);
x("TLBottomBar", Gt);
x("TLDialog", qt);
x("TLDrawer", en);
x("TLSnackbar", ln);
x("TLMenu", on);
x("TLAppShell", rn);
x("TLTextCell", sn);
x("TLTableView", un);
x("TLFormLayout", _n);
x("TLFormGroup", Cn);
x("TLFormField", wn);
x("TLResourceCell", Nn);
x("TLTreeView", Tn);
x("TLColorInput", Vn);
