import { React as e, useTLFieldValue as Te, getComponent as Rt, useTLState as q, useTLCommand as ne, TLChild as V, useTLUpload as et, useI18N as ae, useTLDataUrl as Fe, register as H } from "tl-react-bridge";
const { useCallback: Lt } = e, xt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Lt(
    (s) => {
      r(s.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const a = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    a ? "tlReactTextInput--error" : "",
    !a && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: n ?? "",
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": a || void 0,
      title: a && u ? u : void 0
    }
  ));
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Dt(
    (s) => {
      r(s.target.value);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const a = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, o = [
    "tlReactTextInput",
    a ? "tlReactTextInput--error" : "",
    !a && c ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "password",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": a || void 0,
      title: a && u ? u : void 0
    }
  ));
}, { useCallback: Mt } = e, Pt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), a = Mt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, r != null ? String(r) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, o = t.errorMessage, s = [
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
      onChange: a,
      disabled: t.disabled === !0,
      className: s,
      "aria-invalid": c || void 0,
      title: c && o ? o : void 0
    }
  ));
}, { useCallback: jt } = e, At = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = jt(
    (o) => {
      r(o.target.value || null);
    },
    [r]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, n ?? "");
  const a = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    a ? "tlReactDatePicker--error" : "",
    !a && c ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": a || void 0
    }
  ));
}, { useCallback: Bt } = e, Ot = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = Te(), a = Bt(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((h) => h.value === r)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, o = t.hasWarnings === !0, s = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && o ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: r ?? "",
      onChange: a,
      disabled: t.disabled === !0,
      className: s,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: $t } = e, Ft = ({ controlId: l, state: t }) => {
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
  const a = t.hasError === !0, c = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    a ? "tlReactCheckbox--error" : "",
    !a && c ? "tlReactCheckbox--warning" : ""
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
      "aria-invalid": a || void 0
    }
  );
}, Ht = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, n.map((c) => {
    const u = c.cellModule ? Rt(c.cellModule) : void 0, o = i[c.name];
    if (u) {
      const s = { value: o, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + a + "-" + c.name,
          state: s
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
const { useCallback: Wt } = e, zt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: a }) => {
  const c = q(), u = ne(), o = t ?? "click", s = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, h = a ?? c.displayMode ?? "label-only", v = c.hidden === !0, f = c.tooltip, S = v ? { display: "none" } : void 0, y = c.appearance, E = c.size, C = c.navigateUrl, D = Wt(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(o);
  }, [u, o, C]), x = h === "icon-only", _ = h === "icon-only" || h === "icon-label", b = h === "label-only" || h === "icon-label" || x && !m, g = f ?? (x ? s : void 0), W = g ? `text:${g}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: D,
      disabled: p,
      style: S,
      className: "tlReactButton" + (x ? " tlReactButton--iconOnly" : "") + (y === "link" ? " tlReactButton--link" : "") + (E === "small" ? " tlReactButton--small" : "") + (E === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": W,
      "aria-label": x ? s : void 0
    },
    _ && m && /* @__PURE__ */ e.createElement(Se, { encoded: m, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, s)
  );
}, { useCallback: Ut } = e, Vt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const a = q(), c = ne(), u = t ?? "click", o = n ?? a.label, s = r ?? a.active === !0, m = i ?? a.disabled === !0, p = Ut(() => {
    c(u);
  }, [c, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (s ? " tlReactButtonActive" : "")
    },
    o
  );
}, Kt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Yt } = e, Gt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.tabs ?? [], i = t.activeTabId, a = Yt((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, r.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === i,
      className: "tlReactTabBar__tab" + (c.id === i ? " tlReactTabBar__tab--active" : ""),
      onClick: () => a(c.id)
    },
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, Xt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, a) => /* @__PURE__ */ e.createElement("div", { key: a, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(V, { control: i })))));
}, qt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = q(), n = et(), [r, i] = e.useState("idle"), [a, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), s = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : r !== "idle" ? r : m, v = e.useCallback(async () => {
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
        s.current = C, o.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(C, D ? { mimeType: D } : void 0);
        u.current = x, x.ondataavailable = (_) => {
          _.data.size > 0 && o.current.push(_.data);
        }, x.onstop = async () => {
          C.getTracks().forEach((g) => g.stop()), s.current = null;
          const _ = new Blob(o.current, { type: x.mimeType || "audio/webm" });
          if (o.current = [], _.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const b = new FormData();
          b.append("audio", _, "recording.webm"), await n(b), i("idle");
        }, x.start(), i("recording");
      } catch (C) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", C), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ae(qt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], y = h === "uploading", E = ["tlAudioRecorder__button"];
  return h === "recording" && E.push("tlAudioRecorder__button--recording"), h === "uploading" && E.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: v,
      disabled: y,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[a]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Qt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Jt = ({ controlId: l }) => {
  const t = q(), n = Fe(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [a, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), s = e.useRef(i);
  e.useEffect(() => {
    r ? a === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), c("disabled"));
  }, [r]), e.useEffect(() => {
    i !== s.current && (s.current = i, u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (a === "playing" || a === "paused" || a === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (a === "disabled" || a === "loading")
      return;
    if (a === "playing") {
      u.current && u.current.pause(), c("paused");
      return;
    }
    if (a === "paused" && u.current) {
      u.current.play(), c("playing");
      return;
    }
    if (!o.current) {
      c("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), c("idle");
          return;
        }
        const E = await y.blob();
        o.current = URL.createObjectURL(E);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [a, n]), p = ae(Qt), h = a === "loading" ? p["js.loading"] : a === "playing" ? p["js.audioPlayer.pause"] : a === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = a === "disabled" || a === "loading", f = ["tlAudioPlayer__button"];
  return a === "playing" && f.push("tlAudioPlayer__button--playing"), a === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: v,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${a === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, en = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, tn = ({ controlId: l }) => {
  const t = q(), n = et(), [r, i] = e.useState("idle"), [a, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", s = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : r !== "idle" ? r : o, h = e.useCallback(async (_) => {
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
  }, []), y = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), E = e.useCallback((_) => {
    var g;
    if (_.preventDefault(), _.stopPropagation(), c(!1), r === "uploading") return;
    const b = (g = _.dataTransfer.files) == null ? void 0 : g[0];
    b && h(b);
  }, [r, h]), C = p === "uploading", D = ae(en), x = p === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${a ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: y,
      onDrop: E
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: v,
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
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    s && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, s)
  );
}, nn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, ln = ({ controlId: l }) => {
  const t = q(), n = Fe(), r = ne(), i = !!t.hasData, a = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, s] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      s(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + a, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const y = await S.blob(), E = URL.createObjectURL(y), C = document.createElement("a");
        C.href = E, C.download = c, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(E);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        s(!1);
      }
    }
  }, [i, o, n, a, c]), p = e.useCallback(async () => {
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
      onClick: m,
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
      onClick: p,
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
  const t = q(), n = et(), [r, i] = e.useState("idle"), [a, c] = e.useState(null), [u, o] = e.useState(!1), s = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), v = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), s.current && (s.current.srcObject = null);
  }, []), E = e.useCallback(() => {
    y(), i("idle");
  }, [y]), C = e.useCallback(async () => {
    var R;
    if (r !== "uploading") {
      if (c(null), !S) {
        (R = h.current) == null || R.click();
        return;
      }
      try {
        const K = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = K, i("overlayOpen");
      } catch (K) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", K), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, S]), D = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const R = s.current, K = p.current;
    if (!R || !K)
      return;
    K.width = R.videoWidth, K.height = R.videoHeight;
    const A = K.getContext("2d");
    A && (A.drawImage(R, 0, 0), y(), i("uploading"), K.toBlob(async (T) => {
      if (!T) {
        i("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", T, "capture.jpg"), await n($), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, y]), x = e.useCallback(async (R) => {
    var T;
    const K = (T = R.target.files) == null ? void 0 : T[0];
    if (!K) return;
    i("uploading");
    const A = new FormData();
    A.append("photo", K, K.name), await n(A), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && s.current && m.current && (s.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var K;
    if (r !== "overlayOpen") return;
    (K = v.current) == null || K.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const R = (K) => {
      K.key === "Escape" && E();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [r, E]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const _ = ae(rn), b = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const W = ["tlPhotoCapture__overlayVideo"];
  u && W.push("tlPhotoCapture__overlayVideo--mirrored");
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
      onChange: x
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        ref: s,
        className: W.join(" "),
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
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[a]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, an = {
  "js.photoViewer.alt": "Captured photo"
}, sn = ({ controlId: l }) => {
  const t = q(), n = Fe(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [a, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      a && (URL.revokeObjectURL(a), c(null));
      return;
    }
    if (i === u.current && a)
      return;
    u.current = i, a && (URL.revokeObjectURL(a), c(null));
    let s = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        s || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      s = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const o = ae(an);
  return !r || !a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: a,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, cn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, un = ({ controlId: l }) => {
  const t = q(), n = Fe(), r = !!t.hasPdf, i = t.dataRevision ?? 0, [a, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!r) {
      a && (URL.revokeObjectURL(a), c(null));
      return;
    }
    if (i === u.current && a)
      return;
    u.current = i, a && (URL.revokeObjectURL(a), c(null));
    let s = !1;
    return (async () => {
      try {
        const m = await fetch(n);
        if (!m.ok) {
          console.error("[TLPdfViewer] Failed to fetch PDF:", m.status);
          return;
        }
        const p = await m.blob();
        s || c(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPdfViewer] Fetch error:", m);
      }
    })(), () => {
      s = !0;
    };
  }, [r, i, n]), e.useEffect(() => () => {
    a && URL.revokeObjectURL(a);
  }, []);
  const o = ae(cn);
  return !r || !a ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, o["js.pdfViewer.noDocument"])) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: a,
      title: o["js.pdfViewer.title"]
    }
  ));
}, { useCallback: ot, useRef: He } = e, dn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.orientation, i = t.resizable === !0, a = t.children ?? [], c = r === "horizontal", u = a.length > 0 && a.every((y) => y.collapsed), o = !u && a.some((y) => y.collapsed), s = u ? !c : c, m = He(null), p = He(null), h = He(null), v = ot((y, E) => {
    const C = {
      overflow: y.scrolling || "auto"
    };
    return y.collapsed ? u && !s ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : E !== void 0 ? C.flex = `0 0 ${E}px` : C.flex = `${y.size} 1 0%`, y.minSize > 0 && !y.collapsed && (C.minWidth = c ? y.minSize : void 0, C.minHeight = c ? void 0 : y.minSize), C;
  }, [c, u, o, s]), f = ot((y, E) => {
    y.preventDefault();
    const C = m.current;
    if (!C) return;
    const D = a[E], x = a[E + 1], _ = C.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((P) => {
      b.push(c ? P.offsetWidth : P.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: E,
      startPos: c ? y.clientX : y.clientY,
      startSizeBefore: b[E],
      startSizeAfter: b[E + 1],
      childBefore: D,
      childAfter: x
    };
    const g = (P) => {
      const R = p.current;
      if (!R || !h.current) return;
      const A = (c ? P.clientX : P.clientY) - R.startPos, T = R.childBefore.minSize || 0, $ = R.childAfter.minSize || 0;
      let ee = R.startSizeBefore + A, F = R.startSizeAfter - A;
      ee < T && (F += ee - T, ee = T), F < $ && (ee += F - $, F = $), h.current[R.splitterIndex] = ee, h.current[R.splitterIndex + 1] = F;
      const Z = C.querySelectorAll(":scope > .tlSplitPanel__child"), B = Z[R.splitterIndex], j = Z[R.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${ee}px`), j && (j.style.flex = `0 0 ${F}px`);
    }, W = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", W), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const P = {};
        a.forEach((R, K) => {
          const A = R.control;
          A != null && A.controlId && h.current && (P[A.controlId] = h.current[K]);
        }), n("updateSizes", { sizes: P });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", W), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [a, c, n]), S = [];
  return a.forEach((y, E) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${E}`,
          className: `tlSplitPanel__child${y.collapsed && s ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(y)
        },
        /* @__PURE__ */ e.createElement(V, { control: y.control })
      )
    ), i && E < a.length - 1) {
      const C = a[E + 1];
      !y.collapsed && !C.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${E}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (x) => f(x, E)
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
        flexDirection: s ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    S
  );
}, { useCallback: We } = e, mn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, pn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), fn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), hn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), bn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), _n = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), vn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(mn), i = t.title, a = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, s = t.fullLine === !0, m = a === "MINIMIZED", p = a === "MAXIMIZED", h = a === "HIDDEN", v = We(() => {
    n("toggleMinimize");
  }, [n]), f = We(() => {
    n("toggleMaximize");
  }, [n]), S = We(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const y = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${a.toLowerCase()}${s ? " tlPanel--fullLine" : ""}`,
      style: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(V, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(fn, null) : /* @__PURE__ */ e.createElement(pn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(bn, null) : /* @__PURE__ */ e.createElement(hn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(_n, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(V, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(V, { control: t.buttonBar }))
  );
}, En = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(V, { control: t.child })
  );
}, gn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(V, { control: t.activeChild }));
}, { useCallback: ue, useState: Be, useEffect: Oe, useRef: $e } = e, Cn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ze(l, t, n, r) {
  const i = [];
  for (const a of l)
    if (a.type === "nav") {
      if (a.hidden) continue;
      i.push({ id: a.id, type: "nav", groupId: r });
    } else a.type === "command" ? i.push({ id: a.id, type: "command", groupId: r }) : a.type === "group" && (i.push({ id: a.id, type: "group" }), (n.get(a.id) ?? a.expanded) && !t && i.push(...Ze(a.children, t, n, a.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, wn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: a, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: a,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), yn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: a }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => a(l.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), kn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Sn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Nn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: a }) => {
  const c = $e(null);
  Oe(() => {
    const s = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => a(), 0);
    };
    return document.addEventListener("mousedown", s), () => document.removeEventListener("mousedown", s);
  }, [a]), Oe(() => {
    const s = (m) => {
      m.key === "Escape" && a();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
  }, [a]);
  const u = ue((s) => {
    s.type === "nav" ? (r(s.id), a()) : s.type === "command" && (i(s.id), a());
  }, [r, i, a]), o = {};
  return n && (o.left = n.right, o.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: o }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((s) => {
    if (s.type === "nav" && s.hidden) return null;
    if (s.type === "nav" || s.type === "command") {
      const m = s.type === "nav" && s.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: s.id,
          className: "tlSidebar__flyoutItem" + (m ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => u(s)
        },
        /* @__PURE__ */ e.createElement(Ne, { icon: s.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, s.label),
        s.type === "nav" && s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, s.badge)
      );
    }
    return s.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: s.id, className: "tlSidebar__flyoutSectionHeader" }, s.label) : s.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: s.id, className: "tlSidebar__separator" }) : null;
  }));
}, Tn = ({
  item: l,
  expanded: t,
  activeItemId: n,
  collapsed: r,
  onSelect: i,
  onExecute: a,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: o,
  onFocus: s,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const y = $e(null), [E, C] = Be(null), D = ue(() => {
    r ? v === l.id ? S() : (y.current && C(y.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, v, l.id, c, f, S]), x = ue((b) => {
    y.current = b, o(b);
  }, [o]), _ = r && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: r ? l.label : void 0,
      "aria-expanded": r ? _ : t,
      tabIndex: u,
      ref: x,
      onFocus: () => s(l.id)
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
    Nn,
    {
      item: l,
      activeItemId: n,
      anchorRect: E,
      onSelect: i,
      onExecute: a,
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
      onExecute: a,
      onToggleGroup: c,
      focusedId: m,
      setItemRef: p,
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
  onToggleGroup: a,
  focusedId: c,
  setItemRef: u,
  onItemFocus: o,
  groupStates: s,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return l.hidden ? null : /* @__PURE__ */ e.createElement(
        wn,
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
        yn,
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
      return /* @__PURE__ */ e.createElement(kn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Sn, null);
    case "group": {
      const v = s ? s.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Tn,
        {
          item: l,
          expanded: v,
          activeItemId: t,
          collapsed: n,
          onSelect: r,
          onExecute: i,
          onToggleGroup: a,
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
}, Rn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(Cn), i = t.items ?? [], a = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [s, m] = Be(() => {
    const T = /* @__PURE__ */ new Map(), $ = (ee) => {
      for (const F of ee)
        F.type === "group" && (T.set(F.id, F.expanded), $(F.children));
    };
    return $(i), T;
  }), p = ue((T) => {
    m(($) => {
      const ee = new Map($), F = ee.get(T) ?? !1;
      return ee.set(T, !F), n("toggleGroup", { itemId: T, expanded: !F }), ee;
    });
  }, [n]), h = ue((T) => {
    T !== a && n("selectItem", { itemId: T });
  }, [n, a]), v = ue((T) => {
    n("executeCommand", { itemId: T });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [y, E] = Be(null), C = ue((T) => {
    E(T);
  }, []), D = ue(() => {
    E(null);
  }, []);
  Oe(() => {
    o || E(null);
  }, [o]);
  const [x, _] = Be(() => {
    const T = Ze(i, o, s);
    return T.length > 0 ? T[0].id : "";
  }), b = $e(/* @__PURE__ */ new Map()), g = ue((T) => ($) => {
    $ ? b.current.set(T, $) : b.current.delete(T);
  }, []), W = ue((T) => {
    _(T);
  }, []), P = $e(0), R = ue((T) => {
    _(T), P.current++;
  }, []);
  Oe(() => {
    const T = b.current.get(x);
    T && document.activeElement !== T && T.focus();
  }, [x, P.current]);
  const K = ue((T) => {
    if (T.key === "Escape" && y !== null) {
      T.preventDefault(), D();
      return;
    }
    const $ = Ze(i, o, s);
    if ($.length === 0) return;
    const ee = $.findIndex((Z) => Z.id === x);
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
        T.preventDefault(), F.type === "nav" ? h(F.id) : F.type === "command" ? v(F.id) : F.type === "group" && (o ? y === F.id ? D() : C(F.id) : p(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !o && ((s.get(F.id) ?? !1) || (T.preventDefault(), p(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !o && (s.get(F.id) ?? !1) && (T.preventDefault(), p(F.id));
        break;
      }
    }
  }, [
    i,
    o,
    s,
    x,
    y,
    R,
    h,
    v,
    p,
    C,
    D
  ]), A = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(V, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, i.map((T) => /* @__PURE__ */ e.createElement(
    gt,
    {
      key: T.id,
      item: T,
      activeItemId: a,
      collapsed: o,
      onSelect: h,
      onExecute: v,
      onToggleGroup: p,
      focusedId: x,
      setItemRef: g,
      onItemFocus: W,
      groupStates: s,
      flyoutGroupId: y,
      onOpenFlyout: C,
      onCloseFlyout: D
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(V, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(V, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(V, { control: t.activeContent })));
}, Ln = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", a = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], o = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    a ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: o }, u.map((s, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: s })));
}, xn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(V, { control: t.child }));
}, Dn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", a = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, a.map((u, o) => /* @__PURE__ */ e.createElement(V, { key: o, control: u })));
}, In = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", a = t.headerActions ?? [], c = t.child, u = n != null || a.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, a.map((o, s) => /* @__PURE__ */ e.createElement(V, { key: s, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(V, { control: c })));
}, Mn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], a = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(V, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((s, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: s }))), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, a.map((s, m) => /* @__PURE__ */ e.createElement(V, { key: m, control: s }))));
}, { useCallback: Pn } = e, jn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = Pn((a) => {
    n("navigate", { itemId: a });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, r.map((a, c) => {
    const u = c === r.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: a.id, className: "tlBreadcrumb__entry" }, c > 0 && /* @__PURE__ */ e.createElement(
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
    ), u ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, a.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => i(a.id)
      },
      a.label
    ));
  })));
}, { useCallback: An } = e, Bn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = t.activeItemId, a = An((c) => {
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
        onClick: () => a(c.id),
        "aria-current": u ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + c.icon, "aria-hidden": "true" }), c.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, c.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, c.label)
    );
  }));
}, { useCallback: at, useEffect: st, useRef: On } = e, $n = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.closeOnBackdrop !== !1, a = t.child, c = On(null), u = at(() => {
    n("close");
  }, [n]), o = at((s) => {
    i && s.target === s.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!r) return;
    const s = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", s), () => document.removeEventListener("keydown", s);
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
    /* @__PURE__ */ e.createElement(V, { control: a })
  ) : null;
}, { useEffect: Fn, useRef: Hn } = e, Wn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = Hn(n.length);
  return Fn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(V, { key: i.controlId, control: i })));
}, { useCallback: De, useRef: Ce, useState: Ie } = e, zn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Un = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Vn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(zn), i = t.title ?? "", a = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, s = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [v, f] = Ie(null), [S, y] = Ie(null), [E, C] = Ie(null), D = Ce(null), [x, _] = Ie(!1), b = Ce(null), g = Ce(null), W = Ce(null), P = Ce(null), R = Ce(null), K = De(() => {
    n("close");
  }, [n]), A = De((Z, B) => {
    B.preventDefault();
    const j = P.current;
    if (!j) return;
    const Y = j.getBoundingClientRect(), d = !D.current, k = D.current ?? { x: Y.left, y: Y.top };
    d && (D.current = k, C(k)), R.current = {
      dir: Z,
      startX: B.clientX,
      startY: B.clientY,
      startW: Y.width,
      startH: Y.height,
      startPos: { ...k },
      symmetric: d
    };
    const z = (G) => {
      const M = R.current;
      if (!M) return;
      const Q = G.clientX - M.startX, le = G.clientY - M.startY;
      let te = M.startW, ie = M.startH, de = 0, me = 0;
      M.symmetric ? (M.dir.includes("e") && (te = M.startW + 2 * Q), M.dir.includes("w") && (te = M.startW - 2 * Q), M.dir.includes("s") && (ie = M.startH + 2 * le), M.dir.includes("n") && (ie = M.startH - 2 * le)) : (M.dir.includes("e") && (te = M.startW + Q), M.dir.includes("w") && (te = M.startW - Q, de = Q), M.dir.includes("s") && (ie = M.startH + le), M.dir.includes("n") && (ie = M.startH - le, me = le));
      const he = Math.max(200, te), be = Math.max(100, ie);
      M.symmetric ? (de = (M.startW - he) / 2, me = (M.startH - be) / 2) : (M.dir.includes("w") && he === 200 && (de = M.startW - 200), M.dir.includes("n") && be === 100 && (me = M.startH - 100)), g.current = he, W.current = be, f(he), y(be);
      const N = {
        x: M.startPos.x + de,
        y: M.startPos.y + me
      };
      D.current = N, C(N);
    }, O = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", O);
      const G = g.current, M = W.current;
      (G != null || M != null) && n("resize", {
        ...G != null ? { width: Math.round(G) + "px" } : {},
        ...M != null ? { height: Math.round(M) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", O);
  }, [n]), T = De((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const B = P.current;
    if (!B) return;
    const j = B.getBoundingClientRect(), Y = D.current ?? { x: j.left, y: j.top }, d = Z.clientX - Y.x, k = Z.clientY - Y.y, z = (G) => {
      const M = window.innerWidth, Q = window.innerHeight;
      let le = G.clientX - d, te = G.clientY - k;
      const ie = B.offsetWidth, de = B.offsetHeight;
      le + ie > M && (le = M - ie), te + de > Q && (te = Q - de), le < 0 && (le = 0), te < 0 && (te = 0);
      const me = { x: le, y: te };
      D.current = me, C(me);
    }, O = () => {
      document.removeEventListener("mousemove", z), document.removeEventListener("mouseup", O);
    };
    document.addEventListener("mousemove", z), document.addEventListener("mouseup", O);
  }, []), $ = De(() => {
    var Z, B;
    if (x) {
      const j = b.current;
      j && (C(j.x !== -1 ? { x: j.x, y: j.y } : null), f(j.w), y(j.h)), _(!1);
    } else {
      const j = P.current, Y = j == null ? void 0 : j.getBoundingClientRect();
      b.current = {
        x: ((Z = D.current) == null ? void 0 : Z.x) ?? (Y == null ? void 0 : Y.left) ?? -1,
        y: ((B = D.current) == null ? void 0 : B.y) ?? (Y == null ? void 0 : Y.top) ?? -1,
        w: v ?? (Y == null ? void 0 : Y.width) ?? null,
        h: S ?? null
      }, _(!0), C({ x: 0, y: 0 }), f(null), y(null);
    }
  }, [x, v, S]), ee = x ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : a,
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
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : T,
        onDoubleClick: o ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(V, { control: p })),
      o && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: $,
          title: x ? r["js.window.restore"] : r["js.window.maximize"]
        },
        x ? (
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
          onClick: K,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(V, { control: s })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(V, { control: h }), m.map((Z, B) => /* @__PURE__ */ e.createElement(V, { key: B, control: Z }))),
    o && !x && Un.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (B) => A(Z, B)
      }
    ))
  );
}, { useCallback: Kn, useEffect: Yn } = e, Gn = {
  "js.drawer.close": "Close"
}, Xn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(Gn), i = t.open === !0, a = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, s = Kn(() => {
    n("close");
  }, [n]);
  Yn(() => {
    if (!i) return;
    const p = (h) => {
      h.key === "Escape" && s();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [i, s]);
  const m = [
    "tlDrawer",
    `tlDrawer--${a}`,
    `tlDrawer--${c}`,
    i ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !i }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: s,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(V, { control: o })));
}, { useCallback: qn } = e, Zn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.child, i = qn((a) => {
    a.preventDefault(), a.stopPropagation(), n("openContextMenu", { x: a.clientX, y: a.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(V, { control: r }));
}, { useCallback: Qn, useEffect: Jn, useState: el } = e, tl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.message ?? "", i = t.content ?? "", a = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [s, m] = el(!1), p = Qn(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return Jn(() => {
    if (!u || c === 0) return;
    const h = setTimeout(p, c);
    return () => clearTimeout(h);
  }, [u, c, p]), !u && !s ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${a}${s ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, r)
  );
}, { useCallback: ze, useEffect: Ue, useRef: nl, useState: ct } = e, ll = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.anchorId, a = t.anchorX, c = t.anchorY, u = t.items ?? [], o = nl(null), [s, m] = ct({ top: 0, left: 0 }), [p, h] = ct(0), v = u.filter((E) => E.type === "item" && !E.disabled);
  Ue(() => {
    var g, W;
    if (!r) return;
    const E = ((g = o.current) == null ? void 0 : g.offsetHeight) ?? 200, C = ((W = o.current) == null ? void 0 : W.offsetWidth) ?? 200;
    if (a != null && c != null) {
      let P = c, R = a;
      P + E > window.innerHeight && (P = Math.max(0, window.innerHeight - E)), R + C > window.innerWidth && (R = Math.max(0, window.innerWidth - C)), m({ top: P, left: R }), h(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const x = D.getBoundingClientRect();
    let _ = x.bottom + 4, b = x.left;
    _ + E > window.innerHeight && (_ = x.top - E - 4), b + C > window.innerWidth && (b = x.right - C), m({ top: _, left: b }), h(0);
  }, [r, i, a, c]);
  const f = ze(() => {
    n("close");
  }, [n]), S = ze((E) => {
    n("selectItem", { itemId: E });
  }, [n]);
  Ue(() => {
    if (!r) return;
    const E = (C) => {
      o.current && !o.current.contains(C.target) && f();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [r, f]);
  const y = ze((E) => {
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
      const C = v[p];
      C && S(C.id);
    }
  }, [f, S, v, p]);
  return Ue(() => {
    r && o.current && o.current.focus();
  }, [r]), r ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: o,
      tabIndex: -1,
      style: { position: "fixed", top: s.top, left: s.left },
      onKeyDown: y
    },
    u.map((E, C) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const x = v.indexOf(E) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: E.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (E.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: E.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => S(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + E.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, rl = 768, ol = ({ controlId: l }) => {
  const t = q(), n = ne();
  e.useEffect(() => {
    const s = window.matchMedia(`(max-width: ${rl}px)`), m = (h) => {
      n("reportDisplayClass", { displayClass: h ? "COMPACT" : "REGULAR" });
    };
    m(s.matches);
    const p = (h) => m(h.matches);
    return s.addEventListener("change", p), () => s.removeEventListener("change", p);
  }, [n]);
  const r = t.header, i = t.content, a = t.footer, c = t.snackbar, u = t.dialogManager, o = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(V, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(V, { control: i })), a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(V, { control: a })), /* @__PURE__ */ e.createElement(V, { control: c }), u && /* @__PURE__ */ e.createElement(V, { control: u }), o && /* @__PURE__ */ e.createElement(V, { control: o }));
}, al = ({ controlId: l }) => {
  const t = q(), n = t.text ?? "", r = t.cssClass ?? "", i = t.hasTooltip === !0, a = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: a,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, sl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, cl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(sl), i = e.useRef(null);
  e.useEffect(() => {
    const N = i.current;
    if (!N) return;
    const I = (w) => {
      const L = w.detail;
      let U = L.target;
      for (; U && U !== N; ) {
        const J = U.dataset.row, re = U.dataset.col;
        if (J != null && re != null) {
          L.resolved = { key: J + "|" + re };
          return;
        }
        U = U.parentElement;
      }
    };
    return N.addEventListener("tl-tooltip-resolve", I), () => N.removeEventListener("tl-tooltip-resolve", I);
  }, []);
  const a = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, s = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, v = e.useMemo(
    () => a.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [a]
  ), f = s === "multi", S = 40, y = 20, E = e.useRef(null), C = e.useRef(null), D = e.useRef(null), [x, _] = e.useState({}), b = e.useRef(null), g = e.useRef(!1), W = e.useRef(null), [P, R] = e.useState(null), [K, A] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [a]);
  const T = e.useCallback((N) => x[N.name] ?? N.width, [x]), $ = e.useMemo(() => {
    const N = [];
    let I = f && p > 0 ? S : 0;
    for (let w = 0; w < p && w < a.length; w++)
      N.push(I), I += T(a[w]);
    return N;
  }, [a, p, f, S, T]), ee = c * o, F = e.useRef(null), Z = e.useCallback((N, I, w) => {
    w.preventDefault(), w.stopPropagation(), b.current = { column: N, startX: w.clientX, startWidth: I };
    let L = w.clientX, U = 0;
    const J = () => {
      const oe = b.current;
      if (!oe) return;
      const pe = Math.max(it, oe.startWidth + (L - oe.startX) + U);
      _((ge) => ({ ...ge, [oe.column]: pe }));
    }, re = () => {
      const oe = C.current, pe = E.current;
      if (!oe || !b.current) return;
      const ge = oe.getBoundingClientRect(), nt = 40, lt = 8, Tt = oe.scrollLeft;
      L > ge.right - nt ? oe.scrollLeft += lt : L < ge.left + nt && (oe.scrollLeft = Math.max(0, oe.scrollLeft - lt));
      const rt = oe.scrollLeft - Tt;
      rt !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), U += rt, J()), F.current = requestAnimationFrame(re);
    };
    F.current = requestAnimationFrame(re);
    const Ee = (oe) => {
      L = oe.clientX, J();
    }, xe = (oe) => {
      document.removeEventListener("mousemove", Ee), document.removeEventListener("mouseup", xe), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = b.current;
      if (pe) {
        const ge = Math.max(it, pe.startWidth + (oe.clientX - pe.startX) + U);
        n("columnResize", { column: pe.column, width: ge }), b.current = null, g.current = !0, requestAnimationFrame(() => {
          g.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", Ee), document.addEventListener("mouseup", xe);
  }, [n]), B = e.useCallback(() => {
    E.current && C.current && (E.current.scrollLeft = C.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const N = C.current;
      if (!N) return;
      const I = N.scrollTop, w = Math.ceil(N.clientHeight / o), L = Math.floor(I / o);
      n("scroll", { start: L, count: w });
    }, 80);
  }, [n, o]), j = e.useCallback((N, I, w) => {
    if (g.current) return;
    let L;
    !I || I === "desc" ? L = "asc" : L = "desc";
    const U = w.shiftKey ? "add" : "replace";
    n("sort", { column: N, direction: L, mode: U });
  }, [n]), Y = e.useCallback((N, I) => {
    W.current = N, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", N);
  }, []), d = e.useCallback((N, I) => {
    if (!W.current || W.current === N) {
      R(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const w = I.currentTarget.getBoundingClientRect(), L = I.clientX < w.left + w.width / 2 ? "left" : "right";
    R({ column: N, side: L });
  }, []), k = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const I = W.current;
    if (!I || !P) {
      W.current = null, R(null);
      return;
    }
    let w = a.findIndex((U) => U.name === P.column);
    if (w < 0) {
      W.current = null, R(null);
      return;
    }
    const L = a.findIndex((U) => U.name === I);
    P.side === "right" && w++, L < w && w--, n("columnReorder", { column: I, targetIndex: w }), W.current = null, R(null);
  }, [a, P, n]), z = e.useCallback(() => {
    W.current = null, R(null);
  }, []), O = e.useCallback((N, I) => {
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
  }, [n, m, c]), Q = e.useCallback((N, I, w) => {
    w.stopPropagation(), n("expand", { rowIndex: N, expanded: I });
  }, [n]), le = e.useCallback((N, I) => {
    I.preventDefault(), A({ x: I.clientX, y: I.clientY, colIdx: N });
  }, []), te = e.useCallback(() => {
    K && (n("setFrozenColumnCount", { count: K.colIdx + 1 }), A(null));
  }, [K, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), A(null);
  }, [n]);
  e.useEffect(() => {
    if (!K) return;
    const N = () => A(null), I = (w) => {
      w.key === "Escape" && A(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", I);
    };
  }, [K]);
  const de = a.reduce((N, I) => N + T(I), 0) + (f ? S : 0), me = m === c && c > 0, he = m > 0 && m < c, be = e.useCallback((N) => {
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
        if (!W.current) return;
        N.preventDefault();
        const I = C.current, w = E.current;
        if (!I) return;
        const L = I.getBoundingClientRect(), U = 40, J = 8;
        N.clientX < L.left + U ? I.scrollLeft = Math.max(0, I.scrollLeft - J) : N.clientX > L.right - U && (I.scrollLeft += J), w && (w.scrollLeft = I.scrollLeft);
      },
      onDrop: k
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: de } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          W.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", a.length > 0 && a[0].name !== W.current && R({ column: a[0].name, side: "left" }));
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
    ), a.map((N, I) => {
      const w = T(N);
      a.length - 1;
      let L = "tlTableView__headerCell";
      N.sortable && (L += " tlTableView__headerCell--sortable"), P && P.column === N.name && (L += " tlTableView__headerCell--dragOver-" + P.side);
      const U = I < p, J = I === p - 1;
      return U && (L += " tlTableView__headerCell--frozen"), J && (L += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: L,
          style: {
            width: w,
            minWidth: w,
            position: U ? "sticky" : "relative",
            ...U ? { left: $[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (re) => j(N.name, N.sortDirection, re) : void 0,
          onContextMenu: (re) => le(I, re),
          onDragStart: (re) => Y(N.name, re),
          onDragOver: (re) => d(N.name, re),
          onDrop: k,
          onDragEnd: z
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", v > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => Z(N.name, w, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (W.current && a.length > 0) {
            const I = a[a.length - 1];
            I.name !== W.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", R({ column: I.name, side: "right" }));
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
        a.map((I, w) => {
          const L = T(I), U = w === a.length - 1, J = w < p, re = w === p - 1;
          let Ee = "tlTableView__cell";
          J && (Ee += " tlTableView__cell--frozen"), re && (Ee += " tlTableView__cell--frozenLast");
          const xe = h && w === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: Ee,
              "data-row": N.id,
              "data-col": I.name,
              style: {
                ...U && !J ? { flex: "1 0 auto", minWidth: L } : { width: L, minWidth: L },
                ...J ? { position: "sticky", left: $[w], zIndex: 2 } : {}
              }
            },
            xe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * y } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => Q(N.index, !N.expanded, pe)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(V, { control: N.cells[I.name] })) : /* @__PURE__ */ e.createElement(V, { control: N.cells[I.name] })
          );
        })
      )))
    ),
    K && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: K.y, left: K.x, zIndex: 1e4 },
        onMouseDown: (N) => N.stopPropagation()
      },
      K.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, il = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ct = e.createContext(il), { useMemo: ul, useRef: dl, useState: ml, useEffect: pl } = e, fl = 320, hl = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, a = t.children ?? [], c = t.noModelMessage, u = dl(null), [o, s] = ml(
    r === "top" ? "top" : "side"
  );
  pl(() => {
    if (r !== "auto") {
      s(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((y) => {
      for (const E of y) {
        const D = E.contentRect.width / n;
        s(D < fl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const m = ul(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: h, ref: u }, a.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f }))));
}, { useCallback: bl } = e, _l = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, vl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = ae(_l), i = t.headerControl ?? null, a = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", s = t.fullLine === !0, m = t.children ?? [], p = i != null || a.length > 0 || c, h = bl(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    s ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: v }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, c && /* @__PURE__ */ e.createElement(
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(V, { control: i })), a.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, a.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(V, { key: S, control: f }))));
}, { useContext: El, useState: gl, useCallback: Cl } = e, wl = ({ controlId: l }) => {
  const t = q(), n = El(Ct), r = t.label ?? "", i = t.required === !0, a = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, s = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, v = t.field, f = n.readOnly, [S, y] = gl(!1), E = Cl(() => y((_) => !_), []);
  if (!p) return null;
  const C = a != null, D = c != null && c.length > 0, x = [
    "tlFormField",
    `tlFormField--${s}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && D ? "tlFormField--warning" : "",
    o ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: x }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(V, { control: v })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, a)), !f && !C && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
}, yl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.iconCss, i = t.iconSrc, a = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, s, a && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, a)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", c].filter(Boolean).join(" "), v = u ? "key:tooltip" : void 0;
  return o ? /* @__PURE__ */ e.createElement(
    "a",
    {
      id: l,
      className: h,
      href: "#",
      onClick: p,
      "data-tooltip": v
    },
    m
  ) : /* @__PURE__ */ e.createElement("span", { id: l, className: h, "data-tooltip": v }, m);
}, kl = 20, Sl = () => {
  const l = q(), t = ne(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, a = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, s] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, b) => {
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
    const g = b.getBoundingClientRect(), W = _.clientY - g.top, P = g.height / 3;
    return W < P ? "above" : W > P * 2 ? "below" : "within";
  }, []), y = e.useCallback((_, b) => {
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
  }, [t]), x = e.useCallback((_) => {
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
            const W = g.depth;
            for (let P = o - 1; P >= 0; P--)
              if (n[P].depth < W) {
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
    b !== o && s(b);
  }, [o, n, t, r]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: x
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
        style: { paddingLeft: _.depth * kl },
        draggable: i,
        onClick: (g) => h(_.id, g),
        onContextMenu: (g) => v(_.id, g),
        onDragStart: (g) => y(_.id, g),
        onDragOver: a ? (g) => E(_.id, g) : void 0,
        onDrop: a ? (g) => C(_.id, g) : void 0,
        onDragEnd: D
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), p(_.id, _.expanded);
          },
          tabIndex: -1,
          "aria-label": _.expanded ? "Collapse" : "Expand"
        },
        _.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: _.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(V, { control: _.content }))
    ))
  );
};
var Ve = { exports: {} }, se = {}, Ke = { exports: {} }, X = {};
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
function Nl() {
  if (ut) return X;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), a = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), s = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
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
  }, S = Object.assign, y = {};
  function E(d, k, z) {
    this.props = d, this.context = k, this.refs = y, this.updater = z || f;
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
  function D(d, k, z) {
    this.props = d, this.context = k, this.refs = y, this.updater = z || f;
  }
  var x = D.prototype = new C();
  x.constructor = D, S(x, E.prototype), x.isPureReactComponent = !0;
  var _ = Array.isArray;
  function b() {
  }
  var g = { H: null, A: null, T: null, S: null }, W = Object.prototype.hasOwnProperty;
  function P(d, k, z) {
    var O = z.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: O !== void 0 ? O : null,
      props: z
    };
  }
  function R(d, k) {
    return P(d.type, k, d.props);
  }
  function K(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(z) {
      return k[z];
    });
  }
  var T = /\/+/g;
  function $(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : k.toString(36);
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
  function F(d, k, z, O, G) {
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
                k,
                z,
                O,
                G
              );
          }
      }
    if (Q)
      return G = G(d), Q = O === "" ? "." + $(d, 0) : O, _(G) ? (z = "", Q != null && (z = Q.replace(T, "$&/") + "/"), F(G, k, z, "", function(ie) {
        return ie;
      })) : G != null && (K(G) && (G = R(
        G,
        z + (G.key == null || d && d.key === G.key ? "" : ("" + G.key).replace(
          T,
          "$&/"
        ) + "/") + Q
      )), k.push(G)), 1;
    Q = 0;
    var le = O === "" ? "." : O + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        O = d[te], M = le + $(O, te), Q += F(
          O,
          k,
          z,
          M,
          G
        );
    else if (te = v(d), typeof te == "function")
      for (d = te.call(d), te = 0; !(O = d.next()).done; )
        O = O.value, M = le + $(O, te++), Q += F(
          O,
          k,
          z,
          M,
          G
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return F(
          ee(d),
          k,
          z,
          O,
          G
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, k, z) {
    if (d == null) return d;
    var O = [], G = 0;
    return F(d, O, "", "", function(M) {
      return k.call(z, M, G++);
    }), O;
  }
  function B(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(z) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = z);
        },
        function(z) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = z);
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
    forEach: function(d, k, z) {
      Z(
        d,
        function() {
          k.apply(this, arguments);
        },
        z
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
      if (!K(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return X.Activity = p, X.Children = Y, X.Component = E, X.Fragment = n, X.Profiler = i, X.PureComponent = D, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, X.__COMPILER_RUNTIME = {
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
  }, X.cloneElement = function(d, k, z) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var O = S({}, d.props), G = d.key;
    if (k != null)
      for (M in k.key !== void 0 && (G = "" + k.key), k)
        !W.call(k, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && k.ref === void 0 || (O[M] = k[M]);
    var M = arguments.length - 2;
    if (M === 1) O.children = z;
    else if (1 < M) {
      for (var Q = Array(M), le = 0; le < M; le++)
        Q[le] = arguments[le + 2];
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
      $$typeof: a,
      _context: d
    }, d;
  }, X.createElement = function(d, k, z) {
    var O, G = {}, M = null;
    if (k != null)
      for (O in k.key !== void 0 && (M = "" + k.key), k)
        W.call(k, O) && O !== "key" && O !== "__self" && O !== "__source" && (G[O] = k[O]);
    var Q = arguments.length - 2;
    if (Q === 1) G.children = z;
    else if (1 < Q) {
      for (var le = Array(Q), te = 0; te < Q; te++)
        le[te] = arguments[te + 2];
      G.children = le;
    }
    if (d && d.defaultProps)
      for (O in Q = d.defaultProps, Q)
        G[O] === void 0 && (G[O] = Q[O]);
    return P(d, M, G);
  }, X.createRef = function() {
    return { current: null };
  }, X.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, X.isValidElement = K, X.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, X.memo = function(d, k) {
    return {
      $$typeof: s,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, X.startTransition = function(d) {
    var k = g.T, z = {};
    g.T = z;
    try {
      var O = d(), G = g.S;
      G !== null && G(z, O), typeof O == "object" && O !== null && typeof O.then == "function" && O.then(b, j);
    } catch (M) {
      j(M);
    } finally {
      k !== null && z.types !== null && (k.types = z.types), g.T = k;
    }
  }, X.unstable_useCacheRefresh = function() {
    return g.H.useCacheRefresh();
  }, X.use = function(d) {
    return g.H.use(d);
  }, X.useActionState = function(d, k, z) {
    return g.H.useActionState(d, k, z);
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
  }, X.useImperativeHandle = function(d, k, z) {
    return g.H.useImperativeHandle(d, k, z);
  }, X.useInsertionEffect = function(d, k) {
    return g.H.useInsertionEffect(d, k);
  }, X.useLayoutEffect = function(d, k) {
    return g.H.useLayoutEffect(d, k);
  }, X.useMemo = function(d, k) {
    return g.H.useMemo(d, k);
  }, X.useOptimistic = function(d, k) {
    return g.H.useOptimistic(d, k);
  }, X.useReducer = function(d, k, z) {
    return g.H.useReducer(d, k, z);
  }, X.useRef = function(d) {
    return g.H.useRef(d);
  }, X.useState = function(d) {
    return g.H.useState(d);
  }, X.useSyncExternalStore = function(d, k, z) {
    return g.H.useSyncExternalStore(
      d,
      k,
      z
    );
  }, X.useTransition = function() {
    return g.H.useTransition();
  }, X.version = "19.2.4", X;
}
var dt;
function Tl() {
  return dt || (dt = 1, Ke.exports = Nl()), Ke.exports;
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
function Rl() {
  if (mt) return se;
  mt = 1;
  var l = Tl();
  function t(o) {
    var s = "https://react.dev/errors/" + o;
    if (1 < arguments.length) {
      s += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        s += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + o + "; visit " + s + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function a(o, s, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: o,
      containerInfo: s,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(o, s) {
    if (o === "font") return "";
    if (typeof s == "string")
      return s === "use-credentials" ? s : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, se.createPortal = function(o, s) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!s || s.nodeType !== 1 && s.nodeType !== 9 && s.nodeType !== 11)
      throw Error(t(299));
    return a(o, s, null, m);
  }, se.flushSync = function(o) {
    var s = c.T, m = r.p;
    try {
      if (c.T = null, r.p = 2, o) return o();
    } finally {
      c.T = s, r.p = m, r.d.f();
    }
  }, se.preconnect = function(o, s) {
    typeof o == "string" && (s ? (s = s.crossOrigin, s = typeof s == "string" ? s === "use-credentials" ? s : "" : void 0) : s = null, r.d.C(o, s));
  }, se.prefetchDNS = function(o) {
    typeof o == "string" && r.d.D(o);
  }, se.preinit = function(o, s) {
    if (typeof o == "string" && s && typeof s.as == "string") {
      var m = s.as, p = u(m, s.crossOrigin), h = typeof s.integrity == "string" ? s.integrity : void 0, v = typeof s.fetchPriority == "string" ? s.fetchPriority : void 0;
      m === "style" ? r.d.S(
        o,
        typeof s.precedence == "string" ? s.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: v
        }
      ) : m === "script" && r.d.X(o, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: v,
        nonce: typeof s.nonce == "string" ? s.nonce : void 0
      });
    }
  }, se.preinitModule = function(o, s) {
    if (typeof o == "string")
      if (typeof s == "object" && s !== null) {
        if (s.as == null || s.as === "script") {
          var m = u(
            s.as,
            s.crossOrigin
          );
          r.d.M(o, {
            crossOrigin: m,
            integrity: typeof s.integrity == "string" ? s.integrity : void 0,
            nonce: typeof s.nonce == "string" ? s.nonce : void 0
          });
        }
      } else s == null && r.d.M(o);
  }, se.preload = function(o, s) {
    if (typeof o == "string" && typeof s == "object" && s !== null && typeof s.as == "string") {
      var m = s.as, p = u(m, s.crossOrigin);
      r.d.L(o, m, {
        crossOrigin: p,
        integrity: typeof s.integrity == "string" ? s.integrity : void 0,
        nonce: typeof s.nonce == "string" ? s.nonce : void 0,
        type: typeof s.type == "string" ? s.type : void 0,
        fetchPriority: typeof s.fetchPriority == "string" ? s.fetchPriority : void 0,
        referrerPolicy: typeof s.referrerPolicy == "string" ? s.referrerPolicy : void 0,
        imageSrcSet: typeof s.imageSrcSet == "string" ? s.imageSrcSet : void 0,
        imageSizes: typeof s.imageSizes == "string" ? s.imageSizes : void 0,
        media: typeof s.media == "string" ? s.media : void 0
      });
    }
  }, se.preloadModule = function(o, s) {
    if (typeof o == "string")
      if (s) {
        var m = u(s.as, s.crossOrigin);
        r.d.m(o, {
          as: typeof s.as == "string" && s.as !== "script" ? s.as : void 0,
          crossOrigin: m,
          integrity: typeof s.integrity == "string" ? s.integrity : void 0
        });
      } else r.d.m(o);
  }, se.requestFormReset = function(o) {
    r.d.r(o);
  }, se.unstable_batchedUpdates = function(o, s) {
    return o(s);
  }, se.useFormState = function(o, s, m) {
    return c.H.useFormState(o, s, m);
  }, se.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var pt;
function Ll() {
  if (pt) return Ve.exports;
  pt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ve.exports = Rl(), Ve.exports;
}
var wt = Ll();
const { useState: _e, useCallback: ce, useRef: Re, useEffect: we, useMemo: Qe } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function xl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: r,
  draggable: i,
  onDragStart: a,
  onDragOver: c,
  onDrop: u,
  onDragEnd: o,
  dragClassName: s
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
      className: "tlDropdownSelect__chip" + (s ? " " + s : ""),
      draggable: i || void 0,
      onDragStart: a,
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
function Dl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: a
}) {
  const c = ce(() => r(l.value), [r, l.value]), u = Qe(() => {
    if (!n) return l.label;
    const o = l.label.toLowerCase().indexOf(n.toLowerCase());
    return o < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, o), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(o, o + n.length)), l.label.substring(o + n.length));
  }, [l.label, n]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: a,
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
const Il = ({ controlId: l, state: t }) => {
  const n = ne(), r = t.value ?? [], i = t.multiSelect === !0, a = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, s = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = a && i && !u && o, v = ae({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = v["js.dropdownSelect.nothingFound"], S = ce(
    (w) => v["js.dropdownSelect.removeChip"].replace("{0}", w),
    [v]
  ), [y, E] = _e(!1), [C, D] = _e(""), [x, _] = _e(-1), [b, g] = _e(!1), [W, P] = _e({}), [R, K] = _e(null), [A, T] = _e(null), [$, ee] = _e(null), F = Re(null), Z = Re(null), B = Re(null), j = Re(r);
  j.current = r;
  const Y = Re(-1), d = Qe(
    () => new Set(r.map((w) => w.value)),
    [r]
  ), k = Qe(() => {
    let w = m.filter((L) => !d.has(L.value));
    if (C) {
      const L = C.toLowerCase();
      w = w.filter((U) => U.label.toLowerCase().includes(L));
    }
    return w;
  }, [m, d, C]);
  we(() => {
    C && k.length === 1 ? _(0) : _(-1);
  }, [k.length, C]), we(() => {
    y && s && Z.current && Z.current.focus();
  }, [y, s, r]), we(() => {
    var U, J;
    if (Y.current < 0) return;
    const w = Y.current;
    Y.current = -1;
    const L = (U = F.current) == null ? void 0 : U.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    L && L.length > 0 ? L[Math.min(w, L.length - 1)].focus() : (J = F.current) == null || J.focus();
  }, [r]), we(() => {
    if (!y) return;
    const w = (L) => {
      F.current && !F.current.contains(L.target) && B.current && !B.current.contains(L.target) && (E(!1), D(""));
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [y]), we(() => {
    if (!y || !F.current) return;
    const w = F.current.getBoundingClientRect(), L = window.innerHeight - w.bottom, J = L < 300 && w.top > L;
    P({
      left: w.left,
      width: w.width,
      ...J ? { bottom: window.innerHeight - w.top } : { top: w.bottom }
    });
  }, [y]);
  const z = ce(async () => {
    if (!(u || !o) && (E(!0), D(""), _(-1), g(!1), !s))
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
  }, [u, o, s, n]), O = ce(() => {
    var w;
    E(!1), D(""), _(-1), (w = F.current) == null || w.focus();
  }, []), G = ce(
    (w) => {
      let L;
      if (i) {
        const U = m.find((J) => J.value === w);
        if (U)
          L = [...j.current, U];
        else
          return;
      } else {
        const U = m.find((J) => J.value === w);
        if (U)
          L = [U];
        else
          return;
      }
      j.current = L, n("valueChanged", { value: L.map((U) => U.value) }), i ? (D(""), _(-1)) : O();
    },
    [i, m, n, O]
  ), M = ce(
    (w) => {
      Y.current = j.current.findIndex((U) => U.value === w);
      const L = j.current.filter((U) => U.value !== w);
      j.current = L, n("valueChanged", { value: L.map((U) => U.value) });
    },
    [n]
  ), Q = ce(
    (w) => {
      w.stopPropagation(), n("valueChanged", { value: [] }), O();
    },
    [n, O]
  ), le = ce((w) => {
    D(w.target.value);
  }, []), te = ce(
    (w) => {
      if (!y) {
        if (w.key === "ArrowDown" || w.key === "ArrowUp" || w.key === "Enter" || w.key === " ") {
          if (w.target.tagName === "BUTTON") return;
          w.preventDefault(), w.stopPropagation(), z();
        }
        return;
      }
      switch (w.key) {
        case "ArrowDown":
          w.preventDefault(), w.stopPropagation(), _(
            (L) => L < k.length - 1 ? L + 1 : 0
          );
          break;
        case "ArrowUp":
          w.preventDefault(), w.stopPropagation(), _(
            (L) => L > 0 ? L - 1 : k.length - 1
          );
          break;
        case "Enter":
          w.preventDefault(), w.stopPropagation(), x >= 0 && x < k.length && G(k[x].value);
          break;
        case "Escape":
          w.preventDefault(), w.stopPropagation(), O();
          break;
        case "Tab":
          O();
          break;
        case "Backspace":
          C === "" && i && r.length > 0 && M(r[r.length - 1].value);
          break;
      }
    },
    [
      y,
      z,
      O,
      k,
      x,
      G,
      C,
      i,
      r,
      M
    ]
  ), ie = ce(
    async (w) => {
      w.preventDefault(), g(!1);
      try {
        await n("loadOptions");
      } catch {
        g(!0);
      }
    },
    [n]
  ), de = ce(
    (w, L) => {
      K(w), L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", String(w));
    },
    []
  ), me = ce(
    (w, L) => {
      if (L.preventDefault(), L.dataTransfer.dropEffect = "move", R === null || R === w) {
        T(null), ee(null);
        return;
      }
      const U = L.currentTarget.getBoundingClientRect(), J = U.left + U.width / 2, re = L.clientX < J ? "before" : "after";
      T(w), ee(re);
    },
    [R]
  ), he = ce(
    (w) => {
      if (w.preventDefault(), R === null || A === null || $ === null || R === A) return;
      const L = [...j.current], [U] = L.splice(R, 1);
      let J = A;
      R < A ? J = $ === "before" ? J - 1 : J : J = $ === "before" ? J : J + 1, L.splice(J, 0, U), j.current = L, n("valueChanged", { value: L.map((re) => re.value) }), K(null), T(null), ee(null);
    },
    [R, A, $, n]
  ), be = ce(() => {
    K(null), T(null), ee(null);
  }, []);
  if (we(() => {
    if (x < 0 || !B.current) return;
    const w = B.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    w && w.scrollIntoView({ block: "nearest" });
  }, [x, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((w) => /* @__PURE__ */ e.createElement("span", { key: w.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: w.image }), /* @__PURE__ */ e.createElement("span", null, w.label))));
  const N = !c && r.length > 0 && !u, I = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: W
    },
    (s || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: le,
        onKeyDown: te,
        placeholder: v["js.dropdownSelect.filterPlaceholder"],
        "aria-label": v["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": x >= 0 ? `${l}-opt-${x}` : void 0,
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
      !s && !b && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ie }, v["js.dropdownSelect.error"])),
      s && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      s && k.map((w, L) => /* @__PURE__ */ e.createElement(
        Dl,
        {
          key: w.value,
          id: `${l}-opt-${L}`,
          option: w,
          highlighted: L === x,
          searchTerm: C,
          onSelect: G,
          onMouseEnter: () => _(L)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: F,
      className: "tlDropdownSelect" + (y ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": y,
      "aria-haspopup": "listbox",
      "aria-owns": y ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: y ? void 0 : z,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((w, L) => {
      let U = "";
      return R === L ? U = "tlDropdownSelect__chip--dragging" : A === L && $ === "before" ? U = "tlDropdownSelect__chip--dropBefore" : A === L && $ === "after" && (U = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        xl,
        {
          key: w.value,
          option: w,
          removable: !u && (i || !c),
          onRemove: M,
          removeLabel: S(w.label),
          draggable: h,
          onDragStart: h ? (J) => de(L, J) : void 0,
          onDragOver: h ? (J) => me(L, J) : void 0,
          onDrop: h ? he : void 0,
          onDragEnd: h ? be : void 0,
          dragClassName: h ? U : void 0
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
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), I && wt.createPortal(I, document.body));
}, { useCallback: Ye, useRef: Ml } = e, yt = "application/x-tl-color", Pl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: a
}) => {
  const c = Ml(null), u = Ye(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ye((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), s = Ye(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(yt);
      h ? a(m, h) : c.current !== null && c.current !== m && i(c.current, m), c.current = null;
    },
    [i, a]
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
        onDrop: s(p)
      }
    ))
  );
};
function kt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Je(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function St(l) {
  if (!Je(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Nt(l, t, n) {
  const r = (i) => kt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function jl(l, t, n) {
  const r = l / 255, i = t / 255, a = n / 255, c = Math.max(r, i, a), u = Math.min(r, i, a), o = c - u;
  let s = 0;
  o !== 0 && (c === r ? s = (i - a) / o % 6 : c === i ? s = (a - r) / o + 2 : s = (r - i) / o + 4, s *= 60, s < 0 && (s += 360));
  const m = c === 0 ? 0 : o / c;
  return [s, m, c];
}
function Al(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), a = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + a) * 255),
    Math.round((u + a) * 255),
    Math.round((o + a) * 255)
  ];
}
function Bl(l) {
  return jl(...St(l));
}
function Ge(l, t, n) {
  return Nt(...Al(l, t, n));
}
const { useCallback: ye, useRef: ft } = e, Ol = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Bl(l), a = ft(null), c = ft(null), u = ye(
    (f, S) => {
      var D;
      const y = (D = a.current) == null ? void 0 : D.getBoundingClientRect();
      if (!y) return;
      const E = Math.max(0, Math.min(1, (f - y.left) / y.width)), C = Math.max(0, Math.min(1, 1 - (S - y.top) / y.height));
      t(Ge(n, E, C));
    },
    [n, t]
  ), o = ye(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), s = ye(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = ye(
    (f) => {
      var C;
      const S = (C = c.current) == null ? void 0 : C.getBoundingClientRect();
      if (!S) return;
      const E = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Ge(E, r, i));
    },
    [r, i, t]
  ), p = ye(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = ye(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), v = Ge(n, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: a,
      className: "tlColorInput__svField",
      style: { backgroundColor: v },
      onPointerDown: o,
      onPointerMove: s
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
function $l(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Fl = {
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
}, { useState: Me, useCallback: fe, useEffect: Xe, useRef: Hl, useLayoutEffect: Wl } = e, zl = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: r,
  defaultPalette: i,
  canReset: a,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: o
}) => {
  const [s, m] = Me("palette"), [p, h] = Me(t), v = Hl(null), f = ae(Fl), [S, y] = Me(null);
  Wl(() => {
    if (!l.current || !v.current) return;
    const B = l.current.getBoundingClientRect(), j = v.current.getBoundingClientRect();
    let Y = B.bottom + 4, d = B.left;
    Y + j.height > window.innerHeight && (Y = B.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, B.right - j.width)), y({ top: Y, left: d });
  }, [l]);
  const E = p != null, [C, D, x] = E ? St(p) : [0, 0, 0], [_, b] = Me((p == null ? void 0 : p.toUpperCase()) ?? "");
  Xe(() => {
    b((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Xe(() => {
    const B = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [u]), Xe(() => {
    const B = (Y) => {
      v.current && !v.current.contains(Y.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const g = fe(
    (B) => (j) => {
      const Y = parseInt(j.target.value, 10);
      if (isNaN(Y)) return;
      const d = kt(Y);
      h(Nt(B === "r" ? d : C, B === "g" ? d : D, B === "b" ? d : x));
    },
    [C, D, x]
  ), W = fe(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(yt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), P = fe((B) => {
    const j = B.target.value;
    b(j), Je(j) && h(j);
  }, []), R = fe(() => {
    h(null);
  }, []), K = fe((B) => {
    h(B);
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
      if ($l(n, B)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const Y = [...n];
      Y[j] = B.toUpperCase(), o(Y);
    },
    [n, o]
  ), Z = fe(() => {
    p != null && F(p), c(p);
  }, [p, c, F]);
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
        className: "tlColorInput__tab" + (s === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      f["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (s === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      f["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, s === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Pl,
      {
        colors: n,
        columns: r,
        onSelect: K,
        onConfirm: A,
        onSwap: T,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: ee }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Ol, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (E ? "" : " tlColorInput--noColor"),
        style: E ? { backgroundColor: p } : void 0,
        draggable: E,
        onDragStart: E ? W : void 0
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
        value: E ? x : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !Je(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: P
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, a && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, f["js.colorInput.ok"]))
  );
}, Ul = { "js.colorInput.chooseColor": "Choose color" }, { useState: Vl, useCallback: Pe, useRef: Kl } = e, Yl = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Ul), [i, a] = Vl(!1), c = Kl(null), u = t.value, o = t.editable !== !1, s = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? s, h = Pe(() => {
    o && a(!0);
  }, [o]), v = Pe(
    (y) => {
      a(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Pe(() => {
    a(!1);
  }, []), S = Pe(
    (y) => {
      n("paletteChanged", { palette: y });
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
    zl,
    {
      anchorRef: c,
      currentColor: u,
      palette: s,
      paletteColumns: m,
      defaultPalette: p,
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
}, { useState: Le, useCallback: ve, useEffect: je, useRef: ht, useLayoutEffect: Gl, useMemo: Xl } = e, ql = {
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
}, Zl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: a,
  onLoadIcons: c
}) => {
  const u = ae(ql), [o, s] = Le("simple"), [m, p] = Le(""), [h, v] = Le(t ?? ""), [f, S] = Le(!1), [y, E] = Le(null), C = ht(null), D = ht(null);
  Gl(() => {
    if (!l.current || !C.current) return;
    const A = l.current.getBoundingClientRect(), T = C.current.getBoundingClientRect();
    let $ = A.bottom + 4, ee = A.left;
    $ + T.height > window.innerHeight && ($ = A.top - T.height - 4), ee + T.width > window.innerWidth && (ee = Math.max(0, A.right - T.width)), E({ top: $, left: ee });
  }, [l]), je(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), je(() => {
    r && D.current && D.current.focus();
  }, [r]), je(() => {
    const A = (T) => {
      T.key === "Escape" && a();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [a]), je(() => {
    const A = ($) => {
      C.current && !C.current.contains($.target) && a();
    }, T = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(T), document.removeEventListener("mousedown", A);
    };
  }, [a]);
  const x = Xl(() => {
    if (!m) return n;
    const A = m.toLowerCase();
    return n.filter(
      (T) => T.prefix.toLowerCase().includes(A) || T.label.toLowerCase().includes(A) || T.terms != null && T.terms.some(($) => $.includes(A))
    );
  }, [n, m]), _ = ve((A) => {
    p(A.target.value);
  }, []), b = ve(
    (A) => {
      i(A);
    },
    [i]
  ), g = ve((A) => {
    v(A);
  }, []), W = ve((A) => {
    v(A.target.value);
  }, []), P = ve(() => {
    i(h || null);
  }, [h, i]), R = ve(() => {
    i(null);
  }, [i]), K = ve(async (A) => {
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
      ref: C,
      style: y ? { top: y.top, left: y.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => s("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (o === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => s("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
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
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      r && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && x.map(
        (A) => A.variants.map((T) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: T.encoded,
            className: "tlIconSelect__iconCell" + (T.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": T.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => o === "simple" ? b(T.encoded) : g(T.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), o === "simple" ? b(T.encoded) : g(T.encoded));
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
        onChange: W
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Se, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: a }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: P }, u["js.iconSelect.ok"]))
  );
}, Ql = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Jl, useCallback: Ae, useRef: er } = e, tr = ({ controlId: l, state: t }) => {
  const n = ne(), r = ae(Ql), [i, a] = Jl(!1), c = er(null), u = t.value, o = t.editable !== !1, s = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Ae(() => {
    o && !s && a(!0);
  }, [o, s]), v = Ae(
    (y) => {
      a(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Ae(() => {
    a(!1);
  }, []), S = Ae(async () => {
    await n("loadIcons");
  }, [n]);
  return o ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: s,
      title: u ?? "",
      "aria-label": r["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Zl,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: v,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Se, { encoded: u }) : null));
}, { useCallback: ke, useEffect: nr, useMemo: bt, useRef: lr, useState: qe } = e, rr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, or = [1, 2, 3, 4];
function ar(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function sr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of or)
    n >= i && (r = i);
  return r;
}
function cr(l, t) {
  const n = rr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function ir(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, h) => !!(r[p] && r[p][h]), a = (p, h) => {
    r[p] || (r[p] = {}), r[p][h] = !0;
  }, c = [];
  let u = 0, o = 0;
  const s = (p) => {
    let h = null;
    for (const f of c) f.rowStart === p && (h = f);
    if (!h) return;
    let v = h.colEnd;
    for (; v < n && !i(p, v); ) v++;
    if (v !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let S = h.colEnd; S < v; S++) a(f, S);
      h.colEnd = v;
    }
  };
  for (const p of l) {
    const h = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let v = Math.min(cr(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let D = o; D < n && !i(u, D); D++)
      f++;
    if (v > f) {
      for (s(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let D = o; D < n && !i(u, D); D++)
        f++;
      v = Math.min(v, f);
    }
    const S = o, y = o + v, E = u, C = u + h;
    c.push({ id: p.id, colStart: S, colEnd: y, rowStart: E, rowEnd: C });
    for (let D = E; D < C; D++)
      for (let x = S; x < y; x++) a(D, x);
    o = y, o >= n && (o = 0, u++);
  }
  s(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let h = 0; h < n; h++) {
      if (i(p, h)) continue;
      const v = c.find((f) => f.rowEnd === p && f.colStart <= h && h < f.colEnd);
      if (v) {
        v.rowEnd = p + 1;
        for (let f = v.colStart; f < v.colEnd; f++) a(p, f);
      }
    }
  return c;
}
const ur = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), a = lr(null), [c, u] = qe(1), o = t.editMode === !0;
  nr(() => {
    const b = a.current;
    if (!b) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, W = ar(r, g), P = () => u(sr(b.clientWidth, W));
    P();
    const R = new ResizeObserver(P);
    return R.observe(b), () => R.disconnect();
  }, [r]);
  const s = bt(() => ir(i, c), [i, c]), m = bt(() => {
    const b = {};
    for (const g of s) b[g.id] = g;
    return b;
  }, [s]), [p, h] = qe(null), [v, f] = qe(null), S = ke((b, g) => {
    if (!o) {
      b.preventDefault();
      return;
    }
    h(g), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", g);
  }, [o]), y = ke((b, g) => {
    if (!o || !p || p === g) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const W = b.currentTarget.getBoundingClientRect(), P = b.clientX < W.left + W.width / 2;
    f((R) => R && R.id === g && R.before === P ? R : { id: g, before: P });
  }, [o, p]), E = ke(() => {
  }, []), C = ke((b, g, W) => {
    const P = i.map((T) => T.id), R = P.indexOf(b);
    if (R < 0) return;
    P.splice(R, 1);
    const K = P.indexOf(g);
    if (K < 0) {
      P.splice(R, 0, b);
      return;
    }
    const A = W ? K : K + 1;
    P.splice(A, 0, b), n("reorder", { order: P });
  }, [i, n]), D = ke((b, g) => {
    if (!o || !p || p === g) return;
    b.preventDefault();
    const W = b.currentTarget.getBoundingClientRect(), P = b.clientX < W.left + W.width / 2;
    C(p, g, P), h(null), f(null);
  }, [o, p, C]), x = ke(() => {
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
      ref: a,
      className: "tlDashboard" + (o ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: _ }, i.map((b) => {
      const g = m[b.id];
      if (!g) return null;
      const W = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, P = ["tlDashboard__tile"];
      return p === b.id && P.push("tlDashboard__tile--dragging"), v && v.id === b.id && P.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: P.join(" "),
          style: W,
          draggable: o,
          onDragStart: (R) => S(R, b.id),
          onDragOver: (R) => y(R, b.id),
          onDragLeave: E,
          onDrop: (R) => D(R, b.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(V, { control: b.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: dr, useRef: _t, useState: vt, useEffect: Et, useLayoutEffect: mr } = e, pr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: n }))));
}, fr = ({ group: l }) => {
  var p, h;
  const [t, n] = vt(!1), [r, i] = vt({}), a = _t(null), c = _t(null), u = dr(() => {
    n((v) => !v);
  }, []);
  mr(() => {
    if (!t) return;
    const v = () => {
      const f = a.current;
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
      c.current && !c.current.contains(f.target) && a.current && !a.current.contains(f.target) && n(!1);
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
  if (o.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(V, { control: o[0] })));
  const s = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: a,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? s : void 0,
      title: m ? s : void 0
    },
    m ? /* @__PURE__ */ e.createElement(Se, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, s), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), wt.createPortal(
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
      o.map((v, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: v }))),
      (h = l.subGroups) == null ? void 0 : h.map((v, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, y) => /* @__PURE__ */ e.createElement("div", { key: y, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(V, { control: S })))))
    ),
    document.body
  ));
}, hr = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((a) => a != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, a) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, a > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(fr, { group: i }) : /* @__PURE__ */ e.createElement(pr, { group: i }))));
}, br = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(V, { control: t.frame }));
}, _r = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.content, i = t.showBack, a = t.barLabel;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, i && /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__bar" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlAdaptiveDetail__back",
      onClick: () => n("back", {}),
      "aria-label": "Back"
    },
    "‹"
  ), a && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__title" }, a)), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, r && /* @__PURE__ */ e.createElement(V, { control: r })));
}, vr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(V, { key: i, control: r })));
}, Er = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), gr = {
  "js.sidebar.openDrawer": "Open navigation"
}, Cr = ({ controlId: l }) => {
  const t = ne(), n = ae(gr);
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
H("TLButton", zt);
H("TLToggleButton", Vt);
H("TLTextInput", xt);
H("TLPasswordInput", It);
H("TLNumberInput", Pt);
H("TLDatePicker", At);
H("TLSelect", Ot);
H("TLCheckbox", Ft);
H("TLTable", Ht);
H("TLCounter", Kt);
H("TLTabBar", Gt);
H("TLFieldList", Xt);
H("TLAudioRecorder", Zt);
H("TLAudioPlayer", Jt);
H("TLFileUpload", tn);
H("TLDownload", ln);
H("TLPhotoCapture", on);
H("TLPhotoViewer", sn);
H("TLPdfViewer", un);
H("TLSplitPanel", dn);
H("TLPanel", vn);
H("TLInset", xn);
H("TLMaximizeRoot", En);
H("TLDeckPane", gn);
H("TLSidebar", Rn);
H("TLStack", Ln);
H("TLGrid", Dn);
H("TLCard", In);
H("TLAppBar", Mn);
H("TLBreadcrumb", jn);
H("TLBottomBar", Bn);
H("TLDialog", $n);
H("TLDialogManager", Wn);
H("TLWindow", Vn);
H("TLDrawer", Xn);
H("TLContextMenuRegion", Zn);
H("TLSnackbar", tl);
H("TLMenu", ll);
H("TLAppShell", ol);
H("TLText", al);
H("TLTableView", cl);
H("TLFormLayout", hl);
H("TLFormGroup", vl);
H("TLFormField", wl);
H("TLResourceCell", yl);
H("TLTreeView", Sl);
H("TLDropdownSelect", Il);
H("TLColorInput", Yl);
H("TLIconSelect", tr);
H("TLDashboard", ur);
H("TLToolbar", hr);
H("TLTileStack", br);
H("TLAdaptiveDetail", _r);
H("TLSlot", vr);
H("TLSlotContent", Er);
H("TLDrawerToggle", Cr);
