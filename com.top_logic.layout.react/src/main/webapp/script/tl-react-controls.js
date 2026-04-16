import { React as e, useTLFieldValue as Re, getComponent as Et, useTLState as Z, useTLCommand as ae, TLChild as X, useTLUpload as Xe, useI18N as se, useTLDataUrl as qe, register as W } from "tl-react-bridge";
const { useCallback: gt } = e, Ct = ({ controlId: l, state: t }) => {
  const [r, n] = Re(), c = gt(
    (a) => {
      n(a.target.value);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, r ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = t.errorMessage, s = [
    "tlReactTextInput",
    i ? "tlReactTextInput--error" : "",
    !i && o ? "tlReactTextInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: s,
      "aria-invalid": i || void 0,
      title: i && u ? u : void 0
    }
  ));
}, { useCallback: yt } = e, wt = ({ controlId: l, state: t, config: r }) => {
  const [n, c] = Re(), i = yt(
    (m) => {
      const p = m.target.value;
      c(p === "" ? null : p);
    },
    [c]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, n != null ? String(n) : "");
  const o = t.hasError === !0, u = t.hasWarnings === !0, s = t.errorMessage, a = [
    "tlReactNumberInput",
    o ? "tlReactNumberInput--error" : "",
    !o && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: r != null && r.decimal ? "decimal" : "numeric",
      value: n != null ? String(n) : "",
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": o || void 0,
      title: o && s ? s : void 0
    }
  ));
}, { useCallback: kt } = e, St = ({ controlId: l, state: t }) => {
  const [r, n] = Re(), c = kt(
    (s) => {
      n(s.target.value || null);
    },
    [n]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r ?? "");
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactDatePicker",
    i ? "tlReactDatePicker--error" : "",
    !i && o ? "tlReactDatePicker--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: r ?? "",
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  ));
}, { useCallback: Nt } = e, Rt = ({ controlId: l, state: t, config: r }) => {
  var m;
  const [n, c] = Re(), i = Nt(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), o = t.options ?? (r == null ? void 0 : r.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = o.find((h) => h.value === n)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, s = t.hasWarnings === !0, a = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && s ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: i,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    o.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: Tt } = e, xt = ({ controlId: l, state: t }) => {
  const [r, n] = Re(), c = Tt(
    (s) => {
      n(s.target.checked);
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
  const i = t.hasError === !0, o = t.hasWarnings === !0, u = [
    "tlReactCheckbox",
    i ? "tlReactCheckbox--error" : "",
    !i && o ? "tlReactCheckbox--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      id: l,
      checked: r === !0,
      onChange: c,
      disabled: t.disabled === !0,
      className: u,
      "aria-invalid": i || void 0
    }
  );
}, Lt = ({ controlId: l, state: t }) => {
  const r = t.columns ?? [], n = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, r.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, r.map((o) => {
    const u = o.cellModule ? Et(o.cellModule) : void 0, s = c[o.name];
    if (u) {
      const a = { value: s, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + i + "-" + o.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, s != null ? String(s) : "");
  })))));
};
function we({ encoded: l, className: t }) {
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
const { useCallback: Dt } = e, It = ({ controlId: l, command: t, label: r, disabled: n }) => {
  const c = Z(), i = ae(), o = t ?? "click", u = r ?? c.label, s = n ?? c.disabled === !0, a = c.hidden === !0, m = c.image, p = c.iconOnly === !0, h = a ? { display: "none" } : void 0, S = Dt(() => {
    i(o);
  }, [i, o]);
  return m && p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: S,
      disabled: s,
      style: h,
      className: "tlReactButton tlReactButton--icon",
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(we, { encoded: m })
  ) : m ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: S,
      disabled: s,
      style: h,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(we, { encoded: m, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: S,
      disabled: s,
      style: h,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: Pt } = e, jt = ({ controlId: l, command: t, label: r, active: n, disabled: c }) => {
  const i = Z(), o = ae(), u = t ?? "click", s = r ?? i.label, a = n ?? i.active === !0, m = c ?? i.disabled === !0, p = Pt(() => {
    o(u);
  }, [o, u]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: p,
      disabled: m,
      className: "tlReactButton" + (a ? " tlReactButtonActive" : "")
    },
    s
  );
}, Mt = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => r("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: At } = e, Bt = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.tabs ?? [], c = t.activeTabId, i = At((o) => {
    o !== c && r("selectTab", { tabId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((o) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: o.id,
      role: "tab",
      "aria-selected": o.id === c,
      className: "tlReactTabBar__tab" + (o.id === c ? " tlReactTabBar__tab--active" : ""),
      onClick: () => i(o.id)
    },
    o.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, Ot = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, r && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, r), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(X, { control: c })))));
}, $t = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Ft = ({ controlId: l }) => {
  const t = Z(), r = Xe(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : n !== "idle" ? n : m, S = e.useCallback(async () => {
    if (n === "recording") {
      const g = u.current;
      g && g.state !== "inactive" && g.stop();
      return;
    }
    if (n !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const g = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = g, s.current = [];
        const B = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", P = new MediaRecorder(g, B ? { mimeType: B } : void 0);
        u.current = P, P.ondataavailable = (f) => {
          f.data.size > 0 && s.current.push(f.data);
        }, P.onstop = async () => {
          g.getTracks().forEach((C) => C.stop()), a.current = null;
          const f = new Blob(s.current, { type: P.mimeType || "audio/webm" });
          if (s.current = [], f.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const y = new FormData();
          y.append("audio", f, "recording.webm"), await r(y), c("idle");
        }, P.start(), c("recording");
      } catch (g) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", g), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [n, r]), b = se($t), R = h === "recording" ? b["js.audioRecorder.stop"] : h === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], E = h === "uploading", _ = ["tlAudioRecorder__button"];
  return h === "recording" && _.push("tlAudioRecorder__button--recording"), h === "uploading" && _.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: _.join(" "),
      onClick: S,
      disabled: E,
      title: R,
      "aria-label": R
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Ht = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Wt = ({ controlId: l }) => {
  const t = Z(), r = qe(), n = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(n ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), a = e.useRef(c);
  e.useEffect(() => {
    n ? i === "disabled" && o("idle") : (u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), o("disabled"));
  }, [n]), e.useEffect(() => {
    c !== a.current && (a.current = c, u.current && (u.current.pause(), u.current = null), s.current && (URL.revokeObjectURL(s.current), s.current = null), (i === "playing" || i === "paused" || i === "loading") && o("idle"));
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
        const E = await fetch(r);
        if (!E.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", E.status), o("idle");
          return;
        }
        const _ = await E.blob();
        s.current = URL.createObjectURL(_);
      } catch (E) {
        console.error("[TLAudioPlayer] Fetch error:", E), o("idle");
        return;
      }
    }
    const R = new Audio(s.current);
    u.current = R, R.onended = () => {
      o("idle");
    }, R.play(), o("playing");
  }, [i, r]), p = se(Ht), h = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], S = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: S,
      title: h,
      "aria-label": h
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, zt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Ut = ({ controlId: l }) => {
  const t = Z(), r = Xe(), [n, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : n !== "idle" ? n : s, h = e.useCallback(async (f) => {
    c("uploading");
    const y = new FormData();
    y.append("file", f, f.name), await r(y), c("idle");
  }, [r]), S = e.useCallback((f) => {
    var C;
    const y = (C = f.target.files) == null ? void 0 : C[0];
    y && h(y);
  }, [h]), b = e.useCallback(() => {
    var f;
    n !== "uploading" && ((f = u.current) == null || f.click());
  }, [n]), R = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!0);
  }, []), E = e.useCallback((f) => {
    f.preventDefault(), f.stopPropagation(), o(!1);
  }, []), _ = e.useCallback((f) => {
    var C;
    if (f.preventDefault(), f.stopPropagation(), o(!1), n === "uploading") return;
    const y = (C = f.dataTransfer.files) == null ? void 0 : C[0];
    y && h(y);
  }, [n, h]), g = p === "uploading", B = se(zt), P = p === "uploading" ? B["js.uploading"] : B["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: E,
      onDrop: _
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: S,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: g,
        title: P,
        "aria-label": P
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, Vt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Kt = ({ controlId: l }) => {
  const t = Z(), r = qe(), n = ae(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      a(!0);
      try {
        const b = r + (r.includes("?") ? "&" : "?") + "rev=" + i, R = await fetch(b);
        if (!R.ok) {
          console.error("[TLDownload] Failed to fetch data:", R.status);
          return;
        }
        const E = await R.blob(), _ = URL.createObjectURL(E), g = document.createElement("a");
        g.href = _, g.download = o, g.style.display = "none", document.body.appendChild(g), g.click(), document.body.removeChild(g), URL.revokeObjectURL(_);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        a(!1);
      }
    }
  }, [c, s, r, i, o]), p = e.useCallback(async () => {
    c && await n("clear");
  }, [c, n]), h = se(Vt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const S = s ? h["js.downloading"] : h["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), u && /* @__PURE__ */ e.createElement(
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
}, Yt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, Gt = ({ controlId: l }) => {
  const t = Z(), r = Xe(), [n, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), S = e.useRef(null), b = t.error, R = e.useMemo(
    () => {
      var T;
      return !!(window.isSecureContext && ((T = navigator.mediaDevices) != null && T.getUserMedia));
    },
    []
  ), E = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), _ = e.useCallback(() => {
    E(), c("idle");
  }, [E]), g = e.useCallback(async () => {
    var T;
    if (n !== "uploading") {
      if (o(null), !R) {
        (T = h.current) == null || T.click();
        return;
      }
      try {
        const F = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = F, c("overlayOpen");
      } catch (F) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", F), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [n, R]), B = e.useCallback(async () => {
    if (n !== "overlayOpen")
      return;
    const T = a.current, F = p.current;
    if (!T || !F)
      return;
    F.width = T.videoWidth, F.height = T.videoHeight;
    const x = F.getContext("2d");
    x && (x.drawImage(T, 0, 0), E(), c("uploading"), F.toBlob(async (O) => {
      if (!O) {
        c("idle");
        return;
      }
      const G = new FormData();
      G.append("photo", O, "capture.jpg"), await r(G), c("idle");
    }, "image/jpeg", 0.85));
  }, [n, r, E]), P = e.useCallback(async (T) => {
    var O;
    const F = (O = T.target.files) == null ? void 0 : O[0];
    if (!F) return;
    c("uploading");
    const x = new FormData();
    x.append("photo", F, F.name), await r(x), c("idle"), h.current && (h.current.value = "");
  }, [r]);
  e.useEffect(() => {
    n === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [n]), e.useEffect(() => {
    var F;
    if (n !== "overlayOpen") return;
    (F = S.current) == null || F.focus();
    const T = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = T;
    };
  }, [n]), e.useEffect(() => {
    if (n !== "overlayOpen") return;
    const T = (F) => {
      F.key === "Escape" && _();
    };
    return document.addEventListener("keydown", T), () => document.removeEventListener("keydown", T);
  }, [n, _]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((T) => T.stop()), m.current = null);
  }, []);
  const f = se(Yt), y = n === "uploading" ? f["js.uploading"] : f["js.photoCapture.open"], C = ["tlPhotoCapture__cameraBtn"];
  n === "uploading" && C.push("tlPhotoCapture__cameraBtn--uploading");
  const q = ["tlPhotoCapture__overlayVideo"];
  u && q.push("tlPhotoCapture__overlayVideo--mirrored");
  const N = ["tlPhotoCapture__mirrorBtn"];
  return u && N.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: g,
      disabled: n === "uploading",
      title: y,
      "aria-label": y
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !R && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: h,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: P
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), n === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: S,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: _ }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: a,
        className: q.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: N.join(" "),
        onClick: () => s((T) => !T),
        title: f["js.photoCapture.mirror"],
        "aria-label": f["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: B,
        title: f["js.photoCapture.capture"],
        "aria-label": f["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: _,
        title: f["js.photoCapture.close"],
        "aria-label": f["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, f[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Xt = {
  "js.photoViewer.alt": "Captured photo"
}, qt = ({ controlId: l }) => {
  const t = Z(), r = qe(), n = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
  e.useEffect(() => {
    if (!n) {
      i && (URL.revokeObjectURL(i), o(null));
      return;
    }
    if (c === u.current && i)
      return;
    u.current = c, i && (URL.revokeObjectURL(i), o(null));
    let a = !1;
    return (async () => {
      try {
        const m = await fetch(r);
        if (!m.ok) {
          console.error("[TLPhotoViewer] Failed to fetch image:", m.status);
          return;
        }
        const p = await m.blob();
        a || o(URL.createObjectURL(p));
      } catch (m) {
        console.error("[TLPhotoViewer] Fetch error:", m);
      }
    })(), () => {
      a = !0;
    };
  }, [n, c, r]), e.useEffect(() => () => {
    i && URL.revokeObjectURL(i);
  }, []);
  const s = se(Xt);
  return !n || !i ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: tt, useRef: Be } = e, Zt = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = n === "horizontal", u = i.length > 0 && i.every((E) => E.collapsed), s = !u && i.some((E) => E.collapsed), a = u ? !o : o, m = Be(null), p = Be(null), h = Be(null), S = tt((E, _) => {
    const g = {
      overflow: E.scrolling || "auto"
    };
    return E.collapsed ? u && !a ? g.flex = "1 0 0%" : g.flex = "0 0 auto" : _ !== void 0 ? g.flex = `0 0 ${_}px` : g.flex = `${E.size} 1 0%`, E.minSize > 0 && !E.collapsed && (g.minWidth = o ? E.minSize : void 0, g.minHeight = o ? void 0 : E.minSize), g;
  }, [o, u, s, a]), b = tt((E, _) => {
    E.preventDefault();
    const g = m.current;
    if (!g) return;
    const B = i[_], P = i[_ + 1], f = g.querySelectorAll(":scope > .tlSplitPanel__child"), y = [];
    f.forEach((N) => {
      y.push(o ? N.offsetWidth : N.offsetHeight);
    }), h.current = y, p.current = {
      splitterIndex: _,
      startPos: o ? E.clientX : E.clientY,
      startSizeBefore: y[_],
      startSizeAfter: y[_ + 1],
      childBefore: B,
      childAfter: P
    };
    const C = (N) => {
      const T = p.current;
      if (!T || !h.current) return;
      const x = (o ? N.clientX : N.clientY) - T.startPos, O = T.childBefore.minSize || 0, G = T.childAfter.minSize || 0;
      let re = T.startSizeBefore + x, H = T.startSizeAfter - x;
      re < O && (H += re - O, re = O), H < G && (re += H - G, H = G), h.current[T.splitterIndex] = re, h.current[T.splitterIndex + 1] = H;
      const J = g.querySelectorAll(":scope > .tlSplitPanel__child"), A = J[T.splitterIndex], I = J[T.splitterIndex + 1];
      A && (A.style.flex = `0 0 ${re}px`), I && (I.style.flex = `0 0 ${H}px`);
    }, q = () => {
      if (document.removeEventListener("mousemove", C), document.removeEventListener("mouseup", q), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const N = {};
        i.forEach((T, F) => {
          const x = T.control;
          x != null && x.controlId && h.current && (N[x.controlId] = h.current[F]);
        }), r("updateSizes", { sizes: N });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", C), document.addEventListener("mouseup", q), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, r]), R = [];
  return i.forEach((E, _) => {
    if (R.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${_}`,
          className: `tlSplitPanel__child${E.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: S(E)
        },
        /* @__PURE__ */ e.createElement(X, { control: E.control })
      )
    ), c && _ < i.length - 1) {
      const g = i[_ + 1];
      !E.collapsed && !g.collapsed && R.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${_}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${n}`,
            onMouseDown: (P) => b(P, _)
          }
        )
      );
    }
  }), /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: m,
      id: l,
      className: `tlSplitPanel tlSplitPanel--${n}${u ? " tlSplitPanel--allCollapsed" : ""}`,
      style: {
        display: "flex",
        flexDirection: a ? "row" : "column",
        width: "100%",
        height: "100%"
      }
    },
    R
  );
}, { useCallback: Oe } = e, Qt = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, Jt = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), en = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), rn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = se(Qt), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, a = t.fullLine === !0, m = t.toolbarButtons ?? [], p = i === "MINIMIZED", h = i === "MAXIMIZED", S = i === "HIDDEN", b = Oe(() => {
    r("toggleMinimize");
  }, [r]), R = Oe(() => {
    r("toggleMaximize");
  }, [r]), E = Oe(() => {
    r("popOut");
  }, [r]);
  if (S)
    return null;
  const _ = h ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${i.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: _
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((g, B) => /* @__PURE__ */ e.createElement("span", { key: B, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: g }))), o && !h && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: p ? n["js.panel.restore"] : n["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(en, null) : /* @__PURE__ */ e.createElement(Jt, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: R,
        title: h ? n["js.panel.restore"] : n["js.panel.maximize"]
      },
      h ? /* @__PURE__ */ e.createElement(nn, null) : /* @__PURE__ */ e.createElement(tn, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: E,
        title: n["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(ln, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(X, { control: t.child }))
  );
}, an = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(X, { control: t.child })
  );
}, on = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(X, { control: t.activeChild }));
}, { useCallback: de, useState: je, useEffect: Me, useRef: Ae } = e, sn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ke(l, t, r, n) {
  const c = [];
  for (const i of l)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: n }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: n }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (r.get(i.id) ?? i.expanded) && !t && c.push(...Ke(i.children, t, r, i.id)));
  return c;
}
const ke = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + l, "aria-hidden": "true" }) : null, cn = ({ item: l, active: t, collapsed: r, onSelect: n, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => n(l.id),
    title: r ? l.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(l.id)
  },
  r && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(ke, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(ke, { icon: l.icon }),
  !r && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !r && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), un = ({ item: l, collapsed: t, onExecute: r, tabIndex: n, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => r(l.id),
    title: t ? l.label : void 0,
    tabIndex: n,
    ref: c,
    onFocus: () => i(l.id)
  },
  /* @__PURE__ */ e.createElement(ke, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), dn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(ke, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), mn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), pn = ({ item: l, activeItemId: t, anchorRect: r, onSelect: n, onExecute: c, onClose: i }) => {
  const o = Ae(null);
  Me(() => {
    const a = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", a), () => document.removeEventListener("mousedown", a);
  }, [i]), Me(() => {
    const a = (m) => {
      m.key === "Escape" && i();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [i]);
  const u = de((a) => {
    a.type === "nav" ? (n(a.id), i()) : a.type === "command" && (c(a.id), i());
  }, [n, c, i]), s = {};
  return r && (s.left = r.right, s.top = r.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: o, role: "menu", style: s }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
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
        /* @__PURE__ */ e.createElement(ke, { icon: a.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, a.label),
        a.type === "nav" && a.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, a.badge)
      );
    }
    return a.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: a.id, className: "tlSidebar__flyoutSectionHeader" }, a.label) : a.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: a.id, className: "tlSidebar__separator" }) : null;
  }));
}, fn = ({
  item: l,
  expanded: t,
  activeItemId: r,
  collapsed: n,
  onSelect: c,
  onExecute: i,
  onToggleGroup: o,
  tabIndex: u,
  itemRef: s,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: S,
  onOpenFlyout: b,
  onCloseFlyout: R
}) => {
  const E = Ae(null), [_, g] = je(null), B = de(() => {
    n ? S === l.id ? R() : (E.current && g(E.current.getBoundingClientRect()), b(l.id)) : o(l.id);
  }, [n, S, l.id, o, b, R]), P = de((y) => {
    E.current = y, s(y);
  }, [s]), f = n && S === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (f ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: B,
      title: n ? l.label : void 0,
      "aria-expanded": n ? f : t,
      tabIndex: u,
      ref: P,
      onFocus: () => a(l.id)
    },
    /* @__PURE__ */ e.createElement(ke, { icon: l.icon }),
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
  ), f && /* @__PURE__ */ e.createElement(
    pn,
    {
      item: l,
      activeItemId: r,
      anchorRect: _,
      onSelect: c,
      onExecute: i,
      onClose: R
    }
  ), t && !n && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((y) => /* @__PURE__ */ e.createElement(
    mt,
    {
      key: y.id,
      item: y,
      activeItemId: r,
      collapsed: n,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: h,
      groupStates: null,
      flyoutGroupId: S,
      onOpenFlyout: b,
      onCloseFlyout: R
    }
  ))));
}, mt = ({
  item: l,
  activeItemId: t,
  collapsed: r,
  onSelect: n,
  onExecute: c,
  onToggleGroup: i,
  focusedId: o,
  setItemRef: u,
  onItemFocus: s,
  groupStates: a,
  flyoutGroupId: m,
  onOpenFlyout: p,
  onCloseFlyout: h
}) => {
  switch (l.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        cn,
        {
          item: l,
          active: l.id === t,
          collapsed: r,
          onSelect: n,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "command":
      return /* @__PURE__ */ e.createElement(
        un,
        {
          item: l,
          collapsed: r,
          onExecute: c,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s
        }
      );
    case "header":
      return /* @__PURE__ */ e.createElement(dn, { item: l, collapsed: r });
    case "separator":
      return /* @__PURE__ */ e.createElement(mn, null);
    case "group": {
      const S = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        fn,
        {
          item: l,
          expanded: S,
          activeItemId: t,
          collapsed: r,
          onSelect: n,
          onExecute: c,
          onToggleGroup: i,
          tabIndex: o === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: s,
          focusedId: o,
          setItemRef: u,
          onItemFocus: s,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, hn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = se(sn), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = je(() => {
    const N = /* @__PURE__ */ new Map(), T = (F) => {
      for (const x of F)
        x.type === "group" && (N.set(x.id, x.expanded), T(x.children));
    };
    return T(c), N;
  }), a = de((N) => {
    s((T) => {
      const F = new Map(T), x = F.get(N) ?? !1;
      return F.set(N, !x), r("toggleGroup", { itemId: N, expanded: !x }), F;
    });
  }, [r]), m = de((N) => {
    N !== i && r("selectItem", { itemId: N });
  }, [r, i]), p = de((N) => {
    r("executeCommand", { itemId: N });
  }, [r]), h = de(() => {
    r("toggleCollapse", {});
  }, [r]), [S, b] = je(null), R = de((N) => {
    b(N);
  }, []), E = de(() => {
    b(null);
  }, []);
  Me(() => {
    o || b(null);
  }, [o]);
  const [_, g] = je(() => {
    const N = Ke(c, o, u);
    return N.length > 0 ? N[0].id : "";
  }), B = Ae(/* @__PURE__ */ new Map()), P = de((N) => (T) => {
    T ? B.current.set(N, T) : B.current.delete(N);
  }, []), f = de((N) => {
    g(N);
  }, []), y = Ae(0), C = de((N) => {
    g(N), y.current++;
  }, []);
  Me(() => {
    const N = B.current.get(_);
    N && document.activeElement !== N && N.focus();
  }, [_, y.current]);
  const q = de((N) => {
    if (N.key === "Escape" && S !== null) {
      N.preventDefault(), E();
      return;
    }
    const T = Ke(c, o, u);
    if (T.length === 0) return;
    const F = T.findIndex((O) => O.id === _);
    if (F < 0) return;
    const x = T[F];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const O = (F + 1) % T.length;
        C(T[O].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const O = (F - 1 + T.length) % T.length;
        C(T[O].id);
        break;
      }
      case "Home": {
        N.preventDefault(), C(T[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), C(T[T.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), x.type === "nav" ? m(x.id) : x.type === "command" ? p(x.id) : x.type === "group" && (o ? S === x.id ? E() : R(x.id) : a(x.id));
        break;
      }
      case "ArrowRight": {
        x.type === "group" && !o && ((u.get(x.id) ?? !1) || (N.preventDefault(), a(x.id)));
        break;
      }
      case "ArrowLeft": {
        x.type === "group" && !o && (u.get(x.id) ?? !1) && (N.preventDefault(), a(x.id));
        break;
      }
    }
  }, [
    c,
    o,
    u,
    _,
    S,
    C,
    m,
    p,
    a,
    R,
    E
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": n["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: q }, c.map((N) => /* @__PURE__ */ e.createElement(
    mt,
    {
      key: N.id,
      item: N,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: a,
      focusedId: _,
      setItemRef: P,
      onItemFocus: f,
      groupStates: u,
      flyoutGroupId: S,
      onOpenFlyout: R,
      onCloseFlyout: E
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(X, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(X, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: h,
      title: o ? n["js.sidebar.expand"] : n["js.sidebar.collapse"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(X, { control: t.activeContent })));
}, _n = ({ controlId: l }) => {
  const t = Z(), r = t.direction ?? "column", n = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${r}`,
    `tlStack--gap-${n}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: u }, o.map((s, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: s })));
}, bn = ({ controlId: l }) => {
  const t = Z(), r = t.columns, n = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return n ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${n}, 1fr))` : r && (o.gridTemplateColumns = `repeat(${r}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(X, { key: s, control: u })));
}, vn = ({ controlId: l }) => {
  const t = Z(), r = t.title, n = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = r != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${n}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, r && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, r), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(X, { control: o })));
}, En = ({ controlId: l }) => {
  const t = Z(), r = t.title ?? "", n = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: u }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(X, { control: n })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, r), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, a) => /* @__PURE__ */ e.createElement(X, { key: a, control: s }))));
}, { useCallback: gn } = e, Cn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.items ?? [], c = gn((i) => {
    r("navigate", { itemId: i });
  }, [r]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, n.map((i, o) => {
    const u = o === n.length - 1;
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
}, { useCallback: yn } = e, wn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.items ?? [], c = t.activeItemId, i = yn((o) => {
    o !== c && r("selectItem", { itemId: o });
  }, [r, c]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, n.map((o) => {
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
}, { useCallback: nt, useEffect: lt, useRef: kn } = e, Sn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = kn(null), u = nt(() => {
    r("close");
  }, [r]), s = nt((a) => {
    c && a.target === a.currentTarget && u();
  }, [c, u]);
  return lt(() => {
    if (!n) return;
    const a = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [n, u]), lt(() => {
    n && o.current && o.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: s,
      ref: o,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(X, { control: i })
  ) : null;
}, { useEffect: Nn, useRef: Rn } = e, Tn = ({ controlId: l }) => {
  const r = Z().dialogs ?? [], n = Rn(r.length);
  return Nn(() => {
    r.length < n.current && r.length > 0, n.current = r.length;
  }, [r.length]), r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, r.map((c) => /* @__PURE__ */ e.createElement(X, { key: c.controlId, control: c })));
}, { useCallback: Te, useRef: ge, useState: xe } = e, xn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Ln = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Dn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = se(xn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.minHeight ?? null, s = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbarButtons ?? [], [h, S] = xe(null), [b, R] = xe(null), [E, _] = xe(null), g = ge(null), [B, P] = xe(!1), f = ge(null), y = ge(null), C = ge(null), q = ge(null), N = ge(null), T = Te(() => {
    r("close");
  }, [r]), F = Te((H, J) => {
    J.preventDefault();
    const A = q.current;
    if (!A) return;
    const I = A.getBoundingClientRect(), Q = !g.current, d = g.current ?? { x: I.left, y: I.top };
    Q && (g.current = d, _(d)), N.current = {
      dir: H,
      startX: J.clientX,
      startY: J.clientY,
      startW: I.width,
      startH: I.height,
      startPos: { ...d },
      symmetric: Q
    };
    const v = (j) => {
      const L = N.current;
      if (!L) return;
      const V = j.clientX - L.startX, K = j.clientY - L.startY;
      let le = L.startW, ee = L.startH, ue = 0, me = 0;
      L.symmetric ? (L.dir.includes("e") && (le = L.startW + 2 * V), L.dir.includes("w") && (le = L.startW - 2 * V), L.dir.includes("s") && (ee = L.startH + 2 * K), L.dir.includes("n") && (ee = L.startH - 2 * K)) : (L.dir.includes("e") && (le = L.startW + V), L.dir.includes("w") && (le = L.startW - V, ue = V), L.dir.includes("s") && (ee = L.startH + K), L.dir.includes("n") && (ee = L.startH - K, me = K));
      const he = Math.max(200, le), w = Math.max(100, ee);
      L.symmetric ? (ue = (L.startW - he) / 2, me = (L.startH - w) / 2) : (L.dir.includes("w") && he === 200 && (ue = L.startW - 200), L.dir.includes("n") && w === 100 && (me = L.startH - 100)), y.current = he, C.current = w, S(he), R(w);
      const D = {
        x: L.startPos.x + ue,
        y: L.startPos.y + me
      };
      g.current = D, _(D);
    }, $ = () => {
      document.removeEventListener("mousemove", v), document.removeEventListener("mouseup", $);
      const j = y.current, L = C.current;
      (j != null || L != null) && r("resize", {
        ...j != null ? { width: Math.round(j) + "px" } : {},
        ...L != null ? { height: Math.round(L) + "px" } : {}
      }), N.current = null;
    };
    document.addEventListener("mousemove", v), document.addEventListener("mouseup", $);
  }, [r]), x = Te((H) => {
    if (H.button !== 0 || H.target.closest("button")) return;
    H.preventDefault();
    const J = q.current;
    if (!J) return;
    const A = J.getBoundingClientRect(), I = g.current ?? { x: A.left, y: A.top }, Q = H.clientX - I.x, d = H.clientY - I.y, v = (j) => {
      const L = window.innerWidth, V = window.innerHeight;
      let K = j.clientX - Q, le = j.clientY - d;
      const ee = J.offsetWidth, ue = J.offsetHeight;
      K + ee > L && (K = L - ee), le + ue > V && (le = V - ue), K < 0 && (K = 0), le < 0 && (le = 0);
      const me = { x: K, y: le };
      g.current = me, _(me);
    }, $ = () => {
      document.removeEventListener("mousemove", v), document.removeEventListener("mouseup", $);
    };
    document.addEventListener("mousemove", v), document.addEventListener("mouseup", $);
  }, []), O = Te(() => {
    var H, J;
    if (B) {
      const A = f.current;
      A && (_(A.x !== -1 ? { x: A.x, y: A.y } : null), S(A.w), R(A.h)), P(!1);
    } else {
      const A = q.current, I = A == null ? void 0 : A.getBoundingClientRect();
      f.current = {
        x: ((H = g.current) == null ? void 0 : H.x) ?? (I == null ? void 0 : I.left) ?? -1,
        y: ((J = g.current) == null ? void 0 : J.y) ?? (I == null ? void 0 : I.top) ?? -1,
        w: h ?? (I == null ? void 0 : I.width) ?? null,
        h: b ?? null
      }, P(!0), _({ x: 0, y: 0 }), S(null), R(null);
    }
  }, [B, h, b]), G = B ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: h != null ? h + "px" : i,
    ...b != null ? { height: b + "px" } : o != null ? { height: o } : {},
    ...u != null && b == null ? { minHeight: u } : {},
    maxHeight: E ? "100vh" : "80vh",
    ...E ? { position: "absolute", left: E.x + "px", top: E.y + "px" } : {}
  }, re = l + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlWindow",
      style: G,
      ref: q,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": re
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${B ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: B ? void 0 : x,
        onDoubleClick: O
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: re }, c),
      p.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, p.map((H, J) => /* @__PURE__ */ e.createElement("span", { key: J, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(X, { control: H })))),
      /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: O,
          title: B ? n["js.window.restore"] : n["js.window.maximize"]
        },
        B ? (
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
          onClick: T,
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(X, { control: a })),
    m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, m.map((H, J) => /* @__PURE__ */ e.createElement(X, { key: J, control: H }))),
    s && !B && Ln.map((H) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: H,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${H}`,
        onMouseDown: (J) => F(H, J)
      }
    ))
  );
}, { useCallback: In, useEffect: Pn } = e, jn = {
  "js.drawer.close": "Close"
}, Mn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = se(jn), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, a = In(() => {
    r("close");
  }, [r]);
  Pn(() => {
    if (!c) return;
    const p = (h) => {
      h.key === "Escape" && a();
    };
    return document.addEventListener("keydown", p), () => document.removeEventListener("keydown", p);
  }, [c, a]);
  const m = [
    "tlDrawer",
    `tlDrawer--${i}`,
    `tlDrawer--${o}`,
    c ? "tlDrawer--open" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("aside", { id: l, className: m, "aria-hidden": !c }, u !== null && /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlDrawer__title" }, u), /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDrawer__closeBtn",
      onClick: a,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(X, { control: s })));
}, { useCallback: An } = e, Bn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.child, c = An((i) => {
    i.preventDefault(), i.stopPropagation(), r("openContextMenu", { x: i.clientX, y: i.clientY });
  }, [r]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: c }, n && /* @__PURE__ */ e.createElement(X, { control: n }));
}, { useCallback: On, useEffect: $n, useState: Fn } = e, Hn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.duration ?? 5e3, u = t.visible === !0, s = t.generation ?? 0, [a, m] = Fn(!1), p = On(() => {
    m(!0), setTimeout(() => {
      r("dismiss", { generation: s }), m(!1);
    }, 200);
  }, [r, s]);
  return $n(() => {
    if (!u || o === 0) return;
    const h = setTimeout(p, o);
    return () => clearTimeout(h);
  }, [u, o, p]), !u && !a ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlSnackbar tlSnackbar--${i}${a ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, n)
  );
}, { useCallback: $e, useEffect: Fe, useRef: Wn, useState: rt } = e, zn = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = t.open === !0, c = t.anchorId, i = t.anchorX, o = t.anchorY, u = t.items ?? [], s = Wn(null), [a, m] = rt({ top: 0, left: 0 }), [p, h] = rt(0), S = u.filter((_) => _.type === "item" && !_.disabled);
  Fe(() => {
    var C, q;
    if (!n) return;
    const _ = ((C = s.current) == null ? void 0 : C.offsetHeight) ?? 200, g = ((q = s.current) == null ? void 0 : q.offsetWidth) ?? 200;
    if (i != null && o != null) {
      let N = o, T = i;
      N + _ > window.innerHeight && (N = Math.max(0, window.innerHeight - _)), T + g > window.innerWidth && (T = Math.max(0, window.innerWidth - g)), m({ top: N, left: T }), h(0);
      return;
    }
    if (!c) return;
    const B = document.getElementById(c);
    if (!B) return;
    const P = B.getBoundingClientRect();
    let f = P.bottom + 4, y = P.left;
    f + _ > window.innerHeight && (f = P.top - _ - 4), y + g > window.innerWidth && (y = P.right - g), m({ top: f, left: y }), h(0);
  }, [n, c, i, o]);
  const b = $e(() => {
    r("close");
  }, [r]), R = $e((_) => {
    r("selectItem", { itemId: _ });
  }, [r]);
  Fe(() => {
    if (!n) return;
    const _ = (g) => {
      s.current && !s.current.contains(g.target) && b();
    };
    return document.addEventListener("mousedown", _), () => document.removeEventListener("mousedown", _);
  }, [n, b]);
  const E = $e((_) => {
    if (_.key === "Escape") {
      b();
      return;
    }
    if (_.key === "ArrowDown")
      _.preventDefault(), h((g) => (g + 1) % S.length);
    else if (_.key === "ArrowUp")
      _.preventDefault(), h((g) => (g - 1 + S.length) % S.length);
    else if (_.key === "Enter" || _.key === " ") {
      _.preventDefault();
      const g = S[p];
      g && R(g.id);
    }
  }, [b, R, S, p]);
  return Fe(() => {
    n && s.current && s.current.focus();
  }, [n]), n ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: s,
      tabIndex: -1,
      style: { position: "fixed", top: a.top, left: a.left },
      onKeyDown: E
    },
    u.map((_, g) => {
      if (_.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: g, className: "tlMenu__separator" });
      const P = S.indexOf(_) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: _.id,
          type: "button",
          className: "tlMenu__item" + (P ? " tlMenu__item--focused" : "") + (_.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: _.disabled,
          tabIndex: P ? 0 : -1,
          onClick: () => R(_.id)
        },
        _.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + _.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, _.label)
      );
    })
  ) : null;
}, Un = ({ controlId: l }) => {
  const t = Z(), r = t.header, n = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(X, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(X, { control: n })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(X, { control: c })), /* @__PURE__ */ e.createElement(X, { control: i }), o && /* @__PURE__ */ e.createElement(X, { control: o }), u && /* @__PURE__ */ e.createElement(X, { control: u }));
}, Vn = () => {
  const l = Z(), t = l.text ?? "", r = l.cssClass ?? "", n = r ? `tlText ${r}` : "tlText";
  return /* @__PURE__ */ e.createElement("span", { className: n }, t);
}, Kn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, at = 50, Yn = () => {
  const l = Z(), t = ae(), r = se(Kn), n = l.columns ?? [], c = l.totalRowCount ?? 0, i = l.rows ?? [], o = l.rowHeight ?? 36, u = l.selectionMode ?? "single", s = l.selectedCount ?? 0, a = l.frozenColumnCount ?? 0, m = l.treeMode ?? !1, p = e.useMemo(
    () => n.filter((w) => w.sortPriority && w.sortPriority > 0).length,
    [n]
  ), h = u === "multi", S = 40, b = 20, R = e.useRef(null), E = e.useRef(null), _ = e.useRef(null), [g, B] = e.useState({}), P = e.useRef(null), f = e.useRef(!1), y = e.useRef(null), [C, q] = e.useState(null), [N, T] = e.useState(null);
  e.useEffect(() => {
    P.current || B({});
  }, [n]);
  const F = e.useCallback((w) => g[w.name] ?? w.width, [g]), x = e.useMemo(() => {
    const w = [];
    let D = h && a > 0 ? S : 0;
    for (let Y = 0; Y < a && Y < n.length; Y++)
      w.push(D), D += F(n[Y]);
    return w;
  }, [n, a, h, S, F]), O = c * o, G = e.useRef(null), re = e.useCallback((w, D, Y) => {
    Y.preventDefault(), Y.stopPropagation(), P.current = { column: w, startX: Y.clientX, startWidth: D };
    let te = Y.clientX, k = 0;
    const M = () => {
      const oe = P.current;
      if (!oe) return;
      const pe = Math.max(at, oe.startWidth + (te - oe.startX) + k);
      B((Ee) => ({ ...Ee, [oe.column]: pe }));
    }, z = () => {
      const oe = E.current, pe = R.current;
      if (!oe || !P.current) return;
      const Ee = oe.getBoundingClientRect(), Qe = 40, Je = 8, vt = oe.scrollLeft;
      te > Ee.right - Qe ? oe.scrollLeft += Je : te < Ee.left + Qe && (oe.scrollLeft = Math.max(0, oe.scrollLeft - Je));
      const et = oe.scrollLeft - vt;
      et !== 0 && (pe && (pe.scrollLeft = oe.scrollLeft), k += et, M()), G.current = requestAnimationFrame(z);
    };
    G.current = requestAnimationFrame(z);
    const ne = (oe) => {
      te = oe.clientX, M();
    }, _e = (oe) => {
      document.removeEventListener("mousemove", ne), document.removeEventListener("mouseup", _e), G.current !== null && (cancelAnimationFrame(G.current), G.current = null);
      const pe = P.current;
      if (pe) {
        const Ee = Math.max(at, pe.startWidth + (oe.clientX - pe.startX) + k);
        t("columnResize", { column: pe.column, width: Ee }), P.current = null, f.current = !0, requestAnimationFrame(() => {
          f.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ne), document.addEventListener("mouseup", _e);
  }, [t]), H = e.useCallback(() => {
    R.current && E.current && (R.current.scrollLeft = E.current.scrollLeft), _.current !== null && clearTimeout(_.current), _.current = window.setTimeout(() => {
      const w = E.current;
      if (!w) return;
      const D = w.scrollTop, Y = Math.ceil(w.clientHeight / o), te = Math.floor(D / o);
      t("scroll", { start: te, count: Y });
    }, 80);
  }, [t, o]), J = e.useCallback((w, D, Y) => {
    if (f.current) return;
    let te;
    !D || D === "desc" ? te = "asc" : te = "desc";
    const k = Y.shiftKey ? "add" : "replace";
    t("sort", { column: w, direction: te, mode: k });
  }, [t]), A = e.useCallback((w, D) => {
    y.current = w, D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", w);
  }, []), I = e.useCallback((w, D) => {
    if (!y.current || y.current === w) {
      q(null);
      return;
    }
    D.preventDefault(), D.dataTransfer.dropEffect = "move";
    const Y = D.currentTarget.getBoundingClientRect(), te = D.clientX < Y.left + Y.width / 2 ? "left" : "right";
    q({ column: w, side: te });
  }, []), Q = e.useCallback((w) => {
    w.preventDefault(), w.stopPropagation();
    const D = y.current;
    if (!D || !C) {
      y.current = null, q(null);
      return;
    }
    let Y = n.findIndex((k) => k.name === C.column);
    if (Y < 0) {
      y.current = null, q(null);
      return;
    }
    const te = n.findIndex((k) => k.name === D);
    C.side === "right" && Y++, te < Y && Y--, t("columnReorder", { column: D, targetIndex: Y }), y.current = null, q(null);
  }, [n, C, t]), d = e.useCallback(() => {
    y.current = null, q(null);
  }, []), v = e.useCallback((w, D) => {
    D.shiftKey && D.preventDefault(), t("select", {
      rowIndex: w,
      ctrlKey: D.ctrlKey || D.metaKey,
      shiftKey: D.shiftKey
    });
  }, [t]), $ = e.useCallback((w, D) => {
    D.stopPropagation(), t("select", { rowIndex: w, ctrlKey: !0, shiftKey: !1 });
  }, [t]), j = e.useCallback(() => {
    const w = s === c && c > 0;
    t("selectAll", { selected: !w });
  }, [t, s, c]), L = e.useCallback((w, D, Y) => {
    Y.stopPropagation(), t("expand", { rowIndex: w, expanded: D });
  }, [t]), V = e.useCallback((w, D) => {
    D.preventDefault(), T({ x: D.clientX, y: D.clientY, colIdx: w });
  }, []), K = e.useCallback(() => {
    N && (t("setFrozenColumnCount", { count: N.colIdx + 1 }), T(null));
  }, [N, t]), le = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), T(null);
  }, [t]);
  e.useEffect(() => {
    if (!N) return;
    const w = () => T(null), D = (Y) => {
      Y.key === "Escape" && T(null);
    };
    return document.addEventListener("mousedown", w), document.addEventListener("keydown", D), () => {
      document.removeEventListener("mousedown", w), document.removeEventListener("keydown", D);
    };
  }, [N]);
  const ee = n.reduce((w, D) => w + F(D), 0) + (h ? S : 0), ue = s === c && c > 0, me = s > 0 && s < c, he = e.useCallback((w) => {
    w && (w.indeterminate = me);
  }, [me]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (w) => {
        if (!y.current) return;
        w.preventDefault();
        const D = E.current, Y = R.current;
        if (!D) return;
        const te = D.getBoundingClientRect(), k = 40, M = 8;
        w.clientX < te.left + k ? D.scrollLeft = Math.max(0, D.scrollLeft - M) : w.clientX > te.right - k && (D.scrollLeft += M), Y && (Y.scrollLeft = D.scrollLeft);
      },
      onDrop: Q
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: R }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: ee } }, h && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: S,
          minWidth: S,
          ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (w) => {
          y.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", n.length > 0 && n[0].name !== y.current && q({ column: n[0].name, side: "left" }));
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
    ), n.map((w, D) => {
      const Y = F(w);
      n.length - 1;
      let te = "tlTableView__headerCell";
      w.sortable && (te += " tlTableView__headerCell--sortable"), C && C.column === w.name && (te += " tlTableView__headerCell--dragOver-" + C.side);
      const k = D < a, M = D === a - 1;
      return k && (te += " tlTableView__headerCell--frozen"), M && (te += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.name,
          className: te,
          style: {
            width: Y,
            minWidth: Y,
            position: k ? "sticky" : "relative",
            ...k ? { left: x[D], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: w.sortable ? (z) => J(w.name, w.sortDirection, z) : void 0,
          onContextMenu: (z) => V(D, z),
          onDragStart: (z) => A(w.name, z),
          onDragOver: (z) => I(w.name, z),
          onDrop: Q,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, w.label),
        w.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, w.sortDirection === "asc" ? "▲" : "▼", p > 1 && w.sortPriority != null && w.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, w.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (z) => re(w.name, Y, z)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (w) => {
          if (y.current && n.length > 0) {
            const D = n[n.length - 1];
            D.name !== y.current && (w.preventDefault(), w.dataTransfer.dropEffect = "move", q({ column: D.name, side: "right" }));
          }
        },
        onDrop: Q
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: E,
        className: "tlTableView__body",
        onScroll: H
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: O, position: "relative", width: ee } }, i.map((w) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: w.id,
          className: "tlTableView__row" + (w.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: w.index * o,
            height: o,
            width: ee
          },
          onClick: (D) => v(w.index, D)
        },
        h && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (a > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: S,
              minWidth: S,
              ...a > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (D) => D.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: w.selected,
              onChange: () => {
              },
              onClick: (D) => $(w.index, D),
              tabIndex: -1
            }
          )
        ),
        n.map((D, Y) => {
          const te = F(D), k = Y === n.length - 1, M = Y < a, z = Y === a - 1;
          let ne = "tlTableView__cell";
          M && (ne += " tlTableView__cell--frozen"), z && (ne += " tlTableView__cell--frozenLast");
          const _e = m && Y === 0, oe = w.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: D.name,
              className: ne,
              style: {
                ...k && !M ? { flex: "1 0 auto", minWidth: te } : { width: te, minWidth: te },
                ...M ? { position: "sticky", left: x[Y], zIndex: 2 } : {}
              }
            },
            _e ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: oe * b } }, w.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => L(w.index, !w.expanded, pe)
              },
              w.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(X, { control: w.cells[D.name] })) : /* @__PURE__ */ e.createElement(X, { control: w.cells[D.name] })
          );
        })
      )))
    ),
    N && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: N.y, left: N.x, zIndex: 1e4 },
        onMouseDown: (w) => w.stopPropagation()
      },
      N.colIdx + 1 !== a && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: K }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      a > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: le }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, Gn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, pt = e.createContext(Gn), { useMemo: Xn, useRef: qn, useState: Zn, useEffect: Qn } = e, Jn = 320, el = ({ controlId: l }) => {
  const t = Z(), r = t.maxColumns ?? 3, n = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = qn(null), [s, a] = Zn(
    n === "top" ? "top" : "side"
  );
  Qn(() => {
    if (n !== "auto") {
      a(n);
      return;
    }
    const b = u.current;
    if (!b) return;
    const R = new ResizeObserver((E) => {
      for (const _ of E) {
        const B = _.contentRect.width / r;
        a(B < Jn ? "top" : "side");
      }
    });
    return R.observe(b), () => R.disconnect();
  }, [n, r]);
  const m = Xn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / r))}rem`}, 1fr))`
  }, S = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(pt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: S, style: h, ref: u }, i.map((b, R) => /* @__PURE__ */ e.createElement(X, { key: R, control: b }))));
}, { useCallback: tl } = e, nl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, ll = ({ controlId: l }) => {
  const t = Z(), r = ae(), n = se(nl), c = t.headerControl ?? null, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, h = tl(() => {
    r("toggleCollapse");
  }, [r]), S = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    a ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: S }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: h,
      "aria-expanded": !u,
      title: u ? n["js.formGroup.expand"] : n["js.formGroup.collapse"]
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(X, { control: c })), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, R) => /* @__PURE__ */ e.createElement(X, { key: R, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, R) => /* @__PURE__ */ e.createElement(X, { key: R, control: b }))));
}, { useContext: rl, useState: al, useCallback: ol } = e, sl = ({ controlId: l }) => {
  const t = Z(), r = rl(pt), n = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, a = t.labelPosition ?? r.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.field, S = r.readOnly, [b, R] = al(!1), E = ol(() => R((P) => !P), []);
  if (!p) return null;
  const _ = i != null, g = o != null && o.length > 0, B = [
    "tlFormField",
    `tlFormField--${a}`,
    S ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    _ ? "tlFormField--error" : "",
    !_ && g ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: B }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, n), c && !S && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !S && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(X, { control: h })), !S && _ && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !S && !_ && g && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((P, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, P)))), !S && u && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, cl = () => {
  const l = Z(), t = ae(), r = l.iconCss, n = l.iconSrc, c = l.label, i = l.cssClass, o = l.tooltip, u = l.hasLink, s = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : n ? /* @__PURE__ */ e.createElement("img", { src: n, className: "tlTypeIcon", alt: "" }) : null, a = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((h) => {
    h.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, a) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, a);
}, il = 20, ul = () => {
  const l = Z(), t = ae(), r = l.nodes ?? [], n = l.selectionMode ?? "single", c = l.dragEnabled ?? !1, i = l.dropEnabled ?? !1, o = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [s, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((f, y) => {
    t(y ? "collapse" : "expand", { nodeId: f });
  }, [t]), h = e.useCallback((f, y) => {
    t("select", {
      nodeId: f,
      ctrlKey: y.ctrlKey || y.metaKey,
      shiftKey: y.shiftKey
    });
  }, [t]), S = e.useCallback((f, y) => {
    y.preventDefault(), t("contextMenu", { nodeId: f, x: y.clientX, y: y.clientY });
  }, [t]), b = e.useRef(null), R = e.useCallback((f, y) => {
    const C = y.getBoundingClientRect(), q = f.clientY - C.top, N = C.height / 3;
    return q < N ? "above" : q > N * 2 ? "below" : "within";
  }, []), E = e.useCallback((f, y) => {
    y.dataTransfer.effectAllowed = "move", y.dataTransfer.setData("text/plain", f);
  }, []), _ = e.useCallback((f, y) => {
    y.preventDefault(), y.dataTransfer.dropEffect = "move";
    const C = R(y, y.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: f, position: C }), b.current = null;
    }, 50);
  }, [t, R]), g = e.useCallback((f, y) => {
    y.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const C = R(y, y.currentTarget);
    t("drop", { nodeId: f, position: C });
  }, [t, R]), B = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), P = e.useCallback((f) => {
    if (r.length === 0) return;
    let y = s;
    switch (f.key) {
      case "ArrowDown":
        f.preventDefault(), y = Math.min(s + 1, r.length - 1);
        break;
      case "ArrowUp":
        f.preventDefault(), y = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (f.preventDefault(), s >= 0 && s < r.length) {
          const C = r[s];
          if (C.expandable && !C.expanded) {
            t("expand", { nodeId: C.id });
            return;
          } else C.expanded && (y = s + 1);
        }
        break;
      case "ArrowLeft":
        if (f.preventDefault(), s >= 0 && s < r.length) {
          const C = r[s];
          if (C.expanded) {
            t("collapse", { nodeId: C.id });
            return;
          } else {
            const q = C.depth;
            for (let N = s - 1; N >= 0; N--)
              if (r[N].depth < q) {
                y = N;
                break;
              }
          }
        }
        break;
      case "Enter":
        f.preventDefault(), s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: f.ctrlKey || f.metaKey,
          shiftKey: f.shiftKey
        });
        return;
      case " ":
        f.preventDefault(), n === "multi" && s >= 0 && s < r.length && t("select", {
          nodeId: r[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        f.preventDefault(), y = 0;
        break;
      case "End":
        f.preventDefault(), y = r.length - 1;
        break;
      default:
        return;
    }
    y !== s && a(y);
  }, [s, r, t, n]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: P
    },
    r.map((f, y) => /* @__PURE__ */ e.createElement(
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
          y === s ? "tlTreeView__node--focused" : "",
          o === f.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === f.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === f.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: f.depth * il },
        draggable: c,
        onClick: (C) => h(f.id, C),
        onContextMenu: (C) => S(f.id, C),
        onDragStart: (C) => E(f.id, C),
        onDragOver: i ? (C) => _(f.id, C) : void 0,
        onDrop: i ? (C) => g(f.id, C) : void 0,
        onDragEnd: B
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
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(X, { control: f.content }))
    ))
  );
};
var He = { exports: {} }, ce = {}, We = { exports: {} }, U = {};
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
function dl() {
  if (ot) return U;
  ot = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), r = Symbol.for("react.fragment"), n = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
  function S(d) {
    return d === null || typeof d != "object" ? null : (d = h && d[h] || d["@@iterator"], typeof d == "function" ? d : null);
  }
  var b = {
    isMounted: function() {
      return !1;
    },
    enqueueForceUpdate: function() {
    },
    enqueueReplaceState: function() {
    },
    enqueueSetState: function() {
    }
  }, R = Object.assign, E = {};
  function _(d, v, $) {
    this.props = d, this.context = v, this.refs = E, this.updater = $ || b;
  }
  _.prototype.isReactComponent = {}, _.prototype.setState = function(d, v) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, v, "setState");
  }, _.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function g() {
  }
  g.prototype = _.prototype;
  function B(d, v, $) {
    this.props = d, this.context = v, this.refs = E, this.updater = $ || b;
  }
  var P = B.prototype = new g();
  P.constructor = B, R(P, _.prototype), P.isPureReactComponent = !0;
  var f = Array.isArray;
  function y() {
  }
  var C = { H: null, A: null, T: null, S: null }, q = Object.prototype.hasOwnProperty;
  function N(d, v, $) {
    var j = $.ref;
    return {
      $$typeof: l,
      type: d,
      key: v,
      ref: j !== void 0 ? j : null,
      props: $
    };
  }
  function T(d, v) {
    return N(d.type, v, d.props);
  }
  function F(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function x(d) {
    var v = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function($) {
      return v[$];
    });
  }
  var O = /\/+/g;
  function G(d, v) {
    return typeof d == "object" && d !== null && d.key != null ? x("" + d.key) : v.toString(36);
  }
  function re(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(y, y) : (d.status = "pending", d.then(
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
  function H(d, v, $, j, L) {
    var V = typeof d;
    (V === "undefined" || V === "boolean") && (d = null);
    var K = !1;
    if (d === null) K = !0;
    else
      switch (V) {
        case "bigint":
        case "string":
        case "number":
          K = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case l:
            case t:
              K = !0;
              break;
            case m:
              return K = d._init, H(
                K(d._payload),
                v,
                $,
                j,
                L
              );
          }
      }
    if (K)
      return L = L(d), K = j === "" ? "." + G(d, 0) : j, f(L) ? ($ = "", K != null && ($ = K.replace(O, "$&/") + "/"), H(L, v, $, "", function(ue) {
        return ue;
      })) : L != null && (F(L) && (L = T(
        L,
        $ + (L.key == null || d && d.key === L.key ? "" : ("" + L.key).replace(
          O,
          "$&/"
        ) + "/") + K
      )), v.push(L)), 1;
    K = 0;
    var le = j === "" ? "." : j + ":";
    if (f(d))
      for (var ee = 0; ee < d.length; ee++)
        j = d[ee], V = le + G(j, ee), K += H(
          j,
          v,
          $,
          V,
          L
        );
    else if (ee = S(d), typeof ee == "function")
      for (d = ee.call(d), ee = 0; !(j = d.next()).done; )
        j = j.value, V = le + G(j, ee++), K += H(
          j,
          v,
          $,
          V,
          L
        );
    else if (V === "object") {
      if (typeof d.then == "function")
        return H(
          re(d),
          v,
          $,
          j,
          L
        );
      throw v = String(d), Error(
        "Objects are not valid as a React child (found: " + (v === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : v) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return K;
  }
  function J(d, v, $) {
    if (d == null) return d;
    var j = [], L = 0;
    return H(d, j, "", "", function(V) {
      return v.call($, V, L++);
    }), j;
  }
  function A(d) {
    if (d._status === -1) {
      var v = d._result;
      v = v(), v.then(
        function($) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = $);
        },
        function($) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = $);
        }
      ), d._status === -1 && (d._status = 0, d._result = v);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var I = typeof reportError == "function" ? reportError : function(d) {
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
  }, Q = {
    map: J,
    forEach: function(d, v, $) {
      J(
        d,
        function() {
          v.apply(this, arguments);
        },
        $
      );
    },
    count: function(d) {
      var v = 0;
      return J(d, function() {
        v++;
      }), v;
    },
    toArray: function(d) {
      return J(d, function(v) {
        return v;
      }) || [];
    },
    only: function(d) {
      if (!F(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return U.Activity = p, U.Children = Q, U.Component = _, U.Fragment = r, U.Profiler = c, U.PureComponent = B, U.StrictMode = n, U.Suspense = s, U.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = C, U.__COMPILER_RUNTIME = {
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
  }, U.cloneElement = function(d, v, $) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var j = R({}, d.props), L = d.key;
    if (v != null)
      for (V in v.key !== void 0 && (L = "" + v.key), v)
        !q.call(v, V) || V === "key" || V === "__self" || V === "__source" || V === "ref" && v.ref === void 0 || (j[V] = v[V]);
    var V = arguments.length - 2;
    if (V === 1) j.children = $;
    else if (1 < V) {
      for (var K = Array(V), le = 0; le < V; le++)
        K[le] = arguments[le + 2];
      j.children = K;
    }
    return N(d.type, L, j);
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
  }, U.createElement = function(d, v, $) {
    var j, L = {}, V = null;
    if (v != null)
      for (j in v.key !== void 0 && (V = "" + v.key), v)
        q.call(v, j) && j !== "key" && j !== "__self" && j !== "__source" && (L[j] = v[j]);
    var K = arguments.length - 2;
    if (K === 1) L.children = $;
    else if (1 < K) {
      for (var le = Array(K), ee = 0; ee < K; ee++)
        le[ee] = arguments[ee + 2];
      L.children = le;
    }
    if (d && d.defaultProps)
      for (j in K = d.defaultProps, K)
        L[j] === void 0 && (L[j] = K[j]);
    return N(d, V, L);
  }, U.createRef = function() {
    return { current: null };
  }, U.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, U.isValidElement = F, U.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: A
    };
  }, U.memo = function(d, v) {
    return {
      $$typeof: a,
      type: d,
      compare: v === void 0 ? null : v
    };
  }, U.startTransition = function(d) {
    var v = C.T, $ = {};
    C.T = $;
    try {
      var j = d(), L = C.S;
      L !== null && L($, j), typeof j == "object" && j !== null && typeof j.then == "function" && j.then(y, I);
    } catch (V) {
      I(V);
    } finally {
      v !== null && $.types !== null && (v.types = $.types), C.T = v;
    }
  }, U.unstable_useCacheRefresh = function() {
    return C.H.useCacheRefresh();
  }, U.use = function(d) {
    return C.H.use(d);
  }, U.useActionState = function(d, v, $) {
    return C.H.useActionState(d, v, $);
  }, U.useCallback = function(d, v) {
    return C.H.useCallback(d, v);
  }, U.useContext = function(d) {
    return C.H.useContext(d);
  }, U.useDebugValue = function() {
  }, U.useDeferredValue = function(d, v) {
    return C.H.useDeferredValue(d, v);
  }, U.useEffect = function(d, v) {
    return C.H.useEffect(d, v);
  }, U.useEffectEvent = function(d) {
    return C.H.useEffectEvent(d);
  }, U.useId = function() {
    return C.H.useId();
  }, U.useImperativeHandle = function(d, v, $) {
    return C.H.useImperativeHandle(d, v, $);
  }, U.useInsertionEffect = function(d, v) {
    return C.H.useInsertionEffect(d, v);
  }, U.useLayoutEffect = function(d, v) {
    return C.H.useLayoutEffect(d, v);
  }, U.useMemo = function(d, v) {
    return C.H.useMemo(d, v);
  }, U.useOptimistic = function(d, v) {
    return C.H.useOptimistic(d, v);
  }, U.useReducer = function(d, v, $) {
    return C.H.useReducer(d, v, $);
  }, U.useRef = function(d) {
    return C.H.useRef(d);
  }, U.useState = function(d) {
    return C.H.useState(d);
  }, U.useSyncExternalStore = function(d, v, $) {
    return C.H.useSyncExternalStore(
      d,
      v,
      $
    );
  }, U.useTransition = function() {
    return C.H.useTransition();
  }, U.version = "19.2.4", U;
}
var st;
function ml() {
  return st || (st = 1, We.exports = dl()), We.exports;
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
function pl() {
  if (ct) return ce;
  ct = 1;
  var l = ml();
  function t(s) {
    var a = "https://react.dev/errors/" + s;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + s + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
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
  function i(s, a, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: c,
      key: p == null ? null : "" + p,
      children: s,
      containerInfo: a,
      implementation: m
    };
  }
  var o = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(s, a) {
    if (s === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return ce.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = n, ce.createPortal = function(s, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return i(s, a, null, m);
  }, ce.flushSync = function(s) {
    var a = o.T, m = n.p;
    try {
      if (o.T = null, n.p = 2, s) return s();
    } finally {
      o.T = a, n.p = m, n.d.f();
    }
  }, ce.preconnect = function(s, a) {
    typeof s == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, n.d.C(s, a));
  }, ce.prefetchDNS = function(s) {
    typeof s == "string" && n.d.D(s);
  }, ce.preinit = function(s, a) {
    if (typeof s == "string" && a && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, S = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? n.d.S(
        s,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: S
        }
      ) : m === "script" && n.d.X(s, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: S,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, ce.preinitModule = function(s, a) {
    if (typeof s == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = u(
            a.as,
            a.crossOrigin
          );
          n.d.M(s, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && n.d.M(s);
  }, ce.preload = function(s, a) {
    if (typeof s == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin);
      n.d.L(s, m, {
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
  }, ce.preloadModule = function(s, a) {
    if (typeof s == "string")
      if (a) {
        var m = u(a.as, a.crossOrigin);
        n.d.m(s, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else n.d.m(s);
  }, ce.requestFormReset = function(s) {
    n.d.r(s);
  }, ce.unstable_batchedUpdates = function(s, a) {
    return s(a);
  }, ce.useFormState = function(s, a, m) {
    return o.H.useFormState(s, a, m);
  }, ce.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, ce.version = "19.2.4", ce;
}
var it;
function fl() {
  if (it) return He.exports;
  it = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), He.exports = pl(), He.exports;
}
var hl = fl();
const { useState: be, useCallback: ie, useRef: Se, useEffect: Ce, useMemo: Ye } = e;
function Ze({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function _l({
  option: l,
  removable: t,
  onRemove: r,
  removeLabel: n,
  draggable: c,
  onDragStart: i,
  onDragOver: o,
  onDrop: u,
  onDragEnd: s,
  dragClassName: a
}) {
  const m = ie(
    (p) => {
      p.stopPropagation(), r(l.value);
    },
    [r, l.value]
  );
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlDropdownSelect__chip" + (a ? " " + a : ""),
      draggable: c || void 0,
      onDragStart: i,
      onDragOver: o,
      onDrop: u,
      onDragEnd: s
    },
    c && /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__dragHandle", "aria-hidden": "true" }, "⋮⋮"),
    /* @__PURE__ */ e.createElement(Ze, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__chipLabel" }, l.label),
    t && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__chipRemove",
        onClick: m,
        "aria-label": n
      },
      "×"
    )
  );
}
function bl({
  option: l,
  highlighted: t,
  searchTerm: r,
  onSelect: n,
  onMouseEnter: c,
  id: i
}) {
  const o = ie(() => n(l.value), [n, l.value]), u = Ye(() => {
    if (!r) return l.label;
    const s = l.label.toLowerCase().indexOf(r.toLowerCase());
    return s < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, s), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(s, s + r.length)), l.label.substring(s + r.length));
  }, [l.label, r]);
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
    /* @__PURE__ */ e.createElement(Ze, { image: l.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const vl = ({ controlId: l, state: t }) => {
  const r = ae(), n = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = i && c && !u && s, S = se({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = S["js.dropdownSelect.nothingFound"], R = ie(
    (k) => S["js.dropdownSelect.removeChip"].replace("{0}", k),
    [S]
  ), [E, _] = be(!1), [g, B] = be(""), [P, f] = be(-1), [y, C] = be(!1), [q, N] = be({}), [T, F] = be(null), [x, O] = be(null), [G, re] = be(null), H = Se(null), J = Se(null), A = Se(null), I = Se(n);
  I.current = n;
  const Q = Se(-1), d = Ye(
    () => new Set(n.map((k) => k.value)),
    [n]
  ), v = Ye(() => {
    let k = m.filter((M) => !d.has(M.value));
    if (g) {
      const M = g.toLowerCase();
      k = k.filter((z) => z.label.toLowerCase().includes(M));
    }
    return k;
  }, [m, d, g]);
  Ce(() => {
    g && v.length === 1 ? f(0) : f(-1);
  }, [v.length, g]), Ce(() => {
    E && a && J.current && J.current.focus();
  }, [E, a, n]), Ce(() => {
    var z, ne;
    if (Q.current < 0) return;
    const k = Q.current;
    Q.current = -1;
    const M = (z = H.current) == null ? void 0 : z.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    M && M.length > 0 ? M[Math.min(k, M.length - 1)].focus() : (ne = H.current) == null || ne.focus();
  }, [n]), Ce(() => {
    if (!E) return;
    const k = (M) => {
      H.current && !H.current.contains(M.target) && A.current && !A.current.contains(M.target) && (_(!1), B(""));
    };
    return document.addEventListener("mousedown", k), () => document.removeEventListener("mousedown", k);
  }, [E]), Ce(() => {
    if (!E || !H.current) return;
    const k = H.current.getBoundingClientRect(), M = window.innerHeight - k.bottom, ne = M < 300 && k.top > M;
    N({
      left: k.left,
      width: k.width,
      ...ne ? { bottom: window.innerHeight - k.top } : { top: k.bottom }
    });
  }, [E]);
  const $ = ie(async () => {
    if (!(u || !s) && (_(!0), B(""), f(-1), C(!1), !a))
      try {
        await r("loadOptions");
      } catch {
        C(!0);
      }
  }, [u, s, a, r]), j = ie(() => {
    var k;
    _(!1), B(""), f(-1), (k = H.current) == null || k.focus();
  }, []), L = ie(
    (k) => {
      let M;
      if (c) {
        const z = m.find((ne) => ne.value === k);
        if (z)
          M = [...I.current, z];
        else
          return;
      } else {
        const z = m.find((ne) => ne.value === k);
        if (z)
          M = [z];
        else
          return;
      }
      I.current = M, r("valueChanged", { value: M.map((z) => z.value) }), c ? (B(""), f(-1)) : j();
    },
    [c, m, r, j]
  ), V = ie(
    (k) => {
      Q.current = I.current.findIndex((z) => z.value === k);
      const M = I.current.filter((z) => z.value !== k);
      I.current = M, r("valueChanged", { value: M.map((z) => z.value) });
    },
    [r]
  ), K = ie(
    (k) => {
      k.stopPropagation(), r("valueChanged", { value: [] }), j();
    },
    [r, j]
  ), le = ie((k) => {
    B(k.target.value);
  }, []), ee = ie(
    (k) => {
      if (!E) {
        if (k.key === "ArrowDown" || k.key === "ArrowUp" || k.key === "Enter" || k.key === " ") {
          if (k.target.tagName === "BUTTON") return;
          k.preventDefault(), k.stopPropagation(), $();
        }
        return;
      }
      switch (k.key) {
        case "ArrowDown":
          k.preventDefault(), k.stopPropagation(), f(
            (M) => M < v.length - 1 ? M + 1 : 0
          );
          break;
        case "ArrowUp":
          k.preventDefault(), k.stopPropagation(), f(
            (M) => M > 0 ? M - 1 : v.length - 1
          );
          break;
        case "Enter":
          k.preventDefault(), k.stopPropagation(), P >= 0 && P < v.length && L(v[P].value);
          break;
        case "Escape":
          k.preventDefault(), k.stopPropagation(), j();
          break;
        case "Tab":
          j();
          break;
        case "Backspace":
          g === "" && c && n.length > 0 && V(n[n.length - 1].value);
          break;
      }
    },
    [
      E,
      $,
      j,
      v,
      P,
      L,
      g,
      c,
      n,
      V
    ]
  ), ue = ie(
    async (k) => {
      k.preventDefault(), C(!1);
      try {
        await r("loadOptions");
      } catch {
        C(!0);
      }
    },
    [r]
  ), me = ie(
    (k, M) => {
      F(k), M.dataTransfer.effectAllowed = "move", M.dataTransfer.setData("text/plain", String(k));
    },
    []
  ), he = ie(
    (k, M) => {
      if (M.preventDefault(), M.dataTransfer.dropEffect = "move", T === null || T === k) {
        O(null), re(null);
        return;
      }
      const z = M.currentTarget.getBoundingClientRect(), ne = z.left + z.width / 2, _e = M.clientX < ne ? "before" : "after";
      O(k), re(_e);
    },
    [T]
  ), w = ie(
    (k) => {
      if (k.preventDefault(), T === null || x === null || G === null || T === x) return;
      const M = [...I.current], [z] = M.splice(T, 1);
      let ne = x;
      T < x ? ne = G === "before" ? ne - 1 : ne : ne = G === "before" ? ne : ne + 1, M.splice(ne, 0, z), I.current = M, r("valueChanged", { value: M.map((_e) => _e.value) }), F(null), O(null), re(null);
    },
    [T, x, G, r]
  ), D = ie(() => {
    F(null), O(null), re(null);
  }, []);
  if (Ce(() => {
    if (P < 0 || !A.current) return;
    const k = A.current.querySelector(
      `[id="${l}-opt-${P}"]`
    );
    k && k.scrollIntoView({ block: "nearest" });
  }, [P, l]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : n.map((k) => /* @__PURE__ */ e.createElement("span", { key: k.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Ze, { image: k.image }), /* @__PURE__ */ e.createElement("span", null, k.label))));
  const Y = !o && n.length > 0 && !u, te = E ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: A,
      className: "tlDropdownSelect__dropdown",
      style: q
    },
    (a || y) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: J,
        type: "text",
        className: "tlDropdownSelect__search",
        value: g,
        onChange: le,
        onKeyDown: ee,
        placeholder: S["js.dropdownSelect.filterPlaceholder"],
        "aria-label": S["js.dropdownSelect.filterPlaceholder"],
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
      !a && !y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      y && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ue }, S["js.dropdownSelect.error"])),
      a && v.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      a && v.map((k, M) => /* @__PURE__ */ e.createElement(
        bl,
        {
          key: k.value,
          id: `${l}-opt-${M}`,
          option: k,
          highlighted: M === P,
          searchTerm: g,
          onSelect: L,
          onMouseEnter: () => f(M)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      ref: H,
      className: "tlDropdownSelect" + (E ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": E,
      "aria-haspopup": "listbox",
      "aria-owns": E ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: E ? void 0 : $,
      onKeyDown: ee
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, n.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : n.map((k, M) => {
      let z = "";
      return T === M ? z = "tlDropdownSelect__chip--dragging" : x === M && G === "before" ? z = "tlDropdownSelect__chip--dropBefore" : x === M && G === "after" && (z = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        _l,
        {
          key: k.value,
          option: k,
          removable: !u && (c || !o),
          onRemove: V,
          removeLabel: R(k.label),
          draggable: h,
          onDragStart: h ? (ne) => me(M, ne) : void 0,
          onDragOver: h ? (ne) => he(M, ne) : void 0,
          onDrop: h ? w : void 0,
          onDragEnd: h ? D : void 0,
          dragClassName: h ? z : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, Y && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: K,
        "aria-label": S["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, E ? "▲" : "▼"))
  ), te && hl.createPortal(te, document.body));
}, { useCallback: ze, useRef: El } = e, ft = "application/x-tl-color", gl = ({
  colors: l,
  columns: t,
  onSelect: r,
  onConfirm: n,
  onSwap: c,
  onReplace: i
}) => {
  const o = El(null), u = ze(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = ze((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = ze(
    (m) => (p) => {
      p.preventDefault();
      const h = p.dataTransfer.getData(ft);
      h ? i(m, h) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
    },
    [c, i]
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
        onClick: m != null ? () => r(m) : void 0,
        onDoubleClick: m != null ? () => n(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: s,
        onDrop: a(p)
      }
    ))
  );
};
function ht(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function Ge(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function _t(l) {
  if (!Ge(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function bt(l, t, r) {
  const n = (c) => ht(c).toString(16).padStart(2, "0");
  return "#" + n(l) + n(t) + n(r);
}
function Cl(l, t, r) {
  const n = l / 255, c = t / 255, i = r / 255, o = Math.max(n, c, i), u = Math.min(n, c, i), s = o - u;
  let a = 0;
  s !== 0 && (o === n ? a = (c - i) / s % 6 : o === c ? a = (i - n) / s + 2 : a = (n - c) / s + 4, a *= 60, a < 0 && (a += 360));
  const m = o === 0 ? 0 : s / o;
  return [a, m, o];
}
function yl(l, t, r) {
  const n = r * t, c = n * (1 - Math.abs(l / 60 % 2 - 1)), i = r - n;
  let o = 0, u = 0, s = 0;
  return l < 60 ? (o = n, u = c, s = 0) : l < 120 ? (o = c, u = n, s = 0) : l < 180 ? (o = 0, u = n, s = c) : l < 240 ? (o = 0, u = c, s = n) : l < 300 ? (o = c, u = 0, s = n) : (o = n, u = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((u + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function wl(l) {
  return Cl(..._t(l));
}
function Ue(l, t, r) {
  return bt(...yl(l, t, r));
}
const { useCallback: ye, useRef: ut } = e, kl = ({ color: l, onColorChange: t }) => {
  const [r, n, c] = wl(l), i = ut(null), o = ut(null), u = ye(
    (b, R) => {
      var B;
      const E = (B = i.current) == null ? void 0 : B.getBoundingClientRect();
      if (!E) return;
      const _ = Math.max(0, Math.min(1, (b - E.left) / E.width)), g = Math.max(0, Math.min(1, 1 - (R - E.top) / E.height));
      t(Ue(r, _, g));
    },
    [r, t]
  ), s = ye(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), a = ye(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = ye(
    (b) => {
      var g;
      const R = (g = o.current) == null ? void 0 : g.getBoundingClientRect();
      if (!R) return;
      const _ = Math.max(0, Math.min(1, (b - R.top) / R.height)) * 360;
      t(Ue(_, n, c));
    },
    [n, c, t]
  ), p = ye(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), h = ye(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), S = Ue(r, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: S },
      onPointerDown: s,
      onPointerMove: a
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
      ref: o,
      className: "tlColorInput__hueSlider",
      onPointerDown: p,
      onPointerMove: h
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
function Sl(l, t) {
  const r = t.toUpperCase();
  return l.some((n) => n != null && n.toUpperCase() === r);
}
const Nl = {
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
}, { useState: Le, useCallback: fe, useEffect: Ve, useRef: Rl, useLayoutEffect: Tl } = e, xl = ({
  anchorRef: l,
  currentColor: t,
  palette: r,
  paletteColumns: n,
  defaultPalette: c,
  canReset: i,
  onConfirm: o,
  onCancel: u,
  onPaletteChange: s
}) => {
  const [a, m] = Le("palette"), [p, h] = Le(t), S = Rl(null), b = se(Nl), [R, E] = Le(null);
  Tl(() => {
    if (!l.current || !S.current) return;
    const A = l.current.getBoundingClientRect(), I = S.current.getBoundingClientRect();
    let Q = A.bottom + 4, d = A.left;
    Q + I.height > window.innerHeight && (Q = A.top - I.height - 4), d + I.width > window.innerWidth && (d = Math.max(0, A.right - I.width)), E({ top: Q, left: d });
  }, [l]);
  const _ = p != null, [g, B, P] = _ ? _t(p) : [0, 0, 0], [f, y] = Le((p == null ? void 0 : p.toUpperCase()) ?? "");
  Ve(() => {
    y((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), Ve(() => {
    const A = (I) => {
      I.key === "Escape" && u();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [u]), Ve(() => {
    const A = (Q) => {
      S.current && !S.current.contains(Q.target) && u();
    }, I = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", A);
    };
  }, [u]);
  const C = fe(
    (A) => (I) => {
      const Q = parseInt(I.target.value, 10);
      if (isNaN(Q)) return;
      const d = ht(Q);
      h(bt(A === "r" ? d : g, A === "g" ? d : B, A === "b" ? d : P));
    },
    [g, B, P]
  ), q = fe(
    (A) => {
      if (p != null) {
        A.dataTransfer.setData(ft, p.toUpperCase()), A.dataTransfer.effectAllowed = "move";
        const I = document.createElement("div");
        I.style.width = "33px", I.style.height = "33px", I.style.backgroundColor = p, I.style.borderRadius = "3px", I.style.border = "1px solid rgba(0,0,0,0.1)", I.style.position = "absolute", I.style.top = "-9999px", document.body.appendChild(I), A.dataTransfer.setDragImage(I, 16, 16), requestAnimationFrame(() => document.body.removeChild(I));
      }
    },
    [p]
  ), N = fe((A) => {
    const I = A.target.value;
    y(I), Ge(I) && h(I);
  }, []), T = fe(() => {
    h(null);
  }, []), F = fe((A) => {
    h(A);
  }, []), x = fe(
    (A) => {
      o(A);
    },
    [o]
  ), O = fe(
    (A, I) => {
      const Q = [...r], d = Q[A];
      Q[A] = Q[I], Q[I] = d, s(Q);
    },
    [r, s]
  ), G = fe(
    (A, I) => {
      const Q = [...r];
      Q[A] = I, s(Q);
    },
    [r, s]
  ), re = fe(() => {
    s([...c]);
  }, [c, s]), H = fe(
    (A) => {
      if (Sl(r, A)) return;
      const I = r.indexOf(null);
      if (I < 0) return;
      const Q = [...r];
      Q[I] = A.toUpperCase(), s(Q);
    },
    [r, s]
  ), J = fe(() => {
    p != null && H(p), o(p);
  }, [p, o, H]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: S,
      style: R ? { top: R.top, left: R.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (a === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, a === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      gl,
      {
        colors: r,
        columns: n,
        onSelect: F,
        onConfirm: x,
        onSwap: O,
        onReplace: G
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: re }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(kl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (_ ? "" : " tlColorInput--noColor"),
        style: _ ? { backgroundColor: p } : void 0,
        draggable: _,
        onDragStart: _ ? q : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? g : "",
        onChange: C("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? B : "",
        onChange: C("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: _ ? P : "",
        onChange: C("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (f !== "" && !Ge(f) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: f,
        onChange: N
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: T }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: J }, b["js.colorInput.ok"]))
  );
}, Ll = { "js.colorInput.chooseColor": "Choose color" }, { useState: Dl, useCallback: De, useRef: Il } = e, Pl = ({ controlId: l, state: t }) => {
  const r = ae(), n = se(Ll), [c, i] = Dl(!1), o = Il(null), u = t.value, s = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, h = De(() => {
    s && i(!0);
  }, [s]), S = De(
    (E) => {
      i(!1), r("valueChanged", { value: E });
    },
    [r]
  ), b = De(() => {
    i(!1);
  }, []), R = De(
    (E) => {
      r("paletteChanged", { palette: E });
    },
    [r]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": n["js.colorInput.chooseColor"]
    }
  ), c && /* @__PURE__ */ e.createElement(
    xl,
    {
      anchorRef: o,
      currentColor: u,
      palette: a,
      paletteColumns: m,
      defaultPalette: p,
      canReset: t.canReset !== !1,
      onConfirm: S,
      onCancel: b,
      onPaletteChange: R
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
}, { useState: Ne, useCallback: ve, useEffect: Ie, useRef: dt, useLayoutEffect: jl, useMemo: Ml } = e, Al = {
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
}, Bl = ({
  anchorRef: l,
  currentValue: t,
  icons: r,
  iconsLoaded: n,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const u = se(Al), [s, a] = Ne("simple"), [m, p] = Ne(""), [h, S] = Ne(t ?? ""), [b, R] = Ne(!1), [E, _] = Ne(null), g = dt(null), B = dt(null);
  jl(() => {
    if (!l.current || !g.current) return;
    const x = l.current.getBoundingClientRect(), O = g.current.getBoundingClientRect();
    let G = x.bottom + 4, re = x.left;
    G + O.height > window.innerHeight && (G = x.top - O.height - 4), re + O.width > window.innerWidth && (re = Math.max(0, x.right - O.width)), _({ top: G, left: re });
  }, [l]), Ie(() => {
    !n && !b && o().catch(() => R(!0));
  }, [n, b, o]), Ie(() => {
    n && B.current && B.current.focus();
  }, [n]), Ie(() => {
    const x = (O) => {
      O.key === "Escape" && i();
    };
    return document.addEventListener("keydown", x), () => document.removeEventListener("keydown", x);
  }, [i]), Ie(() => {
    const x = (G) => {
      g.current && !g.current.contains(G.target) && i();
    }, O = setTimeout(() => document.addEventListener("mousedown", x), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", x);
    };
  }, [i]);
  const P = Ml(() => {
    if (!m) return r;
    const x = m.toLowerCase();
    return r.filter(
      (O) => O.prefix.toLowerCase().includes(x) || O.label.toLowerCase().includes(x) || O.terms != null && O.terms.some((G) => G.includes(x))
    );
  }, [r, m]), f = ve((x) => {
    p(x.target.value);
  }, []), y = ve(
    (x) => {
      c(x);
    },
    [c]
  ), C = ve((x) => {
    S(x);
  }, []), q = ve((x) => {
    S(x.target.value);
  }, []), N = ve(() => {
    c(h || null);
  }, [h, c]), T = ve(() => {
    c(null);
  }, [c]), F = ve(async (x) => {
    x.preventDefault(), R(!1);
    try {
      await o();
    } catch {
      R(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: g,
      style: E ? { top: E.top, left: E.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (s === "advanced" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("advanced")
      },
      u["js.iconSelect.advancedTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__searchIcon", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-magnifying-glass" })), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: B,
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
      !n && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: F }, u["js.iconSelect.loadError"])),
      n && P.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
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
            onClick: () => s === "simple" ? y(O.encoded) : C(O.encoded),
            onKeyDown: (G) => {
              (G.key === "Enter" || G.key === " ") && (G.preventDefault(), s === "simple" ? y(O.encoded) : C(O.encoded));
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
        value: h,
        onChange: q
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(we, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: T }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: N }, u["js.iconSelect.ok"]))
  );
}, Ol = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: $l, useCallback: Pe, useRef: Fl } = e, Hl = ({ controlId: l, state: t }) => {
  const r = ae(), n = se(Ol), [c, i] = $l(!1), o = Fl(null), u = t.value, s = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Pe(() => {
    s && !a && i(!0);
  }, [s, a]), S = Pe(
    (E) => {
      i(!1), r("valueChanged", { value: E });
    },
    [r]
  ), b = Pe(() => {
    i(!1);
  }, []), R = Pe(async () => {
    await r("loadIcons");
  }, [r]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: a,
      title: u ?? "",
      "aria-label": n["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Bl,
    {
      anchorRef: o,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: S,
      onCancel: b,
      onLoadIcons: R
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : null));
};
W("TLButton", It);
W("TLToggleButton", jt);
W("TLTextInput", Ct);
W("TLNumberInput", wt);
W("TLDatePicker", St);
W("TLSelect", Rt);
W("TLCheckbox", xt);
W("TLTable", Lt);
W("TLCounter", Mt);
W("TLTabBar", Bt);
W("TLFieldList", Ot);
W("TLAudioRecorder", Ft);
W("TLAudioPlayer", Wt);
W("TLFileUpload", Ut);
W("TLDownload", Kt);
W("TLPhotoCapture", Gt);
W("TLPhotoViewer", qt);
W("TLSplitPanel", Zt);
W("TLPanel", rn);
W("TLMaximizeRoot", an);
W("TLDeckPane", on);
W("TLSidebar", hn);
W("TLStack", _n);
W("TLGrid", bn);
W("TLCard", vn);
W("TLAppBar", En);
W("TLBreadcrumb", Cn);
W("TLBottomBar", wn);
W("TLDialog", Sn);
W("TLDialogManager", Tn);
W("TLWindow", Dn);
W("TLDrawer", Mn);
W("TLContextMenuRegion", Bn);
W("TLSnackbar", Hn);
W("TLMenu", zn);
W("TLAppShell", Un);
W("TLText", Vn);
W("TLTableView", Yn);
W("TLFormLayout", el);
W("TLFormGroup", ll);
W("TLFormField", sl);
W("TLResourceCell", cl);
W("TLTreeView", ul);
W("TLDropdownSelect", vl);
W("TLColorInput", Pl);
W("TLIconSelect", Hl);
