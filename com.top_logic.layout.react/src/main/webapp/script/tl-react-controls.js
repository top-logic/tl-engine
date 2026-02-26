import { React as e, useTLFieldValue as v, getComponent as N, useTLState as b, useTLCommand as T, TLChild as L, useTLUpload as y, register as d } from "tl-react-bridge";
const { useCallback: f } = e, S = ({ state: t }) => {
  const [a, n] = v(), l = f(
    (c) => {
      n(c.target.value);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: a ?? "",
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: B } = e, w = ({ state: t, config: a }) => {
  const [n, l] = v(), c = B(
    (o) => {
      const r = o.target.value, u = r === "" ? null : Number(r);
      l(u);
    },
    [l]
  ), s = a != null && a.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: n != null ? String(n) : "",
      onChange: c,
      step: s,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: A } = e, D = ({ state: t }) => {
  const [a, n] = v(), l = A(
    (c) => {
      n(c.target.value || null);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: a ?? "",
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: F } = e, $ = ({ state: t, config: a }) => {
  const [n, l] = v(), c = F(
    (o) => {
      l(o.target.value || null);
    },
    [l]
  ), s = t.options ?? (a == null ? void 0 : a.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: n ?? "",
      onChange: c,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    s.map((o) => /* @__PURE__ */ e.createElement("option", { key: o.value, value: o.value }, o.label))
  );
}, { useCallback: x } = e, I = ({ state: t }) => {
  const [a, n] = v(), l = x(
    (c) => {
      n(c.target.checked);
    },
    [n]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: a === !0,
      onChange: l,
      disabled: t.disabled === !0 || t.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, V = ({ controlId: t, state: a }) => {
  const n = a.columns ?? [], l = a.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, n.map((c) => /* @__PURE__ */ e.createElement("th", { key: c.name }, c.label)))), /* @__PURE__ */ e.createElement("tbody", null, l.map((c, s) => /* @__PURE__ */ e.createElement("tr", { key: s }, n.map((o) => {
    const r = o.cellModule ? N(o.cellModule) : void 0, u = c[o.name];
    if (r) {
      const i = { value: u, editable: a.editable };
      return /* @__PURE__ */ e.createElement("td", { key: o.name }, /* @__PURE__ */ e.createElement(
        r,
        {
          controlId: t + "-" + s + "-" + o.name,
          state: i
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: o.name }, u != null ? String(u) : "");
  })))));
}, { useCallback: M } = e, P = ({ command: t, label: a, disabled: n }) => {
  const l = b(), c = T(), s = t ?? "click", o = a ?? l.label, r = n ?? l.disabled === !0, u = M(() => {
    c(s);
  }, [c, s]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: u,
      disabled: r,
      className: "tlReactButton"
    },
    o
  );
}, { useCallback: U } = e, z = ({ command: t, label: a, active: n, disabled: l }) => {
  const c = b(), s = T(), o = t ?? "click", r = a ?? c.label, u = n ?? c.active === !0, i = l ?? c.disabled === !0, k = U(() => {
    s(o);
  }, [s, o]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: k,
      disabled: i,
      className: "tlReactButton" + (u ? " tlReactButtonActive" : "")
    },
    r
  );
}, O = () => {
  const t = b(), a = T(), n = t.count ?? 0, l = t.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, l), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, n), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => a("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: j } = e, q = () => {
  const t = b(), a = T(), n = t.tabs ?? [], l = t.activeTabId, c = j((s) => {
    s !== l && a("selectTab", { tabId: s });
  }, [a, l]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, n.map((s) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: s.id,
      role: "tab",
      "aria-selected": s.id === l,
      className: "tlReactTabBar__tab" + (s.id === l ? " tlReactTabBar__tab--active" : ""),
      onClick: () => c(s.id)
    },
    s.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, t.activeContent && /* @__PURE__ */ e.createElement(L, { control: t.activeContent })));
}, G = () => {
  const t = b(), a = t.title, n = t.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, a && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, a), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, n.map((l, c) => /* @__PURE__ */ e.createElement("div", { key: c, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(L, { control: l })))));
}, H = () => {
  const t = b(), a = y(), [n, l] = e.useState("idle"), c = e.useRef(null), s = e.useRef([]), o = e.useRef(null), r = t.status ?? "idle", u = t.error, i = r === "received" ? "idle" : n !== "idle" ? n : r, k = e.useCallback(async () => {
    if (n === "recording") {
      const m = c.current;
      m && m.state !== "inactive" && m.stop();
      return;
    }
    if (n !== "uploading")
      try {
        const m = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        o.current = m, s.current = [];
        const E = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", p = new MediaRecorder(m, E ? { mimeType: E } : void 0);
        c.current = p, p.ondataavailable = (C) => {
          C.data.size > 0 && s.current.push(C.data);
        }, p.onstop = async () => {
          m.getTracks().forEach((g) => g.stop()), o.current = null;
          const C = new Blob(s.current, { type: p.mimeType || "audio/webm" });
          if (s.current = [], C.size === 0) {
            l("idle");
            return;
          }
          l("uploading");
          const h = new FormData();
          h.append("audio", C, "recording.webm"), await a(h), l("idle");
        }, p.start(), l("recording");
      } catch (m) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", m), l("idle");
      }
  }, [n, a]), _ = i === "recording" ? "Stop" : i === "uploading" ? "Uploading…" : "Record", R = i === "uploading";
  return /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: `tlAudioRecorder__button${i === "recording" ? " tlAudioRecorder__button--recording" : ""}`,
      onClick: k,
      disabled: R
    },
    _
  ), i !== "idle" && i !== "recording" && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status" }, i), u && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, u));
};
d("TLButton", P);
d("TLToggleButton", z);
d("TLTextInput", S);
d("TLNumberInput", w);
d("TLDatePicker", D);
d("TLSelect", $);
d("TLCheckbox", I);
d("TLTable", V);
d("TLCounter", O);
d("TLTabBar", q);
d("TLFieldList", G);
d("TLAudioRecorder", H);
