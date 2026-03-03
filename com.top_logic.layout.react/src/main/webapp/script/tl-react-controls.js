import { React as e, useTLFieldValue as Y, getComponent as Ee, useTLState as S, useTLCommand as I, TLChild as T, useTLUpload as se, useI18N as V, useTLDataUrl as ce, register as N } from "tl-react-bridge";
const { useCallback: ve } = e, ge = ({ controlId: s, state: t }) => {
  const [a, r] = Y(), n = ve(
    (l) => {
      r(l.target.value);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: s, className: "tlReactTextInput tlReactTextInput--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: s,
      value: a ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Ce } = e, ye = ({ controlId: s, state: t, config: a }) => {
  const [r, n] = Y(), l = Ce(
    (c) => {
      const d = c.target.value, p = d === "" ? null : Number(d);
      n(p);
    },
    [n]
  ), o = a != null && a.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: s, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: s,
      value: r != null ? String(r) : "",
      onChange: l,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: ke } = e, we = ({ controlId: s, state: t }) => {
  const [a, r] = Y(), n = ke(
    (l) => {
      r(l.target.value || null);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: s, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: s,
      value: a ?? "",
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ne } = e, Le = ({ controlId: s, state: t, config: a }) => {
  var c;
  const [r, n] = Y(), l = Ne(
    (d) => {
      n(d.target.value || null);
    },
    [n]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const d = ((c = o.find((p) => p.value === r)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: s, className: "tlReactSelect tlReactSelect--immutable" }, d);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: s,
      value: r ?? "",
      onChange: l,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((d) => /* @__PURE__ */ e.createElement("option", { key: d.value, value: d.value }, d.label))
  );
}, { useCallback: xe } = e, Se = ({ controlId: s, state: t }) => {
  const [a, r] = Y(), n = xe(
    (l) => {
      r(l.target.checked);
    },
    [r]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: s,
      checked: a === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: s,
      checked: a === !0,
      onChange: n,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Te = ({ controlId: s, state: t }) => {
  const a = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: s, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((n) => /* @__PURE__ */ e.createElement("th", { key: n.name }, n.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((n, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((o) => {
    const c = o.cellModule ? Ee(o.cellModule) : void 0, d = n[o.name];
    if (c) {
      const p = { value: d, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: s + "-" + l + "-" + o.name,
          state: p
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: Re } = e, De = ({ controlId: s, command: t, label: a, disabled: r }) => {
  const n = S(), l = I(), o = t ?? "click", c = a ?? n.label, d = r ?? n.disabled === !0, p = Re(() => {
    l(o);
  }, [l, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: s,
      onClick: p,
      disabled: d,
      className: "tlReactButton"
    },
    c
  );
}, { useCallback: Be } = e, Pe = ({ controlId: s, command: t, label: a, active: r, disabled: n }) => {
  const l = S(), o = I(), c = t ?? "click", d = a ?? l.label, p = r ?? l.active === !0, h = n ?? l.disabled === !0, i = Be(() => {
    o(c);
  }, [o, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: s,
      onClick: i,
      disabled: h,
      className: "tlReactButton" + (p ? " tlReactButtonActive" : "")
    },
    d
  );
}, je = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.count ?? 0, n = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Fe } = e, Me = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.tabs ?? [], n = t.activeTabId, l = Fe((o) => {
    o !== n && a("selectTab", { tabId: o });
  }, [a, n]);
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === n,
      className: "tlReactTabBar__tab" + (o.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => l(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, Ie = ({ controlId: s }) => {
  const t = S(), a = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((n, l) => /* @__PURE__ */ e.createElement("div", { key: l, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(T, { control: n })))));
}, Ae = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, $e = ({ controlId: s }) => {
  const t = S(), a = se(), [r, n] = e.useState("idle"), [l, o] = e.useState(null), c = e.useRef(null), d = e.useRef([]), p = e.useRef(null), h = t.status ?? "idle", i = t.error, u = h === "received" ? "idle" : r !== "idle" ? r : h, v = e.useCallback(async () => {
    if (r === "recording") {
      const E = c.current;
      E && E.state !== "inactive" && E.stop();
      return;
    }
    if (r !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const E = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        p.current = E, d.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(E, D ? { mimeType: D } : void 0);
        c.current = P, P.ondataavailable = (w) => {
          w.data.size > 0 && d.current.push(w.data);
        }, P.onstop = async () => {
          E.getTracks().forEach((j) => j.stop()), p.current = null;
          const w = new Blob(d.current, { type: P.mimeType || "audio/webm" });
          if (d.current = [], w.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const B = new FormData();
          B.append("audio", w, "recording.webm"), await a(B), n("idle");
        }, P.start(), n("recording");
      } catch (E) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", E), o("js.audioRecorder.error.denied"), n("idle");
      }
    }
  }, [r, a]), C = V(Ae), m = u === "recording" ? C["js.audioRecorder.stop"] : u === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], f = u === "uploading", b = ["tlAudioRecorder__button"];
  return u === "recording" && b.push("tlAudioRecorder__button--recording"), u === "uploading" && b.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: s, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: v,
      disabled: f,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${u === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[l]), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, ze = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ve = ({ controlId: s }) => {
  const t = S(), a = ce(), r = !!t.hasAudio, n = t.dataRevision ?? 0, [l, o] = e.useState(r ? "idle" : "disabled"), c = e.useRef(null), d = e.useRef(null), p = e.useRef(n);
  e.useEffect(() => {
    r ? l === "disabled" && o("idle") : (c.current && (c.current.pause(), c.current = null), d.current && (URL.revokeObjectURL(d.current), d.current = null), o("disabled"));
  }, [r]), e.useEffect(() => {
    n !== p.current && (p.current = n, c.current && (c.current.pause(), c.current = null), d.current && (URL.revokeObjectURL(d.current), d.current = null), (l === "playing" || l === "paused" || l === "loading") && o("idle"));
  }, [n]), e.useEffect(() => () => {
    c.current && (c.current.pause(), c.current = null), d.current && (URL.revokeObjectURL(d.current), d.current = null);
  }, []);
  const h = e.useCallback(async () => {
    if (l === "disabled" || l === "loading")
      return;
    if (l === "playing") {
      c.current && c.current.pause(), o("paused");
      return;
    }
    if (l === "paused" && c.current) {
      c.current.play(), o("playing");
      return;
    }
    if (!d.current) {
      o("loading");
      try {
        const f = await fetch(a);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const b = await f.blob();
        d.current = URL.createObjectURL(b);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const m = new Audio(d.current);
    c.current = m, m.onended = () => {
      o("idle");
    }, m.play(), o("playing");
  }, [l, a]), i = V(ze), u = l === "loading" ? i["js.loading"] : l === "playing" ? i["js.audioPlayer.pause"] : l === "disabled" ? i["js.audioPlayer.noAudio"] : i["js.audioPlayer.play"], v = l === "disabled" || l === "loading", C = ["tlAudioPlayer__button"];
  return l === "playing" && C.push("tlAudioPlayer__button--playing"), l === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: s, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: h,
      disabled: v,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${l === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Oe = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ue = ({ controlId: s }) => {
  const t = S(), a = se(), [r, n] = e.useState("idle"), [l, o] = e.useState(!1), c = e.useRef(null), d = t.status ?? "idle", p = t.error, h = t.accept ?? "", i = d === "received" ? "idle" : r !== "idle" ? r : d, u = e.useCallback(async (w) => {
    n("uploading");
    const B = new FormData();
    B.append("file", w, w.name), await a(B), n("idle");
  }, [a]), v = e.useCallback((w) => {
    var j;
    const B = (j = w.target.files) == null ? void 0 : j[0];
    B && u(B);
  }, [u]), C = e.useCallback(() => {
    var w;
    r !== "uploading" && ((w = c.current) == null || w.click());
  }, [r]), m = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!0);
  }, []), f = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation(), o(!1);
  }, []), b = e.useCallback((w) => {
    var j;
    if (w.preventDefault(), w.stopPropagation(), o(!1), r === "uploading") return;
    const B = (j = w.dataTransfer.files) == null ? void 0 : j[0];
    B && u(B);
  }, [r, u]), E = i === "uploading", D = V(Oe), P = i === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      className: `tlFileUpload${l ? " tlFileUpload--dragover" : ""}`,
      onDragOver: m,
      onDragLeave: f,
      onDrop: b
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: h || void 0,
        onChange: v,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (i === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: C,
        disabled: E,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    p && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, p)
  );
}, We = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ke = ({ controlId: s }) => {
  const t = S(), a = ce(), r = I(), n = !!t.hasData, l = t.dataRevision ?? 0, o = t.fileName ?? "download", c = !!t.clearable, [d, p] = e.useState(!1), h = e.useCallback(async () => {
    if (!(!n || d)) {
      p(!0);
      try {
        const C = a + (a.includes("?") ? "&" : "?") + "rev=" + l, m = await fetch(C);
        if (!m.ok) {
          console.error("[TLDownload] Failed to fetch data:", m.status);
          return;
        }
        const f = await m.blob(), b = URL.createObjectURL(f), E = document.createElement("a");
        E.href = b, E.download = o, E.style.display = "none", document.body.appendChild(E), E.click(), document.body.removeChild(E), URL.revokeObjectURL(b);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        p(!1);
      }
    }
  }, [n, d, a, l, o]), i = e.useCallback(async () => {
    n && await r("clear");
  }, [n, r]), u = V(We);
  if (!n)
    return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, u["js.download.noFile"]));
  const v = d ? u["js.downloading"] : u["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (d ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: h,
      disabled: d,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: i,
      title: u["js.download.clear"],
      "aria-label": u["js.download.clearFile"]
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
}, Ge = ({ controlId: s }) => {
  const t = S(), a = se(), [r, n] = e.useState("idle"), [l, o] = e.useState(null), [c, d] = e.useState(!1), p = e.useRef(null), h = e.useRef(null), i = e.useRef(null), u = e.useRef(null), v = e.useRef(null), C = t.error, m = e.useMemo(
    () => {
      var g;
      return !!(window.isSecureContext && ((g = navigator.mediaDevices) != null && g.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    h.current && (h.current.getTracks().forEach((g) => g.stop()), h.current = null), p.current && (p.current.srcObject = null);
  }, []), b = e.useCallback(() => {
    f(), n("idle");
  }, [f]), E = e.useCallback(async () => {
    var g;
    if (r !== "uploading") {
      if (o(null), !m) {
        (g = u.current) == null || g.click();
        return;
      }
      try {
        const x = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        h.current = x, n("overlayOpen");
      } catch (x) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", x), o("js.photoCapture.error.denied"), n("idle");
      }
    }
  }, [r, m]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const g = p.current, x = i.current;
    if (!g || !x)
      return;
    x.width = g.videoWidth, x.height = g.videoHeight;
    const L = x.getContext("2d");
    L && (L.drawImage(g, 0, 0), f(), n("uploading"), x.toBlob(async (F) => {
      if (!F) {
        n("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", F, "capture.jpg"), await a(K), n("idle");
    }, "image/jpeg", 0.85));
  }, [r, a, f]), P = e.useCallback(async (g) => {
    var F;
    const x = (F = g.target.files) == null ? void 0 : F[0];
    if (!x) return;
    n("uploading");
    const L = new FormData();
    L.append("photo", x, x.name), await a(L), n("idle"), u.current && (u.current.value = "");
  }, [a]);
  e.useEffect(() => {
    r === "overlayOpen" && p.current && h.current && (p.current.srcObject = h.current);
  }, [r]), e.useEffect(() => {
    var x;
    if (r !== "overlayOpen") return;
    (x = v.current) == null || x.focus();
    const g = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = g;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const g = (x) => {
      x.key === "Escape" && b();
    };
    return document.addEventListener("keydown", g), () => document.removeEventListener("keydown", g);
  }, [r, b]), e.useEffect(() => () => {
    h.current && (h.current.getTracks().forEach((g) => g.stop()), h.current = null);
  }, []);
  const w = V(He), B = r === "uploading" ? w["js.uploading"] : w["js.photoCapture.open"], j = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && j.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  c && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const y = ["tlPhotoCapture__mirrorBtn"];
  return c && y.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: s, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: j.join(" "),
      onClick: E,
      disabled: r === "uploading",
      title: B,
      "aria-label": B
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !m && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: u,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: i, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: v,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: b }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: p,
        className: U.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: y.join(" "),
        onClick: () => d((g) => !g),
        title: w["js.photoCapture.mirror"],
        "aria-label": w["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: w["js.photoCapture.capture"],
        "aria-label": w["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: b,
        title: w["js.photoCapture.close"],
        "aria-label": w["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), l && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, w[l]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, Xe = {
  "js.photoViewer.alt": "Captured photo"
}, Ye = ({ controlId: s }) => {
  const t = S(), a = ce(), r = !!t.hasPhoto, n = t.dataRevision ?? 0, [l, o] = e.useState(null), c = e.useRef(n);
  e.useEffect(() => {
    if (!r) {
      l && (URL.revokeObjectURL(l), o(null));
      return;
    }
    if (n === c.current && l)
      return;
    c.current = n, l && (URL.revokeObjectURL(l), o(null));
    let p = !1;
    return (async () => {
      try {
        const h = await fetch(a);
        if (!h.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", h.status);
          return;
        }
        const i = await h.blob();
        p || o(URL.createObjectURL(i));
      } catch (h) {
        console.error("[TLPhotoViewer] Fetch error:", h);
      }
    })(), () => {
      p = !0;
    };
  }, [r, n, a]), e.useEffect(() => () => {
    l && URL.revokeObjectURL(l);
  }, []);
  const d = V(Xe);
  return !r || !l ? /* @__PURE__ */ e.createElement("div", { id: s, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: s, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: l,
      alt: d["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ie, useRef: ee } = e, qe = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.orientation, n = t.resizable === !0, l = t.children ?? [], o = r === "horizontal", c = l.length > 0 && l.every((f) => f.collapsed), d = !c && l.some((f) => f.collapsed), p = c ? !o : o, h = ee(null), i = ee(null), u = ee(null), v = ie((f, b) => {
    const E = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? c && !p ? E.flex = "1 0 0%" : E.flex = "0 0 auto" : b !== void 0 ? E.flex = `0 0 ${b}px` : f.unit === "%" || d ? E.flex = `${f.size} 0 0%` : E.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (E.minWidth = o ? f.minSize : void 0, E.minHeight = o ? void 0 : f.minSize), E;
  }, [o, c, d, p]), C = ie((f, b) => {
    f.preventDefault();
    const E = h.current;
    if (!E) return;
    const D = l[b], P = l[b + 1], w = E.querySelectorAll(":scope > .tlSplitPanel__child"), B = [];
    w.forEach((y) => {
      B.push(o ? y.offsetWidth : y.offsetHeight);
    }), u.current = B, i.current = {
      splitterIndex: b,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: B[b],
      startSizeAfter: B[b + 1],
      childBefore: D,
      childAfter: P
    };
    const j = (y) => {
      const g = i.current;
      if (!g || !u.current) return;
      const L = (o ? y.clientX : y.clientY) - g.startPos, F = g.childBefore.minSize || 0, K = g.childAfter.minSize || 0;
      let H = g.startSizeBefore + L, O = g.startSizeAfter - L;
      H < F && (O += H - F, H = F), O < K && (H += O - K, O = K), u.current[g.splitterIndex] = H, u.current[g.splitterIndex + 1] = O;
      const q = E.querySelectorAll(":scope > .tlSplitPanel__child"), X = q[g.splitterIndex], Z = q[g.splitterIndex + 1];
      X && (X.style.flex = `0 0 ${H}px`), Z && (Z.style.flex = `0 0 ${O}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", j), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", u.current) {
        const y = {};
        l.forEach((g, x) => {
          const L = g.control;
          L != null && L.controlId && u.current && (y[L.controlId] = u.current[x]);
        }), a("updateSizes", { sizes: y });
      }
      u.current = null, i.current = null;
    };
    document.addEventListener("mousemove", j), document.addEventListener("mouseup", U), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [l, o, a]), m = [];
  return l.forEach((f, b) => {
    if (m.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${b}`,
          className: `tlSplitPanel__child${f.collapsed && p ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(f)
        },
        /* @__PURE__ */ e.createElement(T, { control: f.control })
      )
    ), n && b < l.length - 1) {
      const E = l[b + 1];
      !f.collapsed && !E.collapsed && m.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${b}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (P) => C(P, b)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: h,
      id: s,
      className: `tlSplitPanel tlSplitPanel--${r}${c ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: p ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    m
  );
}, { useCallback: te } = e, Ze = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Je = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Qe = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), et = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), tt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), at = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), nt = ({ controlId: s }) => {
  const t = S(), a = I(), r = V(Ze), n = t.title, l = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, c = t.showMaximize === !0, d = t.showPopOut === !0, p = t.toolbarButtons ?? [], h = l === "MINIMIZED", i = l === "MAXIMIZED", u = l === "HIDDEN", v = te(() => {
    a("toggleMinimize");
  }, [a]), C = te(() => {
    a("toggleMaximize");
  }, [a]), m = te(() => {
    a("popOut");
  }, [a]);
  if (u)
    return null;
  const f = i ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      className: `tlPanel tlPanel--${l.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, p.map((b, E) => /* @__PURE__ */ e.createElement("span", { key: E, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(T, { control: b }))), o && !i && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: h ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      h ? /* @__PURE__ */ e.createElement(Qe, null) : /* @__PURE__ */ e.createElement(Je, null)
    ), c && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: i ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      i ? /* @__PURE__ */ e.createElement(tt, null) : /* @__PURE__ */ e.createElement(et, null)
    ), d && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: m,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(at, null)
    ))),
    !h && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(T, { control: t.child }))
  );
}, lt = ({ controlId: s }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(T, { control: t.child })
  );
}, rt = ({ controlId: s }) => {
  const t = S();
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(T, { control: t.activeChild }));
}, { useCallback: z, useState: ae, useEffect: J, useRef: re } = e, ot = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function oe(s, t, a, r) {
  const n = [];
  for (const l of s)
    l.type === "nav" ? n.push({ id: l.id, type: "nav", groupId: r }) : l.type === "command" ? n.push({ id: l.id, type: "command", groupId: r }) : l.type === "group" && (n.push({ id: l.id, type: "group" }), (a.get(l.id) ?? l.expanded) && !t && n.push(...oe(l.children, t, a, l.id)));
  return n;
}
const G = ({ icon: s }) => s ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + s, "aria-hidden": "true" }) : null, st = ({ item: s, active: t, collapsed: a, onSelect: r, tabIndex: n, itemRef: l, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(s.id),
    title: a ? s.label : void 0,
    tabIndex: n,
    ref: l,
    onFocus: () => o(s.id)
  },
  a && s.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(G, { icon: s.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, s.badge)) : /* @__PURE__ */ e.createElement(G, { icon: s.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
  !a && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
), ct = ({ item: s, collapsed: t, onExecute: a, tabIndex: r, itemRef: n, onFocus: l }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(s.id),
    title: t ? s.label : void 0,
    tabIndex: r,
    ref: n,
    onFocus: () => l(s.id)
  },
  /* @__PURE__ */ e.createElement(G, { icon: s.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label)
), it = ({ item: s, collapsed: t }) => t && !s.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? s.label : void 0 }, /* @__PURE__ */ e.createElement(G, { icon: s.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label)), dt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), ut = ({ item: s, activeItemId: t, onSelect: a, onExecute: r, onClose: n }) => {
  const l = re(null);
  J(() => {
    const c = (d) => {
      l.current && !l.current.contains(d.target) && setTimeout(() => n(), 0);
    };
    return document.addEventListener("mousedown", c), () => document.removeEventListener("mousedown", c);
  }, [n]), J(() => {
    const c = (d) => {
      d.key === "Escape" && n();
    };
    return document.addEventListener("keydown", c), () => document.removeEventListener("keydown", c);
  }, [n]);
  const o = z((c) => {
    c.type === "nav" ? (a(c.id), n()) : c.type === "command" && (r(c.id), n());
  }, [a, r, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: l, role: "menu" }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, s.label), s.children.map((c) => {
    if (c.type === "nav" || c.type === "command") {
      const d = c.type === "nav" && c.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: c.id,
          className: "tlSidebar__flyoutItem" + (d ? " tlSidebar__flyoutItem--active" : ""),
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
}, mt = ({
  item: s,
  expanded: t,
  activeItemId: a,
  collapsed: r,
  onSelect: n,
  onExecute: l,
  onToggleGroup: o,
  tabIndex: c,
  itemRef: d,
  onFocus: p,
  focusedId: h,
  setItemRef: i,
  onItemFocus: u,
  flyoutGroupId: v,
  onOpenFlyout: C,
  onCloseFlyout: m
}) => {
  const f = z(() => {
    r ? v === s.id ? m() : C(s.id) : o(s.id);
  }, [r, v, s.id, o, C, m]), b = r && v === s.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: f,
      title: r ? s.label : void 0,
      "aria-expanded": r ? b : t,
      tabIndex: c,
      ref: d,
      onFocus: () => p(s.id)
    },
    /* @__PURE__ */ e.createElement(G, { icon: s.icon }),
    !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
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
  ), b && /* @__PURE__ */ e.createElement(
    ut,
    {
      item: s,
      activeItemId: a,
      onSelect: n,
      onExecute: l,
      onClose: m
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, s.children.map((E) => /* @__PURE__ */ e.createElement(
    he,
    {
      key: E.id,
      item: E,
      activeItemId: a,
      collapsed: r,
      onSelect: n,
      onExecute: l,
      onToggleGroup: o,
      focusedId: h,
      setItemRef: i,
      onItemFocus: u,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: C,
      onCloseFlyout: m
    }
  ))));
}, he = ({
  item: s,
  activeItemId: t,
  collapsed: a,
  onSelect: r,
  onExecute: n,
  onToggleGroup: l,
  focusedId: o,
  setItemRef: c,
  onItemFocus: d,
  groupStates: p,
  flyoutGroupId: h,
  onOpenFlyout: i,
  onCloseFlyout: u
}) => {
  switch (s.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        st,
        {
          item: s,
          active: s.id === t,
          collapsed: a,
          onSelect: r,
          tabIndex: o === s.id ? 0 : -1,
          itemRef: c(s.id),
          onFocus: d
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        ct,
        {
          item: s,
          collapsed: a,
          onExecute: n,
          tabIndex: o === s.id ? 0 : -1,
          itemRef: c(s.id),
          onFocus: d
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(it, { item: s, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(dt, null);
    case "group": {
      const v = p ? p.get(s.id) ?? s.expanded : s.expanded;
      return /* @__PURE__ */ e.createElement(
        mt,
        {
          item: s,
          expanded: v,
          activeItemId: t,
          collapsed: a,
          onSelect: r,
          onExecute: n,
          onToggleGroup: l,
          tabIndex: o === s.id ? 0 : -1,
          itemRef: c(s.id),
          onFocus: d,
          focusedId: o,
          setItemRef: c,
          onItemFocus: d,
          flyoutGroupId: h,
          onOpenFlyout: i,
          onCloseFlyout: u
        }
      );
    }
    default:
      return null;
  }
}, pt = ({ controlId: s }) => {
  const t = S(), a = I(), r = V(ot), n = t.items ?? [], l = t.activeItemId, o = t.collapsed, [c, d] = ae(() => {
    const y = /* @__PURE__ */ new Map(), g = (x) => {
      for (const L of x)
        L.type === "group" && (y.set(L.id, L.expanded), g(L.children));
    };
    return g(n), y;
  }), p = z((y) => {
    d((g) => {
      const x = new Map(g), L = x.get(y) ?? !1;
      return x.set(y, !L), a("toggleGroup", { itemId: y, expanded: !L }), x;
    });
  }, [a]), h = z((y) => {
    y !== l && a("selectItem", { itemId: y });
  }, [a, l]), i = z((y) => {
    a("executeCommand", { itemId: y });
  }, [a]), u = z(() => {
    a("toggleCollapse", {});
  }, [a]), [v, C] = ae(null), m = z((y) => {
    C(y);
  }, []), f = z(() => {
    C(null);
  }, []);
  J(() => {
    o || C(null);
  }, [o]);
  const [b, E] = ae(() => {
    const y = oe(n, o, c);
    return y.length > 0 ? y[0].id : "";
  }), D = re(/* @__PURE__ */ new Map()), P = z((y) => (g) => {
    g ? D.current.set(y, g) : D.current.delete(y);
  }, []), w = z((y) => {
    E(y);
  }, []), B = re(0), j = z((y) => {
    E(y), B.current++;
  }, []);
  J(() => {
    const y = D.current.get(b);
    y && document.activeElement !== y && y.focus();
  }, [b, B.current]);
  const U = z((y) => {
    if (y.key === "Escape" && v !== null) {
      y.preventDefault(), f();
      return;
    }
    const g = oe(n, o, c);
    if (g.length === 0) return;
    const x = g.findIndex((F) => F.id === b);
    if (x < 0) return;
    const L = g[x];
    switch (y.key) {
      case "ArrowDown": {
        y.preventDefault();
        const F = (x + 1) % g.length;
        j(g[F].id);
        break;
      }
      case "ArrowUp": {
        y.preventDefault();
        const F = (x - 1 + g.length) % g.length;
        j(g[F].id);
        break;
      }
      case "Home": {
        y.preventDefault(), j(g[0].id);
        break;
      }
      case "End": {
        y.preventDefault(), j(g[g.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        y.preventDefault(), L.type === "nav" ? h(L.id) : L.type === "command" ? i(L.id) : L.type === "group" && (o ? v === L.id ? f() : m(L.id) : p(L.id));
        break;
      }
      case "ArrowRight": {
        L.type === "group" && !o && ((c.get(L.id) ?? !1) || (y.preventDefault(), p(L.id)));
        break;
      }
      case "ArrowLeft": {
        L.type === "group" && !o && (c.get(L.id) ?? !1) && (y.preventDefault(), p(L.id));
        break;
      }
    }
  }, [
    n,
    o,
    c,
    b,
    v,
    j,
    h,
    i,
    p,
    m,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, n.map((y) => /* @__PURE__ */ e.createElement(
    he,
    {
      key: y.id,
      item: y,
      activeItemId: l,
      collapsed: o,
      onSelect: h,
      onExecute: i,
      onToggleGroup: p,
      focusedId: b,
      setItemRef: P,
      onItemFocus: w,
      groupStates: c,
      flyoutGroupId: v,
      onOpenFlyout: m,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(T, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(T, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: u,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(T, { control: t.activeContent })));
}, ft = ({ controlId: s }) => {
  const t = S(), a = t.direction ?? "column", r = t.gap ?? "default", n = t.align ?? "stretch", l = t.wrap === !0, o = t.children ?? [], c = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${n}`,
    l ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: s, className: c }, o.map((d, p) => /* @__PURE__ */ e.createElement(T, { key: p, control: d })));
}, ht = ({ controlId: s }) => {
  const t = S(), a = t.columns, r = t.minColumnWidth, n = t.gap ?? "default", l = t.children ?? [], o = {};
  return r ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: s, className: `tlGrid tlGrid--gap-${n}`, style: o }, l.map((c, d) => /* @__PURE__ */ e.createElement(T, { key: d, control: c })));
}, bt = ({ controlId: s }) => {
  const t = S(), a = t.title, r = t.variant ?? "outlined", n = t.padding ?? "default", l = t.headerActions ?? [], o = t.child, c = a != null || l.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: s, className: `tlCard tlCard--${r}` }, c && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), l.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, l.map((d, p) => /* @__PURE__ */ e.createElement(T, { key: p, control: d })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${n}` }, /* @__PURE__ */ e.createElement(T, { control: o })));
}, _t = ({ controlId: s }) => {
  const t = S(), a = t.title ?? "", r = t.leading, n = t.actions ?? [], l = t.variant ?? "flat", c = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    l === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: s, className: c }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(T, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), n.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, n.map((d, p) => /* @__PURE__ */ e.createElement(T, { key: p, control: d }))));
}, { useCallback: Et } = e, vt = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.items ?? [], n = Et((l) => {
    a("navigate", { itemId: l });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: s, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((l, o) => {
    const c = o === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: l.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), c ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, l.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => n(l.id)
      },
      l.label
    ));
  })));
}, { useCallback: gt } = e, Ct = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.items ?? [], n = t.activeItemId, l = gt((o) => {
    o !== n && a("selectItem", { itemId: o });
  }, [a, n]);
  return /* @__PURE__ */ e.createElement("nav", { id: s, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((o) => {
    const c = o.id === n;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (c ? " tlBottomBar__item--active" : ""),
        onClick: () => l(o.id),
        "aria-current": c ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: de, useEffect: ue, useRef: yt } = e, kt = {
  "js.dialog.close": "Close"
}, wt = ({ controlId: s }) => {
  const t = S(), a = I(), r = V(kt), n = t.open === !0, l = t.title ?? "", o = t.size ?? "medium", c = t.closeOnBackdrop !== !1, d = t.actions ?? [], p = t.child, h = yt(null), i = de(() => {
    a("close");
  }, [a]), u = de((C) => {
    c && C.target === C.currentTarget && i();
  }, [c, i]);
  if (ue(() => {
    if (!n) return;
    const C = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [n, i]), ue(() => {
    n && h.current && h.current.focus();
  }, [n]), !n) return null;
  const v = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlDialog__backdrop", onClick: u }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${o}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": v,
      ref: h,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDialog__title", id: v }, l), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDialog__closeBtn",
        onClick: i,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(T, { control: p })),
    d.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, d.map((C, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: C })))
  ));
}, { useCallback: Nt, useEffect: Lt } = e, xt = {
  "js.drawer.close": "Close"
}, St = ({ controlId: s }) => {
  const t = S(), a = I(), r = V(xt), n = t.open === !0, l = t.position ?? "right", o = t.size ?? "medium", c = t.title ?? null, d = t.child, p = Nt(() => {
    a("close");
  }, [a]);
  Lt(() => {
    if (!n) return;
    const i = (u) => {
      u.key === "Escape" && p();
    };
    return document.addEventListener("keydown", i), () => document.removeEventListener("keydown", i);
  }, [n, p]);
  const h = [
    "tlDrawer",
    `tlDrawer--${l}`,
    `tlDrawer--${o}`,
    n ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: s, className: h, "aria-hidden": !n }, c !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, c), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: p,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, d && /* @__PURE__ */ e.createElement(T, { control: d })));
}, { useCallback: me, useEffect: Tt, useState: Rt } = e, Dt = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.message ?? "", n = t.variant ?? "info", l = t.action, o = t.duration ?? 5e3, c = t.visible === !0, [d, p] = Rt(!1), h = me(() => {
    p(!0), setTimeout(() => {
      a("dismiss"), p(!1);
    }, 200);
  }, [a]), i = me(() => {
    l && a(l.commandName), h();
  }, [a, l, h]);
  return Tt(() => {
    if (!c || o === 0) return;
    const u = setTimeout(h, o);
    return () => clearTimeout(u);
  }, [c, o, h]), !c && !d ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      className: `tlSnackbar tlSnackbar--${n}${d ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r),
    l && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: i }, l.label)
  );
}, { useCallback: ne, useEffect: le, useRef: Bt, useState: pe } = e, Pt = ({ controlId: s }) => {
  const t = S(), a = I(), r = t.open === !0, n = t.anchorId, l = t.items ?? [], o = Bt(null), [c, d] = pe({ top: 0, left: 0 }), [p, h] = pe(0), i = l.filter((m) => m.type === "item" && !m.disabled);
  le(() => {
    var w, B;
    if (!r || !n) return;
    const m = document.getElementById(n);
    if (!m) return;
    const f = m.getBoundingClientRect(), b = ((w = o.current) == null ? void 0 : w.offsetHeight) ?? 200, E = ((B = o.current) == null ? void 0 : B.offsetWidth) ?? 200;
    let D = f.bottom + 4, P = f.left;
    D + b > window.innerHeight && (D = f.top - b - 4), P + E > window.innerWidth && (P = f.right - E), d({ top: D, left: P }), h(0);
  }, [r, n]);
  const u = ne(() => {
    a("close");
  }, [a]), v = ne((m) => {
    a("selectItem", { itemId: m });
  }, [a]);
  le(() => {
    if (!r) return;
    const m = (f) => {
      o.current && !o.current.contains(f.target) && u();
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [r, u]);
  const C = ne((m) => {
    if (m.key === "Escape") {
      u();
      return;
    }
    if (m.key === "ArrowDown")
      m.preventDefault(), h((f) => (f + 1) % i.length);
    else if (m.key === "ArrowUp")
      m.preventDefault(), h((f) => (f - 1 + i.length) % i.length);
    else if (m.key === "Enter" || m.key === " ") {
      m.preventDefault();
      const f = i[p];
      f && v(f.id);
    }
  }, [u, v, i, p]);
  return le(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: c.top, left: c.left },
      onKeyDown: C
    },
    l.map((m, f) => {
      if (m.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const E = i.indexOf(m) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: m.id,
          type: "button",
          className: "tlMenu__item" + (E ? " tlMenu__item--focused" : "") + (m.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: m.disabled,
          tabIndex: E ? 0 : -1,
          onClick: () => v(m.id)
        },
        m.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + m.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, m.label)
      );
    })
  ) : null;
}, jt = ({ controlId: s }) => {
  const t = S(), a = t.header, r = t.content, n = t.footer, l = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: s, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(T, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(T, { control: r })), n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(T, { control: n })), /* @__PURE__ */ e.createElement(T, { control: l }));
}, Ft = () => {
  const t = S().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, fe = 50, Mt = () => {
  const s = S(), t = I(), a = s.columns ?? [], r = s.totalRowCount ?? 0, n = s.rows ?? [], l = s.rowHeight ?? 36, o = s.selectionMode ?? "single", c = s.selectedCount ?? 0, d = o === "multi", p = 40, h = e.useRef(null), i = e.useRef(null), u = e.useRef(null), [v, C] = e.useState({}), m = e.useRef(null), f = e.useRef(!1), b = e.useRef(null), [E, D] = e.useState(null);
  e.useEffect(() => {
    m.current || C({});
  }, [a]);
  const P = (_) => v[_.name] ?? _.width, w = r * l, B = e.useCallback((_, k, R) => {
    R.preventDefault(), R.stopPropagation(), m.current = { column: _, startX: R.clientX, startWidth: k };
    const M = ($) => {
      const W = m.current;
      if (!W) return;
      const Q = Math.max(fe, W.startWidth + ($.clientX - W.startX));
      C((_e) => ({ ..._e, [W.column]: Q }));
    }, A = ($) => {
      document.removeEventListener("mousemove", M), document.removeEventListener("mouseup", A);
      const W = m.current;
      if (W) {
        const Q = Math.max(fe, W.startWidth + ($.clientX - W.startX));
        t("columnResize", { column: W.column, width: Q }), m.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", M), document.addEventListener("mouseup", A);
  }, [t]), j = e.useCallback(() => {
    h.current && i.current && (h.current.scrollLeft = i.current.scrollLeft), u.current !== null && clearTimeout(u.current), u.current = window.setTimeout(() => {
      const _ = i.current;
      if (!_) return;
      const k = _.scrollTop, R = Math.ceil(_.clientHeight / l), M = Math.floor(k / l);
      t("scroll", { start: M, count: R });
    }, 80);
  }, [t, l]), U = e.useCallback((_, k) => {
    if (f.current) return;
    let R;
    !k || k === "desc" ? R = "asc" : R = "desc", t("sort", { column: _, direction: R });
  }, [t]), y = e.useCallback((_, k) => {
    b.current = _, k.dataTransfer.effectAllowed = "move", k.dataTransfer.setData("text/plain", _);
  }, []), g = e.useCallback((_, k) => {
    if (!b.current || b.current === _) {
      D(null);
      return;
    }
    k.preventDefault(), k.dataTransfer.dropEffect = "move";
    const R = k.currentTarget.getBoundingClientRect(), M = k.clientX < R.left + R.width / 2 ? "left" : "right";
    D({ column: _, side: M });
  }, []), x = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation();
    const k = b.current;
    if (!k || !E) {
      b.current = null, D(null);
      return;
    }
    let R = a.findIndex((A) => A.name === E.column);
    if (R < 0) {
      b.current = null, D(null);
      return;
    }
    const M = a.findIndex((A) => A.name === k);
    E.side === "right" && R++, M < R && R--, t("columnReorder", { column: k, targetIndex: R }), b.current = null, D(null);
  }, [a, E, t]), L = e.useCallback(() => {
    b.current = null, D(null);
  }, []), F = e.useCallback((_, k) => {
    k.shiftKey && k.preventDefault(), t("select", {
      rowIndex: _,
      ctrlKey: k.ctrlKey || k.metaKey,
      shiftKey: k.shiftKey
    });
  }, [t]), K = e.useCallback((_, k) => {
    k.stopPropagation(), t("select", { rowIndex: _, ctrlKey: !0, shiftKey: !1 });
  }, [t]), H = e.useCallback(() => {
    const _ = c === r && r > 0;
    t("selectAll", { selected: !_ });
  }, [t, c, r]), O = a.reduce((_, k) => _ + P(k), 0) + (d ? p : 0), q = c === r && r > 0, X = c > 0 && c < r, Z = e.useCallback((_) => {
    _ && (_.indeterminate = X);
  }, [X]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (_) => {
        if (!b.current) return;
        _.preventDefault();
        const k = i.current, R = h.current;
        if (!k) return;
        const M = k.getBoundingClientRect(), A = 40, $ = 8;
        _.clientX < M.left + A ? k.scrollLeft = Math.max(0, k.scrollLeft - $) : _.clientX > M.right - A && (k.scrollLeft += $), R && (R.scrollLeft = k.scrollLeft);
      },
      onDrop: x
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: h }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: O } }, d && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell",
        style: { width: p, minWidth: p },
        onDragOver: (_) => {
          b.current && (_.preventDefault(), _.dataTransfer.dropEffect = "move", a.length > 0 && a[0].name !== b.current && D({ column: a[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Z,
          className: "tlTableView__checkbox",
          checked: q,
          onChange: H
        }
      )
    ), a.map((_, k) => {
      const R = P(_), M = k === a.length - 1;
      let A = "tlTableView__headerCell";
      return _.sortable && (A += " tlTableView__headerCell--sortable"), E && E.column === _.name && (A += " tlTableView__headerCell--dragOver-" + E.side), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.name,
          className: A,
          style: M ? { flex: "1 0 auto", minWidth: R, position: "relative" } : { width: R, minWidth: R, position: "relative" },
          draggable: !0,
          onClick: _.sortable ? () => U(_.name, _.sortDirection) : void 0,
          onDragStart: ($) => y(_.name, $),
          onDragOver: ($) => g(_.name, $),
          onDrop: x,
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, _.label),
        _.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, _.sortDirection === "asc" ? "▲" : "▼"),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: ($) => B(_.name, R, $)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (_) => {
          if (b.current && a.length > 0) {
            const k = a[a.length - 1];
            k.name !== b.current && (_.preventDefault(), _.dataTransfer.dropEffect = "move", D({ column: k.name, side: "right" }));
          }
        },
        onDrop: x
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: i,
        className: "tlTableView__body",
        onScroll: j
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: w, position: "relative", minWidth: O } }, n.map((_) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: "tlTableView__row" + (_.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: _.index * l,
            height: l,
            minWidth: O,
            width: "100%"
          },
          onClick: (k) => F(_.index, k)
        },
        d && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell",
            style: { width: p, minWidth: p },
            onClick: (k) => k.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: _.selected,
              onChange: () => {
              },
              onClick: (k) => K(_.index, k),
              tabIndex: -1
            }
          )
        ),
        a.map((k, R) => {
          const M = P(k), A = R === a.length - 1;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: k.name,
              className: "tlTableView__cell",
              style: A ? { flex: "1 0 auto", minWidth: M } : { width: M, minWidth: M }
            },
            /* @__PURE__ */ e.createElement(T, { control: _.cells[k.name] })
          );
        })
      )))
    )
  );
}, It = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, be = e.createContext(It), { useMemo: At, useRef: $t, useState: zt, useEffect: Vt } = e, Ot = 320, Ut = ({ controlId: s }) => {
  const t = S(), a = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", n = t.readOnly === !0, l = t.children ?? [], o = $t(null), [c, d] = zt(
    r === "top" ? "top" : "side"
  );
  Vt(() => {
    if (r !== "auto") {
      d(r);
      return;
    }
    const v = o.current;
    if (!v) return;
    const C = new ResizeObserver((m) => {
      for (const f of m) {
        const E = f.contentRect.width / a;
        d(E < Ot ? "top" : "side");
      }
    });
    return C.observe(v), () => C.disconnect();
  }, [r, a]);
  const p = At(() => ({
    readOnly: n,
    resolvedLabelPosition: c
  }), [n, c]), i = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, u = [
    "tlFormLayout",
    n ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(be.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: s, className: u, style: i, ref: o }, l.map((v, C) => /* @__PURE__ */ e.createElement(T, { key: C, control: v }))));
}, { useCallback: Wt } = e, Kt = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Ht = ({ controlId: s }) => {
  const t = S(), a = I(), r = V(Kt), n = t.header, l = t.headerActions ?? [], o = t.collapsible === !0, c = t.collapsed === !0, d = t.border ?? "none", p = t.fullLine === !0, h = t.children ?? [], i = n != null || l.length > 0 || o, u = Wt(() => {
    a("toggleCollapse");
  }, [a]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${d}`,
    p ? "tlFormGroup--fullLine" : "",
    c ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: s, className: v }, i && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: u,
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
  ), n && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, n), l.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, l.map((C, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: C })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, h.map((C, m) => /* @__PURE__ */ e.createElement(T, { key: m, control: C }))));
}, { useContext: Gt, useState: Xt, useCallback: Yt } = e, qt = ({ controlId: s }) => {
  const t = S(), a = Gt(be), r = t.label ?? "", n = t.required === !0, l = t.error, o = t.helpText, c = t.dirty === !0, d = t.labelPosition ?? a.resolvedLabelPosition, p = t.fullLine === !0, h = t.visible !== !1, i = t.field, u = a.readOnly, [v, C] = Xt(!1), m = Yt(() => C((E) => !E), []);
  if (!h) return null;
  const f = l != null, b = [
    "tlFormField",
    `tlFormField--${d}`,
    u ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    c ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: s, className: b }, c && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__dirtyBar" }), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, r), n && !u && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && !u && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(T, { control: i })), !u && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, l)), !u && o && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, Zt = 20, Jt = () => {
  const s = S(), t = I(), a = s.nodes ?? [], r = s.selectionMode ?? "single";
  s.dragEnabled, s.dropEnabled;
  const [n, l] = e.useState(-1), o = e.useRef(null), c = e.useCallback((i, u) => {
    t(u ? "collapse" : "expand", { nodeId: i });
  }, [t]), d = e.useCallback((i, u) => {
    t("select", {
      nodeId: i,
      ctrlKey: u.ctrlKey || u.metaKey,
      shiftKey: u.shiftKey
    });
  }, [t]), p = e.useCallback((i, u) => {
    u.preventDefault(), t("contextMenu", { nodeId: i, x: u.clientX, y: u.clientY });
  }, [t]), h = e.useCallback((i) => {
    if (a.length === 0) return;
    let u = n;
    switch (i.key) {
      case "ArrowDown":
        i.preventDefault(), u = Math.min(n + 1, a.length - 1);
        break;
      case "ArrowUp":
        i.preventDefault(), u = Math.max(n - 1, 0);
        break;
      case "ArrowRight":
        if (i.preventDefault(), n >= 0 && n < a.length) {
          const v = a[n];
          if (v.expandable && !v.expanded) {
            t("expand", { nodeId: v.id });
            return;
          } else v.expanded && (u = n + 1);
        }
        break;
      case "ArrowLeft":
        if (i.preventDefault(), n >= 0 && n < a.length) {
          const v = a[n];
          if (v.expanded) {
            t("collapse", { nodeId: v.id });
            return;
          } else {
            const C = v.depth;
            for (let m = n - 1; m >= 0; m--)
              if (a[m].depth < C) {
                u = m;
                break;
              }
          }
        }
        break;
      case "Enter":
        i.preventDefault(), n >= 0 && n < a.length && t("select", {
          nodeId: a[n].id,
          ctrlKey: i.ctrlKey || i.metaKey,
          shiftKey: i.shiftKey
        });
        return;
      case " ":
        i.preventDefault(), r === "multi" && n >= 0 && n < a.length && t("select", {
          nodeId: a[n].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        i.preventDefault(), u = 0;
        break;
      case "End":
        i.preventDefault(), u = a.length - 1;
        break;
      default:
        return;
    }
    u !== n && l(u);
  }, [n, a, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: o,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: h
    },
    a.map((i, u) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: i.id,
        role: "treeitem",
        "aria-expanded": i.expandable ? i.expanded : void 0,
        "aria-selected": i.selected,
        "aria-level": i.depth + 1,
        className: [
          "tlTreeView__node",
          i.selected ? "tlTreeView__node--selected" : "",
          u === n ? "tlTreeView__node--focused" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: i.depth * Zt },
        onClick: (v) => d(i.id, v),
        onContextMenu: (v) => p(i.id, v)
      },
      i.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (v) => {
            v.stopPropagation(), c(i.id, i.expanded);
          },
          tabIndex: -1,
          "aria-label": i.expanded ? "Collapse" : "Expand"
        },
        i.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: i.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(T, { control: i.content }))
    ))
  );
};
N("TLButton", De);
N("TLToggleButton", Pe);
N("TLTextInput", ge);
N("TLNumberInput", ye);
N("TLDatePicker", we);
N("TLSelect", Le);
N("TLCheckbox", Se);
N("TLTable", Te);
N("TLCounter", je);
N("TLTabBar", Me);
N("TLFieldList", Ie);
N("TLAudioRecorder", $e);
N("TLAudioPlayer", Ve);
N("TLFileUpload", Ue);
N("TLDownload", Ke);
N("TLPhotoCapture", Ge);
N("TLPhotoViewer", Ye);
N("TLSplitPanel", qe);
N("TLPanel", nt);
N("TLMaximizeRoot", lt);
N("TLDeckPane", rt);
N("TLSidebar", pt);
N("TLStack", ft);
N("TLGrid", ht);
N("TLCard", bt);
N("TLAppBar", _t);
N("TLBreadcrumb", vt);
N("TLBottomBar", Ct);
N("TLDialog", wt);
N("TLDrawer", St);
N("TLSnackbar", Dt);
N("TLMenu", Pt);
N("TLAppShell", jt);
N("TLTextCell", Ft);
N("TLTableView", Mt);
N("TLFormLayout", Ut);
N("TLFormGroup", Ht);
N("TLFormField", qt);
N("TLTreeView", Jt);
