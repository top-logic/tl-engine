import { React as e, useTLFieldValue as q, getComponent as Pe, useTLState as R, useTLCommand as A, TLChild as P, useTLUpload as ie, useI18N as W, useTLDataUrl as de, register as T } from "tl-react-bridge";
const { useCallback: Be } = e, je = ({ controlId: l, state: t }) => {
  const [o, n] = q(), s = Be(
    (r) => {
      n(r.target.value);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      id: l,
      value: o ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: Me } = e, Fe = ({ controlId: l, state: t, config: o }) => {
  const [n, s] = q(), r = Me(
    (d) => {
      const c = d.target.value, i = c === "" ? null : Number(c);
      s(i);
    },
    [s]
  ), a = o != null && o.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      id: l,
      value: n != null ? String(n) : "",
      onChange: r,
      step: a,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: Ie } = e, ze = ({ controlId: l, state: t }) => {
  const [o, n] = q(), s = Ie(
    (r) => {
      n(r.target.value || null);
    },
    [n]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, o ?? "") : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      id: l,
      value: o ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: Ae } = e, $e = ({ controlId: l, state: t, config: o }) => {
  var d;
  const [n, s] = q(), r = Ae(
    (c) => {
      s(c.target.value || null);
    },
    [s]
  ), a = t.options ?? (o == null ? void 0 : o.options) ?? [];
  if (t.editable === !1) {
    const c = ((d = a.find((i) => i.value === n)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, c);
  }
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      id: l,
      value: n ?? "",
      onChange: r,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((c) => /* @__PURE__ */ e.createElement("option", { key: c.value, value: c.value }, c.label))
  );
}, { useCallback: Ve } = e, Oe = ({ controlId: l, state: t }) => {
  const [o, n] = q(), s = Ve(
    (r) => {
      n(r.target.checked);
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
      onChange: s,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, Ue = ({ controlId: l, state: t }) => {
  const o = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, o.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, r) => /* @__PURE__ */ e.createElement("tr", { key: r }, o.map((a) => {
    const d = a.cellModule ? Pe(a.cellModule) : void 0, c = s[a.name];
    if (d) {
      const i = { value: c, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: a.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: l + "-" + r + "-" + a.name,
          state: i
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: a.name }, c != null ? String(c) : "");
  })))));
}, { useCallback: We } = e, Ke = ({ controlId: l, command: t, label: o, disabled: n }) => {
  const s = R(), r = A(), a = t ?? "click", d = o ?? s.label, c = n ?? s.disabled === !0, i = We(() => {
    r(a);
  }, [r, a]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: i,
      disabled: c,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: He } = e, Ge = ({ controlId: l, command: t, label: o, active: n, disabled: s }) => {
  const r = R(), a = A(), d = t ?? "click", c = o ?? r.label, i = n ?? r.active === !0, h = s ?? r.disabled === !0, v = He(() => {
    a(d);
  }, [a, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: v,
      disabled: h,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, Ye = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.count ?? 0, s = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => o("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Xe } = e, qe = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.tabs ?? [], s = t.activeTabId, r = Xe((a) => {
    a !== s && o("selectTab", { tabId: a });
  }, [o, s]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === s,
      className: "tlReactTabBar__tab" + (a.id === s ? " tlReactTabBar__tab--active" : ""),
      onClick: () => r(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, Ze = ({ controlId: l }) => {
  const t = R(), o = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, o && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, o), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((s, r) => /* @__PURE__ */ e.createElement("div", { key: r, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(P, { control: s })))));
}, Je = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Qe = ({ controlId: l }) => {
  const t = R(), o = ie(), [n, s] = e.useState("idle"), [r, a] = e.useState(null), d = e.useRef(null), c = e.useRef([]), i = e.useRef(null), h = t.status ?? "idle", v = t.error, _ = h === "received" ? "idle" : n !== "idle" ? n : h, N = e.useCallback(async () => {
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
        i.current = k, c.current = [];
        const j = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", B = new MediaRecorder(k, j ? { mimeType: j } : void 0);
        d.current = B, B.ondataavailable = (u) => {
          u.data.size > 0 && c.current.push(u.data);
        }, B.onstop = async () => {
          k.getTracks().forEach((w) => w.stop()), i.current = null;
          const u = new Blob(c.current, { type: B.mimeType || "audio/webm" });
          if (c.current = [], u.size === 0) {
            s("idle");
            return;
          }
          s("uploading");
          const f = new FormData();
          f.append("audio", u, "recording.webm"), await o(f), s("idle");
        }, B.start(), s("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), a("js.audioRecorder.error.denied"), s("idle");
      }
    }
  }, [n, o]), C = W(Je), p = _ === "recording" ? C["js.audioRecorder.stop"] : _ === "uploading" ? C["js.uploading"] : C["js.audioRecorder.record"], b = _ === "uploading", L = ["tlAudioRecorder__button"];
  return _ === "recording" && L.push("tlAudioRecorder__button--recording"), _ === "uploading" && L.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: L.join(" "),
      onClick: N,
      disabled: b,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, C[r]), v && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v));
}, et = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, tt = ({ controlId: l }) => {
  const t = R(), o = de(), n = !!t.hasAudio, s = t.dataRevision ?? 0, [r, a] = e.useState(n ? "idle" : "disabled"), d = e.useRef(null), c = e.useRef(null), i = e.useRef(s);
  e.useEffect(() => {
    n ? r === "disabled" && a("idle") : (d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), a("disabled"));
  }, [n]), e.useEffect(() => {
    s !== i.current && (i.current = s, d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (r === "playing" || r === "paused" || r === "loading") && a("idle"));
  }, [s]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const h = e.useCallback(async () => {
    if (r === "disabled" || r === "loading")
      return;
    if (r === "playing") {
      d.current && d.current.pause(), a("paused");
      return;
    }
    if (r === "paused" && d.current) {
      d.current.play(), a("playing");
      return;
    }
    if (!c.current) {
      a("loading");
      try {
        const b = await fetch(o);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), a("idle");
          return;
        }
        const L = await b.blob();
        c.current = URL.createObjectURL(L);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), a("idle");
        return;
      }
    }
    const p = new Audio(c.current);
    d.current = p, p.onended = () => {
      a("idle");
    }, p.play(), a("playing");
  }, [r, o]), v = W(et), _ = r === "loading" ? v["js.loading"] : r === "playing" ? v["js.audioPlayer.pause"] : r === "disabled" ? v["js.audioPlayer.noAudio"] : v["js.audioPlayer.play"], N = r === "disabled" || r === "loading", C = ["tlAudioPlayer__button"];
  return r === "playing" && C.push("tlAudioPlayer__button--playing"), r === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: h,
      disabled: N,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${r === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, nt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, at = ({ controlId: l }) => {
  const t = R(), o = ie(), [n, s] = e.useState("idle"), [r, a] = e.useState(!1), d = e.useRef(null), c = t.status ?? "idle", i = t.error, h = t.accept ?? "", v = c === "received" ? "idle" : n !== "idle" ? n : c, _ = e.useCallback(async (u) => {
    s("uploading");
    const f = new FormData();
    f.append("file", u, u.name), await o(f), s("idle");
  }, [o]), N = e.useCallback((u) => {
    var w;
    const f = (w = u.target.files) == null ? void 0 : w[0];
    f && _(f);
  }, [_]), C = e.useCallback(() => {
    var u;
    n !== "uploading" && ((u = d.current) == null || u.click());
  }, [n]), p = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!0);
  }, []), b = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!1);
  }, []), L = e.useCallback((u) => {
    var w;
    if (u.preventDefault(), u.stopPropagation(), a(!1), n === "uploading") return;
    const f = (w = u.dataTransfer.files) == null ? void 0 : w[0];
    f && _(f);
  }, [n, _]), k = v === "uploading", j = W(nt), B = v === "uploading" ? j["js.uploading"] : j["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${r ? " tlFileUpload--dragover" : ""}`,
      onDragOver: p,
      onDragLeave: b,
      onDrop: L
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: h || void 0,
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
        disabled: k,
        title: B,
        "aria-label": B
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, lt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ot = ({ controlId: l }) => {
  const t = R(), o = de(), n = A(), s = !!t.hasData, r = t.dataRevision ?? 0, a = t.fileName ?? "download", d = !!t.clearable, [c, i] = e.useState(!1), h = e.useCallback(async () => {
    if (!(!s || c)) {
      i(!0);
      try {
        const C = o + (o.includes("?") ? "&" : "?") + "rev=" + r, p = await fetch(C);
        if (!p.ok) {
          console.error("[TLDownload] Failed to fetch data:", p.status);
          return;
        }
        const b = await p.blob(), L = URL.createObjectURL(b), k = document.createElement("a");
        k.href = L, k.download = a, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(L);
      } catch (C) {
        console.error("[TLDownload] Fetch error:", C);
      } finally {
        i(!1);
      }
    }
  }, [s, c, o, r, a]), v = e.useCallback(async () => {
    s && await n("clear");
  }, [s, n]), _ = W(lt);
  if (!s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const N = c ? _["js.downloading"] : _["js.download.file"].replace("{0}", a);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: h,
      disabled: c,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: _["js.download.clear"],
      "aria-label": _["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, rt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, st = ({ controlId: l }) => {
  const t = R(), o = ie(), [n, s] = e.useState("idle"), [r, a] = e.useState(null), [d, c] = e.useState(!1), i = e.useRef(null), h = e.useRef(null), v = e.useRef(null), _ = e.useRef(null), N = e.useRef(null), C = t.error, p = e.useMemo(
    () => {
      var y;
      return !!(window.isSecureContext && ((y = navigator.mediaDevices) != null && y.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    h.current && (h.current.getTracks().forEach((y) => y.stop()), h.current = null), i.current && (i.current.srcObject = null);
  }, []), L = e.useCallback(() => {
    b(), s("idle");
  }, [b]), k = e.useCallback(async () => {
    var y;
    if (n !== "uploading") {
      if (a(null), !p) {
        (y = _.current) == null || y.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        h.current = D, s("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), a("js.photoCapture.error.denied"), s("idle");
      }
    }
  }, [n, p]), j = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const y = i.current, D = v.current;
    if (!y || !D)
      return;
    D.width = y.videoWidth, D.height = y.videoHeight;
    const x = D.getContext("2d");
    x && (x.drawImage(y, 0, 0), b(), s("uploading"), D.toBlob(async (I) => {
      if (!I) {
        s("idle");
        return;
      }
      const K = new FormData();
      K.append("photo", I, "capture.jpg"), await o(K), s("idle");
    }, "image/jpeg", 0.85));
  }, [n, o, b]), B = e.useCallback(async (y) => {
    var I;
    const D = (I = y.target.files) == null ? void 0 : I[0];
    if (!D) return;
    s("uploading");
    const x = new FormData();
    x.append("photo", D, D.name), await o(x), s("idle"), _.current && (_.current.value = "");
  }, [o]);
  e.useEffect(() => {
    n === "overlayOpen" && i.current && h.current && (i.current.srcObject = h.current);
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
      D.key === "Escape" && L();
    };
    return document.addEventListener("keydown", y), () => document.removeEventListener("keydown", y);
  }, [n, L]), e.useEffect(() => () => {
    h.current && (h.current.getTracks().forEach((y) => y.stop()), h.current = null);
  }, []);
  const u = W(rt), f = n === "uploading" ? u["js.uploading"] : u["js.photoCapture.open"], w = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && w.push("tlPhotoCapture__cameraBtn--uploading");
  const M = ["tlPhotoCapture__overlayVideo"];
  d && M.push("tlPhotoCapture__overlayVideo--mirrored");
  const E = ["tlPhotoCapture__mirrorBtn"];
  return d && E.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: w.join(" "),
      onClick: k,
      disabled: n === "uploading",
      title: f,
      "aria-label": f
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: L }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: i,
        className: M.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: E.join(" "),
        onClick: () => c((y) => !y),
        title: u["js.photoCapture.mirror"],
        "aria-label": u["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: j,
        title: u["js.photoCapture.capture"],
        "aria-label": u["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: L,
        title: u["js.photoCapture.close"],
        "aria-label": u["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), r && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, u[r]), C && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, C));
}, ct = {
  "js.photoViewer.alt": "Captured photo"
}, it = ({ controlId: l }) => {
  const t = R(), o = de(), n = !!t.hasPhoto, s = t.dataRevision ?? 0, [r, a] = e.useState(null), d = e.useRef(s);
  e.useEffect(() => {
    if (!n) {
      r && (URL.revokeObjectURL(r), a(null));
      return;
    }
    if (s === d.current && r)
      return;
    d.current = s, r && (URL.revokeObjectURL(r), a(null));
    let i = !1;
    return (async () => {
      try {
        const h = await fetch(o);
        if (!h.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", h.status);
          return;
        }
        const v = await h.blob();
        i || a(URL.createObjectURL(v));
      } catch (h) {
        console.error("[TLPhotoViewer] Fetch error:", h);
      }
    })(), () => {
      i = !0;
    };
  }, [n, s, o]), e.useEffect(() => () => {
    r && URL.revokeObjectURL(r);
  }, []);
  const c = W(ct);
  return !n || !r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: r,
      alt: c["js.photoViewer.alt"]
    }
  ));
}, { useCallback: me, useRef: le } = e, dt = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.orientation, s = t.resizable === !0, r = t.children ?? [], a = n === "horizontal", d = r.length > 0 && r.every((b) => b.collapsed), c = !d && r.some((b) => b.collapsed), i = d ? !a : a, h = le(null), v = le(null), _ = le(null), N = me((b, L) => {
    const k = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? d && !i ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : L !== void 0 ? k.flex = `0 0 ${L}px` : b.unit === "%" || c ? k.flex = `${b.size} 0 0%` : k.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (k.minWidth = a ? b.minSize : void 0, k.minHeight = a ? void 0 : b.minSize), k;
  }, [a, d, c, i]), C = me((b, L) => {
    b.preventDefault();
    const k = h.current;
    if (!k) return;
    const j = r[L], B = r[L + 1], u = k.querySelectorAll(":scope > .tlSplitPanel__child"), f = [];
    u.forEach((E) => {
      f.push(a ? E.offsetWidth : E.offsetHeight);
    }), _.current = f, v.current = {
      splitterIndex: L,
      startPos: a ? b.clientX : b.clientY,
      startSizeBefore: f[L],
      startSizeAfter: f[L + 1],
      childBefore: j,
      childAfter: B
    };
    const w = (E) => {
      const y = v.current;
      if (!y || !_.current) return;
      const x = (a ? E.clientX : E.clientY) - y.startPos, I = y.childBefore.minSize || 0, K = y.childAfter.minSize || 0;
      let H = y.startSizeBefore + x, G = y.startSizeAfter - x;
      H < I && (G += H - I, H = I), G < K && (H += G - K, G = K), _.current[y.splitterIndex] = H, _.current[y.splitterIndex + 1] = G;
      const Z = k.querySelectorAll(":scope > .tlSplitPanel__child"), J = Z[y.splitterIndex], Y = Z[y.splitterIndex + 1];
      J && (J.style.flex = `0 0 ${H}px`), Y && (Y.style.flex = `0 0 ${G}px`);
    }, M = () => {
      if (document.removeEventListener("mousemove", w), document.removeEventListener("mouseup", M), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const E = {};
        r.forEach((y, D) => {
          const x = y.control;
          x != null && x.controlId && _.current && (E[x.controlId] = _.current[D]);
        }), o("updateSizes", { sizes: E });
      }
      _.current = null, v.current = null;
    };
    document.addEventListener("mousemove", w), document.addEventListener("mouseup", M), document.body.style.cursor = a ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [r, a, o]), p = [];
  return r.forEach((b, L) => {
    if (p.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${L}`,
          className: `tlSplitPanel__child${b.collapsed && i ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(b)
        },
        /* @__PURE__ */ e.createElement(P, { control: b.control })
      )
    ), s && L < r.length - 1) {
      const k = r[L + 1];
      !b.collapsed && !k.collapsed && p.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${L}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (B) => C(B, L)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: h,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: i ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    p
  );
}, { useCallback: oe } = e, ut = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, mt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), pt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ft = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), bt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ht = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), _t = ({ controlId: l }) => {
  const t = R(), o = A(), n = W(ut), s = t.title, r = t.expansionState ?? "NORMALIZED", a = t.showMinimize === !0, d = t.showMaximize === !0, c = t.showPopOut === !0, i = t.toolbarButtons ?? [], h = r === "MINIMIZED", v = r === "MAXIMIZED", _ = r === "HIDDEN", N = oe(() => {
    o("toggleMinimize");
  }, [o]), C = oe(() => {
    o("toggleMaximize");
  }, [o]), p = oe(() => {
    o("popOut");
  }, [o]);
  if (_)
    return null;
  const b = v ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${r.toLowerCase()}`,
      style: b
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, i.map((L, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(P, { control: L }))), a && !v && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: h ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      h ? /* @__PURE__ */ e.createElement(pt, null) : /* @__PURE__ */ e.createElement(mt, null)
    ), d && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: C,
        title: v ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      v ? /* @__PURE__ */ e.createElement(bt, null) : /* @__PURE__ */ e.createElement(ft, null)
    ), c && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: p,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ht, null)
    ))),
    !h && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(P, { control: t.child }))
  );
}, Et = ({ controlId: l }) => {
  const t = R();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(P, { control: t.child })
  );
}, vt = ({ controlId: l }) => {
  const t = R();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(P, { control: t.activeChild }));
}, { useCallback: U, useState: Q, useEffect: ee, useRef: te } = e, gt = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function ce(l, t, o, n) {
  const s = [];
  for (const r of l)
    r.type === "nav" ? s.push({ id: r.id, type: "nav", groupId: n }) : r.type === "command" ? s.push({ id: r.id, type: "command", groupId: n }) : r.type === "group" && (s.push({ id: r.id, type: "group" }), (o.get(r.id) ?? r.expanded) && !t && s.push(...ce(r.children, t, o, r.id)));
  return s;
}
const X = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Ct = ({ item: l, active: t, collapsed: o, onSelect: n, tabIndex: s, itemRef: r, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: o ? l.label : void 0,
    tabIndex: s,
    ref: r,
    onFocus: () => a(l.id)
  },
  o && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(X, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
  !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !o && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), yt = ({ item: l, collapsed: t, onExecute: o, tabIndex: n, itemRef: s, onFocus: r }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => o(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: s,
    onFocus: () => r(l.id)
  },
  /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), kt = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(X, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), wt = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Nt = ({ item: l, activeItemId: t, anchorRect: o, onSelect: n, onExecute: s, onClose: r }) => {
  const a = te(null);
  ee(() => {
    const i = (h) => {
      a.current && !a.current.contains(h.target) && setTimeout(() => r(), 0);
    };
    return document.addEventListener("mousedown", i), () => document.removeEventListener("mousedown", i);
  }, [r]), ee(() => {
    const i = (h) => {
      h.key === "Escape" && r();
    };
    return document.addEventListener("keydown", i), () => document.removeEventListener("keydown", i);
  }, [r]);
  const d = U((i) => {
    i.type === "nav" ? (n(i.id), r()) : i.type === "command" && (s(i.id), r());
  }, [n, s, r]), c = {};
  return o && (c.left = o.right, c.top = o.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: a, role: "menu", style: c }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((i) => {
    if (i.type === "nav" || i.type === "command") {
      const h = i.type === "nav" && i.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: i.id,
          className: "tlSidebar__flyoutItem" + (h ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(i)
        },
        /* @__PURE__ */ e.createElement(X, { icon: i.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, i.label),
        i.type === "nav" && i.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, i.badge)
      );
    }
    return i.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: i.id, className: "tlSidebar__flyoutSectionHeader" }, i.label) : i.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: i.id, className: "tlSidebar__separator" }) : null;
  }));
}, Lt = ({
  item: l,
  expanded: t,
  activeItemId: o,
  collapsed: n,
  onSelect: s,
  onExecute: r,
  onToggleGroup: a,
  tabIndex: d,
  itemRef: c,
  onFocus: i,
  focusedId: h,
  setItemRef: v,
  onItemFocus: _,
  flyoutGroupId: N,
  onOpenFlyout: C,
  onCloseFlyout: p
}) => {
  const b = te(null), [L, k] = Q(null), j = U(() => {
    n ? N === l.id ? p() : (b.current && k(b.current.getBoundingClientRect()), C(l.id)) : a(l.id);
  }, [n, N, l.id, a, C, p]), B = U((f) => {
    b.current = f, c(f);
  }, [c]), u = n && N === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (u ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: j,
      title: n ? l.label : void 0,
      "aria-expanded": n ? u : t,
      tabIndex: d,
      ref: B,
      onFocus: () => i(l.id)
    },
    /* @__PURE__ */ e.createElement(X, { icon: l.icon }),
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
    Nt,
    {
      item: l,
      activeItemId: o,
      anchorRect: L,
      onSelect: s,
      onExecute: r,
      onClose: p
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((f) => /* @__PURE__ */ e.createElement(
    Ee,
    {
      key: f.id,
      item: f,
      activeItemId: o,
      collapsed: n,
      onSelect: s,
      onExecute: r,
      onToggleGroup: a,
      focusedId: h,
      setItemRef: v,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: C,
      onCloseFlyout: p
    }
  ))));
}, Ee = ({
  item: l,
  activeItemId: t,
  collapsed: o,
  onSelect: n,
  onExecute: s,
  onToggleGroup: r,
  focusedId: a,
  setItemRef: d,
  onItemFocus: c,
  groupStates: i,
  flyoutGroupId: h,
  onOpenFlyout: v,
  onCloseFlyout: _
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        Ct,
        {
          item: l,
          active: l.id === t,
          collapsed: o,
          onSelect: n,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: c
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        yt,
        {
          item: l,
          collapsed: o,
          onExecute: s,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: c
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(kt, { item: l, collapsed: o });
    case "separator":
      return /* @__PURE__ */ e.createElement(wt, null);
    case "group": {
      const N = i ? i.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Lt,
        {
          item: l,
          expanded: N,
          activeItemId: t,
          collapsed: o,
          onSelect: n,
          onExecute: s,
          onToggleGroup: r,
          tabIndex: a === l.id ? 0 : -1,
          itemRef: d(l.id),
          onFocus: c,
          focusedId: a,
          setItemRef: d,
          onItemFocus: c,
          flyoutGroupId: h,
          onOpenFlyout: v,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, Tt = ({ controlId: l }) => {
  const t = R(), o = A(), n = W(gt), s = t.items ?? [], r = t.activeItemId, a = t.collapsed, [d, c] = Q(() => {
    const E = /* @__PURE__ */ new Map(), y = (D) => {
      for (const x of D)
        x.type === "group" && (E.set(x.id, x.expanded), y(x.children));
    };
    return y(s), E;
  }), i = U((E) => {
    c((y) => {
      const D = new Map(y), x = D.get(E) ?? !1;
      return D.set(E, !x), o("toggleGroup", { itemId: E, expanded: !x }), D;
    });
  }, [o]), h = U((E) => {
    E !== r && o("selectItem", { itemId: E });
  }, [o, r]), v = U((E) => {
    o("executeCommand", { itemId: E });
  }, [o]), _ = U(() => {
    o("toggleCollapse", {});
  }, [o]), [N, C] = Q(null), p = U((E) => {
    C(E);
  }, []), b = U(() => {
    C(null);
  }, []);
  ee(() => {
    a || C(null);
  }, [a]);
  const [L, k] = Q(() => {
    const E = ce(s, a, d);
    return E.length > 0 ? E[0].id : "";
  }), j = te(/* @__PURE__ */ new Map()), B = U((E) => (y) => {
    y ? j.current.set(E, y) : j.current.delete(E);
  }, []), u = U((E) => {
    k(E);
  }, []), f = te(0), w = U((E) => {
    k(E), f.current++;
  }, []);
  ee(() => {
    const E = j.current.get(L);
    E && document.activeElement !== E && E.focus();
  }, [L, f.current]);
  const M = U((E) => {
    if (E.key === "Escape" && N !== null) {
      E.preventDefault(), b();
      return;
    }
    const y = ce(s, a, d);
    if (y.length === 0) return;
    const D = y.findIndex((I) => I.id === L);
    if (D < 0) return;
    const x = y[D];
    switch (E.key) {
      case "ArrowDown": {
        E.preventDefault();
        const I = (D + 1) % y.length;
        w(y[I].id);
        break;
      }
      case "ArrowUp": {
        E.preventDefault();
        const I = (D - 1 + y.length) % y.length;
        w(y[I].id);
        break;
      }
      case "Home": {
        E.preventDefault(), w(y[0].id);
        break;
      }
      case "End": {
        E.preventDefault(), w(y[y.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        E.preventDefault(), x.type === "nav" ? h(x.id) : x.type === "command" ? v(x.id) : x.type === "group" && (a ? N === x.id ? b() : p(x.id) : i(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !a && ((d.get(x.id) ?? !1) || (E.preventDefault(), i(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !a && (d.get(x.id) ?? !1) && (E.preventDefault(), i(x.id));
        break;
      }
    }
  }, [
    s,
    a,
    d,
    L,
    N,
    w,
    h,
    v,
    i,
    p,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (a ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, a ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: M }, s.map((E) => /* @__PURE__ */ e.createElement(
    Ee,
    {
      key: E.id,
      item: E,
      activeItemId: r,
      collapsed: a,
      onSelect: h,
      onExecute: v,
      onToggleGroup: i,
      focusedId: L,
      setItemRef: B,
      onItemFocus: u,
      groupStates: d,
      flyoutGroupId: N,
      onOpenFlyout: p,
      onCloseFlyout: b
    }
  ))), a ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(P, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(P, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(P, { control: t.activeContent })));
}, xt = ({ controlId: l }) => {
  const t = R(), o = t.direction ?? "column", n = t.gap ?? "default", s = t.align ?? "stretch", r = t.wrap === !0, a = t.children ?? [], d = [
    "tlStack",
    `tlStack--${o}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${s}`,
    r ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: d }, a.map((c, i) => /* @__PURE__ */ e.createElement(P, { key: i, control: c })));
}, St = ({ controlId: l }) => {
  const t = R(), o = t.columns, n = t.minColumnWidth, s = t.gap ?? "default", r = t.children ?? [], a = {};
  return n ? a.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : o && (a.gridTemplateColumns = `repeat(${o}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${s}`, style: a }, r.map((d, c) => /* @__PURE__ */ e.createElement(P, { key: c, control: d })));
}, Dt = ({ controlId: l }) => {
  const t = R(), o = t.title, n = t.variant ?? "outlined", s = t.padding ?? "default", r = t.headerActions ?? [], a = t.child, d = o != null || r.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, o && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, o), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, r.map((c, i) => /* @__PURE__ */ e.createElement(P, { key: i, control: c })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${s}` }, /* @__PURE__ */ e.createElement(P, { control: a })));
}, Rt = ({ controlId: l }) => {
  const t = R(), o = t.title ?? "", n = t.leading, s = t.actions ?? [], r = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    r === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: d }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(P, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, o), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((c, i) => /* @__PURE__ */ e.createElement(P, { key: i, control: c }))));
}, { useCallback: Pt } = e, Bt = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.items ?? [], s = Pt((r) => {
    o("navigate", { itemId: r });
  }, [o]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((r, a) => {
    const d = a === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: r.id, className: "tlBreadcrumb__entry" }, a > 0 && /* @__PURE__ */ e.createElement(
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
}, { useCallback: jt } = e, Mt = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.items ?? [], s = t.activeItemId, r = jt((a) => {
    a !== s && o("selectItem", { itemId: a });
  }, [o, s]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((a) => {
    const d = a.id === s;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: a.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => r(a.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + a.icon, "aria-hidden": "true" }), a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, a.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, a.label)
    );
  }));
}, { useCallback: pe, useEffect: fe, useRef: Ft } = e, It = {
  "js.dialog.close": "Close"
}, zt = ({ controlId: l }) => {
  const t = R(), o = A(), n = W(It), s = t.open === !0, r = t.title ?? "", a = t.size ?? "medium", d = t.closeOnBackdrop !== !1, c = t.actions ?? [], i = t.child, h = Ft(null), v = pe(() => {
    o("close");
  }, [o]), _ = pe((C) => {
    d && C.target === C.currentTarget && v();
  }, [d, v]);
  if (fe(() => {
    if (!s) return;
    const C = (p) => {
      p.key === "Escape" && v();
    };
    return document.addEventListener("keydown", C), () => document.removeEventListener("keydown", C);
  }, [s, v]), fe(() => {
    s && h.current && h.current.focus();
  }, [s]), !s) return null;
  const N = "tlDialog-title";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialog__backdrop", onClick: _ }, /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlDialog tlDialog--${a}`,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": N,
      ref: h,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDialog__body" }, /* @__PURE__ */ e.createElement(P, { control: i })),
    c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDialog__footer" }, c.map((C, p) => /* @__PURE__ */ e.createElement(P, { key: p, control: C })))
  ));
}, { useCallback: At, useEffect: $t } = e, Vt = {
  "js.drawer.close": "Close"
}, Ot = ({ controlId: l }) => {
  const t = R(), o = A(), n = W(Vt), s = t.open === !0, r = t.position ?? "right", a = t.size ?? "medium", d = t.title ?? null, c = t.child, i = At(() => {
    o("close");
  }, [o]);
  $t(() => {
    if (!s) return;
    const v = (_) => {
      _.key === "Escape" && i();
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [s, i]);
  const h = [
    "tlDrawer",
    `tlDrawer--${r}`,
    `tlDrawer--${a}`,
    s ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: h, "aria-hidden": !s }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: i,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, c && /* @__PURE__ */ e.createElement(P, { control: c })));
}, { useCallback: be, useEffect: Ut, useState: Wt } = e, Kt = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.message ?? "", s = t.variant ?? "info", r = t.action, a = t.duration ?? 5e3, d = t.visible === !0, [c, i] = Wt(!1), h = be(() => {
    i(!0), setTimeout(() => {
      o("dismiss"), i(!1);
    }, 200);
  }, [o]), v = be(() => {
    r && o(r.commandName), h();
  }, [o, r, h]);
  return Ut(() => {
    if (!d || a === 0) return;
    const _ = setTimeout(h, a);
    return () => clearTimeout(_);
  }, [d, a, h]), !d && !c ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${c ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: v }, r.label)
  );
}, { useCallback: re, useEffect: se, useRef: Ht, useState: he } = e, Gt = ({ controlId: l }) => {
  const t = R(), o = A(), n = t.open === !0, s = t.anchorId, r = t.items ?? [], a = Ht(null), [d, c] = he({ top: 0, left: 0 }), [i, h] = he(0), v = r.filter((p) => p.type === "item" && !p.disabled);
  se(() => {
    var u, f;
    if (!n || !s) return;
    const p = document.getElementById(s);
    if (!p) return;
    const b = p.getBoundingClientRect(), L = ((u = a.current) == null ? void 0 : u.offsetHeight) ?? 200, k = ((f = a.current) == null ? void 0 : f.offsetWidth) ?? 200;
    let j = b.bottom + 4, B = b.left;
    j + L > window.innerHeight && (j = b.top - L - 4), B + k > window.innerWidth && (B = b.right - k), c({ top: j, left: B }), h(0);
  }, [n, s]);
  const _ = re(() => {
    o("close");
  }, [o]), N = re((p) => {
    o("selectItem", { itemId: p });
  }, [o]);
  se(() => {
    if (!n) return;
    const p = (b) => {
      a.current && !a.current.contains(b.target) && _();
    };
    return document.addEventListener("mousedown", p), () => document.removeEventListener("mousedown", p);
  }, [n, _]);
  const C = re((p) => {
    if (p.key === "Escape") {
      _();
      return;
    }
    if (p.key === "ArrowDown")
      p.preventDefault(), h((b) => (b + 1) % v.length);
    else if (p.key === "ArrowUp")
      p.preventDefault(), h((b) => (b - 1 + v.length) % v.length);
    else if (p.key === "Enter" || p.key === " ") {
      p.preventDefault();
      const b = v[i];
      b && N(b.id);
    }
  }, [_, N, v, i]);
  return se(() => {
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
      onKeyDown: C
    },
    r.map((p, b) => {
      if (p.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
      const k = v.indexOf(p) === i;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: p.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (p.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: p.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => N(p.id)
        },
        p.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + p.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, p.label)
      );
    })
  ) : null;
}, Yt = ({ controlId: l }) => {
  const t = R(), o = t.header, n = t.content, s = t.footer, r = t.snackbar;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(P, { control: o })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(P, { control: n })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(P, { control: s })), /* @__PURE__ */ e.createElement(P, { control: r }));
}, Xt = () => {
  const t = R().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, qt = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, _e = 50, Zt = () => {
  const l = R(), t = A(), o = W(qt), n = l.columns ?? [], s = l.totalRowCount ?? 0, r = l.rows ?? [], a = l.rowHeight ?? 36, d = l.selectionMode ?? "single", c = l.selectedCount ?? 0, i = l.frozenColumnCount ?? 0, h = l.treeMode ?? !1, v = e.useMemo(
    () => n.filter((m) => m.sortPriority && m.sortPriority > 0).length,
    [n]
  ), _ = d === "multi", N = 40, C = 20, p = e.useRef(null), b = e.useRef(null), L = e.useRef(null), [k, j] = e.useState({}), B = e.useRef(null), u = e.useRef(!1), f = e.useRef(null), [w, M] = e.useState(null), [E, y] = e.useState(null);
  e.useEffect(() => {
    B.current || j({});
  }, [n]);
  const D = e.useCallback((m) => k[m.name] ?? m.width, [k]), x = e.useMemo(() => {
    const m = [];
    let g = _ && i > 0 ? N : 0;
    for (let S = 0; S < i && S < n.length; S++)
      m.push(g), g += D(n[S]);
    return m;
  }, [n, i, _, N, D]), I = s * a, K = e.useCallback((m, g, S) => {
    S.preventDefault(), S.stopPropagation(), B.current = { column: m, startX: S.clientX, startWidth: g };
    const F = (V) => {
      const O = B.current;
      if (!O) return;
      const $ = Math.max(_e, O.startWidth + (V.clientX - O.startX));
      j((ae) => ({ ...ae, [O.column]: $ }));
    }, z = (V) => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", z);
      const O = B.current;
      if (O) {
        const $ = Math.max(_e, O.startWidth + (V.clientX - O.startX));
        t("columnResize", { column: O.column, width: $ }), B.current = null, u.current = !0, requestAnimationFrame(() => {
          u.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", z);
  }, [t]), H = e.useCallback(() => {
    p.current && b.current && (p.current.scrollLeft = b.current.scrollLeft), L.current !== null && clearTimeout(L.current), L.current = window.setTimeout(() => {
      const m = b.current;
      if (!m) return;
      const g = m.scrollTop, S = Math.ceil(m.clientHeight / a), F = Math.floor(g / a);
      t("scroll", { start: F, count: S });
    }, 80);
  }, [t, a]), G = e.useCallback((m, g, S) => {
    if (u.current) return;
    let F;
    !g || g === "desc" ? F = "asc" : F = "desc";
    const z = S.shiftKey ? "add" : "replace";
    t("sort", { column: m, direction: F, mode: z });
  }, [t]), Z = e.useCallback((m, g) => {
    f.current = m, g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", m);
  }, []), J = e.useCallback((m, g) => {
    if (!f.current || f.current === m) {
      M(null);
      return;
    }
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const S = g.currentTarget.getBoundingClientRect(), F = g.clientX < S.left + S.width / 2 ? "left" : "right";
    M({ column: m, side: F });
  }, []), Y = e.useCallback((m) => {
    m.preventDefault(), m.stopPropagation();
    const g = f.current;
    if (!g || !w) {
      f.current = null, M(null);
      return;
    }
    let S = n.findIndex((z) => z.name === w.column);
    if (S < 0) {
      f.current = null, M(null);
      return;
    }
    const F = n.findIndex((z) => z.name === g);
    w.side === "right" && S++, F < S && S--, t("columnReorder", { column: g, targetIndex: S }), f.current = null, M(null);
  }, [n, w, t]), ge = e.useCallback(() => {
    f.current = null, M(null);
  }, []), Ce = e.useCallback((m, g) => {
    g.shiftKey && g.preventDefault(), t("select", {
      rowIndex: m,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), ye = e.useCallback((m, g) => {
    g.stopPropagation(), t("select", { rowIndex: m, ctrlKey: !0, shiftKey: !1 });
  }, [t]), ke = e.useCallback(() => {
    const m = c === s && s > 0;
    t("selectAll", { selected: !m });
  }, [t, c, s]), we = e.useCallback((m, g, S) => {
    S.stopPropagation(), t("expand", { rowIndex: m, expanded: g });
  }, [t]), Ne = e.useCallback((m, g) => {
    g.preventDefault(), y({ x: g.clientX, y: g.clientY, colIdx: m });
  }, []), Le = e.useCallback(() => {
    E && (t("setFrozenColumnCount", { count: E.colIdx + 1 }), y(null));
  }, [E, t]), Te = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), y(null);
  }, [t]);
  e.useEffect(() => {
    if (!E) return;
    const m = () => y(null), g = (S) => {
      S.key === "Escape" && y(null);
    };
    return document.addEventListener("mousedown", m), document.addEventListener("keydown", g), () => {
      document.removeEventListener("mousedown", m), document.removeEventListener("keydown", g);
    };
  }, [E]);
  const ne = n.reduce((m, g) => m + D(g), 0) + (_ ? N : 0), xe = c === s && s > 0, ue = c > 0 && c < s, Se = e.useCallback((m) => {
    m && (m.indeterminate = ue);
  }, [ue]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (m) => {
        if (!f.current) return;
        m.preventDefault();
        const g = b.current, S = p.current;
        if (!g) return;
        const F = g.getBoundingClientRect(), z = 40, V = 8;
        m.clientX < F.left + z ? g.scrollLeft = Math.max(0, g.scrollLeft - V) : m.clientX > F.right - z && (g.scrollLeft += V), S && (S.scrollLeft = g.scrollLeft);
      },
      onDrop: Y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: p }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: ne } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (m) => {
          f.current && (m.preventDefault(), m.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== f.current && M({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Se,
          className: "tlTableView__checkbox",
          checked: xe,
          onChange: ke
        }
      )
    ), n.map((m, g) => {
      const S = D(m), F = g === n.length - 1;
      let z = "tlTableView__headerCell";
      m.sortable && (z += " tlTableView__headerCell--sortable"), w && w.column === m.name && (z += " tlTableView__headerCell--dragOver-" + w.side);
      const V = g < i, O = g === i - 1;
      return V && (z += " tlTableView__headerCell--frozen"), O && (z += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: m.name,
          className: z,
          style: {
            ...F && !V ? { flex: "1 0 auto", minWidth: S } : { width: S, minWidth: S },
            position: V ? "sticky" : "relative",
            ...V ? { left: x[g], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: m.sortable ? ($) => G(m.name, m.sortDirection, $) : void 0,
          onContextMenu: ($) => Ne(g, $),
          onDragStart: ($) => Z(m.name, $),
          onDragOver: ($) => J(m.name, $),
          onDrop: Y,
          onDragEnd: ge
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, m.label),
        m.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, m.sortDirection === "asc" ? "▲" : "▼", v > 1 && m.sortPriority != null && m.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, m.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: ($) => K(m.name, S, $)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (m) => {
          if (f.current && n.length > 0) {
            const g = n[n.length - 1];
            g.name !== f.current && (m.preventDefault(), m.dataTransfer.dropEffect = "move", M({ column: g.name, side: "right" }));
          }
        },
        onDrop: Y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: H
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: I, position: "relative", minWidth: ne } }, r.map((m) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: m.id,
          className: "tlTableView__row" + (m.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: m.index * a,
            height: a,
            minWidth: ne,
            width: "100%"
          },
          onClick: (g) => Ce(m.index, g)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (i > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: N,
              minWidth: N,
              ...i > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (g) => g.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: m.selected,
              onChange: () => {
              },
              onClick: (g) => ye(m.index, g),
              tabIndex: -1
            }
          )
        ),
        n.map((g, S) => {
          const F = D(g), z = S === n.length - 1, V = S < i, O = S === i - 1;
          let $ = "tlTableView__cell";
          V && ($ += " tlTableView__cell--frozen"), O && ($ += " tlTableView__cell--frozenLast");
          const ae = h && S === 0, De = m.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: g.name,
              className: $,
              style: {
                ...z && !V ? { flex: "1 0 auto", minWidth: F } : { width: F, minWidth: F },
                ...V ? { position: "sticky", left: x[S], zIndex: 2 } : {}
              }
            },
            ae ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: De * C } }, m.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (Re) => we(m.index, !m.expanded, Re)
              },
              m.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(P, { control: m.cells[g.name] })) : /* @__PURE__ */ e.createElement(P, { control: m.cells[g.name] })
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
        onMouseDown: (m) => m.stopPropagation()
      },
      E.colIdx + 1 !== i && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Le }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.freezeUpTo"])),
      i > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.unfreezeAll"]))
    )
  );
}, Jt = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ve = e.createContext(Jt), { useMemo: Qt, useRef: en, useState: tn, useEffect: nn } = e, an = 320, ln = ({ controlId: l }) => {
  const t = R(), o = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", s = t.readOnly === !0, r = t.children ?? [], a = en(null), [d, c] = tn(
    n === "top" ? "top" : "side"
  );
  nn(() => {
    if (n !== "auto") {
      c(n);
      return;
    }
    const N = a.current;
    if (!N) return;
    const C = new ResizeObserver((p) => {
      for (const b of p) {
        const k = b.contentRect.width / o;
        c(k < an ? "top" : "side");
      }
    });
    return C.observe(N), () => C.disconnect();
  }, [n, o]);
  const i = Qt(() => ({
    readOnly: s,
    resolvedLabelPosition: d
  }), [s, d]), v = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / o))}rem`}, 1fr))`
  }, _ = [
    "tlFormLayout",
    s ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(ve.Provider, { value: i }, /* @__PURE__ */ e.createElement("div", { id: l, className: _, style: v, ref: a }, r.map((N, C) => /* @__PURE__ */ e.createElement(P, { key: C, control: N }))));
}, { useCallback: on } = e, rn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, sn = ({ controlId: l }) => {
  const t = R(), o = A(), n = W(rn), s = t.header, r = t.headerActions ?? [], a = t.collapsible === !0, d = t.collapsed === !0, c = t.border ?? "none", i = t.fullLine === !0, h = t.children ?? [], v = s != null || r.length > 0 || a, _ = on(() => {
    o("toggleCollapse");
  }, [o]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${c}`,
    i ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, v && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, a && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
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
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, s), r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, r.map((C, p) => /* @__PURE__ */ e.createElement(P, { key: p, control: C })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, h.map((C, p) => /* @__PURE__ */ e.createElement(P, { key: p, control: C }))));
}, { useContext: cn, useState: dn, useCallback: un } = e, mn = ({ controlId: l }) => {
  const t = R(), o = cn(ve), n = t.label ?? "", s = t.required === !0, r = t.error, a = t.helpText, d = t.dirty === !0, c = t.labelPosition ?? o.resolvedLabelPosition, i = t.fullLine === !0, h = t.visible !== !1, v = t.field, _ = o.readOnly, [N, C] = dn(!1), p = un(() => C((k) => !k), []);
  if (!h) return null;
  const b = r != null, L = [
    "tlFormField",
    `tlFormField--${c}`,
    _ ? "tlFormField--readonly" : "",
    i ? "tlFormField--fullLine" : "",
    b ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), s && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), a && !_ && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(P, { control: v })), !_ && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, r)), !_ && a && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, a));
}, pn = () => {
  const l = R(), t = A(), o = l.iconCss, n = l.iconSrc, s = l.label, r = l.cssClass, a = l.tooltip, d = l.hasLink, c = o ? /* @__PURE__ */ e.createElement("i", { className: o }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, i = /* @__PURE__ */ e.createElement(e.Fragment, null, c, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), h = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), v = ["tlResourceCell", r].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: v, href: "#", onClick: h, title: a }, i) : /* @__PURE__ */ e.createElement("span", { className: v, title: a }, i);
}, fn = 20, bn = () => {
  const l = R(), t = A(), o = l.nodes ?? [], n = l.selectionMode ?? "single", s = l.dragEnabled ?? !1, r = l.dropEnabled ?? !1, a = l.dropIndicatorNodeId ?? null, d = l.dropIndicatorPosition ?? null, [c, i] = e.useState(-1), h = e.useRef(null), v = e.useCallback((u, f) => {
    t(f ? "collapse" : "expand", { nodeId: u });
  }, [t]), _ = e.useCallback((u, f) => {
    t("select", {
      nodeId: u,
      ctrlKey: f.ctrlKey || f.metaKey,
      shiftKey: f.shiftKey
    });
  }, [t]), N = e.useCallback((u, f) => {
    f.preventDefault(), t("contextMenu", { nodeId: u, x: f.clientX, y: f.clientY });
  }, [t]), C = e.useRef(null), p = e.useCallback((u, f) => {
    const w = f.getBoundingClientRect(), M = u.clientY - w.top, E = w.height / 3;
    return M < E ? "above" : M > E * 2 ? "below" : "within";
  }, []), b = e.useCallback((u, f) => {
    f.dataTransfer.effectAllowed = "move", f.dataTransfer.setData("text/plain", u);
  }, []), L = e.useCallback((u, f) => {
    f.preventDefault(), f.dataTransfer.dropEffect = "move";
    const w = p(f, f.currentTarget);
    C.current != null && window.clearTimeout(C.current), C.current = window.setTimeout(() => {
      t("dragOver", { nodeId: u, position: w }), C.current = null;
    }, 50);
  }, [t, p]), k = e.useCallback((u, f) => {
    f.preventDefault(), C.current != null && (window.clearTimeout(C.current), C.current = null);
    const w = p(f, f.currentTarget);
    t("drop", { nodeId: u, position: w });
  }, [t, p]), j = e.useCallback(() => {
    C.current != null && (window.clearTimeout(C.current), C.current = null), t("dragEnd");
  }, [t]), B = e.useCallback((u) => {
    if (o.length === 0) return;
    let f = c;
    switch (u.key) {
      case "ArrowDown":
        u.preventDefault(), f = Math.min(c + 1, o.length - 1);
        break;
      case "ArrowUp":
        u.preventDefault(), f = Math.max(c - 1, 0);
        break;
      case "ArrowRight":
        if (u.preventDefault(), c >= 0 && c < o.length) {
          const w = o[c];
          if (w.expandable && !w.expanded) {
            t("expand", { nodeId: w.id });
            return;
          } else w.expanded && (f = c + 1);
        }
        break;
      case "ArrowLeft":
        if (u.preventDefault(), c >= 0 && c < o.length) {
          const w = o[c];
          if (w.expanded) {
            t("collapse", { nodeId: w.id });
            return;
          } else {
            const M = w.depth;
            for (let E = c - 1; E >= 0; E--)
              if (o[E].depth < M) {
                f = E;
                break;
              }
          }
        }
        break;
      case "Enter":
        u.preventDefault(), c >= 0 && c < o.length && t("select", {
          nodeId: o[c].id,
          ctrlKey: u.ctrlKey || u.metaKey,
          shiftKey: u.shiftKey
        });
        return;
      case " ":
        u.preventDefault(), n === "multi" && c >= 0 && c < o.length && t("select", {
          nodeId: o[c].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        u.preventDefault(), f = 0;
        break;
      case "End":
        u.preventDefault(), f = o.length - 1;
        break;
      default:
        return;
    }
    f !== c && i(f);
  }, [c, o, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: h,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: B
    },
    o.map((u, f) => /* @__PURE__ */ e.createElement(
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
          f === c ? "tlTreeView__node--focused" : "",
          a === u.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          a === u.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          a === u.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: u.depth * fn },
        draggable: s,
        onClick: (w) => _(u.id, w),
        onContextMenu: (w) => N(u.id, w),
        onDragStart: (w) => b(u.id, w),
        onDragOver: r ? (w) => L(u.id, w) : void 0,
        onDrop: r ? (w) => k(u.id, w) : void 0,
        onDragEnd: j
      },
      u.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (w) => {
            w.stopPropagation(), v(u.id, u.expanded);
          },
          tabIndex: -1,
          "aria-label": u.expanded ? "Collapse" : "Expand"
        },
        u.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: u.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(P, { control: u.content }))
    ))
  );
};
T("TLButton", Ke);
T("TLToggleButton", Ge);
T("TLTextInput", je);
T("TLNumberInput", Fe);
T("TLDatePicker", ze);
T("TLSelect", $e);
T("TLCheckbox", Oe);
T("TLTable", Ue);
T("TLCounter", Ye);
T("TLTabBar", qe);
T("TLFieldList", Ze);
T("TLAudioRecorder", Qe);
T("TLAudioPlayer", tt);
T("TLFileUpload", at);
T("TLDownload", ot);
T("TLPhotoCapture", st);
T("TLPhotoViewer", it);
T("TLSplitPanel", dt);
T("TLPanel", _t);
T("TLMaximizeRoot", Et);
T("TLDeckPane", vt);
T("TLSidebar", Tt);
T("TLStack", xt);
T("TLGrid", St);
T("TLCard", Dt);
T("TLAppBar", Rt);
T("TLBreadcrumb", Bt);
T("TLBottomBar", Mt);
T("TLDialog", zt);
T("TLDrawer", Ot);
T("TLSnackbar", Kt);
T("TLMenu", Gt);
T("TLAppShell", Yt);
T("TLTextCell", Xt);
T("TLTableView", Zt);
T("TLFormLayout", ln);
T("TLFormGroup", sn);
T("TLFormField", mn);
T("TLResourceCell", pn);
T("TLTreeView", bn);
