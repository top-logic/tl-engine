import { React as e, useTLFieldValue as Te, getComponent as Nt, useTLState as q, useTLCommand as le, TLChild as K, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as U } from "tl-react-bridge";
const { useCallback: Tt } = e, Rt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Tt(
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
}, { useCallback: xt } = e, Lt = ({ controlId: l, state: t }) => {
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
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), s = Dt(
    (p) => {
      const m = p.target.value;
      i(m === "" ? null : m);
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
}, { useCallback: Mt } = e, Pt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Mt(
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
}, { useCallback: jt } = e, At = ({ controlId: l, state: t, config: n }) => {
  var p;
  const [r, i] = Te(), s = jt(
    (m) => {
      i(m.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((p = c.find((f) => f.value === r)) == null ? void 0 : p.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
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
    c.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Bt } = e, Ot = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Bt(
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
}, $t = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Nt(c.cellModule) : void 0, o = i[c.name];
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
const { useCallback: Ft } = e, Ht = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = le(), o = t ?? "click", a = n ?? c.label, p = r ?? c.image, m = i ?? c.disabled === !0, f = s ?? c.displayMode ?? "label-only", k = c.hidden === !0, h = c.tooltip, S = k ? { display: "none" } : void 0, w = c.appearance, v = c.size, g = c.navigateUrl, D = Ft(() => {
    if (g) {
      window.location.assign(g);
      return;
    }
    u(o);
  }, [u, o, g]), L = f === "icon-only", _ = f === "icon-only" || f === "icon-label", b = f === "label-only" || f === "icon-label" || L && !p, E = h ?? (L ? a : void 0), H = E ? `text:${E}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: D,
      disabled: m,
      style: S,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (v === "small" ? " tlReactButton--small" : "") + (v === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": H,
      "aria-label": L ? a : void 0
    },
    _ && p && /* @__PURE__ */ e.createElement(Se, { encoded: p, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Wt } = e, zt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = le(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, p = i ?? s.disabled === !0, m = Wt(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: m,
      disabled: p,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    o
  );
}, Ut = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Vt } = e, Kt = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.tabs ?? [], i = t.activeTabId, s = Vt((c) => {
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
}, Yt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: i })))));
}, Gt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Xt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), p = t.status ?? "idle", m = t.error, f = p === "received" ? "idle" : r !== "idle" ? r : p, k = e.useCallback(async () => {
    if (r === "recording") {
      const g = u.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (r !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = g, o.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(g, D ? { mimeType: D } : void 0);
        u.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && o.current.push(_.data);
        }, L.onstop = async () => {
          g.getTracks().forEach((E) => E.stop()), a.current = null;
          const _ = new Blob(o.current, { type: L.mimeType || "audio/webm" });
          if (o.current = [], _.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const b = new FormData();
          b.append("audio", _, "recording.webm"), await n(b), i("idle");
        }, L.start(), i("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), h = ae(Gt), S = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], w = f === "uploading", v = ["tlAudioRecorder__button"];
  return f === "recording" && v.push("tlAudioRecorder__button--recording"), f === "uploading" && v.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: k,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[s]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, qt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Zt = ({ controlId: l }) => {
  const t = q(), n = et(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
  e.useEffect(() => {
    r ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), c("disabled"));
  }, [r]), e.useEffect(() => {
    i !== a.current && (a.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const p = e.useCallback(async () => {
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
        const v = await w.blob();
        o.current = URL.createObjectURL(v);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), m = ae(qt), f = s === "loading" ? m["js.loading"] : s === "playing" ? m["js.audioPlayer.pause"] : s === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], k = s === "disabled" || s === "loading", h = ["tlAudioPlayer__button"];
  return s === "playing" && h.push("tlAudioPlayer__button--playing"), s === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: p,
      disabled: k,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Qt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Jt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, p = t.accept ?? "", m = o === "received" ? "idle" : r !== "idle" ? r : o, f = e.useCallback(async (_) => {
    i("uploading");
    const b = new FormData();
    b.append("file", _, _.name), await n(b), i("idle");
  }, [n]), k = e.useCallback((_) => {
    var E;
    const b = (E = _.target.files) == null ? void 0 : E[0];
    b && f(b);
  }, [f]), h = e.useCallback(() => {
    var _;
    r !== "uploading" && ((_ = u.current) == null || _.click());
  }, [r]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!0);
  }, []), w = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), v = e.useCallback((_) => {
    var E;
    if (_.preventDefault(), _.stopPropagation(), c(!1), r === "uploading") return;
    const b = (E = _.dataTransfer.files) == null ? void 0 : E[0];
    b && f(b);
  }, [r, f]), g = m === "uploading", D = ae(Qt), L = m === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: v
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: p || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: h,
        disabled: g,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, en = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, tn = ({ controlId: l }) => {
  const t = q(), n = et(), r = le(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const h = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(h);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), v = URL.createObjectURL(w), g = document.createElement("a");
        g.href = v, g.download = c, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(v);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), m = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), f = ae(en);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const k = o ? f["js.downloading"] : f["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
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
      onClick: m,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, nn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, ln = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), p = e.useRef(null), m = e.useRef(null), f = e.useRef(null), k = e.useRef(null), h = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((R) => R.stop()), p.current = null), a.current && (a.current.srcObject = null);
  }, []), v = e.useCallback(() => {
    w(), i("idle");
  }, [w]), g = e.useCallback(async () => {
    var R;
    if (r !== "uploading") {
      if (c(null), !S) {
        (R = f.current) == null || R.click();
        return;
      }
      try {
        const V = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = V, i("overlayOpen");
      } catch (V) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", V), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, S]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const R = a.current, V = m.current;
    if (!R || !V)
      return;
    V.width = R.videoWidth, V.height = R.videoHeight;
    const A = V.getContext("2d");
    A && (A.drawImage(R, 0, 0), w(), i("uploading"), V.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", T, "capture.jpg"), await n($), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), L = e.useCallback(async (R) => {
    var T;
    const V = (T = R.target.files) == null ? void 0 : T[0];
    if (!V) return;
    i("uploading");
    const A = new FormData();
    A.append("photo", V, V.name), await n(A), i("idle"), f.current && (f.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && p.current && (a.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var V;
    if (r !== "overlayOpen") return;
    (V = k.current) == null || V.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const R = (V) => {
      V.key === "Escape" && v();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [r, v]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((R) => R.stop()), p.current = null);
  }, []);
  const _ = ae(nn), b = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  u && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return u && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: g,
      disabled: r === "uploading",
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !S && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
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
        className: H.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: P.join(" "),
        onClick: () => o((R) => !R),
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: v,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[s]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, rn = {
  "js.photoViewer.alt": "Captured photo"
}, on = ({ controlId: l }) => {
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
        const p = await fetch(n);
        if (!p.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", p.status);
          return;
        }
        const m = await p.blob();
        a || c(URL.createObjectURL(m));
      } catch (p) {
        console.error("[TLPhotoViewer] Fetch error:", p);
      }
    })(), () => {
      a = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const o = ae(rn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, an = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), o = !u && s.some((w) => w.collapsed), a = u ? !c : c, p = Fe(null), m = Fe(null), f = Fe(null), k = ot((w, v) => {
    const g = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !a ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : v !== void 0 ? g.flex = `0 0 ${v}px` : g.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (g.minWidth = c ? w.minSize : void 0, g.minHeight = c ? void 0 : w.minSize), g;
  }, [c, u, o, a]), h = ot((w, v) => {
    w.preventDefault();
    const g = p.current;
    if (!g) return;
    const D = s[v], L = s[v + 1], _ = g.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((P) => {
      b.push(c ? P.offsetWidth : P.offsetHeight);
    }), f.current = b, m.current = {
      splitterIndex: v,
      startPos: c ? w.clientX : w.clientY,
      startSizeBefore: b[v],
      startSizeAfter: b[v + 1],
      childBefore: D,
      childAfter: L
    };
    const E = (P) => {
      const R = m.current;
      if (!R || !f.current) return;
      const A = (c ? P.clientX : P.clientY) - R.startPos, T = R.childBefore.minSize || 0, $ = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + A, F = R.startSizeAfter - A;
      ee < T && (F += ee - T, ee = T), F < $ && (ee += F - $, F = $), f.current[R.splitterIndex] = ee, f.current[R.splitterIndex + 1] = F;
      const Z = g.querySelectorAll(":scope > .tlSplitPanel__child"), B = Z[R.splitterIndex], j = Z[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${F}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const P = {};
        s.forEach((R, V) => {
          const A = R.control;
          A != null && A.controlId && f.current && (P[A.controlId] = f.current[V]);
        }), n("updateSizes", { sizes: P });
      }
      f.current = null, m.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", H), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((w, v) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${v}`,
          className: `tlSplitPanel__child${w.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(w)
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control })
      )
    ), i && v < s.length - 1) {
      const g = s[v + 1];
      !w.collapsed && !g.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${v}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (L) => h(L, v)
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
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, { useCallback: He } = e, sn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), un = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), mn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), pn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), fn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(sn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, p = s === "MINIMIZED", m = s === "MAXIMIZED", f = s === "HIDDEN", k = He(() => {
    n("toggleMinimize");
  }, [n]), h = He(() => {
    n("toggleMaximize");
  }, [n]), S = He(() => {
    n("popOut");
  }, [n]);
  if (f)
    return null;
  const w = m ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: w
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(K, { control: t.toolbar }), c && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: k,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(un, null) : /* @__PURE__ */ e.createElement(cn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: m ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      m ? /* @__PURE__ */ e.createElement(mn, null) : /* @__PURE__ */ e.createElement(dn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(pn, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !p && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, hn = ({ controlId: l }) => {
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
}, bn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ue, useState: Be, useEffect: Oe, useRef: $e } = e, _n = {
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
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, vn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
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
), En = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), gn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Cn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), yn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = $e(null);
  Oe(() => {
    const a = (p) => {
      c.current && !c.current.contains(p.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [s]), Oe(() => {
    const a = (p) => {
      p.key === "Escape" && s();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [s]);
  const u = ue((a) => {
    a.type === "nav" ? (r(a.id), s()) : a.type === "command" && (i(a.id), s());
  }, [r, i, s]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
    if (a.type === "nav" && a.hidden) return null;
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
}, wn = ({
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
  focusedId: p,
  setItemRef: m,
  onItemFocus: f,
  flyoutGroupId: k,
  onOpenFlyout: h,
  onCloseFlyout: S
}) => {
  const w = $e(null), [v, g] = Be(null), D = ue(() => {
    r ? k === l.id ? S() : (w.current && g(w.current.getBoundingClientRect()), h(l.id)) : c(l.id);
  }, [r, k, l.id, c, h, S]), L = ue((b) => {
    w.current = b, o(b);
  }, [o]), _ = r && k === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: r ? l.label : void 0,
      "aria-expanded": r ? _ : t,
      tabIndex: u,
      ref: L,
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
    yn,
    {
      item: l,
      activeItemId: n,
      anchorRect: v,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((b) => /* @__PURE__ */ e.createElement(
    Et,
    {
      key: b.id,
      item: b,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
      onToggleGroup: c,
      focusedId: p,
      setItemRef: m,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: k,
      onOpenFlyout: h,
      onCloseFlyout: S
    }
  ))));
}, Et = ({
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
  flyoutGroupId: p,
  onOpenFlyout: m,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        vn,
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
        En,
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
      return /* @__PURE__ */ e.createElement(gn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Cn, null);
    case "group": {
      const k = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        wn,
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
          flyoutGroupId: p,
          onOpenFlyout: m,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, kn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(_n), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, p] = Be(() => {
    const T = /* @__PURE__ */ new Map(), $ = (ee) => {
      for (const F of ee)
        F.type === "group" && (T.set(F.id, F.expanded), $(F.children));
    };
    return $(i), T;
  }), m = ue((T) => {
    p(($) => {
      const ee = new Map($), F = ee.get(T) ?? !1;
      return ee.set(T, !F), n("toggleGroup", { itemId: T, expanded: !F }), ee;
    });
  }, [n]), f = ue((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), k = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), h = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [w, v] = Be(null), g = ue((T) => {
    v(T);
  }, []), D = ue(() => {
    v(null);
  }, []);
  Oe(() => {
    o || v(null);
  }, [o]);
  const [L, _] = Be(() => {
    const T = qe(i, o, a);
    return T.length > 0 ? T[0].id : "";
  }), b = $e(/* @__PURE__ */ new Map()), E = ue((T) => ($) => {
    $ ? b.current.set(T, $) : b.current.delete(T);
  }, []), H = ue((T) => {
    _(T);
  }, []), P = $e(0), R = ue((T) => {
    _(T), P.current++;
  }, []);
  Oe(() => {
    const T = b.current.get(L);
    T && document.activeElement !== T && T.focus();
  }, [L, P.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), D();
      return;
    }
    const $ = qe(i, o, a);
    if ($.length === 0) return;
    const ee = $.findIndex((Z) => Z.id === L);
    if (ee < 0) return;
    const F = $[ee];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const Z = (ee + 1) % $.length;
        R($[Z].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const Z = (ee - 1 + $.length) % $.length;
        R($[Z].id);
        break;
      }
      case "Home": {
        T.preventDefault(), R($[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), R($[$.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), F.type === "nav" ? f(F.id) : F.type === "command" ? k(F.id) : F.type === "group" && (o ? w === F.id ? D() : g(F.id) : m(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !o && ((a.get(F.id) ?? !1) || (T.preventDefault(), m(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !o && (a.get(F.id) ?? !1) && (T.preventDefault(), m(F.id));
        break;
      }
    }
  }, [
    i,
    o,
    a,
    L,
    w,
    R,
    f,
    k,
    m,
    g,
    D
  ]), A = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, i.map((T) => /* @__PURE__ */ e.createElement(
    Et,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: o,
      onSelect: f,
      onExecute: k,
      onToggleGroup: m,
      focusedId: L,
      setItemRef: E,
      onItemFocus: H,
      groupStates: a,
      flyoutGroupId: w,
      onOpenFlyout: g,
      onCloseFlyout: D
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
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
}, Sn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, c.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })));
}, Nn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: u })));
}, Tn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(K, { control: c })));
}, Rn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, p) => /* @__PURE__ */ e.createElement(K, { key: p, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, p) => /* @__PURE__ */ e.createElement(K, { key: p, control: a }))));
}, { useCallback: xn } = e, Ln = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = xn((s) => {
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
}, { useCallback: Dn } = e, In = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = t.activeItemId, s = Dn((c) => {
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
}, { useCallback: at, useEffect: st, useRef: Mn } = e, Pn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Mn(null), u = at(() => {
    n("close");
  }, [n]), o = at((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!r) return;
    const a = (p) => {
      p.key === "Escape" && u();
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
}, { useEffect: jn, useRef: An } = e, Bn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = An(n.length);
  return jn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(K, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, On = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, $n = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Fn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(On), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, p = t.actions ?? [], m = t.toolbar, f = t.buttonBar, [k, h] = Ie(null), [S, w] = Ie(null), [v, g] = Ie(null), D = Ce(null), [L, _] = Ie(!1), b = Ce(null), E = Ce(null), H = Ce(null), P = Ce(null), R = Ce(null), V = De(() => {
    n("close");
  }, [n]), A = De((Z, B) => {
    B.preventDefault();
    const j = P.current;
    if (!j) return;
    const Y = j.getBoundingClientRect(), d = !D.current, y = D.current ?? { x: Y.left, y: Y.top };
    d && (D.current = y, g(y)), R.current = {
      dir: Z,
      startX: B.clientX,
      startY: B.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...y },
      symmetric: d
    };
    const W = (G) => {
      const M = R.current;
      if (!M) return;
      const Q = G.clientX - M.startX, ne = G.clientY - M.startY;
      let te = M.startW, ie = M.startH, de = 0, me = 0;
      M.symmetric ? (M.dir.includes("e") && (te = M.startW + 2 * Q), M.dir.includes("w") && (te = M.startW - 2 * Q), M.dir.includes("s") && (ie = M.startH + 2 * ne), M.dir.includes("n") && (ie = M.startH - 2 * ne)) : (M.dir.includes("e") && (te = M.startW + Q), M.dir.includes("w") && (te = M.startW - Q, de = Q), M.dir.includes("s") && (ie = M.startH + ne), M.dir.includes("n") && (ie = M.startH - ne, me = ne));
      const he = Math.max(200, te), be = Math.max(100, ie);
      M.symmetric ? (de = (M.startW - he) / 2, me = (M.startH - be) / 2) : (M.dir.includes("w") && he === 200 && (de = M.startW - 200), M.dir.includes("n") && be === 100 && (me = M.startH - 100)), E.current = he, H.current = be, h(he), w(be);
      const N = {
        x: M.startPos.x + de,
        y: M.startPos.y + me
      };
      D.current = N, g(N);
    }, O = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", O);
      const G = E.current, M = H.current;
      (G != null || M != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...M != null ? { height: Math.round(M) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", O);
  }, [n]), T = De((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const B = P.current;
    if (!B) return;
    const j = B.getBoundingClientRect(), Y = D.current ?? { x: j.left, y: j.top }, d = Z.clientX - Y.x, y = Z.clientY - Y.y, W = (G) => {
      const M = window.innerWidth, Q = window.innerHeight;
      let ne = G.clientX - d, te = G.clientY - y;
      const ie = B.offsetWidth, de = B.offsetHeight;
      ne + ie > M && (ne = M - ie), te + de > Q && (te = Q - de), ne < 0 && (ne = 0), te < 0 && (te = 0);
      const me = { x: ne, y: te };
      D.current = me, g(me);
    }, O = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", O);
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", O);
  }, []), $ = De(() => {
    var Z, B;
    if (L) {
      const j = b.current;
      j && (g(j.x !== -1 ? { x: j.x, y: j.y } : null), h(j.w), w(j.h)), _(!1);
    } else {
      const j = P.current, Y = j == null ? void 0 : j.getBoundingClientRect();
      b.current = {
        x: ((Z = D.current) == null ? void 0 : Z.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((B = D.current) == null ? void 0 : B.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: k ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, _(!0), g({ x: 0, y: 0 }), h(null), w(null);
    }
  }, [L, k, S]), ee = L ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: k != null ? k + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: v ? "100vh" : "80vh",
    ...v ? { position: "absolute", left: v.x + "px", top: v.y + "px" } : {}
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
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : T,
        onDoubleClick: o ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: m })),
      o && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: $,
          title: L ? r["js.window.restore"] : r["js.window.maximize"]
        },
        L ? (
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
    (p.length > 0 || f) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, f && /* @__PURE__ */ e.createElement(K, { control: f }), p.map((Z, B) => /* @__PURE__ */ e.createElement(K, { key: B, control: Z }))),
    o && !L && $n.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (B) => A(Z, B)
      }
    ))
  );
}, { useCallback: Hn, useEffect: Wn } = e, zn = {
  "js.drawer.close": "Close"
}, Un = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(zn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Hn(() => {
    n("close");
  }, [n]);
  Wn(() => {
    if (!i) return;
    const m = (f) => {
      f.key === "Escape" && a();
    };
    return document.addEventListener("keydown", m), () => document.removeEventListener("keydown", m);
  }, [i, a]);
  const p = [
    "tlDrawer",
    `tlDrawer--${s}`,
    `tlDrawer--${c}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: p, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
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
}, { useCallback: Vn } = e, Kn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.child, i = Vn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, { useCallback: Yn, useEffect: Gn, useState: Xn } = e, qn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, p] = Xn(!1), m = Yn(() => {
    p(!0), setTimeout(() => {
      n("dismiss", { generation: o }), p(!1);
    }, 200);
  }, [n, o]);
  return Gn(() => {
    if (!u || c === 0) return;
    const f = setTimeout(m, c);
    return () => clearTimeout(f);
  }, [u, c, m]), !u && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${s}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: We, useEffect: ze, useRef: Zn, useState: ct } = e, Qn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = Zn(null), [a, p] = ct({ top: 0, left: 0 }), [m, f] = ct(0), k = u.filter((v) => v.type === "item" && !v.disabled);
  ze(() => {
    var E, H;
    if (!r) return;
    const v = ((E = o.current) == null ? void 0 : E.offsetHeight) ?? 200, g = ((H = o.current) == null ? void 0 : H.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let P = c, R = s;
      P + v > window.innerHeight && (P = Math.max(0, window.innerHeight - v)), R + g > window.innerWidth && (R = Math.max(0, window.innerWidth - g)), p({ top: P, left: R }), f(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const L = D.getBoundingClientRect();
    let _ = L.bottom + 4, b = L.left;
    _ + v > window.innerHeight && (_ = L.top - v - 4), b + g > window.innerWidth && (b = L.right - g), p({ top: _, left: b }), f(0);
  }, [r, i, s, c]);
  const h = We(() => {
    n("close");
  }, [n]), S = We((v) => {
    n("selectItem", { itemId: v });
  }, [n]);
  ze(() => {
    if (!r) return;
    const v = (g) => {
      o.current && !o.current.contains(g.target) && h();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [r, h]);
  const w = We((v) => {
    if (v.key === "Escape") {
      h();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), f((g) => (g + 1) % k.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), f((g) => (g - 1 + k.length) % k.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const g = k[m];
      g && S(g.id);
    }
  }, [h, S, k, m]);
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
    u.map((v, g) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: g, className: "tlMenu__separator" });
      const L = k.indexOf(v) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, Jn = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: i })), /* @__PURE__ */ e.createElement(K, { control: s }), c && /* @__PURE__ */ e.createElement(K, { control: c }), u && /* @__PURE__ */ e.createElement(K, { control: u }));
}, el = ({ controlId: l }) => {
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
}, tl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, nl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(tl), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const I = (C) => {
      const x = C.detail;
      let z = x.target;
      for (; z && z !== N; ) {
        const J = z.dataset.row, re = z.dataset.col;
        if (J != null && re != null) {
          x.resolved = { key: J + "|" + re };
          return;
        }
        z = z.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", I), () => N.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", p = t.selectedCount ?? 0, m = t.frozenColumnCount ?? 0, f = t.treeMode ?? !1, k = e.useMemo(
    () => s.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [s]
  ), h = a === "multi", S = 40, w = 20, v = e.useRef(null), g = e.useRef(null), D = e.useRef(null), [L, _] = e.useState({}), b = e.useRef(null), E = e.useRef(!1), H = e.useRef(null), [P, R] = e.useState(null), [V, A] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [s]);
  const T = e.useCallback((N) => L[N.name] ?? N.width, [L]), $ = e.useMemo(() => {
    const N = [];
    let I = h && m > 0 ? S : 0;
    for (let C = 0; C < m && C < s.length; C++)
      N.push(I), I += T(s[C]);
    return N;
  }, [s, m, h, S, T]), ee = c * o, F = e.useRef(null), Z = e.useCallback((N, I, C) => {
    C.preventDefault(), C.stopPropagation(), b.current = { column: N, startX: C.clientX, startWidth: I };
    let x = C.clientX, z = 0;
    const J = () => {
      const oe = b.current;
      if (!oe) return;
      const pe = Math.max(it, oe.startWidth + (x - oe.startX) + z);
      _((ge) => ({ ...ge, [oe.column]: pe }));
    }, re = () => {
      const oe = g.current, pe = v.current;
      if (!oe || !b.current) return;
      const ge = oe.getBoundingClientRect(), nt = 40, lt = 8, St = oe.scrollLeft;
      x > ge.right - nt ? oe.scrollLeft += lt : x < ge.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - St;
      rt !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), z += rt, J()), F.current = requestAnimationFrame(re);
    };
    F.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      x = oe.clientX, J();
    }, Le = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", Le), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = b.current;
      if (pe) {
        const ge = Math.max(it, pe.startWidth + (oe.clientX - pe.startX) + z);
        n("columnResize", { column: pe.column, width: ge }), b.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", Le);
  }, [n]), B = e.useCallback(() => {
    v.current && g.current && (v.current.scrollLeft = g.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const N = g.current;
      if (!N) return;
      const I = N.scrollTop, C = Math.ceil(N.clientHeight / o), x = Math.floor(I / o);
      n("scroll", { start: x, count: C });
    }, 80);
  }, [n, o]), j = e.useCallback((N, I, C) => {
    if (E.current) return;
    let x;
    !I || I === "desc" ? x = "asc" : x = "desc";
    const z = C.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: x, mode: z });
  }, [n]), Y = e.useCallback((N, I) => {
    H.current = N, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, I) => {
    if (!H.current || H.current === N) {
      R(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const C = I.currentTarget.getBoundingClientRect(), x = I.clientX < C.left + C.width / 2 ? "left" : "right";
    R({ column: N, side: x });
  }, []), y = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const I = H.current;
    if (!I || !P) {
      H.current = null, R(null);
      return;
    }
    let C = s.findIndex((z) => z.name === P.column);
    if (C < 0) {
      H.current = null, R(null);
      return;
    }
    const x = s.findIndex((z) => z.name === I);
    P.side === "right" && C++, x < C && C--, n("columnReorder", { column: I, targetIndex: C }), H.current = null, R(null);
  }, [s, P, n]), W = e.useCallback(() => {
    H.current = null, R(null);
  }, []), O = e.useCallback((N, I) => {
    I.shiftKey && I.preventDefault(), n("select", {
      rowIndex: N,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [n]), G = e.useCallback((N, I) => {
    I.stopPropagation(), n("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [n]), M = e.useCallback(() => {
    const N = p === c && c > 0;
    n("selectAll", { selected: !N });
  }, [n, p, c]), Q = e.useCallback((N, I, C) => {
    C.stopPropagation(), n("expand", { rowIndex: N, expanded: I });
  }, [n]), ne = e.useCallback((N, I) => {
    I.preventDefault(), A({ x: I.clientX, y: I.clientY, colIdx: N });
  }, []), te = e.useCallback(() => {
    V && (n("setFrozenColumnCount", { count: V.colIdx + 1 }), A(null));
  }, [V, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), A(null);
  }, [n]);
  e.useEffect(() => {
    if (!V) return;
    const N = () => A(null), I = (C) => {
      C.key === "Escape" && A(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", I);
    };
  }, [V]);
  const de = s.reduce((N, I) => N + T(I), 0) + (h ? S : 0), me = p === c && c > 0, he = p > 0 && p < c, be = e.useCallback((N) => {
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
        if (!H.current) return;
        N.preventDefault();
        const I = g.current, C = v.current;
        if (!I) return;
        const x = I.getBoundingClientRect(), z = 40, J = 8;
        N.clientX < x.left + z ? I.scrollLeft = Math.max(0, I.scrollLeft - J) : N.clientX > x.right - z && (I.scrollLeft += J), C && (C.scrollLeft = I.scrollLeft);
      },
      onDrop: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, h && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== H.current && R({ column: s[0].name, side: "left" }));
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
      const C = T(N);
      s.length - 1;
      let x = "tlTableView__headerCell";
      N.sortable && (x += " tlTableView__headerCell--sortable"), P && P.column === N.name && (x += " tlTableView__headerCell--dragOver-" + P.side);
      const z = I < m, J = I === m - 1;
      return z && (x += " tlTableView__headerCell--frozen"), J && (x += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: x,
          style: {
            width: C,
            minWidth: C,
            position: z ? "sticky" : "relative",
            ...z ? { left: $[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (re) => j(N.name, N.sortDirection, re) : void 0,
          onContextMenu: (re) => ne(I, re),
          onDragStart: (re) => Y(N.name, re),
          onDragOver: (re) => d(N.name, re),
          onDrop: y,
          onDragEnd: W
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", k > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => Z(N.name, C, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (H.current && s.length > 0) {
            const I = s[s.length - 1];
            I.name !== H.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", R({ column: I.name, side: "right" }));
          }
        },
        onDrop: y
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: g,
        className: "tlTableView__body",
        onScroll: B
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
          onClick: (I) => O(N.index, I)
        },
        h && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (m > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: S,
              minWidth: S,
              ...m > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
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
        s.map((I, C) => {
          const x = T(I), z = C === s.length - 1, J = C < m, re = C === m - 1;
          let Ee = "tlTableView__cell";
          J && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const Le = f && C === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Ee,
              "data-row": N.id,
              "data-col": I.name,
              style: {
                ...z && !J ? { flex: "1 0 auto", minWidth: x } : { width: x, minWidth: x },
                ...J ? { position: "sticky", left: $[C], zIndex: 2 } : {}
              }
            },
            Le ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * w } }, N.expandable ? /* @__PURE__ */ e.createElement(
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
      V.colIdx + 1 !== m && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      m > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, ll = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, gt = e.createContext(ll), { useMemo: rl, useRef: ol, useState: al, useEffect: sl } = e, cl = 320, il = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = ol(null), [o, a] = al(
    r === "top" ? "top" : "side"
  );
  sl(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const h = u.current;
    if (!h) return;
    const S = new ResizeObserver((w) => {
      for (const v of w) {
        const D = v.contentRect.width / n;
        a(D < cl ? "top" : "side");
      }
    });
    return S.observe(h), () => S.disconnect();
  }, [r, n]);
  const p = rl(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, k = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(gt.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: l, className: k, style: f, ref: u }, s.map((h, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: h }))));
}, { useCallback: ul } = e, dl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ml = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(dl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, p = t.children ?? [], m = i != null || s.length > 0 || c, f = ul(() => {
    n("toggleCollapse");
  }, [n]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: k }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((h, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((h, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: h }))));
}, { useContext: pl, useState: fl, useCallback: hl } = e, bl = ({ controlId: l }) => {
  const t = q(), n = pl(gt), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, m = t.visible !== !1, f = t.hasTooltip === !0, k = t.field, h = n.readOnly, [S, w] = fl(!1), v = hl(() => w((_) => !_), []);
  if (!m) return null;
  const g = s != null, D = c != null && c.length > 0, L = [
    "tlFormField",
    `tlFormField--${a}`,
    h ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    g ? "tlFormField--error" : "",
    !g && D ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": f ? "key:tooltip" : void 0
    },
    r
  ), i && !h && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), o && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !h && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: k })), !h && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !h && !g && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, _)))), !h && u && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, _l = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, p = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), m = e.useCallback((h) => {
    h.preventDefault(), n("goto", {});
  }, [n]), f = ["tlResourceCell", c].filter(Boolean).join(" "), k = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: f,
      href: "#",
      onClick: m,
      "data-tooltip": k
    },
    p
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: f, "data-tooltip": k }, p);
}, vl = 20, El = () => {
  const l = q(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), p = e.useRef(null), m = e.useCallback((_, b) => {
    t(b ? "collapse" : "expand", { nodeId: _ });
  }, [t]), f = e.useCallback((_, b) => {
    t("select", {
      nodeId: _,
      ctrlKey: b.ctrlKey || b.metaKey,
      shiftKey: b.shiftKey
    });
  }, [t]), k = e.useCallback((_, b) => {
    b.preventDefault(), t("contextMenu", { nodeId: _, x: b.clientX, y: b.clientY });
  }, [t]), h = e.useRef(null), S = e.useCallback((_, b) => {
    const E = b.getBoundingClientRect(), H = _.clientY - E.top, P = E.height / 3;
    return H < P ? "above" : H > P * 2 ? "below" : "within";
  }, []), w = e.useCallback((_, b) => {
    b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", _);
  }, []), v = e.useCallback((_, b) => {
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const E = S(b, b.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: E }), h.current = null;
    }, 50);
  }, [t, S]), g = e.useCallback((_, b) => {
    b.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const E = S(b, b.currentTarget);
    t("drop", { nodeId: _, position: E });
  }, [t, S]), D = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), L = e.useCallback((_) => {
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
          const E = n[o];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (b = o + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), o >= 0 && o < n.length) {
          const E = n[o];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const H = E.depth;
            for (let P = o - 1; P >= 0; P--)
              if (n[P].depth < H) {
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
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: L
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
        style: { paddingLeft: _.depth * vl },
        draggable: i,
        onClick: (E) => f(_.id, E),
        onContextMenu: (E) => k(_.id, E),
        onDragStart: (E) => w(_.id, E),
        onDragOver: s ? (E) => v(_.id, E) : void 0,
        onDrop: s ? (E) => g(_.id, E) : void 0,
        onDragEnd: D
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), m(_.id, _.expanded);
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
function gl() {
  if (ut) return X;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), f = Symbol.iterator;
  function k(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var h = {
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
  function v(d, y, W) {
    this.props = d, this.context = y, this.refs = w, this.updater = W || h;
  }
  v.prototype.isReactComponent = {}, v.prototype.setState = function(d, y) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, y, "setState");
  }, v.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function g() {
  }
  g.prototype = v.prototype;
  function D(d, y, W) {
    this.props = d, this.context = y, this.refs = w, this.updater = W || h;
  }
  var L = D.prototype = new g();
  L.constructor = D, S(L, v.prototype), L.isPureReactComponent = !0;
  var _ = Array.isArray;
  function b() {
  }
  var E = { H: null, A: null, T: null, S: null }, H = Object.prototype.hasOwnProperty;
  function P(d, y, W) {
    var O = W.ref;
    return {
      $$typeof: l,
      type: d,
      key: y,
      ref: O !== void 0 ? O : null,
      props: W
    };
  }
  function R(d, y) {
    return P(d.type, y, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var y = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(W) {
      return y[W];
    });
  }
  var T = /\/+/g;
  function $(d, y) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : y.toString(36);
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
  function F(d, y, W, O, G) {
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
            case p:
              return Q = d._init, F(
                Q(d._payload),
                y,
                W,
                O,
                G
              );
          }
      }
    if (Q)
      return G = G(d), Q = O === "" ? "." + $(d, 0) : O, _(G) ? (W = "", Q != null && (W = Q.replace(T, "$&/") + "/"), F(G, y, W, "", function(ie) {
        return ie;
      })) : G != null && (V(G) && (G = R(
        G,
        W + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), y.push(G)), 1;
    Q = 0;
    var ne = O === "" ? "." : O + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        O = d[te], M = ne + $(O, te), Q += F(
          O,
          y,
          W,
          M,
          G
        );
    else if (te = k(d), typeof te == "function")
      for (d = te.call(d), te = 0; !(O = d.next()).done; )
        O = O.value, M = ne + $(O, te++), Q += F(
          O,
          y,
          W,
          M,
          G
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return F(
          ee(d),
          y,
          W,
          O,
          G
        );
      throw y = String(d), Error(
        "Objects are not valid as a React child (found: " + (y === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : y) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, y, W) {
    if (d == null) return d;
    var O = [], G = 0;
    return F(d, O, "", "", function(M) {
      return y.call(W, M, G++);
    }), O;
  }
  function B(d) {
    if (d._status === -1) {
      var y = d._result;
      y = y(), y.then(
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = W);
        },
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = W);
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
    forEach: function(d, y, W) {
      Z(
        d,
        function() {
          y.apply(this, arguments);
        },
        W
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
  return X.Activity = m, X.Children = Y, X.Component = v, X.Fragment = n, X.Profiler = i, X.PureComponent = D, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, X.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, X.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, X.cacheSignal = function() {
    return null;
  }, X.cloneElement = function(d, y, W) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var O = S({}, d.props), G = d.key;
    if (y != null)
      for (M in y.key !== void 0 && (G = "" + y.key), y)
        !H.call(y, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && y.ref === void 0 || (O[M] = y[M]);
    var M = arguments.length - 2;
    if (M === 1) O.children = W;
    else if (1 < M) {
      for (var Q = Array(M), ne = 0; ne < M; ne++)
        Q[ne] = arguments[ne + 2];
      O.children = Q;
    }
    return P(d.type, G, O);
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
  }, X.createElement = function(d, y, W) {
    var O, G = {}, M = null;
    if (y != null)
      for (O in y.key !== void 0 && (M = "" + y.key), y)
        H.call(y, O) && O !== "key" && O !== "__self" && O !== "__source" && (G[O] = y[O]);
    var Q = arguments.length - 2;
    if (Q === 1) G.children = W;
    else if (1 < Q) {
      for (var ne = Array(Q), te = 0; te < Q; te++)
        ne[te] = arguments[te + 2];
      G.children = ne;
    }
    if (d && d.defaultProps)
      for (O in Q = d.defaultProps, Q)
        G[O] === void 0 && (G[O] = Q[O]);
    return P(d, M, G);
  }, X.createRef = function() {
    return { current: null };
  }, X.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, X.isValidElement = V, X.lazy = function(d) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, X.memo = function(d, y) {
    return {
      $$typeof: a,
      type: d,
      compare: y === void 0 ? null : y
    };
  }, X.startTransition = function(d) {
    var y = E.T, W = {};
    E.T = W;
    try {
      var O = d(), G = E.S;
      G !== null && G(W, O), typeof O == "object" && O !== null && typeof O.then == "function" && O.then(b, j);
    } catch (M) {
      j(M);
    } finally {
      y !== null && W.types !== null && (y.types = W.types), E.T = y;
    }
  }, X.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, X.use = function(d) {
    return E.H.use(d);
  }, X.useActionState = function(d, y, W) {
    return E.H.useActionState(d, y, W);
  }, X.useCallback = function(d, y) {
    return E.H.useCallback(d, y);
  }, X.useContext = function(d) {
    return E.H.useContext(d);
  }, X.useDebugValue = function() {
  }, X.useDeferredValue = function(d, y) {
    return E.H.useDeferredValue(d, y);
  }, X.useEffect = function(d, y) {
    return E.H.useEffect(d, y);
  }, X.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, X.useId = function() {
    return E.H.useId();
  }, X.useImperativeHandle = function(d, y, W) {
    return E.H.useImperativeHandle(d, y, W);
  }, X.useInsertionEffect = function(d, y) {
    return E.H.useInsertionEffect(d, y);
  }, X.useLayoutEffect = function(d, y) {
    return E.H.useLayoutEffect(d, y);
  }, X.useMemo = function(d, y) {
    return E.H.useMemo(d, y);
  }, X.useOptimistic = function(d, y) {
    return E.H.useOptimistic(d, y);
  }, X.useReducer = function(d, y, W) {
    return E.H.useReducer(d, y, W);
  }, X.useRef = function(d) {
    return E.H.useRef(d);
  }, X.useState = function(d) {
    return E.H.useState(d);
  }, X.useSyncExternalStore = function(d, y, W) {
    return E.H.useSyncExternalStore(
      d,
      y,
      W
    );
  }, X.useTransition = function() {
    return E.H.useTransition();
  }, X.version = "19.2.4", X;
}
var dt;
function Cl() {
  return dt || (dt = 1, Ve.exports = gl()), Ve.exports;
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
function yl() {
  if (mt) return se;
  mt = 1;
  var l = Cl();
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
  function s(o, a, p) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: m == null ? null : "" + m,
      children: o,
      containerInfo: a,
      implementation: p
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, a) {
    if (o === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(o, a) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return s(o, a, null, p);
  }, se.flushSync = function(o) {
    var a = c.T, p = r.p;
    try {
      if (c.T = null, r.p = 2, o) return o();
    } finally {
      c.T = a, r.p = p, r.d.f();
    }
  }, se.preconnect = function(o, a) {
    typeof o == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, r.d.C(o, a));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && r.d.D(o);
  }, se.preinit = function(o, a) {
    if (typeof o == "string" && a && typeof a.as == "string") {
      var p = a.as, m = u(p, a.crossOrigin), f = typeof a.integrity == "string" ? a.integrity : void 0, k = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      p === "style" ? r.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: m,
          integrity: f,
          fetchPriority: k
        }
      ) : p === "script" && r.d.X(o, {
        crossOrigin: m,
        integrity: f,
        fetchPriority: k,
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
          r.d.M(o, {
            crossOrigin: p,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && r.d.M(o);
  }, se.preload = function(o, a) {
    if (typeof o == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var p = a.as, m = u(p, a.crossOrigin);
      r.d.L(o, p, {
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
        r.d.m(o, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: p,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else r.d.m(o);
  }, se.requestFormReset = function(o) {
    r.d.r(o);
  }, se.unstable_batchedUpdates = function(o, a) {
    return o(a);
  }, se.useFormState = function(o, a, p) {
    return c.H.useFormState(o, a, p);
  }, se.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function wl() {
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
  return l(), Ue.exports = yl(), Ue.exports;
}
var kl = wl();
const { useState: _e, useCallback: ce, useRef: Re, useEffect: ye, useMemo: Ze } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Sl({
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
  const p = ce(
    (m) => {
      m.stopPropagation(), n(l.value);
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
        onClick: p,
        "aria-label": r
      },
      "×"
    )
  );
}
function Nl({
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
const Tl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, p = t.options ?? [], m = t.emptyOptionLabel ?? "", f = s && i && !u && o, k = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = k["js.dropdownSelect.nothingFound"], S = ce(
    (C) => k["js.dropdownSelect.removeChip"].replace("{0}", C),
    [k]
  ), [w, v] = _e(!1), [g, D] = _e(""), [L, _] = _e(-1), [b, E] = _e(!1), [H, P] = _e({}), [R, V] = _e(null), [A, T] = _e(null), [$, ee] = _e(null), F = Re(null), Z = Re(null), B = Re(null), j = Re(r);
  j.current = r;
  const Y = Re(-1), d = Ze(
    () => new Set(r.map((C) => C.value)),
    [r]
  ), y = Ze(() => {
    let C = p.filter((x) => !d.has(x.value));
    if (g) {
      const x = g.toLowerCase();
      C = C.filter((z) => z.label.toLowerCase().includes(x));
    }
    return C;
  }, [p, d, g]);
  ye(() => {
    g && y.length === 1 ? _(0) : _(-1);
  }, [y.length, g]), ye(() => {
    w && a && Z.current && Z.current.focus();
  }, [w, a, r]), ye(() => {
    var z, J;
    if (Y.current < 0) return;
    const C = Y.current;
    Y.current = -1;
    const x = (z = F.current) == null ? void 0 : z.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(C, x.length - 1)].focus() : (J = F.current) == null || J.focus();
  }, [r]), ye(() => {
    if (!w) return;
    const C = (x) => {
      F.current && !F.current.contains(x.target) && B.current && !B.current.contains(x.target) && (v(!1), D(""));
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [w]), ye(() => {
    if (!w || !F.current) return;
    const C = F.current.getBoundingClientRect(), x = window.innerHeight - C.bottom, J = x < 300 && C.top > x;
    P({
      left: C.left,
      width: C.width,
      ...J ? { bottom: window.innerHeight - C.top } : { top: C.bottom }
    });
  }, [w]);
  const W = ce(async () => {
    if (!(u || !o) && (v(!0), D(""), _(-1), E(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, o, a, n]), O = ce(() => {
    var C;
    v(!1), D(""), _(-1), (C = F.current) == null || C.focus();
  }, []), G = ce(
    (C) => {
      let x;
      if (i) {
        const z = p.find((J) => J.value === C);
        if (z)
          x = [...j.current, z];
        else
          return;
      } else {
        const z = p.find((J) => J.value === C);
        if (z)
          x = [z];
        else
          return;
      }
      j.current = x, n("valueChanged", { value: x.map((z) => z.value) }), i ? (D(""), _(-1)) : O();
    },
    [i, p, n, O]
  ), M = ce(
    (C) => {
      Y.current = j.current.findIndex((z) => z.value === C);
      const x = j.current.filter((z) => z.value !== C);
      j.current = x, n("valueChanged", { value: x.map((z) => z.value) });
    },
    [n]
  ), Q = ce(
    (C) => {
      C.stopPropagation(), n("valueChanged", { value: [] }), O();
    },
    [n, O]
  ), ne = ce((C) => {
    D(C.target.value);
  }, []), te = ce(
    (C) => {
      if (!w) {
        if (C.key === "ArrowDown" || C.key === "ArrowUp" || C.key === "Enter" || C.key === " ") {
          if (C.target.tagName === "BUTTON") return;
          C.preventDefault(), C.stopPropagation(), W();
        }
        return;
      }
      switch (C.key) {
        case "ArrowDown":
          C.preventDefault(), C.stopPropagation(), _(
            (x) => x < y.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          C.preventDefault(), C.stopPropagation(), _(
            (x) => x > 0 ? x - 1 : y.length - 1
          );
          break;
        case "Enter":
          C.preventDefault(), C.stopPropagation(), L >= 0 && L < y.length && G(y[L].value);
          break;
        case "Escape":
          C.preventDefault(), C.stopPropagation(), O();
          break;
        case "Tab":
          O();
          break;
        case "Backspace":
          g === "" && i && r.length > 0 && M(r[r.length - 1].value);
          break;
      }
    },
    [
      w,
      W,
      O,
      y,
      L,
      G,
      g,
      i,
      r,
      M
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
  ), de = ce(
    (C, x) => {
      V(C), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(C));
    },
    []
  ), me = ce(
    (C, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", R === null || R === C) {
        T(null), ee(null);
        return;
      }
      const z = x.currentTarget.getBoundingClientRect(), J = z.left + z.width / 2, re = x.clientX < J ? "before" : "after";
      T(C), ee(re);
    },
    [R]
  ), he = ce(
    (C) => {
      if (C.preventDefault(), R === null || A === null || $ === null || R === A) return;
      const x = [...j.current], [z] = x.splice(R, 1);
      let J = A;
      R < A ? J = $ === "before" ? J - 1 : J : J = $ === "before" ? J : J + 1, x.splice(J, 0, z), j.current = x, n("valueChanged", { value: x.map((re) => re.value) }), V(null), T(null), ee(null);
    },
    [R, A, $, n]
  ), be = ce(() => {
    V(null), T(null), ee(null);
  }, []);
  if (ye(() => {
    if (L < 0 || !B.current) return;
    const C = B.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    C && C.scrollIntoView({ block: "nearest" });
  }, [L, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, m) : r.map((C) => /* @__PURE__ */ e.createElement("span", { key: C.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: C.image }), /* @__PURE__ */ e.createElement("span", null, C.label))));
  const N = !c && r.length > 0 && !u, I = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (a || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: g,
        onChange: ne,
        onKeyDown: te,
        placeholder: k["js.dropdownSelect.filterPlaceholder"],
        "aria-label": k["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": L >= 0 ? `${l}-opt-${L}` : void 0,
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
      a && y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      a && y.map((C, x) => /* @__PURE__ */ e.createElement(
        Nl,
        {
          key: C.value,
          id: `${l}-opt-${x}`,
          option: C,
          highlighted: x === L,
          searchTerm: g,
          onSelect: G,
          onMouseEnter: () => _(x)
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
      onClick: w ? void 0 : W,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : r.map((C, x) => {
      let z = "";
      return R === x ? z = "tlDropdownSelect__chip--dragging" : A === x && $ === "before" ? z = "tlDropdownSelect__chip--dropBefore" : A === x && $ === "after" && (z = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Sl,
        {
          key: C.value,
          option: C,
          removable: !u && (i || !c),
          onRemove: M,
          removeLabel: S(C.label),
          draggable: f,
          onDragStart: f ? (J) => de(x, J) : void 0,
          onDragOver: f ? (J) => me(x, J) : void 0,
          onDrop: f ? he : void 0,
          onDragEnd: f ? be : void 0,
          dragClassName: f ? z : void 0
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
  ), I && kl.createPortal(I, document.body));
}, { useCallback: Ke, useRef: Rl } = e, Ct = "application/x-tl-color", xl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Rl(null), u = Ke(
    (p) => (m) => {
      c.current = p, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ke((p) => {
    p.preventDefault(), p.dataTransfer.dropEffect = "move";
  }, []), a = Ke(
    (p) => (m) => {
      m.preventDefault();
      const f = m.dataTransfer.getData(Ct);
      f ? s(p, f) : c.current !== null && c.current !== p && i(c.current, p), c.current = null;
    },
    [i, s]
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
        onDragOver: o,
        onDrop: a(m)
      }
    ))
  );
};
function yt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function wt(l) {
  if (!Qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function kt(l, t, n) {
  const r = (i) => yt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Ll(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const p = c === 0 ? 0 : o / c;
  return [a, p, c];
}
function Dl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function Il(l) {
  return Ll(...wt(l));
}
function Ye(l, t, n) {
  return kt(...Dl(l, t, n));
}
const { useCallback: we, useRef: ft } = e, Ml = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Il(l), s = ft(null), c = ft(null), u = we(
    (h, S) => {
      var D;
      const w = (D = s.current) == null ? void 0 : D.getBoundingClientRect();
      if (!w) return;
      const v = Math.max(0, Math.min(1, (h - w.left) / w.width)), g = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(Ye(n, v, g));
    },
    [n, t]
  ), o = we(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), u(h.clientX, h.clientY);
    },
    [u]
  ), a = we(
    (h) => {
      h.buttons !== 0 && u(h.clientX, h.clientY);
    },
    [u]
  ), p = we(
    (h) => {
      var g;
      const S = (g = c.current) == null ? void 0 : g.getBoundingClientRect();
      if (!S) return;
      const v = Math.max(0, Math.min(1, (h - S.top) / S.height)) * 360;
      t(Ye(v, r, i));
    },
    [r, i, t]
  ), m = we(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), p(h.clientY);
    },
    [p]
  ), f = we(
    (h) => {
      h.buttons !== 0 && p(h.clientY);
    },
    [p]
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
      onPointerDown: m,
      onPointerMove: f
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
function Pl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const jl = {
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
}, { useState: Me, useCallback: fe, useEffect: Ge, useRef: Al, useLayoutEffect: Bl } = e, Ol = ({
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
  const [a, p] = Me("palette"), [m, f] = Me(t), k = Al(null), h = ae(jl), [S, w] = Me(null);
  Bl(() => {
    if (!l.current || !k.current) return;
    const B = l.current.getBoundingClientRect(), j = k.current.getBoundingClientRect();
    let Y = B.bottom + 4, d = B.left;
    Y + j.height > window.innerHeight && (Y = B.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, B.right - j.width)), w({ top: Y, left: d });
  }, [l]);
  const v = m != null, [g, D, L] = v ? wt(m) : [0, 0, 0], [_, b] = Me((m == null ? void 0 : m.toUpperCase()) ?? "");
  Ge(() => {
    b((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Ge(() => {
    const B = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [u]), Ge(() => {
    const B = (Y) => {
      k.current && !k.current.contains(Y.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const E = fe(
    (B) => (j) => {
      const Y = parseInt(j.target.value, 10);
      if (isNaN(Y)) return;
      const d = yt(Y);
      f(kt(B === "r" ? d : g, B === "g" ? d : D, B === "b" ? d : L));
    },
    [g, D, L]
  ), H = fe(
    (B) => {
      if (m != null) {
        B.dataTransfer.setData(Ct, m.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = m, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [m]
  ), P = fe((B) => {
    const j = B.target.value;
    b(j), Qe(j) && f(j);
  }, []), R = fe(() => {
    f(null);
  }, []), V = fe((B) => {
    f(B);
  }, []), A = fe(
    (B) => {
      c(B);
    },
    [c]
  ), T = fe(
    (B, j) => {
      const Y = [...n], d = Y[B];
      Y[B] = Y[j], Y[j] = d, o(Y);
    },
    [n, o]
  ), $ = fe(
    (B, j) => {
      const Y = [...n];
      Y[B] = j, o(Y);
    },
    [n, o]
  ), ee = fe(() => {
    o([...i]);
  }, [i, o]), F = fe(
    (B) => {
      if (Pl(n, B)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const Y = [...n];
      Y[j] = B.toUpperCase(), o(Y);
    },
    [n, o]
  ), Z = fe(() => {
    m != null && F(m), c(m);
  }, [m, c, F]);
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
        onClick: () => p("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      xl,
      {
        colors: n,
        columns: r,
        onSelect: V,
        onConfirm: A,
        onSwap: T,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Ml, { color: m ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (v ? "" : " tlColorInput--noColor"),
        style: v ? { backgroundColor: m } : void 0,
        draggable: v,
        onDragStart: v ? H : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? g : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? D : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: v ? L : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !Qe(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: P
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, h["js.colorInput.ok"]))
  );
}, $l = { "js.colorInput.chooseColor": "Choose color" }, { useState: Fl, useCallback: Pe, useRef: Hl } = e, Wl = ({ controlId: l, state: t }) => {
  const n = le(), r = ae($l), [i, s] = Fl(!1), c = Hl(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], p = t.paletteColumns ?? 6, m = t.defaultPalette ?? a, f = Pe(() => {
    o && s(!0);
  }, [o]), k = Pe(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), h = Pe(() => {
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
      onClick: f,
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
      paletteColumns: p,
      defaultPalette: m,
      canReset: t.canReset !== !1,
      onConfirm: k,
      onCancel: h,
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
}, { useState: xe, useCallback: ve, useEffect: je, useRef: ht, useLayoutEffect: zl, useMemo: Ul } = e, Vl = {
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
}, Kl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ae(Vl), [o, a] = xe("simple"), [p, m] = xe(""), [f, k] = xe(t ?? ""), [h, S] = xe(!1), [w, v] = xe(null), g = ht(null), D = ht(null);
  zl(() => {
    if (!l.current || !g.current) return;
    const A = l.current.getBoundingClientRect(), T = g.current.getBoundingClientRect();
    let $ = A.bottom + 4, ee = A.left;
    $ + T.height > window.innerHeight && ($ = A.top - T.height - 4), ee + T.width > window.innerWidth && (ee = Math.max(0, A.right - T.width)), v({ top: $, left: ee });
  }, [l]), je(() => {
    !r && !h && c().catch(() => S(!0));
  }, [r, h, c]), je(() => {
    r && D.current && D.current.focus();
  }, [r]), je(() => {
    const A = (T) => {
      T.key === "Escape" && s();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [s]), je(() => {
    const A = ($) => {
      g.current && !g.current.contains($.target) && s();
    }, T = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const L = Ul(() => {
    if (!p) return n;
    const A = p.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(A) || T.label.toLowerCase().includes(A) || T.terms != null && T.terms.some(($) => $.includes(A))
    );
  }, [n, p]), _ = ve((A) => {
    m(A.target.value);
  }, []), b = ve(
    (A) => {
      i(A);
    },
    [i]
  ), E = ve((A) => {
    k(A);
  }, []), H = ve((A) => {
    k(A.target.value);
  }, []), P = ve(() => {
    i(f || null);
  }, [f, i]), R = ve(() => {
    i(null);
  }, [i]), V = ve(async (A) => {
    A.preventDefault(), S(!1);
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
      ref: g,
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
        ref: D,
        type: "text",
        className: "tlIconSelect__search",
        value: p,
        onChange: _,
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
      !r && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      r && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && L.map(
        (A) => A.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => o === "simple" ? b(T.encoded) : E(T.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), o === "simple" ? b(T.encoded) : E(T.encoded));
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
        value: f,
        onChange: H
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(Se, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: P }, u["js.iconSelect.ok"]))
  );
}, Yl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Gl, useCallback: Ae, useRef: Xl } = e, ql = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Yl), [i, s] = Gl(!1), c = Xl(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, p = t.icons ?? [], m = t.iconsLoaded === !0, f = Ae(() => {
    o && !a && s(!0);
  }, [o, a]), k = Ae(
    (w) => {
      s(!1), n("valueChanged", { value: w });
    },
    [n]
  ), h = Ae(() => {
    s(!1);
  }, []), S = Ae(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: a,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Kl,
    {
      anchorRef: c,
      currentValue: u,
      icons: p,
      iconsLoaded: m,
      onSelect: k,
      onCancel: h,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: Zl, useMemo: bt, useRef: Ql, useState: Xe } = e, Jl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, er = [1, 2, 3, 4];
function tr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function nr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of er)
    n >= i && (r = i);
  return r;
}
function lr(l, t) {
  const n = Jl[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function rr(l, t) {
  const n = Math.max(1, t), r = {}, i = (m, f) => !!(r[m] && r[m][f]), s = (m, f) => {
    r[m] || (r[m] = {}), r[m][f] = !0;
  }, c = [];
  let u = 0, o = 0;
  const a = (m) => {
    let f = null;
    for (const h of c) h.rowStart === m && (f = h);
    if (!f) return;
    let k = f.colEnd;
    for (; k < n && !i(m, k); ) k++;
    if (k !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let S = f.colEnd; S < k; S++) s(h, S);
      f.colEnd = k;
    }
  };
  for (const m of l) {
    const f = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let k = Math.min(lr(m.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let h = 0;
    for (let D = o; D < n && !i(u, D); D++)
      h++;
    if (k > h) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      h = 0;
      for (let D = o; D < n && !i(u, D); D++)
        h++;
      k = Math.min(k, h);
    }
    const S = o, w = o + k, v = u, g = u + f;
    c.push({ id: m.id, colStart: S, colEnd: w, rowStart: v, rowEnd: g });
    for (let D = v; D < g; D++)
      for (let L = S; L < w; L++) s(D, L);
    o = w, o >= n && (o = 0, u++);
  }
  a(u);
  let p = 0;
  for (const m of c) m.rowEnd > p && (p = m.rowEnd);
  for (let m = 1; m < p; m++)
    for (let f = 0; f < n; f++) {
      if (i(m, f)) continue;
      const k = c.find((h) => h.rowEnd === m && h.colStart <= f && f < h.colEnd);
      if (k) {
        k.rowEnd = m + 1;
        for (let h = k.colStart; h < k.colEnd; h++) s(m, h);
      }
    }
  return c;
}
const or = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), s = Ql(null), [c, u] = Xe(1), o = t.editMode === !0;
  Zl(() => {
    const b = s.current;
    if (!b) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = tr(r, E), P = () => u(nr(b.clientWidth, H));
    P();
    const R = new ResizeObserver(P);
    return R.observe(b), () => R.disconnect();
  }, [r]);
  const a = bt(() => rr(i, c), [i, c]), p = bt(() => {
    const b = {};
    for (const E of a) b[E.id] = E;
    return b;
  }, [a]), [m, f] = Xe(null), [k, h] = Xe(null), S = ke((b, E) => {
    if (!o) {
      b.preventDefault();
      return;
    }
    f(E), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", E);
  }, [o]), w = ke((b, E) => {
    if (!o || !m || m === E) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const H = b.currentTarget.getBoundingClientRect(), P = b.clientX < H.left + H.width / 2;
    h((R) => R && R.id === E && R.before === P ? R : { id: E, before: P });
  }, [o, m]), v = ke(() => {
  }, []), g = ke((b, E, H) => {
    const P = i.map((T) => T.id), R = P.indexOf(b);
    if (R < 0) return;
    P.splice(R, 1);
    const V = P.indexOf(E);
    if (V < 0) {
      P.splice(R, 0, b);
      return;
    }
    const A = H ? V : V + 1;
    P.splice(A, 0, b), n("reorder", { order: P });
  }, [i, n]), D = ke((b, E) => {
    if (!o || !m || m === E) return;
    b.preventDefault();
    const H = b.currentTarget.getBoundingClientRect(), P = b.clientX < H.left + H.width / 2;
    g(m, E, P), f(null), h(null);
  }, [o, m, g]), L = ke(() => {
    f(null), h(null);
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
      const E = p[b.id];
      if (!E) return null;
      const H = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, P = ["tlDashboard__tile"];
      return m === b.id && P.push("tlDashboard__tile--dragging"), k && k.id === b.id && P.push(k.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: P.join(" "),
          style: H,
          draggable: o,
          onDragStart: (R) => S(R, b.id),
          onDragOver: (R) => w(R, b.id),
          onDragLeave: v,
          onDrop: (R) => D(R, b.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: b.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: ar, useRef: _t, useState: sr, useEffect: vt } = e, cr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, ir = ({ group: l }) => {
  var a, p;
  const [t, n] = sr(!1), r = _t(null), i = _t(null), s = ar(() => {
    n((m) => !m);
  }, []);
  vt(() => {
    if (!t) return;
    const m = (f) => {
      i.current && !i.current.contains(f.target) && r.current && !r.current.contains(f.target) && n(!1);
    };
    return document.addEventListener("mousedown", m), () => document.removeEventListener("mousedown", m);
  }, [t]), vt(() => {
    if (!t) return;
    const m = (f) => {
      f.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", m), () => document.removeEventListener("keydown", m);
  }, [t]);
  const c = l.items.filter((m) => m != null);
  if (c.length === 0) return null;
  if (c.length === 1 && !((a = l.subGroups) != null && a.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: c[0] })));
  const u = l.label ?? l.name, o = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: r,
      type: "button",
      className: "tlToolbar__menuTrigger" + (o ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: s,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": o ? u : void 0,
      title: o ? u : void 0
    },
    o ? /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, u), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlToolbar__dropdown",
      role: "menu",
      hidden: !t,
      onClick: () => n(!1)
    },
    c.map((m, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: m }))),
    (p = l.subGroups) == null ? void 0 : p.map((m, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), m.items.map((k, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: k })))))
  ));
}, ur = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(ir, { group: i }) : /* @__PURE__ */ e.createElement(cr, { group: i }))));
}, dr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, mr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(K, { key: i, control: r })));
}, pr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), fr = {
  "js.sidebar.openDrawer": "Open navigation"
}, hr = ({ controlId: l }) => {
  const t = le(), n = ae(fr);
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
};
U("TLButton", Ht);
U("TLToggleButton", zt);
U("TLTextInput", Rt);
U("TLPasswordInput", Lt);
U("TLNumberInput", It);
U("TLDatePicker", Pt);
U("TLSelect", At);
U("TLCheckbox", Ot);
U("TLTable", $t);
U("TLCounter", Ut);
U("TLTabBar", Kt);
U("TLFieldList", Yt);
U("TLAudioRecorder", Xt);
U("TLAudioPlayer", Zt);
U("TLFileUpload", Jt);
U("TLDownload", tn);
U("TLPhotoCapture", ln);
U("TLPhotoViewer", on);
U("TLSplitPanel", an);
U("TLPanel", fn);
U("TLMaximizeRoot", hn);
U("TLDeckPane", bn);
U("TLSidebar", kn);
U("TLStack", Sn);
U("TLGrid", Nn);
U("TLCard", Tn);
U("TLAppBar", Rn);
U("TLBreadcrumb", Ln);
U("TLBottomBar", In);
U("TLDialog", Pn);
U("TLDialogManager", Bn);
U("TLWindow", Fn);
U("TLDrawer", Un);
U("TLContextMenuRegion", Kn);
U("TLSnackbar", qn);
U("TLMenu", Qn);
U("TLAppShell", Jn);
U("TLText", el);
U("TLTableView", nl);
U("TLFormLayout", il);
U("TLFormGroup", ml);
U("TLFormField", bl);
U("TLResourceCell", _l);
U("TLTreeView", El);
U("TLDropdownSelect", Tl);
U("TLColorInput", Wl);
U("TLIconSelect", ql);
U("TLDashboard", or);
U("TLToolbar", ur);
U("TLTileStack", dr);
U("TLSlot", mr);
U("TLSlotContent", pr);
U("TLDrawerToggle", hr);
