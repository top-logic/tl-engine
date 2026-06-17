import { React as e, useTLFieldValue as Ie, getComponent as Pt, useTLState as q, useTLCommand as ae, TLChild as z, useTLUpload as at, useI18N as ie, useTLDataUrl as st, register as W } from "tl-react-bridge";
const { useCallback: Bt } = e, At = ({ controlId: l, state: t }) => {
  const [n, r] = Ie(), i = Bt(
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
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: Ot } = e, $t = ({ controlId: l, state: t }) => {
  const [n, r] = Ie(), i = Ot(
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
}, { useCallback: Ft } = e, Ht = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Ie(), s = Ft(
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
}, { useCallback: Wt } = e, zt = ({ controlId: l, state: t }) => {
  const [n, r] = Ie(), i = Wt(
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
}, { useCallback: Ut } = e, Vt = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = Ie(), s = Ut(
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
}, { useCallback: Kt } = e, Yt = ({ controlId: l, state: t }) => {
  const [n, r] = Ie(), i = Kt(
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
}, Gt = ({ controlId: l, state: t }) => {
  const n = t.columns ?? [], r = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, r.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Pt(c.cellModule) : void 0, o = i[c.name];
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
function Le({ encoded: l, className: t }) {
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
const { useCallback: Xt } = e, qt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = ae(), o = t ?? "click", a = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", v = c.hidden === !0, f = c.tooltip, S = v ? { display: "none" } : void 0, g = c.appearance, C = c.size, y = c.navigateUrl, R = Xt(() => {
    if (y) {
      window.location.assign(y);
      return;
    }
    u(o);
  }, [u, o, y]), x = h === "icon-only", b = h === "icon-only" || h === "icon-label", _ = h === "label-only" || h === "icon-label" || x && !m, E = f ?? (x ? a : void 0), G = E ? `text:${E}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: R,
      disabled: p,
      style: S,
      className: "tlReactButton" + (x ? " tlReactButton--iconOnly" : "") + (g === "link" ? " tlReactButton--link" : "") + (C === "small" ? " tlReactButton--small" : "") + (C === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": G,
      "aria-label": x ? a : void 0
    },
    b && m && /* @__PURE__ */ e.createElement(Le, { encoded: m, className: "tlReactButton__image" }),
    _ && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Zt } = e, Qt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = ae(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, m = i ?? s.disabled === !0, p = Zt(() => {
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
}, Jt = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: en } = e, tn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.tabs ?? [], i = t.activeTabId, s = en((c) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, nn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(z, { control: i })))));
}, ln = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, rn = ({ controlId: l }) => {
  const t = q(), n = at(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : r !== "idle" ? r : m, v = e.useCallback(async () => {
    if (r === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (r !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = y, o.current = [];
        const R = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", x = new MediaRecorder(y, R ? { mimeType: R } : void 0);
        u.current = x, x.ondataavailable = (b) => {
          b.data.size > 0 && o.current.push(b.data);
        }, x.onstop = async () => {
          y.getTracks().forEach((E) => E.stop()), a.current = null;
          const b = new Blob(o.current, { type: x.mimeType || "audio/webm" });
          if (o.current = [], b.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const _ = new FormData();
          _.append("audio", b, "recording.webm"), await n(_), i("idle");
        }, x.start(), i("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [r, n]), f = ie(ln), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], g = h === "uploading", C = ["tlAudioRecorder__button"];
  return h === "recording" && C.push("tlAudioRecorder__button--recording"), h === "uploading" && C.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: v,
      disabled: g,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, on = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, an = ({ controlId: l }) => {
  const t = q(), n = st(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
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
        const g = await fetch(n);
        if (!g.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", g.status), c("idle");
          return;
        }
        const C = await g.blob();
        o.current = URL.createObjectURL(C);
      } catch (g) {
        console.error("[TLAudioPlayer] Fetch error:", g), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = ie(on), h = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
  return s === "playing" && f.push("tlAudioPlayer__button--playing"), s === "loading" && f.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: m,
      disabled: v,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${s === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, sn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, cn = ({ controlId: l }) => {
  const t = q(), n = at(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : r !== "idle" ? r : o, h = e.useCallback(async (b) => {
    i("uploading");
    const _ = new FormData();
    _.append("file", b, b.name), await n(_), i("idle");
  }, [n]), v = e.useCallback((b) => {
    var E;
    const _ = (E = b.target.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [h]), f = e.useCallback(() => {
    var b;
    r !== "uploading" && ((b = u.current) == null || b.click());
  }, [r]), S = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!0);
  }, []), g = e.useCallback((b) => {
    b.preventDefault(), b.stopPropagation(), c(!1);
  }, []), C = e.useCallback((b) => {
    var E;
    if (b.preventDefault(), b.stopPropagation(), c(!1), r === "uploading") return;
    const _ = (E = b.dataTransfer.files) == null ? void 0 : E[0];
    _ && h(_);
  }, [r, h]), y = p === "uploading", R = ie(sn), x = p === "uploading" ? R["js.uploading"] : R["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: g,
      onDrop: C
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
        disabled: y,
        title: x,
        "aria-label": x
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, un = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, dn = ({ controlId: l }) => {
  const t = q(), n = st(), r = ae(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const g = await S.blob(), C = URL.createObjectURL(g), y = document.createElement("a");
        y.href = C, y.download = c, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(C);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = ie(un);
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
}, mn = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, pn = ({ controlId: l }) => {
  const t = q(), n = at(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), v = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), g = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), C = e.useCallback(() => {
    g(), i("idle");
  }, [g]), y = e.useCallback(async () => {
    var T;
    if (r !== "uploading") {
      if (c(null), !S) {
        (T = h.current) == null || T.click();
        return;
      }
      try {
        const U = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = U, i("overlayOpen");
      } catch (U) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", U), c("js.photoCapture.error.denied"), i("idle");
      }
    }
  }, [r, S]), R = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const T = a.current, U = p.current;
    if (!T || !U)
      return;
    U.width = T.videoWidth, U.height = T.videoHeight;
    const P = U.getContext("2d");
    P && (P.drawImage(T, 0, 0), g(), i("uploading"), U.toBlob(async (N) => {
      if (!N) {
        i("idle");
        return;
      }
      const A = new FormData();
      A.append("photo", N, "capture.jpg"), await n(A), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, g]), x = e.useCallback(async (T) => {
    var N;
    const U = (N = T.target.files) == null ? void 0 : N[0];
    if (!U) return;
    i("uploading");
    const P = new FormData();
    P.append("photo", U, U.name), await n(P), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [r]), e.useEffect(() => {
    var U;
    if (r !== "overlayOpen") return;
    (U = v.current) == null || U.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [r]), e.useEffect(() => {
    if (r !== "overlayOpen") return;
    const T = (U) => {
      U.key === "Escape" && C();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [r, C]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const b = ie(mn), _ = r === "uploading" ? b["js.uploading"] : b["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const G = ["tlPhotoCapture__overlayVideo"];
  u && G.push("tlPhotoCapture__overlayVideo--mirrored");
  const I = ["tlPhotoCapture__mirrorBtn"];
  return u && I.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: y,
      disabled: r === "uploading",
      title: _,
      "aria-label": _
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
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: C }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: G.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: I.join(" "),
        onClick: () => o((T) => !T),
        title: b["js.photoCapture.mirror"],
        "aria-label": b["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: R,
        title: b["js.photoCapture.capture"],
        "aria-label": b["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: C,
        title: b["js.photoCapture.close"],
        "aria-label": b["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b[s]), f && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f));
}, fn = {
  "js.photoViewer.alt": "Captured photo"
}, hn = ({ controlId: l }) => {
  const t = q(), n = st(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
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
  const o = ie(fn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, { useCallback: mt, useRef: Ye } = e, bn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((g) => g.collapsed), o = !u && s.some((g) => g.collapsed), a = u ? !c : c, m = Ye(null), p = Ye(null), h = Ye(null), v = mt((g, C) => {
    const y = {
      overflow: g.scrolling || "auto"
    };
    return g.collapsed ? u && !a ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : C !== void 0 ? y.flex = `0 0 ${C}px` : y.flex = `${g.size} 1 0%`, g.minSize > 0 && !g.collapsed && (y.minWidth = c ? g.minSize : void 0, y.minHeight = c ? void 0 : g.minSize), y;
  }, [c, u, o, a]), f = mt((g, C) => {
    g.preventDefault();
    const y = m.current;
    if (!y) return;
    const R = s[C], x = s[C + 1], b = y.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    b.forEach((I) => {
      _.push(c ? I.offsetWidth : I.offsetHeight);
    }), h.current = _, p.current = {
      splitterIndex: C,
      startPos: c ? g.clientX : g.clientY,
      startSizeBefore: _[C],
      startSizeAfter: _[C + 1],
      childBefore: R,
      childAfter: x
    };
    const E = (I) => {
      const T = p.current;
      if (!T || !h.current) return;
      const P = (c ? I.clientX : I.clientY) - T.startPos, N = T.childBefore.minSize || 0, A = T.childAfter.minSize || 0;
      let J = T.startSizeBefore + P, $ = T.startSizeAfter - P;
      J < N && ($ += J - N, J = N), $ < A && (J += $ - A, $ = A), h.current[T.splitterIndex] = J, h.current[T.splitterIndex + 1] = $;
      const Z = y.querySelectorAll(":scope > .tlSplitPanel__child"), B = Z[T.splitterIndex], j = Z[T.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${J}px`), j && (j.style.flex = `0 0 ${$}px`);
    }, G = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", G), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const I = {};
        s.forEach((T, U) => {
          const P = T.control;
          P != null && P.controlId && h.current && (I[P.controlId] = h.current[U]);
        }), n("updateSizes", { sizes: I });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", G), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((g, C) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${C}`,
          className: `tlSplitPanel__child${g.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(g)
        },
        /* @__PURE__ */ e.createElement(z, { control: g.control })
      )
    ), i && C < s.length - 1) {
      const y = s[C + 1];
      !g.collapsed && !y.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${C}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (x) => f(x, C)
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
}, { useCallback: Ge } = e, _n = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, vn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), En = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), gn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), Cn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), yn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), wn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(_n), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", h = s === "HIDDEN", v = Ge(() => {
    n("toggleMinimize");
  }, [n]), f = Ge(() => {
    n("toggleMaximize");
  }, [n]), S = Ge(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const g = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: g
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(z, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(En, null) : /* @__PURE__ */ e.createElement(vn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(Cn, null) : /* @__PURE__ */ e.createElement(gn, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(yn, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(z, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(z, { control: t.buttonBar }))
  );
}, kn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(z, { control: t.child })
  );
}, Sn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(z, { control: t.activeChild }));
}, { useCallback: fe, useState: Ue, useEffect: Ve, useRef: Ke } = e, Nn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function lt(l, t, n, r) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...lt(s.children, t, n, s.id)));
  return i;
}
const De = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, Tn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => r(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(De, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(De, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Rn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: r,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(De, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), xn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(De, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Ln = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Dn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Ke(null);
  Ve(() => {
    const a = (m) => {
      c.current && !c.current.contains(m.target) && setTimeout(() => s(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [s]), Ve(() => {
    const a = (m) => {
      m.key === "Escape" && s();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [s]);
  const u = fe((a) => {
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
        /* @__PURE__ */ e.createElement(De, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, In = ({
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
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const g = Ke(null), [C, y] = Ue(null), R = fe(() => {
    r ? v === l.id ? S() : (g.current && y(g.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, v, l.id, c, f, S]), x = fe((_) => {
    g.current = _, o(_);
  }, [o]), b = r && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (b ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: R,
      title: r ? l.label : void 0,
      "aria-expanded": r ? b : t,
      tabIndex: u,
      ref: x,
      onFocus: () => a(l.id)
    },
    /* @__PURE__ */ e.createElement(De, { icon: l.icon }),
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
  ), b && /* @__PURE__ */ e.createElement(
    Dn,
    {
      item: l,
      activeItemId: n,
      anchorRect: C,
      onSelect: i,
      onExecute: s,
      onClose: S
    }
  ), t && !r && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    Tt,
    {
      key: _.id,
      item: _,
      activeItemId: n,
      collapsed: r,
      onSelect: i,
      onExecute: s,
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
}, Tt = ({
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
        Tn,
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
        Rn,
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
      return /* @__PURE__ */ e.createElement(xn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Ln, null);
    case "group": {
      const v = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        In,
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
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, Mn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(Nn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, m] = Ue(() => {
    const N = /* @__PURE__ */ new Map(), A = (J) => {
      for (const $ of J)
        $.type === "group" && (N.set($.id, $.expanded), A($.children));
    };
    return A(i), N;
  }), p = fe((N) => {
    m((A) => {
      const J = new Map(A), $ = J.get(N) ?? !1;
      return J.set(N, !$), n("toggleGroup", { itemId: N, expanded: !$ }), J;
    });
  }, [n]), h = fe((N) => {
    N !== s && n("selectItem", { itemId: N });
  }, [n, s]), v = fe((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), f = fe(() => {
    n("toggleCollapse", {});
  }, [n]), S = fe(() => {
    n("toggleDrawer", {});
  }, [n]), [g, C] = Ue(null), y = fe((N) => {
    C(N);
  }, []), R = fe(() => {
    C(null);
  }, []);
  Ve(() => {
    o || C(null);
  }, [o]);
  const [x, b] = Ue(() => {
    const N = lt(i, o, a);
    return N.length > 0 ? N[0].id : "";
  }), _ = Ke(/* @__PURE__ */ new Map()), E = fe((N) => (A) => {
    A ? _.current.set(N, A) : _.current.delete(N);
  }, []), G = fe((N) => {
    b(N);
  }, []), I = Ke(0), T = fe((N) => {
    b(N), I.current++;
  }, []);
  Ve(() => {
    const N = _.current.get(x);
    N && document.activeElement !== N && N.focus();
  }, [x, I.current]);
  const U = fe((N) => {
    if (N.key === "Escape" && g !== null) {
      N.preventDefault(), R();
      return;
    }
    const A = lt(i, o, a);
    if (A.length === 0) return;
    const J = A.findIndex((Z) => Z.id === x);
    if (J < 0) return;
    const $ = A[J];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const Z = (J + 1) % A.length;
        T(A[Z].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const Z = (J - 1 + A.length) % A.length;
        T(A[Z].id);
        break;
      }
      case "Home": {
        N.preventDefault(), T(A[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), T(A[A.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), $.type === "nav" ? h($.id) : $.type === "command" ? v($.id) : $.type === "group" && (o ? g === $.id ? R() : y($.id) : p($.id));
        break;
      }
      case "ArrowRight": {
        $.type === "group" && !o && ((a.get($.id) ?? !1) || (N.preventDefault(), p($.id)));
        break;
      }
      case "ArrowLeft": {
        $.type === "group" && !o && (a.get($.id) ?? !1) && (N.preventDefault(), p($.id));
        break;
      }
    }
  }, [
    i,
    o,
    a,
    x,
    g,
    T,
    h,
    v,
    p,
    y,
    R
  ]), P = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: P }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(z, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: U }, i.map((N) => /* @__PURE__ */ e.createElement(
    Tt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: o,
      onSelect: h,
      onExecute: v,
      onToggleGroup: p,
      focusedId: x,
      setItemRef: E,
      onItemFocus: G,
      groupStates: a,
      flyoutGroupId: g,
      onOpenFlyout: y,
      onCloseFlyout: R
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(z, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(z, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(z, { control: t.activeContent })));
}, jn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], o = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: o }, u.map((a, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: a })));
}, Pn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(z, { control: t.child }));
}, Bn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(z, { key: o, control: u })));
}, An = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(z, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(z, { control: c })));
}, On = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(z, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, m) => /* @__PURE__ */ e.createElement(z, { key: m, control: a }))));
}, { useCallback: $n } = e, Fn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.items ?? [], i = $n((s) => {
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
}, { useCallback: Hn } = e, Wn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.items ?? [], i = t.activeItemId, s = Hn((c) => {
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
}, { useCallback: pt, useEffect: ft, useRef: zn } = e, Un = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = zn(null), u = pt(() => {
    n("close");
  }, [n]), o = pt((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return ft(() => {
    if (!r) return;
    const a = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [r, u]), ft(() => {
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
    /* @__PURE__ */ e.createElement(z, { control: s })
  ) : null;
}, { useEffect: Vn, useRef: Kn } = e, Yn = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = Kn(n.length);
  return Vn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(z, { key: i.controlId, control: i })));
}, { useCallback: Oe, useRef: Ne, useState: $e } = e, Gn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Xn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], qn = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(Gn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [v, f] = $e(null), [S, g] = $e(null), [C, y] = $e(null), R = Ne(null), [x, b] = $e(!1), _ = Ne(null), E = Ne(null), G = Ne(null), I = Ne(null), T = Ne(null), U = Oe(() => {
    n("close");
  }, [n]), P = Oe((Z, B) => {
    B.preventDefault();
    const j = I.current;
    if (!j) return;
    const K = j.getBoundingClientRect(), d = !R.current, k = R.current ?? { x: K.left, y: K.top };
    d && (R.current = k, y(k)), T.current = {
      dir: Z,
      startX: B.clientX,
      startY: B.clientY,
      startW: K.width,
      startH: K.height,
      startPos: { ...k },
      symmetric: d
    };
    const F = (V) => {
      const M = T.current;
      if (!M) return;
      const Q = V.clientX - M.startX, le = V.clientY - M.startY;
      let ee = M.startW, me = M.startH, he = 0, be = 0;
      M.symmetric ? (M.dir.includes("e") && (ee = M.startW + 2 * Q), M.dir.includes("w") && (ee = M.startW - 2 * Q), M.dir.includes("s") && (me = M.startH + 2 * le), M.dir.includes("n") && (me = M.startH - 2 * le)) : (M.dir.includes("e") && (ee = M.startW + Q), M.dir.includes("w") && (ee = M.startW - Q, he = Q), M.dir.includes("s") && (me = M.startH + le), M.dir.includes("n") && (me = M.startH - le, be = le));
      const Ee = Math.max(200, ee), ge = Math.max(100, me);
      M.symmetric ? (he = (M.startW - Ee) / 2, be = (M.startH - ge) / 2) : (M.dir.includes("w") && Ee === 200 && (he = M.startW - 200), M.dir.includes("n") && ge === 100 && (be = M.startH - 100)), E.current = Ee, G.current = ge, f(Ee), g(ge);
      const we = {
        x: M.startPos.x + he,
        y: M.startPos.y + be
      };
      R.current = we, y(we);
    }, O = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", O);
      const V = E.current, M = G.current;
      (V != null || M != null) && n("resize", {
        ...V != null ? { width: Math.round(V) + "px" } : {},
        ...M != null ? { height: Math.round(M) + "px" } : {}
      }), T.current = null;
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", O);
  }, [n]), N = Oe((Z) => {
    if (Z.button !== 0 || Z.target.closest("button")) return;
    Z.preventDefault();
    const B = I.current;
    if (!B) return;
    const j = B.getBoundingClientRect(), K = R.current ?? { x: j.left, y: j.top }, d = Z.clientX - K.x, k = Z.clientY - K.y, F = (V) => {
      const M = window.innerWidth, Q = window.innerHeight;
      let le = V.clientX - d, ee = V.clientY - k;
      const me = B.offsetWidth, he = B.offsetHeight;
      le + me > M && (le = M - me), ee + he > Q && (ee = Q - he), le < 0 && (le = 0), ee < 0 && (ee = 0);
      const be = { x: le, y: ee };
      R.current = be, y(be);
    }, O = () => {
      document.removeEventListener("mousemove", F), document.removeEventListener("mouseup", O);
    };
    document.addEventListener("mousemove", F), document.addEventListener("mouseup", O);
  }, []), A = Oe(() => {
    var Z, B;
    if (x) {
      const j = _.current;
      j && (y(j.x !== -1 ? { x: j.x, y: j.y } : null), f(j.w), g(j.h)), b(!1);
    } else {
      const j = I.current, K = j == null ? void 0 : j.getBoundingClientRect();
      _.current = {
        x: ((Z = R.current) == null ? void 0 : Z.x) ?? (K == null ? void 0 : K.left) ?? -1,
        y: ((B = R.current) == null ? void 0 : B.y) ?? (K == null ? void 0 : K.top) ?? -1,
        w: v ?? (K == null ? void 0 : K.width) ?? null,
        h: S ?? null
      }, b(!0), y({ x: 0, y: 0 }), f(null), g(null);
    }
  }, [x, v, S]), J = x ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : s,
    ...S != null ? { height: S + "px" } : c != null ? { height: c } : {},
    ...u != null && S == null ? { minHeight: u } : {},
    maxHeight: C ? "100vh" : "80vh",
    ...C ? { position: "absolute", left: C.x + "px", top: C.y + "px" } : {}
  }, $ = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: J,
      ref: I,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": $
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${x ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: x ? void 0 : N,
        onDoubleClick: o ? A : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: $ }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(z, { control: p })),
      o && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: A,
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
          onClick: U,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(z, { control: a })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(z, { control: h }), m.map((Z, B) => /* @__PURE__ */ e.createElement(z, { key: B, control: Z }))),
    o && !x && Xn.map((Z) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: Z,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${Z}`,
        onMouseDown: (B) => P(Z, B)
      }
    ))
  );
}, { useCallback: Zn, useEffect: Qn } = e, Jn = {
  "js.drawer.close": "Close"
}, el = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(Jn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Zn(() => {
    n("close");
  }, [n]);
  Qn(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(z, { control: o })));
}, { useCallback: tl } = e, nl = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.child, i = tl((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(z, { control: r }));
}, { useCallback: ll, useEffect: rl, useState: ol } = e, al = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, m] = ol(!1), p = ll(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return rl(() => {
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
}, { useCallback: Xe, useEffect: qe, useRef: sl, useState: ht } = e, cl = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = sl(null), [a, m] = ht({ top: 0, left: 0 }), [p, h] = ht(0), v = u.filter((C) => C.type === "item" && !C.disabled);
  qe(() => {
    var E, G;
    if (!r) return;
    const C = ((E = o.current) == null ? void 0 : E.offsetHeight) ?? 200, y = ((G = o.current) == null ? void 0 : G.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let I = c, T = s;
      I + C > window.innerHeight && (I = Math.max(0, window.innerHeight - C)), T + y > window.innerWidth && (T = Math.max(0, window.innerWidth - y)), m({ top: I, left: T }), h(0);
      return;
    }
    if (!i) return;
    const R = document.getElementById(i);
    if (!R) return;
    const x = R.getBoundingClientRect();
    let b = x.bottom + 4, _ = x.left;
    b + C > window.innerHeight && (b = x.top - C - 4), _ + y > window.innerWidth && (_ = x.right - y), m({ top: b, left: _ }), h(0);
  }, [r, i, s, c]);
  const f = Xe(() => {
    n("close");
  }, [n]), S = Xe((C) => {
    n("selectItem", { itemId: C });
  }, [n]);
  qe(() => {
    if (!r) return;
    const C = (y) => {
      o.current && !o.current.contains(y.target) && f();
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [r, f]);
  const g = Xe((C) => {
    if (C.key === "Escape") {
      f();
      return;
    }
    if (C.key === "ArrowDown")
      C.preventDefault(), h((y) => (y + 1) % v.length);
    else if (C.key === "ArrowUp")
      C.preventDefault(), h((y) => (y - 1 + v.length) % v.length);
    else if (C.key === "Enter" || C.key === " ") {
      C.preventDefault();
      const y = v[p];
      y && S(y.id);
    }
  }, [f, S, v, p]);
  return qe(() => {
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
      onKeyDown: g
    },
    u.map((C, y) => {
      if (C.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: y, className: "tlMenu__separator" });
      const x = v.indexOf(C) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: C.id,
          type: "button",
          className: "tlMenu__item" + (x ? " tlMenu__item--focused" : "") + (C.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: C.disabled,
          tabIndex: x ? 0 : -1,
          onClick: () => S(C.id)
        },
        C.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + C.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, C.label)
      );
    })
  ) : null;
}, il = ({ controlId: l }) => {
  const t = q(), n = t.header, r = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(z, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(z, { control: r })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(z, { control: i })), /* @__PURE__ */ e.createElement(z, { control: s }), c && /* @__PURE__ */ e.createElement(z, { control: c }), u && /* @__PURE__ */ e.createElement(z, { control: u }));
}, ul = ({ controlId: l }) => {
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
}, dl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all",
  "js.table.filter": "Filter",
  "js.table.apply": "Apply",
  "js.table.clear": "Clear",
  "js.table.cancel": "Cancel"
}, bt = 50, ml = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(dl), i = e.useRef(null);
  e.useEffect(() => {
    const w = i.current;
    if (!w) return;
    const L = (Y) => {
      const ne = Y.detail;
      let oe = ne.target;
      for (; oe && oe !== w; ) {
        const pe = oe.dataset.row, se = oe.dataset.col;
        if (pe != null && se != null) {
          ne.resolved = { key: pe + "|" + se };
          return;
        }
        oe = oe.parentElement;
      }
    };
    return w.addEventListener("tl-tooltip-resolve", L), () => w.removeEventListener("tl-tooltip-resolve", L);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, v = t.filterPopup ?? null, f = e.useMemo(
    () => s.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [s]
  ), S = a === "multi", g = 40, C = 20, y = e.useRef(null), R = e.useRef(null), x = e.useRef(null), [b, _] = e.useState({}), E = e.useRef(null), G = e.useRef(!1), I = e.useRef(null), [T, U] = e.useState(null), [P, N] = e.useState(null), [A, J] = e.useState(null);
  e.useEffect(() => {
    E.current || _({});
  }, [s]);
  const $ = e.useCallback((w) => b[w.name] ?? w.width, [b]), Z = e.useMemo(() => {
    const w = [];
    let L = S && p > 0 ? g : 0;
    for (let Y = 0; Y < p && Y < s.length; Y++)
      w.push(L), L += $(s[Y]);
    return w;
  }, [s, p, S, g, $]), B = c * o, j = e.useRef(null), K = e.useCallback((w, L, Y) => {
    Y.preventDefault(), Y.stopPropagation(), E.current = { column: w, startX: Y.clientX, startWidth: L };
    let ne = Y.clientX, oe = 0;
    const pe = () => {
      const ce = E.current;
      if (!ce) return;
      const _e = Math.max(bt, ce.startWidth + (ne - ce.startX) + oe);
      _((Se) => ({ ...Se, [ce.column]: _e }));
    }, se = () => {
      const ce = R.current, _e = y.current;
      if (!ce || !E.current) return;
      const Se = ce.getBoundingClientRect(), it = 40, ut = 8, jt = ce.scrollLeft;
      ne > Se.right - it ? ce.scrollLeft += ut : ne < Se.left + it && (ce.scrollLeft = Math.max(0, ce.scrollLeft - ut));
      const dt = ce.scrollLeft - jt;
      dt !== 0 && (_e && (_e.scrollLeft = ce.scrollLeft), oe += dt, pe()), j.current = requestAnimationFrame(se);
    };
    j.current = requestAnimationFrame(se);
    const ke = (ce) => {
      ne = ce.clientX, pe();
    }, Ae = (ce) => {
      document.removeEventListener("mousemove", ke), document.removeEventListener("mouseup", Ae), j.current !== null && (cancelAnimationFrame(j.current), j.current = null);
      const _e = E.current;
      if (_e) {
        const Se = Math.max(bt, _e.startWidth + (ce.clientX - _e.startX) + oe);
        n("columnResize", { column: _e.column, width: Se }), E.current = null, G.current = !0, requestAnimationFrame(() => {
          G.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ke), document.addEventListener("mouseup", Ae);
  }, [n]), d = e.useCallback(() => {
    y.current && R.current && (y.current.scrollLeft = R.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const w = R.current;
      if (!w) return;
      const L = w.scrollTop, Y = Math.ceil(w.clientHeight / o), ne = Math.floor(L / o);
      n("scroll", { start: ne, count: Y });
    }, 80);
  }, [n, o]), k = e.useCallback((w, L, Y) => {
    if (G.current) return;
    let ne;
    !L || L === "desc" ? ne = "asc" : ne = "desc";
    const oe = Y.shiftKey ? "add" : "replace";
    n("sort", { column: w, direction: ne, mode: oe });
  }, [n]), F = e.useCallback((w, L) => {
    I.current = w, L.dataTransfer.effectAllowed = "move", L.dataTransfer.setData("text/plain", w);
  }, []), O = e.useCallback((w, L) => {
    if (!I.current || I.current === w) {
      U(null);
      return;
    }
    L.preventDefault(), L.dataTransfer.dropEffect = "move";
    const Y = L.currentTarget.getBoundingClientRect(), ne = L.clientX < Y.left + Y.width / 2 ? "left" : "right";
    U({ column: w, side: ne });
  }, []), V = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const L = I.current;
    if (!L || !T) {
      I.current = null, U(null);
      return;
    }
    let Y = s.findIndex((oe) => oe.name === T.column);
    if (Y < 0) {
      I.current = null, U(null);
      return;
    }
    const ne = s.findIndex((oe) => oe.name === L);
    T.side === "right" && Y++, ne < Y && Y--, n("columnReorder", { column: L, targetIndex: Y }), I.current = null, U(null);
  }, [s, T, n]), M = e.useCallback(() => {
    I.current = null, U(null);
  }, []), Q = e.useCallback((w, L) => {
    L.shiftKey && L.preventDefault(), n("select", {
      rowIndex: w,
      ctrlKey: L.ctrlKey || L.metaKey,
      shiftKey: L.shiftKey
    });
  }, [n]), le = e.useCallback((w, L) => {
    L.stopPropagation(), n("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [n]), ee = e.useCallback(() => {
    const w = m === c && c > 0;
    n("selectAll", { selected: !w });
  }, [n, m, c]), me = e.useCallback((w, L, Y) => {
    Y.stopPropagation(), n("expand", { rowIndex: w, expanded: L });
  }, [n]), he = e.useCallback((w, L) => {
    L.preventDefault(), N({ x: L.clientX, y: L.clientY, colIdx: w });
  }, []), be = e.useCallback(() => {
    P && (n("setFrozenColumnCount", { count: P.colIdx + 1 }), N(null));
  }, [P, n]), Ee = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), N(null);
  }, [n]);
  e.useEffect(() => {
    if (!P) return;
    const w = () => N(null), L = (Y) => {
      Y.key === "Escape" && N(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", L), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", L);
    };
  }, [P]);
  const ge = e.useCallback((w, L) => {
    L.stopPropagation(), L.preventDefault();
    const Y = L.currentTarget.getBoundingClientRect();
    J({ x: Y.left, y: Y.bottom }), n("openFilter", { column: w });
  }, [n]), we = e.useCallback(() => {
    n("applyFilter", {}), J(null);
  }, [n]), Be = e.useCallback((w) => {
    n("clearFilter", { column: w }), J(null);
  }, [n]), D = e.useCallback(() => {
    n("closeFilter", {}), J(null);
  }, [n]);
  e.useEffect(() => {
    if (!A) return;
    const w = () => D(), L = (Y) => {
      Y.key === "Escape" && D();
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", L), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", L);
    };
  }, [A, D]);
  const H = s.reduce((w, L) => w + $(L), 0) + (S ? g : 0), te = m === c && c > 0, re = m > 0 && m < c, Me = e.useCallback((w) => {
    w && (w.indeterminate = re);
  }, [re]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (w) => {
        if (!I.current) return;
        w.preventDefault();
        const L = R.current, Y = y.current;
        if (!L) return;
        const ne = L.getBoundingClientRect(), oe = 40, pe = 8;
        w.clientX < ne.left + oe ? L.scrollLeft = Math.max(0, L.scrollLeft - pe) : w.clientX > ne.right - oe && (L.scrollLeft += pe), Y && (Y.scrollLeft = L.scrollLeft);
      },
      onDrop: V
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: y }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: H } }, S && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: g,
          minWidth: g,
          ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          I.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== I.current && U({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Me,
          className: "tlTableView__checkbox",
          checked: te,
          onChange: ee
        }
      )
    ), s.map((w, L) => {
      const Y = $(w);
      s.length - 1;
      let ne = "tlTableView__headerCell";
      w.sortable && (ne += " tlTableView__headerCell--sortable"), T && T.column === w.name && (ne += " tlTableView__headerCell--dragOver-" + T.side);
      const oe = L < p, pe = L === p - 1;
      return oe && (ne += " tlTableView__headerCell--frozen"), pe && (ne += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: ne,
          style: {
            width: Y,
            minWidth: Y,
            position: oe ? "sticky" : "relative",
            ...oe ? { left: Z[L], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (se) => k(w.name, w.sortDirection, se) : void 0,
          onContextMenu: (se) => he(L, se),
          onDragStart: (se) => F(w.name, se),
          onDragOver: (se) => O(w.name, se),
          onDrop: V,
          onDragEnd: M
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.filterable && /* @__PURE__ */ e.createElement(
          "button",
          {
            type: "button",
            className: "tlTableView__filterButton" + (w.filterActive ? " tlTableView__filterButton--active" : ""),
            title: r["js.table.filter"],
            style: {
              border: "none",
              background: "transparent",
              cursor: "pointer",
              padding: "0 4px",
              color: w.filterActive ? "#1565c0" : "inherit"
            },
            onMouseDown: (se) => se.stopPropagation(),
            onClick: (se) => ge(w.name, se)
          },
          /* @__PURE__ */ e.createElement("i", { className: w.filterActive ? "bi bi-funnel-fill" : "bi bi-funnel" })
        ),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", f > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (se) => K(w.name, Y, se)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (I.current && s.length > 0) {
            const L = s[s.length - 1];
            L.name !== I.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", U({ column: L.name, side: "right" }));
          }
        },
        onDrop: V
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: R,
        className: "tlTableView__body",
        onScroll: d
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: B, position: "relative", width: H } }, u.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            width: H
          },
          onClick: (L) => Q(w.index, L)
        },
        S && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (p > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: g,
              minWidth: g,
              ...p > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (L) => L.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (L) => le(w.index, L),
              tabIndex: -1
            }
          )
        ),
        s.map((L, Y) => {
          const ne = $(L), oe = Y === s.length - 1, pe = Y < p, se = Y === p - 1;
          let ke = "tlTableView__cell";
          pe && (ke += " tlTableView__cell--frozen"), se && (ke += " tlTableView__cell--frozenLast");
          const Ae = h && Y === 0, ce = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: L.name,
              className: ke,
              "data-row": w.id,
              "data-col": L.name,
              style: {
                ...oe && !pe ? { flex: "1 0 auto", minWidth: ne } : { width: ne, minWidth: ne },
                ...pe ? { position: "sticky", left: Z[Y], zIndex: 2 } : {}
              }
            },
            Ae ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ce * C } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (_e) => me(w.index, !w.expanded, _e)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(z, { control: w.cells[L.name] })) : /* @__PURE__ */ e.createElement(z, { control: w.cells[L.name] })
          );
        })
      )))
    ),
    P && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: P.y, left: P.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      P.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: be }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Ee }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    ),
    v && A && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__filterPopup",
        style: {
          position: "fixed",
          top: A.y,
          left: A.x,
          zIndex: 1e4,
          background: "#fff",
          border: "1px solid #ccc",
          borderRadius: 4,
          padding: 10,
          boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
          minWidth: 220
        },
        onMouseDown: (w) => w.stopPropagation()
      },
      v.fields.map((w, L) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: L,
          className: "tlTableView__filterField",
          style: { display: "flex", alignItems: "center", gap: 8, marginBottom: 6 }
        },
        /* @__PURE__ */ e.createElement("label", { className: "tlTableView__filterLabel", style: { flex: "0 0 90px", fontSize: "0.85em" } }, w.label),
        /* @__PURE__ */ e.createElement("div", { style: { flex: "1 1 auto" } }, /* @__PURE__ */ e.createElement(z, { control: w.control }))
      )),
      /* @__PURE__ */ e.createElement(
        "div",
        {
          className: "tlTableView__filterActions",
          style: { display: "flex", justifyContent: "flex-end", gap: 6, marginTop: 8 }
        },
        /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlButton", onClick: () => Be(v.column) }, r["js.table.clear"]),
        /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlButton", onClick: D }, r["js.table.cancel"]),
        /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlButton tlButton--primary", onClick: we }, r["js.table.apply"])
      )
    )
  );
}, pl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Rt = e.createContext(pl), { useMemo: fl, useRef: hl, useState: bl, useEffect: _l } = e, vl = 320, El = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = hl(null), [o, a] = bl(
    r === "top" ? "top" : "side"
  );
  _l(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((g) => {
      for (const C of g) {
        const R = C.contentRect.width / n;
        a(R < vl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const m = fl(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Rt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: f }))));
}, { useCallback: gl } = e, Cl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, yl = ({ controlId: l }) => {
  const t = q(), n = ae(), r = ie(Cl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, h = gl(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${o}`,
    a ? "tlFormGroup--fullLine" : "",
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(z, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(z, { key: S, control: f }))));
}, { useContext: wl, useState: kl, useCallback: Sl } = e, Nl = ({ controlId: l }) => {
  const t = q(), n = wl(Rt), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, v = t.field, f = n.readOnly, [S, g] = kl(!1), C = Sl(() => g((b) => !b), []);
  if (!p) return null;
  const y = s != null, R = c != null && c.length > 0, x = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    y ? "tlFormField--error" : "",
    !y && R ? "tlFormField--warning" : "",
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
      onClick: C,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(z, { control: v })), !f && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !y && R && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((b, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, b)))), !f && u && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, Tl = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
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
}, Rl = 20, xl = () => {
  const l = q(), t = ae(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((b, _) => {
    t(_ ? "collapse" : "expand", { nodeId: b });
  }, [t]), h = e.useCallback((b, _) => {
    t("select", {
      nodeId: b,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), v = e.useCallback((b, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: b, x: _.clientX, y: _.clientY });
  }, [t]), f = e.useRef(null), S = e.useCallback((b, _) => {
    const E = _.getBoundingClientRect(), G = b.clientY - E.top, I = E.height / 3;
    return G < I ? "above" : G > I * 2 ? "below" : "within";
  }, []), g = e.useCallback((b, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", b);
  }, []), C = e.useCallback((b, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const E = S(_, _.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: b, position: E }), f.current = null;
    }, 50);
  }, [t, S]), y = e.useCallback((b, _) => {
    _.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = S(_, _.currentTarget);
    t("drop", { nodeId: b, position: E });
  }, [t, S]), R = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), x = e.useCallback((b) => {
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
            const G = E.depth;
            for (let I = o - 1; I >= 0; I--)
              if (n[I].depth < G) {
                _ = I;
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
        b.preventDefault(), r === "multi" && o >= 0 && o < n.length && t("select", {
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
          c === b.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === b.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === b.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: b.depth * Rl },
        draggable: i,
        onClick: (E) => h(b.id, E),
        onContextMenu: (E) => v(b.id, E),
        onDragStart: (E) => g(b.id, E),
        onDragOver: s ? (E) => C(b.id, E) : void 0,
        onDrop: s ? (E) => y(b.id, E) : void 0,
        onDragEnd: R
      },
      b.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(b.id, b.expanded);
          },
          tabIndex: -1,
          "aria-label": b.expanded ? "Collapse" : "Expand"
        },
        b.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: b.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(z, { control: b.content }))
    ))
  );
};
var Ze = { exports: {} }, ue = {}, Qe = { exports: {} }, X = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var _t;
function Ll() {
  if (_t) return X;
  _t = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), r = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), o = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
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
  }, S = Object.assign, g = {};
  function C(d, k, F) {
    this.props = d, this.context = k, this.refs = g, this.updater = F || f;
  }
  C.prototype.isReactComponent = {}, C.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, C.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = C.prototype;
  function R(d, k, F) {
    this.props = d, this.context = k, this.refs = g, this.updater = F || f;
  }
  var x = R.prototype = new y();
  x.constructor = R, S(x, C.prototype), x.isPureReactComponent = !0;
  var b = Array.isArray;
  function _() {
  }
  var E = { H: null, A: null, T: null, S: null }, G = Object.prototype.hasOwnProperty;
  function I(d, k, F) {
    var O = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: O !== void 0 ? O : null,
      props: F
    };
  }
  function T(d, k) {
    return I(d.type, k, d.props);
  }
  function U(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function P(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return k[F];
    });
  }
  var N = /\/+/g;
  function A(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? P("" + d.key) : k.toString(36);
  }
  function J(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(_, _) : (d.status = "pending", d.then(
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
  function $(d, k, F, O, V) {
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
              return Q = d._init, $(
                Q(d._payload),
                k,
                F,
                O,
                V
              );
          }
      }
    if (Q)
      return V = V(d), Q = O === "" ? "." + A(d, 0) : O, b(V) ? (F = "", Q != null && (F = Q.replace(N, "$&/") + "/"), $(V, k, F, "", function(me) {
        return me;
      })) : V != null && (U(V) && (V = T(
        V,
        F + (V.key == null || d && d.key === V.key ? "" : ("" + V.key).replace(
          N,
          "$&/"
        ) + "/") + Q
      )), k.push(V)), 1;
    Q = 0;
    var le = O === "" ? "." : O + ":";
    if (b(d))
      for (var ee = 0; ee < d.length; ee++)
        O = d[ee], M = le + A(O, ee), Q += $(
          O,
          k,
          F,
          M,
          V
        );
    else if (ee = v(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(O = d.next()).done; )
        O = O.value, M = le + A(O, ee++), Q += $(
          O,
          k,
          F,
          M,
          V
        );
    else if (M === "object") {
      if (typeof d.then == "function")
        return $(
          J(d),
          k,
          F,
          O,
          V
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function Z(d, k, F) {
    if (d == null) return d;
    var O = [], V = 0;
    return $(d, O, "", "", function(M) {
      return k.call(F, M, V++);
    }), O;
  }
  function B(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
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
  }, K = {
    map: Z,
    forEach: function(d, k, F) {
      Z(
        d,
        function() {
          k.apply(this, arguments);
        },
        F
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
      if (!U(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return X.Activity = p, X.Children = K, X.Component = C, X.Fragment = n, X.Profiler = i, X.PureComponent = R, X.StrictMode = r, X.Suspense = o, X.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, X.__COMPILER_RUNTIME = {
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
  }, X.cloneElement = function(d, k, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var O = S({}, d.props), V = d.key;
    if (k != null)
      for (M in k.key !== void 0 && (V = "" + k.key), k)
        !G.call(k, M) || M === "key" || M === "__self" || M === "__source" || M === "ref" && k.ref === void 0 || (O[M] = k[M]);
    var M = arguments.length - 2;
    if (M === 1) O.children = F;
    else if (1 < M) {
      for (var Q = Array(M), le = 0; le < M; le++)
        Q[le] = arguments[le + 2];
      O.children = Q;
    }
    return I(d.type, V, O);
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
  }, X.createElement = function(d, k, F) {
    var O, V = {}, M = null;
    if (k != null)
      for (O in k.key !== void 0 && (M = "" + k.key), k)
        G.call(k, O) && O !== "key" && O !== "__self" && O !== "__source" && (V[O] = k[O]);
    var Q = arguments.length - 2;
    if (Q === 1) V.children = F;
    else if (1 < Q) {
      for (var le = Array(Q), ee = 0; ee < Q; ee++)
        le[ee] = arguments[ee + 2];
      V.children = le;
    }
    if (d && d.defaultProps)
      for (O in Q = d.defaultProps, Q)
        V[O] === void 0 && (V[O] = Q[O]);
    return I(d, M, V);
  }, X.createRef = function() {
    return { current: null };
  }, X.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, X.isValidElement = U, X.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: B
    };
  }, X.memo = function(d, k) {
    return {
      $$typeof: a,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, X.startTransition = function(d) {
    var k = E.T, F = {};
    E.T = F;
    try {
      var O = d(), V = E.S;
      V !== null && V(F, O), typeof O == "object" && O !== null && typeof O.then == "function" && O.then(_, j);
    } catch (M) {
      j(M);
    } finally {
      k !== null && F.types !== null && (k.types = F.types), E.T = k;
    }
  }, X.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, X.use = function(d) {
    return E.H.use(d);
  }, X.useActionState = function(d, k, F) {
    return E.H.useActionState(d, k, F);
  }, X.useCallback = function(d, k) {
    return E.H.useCallback(d, k);
  }, X.useContext = function(d) {
    return E.H.useContext(d);
  }, X.useDebugValue = function() {
  }, X.useDeferredValue = function(d, k) {
    return E.H.useDeferredValue(d, k);
  }, X.useEffect = function(d, k) {
    return E.H.useEffect(d, k);
  }, X.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, X.useId = function() {
    return E.H.useId();
  }, X.useImperativeHandle = function(d, k, F) {
    return E.H.useImperativeHandle(d, k, F);
  }, X.useInsertionEffect = function(d, k) {
    return E.H.useInsertionEffect(d, k);
  }, X.useLayoutEffect = function(d, k) {
    return E.H.useLayoutEffect(d, k);
  }, X.useMemo = function(d, k) {
    return E.H.useMemo(d, k);
  }, X.useOptimistic = function(d, k) {
    return E.H.useOptimistic(d, k);
  }, X.useReducer = function(d, k, F) {
    return E.H.useReducer(d, k, F);
  }, X.useRef = function(d) {
    return E.H.useRef(d);
  }, X.useState = function(d) {
    return E.H.useState(d);
  }, X.useSyncExternalStore = function(d, k, F) {
    return E.H.useSyncExternalStore(
      d,
      k,
      F
    );
  }, X.useTransition = function() {
    return E.H.useTransition();
  }, X.version = "19.2.4", X;
}
var vt;
function Dl() {
  return vt || (vt = 1, Qe.exports = Ll()), Qe.exports;
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
var Et;
function Il() {
  if (Et) return ue;
  Et = 1;
  var l = Dl();
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
  return ue.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = r, ue.createPortal = function(o, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return s(o, a, null, m);
  }, ue.flushSync = function(o) {
    var a = c.T, m = r.p;
    try {
      if (c.T = null, r.p = 2, o) return o();
    } finally {
      c.T = a, r.p = m, r.d.f();
    }
  }, ue.preconnect = function(o, a) {
    typeof o == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, r.d.C(o, a));
  }, ue.prefetchDNS = function(o) {
    typeof o == "string" && r.d.D(o);
  }, ue.preinit = function(o, a) {
    if (typeof o == "string" && a && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, v = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? r.d.S(
        o,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: v
        }
      ) : m === "script" && r.d.X(o, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: v,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, ue.preinitModule = function(o, a) {
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
  }, ue.preload = function(o, a) {
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
  }, ue.preloadModule = function(o, a) {
    if (typeof o == "string")
      if (a) {
        var m = u(a.as, a.crossOrigin);
        r.d.m(o, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else r.d.m(o);
  }, ue.requestFormReset = function(o) {
    r.d.r(o);
  }, ue.unstable_batchedUpdates = function(o, a) {
    return o(a);
  }, ue.useFormState = function(o, a, m) {
    return c.H.useFormState(o, a, m);
  }, ue.useFormStatus = function() {
    return c.H.useHostTransitionStatus();
  }, ue.version = "19.2.4", ue;
}
var gt;
function Ml() {
  if (gt) return Ze.exports;
  gt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ze.exports = Il(), Ze.exports;
}
var xt = Ml();
const { useState: Ce, useCallback: de, useRef: je, useEffect: Te, useMemo: rt } = e;
function ct({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function jl({
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
  const m = de(
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
    /* @__PURE__ */ e.createElement(ct, { image: l.image }),
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
function Pl({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const c = de(() => r(l.value), [r, l.value]), u = rt(() => {
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
    /* @__PURE__ */ e.createElement(ct, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const Bl = ({ controlId: l, state: t }) => {
  const n = ae(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = s && i && !u && o, v = ie({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = v["js.dropdownSelect.nothingFound"], S = de(
    (D) => v["js.dropdownSelect.removeChip"].replace("{0}", D),
    [v]
  ), [g, C] = Ce(!1), [y, R] = Ce(""), [x, b] = Ce(-1), [_, E] = Ce(!1), [G, I] = Ce({}), [T, U] = Ce(null), [P, N] = Ce(null), [A, J] = Ce(null), $ = je(null), Z = je(null), B = je(null), j = je(r);
  j.current = r;
  const K = je(-1), d = rt(
    () => new Set(r.map((D) => D.value)),
    [r]
  ), k = rt(() => {
    let D = m.filter((H) => !d.has(H.value));
    if (y) {
      const H = y.toLowerCase();
      D = D.filter((te) => te.label.toLowerCase().includes(H));
    }
    return D;
  }, [m, d, y]);
  Te(() => {
    y && k.length === 1 ? b(0) : b(-1);
  }, [k.length, y]), Te(() => {
    g && a && Z.current && Z.current.focus();
  }, [g, a, r]), Te(() => {
    var te, re;
    if (K.current < 0) return;
    const D = K.current;
    K.current = -1;
    const H = (te = $.current) == null ? void 0 : te.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    H && H.length > 0 ? H[Math.min(D, H.length - 1)].focus() : (re = $.current) == null || re.focus();
  }, [r]), Te(() => {
    if (!g) return;
    const D = (H) => {
      $.current && !$.current.contains(H.target) && B.current && !B.current.contains(H.target) && (C(!1), R(""));
    };
    return document.addEventListener("mousedown", D), () => document.removeEventListener("mousedown", D);
  }, [g]), Te(() => {
    if (!g || !$.current) return;
    const D = $.current.getBoundingClientRect(), H = window.innerHeight - D.bottom, re = H < 300 && D.top > H;
    I({
      left: D.left,
      width: D.width,
      ...re ? { bottom: window.innerHeight - D.top } : { top: D.bottom }
    });
  }, [g]);
  const F = de(async () => {
    if (!(u || !o) && (C(!0), R(""), b(-1), E(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, o, a, n]), O = de(() => {
    var D;
    C(!1), R(""), b(-1), (D = $.current) == null || D.focus();
  }, []), V = de(
    (D) => {
      let H;
      if (i) {
        const te = m.find((re) => re.value === D);
        if (te)
          H = [...j.current, te];
        else
          return;
      } else {
        const te = m.find((re) => re.value === D);
        if (te)
          H = [te];
        else
          return;
      }
      j.current = H, n("valueChanged", { value: H.map((te) => te.value) }), i ? (R(""), b(-1)) : O();
    },
    [i, m, n, O]
  ), M = de(
    (D) => {
      K.current = j.current.findIndex((te) => te.value === D);
      const H = j.current.filter((te) => te.value !== D);
      j.current = H, n("valueChanged", { value: H.map((te) => te.value) });
    },
    [n]
  ), Q = de(
    (D) => {
      D.stopPropagation(), n("valueChanged", { value: [] }), O();
    },
    [n, O]
  ), le = de((D) => {
    R(D.target.value);
  }, []), ee = de(
    (D) => {
      if (!g) {
        if (D.key === "ArrowDown" || D.key === "ArrowUp" || D.key === "Enter" || D.key === " ") {
          if (D.target.tagName === "BUTTON") return;
          D.preventDefault(), D.stopPropagation(), F();
        }
        return;
      }
      switch (D.key) {
        case "ArrowDown":
          D.preventDefault(), D.stopPropagation(), b(
            (H) => H < k.length - 1 ? H + 1 : 0
          );
          break;
        case "ArrowUp":
          D.preventDefault(), D.stopPropagation(), b(
            (H) => H > 0 ? H - 1 : k.length - 1
          );
          break;
        case "Enter":
          D.preventDefault(), D.stopPropagation(), x >= 0 && x < k.length && V(k[x].value);
          break;
        case "Escape":
          D.preventDefault(), D.stopPropagation(), O();
          break;
        case "Tab":
          O();
          break;
        case "Backspace":
          y === "" && i && r.length > 0 && M(r[r.length - 1].value);
          break;
      }
    },
    [
      g,
      F,
      O,
      k,
      x,
      V,
      y,
      i,
      r,
      M
    ]
  ), me = de(
    async (D) => {
      D.preventDefault(), E(!1);
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
    },
    [n]
  ), he = de(
    (D, H) => {
      U(D), H.dataTransfer.effectAllowed = "move", H.dataTransfer.setData("text/plain", String(D));
    },
    []
  ), be = de(
    (D, H) => {
      if (H.preventDefault(), H.dataTransfer.dropEffect = "move", T === null || T === D) {
        N(null), J(null);
        return;
      }
      const te = H.currentTarget.getBoundingClientRect(), re = te.left + te.width / 2, Me = H.clientX < re ? "before" : "after";
      N(D), J(Me);
    },
    [T]
  ), Ee = de(
    (D) => {
      if (D.preventDefault(), T === null || P === null || A === null || T === P) return;
      const H = [...j.current], [te] = H.splice(T, 1);
      let re = P;
      T < P ? re = A === "before" ? re - 1 : re : re = A === "before" ? re : re + 1, H.splice(re, 0, te), j.current = H, n("valueChanged", { value: H.map((Me) => Me.value) }), U(null), N(null), J(null);
    },
    [T, P, A, n]
  ), ge = de(() => {
    U(null), N(null), J(null);
  }, []);
  if (Te(() => {
    if (x < 0 || !B.current) return;
    const D = B.current.querySelector(
      `[id="${l}-opt-${x}"]`
    );
    D && D.scrollIntoView({ block: "nearest" });
  }, [x, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((D) => /* @__PURE__ */ e.createElement("span", { key: D.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(ct, { image: D.image }), /* @__PURE__ */ e.createElement("span", null, D.label))));
  const we = !c && r.length > 0 && !u, Be = g ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: G
    },
    (a || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: Z,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: le,
        onKeyDown: ee,
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
      !a && !_ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: me }, v["js.dropdownSelect.error"])),
      a && k.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, f),
      a && k.map((D, H) => /* @__PURE__ */ e.createElement(
        Pl,
        {
          key: D.value,
          id: `${l}-opt-${H}`,
          option: D,
          highlighted: H === x,
          searchTerm: y,
          onSelect: V,
          onMouseEnter: () => b(H)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: $,
      className: "tlDropdownSelect" + (g ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": g,
      "aria-haspopup": "listbox",
      "aria-owns": g ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: g ? void 0 : F,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((D, H) => {
      let te = "";
      return T === H ? te = "tlDropdownSelect__chip--dragging" : P === H && A === "before" ? te = "tlDropdownSelect__chip--dropBefore" : P === H && A === "after" && (te = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        jl,
        {
          key: D.value,
          option: D,
          removable: !u && (i || !c),
          onRemove: M,
          removeLabel: S(D.label),
          draggable: h,
          onDragStart: h ? (re) => he(H, re) : void 0,
          onDragOver: h ? (re) => be(H, re) : void 0,
          onDrop: h ? Ee : void 0,
          onDragEnd: h ? ge : void 0,
          dragClassName: h ? te : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, we && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": v["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, g ? "▲" : "▼"))
  ), Be && xt.createPortal(Be, document.body));
}, { useCallback: Je, useRef: Al } = e, Lt = "application/x-tl-color", Ol = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Al(null), u = Je(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Je((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = Je(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(Lt);
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
function Dt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function ot(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function It(l) {
  if (!ot(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Mt(l, t, n) {
  const r = (i) => Dt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function $l(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const m = c === 0 ? 0 : o / c;
  return [a, m, c];
}
function Fl(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function Hl(l) {
  return $l(...It(l));
}
function et(l, t, n) {
  return Mt(...Fl(l, t, n));
}
const { useCallback: Re, useRef: Ct } = e, Wl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = Hl(l), s = Ct(null), c = Ct(null), u = Re(
    (f, S) => {
      var R;
      const g = (R = s.current) == null ? void 0 : R.getBoundingClientRect();
      if (!g) return;
      const C = Math.max(0, Math.min(1, (f - g.left) / g.width)), y = Math.max(0, Math.min(1, 1 - (S - g.top) / g.height));
      t(et(n, C, y));
    },
    [n, t]
  ), o = Re(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), a = Re(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = Re(
    (f) => {
      var y;
      const S = (y = c.current) == null ? void 0 : y.getBoundingClientRect();
      if (!S) return;
      const C = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(et(C, r, i));
    },
    [r, i, t]
  ), p = Re(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = Re(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), v = et(n, 1, 1);
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
function zl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Ul = {
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
}, { useState: Fe, useCallback: ve, useEffect: tt, useRef: Vl, useLayoutEffect: Kl } = e, Yl = ({
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
  const [a, m] = Fe("palette"), [p, h] = Fe(t), v = Vl(null), f = ie(Ul), [S, g] = Fe(null);
  Kl(() => {
    if (!l.current || !v.current) return;
    const B = l.current.getBoundingClientRect(), j = v.current.getBoundingClientRect();
    let K = B.bottom + 4, d = B.left;
    K + j.height > window.innerHeight && (K = B.top - j.height - 4), d + j.width > window.innerWidth && (d = Math.max(0, B.right - j.width)), g({ top: K, left: d });
  }, [l]);
  const C = p != null, [y, R, x] = C ? It(p) : [0, 0, 0], [b, _] = Fe((p == null ? void 0 : p.toUpperCase()) ?? "");
  tt(() => {
    _((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), tt(() => {
    const B = (j) => {
      j.key === "Escape" && u();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [u]), tt(() => {
    const B = (K) => {
      v.current && !v.current.contains(K.target) && u();
    }, j = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", B);
    };
  }, [u]);
  const E = ve(
    (B) => (j) => {
      const K = parseInt(j.target.value, 10);
      if (isNaN(K)) return;
      const d = Dt(K);
      h(Mt(B === "r" ? d : y, B === "g" ? d : R, B === "b" ? d : x));
    },
    [y, R, x]
  ), G = ve(
    (B) => {
      if (p != null) {
        B.dataTransfer.setData(Lt, p.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const j = document.createElement("div");
        j.style.width = "33px", j.style.height = "33px", j.style.backgroundColor = p, j.style.borderRadius = "3px", j.style.border = "1px solid rgba(0,0,0,0.1)", j.style.position = "absolute", j.style.top = "-9999px", document.body.appendChild(j), B.dataTransfer.setDragImage(j, 16, 16), requestAnimationFrame(() => document.body.removeChild(j));
      }
    },
    [p]
  ), I = ve((B) => {
    const j = B.target.value;
    _(j), ot(j) && h(j);
  }, []), T = ve(() => {
    h(null);
  }, []), U = ve((B) => {
    h(B);
  }, []), P = ve(
    (B) => {
      c(B);
    },
    [c]
  ), N = ve(
    (B, j) => {
      const K = [...n], d = K[B];
      K[B] = K[j], K[j] = d, o(K);
    },
    [n, o]
  ), A = ve(
    (B, j) => {
      const K = [...n];
      K[B] = j, o(K);
    },
    [n, o]
  ), J = ve(() => {
    o([...i]);
  }, [i, o]), $ = ve(
    (B) => {
      if (zl(n, B)) return;
      const j = n.indexOf(null);
      if (j < 0) return;
      const K = [...n];
      K[j] = B.toUpperCase(), o(K);
    },
    [n, o]
  ), Z = ve(() => {
    p != null && $(p), c(p);
  }, [p, c, $]);
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
      Ol,
      {
        colors: n,
        columns: r,
        onSelect: U,
        onConfirm: P,
        onSwap: N,
        onReplace: A
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: J }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Wl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (C ? "" : " tlColorInput--noColor"),
        style: C ? { backgroundColor: p } : void 0,
        draggable: C,
        onDragStart: C ? G : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? y : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? R : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: C ? x : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (b !== "" && !ot(b) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: b,
        onChange: I
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: Z }, f["js.colorInput.ok"]))
  );
}, Gl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Xl, useCallback: He, useRef: ql } = e, Zl = ({ controlId: l, state: t }) => {
  const n = ae(), r = ie(Gl), [i, s] = Xl(!1), c = ql(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, h = He(() => {
    o && s(!0);
  }, [o]), v = He(
    (g) => {
      s(!1), n("valueChanged", { value: g });
    },
    [n]
  ), f = He(() => {
    s(!1);
  }, []), S = He(
    (g) => {
      n("paletteChanged", { palette: g });
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
    Yl,
    {
      anchorRef: c,
      currentColor: u,
      palette: a,
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
}, { useState: Pe, useCallback: ye, useEffect: We, useRef: yt, useLayoutEffect: Ql, useMemo: Jl } = e, er = {
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
}, tr = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = ie(er), [o, a] = Pe("simple"), [m, p] = Pe(""), [h, v] = Pe(t ?? ""), [f, S] = Pe(!1), [g, C] = Pe(null), y = yt(null), R = yt(null);
  Ql(() => {
    if (!l.current || !y.current) return;
    const P = l.current.getBoundingClientRect(), N = y.current.getBoundingClientRect();
    let A = P.bottom + 4, J = P.left;
    A + N.height > window.innerHeight && (A = P.top - N.height - 4), J + N.width > window.innerWidth && (J = Math.max(0, P.right - N.width)), C({ top: A, left: J });
  }, [l]), We(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), We(() => {
    r && R.current && R.current.focus();
  }, [r]), We(() => {
    const P = (N) => {
      N.key === "Escape" && s();
    };
    return document.addEventListener("keydown", P), () => document.removeEventListener("keydown", P);
  }, [s]), We(() => {
    const P = (A) => {
      y.current && !y.current.contains(A.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", P), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", P);
    };
  }, [s]);
  const x = Jl(() => {
    if (!m) return n;
    const P = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(P) || N.label.toLowerCase().includes(P) || N.terms != null && N.terms.some((A) => A.includes(P))
    );
  }, [n, m]), b = ye((P) => {
    p(P.target.value);
  }, []), _ = ye(
    (P) => {
      i(P);
    },
    [i]
  ), E = ye((P) => {
    v(P);
  }, []), G = ye((P) => {
    v(P.target.value);
  }, []), I = ye(() => {
    i(h || null);
  }, [h, i]), T = ye(() => {
    i(null);
  }, [i]), U = ye(async (P) => {
    P.preventDefault(), S(!1);
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
      ref: y,
      style: g ? { top: g.top, left: g.left, visibility: "visible" } : { visibility: "hidden" }
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
        ref: R,
        type: "text",
        className: "tlIconSelect__search",
        value: m,
        onChange: b,
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
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: U }, u["js.iconSelect.loadError"])),
      r && x.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && x.map(
        (P) => P.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: P.label,
            onClick: () => o === "simple" ? _(N.encoded) : E(N.encoded),
            onKeyDown: (A) => {
              (A.key === "Enter" || A.key === " ") && (A.preventDefault(), o === "simple" ? _(N.encoded) : E(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(Le, { encoded: N.encoded })
        ))
      )
    ),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: G
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(Le, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: I }, u["js.iconSelect.ok"]))
  );
}, nr = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: lr, useCallback: ze, useRef: rr } = e, or = ({ controlId: l, state: t }) => {
  const n = ae(), r = ie(nr), [i, s] = lr(!1), c = rr(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = ze(() => {
    o && !a && s(!0);
  }, [o, a]), v = ze(
    (g) => {
      s(!1), n("valueChanged", { value: g });
    },
    [n]
  ), f = ze(() => {
    s(!1);
  }, []), S = ze(async () => {
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
    u ? /* @__PURE__ */ e.createElement(Le, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    tr,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: v,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(Le, { encoded: u }) : null));
}, { useCallback: xe, useEffect: ar, useMemo: wt, useRef: sr, useState: nt } = e, cr = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, ir = [1, 2, 3, 4];
function ur(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function dr(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of ir)
    n >= i && (r = i);
  return r;
}
function mr(l, t) {
  const n = cr[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function pr(l, t) {
  const n = Math.max(1, t), r = {}, i = (p, h) => !!(r[p] && r[p][h]), s = (p, h) => {
    r[p] || (r[p] = {}), r[p][h] = !0;
  }, c = [];
  let u = 0, o = 0;
  const a = (p) => {
    let h = null;
    for (const f of c) f.rowStart === p && (h = f);
    if (!h) return;
    let v = h.colEnd;
    for (; v < n && !i(p, v); ) v++;
    if (v !== h.colEnd) {
      for (let f = h.rowStart; f < h.rowEnd; f++)
        for (let S = h.colEnd; S < v; S++) s(f, S);
      h.colEnd = v;
    }
  };
  for (const p of l) {
    const h = n <= 1 ? 1 : Math.max(1, p.rowSpan || 1);
    let v = Math.min(mr(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let R = o; R < n && !i(u, R); R++)
      f++;
    if (v > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let R = o; R < n && !i(u, R); R++)
        f++;
      v = Math.min(v, f);
    }
    const S = o, g = o + v, C = u, y = u + h;
    c.push({ id: p.id, colStart: S, colEnd: g, rowStart: C, rowEnd: y });
    for (let R = C; R < y; R++)
      for (let x = S; x < g; x++) s(R, x);
    o = g, o >= n && (o = 0, u++);
  }
  a(u);
  let m = 0;
  for (const p of c) p.rowEnd > m && (m = p.rowEnd);
  for (let p = 1; p < m; p++)
    for (let h = 0; h < n; h++) {
      if (i(p, h)) continue;
      const v = c.find((f) => f.rowEnd === p && f.colStart <= h && h < f.colEnd);
      if (v) {
        v.rowEnd = p + 1;
        for (let f = v.colStart; f < v.colEnd; f++) s(p, f);
      }
    }
  return c;
}
const fr = ({ controlId: l }) => {
  const t = q(), n = ae(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((_) => _ && _.id), s = sr(null), [c, u] = nt(1), o = t.editMode === !0;
  ar(() => {
    const _ = s.current;
    if (!_) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, G = ur(r, E), I = () => u(dr(_.clientWidth, G));
    I();
    const T = new ResizeObserver(I);
    return T.observe(_), () => T.disconnect();
  }, [r]);
  const a = wt(() => pr(i, c), [i, c]), m = wt(() => {
    const _ = {};
    for (const E of a) _[E.id] = E;
    return _;
  }, [a]), [p, h] = nt(null), [v, f] = nt(null), S = xe((_, E) => {
    if (!o) {
      _.preventDefault();
      return;
    }
    h(E), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", E);
  }, [o]), g = xe((_, E) => {
    if (!o || !p || p === E) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const G = _.currentTarget.getBoundingClientRect(), I = _.clientX < G.left + G.width / 2;
    f((T) => T && T.id === E && T.before === I ? T : { id: E, before: I });
  }, [o, p]), C = xe(() => {
  }, []), y = xe((_, E, G) => {
    const I = i.map((N) => N.id), T = I.indexOf(_);
    if (T < 0) return;
    I.splice(T, 1);
    const U = I.indexOf(E);
    if (U < 0) {
      I.splice(T, 0, _);
      return;
    }
    const P = G ? U : U + 1;
    I.splice(P, 0, _), n("reorder", { order: I });
  }, [i, n]), R = xe((_, E) => {
    if (!o || !p || p === E) return;
    _.preventDefault();
    const G = _.currentTarget.getBoundingClientRect(), I = _.clientX < G.left + G.width / 2;
    y(p, E, I), h(null), f(null);
  }, [o, p, y]), x = xe(() => {
    h(null), f(null);
  }, []), b = {
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
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: b }, i.map((_) => {
      const E = m[_.id];
      if (!E) return null;
      const G = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, I = ["tlDashboard__tile"];
      return p === _.id && I.push("tlDashboard__tile--dragging"), v && v.id === _.id && I.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: I.join(" "),
          style: G,
          draggable: o,
          onDragStart: (T) => S(T, _.id),
          onDragOver: (T) => g(T, _.id),
          onDragLeave: C,
          onDrop: (T) => R(T, _.id),
          onDragEnd: x
        },
        /* @__PURE__ */ e.createElement(z, { control: _.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: hr, useRef: kt, useState: St, useEffect: Nt, useLayoutEffect: br } = e, _r = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: n }))));
}, vr = ({ group: l }) => {
  var p, h;
  const [t, n] = St(!1), [r, i] = St({}), s = kt(null), c = kt(null), u = hr(() => {
    n((v) => !v);
  }, []);
  br(() => {
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
  }, [t]), Nt(() => {
    if (!t) return;
    const v = (f) => {
      c.current && !c.current.contains(f.target) && s.current && !s.current.contains(f.target) && n(!1);
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [t]), Nt(() => {
    if (!t) return;
    const v = (f) => {
      f.key === "Escape" && n(!1);
    };
    return document.addEventListener("keydown", v), () => document.removeEventListener("keydown", v);
  }, [t]);
  const o = l.items.filter((v) => v != null);
  if (o.length === 0) return null;
  if (o.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(z, { control: o[0] })));
  const a = l.label ?? l.name, m = !!l.icon;
  return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--menu" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      type: "button",
      className: "tlToolbar__menuTrigger" + (m ? " tlToolbar__menuTrigger--icon" : ""),
      onClick: u,
      "aria-expanded": t,
      "aria-haspopup": "true",
      "aria-label": m ? a : void 0,
      title: m ? a : void 0
    },
    m ? /* @__PURE__ */ e.createElement(Le, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, a), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
  ), xt.createPortal(
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
      o.map((v, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: v }))),
      (h = l.subGroups) == null ? void 0 : h.map((v, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, g) => /* @__PURE__ */ e.createElement("div", { key: g, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(z, { control: S })))))
    ),
    document.body
  ));
}, Er = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(vr, { group: i }) : /* @__PURE__ */ e.createElement(_r, { group: i }))));
}, gr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(z, { control: t.frame }));
}, Cr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(z, { key: i, control: r })));
}, yr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), wr = {
  "js.sidebar.openDrawer": "Open navigation"
}, kr = ({ controlId: l }) => {
  const t = ae(), n = ie(wr);
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
W("TLButton", qt);
W("TLToggleButton", Qt);
W("TLTextInput", At);
W("TLPasswordInput", $t);
W("TLNumberInput", Ht);
W("TLDatePicker", zt);
W("TLSelect", Vt);
W("TLCheckbox", Yt);
W("TLTable", Gt);
W("TLCounter", Jt);
W("TLTabBar", tn);
W("TLFieldList", nn);
W("TLAudioRecorder", rn);
W("TLAudioPlayer", an);
W("TLFileUpload", cn);
W("TLDownload", dn);
W("TLPhotoCapture", pn);
W("TLPhotoViewer", hn);
W("TLSplitPanel", bn);
W("TLPanel", wn);
W("TLInset", Pn);
W("TLMaximizeRoot", kn);
W("TLDeckPane", Sn);
W("TLSidebar", Mn);
W("TLStack", jn);
W("TLGrid", Bn);
W("TLCard", An);
W("TLAppBar", On);
W("TLBreadcrumb", Fn);
W("TLBottomBar", Wn);
W("TLDialog", Un);
W("TLDialogManager", Yn);
W("TLWindow", qn);
W("TLDrawer", el);
W("TLContextMenuRegion", nl);
W("TLSnackbar", al);
W("TLMenu", cl);
W("TLAppShell", il);
W("TLText", ul);
W("TLTableView", ml);
W("TLFormLayout", El);
W("TLFormGroup", yl);
W("TLFormField", Nl);
W("TLResourceCell", Tl);
W("TLTreeView", xl);
W("TLDropdownSelect", Bl);
W("TLColorInput", Zl);
W("TLIconSelect", or);
W("TLDashboard", fr);
W("TLToolbar", Er);
W("TLTileStack", gr);
W("TLSlot", Cr);
W("TLSlotContent", yr);
W("TLDrawerToggle", kr);
