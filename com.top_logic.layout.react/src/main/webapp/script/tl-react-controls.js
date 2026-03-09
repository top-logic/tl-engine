import { React as e, useTLFieldValue as q, getComponent as Me, useTLState as P, useTLCommand as A, TLChild as R, useTLUpload as ie, useI18N as W, useTLDataUrl as de, register as L } from "tl-react-bridge";
const { useCallback: Fe } = e, je = ({ controlId: a, state: t }) => {
  const [o, n] = q(), s = Fe(
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
}, { useCallback: Ie } = e, ze = ({ controlId: a, state: t, config: o }) => {
  const [n, s] = q(), r = Ie(
    (d) => {
      const i = d.target.value, c = i === "" ? null : Number(i);
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
}, { useCallback: Ae } = e, $e = ({ controlId: a, state: t }) => {
  const [o, n] = q(), s = Ae(
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
}, { useCallback: Ve } = e, Oe = ({ controlId: a, state: t, config: o }) => {
  var d;
  const [n, s] = q(), r = Ve(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), l = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const i = ((d = l.find((c) => c.value === n)) == null ? void 0 : d.label) ?? "";
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
}, { useCallback: Ue } = e, We = ({ controlId: a, state: t }) => {
  const [o, n] = q(), s = Ue(
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
}, Ke = ({ controlId: a, state: t }) => {
  const o = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: a, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, o.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, r) => /* @__PURE__ */ e.createElement("tr", { key: r }, o.map((l) => {
    const d = l.cellModule ? Me(l.cellModule) : void 0, i = s[l.name];
    if (d) {
      const c = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: l.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: a + "-" + r + "-" + l.name,
          state: c
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: l.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: He } = e, Ge = ({ controlId: a, command: t, label: o, icon: n, disabled: s, displayMode: r }) => {
  const l = P(), d = A(), i = t ?? "click", c = o ?? l.label, p = n ?? l.icon, v = s ?? l.disabled === !0, b = r ?? l.displayMode ?? "label-only", N = He(() => {
    d(i);
  }, [d, i]), C = p && (b === "icon-only" || b === "icon-label"), m = b === "label-only" || b === "icon-label";
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: N,
      disabled: v,
      className: "tlReactButton" + (b === "icon-only" ? " tlReactButton--iconOnly" : ""),
      "aria-label": b === "icon-only" ? c : void 0,
      title: b === "icon-only" ? c : void 0
    },
    C && /* @__PURE__ */ e.createElement("i", { className: "tlReactButton__icon " + p, "aria-hidden": "true" }),
    m && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, c)
  );
}, { useCallback: Ye } = e, Xe = ({ controlId: a, command: t, label: o, active: n, disabled: s }) => {
  const r = P(), l = A(), d = t ?? "click", i = o ?? r.label, c = n ?? r.active === !0, p = s ?? r.disabled === !0, v = Ye(() => {
    l(d);
  }, [l, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: a,
      onClick: v,
      disabled: p,
      className: "tlReactButton" + (c ? " tlReactButtonActive" : "")
    },
    i
  );
}, qe = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ze } = e, Je = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.tabs ?? [], s = t.activeTabId, r = Ze((l) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, Qe = ({ controlId: a }) => {
  const t = P(), o = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((s, r) => /* @__PURE__ */ e.createElement("div", { key: r, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: s })))));
}, et = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, tt = ({ controlId: a }) => {
  const t = P(), o = ie(), [n, s] = e.useState("idle"), [r, l] = e.useState(null), d = e.useRef(null), i = e.useRef([]), c = e.useRef(null), p = t.status ?? "idle", v = t.error, b = p === "received" ? "idle" : n !== "idle" ? n : p, N = e.useCallback(async () => {
    if (n === "recording") {
      const w = d.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (n !== "uploading") {
      if (l(null), !window.isSecureContext || !navigator.mediaDevices) {
        l("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        c.current = w, i.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", B = new MediaRecorder(w, M ? { mimeType: M } : void 0);
        d.current = B, B.ondataavailable = (u) => {
          u.data.size > 0 && i.current.push(u.data);
        }, B.onstop = async () => {
          w.getTracks().forEach((k) => k.stop()), c.current = null;
          const u = new Blob(i.current, { type: B.mimeType || "audio/webm" });
          if (i.current = [], u.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const h = new FormData();
          h.append("audio", u, "recording.webm"), await o(h), s("idle");
        }, B.start(), s("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), l("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [n, o]), C = W(et), m = b === "recording" ? C["js.audioRecorder.stop"] : b === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], _ = b === "uploading", T = ["tlAudioRecorder__button"];
  return b === "recording" && T.push("tlAudioRecorder__button--recording"), b === "uploading" && T.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: N,
      disabled: _,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[r]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, nt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, at = ({ controlId: a }) => {
  const t = P(), o = de(), n = !!t.hasAudio, s = t.dataRevision ?? 0, [r, l] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), i = e.useRef(null), c = e.useRef(s);
  e.useEffect(() => {
    n ? r === "disabled" && l("idle") : (d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), l("disabled"));
  }, [n]), e.useEffect(() => {
    s !== c.current && (c.current = s, d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (r === "playing" || r === "paused" || r === "loading") && l("idle"));
  }, [s]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (r === "disabled" || r === "loading")
      return;
    if (r === "playing") {
      d.current && d.current.pause(), l("paused");
      return;
    }
    if (r === "paused" && d.current) {
      d.current.play(), l("playing");
      return;
    }
    if (!i.current) {
      l("loading");
      try {
        const _ = await fetch(o);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), l("idle");
          return;
        }
        const T = await _.blob();
        i.current = URL.createObjectURL(T);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), l("idle");
        return;
      }
    }
    const m = new Audio(i.current);
    d.current = m, m.onended = () => {
      l("idle");
    }, m.play(), l("playing");
  }, [r, o]), v = W(nt), b = r === "loading" ? v["js.loading"] : r === "playing" ? v["js.audioPlayer.pause"] : r === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], N = r === "disabled" || r === "loading", C = ["tlAudioPlayer__button"];
  return r === "playing" && C.push("tlAudioPlayer__button--playing"), r === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: p,
      disabled: N,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${r === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, lt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ot = ({ controlId: a }) => {
  const t = P(), o = ie(), [n, s] = e.useState("idle"), [r, l] = e.useState(!1), d = e.useRef(null), i = t.status ?? "idle", c = t.error, p = t.accept ?? "", v = i === "received" ? "idle" : n !== "idle" ? n : i, b = e.useCallback(async (u) => {
    s("uploading");
    const h = new FormData();
    h.append("file", u, u.name), await o(h), s("idle");
  }, [o]), N = e.useCallback((u) => {
    var k;
    const h = (k = u.target.files) == null ? void 0 : k[0];
    h && b(h);
  }, [b]), C = e.useCallback(() => {
    var u;
    n !== "uploading" && ((u = d.current) == null || u.click());
  }, [n]), m = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!0);
  }, []), _ = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), l(!1);
  }, []), T = e.useCallback((u) => {
    var k;
    if (u.preventDefault(), u.stopPropagation(), l(!1), n === "uploading") return;
    const h = (k = u.dataTransfer.files) == null ? void 0 : k[0];
    h && b(h);
  }, [n, b]), w = v === "uploading", M = W(lt), B = v === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlFileUpload${r ? " tlFileUpload--dragover" : ""}`,
      onDragOver: m,
      onDragLeave: _,
      onDrop: T
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
        className: "tlFileUpload__button" + (v === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: w,
        title: B,
        "aria-label": B
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    c && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, c)
  );
}, rt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, st = ({ controlId: a }) => {
  const t = P(), o = de(), n = A(), s = !!t.hasData, r = t.dataRevision ?? 0, l = t.fileName ?? "download", d = !!t.clearable, [i, c] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!s || i)) {
      c(!0);
      try {
        const C = o + (o.includes("?") ? "&" : "?") + "rev=" + r, m = await fetch(C);
        if (!m.ok) {
          console.error("[TLDownload] Failed to fetch data:", m.status);
          return;
        }
        const _ = await m.blob(), T = URL.createObjectURL(_), w = document.createElement("a");
        w.href = T, w.download = l, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(T);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        c(!1);
      }
    }
  }, [s, i, o, r, l]), v = e.useCallback(async () => {
    s && await n("clear");
  }, [s, n]), b = W(rt);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, b["js.download.noFile"]));
  const N = i ? b["js.downloading"] : b["js.download.file"].replace("{0}", l);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: b["js.download.clear"],
      "aria-label": b["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, ct = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, it = ({ controlId: a }) => {
  const t = P(), o = ie(), [n, s] = e.useState("idle"), [r, l] = e.useState(null), [d, i] = e.useState(!1), c = e.useRef(null), p = e.useRef(null), v = e.useRef(null), b = e.useRef(null), N = e.useRef(null), C = t.error, m = e.useMemo(
    () => {
      var y;
      return !!(window.isSecureContext && ((y = navigator.mediaDevices) != null && y.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((y) => y.stop()), p.current = null), c.current && (c.current.srcObject = null);
  }, []), T = e.useCallback(() => {
    _(), s("idle");
  }, [_]), w = e.useCallback(async () => {
    var y;
    if (n !== "uploading") {
      if (l(null), !m) {
        (y = b.current) == null || y.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = D, s("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), l("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [n, m]), M = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const y = c.current, D = v.current;
    if (!y || !D)
      return;
    D.width = y.videoWidth, D.height = y.videoHeight;
    const x = D.getContext("2d");
    x && (x.drawImage(y, 0, 0), _(), s("uploading"), D.toBlob(async (I) => {
      if (!I) {
        s("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", I, "capture.jpg"), await o(K), s("idle");
    }, "image/jpeg", 0.85));
  }, [n, o, _]), B = e.useCallback(async (y) => {
    var I;
    const D = (I = y.target.files) == null ? void 0 : I[0];
    if (!D) return;
    s("uploading");
    const x = new FormData();
    x.append("photo", D, D.name), await o(x), s("idle"), b.current && (b.current.value = "");
  }, [o]);
  e.useEffect(() => {
    n === "overlayOpen" && c.current && p.current && (c.current.srcObject = p.current);
  }, [n]), e.useEffect(() => {
    var D;
    if (n !== "overlayOpen") return;
    (D = N.current) == null || D.focus();
    const y = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = y;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const y = (D) => {
      D.key === "Escape" && T();
    };
    return document.addEventListener("keydown", y), () => document.removeEventListener("keydown", y);
  }, [n, T]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((y) => y.stop()), p.current = null);
  }, []);
  const u = W(ct), h = n === "uploading" ? u["js.uploading"] : u["js.photoCapture.open"], k = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && k.push("tlPhotoCapture__cameraBtn--uploading");
  const F = ["tlPhotoCapture__overlayVideo"];
  d && F.push("tlPhotoCapture__overlayVideo--mirrored");
  const E = ["tlPhotoCapture__mirrorBtn"];
  return d && E.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: w,
      disabled: n === "uploading",
      title: h,
      "aria-label": h
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
      onChange: B
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: v, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: T }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: c,
        className: F.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: E.join(" "),
        onClick: () => i((y) => !y),
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
        onClick: T,
        title: u["js.photoCapture.close"],
        "aria-label": u["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, u[r]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, dt = {
  "js.photoViewer.alt": "Captured photo"
}, ut = ({ controlId: a }) => {
  const t = P(), o = de(), n = !!t.hasPhoto, s = t.dataRevision ?? 0, [r, l] = e.useState(null), d = e.useRef(s);
  e.useEffect(() => {
    if (!n) {
      r && (URL.revokeObjectURL(r), l(null));
      return;
    }
    if (s === d.current && r)
      return;
    d.current = s, r && (URL.revokeObjectURL(r), l(null));
    let c = !1;
    return (async () => {
      try {
        const p = await fetch(o);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const v = await p.blob();
        c || l(URL.createObjectURL(v));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      c = !0;
    };
  }, [n, s, o]), e.useEffect(() => () => {
    r && URL.revokeObjectURL(r);
  }, []);
  const i = W(dt);
  return !n || !r ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: r,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: me, useRef: le } = e, mt = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.orientation, s = t.resizable === !0, r = t.children ?? [], l = n === "horizontal", d = r.length > 0 && r.every((_) => _.collapsed), i = !d && r.some((_) => _.collapsed), c = d ? !l : l, p = le(null), v = le(null), b = le(null), N = me((_, T) => {
    const w = {
      overflow: _.scrolling || "auto"
    };
    return _.collapsed ? d && !c ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : T !== void 0 ? w.flex = `0 0 ${T}px` : _.unit === "%" || i ? w.flex = `${_.size} 0 0%` : w.flex = `0 0 ${_.size}px`, _.minSize > 0 && !_.collapsed && (w.minWidth = l ? _.minSize : void 0, w.minHeight = l ? void 0 : _.minSize), w;
  }, [l, d, i, c]), C = me((_, T) => {
    _.preventDefault();
    const w = p.current;
    if (!w) return;
    const M = r[T], B = r[T + 1], u = w.querySelectorAll(":scope > .tlSplitPanel__child"), h = [];
    u.forEach((E) => {
      h.push(l ? E.offsetWidth : E.offsetHeight);
    }), b.current = h, v.current = {
      splitterIndex: T,
      startPos: l ? _.clientX : _.clientY,
      startSizeBefore: h[T],
      startSizeAfter: h[T + 1],
      childBefore: M,
      childAfter: B
    };
    const k = (E) => {
      const y = v.current;
      if (!y || !b.current) return;
      const x = (l ? E.clientX : E.clientY) - y.startPos, I = y.childBefore.minSize || 0, K = y.childAfter.minSize || 0;
      let H = y.startSizeBefore + x, G = y.startSizeAfter - x;
      H < I && (G += H - I, H = I), G < K && (H += G - K, G = K), b.current[y.splitterIndex] = H, b.current[y.splitterIndex + 1] = G;
      const Z = w.querySelectorAll(":scope > .tlSplitPanel__child"), J = Z[y.splitterIndex], Y = Z[y.splitterIndex + 1];
      J && (J.style.flex = `0 0 ${H}px`), Y && (Y.style.flex = `0 0 ${G}px`);
    }, F = () => {
      if (document.removeEventListener("mousemove", k), document.removeEventListener("mouseup", F), document.body.style.cursor = "", document.body.style.userSelect = "", b.current) {
        const E = {};
        r.forEach((y, D) => {
          const x = y.control;
          x != null && x.controlId && b.current && (E[x.controlId] = b.current[D]);
        }), o("updateSizes", { sizes: E });
      }
      b.current = null, v.current = null;
    };
    document.addEventListener("mousemove", k), document.addEventListener("mouseup", F), document.body.style.cursor = l ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [r, l, o]), m = [];
  return r.forEach((_, T) => {
    if (m.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${T}`,
          className: `tlSplitPanel__child${_.collapsed && c ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(_)
        },
        /* @__PURE__ */ e.createElement(R, { control: _.control })
      )
    ), s && T < r.length - 1) {
      const w = r[T + 1];
      !_.collapsed && !w.collapsed && m.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${T}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (B) => C(B, T)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: a,
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
}, { useCallback: oe } = e, pt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, ft = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), bt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ht = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), _t = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Et = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), vt = ({ controlId: a }) => {
  const t = P(), o = A(), n = W(pt), s = t.title, r = t.expansionState ?? "NORMALIZED", l = t.showMinimize === !0, d = t.showMaximize === !0, i = t.showPopOut === !0, c = r === "MINIMIZED", p = r === "MAXIMIZED", v = r === "HIDDEN", b = oe(() => {
    o("toggleMinimize");
  }, [o]), N = oe(() => {
    o("toggleMaximize");
  }, [o]), C = oe(() => {
    o("popOut");
  }, [o]);
  if (v)
    return null;
  const m = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlPanel tlPanel--${r.toLowerCase()}`,
      style: m
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(R, { control: t.toolbar }), l && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: c ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      c ? /* @__PURE__ */ e.createElement(bt, null) : /* @__PURE__ */ e.createElement(ft, null)
    ), d && !c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: p ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(_t, null) : /* @__PURE__ */ e.createElement(ht, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Et, null)
    ))),
    !c && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(R, { control: t.child })),
    !c && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(R, { control: t.buttonBar }))
  );
}, gt = ({ controlId: a }) => {
  const t = P();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(R, { control: t.child })
  );
}, Ct = ({ controlId: a }) => {
  const t = P();
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(R, { control: t.activeChild }));
}, { useCallback: U, useState: Q, useEffect: ee, useRef: te } = e, yt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ce(a, t, o, n) {
  const s = [];
  for (const r of a)
    r.type === "nav" ? s.push({ id: r.id, type: "nav", groupId: n }) : r.type === "command" ? s.push({ id: r.id, type: "command", groupId: n }) : r.type === "group" && (s.push({ id: r.id, type: "group" }), (o.get(r.id) ?? r.expanded) && !t && s.push(...ce(r.children, t, o, r.id)));
  return s;
}
const X = ({ icon: a }) => a ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + a, "aria-hidden": "true" }) : null, kt = ({ item: a, active: t, collapsed: o, onSelect: n, tabIndex: s, itemRef: r, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(a.id),
    title: o ? a.label : void 0,
    tabIndex: s,
    ref: r,
    onFocus: () => l(a.id)
  },
  o && a.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(X, { icon: a.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, a.badge)) : /* @__PURE__ */ e.createElement(X, { icon: a.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
  !o && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
), wt = ({ item: a, collapsed: t, onExecute: o, tabIndex: n, itemRef: s, onFocus: r }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => o(a.id),
    title: t ? a.label : void 0,
    tabIndex: n,
    ref: s,
    onFocus: () => r(a.id)
  },
  /* @__PURE__ */ e.createElement(X, { icon: a.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)
), Nt = ({ item: a, collapsed: t }) => t && !a.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? a.label : void 0 }, /* @__PURE__ */ e.createElement(X, { icon: a.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label)), Tt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Lt = ({ item: a, activeItemId: t, anchorRect: o, onSelect: n, onExecute: s, onClose: r }) => {
  const l = te(null);
  ee(() => {
    const c = (p) => {
      l.current && !l.current.contains(p.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [r]), ee(() => {
    const c = (p) => {
      p.key === "Escape" && r();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [r]);
  const d = U((c) => {
    c.type === "nav" ? (n(c.id), r()) : c.type === "command" && (s(c.id), r());
  }, [n, s, r]), i = {};
  return o && (i.left = o.right, i.top = o.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: l, role: "menu", style: i }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, a.label), a.children.map((c) => {
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
        /* @__PURE__ */ e.createElement(X, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, xt = ({
  item: a,
  expanded: t,
  activeItemId: o,
  collapsed: n,
  onSelect: s,
  onExecute: r,
  onToggleGroup: l,
  tabIndex: d,
  itemRef: i,
  onFocus: c,
  focusedId: p,
  setItemRef: v,
  onItemFocus: b,
  flyoutGroupId: N,
  onOpenFlyout: C,
  onCloseFlyout: m
}) => {
  const _ = te(null), [T, w] = Q(null), M = U(() => {
    n ? N === a.id ? m() : (_.current && w(_.current.getBoundingClientRect()), C(a.id)) : l(a.id);
  }, [n, N, a.id, l, C, m]), B = U((h) => {
    _.current = h, i(h);
  }, [i]), u = n && N === a.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (u ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: n ? a.label : void 0,
      "aria-expanded": n ? u : t,
      tabIndex: d,
      ref: B,
      onFocus: () => c(a.id)
    },
    /* @__PURE__ */ e.createElement(X, { icon: a.icon }),
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
  ), u && /* @__PURE__ */ e.createElement(
    Lt,
    {
      item: a,
      activeItemId: o,
      anchorRect: T,
      onSelect: s,
      onExecute: r,
      onClose: m
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, a.children.map((h) => /* @__PURE__ */ e.createElement(
    ge,
    {
      key: h.id,
      item: h,
      activeItemId: o,
      collapsed: n,
      onSelect: s,
      onExecute: r,
      onToggleGroup: l,
      focusedId: p,
      setItemRef: v,
      onItemFocus: b,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))));
}, ge = ({
  item: a,
  activeItemId: t,
  collapsed: o,
  onSelect: n,
  onExecute: s,
  onToggleGroup: r,
  focusedId: l,
  setItemRef: d,
  onItemFocus: i,
  groupStates: c,
  flyoutGroupId: p,
  onOpenFlyout: v,
  onCloseFlyout: b
}) => {
  switch (a.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        kt,
        {
          item: a,
          active: a.id === t,
          collapsed: o,
          onSelect: n,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        wt,
        {
          item: a,
          collapsed: o,
          onExecute: s,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Nt, { item: a, collapsed: o });
    case "separator":
      return /* @__PURE__ */ e.createElement(Tt, null);
    case "group": {
      const N = c ? c.get(a.id) ?? a.expanded : a.expanded;
      return /* @__PURE__ */ e.createElement(
        xt,
        {
          item: a,
          expanded: N,
          activeItemId: t,
          collapsed: o,
          onSelect: n,
          onExecute: s,
          onToggleGroup: r,
          tabIndex: l === a.id ? 0 : -1,
          itemRef: d(a.id),
          onFocus: i,
          focusedId: l,
          setItemRef: d,
          onItemFocus: i,
          flyoutGroupId: p,
          onOpenFlyout: v,
          onCloseFlyout: b
        }
      );
    }
    default:
      return null;
  }
}, St = ({ controlId: a }) => {
  const t = P(), o = A(), n = W(yt), s = t.items ?? [], r = t.activeItemId, l = t.collapsed, [d, i] = Q(() => {
    const E = /* @__PURE__ */ new Map(), y = (D) => {
      for (const x of D)
        x.type === "group" && (E.set(x.id, x.expanded), y(x.children));
    };
    return y(s), E;
  }), c = U((E) => {
    i((y) => {
      const D = new Map(y), x = D.get(E) ?? !1;
      return D.set(E, !x), o("toggleGroup", { itemId: E, expanded: !x }), D;
    });
  }, [o]), p = U((E) => {
    E !== r && o("selectItem", { itemId: E });
  }, [o, r]), v = U((E) => {
    o("executeCommand", { itemId: E });
  }, [o]), b = U(() => {
    o("toggleCollapse", {});
  }, [o]), [N, C] = Q(null), m = U((E) => {
    C(E);
  }, []), _ = U(() => {
    C(null);
  }, []);
  ee(() => {
    l || C(null);
  }, [l]);
  const [T, w] = Q(() => {
    const E = ce(s, l, d);
    return E.length > 0 ? E[0].id : "";
  }), M = te(/* @__PURE__ */ new Map()), B = U((E) => (y) => {
    y ? M.current.set(E, y) : M.current.delete(E);
  }, []), u = U((E) => {
    w(E);
  }, []), h = te(0), k = U((E) => {
    w(E), h.current++;
  }, []);
  ee(() => {
    const E = M.current.get(T);
    E && document.activeElement !== E && E.focus();
  }, [T, h.current]);
  const F = U((E) => {
    if (E.key === "Escape" && N !== null) {
      E.preventDefault(), _();
      return;
    }
    const y = ce(s, l, d);
    if (y.length === 0) return;
    const D = y.findIndex((I) => I.id === T);
    if (D < 0) return;
    const x = y[D];
    switch (E.key) {
      case "ArrowDown": {
        E.preventDefault();
        const I = (D + 1) % y.length;
        k(y[I].id);
        break;
      }
      case "ArrowUp": {
        E.preventDefault();
        const I = (D - 1 + y.length) % y.length;
        k(y[I].id);
        break;
      }
      case "Home": {
        E.preventDefault(), k(y[0].id);
        break;
      }
      case "End": {
        E.preventDefault(), k(y[y.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        E.preventDefault(), x.type === "nav" ? p(x.id) : x.type === "command" ? v(x.id) : x.type === "group" && (l ? N === x.id ? _() : m(x.id) : c(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !l && ((d.get(x.id) ?? !1) || (E.preventDefault(), c(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !l && (d.get(x.id) ?? !1) && (E.preventDefault(), c(x.id));
        break;
      }
    }
  }, [
    s,
    l,
    d,
    T,
    N,
    k,
    p,
    v,
    c,
    m,
    _
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlSidebar" + (l ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, l ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: F }, s.map((E) => /* @__PURE__ */ e.createElement(
    ge,
    {
      key: E.id,
      item: E,
      activeItemId: r,
      collapsed: l,
      onSelect: p,
      onExecute: v,
      onToggleGroup: c,
      focusedId: T,
      setItemRef: B,
      onItemFocus: u,
      groupStates: d,
      flyoutGroupId: N,
      onOpenFlyout: m,
      onCloseFlyout: _
    }
  ))), l ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(R, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(R, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: b,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(R, { control: t.activeContent })));
}, Dt = ({ controlId: a }) => {
  const t = P(), o = t.direction ?? "column", n = t.gap ?? "default", s = t.align ?? "stretch", r = t.wrap === !0, l = t.children ?? [], d = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${s}`,
    r ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: d }, l.map((i, c) => /* @__PURE__ */ e.createElement(R, { key: c, control: i })));
}, Rt = ({ controlId: a }) => {
  const t = P(), o = t.columns, n = t.minColumnWidth, s = t.gap ?? "default", r = t.children ?? [], l = {};
  return n ? l.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : o && (l.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: a, className: `tlGrid tlGrid--gap-${s}`, style: l }, r.map((d, i) => /* @__PURE__ */ e.createElement(R, { key: i, control: d })));
}, Pt = ({ controlId: a }) => {
  const t = P(), o = t.title, n = t.variant ?? "outlined", s = t.padding ?? "default", r = t.headerActions ?? [], l = t.child, d = o != null || r.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, r.map((i, c) => /* @__PURE__ */ e.createElement(R, { key: c, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(R, { control: l })));
}, Bt = ({ controlId: a }) => {
  const t = P(), o = t.title ?? "", n = t.leading, s = t.actions ?? [], r = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    r === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: a, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(R, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, c) => /* @__PURE__ */ e.createElement(R, { key: c, control: i }))));
}, { useCallback: Mt } = e, Ft = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.items ?? [], s = Mt((r) => {
    o("navigate", { itemId: r });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((r, l) => {
    const d = l === n.length - 1;
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
    ), d ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, r.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => s(r.id)
      },
      r.label
    ));
  })));
}, { useCallback: jt } = e, It = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.items ?? [], s = t.activeItemId, r = jt((l) => {
    l !== s && o("selectItem", { itemId: l });
  }, [o, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: a, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((l) => {
    const d = l.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: l.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => r(l.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + l.icon, "aria-hidden": "true" }), l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, l.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, l.label)
    );
  }));
}, { useCallback: pe, useEffect: fe, useRef: zt } = e, At = {
  "js.dialog.close": "Close"
}, $t = ({ controlId: a }) => {
  const t = P(), o = A(), n = W(At), s = t.open === !0, r = t.title ?? "", l = t.size ?? "medium", d = t.closeOnBackdrop !== !1, i = t.actions ?? [], c = t.child, p = zt(null), v = pe(() => {
    o("close");
  }, [o]), b = pe((C) => {
    d && C.target === C.currentTarget && v();
  }, [d, v]);
  if (fe(() => {
    if (!s) return;
    const C = (m) => {
      m.key === "Escape" && v();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [s, v]), fe(() => {
    s && p.current && p.current.focus();
  }, [s]), !s) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlDialog__backdrop", onClick: b }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${l}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: p,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, r), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: v,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(R, { control: c })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((C, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: C })))
  ));
}, { useCallback: Vt, useEffect: Ot } = e, Ut = {
  "js.drawer.close": "Close"
}, Wt = ({ controlId: a }) => {
  const t = P(), o = A(), n = W(Ut), s = t.open === !0, r = t.position ?? "right", l = t.size ?? "medium", d = t.title ?? null, i = t.child, c = Vt(() => {
    o("close");
  }, [o]);
  Ot(() => {
    if (!s) return;
    const v = (b) => {
      b.key === "Escape" && c();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [s, c]);
  const p = [
    "tlDrawer",
    `tlDrawer--${r}`,
    `tlDrawer--${l}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: a, className: p, "aria-hidden": !s }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, i && /* @__PURE__ */ e.createElement(R, { control: i })));
}, { useCallback: be, useEffect: Kt, useState: Ht } = e, Gt = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.message ?? "", s = t.variant ?? "info", r = t.action, l = t.duration ?? 5e3, d = t.visible === !0, [i, c] = Ht(!1), p = be(() => {
    c(!0), setTimeout(() => {
      o("dismiss"), c(!1);
    }, 200);
  }, [o]), v = be(() => {
    r && o(r.commandName), p();
  }, [o, r, p]);
  return Kt(() => {
    if (!d || l === 0) return;
    const b = setTimeout(p, l);
    return () => clearTimeout(b);
  }, [d, l, p]), !d && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: v }, r.label)
  );
}, { useCallback: re, useEffect: se, useRef: Yt, useState: he } = e, Xt = ({ controlId: a }) => {
  const t = P(), o = A(), n = t.open === !0, s = t.anchorId, r = t.items ?? [], l = Yt(null), [d, i] = he({ top: 0, left: 0 }), [c, p] = he(0), v = r.filter((m) => m.type === "item" && !m.disabled);
  se(() => {
    var u, h;
    if (!n || !s) return;
    const m = document.getElementById(s);
    if (!m) return;
    const _ = m.getBoundingClientRect(), T = ((u = l.current) == null ? void 0 : u.offsetHeight) ?? 200, w = ((h = l.current) == null ? void 0 : h.offsetWidth) ?? 200;
    let M = _.bottom + 4, B = _.left;
    M + T > window.innerHeight && (M = _.top - T - 4), B + w > window.innerWidth && (B = _.right - w), i({ top: M, left: B }), p(0);
  }, [n, s]);
  const b = re(() => {
    o("close");
  }, [o]), N = re((m) => {
    o("selectItem", { itemId: m });
  }, [o]);
  se(() => {
    if (!n) return;
    const m = (_) => {
      l.current && !l.current.contains(_.target) && b();
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [n, b]);
  const C = re((m) => {
    if (m.key === "Escape") {
      b();
      return;
    }
    if (m.key === "ArrowDown")
      m.preventDefault(), p((_) => (_ + 1) % v.length);
    else if (m.key === "ArrowUp")
      m.preventDefault(), p((_) => (_ - 1 + v.length) % v.length);
    else if (m.key === "Enter" || m.key === " ") {
      m.preventDefault();
      const _ = v[c];
      _ && N(_.id);
    }
  }, [b, N, v, c]);
  return se(() => {
    n && l.current && l.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
      className: "tlMenu",
      role: "menu",
      ref: l,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: C
    },
    r.map((m, _) => {
      if (m.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: _, className: "tlMenu__separator" });
      const w = v.indexOf(m) === c;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: m.id,
          type: "button",
          className: "tlMenu__item" + (w ? " tlMenu__item--focused" : "") + (m.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: m.disabled,
          tabIndex: w ? 0 : -1,
          onClick: () => N(m.id)
        },
        m.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + m.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, m.label)
      );
    })
  ) : null;
}, qt = ({ controlId: a }) => {
  const t = P(), o = t.header, n = t.content, s = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: a, className: "tlAppShell" }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(R, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(R, { control: n })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(R, { control: s })), /* @__PURE__ */ e.createElement(R, { control: r }));
}, Zt = () => {
  const t = P().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Jt = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, _e = 50, Qt = () => {
  const a = P(), t = A(), o = W(Jt), n = a.columns ?? [], s = a.totalRowCount ?? 0, r = a.rows ?? [], l = a.rowHeight ?? 36, d = a.selectionMode ?? "single", i = a.selectedCount ?? 0, c = a.frozenColumnCount ?? 0, p = a.treeMode ?? !1, v = e.useMemo(
    () => n.filter((f) => f.sortPriority && f.sortPriority > 0).length,
    [n]
  ), b = d === "multi", N = 40, C = 20, m = e.useRef(null), _ = e.useRef(null), T = e.useRef(null), [w, M] = e.useState({}), B = e.useRef(null), u = e.useRef(!1), h = e.useRef(null), [k, F] = e.useState(null), [E, y] = e.useState(null);
  e.useEffect(() => {
    B.current || M({});
  }, [n]);
  const D = e.useCallback((f) => w[f.name] ?? f.width, [w]), x = e.useMemo(() => {
    const f = [];
    let g = b && c > 0 ? N : 0;
    for (let S = 0; S < c && S < n.length; S++)
      f.push(g), g += D(n[S]);
    return f;
  }, [n, c, b, N, D]), I = s * l, K = e.useCallback((f, g, S) => {
    S.preventDefault(), S.stopPropagation(), B.current = { column: f, startX: S.clientX, startWidth: g };
    const j = (V) => {
      const O = B.current;
      if (!O) return;
      const $ = Math.max(_e, O.startWidth + (V.clientX - O.startX));
      M((ae) => ({ ...ae, [O.column]: $ }));
    }, z = (V) => {
      document.removeEventListener("mousemove", j), document.removeEventListener("mouseup", z);
      const O = B.current;
      if (O) {
        const $ = Math.max(_e, O.startWidth + (V.clientX - O.startX));
        t("columnResize", { column: O.column, width: $ }), B.current = null, u.current = !0, requestAnimationFrame(() => {
          u.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", j), document.addEventListener("mouseup", z);
  }, [t]), H = e.useCallback(() => {
    m.current && _.current && (m.current.scrollLeft = _.current.scrollLeft), T.current !== null && clearTimeout(T.current), T.current = window.setTimeout(() => {
      const f = _.current;
      if (!f) return;
      const g = f.scrollTop, S = Math.ceil(f.clientHeight / l), j = Math.floor(g / l);
      t("scroll", { start: j, count: S });
    }, 80);
  }, [t, l]), G = e.useCallback((f, g, S) => {
    if (u.current) return;
    let j;
    !g || g === "desc" ? j = "asc" : j = "desc";
    const z = S.shiftKey ? "add" : "replace";
    t("sort", { column: f, direction: j, mode: z });
  }, [t]), Z = e.useCallback((f, g) => {
    h.current = f, g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", f);
  }, []), J = e.useCallback((f, g) => {
    if (!h.current || h.current === f) {
      F(null);
      return;
    }
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const S = g.currentTarget.getBoundingClientRect(), j = g.clientX < S.left + S.width / 2 ? "left" : "right";
    F({ column: f, side: j });
  }, []), Y = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation();
    const g = h.current;
    if (!g || !k) {
      h.current = null, F(null);
      return;
    }
    let S = n.findIndex((z) => z.name === k.column);
    if (S < 0) {
      h.current = null, F(null);
      return;
    }
    const j = n.findIndex((z) => z.name === g);
    k.side === "right" && S++, j < S && S--, t("columnReorder", { column: g, targetIndex: S }), h.current = null, F(null);
  }, [n, k, t]), ye = e.useCallback(() => {
    h.current = null, F(null);
  }, []), ke = e.useCallback((f, g) => {
    g.shiftKey && g.preventDefault(), t("select", {
      rowIndex: f,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), we = e.useCallback((f, g) => {
    g.stopPropagation(), t("select", { rowIndex: f, ctrlKey: !0, shiftKey: !1 });
  }, [t]), Ne = e.useCallback(() => {
    const f = i === s && s > 0;
    t("selectAll", { selected: !f });
  }, [t, i, s]), Te = e.useCallback((f, g, S) => {
    S.stopPropagation(), t("expand", { rowIndex: f, expanded: g });
  }, [t]), Le = e.useCallback((f, g) => {
    g.preventDefault(), y({ x: g.clientX, y: g.clientY, colIdx: f });
  }, []), xe = e.useCallback(() => {
    E && (t("setFrozenColumnCount", { count: E.colIdx + 1 }), y(null));
  }, [E, t]), Se = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), y(null);
  }, [t]);
  e.useEffect(() => {
    if (!E) return;
    const f = () => y(null), g = (S) => {
      S.key === "Escape" && y(null);
    };
    return document.addEventListener("mousedown", f), document.addEventListener("keydown", g), () => {
      document.removeEventListener("mousedown", f), document.removeEventListener("keydown", g);
    };
  }, [E]);
  const ne = n.reduce((f, g) => f + D(g), 0) + (b ? N : 0), De = i === s && s > 0, ue = i > 0 && i < s, Re = e.useCallback((f) => {
    f && (f.indeterminate = ue);
  }, [ue]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (f) => {
        if (!h.current) return;
        f.preventDefault();
        const g = _.current, S = m.current;
        if (!g) return;
        const j = g.getBoundingClientRect(), z = 40, V = 8;
        f.clientX < j.left + z ? g.scrollLeft = Math.max(0, g.scrollLeft - V) : f.clientX > j.right - z && (g.scrollLeft += V), S && (S.scrollLeft = g.scrollLeft);
      },
      onDrop: Y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: m }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ne } }, b && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...c > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (f) => {
          h.current && (f.preventDefault(), f.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== h.current && F({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Re,
          className: "tlTableView__checkbox",
          checked: De,
          onChange: Ne
        }
      )
    ), n.map((f, g) => {
      const S = D(f), j = g === n.length - 1;
      let z = "tlTableView__headerCell";
      f.sortable && (z += " tlTableView__headerCell--sortable"), k && k.column === f.name && (z += " tlTableView__headerCell--dragOver-" + k.side);
      const V = g < c, O = g === c - 1;
      return V && (z += " tlTableView__headerCell--frozen"), O && (z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: f.name,
          className: z,
          style: {
            ...j && !V ? { flex: "1 0 auto", minWidth: S } : { width: S, minWidth: S },
            position: V ? "sticky" : "relative",
            ...V ? { left: x[g], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: f.sortable ? ($) => G(f.name, f.sortDirection, $) : void 0,
          onContextMenu: ($) => Le(g, $),
          onDragStart: ($) => Z(f.name, $),
          onDragOver: ($) => J(f.name, $),
          onDrop: Y,
          onDragEnd: ye
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, f.label),
        f.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, f.sortDirection === "asc" ? "▲" : "▼", v > 1 && f.sortPriority != null && f.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, f.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: ($) => K(f.name, S, $)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (f) => {
          if (h.current && n.length > 0) {
            const g = n[n.length - 1];
            g.name !== h.current && (f.preventDefault(), f.dataTransfer.dropEffect = "move", F({ column: g.name, side: "right" }));
          }
        },
        onDrop: Y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: _,
        className: "tlTableView__body",
        onScroll: H
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: I, position: "relative", minWidth: ne } }, r.map((f) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: f.id,
          className: "tlTableView__row" + (f.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: f.index * l,
            height: l,
            minWidth: ne,
            width: "100%"
          },
          onClick: (g) => ke(f.index, g)
        },
        b && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (c > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: N,
              minWidth: N,
              ...c > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (g) => g.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: f.selected,
              onChange: () => {
              },
              onClick: (g) => we(f.index, g),
              tabIndex: -1
            }
          )
        ),
        n.map((g, S) => {
          const j = D(g), z = S === n.length - 1, V = S < c, O = S === c - 1;
          let $ = "tlTableView__cell";
          V && ($ += " tlTableView__cell--frozen"), O && ($ += " tlTableView__cell--frozenLast");
          const ae = p && S === 0, Pe = f.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: g.name,
              className: $,
              style: {
                ...z && !V ? { flex: "1 0 auto", minWidth: j } : { width: j, minWidth: j },
                ...V ? { position: "sticky", left: x[S], zIndex: 2 } : {}
              }
            },
            ae ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: Pe * C } }, f.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (Be) => Te(f.index, !f.expanded, Be)
              },
              f.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(R, { control: f.cells[g.name] })) : /* @__PURE__ */ e.createElement(R, { control: f.cells[g.name] })
          );
        })
      )))
    ),
    E && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: E.y, left: E.x, zIndex: 1e4 },
        onMouseDown: (f) => f.stopPropagation()
      },
      E.colIdx + 1 !== c && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: xe }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.freezeUpTo"])),
      c > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Se }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.unfreezeAll"]))
    )
  );
}, en = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ce = e.createContext(en), { useMemo: tn, useRef: nn, useState: an, useEffect: ln } = e, on = 320, rn = ({ controlId: a }) => {
  const t = P(), o = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", s = t.readOnly === !0, r = t.children ?? [], l = t.noModelMessage, d = nn(null), [i, c] = an(
    n === "top" ? "top" : "side"
  );
  ln(() => {
    if (n !== "auto") {
      c(n);
      return;
    }
    const C = d.current;
    if (!C) return;
    const m = new ResizeObserver((_) => {
      for (const T of _) {
        const M = T.contentRect.width / o;
        c(M < on ? "top" : "side");
      }
    });
    return m.observe(C), () => m.disconnect();
  }, [n, o]);
  const p = tn(() => ({
    readOnly: s,
    resolvedLabelPosition: i
  }), [s, i]), b = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / o))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return l ? /* @__PURE__ */ e.createElement("div", { id: a, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, l)) : /* @__PURE__ */ e.createElement(Ce.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: a, className: N, style: b, ref: d }, r.map((C, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: C }))));
}, { useCallback: sn } = e, cn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, dn = ({ controlId: a }) => {
  const t = P(), o = A(), n = W(cn), s = t.header, r = t.headerActions ?? [], l = t.collapsible === !0, d = t.collapsed === !0, i = t.border ?? "none", c = t.fullLine === !0, p = t.children ?? [], v = s != null || r.length > 0 || l, b = sn(() => {
    o("toggleCollapse");
  }, [o]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    c ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: N }, v && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, l && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: b,
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, r.map((C, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: C })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((C, m) => /* @__PURE__ */ e.createElement(R, { key: m, control: C }))));
}, { useContext: un, useState: mn, useCallback: pn } = e, fn = ({ controlId: a }) => {
  const t = P(), o = un(Ce), n = t.label ?? "", s = t.required === !0, r = t.error, l = t.helpText, d = t.dirty === !0, i = t.labelPosition ?? o.resolvedLabelPosition, c = t.fullLine === !0, p = t.visible !== !1, v = t.field, b = o.readOnly, [N, C] = mn(!1), m = pn(() => C((w) => !w), []);
  if (!p) return null;
  const _ = r != null, T = [
    "tlFormField",
    `tlFormField--${i}`,
    b ? "tlFormField--readonly" : "",
    c ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: a, className: T }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), s && !b && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), l && !b && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(R, { control: v })), !b && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, r)), !b && l && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, l));
}, bn = () => {
  const a = P(), t = A(), o = a.iconCss, n = a.iconSrc, s = a.label, r = a.cssClass, l = a.tooltip, d = a.hasLink, i = o ? /* @__PURE__ */ e.createElement("i", { className: o }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, c = /* @__PURE__ */ e.createElement(e.Fragment, null, i, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((b) => {
    b.preventDefault(), t("goto", {});
  }, [t]), v = ["tlResourceCell", r].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: v, href: "#", onClick: p, title: l }, c) : /* @__PURE__ */ e.createElement("span", { className: v, title: l }, c);
}, hn = 20, _n = () => {
  const a = P(), t = A(), o = a.nodes ?? [], n = a.selectionMode ?? "single", s = a.dragEnabled ?? !1, r = a.dropEnabled ?? !1, l = a.dropIndicatorNodeId ?? null, d = a.dropIndicatorPosition ?? null, [i, c] = e.useState(-1), p = e.useRef(null), v = e.useCallback((u, h) => {
    t(h ? "collapse" : "expand", { nodeId: u });
  }, [t]), b = e.useCallback((u, h) => {
    t("select", {
      nodeId: u,
      ctrlKey: h.ctrlKey || h.metaKey,
      shiftKey: h.shiftKey
    });
  }, [t]), N = e.useCallback((u, h) => {
    h.preventDefault(), t("contextMenu", { nodeId: u, x: h.clientX, y: h.clientY });
  }, [t]), C = e.useRef(null), m = e.useCallback((u, h) => {
    const k = h.getBoundingClientRect(), F = u.clientY - k.top, E = k.height / 3;
    return F < E ? "above" : F > E * 2 ? "below" : "within";
  }, []), _ = e.useCallback((u, h) => {
    h.dataTransfer.effectAllowed = "move", h.dataTransfer.setData("text/plain", u);
  }, []), T = e.useCallback((u, h) => {
    h.preventDefault(), h.dataTransfer.dropEffect = "move";
    const k = m(h, h.currentTarget);
    C.current != null && window.clearTimeout(C.current), C.current = window.setTimeout(() => {
      t("dragOver", { nodeId: u, position: k }), C.current = null;
    }, 50);
  }, [t, m]), w = e.useCallback((u, h) => {
    h.preventDefault(), C.current != null && (window.clearTimeout(C.current), C.current = null);
    const k = m(h, h.currentTarget);
    t("drop", { nodeId: u, position: k });
  }, [t, m]), M = e.useCallback(() => {
    C.current != null && (window.clearTimeout(C.current), C.current = null), t("dragEnd");
  }, [t]), B = e.useCallback((u) => {
    if (o.length === 0) return;
    let h = i;
    switch (u.key) {
      case "ArrowDown":
        u.preventDefault(), h = Math.min(i + 1, o.length - 1);
        break;
      case "ArrowUp":
        u.preventDefault(), h = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (u.preventDefault(), i >= 0 && i < o.length) {
          const k = o[i];
          if (k.expandable && !k.expanded) {
            t("expand", { nodeId: k.id });
            return;
          } else k.expanded && (h = i + 1);
        }
        break;
      case "ArrowLeft":
        if (u.preventDefault(), i >= 0 && i < o.length) {
          const k = o[i];
          if (k.expanded) {
            t("collapse", { nodeId: k.id });
            return;
          } else {
            const F = k.depth;
            for (let E = i - 1; E >= 0; E--)
              if (o[E].depth < F) {
                h = E;
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
        u.preventDefault(), h = 0;
        break;
      case "End":
        u.preventDefault(), h = o.length - 1;
        break;
      default:
        return;
    }
    h !== i && c(h);
  }, [i, o, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: B
    },
    o.map((u, h) => /* @__PURE__ */ e.createElement(
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
          h === i ? "tlTreeView__node--focused" : "",
          l === u.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          l === u.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          l === u.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: u.depth * hn },
        draggable: s,
        onClick: (k) => b(u.id, k),
        onContextMenu: (k) => N(u.id, k),
        onDragStart: (k) => _(u.id, k),
        onDragOver: r ? (k) => T(u.id, k) : void 0,
        onDrop: r ? (k) => w(u.id, k) : void 0,
        onDragEnd: M
      },
      u.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (k) => {
            k.stopPropagation(), v(u.id, u.expanded);
          },
          tabIndex: -1,
          "aria-label": u.expanded ? "Collapse" : "Expand"
        },
        u.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: u.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(R, { control: u.content }))
    ))
  );
}, { useCallback: En, useRef: Ee, useState: vn, useEffect: ve } = e, gn = ({ group: a }) => {
  const t = a.items.filter((o) => o != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((o, n) => /* @__PURE__ */ e.createElement("span", { key: n, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(R, { control: o }))));
}, Cn = ({ group: a }) => {
  var d, i;
  const [t, o] = vn(!1), n = Ee(null), s = Ee(null), r = En(() => {
    o((c) => !c);
  }, []);
  ve(() => {
    if (!t) return;
    const c = (p) => {
      s.current && !s.current.contains(p.target) && n.current && !n.current.contains(p.target) && o(!1);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [t]), ve(() => {
    if (!t) return;
    const c = (p) => {
      p.key === "Escape" && o(!1);
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [t]);
  const l = a.items.filter((c) => c != null);
  return l.length === 0 ? null : l.length === 1 && !((d = a.subGroups) != null && d.length) ? /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(R, { control: l[0] }))) : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: n,
      type: "button",
      className: "tlToolbar__menuTrigger",
      onClick: r,
      "aria-expanded": t,
      "aria-haspopup": "true"
    },
    a.icon && /* @__PURE__ */ e.createElement("i", { className: a.icon, "aria-hidden": "true" }),
    /* @__PURE__ */ e.createElement("span", null, a.label ?? a.name),
    /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" }))
  ), t && /* @__PURE__ */ e.createElement("div", { ref: s, className: "tlToolbar__dropdown", role: "menu" }, l.map((c, p) => /* @__PURE__ */ e.createElement("div", { key: p, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(R, { control: c }))), (i = a.subGroups) == null ? void 0 : i.map((c, p) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${p}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), c.items.map((v, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(R, { control: v })))))));
}, yn = ({ controlId: a }) => {
  const n = (P().groups ?? []).filter((s) => s.items.some((r) => r != null));
  return n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: a, className: "tlToolbar", role: "toolbar" }, n.map((s, r) => /* @__PURE__ */ e.createElement(e.Fragment, { key: s.name }, r > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), s.display === "menu" ? /* @__PURE__ */ e.createElement(Cn, { group: s }) : /* @__PURE__ */ e.createElement(gn, { group: s }))));
};
L("TLButton", Ge);
L("TLToggleButton", Xe);
L("TLTextInput", je);
L("TLNumberInput", ze);
L("TLDatePicker", $e);
L("TLSelect", Oe);
L("TLCheckbox", We);
L("TLTable", Ke);
L("TLCounter", qe);
L("TLTabBar", Je);
L("TLFieldList", Qe);
L("TLAudioRecorder", tt);
L("TLAudioPlayer", at);
L("TLFileUpload", ot);
L("TLDownload", st);
L("TLPhotoCapture", it);
L("TLPhotoViewer", ut);
L("TLSplitPanel", mt);
L("TLPanel", vt);
L("TLMaximizeRoot", gt);
L("TLDeckPane", Ct);
L("TLSidebar", St);
L("TLStack", Dt);
L("TLGrid", Rt);
L("TLCard", Pt);
L("TLAppBar", Bt);
L("TLBreadcrumb", Ft);
L("TLBottomBar", It);
L("TLDialog", $t);
L("TLDrawer", Wt);
L("TLSnackbar", Gt);
L("TLMenu", Xt);
L("TLAppShell", qt);
L("TLTextCell", Zt);
L("TLTableView", Qt);
L("TLFormLayout", rn);
L("TLFormGroup", dn);
L("TLFormField", fn);
L("TLResourceCell", bn);
L("TLTreeView", _n);
L("TLToolbar", yn);
