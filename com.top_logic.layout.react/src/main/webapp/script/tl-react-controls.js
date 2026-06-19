import { React as e, useTLFieldValue as Te, getComponent as Rt, useTLState as Z, useTLCommand as le, TLChild as Y, useTLUpload as We, useI18N as re, useTLDataUrl as xe, register as z } from "tl-react-bridge";
const { useCallback: Lt } = e, xt = ({ controlId: l, state: t }) => {
  const [n, o] = Te(), i = Lt(
    (a) => {
      o(a.target.value);
    },
    [o]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, n ?? "");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, r = [
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
      className: r,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: Dt } = e, It = ({ controlId: l, state: t }) => {
  const [n, o] = Te(), i = Dt(
    (a) => {
      o(a.target.value);
    },
    [o]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactTextInput tlReactTextInput--immutable" }, "••••••••");
  const s = t.hasError === !0, c = t.hasWarnings === !0, u = t.errorMessage, r = [
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
      className: r,
      "aria-invalid": s || void 0,
      title: s && u ? u : void 0
    }
  ));
}, { useCallback: Mt } = e, jt = ({ controlId: l, state: t, config: n }) => {
  const [o, i] = Te(), s = Mt(
    (m) => {
      const p = m.target.value;
      i(p === "" ? null : p);
    },
    [i]
  );
  if (t.editable === !1)
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactNumberInput tlReactNumberInput--immutable" }, o != null ? String(o) : "");
  const c = t.hasError === !0, u = t.hasWarnings === !0, r = t.errorMessage, a = [
    "tlReactNumberInput",
    c ? "tlReactNumberInput--error" : "",
    !c && u ? "tlReactNumberInput--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      inputMode: n != null && n.decimal ? "decimal" : "numeric",
      value: o != null ? String(o) : "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": c || void 0,
      title: c && r ? r : void 0
    }
  ));
}, { useCallback: Pt } = e, Bt = ({ controlId: l, state: t }) => {
  const [n, o] = Te(), i = Pt(
    (r) => {
      o(r.target.value || null);
    },
    [o]
  );
  if (t.editable === !1) {
    const r = t.displayValue ?? n ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactDatePicker tlReactDatePicker--immutable" }, r);
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
}, { useCallback: Ot } = e, At = ({ controlId: l, state: t, config: n }) => {
  var m;
  const [o, i] = Te(), s = Ot(
    (p) => {
      i(p.target.value || null);
    },
    [i]
  ), c = t.options ?? (n == null ? void 0 : n.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = c.find((h) => h.value === o)) == null ? void 0 : m.label) ?? "";
    return /* @__PURE__ */ e.createElement("span", { id: l, className: "tlReactSelect tlReactSelect--immutable" }, p);
  }
  const u = t.hasError === !0, r = t.hasWarnings === !0, a = [
    "tlReactSelect",
    u ? "tlReactSelect--error" : "",
    !u && r ? "tlReactSelect--warning" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("span", { id: l }, /* @__PURE__ */ e.createElement(
    "select",
    {
      value: o ?? "",
      onChange: s,
      disabled: t.disabled === !0,
      className: a,
      "aria-invalid": u || void 0
    },
    t.nullable !== !1 && /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((p) => /* @__PURE__ */ e.createElement("option", { key: p.value, value: p.value }, p.label))
  ));
}, { useCallback: $t } = e, Ft = ({ controlId: l, state: t }) => {
  const [n, o] = Te(), i = $t(
    (r) => {
      o(r.target.checked);
    },
    [o]
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
  const n = t.columns ?? [], o = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: l, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((i) => /* @__PURE__ */ e.createElement("th", { key: i.name }, i.label)))), /* @__PURE__ */ e.createElement("tbody", null, o.map((i, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((c) => {
    const u = c.cellModule ? Rt(c.cellModule) : void 0, r = i[c.name];
    if (u) {
      const a = { value: r, editable: t.editable };
      return /* @__PURE__ */ e.createElement("td", { key: c.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: l + "-" + s + "-" + c.name,
          state: a
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: c.name }, r != null ? String(r) : "");
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
const { useCallback: Ht } = e, zt = ({ controlId: l, command: t, label: n, image: o, disabled: i, displayMode: s }) => {
  const c = Z(), u = le(), r = t ?? "click", a = n ?? c.label, m = o ?? c.image, p = i ?? c.disabled === !0, h = s ?? c.displayMode ?? "label-only", v = c.hidden === !0, f = c.tooltip, S = v ? { display: "none" } : void 0, y = c.appearance, g = c.size, w = c.navigateUrl, D = Ht(() => {
    if (w) {
      window.location.assign(w);
      return;
    }
    u(r);
  }, [u, r, w]), L = h === "icon-only", _ = h === "icon-only" || h === "icon-label", b = h === "label-only" || h === "icon-label" || L && !m, E = f ?? (L ? a : void 0), H = E ? `text:${E}` : void 0;
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: l,
      onClick: D,
      disabled: p,
      style: S,
      className: "tlReactButton" + (L ? " tlReactButton--iconOnly" : "") + (y === "link" ? " tlReactButton--link" : "") + (g === "small" ? " tlReactButton--small" : "") + (g === "large" ? " tlReactButton--large" : ""),
      "data-tooltip": H,
      "aria-label": L ? a : void 0
    },
    _ && m && /* @__PURE__ */ e.createElement(ve, { encoded: m, className: "tlReactButton__image" }),
    b && /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, a)
  );
}, { useCallback: Ut } = e, Vt = ({ controlId: l, command: t, label: n, active: o, disabled: i }) => {
  const s = Z(), c = le(), u = t ?? "click", r = n ?? s.label, a = o ?? s.active === !0, m = i ?? s.disabled === !0, p = Ut(() => {
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
    r
  );
}, Kt = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.count ?? 0, i = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, i), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, o), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => n("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Yt } = e, Gt = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.tabs ?? [], i = t.activeTabId, s = Yt((c) => {
    c !== i && n("selectTab", { tabId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, o.map((c) => /* @__PURE__ */ e.createElement(
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
  const t = Z(), n = t.title, o = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFieldList" }, n && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, o.map((i, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: i })))));
}, qt = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Zt = ({ controlId: l }) => {
  const t = Z(), n = We(), [o, i] = e.useState("idle"), [s, c] = e.useState(null), u = e.useRef(null), r = e.useRef([]), a = e.useRef(null), m = t.status ?? "idle", p = t.error, h = m === "received" ? "idle" : o !== "idle" ? o : m, v = e.useCallback(async () => {
    if (o === "recording") {
      const w = u.current;
      w && w.state !== "inactive" && w.stop();
      return;
    }
    if (o !== "uploading") {
      if (c(null), !window.isSecureContext || !navigator.mediaDevices) {
        c("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const w = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        a.current = w, r.current = [];
        const D = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(w, D ? { mimeType: D } : void 0);
        u.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && r.current.push(_.data);
        }, L.onstop = async () => {
          w.getTracks().forEach((E) => E.stop()), a.current = null;
          const _ = new Blob(r.current, { type: L.mimeType || "audio/webm" });
          if (r.current = [], _.size === 0) {
            i("idle");
            return;
          }
          i("uploading");
          const b = new FormData();
          b.append("audio", _, "recording.webm"), await n(b), i("idle");
        }, L.start(), i("recording");
      } catch (w) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", w), c("js.audioRecorder.error.denied"), i("idle");
      }
    }
  }, [o, n]), f = re(qt), S = h === "recording" ? f["js.audioRecorder.stop"] : h === "uploading" ? f["js.uploading"] : f["js.audioRecorder.record"], y = h === "uploading", g = ["tlAudioRecorder__button"];
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
  const t = Z(), n = xe(), o = !!t.hasAudio, i = t.dataRevision ?? 0, [s, c] = e.useState(o ? "idle" : "disabled"), u = e.useRef(null), r = e.useRef(null), a = e.useRef(i);
  e.useEffect(() => {
    o ? s === "disabled" && c("idle") : (u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), c("disabled"));
  }, [o]), e.useEffect(() => {
    i !== a.current && (a.current = i, u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), (s === "playing" || s === "paused" || s === "loading") && c("idle"));
  }, [i]), e.useEffect(() => () => {
    u.current && (u.current.pause(), u.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
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
    if (!r.current) {
      c("loading");
      try {
        const y = await fetch(n);
        if (!y.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", y.status), c("idle");
          return;
        }
        const g = await y.blob();
        r.current = URL.createObjectURL(g);
      } catch (y) {
        console.error("[TLAudioPlayer] Fetch error:", y), c("idle");
        return;
      }
    }
    const S = new Audio(r.current);
    u.current = S, S.onended = () => {
      c("idle");
    }, S.play(), c("playing");
  }, [s, n]), p = re(Qt), h = s === "loading" ? p["js.loading"] : s === "playing" ? p["js.audioPlayer.pause"] : s === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], v = s === "disabled" || s === "loading", f = ["tlAudioPlayer__button"];
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
  const t = Z(), n = We(), [o, i] = e.useState("idle"), [s, c] = e.useState(!1), u = e.useRef(null), r = t.status ?? "idle", a = t.error, m = t.accept ?? "", p = r === "received" ? "idle" : o !== "idle" ? o : r, h = e.useCallback(async (_) => {
    i("uploading");
    const b = new FormData();
    b.append("file", _, _.name), await n(b), i("idle");
  }, [n]), v = e.useCallback((_) => {
    var E;
    const b = (E = _.target.files) == null ? void 0 : E[0];
    b && h(b);
  }, [h]), f = e.useCallback(() => {
    var _;
    o !== "uploading" && ((_ = u.current) == null || _.click());
  }, [o]), S = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!0);
  }, []), y = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), c(!1);
  }, []), g = e.useCallback((_) => {
    var E;
    if (_.preventDefault(), _.stopPropagation(), c(!1), o === "uploading") return;
    const b = (E = _.dataTransfer.files) == null ? void 0 : E[0];
    b && h(b);
  }, [o, h]), w = p === "uploading", D = re(en), L = p === "uploading" ? D["js.uploading"] : D["js.fileUpload.choose"];
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
        disabled: w,
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
  const o = Z() ?? t ?? {}, i = We(), s = xe(), c = re(nn), u = o.editable !== !1, r = !!o.hasData, a = o.fileName ?? "download", m = o.dataRevision ?? 0, p = o.accept ?? "", h = o.status ?? "idle", v = o.error ?? null, [f, S] = e.useState("idle"), [y, g] = e.useState(!1), [w, D] = e.useState(!1), L = e.useRef(null), _ = e.useCallback(async () => {
    if (!(!r || w)) {
      D(!0);
      try {
        const A = s + (s.includes("?") ? "&" : "?") + "rev=" + m, M = await fetch(A);
        if (!M.ok) {
          console.error("[TLBinaryField] Failed to fetch data:", M.status);
          return;
        }
        const I = await M.blob(), G = URL.createObjectURL(I), d = document.createElement("a");
        d.href = G, d.download = a, d.style.display = "none", document.body.appendChild(d), d.click(), document.body.removeChild(d), URL.revokeObjectURL(G);
      } catch (A) {
        console.error("[TLBinaryField] Fetch error:", A);
      } finally {
        D(!1);
      }
    }
  }, [r, w, s, m, a]), b = e.useCallback(async (A) => {
    S("uploading");
    const M = new FormData();
    M.append("file", A, A.name), await i(M), S("idle");
  }, [i]), E = (h === "received" ? "idle" : f !== "idle" ? f : h) === "uploading", H = e.useCallback((A) => {
    var I;
    const M = (I = A.target.files) == null ? void 0 : I[0];
    M && b(M);
  }, [b]), B = e.useCallback(() => {
    var A;
    E || (A = L.current) == null || A.click();
  }, [E]), R = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), g(!0);
  }, []), K = e.useCallback((A) => {
    A.preventDefault(), A.stopPropagation(), g(!1);
  }, []), O = e.useCallback((A) => {
    var I;
    if (A.preventDefault(), A.stopPropagation(), g(!1), E) return;
    const M = (I = A.dataTransfer.files) == null ? void 0 : I[0];
    M && b(M);
  }, [E, b]), N = w ? c["js.downloading"] : c["js.download.file"].replace("{0}", a), $ = /* @__PURE__ */ e.createElement("span", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (w ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: _,
      disabled: w,
      title: N,
      "aria-label": N
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a));
  if (!u)
    return r ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlBinaryField--view" }, $) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlBinaryField tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, c["js.download.noFile"]));
  const J = E, F = E ? c["js.uploading"] : c["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlBinaryField tlFileUpload${y ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: K,
      onDrop: O
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
    r && $,
    v && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, v)
  );
}, on = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, rn = ({ controlId: l }) => {
  const t = Z(), n = xe(), o = le(), i = !!t.hasData, s = t.dataRevision ?? 0, c = t.fileName ?? "download", u = !!t.clearable, [r, a] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!i || r)) {
      a(!0);
      try {
        const f = n + (n.includes("?") ? "&" : "?") + "rev=" + s, S = await fetch(f);
        if (!S.ok) {
          console.error("[TLDownload] Failed to fetch data:", S.status);
          return;
        }
        const y = await S.blob(), g = URL.createObjectURL(y), w = document.createElement("a");
        w.href = g, w.download = c, w.style.display = "none", document.body.appendChild(w), w.click(), document.body.removeChild(w), URL.revokeObjectURL(g);
      } catch (f) {
        console.error("[TLDownload] Fetch error:", f);
      } finally {
        a(!1);
      }
    }
  }, [i, r, n, s, c]), p = e.useCallback(async () => {
    i && await o("clear");
  }, [i, o]), h = re(on);
  if (!i)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, h["js.download.noFile"]));
  const v = r ? h["js.downloading"] : h["js.download.file"].replace("{0}", c);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (r ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: r,
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
  const t = Z(), n = We(), [o, i] = e.useState("idle"), [s, c] = e.useState(null), [u, r] = e.useState(!1), a = e.useRef(null), m = e.useRef(null), p = e.useRef(null), h = e.useRef(null), v = e.useRef(null), f = t.error, S = e.useMemo(
    () => {
      var R;
      return !!(window.isSecureContext && ((R = navigator.mediaDevices) != null && R.getUserMedia));
    },
    []
  ), y = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null), a.current && (a.current.srcObject = null);
  }, []), g = e.useCallback(() => {
    y(), i("idle");
  }, [y]), w = e.useCallback(async () => {
    var R;
    if (o !== "uploading") {
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
  }, [o, S]), D = e.useCallback(async () => {
    if (o !== "overlayOpen")
      return;
    const R = a.current, K = p.current;
    if (!R || !K)
      return;
    K.width = R.videoWidth, K.height = R.videoHeight;
    const O = K.getContext("2d");
    O && (O.drawImage(R, 0, 0), y(), i("uploading"), K.toBlob(async (N) => {
      if (!N) {
        i("idle");
        return;
      }
      const $ = new FormData();
      $.append("photo", N, "capture.jpg"), await n($), i("idle");
    }, "image/jpeg", 0.85));
  }, [o, n, y]), L = e.useCallback(async (R) => {
    var N;
    const K = (N = R.target.files) == null ? void 0 : N[0];
    if (!K) return;
    i("uploading");
    const O = new FormData();
    O.append("photo", K, K.name), await n(O), i("idle"), h.current && (h.current.value = "");
  }, [n]);
  e.useEffect(() => {
    o === "overlayOpen" && a.current && m.current && (a.current.srcObject = m.current);
  }, [o]), e.useEffect(() => {
    var K;
    if (o !== "overlayOpen") return;
    (K = v.current) == null || K.focus();
    const R = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = R;
    };
  }, [o]), e.useEffect(() => {
    if (o !== "overlayOpen") return;
    const R = (K) => {
      K.key === "Escape" && g();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [o, g]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((R) => R.stop()), m.current = null);
  }, []);
  const _ = re(an), b = o === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  o === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const H = ["tlPhotoCapture__overlayVideo"];
  u && H.push("tlPhotoCapture__overlayVideo--mirrored");
  const B = ["tlPhotoCapture__mirrorBtn"];
  return u && B.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: w,
      disabled: o === "uploading",
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
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), o === "overlayOpen" && /* @__PURE__ */ e.createElement(
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
        onClick: () => r((R) => !R),
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
  const t = Z(), n = xe(), o = !!t.hasPhoto, i = t.dataRevision ?? 0, [s, c] = e.useState(null), u = e.useRef(i);
  e.useEffect(() => {
    if (!o) {
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
  }, [o, i, n]), e.useEffect(() => () => {
    s && URL.revokeObjectURL(s);
  }, []);
  const r = re(cn);
  return !o || !s ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: s,
      alt: r["js.photoViewer.alt"]
    }
  ));
}, dn = {
  "js.pdfViewer.title": "PDF document",
  "js.pdfViewer.noDocument": "No document available"
}, mn = ({ controlId: l }) => {
  const t = Z(), n = xe(), o = !!t.hasPdf, i = t.dataRevision ?? 0, s = re(dn), u = n.indexOf("react-api/"), r = u >= 0 ? n.slice(0, u) : n, a = n + "&rev=" + i, m = r + "html/pdfjs/web/viewer.html?file=" + encodeURIComponent(a);
  return o ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement(
    "iframe",
    {
      className: "tlPdfViewer__frame",
      src: m,
      title: s["js.pdfViewer.title"]
    }
  )) : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlPdfViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPdfViewer__placeholder" }, s["js.pdfViewer.noDocument"]));
}, { useCallback: rt, useRef: He } = e, pn = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.orientation, i = t.resizable === !0, s = t.children ?? [], c = o === "horizontal", u = s.length > 0 && s.every((y) => y.collapsed), r = !u && s.some((y) => y.collapsed), a = u ? !c : c, m = He(null), p = He(null), h = He(null), v = rt((y, g) => {
    const w = {
      overflow: y.scrolling || "auto"
    };
    return y.collapsed ? u && !a ? w.flex = "1 0 0%" : w.flex = "0 0 auto" : g !== void 0 ? w.flex = `0 0 ${g}px` : w.flex = `${y.size} 1 0%`, y.minSize > 0 && !y.collapsed && (w.minWidth = c ? y.minSize : void 0, w.minHeight = c ? void 0 : y.minSize), w;
  }, [c, u, r, a]), f = rt((y, g) => {
    y.preventDefault();
    const w = m.current;
    if (!w) return;
    const D = s[g], L = s[g + 1], _ = w.querySelectorAll(":scope > .tlSplitPanel__child"), b = [];
    _.forEach((B) => {
      b.push(c ? B.offsetWidth : B.offsetHeight);
    }), h.current = b, p.current = {
      splitterIndex: g,
      startPos: c ? y.clientX : y.clientY,
      startSizeBefore: b[g],
      startSizeAfter: b[g + 1],
      childBefore: D,
      childAfter: L
    };
    const E = (B) => {
      const R = p.current;
      if (!R || !h.current) return;
      const O = (c ? B.clientX : B.clientY) - R.startPos, N = R.childBefore.minSize || 0, $ = R.childAfter.minSize || 0;
      let J = R.startSizeBefore + O, F = R.startSizeAfter - O;
      J < N && (F += J - N, J = N), F < $ && (J += F - $, F = $), h.current[R.splitterIndex] = J, h.current[R.splitterIndex + 1] = F;
      const A = w.querySelectorAll(":scope > .tlSplitPanel__child"), M = A[R.splitterIndex], I = A[R.splitterIndex + 1];
      M && (M.style.flex = `0 0 ${J}px`), I && (I.style.flex = `0 0 ${F}px`);
    }, H = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", H), document.body.style.cursor = "", document.body.style.userSelect = "", h.current) {
        const B = {};
        s.forEach((R, K) => {
          const O = R.control;
          O != null && O.controlId && h.current && (B[O.controlId] = h.current[K]);
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
      const w = s[g + 1];
      !y.collapsed && !w.collapsed && S.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${g}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${o}`,
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
      className: `tlSplitPanel tlSplitPanel--${o}${u ? " tlSplitPanel--allCollapsed" : ""}`,
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
  const t = Z(), n = le(), o = re(fn), i = t.title, s = t.expansionState ?? "NORMALIZED", c = t.showMinimize === !0, u = t.showMaximize === !0, r = t.showPopOut === !0, a = t.fullLine === !0, m = s === "MINIMIZED", p = s === "MAXIMIZED", h = s === "HIDDEN", v = ze(() => {
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
        title: m ? o["js.panel.restore"] : o["js.panel.minimize"]
      },
      m ? /* @__PURE__ */ e.createElement(bn, null) : /* @__PURE__ */ e.createElement(hn, null)
    ), u && !m && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: f,
        title: p ? o["js.panel.restore"] : o["js.panel.maximize"]
      },
      p ? /* @__PURE__ */ e.createElement(vn, null) : /* @__PURE__ */ e.createElement(_n, null)
    ), r && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: S,
        title: o["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(En, null)
    ))),
    !m && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child })),
    !m && t.buttonBar && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__buttonBar" }, /* @__PURE__ */ e.createElement(Y, { control: t.buttonBar }))
  );
}, wn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, Cn = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: Ae, useEffect: $e, useRef: Fe } = e, yn = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Qe(l, t, n, o) {
  const i = [];
  for (const s of l)
    if (s.type === "nav") {
      if (s.hidden) continue;
      i.push({ id: s.id, type: "nav", groupId: o });
    } else s.type === "command" ? i.push({ id: s.id, type: "command", groupId: o }) : s.type === "group" && (i.push({ id: s.id, type: "group" }), (n.get(s.id) ?? s.expanded) && !t && i.push(...Qe(s.children, t, n, s.id)));
  return i;
}
const Ne = ({ icon: l }) => l ? /* @__PURE__ */ e.createElement(ve, { encoded: l, className: "tlSidebar__icon" }) : null, kn = ({ item: l, active: t, collapsed: n, onSelect: o, tabIndex: i, itemRef: s, onFocus: c }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => o(l.id),
    title: n ? l.label : void 0,
    tabIndex: i,
    ref: s,
    onFocus: () => c(l.id)
  },
  n && l.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, l.badge)) : /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !n && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
  !n && l.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, l.badge)
), Sn = ({ item: l, collapsed: t, onExecute: n, tabIndex: o, itemRef: i, onFocus: s }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => n(l.id),
    title: t ? l.label : void 0,
    tabIndex: o,
    ref: i,
    onFocus: () => s(l.id)
  },
  /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)
), Nn = ({ item: l, collapsed: t }) => t && !l.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? l.label : void 0 }, /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label)), Tn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), Rn = ({ item: l, activeItemId: t, anchorRect: n, onSelect: o, onExecute: i, onClose: s }) => {
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
    a.type === "nav" ? (o(a.id), s()) : a.type === "command" && (i(a.id), s());
  }, [o, i, s]), r = {};
  return n && (r.left = n.right, r.top = n.top), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyout", ref: c, role: "menu", style: r }, /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__flyoutHeader" }, l.label), l.children.map((a) => {
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
  collapsed: o,
  onSelect: i,
  onExecute: s,
  onToggleGroup: c,
  tabIndex: u,
  itemRef: r,
  onFocus: a,
  focusedId: m,
  setItemRef: p,
  onItemFocus: h,
  flyoutGroupId: v,
  onOpenFlyout: f,
  onCloseFlyout: S
}) => {
  const y = Fe(null), [g, w] = Ae(null), D = ue(() => {
    o ? v === l.id ? S() : (y.current && w(y.current.getBoundingClientRect()), f(l.id)) : c(l.id);
  }, [o, v, l.id, c, f, S]), L = ue((b) => {
    y.current = b, r(b);
  }, [r]), _ = o && v === l.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: D,
      title: o ? l.label : void 0,
      "aria-expanded": o ? _ : t,
      tabIndex: u,
      ref: L,
      onFocus: () => a(l.id)
    },
    /* @__PURE__ */ e.createElement(Ne, { icon: l.icon }),
    !o && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, l.label),
    !o && /* @__PURE__ */ e.createElement(
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
  ), t && !o && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, l.children.map((b) => /* @__PURE__ */ e.createElement(
    gt,
    {
      key: b.id,
      item: b,
      activeItemId: n,
      collapsed: o,
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
  onSelect: o,
  onExecute: i,
  onToggleGroup: s,
  focusedId: c,
  setItemRef: u,
  onItemFocus: r,
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
          onSelect: o,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r
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
          onFocus: r
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
          onSelect: o,
          onExecute: i,
          onToggleGroup: s,
          tabIndex: c === l.id ? 0 : -1,
          itemRef: u(l.id),
          onFocus: r,
          focusedId: c,
          setItemRef: u,
          onItemFocus: r,
          flyoutGroupId: m,
          onOpenFlyout: p,
          onCloseFlyout: h
        }
      );
    }
    default:
      return null;
  }
}, xn = ({ controlId: l }) => {
  const t = Z(), n = le(), o = re(yn), i = t.items ?? [], s = t.activeItemId, c = t.collapsed, u = t.drawerOpen, r = u ? !1 : c, [a, m] = Ae(() => {
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
  }, [n]), [y, g] = Ae(null), w = ue((N) => {
    g(N);
  }, []), D = ue(() => {
    g(null);
  }, []);
  $e(() => {
    r || g(null);
  }, [r]);
  const [L, _] = Ae(() => {
    const N = Qe(i, r, a);
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
      N.preventDefault(), D();
      return;
    }
    const $ = Qe(i, r, a);
    if ($.length === 0) return;
    const J = $.findIndex((A) => A.id === L);
    if (J < 0) return;
    const F = $[J];
    switch (N.key) {
      case "ArrowDown": {
        N.preventDefault();
        const A = (J + 1) % $.length;
        R($[A].id);
        break;
      }
      case "ArrowUp": {
        N.preventDefault();
        const A = (J - 1 + $.length) % $.length;
        R($[A].id);
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
        N.preventDefault(), F.type === "nav" ? h(F.id) : F.type === "command" ? v(F.id) : F.type === "group" && (r ? y === F.id ? D() : w(F.id) : p(F.id));
        break;
      }
      case "ArrowRight": {
        F.type === "group" && !r && ((a.get(F.id) ?? !1) || (N.preventDefault(), p(F.id)));
        break;
      }
      case "ArrowLeft": {
        F.type === "group" && !r && (a.get(F.id) ?? !1) && (N.preventDefault(), p(F.id));
        break;
      }
    }
  }, [
    i,
    r,
    a,
    L,
    y,
    R,
    h,
    v,
    p,
    w,
    D
  ]), O = "tlSidebar" + (r ? " tlSidebar--collapsed" : "") + (u ? " tlSidebar--drawerOpen" : "");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: O }, t.drawerToggleContribution && /* @__PURE__ */ e.createElement(Y, { control: t.drawerToggleContribution }), u && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__backdrop", onClick: S, "aria-hidden": "true" }), /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": o["js.sidebar.ariaLabel"] }, r ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: K }, i.map((N) => /* @__PURE__ */ e.createElement(
    gt,
    {
      key: N.id,
      item: N,
      activeItemId: s,
      collapsed: r,
      onSelect: h,
      onExecute: v,
      onToggleGroup: p,
      focusedId: L,
      setItemRef: E,
      onItemFocus: H,
      groupStates: a,
      flyoutGroupId: y,
      onOpenFlyout: w,
      onCloseFlyout: D
    }
  ))), r ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
      title: r ? o["js.sidebar.expand"] : o["js.sidebar.collapse"]
    },
    /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement(
      "path",
      {
        d: r ? "M6 4l4 4-4 4" : "M10 4l-4 4 4 4",
        fill: "none",
        stroke: "currentColor",
        strokeWidth: "2",
        strokeLinecap: "round",
        strokeLinejoin: "round"
      }
    ))
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, Dn = ({ controlId: l }) => {
  const t = Z(), n = t.direction ?? "column", o = t.gap ?? "default", i = t.align ?? "stretch", s = t.wrap === !0, c = t.growFirst === !0, u = t.children ?? [], r = [
    "tlStack",
    `tlStack--${n}`,
    `tlStack--gap-${o}`,
    `tlStack--align-${i}`,
    s ? "tlStack--wrap" : "",
    c ? "tlStack--grow-first" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: r }, u.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a })));
}, In = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlInset" }, t.child && /* @__PURE__ */ e.createElement(Y, { control: t.child }));
}, Mn = ({ controlId: l }) => {
  const t = Z(), n = t.columns, o = t.minColumnWidth, i = t.gap ?? "default", s = t.children ?? [], c = {};
  return o ? c.gridTemplateColumns = `repeat(auto-fit, minmax(${o}, 1fr))` : n && (c.gridTemplateColumns = `repeat(${n}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: l, className: `tlGrid tlGrid--gap-${i}`, style: c }, s.map((u, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: u })));
}, jn = ({ controlId: l }) => {
  const t = Z(), n = t.title, o = t.variant ?? "outlined", i = t.padding ?? "default", s = t.headerActions ?? [], c = t.child, u = n != null || s.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: `tlCard tlCard--${o}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, n && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, n), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, s.map((r, a) => /* @__PURE__ */ e.createElement(Y, { key: a, control: r })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${i}` }, /* @__PURE__ */ e.createElement(Y, { control: c })));
}, Pn = ({ controlId: l }) => {
  const t = Z(), n = t.title ?? "", o = t.leading, i = t.children ?? [], s = t.actions ?? [], c = t.variant ?? "flat", r = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    c === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: l, className: r }, o && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: o })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, n), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__children" }, i.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a }))), s.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, s.map((a, m) => /* @__PURE__ */ e.createElement(Y, { key: m, control: a }))));
}, { useCallback: Bn } = e, On = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.items ?? [], i = Bn((s) => {
    n("navigate", { itemId: s });
  }, [n]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBreadcrumb", "aria-label": "Breadcrumb" }, /* @__PURE__ */ e.createElement("ol", { className: "tlBreadcrumb__list" }, o.map((s, c) => {
    const u = c === o.length - 1;
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
}, { useCallback: An } = e, $n = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.items ?? [], i = t.activeItemId, s = An((c) => {
    c !== i && n("selectItem", { itemId: c });
  }, [n, i]);
  return /* @__PURE__ */ e.createElement("nav", { id: l, className: "tlBottomBar", "aria-label": "Bottom navigation" }, o.map((c) => {
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
  const t = Z(), n = le(), o = t.open === !0, i = t.closeOnBackdrop !== !1, s = t.child, c = Fn(null), u = at(() => {
    n("close");
  }, [n]), r = at((a) => {
    i && a.target === a.currentTarget && u();
  }, [i, u]);
  return st(() => {
    if (!o) return;
    const a = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", a), () => document.removeEventListener("keydown", a);
  }, [o, u]), st(() => {
    o && c.current && c.current.focus();
  }, [o]), o ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlDialog__backdrop",
      onClick: r,
      ref: c,
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement(Y, { control: s })
  ) : null;
}, { useEffect: Hn, useRef: zn } = e, Un = ({ controlId: l }) => {
  const n = Z().dialogs ?? [], o = zn(n.length);
  return Hn(() => {
    n.length < o.current && n.length > 0, o.current = n.length;
  }, [n.length]), n.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDialogManager" }, n.map((i) => /* @__PURE__ */ e.createElement(Y, { key: i.controlId, control: i })));
}, { useCallback: Ie, useRef: Ce, useState: Me } = e, Vn = {
  "js.window.close": "Close",
  "js.window.maximize": "Maximize",
  "js.window.restore": "Restore"
}, Kn = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], Yn = ({ controlId: l }) => {
  const t = Z(), n = le(), o = re(Vn), i = t.title ?? "", s = t.width ?? "32rem", c = t.height ?? null, u = t.minHeight ?? null, r = t.resizable === !0, a = t.child, m = t.actions ?? [], p = t.toolbar, h = t.buttonBar, [v, f] = Me(null), [S, y] = Me(null), [g, w] = Me(null), D = Ce(null), [L, _] = Me(!1), b = Ce(null), E = Ce(null), H = Ce(null), B = Ce(null), R = Ce(null), K = Ie(() => {
    n("close");
  }, [n]), O = Ie((A, M) => {
    M.preventDefault();
    const I = B.current;
    if (!I) return;
    const G = I.getBoundingClientRect(), d = !D.current, k = D.current ?? { x: G.left, y: G.top };
    d && (D.current = k, w(k)), R.current = {
      dir: A,
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
      const Q = X.clientX - P.startX, ne = X.clientY - P.startY;
      let te = P.startW, ie = P.startH, de = 0, me = 0;
      P.symmetric ? (P.dir.includes("e") && (te = P.startW + 2 * Q), P.dir.includes("w") && (te = P.startW - 2 * Q), P.dir.includes("s") && (ie = P.startH + 2 * ne), P.dir.includes("n") && (ie = P.startH - 2 * ne)) : (P.dir.includes("e") && (te = P.startW + Q), P.dir.includes("w") && (te = P.startW - Q, de = Q), P.dir.includes("s") && (ie = P.startH + ne), P.dir.includes("n") && (ie = P.startH - ne, me = ne));
      const he = Math.max(200, te), be = Math.max(100, ie);
      P.symmetric ? (de = (P.startW - he) / 2, me = (P.startH - be) / 2) : (P.dir.includes("w") && he === 200 && (de = P.startW - 200), P.dir.includes("n") && be === 100 && (me = P.startH - 100)), E.current = he, H.current = be, f(he), y(be);
      const T = {
        x: P.startPos.x + de,
        y: P.startPos.y + me
      };
      D.current = T, w(T);
    }, W = () => {
      document.removeEventListener("mousemove", U), document.removeEventListener("mouseup", W);
      const X = E.current, P = H.current;
      (X != null || P != null) && n("resize", {
        ...X != null ? { width: Math.round(X) + "px" } : {},
        ...P != null ? { height: Math.round(P) + "px" } : {}
      }), R.current = null;
    };
    document.addEventListener("mousemove", U), document.addEventListener("mouseup", W);
  }, [n]), N = Ie((A) => {
    if (A.button !== 0 || A.target.closest("button")) return;
    A.preventDefault();
    const M = B.current;
    if (!M) return;
    const I = M.getBoundingClientRect(), G = D.current ?? { x: I.left, y: I.top }, d = A.clientX - G.x, k = A.clientY - G.y, U = (X) => {
      const P = window.innerWidth, Q = window.innerHeight;
      let ne = X.clientX - d, te = X.clientY - k;
      const ie = M.offsetWidth, de = M.offsetHeight;
      ne + ie > P && (ne = P - ie), te + de > Q && (te = Q - de), ne < 0 && (ne = 0), te < 0 && (te = 0);
      const me = { x: ne, y: te };
      D.current = me, w(me);
    }, W = () => {
      document.removeEventListener("mousemove", U), document.removeEventListener("mouseup", W);
    };
    document.addEventListener("mousemove", U), document.addEventListener("mouseup", W);
  }, []), $ = Ie(() => {
    var A, M;
    if (L) {
      const I = b.current;
      I && (w(I.x !== -1 ? { x: I.x, y: I.y } : null), f(I.w), y(I.h)), _(!1);
    } else {
      const I = B.current, G = I == null ? void 0 : I.getBoundingClientRect();
      b.current = {
        x: ((A = D.current) == null ? void 0 : A.x) ?? (G == null ? void 0 : G.left) ?? -1,
        y: ((M = D.current) == null ? void 0 : M.y) ?? (G == null ? void 0 : G.top) ?? -1,
        w: v ?? (G == null ? void 0 : G.width) ?? null,
        h: S ?? null
      }, _(!0), w({ x: 0, y: 0 }), f(null), y(null);
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
        onDoubleClick: r ? $ : void 0
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: F }, i),
      p && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, /* @__PURE__ */ e.createElement(Y, { control: p })),
      r && /* @__PURE__ */ e.createElement(
        "button",
        {
          type: "button",
          className: "tlWindow__maximizeBtn",
          onClick: $,
          title: L ? o["js.window.restore"] : o["js.window.maximize"]
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
          title: o["js.window.close"]
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
    (m.length > 0 || h) && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, h && /* @__PURE__ */ e.createElement(Y, { control: h }), m.map((A, M) => /* @__PURE__ */ e.createElement(Y, { key: M, control: A }))),
    r && !L && Kn.map((A) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: A,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${A}`,
        onMouseDown: (M) => O(A, M)
      }
    ))
  );
}, { useCallback: Gn, useEffect: Xn } = e, qn = {
  "js.drawer.close": "Close"
}, Zn = ({ controlId: l }) => {
  const t = Z(), n = le(), o = re(qn), i = t.open === !0, s = t.position ?? "right", c = t.size ?? "medium", u = t.title ?? null, r = t.child, a = Gn(() => {
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
      title: o["js.drawer.close"]
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, r && /* @__PURE__ */ e.createElement(Y, { control: r })));
}, { useCallback: Qn } = e, Jn = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.child, i = Qn((s) => {
    s.preventDefault(), s.stopPropagation(), n("openContextMenu", { x: s.clientX, y: s.clientY });
  }, [n]);
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tl-context-menu-region", onContextMenu: i }, o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, { useCallback: el, useEffect: tl, useState: nl } = e, ll = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.message ?? "", i = t.content ?? "", s = t.variant ?? "info", c = t.duration ?? 5e3, u = t.visible === !0, r = t.generation ?? 0, [a, m] = nl(!1), p = el(() => {
    m(!0), setTimeout(() => {
      n("dismiss", { generation: r }), m(!1);
    }, 200);
  }, [n, r]);
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
    i ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: i } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, o)
  );
}, { useCallback: Ue, useEffect: Ve, useRef: ol, useState: ct } = e, rl = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.open === !0, i = t.anchorId, s = t.anchorX, c = t.anchorY, u = t.items ?? [], r = ol(null), [a, m] = ct({ top: 0, left: 0 }), [p, h] = ct(0), v = u.filter((g) => g.type === "item" && !g.disabled);
  Ve(() => {
    var E, H;
    if (!o) return;
    const g = ((E = r.current) == null ? void 0 : E.offsetHeight) ?? 200, w = ((H = r.current) == null ? void 0 : H.offsetWidth) ?? 200;
    if (s != null && c != null) {
      let B = c, R = s;
      B + g > window.innerHeight && (B = Math.max(0, window.innerHeight - g)), R + w > window.innerWidth && (R = Math.max(0, window.innerWidth - w)), m({ top: B, left: R }), h(0);
      return;
    }
    if (!i) return;
    const D = document.getElementById(i);
    if (!D) return;
    const L = D.getBoundingClientRect();
    let _ = L.bottom + 4, b = L.left;
    _ + g > window.innerHeight && (_ = L.top - g - 4), b + w > window.innerWidth && (b = L.right - w), m({ top: _, left: b }), h(0);
  }, [o, i, s, c]);
  const f = Ue(() => {
    n("close");
  }, [n]), S = Ue((g) => {
    n("selectItem", { itemId: g });
  }, [n]);
  Ve(() => {
    if (!o) return;
    const g = (w) => {
      r.current && !r.current.contains(w.target) && f();
    };
    return document.addEventListener("mousedown", g), () => document.removeEventListener("mousedown", g);
  }, [o, f]);
  const y = Ue((g) => {
    if (g.key === "Escape") {
      f();
      return;
    }
    if (g.key === "ArrowDown")
      g.preventDefault(), h((w) => (w + 1) % v.length);
    else if (g.key === "ArrowUp")
      g.preventDefault(), h((w) => (w - 1 + v.length) % v.length);
    else if (g.key === "Enter" || g.key === " ") {
      g.preventDefault();
      const w = v[p];
      w && S(w.id);
    }
  }, [f, S, v, p]);
  return Ve(() => {
    o && r.current && r.current.focus();
  }, [o]), o ? /* @__PURE__ */ e.createElement(
    "div",
    {
      id: l,
      className: "tlMenu",
      role: "menu",
      ref: r,
      tabIndex: -1,
      style: { position: "fixed", top: a.top, left: a.left },
      onKeyDown: y
    },
    u.map((g, w) => {
      if (g.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: w, className: "tlMenu__separator" });
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
}, al = ({ controlId: l }) => {
  const t = Z(), n = t.header, o = t.content, i = t.footer, s = t.snackbar, c = t.dialogManager, u = t.menuOverlay;
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlAppShell" }, n && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: n })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: o })), i && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: i })), /* @__PURE__ */ e.createElement(Y, { control: s }), c && /* @__PURE__ */ e.createElement(Y, { control: c }), u && /* @__PURE__ */ e.createElement(Y, { control: u }));
}, sl = ({ controlId: l }) => {
  const t = Z(), n = t.text ?? "", o = t.cssClass ?? "", i = t.hasTooltip === !0, s = o ? `tlText ${o}` : "tlText";
  return /* @__PURE__ */ e.createElement(
    "span",
    {
      id: l,
      className: s,
      "data-tooltip": i ? "key:tooltip" : void 0
    },
    n
  );
}, cl = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, it = 50, il = ({ controlId: l }) => {
  const t = Z(), n = le(), o = re(cl), i = e.useRef(null);
  e.useEffect(() => {
    const T = i.current;
    if (!T) return;
    const j = (C) => {
      const x = C.detail;
      let V = x.target;
      for (; V && V !== T; ) {
        const ee = V.dataset.row, oe = V.dataset.col;
        if (ee != null && oe != null) {
          x.resolved = { key: ee + "|" + oe };
          return;
        }
        V = V.parentElement;
      }
    };
    return T.addEventListener("tl-tooltip-resolve", j), () => T.removeEventListener("tl-tooltip-resolve", j);
  }, []);
  const s = t.columns ?? [], c = t.totalRowCount ?? 0, u = t.rows ?? [], r = t.rowHeight ?? 36, a = t.selectionMode ?? "single", m = t.selectedCount ?? 0, p = t.frozenColumnCount ?? 0, h = t.treeMode ?? !1, v = e.useMemo(
    () => s.filter((T) => T.sortPriority && T.sortPriority > 0).length,
    [s]
  ), f = a === "multi", S = 40, y = 20, g = e.useRef(null), w = e.useRef(null), D = e.useRef(null), [L, _] = e.useState({}), b = e.useRef(null), E = e.useRef(!1), H = e.useRef(null), [B, R] = e.useState(null), [K, O] = e.useState(null);
  e.useEffect(() => {
    b.current || _({});
  }, [s]);
  const N = e.useCallback((T) => L[T.name] ?? T.width, [L]), $ = e.useMemo(() => {
    const T = [];
    let j = f && p > 0 ? S : 0;
    for (let C = 0; C < p && C < s.length; C++)
      T.push(j), j += N(s[C]);
    return T;
  }, [s, p, f, S, N]), J = c * r, F = e.useRef(null), A = e.useCallback((T, j, C) => {
    C.preventDefault(), C.stopPropagation(), b.current = { column: T, startX: C.clientX, startWidth: j };
    let x = C.clientX, V = 0;
    const ee = () => {
      const ae = b.current;
      if (!ae) return;
      const pe = Math.max(it, ae.startWidth + (x - ae.startX) + V);
      _((we) => ({ ...we, [ae.column]: pe }));
    }, oe = () => {
      const ae = w.current, pe = g.current;
      if (!ae || !b.current) return;
      const we = ae.getBoundingClientRect(), nt = 40, lt = 8, Tt = ae.scrollLeft;
      x > we.right - nt ? ae.scrollLeft += lt : x < we.left + nt && (ae.scrollLeft = Math.max(0, ae.scrollLeft - lt));
      const ot = ae.scrollLeft - Tt;
      ot !== 0 && (pe && (pe.scrollLeft = ae.scrollLeft), V += ot, ee()), F.current = requestAnimationFrame(oe);
    };
    F.current = requestAnimationFrame(oe);
    const ge = (ae) => {
      x = ae.clientX, ee();
    }, De = (ae) => {
      document.removeEventListener("mousemove", ge), document.removeEventListener("mouseup", De), F.current !== null && (cancelAnimationFrame(F.current), F.current = null);
      const pe = b.current;
      if (pe) {
        const we = Math.max(it, pe.startWidth + (ae.clientX - pe.startX) + V);
        n("columnResize", { column: pe.column, width: we }), b.current = null, E.current = !0, requestAnimationFrame(() => {
          E.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ge), document.addEventListener("mouseup", De);
  }, [n]), M = e.useCallback(() => {
    g.current && w.current && (g.current.scrollLeft = w.current.scrollLeft), D.current !== null && clearTimeout(D.current), D.current = window.setTimeout(() => {
      const T = w.current;
      if (!T) return;
      const j = T.scrollTop, C = Math.ceil(T.clientHeight / r), x = Math.floor(j / r);
      n("scroll", { start: x, count: C });
    }, 80);
  }, [n, r]), I = e.useCallback((T, j, C) => {
    if (E.current) return;
    let x;
    !j || j === "desc" ? x = "asc" : x = "desc";
    const V = C.shiftKey ? "add" : "replace";
    n("sort", { column: T, direction: x, mode: V });
  }, [n]), G = e.useCallback((T, j) => {
    H.current = T, j.dataTransfer.effectAllowed = "move", j.dataTransfer.setData("text/plain", T);
  }, []), d = e.useCallback((T, j) => {
    if (!H.current || H.current === T) {
      R(null);
      return;
    }
    j.preventDefault(), j.dataTransfer.dropEffect = "move";
    const C = j.currentTarget.getBoundingClientRect(), x = j.clientX < C.left + C.width / 2 ? "left" : "right";
    R({ column: T, side: x });
  }, []), k = e.useCallback((T) => {
    T.preventDefault(), T.stopPropagation();
    const j = H.current;
    if (!j || !B) {
      H.current = null, R(null);
      return;
    }
    let C = s.findIndex((V) => V.name === B.column);
    if (C < 0) {
      H.current = null, R(null);
      return;
    }
    const x = s.findIndex((V) => V.name === j);
    B.side === "right" && C++, x < C && C--, n("columnReorder", { column: j, targetIndex: C }), H.current = null, R(null);
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
  }, [n, m, c]), Q = e.useCallback((T, j, C) => {
    C.stopPropagation(), n("expand", { rowIndex: T, expanded: j });
  }, [n]), ne = e.useCallback((T, j) => {
    j.preventDefault(), O({ x: j.clientX, y: j.clientY, colIdx: T });
  }, []), te = e.useCallback(() => {
    K && (n("setFrozenColumnCount", { count: K.colIdx + 1 }), O(null));
  }, [K, n]), ie = e.useCallback(() => {
    n("setFrozenColumnCount", { count: 0 }), O(null);
  }, [n]);
  e.useEffect(() => {
    if (!K) return;
    const T = () => O(null), j = (C) => {
      C.key === "Escape" && O(null);
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
        const j = w.current, C = g.current;
        if (!j) return;
        const x = j.getBoundingClientRect(), V = 40, ee = 8;
        T.clientX < x.left + V ? j.scrollLeft = Math.max(0, j.scrollLeft - ee) : T.clientX > x.right - V && (j.scrollLeft += ee), C && (C.scrollLeft = j.scrollLeft);
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
      const C = N(T);
      s.length - 1;
      let x = "tlTableView__headerCell";
      T.sortable && (x += " tlTableView__headerCell--sortable"), B && B.column === T.name && (x += " tlTableView__headerCell--dragOver-" + B.side);
      const V = j < p, ee = j === p - 1;
      return V && (x += " tlTableView__headerCell--frozen"), ee && (x += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: T.name,
          className: x,
          style: {
            width: C,
            minWidth: C,
            position: V ? "sticky" : "relative",
            ...V ? { left: $[j], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: T.sortable ? (oe) => I(T.name, T.sortDirection, oe) : void 0,
          onContextMenu: (oe) => ne(j, oe),
          onDragStart: (oe) => G(T.name, oe),
          onDragOver: (oe) => d(T.name, oe),
          onDrop: k,
          onDragEnd: U
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, T.label),
        T.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, T.sortDirection === "asc" ? "▲" : "▼", v > 1 && T.sortPriority != null && T.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, T.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (oe) => A(T.name, C, oe)
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
        ref: w,
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
            top: T.index * r,
            height: r,
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
        s.map((j, C) => {
          const x = N(j), V = C === s.length - 1, ee = C < p, oe = C === p - 1;
          let ge = "tlTableView__cell";
          ee && (ge += " tlTableView__cell--frozen"), oe && (ge += " tlTableView__cell--frozenLast");
          const De = h && C === 0, ae = T.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: j.name,
              className: ge,
              "data-row": T.id,
              "data-col": j.name,
              style: {
                ...V && !ee ? { flex: "1 0 auto", minWidth: x } : { width: x, minWidth: x },
                ...ee ? { position: "sticky", left: $[C], zIndex: 2 } : {}
              }
            },
            De ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * y } }, T.expandable ? /* @__PURE__ */ e.createElement(
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
      K.colIdx + 1 !== p && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: te }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.freezeUpTo"])),
      p > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, o["js.table.unfreezeAll"]))
    )
  );
}, ul = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, wt = e.createContext(ul), { useMemo: dl, useRef: ml, useState: pl, useEffect: fl } = e, hl = 320, bl = ({ controlId: l }) => {
  const t = Z(), n = t.maxColumns ?? 3, o = t.labelPosition ?? "auto", i = t.readOnly === !0, s = t.children ?? [], c = t.noModelMessage, u = ml(null), [r, a] = pl(
    o === "top" ? "top" : "side"
  );
  fl(() => {
    if (o !== "auto") {
      a(o);
      return;
    }
    const f = u.current;
    if (!f) return;
    const S = new ResizeObserver((y) => {
      for (const g of y) {
        const D = g.contentRect.width / n;
        a(D < hl ? "top" : "side");
      }
    });
    return S.observe(f), () => S.disconnect();
  }, [o, n]);
  const m = dl(() => ({
    readOnly: i,
    resolvedLabelPosition: r
  }), [i, r]), h = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / n))}rem`}, 1fr))`
  }, v = [
    "tlFormLayout",
    i ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return c ? /* @__PURE__ */ e.createElement("div", { id: l, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, c)) : /* @__PURE__ */ e.createElement(wt.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: l, className: v, style: h, ref: u }, s.map((f, S) => /* @__PURE__ */ e.createElement(Y, { key: S, control: f }))));
}, { useCallback: _l } = e, vl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, El = ({ controlId: l }) => {
  const t = Z(), n = le(), o = re(vl), i = t.headerControl ?? null, s = t.headerActions ?? [], c = t.collapsible === !0, u = t.collapsed === !0, r = t.border ?? "none", a = t.fullLine === !0, m = t.children ?? [], p = i != null || s.length > 0 || c, h = _l(() => {
    n("toggleCollapse");
  }, [n]), v = [
    "tlFormGroup",
    `tlFormGroup--border-${r}`,
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
      title: u ? o["js.formGroup.expand"] : o["js.formGroup.collapse"]
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
}, { useContext: gl, useState: wl, useCallback: Cl } = e, yl = ({ controlId: l }) => {
  const t = Z(), n = gl(wt), o = t.label ?? "", i = t.required === !0, s = t.error, c = t.warnings, u = t.helpText, r = t.dirty === !0, a = t.labelPosition ?? n.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, h = t.hasTooltip === !0, v = t.field, f = n.readOnly, [S, y] = wl(!1), g = Cl(() => y((_) => !_), []);
  if (!p) return null;
  const w = s != null, D = c != null && c.length > 0, L = [
    "tlFormField",
    `tlFormField--${a}`,
    f ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    w ? "tlFormField--error" : "",
    !w && D ? "tlFormField--warning" : "",
    r ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: l, className: L }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement(
    "span",
    {
      className: "tlFormField__labelText",
      "data-tooltip": h ? "key:tooltip" : void 0
    },
    o
  ), i && !f && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), r && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !f && /* @__PURE__ */ e.createElement(
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: v })), !f && w && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, s)), !f && !w && D && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, c.map((_, b) => /* @__PURE__ */ e.createElement("div", { key: b, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
}, kl = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.iconCss, i = t.iconSrc, s = t.label, c = t.cssClass, u = t.hasTooltip === !0, r = t.hasLink, a = o ? /* @__PURE__ */ e.createElement("i", { className: o }) : i ? /* @__PURE__ */ e.createElement("img", { src: i, className: "tlTypeIcon", alt: "" }) : null, m = /* @__PURE__ */ e.createElement(e.Fragment, null, a, s && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, s)), p = e.useCallback((f) => {
    f.preventDefault(), n("goto", {});
  }, [n]), h = ["tlResourceCell", c].filter(Boolean).join(" "), v = u ? "key:tooltip" : void 0;
  return r ? /* @__PURE__ */ e.createElement(
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
}, Sl = 20, Nl = () => {
  const l = Z(), t = le(), n = l.nodes ?? [], o = l.selectionMode ?? "single", i = l.dragEnabled ?? !1, s = l.dropEnabled ?? !1, c = l.dropIndicatorNodeId ?? null, u = l.dropIndicatorPosition ?? null, [r, a] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, b) => {
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
  }, [t, S]), w = e.useCallback((_, b) => {
    b.preventDefault(), f.current != null && (window.clearTimeout(f.current), f.current = null);
    const E = S(b, b.currentTarget);
    t("drop", { nodeId: _, position: E });
  }, [t, S]), D = e.useCallback(() => {
    f.current != null && (window.clearTimeout(f.current), f.current = null), t("dragEnd");
  }, [t]), L = e.useCallback((_) => {
    if (n.length === 0) return;
    let b = r;
    switch (_.key) {
      case "ArrowDown":
        _.preventDefault(), b = Math.min(r + 1, n.length - 1);
        break;
      case "ArrowUp":
        _.preventDefault(), b = Math.max(r - 1, 0);
        break;
      case "ArrowRight":
        if (_.preventDefault(), r >= 0 && r < n.length) {
          const E = n[r];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (b = r + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), r >= 0 && r < n.length) {
          const E = n[r];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const H = E.depth;
            for (let B = r - 1; B >= 0; B--)
              if (n[B].depth < H) {
                b = B;
                break;
              }
          }
        }
        break;
      case "Enter":
        _.preventDefault(), r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
          ctrlKey: _.ctrlKey || _.metaKey,
          shiftKey: _.shiftKey
        });
        return;
      case " ":
        _.preventDefault(), o === "multi" && r >= 0 && r < n.length && t("select", {
          nodeId: n[r].id,
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
    b !== r && a(b);
  }, [r, n, t, o]);
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
          b === r ? "tlTreeView__node--focused" : "",
          c === _.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          c === _.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          c === _.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: _.depth * Sl },
        draggable: i,
        onClick: (E) => h(_.id, E),
        onContextMenu: (E) => v(_.id, E),
        onDragStart: (E) => y(_.id, E),
        onDragOver: s ? (E) => g(_.id, E) : void 0,
        onDrop: s ? (E) => w(_.id, E) : void 0,
        onDragEnd: D
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
var Ke = { exports: {} }, se = {}, Ye = { exports: {} }, q = {};
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
function Tl() {
  if (ut) return q;
  ut = 1;
  var l = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), n = Symbol.for("react.fragment"), o = Symbol.for("react.strict_mode"), i = Symbol.for("react.profiler"), s = Symbol.for("react.consumer"), c = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), r = Symbol.for("react.suspense"), a = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), h = Symbol.iterator;
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
  function w() {
  }
  w.prototype = g.prototype;
  function D(d, k, U) {
    this.props = d, this.context = k, this.refs = y, this.updater = U || f;
  }
  var L = D.prototype = new w();
  L.constructor = D, S(L, g.prototype), L.isPureReactComponent = !0;
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
  function O(d) {
    var k = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(U) {
      return k[U];
    });
  }
  var N = /\/+/g;
  function $(d, k) {
    return typeof d == "object" && d !== null && d.key != null ? O("" + d.key) : k.toString(36);
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
    var ne = W === "" ? "." : W + ":";
    if (_(d))
      for (var te = 0; te < d.length; te++)
        W = d[te], P = ne + $(W, te), Q += F(
          W,
          k,
          U,
          P,
          X
        );
    else if (te = v(d), typeof te == "function")
      for (d = te.call(d), te = 0; !(W = d.next()).done; )
        W = W.value, P = ne + $(W, te++), Q += F(
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
  function A(d, k, U) {
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
    map: A,
    forEach: function(d, k, U) {
      A(
        d,
        function() {
          k.apply(this, arguments);
        },
        U
      );
    },
    count: function(d) {
      var k = 0;
      return A(d, function() {
        k++;
      }), k;
    },
    toArray: function(d) {
      return A(d, function(k) {
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
  return q.Activity = p, q.Children = G, q.Component = g, q.Fragment = n, q.Profiler = i, q.PureComponent = D, q.StrictMode = o, q.Suspense = r, q.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, q.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, q.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, q.cacheSignal = function() {
    return null;
  }, q.cloneElement = function(d, k, U) {
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
      for (var Q = Array(P), ne = 0; ne < P; ne++)
        Q[ne] = arguments[ne + 2];
      W.children = Q;
    }
    return B(d.type, X, W);
  }, q.createContext = function(d) {
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
  }, q.createElement = function(d, k, U) {
    var W, X = {}, P = null;
    if (k != null)
      for (W in k.key !== void 0 && (P = "" + k.key), k)
        H.call(k, W) && W !== "key" && W !== "__self" && W !== "__source" && (X[W] = k[W]);
    var Q = arguments.length - 2;
    if (Q === 1) X.children = U;
    else if (1 < Q) {
      for (var ne = Array(Q), te = 0; te < Q; te++)
        ne[te] = arguments[te + 2];
      X.children = ne;
    }
    if (d && d.defaultProps)
      for (W in Q = d.defaultProps, Q)
        X[W] === void 0 && (X[W] = Q[W]);
    return B(d, P, X);
  }, q.createRef = function() {
    return { current: null };
  }, q.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, q.isValidElement = K, q.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: M
    };
  }, q.memo = function(d, k) {
    return {
      $$typeof: a,
      type: d,
      compare: k === void 0 ? null : k
    };
  }, q.startTransition = function(d) {
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
  }, q.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, q.use = function(d) {
    return E.H.use(d);
  }, q.useActionState = function(d, k, U) {
    return E.H.useActionState(d, k, U);
  }, q.useCallback = function(d, k) {
    return E.H.useCallback(d, k);
  }, q.useContext = function(d) {
    return E.H.useContext(d);
  }, q.useDebugValue = function() {
  }, q.useDeferredValue = function(d, k) {
    return E.H.useDeferredValue(d, k);
  }, q.useEffect = function(d, k) {
    return E.H.useEffect(d, k);
  }, q.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, q.useId = function() {
    return E.H.useId();
  }, q.useImperativeHandle = function(d, k, U) {
    return E.H.useImperativeHandle(d, k, U);
  }, q.useInsertionEffect = function(d, k) {
    return E.H.useInsertionEffect(d, k);
  }, q.useLayoutEffect = function(d, k) {
    return E.H.useLayoutEffect(d, k);
  }, q.useMemo = function(d, k) {
    return E.H.useMemo(d, k);
  }, q.useOptimistic = function(d, k) {
    return E.H.useOptimistic(d, k);
  }, q.useReducer = function(d, k, U) {
    return E.H.useReducer(d, k, U);
  }, q.useRef = function(d) {
    return E.H.useRef(d);
  }, q.useState = function(d) {
    return E.H.useState(d);
  }, q.useSyncExternalStore = function(d, k, U) {
    return E.H.useSyncExternalStore(
      d,
      k,
      U
    );
  }, q.useTransition = function() {
    return E.H.useTransition();
  }, q.version = "19.2.4", q;
}
var dt;
function Rl() {
  return dt || (dt = 1, Ye.exports = Tl()), Ye.exports;
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
function Ll() {
  if (mt) return se;
  mt = 1;
  var l = Rl();
  function t(r) {
    var a = "https://react.dev/errors/" + r;
    if (1 < arguments.length) {
      a += "?args[]=" + encodeURIComponent(arguments[1]);
      for (var m = 2; m < arguments.length; m++)
        a += "&args[]=" + encodeURIComponent(arguments[m]);
    }
    return "Minified React error #" + r + "; visit " + a + " for the full message or use the non-minified dev environment for full errors and additional helpful warnings.";
  }
  function n() {
  }
  var o = {
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
  function s(r, a, m) {
    var p = 3 < arguments.length && arguments[3] !== void 0 ? arguments[3] : null;
    return {
      $$typeof: i,
      key: p == null ? null : "" + p,
      children: r,
      containerInfo: a,
      implementation: m
    };
  }
  var c = l.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE;
  function u(r, a) {
    if (r === "font") return "";
    if (typeof a == "string")
      return a === "use-credentials" ? a : "";
  }
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = o, se.createPortal = function(r, a) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!a || a.nodeType !== 1 && a.nodeType !== 9 && a.nodeType !== 11)
      throw Error(t(299));
    return s(r, a, null, m);
  }, se.flushSync = function(r) {
    var a = c.T, m = o.p;
    try {
      if (c.T = null, o.p = 2, r) return r();
    } finally {
      c.T = a, o.p = m, o.d.f();
    }
  }, se.preconnect = function(r, a) {
    typeof r == "string" && (a ? (a = a.crossOrigin, a = typeof a == "string" ? a === "use-credentials" ? a : "" : void 0) : a = null, o.d.C(r, a));
  }, se.prefetchDNS = function(r) {
    typeof r == "string" && o.d.D(r);
  }, se.preinit = function(r, a) {
    if (typeof r == "string" && a && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin), h = typeof a.integrity == "string" ? a.integrity : void 0, v = typeof a.fetchPriority == "string" ? a.fetchPriority : void 0;
      m === "style" ? o.d.S(
        r,
        typeof a.precedence == "string" ? a.precedence : void 0,
        {
          crossOrigin: p,
          integrity: h,
          fetchPriority: v
        }
      ) : m === "script" && o.d.X(r, {
        crossOrigin: p,
        integrity: h,
        fetchPriority: v,
        nonce: typeof a.nonce == "string" ? a.nonce : void 0
      });
    }
  }, se.preinitModule = function(r, a) {
    if (typeof r == "string")
      if (typeof a == "object" && a !== null) {
        if (a.as == null || a.as === "script") {
          var m = u(
            a.as,
            a.crossOrigin
          );
          o.d.M(r, {
            crossOrigin: m,
            integrity: typeof a.integrity == "string" ? a.integrity : void 0,
            nonce: typeof a.nonce == "string" ? a.nonce : void 0
          });
        }
      } else a == null && o.d.M(r);
  }, se.preload = function(r, a) {
    if (typeof r == "string" && typeof a == "object" && a !== null && typeof a.as == "string") {
      var m = a.as, p = u(m, a.crossOrigin);
      o.d.L(r, m, {
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
  }, se.preloadModule = function(r, a) {
    if (typeof r == "string")
      if (a) {
        var m = u(a.as, a.crossOrigin);
        o.d.m(r, {
          as: typeof a.as == "string" && a.as !== "script" ? a.as : void 0,
          crossOrigin: m,
          integrity: typeof a.integrity == "string" ? a.integrity : void 0
        });
      } else o.d.m(r);
  }, se.requestFormReset = function(r) {
    o.d.r(r);
  }, se.unstable_batchedUpdates = function(r, a) {
    return r(a);
  }, se.useFormState = function(r, a, m) {
    return c.H.useFormState(r, a, m);
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
  return l(), Ke.exports = Ll(), Ke.exports;
}
var Ct = xl();
const { useState: _e, useCallback: ce, useRef: Re, useEffect: ye, useMemo: Je } = e;
function tt({ image: l }) {
  if (!l) return null;
  if (l.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: l, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = l.startsWith("css:") ? l.substring(4) : l.startsWith("colored:") ? l.substring(8) : l;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function Dl({
  option: l,
  removable: t,
  onRemove: n,
  removeLabel: o,
  draggable: i,
  onDragStart: s,
  onDragOver: c,
  onDrop: u,
  onDragEnd: r,
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
      onDragEnd: r
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
        "aria-label": o
      },
      "×"
    )
  );
}
function Il({
  option: l,
  highlighted: t,
  searchTerm: n,
  onSelect: o,
  onMouseEnter: i,
  id: s
}) {
  const c = ce(() => o(l.value), [o, l.value]), u = Je(() => {
    if (!n) return l.label;
    const r = l.label.toLowerCase().indexOf(n.toLowerCase());
    return r < 0 ? l.label : /* @__PURE__ */ e.createElement(e.Fragment, null, l.label.substring(0, r), /* @__PURE__ */ e.createElement("strong", null, l.label.substring(r, r + n.length)), l.label.substring(r + n.length));
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
const Ml = ({ controlId: l, state: t }) => {
  const n = le(), o = t.value ?? [], i = t.multiSelect === !0, s = t.customOrder === !0, c = t.mandatory === !0, u = t.disabled === !0, r = t.editable !== !1, a = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", h = s && i && !u && r, v = re({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), f = v["js.dropdownSelect.nothingFound"], S = ce(
    (C) => v["js.dropdownSelect.removeChip"].replace("{0}", C),
    [v]
  ), [y, g] = _e(!1), [w, D] = _e(""), [L, _] = _e(-1), [b, E] = _e(!1), [H, B] = _e({}), [R, K] = _e(null), [O, N] = _e(null), [$, J] = _e(null), F = Re(null), A = Re(null), M = Re(null), I = Re(o);
  I.current = o;
  const G = Re(-1), d = Je(
    () => new Set(o.map((C) => C.value)),
    [o]
  ), k = Je(() => {
    let C = m.filter((x) => !d.has(x.value));
    if (w) {
      const x = w.toLowerCase();
      C = C.filter((V) => V.label.toLowerCase().includes(x));
    }
    return C;
  }, [m, d, w]);
  ye(() => {
    w && k.length === 1 ? _(0) : _(-1);
  }, [k.length, w]), ye(() => {
    y && a && A.current && A.current.focus();
  }, [y, a, o]), ye(() => {
    var V, ee;
    if (G.current < 0) return;
    const C = G.current;
    G.current = -1;
    const x = (V = F.current) == null ? void 0 : V.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    x && x.length > 0 ? x[Math.min(C, x.length - 1)].focus() : (ee = F.current) == null || ee.focus();
  }, [o]), ye(() => {
    if (!y) return;
    const C = (x) => {
      F.current && !F.current.contains(x.target) && M.current && !M.current.contains(x.target) && (g(!1), D(""));
    };
    return document.addEventListener("mousedown", C), () => document.removeEventListener("mousedown", C);
  }, [y]), ye(() => {
    if (!y || !F.current) return;
    const C = F.current.getBoundingClientRect(), x = window.innerHeight - C.bottom, ee = x < 300 && C.top > x;
    B({
      left: C.left,
      width: C.width,
      ...ee ? { bottom: window.innerHeight - C.top } : { top: C.bottom }
    });
  }, [y]);
  const U = ce(async () => {
    if (!(u || !r) && (g(!0), D(""), _(-1), E(!1), !a))
      try {
        await n("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, r, a, n]), W = ce(() => {
    var C;
    g(!1), D(""), _(-1), (C = F.current) == null || C.focus();
  }, []), X = ce(
    (C) => {
      let x;
      if (i) {
        const V = m.find((ee) => ee.value === C);
        if (V)
          x = [...I.current, V];
        else
          return;
      } else {
        const V = m.find((ee) => ee.value === C);
        if (V)
          x = [V];
        else
          return;
      }
      I.current = x, n("valueChanged", { value: x.map((V) => V.value) }), i ? (D(""), _(-1)) : W();
    },
    [i, m, n, W]
  ), P = ce(
    (C) => {
      G.current = I.current.findIndex((V) => V.value === C);
      const x = I.current.filter((V) => V.value !== C);
      I.current = x, n("valueChanged", { value: x.map((V) => V.value) });
    },
    [n]
  ), Q = ce(
    (C) => {
      C.stopPropagation(), n("valueChanged", { value: [] }), W();
    },
    [n, W]
  ), ne = ce((C) => {
    D(C.target.value);
  }, []), te = ce(
    (C) => {
      if (!y) {
        if (C.key === "ArrowDown" || C.key === "ArrowUp" || C.key === "Enter" || C.key === " ") {
          if (C.target.tagName === "BUTTON") return;
          C.preventDefault(), C.stopPropagation(), U();
        }
        return;
      }
      switch (C.key) {
        case "ArrowDown":
          C.preventDefault(), C.stopPropagation(), _(
            (x) => x < k.length - 1 ? x + 1 : 0
          );
          break;
        case "ArrowUp":
          C.preventDefault(), C.stopPropagation(), _(
            (x) => x > 0 ? x - 1 : k.length - 1
          );
          break;
        case "Enter":
          C.preventDefault(), C.stopPropagation(), L >= 0 && L < k.length && X(k[L].value);
          break;
        case "Escape":
          C.preventDefault(), C.stopPropagation(), W();
          break;
        case "Tab":
          W();
          break;
        case "Backspace":
          w === "" && i && o.length > 0 && P(o[o.length - 1].value);
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
      w,
      i,
      o,
      P
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
      K(C), x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", String(C));
    },
    []
  ), me = ce(
    (C, x) => {
      if (x.preventDefault(), x.dataTransfer.dropEffect = "move", R === null || R === C) {
        N(null), J(null);
        return;
      }
      const V = x.currentTarget.getBoundingClientRect(), ee = V.left + V.width / 2, oe = x.clientX < ee ? "before" : "after";
      N(C), J(oe);
    },
    [R]
  ), he = ce(
    (C) => {
      if (C.preventDefault(), R === null || O === null || $ === null || R === O) return;
      const x = [...I.current], [V] = x.splice(R, 1);
      let ee = O;
      R < O ? ee = $ === "before" ? ee - 1 : ee : ee = $ === "before" ? ee : ee + 1, x.splice(ee, 0, V), I.current = x, n("valueChanged", { value: x.map((oe) => oe.value) }), K(null), N(null), J(null);
    },
    [R, O, $, n]
  ), be = ce(() => {
    K(null), N(null), J(null);
  }, []);
  if (ye(() => {
    if (L < 0 || !M.current) return;
    const C = M.current.querySelector(
      `[id="${l}-opt-${L}"]`
    );
    C && C.scrollIntoView({ block: "nearest" });
  }, [L, l]), !r)
    return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlDropdownSelect tlDropdownSelect--immutable" }, o.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : o.map((C) => /* @__PURE__ */ e.createElement("span", { key: C.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(tt, { image: C.image }), /* @__PURE__ */ e.createElement("span", null, C.label))));
  const T = !c && o.length > 0 && !u, j = y ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: M,
      className: "tlDropdownSelect__dropdown",
      style: H
    },
    (a || b) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: A,
        type: "text",
        className: "tlDropdownSelect__search",
        value: w,
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
      a && k.map((C, x) => /* @__PURE__ */ e.createElement(
        Il,
        {
          key: C.value,
          id: `${l}-opt-${x}`,
          option: C,
          highlighted: x === L,
          searchTerm: w,
          onSelect: X,
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
      className: "tlDropdownSelect" + (y ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": y,
      "aria-haspopup": "listbox",
      "aria-owns": y ? `${l}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: y ? void 0 : U,
      onKeyDown: te
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, o.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : o.map((C, x) => {
      let V = "";
      return R === x ? V = "tlDropdownSelect__chip--dragging" : O === x && $ === "before" ? V = "tlDropdownSelect__chip--dropBefore" : O === x && $ === "after" && (V = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        Dl,
        {
          key: C.value,
          option: C,
          removable: !u && (i || !c),
          onRemove: P,
          removeLabel: S(C.label),
          draggable: h,
          onDragStart: h ? (ee) => de(x, ee) : void 0,
          onDragOver: h ? (ee) => me(x, ee) : void 0,
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
  ), j && Ct.createPortal(j, document.body));
}, { useCallback: Ge, useRef: jl } = e, yt = "application/x-tl-color", Pl = ({
  colors: l,
  columns: t,
  onSelect: n,
  onConfirm: o,
  onSwap: i,
  onReplace: s
}) => {
  const c = jl(null), u = Ge(
    (m) => (p) => {
      c.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), r = Ge((m) => {
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
        onDoubleClick: m != null ? () => o(m) : void 0,
        onDragStart: m != null ? u(p) : void 0,
        onDragOver: r,
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
  const o = (i) => kt(i).toString(16).padStart(2, "0");
  return "#" + o(l) + o(t) + o(n);
}
function Bl(l, t, n) {
  const o = l / 255, i = t / 255, s = n / 255, c = Math.max(o, i, s), u = Math.min(o, i, s), r = c - u;
  let a = 0;
  r !== 0 && (c === o ? a = (i - s) / r % 6 : c === i ? a = (s - o) / r + 2 : a = (o - i) / r + 4, a *= 60, a < 0 && (a += 360));
  const m = c === 0 ? 0 : r / c;
  return [a, m, c];
}
function Ol(l, t, n) {
  const o = n * t, i = o * (1 - Math.abs(l / 60 % 2 - 1)), s = n - o;
  let c = 0, u = 0, r = 0;
  return l < 60 ? (c = o, u = i, r = 0) : l < 120 ? (c = i, u = o, r = 0) : l < 180 ? (c = 0, u = o, r = i) : l < 240 ? (c = 0, u = i, r = o) : l < 300 ? (c = i, u = 0, r = o) : (c = o, u = 0, r = i), [
    Math.round((c + s) * 255),
    Math.round((u + s) * 255),
    Math.round((r + s) * 255)
  ];
}
function Al(l) {
  return Bl(...St(l));
}
function Xe(l, t, n) {
  return Nt(...Ol(l, t, n));
}
const { useCallback: ke, useRef: ft } = e, $l = ({ color: l, onColorChange: t }) => {
  const [n, o, i] = Al(l), s = ft(null), c = ft(null), u = ke(
    (f, S) => {
      var D;
      const y = (D = s.current) == null ? void 0 : D.getBoundingClientRect();
      if (!y) return;
      const g = Math.max(0, Math.min(1, (f - y.left) / y.width)), w = Math.max(0, Math.min(1, 1 - (S - y.top) / y.height));
      t(Xe(n, g, w));
    },
    [n, t]
  ), r = ke(
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
      var w;
      const S = (w = c.current) == null ? void 0 : w.getBoundingClientRect();
      if (!S) return;
      const g = Math.max(0, Math.min(1, (f - S.top) / S.height)) * 360;
      t(Xe(g, o, i));
    },
    [o, i, t]
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
      onPointerDown: r,
      onPointerMove: a
    },
    /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__svHandle",
        style: { left: `${o * 100}%`, top: `${(1 - i) * 100}%` }
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
function Fl(l, t) {
  const n = t.toUpperCase();
  return l.some((o) => o != null && o.toUpperCase() === n);
}
const Wl = {
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
}, { useState: je, useCallback: fe, useEffect: qe, useRef: Hl, useLayoutEffect: zl } = e, Ul = ({
  anchorRef: l,
  currentColor: t,
  palette: n,
  paletteColumns: o,
  defaultPalette: i,
  canReset: s,
  onConfirm: c,
  onCancel: u,
  onPaletteChange: r
}) => {
  const [a, m] = je("palette"), [p, h] = je(t), v = Hl(null), f = re(Wl), [S, y] = je(null);
  zl(() => {
    if (!l.current || !v.current) return;
    const M = l.current.getBoundingClientRect(), I = v.current.getBoundingClientRect();
    let G = M.bottom + 4, d = M.left;
    G + I.height > window.innerHeight && (G = M.top - I.height - 4), d + I.width > window.innerWidth && (d = Math.max(0, M.right - I.width)), y({ top: G, left: d });
  }, [l]);
  const g = p != null, [w, D, L] = g ? St(p) : [0, 0, 0], [_, b] = je((p == null ? void 0 : p.toUpperCase()) ?? "");
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
      h(Nt(M === "r" ? d : w, M === "g" ? d : D, M === "b" ? d : L));
    },
    [w, D, L]
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
  }, []), O = fe(
    (M) => {
      c(M);
    },
    [c]
  ), N = fe(
    (M, I) => {
      const G = [...n], d = G[M];
      G[M] = G[I], G[I] = d, r(G);
    },
    [n, r]
  ), $ = fe(
    (M, I) => {
      const G = [...n];
      G[M] = I, r(G);
    },
    [n, r]
  ), J = fe(() => {
    r([...i]);
  }, [i, r]), F = fe(
    (M) => {
      if (Fl(n, M)) return;
      const I = n.indexOf(null);
      if (I < 0) return;
      const G = [...n];
      G[I] = M.toUpperCase(), r(G);
    },
    [n, r]
  ), A = fe(() => {
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
      Pl,
      {
        colors: n,
        columns: o,
        onSelect: K,
        onConfirm: O,
        onSwap: N,
        onReplace: $
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: J }, f["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement($l, { color: p ?? "#000000", onColorChange: h }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, f["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
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
        value: g ? w : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, f["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: g ? D : "",
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
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, s && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: R }, f["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, f["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: A }, f["js.colorInput.ok"]))
  );
}, Vl = { "js.colorInput.chooseColor": "Choose color" }, { useState: Kl, useCallback: Pe, useRef: Yl } = e, Gl = ({ controlId: l, state: t }) => {
  const n = le(), o = re(Vl), [i, s] = Kl(!1), c = Yl(null), u = t.value, r = t.editable !== !1, a = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? a, h = Pe(() => {
    r && s(!0);
  }, [r]), v = Pe(
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
  return r ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: h,
      disabled: t.disabled === !0,
      title: u ?? "",
      "aria-label": o["js.colorInput.chooseColor"]
    }
  ), i && /* @__PURE__ */ e.createElement(
    Ul,
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
}, { useState: Le, useCallback: Ee, useEffect: Be, useRef: ht, useLayoutEffect: Xl, useMemo: ql } = e, Zl = {
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
}, Ql = ({
  anchorRef: l,
  currentValue: t,
  icons: n,
  iconsLoaded: o,
  onSelect: i,
  onCancel: s,
  onLoadIcons: c
}) => {
  const u = re(Zl), [r, a] = Le("simple"), [m, p] = Le(""), [h, v] = Le(t ?? ""), [f, S] = Le(!1), [y, g] = Le(null), w = ht(null), D = ht(null);
  Xl(() => {
    if (!l.current || !w.current) return;
    const O = l.current.getBoundingClientRect(), N = w.current.getBoundingClientRect();
    let $ = O.bottom + 4, J = O.left;
    $ + N.height > window.innerHeight && ($ = O.top - N.height - 4), J + N.width > window.innerWidth && (J = Math.max(0, O.right - N.width)), g({ top: $, left: J });
  }, [l]), Be(() => {
    !o && !f && c().catch(() => S(!0));
  }, [o, f, c]), Be(() => {
    o && D.current && D.current.focus();
  }, [o]), Be(() => {
    const O = (N) => {
      N.key === "Escape" && s();
    };
    return document.addEventListener("keydown", O), () => document.removeEventListener("keydown", O);
  }, [s]), Be(() => {
    const O = ($) => {
      w.current && !w.current.contains($.target) && s();
    }, N = setTimeout(() => document.addEventListener("mousedown", O), 0);
    return () => {
      clearTimeout(N), document.removeEventListener("mousedown", O);
    };
  }, [s]);
  const L = ql(() => {
    if (!m) return n;
    const O = m.toLowerCase();
    return n.filter(
      (N) => N.prefix.toLowerCase().includes(O) || N.label.toLowerCase().includes(O) || N.terms != null && N.terms.some(($) => $.includes(O))
    );
  }, [n, m]), _ = Ee((O) => {
    p(O.target.value);
  }, []), b = Ee(
    (O) => {
      i(O);
    },
    [i]
  ), E = Ee((O) => {
    v(O);
  }, []), H = Ee((O) => {
    v(O.target.value);
  }, []), B = Ee(() => {
    i(h || null);
  }, [h, i]), R = Ee(() => {
    i(null);
  }, [i]), K = Ee(async (O) => {
    O.preventDefault(), S(!1);
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
      ref: w,
      style: y ? { top: y.top, left: y.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "simple" ? " tlIconSelect__tab--active" : ""),
        onClick: () => a("simple")
      },
      u["js.iconSelect.simpleTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlIconSelect__tab" + (r === "advanced" ? " tlIconSelect__tab--active" : ""),
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
      !o && !f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      f && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: K }, u["js.iconSelect.loadError"])),
      o && L.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      o && L.map(
        (O) => O.variants.map((N) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: N.encoded,
            className: "tlIconSelect__iconCell" + (N.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": N.encoded === t,
            tabIndex: 0,
            title: O.label,
            onClick: () => r === "simple" ? b(N.encoded) : E(N.encoded),
            onKeyDown: ($) => {
              ($.key === "Enter" || $.key === " ") && ($.preventDefault(), r === "simple" ? b(N.encoded) : E(N.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(ve, { encoded: N.encoded })
        ))
      )
    ),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: h,
        onChange: H
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, h && /* @__PURE__ */ e.createElement(ve, { encoded: h })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, h ? h.startsWith("css:") ? h.substring(4) : h : ""))),
    r === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: s }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: R }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: B }, u["js.iconSelect.ok"]))
  );
}, Jl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: eo, useCallback: Oe, useRef: to } = e, no = ({ controlId: l, state: t }) => {
  const n = le(), o = re(Jl), [i, s] = eo(!1), c = to(null), u = t.value, r = t.editable !== !1, a = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, h = Oe(() => {
    r && !a && s(!0);
  }, [r, a]), v = Oe(
    (y) => {
      s(!1), n("valueChanged", { value: y });
    },
    [n]
  ), f = Oe(() => {
    s(!1);
  }, []), S = Oe(async () => {
    await n("loadIcons");
  }, [n]);
  return r ? /* @__PURE__ */ e.createElement("span", { id: l, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: c,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: h,
      disabled: a,
      title: u ?? "",
      "aria-label": o["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(ve, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), i && /* @__PURE__ */ e.createElement(
    Ql,
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
}, { useCallback: Se, useEffect: lo, useMemo: bt, useRef: oo, useState: Ze } = e, ro = {
  quarter: 0.25,
  third: 1 / 3,
  half: 0.5,
  "two-thirds": 2 / 3,
  full: 1
}, ao = [1, 2, 3, 4];
function so(l, t) {
  const n = /^([\d.]+)(rem|em|px)?$/.exec(l.trim());
  if (!n) return 16 * t;
  const o = parseFloat(n[1]), i = n[2] || "px";
  return i === "rem" || i === "em" ? o * t : o;
}
function co(l, t) {
  const n = Math.max(1, Math.floor(l / t));
  let o = 1;
  for (const i of ao)
    n >= i && (o = i);
  return o;
}
function io(l, t) {
  const n = ro[l] ?? 1;
  return Math.max(1, Math.round(n * t));
}
function uo(l, t) {
  const n = Math.max(1, t), o = {}, i = (p, h) => !!(o[p] && o[p][h]), s = (p, h) => {
    o[p] || (o[p] = {}), o[p][h] = !0;
  }, c = [];
  let u = 0, r = 0;
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
    let v = Math.min(io(p.width, n), n);
    for (; i(u, r); )
      r++, r >= n && (r = 0, u++);
    let f = 0;
    for (let D = r; D < n && !i(u, D); D++)
      f++;
    if (v > f) {
      for (a(u), r = 0, u++; i(u, r); )
        r++, r >= n && (r = 0, u++);
      f = 0;
      for (let D = r; D < n && !i(u, D); D++)
        f++;
      v = Math.min(v, f);
    }
    const S = r, y = r + v, g = u, w = u + h;
    c.push({ id: p.id, colStart: S, colEnd: y, rowStart: g, rowEnd: w });
    for (let D = g; D < w; D++)
      for (let L = S; L < y; L++) s(D, L);
    r = y, r >= n && (r = 0, u++);
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
const mo = ({ controlId: l }) => {
  const t = Z(), n = le(), o = t.minColWidth ?? "16rem", i = (t.children ?? []).filter((b) => b && b.id), s = oo(null), [c, u] = Ze(1), r = t.editMode === !0;
  lo(() => {
    const b = s.current;
    if (!b) return;
    const E = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16, H = so(o, E), B = () => u(co(b.clientWidth, H));
    B();
    const R = new ResizeObserver(B);
    return R.observe(b), () => R.disconnect();
  }, [o]);
  const a = bt(() => uo(i, c), [i, c]), m = bt(() => {
    const b = {};
    for (const E of a) b[E.id] = E;
    return b;
  }, [a]), [p, h] = Ze(null), [v, f] = Ze(null), S = Se((b, E) => {
    if (!r) {
      b.preventDefault();
      return;
    }
    h(E), b.dataTransfer.effectAllowed = "move", b.dataTransfer.setData("text/plain", E);
  }, [r]), y = Se((b, E) => {
    if (!r || !p || p === E) return;
    b.preventDefault(), b.dataTransfer.dropEffect = "move";
    const H = b.currentTarget.getBoundingClientRect(), B = b.clientX < H.left + H.width / 2;
    f((R) => R && R.id === E && R.before === B ? R : { id: E, before: B });
  }, [r, p]), g = Se(() => {
  }, []), w = Se((b, E, H) => {
    const B = i.map((N) => N.id), R = B.indexOf(b);
    if (R < 0) return;
    B.splice(R, 1);
    const K = B.indexOf(E);
    if (K < 0) {
      B.splice(R, 0, b);
      return;
    }
    const O = H ? K : K + 1;
    B.splice(O, 0, b), n("reorder", { order: B });
  }, [i, n]), D = Se((b, E) => {
    if (!r || !p || p === E) return;
    b.preventDefault();
    const H = b.currentTarget.getBoundingClientRect(), B = b.clientX < H.left + H.width / 2;
    w(p, E, B), h(null), f(null);
  }, [r, p, w]), L = Se(() => {
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
      className: "tlDashboard" + (r ? " tlDashboard--edit" : "")
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
          draggable: r,
          onDragStart: (R) => S(R, b.id),
          onDragOver: (R) => y(R, b.id),
          onDragLeave: g,
          onDrop: (R) => D(R, b.id),
          onDragEnd: L
        },
        /* @__PURE__ */ e.createElement(Y, { control: b.control }),
        r && /* @__PURE__ */ e.createElement("div", { className: "tlDashboard__overlay" })
      );
    }))
  );
}, { useCallback: po, useRef: _t, useState: vt, useEffect: Et, useLayoutEffect: fo } = e, ho = ({ group: l }) => {
  const t = l.items.filter((n) => n != null);
  return t.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, t.map((n, o) => /* @__PURE__ */ e.createElement("span", { key: o, className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: n }))));
}, bo = ({ group: l }) => {
  var p, h;
  const [t, n] = vt(!1), [o, i] = vt({}), s = _t(null), c = _t(null), u = po(() => {
    n((v) => !v);
  }, []);
  fo(() => {
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
  const r = l.items.filter((v) => v != null);
  if (r.length === 0) return null;
  if (r.length === 1 && !((p = l.subGroups) != null && p.length) && !l.icon)
    return /* @__PURE__ */ e.createElement("div", { className: "tlToolbar__group tlToolbar__group--inline" }, /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__item" }, /* @__PURE__ */ e.createElement(Y, { control: r[0] })));
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
  ), Ct.createPortal(
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: c,
        className: "tlToolbar__dropdown",
        role: "menu",
        hidden: !t,
        style: t ? o : void 0,
        onClick: () => n(!1)
      },
      r.map((v, f) => /* @__PURE__ */ e.createElement("div", { key: f, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: v }))),
      (h = l.subGroups) == null ? void 0 : h.map((v, f) => /* @__PURE__ */ e.createElement(e.Fragment, { key: `sub-${f}` }, /* @__PURE__ */ e.createElement("hr", { className: "tlToolbar__dropdownSeparator" }), v.items.map((S, y) => /* @__PURE__ */ e.createElement("div", { key: y, className: "tlToolbar__dropdownItem", role: "menuitem" }, /* @__PURE__ */ e.createElement(Y, { control: S })))))
    ),
    document.body
  ));
}, _o = ({ controlId: l }) => {
  const o = (Z().groups ?? []).filter((i) => i.items.some((s) => s != null));
  return o.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: l, className: "tlToolbar", role: "toolbar" }, o.map((i, s) => /* @__PURE__ */ e.createElement(e.Fragment, { key: i.name }, s > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlToolbar__separator", "aria-hidden": "true" }), i.display === "menu" ? /* @__PURE__ */ e.createElement(bo, { group: i }) : /* @__PURE__ */ e.createElement(ho, { group: i }))));
}, vo = ({ controlId: l }) => {
  const t = Z();
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlTileStack", style: { width: "100%", height: "100%" } }, t.frame && /* @__PURE__ */ e.createElement(Y, { control: t.frame }));
}, Eo = ({ controlId: l }) => {
  const n = Z().children ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlot" }, n.map((o, i) => /* @__PURE__ */ e.createElement(Y, { key: i, control: o })));
}, go = ({ controlId: l }) => /* @__PURE__ */ e.createElement("div", { id: l, className: "tlSlotContent", style: { display: "none" } }), wo = {
  "js.sidebar.openDrawer": "Open navigation"
}, Co = ({ controlId: l }) => {
  const t = le(), n = re(wo);
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
z("TLTextInput", xt);
z("TLPasswordInput", It);
z("TLNumberInput", jt);
z("TLDatePicker", Bt);
z("TLSelect", At);
z("TLCheckbox", Ft);
z("TLTable", Wt);
z("TLCounter", Kt);
z("TLTabBar", Gt);
z("TLFieldList", Xt);
z("TLAudioRecorder", Zt);
z("TLAudioPlayer", Jt);
z("TLFileUpload", tn);
z("TLBinaryField", ln);
z("TLDownload", rn);
z("TLPhotoCapture", sn);
z("TLPhotoViewer", un);
z("TLPdfViewer", mn);
z("TLSplitPanel", pn);
z("TLPanel", gn);
z("TLInset", In);
z("TLMaximizeRoot", wn);
z("TLDeckPane", Cn);
z("TLSidebar", xn);
z("TLStack", Dn);
z("TLGrid", Mn);
z("TLCard", jn);
z("TLAppBar", Pn);
z("TLBreadcrumb", On);
z("TLBottomBar", $n);
z("TLDialog", Wn);
z("TLDialogManager", Un);
z("TLWindow", Yn);
z("TLDrawer", Zn);
z("TLContextMenuRegion", Jn);
z("TLSnackbar", ll);
z("TLMenu", rl);
z("TLAppShell", al);
z("TLText", sl);
z("TLTableView", il);
z("TLFormLayout", bl);
z("TLFormGroup", El);
z("TLFormField", yl);
z("TLResourceCell", kl);
z("TLTreeView", Nl);
z("TLDropdownSelect", Ml);
z("TLColorInput", Gl);
z("TLIconSelect", no);
z("TLDashboard", mo);
z("TLToolbar", _o);
z("TLTileStack", vo);
z("TLSlot", Eo);
z("TLSlotContent", go);
z("TLDrawerToggle", Co);
