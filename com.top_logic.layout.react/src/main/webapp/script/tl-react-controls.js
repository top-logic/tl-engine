import { React as e, useTLFieldValue as h, getComponent as S, useTLState as g, useTLCommand as y, TLChild as R, useTLUpload as N, useTLDataUrl as D, register as m } from "tl-react-bridge";
const { useCallback: A } = e, U = ({ state: n }) => {
  const [c, a] = h(), t = A(
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
  const [a, t] = h(), s = F(
    (r) => {
      const u = r.target.value, i = u === "" ? null : Number(u);
      t(i);
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
  const [c, a] = h(), t = w(
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
  const [a, t] = h(), s = $(
    (r) => {
      t(r.target.value || null);
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
    l.map((r) => /* @__PURE__ */ e.createElement("option", { key: r.value, value: r.value }, r.label))
  );
}, { useCallback: O } = e, x = ({ state: n }) => {
  const [c, a] = h(), t = O(
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
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, t.map((s, l) => /* @__PURE__ */ e.createElement("tr", { key: l }, a.map((r) => {
    const u = r.cellModule ? S(r.cellModule) : void 0, i = s[r.name];
    if (u) {
      const d = { value: i, editable: c.editable };
      return /* @__PURE__ */ e.createElement("td", { key: r.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: n + "-" + l + "-" + r.name,
          state: d
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: r.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: M } = e, j = ({ command: n, label: c, disabled: a }) => {
  const t = g(), s = y(), l = n ?? "click", r = c ?? t.label, u = a ?? t.disabled === !0, i = M(() => {
    s(l);
  }, [s, l]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: u,
      className: "tlReactButton"
    },
    r
  );
}, { useCallback: z } = e, q = ({ command: n, label: c, active: a, disabled: t }) => {
  const s = g(), l = y(), r = n ?? "click", u = c ?? s.label, i = a ?? s.active === !0, d = t ?? s.disabled === !0, v = z(() => {
    l(r);
  }, [l, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: d,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    u
  );
}, G = () => {
  const n = g(), c = y(), a = n.count ?? 0, t = n.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, t), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => c("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: H } = e, J = () => {
  const n = g(), c = y(), a = n.tabs ?? [], t = n.activeTabId, s = H((l) => {
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
  const n = g(), c = n.title, a = n.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, c && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, c), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((t, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: t })))));
}, Q = () => {
  const n = g(), c = N(), [a, t] = e.useState("idle"), s = e.useRef(null), l = e.useRef([]), r = e.useRef(null), u = n.status ?? "idle", i = n.error, d = u === "received" ? "idle" : a !== "idle" ? a : u, v = e.useCallback(async () => {
    if (a === "recording") {
      const p = s.current;
      p && p.state !== "inactive" && p.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const p = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = p, l.current = [];
        const E = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", f = new MediaRecorder(p, E ? { mimeType: E } : void 0);
        s.current = f, f.ondataavailable = (T) => {
          T.data.size > 0 && l.current.push(T.data);
        }, f.onstop = async () => {
          p.getTracks().forEach((o) => o.stop()), r.current = null;
          const T = new Blob(l.current, { type: f.mimeType || "audio/webm" });
          if (l.current = [], T.size === 0) {
            t("idle");
            return;
          }
          t("uploading");
          const _ = new FormData();
          _.append("audio", T, "recording.webm"), await c(_), t("idle");
        }, f.start(), t("recording");
      } catch (p) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", p), t("idle");
      }
  }, [a, c]), b = d === "recording" ? "Stop" : d === "uploading" ? "Uploading…" : "Record", k = d === "uploading";
  return /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: `tlAudioRecorder__button${d === "recording" ? " tlAudioRecorder__button--recording" : ""}`,
      onClick: v,
      disabled: k
    },
    b
  ), d !== "idle" && d !== "recording" && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status" }, d), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, W = () => {
  const n = g(), c = D(), a = !!n.hasAudio, [t, s] = e.useState(a ? "idle" : "disabled"), l = e.useRef(null), r = e.useRef(null);
  e.useEffect(() => {
    a ? t === "disabled" && s("idle") : (l.current && (l.current.pause(), l.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null), s("disabled"));
  }, [a]), e.useEffect(() => () => {
    l.current && (l.current.pause(), l.current = null), r.current && (URL.revokeObjectURL(r.current), r.current = null);
  }, []);
  const u = e.useCallback(async () => {
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
    if (!r.current) {
      s("loading");
      try {
        const b = await fetch(c);
        if (!b.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", b.status), s("idle");
          return;
        }
        const k = await b.blob();
        r.current = URL.createObjectURL(k);
      } catch (b) {
        console.error("[TLAudioPlayer] Fetch error:", b), s("idle");
        return;
      }
    }
    const v = new Audio(r.current);
    l.current = v, v.onended = () => {
      s("idle");
    }, v.play(), s("playing");
  }, [t, c]), i = t === "loading" ? "Loading…" : t === "playing" ? "Pause" : "Play", d = t === "disabled" || t === "loading";
  return /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: `tlAudioPlayer__button${t === "playing" ? " tlAudioPlayer__button--playing" : ""}`,
      onClick: u,
      disabled: d
    },
    i
  ), t === "loading" && /* @__PURE__ */ e.createElement("span", { className: "tlAudioPlayer__status" }, "Loading\\u2026"));
}, X = () => {
  const n = g(), c = N(), [a, t] = e.useState("idle"), [s, l] = e.useState(!1), r = e.useRef(null), u = n.status ?? "idle", i = n.error, d = n.accept ?? "", v = u === "received" ? "idle" : a !== "idle" ? a : u, b = e.useCallback(async (o) => {
    t("uploading");
    const C = new FormData();
    C.append("file", o, o.name), await c(C), t("idle");
  }, [c]), k = e.useCallback((o) => {
    var L;
    const C = (L = o.target.files) == null ? void 0 : L[0];
    C && b(C);
  }, [b]), p = e.useCallback(() => {
    var o;
    a !== "uploading" && ((o = r.current) == null || o.click());
  }, [a]), E = e.useCallback((o) => {
    o.preventDefault(), o.stopPropagation(), l(!0);
  }, []), f = e.useCallback((o) => {
    o.preventDefault(), o.stopPropagation(), l(!1);
  }, []), T = e.useCallback((o) => {
    var L;
    if (o.preventDefault(), o.stopPropagation(), l(!1), a === "uploading") return;
    const C = (L = o.dataTransfer.files) == null ? void 0 : L[0];
    C && b(C);
  }, [a, b]), _ = v === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: E,
      onDragLeave: f,
      onDrop: T
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: r,
        type: "file",
        accept: d || void 0,
        onChange: k,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: p,
        disabled: _
      },
      v === "uploading" ? "Uploading…" : "Choose File"
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
};
m("TLButton", j);
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
