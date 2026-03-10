import { React as e, useTLFieldValue as J, getComponent as We, useTLState as I, useTLCommand as z, TLChild as B, useTLUpload as _e, useI18N as W, useTLDataUrl as ve, register as x } from "tl-react-bridge";
const { useCallback: He } = e, Ke = ({ controlId: l, state: t }) => {
  const [o, n] = J(), r = He(
    (s) => {
      n(s.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: l,
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Ge } = e, Ye = ({ controlId: l, state: t, config: o }) => {
  const [n, r] = J(), s = Ge(
    (d) => {
      const i = d.target.value, c = i === "" ? null : Number(i);
      r(c);
    },
    [r]
  ), a = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: n != null ? String(n) : "",
      onChange: s,
      step: a,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Xe } = e, qe = ({ controlId: l, state: t }) => {
  const [o, n] = J(), r = Xe(
    (s) => {
      n(s.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: l,
      value: o ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ze } = e, Je = ({ controlId: l, state: t, config: o }) => {
  var d;
  const [n, r] = J(), s = Ze(
    (i) => {
      r(i.target.value || null);
    },
    [r]
  ), a = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const i = ((d = a.find((c) => c.value === n)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: Qe } = e, et = ({ controlId: l, state: t }) => {
  const [o, n] = J(), r = Qe(
    (s) => {
      n(s.target.checked);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: o === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: o === !0,
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, tt = ({ controlId: l, state: t }) => {
  const o = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, o.map((r) => /* @__PURE__ */ e.createElement("th", { key: r.name }, r.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((r, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, o.map((a) => {
    const d = a.cellModule ? We(a.cellModule) : void 0, i = r[a.name];
    if (d) {
      const c = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: a.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: l + "-" + s + "-" + a.name,
          state: c
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: a.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: nt } = e, at = ({ controlId: l, command: t, label: o, disabled: n }) => {
  const r = I(), s = z(), a = t ?? "click", d = o ?? r.label, i = n ?? r.disabled === !0, c = nt(() => {
    s(a);
  }, [s, a]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: c,
      disabled: i,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: lt } = e, ot = ({ controlId: l, command: t, label: o, active: n, disabled: r }) => {
  const s = I(), a = z(), d = t ?? "click", i = o ?? s.label, c = n ?? s.active === !0, p = r ?? s.disabled === !0, C = lt(() => {
    a(d);
  }, [a, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: p,
      className: "tlReactButton" + (c ? " tlReactButtonActive" : "")
    },
    i
  );
}, rt = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.count ?? 0, r = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: st } = e, ct = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.tabs ?? [], r = t.activeTabId, s = st((a) => {
    a !== r && o("selectTab", { tabId: a });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === r,
      className: "tlReactTabBar__tab" + (a.id === r ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(B, { control: t.activeContent })));
}, it = ({ controlId: l }) => {
  const t = I(), o = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((r, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(B, { control: r })))));
}, dt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, ut = ({ controlId: l }) => {
  const t = I(), o = _e(), [n, r] = e.useState("idle"), [s, a] = e.useState(null), d = e.useRef(null), i = e.useRef([]), c = e.useRef(null), p = t.status ?? "idle", C = t.error, E = p === "received" ? "idle" : n !== "idle" ? n : p, N = e.useCallback(async () => {
    if (n === "recording") {
      const k = d.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (n !== "uploading") {
      if (a(null), !window.isSecureContext || !navigator.mediaDevices) {
        a("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = k, i.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, M ? { mimeType: M } : void 0);
        d.current = P, P.ondataavailable = (u) => {
          u.data.size > 0 && i.current.push(u.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((L) => L.stop()), c.current = null;
          const u = new Blob(i.current, { type: P.mimeType || "audio/webm" });
          if (i.current = [], u.size === 0) {
            r("idle");
            return;
          }
          r("uploading");
          const v = new FormData();
          v.append("audio", u, "recording.webm"), await o(v), r("idle");
        }, P.start(), r("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), a("js.audioRecorder.error.denied"), r("idle");
      }
    }
  }, [n, o]), f = W(dt), m = E === "recording" ? f["js.audioRecorder.stop"] : E === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], b = E === "uploading", w = ["tlAudioRecorder__button"];
  return E === "recording" && w.push("tlAudioRecorder__button--recording"), E === "uploading" && w.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: N,
      disabled: b,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${E === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), C && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C));
}, mt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, pt = ({ controlId: l }) => {
  const t = I(), o = ve(), n = !!t.hasAudio, r = t.dataRevision ?? 0, [s, a] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), i = e.useRef(null), c = e.useRef(r);
  e.useEffect(() => {
    n ? s === "disabled" && a("idle") : (d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), a("disabled"));
  }, [n]), e.useEffect(() => {
    r !== c.current && (c.current = r, d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (s === "playing" || s === "paused" || s === "loading") && a("idle"));
  }, [r]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      d.current && d.current.pause(), a("paused");
      return;
    }
    if (s === "paused" && d.current) {
      d.current.play(), a("playing");
      return;
    }
    if (!i.current) {
      a("loading");
      try {
        const b = await fetch(o);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), a("idle");
          return;
        }
        const w = await b.blob();
        i.current = URL.createObjectURL(w);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), a("idle");
        return;
      }
    }
    const m = new Audio(i.current);
    d.current = m, m.onended = () => {
      a("idle");
    }, m.play(), a("playing");
  }, [s, o]), C = W(mt), E = s === "loading" ? C["js.loading"] : s === "playing" ? C["js.audioPlayer.pause"] : s === "disabled" ? C["js.audioPlayer.noAudio"] : C["js.audioPlayer.play"], N = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: p,
      disabled: N,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ft = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, bt = ({ controlId: l }) => {
  const t = I(), o = _e(), [n, r] = e.useState("idle"), [s, a] = e.useState(!1), d = e.useRef(null), i = t.status ?? "idle", c = t.error, p = t.accept ?? "", C = i === "received" ? "idle" : n !== "idle" ? n : i, E = e.useCallback(async (u) => {
    r("uploading");
    const v = new FormData();
    v.append("file", u, u.name), await o(v), r("idle");
  }, [o]), N = e.useCallback((u) => {
    var L;
    const v = (L = u.target.files) == null ? void 0 : L[0];
    v && E(v);
  }, [E]), f = e.useCallback(() => {
    var u;
    n !== "uploading" && ((u = d.current) == null || u.click());
  }, [n]), m = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!0);
  }, []), b = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!1);
  }, []), w = e.useCallback((u) => {
    var L;
    if (u.preventDefault(), u.stopPropagation(), a(!1), n === "uploading") return;
    const v = (L = u.dataTransfer.files) == null ? void 0 : L[0];
    v && E(v);
  }, [n, E]), k = C === "uploading", M = W(ft), P = C === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: m,
      onDragLeave: b,
      onDrop: w
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
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
        className: "tlFileUpload__button" + (C === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
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
}, _t = ({ controlId: l }) => {
  const t = I(), o = ve(), n = z(), r = !!t.hasData, s = t.dataRevision ?? 0, a = t.fileName ?? "download", d = !!t.clearable, [i, c] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!r || i)) {
      c(!0);
      try {
        const f = o + (o.includes("?") ? "&" : "?") + "rev=" + s, m = await fetch(f);
        if (!m.ok) {
          console.error("[TLDownload] Failed to fetch data:", m.status);
          return;
        }
        const b = await m.blob(), w = URL.createObjectURL(b), k = document.createElement("a");
        k.href = w, k.download = a, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(w);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        c(!1);
      }
    }
  }, [r, i, o, s, a]), C = e.useCallback(async () => {
    r && await n("clear");
  }, [r, n]), E = W(ht);
  if (!r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, E["js.download.noFile"]));
  const N = i ? E["js.downloading"] : E["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: i,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: C,
      title: E["js.download.clear"],
      "aria-label": E["js.download.clearFile"]
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
}, Et = ({ controlId: l }) => {
  const t = I(), o = _e(), [n, r] = e.useState("idle"), [s, a] = e.useState(null), [d, i] = e.useState(!1), c = e.useRef(null), p = e.useRef(null), C = e.useRef(null), E = e.useRef(null), N = e.useRef(null), f = t.error, m = e.useMemo(
    () => {
      var g;
      return !!(window.isSecureContext && ((g = navigator.mediaDevices) != null && g.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((g) => g.stop()), p.current = null), c.current && (c.current.srcObject = null);
  }, []), w = e.useCallback(() => {
    b(), r("idle");
  }, [b]), k = e.useCallback(async () => {
    var g;
    if (n !== "uploading") {
      if (a(null), !m) {
        (g = E.current) == null || g.click();
        return;
      }
      try {
        const T = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = T, r("overlayOpen");
      } catch (T) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", T), a("js.photoCapture.error.denied"), r("idle");
      }
    }
  }, [n, m]), M = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const g = c.current, T = C.current;
    if (!g || !T)
      return;
    T.width = g.videoWidth, T.height = g.videoHeight;
    const R = T.getContext("2d");
    R && (R.drawImage(g, 0, 0), b(), r("uploading"), T.toBlob(async (j) => {
      if (!j) {
        r("idle");
        return;
      }
      const H = new FormData();
      H.append("photo", j, "capture.jpg"), await o(H), r("idle");
    }, "image/jpeg", 0.85));
  }, [n, o, b]), P = e.useCallback(async (g) => {
    var j;
    const T = (j = g.target.files) == null ? void 0 : j[0];
    if (!T) return;
    r("uploading");
    const R = new FormData();
    R.append("photo", T, T.name), await o(R), r("idle"), E.current && (E.current.value = "");
  }, [o]);
  e.useEffect(() => {
    n === "overlayOpen" && c.current && p.current && (c.current.srcObject = p.current);
  }, [n]), e.useEffect(() => {
    var T;
    if (n !== "overlayOpen") return;
    (T = N.current) == null || T.focus();
    const g = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = g;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const g = (T) => {
      T.key === "Escape" && w();
    };
    return document.addEventListener("keydown", g), () => document.removeEventListener("keydown", g);
  }, [n, w]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((g) => g.stop()), p.current = null);
  }, []);
  const u = W(vt), v = n === "uploading" ? u["js.uploading"] : u["js.photoCapture.open"], L = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && L.push("tlPhotoCapture__cameraBtn--uploading");
  const S = ["tlPhotoCapture__overlayVideo"];
  d && S.push("tlPhotoCapture__overlayVideo--mirrored");
  const _ = ["tlPhotoCapture__mirrorBtn"];
  return d && _.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: k,
      disabled: n === "uploading",
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !m && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: E,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: C, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: w }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: S.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: _.join(" "),
        onClick: () => i((g) => !g),
        title: u["js.photoCapture.mirror"],
        "aria-label": u["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: M,
        title: u["js.photoCapture.capture"],
        "aria-label": u["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: w,
        title: u["js.photoCapture.close"],
        "aria-label": u["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, u[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, Ct = {
  "js.photoViewer.alt": "Captured photo"
}, gt = ({ controlId: l }) => {
  const t = I(), o = ve(), n = !!t.hasPhoto, r = t.dataRevision ?? 0, [s, a] = e.useState(null), d = e.useRef(r);
  e.useEffect(() => {
    if (!n) {
      s && (URL.revokeObjectURL(s), a(null));
      return;
    }
    if (r === d.current && s)
      return;
    d.current = r, s && (URL.revokeObjectURL(s), a(null));
    let c = !1;
    return (async () => {
      try {
        const p = await fetch(o);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const C = await p.blob();
        c || a(URL.createObjectURL(C));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      c = !0;
    };
  }, [n, r, o]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const i = W(Ct);
  return !n || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ce, useRef: se } = e, yt = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.orientation, r = t.resizable === !0, s = t.children ?? [], a = n === "horizontal", d = s.length > 0 && s.every((b) => b.collapsed), i = !d && s.some((b) => b.collapsed), c = d ? !a : a, p = se(null), C = se(null), E = se(null), N = Ce((b, w) => {
    const k = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? d && !c ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : w !== void 0 ? k.flex = `0 0 ${w}px` : b.unit === "%" || i ? k.flex = `${b.size} 0 0%` : k.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (k.minWidth = a ? b.minSize : void 0, k.minHeight = a ? void 0 : b.minSize), k;
  }, [a, d, i, c]), f = Ce((b, w) => {
    b.preventDefault();
    const k = p.current;
    if (!k) return;
    const M = s[w], P = s[w + 1], u = k.querySelectorAll(":scope > .tlSplitPanel__child"), v = [];
    u.forEach((_) => {
      v.push(a ? _.offsetWidth : _.offsetHeight);
    }), E.current = v, C.current = {
      splitterIndex: w,
      startPos: a ? b.clientX : b.clientY,
      startSizeBefore: v[w],
      startSizeAfter: v[w + 1],
      childBefore: M,
      childAfter: P
    };
    const L = (_) => {
      const g = C.current;
      if (!g || !E.current) return;
      const R = (a ? _.clientX : _.clientY) - g.startPos, j = g.childBefore.minSize || 0, H = g.childAfter.minSize || 0;
      let K = g.startSizeBefore + R, G = g.startSizeAfter - R;
      K < j && (G += K - j, K = j), G < H && (K += G - H, G = H), E.current[g.splitterIndex] = K, E.current[g.splitterIndex + 1] = G;
      const Q = k.querySelectorAll(":scope > .tlSplitPanel__child"), ee = Q[g.splitterIndex], X = Q[g.splitterIndex + 1];
      ee && (ee.style.flex = `0 0 ${K}px`), X && (X.style.flex = `0 0 ${G}px`);
    }, S = () => {
      if (document.removeEventListener("mousemove", L), document.removeEventListener("mouseup", S), document.body.style.cursor = "", document.body.style.userSelect = "", E.current) {
        const _ = {};
        s.forEach((g, T) => {
          const R = g.control;
          R != null && R.controlId && E.current && (_[R.controlId] = E.current[T]);
        }), o("updateSizes", { sizes: _ });
      }
      E.current = null, C.current = null;
    };
    document.addEventListener("mousemove", L), document.addEventListener("mouseup", S), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, a, o]), m = [];
  return s.forEach((b, w) => {
    if (m.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${w}`,
          className: `tlSplitPanel__child${b.collapsed && c ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(b)
        },
        /* @__PURE__ */ e.createElement(B, { control: b.control })
      )
    ), r && w < s.length - 1) {
      const k = s[w + 1];
      !b.collapsed && !k.collapsed && m.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${w}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => f(P, w)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: c ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    m
  );
}, { useCallback: ce } = e, kt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, wt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), Lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), St = ({ controlId: l }) => {
  const t = I(), o = z(), n = W(kt), r = t.title, s = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, d = t.showMaximize === !0, i = t.showPopOut === !0, c = t.toolbarButtons ?? [], p = s === "MINIMIZED", C = s === "MAXIMIZED", E = s === "HIDDEN", N = ce(() => {
    o("toggleMinimize");
  }, [o]), f = ce(() => {
    o("toggleMaximize");
  }, [o]), m = ce(() => {
    o("popOut");
  }, [o]);
  if (E)
    return null;
  const b = C ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}`,
      style: b
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, c.map((w, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(B, { control: w }))), a && !C && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: p ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(Nt, null) : /* @__PURE__ */ e.createElement(wt, null)
    ), d && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: C ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      C ? /* @__PURE__ */ e.createElement(Tt, null) : /* @__PURE__ */ e.createElement(Lt, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: m,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(xt, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(B, { control: t.child }))
  );
}, Rt = ({ controlId: l }) => {
  const t = I();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(B, { control: t.child })
  );
}, Dt = ({ controlId: l }) => {
  const t = I();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(B, { control: t.activeChild }));
}, { useCallback: U, useState: ne, useEffect: ae, useRef: le } = e, It = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function be(l, t, o, n) {
  const r = [];
  for (const s of l)
    s.type === "nav" ? r.push({ id: s.id, type: "nav", groupId: n }) : s.type === "command" ? r.push({ id: s.id, type: "command", groupId: n }) : s.type === "group" && (r.push({ id: s.id, type: "group" }), (o.get(s.id) ?? s.expanded) && !t && r.push(...be(s.children, t, o, s.id)));
  return r;
}
const Z = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Mt = ({ item: l, active: t, collapsed: o, onSelect: n, tabIndex: r, itemRef: s, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: o ? l.label : void 0,
    tabIndex: r,
    ref: s,
    onFocus: () => a(l.id)
  },
  o && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Z, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Z, { icon: l.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !o && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Pt = ({ item: l, collapsed: t, onExecute: o, tabIndex: n, itemRef: r, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => o(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: r,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Z, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Bt = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Z, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Ft = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), jt = ({ item: l, activeItemId: t, anchorRect: o, onSelect: n, onExecute: r, onClose: s }) => {
  const a = le(null);
  ae(() => {
    const c = (p) => {
      a.current && !a.current.contains(p.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), ae(() => {
    const c = (p) => {
      p.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const d = U((c) => {
    c.type === "nav" ? (n(c.id), s()) : c.type === "command" && (r(c.id), s());
  }, [n, r, s]), i = {};
  return o && (i.left = o.right, i.top = o.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu", style: i }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const p = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (p ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(c)
        },
        /* @__PURE__ */ e.createElement(Z, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, $t = ({
  item: l,
  expanded: t,
  activeItemId: o,
  collapsed: n,
  onSelect: r,
  onExecute: s,
  onToggleGroup: a,
  tabIndex: d,
  itemRef: i,
  onFocus: c,
  focusedId: p,
  setItemRef: C,
  onItemFocus: E,
  flyoutGroupId: N,
  onOpenFlyout: f,
  onCloseFlyout: m
}) => {
  const b = le(null), [w, k] = ne(null), M = U(() => {
    n ? N === l.id ? m() : (b.current && k(b.current.getBoundingClientRect()), f(l.id)) : a(l.id);
  }, [n, N, l.id, a, f, m]), P = U((v) => {
    b.current = v, i(v);
  }, [i]), u = n && N === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (u ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: n ? l.label : void 0,
      "aria-expanded": n ? u : t,
      tabIndex: d,
      ref: P,
      onFocus: () => c(l.id)
    },
    /* @__PURE__ */ e.createElement(Z, { icon: l.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
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
  ), u && /* @__PURE__ */ e.createElement(
    jt,
    {
      item: l,
      activeItemId: o,
      anchorRect: w,
      onSelect: r,
      onExecute: s,
      onClose: m
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((v) => /* @__PURE__ */ e.createElement(
    Te,
    {
      key: v.id,
      item: v,
      activeItemId: o,
      collapsed: n,
      onSelect: r,
      onExecute: s,
      onToggleGroup: a,
      focusedId: p,
      setItemRef: C,
      onItemFocus: E,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: f,
      onCloseFlyout: m
    }
  ))));
}, Te = ({
  item: l,
  activeItemId: t,
  collapsed: o,
  onSelect: n,
  onExecute: r,
  onToggleGroup: s,
  focusedId: a,
  setItemRef: d,
  onItemFocus: i,
  groupStates: c,
  flyoutGroupId: p,
  onOpenFlyout: C,
  onCloseFlyout: E
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Mt,
        {
          item: l,
          active: l.id === t,
          collapsed: o,
          onSelect: n,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        Pt,
        {
          item: l,
          collapsed: o,
          onExecute: r,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Bt, { item: l, collapsed: o });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ft, null);
    case "group": {
      const N = c ? c.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        $t,
        {
          item: l,
          expanded: N,
          activeItemId: t,
          collapsed: o,
          onSelect: n,
          onExecute: r,
          onToggleGroup: s,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: i,
          focusedId: a,
          setItemRef: d,
          onItemFocus: i,
          flyoutGroupId: p,
          onOpenFlyout: C,
          onCloseFlyout: E
        }
      );
    }
    default:
      return null;
  }
}, zt = ({ controlId: l }) => {
  const t = I(), o = z(), n = W(It), r = t.items ?? [], s = t.activeItemId, a = t.collapsed, [d, i] = ne(() => {
    const _ = /* @__PURE__ */ new Map(), g = (T) => {
      for (const R of T)
        R.type === "group" && (_.set(R.id, R.expanded), g(R.children));
    };
    return g(r), _;
  }), c = U((_) => {
    i((g) => {
      const T = new Map(g), R = T.get(_) ?? !1;
      return T.set(_, !R), o("toggleGroup", { itemId: _, expanded: !R }), T;
    });
  }, [o]), p = U((_) => {
    _ !== s && o("selectItem", { itemId: _ });
  }, [o, s]), C = U((_) => {
    o("executeCommand", { itemId: _ });
  }, [o]), E = U(() => {
    o("toggleCollapse", {});
  }, [o]), [N, f] = ne(null), m = U((_) => {
    f(_);
  }, []), b = U(() => {
    f(null);
  }, []);
  ae(() => {
    a || f(null);
  }, [a]);
  const [w, k] = ne(() => {
    const _ = be(r, a, d);
    return _.length > 0 ? _[0].id : "";
  }), M = le(/* @__PURE__ */ new Map()), P = U((_) => (g) => {
    g ? M.current.set(_, g) : M.current.delete(_);
  }, []), u = U((_) => {
    k(_);
  }, []), v = le(0), L = U((_) => {
    k(_), v.current++;
  }, []);
  ae(() => {
    const _ = M.current.get(w);
    _ && document.activeElement !== _ && _.focus();
  }, [w, v.current]);
  const S = U((_) => {
    if (_.key === "Escape" && N !== null) {
      _.preventDefault(), b();
      return;
    }
    const g = be(r, a, d);
    if (g.length === 0) return;
    const T = g.findIndex((j) => j.id === w);
    if (T < 0) return;
    const R = g[T];
    switch (_.key) {
      case "ArrowDown": {
        _.preventDefault();
        const j = (T + 1) % g.length;
        L(g[j].id);
        break;
      }
      case "ArrowUp": {
        _.preventDefault();
        const j = (T - 1 + g.length) % g.length;
        L(g[j].id);
        break;
      }
      case "Home": {
        _.preventDefault(), L(g[0].id);
        break;
      }
      case "End": {
        _.preventDefault(), L(g[g.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        _.preventDefault(), R.type === "nav" ? p(R.id) : R.type === "command" ? C(R.id) : R.type === "group" && (a ? N === R.id ? b() : m(R.id) : c(R.id));
        break;
      }
      case "ArrowRight": {
        R.type === "group" && !a && ((d.get(R.id) ?? !1) || (_.preventDefault(), c(R.id)));
        break;
      }
      case "ArrowLeft": {
        R.type === "group" && !a && (d.get(R.id) ?? !1) && (_.preventDefault(), c(R.id));
        break;
      }
    }
  }, [
    r,
    a,
    d,
    w,
    N,
    L,
    p,
    C,
    c,
    m,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(B, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(B, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: S }, r.map((_) => /* @__PURE__ */ e.createElement(
    Te,
    {
      key: _.id,
      item: _,
      activeItemId: s,
      collapsed: a,
      onSelect: p,
      onExecute: C,
      onToggleGroup: c,
      focusedId: w,
      setItemRef: P,
      onItemFocus: u,
      groupStates: d,
      flyoutGroupId: N,
      onOpenFlyout: m,
      onCloseFlyout: b
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(B, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(B, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: E,
      title: a ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(B, { control: t.activeContent })));
}, At = ({ controlId: l }) => {
  const t = I(), o = t.direction ?? "column", n = t.gap ?? "default", r = t.align ?? "stretch", s = t.wrap === !0, a = t.children ?? [], d = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${r}`,
    s ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, a.map((i, c) => /* @__PURE__ */ e.createElement(B, { key: c, control: i })));
}, Vt = ({ controlId: l }) => {
  const t = I(), o = t.columns, n = t.minColumnWidth, r = t.gap ?? "default", s = t.children ?? [], a = {};
  return n ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : o && (a.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${r}`, style: a }, s.map((d, i) => /* @__PURE__ */ e.createElement(B, { key: i, control: d })));
}, Ot = ({ controlId: l }) => {
  const t = I(), o = t.title, n = t.variant ?? "outlined", r = t.padding ?? "default", s = t.headerActions ?? [], a = t.child, d = o != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((i, c) => /* @__PURE__ */ e.createElement(B, { key: c, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${r}` }, /* @__PURE__ */ e.createElement(B, { control: a })));
}, Ut = ({ controlId: l }) => {
  const t = I(), o = t.title ?? "", n = t.leading, r = t.actions ?? [], s = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    s === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(B, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, r.map((i, c) => /* @__PURE__ */ e.createElement(B, { key: c, control: i }))));
}, { useCallback: Wt } = e, Ht = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.items ?? [], r = Wt((s) => {
    o("navigate", { itemId: s });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((s, a) => {
    const d = a === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, a > 0 && /* @__PURE__ */ e.createElement(
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
    ), d ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => r(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: Kt } = e, Gt = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.items ?? [], r = t.activeItemId, s = Kt((a) => {
    a !== r && o("selectItem", { itemId: a });
  }, [o, r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((a) => {
    const d = a.id === r;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: a.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => s(a.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + a.icon, "aria-hidden": "true" }), a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, a.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, a.label)
    );
  }));
}, { useCallback: ge, useEffect: ye, useRef: Yt } = e, Xt = {
  "js.dialog.close": "Close"
}, qt = ({ controlId: l }) => {
  const t = I(), o = z(), n = W(Xt), r = t.open === !0, s = t.title ?? "", a = t.size ?? "medium", d = t.closeOnBackdrop !== !1, i = t.actions ?? [], c = t.child, p = Yt(null), C = ge(() => {
    o("close");
  }, [o]), E = ge((f) => {
    d && f.target === f.currentTarget && C();
  }, [d, C]);
  if (ye(() => {
    if (!r) return;
    const f = (m) => {
      m.key === "Escape" && C();
    };
    return document.addEventListener("keydown", f), () => document.removeEventListener("keydown", f);
  }, [r, C]), ye(() => {
    r && p.current && p.current.focus();
  }, [r]), !r) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: E }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${a}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, s), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: C,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(B, { control: c })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((f, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: f })))
  ));
}, { useCallback: Zt, useEffect: Jt } = e, Qt = {
  "js.drawer.close": "Close"
}, en = ({ controlId: l }) => {
  const t = I(), o = z(), n = W(Qt), r = t.open === !0, s = t.position ?? "right", a = t.size ?? "medium", d = t.title ?? null, i = t.child, c = Zt(() => {
    o("close");
  }, [o]);
  Jt(() => {
    if (!r) return;
    const C = (E) => {
      E.key === "Escape" && c();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [r, c]);
  const p = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${a}`,
    r ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: p, "aria-hidden": !r }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(B, { control: i })));
}, { useCallback: ke, useEffect: tn, useState: nn } = e, an = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.message ?? "", r = t.content ?? "", s = t.variant ?? "info", a = t.action, d = t.duration ?? 5e3, i = t.visible === !0, [c, p] = nn(!1), C = ke(() => {
    p(!0), setTimeout(() => {
      o("dismiss"), p(!1);
    }, 200);
  }, [o]), E = ke(() => {
    a && o(a.commandName), C();
  }, [o, a, C]);
  return tn(() => {
    if (!i || d === 0) return;
    const N = setTimeout(C, d);
    return () => clearTimeout(N);
  }, [i, d, C]), !i && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    r ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: r } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: E }, a.label)
  );
}, { useCallback: ie, useEffect: de, useRef: ln, useState: we } = e, on = ({ controlId: l }) => {
  const t = I(), o = z(), n = t.open === !0, r = t.anchorId, s = t.items ?? [], a = ln(null), [d, i] = we({ top: 0, left: 0 }), [c, p] = we(0), C = s.filter((m) => m.type === "item" && !m.disabled);
  de(() => {
    var u, v;
    if (!n || !r) return;
    const m = document.getElementById(r);
    if (!m) return;
    const b = m.getBoundingClientRect(), w = ((u = a.current) == null ? void 0 : u.offsetHeight) ?? 200, k = ((v = a.current) == null ? void 0 : v.offsetWidth) ?? 200;
    let M = b.bottom + 4, P = b.left;
    M + w > window.innerHeight && (M = b.top - w - 4), P + k > window.innerWidth && (P = b.right - k), i({ top: M, left: P }), p(0);
  }, [n, r]);
  const E = ie(() => {
    o("close");
  }, [o]), N = ie((m) => {
    o("selectItem", { itemId: m });
  }, [o]);
  de(() => {
    if (!n) return;
    const m = (b) => {
      a.current && !a.current.contains(b.target) && E();
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [n, E]);
  const f = ie((m) => {
    if (m.key === "Escape") {
      E();
      return;
    }
    if (m.key === "ArrowDown")
      m.preventDefault(), p((b) => (b + 1) % C.length);
    else if (m.key === "ArrowUp")
      m.preventDefault(), p((b) => (b - 1 + C.length) % C.length);
    else if (m.key === "Enter" || m.key === " ") {
      m.preventDefault();
      const b = C[c];
      b && N(b.id);
    }
  }, [E, N, C, c]);
  return de(() => {
    n && a.current && a.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: a,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: f
    },
    s.map((m, b) => {
      if (m.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
      const k = C.indexOf(m) === c;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: m.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (m.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: m.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => N(m.id)
        },
        m.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + m.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, m.label)
      );
    })
  ) : null;
}, rn = ({ controlId: l }) => {
  const t = I(), o = t.header, n = t.content, r = t.footer, s = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(B, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(B, { control: n })), r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(B, { control: r })), /* @__PURE__ */ e.createElement(B, { control: s }));
}, sn = () => {
  const t = I().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, cn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, Ne = 50, dn = () => {
  const l = I(), t = z(), o = W(cn), n = l.columns ?? [], r = l.totalRowCount ?? 0, s = l.rows ?? [], a = l.rowHeight ?? 36, d = l.selectionMode ?? "single", i = l.selectedCount ?? 0, c = l.frozenColumnCount ?? 0, p = l.treeMode ?? !1, C = e.useMemo(
    () => n.filter((h) => h.sortPriority && h.sortPriority > 0).length,
    [n]
  ), E = d === "multi", N = 40, f = 20, m = e.useRef(null), b = e.useRef(null), w = e.useRef(null), [k, M] = e.useState({}), P = e.useRef(null), u = e.useRef(!1), v = e.useRef(null), [L, S] = e.useState(null), [_, g] = e.useState(null);
  e.useEffect(() => {
    P.current || M({});
  }, [n]);
  const T = e.useCallback((h) => k[h.name] ?? h.width, [k]), R = e.useMemo(() => {
    const h = [];
    let y = E && c > 0 ? N : 0;
    for (let D = 0; D < c && D < n.length; D++)
      h.push(y), y += T(n[D]);
    return h;
  }, [n, c, E, N, T]), j = r * a, H = e.useCallback((h, y, D) => {
    D.preventDefault(), D.stopPropagation(), P.current = { column: h, startX: D.clientX, startWidth: y };
    const F = (V) => {
      const O = P.current;
      if (!O) return;
      const A = Math.max(Ne, O.startWidth + (V.clientX - O.startX));
      M((re) => ({ ...re, [O.column]: A }));
    }, $ = (V) => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", $);
      const O = P.current;
      if (O) {
        const A = Math.max(Ne, O.startWidth + (V.clientX - O.startX));
        t("columnResize", { column: O.column, width: A }), P.current = null, u.current = !0, requestAnimationFrame(() => {
          u.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", $);
  }, [t]), K = e.useCallback(() => {
    m.current && b.current && (m.current.scrollLeft = b.current.scrollLeft), w.current !== null && clearTimeout(w.current), w.current = window.setTimeout(() => {
      const h = b.current;
      if (!h) return;
      const y = h.scrollTop, D = Math.ceil(h.clientHeight / a), F = Math.floor(y / a);
      t("scroll", { start: F, count: D });
    }, 80);
  }, [t, a]), G = e.useCallback((h, y, D) => {
    if (u.current) return;
    let F;
    !y || y === "desc" ? F = "asc" : F = "desc";
    const $ = D.shiftKey ? "add" : "replace";
    t("sort", { column: h, direction: F, mode: $ });
  }, [t]), Q = e.useCallback((h, y) => {
    v.current = h, y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", h);
  }, []), ee = e.useCallback((h, y) => {
    if (!v.current || v.current === h) {
      S(null);
      return;
    }
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const D = y.currentTarget.getBoundingClientRect(), F = y.clientX < D.left + D.width / 2 ? "left" : "right";
    S({ column: h, side: F });
  }, []), X = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation();
    const y = v.current;
    if (!y || !L) {
      v.current = null, S(null);
      return;
    }
    let D = n.findIndex(($) => $.name === L.column);
    if (D < 0) {
      v.current = null, S(null);
      return;
    }
    const F = n.findIndex(($) => $.name === y);
    L.side === "right" && D++, F < D && D--, t("columnReorder", { column: y, targetIndex: D }), v.current = null, S(null);
  }, [n, L, t]), Ie = e.useCallback(() => {
    v.current = null, S(null);
  }, []), Me = e.useCallback((h, y) => {
    y.shiftKey && y.preventDefault(), t("select", {
      rowIndex: h,
      ctrlKey: y.ctrlKey || y.metaKey,
      shiftKey: y.shiftKey
    });
  }, [t]), Pe = e.useCallback((h, y) => {
    y.stopPropagation(), t("select", { rowIndex: h, ctrlKey: !0, shiftKey: !1 });
  }, [t]), Be = e.useCallback(() => {
    const h = i === r && r > 0;
    t("selectAll", { selected: !h });
  }, [t, i, r]), Fe = e.useCallback((h, y, D) => {
    D.stopPropagation(), t("expand", { rowIndex: h, expanded: y });
  }, [t]), je = e.useCallback((h, y) => {
    y.preventDefault(), g({ x: y.clientX, y: y.clientY, colIdx: h });
  }, []), $e = e.useCallback(() => {
    _ && (t("setFrozenColumnCount", { count: _.colIdx + 1 }), g(null));
  }, [_, t]), ze = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), g(null);
  }, [t]);
  e.useEffect(() => {
    if (!_) return;
    const h = () => g(null), y = (D) => {
      D.key === "Escape" && g(null);
    };
    return document.addEventListener("mousedown", h), document.addEventListener("keydown", y), () => {
      document.removeEventListener("mousedown", h), document.removeEventListener("keydown", y);
    };
  }, [_]);
  const oe = n.reduce((h, y) => h + T(y), 0) + (E ? N : 0), Ae = i === r && r > 0, Ee = i > 0 && i < r, Ve = e.useCallback((h) => {
    h && (h.indeterminate = Ee);
  }, [Ee]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (h) => {
        if (!v.current) return;
        h.preventDefault();
        const y = b.current, D = m.current;
        if (!y) return;
        const F = y.getBoundingClientRect(), $ = 40, V = 8;
        h.clientX < F.left + $ ? y.scrollLeft = Math.max(0, y.scrollLeft - V) : h.clientX > F.right - $ && (y.scrollLeft += V), D && (D.scrollLeft = y.scrollLeft);
      },
      onDrop: X
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: m }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: oe } }, E && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...c > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (h) => {
          v.current && (h.preventDefault(), h.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== v.current && S({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Ve,
          className: "tlTableView__checkbox",
          checked: Ae,
          onChange: Be
        }
      )
    ), n.map((h, y) => {
      const D = T(h), F = y === n.length - 1;
      let $ = "tlTableView__headerCell";
      h.sortable && ($ += " tlTableView__headerCell--sortable"), L && L.column === h.name && ($ += " tlTableView__headerCell--dragOver-" + L.side);
      const V = y < c, O = y === c - 1;
      return V && ($ += " tlTableView__headerCell--frozen"), O && ($ += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: h.name,
          className: $,
          style: {
            ...F && !V ? { flex: "1 0 auto", minWidth: D } : { width: D, minWidth: D },
            position: V ? "sticky" : "relative",
            ...V ? { left: R[y], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: h.sortable ? (A) => G(h.name, h.sortDirection, A) : void 0,
          onContextMenu: (A) => je(y, A),
          onDragStart: (A) => Q(h.name, A),
          onDragOver: (A) => ee(h.name, A),
          onDrop: X,
          onDragEnd: Ie
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, h.label),
        h.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, h.sortDirection === "asc" ? "▲" : "▼", C > 1 && h.sortPriority != null && h.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, h.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (A) => H(h.name, D, A)
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
            y.name !== v.current && (h.preventDefault(), h.dataTransfer.dropEffect = "move", S({ column: y.name, side: "right" }));
          }
        },
        onDrop: X
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: K
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: j, position: "relative", minWidth: oe } }, s.map((h) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: h.id,
          className: "tlTableView__row" + (h.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: h.index * a,
            height: a,
            minWidth: oe,
            width: "100%"
          },
          onClick: (y) => Me(h.index, y)
        },
        E && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: N,
              minWidth: N,
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
        n.map((y, D) => {
          const F = T(y), $ = D === n.length - 1, V = D < c, O = D === c - 1;
          let A = "tlTableView__cell";
          V && (A += " tlTableView__cell--frozen"), O && (A += " tlTableView__cell--frozenLast");
          const re = p && D === 0, Oe = h.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: y.name,
              className: A,
              style: {
                ...$ && !V ? { flex: "1 0 auto", minWidth: F } : { width: F, minWidth: F },
                ...V ? { position: "sticky", left: R[D], zIndex: 2 } : {}
              }
            },
            re ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: Oe * f } }, h.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (Ue) => Fe(h.index, !h.expanded, Ue)
              },
              h.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(B, { control: h.cells[y.name] })) : /* @__PURE__ */ e.createElement(B, { control: h.cells[y.name] })
          );
        })
      )))
    ),
    _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: _.y, left: _.x, zIndex: 1e4 },
        onMouseDown: (h) => h.stopPropagation()
      },
      _.colIdx + 1 !== c && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: $e }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.freezeUpTo"])),
      c > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ze }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.unfreezeAll"]))
    )
  );
}, un = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, xe = e.createContext(un), { useMemo: mn, useRef: pn, useState: fn, useEffect: bn } = e, hn = 320, _n = ({ controlId: l }) => {
  const t = I(), o = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", r = t.readOnly === !0, s = t.children ?? [], a = t.noModelMessage, d = pn(null), [i, c] = fn(
    n === "top" ? "top" : "side"
  );
  bn(() => {
    if (n !== "auto") {
      c(n);
      return;
    }
    const f = d.current;
    if (!f) return;
    const m = new ResizeObserver((b) => {
      for (const w of b) {
        const M = w.contentRect.width / o;
        c(M < hn ? "top" : "side");
      }
    });
    return m.observe(f), () => m.disconnect();
  }, [n, o]);
  const p = mn(() => ({
    readOnly: r,
    resolvedLabelPosition: i
  }), [r, i]), E = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / o))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    r ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, a)) : /* @__PURE__ */ e.createElement(xe.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: E, ref: d }, s.map((f, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: f }))));
}, { useCallback: vn } = e, En = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Cn = ({ controlId: l }) => {
  const t = I(), o = z(), n = W(En), r = t.header, s = t.headerActions ?? [], a = t.collapsible === !0, d = t.collapsed === !0, i = t.border ?? "none", c = t.fullLine === !0, p = t.children ?? [], C = r != null || s.length > 0 || a, E = vn(() => {
    o("toggleCollapse");
  }, [o]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    c ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, C && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, a && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: E,
      "aria-expanded": !d,
      title: d ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: d ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, r), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((f, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: f }))));
}, { useContext: gn, useState: yn, useCallback: kn } = e, wn = ({ controlId: l }) => {
  const t = I(), o = gn(xe), n = t.label ?? "", r = t.required === !0, s = t.error, a = t.helpText, d = t.dirty === !0, i = t.labelPosition ?? o.resolvedLabelPosition, c = t.fullLine === !0, p = t.visible !== !1, C = t.field, E = o.readOnly, [N, f] = yn(!1), m = kn(() => f((k) => !k), []);
  if (!p) return null;
  const b = s != null, w = [
    "tlFormField",
    `tlFormField--${i}`,
    E ? "tlFormField--readonly" : "",
    c ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: w }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), r && !E && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), a && !E && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(B, { control: C })), !E && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !E && a && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, a));
}, Nn = () => {
  const l = I(), t = z(), o = l.iconCss, n = l.iconSrc, r = l.label, s = l.cssClass, a = l.tooltip, d = l.hasLink, i = o ? /* @__PURE__ */ e.createElement("i", { className: o }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, c = /* @__PURE__ */ e.createElement(e.Fragment, null, i, r && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, r)), p = e.useCallback((E) => {
    E.preventDefault(), t("goto", {});
  }, [t]), C = ["tlResourceCell", s].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: C, href: "#", onClick: p, title: a }, c) : /* @__PURE__ */ e.createElement("span", { className: C, title: a }, c);
}, Ln = 20, Tn = () => {
  const l = I(), t = z(), o = l.nodes ?? [], n = l.selectionMode ?? "single", r = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, a = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [i, c] = e.useState(-1), p = e.useRef(null), C = e.useCallback((u, v) => {
    t(v ? "collapse" : "expand", { nodeId: u });
  }, [t]), E = e.useCallback((u, v) => {
    t("select", {
      nodeId: u,
      ctrlKey: v.ctrlKey || v.metaKey,
      shiftKey: v.shiftKey
    });
  }, [t]), N = e.useCallback((u, v) => {
    v.preventDefault(), t("contextMenu", { nodeId: u, x: v.clientX, y: v.clientY });
  }, [t]), f = e.useRef(null), m = e.useCallback((u, v) => {
    const L = v.getBoundingClientRect(), S = u.clientY - L.top, _ = L.height / 3;
    return S < _ ? "above" : S > _ * 2 ? "below" : "within";
  }, []), b = e.useCallback((u, v) => {
    v.dataTransfer.effectAllowed = "move", v.dataTransfer.setData("text/plain", u);
  }, []), w = e.useCallback((u, v) => {
    v.preventDefault(), v.dataTransfer.dropEffect = "move";
    const L = m(v, v.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: u, position: L }), f.current = null;
    }, 50);
  }, [t, m]), k = e.useCallback((u, v) => {
    v.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const L = m(v, v.currentTarget);
    t("drop", { nodeId: u, position: L });
  }, [t, m]), M = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((u) => {
    if (o.length === 0) return;
    let v = i;
    switch (u.key) {
      case "ArrowDown":
        u.preventDefault(), v = Math.min(i + 1, o.length - 1);
        break;
      case "ArrowUp":
        u.preventDefault(), v = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (u.preventDefault(), i >= 0 && i < o.length) {
          const L = o[i];
          if (L.expandable && !L.expanded) {
            t("expand", { nodeId: L.id });
            return;
          } else L.expanded && (v = i + 1);
        }
        break;
      case "ArrowLeft":
        if (u.preventDefault(), i >= 0 && i < o.length) {
          const L = o[i];
          if (L.expanded) {
            t("collapse", { nodeId: L.id });
            return;
          } else {
            const S = L.depth;
            for (let _ = i - 1; _ >= 0; _--)
              if (o[_].depth < S) {
                v = _;
                break;
              }
          }
        }
        break;
      case "Enter":
        u.preventDefault(), i >= 0 && i < o.length && t("select", {
          nodeId: o[i].id,
          ctrlKey: u.ctrlKey || u.metaKey,
          shiftKey: u.shiftKey
        });
        return;
      case " ":
        u.preventDefault(), n === "multi" && i >= 0 && i < o.length && t("select", {
          nodeId: o[i].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        u.preventDefault(), v = 0;
        break;
      case "End":
        u.preventDefault(), v = o.length - 1;
        break;
      default:
        return;
    }
    v !== i && c(v);
  }, [i, o, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    o.map((u, v) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: u.id,
        role: "treeitem",
        "aria-expanded": u.expandable ? u.expanded : void 0,
        "aria-selected": u.selected,
        "aria-level": u.depth + 1,
        className: [
          "tlTreeView__node",
          u.selected ? "tlTreeView__node--selected" : "",
          v === i ? "tlTreeView__node--focused" : "",
          a === u.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          a === u.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          a === u.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: u.depth * Ln },
        draggable: r,
        onClick: (L) => E(u.id, L),
        onContextMenu: (L) => N(u.id, L),
        onDragStart: (L) => b(u.id, L),
        onDragOver: s ? (L) => w(u.id, L) : void 0,
        onDrop: s ? (L) => k(u.id, L) : void 0,
        onDragEnd: M
      },
      u.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (L) => {
            L.stopPropagation(), C(u.id, u.expanded);
          },
          tabIndex: -1,
          "aria-label": u.expanded ? "Collapse" : "Expand"
        },
        u.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: u.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(B, { control: u.content }))
    ))
  );
}, { useCallback: ue, useRef: xn } = e, Sn = ({
  colors: l,
  columns: t,
  onSelect: o,
  onConfirm: n,
  onSwap: r
}) => {
  const s = xn(null), a = ue(
    (c) => (p) => {
      s.current = c, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), d = ue((c) => {
    c.preventDefault(), c.dataTransfer.dropEffect = "move";
  }, []), i = ue(
    (c) => (p) => {
      p.preventDefault(), s.current !== null && s.current !== c && r(s.current, c), s.current = null;
    },
    [r]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    l.map((c, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (c == null ? " tlColorInput__paletteCell--empty" : ""),
        style: c != null ? { backgroundColor: c } : void 0,
        title: c ?? "",
        draggable: c != null,
        onClick: c != null ? () => o(c) : void 0,
        onDoubleClick: c != null ? () => n(c) : void 0,
        onDragStart: c != null ? a(p) : void 0,
        onDragOver: d,
        onDrop: i(p)
      }
    ))
  );
};
function Se(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function he(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Re(l) {
  if (!he(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function De(l, t, o) {
  const n = (r) => Se(r).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(o);
}
function Rn(l, t, o) {
  const n = l / 255, r = t / 255, s = o / 255, a = Math.max(n, r, s), d = Math.min(n, r, s), i = a - d;
  let c = 0;
  i !== 0 && (a === n ? c = (r - s) / i % 6 : a === r ? c = (s - n) / i + 2 : c = (n - r) / i + 4, c *= 60, c < 0 && (c += 360));
  const p = a === 0 ? 0 : i / a;
  return [c, p, a];
}
function Dn(l, t, o) {
  const n = o * t, r = n * (1 - Math.abs(l / 60 % 2 - 1)), s = o - n;
  let a = 0, d = 0, i = 0;
  return l < 60 ? (a = n, d = r, i = 0) : l < 120 ? (a = r, d = n, i = 0) : l < 180 ? (a = 0, d = n, i = r) : l < 240 ? (a = 0, d = r, i = n) : l < 300 ? (a = r, d = 0, i = n) : (a = n, d = 0, i = r), [
    Math.round((a + s) * 255),
    Math.round((d + s) * 255),
    Math.round((i + s) * 255)
  ];
}
function In(l) {
  return Rn(...Re(l));
}
function me(l, t, o) {
  return De(...Dn(l, t, o));
}
const { useCallback: q, useRef: Le } = e, Mn = ({ color: l, onColorChange: t }) => {
  const [o, n, r] = In(l), s = Le(null), a = Le(null), d = q(
    (f, m) => {
      var M;
      const b = (M = s.current) == null ? void 0 : M.getBoundingClientRect();
      if (!b) return;
      const w = Math.max(0, Math.min(1, (f - b.left) / b.width)), k = Math.max(0, Math.min(1, 1 - (m - b.top) / b.height));
      t(me(o, w, k));
    },
    [o, t]
  ), i = q(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), d(f.clientX, f.clientY);
    },
    [d]
  ), c = q(
    (f) => {
      f.buttons !== 0 && d(f.clientX, f.clientY);
    },
    [d]
  ), p = q(
    (f) => {
      var k;
      const m = (k = a.current) == null ? void 0 : k.getBoundingClientRect();
      if (!m) return;
      const w = Math.max(0, Math.min(1, (f - m.top) / m.height)) * 360;
      t(me(w, n, r));
    },
    [n, r, t]
  ), C = q(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), p(f.clientY);
    },
    [p]
  ), E = q(
    (f) => {
      f.buttons !== 0 && p(f.clientY);
    },
    [p]
  ), N = me(o, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: N },
      onPointerDown: i,
      onPointerMove: c
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - r) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: a,
      className: "tlColorInput__hueSlider",
      onPointerDown: C,
      onPointerMove: E
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${o / 360 * 100}%` }
      }
    )
  ));
}, { useState: pe, useCallback: Y, useEffect: fe, useRef: Pn } = e, Bn = ({
  currentColor: l,
  palette: t,
  paletteColumns: o,
  defaultPalette: n,
  onConfirm: r,
  onCancel: s,
  onPaletteChange: a
}) => {
  const [d, i] = pe("palette"), [c, p] = pe(l), C = Pn(null), [E, N, f] = Re(c), [m, b] = pe(c.toUpperCase());
  fe(() => {
    b(c.toUpperCase());
  }, [c]), fe(() => {
    const S = (_) => {
      _.key === "Escape" && s();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [s]), fe(() => {
    const S = (g) => {
      C.current && !C.current.contains(g.target) && s();
    }, _ = setTimeout(() => document.addEventListener("mousedown", S), 0);
    return () => {
      clearTimeout(_), document.removeEventListener("mousedown", S);
    };
  }, [s]);
  const w = Y(
    (S) => (_) => {
      const g = parseInt(_.target.value, 10);
      if (isNaN(g)) return;
      const T = Se(g);
      p(De(S === "r" ? T : E, S === "g" ? T : N, S === "b" ? T : f));
    },
    [E, N, f]
  ), k = Y((S) => {
    const _ = S.target.value;
    b(_), he(_) && p(_);
  }, []), M = Y((S) => {
    p(S);
  }, []), P = Y(
    (S) => {
      r(S);
    },
    [r]
  ), u = Y(
    (S, _) => {
      const g = [...t], T = g[S];
      g[S] = g[_], g[_] = T, a(g);
    },
    [t, a]
  ), v = Y(() => {
    a([...n]);
  }, [n, a]), L = Y(() => {
    r(c);
  }, [c, r]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__popup", ref: C }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlColorInput__tab" + (d === "palette" ? " tlColorInput__tab--active" : ""),
      onClick: () => i("palette")
    },
    "Color Palette"
  ), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlColorInput__tab" + (d === "mixer" ? " tlColorInput__tab--active" : ""),
      onClick: () => i("mixer")
    },
    "Color Mixer"
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, d === "palette" ? /* @__PURE__ */ e.createElement(
    Sn,
    {
      colors: t,
      columns: o,
      onSelect: M,
      onConfirm: P,
      onSwap: u
    }
  ) : /* @__PURE__ */ e.createElement(Mn, { color: c, onColorChange: p }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, "Current"), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: l } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, "New"), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewSwatch", style: { backgroundColor: c } })), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, "Red"), /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlColorInput__input",
      type: "number",
      min: 0,
      max: 255,
      value: E,
      onChange: w("r")
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, "Green"), /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlColorInput__input",
      type: "number",
      min: 0,
      max: 255,
      value: N,
      onChange: w("g")
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, "Blue"), /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlColorInput__input",
      type: "number",
      min: 0,
      max: 255,
      value: f,
      onChange: w("b")
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, "Hex"), /* @__PURE__ */ e.createElement(
    "input",
    {
      className: "tlColorInput__input" + (he(m) ? "" : " tlColorInput__input--error"),
      type: "text",
      value: m,
      onChange: k
    }
  )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, d === "palette" && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: v }, "Reset"), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: s }, "Cancel"), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: L }, "OK")))));
}, { useState: Fn, useCallback: te, useRef: jn } = e, $n = ({ controlId: l, state: t }) => {
  const o = z(), [n, r] = Fn(!1), s = jn(null), a = t.value, d = t.editable !== !1, i = t.palette ?? [], c = t.paletteColumns ?? 6, p = t.defaultPalette ?? i, C = te(() => {
    d && r(!0);
  }, [d]), E = te(
    (m) => {
      r(!1), o("valueChanged", { value: m });
    },
    [o]
  ), N = te(() => {
    r(!1);
  }, []), f = te(
    (m) => {
      o("paletteChanged", { palette: m });
    },
    [o]
  );
  return d ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (a == null ? " tlColorInput__swatch--noColor" : ""),
      style: a != null ? { backgroundColor: a } : void 0,
      onClick: C,
      disabled: t.disabled === !0,
      title: a ?? "",
      "aria-label": "Choose color"
    }
  ), n && /* @__PURE__ */ e.createElement(
    Bn,
    {
      currentColor: a ?? "#000000",
      palette: i,
      paletteColumns: c,
      defaultPalette: p,
      onConfirm: E,
      onCancel: N,
      onPaletteChange: f
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (a == null ? " tlColorInput--noColor" : ""),
      style: a != null ? { backgroundColor: a } : void 0,
      title: a ?? ""
    }
  );
};
x("TLButton", at);
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
x("TLAudioRecorder", ut);
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
x("TLSnackbar", an);
x("TLMenu", on);
x("TLAppShell", rn);
x("TLTextCell", sn);
x("TLTableView", dn);
x("TLFormLayout", _n);
x("TLFormGroup", Cn);
x("TLFormField", wn);
x("TLResourceCell", Nn);
x("TLTreeView", Tn);
x("TLColorInput", $n);
