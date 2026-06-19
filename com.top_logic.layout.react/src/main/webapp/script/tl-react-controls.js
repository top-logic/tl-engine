import { React as e, useTLFieldValue as Te, getComponent as Rt, useTLState as q, useTLCommand as ne, TLChild as Y, useTLUpload as We, useI18N as oe, useTLDataUrl as De, register as z } from "tl-react-bridge";
const { useCallback: Lt } = e, Dt = ({ controlId: l, state: t }) => {
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
      placeholder: t.placeholder ?? void 0,
      onChange: i,
      disabled: t.disabled === !0,
      className: o,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: xt } = e, It = ({ controlId: l, state: t }) => {
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
}, { useCallback: Mt } = e, jt = ({ controlId: l, state: t, config: n }) => {
  const [r, i] = Te(), s = Mt(
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
}, { useCallback: Pt } = e, Bt = ({ controlId: l, state: t }) => {
  const [n, r] = Te(), i = Pt(
    (o) => {
      r(o.target.value || null);
    },
    [r]
  );
  if (t.editable === !1) {
    const o = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, o);
  }
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
}, { useCallback: At } = e, Ot = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [r, i] = Te(), s = At(
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
}, Wt = ({ controlId: l, state: t }) => {
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
function ve({ encoded: l, className: t }) {
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
const { useCallback: Ht } = e, zt = ({ controlId: l, command: t, label: n, image: r, disabled: i, displayMode: s }) => {
  const c = q(), u = ne(), o = t ?? "click", a = n ?? c.label, m = r ?? c.image, p = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", v = c.hidden === !0, f = c.tooltip, S = v ? { display: "none" } : void 0, y = c.appearance, g = c.size, C = c.navigateUrl, x = Ht(() => {
    if (C) {
      window.location.assign(C);
      return;
    }
    u(o);
  }, [u, o, C]), L = h === "icon-only", _ = h === "icon-only" || h === "icon-label", b = h === "label-only" || h === "icon-label" || L && !m, E = f ?? (L ? a : void 0), H = E ? `text:${E}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: x,
      disabled: p,
      style: S,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (y === "link" ? " tlReactButton--link" : "") + (g === "small" ? " tlReactButton--small" : "") + (g === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": H,
      "aria-label": L ? a : void 0
    },
    _ && m && /* @__PURE__ */ e.createElement(ve, { encoded: m, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Ut } = e, Vt = ({ controlId: l, command: t, label: n, active: r, disabled: i }) => {
  const s = q(), c = ne(), u = t ?? "click", o = n ?? s.label, a = r ?? s.active === !0, m = i ?? s.disabled === !0, p = Ut(() => {
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
}, Kt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, r), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Yt } = e, Gt = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.tabs ?? [], i = t.activeTabId, s = Yt((c) => {
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
    c.icon && /* @__PURE__ */ e.createElement(ve, { encoded: c.icon, className: "tlReactTabBar__tabIcon" }),
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, Xt = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, r.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: i })))));
}, qt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = q(), n = We(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), o = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : r !== "idle" ? r : m, v = e.useCallback(async () => {
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
        const x = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(C, x ? { mimeType: x } : void 0);
        u.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && o.current.push(_.data);
        }, L.onstop = async () => {
          C.getTracks().forEach((E) => E.stop()), a.current = null;
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
  }, [r, n]), f = oe(qt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], y = h === "uploading", g = ["tlAudioRecorder__button"];
  return h === "recording" && g.push("tlAudioRecorder__button--recording"), h === "uploading" && g.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: g.join(" "),
      onClick: v,
      disabled: y,
      title: S,
      "aria-label": S
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${h === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), s && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, f[s]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, Qt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Jt = ({ controlId: l }) => {
  const t = q(), n = De(), r = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(r ? "idle" : "disabled"), u = e.useRef(null), o = e.useRef(null), a = e.useRef(i);
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
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), c("idle");
          return;
        }
        const g = await y.blob();
        o.current = URL.createObjectURL(g);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), c("idle");
        return;
      }
    }
    const S = new Audio(o.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = oe(Qt), h = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
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
}, en = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, tn = ({ controlId: l }) => {
  const t = q(), n = We(), [r, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), o = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = o === "received" ? "idle" : r !== "idle" ? r : o, h = e.useCallback(async (_) => {
    i("uploading");
    const b = new FormData();
    b.append("file", _, _.name), await n(b), i("idle");
  }, [n]), v = e.useCallback((_) => {
    var E;
    const b = (E = _.target.files) == null ? void 0 : E[0];
    b && h(b);
  }, [h]), f = e.useCallback(() => {
    var _;
    r !== "uploading" && ((_ = u.current) == null || _.click());
  }, [r]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!0);
  }, []), y = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), g = e.useCallback((_) => {
    var E;
    if (_.preventDefault(), _.stopPropagation(), c(!1), r === "uploading") return;
    const b = (E = _.dataTransfer.files) == null ? void 0 : E[0];
    b && h(b);
  }, [r, h]), C = p === "uploading", x = oe(en), L = p === "uploading" ? x["js.uploading"] : x["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: S,
      onDragLeave: y,
      onDrop: g
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
        title: L,
        "aria-label": L
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    a && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, a)
  );
}, nn = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…",
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…"
}, ln = ({ controlId: l, state: t }) => {
  const r = q() ?? t ?? {}, i = We(), s = De(), c = oe(nn), u = r.editable !== !1, o = !!r.hasData, a = r.fileName ?? "download", m = r.dataRevision ?? 0, p = r.accept ?? "", h = r.status ?? "idle", v = r.error ?? null, [f, S] = e.useState("idle"), [y, g] = e.useState(!1), [C, x] = e.useState(!1), L = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!o || C)) {
      x(!0);
      try {
        const O = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(O);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const I = await M.blob(), G = URL.createObjectURL(I), d = document.createElement("a");
        d.href = G, d.download = a, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(G);
      } catch (O) {
        console.error("[TLBinaryField] Fetch error:", O);
      } finally {
        x(!1);
      }
    }
  }, [o, C, s, m, a]), b = e.useCallback(async (O) => {
    S("uploading");
    const M = new FormData();
    M.append("file", O, O.name), await i(M), S("idle");
  }, [i]), E = (h === "received" ? "idle" : f !== "idle" ? f : h) === "uploading", H = e.useCallback((O) => {
    var I;
    const M = (I = O.target.files) == null ? void 0 : I[0];
    M && b(M);
  }, [b]), B = e.useCallback(() => {
    var O;
    E || (O = L.current) == null || O.click();
  }, [E]), R = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), g(!0);
  }, []), K = e.useCallback((O) => {
    O.preventDefault(), O.stopPropagation(), g(!1);
  }, []), A = e.useCallback((O) => {
    var I;
    if (O.preventDefault(), O.stopPropagation(), g(!1), E) return;
    const M = (I = O.dataTransfer.files) == null ? void 0 : I[0];
    M && b(M);
  }, [E, b]), N = C ? c["js.downloading"] : c["js.download.file"].replace("{0}", a), $ = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (C ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: C,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a));
  if (!u)
    return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, $) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const J = E, F = E ? c["js.uploading"] : c["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${y ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: K,
      onDrop: A
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: L,
        type: "file",
        accept: p || void 0,
        onChange: H,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (J ? " tlFileUpload__button--uploading" : ""),
        onClick: B,
        disabled: J,
        title: F,
        "aria-label": F
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    o && $,
    v && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, v)
  );
}, rn = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, on = ({ controlId: l }) => {
  const t = q(), n = De(), r = ne(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [o, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || o)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const y = await S.blob(), g = URL.createObjectURL(y), C = document.createElement("a");
        C.href = g, C.download = c, C.style.display = "none", document.body.appendChild(C), C.click(), document.body.removeChild(C), URL.revokeObjectURL(g);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, o, n, s, c]), p = e.useCallback(async () => {
    i && await r("clear");
  }, [i, r]), h = oe(rn);
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
}, an = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, sn = ({ controlId: l }) => {
  const t = q(), n = We(), [r, i] = e.useState("idle"), [s, c] = e.useState(null), [u, o] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), v = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), g = e.useCallback(() => {
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
  }, [r, S]), x = e.useCallback(async () => {
    if (r !== "overlayOpen")
      return;
    const R = a.current, K = p.current;
    if (!R || !K)
      return;
    K.width = R.videoWidth, K.height = R.videoHeight;
    const A = K.getContext("2d");
    A && (A.drawImage(R, 0, 0), y(), i("uploading"), K.toBlob(async (N) => {
      if (!N) {
        i("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", N, "capture.jpg"), await n($), i("idle");
    }, "image/jpeg", 0.85));
  }, [r, n, y]), L = e.useCallback(async (R) => {
    var N;
    const K = (N = R.target.files) == null ? void 0 : N[0];
    if (!K) return;
    i("uploading");
    const A = new FormData();
    A.append("photo", K, K.name), await n(A), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    r === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
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
      K.key === "Escape" && g();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [r, g]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const _ = oe(an), b = r === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  r === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  u && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const B = ["tlPhotoCapture__mirrorBtn"];
  return u && B.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), r === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: v,
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
        className: H.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: B.join(" "),
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
}, cn = {
  "js.photoViewer.alt": "Captured photo"
}, un = ({ controlId: l }) => {
  const t = q(), n = De(), r = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
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
  const o = oe(cn);
  return !r || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: o["js.photoViewer.alt"]
    }
  ));
}, dn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, mn = ({ controlId: l }) => {
  const t = q(), n = De(), r = !!t.hasPdf, i = t.dataRevision ?? 0, s = oe(dn), u = n.indexOf("react-api/"), o = u >= 0 ? n.slice(0, u) : n, a = n + "&rev=" + i, m = o + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(a);
  return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: ot, useRef: He } = e, pn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = r === "horizontal", u = s.length > 0 && s.every((y) => y.collapsed), o = !u && s.some((y) => y.collapsed), a = u ? !c : c, m = He(null), p = He(null), h = He(null), v = ot((y, g) => {
    const C = {
      overflow: y.scrolling || "auto"
    };
    return y.collapsed ? u && !a ? C.flex = "1 0 0%" : C.flex = "0 0 auto" : g !== void 0 ? C.flex = `0 0 ${g}px` : C.flex = `${y.size} 1 0%`, y.minSize > 0 && !y.collapsed && (C.minWidth = c ? y.minSize : void 0, C.minHeight = c ? void 0 : y.minSize), C;
  }, [c, u, o, a]), f = ot((y, g) => {
    y.preventDefault();
    const C = m.current;
    if (!C) return;
    const x = s[g], L = s[g + 1], _ = C.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((B) => {
      b.push(c ? B.offsetWidth : B.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: g,
      startPos: c ? y.clientX : y.clientY,
      startSizeBefore: b[g],
      startSizeAfter: b[g + 1],
      childBefore: x,
      childAfter: L
    };
    const E = (B) => {
      const R = p.current;
      if (!R || !h.current) return;
      const A = (c ? B.clientX : B.clientY) - R.startPos, N = R.childBefore.minSize || 0, $ = R.childAfter.minSize || 0;
      let J = R.startSizeBefore + A, F = R.startSizeAfter - A;
      J < N && (F += J - N, J = N), F < $ && (J += F - $, F = $), h.current[R.splitterIndex] = J, h.current[R.splitterIndex + 1] = F;
      const O = C.querySelectorAll(":scope > .tlSplitPanel__child"), M = O[R.splitterIndex], I = O[R.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${J}px`), I && (I.style.flex = `0 0 ${F}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const B = {};
        s.forEach((R, K) => {
          const A = R.control;
          A != null && A.controlId && h.current && (B[A.controlId] = h.current[K]);
        }), n("updateSizes", { sizes: B });
      }
      h.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", H), document.body.style.cursor = c ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [s, c, n]), S = [];
  return s.forEach((y, g) => {
    if (S.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${g}`,
          className: `tlSplitPanel__child${y.collapsed && a ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: v(y)
        },
        /* @__PURE__ */ e.createElement(Y, { control: y.control })
      )
    ), i && g < s.length - 1) {
      const C = s[g + 1];
      !y.collapsed && !C.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${r}`,
            onMouseDown: (L) => f(L, g)
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
}, { useCallback: ze } = e, fn = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, hn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), bn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), _n = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), vn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), En = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), gn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(fn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, o = t.showPopOut === !0, a = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", h = s === "HIDDEN", v = ze(() => {
    n("toggleMinimize");
  }, [n]), f = ze(() => {
    n("toggleMaximize");
  }, [n]), S = ze(() => {
    n("popOut");
  }, [n]);
  if (h)
    return null;
  const y = p ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlPanel tlPanel--${s.toLowerCase()}${a ? " tlPanel--fullLine" : ""}`,
      style: y
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, t.toolbar && /* @__PURE__ */ e.createElement(Y, { control: t.toolbar }), c && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: m ? r["js.panel.restore"] : r["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(bn, null) : /* @__PURE__ */ e.createElement(hn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? r["js.panel.restore"] : r["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(vn, null) : /* @__PURE__ */ e.createElement(_n, null)
    ), o && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: r["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(En, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(Y, { control: t.buttonBar }))
  );
}, Cn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, wn = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: Oe, useEffect: $e, useRef: Fe } = e, yn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Qe(l, t, n, r) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: r });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: r }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...Qe(s.children, t, n, s.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ve, { encoded: l, className: "tlSidebar__icon" }) : null, kn = ({ item: l, active: t, collapsed: n, onSelect: r, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
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
), Sn = ({ item: l, collapsed: t, onExecute: n, tabIndex: r, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
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
), Nn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Tn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Rn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: r, onExecute: i, onClose: s }) => {
  const c = Fe(null);
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
}, Ln = ({
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
  const y = Fe(null), [g, C] = Oe(null), x = ue(() => {
    r ? v === l.id ? S() : (y.current && C(y.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [r, v, l.id, c, f, S]), L = ue((b) => {
    y.current = b, o(b);
  }, [o]), _ = r && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: x,
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
    Rn,
    {
      item: l,
      activeItemId: n,
      anchorRect: g,
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
        kn,
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
        Sn,
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
      return /* @__PURE__ */ e.createElement(Nn, { item: l, collapsed: n });
    case "separator":
      return /* @__PURE__ */ e.createElement(Tn, null);
    case "group": {
      const v = a ? a.get(l.id) ?? l.expanded : l.expanded;
      return /* @__PURE__ */ e.createElement(
        Ln,
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
}, Dn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(yn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, o = u ? !1 : c, [a, m] = Oe(() => {
    const N = /* @__PURE__ */ new Map(), $ = (J) => {
      for (const F of J)
        F.type === "group" && (N.set(F.id, F.expanded), $(F.children));
    };
    return $(i), N;
  }), p = ue((N) => {
    m(($) => {
      const J = new Map($), F = J.get(N) ?? !1;
      return J.set(N, !F), n("toggleGroup", { itemId: N, expanded: !F }), J;
    });
  }, [n]), h = ue((N) => {
    N !== s && n("selectItem", { itemId: N });
  }, [n, s]), v = ue((N) => {
    n("executeCommand", { itemId: N });
  }, [n]), f = ue(() => {
    n("toggleCollapse", {});
  }, [n]), S = ue(() => {
    n("toggleDrawer", {});
  }, [n]), [y, g] = Oe(null), C = ue((N) => {
    g(N);
  }, []), x = ue(() => {
    g(null);
  }, []);
  $e(() => {
    o || g(null);
  }, [o]);
  const [L, _] = Oe(() => {
    const N = Qe(i, o, a);
    return N.length > 0 ? N[0].id : "";
  }), b = Fe(/* @__PURE__ */ new Map()), E = ue((N) => ($) => {
    $ ? b.current.set(N, $) : b.current.delete(N);
  }, []), H = ue((N) => {
    _(N);
  }, []), B = Fe(0), R = ue((N) => {
    _(N), B.current++;
  }, []);
  $e(() => {
    const N = b.current.get(L);
    N && document.activeElement !== N && N.focus();
  }, [L, B.current]);
  const K = ue((N) => {
    if (N.key === "Escape" && y !== null) {
      N.preventDefault(), x();
      return;
    }
    const $ = Qe(i, o, a);
    if ($.length === 0) return;
    const J = $.findIndex((O) => O.id === L);
    if (J < 0) return;
    const F = $[J];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const O = (J + 1) % $.length;
        R($[O].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const O = (J - 1 + $.length) % $.length;
        R($[O].id);
        break;
      }
      case "Home": {
        N.preventDefault(), R($[0].id);
        break;
      }
      case "End": {
        N.preventDefault(), R($[$.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        N.preventDefault(), F.type === "nav" ? h(F.id) : F.type === "command" ? v(F.id) : F.type === "group" && (o ? y === F.id ? x() : C(F.id) : p(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !o && ((a.get(F.id) ?? !1) || (N.preventDefault(), p(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !o && (a.get(F.id) ?? !1) && (N.preventDefault(), p(F.id));
        break;
      }
    }
  }, [
    i,
    o,
    a,
    L,
    y,
    R,
    h,
    v,
    p,
    C,
    x
  ]), A = "tlSidebar" + (o ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: A }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(Y, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": r["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, i.map((N) => /* @__PURE__ */ e.createElement(
    gt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: o,
      onSelect: h,
      onExecute: v,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: E,
      onItemFocus: H,
      groupStates: a,
      flyoutGroupId: y,
      onOpenFlyout: C,
      onCloseFlyout: x
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, xn = ({ controlId: l }) => {
  const t = q(), n = t.direction ?? "column", r = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], o = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${r}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: o }, u.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a })));
}, In = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(Y, { control: t.child }));
}, Mn = ({ controlId: l }) => {
  const t = q(), n = t.columns, r = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return r ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${r}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, o) => /* @__PURE__ */ e.createElement(Y, { key: o, control: u })));
}, jn = ({ controlId: l }) => {
  const t = q(), n = t.title, r = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${r}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((o, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: o })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(Y, { control: c })));
}, Pn = ({ controlId: l }) => {
  const t = q(), n = t.title ?? "", r = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", o = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: o }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: r })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a }))));
}, { useCallback: Bn } = e, An = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = Bn((s) => {
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
}, { useCallback: On } = e, $n = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.items ?? [], i = t.activeItemId, s = On((c) => {
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
}, { useCallback: at, useEffect: st, useRef: Fn } = e, Wn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Fn(null), u = at(() => {
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
    /* @__PURE__ */ e.createElement(Y, { control: s })
  ) : null;
}, { useEffect: Hn, useRef: zn } = e, Un = ({ controlId: l }) => {
  const n = q().dialogs ?? [], r = zn(n.length);
  return Hn(() => {
    n.length < r.current && n.length > 0, r.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(Y, { key: i.controlId, control: i })));
}, { useCallback: Ie, useRef: we, useState: Me } = e, Vn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Kn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Yn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(Vn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, o = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [v, f] = Me(null), [S, y] = Me(null), [g, C] = Me(null), x = we(null), [L, _] = Me(!1), b = we(null), E = we(null), H = we(null), B = we(null), R = we(null), K = Ie(() => {
    n("close");
  }, [n]), A = Ie((O, M) => {
    M.preventDefault();
    const I = B.current;
    if (!I) return;
    const G = I.getBoundingClientRect(), d = !x.current, k = x.current ?? { x: G.left, y: G.top };
    d && (x.current = k, C(k)), R.current = {
      dir: O,
      startX: M.clientX,
      startY: M.clientY,
      startW: G.width,
      startH: G.height,
      startPos: { ...k },
      symmetric: d
    };
    const U = (X) => {
      const P = R.current;
      if (!P) return;
      const Q = X.clientX - P.startX, le = X.clientY - P.startY;
      let te = P.startW, ie = P.startH, de = 0, me = 0;
      P.symmetric ? (P.dir.includes("e") && (te = P.startW + 2 * Q), P.dir.includes("w") && (te = P.startW - 2 * Q), P.dir.includes("s") && (ie = P.startH + 2 * le), P.dir.includes("n") && (ie = P.startH - 2 * le)) : (P.dir.includes("e") && (te = P.startW + Q), P.dir.includes("w") && (te = P.startW - Q, de = Q), P.dir.includes("s") && (ie = P.startH + le), P.dir.includes("n") && (ie = P.startH - le, me = le));
      const he = Math.max(200, te), be = Math.max(100, ie);
      P.symmetric ? (de = (P.startW - he) / 2, me = (P.startH - be) / 2) : (P.dir.includes("w") && he === 200 && (de = P.startW - 200), P.dir.includes("n") && be === 100 && (me = P.startH - 100)), E.current = he, H.current = be, f(he), y(be);
      const T = {
        x: P.startPos.x + de,
        y: P.startPos.y + me
      };
      x.current = T, C(T);
    }, W = () => {
      document.removeEventListener("mousemove", U), document.removeEventListener("mouseup", W);
      const X = E.current, P = H.current;
      (X != null || P != null) && n("resize", {
        ...X != null ? { width: Math.round(X) + "px" } : {},
        ...P != null ? { height: Math.round(P) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", U), document.addEventListener("mouseup", W);
  }, [n]), N = Ie((O) => {
    if (O.button !== 0 || O.target.closest("button")) return;
    O.preventDefault();
    const M = B.current;
    if (!M) return;
    const I = M.getBoundingClientRect(), G = x.current ?? { x: I.left, y: I.top }, d = O.clientX - G.x, k = O.clientY - G.y, U = (X) => {
      const P = window.innerWidth, Q = window.innerHeight;
      let le = X.clientX - d, te = X.clientY - k;
      const ie = M.offsetWidth, de = M.offsetHeight;
      le + ie > P && (le = P - ie), te + de > Q && (te = Q - de), le < 0 && (le = 0), te < 0 && (te = 0);
      const me = { x: le, y: te };
      x.current = me, C(me);
    }, W = () => {
      document.removeEventListener("mousemove", U), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", U), document.addEventListener("mouseup", W);
  }, []), $ = Ie(() => {
    var O, M;
    if (L) {
      const I = b.current;
      I && (C(I.x !== -1 ? { x: I.x, y: I.y } : null), f(I.w), y(I.h)), _(!1);
    } else {
      const I = B.current, G = I == null ? void 0 : I.getBoundingClientRect();
      b.current = {
        x: ((O = x.current) == null ? void 0 : O.x) ?? (G == null ? void 0 : G.left) ?? -1,
        y: ((M = x.current) == null ? void 0 : M.y) ?? (G == null ? void 0 : G.top) ?? -1,
        w: v ?? (G == null ? void 0 : G.width) ?? null,
        h: S ?? null
      }, _(!0), C({ x: 0, y: 0 }), f(null), y(null);
    }
  }, [L, v, S]), J = L ? { position: "absolute", top: 0, left: 0, width: "100vw", height: "100vh", maxHeight: "100vh", borderRadius: 0 } : {
    width: v != null ? v + "px" : s,
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
      style: J,
      ref: B,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": F
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: `tlWindow__header${L ? " tlWindow__header--maximized" : ""}`,
        onMouseDown: L ? void 0 : N,
        onDoubleClick: o ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(Y, { control: p })),
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
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Y, { control: a })),
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(Y, { control: h }), m.map((O, M) => /* @__PURE__ */ e.createElement(Y, { key: M, control: O }))),
    o && !L && Kn.map((O) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: O,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${O}`,
        onMouseDown: (M) => A(O, M)
      }
    ))
  );
}, { useCallback: Gn, useEffect: Xn } = e, qn = {
  "js.drawer.close": "Close"
}, Zn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(qn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, o = t.child, a = Gn(() => {
    n("close");
  }, [n]);
  Xn(() => {
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, o && /* @__PURE__ */ e.createElement(Y, { control: o })));
}, { useCallback: Qn } = e, Jn = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.child, i = Qn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, r && /* @__PURE__ */ e.createElement(Y, { control: r }));
}, { useCallback: el, useEffect: tl, useState: nl } = e, ll = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, o = t.generation ?? 0, [a, m] = nl(!1), p = el(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: o }), m(!1);
    }, 200);
  }, [n, o]);
  return tl(() => {
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
}, { useCallback: Ue, useEffect: Ve, useRef: rl, useState: ct } = e, ol = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], o = rl(null), [a, m] = ct({ top: 0, left: 0 }), [p, h] = ct(0), v = u.filter((g) => g.type === "item" && !g.disabled);
  Ve(() => {
    var E, H;
    if (!r) return;
    const g = ((E = o.current) == null ? void 0 : E.offsetHeight) ?? 200, C = ((H = o.current) == null ? void 0 : H.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let B = c, R = s;
      B + g > window.innerHeight && (B = Math.max(0, window.innerHeight - g)), R + C > window.innerWidth && (R = Math.max(0, window.innerWidth - C)), m({ top: B, left: R }), h(0);
      return;
    }
    if (!i) return;
    const x = document.getElementById(i);
    if (!x) return;
    const L = x.getBoundingClientRect();
    let _ = L.bottom + 4, b = L.left;
    _ + g > window.innerHeight && (_ = L.top - g - 4), b + C > window.innerWidth && (b = L.right - C), m({ top: _, left: b }), h(0);
  }, [r, i, s, c]);
  const f = Ue(() => {
    n("close");
  }, [n]), S = Ue((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  Ve(() => {
    if (!r) return;
    const g = (C) => {
      o.current && !o.current.contains(C.target) && f();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [r, f]);
  const y = Ue((g) => {
    if (g.key === "Escape") {
      f();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), h((C) => (C + 1) % v.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), h((C) => (C - 1 + v.length) % v.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const C = v[p];
      C && S(C.id);
    }
  }, [f, S, v, p]);
  return Ve(() => {
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
      onKeyDown: y
    },
    u.map((g, C) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: C, className: "tlMenu__separator" });
      const L = v.indexOf(g) === p;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: g.id,
          type: "button",
          className: "tlMenu__item" + (L ? " tlMenu__item--focused" : "") + (g.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: g.disabled,
          tabIndex: L ? 0 : -1,
          onClick: () => S(g.id)
        },
        g.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + g.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, g.label)
      );
    })
  ) : null;
}, al = 768, sl = ({ controlId: l }) => {
  const t = q(), n = ne();
  e.useEffect(() => {
    const a = window.matchMedia(`(max-width: ${al}px)`), m = (h) => {
      n("reportDisplayClass", { displayClass: h ? "COMPACT" : "REGULAR" });
    };
    m(a.matches);
    const p = (h) => m(h.matches);
    return a.addEventListener("change", p), () => a.removeEventListener("change", p);
  }, [n]);
  const r = t.header, i = t.content, s = t.footer, c = t.snackbar, u = t.dialogManager, o = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, r && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: r })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: i })), s && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: s })), /* @__PURE__ */ e.createElement(Y, { control: c }), u && /* @__PURE__ */ e.createElement(Y, { control: u }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, cl = ({ controlId: l }) => {
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
}, il = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, ul = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(il), i = e.useRef(null);
  e.useEffect(() => {
    const T = i.current;
    if (!T) return;
    const j = (w) => {
      const D = w.detail;
      let V = D.target;
      for (; V && V !== T; ) {
        const ee = V.dataset.row, re = V.dataset.col;
        if (ee != null && re != null) {
          D.resolved = { key: ee + "|" + re };
          return;
        }
        V = V.parentElement;
      }
    };
    return T.addEventListener("tl-tooltip-resolve", j), () => T.removeEventListener("tl-tooltip-resolve", j);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], o = t.rowHeight ?? 36, a = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, v = e.useMemo(
    () => s.filter((T) => T.sortPriority && T.sortPriority > 0).length,
    [s]
  ), f = a === "multi", S = 40, y = 20, g = e.useRef(null), C = e.useRef(null), x = e.useRef(null), [L, _] = e.useState({}), b = e.useRef(null), E = e.useRef(!1), H = e.useRef(null), [B, R] = e.useState(null), [K, A] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [s]);
  const N = e.useCallback((T) => L[T.name] ?? T.width, [L]), $ = e.useMemo(() => {
    const T = [];
    let j = f && p > 0 ? S : 0;
    for (let w = 0; w < p && w < s.length; w++)
      T.push(j), j += N(s[w]);
    return T;
  }, [s, p, f, S, N]), J = c * o, F = e.useRef(null), O = e.useCallback((T, j, w) => {
    w.preventDefault(), w.stopPropagation(), b.current = { column: T, startX: w.clientX, startWidth: j };
    let D = w.clientX, V = 0;
    const ee = () => {
      const ae = b.current;
      if (!ae) return;
      const pe = Math.max(it, ae.startWidth + (D - ae.startX) + V);
      _((Ce) => ({ ...Ce, [ae.column]: pe }));
    }, re = () => {
      const ae = C.current, pe = g.current;
      if (!ae || !b.current) return;
      const Ce = ae.getBoundingClientRect(), nt = 40, lt = 8, Tt = ae.scrollLeft;
      D > Ce.right - nt ? ae.scrollLeft += lt : D < Ce.left + nt && (ae.scrollLeft = Math.max(0, ae.scrollLeft - lt));
      const rt = ae.scrollLeft - Tt;
      rt !== 0 && (pe && (pe.scrollLeft = ae.scrollLeft), V += rt, ee()), F.current = requestAnimationFrame(re);
    };
    F.current = requestAnimationFrame(re);
    const ge = (ae) => {
      D = ae.clientX, ee();
    }, xe = (ae) => {
      document.removeEventListener("mousemove", ge), document.removeEventListener("mouseup", xe), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = b.current;
      if (pe) {
        const Ce = Math.max(it, pe.startWidth + (ae.clientX - pe.startX) + V);
        n("columnResize", { column: pe.column, width: Ce }), b.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ge), document.addEventListener("mouseup", xe);
  }, [n]), M = e.useCallback(() => {
    g.current && C.current && (g.current.scrollLeft = C.current.scrollLeft), x.current !== null && clearTimeout(x.current), x.current = window.setTimeout(() => {
      const T = C.current;
      if (!T) return;
      const j = T.scrollTop, w = Math.ceil(T.clientHeight / o), D = Math.floor(j / o);
      n("scroll", { start: D, count: w });
    }, 80);
  }, [n, o]), I = e.useCallback((T, j, w) => {
    if (E.current) return;
    let D;
    !j || j === "desc" ? D = "asc" : D = "desc";
    const V = w.shiftKey ? "add" : "replace";
    n("sort", { column: T, direction: D, mode: V });
  }, [n]), G = e.useCallback((T, j) => {
    H.current = T, j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", T);
  }, []), d = e.useCallback((T, j) => {
    if (!H.current || H.current === T) {
      R(null);
      return;
    }
    j.preventDefault(), j.dataTransfer.dropEffect = "move";
    const w = j.currentTarget.getBoundingClientRect(), D = j.clientX < w.left + w.width / 2 ? "left" : "right";
    R({ column: T, side: D });
  }, []), k = e.useCallback((T) => {
    T.preventDefault(), T.stopPropagation();
    const j = H.current;
    if (!j || !B) {
      H.current = null, R(null);
      return;
    }
    let w = s.findIndex((V) => V.name === B.column);
    if (w < 0) {
      H.current = null, R(null);
      return;
    }
    const D = s.findIndex((V) => V.name === j);
    B.side === "right" && w++, D < w && w--, n("columnReorder", { column: j, targetIndex: w }), H.current = null, R(null);
  }, [s, B, n]), U = e.useCallback(() => {
    H.current = null, R(null);
  }, []), W = e.useCallback((T, j) => {
    j.shiftKey && j.preventDefault(), n("select", {
      rowIndex: T,
      ctrlKey: j.ctrlKey || j.metaKey,
      shiftKey: j.shiftKey
    });
  }, [n]), X = e.useCallback((T, j) => {
    j.stopPropagation(), n("select", { rowIndex: T, ctrlKey: !0, shiftKey: !1 });
  }, [n]), P = e.useCallback(() => {
    const T = m === c && c > 0;
    n("selectAll", { selected: !T });
  }, [n, m, c]), Q = e.useCallback((T, j, w) => {
    w.stopPropagation(), n("expand", { rowIndex: T, expanded: j });
  }, [n]), le = e.useCallback((T, j) => {
    j.preventDefault(), A({ x: j.clientX, y: j.clientY, colIdx: T });
  }, []), te = e.useCallback(() => {
    K && (n("setFrozenColumnCount", { count: K.colIdx + 1 }), A(null));
  }, [K, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), A(null);
  }, [n]);
  e.useEffect(() => {
    if (!K) return;
    const T = () => A(null), j = (w) => {
      w.key === "Escape" && A(null);
    };
    return document.addEventListener("mousedown", T), document.addEventListener("keydown", j), () => {
      document.removeEventListener("mousedown", T), document.removeEventListener("keydown", j);
    };
  }, [K]);
  const de = s.reduce((T, j) => T + N(j), 0) + (f ? S : 0), me = m === c && c > 0, he = m > 0 && m < c, be = e.useCallback((T) => {
    T && (T.indeterminate = he);
  }, [he]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      id: l,
      className: "tlTableView",
      "data-tooltip": "dynamic",
      onDragOver: (T) => {
        if (!H.current) return;
        T.preventDefault();
        const j = C.current, w = g.current;
        if (!j) return;
        const D = j.getBoundingClientRect(), V = 40, ee = 8;
        T.clientX < D.left + V ? j.scrollLeft = Math.max(0, j.scrollLeft - ee) : T.clientX > D.right - V && (j.scrollLeft += ee), w && (w.scrollLeft = j.scrollLeft);
      },
      onDrop: k
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
        onDragOver: (T) => {
          H.current && (T.preventDefault(), T.dataTransfer.dropEffect = "move", s.length > 0 && s[0].name !== H.current && R({ column: s[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: be,
          className: "tlTableView__checkbox",
          checked: me,
          onChange: P
        }
      )
    ), s.map((T, j) => {
      const w = N(T);
      s.length - 1;
      let D = "tlTableView__headerCell";
      T.sortable && (D += " tlTableView__headerCell--sortable"), B && B.column === T.name && (D += " tlTableView__headerCell--dragOver-" + B.side);
      const V = j < p, ee = j === p - 1;
      return V && (D += " tlTableView__headerCell--frozen"), ee && (D += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.name,
          className: D,
          style: {
            width: w,
            minWidth: w,
            position: V ? "sticky" : "relative",
            ...V ? { left: $[j], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: T.sortable ? (re) => I(T.name, T.sortDirection, re) : void 0,
          onContextMenu: (re) => le(j, re),
          onDragStart: (re) => G(T.name, re),
          onDragOver: (re) => d(T.name, re),
          onDrop: k,
          onDragEnd: U
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, T.label),
        T.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, T.sortDirection === "asc" ? "▲" : "▼", v > 1 && T.sortPriority != null && T.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, T.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (re) => O(T.name, w, re)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (T) => {
          if (H.current && s.length > 0) {
            const j = s[s.length - 1];
            j.name !== H.current && (T.preventDefault(), T.dataTransfer.dropEffect = "move", R({ column: j.name, side: "right" }));
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
        onScroll: M
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: J, position: "relative", width: de } }, u.map((T) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.id,
          className: "tlTableView__row" + (T.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: T.index * o,
            height: o,
            width: de
          },
          onClick: (j) => W(T.index, j)
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
            onClick: (j) => j.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: T.selected,
              onChange: () => {
              },
              onClick: (j) => X(T.index, j),
              tabIndex: -1
            }
          )
        ),
        s.map((j, w) => {
          const D = N(j), V = w === s.length - 1, ee = w < p, re = w === p - 1;
          let ge = "tlTableView__cell";
          ee && (ge += " tlTableView__cell--frozen"), re && (ge += " tlTableView__cell--frozenLast");
          const xe = h && w === 0, ae = T.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: j.name,
              className: ge,
              "data-row": T.id,
              "data-col": j.name,
              style: {
                ...V && !ee ? { flex: "1 0 auto", minWidth: D } : { width: D, minWidth: D },
                ...ee ? { position: "sticky", left: $[w], zIndex: 2 } : {}
              }
            },
            xe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * y } }, T.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (pe) => Q(T.index, !T.expanded, pe)
              },
              T.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: T.cells[j.name] })) : /* @__PURE__ */ e.createElement(Y, { control: T.cells[j.name] })
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
        onMouseDown: (T) => T.stopPropagation()
      },
      K.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, r["js.table.unfreezeAll"]))
    )
  );
}, dl = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, Ct = e.createContext(dl), { useMemo: ml, useRef: pl, useState: fl, useEffect: hl } = e, bl = 320, _l = ({ controlId: l }) => {
  const t = q(), n = t.maxColumns ?? 3, r = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = pl(null), [o, a] = fl(
    r === "top" ? "top" : "side"
  );
  hl(() => {
    if (r !== "auto") {
      a(r);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((y) => {
      for (const g of y) {
        const x = g.contentRect.width / n;
        a(x < bl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [r, n]);
  const m = ml(() => ({
    readOnly: i,
    resolvedLabelPosition: o
  }), [i, o]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(Ct.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(Y, { key: S, control: f }))));
}, { useCallback: vl } = e, El = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, gl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = oe(El), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, o = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, h = vl(() => {
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
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, /* @__PURE__ */ e.createElement(Y, { control: i })), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, s.map((f, S) => /* @__PURE__ */ e.createElement(Y, { key: S, control: f })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((f, S) => /* @__PURE__ */ e.createElement(Y, { key: S, control: f }))));
}, { useContext: Cl, useState: wl, useCallback: yl } = e, kl = ({ controlId: l }) => {
  const t = q(), n = Cl(Ct), r = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, o = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, v = t.field, f = n.readOnly, [S, y] = wl(!1), g = yl(() => y((_) => !_), []);
  if (!p) return null;
  const C = s != null, x = c != null && c.length > 0, L = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    C ? "tlFormField--error" : "",
    !C && x ? "tlFormField--warning" : "",
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: v })), !f && C && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
}, Sl = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, o = t.hasLink, a = r ? /* @__PURE__ */ e.createElement("i", { className: r }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
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
}, Nl = 20, Tl = () => {
  const l = q(), t = ne(), n = l.nodes ?? [], r = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [o, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, b) => {
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
    const E = b.getBoundingClientRect(), H = _.clientY - E.top, B = E.height / 3;
    return H < B ? "above" : H > B * 2 ? "below" : "within";
  }, []), y = e.useCallback((_, b) => {
    b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", _);
  }, []), g = e.useCallback((_, b) => {
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const E = S(b, b.currentTarget);
    f.current != null && window.clearTimeout(f.current), f.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: E }), f.current = null;
    }, 50);
  }, [t, S]), C = e.useCallback((_, b) => {
    b.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = S(b, b.currentTarget);
    t("drop", { nodeId: _, position: E });
  }, [t, S]), x = e.useCallback(() => {
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
            for (let B = o - 1; B >= 0; B--)
              if (n[B].depth < H) {
                b = B;
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
        style: { paddingLeft: _.depth * Nl },
        draggable: i,
        onClick: (E) => h(_.id, E),
        onContextMenu: (E) => v(_.id, E),
        onDragStart: (E) => y(_.id, E),
        onDragOver: s ? (E) => g(_.id, E) : void 0,
        onDrop: s ? (E) => C(_.id, E) : void 0,
        onDragEnd: x
      },
      _.expandable ? /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlTreeView__toggle",
          onClick: (E) => {
            E.stopPropagation(), p(_.id, _.expanded);
          },
          tabIndex: -1,
          "aria-label": _.expanded ? "Collapse" : "Expand"
        },
        _.loading ? /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__spinner" }) : /* @__PURE__ */ e.createElement("span", { className: _.expanded ? "tlTreeView__chevron--down" : "tlTreeView__chevron--right" })
      ) : /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__toggleSpacer" }),
      /* @__PURE__ */ e.createElement("span", { className: "tlTreeView__content" }, /* @__PURE__ */ e.createElement(Y, { control: _.content }))
    ))
  );
};
var Ke = { exports: {} }, se = {}, Ye = { exports: {} }, Z = {};
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
function Rl() {
  if (ut) return Z;
  ut = 1;
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
  }, S = Object.assign, y = {};
  function g(d, k, U) {
    this.props = d, this.context = k, this.refs = y, this.updater = U || f;
  }
  g.prototype.isReactComponent = {}, g.prototype.setState = function(d, k) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, k, "setState");
  }, g.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function C() {
  }
  C.prototype = g.prototype;
  function x(d, k, U) {
    this.props = d, this.context = k, this.refs = y, this.updater = U || f;
  }
  var L = x.prototype = new C();
  L.constructor = x, S(L, g.prototype), L.isPureReactComponent = !0;
  var _ = Array.isArray;
  function b() {
  }
  var E = { H: null, A: null, T: null, S: null }, H = Object.prototype.hasOwnProperty;
  function B(d, k, U) {
    var W = U.ref;
    return {
      $$typeof: l,
      type: d,
      key: k,
      ref: W !== void 0 ? W : null,
      props: U
    };
  }
  function R(d, k) {
    return B(d.type, k, d.props);
  }
  function K(d) {
    return typeof d == "object" && d !== null && d.$$typeof === l;
  }
  function A(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(U) {
      return k[U];
    });
  }
  var N = /\/+/g;
  function $(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? A("" + d.key) : k.toString(36);
  }
  function J(d) {
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
  function F(d, k, U, W, X) {
    var P = typeof d;
    (P === "undefined" || P === "boolean") && (d = null);
    var Q = !1;
    if (d === null) Q = !0;
    else
      switch (P) {
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
                U,
                W,
                X
              );
          }
      }
    if (Q)
      return X = X(d), Q = W === "" ? "." + $(d, 0) : W, _(X) ? (U = "", Q != null && (U = Q.replace(N, "$&/") + "/"), F(X, k, U, "", function(ie) {
        return ie;
      })) : X != null && (K(X) && (X = R(
        X,
        U + (X.key == null || d && d.key === X.key ? "" : ("" + X.key).replace(
          N,
          "$&/"
        ) + "/") + Q
      )), k.push(X)), 1;
    Q = 0;
    var le = W === "" ? "." : W + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        W = d[te], P = le + $(W, te), Q += F(
          W,
          k,
          U,
          P,
          X
        );
    else if (te = v(d), typeof te == "function")
      for (d = te.call(d), te = 0; !(W = d.next()).done; )
        W = W.value, P = le + $(W, te++), Q += F(
          W,
          k,
          U,
          P,
          X
        );
    else if (P === "object") {
      if (typeof d.then == "function")
        return F(
          J(d),
          k,
          U,
          W,
          X
        );
      throw k = String(d), Error(
        "Objects are not valid as a React child (found: " + (k === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : k) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return Q;
  }
  function O(d, k, U) {
    if (d == null) return d;
    var W = [], X = 0;
    return F(d, W, "", "", function(P) {
      return k.call(U, P, X++);
    }), W;
  }
  function M(d) {
    if (d._status === -1) {
      var k = d._result;
      k = k(), k.then(
        function(U) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = U);
        },
        function(U) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = U);
        }
      ), d._status === -1 && (d._status = 0, d._result = k);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var I = typeof reportError == "function" ? reportError : function(d) {
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
  }, G = {
    map: O,
    forEach: function(d, k, U) {
      O(
        d,
        function() {
          k.apply(this, arguments);
        },
        U
      );
    },
    count: function(d) {
      var k = 0;
      return O(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return O(d, function(k) {
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
  return Z.Activity = p, Z.Children = G, Z.Component = g, Z.Fragment = n, Z.Profiler = i, Z.PureComponent = x, Z.StrictMode = r, Z.Suspense = o, Z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, Z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, Z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, Z.cacheSignal = function() {
    return null;
  }, Z.cloneElement = function(d, k, U) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var W = S({}, d.props), X = d.key;
    if (k != null)
      for (P in k.key !== void 0 && (X = "" + k.key), k)
        !H.call(k, P) || P === "key" || P === "__self" || P === "__source" || P === "ref" && k.ref === void 0 || (W[P] = k[P]);
    var P = arguments.length - 2;
    if (P === 1) W.children = U;
    else if (1 < P) {
      for (var Q = Array(P), le = 0; le < P; le++)
        Q[le] = arguments[le + 2];
      W.children = Q;
    }
    return B(d.type, X, W);
  }, Z.createContext = function(d) {
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
  }, Z.createElement = function(d, k, U) {
    var W, X = {}, P = null;
    if (k != null)
      for (W in k.key !== void 0 && (P = "" + k.key), k)
        H.call(k, W) && W !== "key" && W !== "__self" && W !== "__source" && (X[W] = k[W]);
    var Q = arguments.length - 2;
    if (Q === 1) X.children = U;
    else if (1 < Q) {
      for (var le = Array(Q), te = 0; te < Q; te++)
        le[te] = arguments[te + 2];
      X.children = le;
    }
    if (d && d.defaultProps)
      for (W in Q = d.defaultProps, Q)
        X[W] === void 0 && (X[W] = Q[W]);
    return B(d, P, X);
  }, Z.createRef = function() {
    return { current: null };
  }, Z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, Z.isValidElement = K, Z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: M
    };
  }, Z.memo = function(d, k) {
    return {
      $$typeof: a,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, Z.startTransition = function(d) {
    var k = E.T, U = {};
    E.T = U;
    try {
      var W = d(), X = E.S;
      X !== null && X(U, W), typeof W == "object" && W !== null && typeof W.then == "function" && W.then(b, I);
    } catch (P) {
      I(P);
    } finally {
      k !== null && U.types !== null && (k.types = U.types), E.T = k;
    }
  }, Z.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, Z.use = function(d) {
    return E.H.use(d);
  }, Z.useActionState = function(d, k, U) {
    return E.H.useActionState(d, k, U);
  }, Z.useCallback = function(d, k) {
    return E.H.useCallback(d, k);
  }, Z.useContext = function(d) {
    return E.H.useContext(d);
  }, Z.useDebugValue = function() {
  }, Z.useDeferredValue = function(d, k) {
    return E.H.useDeferredValue(d, k);
  }, Z.useEffect = function(d, k) {
    return E.H.useEffect(d, k);
  }, Z.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, Z.useId = function() {
    return E.H.useId();
  }, Z.useImperativeHandle = function(d, k, U) {
    return E.H.useImperativeHandle(d, k, U);
  }, Z.useInsertionEffect = function(d, k) {
    return E.H.useInsertionEffect(d, k);
  }, Z.useLayoutEffect = function(d, k) {
    return E.H.useLayoutEffect(d, k);
  }, Z.useMemo = function(d, k) {
    return E.H.useMemo(d, k);
  }, Z.useOptimistic = function(d, k) {
    return E.H.useOptimistic(d, k);
  }, Z.useReducer = function(d, k, U) {
    return E.H.useReducer(d, k, U);
  }, Z.useRef = function(d) {
    return E.H.useRef(d);
  }, Z.useState = function(d) {
    return E.H.useState(d);
  }, Z.useSyncExternalStore = function(d, k, U) {
    return E.H.useSyncExternalStore(
      d,
      k,
      U
    );
  }, Z.useTransition = function() {
    return E.H.useTransition();
  }, Z.version = "19.2.4", Z;
}
var dt;
function Ll() {
  return dt || (dt = 1, Ye.exports = Rl()), Ye.exports;
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
function Dl() {
  if (mt) return se;
  mt = 1;
  var l = Ll();
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
function xl() {
  if (pt) return Ke.exports;
  pt = 1;
  function l() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(l);
      } catch (t) {
        console.error(t);
      }
  }
  return l(), Ke.exports = Dl(), Ke.exports;
}
var wt = xl();
const { useState: _e, useCallback: ce, useRef: Re, useEffect: ye, useMemo: Je } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Il({
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
function Ml({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: r,
  onMouseEnter: i,
  id: s
}) {
  const c = ce(() => r(l.value), [r, l.value]), u = Je(() => {
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
const jl = ({ controlId: l, state: t }) => {
  const n = ne(), r = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, o = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = s && i && !u && o, v = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = v["js.dropdownSelect.nothingFound"], S = ce(
    (w) => v["js.dropdownSelect.removeChip"].replace("{0}", w),
    [v]
  ), [y, g] = _e(!1), [C, x] = _e(""), [L, _] = _e(-1), [b, E] = _e(!1), [H, B] = _e({}), [R, K] = _e(null), [A, N] = _e(null), [$, J] = _e(null), F = Re(null), O = Re(null), M = Re(null), I = Re(r);
  I.current = r;
  const G = Re(-1), d = Je(
    () => new Set(r.map((w) => w.value)),
    [r]
  ), k = Je(() => {
    let w = m.filter((D) => !d.has(D.value));
    if (C) {
      const D = C.toLowerCase();
      w = w.filter((V) => V.label.toLowerCase().includes(D));
    }
    return w;
  }, [m, d, C]);
  ye(() => {
    C && k.length === 1 ? _(0) : _(-1);
  }, [k.length, C]), ye(() => {
    y && a && O.current && O.current.focus();
  }, [y, a, r]), ye(() => {
    var V, ee;
    if (G.current < 0) return;
    const w = G.current;
    G.current = -1;
    const D = (V = F.current) == null ? void 0 : V.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    D && D.length > 0 ? D[Math.min(w, D.length - 1)].focus() : (ee = F.current) == null || ee.focus();
  }, [r]), ye(() => {
    if (!y) return;
    const w = (D) => {
      F.current && !F.current.contains(D.target) && M.current && !M.current.contains(D.target) && (g(!1), x(""));
    };
    return document.addEventListener("mousedown", w), () => document.removeEventListener("mousedown", w);
  }, [y]), ye(() => {
    if (!y || !F.current) return;
    const w = F.current.getBoundingClientRect(), D = window.innerHeight - w.bottom, ee = D < 300 && w.top > D;
    B({
      left: w.left,
      width: w.width,
      ...ee ? { bottom: window.innerHeight - w.top } : { top: w.bottom }
    });
  }, [y]);
  const U = ce(async () => {
    if (!(u || !o) && (g(!0), x(""), _(-1), E(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, o, a, n]), W = ce(() => {
    var w;
    g(!1), x(""), _(-1), (w = F.current) == null || w.focus();
  }, []), X = ce(
    (w) => {
      let D;
      if (i) {
        const V = m.find((ee) => ee.value === w);
        if (V)
          D = [...I.current, V];
        else
          return;
      } else {
        const V = m.find((ee) => ee.value === w);
        if (V)
          D = [V];
        else
          return;
      }
      I.current = D, n("valueChanged", { value: D.map((V) => V.value) }), i ? (x(""), _(-1)) : W();
    },
    [i, m, n, W]
  ), P = ce(
    (w) => {
      G.current = I.current.findIndex((V) => V.value === w);
      const D = I.current.filter((V) => V.value !== w);
      I.current = D, n("valueChanged", { value: D.map((V) => V.value) });
    },
    [n]
  ), Q = ce(
    (w) => {
      w.stopPropagation(), n("valueChanged", { value: [] }), W();
    },
    [n, W]
  ), le = ce((w) => {
    x(w.target.value);
  }, []), te = ce(
    (w) => {
      if (!y) {
        if (w.key === "ArrowDown" || w.key === "ArrowUp" || w.key === "Enter" || w.key === " ") {
          if (w.target.tagName === "BUTTON") return;
          w.preventDefault(), w.stopPropagation(), U();
        }
        return;
      }
      switch (w.key) {
        case "ArrowDown":
          w.preventDefault(), w.stopPropagation(), _(
            (D) => D < k.length - 1 ? D + 1 : 0
          );
          break;
        case "ArrowUp":
          w.preventDefault(), w.stopPropagation(), _(
            (D) => D > 0 ? D - 1 : k.length - 1
          );
          break;
        case "Enter":
          w.preventDefault(), w.stopPropagation(), L >= 0 && L < k.length && X(k[L].value);
          break;
        case "Escape":
          w.preventDefault(), w.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          C === "" && i && r.length > 0 && P(r[r.length - 1].value);
          break;
      }
    },
    [
      y,
      U,
      W,
      k,
      L,
      X,
      C,
      i,
      r,
      P
    ]
  ), ie = ce(
    async (w) => {
      w.preventDefault(), E(!1);
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
    },
    [n]
  ), de = ce(
    (w, D) => {
      K(w), D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", String(w));
    },
    []
  ), me = ce(
    (w, D) => {
      if (D.preventDefault(), D.dataTransfer.dropEffect = "move", R === null || R === w) {
        N(null), J(null);
        return;
      }
      const V = D.currentTarget.getBoundingClientRect(), ee = V.left + V.width / 2, re = D.clientX < ee ? "before" : "after";
      N(w), J(re);
    },
    [R]
  ), he = ce(
    (w) => {
      if (w.preventDefault(), R === null || A === null || $ === null || R === A) return;
      const D = [...I.current], [V] = D.splice(R, 1);
      let ee = A;
      R < A ? ee = $ === "before" ? ee - 1 : ee : ee = $ === "before" ? ee : ee + 1, D.splice(ee, 0, V), I.current = D, n("valueChanged", { value: D.map((re) => re.value) }), K(null), N(null), J(null);
    },
    [R, A, $, n]
  ), be = ce(() => {
    K(null), N(null), J(null);
  }, []);
  if (ye(() => {
    if (L < 0 || !M.current) return;
    const w = M.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    w && w.scrollIntoView({ block: "nearest" });
  }, [L, l]), !o)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : r.map((w) => /* @__PURE__ */ e.createElement("span", { key: w.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: w.image }), /* @__PURE__ */ e.createElement("span", null, w.label))));
  const T = !c && r.length > 0 && !u, j = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (a || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: O,
        type: "text",
        className: "tlDropdownSelect__search",
        value: C,
        onChange: le,
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
      a && k.map((w, D) => /* @__PURE__ */ e.createElement(
        Ml,
        {
          key: w.value,
          id: `${l}-opt-${D}`,
          option: w,
          highlighted: D === L,
          searchTerm: C,
          onSelect: X,
          onMouseEnter: () => _(D)
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
      onClick: y ? void 0 : U,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, r.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : r.map((w, D) => {
      let V = "";
      return R === D ? V = "tlDropdownSelect__chip--dragging" : A === D && $ === "before" ? V = "tlDropdownSelect__chip--dropBefore" : A === D && $ === "after" && (V = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Il,
        {
          key: w.value,
          option: w,
          removable: !u && (i || !c),
          onRemove: P,
          removeLabel: S(w.label),
          draggable: h,
          onDragStart: h ? (ee) => de(D, ee) : void 0,
          onDragOver: h ? (ee) => me(D, ee) : void 0,
          onDrop: h ? he : void 0,
          onDragEnd: h ? be : void 0,
          dragClassName: h ? V : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, T && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: Q,
        "aria-label": v["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, y ? "▲" : "▼"))
  ), j && wt.createPortal(j, document.body));
}, { useCallback: Ge, useRef: Pl } = e, yt = "application/x-tl-color", Bl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: r,
  onSwap: i,
  onReplace: s
}) => {
  const c = Pl(null), u = Ge(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), o = Ge((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), a = Ge(
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
function kt(l) {
  return Math.max(0, Math.min(255, Math.round(l)));
}
function et(l) {
  return /^#[0-9a-fA-F]{6}$/.test(l);
}
function St(l) {
  if (!et(l)) return [0, 0, 0];
  const t = parseInt(l.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function Nt(l, t, n) {
  const r = (i) => kt(i).toString(16).padStart(2, "0");
  return "#" + r(l) + r(t) + r(n);
}
function Al(l, t, n) {
  const r = l / 255, i = t / 255, s = n / 255, c = Math.max(r, i, s), u = Math.min(r, i, s), o = c - u;
  let a = 0;
  o !== 0 && (c === r ? a = (i - s) / o % 6 : c === i ? a = (s - r) / o + 2 : a = (r - i) / o + 4, a *= 60, a < 0 && (a += 360));
  const m = c === 0 ? 0 : o / c;
  return [a, m, c];
}
function Ol(l, t, n) {
  const r = n * t, i = r * (1 - Math.abs(l / 60 % 2 - 1)), s = n - r;
  let c = 0, u = 0, o = 0;
  return l < 60 ? (c = r, u = i, o = 0) : l < 120 ? (c = i, u = r, o = 0) : l < 180 ? (c = 0, u = r, o = i) : l < 240 ? (c = 0, u = i, o = r) : l < 300 ? (c = i, u = 0, o = r) : (c = r, u = 0, o = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((o + s) * 255)
  ];
}
function $l(l) {
  return Al(...St(l));
}
function Xe(l, t, n) {
  return Nt(...Ol(l, t, n));
}
const { useCallback: ke, useRef: ft } = e, Fl = ({ color: l, onColorChange: t }) => {
  const [n, r, i] = $l(l), s = ft(null), c = ft(null), u = ke(
    (f, S) => {
      var x;
      const y = (x = s.current) == null ? void 0 : x.getBoundingClientRect();
      if (!y) return;
      const g = Math.max(0, Math.min(1, (f - y.left) / y.width)), C = Math.max(0, Math.min(1, 1 - (S - y.top) / y.height));
      t(Xe(n, g, C));
    },
    [n, t]
  ), o = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), u(f.clientX, f.clientY);
    },
    [u]
  ), a = ke(
    (f) => {
      f.buttons !== 0 && u(f.clientX, f.clientY);
    },
    [u]
  ), m = ke(
    (f) => {
      var C;
      const S = (C = c.current) == null ? void 0 : C.getBoundingClientRect();
      if (!S) return;
      const g = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Xe(g, r, i));
    },
    [r, i, t]
  ), p = ke(
    (f) => {
      f.preventDefault(), f.target.setPointerCapture(f.pointerId), m(f.clientY);
    },
    [m]
  ), h = ke(
    (f) => {
      f.buttons !== 0 && m(f.clientY);
    },
    [m]
  ), v = Xe(n, 1, 1);
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
function Wl(l, t) {
  const n = t.toUpperCase();
  return l.some((r) => r != null && r.toUpperCase() === n);
}
const Hl = {
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
}, { useState: je, useCallback: fe, useEffect: qe, useRef: zl, useLayoutEffect: Ul } = e, Vl = ({
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
  const [a, m] = je("palette"), [p, h] = je(t), v = zl(null), f = oe(Hl), [S, y] = je(null);
  Ul(() => {
    if (!l.current || !v.current) return;
    const M = l.current.getBoundingClientRect(), I = v.current.getBoundingClientRect();
    let G = M.bottom + 4, d = M.left;
    G + I.height > window.innerHeight && (G = M.top - I.height - 4), d + I.width > window.innerWidth && (d = Math.max(0, M.right - I.width)), y({ top: G, left: d });
  }, [l]);
  const g = p != null, [C, x, L] = g ? St(p) : [0, 0, 0], [_, b] = je((p == null ? void 0 : p.toUpperCase()) ?? "");
  qe(() => {
    b((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), qe(() => {
    const M = (I) => {
      I.key === "Escape" && u();
    };
    return document.addEventListener("keydown", M), () => document.removeEventListener("keydown", M);
  }, [u]), qe(() => {
    const M = (G) => {
      v.current && !v.current.contains(G.target) && u();
    }, I = setTimeout(() => document.addEventListener("mousedown", M), 0);
    return () => {
      clearTimeout(I), document.removeEventListener("mousedown", M);
    };
  }, [u]);
  const E = fe(
    (M) => (I) => {
      const G = parseInt(I.target.value, 10);
      if (isNaN(G)) return;
      const d = kt(G);
      h(Nt(M === "r" ? d : C, M === "g" ? d : x, M === "b" ? d : L));
    },
    [C, x, L]
  ), H = fe(
    (M) => {
      if (p != null) {
        M.dataTransfer.setData(yt, p.toUpperCase()), M.dataTransfer.effectAllowed = "move";
        const I = document.createElement("div");
        I.style.width = "33px", I.style.height = "33px", I.style.backgroundColor = p, I.style.borderRadius = "3px", I.style.border = "1px solid rgba(0,0,0,0.1)", I.style.position = "absolute", I.style.top = "-9999px", document.body.appendChild(I), M.dataTransfer.setDragImage(I, 16, 16), requestAnimationFrame(() => document.body.removeChild(I));
      }
    },
    [p]
  ), B = fe((M) => {
    const I = M.target.value;
    b(I), et(I) && h(I);
  }, []), R = fe(() => {
    h(null);
  }, []), K = fe((M) => {
    h(M);
  }, []), A = fe(
    (M) => {
      c(M);
    },
    [c]
  ), N = fe(
    (M, I) => {
      const G = [...n], d = G[M];
      G[M] = G[I], G[I] = d, o(G);
    },
    [n, o]
  ), $ = fe(
    (M, I) => {
      const G = [...n];
      G[M] = I, o(G);
    },
    [n, o]
  ), J = fe(() => {
    o([...i]);
  }, [i, o]), F = fe(
    (M) => {
      if (Wl(n, M)) return;
      const I = n.indexOf(null);
      if (I < 0) return;
      const G = [...n];
      G[I] = M.toUpperCase(), o(G);
    },
    [n, o]
  ), O = fe(() => {
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
      Bl,
      {
        colors: n,
        columns: r,
        onSelect: K,
        onConfirm: A,
        onSwap: N,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: J }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(Fl, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        onDragStart: g ? H : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? C : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? x : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? L : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !et(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: B
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: O }, f["js.colorInput.ok"]))
  );
}, Kl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Yl, useCallback: Pe, useRef: Gl } = e, Xl = ({ controlId: l, state: t }) => {
  const n = ne(), r = oe(Kl), [i, s] = Yl(!1), c = Gl(null), u = t.value, o = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, h = Pe(() => {
    o && s(!0);
  }, [o]), v = Pe(
    (y) => {
      s(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Pe(() => {
    s(!1);
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
    Vl,
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
}, { useState: Le, useCallback: Ee, useEffect: Be, useRef: ht, useLayoutEffect: ql, useMemo: Zl } = e, Ql = {
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
}, Jl = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: r,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = oe(Ql), [o, a] = Le("simple"), [m, p] = Le(""), [h, v] = Le(t ?? ""), [f, S] = Le(!1), [y, g] = Le(null), C = ht(null), x = ht(null);
  ql(() => {
    if (!l.current || !C.current) return;
    const A = l.current.getBoundingClientRect(), N = C.current.getBoundingClientRect();
    let $ = A.bottom + 4, J = A.left;
    $ + N.height > window.innerHeight && ($ = A.top - N.height - 4), J + N.width > window.innerWidth && (J = Math.max(0, A.right - N.width)), g({ top: $, left: J });
  }, [l]), Be(() => {
    !r && !f && c().catch(() => S(!0));
  }, [r, f, c]), Be(() => {
    r && x.current && x.current.focus();
  }, [r]), Be(() => {
    const A = (N) => {
      N.key === "Escape" && s();
    };
    return document.addEventListener("keydown", A), () => document.removeEventListener("keydown", A);
  }, [s]), Be(() => {
    const A = ($) => {
      C.current && !C.current.contains($.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", A), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", A);
    };
  }, [s]);
  const L = Zl(() => {
    if (!m) return n;
    const A = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(A) || N.label.toLowerCase().includes(A) || N.terms != null && N.terms.some(($) => $.includes(A))
    );
  }, [n, m]), _ = Ee((A) => {
    p(A.target.value);
  }, []), b = Ee(
    (A) => {
      i(A);
    },
    [i]
  ), E = Ee((A) => {
    v(A);
  }, []), H = Ee((A) => {
    v(A.target.value);
  }, []), B = Ee(() => {
    i(h || null);
  }, [h, i]), R = Ee(() => {
    i(null);
  }, [i]), K = Ee(async (A) => {
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
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      r && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      r && L.map(
        (A) => A.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: A.label,
            onClick: () => o === "simple" ? b(N.encoded) : E(N.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), o === "simple" ? b(N.encoded) : E(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ve, { encoded: N.encoded })
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
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(ve, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    o === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: B }, u["js.iconSelect.ok"]))
  );
}, er = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: tr, useCallback: Ae, useRef: nr } = e, lr = ({ controlId: l, state: t }) => {
  const n = ne(), r = oe(er), [i, s] = tr(!1), c = nr(null), u = t.value, o = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Ae(() => {
    o && !a && s(!0);
  }, [o, a]), v = Ae(
    (y) => {
      s(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Ae(() => {
    s(!1);
  }, []), S = Ae(async () => {
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
    u ? /* @__PURE__ */ e.createElement(ve, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Jl,
    {
      anchorRef: c,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: v,
      onCancel: f,
      onLoadIcons: S
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(ve, { encoded: u }) : null));
}, { useCallback: Se, useEffect: rr, useMemo: bt, useRef: or, useState: Ze } = e, ar = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, sr = [1, 2, 3, 4];
function cr(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const r = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? r * t : r;
}
function ir(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let r = 1;
  for (const i of sr)
    n >= i && (r = i);
  return r;
}
function ur(l, t) {
  const n = ar[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function dr(l, t) {
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
    let v = Math.min(ur(p.width, n), n);
    for (; i(u, o); )
      o++, o >= n && (o = 0, u++);
    let f = 0;
    for (let x = o; x < n && !i(u, x); x++)
      f++;
    if (v > f) {
      for (a(u), o = 0, u++; i(u, o); )
        o++, o >= n && (o = 0, u++);
      f = 0;
      for (let x = o; x < n && !i(u, x); x++)
        f++;
      v = Math.min(v, f);
    }
    const S = o, y = o + v, g = u, C = u + h;
    c.push({ id: p.id, colStart: S, colEnd: y, rowStart: g, rowEnd: C });
    for (let x = g; x < C; x++)
      for (let L = S; L < y; L++) s(x, L);
    o = y, o >= n && (o = 0, u++);
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
const mr = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), s = or(null), [c, u] = Ze(1), o = t.editMode === !0;
  rr(() => {
    const b = s.current;
    if (!b) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = cr(r, E), B = () => u(ir(b.clientWidth, H));
    B();
    const R = new ResizeObserver(B);
    return R.observe(b), () => R.disconnect();
  }, [r]);
  const a = bt(() => dr(i, c), [i, c]), m = bt(() => {
    const b = {};
    for (const E of a) b[E.id] = E;
    return b;
  }, [a]), [p, h] = Ze(null), [v, f] = Ze(null), S = Se((b, E) => {
    if (!o) {
      b.preventDefault();
      return;
    }
    h(E), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", E);
  }, [o]), y = Se((b, E) => {
    if (!o || !p || p === E) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const H = b.currentTarget.getBoundingClientRect(), B = b.clientX < H.left + H.width / 2;
    f((R) => R && R.id === E && R.before === B ? R : { id: E, before: B });
  }, [o, p]), g = Se(() => {
  }, []), C = Se((b, E, H) => {
    const B = i.map((N) => N.id), R = B.indexOf(b);
    if (R < 0) return;
    B.splice(R, 1);
    const K = B.indexOf(E);
    if (K < 0) {
      B.splice(R, 0, b);
      return;
    }
    const A = H ? K : K + 1;
    B.splice(A, 0, b), n("reorder", { order: B });
  }, [i, n]), x = Se((b, E) => {
    if (!o || !p || p === E) return;
    b.preventDefault();
    const H = b.currentTarget.getBoundingClientRect(), B = b.clientX < H.left + H.width / 2;
    C(p, E, B), h(null), f(null);
  }, [o, p, C]), L = Se(() => {
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
      const E = m[b.id];
      if (!E) return null;
      const H = {
        gridColumn: `${E.colStart + 1} / ${E.colEnd + 1}`,
        gridRow: `${E.rowStart + 1} / ${E.rowEnd + 1}`
      }, B = ["tlDashboard__tile"];
      return p === b.id && B.push("tlDashboard__tile--dragging"), v && v.id === b.id && B.push(v.before ? "tlDashboard__tile--dropBefore" : "tlDashboard__tile--dropAfter"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: b.id,
          className: B.join(" "),
          style: H,
          draggable: o,
          onDragStart: (R) => S(R, b.id),
          onDragOver: (R) => y(R, b.id),
          onDragLeave: g,
          onDrop: (R) => x(R, b.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(Y, { control: b.control }),
        o && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: pr, useRef: _t, useState: vt, useEffect: Et, useLayoutEffect: fr } = e, hr = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, r) => /* @__PURE__ */ e.createElement("span", { key: r, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: n }))));
}, br = ({ group: l }) => {
  var p, h;
  const [t, n] = vt(!1), [r, i] = vt({}), s = _t(null), c = _t(null), u = pr(() => {
    n((v) => !v);
  }, []);
  fr(() => {
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
  if (o.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: o[0] })));
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
    m ? /* @__PURE__ */ e.createElement(ve, { encoded: l.icon, className: "tlToolbar__menuIcon" }) : /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement("span", null, a), /* @__PURE__ */ e.createElement("svg", { className: "tlToolbar__chevron", viewBox: "0 0 24 24", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("polyline", { points: "6,9 12,15 18,9" })))
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
      o.map((v, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: v }))),
      (h = l.subGroups) == null ? void 0 : h.map((v, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, y) => /* @__PURE__ */ e.createElement("div", { key: y, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: S })))))
    ),
    document.body
  ));
}, _r = ({ controlId: l }) => {
  const r = (q().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return r.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, r.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(br, { group: i }) : /* @__PURE__ */ e.createElement(hr, { group: i }))));
}, vr = ({ controlId: l }) => {
  const t = q();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(Y, { control: t.frame }));
}, Er = ({ controlId: l }) => {
  const t = q(), n = ne(), r = t.content, i = t.breadcrumb ?? null;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAdaptiveDetail" }, i && i.length > 0 && /* @__PURE__ */ e.createElement("nav", { className: "tlAdaptiveDetail__breadcrumb", "aria-label": "Breadcrumb" }, i.map((s, c) => {
    const u = c === i.length - 1;
    return /* @__PURE__ */ e.createElement(e.Fragment, { key: s.depth }, c > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__sep" }, "›"), u ? /* @__PURE__ */ e.createElement("span", { className: "tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current" }, s.label) : /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlAdaptiveDetail__crumb",
        onClick: () => n("navigate", { depth: s.depth })
      },
      s.label
    ));
  })), /* @__PURE__ */ e.createElement("div", { className: "tlAdaptiveDetail__content" }, r && /* @__PURE__ */ e.createElement(Y, { control: r })));
}, gr = ({ controlId: l }) => {
  const n = q().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((r, i) => /* @__PURE__ */ e.createElement(Y, { key: i, control: r })));
}, Cr = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), wr = {
  "js.sidebar.openDrawer": "Open navigation"
}, yr = ({ controlId: l }) => {
  const t = ne(), n = oe(wr);
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
z("TLButton", zt);
z("TLToggleButton", Vt);
z("TLTextInput", Dt);
z("TLPasswordInput", It);
z("TLNumberInput", jt);
z("TLDatePicker", Bt);
z("TLSelect", Ot);
z("TLCheckbox", Ft);
z("TLTable", Wt);
z("TLCounter", Kt);
z("TLTabBar", Gt);
z("TLFieldList", Xt);
z("TLAudioRecorder", Zt);
z("TLAudioPlayer", Jt);
z("TLFileUpload", tn);
z("TLBinaryField", ln);
z("TLDownload", on);
z("TLPhotoCapture", sn);
z("TLPhotoViewer", un);
z("TLPdfViewer", mn);
z("TLSplitPanel", pn);
z("TLPanel", gn);
z("TLInset", In);
z("TLMaximizeRoot", Cn);
z("TLDeckPane", wn);
z("TLSidebar", Dn);
z("TLStack", xn);
z("TLGrid", Mn);
z("TLCard", jn);
z("TLAppBar", Pn);
z("TLBreadcrumb", An);
z("TLBottomBar", $n);
z("TLDialog", Wn);
z("TLDialogManager", Un);
z("TLWindow", Yn);
z("TLDrawer", Zn);
z("TLContextMenuRegion", Jn);
z("TLSnackbar", ll);
z("TLMenu", ol);
z("TLAppShell", sl);
z("TLText", cl);
z("TLTableView", ul);
z("TLFormLayout", _l);
z("TLFormGroup", gl);
z("TLFormField", kl);
z("TLResourceCell", Sl);
z("TLTreeView", Tl);
z("TLDropdownSelect", jl);
z("TLColorInput", Xl);
z("TLIconSelect", lr);
z("TLDashboard", mr);
z("TLToolbar", _r);
z("TLTileStack", vr);
z("TLAdaptiveDetail", Er);
z("TLSlot", gr);
z("TLSlotContent", Cr);
z("TLDrawerToggle", yr);
