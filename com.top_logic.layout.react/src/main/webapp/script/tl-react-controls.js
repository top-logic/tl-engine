import { React as e, useTLFieldValue as _, getComponent as A, useTLState as f, useTLCommand as y, TLChild as R, useTLUpload as N, useTLDataUrl as S, register as b } from "tl-react-bridge";
const { useCallback: D } = e, U = ({ state: n }) => {
  const [c, a] = _(), t = D(
    (s) => {
      a(s.target.value);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: c ?? "",
      onChange: t,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: F } = e, P = ({ state: n, config: c }) => {
  const [a, t] = _(), s = F(
    (o) => {
      const i = o.target.value, u = i === "" ? null : Number(i);
      t(u);
    },
    [t]
  ), l = c != null && c.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: s,
      step: l,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: w } = e, B = ({ state: n }) => {
  const [c, a] = _(), t = w(
    (s) => {
      a(s.target.value || null);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: c ?? "",
      onChange: t,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: $ } = e, I = ({ state: n, config: c }) => {
  const [a, t] = _(), s = $(
    (o) => {
      t(o.target.value || null);
    },
    [t]
  ), l = n.options ?? (c == null ? void 0 : c.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: s,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    l.map((o) => /* @__PURE__ */ e.createElement("option", { key: o.value, value: o.value }, o.label))
  );
}, { useCallback: O } = e, x = ({ state: n }) => {
  const [c, a] = _(), t = O(
    (s) => {
      a(s.target.checked);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: c === !0,
      onChange: t,
      disabled: n.disabled === !0 || n.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, V = ({ controlId: n, state: c }) => {
  const a = c.columns ?? [], t = c.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, t.map((s, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((o) => {
    const i = o.cellModule ? A(o.cellModule) : void 0, u = s[o.name];
    if (i) {
      const d = { value: u, editable: c.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        i,
        {
          controlId: n + "-" + l + "-" + o.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, u != null ? String(u) : "");
  })))));
}, { useCallback: M } = e, j = ({ command: n, label: c, disabled: a }) => {
  const t = f(), s = y(), l = n ?? "click", o = c ?? t.label, i = a ?? t.disabled === !0, u = M(() => {
    s(l);
  }, [s, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: u,
      disabled: i,
      className: "tlReactButton"
    },
    o
  );
}, { useCallback: z } = e, q = ({ command: n, label: c, active: a, disabled: t }) => {
  const s = f(), l = y(), o = n ?? "click", i = c ?? s.label, u = a ?? s.active === !0, d = t ?? s.disabled === !0, C = z(() => {
    l(o);
  }, [l, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: C,
      disabled: d,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    i
  );
}, G = () => {
  const n = f(), c = y(), a = n.count ?? 0, t = n.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, t), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: H } = e, J = () => {
  const n = f(), c = y(), a = n.tabs ?? [], t = n.activeTabId, s = H((l) => {
    l !== t && c("selectTab", { tabId: l });
  }, [c, t]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((l) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: l.id,
      role: "tab",
      "aria-selected": l.id === t,
      className: "tlReactTabBar__tab" + (l.id === t ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(l.id)
    },
    l.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, n.activeContent && /* @__PURE__ */ e.createElement(R, { control: n.activeContent })));
}, K = () => {
  const n = f(), c = n.title, a = n.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, c && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((t, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: t })))));
}, Q = () => {
  const n = f(), c = N(), [a, t] = e.useState("idle"), s = e.useRef(null), l = e.useRef([]), o = e.useRef(null), i = n.status ?? "idle", u = n.error, d = i === "received" ? "idle" : a !== "idle" ? a : i, C = e.useCallback(async () => {
    if (a === "recording") {
      const p = s.current;
      p && p.state !== "inactive" && p.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const p = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = p, l.current = [];
        const E = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", k = new MediaRecorder(p, E ? { mimeType: E } : void 0);
        s.current = k, k.ondataavailable = (h) => {
          h.data.size > 0 && l.current.push(h.data);
        }, k.onstop = async () => {
          p.getTracks().forEach((v) => v.stop()), o.current = null;
          const h = new Blob(l.current, { type: k.mimeType || "audio/webm" });
          if (l.current = [], h.size === 0) {
            t("idle");
            return;
          }
          t("uploading");
          const r = new FormData();
          r.append("audio", h, "recording.webm"), await c(r), t("idle");
        }, k.start(), t("recording");
      } catch (p) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", p), t("idle");
      }
  }, [a, c]), m = d === "recording" ? "Stop recording" : d === "uploading" ? "Uploading…" : "Record audio", g = d === "uploading", T = ["tlAudioRecorder__button"];
  return d === "recording" && T.push("tlAudioRecorder__button--recording"), d === "uploading" && T.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: T.join(" "),
      onClick: C,
      disabled: g,
      title: m,
      "aria-label": m
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${d === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, u));
}, W = () => {
  const n = f(), c = S(), a = !!n.hasAudio, [t, s] = e.useState(a ? "idle" : "disabled"), l = e.useRef(null), o = e.useRef(null);
  e.useEffect(() => {
    a ? t === "disabled" && s("idle") : (l.current && (l.current.pause(), l.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null), s("disabled"));
  }, [a]), e.useEffect(() => () => {
    l.current && (l.current.pause(), l.current = null), o.current && (URL.revokeObjectURL(o.current), o.current = null);
  }, []);
  const i = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      l.current && l.current.pause(), s("paused");
      return;
    }
    if (t === "paused" && l.current) {
      l.current.play(), s("playing");
      return;
    }
    if (!o.current) {
      s("loading");
      try {
        const g = await fetch(c);
        if (!g.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", g.status), s("idle");
          return;
        }
        const T = await g.blob();
        o.current = URL.createObjectURL(T);
      } catch (g) {
        console.error("[TLAudioPlayer] Fetch error:", g), s("idle");
        return;
      }
    }
    const m = new Audio(o.current);
    l.current = m, m.onended = () => {
      s("idle");
    }, m.play(), s("playing");
  }, [t, c]), u = t === "loading" ? "Loading…" : t === "playing" ? "Pause audio" : t === "disabled" ? "No audio" : "Play audio", d = t === "disabled" || t === "loading", C = ["tlAudioPlayer__button"];
  return t === "playing" && C.push("tlAudioPlayer__button--playing"), t === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: i,
      disabled: d,
      title: u,
      "aria-label": u
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, X = () => {
  const n = f(), c = N(), [a, t] = e.useState("idle"), [s, l] = e.useState(!1), o = e.useRef(null), i = n.status ?? "idle", u = n.error, d = n.accept ?? "", C = i === "received" ? "idle" : a !== "idle" ? a : i, m = e.useCallback(async (r) => {
    t("uploading");
    const v = new FormData();
    v.append("file", r, r.name), await c(v), t("idle");
  }, [c]), g = e.useCallback((r) => {
    var L;
    const v = (L = r.target.files) == null ? void 0 : L[0];
    v && m(v);
  }, [m]), T = e.useCallback(() => {
    var r;
    a !== "uploading" && ((r = o.current) == null || r.click());
  }, [a]), p = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), l(!0);
  }, []), E = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), l(!1);
  }, []), k = e.useCallback((r) => {
    var L;
    if (r.preventDefault(), r.stopPropagation(), l(!1), a === "uploading") return;
    const v = (L = r.dataTransfer.files) == null ? void 0 : L[0];
    v && m(v);
  }, [a, m]), h = C === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: p,
      onDragLeave: E,
      onDrop: k
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: o,
        type: "file",
        accept: d || void 0,
        onChange: g,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: T,
        disabled: h
      },
      C === "uploading" ? "Uploading…" : "Choose File"
    ),
    u && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, u)
  );
};
b("TLButton", j);
b("TLToggleButton", q);
b("TLTextInput", U);
b("TLNumberInput", P);
b("TLDatePicker", B);
b("TLSelect", I);
b("TLCheckbox", x);
b("TLTable", V);
b("TLCounter", G);
b("TLTabBar", J);
b("TLFieldList", K);
b("TLAudioRecorder", Q);
b("TLAudioPlayer", W);
b("TLFileUpload", X);
