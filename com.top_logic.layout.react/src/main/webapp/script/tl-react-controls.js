import { React as e, useTLFieldValue as ke, getComponent as gt, useTLState as G, useTLCommand as ne, TLChild as Y, useTLUpload as Ye, useI18N as oe, useTLDataUrl as Ge, register as H } from "tl-react-bridge";
const { useCallback: Ct } = e, yt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = Ct(
    (r) => {
      l(r.target.value);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactTextInput tlReactTextInput--immutable" }, a ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = t.errorMessage, s = [
    "tlReactTextInput",
    i ? "tlReactTextInput--error" : "",
    !i && o ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: a ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: s,
      "aria-invalid": i || void 0,
      title: i && u ? u : void 0
    }
  ));
}, { useCallback: wt } = e, kt = ({ controlId: n, state: t, config: a }) => {
  const [l, c] = ke(), i = wt(
    (m) => {
      const p = m.target.value;
      c(p === "" ? null : p);
    },
    [c]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "");
  const o = t.hasError === !0, u = t.hasWarnings === !0, s = t.errorMessage, r = [
    "tlReactNumberInput",
    o ? "tlReactNumberInput--error" : "",
    !o && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: a != null && a.decimal ? "decimal" : "numeric",
      value: l != null ? String(l) : "",
      onChange: i,
      disabled: t.disabled === !0,
      className: r,
      "aria-invalid": o || void 0,
      title: o && s ? s : void 0
    }
  ));
}, { useCallback: St } = e, Nt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = St(
    (s) => {
      l(s.target.value || null);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactDatePicker tlReactDatePicker--immutable" }, a ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && o ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
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
}, { useCallback: Tt } = e, Rt = ({ controlId: n, state: t, config: a }) => {
  var m;
  const [l, c] = ke(), i = Tt(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = o.find((_) => _.value === l)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: n, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, s = t.hasWarnings === !0, r = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && s ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: n }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: r,
      "aria-invalid": u || void 0
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Lt } = e, xt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = Lt(
    (s) => {
      l(s.target.checked);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: n,
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
      id: n,
      checked: a === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  );
}, Dt = ({ controlId: n, state: t }) => {
  const a = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: n, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, a.map((o) => {
    const u = o.cellModule ? gt(o.cellModule) : void 0, s = c[o.name];
    if (u) {
      const r = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: n + "-" + i + "-" + o.name,
          state: r
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
};
function we({ encoded: n, className: t }) {
  if (n.startsWith("css:")) {
    const a = n.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: a + (t ? " " + t : "") });
  }
  if (n.startsWith("colored:")) {
    const a = n.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: a + (t ? " " + t : "") });
  }
  return n.startsWith("/") || n.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
}
const { useCallback: It } = e, jt = ({ controlId: n, command: t, label: a, disabled: l }) => {
  const c = G(), i = ne(), o = t ?? "click", u = a ?? c.label, s = l ?? c.disabled === !0, r = c.hidden === !0, m = c.image, p = r ? { display: "none" } : void 0, _ = It(() => {
    i(o);
  }, [i, o]);
  return m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: _,
      disabled: s,
      style: p,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: _,
      disabled: s,
      style: p,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: Pt } = e, Mt = ({ controlId: n, command: t, label: a, active: l, disabled: c }) => {
  const i = G(), o = ne(), u = t ?? "click", s = a ?? i.label, r = l ?? i.active === !0, m = c ?? i.disabled === !0, p = Pt(() => {
    o(u);
  }, [o, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (r ? " tlReactButtonActive" : "")
    },
    s
  );
}, At = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Bt } = e, Ot = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.tabs ?? [], c = t.activeTabId, i = Bt((o) => {
    o !== c && a("selectTab", { tabId: o });
  }, [a, c]);
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
}, $t = ({ controlId: n }) => {
  const t = G(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Ft = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ht = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, _ = m === "received" ? "idle" : l !== "idle" ? l : m, N = e.useCallback(async () => {
    if (l === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (l !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = w, s.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", I = new MediaRecorder(w, M ? { mimeType: M } : void 0);
        u.current = I, I.ondataavailable = (h) => {
          h.data.size > 0 && s.current.push(h.data);
        }, I.onstop = async () => {
          w.getTracks().forEach((E) => E.stop()), r.current = null;
          const h = new Blob(s.current, { type: I.mimeType || "audio/webm" });
          if (s.current = [], h.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const C = new FormData();
          C.append("audio", h, "recording.webm"), await a(C), c("idle");
        }, I.start(), c("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, a]), v = oe(Ft), b = _ === "recording" ? v["js.audioRecorder.stop"] : _ === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], f = _ === "uploading", S = ["tlAudioRecorder__button"];
  return _ === "recording" && S.push("tlAudioRecorder__button--recording"), _ === "uploading" && S.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: N,
      disabled: f,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, zt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Wt = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
  e.useEffect(() => {
    l ? i === "disabled" && o("idle") : (u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [l]), e.useEffect(() => {
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
        const f = await fetch(a);
        if (!f.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", f.status), o("idle");
          return;
        }
        const S = await f.blob();
        s.current = URL.createObjectURL(S);
      } catch (f) {
        console.error("[TLAudioPlayer] Fetch error:", f), o("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    u.current = b, b.onended = () => {
      o("idle");
    }, b.play(), o("playing");
  }, [i, a]), p = oe(zt), _ = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = i === "disabled" || i === "loading", v = ["tlAudioPlayer__button"];
  return i === "playing" && v.push("tlAudioPlayer__button--playing"), i === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: m,
      disabled: N,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Ut = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Vt = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, _ = e.useCallback(async (h) => {
    c("uploading");
    const C = new FormData();
    C.append("file", h, h.name), await a(C), c("idle");
  }, [a]), N = e.useCallback((h) => {
    var E;
    const C = (E = h.target.files) == null ? void 0 : E[0];
    C && _(C);
  }, [_]), v = e.useCallback(() => {
    var h;
    l !== "uploading" && ((h = u.current) == null || h.click());
  }, [l]), b = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!0);
  }, []), f = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!1);
  }, []), S = e.useCallback((h) => {
    var E;
    if (h.preventDefault(), h.stopPropagation(), o(!1), l === "uploading") return;
    const C = (E = h.dataTransfer.files) == null ? void 0 : E[0];
    C && _(C);
  }, [l, _]), w = p === "uploading", M = oe(Ut), I = p === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: f,
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
        onClick: v,
        disabled: w,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, Kt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Yt = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = ne(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const v = a + (a.includes("?") ? "&" : "?") + "rev=" + i, b = await fetch(v);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const f = await b.blob(), S = URL.createObjectURL(f), w = document.createElement("a");
        w.href = S, w.download = o, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(S);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), _ = oe(Kt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const N = s ? _["js.downloading"] : _["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
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
}, Gt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Xt = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), _ = e.useRef(null), N = e.useRef(null), v = t.error, b = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), S = e.useCallback(() => {
    f(), c("idle");
  }, [f]), w = e.useCallback(async () => {
    var L;
    if (l !== "uploading") {
      if (o(null), !b) {
        (L = _.current) == null || L.click();
        return;
      }
      try {
        const B = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = B, c("overlayOpen");
      } catch (B) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", B), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [l, b]), M = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const L = r.current, B = p.current;
    if (!L || !B)
      return;
    B.width = L.videoWidth, B.height = L.videoHeight;
    const R = B.getContext("2d");
    R && (R.drawImage(L, 0, 0), f(), c("uploading"), B.toBlob(async (j) => {
      if (!j) {
        c("idle");
        return;
      }
      const P = new FormData();
      P.append("photo", j, "capture.jpg"), await a(P), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, a, f]), I = e.useCallback(async (L) => {
    var j;
    const B = (j = L.target.files) == null ? void 0 : j[0];
    if (!B) return;
    c("uploading");
    const R = new FormData();
    R.append("photo", B, B.name), await a(R), c("idle"), _.current && (_.current.value = "");
  }, [a]);
  e.useEffect(() => {
    l === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var B;
    if (l !== "overlayOpen") return;
    (B = N.current) == null || B.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const L = (B) => {
      B.key === "Escape" && S();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [l, S]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const h = oe(Gt), C = l === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  u && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: w,
      disabled: l === "uploading",
      title: C,
      "aria-label": C
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
      onChange: I
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        title: h["js.photoCapture.mirror"],
        "aria-label": h["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: M,
        title: h["js.photoCapture.capture"],
        "aria-label": h["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: S,
        title: h["js.photoCapture.close"],
        "aria-label": h["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h[i]), v && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v));
}, qt = {
  "js.photoViewer.alt": "Captured photo"
}, Zt = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!l) {
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
  }, [l, c, a]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = oe(qt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Je, useRef: Me } = e, Qt = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", u = i.length > 0 && i.every((f) => f.collapsed), s = !u && i.some((f) => f.collapsed), r = u ? !o : o, m = Me(null), p = Me(null), _ = Me(null), N = Je((f, S) => {
    const w = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? u && !r ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : S !== void 0 ? w.flex = `0 0 ${S}px` : f.unit === "%" || s ? w.flex = `${f.size} 0 0%` : w.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (w.minWidth = o ? f.minSize : void 0, w.minHeight = o ? void 0 : f.minSize), w;
  }, [o, u, s, r]), v = Je((f, S) => {
    f.preventDefault();
    const w = m.current;
    if (!w) return;
    const M = i[S], I = i[S + 1], h = w.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    h.forEach((T) => {
      C.push(o ? T.offsetWidth : T.offsetHeight);
    }), _.current = C, p.current = {
      splitterIndex: S,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: C[S],
      startSizeAfter: C[S + 1],
      childBefore: M,
      childAfter: I
    };
    const E = (T) => {
      const L = p.current;
      if (!L || !_.current) return;
      const R = (o ? T.clientX : T.clientY) - L.startPos, j = L.childBefore.minSize || 0, P = L.childAfter.minSize || 0;
      let te = L.startSizeBefore + R, Z = L.startSizeAfter - R;
      te < j && (Z += te - j, te = j), Z < P && (te += Z - P, Z = P), _.current[L.splitterIndex] = te, _.current[L.splitterIndex + 1] = Z;
      const re = w.querySelectorAll(":scope > .tlSplitPanel__child"), $ = re[L.splitterIndex], O = re[L.splitterIndex + 1];
      $ && ($.style.flex = `0 0 ${te}px`), O && (O.style.flex = `0 0 ${Z}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const T = {};
        i.forEach((L, B) => {
          const R = L.control;
          R != null && R.controlId && _.current && (T[R.controlId] = _.current[B]);
        }), a("updateSizes", { sizes: T });
      }
      _.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", V), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), b = [];
  return i.forEach((f, S) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${S}`,
          className: `tlSplitPanel__child${f.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(f)
        },
        /* @__PURE__ */ e.createElement(Y, { control: f.control })
      )
    ), c && S < i.length - 1) {
      const w = i[S + 1];
      !f.collapsed && !w.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${S}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (I) => v(I, S)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: n,
      className: `tlSplitPanel tlSplitPanel--${l}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: r ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    b
  );
}, { useCallback: Ae } = e, Jt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, en = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), an = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(Jt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, r = t.fullLine === !0, m = t.toolbarButtons ?? [], p = i === "MINIMIZED", _ = i === "MAXIMIZED", N = i === "HIDDEN", v = Ae(() => {
    a("toggleMinimize");
  }, [a]), b = Ae(() => {
    a("toggleMaximize");
  }, [a]), f = Ae(() => {
    a("popOut");
  }, [a]);
  if (N)
    return null;
  const S = _ ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}${r ? " tlPanel--fullLine" : ""}`,
      style: S
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((w, M) => /* @__PURE__ */ e.createElement("span", { key: M, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: w }))), o && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(tn, null) : /* @__PURE__ */ e.createElement(en, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: _ ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      _ ? /* @__PURE__ */ e.createElement(ln, null) : /* @__PURE__ */ e.createElement(nn, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(rn, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child }))
  );
}, on = ({ controlId: n }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, sn = ({ controlId: n }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: De, useEffect: Ie, useRef: je } = e, cn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ue(n, t, a, l) {
  const c = [];
  for (const i of n)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: l }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: l }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (a.get(i.id) ?? i.expanded) && !t && c.push(...Ue(i.children, t, a, i.id)));
  return c;
}
const Ee = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, un = ({ item: n, active: t, collapsed: a, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: a ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  a && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
  !a && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
), dn = ({ item: n, collapsed: t, onExecute: a, tabIndex: l, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(n.id),
    title: t ? n.label : void 0,
    tabIndex: l,
    ref: c,
    onFocus: () => i(n.id)
  },
  /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), mn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), pn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), fn = ({ item: n, activeItemId: t, anchorRect: a, onSelect: l, onExecute: c, onClose: i }) => {
  const o = je(null);
  Ie(() => {
    const r = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [i]), Ie(() => {
    const r = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [i]);
  const u = ue((r) => {
    r.type === "nav" ? (l(r.id), i()) : r.type === "command" && (c(r.id), i());
  }, [l, c, i]), s = {};
  return a && (s.left = a.right, s.top = a.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, n.label), n.children.map((r) => {
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
        /* @__PURE__ */ e.createElement(Ee, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, hn = ({
  item: n,
  expanded: t,
  activeItemId: a,
  collapsed: l,
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
  onOpenFlyout: v,
  onCloseFlyout: b
}) => {
  const f = je(null), [S, w] = De(null), M = ue(() => {
    l ? N === n.id ? b() : (f.current && w(f.current.getBoundingClientRect()), v(n.id)) : o(n.id);
  }, [l, N, n.id, o, v, b]), I = ue((C) => {
    f.current = C, s(C);
  }, [s]), h = l && N === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: l ? n.label : void 0,
      "aria-expanded": l ? h : t,
      tabIndex: u,
      ref: I,
      onFocus: () => r(n.id)
    },
    /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
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
    fn,
    {
      item: n,
      activeItemId: a,
      anchorRect: S,
      onSelect: c,
      onExecute: i,
      onClose: b
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, n.children.map((C) => /* @__PURE__ */ e.createElement(
    pt,
    {
      key: C.id,
      item: C,
      activeItemId: a,
      collapsed: l,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: _,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: v,
      onCloseFlyout: b
    }
  ))));
}, pt = ({
  item: n,
  activeItemId: t,
  collapsed: a,
  onSelect: l,
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
  switch (n.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        un,
        {
          item: n,
          active: n.id === t,
          collapsed: a,
          onSelect: l,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: u(n.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        dn,
        {
          item: n,
          collapsed: a,
          onExecute: c,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: u(n.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(mn, { item: n, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(pn, null);
    case "group": {
      const N = r ? r.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        hn,
        {
          item: n,
          expanded: N,
          activeItemId: t,
          collapsed: a,
          onSelect: l,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === n.id ? 0 : -1,
          itemRef: u(n.id),
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
}, _n = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(cn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = De(() => {
    const T = /* @__PURE__ */ new Map(), L = (B) => {
      for (const R of B)
        R.type === "group" && (T.set(R.id, R.expanded), L(R.children));
    };
    return L(c), T;
  }), r = ue((T) => {
    s((L) => {
      const B = new Map(L), R = B.get(T) ?? !1;
      return B.set(T, !R), a("toggleGroup", { itemId: T, expanded: !R }), B;
    });
  }, [a]), m = ue((T) => {
    T !== i && a("selectItem", { itemId: T });
  }, [a, i]), p = ue((T) => {
    a("executeCommand", { itemId: T });
  }, [a]), _ = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [N, v] = De(null), b = ue((T) => {
    v(T);
  }, []), f = ue(() => {
    v(null);
  }, []);
  Ie(() => {
    o || v(null);
  }, [o]);
  const [S, w] = De(() => {
    const T = Ue(c, o, u);
    return T.length > 0 ? T[0].id : "";
  }), M = je(/* @__PURE__ */ new Map()), I = ue((T) => (L) => {
    L ? M.current.set(T, L) : M.current.delete(T);
  }, []), h = ue((T) => {
    w(T);
  }, []), C = je(0), E = ue((T) => {
    w(T), C.current++;
  }, []);
  Ie(() => {
    const T = M.current.get(S);
    T && document.activeElement !== T && T.focus();
  }, [S, C.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && N !== null) {
      T.preventDefault(), f();
      return;
    }
    const L = Ue(c, o, u);
    if (L.length === 0) return;
    const B = L.findIndex((j) => j.id === S);
    if (B < 0) return;
    const R = L[B];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const j = (B + 1) % L.length;
        E(L[j].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const j = (B - 1 + L.length) % L.length;
        E(L[j].id);
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
        T.preventDefault(), R.type === "nav" ? m(R.id) : R.type === "command" ? p(R.id) : R.type === "group" && (o ? N === R.id ? f() : b(R.id) : r(R.id));
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
    b,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((T) => /* @__PURE__ */ e.createElement(
    pt,
    {
      key: T.id,
      item: T,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: S,
      setItemRef: I,
      onItemFocus: h,
      groupStates: u,
      flyoutGroupId: N,
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
}, bn = ({ controlId: n }) => {
  const t = G(), a = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: u }, o.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })));
}, vn = ({ controlId: n }) => {
  const t = G(), a = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: u })));
}, En = ({ controlId: n }) => {
  const t = G(), a = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, gn = ({ controlId: n }) => {
  const t = G(), a = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: u }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s }))));
}, { useCallback: Cn } = e, yn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.items ?? [], c = Cn((i) => {
    a("navigate", { itemId: i });
  }, [a]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((i, o) => {
    const u = o === l.length - 1;
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
}, { useCallback: wn } = e, kn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.items ?? [], c = t.activeItemId, i = wn((o) => {
    o !== c && a("selectItem", { itemId: o });
  }, [a, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: n, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((o) => {
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
}, { useCallback: et, useEffect: tt, useRef: Sn } = e, Nn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = Sn(null), u = et(() => {
    a("close");
  }, [a]), s = et((r) => {
    c && r.target === r.currentTarget && u();
  }, [c, u]);
  return tt(() => {
    if (!l) return;
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [l, u]), tt(() => {
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
}, { useEffect: Tn, useRef: Rn } = e, Ln = ({ controlId: n }) => {
  const a = G().dialogs ?? [], l = Rn(a.length);
  return Tn(() => {
    a.length < l.current && a.length > 0, l.current = a.length;
  }, [a.length]), a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialogManager" }, a.map((c) => /* @__PURE__ */ e.createElement(Y, { key: c.controlId, control: c })));
}, { useCallback: nt, useRef: Ne, useState: lt } = e, xn = {
  "js.window.close": "Close"
}, Dn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], In = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(xn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.resizable === !0, s = t.child, r = t.actions ?? [], m = t.toolbarButtons ?? [], [p, _] = lt(null), [N, v] = lt(null), b = Ne(null), f = Ne(null), S = Ne(null), w = Ne(null), M = nt(() => {
    a("close");
  }, [a]), I = nt((E, V) => {
    V.preventDefault();
    const T = S.current;
    if (!T) return;
    const L = T.getBoundingClientRect();
    w.current = {
      dir: E,
      startX: V.clientX,
      startY: V.clientY,
      startW: L.width,
      startH: L.height
    };
    const B = (j) => {
      const P = w.current;
      if (!P) return;
      const te = j.clientX - P.startX, Z = j.clientY - P.startY;
      let re = P.startW, $ = P.startH;
      P.dir.includes("e") && (re = P.startW + te), P.dir.includes("w") && (re = P.startW - te), P.dir.includes("s") && ($ = P.startH + Z), P.dir.includes("n") && ($ = P.startH - Z);
      const O = Math.max(200, re), X = Math.max(100, $);
      b.current = O, f.current = X, _(O), v(X);
    }, R = () => {
      document.removeEventListener("mousemove", B), document.removeEventListener("mouseup", R);
      const j = b.current, P = f.current;
      (j != null || P != null) && (a("resize", {
        ...j != null ? { width: Math.round(j) + "px" } : {},
        ...P != null ? { height: Math.round(P) + "px" } : {}
      }), b.current = null, f.current = null, _(null), v(null)), w.current = null;
    };
    document.addEventListener("mousemove", B), document.addEventListener("mouseup", R);
  }, [a]), h = {
    width: p != null ? p + "px" : i,
    ...N != null ? { height: N + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, C = n + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlWindow",
      style: h,
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
        onClick: M,
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
    r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, r.map((E, V) => /* @__PURE__ */ e.createElement(Y, { key: V, control: E }))),
    u && Dn.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (V) => I(E, V)
      }
    ))
  );
}, { useCallback: jn, useEffect: Pn } = e, Mn = {
  "js.drawer.close": "Close"
}, An = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(Mn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, r = jn(() => {
    a("close");
  }, [a]);
  Pn(() => {
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
  return /* @__PURE__ */ e.createElement("aside", { id: n, className: m, "aria-hidden": !c }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: r,
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
}, { useCallback: rt, useEffect: Bn, useState: On } = e, $n = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, u = t.duration ?? 5e3, s = t.visible === !0, r = t.generation ?? 0, [m, p] = On(!1), _ = rt(() => {
    p(!0), setTimeout(() => {
      a("dismiss", { generation: r }), p(!1);
    }, 200);
  }, [a, r]), N = rt(() => {
    o && a(o.commandName), _();
  }, [a, o, _]);
  return Bn(() => {
    if (!s || u === 0) return;
    const v = setTimeout(_, u);
    return () => clearTimeout(v);
  }, [s, u, _]), console.log("[TLSnackbar] render", { visible: s, exiting: m, generation: r, content: c, message: l }), !s && !m ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlSnackbar tlSnackbar--${i}${m ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: N }, o.label)
  );
}, { useCallback: Be, useEffect: Oe, useRef: Fn, useState: at } = e, Hn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Fn(null), [u, s] = at({ top: 0, left: 0 }), [r, m] = at(0), p = i.filter((b) => b.type === "item" && !b.disabled);
  Oe(() => {
    var h, C;
    if (!l || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const f = b.getBoundingClientRect(), S = ((h = o.current) == null ? void 0 : h.offsetHeight) ?? 200, w = ((C = o.current) == null ? void 0 : C.offsetWidth) ?? 200;
    let M = f.bottom + 4, I = f.left;
    M + S > window.innerHeight && (M = f.top - S - 4), I + w > window.innerWidth && (I = f.right - w), s({ top: M, left: I }), m(0);
  }, [l, c]);
  const _ = Be(() => {
    a("close");
  }, [a]), N = Be((b) => {
    a("selectItem", { itemId: b });
  }, [a]);
  Oe(() => {
    if (!l) return;
    const b = (f) => {
      o.current && !o.current.contains(f.target) && _();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [l, _]);
  const v = Be((b) => {
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
      const f = p[r];
      f && N(f.id);
    }
  }, [_, N, p, r]);
  return Oe(() => {
    l && o.current && o.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: u.top, left: u.left },
      onKeyDown: v
    },
    i.map((b, f) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const w = p.indexOf(b) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (w ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: w ? 0 : -1,
          onClick: () => N(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, zn = ({ controlId: n }) => {
  const t = G(), a = t.header, l = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, Wn = () => {
  const t = G().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Un = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, ot = 50, Vn = () => {
  const n = G(), t = ne(), a = oe(Un), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, u = n.selectionMode ?? "single", s = n.selectedCount ?? 0, r = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((k) => k.sortPriority && k.sortPriority > 0).length,
    [l]
  ), _ = u === "multi", N = 40, v = 20, b = e.useRef(null), f = e.useRef(null), S = e.useRef(null), [w, M] = e.useState({}), I = e.useRef(null), h = e.useRef(!1), C = e.useRef(null), [E, V] = e.useState(null), [T, L] = e.useState(null);
  e.useEffect(() => {
    I.current || M({});
  }, [l]);
  const B = e.useCallback((k) => w[k.name] ?? k.width, [w]), R = e.useMemo(() => {
    const k = [];
    let x = _ && r > 0 ? N : 0;
    for (let U = 0; U < r && U < l.length; U++)
      k.push(x), x += B(l[U]);
    return k;
  }, [l, r, _, N, B]), j = c * o, P = e.useRef(null), te = e.useCallback((k, x, U) => {
    U.preventDefault(), U.stopPropagation(), I.current = { column: k, startX: U.clientX, startWidth: x };
    let Q = U.clientX, y = 0;
    const D = () => {
      const ae = I.current;
      if (!ae) return;
      const de = Math.max(ot, ae.startWidth + (Q - ae.startX) + y);
      M((_e) => ({ ..._e, [ae.column]: de }));
    }, z = () => {
      const ae = f.current, de = b.current;
      if (!ae || !I.current) return;
      const _e = ae.getBoundingClientRect(), qe = 40, Ze = 8, Et = ae.scrollLeft;
      Q > _e.right - qe ? ae.scrollLeft += Ze : Q < _e.left + qe && (ae.scrollLeft = Math.max(0, ae.scrollLeft - Ze));
      const Qe = ae.scrollLeft - Et;
      Qe !== 0 && (de && (de.scrollLeft = ae.scrollLeft), y += Qe, D()), P.current = requestAnimationFrame(z);
    };
    P.current = requestAnimationFrame(z);
    const ee = (ae) => {
      Q = ae.clientX, D();
    }, pe = (ae) => {
      document.removeEventListener("mousemove", ee), document.removeEventListener("mouseup", pe), P.current !== null && (cancelAnimationFrame(P.current), P.current = null);
      const de = I.current;
      if (de) {
        const _e = Math.max(ot, de.startWidth + (ae.clientX - de.startX) + y);
        t("columnResize", { column: de.column, width: _e }), I.current = null, h.current = !0, requestAnimationFrame(() => {
          h.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ee), document.addEventListener("mouseup", pe);
  }, [t]), Z = e.useCallback(() => {
    b.current && f.current && (b.current.scrollLeft = f.current.scrollLeft), S.current !== null && clearTimeout(S.current), S.current = window.setTimeout(() => {
      const k = f.current;
      if (!k) return;
      const x = k.scrollTop, U = Math.ceil(k.clientHeight / o), Q = Math.floor(x / o);
      t("scroll", { start: Q, count: U });
    }, 80);
  }, [t, o]), re = e.useCallback((k, x, U) => {
    if (h.current) return;
    let Q;
    !x || x === "desc" ? Q = "asc" : Q = "desc";
    const y = U.shiftKey ? "add" : "replace";
    t("sort", { column: k, direction: Q, mode: y });
  }, [t]), $ = e.useCallback((k, x) => {
    C.current = k, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", k);
  }, []), O = e.useCallback((k, x) => {
    if (!C.current || C.current === k) {
      V(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), Q = x.clientX < U.left + U.width / 2 ? "left" : "right";
    V({ column: k, side: Q });
  }, []), X = e.useCallback((k) => {
    k.preventDefault(), k.stopPropagation();
    const x = C.current;
    if (!x || !E) {
      C.current = null, V(null);
      return;
    }
    let U = l.findIndex((y) => y.name === E.column);
    if (U < 0) {
      C.current = null, V(null);
      return;
    }
    const Q = l.findIndex((y) => y.name === x);
    E.side === "right" && U++, Q < U && U--, t("columnReorder", { column: x, targetIndex: U }), C.current = null, V(null);
  }, [l, E, t]), d = e.useCallback(() => {
    C.current = null, V(null);
  }, []), g = e.useCallback((k, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: k,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), F = e.useCallback((k, x) => {
    x.stopPropagation(), t("select", { rowIndex: k, ctrlKey: !0, shiftKey: !1 });
  }, [t]), A = e.useCallback(() => {
    const k = s === c && c > 0;
    t("selectAll", { selected: !k });
  }, [t, s, c]), K = e.useCallback((k, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: k, expanded: x });
  }, [t]), q = e.useCallback((k, x) => {
    x.preventDefault(), L({ x: x.clientX, y: x.clientY, colIdx: k });
  }, []), J = e.useCallback(() => {
    T && (t("setFrozenColumnCount", { count: T.colIdx + 1 }), L(null));
  }, [T, t]), ie = e.useCallback(() => {
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
  const le = l.reduce((k, x) => k + B(x), 0) + (_ ? N : 0), ge = s === c && c > 0, Se = s > 0 && s < c, Pe = e.useCallback((k) => {
    k && (k.indeterminate = Se);
  }, [Se]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (k) => {
        if (!C.current) return;
        k.preventDefault();
        const x = f.current, U = b.current;
        if (!x) return;
        const Q = x.getBoundingClientRect(), y = 40, D = 8;
        k.clientX < Q.left + y ? x.scrollLeft = Math.max(0, x.scrollLeft - D) : k.clientX > Q.right - y && (x.scrollLeft += D), U && (U.scrollLeft = x.scrollLeft);
      },
      onDrop: X
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: le } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (k) => {
          C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== C.current && V({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Pe,
          className: "tlTableView__checkbox",
          checked: ge,
          onChange: A
        }
      )
    ), l.map((k, x) => {
      const U = B(k);
      l.length - 1;
      let Q = "tlTableView__headerCell";
      k.sortable && (Q += " tlTableView__headerCell--sortable"), E && E.column === k.name && (Q += " tlTableView__headerCell--dragOver-" + E.side);
      const y = x < r, D = x === r - 1;
      return y && (Q += " tlTableView__headerCell--frozen"), D && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.name,
          className: Q,
          style: {
            width: U,
            minWidth: U,
            position: y ? "sticky" : "relative",
            ...y ? { left: R[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: k.sortable ? (z) => re(k.name, k.sortDirection, z) : void 0,
          onContextMenu: (z) => q(x, z),
          onDragStart: (z) => $(k.name, z),
          onDragOver: (z) => O(k.name, z),
          onDrop: X,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, k.label),
        k.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, k.sortDirection === "asc" ? "▲" : "▼", p > 1 && k.sortPriority != null && k.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, k.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (z) => te(k.name, U, z)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (k) => {
          if (C.current && l.length > 0) {
            const x = l[l.length - 1];
            x.name !== C.current && (k.preventDefault(), k.dataTransfer.dropEffect = "move", V({ column: x.name, side: "right" }));
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
        onScroll: Z
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: j, position: "relative", width: le } }, i.map((k) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: k.id,
          className: "tlTableView__row" + (k.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: k.index * o,
            height: o,
            width: le
          },
          onClick: (x) => g(k.index, x)
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
              onClick: (x) => F(k.index, x),
              tabIndex: -1
            }
          )
        ),
        l.map((x, U) => {
          const Q = B(x), y = U === l.length - 1, D = U < r, z = U === r - 1;
          let ee = "tlTableView__cell";
          D && (ee += " tlTableView__cell--frozen"), z && (ee += " tlTableView__cell--frozenLast");
          const pe = m && U === 0, ae = k.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: ee,
              style: {
                ...y && !D ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...D ? { position: "sticky", left: R[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * v } }, k.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => K(k.index, !k.expanded, de)
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
      T.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: J }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Kn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ft = e.createContext(Kn), { useMemo: Yn, useRef: Gn, useState: Xn, useEffect: qn } = e, Zn = 320, Qn = ({ controlId: n }) => {
  const t = G(), a = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Gn(null), [s, r] = Xn(
    l === "top" ? "top" : "side"
  );
  qn(() => {
    if (l !== "auto") {
      r(l);
      return;
    }
    const v = u.current;
    if (!v) return;
    const b = new ResizeObserver((f) => {
      for (const S of f) {
        const M = S.contentRect.width / a;
        r(M < Zn ? "top" : "side");
      }
    });
    return b.observe(v), () => b.disconnect();
  }, [l, a]);
  const m = Yn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), _ = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(ft.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: N, style: _, ref: u }, i.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v }))));
}, { useCallback: Jn } = e, el = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, tl = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(el), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, _ = Jn(() => {
    a("toggleCollapse");
  }, [a]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    r ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: N }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: _,
      "aria-expanded": !u,
      title: u ? l["js.formGroup.expand"] : l["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((v, b) => /* @__PURE__ */ e.createElement(Y, { key: b, control: v }))));
}, { useContext: nl, useState: ll, useCallback: rl } = e, al = ({ controlId: n }) => {
  const t = G(), a = nl(ft), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, r = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, _ = t.field, N = a.readOnly, [v, b] = ll(!1), f = rl(() => b((I) => !I), []);
  if (!p) return null;
  const S = i != null, w = o != null && o.length > 0, M = [
    "tlFormField",
    `tlFormField--${r}`,
    N ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    S ? "tlFormField--error" : "",
    !S && w ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: M }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !N && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !N && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: f,
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !N && !S && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((I, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, I)))), !N && u && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, ol = () => {
  const n = G(), t = ne(), a = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, u = n.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, sl = 20, cl = () => {
  const n = G(), t = ne(), a = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, u = n.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((h, C) => {
    t(C ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, C) => {
    t("select", {
      nodeId: h,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), N = e.useCallback((h, C) => {
    C.preventDefault(), t("contextMenu", { nodeId: h, x: C.clientX, y: C.clientY });
  }, [t]), v = e.useRef(null), b = e.useCallback((h, C) => {
    const E = C.getBoundingClientRect(), V = h.clientY - E.top, T = E.height / 3;
    return V < T ? "above" : V > T * 2 ? "below" : "within";
  }, []), f = e.useCallback((h, C) => {
    C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", h);
  }, []), S = e.useCallback((h, C) => {
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const E = b(C, C.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: E }), v.current = null;
    }, 50);
  }, [t, b]), w = e.useCallback((h, C) => {
    C.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const E = b(C, C.currentTarget);
    t("drop", { nodeId: h, position: E });
  }, [t, b]), M = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), I = e.useCallback((h) => {
    if (a.length === 0) return;
    let C = s;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), C = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), C = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), s >= 0 && s < a.length) {
          const E = a[s];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (C = s + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), s >= 0 && s < a.length) {
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
        h.preventDefault(), s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: h.ctrlKey || h.metaKey,
          shiftKey: h.shiftKey
        });
        return;
      case " ":
        h.preventDefault(), l === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        h.preventDefault(), C = 0;
        break;
      case "End":
        h.preventDefault(), C = a.length - 1;
        break;
      default:
        return;
    }
    C !== s && r(C);
  }, [s, a, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: I
    },
    a.map((h, C) => /* @__PURE__ */ e.createElement(
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
          C === s ? "tlTreeView__node--focused" : "",
          o === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * sl },
        draggable: c,
        onClick: (E) => _(h.id, E),
        onContextMenu: (E) => N(h.id, E),
        onDragStart: (E) => f(h.id, E),
        onDragOver: i ? (E) => S(h.id, E) : void 0,
        onDrop: i ? (E) => w(h.id, E) : void 0,
        onDragEnd: M
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(h.id, h.expanded);
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
var $e = { exports: {} }, se = {}, Fe = { exports: {} }, W = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var st;
function il() {
  if (st) return W;
  st = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), _ = Symbol.iterator;
  function N(d) {
    return d === null || typeof d != "object" ? null : (d = _ && d[_] || d["@@iterator"], typeof d == "function" ? d : null);
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
  function S(d, g, F) {
    this.props = d, this.context = g, this.refs = f, this.updater = F || v;
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
  function w() {
  }
  w.prototype = S.prototype;
  function M(d, g, F) {
    this.props = d, this.context = g, this.refs = f, this.updater = F || v;
  }
  var I = M.prototype = new w();
  I.constructor = M, b(I, S.prototype), I.isPureReactComponent = !0;
  var h = Array.isArray;
  function C() {
  }
  var E = { H: null, A: null, T: null, S: null }, V = Object.prototype.hasOwnProperty;
  function T(d, g, F) {
    var A = F.ref;
    return {
      $$typeof: n,
      type: d,
      key: g,
      ref: A !== void 0 ? A : null,
      props: F
    };
  }
  function L(d, g) {
    return T(d.type, g, d.props);
  }
  function B(d) {
    return typeof d == "object" && d !== null && d.$$typeof === n;
  }
  function R(d) {
    var g = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return g[F];
    });
  }
  var j = /\/+/g;
  function P(d, g) {
    return typeof d == "object" && d !== null && d.key != null ? R("" + d.key) : g.toString(36);
  }
  function te(d) {
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
  function Z(d, g, F, A, K) {
    var q = typeof d;
    (q === "undefined" || q === "boolean") && (d = null);
    var J = !1;
    if (d === null) J = !0;
    else
      switch (q) {
        case "bigint":
        case "string":
        case "number":
          J = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case n:
            case t:
              J = !0;
              break;
            case m:
              return J = d._init, Z(
                J(d._payload),
                g,
                F,
                A,
                K
              );
          }
      }
    if (J)
      return K = K(d), J = A === "" ? "." + P(d, 0) : A, h(K) ? (F = "", J != null && (F = J.replace(j, "$&/") + "/"), Z(K, g, F, "", function(ge) {
        return ge;
      })) : K != null && (B(K) && (K = L(
        K,
        F + (K.key == null || d && d.key === K.key ? "" : ("" + K.key).replace(
          j,
          "$&/"
        ) + "/") + J
      )), g.push(K)), 1;
    J = 0;
    var ie = A === "" ? "." : A + ":";
    if (h(d))
      for (var le = 0; le < d.length; le++)
        A = d[le], q = ie + P(A, le), J += Z(
          A,
          g,
          F,
          q,
          K
        );
    else if (le = N(d), typeof le == "function")
      for (d = le.call(d), le = 0; !(A = d.next()).done; )
        A = A.value, q = ie + P(A, le++), J += Z(
          A,
          g,
          F,
          q,
          K
        );
    else if (q === "object") {
      if (typeof d.then == "function")
        return Z(
          te(d),
          g,
          F,
          A,
          K
        );
      throw g = String(d), Error(
        "Objects are not valid as a React child (found: " + (g === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : g) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function re(d, g, F) {
    if (d == null) return d;
    var A = [], K = 0;
    return Z(d, A, "", "", function(q) {
      return g.call(F, q, K++);
    }), A;
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
  var O = typeof reportError == "function" ? reportError : function(d) {
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
  }, X = {
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
      if (!B(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return W.Activity = p, W.Children = X, W.Component = S, W.Fragment = a, W.Profiler = c, W.PureComponent = M, W.StrictMode = l, W.Suspense = s, W.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, W.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, W.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, W.cacheSignal = function() {
    return null;
  }, W.cloneElement = function(d, g, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var A = b({}, d.props), K = d.key;
    if (g != null)
      for (q in g.key !== void 0 && (K = "" + g.key), g)
        !V.call(g, q) || q === "key" || q === "__self" || q === "__source" || q === "ref" && g.ref === void 0 || (A[q] = g[q]);
    var q = arguments.length - 2;
    if (q === 1) A.children = F;
    else if (1 < q) {
      for (var J = Array(q), ie = 0; ie < q; ie++)
        J[ie] = arguments[ie + 2];
      A.children = J;
    }
    return T(d.type, K, A);
  }, W.createContext = function(d) {
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
  }, W.createElement = function(d, g, F) {
    var A, K = {}, q = null;
    if (g != null)
      for (A in g.key !== void 0 && (q = "" + g.key), g)
        V.call(g, A) && A !== "key" && A !== "__self" && A !== "__source" && (K[A] = g[A]);
    var J = arguments.length - 2;
    if (J === 1) K.children = F;
    else if (1 < J) {
      for (var ie = Array(J), le = 0; le < J; le++)
        ie[le] = arguments[le + 2];
      K.children = ie;
    }
    if (d && d.defaultProps)
      for (A in J = d.defaultProps, J)
        K[A] === void 0 && (K[A] = J[A]);
    return T(d, q, K);
  }, W.createRef = function() {
    return { current: null };
  }, W.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, W.isValidElement = B, W.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: $
    };
  }, W.memo = function(d, g) {
    return {
      $$typeof: r,
      type: d,
      compare: g === void 0 ? null : g
    };
  }, W.startTransition = function(d) {
    var g = E.T, F = {};
    E.T = F;
    try {
      var A = d(), K = E.S;
      K !== null && K(F, A), typeof A == "object" && A !== null && typeof A.then == "function" && A.then(C, O);
    } catch (q) {
      O(q);
    } finally {
      g !== null && F.types !== null && (g.types = F.types), E.T = g;
    }
  }, W.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, W.use = function(d) {
    return E.H.use(d);
  }, W.useActionState = function(d, g, F) {
    return E.H.useActionState(d, g, F);
  }, W.useCallback = function(d, g) {
    return E.H.useCallback(d, g);
  }, W.useContext = function(d) {
    return E.H.useContext(d);
  }, W.useDebugValue = function() {
  }, W.useDeferredValue = function(d, g) {
    return E.H.useDeferredValue(d, g);
  }, W.useEffect = function(d, g) {
    return E.H.useEffect(d, g);
  }, W.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, W.useId = function() {
    return E.H.useId();
  }, W.useImperativeHandle = function(d, g, F) {
    return E.H.useImperativeHandle(d, g, F);
  }, W.useInsertionEffect = function(d, g) {
    return E.H.useInsertionEffect(d, g);
  }, W.useLayoutEffect = function(d, g) {
    return E.H.useLayoutEffect(d, g);
  }, W.useMemo = function(d, g) {
    return E.H.useMemo(d, g);
  }, W.useOptimistic = function(d, g) {
    return E.H.useOptimistic(d, g);
  }, W.useReducer = function(d, g, F) {
    return E.H.useReducer(d, g, F);
  }, W.useRef = function(d) {
    return E.H.useRef(d);
  }, W.useState = function(d) {
    return E.H.useState(d);
  }, W.useSyncExternalStore = function(d, g, F) {
    return E.H.useSyncExternalStore(
      d,
      g,
      F
    );
  }, W.useTransition = function() {
    return E.H.useTransition();
  }, W.version = "19.2.4", W;
}
var ct;
function ul() {
  return ct || (ct = 1, Fe.exports = il()), Fe.exports;
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
var it;
function dl() {
  if (it) return se;
  it = 1;
  var n = ul();
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
  var l = {
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
  var o = n.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(s, r) {
    if (s === "font") return "";
    if (typeof r == "string")
      return r === "use-credentials" ? r : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, se.createPortal = function(s, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return i(s, r, null, m);
  }, se.flushSync = function(s) {
    var r = o.T, m = l.p;
    try {
      if (o.T = null, l.p = 2, s) return s();
    } finally {
      o.T = r, l.p = m, l.d.f();
    }
  }, se.preconnect = function(s, r) {
    typeof s == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, l.d.C(s, r));
  }, se.prefetchDNS = function(s) {
    typeof s == "string" && l.d.D(s);
  }, se.preinit = function(s, r) {
    if (typeof s == "string" && r && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin), _ = typeof r.integrity == "string" ? r.integrity : void 0, N = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? l.d.S(
        s,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: _,
          fetchPriority: N
        }
      ) : m === "script" && l.d.X(s, {
        crossOrigin: p,
        integrity: _,
        fetchPriority: N,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, se.preinitModule = function(s, r) {
    if (typeof s == "string")
      if (typeof r == "object" && r !== null) {
        if (r.as == null || r.as === "script") {
          var m = u(
            r.as,
            r.crossOrigin
          );
          l.d.M(s, {
            crossOrigin: m,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && l.d.M(s);
  }, se.preload = function(s, r) {
    if (typeof s == "string" && typeof r == "object" && r !== null && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin);
      l.d.L(s, m, {
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
  }, se.preloadModule = function(s, r) {
    if (typeof s == "string")
      if (r) {
        var m = u(r.as, r.crossOrigin);
        l.d.m(s, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else l.d.m(s);
  }, se.requestFormReset = function(s) {
    l.d.r(s);
  }, se.unstable_batchedUpdates = function(s, r) {
    return s(r);
  }, se.useFormState = function(s, r, m) {
    return o.H.useFormState(s, r, m);
  }, se.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var ut;
function ml() {
  if (ut) return $e.exports;
  ut = 1;
  function n() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(n);
      } catch (t) {
        console.error(t);
      }
  }
  return n(), $e.exports = dl(), $e.exports;
}
var pl = ml();
const { useState: fe, useCallback: ce, useRef: Ce, useEffect: be, useMemo: Ve } = e;
function Xe({ image: n }) {
  if (!n) return null;
  if (n.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = n.startsWith("css:") ? n.substring(4) : n.startsWith("colored:") ? n.substring(8) : n;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function fl({
  option: n,
  removable: t,
  onRemove: a,
  removeLabel: l,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: u,
  onDragEnd: s,
  dragClassName: r
}) {
  const m = ce(
    (p) => {
      p.stopPropagation(), a(n.value);
    },
    [a, n.value]
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
    /* @__PURE__ */ e.createElement(Xe, { image: n.image }),
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
function hl({
  option: n,
  highlighted: t,
  searchTerm: a,
  onSelect: l,
  onMouseEnter: c,
  id: i
}) {
  const o = ce(() => l(n.value), [l, n.value]), u = Ve(() => {
    if (!a) return n.label;
    const s = n.label.toLowerCase().indexOf(a.toLowerCase());
    return s < 0 ? n.label : /* @__PURE__ */ e.createElement(e.Fragment, null, n.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, n.label.substring(s, s + a.length)), n.label.substring(s + a.length));
  }, [n.label, a]);
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
    /* @__PURE__ */ e.createElement(Xe, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const _l = ({ controlId: n, state: t }) => {
  const a = ne(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", _ = i && c && !u && s, N = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), v = N["js.dropdownSelect.nothingFound"], b = ce(
    (y) => N["js.dropdownSelect.removeChip"].replace("{0}", y),
    [N]
  ), [f, S] = fe(!1), [w, M] = fe(""), [I, h] = fe(-1), [C, E] = fe(!1), [V, T] = fe({}), [L, B] = fe(null), [R, j] = fe(null), [P, te] = fe(null), Z = Ce(null), re = Ce(null), $ = Ce(null), O = Ce(l);
  O.current = l;
  const X = Ce(-1), d = Ve(
    () => new Set(l.map((y) => y.value)),
    [l]
  ), g = Ve(() => {
    let y = m.filter((D) => !d.has(D.value));
    if (w) {
      const D = w.toLowerCase();
      y = y.filter((z) => z.label.toLowerCase().includes(D));
    }
    return y;
  }, [m, d, w]);
  be(() => {
    w && g.length === 1 ? h(0) : h(-1);
  }, [g.length, w]), be(() => {
    f && r && re.current && re.current.focus();
  }, [f, r, l]), be(() => {
    var z, ee;
    if (X.current < 0) return;
    const y = X.current;
    X.current = -1;
    const D = (z = Z.current) == null ? void 0 : z.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    D && D.length > 0 ? D[Math.min(y, D.length - 1)].focus() : (ee = Z.current) == null || ee.focus();
  }, [l]), be(() => {
    if (!f) return;
    const y = (D) => {
      Z.current && !Z.current.contains(D.target) && $.current && !$.current.contains(D.target) && (S(!1), M(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [f]), be(() => {
    if (!f || !Z.current) return;
    const y = Z.current.getBoundingClientRect(), D = window.innerHeight - y.bottom, ee = D < 300 && y.top > D;
    T({
      left: y.left,
      width: y.width,
      ...ee ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [f]);
  const F = ce(async () => {
    if (!(u || !s) && (S(!0), M(""), h(-1), E(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, s, r, a]), A = ce(() => {
    var y;
    S(!1), M(""), h(-1), (y = Z.current) == null || y.focus();
  }, []), K = ce(
    (y) => {
      let D;
      if (c) {
        const z = m.find((ee) => ee.value === y);
        if (z)
          D = [...O.current, z];
        else
          return;
      } else {
        const z = m.find((ee) => ee.value === y);
        if (z)
          D = [z];
        else
          return;
      }
      O.current = D, a("valueChanged", { value: D.map((z) => z.value) }), c ? (M(""), h(-1)) : A();
    },
    [c, m, a, A]
  ), q = ce(
    (y) => {
      X.current = O.current.findIndex((z) => z.value === y);
      const D = O.current.filter((z) => z.value !== y);
      O.current = D, a("valueChanged", { value: D.map((z) => z.value) });
    },
    [a]
  ), J = ce(
    (y) => {
      y.stopPropagation(), a("valueChanged", { value: [] }), A();
    },
    [a, A]
  ), ie = ce((y) => {
    M(y.target.value);
  }, []), le = ce(
    (y) => {
      if (!f) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), F();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), h(
            (D) => D < g.length - 1 ? D + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), h(
            (D) => D > 0 ? D - 1 : g.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), I >= 0 && I < g.length && K(g[I].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), A();
          break;
        case "Tab":
          A();
          break;
        case "Backspace":
          w === "" && c && l.length > 0 && q(l[l.length - 1].value);
          break;
      }
    },
    [
      f,
      F,
      A,
      g,
      I,
      K,
      w,
      c,
      l,
      q
    ]
  ), ge = ce(
    async (y) => {
      y.preventDefault(), E(!1);
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
    },
    [a]
  ), Se = ce(
    (y, D) => {
      B(y), D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), Pe = ce(
    (y, D) => {
      if (D.preventDefault(), D.dataTransfer.dropEffect = "move", L === null || L === y) {
        j(null), te(null);
        return;
      }
      const z = D.currentTarget.getBoundingClientRect(), ee = z.left + z.width / 2, pe = D.clientX < ee ? "before" : "after";
      j(y), te(pe);
    },
    [L]
  ), k = ce(
    (y) => {
      if (y.preventDefault(), L === null || R === null || P === null || L === R) return;
      const D = [...O.current], [z] = D.splice(L, 1);
      let ee = R;
      L < R ? ee = P === "before" ? ee - 1 : ee : ee = P === "before" ? ee : ee + 1, D.splice(ee, 0, z), O.current = D, a("valueChanged", { value: D.map((pe) => pe.value) }), B(null), j(null), te(null);
    },
    [L, R, P, a]
  ), x = ce(() => {
    B(null), j(null), te(null);
  }, []);
  if (be(() => {
    if (I < 0 || !$.current) return;
    const y = $.current.querySelector(
      `[id="${n}-opt-${I}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [I, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Xe, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const U = !o && l.length > 0 && !u, Q = f ? /* @__PURE__ */ e.createElement(
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
        value: w,
        onChange: ie,
        onKeyDown: le,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": I >= 0 ? `${n}-opt-${I}` : void 0,
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
      !r && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ge }, N["js.dropdownSelect.error"])),
      r && g.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, v),
      r && g.map((y, D) => /* @__PURE__ */ e.createElement(
        hl,
        {
          key: y.value,
          id: `${n}-opt-${D}`,
          option: y,
          highlighted: D === I,
          searchTerm: w,
          onSelect: K,
          onMouseEnter: () => h(D)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: Z,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${n}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: f ? void 0 : F,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((y, D) => {
      let z = "";
      return L === D ? z = "tlDropdownSelect__chip--dragging" : R === D && P === "before" ? z = "tlDropdownSelect__chip--dropBefore" : R === D && P === "after" && (z = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        fl,
        {
          key: y.value,
          option: y,
          removable: !u && (c || !o),
          onRemove: q,
          removeLabel: b(y.label),
          draggable: _,
          onDragStart: _ ? (ee) => Se(D, ee) : void 0,
          onDragOver: _ ? (ee) => Pe(D, ee) : void 0,
          onDrop: _ ? k : void 0,
          onDragEnd: _ ? x : void 0,
          dragClassName: _ ? z : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, U && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": N["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), Q && pl.createPortal(Q, document.body));
}, { useCallback: He, useRef: bl } = e, ht = "application/x-tl-color", vl = ({
  colors: n,
  columns: t,
  onSelect: a,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = bl(null), u = He(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = He((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = He(
    (m) => (p) => {
      p.preventDefault();
      const _ = p.dataTransfer.getData(ht);
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
        onClick: m != null ? () => a(m) : void 0,
        onDoubleClick: m != null ? () => l(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: s,
        onDrop: r(p)
      }
    ))
  );
};
function _t(n) {
  return Math.max(0, Math.min(255, Math.round(n)));
}
function Ke(n) {
  return /^#[0-9a-fA-F]{6}$/.test(n);
}
function bt(n) {
  if (!Ke(n)) return [0, 0, 0];
  const t = parseInt(n.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function vt(n, t, a) {
  const l = (c) => _t(c).toString(16).padStart(2, "0");
  return "#" + l(n) + l(t) + l(a);
}
function El(n, t, a) {
  const l = n / 255, c = t / 255, i = a / 255, o = Math.max(l, c, i), u = Math.min(l, c, i), s = o - u;
  let r = 0;
  s !== 0 && (o === l ? r = (c - i) / s % 6 : o === c ? r = (i - l) / s + 2 : r = (l - c) / s + 4, r *= 60, r < 0 && (r += 360));
  const m = o === 0 ? 0 : s / o;
  return [r, m, o];
}
function gl(n, t, a) {
  const l = a * t, c = l * (1 - Math.abs(n / 60 % 2 - 1)), i = a - l;
  let o = 0, u = 0, s = 0;
  return n < 60 ? (o = l, u = c, s = 0) : n < 120 ? (o = c, u = l, s = 0) : n < 180 ? (o = 0, u = l, s = c) : n < 240 ? (o = 0, u = c, s = l) : n < 300 ? (o = c, u = 0, s = l) : (o = l, u = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((u + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function Cl(n) {
  return El(...bt(n));
}
function ze(n, t, a) {
  return vt(...gl(n, t, a));
}
const { useCallback: ve, useRef: dt } = e, yl = ({ color: n, onColorChange: t }) => {
  const [a, l, c] = Cl(n), i = dt(null), o = dt(null), u = ve(
    (v, b) => {
      var M;
      const f = (M = i.current) == null ? void 0 : M.getBoundingClientRect();
      if (!f) return;
      const S = Math.max(0, Math.min(1, (v - f.left) / f.width)), w = Math.max(0, Math.min(1, 1 - (b - f.top) / f.height));
      t(ze(a, S, w));
    },
    [a, t]
  ), s = ve(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), u(v.clientX, v.clientY);
    },
    [u]
  ), r = ve(
    (v) => {
      v.buttons !== 0 && u(v.clientX, v.clientY);
    },
    [u]
  ), m = ve(
    (v) => {
      var w;
      const b = (w = o.current) == null ? void 0 : w.getBoundingClientRect();
      if (!b) return;
      const S = Math.max(0, Math.min(1, (v - b.top) / b.height)) * 360;
      t(ze(S, l, c));
    },
    [l, c, t]
  ), p = ve(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), m(v.clientY);
    },
    [m]
  ), _ = ve(
    (v) => {
      v.buttons !== 0 && m(v.clientY);
    },
    [m]
  ), N = ze(a, 1, 1);
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
        style: { top: `${a / 360 * 100}%` }
      }
    )
  ));
};
function wl(n, t) {
  const a = t.toUpperCase();
  return n.some((l) => l != null && l.toUpperCase() === a);
}
const kl = {
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
}, { useState: Te, useCallback: me, useEffect: We, useRef: Sl, useLayoutEffect: Nl } = e, Tl = ({
  anchorRef: n,
  currentColor: t,
  palette: a,
  paletteColumns: l,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: u,
  onPaletteChange: s
}) => {
  const [r, m] = Te("palette"), [p, _] = Te(t), N = Sl(null), v = oe(kl), [b, f] = Te(null);
  Nl(() => {
    if (!n.current || !N.current) return;
    const $ = n.current.getBoundingClientRect(), O = N.current.getBoundingClientRect();
    let X = $.bottom + 4, d = $.left;
    X + O.height > window.innerHeight && (X = $.top - O.height - 4), d + O.width > window.innerWidth && (d = Math.max(0, $.right - O.width)), f({ top: X, left: d });
  }, [n]);
  const S = p != null, [w, M, I] = S ? bt(p) : [0, 0, 0], [h, C] = Te((p == null ? void 0 : p.toUpperCase()) ?? "");
  We(() => {
    C((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), We(() => {
    const $ = (O) => {
      O.key === "Escape" && u();
    };
    return document.addEventListener("keydown", $), () => document.removeEventListener("keydown", $);
  }, [u]), We(() => {
    const $ = (X) => {
      N.current && !N.current.contains(X.target) && u();
    }, O = setTimeout(() => document.addEventListener("mousedown", $), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", $);
    };
  }, [u]);
  const E = me(
    ($) => (O) => {
      const X = parseInt(O.target.value, 10);
      if (isNaN(X)) return;
      const d = _t(X);
      _(vt($ === "r" ? d : w, $ === "g" ? d : M, $ === "b" ? d : I));
    },
    [w, M, I]
  ), V = me(
    ($) => {
      if (p != null) {
        $.dataTransfer.setData(ht, p.toUpperCase()), $.dataTransfer.effectAllowed = "move";
        const O = document.createElement("div");
        O.style.width = "33px", O.style.height = "33px", O.style.backgroundColor = p, O.style.borderRadius = "3px", O.style.border = "1px solid rgba(0,0,0,0.1)", O.style.position = "absolute", O.style.top = "-9999px", document.body.appendChild(O), $.dataTransfer.setDragImage(O, 16, 16), requestAnimationFrame(() => document.body.removeChild(O));
      }
    },
    [p]
  ), T = me(($) => {
    const O = $.target.value;
    C(O), Ke(O) && _(O);
  }, []), L = me(() => {
    _(null);
  }, []), B = me(($) => {
    _($);
  }, []), R = me(
    ($) => {
      o($);
    },
    [o]
  ), j = me(
    ($, O) => {
      const X = [...a], d = X[$];
      X[$] = X[O], X[O] = d, s(X);
    },
    [a, s]
  ), P = me(
    ($, O) => {
      const X = [...a];
      X[$] = O, s(X);
    },
    [a, s]
  ), te = me(() => {
    s([...c]);
  }, [c, s]), Z = me(
    ($) => {
      if (wl(a, $)) return;
      const O = a.indexOf(null);
      if (O < 0) return;
      const X = [...a];
      X[O] = $.toUpperCase(), s(X);
    },
    [a, s]
  ), re = me(() => {
    p != null && Z(p), o(p);
  }, [p, o, Z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: N,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      v["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      v["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      vl,
      {
        colors: a,
        columns: l,
        onSelect: B,
        onConfirm: R,
        onSwap: j,
        onReplace: P
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: te }, v["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(yl, { color: p ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, v["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, v["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (S ? "" : " tlColorInput--noColor"),
        style: S ? { backgroundColor: p } : void 0,
        draggable: S,
        onDragStart: S ? V : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? w : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? M : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? I : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !Ke(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, v["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, v["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: re }, v["js.colorInput.ok"]))
  );
}, Rl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ll, useCallback: Re, useRef: xl } = e, Dl = ({ controlId: n, state: t }) => {
  const a = ne(), l = oe(Rl), [c, i] = Ll(!1), o = xl(null), u = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, _ = Re(() => {
    s && i(!0);
  }, [s]), N = Re(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), v = Re(() => {
    i(!1);
  }, []), b = Re(
    (f) => {
      a("paletteChanged", { palette: f });
    },
    [a]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: _,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": l["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Tl,
    {
      anchorRef: o,
      currentColor: u,
      palette: r,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: v,
      onPaletteChange: b
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: n,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
    }
  );
}, { useState: ye, useCallback: he, useEffect: Le, useRef: mt, useLayoutEffect: Il, useMemo: jl } = e, Pl = {
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
}, Ml = ({
  anchorRef: n,
  currentValue: t,
  icons: a,
  iconsLoaded: l,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const u = oe(Pl), [s, r] = ye("simple"), [m, p] = ye(""), [_, N] = ye(t ?? ""), [v, b] = ye(!1), [f, S] = ye(null), w = mt(null), M = mt(null);
  Il(() => {
    if (!n.current || !w.current) return;
    const R = n.current.getBoundingClientRect(), j = w.current.getBoundingClientRect();
    let P = R.bottom + 4, te = R.left;
    P + j.height > window.innerHeight && (P = R.top - j.height - 4), te + j.width > window.innerWidth && (te = Math.max(0, R.right - j.width)), S({ top: P, left: te });
  }, [n]), Le(() => {
    !l && !v && o().catch(() => b(!0));
  }, [l, v, o]), Le(() => {
    l && M.current && M.current.focus();
  }, [l]), Le(() => {
    const R = (j) => {
      j.key === "Escape" && i();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [i]), Le(() => {
    const R = (P) => {
      w.current && !w.current.contains(P.target) && i();
    }, j = setTimeout(() => document.addEventListener("mousedown", R), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", R);
    };
  }, [i]);
  const I = jl(() => {
    if (!m) return a;
    const R = m.toLowerCase();
    return a.filter(
      (j) => j.prefix.toLowerCase().includes(R) || j.label.toLowerCase().includes(R) || j.terms != null && j.terms.some((P) => P.includes(R))
    );
  }, [a, m]), h = he((R) => {
    p(R.target.value);
  }, []), C = he(
    (R) => {
      c(R);
    },
    [c]
  ), E = he((R) => {
    N(R);
  }, []), V = he((R) => {
    N(R.target.value);
  }, []), T = he(() => {
    c(_ || null);
  }, [_, c]), L = he(() => {
    c(null);
  }, [c]), B = he(async (R) => {
    R.preventDefault(), b(!1);
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
      ref: w,
      style: f ? { top: f.top, left: f.left, visibility: "visible" } : { visibility: "hidden" }
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
        ref: M,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: h,
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
      !l && !v && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      v && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: B }, u["js.iconSelect.loadError"])),
      l && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      l && I.map(
        (R) => R.variants.map((j) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: j.encoded,
            className: "tlIconSelect__iconCell" + (j.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": j.encoded === t,
            tabIndex: 0,
            title: R.label,
            onClick: () => s === "simple" ? C(j.encoded) : E(j.encoded),
            onKeyDown: (P) => {
              (P.key === "Enter" || P.key === " ") && (P.preventDefault(), s === "simple" ? C(j.encoded) : E(j.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: j.encoded })
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
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(we, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: L }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, Al = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Bl, useCallback: xe, useRef: Ol } = e, $l = ({ controlId: n, state: t }) => {
  const a = ne(), l = oe(Al), [c, i] = Bl(!1), o = Ol(null), u = t.value, s = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, _ = xe(() => {
    s && !r && i(!0);
  }, [s, r]), N = xe(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), v = xe(() => {
    i(!1);
  }, []), b = xe(async () => {
    await a("loadIcons");
  }, [a]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: _,
      disabled: r,
      title: u ?? "",
      "aria-label": l["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ml,
    {
      anchorRef: o,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: N,
      onCancel: v,
      onLoadIcons: b
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : null));
};
H("TLButton", jt);
H("TLToggleButton", Mt);
H("TLTextInput", yt);
H("TLNumberInput", kt);
H("TLDatePicker", Nt);
H("TLSelect", Rt);
H("TLCheckbox", xt);
H("TLTable", Dt);
H("TLCounter", At);
H("TLTabBar", Ot);
H("TLFieldList", $t);
H("TLAudioRecorder", Ht);
H("TLAudioPlayer", Wt);
H("TLFileUpload", Vt);
H("TLDownload", Yt);
H("TLPhotoCapture", Xt);
H("TLPhotoViewer", Zt);
H("TLSplitPanel", Qt);
H("TLPanel", an);
H("TLMaximizeRoot", on);
H("TLDeckPane", sn);
H("TLSidebar", _n);
H("TLStack", bn);
H("TLGrid", vn);
H("TLCard", En);
H("TLAppBar", gn);
H("TLBreadcrumb", yn);
H("TLBottomBar", kn);
H("TLDialog", Nn);
H("TLDialogManager", Ln);
H("TLWindow", In);
H("TLDrawer", An);
H("TLSnackbar", $n);
H("TLMenu", Hn);
H("TLAppShell", zn);
H("TLTextCell", Wn);
H("TLTableView", Vn);
H("TLFormLayout", Qn);
H("TLFormGroup", tl);
H("TLFormField", al);
H("TLResourceCell", ol);
H("TLTreeView", cl);
H("TLDropdownSelect", _l);
H("TLColorInput", Dl);
H("TLIconSelect", $l);
