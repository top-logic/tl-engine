import { React as e, useTLFieldValue as Se, getComponent as Et, useTLState as X, useTLCommand as le, TLChild as G, useTLUpload as Xe, useI18N as oe, useTLDataUrl as qe, register as F } from "tl-react-bridge";
const { useCallback: gt } = e, Ct = ({ controlId: n, state: t }) => {
  const [a, l] = Se(), c = gt(
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
}, { useCallback: yt } = e, wt = ({ controlId: n, state: t, config: a }) => {
  const [l, c] = Se(), i = yt(
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
}, { useCallback: kt } = e, St = ({ controlId: n, state: t }) => {
  const [a, l] = Se(), c = kt(
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
}, { useCallback: Nt } = e, Tt = ({ controlId: n, state: t, config: a }) => {
  var m;
  const [l, c] = Se(), i = Nt(
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
}, { useCallback: Rt } = e, Lt = ({ controlId: n, state: t }) => {
  const [a, l] = Se(), c = Rt(
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
}, xt = ({ controlId: n, state: t }) => {
  const a = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: n, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, a.map((o) => {
    const u = o.cellModule ? Et(o.cellModule) : void 0, s = c[o.name];
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
function Ee({ encoded: n, className: t }) {
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
const { useCallback: Dt } = e, It = ({ controlId: n, command: t, label: a, disabled: l }) => {
  const c = X(), i = le(), o = t ?? "click", u = a ?? c.label, s = l ?? c.disabled === !0, r = c.hidden === !0, m = c.image, p = c.iconOnly === !0, _ = r ? { display: "none" } : void 0, N = Dt(() => {
    i(o);
  }, [i, o]);
  return m && p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: N,
      disabled: s,
      style: _,
      className: "tlReactButton tlReactButton--icon",
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(Ee, { encoded: m })
  ) : m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: N,
      disabled: s,
      style: _,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(Ee, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: N,
      disabled: s,
      style: _,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: jt } = e, Pt = ({ controlId: n, command: t, label: a, active: l, disabled: c }) => {
  const i = X(), o = le(), u = t ?? "click", s = a ?? i.label, r = l ?? i.active === !0, m = c ?? i.disabled === !0, p = jt(() => {
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
}, Mt = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: At } = e, Bt = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.tabs ?? [], c = t.activeTabId, i = At((o) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, Ot = ({ controlId: n }) => {
  const t = X(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: c })))));
}, $t = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ft = ({ controlId: n }) => {
  const t = X(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, _ = m === "received" ? "idle" : l !== "idle" ? l : m, N = e.useCallback(async () => {
    if (l === "recording") {
      const k = u.current;
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
        r.current = k, s.current = [];
        const O = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, O ? { mimeType: O } : void 0);
        u.current = P, P.ondataavailable = (f) => {
          f.data.size > 0 && s.current.push(f.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((C) => C.stop()), r.current = null;
          const f = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], f.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", f, "recording.webm"), await a(g), c("idle");
        }, P.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, a]), E = oe($t), b = _ === "recording" ? E["js.audioRecorder.stop"] : _ === "uploading" ? E["js.uploading"] : E["js.audioRecorder.record"], h = _ === "uploading", T = ["tlAudioRecorder__button"];
  return _ === "recording" && T.push("tlAudioRecorder__button--recording"), _ === "uploading" && T.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: N,
      disabled: h,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, E[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Ht = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Wt = ({ controlId: n }) => {
  const t = X(), a = qe(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
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
        const h = await fetch(a);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), o("idle");
          return;
        }
        const T = await h.blob();
        s.current = URL.createObjectURL(T);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), o("idle");
        return;
      }
    }
    const b = new Audio(s.current);
    u.current = b, b.onended = () => {
      o("idle");
    }, b.play(), o("playing");
  }, [i, a]), p = oe(Ht), _ = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], N = i === "disabled" || i === "loading", E = ["tlAudioPlayer__button"];
  return i === "playing" && E.push("tlAudioPlayer__button--playing"), i === "loading" && E.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: m,
      disabled: N,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, zt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ut = ({ controlId: n }) => {
  const t = X(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, _ = e.useCallback(async (f) => {
    c("uploading");
    const g = new FormData();
    g.append("file", f, f.name), await a(g), c("idle");
  }, [a]), N = e.useCallback((f) => {
    var C;
    const g = (C = f.target.files) == null ? void 0 : C[0];
    g && _(g);
  }, [_]), E = e.useCallback(() => {
    var f;
    l !== "uploading" && ((f = u.current) == null || f.click());
  }, [l]), b = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), h = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), T = e.useCallback((f) => {
    var C;
    if (f.preventDefault(), f.stopPropagation(), o(!1), l === "uploading") return;
    const g = (C = f.dataTransfer.files) == null ? void 0 : C[0];
    g && _(g);
  }, [l, _]), k = p === "uploading", O = oe(zt), P = p === "uploading" ? O["js.uploading"] : O["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: h,
      onDrop: T
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
        onClick: E,
        disabled: k,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, Vt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Kt = ({ controlId: n }) => {
  const t = X(), a = qe(), l = le(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const E = a + (a.includes("?") ? "&" : "?") + "rev=" + i, b = await fetch(E);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const h = await b.blob(), T = URL.createObjectURL(h), k = document.createElement("a");
        k.href = T, k.download = o, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(T);
      } catch (E) {
        console.error("[TLDownload] Fetch error:", E);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), _ = oe(Vt);
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
}, Yt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Gt = ({ controlId: n }) => {
  const t = X(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), _ = e.useRef(null), N = e.useRef(null), E = t.error, b = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), T = e.useCallback(() => {
    h(), c("idle");
  }, [h]), k = e.useCallback(async () => {
    var L;
    if (l !== "uploading") {
      if (o(null), !b) {
        (L = _.current) == null || L.click();
        return;
      }
      try {
        const D = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = D, c("overlayOpen");
      } catch (D) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", D), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [l, b]), O = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const L = r.current, D = p.current;
    if (!L || !D)
      return;
    D.width = L.videoWidth, D.height = L.videoHeight;
    const y = D.getContext("2d");
    y && (y.drawImage(L, 0, 0), h(), c("uploading"), D.toBlob(async (A) => {
      if (!A) {
        c("idle");
        return;
      }
      const z = new FormData();
      z.append("photo", A, "capture.jpg"), await a(z), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, a, h]), P = e.useCallback(async (L) => {
    var A;
    const D = (A = L.target.files) == null ? void 0 : A[0];
    if (!D) return;
    c("uploading");
    const y = new FormData();
    y.append("photo", D, D.name), await a(y), c("idle"), _.current && (_.current.value = "");
  }, [a]);
  e.useEffect(() => {
    l === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var D;
    if (l !== "overlayOpen") return;
    (D = N.current) == null || D.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const L = (D) => {
      D.key === "Escape" && T();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [l, T]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const f = oe(Yt), g = l === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const q = ["tlPhotoCapture__overlayVideo"];
  u && q.push("tlPhotoCapture__overlayVideo--mirrored");
  const R = ["tlPhotoCapture__mirrorBtn"];
  return u && R.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: k,
      disabled: l === "uploading",
      title: g,
      "aria-label": g
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
        ref: r,
        className: q.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: R.join(" "),
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
        onClick: O,
        title: f["js.photoCapture.capture"],
        "aria-label": f["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: T,
        title: f["js.photoCapture.close"],
        "aria-label": f["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[i]), E && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E));
}, Xt = {
  "js.photoViewer.alt": "Captured photo"
}, qt = ({ controlId: n }) => {
  const t = X(), a = qe(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
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
  const s = oe(Xt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: tt, useRef: Me } = e, Zt = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", u = i.length > 0 && i.every((h) => h.collapsed), s = !u && i.some((h) => h.collapsed), r = u ? !o : o, m = Me(null), p = Me(null), _ = Me(null), N = tt((h, T) => {
    const k = {
      overflow: h.scrolling || "auto"
    };
    return h.collapsed ? u && !r ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : T !== void 0 ? k.flex = `0 0 ${T}px` : h.unit === "%" || s ? k.flex = `${h.size} 0 0%` : k.flex = `0 0 ${h.size}px`, h.minSize > 0 && !h.collapsed && (k.minWidth = o ? h.minSize : void 0, k.minHeight = o ? void 0 : h.minSize), k;
  }, [o, u, s, r]), E = tt((h, T) => {
    h.preventDefault();
    const k = m.current;
    if (!k) return;
    const O = i[T], P = i[T + 1], f = k.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    f.forEach((R) => {
      g.push(o ? R.offsetWidth : R.offsetHeight);
    }), _.current = g, p.current = {
      splitterIndex: T,
      startPos: o ? h.clientX : h.clientY,
      startSizeBefore: g[T],
      startSizeAfter: g[T + 1],
      childBefore: O,
      childAfter: P
    };
    const C = (R) => {
      const L = p.current;
      if (!L || !_.current) return;
      const y = (o ? R.clientX : R.clientY) - L.startPos, A = L.childBefore.minSize || 0, z = L.childAfter.minSize || 0;
      let te = L.startSizeBefore + y, Z = L.startSizeAfter - y;
      te < A && (Z += te - A, te = A), Z < z && (te += Z - z, Z = z), _.current[L.splitterIndex] = te, _.current[L.splitterIndex + 1] = Z;
      const ne = k.querySelectorAll(":scope > .tlSplitPanel__child"), I = ne[L.splitterIndex], $ = ne[L.splitterIndex + 1];
      I && (I.style.flex = `0 0 ${te}px`), $ && ($.style.flex = `0 0 ${Z}px`);
    }, q = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", q), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const R = {};
        i.forEach((L, D) => {
          const y = L.control;
          y != null && y.controlId && _.current && (R[y.controlId] = _.current[D]);
        }), a("updateSizes", { sizes: R });
      }
      _.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", q), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), b = [];
  return i.forEach((h, T) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${T}`,
          className: `tlSplitPanel__child${h.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(h)
        },
        /* @__PURE__ */ e.createElement(G, { control: h.control })
      )
    ), c && T < i.length - 1) {
      const k = i[T + 1];
      !h.collapsed && !k.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${T}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (P) => E(P, T)
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
}, { useCallback: Ae } = e, Qt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Jt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), en = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), rn = ({ controlId: n }) => {
  const t = X(), a = le(), l = oe(Qt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, r = t.fullLine === !0, m = t.toolbarButtons ?? [], p = i === "MINIMIZED", _ = i === "MAXIMIZED", N = i === "HIDDEN", E = Ae(() => {
    a("toggleMinimize");
  }, [a]), b = Ae(() => {
    a("toggleMaximize");
  }, [a]), h = Ae(() => {
    a("popOut");
  }, [a]);
  if (N)
    return null;
  const T = _ ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}${r ? " tlPanel--fullLine" : ""}`,
      style: T
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((k, O) => /* @__PURE__ */ e.createElement("span", { key: O, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(G, { control: k }))), o && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(en, null) : /* @__PURE__ */ e.createElement(Jt, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: _ ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      _ ? /* @__PURE__ */ e.createElement(nn, null) : /* @__PURE__ */ e.createElement(tn, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ln, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(G, { control: t.child }))
  );
}, an = ({ controlId: n }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(G, { control: t.child })
  );
}, on = ({ controlId: n }) => {
  const t = X();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild }));
}, { useCallback: ue, useState: De, useEffect: Ie, useRef: je } = e, sn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ke(n, t, a, l) {
  const c = [];
  for (const i of n)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: l }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: l }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (a.get(i.id) ?? i.expanded) && !t && c.push(...Ke(i.children, t, a, i.id)));
  return c;
}
const ge = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, cn = ({ item: n, active: t, collapsed: a, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: a ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  a && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ge, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(ge, { icon: n.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
  !a && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
), un = ({ item: n, collapsed: t, onExecute: a, tabIndex: l, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(n.id),
    title: t ? n.label : void 0,
    tabIndex: l,
    ref: c,
    onFocus: () => i(n.id)
  },
  /* @__PURE__ */ e.createElement(ge, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), dn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(ge, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), mn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), pn = ({ item: n, activeItemId: t, anchorRect: a, onSelect: l, onExecute: c, onClose: i }) => {
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
        /* @__PURE__ */ e.createElement(ge, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, fn = ({
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
  onOpenFlyout: E,
  onCloseFlyout: b
}) => {
  const h = je(null), [T, k] = De(null), O = ue(() => {
    l ? N === n.id ? b() : (h.current && k(h.current.getBoundingClientRect()), E(n.id)) : o(n.id);
  }, [l, N, n.id, o, E, b]), P = ue((g) => {
    h.current = g, s(g);
  }, [s]), f = l && N === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: O,
      title: l ? n.label : void 0,
      "aria-expanded": l ? f : t,
      tabIndex: u,
      ref: P,
      onFocus: () => r(n.id)
    },
    /* @__PURE__ */ e.createElement(ge, { icon: n.icon }),
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
  ), f && /* @__PURE__ */ e.createElement(
    pn,
    {
      item: n,
      activeItemId: a,
      anchorRect: T,
      onSelect: c,
      onExecute: i,
      onClose: b
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, n.children.map((g) => /* @__PURE__ */ e.createElement(
    mt,
    {
      key: g.id,
      item: g,
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
      onOpenFlyout: E,
      onCloseFlyout: b
    }
  ))));
}, mt = ({
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
        cn,
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
        un,
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
      return /* @__PURE__ */ e.createElement(dn, { item: n, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(mn, null);
    case "group": {
      const N = r ? r.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        fn,
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
}, hn = ({ controlId: n }) => {
  const t = X(), a = le(), l = oe(sn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = De(() => {
    const R = /* @__PURE__ */ new Map(), L = (D) => {
      for (const y of D)
        y.type === "group" && (R.set(y.id, y.expanded), L(y.children));
    };
    return L(c), R;
  }), r = ue((R) => {
    s((L) => {
      const D = new Map(L), y = D.get(R) ?? !1;
      return D.set(R, !y), a("toggleGroup", { itemId: R, expanded: !y }), D;
    });
  }, [a]), m = ue((R) => {
    R !== i && a("selectItem", { itemId: R });
  }, [a, i]), p = ue((R) => {
    a("executeCommand", { itemId: R });
  }, [a]), _ = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [N, E] = De(null), b = ue((R) => {
    E(R);
  }, []), h = ue(() => {
    E(null);
  }, []);
  Ie(() => {
    o || E(null);
  }, [o]);
  const [T, k] = De(() => {
    const R = Ke(c, o, u);
    return R.length > 0 ? R[0].id : "";
  }), O = je(/* @__PURE__ */ new Map()), P = ue((R) => (L) => {
    L ? O.current.set(R, L) : O.current.delete(R);
  }, []), f = ue((R) => {
    k(R);
  }, []), g = je(0), C = ue((R) => {
    k(R), g.current++;
  }, []);
  Ie(() => {
    const R = O.current.get(T);
    R && document.activeElement !== R && R.focus();
  }, [T, g.current]);
  const q = ue((R) => {
    if (R.key === "Escape" && N !== null) {
      R.preventDefault(), h();
      return;
    }
    const L = Ke(c, o, u);
    if (L.length === 0) return;
    const D = L.findIndex((A) => A.id === T);
    if (D < 0) return;
    const y = L[D];
    switch (R.key) {
      case "ArrowDown": {
        R.preventDefault();
        const A = (D + 1) % L.length;
        C(L[A].id);
        break;
      }
      case "ArrowUp": {
        R.preventDefault();
        const A = (D - 1 + L.length) % L.length;
        C(L[A].id);
        break;
      }
      case "Home": {
        R.preventDefault(), C(L[0].id);
        break;
      }
      case "End": {
        R.preventDefault(), C(L[L.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        R.preventDefault(), y.type === "nav" ? m(y.id) : y.type === "command" ? p(y.id) : y.type === "group" && (o ? N === y.id ? h() : b(y.id) : r(y.id));
        break;
      }
      case "ArrowRight": {
        y.type === "group" && !o && ((u.get(y.id) ?? !1) || (R.preventDefault(), r(y.id)));
        break;
      }
      case "ArrowLeft": {
        y.type === "group" && !o && (u.get(y.id) ?? !1) && (R.preventDefault(), r(y.id));
        break;
      }
    }
  }, [
    c,
    o,
    u,
    T,
    N,
    C,
    m,
    p,
    r,
    b,
    h
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: q }, c.map((R) => /* @__PURE__ */ e.createElement(
    mt,
    {
      key: R.id,
      item: R,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: T,
      setItemRef: P,
      onItemFocus: f,
      groupStates: u,
      flyoutGroupId: N,
      onOpenFlyout: b,
      onCloseFlyout: h
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, _n = ({ controlId: n }) => {
  const t = X(), a = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: u }, o.map((s, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: s })));
}, bn = ({ controlId: n }) => {
  const t = X(), a = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(G, { key: s, control: u })));
}, vn = ({ controlId: n }) => {
  const t = X(), a = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(G, { control: o })));
}, En = ({ controlId: n }) => {
  const t = X(), a = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: u }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(G, { key: r, control: s }))));
}, { useCallback: gn } = e, Cn = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.items ?? [], c = gn((i) => {
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
}, { useCallback: yn } = e, wn = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.items ?? [], c = t.activeItemId, i = yn((o) => {
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
}, { useCallback: nt, useEffect: lt, useRef: kn } = e, Sn = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = kn(null), u = nt(() => {
    a("close");
  }, [a]), s = nt((r) => {
    c && r.target === r.currentTarget && u();
  }, [c, u]);
  return lt(() => {
    if (!l) return;
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [l, u]), lt(() => {
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
    /* @__PURE__ */ e.createElement(G, { control: i })
  ) : null;
}, { useEffect: Nn, useRef: Tn } = e, Rn = ({ controlId: n }) => {
  const a = X().dialogs ?? [], l = Tn(a.length);
  return Nn(() => {
    a.length < l.current && a.length > 0, l.current = a.length;
  }, [a.length]), a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialogManager" }, a.map((c) => /* @__PURE__ */ e.createElement(G, { key: c.controlId, control: c })));
}, { useCallback: Be, useRef: ye, useState: Oe } = e, Ln = {
  "js.window.close": "Close"
}, xn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Dn = ({ controlId: n }) => {
  const t = X(), a = le(), l = oe(Ln), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.resizable === !0, s = t.child, r = t.actions ?? [], m = t.toolbarButtons ?? [], [p, _] = Oe(null), [N, E] = Oe(null), [b, h] = Oe(null), T = ye(null), k = ye(null), O = ye(null), P = ye(null), f = ye(null), g = Be(() => {
    a("close");
  }, [a]), C = Be((D, y) => {
    y.preventDefault();
    const A = P.current;
    if (!A) return;
    const z = A.getBoundingClientRect();
    f.current = {
      dir: D,
      startX: y.clientX,
      startY: y.clientY,
      startW: z.width,
      startH: z.height
    };
    const te = (ne) => {
      const I = f.current;
      if (!I) return;
      const $ = ne.clientX - I.startX, K = ne.clientY - I.startY;
      let d = I.startW, v = I.startH;
      I.dir.includes("e") && (d = I.startW + $), I.dir.includes("w") && (d = I.startW - $), I.dir.includes("s") && (v = I.startH + K), I.dir.includes("n") && (v = I.startH - K);
      const B = Math.max(200, d), M = Math.max(100, v);
      k.current = B, O.current = M, _(B), E(M);
    }, Z = () => {
      document.removeEventListener("mousemove", te), document.removeEventListener("mouseup", Z);
      const ne = k.current, I = O.current;
      (ne != null || I != null) && (a("resize", {
        ...ne != null ? { width: Math.round(ne) + "px" } : {},
        ...I != null ? { height: Math.round(I) + "px" } : {}
      }), k.current = null, O.current = null, _(null), E(null)), f.current = null;
    };
    document.addEventListener("mousemove", te), document.addEventListener("mouseup", Z);
  }, [a]), q = Be((D) => {
    if (D.button !== 0 || D.target.closest("button")) return;
    D.preventDefault();
    const y = P.current;
    if (!y) return;
    const A = y.getBoundingClientRect(), z = T.current ?? { x: A.left, y: A.top }, te = D.clientX - z.x, Z = D.clientY - z.y, ne = ($) => {
      const K = window.innerWidth, d = window.innerHeight;
      let v = $.clientX - te, B = $.clientY - Z;
      const M = y.offsetWidth, V = y.offsetHeight;
      v + M > K && (v = K - M), B + V > d && (B = d - V), v < 0 && (v = 0), B < 0 && (B = 0);
      const Y = { x: v, y: B };
      T.current = Y, h(Y);
    }, I = () => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", I);
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", I);
  }, []), R = {
    width: p != null ? p + "px" : i,
    ...N != null ? { height: N + "px" } : o != null ? { height: o } : {},
    maxHeight: b ? "100vh" : "80vh",
    ...b ? { position: "absolute", left: b.x + "px", top: b.y + "px" } : {}
  }, L = n + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlWindow",
      style: R,
      ref: P,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": L
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header", onMouseDown: q }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: L }, c), m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((D, y) => /* @__PURE__ */ e.createElement("span", { key: y, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(G, { control: D })))), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: g,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(G, { control: s })),
    r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, r.map((D, y) => /* @__PURE__ */ e.createElement(G, { key: y, control: D }))),
    u && xn.map((D) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: D,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${D}`,
        onMouseDown: (y) => C(D, y)
      }
    ))
  );
}, { useCallback: In, useEffect: jn } = e, Pn = {
  "js.drawer.close": "Close"
}, Mn = ({ controlId: n }) => {
  const t = X(), a = le(), l = oe(Pn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, r = In(() => {
    a("close");
  }, [a]);
  jn(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(G, { control: s })));
}, { useCallback: An, useEffect: Bn, useState: On } = e, $n = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.duration ?? 5e3, u = t.visible === !0, s = t.generation ?? 0, [r, m] = On(!1), p = An(() => {
    m(!0), setTimeout(() => {
      a("dismiss", { generation: s }), m(!1);
    }, 200);
  }, [a, s]);
  return Bn(() => {
    if (!u || o === 0) return;
    const _ = setTimeout(p, o);
    return () => clearTimeout(_);
  }, [u, o, p]), !u && !r ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlSnackbar tlSnackbar--${i}${r ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l)
  );
}, { useCallback: $e, useEffect: Fe, useRef: Fn, useState: rt } = e, Hn = ({ controlId: n }) => {
  const t = X(), a = le(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Fn(null), [u, s] = rt({ top: 0, left: 0 }), [r, m] = rt(0), p = i.filter((b) => b.type === "item" && !b.disabled);
  Fe(() => {
    var f, g;
    if (!l || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const h = b.getBoundingClientRect(), T = ((f = o.current) == null ? void 0 : f.offsetHeight) ?? 200, k = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let O = h.bottom + 4, P = h.left;
    O + T > window.innerHeight && (O = h.top - T - 4), P + k > window.innerWidth && (P = h.right - k), s({ top: O, left: P }), m(0);
  }, [l, c]);
  const _ = $e(() => {
    a("close");
  }, [a]), N = $e((b) => {
    a("selectItem", { itemId: b });
  }, [a]);
  Fe(() => {
    if (!l) return;
    const b = (h) => {
      o.current && !o.current.contains(h.target) && _();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [l, _]);
  const E = $e((b) => {
    if (b.key === "Escape") {
      _();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), m((h) => (h + 1) % p.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), m((h) => (h - 1 + p.length) % p.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const h = p[r];
      h && N(h.id);
    }
  }, [_, N, p, r]);
  return Fe(() => {
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
      onKeyDown: E
    },
    i.map((b, h) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: h, className: "tlMenu__separator" });
      const k = p.indexOf(b) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => N(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, Wn = ({ controlId: n }) => {
  const t = X(), a = t.header, l = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(G, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: c })), /* @__PURE__ */ e.createElement(G, { control: i }), o && /* @__PURE__ */ e.createElement(G, { control: o }));
}, zn = () => {
  const t = X().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Un = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, at = 50, Vn = () => {
  const n = X(), t = le(), a = oe(Un), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, u = n.selectionMode ?? "single", s = n.selectedCount ?? 0, r = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((S) => S.sortPriority && S.sortPriority > 0).length,
    [l]
  ), _ = u === "multi", N = 40, E = 20, b = e.useRef(null), h = e.useRef(null), T = e.useRef(null), [k, O] = e.useState({}), P = e.useRef(null), f = e.useRef(!1), g = e.useRef(null), [C, q] = e.useState(null), [R, L] = e.useState(null);
  e.useEffect(() => {
    P.current || O({});
  }, [l]);
  const D = e.useCallback((S) => k[S.name] ?? S.width, [k]), y = e.useMemo(() => {
    const S = [];
    let x = _ && r > 0 ? N : 0;
    for (let U = 0; U < r && U < l.length; U++)
      S.push(x), x += D(l[U]);
    return S;
  }, [l, r, _, N, D]), A = c * o, z = e.useRef(null), te = e.useCallback((S, x, U) => {
    U.preventDefault(), U.stopPropagation(), P.current = { column: S, startX: U.clientX, startWidth: x };
    let Q = U.clientX, w = 0;
    const j = () => {
      const ae = P.current;
      if (!ae) return;
      const de = Math.max(at, ae.startWidth + (Q - ae.startX) + w);
      O((_e) => ({ ..._e, [ae.column]: de }));
    }, H = () => {
      const ae = h.current, de = b.current;
      if (!ae || !P.current) return;
      const _e = ae.getBoundingClientRect(), Qe = 40, Je = 8, vt = ae.scrollLeft;
      Q > _e.right - Qe ? ae.scrollLeft += Je : Q < _e.left + Qe && (ae.scrollLeft = Math.max(0, ae.scrollLeft - Je));
      const et = ae.scrollLeft - vt;
      et !== 0 && (de && (de.scrollLeft = ae.scrollLeft), w += et, j()), z.current = requestAnimationFrame(H);
    };
    z.current = requestAnimationFrame(H);
    const ee = (ae) => {
      Q = ae.clientX, j();
    }, pe = (ae) => {
      document.removeEventListener("mousemove", ee), document.removeEventListener("mouseup", pe), z.current !== null && (cancelAnimationFrame(z.current), z.current = null);
      const de = P.current;
      if (de) {
        const _e = Math.max(at, de.startWidth + (ae.clientX - de.startX) + w);
        t("columnResize", { column: de.column, width: _e }), P.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ee), document.addEventListener("mouseup", pe);
  }, [t]), Z = e.useCallback(() => {
    b.current && h.current && (b.current.scrollLeft = h.current.scrollLeft), T.current !== null && clearTimeout(T.current), T.current = window.setTimeout(() => {
      const S = h.current;
      if (!S) return;
      const x = S.scrollTop, U = Math.ceil(S.clientHeight / o), Q = Math.floor(x / o);
      t("scroll", { start: Q, count: U });
    }, 80);
  }, [t, o]), ne = e.useCallback((S, x, U) => {
    if (f.current) return;
    let Q;
    !x || x === "desc" ? Q = "asc" : Q = "desc";
    const w = U.shiftKey ? "add" : "replace";
    t("sort", { column: S, direction: Q, mode: w });
  }, [t]), I = e.useCallback((S, x) => {
    g.current = S, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", S);
  }, []), $ = e.useCallback((S, x) => {
    if (!g.current || g.current === S) {
      q(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), Q = x.clientX < U.left + U.width / 2 ? "left" : "right";
    q({ column: S, side: Q });
  }, []), K = e.useCallback((S) => {
    S.preventDefault(), S.stopPropagation();
    const x = g.current;
    if (!x || !C) {
      g.current = null, q(null);
      return;
    }
    let U = l.findIndex((w) => w.name === C.column);
    if (U < 0) {
      g.current = null, q(null);
      return;
    }
    const Q = l.findIndex((w) => w.name === x);
    C.side === "right" && U++, Q < U && U--, t("columnReorder", { column: x, targetIndex: U }), g.current = null, q(null);
  }, [l, C, t]), d = e.useCallback(() => {
    g.current = null, q(null);
  }, []), v = e.useCallback((S, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: S,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), B = e.useCallback((S, x) => {
    x.stopPropagation(), t("select", { rowIndex: S, ctrlKey: !0, shiftKey: !1 });
  }, [t]), M = e.useCallback(() => {
    const S = s === c && c > 0;
    t("selectAll", { selected: !S });
  }, [t, s, c]), V = e.useCallback((S, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: S, expanded: x });
  }, [t]), Y = e.useCallback((S, x) => {
    x.preventDefault(), L({ x: x.clientX, y: x.clientY, colIdx: S });
  }, []), J = e.useCallback(() => {
    R && (t("setFrozenColumnCount", { count: R.colIdx + 1 }), L(null));
  }, [R, t]), ie = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), L(null);
  }, [t]);
  e.useEffect(() => {
    if (!R) return;
    const S = () => L(null), x = (U) => {
      U.key === "Escape" && L(null);
    };
    return document.addEventListener("mousedown", S), document.addEventListener("keydown", x), () => {
      document.removeEventListener("mousedown", S), document.removeEventListener("keydown", x);
    };
  }, [R]);
  const re = l.reduce((S, x) => S + D(x), 0) + (_ ? N : 0), Ce = s === c && c > 0, Ne = s > 0 && s < c, Pe = e.useCallback((S) => {
    S && (S.indeterminate = Ne);
  }, [Ne]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (S) => {
        if (!g.current) return;
        S.preventDefault();
        const x = h.current, U = b.current;
        if (!x) return;
        const Q = x.getBoundingClientRect(), w = 40, j = 8;
        S.clientX < Q.left + w ? x.scrollLeft = Math.max(0, x.scrollLeft - j) : S.clientX > Q.right - w && (x.scrollLeft += j), U && (U.scrollLeft = x.scrollLeft);
      },
      onDrop: K
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: re } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: N,
          minWidth: N,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (S) => {
          g.current && (S.preventDefault(), S.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== g.current && q({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Pe,
          className: "tlTableView__checkbox",
          checked: Ce,
          onChange: M
        }
      )
    ), l.map((S, x) => {
      const U = D(S);
      l.length - 1;
      let Q = "tlTableView__headerCell";
      S.sortable && (Q += " tlTableView__headerCell--sortable"), C && C.column === S.name && (Q += " tlTableView__headerCell--dragOver-" + C.side);
      const w = x < r, j = x === r - 1;
      return w && (Q += " tlTableView__headerCell--frozen"), j && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.name,
          className: Q,
          style: {
            width: U,
            minWidth: U,
            position: w ? "sticky" : "relative",
            ...w ? { left: y[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: S.sortable ? (H) => ne(S.name, S.sortDirection, H) : void 0,
          onContextMenu: (H) => Y(x, H),
          onDragStart: (H) => I(S.name, H),
          onDragOver: (H) => $(S.name, H),
          onDrop: K,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, S.label),
        S.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, S.sortDirection === "asc" ? "▲" : "▼", p > 1 && S.sortPriority != null && S.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, S.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (H) => te(S.name, U, H)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (S) => {
          if (g.current && l.length > 0) {
            const x = l[l.length - 1];
            x.name !== g.current && (S.preventDefault(), S.dataTransfer.dropEffect = "move", q({ column: x.name, side: "right" }));
          }
        },
        onDrop: K
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: h,
        className: "tlTableView__body",
        onScroll: Z
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: A, position: "relative", width: re } }, i.map((S) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.id,
          className: "tlTableView__row" + (S.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: S.index * o,
            height: o,
            width: re
          },
          onClick: (x) => v(S.index, x)
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
              checked: S.selected,
              onChange: () => {
              },
              onClick: (x) => B(S.index, x),
              tabIndex: -1
            }
          )
        ),
        l.map((x, U) => {
          const Q = D(x), w = U === l.length - 1, j = U < r, H = U === r - 1;
          let ee = "tlTableView__cell";
          j && (ee += " tlTableView__cell--frozen"), H && (ee += " tlTableView__cell--frozenLast");
          const pe = m && U === 0, ae = S.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: ee,
              style: {
                ...w && !j ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...j ? { position: "sticky", left: y[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * E } }, S.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => V(S.index, !S.expanded, de)
              },
              S.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(G, { control: S.cells[x.name] })) : /* @__PURE__ */ e.createElement(G, { control: S.cells[x.name] })
          );
        })
      )))
    ),
    R && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: R.y, left: R.x, zIndex: 1e4 },
        onMouseDown: (S) => S.stopPropagation()
      },
      R.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: J }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Kn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, pt = e.createContext(Kn), { useMemo: Yn, useRef: Gn, useState: Xn, useEffect: qn } = e, Zn = 320, Qn = ({ controlId: n }) => {
  const t = X(), a = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Gn(null), [s, r] = Xn(
    l === "top" ? "top" : "side"
  );
  qn(() => {
    if (l !== "auto") {
      r(l);
      return;
    }
    const E = u.current;
    if (!E) return;
    const b = new ResizeObserver((h) => {
      for (const T of h) {
        const O = T.contentRect.width / a;
        r(O < Zn ? "top" : "side");
      }
    });
    return b.observe(E), () => b.disconnect();
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
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(pt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: N, style: _, ref: u }, i.map((E, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: E }))));
}, { useCallback: Jn } = e, el = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, tl = ({ controlId: n }) => {
  const t = X(), a = le(), l = oe(el), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, _ = Jn(() => {
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((E, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: E })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((E, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: E }))));
}, { useContext: nl, useState: ll, useCallback: rl } = e, al = ({ controlId: n }) => {
  const t = X(), a = nl(pt), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, r = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, _ = t.field, N = a.readOnly, [E, b] = ll(!1), h = rl(() => b((P) => !P), []);
  if (!p) return null;
  const T = i != null, k = o != null && o.length > 0, O = [
    "tlFormField",
    `tlFormField--${r}`,
    N ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    T ? "tlFormField--error" : "",
    !T && k ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: O }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !N && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !N && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: _ })), !N && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !N && !T && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((P, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, P)))), !N && u && E && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, ol = () => {
  const n = X(), t = le(), a = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, u = n.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, sl = 20, cl = () => {
  const n = X(), t = le(), a = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, u = n.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((f, g) => {
    t(g ? "collapse" : "expand", { nodeId: f });
  }, [t]), _ = e.useCallback((f, g) => {
    t("select", {
      nodeId: f,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), N = e.useCallback((f, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: f, x: g.clientX, y: g.clientY });
  }, [t]), E = e.useRef(null), b = e.useCallback((f, g) => {
    const C = g.getBoundingClientRect(), q = f.clientY - C.top, R = C.height / 3;
    return q < R ? "above" : q > R * 2 ? "below" : "within";
  }, []), h = e.useCallback((f, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", f);
  }, []), T = e.useCallback((f, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const C = b(g, g.currentTarget);
    E.current != null && window.clearTimeout(E.current), E.current = window.setTimeout(() => {
      t("dragOver", { nodeId: f, position: C }), E.current = null;
    }, 50);
  }, [t, b]), k = e.useCallback((f, g) => {
    g.preventDefault(), E.current != null && (window.clearTimeout(E.current), E.current = null);
    const C = b(g, g.currentTarget);
    t("drop", { nodeId: f, position: C });
  }, [t, b]), O = e.useCallback(() => {
    E.current != null && (window.clearTimeout(E.current), E.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((f) => {
    if (a.length === 0) return;
    let g = s;
    switch (f.key) {
      case "ArrowDown":
        f.preventDefault(), g = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        f.preventDefault(), g = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (f.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const q = C.depth;
            for (let R = s - 1; R >= 0; R--)
              if (a[R].depth < q) {
                g = R;
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
        f.preventDefault(), l === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        f.preventDefault(), g = 0;
        break;
      case "End":
        f.preventDefault(), g = a.length - 1;
        break;
      default:
        return;
    }
    g !== s && r(g);
  }, [s, a, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    a.map((f, g) => /* @__PURE__ */ e.createElement(
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
          g === s ? "tlTreeView__node--focused" : "",
          o === f.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === f.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === f.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: f.depth * sl },
        draggable: c,
        onClick: (C) => _(f.id, C),
        onContextMenu: (C) => N(f.id, C),
        onDragStart: (C) => h(f.id, C),
        onDragOver: i ? (C) => T(f.id, C) : void 0,
        onDrop: i ? (C) => k(f.id, C) : void 0,
        onDragEnd: O
      },
      f.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(f.id, f.expanded);
          },
          tabIndex: -1,
          "aria-label": f.expanded ? "Collapse" : "Expand"
        },
        f.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: f.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: f.content }))
    ))
  );
};
var He = { exports: {} }, se = {}, We = { exports: {} }, W = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ot;
function il() {
  if (ot) return W;
  ot = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), _ = Symbol.iterator;
  function N(d) {
    return d === null || typeof d != "object" ? null : (d = _ && d[_] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var E = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, b = Object.assign, h = {};
  function T(d, v, B) {
    this.props = d, this.context = v, this.refs = h, this.updater = B || E;
  }
  T.prototype.isReactComponent = {}, T.prototype.setState = function(d, v) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, v, "setState");
  }, T.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function k() {
  }
  k.prototype = T.prototype;
  function O(d, v, B) {
    this.props = d, this.context = v, this.refs = h, this.updater = B || E;
  }
  var P = O.prototype = new k();
  P.constructor = O, b(P, T.prototype), P.isPureReactComponent = !0;
  var f = Array.isArray;
  function g() {
  }
  var C = { H: null, A: null, T: null, S: null }, q = Object.prototype.hasOwnProperty;
  function R(d, v, B) {
    var M = B.ref;
    return {
      $$typeof: n,
      type: d,
      key: v,
      ref: M !== void 0 ? M : null,
      props: B
    };
  }
  function L(d, v) {
    return R(d.type, v, d.props);
  }
  function D(d) {
    return typeof d == "object" && d !== null && d.$$typeof === n;
  }
  function y(d) {
    var v = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(B) {
      return v[B];
    });
  }
  var A = /\/+/g;
  function z(d, v) {
    return typeof d == "object" && d !== null && d.key != null ? y("" + d.key) : v.toString(36);
  }
  function te(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(g, g) : (d.status = "pending", d.then(
          function(v) {
            d.status === "pending" && (d.status = "fulfilled", d.value = v);
          },
          function(v) {
            d.status === "pending" && (d.status = "rejected", d.reason = v);
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
  function Z(d, v, B, M, V) {
    var Y = typeof d;
    (Y === "undefined" || Y === "boolean") && (d = null);
    var J = !1;
    if (d === null) J = !0;
    else
      switch (Y) {
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
                v,
                B,
                M,
                V
              );
          }
      }
    if (J)
      return V = V(d), J = M === "" ? "." + z(d, 0) : M, f(V) ? (B = "", J != null && (B = J.replace(A, "$&/") + "/"), Z(V, v, B, "", function(Ce) {
        return Ce;
      })) : V != null && (D(V) && (V = L(
        V,
        B + (V.key == null || d && d.key === V.key ? "" : ("" + V.key).replace(
          A,
          "$&/"
        ) + "/") + J
      )), v.push(V)), 1;
    J = 0;
    var ie = M === "" ? "." : M + ":";
    if (f(d))
      for (var re = 0; re < d.length; re++)
        M = d[re], Y = ie + z(M, re), J += Z(
          M,
          v,
          B,
          Y,
          V
        );
    else if (re = N(d), typeof re == "function")
      for (d = re.call(d), re = 0; !(M = d.next()).done; )
        M = M.value, Y = ie + z(M, re++), J += Z(
          M,
          v,
          B,
          Y,
          V
        );
    else if (Y === "object") {
      if (typeof d.then == "function")
        return Z(
          te(d),
          v,
          B,
          M,
          V
        );
      throw v = String(d), Error(
        "Objects are not valid as a React child (found: " + (v === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : v) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function ne(d, v, B) {
    if (d == null) return d;
    var M = [], V = 0;
    return Z(d, M, "", "", function(Y) {
      return v.call(B, Y, V++);
    }), M;
  }
  function I(d) {
    if (d._status === -1) {
      var v = d._result;
      v = v(), v.then(
        function(B) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = B);
        },
        function(B) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = B);
        }
      ), d._status === -1 && (d._status = 0, d._result = v);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var $ = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var v = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(v)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, K = {
    map: ne,
    forEach: function(d, v, B) {
      ne(
        d,
        function() {
          v.apply(this, arguments);
        },
        B
      );
    },
    count: function(d) {
      var v = 0;
      return ne(d, function() {
        v++;
      }), v;
    },
    toArray: function(d) {
      return ne(d, function(v) {
        return v;
      }) || [];
    },
    only: function(d) {
      if (!D(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return W.Activity = p, W.Children = K, W.Component = T, W.Fragment = a, W.Profiler = c, W.PureComponent = O, W.StrictMode = l, W.Suspense = s, W.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, W.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return C.H.useMemoCache(d);
    }
  }, W.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, W.cacheSignal = function() {
    return null;
  }, W.cloneElement = function(d, v, B) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var M = b({}, d.props), V = d.key;
    if (v != null)
      for (Y in v.key !== void 0 && (V = "" + v.key), v)
        !q.call(v, Y) || Y === "key" || Y === "__self" || Y === "__source" || Y === "ref" && v.ref === void 0 || (M[Y] = v[Y]);
    var Y = arguments.length - 2;
    if (Y === 1) M.children = B;
    else if (1 < Y) {
      for (var J = Array(Y), ie = 0; ie < Y; ie++)
        J[ie] = arguments[ie + 2];
      M.children = J;
    }
    return R(d.type, V, M);
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
  }, W.createElement = function(d, v, B) {
    var M, V = {}, Y = null;
    if (v != null)
      for (M in v.key !== void 0 && (Y = "" + v.key), v)
        q.call(v, M) && M !== "key" && M !== "__self" && M !== "__source" && (V[M] = v[M]);
    var J = arguments.length - 2;
    if (J === 1) V.children = B;
    else if (1 < J) {
      for (var ie = Array(J), re = 0; re < J; re++)
        ie[re] = arguments[re + 2];
      V.children = ie;
    }
    if (d && d.defaultProps)
      for (M in J = d.defaultProps, J)
        V[M] === void 0 && (V[M] = J[M]);
    return R(d, Y, V);
  }, W.createRef = function() {
    return { current: null };
  }, W.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, W.isValidElement = D, W.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: I
    };
  }, W.memo = function(d, v) {
    return {
      $$typeof: r,
      type: d,
      compare: v === void 0 ? null : v
    };
  }, W.startTransition = function(d) {
    var v = C.T, B = {};
    C.T = B;
    try {
      var M = d(), V = C.S;
      V !== null && V(B, M), typeof M == "object" && M !== null && typeof M.then == "function" && M.then(g, $);
    } catch (Y) {
      $(Y);
    } finally {
      v !== null && B.types !== null && (v.types = B.types), C.T = v;
    }
  }, W.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, W.use = function(d) {
    return C.H.use(d);
  }, W.useActionState = function(d, v, B) {
    return C.H.useActionState(d, v, B);
  }, W.useCallback = function(d, v) {
    return C.H.useCallback(d, v);
  }, W.useContext = function(d) {
    return C.H.useContext(d);
  }, W.useDebugValue = function() {
  }, W.useDeferredValue = function(d, v) {
    return C.H.useDeferredValue(d, v);
  }, W.useEffect = function(d, v) {
    return C.H.useEffect(d, v);
  }, W.useEffectEvent = function(d) {
    return C.H.useEffectEvent(d);
  }, W.useId = function() {
    return C.H.useId();
  }, W.useImperativeHandle = function(d, v, B) {
    return C.H.useImperativeHandle(d, v, B);
  }, W.useInsertionEffect = function(d, v) {
    return C.H.useInsertionEffect(d, v);
  }, W.useLayoutEffect = function(d, v) {
    return C.H.useLayoutEffect(d, v);
  }, W.useMemo = function(d, v) {
    return C.H.useMemo(d, v);
  }, W.useOptimistic = function(d, v) {
    return C.H.useOptimistic(d, v);
  }, W.useReducer = function(d, v, B) {
    return C.H.useReducer(d, v, B);
  }, W.useRef = function(d) {
    return C.H.useRef(d);
  }, W.useState = function(d) {
    return C.H.useState(d);
  }, W.useSyncExternalStore = function(d, v, B) {
    return C.H.useSyncExternalStore(
      d,
      v,
      B
    );
  }, W.useTransition = function() {
    return C.H.useTransition();
  }, W.version = "19.2.4", W;
}
var st;
function ul() {
  return st || (st = 1, We.exports = il()), We.exports;
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
var ct;
function dl() {
  if (ct) return se;
  ct = 1;
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
var it;
function ml() {
  if (it) return He.exports;
  it = 1;
  function n() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(n);
      } catch (t) {
        console.error(t);
      }
  }
  return n(), He.exports = dl(), He.exports;
}
var pl = ml();
const { useState: fe, useCallback: ce, useRef: we, useEffect: be, useMemo: Ye } = e;
function Ze({ image: n }) {
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
    /* @__PURE__ */ e.createElement(Ze, { image: n.image }),
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
  const o = ce(() => l(n.value), [l, n.value]), u = Ye(() => {
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
    /* @__PURE__ */ e.createElement(Ze, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const _l = ({ controlId: n, state: t }) => {
  const a = le(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", _ = i && c && !u && s, N = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), E = N["js.dropdownSelect.nothingFound"], b = ce(
    (w) => N["js.dropdownSelect.removeChip"].replace("{0}", w),
    [N]
  ), [h, T] = fe(!1), [k, O] = fe(""), [P, f] = fe(-1), [g, C] = fe(!1), [q, R] = fe({}), [L, D] = fe(null), [y, A] = fe(null), [z, te] = fe(null), Z = we(null), ne = we(null), I = we(null), $ = we(l);
  $.current = l;
  const K = we(-1), d = Ye(
    () => new Set(l.map((w) => w.value)),
    [l]
  ), v = Ye(() => {
    let w = m.filter((j) => !d.has(j.value));
    if (k) {
      const j = k.toLowerCase();
      w = w.filter((H) => H.label.toLowerCase().includes(j));
    }
    return w;
  }, [m, d, k]);
  be(() => {
    k && v.length === 1 ? f(0) : f(-1);
  }, [v.length, k]), be(() => {
    h && r && ne.current && ne.current.focus();
  }, [h, r, l]), be(() => {
    var H, ee;
    if (K.current < 0) return;
    const w = K.current;
    K.current = -1;
    const j = (H = Z.current) == null ? void 0 : H.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    j && j.length > 0 ? j[Math.min(w, j.length - 1)].focus() : (ee = Z.current) == null || ee.focus();
  }, [l]), be(() => {
    if (!h) return;
    const w = (j) => {
      Z.current && !Z.current.contains(j.target) && I.current && !I.current.contains(j.target) && (T(!1), O(""));
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [h]), be(() => {
    if (!h || !Z.current) return;
    const w = Z.current.getBoundingClientRect(), j = window.innerHeight - w.bottom, ee = j < 300 && w.top > j;
    R({
      left: w.left,
      width: w.width,
      ...ee ? { bottom: window.innerHeight - w.top } : { top: w.bottom }
    });
  }, [h]);
  const B = ce(async () => {
    if (!(u || !s) && (T(!0), O(""), f(-1), C(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, s, r, a]), M = ce(() => {
    var w;
    T(!1), O(""), f(-1), (w = Z.current) == null || w.focus();
  }, []), V = ce(
    (w) => {
      let j;
      if (c) {
        const H = m.find((ee) => ee.value === w);
        if (H)
          j = [...$.current, H];
        else
          return;
      } else {
        const H = m.find((ee) => ee.value === w);
        if (H)
          j = [H];
        else
          return;
      }
      $.current = j, a("valueChanged", { value: j.map((H) => H.value) }), c ? (O(""), f(-1)) : M();
    },
    [c, m, a, M]
  ), Y = ce(
    (w) => {
      K.current = $.current.findIndex((H) => H.value === w);
      const j = $.current.filter((H) => H.value !== w);
      $.current = j, a("valueChanged", { value: j.map((H) => H.value) });
    },
    [a]
  ), J = ce(
    (w) => {
      w.stopPropagation(), a("valueChanged", { value: [] }), M();
    },
    [a, M]
  ), ie = ce((w) => {
    O(w.target.value);
  }, []), re = ce(
    (w) => {
      if (!h) {
        if (w.key === "ArrowDown" || w.key === "ArrowUp" || w.key === "Enter" || w.key === " ") {
          if (w.target.tagName === "BUTTON") return;
          w.preventDefault(), w.stopPropagation(), B();
        }
        return;
      }
      switch (w.key) {
        case "ArrowDown":
          w.preventDefault(), w.stopPropagation(), f(
            (j) => j < v.length - 1 ? j + 1 : 0
          );
          break;
        case "ArrowUp":
          w.preventDefault(), w.stopPropagation(), f(
            (j) => j > 0 ? j - 1 : v.length - 1
          );
          break;
        case "Enter":
          w.preventDefault(), w.stopPropagation(), P >= 0 && P < v.length && V(v[P].value);
          break;
        case "Escape":
          w.preventDefault(), w.stopPropagation(), M();
          break;
        case "Tab":
          M();
          break;
        case "Backspace":
          k === "" && c && l.length > 0 && Y(l[l.length - 1].value);
          break;
      }
    },
    [
      h,
      B,
      M,
      v,
      P,
      V,
      k,
      c,
      l,
      Y
    ]
  ), Ce = ce(
    async (w) => {
      w.preventDefault(), C(!1);
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
    },
    [a]
  ), Ne = ce(
    (w, j) => {
      D(w), j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", String(w));
    },
    []
  ), Pe = ce(
    (w, j) => {
      if (j.preventDefault(), j.dataTransfer.dropEffect = "move", L === null || L === w) {
        A(null), te(null);
        return;
      }
      const H = j.currentTarget.getBoundingClientRect(), ee = H.left + H.width / 2, pe = j.clientX < ee ? "before" : "after";
      A(w), te(pe);
    },
    [L]
  ), S = ce(
    (w) => {
      if (w.preventDefault(), L === null || y === null || z === null || L === y) return;
      const j = [...$.current], [H] = j.splice(L, 1);
      let ee = y;
      L < y ? ee = z === "before" ? ee - 1 : ee : ee = z === "before" ? ee : ee + 1, j.splice(ee, 0, H), $.current = j, a("valueChanged", { value: j.map((pe) => pe.value) }), D(null), A(null), te(null);
    },
    [L, y, z, a]
  ), x = ce(() => {
    D(null), A(null), te(null);
  }, []);
  if (be(() => {
    if (P < 0 || !I.current) return;
    const w = I.current.querySelector(
      `[id="${n}-opt-${P}"]`
    );
    w && w.scrollIntoView({ block: "nearest" });
  }, [P, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((w) => /* @__PURE__ */ e.createElement("span", { key: w.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ze, { image: w.image }), /* @__PURE__ */ e.createElement("span", null, w.label))));
  const U = !o && l.length > 0 && !u, Q = h ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: I,
      className: "tlDropdownSelect__dropdown",
      style: q
    },
    (r || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: ne,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
        onChange: ie,
        onKeyDown: re,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
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
      !r && !g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: Ce }, N["js.dropdownSelect.error"])),
      r && v.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, E),
      r && v.map((w, j) => /* @__PURE__ */ e.createElement(
        hl,
        {
          key: w.value,
          id: `${n}-opt-${j}`,
          option: w,
          highlighted: j === P,
          searchTerm: k,
          onSelect: V,
          onMouseEnter: () => f(j)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: Z,
      className: "tlDropdownSelect" + (h ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": h,
      "aria-haspopup": "listbox",
      "aria-owns": h ? `${n}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: h ? void 0 : B,
      onKeyDown: re
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((w, j) => {
      let H = "";
      return L === j ? H = "tlDropdownSelect__chip--dragging" : y === j && z === "before" ? H = "tlDropdownSelect__chip--dropBefore" : y === j && z === "after" && (H = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        fl,
        {
          key: w.value,
          option: w,
          removable: !u && (c || !o),
          onRemove: Y,
          removeLabel: b(w.label),
          draggable: _,
          onDragStart: _ ? (ee) => Ne(j, ee) : void 0,
          onDragOver: _ ? (ee) => Pe(j, ee) : void 0,
          onDrop: _ ? S : void 0,
          onDragEnd: _ ? x : void 0,
          dragClassName: _ ? H : void 0
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, h ? "▲" : "▼"))
  ), Q && pl.createPortal(Q, document.body));
}, { useCallback: ze, useRef: bl } = e, ft = "application/x-tl-color", vl = ({
  colors: n,
  columns: t,
  onSelect: a,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = bl(null), u = ze(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = ze((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = ze(
    (m) => (p) => {
      p.preventDefault();
      const _ = p.dataTransfer.getData(ft);
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
function ht(n) {
  return Math.max(0, Math.min(255, Math.round(n)));
}
function Ge(n) {
  return /^#[0-9a-fA-F]{6}$/.test(n);
}
function _t(n) {
  if (!Ge(n)) return [0, 0, 0];
  const t = parseInt(n.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function bt(n, t, a) {
  const l = (c) => ht(c).toString(16).padStart(2, "0");
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
  return El(..._t(n));
}
function Ue(n, t, a) {
  return bt(...gl(n, t, a));
}
const { useCallback: ve, useRef: ut } = e, yl = ({ color: n, onColorChange: t }) => {
  const [a, l, c] = Cl(n), i = ut(null), o = ut(null), u = ve(
    (E, b) => {
      var O;
      const h = (O = i.current) == null ? void 0 : O.getBoundingClientRect();
      if (!h) return;
      const T = Math.max(0, Math.min(1, (E - h.left) / h.width)), k = Math.max(0, Math.min(1, 1 - (b - h.top) / h.height));
      t(Ue(a, T, k));
    },
    [a, t]
  ), s = ve(
    (E) => {
      E.preventDefault(), E.target.setPointerCapture(E.pointerId), u(E.clientX, E.clientY);
    },
    [u]
  ), r = ve(
    (E) => {
      E.buttons !== 0 && u(E.clientX, E.clientY);
    },
    [u]
  ), m = ve(
    (E) => {
      var k;
      const b = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!b) return;
      const T = Math.max(0, Math.min(1, (E - b.top) / b.height)) * 360;
      t(Ue(T, l, c));
    },
    [l, c, t]
  ), p = ve(
    (E) => {
      E.preventDefault(), E.target.setPointerCapture(E.pointerId), m(E.clientY);
    },
    [m]
  ), _ = ve(
    (E) => {
      E.buttons !== 0 && m(E.clientY);
    },
    [m]
  ), N = Ue(a, 1, 1);
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
}, { useState: Te, useCallback: me, useEffect: Ve, useRef: Sl, useLayoutEffect: Nl } = e, Tl = ({
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
  const [r, m] = Te("palette"), [p, _] = Te(t), N = Sl(null), E = oe(kl), [b, h] = Te(null);
  Nl(() => {
    if (!n.current || !N.current) return;
    const I = n.current.getBoundingClientRect(), $ = N.current.getBoundingClientRect();
    let K = I.bottom + 4, d = I.left;
    K + $.height > window.innerHeight && (K = I.top - $.height - 4), d + $.width > window.innerWidth && (d = Math.max(0, I.right - $.width)), h({ top: K, left: d });
  }, [n]);
  const T = p != null, [k, O, P] = T ? _t(p) : [0, 0, 0], [f, g] = Te((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ve(() => {
    g((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ve(() => {
    const I = ($) => {
      $.key === "Escape" && u();
    };
    return document.addEventListener("keydown", I), () => document.removeEventListener("keydown", I);
  }, [u]), Ve(() => {
    const I = (K) => {
      N.current && !N.current.contains(K.target) && u();
    }, $ = setTimeout(() => document.addEventListener("mousedown", I), 0);
    return () => {
      clearTimeout($), document.removeEventListener("mousedown", I);
    };
  }, [u]);
  const C = me(
    (I) => ($) => {
      const K = parseInt($.target.value, 10);
      if (isNaN(K)) return;
      const d = ht(K);
      _(bt(I === "r" ? d : k, I === "g" ? d : O, I === "b" ? d : P));
    },
    [k, O, P]
  ), q = me(
    (I) => {
      if (p != null) {
        I.dataTransfer.setData(ft, p.toUpperCase()), I.dataTransfer.effectAllowed = "move";
        const $ = document.createElement("div");
        $.style.width = "33px", $.style.height = "33px", $.style.backgroundColor = p, $.style.borderRadius = "3px", $.style.border = "1px solid rgba(0,0,0,0.1)", $.style.position = "absolute", $.style.top = "-9999px", document.body.appendChild($), I.dataTransfer.setDragImage($, 16, 16), requestAnimationFrame(() => document.body.removeChild($));
      }
    },
    [p]
  ), R = me((I) => {
    const $ = I.target.value;
    g($), Ge($) && _($);
  }, []), L = me(() => {
    _(null);
  }, []), D = me((I) => {
    _(I);
  }, []), y = me(
    (I) => {
      o(I);
    },
    [o]
  ), A = me(
    (I, $) => {
      const K = [...a], d = K[I];
      K[I] = K[$], K[$] = d, s(K);
    },
    [a, s]
  ), z = me(
    (I, $) => {
      const K = [...a];
      K[I] = $, s(K);
    },
    [a, s]
  ), te = me(() => {
    s([...c]);
  }, [c, s]), Z = me(
    (I) => {
      if (wl(a, I)) return;
      const $ = a.indexOf(null);
      if ($ < 0) return;
      const K = [...a];
      K[$] = I.toUpperCase(), s(K);
    },
    [a, s]
  ), ne = me(() => {
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
      E["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      E["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      vl,
      {
        colors: a,
        columns: l,
        onSelect: D,
        onConfirm: y,
        onSwap: A,
        onReplace: z
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: te }, E["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(yl, { color: p ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, E["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, E["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (T ? "" : " tlColorInput--noColor"),
        style: T ? { backgroundColor: p } : void 0,
        draggable: T,
        onDragStart: T ? q : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? k : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? O : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: T ? P : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, E["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (f !== "" && !Ge(f) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: f,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, E["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, E["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: ne }, E["js.colorInput.ok"]))
  );
}, Rl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Ll, useCallback: Re, useRef: xl } = e, Dl = ({ controlId: n, state: t }) => {
  const a = le(), l = oe(Rl), [c, i] = Ll(!1), o = xl(null), u = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, _ = Re(() => {
    s && i(!0);
  }, [s]), N = Re(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), E = Re(() => {
    i(!1);
  }, []), b = Re(
    (h) => {
      a("paletteChanged", { palette: h });
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
      onCancel: E,
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
}, { useState: ke, useCallback: he, useEffect: Le, useRef: dt, useLayoutEffect: Il, useMemo: jl } = e, Pl = {
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
  const u = oe(Pl), [s, r] = ke("simple"), [m, p] = ke(""), [_, N] = ke(t ?? ""), [E, b] = ke(!1), [h, T] = ke(null), k = dt(null), O = dt(null);
  Il(() => {
    if (!n.current || !k.current) return;
    const y = n.current.getBoundingClientRect(), A = k.current.getBoundingClientRect();
    let z = y.bottom + 4, te = y.left;
    z + A.height > window.innerHeight && (z = y.top - A.height - 4), te + A.width > window.innerWidth && (te = Math.max(0, y.right - A.width)), T({ top: z, left: te });
  }, [n]), Le(() => {
    !l && !E && o().catch(() => b(!0));
  }, [l, E, o]), Le(() => {
    l && O.current && O.current.focus();
  }, [l]), Le(() => {
    const y = (A) => {
      A.key === "Escape" && i();
    };
    return document.addEventListener("keydown", y), () => document.removeEventListener("keydown", y);
  }, [i]), Le(() => {
    const y = (z) => {
      k.current && !k.current.contains(z.target) && i();
    }, A = setTimeout(() => document.addEventListener("mousedown", y), 0);
    return () => {
      clearTimeout(A), document.removeEventListener("mousedown", y);
    };
  }, [i]);
  const P = jl(() => {
    if (!m) return a;
    const y = m.toLowerCase();
    return a.filter(
      (A) => A.prefix.toLowerCase().includes(y) || A.label.toLowerCase().includes(y) || A.terms != null && A.terms.some((z) => z.includes(y))
    );
  }, [a, m]), f = he((y) => {
    p(y.target.value);
  }, []), g = he(
    (y) => {
      c(y);
    },
    [c]
  ), C = he((y) => {
    N(y);
  }, []), q = he((y) => {
    N(y.target.value);
  }, []), R = he(() => {
    c(_ || null);
  }, [_, c]), L = he(() => {
    c(null);
  }, [c]), D = he(async (y) => {
    y.preventDefault(), b(!1);
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
        ref: O,
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
      !l && !E && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      E && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: D }, u["js.iconSelect.loadError"])),
      l && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      l && P.map(
        (y) => y.variants.map((A) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: A.encoded,
            className: "tlIconSelect__iconCell" + (A.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": A.encoded === t,
            tabIndex: 0,
            title: y.label,
            onClick: () => s === "simple" ? g(A.encoded) : C(A.encoded),
            onKeyDown: (z) => {
              (z.key === "Enter" || z.key === " ") && (z.preventDefault(), s === "simple" ? g(A.encoded) : C(A.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Ee, { encoded: A.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: q
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(Ee, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: L }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, Al = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Bl, useCallback: xe, useRef: Ol } = e, $l = ({ controlId: n, state: t }) => {
  const a = le(), l = oe(Al), [c, i] = Bl(!1), o = Ol(null), u = t.value, s = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, _ = xe(() => {
    s && !r && i(!0);
  }, [s, r]), N = xe(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), E = xe(() => {
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
    u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ml,
    {
      anchorRef: o,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: N,
      onCancel: E,
      onLoadIcons: b
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Ee, { encoded: u }) : null));
};
F("TLButton", It);
F("TLToggleButton", Pt);
F("TLTextInput", Ct);
F("TLNumberInput", wt);
F("TLDatePicker", St);
F("TLSelect", Tt);
F("TLCheckbox", Lt);
F("TLTable", xt);
F("TLCounter", Mt);
F("TLTabBar", Bt);
F("TLFieldList", Ot);
F("TLAudioRecorder", Ft);
F("TLAudioPlayer", Wt);
F("TLFileUpload", Ut);
F("TLDownload", Kt);
F("TLPhotoCapture", Gt);
F("TLPhotoViewer", qt);
F("TLSplitPanel", Zt);
F("TLPanel", rn);
F("TLMaximizeRoot", an);
F("TLDeckPane", on);
F("TLSidebar", hn);
F("TLStack", _n);
F("TLGrid", bn);
F("TLCard", vn);
F("TLAppBar", En);
F("TLBreadcrumb", Cn);
F("TLBottomBar", wn);
F("TLDialog", Sn);
F("TLDialogManager", Rn);
F("TLWindow", Dn);
F("TLDrawer", Mn);
F("TLSnackbar", $n);
F("TLMenu", Hn);
F("TLAppShell", Wn);
F("TLTextCell", zn);
F("TLTableView", Vn);
F("TLFormLayout", Qn);
F("TLFormGroup", tl);
F("TLFormField", al);
F("TLResourceCell", ol);
F("TLTreeView", cl);
F("TLDropdownSelect", _l);
F("TLColorInput", Dl);
F("TLIconSelect", $l);
