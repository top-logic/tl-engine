import { React as e, useTLFieldValue as E, getComponent as S, useTLState as T, useTLCommand as f, TLChild as N, useTLUpload as y, register as m } from "tl-react-bridge";
const { useCallback: D } = e, F = ({ state: t }) => {
  const [l, a] = E(), n = D(
    (s) => {
      a(s.target.value);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: l ?? "",
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: B } = e, w = ({ state: t, config: l }) => {
  const [a, n] = E(), s = B(
    (o) => {
      const u = o.target.value, d = u === "" ? null : Number(u);
      n(d);
    },
    [n]
  ), c = l != null && l.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: a != null ? String(a) : "",
      onChange: s,
      step: c,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: U } = e, A = ({ state: t }) => {
  const [l, a] = E(), n = U(
    (s) => {
      a(s.target.value || null);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: l ?? "",
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: I } = e, $ = ({ state: t, config: l }) => {
  const [a, n] = E(), s = I(
    (o) => {
      n(o.target.value || null);
    },
    [n]
  ), c = t.options ?? (l == null ? void 0 : l.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: a ?? "",
      onChange: s,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    c.map((o) => /* @__PURE__ */ e.createElement("option", { key: o.value, value: o.value }, o.label))
  );
}, { useCallback: x } = e, P = ({ state: t }) => {
  const [l, a] = E(), n = x(
    (s) => {
      a(s.target.checked);
    },
    [a]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: l === !0,
      onChange: n,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, V = ({ controlId: t, state: l }) => {
  const a = l.columns ?? [], n = l.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, c) => /* @__PURE__ */ e.createElement("tr", { key: c }, a.map((o) => {
    const u = o.cellModule ? S(o.cellModule) : void 0, d = s[o.name];
    if (u) {
      const i = { value: d, editable: l.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        u,
        {
          controlId: t + "-" + c + "-" + o.name,
          state: i
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, d != null ? String(d) : "");
  })))));
}, { useCallback: M } = e, O = ({ command: t, label: l, disabled: a }) => {
  const n = T(), s = f(), c = t ?? "click", o = l ?? n.label, u = a ?? n.disabled === !0, d = M(() => {
    s(c);
  }, [s, c]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: d,
      disabled: u,
      className: "tlReactButton"
    },
    o
  );
}, { useCallback: z } = e, j = ({ command: t, label: l, active: a, disabled: n }) => {
  const s = T(), c = f(), o = t ?? "click", u = l ?? s.label, d = a ?? s.active === !0, i = n ?? s.disabled === !0, k = z(() => {
    c(o);
  }, [c, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: k,
      disabled: i,
      className: "tlReactButton" + (d ? " tlReactButtonActive" : "")
    },
    u
  );
}, q = () => {
  const t = T(), l = f(), a = t.count ?? 0, n = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: G } = e, H = () => {
  const t = T(), l = f(), a = t.tabs ?? [], n = t.activeTabId, s = G((c) => {
    c !== n && l("selectTab", { tabId: c });
  }, [l, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, a.map((c) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: c.id,
      role: "tab",
      "aria-selected": c.id === n,
      className: "tlReactTabBar__tab" + (c.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => s(c.id)
    },
    c.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(N, { control: t.activeContent })));
}, J = () => {
  const t = T(), l = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((n, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(N, { control: n })))));
}, K = () => {
  const t = T(), l = y(), [a, n] = e.useState("idle"), s = e.useRef(null), c = e.useRef([]), o = e.useRef(null), u = t.status ?? "idle", d = t.error, i = u === "received" ? "idle" : a !== "idle" ? a : u, k = e.useCallback(async () => {
    if (a === "recording") {
      const b = s.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = b, c.current = [];
        const L = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", v = new MediaRecorder(b, L ? { mimeType: L } : void 0);
        s.current = v, v.ondataavailable = (C) => {
          C.data.size > 0 && c.current.push(C.data);
        }, v.onstop = async () => {
          b.getTracks().forEach((r) => r.stop()), o.current = null;
          const C = new Blob(c.current, { type: v.mimeType || "audio/webm" });
          if (c.current = [], C.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const _ = new FormData();
          _.append("audio", C, "recording.webm"), await l(_), n("idle");
        }, v.start(), n("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), n("idle");
      }
  }, [a, l]), g = i === "recording" ? "Stop" : i === "uploading" ? "Uploading…" : "Record", R = i === "uploading";
  return /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: `tlAudioRecorder__button${i === "recording" ? " tlAudioRecorder__button--recording" : ""}`,
      onClick: k,
      disabled: R
    },
    g
  ), i !== "idle" && i !== "recording" && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status" }, i), d && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, d));
}, Q = () => {
  const t = T(), l = y(), [a, n] = e.useState("idle"), [s, c] = e.useState(!1), o = e.useRef(null), u = t.status ?? "idle", d = t.error, i = t.accept ?? "", k = u === "received" ? "idle" : a !== "idle" ? a : u, g = e.useCallback(async (r) => {
    n("uploading");
    const p = new FormData();
    p.append("file", r, r.name), await l(p), n("idle");
  }, [l]), R = e.useCallback((r) => {
    var h;
    const p = (h = r.target.files) == null ? void 0 : h[0];
    p && g(p);
  }, [g]), b = e.useCallback(() => {
    var r;
    a !== "uploading" && ((r = o.current) == null || r.click());
  }, [a]), L = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), c(!0);
  }, []), v = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), c(!1);
  }, []), C = e.useCallback((r) => {
    var h;
    if (r.preventDefault(), r.stopPropagation(), c(!1), a === "uploading") return;
    const p = (h = r.dataTransfer.files) == null ? void 0 : h[0];
    p && g(p);
  }, [a, g]), _ = k === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${s ? " tlFileUpload--dragover" : ""}`,
      onDragOver: L,
      onDragLeave: v,
      onDrop: C
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: o,
        type: "file",
        accept: i || void 0,
        onChange: R,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: b,
        disabled: _
      },
      k === "uploading" ? "Uploading…" : "Choose File"
    ),
    d && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, d)
  );
};
m("TLButton", O);
m("TLToggleButton", j);
m("TLTextInput", F);
m("TLNumberInput", w);
m("TLDatePicker", A);
m("TLSelect", $);
m("TLCheckbox", P);
m("TLTable", V);
m("TLCounter", q);
m("TLTabBar", H);
m("TLFieldList", J);
m("TLAudioRecorder", K);
m("TLFileUpload", Q);
