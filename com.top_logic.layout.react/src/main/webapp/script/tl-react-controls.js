import { React as e, useTLFieldValue as _, getComponent as A, useTLState as g, useTLCommand as E, TLChild as y, useTLUpload as N, useTLDataUrl as S, register as m } from "tl-react-bridge";
const { useCallback: D } = e, U = ({ state: l }) => {
  const [c, a] = _(), n = D(
    (t) => {
      a(t.target.value);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: c ?? "",
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: F } = e, P = ({ state: l, config: c }) => {
  const [a, n] = _(), t = F(
    (r) => {
      const o = r.target.value, i = o === "" ? null : Number(o);
      n(i);
    },
    [n]
  ), s = c != null && c.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: t,
      step: s,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: w } = e, B = ({ state: l }) => {
  const [c, a] = _(), n = w(
    (t) => {
      a(t.target.value || null);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: c ?? "",
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: $ } = e, I = ({ state: l, config: c }) => {
  const [a, n] = _(), t = $(
    (r) => {
      n(r.target.value || null);
    },
    [n]
  ), s = l.options ?? (c == null ? void 0 : c.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: t,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((r) => /* @__PURE__ */ e.createElement("option", { key: r.value, value: r.value }, r.label))
  );
}, { useCallback: O } = e, x = ({ state: l }) => {
  const [c, a] = _(), n = O(
    (t) => {
      a(t.target.checked);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: c === !0,
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, V = ({ controlId: l, state: c }) => {
  const a = c.columns ?? [], n = c.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((t, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, a.map((r) => {
    const o = r.cellModule ? A(r.cellModule) : void 0, i = t[r.name];
    if (o) {
      const b = { value: i, editable: c.editable };
      return /* @__PURE__ */ e.createElement("td", { key: r.name }, /* @__PURE__ */ e.createElement(
        o,
        {
          controlId: l + "-" + s + "-" + r.name,
          state: b
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: r.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: j } = e, M = ({ command: l, label: c, disabled: a }) => {
  const n = g(), t = E(), s = l ?? "click", r = c ?? n.label, o = a ?? n.disabled === !0, i = j(() => {
    t(s);
  }, [t, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: o,
      className: "tlReactButton"
    },
    r
  );
}, { useCallback: z } = e, q = ({ command: l, label: c, active: a, disabled: n }) => {
  const t = g(), s = E(), r = l ?? "click", o = c ?? t.label, i = a ?? t.active === !0, b = n ?? t.disabled === !0, v = z(() => {
    s(r);
  }, [s, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: b,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    o
  );
}, G = () => {
  const l = g(), c = E(), a = l.count ?? 0, n = l.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: H } = e, J = () => {
  const l = g(), c = E(), a = l.tabs ?? [], n = l.activeTabId, t = H((s) => {
    s !== n && c("selectTab", { tabId: s });
  }, [c, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === n,
      className: "tlReactTabBar__tab" + (s.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => t(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, l.activeContent && /* @__PURE__ */ e.createElement(y, { control: l.activeContent })));
}, K = () => {
  const l = g(), c = l.title, a = l.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, c && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((n, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(y, { control: n })))));
}, Q = () => {
  const l = g(), c = N(), [a, n] = e.useState("idle"), t = e.useRef(null), s = e.useRef([]), r = e.useRef(null), o = l.status ?? "idle", i = l.error, b = o === "received" ? "idle" : a !== "idle" ? a : o, v = e.useCallback(async () => {
    if (a === "recording") {
      const d = t.current;
      d && d.state !== "inactive" && d.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const d = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = d, s.current = [];
        const h = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", T = new MediaRecorder(d, h ? { mimeType: h } : void 0);
        t.current = T, T.ondataavailable = (L) => {
          L.data.size > 0 && s.current.push(L.data);
        }, T.onstop = async () => {
          d.getTracks().forEach((p) => p.stop()), r.current = null;
          const L = new Blob(s.current, { type: T.mimeType || "audio/webm" });
          if (s.current = [], L.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const u = new FormData();
          u.append("audio", L, "recording.webm"), await c(u), n("idle");
        }, T.start(), n("recording");
      } catch (d) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", d), n("idle");
      }
  }, [a, c]), C = b === "recording" ? "Stop recording" : b === "uploading" ? "Uploading…" : "Record audio", k = b === "uploading", f = ["tlAudioRecorder__button"];
  return b === "recording" && f.push("tlAudioRecorder__button--recording"), b === "uploading" && f.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: f.join(" "),
      onClick: v,
      disabled: k,
      title: C,
      "aria-label": C
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${b === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, W = () => {
  const l = g(), c = S(), a = !!l.hasAudio, n = l.dataRevision ?? 0, [t, s] = e.useState(a ? "idle" : "disabled"), r = e.useRef(null), o = e.useRef(null), i = e.useRef(n);
  e.useEffect(() => {
    a ? t === "disabled" && s("idle") : (r.current && (r.current.pause(), r.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), s("disabled"));
  }, [a]), e.useEffect(() => {
    n !== i.current && (i.current = n, r.current && (r.current.pause(), r.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), (t === "playing" || t === "paused" || t === "loading") && s("idle"));
  }, [n]), e.useEffect(() => () => {
    r.current && (r.current.pause(), r.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const b = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      r.current && r.current.pause(), s("paused");
      return;
    }
    if (t === "paused" && r.current) {
      r.current.play(), s("playing");
      return;
    }
    if (!o.current) {
      s("loading");
      try {
        const d = await fetch(c);
        if (!d.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", d.status), s("idle");
          return;
        }
        const h = await d.blob();
        o.current = URL.createObjectURL(h);
      } catch (d) {
        console.error("[TLAudioPlayer] Fetch error:", d), s("idle");
        return;
      }
    }
    const f = new Audio(o.current);
    r.current = f, f.onended = () => {
      s("idle");
    }, f.play(), s("playing");
  }, [t, c]), v = t === "loading" ? "Loading…" : t === "playing" ? "Pause audio" : t === "disabled" ? "No audio" : "Play audio", C = t === "disabled" || t === "loading", k = ["tlAudioPlayer__button"];
  return t === "playing" && k.push("tlAudioPlayer__button--playing"), t === "loading" && k.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: k.join(" "),
      onClick: b,
      disabled: C,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, X = () => {
  const l = g(), c = N(), [a, n] = e.useState("idle"), [t, s] = e.useState(!1), r = e.useRef(null), o = l.status ?? "idle", i = l.error, b = l.accept ?? "", v = o === "received" ? "idle" : a !== "idle" ? a : o, C = e.useCallback(async (u) => {
    n("uploading");
    const p = new FormData();
    p.append("file", u, u.name), await c(p), n("idle");
  }, [c]), k = e.useCallback((u) => {
    var R;
    const p = (R = u.target.files) == null ? void 0 : R[0];
    p && C(p);
  }, [C]), f = e.useCallback(() => {
    var u;
    a !== "uploading" && ((u = r.current) == null || u.click());
  }, [a]), d = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), s(!0);
  }, []), h = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), s(!1);
  }, []), T = e.useCallback((u) => {
    var R;
    if (u.preventDefault(), u.stopPropagation(), s(!1), a === "uploading") return;
    const p = (R = u.dataTransfer.files) == null ? void 0 : R[0];
    p && C(p);
  }, [a, C]), L = v === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: d,
      onDragLeave: h,
      onDrop: T
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: r,
        type: "file",
        accept: b || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: f,
        disabled: L
      },
      v === "uploading" ? "Uploading…" : "Choose File"
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
};
m("TLButton", M);
m("TLToggleButton", q);
m("TLTextInput", U);
m("TLNumberInput", P);
m("TLDatePicker", B);
m("TLSelect", I);
m("TLCheckbox", x);
m("TLTable", V);
m("TLCounter", G);
m("TLTabBar", J);
m("TLFieldList", K);
m("TLAudioRecorder", Q);
m("TLAudioPlayer", W);
m("TLFileUpload", X);
