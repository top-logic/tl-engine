import { React as e, useTLFieldValue as Te, getComponent as Tt, useTLState as q, useTLCommand as ne, TLChild as K, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as z } from "tl-react-bridge";
const { useCallback: Lt } = e, Rt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Lt(
    (a) => {
      r(a.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: xt } = e, Dt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = xt(
    (a) => {
      r(a.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    s ? "tlReactTextInput--error" : "",
    !s && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: It } = e, Mt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), s = It(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, o = t.errorMessage, a = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: r != null ? String(r) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": c || void 0,
      title: c && o ? o : void 0
    }
  ));
}, { useCallback: Pt } = e, jt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Pt(
    (o) => {
      r(o.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    s ? "tlReactDatePicker--error" : "",
    !s && c ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": s || void 0
    }
  ));
}, { useCallback: Bt } = e, At = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = Te(), s = Bt(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((h) => h.value === r)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, o = t.hasWarnings === !0, a = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && o ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: $t } = e, Ot = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = $t(
    (o) => {
      r(o.target.checked);
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
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    s ? "tlReactCheckbox--error" : "",
    !s && c ? "tlReactCheckbox--warning" : ""
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
      "aria-invalid": s || void 0
    }
  );
}, Ft = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Tt(c.cellModule) : void 0, o = i[c.name];
    if (u) {
      const a = { value: o, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + s + "-" + c.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, o != null ? String(o) : "");
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
const { useCallback: Ht } = e, Wt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = ne(), o = t ?? "click", a = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", k = c.hidden === !0, f = c.tooltip, S = k ? { display: "none" } : void 0, w = Ht(() => {
    u(o);
  }, [u, o]), g = h === "icon-only", C = h === "icon-only" || h === "icon-label", x = h === "label-only" || h === "icon-label" || g && !m, D = f ?? (g ? a : void 0), _ = D ? `text:${D}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: w,
      disabled: p,
      style: S,
      className: "tlReactButton" + (g ? " tlReactButton--iconOnly" : ""),
      "data-tooltip": _,
      "aria-label": g ? a : void 0
    },
    C && m && /* @__PURE__ */ e.createElement(Se, { encoded: m, className: "tlReactButton__image" }),
    x && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: zt } = e, Ut = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = ne(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, m = i ?? s.disabled === !0, p = zt(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    o
  );
}, Vt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Kt } = e, Yt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.tabs ?? [], i = t.activeTabId, s = Kt((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === i,
      className: "tlReactTabBar__tab" + (c.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(c.id)
    },
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Gt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: i })))));
}, Xt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, qt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : r !== "idle" ? r : m, k = e.useCallback(async () => {
    if (r === "recording") {
      const C = u.current;
      C && C.state !== "inactive" && C.stop();
      return;
    }
    if (r !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const C = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = C, o.current = [];
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", D = new MediaRecorder(C, x ? { mimeType: x } : void 0);
        u.current = D, D.ondataavailable = (_) => {
          _.data.size > 0 && o.current.push(_.data);
        }, D.onstop = async () => {
          C.getTracks().forEach((v) => v.stop()), a.current = null;
          const _ = new Blob(o.current, { type: D.mimeType || "audio/webm" });
          if (o.current = [], _.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const b = new FormData();
          b.append("audio", _, "recording.webm"), await n(b), i("idle");
        }, D.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(Xt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], w = h === "uploading", g = ["tlAudioRecorder__button"];
  return h === "recording" && g.push("tlAudioRecorder__button--recording"), h === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: k,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Zt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Qt = ({ controlId: l }) => {
  const t = q(), n = et(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
  e.useEffect(() => {
    r ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), c("disabled"));
  }, [r]), e.useEffect(() => {
    i !== a.current && (a.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (s === "disabled" || s === "loading")
      return;
    if (s === "playing") {
      u.current && u.current.pause(), c("paused");
      return;
    }
    if (s === "paused" && u.current) {
      u.current.play(), c("playing");
      return;
    }
    if (!o.current) {
      c("loading");
      try {
        const w = await fetch(n);
        if (!w.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", w.status), c("idle");
          return;
        }
        const g = await w.blob();
        o.current = URL.createObjectURL(g);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = ae(Zt), h = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], k = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: k,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Jt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, en = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : r !== "idle" ? r : o, h = e.useCallback(async (_) => {
    i("uploading");
    const b = new FormData();
    b.append("file", _, _.name), await n(b), i("idle");
  }, [n]), k = e.useCallback((_) => {
    var v;
    const b = (v = _.target.files) == null ? void 0 : v[0];
    b && h(b);
  }, [h]), f = e.useCallback(() => {
    var _;
    r !== "uploading" && ((_ = u.current) == null || _.click());
  }, [r]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!0);
  }, []), w = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), g = e.useCallback((_) => {
    var v;
    if (_.preventDefault(), _.stopPropagation(), c(!1), r === "uploading") return;
    const b = (v = _.dataTransfer.files) == null ? void 0 : v[0];
    b && h(b);
  }, [r, h]), C = p === "uploading", x = ae(Jt), D = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: g
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: C,
        title: D,
        "aria-label": D
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, tn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, nn = ({ controlId: l }) => {
  const t = q(), n = et(), r = ne(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), g = URL.createObjectURL(w), C = document.createElement("a");
        C.href = g, C.download = c, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(g);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = ae(tn);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const k = o ? h["js.downloading"] : h["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: o,
      title: k,
      "aria-label": k
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: h["js.download.clear"],
      "aria-label": h["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, ln = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, rn = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), k = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    w(), i("idle");
  }, [w]), C = e.useCallback(async () => {
    var L;
    if (r !== "uploading") {
      if (c(null), !S) {
        (L = h.current) == null || L.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = V, i("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, S]), x = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const L = a.current, V = p.current;
    if (!L || !V)
      return;
    V.width = L.videoWidth, V.height = L.videoHeight;
    const B = V.getContext("2d");
    B && (B.drawImage(L, 0, 0), w(), i("uploading"), V.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", T, "capture.jpg"), await n(O), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), D = e.useCallback(async (L) => {
    var T;
    const V = (T = L.target.files) == null ? void 0 : T[0];
    if (!V) return;
    i("uploading");
    const B = new FormData();
    B.append("photo", V, V.name), await n(B), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var V;
    if (r !== "overlayOpen") return;
    (V = k.current) == null || V.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const L = (V) => {
      V.key === "Escape" && g();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [r, g]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const _ = ae(ln), b = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], v = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && v.push("tlPhotoCapture__cameraBtn--uploading");
  const U = ["tlPhotoCapture__overlayVideo"];
  u && U.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return u && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: C,
      disabled: r === "uploading",
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: D
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: g }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: U.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: P.join(" "),
        onClick: () => o((L) => !L),
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: x,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: g,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, on = {
  "js.photoViewer.alt": "Captured photo"
}, an = ({ controlId: l }) => {
  const t = q(), n = et(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      s && (URL.revokeObjectURL(s), c(null));
      return;
    }
    if (i === u.current && s)
      return;
    u.current = i, s && (URL.revokeObjectURL(s), c(null));
    let a = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        a || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const o = ae(on);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, sn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), o = !u && s.some((w) => w.collapsed), a = u ? !c : c, m = Fe(null), p = Fe(null), h = Fe(null), k = ot((w, g) => {
    const C = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !a ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : g !== void 0 ? C.flex = `0 0 ${g}px` : C.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (C.minWidth = c ? w.minSize : void 0, C.minHeight = c ? void 0 : w.minSize), C;
  }, [c, u, o, a]), f = ot((w, g) => {
    w.preventDefault();
    const C = m.current;
    if (!C) return;
    const x = s[g], D = s[g + 1], _ = C.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((P) => {
      b.push(c ? P.offsetWidth : P.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: g,
      startPos: c ? w.clientX : w.clientY,
      startSizeBefore: b[g],
      startSizeAfter: b[g + 1],
      childBefore: x,
      childAfter: D
    };
    const v = (P) => {
      const L = p.current;
      if (!L || !h.current) return;
      const B = (c ? P.clientX : P.clientY) - L.startPos, T = L.childBefore.minSize || 0, O = L.childAfter.minSize || 0;
      let ee = L.startSizeBefore + B, F = L.startSizeAfter - B;
      ee < T && (F += ee - T, ee = T), F < O && (ee += F - O, F = O), h.current[L.splitterIndex] = ee, h.current[L.splitterIndex + 1] = F;
      const Z = C.querySelectorAll(":scope > .tlSplitPanel__child"), A = Z[L.splitterIndex], j = Z[L.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${F}px`);
    }, U = () => {
      if (document.removeEventListener("mousemove", v), document.removeEventListener("mouseup", U), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const P = {};
        s.forEach((L, V) => {
          const B = L.control;
          B != null && B.controlId && h.current && (P[B.controlId] = h.current[V]);
        }), n("updateSizes", { sizes: P });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", v), document.addEventListener("mouseup", U), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((w, g) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${w.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(w)
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control })
      )
    ), i && g < s.length - 1) {
      const C = s[g + 1];
      !w.collapsed && !C.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (D) => f(D, g)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${r}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, { useCallback: He } = e, cn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, un = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), mn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), pn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), fn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), hn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(cn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", h = s === "HIDDEN", k = He(() => {
    n("toggleMinimize");
  }, [n]), f = He(() => {
    n("toggleMaximize");
  }, [n]), S = He(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const w = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(dn, null) : /* @__PURE__ */ e.createElement(un, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(pn, null) : /* @__PURE__ */ e.createElement(mn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(fn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, bn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(K, { control: t.child })
  );
}, _n = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ue, useState: Ae, useEffect: $e, useRef: Oe } = e, gn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function qe(l, t, n, r) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...qe(s.children, t, n, s.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, En = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), vn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Cn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), yn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), wn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Oe(null);
  $e(() => {
    const a = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [s]), $e(() => {
    const a = (m) => {
      m.key === "Escape" && s();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [s]);
  const u = ue((a) => {
    a.type === "nav" ? (r(a.id), s()) : a.type === "command" && (i(a.id), s());
  }, [r, i, s]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
    if (a.type === "nav" && a.hidden) return null;
    if (a.type === "nav" || a.type === "command") {
      const m = a.type === "nav" && a.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: a.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
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
}, kn = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: s,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: o,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: k,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const w = Oe(null), [g, C] = Ae(null), x = ue(() => {
    r ? k === l.id ? S() : (w.current && C(w.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, k, l.id, c, f, S]), D = ue((b) => {
    w.current = b, o(b);
  }, [o]), _ = r && k === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
      title: r ? l.label : void 0,
      "aria-expanded": r ? _ : t,
      tabIndex: u,
      ref: D,
      onFocus: () => a(l.id)
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
  ), _ && /* @__PURE__ */ e.createElement(
    wn,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((b) => /* @__PURE__ */ e.createElement(
    vt,
    {
      key: b.id,
      item: b,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: p,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: f,
      onCloseFlyout: S
    }
  ))));
}, vt = ({
  item: l,
  activeItemId: t,
  collapsed: n,
  onSelect: r,
  onExecute: i,
  onToggleGroup: s,
  focusedId: c,
  setItemRef: u,
  onItemFocus: o,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        En,
        {
          item: l,
          active: l.id === t,
          collapsed: n,
          onSelect: r,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        vn,
        {
          item: l,
          collapsed: n,
          onExecute: i,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(Cn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(yn, null);
    case "group": {
      const k = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        kn,
        {
          item: l,
          expanded: k,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: s,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: o,
          focusedId: c,
          setItemRef: u,
          onItemFocus: o,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, Sn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(gn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, m] = Ae(() => {
    const T = /* @__PURE__ */ new Map(), O = (ee) => {
      for (const F of ee)
        F.type === "group" && (T.set(F.id, F.expanded), O(F.children));
    };
    return O(i), T;
  }), p = ue((T) => {
    m((O) => {
      const ee = new Map(O), F = ee.get(T) ?? !1;
      return ee.set(T, !F), n("toggleGroup", { itemId: T, expanded: !F }), ee;
    });
  }, [n]), h = ue((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), k = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [w, g] = Ae(null), C = ue((T) => {
    g(T);
  }, []), x = ue(() => {
    g(null);
  }, []);
  $e(() => {
    o || g(null);
  }, [o]);
  const [D, _] = Ae(() => {
    const T = qe(i, o, a);
    return T.length > 0 ? T[0].id : "";
  }), b = Oe(/* @__PURE__ */ new Map()), v = ue((T) => (O) => {
    O ? b.current.set(T, O) : b.current.delete(T);
  }, []), U = ue((T) => {
    _(T);
  }, []), P = Oe(0), L = ue((T) => {
    _(T), P.current++;
  }, []);
  $e(() => {
    const T = b.current.get(D);
    T && document.activeElement !== T && T.focus();
  }, [D, P.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), x();
      return;
    }
    const O = qe(i, o, a);
    if (O.length === 0) return;
    const ee = O.findIndex((Z) => Z.id === D);
    if (ee < 0) return;
    const F = O[ee];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const Z = (ee + 1) % O.length;
        L(O[Z].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const Z = (ee - 1 + O.length) % O.length;
        L(O[Z].id);
        break;
      }
      case "Home": {
        T.preventDefault(), L(O[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), L(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), F.type === "nav" ? h(F.id) : F.type === "command" ? k(F.id) : F.type === "group" && (o ? w === F.id ? x() : C(F.id) : p(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !o && ((a.get(F.id) ?? !1) || (T.preventDefault(), p(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !o && (a.get(F.id) ?? !1) && (T.preventDefault(), p(F.id));
        break;
      }
    }
  }, [
    i,
    o,
    a,
    D,
    w,
    L,
    h,
    k,
    p,
    C,
    x
  ]), B = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, i.map((T) => /* @__PURE__ */ e.createElement(
    vt,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: o,
      onSelect: h,
      onExecute: k,
      onToggleGroup: p,
      focusedId: D,
      setItemRef: v,
      onItemFocus: U,
      groupStates: a,
      flyoutGroupId: w,
      onOpenFlyout: C,
      onCloseFlyout: x
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(K, { control: t.activeContent })));
}, Nn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, c.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })));
}, Tn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: u })));
}, Ln = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(K, { control: c })));
}, Rn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, m) => /* @__PURE__ */ e.createElement(K, { key: m, control: a }))));
}, { useCallback: xn } = e, Dn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = xn((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((s, c) => {
    const u = c === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: s.id, className: "tlBreadcrumb__entry" }, c > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => i(s.id)
      },
      s.label
    ));
  })));
}, { useCallback: In } = e, Mn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = t.activeItemId, s = In((c) => {
    c !== i && n("selectItem", { itemId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, r.map((c) => {
    const u = c.id === i;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: c.id,
        type: "button",
        className: "tlBottomBar__item" + (u ? " tlBottomBar__item--active" : ""),
        onClick: () => s(c.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + c.icon, "aria-hidden": "true" }), c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, c.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, c.label)
    );
  }));
}, { useCallback: at, useEffect: st, useRef: Pn } = e, jn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Pn(null), u = at(() => {
    n("close");
  }, [n]), o = at((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!r) return;
    const a = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [r, u]), st(() => {
    r && c.current && c.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: o,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(K, { control: s })
  ) : null;
}, { useEffect: Bn, useRef: An } = e, $n = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = An(n.length);
  return Bn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(K, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, On = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Fn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Hn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(On), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [k, f] = Ie(null), [S, w] = Ie(null), [g, C] = Ie(null), x = Ce(null), [D, _] = Ie(!1), b = Ce(null), v = Ce(null), U = Ce(null), P = Ce(null), L = Ce(null), V = De(() => {
    n("close");
  }, [n]), B = De((Z, A) => {
    A.preventDefault();
    const j = P.current;
    if (!j) return;
    const Y = j.getBoundingClientRect(), d = !x.current, y = x.current ?? { x: Y.left, y: Y.top };
    d && (x.current = y, C(y)), L.current = {
      dir: Z,
      startX: A.clientX,
      startY: A.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...y },
      symmetric: d
    };
    const H = (G) => {
      const M = L.current;
      if (!M) return;
      const Q = G.clientX - M.startX, le = G.clientY - M.startY;
      let te = M.startW, ie = M.startH, de = 0, me = 0;
      M.symmetric ? (M.dir.includes("e") && (te = M.startW + 2 * Q), M.dir.includes("w") && (te = M.startW - 2 * Q), M.dir.includes("s") && (ie = M.startH + 2 * le), M.dir.includes("n") && (ie = M.startH - 2 * le)) : (M.dir.includes("e") && (te = M.startW + Q), M.dir.includes("w") && (te = M.startW - Q, de = Q), M.dir.includes("s") && (ie = M.startH + le), M.dir.includes("n") && (ie = M.startH - le, me = le));
      const he = Math.max(200, te), be = Math.max(100, ie);
      M.symmetric ? (de = (M.startW - he) / 2, me = (M.startH - be) / 2) : (M.dir.includes("w") && he === 200 && (de = M.startW - 200), M.dir.includes("n") && be === 100 && (me = M.startH - 100)), v.current = he, U.current = be, f(he), w(be);
      const N = {
        x: M.startPos.x + de,
        y: M.startPos.y + me
      };
      x.current = N, C(N);
    }, $ = () => {
      document.removeEventListener("mousemove", H), document.removeEventListener("mouseup", $);
      const G = v.current, M = U.current;
      (G != null || M != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...M != null ? { height: Math.round(M) + "px" } : {}
      }), L.current = null;
    };
    document.addEventListener("mousemove", H), document.addEventListener("mouseup", $);
  }, [n]), T = De((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const A = P.current;
    if (!A) return;
    const j = A.getBoundingClientRect(), Y = x.current ?? { x: j.left, y: j.top }, d = Z.clientX - Y.x, y = Z.clientY - Y.y, H = (G) => {
      const M = window.innerWidth, Q = window.innerHeight;
      let le = G.clientX - d, te = G.clientY - y;
      const ie = A.offsetWidth, de = A.offsetHeight;
      le + ie > M && (le = M - ie), te + de > Q && (te = Q - de), le < 0 && (le = 0), te < 0 && (te = 0);
      const me = { x: le, y: te };
      x.current = me, C(me);
    }, $ = () => {
      document.removeEventListener("mousemove", H), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", H), document.addEventListener("mouseup", $);
  }, []), O = De(() => {
    var Z, A;
    if (D) {
      const j = b.current;
      j && (C(j.x !== -1 ? { x: j.x, y: j.y } : null), f(j.w), w(j.h)), _(!1);
    } else {
      const j = P.current, Y = j == null ? void 0 : j.getBoundingClientRect();
      b.current = {
        x: ((Z = x.current) == null ? void 0 : Z.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((A = x.current) == null ? void 0 : A.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: k ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, _(!0), C({ x: 0, y: 0 }), f(null), w(null);
    }
  }, [D, k, S]), ee = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: k != null ? k + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: g ? "100vh" : "80vh",
    ...g ? { position: "absolute", left: g.x + "px", top: g.y + "px" } : {}
  }, F = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: ee,
      ref: P,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": F
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : T,
        onDoubleClick: O
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: p })),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
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
          onClick: V,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(K, { control: a })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(K, { control: h }), m.map((Z, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: Z }))),
    o && !D && Fn.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (A) => B(Z, A)
      }
    ))
  );
}, { useCallback: Wn, useEffect: zn } = e, Un = {
  "js.drawer.close": "Close"
}, Vn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(Un), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Wn(() => {
    n("close");
  }, [n]);
  zn(() => {
    if (!i) return;
    const p = (h) => {
      h.key === "Escape" && a();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, a]);
  const m = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${c}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: a,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(K, { control: o })));
}, { useCallback: Kn } = e, Yn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.child, i = Kn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, { useCallback: Gn, useEffect: Xn, useState: qn } = e, Zn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, m] = qn(!1), p = Gn(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return Xn(() => {
    if (!u || c === 0) return;
    const h = setTimeout(p, c);
    return () => clearTimeout(h);
  }, [u, c, p]), !u && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: We, useEffect: ze, useRef: Qn, useState: ct } = e, Jn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = Qn(null), [a, m] = ct({ top: 0, left: 0 }), [p, h] = ct(0), k = u.filter((g) => g.type === "item" && !g.disabled);
  ze(() => {
    var v, U;
    if (!r) return;
    const g = ((v = o.current) == null ? void 0 : v.offsetHeight) ?? 200, C = ((U = o.current) == null ? void 0 : U.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let P = c, L = s;
      P + g > window.innerHeight && (P = Math.max(0, window.innerHeight - g)), L + C > window.innerWidth && (L = Math.max(0, window.innerWidth - C)), m({ top: P, left: L }), h(0);
      return;
    }
    if (!i) return;
    const x = document.getElementById(i);
    if (!x) return;
    const D = x.getBoundingClientRect();
    let _ = D.bottom + 4, b = D.left;
    _ + g > window.innerHeight && (_ = D.top - g - 4), b + C > window.innerWidth && (b = D.right - C), m({ top: _, left: b }), h(0);
  }, [r, i, s, c]);
  const f = We(() => {
    n("close");
  }, [n]), S = We((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  ze(() => {
    if (!r) return;
    const g = (C) => {
      o.current && !o.current.contains(C.target) && f();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [r, f]);
  const w = We((g) => {
    if (g.key === "Escape") {
      f();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), h((C) => (C + 1) % k.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), h((C) => (C - 1 + k.length) % k.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const C = k[p];
      C && S(C.id);
    }
  }, [f, S, k, p]);
  return ze(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: a.top, left: a.left },
      onKeyDown: w
    },
    u.map((g, C) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const D = k.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (D ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: D ? 0 : -1,
          onClick: () => S(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, el = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: i })), /* @__PURE__ */ e.createElement(K, { control: s }), c && /* @__PURE__ */ e.createElement(K, { control: c }), u && /* @__PURE__ */ e.createElement(K, { control: u }));
}, tl = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, s = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, nl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, ll = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(nl), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const I = (E) => {
      const R = E.detail;
      let W = R.target;
      for (; W && W !== N; ) {
        const J = W.dataset.row, re = W.dataset.col;
        if (J != null && re != null) {
          R.resolved = { key: J + "|" + re };
          return;
        }
        W = W.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", I), () => N.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, k = e.useMemo(
    () => s.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [s]
  ), f = a === "multi", S = 40, w = 20, g = e.useRef(null), C = e.useRef(null), x = e.useRef(null), [D, _] = e.useState({}), b = e.useRef(null), v = e.useRef(!1), U = e.useRef(null), [P, L] = e.useState(null), [V, B] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [s]);
  const T = e.useCallback((N) => D[N.name] ?? N.width, [D]), O = e.useMemo(() => {
    const N = [];
    let I = f && p > 0 ? S : 0;
    for (let E = 0; E < p && E < s.length; E++)
      N.push(I), I += T(s[E]);
    return N;
  }, [s, p, f, S, T]), ee = c * o, F = e.useRef(null), Z = e.useCallback((N, I, E) => {
    E.preventDefault(), E.stopPropagation(), b.current = { column: N, startX: E.clientX, startWidth: I };
    let R = E.clientX, W = 0;
    const J = () => {
      const oe = b.current;
      if (!oe) return;
      const pe = Math.max(it, oe.startWidth + (R - oe.startX) + W);
      _((ve) => ({ ...ve, [oe.column]: pe }));
    }, re = () => {
      const oe = C.current, pe = g.current;
      if (!oe || !b.current) return;
      const ve = oe.getBoundingClientRect(), nt = 40, lt = 8, Nt = oe.scrollLeft;
      R > ve.right - nt ? oe.scrollLeft += lt : R < ve.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - Nt;
      rt !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), W += rt, J()), F.current = requestAnimationFrame(re);
    };
    F.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      R = oe.clientX, J();
    }, xe = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", xe), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = b.current;
      if (pe) {
        const ve = Math.max(it, pe.startWidth + (oe.clientX - pe.startX) + W);
        n("columnResize", { column: pe.column, width: ve }), b.current = null, v.current = !0, requestAnimationFrame(() => {
          v.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", xe);
  }, [n]), A = e.useCallback(() => {
    g.current && C.current && (g.current.scrollLeft = C.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const N = C.current;
      if (!N) return;
      const I = N.scrollTop, E = Math.ceil(N.clientHeight / o), R = Math.floor(I / o);
      n("scroll", { start: R, count: E });
    }, 80);
  }, [n, o]), j = e.useCallback((N, I, E) => {
    if (v.current) return;
    let R;
    !I || I === "desc" ? R = "asc" : R = "desc";
    const W = E.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: R, mode: W });
  }, [n]), Y = e.useCallback((N, I) => {
    U.current = N, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, I) => {
    if (!U.current || U.current === N) {
      L(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const E = I.currentTarget.getBoundingClientRect(), R = I.clientX < E.left + E.width / 2 ? "left" : "right";
    L({ column: N, side: R });
  }, []), y = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const I = U.current;
    if (!I || !P) {
      U.current = null, L(null);
      return;
    }
    let E = s.findIndex((W) => W.name === P.column);
    if (E < 0) {
      U.current = null, L(null);
      return;
    }
    const R = s.findIndex((W) => W.name === I);
    P.side === "right" && E++, R < E && E--, n("columnReorder", { column: I, targetIndex: E }), U.current = null, L(null);
  }, [s, P, n]), H = e.useCallback(() => {
    U.current = null, L(null);
  }, []), $ = e.useCallback((N, I) => {
    I.shiftKey && I.preventDefault(), n("select", {
      rowIndex: N,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [n]), G = e.useCallback((N, I) => {
    I.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), M = e.useCallback(() => {
    const N = m === c && c > 0;
    n("selectAll", { selected: !N });
  }, [n, m, c]), Q = e.useCallback((N, I, E) => {
    E.stopPropagation(), n("expand", { rowIndex: N, expanded: I });
  }, [n]), le = e.useCallback((N, I) => {
    I.preventDefault(), B({ x: I.clientX, y: I.clientY, colIdx: N });
  }, []), te = e.useCallback(() => {
    V && (n("setFrozenColumnCount", { count: V.colIdx + 1 }), B(null));
  }, [V, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), B(null);
  }, [n]);
  e.useEffect(() => {
    if (!V) return;
    const N = () => B(null), I = (E) => {
      E.key === "Escape" && B(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", I);
    };
  }, [V]);
  const de = s.reduce((N, I) => N + T(I), 0) + (f ? S : 0), me = m === c && c > 0, he = m > 0 && m < c, be = e.useCallback((N) => {
    N && (N.indeterminate = he);
  }, [he]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (N) => {
        if (!U.current) return;
        N.preventDefault();
        const I = C.current, E = g.current;
        if (!I) return;
        const R = I.getBoundingClientRect(), W = 40, J = 8;
        N.clientX < R.left + W ? I.scrollLeft = Math.max(0, I.scrollLeft - J) : N.clientX > R.right - W && (I.scrollLeft += J), E && (E.scrollLeft = I.scrollLeft);
      },
      onDrop: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: g }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          U.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== U.current && L({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: be,
          className: "tlTableView__checkbox",
          checked: me,
          onChange: M
        }
      )
    ), s.map((N, I) => {
      const E = T(N);
      s.length - 1;
      let R = "tlTableView__headerCell";
      N.sortable && (R += " tlTableView__headerCell--sortable"), P && P.column === N.name && (R += " tlTableView__headerCell--dragOver-" + P.side);
      const W = I < p, J = I === p - 1;
      return W && (R += " tlTableView__headerCell--frozen"), J && (R += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: R,
          style: {
            width: E,
            minWidth: E,
            position: W ? "sticky" : "relative",
            ...W ? { left: O[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (re) => j(N.name, N.sortDirection, re) : void 0,
          onContextMenu: (re) => le(I, re),
          onDragStart: (re) => Y(N.name, re),
          onDragOver: (re) => d(N.name, re),
          onDrop: y,
          onDragEnd: H
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", k > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => Z(N.name, E, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (U.current && s.length > 0) {
            const I = s[s.length - 1];
            I.name !== U.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", L({ column: I.name, side: "right" }));
          }
        },
        onDrop: y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: C,
        className: "tlTableView__body",
        onScroll: A
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: ee, position: "relative", width: de } }, u.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: N.index * o,
            height: o,
            width: de
          },
          onClick: (I) => $(N.index, I)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: S,
              minWidth: S,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (I) => G(N.index, I),
              tabIndex: -1
            }
          )
        ),
        s.map((I, E) => {
          const R = T(I), W = E === s.length - 1, J = E < p, re = E === p - 1;
          let Ee = "tlTableView__cell";
          J && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const xe = h && E === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Ee,
              "data-row": N.id,
              "data-col": I.name,
              style: {
                ...W && !J ? { flex: "1 0 auto", minWidth: R } : { width: R, minWidth: R },
                ...J ? { position: "sticky", left: O[E], zIndex: 2 } : {}
              }
            },
            xe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * w } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => Q(N.index, !N.expanded, pe)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(K, { control: N.cells[I.name] })) : /* @__PURE__ */ e.createElement(K, { control: N.cells[I.name] })
          );
        })
      )))
    ),
    V && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: V.y, left: V.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      V.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, rl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ct = e.createContext(rl), { useMemo: ol, useRef: al, useState: sl, useEffect: cl } = e, il = 320, ul = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = al(null), [o, a] = sl(
    r === "top" ? "top" : "side"
  );
  cl(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((w) => {
      for (const g of w) {
        const x = g.contentRect.width / n;
        a(x < il ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const m = ol(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, k = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useCallback: dl } = e, ml = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, pl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(ml), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, h = dl(() => {
    n("toggleCollapse");
  }, [n]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: k }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useContext: fl, useState: hl, useCallback: bl } = e, _l = ({ controlId: l }) => {
  const t = q(), n = fl(Ct), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, k = t.field, f = n.readOnly, [S, w] = hl(!1), g = bl(() => w((_) => !_), []);
  if (!p) return null;
  const C = s != null, x = c != null && c.length > 0, D = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && x ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    r
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: g,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: k })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !C && x && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, _)))), !f && u && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, gl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", c].filter(Boolean).join(" "), k = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: p,
      "data-tooltip": k
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": k }, m);
}, El = 20, vl = () => {
  const l = q(), t = ne(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, b) => {
    t(b ? "collapse" : "expand", { nodeId: _ });
  }, [t]), h = e.useCallback((_, b) => {
    t("select", {
      nodeId: _,
      ctrlKey: b.ctrlKey || b.metaKey,
      shiftKey: b.shiftKey
    });
  }, [t]), k = e.useCallback((_, b) => {
    b.preventDefault(), t("contextMenu", { nodeId: _, x: b.clientX, y: b.clientY });
  }, [t]), f = e.useRef(null), S = e.useCallback((_, b) => {
    const v = b.getBoundingClientRect(), U = _.clientY - v.top, P = v.height / 3;
    return U < P ? "above" : U > P * 2 ? "below" : "within";
  }, []), w = e.useCallback((_, b) => {
    b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", _);
  }, []), g = e.useCallback((_, b) => {
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const v = S(b, b.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: v }), f.current = null;
    }, 50);
  }, [t, S]), C = e.useCallback((_, b) => {
    b.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const v = S(b, b.currentTarget);
    t("drop", { nodeId: _, position: v });
  }, [t, S]), x = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), D = e.useCallback((_) => {
    if (n.length === 0) return;
    let b = o;
    switch (_.key) {
      case "ArrowDown":
        _.preventDefault(), b = Math.min(o + 1, n.length - 1);
        break;
      case "ArrowUp":
        _.preventDefault(), b = Math.max(o - 1, 0);
        break;
      case "ArrowRight":
        if (_.preventDefault(), o >= 0 && o < n.length) {
          const v = n[o];
          if (v.expandable && !v.expanded) {
            t("expand", { nodeId: v.id });
            return;
          } else v.expanded && (b = o + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), o >= 0 && o < n.length) {
          const v = n[o];
          if (v.expanded) {
            t("collapse", { nodeId: v.id });
            return;
          } else {
            const U = v.depth;
            for (let P = o - 1; P >= 0; P--)
              if (n[P].depth < U) {
                b = P;
                break;
              }
          }
        }
        break;
      case "Enter":
        _.preventDefault(), o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: _.ctrlKey || _.metaKey,
          shiftKey: _.shiftKey
        });
        return;
      case " ":
        _.preventDefault(), r === "multi" && o >= 0 && o < n.length && t("select", {
          nodeId: n[o].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        _.preventDefault(), b = 0;
        break;
      case "End":
        _.preventDefault(), b = n.length - 1;
        break;
      default:
        return;
    }
    b !== o && a(b);
  }, [o, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: D
    },
    n.map((_, b) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: _.id,
        role: "treeitem",
        "aria-expanded": _.expandable ? _.expanded : void 0,
        "aria-selected": _.selected,
        "aria-level": _.depth + 1,
        className: [
          "tlTreeView__node",
          _.selected ? "tlTreeView__node--selected" : "",
          b === o ? "tlTreeView__node--focused" : "",
          c === _.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === _.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === _.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: _.depth * El },
        draggable: i,
        onClick: (v) => h(_.id, v),
        onContextMenu: (v) => k(_.id, v),
        onDragStart: (v) => w(_.id, v),
        onDragOver: s ? (v) => g(_.id, v) : void 0,
        onDrop: s ? (v) => C(_.id, v) : void 0,
        onDragEnd: x
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (v) => {
            v.stopPropagation(), p(_.id, _.expanded);
          },
          tabIndex: -1,
          "aria-label": _.expanded ? "Collapse" : "Expand"
        },
        _.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: _.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(K, { control: _.content }))
    ))
  );
};
var Ue = { exports: {} }, se = {}, Ve = { exports: {} }, X = {};
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
function Cl() {
  if (ut) return X;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
  function k(d) {
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
  }, S = Object.assign, w = {};
  function g(d, y, H) {
    this.props = d, this.context = y, this.refs = w, this.updater = H || f;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(d, y) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, y, "setState");
  }, g.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = g.prototype;
  function x(d, y, H) {
    this.props = d, this.context = y, this.refs = w, this.updater = H || f;
  }
  var D = x.prototype = new C();
  D.constructor = x, S(D, g.prototype), D.isPureReactComponent = !0;
  var _ = Array.isArray;
  function b() {
  }
  var v = { H: null, A: null, T: null, S: null }, U = Object.prototype.hasOwnProperty;
  function P(d, y, H) {
    var $ = H.ref;
    return {
      $$typeof: l,
      type: d,
      key: y,
      ref: $ !== void 0 ? $ : null,
      props: H
    };
  }
  function L(d, y) {
    return P(d.type, y, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var y = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(H) {
      return y[H];
    });
  }
  var T = /\/+/g;
  function O(d, y) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : y.toString(36);
  }
  function ee(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(b, b) : (d.status = "pending", d.then(
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
  function F(d, y, H, $, G) {
    var M = typeof d;
    (M === "undefined" || M === "boolean") && (d = null);
    var Q = !1;
    if (d === null) Q = !0;
    else
      switch (M) {
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
              return Q = d._init, F(
                Q(d._payload),
                y,
                H,
                $,
                G
              );
          }
      }
    if (Q)
      return G = G(d), Q = $ === "" ? "." + O(d, 0) : $, _(G) ? (H = "", Q != null && (H = Q.replace(T, "$&/") + "/"), F(G, y, H, "", function(ie) {
        return ie;
      })) : G != null && (V(G) && (G = L(
        G,
        H + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), y.push(G)), 1;
    Q = 0;
    var le = $ === "" ? "." : $ + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        $ = d[te], M = le + O($, te), Q += F(
          $,
          y,
          H,
          M,
          G
        );
    else if (te = k(d), typeof te == "function")
      for (d = te.call(d), te = 0; !($ = d.next()).done; )
        $ = $.value, M = le + O($, te++), Q += F(
          $,
          y,
          H,
          M,
          G
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return F(
          ee(d),
          y,
          H,
          $,
          G
        );
      throw y = String(d), Error(
        "Objects are not valid as a React child (found: " + (y === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : y) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, y, H) {
    if (d == null) return d;
    var $ = [], G = 0;
    return F(d, $, "", "", function(M) {
      return y.call(H, M, G++);
    }), $;
  }
  function A(d) {
    if (d._status === -1) {
      var y = d._result;
      y = y(), y.then(
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = H);
        },
        function(H) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = H);
        }
      ), d._status === -1 && (d._status = 0, d._result = y);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
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
  }, Y = {
    map: Z,
    forEach: function(d, y, H) {
      Z(
        d,
        function() {
          y.apply(this, arguments);
        },
        H
      );
    },
    count: function(d) {
      var y = 0;
      return Z(d, function() {
        y++;
      }), y;
    },
    toArray: function(d) {
      return Z(d, function(y) {
        return y;
      }) || [];
    },
    only: function(d) {
      if (!V(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return X.Activity = p, X.Children = Y, X.Component = g, X.Fragment = n, X.Profiler = i, X.PureComponent = x, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = v, X.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return v.H.useMemoCache(d);
    }
  }, X.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, X.cacheSignal = function() {
    return null;
  }, X.cloneElement = function(d, y, H) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var $ = S({}, d.props), G = d.key;
    if (y != null)
      for (M in y.key !== void 0 && (G = "" + y.key), y)
        !U.call(y, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && y.ref === void 0 || ($[M] = y[M]);
    var M = arguments.length - 2;
    if (M === 1) $.children = H;
    else if (1 < M) {
      for (var Q = Array(M), le = 0; le < M; le++)
        Q[le] = arguments[le + 2];
      $.children = Q;
    }
    return P(d.type, G, $);
  }, X.createContext = function(d) {
    return d = {
      $$typeof: c,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: s,
      _context: d
    }, d;
  }, X.createElement = function(d, y, H) {
    var $, G = {}, M = null;
    if (y != null)
      for ($ in y.key !== void 0 && (M = "" + y.key), y)
        U.call(y, $) && $ !== "key" && $ !== "__self" && $ !== "__source" && (G[$] = y[$]);
    var Q = arguments.length - 2;
    if (Q === 1) G.children = H;
    else if (1 < Q) {
      for (var le = Array(Q), te = 0; te < Q; te++)
        le[te] = arguments[te + 2];
      G.children = le;
    }
    if (d && d.defaultProps)
      for ($ in Q = d.defaultProps, Q)
        G[$] === void 0 && (G[$] = Q[$]);
    return P(d, M, G);
  }, X.createRef = function() {
    return { current: null };
  }, X.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, X.isValidElement = V, X.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, X.memo = function(d, y) {
    return {
      $$typeof: a,
      type: d,
      compare: y === void 0 ? null : y
    };
  }, X.startTransition = function(d) {
    var y = v.T, H = {};
    v.T = H;
    try {
      var $ = d(), G = v.S;
      G !== null && G(H, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(b, j);
    } catch (M) {
      j(M);
    } finally {
      y !== null && H.types !== null && (y.types = H.types), v.T = y;
    }
  }, X.unstable_useCacheRefresh = function() {
    return v.H.useCacheRefresh();
  }, X.use = function(d) {
    return v.H.use(d);
  }, X.useActionState = function(d, y, H) {
    return v.H.useActionState(d, y, H);
  }, X.useCallback = function(d, y) {
    return v.H.useCallback(d, y);
  }, X.useContext = function(d) {
    return v.H.useContext(d);
  }, X.useDebugValue = function() {
  }, X.useDeferredValue = function(d, y) {
    return v.H.useDeferredValue(d, y);
  }, X.useEffect = function(d, y) {
    return v.H.useEffect(d, y);
  }, X.useEffectEvent = function(d) {
    return v.H.useEffectEvent(d);
  }, X.useId = function() {
    return v.H.useId();
  }, X.useImperativeHandle = function(d, y, H) {
    return v.H.useImperativeHandle(d, y, H);
  }, X.useInsertionEffect = function(d, y) {
    return v.H.useInsertionEffect(d, y);
  }, X.useLayoutEffect = function(d, y) {
    return v.H.useLayoutEffect(d, y);
  }, X.useMemo = function(d, y) {
    return v.H.useMemo(d, y);
  }, X.useOptimistic = function(d, y) {
    return v.H.useOptimistic(d, y);
  }, X.useReducer = function(d, y, H) {
    return v.H.useReducer(d, y, H);
  }, X.useRef = function(d) {
    return v.H.useRef(d);
  }, X.useState = function(d) {
    return v.H.useState(d);
  }, X.useSyncExternalStore = function(d, y, H) {
    return v.H.useSyncExternalStore(
      d,
      y,
      H
    );
  }, X.useTransition = function() {
    return v.H.useTransition();
  }, X.version = "19.2.4", X;
}
var dt;
function yl() {
  return dt || (dt = 1, Ve.exports = Cl()), Ve.exports;
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
function wl() {
  if (mt) return se;
  mt = 1;
  var l = yl();
  function t(o) {
    var a = "https://react.dev/errors/" + o;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + o + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function s(o, a, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: o,
      containerInfo: a,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, a) {
    if (o === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(o, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return s(o, a, null, m);
  }, se.flushSync = function(o) {
    var a = c.T, m = r.p;
    try {
      if (c.T = null, r.p = 2, o) return o();
    } finally {
      c.T = a, r.p = m, r.d.f();
    }
  }, se.preconnect = function(o, a) {
    typeof o == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, r.d.C(o, a));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && r.d.D(o);
  }, se.preinit = function(o, a) {
    if (typeof o == "string" && a && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, k = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? r.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: k
        }
      ) : m === "script" && r.d.X(o, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: k,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, se.preinitModule = function(o, a) {
    if (typeof o == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = u(
            a.as,
            a.crossOrigin
          );
          r.d.M(o, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && r.d.M(o);
  }, se.preload = function(o, a) {
    if (typeof o == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin);
      r.d.L(o, m, {
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
  }, se.preloadModule = function(o, a) {
    if (typeof o == "string")
      if (a) {
        var m = u(a.as, a.crossOrigin);
        r.d.m(o, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else r.d.m(o);
  }, se.requestFormReset = function(o) {
    r.d.r(o);
  }, se.unstable_batchedUpdates = function(o, a) {
    return o(a);
  }, se.useFormState = function(o, a, m) {
    return c.H.useFormState(o, a, m);
  }, se.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function kl() {
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
  return l(), Ue.exports = wl(), Ue.exports;
}
var Sl = kl();
const { useState: _e, useCallback: ce, useRef: Le, useEffect: ye, useMemo: Ze } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Nl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: s,
  onDragOver: c,
  onDrop: u,
  onDragEnd: o,
  dragClassName: a
}) {
  const m = ce(
    (p) => {
      p.stopPropagation(), n(l.value);
    },
    [n, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
      draggable: i || void 0,
      onDragStart: s,
      onDragOver: c,
      onDrop: u,
      onDragEnd: o
    },
    i && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": r
      },
      "×"
    )
  );
}
function Tl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const c = ce(() => r(l.value), [r, l.value]), u = Ze(() => {
    if (!n) return l.label;
    const o = l.label.toLowerCase().indexOf(n.toLowerCase());
    return o < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, o), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(o, o + n.length)), l.label.substring(o + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: s,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: c,
      onMouseEnter: i
    },
    /* @__PURE__ */ e.createElement(tt, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Ll = ({ controlId: l, state: t }) => {
  const n = ne(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = s && i && !u && o, k = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = k["js.dropdownSelect.nothingFound"], S = ce(
    (E) => k["js.dropdownSelect.removeChip"].replace("{0}", E),
    [k]
  ), [w, g] = _e(!1), [C, x] = _e(""), [D, _] = _e(-1), [b, v] = _e(!1), [U, P] = _e({}), [L, V] = _e(null), [B, T] = _e(null), [O, ee] = _e(null), F = Le(null), Z = Le(null), A = Le(null), j = Le(r);
  j.current = r;
  const Y = Le(-1), d = Ze(
    () => new Set(r.map((E) => E.value)),
    [r]
  ), y = Ze(() => {
    let E = m.filter((R) => !d.has(R.value));
    if (C) {
      const R = C.toLowerCase();
      E = E.filter((W) => W.label.toLowerCase().includes(R));
    }
    return E;
  }, [m, d, C]);
  ye(() => {
    C && y.length === 1 ? _(0) : _(-1);
  }, [y.length, C]), ye(() => {
    w && a && Z.current && Z.current.focus();
  }, [w, a, r]), ye(() => {
    var W, J;
    if (Y.current < 0) return;
    const E = Y.current;
    Y.current = -1;
    const R = (W = F.current) == null ? void 0 : W.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    R && R.length > 0 ? R[Math.min(E, R.length - 1)].focus() : (J = F.current) == null || J.focus();
  }, [r]), ye(() => {
    if (!w) return;
    const E = (R) => {
      F.current && !F.current.contains(R.target) && A.current && !A.current.contains(R.target) && (g(!1), x(""));
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [w]), ye(() => {
    if (!w || !F.current) return;
    const E = F.current.getBoundingClientRect(), R = window.innerHeight - E.bottom, J = R < 300 && E.top > R;
    P({
      left: E.left,
      width: E.width,
      ...J ? { bottom: window.innerHeight - E.top } : { top: E.bottom }
    });
  }, [w]);
  const H = ce(async () => {
    if (!(u || !o) && (g(!0), x(""), _(-1), v(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        v(!0);
      }
  }, [u, o, a, n]), $ = ce(() => {
    var E;
    g(!1), x(""), _(-1), (E = F.current) == null || E.focus();
  }, []), G = ce(
    (E) => {
      let R;
      if (i) {
        const W = m.find((J) => J.value === E);
        if (W)
          R = [...j.current, W];
        else
          return;
      } else {
        const W = m.find((J) => J.value === E);
        if (W)
          R = [W];
        else
          return;
      }
      j.current = R, n("valueChanged", { value: R.map((W) => W.value) }), i ? (x(""), _(-1)) : $();
    },
    [i, m, n, $]
  ), M = ce(
    (E) => {
      Y.current = j.current.findIndex((W) => W.value === E);
      const R = j.current.filter((W) => W.value !== E);
      j.current = R, n("valueChanged", { value: R.map((W) => W.value) });
    },
    [n]
  ), Q = ce(
    (E) => {
      E.stopPropagation(), n("valueChanged", { value: [] }), $();
    },
    [n, $]
  ), le = ce((E) => {
    x(E.target.value);
  }, []), te = ce(
    (E) => {
      if (!w) {
        if (E.key === "ArrowDown" || E.key === "ArrowUp" || E.key === "Enter" || E.key === " ") {
          if (E.target.tagName === "BUTTON") return;
          E.preventDefault(), E.stopPropagation(), H();
        }
        return;
      }
      switch (E.key) {
        case "ArrowDown":
          E.preventDefault(), E.stopPropagation(), _(
            (R) => R < y.length - 1 ? R + 1 : 0
          );
          break;
        case "ArrowUp":
          E.preventDefault(), E.stopPropagation(), _(
            (R) => R > 0 ? R - 1 : y.length - 1
          );
          break;
        case "Enter":
          E.preventDefault(), E.stopPropagation(), D >= 0 && D < y.length && G(y[D].value);
          break;
        case "Escape":
          E.preventDefault(), E.stopPropagation(), $();
          break;
        case "Tab":
          $();
          break;
        case "Backspace":
          C === "" && i && r.length > 0 && M(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      H,
      $,
      y,
      D,
      G,
      C,
      i,
      r,
      M
    ]
  ), ie = ce(
    async (E) => {
      E.preventDefault(), v(!1);
      try {
        await n("loadOptions");
      } catch {
        v(!0);
      }
    },
    [n]
  ), de = ce(
    (E, R) => {
      V(E), R.dataTransfer.effectAllowed = "move", R.dataTransfer.setData("text/plain", String(E));
    },
    []
  ), me = ce(
    (E, R) => {
      if (R.preventDefault(), R.dataTransfer.dropEffect = "move", L === null || L === E) {
        T(null), ee(null);
        return;
      }
      const W = R.currentTarget.getBoundingClientRect(), J = W.left + W.width / 2, re = R.clientX < J ? "before" : "after";
      T(E), ee(re);
    },
    [L]
  ), he = ce(
    (E) => {
      if (E.preventDefault(), L === null || B === null || O === null || L === B) return;
      const R = [...j.current], [W] = R.splice(L, 1);
      let J = B;
      L < B ? J = O === "before" ? J - 1 : J : J = O === "before" ? J : J + 1, R.splice(J, 0, W), j.current = R, n("valueChanged", { value: R.map((re) => re.value) }), V(null), T(null), ee(null);
    },
    [L, B, O, n]
  ), be = ce(() => {
    V(null), T(null), ee(null);
  }, []);
  if (ye(() => {
    if (D < 0 || !A.current) return;
    const E = A.current.querySelector(
      `[id="${l}-opt-${D}"]`
    );
    E && E.scrollIntoView({ block: "nearest" });
  }, [D, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((E) => /* @__PURE__ */ e.createElement("span", { key: E.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: E.image }), /* @__PURE__ */ e.createElement("span", null, E.label))));
  const N = !c && r.length > 0 && !u, I = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: U
    },
    (a || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: le,
        onKeyDown: te,
        placeholder: k["js.dropdownSelect.filterPlaceholder"],
        "aria-label": k["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": D >= 0 ? `${l}-opt-${D}` : void 0,
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
      !a && !b && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, k["js.dropdownSelect.error"])),
      a && y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      a && y.map((E, R) => /* @__PURE__ */ e.createElement(
        Tl,
        {
          key: E.value,
          id: `${l}-opt-${R}`,
          option: E,
          highlighted: R === D,
          searchTerm: C,
          onSelect: G,
          onMouseEnter: () => _(R)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: F,
      className: "tlDropdownSelect" + (w ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": w,
      "aria-haspopup": "listbox",
      "aria-owns": w ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: w ? void 0 : H,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((E, R) => {
      let W = "";
      return L === R ? W = "tlDropdownSelect__chip--dragging" : B === R && O === "before" ? W = "tlDropdownSelect__chip--dropBefore" : B === R && O === "after" && (W = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Nl,
        {
          key: E.value,
          option: E,
          removable: !u && (i || !c),
          onRemove: M,
          removeLabel: S(E.label),
          draggable: h,
          onDragStart: h ? (J) => de(R, J) : void 0,
          onDragOver: h ? (J) => me(R, J) : void 0,
          onDrop: h ? he : void 0,
          onDragEnd: h ? be : void 0,
          dragClassName: h ? W : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, N && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": k["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), I && Sl.createPortal(I, document.body));
}, { useCallback: Ke, useRef: Rl } = e, yt = "application/x-tl-color", xl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Rl(null), u = Ke(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ke((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = Ke(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(yt);
      h ? s(m, h) : c.current !== null && c.current !== m && i(c.current, m), c.current = null;
    },
    [i, s]
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
        onClick: m != null ? () => n(m) : void 0,
        onDoubleClick: m != null ? () => r(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: o,
        onDrop: a(p)
      }
    ))
  );
};
function wt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function kt(l) {
  if (!Qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function St(l, t, n) {
  const r = (i) => wt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Dl(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const m = c === 0 ? 0 : o / c;
  return [a, m, c];
}
function Il(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function Ml(l) {
  return Dl(...kt(l));
}
function Ye(l, t, n) {
  return St(...Il(l, t, n));
}
const { useCallback: we, useRef: ft } = e, Pl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Ml(l), s = ft(null), c = ft(null), u = we(
    (f, S) => {
      var x;
      const w = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!w) return;
      const g = Math.max(0, Math.min(1, (f - w.left) / w.width)), C = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(Ye(n, g, C));
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
  ), m = we(
    (f) => {
      var C;
      const S = (C = c.current) == null ? void 0 : C.getBoundingClientRect();
      if (!S) return;
      const g = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Ye(g, r, i));
    },
    [r, i, t]
  ), p = we(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = we(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), k = Ye(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: k },
      onPointerDown: o,
      onPointerMove: a
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
      ref: c,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
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
function jl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Bl = {
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
}, { useState: Me, useCallback: fe, useEffect: Ge, useRef: Al, useLayoutEffect: $l } = e, Ol = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: s,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: o
}) => {
  const [a, m] = Me("palette"), [p, h] = Me(t), k = Al(null), f = ae(Bl), [S, w] = Me(null);
  $l(() => {
    if (!l.current || !k.current) return;
    const A = l.current.getBoundingClientRect(), j = k.current.getBoundingClientRect();
    let Y = A.bottom + 4, d = A.left;
    Y + j.height > window.innerHeight && (Y = A.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, A.right - j.width)), w({ top: Y, left: d });
  }, [l]);
  const g = p != null, [C, x, D] = g ? kt(p) : [0, 0, 0], [_, b] = Me((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ge(() => {
    b((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ge(() => {
    const A = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ge(() => {
    const A = (Y) => {
      k.current && !k.current.contains(Y.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const v = fe(
    (A) => (j) => {
      const Y = parseInt(j.target.value, 10);
      if (isNaN(Y)) return;
      const d = wt(Y);
      h(St(A === "r" ? d : C, A === "g" ? d : x, A === "b" ? d : D));
    },
    [C, x, D]
  ), U = fe(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(yt, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), A.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), P = fe((A) => {
    const j = A.target.value;
    b(j), Qe(j) && h(j);
  }, []), L = fe(() => {
    h(null);
  }, []), V = fe((A) => {
    h(A);
  }, []), B = fe(
    (A) => {
      c(A);
    },
    [c]
  ), T = fe(
    (A, j) => {
      const Y = [...n], d = Y[A];
      Y[A] = Y[j], Y[j] = d, o(Y);
    },
    [n, o]
  ), O = fe(
    (A, j) => {
      const Y = [...n];
      Y[A] = j, o(Y);
    },
    [n, o]
  ), ee = fe(() => {
    o([...i]);
  }, [i, o]), F = fe(
    (A) => {
      if (jl(n, A)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const Y = [...n];
      Y[j] = A.toUpperCase(), o(Y);
    },
    [n, o]
  ), Z = fe(() => {
    p != null && F(p), c(p);
  }, [p, c, F]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: k,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      xl,
      {
        colors: n,
        columns: r,
        onSelect: V,
        onConfirm: B,
        onSwap: T,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Pl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (g ? "" : " tlColorInput--noColor"),
        style: g ? { backgroundColor: p } : void 0,
        draggable: g,
        onDragStart: g ? U : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? C : "",
        onChange: v("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? x : "",
        onChange: v("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? D : "",
        onChange: v("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !Qe(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: P
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, f["js.colorInput.ok"]))
  );
}, Fl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Hl, useCallback: Pe, useRef: Wl } = e, zl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Fl), [i, s] = Hl(!1), c = Wl(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, h = Pe(() => {
    o && s(!0);
  }, [o]), k = Pe(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Pe(() => {
    s(!1);
  }, []), S = Pe(
    (w) => {
      n("paletteChanged", { palette: w });
    },
    [n]
  );
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": r["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Ol,
    {
      anchorRef: c,
      currentColor: u,
      palette: a,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: k,
      onCancel: f,
      onPaletteChange: S
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
}, { useState: Re, useCallback: ge, useEffect: je, useRef: ht, useLayoutEffect: Ul, useMemo: Vl } = e, Kl = {
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
}, Yl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ae(Kl), [o, a] = Re("simple"), [m, p] = Re(""), [h, k] = Re(t ?? ""), [f, S] = Re(!1), [w, g] = Re(null), C = ht(null), x = ht(null);
  Ul(() => {
    if (!l.current || !C.current) return;
    const B = l.current.getBoundingClientRect(), T = C.current.getBoundingClientRect();
    let O = B.bottom + 4, ee = B.left;
    O + T.height > window.innerHeight && (O = B.top - T.height - 4), ee + T.width > window.innerWidth && (ee = Math.max(0, B.right - T.width)), g({ top: O, left: ee });
  }, [l]), je(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), je(() => {
    r && x.current && x.current.focus();
  }, [r]), je(() => {
    const B = (T) => {
      T.key === "Escape" && s();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [s]), je(() => {
    const B = (O) => {
      C.current && !C.current.contains(O.target) && s();
    }, T = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", B);
    };
  }, [s]);
  const D = Vl(() => {
    if (!m) return n;
    const B = m.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(B) || T.label.toLowerCase().includes(B) || T.terms != null && T.terms.some((O) => O.includes(B))
    );
  }, [n, m]), _ = ge((B) => {
    p(B.target.value);
  }, []), b = ge(
    (B) => {
      i(B);
    },
    [i]
  ), v = ge((B) => {
    k(B);
  }, []), U = ge((B) => {
    k(B.target.value);
  }, []), P = ge(() => {
    i(h || null);
  }, [h, i]), L = ge(() => {
    i(null);
  }, [i]), V = ge(async (B) => {
    B.preventDefault(), S(!1);
    try {
      await c();
    } catch {
      S(!0);
    }
  }, [c]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: C,
      style: w ? { top: w.top, left: w.left, visibility: "visible" } : { visibility: "hidden" }
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
        ref: x,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: _,
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
      !r && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      r && D.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && D.map(
        (B) => B.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => o === "simple" ? b(T.encoded) : v(T.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), o === "simple" ? b(T.encoded) : v(T.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Se, { encoded: T.encoded })
        ))
      )
    ),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: U
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Se, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: L }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: P }, u["js.iconSelect.ok"]))
  );
}, Gl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Xl, useCallback: Be, useRef: ql } = e, Zl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Gl), [i, s] = Xl(!1), c = ql(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Be(() => {
    o && !a && s(!0);
  }, [o, a]), k = Be(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), f = Be(() => {
    s(!1);
  }, []), S = Be(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: a,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Yl,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: k,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: Ql, useMemo: bt, useRef: Jl, useState: Xe } = e, er = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, tr = [1, 2, 3, 4];
function nr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function lr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of tr)
    n >= i && (r = i);
  return r;
}
function rr(l, t) {
  const n = er[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function or(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, h) => !!(r[p] && r[p][h]), s = (p, h) => {
    r[p] || (r[p] = {}), r[p][h] = !0;
  }, c = [];
  let u = 0, o = 0;
  const a = (p) => {
    let h = null;
    for (const f of c) f.rowStart === p && (h = f);
    if (!h) return;
    let k = h.colEnd;
    for (; k < n && !i(p, k); ) k++;
    if (k !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let S = h.colEnd; S < k; S++) s(f, S);
      h.colEnd = k;
    }
  };
  for (const p of l) {
    const h = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let k = Math.min(rr(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let x = o; x < n && !i(u, x); x++)
      f++;
    if (k > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let x = o; x < n && !i(u, x); x++)
        f++;
      k = Math.min(k, f);
    }
    const S = o, w = o + k, g = u, C = u + h;
    c.push({ id: p.id, colStart: S, colEnd: w, rowStart: g, rowEnd: C });
    for (let x = g; x < C; x++)
      for (let D = S; D < w; D++) s(x, D);
    o = w, o >= n && (o = 0, u++);
  }
  a(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let h = 0; h < n; h++) {
      if (i(p, h)) continue;
      const k = c.find((f) => f.rowEnd === p && f.colStart <= h && h < f.colEnd);
      if (k) {
        k.rowEnd = p + 1;
        for (let f = k.colStart; f < k.colEnd; f++) s(p, f);
      }
    }
  return c;
}
const ar = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), s = Jl(null), [c, u] = Xe(1), o = t.editMode === !0;
  Ql(() => {
    const b = s.current;
    if (!b) return;
    const v = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, U = nr(r, v), P = () => u(lr(b.clientWidth, U));
    P();
    const L = new ResizeObserver(P);
    return L.observe(b), () => L.disconnect();
  }, [r]);
  const a = bt(() => or(i, c), [i, c]), m = bt(() => {
    const b = {};
    for (const v of a) b[v.id] = v;
    return b;
  }, [a]), [p, h] = Xe(null), [k, f] = Xe(null), S = ke((b, v) => {
    if (!o) {
      b.preventDefault();
      return;
    }
    h(v), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", v);
  }, [o]), w = ke((b, v) => {
    if (!o || !p || p === v) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const U = b.currentTarget.getBoundingClientRect(), P = b.clientX < U.left + U.width / 2;
    f((L) => L && L.id === v && L.before === P ? L : { id: v, before: P });
  }, [o, p]), g = ke(() => {
  }, []), C = ke((b, v, U) => {
    const P = i.map((T) => T.id), L = P.indexOf(b);
    if (L < 0) return;
    P.splice(L, 1);
    const V = P.indexOf(v);
    if (V < 0) {
      P.splice(L, 0, b);
      return;
    }
    const B = U ? V : V + 1;
    P.splice(B, 0, b), n("reorder", { order: P });
  }, [i, n]), x = ke((b, v) => {
    if (!o || !p || p === v) return;
    b.preventDefault();
    const U = b.currentTarget.getBoundingClientRect(), P = b.clientX < U.left + U.width / 2;
    C(p, v, P), h(null), f(null);
  }, [o, p, C]), D = ke(() => {
    h(null), f(null);
  }, []), _ = {
    display: "grid",
    gridTemplateColumns: `repeat(${c}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: s,
      className: "tlDashboard" + (o ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, i.map((b) => {
      const v = m[b.id];
      if (!v) return null;
      const U = {
        gridColumn: `${v.colStart + 1} / ${v.colEnd + 1}`,
        gridRow: `${v.rowStart + 1} / ${v.rowEnd + 1}`
      }, P = ["tlDashboard__tile"];
      return p === b.id && P.push("tlDashboard__tile--dragging"), k && k.id === b.id && P.push(k.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: P.join(" "),
          style: U,
          draggable: o,
          onDragStart: (L) => S(L, b.id),
          onDragOver: (L) => w(L, b.id),
          onDragLeave: g,
          onDrop: (L) => x(L, b.id),
          onDragEnd: D
        },
        /* @__PURE__ */ e.createElement(K, { control: b.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: sr, useRef: _t, useState: cr, useEffect: gt } = e, ir = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, ur = ({ group: l }) => {
  var u, o;
  const [t, n] = cr(!1), r = _t(null), i = _t(null), s = sr(() => {
    n((a) => !a);
  }, []);
  gt(() => {
    if (!t) return;
    const a = (m) => {
      i.current && !i.current.contains(m.target) && r.current && !r.current.contains(m.target) && n(!1);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [t]), gt(() => {
    if (!t) return;
    const a = (m) => {
      m.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [t]);
  const c = l.items.filter((a) => a != null);
  return c.length === 0 ? null : c.length === 1 && !((u = l.subGroups) != null && u.length) ? /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: c[0] }))) : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      type: "button",
      className: "tlToolbar__menuTrigger",
      onClick: s,
      "aria-expanded": t,
      "aria-haspopup": "true"
    },
    l.icon && /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }),
    /* @__PURE__ */ e.createElement("span", null, l.label ?? l.name),
    /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" }))
  ), t && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlToolbar__dropdown",
      role: "menu",
      onClick: () => n(!1)
    },
    c.map((a, m) => /* @__PURE__ */ e.createElement("div", { key: m, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: a }))),
    (o = l.subGroups) == null ? void 0 : o.map((a, m) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${m}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), a.items.map((p, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: p })))))
  ));
}, dr = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(ur, { group: i }) : /* @__PURE__ */ e.createElement(ir, { group: i }))));
}, mr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, pr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(K, { key: i, control: r })));
}, fr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), hr = {
  "js.sidebar.openDrawer": "Open navigation"
}, br = ({ controlId: l }) => {
  const t = ne(), n = ae(hr);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      id: l,
      type: "button",
      className: "tlDrawerToggle",
      "aria-label": n["js.sidebar.openDrawer"],
      onClick: () => t("toggle", {})
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "20", height: "20", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: "M2 4h12M2 8h12M2 12h12",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round"
      }
    ))
  );
}, { useState: Et } = e, _r = ({ controlId: l }) => {
  const t = q(), n = ne(), [r, i] = Et(""), [s, c] = Et(""), u = t.loginMethods ?? [], o = t.errorMessage, a = (m) => {
    m.preventDefault(), n("login", { username: r, password: s });
  };
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlLogin" }, /* @__PURE__ */ e.createElement("h2", { className: "tlLogin__title" }, t.title ?? "Login"), /* @__PURE__ */ e.createElement("form", { className: "tlLogin__form", onSubmit: a }, /* @__PURE__ */ e.createElement("label", { className: "tlLogin__field" }, /* @__PURE__ */ e.createElement("span", { className: "tlLogin__label" }, t.usernameLabel ?? "User name"), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      autoFocus: !0,
      autoComplete: "username",
      value: r,
      onChange: (m) => i(m.target.value)
    }
  )), /* @__PURE__ */ e.createElement("label", { className: "tlLogin__field" }, /* @__PURE__ */ e.createElement("span", { className: "tlLogin__label" }, t.passwordLabel ?? "Password"), /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      autoComplete: "current-password",
      value: s,
      onChange: (m) => c(m.target.value)
    }
  )), o ? /* @__PURE__ */ e.createElement("div", { className: "tlLogin__error", role: "alert" }, o) : null, /* @__PURE__ */ e.createElement("button", { type: "submit", className: "tlReactButton tlLogin__submit" }, t.loginLabel ?? "Login")), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlLogin__methods" }, u.map((m) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: m.id,
      type: "button",
      className: "tlReactButton tlLogin__method",
      onClick: () => window.location.assign(m.url)
    },
    m.label
  ))));
}, gr = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.loggedIn === !0, i = t.userName ?? "";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlUserMenu" }, /* @__PURE__ */ e.createElement("span", { className: "tlUserMenu__name" }, i), r ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlReactButton tlUserMenu__action",
      onClick: () => n("logout")
    },
    t.logoutLabel ?? "Logout"
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlReactButton tlUserMenu__action",
      onClick: () => n("openLogin")
    },
    t.loginLabel ?? "Login"
  ));
};
z("TLButton", Wt);
z("TLToggleButton", Ut);
z("TLTextInput", Rt);
z("TLPasswordInput", Dt);
z("TLNumberInput", Mt);
z("TLDatePicker", jt);
z("TLSelect", At);
z("TLCheckbox", Ot);
z("TLTable", Ft);
z("TLCounter", Vt);
z("TLTabBar", Yt);
z("TLFieldList", Gt);
z("TLAudioRecorder", qt);
z("TLAudioPlayer", Qt);
z("TLFileUpload", en);
z("TLDownload", nn);
z("TLPhotoCapture", rn);
z("TLPhotoViewer", an);
z("TLSplitPanel", sn);
z("TLPanel", hn);
z("TLMaximizeRoot", bn);
z("TLDeckPane", _n);
z("TLSidebar", Sn);
z("TLStack", Nn);
z("TLGrid", Tn);
z("TLCard", Ln);
z("TLAppBar", Rn);
z("TLBreadcrumb", Dn);
z("TLBottomBar", Mn);
z("TLDialog", jn);
z("TLDialogManager", $n);
z("TLWindow", Hn);
z("TLDrawer", Vn);
z("TLContextMenuRegion", Yn);
z("TLSnackbar", Zn);
z("TLMenu", Jn);
z("TLAppShell", el);
z("TLText", tl);
z("TLTableView", ll);
z("TLFormLayout", ul);
z("TLFormGroup", pl);
z("TLFormField", _l);
z("TLResourceCell", gl);
z("TLTreeView", vl);
z("TLDropdownSelect", Ll);
z("TLColorInput", zl);
z("TLIconSelect", Zl);
z("TLDashboard", ar);
z("TLToolbar", dr);
z("TLTileStack", mr);
z("TLSlot", pr);
z("TLSlotContent", fr);
z("TLDrawerToggle", br);
z("TLLogin", _r);
z("TLUserMenu", gr);
