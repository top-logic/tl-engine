import { React as e, useTLFieldValue as q, getComponent as Ce, useTLState as P, useTLCommand as A, TLChild as B, useTLUpload as se, useI18N as W, useTLDataUrl as ce, register as T } from "tl-react-bridge";
const { useCallback: ye } = e, ke = ({ controlId: l, state: t }) => {
  const [n, r] = q(), s = ye(
    (a) => {
      r(a.target.value);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: l,
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: we } = e, Ne = ({ controlId: l, state: t, config: n }) => {
  const [r, s] = q(), a = we(
    (c) => {
      const i = c.target.value, u = i === "" ? null : Number(i);
      s(u);
    },
    [s]
  ), o = n != null && n.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: r != null ? String(r) : "",
      onChange: a,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Le } = e, Te = ({ controlId: l, state: t }) => {
  const [n, r] = q(), s = Le(
    (a) => {
      r(a.target.value || null);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: l,
      value: n ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: xe } = e, Se = ({ controlId: l, state: t, config: n }) => {
  var c;
  const [r, s] = q(), a = xe(
    (i) => {
      s(i.target.value || null);
    },
    [s]
  ), o = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const i = ((c = o.find((u) => u.value === r)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, i);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: r ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((i) => /* @__PURE__ */ e.createElement("option", { key: i.value, value: i.value }, i.label))
  );
}, { useCallback: De } = e, Re = ({ controlId: l, state: t }) => {
  const [n, r] = q(), s = De(
    (a) => {
      r(a.target.checked);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: n === !0,
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Pe = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((s, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, n.map((o) => {
    const c = o.cellModule ? Ce(o.cellModule) : void 0, i = s[o.name];
    if (c) {
      const u = { value: i, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: l + "-" + a + "-" + o.name,
          state: u
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Be } = e, je = ({ controlId: l, command: t, label: n, disabled: r }) => {
  const s = P(), a = A(), o = t ?? "click", c = n ?? s.label, i = r ?? s.disabled === !0, u = Be(() => {
    a(o);
  }, [a, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: u,
      disabled: i,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: Fe } = e, Me = ({ controlId: l, command: t, label: n, active: r, disabled: s }) => {
  const a = P(), o = A(), c = t ?? "click", i = n ?? a.label, u = r ?? a.active === !0, b = s ?? a.disabled === !0, p = Fe(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: b,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    i
  );
}, Ie = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ae } = e, ze = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.tabs ?? [], s = t.activeTabId, a = Ae((o) => {
    o !== s && n("selectTab", { tabId: o });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === s,
      className: "tlReactTabBar__tab" + (o.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(B, { control: t.activeContent })));
}, $e = ({ controlId: l }) => {
  const t = P(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((s, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(B, { control: s })))));
}, Ve = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Oe = ({ controlId: l }) => {
  const t = P(), n = se(), [r, s] = e.useState("idle"), [a, o] = e.useState(null), c = e.useRef(null), i = e.useRef([]), u = e.useRef(null), b = t.status ?? "idle", p = t.error, h = b === "received" ? "idle" : r !== "idle" ? r : b, N = e.useCallback(async () => {
    if (r === "recording") {
      const C = c.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (r !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        u.current = C, i.current = [];
        const S = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(C, S ? { mimeType: S } : void 0);
        c.current = j, j.ondataavailable = (d) => {
          d.data.size > 0 && i.current.push(d.data);
        }, j.onstop = async () => {
          C.getTracks().forEach((L) => L.stop()), u.current = null;
          const d = new Blob(i.current, { type: j.mimeType || "audio/webm" });
          if (i.current = [], d.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const E = new FormData();
          E.append("audio", d, "recording.webm"), await n(E), s("idle");
        }, j.start(), s("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), o("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [r, n]), v = W(Ve), m = h === "recording" ? v["js.audioRecorder.stop"] : h === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], f = h === "uploading", k = ["tlAudioRecorder__button"];
  return h === "recording" && k.push("tlAudioRecorder__button--recording"), h === "uploading" && k.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: N,
      disabled: f,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[a]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Ue = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, We = ({ controlId: l }) => {
  const t = P(), n = ce(), r = !!t.hasAudio, s = t.dataRevision ?? 0, [a, o] = e.useState(r ? "idle" : "disabled"), c = e.useRef(null), i = e.useRef(null), u = e.useRef(s);
  e.useEffect(() => {
    r ? a === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), o("disabled"));
  }, [r]), e.useEffect(() => {
    s !== u.current && (u.current = s, c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null), (a === "playing" || a === "paused" || a === "loading") && o("idle"));
  }, [s]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), i.current && (URL.revokeObjectURL(i.current), i.current = null);
  }, []);
  const b = e.useCallback(async () => {
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
        const f = await fetch(n);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const k = await f.blob();
        i.current = URL.createObjectURL(k);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const m = new Audio(i.current);
    c.current = m, m.onended = () => {
      o("idle");
    }, m.play(), o("playing");
  }, [a, n]), p = W(Ue), h = a === "loading" ? p["js.loading"] : a === "playing" ? p["js.audioPlayer.pause"] : a === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = a === "disabled" || a === "loading", v = ["tlAudioPlayer__button"];
  return a === "playing" && v.push("tlAudioPlayer__button--playing"), a === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: b,
      disabled: N,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ke = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, He = ({ controlId: l }) => {
  const t = P(), n = se(), [r, s] = e.useState("idle"), [a, o] = e.useState(!1), c = e.useRef(null), i = t.status ?? "idle", u = t.error, b = t.accept ?? "", p = i === "received" ? "idle" : r !== "idle" ? r : i, h = e.useCallback(async (d) => {
    s("uploading");
    const E = new FormData();
    E.append("file", d, d.name), await n(E), s("idle");
  }, [n]), N = e.useCallback((d) => {
    var L;
    const E = (L = d.target.files) == null ? void 0 : L[0];
    E && h(E);
  }, [h]), v = e.useCallback(() => {
    var d;
    r !== "uploading" && ((d = c.current) == null || d.click());
  }, [r]), m = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), o(!0);
  }, []), f = e.useCallback((d) => {
    d.preventDefault(), d.stopPropagation(), o(!1);
  }, []), k = e.useCallback((d) => {
    var L;
    if (d.preventDefault(), d.stopPropagation(), o(!1), r === "uploading") return;
    const E = (L = d.dataTransfer.files) == null ? void 0 : L[0];
    E && h(E);
  }, [r, h]), C = p === "uploading", S = W(Ke), j = p === "uploading" ? S["js.uploading"] : S["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: m,
      onDragLeave: f,
      onDrop: k
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: b || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: C,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
}, Ge = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ye = ({ controlId: l }) => {
  const t = P(), n = ce(), r = A(), s = !!t.hasData, a = t.dataRevision ?? 0, o = t.fileName ?? "download", c = !!t.clearable, [i, u] = e.useState(!1), b = e.useCallback(async () => {
    if (!(!s || i)) {
      u(!0);
      try {
        const v = n + (n.includes("?") ? "&" : "?") + "rev=" + a, m = await fetch(v);
        if (!m.ok) {
          console.error("[TLDownload] Failed to fetch data:", m.status);
          return;
        }
        const f = await m.blob(), k = URL.createObjectURL(f), C = document.createElement("a");
        C.href = k, C.download = o, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(k);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        u(!1);
      }
    }
  }, [s, i, n, a, o]), p = e.useCallback(async () => {
    s && await r("clear");
  }, [s, r]), h = W(Ge);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const N = i ? h["js.downloading"] : h["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (i ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: b,
      disabled: i,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Xe = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, qe = ({ controlId: l }) => {
  const t = P(), n = se(), [r, s] = e.useState("idle"), [a, o] = e.useState(null), [c, i] = e.useState(!1), u = e.useRef(null), b = e.useRef(null), p = e.useRef(null), h = e.useRef(null), N = e.useRef(null), v = t.error, m = e.useMemo(
    () => {
      var y;
      return !!(window.isSecureContext && ((y = navigator.mediaDevices) != null && y.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    b.current && (b.current.getTracks().forEach((y) => y.stop()), b.current = null), u.current && (u.current.srcObject = null);
  }, []), k = e.useCallback(() => {
    f(), s("idle");
  }, [f]), C = e.useCallback(async () => {
    var y;
    if (r !== "uploading") {
      if (o(null), !m) {
        (y = h.current) == null || y.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        b.current = D, s("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), o("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [r, m]), S = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const y = u.current, D = p.current;
    if (!y || !D)
      return;
    D.width = y.videoWidth, D.height = y.videoHeight;
    const x = D.getContext("2d");
    x && (x.drawImage(y, 0, 0), f(), s("uploading"), D.toBlob(async (M) => {
      if (!M) {
        s("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", M, "capture.jpg"), await n(K), s("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, f]), j = e.useCallback(async (y) => {
    var M;
    const D = (M = y.target.files) == null ? void 0 : M[0];
    if (!D) return;
    s("uploading");
    const x = new FormData();
    x.append("photo", D, D.name), await n(x), s("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && u.current && b.current && (u.current.srcObject = b.current);
  }, [r]), e.useEffect(() => {
    var D;
    if (r !== "overlayOpen") return;
    (D = N.current) == null || D.focus();
    const y = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = y;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const y = (D) => {
      D.key === "Escape" && k();
    };
    return document.addEventListener("keydown", y), () => document.removeEventListener("keydown", y);
  }, [r, k]), e.useEffect(() => () => {
    b.current && (b.current.getTracks().forEach((y) => y.stop()), b.current = null);
  }, []);
  const d = W(Xe), E = r === "uploading" ? d["js.uploading"] : d["js.photoCapture.open"], L = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && L.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  c && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const g = ["tlPhotoCapture__mirrorBtn"];
  return c && g.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: C,
      disabled: r === "uploading",
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !m && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: k }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: u,
        className: V.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: g.join(" "),
        onClick: () => i((y) => !y),
        title: d["js.photoCapture.mirror"],
        "aria-label": d["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: S,
        title: d["js.photoCapture.capture"],
        "aria-label": d["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: k,
        title: d["js.photoCapture.close"],
        "aria-label": d["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, d[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, Ze = {
  "js.photoViewer.alt": "Captured photo"
}, Je = ({ controlId: l }) => {
  const t = P(), n = ce(), r = !!t.hasPhoto, s = t.dataRevision ?? 0, [a, o] = e.useState(null), c = e.useRef(s);
  e.useEffect(() => {
    if (!r) {
      a && (URL.revokeObjectURL(a), o(null));
      return;
    }
    if (s === c.current && a)
      return;
    c.current = s, a && (URL.revokeObjectURL(a), o(null));
    let u = !1;
    return (async () => {
      try {
        const b = await fetch(n);
        if (!b.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", b.status);
          return;
        }
        const p = await b.blob();
        u || o(URL.createObjectURL(p));
      } catch (b) {
        console.error("[TLPhotoViewer] Fetch error:", b);
      }
    })(), () => {
      u = !0;
    };
  }, [r, s, n]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const i = W(Ze);
  return !r || !a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: i["js.photoViewer.alt"]
    }
  ));
}, { useCallback: de, useRef: ee } = e, Qe = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.orientation, s = t.resizable === !0, a = t.children ?? [], o = r === "horizontal", c = a.length > 0 && a.every((f) => f.collapsed), i = !c && a.some((f) => f.collapsed), u = c ? !o : o, b = ee(null), p = ee(null), h = ee(null), N = de((f, k) => {
    const C = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? c && !u ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : k !== void 0 ? C.flex = `0 0 ${k}px` : f.unit === "%" || i ? C.flex = `${f.size} 0 0%` : C.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (C.minWidth = o ? f.minSize : void 0, C.minHeight = o ? void 0 : f.minSize), C;
  }, [o, c, i, u]), v = de((f, k) => {
    f.preventDefault();
    const C = b.current;
    if (!C) return;
    const S = a[k], j = a[k + 1], d = C.querySelectorAll(":scope > .tlSplitPanel__child"), E = [];
    d.forEach((g) => {
      E.push(o ? g.offsetWidth : g.offsetHeight);
    }), h.current = E, p.current = {
      splitterIndex: k,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: E[k],
      startSizeAfter: E[k + 1],
      childBefore: S,
      childAfter: j
    };
    const L = (g) => {
      const y = p.current;
      if (!y || !h.current) return;
      const x = (o ? g.clientX : g.clientY) - y.startPos, M = y.childBefore.minSize || 0, K = y.childAfter.minSize || 0;
      let H = y.startSizeBefore + x, G = y.startSizeAfter - x;
      H < M && (G += H - M, H = M), G < K && (H += G - K, G = K), h.current[y.splitterIndex] = H, h.current[y.splitterIndex + 1] = G;
      const Z = C.querySelectorAll(":scope > .tlSplitPanel__child"), J = Z[y.splitterIndex], Y = Z[y.splitterIndex + 1];
      J && (J.style.flex = `0 0 ${H}px`), Y && (Y.style.flex = `0 0 ${G}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", L), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const g = {};
        a.forEach((y, D) => {
          const x = y.control;
          x != null && x.controlId && h.current && (g[x.controlId] = h.current[D]);
        }), n("updateSizes", { sizes: g });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", L), document.addEventListener("mouseup", V), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, o, n]), m = [];
  return a.forEach((f, k) => {
    if (m.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${k}`,
          className: `tlSplitPanel__child${f.collapsed && u ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(f)
        },
        /* @__PURE__ */ e.createElement(B, { control: f.control })
      )
    ), s && k < a.length - 1) {
      const C = a[k + 1];
      !f.collapsed && !C.collapsed && m.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${k}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (j) => v(j, k)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: b,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: u ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    m
  );
}, { useCallback: te } = e, et = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), nt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), at = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), lt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), rt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), ot = ({ controlId: l }) => {
  const t = P(), n = A(), r = W(et), s = t.title, a = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, c = t.showMaximize === !0, i = t.showPopOut === !0, u = t.toolbarButtons ?? [], b = a === "MINIMIZED", p = a === "MAXIMIZED", h = a === "HIDDEN", N = te(() => {
    n("toggleMinimize");
  }, [n]), v = te(() => {
    n("toggleMaximize");
  }, [n]), m = te(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const f = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${a.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, u.map((k, C) => /* @__PURE__ */ e.createElement("span", { key: C, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(B, { control: k }))), o && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: b ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      b ? /* @__PURE__ */ e.createElement(nt, null) : /* @__PURE__ */ e.createElement(tt, null)
    ), c && !b && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(lt, null) : /* @__PURE__ */ e.createElement(at, null)
    ), i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: m,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(rt, null)
    ))),
    !b && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(B, { control: t.child }))
  );
}, st = ({ controlId: l }) => {
  const t = P();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(B, { control: t.child })
  );
}, ct = ({ controlId: l }) => {
  const t = P();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(B, { control: t.activeChild }));
}, { useCallback: U, useState: ne, useEffect: Q, useRef: re } = e, it = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function oe(l, t, n, r) {
  const s = [];
  for (const a of l)
    a.type === "nav" ? s.push({ id: a.id, type: "nav", groupId: r }) : a.type === "command" ? s.push({ id: a.id, type: "command", groupId: r }) : a.type === "group" && (s.push({ id: a.id, type: "group" }), (n.get(a.id) ?? a.expanded) && !t && s.push(...oe(a.children, t, n, a.id)));
  return s;
}
const X = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, dt = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: s, itemRef: a, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: s,
    ref: a,
    onFocus: () => o(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(X, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), ut = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: s, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: s,
    onFocus: () => a(l.id)
  },
  /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), mt = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(X, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), pt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ft = ({ item: l, activeItemId: t, onSelect: n, onExecute: r, onClose: s }) => {
  const a = re(null);
  Q(() => {
    const c = (i) => {
      a.current && !a.current.contains(i.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [s]), Q(() => {
    const c = (i) => {
      i.key === "Escape" && s();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [s]);
  const o = U((c) => {
    c.type === "nav" ? (n(c.id), s()) : c.type === "command" && (r(c.id), s());
  }, [n, r, s]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((c) => {
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
        /* @__PURE__ */ e.createElement(X, { icon: c.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, c.label),
        c.type === "nav" && c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, c.badge)
      );
    }
    return c.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: c.id, className: "tlSidebar__flyoutSectionHeader" }, c.label) : c.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: c.id, className: "tlSidebar__separator" }) : null;
  }));
}, bt = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: s,
  onExecute: a,
  onToggleGroup: o,
  tabIndex: c,
  itemRef: i,
  onFocus: u,
  focusedId: b,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: N,
  onOpenFlyout: v,
  onCloseFlyout: m
}) => {
  const f = U(() => {
    r ? N === l.id ? m() : v(l.id) : o(l.id);
  }, [r, N, l.id, o, v, m]), k = r && N === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (k ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: f,
      title: r ? l.label : void 0,
      "aria-expanded": r ? k : t,
      tabIndex: c,
      ref: i,
      onFocus: () => u(l.id)
    },
    /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !r && /* @__PURE__ */ e.createElement(
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
  ), k && /* @__PURE__ */ e.createElement(
    ft,
    {
      item: l,
      activeItemId: n,
      onSelect: s,
      onExecute: a,
      onClose: m
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((C) => /* @__PURE__ */ e.createElement(
    he,
    {
      key: C.id,
      item: C,
      activeItemId: n,
      collapsed: r,
      onSelect: s,
      onExecute: a,
      onToggleGroup: o,
      focusedId: b,
      setItemRef: p,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: v,
      onCloseFlyout: m
    }
  ))));
}, he = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: s,
  onToggleGroup: a,
  focusedId: o,
  setItemRef: c,
  onItemFocus: i,
  groupStates: u,
  flyoutGroupId: b,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        dt,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ut,
        {
          item: l,
          collapsed: n,
          onExecute: s,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(mt, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(pt, null);
    case "group": {
      const N = u ? u.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        bt,
        {
          item: l,
          expanded: N,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: s,
          onToggleGroup: a,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: c(l.id),
          onFocus: i,
          focusedId: o,
          setItemRef: c,
          onItemFocus: i,
          flyoutGroupId: b,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, ht = ({ controlId: l }) => {
  const t = P(), n = A(), r = W(it), s = t.items ?? [], a = t.activeItemId, o = t.collapsed, [c, i] = ne(() => {
    const g = /* @__PURE__ */ new Map(), y = (D) => {
      for (const x of D)
        x.type === "group" && (g.set(x.id, x.expanded), y(x.children));
    };
    return y(s), g;
  }), u = U((g) => {
    i((y) => {
      const D = new Map(y), x = D.get(g) ?? !1;
      return D.set(g, !x), n("toggleGroup", { itemId: g, expanded: !x }), D;
    });
  }, [n]), b = U((g) => {
    g !== a && n("selectItem", { itemId: g });
  }, [n, a]), p = U((g) => {
    n("executeCommand", { itemId: g });
  }, [n]), h = U(() => {
    n("toggleCollapse", {});
  }, [n]), [N, v] = ne(null), m = U((g) => {
    v(g);
  }, []), f = U(() => {
    v(null);
  }, []);
  Q(() => {
    o || v(null);
  }, [o]);
  const [k, C] = ne(() => {
    const g = oe(s, o, c);
    return g.length > 0 ? g[0].id : "";
  }), S = re(/* @__PURE__ */ new Map()), j = U((g) => (y) => {
    y ? S.current.set(g, y) : S.current.delete(g);
  }, []), d = U((g) => {
    C(g);
  }, []), E = re(0), L = U((g) => {
    C(g), E.current++;
  }, []);
  Q(() => {
    const g = S.current.get(k);
    g && document.activeElement !== g && g.focus();
  }, [k, E.current]);
  const V = U((g) => {
    if (g.key === "Escape" && N !== null) {
      g.preventDefault(), f();
      return;
    }
    const y = oe(s, o, c);
    if (y.length === 0) return;
    const D = y.findIndex((M) => M.id === k);
    if (D < 0) return;
    const x = y[D];
    switch (g.key) {
      case "ArrowDown": {
        g.preventDefault();
        const M = (D + 1) % y.length;
        L(y[M].id);
        break;
      }
      case "ArrowUp": {
        g.preventDefault();
        const M = (D - 1 + y.length) % y.length;
        L(y[M].id);
        break;
      }
      case "Home": {
        g.preventDefault(), L(y[0].id);
        break;
      }
      case "End": {
        g.preventDefault(), L(y[y.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        g.preventDefault(), x.type === "nav" ? b(x.id) : x.type === "command" ? p(x.id) : x.type === "group" && (o ? N === x.id ? f() : m(x.id) : u(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !o && ((c.get(x.id) ?? !1) || (g.preventDefault(), u(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !o && (c.get(x.id) ?? !1) && (g.preventDefault(), u(x.id));
        break;
      }
    }
  }, [
    s,
    o,
    c,
    k,
    N,
    L,
    b,
    p,
    u,
    m,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(B, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(B, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, s.map((g) => /* @__PURE__ */ e.createElement(
    he,
    {
      key: g.id,
      item: g,
      activeItemId: a,
      collapsed: o,
      onSelect: b,
      onExecute: p,
      onToggleGroup: u,
      focusedId: k,
      setItemRef: j,
      onItemFocus: d,
      groupStates: c,
      flyoutGroupId: N,
      onOpenFlyout: m,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(B, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(B, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: o ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(B, { control: t.activeContent })));
}, _t = ({ controlId: l }) => {
  const t = P(), n = t.direction ?? "column", r = t.gap ?? "default", s = t.align ?? "stretch", a = t.wrap === !0, o = t.children ?? [], c = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${s}`,
    a ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: c }, o.map((i, u) => /* @__PURE__ */ e.createElement(B, { key: u, control: i })));
}, Et = ({ controlId: l }) => {
  const t = P(), n = t.columns, r = t.minColumnWidth, s = t.gap ?? "default", a = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (o.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: o }, a.map((c, i) => /* @__PURE__ */ e.createElement(B, { key: i, control: c })));
}, vt = ({ controlId: l }) => {
  const t = P(), n = t.title, r = t.variant ?? "outlined", s = t.padding ?? "default", a = t.headerActions ?? [], o = t.child, c = n != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((i, u) => /* @__PURE__ */ e.createElement(B, { key: u, control: i })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(B, { control: o })));
}, gt = ({ controlId: l }) => {
  const t = P(), n = t.title ?? "", r = t.leading, s = t.actions ?? [], a = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    a === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: c }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(B, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((i, u) => /* @__PURE__ */ e.createElement(B, { key: u, control: i }))));
}, { useCallback: Ct } = e, yt = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.items ?? [], s = Ct((a) => {
    n("navigate", { itemId: a });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((a, o) => {
    const c = o === r.length - 1;
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
}, { useCallback: kt } = e, wt = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.items ?? [], s = t.activeItemId, a = kt((o) => {
    o !== s && n("selectItem", { itemId: o });
  }, [n, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((o) => {
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
}, { useCallback: ue, useEffect: me, useRef: Nt } = e, Lt = {
  "js.dialog.close": "Close"
}, Tt = ({ controlId: l }) => {
  const t = P(), n = A(), r = W(Lt), s = t.open === !0, a = t.title ?? "", o = t.size ?? "medium", c = t.closeOnBackdrop !== !1, i = t.actions ?? [], u = t.child, b = Nt(null), p = ue(() => {
    n("close");
  }, [n]), h = ue((v) => {
    c && v.target === v.currentTarget && p();
  }, [c, p]);
  if (me(() => {
    if (!s) return;
    const v = (m) => {
      m.key === "Escape" && p();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [s, p]), me(() => {
    s && b.current && b.current.focus();
  }, [s]), !s) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: h }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: b,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: N }, a), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: p,
        title: r["js.dialog.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(B, { control: u })),
    i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, i.map((v, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: v })))
  ));
}, { useCallback: xt, useEffect: St } = e, Dt = {
  "js.drawer.close": "Close"
}, Rt = ({ controlId: l }) => {
  const t = P(), n = A(), r = W(Dt), s = t.open === !0, a = t.position ?? "right", o = t.size ?? "medium", c = t.title ?? null, i = t.child, u = xt(() => {
    n("close");
  }, [n]);
  St(() => {
    if (!s) return;
    const p = (h) => {
      h.key === "Escape" && u();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [s, u]);
  const b = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${o}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: b, "aria-hidden": !s }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: u,
      title: r["js.drawer.close"]
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
}, { useCallback: pe, useEffect: Pt, useState: Bt } = e, jt = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.message ?? "", s = t.variant ?? "info", a = t.action, o = t.duration ?? 5e3, c = t.visible === !0, [i, u] = Bt(!1), b = pe(() => {
    u(!0), setTimeout(() => {
      n("dismiss"), u(!1);
    }, 200);
  }, [n]), p = pe(() => {
    a && n(a.commandName), b();
  }, [n, a, b]);
  return Pt(() => {
    if (!c || o === 0) return;
    const h = setTimeout(b, o);
    return () => clearTimeout(h);
  }, [c, o, b]), !c && !i ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${i ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: p }, a.label)
  );
}, { useCallback: ae, useEffect: le, useRef: Ft, useState: fe } = e, Mt = ({ controlId: l }) => {
  const t = P(), n = A(), r = t.open === !0, s = t.anchorId, a = t.items ?? [], o = Ft(null), [c, i] = fe({ top: 0, left: 0 }), [u, b] = fe(0), p = a.filter((m) => m.type === "item" && !m.disabled);
  le(() => {
    var d, E;
    if (!r || !s) return;
    const m = document.getElementById(s);
    if (!m) return;
    const f = m.getBoundingClientRect(), k = ((d = o.current) == null ? void 0 : d.offsetHeight) ?? 200, C = ((E = o.current) == null ? void 0 : E.offsetWidth) ?? 200;
    let S = f.bottom + 4, j = f.left;
    S + k > window.innerHeight && (S = f.top - k - 4), j + C > window.innerWidth && (j = f.right - C), i({ top: S, left: j }), b(0);
  }, [r, s]);
  const h = ae(() => {
    n("close");
  }, [n]), N = ae((m) => {
    n("selectItem", { itemId: m });
  }, [n]);
  le(() => {
    if (!r) return;
    const m = (f) => {
      o.current && !o.current.contains(f.target) && h();
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [r, h]);
  const v = ae((m) => {
    if (m.key === "Escape") {
      h();
      return;
    }
    if (m.key === "ArrowDown")
      m.preventDefault(), b((f) => (f + 1) % p.length);
    else if (m.key === "ArrowUp")
      m.preventDefault(), b((f) => (f - 1 + p.length) % p.length);
    else if (m.key === "Enter" || m.key === " ") {
      m.preventDefault();
      const f = p[u];
      f && N(f.id);
    }
  }, [h, N, p, u]);
  return le(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: v
    },
    a.map((m, f) => {
      if (m.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const C = p.indexOf(m) === u;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: m.id,
          type: "button",
          className: "tlMenu__item" + (C ? " tlMenu__item--focused" : "") + (m.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: m.disabled,
          tabIndex: C ? 0 : -1,
          onClick: () => N(m.id)
        },
        m.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + m.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, m.label)
      );
    })
  ) : null;
}, It = ({ controlId: l }) => {
  const t = P(), n = t.header, r = t.content, s = t.footer, a = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(B, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(B, { control: r })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(B, { control: s })), /* @__PURE__ */ e.createElement(B, { control: a }));
}, At = () => {
  const t = P().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, be = 50, zt = () => {
  const l = P(), t = A(), n = l.columns ?? [], r = l.totalRowCount ?? 0, s = l.rows ?? [], a = l.rowHeight ?? 36, o = l.selectionMode ?? "single", c = l.selectedCount ?? 0, i = l.frozenColumnCount ?? 0, u = e.useMemo(
    () => n.filter((_) => _.sortPriority && _.sortPriority > 0).length,
    [n]
  ), b = o === "multi", p = 40, h = e.useRef(null), N = e.useRef(null), v = e.useRef(null), [m, f] = e.useState({}), k = e.useRef(null), C = e.useRef(!1), S = e.useRef(null), [j, d] = e.useState(null);
  e.useEffect(() => {
    k.current || f({});
  }, [n]);
  const E = e.useCallback((_) => m[_.name] ?? _.width, [m]), L = e.useMemo(() => {
    const _ = [];
    let w = b && i > 0 ? p : 0;
    for (let R = 0; R < i && R < n.length; R++)
      _.push(w), w += E(n[R]);
    return _;
  }, [n, i, b, p, E]), V = r * a, g = e.useCallback((_, w, R) => {
    R.preventDefault(), R.stopPropagation(), k.current = { column: _, startX: R.clientX, startWidth: w };
    const F = (z) => {
      const O = k.current;
      if (!O) return;
      const $ = Math.max(be, O.startWidth + (z.clientX - O.startX));
      f((ge) => ({ ...ge, [O.column]: $ }));
    }, I = (z) => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", I);
      const O = k.current;
      if (O) {
        const $ = Math.max(be, O.startWidth + (z.clientX - O.startX));
        t("columnResize", { column: O.column, width: $ }), k.current = null, C.current = !0, requestAnimationFrame(() => {
          C.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", I);
  }, [t]), y = e.useCallback(() => {
    h.current && N.current && (h.current.scrollLeft = N.current.scrollLeft), v.current !== null && clearTimeout(v.current), v.current = window.setTimeout(() => {
      const _ = N.current;
      if (!_) return;
      const w = _.scrollTop, R = Math.ceil(_.clientHeight / a), F = Math.floor(w / a);
      t("scroll", { start: F, count: R });
    }, 80);
  }, [t, a]), D = e.useCallback((_, w, R) => {
    if (C.current) return;
    let F;
    !w || w === "desc" ? F = "asc" : F = "desc";
    const I = R.shiftKey ? "add" : "replace";
    t("sort", { column: _, direction: F, mode: I });
  }, [t]), x = e.useCallback((_, w) => {
    S.current = _, w.dataTransfer.effectAllowed = "move", w.dataTransfer.setData("text/plain", _);
  }, []), M = e.useCallback((_, w) => {
    if (!S.current || S.current === _) {
      d(null);
      return;
    }
    w.preventDefault(), w.dataTransfer.dropEffect = "move";
    const R = w.currentTarget.getBoundingClientRect(), F = w.clientX < R.left + R.width / 2 ? "left" : "right";
    d({ column: _, side: F });
  }, []), K = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation();
    const w = S.current;
    if (!w || !j) {
      S.current = null, d(null);
      return;
    }
    let R = n.findIndex((I) => I.name === j.column);
    if (R < 0) {
      S.current = null, d(null);
      return;
    }
    const F = n.findIndex((I) => I.name === w);
    j.side === "right" && R++, F < R && R--, t("columnReorder", { column: w, targetIndex: R }), S.current = null, d(null);
  }, [n, j, t]), H = e.useCallback(() => {
    S.current = null, d(null);
  }, []), G = e.useCallback((_, w) => {
    w.shiftKey && w.preventDefault(), t("select", {
      rowIndex: _,
      ctrlKey: w.ctrlKey || w.metaKey,
      shiftKey: w.shiftKey
    });
  }, [t]), Z = e.useCallback((_, w) => {
    w.stopPropagation(), t("select", { rowIndex: _, ctrlKey: !0, shiftKey: !1 });
  }, [t]), J = e.useCallback(() => {
    const _ = c === r && r > 0;
    t("selectAll", { selected: !_ });
  }, [t, c, r]), Y = n.reduce((_, w) => _ + E(w), 0) + (b ? p : 0), Ee = c === r && r > 0, ie = c > 0 && c < r, ve = e.useCallback((_) => {
    _ && (_.indeterminate = ie);
  }, [ie]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (_) => {
        if (!S.current) return;
        _.preventDefault();
        const w = N.current, R = h.current;
        if (!w) return;
        const F = w.getBoundingClientRect(), I = 40, z = 8;
        _.clientX < F.left + I ? w.scrollLeft = Math.max(0, w.scrollLeft - z) : _.clientX > F.right - I && (w.scrollLeft += z), R && (R.scrollLeft = w.scrollLeft);
      },
      onDrop: K
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: Y } }, b && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: p,
          minWidth: p,
          ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (_) => {
          S.current && (_.preventDefault(), _.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== S.current && d({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ve,
          className: "tlTableView__checkbox",
          checked: Ee,
          onChange: J
        }
      )
    ), n.map((_, w) => {
      const R = E(_), F = w === n.length - 1;
      let I = "tlTableView__headerCell";
      _.sortable && (I += " tlTableView__headerCell--sortable"), j && j.column === _.name && (I += " tlTableView__headerCell--dragOver-" + j.side);
      const z = w < i, O = w === i - 1;
      return z && (I += " tlTableView__headerCell--frozen"), O && (I += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.name,
          className: I,
          style: {
            ...F && !z ? { flex: "1 0 auto", minWidth: R } : { width: R, minWidth: R },
            position: z ? "sticky" : "relative",
            ...z ? { left: L[w], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: _.sortable ? ($) => D(_.name, _.sortDirection, $) : void 0,
          onDragStart: ($) => x(_.name, $),
          onDragOver: ($) => M(_.name, $),
          onDrop: K,
          onDragEnd: H
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, _.label),
        _.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, _.sortDirection === "asc" ? "▲" : "▼", u > 1 && _.sortPriority != null && _.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, _.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: ($) => g(_.name, R, $)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (_) => {
          if (S.current && n.length > 0) {
            const w = n[n.length - 1];
            w.name !== S.current && (_.preventDefault(), _.dataTransfer.dropEffect = "move", d({ column: w.name, side: "right" }));
          }
        },
        onDrop: K
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: N,
        className: "tlTableView__body",
        onScroll: y
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: V, position: "relative", minWidth: Y } }, s.map((_) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: "tlTableView__row" + (_.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: _.index * a,
            height: a,
            minWidth: Y,
            width: "100%"
          },
          onClick: (w) => G(_.index, w)
        },
        b && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: p,
              minWidth: p,
              ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (w) => w.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: _.selected,
              onChange: () => {
              },
              onClick: (w) => Z(_.index, w),
              tabIndex: -1
            }
          )
        ),
        n.map((w, R) => {
          const F = E(w), I = R === n.length - 1, z = R < i, O = R === i - 1;
          let $ = "tlTableView__cell";
          return z && ($ += " tlTableView__cell--frozen"), O && ($ += " tlTableView__cell--frozenLast"), /* @__PURE__ */ e.createElement(
            "div",
            {
              key: w.name,
              className: $,
              style: {
                ...I && !z ? { flex: "1 0 auto", minWidth: F } : { width: F, minWidth: F },
                ...z ? { position: "sticky", left: L[R], zIndex: 2 } : {}
              }
            },
            /* @__PURE__ */ e.createElement(B, { control: _.cells[w.name] })
          );
        })
      )))
    )
  );
}, $t = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, _e = e.createContext($t), { useMemo: Vt, useRef: Ot, useState: Ut, useEffect: Wt } = e, Kt = 320, Ht = ({ controlId: l }) => {
  const t = P(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", s = t.readOnly === !0, a = t.children ?? [], o = Ot(null), [c, i] = Ut(
    r === "top" ? "top" : "side"
  );
  Wt(() => {
    if (r !== "auto") {
      i(r);
      return;
    }
    const N = o.current;
    if (!N) return;
    const v = new ResizeObserver((m) => {
      for (const f of m) {
        const C = f.contentRect.width / n;
        i(C < Kt ? "top" : "side");
      }
    });
    return v.observe(N), () => v.disconnect();
  }, [r, n]);
  const u = Vt(() => ({
    readOnly: s,
    resolvedLabelPosition: c
  }), [s, c]), p = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, h = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(_e.Provider, { value: u }, /* @__PURE__ */ e.createElement("div", { id: l, className: h, style: p, ref: o }, a.map((N, v) => /* @__PURE__ */ e.createElement(B, { key: v, control: N }))));
}, { useCallback: Gt } = e, Yt = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Xt = ({ controlId: l }) => {
  const t = P(), n = A(), r = W(Yt), s = t.header, a = t.headerActions ?? [], o = t.collapsible === !0, c = t.collapsed === !0, i = t.border ?? "none", u = t.fullLine === !0, b = t.children ?? [], p = s != null || a.length > 0 || o, h = Gt(() => {
    n("toggleCollapse");
  }, [n]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${i}`,
    u ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
      "aria-expanded": !c,
      title: c ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, a.map((v, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, b.map((v, m) => /* @__PURE__ */ e.createElement(B, { key: m, control: v }))));
}, { useContext: qt, useState: Zt, useCallback: Jt } = e, Qt = ({ controlId: l }) => {
  const t = P(), n = qt(_e), r = t.label ?? "", s = t.required === !0, a = t.error, o = t.helpText, c = t.dirty === !0, i = t.labelPosition ?? n.resolvedLabelPosition, u = t.fullLine === !0, b = t.visible !== !1, p = t.field, h = n.readOnly, [N, v] = Zt(!1), m = Jt(() => v((C) => !C), []);
  if (!b) return null;
  const f = a != null, k = [
    "tlFormField",
    `tlFormField--${i}`,
    h ? "tlFormField--readonly" : "",
    u ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: k }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), s && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !h && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(B, { control: p })), !h && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, a)), !h && o && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, en = () => {
  const l = P(), t = A(), n = l.iconCss, r = l.iconSrc, s = l.label, a = l.cssClass, o = l.tooltip, c = l.hasLink, i = n ? /* @__PURE__ */ e.createElement("i", { className: n }) : r ? /* @__PURE__ */ e.createElement("img", { src: r, className: "tlTypeIcon", alt: "" }) : null, u = /* @__PURE__ */ e.createElement(e.Fragment, null, i, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), b = e.useCallback((h) => {
    h.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", a].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: b, title: o }, u) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, u);
}, tn = 20, nn = () => {
  const l = P(), t = A(), n = l.nodes ?? [], r = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, a = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, c = l.dropIndicatorPosition ?? null, [i, u] = e.useState(-1), b = e.useRef(null), p = e.useCallback((d, E) => {
    t(E ? "collapse" : "expand", { nodeId: d });
  }, [t]), h = e.useCallback((d, E) => {
    t("select", {
      nodeId: d,
      ctrlKey: E.ctrlKey || E.metaKey,
      shiftKey: E.shiftKey
    });
  }, [t]), N = e.useCallback((d, E) => {
    E.preventDefault(), t("contextMenu", { nodeId: d, x: E.clientX, y: E.clientY });
  }, [t]), v = e.useRef(null), m = e.useCallback((d, E) => {
    const L = E.getBoundingClientRect(), V = d.clientY - L.top, g = L.height / 3;
    return V < g ? "above" : V > g * 2 ? "below" : "within";
  }, []), f = e.useCallback((d, E) => {
    E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", d);
  }, []), k = e.useCallback((d, E) => {
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const L = m(E, E.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: d, position: L }), v.current = null;
    }, 50);
  }, [t, m]), C = e.useCallback((d, E) => {
    E.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const L = m(E, E.currentTarget);
    t("drop", { nodeId: d, position: L });
  }, [t, m]), S = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((d) => {
    if (n.length === 0) return;
    let E = i;
    switch (d.key) {
      case "ArrowDown":
        d.preventDefault(), E = Math.min(i + 1, n.length - 1);
        break;
      case "ArrowUp":
        d.preventDefault(), E = Math.max(i - 1, 0);
        break;
      case "ArrowRight":
        if (d.preventDefault(), i >= 0 && i < n.length) {
          const L = n[i];
          if (L.expandable && !L.expanded) {
            t("expand", { nodeId: L.id });
            return;
          } else L.expanded && (E = i + 1);
        }
        break;
      case "ArrowLeft":
        if (d.preventDefault(), i >= 0 && i < n.length) {
          const L = n[i];
          if (L.expanded) {
            t("collapse", { nodeId: L.id });
            return;
          } else {
            const V = L.depth;
            for (let g = i - 1; g >= 0; g--)
              if (n[g].depth < V) {
                E = g;
                break;
              }
          }
        }
        break;
      case "Enter":
        d.preventDefault(), i >= 0 && i < n.length && t("select", {
          nodeId: n[i].id,
          ctrlKey: d.ctrlKey || d.metaKey,
          shiftKey: d.shiftKey
        });
        return;
      case " ":
        d.preventDefault(), r === "multi" && i >= 0 && i < n.length && t("select", {
          nodeId: n[i].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        d.preventDefault(), E = 0;
        break;
      case "End":
        d.preventDefault(), E = n.length - 1;
        break;
      default:
        return;
    }
    E !== i && u(E);
  }, [i, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: b,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    n.map((d, E) => /* @__PURE__ */ e.createElement(
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
          E === i ? "tlTreeView__node--focused" : "",
          o === d.id && c === "above" ? "tlTreeView__node--drop-above" : "",
          o === d.id && c === "within" ? "tlTreeView__node--drop-within" : "",
          o === d.id && c === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: d.depth * tn },
        draggable: s,
        onClick: (L) => h(d.id, L),
        onContextMenu: (L) => N(d.id, L),
        onDragStart: (L) => f(d.id, L),
        onDragOver: a ? (L) => k(d.id, L) : void 0,
        onDrop: a ? (L) => C(d.id, L) : void 0,
        onDragEnd: S
      },
      d.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (L) => {
            L.stopPropagation(), p(d.id, d.expanded);
          },
          tabIndex: -1,
          "aria-label": d.expanded ? "Collapse" : "Expand"
        },
        d.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: d.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(B, { control: d.content }))
    ))
  );
};
T("TLButton", je);
T("TLToggleButton", Me);
T("TLTextInput", ke);
T("TLNumberInput", Ne);
T("TLDatePicker", Te);
T("TLSelect", Se);
T("TLCheckbox", Re);
T("TLTable", Pe);
T("TLCounter", Ie);
T("TLTabBar", ze);
T("TLFieldList", $e);
T("TLAudioRecorder", Oe);
T("TLAudioPlayer", We);
T("TLFileUpload", He);
T("TLDownload", Ye);
T("TLPhotoCapture", qe);
T("TLPhotoViewer", Je);
T("TLSplitPanel", Qe);
T("TLPanel", ot);
T("TLMaximizeRoot", st);
T("TLDeckPane", ct);
T("TLSidebar", ht);
T("TLStack", _t);
T("TLGrid", Et);
T("TLCard", vt);
T("TLAppBar", gt);
T("TLBreadcrumb", yt);
T("TLBottomBar", wt);
T("TLDialog", Tt);
T("TLDrawer", Rt);
T("TLSnackbar", jt);
T("TLMenu", Mt);
T("TLAppShell", It);
T("TLTextCell", At);
T("TLTableView", zt);
T("TLFormLayout", Ht);
T("TLFormGroup", Xt);
T("TLFormField", Qt);
T("TLResourceCell", en);
T("TLTreeView", nn);
