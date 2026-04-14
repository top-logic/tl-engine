import { React as e, useTLFieldValue as Te, getComponent as yt, useTLState as Z, useTLCommand as oe, TLChild as q, useTLUpload as Ze, useI18N as se, useTLDataUrl as Qe, register as z } from "tl-react-bridge";
const { useCallback: wt } = e, kt = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = wt(
    (o) => {
      n(o.target.value);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "");
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = t.errorMessage, a = [
    "tlReactTextInput",
    u ? "tlReactTextInput--error" : "",
    !u && s ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0,
      title: u && i ? i : void 0
    }
  ));
}, { useCallback: St } = e, Nt = ({ controlId: l, state: t, config: r }) => {
  const [n, c] = Te(), u = St(
    (p) => {
      const m = p.target.value;
      c(m === "" ? null : m);
    },
    [c]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "");
  const s = t.hasError === !0, i = t.hasWarnings === !0, a = t.errorMessage, o = [
    "tlReactNumberInput",
    s ? "tlReactNumberInput--error" : "",
    !s && i ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: r != null && r.decimal ? "decimal" : "numeric",
      value: n != null ? String(n) : "",
      onChange: u,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && a ? a : void 0
    }
  ));
}, { useCallback: Rt } = e, Tt = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = Rt(
    (a) => {
      n(a.target.value || null);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "");
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = [
    "tlReactDatePicker",
    u ? "tlReactDatePicker--error" : "",
    !u && s ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    }
  ));
}, { useCallback: xt } = e, Lt = ({ controlId: l, state: t, config: r }) => {
  var p;
  const [n, c] = Te(), u = xt(
    (m) => {
      c(m.target.value || null);
    },
    [c]
  ), s = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const m = ((p = s.find((f) => f.value === n)) == null ? void 0 : p.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, m);
  }
  const i = t.hasError === !0, a = t.hasWarnings === !0, o = [
    "tlReactSelect",
    i ? "tlReactSelect--error" : "",
    !i && a ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: u,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": i || void 0
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((m) => /* @__PURE__ */ e.createElement("option", { key: m.value, value: m.value }, m.label))
  ));
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [r, n] = Te(), c = Dt(
    (a) => {
      n(a.target.checked);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement(
      "input",
      {
        type: "checkbox",
        id: l,
        checked: r === !0,
        disabled: !0,
        className: "tlReactCheckbox tlReactCheckbox--immutable"
      }
    );
  const u = t.hasError === !0, s = t.hasWarnings === !0, i = [
    "tlReactCheckbox",
    u ? "tlReactCheckbox--error" : "",
    !u && s ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: i,
      "aria-invalid": u || void 0
    }
  );
}, Mt = ({ controlId: l, state: t }) => {
  const r = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, u) => /* @__PURE__ */ e.createElement("tr", { key: u }, r.map((s) => {
    const i = s.cellModule ? yt(s.cellModule) : void 0, a = c[s.name];
    if (i) {
      const o = { value: a, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: s.name }, /* @__PURE__ */ e.createElement(
        i,
        {
          controlId: l + "-" + u + "-" + s.name,
          state: o
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: s.name }, a != null ? String(a) : "");
  })))));
};
function ke({ encoded: l, className: t }) {
  if (l.startsWith("css:")) {
    const r = l.substring(4);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  if (l.startsWith("colored:")) {
    const r = l.substring(8);
    return /* @__PURE__ */ e.createElement("i", { className: r + (t ? " " + t : "") });
  }
  return l.startsWith("/") || l.startsWith("theme:") ? /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: t, style: { width: "1em", height: "1em" } }) : /* @__PURE__ */ e.createElement("i", { className: l + (t ? " " + t : "") });
}
const { useCallback: Pt } = e, jt = ({ controlId: l, command: t, label: r, disabled: n }) => {
  const c = Z(), u = oe(), s = t ?? "click", i = r ?? c.label, a = n ?? c.disabled === !0, o = c.hidden === !0, p = c.image, m = c.iconOnly === !0, f = o ? { display: "none" } : void 0, C = Pt(() => {
    u(s);
  }, [u, s]);
  return p && m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: a,
      style: f,
      className: "tlReactButton tlReactButton--icon",
      title: i,
      "aria-label": i
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: p })
  ) : p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: a,
      style: f,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(ke, { encoded: p, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, i)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: C,
      disabled: a,
      style: f,
      className: "tlReactButton"
    },
    i
  );
}, { useCallback: At } = e, Bt = ({ controlId: l, command: t, label: r, active: n, disabled: c }) => {
  const u = Z(), s = oe(), i = t ?? "click", a = r ?? u.label, o = n ?? u.active === !0, p = c ?? u.disabled === !0, m = At(() => {
    s(i);
  }, [s, i]);
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
}, Ot = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: $t } = e, Ft = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.tabs ?? [], c = t.activeTabId, u = $t((s) => {
    s !== c && r("selectTab", { tabId: s });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === c,
      className: "tlReactTabBar__tab" + (s.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => u(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, Ht = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, u) => /* @__PURE__ */ e.createElement("div", { key: u, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(q, { control: c })))));
}, Wt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, zt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(null), i = e.useRef(null), a = e.useRef([]), o = e.useRef(null), p = t.status ?? "idle", m = t.error, f = p === "received" ? "idle" : n !== "idle" ? n : p, C = e.useCallback(async () => {
    if (n === "recording") {
      const k = i.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (n !== "uploading") {
      if (s(null), !window.isSecureContext || !navigator.mediaDevices) {
        s("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = k, a.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(k, D ? { mimeType: D } : void 0);
        i.current = P, P.ondataavailable = (v) => {
          v.data.size > 0 && a.current.push(v.data);
        }, P.onstop = async () => {
          k.getTracks().forEach((g) => g.stop()), o.current = null;
          const v = new Blob(a.current, { type: P.mimeType || "audio/webm" });
          if (a.current = [], v.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const _ = new FormData();
          _.append("audio", v, "recording.webm"), await r(_), c("idle");
        }, P.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), s("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, r]), h = se(Wt), E = f === "recording" ? h["js.audioRecorder.stop"] : f === "uploading" ? h["js.uploading"] : h["js.audioRecorder.record"], b = f === "uploading", N = ["tlAudioRecorder__button"];
  return f === "recording" && N.push("tlAudioRecorder__button--recording"), f === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: C,
      disabled: b,
      title: E,
      "aria-label": E
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, h[u]), m && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, m));
}, Ut = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Vt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [u, s] = e.useState(n ? "idle" : "disabled"), i = e.useRef(null), a = e.useRef(null), o = e.useRef(c);
  e.useEffect(() => {
    n ? u === "disabled" && s("idle") : (i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), s("disabled"));
  }, [n]), e.useEffect(() => {
    c !== o.current && (o.current = c, i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null), (u === "playing" || u === "paused" || u === "loading") && s("idle"));
  }, [c]), e.useEffect(() => () => {
    i.current && (i.current.pause(), i.current = null), a.current && (URL.revokeObjectURL(a.current), a.current = null);
  }, []);
  const p = e.useCallback(async () => {
    if (u === "disabled" || u === "loading")
      return;
    if (u === "playing") {
      i.current && i.current.pause(), s("paused");
      return;
    }
    if (u === "paused" && i.current) {
      i.current.play(), s("playing");
      return;
    }
    if (!a.current) {
      s("loading");
      try {
        const b = await fetch(r);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), s("idle");
          return;
        }
        const N = await b.blob();
        a.current = URL.createObjectURL(N);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), s("idle");
        return;
      }
    }
    const E = new Audio(a.current);
    i.current = E, E.onended = () => {
      s("idle");
    }, E.play(), s("playing");
  }, [u, r]), m = se(Ut), f = u === "loading" ? m["js.loading"] : u === "playing" ? m["js.audioPlayer.pause"] : u === "disabled" ? m["js.audioPlayer.noAudio"] : m["js.audioPlayer.play"], C = u === "disabled" || u === "loading", h = ["tlAudioPlayer__button"];
  return u === "playing" && h.push("tlAudioPlayer__button--playing"), u === "loading" && h.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: p,
      disabled: C,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${u === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Kt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Yt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(!1), i = e.useRef(null), a = t.status ?? "idle", o = t.error, p = t.accept ?? "", m = a === "received" ? "idle" : n !== "idle" ? n : a, f = e.useCallback(async (v) => {
    c("uploading");
    const _ = new FormData();
    _.append("file", v, v.name), await r(_), c("idle");
  }, [r]), C = e.useCallback((v) => {
    var g;
    const _ = (g = v.target.files) == null ? void 0 : g[0];
    _ && f(_);
  }, [f]), h = e.useCallback(() => {
    var v;
    n !== "uploading" && ((v = i.current) == null || v.click());
  }, [n]), E = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!0);
  }, []), b = e.useCallback((v) => {
    v.preventDefault(), v.stopPropagation(), s(!1);
  }, []), N = e.useCallback((v) => {
    var g;
    if (v.preventDefault(), v.stopPropagation(), s(!1), n === "uploading") return;
    const _ = (g = v.dataTransfer.files) == null ? void 0 : g[0];
    _ && f(_);
  }, [n, f]), k = m === "uploading", D = se(Kt), P = m === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${u ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: b,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: i,
        type: "file",
        accept: p || void 0,
        onChange: C,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (m === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: h,
        disabled: k,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, o)
  );
}, Gt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Xt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = oe(), c = !!t.hasData, u = t.dataRevision ?? 0, s = t.fileName ?? "download", i = !!t.clearable, [a, o] = e.useState(!1), p = e.useCallback(async () => {
    if (!(!c || a)) {
      o(!0);
      try {
        const h = r + (r.includes("?") ? "&" : "?") + "rev=" + u, E = await fetch(h);
        if (!E.ok) {
          console.error("[TLDownload] Failed to fetch data:", E.status);
          return;
        }
        const b = await E.blob(), N = URL.createObjectURL(b), k = document.createElement("a");
        k.href = N, k.download = s, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(N);
      } catch (h) {
        console.error("[TLDownload] Fetch error:", h);
      } finally {
        o(!1);
      }
    }
  }, [c, a, r, u, s]), m = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), f = se(Gt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const C = a ? f["js.downloading"] : f["js.download.file"].replace("{0}", s);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (a ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: p,
      disabled: a,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: s }, s), i && /* @__PURE__ */ e.createElement(
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
}, qt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = Z(), r = Ze(), [n, c] = e.useState("idle"), [u, s] = e.useState(null), [i, a] = e.useState(!1), o = e.useRef(null), p = e.useRef(null), m = e.useRef(null), f = e.useRef(null), C = e.useRef(null), h = t.error, E = e.useMemo(
    () => {
      var S;
      return !!(window.isSecureContext && ((S = navigator.mediaDevices) != null && S.getUserMedia));
    },
    []
  ), b = e.useCallback(() => {
    p.current && (p.current.getTracks().forEach((S) => S.stop()), p.current = null), o.current && (o.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    b(), c("idle");
  }, [b]), k = e.useCallback(async () => {
    var S;
    if (n !== "uploading") {
      if (s(null), !E) {
        (S = f.current) == null || S.click();
        return;
      }
      try {
        const $ = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        p.current = $, c("overlayOpen");
      } catch ($) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", $), s("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [n, E]), D = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const S = o.current, $ = m.current;
    if (!S || !$)
      return;
    $.width = S.videoWidth, $.height = S.videoHeight;
    const x = $.getContext("2d");
    x && (x.drawImage(S, 0, 0), b(), c("uploading"), $.toBlob(async (O) => {
      if (!O) {
        c("idle");
        return;
      }
      const X = new FormData();
      X.append("photo", O, "capture.jpg"), await r(X), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, r, b]), P = e.useCallback(async (S) => {
    var O;
    const $ = (O = S.target.files) == null ? void 0 : O[0];
    if (!$) return;
    c("uploading");
    const x = new FormData();
    x.append("photo", $, $.name), await r(x), c("idle"), f.current && (f.current.value = "");
  }, [r]);
  e.useEffect(() => {
    n === "overlayOpen" && o.current && p.current && (o.current.srcObject = p.current);
  }, [n]), e.useEffect(() => {
    var $;
    if (n !== "overlayOpen") return;
    ($ = C.current) == null || $.focus();
    const S = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = S;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const S = ($) => {
      $.key === "Escape" && N();
    };
    return document.addEventListener("keydown", S), () => document.removeEventListener("keydown", S);
  }, [n, N]), e.useEffect(() => () => {
    p.current && (p.current.getTracks().forEach((S) => S.stop()), p.current = null);
  }, []);
  const v = se(qt), _ = n === "uploading" ? v["js.uploading"] : v["js.photoCapture.open"], g = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && g.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  i && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const w = ["tlPhotoCapture__mirrorBtn"];
  return i && w.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: k,
      disabled: n === "uploading",
      title: _,
      "aria-label": _
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !E && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: m, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: C,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: o,
        className: H.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: w.join(" "),
        onClick: () => a((S) => !S),
        title: v["js.photoCapture.mirror"],
        "aria-label": v["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: D,
        title: v["js.photoCapture.capture"],
        "aria-label": v["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: v["js.photoCapture.close"],
        "aria-label": v["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, v[u]), h && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, h));
}, Qt = {
  "js.photoViewer.alt": "Captured photo"
}, Jt = ({ controlId: l }) => {
  const t = Z(), r = Qe(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [u, s] = e.useState(null), i = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      u && (URL.revokeObjectURL(u), s(null));
      return;
    }
    if (c === i.current && u)
      return;
    i.current = c, u && (URL.revokeObjectURL(u), s(null));
    let o = !1;
    return (async () => {
      try {
        const p = await fetch(r);
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
  }, [n, c, r]), e.useEffect(() => () => {
    u && URL.revokeObjectURL(u);
  }, []);
  const a = se(Qt);
  return !n || !u ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: u,
      alt: a["js.photoViewer.alt"]
    }
  ));
}, { useCallback: lt, useRef: Oe } = e, en = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.orientation, c = t.resizable === !0, u = t.children ?? [], s = n === "horizontal", i = u.length > 0 && u.every((b) => b.collapsed), a = !i && u.some((b) => b.collapsed), o = i ? !s : s, p = Oe(null), m = Oe(null), f = Oe(null), C = lt((b, N) => {
    const k = {
      overflow: b.scrolling || "auto"
    };
    return b.collapsed ? i && !o ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : N !== void 0 ? k.flex = `0 0 ${N}px` : b.unit === "%" || a ? k.flex = `${b.size} 0 0%` : k.flex = `0 0 ${b.size}px`, b.minSize > 0 && !b.collapsed && (k.minWidth = s ? b.minSize : void 0, k.minHeight = s ? void 0 : b.minSize), k;
  }, [s, i, a, o]), h = lt((b, N) => {
    b.preventDefault();
    const k = p.current;
    if (!k) return;
    const D = u[N], P = u[N + 1], v = k.querySelectorAll(":scope > .tlSplitPanel__child"), _ = [];
    v.forEach((w) => {
      _.push(s ? w.offsetWidth : w.offsetHeight);
    }), f.current = _, m.current = {
      splitterIndex: N,
      startPos: s ? b.clientX : b.clientY,
      startSizeBefore: _[N],
      startSizeAfter: _[N + 1],
      childBefore: D,
      childAfter: P
    };
    const g = (w) => {
      const S = m.current;
      if (!S || !f.current) return;
      const x = (s ? w.clientX : w.clientY) - S.startPos, O = S.childBefore.minSize || 0, X = S.childAfter.minSize || 0;
      let re = S.startSizeBefore + x, W = S.startSizeAfter - x;
      re < O && (W += re - O, re = O), W < X && (re += W - X, W = X), f.current[S.splitterIndex] = re, f.current[S.splitterIndex + 1] = W;
      const J = k.querySelectorAll(":scope > .tlSplitPanel__child"), B = J[S.splitterIndex], M = J[S.splitterIndex + 1];
      B && (B.style.flex = `0 0 ${re}px`), M && (M.style.flex = `0 0 ${W}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", g), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const w = {};
        u.forEach((S, $) => {
          const x = S.control;
          x != null && x.controlId && f.current && (w[x.controlId] = f.current[$]);
        }), r("updateSizes", { sizes: w });
      }
      f.current = null, m.current = null;
    };
    document.addEventListener("mousemove", g), document.addEventListener("mouseup", H), document.body.style.cursor = s ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [u, s, r]), E = [];
  return u.forEach((b, N) => {
    if (E.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${b.collapsed && o ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: C(b)
        },
        /* @__PURE__ */ e.createElement(q, { control: b.control })
      )
    ), c && N < u.length - 1) {
      const k = u[N + 1];
      !b.collapsed && !k.collapsed && E.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => h(P, N)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: p,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${i ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: o ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    E
  );
}, { useCallback: $e } = e, tn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), on = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), sn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(tn), c = t.title, u = t.expansionState ?? "NORMALIZED", s = t.showMinimize === !0, i = t.showMaximize === !0, a = t.showPopOut === !0, o = t.fullLine === !0, p = t.toolbarButtons ?? [], m = u === "MINIMIZED", f = u === "MAXIMIZED", C = u === "HIDDEN", h = $e(() => {
    r("toggleMinimize");
  }, [r]), E = $e(() => {
    r("toggleMaximize");
  }, [r]), b = $e(() => {
    r("popOut");
  }, [r]);
  if (C)
    return null;
  const N = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${u.toLowerCase()}${o ? " tlPanel--fullLine" : ""}`,
      style: N
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, p.map((k, D) => /* @__PURE__ */ e.createElement("span", { key: D, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: k }))), s && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: m ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(ln, null) : /* @__PURE__ */ e.createElement(nn, null)
    ), i && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: f ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(on, null) : /* @__PURE__ */ e.createElement(rn, null)
    ), a && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(an, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(q, { control: t.child }))
  );
}, cn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(q, { control: t.child })
  );
}, un = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(q, { control: t.activeChild }));
}, { useCallback: de, useState: je, useEffect: Ae, useRef: Be } = e, dn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ge(l, t, r, n) {
  const c = [];
  for (const u of l)
    u.type === "nav" ? c.push({ id: u.id, type: "nav", groupId: n }) : u.type === "command" ? c.push({ id: u.id, type: "command", groupId: n }) : u.type === "group" && (c.push({ id: u.id, type: "group" }), (r.get(u.id) ?? u.expanded) && !t && c.push(...Ge(u.children, t, r, u.id)));
  return c;
}
const Se = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, mn = ({ item: l, active: t, collapsed: r, onSelect: n, tabIndex: c, itemRef: u, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: r ? l.label : void 0,
    tabIndex: c,
    ref: u,
    onFocus: () => s(l.id)
  },
  r && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !r && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), pn = ({ item: l, collapsed: t, onExecute: r, tabIndex: n, itemRef: c, onFocus: u }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => u(l.id)
  },
  /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), fn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Se, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), hn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), _n = ({ item: l, activeItemId: t, anchorRect: r, onSelect: n, onExecute: c, onClose: u }) => {
  const s = Be(null);
  Ae(() => {
    const o = (p) => {
      s.current && !s.current.contains(p.target) && setTimeout(() => u(), 0);
    };
    return document.addEventListener("mousedown", o), () => document.removeEventListener("mousedown", o);
  }, [u]), Ae(() => {
    const o = (p) => {
      p.key === "Escape" && u();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [u]);
  const i = de((o) => {
    o.type === "nav" ? (n(o.id), u()) : o.type === "command" && (c(o.id), u());
  }, [n, c, u]), a = {};
  return r && (a.left = r.right, a.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: s, role: "menu", style: a }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((o) => {
    if (o.type === "nav" || o.type === "command") {
      const p = o.type === "nav" && o.id === t;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: o.id,
          className: "tlSidebar__flyoutItem" + (p ? " tlSidebar__flyoutItem--active" : ""),
          role: "menuitem",
          onClick: () => i(o)
        },
        /* @__PURE__ */ e.createElement(Se, { icon: o.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, o.label),
        o.type === "nav" && o.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, o.badge)
      );
    }
    return o.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: o.id, className: "tlSidebar__flyoutSectionHeader" }, o.label) : o.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: o.id, className: "tlSidebar__separator" }) : null;
  }));
}, bn = ({
  item: l,
  expanded: t,
  activeItemId: r,
  collapsed: n,
  onSelect: c,
  onExecute: u,
  onToggleGroup: s,
  tabIndex: i,
  itemRef: a,
  onFocus: o,
  focusedId: p,
  setItemRef: m,
  onItemFocus: f,
  flyoutGroupId: C,
  onOpenFlyout: h,
  onCloseFlyout: E
}) => {
  const b = Be(null), [N, k] = je(null), D = de(() => {
    n ? C === l.id ? E() : (b.current && k(b.current.getBoundingClientRect()), h(l.id)) : s(l.id);
  }, [n, C, l.id, s, h, E]), P = de((_) => {
    b.current = _, a(_);
  }, [a]), v = n && C === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (v ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: n ? l.label : void 0,
      "aria-expanded": n ? v : t,
      tabIndex: i,
      ref: P,
      onFocus: () => o(l.id)
    },
    /* @__PURE__ */ e.createElement(Se, { icon: l.icon }),
    !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !n && /* @__PURE__ */ e.createElement(
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
  ), v && /* @__PURE__ */ e.createElement(
    _n,
    {
      item: l,
      activeItemId: r,
      anchorRect: N,
      onSelect: c,
      onExecute: u,
      onClose: E
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((_) => /* @__PURE__ */ e.createElement(
    ht,
    {
      key: _.id,
      item: _,
      activeItemId: r,
      collapsed: n,
      onSelect: c,
      onExecute: u,
      onToggleGroup: s,
      focusedId: p,
      setItemRef: m,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: C,
      onOpenFlyout: h,
      onCloseFlyout: E
    }
  ))));
}, ht = ({
  item: l,
  activeItemId: t,
  collapsed: r,
  onSelect: n,
  onExecute: c,
  onToggleGroup: u,
  focusedId: s,
  setItemRef: i,
  onItemFocus: a,
  groupStates: o,
  flyoutGroupId: p,
  onOpenFlyout: m,
  onCloseFlyout: f
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        mn,
        {
          item: l,
          active: l.id === t,
          collapsed: r,
          onSelect: n,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        pn,
        {
          item: l,
          collapsed: r,
          onExecute: c,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(fn, { item: l, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(hn, null);
    case "group": {
      const C = o ? o.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        bn,
        {
          item: l,
          expanded: C,
          activeItemId: t,
          collapsed: r,
          onSelect: n,
          onExecute: c,
          onToggleGroup: u,
          tabIndex: s === l.id ? 0 : -1,
          itemRef: i(l.id),
          onFocus: a,
          focusedId: s,
          setItemRef: i,
          onItemFocus: a,
          flyoutGroupId: p,
          onOpenFlyout: m,
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, vn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(dn), c = t.items ?? [], u = t.activeItemId, s = t.collapsed, [i, a] = je(() => {
    const w = /* @__PURE__ */ new Map(), S = ($) => {
      for (const x of $)
        x.type === "group" && (w.set(x.id, x.expanded), S(x.children));
    };
    return S(c), w;
  }), o = de((w) => {
    a((S) => {
      const $ = new Map(S), x = $.get(w) ?? !1;
      return $.set(w, !x), r("toggleGroup", { itemId: w, expanded: !x }), $;
    });
  }, [r]), p = de((w) => {
    w !== u && r("selectItem", { itemId: w });
  }, [r, u]), m = de((w) => {
    r("executeCommand", { itemId: w });
  }, [r]), f = de(() => {
    r("toggleCollapse", {});
  }, [r]), [C, h] = je(null), E = de((w) => {
    h(w);
  }, []), b = de(() => {
    h(null);
  }, []);
  Ae(() => {
    s || h(null);
  }, [s]);
  const [N, k] = je(() => {
    const w = Ge(c, s, i);
    return w.length > 0 ? w[0].id : "";
  }), D = Be(/* @__PURE__ */ new Map()), P = de((w) => (S) => {
    S ? D.current.set(w, S) : D.current.delete(w);
  }, []), v = de((w) => {
    k(w);
  }, []), _ = Be(0), g = de((w) => {
    k(w), _.current++;
  }, []);
  Ae(() => {
    const w = D.current.get(N);
    w && document.activeElement !== w && w.focus();
  }, [N, _.current]);
  const H = de((w) => {
    if (w.key === "Escape" && C !== null) {
      w.preventDefault(), b();
      return;
    }
    const S = Ge(c, s, i);
    if (S.length === 0) return;
    const $ = S.findIndex((O) => O.id === N);
    if ($ < 0) return;
    const x = S[$];
    switch (w.key) {
      case "ArrowDown": {
        w.preventDefault();
        const O = ($ + 1) % S.length;
        g(S[O].id);
        break;
      }
      case "ArrowUp": {
        w.preventDefault();
        const O = ($ - 1 + S.length) % S.length;
        g(S[O].id);
        break;
      }
      case "Home": {
        w.preventDefault(), g(S[0].id);
        break;
      }
      case "End": {
        w.preventDefault(), g(S[S.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        w.preventDefault(), x.type === "nav" ? p(x.id) : x.type === "command" ? m(x.id) : x.type === "group" && (s ? C === x.id ? b() : E(x.id) : o(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !s && ((i.get(x.id) ?? !1) || (w.preventDefault(), o(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !s && (i.get(x.id) ?? !1) && (w.preventDefault(), o(x.id));
        break;
      }
    }
  }, [
    c,
    s,
    i,
    N,
    C,
    g,
    p,
    m,
    o,
    E,
    b
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (s ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, s ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: H }, c.map((w) => /* @__PURE__ */ e.createElement(
    ht,
    {
      key: w.id,
      item: w,
      activeItemId: u,
      collapsed: s,
      onSelect: p,
      onExecute: m,
      onToggleGroup: o,
      focusedId: N,
      setItemRef: P,
      onItemFocus: v,
      groupStates: i,
      flyoutGroupId: C,
      onOpenFlyout: E,
      onCloseFlyout: b
    }
  ))), s ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(q, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(q, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: s ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(q, { control: t.activeContent })));
}, En = ({ controlId: l }) => {
  const t = Z(), r = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", u = t.wrap === !0, s = t.children ?? [], i = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    u ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: i }, s.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })));
}, gn = ({ controlId: l }) => {
  const t = Z(), r = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", u = t.children ?? [], s = {};
  return n ? s.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : r && (s.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: s }, u.map((i, a) => /* @__PURE__ */ e.createElement(q, { key: a, control: i })));
}, Cn = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", u = t.headerActions ?? [], s = t.child, i = r != null || u.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, i && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, u.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(q, { control: s })));
}, yn = ({ controlId: l }) => {
  const t = Z(), r = t.title ?? "", n = t.leading, c = t.actions ?? [], u = t.variant ?? "flat", i = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    u === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: i }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(q, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((a, o) => /* @__PURE__ */ e.createElement(q, { key: o, control: a }))));
}, { useCallback: wn } = e, kn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.items ?? [], c = wn((u) => {
    r("navigate", { itemId: u });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((u, s) => {
    const i = s === n.length - 1;
    return /* @__PURE__ */ e.createElement("li", { key: u.id, className: "tlBreadcrumb__entry" }, s > 0 && /* @__PURE__ */ e.createElement(
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
    ), i ? /* @__PURE__ */ e.createElement("span", { className: "tlBreadcrumb__current", "aria-current": "page" }, u.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlBreadcrumb__item",
        onClick: () => c(u.id)
      },
      u.label
    ));
  })));
}, { useCallback: Sn } = e, Nn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.items ?? [], c = t.activeItemId, u = Sn((s) => {
    s !== c && r("selectItem", { itemId: s });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((s) => {
    const i = s.id === c;
    return /* @__PURE__ */ e.createElement(
      "button",
      {
        key: s.id,
        type: "button",
        className: "tlBottomBar__item" + (i ? " tlBottomBar__item--active" : ""),
        onClick: () => u(s.id),
        "aria-current": i ? "page" : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__iconWrap" }, /* @__PURE__ */ e.createElement("i", { className: "tlBottomBar__icon " + s.icon, "aria-hidden": "true" }), s.badge && /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__badge" }, s.badge)),
      /* @__PURE__ */ e.createElement("span", { className: "tlBottomBar__label" }, s.label)
    );
  }));
}, { useCallback: rt, useEffect: ot, useRef: Rn } = e, Tn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.open === !0, c = t.closeOnBackdrop !== !1, u = t.child, s = Rn(null), i = rt(() => {
    r("close");
  }, [r]), a = rt((o) => {
    c && o.target === o.currentTarget && i();
  }, [c, i]);
  return ot(() => {
    if (!n) return;
    const o = (p) => {
      p.key === "Escape" && i();
    };
    return document.addEventListener("keydown", o), () => document.removeEventListener("keydown", o);
  }, [n, i]), ot(() => {
    n && s.current && s.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: a,
      ref: s,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(q, { control: u })
  ) : null;
}, { useEffect: xn, useRef: Ln } = e, Dn = ({ controlId: l }) => {
  const r = Z().dialogs ?? [], n = Ln(r.length);
  return xn(() => {
    r.length < n.current && r.length > 0, n.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, r.map((c) => /* @__PURE__ */ e.createElement(q, { key: c.controlId, control: c })));
}, { useCallback: xe, useRef: ge, useState: Le } = e, In = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Mn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Pn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(In), c = t.title ?? "", u = t.width ?? "32rem", s = t.height ?? null, i = t.minHeight ?? null, a = t.resizable === !0, o = t.child, p = t.actions ?? [], m = t.toolbarButtons ?? [], [f, C] = Le(null), [h, E] = Le(null), [b, N] = Le(null), k = ge(null), [D, P] = Le(!1), v = ge(null), _ = ge(null), g = ge(null), H = ge(null), w = ge(null), S = xe(() => {
    r("close");
  }, [r]), $ = xe((W, J) => {
    J.preventDefault();
    const B = H.current;
    if (!B) return;
    const M = B.getBoundingClientRect(), Q = !k.current, d = k.current ?? { x: M.left, y: M.top };
    Q && (k.current = d, N(d)), w.current = {
      dir: W,
      startX: J.clientX,
      startY: J.clientY,
      startW: M.width,
      startH: M.height,
      startPos: { ...d },
      symmetric: Q
    };
    const y = (j) => {
      const L = w.current;
      if (!L) return;
      const K = j.clientX - L.startX, Y = j.clientY - L.startY;
      let le = L.startW, ee = L.startH, ue = 0, me = 0;
      L.symmetric ? (L.dir.includes("e") && (le = L.startW + 2 * K), L.dir.includes("w") && (le = L.startW - 2 * K), L.dir.includes("s") && (ee = L.startH + 2 * Y), L.dir.includes("n") && (ee = L.startH - 2 * Y)) : (L.dir.includes("e") && (le = L.startW + K), L.dir.includes("w") && (le = L.startW - K, ue = K), L.dir.includes("s") && (ee = L.startH + Y), L.dir.includes("n") && (ee = L.startH - Y, me = Y));
      const he = Math.max(200, le), R = Math.max(100, ee);
      L.symmetric ? (ue = (L.startW - he) / 2, me = (L.startH - R) / 2) : (L.dir.includes("w") && he === 200 && (ue = L.startW - 200), L.dir.includes("n") && R === 100 && (me = L.startH - 100)), _.current = he, g.current = R, C(he), E(R);
      const I = {
        x: L.startPos.x + ue,
        y: L.startPos.y + me
      };
      k.current = I, N(I);
    }, F = () => {
      document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", F);
      const j = _.current, L = g.current;
      (j != null || L != null) && r("resize", {
        ...j != null ? { width: Math.round(j) + "px" } : {},
        ...L != null ? { height: Math.round(L) + "px" } : {}
      }), w.current = null;
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", F);
  }, [r]), x = xe((W) => {
    if (W.button !== 0 || W.target.closest("button")) return;
    W.preventDefault();
    const J = H.current;
    if (!J) return;
    const B = J.getBoundingClientRect(), M = k.current ?? { x: B.left, y: B.top }, Q = W.clientX - M.x, d = W.clientY - M.y, y = (j) => {
      const L = window.innerWidth, K = window.innerHeight;
      let Y = j.clientX - Q, le = j.clientY - d;
      const ee = J.offsetWidth, ue = J.offsetHeight;
      Y + ee > L && (Y = L - ee), le + ue > K && (le = K - ue), Y < 0 && (Y = 0), le < 0 && (le = 0);
      const me = { x: Y, y: le };
      k.current = me, N(me);
    }, F = () => {
      document.removeEventListener("mousemove", y), document.removeEventListener("mouseup", F);
    };
    document.addEventListener("mousemove", y), document.addEventListener("mouseup", F);
  }, []), O = xe(() => {
    var W, J;
    if (D) {
      const B = v.current;
      B && (N(B.x !== -1 ? { x: B.x, y: B.y } : null), C(B.w), E(B.h)), P(!1);
    } else {
      const B = H.current, M = B == null ? void 0 : B.getBoundingClientRect();
      v.current = {
        x: ((W = k.current) == null ? void 0 : W.x) ?? (M == null ? void 0 : M.left) ?? -1,
        y: ((J = k.current) == null ? void 0 : J.y) ?? (M == null ? void 0 : M.top) ?? -1,
        w: f ?? (M == null ? void 0 : M.width) ?? null,
        h: h ?? null
      }, P(!0), N({ x: 0, y: 0 }), C(null), E(null);
    }
  }, [D, f, h]), X = D ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: f != null ? f + "px" : u,
    ...h != null ? { height: h + "px" } : s != null ? { height: s } : {},
    ...i != null && h == null ? { minHeight: i } : {},
    maxHeight: b ? "100vh" : "80vh",
    ...b ? { position: "absolute", left: b.x + "px", top: b.y + "px" } : {}
  }, re = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: X,
      ref: H,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": re
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${D ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: D ? void 0 : x,
        onDoubleClick: O
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: re }, c),
      m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((W, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(q, { control: W })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: D ? n["js.window.restore"] : n["js.window.maximize"]
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
          title: n["js.window.close"]
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(q, { control: o })),
    p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, p.map((W, J) => /* @__PURE__ */ e.createElement(q, { key: J, control: W }))),
    a && !D && Mn.map((W) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: W,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${W}`,
        onMouseDown: (J) => $(W, J)
      }
    ))
  );
}, { useCallback: jn, useEffect: An } = e, Bn = {
  "js.drawer.close": "Close"
}, On = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(Bn), c = t.open === !0, u = t.position ?? "right", s = t.size ?? "medium", i = t.title ?? null, a = t.child, o = jn(() => {
    r("close");
  }, [r]);
  An(() => {
    if (!c) return;
    const m = (f) => {
      f.key === "Escape" && o();
    };
    return document.addEventListener("keydown", m), () => document.removeEventListener("keydown", m);
  }, [c, o]);
  const p = [
    "tlDrawer",
    `tlDrawer--${u}`,
    `tlDrawer--${s}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: p, "aria-hidden": !c }, i !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, i), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: o,
      title: n["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, a && /* @__PURE__ */ e.createElement(q, { control: a })));
}, { useCallback: $n, useEffect: Fn, useState: Hn } = e, Wn = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.message ?? "", c = t.content ?? "", u = t.variant ?? "info", s = t.duration ?? 5e3, i = t.visible === !0, a = t.generation ?? 0, [o, p] = Hn(!1), m = $n(() => {
    p(!0), setTimeout(() => {
      r("dismiss", { generation: a }), p(!1);
    }, 200);
  }, [r, a]);
  return Fn(() => {
    if (!i || s === 0) return;
    const f = setTimeout(m, s);
    return () => clearTimeout(f);
  }, [i, s, m]), !i && !o ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${u}${o ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n)
  );
}, { useCallback: Fe, useEffect: He, useRef: zn, useState: at } = e, Un = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.open === !0, c = t.anchorId, u = t.items ?? [], s = zn(null), [i, a] = at({ top: 0, left: 0 }), [o, p] = at(0), m = u.filter((E) => E.type === "item" && !E.disabled);
  He(() => {
    var v, _;
    if (!n || !c) return;
    const E = document.getElementById(c);
    if (!E) return;
    const b = E.getBoundingClientRect(), N = ((v = s.current) == null ? void 0 : v.offsetHeight) ?? 200, k = ((_ = s.current) == null ? void 0 : _.offsetWidth) ?? 200;
    let D = b.bottom + 4, P = b.left;
    D + N > window.innerHeight && (D = b.top - N - 4), P + k > window.innerWidth && (P = b.right - k), a({ top: D, left: P }), p(0);
  }, [n, c]);
  const f = Fe(() => {
    r("close");
  }, [r]), C = Fe((E) => {
    r("selectItem", { itemId: E });
  }, [r]);
  He(() => {
    if (!n) return;
    const E = (b) => {
      s.current && !s.current.contains(b.target) && f();
    };
    return document.addEventListener("mousedown", E), () => document.removeEventListener("mousedown", E);
  }, [n, f]);
  const h = Fe((E) => {
    if (E.key === "Escape") {
      f();
      return;
    }
    if (E.key === "ArrowDown")
      E.preventDefault(), p((b) => (b + 1) % m.length);
    else if (E.key === "ArrowUp")
      E.preventDefault(), p((b) => (b - 1 + m.length) % m.length);
    else if (E.key === "Enter" || E.key === " ") {
      E.preventDefault();
      const b = m[o];
      b && C(b.id);
    }
  }, [f, C, m, o]);
  return He(() => {
    n && s.current && s.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: i.top, left: i.left },
      onKeyDown: h
    },
    u.map((E, b) => {
      if (E.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: b, className: "tlMenu__separator" });
      const k = m.indexOf(E) === o;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: E.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (E.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: E.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => C(E.id)
        },
        E.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + E.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, E.label)
      );
    })
  ) : null;
}, Vn = ({ controlId: l }) => {
  const t = Z(), r = t.header, n = t.content, c = t.footer, u = t.snackbar, s = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(q, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(q, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(q, { control: c })), /* @__PURE__ */ e.createElement(q, { control: u }), s && /* @__PURE__ */ e.createElement(q, { control: s }));
}, Kn = () => {
  const t = Z().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Yn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, st = 50, Gn = () => {
  const l = Z(), t = oe(), r = se(Yn), n = l.columns ?? [], c = l.totalRowCount ?? 0, u = l.rows ?? [], s = l.rowHeight ?? 36, i = l.selectionMode ?? "single", a = l.selectedCount ?? 0, o = l.frozenColumnCount ?? 0, p = l.treeMode ?? !1, m = e.useMemo(
    () => n.filter((R) => R.sortPriority && R.sortPriority > 0).length,
    [n]
  ), f = i === "multi", C = 40, h = 20, E = e.useRef(null), b = e.useRef(null), N = e.useRef(null), [k, D] = e.useState({}), P = e.useRef(null), v = e.useRef(!1), _ = e.useRef(null), [g, H] = e.useState(null), [w, S] = e.useState(null);
  e.useEffect(() => {
    P.current || D({});
  }, [n]);
  const $ = e.useCallback((R) => k[R.name] ?? R.width, [k]), x = e.useMemo(() => {
    const R = [];
    let I = f && o > 0 ? C : 0;
    for (let G = 0; G < o && G < n.length; G++)
      R.push(I), I += $(n[G]);
    return R;
  }, [n, o, f, C, $]), O = c * s, X = e.useRef(null), re = e.useCallback((R, I, G) => {
    G.preventDefault(), G.stopPropagation(), P.current = { column: R, startX: G.clientX, startWidth: I };
    let te = G.clientX, T = 0;
    const A = () => {
      const ae = P.current;
      if (!ae) return;
      const pe = Math.max(st, ae.startWidth + (te - ae.startX) + T);
      D((Ee) => ({ ...Ee, [ae.column]: pe }));
    }, U = () => {
      const ae = b.current, pe = E.current;
      if (!ae || !P.current) return;
      const Ee = ae.getBoundingClientRect(), et = 40, tt = 8, Ct = ae.scrollLeft;
      te > Ee.right - et ? ae.scrollLeft += tt : te < Ee.left + et && (ae.scrollLeft = Math.max(0, ae.scrollLeft - tt));
      const nt = ae.scrollLeft - Ct;
      nt !== 0 && (pe && (pe.scrollLeft = ae.scrollLeft), T += nt, A()), X.current = requestAnimationFrame(U);
    };
    X.current = requestAnimationFrame(U);
    const ne = (ae) => {
      te = ae.clientX, A();
    }, _e = (ae) => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", _e), X.current !== null && (cancelAnimationFrame(X.current), X.current = null);
      const pe = P.current;
      if (pe) {
        const Ee = Math.max(st, pe.startWidth + (ae.clientX - pe.startX) + T);
        t("columnResize", { column: pe.column, width: Ee }), P.current = null, v.current = !0, requestAnimationFrame(() => {
          v.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", _e);
  }, [t]), W = e.useCallback(() => {
    E.current && b.current && (E.current.scrollLeft = b.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const R = b.current;
      if (!R) return;
      const I = R.scrollTop, G = Math.ceil(R.clientHeight / s), te = Math.floor(I / s);
      t("scroll", { start: te, count: G });
    }, 80);
  }, [t, s]), J = e.useCallback((R, I, G) => {
    if (v.current) return;
    let te;
    !I || I === "desc" ? te = "asc" : te = "desc";
    const T = G.shiftKey ? "add" : "replace";
    t("sort", { column: R, direction: te, mode: T });
  }, [t]), B = e.useCallback((R, I) => {
    _.current = R, I.dataTransfer.effectAllowed = "move", I.dataTransfer.setData("text/plain", R);
  }, []), M = e.useCallback((R, I) => {
    if (!_.current || _.current === R) {
      H(null);
      return;
    }
    I.preventDefault(), I.dataTransfer.dropEffect = "move";
    const G = I.currentTarget.getBoundingClientRect(), te = I.clientX < G.left + G.width / 2 ? "left" : "right";
    H({ column: R, side: te });
  }, []), Q = e.useCallback((R) => {
    R.preventDefault(), R.stopPropagation();
    const I = _.current;
    if (!I || !g) {
      _.current = null, H(null);
      return;
    }
    let G = n.findIndex((T) => T.name === g.column);
    if (G < 0) {
      _.current = null, H(null);
      return;
    }
    const te = n.findIndex((T) => T.name === I);
    g.side === "right" && G++, te < G && G--, t("columnReorder", { column: I, targetIndex: G }), _.current = null, H(null);
  }, [n, g, t]), d = e.useCallback(() => {
    _.current = null, H(null);
  }, []), y = e.useCallback((R, I) => {
    I.shiftKey && I.preventDefault(), t("select", {
      rowIndex: R,
      ctrlKey: I.ctrlKey || I.metaKey,
      shiftKey: I.shiftKey
    });
  }, [t]), F = e.useCallback((R, I) => {
    I.stopPropagation(), t("select", { rowIndex: R, ctrlKey: !0, shiftKey: !1 });
  }, [t]), j = e.useCallback(() => {
    const R = a === c && c > 0;
    t("selectAll", { selected: !R });
  }, [t, a, c]), L = e.useCallback((R, I, G) => {
    G.stopPropagation(), t("expand", { rowIndex: R, expanded: I });
  }, [t]), K = e.useCallback((R, I) => {
    I.preventDefault(), S({ x: I.clientX, y: I.clientY, colIdx: R });
  }, []), Y = e.useCallback(() => {
    w && (t("setFrozenColumnCount", { count: w.colIdx + 1 }), S(null));
  }, [w, t]), le = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), S(null);
  }, [t]);
  e.useEffect(() => {
    if (!w) return;
    const R = () => S(null), I = (G) => {
      G.key === "Escape" && S(null);
    };
    return document.addEventListener("mousedown", R), document.addEventListener("keydown", I), () => {
      document.removeEventListener("mousedown", R), document.removeEventListener("keydown", I);
    };
  }, [w]);
  const ee = n.reduce((R, I) => R + $(I), 0) + (f ? C : 0), ue = a === c && c > 0, me = a > 0 && a < c, he = e.useCallback((R) => {
    R && (R.indeterminate = me);
  }, [me]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (R) => {
        if (!_.current) return;
        R.preventDefault();
        const I = b.current, G = E.current;
        if (!I) return;
        const te = I.getBoundingClientRect(), T = 40, A = 8;
        R.clientX < te.left + T ? I.scrollLeft = Math.max(0, I.scrollLeft - A) : R.clientX > te.right - T && (I.scrollLeft += A), G && (G.scrollLeft = I.scrollLeft);
      },
      onDrop: Q
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: E }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ee } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (o > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: C,
          minWidth: C,
          ...o > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (R) => {
          _.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== _.current && H({ column: n[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: he,
          className: "tlTableView__checkbox",
          checked: ue,
          onChange: j
        }
      )
    ), n.map((R, I) => {
      const G = $(R);
      n.length - 1;
      let te = "tlTableView__headerCell";
      R.sortable && (te += " tlTableView__headerCell--sortable"), g && g.column === R.name && (te += " tlTableView__headerCell--dragOver-" + g.side);
      const T = I < o, A = I === o - 1;
      return T && (te += " tlTableView__headerCell--frozen"), A && (te += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.name,
          className: te,
          style: {
            width: G,
            minWidth: G,
            position: T ? "sticky" : "relative",
            ...T ? { left: x[I], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: R.sortable ? (U) => J(R.name, R.sortDirection, U) : void 0,
          onContextMenu: (U) => K(I, U),
          onDragStart: (U) => B(R.name, U),
          onDragOver: (U) => M(R.name, U),
          onDrop: Q,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, R.label),
        R.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, R.sortDirection === "asc" ? "▲" : "▼", m > 1 && R.sortPriority != null && R.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, R.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (U) => re(R.name, G, U)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (R) => {
          if (_.current && n.length > 0) {
            const I = n[n.length - 1];
            I.name !== _.current && (R.preventDefault(), R.dataTransfer.dropEffect = "move", H({ column: I.name, side: "right" }));
          }
        },
        onDrop: Q
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: b,
        className: "tlTableView__body",
        onScroll: W
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", width: ee } }, u.map((R) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: R.id,
          className: "tlTableView__row" + (R.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: R.index * s,
            height: s,
            width: ee
          },
          onClick: (I) => y(R.index, I)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (o > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: C,
              minWidth: C,
              ...o > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (I) => I.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: R.selected,
              onChange: () => {
              },
              onClick: (I) => F(R.index, I),
              tabIndex: -1
            }
          )
        ),
        n.map((I, G) => {
          const te = $(I), T = G === n.length - 1, A = G < o, U = G === o - 1;
          let ne = "tlTableView__cell";
          A && (ne += " tlTableView__cell--frozen"), U && (ne += " tlTableView__cell--frozenLast");
          const _e = p && G === 0, ae = R.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: I.name,
              className: ne,
              style: {
                ...T && !A ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...A ? { position: "sticky", left: x[G], zIndex: 2 } : {}
              }
            },
            _e ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * h } }, R.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => L(R.index, !R.expanded, pe)
              },
              R.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(q, { control: R.cells[I.name] })) : /* @__PURE__ */ e.createElement(q, { control: R.cells[I.name] })
          );
        })
      )))
    ),
    w && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: w.y, left: w.x, zIndex: 1e4 },
        onMouseDown: (R) => R.stopPropagation()
      },
      w.colIdx + 1 !== o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: Y }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      o > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: le }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Xn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, _t = e.createContext(Xn), { useMemo: qn, useRef: Zn, useState: Qn, useEffect: Jn } = e, el = 320, tl = ({ controlId: l }) => {
  const t = Z(), r = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, u = t.children ?? [], s = t.noModelMessage, i = Zn(null), [a, o] = Qn(
    n === "top" ? "top" : "side"
  );
  Jn(() => {
    if (n !== "auto") {
      o(n);
      return;
    }
    const h = i.current;
    if (!h) return;
    const E = new ResizeObserver((b) => {
      for (const N of b) {
        const D = N.contentRect.width / r;
        o(D < el ? "top" : "side");
      }
    });
    return E.observe(h), () => E.disconnect();
  }, [n, r]);
  const p = qn(() => ({
    readOnly: c,
    resolvedLabelPosition: a
  }), [c, a]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, C = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: i }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, s)) : /* @__PURE__ */ e.createElement(_t.Provider, { value: p }, /* @__PURE__ */ e.createElement("div", { id: l, className: C, style: f, ref: i }, u.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h }))));
}, { useCallback: nl } = e, ll = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, rl = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = se(ll), c = t.header, u = t.headerActions ?? [], s = t.collapsible === !0, i = t.collapsed === !0, a = t.border ?? "none", o = t.fullLine === !0, p = t.children ?? [], m = c != null || u.length > 0 || s, f = nl(() => {
    r("toggleCollapse");
  }, [r]), C = [
    "tlFormGroup",
    `tlFormGroup--border-${a}`,
    o ? "tlFormGroup--fullLine" : "",
    i ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: C }, m && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, s && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
      "aria-expanded": !i,
      title: i ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
    },
    /* @__PURE__ */ e.createElement(
      "svg",
      {
        viewBox: "0 0 16 16",
        width: "14",
        height: "14",
        "aria-hidden": "true",
        className: i ? "tlFormGroup__chevron--collapsed" : "tlFormGroup__chevron"
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), u.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, u.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, p.map((h, E) => /* @__PURE__ */ e.createElement(q, { key: E, control: h }))));
}, { useContext: ol, useState: al, useCallback: sl } = e, cl = ({ controlId: l }) => {
  const t = Z(), r = ol(_t), n = t.label ?? "", c = t.required === !0, u = t.error, s = t.warnings, i = t.helpText, a = t.dirty === !0, o = t.labelPosition ?? r.resolvedLabelPosition, p = t.fullLine === !0, m = t.visible !== !1, f = t.field, C = r.readOnly, [h, E] = al(!1), b = sl(() => E((P) => !P), []);
  if (!m) return null;
  const N = u != null, k = s != null && s.length > 0, D = [
    "tlFormField",
    `tlFormField--${o}`,
    C ? "tlFormField--readonly" : "",
    p ? "tlFormField--fullLine" : "",
    N ? "tlFormField--error" : "",
    !N && k ? "tlFormField--warning" : "",
    a ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: D }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !C && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), a && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), i && !C && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: b,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(q, { control: f })), !C && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, u)), !C && !N && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, s.map((P, v) => /* @__PURE__ */ e.createElement("div", { key: v, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, P)))), !C && i && h && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, i));
}, il = () => {
  const l = Z(), t = oe(), r = l.iconCss, n = l.iconSrc, c = l.label, u = l.cssClass, s = l.tooltip, i = l.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, o = /* @__PURE__ */ e.createElement(e.Fragment, null, a, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), p = e.useCallback((f) => {
    f.preventDefault(), t("goto", {});
  }, [t]), m = ["tlResourceCell", u].filter(Boolean).join(" ");
  return i ? /* @__PURE__ */ e.createElement("a", { className: m, href: "#", onClick: p, title: s }, o) : /* @__PURE__ */ e.createElement("span", { className: m, title: s }, o);
}, ul = 20, dl = () => {
  const l = Z(), t = oe(), r = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, u = l.dropEnabled ?? !1, s = l.dropIndicatorNodeId ?? null, i = l.dropIndicatorPosition ?? null, [a, o] = e.useState(-1), p = e.useRef(null), m = e.useCallback((v, _) => {
    t(_ ? "collapse" : "expand", { nodeId: v });
  }, [t]), f = e.useCallback((v, _) => {
    t("select", {
      nodeId: v,
      ctrlKey: _.ctrlKey || _.metaKey,
      shiftKey: _.shiftKey
    });
  }, [t]), C = e.useCallback((v, _) => {
    _.preventDefault(), t("contextMenu", { nodeId: v, x: _.clientX, y: _.clientY });
  }, [t]), h = e.useRef(null), E = e.useCallback((v, _) => {
    const g = _.getBoundingClientRect(), H = v.clientY - g.top, w = g.height / 3;
    return H < w ? "above" : H > w * 2 ? "below" : "within";
  }, []), b = e.useCallback((v, _) => {
    _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", v);
  }, []), N = e.useCallback((v, _) => {
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const g = E(_, _.currentTarget);
    h.current != null && window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      t("dragOver", { nodeId: v, position: g }), h.current = null;
    }, 50);
  }, [t, E]), k = e.useCallback((v, _) => {
    _.preventDefault(), h.current != null && (window.clearTimeout(h.current), h.current = null);
    const g = E(_, _.currentTarget);
    t("drop", { nodeId: v, position: g });
  }, [t, E]), D = e.useCallback(() => {
    h.current != null && (window.clearTimeout(h.current), h.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((v) => {
    if (r.length === 0) return;
    let _ = a;
    switch (v.key) {
      case "ArrowDown":
        v.preventDefault(), _ = Math.min(a + 1, r.length - 1);
        break;
      case "ArrowUp":
        v.preventDefault(), _ = Math.max(a - 1, 0);
        break;
      case "ArrowRight":
        if (v.preventDefault(), a >= 0 && a < r.length) {
          const g = r[a];
          if (g.expandable && !g.expanded) {
            t("expand", { nodeId: g.id });
            return;
          } else g.expanded && (_ = a + 1);
        }
        break;
      case "ArrowLeft":
        if (v.preventDefault(), a >= 0 && a < r.length) {
          const g = r[a];
          if (g.expanded) {
            t("collapse", { nodeId: g.id });
            return;
          } else {
            const H = g.depth;
            for (let w = a - 1; w >= 0; w--)
              if (r[w].depth < H) {
                _ = w;
                break;
              }
          }
        }
        break;
      case "Enter":
        v.preventDefault(), a >= 0 && a < r.length && t("select", {
          nodeId: r[a].id,
          ctrlKey: v.ctrlKey || v.metaKey,
          shiftKey: v.shiftKey
        });
        return;
      case " ":
        v.preventDefault(), n === "multi" && a >= 0 && a < r.length && t("select", {
          nodeId: r[a].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        v.preventDefault(), _ = 0;
        break;
      case "End":
        v.preventDefault(), _ = r.length - 1;
        break;
      default:
        return;
    }
    _ !== a && o(_);
  }, [a, r, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: p,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    r.map((v, _) => /* @__PURE__ */ e.createElement(
      "li",
      {
        key: v.id,
        role: "treeitem",
        "aria-expanded": v.expandable ? v.expanded : void 0,
        "aria-selected": v.selected,
        "aria-level": v.depth + 1,
        className: [
          "tlTreeView__node",
          v.selected ? "tlTreeView__node--selected" : "",
          _ === a ? "tlTreeView__node--focused" : "",
          s === v.id && i === "above" ? "tlTreeView__node--drop-above" : "",
          s === v.id && i === "within" ? "tlTreeView__node--drop-within" : "",
          s === v.id && i === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: v.depth * ul },
        draggable: c,
        onClick: (g) => f(v.id, g),
        onContextMenu: (g) => C(v.id, g),
        onDragStart: (g) => b(v.id, g),
        onDragOver: u ? (g) => N(v.id, g) : void 0,
        onDrop: u ? (g) => k(v.id, g) : void 0,
        onDragEnd: D
      },
      v.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (g) => {
            g.stopPropagation(), m(v.id, v.expanded);
          },
          tabIndex: -1,
          "aria-label": v.expanded ? "Collapse" : "Expand"
        },
        v.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: v.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(q, { control: v.content }))
    ))
  );
};
var We = { exports: {} }, ce = {}, ze = { exports: {} }, V = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var ct;
function ml() {
  if (ct) return V;
  ct = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), u = Symbol.for("react.consumer"), s = Symbol.for("react.context"), i = Symbol.for("react.forward_ref"), a = Symbol.for("react.suspense"), o = Symbol.for("react.memo"), p = Symbol.for("react.lazy"), m = Symbol.for("react.activity"), f = Symbol.iterator;
  function C(d) {
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
  }, E = Object.assign, b = {};
  function N(d, y, F) {
    this.props = d, this.context = y, this.refs = b, this.updater = F || h;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(d, y) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, y, "setState");
  }, N.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function k() {
  }
  k.prototype = N.prototype;
  function D(d, y, F) {
    this.props = d, this.context = y, this.refs = b, this.updater = F || h;
  }
  var P = D.prototype = new k();
  P.constructor = D, E(P, N.prototype), P.isPureReactComponent = !0;
  var v = Array.isArray;
  function _() {
  }
  var g = { H: null, A: null, T: null, S: null }, H = Object.prototype.hasOwnProperty;
  function w(d, y, F) {
    var j = F.ref;
    return {
      $$typeof: l,
      type: d,
      key: y,
      ref: j !== void 0 ? j : null,
      props: F
    };
  }
  function S(d, y) {
    return w(d.type, y, d.props);
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
  var O = /\/+/g;
  function X(d, y) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : y.toString(36);
  }
  function re(d) {
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
  function W(d, y, F, j, L) {
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
              return Y = d._init, W(
                Y(d._payload),
                y,
                F,
                j,
                L
              );
          }
      }
    if (Y)
      return L = L(d), Y = j === "" ? "." + X(d, 0) : j, v(L) ? (F = "", Y != null && (F = Y.replace(O, "$&/") + "/"), W(L, y, F, "", function(ue) {
        return ue;
      })) : L != null && ($(L) && (L = S(
        L,
        F + (L.key == null || d && d.key === L.key ? "" : ("" + L.key).replace(
          O,
          "$&/"
        ) + "/") + Y
      )), y.push(L)), 1;
    Y = 0;
    var le = j === "" ? "." : j + ":";
    if (v(d))
      for (var ee = 0; ee < d.length; ee++)
        j = d[ee], K = le + X(j, ee), Y += W(
          j,
          y,
          F,
          K,
          L
        );
    else if (ee = C(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(j = d.next()).done; )
        j = j.value, K = le + X(j, ee++), Y += W(
          j,
          y,
          F,
          K,
          L
        );
    else if (K === "object") {
      if (typeof d.then == "function")
        return W(
          re(d),
          y,
          F,
          j,
          L
        );
      throw y = String(d), Error(
        "Objects are not valid as a React child (found: " + (y === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : y) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Y;
  }
  function J(d, y, F) {
    if (d == null) return d;
    var j = [], L = 0;
    return W(d, j, "", "", function(K) {
      return y.call(F, K, L++);
    }), j;
  }
  function B(d) {
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
  var M = typeof reportError == "function" ? reportError : function(d) {
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
  return V.Activity = m, V.Children = Q, V.Component = N, V.Fragment = r, V.Profiler = c, V.PureComponent = D, V.StrictMode = n, V.Suspense = a, V.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = g, V.__COMPILER_RUNTIME = {
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
    var j = E({}, d.props), L = d.key;
    if (y != null)
      for (K in y.key !== void 0 && (L = "" + y.key), y)
        !H.call(y, K) || K === "key" || K === "__self" || K === "__source" || K === "ref" && y.ref === void 0 || (j[K] = y[K]);
    var K = arguments.length - 2;
    if (K === 1) j.children = F;
    else if (1 < K) {
      for (var Y = Array(K), le = 0; le < K; le++)
        Y[le] = arguments[le + 2];
      j.children = Y;
    }
    return w(d.type, L, j);
  }, V.createContext = function(d) {
    return d = {
      $$typeof: s,
      _currentValue: d,
      _currentValue2: d,
      _threadCount: 0,
      Provider: null,
      Consumer: null
    }, d.Provider = d, d.Consumer = {
      $$typeof: u,
      _context: d
    }, d;
  }, V.createElement = function(d, y, F) {
    var j, L = {}, K = null;
    if (y != null)
      for (j in y.key !== void 0 && (K = "" + y.key), y)
        H.call(y, j) && j !== "key" && j !== "__self" && j !== "__source" && (L[j] = y[j]);
    var Y = arguments.length - 2;
    if (Y === 1) L.children = F;
    else if (1 < Y) {
      for (var le = Array(Y), ee = 0; ee < Y; ee++)
        le[ee] = arguments[ee + 2];
      L.children = le;
    }
    if (d && d.defaultProps)
      for (j in Y = d.defaultProps, Y)
        L[j] === void 0 && (L[j] = Y[j]);
    return w(d, K, L);
  }, V.createRef = function() {
    return { current: null };
  }, V.forwardRef = function(d) {
    return { $$typeof: i, render: d };
  }, V.isValidElement = $, V.lazy = function(d) {
    return {
      $$typeof: p,
      _payload: { _status: -1, _result: d },
      _init: B
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
      var j = d(), L = g.S;
      L !== null && L(F, j), typeof j == "object" && j !== null && typeof j.then == "function" && j.then(_, M);
    } catch (K) {
      M(K);
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
var it;
function pl() {
  return it || (it = 1, ze.exports = ml()), ze.exports;
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
var ut;
function fl() {
  if (ut) return ce;
  ut = 1;
  var l = pl();
  function t(a) {
    var o = "https://react.dev/errors/" + a;
    if (1 < arguments.length) {
      o += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var p = 2; p < arguments.length; p++)
        o += "&args[]=" + encodeURIComponent(arguments[p]);
    }
    return "Minified React error #" + a + "; visit " + o + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function r() {
  }
  var n = {
    d: {
      f: r,
      r: function() {
        throw Error(t(522));
      },
      D: r,
      C: r,
      L: r,
      m: r,
      X: r,
      S: r,
      M: r
    },
    p: 0,
    findDOMNode: null
  }, c = Symbol.for("react.portal");
  function u(a, o, p) {
    var m = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: m == null ? null : "" + m,
      children: a,
      containerInfo: o,
      implementation: p
    };
  }
  var s = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function i(a, o) {
    if (a === "font") return "";
    if (typeof o == "string")
      return o === "use-credentials" ? o : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, ce.createPortal = function(a, o) {
    var p = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!o || o.nodeType !== 1 && o.nodeType !== 9 && o.nodeType !== 11)
      throw Error(t(299));
    return u(a, o, null, p);
  }, ce.flushSync = function(a) {
    var o = s.T, p = n.p;
    try {
      if (s.T = null, n.p = 2, a) return a();
    } finally {
      s.T = o, n.p = p, n.d.f();
    }
  }, ce.preconnect = function(a, o) {
    typeof a == "string" && (o ? (o = o.crossOrigin, o = typeof o == "string" ? o === "use-credentials" ? o : "" : void 0) : o = null, n.d.C(a, o));
  }, ce.prefetchDNS = function(a) {
    typeof a == "string" && n.d.D(a);
  }, ce.preinit = function(a, o) {
    if (typeof a == "string" && o && typeof o.as == "string") {
      var p = o.as, m = i(p, o.crossOrigin), f = typeof o.integrity == "string" ? o.integrity : void 0, C = typeof o.fetchPriority == "string" ? o.fetchPriority : void 0;
      p === "style" ? n.d.S(
        a,
        typeof o.precedence == "string" ? o.precedence : void 0,
        {
          crossOrigin: m,
          integrity: f,
          fetchPriority: C
        }
      ) : p === "script" && n.d.X(a, {
        crossOrigin: m,
        integrity: f,
        fetchPriority: C,
        nonce: typeof o.nonce == "string" ? o.nonce : void 0
      });
    }
  }, ce.preinitModule = function(a, o) {
    if (typeof a == "string")
      if (typeof o == "object" && o !== null) {
        if (o.as == null || o.as === "script") {
          var p = i(
            o.as,
            o.crossOrigin
          );
          n.d.M(a, {
            crossOrigin: p,
            integrity: typeof o.integrity == "string" ? o.integrity : void 0,
            nonce: typeof o.nonce == "string" ? o.nonce : void 0
          });
        }
      } else o == null && n.d.M(a);
  }, ce.preload = function(a, o) {
    if (typeof a == "string" && typeof o == "object" && o !== null && typeof o.as == "string") {
      var p = o.as, m = i(p, o.crossOrigin);
      n.d.L(a, p, {
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
  }, ce.preloadModule = function(a, o) {
    if (typeof a == "string")
      if (o) {
        var p = i(o.as, o.crossOrigin);
        n.d.m(a, {
          as: typeof o.as == "string" && o.as !== "script" ? o.as : void 0,
          crossOrigin: p,
          integrity: typeof o.integrity == "string" ? o.integrity : void 0
        });
      } else n.d.m(a);
  }, ce.requestFormReset = function(a) {
    n.d.r(a);
  }, ce.unstable_batchedUpdates = function(a, o) {
    return a(o);
  }, ce.useFormState = function(a, o, p) {
    return s.H.useFormState(a, o, p);
  }, ce.useFormStatus = function() {
    return s.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var dt;
function hl() {
  if (dt) return We.exports;
  dt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), We.exports = fl(), We.exports;
}
var _l = hl();
const { useState: be, useCallback: ie, useRef: Ne, useEffect: Ce, useMemo: Xe } = e;
function Je({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function bl({
  option: l,
  removable: t,
  onRemove: r,
  removeLabel: n,
  draggable: c,
  onDragStart: u,
  onDragOver: s,
  onDrop: i,
  onDragEnd: a,
  dragClassName: o
}) {
  const p = ie(
    (m) => {
      m.stopPropagation(), r(l.value);
    },
    [r, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (o ? " " + o : ""),
      draggable: c || void 0,
      onDragStart: u,
      onDragOver: s,
      onDrop: i,
      onDragEnd: a
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: p,
        "aria-label": n
      },
      "×"
    )
  );
}
function vl({
  option: l,
  highlighted: t,
  searchTerm: r,
  onSelect: n,
  onMouseEnter: c,
  id: u
}) {
  const s = ie(() => n(l.value), [n, l.value]), i = Xe(() => {
    if (!r) return l.label;
    const a = l.label.toLowerCase().indexOf(r.toLowerCase());
    return a < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, a), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(a, a + r.length)), l.label.substring(a + r.length));
  }, [l.label, r]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: u,
      role: "option",
      "aria-selected": t,
      className: "tlDropdownSelect__option" + (t ? " tlDropdownSelect__option--highlighted" : ""),
      onClick: s,
      onMouseEnter: c
    },
    /* @__PURE__ */ e.createElement(Je, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, i)
  );
}
const El = ({ controlId: l, state: t }) => {
  const r = oe(), n = t.value ?? [], c = t.multiSelect === !0, u = t.customOrder === !0, s = t.mandatory === !0, i = t.disabled === !0, a = t.editable !== !1, o = t.optionsLoaded === !0, p = t.options ?? [], m = t.emptyOptionLabel ?? "", f = u && c && !i && a, C = se({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), h = C["js.dropdownSelect.nothingFound"], E = ie(
    (T) => C["js.dropdownSelect.removeChip"].replace("{0}", T),
    [C]
  ), [b, N] = be(!1), [k, D] = be(""), [P, v] = be(-1), [_, g] = be(!1), [H, w] = be({}), [S, $] = be(null), [x, O] = be(null), [X, re] = be(null), W = Ne(null), J = Ne(null), B = Ne(null), M = Ne(n);
  M.current = n;
  const Q = Ne(-1), d = Xe(
    () => new Set(n.map((T) => T.value)),
    [n]
  ), y = Xe(() => {
    let T = p.filter((A) => !d.has(A.value));
    if (k) {
      const A = k.toLowerCase();
      T = T.filter((U) => U.label.toLowerCase().includes(A));
    }
    return T;
  }, [p, d, k]);
  Ce(() => {
    k && y.length === 1 ? v(0) : v(-1);
  }, [y.length, k]), Ce(() => {
    b && o && J.current && J.current.focus();
  }, [b, o, n]), Ce(() => {
    var U, ne;
    if (Q.current < 0) return;
    const T = Q.current;
    Q.current = -1;
    const A = (U = W.current) == null ? void 0 : U.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    A && A.length > 0 ? A[Math.min(T, A.length - 1)].focus() : (ne = W.current) == null || ne.focus();
  }, [n]), Ce(() => {
    if (!b) return;
    const T = (A) => {
      W.current && !W.current.contains(A.target) && B.current && !B.current.contains(A.target) && (N(!1), D(""));
    };
    return document.addEventListener("mousedown", T), () => document.removeEventListener("mousedown", T);
  }, [b]), Ce(() => {
    if (!b || !W.current) return;
    const T = W.current.getBoundingClientRect(), A = window.innerHeight - T.bottom, ne = A < 300 && T.top > A;
    w({
      left: T.left,
      width: T.width,
      ...ne ? { bottom: window.innerHeight - T.top } : { top: T.bottom }
    });
  }, [b]);
  const F = ie(async () => {
    if (!(i || !a) && (N(!0), D(""), v(-1), g(!1), !o))
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
  }, [i, a, o, r]), j = ie(() => {
    var T;
    N(!1), D(""), v(-1), (T = W.current) == null || T.focus();
  }, []), L = ie(
    (T) => {
      let A;
      if (c) {
        const U = p.find((ne) => ne.value === T);
        if (U)
          A = [...M.current, U];
        else
          return;
      } else {
        const U = p.find((ne) => ne.value === T);
        if (U)
          A = [U];
        else
          return;
      }
      M.current = A, r("valueChanged", { value: A.map((U) => U.value) }), c ? (D(""), v(-1)) : j();
    },
    [c, p, r, j]
  ), K = ie(
    (T) => {
      Q.current = M.current.findIndex((U) => U.value === T);
      const A = M.current.filter((U) => U.value !== T);
      M.current = A, r("valueChanged", { value: A.map((U) => U.value) });
    },
    [r]
  ), Y = ie(
    (T) => {
      T.stopPropagation(), r("valueChanged", { value: [] }), j();
    },
    [r, j]
  ), le = ie((T) => {
    D(T.target.value);
  }, []), ee = ie(
    (T) => {
      if (!b) {
        if (T.key === "ArrowDown" || T.key === "ArrowUp" || T.key === "Enter" || T.key === " ") {
          if (T.target.tagName === "BUTTON") return;
          T.preventDefault(), T.stopPropagation(), F();
        }
        return;
      }
      switch (T.key) {
        case "ArrowDown":
          T.preventDefault(), T.stopPropagation(), v(
            (A) => A < y.length - 1 ? A + 1 : 0
          );
          break;
        case "ArrowUp":
          T.preventDefault(), T.stopPropagation(), v(
            (A) => A > 0 ? A - 1 : y.length - 1
          );
          break;
        case "Enter":
          T.preventDefault(), T.stopPropagation(), P >= 0 && P < y.length && L(y[P].value);
          break;
        case "Escape":
          T.preventDefault(), T.stopPropagation(), j();
          break;
        case "Tab":
          j();
          break;
        case "Backspace":
          k === "" && c && n.length > 0 && K(n[n.length - 1].value);
          break;
      }
    },
    [
      b,
      F,
      j,
      y,
      P,
      L,
      k,
      c,
      n,
      K
    ]
  ), ue = ie(
    async (T) => {
      T.preventDefault(), g(!1);
      try {
        await r("loadOptions");
      } catch {
        g(!0);
      }
    },
    [r]
  ), me = ie(
    (T, A) => {
      $(T), A.dataTransfer.effectAllowed = "move", A.dataTransfer.setData("text/plain", String(T));
    },
    []
  ), he = ie(
    (T, A) => {
      if (A.preventDefault(), A.dataTransfer.dropEffect = "move", S === null || S === T) {
        O(null), re(null);
        return;
      }
      const U = A.currentTarget.getBoundingClientRect(), ne = U.left + U.width / 2, _e = A.clientX < ne ? "before" : "after";
      O(T), re(_e);
    },
    [S]
  ), R = ie(
    (T) => {
      if (T.preventDefault(), S === null || x === null || X === null || S === x) return;
      const A = [...M.current], [U] = A.splice(S, 1);
      let ne = x;
      S < x ? ne = X === "before" ? ne - 1 : ne : ne = X === "before" ? ne : ne + 1, A.splice(ne, 0, U), M.current = A, r("valueChanged", { value: A.map((_e) => _e.value) }), $(null), O(null), re(null);
    },
    [S, x, X, r]
  ), I = ie(() => {
    $(null), O(null), re(null);
  }, []);
  if (Ce(() => {
    if (P < 0 || !B.current) return;
    const T = B.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    T && T.scrollIntoView({ block: "nearest" });
  }, [P, l]), !a)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, m) : n.map((T) => /* @__PURE__ */ e.createElement("span", { key: T.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Je, { image: T.image }), /* @__PURE__ */ e.createElement("span", null, T.label))));
  const G = !s && n.length > 0 && !i, te = b ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: B,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (o || _) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
        onChange: le,
        onKeyDown: ee,
        placeholder: C["js.dropdownSelect.filterPlaceholder"],
        "aria-label": C["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": P >= 0 ? `${l}-opt-${P}` : void 0,
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
      _ && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ue }, C["js.dropdownSelect.error"])),
      o && y.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, h),
      o && y.map((T, A) => /* @__PURE__ */ e.createElement(
        vl,
        {
          key: T.value,
          id: `${l}-opt-${A}`,
          option: T,
          highlighted: A === P,
          searchTerm: k,
          onSelect: L,
          onMouseEnter: () => v(A)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: W,
      className: "tlDropdownSelect" + (b ? " tlDropdownSelect--open" : "") + (i ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": b,
      "aria-haspopup": "listbox",
      "aria-owns": b ? `${l}-listbox` : void 0,
      tabIndex: i ? -1 : 0,
      onClick: b ? void 0 : F,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, m) : n.map((T, A) => {
      let U = "";
      return S === A ? U = "tlDropdownSelect__chip--dragging" : x === A && X === "before" ? U = "tlDropdownSelect__chip--dropBefore" : x === A && X === "after" && (U = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        bl,
        {
          key: T.value,
          option: T,
          removable: !i && (c || !s),
          onRemove: K,
          removeLabel: E(T.label),
          draggable: f,
          onDragStart: f ? (ne) => me(A, ne) : void 0,
          onDragOver: f ? (ne) => he(A, ne) : void 0,
          onDrop: f ? R : void 0,
          onDragEnd: f ? I : void 0,
          dragClassName: f ? U : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, G && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Y,
        "aria-label": C["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, b ? "▲" : "▼"))
  ), te && _l.createPortal(te, document.body));
}, { useCallback: Ue, useRef: gl } = e, bt = "application/x-tl-color", Cl = ({
  colors: l,
  columns: t,
  onSelect: r,
  onConfirm: n,
  onSwap: c,
  onReplace: u
}) => {
  const s = gl(null), i = Ue(
    (p) => (m) => {
      s.current = p, m.dataTransfer.effectAllowed = "move";
    },
    []
  ), a = Ue((p) => {
    p.preventDefault(), p.dataTransfer.dropEffect = "move";
  }, []), o = Ue(
    (p) => (m) => {
      m.preventDefault();
      const f = m.dataTransfer.getData(bt);
      f ? u(p, f) : s.current !== null && s.current !== p && c(s.current, p), s.current = null;
    },
    [c, u]
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
        onClick: p != null ? () => r(p) : void 0,
        onDoubleClick: p != null ? () => n(p) : void 0,
        onDragStart: p != null ? i(m) : void 0,
        onDragOver: a,
        onDrop: o(m)
      }
    ))
  );
};
function vt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function qe(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function Et(l) {
  if (!qe(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function gt(l, t, r) {
  const n = (c) => vt(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(r);
}
function yl(l, t, r) {
  const n = l / 255, c = t / 255, u = r / 255, s = Math.max(n, c, u), i = Math.min(n, c, u), a = s - i;
  let o = 0;
  a !== 0 && (s === n ? o = (c - u) / a % 6 : s === c ? o = (u - n) / a + 2 : o = (n - c) / a + 4, o *= 60, o < 0 && (o += 360));
  const p = s === 0 ? 0 : a / s;
  return [o, p, s];
}
function wl(l, t, r) {
  const n = r * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), u = r - n;
  let s = 0, i = 0, a = 0;
  return l < 60 ? (s = n, i = c, a = 0) : l < 120 ? (s = c, i = n, a = 0) : l < 180 ? (s = 0, i = n, a = c) : l < 240 ? (s = 0, i = c, a = n) : l < 300 ? (s = c, i = 0, a = n) : (s = n, i = 0, a = c), [
    Math.round((s + u) * 255),
    Math.round((i + u) * 255),
    Math.round((a + u) * 255)
  ];
}
function kl(l) {
  return yl(...Et(l));
}
function Ve(l, t, r) {
  return gt(...wl(l, t, r));
}
const { useCallback: ye, useRef: mt } = e, Sl = ({ color: l, onColorChange: t }) => {
  const [r, n, c] = kl(l), u = mt(null), s = mt(null), i = ye(
    (h, E) => {
      var D;
      const b = (D = u.current) == null ? void 0 : D.getBoundingClientRect();
      if (!b) return;
      const N = Math.max(0, Math.min(1, (h - b.left) / b.width)), k = Math.max(0, Math.min(1, 1 - (E - b.top) / b.height));
      t(Ve(r, N, k));
    },
    [r, t]
  ), a = ye(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), i(h.clientX, h.clientY);
    },
    [i]
  ), o = ye(
    (h) => {
      h.buttons !== 0 && i(h.clientX, h.clientY);
    },
    [i]
  ), p = ye(
    (h) => {
      var k;
      const E = (k = s.current) == null ? void 0 : k.getBoundingClientRect();
      if (!E) return;
      const N = Math.max(0, Math.min(1, (h - E.top) / E.height)) * 360;
      t(Ve(N, n, c));
    },
    [n, c, t]
  ), m = ye(
    (h) => {
      h.preventDefault(), h.target.setPointerCapture(h.pointerId), p(h.clientY);
    },
    [p]
  ), f = ye(
    (h) => {
      h.buttons !== 0 && p(h.clientY);
    },
    [p]
  ), C = Ve(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: u,
      className: "tlColorInput__svField",
      style: { backgroundColor: C },
      onPointerDown: a,
      onPointerMove: o
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${n * 100}%`, top: `${(1 - c) * 100}%` }
      }
    )
  ), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: s,
      className: "tlColorInput__hueSlider",
      onPointerDown: m,
      onPointerMove: f
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__hueHandle",
        style: { top: `${r / 360 * 100}%` }
      }
    )
  ));
};
function Nl(l, t) {
  const r = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === r);
}
const Rl = {
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
}, { useState: De, useCallback: fe, useEffect: Ke, useRef: Tl, useLayoutEffect: xl } = e, Ll = ({
  anchorRef: l,
  currentColor: t,
  palette: r,
  paletteColumns: n,
  defaultPalette: c,
  canReset: u,
  onConfirm: s,
  onCancel: i,
  onPaletteChange: a
}) => {
  const [o, p] = De("palette"), [m, f] = De(t), C = Tl(null), h = se(Rl), [E, b] = De(null);
  xl(() => {
    if (!l.current || !C.current) return;
    const B = l.current.getBoundingClientRect(), M = C.current.getBoundingClientRect();
    let Q = B.bottom + 4, d = B.left;
    Q + M.height > window.innerHeight && (Q = B.top - M.height - 4), d + M.width > window.innerWidth && (d = Math.max(0, B.right - M.width)), b({ top: Q, left: d });
  }, [l]);
  const N = m != null, [k, D, P] = N ? Et(m) : [0, 0, 0], [v, _] = De((m == null ? void 0 : m.toUpperCase()) ?? "");
  Ke(() => {
    _((m == null ? void 0 : m.toUpperCase()) ?? "");
  }, [m]), Ke(() => {
    const B = (M) => {
      M.key === "Escape" && i();
    };
    return document.addEventListener("keydown", B), () => document.removeEventListener("keydown", B);
  }, [i]), Ke(() => {
    const B = (Q) => {
      C.current && !C.current.contains(Q.target) && i();
    }, M = setTimeout(() => document.addEventListener("mousedown", B), 0);
    return () => {
      clearTimeout(M), document.removeEventListener("mousedown", B);
    };
  }, [i]);
  const g = fe(
    (B) => (M) => {
      const Q = parseInt(M.target.value, 10);
      if (isNaN(Q)) return;
      const d = vt(Q);
      f(gt(B === "r" ? d : k, B === "g" ? d : D, B === "b" ? d : P));
    },
    [k, D, P]
  ), H = fe(
    (B) => {
      if (m != null) {
        B.dataTransfer.setData(bt, m.toUpperCase()), B.dataTransfer.effectAllowed = "move";
        const M = document.createElement("div");
        M.style.width = "33px", M.style.height = "33px", M.style.backgroundColor = m, M.style.borderRadius = "3px", M.style.border = "1px solid rgba(0,0,0,0.1)", M.style.position = "absolute", M.style.top = "-9999px", document.body.appendChild(M), B.dataTransfer.setDragImage(M, 16, 16), requestAnimationFrame(() => document.body.removeChild(M));
      }
    },
    [m]
  ), w = fe((B) => {
    const M = B.target.value;
    _(M), qe(M) && f(M);
  }, []), S = fe(() => {
    f(null);
  }, []), $ = fe((B) => {
    f(B);
  }, []), x = fe(
    (B) => {
      s(B);
    },
    [s]
  ), O = fe(
    (B, M) => {
      const Q = [...r], d = Q[B];
      Q[B] = Q[M], Q[M] = d, a(Q);
    },
    [r, a]
  ), X = fe(
    (B, M) => {
      const Q = [...r];
      Q[B] = M, a(Q);
    },
    [r, a]
  ), re = fe(() => {
    a([...c]);
  }, [c, a]), W = fe(
    (B) => {
      if (Nl(r, B)) return;
      const M = r.indexOf(null);
      if (M < 0) return;
      const Q = [...r];
      Q[M] = B.toUpperCase(), a(Q);
    },
    [r, a]
  ), J = fe(() => {
    m != null && W(m), s(m);
  }, [m, s, W]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: C,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("palette")
      },
      h["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (o === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => p("mixer")
      },
      h["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, o === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      Cl,
      {
        colors: r,
        columns: n,
        onSelect: $,
        onConfirm: x,
        onSwap: O,
        onReplace: X
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: re }, h["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Sl, { color: m ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, h["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: m } : void 0,
        draggable: N,
        onDragStart: N ? H : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? k : "",
        onChange: g("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? D : "",
        onChange: g("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? P : "",
        onChange: g("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, h["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (v !== "" && !qe(v) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: v,
        onChange: w
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, u && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: S }, h["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: i }, h["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, h["js.colorInput.ok"]))
  );
}, Dl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Il, useCallback: Ie, useRef: Ml } = e, Pl = ({ controlId: l, state: t }) => {
  const r = oe(), n = se(Dl), [c, u] = Il(!1), s = Ml(null), i = t.value, a = t.editable !== !1, o = t.palette ?? [], p = t.paletteColumns ?? 6, m = t.defaultPalette ?? o, f = Ie(() => {
    a && u(!0);
  }, [a]), C = Ie(
    (b) => {
      u(!1), r("valueChanged", { value: b });
    },
    [r]
  ), h = Ie(() => {
    u(!1);
  }, []), E = Ie(
    (b) => {
      r("paletteChanged", { palette: b });
    },
    [r]
  );
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlColorInput__swatch" + (i == null ? " tlColorInput__swatch--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      onClick: f,
      disabled: t.disabled === !0,
      title: i ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    Ll,
    {
      anchorRef: s,
      currentColor: i,
      palette: o,
      paletteColumns: p,
      defaultPalette: m,
      canReset: t.canReset !== !1,
      onConfirm: C,
      onCancel: h,
      onPaletteChange: E
    }
  )) : /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: "tlColorInput tlColorInput--immutable" + (i == null ? " tlColorInput--noColor" : ""),
      style: i != null ? { backgroundColor: i } : void 0,
      title: i ?? ""
    }
  );
}, { useState: Re, useCallback: ve, useEffect: Me, useRef: pt, useLayoutEffect: jl, useMemo: Al } = e, Bl = {
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
}, Ol = ({
  anchorRef: l,
  currentValue: t,
  icons: r,
  iconsLoaded: n,
  onSelect: c,
  onCancel: u,
  onLoadIcons: s
}) => {
  const i = se(Bl), [a, o] = Re("simple"), [p, m] = Re(""), [f, C] = Re(t ?? ""), [h, E] = Re(!1), [b, N] = Re(null), k = pt(null), D = pt(null);
  jl(() => {
    if (!l.current || !k.current) return;
    const x = l.current.getBoundingClientRect(), O = k.current.getBoundingClientRect();
    let X = x.bottom + 4, re = x.left;
    X + O.height > window.innerHeight && (X = x.top - O.height - 4), re + O.width > window.innerWidth && (re = Math.max(0, x.right - O.width)), N({ top: X, left: re });
  }, [l]), Me(() => {
    !n && !h && s().catch(() => E(!0));
  }, [n, h, s]), Me(() => {
    n && D.current && D.current.focus();
  }, [n]), Me(() => {
    const x = (O) => {
      O.key === "Escape" && u();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [u]), Me(() => {
    const x = (X) => {
      k.current && !k.current.contains(X.target) && u();
    }, O = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", x);
    };
  }, [u]);
  const P = Al(() => {
    if (!p) return r;
    const x = p.toLowerCase();
    return r.filter(
      (O) => O.prefix.toLowerCase().includes(x) || O.label.toLowerCase().includes(x) || O.terms != null && O.terms.some((X) => X.includes(x))
    );
  }, [r, p]), v = ve((x) => {
    m(x.target.value);
  }, []), _ = ve(
    (x) => {
      c(x);
    },
    [c]
  ), g = ve((x) => {
    C(x);
  }, []), H = ve((x) => {
    C(x.target.value);
  }, []), w = ve(() => {
    c(f || null);
  }, [f, c]), S = ve(() => {
    c(null);
  }, [c]), $ = ve(async (x) => {
    x.preventDefault(), E(!1);
    try {
      await s();
    } catch {
      E(!0);
    }
  }, [s]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: k,
      style: b ? { top: b.top, left: b.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("simple")
      },
      i["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (a === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => o("advanced")
      },
      i["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: D,
        type: "text",
        className: "tlIconSelect__search",
        value: p,
        onChange: v,
        placeholder: i["js.iconSelect.filterPlaceholder"],
        "aria-label": i["js.iconSelect.filterPlaceholder"]
      }
    ), p && /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__resetBtn",
        onClick: () => m(""),
        title: i["js.iconSelect.clearFilter"]
      },
      "×"
    )),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlIconSelect__grid",
        role: "listbox"
      },
      !n && !h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      h && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: $ }, i["js.iconSelect.loadError"])),
      n && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, i["js.iconSelect.noResults"]),
      n && P.map(
        (x) => x.variants.map((O) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: O.encoded,
            className: "tlIconSelect__iconCell" + (O.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": O.encoded === t,
            tabIndex: 0,
            title: x.label,
            onClick: () => a === "simple" ? _(O.encoded) : g(O.encoded),
            onKeyDown: (X) => {
              (X.key === "Enter" || X.key === " ") && (X.preventDefault(), a === "simple" ? _(O.encoded) : g(O.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ke, { encoded: O.encoded })
        ))
      )
    ),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: H
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, i["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(ke, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    a === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: u }, i["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: S }, i["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: w }, i["js.iconSelect.ok"]))
  );
}, $l = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Fl, useCallback: Pe, useRef: Hl } = e, Wl = ({ controlId: l, state: t }) => {
  const r = oe(), n = se($l), [c, u] = Fl(!1), s = Hl(null), i = t.value, a = t.editable !== !1, o = t.disabled === !0, p = t.icons ?? [], m = t.iconsLoaded === !0, f = Pe(() => {
    a && !o && u(!0);
  }, [a, o]), C = Pe(
    (b) => {
      u(!1), r("valueChanged", { value: b });
    },
    [r]
  ), h = Pe(() => {
    u(!1);
  }, []), E = Pe(async () => {
    await r("loadIcons");
  }, [r]);
  return a ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: s,
      className: "tlIconSelect__swatch" + (i == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: o,
      title: i ?? "",
      "aria-label": n["js.iconSelect.chooseIcon"]
    },
    i ? /* @__PURE__ */ e.createElement(ke, { encoded: i }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Ol,
    {
      anchorRef: s,
      currentValue: i,
      icons: p,
      iconsLoaded: m,
      onSelect: C,
      onCancel: h,
      onLoadIcons: E
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, i ? /* @__PURE__ */ e.createElement(ke, { encoded: i }) : null));
}, { useCallback: we, useEffect: zl, useMemo: ft, useRef: Ul, useState: Ye } = e, Vl = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, Kl = [1, 2, 3, 4];
function Yl(l, t) {
  const r = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!r) return 16 * t;
  const n = parseFloat(r[1]), c = r[2] || "px";
  return c === "rem" || c === "em" ? n * t : n;
}
function Gl(l, t) {
  const r = Math.max(1, Math.floor(l / t));
  let n = 1;
  for (const c of Kl)
    r >= c && (n = c);
  return n;
}
function Xl(l, t) {
  const r = Vl[l] ?? 1;
  return Math.max(1, Math.round(r * t));
}
function ql(l, t) {
  const r = Math.max(1, t), n = {}, c = (m, f) => !!(n[m] && n[m][f]), u = (m, f) => {
    n[m] || (n[m] = {}), n[m][f] = !0;
  }, s = [];
  let i = 0, a = 0;
  const o = (m) => {
    let f = null;
    for (const h of s) h.rowStart === m && (f = h);
    if (!f) return;
    let C = f.colEnd;
    for (; C < r && !c(m, C); ) C++;
    if (C !== f.colEnd) {
      for (let h = f.rowStart; h < f.rowEnd; h++)
        for (let E = f.colEnd; E < C; E++) u(h, E);
      f.colEnd = C;
    }
  };
  for (const m of l) {
    const f = r <= 1 ? 1 : Math.max(1, m.rowSpan || 1);
    let C = Math.min(Xl(m.width, r), r);
    for (; c(i, a); )
      a++, a >= r && (a = 0, i++);
    let h = 0;
    for (let D = a; D < r && !c(i, D); D++)
      h++;
    if (C > h) {
      for (o(i), a = 0, i++; c(i, a); )
        a++, a >= r && (a = 0, i++);
      h = 0;
      for (let D = a; D < r && !c(i, D); D++)
        h++;
      C = Math.min(C, h);
    }
    const E = a, b = a + C, N = i, k = i + f;
    s.push({ id: m.id, colStart: E, colEnd: b, rowStart: N, rowEnd: k });
    for (let D = N; D < k; D++)
      for (let P = E; P < b; P++) u(D, P);
    a = b, a >= r && (a = 0, i++);
  }
  o(i);
  let p = 0;
  for (const m of s) m.rowEnd > p && (p = m.rowEnd);
  for (let m = 1; m < p; m++)
    for (let f = 0; f < r; f++) {
      if (c(m, f)) continue;
      const C = s.find((h) => h.rowEnd === m && h.colStart <= f && f < h.colEnd);
      if (C) {
        C.rowEnd = m + 1;
        for (let h = C.colStart; h < C.colEnd; h++) u(m, h);
      }
    }
  return s;
}
const Zl = ({ controlId: l }) => {
  const t = Z(), r = oe(), n = t.minColWidth ?? "16rem", c = (t.children ?? []).filter((_) => _ && _.id), u = Ul(null), [s, i] = Ye(1), a = t.editMode === !0;
  zl(() => {
    const _ = u.current;
    if (!_) return;
    const g = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = Yl(n, g), w = () => i(Gl(_.clientWidth, H));
    w();
    const S = new ResizeObserver(w);
    return S.observe(_), () => S.disconnect();
  }, [n]);
  const o = ft(() => ql(c, s), [c, s]), p = ft(() => {
    const _ = {};
    for (const g of o) _[g.id] = g;
    return _;
  }, [o]), [m, f] = Ye(null), [C, h] = Ye(null), E = we((_, g) => {
    if (!a) {
      _.preventDefault();
      return;
    }
    f(g), _.dataTransfer.effectAllowed = "move", _.dataTransfer.setData("text/plain", g);
  }, [a]), b = we((_, g) => {
    if (!a || !m || m === g) return;
    _.preventDefault(), _.dataTransfer.dropEffect = "move";
    const H = _.currentTarget.getBoundingClientRect(), w = _.clientX < H.left + H.width / 2;
    h((S) => S && S.id === g && S.before === w ? S : { id: g, before: w });
  }, [a, m]), N = we(() => {
  }, []), k = we((_, g, H) => {
    const w = c.map((O) => O.id), S = w.indexOf(_);
    if (S < 0) return;
    w.splice(S, 1);
    const $ = w.indexOf(g);
    if ($ < 0) {
      w.splice(S, 0, _);
      return;
    }
    const x = H ? $ : $ + 1;
    w.splice(x, 0, _), r("reorder", { order: w });
  }, [c, r]), D = we((_, g) => {
    if (!a || !m || m === g) return;
    _.preventDefault();
    const H = _.currentTarget.getBoundingClientRect(), w = _.clientX < H.left + H.width / 2;
    k(m, g, w), f(null), h(null);
  }, [a, m, k]), P = we(() => {
    f(null), h(null);
  }, []), v = {
    display: "grid",
    gridTemplateColumns: `repeat(${s}, 1fr)`,
    gap: "1rem"
  };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: u,
      className: "tlDashboard" + (a ? " tlDashboard--edit" : "")
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__grid", style: v }, c.map((_) => {
      const g = p[_.id];
      if (!g) return null;
      const H = {
        gridColumn: `${g.colStart + 1} / ${g.colEnd + 1}`,
        gridRow: `${g.rowStart + 1} / ${g.rowEnd + 1}`
      }, w = ["tlDashboard__tile"];
      return m === _.id && w.push("tlDashboard__tile--dragging"), C && C.id === _.id && w.push(C.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: _.id,
          className: w.join(" "),
          style: H,
          draggable: a,
          onDragStart: (S) => E(S, _.id),
          onDragOver: (S) => b(S, _.id),
          onDragLeave: N,
          onDrop: (S) => D(S, _.id),
          onDragEnd: P
        },
        /* @__PURE__ */ e.createElement(q, { control: _.control }),
        a && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
};
z("TLButton", jt);
z("TLToggleButton", Bt);
z("TLTextInput", kt);
z("TLNumberInput", Nt);
z("TLDatePicker", Tt);
z("TLSelect", Lt);
z("TLCheckbox", It);
z("TLTable", Mt);
z("TLCounter", Ot);
z("TLTabBar", Ft);
z("TLFieldList", Ht);
z("TLAudioRecorder", zt);
z("TLAudioPlayer", Vt);
z("TLFileUpload", Yt);
z("TLDownload", Xt);
z("TLPhotoCapture", Zt);
z("TLPhotoViewer", Jt);
z("TLSplitPanel", en);
z("TLPanel", sn);
z("TLMaximizeRoot", cn);
z("TLDeckPane", un);
z("TLSidebar", vn);
z("TLStack", En);
z("TLGrid", gn);
z("TLCard", Cn);
z("TLAppBar", yn);
z("TLBreadcrumb", kn);
z("TLBottomBar", Nn);
z("TLDialog", Tn);
z("TLDialogManager", Dn);
z("TLWindow", Pn);
z("TLDrawer", On);
z("TLSnackbar", Wn);
z("TLMenu", Un);
z("TLAppShell", Vn);
z("TLTextCell", Kn);
z("TLTableView", Gn);
z("TLFormLayout", tl);
z("TLFormGroup", rl);
z("TLFormField", cl);
z("TLResourceCell", il);
z("TLTreeView", dl);
z("TLDropdownSelect", El);
z("TLColorInput", Pl);
z("TLIconSelect", Wl);
z("TLDashboard", Zl);
