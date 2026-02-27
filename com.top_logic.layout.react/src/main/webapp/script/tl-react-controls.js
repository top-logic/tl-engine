import { React as e, useTLFieldValue as D, getComponent as O, useTLState as R, useTLCommand as U, TLChild as x, useTLUpload as S, useI18N as T, useTLDataUrl as B, register as C } from "tl-react-bridge";
const { useCallback: M } = e, V = ({ state: r }) => {
  const [o, t] = D(), l = M(
    (a) => {
      t(a.target.value);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: o ?? "",
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: $ } = e, K = ({ state: r, config: o }) => {
  const [t, l] = D(), a = $(
    (c) => {
      const s = c.target.value, i = s === "" ? null : Number(s);
      l(i);
    },
    [l]
  ), n = o != null && o.decimal ? "0.01" : "1";
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactNumberInput tlReactNumberInput--immutable" }, t != null ? String(t) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: t != null ? String(t) : "",
      onChange: a,
      step: n,
      disabled: r.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: W } = e, Y = ({ state: r }) => {
  const [o, t] = D(), l = W(
    (a) => {
      t(a.target.value || null);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement("span", { className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: o ?? "",
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: z } = e, H = ({ state: r, config: o }) => {
  var c;
  const [t, l] = D(), a = z(
    (s) => {
      l(s.target.value || null);
    },
    [l]
  ), n = r.options ?? (o == null ? void 0 : o.options) ?? [];
  if (r.editable === !1) {
    const s = ((c = n.find((i) => i.value === t)) == null ? void 0 : c.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { className: "tlReactSelect tlReactSelect--immutable" }, s);
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
    n.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  );
}, { useCallback: q } = e, G = ({ state: r }) => {
  const [o, t] = D(), l = q(
    (a) => {
      t(a.target.checked);
    },
    [t]
  );
  return r.editable === !1 ? /* @__PURE__ */ e.createElement(
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
      onChange: l,
      disabled: r.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, J = ({ controlId: r, state: o }) => {
  const t = o.columns ?? [], l = o.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, t.map((a) => /* @__PURE__ */ e.createElement("th", { key: a.name }, a.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((a, n) => /* @__PURE__ */ e.createElement("tr", { key: n }, t.map((c) => {
    const s = c.cellModule ? O(c.cellModule) : void 0, i = a[c.name];
    if (s) {
      const d = { value: i, editable: o.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        s,
        {
          controlId: r + "-" + n + "-" + c.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: Q } = e, X = ({ command: r, label: o, disabled: t }) => {
  const l = R(), a = U(), n = r ?? "click", c = o ?? l.label, s = t ?? l.disabled === !0, i = Q(() => {
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
}, { useCallback: Z } = e, ee = ({ command: r, label: o, active: t, disabled: l }) => {
  const a = R(), n = U(), c = r ?? "click", s = o ?? a.label, i = t ?? a.active === !0, d = l ?? a.disabled === !0, b = Z(() => {
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
}, te = () => {
  const r = R(), o = U(), t = r.count ?? 0, l = r.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, t), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: ae } = e, le = () => {
  const r = R(), o = U(), t = r.tabs ?? [], l = r.activeTabId, a = ae((n) => {
    n !== l && o("selectTab", { tabId: n });
  }, [o, l]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, t.map((n) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: n.id,
      role: "tab",
      "aria-selected": n.id === l,
      className: "tlReactTabBar__tab" + (n.id === l ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(n.id)
    },
    n.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, r.activeContent && /* @__PURE__ */ e.createElement(x, { control: r.activeContent })));
}, oe = () => {
  const r = R(), o = r.title, t = r.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, t.map((l, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(x, { control: l })))));
}, ne = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, re = () => {
  const r = R(), o = S(), [t, l] = e.useState("idle"), [a, n] = e.useState(null), c = e.useRef(null), s = e.useRef([]), i = e.useRef(null), d = r.status ?? "idle", b = r.error, m = d === "received" ? "idle" : t !== "idle" ? t : d, k = e.useCallback(async () => {
    if (t === "recording") {
      const h = c.current;
      h && h.state !== "inactive" && h.stop();
      return;
    }
    if (t !== "uploading") {
      if (n(null), !window.isSecureContext || !navigator.mediaDevices) {
        n("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const h = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        i.current = h, s.current = [];
        const N = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(h, N ? { mimeType: N } : void 0);
        c.current = L, L.ondataavailable = (u) => {
          u.data.size > 0 && s.current.push(u.data);
        }, L.onstop = async () => {
          h.getTracks().forEach((w) => w.stop()), i.current = null;
          const u = new Blob(s.current, { type: L.mimeType || "audio/webm" });
          if (s.current = [], u.size === 0) {
            l("idle");
            return;
          }
          l("uploading");
          const E = new FormData();
          E.append("audio", u, "recording.webm"), await o(E), l("idle");
        }, L.start(), l("recording");
      } catch (h) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", h), n("js.audioRecorder.error.denied"), l("idle");
      }
    }
  }, [t, o]), v = T(ne), g = m === "recording" ? v["js.audioRecorder.stop"] : m === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], _ = m === "uploading", y = ["tlAudioRecorder__button"];
  return m === "recording" && y.push("tlAudioRecorder__button--recording"), m === "uploading" && y.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: y.join(" "),
      onClick: k,
      disabled: _,
      title: g,
      "aria-label": g
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${m === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[a]), b && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b));
}, se = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, ce = () => {
  const r = R(), o = B(), t = !!r.hasAudio, l = r.dataRevision ?? 0, [a, n] = e.useState(t ? "idle" : "disabled"), c = e.useRef(null), s = e.useRef(null), i = e.useRef(l);
  e.useEffect(() => {
    t ? a === "disabled" && n("idle") : (c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), n("disabled"));
  }, [t]), e.useEffect(() => {
    l !== i.current && (i.current = l, c.current && (c.current.pause(), c.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (a === "playing" || a === "paused" || a === "loading") && n("idle"));
  }, [l]), e.useEffect(() => () => {
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
        const _ = await fetch(o);
        if (!_.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", _.status), n("idle");
          return;
        }
        const y = await _.blob();
        s.current = URL.createObjectURL(y);
      } catch (_) {
        console.error("[TLAudioPlayer] Fetch error:", _), n("idle");
        return;
      }
    }
    const g = new Audio(s.current);
    c.current = g, g.onended = () => {
      n("idle");
    }, g.play(), n("playing");
  }, [a, o]), b = T(se), m = a === "loading" ? b["js.loading"] : a === "playing" ? b["js.audioPlayer.pause"] : a === "disabled" ? b["js.audioPlayer.noAudio"] : b["js.audioPlayer.play"], k = a === "disabled" || a === "loading", v = ["tlAudioPlayer__button"];
  return a === "playing" && v.push("tlAudioPlayer__button--playing"), a === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: d,
      disabled: k,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, ie = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, ue = () => {
  const r = R(), o = S(), [t, l] = e.useState("idle"), [a, n] = e.useState(!1), c = e.useRef(null), s = r.status ?? "idle", i = r.error, d = r.accept ?? "", b = s === "received" ? "idle" : t !== "idle" ? t : s, m = e.useCallback(async (u) => {
    l("uploading");
    const E = new FormData();
    E.append("file", u, u.name), await o(E), l("idle");
  }, [o]), k = e.useCallback((u) => {
    var w;
    const E = (w = u.target.files) == null ? void 0 : w[0];
    E && m(E);
  }, [m]), v = e.useCallback(() => {
    var u;
    t !== "uploading" && ((u = c.current) == null || u.click());
  }, [t]), g = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), n(!0);
  }, []), _ = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), n(!1);
  }, []), y = e.useCallback((u) => {
    var w;
    if (u.preventDefault(), u.stopPropagation(), n(!1), t === "uploading") return;
    const E = (w = u.dataTransfer.files) == null ? void 0 : w[0];
    E && m(E);
  }, [t, m]), h = b === "uploading", N = T(ie), L = b === "uploading" ? N["js.uploading"] : N["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: g,
      onDragLeave: _,
      onDrop: y
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: c,
        type: "file",
        accept: d || void 0,
        onChange: k,
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
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, de = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, me = () => {
  const r = R(), o = B(), t = U(), l = !!r.hasData, a = r.dataRevision ?? 0, n = r.fileName ?? "download", c = !!r.clearable, [s, i] = e.useState(!1), d = e.useCallback(async () => {
    if (!(!l || s)) {
      i(!0);
      try {
        const v = o + (o.includes("?") ? "&" : "?") + "rev=" + a, g = await fetch(v);
        if (!g.ok) {
          console.error("[TLDownload] Failed to fetch data:", g.status);
          return;
        }
        const _ = await g.blob(), y = URL.createObjectURL(_), h = document.createElement("a");
        h.href = y, h.download = n, h.style.display = "none", document.body.appendChild(h), h.click(), document.body.removeChild(h), URL.revokeObjectURL(y);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        i(!1);
      }
    }
  }, [l, s, o, a, n]), b = e.useCallback(async () => {
    l && await t("clear");
  }, [l, t]), m = T(de);
  if (!l)
    return /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, m["js.download.noFile"]));
  const k = s ? m["js.downloading"] : m["js.download.file"].replace("{0}", n);
  return /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: d,
      disabled: s,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: n }, n), c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: b,
      title: m["js.download.clear"],
      "aria-label": m["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, pe = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, be = () => {
  const r = R(), o = S(), [t, l] = e.useState("idle"), [a, n] = e.useState(null), [c, s] = e.useState(!1), i = e.useRef(null), d = e.useRef(null), b = e.useRef(null), m = e.useRef(null), k = e.useRef(null), v = r.error, g = e.useMemo(
    () => {
      var p;
      return !!(window.isSecureContext && ((p = navigator.mediaDevices) != null && p.getUserMedia));
    },
    []
  ), _ = e.useCallback(() => {
    d.current && (d.current.getTracks().forEach((p) => p.stop()), d.current = null), i.current && (i.current.srcObject = null);
  }, []), y = e.useCallback(() => {
    _(), l("idle");
  }, [_]), h = e.useCallback(async () => {
    var p;
    if (t !== "uploading") {
      if (n(null), !g) {
        (p = m.current) == null || p.click();
        return;
      }
      try {
        const f = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        d.current = f, l("overlayOpen");
      } catch (f) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", f), n("js.photoCapture.error.denied"), l("idle");
      }
    }
  }, [t, g]), N = e.useCallback(async () => {
    if (t !== "overlayOpen")
      return;
    const p = i.current, f = b.current;
    if (!p || !f)
      return;
    f.width = p.videoWidth, f.height = p.videoHeight;
    const j = f.getContext("2d");
    j && (j.drawImage(p, 0, 0), _(), l("uploading"), f.toBlob(async (P) => {
      if (!P) {
        l("idle");
        return;
      }
      const I = new FormData();
      I.append("photo", P, "capture.jpg"), await o(I), l("idle");
    }, "image/jpeg", 0.85));
  }, [t, o, _]), L = e.useCallback(async (p) => {
    var P;
    const f = (P = p.target.files) == null ? void 0 : P[0];
    if (!f) return;
    l("uploading");
    const j = new FormData();
    j.append("photo", f, f.name), await o(j), l("idle"), m.current && (m.current.value = "");
  }, [o]);
  e.useEffect(() => {
    t === "overlayOpen" && i.current && d.current && (i.current.srcObject = d.current);
  }, [t]), e.useEffect(() => {
    var f;
    if (t !== "overlayOpen") return;
    (f = k.current) == null || f.focus();
    const p = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = p;
    };
  }, [t]), e.useEffect(() => {
    if (t !== "overlayOpen") return;
    const p = (f) => {
      f.key === "Escape" && y();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [t, y]), e.useEffect(() => () => {
    d.current && (d.current.getTracks().forEach((p) => p.stop()), d.current = null);
  }, []);
  const u = T(pe), E = t === "uploading" ? u["js.uploading"] : u["js.photoCapture.open"], w = ["tlPhotoCapture__cameraBtn"];
  t === "uploading" && w.push("tlPhotoCapture__cameraBtn--uploading");
  const F = ["tlPhotoCapture__overlayVideo"];
  c && F.push("tlPhotoCapture__overlayVideo--mirrored");
  const A = ["tlPhotoCapture__mirrorBtn"];
  return c && A.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: h,
      disabled: t === "uploading",
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !g && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: m,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: b, style: { display: "none" } }), t === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: y }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: F.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: A.join(" "),
        onClick: () => s((p) => !p),
        title: u["js.photoCapture.mirror"],
        "aria-label": u["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: N,
        title: u["js.photoCapture.capture"],
        "aria-label": u["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: y,
        title: u["js.photoCapture.close"],
        "aria-label": u["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, u[a]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, he = {
  "js.photoViewer.alt": "Captured photo"
}, fe = () => {
  const r = R(), o = B(), t = !!r.hasPhoto, l = r.dataRevision ?? 0, [a, n] = e.useState(null), c = e.useRef(l);
  e.useEffect(() => {
    if (!t) {
      a && (URL.revokeObjectURL(a), n(null));
      return;
    }
    if (l === c.current && a)
      return;
    c.current = l, a && (URL.revokeObjectURL(a), n(null));
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
  }, [t, l, o]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const s = T(he);
  return !t || !a ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: s["js.photoViewer.alt"]
    }
  ));
};
C("TLButton", X);
C("TLToggleButton", ee);
C("TLTextInput", V);
C("TLNumberInput", K);
C("TLDatePicker", Y);
C("TLSelect", H);
C("TLCheckbox", G);
C("TLTable", J);
C("TLCounter", te);
C("TLTabBar", le);
C("TLFieldList", oe);
C("TLAudioRecorder", re);
C("TLAudioPlayer", ce);
C("TLFileUpload", ue);
C("TLDownload", me);
C("TLPhotoCapture", be);
C("TLPhotoViewer", fe);
