import { React as e, useTLFieldValue as Re, getComponent as Et, useTLState as Z, useTLCommand as ae, TLChild as q, useTLUpload as Xe, useI18N as se, useTLDataUrl as qe, register as H } from "tl-react-bridge";
const { useCallback: gt } = e, Ct = ({ controlId: n, state: t }) => {
  const [a, l] = Re(), c = gt(
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
  const [l, c] = Re(), i = yt(
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
  const [a, l] = Re(), c = kt(
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
}, { useCallback: Nt } = e, Rt = ({ controlId: n, state: t, config: a }) => {
  var m;
  const [l, c] = Re(), i = Nt(
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
}, { useCallback: Tt } = e, xt = ({ controlId: n, state: t }) => {
  const [a, l] = Re(), c = Tt(
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
}, Lt = ({ controlId: n, state: t }) => {
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
const { useCallback: Dt } = e, It = ({ controlId: n, command: t, label: a, disabled: l }) => {
  const c = Z(), i = ae(), o = t ?? "click", u = a ?? c.label, s = l ?? c.disabled === !0, r = c.hidden === !0, m = c.image, p = c.iconOnly === !0, _ = r ? { display: "none" } : void 0, k = Dt(() => {
    i(o);
  }, [i, o]);
  return m && p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: k,
      disabled: s,
      style: _,
      className: "tlReactButton tlReactButton--icon",
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(we, { encoded: m })
  ) : m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: k,
      disabled: s,
      style: _,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: k,
      disabled: s,
      style: _,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: jt } = e, Pt = ({ controlId: n, command: t, label: a, active: l, disabled: c }) => {
  const i = Z(), o = ae(), u = t ?? "click", s = a ?? i.label, r = l ?? i.active === !0, m = c ?? i.disabled === !0, p = jt(() => {
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
  const t = Z(), a = ae(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: At } = e, Bt = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = t.tabs ?? [], c = t.activeTabId, i = At((o) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, Ot = ({ controlId: n }) => {
  const t = Z(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: c })))));
}, $t = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ft = ({ controlId: n }) => {
  const t = Z(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, _ = m === "received" ? "idle" : l !== "idle" ? l : m, k = e.useCallback(async () => {
    if (l === "recording") {
      const y = u.current;
      y && y.state !== "inactive" && y.stop();
      return;
    }
    if (l !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const y = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = y, s.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", B = new MediaRecorder(y, M ? { mimeType: M } : void 0);
        u.current = B, B.ondataavailable = (h) => {
          h.data.size > 0 && s.current.push(h.data);
        }, B.onstop = async () => {
          y.getTracks().forEach((C) => C.stop()), r.current = null;
          const h = new Blob(s.current, { type: B.mimeType || "audio/webm" });
          if (s.current = [], h.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const g = new FormData();
          g.append("audio", h, "recording.webm"), await a(g), c("idle");
        }, B.start(), c("recording");
      } catch (y) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", y), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, a]), v = se($t), b = _ === "recording" ? v["js.audioRecorder.stop"] : _ === "uploading" ? v["js.uploading"] : v["js.audioRecorder.record"], f = _ === "uploading", S = ["tlAudioRecorder__button"];
  return _ === "recording" && S.push("tlAudioRecorder__button--recording"), _ === "uploading" && S.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: S.join(" "),
      onClick: k,
      disabled: f,
      title: b,
      "aria-label": b
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${_ === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, v[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Ht = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, zt = ({ controlId: n }) => {
  const t = Z(), a = qe(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
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
  }, [i, a]), p = se(Ht), _ = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], k = i === "disabled" || i === "loading", v = ["tlAudioPlayer__button"];
  return i === "playing" && v.push("tlAudioPlayer__button--playing"), i === "loading" && v.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: v.join(" "),
      onClick: m,
      disabled: k,
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Wt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ut = ({ controlId: n }) => {
  const t = Z(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, _ = e.useCallback(async (h) => {
    c("uploading");
    const g = new FormData();
    g.append("file", h, h.name), await a(g), c("idle");
  }, [a]), k = e.useCallback((h) => {
    var C;
    const g = (C = h.target.files) == null ? void 0 : C[0];
    g && _(g);
  }, [_]), v = e.useCallback(() => {
    var h;
    l !== "uploading" && ((h = u.current) == null || h.click());
  }, [l]), b = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!0);
  }, []), f = e.useCallback((h) => {
    h.preventDefault(), h.stopPropagation(), o(!1);
  }, []), S = e.useCallback((h) => {
    var C;
    if (h.preventDefault(), h.stopPropagation(), o(!1), l === "uploading") return;
    const g = (C = h.dataTransfer.files) == null ? void 0 : C[0];
    g && _(g);
  }, [l, _]), y = p === "uploading", M = se(Wt), B = p === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
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
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: v,
        disabled: y,
        title: B,
        "aria-label": B
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
  const t = Z(), a = qe(), l = ae(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const v = a + (a.includes("?") ? "&" : "?") + "rev=" + i, b = await fetch(v);
        if (!b.ok) {
          console.error("[TLDownload] Failed to fetch data:", b.status);
          return;
        }
        const f = await b.blob(), S = URL.createObjectURL(f), y = document.createElement("a");
        y.href = S, y.download = o, y.style.display = "none", document.body.appendChild(y), y.click(), document.body.removeChild(y), URL.revokeObjectURL(S);
      } catch (v) {
        console.error("[TLDownload] Fetch error:", v);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), _ = se(Vt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, _["js.download.noFile"]));
  const k = s ? _["js.downloading"] : _["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: k,
      "aria-label": k
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
  const t = Z(), a = Xe(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), _ = e.useRef(null), k = e.useRef(null), v = t.error, b = e.useMemo(
    () => {
      var x;
      return !!(window.isSecureContext && ((x = navigator.mediaDevices) != null && x.getUserMedia));
    },
    []
  ), f = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((x) => x.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), S = e.useCallback(() => {
    f(), c("idle");
  }, [f]), y = e.useCallback(async () => {
    var x;
    if (l !== "uploading") {
      if (o(null), !b) {
        (x = _.current) == null || x.click();
        return;
      }
      try {
        const $ = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = $, c("overlayOpen");
      } catch ($) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", $), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [l, b]), M = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const x = r.current, $ = p.current;
    if (!x || !$)
      return;
    $.width = x.videoWidth, $.height = x.videoHeight;
    const T = $.getContext("2d");
    T && (T.drawImage(x, 0, 0), f(), c("uploading"), $.toBlob(async (O) => {
      if (!O) {
        c("idle");
        return;
      }
      const Y = new FormData();
      Y.append("photo", O, "capture.jpg"), await a(Y), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, a, f]), B = e.useCallback(async (x) => {
    var O;
    const $ = (O = x.target.files) == null ? void 0 : O[0];
    if (!$) return;
    c("uploading");
    const T = new FormData();
    T.append("photo", $, $.name), await a(T), c("idle"), _.current && (_.current.value = "");
  }, [a]);
  e.useEffect(() => {
    l === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var $;
    if (l !== "overlayOpen") return;
    ($ = k.current) == null || $.focus();
    const x = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = x;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const x = ($) => {
      $.key === "Escape" && S();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [l, S]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((x) => x.stop()), m.current = null);
  }, []);
  const h = se(Yt), g = l === "uploading" ? h["js.uploading"] : h["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const Q = ["tlPhotoCapture__overlayVideo"];
  u && Q.push("tlPhotoCapture__overlayVideo--mirrored");
  const R = ["tlPhotoCapture__mirrorBtn"];
  return u && R.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: y,
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
      onChange: B
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: k,
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
        className: Q.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: R.join(" "),
        onClick: () => s((x) => !x),
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
}, Xt = {
  "js.photoViewer.alt": "Captured photo"
}, qt = ({ controlId: n }) => {
  const t = Z(), a = qe(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
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
  const s = se(Xt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: tt, useRef: Be } = e, Zt = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", u = i.length > 0 && i.every((f) => f.collapsed), s = !u && i.some((f) => f.collapsed), r = u ? !o : o, m = Be(null), p = Be(null), _ = Be(null), k = tt((f, S) => {
    const y = {
      overflow: f.scrolling || "auto"
    };
    return f.collapsed ? u && !r ? y.flex = "1 0 0%" : y.flex = "0 0 auto" : S !== void 0 ? y.flex = `0 0 ${S}px` : f.unit === "%" || s ? y.flex = `${f.size} 0 0%` : y.flex = `0 0 ${f.size}px`, f.minSize > 0 && !f.collapsed && (y.minWidth = o ? f.minSize : void 0, y.minHeight = o ? void 0 : f.minSize), y;
  }, [o, u, s, r]), v = tt((f, S) => {
    f.preventDefault();
    const y = m.current;
    if (!y) return;
    const M = i[S], B = i[S + 1], h = y.querySelectorAll(":scope > .tlSplitPanel__child"), g = [];
    h.forEach((R) => {
      g.push(o ? R.offsetWidth : R.offsetHeight);
    }), _.current = g, p.current = {
      splitterIndex: S,
      startPos: o ? f.clientX : f.clientY,
      startSizeBefore: g[S],
      startSizeAfter: g[S + 1],
      childBefore: M,
      childAfter: B
    };
    const C = (R) => {
      const x = p.current;
      if (!x || !_.current) return;
      const T = (o ? R.clientX : R.clientY) - x.startPos, O = x.childBefore.minSize || 0, Y = x.childAfter.minSize || 0;
      let re = x.startSizeBefore + T, F = x.startSizeAfter - T;
      re < O && (F += re - O, re = O), F < Y && (re += F - Y, F = Y), _.current[x.splitterIndex] = re, _.current[x.splitterIndex + 1] = F;
      const J = y.querySelectorAll(":scope > .tlSplitPanel__child"), P = J[x.splitterIndex], I = J[x.splitterIndex + 1];
      P && (P.style.flex = `0 0 ${re}px`), I && (I.style.flex = `0 0 ${F}px`);
    }, Q = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", Q), document.body.style.cursor = "", document.body.style.userSelect = "", _.current) {
        const R = {};
        i.forEach((x, $) => {
          const T = x.control;
          T != null && T.controlId && _.current && (R[T.controlId] = _.current[$]);
        }), a("updateSizes", { sizes: R });
      }
      _.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", Q), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), b = [];
  return i.forEach((f, S) => {
    if (b.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${S}`,
          className: `tlSplitPanel__child${f.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: k(f)
        },
        /* @__PURE__ */ e.createElement(q, { control: f.control })
      )
    ), c && S < i.length - 1) {
      const y = i[S + 1];
      !f.collapsed && !y.collapsed && b.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${S}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (B) => v(B, S)
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
}, { useCallback: Oe } = e, Qt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Jt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), en = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), rn = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = se(Qt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, r = t.fullLine === !0, m = t.toolbarButtons ?? [], p = i === "MINIMIZED", _ = i === "MAXIMIZED", k = i === "HIDDEN", v = Oe(() => {
    a("toggleMinimize");
  }, [a]), b = Oe(() => {
    a("toggleMaximize");
  }, [a]), f = Oe(() => {
    a("popOut");
  }, [a]);
  if (k)
    return null;
  const S = _ ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}${r ? " tlPanel--fullLine" : ""}`,
      style: S
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((y, M) => /* @__PURE__ */ e.createElement("span", { key: M, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: y }))), o && !_ && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
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
        onClick: f,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ln, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child }))
  );
}, an = ({ controlId: n }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(q, { control: t.child })
  );
}, on = ({ controlId: n }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(q, { control: t.activeChild }));
}, { useCallback: ue, useState: Pe, useEffect: Me, useRef: Ae } = e, sn = {
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
const ke = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, cn = ({ item: n, active: t, collapsed: a, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: a ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  a && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ke, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(ke, { icon: n.icon }),
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
  /* @__PURE__ */ e.createElement(ke, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), dn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(ke, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), mn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), pn = ({ item: n, activeItemId: t, anchorRect: a, onSelect: l, onExecute: c, onClose: i }) => {
  const o = Ae(null);
  Me(() => {
    const r = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [i]), Me(() => {
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
        /* @__PURE__ */ e.createElement(ke, { icon: r.icon }),
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
  flyoutGroupId: k,
  onOpenFlyout: v,
  onCloseFlyout: b
}) => {
  const f = Ae(null), [S, y] = Pe(null), M = ue(() => {
    l ? k === n.id ? b() : (f.current && y(f.current.getBoundingClientRect()), v(n.id)) : o(n.id);
  }, [l, k, n.id, o, v, b]), B = ue((g) => {
    f.current = g, s(g);
  }, [s]), h = l && k === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (h ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: l ? n.label : void 0,
      "aria-expanded": l ? h : t,
      tabIndex: u,
      ref: B,
      onFocus: () => r(n.id)
    },
    /* @__PURE__ */ e.createElement(ke, { icon: n.icon }),
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
    pn,
    {
      item: n,
      activeItemId: a,
      anchorRect: S,
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
      flyoutGroupId: k,
      onOpenFlyout: v,
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
      const k = r ? r.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        fn,
        {
          item: n,
          expanded: k,
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
  const t = Z(), a = ae(), l = se(sn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = Pe(() => {
    const R = /* @__PURE__ */ new Map(), x = ($) => {
      for (const T of $)
        T.type === "group" && (R.set(T.id, T.expanded), x(T.children));
    };
    return x(c), R;
  }), r = ue((R) => {
    s((x) => {
      const $ = new Map(x), T = $.get(R) ?? !1;
      return $.set(R, !T), a("toggleGroup", { itemId: R, expanded: !T }), $;
    });
  }, [a]), m = ue((R) => {
    R !== i && a("selectItem", { itemId: R });
  }, [a, i]), p = ue((R) => {
    a("executeCommand", { itemId: R });
  }, [a]), _ = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [k, v] = Pe(null), b = ue((R) => {
    v(R);
  }, []), f = ue(() => {
    v(null);
  }, []);
  Me(() => {
    o || v(null);
  }, [o]);
  const [S, y] = Pe(() => {
    const R = Ke(c, o, u);
    return R.length > 0 ? R[0].id : "";
  }), M = Ae(/* @__PURE__ */ new Map()), B = ue((R) => (x) => {
    x ? M.current.set(R, x) : M.current.delete(R);
  }, []), h = ue((R) => {
    y(R);
  }, []), g = Ae(0), C = ue((R) => {
    y(R), g.current++;
  }, []);
  Me(() => {
    const R = M.current.get(S);
    R && document.activeElement !== R && R.focus();
  }, [S, g.current]);
  const Q = ue((R) => {
    if (R.key === "Escape" && k !== null) {
      R.preventDefault(), f();
      return;
    }
    const x = Ke(c, o, u);
    if (x.length === 0) return;
    const $ = x.findIndex((O) => O.id === S);
    if ($ < 0) return;
    const T = x[$];
    switch (R.key) {
      case "ArrowDown": {
        R.preventDefault();
        const O = ($ + 1) % x.length;
        C(x[O].id);
        break;
      }
      case "ArrowUp": {
        R.preventDefault();
        const O = ($ - 1 + x.length) % x.length;
        C(x[O].id);
        break;
      }
      case "Home": {
        R.preventDefault(), C(x[0].id);
        break;
      }
      case "End": {
        R.preventDefault(), C(x[x.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        R.preventDefault(), T.type === "nav" ? m(T.id) : T.type === "command" ? p(T.id) : T.type === "group" && (o ? k === T.id ? f() : b(T.id) : r(T.id));
        break;
      }
      case "ArrowRight": {
        T.type === "group" && !o && ((u.get(T.id) ?? !1) || (R.preventDefault(), r(T.id)));
        break;
      }
      case "ArrowLeft": {
        T.type === "group" && !o && (u.get(T.id) ?? !1) && (R.preventDefault(), r(T.id));
        break;
      }
    }
  }, [
    c,
    o,
    u,
    S,
    k,
    C,
    m,
    p,
    r,
    b,
    f
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: Q }, c.map((R) => /* @__PURE__ */ e.createElement(
    mt,
    {
      key: R.id,
      item: R,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: S,
      setItemRef: B,
      onItemFocus: h,
      groupStates: u,
      flyoutGroupId: k,
      onOpenFlyout: b,
      onCloseFlyout: f
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, _n = ({ controlId: n }) => {
  const t = Z(), a = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: u }, o.map((s, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: s })));
}, bn = ({ controlId: n }) => {
  const t = Z(), a = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(q, { key: s, control: u })));
}, vn = ({ controlId: n }) => {
  const t = Z(), a = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(q, { control: o })));
}, En = ({ controlId: n }) => {
  const t = Z(), a = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: u }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(q, { key: r, control: s }))));
}, { useCallback: gn } = e, Cn = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = t.items ?? [], c = gn((i) => {
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
  const t = Z(), a = ae(), l = t.items ?? [], c = t.activeItemId, i = yn((o) => {
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
  const t = Z(), a = ae(), l = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = kn(null), u = nt(() => {
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
    /* @__PURE__ */ e.createElement(q, { control: i })
  ) : null;
}, { useEffect: Nn, useRef: Rn } = e, Tn = ({ controlId: n }) => {
  const a = Z().dialogs ?? [], l = Rn(a.length);
  return Nn(() => {
    a.length < l.current && a.length > 0, l.current = a.length;
  }, [a.length]), a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialogManager" }, a.map((c) => /* @__PURE__ */ e.createElement(q, { key: c.controlId, control: c })));
}, { useCallback: Te, useRef: ge, useState: xe } = e, xn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ln = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Dn = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = se(xn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.minHeight ?? null, s = t.resizable === !0, r = t.child, m = t.actions ?? [], p = t.toolbarButtons ?? [], [_, k] = xe(null), [v, b] = xe(null), [f, S] = xe(null), y = ge(null), [M, B] = xe(!1), h = ge(null), g = ge(null), C = ge(null), Q = ge(null), R = ge(null), x = Te(() => {
    a("close");
  }, [a]), $ = Te((F, J) => {
    J.preventDefault();
    const P = Q.current;
    if (!P) return;
    const I = P.getBoundingClientRect();
    R.current = {
      dir: F,
      startX: J.clientX,
      startY: J.clientY,
      startW: I.width,
      startH: I.height,
      startPos: y.current ? { ...y.current } : { x: I.left, y: I.top }
    };
    const X = (E) => {
      const L = R.current;
      if (!L) return;
      const A = E.clientX - L.startX, z = E.clientY - L.startY;
      let V = L.startW, G = L.startH, le = 0, te = 0;
      L.dir.includes("e") && (V = L.startW + A), L.dir.includes("w") && (V = L.startW - A, le = A), L.dir.includes("s") && (G = L.startH + z), L.dir.includes("n") && (G = L.startH - z, te = z);
      const de = Math.max(200, V), me = Math.max(100, G);
      if (L.dir.includes("w") && de === 200 && (le = L.startW - 200), L.dir.includes("n") && me === 100 && (te = L.startH - 100), g.current = de, C.current = me, k(de), b(me), le !== 0 || te !== 0) {
        const ve = {
          x: L.startPos.x + le,
          y: L.startPos.y + te
        };
        y.current = ve, S(ve);
      }
    }, d = () => {
      document.removeEventListener("mousemove", X), document.removeEventListener("mouseup", d);
      const E = g.current, L = C.current;
      (E != null || L != null) && (a("resize", {
        ...E != null ? { width: Math.round(E) + "px" } : {},
        ...L != null ? { height: Math.round(L) + "px" } : {}
      }), g.current = null, C.current = null, k(null), b(null)), R.current = null;
    };
    document.addEventListener("mousemove", X), document.addEventListener("mouseup", d);
  }, [a]), T = Te((F) => {
    if (F.button !== 0 || F.target.closest("button")) return;
    F.preventDefault();
    const J = Q.current;
    if (!J) return;
    const P = J.getBoundingClientRect(), I = y.current ?? { x: P.left, y: P.top }, X = F.clientX - I.x, d = F.clientY - I.y, E = (A) => {
      const z = window.innerWidth, V = window.innerHeight;
      let G = A.clientX - X, le = A.clientY - d;
      const te = J.offsetWidth, de = J.offsetHeight;
      G + te > z && (G = z - te), le + de > V && (le = V - de), G < 0 && (G = 0), le < 0 && (le = 0);
      const me = { x: G, y: le };
      y.current = me, S(me);
    }, L = () => {
      document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", L);
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", L);
  }, []), O = Te(() => {
    var F, J;
    if (M) {
      const P = h.current;
      P && (S(P.x !== -1 ? { x: P.x, y: P.y } : null), k(P.w), b(P.h)), B(!1);
    } else {
      const P = Q.current, I = P == null ? void 0 : P.getBoundingClientRect();
      h.current = {
        x: ((F = y.current) == null ? void 0 : F.x) ?? (I == null ? void 0 : I.left) ?? -1,
        y: ((J = y.current) == null ? void 0 : J.y) ?? (I == null ? void 0 : I.top) ?? -1,
        w: _ ?? (I == null ? void 0 : I.width) ?? null,
        h: v ?? null
      }, B(!0), S({ x: 0, y: 0 }), k(null), b(null);
    }
  }, [M, _, v]), Y = M ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: _ != null ? _ + "px" : i,
    ...v != null ? { height: v + "px" } : o != null ? { height: o } : {},
    ...u != null && v == null ? { minHeight: u } : {},
    maxHeight: f ? "100vh" : "80vh",
    ...f ? { position: "absolute", left: f.x + "px", top: f.y + "px" } : {}
  }, re = n + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlWindow",
      style: Y,
      ref: Q,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": re
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${M ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: M ? void 0 : T,
        onDoubleClick: O
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: re }, c),
      p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, p.map((F, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: F })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: M ? l["js.window.restore"] : l["js.window.maximize"]
        },
        M ? (
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
          onClick: x,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(q, { control: r })),
    m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m.map((F, J) => /* @__PURE__ */ e.createElement(q, { key: J, control: F }))),
    s && !M && Ln.map((F) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: F,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${F}`,
        onMouseDown: (J) => $(F, J)
      }
    ))
  );
}, { useCallback: In, useEffect: jn } = e, Pn = {
  "js.drawer.close": "Close"
}, Mn = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = se(Pn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, r = In(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(q, { control: s })));
}, { useCallback: An, useEffect: Bn, useState: On } = e, $n = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.duration ?? 5e3, u = t.visible === !0, s = t.generation ?? 0, [r, m] = On(!1), p = An(() => {
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
  const t = Z(), a = ae(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Fn(null), [u, s] = rt({ top: 0, left: 0 }), [r, m] = rt(0), p = i.filter((b) => b.type === "item" && !b.disabled);
  Fe(() => {
    var h, g;
    if (!l || !c) return;
    const b = document.getElementById(c);
    if (!b) return;
    const f = b.getBoundingClientRect(), S = ((h = o.current) == null ? void 0 : h.offsetHeight) ?? 200, y = ((g = o.current) == null ? void 0 : g.offsetWidth) ?? 200;
    let M = f.bottom + 4, B = f.left;
    M + S > window.innerHeight && (M = f.top - S - 4), B + y > window.innerWidth && (B = f.right - y), s({ top: M, left: B }), m(0);
  }, [l, c]);
  const _ = $e(() => {
    a("close");
  }, [a]), k = $e((b) => {
    a("selectItem", { itemId: b });
  }, [a]);
  Fe(() => {
    if (!l) return;
    const b = (f) => {
      o.current && !o.current.contains(f.target) && _();
    };
    return document.addEventListener("mousedown", b), () => document.removeEventListener("mousedown", b);
  }, [l, _]);
  const v = $e((b) => {
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
      f && k(f.id);
    }
  }, [_, k, p, r]);
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
      onKeyDown: v
    },
    i.map((b, f) => {
      if (b.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: f, className: "tlMenu__separator" });
      const y = p.indexOf(b) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: b.id,
          type: "button",
          className: "tlMenu__item" + (y ? " tlMenu__item--focused" : "") + (b.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: b.disabled,
          tabIndex: y ? 0 : -1,
          onClick: () => k(b.id)
        },
        b.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + b.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, b.label)
      );
    })
  ) : null;
}, zn = ({ controlId: n }) => {
  const t = Z(), a = t.header, l = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: c })), /* @__PURE__ */ e.createElement(q, { control: i }), o && /* @__PURE__ */ e.createElement(q, { control: o }));
}, Wn = () => {
  const t = Z().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Un = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, at = 50, Vn = () => {
  const n = Z(), t = ae(), a = se(Un), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, u = n.selectionMode ?? "single", s = n.selectedCount ?? 0, r = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((N) => N.sortPriority && N.sortPriority > 0).length,
    [l]
  ), _ = u === "multi", k = 40, v = 20, b = e.useRef(null), f = e.useRef(null), S = e.useRef(null), [y, M] = e.useState({}), B = e.useRef(null), h = e.useRef(!1), g = e.useRef(null), [C, Q] = e.useState(null), [R, x] = e.useState(null);
  e.useEffect(() => {
    B.current || M({});
  }, [l]);
  const $ = e.useCallback((N) => y[N.name] ?? N.width, [y]), T = e.useMemo(() => {
    const N = [];
    let D = _ && r > 0 ? k : 0;
    for (let K = 0; K < r && K < l.length; K++)
      N.push(D), D += $(l[K]);
    return N;
  }, [l, r, _, k, $]), O = c * o, Y = e.useRef(null), re = e.useCallback((N, D, K) => {
    K.preventDefault(), K.stopPropagation(), B.current = { column: N, startX: K.clientX, startWidth: D };
    let ee = K.clientX, w = 0;
    const j = () => {
      const oe = B.current;
      if (!oe) return;
      const pe = Math.max(at, oe.startWidth + (ee - oe.startX) + w);
      M((Ee) => ({ ...Ee, [oe.column]: pe }));
    }, W = () => {
      const oe = f.current, pe = b.current;
      if (!oe || !B.current) return;
      const Ee = oe.getBoundingClientRect(), Qe = 40, Je = 8, vt = oe.scrollLeft;
      ee > Ee.right - Qe ? oe.scrollLeft += Je : ee < Ee.left + Qe && (oe.scrollLeft = Math.max(0, oe.scrollLeft - Je));
      const et = oe.scrollLeft - vt;
      et !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), w += et, j()), Y.current = requestAnimationFrame(W);
    };
    Y.current = requestAnimationFrame(W);
    const ne = (oe) => {
      ee = oe.clientX, j();
    }, he = (oe) => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", he), Y.current !== null && (cancelAnimationFrame(Y.current), Y.current = null);
      const pe = B.current;
      if (pe) {
        const Ee = Math.max(at, pe.startWidth + (oe.clientX - pe.startX) + w);
        t("columnResize", { column: pe.column, width: Ee }), B.current = null, h.current = !0, requestAnimationFrame(() => {
          h.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", he);
  }, [t]), F = e.useCallback(() => {
    b.current && f.current && (b.current.scrollLeft = f.current.scrollLeft), S.current !== null && clearTimeout(S.current), S.current = window.setTimeout(() => {
      const N = f.current;
      if (!N) return;
      const D = N.scrollTop, K = Math.ceil(N.clientHeight / o), ee = Math.floor(D / o);
      t("scroll", { start: ee, count: K });
    }, 80);
  }, [t, o]), J = e.useCallback((N, D, K) => {
    if (h.current) return;
    let ee;
    !D || D === "desc" ? ee = "asc" : ee = "desc";
    const w = K.shiftKey ? "add" : "replace";
    t("sort", { column: N, direction: ee, mode: w });
  }, [t]), P = e.useCallback((N, D) => {
    g.current = N, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", N);
  }, []), I = e.useCallback((N, D) => {
    if (!g.current || g.current === N) {
      Q(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const K = D.currentTarget.getBoundingClientRect(), ee = D.clientX < K.left + K.width / 2 ? "left" : "right";
    Q({ column: N, side: ee });
  }, []), X = e.useCallback((N) => {
    N.preventDefault(), N.stopPropagation();
    const D = g.current;
    if (!D || !C) {
      g.current = null, Q(null);
      return;
    }
    let K = l.findIndex((w) => w.name === C.column);
    if (K < 0) {
      g.current = null, Q(null);
      return;
    }
    const ee = l.findIndex((w) => w.name === D);
    C.side === "right" && K++, ee < K && K--, t("columnReorder", { column: D, targetIndex: K }), g.current = null, Q(null);
  }, [l, C, t]), d = e.useCallback(() => {
    g.current = null, Q(null);
  }, []), E = e.useCallback((N, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: N,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), L = e.useCallback((N, D) => {
    D.stopPropagation(), t("select", { rowIndex: N, ctrlKey: !0, shiftKey: !1 });
  }, [t]), A = e.useCallback(() => {
    const N = s === c && c > 0;
    t("selectAll", { selected: !N });
  }, [t, s, c]), z = e.useCallback((N, D, K) => {
    K.stopPropagation(), t("expand", { rowIndex: N, expanded: D });
  }, [t]), V = e.useCallback((N, D) => {
    D.preventDefault(), x({ x: D.clientX, y: D.clientY, colIdx: N });
  }, []), G = e.useCallback(() => {
    R && (t("setFrozenColumnCount", { count: R.colIdx + 1 }), x(null));
  }, [R, t]), le = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), x(null);
  }, [t]);
  e.useEffect(() => {
    if (!R) return;
    const N = () => x(null), D = (K) => {
      K.key === "Escape" && x(null);
    };
    return document.addEventListener("mousedown", N), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", N), document.removeEventListener("keydown", D);
    };
  }, [R]);
  const te = l.reduce((N, D) => N + $(D), 0) + (_ ? k : 0), de = s === c && c > 0, me = s > 0 && s < c, ve = e.useCallback((N) => {
    N && (N.indeterminate = me);
  }, [me]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (N) => {
        if (!g.current) return;
        N.preventDefault();
        const D = f.current, K = b.current;
        if (!D) return;
        const ee = D.getBoundingClientRect(), w = 40, j = 8;
        N.clientX < ee.left + w ? D.scrollLeft = Math.max(0, D.scrollLeft - j) : N.clientX > ee.right - w && (D.scrollLeft += j), K && (K.scrollLeft = D.scrollLeft);
      },
      onDrop: X
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: b }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: te } }, _ && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: k,
          minWidth: k,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (N) => {
          g.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== g.current && Q({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: ve,
          className: "tlTableView__checkbox",
          checked: de,
          onChange: A
        }
      )
    ), l.map((N, D) => {
      const K = $(N);
      l.length - 1;
      let ee = "tlTableView__headerCell";
      N.sortable && (ee += " tlTableView__headerCell--sortable"), C && C.column === N.name && (ee += " tlTableView__headerCell--dragOver-" + C.side);
      const w = D < r, j = D === r - 1;
      return w && (ee += " tlTableView__headerCell--frozen"), j && (ee += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.name,
          className: ee,
          style: {
            width: K,
            minWidth: K,
            position: w ? "sticky" : "relative",
            ...w ? { left: T[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: N.sortable ? (W) => J(N.name, N.sortDirection, W) : void 0,
          onContextMenu: (W) => V(D, W),
          onDragStart: (W) => P(N.name, W),
          onDragOver: (W) => I(N.name, W),
          onDrop: X,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, N.label),
        N.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, N.sortDirection === "asc" ? "▲" : "▼", p > 1 && N.sortPriority != null && N.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, N.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (W) => re(N.name, K, W)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (N) => {
          if (g.current && l.length > 0) {
            const D = l[l.length - 1];
            D.name !== g.current && (N.preventDefault(), N.dataTransfer.dropEffect = "move", Q({ column: D.name, side: "right" }));
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
        onScroll: F
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", width: te } }, i.map((N) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: N.id,
          className: "tlTableView__row" + (N.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: N.index * o,
            height: o,
            width: te
          },
          onClick: (D) => E(N.index, D)
        },
        _ && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: k,
              minWidth: k,
              ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: N.selected,
              onChange: () => {
              },
              onClick: (D) => L(N.index, D),
              tabIndex: -1
            }
          )
        ),
        l.map((D, K) => {
          const ee = $(D), w = K === l.length - 1, j = K < r, W = K === r - 1;
          let ne = "tlTableView__cell";
          j && (ne += " tlTableView__cell--frozen"), W && (ne += " tlTableView__cell--frozenLast");
          const he = m && K === 0, oe = N.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: ne,
              style: {
                ...w && !j ? { flex: "1 0 auto", minWidth: ee } : { width: ee, minWidth: ee },
                ...j ? { position: "sticky", left: T[K], zIndex: 2 } : {}
              }
            },
            he ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * v } }, N.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => z(N.index, !N.expanded, pe)
              },
              N.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(q, { control: N.cells[D.name] })) : /* @__PURE__ */ e.createElement(q, { control: N.cells[D.name] })
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
        onMouseDown: (N) => N.stopPropagation()
      },
      R.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: G }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: le }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Kn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, pt = e.createContext(Kn), { useMemo: Yn, useRef: Gn, useState: Xn, useEffect: qn } = e, Zn = 320, Qn = ({ controlId: n }) => {
  const t = Z(), a = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Gn(null), [s, r] = Xn(
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
  }, k = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(pt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: k, style: _, ref: u }, i.map((v, b) => /* @__PURE__ */ e.createElement(q, { key: b, control: v }))));
}, { useCallback: Jn } = e, el = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, tl = ({ controlId: n }) => {
  const t = Z(), a = ae(), l = se(el), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, _ = Jn(() => {
    a("toggleCollapse");
  }, [a]), k = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    r ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: k }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((v, b) => /* @__PURE__ */ e.createElement(q, { key: b, control: v })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((v, b) => /* @__PURE__ */ e.createElement(q, { key: b, control: v }))));
}, { useContext: nl, useState: ll, useCallback: rl } = e, al = ({ controlId: n }) => {
  const t = Z(), a = nl(pt), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, r = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, _ = t.field, k = a.readOnly, [v, b] = ll(!1), f = rl(() => b((B) => !B), []);
  if (!p) return null;
  const S = i != null, y = o != null && o.length > 0, M = [
    "tlFormField",
    `tlFormField--${r}`,
    k ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    S ? "tlFormField--error" : "",
    !S && y ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: M }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !k && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !k && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: _ })), !k && S && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !k && !S && y && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((B, h) => /* @__PURE__ */ e.createElement("div", { key: h, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, B)))), !k && u && v && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, ol = () => {
  const n = Z(), t = ae(), a = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, u = n.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((_) => {
    _.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, sl = 20, cl = () => {
  const n = Z(), t = ae(), a = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, u = n.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((h, g) => {
    t(g ? "collapse" : "expand", { nodeId: h });
  }, [t]), _ = e.useCallback((h, g) => {
    t("select", {
      nodeId: h,
      ctrlKey: g.ctrlKey || g.metaKey,
      shiftKey: g.shiftKey
    });
  }, [t]), k = e.useCallback((h, g) => {
    g.preventDefault(), t("contextMenu", { nodeId: h, x: g.clientX, y: g.clientY });
  }, [t]), v = e.useRef(null), b = e.useCallback((h, g) => {
    const C = g.getBoundingClientRect(), Q = h.clientY - C.top, R = C.height / 3;
    return Q < R ? "above" : Q > R * 2 ? "below" : "within";
  }, []), f = e.useCallback((h, g) => {
    g.dataTransfer.effectAllowed = "move", g.dataTransfer.setData("text/plain", h);
  }, []), S = e.useCallback((h, g) => {
    g.preventDefault(), g.dataTransfer.dropEffect = "move";
    const C = b(g, g.currentTarget);
    v.current != null && window.clearTimeout(v.current), v.current = window.setTimeout(() => {
      t("dragOver", { nodeId: h, position: C }), v.current = null;
    }, 50);
  }, [t, b]), y = e.useCallback((h, g) => {
    g.preventDefault(), v.current != null && (window.clearTimeout(v.current), v.current = null);
    const C = b(g, g.currentTarget);
    t("drop", { nodeId: h, position: C });
  }, [t, b]), M = e.useCallback(() => {
    v.current != null && (window.clearTimeout(v.current), v.current = null), t("dragEnd");
  }, [t]), B = e.useCallback((h) => {
    if (a.length === 0) return;
    let g = s;
    switch (h.key) {
      case "ArrowDown":
        h.preventDefault(), g = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        h.preventDefault(), g = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (h.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (g = s + 1);
        }
        break;
      case "ArrowLeft":
        if (h.preventDefault(), s >= 0 && s < a.length) {
          const C = a[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const Q = C.depth;
            for (let R = s - 1; R >= 0; R--)
              if (a[R].depth < Q) {
                g = R;
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
        h.preventDefault(), g = 0;
        break;
      case "End":
        h.preventDefault(), g = a.length - 1;
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
      onKeyDown: B
    },
    a.map((h, g) => /* @__PURE__ */ e.createElement(
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
          g === s ? "tlTreeView__node--focused" : "",
          o === h.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === h.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === h.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: h.depth * sl },
        draggable: c,
        onClick: (C) => _(h.id, C),
        onContextMenu: (C) => k(h.id, C),
        onDragStart: (C) => f(h.id, C),
        onDragOver: i ? (C) => S(h.id, C) : void 0,
        onDrop: i ? (C) => y(h.id, C) : void 0,
        onDragEnd: M
      },
      h.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (C) => {
            C.stopPropagation(), p(h.id, h.expanded);
          },
          tabIndex: -1,
          "aria-label": h.expanded ? "Collapse" : "Expand"
        },
        h.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: h.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(q, { control: h.content }))
    ))
  );
};
var He = { exports: {} }, ce = {}, ze = { exports: {} }, U = {};
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
  if (ot) return U;
  ot = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), _ = Symbol.iterator;
  function k(d) {
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
  function S(d, E, L) {
    this.props = d, this.context = E, this.refs = f, this.updater = L || v;
  }
  S.prototype.isReactComponent = {}, S.prototype.setState = function(d, E) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, E, "setState");
  }, S.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function y() {
  }
  y.prototype = S.prototype;
  function M(d, E, L) {
    this.props = d, this.context = E, this.refs = f, this.updater = L || v;
  }
  var B = M.prototype = new y();
  B.constructor = M, b(B, S.prototype), B.isPureReactComponent = !0;
  var h = Array.isArray;
  function g() {
  }
  var C = { H: null, A: null, T: null, S: null }, Q = Object.prototype.hasOwnProperty;
  function R(d, E, L) {
    var A = L.ref;
    return {
      $$typeof: n,
      type: d,
      key: E,
      ref: A !== void 0 ? A : null,
      props: L
    };
  }
  function x(d, E) {
    return R(d.type, E, d.props);
  }
  function $(d) {
    return typeof d == "object" && d !== null && d.$$typeof === n;
  }
  function T(d) {
    var E = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(L) {
      return E[L];
    });
  }
  var O = /\/+/g;
  function Y(d, E) {
    return typeof d == "object" && d !== null && d.key != null ? T("" + d.key) : E.toString(36);
  }
  function re(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(g, g) : (d.status = "pending", d.then(
          function(E) {
            d.status === "pending" && (d.status = "fulfilled", d.value = E);
          },
          function(E) {
            d.status === "pending" && (d.status = "rejected", d.reason = E);
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
  function F(d, E, L, A, z) {
    var V = typeof d;
    (V === "undefined" || V === "boolean") && (d = null);
    var G = !1;
    if (d === null) G = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          G = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case n:
            case t:
              G = !0;
              break;
            case m:
              return G = d._init, F(
                G(d._payload),
                E,
                L,
                A,
                z
              );
          }
      }
    if (G)
      return z = z(d), G = A === "" ? "." + Y(d, 0) : A, h(z) ? (L = "", G != null && (L = G.replace(O, "$&/") + "/"), F(z, E, L, "", function(de) {
        return de;
      })) : z != null && ($(z) && (z = x(
        z,
        L + (z.key == null || d && d.key === z.key ? "" : ("" + z.key).replace(
          O,
          "$&/"
        ) + "/") + G
      )), E.push(z)), 1;
    G = 0;
    var le = A === "" ? "." : A + ":";
    if (h(d))
      for (var te = 0; te < d.length; te++)
        A = d[te], V = le + Y(A, te), G += F(
          A,
          E,
          L,
          V,
          z
        );
    else if (te = k(d), typeof te == "function")
      for (d = te.call(d), te = 0; !(A = d.next()).done; )
        A = A.value, V = le + Y(A, te++), G += F(
          A,
          E,
          L,
          V,
          z
        );
    else if (V === "object") {
      if (typeof d.then == "function")
        return F(
          re(d),
          E,
          L,
          A,
          z
        );
      throw E = String(d), Error(
        "Objects are not valid as a React child (found: " + (E === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : E) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return G;
  }
  function J(d, E, L) {
    if (d == null) return d;
    var A = [], z = 0;
    return F(d, A, "", "", function(V) {
      return E.call(L, V, z++);
    }), A;
  }
  function P(d) {
    if (d._status === -1) {
      var E = d._result;
      E = E(), E.then(
        function(L) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = L);
        },
        function(L) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = L);
        }
      ), d._status === -1 && (d._status = 0, d._result = E);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var I = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var E = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(E)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, X = {
    map: J,
    forEach: function(d, E, L) {
      J(
        d,
        function() {
          E.apply(this, arguments);
        },
        L
      );
    },
    count: function(d) {
      var E = 0;
      return J(d, function() {
        E++;
      }), E;
    },
    toArray: function(d) {
      return J(d, function(E) {
        return E;
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
  return U.Activity = p, U.Children = X, U.Component = S, U.Fragment = a, U.Profiler = c, U.PureComponent = M, U.StrictMode = l, U.Suspense = s, U.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, U.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return C.H.useMemoCache(d);
    }
  }, U.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, U.cacheSignal = function() {
    return null;
  }, U.cloneElement = function(d, E, L) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var A = b({}, d.props), z = d.key;
    if (E != null)
      for (V in E.key !== void 0 && (z = "" + E.key), E)
        !Q.call(E, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && E.ref === void 0 || (A[V] = E[V]);
    var V = arguments.length - 2;
    if (V === 1) A.children = L;
    else if (1 < V) {
      for (var G = Array(V), le = 0; le < V; le++)
        G[le] = arguments[le + 2];
      A.children = G;
    }
    return R(d.type, z, A);
  }, U.createContext = function(d) {
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
  }, U.createElement = function(d, E, L) {
    var A, z = {}, V = null;
    if (E != null)
      for (A in E.key !== void 0 && (V = "" + E.key), E)
        Q.call(E, A) && A !== "key" && A !== "__self" && A !== "__source" && (z[A] = E[A]);
    var G = arguments.length - 2;
    if (G === 1) z.children = L;
    else if (1 < G) {
      for (var le = Array(G), te = 0; te < G; te++)
        le[te] = arguments[te + 2];
      z.children = le;
    }
    if (d && d.defaultProps)
      for (A in G = d.defaultProps, G)
        z[A] === void 0 && (z[A] = G[A]);
    return R(d, V, z);
  }, U.createRef = function() {
    return { current: null };
  }, U.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, U.isValidElement = $, U.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: P
    };
  }, U.memo = function(d, E) {
    return {
      $$typeof: r,
      type: d,
      compare: E === void 0 ? null : E
    };
  }, U.startTransition = function(d) {
    var E = C.T, L = {};
    C.T = L;
    try {
      var A = d(), z = C.S;
      z !== null && z(L, A), typeof A == "object" && A !== null && typeof A.then == "function" && A.then(g, I);
    } catch (V) {
      I(V);
    } finally {
      E !== null && L.types !== null && (E.types = L.types), C.T = E;
    }
  }, U.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, U.use = function(d) {
    return C.H.use(d);
  }, U.useActionState = function(d, E, L) {
    return C.H.useActionState(d, E, L);
  }, U.useCallback = function(d, E) {
    return C.H.useCallback(d, E);
  }, U.useContext = function(d) {
    return C.H.useContext(d);
  }, U.useDebugValue = function() {
  }, U.useDeferredValue = function(d, E) {
    return C.H.useDeferredValue(d, E);
  }, U.useEffect = function(d, E) {
    return C.H.useEffect(d, E);
  }, U.useEffectEvent = function(d) {
    return C.H.useEffectEvent(d);
  }, U.useId = function() {
    return C.H.useId();
  }, U.useImperativeHandle = function(d, E, L) {
    return C.H.useImperativeHandle(d, E, L);
  }, U.useInsertionEffect = function(d, E) {
    return C.H.useInsertionEffect(d, E);
  }, U.useLayoutEffect = function(d, E) {
    return C.H.useLayoutEffect(d, E);
  }, U.useMemo = function(d, E) {
    return C.H.useMemo(d, E);
  }, U.useOptimistic = function(d, E) {
    return C.H.useOptimistic(d, E);
  }, U.useReducer = function(d, E, L) {
    return C.H.useReducer(d, E, L);
  }, U.useRef = function(d) {
    return C.H.useRef(d);
  }, U.useState = function(d) {
    return C.H.useState(d);
  }, U.useSyncExternalStore = function(d, E, L) {
    return C.H.useSyncExternalStore(
      d,
      E,
      L
    );
  }, U.useTransition = function() {
    return C.H.useTransition();
  }, U.version = "19.2.4", U;
}
var st;
function ul() {
  return st || (st = 1, ze.exports = il()), ze.exports;
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
  if (ct) return ce;
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
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, ce.createPortal = function(s, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return i(s, r, null, m);
  }, ce.flushSync = function(s) {
    var r = o.T, m = l.p;
    try {
      if (o.T = null, l.p = 2, s) return s();
    } finally {
      o.T = r, l.p = m, l.d.f();
    }
  }, ce.preconnect = function(s, r) {
    typeof s == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, l.d.C(s, r));
  }, ce.prefetchDNS = function(s) {
    typeof s == "string" && l.d.D(s);
  }, ce.preinit = function(s, r) {
    if (typeof s == "string" && r && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin), _ = typeof r.integrity == "string" ? r.integrity : void 0, k = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? l.d.S(
        s,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: _,
          fetchPriority: k
        }
      ) : m === "script" && l.d.X(s, {
        crossOrigin: p,
        integrity: _,
        fetchPriority: k,
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
          l.d.M(s, {
            crossOrigin: m,
            integrity: typeof r.integrity == "string" ? r.integrity : void 0,
            nonce: typeof r.nonce == "string" ? r.nonce : void 0
          });
        }
      } else r == null && l.d.M(s);
  }, ce.preload = function(s, r) {
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
  }, ce.preloadModule = function(s, r) {
    if (typeof s == "string")
      if (r) {
        var m = u(r.as, r.crossOrigin);
        l.d.m(s, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else l.d.m(s);
  }, ce.requestFormReset = function(s) {
    l.d.r(s);
  }, ce.unstable_batchedUpdates = function(s, r) {
    return s(r);
  }, ce.useFormState = function(s, r, m) {
    return o.H.useFormState(s, r, m);
  }, ce.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
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
const { useState: _e, useCallback: ie, useRef: Se, useEffect: Ce, useMemo: Ye } = e;
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
  const m = ie(
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
  const o = ie(() => l(n.value), [l, n.value]), u = Ye(() => {
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
  const a = ae(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", _ = i && c && !u && s, k = se({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), v = k["js.dropdownSelect.nothingFound"], b = ie(
    (w) => k["js.dropdownSelect.removeChip"].replace("{0}", w),
    [k]
  ), [f, S] = _e(!1), [y, M] = _e(""), [B, h] = _e(-1), [g, C] = _e(!1), [Q, R] = _e({}), [x, $] = _e(null), [T, O] = _e(null), [Y, re] = _e(null), F = Se(null), J = Se(null), P = Se(null), I = Se(l);
  I.current = l;
  const X = Se(-1), d = Ye(
    () => new Set(l.map((w) => w.value)),
    [l]
  ), E = Ye(() => {
    let w = m.filter((j) => !d.has(j.value));
    if (y) {
      const j = y.toLowerCase();
      w = w.filter((W) => W.label.toLowerCase().includes(j));
    }
    return w;
  }, [m, d, y]);
  Ce(() => {
    y && E.length === 1 ? h(0) : h(-1);
  }, [E.length, y]), Ce(() => {
    f && r && J.current && J.current.focus();
  }, [f, r, l]), Ce(() => {
    var W, ne;
    if (X.current < 0) return;
    const w = X.current;
    X.current = -1;
    const j = (W = F.current) == null ? void 0 : W.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    j && j.length > 0 ? j[Math.min(w, j.length - 1)].focus() : (ne = F.current) == null || ne.focus();
  }, [l]), Ce(() => {
    if (!f) return;
    const w = (j) => {
      F.current && !F.current.contains(j.target) && P.current && !P.current.contains(j.target) && (S(!1), M(""));
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [f]), Ce(() => {
    if (!f || !F.current) return;
    const w = F.current.getBoundingClientRect(), j = window.innerHeight - w.bottom, ne = j < 300 && w.top > j;
    R({
      left: w.left,
      width: w.width,
      ...ne ? { bottom: window.innerHeight - w.top } : { top: w.bottom }
    });
  }, [f]);
  const L = ie(async () => {
    if (!(u || !s) && (S(!0), M(""), h(-1), C(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, s, r, a]), A = ie(() => {
    var w;
    S(!1), M(""), h(-1), (w = F.current) == null || w.focus();
  }, []), z = ie(
    (w) => {
      let j;
      if (c) {
        const W = m.find((ne) => ne.value === w);
        if (W)
          j = [...I.current, W];
        else
          return;
      } else {
        const W = m.find((ne) => ne.value === w);
        if (W)
          j = [W];
        else
          return;
      }
      I.current = j, a("valueChanged", { value: j.map((W) => W.value) }), c ? (M(""), h(-1)) : A();
    },
    [c, m, a, A]
  ), V = ie(
    (w) => {
      X.current = I.current.findIndex((W) => W.value === w);
      const j = I.current.filter((W) => W.value !== w);
      I.current = j, a("valueChanged", { value: j.map((W) => W.value) });
    },
    [a]
  ), G = ie(
    (w) => {
      w.stopPropagation(), a("valueChanged", { value: [] }), A();
    },
    [a, A]
  ), le = ie((w) => {
    M(w.target.value);
  }, []), te = ie(
    (w) => {
      if (!f) {
        if (w.key === "ArrowDown" || w.key === "ArrowUp" || w.key === "Enter" || w.key === " ") {
          if (w.target.tagName === "BUTTON") return;
          w.preventDefault(), w.stopPropagation(), L();
        }
        return;
      }
      switch (w.key) {
        case "ArrowDown":
          w.preventDefault(), w.stopPropagation(), h(
            (j) => j < E.length - 1 ? j + 1 : 0
          );
          break;
        case "ArrowUp":
          w.preventDefault(), w.stopPropagation(), h(
            (j) => j > 0 ? j - 1 : E.length - 1
          );
          break;
        case "Enter":
          w.preventDefault(), w.stopPropagation(), B >= 0 && B < E.length && z(E[B].value);
          break;
        case "Escape":
          w.preventDefault(), w.stopPropagation(), A();
          break;
        case "Tab":
          A();
          break;
        case "Backspace":
          y === "" && c && l.length > 0 && V(l[l.length - 1].value);
          break;
      }
    },
    [
      f,
      L,
      A,
      E,
      B,
      z,
      y,
      c,
      l,
      V
    ]
  ), de = ie(
    async (w) => {
      w.preventDefault(), C(!1);
      try {
        await a("loadOptions");
      } catch {
        C(!0);
      }
    },
    [a]
  ), me = ie(
    (w, j) => {
      $(w), j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", String(w));
    },
    []
  ), ve = ie(
    (w, j) => {
      if (j.preventDefault(), j.dataTransfer.dropEffect = "move", x === null || x === w) {
        O(null), re(null);
        return;
      }
      const W = j.currentTarget.getBoundingClientRect(), ne = W.left + W.width / 2, he = j.clientX < ne ? "before" : "after";
      O(w), re(he);
    },
    [x]
  ), N = ie(
    (w) => {
      if (w.preventDefault(), x === null || T === null || Y === null || x === T) return;
      const j = [...I.current], [W] = j.splice(x, 1);
      let ne = T;
      x < T ? ne = Y === "before" ? ne - 1 : ne : ne = Y === "before" ? ne : ne + 1, j.splice(ne, 0, W), I.current = j, a("valueChanged", { value: j.map((he) => he.value) }), $(null), O(null), re(null);
    },
    [x, T, Y, a]
  ), D = ie(() => {
    $(null), O(null), re(null);
  }, []);
  if (Ce(() => {
    if (B < 0 || !P.current) return;
    const w = P.current.querySelector(
      `[id="${n}-opt-${B}"]`
    );
    w && w.scrollIntoView({ block: "nearest" });
  }, [B, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((w) => /* @__PURE__ */ e.createElement("span", { key: w.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ze, { image: w.image }), /* @__PURE__ */ e.createElement("span", null, w.label))));
  const K = !o && l.length > 0 && !u, ee = f ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: P,
      className: "tlDropdownSelect__dropdown",
      style: Q
    },
    (r || g) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: y,
        onChange: le,
        onKeyDown: te,
        placeholder: k["js.dropdownSelect.filterPlaceholder"],
        "aria-label": k["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": B >= 0 ? `${n}-opt-${B}` : void 0,
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
      g && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: de }, k["js.dropdownSelect.error"])),
      r && E.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, v),
      r && E.map((w, j) => /* @__PURE__ */ e.createElement(
        hl,
        {
          key: w.value,
          id: `${n}-opt-${j}`,
          option: w,
          highlighted: j === B,
          searchTerm: y,
          onSelect: z,
          onMouseEnter: () => h(j)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: F,
      className: "tlDropdownSelect" + (f ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": f,
      "aria-haspopup": "listbox",
      "aria-owns": f ? `${n}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: f ? void 0 : L,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((w, j) => {
      let W = "";
      return x === j ? W = "tlDropdownSelect__chip--dragging" : T === j && Y === "before" ? W = "tlDropdownSelect__chip--dropBefore" : T === j && Y === "after" && (W = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        fl,
        {
          key: w.value,
          option: w,
          removable: !u && (c || !o),
          onRemove: V,
          removeLabel: b(w.label),
          draggable: _,
          onDragStart: _ ? (ne) => me(j, ne) : void 0,
          onDragOver: _ ? (ne) => ve(j, ne) : void 0,
          onDrop: _ ? N : void 0,
          onDragEnd: _ ? D : void 0,
          dragClassName: _ ? W : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, K && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: G,
        "aria-label": k["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, f ? "▲" : "▼"))
  ), ee && pl.createPortal(ee, document.body));
}, { useCallback: We, useRef: bl } = e, ft = "application/x-tl-color", vl = ({
  colors: n,
  columns: t,
  onSelect: a,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = bl(null), u = We(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = We((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = We(
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
const { useCallback: ye, useRef: ut } = e, yl = ({ color: n, onColorChange: t }) => {
  const [a, l, c] = Cl(n), i = ut(null), o = ut(null), u = ye(
    (v, b) => {
      var M;
      const f = (M = i.current) == null ? void 0 : M.getBoundingClientRect();
      if (!f) return;
      const S = Math.max(0, Math.min(1, (v - f.left) / f.width)), y = Math.max(0, Math.min(1, 1 - (b - f.top) / f.height));
      t(Ue(a, S, y));
    },
    [a, t]
  ), s = ye(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), u(v.clientX, v.clientY);
    },
    [u]
  ), r = ye(
    (v) => {
      v.buttons !== 0 && u(v.clientX, v.clientY);
    },
    [u]
  ), m = ye(
    (v) => {
      var y;
      const b = (y = o.current) == null ? void 0 : y.getBoundingClientRect();
      if (!b) return;
      const S = Math.max(0, Math.min(1, (v - b.top) / b.height)) * 360;
      t(Ue(S, l, c));
    },
    [l, c, t]
  ), p = ye(
    (v) => {
      v.preventDefault(), v.target.setPointerCapture(v.pointerId), m(v.clientY);
    },
    [m]
  ), _ = ye(
    (v) => {
      v.buttons !== 0 && m(v.clientY);
    },
    [m]
  ), k = Ue(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: k },
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
}, { useState: Le, useCallback: fe, useEffect: Ve, useRef: Sl, useLayoutEffect: Nl } = e, Rl = ({
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
  const [r, m] = Le("palette"), [p, _] = Le(t), k = Sl(null), v = se(kl), [b, f] = Le(null);
  Nl(() => {
    if (!n.current || !k.current) return;
    const P = n.current.getBoundingClientRect(), I = k.current.getBoundingClientRect();
    let X = P.bottom + 4, d = P.left;
    X + I.height > window.innerHeight && (X = P.top - I.height - 4), d + I.width > window.innerWidth && (d = Math.max(0, P.right - I.width)), f({ top: X, left: d });
  }, [n]);
  const S = p != null, [y, M, B] = S ? _t(p) : [0, 0, 0], [h, g] = Le((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ve(() => {
    g((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ve(() => {
    const P = (I) => {
      I.key === "Escape" && u();
    };
    return document.addEventListener("keydown", P), () => document.removeEventListener("keydown", P);
  }, [u]), Ve(() => {
    const P = (X) => {
      k.current && !k.current.contains(X.target) && u();
    }, I = setTimeout(() => document.addEventListener("mousedown", P), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", P);
    };
  }, [u]);
  const C = fe(
    (P) => (I) => {
      const X = parseInt(I.target.value, 10);
      if (isNaN(X)) return;
      const d = ht(X);
      _(bt(P === "r" ? d : y, P === "g" ? d : M, P === "b" ? d : B));
    },
    [y, M, B]
  ), Q = fe(
    (P) => {
      if (p != null) {
        P.dataTransfer.setData(ft, p.toUpperCase()), P.dataTransfer.effectAllowed = "move";
        const I = document.createElement("div");
        I.style.width = "33px", I.style.height = "33px", I.style.backgroundColor = p, I.style.borderRadius = "3px", I.style.border = "1px solid rgba(0,0,0,0.1)", I.style.position = "absolute", I.style.top = "-9999px", document.body.appendChild(I), P.dataTransfer.setDragImage(I, 16, 16), requestAnimationFrame(() => document.body.removeChild(I));
      }
    },
    [p]
  ), R = fe((P) => {
    const I = P.target.value;
    g(I), Ge(I) && _(I);
  }, []), x = fe(() => {
    _(null);
  }, []), $ = fe((P) => {
    _(P);
  }, []), T = fe(
    (P) => {
      o(P);
    },
    [o]
  ), O = fe(
    (P, I) => {
      const X = [...a], d = X[P];
      X[P] = X[I], X[I] = d, s(X);
    },
    [a, s]
  ), Y = fe(
    (P, I) => {
      const X = [...a];
      X[P] = I, s(X);
    },
    [a, s]
  ), re = fe(() => {
    s([...c]);
  }, [c, s]), F = fe(
    (P) => {
      if (wl(a, P)) return;
      const I = a.indexOf(null);
      if (I < 0) return;
      const X = [...a];
      X[I] = P.toUpperCase(), s(X);
    },
    [a, s]
  ), J = fe(() => {
    p != null && F(p), o(p);
  }, [p, o, F]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: k,
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
        onSelect: $,
        onConfirm: T,
        onSwap: O,
        onReplace: Y
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: re }, v["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(yl, { color: p ?? "#000000", onColorChange: _ }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, v["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: S ? Q : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? y : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? M : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: S ? B : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, v["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (h !== "" && !Ge(h) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: h,
        onChange: R
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: x }, v["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, v["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, v["js.colorInput.ok"]))
  );
}, Tl = { "js.colorInput.chooseColor": "Choose color" }, { useState: xl, useCallback: De, useRef: Ll } = e, Dl = ({ controlId: n, state: t }) => {
  const a = ae(), l = se(Tl), [c, i] = xl(!1), o = Ll(null), u = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, _ = De(() => {
    s && i(!0);
  }, [s]), k = De(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), v = De(() => {
    i(!1);
  }, []), b = De(
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
    Rl,
    {
      anchorRef: o,
      currentColor: u,
      palette: r,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: k,
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
}, { useState: Ne, useCallback: be, useEffect: Ie, useRef: dt, useLayoutEffect: Il, useMemo: jl } = e, Pl = {
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
  const u = se(Pl), [s, r] = Ne("simple"), [m, p] = Ne(""), [_, k] = Ne(t ?? ""), [v, b] = Ne(!1), [f, S] = Ne(null), y = dt(null), M = dt(null);
  Il(() => {
    if (!n.current || !y.current) return;
    const T = n.current.getBoundingClientRect(), O = y.current.getBoundingClientRect();
    let Y = T.bottom + 4, re = T.left;
    Y + O.height > window.innerHeight && (Y = T.top - O.height - 4), re + O.width > window.innerWidth && (re = Math.max(0, T.right - O.width)), S({ top: Y, left: re });
  }, [n]), Ie(() => {
    !l && !v && o().catch(() => b(!0));
  }, [l, v, o]), Ie(() => {
    l && M.current && M.current.focus();
  }, [l]), Ie(() => {
    const T = (O) => {
      O.key === "Escape" && i();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [i]), Ie(() => {
    const T = (Y) => {
      y.current && !y.current.contains(Y.target) && i();
    }, O = setTimeout(() => document.addEventListener("mousedown", T), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", T);
    };
  }, [i]);
  const B = jl(() => {
    if (!m) return a;
    const T = m.toLowerCase();
    return a.filter(
      (O) => O.prefix.toLowerCase().includes(T) || O.label.toLowerCase().includes(T) || O.terms != null && O.terms.some((Y) => Y.includes(T))
    );
  }, [a, m]), h = be((T) => {
    p(T.target.value);
  }, []), g = be(
    (T) => {
      c(T);
    },
    [c]
  ), C = be((T) => {
    k(T);
  }, []), Q = be((T) => {
    k(T.target.value);
  }, []), R = be(() => {
    c(_ || null);
  }, [_, c]), x = be(() => {
    c(null);
  }, [c]), $ = be(async (T) => {
    T.preventDefault(), b(!1);
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
      ref: y,
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
      v && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: $ }, u["js.iconSelect.loadError"])),
      l && B.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      l && B.map(
        (T) => T.variants.map((O) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: O.encoded,
            className: "tlIconSelect__iconCell" + (O.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": O.encoded === t,
            tabIndex: 0,
            title: T.label,
            onClick: () => s === "simple" ? g(O.encoded) : C(O.encoded),
            onKeyDown: (Y) => {
              (Y.key === "Enter" || Y.key === " ") && (Y.preventDefault(), s === "simple" ? g(O.encoded) : C(O.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: O.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: _,
        onChange: Q
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, _ && /* @__PURE__ */ e.createElement(we, { encoded: _ })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, _ ? _.startsWith("css:") ? _.substring(4) : _ : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: x }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: R }, u["js.iconSelect.ok"]))
  );
}, Al = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Bl, useCallback: je, useRef: Ol } = e, $l = ({ controlId: n, state: t }) => {
  const a = ae(), l = se(Al), [c, i] = Bl(!1), o = Ol(null), u = t.value, s = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, _ = je(() => {
    s && !r && i(!0);
  }, [s, r]), k = je(
    (f) => {
      i(!1), a("valueChanged", { value: f });
    },
    [a]
  ), v = je(() => {
    i(!1);
  }, []), b = je(async () => {
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
      onSelect: k,
      onCancel: v,
      onLoadIcons: b
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : null));
};
H("TLButton", It);
H("TLToggleButton", Pt);
H("TLTextInput", Ct);
H("TLNumberInput", wt);
H("TLDatePicker", St);
H("TLSelect", Rt);
H("TLCheckbox", xt);
H("TLTable", Lt);
H("TLCounter", Mt);
H("TLTabBar", Bt);
H("TLFieldList", Ot);
H("TLAudioRecorder", Ft);
H("TLAudioPlayer", zt);
H("TLFileUpload", Ut);
H("TLDownload", Kt);
H("TLPhotoCapture", Gt);
H("TLPhotoViewer", qt);
H("TLSplitPanel", Zt);
H("TLPanel", rn);
H("TLMaximizeRoot", an);
H("TLDeckPane", on);
H("TLSidebar", hn);
H("TLStack", _n);
H("TLGrid", bn);
H("TLCard", vn);
H("TLAppBar", En);
H("TLBreadcrumb", Cn);
H("TLBottomBar", wn);
H("TLDialog", Sn);
H("TLDialogManager", Tn);
H("TLWindow", Dn);
H("TLDrawer", Mn);
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
