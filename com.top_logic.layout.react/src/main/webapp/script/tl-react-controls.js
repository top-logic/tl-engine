import { React as e, useTLFieldValue as ke, getComponent as gt, useTLState as G, useTLCommand as ne, TLChild as Y, useTLUpload as Ye, useI18N as oe, useTLDataUrl as Ge, register as H } from "tl-react-bridge";
const { useCallback: Ct } = e, yt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = Ct(
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
}, { useCallback: wt } = e, kt = ({ controlId: n, state: t, config: a }) => {
  const [l, c] = ke(), i = wt(
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
}, { useCallback: St } = e, Nt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = St(
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
}, { useCallback: Tt } = e, Rt = ({ controlId: n, state: t, config: a }) => {
  var m;
  const [l, c] = ke(), i = Tt(
    (p) => {
      c(p.target.value || null);
    },
    [c]
  ), o = t.options ?? (a == null ? void 0 : a.options) ?? [];
  if (t.editable === !1) {
    const p = ((m = o.find((f) => f.value === l)) == null ? void 0 : m.label) ?? "";
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
}, { useCallback: Lt } = e, xt = ({ controlId: n, state: t }) => {
  const [a, l] = ke(), c = Lt(
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
}, Dt = ({ controlId: n, state: t }) => {
  const a = t.columns ?? [], l = t.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { id: n, className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, i) => /* @__PURE__ */ e.createElement("tr", { key: i }, a.map((o) => {
    const u = o.cellModule ? gt(o.cellModule) : void 0, s = c[o.name];
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
const { useCallback: It } = e, jt = {
  detail: () => /* @__PURE__ */ e.createElement(
    "svg",
    {
      viewBox: "0 0 16 16",
      width: "16",
      height: "16",
      fill: "none",
      stroke: "currentColor",
      strokeWidth: "1.5",
      strokeLinecap: "round",
      strokeLinejoin: "round"
    },
    /* @__PURE__ */ e.createElement("circle", { cx: "8", cy: "8", r: "6" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "5.5", x2: "8", y2: "5.5", strokeWidth: "2" }),
    /* @__PURE__ */ e.createElement("line", { x1: "8", y1: "7.5", x2: "8", y2: "11" })
  ),
  delete: () => /* @__PURE__ */ e.createElement(
    "svg",
    {
      viewBox: "0 0 16 16",
      width: "16",
      height: "16",
      fill: "none",
      stroke: "currentColor",
      strokeWidth: "1.5",
      strokeLinecap: "round",
      strokeLinejoin: "round"
    },
    /* @__PURE__ */ e.createElement("line", { x1: "2", y1: "4", x2: "14", y2: "4" }),
    /* @__PURE__ */ e.createElement("path", { d: "M5.5 4V2.5h5V4" }),
    /* @__PURE__ */ e.createElement("path", { d: "M3.5 4v9.5a1 1 0 0 0 1 1h7a1 1 0 0 0 1-1V4" }),
    /* @__PURE__ */ e.createElement("line", { x1: "6.5", y1: "7", x2: "6.5", y2: "11" }),
    /* @__PURE__ */ e.createElement("line", { x1: "9.5", y1: "7", x2: "9.5", y2: "11" })
  )
}, Pt = ({ controlId: n, command: t, label: a, disabled: l }) => {
  const c = G(), i = ne(), o = t ?? "click", u = a ?? c.label, s = l ?? c.disabled === !0, r = c.hidden === !0, m = c.icon, p = c.image, f = r ? { display: "none" } : void 0, w = It(() => {
    i(o);
  }, [i, o]), b = m ? jt[m] : void 0;
  return b ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: w,
      disabled: s,
      style: f,
      className: "tlReactButton tlReactButton--icon",
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement(b, null)
  ) : p ? /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: w,
      disabled: s,
      style: f,
      className: "tlReactButton"
    },
    /* @__PURE__ */ e.createElement(we, { encoded: p, className: "tlReactButton__image" }),
    /* @__PURE__ */ e.createElement("span", { className: "tlReactButton__label" }, u)
  ) : /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      id: n,
      onClick: w,
      disabled: s,
      style: f,
      className: "tlReactButton"
    },
    u
  );
}, { useCallback: Mt } = e, At = ({ controlId: n, command: t, label: a, active: l, disabled: c }) => {
  const i = G(), o = ne(), u = t ?? "click", s = a ?? i.label, r = l ?? i.active === !0, m = c ?? i.disabled === !0, p = Mt(() => {
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
}, Bt = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.count ?? 0, c = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, l), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Ot } = e, $t = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.tabs ?? [], c = t.activeTabId, i = Ot((o) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, Ft = ({ controlId: n }) => {
  const t = G(), a = t.title, l = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, l.map((c, i) => /* @__PURE__ */ e.createElement("div", { key: i, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(Y, { control: c })))));
}, Ht = {
  "js.audioRecorder.record": "Record audio",
  "js.audioRecorder.stop": "Stop recording",
  "js.uploading": "Uploading…",
  "js.audioRecorder.error.insecure": "Microphone requires a secure connection (HTTPS).",
  "js.audioRecorder.error.denied": "Microphone access denied or unavailable."
}, Wt = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), u = e.useRef(null), s = e.useRef([]), r = e.useRef(null), m = t.status ?? "idle", p = t.error, f = m === "received" ? "idle" : l !== "idle" ? l : m, w = e.useCallback(async () => {
    if (l === "recording") {
      const k = u.current;
      k && k.state !== "inactive" && k.stop();
      return;
    }
    if (l !== "uploading") {
      if (o(null), !window.isSecureContext || !navigator.mediaDevices) {
        o("js.audioRecorder.error.insecure");
        return;
      }
      try {
        const k = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = k, s.current = [];
        const M = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", I = new MediaRecorder(k, M ? { mimeType: M } : void 0);
        u.current = I, I.ondataavailable = (_) => {
          _.data.size > 0 && s.current.push(_.data);
        }, I.onstop = async () => {
          k.getTracks().forEach((E) => E.stop()), r.current = null;
          const _ = new Blob(s.current, { type: I.mimeType || "audio/webm" });
          if (s.current = [], _.size === 0) {
            c("idle");
            return;
          }
          c("uploading");
          const C = new FormData();
          C.append("audio", _, "recording.webm"), await a(C), c("idle");
        }, I.start(), c("recording");
      } catch (k) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", k), o("js.audioRecorder.error.denied"), c("idle");
      }
    }
  }, [l, a]), b = oe(Ht), v = f === "recording" ? b["js.audioRecorder.stop"] : f === "uploading" ? b["js.uploading"] : b["js.audioRecorder.record"], h = f === "uploading", N = ["tlAudioRecorder__button"];
  return f === "recording" && N.push("tlAudioRecorder__button--recording"), f === "uploading" && N.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: N.join(" "),
      onClick: w,
      disabled: h,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${f === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, b[i]), p && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, p));
}, zt = {
  "js.audioPlayer.play": "Play audio",
  "js.audioPlayer.pause": "Pause audio",
  "js.audioPlayer.noAudio": "No audio",
  "js.loading": "Loading…"
}, Ut = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = !!t.hasAudio, c = t.dataRevision ?? 0, [i, o] = e.useState(l ? "idle" : "disabled"), u = e.useRef(null), s = e.useRef(null), r = e.useRef(c);
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
        const h = await fetch(a);
        if (!h.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", h.status), o("idle");
          return;
        }
        const N = await h.blob();
        s.current = URL.createObjectURL(N);
      } catch (h) {
        console.error("[TLAudioPlayer] Fetch error:", h), o("idle");
        return;
      }
    }
    const v = new Audio(s.current);
    u.current = v, v.onended = () => {
      o("idle");
    }, v.play(), o("playing");
  }, [i, a]), p = oe(zt), f = i === "loading" ? p["js.loading"] : i === "playing" ? p["js.audioPlayer.pause"] : i === "disabled" ? p["js.audioPlayer.noAudio"] : p["js.audioPlayer.play"], w = i === "disabled" || i === "loading", b = ["tlAudioPlayer__button"];
  return i === "playing" && b.push("tlAudioPlayer__button--playing"), i === "loading" && b.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: b.join(" "),
      onClick: m,
      disabled: w,
      title: f,
      "aria-label": f
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${i === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, Vt = {
  "js.fileUpload.choose": "Choose file",
  "js.uploading": "Uploading…"
}, Kt = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(!1), u = e.useRef(null), s = t.status ?? "idle", r = t.error, m = t.accept ?? "", p = s === "received" ? "idle" : l !== "idle" ? l : s, f = e.useCallback(async (_) => {
    c("uploading");
    const C = new FormData();
    C.append("file", _, _.name), await a(C), c("idle");
  }, [a]), w = e.useCallback((_) => {
    var E;
    const C = (E = _.target.files) == null ? void 0 : E[0];
    C && f(C);
  }, [f]), b = e.useCallback(() => {
    var _;
    l !== "uploading" && ((_ = u.current) == null || _.click());
  }, [l]), v = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), o(!0);
  }, []), h = e.useCallback((_) => {
    _.preventDefault(), _.stopPropagation(), o(!1);
  }, []), N = e.useCallback((_) => {
    var E;
    if (_.preventDefault(), _.stopPropagation(), o(!1), l === "uploading") return;
    const C = (E = _.dataTransfer.files) == null ? void 0 : E[0];
    C && f(C);
  }, [l, f]), k = p === "uploading", M = oe(Vt), I = p === "uploading" ? M["js.uploading"] : M["js.fileUpload.choose"];
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlFileUpload${i ? " tlFileUpload--dragover" : ""}`,
      onDragOver: v,
      onDragLeave: h,
      onDrop: N
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: u,
        type: "file",
        accept: m || void 0,
        onChange: w,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button" + (p === "uploading" ? " tlFileUpload__button--uploading" : ""),
        onClick: b,
        disabled: k,
        title: I,
        "aria-label": I
      },
      /* @__PURE__ */ e.createElement("svg", { className: "tlFileUpload__icon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
    ),
    r && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, r)
  );
}, Yt = {
  "js.download.noFile": "No file",
  "js.download.file": "Download {0}",
  "js.downloading": "Downloading…",
  "js.download.clear": "Clear",
  "js.download.clearFile": "Clear file"
}, Gt = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = ne(), c = !!t.hasData, i = t.dataRevision ?? 0, o = t.fileName ?? "download", u = !!t.clearable, [s, r] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!c || s)) {
      r(!0);
      try {
        const b = a + (a.includes("?") ? "&" : "?") + "rev=" + i, v = await fetch(b);
        if (!v.ok) {
          console.error("[TLDownload] Failed to fetch data:", v.status);
          return;
        }
        const h = await v.blob(), N = URL.createObjectURL(h), k = document.createElement("a");
        k.href = N, k.download = o, k.style.display = "none", document.body.appendChild(k), k.click(), document.body.removeChild(k), URL.revokeObjectURL(N);
      } catch (b) {
        console.error("[TLDownload] Fetch error:", b);
      } finally {
        r(!1);
      }
    }
  }, [c, s, a, i, o]), p = e.useCallback(async () => {
    c && await l("clear");
  }, [c, l]), f = oe(Yt);
  if (!c)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, f["js.download.noFile"]));
  const w = s ? f["js.downloading"] : f["js.download.file"].replace("{0}", o);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (s ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: s,
      title: w,
      "aria-label": w
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: o }, o), u && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: p,
      title: f["js.download.clear"],
      "aria-label": f["js.download.clearFile"]
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  ));
}, Xt = {
  "js.photoCapture.open": "Open camera",
  "js.photoCapture.close": "Close camera",
  "js.photoCapture.capture": "Capture photo",
  "js.photoCapture.mirror": "Mirror camera",
  "js.uploading": "Uploading…",
  "js.photoCapture.error.denied": "Camera access denied or unavailable."
}, qt = ({ controlId: n }) => {
  const t = G(), a = Ye(), [l, c] = e.useState("idle"), [i, o] = e.useState(null), [u, s] = e.useState(!1), r = e.useRef(null), m = e.useRef(null), p = e.useRef(null), f = e.useRef(null), w = e.useRef(null), b = t.error, v = e.useMemo(
    () => {
      var L;
      return !!(window.isSecureContext && ((L = navigator.mediaDevices) != null && L.getUserMedia));
    },
    []
  ), h = e.useCallback(() => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null), r.current && (r.current.srcObject = null);
  }, []), N = e.useCallback(() => {
    h(), c("idle");
  }, [h]), k = e.useCallback(async () => {
    var L;
    if (l !== "uploading") {
      if (o(null), !v) {
        (L = f.current) == null || L.click();
        return;
      }
      try {
        const B = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        m.current = B, c("overlayOpen");
      } catch (B) {
        console.error("[TLPhotoCapture] Camera access denied or unavailable:", B), o("js.photoCapture.error.denied"), c("idle");
      }
    }
  }, [l, v]), M = e.useCallback(async () => {
    if (l !== "overlayOpen")
      return;
    const L = r.current, B = p.current;
    if (!L || !B)
      return;
    B.width = L.videoWidth, B.height = L.videoHeight;
    const R = B.getContext("2d");
    R && (R.drawImage(L, 0, 0), h(), c("uploading"), B.toBlob(async (j) => {
      if (!j) {
        c("idle");
        return;
      }
      const P = new FormData();
      P.append("photo", j, "capture.jpg"), await a(P), c("idle");
    }, "image/jpeg", 0.85));
  }, [l, a, h]), I = e.useCallback(async (L) => {
    var j;
    const B = (j = L.target.files) == null ? void 0 : j[0];
    if (!B) return;
    c("uploading");
    const R = new FormData();
    R.append("photo", B, B.name), await a(R), c("idle"), f.current && (f.current.value = "");
  }, [a]);
  e.useEffect(() => {
    l === "overlayOpen" && r.current && m.current && (r.current.srcObject = m.current);
  }, [l]), e.useEffect(() => {
    var B;
    if (l !== "overlayOpen") return;
    (B = w.current) == null || B.focus();
    const L = document.body.style.overflow;
    return document.body.style.overflow = "hidden", () => {
      document.body.style.overflow = L;
    };
  }, [l]), e.useEffect(() => {
    if (l !== "overlayOpen") return;
    const L = (B) => {
      B.key === "Escape" && N();
    };
    return document.addEventListener("keydown", L), () => document.removeEventListener("keydown", L);
  }, [l, N]), e.useEffect(() => () => {
    m.current && (m.current.getTracks().forEach((L) => L.stop()), m.current = null);
  }, []);
  const _ = oe(Xt), C = l === "uploading" ? _["js.uploading"] : _["js.photoCapture.open"], E = ["tlPhotoCapture__cameraBtn"];
  l === "uploading" && E.push("tlPhotoCapture__cameraBtn--uploading");
  const V = ["tlPhotoCapture__overlayVideo"];
  u && V.push("tlPhotoCapture__overlayVideo--mirrored");
  const T = ["tlPhotoCapture__mirrorBtn"];
  return u && T.push("tlPhotoCapture__mirrorBtn--active"), /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoCapture" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__controls" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: E.join(" "),
      onClick: k,
      disabled: l === "uploading",
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__cameraIcon" })
  )), !v && /* @__PURE__ */ e.createElement(
    "input",
    {
      ref: f,
      type: "file",
      accept: "image/*",
      capture: "environment",
      hidden: !0,
      onChange: I
    }
  ), /* @__PURE__ */ e.createElement("canvas", { ref: p, style: { display: "none" } }), l === "overlayOpen" && /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: w,
      className: "tlPhotoCapture__overlay",
      role: "dialog",
      "aria-modal": "true",
      tabIndex: -1
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayBackdrop", onClick: N }),
    /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayContent" }, /* @__PURE__ */ e.createElement(
      "video",
      {
        ref: r,
        className: V.join(" "),
        autoPlay: !0,
        muted: !0,
        playsInline: !0
      }
    ), /* @__PURE__ */ e.createElement("div", { className: "tlPhotoCapture__overlayToolbar" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: T.join(" "),
        onClick: () => s((L) => !L),
        title: _["js.photoCapture.mirror"],
        "aria-label": _["js.photoCapture.mirror"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("polyline", { points: "7 8 3 12 7 16" }), /* @__PURE__ */ e.createElement("polyline", { points: "17 8 21 12 17 16" }), /* @__PURE__ */ e.createElement("line", { x1: "12", y1: "3", x2: "12", y2: "21", strokeDasharray: "2 2" }))
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCaptureBtn",
        onClick: M,
        title: _["js.photoCapture.capture"],
        "aria-label": _["js.photoCapture.capture"]
      },
      /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__overlayCaptureIcon" })
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPhotoCapture__overlayCloseBtn",
        onClick: N,
        title: _["js.photoCapture.close"],
        "aria-label": _["js.photoCapture.close"]
      },
      /* @__PURE__ */ e.createElement("svg", { width: "20", height: "20", viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: "2", strokeLinecap: "round", strokeLinejoin: "round" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "6", x2: "18", y2: "18" }), /* @__PURE__ */ e.createElement("line", { x1: "18", y1: "6", x2: "6", y2: "18" }))
    )))
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, _[i]), b && /* @__PURE__ */ e.createElement("span", { className: "tlPhotoCapture__status tlPhotoCapture__status--error" }, b));
}, Zt = {
  "js.photoViewer.alt": "Captured photo"
}, Qt = ({ controlId: n }) => {
  const t = G(), a = Ge(), l = !!t.hasPhoto, c = t.dataRevision ?? 0, [i, o] = e.useState(null), u = e.useRef(c);
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
  const s = oe(Zt);
  return !l || !i ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement("div", { className: "tlPhotoViewer__placeholder" })) : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlPhotoViewer" }, /* @__PURE__ */ e.createElement(
    "img",
    {
      className: "tlPhotoViewer__image",
      src: i,
      alt: s["js.photoViewer.alt"]
    }
  ));
}, { useCallback: Je, useRef: Me } = e, Jt = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.orientation, c = t.resizable === !0, i = t.children ?? [], o = l === "horizontal", u = i.length > 0 && i.every((h) => h.collapsed), s = !u && i.some((h) => h.collapsed), r = u ? !o : o, m = Me(null), p = Me(null), f = Me(null), w = Je((h, N) => {
    const k = {
      overflow: h.scrolling || "auto"
    };
    return h.collapsed ? u && !r ? k.flex = "1 0 0%" : k.flex = "0 0 auto" : N !== void 0 ? k.flex = `0 0 ${N}px` : h.unit === "%" || s ? k.flex = `${h.size} 0 0%` : k.flex = `0 0 ${h.size}px`, h.minSize > 0 && !h.collapsed && (k.minWidth = o ? h.minSize : void 0, k.minHeight = o ? void 0 : h.minSize), k;
  }, [o, u, s, r]), b = Je((h, N) => {
    h.preventDefault();
    const k = m.current;
    if (!k) return;
    const M = i[N], I = i[N + 1], _ = k.querySelectorAll(":scope > .tlSplitPanel__child"), C = [];
    _.forEach((T) => {
      C.push(o ? T.offsetWidth : T.offsetHeight);
    }), f.current = C, p.current = {
      splitterIndex: N,
      startPos: o ? h.clientX : h.clientY,
      startSizeBefore: C[N],
      startSizeAfter: C[N + 1],
      childBefore: M,
      childAfter: I
    };
    const E = (T) => {
      const L = p.current;
      if (!L || !f.current) return;
      const R = (o ? T.clientX : T.clientY) - L.startPos, j = L.childBefore.minSize || 0, P = L.childAfter.minSize || 0;
      let te = L.startSizeBefore + R, Z = L.startSizeAfter - R;
      te < j && (Z += te - j, te = j), Z < P && (te += Z - P, Z = P), f.current[L.splitterIndex] = te, f.current[L.splitterIndex + 1] = Z;
      const re = k.querySelectorAll(":scope > .tlSplitPanel__child"), $ = re[L.splitterIndex], O = re[L.splitterIndex + 1];
      $ && ($.style.flex = `0 0 ${te}px`), O && (O.style.flex = `0 0 ${Z}px`);
    }, V = () => {
      if (document.removeEventListener("mousemove", E), document.removeEventListener("mouseup", V), document.body.style.cursor = "", document.body.style.userSelect = "", f.current) {
        const T = {};
        i.forEach((L, B) => {
          const R = L.control;
          R != null && R.controlId && f.current && (T[R.controlId] = f.current[B]);
        }), a("updateSizes", { sizes: T });
      }
      f.current = null, p.current = null;
    };
    document.addEventListener("mousemove", E), document.addEventListener("mouseup", V), document.body.style.cursor = o ? "col-resize" : "row-resize", document.body.style.userSelect = "none";
  }, [i, o, a]), v = [];
  return i.forEach((h, N) => {
    if (v.push(
      /* @__PURE__ */ e.createElement(
        "div",
        {
          key: `child-${N}`,
          className: `tlSplitPanel__child${h.collapsed && r ? " tlSplitPanel__child--collapsedHorizontal" : ""}`,
          style: w(h)
        },
        /* @__PURE__ */ e.createElement(Y, { control: h.control })
      )
    ), c && N < i.length - 1) {
      const k = i[N + 1];
      !h.collapsed && !k.collapsed && v.push(
        /* @__PURE__ */ e.createElement(
          "div",
          {
            key: `splitter-${N}`,
            className: `tlSplitPanel__splitter tlSplitPanel__splitter--${l}`,
            onMouseDown: (I) => b(I, N)
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
    v
  );
}, { useCallback: Ae } = e, en = {
  "js.panel.minimize": "Minimize",
  "js.panel.maximize": "Maximize",
  "js.panel.restore": "Restore",
  "js.panel.popOut": "Pop out"
}, tn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("line", { x1: "6", y1: "12", x2: "18", y2: "12" })), nn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "6", y: "9", width: "12", height: "10", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "9,7 12,4 15,7" })), ln = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "4", width: "16", height: "16", rx: "1" })), rn = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("rect", { x: "4", y: "8", width: "12", height: "12", rx: "1" }), /* @__PURE__ */ e.createElement("polyline", { points: "8,8 8,4 20,4 20,16 16,16" })), an = () => /* @__PURE__ */ e.createElement("svg", { viewBox: "0 0 24 24" }, /* @__PURE__ */ e.createElement("polyline", { points: "15,3 21,3 21,9" }), /* @__PURE__ */ e.createElement("line", { x1: "21", y1: "3", x2: "12", y2: "12" }), /* @__PURE__ */ e.createElement("path", { d: "M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" })), on = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(en), c = t.title, i = t.expansionState ?? "NORMALIZED", o = t.showMinimize === !0, u = t.showMaximize === !0, s = t.showPopOut === !0, r = t.fullLine === !0, m = t.toolbarButtons ?? [], p = i === "MINIMIZED", f = i === "MAXIMIZED", w = i === "HIDDEN", b = Ae(() => {
    a("toggleMinimize");
  }, [a]), v = Ae(() => {
    a("toggleMaximize");
  }, [a]), h = Ae(() => {
    a("popOut");
  }, [a]);
  if (w)
    return null;
  const N = f ? { position: "absolute", inset: 0, zIndex: 10, display: "flex", flexDirection: "column" } : { display: "flex", flexDirection: "column", width: "100%", height: "100%" };
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlPanel tlPanel--${i.toLowerCase()}${r ? " tlPanel--fullLine" : ""}`,
      style: N
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlPanel__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlPanel__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlPanel__toolbar" }, m.map((k, M) => /* @__PURE__ */ e.createElement("span", { key: M, className: "tlPanel__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: k }))), o && !f && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: b,
        title: p ? l["js.panel.restore"] : l["js.panel.minimize"]
      },
      p ? /* @__PURE__ */ e.createElement(nn, null) : /* @__PURE__ */ e.createElement(tn, null)
    ), u && !p && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: v,
        title: f ? l["js.panel.restore"] : l["js.panel.maximize"]
      },
      f ? /* @__PURE__ */ e.createElement(rn, null) : /* @__PURE__ */ e.createElement(ln, null)
    ), s && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlPanel__actionButton",
        onClick: h,
        title: l["js.panel.popOut"]
      },
      /* @__PURE__ */ e.createElement(an, null)
    ))),
    !p && /* @__PURE__ */ e.createElement("div", { className: "tlPanel__content" }, /* @__PURE__ */ e.createElement(Y, { control: t.child }))
  );
}, sn = ({ controlId: n }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlMaximizeRoot${t.maximized === !0 ? " tlMaximizeRoot--maximized" : ""}`,
      style: { position: "relative", width: "100%", height: "100%", overflow: "hidden" }
    },
    /* @__PURE__ */ e.createElement(Y, { control: t.child })
  );
}, cn = ({ controlId: n }) => {
  const t = G();
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDeckPane", style: { width: "100%", height: "100%" } }, t.activeChild && /* @__PURE__ */ e.createElement(Y, { control: t.activeChild }));
}, { useCallback: ue, useState: De, useEffect: Ie, useRef: je } = e, un = {
  "js.sidebar.ariaLabel": "Sidebar navigation",
  "js.sidebar.expand": "Expand sidebar",
  "js.sidebar.collapse": "Collapse sidebar"
};
function Ue(n, t, a, l) {
  const c = [];
  for (const i of n)
    i.type === "nav" ? c.push({ id: i.id, type: "nav", groupId: l }) : i.type === "command" ? c.push({ id: i.id, type: "command", groupId: l }) : i.type === "group" && (c.push({ id: i.id, type: "group" }), (a.get(i.id) ?? i.expanded) && !t && c.push(...Ue(i.children, t, a, i.id)));
  return c;
}
const Ee = ({ icon: n }) => n ? /* @__PURE__ */ e.createElement("i", { className: "tlSidebar__icon " + n, "aria-hidden": "true" }) : null, dn = ({ item: n, active: t, collapsed: a, onSelect: l, tabIndex: c, itemRef: i, onFocus: o }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__navItem" + (t ? " tlSidebar__navItem--active" : ""),
    onClick: () => l(n.id),
    title: a ? n.label : void 0,
    tabIndex: c,
    ref: i,
    onFocus: () => o(n.id)
  },
  a && n.badge ? /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__iconWrap" }, /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }), /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge tlSidebar__badge--collapsed" }, n.badge)) : /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
  !a && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label),
  !a && n.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, n.badge)
), mn = ({ item: n, collapsed: t, onExecute: a, tabIndex: l, itemRef: c, onFocus: i }) => /* @__PURE__ */ e.createElement(
  "button",
  {
    className: "tlSidebar__item tlSidebar__commandItem",
    onClick: () => a(n.id),
    title: t ? n.label : void 0,
    tabIndex: l,
    ref: c,
    onFocus: () => i(n.id)
  },
  /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
  !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)
), pn = ({ item: n, collapsed: t }) => t && !n.icon ? null : /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerItem", title: t ? n.label : void 0 }, /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }), !t && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, n.label)), fn = () => /* @__PURE__ */ e.createElement("hr", { className: "tlSidebar__separator" }), hn = ({ item: n, activeItemId: t, anchorRect: a, onSelect: l, onExecute: c, onClose: i }) => {
  const o = je(null);
  Ie(() => {
    const r = (m) => {
      o.current && !o.current.contains(m.target) && setTimeout(() => i(), 0);
    };
    return document.addEventListener("mousedown", r), () => document.removeEventListener("mousedown", r);
  }, [i]), Ie(() => {
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
        /* @__PURE__ */ e.createElement(Ee, { icon: r.icon }),
        /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__label" }, r.label),
        r.type === "nav" && r.badge && /* @__PURE__ */ e.createElement("span", { className: "tlSidebar__badge" }, r.badge)
      );
    }
    return r.type === "header" ? /* @__PURE__ */ e.createElement("div", { key: r.id, className: "tlSidebar__flyoutSectionHeader" }, r.label) : r.type === "separator" ? /* @__PURE__ */ e.createElement("hr", { key: r.id, className: "tlSidebar__separator" }) : null;
  }));
}, _n = ({
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
  onItemFocus: f,
  flyoutGroupId: w,
  onOpenFlyout: b,
  onCloseFlyout: v
}) => {
  const h = je(null), [N, k] = De(null), M = ue(() => {
    l ? w === n.id ? v() : (h.current && k(h.current.getBoundingClientRect()), b(n.id)) : o(n.id);
  }, [l, w, n.id, o, b, v]), I = ue((C) => {
    h.current = C, s(C);
  }, [s]), _ = l && w === n.id;
  return /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__group" + (_ ? " tlSidebar__group--flyoutOpen" : "") }, /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__item tlSidebar__groupHeader",
      onClick: M,
      title: l ? n.label : void 0,
      "aria-expanded": l ? _ : t,
      tabIndex: u,
      ref: I,
      onFocus: () => r(n.id)
    },
    /* @__PURE__ */ e.createElement(Ee, { icon: n.icon }),
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
  ), _ && /* @__PURE__ */ e.createElement(
    hn,
    {
      item: n,
      activeItemId: a,
      anchorRect: N,
      onSelect: c,
      onExecute: i,
      onClose: v
    }
  ), t && !l && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__groupChildren" }, n.children.map((C) => /* @__PURE__ */ e.createElement(
    pt,
    {
      key: C.id,
      item: C,
      activeItemId: a,
      collapsed: l,
      onSelect: c,
      onExecute: i,
      onToggleGroup: o,
      focusedId: m,
      setItemRef: p,
      onItemFocus: f,
      groupStates: null,
      flyoutGroupId: w,
      onOpenFlyout: b,
      onCloseFlyout: v
    }
  ))));
}, pt = ({
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
  onCloseFlyout: f
}) => {
  switch (n.type) {
    case "nav":
      return /* @__PURE__ */ e.createElement(
        dn,
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
        mn,
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
      return /* @__PURE__ */ e.createElement(pn, { item: n, collapsed: a });
    case "separator":
      return /* @__PURE__ */ e.createElement(fn, null);
    case "group": {
      const w = r ? r.get(n.id) ?? n.expanded : n.expanded;
      return /* @__PURE__ */ e.createElement(
        _n,
        {
          item: n,
          expanded: w,
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
          onCloseFlyout: f
        }
      );
    }
    default:
      return null;
  }
}, bn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(un), c = t.items ?? [], i = t.activeItemId, o = t.collapsed, [u, s] = De(() => {
    const T = /* @__PURE__ */ new Map(), L = (B) => {
      for (const R of B)
        R.type === "group" && (T.set(R.id, R.expanded), L(R.children));
    };
    return L(c), T;
  }), r = ue((T) => {
    s((L) => {
      const B = new Map(L), R = B.get(T) ?? !1;
      return B.set(T, !R), a("toggleGroup", { itemId: T, expanded: !R }), B;
    });
  }, [a]), m = ue((T) => {
    T !== i && a("selectItem", { itemId: T });
  }, [a, i]), p = ue((T) => {
    a("executeCommand", { itemId: T });
  }, [a]), f = ue(() => {
    a("toggleCollapse", {});
  }, [a]), [w, b] = De(null), v = ue((T) => {
    b(T);
  }, []), h = ue(() => {
    b(null);
  }, []);
  Ie(() => {
    o || b(null);
  }, [o]);
  const [N, k] = De(() => {
    const T = Ue(c, o, u);
    return T.length > 0 ? T[0].id : "";
  }), M = je(/* @__PURE__ */ new Map()), I = ue((T) => (L) => {
    L ? M.current.set(T, L) : M.current.delete(T);
  }, []), _ = ue((T) => {
    k(T);
  }, []), C = je(0), E = ue((T) => {
    k(T), C.current++;
  }, []);
  Ie(() => {
    const T = M.current.get(N);
    T && document.activeElement !== T && T.focus();
  }, [N, C.current]);
  const V = ue((T) => {
    if (T.key === "Escape" && w !== null) {
      T.preventDefault(), h();
      return;
    }
    const L = Ue(c, o, u);
    if (L.length === 0) return;
    const B = L.findIndex((j) => j.id === N);
    if (B < 0) return;
    const R = L[B];
    switch (T.key) {
      case "ArrowDown": {
        T.preventDefault();
        const j = (B + 1) % L.length;
        E(L[j].id);
        break;
      }
      case "ArrowUp": {
        T.preventDefault();
        const j = (B - 1 + L.length) % L.length;
        E(L[j].id);
        break;
      }
      case "Home": {
        T.preventDefault(), E(L[0].id);
        break;
      }
      case "End": {
        T.preventDefault(), E(L[L.length - 1].id);
        break;
      }
      case "Enter":
      case " ": {
        T.preventDefault(), R.type === "nav" ? m(R.id) : R.type === "command" ? p(R.id) : R.type === "group" && (o ? w === R.id ? h() : v(R.id) : r(R.id));
        break;
      }
      case "ArrowRight": {
        R.type === "group" && !o && ((u.get(R.id) ?? !1) || (T.preventDefault(), r(R.id)));
        break;
      }
      case "ArrowLeft": {
        R.type === "group" && !o && (u.get(R.id) ?? !1) && (T.preventDefault(), r(R.id));
        break;
      }
    }
  }, [
    c,
    o,
    u,
    N,
    w,
    E,
    m,
    p,
    r,
    v,
    h
  ]);
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlSidebar" + (o ? " tlSidebar--collapsed" : "") }, /* @__PURE__ */ e.createElement("nav", { className: "tlSidebar__nav", "aria-label": l["js.sidebar.ariaLabel"] }, o ? t.headerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot tlSidebar__headerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerCollapsedContent })) : t.headerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__headerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.headerContent })), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__items", onKeyDown: V }, c.map((T) => /* @__PURE__ */ e.createElement(
    pt,
    {
      key: T.id,
      item: T,
      activeItemId: i,
      collapsed: o,
      onSelect: m,
      onExecute: p,
      onToggleGroup: r,
      focusedId: N,
      setItemRef: I,
      onItemFocus: _,
      groupStates: u,
      flyoutGroupId: w,
      onOpenFlyout: v,
      onCloseFlyout: h
    }
  ))), o ? t.footerCollapsedContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot tlSidebar__footerSlot--collapsed" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerCollapsedContent })) : t.footerContent && /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__footerSlot" }, /* @__PURE__ */ e.createElement(Y, { control: t.footerContent })), /* @__PURE__ */ e.createElement(
    "button",
    {
      className: "tlSidebar__collapseBtn",
      onClick: f,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlSidebar__content" }, t.activeContent && /* @__PURE__ */ e.createElement(Y, { control: t.activeContent })));
}, vn = ({ controlId: n }) => {
  const t = G(), a = t.direction ?? "column", l = t.gap ?? "default", c = t.align ?? "stretch", i = t.wrap === !0, o = t.children ?? [], u = [
    "tlStack",
    `tlStack--${a}`,
    `tlStack--gap-${l}`,
    `tlStack--align-${c}`,
    i ? "tlStack--wrap" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: u }, o.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })));
}, En = ({ controlId: n }) => {
  const t = G(), a = t.columns, l = t.minColumnWidth, c = t.gap ?? "default", i = t.children ?? [], o = {};
  return l ? o.gridTemplateColumns = `repeat(auto-fit, minmax(${l}, 1fr))` : a && (o.gridTemplateColumns = `repeat(${a}, 1fr)`), /* @__PURE__ */ e.createElement("div", { id: n, className: `tlGrid tlGrid--gap-${c}`, style: o }, i.map((u, s) => /* @__PURE__ */ e.createElement(Y, { key: s, control: u })));
}, gn = ({ controlId: n }) => {
  const t = G(), a = t.title, l = t.variant ?? "outlined", c = t.padding ?? "default", i = t.headerActions ?? [], o = t.child, u = a != null || i.length > 0;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: `tlCard tlCard--${l}` }, u && /* @__PURE__ */ e.createElement("div", { className: "tlCard__header" }, a && /* @__PURE__ */ e.createElement("span", { className: "tlCard__title" }, a), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlCard__headerActions" }, i.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s })))), /* @__PURE__ */ e.createElement("div", { className: `tlCard__body tlCard__body--pad-${c}` }, /* @__PURE__ */ e.createElement(Y, { control: o })));
}, Cn = ({ controlId: n }) => {
  const t = G(), a = t.title ?? "", l = t.leading, c = t.actions ?? [], i = t.variant ?? "flat", u = [
    "tlAppBar",
    `tlAppBar--${t.color ?? "primary"}`,
    i === "elevated" ? "tlAppBar--elevated" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("header", { id: n, className: u }, l && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__leading" }, /* @__PURE__ */ e.createElement(Y, { control: l })), /* @__PURE__ */ e.createElement("h1", { className: "tlAppBar__title" }, a), c.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlAppBar__actions" }, c.map((s, r) => /* @__PURE__ */ e.createElement(Y, { key: r, control: s }))));
}, { useCallback: yn } = e, wn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.items ?? [], c = yn((i) => {
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
}, { useCallback: kn } = e, Sn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.items ?? [], c = t.activeItemId, i = kn((o) => {
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
}, { useCallback: et, useEffect: tt, useRef: Nn } = e, Tn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.open === !0, c = t.closeOnBackdrop !== !1, i = t.child, o = Nn(null), u = et(() => {
    a("close");
  }, [a]), s = et((r) => {
    c && r.target === r.currentTarget && u();
  }, [c, u]);
  return tt(() => {
    if (!l) return;
    const r = (m) => {
      m.key === "Escape" && u();
    };
    return document.addEventListener("keydown", r), () => document.removeEventListener("keydown", r);
  }, [l, u]), tt(() => {
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
    /* @__PURE__ */ e.createElement(Y, { control: i })
  ) : null;
}, { useEffect: Rn, useRef: Ln } = e, xn = ({ controlId: n }) => {
  const a = G().dialogs ?? [], l = Ln(a.length);
  return Rn(() => {
    a.length < l.current && a.length > 0, l.current = a.length;
  }, [a.length]), a.length === 0 ? null : /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDialogManager" }, a.map((c) => /* @__PURE__ */ e.createElement(Y, { key: c.controlId, control: c })));
}, { useCallback: nt, useRef: Ne, useState: lt } = e, Dn = {
  "js.window.close": "Close"
}, In = ["n", "ne", "e", "se", "s", "sw", "w", "nw"], jn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(Dn), c = t.title ?? "", i = t.width ?? "32rem", o = t.height ?? null, u = t.resizable === !0, s = t.child, r = t.actions ?? [], m = t.toolbarButtons ?? [], [p, f] = lt(null), [w, b] = lt(null), v = Ne(null), h = Ne(null), N = Ne(null), k = Ne(null), M = nt(() => {
    a("close");
  }, [a]), I = nt((E, V) => {
    V.preventDefault();
    const T = N.current;
    if (!T) return;
    const L = T.getBoundingClientRect();
    k.current = {
      dir: E,
      startX: V.clientX,
      startY: V.clientY,
      startW: L.width,
      startH: L.height
    };
    const B = (j) => {
      const P = k.current;
      if (!P) return;
      const te = j.clientX - P.startX, Z = j.clientY - P.startY;
      let re = P.startW, $ = P.startH;
      P.dir.includes("e") && (re = P.startW + te), P.dir.includes("w") && (re = P.startW - te), P.dir.includes("s") && ($ = P.startH + Z), P.dir.includes("n") && ($ = P.startH - Z);
      const O = Math.max(200, re), X = Math.max(100, $);
      v.current = O, h.current = X, f(O), b(X);
    }, R = () => {
      document.removeEventListener("mousemove", B), document.removeEventListener("mouseup", R);
      const j = v.current, P = h.current;
      (j != null || P != null) && (a("resize", {
        ...j != null ? { width: Math.round(j) + "px" } : {},
        ...P != null ? { height: Math.round(P) + "px" } : {}
      }), v.current = null, h.current = null, f(null), b(null)), k.current = null;
    };
    document.addEventListener("mousemove", B), document.addEventListener("mouseup", R);
  }, [a]), _ = {
    width: p != null ? p + "px" : i,
    ...w != null ? { height: w + "px" } : o != null ? { height: o } : {},
    maxHeight: "80vh"
  }, C = n + "-title";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: "tlWindow",
      style: _,
      ref: N,
      role: "dialog",
      "aria-modal": "true",
      "aria-labelledby": C
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__header" }, /* @__PURE__ */ e.createElement("span", { className: "tlWindow__title", id: C }, c), m.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__toolbar" }, m.map((E, V) => /* @__PURE__ */ e.createElement("span", { key: V, className: "tlWindow__toolbarButton" }, /* @__PURE__ */ e.createElement(Y, { control: E })))), /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlWindow__closeBtn",
        onClick: M,
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
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlWindow__body" }, /* @__PURE__ */ e.createElement(Y, { control: s })),
    r.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlWindow__footer" }, r.map((E, V) => /* @__PURE__ */ e.createElement(Y, { key: V, control: E }))),
    u && In.map((E) => /* @__PURE__ */ e.createElement(
      "div",
      {
        key: E,
        className: `tlWindow__resizeHandle tlWindow__resizeHandle--${E}`,
        onMouseDown: (V) => I(E, V)
      }
    ))
  );
}, { useCallback: Pn, useEffect: Mn } = e, An = {
  "js.drawer.close": "Close"
}, Bn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(An), c = t.open === !0, i = t.position ?? "right", o = t.size ?? "medium", u = t.title ?? null, s = t.child, r = Pn(() => {
    a("close");
  }, [a]);
  Mn(() => {
    if (!c) return;
    const p = (f) => {
      f.key === "Escape" && r();
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlDrawer__body" }, s && /* @__PURE__ */ e.createElement(Y, { control: s })));
}, { useCallback: rt, useEffect: On, useState: $n } = e, Fn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.message ?? "", c = t.content ?? "", i = t.variant ?? "info", o = t.action, u = t.duration ?? 5e3, s = t.visible === !0, r = t.generation ?? 0, [m, p] = $n(!1), f = rt(() => {
    p(!0), setTimeout(() => {
      a("dismiss", { generation: r }), p(!1);
    }, 200);
  }, [a, r]), w = rt(() => {
    o && a(o.commandName), f();
  }, [a, o, f]);
  return On(() => {
    if (!s || u === 0) return;
    const b = setTimeout(f, u);
    return () => clearTimeout(b);
  }, [s, u, f]), console.log("[TLSnackbar] render", { visible: s, exiting: m, generation: r, content: c, message: l }), !s && !m ? null : /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      className: `tlSnackbar tlSnackbar--${i}${m ? " tlSnackbar--exiting" : ""}`,
      role: "status",
      "aria-live": "polite"
    },
    c ? /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message", dangerouslySetInnerHTML: { __html: c } }) : /* @__PURE__ */ e.createElement("span", { className: "tlSnackbar__message" }, l),
    o && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlSnackbar__action", onClick: w }, o.label)
  );
}, { useCallback: Be, useEffect: Oe, useRef: Hn, useState: at } = e, Wn = ({ controlId: n }) => {
  const t = G(), a = ne(), l = t.open === !0, c = t.anchorId, i = t.items ?? [], o = Hn(null), [u, s] = at({ top: 0, left: 0 }), [r, m] = at(0), p = i.filter((v) => v.type === "item" && !v.disabled);
  Oe(() => {
    var _, C;
    if (!l || !c) return;
    const v = document.getElementById(c);
    if (!v) return;
    const h = v.getBoundingClientRect(), N = ((_ = o.current) == null ? void 0 : _.offsetHeight) ?? 200, k = ((C = o.current) == null ? void 0 : C.offsetWidth) ?? 200;
    let M = h.bottom + 4, I = h.left;
    M + N > window.innerHeight && (M = h.top - N - 4), I + k > window.innerWidth && (I = h.right - k), s({ top: M, left: I }), m(0);
  }, [l, c]);
  const f = Be(() => {
    a("close");
  }, [a]), w = Be((v) => {
    a("selectItem", { itemId: v });
  }, [a]);
  Oe(() => {
    if (!l) return;
    const v = (h) => {
      o.current && !o.current.contains(h.target) && f();
    };
    return document.addEventListener("mousedown", v), () => document.removeEventListener("mousedown", v);
  }, [l, f]);
  const b = Be((v) => {
    if (v.key === "Escape") {
      f();
      return;
    }
    if (v.key === "ArrowDown")
      v.preventDefault(), m((h) => (h + 1) % p.length);
    else if (v.key === "ArrowUp")
      v.preventDefault(), m((h) => (h - 1 + p.length) % p.length);
    else if (v.key === "Enter" || v.key === " ") {
      v.preventDefault();
      const h = p[r];
      h && w(h.id);
    }
  }, [f, w, p, r]);
  return Oe(() => {
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
      onKeyDown: b
    },
    i.map((v, h) => {
      if (v.type === "separator")
        return /* @__PURE__ */ e.createElement("hr", { key: h, className: "tlMenu__separator" });
      const k = p.indexOf(v) === r;
      return /* @__PURE__ */ e.createElement(
        "button",
        {
          key: v.id,
          type: "button",
          className: "tlMenu__item" + (k ? " tlMenu__item--focused" : "") + (v.disabled ? " tlMenu__item--disabled" : ""),
          role: "menuitem",
          disabled: v.disabled,
          tabIndex: k ? 0 : -1,
          onClick: () => w(v.id)
        },
        v.icon && /* @__PURE__ */ e.createElement("i", { className: "tlMenu__icon " + v.icon, "aria-hidden": "true" }),
        /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, v.label)
      );
    })
  ) : null;
}, zn = ({ controlId: n }) => {
  const t = G(), a = t.header, l = t.content, c = t.footer, i = t.snackbar, o = t.dialogManager;
  return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlAppShell" }, a && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__header" }, /* @__PURE__ */ e.createElement(Y, { control: a })), /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__content" }, /* @__PURE__ */ e.createElement(Y, { control: l })), c && /* @__PURE__ */ e.createElement("div", { className: "tlAppShell__footer" }, /* @__PURE__ */ e.createElement(Y, { control: c })), /* @__PURE__ */ e.createElement(Y, { control: i }), o && /* @__PURE__ */ e.createElement(Y, { control: o }));
}, Un = () => {
  const t = G().text ?? "";
  return /* @__PURE__ */ e.createElement("span", { className: "tlTextCell" }, t);
}, Vn = {
  "js.table.freezeUpTo": "Freeze up to here",
  "js.table.unfreezeAll": "Unfreeze all"
}, ot = 50, Kn = () => {
  const n = G(), t = ne(), a = oe(Vn), l = n.columns ?? [], c = n.totalRowCount ?? 0, i = n.rows ?? [], o = n.rowHeight ?? 36, u = n.selectionMode ?? "single", s = n.selectedCount ?? 0, r = n.frozenColumnCount ?? 0, m = n.treeMode ?? !1, p = e.useMemo(
    () => l.filter((S) => S.sortPriority && S.sortPriority > 0).length,
    [l]
  ), f = u === "multi", w = 40, b = 20, v = e.useRef(null), h = e.useRef(null), N = e.useRef(null), [k, M] = e.useState({}), I = e.useRef(null), _ = e.useRef(!1), C = e.useRef(null), [E, V] = e.useState(null), [T, L] = e.useState(null);
  e.useEffect(() => {
    I.current || M({});
  }, [l]);
  const B = e.useCallback((S) => k[S.name] ?? S.width, [k]), R = e.useMemo(() => {
    const S = [];
    let x = f && r > 0 ? w : 0;
    for (let U = 0; U < r && U < l.length; U++)
      S.push(x), x += B(l[U]);
    return S;
  }, [l, r, f, w, B]), j = c * o, P = e.useRef(null), te = e.useCallback((S, x, U) => {
    U.preventDefault(), U.stopPropagation(), I.current = { column: S, startX: U.clientX, startWidth: x };
    let Q = U.clientX, y = 0;
    const D = () => {
      const ae = I.current;
      if (!ae) return;
      const de = Math.max(ot, ae.startWidth + (Q - ae.startX) + y);
      M((_e) => ({ ..._e, [ae.column]: de }));
    }, W = () => {
      const ae = h.current, de = v.current;
      if (!ae || !I.current) return;
      const _e = ae.getBoundingClientRect(), qe = 40, Ze = 8, Et = ae.scrollLeft;
      Q > _e.right - qe ? ae.scrollLeft += Ze : Q < _e.left + qe && (ae.scrollLeft = Math.max(0, ae.scrollLeft - Ze));
      const Qe = ae.scrollLeft - Et;
      Qe !== 0 && (de && (de.scrollLeft = ae.scrollLeft), y += Qe, D()), P.current = requestAnimationFrame(W);
    };
    P.current = requestAnimationFrame(W);
    const ee = (ae) => {
      Q = ae.clientX, D();
    }, pe = (ae) => {
      document.removeEventListener("mousemove", ee), document.removeEventListener("mouseup", pe), P.current !== null && (cancelAnimationFrame(P.current), P.current = null);
      const de = I.current;
      if (de) {
        const _e = Math.max(ot, de.startWidth + (ae.clientX - de.startX) + y);
        t("columnResize", { column: de.column, width: _e }), I.current = null, _.current = !0, requestAnimationFrame(() => {
          _.current = !1;
        });
      }
    };
    document.addEventListener("mousemove", ee), document.addEventListener("mouseup", pe);
  }, [t]), Z = e.useCallback(() => {
    v.current && h.current && (v.current.scrollLeft = h.current.scrollLeft), N.current !== null && clearTimeout(N.current), N.current = window.setTimeout(() => {
      const S = h.current;
      if (!S) return;
      const x = S.scrollTop, U = Math.ceil(S.clientHeight / o), Q = Math.floor(x / o);
      t("scroll", { start: Q, count: U });
    }, 80);
  }, [t, o]), re = e.useCallback((S, x, U) => {
    if (_.current) return;
    let Q;
    !x || x === "desc" ? Q = "asc" : Q = "desc";
    const y = U.shiftKey ? "add" : "replace";
    t("sort", { column: S, direction: Q, mode: y });
  }, [t]), $ = e.useCallback((S, x) => {
    C.current = S, x.dataTransfer.effectAllowed = "move", x.dataTransfer.setData("text/plain", S);
  }, []), O = e.useCallback((S, x) => {
    if (!C.current || C.current === S) {
      V(null);
      return;
    }
    x.preventDefault(), x.dataTransfer.dropEffect = "move";
    const U = x.currentTarget.getBoundingClientRect(), Q = x.clientX < U.left + U.width / 2 ? "left" : "right";
    V({ column: S, side: Q });
  }, []), X = e.useCallback((S) => {
    S.preventDefault(), S.stopPropagation();
    const x = C.current;
    if (!x || !E) {
      C.current = null, V(null);
      return;
    }
    let U = l.findIndex((y) => y.name === E.column);
    if (U < 0) {
      C.current = null, V(null);
      return;
    }
    const Q = l.findIndex((y) => y.name === x);
    E.side === "right" && U++, Q < U && U--, t("columnReorder", { column: x, targetIndex: U }), C.current = null, V(null);
  }, [l, E, t]), d = e.useCallback(() => {
    C.current = null, V(null);
  }, []), g = e.useCallback((S, x) => {
    x.shiftKey && x.preventDefault(), t("select", {
      rowIndex: S,
      ctrlKey: x.ctrlKey || x.metaKey,
      shiftKey: x.shiftKey
    });
  }, [t]), F = e.useCallback((S, x) => {
    x.stopPropagation(), t("select", { rowIndex: S, ctrlKey: !0, shiftKey: !1 });
  }, [t]), A = e.useCallback(() => {
    const S = s === c && c > 0;
    t("selectAll", { selected: !S });
  }, [t, s, c]), K = e.useCallback((S, x, U) => {
    U.stopPropagation(), t("expand", { rowIndex: S, expanded: x });
  }, [t]), q = e.useCallback((S, x) => {
    x.preventDefault(), L({ x: x.clientX, y: x.clientY, colIdx: S });
  }, []), J = e.useCallback(() => {
    T && (t("setFrozenColumnCount", { count: T.colIdx + 1 }), L(null));
  }, [T, t]), ie = e.useCallback(() => {
    t("setFrozenColumnCount", { count: 0 }), L(null);
  }, [t]);
  e.useEffect(() => {
    if (!T) return;
    const S = () => L(null), x = (U) => {
      U.key === "Escape" && L(null);
    };
    return document.addEventListener("mousedown", S), document.addEventListener("keydown", x), () => {
      document.removeEventListener("mousedown", S), document.removeEventListener("keydown", x);
    };
  }, [T]);
  const le = l.reduce((S, x) => S + B(x), 0) + (f ? w : 0), ge = s === c && c > 0, Se = s > 0 && s < c, Pe = e.useCallback((S) => {
    S && (S.indeterminate = Se);
  }, [Se]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlTableView",
      onDragOver: (S) => {
        if (!C.current) return;
        S.preventDefault();
        const x = h.current, U = v.current;
        if (!x) return;
        const Q = x.getBoundingClientRect(), y = 40, D = 8;
        S.clientX < Q.left + y ? x.scrollLeft = Math.max(0, x.scrollLeft - D) : S.clientX > Q.right - y && (x.scrollLeft += D), U && (U.scrollLeft = x.scrollLeft);
      },
      onDrop: X
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlTableView__header", ref: v }, /* @__PURE__ */ e.createElement("div", { className: "tlTableView__headerRow", style: { width: le } }, f && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlTableView__headerCell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__headerCell--frozen" : ""),
        style: {
          width: w,
          minWidth: w,
          ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
        },
        onDragOver: (S) => {
          C.current && (S.preventDefault(), S.dataTransfer.dropEffect = "move", l.length > 0 && l[0].name !== C.current && V({ column: l[0].name, side: "left" }));
        }
      },
      /* @__PURE__ */ e.createElement(
        "input",
        {
          type: "checkbox",
          ref: Pe,
          className: "tlTableView__checkbox",
          checked: ge,
          onChange: A
        }
      )
    ), l.map((S, x) => {
      const U = B(S);
      l.length - 1;
      let Q = "tlTableView__headerCell";
      S.sortable && (Q += " tlTableView__headerCell--sortable"), E && E.column === S.name && (Q += " tlTableView__headerCell--dragOver-" + E.side);
      const y = x < r, D = x === r - 1;
      return y && (Q += " tlTableView__headerCell--frozen"), D && (Q += " tlTableView__headerCell--frozenLast"), /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.name,
          className: Q,
          style: {
            width: U,
            minWidth: U,
            position: y ? "sticky" : "relative",
            ...y ? { left: R[x], zIndex: 2 } : {}
          },
          draggable: !0,
          onClick: S.sortable ? (W) => re(S.name, S.sortDirection, W) : void 0,
          onContextMenu: (W) => q(x, W),
          onDragStart: (W) => $(S.name, W),
          onDragOver: (W) => O(S.name, W),
          onDrop: X,
          onDragEnd: d
        },
        /* @__PURE__ */ e.createElement("span", { className: "tlTableView__headerLabel" }, S.label),
        S.sortDirection && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortIndicator" }, S.sortDirection === "asc" ? "▲" : "▼", p > 1 && S.sortPriority != null && S.sortPriority > 0 && /* @__PURE__ */ e.createElement("span", { className: "tlTableView__sortPriority" }, S.sortPriority)),
        /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__resizeHandle",
            onMouseDown: (W) => te(S.name, U, W)
          }
        )
      );
    }), /* @__PURE__ */ e.createElement(
      "div",
      {
        style: { flex: "0 0 0", minHeight: "100%" },
        onDragOver: (S) => {
          if (C.current && l.length > 0) {
            const x = l[l.length - 1];
            x.name !== C.current && (S.preventDefault(), S.dataTransfer.dropEffect = "move", V({ column: x.name, side: "right" }));
          }
        },
        onDrop: X
      }
    ))),
    /* @__PURE__ */ e.createElement(
      "div",
      {
        ref: h,
        className: "tlTableView__body",
        onScroll: Z
      },
      /* @__PURE__ */ e.createElement("div", { style: { height: j, position: "relative", width: le } }, i.map((S) => /* @__PURE__ */ e.createElement(
        "div",
        {
          key: S.id,
          className: "tlTableView__row" + (S.selected ? " tlTableView__row--selected" : ""),
          style: {
            position: "absolute",
            top: S.index * o,
            height: o,
            width: le
          },
          onClick: (x) => g(S.index, x)
        },
        f && /* @__PURE__ */ e.createElement(
          "div",
          {
            className: "tlTableView__cell tlTableView__checkboxCell" + (r > 0 ? " tlTableView__cell--frozen" : ""),
            style: {
              width: w,
              minWidth: w,
              ...r > 0 ? { position: "sticky", left: 0, zIndex: 2 } : {}
            },
            onClick: (x) => x.stopPropagation()
          },
          /* @__PURE__ */ e.createElement(
            "input",
            {
              type: "checkbox",
              className: "tlTableView__checkbox",
              checked: S.selected,
              onChange: () => {
              },
              onClick: (x) => F(S.index, x),
              tabIndex: -1
            }
          )
        ),
        l.map((x, U) => {
          const Q = B(x), y = U === l.length - 1, D = U < r, W = U === r - 1;
          let ee = "tlTableView__cell";
          D && (ee += " tlTableView__cell--frozen"), W && (ee += " tlTableView__cell--frozenLast");
          const pe = m && U === 0, ae = S.treeDepth ?? 0;
          return /* @__PURE__ */ e.createElement(
            "div",
            {
              key: x.name,
              className: ee,
              style: {
                ...y && !D ? { flex: "1 0 auto", minWidth: Q } : { width: Q, minWidth: Q },
                ...D ? { position: "sticky", left: R[U], zIndex: 2 } : {}
              }
            },
            pe ? /* @__PURE__ */ e.createElement("div", { className: "tlTableView__treeCell", style: { paddingLeft: ae * b } }, S.expandable ? /* @__PURE__ */ e.createElement(
              "button",
              {
                className: "tlTableView__treeToggle",
                onClick: (de) => K(S.index, !S.expanded, de)
              },
              S.expanded ? "▾" : "▸"
            ) : /* @__PURE__ */ e.createElement("span", { className: "tlTableView__treeToggleSpacer" }), /* @__PURE__ */ e.createElement(Y, { control: S.cells[x.name] })) : /* @__PURE__ */ e.createElement(Y, { control: S.cells[x.name] })
          );
        })
      )))
    ),
    T && /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlMenu",
        role: "menu",
        style: { position: "fixed", top: T.y, left: T.x, zIndex: 1e4 },
        onMouseDown: (S) => S.stopPropagation()
      },
      T.colIdx + 1 !== r && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: J }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.freezeUpTo"])),
      r > 0 && /* @__PURE__ */ e.createElement("button", { type: "button", className: "tlMenu__item", role: "menuitem", onClick: ie }, /* @__PURE__ */ e.createElement("span", { className: "tlMenu__label" }, a["js.table.unfreezeAll"]))
    )
  );
}, Yn = {
  readOnly: !1,
  resolvedLabelPosition: "side"
}, ft = e.createContext(Yn), { useMemo: Gn, useRef: Xn, useState: qn, useEffect: Zn } = e, Qn = 320, Jn = ({ controlId: n }) => {
  const t = G(), a = t.maxColumns ?? 3, l = t.labelPosition ?? "auto", c = t.readOnly === !0, i = t.children ?? [], o = t.noModelMessage, u = Xn(null), [s, r] = qn(
    l === "top" ? "top" : "side"
  );
  Zn(() => {
    if (l !== "auto") {
      r(l);
      return;
    }
    const b = u.current;
    if (!b) return;
    const v = new ResizeObserver((h) => {
      for (const N of h) {
        const M = N.contentRect.width / a;
        r(M < Qn ? "top" : "side");
      }
    });
    return v.observe(b), () => v.disconnect();
  }, [l, a]);
  const m = Gn(() => ({
    readOnly: c,
    resolvedLabelPosition: s
  }), [c, s]), f = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${`${Math.max(16, Math.floor(64 / a))}rem`}, 1fr))`
  }, w = [
    "tlFormLayout",
    c ? "tlFormLayout--readonly" : ""
  ].filter(Boolean).join(" ");
  return o ? /* @__PURE__ */ e.createElement("div", { id: n, className: "tlFormLayout tlFormLayout--empty", ref: u }, /* @__PURE__ */ e.createElement("p", { className: "tlFormLayout__noModel" }, o)) : /* @__PURE__ */ e.createElement(ft.Provider, { value: m }, /* @__PURE__ */ e.createElement("div", { id: n, className: w, style: f, ref: u }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useCallback: el } = e, tl = {
  "js.formGroup.collapse": "Collapse",
  "js.formGroup.expand": "Expand"
}, nl = ({ controlId: n }) => {
  const t = G(), a = ne(), l = oe(tl), c = t.header, i = t.headerActions ?? [], o = t.collapsible === !0, u = t.collapsed === !0, s = t.border ?? "none", r = t.fullLine === !0, m = t.children ?? [], p = c != null || i.length > 0 || o, f = el(() => {
    a("toggleCollapse");
  }, [a]), w = [
    "tlFormGroup",
    `tlFormGroup--border-${s}`,
    r ? "tlFormGroup--fullLine" : "",
    u ? "tlFormGroup--collapsed" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: w }, p && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__header" }, o && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormGroup__collapseToggle",
      onClick: f,
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
  ), c && /* @__PURE__ */ e.createElement("span", { className: "tlFormGroup__title" }, c), i.length > 0 && /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__actions" }, i.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b })))), /* @__PURE__ */ e.createElement("div", { className: "tlFormGroup__body" }, m.map((b, v) => /* @__PURE__ */ e.createElement(Y, { key: v, control: b }))));
}, { useContext: ll, useState: rl, useCallback: al } = e, ol = ({ controlId: n }) => {
  const t = G(), a = ll(ft), l = t.label ?? "", c = t.required === !0, i = t.error, o = t.warnings, u = t.helpText, s = t.dirty === !0, r = t.labelPosition ?? a.resolvedLabelPosition, m = t.fullLine === !0, p = t.visible !== !1, f = t.field, w = a.readOnly, [b, v] = rl(!1), h = al(() => v((I) => !I), []);
  if (!p) return null;
  const N = i != null, k = o != null && o.length > 0, M = [
    "tlFormField",
    `tlFormField--${r}`,
    w ? "tlFormField--readonly" : "",
    m ? "tlFormField--fullLine" : "",
    N ? "tlFormField--error" : "",
    !N && k ? "tlFormField--warning" : "",
    s ? "tlFormField--dirty" : ""
  ].filter(Boolean).join(" ");
  return /* @__PURE__ */ e.createElement("div", { id: n, className: M }, /* @__PURE__ */ e.createElement("div", { className: "tlFormField__label" }, /* @__PURE__ */ e.createElement("span", { className: "tlFormField__labelText" }, l), c && !w && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__required" }, "*"), s && /* @__PURE__ */ e.createElement("span", { className: "tlFormField__dirtyDot" }), u && !w && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlFormField__helpIcon",
      onClick: h,
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
  )), /* @__PURE__ */ e.createElement("div", { className: "tlFormField__input" }, /* @__PURE__ */ e.createElement(Y, { control: f })), !w && N && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__error", role: "alert" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, i)), !w && !N && k && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__warnings", "aria-live": "polite" }, o.map((I, _) => /* @__PURE__ */ e.createElement("div", { key: _, className: "tlFormField__warning" }, /* @__PURE__ */ e.createElement(
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
  ), /* @__PURE__ */ e.createElement("span", null, I)))), !w && u && b && /* @__PURE__ */ e.createElement("div", { className: "tlFormField__helpText" }, u));
}, sl = () => {
  const n = G(), t = ne(), a = n.iconCss, l = n.iconSrc, c = n.label, i = n.cssClass, o = n.tooltip, u = n.hasLink, s = a ? /* @__PURE__ */ e.createElement("i", { className: a }) : l ? /* @__PURE__ */ e.createElement("img", { src: l, className: "tlTypeIcon", alt: "" }) : null, r = /* @__PURE__ */ e.createElement(e.Fragment, null, s, c && /* @__PURE__ */ e.createElement("span", { className: "tlResourceLabel" }, c)), m = e.useCallback((f) => {
    f.preventDefault(), t("goto", {});
  }, [t]), p = ["tlResourceCell", i].filter(Boolean).join(" ");
  return u ? /* @__PURE__ */ e.createElement("a", { className: p, href: "#", onClick: m, title: o }, r) : /* @__PURE__ */ e.createElement("span", { className: p, title: o }, r);
}, cl = 20, il = () => {
  const n = G(), t = ne(), a = n.nodes ?? [], l = n.selectionMode ?? "single", c = n.dragEnabled ?? !1, i = n.dropEnabled ?? !1, o = n.dropIndicatorNodeId ?? null, u = n.dropIndicatorPosition ?? null, [s, r] = e.useState(-1), m = e.useRef(null), p = e.useCallback((_, C) => {
    t(C ? "collapse" : "expand", { nodeId: _ });
  }, [t]), f = e.useCallback((_, C) => {
    t("select", {
      nodeId: _,
      ctrlKey: C.ctrlKey || C.metaKey,
      shiftKey: C.shiftKey
    });
  }, [t]), w = e.useCallback((_, C) => {
    C.preventDefault(), t("contextMenu", { nodeId: _, x: C.clientX, y: C.clientY });
  }, [t]), b = e.useRef(null), v = e.useCallback((_, C) => {
    const E = C.getBoundingClientRect(), V = _.clientY - E.top, T = E.height / 3;
    return V < T ? "above" : V > T * 2 ? "below" : "within";
  }, []), h = e.useCallback((_, C) => {
    C.dataTransfer.effectAllowed = "move", C.dataTransfer.setData("text/plain", _);
  }, []), N = e.useCallback((_, C) => {
    C.preventDefault(), C.dataTransfer.dropEffect = "move";
    const E = v(C, C.currentTarget);
    b.current != null && window.clearTimeout(b.current), b.current = window.setTimeout(() => {
      t("dragOver", { nodeId: _, position: E }), b.current = null;
    }, 50);
  }, [t, v]), k = e.useCallback((_, C) => {
    C.preventDefault(), b.current != null && (window.clearTimeout(b.current), b.current = null);
    const E = v(C, C.currentTarget);
    t("drop", { nodeId: _, position: E });
  }, [t, v]), M = e.useCallback(() => {
    b.current != null && (window.clearTimeout(b.current), b.current = null), t("dragEnd");
  }, [t]), I = e.useCallback((_) => {
    if (a.length === 0) return;
    let C = s;
    switch (_.key) {
      case "ArrowDown":
        _.preventDefault(), C = Math.min(s + 1, a.length - 1);
        break;
      case "ArrowUp":
        _.preventDefault(), C = Math.max(s - 1, 0);
        break;
      case "ArrowRight":
        if (_.preventDefault(), s >= 0 && s < a.length) {
          const E = a[s];
          if (E.expandable && !E.expanded) {
            t("expand", { nodeId: E.id });
            return;
          } else E.expanded && (C = s + 1);
        }
        break;
      case "ArrowLeft":
        if (_.preventDefault(), s >= 0 && s < a.length) {
          const E = a[s];
          if (E.expanded) {
            t("collapse", { nodeId: E.id });
            return;
          } else {
            const V = E.depth;
            for (let T = s - 1; T >= 0; T--)
              if (a[T].depth < V) {
                C = T;
                break;
              }
          }
        }
        break;
      case "Enter":
        _.preventDefault(), s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: _.ctrlKey || _.metaKey,
          shiftKey: _.shiftKey
        });
        return;
      case " ":
        _.preventDefault(), l === "multi" && s >= 0 && s < a.length && t("select", {
          nodeId: a[s].id,
          ctrlKey: !0,
          shiftKey: !1
        });
        return;
      case "Home":
        _.preventDefault(), C = 0;
        break;
      case "End":
        _.preventDefault(), C = a.length - 1;
        break;
      default:
        return;
    }
    C !== s && r(C);
  }, [s, a, t, l]);
  return /* @__PURE__ */ e.createElement(
    "ul",
    {
      ref: m,
      role: "tree",
      className: "tlTreeView",
      tabIndex: 0,
      onKeyDown: I
    },
    a.map((_, C) => /* @__PURE__ */ e.createElement(
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
          C === s ? "tlTreeView__node--focused" : "",
          o === _.id && u === "above" ? "tlTreeView__node--drop-above" : "",
          o === _.id && u === "within" ? "tlTreeView__node--drop-within" : "",
          o === _.id && u === "below" ? "tlTreeView__node--drop-below" : ""
        ].filter(Boolean).join(" "),
        style: { paddingLeft: _.depth * cl },
        draggable: c,
        onClick: (E) => f(_.id, E),
        onContextMenu: (E) => w(_.id, E),
        onDragStart: (E) => h(_.id, E),
        onDragOver: i ? (E) => N(_.id, E) : void 0,
        onDrop: i ? (E) => k(_.id, E) : void 0,
        onDragEnd: M
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
var $e = { exports: {} }, se = {}, Fe = { exports: {} }, z = {};
/**
 * @license React
 * react.production.js
 *
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var st;
function ul() {
  if (st) return z;
  st = 1;
  var n = Symbol.for("react.transitional.element"), t = Symbol.for("react.portal"), a = Symbol.for("react.fragment"), l = Symbol.for("react.strict_mode"), c = Symbol.for("react.profiler"), i = Symbol.for("react.consumer"), o = Symbol.for("react.context"), u = Symbol.for("react.forward_ref"), s = Symbol.for("react.suspense"), r = Symbol.for("react.memo"), m = Symbol.for("react.lazy"), p = Symbol.for("react.activity"), f = Symbol.iterator;
  function w(d) {
    return d === null || typeof d != "object" ? null : (d = f && d[f] || d["@@iterator"], typeof d == "function" ? d : null);
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
  }, v = Object.assign, h = {};
  function N(d, g, F) {
    this.props = d, this.context = g, this.refs = h, this.updater = F || b;
  }
  N.prototype.isReactComponent = {}, N.prototype.setState = function(d, g) {
    if (typeof d != "object" && typeof d != "function" && d != null)
      throw Error(
        "takes an object of state variables to update or a function which returns an object of state variables."
      );
    this.updater.enqueueSetState(this, d, g, "setState");
  }, N.prototype.forceUpdate = function(d) {
    this.updater.enqueueForceUpdate(this, d, "forceUpdate");
  };
  function k() {
  }
  k.prototype = N.prototype;
  function M(d, g, F) {
    this.props = d, this.context = g, this.refs = h, this.updater = F || b;
  }
  var I = M.prototype = new k();
  I.constructor = M, v(I, N.prototype), I.isPureReactComponent = !0;
  var _ = Array.isArray;
  function C() {
  }
  var E = { H: null, A: null, T: null, S: null }, V = Object.prototype.hasOwnProperty;
  function T(d, g, F) {
    var A = F.ref;
    return {
      $$typeof: n,
      type: d,
      key: g,
      ref: A !== void 0 ? A : null,
      props: F
    };
  }
  function L(d, g) {
    return T(d.type, g, d.props);
  }
  function B(d) {
    return typeof d == "object" && d !== null && d.$$typeof === n;
  }
  function R(d) {
    var g = { "=": "=0", ":": "=2" };
    return "$" + d.replace(/[=:]/g, function(F) {
      return g[F];
    });
  }
  var j = /\/+/g;
  function P(d, g) {
    return typeof d == "object" && d !== null && d.key != null ? R("" + d.key) : g.toString(36);
  }
  function te(d) {
    switch (d.status) {
      case "fulfilled":
        return d.value;
      case "rejected":
        throw d.reason;
      default:
        switch (typeof d.status == "string" ? d.then(C, C) : (d.status = "pending", d.then(
          function(g) {
            d.status === "pending" && (d.status = "fulfilled", d.value = g);
          },
          function(g) {
            d.status === "pending" && (d.status = "rejected", d.reason = g);
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
  function Z(d, g, F, A, K) {
    var q = typeof d;
    (q === "undefined" || q === "boolean") && (d = null);
    var J = !1;
    if (d === null) J = !0;
    else
      switch (q) {
        case "bigint":
        case "string":
        case "number":
          J = !0;
          break;
        case "object":
          switch (d.$$typeof) {
            case n:
            case t:
              J = !0;
              break;
            case m:
              return J = d._init, Z(
                J(d._payload),
                g,
                F,
                A,
                K
              );
          }
      }
    if (J)
      return K = K(d), J = A === "" ? "." + P(d, 0) : A, _(K) ? (F = "", J != null && (F = J.replace(j, "$&/") + "/"), Z(K, g, F, "", function(ge) {
        return ge;
      })) : K != null && (B(K) && (K = L(
        K,
        F + (K.key == null || d && d.key === K.key ? "" : ("" + K.key).replace(
          j,
          "$&/"
        ) + "/") + J
      )), g.push(K)), 1;
    J = 0;
    var ie = A === "" ? "." : A + ":";
    if (_(d))
      for (var le = 0; le < d.length; le++)
        A = d[le], q = ie + P(A, le), J += Z(
          A,
          g,
          F,
          q,
          K
        );
    else if (le = w(d), typeof le == "function")
      for (d = le.call(d), le = 0; !(A = d.next()).done; )
        A = A.value, q = ie + P(A, le++), J += Z(
          A,
          g,
          F,
          q,
          K
        );
    else if (q === "object") {
      if (typeof d.then == "function")
        return Z(
          te(d),
          g,
          F,
          A,
          K
        );
      throw g = String(d), Error(
        "Objects are not valid as a React child (found: " + (g === "[object Object]" ? "object with keys {" + Object.keys(d).join(", ") + "}" : g) + "). If you meant to render a collection of children, use an array instead."
      );
    }
    return J;
  }
  function re(d, g, F) {
    if (d == null) return d;
    var A = [], K = 0;
    return Z(d, A, "", "", function(q) {
      return g.call(F, q, K++);
    }), A;
  }
  function $(d) {
    if (d._status === -1) {
      var g = d._result;
      g = g(), g.then(
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 1, d._result = F);
        },
        function(F) {
          (d._status === 0 || d._status === -1) && (d._status = 2, d._result = F);
        }
      ), d._status === -1 && (d._status = 0, d._result = g);
    }
    if (d._status === 1) return d._result.default;
    throw d._result;
  }
  var O = typeof reportError == "function" ? reportError : function(d) {
    if (typeof window == "object" && typeof window.ErrorEvent == "function") {
      var g = new window.ErrorEvent("error", {
        bubbles: !0,
        cancelable: !0,
        message: typeof d == "object" && d !== null && typeof d.message == "string" ? String(d.message) : String(d),
        error: d
      });
      if (!window.dispatchEvent(g)) return;
    } else if (typeof process == "object" && typeof process.emit == "function") {
      process.emit("uncaughtException", d);
      return;
    }
    console.error(d);
  }, X = {
    map: re,
    forEach: function(d, g, F) {
      re(
        d,
        function() {
          g.apply(this, arguments);
        },
        F
      );
    },
    count: function(d) {
      var g = 0;
      return re(d, function() {
        g++;
      }), g;
    },
    toArray: function(d) {
      return re(d, function(g) {
        return g;
      }) || [];
    },
    only: function(d) {
      if (!B(d))
        throw Error(
          "React.Children.only expected to receive a single React element child."
        );
      return d;
    }
  };
  return z.Activity = p, z.Children = X, z.Component = N, z.Fragment = a, z.Profiler = c, z.PureComponent = M, z.StrictMode = l, z.Suspense = s, z.__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = E, z.__COMPILER_RUNTIME = {
    __proto__: null,
    c: function(d) {
      return E.H.useMemoCache(d);
    }
  }, z.cache = function(d) {
    return function() {
      return d.apply(null, arguments);
    };
  }, z.cacheSignal = function() {
    return null;
  }, z.cloneElement = function(d, g, F) {
    if (d == null)
      throw Error(
        "The argument must be a React element, but you passed " + d + "."
      );
    var A = v({}, d.props), K = d.key;
    if (g != null)
      for (q in g.key !== void 0 && (K = "" + g.key), g)
        !V.call(g, q) || q === "key" || q === "__self" || q === "__source" || q === "ref" && g.ref === void 0 || (A[q] = g[q]);
    var q = arguments.length - 2;
    if (q === 1) A.children = F;
    else if (1 < q) {
      for (var J = Array(q), ie = 0; ie < q; ie++)
        J[ie] = arguments[ie + 2];
      A.children = J;
    }
    return T(d.type, K, A);
  }, z.createContext = function(d) {
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
  }, z.createElement = function(d, g, F) {
    var A, K = {}, q = null;
    if (g != null)
      for (A in g.key !== void 0 && (q = "" + g.key), g)
        V.call(g, A) && A !== "key" && A !== "__self" && A !== "__source" && (K[A] = g[A]);
    var J = arguments.length - 2;
    if (J === 1) K.children = F;
    else if (1 < J) {
      for (var ie = Array(J), le = 0; le < J; le++)
        ie[le] = arguments[le + 2];
      K.children = ie;
    }
    if (d && d.defaultProps)
      for (A in J = d.defaultProps, J)
        K[A] === void 0 && (K[A] = J[A]);
    return T(d, q, K);
  }, z.createRef = function() {
    return { current: null };
  }, z.forwardRef = function(d) {
    return { $$typeof: u, render: d };
  }, z.isValidElement = B, z.lazy = function(d) {
    return {
      $$typeof: m,
      _payload: { _status: -1, _result: d },
      _init: $
    };
  }, z.memo = function(d, g) {
    return {
      $$typeof: r,
      type: d,
      compare: g === void 0 ? null : g
    };
  }, z.startTransition = function(d) {
    var g = E.T, F = {};
    E.T = F;
    try {
      var A = d(), K = E.S;
      K !== null && K(F, A), typeof A == "object" && A !== null && typeof A.then == "function" && A.then(C, O);
    } catch (q) {
      O(q);
    } finally {
      g !== null && F.types !== null && (g.types = F.types), E.T = g;
    }
  }, z.unstable_useCacheRefresh = function() {
    return E.H.useCacheRefresh();
  }, z.use = function(d) {
    return E.H.use(d);
  }, z.useActionState = function(d, g, F) {
    return E.H.useActionState(d, g, F);
  }, z.useCallback = function(d, g) {
    return E.H.useCallback(d, g);
  }, z.useContext = function(d) {
    return E.H.useContext(d);
  }, z.useDebugValue = function() {
  }, z.useDeferredValue = function(d, g) {
    return E.H.useDeferredValue(d, g);
  }, z.useEffect = function(d, g) {
    return E.H.useEffect(d, g);
  }, z.useEffectEvent = function(d) {
    return E.H.useEffectEvent(d);
  }, z.useId = function() {
    return E.H.useId();
  }, z.useImperativeHandle = function(d, g, F) {
    return E.H.useImperativeHandle(d, g, F);
  }, z.useInsertionEffect = function(d, g) {
    return E.H.useInsertionEffect(d, g);
  }, z.useLayoutEffect = function(d, g) {
    return E.H.useLayoutEffect(d, g);
  }, z.useMemo = function(d, g) {
    return E.H.useMemo(d, g);
  }, z.useOptimistic = function(d, g) {
    return E.H.useOptimistic(d, g);
  }, z.useReducer = function(d, g, F) {
    return E.H.useReducer(d, g, F);
  }, z.useRef = function(d) {
    return E.H.useRef(d);
  }, z.useState = function(d) {
    return E.H.useState(d);
  }, z.useSyncExternalStore = function(d, g, F) {
    return E.H.useSyncExternalStore(
      d,
      g,
      F
    );
  }, z.useTransition = function() {
    return E.H.useTransition();
  }, z.version = "19.2.4", z;
}
var ct;
function dl() {
  return ct || (ct = 1, Fe.exports = ul()), Fe.exports;
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
var it;
function ml() {
  if (it) return se;
  it = 1;
  var n = dl();
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
  return se.__DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE = l, se.createPortal = function(s, r) {
    var m = 2 < arguments.length && arguments[2] !== void 0 ? arguments[2] : null;
    if (!r || r.nodeType !== 1 && r.nodeType !== 9 && r.nodeType !== 11)
      throw Error(t(299));
    return i(s, r, null, m);
  }, se.flushSync = function(s) {
    var r = o.T, m = l.p;
    try {
      if (o.T = null, l.p = 2, s) return s();
    } finally {
      o.T = r, l.p = m, l.d.f();
    }
  }, se.preconnect = function(s, r) {
    typeof s == "string" && (r ? (r = r.crossOrigin, r = typeof r == "string" ? r === "use-credentials" ? r : "" : void 0) : r = null, l.d.C(s, r));
  }, se.prefetchDNS = function(s) {
    typeof s == "string" && l.d.D(s);
  }, se.preinit = function(s, r) {
    if (typeof s == "string" && r && typeof r.as == "string") {
      var m = r.as, p = u(m, r.crossOrigin), f = typeof r.integrity == "string" ? r.integrity : void 0, w = typeof r.fetchPriority == "string" ? r.fetchPriority : void 0;
      m === "style" ? l.d.S(
        s,
        typeof r.precedence == "string" ? r.precedence : void 0,
        {
          crossOrigin: p,
          integrity: f,
          fetchPriority: w
        }
      ) : m === "script" && l.d.X(s, {
        crossOrigin: p,
        integrity: f,
        fetchPriority: w,
        nonce: typeof r.nonce == "string" ? r.nonce : void 0
      });
    }
  }, se.preinitModule = function(s, r) {
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
  }, se.preload = function(s, r) {
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
  }, se.preloadModule = function(s, r) {
    if (typeof s == "string")
      if (r) {
        var m = u(r.as, r.crossOrigin);
        l.d.m(s, {
          as: typeof r.as == "string" && r.as !== "script" ? r.as : void 0,
          crossOrigin: m,
          integrity: typeof r.integrity == "string" ? r.integrity : void 0
        });
      } else l.d.m(s);
  }, se.requestFormReset = function(s) {
    l.d.r(s);
  }, se.unstable_batchedUpdates = function(s, r) {
    return s(r);
  }, se.useFormState = function(s, r, m) {
    return o.H.useFormState(s, r, m);
  }, se.useFormStatus = function() {
    return o.H.useHostTransitionStatus();
  }, se.version = "19.2.4", se;
}
var ut;
function pl() {
  if (ut) return $e.exports;
  ut = 1;
  function n() {
    if (!(typeof __REACT_DEVTOOLS_GLOBAL_HOOK__ > "u" || typeof __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE != "function"))
      try {
        __REACT_DEVTOOLS_GLOBAL_HOOK__.checkDCE(n);
      } catch (t) {
        console.error(t);
      }
  }
  return n(), $e.exports = ml(), $e.exports;
}
var fl = pl();
const { useState: fe, useCallback: ce, useRef: Ce, useEffect: be, useMemo: Ve } = e;
function Xe({ image: n }) {
  if (!n) return null;
  if (n.startsWith("/"))
    return /* @__PURE__ */ e.createElement("img", { src: n, alt: "", className: "tlDropdownSelect__optionImage" });
  const t = n.startsWith("css:") ? n.substring(4) : n.startsWith("colored:") ? n.substring(8) : n;
  return /* @__PURE__ */ e.createElement("span", { className: `tlDropdownSelect__optionIcon ${t}` });
}
function hl({
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
  const m = ce(
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
    /* @__PURE__ */ e.createElement(Xe, { image: n.image }),
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
function _l({
  option: n,
  highlighted: t,
  searchTerm: a,
  onSelect: l,
  onMouseEnter: c,
  id: i
}) {
  const o = ce(() => l(n.value), [l, n.value]), u = Ve(() => {
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
    /* @__PURE__ */ e.createElement(Xe, { image: n.image }),
    /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__optionLabel" }, u)
  );
}
const bl = ({ controlId: n, state: t }) => {
  const a = ne(), l = t.value ?? [], c = t.multiSelect === !0, i = t.customOrder === !0, o = t.mandatory === !0, u = t.disabled === !0, s = t.editable !== !1, r = t.optionsLoaded === !0, m = t.options ?? [], p = t.emptyOptionLabel ?? "", f = i && c && !u && s, w = oe({
    "js.dropdownSelect.nothingFound": "Nothing found",
    "js.dropdownSelect.filterPlaceholder": "Filter…",
    "js.dropdownSelect.clear": "Clear selection",
    "js.dropdownSelect.removeChip": "Remove {0}",
    "js.dropdownSelect.loading": "Loading…",
    "js.dropdownSelect.error": "Failed to load options. Retry"
  }), b = w["js.dropdownSelect.nothingFound"], v = ce(
    (y) => w["js.dropdownSelect.removeChip"].replace("{0}", y),
    [w]
  ), [h, N] = fe(!1), [k, M] = fe(""), [I, _] = fe(-1), [C, E] = fe(!1), [V, T] = fe({}), [L, B] = fe(null), [R, j] = fe(null), [P, te] = fe(null), Z = Ce(null), re = Ce(null), $ = Ce(null), O = Ce(l);
  O.current = l;
  const X = Ce(-1), d = Ve(
    () => new Set(l.map((y) => y.value)),
    [l]
  ), g = Ve(() => {
    let y = m.filter((D) => !d.has(D.value));
    if (k) {
      const D = k.toLowerCase();
      y = y.filter((W) => W.label.toLowerCase().includes(D));
    }
    return y;
  }, [m, d, k]);
  be(() => {
    k && g.length === 1 ? _(0) : _(-1);
  }, [g.length, k]), be(() => {
    h && r && re.current && re.current.focus();
  }, [h, r, l]), be(() => {
    var W, ee;
    if (X.current < 0) return;
    const y = X.current;
    X.current = -1;
    const D = (W = Z.current) == null ? void 0 : W.querySelectorAll(
      ".tlDropdownSelect__chipRemove"
    );
    D && D.length > 0 ? D[Math.min(y, D.length - 1)].focus() : (ee = Z.current) == null || ee.focus();
  }, [l]), be(() => {
    if (!h) return;
    const y = (D) => {
      Z.current && !Z.current.contains(D.target) && $.current && !$.current.contains(D.target) && (N(!1), M(""));
    };
    return document.addEventListener("mousedown", y), () => document.removeEventListener("mousedown", y);
  }, [h]), be(() => {
    if (!h || !Z.current) return;
    const y = Z.current.getBoundingClientRect(), D = window.innerHeight - y.bottom, ee = D < 300 && y.top > D;
    T({
      left: y.left,
      width: y.width,
      ...ee ? { bottom: window.innerHeight - y.top } : { top: y.bottom }
    });
  }, [h]);
  const F = ce(async () => {
    if (!(u || !s) && (N(!0), M(""), _(-1), E(!1), !r))
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
  }, [u, s, r, a]), A = ce(() => {
    var y;
    N(!1), M(""), _(-1), (y = Z.current) == null || y.focus();
  }, []), K = ce(
    (y) => {
      let D;
      if (c) {
        const W = m.find((ee) => ee.value === y);
        if (W)
          D = [...O.current, W];
        else
          return;
      } else {
        const W = m.find((ee) => ee.value === y);
        if (W)
          D = [W];
        else
          return;
      }
      O.current = D, a("valueChanged", { value: D.map((W) => W.value) }), c ? (M(""), _(-1)) : A();
    },
    [c, m, a, A]
  ), q = ce(
    (y) => {
      X.current = O.current.findIndex((W) => W.value === y);
      const D = O.current.filter((W) => W.value !== y);
      O.current = D, a("valueChanged", { value: D.map((W) => W.value) });
    },
    [a]
  ), J = ce(
    (y) => {
      y.stopPropagation(), a("valueChanged", { value: [] }), A();
    },
    [a, A]
  ), ie = ce((y) => {
    M(y.target.value);
  }, []), le = ce(
    (y) => {
      if (!h) {
        if (y.key === "ArrowDown" || y.key === "ArrowUp" || y.key === "Enter" || y.key === " ") {
          if (y.target.tagName === "BUTTON") return;
          y.preventDefault(), y.stopPropagation(), F();
        }
        return;
      }
      switch (y.key) {
        case "ArrowDown":
          y.preventDefault(), y.stopPropagation(), _(
            (D) => D < g.length - 1 ? D + 1 : 0
          );
          break;
        case "ArrowUp":
          y.preventDefault(), y.stopPropagation(), _(
            (D) => D > 0 ? D - 1 : g.length - 1
          );
          break;
        case "Enter":
          y.preventDefault(), y.stopPropagation(), I >= 0 && I < g.length && K(g[I].value);
          break;
        case "Escape":
          y.preventDefault(), y.stopPropagation(), A();
          break;
        case "Tab":
          A();
          break;
        case "Backspace":
          k === "" && c && l.length > 0 && q(l[l.length - 1].value);
          break;
      }
    },
    [
      h,
      F,
      A,
      g,
      I,
      K,
      k,
      c,
      l,
      q
    ]
  ), ge = ce(
    async (y) => {
      y.preventDefault(), E(!1);
      try {
        await a("loadOptions");
      } catch {
        E(!0);
      }
    },
    [a]
  ), Se = ce(
    (y, D) => {
      B(y), D.dataTransfer.effectAllowed = "move", D.dataTransfer.setData("text/plain", String(y));
    },
    []
  ), Pe = ce(
    (y, D) => {
      if (D.preventDefault(), D.dataTransfer.dropEffect = "move", L === null || L === y) {
        j(null), te(null);
        return;
      }
      const W = D.currentTarget.getBoundingClientRect(), ee = W.left + W.width / 2, pe = D.clientX < ee ? "before" : "after";
      j(y), te(pe);
    },
    [L]
  ), S = ce(
    (y) => {
      if (y.preventDefault(), L === null || R === null || P === null || L === R) return;
      const D = [...O.current], [W] = D.splice(L, 1);
      let ee = R;
      L < R ? ee = P === "before" ? ee - 1 : ee : ee = P === "before" ? ee : ee + 1, D.splice(ee, 0, W), O.current = D, a("valueChanged", { value: D.map((pe) => pe.value) }), B(null), j(null), te(null);
    },
    [L, R, P, a]
  ), x = ce(() => {
    B(null), j(null), te(null);
  }, []);
  if (be(() => {
    if (I < 0 || !$.current) return;
    const y = $.current.querySelector(
      `[id="${n}-opt-${I}"]`
    );
    y && y.scrollIntoView({ block: "nearest" });
  }, [I, n]), !s)
    return /* @__PURE__ */ e.createElement("div", { id: n, className: "tlDropdownSelect tlDropdownSelect--immutable" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__empty" }, p) : l.map((y) => /* @__PURE__ */ e.createElement("span", { key: y.value, className: "tlDropdownSelect__readonlyValue" }, /* @__PURE__ */ e.createElement(Xe, { image: y.image }), /* @__PURE__ */ e.createElement("span", null, y.label))));
  const U = !o && l.length > 0 && !u, Q = h ? /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: $,
      className: "tlDropdownSelect__dropdown",
      style: V
    },
    (r || C) && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__searchWrapper" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__searchIcon", "aria-hidden": "true" }, "🔍"), /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: re,
        type: "text",
        className: "tlDropdownSelect__search",
        value: k,
        onChange: ie,
        onKeyDown: le,
        placeholder: w["js.dropdownSelect.filterPlaceholder"],
        "aria-label": w["js.dropdownSelect.filterPlaceholder"],
        "aria-activedescendant": I >= 0 ? `${n}-opt-${I}` : void 0,
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
      !r && !C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__spinner" })),
      C && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__error" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: ge }, w["js.dropdownSelect.error"])),
      r && g.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__noResults" }, b),
      r && g.map((y, D) => /* @__PURE__ */ e.createElement(
        _l,
        {
          key: y.value,
          id: `${n}-opt-${D}`,
          option: y,
          highlighted: D === I,
          searchTerm: k,
          onSelect: K,
          onMouseEnter: () => _(D)
        }
      ))
    )
  ) : null;
  return /* @__PURE__ */ e.createElement(e.Fragment, null, /* @__PURE__ */ e.createElement(
    "div",
    {
      id: n,
      ref: Z,
      className: "tlDropdownSelect" + (h ? " tlDropdownSelect--open" : "") + (u ? " tlDropdownSelect--disabled" : ""),
      role: "combobox",
      "aria-expanded": h,
      "aria-haspopup": "listbox",
      "aria-owns": h ? `${n}-listbox` : void 0,
      tabIndex: u ? -1 : 0,
      onClick: h ? void 0 : F,
      onKeyDown: le
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__chips" }, l.length === 0 ? /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__placeholder" }, p) : l.map((y, D) => {
      let W = "";
      return L === D ? W = "tlDropdownSelect__chip--dragging" : R === D && P === "before" ? W = "tlDropdownSelect__chip--dropBefore" : R === D && P === "after" && (W = "tlDropdownSelect__chip--dropAfter"), /* @__PURE__ */ e.createElement(
        hl,
        {
          key: y.value,
          option: y,
          removable: !u && (c || !o),
          onRemove: q,
          removeLabel: v(y.label),
          draggable: f,
          onDragStart: f ? (ee) => Se(D, ee) : void 0,
          onDragOver: f ? (ee) => Pe(D, ee) : void 0,
          onDrop: f ? S : void 0,
          onDragEnd: f ? x : void 0,
          dragClassName: f ? W : void 0
        }
      );
    })),
    /* @__PURE__ */ e.createElement("div", { className: "tlDropdownSelect__controls" }, U && /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlDropdownSelect__clearAll",
        onClick: J,
        "aria-label": w["js.dropdownSelect.clear"]
      },
      "×"
    ), /* @__PURE__ */ e.createElement("span", { className: "tlDropdownSelect__arrow", "aria-hidden": "true" }, h ? "▲" : "▼"))
  ), Q && fl.createPortal(Q, document.body));
}, { useCallback: He, useRef: vl } = e, ht = "application/x-tl-color", El = ({
  colors: n,
  columns: t,
  onSelect: a,
  onConfirm: l,
  onSwap: c,
  onReplace: i
}) => {
  const o = vl(null), u = He(
    (m) => (p) => {
      o.current = m, p.dataTransfer.effectAllowed = "move";
    },
    []
  ), s = He((m) => {
    m.preventDefault(), m.dataTransfer.dropEffect = "move";
  }, []), r = He(
    (m) => (p) => {
      p.preventDefault();
      const f = p.dataTransfer.getData(ht);
      f ? i(m, f) : o.current !== null && o.current !== m && c(o.current, m), o.current = null;
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
function _t(n) {
  return Math.max(0, Math.min(255, Math.round(n)));
}
function Ke(n) {
  return /^#[0-9a-fA-F]{6}$/.test(n);
}
function bt(n) {
  if (!Ke(n)) return [0, 0, 0];
  const t = parseInt(n.slice(1), 16);
  return [t >> 16 & 255, t >> 8 & 255, t & 255];
}
function vt(n, t, a) {
  const l = (c) => _t(c).toString(16).padStart(2, "0");
  return "#" + l(n) + l(t) + l(a);
}
function gl(n, t, a) {
  const l = n / 255, c = t / 255, i = a / 255, o = Math.max(l, c, i), u = Math.min(l, c, i), s = o - u;
  let r = 0;
  s !== 0 && (o === l ? r = (c - i) / s % 6 : o === c ? r = (i - l) / s + 2 : r = (l - c) / s + 4, r *= 60, r < 0 && (r += 360));
  const m = o === 0 ? 0 : s / o;
  return [r, m, o];
}
function Cl(n, t, a) {
  const l = a * t, c = l * (1 - Math.abs(n / 60 % 2 - 1)), i = a - l;
  let o = 0, u = 0, s = 0;
  return n < 60 ? (o = l, u = c, s = 0) : n < 120 ? (o = c, u = l, s = 0) : n < 180 ? (o = 0, u = l, s = c) : n < 240 ? (o = 0, u = c, s = l) : n < 300 ? (o = c, u = 0, s = l) : (o = l, u = 0, s = c), [
    Math.round((o + i) * 255),
    Math.round((u + i) * 255),
    Math.round((s + i) * 255)
  ];
}
function yl(n) {
  return gl(...bt(n));
}
function We(n, t, a) {
  return vt(...Cl(n, t, a));
}
const { useCallback: ve, useRef: dt } = e, wl = ({ color: n, onColorChange: t }) => {
  const [a, l, c] = yl(n), i = dt(null), o = dt(null), u = ve(
    (b, v) => {
      var M;
      const h = (M = i.current) == null ? void 0 : M.getBoundingClientRect();
      if (!h) return;
      const N = Math.max(0, Math.min(1, (b - h.left) / h.width)), k = Math.max(0, Math.min(1, 1 - (v - h.top) / h.height));
      t(We(a, N, k));
    },
    [a, t]
  ), s = ve(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), u(b.clientX, b.clientY);
    },
    [u]
  ), r = ve(
    (b) => {
      b.buttons !== 0 && u(b.clientX, b.clientY);
    },
    [u]
  ), m = ve(
    (b) => {
      var k;
      const v = (k = o.current) == null ? void 0 : k.getBoundingClientRect();
      if (!v) return;
      const N = Math.max(0, Math.min(1, (b - v.top) / v.height)) * 360;
      t(We(N, l, c));
    },
    [l, c, t]
  ), p = ve(
    (b) => {
      b.preventDefault(), b.target.setPointerCapture(b.pointerId), m(b.clientY);
    },
    [m]
  ), f = ve(
    (b) => {
      b.buttons !== 0 && m(b.clientY);
    },
    [m]
  ), w = We(a, 1, 1);
  return /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__mixer" }, /* @__PURE__ */ e.createElement(
    "div",
    {
      ref: i,
      className: "tlColorInput__svField",
      style: { backgroundColor: w },
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
      onPointerMove: f
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
function kl(n, t) {
  const a = t.toUpperCase();
  return n.some((l) => l != null && l.toUpperCase() === a);
}
const Sl = {
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
}, { useState: Te, useCallback: me, useEffect: ze, useRef: Nl, useLayoutEffect: Tl } = e, Rl = ({
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
  const [r, m] = Te("palette"), [p, f] = Te(t), w = Nl(null), b = oe(Sl), [v, h] = Te(null);
  Tl(() => {
    if (!n.current || !w.current) return;
    const $ = n.current.getBoundingClientRect(), O = w.current.getBoundingClientRect();
    let X = $.bottom + 4, d = $.left;
    X + O.height > window.innerHeight && (X = $.top - O.height - 4), d + O.width > window.innerWidth && (d = Math.max(0, $.right - O.width)), h({ top: X, left: d });
  }, [n]);
  const N = p != null, [k, M, I] = N ? bt(p) : [0, 0, 0], [_, C] = Te((p == null ? void 0 : p.toUpperCase()) ?? "");
  ze(() => {
    C((p == null ? void 0 : p.toUpperCase()) ?? "");
  }, [p]), ze(() => {
    const $ = (O) => {
      O.key === "Escape" && u();
    };
    return document.addEventListener("keydown", $), () => document.removeEventListener("keydown", $);
  }, [u]), ze(() => {
    const $ = (X) => {
      w.current && !w.current.contains(X.target) && u();
    }, O = setTimeout(() => document.addEventListener("mousedown", $), 0);
    return () => {
      clearTimeout(O), document.removeEventListener("mousedown", $);
    };
  }, [u]);
  const E = me(
    ($) => (O) => {
      const X = parseInt(O.target.value, 10);
      if (isNaN(X)) return;
      const d = _t(X);
      f(vt($ === "r" ? d : k, $ === "g" ? d : M, $ === "b" ? d : I));
    },
    [k, M, I]
  ), V = me(
    ($) => {
      if (p != null) {
        $.dataTransfer.setData(ht, p.toUpperCase()), $.dataTransfer.effectAllowed = "move";
        const O = document.createElement("div");
        O.style.width = "33px", O.style.height = "33px", O.style.backgroundColor = p, O.style.borderRadius = "3px", O.style.border = "1px solid rgba(0,0,0,0.1)", O.style.position = "absolute", O.style.top = "-9999px", document.body.appendChild(O), $.dataTransfer.setDragImage(O, 16, 16), requestAnimationFrame(() => document.body.removeChild(O));
      }
    },
    [p]
  ), T = me(($) => {
    const O = $.target.value;
    C(O), Ke(O) && f(O);
  }, []), L = me(() => {
    f(null);
  }, []), B = me(($) => {
    f($);
  }, []), R = me(
    ($) => {
      o($);
    },
    [o]
  ), j = me(
    ($, O) => {
      const X = [...a], d = X[$];
      X[$] = X[O], X[O] = d, s(X);
    },
    [a, s]
  ), P = me(
    ($, O) => {
      const X = [...a];
      X[$] = O, s(X);
    },
    [a, s]
  ), te = me(() => {
    s([...c]);
  }, [c, s]), Z = me(
    ($) => {
      if (kl(a, $)) return;
      const O = a.indexOf(null);
      if (O < 0) return;
      const X = [...a];
      X[O] = $.toUpperCase(), s(X);
    },
    [a, s]
  ), re = me(() => {
    p != null && Z(p), o(p);
  }, [p, o, Z]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlColorInput__popup",
      ref: w,
      style: v ? { top: v.top, left: v.left, visibility: "visible" } : { visibility: "hidden" }
    },
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__tabs" }, /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "palette" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("palette")
      },
      b["js.colorInput.paletteTab"]
    ), /* @__PURE__ */ e.createElement(
      "button",
      {
        className: "tlColorInput__tab" + (r === "mixer" ? " tlColorInput__tab--active" : ""),
        onClick: () => m("mixer")
      },
      b["js.colorInput.mixerTab"]
    )),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__body" }, r === "palette" ? /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__paletteArea" }, /* @__PURE__ */ e.createElement(
      El,
      {
        colors: a,
        columns: l,
        onSelect: B,
        onConfirm: R,
        onSwap: j,
        onReplace: P
      }
    ), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__paletteReset", onClick: te }, b["js.colorInput.reset"])) : /* @__PURE__ */ e.createElement(wl, { color: p ?? "#000000", onColorChange: f }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__controls" }, /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.current"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (t == null ? " tlColorInput--noColor" : ""),
        style: t != null ? { backgroundColor: t } : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__previewRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__previewLabel" }, b["js.colorInput.new"]), /* @__PURE__ */ e.createElement(
      "div",
      {
        className: "tlColorInput__previewSwatch" + (N ? "" : " tlColorInput--noColor"),
        style: N ? { backgroundColor: p } : void 0,
        draggable: N,
        onDragStart: N ? V : void 0
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__divider" }), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.red"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? k : "",
        onChange: E("r")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.green"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? M : "",
        onChange: E("g")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.blue"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input",
        type: "number",
        min: 0,
        max: 255,
        value: N ? I : "",
        onChange: E("b")
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__inputRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlColorInput__inputLabel" }, b["js.colorInput.hex"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlColorInput__input" + (_ !== "" && !Ke(_) ? " tlColorInput__input--error" : ""),
        type: "text",
        value: _,
        onChange: T
      }
    )))),
    /* @__PURE__ */ e.createElement("div", { className: "tlColorInput__actions" }, i && /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--reset", onClick: L }, b["js.colorInput.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--cancel", onClick: u }, b["js.colorInput.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlColorInput__btn tlColorInput__btn--ok", onClick: re }, b["js.colorInput.ok"]))
  );
}, Ll = { "js.colorInput.chooseColor": "Choose color" }, { useState: xl, useCallback: Re, useRef: Dl } = e, Il = ({ controlId: n, state: t }) => {
  const a = ne(), l = oe(Ll), [c, i] = xl(!1), o = Dl(null), u = t.value, s = t.editable !== !1, r = t.palette ?? [], m = t.paletteColumns ?? 6, p = t.defaultPalette ?? r, f = Re(() => {
    s && i(!0);
  }, [s]), w = Re(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), b = Re(() => {
    i(!1);
  }, []), v = Re(
    (h) => {
      a("paletteChanged", { palette: h });
    },
    [a]
  );
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlColorInput" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlColorInput__swatch" + (u == null ? " tlColorInput__swatch--noColor" : ""),
      style: u != null ? { backgroundColor: u } : void 0,
      onClick: f,
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
      onConfirm: w,
      onCancel: b,
      onPaletteChange: v
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
}, { useState: ye, useCallback: he, useEffect: Le, useRef: mt, useLayoutEffect: jl, useMemo: Pl } = e, Ml = {
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
}, Al = ({
  anchorRef: n,
  currentValue: t,
  icons: a,
  iconsLoaded: l,
  onSelect: c,
  onCancel: i,
  onLoadIcons: o
}) => {
  const u = oe(Ml), [s, r] = ye("simple"), [m, p] = ye(""), [f, w] = ye(t ?? ""), [b, v] = ye(!1), [h, N] = ye(null), k = mt(null), M = mt(null);
  jl(() => {
    if (!n.current || !k.current) return;
    const R = n.current.getBoundingClientRect(), j = k.current.getBoundingClientRect();
    let P = R.bottom + 4, te = R.left;
    P + j.height > window.innerHeight && (P = R.top - j.height - 4), te + j.width > window.innerWidth && (te = Math.max(0, R.right - j.width)), N({ top: P, left: te });
  }, [n]), Le(() => {
    !l && !b && o().catch(() => v(!0));
  }, [l, b, o]), Le(() => {
    l && M.current && M.current.focus();
  }, [l]), Le(() => {
    const R = (j) => {
      j.key === "Escape" && i();
    };
    return document.addEventListener("keydown", R), () => document.removeEventListener("keydown", R);
  }, [i]), Le(() => {
    const R = (P) => {
      k.current && !k.current.contains(P.target) && i();
    }, j = setTimeout(() => document.addEventListener("mousedown", R), 0);
    return () => {
      clearTimeout(j), document.removeEventListener("mousedown", R);
    };
  }, [i]);
  const I = Pl(() => {
    if (!m) return a;
    const R = m.toLowerCase();
    return a.filter(
      (j) => j.prefix.toLowerCase().includes(R) || j.label.toLowerCase().includes(R) || j.terms != null && j.terms.some((P) => P.includes(R))
    );
  }, [a, m]), _ = he((R) => {
    p(R.target.value);
  }, []), C = he(
    (R) => {
      c(R);
    },
    [c]
  ), E = he((R) => {
    w(R);
  }, []), V = he((R) => {
    w(R.target.value);
  }, []), T = he(() => {
    c(f || null);
  }, [f, c]), L = he(() => {
    c(null);
  }, [c]), B = he(async (R) => {
    R.preventDefault(), v(!1);
    try {
      await o();
    } catch {
      v(!0);
    }
  }, [o]);
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: "tlIconSelect__popup",
      ref: k,
      style: h ? { top: h.top, left: h.left, visibility: "visible" } : { visibility: "hidden" }
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
      !l && !b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__loading" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__spinner" })),
      b && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, /* @__PURE__ */ e.createElement("a", { href: "#", onClick: B }, u["js.iconSelect.loadError"])),
      l && I.length === 0 && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__noResults" }, u["js.iconSelect.noResults"]),
      l && I.map(
        (R) => R.variants.map((j) => /* @__PURE__ */ e.createElement(
          "div",
          {
            key: j.encoded,
            className: "tlIconSelect__iconCell" + (j.encoded === t ? " tlIconSelect__iconCell--selected" : ""),
            role: "option",
            "aria-selected": j.encoded === t,
            tabIndex: 0,
            title: R.label,
            onClick: () => s === "simple" ? C(j.encoded) : E(j.encoded),
            onKeyDown: (P) => {
              (P.key === "Enter" || P.key === " ") && (P.preventDefault(), s === "simple" ? C(j.encoded) : E(j.encoded));
            }
          },
          /* @__PURE__ */ e.createElement(we, { encoded: j.encoded })
        ))
      )
    ),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__advancedArea" }, /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__editRow" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.classLabel"]), /* @__PURE__ */ e.createElement(
      "input",
      {
        className: "tlIconSelect__editInput",
        type: "text",
        value: f,
        onChange: V
      }
    )), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewArea" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__editLabel" }, u["js.iconSelect.previewLabel"]), /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__previewIcon" }, f && /* @__PURE__ */ e.createElement(we, { encoded: f })), /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__previewLabel" }, f ? f.startsWith("css:") ? f.substring(4) : f : ""))),
    s === "advanced" && /* @__PURE__ */ e.createElement("div", { className: "tlIconSelect__actions" }, /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--cancel", onClick: i }, u["js.iconSelect.cancel"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--clear", onClick: L }, u["js.iconSelect.clear"]), /* @__PURE__ */ e.createElement("button", { className: "tlIconSelect__btn tlIconSelect__btn--ok", onClick: T }, u["js.iconSelect.ok"]))
  );
}, Bl = { "js.iconSelect.chooseIcon": "Choose icon" }, { useState: Ol, useCallback: xe, useRef: $l } = e, Fl = ({ controlId: n, state: t }) => {
  const a = ne(), l = oe(Bl), [c, i] = Ol(!1), o = $l(null), u = t.value, s = t.editable !== !1, r = t.disabled === !0, m = t.icons ?? [], p = t.iconsLoaded === !0, f = xe(() => {
    s && !r && i(!0);
  }, [s, r]), w = xe(
    (h) => {
      i(!1), a("valueChanged", { value: h });
    },
    [a]
  ), b = xe(() => {
    i(!1);
  }, []), v = xe(async () => {
    await a("loadIcons");
  }, [a]);
  return s ? /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      ref: o,
      className: "tlIconSelect__swatch" + (u == null ? " tlIconSelect__swatch--empty" : ""),
      onClick: f,
      disabled: r,
      title: u ?? "",
      "aria-label": l["js.iconSelect.chooseIcon"]
    },
    u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : /* @__PURE__ */ e.createElement("i", { className: "fa-solid fa-icons" })
  ), c && /* @__PURE__ */ e.createElement(
    Al,
    {
      anchorRef: o,
      currentValue: u,
      icons: m,
      iconsLoaded: p,
      onSelect: w,
      onCancel: b,
      onLoadIcons: v
    }
  )) : /* @__PURE__ */ e.createElement("span", { id: n, className: "tlIconSelect tlIconSelect--immutable" }, /* @__PURE__ */ e.createElement("span", { className: "tlIconSelect__swatch" }, u ? /* @__PURE__ */ e.createElement(we, { encoded: u }) : null));
};
H("TLButton", Pt);
H("TLToggleButton", At);
H("TLTextInput", yt);
H("TLNumberInput", kt);
H("TLDatePicker", Nt);
H("TLSelect", Rt);
H("TLCheckbox", xt);
H("TLTable", Dt);
H("TLCounter", Bt);
H("TLTabBar", $t);
H("TLFieldList", Ft);
H("TLAudioRecorder", Wt);
H("TLAudioPlayer", Ut);
H("TLFileUpload", Kt);
H("TLDownload", Gt);
H("TLPhotoCapture", qt);
H("TLPhotoViewer", Qt);
H("TLSplitPanel", Jt);
H("TLPanel", on);
H("TLMaximizeRoot", sn);
H("TLDeckPane", cn);
H("TLSidebar", bn);
H("TLStack", vn);
H("TLGrid", En);
H("TLCard", gn);
H("TLAppBar", Cn);
H("TLBreadcrumb", wn);
H("TLBottomBar", Sn);
H("TLDialog", Tn);
H("TLDialogManager", xn);
H("TLWindow", jn);
H("TLDrawer", Bn);
H("TLSnackbar", Fn);
H("TLMenu", Wn);
H("TLAppShell", zn);
H("TLTextCell", Un);
H("TLTableView", Kn);
H("TLFormLayout", Jn);
H("TLFormGroup", nl);
H("TLFormField", ol);
H("TLResourceCell", sl);
H("TLTreeView", il);
H("TLDropdownSelect", bl);
H("TLColorInput", Il);
H("TLIconSelect", Fl);
