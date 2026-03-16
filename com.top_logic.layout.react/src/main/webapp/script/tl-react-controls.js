import { React as e, useTLFieldValue as Ce, getComponent as ft, useTLState as G, useTLCommand as le, TLChild as Y, useTLUpload as Ue, useI18N as oe, useTLDataUrl as Ve, register as W } from "tl-react-bridge";
const { useCallback: ht } = e, _t = ({ controlId: l, state: t }) => {
  const [a, n] = Ce(), c = ht(
    (s) => {
      n(s.target.value);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, a ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactTextInput",
    i ? "tlReactTextInput--error" : "",
    !i && o ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: a ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: bt } = e, vt = ({ controlId: l, state: t, config: a }) => {
  const [n, c] = Ce(), i = bt(
    (r) => {
      const m = r.target.value;
      c(m === "" ? null : m);
    },
    [c]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "");
  const o = t.hasError === !0, u = t.hasWarnings === !0, s = [
    "tlReactNumberInput",
    o ? "tlReactNumberInput--error" : "",
    !o && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: a != null && a.decimal ? "decimal" : "numeric",
      value: n != null ? String(n) : "",
      onChange: i,
      disabled: t.disabled === !0,
      className: s,
      "aria-invalid": o || void 0
    }
  ));
}, { useCallback: Et } = e, gt = ({ controlId: l, state: t }) => {
  const [a, n] = Ce(), c = Et(
    (s) => {
      n(s.target.value || null);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && o ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: a ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: Ct } = e, yt = ({ controlId: l, state: t, config: a }) => {
  var m;
  const [n, c] = Ce(), i = Ct(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = o.find((_) => _.value === n)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, s = t.hasWarnings === !0, r = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && s ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: r,
      "aria-invalid": u || void 0
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: wt } = e, kt = ({ controlId: l, state: t }) => {
  const [a, n] = Ce(), c = wt(
    (s) => {
      n(s.target.checked);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        checked: a === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    i ? "tlReactCheckbox--error" : "",
    !i && o ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: a === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  );
}, St = ({ controlId: l, state: t }) => {
  const a = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, a.map((o) => {
    const u = o.cellModule ? ft(o.cellModule) : void 0, s = c[o.name];
    if (u) {
      const r = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + i + "-" + o.name,
          state: r
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
}, { useCallback: Nt } = e, Tt = ({ controlId: l, command: t, label: a, disabled: n }) => {
  const c = G(), i = le(), o = t ?? "click", u = a ?? c.label, s = n ?? c.disabled === !0, r = Nt(() => {
    i(o);
  }, [i, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: r,
      disabled: s,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: Rt } = e, Lt = ({ controlId: l, command: t, label: a, active: n, disabled: c }) => {
  const i = G(), o = le(), u = t ?? "click", s = a ?? i.label, r = n ?? i.active === !0, m = c ?? i.disabled === !0, p = Rt(() => {
    o(u);
  }, [o, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : "")
    },
    s
  );
}, xt = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Dt } = e, It = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.tabs ?? [], c = t.activeTabId, i = Dt((o) => {
    o !== c && a("selectTab", { tabId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
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
}, jt = ({ controlId: l }) => {
  const t = G(), a = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Pt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Mt = ({ controlId: l }) => {
  const t = G(), a = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, _ = m === "received" ? "idle" : n !== "idle" ? n : m, N = e.useCallback(async () => {
    if (n === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = y, s.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(y, B ? { mimeType: B } : void 0);
        u.current = j, j.ondataavailable = (f) => {
          f.data.size > 0 && s.current.push(f.data);
        }, j.onstop = async () => {
          y.getTracks().forEach((E) => E.stop()), r.current = null;
          const f = new Blob(s.current, { type: j.mimeType || "audio/webm" });
          if (s.current = [], f.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const C = new FormData();
          C.append("audio", f, "recording.webm"), await a(C), c("idle");
        }, j.start(), c("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, a]), b = oe(Pt), v = _ === "recording" ? b["js.audioRecorder.stop"] : _ === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], h = _ === "uploading", S = ["tlAudioRecorder__button"];
  return _ === "recording" && S.push("tlAudioRecorder__button--recording"), _ === "uploading" && S.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: N,
      disabled: h,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, At = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Bt = ({ controlId: l }) => {
  const t = G(), a = Ve(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(n ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
  e.useEffect(() => {
    n ? i === "disabled" && o("idle") : (u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    c !== r.current && (r.current = c, u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
  }, [c]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (i === "disabled" || i === "loading")
      return;
    if (i === "playing") {
      u.current && u.current.pause(), o("paused");
      return;
    }
    if (i === "paused" && u.current) {
      u.current.play(), o("playing");
      return;
    }
    if (!s.current) {
      o("loading");
      try {
        const h = await fetch(a);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), o("idle");
          return;
        }
        const S = await h.blob();
        s.current = URL.createObjectURL(S);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), o("idle");
        return;
      }
    }
    const v = new Audio(s.current);
    u.current = v, v.onended = () => {
      o("idle");
    }, v.play(), o("playing");
  }, [i, a]), p = oe(At), _ = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: N,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ot = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, $t = ({ controlId: l }) => {
  const t = G(), a = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : n !== "idle" ? n : s, _ = e.useCallback(async (f) => {
    c("uploading");
    const C = new FormData();
    C.append("file", f, f.name), await a(C), c("idle");
  }, [a]), N = e.useCallback((f) => {
    var E;
    const C = (E = f.target.files) == null ? void 0 : E[0];
    C && _(C);
  }, [_]), b = e.useCallback(() => {
    var f;
    n !== "uploading" && ((f = u.current) == null || f.click());
  }, [n]), v = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), h = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), S = e.useCallback((f) => {
    var E;
    if (f.preventDefault(), f.stopPropagation(), o(!1), n === "uploading") return;
    const C = (E = f.dataTransfer.files) == null ? void 0 : E[0];
    C && _(C);
  }, [n, _]), y = p === "uploading", B = oe(Ot), j = p === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: h,
      onDrop: S
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: N,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: y,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, Ft = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Ht = ({ controlId: l }) => {
  const t = G(), a = Ve(), n = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const b = a + (a.includes("?") ? "&" : "?") + "rev=" + i, v = await fetch(b);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const h = await v.blob(), S = URL.createObjectURL(h), y = document.createElement("a");
        y.href = S, y.download = o, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(S);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, i, o]), p = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), _ = oe(Ft);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const N = s ? _["js.downloading"] : _["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), u && /* @__PURE__ */ e.createElement(
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
}, Wt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, zt = ({ controlId: l }) => {
  const t = G(), a = Ue(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), _ = e.useRef(null), N = e.useRef(null), b = t.error, v = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), S = e.useCallback(() => {
    h(), c("idle");
  }, [h]), y = e.useCallback(async () => {
    var L;
    if (n !== "uploading") {
      if (o(null), !v) {
        (L = _.current) == null || L.click();
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
  }, [n, v]), B = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const L = r.current, A = p.current;
    if (!L || !A)
      return;
    A.width = L.videoWidth, A.height = L.videoHeight;
    const R = A.getContext("2d");
    R && (R.drawImage(L, 0, 0), h(), c("uploading"), A.toBlob(async (I) => {
      if (!I) {
        c("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", I, "capture.jpg"), await a(O), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, a, h]), j = e.useCallback(async (L) => {
    var I;
    const A = (I = L.target.files) == null ? void 0 : I[0];
    if (!A) return;
    c("uploading");
    const R = new FormData();
    R.append("photo", A, A.name), await a(R), c("idle"), _.current && (_.current.value = "");
  }, [a]);
  e.useEffect(() => {
    n === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var A;
    if (n !== "overlayOpen") return;
    (A = N.current) == null || A.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const L = (A) => {
      A.key === "Escape" && S();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [n, S]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const f = oe(Wt), C = n === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  u && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: y,
      disabled: n === "uploading",
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: _,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: S }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: r,
        className: V.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: T.join(" "),
        onClick: () => s((L) => !L),
        title: f["js.photoCapture.mirror"],
        "aria-label": f["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: f["js.photoCapture.capture"],
        "aria-label": f["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: S,
        title: f["js.photoCapture.close"],
        "aria-label": f["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Ut = {
  "js.photoViewer.alt": "Captured photo"
}, Vt = ({ controlId: l }) => {
  const t = G(), a = Ve(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (c === u.current && i)
      return;
    u.current = c, i && (URL.revokeObjectURL(i), o(null));
    let r = !1;
    return (async () => {
      try {
        const m = await fetch(a);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        r || o(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      r = !0;
    };
  }, [n, c, a]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(Ut);
  return !n || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Ye, useRef: Ie } = e, Kt = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = n === "horizontal", u = i.length > 0 && i.every((h) => h.collapsed), s = !u && i.some((h) => h.collapsed), r = u ? !o : o, m = Ie(null), p = Ie(null), _ = Ie(null), N = Ye((h, S) => {
    const y = {
      overflow: h.scrolling || "auto"
    };
    return h.collapsed ? u && !r ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : S !== void 0 ? y.flex = `0 0 ${S}px` : h.unit === "%" || s ? y.flex = `${h.size} 0 0%` : y.flex = `0 0 ${h.size}px`, h.minSize > 0 && !h.collapsed && (y.minWidth = o ? h.minSize : void 0, y.minHeight = o ? void 0 : h.minSize), y;
  }, [o, u, s, r]), b = Ye((h, S) => {
    h.preventDefault();
    const y = m.current;
    if (!y) return;
    const B = i[S], j = i[S + 1], f = y.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    f.forEach((T) => {
      C.push(o ? T.offsetWidth : T.offsetHeight);
    }), _.current = C, p.current = {
      splitterIndex: S,
      startPos: o ? h.clientX : h.clientY,
      startSizeBefore: C[S],
      startSizeAfter: C[S + 1],
      childBefore: B,
      childAfter: j
    };
    const E = (T) => {
      const L = p.current;
      if (!L || !_.current) return;
      const R = (o ? T.clientX : T.clientY) - L.startPos, I = L.childBefore.minSize || 0, O = L.childAfter.minSize || 0;
      let ee = L.startSizeBefore + R, Z = L.startSizeAfter - R;
      ee < I && (Z += ee - I, ee = I), Z < O && (ee += Z - O, Z = O), _.current[L.splitterIndex] = ee, _.current[L.splitterIndex + 1] = Z;
      const re = y.querySelectorAll(":scope > .tlSplitPanel__child"), $ = re[L.splitterIndex], P = re[L.splitterIndex + 1];
      $ && ($.style.flex = `0 0 ${ee}px`), P && (P.style.flex = `0 0 ${Z}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const T = {};
        i.forEach((L, A) => {
          const R = L.control;
          R != null && R.controlId && _.current && (T[R.controlId] = _.current[A]);
        }), a("updateSizes", { sizes: T });
      }
      _.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", V), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), v = [];
  return i.forEach((h, S) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${S}`,
          className: `tlSplitPanel__child${h.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(h)
        },
        /* @__PURE__ */ e.createElement(Y, { control: h.control })
      )
    ), c && S < i.length - 1) {
      const y = i[S + 1];
      !h.collapsed && !y.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${S}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (j) => b(j, S)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: r ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    v
  );
}, { useCallback: je } = e, Yt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Gt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), Xt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Zt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), Qt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), Jt = ({ controlId: l }) => {
  const t = G(), a = le(), n = oe(Yt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, r = t.toolbarButtons ?? [], m = i === "MINIMIZED", p = i === "MAXIMIZED", _ = i === "HIDDEN", N = je(() => {
    a("toggleMinimize");
  }, [a]), b = je(() => {
    a("toggleMaximize");
  }, [a]), v = je(() => {
    a("popOut");
  }, [a]);
  if (_)
    return null;
  const h = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}`,
      style: h
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, r.map((S, y) => /* @__PURE__ */ e.createElement("span", { key: y, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: S }))), o && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: N,
        title: m ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(Xt, null) : /* @__PURE__ */ e.createElement(Gt, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: p ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Zt, null) : /* @__PURE__ */ e.createElement(qt, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(Qt, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child }))
  );
}, en = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, tn = ({ controlId: l }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: Te, useEffect: Re, useRef: Le } = e, nn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function He(l, t, a, n) {
  const c = [];
  for (const i of l)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: n }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: n }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (a.get(i.id) ?? i.expanded) && !t && c.push(...He(i.children, t, a, i.id)));
  return c;
}
const be = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, ln = ({ item: l, active: t, collapsed: a, onSelect: n, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: a ? l.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(l.id)
  },
  a && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(be, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !a && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), rn = ({ item: l, collapsed: t, onExecute: a, tabIndex: n, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), an = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(be, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), on = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), sn = ({ item: l, activeItemId: t, anchorRect: a, onSelect: n, onExecute: c, onClose: i }) => {
  const o = Le(null);
  Re(() => {
    const r = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [i]), Re(() => {
    const r = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [i]);
  const u = ue((r) => {
    r.type === "nav" ? (n(r.id), i()) : r.type === "command" && (c(r.id), i());
  }, [n, c, i]), s = {};
  return a && (s.left = a.right, s.top = a.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((r) => {
    if (r.type === "nav" || r.type === "command") {
      const m = r.type === "nav" && r.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: r.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(r)
        },
        /* @__PURE__ */ e.createElement(be, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, cn = ({
  item: l,
  expanded: t,
  activeItemId: a,
  collapsed: n,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: u,
  itemRef: s,
  onFocus: r,
  focusedId: m,
  setItemRef: p,
  onItemFocus: _,
  flyoutGroupId: N,
  onOpenFlyout: b,
  onCloseFlyout: v
}) => {
  const h = Le(null), [S, y] = Te(null), B = ue(() => {
    n ? N === l.id ? v() : (h.current && y(h.current.getBoundingClientRect()), b(l.id)) : o(l.id);
  }, [n, N, l.id, o, b, v]), j = ue((C) => {
    h.current = C, s(C);
  }, [s]), f = n && N === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: B,
      title: n ? l.label : void 0,
      "aria-expanded": n ? f : t,
      tabIndex: u,
      ref: j,
      onFocus: () => r(l.id)
    },
    /* @__PURE__ */ e.createElement(be, { icon: l.icon }),
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
  ), f && /* @__PURE__ */ e.createElement(
    sn,
    {
      item: l,
      activeItemId: a,
      anchorRect: S,
      onSelect: c,
      onExecute: i,
      onClose: v
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((C) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: C.id,
      item: C,
      activeItemId: a,
      collapsed: n,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: b,
      onCloseFlyout: v
    }
  ))));
}, st = ({
  item: l,
  activeItemId: t,
  collapsed: a,
  onSelect: n,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: u,
  onItemFocus: s,
  groupStates: r,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: _
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        ln,
        {
          item: l,
          active: l.id === t,
          collapsed: a,
          onSelect: n,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        rn,
        {
          item: l,
          collapsed: a,
          onExecute: c,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(an, { item: l, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(on, null);
    case "group": {
      const N = r ? r.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        cn,
        {
          item: l,
          expanded: N,
          activeItemId: t,
          collapsed: a,
          onSelect: n,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: u,
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
}, un = ({ controlId: l }) => {
  const t = G(), a = le(), n = oe(nn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = Te(() => {
    const T = /* @__PURE__ */ new Map(), L = (A) => {
      for (const R of A)
        R.type === "group" && (T.set(R.id, R.expanded), L(R.children));
    };
    return L(c), T;
  }), r = ue((T) => {
    s((L) => {
      const A = new Map(L), R = A.get(T) ?? !1;
      return A.set(T, !R), a("toggleGroup", { itemId: T, expanded: !R }), A;
    });
  }, [a]), m = ue((T) => {
    T !== i && a("selectItem", { itemId: T });
  }, [a, i]), p = ue((T) => {
    a("executeCommand", { itemId: T });
  }, [a]), _ = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [N, b] = Te(null), v = ue((T) => {
    b(T);
  }, []), h = ue(() => {
    b(null);
  }, []);
  Re(() => {
    o || b(null);
  }, [o]);
  const [S, y] = Te(() => {
    const T = He(c, o, u);
    return T.length > 0 ? T[0].id : "";
  }), B = Le(/* @__PURE__ */ new Map()), j = ue((T) => (L) => {
    L ? B.current.set(T, L) : B.current.delete(T);
  }, []), f = ue((T) => {
    y(T);
  }, []), C = Le(0), E = ue((T) => {
    y(T), C.current++;
  }, []);
  Re(() => {
    const T = B.current.get(S);
    T && document.activeElement !== T && T.focus();
  }, [S, C.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && N !== null) {
      T.preventDefault(), h();
      return;
    }
    const L = He(c, o, u);
    if (L.length === 0) return;
    const A = L.findIndex((I) => I.id === S);
    if (A < 0) return;
    const R = L[A];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const I = (A + 1) % L.length;
        E(L[I].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const I = (A - 1 + L.length) % L.length;
        E(L[I].id);
        break;
      }
      case "Home": {
        T.preventDefault(), E(L[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), E(L[L.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), R.type === "nav" ? m(R.id) : R.type === "command" ? p(R.id) : R.type === "group" && (o ? N === R.id ? h() : v(R.id) : r(R.id));
        break;
      }
      case "ArrowRight": {
        R.type === "group" && !o && ((u.get(R.id) ?? !1) || (T.preventDefault(), r(R.id)));
        break;
      }
      case "ArrowLeft": {
        R.type === "group" && !o && (u.get(R.id) ?? !1) && (T.preventDefault(), r(R.id));
        break;
      }
    }
  }, [
    c,
    o,
    u,
    S,
    N,
    E,
    m,
    p,
    r,
    v,
    h
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((T) => /* @__PURE__ */ e.createElement(
    st,
    {
      key: T.id,
      item: T,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: S,
      setItemRef: j,
      onItemFocus: f,
      groupStates: u,
      flyoutGroupId: N,
      onOpenFlyout: v,
      onCloseFlyout: h
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: _,
      title: o ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
}, dn = ({ controlId: l }) => {
  const t = G(), a = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, o.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })));
}, mn = ({ controlId: l }) => {
  const t = G(), a = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: u })));
}, pn = ({ controlId: l }) => {
  const t = G(), a = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, fn = ({ controlId: l }) => {
  const t = G(), a = t.title ?? "", n = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: u }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s }))));
}, { useCallback: hn } = e, _n = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.items ?? [], c = hn((i) => {
    a("navigate", { itemId: i });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((i, o) => {
    const u = o === n.length - 1;
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, i.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(i.id)
      },
      i.label
    ));
  })));
}, { useCallback: bn } = e, vn = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.items ?? [], c = t.activeItemId, i = bn((o) => {
    o !== c && a("selectItem", { itemId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
    const u = o.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: o.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => i(o.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + o.icon, "aria-hidden": "true" }), o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, o.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, o.label)
    );
  }));
}, { useCallback: Ge, useEffect: Xe, useRef: En } = e, gn = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = En(null), u = Ge(() => {
    a("close");
  }, [a]), s = Ge((r) => {
    c && r.target === r.currentTarget && u();
  }, [c, u]);
  return Xe(() => {
    if (!n) return;
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [n, u]), Xe(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: s,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Y, { control: i })
  ) : null;
}, { useEffect: Cn, useRef: yn } = e, wn = ({ controlId: l }) => {
  const a = G().dialogs ?? [], n = yn(a.length);
  return Cn(() => {
    a.length < n.current && a.length > 0, n.current = a.length;
  }, [a.length]), a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, a.map((c) => /* @__PURE__ */ e.createElement(Y, { key: c.controlId, control: c })));
}, { useCallback: qe, useRef: ye, useState: Ze } = e, kn = {
  "js.window.close": "Close"
}, Sn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Nn = ({ controlId: l }) => {
  const t = G(), a = le(), n = oe(kn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.resizable === !0, s = t.child, r = t.actions ?? [], m = t.toolbarButtons ?? [], [p, _] = Ze(null), [N, b] = Ze(null), v = ye(null), h = ye(null), S = ye(null), y = ye(null), B = qe(() => {
    a("close");
  }, [a]), j = qe((E, V) => {
    V.preventDefault();
    const T = S.current;
    if (!T) return;
    const L = T.getBoundingClientRect();
    y.current = {
      dir: E,
      startX: V.clientX,
      startY: V.clientY,
      startW: L.width,
      startH: L.height
    };
    const A = (I) => {
      const O = y.current;
      if (!O) return;
      const ee = I.clientX - O.startX, Z = I.clientY - O.startY;
      let re = O.startW, $ = O.startH;
      O.dir.includes("e") && (re = O.startW + ee), O.dir.includes("w") && (re = O.startW - ee), O.dir.includes("s") && ($ = O.startH + Z), O.dir.includes("n") && ($ = O.startH - Z);
      const P = Math.max(200, re), q = Math.max(100, $);
      v.current = P, h.current = q, _(P), b(q);
    }, R = () => {
      document.removeEventListener("mousemove", A), document.removeEventListener("mouseup", R);
      const I = v.current, O = h.current;
      (I != null || O != null) && (a("resize", {
        ...I != null ? { width: Math.round(I) + "px" } : {},
        ...O != null ? { height: Math.round(O) + "px" } : {}
      }), v.current = null, h.current = null, _(null), b(null)), y.current = null;
    };
    document.addEventListener("mousemove", A), document.addEventListener("mouseup", R);
  }, [a]), f = {
    width: p != null ? p + "px" : i,
    ...N != null ? { height: N + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, C = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: f,
      ref: S,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": C
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: C }, c), m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((E, V) => /* @__PURE__ */ e.createElement("span", { key: V, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: E })))), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: B,
        title: n["js.window.close"]
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
    r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, r.map((E, V) => /* @__PURE__ */ e.createElement(Y, { key: V, control: E }))),
    u && Sn.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (V) => j(E, V)
      }
    ))
  );
}, { useCallback: Tn, useEffect: Rn } = e, Ln = {
  "js.drawer.close": "Close"
}, xn = ({ controlId: l }) => {
  const t = G(), a = le(), n = oe(Ln), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, r = Tn(() => {
    a("close");
  }, [a]);
  Rn(() => {
    if (!c) return;
    const p = (_) => {
      _.key === "Escape" && r();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, r]);
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: r,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(Y, { control: s })));
}, { useCallback: Qe, useEffect: Dn, useState: In } = e, jn = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, u = t.duration ?? 5e3, s = t.visible === !0, r = t.generation ?? 0, [m, p] = In(!1), _ = Qe(() => {
    p(!0), setTimeout(() => {
      a("dismiss", { generation: r }), p(!1);
    }, 200);
  }, [a, r]), N = Qe(() => {
    o && a(o.commandName), _();
  }, [a, o, _]);
  return Dn(() => {
    if (!s || u === 0) return;
    const b = setTimeout(_, u);
    return () => clearTimeout(b);
  }, [s, u, _]), console.log("[TLSnackbar] render", { visible: s, exiting: m, generation: r, content: c, message: n }), !s && !m ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${m ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: N }, o.label)
  );
}, { useCallback: Pe, useEffect: Me, useRef: Pn, useState: Je } = e, Mn = ({ controlId: l }) => {
  const t = G(), a = le(), n = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Pn(null), [u, s] = Je({ top: 0, left: 0 }), [r, m] = Je(0), p = i.filter((v) => v.type === "item" && !v.disabled);
  Me(() => {
    var f, C;
    if (!n || !c) return;
    const v = document.getElementById(c);
    if (!v) return;
    const h = v.getBoundingClientRect(), S = ((f = o.current) == null ? void 0 : f.offsetHeight) ?? 200, y = ((C = o.current) == null ? void 0 : C.offsetWidth) ?? 200;
    let B = h.bottom + 4, j = h.left;
    B + S > window.innerHeight && (B = h.top - S - 4), j + y > window.innerWidth && (j = h.right - y), s({ top: B, left: j }), m(0);
  }, [n, c]);
  const _ = Pe(() => {
    a("close");
  }, [a]), N = Pe((v) => {
    a("selectItem", { itemId: v });
  }, [a]);
  Me(() => {
    if (!n) return;
    const v = (h) => {
      o.current && !o.current.contains(h.target) && _();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [n, _]);
  const b = Pe((v) => {
    if (v.key === "Escape") {
      _();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), m((h) => (h + 1) % p.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((h) => (h - 1 + p.length) % p.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const h = p[r];
      h && N(h.id);
    }
  }, [_, N, p, r]);
  return Me(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: u.top, left: u.left },
      onKeyDown: b
    },
    i.map((v, h) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: h, className: "tlMenu__separator" });
      const y = p.indexOf(v) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (y ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: y ? 0 : -1,
          onClick: () => N(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, An = ({ controlId: l }) => {
  const t = G(), a = t.header, n = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, Bn = () => {
  const t = G().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, On = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, et = 50, $n = () => {
  const l = G(), t = le(), a = oe(On), n = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, u = l.selectionMode ?? "single", s = l.selectedCount ?? 0, r = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, p = e.useMemo(
    () => n.filter((k) => k.sortPriority && k.sortPriority > 0).length,
    [n]
  ), _ = u === "multi", N = 40, b = 20, v = e.useRef(null), h = e.useRef(null), S = e.useRef(null), [y, B] = e.useState({}), j = e.useRef(null), f = e.useRef(!1), C = e.useRef(null), [E, V] = e.useState(null), [T, L] = e.useState(null);
  e.useEffect(() => {
    j.current || B({});
  }, [n]);
  const A = e.useCallback((k) => y[k.name] ?? k.width, [y]), R = e.useMemo(() => {
    const k = [];
    let x = _ && r > 0 ? N : 0;
    for (let U = 0; U < r && U < n.length; U++)
      k.push(x), x += A(n[U]);
    return k;
  }, [n, r, _, N, A]), I = c * o, O = e.useCallback((k, x, U) => {
    U.preventDefault(), U.stopPropagation(), j.current = { column: k, startX: U.clientX, startWidth: x };
    const te = (w) => {
      const D = j.current;
      if (!D) return;
      const H = Math.max(et, D.startWidth + (w.clientX - D.startX));
      B((J) => ({ ...J, [D.column]: H }));
    }, ne = (w) => {
      document.removeEventListener("mousemove", te), document.removeEventListener("mouseup", ne);
      const D = j.current;
      if (D) {
        const H = Math.max(et, D.startWidth + (w.clientX - D.startX));
        t("columnResize", { column: D.column, width: H }), j.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", te), document.addEventListener("mouseup", ne);
  }, [t]), ee = e.useCallback(() => {
    v.current && h.current && (v.current.scrollLeft = h.current.scrollLeft), S.current !== null && clearTimeout(S.current), S.current = window.setTimeout(() => {
      const k = h.current;
      if (!k) return;
      const x = k.scrollTop, U = Math.ceil(k.clientHeight / o), te = Math.floor(x / o);
      t("scroll", { start: te, count: U });
    }, 80);
  }, [t, o]), Z = e.useCallback((k, x, U) => {
    if (f.current) return;
    let te;
    !x || x === "desc" ? te = "asc" : te = "desc";
    const ne = U.shiftKey ? "add" : "replace";
    t("sort", { column: k, direction: te, mode: ne });
  }, [t]), re = e.useCallback((k, x) => {
    C.current = k, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", k);
  }, []), $ = e.useCallback((k, x) => {
    if (!C.current || C.current === k) {
      V(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), te = x.clientX < U.left + U.width / 2 ? "left" : "right";
    V({ column: k, side: te });
  }, []), P = e.useCallback((k) => {
    k.preventDefault(), k.stopPropagation();
    const x = C.current;
    if (!x || !E) {
      C.current = null, V(null);
      return;
    }
    let U = n.findIndex((ne) => ne.name === E.column);
    if (U < 0) {
      C.current = null, V(null);
      return;
    }
    const te = n.findIndex((ne) => ne.name === x);
    E.side === "right" && U++, te < U && U--, t("columnReorder", { column: x, targetIndex: U }), C.current = null, V(null);
  }, [n, E, t]), q = e.useCallback(() => {
    C.current = null, V(null);
  }, []), d = e.useCallback((k, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: k,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), g = e.useCallback((k, x) => {
    x.stopPropagation(), t("select", { rowIndex: k, ctrlKey: !0, shiftKey: !1 });
  }, [t]), F = e.useCallback(() => {
    const k = s === c && c > 0;
    t("selectAll", { selected: !k });
  }, [t, s, c]), M = e.useCallback((k, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: k, expanded: x });
  }, [t]), K = e.useCallback((k, x) => {
    x.preventDefault(), L({ x: x.clientX, y: x.clientY, colIdx: k });
  }, []), X = e.useCallback(() => {
    T && (t("setFrozenColumnCount", { count: T.colIdx + 1 }), L(null));
  }, [T, t]), Q = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), L(null);
  }, [t]);
  e.useEffect(() => {
    if (!T) return;
    const k = () => L(null), x = (U) => {
      U.key === "Escape" && L(null);
    };
    return document.addEventListener("mousedown", k), document.addEventListener("keydown", x), () => {
      document.removeEventListener("mousedown", k), document.removeEventListener("keydown", x);
    };
  }, [T]);
  const se = n.reduce((k, x) => k + A(x), 0) + (_ ? N : 0), ae = s === c && c > 0, fe = s > 0 && s < c, De = e.useCallback((k) => {
    k && (k.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (k) => {
        if (!C.current) return;
        k.preventDefault();
        const x = h.current, U = v.current;
        if (!x) return;
        const te = x.getBoundingClientRect(), ne = 40, w = 8;
        k.clientX < te.left + ne ? x.scrollLeft = Math.max(0, x.scrollLeft - w) : k.clientX > te.right - ne && (x.scrollLeft += w), U && (U.scrollLeft = x.scrollLeft);
      },
      onDrop: P
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { minWidth: se } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (k) => {
          C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== C.current && V({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: De,
          className: "tlTableView__checkbox",
          checked: ae,
          onChange: F
        }
      )
    ), n.map((k, x) => {
      const U = A(k), te = x === n.length - 1;
      let ne = "tlTableView__headerCell";
      k.sortable && (ne += " tlTableView__headerCell--sortable"), E && E.column === k.name && (ne += " tlTableView__headerCell--dragOver-" + E.side);
      const w = x < r, D = x === r - 1;
      return w && (ne += " tlTableView__headerCell--frozen"), D && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.name,
          className: ne,
          style: {
            ...te && !w ? { flex: "1 0 auto", minWidth: U } : { width: U, minWidth: U },
            position: w ? "sticky" : "relative",
            ...w ? { left: R[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: k.sortable ? (H) => Z(k.name, k.sortDirection, H) : void 0,
          onContextMenu: (H) => K(x, H),
          onDragStart: (H) => re(k.name, H),
          onDragOver: (H) => $(k.name, H),
          onDrop: P,
          onDragEnd: q
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, k.label),
        k.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, k.sortDirection === "asc" ? "▲" : "▼", p > 1 && k.sortPriority != null && k.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, k.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (H) => O(k.name, U, H)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (k) => {
          if (C.current && n.length > 0) {
            const x = n[n.length - 1];
            x.name !== C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", V({ column: x.name, side: "right" }));
          }
        },
        onDrop: P
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: h,
        className: "tlTableView__body",
        onScroll: ee
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: I, position: "relative", minWidth: se } }, i.map((k) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlTableView__row" + (k.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: k.index * o,
            height: o,
            minWidth: se,
            width: "100%"
          },
          onClick: (x) => d(k.index, x)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: N,
              minWidth: N,
              ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (x) => x.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: k.selected,
              onChange: () => {
              },
              onClick: (x) => g(k.index, x),
              tabIndex: -1
            }
          )
        ),
        n.map((x, U) => {
          const te = A(x), ne = U === n.length - 1, w = U < r, D = U === r - 1;
          let H = "tlTableView__cell";
          w && (H += " tlTableView__cell--frozen"), D && (H += " tlTableView__cell--frozenLast");
          const J = m && U === 0, ve = k.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: H,
              style: {
                ...ne && !w ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...w ? { position: "sticky", left: R[U], zIndex: 2 } : {}
              }
            },
            J ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ve * b } }, k.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pt) => M(k.index, !k.expanded, pt)
              },
              k.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: k.cells[x.name] })) : /* @__PURE__ */ e.createElement(Y, { control: k.cells[x.name] })
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
        onMouseDown: (k) => k.stopPropagation()
      },
      T.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: X }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Q }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Fn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ct = e.createContext(Fn), { useMemo: Hn, useRef: Wn, useState: zn, useEffect: Un } = e, Vn = 320, Kn = ({ controlId: l }) => {
  const t = G(), a = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Wn(null), [s, r] = zn(
    n === "top" ? "top" : "side"
  );
  Un(() => {
    if (n !== "auto") {
      r(n);
      return;
    }
    const b = u.current;
    if (!b) return;
    const v = new ResizeObserver((h) => {
      for (const S of h) {
        const B = S.contentRect.width / a;
        r(B < Vn ? "top" : "side");
      }
    });
    return v.observe(b), () => v.disconnect();
  }, [n, a]);
  const m = Hn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), _ = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: N, style: _, ref: u }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useCallback: Yn } = e, Gn = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, Xn = ({ controlId: l }) => {
  const t = G(), a = le(), n = oe(Gn), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, _ = Yn(() => {
    a("toggleCollapse");
  }, [a]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    r ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: N }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useContext: qn, useState: Zn, useCallback: Qn } = e, Jn = ({ controlId: l }) => {
  const t = G(), a = qn(ct), n = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, r = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, _ = t.field, N = a.readOnly, [b, v] = Zn(!1), h = Qn(() => v((j) => !j), []);
  if (!p) return null;
  const S = i != null, y = o != null && o.length > 0, B = [
    "tlFormField",
    `tlFormField--${r}`,
    N ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    S ? "tlFormField--error" : "",
    !S && y ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !N && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !N && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: _ })), !N && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !N && !S && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((j, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
    "svg",
    {
      className: "tlFormField__warningIcon",
      viewBox: "0 0 16 16",
      width: "14",
      height: "14",
      "aria-hidden": "true"
    },
    /* @__PURE__ */ e.createElement("path", { d: "M8 1l7 14H1L8 1z", fill: "none", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "6", x2: "8", y2: "10", stroke: "currentColor", strokeWidth: "1.2" }),
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "12", r: "0.8", fill: "currentColor" })
  ), /* @__PURE__ */ e.createElement("span", null, j)))), !N && u && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, el = () => {
  const l = G(), t = le(), a = l.iconCss, n = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, u = l.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, tl = 20, nl = () => {
  const l = G(), t = le(), a = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((f, C) => {
    t(C ? "collapse" : "expand", { nodeId: f });
  }, [t]), _ = e.useCallback((f, C) => {
    t("select", {
      nodeId: f,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), N = e.useCallback((f, C) => {
    C.preventDefault(), t("contextMenu", { nodeId: f, x: C.clientX, y: C.clientY });
  }, [t]), b = e.useRef(null), v = e.useCallback((f, C) => {
    const E = C.getBoundingClientRect(), V = f.clientY - E.top, T = E.height / 3;
    return V < T ? "above" : V > T * 2 ? "below" : "within";
  }, []), h = e.useCallback((f, C) => {
    C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", f);
  }, []), S = e.useCallback((f, C) => {
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const E = v(C, C.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: f, position: E }), b.current = null;
    }, 50);
  }, [t, v]), y = e.useCallback((f, C) => {
    C.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const E = v(C, C.currentTarget);
    t("drop", { nodeId: f, position: E });
  }, [t, v]), B = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((f) => {
    if (a.length === 0) return;
    let C = s;
    switch (f.key) {
      case "ArrowDown":
        f.preventDefault(), C = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        f.preventDefault(), C = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const E = a[s];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (C = s + 1);
        }
        break;
      case "ArrowLeft":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const E = a[s];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const V = E.depth;
            for (let T = s - 1; T >= 0; T--)
              if (a[T].depth < V) {
                C = T;
                break;
              }
          }
        }
        break;
      case "Enter":
        f.preventDefault(), s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: f.ctrlKey || f.metaKey,
          shiftKey: f.shiftKey
        });
        return;
      case " ":
        f.preventDefault(), n === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        f.preventDefault(), C = 0;
        break;
      case "End":
        f.preventDefault(), C = a.length - 1;
        break;
      default:
        return;
    }
    C !== s && r(C);
  }, [s, a, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    a.map((f, C) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: f.id,
        role: "treeitem",
        "aria-expanded": f.expandable ? f.expanded : void 0,
        "aria-selected": f.selected,
        "aria-level": f.depth + 1,
        className: [
          "tlTreeView__node",
          f.selected ? "tlTreeView__node--selected" : "",
          C === s ? "tlTreeView__node--focused" : "",
          o === f.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === f.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === f.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: f.depth * tl },
        draggable: c,
        onClick: (E) => _(f.id, E),
        onContextMenu: (E) => N(f.id, E),
        onDragStart: (E) => h(f.id, E),
        onDragOver: i ? (E) => S(f.id, E) : void 0,
        onDrop: i ? (E) => y(f.id, E) : void 0,
        onDragEnd: B
      },
      f.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(f.id, f.expanded);
          },
          tabIndex: -1,
          "aria-label": f.expanded ? "Collapse" : "Expand"
        },
        f.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: f.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: f.content }))
    ))
  );
};
var Ae = { exports: {} }, ce = {}, Be = { exports: {} }, z = {};
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
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), _ = Symbol.iterator;
  function N(d) {
    return d === null || typeof d != "object" ? null : (d = _ && d[_] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var b = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, v = Object.assign, h = {};
  function S(d, g, F) {
    this.props = d, this.context = g, this.refs = h, this.updater = F || b;
  }
  S.prototype.isReactComponent = {}, S.prototype.setState = function(d, g) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, g, "setState");
  }, S.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = S.prototype;
  function B(d, g, F) {
    this.props = d, this.context = g, this.refs = h, this.updater = F || b;
  }
  var j = B.prototype = new y();
  j.constructor = B, v(j, S.prototype), j.isPureReactComponent = !0;
  var f = Array.isArray;
  function C() {
  }
  var E = { H: null, A: null, T: null, S: null }, V = Object.prototype.hasOwnProperty;
  function T(d, g, F) {
    var M = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: g,
      ref: M !== void 0 ? M : null,
      props: F
    };
  }
  function L(d, g) {
    return T(d.type, g, d.props);
  }
  function A(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function R(d) {
    var g = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return g[F];
    });
  }
  var I = /\/+/g;
  function O(d, g) {
    return typeof d == "object" && d !== null && d.key != null ? R("" + d.key) : g.toString(36);
  }
  function ee(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(C, C) : (d.status = "pending", d.then(
          function(g) {
            d.status === "pending" && (d.status = "fulfilled", d.value = g);
          },
          function(g) {
            d.status === "pending" && (d.status = "rejected", d.reason = g);
          }
        )), d.status) {
          case "fulfilled":
            return d.value;
          case "rejected":
            throw d.reason;
        }
    }
    throw d;
  }
  function Z(d, g, F, M, K) {
    var X = typeof d;
    (X === "undefined" || X === "boolean") && (d = null);
    var Q = !1;
    if (d === null) Q = !0;
    else
      switch (X) {
        case "bigint":
        case "string":
        case "number":
          Q = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              Q = !0;
              break;
            case m:
              return Q = d._init, Z(
                Q(d._payload),
                g,
                F,
                M,
                K
              );
          }
      }
    if (Q)
      return K = K(d), Q = M === "" ? "." + O(d, 0) : M, f(K) ? (F = "", Q != null && (F = Q.replace(I, "$&/") + "/"), Z(K, g, F, "", function(fe) {
        return fe;
      })) : K != null && (A(K) && (K = L(
        K,
        F + (K.key == null || d && d.key === K.key ? "" : ("" + K.key).replace(
          I,
          "$&/"
        ) + "/") + Q
      )), g.push(K)), 1;
    Q = 0;
    var se = M === "" ? "." : M + ":";
    if (f(d))
      for (var ae = 0; ae < d.length; ae++)
        M = d[ae], X = se + O(M, ae), Q += Z(
          M,
          g,
          F,
          X,
          K
        );
    else if (ae = N(d), typeof ae == "function")
      for (d = ae.call(d), ae = 0; !(M = d.next()).done; )
        M = M.value, X = se + O(M, ae++), Q += Z(
          M,
          g,
          F,
          X,
          K
        );
    else if (X === "object") {
      if (typeof d.then == "function")
        return Z(
          ee(d),
          g,
          F,
          M,
          K
        );
      throw g = String(d), Error(
        "Objects are not valid as a React child (found: " + (g === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : g) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function re(d, g, F) {
    if (d == null) return d;
    var M = [], K = 0;
    return Z(d, M, "", "", function(X) {
      return g.call(F, X, K++);
    }), M;
  }
  function $(d) {
    if (d._status === -1) {
      var g = d._result;
      g = g(), g.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = g);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var g = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(g)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, q = {
    map: re,
    forEach: function(d, g, F) {
      re(
        d,
        function() {
          g.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var g = 0;
      return re(d, function() {
        g++;
      }), g;
    },
    toArray: function(d) {
      return re(d, function(g) {
        return g;
      }) || [];
    },
    only: function(d) {
      if (!A(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return z.Activity = p, z.Children = q, z.Component = S, z.Fragment = a, z.Profiler = c, z.PureComponent = B, z.StrictMode = n, z.Suspense = s, z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, z.cacheSignal = function() {
    return null;
  }, z.cloneElement = function(d, g, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var M = v({}, d.props), K = d.key;
    if (g != null)
      for (X in g.key !== void 0 && (K = "" + g.key), g)
        !V.call(g, X) || X === "key" || X === "__self" || X === "__source" || X === "ref" && g.ref === void 0 || (M[X] = g[X]);
    var X = arguments.length - 2;
    if (X === 1) M.children = F;
    else if (1 < X) {
      for (var Q = Array(X), se = 0; se < X; se++)
        Q[se] = arguments[se + 2];
      M.children = Q;
    }
    return T(d.type, K, M);
  }, z.createContext = function(d) {
    return d = {
      $$typeof: o,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: i,
      _context: d
    }, d;
  }, z.createElement = function(d, g, F) {
    var M, K = {}, X = null;
    if (g != null)
      for (M in g.key !== void 0 && (X = "" + g.key), g)
        V.call(g, M) && M !== "key" && M !== "__self" && M !== "__source" && (K[M] = g[M]);
    var Q = arguments.length - 2;
    if (Q === 1) K.children = F;
    else if (1 < Q) {
      for (var se = Array(Q), ae = 0; ae < Q; ae++)
        se[ae] = arguments[ae + 2];
      K.children = se;
    }
    if (d && d.defaultProps)
      for (M in Q = d.defaultProps, Q)
        K[M] === void 0 && (K[M] = Q[M]);
    return T(d, X, K);
  }, z.createRef = function() {
    return { current: null };
  }, z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, z.isValidElement = A, z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: $
    };
  }, z.memo = function(d, g) {
    return {
      $$typeof: r,
      type: d,
      compare: g === void 0 ? null : g
    };
  }, z.startTransition = function(d) {
    var g = E.T, F = {};
    E.T = F;
    try {
      var M = d(), K = E.S;
      K !== null && K(F, M), typeof M == "object" && M !== null && typeof M.then == "function" && M.then(C, P);
    } catch (X) {
      P(X);
    } finally {
      g !== null && F.types !== null && (g.types = F.types), E.T = g;
    }
  }, z.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, z.use = function(d) {
    return E.H.use(d);
  }, z.useActionState = function(d, g, F) {
    return E.H.useActionState(d, g, F);
  }, z.useCallback = function(d, g) {
    return E.H.useCallback(d, g);
  }, z.useContext = function(d) {
    return E.H.useContext(d);
  }, z.useDebugValue = function() {
  }, z.useDeferredValue = function(d, g) {
    return E.H.useDeferredValue(d, g);
  }, z.useEffect = function(d, g) {
    return E.H.useEffect(d, g);
  }, z.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, z.useId = function() {
    return E.H.useId();
  }, z.useImperativeHandle = function(d, g, F) {
    return E.H.useImperativeHandle(d, g, F);
  }, z.useInsertionEffect = function(d, g) {
    return E.H.useInsertionEffect(d, g);
  }, z.useLayoutEffect = function(d, g) {
    return E.H.useLayoutEffect(d, g);
  }, z.useMemo = function(d, g) {
    return E.H.useMemo(d, g);
  }, z.useOptimistic = function(d, g) {
    return E.H.useOptimistic(d, g);
  }, z.useReducer = function(d, g, F) {
    return E.H.useReducer(d, g, F);
  }, z.useRef = function(d) {
    return E.H.useRef(d);
  }, z.useState = function(d) {
    return E.H.useState(d);
  }, z.useSyncExternalStore = function(d, g, F) {
    return E.H.useSyncExternalStore(
      d,
      g,
      F
    );
  }, z.useTransition = function() {
    return E.H.useTransition();
  }, z.version = "19.2.4", z;
}
var nt;
function rl() {
  return nt || (nt = 1, Be.exports = ll()), Be.exports;
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
function al() {
  if (lt) return ce;
  lt = 1;
  var l = rl();
  function t(s) {
    var r = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      r += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        r += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + r + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function a() {
  }
  var n = {
    d: {
      f: a,
      r: function() {
        throw Error(t(522));
      },
      D: a,
      C: a,
      L: a,
      m: a,
      X: a,
      S: a,
      M: a
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function i(s, r, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: s,
      containerInfo: r,
      implementation: m
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(s, r) {
    if (s === "font") return "";
    if (typeof r == "string")
      return r === "use-credentials" ? r : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, ce.createPortal = function(s, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return i(s, r, null, m);
  }, ce.flushSync = function(s) {
    var r = o.T, m = n.p;
    try {
      if (o.T = null, n.p = 2, s) return s();
    } finally {
      o.T = r, n.p = m, n.d.f();
    }
  }, ce.preconnect = function(s, r) {
    typeof s == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, n.d.C(s, r));
  }, ce.prefetchDNS = function(s) {
    typeof s == "string" && n.d.D(s);
  }, ce.preinit = function(s, r) {
    if (typeof s == "string" && r && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin), _ = typeof r.integrity == "string" ? r.integrity : void 0, N = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? n.d.S(
        s,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: _,
          fetchPriority: N
        }
      ) : m === "script" && n.d.X(s, {
        crossOrigin: p,
        integrity: _,
        fetchPriority: N,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, ce.preinitModule = function(s, r) {
    if (typeof s == "string")
      if (typeof r == "object" && r !== null) {
        if (r.as == null || r.as === "script") {
          var m = u(
            r.as,
            r.crossOrigin
          );
          n.d.M(s, {
            crossOrigin: m,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && n.d.M(s);
  }, ce.preload = function(s, r) {
    if (typeof s == "string" && typeof r == "object" && r !== null && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin);
      n.d.L(s, m, {
        crossOrigin: p,
        integrity: typeof r.integrity == "string" ? r.integrity : void 0,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0,
        type: typeof r.type == "string" ? r.type : void 0,
        fetchPriority: typeof r.fetchPriority == "string" ? r.fetchPriority : void 0,
        referrerPolicy: typeof r.referrerPolicy == "string" ? r.referrerPolicy : void 0,
        imageSrcSet: typeof r.imageSrcSet == "string" ? r.imageSrcSet : void 0,
        imageSizes: typeof r.imageSizes == "string" ? r.imageSizes : void 0,
        media: typeof r.media == "string" ? r.media : void 0
      });
    }
  }, ce.preloadModule = function(s, r) {
    if (typeof s == "string")
      if (r) {
        var m = u(r.as, r.crossOrigin);
        n.d.m(s, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else n.d.m(s);
  }, ce.requestFormReset = function(s) {
    n.d.r(s);
  }, ce.unstable_batchedUpdates = function(s, r) {
    return s(r);
  }, ce.useFormState = function(s, r, m) {
    return o.H.useFormState(s, r, m);
  }, ce.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var rt;
function ol() {
  if (rt) return Ae.exports;
  rt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ae.exports = al(), Ae.exports;
}
var sl = ol();
const { useState: me, useCallback: ie, useRef: Ee, useEffect: he, useMemo: We } = e;
function Ke({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function cl({
  option: l,
  removable: t,
  onRemove: a,
  removeLabel: n,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: u,
  onDragEnd: s,
  dragClassName: r
}) {
  const m = ie(
    (p) => {
      p.stopPropagation(), a(l.value);
    },
    [a, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (r ? " " + r : ""),
      draggable: c || void 0,
      onDragStart: i,
      onDragOver: o,
      onDrop: u,
      onDragEnd: s
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Ke, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": n
      },
      "×"
    )
  );
}
function il({
  option: l,
  highlighted: t,
  searchTerm: a,
  onSelect: n,
  onMouseEnter: c,
  id: i
}) {
  const o = ie(() => n(l.value), [n, l.value]), u = We(() => {
    if (!a) return l.label;
    const s = l.label.toLowerCase().indexOf(a.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + a.length)), l.label.substring(s + a.length));
  }, [l.label, a]);
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
    /* @__PURE__ */ e.createElement(Ke, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const ul = ({ controlId: l, state: t }) => {
  const a = le(), n = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", _ = i && c && !u && s, N = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = N["js.dropdownSelect.nothingFound"], v = ie(
    (w) => N["js.dropdownSelect.removeChip"].replace("{0}", w),
    [N]
  ), [h, S] = me(!1), [y, B] = me(""), [j, f] = me(-1), [C, E] = me(!1), [V, T] = me({}), [L, A] = me(null), [R, I] = me(null), [O, ee] = me(null), Z = Ee(null), re = Ee(null), $ = Ee(null), P = Ee(n);
  P.current = n;
  const q = Ee(-1), d = We(
    () => new Set(n.map((w) => w.value)),
    [n]
  ), g = We(() => {
    let w = m.filter((D) => !d.has(D.value));
    if (y) {
      const D = y.toLowerCase();
      w = w.filter((H) => H.label.toLowerCase().includes(D));
    }
    return w;
  }, [m, d, y]);
  he(() => {
    y && g.length === 1 ? f(0) : f(-1);
  }, [g.length, y]), he(() => {
    h && r && re.current && re.current.focus();
  }, [h, r, n]), he(() => {
    var H, J;
    if (q.current < 0) return;
    const w = q.current;
    q.current = -1;
    const D = (H = Z.current) == null ? void 0 : H.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    D && D.length > 0 ? D[Math.min(w, D.length - 1)].focus() : (J = Z.current) == null || J.focus();
  }, [n]), he(() => {
    if (!h) return;
    const w = (D) => {
      Z.current && !Z.current.contains(D.target) && $.current && !$.current.contains(D.target) && (S(!1), B(""));
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [h]), he(() => {
    if (!h || !Z.current) return;
    const w = Z.current.getBoundingClientRect(), D = window.innerHeight - w.bottom, J = D < 300 && w.top > D;
    T({
      left: w.left,
      width: w.width,
      ...J ? { bottom: window.innerHeight - w.top } : { top: w.bottom }
    });
  }, [h]);
  const F = ie(async () => {
    if (!(u || !s) && (S(!0), B(""), f(-1), E(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, s, r, a]), M = ie(() => {
    var w;
    S(!1), B(""), f(-1), (w = Z.current) == null || w.focus();
  }, []), K = ie(
    (w) => {
      let D;
      if (c) {
        const H = m.find((J) => J.value === w);
        if (H)
          D = [...P.current, H];
        else
          return;
      } else {
        const H = m.find((J) => J.value === w);
        if (H)
          D = [H];
        else
          return;
      }
      P.current = D, a("valueChanged", { value: D.map((H) => H.value) }), c ? (B(""), f(-1)) : M();
    },
    [c, m, a, M]
  ), X = ie(
    (w) => {
      q.current = P.current.findIndex((H) => H.value === w);
      const D = P.current.filter((H) => H.value !== w);
      P.current = D, a("valueChanged", { value: D.map((H) => H.value) });
    },
    [a]
  ), Q = ie(
    (w) => {
      w.stopPropagation(), a("valueChanged", { value: [] }), M();
    },
    [a, M]
  ), se = ie((w) => {
    B(w.target.value);
  }, []), ae = ie(
    (w) => {
      if (!h) {
        if (w.key === "ArrowDown" || w.key === "ArrowUp" || w.key === "Enter" || w.key === " ") {
          if (w.target.tagName === "BUTTON") return;
          w.preventDefault(), w.stopPropagation(), F();
        }
        return;
      }
      switch (w.key) {
        case "ArrowDown":
          w.preventDefault(), w.stopPropagation(), f(
            (D) => D < g.length - 1 ? D + 1 : 0
          );
          break;
        case "ArrowUp":
          w.preventDefault(), w.stopPropagation(), f(
            (D) => D > 0 ? D - 1 : g.length - 1
          );
          break;
        case "Enter":
          w.preventDefault(), w.stopPropagation(), j >= 0 && j < g.length && K(g[j].value);
          break;
        case "Escape":
          w.preventDefault(), w.stopPropagation(), M();
          break;
        case "Tab":
          M();
          break;
        case "Backspace":
          y === "" && c && n.length > 0 && X(n[n.length - 1].value);
          break;
      }
    },
    [
      h,
      F,
      M,
      g,
      j,
      K,
      y,
      c,
      n,
      X
    ]
  ), fe = ie(
    async (w) => {
      w.preventDefault(), E(!1);
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
    },
    [a]
  ), De = ie(
    (w, D) => {
      A(w), D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", String(w));
    },
    []
  ), k = ie(
    (w, D) => {
      if (D.preventDefault(), D.dataTransfer.dropEffect = "move", L === null || L === w) {
        I(null), ee(null);
        return;
      }
      const H = D.currentTarget.getBoundingClientRect(), J = H.left + H.width / 2, ve = D.clientX < J ? "before" : "after";
      I(w), ee(ve);
    },
    [L]
  ), x = ie(
    (w) => {
      if (w.preventDefault(), L === null || R === null || O === null || L === R) return;
      const D = [...P.current], [H] = D.splice(L, 1);
      let J = R;
      L < R ? J = O === "before" ? J - 1 : J : J = O === "before" ? J : J + 1, D.splice(J, 0, H), P.current = D, a("valueChanged", { value: D.map((ve) => ve.value) }), A(null), I(null), ee(null);
    },
    [L, R, O, a]
  ), U = ie(() => {
    A(null), I(null), ee(null);
  }, []);
  if (he(() => {
    if (j < 0 || !$.current) return;
    const w = $.current.querySelector(
      `[id="${l}-opt-${j}"]`
    );
    w && w.scrollIntoView({ block: "nearest" });
  }, [j, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : n.map((w) => /* @__PURE__ */ e.createElement("span", { key: w.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ke, { image: w.image }), /* @__PURE__ */ e.createElement("span", null, w.label))));
  const te = !o && n.length > 0 && !u, ne = h ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: $,
      className: "tlDropdownSelect__dropdown",
      style: V
    },
    (r || C) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: re,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: se,
        onKeyDown: ae,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": j >= 0 ? `${l}-opt-${j}` : void 0,
        "aria-controls": `${l}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${l}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !r && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: fe }, N["js.dropdownSelect.error"])),
      r && g.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      r && g.map((w, D) => /* @__PURE__ */ e.createElement(
        il,
        {
          key: w.value,
          id: `${l}-opt-${D}`,
          option: w,
          highlighted: D === j,
          searchTerm: y,
          onSelect: K,
          onMouseEnter: () => f(D)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: Z,
      className: "tlDropdownSelect" + (h ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": h,
      "aria-haspopup": "listbox",
      "aria-owns": h ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: h ? void 0 : F,
      onKeyDown: ae
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : n.map((w, D) => {
      let H = "";
      return L === D ? H = "tlDropdownSelect__chip--dragging" : R === D && O === "before" ? H = "tlDropdownSelect__chip--dropBefore" : R === D && O === "after" && (H = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        cl,
        {
          key: w.value,
          option: w,
          removable: !u && (c || !o),
          onRemove: X,
          removeLabel: v(w.label),
          draggable: _,
          onDragStart: _ ? (J) => De(D, J) : void 0,
          onDragOver: _ ? (J) => k(D, J) : void 0,
          onDrop: _ ? x : void 0,
          onDragEnd: _ ? U : void 0,
          dragClassName: _ ? H : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, te && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": N["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, h ? "▲" : "▼"))
  ), ne && sl.createPortal(ne, document.body));
}, { useCallback: Oe, useRef: dl } = e, it = "application/x-tl-color", ml = ({
  colors: l,
  columns: t,
  onSelect: a,
  onConfirm: n,
  onSwap: c,
  onReplace: i
}) => {
  const o = dl(null), u = Oe(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = Oe((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = Oe(
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
    l.map((m, p) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: p,
        className: "tlColorInput__paletteCell" + (m == null ? " tlColorInput__paletteCell--empty" : ""),
        style: m != null ? { backgroundColor: m } : void 0,
        title: m ?? "",
        draggable: m != null,
        onClick: m != null ? () => a(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: s,
        onDrop: r(p)
      }
    ))
  );
};
function ut(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ze(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function dt(l) {
  if (!ze(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function mt(l, t, a) {
  const n = (c) => ut(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(a);
}
function pl(l, t, a) {
  const n = l / 255, c = t / 255, i = a / 255, o = Math.max(n, c, i), u = Math.min(n, c, i), s = o - u;
  let r = 0;
  s !== 0 && (o === n ? r = (c - i) / s % 6 : o === c ? r = (i - n) / s + 2 : r = (n - c) / s + 4, r *= 60, r < 0 && (r += 360));
  const m = o === 0 ? 0 : s / o;
  return [r, m, o];
}
function fl(l, t, a) {
  const n = a * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), i = a - n;
  let o = 0, u = 0, s = 0;
  return l < 60 ? (o = n, u = c, s = 0) : l < 120 ? (o = c, u = n, s = 0) : l < 180 ? (o = 0, u = n, s = c) : l < 240 ? (o = 0, u = c, s = n) : l < 300 ? (o = c, u = 0, s = n) : (o = n, u = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((u + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function hl(l) {
  return pl(...dt(l));
}
function $e(l, t, a) {
  return mt(...fl(l, t, a));
}
const { useCallback: _e, useRef: at } = e, _l = ({ color: l, onColorChange: t }) => {
  const [a, n, c] = hl(l), i = at(null), o = at(null), u = _e(
    (b, v) => {
      var B;
      const h = (B = i.current) == null ? void 0 : B.getBoundingClientRect();
      if (!h) return;
      const S = Math.max(0, Math.min(1, (b - h.left) / h.width)), y = Math.max(0, Math.min(1, 1 - (v - h.top) / h.height));
      t($e(a, S, y));
    },
    [a, t]
  ), s = _e(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), r = _e(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = _e(
    (b) => {
      var y;
      const v = (y = o.current) == null ? void 0 : y.getBoundingClientRect();
      if (!v) return;
      const S = Math.max(0, Math.min(1, (b - v.top) / v.height)) * 360;
      t($e(S, n, c));
    },
    [n, c, t]
  ), p = _e(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), _ = _e(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), N = $e(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: N },
      onPointerDown: s,
      onPointerMove: r
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - c) * 100}%` }
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
        style: { top: `${a / 360 * 100}%` }
      }
    )
  ));
};
function bl(l, t) {
  const a = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === a);
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
  anchorRef: l,
  currentColor: t,
  palette: a,
  paletteColumns: n,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: u,
  onPaletteChange: s
}) => {
  const [r, m] = we("palette"), [p, _] = we(t), N = El(null), b = oe(vl), [v, h] = we(null);
  gl(() => {
    if (!l.current || !N.current) return;
    const $ = l.current.getBoundingClientRect(), P = N.current.getBoundingClientRect();
    let q = $.bottom + 4, d = $.left;
    q + P.height > window.innerHeight && (q = $.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, $.right - P.width)), h({ top: q, left: d });
  }, [l]);
  const S = p != null, [y, B, j] = S ? dt(p) : [0, 0, 0], [f, C] = we((p == null ? void 0 : p.toUpperCase()) ?? "");
  Fe(() => {
    C((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Fe(() => {
    const $ = (P) => {
      P.key === "Escape" && u();
    };
    return document.addEventListener("keydown", $), () => document.removeEventListener("keydown", $);
  }, [u]), Fe(() => {
    const $ = (q) => {
      N.current && !N.current.contains(q.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", $), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", $);
    };
  }, [u]);
  const E = de(
    ($) => (P) => {
      const q = parseInt(P.target.value, 10);
      if (isNaN(q)) return;
      const d = ut(q);
      _(mt($ === "r" ? d : y, $ === "g" ? d : B, $ === "b" ? d : j));
    },
    [y, B, j]
  ), V = de(
    ($) => {
      if (p != null) {
        $.dataTransfer.setData(it, p.toUpperCase()), $.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = p, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), $.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [p]
  ), T = de(($) => {
    const P = $.target.value;
    C(P), ze(P) && _(P);
  }, []), L = de(() => {
    _(null);
  }, []), A = de(($) => {
    _($);
  }, []), R = de(
    ($) => {
      o($);
    },
    [o]
  ), I = de(
    ($, P) => {
      const q = [...a], d = q[$];
      q[$] = q[P], q[P] = d, s(q);
    },
    [a, s]
  ), O = de(
    ($, P) => {
      const q = [...a];
      q[$] = P, s(q);
    },
    [a, s]
  ), ee = de(() => {
    s([...c]);
  }, [c, s]), Z = de(
    ($) => {
      if (bl(a, $)) return;
      const P = a.indexOf(null);
      if (P < 0) return;
      const q = [...a];
      q[P] = $.toUpperCase(), s(q);
    },
    [a, s]
  ), re = de(() => {
    p != null && Z(p), o(p);
  }, [p, o, Z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: N,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      ml,
      {
        colors: a,
        columns: n,
        onSelect: A,
        onConfirm: R,
        onSwap: I,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(_l, { color: p ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (S ? "" : " tlColorInput--noColor"),
        style: S ? { backgroundColor: p } : void 0,
        draggable: S,
        onDragStart: S ? V : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? y : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? B : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? j : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (f !== "" && !ze(f) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: f,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: re }, b["js.colorInput.ok"]))
  );
}, yl = { "js.colorInput.chooseColor": "Choose color" }, { useState: wl, useCallback: ke, useRef: kl } = e, Sl = ({ controlId: l, state: t }) => {
  const a = le(), n = oe(yl), [c, i] = wl(!1), o = kl(null), u = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, _ = ke(() => {
    s && i(!0);
  }, [s]), N = ke(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), b = ke(() => {
    i(!1);
  }, []), v = ke(
    (h) => {
      a("paletteChanged", { palette: h });
    },
    [a]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Cl,
    {
      anchorRef: o,
      currentColor: u,
      palette: r,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: b,
      onPaletteChange: v
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
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
function xe({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const a = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: a + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const a = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: a + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const Ll = ({
  anchorRef: l,
  currentValue: t,
  icons: a,
  iconsLoaded: n,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const u = oe(Rl), [s, r] = ge("simple"), [m, p] = ge(""), [_, N] = ge(t ?? ""), [b, v] = ge(!1), [h, S] = ge(null), y = ot(null), B = ot(null);
  Nl(() => {
    if (!l.current || !y.current) return;
    const R = l.current.getBoundingClientRect(), I = y.current.getBoundingClientRect();
    let O = R.bottom + 4, ee = R.left;
    O + I.height > window.innerHeight && (O = R.top - I.height - 4), ee + I.width > window.innerWidth && (ee = Math.max(0, R.right - I.width)), S({ top: O, left: ee });
  }, [l]), Se(() => {
    !n && !b && o().catch(() => v(!0));
  }, [n, b, o]), Se(() => {
    n && B.current && B.current.focus();
  }, [n]), Se(() => {
    const R = (I) => {
      I.key === "Escape" && i();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [i]), Se(() => {
    const R = (O) => {
      y.current && !y.current.contains(O.target) && i();
    }, I = setTimeout(() => document.addEventListener("mousedown", R), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", R);
    };
  }, [i]);
  const j = Tl(() => {
    if (!m) return a;
    const R = m.toLowerCase();
    return a.filter(
      (I) => I.prefix.toLowerCase().includes(R) || I.label.toLowerCase().includes(R) || I.terms != null && I.terms.some((O) => O.includes(R))
    );
  }, [a, m]), f = pe((R) => {
    p(R.target.value);
  }, []), C = pe(
    (R) => {
      c(R);
    },
    [c]
  ), E = pe((R) => {
    N(R);
  }, []), V = pe((R) => {
    N(R.target.value);
  }, []), T = pe(() => {
    c(_ || null);
  }, [_, c]), L = pe(() => {
    c(null);
  }, [c]), A = pe(async (R) => {
    R.preventDefault(), v(!1);
    try {
      await o();
    } catch {
      v(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: y,
      style: h ? { top: h.top, left: h.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => r("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => r("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: f,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), m && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => p(""),
        title: u["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !n && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: A }, u["js.iconSelect.loadError"])),
      n && j.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      n && j.map(
        (R) => R.variants.map((I) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: I.encoded,
            className: "tlIconSelect__iconCell" + (I.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": I.encoded === t,
            tabIndex: 0,
            title: R.label,
            onClick: () => s === "simple" ? C(I.encoded) : E(I.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), s === "simple" ? C(I.encoded) : E(I.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(xe, { encoded: I.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: V
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(xe, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: L }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, xl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Dl, useCallback: Ne, useRef: Il } = e, jl = ({ controlId: l, state: t }) => {
  const a = le(), n = oe(xl), [c, i] = Dl(!1), o = Il(null), u = t.value, s = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, _ = Ne(() => {
    s && !r && i(!0);
  }, [s, r]), N = Ne(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), b = Ne(() => {
    i(!1);
  }, []), v = Ne(async () => {
    await a("loadIcons");
  }, [a]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: r,
      title: u ?? "",
      "aria-label": n["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(xe, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ll,
    {
      anchorRef: o,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: N,
      onCancel: b,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(xe, { encoded: u }) : null));
};
W("TLButton", Tt);
W("TLToggleButton", Lt);
W("TLTextInput", _t);
W("TLNumberInput", vt);
W("TLDatePicker", gt);
W("TLSelect", yt);
W("TLCheckbox", kt);
W("TLTable", St);
W("TLCounter", xt);
W("TLTabBar", It);
W("TLFieldList", jt);
W("TLAudioRecorder", Mt);
W("TLAudioPlayer", Bt);
W("TLFileUpload", $t);
W("TLDownload", Ht);
W("TLPhotoCapture", zt);
W("TLPhotoViewer", Vt);
W("TLSplitPanel", Kt);
W("TLPanel", Jt);
W("TLMaximizeRoot", en);
W("TLDeckPane", tn);
W("TLSidebar", un);
W("TLStack", dn);
W("TLGrid", mn);
W("TLCard", pn);
W("TLAppBar", fn);
W("TLBreadcrumb", _n);
W("TLBottomBar", vn);
W("TLDialog", gn);
W("TLDialogManager", wn);
W("TLWindow", Nn);
W("TLDrawer", xn);
W("TLSnackbar", jn);
W("TLMenu", Mn);
W("TLAppShell", An);
W("TLTextCell", Bn);
W("TLTableView", $n);
W("TLFormLayout", Kn);
W("TLFormGroup", Xn);
W("TLFormField", Jn);
W("TLResourceCell", el);
W("TLTreeView", nl);
W("TLDropdownSelect", ul);
W("TLColorInput", Sl);
W("TLIconSelect", jl);
