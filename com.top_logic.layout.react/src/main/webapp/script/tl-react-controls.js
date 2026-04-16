import { React as e, useTLFieldValue as xe, getComponent as kt, useTLState as G, useTLCommand as le, TLChild as X, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as U } from "tl-react-bridge";
const { useCallback: St } = e, Nt = ({ controlId: r, state: t }) => {
  const [n, l] = xe(), i = St(
    (a) => {
      l(a.target.value);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    c ? "tlReactTextInput--error" : "",
    !c && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: r }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": c || void 0,
      title: c && u ? u : void 0
    }
  ));
}, { useCallback: Tt } = e, Rt = ({ controlId: r, state: t, config: n }) => {
  const [l, i] = xe(), c = Tt(
    (p) => {
      const m = p.target.value;
      i(m === "" ? null : m);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactNumberInput tlReactNumberInput--immutable" }, l != null ? String(l) : "");
  const s = t.hasError === !0, u = t.hasWarnings === !0, o = t.errorMessage, a = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: r }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: l != null ? String(l) : "",
      onChange: c,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": s || void 0,
      title: s && o ? o : void 0
    }
  ));
}, { useCallback: xt } = e, Lt = ({ controlId: r, state: t }) => {
  const [n, l] = xe(), i = xt(
    (o) => {
      l(o.target.value || null);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: r }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  ));
}, { useCallback: Dt } = e, It = ({ controlId: r, state: t, config: n }) => {
  var p;
  const [l, i] = xe(), c = Dt(
    (m) => {
      i(m.target.value || null);
    },
    [i]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((p = s.find((h) => h.value === l)) == null ? void 0 : p.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: r, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const u = t.hasError === !0, o = t.hasWarnings === !0, a = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && o ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: r }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: l ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Mt } = e, Pt = ({ controlId: r, state: t }) => {
  const [n, l] = xe(), i = Mt(
    (o) => {
      l(o.target.checked);
    },
    [l]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: r,
        checked: n === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    c ? "tlReactCheckbox--error" : "",
    !c && s ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: r,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  );
}, jt = ({ controlId: r, state: t }) => {
  const n = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: r, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((i, c) => /* @__PURE__ */ e.createElement("tr", { key: c }, n.map((s) => {
    const u = s.cellModule ? kt(s.cellModule) : void 0, o = i[s.name];
    if (u) {
      const a = { value: o, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: r + "-" + c + "-" + s.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, o != null ? String(o) : "");
  })))));
};
function Se({ encoded: r, className: t }) {
  if (r.startsWith("css:")) {
    const n = r.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  if (r.startsWith("colored:")) {
    const n = r.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  return r.startsWith("/") || r.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: r, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
}
const { useCallback: At } = e, Bt = ({ controlId: r, command: t, label: n, disabled: l }) => {
  const i = G(), c = le(), s = t ?? "click", u = n ?? i.label, o = l ?? i.disabled === !0, a = i.hidden === !0, p = i.image, m = i.iconOnly === !0, h = i.tooltip, N = a ? { display: "none" } : void 0, f = h ?? u, T = h ? `text:${h}` : void 0, y = At(() => {
    c(s);
  }, [c, s]);
  return p && m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: y,
      disabled: o,
      style: N,
      className: "tlReactButton tlReactButton--icon",
      "data-tooltip": f ? `text:${f}` : void 0,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(Se, { encoded: p })
  ) : p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: y,
      disabled: o,
      style: N,
      className: "tlReactButton",
      "data-tooltip": T
    },
    /* @__PURE__ */ e.createElement(Se, { encoded: p, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: y,
      disabled: o,
      style: N,
      className: "tlReactButton",
      "data-tooltip": T
    },
    u
  );
}, { useCallback: Ot } = e, $t = ({ controlId: r, command: t, label: n, active: l, disabled: i }) => {
  const c = G(), s = le(), u = t ?? "click", o = n ?? c.label, a = l ?? c.active === !0, p = i ?? c.disabled === !0, m = Ot(() => {
    s(u);
  }, [s, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: r,
      onClick: m,
      disabled: p,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    o
  );
}, Ft = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ht } = e, Wt = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.tabs ?? [], i = t.activeTabId, c = Ht((s) => {
    s !== i && n("selectTab", { tabId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, l.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === i,
      className: "tlReactTabBar__tab" + (s.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, zt = ({ controlId: r }) => {
  const t = G(), n = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((i, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(X, { control: i })))));
}, Ut = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Vt = ({ controlId: r }) => {
  const t = G(), n = Je(), [l, i] = e.useState("idle"), [c, s] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), p = t.status ?? "idle", m = t.error, h = p === "received" ? "idle" : l !== "idle" ? l : p, N = e.useCallback(async () => {
    if (l === "recording") {
      const g = u.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (l !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = g, o.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", j = new MediaRecorder(g, D ? { mimeType: D } : void 0);
        u.current = j, j.ondataavailable = (b) => {
          b.data.size > 0 && o.current.push(b.data);
        }, j.onstop = async () => {
          g.getTracks().forEach((E) => E.stop()), a.current = null;
          const b = new Blob(o.current, { type: j.mimeType || "audio/webm" });
          if (o.current = [], b.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", b, "recording.webm"), await n(_), i("idle");
        }, j.start(), i("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), s("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [l, n]), f = ae(Ut), T = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], y = h === "uploading", v = ["tlAudioRecorder__button"];
  return h === "recording" && v.push("tlAudioRecorder__button--recording"), h === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: N,
      disabled: y,
      title: T,
      "aria-label": T
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[c]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Kt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Yt = ({ controlId: r }) => {
  const t = G(), n = et(), l = !!t.hasAudio, i = t.dataRevision ?? 0, [c, s] = e.useState(l ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
  e.useEffect(() => {
    l ? c === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), s("disabled"));
  }, [l]), e.useEffect(() => {
    i !== a.current && (a.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (c === "playing" || c === "paused" || c === "loading") && s("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (c === "disabled" || c === "loading")
      return;
    if (c === "playing") {
      u.current && u.current.pause(), s("paused");
      return;
    }
    if (c === "paused" && u.current) {
      u.current.play(), s("playing");
      return;
    }
    if (!o.current) {
      s("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), s("idle");
          return;
        }
        const v = await y.blob();
        o.current = URL.createObjectURL(v);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), s("idle");
        return;
      }
    }
    const T = new Audio(o.current);
    u.current = T, T.onended = () => {
      s("idle");
    }, T.play(), s("playing");
  }, [c, n]), m = ae(Kt), h = c === "loading" ? m["js.loading"] : c === "playing" ? m["js.audioPlayer.pause"] : c === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], N = c === "disabled" || c === "loading", f = ["tlAudioPlayer__button"];
  return c === "playing" && f.push("tlAudioPlayer__button--playing"), c === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: p,
      disabled: N,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Xt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Gt = ({ controlId: r }) => {
  const t = G(), n = Je(), [l, i] = e.useState("idle"), [c, s] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, p = t.accept ?? "", m = o === "received" ? "idle" : l !== "idle" ? l : o, h = e.useCallback(async (b) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", b, b.name), await n(_), i("idle");
  }, [n]), N = e.useCallback((b) => {
    var E;
    const _ = (E = b.target.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [h]), f = e.useCallback(() => {
    var b;
    l !== "uploading" && ((b = u.current) == null || b.click());
  }, [l]), T = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), s(!0);
  }, []), y = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), s(!1);
  }, []), v = e.useCallback((b) => {
    var E;
    if (b.preventDefault(), b.stopPropagation(), s(!1), l === "uploading") return;
    const _ = (E = b.dataTransfer.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [l, h]), g = m === "uploading", D = ae(Xt), j = m === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: T,
      onDragLeave: y,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
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
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: g,
        title: j,
        "aria-label": j
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, qt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Zt = ({ controlId: r }) => {
  const t = G(), n = et(), l = le(), i = !!t.hasData, c = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + c, T = await fetch(f);
        if (!T.ok) {
          console.error("[TLDownload] Failed to fetch data:", T.status);
          return;
        }
        const y = await T.blob(), v = URL.createObjectURL(y), g = document.createElement("a");
        g.href = v, g.download = s, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(v);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, c, s]), m = e.useCallback(async () => {
    i && await l("clear");
  }, [i, l]), h = ae(qt);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const N = o ? h["js.downloading"] : h["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: o,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: m,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Qt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Jt = ({ controlId: r }) => {
  const t = G(), n = Je(), [l, i] = e.useState("idle"), [c, s] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), p = e.useRef(null), m = e.useRef(null), h = e.useRef(null), N = e.useRef(null), f = t.error, T = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((S) => S.stop()), p.current = null), a.current && (a.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    y(), i("idle");
  }, [y]), g = e.useCallback(async () => {
    var S;
    if (l !== "uploading") {
      if (s(null), !T) {
        (S = h.current) == null || S.click();
        return;
      }
      try {
        const $ = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = $, i("overlayOpen");
      } catch ($) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", $), s("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [l, T]), D = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const S = a.current, $ = m.current;
    if (!S || !$)
      return;
    $.width = S.videoWidth, $.height = S.videoHeight;
    const x = $.getContext("2d");
    x && (x.drawImage(S, 0, 0), y(), i("uploading"), $.toBlob(async (O) => {
      if (!O) {
        i("idle");
        return;
      }
      const Z = new FormData();
      Z.append("photo", O, "capture.jpg"), await n(Z), i("idle");
    }, "image/jpeg", 0.85));
  }, [l, n, y]), j = e.useCallback(async (S) => {
    var O;
    const $ = (O = S.target.files) == null ? void 0 : O[0];
    if (!$) return;
    i("uploading");
    const x = new FormData();
    x.append("photo", $, $.name), await n(x), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    l === "overlayOpen" && a.current && p.current && (a.current.srcObject = p.current);
  }, [l]), e.useEffect(() => {
    var $;
    if (l !== "overlayOpen") return;
    ($ = N.current) == null || $.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const S = ($) => {
      $.key === "Escape" && v();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [l, v]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((S) => S.stop()), p.current = null);
  }, []);
  const b = ae(Qt), _ = l === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const F = ["tlPhotoCapture__overlayVideo"];
  u && F.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return u && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: g,
      disabled: l === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !T && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: j
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: N,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: v }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: F.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: k.join(" "),
        onClick: () => o((S) => !S),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[c]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, en = {
  "js.photoViewer.alt": "Captured photo"
}, tn = ({ controlId: r }) => {
  const t = G(), n = et(), l = !!t.hasPhoto, i = t.dataRevision ?? 0, [c, s] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!l) {
      c && (URL.revokeObjectURL(c), s(null));
      return;
    }
    if (i === u.current && c)
      return;
    u.current = i, c && (URL.revokeObjectURL(c), s(null));
    let a = !1;
    return (async () => {
      try {
        const p = await fetch(n);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const m = await p.blob();
        a || s(URL.createObjectURL(m));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      a = !0;
    };
  }, [l, i, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const o = ae(en);
  return !l || !c ? /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: r, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, nn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.orientation, i = t.resizable === !0, c = t.children ?? [], s = l === "horizontal", u = c.length > 0 && c.every((y) => y.collapsed), o = !u && c.some((y) => y.collapsed), a = u ? !s : s, p = Fe(null), m = Fe(null), h = Fe(null), N = ot((y, v) => {
    const g = {
      overflow: y.scrolling || "auto"
    };
    return y.collapsed ? u && !a ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : v !== void 0 ? g.flex = `0 0 ${v}px` : g.flex = `${y.size} 1 0%`, y.minSize > 0 && !y.collapsed && (g.minWidth = s ? y.minSize : void 0, g.minHeight = s ? void 0 : y.minSize), g;
  }, [s, u, o, a]), f = ot((y, v) => {
    y.preventDefault();
    const g = p.current;
    if (!g) return;
    const D = c[v], j = c[v + 1], b = g.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    b.forEach((k) => {
      _.push(s ? k.offsetWidth : k.offsetHeight);
    }), h.current = _, m.current = {
      splitterIndex: v,
      startPos: s ? y.clientX : y.clientY,
      startSizeBefore: _[v],
      startSizeAfter: _[v + 1],
      childBefore: D,
      childAfter: j
    };
    const E = (k) => {
      const S = m.current;
      if (!S || !h.current) return;
      const x = (s ? k.clientX : k.clientY) - S.startPos, O = S.childBefore.minSize || 0, Z = S.childAfter.minSize || 0;
      let ne = S.startSizeBefore + x, z = S.startSizeAfter - x;
      ne < O && (z += ne - O, ne = O), z < Z && (ne += z - Z, z = Z), h.current[S.splitterIndex] = ne, h.current[S.splitterIndex + 1] = z;
      const J = g.querySelectorAll(":scope > .tlSplitPanel__child"), B = J[S.splitterIndex], P = J[S.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ne}px`), P && (P.style.flex = `0 0 ${z}px`);
    }, F = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", F), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const k = {};
        c.forEach((S, $) => {
          const x = S.control;
          x != null && x.controlId && h.current && (k[x.controlId] = h.current[$]);
        }), n("updateSizes", { sizes: k });
      }
      h.current = null, m.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", F), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, s, n]), T = [];
  return c.forEach((y, v) => {
    if (T.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${y.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: N(y)
        },
        /* @__PURE__ */ e.createElement(X, { control: y.control })
      )
    ), i && v < c.length - 1) {
      const g = c[v + 1];
      !y.collapsed && !g.collapsed && T.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (j) => f(j, v)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: r,
      className: `tlSplitPanel tlSplitPanel--${l}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    T
  );
}, { useCallback: He } = e, ln = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), on = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), sn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), un = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae(ln), i = t.title, c = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, p = t.toolbarButtons ?? [], m = c === "MINIMIZED", h = c === "MAXIMIZED", N = c === "HIDDEN", f = He(() => {
    n("toggleMinimize");
  }, [n]), T = He(() => {
    n("toggleMaximize");
  }, [n]), y = He(() => {
    n("popOut");
  }, [n]);
  if (N)
    return null;
  const v = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlPanel tlPanel--${c.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: v
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, p.map((g, D) => /* @__PURE__ */ e.createElement("span", { key: D, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: g }))), s && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: m ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(on, null) : /* @__PURE__ */ e.createElement(rn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: T,
        title: h ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(sn, null) : /* @__PURE__ */ e.createElement(an, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: y,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(cn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(X, { control: t.child }))
  );
}, dn = ({ controlId: r }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(X, { control: t.child })
  );
}, mn = ({ controlId: r }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(X, { control: t.activeChild }));
}, { useCallback: de, useState: Be, useEffect: Oe, useRef: $e } = e, pn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function qe(r, t, n, l) {
  const i = [];
  for (const c of r)
    c.type === "nav" ? i.push({ id: c.id, type: "nav", groupId: l }) : c.type === "command" ? i.push({ id: c.id, type: "command", groupId: l }) : c.type === "group" && (i.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && i.push(...qe(c.children, t, n, c.id)));
  return i;
}
const Ne = ({ icon: r }) => r ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + r, "aria-hidden": "true" }) : null, fn = ({ item: r, active: t, collapsed: n, onSelect: l, tabIndex: i, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(r.id),
    title: n ? r.label : void 0,
    tabIndex: i,
    ref: c,
    onFocus: () => s(r.id)
  },
  n && r.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: r.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, r.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: r.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
  !n && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
), hn = ({ item: r, collapsed: t, onExecute: n, tabIndex: l, itemRef: i, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(r.id),
    title: t ? r.label : void 0,
    tabIndex: l,
    ref: i,
    onFocus: () => c(r.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: r.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)
), _n = ({ item: r, collapsed: t }) => t && !r.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? r.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: r.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label)), bn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), vn = ({ item: r, activeItemId: t, anchorRect: n, onSelect: l, onExecute: i, onClose: c }) => {
  const s = $e(null);
  Oe(() => {
    const a = (p) => {
      s.current && !s.current.contains(p.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [c]), Oe(() => {
    const a = (p) => {
      p.key === "Escape" && c();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [c]);
  const u = de((a) => {
    a.type === "nav" ? (l(a.id), c()) : a.type === "command" && (i(a.id), c());
  }, [l, i, c]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, r.label), r.children.map((a) => {
    if (a.type === "nav" || a.type === "command") {
      const p = a.type === "nav" && a.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: a.id,
          className: "tlSidebar__flyoutItem" + (p ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(a)
        },
        /* @__PURE__ */ e.createElement(Ne, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, En = ({
  item: r,
  expanded: t,
  activeItemId: n,
  collapsed: l,
  onSelect: i,
  onExecute: c,
  onToggleGroup: s,
  tabIndex: u,
  itemRef: o,
  onFocus: a,
  focusedId: p,
  setItemRef: m,
  onItemFocus: h,
  flyoutGroupId: N,
  onOpenFlyout: f,
  onCloseFlyout: T
}) => {
  const y = $e(null), [v, g] = Be(null), D = de(() => {
    l ? N === r.id ? T() : (y.current && g(y.current.getBoundingClientRect()), f(r.id)) : s(r.id);
  }, [l, N, r.id, s, f, T]), j = de((_) => {
    y.current = _, o(_);
  }, [o]), b = l && N === r.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: l ? r.label : void 0,
      "aria-expanded": l ? b : t,
      tabIndex: u,
      ref: j,
      onFocus: () => a(r.id)
    },
    /* @__PURE__ */ e.createElement(Ne, { icon: r.icon }),
    !l && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
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
  ), b && /* @__PURE__ */ e.createElement(
    vn,
    {
      item: r,
      activeItemId: n,
      anchorRect: v,
      onSelect: i,
      onExecute: c,
      onClose: T
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, r.children.map((_) => /* @__PURE__ */ e.createElement(
    bt,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: l,
      onSelect: i,
      onExecute: c,
      onToggleGroup: s,
      focusedId: p,
      setItemRef: m,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: N,
      onOpenFlyout: f,
      onCloseFlyout: T
    }
  ))));
}, bt = ({
  item: r,
  activeItemId: t,
  collapsed: n,
  onSelect: l,
  onExecute: i,
  onToggleGroup: c,
  focusedId: s,
  setItemRef: u,
  onItemFocus: o,
  groupStates: a,
  flyoutGroupId: p,
  onOpenFlyout: m,
  onCloseFlyout: h
}) => {
  switch (r.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        fn,
        {
          item: r,
          active: r.id === t,
          collapsed: n,
          onSelect: l,
          tabIndex: s === r.id ? 0 : -1,
          itemRef: u(r.id),
          onFocus: o
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        hn,
        {
          item: r,
          collapsed: n,
          onExecute: i,
          tabIndex: s === r.id ? 0 : -1,
          itemRef: u(r.id),
          onFocus: o
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(_n, { item: r, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(bn, null);
    case "group": {
      const N = a ? a.get(r.id) ?? r.expanded : r.expanded;
      return /* @__PURE__ */ e.createElement(
        En,
        {
          item: r,
          expanded: N,
          activeItemId: t,
          collapsed: n,
          onSelect: l,
          onExecute: i,
          onToggleGroup: c,
          tabIndex: s === r.id ? 0 : -1,
          itemRef: u(r.id),
          onFocus: o,
          focusedId: s,
          setItemRef: u,
          onItemFocus: o,
          flyoutGroupId: p,
          onOpenFlyout: m,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, gn = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae(pn), i = t.items ?? [], c = t.activeItemId, s = t.collapsed, [u, o] = Be(() => {
    const k = /* @__PURE__ */ new Map(), S = ($) => {
      for (const x of $)
        x.type === "group" && (k.set(x.id, x.expanded), S(x.children));
    };
    return S(i), k;
  }), a = de((k) => {
    o((S) => {
      const $ = new Map(S), x = $.get(k) ?? !1;
      return $.set(k, !x), n("toggleGroup", { itemId: k, expanded: !x }), $;
    });
  }, [n]), p = de((k) => {
    k !== c && n("selectItem", { itemId: k });
  }, [n, c]), m = de((k) => {
    n("executeCommand", { itemId: k });
  }, [n]), h = de(() => {
    n("toggleCollapse", {});
  }, [n]), [N, f] = Be(null), T = de((k) => {
    f(k);
  }, []), y = de(() => {
    f(null);
  }, []);
  Oe(() => {
    s || f(null);
  }, [s]);
  const [v, g] = Be(() => {
    const k = qe(i, s, u);
    return k.length > 0 ? k[0].id : "";
  }), D = $e(/* @__PURE__ */ new Map()), j = de((k) => (S) => {
    S ? D.current.set(k, S) : D.current.delete(k);
  }, []), b = de((k) => {
    g(k);
  }, []), _ = $e(0), E = de((k) => {
    g(k), _.current++;
  }, []);
  Oe(() => {
    const k = D.current.get(v);
    k && document.activeElement !== k && k.focus();
  }, [v, _.current]);
  const F = de((k) => {
    if (k.key === "Escape" && N !== null) {
      k.preventDefault(), y();
      return;
    }
    const S = qe(i, s, u);
    if (S.length === 0) return;
    const $ = S.findIndex((O) => O.id === v);
    if ($ < 0) return;
    const x = S[$];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const O = ($ + 1) % S.length;
        E(S[O].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const O = ($ - 1 + S.length) % S.length;
        E(S[O].id);
        break;
      }
      case "Home": {
        k.preventDefault(), E(S[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), E(S[S.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), x.type === "nav" ? p(x.id) : x.type === "command" ? m(x.id) : x.type === "group" && (s ? N === x.id ? y() : T(x.id) : a(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !s && ((u.get(x.id) ?? !1) || (k.preventDefault(), a(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !s && (u.get(x.id) ?? !1) && (k.preventDefault(), a(x.id));
        break;
      }
    }
  }, [
    i,
    s,
    u,
    v,
    N,
    E,
    p,
    m,
    a,
    T,
    y
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: F }, i.map((k) => /* @__PURE__ */ e.createElement(
    bt,
    {
      key: k.id,
      item: k,
      activeItemId: c,
      collapsed: s,
      onSelect: p,
      onExecute: m,
      onToggleGroup: a,
      focusedId: v,
      setItemRef: j,
      onItemFocus: b,
      groupStates: u,
      flyoutGroupId: N,
      onOpenFlyout: T,
      onCloseFlyout: y
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: s ? l["js.sidebar.expand"] : l["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: s ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, Cn = ({ controlId: r }) => {
  const t = G(), n = t.direction ?? "column", l = t.gap ?? "default", i = t.align ?? "stretch", c = t.wrap === !0, s = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${i}`,
    c ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: u }, s.map((o, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: o })));
}, yn = ({ controlId: r }) => {
  const t = G(), n = t.columns, l = t.minColumnWidth, i = t.gap ?? "default", c = t.children ?? [], s = {};
  return l ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: r, className: `tlGrid tlGrid--gap-${i}`, style: s }, c.map((u, o) => /* @__PURE__ */ e.createElement(X, { key: o, control: u })));
}, wn = ({ controlId: r }) => {
  const t = G(), n = t.title, l = t.variant ?? "outlined", i = t.padding ?? "default", c = t.headerActions ?? [], s = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: `tlCard tlCard--${l}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((o, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(X, { control: s })));
}, kn = ({ controlId: r }) => {
  const t = G(), n = t.title ?? "", l = t.leading, i = t.actions ?? [], c = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: r, className: u }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(X, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((o, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: o }))));
}, { useCallback: Sn } = e, Nn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.items ?? [], i = Sn((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, l.map((c, s) => {
    const u = s === l.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: c.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, c.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => i(c.id)
      },
      c.label
    ));
  })));
}, { useCallback: Tn } = e, Rn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.items ?? [], i = t.activeItemId, c = Tn((s) => {
    s !== i && n("selectItem", { itemId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: r, className: "tlBottomBar", "aria-label": "Bottom navigation" }, l.map((s) => {
    const u = s.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => c(s.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: at, useEffect: st, useRef: xn } = e, Ln = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.open === !0, i = t.closeOnBackdrop !== !1, c = t.child, s = xn(null), u = at(() => {
    n("close");
  }, [n]), o = at((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!l) return;
    const a = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [l, u]), st(() => {
    l && s.current && s.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlDialog__backdrop",
      onClick: o,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(X, { control: c })
  ) : null;
}, { useEffect: Dn, useRef: In } = e, Mn = ({ controlId: r }) => {
  const n = G().dialogs ?? [], l = In(n.length);
  return Dn(() => {
    n.length < l.current && n.length > 0, l.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(X, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, Pn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, jn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], An = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae(Pn), i = t.title ?? "", c = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, p = t.actions ?? [], m = t.toolbarButtons ?? [], [h, N] = Ie(null), [f, T] = Ie(null), [y, v] = Ie(null), g = Ce(null), [D, j] = Ie(!1), b = Ce(null), _ = Ce(null), E = Ce(null), F = Ce(null), k = Ce(null), S = De(() => {
    n("close");
  }, [n]), $ = De((z, J) => {
    J.preventDefault();
    const B = F.current;
    if (!B) return;
    const P = B.getBoundingClientRect(), Q = !g.current, d = g.current ?? { x: P.left, y: P.top };
    Q && (g.current = d, v(d)), k.current = {
      dir: z,
      startX: J.clientX,
      startY: J.clientY,
      startW: P.width,
      startH: P.height,
      startPos: { ...d },
      symmetric: Q
    };
    const w = (A) => {
      const I = k.current;
      if (!I) return;
      const K = A.clientX - I.startX, Y = A.clientY - I.startY;
      let te = I.startW, ee = I.startH, ie = 0, ue = 0;
      I.symmetric ? (I.dir.includes("e") && (te = I.startW + 2 * K), I.dir.includes("w") && (te = I.startW - 2 * K), I.dir.includes("s") && (ee = I.startH + 2 * Y), I.dir.includes("n") && (ee = I.startH - 2 * Y)) : (I.dir.includes("e") && (te = I.startW + K), I.dir.includes("w") && (te = I.startW - K, ie = K), I.dir.includes("s") && (ee = I.startH + Y), I.dir.includes("n") && (ee = I.startH - Y, ue = Y));
      const he = Math.max(200, te), fe = Math.max(100, ee);
      I.symmetric ? (ie = (I.startW - he) / 2, ue = (I.startH - fe) / 2) : (I.dir.includes("w") && he === 200 && (ie = I.startW - 200), I.dir.includes("n") && fe === 100 && (ue = I.startH - 100)), _.current = he, E.current = fe, N(he), T(fe);
      const ve = {
        x: I.startPos.x + ie,
        y: I.startPos.y + ue
      };
      g.current = ve, v(ve);
    }, H = () => {
      document.removeEventListener("mousemove", w), document.removeEventListener("mouseup", H);
      const A = _.current, I = E.current;
      (A != null || I != null) && n("resize", {
        ...A != null ? { width: Math.round(A) + "px" } : {},
        ...I != null ? { height: Math.round(I) + "px" } : {}
      }), k.current = null;
    };
    document.addEventListener("mousemove", w), document.addEventListener("mouseup", H);
  }, [n]), x = De((z) => {
    if (z.button !== 0 || z.target.closest("button")) return;
    z.preventDefault();
    const J = F.current;
    if (!J) return;
    const B = J.getBoundingClientRect(), P = g.current ?? { x: B.left, y: B.top }, Q = z.clientX - P.x, d = z.clientY - P.y, w = (A) => {
      const I = window.innerWidth, K = window.innerHeight;
      let Y = A.clientX - Q, te = A.clientY - d;
      const ee = J.offsetWidth, ie = J.offsetHeight;
      Y + ee > I && (Y = I - ee), te + ie > K && (te = K - ie), Y < 0 && (Y = 0), te < 0 && (te = 0);
      const ue = { x: Y, y: te };
      g.current = ue, v(ue);
    }, H = () => {
      document.removeEventListener("mousemove", w), document.removeEventListener("mouseup", H);
    };
    document.addEventListener("mousemove", w), document.addEventListener("mouseup", H);
  }, []), O = De(() => {
    var z, J;
    if (D) {
      const B = b.current;
      B && (v(B.x !== -1 ? { x: B.x, y: B.y } : null), N(B.w), T(B.h)), j(!1);
    } else {
      const B = F.current, P = B == null ? void 0 : B.getBoundingClientRect();
      b.current = {
        x: ((z = g.current) == null ? void 0 : z.x) ?? (P == null ? void 0 : P.left) ?? -1,
        y: ((J = g.current) == null ? void 0 : J.y) ?? (P == null ? void 0 : P.top) ?? -1,
        w: h ?? (P == null ? void 0 : P.width) ?? null,
        h: f ?? null
      }, j(!0), v({ x: 0, y: 0 }), N(null), T(null);
    }
  }, [D, h, f]), Z = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : c,
    ...f != null ? { height: f + "px" } : s != null ? { height: s } : {},
    ...u != null && f == null ? { minHeight: u } : {},
    maxHeight: y ? "100vh" : "80vh",
    ...y ? { position: "absolute", left: y.x + "px", top: y.y + "px" } : {}
  }, ne = r + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlWindow",
      style: Z,
      ref: F,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": ne
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : x,
        onDoubleClick: O
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: ne }, i),
      m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((z, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: z })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: D ? l["js.window.restore"] : l["js.window.maximize"]
        },
        D ? (
          // Restore icon: two overlapping squares.
          /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "18", height: "18", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1.5", fill: "none", stroke: "currentColor", strokeWidth: "2" }), /* @__PURE__ */ e.createElement("path", { d: "M8 8V5.5A1.5 1.5 0 0 1 9.5 4H18.5A1.5 1.5 0 0 1 20 5.5V14.5A1.5 1.5 0 0 1 18.5 16H16", fill: "none", stroke: "currentColor", strokeWidth: "2" }))
        ) : (
          // Maximize icon: single square.
          /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24", width: "18", height: "18", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1.5", fill: "none", stroke: "currentColor", strokeWidth: "2" }))
        )
      ),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__closeBtn",
          onClick: S,
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
      )
    ),
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(X, { control: a })),
    p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p.map((z, J) => /* @__PURE__ */ e.createElement(X, { key: J, control: z }))),
    o && !D && jn.map((z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${z}`,
        onMouseDown: (J) => $(z, J)
      }
    ))
  );
}, { useCallback: Bn, useEffect: On } = e, $n = {
  "js.drawer.close": "Close"
}, Fn = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae($n), i = t.open === !0, c = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Bn(() => {
    n("close");
  }, [n]);
  On(() => {
    if (!i) return;
    const m = (h) => {
      h.key === "Escape" && a();
    };
    return document.addEventListener("keydown", m), () => document.removeEventListener("keydown", m);
  }, [i, a]);
  const p = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${s}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: r, className: p, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(X, { control: o })));
}, { useCallback: Hn } = e, Wn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.child, i = Hn((c) => {
    c.preventDefault(), c.stopPropagation(), n("openContextMenu", { x: c.clientX, y: c.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tl-context-menu-region", onContextMenu: i }, l && /* @__PURE__ */ e.createElement(X, { control: l }));
}, { useCallback: zn, useEffect: Un, useState: Vn } = e, Kn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.message ?? "", i = t.content ?? "", c = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, p] = Vn(!1), m = zn(() => {
    p(!0), setTimeout(() => {
      n("dismiss", { generation: o }), p(!1);
    }, 200);
  }, [n, o]);
  return Un(() => {
    if (!u || s === 0) return;
    const h = setTimeout(m, s);
    return () => clearTimeout(h);
  }, [u, s, m]), !u && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: `tlSnackbar tlSnackbar--${c}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l)
  );
}, { useCallback: We, useEffect: ze, useRef: Yn, useState: ct } = e, Xn = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.open === !0, i = t.anchorId, c = t.anchorX, s = t.anchorY, u = t.items ?? [], o = Yn(null), [a, p] = ct({ top: 0, left: 0 }), [m, h] = ct(0), N = u.filter((v) => v.type === "item" && !v.disabled);
  ze(() => {
    var E, F;
    if (!l) return;
    const v = ((E = o.current) == null ? void 0 : E.offsetHeight) ?? 200, g = ((F = o.current) == null ? void 0 : F.offsetWidth) ?? 200;
    if (c != null && s != null) {
      let k = s, S = c;
      k + v > window.innerHeight && (k = Math.max(0, window.innerHeight - v)), S + g > window.innerWidth && (S = Math.max(0, window.innerWidth - g)), p({ top: k, left: S }), h(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const j = D.getBoundingClientRect();
    let b = j.bottom + 4, _ = j.left;
    b + v > window.innerHeight && (b = j.top - v - 4), _ + g > window.innerWidth && (_ = j.right - g), p({ top: b, left: _ }), h(0);
  }, [l, i, c, s]);
  const f = We(() => {
    n("close");
  }, [n]), T = We((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  ze(() => {
    if (!l) return;
    const v = (g) => {
      o.current && !o.current.contains(g.target) && f();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [l, f]);
  const y = We((v) => {
    if (v.key === "Escape") {
      f();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), h((g) => (g + 1) % N.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), h((g) => (g - 1 + N.length) % N.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const g = N[m];
      g && T(g.id);
    }
  }, [f, T, N, m]);
  return ze(() => {
    l && o.current && o.current.focus();
  }, [l]), l ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: a.top, left: a.left },
      onKeyDown: y
    },
    u.map((v, g) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: g, className: "tlMenu__separator" });
      const j = N.indexOf(v) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (j ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: j ? 0 : -1,
          onClick: () => T(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Gn = ({ controlId: r }) => {
  const t = G(), n = t.header, l = t.content, i = t.footer, c = t.snackbar, s = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(X, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(X, { control: l })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(X, { control: i })), /* @__PURE__ */ e.createElement(X, { control: c }), s && /* @__PURE__ */ e.createElement(X, { control: s }), u && /* @__PURE__ */ e.createElement(X, { control: u }));
}, qn = ({ controlId: r }) => {
  const t = G(), n = t.text ?? "", l = t.cssClass ?? "", i = t.hasTooltip === !0, c = l ? `tlText ${l}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: r,
      className: c,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, Zn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, Qn = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae(Zn), i = e.useRef(null);
  e.useEffect(() => {
    const R = i.current;
    if (!R) return;
    const M = (C) => {
      const L = C.detail;
      let W = L.target;
      for (; W && W !== R; ) {
        const q = W.dataset.row, re = W.dataset.col;
        if (q != null && re != null) {
          L.resolved = { key: q + "|" + re };
          return;
        }
        W = W.parentElement;
      }
    };
    return R.addEventListener("tl-tooltip-resolve", M), () => R.removeEventListener("tl-tooltip-resolve", M);
  }, []);
  const c = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", p = t.selectedCount ?? 0, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, N = e.useMemo(
    () => c.filter((R) => R.sortPriority && R.sortPriority > 0).length,
    [c]
  ), f = a === "multi", T = 40, y = 20, v = e.useRef(null), g = e.useRef(null), D = e.useRef(null), [j, b] = e.useState({}), _ = e.useRef(null), E = e.useRef(!1), F = e.useRef(null), [k, S] = e.useState(null), [$, x] = e.useState(null);
  e.useEffect(() => {
    _.current || b({});
  }, [c]);
  const O = e.useCallback((R) => j[R.name] ?? R.width, [j]), Z = e.useMemo(() => {
    const R = [];
    let M = f && m > 0 ? T : 0;
    for (let C = 0; C < m && C < c.length; C++)
      R.push(M), M += O(c[C]);
    return R;
  }, [c, m, f, T, O]), ne = s * o, z = e.useRef(null), J = e.useCallback((R, M, C) => {
    C.preventDefault(), C.stopPropagation(), _.current = { column: R, startX: C.clientX, startWidth: M };
    let L = C.clientX, W = 0;
    const q = () => {
      const oe = _.current;
      if (!oe) return;
      const me = Math.max(it, oe.startWidth + (L - oe.startX) + W);
      b((ge) => ({ ...ge, [oe.column]: me }));
    }, re = () => {
      const oe = g.current, me = v.current;
      if (!oe || !_.current) return;
      const ge = oe.getBoundingClientRect(), nt = 40, lt = 8, wt = oe.scrollLeft;
      L > ge.right - nt ? oe.scrollLeft += lt : L < ge.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - wt;
      rt !== 0 && (me && (me.scrollLeft = oe.scrollLeft), W += rt, q()), z.current = requestAnimationFrame(re);
    };
    z.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      L = oe.clientX, q();
    }, Le = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", Le), z.current !== null && (cancelAnimationFrame(z.current), z.current = null);
      const me = _.current;
      if (me) {
        const ge = Math.max(it, me.startWidth + (oe.clientX - me.startX) + W);
        n("columnResize", { column: me.column, width: ge }), _.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", Le);
  }, [n]), B = e.useCallback(() => {
    v.current && g.current && (v.current.scrollLeft = g.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const R = g.current;
      if (!R) return;
      const M = R.scrollTop, C = Math.ceil(R.clientHeight / o), L = Math.floor(M / o);
      n("scroll", { start: L, count: C });
    }, 80);
  }, [n, o]), P = e.useCallback((R, M, C) => {
    if (E.current) return;
    let L;
    !M || M === "desc" ? L = "asc" : L = "desc";
    const W = C.shiftKey ? "add" : "replace";
    n("sort", { column: R, direction: L, mode: W });
  }, [n]), Q = e.useCallback((R, M) => {
    F.current = R, M.dataTransfer.effectAllowed = "move", M.dataTransfer.setData("text/plain", R);
  }, []), d = e.useCallback((R, M) => {
    if (!F.current || F.current === R) {
      S(null);
      return;
    }
    M.preventDefault(), M.dataTransfer.dropEffect = "move";
    const C = M.currentTarget.getBoundingClientRect(), L = M.clientX < C.left + C.width / 2 ? "left" : "right";
    S({ column: R, side: L });
  }, []), w = e.useCallback((R) => {
    R.preventDefault(), R.stopPropagation();
    const M = F.current;
    if (!M || !k) {
      F.current = null, S(null);
      return;
    }
    let C = c.findIndex((W) => W.name === k.column);
    if (C < 0) {
      F.current = null, S(null);
      return;
    }
    const L = c.findIndex((W) => W.name === M);
    k.side === "right" && C++, L < C && C--, n("columnReorder", { column: M, targetIndex: C }), F.current = null, S(null);
  }, [c, k, n]), H = e.useCallback(() => {
    F.current = null, S(null);
  }, []), A = e.useCallback((R, M) => {
    M.shiftKey && M.preventDefault(), n("select", {
      rowIndex: R,
      ctrlKey: M.ctrlKey || M.metaKey,
      shiftKey: M.shiftKey
    });
  }, [n]), I = e.useCallback((R, M) => {
    M.stopPropagation(), n("select", { rowIndex: R, ctrlKey: !0, shiftKey: !1 });
  }, [n]), K = e.useCallback(() => {
    const R = p === s && s > 0;
    n("selectAll", { selected: !R });
  }, [n, p, s]), Y = e.useCallback((R, M, C) => {
    C.stopPropagation(), n("expand", { rowIndex: R, expanded: M });
  }, [n]), te = e.useCallback((R, M) => {
    M.preventDefault(), x({ x: M.clientX, y: M.clientY, colIdx: R });
  }, []), ee = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), x(null));
  }, [$, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), x(null);
  }, [n]);
  e.useEffect(() => {
    if (!$) return;
    const R = () => x(null), M = (C) => {
      C.key === "Escape" && x(null);
    };
    return document.addEventListener("mousedown", R), document.addEventListener("keydown", M), () => {
      document.removeEventListener("mousedown", R), document.removeEventListener("keydown", M);
    };
  }, [$]);
  const ue = c.reduce((R, M) => R + O(M), 0) + (f ? T : 0), he = p === s && s > 0, fe = p > 0 && p < s, ve = e.useCallback((R) => {
    R && (R.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: r,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (R) => {
        if (!F.current) return;
        R.preventDefault();
        const M = g.current, C = v.current;
        if (!M) return;
        const L = M.getBoundingClientRect(), W = 40, q = 8;
        R.clientX < L.left + W ? M.scrollLeft = Math.max(0, M.scrollLeft - q) : R.clientX > L.right - W && (M.scrollLeft += q), C && (C.scrollLeft = M.scrollLeft);
      },
      onDrop: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ue } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: T,
          minWidth: T,
          ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (R) => {
          F.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== F.current && S({ column: c[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ve,
          className: "tlTableView__checkbox",
          checked: he,
          onChange: K
        }
      )
    ), c.map((R, M) => {
      const C = O(R);
      c.length - 1;
      let L = "tlTableView__headerCell";
      R.sortable && (L += " tlTableView__headerCell--sortable"), k && k.column === R.name && (L += " tlTableView__headerCell--dragOver-" + k.side);
      const W = M < m, q = M === m - 1;
      return W && (L += " tlTableView__headerCell--frozen"), q && (L += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.name,
          className: L,
          style: {
            width: C,
            minWidth: C,
            position: W ? "sticky" : "relative",
            ...W ? { left: Z[M], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: R.sortable ? (re) => P(R.name, R.sortDirection, re) : void 0,
          onContextMenu: (re) => te(M, re),
          onDragStart: (re) => Q(R.name, re),
          onDragOver: (re) => d(R.name, re),
          onDrop: w,
          onDragEnd: H
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, R.label),
        R.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, R.sortDirection === "asc" ? "▲" : "▼", N > 1 && R.sortPriority != null && R.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, R.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => J(R.name, C, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (R) => {
          if (F.current && c.length > 0) {
            const M = c[c.length - 1];
            M.name !== F.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", S({ column: M.name, side: "right" }));
          }
        },
        onDrop: w
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: g,
        className: "tlTableView__body",
        onScroll: B
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ne, position: "relative", width: ue } }, u.map((R) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.id,
          className: "tlTableView__row" + (R.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: R.index * o,
            height: o,
            width: ue
          },
          onClick: (M) => A(R.index, M)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: T,
              minWidth: T,
              ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (M) => M.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: R.selected,
              onChange: () => {
              },
              onClick: (M) => I(R.index, M),
              tabIndex: -1
            }
          )
        ),
        c.map((M, C) => {
          const L = O(M), W = C === c.length - 1, q = C < m, re = C === m - 1;
          let Ee = "tlTableView__cell";
          q && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const Le = h && C === 0, oe = R.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: M.name,
              className: Ee,
              "data-row": R.id,
              "data-col": M.name,
              style: {
                ...W && !q ? { flex: "1 0 auto", minWidth: L } : { width: L, minWidth: L },
                ...q ? { position: "sticky", left: Z[C], zIndex: 2 } : {}
              }
            },
            Le ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * y } }, R.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => Y(R.index, !R.expanded, me)
              },
              R.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(X, { control: R.cells[M.name] })) : /* @__PURE__ */ e.createElement(X, { control: R.cells[M.name] })
          );
        })
      )))
    ),
    $ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: $.y, left: $.x, zIndex: 1e4 },
        onMouseDown: (R) => R.stopPropagation()
      },
      $.colIdx + 1 !== m && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.freezeUpTo"])),
      m > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, l["js.table.unfreezeAll"]))
    )
  );
}, Jn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, vt = e.createContext(Jn), { useMemo: el, useRef: tl, useState: nl, useEffect: ll } = e, rl = 320, ol = ({ controlId: r }) => {
  const t = G(), n = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", i = t.readOnly === !0, c = t.children ?? [], s = t.noModelMessage, u = tl(null), [o, a] = nl(
    l === "top" ? "top" : "side"
  );
  ll(() => {
    if (l !== "auto") {
      a(l);
      return;
    }
    const f = u.current;
    if (!f) return;
    const T = new ResizeObserver((y) => {
      for (const v of y) {
        const D = v.contentRect.width / n;
        a(D < rl ? "top" : "side");
      }
    });
    return T.observe(f), () => T.disconnect();
  }, [l, n]);
  const p = el(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, N = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: r, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(vt.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: r, className: N, style: h, ref: u }, c.map((f, T) => /* @__PURE__ */ e.createElement(X, { key: T, control: f }))));
}, { useCallback: al } = e, sl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, cl = ({ controlId: r }) => {
  const t = G(), n = le(), l = ae(sl), i = t.headerControl ?? null, c = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, p = t.children ?? [], m = i != null || c.length > 0 || s, h = al(() => {
    n("toggleCollapse");
  }, [n]), N = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: N }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(X, { control: i })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((f, T) => /* @__PURE__ */ e.createElement(X, { key: T, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((f, T) => /* @__PURE__ */ e.createElement(X, { key: T, control: f }))));
}, { useContext: il, useState: ul, useCallback: dl } = e, ml = ({ controlId: r }) => {
  const t = G(), n = il(vt), l = t.label ?? "", i = t.required === !0, c = t.error, s = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, m = t.visible !== !1, h = t.hasTooltip === !0, N = t.field, f = n.readOnly, [T, y] = ul(!1), v = dl(() => y((b) => !b), []);
  if (!m) return null;
  const g = c != null, D = s != null && s.length > 0, j = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    g ? "tlFormField--error" : "",
    !g && D ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: r, className: j }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    l
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: v,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(X, { control: N })), !f && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, c)), !f && !g && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, s.map((b, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, b)))), !f && u && T && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, pl = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.iconCss, i = t.iconSrc, c = t.label, s = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = l ? /* @__PURE__ */ e.createElement("i", { className: l }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, p = /* @__PURE__ */ e.createElement(e.Fragment, null, a, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", s].filter(Boolean).join(" "), N = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: r,
      className: h,
      href: "#",
      onClick: m,
      "data-tooltip": N
    },
    p
  ) : /* @__PURE__ */ e.createElement("span", { id: r, className: h, "data-tooltip": N }, p);
}, fl = 20, hl = () => {
  const r = G(), t = le(), n = r.nodes ?? [], l = r.selectionMode ?? "single", i = r.dragEnabled ?? !1, c = r.dropEnabled ?? !1, s = r.dropIndicatorNodeId ?? null, u = r.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), p = e.useRef(null), m = e.useCallback((b, _) => {
    t(_ ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, _) => {
    t("select", {
      nodeId: b,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), N = e.useCallback((b, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: b, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), T = e.useCallback((b, _) => {
    const E = _.getBoundingClientRect(), F = b.clientY - E.top, k = E.height / 3;
    return F < k ? "above" : F > k * 2 ? "below" : "within";
  }, []), y = e.useCallback((b, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", b);
  }, []), v = e.useCallback((b, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const E = T(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: E }), f.current = null;
    }, 50);
  }, [t, T]), g = e.useCallback((b, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = T(_, _.currentTarget);
    t("drop", { nodeId: b, position: E });
  }, [t, T]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), j = e.useCallback((b) => {
    if (n.length === 0) return;
    let _ = o;
    switch (b.key) {
      case "ArrowDown":
        b.preventDefault(), _ = Math.min(o + 1, n.length - 1);
        break;
      case "ArrowUp":
        b.preventDefault(), _ = Math.max(o - 1, 0);
        break;
      case "ArrowRight":
        if (b.preventDefault(), o >= 0 && o < n.length) {
          const E = n[o];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (_ = o + 1);
        }
        break;
      case "ArrowLeft":
        if (b.preventDefault(), o >= 0 && o < n.length) {
          const E = n[o];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const F = E.depth;
            for (let k = o - 1; k >= 0; k--)
              if (n[k].depth < F) {
                _ = k;
                break;
              }
          }
        }
        break;
      case "Enter":
        b.preventDefault(), o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: b.ctrlKey || b.metaKey,
          shiftKey: b.shiftKey
        });
        return;
      case " ":
        b.preventDefault(), l === "multi" && o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        b.preventDefault(), _ = 0;
        break;
      case "End":
        b.preventDefault(), _ = n.length - 1;
        break;
      default:
        return;
    }
    _ !== o && a(_);
  }, [o, n, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: j
    },
    n.map((b, _) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: b.id,
        role: "treeitem",
        "aria-expanded": b.expandable ? b.expanded : void 0,
        "aria-selected": b.selected,
        "aria-level": b.depth + 1,
        className: [
          "tlTreeView__node",
          b.selected ? "tlTreeView__node--selected" : "",
          _ === o ? "tlTreeView__node--focused" : "",
          s === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * fl },
        draggable: i,
        onClick: (E) => h(b.id, E),
        onContextMenu: (E) => N(b.id, E),
        onDragStart: (E) => y(b.id, E),
        onDragOver: c ? (E) => v(b.id, E) : void 0,
        onDrop: c ? (E) => g(b.id, E) : void 0,
        onDragEnd: D
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), m(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(X, { control: b.content }))
    ))
  );
};
var Ue = { exports: {} }, se = {}, Ve = { exports: {} }, V = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ut;
function _l() {
  if (ut) return V;
  ut = 1;
  var r = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), h = Symbol.iterator;
  function N(d) {
    return d === null || typeof d != "object" ? null : (d = h && d[h] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var f = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, T = Object.assign, y = {};
  function v(d, w, H) {
    this.props = d, this.context = w, this.refs = y, this.updater = H || f;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, w) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, w, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function g() {
  }
  g.prototype = v.prototype;
  function D(d, w, H) {
    this.props = d, this.context = w, this.refs = y, this.updater = H || f;
  }
  var j = D.prototype = new g();
  j.constructor = D, T(j, v.prototype), j.isPureReactComponent = !0;
  var b = Array.isArray;
  function _() {
  }
  var E = { H: null, A: null, T: null, S: null }, F = Object.prototype.hasOwnProperty;
  function k(d, w, H) {
    var A = H.ref;
    return {
      $$typeof: r,
      type: d,
      key: w,
      ref: A !== void 0 ? A : null,
      props: H
    };
  }
  function S(d, w) {
    return k(d.type, w, d.props);
  }
  function $(d) {
    return typeof d == "object" && d !== null && d.$$typeof === r;
  }
  function x(d) {
    var w = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(H) {
      return w[H];
    });
  }
  var O = /\/+/g;
  function Z(d, w) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : w.toString(36);
  }
  function ne(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
          function(w) {
            d.status === "pending" && (d.status = "fulfilled", d.value = w);
          },
          function(w) {
            d.status === "pending" && (d.status = "rejected", d.reason = w);
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
  function z(d, w, H, A, I) {
    var K = typeof d;
    (K === "undefined" || K === "boolean") && (d = null);
    var Y = !1;
    if (d === null) Y = !0;
    else
      switch (K) {
        case "bigint":
        case "string":
        case "number":
          Y = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case r:
            case t:
              Y = !0;
              break;
            case p:
              return Y = d._init, z(
                Y(d._payload),
                w,
                H,
                A,
                I
              );
          }
      }
    if (Y)
      return I = I(d), Y = A === "" ? "." + Z(d, 0) : A, b(I) ? (H = "", Y != null && (H = Y.replace(O, "$&/") + "/"), z(I, w, H, "", function(ie) {
        return ie;
      })) : I != null && ($(I) && (I = S(
        I,
        H + (I.key == null || d && d.key === I.key ? "" : ("" + I.key).replace(
          O,
          "$&/"
        ) + "/") + Y
      )), w.push(I)), 1;
    Y = 0;
    var te = A === "" ? "." : A + ":";
    if (b(d))
      for (var ee = 0; ee < d.length; ee++)
        A = d[ee], K = te + Z(A, ee), Y += z(
          A,
          w,
          H,
          K,
          I
        );
    else if (ee = N(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(A = d.next()).done; )
        A = A.value, K = te + Z(A, ee++), Y += z(
          A,
          w,
          H,
          K,
          I
        );
    else if (K === "object") {
      if (typeof d.then == "function")
        return z(
          ne(d),
          w,
          H,
          A,
          I
        );
      throw w = String(d), Error(
        "Objects are not valid as a React child (found: " + (w === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : w) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Y;
  }
  function J(d, w, H) {
    if (d == null) return d;
    var A = [], I = 0;
    return z(d, A, "", "", function(K) {
      return w.call(H, K, I++);
    }), A;
  }
  function B(d) {
    if (d._status === -1) {
      var w = d._result;
      w = w(), w.then(
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = H);
        },
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = H);
        }
      ), d._status === -1 && (d._status = 0, d._result = w);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var w = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(w)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Q = {
    map: J,
    forEach: function(d, w, H) {
      J(
        d,
        function() {
          w.apply(this, arguments);
        },
        H
      );
    },
    count: function(d) {
      var w = 0;
      return J(d, function() {
        w++;
      }), w;
    },
    toArray: function(d) {
      return J(d, function(w) {
        return w;
      }) || [];
    },
    only: function(d) {
      if (!$(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return V.Activity = m, V.Children = Q, V.Component = v, V.Fragment = n, V.Profiler = i, V.PureComponent = D, V.StrictMode = l, V.Suspense = o, V.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, V.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, V.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, V.cacheSignal = function() {
    return null;
  }, V.cloneElement = function(d, w, H) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var A = T({}, d.props), I = d.key;
    if (w != null)
      for (K in w.key !== void 0 && (I = "" + w.key), w)
        !F.call(w, K) || K === "key" || K === "__self" || K === "__source" || K === "ref" && w.ref === void 0 || (A[K] = w[K]);
    var K = arguments.length - 2;
    if (K === 1) A.children = H;
    else if (1 < K) {
      for (var Y = Array(K), te = 0; te < K; te++)
        Y[te] = arguments[te + 2];
      A.children = Y;
    }
    return k(d.type, I, A);
  }, V.createContext = function(d) {
    return d = {
      $$typeof: s,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: c,
      _context: d
    }, d;
  }, V.createElement = function(d, w, H) {
    var A, I = {}, K = null;
    if (w != null)
      for (A in w.key !== void 0 && (K = "" + w.key), w)
        F.call(w, A) && A !== "key" && A !== "__self" && A !== "__source" && (I[A] = w[A]);
    var Y = arguments.length - 2;
    if (Y === 1) I.children = H;
    else if (1 < Y) {
      for (var te = Array(Y), ee = 0; ee < Y; ee++)
        te[ee] = arguments[ee + 2];
      I.children = te;
    }
    if (d && d.defaultProps)
      for (A in Y = d.defaultProps, Y)
        I[A] === void 0 && (I[A] = Y[A]);
    return k(d, K, I);
  }, V.createRef = function() {
    return { current: null };
  }, V.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, V.isValidElement = $, V.lazy = function(d) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, V.memo = function(d, w) {
    return {
      $$typeof: a,
      type: d,
      compare: w === void 0 ? null : w
    };
  }, V.startTransition = function(d) {
    var w = E.T, H = {};
    E.T = H;
    try {
      var A = d(), I = E.S;
      I !== null && I(H, A), typeof A == "object" && A !== null && typeof A.then == "function" && A.then(_, P);
    } catch (K) {
      P(K);
    } finally {
      w !== null && H.types !== null && (w.types = H.types), E.T = w;
    }
  }, V.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, V.use = function(d) {
    return E.H.use(d);
  }, V.useActionState = function(d, w, H) {
    return E.H.useActionState(d, w, H);
  }, V.useCallback = function(d, w) {
    return E.H.useCallback(d, w);
  }, V.useContext = function(d) {
    return E.H.useContext(d);
  }, V.useDebugValue = function() {
  }, V.useDeferredValue = function(d, w) {
    return E.H.useDeferredValue(d, w);
  }, V.useEffect = function(d, w) {
    return E.H.useEffect(d, w);
  }, V.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, V.useId = function() {
    return E.H.useId();
  }, V.useImperativeHandle = function(d, w, H) {
    return E.H.useImperativeHandle(d, w, H);
  }, V.useInsertionEffect = function(d, w) {
    return E.H.useInsertionEffect(d, w);
  }, V.useLayoutEffect = function(d, w) {
    return E.H.useLayoutEffect(d, w);
  }, V.useMemo = function(d, w) {
    return E.H.useMemo(d, w);
  }, V.useOptimistic = function(d, w) {
    return E.H.useOptimistic(d, w);
  }, V.useReducer = function(d, w, H) {
    return E.H.useReducer(d, w, H);
  }, V.useRef = function(d) {
    return E.H.useRef(d);
  }, V.useState = function(d) {
    return E.H.useState(d);
  }, V.useSyncExternalStore = function(d, w, H) {
    return E.H.useSyncExternalStore(
      d,
      w,
      H
    );
  }, V.useTransition = function() {
    return E.H.useTransition();
  }, V.version = "19.2.4", V;
}
var dt;
function bl() {
  return dt || (dt = 1, Ve.exports = _l()), Ve.exports;
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
var mt;
function vl() {
  if (mt) return se;
  mt = 1;
  var r = bl();
  function t(o) {
    var a = "https://react.dev/errors/" + o;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var p = 2; p < arguments.length; p++)
        a += "&args[]=" + encodeURIComponent(arguments[p]);
    }
    return "Minified React error #" + o + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var l = {
    d: {
      f: n,
      r: function() {
        throw Error(t(522));
      },
      D: n,
      C: n,
      L: n,
      m: n,
      X: n,
      S: n,
      M: n
    },
    p: 0,
    findDOMNode: null
  }, i = Symbol.for("react.portal");
  function c(o, a, p) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: m == null ? null : "" + m,
      children: o,
      containerInfo: a,
      implementation: p
    };
  }
  var s = r.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, a) {
    if (o === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, se.createPortal = function(o, a) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return c(o, a, null, p);
  }, se.flushSync = function(o) {
    var a = s.T, p = l.p;
    try {
      if (s.T = null, l.p = 2, o) return o();
    } finally {
      s.T = a, l.p = p, l.d.f();
    }
  }, se.preconnect = function(o, a) {
    typeof o == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, l.d.C(o, a));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && l.d.D(o);
  }, se.preinit = function(o, a) {
    if (typeof o == "string" && a && typeof a.as == "string") {
      var p = a.as, m = u(p, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, N = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      p === "style" ? l.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: m,
          integrity: h,
          fetchPriority: N
        }
      ) : p === "script" && l.d.X(o, {
        crossOrigin: m,
        integrity: h,
        fetchPriority: N,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, se.preinitModule = function(o, a) {
    if (typeof o == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var p = u(
            a.as,
            a.crossOrigin
          );
          l.d.M(o, {
            crossOrigin: p,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && l.d.M(o);
  }, se.preload = function(o, a) {
    if (typeof o == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var p = a.as, m = u(p, a.crossOrigin);
      l.d.L(o, p, {
        crossOrigin: m,
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
  }, se.preloadModule = function(o, a) {
    if (typeof o == "string")
      if (a) {
        var p = u(a.as, a.crossOrigin);
        l.d.m(o, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: p,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else l.d.m(o);
  }, se.requestFormReset = function(o) {
    l.d.r(o);
  }, se.unstable_batchedUpdates = function(o, a) {
    return o(a);
  }, se.useFormState = function(o, a, p) {
    return s.H.useFormState(o, a, p);
  }, se.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function El() {
  if (pt) return Ue.exports;
  pt = 1;
  function r() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(r);
      } catch (t) {
        console.error(t);
      }
  }
  return r(), Ue.exports = vl(), Ue.exports;
}
var gl = El();
const { useState: _e, useCallback: ce, useRef: Te, useEffect: ye, useMemo: Ze } = e;
function tt({ image: r }) {
  if (!r) return null;
  if (r.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: r, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = r.startsWith("css:") ? r.substring(4) : r.startsWith("colored:") ? r.substring(8) : r;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Cl({
  option: r,
  removable: t,
  onRemove: n,
  removeLabel: l,
  draggable: i,
  onDragStart: c,
  onDragOver: s,
  onDrop: u,
  onDragEnd: o,
  dragClassName: a
}) {
  const p = ce(
    (m) => {
      m.stopPropagation(), n(r.value);
    },
    [n, r.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
      draggable: i || void 0,
      onDragStart: c,
      onDragOver: s,
      onDrop: u,
      onDragEnd: o
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(tt, { image: r.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, r.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: p,
        "aria-label": l
      },
      "×"
    )
  );
}
function yl({
  option: r,
  highlighted: t,
  searchTerm: n,
  onSelect: l,
  onMouseEnter: i,
  id: c
}) {
  const s = ce(() => l(r.value), [l, r.value]), u = Ze(() => {
    if (!n) return r.label;
    const o = r.label.toLowerCase().indexOf(n.toLowerCase());
    return o < 0 ? r.label : /* @__PURE__ */ e.createElement(e.Fragment, null, r.label.substring(0, o), /* @__PURE__ */ e.createElement("strong", null, r.label.substring(o, o + n.length)), r.label.substring(o + n.length));
  }, [r.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: c,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(tt, { image: r.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const wl = ({ controlId: r, state: t }) => {
  const n = le(), l = t.value ?? [], i = t.multiSelect === !0, c = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, p = t.options ?? [], m = t.emptyOptionLabel ?? "", h = c && i && !u && o, N = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = N["js.dropdownSelect.nothingFound"], T = ce(
    (C) => N["js.dropdownSelect.removeChip"].replace("{0}", C),
    [N]
  ), [y, v] = _e(!1), [g, D] = _e(""), [j, b] = _e(-1), [_, E] = _e(!1), [F, k] = _e({}), [S, $] = _e(null), [x, O] = _e(null), [Z, ne] = _e(null), z = Te(null), J = Te(null), B = Te(null), P = Te(l);
  P.current = l;
  const Q = Te(-1), d = Ze(
    () => new Set(l.map((C) => C.value)),
    [l]
  ), w = Ze(() => {
    let C = p.filter((L) => !d.has(L.value));
    if (g) {
      const L = g.toLowerCase();
      C = C.filter((W) => W.label.toLowerCase().includes(L));
    }
    return C;
  }, [p, d, g]);
  ye(() => {
    g && w.length === 1 ? b(0) : b(-1);
  }, [w.length, g]), ye(() => {
    y && a && J.current && J.current.focus();
  }, [y, a, l]), ye(() => {
    var W, q;
    if (Q.current < 0) return;
    const C = Q.current;
    Q.current = -1;
    const L = (W = z.current) == null ? void 0 : W.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(C, L.length - 1)].focus() : (q = z.current) == null || q.focus();
  }, [l]), ye(() => {
    if (!y) return;
    const C = (L) => {
      z.current && !z.current.contains(L.target) && B.current && !B.current.contains(L.target) && (v(!1), D(""));
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [y]), ye(() => {
    if (!y || !z.current) return;
    const C = z.current.getBoundingClientRect(), L = window.innerHeight - C.bottom, q = L < 300 && C.top > L;
    k({
      left: C.left,
      width: C.width,
      ...q ? { bottom: window.innerHeight - C.top } : { top: C.bottom }
    });
  }, [y]);
  const H = ce(async () => {
    if (!(u || !o) && (v(!0), D(""), b(-1), E(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, o, a, n]), A = ce(() => {
    var C;
    v(!1), D(""), b(-1), (C = z.current) == null || C.focus();
  }, []), I = ce(
    (C) => {
      let L;
      if (i) {
        const W = p.find((q) => q.value === C);
        if (W)
          L = [...P.current, W];
        else
          return;
      } else {
        const W = p.find((q) => q.value === C);
        if (W)
          L = [W];
        else
          return;
      }
      P.current = L, n("valueChanged", { value: L.map((W) => W.value) }), i ? (D(""), b(-1)) : A();
    },
    [i, p, n, A]
  ), K = ce(
    (C) => {
      Q.current = P.current.findIndex((W) => W.value === C);
      const L = P.current.filter((W) => W.value !== C);
      P.current = L, n("valueChanged", { value: L.map((W) => W.value) });
    },
    [n]
  ), Y = ce(
    (C) => {
      C.stopPropagation(), n("valueChanged", { value: [] }), A();
    },
    [n, A]
  ), te = ce((C) => {
    D(C.target.value);
  }, []), ee = ce(
    (C) => {
      if (!y) {
        if (C.key === "ArrowDown" || C.key === "ArrowUp" || C.key === "Enter" || C.key === " ") {
          if (C.target.tagName === "BUTTON") return;
          C.preventDefault(), C.stopPropagation(), H();
        }
        return;
      }
      switch (C.key) {
        case "ArrowDown":
          C.preventDefault(), C.stopPropagation(), b(
            (L) => L < w.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          C.preventDefault(), C.stopPropagation(), b(
            (L) => L > 0 ? L - 1 : w.length - 1
          );
          break;
        case "Enter":
          C.preventDefault(), C.stopPropagation(), j >= 0 && j < w.length && I(w[j].value);
          break;
        case "Escape":
          C.preventDefault(), C.stopPropagation(), A();
          break;
        case "Tab":
          A();
          break;
        case "Backspace":
          g === "" && i && l.length > 0 && K(l[l.length - 1].value);
          break;
      }
    },
    [
      y,
      H,
      A,
      w,
      j,
      I,
      g,
      i,
      l,
      K
    ]
  ), ie = ce(
    async (C) => {
      C.preventDefault(), E(!1);
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
    },
    [n]
  ), ue = ce(
    (C, L) => {
      $(C), L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", String(C));
    },
    []
  ), he = ce(
    (C, L) => {
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", S === null || S === C) {
        O(null), ne(null);
        return;
      }
      const W = L.currentTarget.getBoundingClientRect(), q = W.left + W.width / 2, re = L.clientX < q ? "before" : "after";
      O(C), ne(re);
    },
    [S]
  ), fe = ce(
    (C) => {
      if (C.preventDefault(), S === null || x === null || Z === null || S === x) return;
      const L = [...P.current], [W] = L.splice(S, 1);
      let q = x;
      S < x ? q = Z === "before" ? q - 1 : q : q = Z === "before" ? q : q + 1, L.splice(q, 0, W), P.current = L, n("valueChanged", { value: L.map((re) => re.value) }), $(null), O(null), ne(null);
    },
    [S, x, Z, n]
  ), ve = ce(() => {
    $(null), O(null), ne(null);
  }, []);
  if (ye(() => {
    if (j < 0 || !B.current) return;
    const C = B.current.querySelector(
      `[id="${r}-opt-${j}"]`
    );
    C && C.scrollIntoView({ block: "nearest" });
  }, [j, r]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: r, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, m) : l.map((C) => /* @__PURE__ */ e.createElement("span", { key: C.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: C.image }), /* @__PURE__ */ e.createElement("span", null, C.label))));
  const R = !s && l.length > 0 && !u, M = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: F
    },
    (a || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: g,
        onChange: te,
        onKeyDown: ee,
        placeholder: N["js.dropdownSelect.filterPlaceholder"],
        "aria-label": N["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": j >= 0 ? `${r}-opt-${j}` : void 0,
        "aria-controls": `${r}-listbox`
      }
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        id: `${r}-listbox`,
        role: "listbox",
        className: "tlDropdownSelect__list"
      },
      !a && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, N["js.dropdownSelect.error"])),
      a && w.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      a && w.map((C, L) => /* @__PURE__ */ e.createElement(
        yl,
        {
          key: C.value,
          id: `${r}-opt-${L}`,
          option: C,
          highlighted: L === j,
          searchTerm: g,
          onSelect: I,
          onMouseEnter: () => b(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      ref: z,
      className: "tlDropdownSelect" + (y ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": y,
      "aria-haspopup": "listbox",
      "aria-owns": y ? `${r}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: y ? void 0 : H,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : l.map((C, L) => {
      let W = "";
      return S === L ? W = "tlDropdownSelect__chip--dragging" : x === L && Z === "before" ? W = "tlDropdownSelect__chip--dropBefore" : x === L && Z === "after" && (W = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Cl,
        {
          key: C.value,
          option: C,
          removable: !u && (i || !s),
          onRemove: K,
          removeLabel: T(C.label),
          draggable: h,
          onDragStart: h ? (q) => ue(L, q) : void 0,
          onDragOver: h ? (q) => he(L, q) : void 0,
          onDrop: h ? fe : void 0,
          onDragEnd: h ? ve : void 0,
          dragClassName: h ? W : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, R && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Y,
        "aria-label": N["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), M && gl.createPortal(M, document.body));
}, { useCallback: Ke, useRef: kl } = e, Et = "application/x-tl-color", Sl = ({
  colors: r,
  columns: t,
  onSelect: n,
  onConfirm: l,
  onSwap: i,
  onReplace: c
}) => {
  const s = kl(null), u = Ke(
    (p) => (m) => {
      s.current = p, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ke((p) => {
    p.preventDefault(), p.dataTransfer.dropEffect = "move";
  }, []), a = Ke(
    (p) => (m) => {
      m.preventDefault();
      const h = m.dataTransfer.getData(Et);
      h ? c(p, h) : s.current !== null && s.current !== p && i(s.current, p), s.current = null;
    },
    [i, c]
  );
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__palette",
      style: { gridTemplateColumns: `repeat(${t}, 1fr)` }
    },
    r.map((p, m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m,
        className: "tlColorInput__paletteCell" + (p == null ? " tlColorInput__paletteCell--empty" : ""),
        style: p != null ? { backgroundColor: p } : void 0,
        title: p ?? "",
        draggable: p != null,
        onClick: p != null ? () => n(p) : void 0,
        onDoubleClick: p != null ? () => l(p) : void 0,
        onDragStart: p != null ? u(m) : void 0,
        onDragOver: o,
        onDrop: a(m)
      }
    ))
  );
};
function gt(r) {
  return Math.max(0, Math.min(255, Math.round(r)));
}
function Qe(r) {
  return /^#[0-9a-fA-F]{6}$/.test(r);
}
function Ct(r) {
  if (!Qe(r)) return [0, 0, 0];
  const t = parseInt(r.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function yt(r, t, n) {
  const l = (i) => gt(i).toString(16).padStart(2, "0");
  return "#" + l(r) + l(t) + l(n);
}
function Nl(r, t, n) {
  const l = r / 255, i = t / 255, c = n / 255, s = Math.max(l, i, c), u = Math.min(l, i, c), o = s - u;
  let a = 0;
  o !== 0 && (s === l ? a = (i - c) / o % 6 : s === i ? a = (c - l) / o + 2 : a = (l - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const p = s === 0 ? 0 : o / s;
  return [a, p, s];
}
function Tl(r, t, n) {
  const l = n * t, i = l * (1 - Math.abs(r / 60 % 2 - 1)), c = n - l;
  let s = 0, u = 0, o = 0;
  return r < 60 ? (s = l, u = i, o = 0) : r < 120 ? (s = i, u = l, o = 0) : r < 180 ? (s = 0, u = l, o = i) : r < 240 ? (s = 0, u = i, o = l) : r < 300 ? (s = i, u = 0, o = l) : (s = l, u = 0, o = i), [
    Math.round((s + c) * 255),
    Math.round((u + c) * 255),
    Math.round((o + c) * 255)
  ];
}
function Rl(r) {
  return Nl(...Ct(r));
}
function Ye(r, t, n) {
  return yt(...Tl(r, t, n));
}
const { useCallback: we, useRef: ft } = e, xl = ({ color: r, onColorChange: t }) => {
  const [n, l, i] = Rl(r), c = ft(null), s = ft(null), u = we(
    (f, T) => {
      var D;
      const y = (D = c.current) == null ? void 0 : D.getBoundingClientRect();
      if (!y) return;
      const v = Math.max(0, Math.min(1, (f - y.left) / y.width)), g = Math.max(0, Math.min(1, 1 - (T - y.top) / y.height));
      t(Ye(n, v, g));
    },
    [n, t]
  ), o = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), a = we(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), p = we(
    (f) => {
      var g;
      const T = (g = s.current) == null ? void 0 : g.getBoundingClientRect();
      if (!T) return;
      const v = Math.max(0, Math.min(1, (f - T.top) / T.height)) * 360;
      t(Ye(v, l, i));
    },
    [l, i, t]
  ), m = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), p(f.clientY);
    },
    [p]
  ), h = we(
    (f) => {
      f.buttons !== 0 && p(f.clientY);
    },
    [p]
  ), N = Ye(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: N },
      onPointerDown: o,
      onPointerMove: a
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${l * 100}%`, top: `${(1 - i) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__hueSlider",
      onPointerDown: m,
      onPointerMove: h
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${n / 360 * 100}%` }
      }
    )
  ));
};
function Ll(r, t) {
  const n = t.toUpperCase();
  return r.some((l) => l != null && l.toUpperCase() === n);
}
const Dl = {
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
}, { useState: Me, useCallback: pe, useEffect: Xe, useRef: Il, useLayoutEffect: Ml } = e, Pl = ({
  anchorRef: r,
  currentColor: t,
  palette: n,
  paletteColumns: l,
  defaultPalette: i,
  canReset: c,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: o
}) => {
  const [a, p] = Me("palette"), [m, h] = Me(t), N = Il(null), f = ae(Dl), [T, y] = Me(null);
  Ml(() => {
    if (!r.current || !N.current) return;
    const B = r.current.getBoundingClientRect(), P = N.current.getBoundingClientRect();
    let Q = B.bottom + 4, d = B.left;
    Q + P.height > window.innerHeight && (Q = B.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, B.right - P.width)), y({ top: Q, left: d });
  }, [r]);
  const v = m != null, [g, D, j] = v ? Ct(m) : [0, 0, 0], [b, _] = Me((m == null ? void 0 : m.toUpperCase()) ?? "");
  Xe(() => {
    _((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Xe(() => {
    const B = (P) => {
      P.key === "Escape" && u();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [u]), Xe(() => {
    const B = (Q) => {
      N.current && !N.current.contains(Q.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const E = pe(
    (B) => (P) => {
      const Q = parseInt(P.target.value, 10);
      if (isNaN(Q)) return;
      const d = gt(Q);
      h(yt(B === "r" ? d : g, B === "g" ? d : D, B === "b" ? d : j));
    },
    [g, D, j]
  ), F = pe(
    (B) => {
      if (m != null) {
        B.dataTransfer.setData(Et, m.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = m, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), B.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [m]
  ), k = pe((B) => {
    const P = B.target.value;
    _(P), Qe(P) && h(P);
  }, []), S = pe(() => {
    h(null);
  }, []), $ = pe((B) => {
    h(B);
  }, []), x = pe(
    (B) => {
      s(B);
    },
    [s]
  ), O = pe(
    (B, P) => {
      const Q = [...n], d = Q[B];
      Q[B] = Q[P], Q[P] = d, o(Q);
    },
    [n, o]
  ), Z = pe(
    (B, P) => {
      const Q = [...n];
      Q[B] = P, o(Q);
    },
    [n, o]
  ), ne = pe(() => {
    o([...i]);
  }, [i, o]), z = pe(
    (B) => {
      if (Ll(n, B)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const Q = [...n];
      Q[P] = B.toUpperCase(), o(Q);
    },
    [n, o]
  ), J = pe(() => {
    m != null && z(m), s(m);
  }, [m, s, z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: N,
      style: T ? { top: T.top, left: T.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Sl,
      {
        colors: n,
        columns: l,
        onSelect: $,
        onConfirm: x,
        onSwap: O,
        onReplace: Z
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ne }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(xl, { color: m ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: m } : void 0,
        draggable: v,
        onDragStart: v ? F : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? g : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? D : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? j : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !Qe(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: k
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: S }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, f["js.colorInput.ok"]))
  );
}, jl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Al, useCallback: Pe, useRef: Bl } = e, Ol = ({ controlId: r, state: t }) => {
  const n = le(), l = ae(jl), [i, c] = Al(!1), s = Bl(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], p = t.paletteColumns ?? 6, m = t.defaultPalette ?? a, h = Pe(() => {
    o && c(!0);
  }, [o]), N = Pe(
    (y) => {
      c(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Pe(() => {
    c(!1);
  }, []), T = Pe(
    (y) => {
      n("paletteChanged", { palette: y });
    },
    [n]
  );
  return o ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": l["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Pl,
    {
      anchorRef: s,
      currentColor: u,
      palette: a,
      paletteColumns: p,
      defaultPalette: m,
      canReset: t.canReset !== !1,
      onConfirm: N,
      onCancel: f,
      onPaletteChange: T
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: r,
      className: "tlColorInput tlColorInput--immutable" + (u == null ? " tlColorInput--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      title: u ?? ""
    }
  );
}, { useState: Re, useCallback: be, useEffect: je, useRef: ht, useLayoutEffect: $l, useMemo: Fl } = e, Hl = {
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
}, Wl = ({
  anchorRef: r,
  currentValue: t,
  icons: n,
  iconsLoaded: l,
  onSelect: i,
  onCancel: c,
  onLoadIcons: s
}) => {
  const u = ae(Hl), [o, a] = Re("simple"), [p, m] = Re(""), [h, N] = Re(t ?? ""), [f, T] = Re(!1), [y, v] = Re(null), g = ht(null), D = ht(null);
  $l(() => {
    if (!r.current || !g.current) return;
    const x = r.current.getBoundingClientRect(), O = g.current.getBoundingClientRect();
    let Z = x.bottom + 4, ne = x.left;
    Z + O.height > window.innerHeight && (Z = x.top - O.height - 4), ne + O.width > window.innerWidth && (ne = Math.max(0, x.right - O.width)), v({ top: Z, left: ne });
  }, [r]), je(() => {
    !l && !f && s().catch(() => T(!0));
  }, [l, f, s]), je(() => {
    l && D.current && D.current.focus();
  }, [l]), je(() => {
    const x = (O) => {
      O.key === "Escape" && c();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [c]), je(() => {
    const x = (Z) => {
      g.current && !g.current.contains(Z.target) && c();
    }, O = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", x);
    };
  }, [c]);
  const j = Fl(() => {
    if (!p) return n;
    const x = p.toLowerCase();
    return n.filter(
      (O) => O.prefix.toLowerCase().includes(x) || O.label.toLowerCase().includes(x) || O.terms != null && O.terms.some((Z) => Z.includes(x))
    );
  }, [n, p]), b = be((x) => {
    m(x.target.value);
  }, []), _ = be(
    (x) => {
      i(x);
    },
    [i]
  ), E = be((x) => {
    N(x);
  }, []), F = be((x) => {
    N(x.target.value);
  }, []), k = be(() => {
    i(h || null);
  }, [h, i]), S = be(() => {
    i(null);
  }, [i]), $ = be(async (x) => {
    x.preventDefault(), T(!1);
    try {
      await s();
    } catch {
      T(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: g,
      style: y ? { top: y.top, left: y.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "text",
        className: "tlIconSelect__search",
        value: p,
        onChange: b,
        placeholder: u["js.iconSelect.filterPlaceholder"],
        "aria-label": u["js.iconSelect.filterPlaceholder"]
      }
    ), p && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => m(""),
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
      !l && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: $ }, u["js.iconSelect.loadError"])),
      l && j.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      l && j.map(
        (x) => x.variants.map((O) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: O.encoded,
            className: "tlIconSelect__iconCell" + (O.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": O.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => o === "simple" ? _(O.encoded) : E(O.encoded),
            onKeyDown: (Z) => {
              (Z.key === "Enter" || Z.key === " ") && (Z.preventDefault(), o === "simple" ? _(O.encoded) : E(O.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: O.encoded })
        ))
      )
    ),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: F
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Se, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: k }, u["js.iconSelect.ok"]))
  );
}, zl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Ul, useCallback: Ae, useRef: Vl } = e, Kl = ({ controlId: r, state: t }) => {
  const n = le(), l = ae(zl), [i, c] = Ul(!1), s = Vl(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, p = t.icons ?? [], m = t.iconsLoaded === !0, h = Ae(() => {
    o && !a && c(!0);
  }, [o, a]), N = Ae(
    (y) => {
      c(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Ae(() => {
    c(!1);
  }, []), T = Ae(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: r, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: a,
      title: u ?? "",
      "aria-label": l["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Wl,
    {
      anchorRef: s,
      currentValue: u,
      icons: p,
      iconsLoaded: m,
      onSelect: N,
      onCancel: f,
      onLoadIcons: T
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: r, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: Yl, useMemo: _t, useRef: Xl, useState: Ge } = e, Gl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, ql = [1, 2, 3, 4];
function Zl(r, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(r.trim());
  if (!n) return 16 * t;
  const l = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? l * t : l;
}
function Ql(r, t) {
  const n = Math.max(1, Math.floor(r / t));
  let l = 1;
  for (const i of ql)
    n >= i && (l = i);
  return l;
}
function Jl(r, t) {
  const n = Gl[r] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function er(r, t) {
  const n = Math.max(1, t), l = {}, i = (m, h) => !!(l[m] && l[m][h]), c = (m, h) => {
    l[m] || (l[m] = {}), l[m][h] = !0;
  }, s = [];
  let u = 0, o = 0;
  const a = (m) => {
    let h = null;
    for (const f of s) f.rowStart === m && (h = f);
    if (!h) return;
    let N = h.colEnd;
    for (; N < n && !i(m, N); ) N++;
    if (N !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let T = h.colEnd; T < N; T++) c(f, T);
      h.colEnd = N;
    }
  };
  for (const m of r) {
    const h = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let N = Math.min(Jl(m.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let D = o; D < n && !i(u, D); D++)
      f++;
    if (N > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let D = o; D < n && !i(u, D); D++)
        f++;
      N = Math.min(N, f);
    }
    const T = o, y = o + N, v = u, g = u + h;
    s.push({ id: m.id, colStart: T, colEnd: y, rowStart: v, rowEnd: g });
    for (let D = v; D < g; D++)
      for (let j = T; j < y; j++) c(D, j);
    o = y, o >= n && (o = 0, u++);
  }
  a(u);
  let p = 0;
  for (const m of s) m.rowEnd > p && (p = m.rowEnd);
  for (let m = 1; m < p; m++)
    for (let h = 0; h < n; h++) {
      if (i(m, h)) continue;
      const N = s.find((f) => f.rowEnd === m && f.colStart <= h && h < f.colEnd);
      if (N) {
        N.rowEnd = m + 1;
        for (let f = N.colStart; f < N.colEnd; f++) c(m, f);
      }
    }
  return s;
}
const tr = ({ controlId: r }) => {
  const t = G(), n = le(), l = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), c = Xl(null), [s, u] = Ge(1), o = t.editMode === !0;
  Yl(() => {
    const _ = c.current;
    if (!_) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, F = Zl(l, E), k = () => u(Ql(_.clientWidth, F));
    k();
    const S = new ResizeObserver(k);
    return S.observe(_), () => S.disconnect();
  }, [l]);
  const a = _t(() => er(i, s), [i, s]), p = _t(() => {
    const _ = {};
    for (const E of a) _[E.id] = E;
    return _;
  }, [a]), [m, h] = Ge(null), [N, f] = Ge(null), T = ke((_, E) => {
    if (!o) {
      _.preventDefault();
      return;
    }
    h(E), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", E);
  }, [o]), y = ke((_, E) => {
    if (!o || !m || m === E) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const F = _.currentTarget.getBoundingClientRect(), k = _.clientX < F.left + F.width / 2;
    f((S) => S && S.id === E && S.before === k ? S : { id: E, before: k });
  }, [o, m]), v = ke(() => {
  }, []), g = ke((_, E, F) => {
    const k = i.map((O) => O.id), S = k.indexOf(_);
    if (S < 0) return;
    k.splice(S, 1);
    const $ = k.indexOf(E);
    if ($ < 0) {
      k.splice(S, 0, _);
      return;
    }
    const x = F ? $ : $ + 1;
    k.splice(x, 0, _), n("reorder", { order: k });
  }, [i, n]), D = ke((_, E) => {
    if (!o || !m || m === E) return;
    _.preventDefault();
    const F = _.currentTarget.getBoundingClientRect(), k = _.clientX < F.left + F.width / 2;
    g(m, E, k), h(null), f(null);
  }, [o, m, g]), j = ke(() => {
    h(null), f(null);
  }, []), b = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: r,
      ref: c,
      className: "tlDashboard" + (o ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, i.map((_) => {
      const E = p[_.id];
      if (!E) return null;
      const F = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, k = ["tlDashboard__tile"];
      return m === _.id && k.push("tlDashboard__tile--dragging"), N && N.id === _.id && k.push(N.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: k.join(" "),
          style: F,
          draggable: o,
          onDragStart: (S) => T(S, _.id),
          onDragOver: (S) => y(S, _.id),
          onDragLeave: v,
          onDrop: (S) => D(S, _.id),
          onDragEnd: j
        },
        /* @__PURE__ */ e.createElement(X, { control: _.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
};
U("TLButton", Bt);
U("TLToggleButton", $t);
U("TLTextInput", Nt);
U("TLNumberInput", Rt);
U("TLDatePicker", Lt);
U("TLSelect", It);
U("TLCheckbox", Pt);
U("TLTable", jt);
U("TLCounter", Ft);
U("TLTabBar", Wt);
U("TLFieldList", zt);
U("TLAudioRecorder", Vt);
U("TLAudioPlayer", Yt);
U("TLFileUpload", Gt);
U("TLDownload", Zt);
U("TLPhotoCapture", Jt);
U("TLPhotoViewer", tn);
U("TLSplitPanel", nn);
U("TLPanel", un);
U("TLMaximizeRoot", dn);
U("TLDeckPane", mn);
U("TLSidebar", gn);
U("TLStack", Cn);
U("TLGrid", yn);
U("TLCard", wn);
U("TLAppBar", kn);
U("TLBreadcrumb", Nn);
U("TLBottomBar", Rn);
U("TLDialog", Ln);
U("TLDialogManager", Mn);
U("TLWindow", An);
U("TLDrawer", Fn);
U("TLContextMenuRegion", Wn);
U("TLSnackbar", Kn);
U("TLMenu", Xn);
U("TLAppShell", Gn);
U("TLText", qn);
U("TLTableView", Qn);
U("TLFormLayout", ol);
U("TLFormGroup", cl);
U("TLFormField", ml);
U("TLResourceCell", pl);
U("TLTreeView", hl);
U("TLDropdownSelect", wl);
U("TLColorInput", Ol);
U("TLIconSelect", Kl);
U("TLDashboard", tr);
