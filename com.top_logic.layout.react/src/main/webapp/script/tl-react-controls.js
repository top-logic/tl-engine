import { React as e, useTLFieldValue as h, getComponent as w, useTLState as T, useTLCommand as _, TLChild as y, useTLUpload as S, register as m } from "tl-react-bridge";
const { useCallback: U } = e, A = ({ state: t }) => {
  const [l, a] = h(), n = U(
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
}, { useCallback: I } = e, $ = ({ state: t, config: l }) => {
  const [a, n] = h(), s = I(
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
}, { useCallback: x } = e, P = ({ state: t }) => {
  const [l, a] = h(), n = x(
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
}, { useCallback: V } = e, M = ({ state: t, config: l }) => {
  const [a, n] = h(), s = V(
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
}, { useCallback: O } = e, z = ({ state: t }) => {
  const [l, a] = h(), n = O(
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
}, j = ({ controlId: t, state: l }) => {
  const a = l.columns ?? [], n = l.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, a.map((s) => /* @__PURE__ */ e.createElement("th", { key: s.name }, s.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((s, c) => /* @__PURE__ */ e.createElement("tr", { key: c }, a.map((o) => {
    const u = o.cellModule ? w(o.cellModule) : void 0, d = s[o.name];
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
}, { useCallback: q } = e, G = ({ command: t, label: l, disabled: a }) => {
  const n = T(), s = _(), c = t ?? "click", o = l ?? n.label, u = a ?? n.disabled === !0, d = q(() => {
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
}, { useCallback: H } = e, J = ({ command: t, label: l, active: a, disabled: n }) => {
  const s = T(), c = _(), o = t ?? "click", u = l ?? s.label, d = a ?? s.active === !0, i = n ?? s.disabled === !0, k = H(() => {
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
}, K = () => {
  const t = T(), l = _(), a = t.count ?? 0, n = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, a), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => l("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: Q } = e, W = () => {
  const t = T(), l = _(), a = t.tabs ?? [], n = t.activeTabId, s = Q((c) => {
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
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(y, { control: t.activeContent })));
}, X = () => {
  const t = T(), l = t.title, a = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, l && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, a.map((n, s) => /* @__PURE__ */ e.createElement("div", { key: s, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(y, { control: n })))));
}, Y = () => {
  const t = T(), l = S(), [a, n] = e.useState("idle"), s = e.useRef(null), c = e.useRef([]), o = e.useRef(null), u = t.status ?? "idle", d = t.error, i = u === "received" ? "idle" : a !== "idle" ? a : u, k = e.useCallback(async () => {
    if (a === "recording") {
      const b = s.current;
      b && b.state !== "inactive" && b.stop();
      return;
    }
    if (a !== "uploading")
      try {
        const b = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = b, c.current = [];
        const g = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", p = new MediaRecorder(b, g ? { mimeType: g } : void 0);
        s.current = p, p.ondataavailable = (C) => {
          C.data.size > 0 && c.current.push(C.data);
        }, p.onstop = async () => {
          b.getTracks().forEach((R) => R.stop()), o.current = null;
          const C = new Blob(c.current, { type: p.mimeType || "audio/webm" });
          if (c.current = [], C.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const L = new FormData();
          L.append("audio", C, "recording.webm"), await l(L), n("idle");
        }, p.start(), n("recording");
      } catch (b) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", b), n("idle");
      }
  }, [a, l]), f = i === "recording" ? "Stop" : i === "uploading" ? "Uploading…" : "Record", N = i === "uploading";
  return /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: `tlAudioRecorder__button${i === "recording" ? " tlAudioRecorder__button--recording" : ""}`,
      onClick: k,
      disabled: N
    },
    f
  ), i !== "idle" && i !== "recording" && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status" }, i), d && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, d));
}, Z = () => {
  const t = T(), l = S(), [a, n] = e.useState("idle"), [s, c] = e.useState(null), [o, u] = e.useState(!1), d = e.useRef(null), i = t.status ?? "idle", k = t.error, f = t.fileName, N = t.accept ?? "", b = i === "received" ? "idle" : a !== "idle" ? a : i, g = s ?? f, p = e.useCallback(async (r) => {
    c(r.name), n("uploading");
    const v = new FormData();
    v.append("file", r, r.name), await l(v), n("idle");
  }, [l]), C = e.useCallback((r) => {
    var E;
    const v = (E = r.target.files) == null ? void 0 : E[0];
    v && p(v);
  }, [p]), L = e.useCallback(() => {
    var r;
    a !== "uploading" && ((r = d.current) == null || r.click());
  }, [a]), R = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), u(!0);
  }, []), D = e.useCallback((r) => {
    r.preventDefault(), r.stopPropagation(), u(!1);
  }, []), F = e.useCallback((r) => {
    var E;
    if (r.preventDefault(), r.stopPropagation(), u(!1), a === "uploading") return;
    const v = (E = r.dataTransfer.files) == null ? void 0 : E[0];
    v && p(v);
  }, [a, p]), B = b === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${o ? " tlFileUpload--dragover" : ""}`,
      onDragOver: R,
      onDragLeave: D,
      onDrop: F
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: d,
        type: "file",
        accept: N || void 0,
        onChange: C,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: L,
        disabled: B
      },
      b === "uploading" ? "Uploading…" : "Choose File"
    ),
    g && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__fileName" }, g),
    k && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, k)
  );
};
m("TLButton", G);
m("TLToggleButton", J);
m("TLTextInput", A);
m("TLNumberInput", $);
m("TLDatePicker", P);
m("TLSelect", M);
m("TLCheckbox", z);
m("TLTable", j);
m("TLCounter", K);
m("TLTabBar", W);
m("TLFieldList", X);
m("TLAudioRecorder", Y);
m("TLFileUpload", Z);
