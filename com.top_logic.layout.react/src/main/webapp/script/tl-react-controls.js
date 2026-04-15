import { React as e, useTLFieldValue as xe, getComponent as kt, useTLState as q, useTLCommand as le, TLChild as G, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as U } from "tl-react-bridge";
const { useCallback: St } = e, Nt = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = St(
    (o) => {
      r(o.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = t.errorMessage, a = [
    "tlReactTextInput",
    c ? "tlReactTextInput--error" : "",
    !c && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": c || void 0,
      title: c && u ? u : void 0
    }
  ));
}, { useCallback: Tt } = e, Rt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = xe(), c = Tt(
    (p) => {
      const m = p.target.value;
      i(m === "" ? null : m);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const s = t.hasError === !0, u = t.hasWarnings === !0, a = t.errorMessage, o = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && a ? a : void 0
    }
  ));
}, { useCallback: xt } = e, Lt = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = xt(
    (a) => {
      r(a.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const c = t.hasError === !0, s = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    c ? "tlReactDatePicker--error" : "",
    !c && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
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
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t, config: n }) => {
  var p;
  const [r, i] = xe(), c = Dt(
    (m) => {
      i(m.target.value || null);
    },
    [i]
  ), s = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((p = s.find((h) => h.value === r)) == null ? void 0 : p.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const u = t.hasError === !0, a = t.hasWarnings === !0, o = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && a ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Mt } = e, Pt = ({ controlId: l, state: t }) => {
  const [n, r] = xe(), i = Mt(
    (a) => {
      r(a.target.checked);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
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
      id: l,
      checked: n === !0,
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": c || void 0
    }
  );
}, jt = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, c) => /* @__PURE__ */ e.createElement("tr", { key: c }, n.map((s) => {
    const u = s.cellModule ? kt(s.cellModule) : void 0, a = i[s.name];
    if (u) {
      const o = { value: a, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + c + "-" + s.name,
          state: o
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, a != null ? String(a) : "");
  })))));
};
function Se({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const n = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const n = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: n + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const { useCallback: At } = e, Bt = ({ controlId: l, command: t, label: n, disabled: r }) => {
  const i = q(), c = le(), s = t ?? "click", u = n ?? i.label, a = r ?? i.disabled === !0, o = i.hidden === !0, p = i.image, m = i.iconOnly === !0, h = i.tooltip, S = o ? { display: "none" } : void 0, f = h ?? u, b = h ? `text:${h}` : void 0, v = At(() => {
    c(s);
  }, [c, s]);
  return p && m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: v,
      disabled: a,
      style: S,
      className: "tlReactButton tlReactButton--icon",
      "data-tooltip": f ? `text:${f}` : void 0,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(Se, { encoded: p })
  ) : p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: v,
      disabled: a,
      style: S,
      className: "tlReactButton",
      "data-tooltip": b
    },
    /* @__PURE__ */ e.createElement(Se, { encoded: p, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: v,
      disabled: a,
      style: S,
      className: "tlReactButton",
      "data-tooltip": b
    },
    u
  );
}, { useCallback: Ot } = e, $t = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const c = q(), s = le(), u = t ?? "click", a = n ?? c.label, o = r ?? c.active === !0, p = i ?? c.disabled === !0, m = Ot(() => {
    s(u);
  }, [s, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: p,
      className: "tlReactButton" + (o ? " tlReactButtonActive" : "")
    },
    a
  );
}, Ft = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ht } = e, Wt = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.tabs ?? [], i = t.activeTabId, c = Ht((s) => {
    s !== i && n("selectTab", { tabId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === i,
      className: "tlReactTabBar__tab" + (s.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, zt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(G, { control: i })))));
}, Ut = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Vt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(null), u = e.useRef(null), a = e.useRef([]), o = e.useRef(null), p = t.status ?? "idle", m = t.error, h = p === "received" ? "idle" : r !== "idle" ? r : p, S = e.useCallback(async () => {
    if (r === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (r !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = w, a.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", O = new MediaRecorder(w, D ? { mimeType: D } : void 0);
        u.current = O, O.ondataavailable = (E) => {
          E.data.size > 0 && a.current.push(E.data);
        }, O.onstop = async () => {
          w.getTracks().forEach((g) => g.stop()), o.current = null;
          const E = new Blob(a.current, { type: O.mimeType || "audio/webm" });
          if (a.current = [], E.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", E, "recording.webm"), await n(_), i("idle");
        }, O.start(), i("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), s("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(Ut), b = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], v = h === "uploading", R = ["tlAudioRecorder__button"];
  return h === "recording" && R.push("tlAudioRecorder__button--recording"), h === "uploading" && R.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: R.join(" "),
      onClick: S,
      disabled: v,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[c]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Kt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Yt = ({ controlId: l }) => {
  const t = q(), n = et(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [c, s] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), a = e.useRef(null), o = e.useRef(i);
  e.useEffect(() => {
    r ? c === "disabled" && s("idle") : (u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), s("disabled"));
  }, [r]), e.useEffect(() => {
    i !== o.current && (o.current = i, u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (c === "playing" || c === "paused" || c === "loading") && s("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
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
    if (!a.current) {
      s("loading");
      try {
        const v = await fetch(n);
        if (!v.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", v.status), s("idle");
          return;
        }
        const R = await v.blob();
        a.current = URL.createObjectURL(R);
      } catch (v) {
        console.error("[TLAudioPlayer] Fetch error:", v), s("idle");
        return;
      }
    }
    const b = new Audio(a.current);
    u.current = b, b.onended = () => {
      s("idle");
    }, b.play(), s("playing");
  }, [c, n]), m = ae(Kt), h = c === "loading" ? m["js.loading"] : c === "playing" ? m["js.audioPlayer.pause"] : c === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], S = c === "disabled" || c === "loading", f = ["tlAudioPlayer__button"];
  return c === "playing" && f.push("tlAudioPlayer__button--playing"), c === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: p,
      disabled: S,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${c === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Gt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Xt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(!1), u = e.useRef(null), a = t.status ?? "idle", o = t.error, p = t.accept ?? "", m = a === "received" ? "idle" : r !== "idle" ? r : a, h = e.useCallback(async (E) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", E, E.name), await n(_), i("idle");
  }, [n]), S = e.useCallback((E) => {
    var g;
    const _ = (g = E.target.files) == null ? void 0 : g[0];
    _ && h(_);
  }, [h]), f = e.useCallback(() => {
    var E;
    r !== "uploading" && ((E = u.current) == null || E.click());
  }, [r]), b = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!0);
  }, []), v = e.useCallback((E) => {
    E.preventDefault(), E.stopPropagation(), s(!1);
  }, []), R = e.useCallback((E) => {
    var g;
    if (E.preventDefault(), E.stopPropagation(), s(!1), r === "uploading") return;
    const _ = (g = E.dataTransfer.files) == null ? void 0 : g[0];
    _ && h(_);
  }, [r, h]), w = m === "uploading", D = ae(Gt), O = m === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${c ? " tlFileUpload--dragover" : ""}`,
      onDragOver: b,
      onDragLeave: v,
      onDrop: R
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: p || void 0,
        onChange: S,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: w,
        title: O,
        "aria-label": O
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, qt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Zt = ({ controlId: l }) => {
  const t = q(), n = et(), r = le(), i = !!t.hasData, c = t.dataRevision ?? 0, s = t.fileName ?? "download", u = !!t.clearable, [a, o] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || a)) {
      o(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + c, b = await fetch(f);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const v = await b.blob(), R = URL.createObjectURL(v), w = document.createElement("a");
        w.href = R, w.download = s, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(R);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        o(!1);
      }
    }
  }, [i, a, n, c, s]), m = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = ae(qt);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const S = a ? h["js.downloading"] : h["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: a,
      title: S,
      "aria-label": S
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
}, Jt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [c, s] = e.useState(null), [u, a] = e.useState(!1), o = e.useRef(null), p = e.useRef(null), m = e.useRef(null), h = e.useRef(null), S = e.useRef(null), f = t.error, b = e.useMemo(
    () => {
      var N;
      return !!(window.isSecureContext && ((N = navigator.mediaDevices) != null && N.getUserMedia));
    },
    []
  ), v = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((N) => N.stop()), p.current = null), o.current && (o.current.srcObject = null);
  }, []), R = e.useCallback(() => {
    v(), i("idle");
  }, [v]), w = e.useCallback(async () => {
    var N;
    if (r !== "uploading") {
      if (s(null), !b) {
        (N = h.current) == null || N.click();
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
  }, [r, b]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const N = o.current, $ = m.current;
    if (!N || !$)
      return;
    $.width = N.videoWidth, $.height = N.videoHeight;
    const x = $.getContext("2d");
    x && (x.drawImage(N, 0, 0), v(), i("uploading"), $.toBlob(async (B) => {
      if (!B) {
        i("idle");
        return;
      }
      const Z = new FormData();
      Z.append("photo", B, "capture.jpg"), await n(Z), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, v]), O = e.useCallback(async (N) => {
    var B;
    const $ = (B = N.target.files) == null ? void 0 : B[0];
    if (!$) return;
    i("uploading");
    const x = new FormData();
    x.append("photo", $, $.name), await n(x), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && o.current && p.current && (o.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var $;
    if (r !== "overlayOpen") return;
    ($ = S.current) == null || $.focus();
    const N = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = N;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const N = ($) => {
      $.key === "Escape" && R();
    };
    return document.addEventListener("keydown", N), () => document.removeEventListener("keydown", N);
  }, [r, R]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((N) => N.stop()), p.current = null);
  }, []);
  const E = ae(Qt), _ = r === "uploading" ? E["js.uploading"] : E["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const W = ["tlPhotoCapture__overlayVideo"];
  u && W.push("tlPhotoCapture__overlayVideo--mirrored");
  const k = ["tlPhotoCapture__mirrorBtn"];
  return u && k.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: w,
      disabled: r === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !b && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: O
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: S,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: R }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: W.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: k.join(" "),
        onClick: () => a((N) => !N),
        title: E["js.photoCapture.mirror"],
        "aria-label": E["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: E["js.photoCapture.capture"],
        "aria-label": E["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: R,
        title: E["js.photoCapture.close"],
        "aria-label": E["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, E[c]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, en = {
  "js.photoViewer.alt": "Captured photo"
}, tn = ({ controlId: l }) => {
  const t = q(), n = et(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [c, s] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      c && (URL.revokeObjectURL(c), s(null));
      return;
    }
    if (i === u.current && c)
      return;
    u.current = i, c && (URL.revokeObjectURL(c), s(null));
    let o = !1;
    return (async () => {
      try {
        const p = await fetch(n);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const m = await p.blob();
        o || s(URL.createObjectURL(m));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      o = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    c && URL.revokeObjectURL(c);
  }, []);
  const a = ae(en);
  return !r || !c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: c,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, nn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.orientation, i = t.resizable === !0, c = t.children ?? [], s = r === "horizontal", u = c.length > 0 && c.every((v) => v.collapsed), a = !u && c.some((v) => v.collapsed), o = u ? !s : s, p = Fe(null), m = Fe(null), h = Fe(null), S = ot((v, R) => {
    const w = {
      overflow: v.scrolling || "auto"
    };
    return v.collapsed ? u && !o ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : R !== void 0 ? w.flex = `0 0 ${R}px` : w.flex = `${v.size} 1 0%`, v.minSize > 0 && !v.collapsed && (w.minWidth = s ? v.minSize : void 0, w.minHeight = s ? void 0 : v.minSize), w;
  }, [s, u, a, o]), f = ot((v, R) => {
    v.preventDefault();
    const w = p.current;
    if (!w) return;
    const D = c[R], O = c[R + 1], E = w.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    E.forEach((k) => {
      _.push(s ? k.offsetWidth : k.offsetHeight);
    }), h.current = _, m.current = {
      splitterIndex: R,
      startPos: s ? v.clientX : v.clientY,
      startSizeBefore: _[R],
      startSizeAfter: _[R + 1],
      childBefore: D,
      childAfter: O
    };
    const g = (k) => {
      const N = m.current;
      if (!N || !h.current) return;
      const x = (s ? k.clientX : k.clientY) - N.startPos, B = N.childBefore.minSize || 0, Z = N.childAfter.minSize || 0;
      let ne = N.startSizeBefore + x, z = N.startSizeAfter - x;
      ne < B && (z += ne - B, ne = B), z < Z && (ne += z - Z, z = Z), h.current[N.splitterIndex] = ne, h.current[N.splitterIndex + 1] = z;
      const J = w.querySelectorAll(":scope > .tlSplitPanel__child"), A = J[N.splitterIndex], P = J[N.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${ne}px`), P && (P.style.flex = `0 0 ${z}px`);
    }, W = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", W), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const k = {};
        c.forEach((N, $) => {
          const x = N.control;
          x != null && x.controlId && h.current && (k[x.controlId] = h.current[$]);
        }), n("updateSizes", { sizes: k });
      }
      h.current = null, m.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", W), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [c, s, n]), b = [];
  return c.forEach((v, R) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${R}`,
          className: `tlSplitPanel__child${v.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: S(v)
        },
        /* @__PURE__ */ e.createElement(G, { control: v.control })
      )
    ), i && R < c.length - 1) {
      const w = c[R + 1];
      !v.collapsed && !w.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${R}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (O) => f(O, R)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    b
  );
}, { useCallback: He } = e, ln = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), on = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), sn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), un = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(ln), i = t.title, c = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, u = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, p = t.toolbarButtons ?? [], m = c === "MINIMIZED", h = c === "MAXIMIZED", S = c === "HIDDEN", f = He(() => {
    n("toggleMinimize");
  }, [n]), b = He(() => {
    n("toggleMaximize");
  }, [n]), v = He(() => {
    n("popOut");
  }, [n]);
  if (S)
    return null;
  const R = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${c.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: R
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, p.map((w, D) => /* @__PURE__ */ e.createElement("span", { key: D, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(G, { control: w }))), s && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(on, null) : /* @__PURE__ */ e.createElement(rn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: h ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(sn, null) : /* @__PURE__ */ e.createElement(an, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(cn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(G, { control: t.child }))
  );
}, dn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(G, { control: t.child })
  );
}, mn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(G, { control: t.activeChild }));
}, { useCallback: de, useState: Be, useEffect: Oe, useRef: $e } = e, pn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function qe(l, t, n, r) {
  const i = [];
  for (const c of l)
    c.type === "nav" ? i.push({ id: c.id, type: "nav", groupId: r }) : c.type === "command" ? i.push({ id: c.id, type: "command", groupId: r }) : c.type === "group" && (i.push({ id: c.id, type: "group" }), (n.get(c.id) ?? c.expanded) && !t && i.push(...qe(c.children, t, n, c.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, fn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: c, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: c,
    onFocus: () => s(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), hn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => c(l.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), _n = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), bn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), vn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: c }) => {
  const s = $e(null);
  Oe(() => {
    const o = (p) => {
      s.current && !s.current.contains(p.target) && setTimeout(() => c(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [c]), Oe(() => {
    const o = (p) => {
      p.key === "Escape" && c();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [c]);
  const u = de((o) => {
    o.type === "nav" ? (r(o.id), c()) : o.type === "command" && (i(o.id), c());
  }, [r, i, c]), a = {};
  return n && (a.left = n.right, a.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" || o.type === "command") {
      const p = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (p ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(o)
        },
        /* @__PURE__ */ e.createElement(Ne, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, En = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: c,
  onToggleGroup: s,
  tabIndex: u,
  itemRef: a,
  onFocus: o,
  focusedId: p,
  setItemRef: m,
  onItemFocus: h,
  flyoutGroupId: S,
  onOpenFlyout: f,
  onCloseFlyout: b
}) => {
  const v = $e(null), [R, w] = Be(null), D = de(() => {
    r ? S === l.id ? b() : (v.current && w(v.current.getBoundingClientRect()), f(l.id)) : s(l.id);
  }, [r, S, l.id, s, f, b]), O = de((_) => {
    v.current = _, a(_);
  }, [a]), E = r && S === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (E ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: r ? l.label : void 0,
      "aria-expanded": r ? E : t,
      tabIndex: u,
      ref: O,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
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
  ), E && /* @__PURE__ */ e.createElement(
    vn,
    {
      item: l,
      activeItemId: n,
      anchorRect: R,
      onSelect: i,
      onExecute: c,
      onClose: b
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    bt,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: c,
      onToggleGroup: s,
      focusedId: p,
      setItemRef: m,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: S,
      onOpenFlyout: f,
      onCloseFlyout: b
    }
  ))));
}, bt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: c,
  focusedId: s,
  setItemRef: u,
  onItemFocus: a,
  groupStates: o,
  flyoutGroupId: p,
  onOpenFlyout: m,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        fn,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        hn,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(_n, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(bn, null);
    case "group": {
      const S = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        En,
        {
          item: l,
          expanded: S,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: c,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: a,
          focusedId: s,
          setItemRef: u,
          onItemFocus: a,
          flyoutGroupId: p,
          onOpenFlyout: m,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, gn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(pn), i = t.items ?? [], c = t.activeItemId, s = t.collapsed, [u, a] = Be(() => {
    const k = /* @__PURE__ */ new Map(), N = ($) => {
      for (const x of $)
        x.type === "group" && (k.set(x.id, x.expanded), N(x.children));
    };
    return N(i), k;
  }), o = de((k) => {
    a((N) => {
      const $ = new Map(N), x = $.get(k) ?? !1;
      return $.set(k, !x), n("toggleGroup", { itemId: k, expanded: !x }), $;
    });
  }, [n]), p = de((k) => {
    k !== c && n("selectItem", { itemId: k });
  }, [n, c]), m = de((k) => {
    n("executeCommand", { itemId: k });
  }, [n]), h = de(() => {
    n("toggleCollapse", {});
  }, [n]), [S, f] = Be(null), b = de((k) => {
    f(k);
  }, []), v = de(() => {
    f(null);
  }, []);
  Oe(() => {
    s || f(null);
  }, [s]);
  const [R, w] = Be(() => {
    const k = qe(i, s, u);
    return k.length > 0 ? k[0].id : "";
  }), D = $e(/* @__PURE__ */ new Map()), O = de((k) => (N) => {
    N ? D.current.set(k, N) : D.current.delete(k);
  }, []), E = de((k) => {
    w(k);
  }, []), _ = $e(0), g = de((k) => {
    w(k), _.current++;
  }, []);
  Oe(() => {
    const k = D.current.get(R);
    k && document.activeElement !== k && k.focus();
  }, [R, _.current]);
  const W = de((k) => {
    if (k.key === "Escape" && S !== null) {
      k.preventDefault(), v();
      return;
    }
    const N = qe(i, s, u);
    if (N.length === 0) return;
    const $ = N.findIndex((B) => B.id === R);
    if ($ < 0) return;
    const x = N[$];
    switch (k.key) {
      case "ArrowDown": {
        k.preventDefault();
        const B = ($ + 1) % N.length;
        g(N[B].id);
        break;
      }
      case "ArrowUp": {
        k.preventDefault();
        const B = ($ - 1 + N.length) % N.length;
        g(N[B].id);
        break;
      }
      case "Home": {
        k.preventDefault(), g(N[0].id);
        break;
      }
      case "End": {
        k.preventDefault(), g(N[N.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        k.preventDefault(), x.type === "nav" ? p(x.id) : x.type === "command" ? m(x.id) : x.type === "group" && (s ? S === x.id ? v() : b(x.id) : o(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !s && ((u.get(x.id) ?? !1) || (k.preventDefault(), o(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !s && (u.get(x.id) ?? !1) && (k.preventDefault(), o(x.id));
        break;
      }
    }
  }, [
    i,
    s,
    u,
    R,
    S,
    g,
    p,
    m,
    o,
    b,
    v
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: W }, i.map((k) => /* @__PURE__ */ e.createElement(
    bt,
    {
      key: k.id,
      item: k,
      activeItemId: c,
      collapsed: s,
      onSelect: p,
      onExecute: m,
      onToggleGroup: o,
      focusedId: R,
      setItemRef: O,
      onItemFocus: E,
      groupStates: u,
      flyoutGroupId: S,
      onOpenFlyout: b,
      onCloseFlyout: v
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(G, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(G, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: s ? r["js.sidebar.expand"] : r["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(G, { control: t.activeContent })));
}, Cn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", c = t.wrap === !0, s = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    c ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, s.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })));
}, yn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", c = t.children ?? [], s = {};
  return r ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (s.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: s }, c.map((u, a) => /* @__PURE__ */ e.createElement(G, { key: a, control: u })));
}, wn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", c = t.headerActions ?? [], s = t.child, u = n != null || c.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, c.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(G, { control: s })));
}, kn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.actions ?? [], c = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: u }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(G, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, i.map((a, o) => /* @__PURE__ */ e.createElement(G, { key: o, control: a }))));
}, { useCallback: Sn } = e, Nn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = Sn((c) => {
    n("navigate", { itemId: c });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((c, s) => {
    const u = s === r.length - 1;
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
}, { useCallback: Tn } = e, Rn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = t.activeItemId, c = Tn((s) => {
    s !== i && n("selectItem", { itemId: s });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((s) => {
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
}, { useCallback: at, useEffect: st, useRef: xn } = e, Ln = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.closeOnBackdrop !== !1, c = t.child, s = xn(null), u = at(() => {
    n("close");
  }, [n]), a = at((o) => {
    i && o.target === o.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!r) return;
    const o = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [r, u]), st(() => {
    r && s.current && s.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(G, { control: c })
  ) : null;
}, { useEffect: Dn, useRef: In } = e, Mn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = In(n.length);
  return Dn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(G, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, Pn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, jn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], An = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(Pn), i = t.title ?? "", c = t.width ?? "32rem", s = t.height ?? null, u = t.minHeight ?? null, a = t.resizable === !0, o = t.child, p = t.actions ?? [], m = t.toolbarButtons ?? [], [h, S] = Ie(null), [f, b] = Ie(null), [v, R] = Ie(null), w = Ce(null), [D, O] = Ie(!1), E = Ce(null), _ = Ce(null), g = Ce(null), W = Ce(null), k = Ce(null), N = De(() => {
    n("close");
  }, [n]), $ = De((z, J) => {
    J.preventDefault();
    const A = W.current;
    if (!A) return;
    const P = A.getBoundingClientRect(), Q = !w.current, d = w.current ?? { x: P.left, y: P.top };
    Q && (w.current = d, R(d)), k.current = {
      dir: z,
      startX: J.clientX,
      startY: J.clientY,
      startW: P.width,
      startH: P.height,
      startPos: { ...d },
      symmetric: Q
    };
    const y = (j) => {
      const I = k.current;
      if (!I) return;
      const K = j.clientX - I.startX, Y = j.clientY - I.startY;
      let te = I.startW, ee = I.startH, ie = 0, ue = 0;
      I.symmetric ? (I.dir.includes("e") && (te = I.startW + 2 * K), I.dir.includes("w") && (te = I.startW - 2 * K), I.dir.includes("s") && (ee = I.startH + 2 * Y), I.dir.includes("n") && (ee = I.startH - 2 * Y)) : (I.dir.includes("e") && (te = I.startW + K), I.dir.includes("w") && (te = I.startW - K, ie = K), I.dir.includes("s") && (ee = I.startH + Y), I.dir.includes("n") && (ee = I.startH - Y, ue = Y));
      const he = Math.max(200, te), fe = Math.max(100, ee);
      I.symmetric ? (ie = (I.startW - he) / 2, ue = (I.startH - fe) / 2) : (I.dir.includes("w") && he === 200 && (ie = I.startW - 200), I.dir.includes("n") && fe === 100 && (ue = I.startH - 100)), _.current = he, g.current = fe, S(he), b(fe);
      const ve = {
        x: I.startPos.x + ie,
        y: I.startPos.y + ue
      };
      w.current = ve, R(ve);
    }, F = () => {
      document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", F);
      const j = _.current, I = g.current;
      (j != null || I != null) && n("resize", {
        ...j != null ? { width: Math.round(j) + "px" } : {},
        ...I != null ? { height: Math.round(I) + "px" } : {}
      }), k.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", F);
  }, [n]), x = De((z) => {
    if (z.button !== 0 || z.target.closest("button")) return;
    z.preventDefault();
    const J = W.current;
    if (!J) return;
    const A = J.getBoundingClientRect(), P = w.current ?? { x: A.left, y: A.top }, Q = z.clientX - P.x, d = z.clientY - P.y, y = (j) => {
      const I = window.innerWidth, K = window.innerHeight;
      let Y = j.clientX - Q, te = j.clientY - d;
      const ee = J.offsetWidth, ie = J.offsetHeight;
      Y + ee > I && (Y = I - ee), te + ie > K && (te = K - ie), Y < 0 && (Y = 0), te < 0 && (te = 0);
      const ue = { x: Y, y: te };
      w.current = ue, R(ue);
    }, F = () => {
      document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", F);
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", F);
  }, []), B = De(() => {
    var z, J;
    if (D) {
      const A = E.current;
      A && (R(A.x !== -1 ? { x: A.x, y: A.y } : null), S(A.w), b(A.h)), O(!1);
    } else {
      const A = W.current, P = A == null ? void 0 : A.getBoundingClientRect();
      E.current = {
        x: ((z = w.current) == null ? void 0 : z.x) ?? (P == null ? void 0 : P.left) ?? -1,
        y: ((J = w.current) == null ? void 0 : J.y) ?? (P == null ? void 0 : P.top) ?? -1,
        w: h ?? (P == null ? void 0 : P.width) ?? null,
        h: f ?? null
      }, O(!0), R({ x: 0, y: 0 }), S(null), b(null);
    }
  }, [D, h, f]), Z = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : c,
    ...f != null ? { height: f + "px" } : s != null ? { height: s } : {},
    ...u != null && f == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
  }, ne = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: Z,
      ref: W,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": ne
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : x,
        onDoubleClick: B
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: ne }, i),
      m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((z, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(G, { control: z })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: B,
          title: D ? r["js.window.restore"] : r["js.window.maximize"]
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
          onClick: N,
          title: r["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(G, { control: o })),
    p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p.map((z, J) => /* @__PURE__ */ e.createElement(G, { key: J, control: z }))),
    a && !D && jn.map((z) => /* @__PURE__ */ e.createElement(
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
}, Fn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae($n), i = t.open === !0, c = t.position ?? "right", s = t.size ?? "medium", u = t.title ?? null, a = t.child, o = Bn(() => {
    n("close");
  }, [n]);
  On(() => {
    if (!i) return;
    const m = (h) => {
      h.key === "Escape" && o();
    };
    return document.addEventListener("keydown", m), () => document.removeEventListener("keydown", m);
  }, [i, o]);
  const p = [
    "tlDrawer",
    `tlDrawer--${c}`,
    `tlDrawer--${s}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: p, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(G, { control: a })));
}, { useCallback: Hn, useEffect: Wn, useState: zn } = e, Un = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.message ?? "", i = t.content ?? "", c = t.variant ?? "info", s = t.duration ?? 5e3, u = t.visible === !0, a = t.generation ?? 0, [o, p] = zn(!1), m = Hn(() => {
    p(!0), setTimeout(() => {
      n("dismiss", { generation: a }), p(!1);
    }, 200);
  }, [n, a]);
  return Wn(() => {
    if (!u || s === 0) return;
    const h = setTimeout(m, s);
    return () => clearTimeout(h);
  }, [u, s, m]), !u && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${c}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: We, useEffect: ze, useRef: Vn, useState: ct } = e, Kn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.anchorId, c = t.items ?? [], s = Vn(null), [u, a] = ct({ top: 0, left: 0 }), [o, p] = ct(0), m = c.filter((b) => b.type === "item" && !b.disabled);
  ze(() => {
    var E, _;
    if (!r || !i) return;
    const b = document.getElementById(i);
    if (!b) return;
    const v = b.getBoundingClientRect(), R = ((E = s.current) == null ? void 0 : E.offsetHeight) ?? 200, w = ((_ = s.current) == null ? void 0 : _.offsetWidth) ?? 200;
    let D = v.bottom + 4, O = v.left;
    D + R > window.innerHeight && (D = v.top - R - 4), O + w > window.innerWidth && (O = v.right - w), a({ top: D, left: O }), p(0);
  }, [r, i]);
  const h = We(() => {
    n("close");
  }, [n]), S = We((b) => {
    n("selectItem", { itemId: b });
  }, [n]);
  ze(() => {
    if (!r) return;
    const b = (v) => {
      s.current && !s.current.contains(v.target) && h();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [r, h]);
  const f = We((b) => {
    if (b.key === "Escape") {
      h();
      return;
    }
    if (b.key === "ArrowDown")
      b.preventDefault(), p((v) => (v + 1) % m.length);
    else if (b.key === "ArrowUp")
      b.preventDefault(), p((v) => (v - 1 + m.length) % m.length);
    else if (b.key === "Enter" || b.key === " ") {
      b.preventDefault();
      const v = m[o];
      v && S(v.id);
    }
  }, [h, S, m, o]);
  return ze(() => {
    r && s.current && s.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: u.top, left: u.left },
      onKeyDown: f
    },
    c.map((b, v) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: v, className: "tlMenu__separator" });
      const w = m.indexOf(b) === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (w ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: w ? 0 : -1,
          onClick: () => S(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, Yn = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, c = t.snackbar, s = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(G, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(G, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(G, { control: i })), /* @__PURE__ */ e.createElement(G, { control: c }), s && /* @__PURE__ */ e.createElement(G, { control: s }));
}, Gn = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, c = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: c,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, Xn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, qn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(Xn), i = e.useRef(null);
  e.useEffect(() => {
    const T = i.current;
    if (!T) return;
    const M = (C) => {
      const L = C.detail;
      let H = L.target;
      for (; H && H !== T; ) {
        const X = H.dataset.row, re = H.dataset.col;
        if (X != null && re != null) {
          L.resolved = { key: X + "|" + re };
          return;
        }
        H = H.parentElement;
      }
    };
    return T.addEventListener("tl-tooltip-resolve", M), () => T.removeEventListener("tl-tooltip-resolve", M);
  }, []);
  const c = t.columns ?? [], s = t.totalRowCount ?? 0, u = t.rows ?? [], a = t.rowHeight ?? 36, o = t.selectionMode ?? "single", p = t.selectedCount ?? 0, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, S = e.useMemo(
    () => c.filter((T) => T.sortPriority && T.sortPriority > 0).length,
    [c]
  ), f = o === "multi", b = 40, v = 20, R = e.useRef(null), w = e.useRef(null), D = e.useRef(null), [O, E] = e.useState({}), _ = e.useRef(null), g = e.useRef(!1), W = e.useRef(null), [k, N] = e.useState(null), [$, x] = e.useState(null);
  e.useEffect(() => {
    _.current || E({});
  }, [c]);
  const B = e.useCallback((T) => O[T.name] ?? T.width, [O]), Z = e.useMemo(() => {
    const T = [];
    let M = f && m > 0 ? b : 0;
    for (let C = 0; C < m && C < c.length; C++)
      T.push(M), M += B(c[C]);
    return T;
  }, [c, m, f, b, B]), ne = s * a, z = e.useRef(null), J = e.useCallback((T, M, C) => {
    C.preventDefault(), C.stopPropagation(), _.current = { column: T, startX: C.clientX, startWidth: M };
    let L = C.clientX, H = 0;
    const X = () => {
      const oe = _.current;
      if (!oe) return;
      const me = Math.max(it, oe.startWidth + (L - oe.startX) + H);
      E((ge) => ({ ...ge, [oe.column]: me }));
    }, re = () => {
      const oe = w.current, me = R.current;
      if (!oe || !_.current) return;
      const ge = oe.getBoundingClientRect(), nt = 40, lt = 8, wt = oe.scrollLeft;
      L > ge.right - nt ? oe.scrollLeft += lt : L < ge.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - wt;
      rt !== 0 && (me && (me.scrollLeft = oe.scrollLeft), H += rt, X()), z.current = requestAnimationFrame(re);
    };
    z.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      L = oe.clientX, X();
    }, Le = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", Le), z.current !== null && (cancelAnimationFrame(z.current), z.current = null);
      const me = _.current;
      if (me) {
        const ge = Math.max(it, me.startWidth + (oe.clientX - me.startX) + H);
        n("columnResize", { column: me.column, width: ge }), _.current = null, g.current = !0, requestAnimationFrame(() => {
          g.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", Le);
  }, [n]), A = e.useCallback(() => {
    R.current && w.current && (R.current.scrollLeft = w.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const T = w.current;
      if (!T) return;
      const M = T.scrollTop, C = Math.ceil(T.clientHeight / a), L = Math.floor(M / a);
      n("scroll", { start: L, count: C });
    }, 80);
  }, [n, a]), P = e.useCallback((T, M, C) => {
    if (g.current) return;
    let L;
    !M || M === "desc" ? L = "asc" : L = "desc";
    const H = C.shiftKey ? "add" : "replace";
    n("sort", { column: T, direction: L, mode: H });
  }, [n]), Q = e.useCallback((T, M) => {
    W.current = T, M.dataTransfer.effectAllowed = "move", M.dataTransfer.setData("text/plain", T);
  }, []), d = e.useCallback((T, M) => {
    if (!W.current || W.current === T) {
      N(null);
      return;
    }
    M.preventDefault(), M.dataTransfer.dropEffect = "move";
    const C = M.currentTarget.getBoundingClientRect(), L = M.clientX < C.left + C.width / 2 ? "left" : "right";
    N({ column: T, side: L });
  }, []), y = e.useCallback((T) => {
    T.preventDefault(), T.stopPropagation();
    const M = W.current;
    if (!M || !k) {
      W.current = null, N(null);
      return;
    }
    let C = c.findIndex((H) => H.name === k.column);
    if (C < 0) {
      W.current = null, N(null);
      return;
    }
    const L = c.findIndex((H) => H.name === M);
    k.side === "right" && C++, L < C && C--, n("columnReorder", { column: M, targetIndex: C }), W.current = null, N(null);
  }, [c, k, n]), F = e.useCallback(() => {
    W.current = null, N(null);
  }, []), j = e.useCallback((T, M) => {
    M.shiftKey && M.preventDefault(), n("select", {
      rowIndex: T,
      ctrlKey: M.ctrlKey || M.metaKey,
      shiftKey: M.shiftKey
    });
  }, [n]), I = e.useCallback((T, M) => {
    M.stopPropagation(), n("select", { rowIndex: T, ctrlKey: !0, shiftKey: !1 });
  }, [n]), K = e.useCallback(() => {
    const T = p === s && s > 0;
    n("selectAll", { selected: !T });
  }, [n, p, s]), Y = e.useCallback((T, M, C) => {
    C.stopPropagation(), n("expand", { rowIndex: T, expanded: M });
  }, [n]), te = e.useCallback((T, M) => {
    M.preventDefault(), x({ x: M.clientX, y: M.clientY, colIdx: T });
  }, []), ee = e.useCallback(() => {
    $ && (n("setFrozenColumnCount", { count: $.colIdx + 1 }), x(null));
  }, [$, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), x(null);
  }, [n]);
  e.useEffect(() => {
    if (!$) return;
    const T = () => x(null), M = (C) => {
      C.key === "Escape" && x(null);
    };
    return document.addEventListener("mousedown", T), document.addEventListener("keydown", M), () => {
      document.removeEventListener("mousedown", T), document.removeEventListener("keydown", M);
    };
  }, [$]);
  const ue = c.reduce((T, M) => T + B(M), 0) + (f ? b : 0), he = p === s && s > 0, fe = p > 0 && p < s, ve = e.useCallback((T) => {
    T && (T.indeterminate = fe);
  }, [fe]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (T) => {
        if (!W.current) return;
        T.preventDefault();
        const M = w.current, C = R.current;
        if (!M) return;
        const L = M.getBoundingClientRect(), H = 40, X = 8;
        T.clientX < L.left + H ? M.scrollLeft = Math.max(0, M.scrollLeft - X) : T.clientX > L.right - H && (M.scrollLeft += X), C && (C.scrollLeft = M.scrollLeft);
      },
      onDrop: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: R }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ue } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: b,
          minWidth: b,
          ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (T) => {
          W.current && (T.preventDefault(), T.dataTransfer.dropEffect = "move", c.length > 0 && c[0].name !== W.current && N({ column: c[0].name, side: "left" }));
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
    ), c.map((T, M) => {
      const C = B(T);
      c.length - 1;
      let L = "tlTableView__headerCell";
      T.sortable && (L += " tlTableView__headerCell--sortable"), k && k.column === T.name && (L += " tlTableView__headerCell--dragOver-" + k.side);
      const H = M < m, X = M === m - 1;
      return H && (L += " tlTableView__headerCell--frozen"), X && (L += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.name,
          className: L,
          style: {
            width: C,
            minWidth: C,
            position: H ? "sticky" : "relative",
            ...H ? { left: Z[M], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: T.sortable ? (re) => P(T.name, T.sortDirection, re) : void 0,
          onContextMenu: (re) => te(M, re),
          onDragStart: (re) => Q(T.name, re),
          onDragOver: (re) => d(T.name, re),
          onDrop: y,
          onDragEnd: F
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, T.label),
        T.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, T.sortDirection === "asc" ? "▲" : "▼", S > 1 && T.sortPriority != null && T.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, T.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => J(T.name, C, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (T) => {
          if (W.current && c.length > 0) {
            const M = c[c.length - 1];
            M.name !== W.current && (T.preventDefault(), T.dataTransfer.dropEffect = "move", N({ column: M.name, side: "right" }));
          }
        },
        onDrop: y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: w,
        className: "tlTableView__body",
        onScroll: A
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ne, position: "relative", width: ue } }, u.map((T) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.id,
          className: "tlTableView__row" + (T.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: T.index * a,
            height: a,
            width: ue
          },
          onClick: (M) => j(T.index, M)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: b,
              minWidth: b,
              ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (M) => M.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: T.selected,
              onChange: () => {
              },
              onClick: (M) => I(T.index, M),
              tabIndex: -1
            }
          )
        ),
        c.map((M, C) => {
          const L = B(M), H = C === c.length - 1, X = C < m, re = C === m - 1;
          let Ee = "tlTableView__cell";
          X && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const Le = h && C === 0, oe = T.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: M.name,
              className: Ee,
              "data-row": T.id,
              "data-col": M.name,
              style: {
                ...H && !X ? { flex: "1 0 auto", minWidth: L } : { width: L, minWidth: L },
                ...X ? { position: "sticky", left: Z[C], zIndex: 2 } : {}
              }
            },
            Le ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * v } }, T.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (me) => Y(T.index, !T.expanded, me)
              },
              T.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(G, { control: T.cells[M.name] })) : /* @__PURE__ */ e.createElement(G, { control: T.cells[M.name] })
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
        onMouseDown: (T) => T.stopPropagation()
      },
      $.colIdx + 1 !== m && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      m > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Zn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, vt = e.createContext(Zn), { useMemo: Qn, useRef: Jn, useState: el, useEffect: tl } = e, nl = 320, ll = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, c = t.children ?? [], s = t.noModelMessage, u = Jn(null), [a, o] = el(
    r === "top" ? "top" : "side"
  );
  tl(() => {
    if (r !== "auto") {
      o(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const b = new ResizeObserver((v) => {
      for (const R of v) {
        const D = R.contentRect.width / n;
        o(D < nl ? "top" : "side");
      }
    });
    return b.observe(f), () => b.disconnect();
  }, [r, n]);
  const p = Qn(() => ({
    readOnly: i,
    resolvedLabelPosition: a
  }), [i, a]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, S = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(vt.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: h, ref: u }, c.map((f, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: f }))));
}, { useCallback: rl } = e, ol = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, al = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(ol), i = t.headerControl ?? null, c = t.headerActions ?? [], s = t.collapsible === !0, u = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, p = t.children ?? [], m = i != null || c.length > 0 || s, h = rl(() => {
    n("toggleCollapse");
  }, [n]), S = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: S }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
      "aria-expanded": !u,
      title: u ? r["js.formGroup.expand"] : r["js.formGroup.collapse"]
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(G, { control: i })), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, c.map((f, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((f, b) => /* @__PURE__ */ e.createElement(G, { key: b, control: f }))));
}, { useContext: sl, useState: cl, useCallback: il } = e, ul = ({ controlId: l }) => {
  const t = q(), n = sl(vt), r = t.label ?? "", i = t.required === !0, c = t.error, s = t.warnings, u = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, m = t.visible !== !1, h = t.hasTooltip === !0, S = t.field, f = n.readOnly, [b, v] = cl(!1), R = il(() => v((E) => !E), []);
  if (!m) return null;
  const w = c != null, D = s != null && s.length > 0, O = [
    "tlFormField",
    `tlFormField--${o}`,
    f ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    w ? "tlFormField--error" : "",
    !w && D ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: O }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    r
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: R,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(G, { control: S })), !f && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, c)), !f && !w && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, s.map((E, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, E)))), !f && u && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, dl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.iconCss, i = t.iconSrc, c = t.label, s = t.cssClass, u = t.hasTooltip === !0, a = t.hasLink, o = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, p = /* @__PURE__ */ e.createElement(e.Fragment, null, o, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", s].filter(Boolean).join(" "), S = u ? "key:tooltip" : void 0;
  return a ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: m,
      "data-tooltip": S
    },
    p
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": S }, p);
}, ml = 20, pl = () => {
  const l = q(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, c = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), p = e.useRef(null), m = e.useCallback((E, _) => {
    t(_ ? "collapse" : "expand", { nodeId: E });
  }, [t]), h = e.useCallback((E, _) => {
    t("select", {
      nodeId: E,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), S = e.useCallback((E, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: E, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), b = e.useCallback((E, _) => {
    const g = _.getBoundingClientRect(), W = E.clientY - g.top, k = g.height / 3;
    return W < k ? "above" : W > k * 2 ? "below" : "within";
  }, []), v = e.useCallback((E, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", E);
  }, []), R = e.useCallback((E, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const g = b(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: E, position: g }), f.current = null;
    }, 50);
  }, [t, b]), w = e.useCallback((E, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const g = b(_, _.currentTarget);
    t("drop", { nodeId: E, position: g });
  }, [t, b]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), O = e.useCallback((E) => {
    if (n.length === 0) return;
    let _ = a;
    switch (E.key) {
      case "ArrowDown":
        E.preventDefault(), _ = Math.min(a + 1, n.length - 1);
        break;
      case "ArrowUp":
        E.preventDefault(), _ = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (E.preventDefault(), a >= 0 && a < n.length) {
          const g = n[a];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (_ = a + 1);
        }
        break;
      case "ArrowLeft":
        if (E.preventDefault(), a >= 0 && a < n.length) {
          const g = n[a];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const W = g.depth;
            for (let k = a - 1; k >= 0; k--)
              if (n[k].depth < W) {
                _ = k;
                break;
              }
          }
        }
        break;
      case "Enter":
        E.preventDefault(), a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: E.ctrlKey || E.metaKey,
          shiftKey: E.shiftKey
        });
        return;
      case " ":
        E.preventDefault(), r === "multi" && a >= 0 && a < n.length && t("select", {
          nodeId: n[a].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        E.preventDefault(), _ = 0;
        break;
      case "End":
        E.preventDefault(), _ = n.length - 1;
        break;
      default:
        return;
    }
    _ !== a && o(_);
  }, [a, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: O
    },
    n.map((E, _) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: E.id,
        role: "treeitem",
        "aria-expanded": E.expandable ? E.expanded : void 0,
        "aria-selected": E.selected,
        "aria-level": E.depth + 1,
        className: [
          "tlTreeView__node",
          E.selected ? "tlTreeView__node--selected" : "",
          _ === a ? "tlTreeView__node--focused" : "",
          s === E.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          s === E.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          s === E.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: E.depth * ml },
        draggable: i,
        onClick: (g) => h(E.id, g),
        onContextMenu: (g) => S(E.id, g),
        onDragStart: (g) => v(E.id, g),
        onDragOver: c ? (g) => R(E.id, g) : void 0,
        onDrop: c ? (g) => w(E.id, g) : void 0,
        onDragEnd: D
      },
      E.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), m(E.id, E.expanded);
          },
          tabIndex: -1,
          "aria-label": E.expanded ? "Collapse" : "Expand"
        },
        E.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: E.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(G, { control: E.content }))
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
function fl() {
  if (ut) return V;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), c = Symbol.for("react.consumer"), s = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), h = Symbol.iterator;
  function S(d) {
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
  }, b = Object.assign, v = {};
  function R(d, y, F) {
    this.props = d, this.context = y, this.refs = v, this.updater = F || f;
  }
  R.prototype.isReactComponent = {}, R.prototype.setState = function(d, y) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, y, "setState");
  }, R.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function w() {
  }
  w.prototype = R.prototype;
  function D(d, y, F) {
    this.props = d, this.context = y, this.refs = v, this.updater = F || f;
  }
  var O = D.prototype = new w();
  O.constructor = D, b(O, R.prototype), O.isPureReactComponent = !0;
  var E = Array.isArray;
  function _() {
  }
  var g = { H: null, A: null, T: null, S: null }, W = Object.prototype.hasOwnProperty;
  function k(d, y, F) {
    var j = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: y,
      ref: j !== void 0 ? j : null,
      props: F
    };
  }
  function N(d, y) {
    return k(d.type, y, d.props);
  }
  function $(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function x(d) {
    var y = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return y[F];
    });
  }
  var B = /\/+/g;
  function Z(d, y) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : y.toString(36);
  }
  function ne(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
          function(y) {
            d.status === "pending" && (d.status = "fulfilled", d.value = y);
          },
          function(y) {
            d.status === "pending" && (d.status = "rejected", d.reason = y);
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
  function z(d, y, F, j, I) {
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
            case l:
            case t:
              Y = !0;
              break;
            case p:
              return Y = d._init, z(
                Y(d._payload),
                y,
                F,
                j,
                I
              );
          }
      }
    if (Y)
      return I = I(d), Y = j === "" ? "." + Z(d, 0) : j, E(I) ? (F = "", Y != null && (F = Y.replace(B, "$&/") + "/"), z(I, y, F, "", function(ie) {
        return ie;
      })) : I != null && ($(I) && (I = N(
        I,
        F + (I.key == null || d && d.key === I.key ? "" : ("" + I.key).replace(
          B,
          "$&/"
        ) + "/") + Y
      )), y.push(I)), 1;
    Y = 0;
    var te = j === "" ? "." : j + ":";
    if (E(d))
      for (var ee = 0; ee < d.length; ee++)
        j = d[ee], K = te + Z(j, ee), Y += z(
          j,
          y,
          F,
          K,
          I
        );
    else if (ee = S(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(j = d.next()).done; )
        j = j.value, K = te + Z(j, ee++), Y += z(
          j,
          y,
          F,
          K,
          I
        );
    else if (K === "object") {
      if (typeof d.then == "function")
        return z(
          ne(d),
          y,
          F,
          j,
          I
        );
      throw y = String(d), Error(
        "Objects are not valid as a React child (found: " + (y === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : y) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Y;
  }
  function J(d, y, F) {
    if (d == null) return d;
    var j = [], I = 0;
    return z(d, j, "", "", function(K) {
      return y.call(F, K, I++);
    }), j;
  }
  function A(d) {
    if (d._status === -1) {
      var y = d._result;
      y = y(), y.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = y);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var P = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var y = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(y)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Q = {
    map: J,
    forEach: function(d, y, F) {
      J(
        d,
        function() {
          y.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var y = 0;
      return J(d, function() {
        y++;
      }), y;
    },
    toArray: function(d) {
      return J(d, function(y) {
        return y;
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
  return V.Activity = m, V.Children = Q, V.Component = R, V.Fragment = n, V.Profiler = i, V.PureComponent = D, V.StrictMode = r, V.Suspense = a, V.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, V.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return g.H.useMemoCache(d);
    }
  }, V.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, V.cacheSignal = function() {
    return null;
  }, V.cloneElement = function(d, y, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var j = b({}, d.props), I = d.key;
    if (y != null)
      for (K in y.key !== void 0 && (I = "" + y.key), y)
        !W.call(y, K) || K === "key" || K === "__self" || K === "__source" || K === "ref" && y.ref === void 0 || (j[K] = y[K]);
    var K = arguments.length - 2;
    if (K === 1) j.children = F;
    else if (1 < K) {
      for (var Y = Array(K), te = 0; te < K; te++)
        Y[te] = arguments[te + 2];
      j.children = Y;
    }
    return k(d.type, I, j);
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
  }, V.createElement = function(d, y, F) {
    var j, I = {}, K = null;
    if (y != null)
      for (j in y.key !== void 0 && (K = "" + y.key), y)
        W.call(y, j) && j !== "key" && j !== "__self" && j !== "__source" && (I[j] = y[j]);
    var Y = arguments.length - 2;
    if (Y === 1) I.children = F;
    else if (1 < Y) {
      for (var te = Array(Y), ee = 0; ee < Y; ee++)
        te[ee] = arguments[ee + 2];
      I.children = te;
    }
    if (d && d.defaultProps)
      for (j in Y = d.defaultProps, Y)
        I[j] === void 0 && (I[j] = Y[j]);
    return k(d, K, I);
  }, V.createRef = function() {
    return { current: null };
  }, V.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, V.isValidElement = $, V.lazy = function(d) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, V.memo = function(d, y) {
    return {
      $$typeof: o,
      type: d,
      compare: y === void 0 ? null : y
    };
  }, V.startTransition = function(d) {
    var y = g.T, F = {};
    g.T = F;
    try {
      var j = d(), I = g.S;
      I !== null && I(F, j), typeof j == "object" && j !== null && typeof j.then == "function" && j.then(_, P);
    } catch (K) {
      P(K);
    } finally {
      y !== null && F.types !== null && (y.types = F.types), g.T = y;
    }
  }, V.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, V.use = function(d) {
    return g.H.use(d);
  }, V.useActionState = function(d, y, F) {
    return g.H.useActionState(d, y, F);
  }, V.useCallback = function(d, y) {
    return g.H.useCallback(d, y);
  }, V.useContext = function(d) {
    return g.H.useContext(d);
  }, V.useDebugValue = function() {
  }, V.useDeferredValue = function(d, y) {
    return g.H.useDeferredValue(d, y);
  }, V.useEffect = function(d, y) {
    return g.H.useEffect(d, y);
  }, V.useEffectEvent = function(d) {
    return g.H.useEffectEvent(d);
  }, V.useId = function() {
    return g.H.useId();
  }, V.useImperativeHandle = function(d, y, F) {
    return g.H.useImperativeHandle(d, y, F);
  }, V.useInsertionEffect = function(d, y) {
    return g.H.useInsertionEffect(d, y);
  }, V.useLayoutEffect = function(d, y) {
    return g.H.useLayoutEffect(d, y);
  }, V.useMemo = function(d, y) {
    return g.H.useMemo(d, y);
  }, V.useOptimistic = function(d, y) {
    return g.H.useOptimistic(d, y);
  }, V.useReducer = function(d, y, F) {
    return g.H.useReducer(d, y, F);
  }, V.useRef = function(d) {
    return g.H.useRef(d);
  }, V.useState = function(d) {
    return g.H.useState(d);
  }, V.useSyncExternalStore = function(d, y, F) {
    return g.H.useSyncExternalStore(
      d,
      y,
      F
    );
  }, V.useTransition = function() {
    return g.H.useTransition();
  }, V.version = "19.2.4", V;
}
var dt;
function hl() {
  return dt || (dt = 1, Ve.exports = fl()), Ve.exports;
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
function _l() {
  if (mt) return se;
  mt = 1;
  var l = hl();
  function t(a) {
    var o = "https://react.dev/errors/" + a;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var p = 2; p < arguments.length; p++)
        o += "&args[]=" + encodeURIComponent(arguments[p]);
    }
    return "Minified React error #" + a + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var r = {
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
  function c(a, o, p) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: m == null ? null : "" + m,
      children: a,
      containerInfo: o,
      implementation: p
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(a, o) {
    if (a === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(a, o) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return c(a, o, null, p);
  }, se.flushSync = function(a) {
    var o = s.T, p = r.p;
    try {
      if (s.T = null, r.p = 2, a) return a();
    } finally {
      s.T = o, r.p = p, r.d.f();
    }
  }, se.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, r.d.C(a, o));
  }, se.prefetchDNS = function(a) {
    typeof a == "string" && r.d.D(a);
  }, se.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var p = o.as, m = u(p, o.crossOrigin), h = typeof o.integrity == "string" ? o.integrity : void 0, S = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      p === "style" ? r.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: m,
          integrity: h,
          fetchPriority: S
        }
      ) : p === "script" && r.d.X(a, {
        crossOrigin: m,
        integrity: h,
        fetchPriority: S,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, se.preinitModule = function(a, o) {
    if (typeof a == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var p = u(
            o.as,
            o.crossOrigin
          );
          r.d.M(a, {
            crossOrigin: p,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && r.d.M(a);
  }, se.preload = function(a, o) {
    if (typeof a == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var p = o.as, m = u(p, o.crossOrigin);
      r.d.L(a, p, {
        crossOrigin: m,
        integrity: typeof o.integrity == "string" ? o.integrity : void 0,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0,
        type: typeof o.type == "string" ? o.type : void 0,
        fetchPriority: typeof o.fetchPriority == "string" ? o.fetchPriority : void 0,
        referrerPolicy: typeof o.referrerPolicy == "string" ? o.referrerPolicy : void 0,
        imageSrcSet: typeof o.imageSrcSet == "string" ? o.imageSrcSet : void 0,
        imageSizes: typeof o.imageSizes == "string" ? o.imageSizes : void 0,
        media: typeof o.media == "string" ? o.media : void 0
      });
    }
  }, se.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var p = u(o.as, o.crossOrigin);
        r.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: p,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else r.d.m(a);
  }, se.requestFormReset = function(a) {
    r.d.r(a);
  }, se.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, se.useFormState = function(a, o, p) {
    return s.H.useFormState(a, o, p);
  }, se.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function bl() {
  if (pt) return Ue.exports;
  pt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ue.exports = _l(), Ue.exports;
}
var vl = bl();
const { useState: _e, useCallback: ce, useRef: Te, useEffect: ye, useMemo: Ze } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function El({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: c,
  onDragOver: s,
  onDrop: u,
  onDragEnd: a,
  dragClassName: o
}) {
  const p = ce(
    (m) => {
      m.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: i || void 0,
      onDragStart: c,
      onDragOver: s,
      onDrop: u,
      onDragEnd: a
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: p,
        "aria-label": r
      },
      "×"
    )
  );
}
function gl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: c
}) {
  const s = ce(() => r(l.value), [r, l.value]), u = Ze(() => {
    if (!n) return l.label;
    const a = l.label.toLowerCase().indexOf(n.toLowerCase());
    return a < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, a), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(a, a + n.length)), l.label.substring(a + n.length));
  }, [l.label, n]);
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
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Cl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], i = t.multiSelect === !0, c = t.customOrder === !0, s = t.mandatory === !0, u = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, p = t.options ?? [], m = t.emptyOptionLabel ?? "", h = c && i && !u && a, S = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = S["js.dropdownSelect.nothingFound"], b = ce(
    (C) => S["js.dropdownSelect.removeChip"].replace("{0}", C),
    [S]
  ), [v, R] = _e(!1), [w, D] = _e(""), [O, E] = _e(-1), [_, g] = _e(!1), [W, k] = _e({}), [N, $] = _e(null), [x, B] = _e(null), [Z, ne] = _e(null), z = Te(null), J = Te(null), A = Te(null), P = Te(r);
  P.current = r;
  const Q = Te(-1), d = Ze(
    () => new Set(r.map((C) => C.value)),
    [r]
  ), y = Ze(() => {
    let C = p.filter((L) => !d.has(L.value));
    if (w) {
      const L = w.toLowerCase();
      C = C.filter((H) => H.label.toLowerCase().includes(L));
    }
    return C;
  }, [p, d, w]);
  ye(() => {
    w && y.length === 1 ? E(0) : E(-1);
  }, [y.length, w]), ye(() => {
    v && o && J.current && J.current.focus();
  }, [v, o, r]), ye(() => {
    var H, X;
    if (Q.current < 0) return;
    const C = Q.current;
    Q.current = -1;
    const L = (H = z.current) == null ? void 0 : H.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(C, L.length - 1)].focus() : (X = z.current) == null || X.focus();
  }, [r]), ye(() => {
    if (!v) return;
    const C = (L) => {
      z.current && !z.current.contains(L.target) && A.current && !A.current.contains(L.target) && (R(!1), D(""));
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [v]), ye(() => {
    if (!v || !z.current) return;
    const C = z.current.getBoundingClientRect(), L = window.innerHeight - C.bottom, X = L < 300 && C.top > L;
    k({
      left: C.left,
      width: C.width,
      ...X ? { bottom: window.innerHeight - C.top } : { top: C.bottom }
    });
  }, [v]);
  const F = ce(async () => {
    if (!(u || !a) && (R(!0), D(""), E(-1), g(!1), !o))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, a, o, n]), j = ce(() => {
    var C;
    R(!1), D(""), E(-1), (C = z.current) == null || C.focus();
  }, []), I = ce(
    (C) => {
      let L;
      if (i) {
        const H = p.find((X) => X.value === C);
        if (H)
          L = [...P.current, H];
        else
          return;
      } else {
        const H = p.find((X) => X.value === C);
        if (H)
          L = [H];
        else
          return;
      }
      P.current = L, n("valueChanged", { value: L.map((H) => H.value) }), i ? (D(""), E(-1)) : j();
    },
    [i, p, n, j]
  ), K = ce(
    (C) => {
      Q.current = P.current.findIndex((H) => H.value === C);
      const L = P.current.filter((H) => H.value !== C);
      P.current = L, n("valueChanged", { value: L.map((H) => H.value) });
    },
    [n]
  ), Y = ce(
    (C) => {
      C.stopPropagation(), n("valueChanged", { value: [] }), j();
    },
    [n, j]
  ), te = ce((C) => {
    D(C.target.value);
  }, []), ee = ce(
    (C) => {
      if (!v) {
        if (C.key === "ArrowDown" || C.key === "ArrowUp" || C.key === "Enter" || C.key === " ") {
          if (C.target.tagName === "BUTTON") return;
          C.preventDefault(), C.stopPropagation(), F();
        }
        return;
      }
      switch (C.key) {
        case "ArrowDown":
          C.preventDefault(), C.stopPropagation(), E(
            (L) => L < y.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          C.preventDefault(), C.stopPropagation(), E(
            (L) => L > 0 ? L - 1 : y.length - 1
          );
          break;
        case "Enter":
          C.preventDefault(), C.stopPropagation(), O >= 0 && O < y.length && I(y[O].value);
          break;
        case "Escape":
          C.preventDefault(), C.stopPropagation(), j();
          break;
        case "Tab":
          j();
          break;
        case "Backspace":
          w === "" && i && r.length > 0 && K(r[r.length - 1].value);
          break;
      }
    },
    [
      v,
      F,
      j,
      y,
      O,
      I,
      w,
      i,
      r,
      K
    ]
  ), ie = ce(
    async (C) => {
      C.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
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
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", N === null || N === C) {
        B(null), ne(null);
        return;
      }
      const H = L.currentTarget.getBoundingClientRect(), X = H.left + H.width / 2, re = L.clientX < X ? "before" : "after";
      B(C), ne(re);
    },
    [N]
  ), fe = ce(
    (C) => {
      if (C.preventDefault(), N === null || x === null || Z === null || N === x) return;
      const L = [...P.current], [H] = L.splice(N, 1);
      let X = x;
      N < x ? X = Z === "before" ? X - 1 : X : X = Z === "before" ? X : X + 1, L.splice(X, 0, H), P.current = L, n("valueChanged", { value: L.map((re) => re.value) }), $(null), B(null), ne(null);
    },
    [N, x, Z, n]
  ), ve = ce(() => {
    $(null), B(null), ne(null);
  }, []);
  if (ye(() => {
    if (O < 0 || !A.current) return;
    const C = A.current.querySelector(
      `[id="${l}-opt-${O}"]`
    );
    C && C.scrollIntoView({ block: "nearest" });
  }, [O, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, m) : r.map((C) => /* @__PURE__ */ e.createElement("span", { key: C.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: C.image }), /* @__PURE__ */ e.createElement("span", null, C.label))));
  const T = !s && r.length > 0 && !u, M = v ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: W
    },
    (o || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
        onChange: te,
        onKeyDown: ee,
        placeholder: S["js.dropdownSelect.filterPlaceholder"],
        "aria-label": S["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": O >= 0 ? `${l}-opt-${O}` : void 0,
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
      !o && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, S["js.dropdownSelect.error"])),
      o && y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      o && y.map((C, L) => /* @__PURE__ */ e.createElement(
        gl,
        {
          key: C.value,
          id: `${l}-opt-${L}`,
          option: C,
          highlighted: L === O,
          searchTerm: w,
          onSelect: I,
          onMouseEnter: () => E(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: z,
      className: "tlDropdownSelect" + (v ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": v,
      "aria-haspopup": "listbox",
      "aria-owns": v ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: v ? void 0 : F,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : r.map((C, L) => {
      let H = "";
      return N === L ? H = "tlDropdownSelect__chip--dragging" : x === L && Z === "before" ? H = "tlDropdownSelect__chip--dropBefore" : x === L && Z === "after" && (H = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        El,
        {
          key: C.value,
          option: C,
          removable: !u && (i || !s),
          onRemove: K,
          removeLabel: b(C.label),
          draggable: h,
          onDragStart: h ? (X) => ue(L, X) : void 0,
          onDragOver: h ? (X) => he(L, X) : void 0,
          onDrop: h ? fe : void 0,
          onDragEnd: h ? ve : void 0,
          dragClassName: h ? H : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, T && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Y,
        "aria-label": S["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, v ? "▲" : "▼"))
  ), M && vl.createPortal(M, document.body));
}, { useCallback: Ke, useRef: yl } = e, Et = "application/x-tl-color", wl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: c
}) => {
  const s = yl(null), u = Ke(
    (p) => (m) => {
      s.current = p, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Ke((p) => {
    p.preventDefault(), p.dataTransfer.dropEffect = "move";
  }, []), o = Ke(
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
    l.map((p, m) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: m,
        className: "tlColorInput__paletteCell" + (p == null ? " tlColorInput__paletteCell--empty" : ""),
        style: p != null ? { backgroundColor: p } : void 0,
        title: p ?? "",
        draggable: p != null,
        onClick: p != null ? () => n(p) : void 0,
        onDoubleClick: p != null ? () => r(p) : void 0,
        onDragStart: p != null ? u(m) : void 0,
        onDragOver: a,
        onDrop: o(m)
      }
    ))
  );
};
function gt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Ct(l) {
  if (!Qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function yt(l, t, n) {
  const r = (i) => gt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function kl(l, t, n) {
  const r = l / 255, i = t / 255, c = n / 255, s = Math.max(r, i, c), u = Math.min(r, i, c), a = s - u;
  let o = 0;
  a !== 0 && (s === r ? o = (i - c) / a % 6 : s === i ? o = (c - r) / a + 2 : o = (r - i) / a + 4, o *= 60, o < 0 && (o += 360));
  const p = s === 0 ? 0 : a / s;
  return [o, p, s];
}
function Sl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), c = n - r;
  let s = 0, u = 0, a = 0;
  return l < 60 ? (s = r, u = i, a = 0) : l < 120 ? (s = i, u = r, a = 0) : l < 180 ? (s = 0, u = r, a = i) : l < 240 ? (s = 0, u = i, a = r) : l < 300 ? (s = i, u = 0, a = r) : (s = r, u = 0, a = i), [
    Math.round((s + c) * 255),
    Math.round((u + c) * 255),
    Math.round((a + c) * 255)
  ];
}
function Nl(l) {
  return kl(...Ct(l));
}
function Ye(l, t, n) {
  return yt(...Sl(l, t, n));
}
const { useCallback: we, useRef: ft } = e, Tl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Nl(l), c = ft(null), s = ft(null), u = we(
    (f, b) => {
      var D;
      const v = (D = c.current) == null ? void 0 : D.getBoundingClientRect();
      if (!v) return;
      const R = Math.max(0, Math.min(1, (f - v.left) / v.width)), w = Math.max(0, Math.min(1, 1 - (b - v.top) / v.height));
      t(Ye(n, R, w));
    },
    [n, t]
  ), a = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), o = we(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), p = we(
    (f) => {
      var w;
      const b = (w = s.current) == null ? void 0 : w.getBoundingClientRect();
      if (!b) return;
      const R = Math.max(0, Math.min(1, (f - b.top) / b.height)) * 360;
      t(Ye(R, r, i));
    },
    [r, i, t]
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
  ), S = Ye(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: c,
      className: "tlColorInput__svField",
      style: { backgroundColor: S },
      onPointerDown: a,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${r * 100}%`, top: `${(1 - i) * 100}%` }
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
function Rl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const xl = {
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
}, { useState: Me, useCallback: pe, useEffect: Ge, useRef: Ll, useLayoutEffect: Dl } = e, Il = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: c,
  onConfirm: s,
  onCancel: u,
  onPaletteChange: a
}) => {
  const [o, p] = Me("palette"), [m, h] = Me(t), S = Ll(null), f = ae(xl), [b, v] = Me(null);
  Dl(() => {
    if (!l.current || !S.current) return;
    const A = l.current.getBoundingClientRect(), P = S.current.getBoundingClientRect();
    let Q = A.bottom + 4, d = A.left;
    Q + P.height > window.innerHeight && (Q = A.top - P.height - 4), d + P.width > window.innerWidth && (d = Math.max(0, A.right - P.width)), v({ top: Q, left: d });
  }, [l]);
  const R = m != null, [w, D, O] = R ? Ct(m) : [0, 0, 0], [E, _] = Me((m == null ? void 0 : m.toUpperCase()) ?? "");
  Ge(() => {
    _((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Ge(() => {
    const A = (P) => {
      P.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ge(() => {
    const A = (Q) => {
      S.current && !S.current.contains(Q.target) && u();
    }, P = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(P), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const g = pe(
    (A) => (P) => {
      const Q = parseInt(P.target.value, 10);
      if (isNaN(Q)) return;
      const d = gt(Q);
      h(yt(A === "r" ? d : w, A === "g" ? d : D, A === "b" ? d : O));
    },
    [w, D, O]
  ), W = pe(
    (A) => {
      if (m != null) {
        A.dataTransfer.setData(Et, m.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const P = document.createElement("div");
        P.style.width = "33px", P.style.height = "33px", P.style.backgroundColor = m, P.style.borderRadius = "3px", P.style.border = "1px solid rgba(0,0,0,0.1)", P.style.position = "absolute", P.style.top = "-9999px", document.body.appendChild(P), A.dataTransfer.setDragImage(P, 16, 16), requestAnimationFrame(() => document.body.removeChild(P));
      }
    },
    [m]
  ), k = pe((A) => {
    const P = A.target.value;
    _(P), Qe(P) && h(P);
  }, []), N = pe(() => {
    h(null);
  }, []), $ = pe((A) => {
    h(A);
  }, []), x = pe(
    (A) => {
      s(A);
    },
    [s]
  ), B = pe(
    (A, P) => {
      const Q = [...n], d = Q[A];
      Q[A] = Q[P], Q[P] = d, a(Q);
    },
    [n, a]
  ), Z = pe(
    (A, P) => {
      const Q = [...n];
      Q[A] = P, a(Q);
    },
    [n, a]
  ), ne = pe(() => {
    a([...i]);
  }, [i, a]), z = pe(
    (A) => {
      if (Rl(n, A)) return;
      const P = n.indexOf(null);
      if (P < 0) return;
      const Q = [...n];
      Q[P] = A.toUpperCase(), a(Q);
    },
    [n, a]
  ), J = pe(() => {
    m != null && z(m), s(m);
  }, [m, s, z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: S,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      wl,
      {
        colors: n,
        columns: r,
        onSelect: $,
        onConfirm: x,
        onSwap: B,
        onReplace: Z
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ne }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Tl, { color: m ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (R ? "" : " tlColorInput--noColor"),
        style: R ? { backgroundColor: m } : void 0,
        draggable: R,
        onDragStart: R ? W : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: R ? w : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: R ? D : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: R ? O : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (E !== "" && !Qe(E) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: E,
        onChange: k
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, c && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: N }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, f["js.colorInput.ok"]))
  );
}, Ml = { "js.colorInput.chooseColor": "Choose color" }, { useState: Pl, useCallback: Pe, useRef: jl } = e, Al = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Ml), [i, c] = Pl(!1), s = jl(null), u = t.value, a = t.editable !== !1, o = t.palette ?? [], p = t.paletteColumns ?? 6, m = t.defaultPalette ?? o, h = Pe(() => {
    a && c(!0);
  }, [a]), S = Pe(
    (v) => {
      c(!1), n("valueChanged", { value: v });
    },
    [n]
  ), f = Pe(() => {
    c(!1);
  }, []), b = Pe(
    (v) => {
      n("paletteChanged", { palette: v });
    },
    [n]
  );
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Il,
    {
      anchorRef: s,
      currentColor: u,
      palette: o,
      paletteColumns: p,
      defaultPalette: m,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: f,
      onPaletteChange: b
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
}, { useState: Re, useCallback: be, useEffect: je, useRef: ht, useLayoutEffect: Bl, useMemo: Ol } = e, $l = {
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
}, Fl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: c,
  onLoadIcons: s
}) => {
  const u = ae($l), [a, o] = Re("simple"), [p, m] = Re(""), [h, S] = Re(t ?? ""), [f, b] = Re(!1), [v, R] = Re(null), w = ht(null), D = ht(null);
  Bl(() => {
    if (!l.current || !w.current) return;
    const x = l.current.getBoundingClientRect(), B = w.current.getBoundingClientRect();
    let Z = x.bottom + 4, ne = x.left;
    Z + B.height > window.innerHeight && (Z = x.top - B.height - 4), ne + B.width > window.innerWidth && (ne = Math.max(0, x.right - B.width)), R({ top: Z, left: ne });
  }, [l]), je(() => {
    !r && !f && s().catch(() => b(!0));
  }, [r, f, s]), je(() => {
    r && D.current && D.current.focus();
  }, [r]), je(() => {
    const x = (B) => {
      B.key === "Escape" && c();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [c]), je(() => {
    const x = (Z) => {
      w.current && !w.current.contains(Z.target) && c();
    }, B = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(B), document.removeEventListener("mousedown", x);
    };
  }, [c]);
  const O = Ol(() => {
    if (!p) return n;
    const x = p.toLowerCase();
    return n.filter(
      (B) => B.prefix.toLowerCase().includes(x) || B.label.toLowerCase().includes(x) || B.terms != null && B.terms.some((Z) => Z.includes(x))
    );
  }, [n, p]), E = be((x) => {
    m(x.target.value);
  }, []), _ = be(
    (x) => {
      i(x);
    },
    [i]
  ), g = be((x) => {
    S(x);
  }, []), W = be((x) => {
    S(x.target.value);
  }, []), k = be(() => {
    i(h || null);
  }, [h, i]), N = be(() => {
    i(null);
  }, [i]), $ = be(async (x) => {
    x.preventDefault(), b(!1);
    try {
      await s();
    } catch {
      b(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: w,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
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
        onChange: E,
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
      !r && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: $ }, u["js.iconSelect.loadError"])),
      r && O.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && O.map(
        (x) => x.variants.map((B) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: B.encoded,
            className: "tlIconSelect__iconCell" + (B.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": B.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => a === "simple" ? _(B.encoded) : g(B.encoded),
            onKeyDown: (Z) => {
              (Z.key === "Enter" || Z.key === " ") && (Z.preventDefault(), a === "simple" ? _(B.encoded) : g(B.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: B.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: W
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Se, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: c }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: N }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: k }, u["js.iconSelect.ok"]))
  );
}, Hl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Wl, useCallback: Ae, useRef: zl } = e, Ul = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Hl), [i, c] = Wl(!1), s = zl(null), u = t.value, a = t.editable !== !1, o = t.disabled === !0, p = t.icons ?? [], m = t.iconsLoaded === !0, h = Ae(() => {
    a && !o && c(!0);
  }, [a, o]), S = Ae(
    (v) => {
      c(!1), n("valueChanged", { value: v });
    },
    [n]
  ), f = Ae(() => {
    c(!1);
  }, []), b = Ae(async () => {
    await n("loadIcons");
  }, [n]);
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: o,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Fl,
    {
      anchorRef: s,
      currentValue: u,
      icons: p,
      iconsLoaded: m,
      onSelect: S,
      onCancel: f,
      onLoadIcons: b
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: Vl, useMemo: _t, useRef: Kl, useState: Xe } = e, Yl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Gl = [1, 2, 3, 4];
function Xl(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function ql(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of Gl)
    n >= i && (r = i);
  return r;
}
function Zl(l, t) {
  const n = Yl[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function Ql(l, t) {
  const n = Math.max(1, t), r = {}, i = (m, h) => !!(r[m] && r[m][h]), c = (m, h) => {
    r[m] || (r[m] = {}), r[m][h] = !0;
  }, s = [];
  let u = 0, a = 0;
  const o = (m) => {
    let h = null;
    for (const f of s) f.rowStart === m && (h = f);
    if (!h) return;
    let S = h.colEnd;
    for (; S < n && !i(m, S); ) S++;
    if (S !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let b = h.colEnd; b < S; b++) c(f, b);
      h.colEnd = S;
    }
  };
  for (const m of l) {
    const h = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let S = Math.min(Zl(m.width, n), n);
    for (; i(u, a); )
      a++, a >= n && (a = 0, u++);
    let f = 0;
    for (let D = a; D < n && !i(u, D); D++)
      f++;
    if (S > f) {
      for (o(u), a = 0, u++; i(u, a); )
        a++, a >= n && (a = 0, u++);
      f = 0;
      for (let D = a; D < n && !i(u, D); D++)
        f++;
      S = Math.min(S, f);
    }
    const b = a, v = a + S, R = u, w = u + h;
    s.push({ id: m.id, colStart: b, colEnd: v, rowStart: R, rowEnd: w });
    for (let D = R; D < w; D++)
      for (let O = b; O < v; O++) c(D, O);
    a = v, a >= n && (a = 0, u++);
  }
  o(u);
  let p = 0;
  for (const m of s) m.rowEnd > p && (p = m.rowEnd);
  for (let m = 1; m < p; m++)
    for (let h = 0; h < n; h++) {
      if (i(m, h)) continue;
      const S = s.find((f) => f.rowEnd === m && f.colStart <= h && h < f.colEnd);
      if (S) {
        S.rowEnd = m + 1;
        for (let f = S.colStart; f < S.colEnd; f++) c(m, f);
      }
    }
  return s;
}
const Jl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), c = Kl(null), [s, u] = Xe(1), a = t.editMode === !0;
  Vl(() => {
    const _ = c.current;
    if (!_) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, W = Xl(r, g), k = () => u(ql(_.clientWidth, W));
    k();
    const N = new ResizeObserver(k);
    return N.observe(_), () => N.disconnect();
  }, [r]);
  const o = _t(() => Ql(i, s), [i, s]), p = _t(() => {
    const _ = {};
    for (const g of o) _[g.id] = g;
    return _;
  }, [o]), [m, h] = Xe(null), [S, f] = Xe(null), b = ke((_, g) => {
    if (!a) {
      _.preventDefault();
      return;
    }
    h(g), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", g);
  }, [a]), v = ke((_, g) => {
    if (!a || !m || m === g) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const W = _.currentTarget.getBoundingClientRect(), k = _.clientX < W.left + W.width / 2;
    f((N) => N && N.id === g && N.before === k ? N : { id: g, before: k });
  }, [a, m]), R = ke(() => {
  }, []), w = ke((_, g, W) => {
    const k = i.map((B) => B.id), N = k.indexOf(_);
    if (N < 0) return;
    k.splice(N, 1);
    const $ = k.indexOf(g);
    if ($ < 0) {
      k.splice(N, 0, _);
      return;
    }
    const x = W ? $ : $ + 1;
    k.splice(x, 0, _), n("reorder", { order: k });
  }, [i, n]), D = ke((_, g) => {
    if (!a || !m || m === g) return;
    _.preventDefault();
    const W = _.currentTarget.getBoundingClientRect(), k = _.clientX < W.left + W.width / 2;
    w(m, g, k), h(null), f(null);
  }, [a, m, w]), O = ke(() => {
    h(null), f(null);
  }, []), E = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: c,
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: E }, i.map((_) => {
      const g = p[_.id];
      if (!g) return null;
      const W = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, k = ["tlDashboard__tile"];
      return m === _.id && k.push("tlDashboard__tile--dragging"), S && S.id === _.id && k.push(S.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: k.join(" "),
          style: W,
          draggable: a,
          onDragStart: (N) => b(N, _.id),
          onDragOver: (N) => v(N, _.id),
          onDragLeave: R,
          onDrop: (N) => D(N, _.id),
          onDragEnd: O
        },
        /* @__PURE__ */ e.createElement(G, { control: _.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
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
U("TLFileUpload", Xt);
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
U("TLSnackbar", Un);
U("TLMenu", Kn);
U("TLAppShell", Yn);
U("TLText", Gn);
U("TLTableView", qn);
U("TLFormLayout", ll);
U("TLFormGroup", al);
U("TLFormField", ul);
U("TLResourceCell", dl);
U("TLTreeView", pl);
U("TLDropdownSelect", Cl);
U("TLColorInput", Al);
U("TLIconSelect", Ul);
U("TLDashboard", Jl);
