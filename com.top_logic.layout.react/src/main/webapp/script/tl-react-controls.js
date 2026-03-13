import { React as e, useTLFieldValue as Ce, getComponent as ft, useTLState as K, useTLCommand as le, TLChild as Y, useTLUpload as We, useI18N as oe, useTLDataUrl as Ve, register as H } from "tl-react-bridge";
const { useCallback: ht } = e, _t = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = ht(
    (i) => {
      l(i.target.value);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactTextInput"
    }
  ));
}, { useCallback: bt } = e, vt = ({ controlId: n, state: t, config: r }) => {
  const [l, c] = Ce(), i = bt(
    (d) => {
      const s = d.target.value, a = s === "" ? null : Number(s);
      c(a);
    },
    [c]
  ), o = r != null && r.decimal ? "0.01" : "1";
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: l != null ? String(l) : "",
      onChange: i,
      step: o,
      disabled: t.disabled === !0,
      className: "tlReactNumberInput"
    }
  ));
}, { useCallback: Et } = e, gt = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = Et(
    (i) => {
      l(i.target.value || null);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "") : /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactDatePicker"
    }
  ));
}, { useCallback: Ct } = e, yt = ({ controlId: n, state: t, config: r }) => {
  var d;
  const [l, c] = Ce(), i = Ct(
    (s) => {
      c(s.target.value || null);
    },
    [c]
  ), o = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const s = ((d = o.find((a) => a.value === l)) == null ? void 0 : d.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactSelect tlReactSelect--immutable" }, s);
  }
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((s) => /* @__PURE__ */ e.createElement("option", { key: s.value, value: s.value }, s.label))
  ));
}, { useCallback: wt } = e, kt = ({ controlId: n, state: t }) => {
  const [r, l] = Ce(), c = wt(
    (i) => {
      l(i.target.checked);
    },
    [l]
  );
  return t.editable === !1 ? /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: n,
      checked: r === !0,
      disabled: !0,
      className: "tlReactCheckbox tlReactCheckbox--immutable"
    }
  ) : /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: n,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: "tlReactCheckbox"
    }
  );
}, St = ({ controlId: n, state: t }) => {
  const r = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: n, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, r.map((o) => {
    const d = o.cellModule ? ft(o.cellModule) : void 0, s = c[o.name];
    if (d) {
      const a = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        d,
        {
          controlId: n + "-" + i + "-" + o.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: Nt } = e, Tt = ({ controlId: n, command: t, label: r, disabled: l }) => {
  const c = K(), i = le(), o = t ?? "click", d = r ?? c.label, s = l ?? c.disabled === !0, a = Nt(() => {
    i(o);
  }, [i, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: a,
      disabled: s,
      className: "tlReactButton"
    },
    d
  );
}, { useCallback: Rt } = e, Lt = ({ controlId: n, command: t, label: r, active: l, disabled: c }) => {
  const i = K(), o = le(), d = t ?? "click", s = r ?? i.label, a = l ?? i.active === !0, m = c ?? i.disabled === !0, p = Rt(() => {
    o(d);
  }, [o, d]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    s
  );
}, Dt = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: xt } = e, It = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.tabs ?? [], c = t.activeTabId, i = xt((o) => {
    o !== c && r("selectTab", { tabId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, jt = ({ controlId: n }) => {
  const t = K(), r = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Pt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Mt = ({ controlId: n }) => {
  const t = K(), r = We(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), d = e.useRef(null), s = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, _ = m === "received" ? "idle" : l !== "idle" ? l : m, L = e.useCallback(async () => {
    if (l === "recording") {
      const k = d.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (l !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = k, s.current = [];
        const O = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, O ? { mimeType: O } : void 0);
        d.current = P, P.ondataavailable = (h) => {
          h.data.size > 0 && s.current.push(h.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((g) => g.stop()), a.current = null;
          const h = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], h.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const E = new FormData();
          E.append("audio", h, "recording.webm"), await r(E), c("idle");
        }, P.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, r]), v = oe(Pt), b = _ === "recording" ? v["js.audioRecorder.stop"] : _ === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], f = _ === "uploading", N = ["tlAudioRecorder__button"];
  return _ === "recording" && N.push("tlAudioRecorder__button--recording"), _ === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: L,
      disabled: f,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, At = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ot = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), d = e.useRef(null), s = e.useRef(null), a = e.useRef(c);
  e.useEffect(() => {
    l ? i === "disabled" && o("idle") : (d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [l]), e.useEffect(() => {
    c !== a.current && (a.current = c, d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
  }, [c]), e.useEffect(() => () => {
    d.current && (d.current.pause(), d.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      d.current && d.current.pause(), o("paused");
      return;
    }
    if (i === "paused" && d.current) {
      d.current.play(), o("playing");
      return;
    }
    if (!s.current) {
      o("loading");
      try {
        const f = await fetch(r);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const N = await f.blob();
        s.current = URL.createObjectURL(N);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    d.current = b, b.onended = () => {
      o("idle");
    }, b.play(), o("playing");
  }, [i, r]), p = oe(At), _ = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], L = i === "disabled" || i === "loading", v = ["tlAudioPlayer__button"];
  return i === "playing" && v.push("tlAudioPlayer__button--playing"), i === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: m,
      disabled: L,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, $t = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Bt = ({ controlId: n }) => {
  const t = K(), r = We(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), d = e.useRef(null), s = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, _ = e.useCallback(async (h) => {
    c("uploading");
    const E = new FormData();
    E.append("file", h, h.name), await r(E), c("idle");
  }, [r]), L = e.useCallback((h) => {
    var g;
    const E = (g = h.target.files) == null ? void 0 : g[0];
    E && _(E);
  }, [_]), v = e.useCallback(() => {
    var h;
    l !== "uploading" && ((h = d.current) == null || h.click());
  }, [l]), b = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!0);
  }, []), f = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!1);
  }, []), N = e.useCallback((h) => {
    var g;
    if (h.preventDefault(), h.stopPropagation(), o(!1), l === "uploading") return;
    const E = (g = h.dataTransfer.files) == null ? void 0 : g[0];
    E && _(E);
  }, [l, _]), k = p === "uploading", O = oe($t), P = p === "uploading" ? O["js.uploading"] : O["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: f,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
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
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: k,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, Ft = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ht = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", d = !!t.clearable, [s, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      a(!0);
      try {
        const v = r + (r.includes("?") ? "&" : "?") + "rev=" + i, b = await fetch(v);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const f = await b.blob(), N = URL.createObjectURL(f), k = document.createElement("a");
        k.href = N, k.download = o, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(N);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        a(!1);
      }
    }
  }, [c, s, r, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), _ = oe(Ft);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const L = s ? _["js.downloading"] : _["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: L,
      "aria-label": L
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), d && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: _["js.download.clear"],
      "aria-label": _["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, zt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Ut = ({ controlId: n }) => {
  const t = K(), r = We(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [d, s] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), _ = e.useRef(null), L = e.useRef(null), v = t.error, b = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    f(), c("idle");
  }, [f]), k = e.useCallback(async () => {
    var R;
    if (l !== "uploading") {
      if (o(null), !b) {
        (R = _.current) == null || R.click();
        return;
      }
      try {
        const A = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = A, c("overlayOpen");
      } catch (A) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", A), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [l, b]), O = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const R = a.current, A = p.current;
    if (!R || !A)
      return;
    A.width = R.videoWidth, A.height = R.videoHeight;
    const S = A.getContext("2d");
    S && (S.drawImage(R, 0, 0), f(), c("uploading"), A.toBlob(async (D) => {
      if (!D) {
        c("idle");
        return;
      }
      const V = new FormData();
      V.append("photo", D, "capture.jpg"), await r(V), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, r, f]), P = e.useCallback(async (R) => {
    var D;
    const A = (D = R.target.files) == null ? void 0 : D[0];
    if (!A) return;
    c("uploading");
    const S = new FormData();
    S.append("photo", A, A.name), await r(S), c("idle"), _.current && (_.current.value = "");
  }, [r]);
  e.useEffect(() => {
    l === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var A;
    if (l !== "overlayOpen") return;
    (A = L.current) == null || A.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const R = (A) => {
      A.key === "Escape" && N();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [l, N]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const h = oe(zt), E = l === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const X = ["tlPhotoCapture__overlayVideo"];
  d && X.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return d && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: k,
      disabled: l === "uploading",
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !b && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: _,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: L,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: X.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: T.join(" "),
        onClick: () => s((R) => !R),
        title: h["js.photoCapture.mirror"],
        "aria-label": h["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: O,
        title: h["js.photoCapture.capture"],
        "aria-label": h["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[i]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, Wt = {
  "js.photoViewer.alt": "Captured photo"
}, Vt = ({ controlId: n }) => {
  const t = K(), r = Ve(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), d = e.useRef(c);
  e.useEffect(() => {
    if (!l) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (c === d.current && i)
      return;
    d.current = c, i && (URL.revokeObjectURL(i), o(null));
    let a = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        a || o(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [l, c, r]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(Wt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ye, useRef: Ie } = e, Kt = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", d = i.length > 0 && i.every((f) => f.collapsed), s = !d && i.some((f) => f.collapsed), a = d ? !o : o, m = Ie(null), p = Ie(null), _ = Ie(null), L = Ye((f, N) => {
    const k = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? d && !a ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : N !== void 0 ? k.flex = `0 0 ${N}px` : f.unit === "%" || s ? k.flex = `${f.size} 0 0%` : k.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (k.minWidth = o ? f.minSize : void 0, k.minHeight = o ? void 0 : f.minSize), k;
  }, [o, d, s, a]), v = Ye((f, N) => {
    f.preventDefault();
    const k = m.current;
    if (!k) return;
    const O = i[N], P = i[N + 1], h = k.querySelectorAll(":scope > .tlSplitPanel__child"), E = [];
    h.forEach((T) => {
      E.push(o ? T.offsetWidth : T.offsetHeight);
    }), _.current = E, p.current = {
      splitterIndex: N,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: E[N],
      startSizeAfter: E[N + 1],
      childBefore: O,
      childAfter: P
    };
    const g = (T) => {
      const R = p.current;
      if (!R || !_.current) return;
      const S = (o ? T.clientX : T.clientY) - R.startPos, D = R.childBefore.minSize || 0, V = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + S, q = R.startSizeAfter - S;
      ee < D && (q += ee - D, ee = D), q < V && (ee += q - V, q = V), _.current[R.splitterIndex] = ee, _.current[R.splitterIndex + 1] = q;
      const ae = k.querySelectorAll(":scope > .tlSplitPanel__child"), B = ae[R.splitterIndex], j = ae[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${q}px`);
    }, X = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", X), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const T = {};
        i.forEach((R, A) => {
          const S = R.control;
          S != null && S.controlId && _.current && (T[S.controlId] = _.current[A]);
        }), r("updateSizes", { sizes: T });
      }
      _.current = null, p.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", X), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, r]), b = [];
  return i.forEach((f, N) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${f.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: L(f)
        },
        /* @__PURE__ */ e.createElement(Y, { control: f.control })
      )
    ), c && N < i.length - 1) {
      const k = i[N + 1];
      !f.collapsed && !k.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (P) => v(P, N)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: n,
      className: `tlSplitPanel tlSplitPanel--${l}${d ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    b
  );
}, { useCallback: je } = e, Yt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Gt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Jt = ({ controlId: n }) => {
  const t = K(), r = le(), l = oe(Yt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, d = t.showMaximize === !0, s = t.showPopOut === !0, a = t.toolbarButtons ?? [], m = i === "MINIMIZED", p = i === "MAXIMIZED", _ = i === "HIDDEN", L = je(() => {
    r("toggleMinimize");
  }, [r]), v = je(() => {
    r("toggleMaximize");
  }, [r]), b = je(() => {
    r("popOut");
  }, [r]);
  if (_)
    return null;
  const f = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: f
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, a.map((N, k) => /* @__PURE__ */ e.createElement("span", { key: k, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: N }))), o && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: L,
        title: m ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Xt, null) : /* @__PURE__ */ e.createElement(Gt, null)
    ), d && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: p ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Zt, null) : /* @__PURE__ */ e.createElement(qt, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Qt, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child }))
  );
}, en = ({ controlId: n }) => {
  const t = K();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, tn = ({ controlId: n }) => {
  const t = K();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: Te, useEffect: Re, useRef: Le } = e, nn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function He(n, t, r, l) {
  const c = [];
  for (const i of n)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: l }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: l }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (r.get(i.id) ?? i.expanded) && !t && c.push(...He(i.children, t, r, i.id)));
  return c;
}
const be = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, ln = ({ item: n, active: t, collapsed: r, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: r ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  r && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(be, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
  !r && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
), an = ({ item: n, collapsed: t, onExecute: r, tabIndex: l, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(n.id),
    title: t ? n.label : void 0,
    tabIndex: l,
    ref: c,
    onFocus: () => i(n.id)
  },
  /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), rn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(be, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), on = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), sn = ({ item: n, activeItemId: t, anchorRect: r, onSelect: l, onExecute: c, onClose: i }) => {
  const o = Le(null);
  Re(() => {
    const a = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [i]), Re(() => {
    const a = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [i]);
  const d = ue((a) => {
    a.type === "nav" ? (l(a.id), i()) : a.type === "command" && (c(a.id), i());
  }, [l, c, i]), s = {};
  return r && (s.left = r.right, s.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, n.label), n.children.map((a) => {
    if (a.type === "nav" || a.type === "command") {
      const m = a.type === "nav" && a.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: a.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => d(a)
        },
        /* @__PURE__ */ e.createElement(be, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, cn = ({
  item: n,
  expanded: t,
  activeItemId: r,
  collapsed: l,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: d,
  itemRef: s,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: _,
  flyoutGroupId: L,
  onOpenFlyout: v,
  onCloseFlyout: b
}) => {
  const f = Le(null), [N, k] = Te(null), O = ue(() => {
    l ? L === n.id ? b() : (f.current && k(f.current.getBoundingClientRect()), v(n.id)) : o(n.id);
  }, [l, L, n.id, o, v, b]), P = ue((E) => {
    f.current = E, s(E);
  }, [s]), h = l && L === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: O,
      title: l ? n.label : void 0,
      "aria-expanded": l ? h : t,
      tabIndex: d,
      ref: P,
      onFocus: () => a(n.id)
    },
    /* @__PURE__ */ e.createElement(be, { icon: n.icon }),
    !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
    !l && /* @__PURE__ */ e.createElement(
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
  ), h && /* @__PURE__ */ e.createElement(
    sn,
    {
      item: n,
      activeItemId: r,
      anchorRect: N,
      onSelect: c,
      onExecute: i,
      onClose: b
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, n.children.map((E) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: E.id,
      item: E,
      activeItemId: r,
      collapsed: l,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: L,
      onOpenFlyout: v,
      onCloseFlyout: b
    }
  ))));
}, st = ({
  item: n,
  activeItemId: t,
  collapsed: r,
  onSelect: l,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: d,
  onItemFocus: s,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: _
}) => {
  switch (n.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        ln,
        {
          item: n,
          active: n.id === t,
          collapsed: r,
          onSelect: l,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        an,
        {
          item: n,
          collapsed: r,
          onExecute: c,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(rn, { item: n, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(on, null);
    case "group": {
      const L = a ? a.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        cn,
        {
          item: n,
          expanded: L,
          activeItemId: t,
          collapsed: r,
          onSelect: l,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: d(n.id),
          onFocus: s,
          focusedId: o,
          setItemRef: d,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: _
        }
      );
    }
    default:
      return null;
  }
}, un = ({ controlId: n }) => {
  const t = K(), r = le(), l = oe(nn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [d, s] = Te(() => {
    const T = /* @__PURE__ */ new Map(), R = (A) => {
      for (const S of A)
        S.type === "group" && (T.set(S.id, S.expanded), R(S.children));
    };
    return R(c), T;
  }), a = ue((T) => {
    s((R) => {
      const A = new Map(R), S = A.get(T) ?? !1;
      return A.set(T, !S), r("toggleGroup", { itemId: T, expanded: !S }), A;
    });
  }, [r]), m = ue((T) => {
    T !== i && r("selectItem", { itemId: T });
  }, [r, i]), p = ue((T) => {
    r("executeCommand", { itemId: T });
  }, [r]), _ = ue(() => {
    r("toggleCollapse", {});
  }, [r]), [L, v] = Te(null), b = ue((T) => {
    v(T);
  }, []), f = ue(() => {
    v(null);
  }, []);
  Re(() => {
    o || v(null);
  }, [o]);
  const [N, k] = Te(() => {
    const T = He(c, o, d);
    return T.length > 0 ? T[0].id : "";
  }), O = Le(/* @__PURE__ */ new Map()), P = ue((T) => (R) => {
    R ? O.current.set(T, R) : O.current.delete(T);
  }, []), h = ue((T) => {
    k(T);
  }, []), E = Le(0), g = ue((T) => {
    k(T), E.current++;
  }, []);
  Re(() => {
    const T = O.current.get(N);
    T && document.activeElement !== T && T.focus();
  }, [N, E.current]);
  const X = ue((T) => {
    if (T.key === "Escape" && L !== null) {
      T.preventDefault(), f();
      return;
    }
    const R = He(c, o, d);
    if (R.length === 0) return;
    const A = R.findIndex((D) => D.id === N);
    if (A < 0) return;
    const S = R[A];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const D = (A + 1) % R.length;
        g(R[D].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const D = (A - 1 + R.length) % R.length;
        g(R[D].id);
        break;
      }
      case "Home": {
        T.preventDefault(), g(R[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), g(R[R.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), S.type === "nav" ? m(S.id) : S.type === "command" ? p(S.id) : S.type === "group" && (o ? L === S.id ? f() : b(S.id) : a(S.id));
        break;
      }
      case "ArrowRight": {
        S.type === "group" && !o && ((d.get(S.id) ?? !1) || (T.preventDefault(), a(S.id)));
        break;
      }
      case "ArrowLeft": {
        S.type === "group" && !o && (d.get(S.id) ?? !1) && (T.preventDefault(), a(S.id));
        break;
      }
    }
  }, [
    c,
    o,
    d,
    N,
    L,
    g,
    m,
    p,
    a,
    b,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: X }, c.map((T) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: T.id,
      item: T,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: a,
      focusedId: N,
      setItemRef: P,
      onItemFocus: h,
      groupStates: d,
      flyoutGroupId: L,
      onOpenFlyout: b,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
      title: o ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, dn = ({ controlId: n }) => {
  const t = K(), r = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], d = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: d }, o.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })));
}, mn = ({ controlId: n }) => {
  const t = K(), r = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : r && (o.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((d, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: d })));
}, pn = ({ controlId: n }) => {
  const t = K(), r = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, d = r != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, d && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, fn = ({ controlId: n }) => {
  const t = K(), r = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", d = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: d }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: s }))));
}, { useCallback: hn } = e, _n = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.items ?? [], c = hn((i) => {
    r("navigate", { itemId: i });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((i, o) => {
    const d = o === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: i.id, className: "tlBreadcrumb__entry" }, o > 0 && /* @__PURE__ */ e.createElement(
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
    ), d ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: bn } = e, vn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.items ?? [], c = t.activeItemId, i = bn((o) => {
    o !== c && r("selectItem", { itemId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((o) => {
    const d = o.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (d ? " tlBottomBar__item--active" : ""),
        onClick: () => i(o.id),
        "aria-current": d ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: Ge, useEffect: Xe, useRef: En } = e, gn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = En(null), d = Ge(() => {
    r("close");
  }, [r]), s = Ge((a) => {
    c && a.target === a.currentTarget && d();
  }, [c, d]);
  return Xe(() => {
    if (!l) return;
    const a = (m) => {
      m.key === "Escape" && d();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [l, d]), Xe(() => {
    l && o.current && o.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlDialog__backdrop",
      onClick: s,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Y, { control: i })
  ) : null;
}, { useEffect: Cn, useRef: yn } = e, wn = ({ controlId: n }) => {
  const r = K().dialogs ?? [], l = yn(r.length);
  return Cn(() => {
    r.length < l.current && r.length > 0, l.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialogManager" }, r.map((c, i) => /* @__PURE__ */ e.createElement(Y, { key: i, control: c })));
}, { useCallback: qe, useRef: ye, useState: Ze } = e, kn = {
  "js.window.close": "Close"
}, Sn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Nn = ({ controlId: n }) => {
  const t = K(), r = le(), l = oe(kn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, d = t.resizable === !0, s = t.child, a = t.actions ?? [], [m, p] = Ze(null), [_, L] = Ze(null), v = ye(null), b = ye(null), f = ye(null), N = ye(null), k = qe(() => {
    r("close");
  }, [r]), O = qe((E, g) => {
    g.preventDefault();
    const X = f.current;
    if (!X) return;
    const T = X.getBoundingClientRect();
    N.current = {
      dir: E,
      startX: g.clientX,
      startY: g.clientY,
      startW: T.width,
      startH: T.height
    };
    const R = (S) => {
      const D = N.current;
      if (!D) return;
      const V = S.clientX - D.startX, ee = S.clientY - D.startY;
      let q = D.startW, ae = D.startH;
      D.dir.includes("e") && (q = D.startW + V), D.dir.includes("w") && (q = D.startW - V), D.dir.includes("s") && (ae = D.startH + ee), D.dir.includes("n") && (ae = D.startH - ee);
      const B = Math.max(200, q), j = Math.max(100, ae);
      v.current = B, b.current = j, p(B), L(j);
    }, A = () => {
      document.removeEventListener("mousemove", R), document.removeEventListener("mouseup", A);
      const S = v.current, D = b.current;
      (S != null || D != null) && (r("resize", {
        ...S != null ? { width: Math.round(S) + "px" } : {},
        ...D != null ? { height: Math.round(D) + "px" } : {}
      }), v.current = null, b.current = null, p(null), L(null)), N.current = null;
    };
    document.addEventListener("mousemove", R), document.addEventListener("mouseup", A);
  }, [r]), P = {
    width: m != null ? m + "px" : i,
    ..._ != null ? { height: _ + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, h = n + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlWindow",
      style: P,
      ref: f,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": h
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: h }, c), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: k,
        title: l["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Y, { control: s })),
    a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, a.map((E, g) => /* @__PURE__ */ e.createElement(Y, { key: g, control: E }))),
    d && Sn.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (g) => O(E, g)
      }
    ))
  );
}, { useCallback: Tn, useEffect: Rn } = e, Ln = {
  "js.drawer.close": "Close"
}, Dn = ({ controlId: n }) => {
  const t = K(), r = le(), l = oe(Ln), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", d = t.title ?? null, s = t.child, a = Tn(() => {
    r("close");
  }, [r]);
  Rn(() => {
    if (!c) return;
    const p = (_) => {
      _.key === "Escape" && a();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, a]);
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: n, className: m, "aria-hidden": !c }, d !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, d), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: a,
      title: l["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(Y, { control: s })));
}, { useCallback: Qe, useEffect: xn, useState: In } = e, jn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, d = t.duration ?? 5e3, s = t.visible === !0, [a, m] = In(!1), p = Qe(() => {
    m(!0), setTimeout(() => {
      r("dismiss"), m(!1);
    }, 200);
  }, [r]), _ = Qe(() => {
    o && r(o.commandName), p();
  }, [r, o, p]);
  return xn(() => {
    if (!s || d === 0) return;
    const L = setTimeout(p, d);
    return () => clearTimeout(L);
  }, [s, d, p]), !s && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlSnackbar tlSnackbar--${i}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: _ }, o.label)
  );
}, { useCallback: Pe, useEffect: Me, useRef: Pn, useState: Je } = e, Mn = ({ controlId: n }) => {
  const t = K(), r = le(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Pn(null), [d, s] = Je({ top: 0, left: 0 }), [a, m] = Je(0), p = i.filter((b) => b.type === "item" && !b.disabled);
  Me(() => {
    var h, E;
    if (!l || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const f = b.getBoundingClientRect(), N = ((h = o.current) == null ? void 0 : h.offsetHeight) ?? 200, k = ((E = o.current) == null ? void 0 : E.offsetWidth) ?? 200;
    let O = f.bottom + 4, P = f.left;
    O + N > window.innerHeight && (O = f.top - N - 4), P + k > window.innerWidth && (P = f.right - k), s({ top: O, left: P }), m(0);
  }, [l, c]);
  const _ = Pe(() => {
    r("close");
  }, [r]), L = Pe((b) => {
    r("selectItem", { itemId: b });
  }, [r]);
  Me(() => {
    if (!l) return;
    const b = (f) => {
      o.current && !o.current.contains(f.target) && _();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [l, _]);
  const v = Pe((b) => {
    if (b.key === "Escape") {
      _();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), m((f) => (f + 1) % p.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), m((f) => (f - 1 + p.length) % p.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const f = p[a];
      f && L(f.id);
    }
  }, [_, L, p, a]);
  return Me(() => {
    l && o.current && o.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: d.top, left: d.left },
      onKeyDown: v
    },
    i.map((b, f) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const k = p.indexOf(b) === a;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => L(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, An = ({ controlId: n }) => {
  const t = K(), r = t.header, l = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, On = () => {
  const t = K().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, $n = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, et = 50, Bn = () => {
  const n = K(), t = le(), r = oe($n), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, d = n.selectionMode ?? "single", s = n.selectedCount ?? 0, a = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [l]
  ), _ = d === "multi", L = 40, v = 20, b = e.useRef(null), f = e.useRef(null), N = e.useRef(null), [k, O] = e.useState({}), P = e.useRef(null), h = e.useRef(!1), E = e.useRef(null), [g, X] = e.useState(null), [T, R] = e.useState(null);
  e.useEffect(() => {
    P.current || O({});
  }, [l]);
  const A = e.useCallback((w) => k[w.name] ?? w.width, [k]), S = e.useMemo(() => {
    const w = [];
    let x = _ && a > 0 ? L : 0;
    for (let U = 0; U < a && U < l.length; U++)
      w.push(x), x += A(l[U]);
    return w;
  }, [l, a, _, L, A]), D = c * o, V = e.useCallback((w, x, U) => {
    U.preventDefault(), U.stopPropagation(), P.current = { column: w, startX: U.clientX, startWidth: x };
    const te = (y) => {
      const I = P.current;
      if (!I) return;
      const F = Math.max(et, I.startWidth + (y.clientX - I.startX));
      O((J) => ({ ...J, [I.column]: F }));
    }, ne = (y) => {
      document.removeEventListener("mousemove", te), document.removeEventListener("mouseup", ne);
      const I = P.current;
      if (I) {
        const F = Math.max(et, I.startWidth + (y.clientX - I.startX));
        t("columnResize", { column: I.column, width: F }), P.current = null, h.current = !0, requestAnimationFrame(() => {
          h.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", te), document.addEventListener("mouseup", ne);
  }, [t]), ee = e.useCallback(() => {
    b.current && f.current && (b.current.scrollLeft = f.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const w = f.current;
      if (!w) return;
      const x = w.scrollTop, U = Math.ceil(w.clientHeight / o), te = Math.floor(x / o);
      t("scroll", { start: te, count: U });
    }, 80);
  }, [t, o]), q = e.useCallback((w, x, U) => {
    if (h.current) return;
    let te;
    !x || x === "desc" ? te = "asc" : te = "desc";
    const ne = U.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: te, mode: ne });
  }, [t]), ae = e.useCallback((w, x) => {
    E.current = w, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", w);
  }, []), B = e.useCallback((w, x) => {
    if (!E.current || E.current === w) {
      X(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), te = x.clientX < U.left + U.width / 2 ? "left" : "right";
    X({ column: w, side: te });
  }, []), j = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const x = E.current;
    if (!x || !g) {
      E.current = null, X(null);
      return;
    }
    let U = l.findIndex((ne) => ne.name === g.column);
    if (U < 0) {
      E.current = null, X(null);
      return;
    }
    const te = l.findIndex((ne) => ne.name === x);
    g.side === "right" && U++, te < U && U--, t("columnReorder", { column: x, targetIndex: U }), E.current = null, X(null);
  }, [l, g, t]), Z = e.useCallback(() => {
    E.current = null, X(null);
  }, []), u = e.useCallback((w, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), C = e.useCallback((w, x) => {
    x.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), $ = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), M = e.useCallback((w, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: w, expanded: x });
  }, [t]), W = e.useCallback((w, x) => {
    x.preventDefault(), R({ x: x.clientX, y: x.clientY, colIdx: w });
  }, []), G = e.useCallback(() => {
    T && (t("setFrozenColumnCount", { count: T.colIdx + 1 }), R(null));
  }, [T, t]), Q = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), R(null);
  }, [t]);
  e.useEffect(() => {
    if (!T) return;
    const w = () => R(null), x = (U) => {
      U.key === "Escape" && R(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", x), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", x);
    };
  }, [T]);
  const se = l.reduce((w, x) => w + A(x), 0) + (_ ? L : 0), re = s === c && c > 0, fe = s > 0 && s < c, xe = e.useCallback((w) => {
    w && (w.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!E.current) return;
        w.preventDefault();
        const x = f.current, U = b.current;
        if (!x) return;
        const te = x.getBoundingClientRect(), ne = 40, y = 8;
        w.clientX < te.left + ne ? x.scrollLeft = Math.max(0, x.scrollLeft - y) : w.clientX > te.right - ne && (x.scrollLeft += y), U && (U.scrollLeft = x.scrollLeft);
      },
      onDrop: j
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: se } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: L,
          minWidth: L,
          ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          E.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== E.current && X({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: xe,
          className: "tlTableView__checkbox",
          checked: re,
          onChange: $
        }
      )
    ), l.map((w, x) => {
      const U = A(w), te = x === l.length - 1;
      let ne = "tlTableView__headerCell";
      w.sortable && (ne += " tlTableView__headerCell--sortable"), g && g.column === w.name && (ne += " tlTableView__headerCell--dragOver-" + g.side);
      const y = x < a, I = x === a - 1;
      return y && (ne += " tlTableView__headerCell--frozen"), I && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ne,
          style: {
            ...te && !y ? { flex: "1 0 auto", minWidth: U } : { width: U, minWidth: U },
            position: y ? "sticky" : "relative",
            ...y ? { left: S[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (F) => q(w.name, w.sortDirection, F) : void 0,
          onContextMenu: (F) => W(x, F),
          onDragStart: (F) => ae(w.name, F),
          onDragOver: (F) => B(w.name, F),
          onDrop: j,
          onDragEnd: Z
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", p > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (F) => V(w.name, U, F)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (E.current && l.length > 0) {
            const x = l[l.length - 1];
            x.name !== E.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", X({ column: x.name, side: "right" }));
          }
        },
        onDrop: j
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: f,
        className: "tlTableView__body",
        onScroll: ee
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: D, position: "relative", minWidth: se } }, i.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            minWidth: se,
            width: "100%"
          },
          onClick: (x) => u(w.index, x)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: L,
              minWidth: L,
              ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (x) => x.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (x) => C(w.index, x),
              tabIndex: -1
            }
          )
        ),
        l.map((x, U) => {
          const te = A(x), ne = U === l.length - 1, y = U < a, I = U === a - 1;
          let F = "tlTableView__cell";
          y && (F += " tlTableView__cell--frozen"), I && (F += " tlTableView__cell--frozenLast");
          const J = m && U === 0, ve = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: F,
              style: {
                ...ne && !y ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...y ? { position: "sticky", left: S[U], zIndex: 2 } : {}
              }
            },
            J ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ve * v } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pt) => M(w.index, !w.expanded, pt)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: w.cells[x.name] })) : /* @__PURE__ */ e.createElement(Y, { control: w.cells[x.name] })
          );
        })
      )))
    ),
    T && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: T.y, left: T.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      T.colIdx + 1 !== a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: G }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      a > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Q }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Fn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ct = e.createContext(Fn), { useMemo: Hn, useRef: zn, useState: Un, useEffect: Wn } = e, Vn = 320, Kn = ({ controlId: n }) => {
  const t = K(), r = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, d = zn(null), [s, a] = Un(
    l === "top" ? "top" : "side"
  );
  Wn(() => {
    if (l !== "auto") {
      a(l);
      return;
    }
    const v = d.current;
    if (!v) return;
    const b = new ResizeObserver((f) => {
      for (const N of f) {
        const O = N.contentRect.width / r;
        a(O < Vn ? "top" : "side");
      }
    });
    return b.observe(v), () => b.disconnect();
  }, [l, r]);
  const m = Hn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), _ = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, L = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: d }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: L, style: _, ref: d }, i.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v }))));
}, { useCallback: Yn } = e, Gn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Xn = ({ controlId: n }) => {
  const t = K(), r = le(), l = oe(Gn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, d = t.collapsed === !0, s = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, _ = Yn(() => {
    r("toggleCollapse");
  }, [r]), L = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    a ? "tlFormGroup--fullLine" : "",
    d ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: L }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
      "aria-expanded": !d,
      title: d ? l["js.formGroup.expand"] : l["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v }))));
}, { useContext: qn, useState: Zn, useCallback: Qn } = e, Jn = ({ controlId: n }) => {
  const t = K(), r = qn(ct), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.helpText, d = t.dirty === !0, s = t.labelPosition ?? r.resolvedLabelPosition, a = t.fullLine === !0, m = t.visible !== !1, p = t.field, _ = r.readOnly, [L, v] = Zn(!1), b = Qn(() => v((k) => !k), []);
  if (!m) return null;
  const f = i != null, N = [
    "tlFormField",
    `tlFormField--${s}`,
    _ ? "tlFormField--readonly" : "",
    a ? "tlFormField--fullLine" : "",
    f ? "tlFormField--error" : "",
    d ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: N }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !_ && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), d && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), o && !_ && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: b,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: p })), !_ && f && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !_ && o && L && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, o));
}, el = () => {
  const n = K(), t = le(), r = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, d = n.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, a = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return d ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, a) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, a);
}, tl = 20, nl = () => {
  const n = K(), t = le(), r = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, d = n.dropIndicatorPosition ?? null, [s, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((h, E) => {
    t(E ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, E) => {
    t("select", {
      nodeId: h,
      ctrlKey: E.ctrlKey || E.metaKey,
      shiftKey: E.shiftKey
    });
  }, [t]), L = e.useCallback((h, E) => {
    E.preventDefault(), t("contextMenu", { nodeId: h, x: E.clientX, y: E.clientY });
  }, [t]), v = e.useRef(null), b = e.useCallback((h, E) => {
    const g = E.getBoundingClientRect(), X = h.clientY - g.top, T = g.height / 3;
    return X < T ? "above" : X > T * 2 ? "below" : "within";
  }, []), f = e.useCallback((h, E) => {
    E.dataTransfer.effectAllowed = "move", E.dataTransfer.setData("text/plain", h);
  }, []), N = e.useCallback((h, E) => {
    E.preventDefault(), E.dataTransfer.dropEffect = "move";
    const g = b(E, E.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: g }), v.current = null;
    }, 50);
  }, [t, b]), k = e.useCallback((h, E) => {
    E.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const g = b(E, E.currentTarget);
    t("drop", { nodeId: h, position: g });
  }, [t, b]), O = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((h) => {
    if (r.length === 0) return;
    let E = s;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), E = Math.min(s + 1, r.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), E = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const g = r[s];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (E = s + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), s >= 0 && s < r.length) {
          const g = r[s];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const X = g.depth;
            for (let T = s - 1; T >= 0; T--)
              if (r[T].depth < X) {
                E = T;
                break;
              }
          }
        }
        break;
      case "Enter":
        h.preventDefault(), s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), l === "multi" && s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), E = 0;
        break;
      case "End":
        h.preventDefault(), E = r.length - 1;
        break;
      default:
        return;
    }
    E !== s && a(E);
  }, [s, r, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    r.map((h, E) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: h.id,
        role: "treeitem",
        "aria-expanded": h.expandable ? h.expanded : void 0,
        "aria-selected": h.selected,
        "aria-level": h.depth + 1,
        className: [
          "tlTreeView__node",
          h.selected ? "tlTreeView__node--selected" : "",
          E === s ? "tlTreeView__node--focused" : "",
          o === h.id && d === "above" ? "tlTreeView__node--drop-above" : "",
          o === h.id && d === "within" ? "tlTreeView__node--drop-within" : "",
          o === h.id && d === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * tl },
        draggable: c,
        onClick: (g) => _(h.id, g),
        onContextMenu: (g) => L(h.id, g),
        onDragStart: (g) => f(h.id, g),
        onDragOver: i ? (g) => N(h.id, g) : void 0,
        onDrop: i ? (g) => k(h.id, g) : void 0,
        onDragEnd: O
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), p(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: h.content }))
    ))
  );
};
var Ae = { exports: {} }, ce = {}, Oe = { exports: {} }, z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var tt;
function ll() {
  if (tt) return z;
  tt = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), d = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), _ = Symbol.iterator;
  function L(u) {
    return u === null || typeof u != "object" ? null : (u = _ && u[_] || u["@@iterator"], typeof u == "function" ? u : null);
  }
  var v = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, b = Object.assign, f = {};
  function N(u, C, $) {
    this.props = u, this.context = C, this.refs = f, this.updater = $ || v;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(u, C) {
    if (typeof u != "object" && typeof u != "function" && u != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, u, C, "setState");
  }, N.prototype.forceUpdate = function(u) {
    this.updater.enqueueForceUpdate(this, u, "forceUpdate");
  };
  function k() {
  }
  k.prototype = N.prototype;
  function O(u, C, $) {
    this.props = u, this.context = C, this.refs = f, this.updater = $ || v;
  }
  var P = O.prototype = new k();
  P.constructor = O, b(P, N.prototype), P.isPureReactComponent = !0;
  var h = Array.isArray;
  function E() {
  }
  var g = { H: null, A: null, T: null, S: null }, X = Object.prototype.hasOwnProperty;
  function T(u, C, $) {
    var M = $.ref;
    return {
      $$typeof: n,
      type: u,
      key: C,
      ref: M !== void 0 ? M : null,
      props: $
    };
  }
  function R(u, C) {
    return T(u.type, C, u.props);
  }
  function A(u) {
    return typeof u == "object" && u !== null && u.$$typeof === n;
  }
  function S(u) {
    var C = { "=": "=0", ":": "=2" };
    return "$" + u.replace(/[=:]/g, function($) {
      return C[$];
    });
  }
  var D = /\/+/g;
  function V(u, C) {
    return typeof u == "object" && u !== null && u.key != null ? S("" + u.key) : C.toString(36);
  }
  function ee(u) {
    switch (u.status) {
      case "fulfilled":
        return u.value;
      case "rejected":
        throw u.reason;
      default:
        switch (typeof u.status == "string" ? u.then(E, E) : (u.status = "pending", u.then(
          function(C) {
            u.status === "pending" && (u.status = "fulfilled", u.value = C);
          },
          function(C) {
            u.status === "pending" && (u.status = "rejected", u.reason = C);
          }
        )), u.status) {
          case "fulfilled":
            return u.value;
          case "rejected":
            throw u.reason;
        }
    }
    throw u;
  }
  function q(u, C, $, M, W) {
    var G = typeof u;
    (G === "undefined" || G === "boolean") && (u = null);
    var Q = !1;
    if (u === null) Q = !0;
    else
      switch (G) {
        case "bigint":
        case "string":
        case "number":
          Q = !0;
          break;
        case "object":
          switch (u.$$typeof) {
            case n:
            case t:
              Q = !0;
              break;
            case m:
              return Q = u._init, q(
                Q(u._payload),
                C,
                $,
                M,
                W
              );
          }
      }
    if (Q)
      return W = W(u), Q = M === "" ? "." + V(u, 0) : M, h(W) ? ($ = "", Q != null && ($ = Q.replace(D, "$&/") + "/"), q(W, C, $, "", function(fe) {
        return fe;
      })) : W != null && (A(W) && (W = R(
        W,
        $ + (W.key == null || u && u.key === W.key ? "" : ("" + W.key).replace(
          D,
          "$&/"
        ) + "/") + Q
      )), C.push(W)), 1;
    Q = 0;
    var se = M === "" ? "." : M + ":";
    if (h(u))
      for (var re = 0; re < u.length; re++)
        M = u[re], G = se + V(M, re), Q += q(
          M,
          C,
          $,
          G,
          W
        );
    else if (re = L(u), typeof re == "function")
      for (u = re.call(u), re = 0; !(M = u.next()).done; )
        M = M.value, G = se + V(M, re++), Q += q(
          M,
          C,
          $,
          G,
          W
        );
    else if (G === "object") {
      if (typeof u.then == "function")
        return q(
          ee(u),
          C,
          $,
          M,
          W
        );
      throw C = String(u), Error(
        "Objects are not valid as a React child (found: " + (C === "[object Object]" ? "object with keys {" + Object.keys(u).join(", ") + "}" : C) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function ae(u, C, $) {
    if (u == null) return u;
    var M = [], W = 0;
    return q(u, M, "", "", function(G) {
      return C.call($, G, W++);
    }), M;
  }
  function B(u) {
    if (u._status === -1) {
      var C = u._result;
      C = C(), C.then(
        function($) {
          (u._status === 0 || u._status === -1) && (u._status = 1, u._result = $);
        },
        function($) {
          (u._status === 0 || u._status === -1) && (u._status = 2, u._result = $);
        }
      ), u._status === -1 && (u._status = 0, u._result = C);
    }
    if (u._status === 1) return u._result.default;
    throw u._result;
  }
  var j = typeof reportError == "function" ? reportError : function(u) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var C = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof u == "object" && u !== null && typeof u.message == "string" ? String(u.message) : String(u),
        error: u
      });
      if (!window.dispatchEvent(C)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", u);
      return;
    }
    console.error(u);
  }, Z = {
    map: ae,
    forEach: function(u, C, $) {
      ae(
        u,
        function() {
          C.apply(this, arguments);
        },
        $
      );
    },
    count: function(u) {
      var C = 0;
      return ae(u, function() {
        C++;
      }), C;
    },
    toArray: function(u) {
      return ae(u, function(C) {
        return C;
      }) || [];
    },
    only: function(u) {
      if (!A(u))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return u;
    }
  };
  return z.Activity = p, z.Children = Z, z.Component = N, z.Fragment = r, z.Profiler = c, z.PureComponent = O, z.StrictMode = l, z.Suspense = s, z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(u) {
      return g.H.useMemoCache(u);
    }
  }, z.cache = function(u) {
    return function() {
      return u.apply(null, arguments);
    };
  }, z.cacheSignal = function() {
    return null;
  }, z.cloneElement = function(u, C, $) {
    if (u == null)
      throw Error(
        "The argument must be a React element, but you passed " + u + "."
      );
    var M = b({}, u.props), W = u.key;
    if (C != null)
      for (G in C.key !== void 0 && (W = "" + C.key), C)
        !X.call(C, G) || G === "key" || G === "__self" || G === "__source" || G === "ref" && C.ref === void 0 || (M[G] = C[G]);
    var G = arguments.length - 2;
    if (G === 1) M.children = $;
    else if (1 < G) {
      for (var Q = Array(G), se = 0; se < G; se++)
        Q[se] = arguments[se + 2];
      M.children = Q;
    }
    return T(u.type, W, M);
  }, z.createContext = function(u) {
    return u = {
      $$typeof: o,
      _currentValue: u,
      _currentValue2: u,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, u.Provider = u, u.Consumer = {
      $$typeof: i,
      _context: u
    }, u;
  }, z.createElement = function(u, C, $) {
    var M, W = {}, G = null;
    if (C != null)
      for (M in C.key !== void 0 && (G = "" + C.key), C)
        X.call(C, M) && M !== "key" && M !== "__self" && M !== "__source" && (W[M] = C[M]);
    var Q = arguments.length - 2;
    if (Q === 1) W.children = $;
    else if (1 < Q) {
      for (var se = Array(Q), re = 0; re < Q; re++)
        se[re] = arguments[re + 2];
      W.children = se;
    }
    if (u && u.defaultProps)
      for (M in Q = u.defaultProps, Q)
        W[M] === void 0 && (W[M] = Q[M]);
    return T(u, G, W);
  }, z.createRef = function() {
    return { current: null };
  }, z.forwardRef = function(u) {
    return { $$typeof: d, render: u };
  }, z.isValidElement = A, z.lazy = function(u) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: u },
      _init: B
    };
  }, z.memo = function(u, C) {
    return {
      $$typeof: a,
      type: u,
      compare: C === void 0 ? null : C
    };
  }, z.startTransition = function(u) {
    var C = g.T, $ = {};
    g.T = $;
    try {
      var M = u(), W = g.S;
      W !== null && W($, M), typeof M == "object" && M !== null && typeof M.then == "function" && M.then(E, j);
    } catch (G) {
      j(G);
    } finally {
      C !== null && $.types !== null && (C.types = $.types), g.T = C;
    }
  }, z.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, z.use = function(u) {
    return g.H.use(u);
  }, z.useActionState = function(u, C, $) {
    return g.H.useActionState(u, C, $);
  }, z.useCallback = function(u, C) {
    return g.H.useCallback(u, C);
  }, z.useContext = function(u) {
    return g.H.useContext(u);
  }, z.useDebugValue = function() {
  }, z.useDeferredValue = function(u, C) {
    return g.H.useDeferredValue(u, C);
  }, z.useEffect = function(u, C) {
    return g.H.useEffect(u, C);
  }, z.useEffectEvent = function(u) {
    return g.H.useEffectEvent(u);
  }, z.useId = function() {
    return g.H.useId();
  }, z.useImperativeHandle = function(u, C, $) {
    return g.H.useImperativeHandle(u, C, $);
  }, z.useInsertionEffect = function(u, C) {
    return g.H.useInsertionEffect(u, C);
  }, z.useLayoutEffect = function(u, C) {
    return g.H.useLayoutEffect(u, C);
  }, z.useMemo = function(u, C) {
    return g.H.useMemo(u, C);
  }, z.useOptimistic = function(u, C) {
    return g.H.useOptimistic(u, C);
  }, z.useReducer = function(u, C, $) {
    return g.H.useReducer(u, C, $);
  }, z.useRef = function(u) {
    return g.H.useRef(u);
  }, z.useState = function(u) {
    return g.H.useState(u);
  }, z.useSyncExternalStore = function(u, C, $) {
    return g.H.useSyncExternalStore(
      u,
      C,
      $
    );
  }, z.useTransition = function() {
    return g.H.useTransition();
  }, z.version = "19.2.4", z;
}
var nt;
function al() {
  return nt || (nt = 1, Oe.exports = ll()), Oe.exports;
}
/**
 * @license React
 * react-dom.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var lt;
function rl() {
  if (lt) return ce;
  lt = 1;
  var n = al();
  function t(s) {
    var a = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function r() {
  }
  var l = {
    d: {
      f: r,
      r: function() {
        throw Error(t(522));
      },
      D: r,
      C: r,
      L: r,
      m: r,
      X: r,
      S: r,
      M: r
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function i(s, a, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: s,
      containerInfo: a,
      implementation: m
    };
  }
  var o = n.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function d(s, a) {
    if (s === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, ce.createPortal = function(s, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return i(s, a, null, m);
  }, ce.flushSync = function(s) {
    var a = o.T, m = l.p;
    try {
      if (o.T = null, l.p = 2, s) return s();
    } finally {
      o.T = a, l.p = m, l.d.f();
    }
  }, ce.preconnect = function(s, a) {
    typeof s == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, l.d.C(s, a));
  }, ce.prefetchDNS = function(s) {
    typeof s == "string" && l.d.D(s);
  }, ce.preinit = function(s, a) {
    if (typeof s == "string" && a && typeof a.as == "string") {
      var m = a.as, p = d(m, a.crossOrigin), _ = typeof a.integrity == "string" ? a.integrity : void 0, L = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? l.d.S(
        s,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: _,
          fetchPriority: L
        }
      ) : m === "script" && l.d.X(s, {
        crossOrigin: p,
        integrity: _,
        fetchPriority: L,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, ce.preinitModule = function(s, a) {
    if (typeof s == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = d(
            a.as,
            a.crossOrigin
          );
          l.d.M(s, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && l.d.M(s);
  }, ce.preload = function(s, a) {
    if (typeof s == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = d(m, a.crossOrigin);
      l.d.L(s, m, {
        crossOrigin: p,
        integrity: typeof a.integrity == "string" ? a.integrity : void 0,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0,
        type: typeof a.type == "string" ? a.type : void 0,
        fetchPriority: typeof a.fetchPriority == "string" ? a.fetchPriority : void 0,
        referrerPolicy: typeof a.referrerPolicy == "string" ? a.referrerPolicy : void 0,
        imageSrcSet: typeof a.imageSrcSet == "string" ? a.imageSrcSet : void 0,
        imageSizes: typeof a.imageSizes == "string" ? a.imageSizes : void 0,
        media: typeof a.media == "string" ? a.media : void 0
      });
    }
  }, ce.preloadModule = function(s, a) {
    if (typeof s == "string")
      if (a) {
        var m = d(a.as, a.crossOrigin);
        l.d.m(s, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else l.d.m(s);
  }, ce.requestFormReset = function(s) {
    l.d.r(s);
  }, ce.unstable_batchedUpdates = function(s, a) {
    return s(a);
  }, ce.useFormState = function(s, a, m) {
    return o.H.useFormState(s, a, m);
  }, ce.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var at;
function ol() {
  if (at) return Ae.exports;
  at = 1;
  function n() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(n);
      } catch (t) {
        console.error(t);
      }
  }
  return n(), Ae.exports = rl(), Ae.exports;
}
var sl = ol();
const { useState: me, useCallback: ie, useRef: Ee, useEffect: he, useMemo: ze } = e;
function Ke({ image: n }) {
  if (!n) return null;
  if (n.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = n.startsWith("css:") ? n.substring(4) : n.startsWith("colored:") ? n.substring(8) : n;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function cl({
  option: n,
  removable: t,
  onRemove: r,
  removeLabel: l,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: d,
  onDragEnd: s,
  dragClassName: a
}) {
  const m = ie(
    (p) => {
      p.stopPropagation(), r(n.value);
    },
    [r, n.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
      draggable: c || void 0,
      onDragStart: i,
      onDragOver: o,
      onDrop: d,
      onDragEnd: s
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Ke, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, n.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": l
      },
      "×"
    )
  );
}
function il({
  option: n,
  highlighted: t,
  searchTerm: r,
  onSelect: l,
  onMouseEnter: c,
  id: i
}) {
  const o = ie(() => l(n.value), [l, n.value]), d = ze(() => {
    if (!r) return n.label;
    const s = n.label.toLowerCase().indexOf(r.toLowerCase());
    return s < 0 ? n.label : /* @__PURE__ */ e.createElement(e.Fragment, null, n.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, n.label.substring(s, s + r.length)), n.label.substring(s + r.length));
  }, [n.label, r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: i,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: o,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(Ke, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, d)
  );
}
const ul = ({ controlId: n, state: t }) => {
  const r = le(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, d = t.disabled === !0, s = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", _ = i && c && !d && s, L = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), v = L["js.dropdownSelect.nothingFound"], b = ie(
    (y) => L["js.dropdownSelect.removeChip"].replace("{0}", y),
    [L]
  ), [f, N] = me(!1), [k, O] = me(""), [P, h] = me(-1), [E, g] = me(!1), [X, T] = me({}), [R, A] = me(null), [S, D] = me(null), [V, ee] = me(null), q = Ee(null), ae = Ee(null), B = Ee(null), j = Ee(l);
  j.current = l;
  const Z = Ee(-1), u = ze(
    () => new Set(l.map((y) => y.value)),
    [l]
  ), C = ze(() => {
    let y = m.filter((I) => !u.has(I.value));
    if (k) {
      const I = k.toLowerCase();
      y = y.filter((F) => F.label.toLowerCase().includes(I));
    }
    return y;
  }, [m, u, k]);
  he(() => {
    k && C.length === 1 ? h(0) : h(-1);
  }, [C.length, k]), he(() => {
    f && a && ae.current && ae.current.focus();
  }, [f, a, l]), he(() => {
    var F, J;
    if (Z.current < 0) return;
    const y = Z.current;
    Z.current = -1;
    const I = (F = q.current) == null ? void 0 : F.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    I && I.length > 0 ? I[Math.min(y, I.length - 1)].focus() : (J = q.current) == null || J.focus();
  }, [l]), he(() => {
    if (!f) return;
    const y = (I) => {
      q.current && !q.current.contains(I.target) && B.current && !B.current.contains(I.target) && (N(!1), O(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [f]), he(() => {
    if (!f || !q.current) return;
    const y = q.current.getBoundingClientRect(), I = window.innerHeight - y.bottom, J = I < 300 && y.top > I;
    T({
      left: y.left,
      width: y.width,
      ...J ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [f]);
  const $ = ie(async () => {
    if (!(d || !s) && (N(!0), O(""), h(-1), g(!1), !a))
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
  }, [d, s, a, r]), M = ie(() => {
    var y;
    N(!1), O(""), h(-1), (y = q.current) == null || y.focus();
  }, []), W = ie(
    (y) => {
      let I;
      if (c) {
        const F = m.find((J) => J.value === y);
        if (F)
          I = [...j.current, F];
        else
          return;
      } else {
        const F = m.find((J) => J.value === y);
        if (F)
          I = [F];
        else
          return;
      }
      j.current = I, r("valueChanged", { value: I.map((F) => F.value) }), c ? (O(""), h(-1)) : M();
    },
    [c, m, r, M]
  ), G = ie(
    (y) => {
      Z.current = j.current.findIndex((F) => F.value === y);
      const I = j.current.filter((F) => F.value !== y);
      j.current = I, r("valueChanged", { value: I.map((F) => F.value) });
    },
    [r]
  ), Q = ie(
    (y) => {
      y.stopPropagation(), r("valueChanged", { value: [] }), M();
    },
    [r, M]
  ), se = ie((y) => {
    O(y.target.value);
  }, []), re = ie(
    (y) => {
      if (!f) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), $();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), h(
            (I) => I < C.length - 1 ? I + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), h(
            (I) => I > 0 ? I - 1 : C.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), P >= 0 && P < C.length && W(C[P].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), M();
          break;
        case "Tab":
          M();
          break;
        case "Backspace":
          k === "" && c && l.length > 0 && G(l[l.length - 1].value);
          break;
      }
    },
    [
      f,
      $,
      M,
      C,
      P,
      W,
      k,
      c,
      l,
      G
    ]
  ), fe = ie(
    async (y) => {
      y.preventDefault(), g(!1);
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
    },
    [r]
  ), xe = ie(
    (y, I) => {
      A(y), I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), w = ie(
    (y, I) => {
      if (I.preventDefault(), I.dataTransfer.dropEffect = "move", R === null || R === y) {
        D(null), ee(null);
        return;
      }
      const F = I.currentTarget.getBoundingClientRect(), J = F.left + F.width / 2, ve = I.clientX < J ? "before" : "after";
      D(y), ee(ve);
    },
    [R]
  ), x = ie(
    (y) => {
      if (y.preventDefault(), R === null || S === null || V === null || R === S) return;
      const I = [...j.current], [F] = I.splice(R, 1);
      let J = S;
      R < S ? J = V === "before" ? J - 1 : J : J = V === "before" ? J : J + 1, I.splice(J, 0, F), j.current = I, r("valueChanged", { value: I.map((ve) => ve.value) }), A(null), D(null), ee(null);
    },
    [R, S, V, r]
  ), U = ie(() => {
    A(null), D(null), ee(null);
  }, []);
  if (he(() => {
    if (P < 0 || !B.current) return;
    const y = B.current.querySelector(
      `[id="${n}-opt-${P}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [P, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ke, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const te = !o && l.length > 0 && !d, ne = f ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: X
    },
    (a || E) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: ae,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
        onChange: se,
        onKeyDown: re,
        placeholder: L["js.dropdownSelect.filterPlaceholder"],
        "aria-label": L["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": P >= 0 ? `${n}-opt-${P}` : void 0,
        "aria-controls": `${n}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${n}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !a && !E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      E && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: fe }, L["js.dropdownSelect.error"])),
      a && C.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, v),
      a && C.map((y, I) => /* @__PURE__ */ e.createElement(
        il,
        {
          key: y.value,
          id: `${n}-opt-${I}`,
          option: y,
          highlighted: I === P,
          searchTerm: k,
          onSelect: W,
          onMouseEnter: () => h(I)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: q,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (d ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${n}-listbox` : void 0,
      tabIndex: d ? -1 : 0,
      onClick: f ? void 0 : $,
      onKeyDown: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((y, I) => {
      let F = "";
      return R === I ? F = "tlDropdownSelect__chip--dragging" : S === I && V === "before" ? F = "tlDropdownSelect__chip--dropBefore" : S === I && V === "after" && (F = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        cl,
        {
          key: y.value,
          option: y,
          removable: !d && (c || !o),
          onRemove: G,
          removeLabel: b(y.label),
          draggable: _,
          onDragStart: _ ? (J) => xe(I, J) : void 0,
          onDragOver: _ ? (J) => w(I, J) : void 0,
          onDrop: _ ? x : void 0,
          onDragEnd: _ ? U : void 0,
          dragClassName: _ ? F : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, te && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": L["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), ne && sl.createPortal(ne, document.body));
}, { useCallback: $e, useRef: dl } = e, it = "application/x-tl-color", ml = ({
  colors: n,
  columns: t,
  onSelect: r,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = dl(null), d = $e(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = $e((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = $e(
    (m) => (p) => {
      p.preventDefault();
      const _ = p.dataTransfer.getData(it);
      _ ? i(m, _) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
    },
    [c, i]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    n.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => l(m) : void 0,
        onDragStart: m != null ? d(p) : void 0,
        onDragOver: s,
        onDrop: a(p)
      }
    ))
  );
};
function ut(n) {
  return Math.max(0, Math.min(255, Math.round(n)));
}
function Ue(n) {
  return /^#[0-9a-fA-F]{6}$/.test(n);
}
function dt(n) {
  if (!Ue(n)) return [0, 0, 0];
  const t = parseInt(n.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function mt(n, t, r) {
  const l = (c) => ut(c).toString(16).padStart(2, "0");
  return "#" + l(n) + l(t) + l(r);
}
function pl(n, t, r) {
  const l = n / 255, c = t / 255, i = r / 255, o = Math.max(l, c, i), d = Math.min(l, c, i), s = o - d;
  let a = 0;
  s !== 0 && (o === l ? a = (c - i) / s % 6 : o === c ? a = (i - l) / s + 2 : a = (l - c) / s + 4, a *= 60, a < 0 && (a += 360));
  const m = o === 0 ? 0 : s / o;
  return [a, m, o];
}
function fl(n, t, r) {
  const l = r * t, c = l * (1 - Math.abs(n / 60 % 2 - 1)), i = r - l;
  let o = 0, d = 0, s = 0;
  return n < 60 ? (o = l, d = c, s = 0) : n < 120 ? (o = c, d = l, s = 0) : n < 180 ? (o = 0, d = l, s = c) : n < 240 ? (o = 0, d = c, s = l) : n < 300 ? (o = c, d = 0, s = l) : (o = l, d = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((d + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function hl(n) {
  return pl(...dt(n));
}
function Be(n, t, r) {
  return mt(...fl(n, t, r));
}
const { useCallback: _e, useRef: rt } = e, _l = ({ color: n, onColorChange: t }) => {
  const [r, l, c] = hl(n), i = rt(null), o = rt(null), d = _e(
    (v, b) => {
      var O;
      const f = (O = i.current) == null ? void 0 : O.getBoundingClientRect();
      if (!f) return;
      const N = Math.max(0, Math.min(1, (v - f.left) / f.width)), k = Math.max(0, Math.min(1, 1 - (b - f.top) / f.height));
      t(Be(r, N, k));
    },
    [r, t]
  ), s = _e(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), d(v.clientX, v.clientY);
    },
    [d]
  ), a = _e(
    (v) => {
      v.buttons !== 0 && d(v.clientX, v.clientY);
    },
    [d]
  ), m = _e(
    (v) => {
      var k;
      const b = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!b) return;
      const N = Math.max(0, Math.min(1, (v - b.top) / b.height)) * 360;
      t(Be(N, l, c));
    },
    [l, c, t]
  ), p = _e(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), m(v.clientY);
    },
    [m]
  ), _ = _e(
    (v) => {
      v.buttons !== 0 && m(v.clientY);
    },
    [m]
  ), L = Be(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: L },
      onPointerDown: s,
      onPointerMove: a
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${l * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: _
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${r / 360 * 100}%` }
      }
    )
  ));
};
function bl(n, t) {
  const r = t.toUpperCase();
  return n.some((l) => l != null && l.toUpperCase() === r);
}
const vl = {
  "js.colorInput.paletteTab": "Color Palette",
  "js.colorInput.mixerTab": "Color Mixer",
  "js.colorInput.current": "Current",
  "js.colorInput.new": "New",
  "js.colorInput.red": "Red",
  "js.colorInput.green": "Green",
  "js.colorInput.blue": "Blue",
  "js.colorInput.hex": "Hex",
  "js.colorInput.clear": "Clear",
  "js.colorInput.reset": "Reset",
  "js.colorInput.cancel": "Cancel",
  "js.colorInput.ok": "OK"
}, { useState: we, useCallback: de, useEffect: Fe, useRef: El, useLayoutEffect: gl } = e, Cl = ({
  anchorRef: n,
  currentColor: t,
  palette: r,
  paletteColumns: l,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: d,
  onPaletteChange: s
}) => {
  const [a, m] = we("palette"), [p, _] = we(t), L = El(null), v = oe(vl), [b, f] = we(null);
  gl(() => {
    if (!n.current || !L.current) return;
    const B = n.current.getBoundingClientRect(), j = L.current.getBoundingClientRect();
    let Z = B.bottom + 4, u = B.left;
    Z + j.height > window.innerHeight && (Z = B.top - j.height - 4), u + j.width > window.innerWidth && (u = Math.max(0, B.right - j.width)), f({ top: Z, left: u });
  }, [n]);
  const N = p != null, [k, O, P] = N ? dt(p) : [0, 0, 0], [h, E] = we((p == null ? void 0 : p.toUpperCase()) ?? "");
  Fe(() => {
    E((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Fe(() => {
    const B = (j) => {
      j.key === "Escape" && d();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [d]), Fe(() => {
    const B = (Z) => {
      L.current && !L.current.contains(Z.target) && d();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [d]);
  const g = de(
    (B) => (j) => {
      const Z = parseInt(j.target.value, 10);
      if (isNaN(Z)) return;
      const u = ut(Z);
      _(mt(B === "r" ? u : k, B === "g" ? u : O, B === "b" ? u : P));
    },
    [k, O, P]
  ), X = de(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(it, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), T = de((B) => {
    const j = B.target.value;
    E(j), Ue(j) && _(j);
  }, []), R = de(() => {
    _(null);
  }, []), A = de((B) => {
    _(B);
  }, []), S = de(
    (B) => {
      o(B);
    },
    [o]
  ), D = de(
    (B, j) => {
      const Z = [...r], u = Z[B];
      Z[B] = Z[j], Z[j] = u, s(Z);
    },
    [r, s]
  ), V = de(
    (B, j) => {
      const Z = [...r];
      Z[B] = j, s(Z);
    },
    [r, s]
  ), ee = de(() => {
    s([...c]);
  }, [c, s]), q = de(
    (B) => {
      if (bl(r, B)) return;
      const j = r.indexOf(null);
      if (j < 0) return;
      const Z = [...r];
      Z[j] = B.toUpperCase(), s(Z);
    },
    [r, s]
  ), ae = de(() => {
    p != null && q(p), o(p);
  }, [p, o, q]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: L,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      v["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      v["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      ml,
      {
        colors: r,
        columns: l,
        onSelect: A,
        onConfirm: S,
        onSwap: D,
        onReplace: V
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, v["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(_l, { color: p ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, v["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, v["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: p } : void 0,
        draggable: N,
        onDragStart: N ? X : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? k : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? O : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? P : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !Ue(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, v["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: d }, v["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: ae }, v["js.colorInput.ok"]))
  );
}, yl = { "js.colorInput.chooseColor": "Choose color" }, { useState: wl, useCallback: ke, useRef: kl } = e, Sl = ({ controlId: n, state: t }) => {
  const r = le(), l = oe(yl), [c, i] = wl(!1), o = kl(null), d = t.value, s = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, _ = ke(() => {
    s && i(!0);
  }, [s]), L = ke(
    (f) => {
      i(!1), r("valueChanged", { value: f });
    },
    [r]
  ), v = ke(() => {
    i(!1);
  }, []), b = ke(
    (f) => {
      r("paletteChanged", { palette: f });
    },
    [r]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (d == null ? " tlColorInput__swatch--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: d ?? "",
      "aria-label": l["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Cl,
    {
      anchorRef: o,
      currentColor: d,
      palette: a,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: L,
      onCancel: v,
      onPaletteChange: b
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: n,
      className: "tlColorInput tlColorInput--immutable" + (d == null ? " tlColorInput--noColor" : ""),
      style: d != null ? { backgroundColor: d } : void 0,
      title: d ?? ""
    }
  );
}, { useState: ge, useCallback: pe, useEffect: Se, useRef: ot, useLayoutEffect: Nl, useMemo: Tl } = e, Rl = {
  "js.iconSelect.simpleTab": "Simple",
  "js.iconSelect.advancedTab": "Advanced",
  "js.iconSelect.filterPlaceholder": "Filter icons…",
  "js.iconSelect.noResults": "No icons found",
  "js.iconSelect.loading": "Loading…",
  "js.iconSelect.loadError": "Failed to load. Click to retry.",
  "js.iconSelect.classLabel": "Class",
  "js.iconSelect.previewLabel": "Preview",
  "js.iconSelect.cancel": "Cancel",
  "js.iconSelect.ok": "OK",
  "js.iconSelect.clear": "Clear icon",
  "js.iconSelect.clearFilter": "Clear filter"
};
function De({ encoded: n, className: t }) {
  if (n.startsWith("css:")) {
    const r = n.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  if (n.startsWith("colored:")) {
    const r = n.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  return n.startsWith("/") || n.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
}
const Ll = ({
  anchorRef: n,
  currentValue: t,
  icons: r,
  iconsLoaded: l,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const d = oe(Rl), [s, a] = ge("simple"), [m, p] = ge(""), [_, L] = ge(t ?? ""), [v, b] = ge(!1), [f, N] = ge(null), k = ot(null), O = ot(null);
  Nl(() => {
    if (!n.current || !k.current) return;
    const S = n.current.getBoundingClientRect(), D = k.current.getBoundingClientRect();
    let V = S.bottom + 4, ee = S.left;
    V + D.height > window.innerHeight && (V = S.top - D.height - 4), ee + D.width > window.innerWidth && (ee = Math.max(0, S.right - D.width)), N({ top: V, left: ee });
  }, [n]), Se(() => {
    !l && !v && o().catch(() => b(!0));
  }, [l, v, o]), Se(() => {
    l && O.current && O.current.focus();
  }, [l]), Se(() => {
    const S = (D) => {
      D.key === "Escape" && i();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [i]), Se(() => {
    const S = (V) => {
      k.current && !k.current.contains(V.target) && i();
    }, D = setTimeout(() => document.addEventListener("mousedown", S), 0);
    return () => {
      clearTimeout(D), document.removeEventListener("mousedown", S);
    };
  }, [i]);
  const P = Tl(() => {
    if (!m) return r;
    const S = m.toLowerCase();
    return r.filter(
      (D) => D.prefix.toLowerCase().includes(S) || D.label.toLowerCase().includes(S) || D.terms != null && D.terms.some((V) => V.includes(S))
    );
  }, [r, m]), h = pe((S) => {
    p(S.target.value);
  }, []), E = pe(
    (S) => {
      c(S);
    },
    [c]
  ), g = pe((S) => {
    L(S);
  }, []), X = pe((S) => {
    L(S.target.value);
  }, []), T = pe(() => {
    c(_ || null);
  }, [_, c]), R = pe(() => {
    c(null);
  }, [c]), A = pe(async (S) => {
    S.preventDefault(), b(!1);
    try {
      await o();
    } catch {
      b(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: k,
      style: f ? { top: f.top, left: f.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("simple")
      },
      d["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("advanced")
      },
      d["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: O,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: h,
        placeholder: d["js.iconSelect.filterPlaceholder"],
        "aria-label": d["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => p(""),
        title: d["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !l && !v && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: A }, d["js.iconSelect.loadError"])),
      l && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, d["js.iconSelect.noResults"]),
      l && P.map(
        (S) => S.variants.map((D) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: D.encoded,
            className: "tlIconSelect__iconCell" + (D.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": D.encoded === t,
            tabIndex: 0,
            title: S.label,
            onClick: () => s === "simple" ? E(D.encoded) : g(D.encoded),
            onKeyDown: (V) => {
              (V.key === "Enter" || V.key === " ") && (V.preventDefault(), s === "simple" ? E(D.encoded) : g(D.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(De, { encoded: D.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: X
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, d["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(De, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, d["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, d["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, d["js.iconSelect.ok"]))
  );
}, Dl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: xl, useCallback: Ne, useRef: Il } = e, jl = ({ controlId: n, state: t }) => {
  const r = le(), l = oe(Dl), [c, i] = xl(!1), o = Il(null), d = t.value, s = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, _ = Ne(() => {
    s && !a && i(!0);
  }, [s, a]), L = Ne(
    (f) => {
      i(!1), r("valueChanged", { value: f });
    },
    [r]
  ), v = Ne(() => {
    i(!1);
  }, []), b = Ne(async () => {
    await r("loadIcons");
  }, [r]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (d == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: a,
      title: d ?? "",
      "aria-label": l["js.iconSelect.chooseIcon"]
    },
    d ? /* @__PURE__ */ e.createElement(De, { encoded: d }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ll,
    {
      anchorRef: o,
      currentValue: d,
      icons: m,
      iconsLoaded: p,
      onSelect: L,
      onCancel: v,
      onLoadIcons: b
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, d ? /* @__PURE__ */ e.createElement(De, { encoded: d }) : null));
};
H("TLButton", Tt);
H("TLToggleButton", Lt);
H("TLTextInput", _t);
H("TLNumberInput", vt);
H("TLDatePicker", gt);
H("TLSelect", yt);
H("TLCheckbox", kt);
H("TLTable", St);
H("TLCounter", Dt);
H("TLTabBar", It);
H("TLFieldList", jt);
H("TLAudioRecorder", Mt);
H("TLAudioPlayer", Ot);
H("TLFileUpload", Bt);
H("TLDownload", Ht);
H("TLPhotoCapture", Ut);
H("TLPhotoViewer", Vt);
H("TLSplitPanel", Kt);
H("TLPanel", Jt);
H("TLMaximizeRoot", en);
H("TLDeckPane", tn);
H("TLSidebar", un);
H("TLStack", dn);
H("TLGrid", mn);
H("TLCard", pn);
H("TLAppBar", fn);
H("TLBreadcrumb", _n);
H("TLBottomBar", vn);
H("TLDialog", gn);
H("TLDialogManager", wn);
H("TLWindow", Nn);
H("TLDrawer", Dn);
H("TLSnackbar", jn);
H("TLMenu", Mn);
H("TLAppShell", An);
H("TLTextCell", On);
H("TLTableView", Bn);
H("TLFormLayout", Kn);
H("TLFormGroup", Xn);
H("TLFormField", Jn);
H("TLResourceCell", el);
H("TLTreeView", nl);
H("TLDropdownSelect", ul);
H("TLColorInput", Sl);
H("TLIconSelect", jl);
