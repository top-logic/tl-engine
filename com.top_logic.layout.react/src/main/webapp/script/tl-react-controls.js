import { React as e, useTLFieldValue as Te, getComponent as Rt, useTLState as q, useTLCommand as le, TLChild as K, useTLUpload as Je, useI18N as ae, useTLDataUrl as et, register as U } from "tl-react-bridge";
const { useCallback: xt } = e, Lt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = xt(
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
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Dt(
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
}, { useCallback: Mt } = e, Pt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), s = Mt(
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
}, { useCallback: jt } = e, Bt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = jt(
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
}, { useCallback: At } = e, $t = ({ controlId: l, state: t, config: n }) => {
  var p;
  const [r, i] = Te(), s = At(
    (m) => {
      i(m.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const m = ((p = c.find((h) => h.value === r)) == null ? void 0 : p.label) ?? "";
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
}, { useCallback: Ot } = e, Ft = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Ot(
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
}, Ht = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Rt(c.cellModule) : void 0, o = i[c.name];
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
const { useCallback: Wt } = e, zt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = le(), o = t ?? "click", a = n ?? c.label, p = r ?? c.image, m = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", v = c.hidden === !0, f = c.tooltip, S = v ? { display: "none" } : void 0, w = c.appearance, E = c.size, C = c.navigateUrl, D = Wt(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(o);
  }, [u, o, C]), L = h === "icon-only", _ = h === "icon-only" || h === "icon-label", b = h === "label-only" || h === "icon-label" || L && !p, g = f ?? (L ? a : void 0), H = g ? `text:${g}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: D,
      disabled: m,
      style: S,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (w === "link" ? " tlReactButton--link" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": H,
      "aria-label": L ? a : void 0
    },
    _ && p && /* @__PURE__ */ e.createElement(Se, { encoded: p, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Ut } = e, Vt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = le(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, p = i ?? s.disabled === !0, m = Ut(() => {
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
}, Kt = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Yt } = e, Gt = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.tabs ?? [], i = t.activeTabId, s = Yt((c) => {
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
}, Xt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(K, { control: i })))));
}, qt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), p = t.status ?? "idle", m = t.error, h = p === "received" ? "idle" : r !== "idle" ? r : p, v = e.useCallback(async () => {
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
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(C, D ? { mimeType: D } : void 0);
        u.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && o.current.push(_.data);
        }, L.onstop = async () => {
          C.getTracks().forEach((g) => g.stop()), a.current = null;
          const _ = new Blob(o.current, { type: L.mimeType || "audio/webm" });
          if (o.current = [], _.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const b = new FormData();
          b.append("audio", _, "recording.webm"), await n(b), i("idle");
        }, L.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(qt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], w = h === "uploading", E = ["tlAudioRecorder__button"];
  return h === "recording" && E.push("tlAudioRecorder__button--recording"), h === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: v,
      disabled: w,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Qt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Jt = ({ controlId: l }) => {
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
        const E = await w.blob();
        o.current = URL.createObjectURL(E);
      } catch (w) {
        console.error("[TLAudioPlayer] Fetch error:", w), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), m = ae(Qt), h = s === "loading" ? m["js.loading"] : s === "playing" ? m["js.audioPlayer.pause"] : s === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], v = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: p,
      disabled: v,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, en = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, tn = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, p = t.accept ?? "", m = o === "received" ? "idle" : r !== "idle" ? r : o, h = e.useCallback(async (_) => {
    i("uploading");
    const b = new FormData();
    b.append("file", _, _.name), await n(b), i("idle");
  }, [n]), v = e.useCallback((_) => {
    var g;
    const b = (g = _.target.files) == null ? void 0 : g[0];
    b && h(b);
  }, [h]), f = e.useCallback(() => {
    var _;
    r !== "uploading" && ((_ = u.current) == null || _.click());
  }, [r]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!0);
  }, []), w = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), E = e.useCallback((_) => {
    var g;
    if (_.preventDefault(), _.stopPropagation(), c(!1), r === "uploading") return;
    const b = (g = _.dataTransfer.files) == null ? void 0 : g[0];
    b && h(b);
  }, [r, h]), C = m === "uploading", D = ae(en), L = m === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: w,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: p || void 0,
        onChange: v,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: f,
        disabled: C,
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, nn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ln = ({ controlId: l }) => {
  const t = q(), n = et(), r = le(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const w = await S.blob(), E = URL.createObjectURL(w), C = document.createElement("a");
        C.href = E, C.download = c, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(E);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), m = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = ae(nn);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const v = o ? h["js.downloading"] : h["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (o ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: o,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: c }, c), u && /* @__PURE__ */ e.createElement(
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
}, rn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, on = ({ controlId: l }) => {
  const t = q(), n = Je(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), p = e.useRef(null), m = e.useRef(null), h = e.useRef(null), v = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), w = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((R) => R.stop()), p.current = null), a.current && (a.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    w(), i("idle");
  }, [w]), C = e.useCallback(async () => {
    var R;
    if (r !== "uploading") {
      if (c(null), !S) {
        (R = h.current) == null || R.click();
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
    const B = V.getContext("2d");
    B && (B.drawImage(R, 0, 0), w(), i("uploading"), V.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const O = new FormData();
      O.append("photo", T, "capture.jpg"), await n(O), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, w]), L = e.useCallback(async (R) => {
    var T;
    const V = (T = R.target.files) == null ? void 0 : T[0];
    if (!V) return;
    i("uploading");
    const B = new FormData();
    B.append("photo", V, V.name), await n(B), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && p.current && (a.current.srcObject = p.current);
  }, [r]), e.useEffect(() => {
    var V;
    if (r !== "overlayOpen") return;
    (V = v.current) == null || V.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const R = (V) => {
      V.key === "Escape" && E();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [r, E]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((R) => R.stop()), p.current = null);
  }, []);
  const _ = ae(rn), b = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  u && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const P = ["tlPhotoCapture__mirrorBtn"];
  return u && P.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
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
      onChange: L
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: v,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: E }),
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
        onClick: E,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, an = {
  "js.photoViewer.alt": "Captured photo"
}, sn = ({ controlId: l }) => {
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
  const o = ae(an);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: ot, useRef: Fe } = e, cn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((w) => w.collapsed), o = !u && s.some((w) => w.collapsed), a = u ? !c : c, p = Fe(null), m = Fe(null), h = Fe(null), v = ot((w, E) => {
    const C = {
      overflow: w.scrolling || "auto"
    };
    return w.collapsed ? u && !a ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : E !== void 0 ? C.flex = `0 0 ${E}px` : C.flex = `${w.size} 1 0%`, w.minSize > 0 && !w.collapsed && (C.minWidth = c ? w.minSize : void 0, C.minHeight = c ? void 0 : w.minSize), C;
  }, [c, u, o, a]), f = ot((w, E) => {
    w.preventDefault();
    const C = p.current;
    if (!C) return;
    const D = s[E], L = s[E + 1], _ = C.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((P) => {
      b.push(c ? P.offsetWidth : P.offsetHeight);
    }), h.current = b, m.current = {
      splitterIndex: E,
      startPos: c ? w.clientX : w.clientY,
      startSizeBefore: b[E],
      startSizeAfter: b[E + 1],
      childBefore: D,
      childAfter: L
    };
    const g = (P) => {
      const R = m.current;
      if (!R || !h.current) return;
      const B = (c ? P.clientX : P.clientY) - R.startPos, T = R.childBefore.minSize || 0, O = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + B, F = R.startSizeAfter - B;
      ee < T && (F += ee - T, ee = T), F < O && (ee += F - O, F = O), h.current[R.splitterIndex] = ee, h.current[R.splitterIndex + 1] = F;
      const Z = C.querySelectorAll(":scope > .tlSplitPanel__child"), A = Z[R.splitterIndex], j = Z[R.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${F}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const P = {};
        s.forEach((R, V) => {
          const B = R.control;
          B != null && B.controlId && h.current && (P[B.controlId] = h.current[V]);
        }), n("updateSizes", { sizes: P });
      }
      h.current = null, m.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", H), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((w, E) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${E}`,
          className: `tlSplitPanel__child${w.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(w)
        },
        /* @__PURE__ */ e.createElement(K, { control: w.control })
      )
    ), i && E < s.length - 1) {
      const C = s[E + 1];
      !w.collapsed && !C.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${E}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (L) => f(L, E)
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
}, { useCallback: He } = e, un = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, dn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), mn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), pn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), fn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), hn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), bn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(un), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, p = s === "MINIMIZED", m = s === "MAXIMIZED", h = s === "HIDDEN", v = He(() => {
    n("toggleMinimize");
  }, [n]), f = He(() => {
    n("toggleMaximize");
  }, [n]), S = He(() => {
    n("popOut");
  }, [n]);
  if (h)
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
        onClick: v,
        title: p ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(mn, null) : /* @__PURE__ */ e.createElement(dn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: m ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      m ? /* @__PURE__ */ e.createElement(fn, null) : /* @__PURE__ */ e.createElement(pn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(hn, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(K, { control: t.child })),
    !p && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(K, { control: t.buttonBar }))
  );
}, _n = ({ controlId: l }) => {
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
}, vn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(K, { control: t.activeChild }));
}, { useCallback: ue, useState: Ae, useEffect: $e, useRef: Oe } = e, En = {
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
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, gn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
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
), Cn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), yn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), wn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), kn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Oe(null);
  $e(() => {
    const a = (p) => {
      c.current && !c.current.contains(p.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [s]), $e(() => {
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
}, Sn = ({
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
  onItemFocus: h,
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const w = Oe(null), [E, C] = Ae(null), D = ue(() => {
    r ? v === l.id ? S() : (w.current && C(w.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, v, l.id, c, f, S]), L = ue((b) => {
    w.current = b, o(b);
  }, [o]), _ = r && v === l.id;
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
    kn,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((b) => /* @__PURE__ */ e.createElement(
    gt,
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
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: v,
      onOpenFlyout: f,
      onCloseFlyout: S
    }
  ))));
}, gt = ({
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
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        gn,
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
        Cn,
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
      return /* @__PURE__ */ e.createElement(yn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(wn, null);
    case "group": {
      const v = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Sn,
        {
          item: l,
          expanded: v,
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
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, Nn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(En), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, p] = Ae(() => {
    const T = /* @__PURE__ */ new Map(), O = (ee) => {
      for (const F of ee)
        F.type === "group" && (T.set(F.id, F.expanded), O(F.children));
    };
    return O(i), T;
  }), m = ue((T) => {
    p((O) => {
      const ee = new Map(O), F = ee.get(T) ?? !1;
      return ee.set(T, !F), n("toggleGroup", { itemId: T, expanded: !F }), ee;
    });
  }, [n]), h = ue((T) => {
    T !== s && n("selectItem", { itemId: T });
  }, [n, s]), v = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [w, E] = Ae(null), C = ue((T) => {
    E(T);
  }, []), D = ue(() => {
    E(null);
  }, []);
  $e(() => {
    o || E(null);
  }, [o]);
  const [L, _] = Ae(() => {
    const T = qe(i, o, a);
    return T.length > 0 ? T[0].id : "";
  }), b = Oe(/* @__PURE__ */ new Map()), g = ue((T) => (O) => {
    O ? b.current.set(T, O) : b.current.delete(T);
  }, []), H = ue((T) => {
    _(T);
  }, []), P = Oe(0), R = ue((T) => {
    _(T), P.current++;
  }, []);
  $e(() => {
    const T = b.current.get(L);
    T && document.activeElement !== T && T.focus();
  }, [L, P.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), D();
      return;
    }
    const O = qe(i, o, a);
    if (O.length === 0) return;
    const ee = O.findIndex((Z) => Z.id === L);
    if (ee < 0) return;
    const F = O[ee];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const Z = (ee + 1) % O.length;
        R(O[Z].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const Z = (ee - 1 + O.length) % O.length;
        R(O[Z].id);
        break;
      }
      case "Home": {
        T.preventDefault(), R(O[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), R(O[O.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), F.type === "nav" ? h(F.id) : F.type === "command" ? v(F.id) : F.type === "group" && (o ? w === F.id ? D() : C(F.id) : m(F.id));
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
    h,
    v,
    m,
    C,
    D
  ]), B = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(K, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(K, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(K, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, i.map((T) => /* @__PURE__ */ e.createElement(
    gt,
    {
      key: T.id,
      item: T,
      activeItemId: s,
      collapsed: o,
      onSelect: h,
      onExecute: v,
      onToggleGroup: m,
      focusedId: L,
      setItemRef: g,
      onItemFocus: H,
      groupStates: a,
      flyoutGroupId: w,
      onOpenFlyout: C,
      onCloseFlyout: D
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
}, Tn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.children ?? [], u = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, c.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })));
}, Rn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(K, { control: t.child }));
}, xn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(K, { key: o, control: u })));
}, Ln = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(K, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(K, { control: c })));
}, Dn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(K, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, p) => /* @__PURE__ */ e.createElement(K, { key: p, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, p) => /* @__PURE__ */ e.createElement(K, { key: p, control: a }))));
}, { useCallback: In } = e, Mn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = In((s) => {
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
}, { useCallback: Pn } = e, jn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.items ?? [], i = t.activeItemId, s = Pn((c) => {
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
}, { useCallback: at, useEffect: st, useRef: Bn } = e, An = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Bn(null), u = at(() => {
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
}, { useEffect: $n, useRef: On } = e, Fn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = On(n.length);
  return $n(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(K, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, Hn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Wn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], zn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(Hn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, p = t.actions ?? [], m = t.toolbar, h = t.buttonBar, [v, f] = Ie(null), [S, w] = Ie(null), [E, C] = Ie(null), D = Ce(null), [L, _] = Ie(!1), b = Ce(null), g = Ce(null), H = Ce(null), P = Ce(null), R = Ce(null), V = De(() => {
    n("close");
  }, [n]), B = De((Z, A) => {
    A.preventDefault();
    const j = P.current;
    if (!j) return;
    const Y = j.getBoundingClientRect(), d = !D.current, k = D.current ?? { x: Y.left, y: Y.top };
    d && (D.current = k, C(k)), R.current = {
      dir: Z,
      startX: A.clientX,
      startY: A.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const W = (G) => {
      const M = R.current;
      if (!M) return;
      const Q = G.clientX - M.startX, ne = G.clientY - M.startY;
      let te = M.startW, ie = M.startH, de = 0, me = 0;
      M.symmetric ? (M.dir.includes("e") && (te = M.startW + 2 * Q), M.dir.includes("w") && (te = M.startW - 2 * Q), M.dir.includes("s") && (ie = M.startH + 2 * ne), M.dir.includes("n") && (ie = M.startH - 2 * ne)) : (M.dir.includes("e") && (te = M.startW + Q), M.dir.includes("w") && (te = M.startW - Q, de = Q), M.dir.includes("s") && (ie = M.startH + ne), M.dir.includes("n") && (ie = M.startH - ne, me = ne));
      const he = Math.max(200, te), be = Math.max(100, ie);
      M.symmetric ? (de = (M.startW - he) / 2, me = (M.startH - be) / 2) : (M.dir.includes("w") && he === 200 && (de = M.startW - 200), M.dir.includes("n") && be === 100 && (me = M.startH - 100)), g.current = he, H.current = be, f(he), w(be);
      const N = {
        x: M.startPos.x + de,
        y: M.startPos.y + me
      };
      D.current = N, C(N);
    }, $ = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", $);
      const G = g.current, M = H.current;
      (G != null || M != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...M != null ? { height: Math.round(M) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", $);
  }, [n]), T = De((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const A = P.current;
    if (!A) return;
    const j = A.getBoundingClientRect(), Y = D.current ?? { x: j.left, y: j.top }, d = Z.clientX - Y.x, k = Z.clientY - Y.y, W = (G) => {
      const M = window.innerWidth, Q = window.innerHeight;
      let ne = G.clientX - d, te = G.clientY - k;
      const ie = A.offsetWidth, de = A.offsetHeight;
      ne + ie > M && (ne = M - ie), te + de > Q && (te = Q - de), ne < 0 && (ne = 0), te < 0 && (te = 0);
      const me = { x: ne, y: te };
      D.current = me, C(me);
    }, $ = () => {
      document.removeEventListener("mousemove", W), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", W), document.addEventListener("mouseup", $);
  }, []), O = De(() => {
    var Z, A;
    if (L) {
      const j = b.current;
      j && (C(j.x !== -1 ? { x: j.x, y: j.y } : null), f(j.w), w(j.h)), _(!1);
    } else {
      const j = P.current, Y = j == null ? void 0 : j.getBoundingClientRect();
      b.current = {
        x: ((Z = D.current) == null ? void 0 : Z.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((A = D.current) == null ? void 0 : A.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: v ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, _(!0), C({ x: 0, y: 0 }), f(null), w(null);
    }
  }, [L, v, S]), ee = L ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
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
        onDoubleClick: o ? O : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      m && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(K, { control: m })),
      o && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
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
    (p.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(K, { control: h }), p.map((Z, A) => /* @__PURE__ */ e.createElement(K, { key: A, control: Z }))),
    o && !L && Wn.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (A) => B(Z, A)
      }
    ))
  );
}, { useCallback: Un, useEffect: Vn } = e, Kn = {
  "js.drawer.close": "Close"
}, Yn = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(Kn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Un(() => {
    n("close");
  }, [n]);
  Vn(() => {
    if (!i) return;
    const m = (h) => {
      h.key === "Escape" && a();
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
}, { useCallback: Gn } = e, Xn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.child, i = Gn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(K, { control: r }));
}, { useCallback: qn, useEffect: Zn, useState: Qn } = e, Jn = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, p] = Qn(!1), m = qn(() => {
    p(!0), setTimeout(() => {
      n("dismiss", { generation: o }), p(!1);
    }, 200);
  }, [n, o]);
  return Zn(() => {
    if (!u || c === 0) return;
    const h = setTimeout(m, c);
    return () => clearTimeout(h);
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
}, { useCallback: We, useEffect: ze, useRef: el, useState: ct } = e, tl = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = el(null), [a, p] = ct({ top: 0, left: 0 }), [m, h] = ct(0), v = u.filter((E) => E.type === "item" && !E.disabled);
  ze(() => {
    var g, H;
    if (!r) return;
    const E = ((g = o.current) == null ? void 0 : g.offsetHeight) ?? 200, C = ((H = o.current) == null ? void 0 : H.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let P = c, R = s;
      P + E > window.innerHeight && (P = Math.max(0, window.innerHeight - E)), R + C > window.innerWidth && (R = Math.max(0, window.innerWidth - C)), p({ top: P, left: R }), h(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const L = D.getBoundingClientRect();
    let _ = L.bottom + 4, b = L.left;
    _ + E > window.innerHeight && (_ = L.top - E - 4), b + C > window.innerWidth && (b = L.right - C), p({ top: _, left: b }), h(0);
  }, [r, i, s, c]);
  const f = We(() => {
    n("close");
  }, [n]), S = We((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  ze(() => {
    if (!r) return;
    const E = (C) => {
      o.current && !o.current.contains(C.target) && f();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [r, f]);
  const w = We((E) => {
    if (E.key === "Escape") {
      f();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), h((C) => (C + 1) % v.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), h((C) => (C - 1 + v.length) % v.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const C = v[m];
      C && S(C.id);
    }
  }, [f, S, v, m]);
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
    u.map((E, C) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const L = v.indexOf(E) === m;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: E.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (E.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: E.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + E.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, nl = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(K, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(K, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(K, { control: i })), /* @__PURE__ */ e.createElement(K, { control: s }), c && /* @__PURE__ */ e.createElement(K, { control: c }), u && /* @__PURE__ */ e.createElement(K, { control: u }));
}, ll = ({ controlId: l }) => {
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
}, rl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, ol = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(rl), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const I = (y) => {
      const x = y.detail;
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
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", p = t.selectedCount ?? 0, m = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, v = e.useMemo(
    () => s.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [s]
  ), f = a === "multi", S = 40, w = 20, E = e.useRef(null), C = e.useRef(null), D = e.useRef(null), [L, _] = e.useState({}), b = e.useRef(null), g = e.useRef(!1), H = e.useRef(null), [P, R] = e.useState(null), [V, B] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [s]);
  const T = e.useCallback((N) => L[N.name] ?? N.width, [L]), O = e.useMemo(() => {
    const N = [];
    let I = f && m > 0 ? S : 0;
    for (let y = 0; y < m && y < s.length; y++)
      N.push(I), I += T(s[y]);
    return N;
  }, [s, m, f, S, T]), ee = c * o, F = e.useRef(null), Z = e.useCallback((N, I, y) => {
    y.preventDefault(), y.stopPropagation(), b.current = { column: N, startX: y.clientX, startWidth: I };
    let x = y.clientX, z = 0;
    const J = () => {
      const oe = b.current;
      if (!oe) return;
      const pe = Math.max(it, oe.startWidth + (x - oe.startX) + z);
      _((ge) => ({ ...ge, [oe.column]: pe }));
    }, re = () => {
      const oe = C.current, pe = E.current;
      if (!oe || !b.current) return;
      const ge = oe.getBoundingClientRect(), nt = 40, lt = 8, Tt = oe.scrollLeft;
      x > ge.right - nt ? oe.scrollLeft += lt : x < ge.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - Tt;
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
        n("columnResize", { column: pe.column, width: ge }), b.current = null, g.current = !0, requestAnimationFrame(() => {
          g.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", Le);
  }, [n]), A = e.useCallback(() => {
    E.current && C.current && (E.current.scrollLeft = C.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const N = C.current;
      if (!N) return;
      const I = N.scrollTop, y = Math.ceil(N.clientHeight / o), x = Math.floor(I / o);
      n("scroll", { start: x, count: y });
    }, 80);
  }, [n, o]), j = e.useCallback((N, I, y) => {
    if (g.current) return;
    let x;
    !I || I === "desc" ? x = "asc" : x = "desc";
    const z = y.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: x, mode: z });
  }, [n]), Y = e.useCallback((N, I) => {
    H.current = N, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, I) => {
    if (!H.current || H.current === N) {
      R(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const y = I.currentTarget.getBoundingClientRect(), x = I.clientX < y.left + y.width / 2 ? "left" : "right";
    R({ column: N, side: x });
  }, []), k = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const I = H.current;
    if (!I || !P) {
      H.current = null, R(null);
      return;
    }
    let y = s.findIndex((z) => z.name === P.column);
    if (y < 0) {
      H.current = null, R(null);
      return;
    }
    const x = s.findIndex((z) => z.name === I);
    P.side === "right" && y++, x < y && y--, n("columnReorder", { column: I, targetIndex: y }), H.current = null, R(null);
  }, [s, P, n]), W = e.useCallback(() => {
    H.current = null, R(null);
  }, []), $ = e.useCallback((N, I) => {
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
  }, [n, p, c]), Q = e.useCallback((N, I, y) => {
    y.stopPropagation(), n("expand", { rowIndex: N, expanded: I });
  }, [n]), ne = e.useCallback((N, I) => {
    I.preventDefault(), B({ x: I.clientX, y: I.clientY, colIdx: N });
  }, []), te = e.useCallback(() => {
    V && (n("setFrozenColumnCount", { count: V.colIdx + 1 }), B(null));
  }, [V, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), B(null);
  }, [n]);
  e.useEffect(() => {
    if (!V) return;
    const N = () => B(null), I = (y) => {
      y.key === "Escape" && B(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", I);
    };
  }, [V]);
  const de = s.reduce((N, I) => N + T(I), 0) + (f ? S : 0), me = p === c && c > 0, he = p > 0 && p < c, be = e.useCallback((N) => {
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
        const I = C.current, y = E.current;
        if (!I) return;
        const x = I.getBoundingClientRect(), z = 40, J = 8;
        N.clientX < x.left + z ? I.scrollLeft = Math.max(0, I.scrollLeft - J) : N.clientX > x.right - z && (I.scrollLeft += J), y && (y.scrollLeft = I.scrollLeft);
      },
      onDrop: k
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, f && /* @__PURE__ */ e.createElement(
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
      const y = T(N);
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
            width: y,
            minWidth: y,
            position: z ? "sticky" : "relative",
            ...z ? { left: O[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (re) => j(N.name, N.sortDirection, re) : void 0,
          onContextMenu: (re) => ne(I, re),
          onDragStart: (re) => Y(N.name, re),
          onDragOver: (re) => d(N.name, re),
          onDrop: k,
          onDragEnd: W
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", v > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => Z(N.name, y, re)
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
        onDrop: k
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
        s.map((I, y) => {
          const x = T(I), z = y === s.length - 1, J = y < m, re = y === m - 1;
          let Ee = "tlTableView__cell";
          J && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const Le = h && y === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Ee,
              "data-row": N.id,
              "data-col": I.name,
              style: {
                ...z && !J ? { flex: "1 0 auto", minWidth: x } : { width: x, minWidth: x },
                ...J ? { position: "sticky", left: O[y], zIndex: 2 } : {}
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
}, al = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ct = e.createContext(al), { useMemo: sl, useRef: cl, useState: il, useEffect: ul } = e, dl = 320, ml = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = cl(null), [o, a] = il(
    r === "top" ? "top" : "side"
  );
  ul(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((w) => {
      for (const E of w) {
        const D = E.contentRect.width / n;
        a(D < dl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const p = sl(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Ct.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useCallback: pl } = e, fl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, hl = ({ controlId: l }) => {
  const t = q(), n = le(), r = ae(fl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, p = t.children ?? [], m = i != null || s.length > 0 || c, h = pl(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: v }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(K, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((f, S) => /* @__PURE__ */ e.createElement(K, { key: S, control: f }))));
}, { useContext: bl, useState: _l, useCallback: vl } = e, El = ({ controlId: l }) => {
  const t = q(), n = bl(Ct), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, p = t.fullLine === !0, m = t.visible !== !1, h = t.hasTooltip === !0, v = t.field, f = n.readOnly, [S, w] = _l(!1), E = vl(() => w((_) => !_), []);
  if (!m) return null;
  const C = s != null, D = c != null && c.length > 0, L = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && D ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
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
      onClick: E,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(K, { control: v })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !C && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  const t = q(), n = le(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, p = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), m = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", c].filter(Boolean).join(" "), v = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: m,
      "data-tooltip": v
    },
    p
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": v }, p);
}, Cl = 20, yl = () => {
  const l = q(), t = le(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), p = e.useRef(null), m = e.useCallback((_, b) => {
    t(b ? "collapse" : "expand", { nodeId: _ });
  }, [t]), h = e.useCallback((_, b) => {
    t("select", {
      nodeId: _,
      ctrlKey: b.ctrlKey || b.metaKey,
      shiftKey: b.shiftKey
    });
  }, [t]), v = e.useCallback((_, b) => {
    b.preventDefault(), t("contextMenu", { nodeId: _, x: b.clientX, y: b.clientY });
  }, [t]), f = e.useRef(null), S = e.useCallback((_, b) => {
    const g = b.getBoundingClientRect(), H = _.clientY - g.top, P = g.height / 3;
    return H < P ? "above" : H > P * 2 ? "below" : "within";
  }, []), w = e.useCallback((_, b) => {
    b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", _);
  }, []), E = e.useCallback((_, b) => {
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const g = S(b, b.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: g }), f.current = null;
    }, 50);
  }, [t, S]), C = e.useCallback((_, b) => {
    b.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const g = S(b, b.currentTarget);
    t("drop", { nodeId: _, position: g });
  }, [t, S]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
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
          const g = n[o];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (b = o + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), o >= 0 && o < n.length) {
          const g = n[o];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const H = g.depth;
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
        style: { paddingLeft: _.depth * Cl },
        draggable: i,
        onClick: (g) => h(_.id, g),
        onContextMenu: (g) => v(_.id, g),
        onDragStart: (g) => w(_.id, g),
        onDragOver: s ? (g) => E(_.id, g) : void 0,
        onDrop: s ? (g) => C(_.id, g) : void 0,
        onDragEnd: D
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), m(_.id, _.expanded);
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
function wl() {
  if (ut) return X;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), h = Symbol.iterator;
  function v(d) {
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
  function E(d, k, W) {
    this.props = d, this.context = k, this.refs = w, this.updater = W || f;
  }
  E.prototype.isReactComponent = {}, E.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, E.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = E.prototype;
  function D(d, k, W) {
    this.props = d, this.context = k, this.refs = w, this.updater = W || f;
  }
  var L = D.prototype = new C();
  L.constructor = D, S(L, E.prototype), L.isPureReactComponent = !0;
  var _ = Array.isArray;
  function b() {
  }
  var g = { H: null, A: null, T: null, S: null }, H = Object.prototype.hasOwnProperty;
  function P(d, k, W) {
    var $ = W.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: $ !== void 0 ? $ : null,
      props: W
    };
  }
  function R(d, k) {
    return P(d.type, k, d.props);
  }
  function V(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function B(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(W) {
      return k[W];
    });
  }
  var T = /\/+/g;
  function O(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? B("" + d.key) : k.toString(36);
  }
  function ee(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(b, b) : (d.status = "pending", d.then(
          function(k) {
            d.status === "pending" && (d.status = "fulfilled", d.value = k);
          },
          function(k) {
            d.status === "pending" && (d.status = "rejected", d.reason = k);
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
  function F(d, k, W, $, G) {
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
                k,
                W,
                $,
                G
              );
          }
      }
    if (Q)
      return G = G(d), Q = $ === "" ? "." + O(d, 0) : $, _(G) ? (W = "", Q != null && (W = Q.replace(T, "$&/") + "/"), F(G, k, W, "", function(ie) {
        return ie;
      })) : G != null && (V(G) && (G = R(
        G,
        W + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), k.push(G)), 1;
    Q = 0;
    var ne = $ === "" ? "." : $ + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        $ = d[te], M = ne + O($, te), Q += F(
          $,
          k,
          W,
          M,
          G
        );
    else if (te = v(d), typeof te == "function")
      for (d = te.call(d), te = 0; !($ = d.next()).done; )
        $ = $.value, M = ne + O($, te++), Q += F(
          $,
          k,
          W,
          M,
          G
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return F(
          ee(d),
          k,
          W,
          $,
          G
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, k, W) {
    if (d == null) return d;
    var $ = [], G = 0;
    return F(d, $, "", "", function(M) {
      return k.call(W, M, G++);
    }), $;
  }
  function A(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = W);
        },
        function(W) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = W);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var j = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var k = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(k)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, Y = {
    map: Z,
    forEach: function(d, k, W) {
      Z(
        d,
        function() {
          k.apply(this, arguments);
        },
        W
      );
    },
    count: function(d) {
      var k = 0;
      return Z(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return Z(d, function(k) {
        return k;
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
  return X.Activity = m, X.Children = Y, X.Component = E, X.Fragment = n, X.Profiler = i, X.PureComponent = D, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, X.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return g.H.useMemoCache(d);
    }
  }, X.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, X.cacheSignal = function() {
    return null;
  }, X.cloneElement = function(d, k, W) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var $ = S({}, d.props), G = d.key;
    if (k != null)
      for (M in k.key !== void 0 && (G = "" + k.key), k)
        !H.call(k, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && k.ref === void 0 || ($[M] = k[M]);
    var M = arguments.length - 2;
    if (M === 1) $.children = W;
    else if (1 < M) {
      for (var Q = Array(M), ne = 0; ne < M; ne++)
        Q[ne] = arguments[ne + 2];
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
  }, X.createElement = function(d, k, W) {
    var $, G = {}, M = null;
    if (k != null)
      for ($ in k.key !== void 0 && (M = "" + k.key), k)
        H.call(k, $) && $ !== "key" && $ !== "__self" && $ !== "__source" && (G[$] = k[$]);
    var Q = arguments.length - 2;
    if (Q === 1) G.children = W;
    else if (1 < Q) {
      for (var ne = Array(Q), te = 0; te < Q; te++)
        ne[te] = arguments[te + 2];
      G.children = ne;
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
      $$typeof: p,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, X.memo = function(d, k) {
    return {
      $$typeof: a,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, X.startTransition = function(d) {
    var k = g.T, W = {};
    g.T = W;
    try {
      var $ = d(), G = g.S;
      G !== null && G(W, $), typeof $ == "object" && $ !== null && typeof $.then == "function" && $.then(b, j);
    } catch (M) {
      j(M);
    } finally {
      k !== null && W.types !== null && (k.types = W.types), g.T = k;
    }
  }, X.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, X.use = function(d) {
    return g.H.use(d);
  }, X.useActionState = function(d, k, W) {
    return g.H.useActionState(d, k, W);
  }, X.useCallback = function(d, k) {
    return g.H.useCallback(d, k);
  }, X.useContext = function(d) {
    return g.H.useContext(d);
  }, X.useDebugValue = function() {
  }, X.useDeferredValue = function(d, k) {
    return g.H.useDeferredValue(d, k);
  }, X.useEffect = function(d, k) {
    return g.H.useEffect(d, k);
  }, X.useEffectEvent = function(d) {
    return g.H.useEffectEvent(d);
  }, X.useId = function() {
    return g.H.useId();
  }, X.useImperativeHandle = function(d, k, W) {
    return g.H.useImperativeHandle(d, k, W);
  }, X.useInsertionEffect = function(d, k) {
    return g.H.useInsertionEffect(d, k);
  }, X.useLayoutEffect = function(d, k) {
    return g.H.useLayoutEffect(d, k);
  }, X.useMemo = function(d, k) {
    return g.H.useMemo(d, k);
  }, X.useOptimistic = function(d, k) {
    return g.H.useOptimistic(d, k);
  }, X.useReducer = function(d, k, W) {
    return g.H.useReducer(d, k, W);
  }, X.useRef = function(d) {
    return g.H.useRef(d);
  }, X.useState = function(d) {
    return g.H.useState(d);
  }, X.useSyncExternalStore = function(d, k, W) {
    return g.H.useSyncExternalStore(
      d,
      k,
      W
    );
  }, X.useTransition = function() {
    return g.H.useTransition();
  }, X.version = "19.2.4", X;
}
var dt;
function kl() {
  return dt || (dt = 1, Ve.exports = wl()), Ve.exports;
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
function Sl() {
  if (mt) return se;
  mt = 1;
  var l = kl();
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
      var p = a.as, m = u(p, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, v = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      p === "style" ? r.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: m,
          integrity: h,
          fetchPriority: v
        }
      ) : p === "script" && r.d.X(o, {
        crossOrigin: m,
        integrity: h,
        fetchPriority: v,
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
function Nl() {
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
  return l(), Ue.exports = Sl(), Ue.exports;
}
var yt = Nl();
const { useState: _e, useCallback: ce, useRef: Re, useEffect: ye, useMemo: Ze } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Tl({
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
function Rl({
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
const xl = ({ controlId: l, state: t }) => {
  const n = le(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, p = t.options ?? [], m = t.emptyOptionLabel ?? "", h = s && i && !u && o, v = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = v["js.dropdownSelect.nothingFound"], S = ce(
    (y) => v["js.dropdownSelect.removeChip"].replace("{0}", y),
    [v]
  ), [w, E] = _e(!1), [C, D] = _e(""), [L, _] = _e(-1), [b, g] = _e(!1), [H, P] = _e({}), [R, V] = _e(null), [B, T] = _e(null), [O, ee] = _e(null), F = Re(null), Z = Re(null), A = Re(null), j = Re(r);
  j.current = r;
  const Y = Re(-1), d = Ze(
    () => new Set(r.map((y) => y.value)),
    [r]
  ), k = Ze(() => {
    let y = p.filter((x) => !d.has(x.value));
    if (C) {
      const x = C.toLowerCase();
      y = y.filter((z) => z.label.toLowerCase().includes(x));
    }
    return y;
  }, [p, d, C]);
  ye(() => {
    C && k.length === 1 ? _(0) : _(-1);
  }, [k.length, C]), ye(() => {
    w && a && Z.current && Z.current.focus();
  }, [w, a, r]), ye(() => {
    var z, J;
    if (Y.current < 0) return;
    const y = Y.current;
    Y.current = -1;
    const x = (z = F.current) == null ? void 0 : z.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(y, x.length - 1)].focus() : (J = F.current) == null || J.focus();
  }, [r]), ye(() => {
    if (!w) return;
    const y = (x) => {
      F.current && !F.current.contains(x.target) && A.current && !A.current.contains(x.target) && (E(!1), D(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [w]), ye(() => {
    if (!w || !F.current) return;
    const y = F.current.getBoundingClientRect(), x = window.innerHeight - y.bottom, J = x < 300 && y.top > x;
    P({
      left: y.left,
      width: y.width,
      ...J ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [w]);
  const W = ce(async () => {
    if (!(u || !o) && (E(!0), D(""), _(-1), g(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, o, a, n]), $ = ce(() => {
    var y;
    E(!1), D(""), _(-1), (y = F.current) == null || y.focus();
  }, []), G = ce(
    (y) => {
      let x;
      if (i) {
        const z = p.find((J) => J.value === y);
        if (z)
          x = [...j.current, z];
        else
          return;
      } else {
        const z = p.find((J) => J.value === y);
        if (z)
          x = [z];
        else
          return;
      }
      j.current = x, n("valueChanged", { value: x.map((z) => z.value) }), i ? (D(""), _(-1)) : $();
    },
    [i, p, n, $]
  ), M = ce(
    (y) => {
      Y.current = j.current.findIndex((z) => z.value === y);
      const x = j.current.filter((z) => z.value !== y);
      j.current = x, n("valueChanged", { value: x.map((z) => z.value) });
    },
    [n]
  ), Q = ce(
    (y) => {
      y.stopPropagation(), n("valueChanged", { value: [] }), $();
    },
    [n, $]
  ), ne = ce((y) => {
    D(y.target.value);
  }, []), te = ce(
    (y) => {
      if (!w) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), W();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), _(
            (x) => x < k.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), _(
            (x) => x > 0 ? x - 1 : k.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), L >= 0 && L < k.length && G(k[L].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), $();
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
      W,
      $,
      k,
      L,
      G,
      C,
      i,
      r,
      M
    ]
  ), ie = ce(
    async (y) => {
      y.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
    },
    [n]
  ), de = ce(
    (y, x) => {
      V(y), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), me = ce(
    (y, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", R === null || R === y) {
        T(null), ee(null);
        return;
      }
      const z = x.currentTarget.getBoundingClientRect(), J = z.left + z.width / 2, re = x.clientX < J ? "before" : "after";
      T(y), ee(re);
    },
    [R]
  ), he = ce(
    (y) => {
      if (y.preventDefault(), R === null || B === null || O === null || R === B) return;
      const x = [...j.current], [z] = x.splice(R, 1);
      let J = B;
      R < B ? J = O === "before" ? J - 1 : J : J = O === "before" ? J : J + 1, x.splice(J, 0, z), j.current = x, n("valueChanged", { value: x.map((re) => re.value) }), V(null), T(null), ee(null);
    },
    [R, B, O, n]
  ), be = ce(() => {
    V(null), T(null), ee(null);
  }, []);
  if (ye(() => {
    if (L < 0 || !A.current) return;
    const y = A.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [L, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, m) : r.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const N = !c && r.length > 0 && !u, I = w ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (a || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: ne,
        onKeyDown: te,
        placeholder: v["js.dropdownSelect.filterPlaceholder"],
        "aria-label": v["js.dropdownSelect.filterPlaceholder"],
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
      b && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, v["js.dropdownSelect.error"])),
      a && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      a && k.map((y, x) => /* @__PURE__ */ e.createElement(
        Rl,
        {
          key: y.value,
          id: `${l}-opt-${x}`,
          option: y,
          highlighted: x === L,
          searchTerm: C,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : r.map((y, x) => {
      let z = "";
      return R === x ? z = "tlDropdownSelect__chip--dragging" : B === x && O === "before" ? z = "tlDropdownSelect__chip--dropBefore" : B === x && O === "after" && (z = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Tl,
        {
          key: y.value,
          option: y,
          removable: !u && (i || !c),
          onRemove: M,
          removeLabel: S(y.label),
          draggable: h,
          onDragStart: h ? (J) => de(x, J) : void 0,
          onDragOver: h ? (J) => me(x, J) : void 0,
          onDrop: h ? he : void 0,
          onDragEnd: h ? be : void 0,
          dragClassName: h ? z : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, N && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": v["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, w ? "▲" : "▼"))
  ), I && yt.createPortal(I, document.body));
}, { useCallback: Ke, useRef: Ll } = e, wt = "application/x-tl-color", Dl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Ll(null), u = Ke(
    (p) => (m) => {
      c.current = p, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ke((p) => {
    p.preventDefault(), p.dataTransfer.dropEffect = "move";
  }, []), a = Ke(
    (p) => (m) => {
      m.preventDefault();
      const h = m.dataTransfer.getData(wt);
      h ? s(p, h) : c.current !== null && c.current !== p && i(c.current, p), c.current = null;
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
function kt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function St(l) {
  if (!Qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Nt(l, t, n) {
  const r = (i) => kt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Il(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const p = c === 0 ? 0 : o / c;
  return [a, p, c];
}
function Ml(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function Pl(l) {
  return Il(...St(l));
}
function Ye(l, t, n) {
  return Nt(...Ml(l, t, n));
}
const { useCallback: we, useRef: ft } = e, jl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Pl(l), s = ft(null), c = ft(null), u = we(
    (f, S) => {
      var D;
      const w = (D = s.current) == null ? void 0 : D.getBoundingClientRect();
      if (!w) return;
      const E = Math.max(0, Math.min(1, (f - w.left) / w.width)), C = Math.max(0, Math.min(1, 1 - (S - w.top) / w.height));
      t(Ye(n, E, C));
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
      var C;
      const S = (C = c.current) == null ? void 0 : C.getBoundingClientRect();
      if (!S) return;
      const E = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Ye(E, r, i));
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
  ), v = Ye(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__svField",
      style: { backgroundColor: v },
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
function Bl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Al = {
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
}, { useState: Me, useCallback: fe, useEffect: Ge, useRef: $l, useLayoutEffect: Ol } = e, Fl = ({
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
  const [a, p] = Me("palette"), [m, h] = Me(t), v = $l(null), f = ae(Al), [S, w] = Me(null);
  Ol(() => {
    if (!l.current || !v.current) return;
    const A = l.current.getBoundingClientRect(), j = v.current.getBoundingClientRect();
    let Y = A.bottom + 4, d = A.left;
    Y + j.height > window.innerHeight && (Y = A.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, A.right - j.width)), w({ top: Y, left: d });
  }, [l]);
  const E = m != null, [C, D, L] = E ? St(m) : [0, 0, 0], [_, b] = Me((m == null ? void 0 : m.toUpperCase()) ?? "");
  Ge(() => {
    b((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Ge(() => {
    const A = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ge(() => {
    const A = (Y) => {
      v.current && !v.current.contains(Y.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const g = fe(
    (A) => (j) => {
      const Y = parseInt(j.target.value, 10);
      if (isNaN(Y)) return;
      const d = kt(Y);
      h(Nt(A === "r" ? d : C, A === "g" ? d : D, A === "b" ? d : L));
    },
    [C, D, L]
  ), H = fe(
    (A) => {
      if (m != null) {
        A.dataTransfer.setData(wt, m.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = m, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), A.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [m]
  ), P = fe((A) => {
    const j = A.target.value;
    b(j), Qe(j) && h(j);
  }, []), R = fe(() => {
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
      if (Bl(n, A)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const Y = [...n];
      Y[j] = A.toUpperCase(), o(Y);
    },
    [n, o]
  ), Z = fe(() => {
    m != null && F(m), c(m);
  }, [m, c, F]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: v,
      style: S ? { top: S.top, left: S.left, visibility: "visible" } : { visibility: "hidden" }
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
      Dl,
      {
        colors: n,
        columns: r,
        onSelect: V,
        onConfirm: B,
        onSwap: T,
        onReplace: O
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(jl, { color: m ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (E ? "" : " tlColorInput--noColor"),
        style: E ? { backgroundColor: m } : void 0,
        draggable: E,
        onDragStart: E ? H : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? C : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? D : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: E ? L : "",
        onChange: g("b")
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
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, f["js.colorInput.ok"]))
  );
}, Hl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Wl, useCallback: Pe, useRef: zl } = e, Ul = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Hl), [i, s] = Wl(!1), c = zl(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], p = t.paletteColumns ?? 6, m = t.defaultPalette ?? a, h = Pe(() => {
    o && s(!0);
  }, [o]), v = Pe(
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
    Fl,
    {
      anchorRef: c,
      currentColor: u,
      palette: a,
      paletteColumns: p,
      defaultPalette: m,
      canReset: t.canReset !== !1,
      onConfirm: v,
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
}, { useState: xe, useCallback: ve, useEffect: je, useRef: ht, useLayoutEffect: Vl, useMemo: Kl } = e, Yl = {
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
}, Gl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ae(Yl), [o, a] = xe("simple"), [p, m] = xe(""), [h, v] = xe(t ?? ""), [f, S] = xe(!1), [w, E] = xe(null), C = ht(null), D = ht(null);
  Vl(() => {
    if (!l.current || !C.current) return;
    const B = l.current.getBoundingClientRect(), T = C.current.getBoundingClientRect();
    let O = B.bottom + 4, ee = B.left;
    O + T.height > window.innerHeight && (O = B.top - T.height - 4), ee + T.width > window.innerWidth && (ee = Math.max(0, B.right - T.width)), E({ top: O, left: ee });
  }, [l]), je(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), je(() => {
    r && D.current && D.current.focus();
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
  const L = Kl(() => {
    if (!p) return n;
    const B = p.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(B) || T.label.toLowerCase().includes(B) || T.terms != null && T.terms.some((O) => O.includes(B))
    );
  }, [n, p]), _ = ve((B) => {
    m(B.target.value);
  }, []), b = ve(
    (B) => {
      i(B);
    },
    [i]
  ), g = ve((B) => {
    v(B);
  }, []), H = ve((B) => {
    v(B.target.value);
  }, []), P = ve(() => {
    i(h || null);
  }, [h, i]), R = ve(() => {
    i(null);
  }, [i]), V = ve(async (B) => {
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
      !r && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: V }, u["js.iconSelect.loadError"])),
      r && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && L.map(
        (B) => B.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: B.label,
            onClick: () => o === "simple" ? b(T.encoded) : g(T.encoded),
            onKeyDown: (O) => {
              (O.key === "Enter" || O.key === " ") && (O.preventDefault(), o === "simple" ? b(T.encoded) : g(T.encoded));
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
        onChange: H
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Se, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: P }, u["js.iconSelect.ok"]))
  );
}, Xl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: ql, useCallback: Be, useRef: Zl } = e, Ql = ({ controlId: l, state: t }) => {
  const n = le(), r = ae(Xl), [i, s] = ql(!1), c = Zl(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, p = t.icons ?? [], m = t.iconsLoaded === !0, h = Be(() => {
    o && !a && s(!0);
  }, [o, a]), v = Be(
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
    Gl,
    {
      anchorRef: c,
      currentValue: u,
      icons: p,
      iconsLoaded: m,
      onSelect: v,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: Jl, useMemo: bt, useRef: er, useState: Xe } = e, tr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, nr = [1, 2, 3, 4];
function lr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function rr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of nr)
    n >= i && (r = i);
  return r;
}
function or(l, t) {
  const n = tr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function ar(l, t) {
  const n = Math.max(1, t), r = {}, i = (m, h) => !!(r[m] && r[m][h]), s = (m, h) => {
    r[m] || (r[m] = {}), r[m][h] = !0;
  }, c = [];
  let u = 0, o = 0;
  const a = (m) => {
    let h = null;
    for (const f of c) f.rowStart === m && (h = f);
    if (!h) return;
    let v = h.colEnd;
    for (; v < n && !i(m, v); ) v++;
    if (v !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let S = h.colEnd; S < v; S++) s(f, S);
      h.colEnd = v;
    }
  };
  for (const m of l) {
    const h = n <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let v = Math.min(or(m.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let D = o; D < n && !i(u, D); D++)
      f++;
    if (v > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let D = o; D < n && !i(u, D); D++)
        f++;
      v = Math.min(v, f);
    }
    const S = o, w = o + v, E = u, C = u + h;
    c.push({ id: m.id, colStart: S, colEnd: w, rowStart: E, rowEnd: C });
    for (let D = E; D < C; D++)
      for (let L = S; L < w; L++) s(D, L);
    o = w, o >= n && (o = 0, u++);
  }
  a(u);
  let p = 0;
  for (const m of c) m.rowEnd > p && (p = m.rowEnd);
  for (let m = 1; m < p; m++)
    for (let h = 0; h < n; h++) {
      if (i(m, h)) continue;
      const v = c.find((f) => f.rowEnd === m && f.colStart <= h && h < f.colEnd);
      if (v) {
        v.rowEnd = m + 1;
        for (let f = v.colStart; f < v.colEnd; f++) s(m, f);
      }
    }
  return c;
}
const sr = ({ controlId: l }) => {
  const t = q(), n = le(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), s = er(null), [c, u] = Xe(1), o = t.editMode === !0;
  Jl(() => {
    const b = s.current;
    if (!b) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = lr(r, g), P = () => u(rr(b.clientWidth, H));
    P();
    const R = new ResizeObserver(P);
    return R.observe(b), () => R.disconnect();
  }, [r]);
  const a = bt(() => ar(i, c), [i, c]), p = bt(() => {
    const b = {};
    for (const g of a) b[g.id] = g;
    return b;
  }, [a]), [m, h] = Xe(null), [v, f] = Xe(null), S = ke((b, g) => {
    if (!o) {
      b.preventDefault();
      return;
    }
    h(g), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", g);
  }, [o]), w = ke((b, g) => {
    if (!o || !m || m === g) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const H = b.currentTarget.getBoundingClientRect(), P = b.clientX < H.left + H.width / 2;
    f((R) => R && R.id === g && R.before === P ? R : { id: g, before: P });
  }, [o, m]), E = ke(() => {
  }, []), C = ke((b, g, H) => {
    const P = i.map((T) => T.id), R = P.indexOf(b);
    if (R < 0) return;
    P.splice(R, 1);
    const V = P.indexOf(g);
    if (V < 0) {
      P.splice(R, 0, b);
      return;
    }
    const B = H ? V : V + 1;
    P.splice(B, 0, b), n("reorder", { order: P });
  }, [i, n]), D = ke((b, g) => {
    if (!o || !m || m === g) return;
    b.preventDefault();
    const H = b.currentTarget.getBoundingClientRect(), P = b.clientX < H.left + H.width / 2;
    C(m, g, P), h(null), f(null);
  }, [o, m, C]), L = ke(() => {
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
      const g = p[b.id];
      if (!g) return null;
      const H = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, P = ["tlDashboard__tile"];
      return m === b.id && P.push("tlDashboard__tile--dragging"), v && v.id === b.id && P.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: P.join(" "),
          style: H,
          draggable: o,
          onDragStart: (R) => S(R, b.id),
          onDragOver: (R) => w(R, b.id),
          onDragLeave: E,
          onDrop: (R) => D(R, b.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(K, { control: b.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: cr, useRef: _t, useState: vt, useEffect: Et, useLayoutEffect: ir } = e, ur = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: n }))));
}, dr = ({ group: l }) => {
  var m, h;
  const [t, n] = vt(!1), [r, i] = vt({}), s = _t(null), c = _t(null), u = cr(() => {
    n((v) => !v);
  }, []);
  ir(() => {
    if (!t) return;
    const v = () => {
      const f = s.current;
      if (!f) return;
      const S = f.getBoundingClientRect();
      i({
        position: "fixed",
        top: S.bottom + 4,
        right: Math.max(8, window.innerWidth - S.right),
        left: "auto"
      });
    };
    return v(), window.addEventListener("resize", v), window.addEventListener("scroll", v, !0), () => {
      window.removeEventListener("resize", v), window.removeEventListener("scroll", v, !0);
    };
  }, [t]), Et(() => {
    if (!t) return;
    const v = (f) => {
      c.current && !c.current.contains(f.target) && s.current && !s.current.contains(f.target) && n(!1);
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [t]), Et(() => {
    if (!t) return;
    const v = (f) => {
      f.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [t]);
  const o = l.items.filter((v) => v != null);
  if (o.length === 0) return null;
  if (o.length === 1 && !((m = l.subGroups) != null && m.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(K, { control: o[0] })));
  const a = l.label ?? l.name, p = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (p ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": p ? a : void 0,
      title: p ? a : void 0
    },
    p ? /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, a), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), yt.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: c,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? r : void 0,
        onClick: () => n(!1)
      },
      o.map((v, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: v }))),
      (h = l.subGroups) == null ? void 0 : h.map((v, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, w) => /* @__PURE__ */ e.createElement("div", { key: w, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(K, { control: S })))))
    ),
    document.body
  ));
}, mr = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(dr, { group: i }) : /* @__PURE__ */ e.createElement(ur, { group: i }))));
}, pr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(K, { control: t.frame }));
}, fr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(K, { key: i, control: r })));
}, hr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), br = {
  "js.sidebar.openDrawer": "Open navigation"
}, _r = ({ controlId: l }) => {
  const t = le(), n = ae(br);
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
U("TLButton", zt);
U("TLToggleButton", Vt);
U("TLTextInput", Lt);
U("TLPasswordInput", It);
U("TLNumberInput", Pt);
U("TLDatePicker", Bt);
U("TLSelect", $t);
U("TLCheckbox", Ft);
U("TLTable", Ht);
U("TLCounter", Kt);
U("TLTabBar", Gt);
U("TLFieldList", Xt);
U("TLAudioRecorder", Zt);
U("TLAudioPlayer", Jt);
U("TLFileUpload", tn);
U("TLDownload", ln);
U("TLPhotoCapture", on);
U("TLPhotoViewer", sn);
U("TLSplitPanel", cn);
U("TLPanel", bn);
U("TLInset", Rn);
U("TLMaximizeRoot", _n);
U("TLDeckPane", vn);
U("TLSidebar", Nn);
U("TLStack", Tn);
U("TLGrid", xn);
U("TLCard", Ln);
U("TLAppBar", Dn);
U("TLBreadcrumb", Mn);
U("TLBottomBar", jn);
U("TLDialog", An);
U("TLDialogManager", Fn);
U("TLWindow", zn);
U("TLDrawer", Yn);
U("TLContextMenuRegion", Xn);
U("TLSnackbar", Jn);
U("TLMenu", tl);
U("TLAppShell", nl);
U("TLText", ll);
U("TLTableView", ol);
U("TLFormLayout", ml);
U("TLFormGroup", hl);
U("TLFormField", El);
U("TLResourceCell", gl);
U("TLTreeView", yl);
U("TLDropdownSelect", xl);
U("TLColorInput", Ul);
U("TLIconSelect", Ql);
U("TLDashboard", sr);
U("TLToolbar", mr);
U("TLTileStack", pr);
U("TLSlot", fr);
U("TLSlotContent", hr);
U("TLDrawerToggle", _r);
