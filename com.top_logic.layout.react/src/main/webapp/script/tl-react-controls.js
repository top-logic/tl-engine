import { React as e, useTLFieldValue as R, getComponent as D, useTLState as L, useTLCommand as T, TLChild as N, useTLUpload as w, useTLDataUrl as y, register as b } from "tl-react-bridge";
const { useCallback: P } = e, U = ({ state: o }) => {
  const [r, a] = R(), n = P(
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
}, { useCallback: S } = e, B = ({ state: o, config: r }) => {
  const [a, n] = R(), t = S(
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
}, { useCallback: A } = e, F = ({ state: o }) => {
  const [r, a] = R(), n = A(
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
}, { useCallback: j } = e, O = ({ state: o, config: r }) => {
  const [a, n] = R(), t = j(
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
}, { useCallback: I } = e, V = ({ state: o }) => {
  const [r, a] = R(), n = I(
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
}, M = ({ controlId: o, state: r }) => {
  const a = r.columns ?? [], n = r.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((t, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((s) => {
    const c = s.cellModule ? D(s.cellModule) : void 0, i = t[s.name];
    if (c) {
      const p = { value: i, editable: r.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: o + "-" + l + "-" + s.name,
          state: p
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: x } = e, $ = ({ command: o, label: r, disabled: a }) => {
  const n = L(), t = T(), l = o ?? "click", s = r ?? n.label, c = a ?? n.disabled === !0, i = x(() => {
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
}, { useCallback: W } = e, z = ({ command: o, label: r, active: a, disabled: n }) => {
  const t = L(), l = T(), s = o ?? "click", c = r ?? t.label, i = a ?? t.active === !0, p = n ?? t.disabled === !0, v = W(() => {
    l(s);
  }, [l, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: p,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, H = () => {
  const o = L(), r = T(), a = o.count ?? 0, n = o.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: q } = e, G = () => {
  const o = L(), r = T(), a = o.tabs ?? [], n = o.activeTabId, t = q((l) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, o.activeContent && /* @__PURE__ */ e.createElement(N, { control: o.activeContent })));
}, J = () => {
  const o = L(), r = o.title, a = o.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((n, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(N, { control: n })))));
}, K = () => {
  const o = L(), r = w(), [a, n] = e.useState("idle"), t = e.useRef(null), l = e.useRef([]), s = e.useRef(null), c = o.status ?? "idle", i = o.error, p = c === "received" ? "idle" : a !== "idle" ? a : c, v = e.useCallback(async () => {
    if (a === "recording") {
      const d = t.current;
      d && d.state !== "inactive" && d.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const d = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        s.current = d, l.current = [];
        const u = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", g = new MediaRecorder(d, u ? { mimeType: u } : void 0);
        t.current = g, g.ondataavailable = (k) => {
          k.data.size > 0 && l.current.push(k.data);
        }, g.onstop = async () => {
          d.getTracks().forEach((_) => _.stop()), s.current = null;
          const k = new Blob(l.current, { type: g.mimeType || "audio/webm" });
          if (l.current = [], k.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const m = new FormData();
          m.append("audio", k, "recording.webm"), await r(m), n("idle");
        }, g.start(), n("recording");
      } catch (d) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", d), n("idle");
      }
  }, [a, r]), f = p === "recording" ? "Stop recording" : p === "uploading" ? "Uploading…" : "Record audio", C = p === "uploading", h = ["tlAudioRecorder__button"];
  return p === "recording" && h.push("tlAudioRecorder__button--recording"), p === "uploading" && h.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: v,
      disabled: C,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${p === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, Q = () => {
  const o = L(), r = y(), a = !!o.hasAudio, n = o.dataRevision ?? 0, [t, l] = e.useState(a ? "idle" : "disabled"), s = e.useRef(null), c = e.useRef(null), i = e.useRef(n);
  e.useEffect(() => {
    a ? t === "disabled" && l("idle") : (s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), l("disabled"));
  }, [a]), e.useEffect(() => {
    n !== i.current && (i.current = n, s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (t === "playing" || t === "paused" || t === "loading") && l("idle"));
  }, [n]), e.useEffect(() => () => {
    s.current && (s.current.pause(), s.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const p = e.useCallback(async () => {
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
        const d = await fetch(r);
        if (!d.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", d.status), l("idle");
          return;
        }
        const u = await d.blob();
        c.current = URL.createObjectURL(u);
      } catch (d) {
        console.error("[TLAudioPlayer] Fetch error:", d), l("idle");
        return;
      }
    }
    const h = new Audio(c.current);
    s.current = h, h.onended = () => {
      l("idle");
    }, h.play(), l("playing");
  }, [t, r]), v = t === "loading" ? "Loading…" : t === "playing" ? "Pause audio" : t === "disabled" ? "No audio" : "Play audio", f = t === "disabled" || t === "loading", C = ["tlAudioPlayer__button"];
  return t === "playing" && C.push("tlAudioPlayer__button--playing"), t === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: p,
      disabled: f,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, X = () => {
  const o = L(), r = w(), [a, n] = e.useState("idle"), [t, l] = e.useState(!1), s = e.useRef(null), c = o.status ?? "idle", i = o.error, p = o.accept ?? "", v = c === "received" ? "idle" : a !== "idle" ? a : c, f = e.useCallback(async (m) => {
    n("uploading");
    const _ = new FormData();
    _.append("file", m, m.name), await r(_), n("idle");
  }, [r]), C = e.useCallback((m) => {
    var E;
    const _ = (E = m.target.files) == null ? void 0 : E[0];
    _ && f(_);
  }, [f]), h = e.useCallback(() => {
    var m;
    a !== "uploading" && ((m = s.current) == null || m.click());
  }, [a]), d = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), l(!0);
  }, []), u = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation(), l(!1);
  }, []), g = e.useCallback((m) => {
    var E;
    if (m.preventDefault(), m.stopPropagation(), l(!1), a === "uploading") return;
    const _ = (E = m.dataTransfer.files) == null ? void 0 : E[0];
    _ && f(_);
  }, [a, f]), k = v === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: d,
      onDragLeave: u,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: s,
        type: "file",
        accept: p || void 0,
        onChange: C,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: h,
        disabled: k
      },
      v === "uploading" ? "Uploading…" : "Choose File"
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Y = () => {
  const o = L(), r = y(), a = T(), n = !!o.hasData, t = o.dataRevision ?? 0, l = o.fileName ?? "download", s = !!o.clearable, [c, i] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!n || c)) {
      i(!0);
      try {
        const f = r + (r.includes("?") ? "&" : "?") + "rev=" + t, C = await fetch(f);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const h = await C.blob(), d = URL.createObjectURL(h), u = document.createElement("a");
        u.href = d, u.download = l, u.style.display = "none", document.body.appendChild(u), u.click(), document.body.removeChild(u), URL.revokeObjectURL(d);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        i(!1);
      }
    }
  }, [n, c, r, t, l]), v = e.useCallback(async () => {
    n && await a("clear");
  }, [n, a]);
  return n ? /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: c,
      title: c ? "Downloading…" : "Download " + l,
      "aria-label": c ? "Downloading…" : "Download " + l
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: l }, l), s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: "Clear",
      "aria-label": "Clear file"
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  )) : /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, "No file"));
}, Z = () => {
  const o = L(), r = w(), [a, n] = e.useState("idle"), t = e.useRef(null), l = e.useRef(null), s = e.useRef(null), c = o.error, i = e.useCallback(() => {
    l.current && (l.current.getTracks().forEach((u) => u.stop()), l.current = null), t.current && (t.current.srcObject = null);
  }, []), p = e.useCallback(async () => {
    if (a !== "uploading") {
      if (a === "previewing") {
        i(), n("idle");
        return;
      }
      try {
        const u = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        l.current = u, n("previewing");
      } catch (u) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", u), n("idle");
      }
    }
  }, [a, i]), v = e.useCallback(async () => {
    if (a !== "previewing")
      return;
    const u = t.current, g = s.current;
    if (!u || !g)
      return;
    g.width = u.videoWidth, g.height = u.videoHeight;
    const k = g.getContext("2d");
    k && (k.drawImage(u, 0, 0), i(), n("uploading"), g.toBlob(async (m) => {
      if (!m) {
        n("idle");
        return;
      }
      const _ = new FormData();
      _.append("photo", m, "capture.jpg"), await r(_), n("idle");
    }, "image/jpeg", 0.85));
  }, [a, r, i]);
  e.useEffect(() => {
    a === "previewing" && t.current && l.current && (t.current.srcObject = l.current);
  }, [a]), e.useEffect(() => () => {
    l.current && (l.current.getTracks().forEach((u) => u.stop()), l.current = null);
  }, []);
  const f = a === "previewing" ? "Close camera" : a === "uploading" ? "Uploading…" : "Open camera", C = a === "uploading", h = ["tlPhotoCapture__cameraBtn"];
  a === "previewing" && h.push("tlPhotoCapture__cameraBtn--active");
  const d = ["tlPhotoCapture__captureBtn"];
  return a === "uploading" && d.push("tlPhotoCapture__captureBtn--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: p,
      disabled: C,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  ), a === "previewing" && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: d.join(" "),
      onClick: v,
      title: "Capture photo",
      "aria-label": "Capture photo"
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
}, ee = () => {
  const o = L(), r = y(), a = !!o.hasPhoto, n = o.dataRevision ?? 0, [t, l] = e.useState(null), s = e.useRef(n);
  return e.useEffect(() => {
    if (!a) {
      t && (URL.revokeObjectURL(t), l(null));
      return;
    }
    if (n === s.current && t)
      return;
    s.current = n, t && (URL.revokeObjectURL(t), l(null));
    let c = !1;
    return (async () => {
      try {
        const i = await fetch(r);
        if (!i.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", i.status);
          return;
        }
        const p = await i.blob();
        c || l(URL.createObjectURL(p));
      } catch (i) {
        console.error("[TLPhotoViewer] Fetch error:", i);
      }
    })(), () => {
      c = !0;
    };
  }, [a, n, r]), e.useEffect(() => () => {
    t && URL.revokeObjectURL(t);
  }, []), !a || !t ? /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: t,
      alt: "Captured photo"
    }
  ));
};
b("TLButton", $);
b("TLToggleButton", z);
b("TLTextInput", U);
b("TLNumberInput", B);
b("TLDatePicker", F);
b("TLSelect", O);
b("TLCheckbox", V);
b("TLTable", M);
b("TLCounter", H);
b("TLTabBar", G);
b("TLFieldList", J);
b("TLAudioRecorder", K);
b("TLAudioPlayer", Q);
b("TLFileUpload", X);
b("TLDownload", Y);
b("TLPhotoCapture", Z);
b("TLPhotoViewer", ee);
